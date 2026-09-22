package it.nexera.ris.common.helpers;

import it.nexera.ris.common.annotations.CdaTag;
import it.nexera.ris.common.xml.wrappers.HistoricalReportXMLWrapper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.HtmlEncoding;
import it.nexera.ris.persistence.materialized.HistoricalReportMV;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * XMLHelper
 * Used for working with xml documents
 */
public class XmlHelper extends BaseHelper {

    public static Document openXmlDocument(String fileName)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false);
        Document document = factory.newDocumentBuilder().parse(fileName);
        return document;
    }

    public static Document openXmlDocument(InputStream is) throws SAXException,
            IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false);
        Document document = factory.newDocumentBuilder().parse(is);
        return document;
    }

    public static Document createXmlDocument()
            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false);
        Document document = factory.newDocumentBuilder().newDocument();
        return document;
    }

    public static Element selectElement(Element parent, String tagName)
            throws Exception {
        NodeList lst = parent.getElementsByTagName(tagName);
        if (lst == null || lst.getLength() == 0) {
            return null;
        }

        if (lst.getLength() > 1 && occuresMoreThanOnce(lst, parent)) {
            throw new Exception("Error ! Element" + tagName
                    + " occurs more than once");
        }

        return (Element) lst.item(0);
    }

    public static Element[] selectElements(Element parent, String tagName)
            throws Exception {
        NodeList lst = parent.getElementsByTagName(tagName);

        if (lst == null || lst.getLength() == 0) {
            throw new Exception("Error! Element " + tagName + " not found");
        }

        int lstLenght = lst.getLength();
        Element[] elements = new Element[lstLenght];

        for (int i = 0; i < lstLenght; i++) {
            elements[i] = (Element) lst.item(i);
        }

        return elements;
    }

    public static void appendStringElement(Element parent, String tagName,
                                           String value) {
        Element child = parent.getOwnerDocument().createElement(tagName);
        child.setTextContent(value);
        parent.appendChild(child);
    }

    public static void appendStringElement(Element parent, String tagName,
                                           String nameAttributeValue, String value) {
        Element child = parent.getOwnerDocument().createElement(tagName);
        child.setAttribute("name", nameAttributeValue);
        child.setTextContent(value);
        parent.appendChild(child);
    }

    public static void appendStringElement(Element parent, String tagName,
                                           HashMap<String, String> attributes, String value) {
        Element child = parent.getOwnerDocument().createElement(tagName);

        if (attributes != null) {
            for (Entry<String, String> entry : attributes.entrySet()) {
                child.setAttribute(entry.getKey(), entry.getValue());
            }
        }

        child.setTextContent(value);
        parent.appendChild(child);
    }

    public static String getStringElement(Element parent, String tagName)
            throws Exception {
        Element element = selectElement(parent, tagName);
        if (element == null) {
            return null;
        }
        String value = element.getTextContent();
        return value;
    }

    public static Integer getIntegerElement(Element parent, String tagName)
            throws Exception {
        Element element = selectElement(parent, tagName);
        if (element == null) {
            return null;
        }
        String value = element.getTextContent();
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
        }
        return null;
    }

    private static Boolean occuresMoreThanOnce(NodeList lst, Element element) {
        int count = 0;
        for (int i = 0; i < lst.getLength(); i++) {
            if (lst.item(i).getParentNode().equals(element)) {
                count++;
            }
        }
        return count > 1;
    }

    public static void setStringElement(Element parent, String tagName,
                                        String value) throws Exception {
        Element element = selectElement(parent, tagName);
        element.setTextContent(value);
    }

    public static void saveXmlDocument(Document document, String filePath)
            throws TransformerException, IOException {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute("indent-number", 4);
        Transformer trans = factory.newTransformer();
        trans.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(document);
        trans.transform(source, result);
        String xmlString = sw.toString();

        byte buf[] = xmlString.getBytes();
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        file.createNewFile();
        try (OutputStream fos = new FileOutputStream(filePath)) {
            for (byte aBuf : buf) {
                fos.write(aBuf);
            }
        }
        buf = null;
    }

    public static String createCdaXml(HistoricalReportMV selectedHistoricalReport) throws InvocationTargetException,
            IllegalAccessException {
        return createCdaXml(selectedHistoricalReport, null);
    }

    public static String createCdaXml(HistoricalReportMV selectedHistoricalReport, Session session) throws InvocationTargetException,
            IllegalAccessException {
        if (selectedHistoricalReport != null) {
            String xml = FileHelper.readLayoutFile("template.xml", "ReportSearch");
            Method[] methods = selectedHistoricalReport.getClass().getMethods();
            for (Method m : methods) {
                Annotation[] annotations = m.getDeclaredAnnotations();
                for (Annotation a : annotations) {
                    CdaTag annot = null;

                    if (a instanceof CdaTag) {
                        annot = (CdaTag) a;
                    }

                    if (annot != null) {
                        String tag = annot.value().getTag().toUpperCase();
                        if (xml.contains(tag)) {
                            Object value = m.invoke(selectedHistoricalReport);
                            if (value == null) {
                                value = "";
                            }
                            xml = xml.replaceAll(tag, value.toString());
                        }
                    }
                }
            }
            return escapeHtmlEntities(xml, session);
        }

        return null;
    }

    public static String createCdaXml(HistoricalReportXMLWrapper selectedHistoricalReport) throws InvocationTargetException,
            IllegalAccessException {
        if (selectedHistoricalReport != null) {
            String xml = FileHelper.readLayoutFile("template.xml", "ReportSearch");
            Method[] methods = selectedHistoricalReport.getClass().getMethods();
            for (Method m : methods) {
                Annotation[] annotations = m.getDeclaredAnnotations();
                for (Annotation a : annotations) {
                    CdaTag annot = null;

                    if (a instanceof CdaTag) {
                        annot = (CdaTag) a;
                    }

                    if (annot != null) {
                        String tag = annot.value().getTag().toUpperCase();
                        if (xml.contains(tag)) {
                            Object value = m.invoke(selectedHistoricalReport);
                            if (value == null) {
                                value = "";
                            }
                            xml = xml.replaceAll(tag, value.toString());
                        }
                    }
                }
            }
            return xml;
        }
        return null;
    }

    public static String escapeHtmlEntities(String text, Session session) {
        try {
            if (session == null)
                session = DaoManager.getSession();
            List<HtmlEncoding> htmlEncodingList = ConnectionManager.load(HtmlEncoding.class, session);

            if (!ValidationHelper.isNullOrEmpty(htmlEncodingList)) {
                String[] htmlEntitiesArray = htmlEncodingList
                        .stream()
                        .map(HtmlEncoding::getHtmlEntity)
                        .toArray(size -> new String[size]);
                String[] htmlReplacedArray = htmlEncodingList
                        .stream()
                        .map(HtmlEncoding::getHtmlReplacement)
                        .toArray(size -> new String[size]);
                for(int h = 0; h < htmlEntitiesArray.length;h++){
                    log.info( "Html Entity : " + htmlEntitiesArray[h] + " : " + htmlReplacedArray[h]);
                }
                if (htmlEntitiesArray != null && htmlReplacedArray != null)
                    return StringUtils.replaceEach(text, htmlEntitiesArray, htmlReplacedArray);
            }
        } catch (Exception e) {
            log.error("Error in fetching html entities");
            LogHelper.log(log, e);
        }
        return text;
    }
}
