/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.cdt.core.settings.model.ICLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.internal.core.Option;

public final class OptionStringValue {
	private static final String ATTR_SRC_PATH = "srcPath";  //$NON-NLS-1$
	private static final String ATTR_SRC_ROOT_PATH = "srcRootPath"; //$NON-NLS-1$
	private static final String ATTR_SRC_PREFIX_MAPPING = "srcPrefixMapping"; //$NON-NLS-1$

	private String value;
	private String srcPath;
	private String srcRootPath;
	private String srcPrefixMapping;
	private boolean isBuiltIn;
	
	public OptionStringValue(ICStorageElement el){
		if (el.getAttribute(Option.LIST_ITEM_BUILTIN) != null) {
			isBuiltIn = Boolean.valueOf(el.getAttribute(Option.LIST_ITEM_BUILTIN)).booleanValue();
		} else {
			isBuiltIn = false;
		}
		value = el.getAttribute(Option.LIST_ITEM_VALUE);
		srcPath = el.getAttribute(ATTR_SRC_PATH);
		srcRootPath = el.getAttribute(ATTR_SRC_ROOT_PATH);
		srcPrefixMapping = el.getAttribute(ATTR_SRC_PREFIX_MAPPING);
		if(value == null)
			value = Option.EMPTY_STRING;
	}

	public OptionStringValue(IManagedConfigElement el){
		if (el.getAttribute(Option.LIST_ITEM_BUILTIN) != null) {
			isBuiltIn = Boolean.valueOf(el.getAttribute(Option.LIST_ITEM_BUILTIN)).booleanValue();
		} else {
			isBuiltIn = false;
		}
		value = el.getAttribute(Option.LIST_ITEM_VALUE);
		srcPath = el.getAttribute(ATTR_SRC_PATH);
		srcRootPath = el.getAttribute(ATTR_SRC_ROOT_PATH);
		srcPrefixMapping = el.getAttribute(ATTR_SRC_PREFIX_MAPPING);
		if(value == null)
			value = Option.EMPTY_STRING;
	}

	public OptionStringValue(OptionStringValue base){
		isBuiltIn = base.isBuiltIn;
		value = base.value;
		srcPath = base.srcPath;
		srcRootPath = base.srcRootPath;
		srcPrefixMapping = base.srcPrefixMapping;
	}

	public OptionStringValue(String value){
		this(value, false);
	}

	public OptionStringValue(String value, boolean isBuiltIn){
		this(value, isBuiltIn, null, null, null);
	}

	/**
	 * source path settings are applicable for the {@link IOption#LIBRARY_FILES} only
	 */
	public OptionStringValue(String value, boolean isBuiltIn, String srcPath, String srcRootPath, String srcPrefixMapping){
		if(value == null)
			value = Option.EMPTY_STRING;
		this.isBuiltIn = isBuiltIn;
		this.value = value;
		this.srcPath = srcPath;
		this.srcRootPath = srcRootPath;
		this.srcPrefixMapping = srcPrefixMapping;
	}
	
	public void serialize(ICStorageElement el){
		el.setAttribute(Option.LIST_ITEM_VALUE, value);
		el.setAttribute(Option.LIST_ITEM_BUILTIN, Boolean.toString(isBuiltIn));
		if(srcPath != null)
			el.setAttribute(ATTR_SRC_PATH, srcPath);
		if(srcRootPath != null)
			el.setAttribute(ATTR_SRC_ROOT_PATH, srcRootPath);
		if(srcPrefixMapping != null)
			el.setAttribute(ATTR_SRC_PREFIX_MAPPING, srcPrefixMapping);
	}

	public boolean isBuiltIn(){
		return isBuiltIn;
	}
	
	public String getValue(){
		return value;
	}

	/**
	 * source attachment settings are applicable for the {@link IOption#LIBRARY_FILES} only
	 * added to fully support the {@link ICLibraryFileEntry} settings 
	 * 
	 * @see ICLibraryFileEntry
	 * @see ICLibraryFileEntry#getSourceAttachmentPath()
	 * @see ICLibraryFileEntry#getSourceAttachmentRootPath()
	 * @see ICLibraryFileEntry#getSourceAttachmentPrefixMapping()
	 * 
	 */
	public String getSourceAttachmentPath(){
		return srcPath;
	}

	/**
	 * source attachment settings are applicable for the {@link IOption#LIBRARY_FILES} only
	 * added to fully support the {@link ICLibraryFileEntry} settings 
	 * 
	 * @see ICLibraryFileEntry
	 * @see ICLibraryFileEntry#getSourceAttachmentPath()
	 * @see ICLibraryFileEntry#getSourceAttachmentRootPath()
	 * @see ICLibraryFileEntry#getSourceAttachmentPrefixMapping()
	 * 
	 */
	public String getSourceAttachmentRootPath(){
		return srcRootPath;
	}

	/**
	 * source attachment settings are applicable for the {@link IOption#LIBRARY_FILES} only
	 * added to fully support the {@link ICLibraryFileEntry} settings 
	 * 
	 * @see ICLibraryFileEntry
	 * @see ICLibraryFileEntry#getSourceAttachmentPath()
	 * @see ICLibraryFileEntry#getSourceAttachmentRootPath()
	 * @see ICLibraryFileEntry#getSourceAttachmentPrefixMapping()
	 * 
	 */
	public String getSourceAttachmentPrefixMapping(){
		return srcPrefixMapping;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		
		if(!(obj instanceof OptionStringValue))
			return false;
		
		OptionStringValue other = (OptionStringValue)obj;
		
		if(isBuiltIn != other.isBuiltIn)
			return false;
		
		if(!CDataUtil.objectsEqual(value, other.value))
			return false;

		if(!CDataUtil.objectsEqual(srcPath, other.srcPath))
			return false;

		if(!CDataUtil.objectsEqual(srcRootPath, other.srcRootPath))
			return false;

		if(!CDataUtil.objectsEqual(srcPrefixMapping, other.srcPrefixMapping))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return code(value);
	}
	
	private static int code(String str){
		return str != null ? str.hashCode() : 0;
	}

	@Override
	public String toString() {
		return new StringBuffer().append("ov:").append(value.toString()).toString(); //$NON-NLS-1$
	}
}