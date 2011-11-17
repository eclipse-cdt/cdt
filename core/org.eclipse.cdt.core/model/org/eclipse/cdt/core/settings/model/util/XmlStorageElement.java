/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Bug 253911
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @deprecated
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@Deprecated
public class XmlStorageElement implements ICStorageElement {

	Element fElement;
	private ICStorageElement fParent;
	private List<XmlStorageElement> fChildList = new ArrayList<XmlStorageElement>();
	private boolean fChildrenCreated;
	private String[] fAttributeFilters;
	private String[] fChildFilters;
	private boolean fParentRefAlowed;

	public XmlStorageElement(Element element){
		this(element, null, false);
	}

	public XmlStorageElement(Element element, ICStorageElement parent, boolean alowReferencingParent){
		this(element, parent, alowReferencingParent, null, null);
	}

	public XmlStorageElement(Element element,
			ICStorageElement parent,
			boolean alowReferencingParent,
			String[] attributeFilters,
			String[] childFilters){
		fElement = element;
		fParent = parent;
		fParentRefAlowed = alowReferencingParent;

		if(attributeFilters != null && attributeFilters.length != 0)
			fAttributeFilters = attributeFilters.clone();

		if(childFilters != null && childFilters.length != 0)
			fChildFilters = childFilters.clone();
	}

//	public String[] getAttributeFilters(){
//		if(fAttributeFilters != null)
//			return (String[])fAttributeFilters.clone();
//		return null;
//	}

//	public String[] getChildFilters(){
//		if(fChildFilters != null)
//			return (String[])fChildFilters.clone();
//		return null;
//	}
//
//	public boolean isParentRefAlowed(){
//		return fParentRefAlowed;
//	}

	private void createChildren(){
		if(fChildrenCreated)
			return;

		fChildrenCreated = true;
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
		return new XmlStorageElement(element, this, alowReferencingParent, attributeFilters, childFilters);
	}

	@Override
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

	@Override
	public ICStorageElement getParent() {
		return fParentRefAlowed ? fParent : null;
	}

	@Override
	public String getAttribute(String name) {
		if(isPropertyAlowed(name) && fElement.hasAttribute(name))
			return fElement.getAttribute(name);
		return null;
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

//	protected void childRemoved(ICStorageElement child) {
//		fChildList.remove(child);
//	}

	protected void removed(){
//		fElement.getParentNode().removeChild(fElement);
		fElement = null;
//		if(fParent != null)
//			((XmlStorageElement)fParent).childRemoved(this);
	}


	@Override
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
					xmlEl.removed();
				}
			}
		}

	}

	@Override
	public void removeAttribute(String name) {
		if(isPropertyAlowed(name))
			fElement.removeAttribute(name);
	}

	@Override
	public void setAttribute(String name, String value) {
		if(isPropertyAlowed(name))
			fElement.setAttribute(name, value);
	}

	@Override
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

		NodeList list = fElement.getChildNodes();
		for(int i = 0; i < list.getLength(); i++){
			Node node = list.item(i);
			if(node.getNodeType() == Node.TEXT_NODE)
				fElement.removeChild(node);
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

	@Override
	public String getName() {
		return fElement.getNodeName();
	}

	@Override
	public ICStorageElement createChild(String name) {
		return createChild(name, true, null, null);
	}

	@Override
	public String getValue() {
		Text text = getTextChild();
		if(text != null)
			return text.getData();
		return null;
	}

	@Override
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

	@Override
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
			throw new UnsupportedOperationException();
		}
	}

	public String[] getAttributeFilters(){
		if(fAttributeFilters != null)
			return fAttributeFilters.clone();
		return new String[0];
	}

	public String[] getChildFilters(){
		if(fChildFilters != null)
			return fChildFilters.clone();
		return new String[0];
	}

	public boolean isParentRefAlowed(){
		return fParentRefAlowed;
	}

	public boolean matches(ICStorageElement el){
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
				if(!children[i].matches(otherChildren[i]))
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

	@Override
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

	/**
	 * @since 5.1
	 */
	@Override
	public ICStorageElement createCopy() throws UnsupportedOperationException, CoreException {
		// todo Auto-generated method stub
		return null;
	}

	/**
	 * @since 5.1
	 */
	@Override
	public boolean equals(ICStorageElement other) {
		// todo Auto-generated method stub
		return false;
	}

	/**
	 * @since 5.1
	 */
	@Override
	public ICStorageElement[] getChildrenByName(String name) {
		// todo Auto-generated method stub
		return null;
	}

	/**
	 * @since 5.1
	 */
	@Override
	public boolean hasAttribute(String name) {
		// todo Auto-generated method stub
		return false;
	}

	/**
	 * @since 5.1
	 */
	@Override
	public boolean hasChildren() {
		// todo Auto-generated method stub
		return false;
	}

}
