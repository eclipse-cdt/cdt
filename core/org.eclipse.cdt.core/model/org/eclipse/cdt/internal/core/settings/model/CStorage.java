/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CStorage implements ICSettingsStorage{
	private static final String MODULE_ELEMENT_NAME = "storageModule";	//$NON-NLS-1$
	private static final String MODULE_ID_ATTRIBUTE = "moduleId";	//$NON-NLS-1$
	Element fElement;
	private Map fStorageElementMap = new HashMap();
	private boolean fChildrenInited;
	private boolean fIsReadOnly;
	private boolean fIsDirty;
	
	public CStorage(Element element, boolean isReadOnly){
		fElement = element;
		fIsReadOnly = isReadOnly;
	}
	
	public CStorage(InternalXmlStorageElement element){
		fElement = element.fElement;
		fIsReadOnly = element.isReadOnly();
		element.storageCreated(this);
	}
	
	public boolean isReadOnly(){
		return fIsReadOnly;
	}
	
	private void initChildren(){
		if(fChildrenInited)
			return;
		fChildrenInited = true;
		
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
	}
	
	private InternalXmlStorageElement createAddStorageElement(String id, Element element){
		InternalXmlStorageElement se = createStorageElement(element, fIsReadOnly);
		fStorageElementMap.put(id, se);
		return se;
	}
	
	public static InternalXmlStorageElement createStorageElement(Element el, boolean isReadOnly){
		return new InternalXmlStorageElement(el, null, false, new String[]{MODULE_ID_ATTRIBUTE}, null, isReadOnly);
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

		removeStorage(id);
		
		InternalXmlStorageElement xmlStEl = (InternalXmlStorageElement)el;
		Element xmlEl = xmlStEl.fElement;
		Document thisDoc = fElement.getOwnerDocument();
		Document otherDoc = xmlEl.getOwnerDocument();
		if(!thisDoc.equals(otherDoc)){
			xmlEl = (Element)thisDoc.importNode(xmlEl, true);
		}
		
		Element newEl = thisDoc.createElement(MODULE_ELEMENT_NAME);
		NodeList nl = xmlEl.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++){
			Node child = nl.item(i).cloneNode(true);
			newEl.appendChild(child);
		}
		
		xmlEl = newEl;
		
		xmlEl = (Element)fElement.appendChild(xmlEl);
		xmlEl.setAttribute(MODULE_ID_ATTRIBUTE, id);
		return createAddStorageElement(id, xmlEl);
	}

	public ICStorageElement getStorage(String id, boolean create){
		initChildren();
		
		InternalXmlStorageElement se = (InternalXmlStorageElement)fStorageElementMap.get(id);
		if(se == null && create){
//			if(fIsReadOnly)
//				throw ExceptionFactory.createIsReadOnlyException();
			
			fIsDirty = true;
			Document doc = fElement.getOwnerDocument();
			Element child = doc.createElement(MODULE_ELEMENT_NAME);
			child.setAttribute(MODULE_ID_ATTRIBUTE, id);
			fElement.appendChild(child);
			se = createAddStorageElement(id, child);
		}
		return se;
	}
	
	public void removeStorage(String id){
		initChildren();
		InternalXmlStorageElement se = (InternalXmlStorageElement)fStorageElementMap.remove(id);
		
		if(se != null){
			if(fIsReadOnly)
				throw ExceptionFactory.createIsReadOnlyException();

			fIsDirty = true;
			fElement.removeChild(se.fElement);
			se.removed();
		}
	}
	
	public boolean isDirty(){
		if(fIsDirty)
			return true;
		
		for(Iterator iter = fStorageElementMap.values().iterator(); iter.hasNext();){
			InternalXmlStorageElement el = (InternalXmlStorageElement)iter.next();
			if(el.isDirty())
				return true;
		}
		
		return false;
	}

	public void setDirty(boolean isDirty){
		fIsDirty = isDirty;
		
		if(!fIsDirty){
			for(Iterator iter = fStorageElementMap.values().iterator(); iter.hasNext();){
				InternalXmlStorageElement el = (InternalXmlStorageElement)iter.next();
				el.setDirty(false);
			}
		}		
	}
}
