/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model.xml;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.settings.model.ICSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.settings.model.ExceptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XmlStorageElement implements ICStorageElement {
	
	protected static final String[] emptyStringList = new String[0];

	public Element fElement;
	private ICStorageElement fParent;
	protected List<ICStorageElement> fChildList = new ArrayList<ICStorageElement>();
	private boolean fChildrenCreated;
	private String[] fAttributeFilters;
	private String[] fChildFilters;

	public XmlStorageElement(Element element){
		this(element, null, false);
	}

	public XmlStorageElement(Element element, ICStorageElement parent, boolean alowReferencingParent){
		this(element, parent, null, null);
	}

	public XmlStorageElement(Element element,
			ICStorageElement parent,
			String[] attributeFilters,
			String[] childFilters){
		fElement = element;
		fParent = parent;
		
		if(attributeFilters != null && attributeFilters.length != 0)
			fAttributeFilters = attributeFilters.clone();
		
		if(childFilters != null && childFilters.length != 0)
			fChildFilters = childFilters.clone();
	}
	
	/**
	 * Create ICStorageElement children from Xml tree
	 */
	private void createChildren(){
		if(fChildrenCreated)
			return;

		fChildrenCreated = true;
		fChildList.clear();
		NodeList list = fElement.getChildNodes();
		int size = list.getLength();
		for(int i = 0; i < size; i++){
			Node node = list.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE
					&& isChildAlowed(node.getNodeName())){
				createAddChild((Element)node, true, null, null);
			}
		}
	}
	
	private XmlStorageElement createAddChild(Element element,
			boolean alowReferencingParent,
			String[] attributeFilters,
			String[] childFilters){
		XmlStorageElement child = createChild(element, alowReferencingParent, attributeFilters, childFilters);
		fChildList.add(child);
		return child; 
	}
	
	protected XmlStorageElement createChild(Element element,
			boolean alowReferencingParent,
			String[] attributeFilters,
			String[] childFilters){
		return new XmlStorageElement(element, this, attributeFilters, childFilters);
	}

	public ICStorageElement[] getChildren() {
		return getChildren(XmlStorageElement.class);
	}

	protected ICStorageElement[] getChildren(Class<XmlStorageElement> clazz){
		return getChildren(clazz, true);
	}

	protected ICStorageElement[] getChildren(boolean load){
		return getChildren(XmlStorageElement.class, load);
	}

	protected ICStorageElement[] getChildren(Class<XmlStorageElement> clazz, boolean load){
		if(load)
			createChildren();

		ICStorageElement[] children = (ICStorageElement[])java.lang.reflect.Array.newInstance(
                                clazz, fChildList.size());

		return fChildList.toArray(children);
	}

	public ICStorageElement[] getChildrenByName(String name) {
		createChildren();
		ArrayList<ICStorageElement> children = new ArrayList<ICStorageElement>();
		for (ICStorageElement child : fChildList)
			if (name.equals(child.getName()))
				children.add(child);
		return children.toArray(new ICStorageElement[children.size()]);
	}
	
	public boolean hasChildren() {
		createChildren();
		return !fChildList.isEmpty();
	}

	public ICStorageElement getParent() {
		return fParent;
	}

	public String getAttribute(String name) {
		if(isPropertyAlowed(name) && fElement.hasAttribute(name))
			return fElement.getAttribute(name);
		return null;
	}
	
	public boolean hasAttribute(String name) {
		return fElement.hasAttribute(name);
	}
	
	private boolean isPropertyAlowed(String name){
		if(fAttributeFilters != null){
			return checkString(name, fAttributeFilters);
		}
		return true;
	}

	private boolean isChildAlowed(String name){
		if(fChildFilters != null){
			return checkString(name, fChildFilters);
		}
		return true;
	}

	private boolean checkString(String name, String array[]){
		if(array.length > 0){
			for(int i = 0; i < array.length; i++){
				if(name.equals(array[i]))
					return false;
			}
		}
		return true;
	}

	public void removeChild(ICStorageElement el) {
		if(el instanceof XmlStorageElement){
			ICStorageElement[] children = getChildren();
			for(int i = 0; i < children.length; i++){
				if(children[i] == el){
					XmlStorageElement xmlEl = (XmlStorageElement)el;
					Node nextSibling = xmlEl.fElement.getNextSibling();
					fElement.removeChild(xmlEl.fElement);
					if (nextSibling != null && nextSibling.getNodeType() == Node.TEXT_NODE) {
						String value = nextSibling.getNodeValue();
						if (value != null && value.trim().length() == 0) {
							// remove whitespace
							fElement.removeChild(nextSibling);
						}
					}
					fChildList.remove(el);
				}
			}
		}
		
	}

	public void removeAttribute(String name) {
		if(isPropertyAlowed(name))
			fElement.removeAttribute(name);
	}

	public void setAttribute(String name, String value) {
		if(isPropertyAlowed(name))
			fElement.setAttribute(name, value);
	}
	
	public void clear(){
		createChildren();

		ICStorageElement children[] = fChildList.toArray(new ICStorageElement[fChildList.size()]);
		for(int i = 0; i < children.length; i++){
			removeChild(children[i]);
		}

		NamedNodeMap map = fElement.getAttributes();
		for(int i = 0; i < map.getLength(); i++){
			Node attr = map.item(i);
			if(isPropertyAlowed(attr.getNodeName()))
				map.removeNamedItem(attr.getNodeName());
		}

		Node node = fElement.getFirstChild();
		while (node != null) {
			Node nextChildNode = node.getNextSibling();
			if(node.getNodeType() == Node.TEXT_NODE)
				fElement.removeChild(node);
			// update the pointer
			node = nextChildNode;
		}
	}

	public ICStorageElement createChild(String name, 
			boolean alowReferencingParent,
			String[] attributeFilters,
			String[] childFilters) {
		if(!isChildAlowed(name))
			return null;
		Element childElement = fElement.getOwnerDocument().createElement(name);
		fElement.appendChild(childElement);
		return createAddChild(childElement, alowReferencingParent, attributeFilters, childFilters);
	}

	public String getName() {
		return fElement.getNodeName();
	}

	public ICStorageElement createChild(String name) {
		return createChild(name, true, null, null);
	}

	public String getValue() {
		Text text = getTextChild();
		if(text != null)
			return text.getData();
		return null;
	}

	public void setValue(String value) {
		Text text = getTextChild();
		if(value != null){
			if(text == null){
				text = fElement.getOwnerDocument().createTextNode(value);
				fElement.appendChild(text);
			} else {
				text.setData(value);
			}
		} else {
			if(text != null){
				fElement.removeChild(text);
			}
		}
	}
	
	private Text getTextChild(){
		NodeList nodes = fElement.getChildNodes();
		Text text = null;
		for(int i = 0; i < nodes.getLength(); i++){
			Node node = nodes.item(i);
			if(node.getNodeType() == Node.TEXT_NODE){
				text = (Text)node;
				break;
			}
		}

		return text;
	}
	
	public ICStorageElement importChild(ICStorageElement el) throws UnsupportedOperationException {
		return addChild(el, true, null, null);
	}

	public ICStorageElement addChild(ICStorageElement el, 
			boolean alowReferencingParent,
			String[] attributeFilters,
			String[] childFilters) throws UnsupportedOperationException {
		
		if(!isChildAlowed(el.getName()))
			return null;

		if(el instanceof XmlStorageElement){
			XmlStorageElement xmlStEl = (XmlStorageElement)el;
			Element xmlEl = xmlStEl.fElement;
			Document thisDoc = fElement.getOwnerDocument();
			Document otherDoc = xmlEl.getOwnerDocument();
			if(!thisDoc.equals(otherDoc)){
				xmlEl = (Element)thisDoc.importNode(xmlEl, true);
			} else {
				xmlEl = (Element)xmlEl.cloneNode(true);
			}
			
			xmlEl = (Element)fElement.appendChild(xmlEl);
			return createAddChild(xmlEl, alowReferencingParent, attributeFilters, childFilters);
		} else {
			// FIXME JBB allow import of other types of ICStorageElement
			throw new UnsupportedOperationException();
		}
	}
	
	public String[] getAttributeFilters(){
		if(fAttributeFilters != null)
			return fAttributeFilters.clone();
		return emptyStringList;
	}

	public String[] getChildFilters(){
		if(fChildFilters != null)
			return fChildFilters.clone();
		return emptyStringList;
	}

	public boolean equals(ICStorageElement el){
		if(!getName().equals(el.getName()))
			return false;
		
		if (!valuesMatch(getValue(), el.getValue()))
			return false;
		
		String[] attrs = getAttributeNames();
		String[] otherAttrs = el.getAttributeNames();
		if(attrs.length != otherAttrs.length)
			return false;
		
		if(attrs.length != 0){
			Set<String> set = new HashSet<String>(Arrays.asList(attrs));
			set.removeAll(Arrays.asList(otherAttrs));
			if(set.size() != 0)
				return false;

			for(int i = 0; i < attrs.length; i++){
				if(!getAttribute(attrs[i]).equals(el.getAttribute(attrs[i])))
					return false;
			}

		}
		
		
		XmlStorageElement[] children = (XmlStorageElement[])getChildren();
		ICStorageElement[] otherChildren = el.getChildren();
		
		if(children.length != otherChildren.length)
			return false;
		
		if(children.length != 0){
			for(int i = 0; i < children.length; i++){
				if(!children[i].equals(otherChildren[i]))
					return false;
			}
		}
		
		return true;
	}

	private static boolean valuesMatch(String value, String other) {
		if(value == null) {
			return other == null || other.trim().length() == 0;
		} else if (other == null) {
			return value.trim().length() == 0;
		} else {
			return value.trim().equals(other.trim());
		}
	}

	public String[] getAttributeNames() {
		NamedNodeMap nodeMap = fElement.getAttributes();
		int length = nodeMap.getLength();
		List<String> list = new ArrayList<String>(length);
		for(int i = 0; i < length; i++){
			Node node = nodeMap.item(i);
			String name = node.getNodeName();
			if(isPropertyAlowed(name))
				list.add(name);
		}
		return list.toArray(new String[list.size()]);
	}
	
	public ICStorageElement createCopy() throws UnsupportedOperationException, CoreException {
		Element newEl = createXmlElementCopy();
		return new XmlStorageElement(newEl, null, fAttributeFilters, fChildFilters);
	}
	
	protected Element createXmlElementCopy() throws CoreException {

		try {
			Element newXmlEl = null;
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			if(fElement.getParentNode().getNodeType() == Node.DOCUMENT_NODE){
				Document baseDoc = fElement.getOwnerDocument();
				NodeList list = baseDoc.getChildNodes();
				for(int i = 0; i < list.getLength(); i++){
					Node node = list.item(i);
					node = importAddNode(doc, node);
					if(node.getNodeType() == Node.ELEMENT_NODE && newXmlEl == null){
						newXmlEl = (Element)node;
					}
				}
				
			} else {
				newXmlEl = (Element)importAddNode(doc, fElement);
			}
//			Document baseDoc = el.fElement.getOwnerDocument();
//			Element baseEl = baseDoc.getDocumentElement();
//			Element newXmlEl = (Element)doc.importNode(baseEl, true);

			
//			doc.appendChild(newXmlEl);
			return newXmlEl;
		} catch (ParserConfigurationException e) {
			throw ExceptionFactory.createCoreException(e);
		} catch (FactoryConfigurationError e) {
			throw ExceptionFactory.createCoreException(e);
		}
	}

	private Node importAddNode(Document doc, Node node){
		if(node.getOwnerDocument().equals(doc)){
			node = node.cloneNode(true);
		} else {
			node = doc.importNode(node, true);
		}
	
		return doc.appendChild(node);
	}

	public ICSettingsStorage createSettingStorage(boolean readOnly) throws CoreException, UnsupportedOperationException {
		return new XmlStorage(fElement, readOnly);
	}

	/*
	 * toString() outputs XML tree -- useful for debugging
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");	//$NON-NLS-1$

			DOMSource source = new DOMSource(fElement);
			StreamResult result = new StreamResult(stream);
			transformer.transform(source, result);
			builder.append(stream.toString());

		} catch (Exception e){
			return fElement.toString();
		}

		return builder.toString();
	}

}
