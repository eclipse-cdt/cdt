/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine;

import java.io.IOException;
import java.net.URL;
import com.ibm.icu.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


/**
 * This class contains methods to get first process block element, next process block 
 * element and checks for next process block element.
 */
public class TemplateDescriptor {
	public static final String PROPERTY_GROUP = "property-group"; //$NON-NLS-1$
	public static final String PROCESS = "process"; //$NON-NLS-1$
	public static final String IF = "if"; //$NON-NLS-1$
	public static final String ID = "id"; //$NON-NLS-1$
	public static final String DEFAULT = "default"; //$NON-NLS-1$
    public static final String PERSIST = "persist";                 //$NON-NLS-1$
    public static final String BOOL_TRUE = "true";                   //$NON-NLS-1$

    private Document document;
	private Element rootElement;
	private List<String> persistVector;
	private String pluginId;

	/**
	 * Constructor  which construct the Document based the URL
	 * @param descriptorURL
	 * @throws TemplateInitializationException
	 */
	public TemplateDescriptor(URL descriptorURL, String pluginId) throws TemplateInitializationException {
		String msg= MessageFormat.format(TemplateEngineMessages.getString("TemplateCore.InitFailed"), new Object[]{descriptorURL}); //$NON-NLS-1$
		try {
			this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(descriptorURL.openStream());
		} catch(ParserConfigurationException pce) {
			throw new TemplateInitializationException(msg, pce);
		} catch(IOException ioe) {
			throw new TemplateInitializationException(msg, ioe);
		} catch(SAXException se) {
			throw new TemplateInitializationException(msg, se);
		}
		this.rootElement = document.getDocumentElement();
		this.persistVector = new ArrayList<String>();
		this.pluginId = pluginId;
	}

	/**
	 * This method is to get the default key value pair (HashMap) form template
	 * descriptor root element.
	 * 
	 * @return default values with keys
	 */
	public Map<String, String> getTemplateDefaults(Element element) {
		Map<String, String> templateDefaults = new HashMap<String, String>();
		Element propertyGroupElement;
		List<Element> children = TemplateEngine.getChildrenOfElement(element);
		for (int i = 0, l = children.size(); i < l; i++) {
			propertyGroupElement = children.get(i);
			if (isNestedElement(propertyGroupElement)) {
				templateDefaults = getTemplateDefaults(propertyGroupElement);
			}
			propertyElements(templateDefaults, propertyGroupElement);
		}
		return templateDefaults;
	}

	/**
	 * Checks whether element nested or not
	 * @param element
	 * @return
	 */
	private boolean isNestedElement(Element element){
		boolean result=false;
		if (element!=null){
			List<Element> children = TemplateEngine.getChildrenOfElement(element);
			String elementName = element.getNodeName();
			Element testElement;
			String testElementName = null;
			if (children.size() > 0){
				testElement = children.get(0);
				testElementName=testElement.getNodeName();
			}
			if(elementName.equals(testElementName))
				result=true;
			else result=false;
		}
		return result;
	}

	/**
	 * This method is to get the list of property-group elements from template
	 * descriptor root element.
	 * 
	 * @return list of property-group elements
	 */
	public List<Element> getPropertyGroupList() {
		List<Element> resultList = null;
		List<Element> list = new ArrayList<Element>();
		resultList = list;
		if (rootElement != null) {
			List<Element> tempList = TemplateEngine.getChildrenOfElement(rootElement);
			for (int i = 0, l = tempList.size(); i < l; i++) {
				Element nextPropertyGroup = tempList.get(i);
				String nextPGName = nextPropertyGroup.getNodeName();
				if (nextPGName.equalsIgnoreCase(PROPERTY_GROUP)) {
					list.add(nextPropertyGroup);
				}
			}
		}
		return resultList;
	}

	/**
	 * This method is to get the complex property-group from template descriptor
	 * root element. complex means a property-group contains other
	 * property-group(s)
	 * 
	 * @param element
	 *            root element of type JDOM Element
	 * @return property-group root element of type JDOM Element
	 */
	public Element getRootPropertyGroup(Element element) {
		if (element != null) {
			String rootElementName = element.getNodeName();
			if (rootElementName.equalsIgnoreCase(PROPERTY_GROUP) && isNestedElement(element)) {
				return element;
			}
			return element;
		} else {
			String nextPGElementName = null;
			List<Element> propertyGroupList = TemplateEngine.getChildrenOfElement(element);
			for (int i = 0, l = propertyGroupList.size(); i < l; i++) {
				Element nextPGElement = propertyGroupList.get(i);
				if (isNestedElement(nextPGElement))
					nextPGElementName = nextPGElement.getNodeName();
				if (PROPERTY_GROUP.equalsIgnoreCase(nextPGElementName) && isNestedElement(nextPGElement)) {
					return nextPGElement;
				}
			}
		}
		return null;
	}

	/**
	 * This private method is used in getTemplateDefaults() to get defaults from
	 * property elements
	 * 
	 * @param defaults
	 *            HashMap to store deraults
	 * @param propertyGroupElement
	 *            traverse the complex property-group element
	 */
	private void propertyElements(Map<String, String> defaults, Element propertyGroupElement) {
		List<Element> children = TemplateEngine.getChildrenOfElement(propertyGroupElement);
		for (int i = 0, l = children.size(); i < l; i++) {
			Element propertyElement = children.get(i);
			String key = propertyElement.getAttribute(ID);
			String value = propertyElement.getAttribute(DEFAULT);
			if (key != null && !key.equals("")) { //$NON-NLS-1$
				defaults.put(key, value);
			}

			String persist = propertyElement.getAttribute(PERSIST);
			if ((persist != null) && (persist.trim().equalsIgnoreCase(BOOL_TRUE))) {
				persistVector.add(key);
			}
		}
	}

	/**
	 * added to return root of this document.
	 */
	public Element getRootElement() {
		return rootElement;
	}

	/**
	 * return the list of IDs whose Persist attribute is true.
	 * 
	 * @return Vector.
	 */
	public List<String> getPersistTrueIDs() {
		return persistVector;
	}
	
	public String getPluginId() {
		return pluginId;
	}
}
