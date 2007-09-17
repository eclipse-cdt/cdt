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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.util.XmlStorageElement;
import org.w3c.dom.Element;

public class InternalXmlStorageElement extends XmlStorageElement {
	boolean fIsDirty;
	Element fElement;
	private boolean fIsReadOnly;
	private List fStorageList;

	public InternalXmlStorageElement(Element element, ICStorageElement parent,
			boolean alowReferencingParent, String[] attributeFilters,
			String[] childFilters, boolean readOnly) {
		super(element, parent, alowReferencingParent, attributeFilters, childFilters);
		fElement = element;
		fIsReadOnly = readOnly;
	}

	public InternalXmlStorageElement(Element element, ICStorageElement parent,
			boolean alowReferencingParent, boolean readOnly) {
		super(element, parent, alowReferencingParent);
		fElement = element;
		fIsReadOnly = readOnly;
	}

	public InternalXmlStorageElement(Element element, boolean readOnly) {
		super(element);
		fElement = element;
		fIsReadOnly = readOnly;
	}
	
	void storageCreated(CStorage storage){
		List list = getStorageList(true);
		list.add(storage);
	}
	
	private List getStorageList(boolean create){
		if(fStorageList == null && create)
			fStorageList = new ArrayList();
		return fStorageList;
	}
	
	public boolean isReadOnly(){
		return fIsReadOnly;
	}
	
	public void setReadOnly(boolean readOnly){
		setReadOnly(readOnly, true);
	}
	
	public void setReadOnly(boolean readOnly, boolean keepModify){
		fIsReadOnly = readOnly;
		fIsDirty &= keepModify;

		ICStorageElement children[] = getChildren(false);
		for(int i = 0; i < children.length; i++){
			((InternalXmlStorageElement)children[i]).setReadOnly(readOnly, keepModify);
		}

	}
	
	public boolean isDirty(){
		if(fIsDirty)
			return true;
		
		List list = getStorageList(false);
		if(list != null){
			for(Iterator iter = list.iterator(); iter.hasNext();){
				CStorage storage = (CStorage)iter.next();
				if(storage.isDirty())
					return true;
			}
		}
		
		ICStorageElement children[] = getChildren();
		for(int i = 0; i < children.length; i++){
			if(((InternalXmlStorageElement)children[i]).isDirty())
				return true;
		}
		
		return false;
	}

	public void setDirty(boolean dirty){
		fIsDirty = dirty;
		
		if(!dirty){
			List list = getStorageList(false);
			if(list != null){
				for(Iterator iter = list.iterator(); iter.hasNext();){
					CStorage storage = (CStorage)iter.next();
					storage.setDirty(false);
				}
			}

			ICStorageElement children[] = getChildren();
			for(int i = 0; i < children.length; i++){
				((InternalXmlStorageElement)children[i]).setDirty(false);
			}
		}
	}

	public void clear() {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();

		super.clear();
		fIsDirty = true;
	}

	protected XmlStorageElement createChild(Element element,
			boolean alowReferencingParent, String[] attributeFilters,
			String[] childFilters) {
/*		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
*/
		return new InternalXmlStorageElement(element, this, alowReferencingParent, attributeFilters, childFilters, fIsReadOnly);
	}

	public ICStorageElement createChild(String name,
			boolean alowReferencingParent, String[] attributeFilters,
			String[] childFilters) {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();

		fIsDirty = true;
		return super.createChild(name, alowReferencingParent, attributeFilters,
				childFilters);
	}

	public ICStorageElement createChild(String name) {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();

		fIsDirty = true;
		return super.createChild(name);
	}

	public void removeAttribute(String name) {
		fIsDirty = true;
		super.removeAttribute(name);
	}

	public void setAttribute(String name, String value) {
		fIsDirty = true;
		super.setAttribute(name, value);
	}

	public void setValue(String value) {
		fIsDirty = true;
		super.setValue(value);
	}

	protected void removed() {
		super.removed();
	}

	public ICStorageElement importChild(ICStorageElement el)
			throws UnsupportedOperationException {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		fIsDirty = true;
		return super.importChild(el);
	}

	public void removeChild(ICStorageElement el) {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();

		fIsDirty = true;
		super.removeChild(el);
	}
}
