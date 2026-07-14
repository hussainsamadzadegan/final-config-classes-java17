package finalconfigclasses.cfg.web;

import finalconfigclasses.cfg.model.Attribute;
import finalconfigclasses.cfg.model.ConfigClass;
import finalconfigclasses.cfg.model.ConfigClasses;
import finalconfigclasses.cfg.model.Property;

import java.util.*;

/**
 * Resolves the raw {@link ConfigClasses} description model (as parsed from
 * config-classes.xml by {@link finalconfigclasses.cfg.model.ConfigModelIO})
 * into a flat, frontend-ready contract:
 * <ul>
 *   <li>{@code extends} chains are walked and flattened, so a subclass's
 *       resolved attribute/property list already includes everything it
 *       inherits (with the subclass's own fields taking precedence over an
 *       inherited field of the same name).</li>
 *   <li>Nullable-suffix types ("int?", "bool?", ...) are split into a plain
 *       {@code baseType} plus a {@code nullable} flag, so the frontend never
 *       has to parse the type string itself.</li>
 * </ul>
 * The result is plain data (no behavior), safe to serialize directly to JSON
 * by Jackson in {@link ConfigModelController}.
 */
public final class ConfigModelResolver {

    private ConfigModelResolver() {
    }

    public static List<ResolvedConfigClass> resolveAll(ConfigClasses classes) {
        Map<String, ConfigClass> byName = new HashMap<>();
        for (ConfigClass c : classes.getConfigClass()) {
            byName.put(c.getName(), c);
        }
        List<ResolvedConfigClass> result = new ArrayList<>();
        for (ConfigClass c : classes.getConfigClass()) {
            result.add(resolve(c, byName));
        }
        return result;
    }

    private static ResolvedConfigClass resolve(ConfigClass c, Map<String, ConfigClass> byName) {
        LinkedHashMap<String, ResolvedAttribute> attrs = new LinkedHashMap<>();
        LinkedHashMap<String, ResolvedProperty> props = new LinkedHashMap<>();

        // Walk the extends chain root-first, so a subclass's own fields
        // (applied last) can override an inherited one with the same name.
        Deque<ConfigClass> chain = new ArrayDeque<>();
        ConfigClass cur = c;
        int guard = 0;
        while (cur != null && guard++ < 64) { // guard against an accidental extends cycle
            chain.push(cur);
            String parentName = cur.getExtends();
            cur = "BaseConfigBean".equals(parentName) ? null : byName.get(parentName);
        }
        for (ConfigClass link : chain) {
            for (Attribute a : link.getAttribute()) {
                attrs.put(a.getName(), toResolvedAttribute(a));
            }
            for (Property p : link.getProperty()) {
                props.put(p.getName(), toResolvedProperty(p));
            }
        }

        ResolvedConfigClass rc = new ResolvedConfigClass();
        rc.name = c.getName();
        rc.key = c.getKey();
        rc.attributes = new ArrayList<>(attrs.values());
        rc.properties = new ArrayList<>(props.values());
        return rc;
    }

    private static ResolvedAttribute toResolvedAttribute(Attribute a) {
        ResolvedAttribute ra = new ResolvedAttribute();
        ra.name = a.getName();
        ra.key = a.getKey();
        ra.isArray = "true".equals(a.getIsArray());
        String rawType = a.getType();
        ra.nullable = rawType != null && rawType.endsWith("?");
        ra.baseType = ra.nullable ? rawType.substring(0, rawType.length() - 1) : rawType;
        return ra;
    }

    private static ResolvedProperty toResolvedProperty(Property p) {
        ResolvedProperty rp = new ResolvedProperty();
        rp.name = p.getName();
        rp.refClassName = p.getType(); // name of another ConfigClass
        rp.isArray = "true".equals(p.getIsArray());
        return rp;
    }

    /** Frontend-facing shape of one resolved config-class. */
    public static class ResolvedConfigClass {
        public String name;
        public String key;
        public List<ResolvedAttribute> attributes;
        public List<ResolvedProperty> properties;
    }

    /** Frontend-facing shape of one resolved simple-value field. */
    public static class ResolvedAttribute {
        public String name;
        public String key;
        public String baseType; // String | bool | char | short | int | long | float | double
        public boolean isArray;
        public boolean nullable;
    }

    /** Frontend-facing shape of one resolved nested-config-class reference. */
    public static class ResolvedProperty {
        public String name;
        public String refClassName;
        public boolean isArray;
    }
}
