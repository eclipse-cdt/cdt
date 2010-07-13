/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.settings.model.ExceptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Concrete implementation of ICSettingsStorage backed by an XML document
 *
 * ICStorageElements are stored in the tree under a storageModule element.
 * (This class was previously called CStorage)
 * TODO JBB Make this independent of the Xml Element
 */
public class XmlStorage implements ICSettingsStorage {
	public static final String MODULE_ELEMENT_NAME = "storageModule";	//$NON-NLS-1$
	public static final String MODULE_ID_ATTRIBUTE = "moduleId";	//$NON-NLS-1$
	// Lock to prevent concurrent access to XML DOM which isn't thread-safe for read (Bug 319245)
	final Object fLock;
	public Element fElement;
	private Map<String, InternalXmlStorageElement> fStorageElementMap = new HashMap<String, InternalXmlStorageElement>();
	private volatile boolean fChildrenInited;
	private boolean fIsReadOnly;
	private boolean fIsDirty;

	public XmlStorage(Element element, boolean isReadOnly){
		fElement = element;
		fLock = element.getOwnerDocument();
		fIsReadOnly = isReadOnly;
	}

	public XmlStorage(InternalXmlStorageElement element) throws CoreException {
		fElement = element.fElement;
		fLock = fElement.getOwnerDocument();
		fIsReadOnly = element.isReadOnly();
		element.storageCreated(this);
		sanityCheck(element);
	}

	/**
	 * Check that the XmlStorageElement on which this SettingsStorage
	 * is based doesn't have any attributes, values or children which
	 * are invalid for a settings storage.
	 * @param element
	 * @throws CoreException
	 */
	public void sanityCheck(ICStorageElement element) throws CoreException {
		if (element.getValue() != null && element.getValue().trim().length() > 0)
			throw ExceptionFactory.createCoreException("XmlStorage '" + element.getName() + "' has unexpected child Value: " + element.getValue()); //$NON-NLS-1$ //$NON-NLS-2$
		for (ICStorageElement child : element.getChildren()) {
			if (!MODULE_ELEMENT_NAME.equals(child.getName()))
				throw ExceptionFactory.createCoreException("XmlStorage '" + element.getName() + "' has unexpected child element: " + child.getName()); //$NON-NLS-1$ //$NON-NLS-2$
			if (child.getAttribute(MODULE_ID_ATTRIBUTE) == null)
				throw ExceptionFactory.createCoreException("XmlStorage '" + element.getName() + "' has storageModule child without moduleId"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public boolean isReadOnly(){
		return fIsReadOnly;
	}

	/**
	 * Initialize the set of storageModules of this XmlStorage
	 */
	private void initChildren(){
		if(fChildrenInited)
			return;

		synchronized (fLock) {
		if (fChildrenInited)
			return;
		NodeList children = fElement.getChildNodes();
		int size = children.getLength();
		for(int i = 0; i < size; i++){
			Node node = children.item(i);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			if(!MODULE_ELEMENT_NAME.equals(node.getNodeName()))
				continue;

			Element element = (Element)node;
			String moduleId = element.getAttribute(MODULE_ID_ATTRIBUTE).trim();
			if(moduleId.length() == 0)
				continue;

			createAddStorageElement(moduleId, element);
		}
		fChildrenInited = true;
		}
	}

	private InternalXmlStorageElement createAddStorageElement(String id, Element element){
		InternalXmlStorageElement se = createStorageElement(element, fIsReadOnly);
		fStorageElementMap.put(id, se);
		return se;
	}

	public static InternalXmlStorageElement createStorageElement(Element el, boolean isReadOnly){
		return new InternalXmlStorageElement(el, null, new String[]{MODULE_ID_ATTRIBUTE}, null, isReadOnly);
	}

//	public ICStorageElement getStorage(String id){
//		return getStorage(id, true);
//	}


	public boolean containsStorage(String id) throws CoreException {
		return getStorage(id, false) != null;
	}

	public ICStorageElement importStorage(String id, ICStorageElement el) throws UnsupportedOperationException {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();

		// Remove existing storage with this ID
		removeStorage(id);

		// Create the storage element for import
		synchronized (fLock) {
		Document thisDoc = fElement.getOwnerDocument();
		Element newEl = thisDoc.createElement(MODULE_ELEMENT_NAME);

		fIsDirty = true;

		if (el instanceof XmlStorageElement) {
			// If we're importing an XmlStorageElement use XML methods
			XmlStorageElement xmlStEl = (XmlStorageElement)el;
			synchronized (xmlStEl.fLock) {
			Element xmlEl = xmlStEl.fElement;
			Document otherDoc = xmlEl.getOwnerDocument();
			if(!thisDoc.equals(otherDoc)){
				xmlEl = (Element)thisDoc.importNode(xmlEl, true);
			}

			NodeList nl = xmlEl.getChildNodes();
			for(int i = 0; i < nl.getLength(); i++){
				Node child = nl.item(i).cloneNode(true);
				newEl.appendChild(child);
			}
			newEl = (Element)fElement.appendChild(newEl);
			newEl.setAttribute(MODULE_ID_ATTRIBUTE, id);

			return createAddStorageElement(id, newEl);
			}
		} else {
			// Otherwise importing generic ICStorageElement
			ICStorageElement storageEl = getStorage(id, true);
			for (String attrName: el.getAttributeNames())
				storageEl.setAttribute(attrName, el.getAttribute(attrName));
			for (ICStorageElement child: el.getChildren())
				storageEl.importChild(child);
			return storageEl;
		}
		}
	}

	public ICStorageElement getStorage(String id, boolean create){
		initChildren();

		InternalXmlStorageElement se = fStorageElementMap.get(id);
		if(se == null && create){
//			if(fIsReadOnly)
//				throw ExceptionFactory.createIsReadOnlyException();

			fIsDirty = true;
			synchronized (fLock) {
			Document doc = fElement.getOwnerDocument();
			Element child = createStorageXmlElement(doc, id);
			fElement.appendChild(child);
			se = createAddStorageElement(id, child);
			}
		}
		return se;
	}

	public static Element createStorageXmlElement(Document doc, String storageId){
		Element child = doc.createElement(MODULE_ELEMENT_NAME);
		child.setAttribute(MODULE_ID_ATTRIBUTE, storageId);

		return child;
	}

	public void removeStorage(String id){
		initChildren();
		InternalXmlStorageElement se = fStorageElementMap.remove(id);

		if(se != null){
			if(fIsReadOnly)
				throw ExceptionFactory.createIsReadOnlyException();

			synchronized (fLock) {
			synchronized (se.fLock){ 
			fIsDirty = true;
			Node nextSibling = se.fElement.getNextSibling();
			fElement.removeChild(se.fElement);
			if (nextSibling != null && nextSibling.getNodeType() == Node.TEXT_NODE) {
				String value = nextSibling.getNodeValue();
				if (value != null && value.trim().length() == 0) {
					// remove whitespace
					fElement.removeChild(nextSibling);
				}
			}
			}}
		}
	}

	public boolean isModified(){
		if(fIsDirty)
			return true;

		for(Iterator<InternalXmlStorageElement> iter = fStorageElementMap.values().iterator(); iter.hasNext();){
			InternalXmlStorageElement el = iter.next();
			if(el.isModified())
				return true;
		}

		return false;
	}

	public void setReadOnly(boolean readOnly, boolean keepModify){
		fIsReadOnly = readOnly;
		fIsDirty &= keepModify;
		for(Iterator<InternalXmlStorageElement> iter = fStorageElementMap.values().iterator(); iter.hasNext();){
			InternalXmlStorageElement el = iter.next();
			el.setReadOnly(readOnly, keepModify);
		}
	}

	public void setDirty(boolean isDirty){
		fIsDirty = isDirty;

		if(!fIsDirty){
			for(Iterator<InternalXmlStorageElement> iter = fStorageElementMap.values().iterator(); iter.hasNext();){
				InternalXmlStorageElement el = iter.next();
				el.setDirty(false);
			}
		}
	}

	public void save() throws CoreException {
		throw new UnsupportedOperationException();
	}
}
