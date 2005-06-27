/*******************************************************************************
 * Copyright (c) 2002, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;



import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.util.OverflowingLRUCache;


/**
 * The cache of C elements to their respective info.
 * 
 * This class is similar to the JDT CModelCache class.
 */
public class CModelCache {
	public static final int PROJ_CACHE_SIZE = 50;	
	public static final int FOLDER_CACHE_SIZE = 500;	
	public static final int FILE_CACHE_SIZE = 2000;
	public static final int CHILDREN_CACHE_SIZE = FILE_CACHE_SIZE * 20;
	
	/**
	 * Cache of open projects and roots.
	 */
	protected Map projectAndRootCache;
	
	/**
	 * Cache of open containers
	 */
	protected Map folderCache;

	/**
	 * Cache of open translation unit files
	 */
	protected OverflowingLRUCache fileCache;

	/**
	 * Cache of children of C elements
	 */
	protected Map childrenCache;
	
public CModelCache() {
	this.projectAndRootCache = new HashMap(PROJ_CACHE_SIZE);
	this.folderCache = new HashMap(FOLDER_CACHE_SIZE);	
	this.fileCache = new ElementCache(FILE_CACHE_SIZE);
	this.childrenCache = new HashMap(CHILDREN_CACHE_SIZE); // average 20 children per openable
}

public double openableFillingRatio() {
	return this.fileCache.fillingRatio();
}
	
/**
 *  Returns the info for the element.
 */
public Object getInfo(ICElement element) {
	switch (element.getElementType()) {
		case ICElement.C_MODEL:
		case ICElement.C_PROJECT:
			return this.projectAndRootCache.get(element);
		case ICElement.C_CCONTAINER:
			return this.folderCache.get(element);
		case ICElement.C_ARCHIVE:
		case ICElement.C_BINARY:		
		case ICElement.C_UNIT:
			return this.fileCache.get(element);
		default:
			return this.childrenCache.get(element);
	}
}

/**
 *  Returns the info for this element without
 *  disturbing the cache ordering.
 */
protected Object peekAtInfo(ICElement element) {
	switch (element.getElementType()) {
		case ICElement.C_MODEL:
		case ICElement.C_PROJECT:
			return this.projectAndRootCache.get(element);
		case ICElement.C_CCONTAINER:
			return this.folderCache.get(element);
		case ICElement.C_ARCHIVE:
		case ICElement.C_BINARY:		
		case ICElement.C_UNIT:
			return this.fileCache.peek(element);
		default:
			return this.childrenCache.get(element);
	}
}

/**
 * Remember the info for the element.
 */
protected void putInfo(ICElement element, Object info) {
	switch (element.getElementType()) {
		case ICElement.C_MODEL:
		case ICElement.C_PROJECT:
			this.projectAndRootCache.put(element, info);
			break;
		case ICElement.C_CCONTAINER:
			this.folderCache.put(element, info);
			break;
		case ICElement.C_ARCHIVE:
		case ICElement.C_BINARY:		
		case ICElement.C_UNIT:
			this.fileCache.put(element, info);
			break;
		default:
			this.childrenCache.put(element, info);
	}
}
/**
 * Removes the info of the element from the cache.
 */
protected void removeInfo(ICElement element) {
	switch (element.getElementType()) {
		case ICElement.C_MODEL:
		case ICElement.C_PROJECT:
			this.projectAndRootCache.remove(element);
			break;
		case ICElement.C_CCONTAINER:
			this.folderCache.remove(element);
			break;
		case ICElement.C_ARCHIVE:
		case ICElement.C_BINARY:		
		case ICElement.C_UNIT:
			this.fileCache.remove(element);
			break;
		default:
			this.childrenCache.remove(element);
	}
}

}
