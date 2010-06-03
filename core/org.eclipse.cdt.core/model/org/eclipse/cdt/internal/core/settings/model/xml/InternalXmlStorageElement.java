/*******************************************************************************
 * Copyright (c) 2007, 2009 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Intel Corporation - Initial API and implementation
 * 	James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model.xml;

import org.eclipse.cdt.core.settings.model.ICSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.settings.model.ExceptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Element;

/**
 *
 * Internal XmlStorageElement adds the following functionality
 *  - Dirty flag
 *  - Read-only flag
 *  - XmlStorage which corresponds to the Xml ICSettingsStorage
 *	    if this ICStorageElement is root of a storage tree
 */
public class InternalXmlStorageElement extends XmlStorageElement {
	boolean fIsDirty;
	private boolean fIsReadOnly;
	XmlStorage fStorage;

	public InternalXmlStorageElement(Element element, ICStorageElement parent,
			String[] attributeFilters,
			String[] childFilters, boolean readOnly) {
		super(element, parent, attributeFilters, childFilters);
		fIsReadOnly = readOnly;
	}

	public InternalXmlStorageElement(Element element, ICStorageElement parent,
			boolean alowReferencingParent, boolean readOnly) {
		super(element, parent, alowReferencingParent);
		fIsReadOnly = readOnly;
	}

	public InternalXmlStorageElement(Element element, boolean readOnly) {
		super(element);
		fIsReadOnly = readOnly;
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

	public boolean isModified(){
		if(fIsDirty)
			return true;

		if (fStorage != null && fStorage.isModified())
			return true;

		ICStorageElement children[] = getChildren();
		for(int i = 0; i < children.length; i++){
			if(((InternalXmlStorageElement)children[i]).isModified())
				return true;
		}

		return false;
	}

	public void setDirty(boolean dirty){
		fIsDirty = dirty;

		if(!dirty){
			if (fStorage != null)
				fStorage.setDirty(false);

			ICStorageElement children[] = getChildren();
			for(int i = 0; i < children.length; i++){
				((InternalXmlStorageElement)children[i]).setDirty(false);
			}
		}
	}
	
	public void storageCreated(XmlStorage storage) {
//		Assert.isTrue(fStorage == null, "Storage created on an XmlStorageElement already exists");
		fStorage = storage;
	}

	@Override
	public void clear() {
		makeModification();
		super.clear();
	}

	@Override
	protected XmlStorageElement createChild(Element element,
			boolean alowReferencingParent, String[] attributeFilters,
			String[] childFilters) {
/*		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
*/
		return new InternalXmlStorageElement(element, this, attributeFilters, childFilters, fIsReadOnly);
	}

	@Override
	public ICStorageElement createChild(String name,
			boolean alowReferencingParent, String[] attributeFilters,
			String[] childFilters) {
		makeModification();
		return super.createChild(name, alowReferencingParent, attributeFilters, childFilters);
	}

	@Override
	public ICStorageElement createChild(String name) {
		makeModification();
		return super.createChild(name);
	}

	@Override
	public void removeAttribute(String name) {
		makeModification();
		super.removeAttribute(name);
	}

	@Override
	public void setAttribute(String name, String value) {
		makeModification();
		super.setAttribute(name, value);
	}

	@Override
	public void setValue(String value) {
		makeModification();
		super.setValue(value);
	}

	@Override
	public ICStorageElement importChild(ICStorageElement el) throws UnsupportedOperationException {
		makeModification();
		return super.importChild(el);
	}

	@Override
	public void removeChild(ICStorageElement el) {
		makeModification();
		super.removeChild(el);
	}

	@Override
	public ICSettingsStorage createSettingStorage(boolean readOnly) throws CoreException, UnsupportedOperationException {
		if (!isReadOnly() && readOnly)
			return new XmlStorage(fElement, true);
		return new XmlStorage(this);
	}

	/**
	 *  - Check whether modifcation is allowed
	 *  - Mark this element as dirty
	 *
	 *  If modification is not allowed (fIsReadOnly == true)
	 *  then throw write access exception.
	 */
	private void makeModification() {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		fIsDirty = true;
	}
}
