/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - initial API and implementation (bug 460837)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/** encodes and decodes memento to and from different data types; list, map, String*/
public class MementoUtils {
	protected static final String ROOT_ELEMENT_TAGNAME = "root_element"; //$NON-NLS-1$
	protected static final String ELEMENT_TAGNAME = "elem"; //$NON-NLS-1$
	protected static final String ATTRIBUTE_KEY = "key"; //$NON-NLS-1$
	protected static final String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$

	/** Returns a XML memento, that encodes a single String parameter */
	public static String encodeStringIntoMemento(String str) {
		List<String> list = new ArrayList<>();
		list.add(str);
		return encodeListIntoMemento(list);
	}

	/** Returns a single String parameter, decoded from a XML memento */
	public static String decodeStringFromMemento(String memento) {
		return decodeListFromMemento(memento).get(0);
	}

	/** Returns a XML memento, that encodes a Map of Strings */
	public static String encodeMapIntoMemento(Map<String, String> keyPairValues) {
		String returnValue = null;

		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = dfactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element rootElement = doc.createElement(ROOT_ELEMENT_TAGNAME);
			doc.appendChild(rootElement);
			// create one XML element per map entry
			for (String key : keyPairValues.keySet()) {
				Element elem = doc.createElement(ELEMENT_TAGNAME);
				// store key and value as values of 2 attributes
				elem.setAttribute(ATTRIBUTE_KEY, key);
				elem.setAttribute(ATTRIBUTE_VALUE, keyPairValues.get(key));
				rootElement.appendChild(elem);
			}

			ByteArrayOutputStream s = new ByteArrayOutputStream();

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

			DOMSource source = new DOMSource(doc);
			StreamResult outputTarget = new StreamResult(s);
			transformer.transform(source, outputTarget);

			returnValue = s.toString("UTF8"); //$NON-NLS-1$
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnValue;
	}

	/** Returns a Map of Strings, decoded from a XML memento */
	public static Map<String, String> decodeMapFromMemento(String memento) {
		Map<String, String> keyPairValues = new HashMap<>();

		Element root = null;
		DocumentBuilder parser;
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			root = parser.parse(new InputSource(new StringReader(memento))).getDocumentElement();
			NodeList nodeList = root.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element elem = (Element) node;
					NamedNodeMap nodeMap = elem.getAttributes();
					String key = null;
					String value = null;
					for (int idx = 0; idx < nodeMap.getLength(); idx++) {
						Node attrNode = nodeMap.item(idx);
						if (attrNode.getNodeType() == Node.ATTRIBUTE_NODE) {
							Attr attr = (Attr) attrNode;
							if (attr.getName().equals(ATTRIBUTE_KEY)) {
								key = attr.getValue();
							} else if (attr.getName().equals(ATTRIBUTE_VALUE)) {
								value = attr.getValue();
							}
						}
					}
					if (key != null && value != null) {
						keyPairValues.put(key, value);
					} else {
						throw new Exception();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return keyPairValues;
	}

	/** Returns a XML memento, that encodes a List of Strings */
	public static String encodeListIntoMemento(List<String> labels) {
		String returnValue = null;

		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = dfactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element rootElement = doc.createElement(ROOT_ELEMENT_TAGNAME);
			doc.appendChild(rootElement);
			// create one XML element per list entry to save
			for (String lbl : labels) {
				Element elem = doc.createElement(ELEMENT_TAGNAME);
				elem.setAttribute(ATTRIBUTE_VALUE, lbl);
				rootElement.appendChild(elem);
			}

			ByteArrayOutputStream s = new ByteArrayOutputStream();

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

			DOMSource source = new DOMSource(doc);
			StreamResult outputTarget = new StreamResult(s);
			transformer.transform(source, outputTarget);

			returnValue = s.toString("UTF8"); //$NON-NLS-1$
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnValue;
	}

	/** Returns a List of Strings, decoded from a XML memento */
	public static List<String> decodeListFromMemento(String memento) {
		List<String> list = new ArrayList<>();

		Element root = null;
		DocumentBuilder parser;
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			root = parser.parse(new InputSource(new StringReader(memento))).getDocumentElement();
			NodeList nodeList = root.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element elem = (Element) node;
					String value = elem.getAttribute(ATTRIBUTE_VALUE);
					if (value != null) {
						list.add(value);
					} else {
						throw new Exception();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
}
