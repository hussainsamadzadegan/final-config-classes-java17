package finalconfigclasses.cfg.engine;

import finalconfigclasses.cfg.model.Attribute;
import finalconfigclasses.cfg.model.ConfigClass;
import finalconfigclasses.cfg.model.Property;

/**
 * Helper functions exposed to the Velocity templates (confclass-template.vm,
 * diffhelper-template.vm, array-template.vm, flexible/rigid-lodsav-template.vm)
 * used by the code generator. Logic is unchanged from the original Java 8
 * version; only the import of the description-model classes was updated
 * (they moved from the now-removed JAXB-based {@code finalconfigclasses.cfg.jaxb}
 * package to the plain-POJO {@code finalconfigclasses.cfg.model} package).
 */
public final class Utils {

	public static String firstCharUp(String str) {
		if (str == null)
			return null;
		if (str.length() == 0)
			return str;
		StringBuilder sb = new StringBuilder(str);
		char c = str.charAt(0);
		sb.setCharAt(0, Character.toUpperCase(c));
		return sb.toString();
	}

	public static String wrapper(String type) {
		if ("boolean".equals(type))
			return "Boolean";
		if ("char".equals(type))
			return "Character";
		if ("byte".equals(type))
			return "Byte";
		if ("short".equals(type))
			return "Short";
		if ("int".equals(type))
			return "Integer";
		if ("long".equals(type))
			return "Long";
		if ("float".equals(type))
			return "Float";
		if ("double".equals(type))
			return "Double";
		return type;
	}

	public static String pcGetter(String type) {
		if ("boolean".equals(type)
			|| "Boolean".equals(type))
			return "getBoolean";
		if ("char".equals(type)
				|| "Character".equals(type))
				return "getCharacter";
		if ("byte".equals(type)
				|| "Byte".equals(type))
				return "getByte";
		if ("short".equals(type)
				|| "Short".equals(type))
				return "getShort";
		if ("int".equals(type)
				|| "Integer".equals(type))
				return "getInteger";
		if ("long".equals(type)
				|| "Long".equals(type))
				return "getLong";
		if ("float".equals(type)
				|| "Float".equals(type))
				return "getFloat";
		if ("double".equals(type)
				|| "Double".equals(type))
				return "getDouble";
		if ("String".equals(type))
				return "getString";
		return type;
	}

	public static boolean isAttribute(Object obj) {
		return obj instanceof Attribute;
	}

	public static String realType(Object obj) {
		if (obj instanceof Attribute) {
			Attribute attr = (Attribute) obj;
			return attr.getType();
		} else if (obj instanceof Property) {
			Property prop = (Property) obj;
			return prop.getType();
		} else {
			return "";
		}
	}

	public static String getterMethod(Object obj) {
		if (obj instanceof Attribute) {
			return "getAttr";
		} else if (obj instanceof Property) {
			return "getProp";
		} else {
			return "";
		}
	}

	public static String setterMethod(Object obj) {
		if (obj instanceof Attribute) {
			return "setAttr";
		} else if (obj instanceof Property) {
			return "setProp";
		} else {
			return "";
		}
	}

	public static String getClass(String type) {
		return type + ".class";
	}

	public static String getComponentType(String type) {
		return type + "[].class.getComponentType()";
	}

	public static String getPackage(String fqn) {
		if (fqn == null)
			return null;
		int lastDot = fqn.lastIndexOf('.');
		if (lastDot == -1)
			return fqn;
		return fqn.substring(0, lastDot);
	}

	public static boolean isNullableType(String type) {
		if ("boolean".equals(type))
			return false;
		if ("char".equals(type))
			return false;
		if ("byte".equals(type))
			return false;
		if ("short".equals(type))
			return false;
		if ("int".equals(type))
			return false;
		if ("long".equals(type))
			return false;
		if ("float".equals(type))
			return false;
		if ("double".equals(type))
			return false;
		if ("String".equals(type))
			return true;
		return true;
	}

	public static String appendDiffHelperString(String className) {
		if (className == null)
			return null;
		return className + "DiffHelper";
	}

	public static String baseDiffHelper(ConfigClass configClass) {
		String className = configClass.getName();
		if (className == null)
			return null;
		String baseClass = configClass.getExtends();
		if ("BaseConfigBean".equals(baseClass)) {
			return "ConfigDiffHelper";
		} else {
			return baseClass + "DiffHelper";
		}
	}
}
