package finalconfigclasses.util;

import java.util.HashMap;
import java.util.NoSuchElementException;

public class ParameterMap extends HashMap<String, Object> {

	private static final long serialVersionUID = -2774080826427563908L;

	public void putObject(String key, Object value) {
		put(key, value);
	}
	
    public char getChar(String key)
    {
        Character b = getCharacter(key, null);
        if (b != null)
        {
            return b.charValue();
        }
        else
        {
            throw new NoSuchElementException("Doesn't map to an existing object");
        }
    }

    public char getChar(String key, char defaultValue)
    {
        return getCharacter(key, Character.valueOf(defaultValue)).charValue();
    }

    public Character getCharacter(String key, Character defaultValue)
    {
        Object value = get(key);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return (Character)value;
        }
    }

    public boolean getBoolean(String key)
    {
        Boolean b = getBoolean(key, null);
        if (b != null)
        {
            return b.booleanValue();
        }
        else
        {
            throw new NoSuchElementException("Doesn't map to an existing object");
        }
    }

    public boolean getBoolean(String key, boolean defaultValue)
    {
        return getBoolean(key, Boolean.valueOf(defaultValue)).booleanValue();
    }

    public Boolean getBoolean(String key, Boolean defaultValue)
    {
        Object value = get(key);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return (Boolean)value;
        }
    }

    public double getDouble(String key)
    {
        Double d = getDouble(key, null);
        if (d != null)
        {
            return d.doubleValue();
        }
        else
        {
            throw new NoSuchElementException("Doesn't map to an existing object");
        }
    }

    public double getDouble(String key, double defaultValue)
    {
        return getDouble(key,  Double.valueOf(defaultValue)).doubleValue();
    }

    public Double getDouble(String key, Double defaultValue)
    {
        Object value = get(key);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return (Double)value;
        }
    }

    public float getFloat(String key)
    {
        Float f = getFloat(key, null);
        if (f != null)
        {
            return f.floatValue();
        }
        else
        {
            throw new NoSuchElementException("Doesn't map to an existing object");
        }
    }

    public float getFloat(String key, float defaultValue)
    {
        return getFloat(key, Float.valueOf(defaultValue)).floatValue();
    }

    public Float getFloat(String key, Float defaultValue)
    {
        Object value = get(key);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return (Float)value;
        }
    }

    public short getShort(String key)
    {
        Short s = getShort(key, null);
        if (s != null)
        {
            return s.shortValue();
        }
        else
        {
            throw new NoSuchElementException("Doesn't map to an existing object");
        }
    }

    public short getShort(String key, short defaultValue)
    {
        return getShort(key, Short.valueOf(defaultValue)).shortValue();
    }

    public Short getShort(String key, Short defaultValue)
    {
        Object value = get(key);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return (Short)value;
        }
    }

    public long getLong(String key)
    {
        Long l = getLong(key, null);
        if (l != null)
        {
            return l.longValue();
        }
        else
        {
            throw new NoSuchElementException("Doesn't map to an existing object");
        }
    }

    public long getLong(String key, long defaultValue)
    {
        return getLong(key, Long.valueOf(defaultValue)).longValue();
    }

    public Long getLong(String key, Long defaultValue)
    {
        Object value = get(key);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return (Long)value;
        }
    }

    public int getInt(String key)
    {
        Integer i = getInteger(key, null);
        if (i != null)
        {
            return i.intValue();
        }
        else
        {
            throw new NoSuchElementException("Doesn't map to an existing object");
        }
    }

    public int getInt(String key, int defaultValue)
    {
        Integer i = getInteger(key, Integer.valueOf(defaultValue));

        if (i == null)
        {
            return defaultValue;
        }

        return i.intValue();
    }

    public Integer getInteger(String key, Integer defaultValue)
    {
        Object value = get(key);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return (Integer)value;
        }
    }
    
    public String getString(String key)
    {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue)
    {
        Object value = get(key);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return (String)value;
        }
    }
    	
}
