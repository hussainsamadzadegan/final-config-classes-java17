package finalconfigclasses.cfg.web;

import finalconfigclasses.cfg.ConfigBean;
import finalconfigclasses.cfg.ConfigException;
import finalconfigclasses.cfg.Registry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    @GetMapping("/{className}")
    public ResponseEntity<Map<String, Object>> load(@PathVariable String className) {
        try {
            ConfigBean bean = Registry.getInstance().getConfig(className);
            if (bean == null) {
                return ResponseEntity.notFound().build();
            }
            try {
                bean.load();
            } catch (ConfigException e) {
                throw new RuntimeException("Failed to load config: " + className, e);
            }
            return ResponseEntity.ok(extractAttributes(bean));
        } catch (ConfigException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{className}")
    public ResponseEntity<Void> save(@PathVariable String className,
                                     @RequestBody Map<String, Object> values) {
        try {
            ConfigBean bean = Registry.getInstance().getConfig(className);
            if (bean == null) {
                return ResponseEntity.notFound().build();
            }
            applyAttributes(bean, values);
            try {
                bean.save();
            } catch (ConfigException e) {
                throw new RuntimeException("Failed to save config: " + className, e);
            }
            return ResponseEntity.ok().build();
        } catch (ConfigException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private Map<String, Object> extractAttributes(ConfigBean bean) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            BeanInfo info = Introspector.getBeanInfo(bean.getClass(), Object.class);
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                String name = pd.getName();
                if (EXCLUDED.contains(name) || name.startsWith("_")) continue;
                Method getter = pd.getReadMethod();
                if (getter == null) continue;
                result.put(name, getter.invoke(bean));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed reading attributes of " + bean.getClass(), e);
        }
        return result;
    }

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
                        if (item instanceof Map) applyAttributes(elem, (Map<String, Object>) item);
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
            // primitive arrays can't be null; return empty array instead
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
