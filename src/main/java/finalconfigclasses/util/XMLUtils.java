package finalconfigclasses.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public final class XMLUtils {
	
    ////////////////////////////////////////
    // Simple attributes helpers
    ////////////////////////////////////////
	
    public static char getChar(Element xmlVal)
    {
        Character b = getCharacter(xmlVal, null);
        if (b != null)
        {
            return b.charValue();
        }
        else
        {
            throw new NoSuchElementException("Doesn't map to an existing object");
        }
    }

    public static char getChar(Element xmlVal, char defaultValue)
    {
        return getCharacter(xmlVal, Character.valueOf(defaultValue)).charValue();
    }

    public static Character getCharacter(Element xmlVal, Character defaultValue)
    {
        String value = get(xmlVal);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return Character.valueOf(value.charAt(0));
        }
    }

    public static boolean getBoolean(Element xmlVal)
    {
        Boolean b = getBoolean(xmlVal, null);
        if (b != null)
        {
            return b.booleanValue();
        }
        else
        {
            throw new NoSuchElementException("Doesn't map to an existing object");
        }
    }

    public static boolean getBoolean(Element xmlVal, boolean defaultValue)
    {
        return getBoolean(xmlVal, Boolean.valueOf(defaultValue)).booleanValue();
    }

    public static Boolean getBoolean(Element xmlVal, Boolean defaultValue)
    {
        String value = get(xmlVal);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return Boolean.valueOf(value);
        }
    }

    public static double getDouble(Element xmlVal)
    {
        Double d = getDouble(xmlVal, null);
        if (d != null)
        {
            return d.doubleValue();
        }
        else
        {
            throw new NoSuchElementException("Doesn't map to an existing object");
        }
    }

    public static double getDouble(Element xmlVal, double defaultValue)
    {
        return getDouble(xmlVal,  Double.valueOf(defaultValue)).doubleValue();
    }

    public static Double getDouble(Element xmlVal, Double defaultValue)
    {
        String value = get(xmlVal);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return Double.valueOf(value);
        }
    }

    public static float getFloat(Element xmlVal)
    {
        Float f = getFloat(xmlVal, null);
        if (f != null)
        {
            return f.floatValue();
        }
        else
        {
            throw new NoSuchElementException("Doesn't map to an existing object");
        }
    }

    public static float getFloat(Element xmlVal, float defaultValue)
    {
        return getFloat(xmlVal, Float.valueOf(defaultValue)).floatValue();
    }

    public static Float getFloat(Element xmlVal, Float defaultValue)
    {
        String value = get(xmlVal);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return Float.valueOf(value);
        }
    }

    public static short getShort(Element xmlVal)
    {
        Short s = getShort(xmlVal, null);
        if (s != null)
        {
            return s.shortValue();
        }
        else
        {
            throw new NoSuchElementException("Doesn't map to an existing object");
        }
    }

    public static short getShort(Element xmlVal, short defaultValue)
    {
        return getShort(xmlVal, Short.valueOf(defaultValue)).shortValue();
    }

    public static Short getShort(Element xmlVal, Short defaultValue)
    {
        String value = get(xmlVal);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return Short.valueOf(value);
        }
    }

    public static long getLong(Element xmlVal)
    {
        Long l = getLong(xmlVal, null);
        if (l != null)
        {
            return l.longValue();
        }
        else
        {
            throw new NoSuchElementException("Doesn't map to an existing object");
        }
    }

    public static long getLong(Element xmlVal, long defaultValue)
    {
        return getLong(xmlVal, Long.valueOf(defaultValue)).longValue();
    }

    public static Long getLong(Element xmlVal, Long defaultValue)
    {
        String value = get(xmlVal);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return Long.valueOf(value);
        }
    }

    public static int getInt(Element xmlVal)
    {
        Integer i = getInteger(xmlVal, null);
        if (i != null)
        {
            return i.intValue();
        }
        else
        {
            throw new NoSuchElementException("Doesn't map to an existing object");
        }
    }

    public static int getInt(Element xmlVal, int defaultValue)
    {
        Integer i = getInteger(xmlVal, Integer.valueOf(defaultValue));

        if (i == null)
        {
            return defaultValue;
        }

        return i.intValue();
    }

    public static Integer getInteger(Element xmlVal, Integer defaultValue)
    {
        String value = get(xmlVal);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return Integer.valueOf(value);
        }
    }
    
    public static String getString(Element xmlVal)
    {
        return getString(xmlVal, null);
    }

    public static String getString(Element xmlVal, String defaultValue)
    {
        String value = get(xmlVal);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return value;
        }
    }
    
    ////////////////////////////////////////
    // XML to Attributes helpers
    ////////////////////////////////////////
	
    private static String get(Element xmlVal) {
	    try {
            boolean isNull = getIsNull(xmlVal);
		    if(isNull) {//<attr isNull="true"></attr>
			    return null;
		    } else {                    
			    if(xmlVal.getFirstChild() == null) {//<attr></attr>
				    return "";
			    } else {//<attr>value</attr>
                    return xmlVal.getFirstChild().getNodeValue();
			    }				
		    }
	    } catch (Exception e) {
            throw new IllegalArgumentException("Invalid xml :" + xmlVal);
	    }
    }
	
    public static String[] toArray(Element xmlVal) {
	    try {
		    String[] result;
            boolean isNull = getIsNull(xmlVal);
		    if(isNull) {//<array isNull="true"></array>
			    result = null;
		    } else {
                Node child = xmlVal.getFirstChild();
			    if(child == null) {//<array></array>
				    result = new String[0];
			    } else {//<array><arra>value</arra></array>
                    ArrayList list = new ArrayList();
				    while(child != null) {
				    	//here we may encounter Text nodes which are useless
				    	//for us, so following if statement is required.
				    	if(child instanceof Element) {
				    		list.add(child);
				    	}
					    child = child.getNextSibling();
				    }
				    result = new String[list.size()];
				    for(int i = 0; i < result.length; i++) {
					    result[i] = getString((Element)list.get(i));	
				    }								
			    }				
		    }
		    return result;
	    } catch (IllegalArgumentException e) {
		    throw e;
	    } catch (Exception e) {
            throw new IllegalArgumentException("Invalid xml:" + xmlVal);
	    }
    }
	
    private static boolean getIsNull(Element xmlVal) {
    	Attr found = xmlVal.getAttributeNode("isNull");
        if (found == null)
		    return false;
        return Boolean.parseBoolean(found.getValue());		
    }

    public static boolean getIsSet(Element xmlVal)
    {
    	Attr found = xmlVal.getAttributeNode("isSet");    	
        if (found == null)
            return false;
        return Boolean.parseBoolean(found.getValue());
    }
    
    ////////////////////////////////////////
    // Load/Save helpers
    ////////////////////////////////////////
	
	public static Element loadAttr(Document xmlDoc, String document, String xpath, String attrKey) throws Exception {
		String expression = "/" + xpath + "/" + attrKey;
		Node node = selectSingleNode(xmlDoc, expression);
		Element attrElm = (Element) node;
		return attrElm;
	}
	
	public static void saveAttr(Document xmlDoc, String document, String xpath, String attrKey, Element attrElement) throws Exception {
		String expression = "/" + xpath + "/" + attrKey;
		Node oldNode = selectSingleNode(xmlDoc, expression);
		Node parent = oldNode.getParentNode();
		parent.replaceChild(attrElement, oldNode);
	}
	
	public static void saveOrCreateAttr(Document xmlDoc, String document, String xpath, String attrKey, Element attrElement) throws Exception {
		String expression = "/" + xpath + "/" + attrKey;
		Node oldNode = createElementByXPath(xmlDoc, expression);
		Node parent = oldNode.getParentNode();
		parent.replaceChild(attrElement, oldNode);
	}
	
	private static Node selectSingleNode(Document xmlDoc, String expression) throws Exception {
		XPathFactory xf = XPathFactory.newInstance();
		XPath xp = xf.newXPath();
		XPathExpression ex = xp.compile(expression);
		Node node = (Node)ex.evaluate(xmlDoc, XPathConstants.NODE);
		return node;
	}
	
    public static Element createElementByXPath(Document xmlDoc, String xpath) throws Exception {
        if (xpath == null)
            throw new IllegalArgumentException("Null xpath not allowed.");
        ArrayList<String> toCreate = new ArrayList<String>();
        Node node = null;
        while (true) {
        	if(xpath.length() == 0) {
        		break;
        	}
            node = selectSingleNode(xmlDoc, xpath);
            if (node != null)
                break;
            int slashIdx = xpath.lastIndexOf('/');
            if (slashIdx == -1) {
                toCreate.add(0, xpath);
                break;
            } else {
                toCreate.add(0, xpath.substring(slashIdx + 1));
                xpath = xpath.substring(0, slashIdx);
            }
        }
        if (toCreate.size() > 0) {
            if (node == null)
                node = xmlDoc;
            for(String part : toCreate) {
                node = createPart(node, part);
            }
        }
        return (Element)node;
    }

    //this method assume part is in forms of tag or tag[@name='foo']
    private static Node createPart(Node parent, String part)
    {
        if (part == null)
            throw new IllegalArgumentException("Null part not allowed.");
        String tag = part;
        String name = null;
        int idx = part.indexOf("[@name='");
        if (idx != -1) {
            tag = part.substring(0, idx);
            idx = part.indexOf('\'');
            name = part.substring(idx + 1);
            name = name.substring(0, name.length() - 2);
        }
        Element elem = null;
        if (parent instanceof Document) {
            elem = ((Document)parent).createElement(tag);
        } else {
            elem = parent.getOwnerDocument().createElement(tag);
        }
        if (name != null) {
            elem.setAttribute("name", name);
        }
        parent.appendChild(elem);
        return elem;
    }
    
    private static Document newEmptyDocument() throws ParserConfigurationException {
		DocumentBuilderFactory dbf
		 = DocumentBuilderFactory.newInstance();
    	DocumentBuilder db = dbf.newDocumentBuilder();
    	return db.newDocument();
    }
    
	public static Document loadDocument(String fileName) throws Exception {
		DocumentBuilderFactory dbf
		 = DocumentBuilderFactory.newInstance();
       //dbf.setIgnoringElementContentWhitespace(true);
       //dbf.setValidating(false);
       //dbf.setCoalescing(true);
       //dbf.setIgnoringComments(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
	   //dbf.setAttribute("normalize-characters", true);
		//db.setEntityResolver(new Resolver());
		db.setErrorHandler(new EH());
		InputStream in = new FileInputStream(fileName);
		InputSource ins = new InputSource(in);
		Document doc = db.parse(ins);
		return doc;
	}
	
	public static Document loadOrCreateDocument(String fileName, boolean createFileIfAbsent) throws Exception {
		Document doc = null;
		File f = new File(fileName);
		if (f.exists()) {
			doc = newEmptyDocument();
			try {
			doc = loadDocument(fileName);
			} catch (SAXException ignore) {
				// TODO: Log exception.
                //Here we assume that xml file does not
                //have a syntax problems. It is just missing
                //its root element because we have created an
                //empty xml file at the first time. So here
                //we return an empty XmlDocument object and
                //ignore exception.				
			}
		} else {
			if(createFileIfAbsent) {
				if(f.getParentFile() != null)
					f.getParentFile().mkdirs();
				f.createNewFile();
			}
			doc = newEmptyDocument();
		}
		return doc;
	}
	
	public static void saveDocument(Document doc, String fileName) throws Exception {		
        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            tf.setAttribute("indent-number", Integer.valueOf(2));
        } catch (IllegalArgumentException iae) {
        	//Ignore the IAE. Should not fail the writeout even the
            //transformer provider does not support "indent-number".        	
        }
        Transformer t = tf.newTransformer();    
        //t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doc.getDoctype().getSystemId());
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty(OutputKeys.METHOD, "xml");
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        DOMSource doms = new DOMSource(doc);
        OutputStream out = new FileOutputStream(fileName);
        Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        StreamResult sr = new StreamResult(writer);
        t.transform(doms, sr);		
	}
	
    ////////////////////////////////////////
    // Attribute to XML helpers
    ////////////////////////////////////////
	
    public static Element toXML(Document doc, String attrKey, Object attrVal, boolean isSet) {
        Element element = _toXML(doc, attrKey, attrVal);
        element.setAttribute("isSet", ""+isSet);
        return element;
    }

    private static Element _toXML(Document doc, String attrKey, Object attrVal)
    {
        Element element = doc.createElement(attrKey);
        if(attrVal == null) {
            element.setAttribute("isNull", "true");
	    }
        if(attrVal != null) {
		    String str = attrVal.toString();
		    //str = escapeXml(str);
            Text text = doc.createTextNode(str);
            element.appendChild(text);
	    }
        return element;
    }

    public static Element toArrXML(Document doc, String attrKey, Object arr, boolean isSet) {
        Element element = doc.createElement(attrKey);
        
	    if(arr == null) {
            element.setAttribute("isNull", "true");
            element.setAttribute("isSet", ""+isSet);
            return element;
	    }

        element.setAttribute("isSet", ""+isSet);

        int length = Array.getLength(arr);
	    if(length == 0) {
            return element;
	    }    		
		
	    //removing last (s)
	    String _attrKey = attrKey.substring(0, attrKey.length() - 1);
		
	    for(int i = 0; i < length; i++) {
	    	Object obj = Array.get(arr, i);
	    	Element elm = _toXML(doc, _attrKey, obj);
            element.appendChild(elm);
	    }

        return element; 
    }

    private static String escapeXml(String s) {
        if (s == null)
            return null;
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '\'') {
                sb.append("&apos;");
            } else if (c == '&') {
                sb.append("&amp;");
            } else if (c == '"') {
                sb.append("&quot;");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    ////////////////////////////////////////
    // private nested classes
    ////////////////////////////////////////
    
	/*
    private static class Resolver implements EntityResolver {  		
        public InputSource resolveEntity(String pid, String sid)
            throws SAXException
        {
            if (sid.equals(PROPS_DTD_URI)) {
                InputSource is;
                is = new InputSource(new StringReader(PROPS_DTD));
                is.setSystemId(PROPS_DTD_URI);
                return is;
            }
            throw new SAXException("Invalid system identifier: " + sid);
        }
    }
    */

    private static class EH implements ErrorHandler {
        public void error(SAXParseException x) throws SAXException {
            throw x;
        }
        public void fatalError(SAXParseException x) throws SAXException {
            throw x;
        }
        public void warning(SAXParseException x) throws SAXException {
            throw x;
        }
    }
    
    ////////////////////////////////////////
    // private constructor
    ////////////////////////////////////////
	
    private XMLUtils() {}    
}
