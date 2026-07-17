package finalconfigclasses.cfg.web;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import finalconfigclasses.cfg.ConfigBean;
import finalconfigclasses.cfg.ConfigException;
import finalconfigclasses.cfg.Registry;
import finalconfigclasses.cfg.gen.BankConfigImpl;
import finalconfigclasses.cfg.misc.LoadAllVisitor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/config-values")
public class ConfigValuesController {

    // properties we never want to expose/accept via REST
    private static final Set<String> EXCLUDED = Set.of(
            "class", "propertyChangeListeners", "nodeChangeListeners",
            "beanUpdateListeners", "propertiesLock"
    );

    private static final int MAX_DEPTH = 32;

    @GetMapping("/{className}")
    public ResponseEntity<Map<String, Object>> load(@PathVariable("className") String className) {
        try {
            ConfigBean bean = Registry.getInstance().getConfig(className);
            if (bean == null) {
                return ResponseEntity.notFound().build();
            }
            try {
                bean.accept(new LoadAllVisitor());
            } catch (Exception e) {
                throw new RuntimeException("Failed to load config: " + className, e);
            }
            return ResponseEntity.ok(extractAttributes(bean));
        } catch (ConfigException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{className}")
    public ResponseEntity<Void> save(@PathVariable("className") String className,
                                     @RequestBody Map<String, Object> values) {
        try {
            ConfigBean bean = new BankConfigImpl();
            if (bean == null) {
                return ResponseEntity.notFound().build();
            }

            // Apply changes to a proposed bean
            applyAttributes(bean, values);

            // === SAVE FULL OBJECT FOR BANKCONFIGIMPL ===
            if (bean instanceof BankConfigImpl) {
                XStream xstream = new XStream(new StaxDriver());
                String fullXml = xstream.toXML(bean);

                System.out.println("Saving FULL BankConfigImpl as XML to ZooKeeper");

                // Save full bean
                Registry.getInstance().importConfig("BankConfigImpl", fullXml, true);
            } else {
                // For other beans, keep snapshot behavior
                bean.save();
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }    // ---- read path: recursive extraction so EXCLUDED is enforced at every depth ----

    private Map<String, Object> extractAttributes(ConfigBean bean) {
        return extractAttributes(bean, new IdentityHashMap<>(), 0);
    }

    private Map<String, Object> extractAttributes(ConfigBean bean, Map<Object, Object> seen, int depth) {
        if (bean == null) {
            return null;
        }
        if (depth > MAX_DEPTH) {
            throw new RuntimeException("ConfigBean hierarchy too deep (possible cycle) at " + bean.getClass());
        }
        if (seen.containsKey(bean)) {
            // Cyclic reference back to an ancestor bean - don't recurse again.
            Map<String, Object> cyclic = new LinkedHashMap<>();
            cyclic.put("$ref", bean.getClass().getName());
            return cyclic;
        }
        seen.put(bean, Boolean.TRUE);

        Map<String, Object> result = new LinkedHashMap<>();
        try {
            BeanInfo info = Introspector.getBeanInfo(bean.getClass(), Object.class);
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                String name = pd.getName();
                if (EXCLUDED.contains(name) || name.startsWith("_")) continue;
                Method getter = pd.getReadMethod();
                if (getter == null) continue;

                Object rawValue = getter.invoke(bean);
                result.put(name, extractValue(rawValue, seen, depth + 1));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed reading attributes of " + bean.getClass(), e);
        } finally {
            seen.remove(bean);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object extractValue(Object value, Map<Object, Object> seen, int depth) {
        if (value == null) {
            return null;
        }

        // Nested config bean -> recurse into its own filtered attribute map
        if (value instanceof ConfigBean) {
            return extractAttributes((ConfigBean) value, seen, depth);
        }

        // Array (of config beans or plain values) -> List, recursing element-wise
        if (value.getClass().isArray()) {
            int len = Array.getLength(value);
            List<Object> list = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                list.add(extractValue(Array.get(value, i), seen, depth + 1));
            }
            return list;
        }

        // Collection -> List, recursing element-wise
        if (value instanceof Collection) {
            List<Object> list = new ArrayList<>(((Collection<?>) value).size());
            for (Object element : (Collection<Object>) value) {
                list.add(extractValue(element, seen, depth + 1));
            }
            return list;
        }

        // Map -> recurse into values
        if (value instanceof Map) {
            Map<Object, Object> src = (Map<Object, Object>) value;
            Map<Object, Object> out = new LinkedHashMap<>();
            for (Map.Entry<Object, Object> e : src.entrySet()) {
                out.put(e.getKey(), extractValue(e.getValue(), seen, depth + 1));
            }
            return out;
        }

        // Primitive, String, enum, wrapper types, etc. -> leave as-is
        return value;
    }

    // ---- write path: unchanged from your existing recursive applyAttributes ----

    private void applyAttributes(ConfigBean bean, Map<String, Object> values) {
        try {
            BeanInfo info = Introspector.getBeanInfo(bean.getClass(), Object.class);
            Map<String, PropertyDescriptor> byName = new LinkedHashMap<>();
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) byName.put(pd.getName(), pd);

            for (Map.Entry<String, Object> e : values.entrySet()) {
                PropertyDescriptor pd = byName.get(e.getKey());
                if (pd == null || EXCLUDED.contains(pd.getName())) continue;
                Method setter = pd.getWriteMethod();
                if (setter == null) continue;

                Class<?> type = pd.getPropertyType();
                Object raw = e.getValue();
                Object arg;

                if (raw instanceof Map && ConfigBean.class.isAssignableFrom(type)) {
                    // nested ConfigBean: get existing instance and recurse
                    Method getter = pd.getReadMethod();
                    ConfigBean nested = getter != null ? (ConfigBean) getter.invoke(bean) : null;
                    if (nested == null) nested = (ConfigBean) type.getDeclaredConstructor().newInstance();
                    applyAttributes(nested, (Map<String, Object>) raw);
                    nested.save();
                    arg = nested;
                } else if (raw instanceof List && type.isArray()
                        && ConfigBean.class.isAssignableFrom(type.getComponentType())) {
                    // array of ConfigBean
                    List<?> list = (List<?>) raw;
                    Class<?> comp = type.getComponentType();
                    Object arr = Array.newInstance(comp, list.size());
                    for (int i = 0; i < list.size(); i++) {
                        Object item = list.get(i);
                        ConfigBean elem = (ConfigBean) comp.getDeclaredConstructor().newInstance();
                        if (item instanceof Map) {
                            applyAttributes(elem, (Map<String, Object>) item);
                            elem.save();
                        }
                        Array.set(arr, i, elem);
                    }
                    arg = arr;
                } else {
                    arg = convert(raw, type);
                }

                setter.invoke(bean, arg);
            }
        } catch (InvocationTargetException ite) {
            throw new RuntimeException("Rejected value while applying attributes on "
                    + bean.getClass(), ite.getCause());
        } catch (Exception e) {
            throw new RuntimeException("Failed applying attributes on " + bean.getClass(), e);
        }
    }

    private Object convert(Object raw, Class<?> targetType) {
        if (raw == null) {
            if (targetType.isArray() && targetType.getComponentType().isPrimitive()) {
                return java.lang.reflect.Array.newInstance(targetType.getComponentType(), 0);
            }
            return null;
        }
        if (targetType.isInstance(raw)) return raw;
        if (targetType.isArray() && raw instanceof java.util.List<?> list) {
            Class<?> comp = targetType.getComponentType();
            Object arr = java.lang.reflect.Array.newInstance(comp, list.size());
            for (int i = 0; i < list.size(); i++) {
                java.lang.reflect.Array.set(arr, i, convert(list.get(i), comp));
            }
            return arr;
        }
        String s = String.valueOf(raw);
        if (targetType == int.class    || targetType == Integer.class) return Integer.valueOf(s);
        if (targetType == long.class   || targetType == Long.class)    return Long.valueOf(s);
        if (targetType == double.class || targetType == Double.class)  return Double.valueOf(s);
        if (targetType == boolean.class|| targetType == Boolean.class) return Boolean.valueOf(s);
        return s;
    }
}