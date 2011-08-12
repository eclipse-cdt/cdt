/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev (Quoin Inc.).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.)
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.resources.ResourcesUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML utilities.
 *
 */
public class XmlUtil {
	private static final String EOL_XML = "\n"; //$NON-NLS-1$
	private static final String DEFAULT_IDENT = "\t"; //$NON-NLS-1$

	/**
	 * Convenience method to create new XML DOM Document.
	 *
	 * @return a new instance of a DOM {@link Document}.
	 * @throws ParserConfigurationException in case of a problem retrieving {@link DocumentBuilder}.
	 */
	public static Document newDocument() throws ParserConfigurationException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return builder.newDocument();
	}

	/**
	 * Convenience method to retrieve value of a node.
	 * @return node value or {@code null}
	 */
	public static String determineNodeValue(Node node) {
		return node!=null ? node.getNodeValue() : null;
	}

	/**
	 * Convenience method to retrieve an attribute of an element.
	 * Note that calling element.getAttributes() once may be more efficient when pulling several attributes.
	 *
	 * @param element - element to retrieve the attribute from.
	 * @param attr - attribute to get value.
	 * @return attribute value or {@code null}
	 */
	public static String determineAttributeValue(Node element, String attr) {
		NamedNodeMap attributes = element.getAttributes();
		return attributes!=null ? determineNodeValue(attributes.getNamedItem(attr)) : null;
	}

	/**
	 * The method creates an element with specified name and attributes and appends it to the parent element.
	 * This is a convenience method for often used sequence of calls.
	 *
	 * @param parent - the node where to append the new element.
	 * @param name - the name of the element type being created.
	 * @param attributes - string array of pairs attributes and their values.
	 *     Each attribute must have a value, so the array must have even number of elements.
	 * @return the newly created element.
	 *
	 * @throws ArrayIndexOutOfBoundsException in case of odd number of elements of the attribute array
	 *    (i.e. the last attribute is missing a value).
	 */
	public static Element appendElement(Node parent, String name, String[] attributes) {
		Document doc = parent instanceof Document ? (Document)parent : parent.getOwnerDocument();
		Element element = doc.createElement(name);
		if (attributes!=null) {
		int attrLen = attributes.length;
			for (int i=0;i<attrLen;i+=2) {
				String attrName = attributes[i];
				String attrValue = attributes[i+1];
				element.setAttribute(attrName, attrValue);
			}
		}
		parent.appendChild(element);
		return element;
	}

	/**
	 * The method creates an element with specified name and appends it to the parent element.
	 * This is a shortcut for {@link #appendElement(Node, String, String[])} with no attributes specified.
	 *
	 * @param parent - the node where to append the new element.
	 * @param name - the name of the element type being created.
	 * @return the newly created element.
	 */
	public static Element appendElement(Node parent, String name) {
		return appendElement(parent, name, null);
	}

	/**
	 * As a workaround for {@code javax.xml.transform.Transformer} not being able
	 * to pretty print XML. This method prepares DOM {@code Document} for the transformer
	 * to be pretty printed, i.e. providing proper indentations for enclosed tags.
	 *
	 * @param doc - DOM document to be pretty printed
	 */
	public static void prettyFormat(Document doc) {
		prettyFormat(doc, DEFAULT_IDENT);
	}

	/**
	 * As a workaround for {@code javax.xml.transform.Transformer} not being able
	 * to pretty print XML. This method prepares DOM {@code Document} for the transformer
	 * to be pretty printed, i.e. providing proper indentations for enclosed tags.
	 *
	 * @param doc - DOM document to be pretty printed
	 * @param ident - custom indentation as a string of white spaces
	 */
	public static void prettyFormat(Document doc, String ident) {
		doc.normalize();
		Element documentElement = doc.getDocumentElement();
		if (documentElement!=null) {
			prettyFormat(documentElement, "", ident); //$NON-NLS-1$
		}
	}

	/**
	 * The method inserts end-of-line+indentation Text nodes where indentation is necessary.
	 *
	 * @param node - node to be pretty formatted
	 * @param identLevel - initial indentation level of the node
	 * @param ident - additional indentation inside the node
	 */
	private static void prettyFormat(Node node, String identLevel, String ident) {
		NodeList nodelist = node.getChildNodes();
		int iStart=0;
		Node item = nodelist.item(0);
		if (item!=null) {
			short type = item.getNodeType();
			if (type==Node.ELEMENT_NODE || type==Node.COMMENT_NODE) {
				Node newChild = node.getOwnerDocument().createTextNode(EOL_XML + identLevel + ident);
				node.insertBefore(newChild, item);
				iStart=1;
			}
		}
		for (int i=iStart;i<nodelist.getLength();i++) {
			item = nodelist.item(i);
			if (item!=null) {
				short type = item.getNodeType();
				if (type==Node.TEXT_NODE && item.getNodeValue().trim().length()==0) {
					if (i+1<nodelist.getLength()) {
						item.setNodeValue(EOL_XML + identLevel + ident);
					} else {
						item.setNodeValue(EOL_XML + identLevel);
					}
				} else if (type==Node.ELEMENT_NODE) {
					prettyFormat(item, identLevel + ident, ident);
					if (i+1<nodelist.getLength()) {
						Node nextItem = nodelist.item(i+1);
						if (nextItem!=null) {
							short nextType = nextItem.getNodeType();
							if (nextType==Node.ELEMENT_NODE || nextType==Node.COMMENT_NODE) {
								Node newChild = node.getOwnerDocument().createTextNode(EOL_XML + identLevel + ident);
								node.insertBefore(newChild, nextItem);
								i++;
								continue;
							}
						}
					} else {
						Node newChild = node.getOwnerDocument().createTextNode(EOL_XML + identLevel);
						node.appendChild(newChild);
						i++;
						continue;
					}
				}
			}
		}
	}

	/**
	 * Load XML from input stream to DOM Document.
	 *
	 * @param xmlStream - XML stream.
	 * @return new loaded DOM Document.
	 * @throws CoreException if something goes wrong.
	 */
	private static Document loadXml(InputStream xmlStream) throws CoreException {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(xmlStream);
		} catch (Exception e) {
			throw new CoreException(CCorePlugin.createStatus(Messages.XmlUtil_InternalErrorLoading, e));
		}
	}

	/**
	 * Load XML from file to DOM Document.
	 *
	 * @param uriLocation - location of XML file.
	 * @return new loaded XML Document or {@code null} if file does not exist.
	 * @throws CoreException if something goes wrong.
	 */
	public static Document loadXml(URI uriLocation) throws CoreException {
		java.io.File xmlFile = new java.io.File(uriLocation);
		if (!xmlFile.exists()) {
			return null;
		}

		InputStream xmlStream;
		try {
			xmlStream = new FileInputStream(xmlFile);
		} catch (Exception e) {
			throw new CoreException(CCorePlugin.createStatus(Messages.XmlUtil_InternalErrorLoading, e));
		}
		return loadXml(xmlStream);
	}

	/**
	 * Load XML from file to DOM Document.
	 *
	 * @param xmlFile - XML file
	 * @return new loaded XML Document.
	 * @throws CoreException if something goes wrong.
	 */
	public static Document loadXml(IFile xmlFile) throws CoreException {
		InputStream xmlStream = xmlFile.getContents();
		return loadXml(xmlStream);
	}

	/**
	 * Serialize XML Document into a file.<br/>
	 * Note: clients should synchronize access to this method.
	 *
	 * @param doc - DOM Document to serialize.
	 * @param uriLocation - URI of the file.
	 * @throws IOException in case of problems with file I/O
	 * @throws TransformerException in case of problems with XML output
	 */
	public static void serializeXml(Document doc, URI uriLocation) throws IOException, TransformerException {
		XmlUtil.prettyFormat(doc);

		java.io.File storeFile = new java.io.File(uriLocation);
		if (!storeFile.exists()) {
			storeFile.createNewFile();
		}
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");	//$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");	//$NON-NLS-1$

		XmlUtil.prettyFormat(doc);
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new FileOutputStream(storeFile));
		transformer.transform(source, result);

		result.getOutputStream().close();
		ResourcesUtil.refreshWorkspaceFiles(uriLocation);
	}

	/**
	 * Serialize XML Document into a byte array.
	 * @param doc - DOM Document to serialize.
	 * @return XML as a byte array.
	 * @throws CoreException if something goes wrong.
	 */
	private static byte[] toByteArray(Document doc) throws CoreException {
		XmlUtil.prettyFormat(doc);

		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(stream);
			transformer.transform(source, result);

			return stream.toByteArray();
		} catch (Exception e) {
			throw new CoreException(CCorePlugin.createStatus(Messages.XmlUtil_InternalErrorSerializing, e));
		}
	}

	/**
	 * Serialize XML Document into a workspace file.<br/>
	 * Note: clients should synchronize access to this method.
	 *
	 * @param doc - DOM Document to serialize.
	 * @param file - file where to write the XML.
	 * @throws CoreException if something goes wrong.
	 */
	public static void serializeXml(Document doc, IFile file) throws CoreException {
		XmlUtil.prettyFormat(doc);

		InputStream input = new ByteArrayInputStream(toByteArray(doc));
		if (file.exists()) {
			file.setContents(input, IResource.FORCE, null);
		} else {
			file.create(input, IResource.FORCE, null);
		}
	}

	/**
	 * Serialize XML Document into a string.
	 *
	 * @param doc - DOM Document to serialize.
	 * @return XML as a String.
	 * @throws CoreException if something goes wrong.
	 */
	public static String toString(Document doc) throws CoreException {
		return new String(toByteArray(doc));
	}
}
