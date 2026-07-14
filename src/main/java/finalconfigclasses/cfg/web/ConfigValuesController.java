package finalconfigclasses.cfg.web;

import finalconfigclasses.cfg.ConfigBean;
import finalconfigclasses.cfg.ConfigException;
import finalconfigclasses.cfg.Registry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
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
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                byName.put(pd.getName(), pd);
            }
            for (Map.Entry<String, Object> e : values.entrySet()) {
                PropertyDescriptor pd = byName.get(e.getKey());
                if (pd == null || EXCLUDED.contains(pd.getName())) continue;
                Method setter = pd.getWriteMethod();
                if (setter == null) continue;
                setter.invoke(bean, convert(e.getValue(), pd.getPropertyType()));
            }
        } catch (InvocationTargetException ite) {
            throw new RuntimeException("Rejected value while applying attributes on "
                    + bean.getClass(), ite.getCause());
        } catch (Exception e) {
            throw new RuntimeException("Failed applying attributes on " + bean.getClass(), e);
        }
    }

    private Object convert(Object raw, Class<?> targetType) {
        if (raw == null || targetType.isInstance(raw)) return raw;
        String s = String.valueOf(raw);
        if (targetType == int.class || targetType == Integer.class) return Integer.valueOf(s);
        if (targetType == long.class || targetType == Long.class) return Long.valueOf(s);
        if (targetType == double.class || targetType == Double.class) return Double.valueOf(s);
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.valueOf(s);
        return s;
    }
}
