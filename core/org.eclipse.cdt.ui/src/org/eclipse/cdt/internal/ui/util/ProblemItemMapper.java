/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;


import org.eclipse.cdt.core.model.ICElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Item;

import org.eclipse.jface.viewers.ILabelProvider;


/**
 * Helper class for updating error markers.
 * Items are mapped to paths of their underlying resources.
 * Method <code>problemsChanged</code> updates all items that are affected from the changed
 * elements.
 */
public class ProblemItemMapper {

	private static final int NUMBER_LIST_REUSE= 10;

	// map from path to item
	private HashMap fPathToItem;
	private Stack fReuseLists;

	public ProblemItemMapper() {
		fPathToItem= new HashMap();
		fReuseLists= new Stack();
	}

	/**
	 * Updates the icons of all mapped elements containing to the changed elements.
	 * Must be called from the UI thread.
	 */
	public void problemsChanged(Collection changedPaths, ILabelProvider lprovider) {
		// iterate over the smaller set/map
		if (changedPaths.size() <= fPathToItem.size()) {
			iterateChanges(changedPaths, lprovider);
		} else {
			iterateItems(changedPaths, lprovider);
		}
	}
	
	private void iterateChanges(Collection changedPaths, ILabelProvider lprovider) {
		Iterator elements= changedPaths.iterator();
		while (elements.hasNext()) {
			IPath curr= (IPath) elements.next();
			Object obj= fPathToItem.get(curr);
			if (obj == null) {
				// not mapped
			} else if (obj instanceof Item) {
				refreshIcon(lprovider, (Item)obj);
			} else { // List of Items
				List list= (List) obj;
				for (int i= 0; i < list.size(); i++) {
					refreshIcon(lprovider, (Item) list.get(i));
				}
			}
		}
	}
	
	private void iterateItems(Collection changedPaths, ILabelProvider lprovider) {
		Iterator keys= fPathToItem.keySet().iterator();
		while (keys.hasNext()) {
			IPath curr= (IPath) keys.next();
			if (changedPaths.contains(curr)) {
				Object obj= fPathToItem.get(curr);
				if (obj instanceof Item) {
					refreshIcon(lprovider, (Item)obj);
				} else { // List of Items
					List list= (List) obj;
					for (int i= 0; i < list.size(); i++) {
						refreshIcon(lprovider, (Item) list.get(i));
					}
				}
			}
		}
	}	
		
	private void refreshIcon(ILabelProvider lprovider, Item item) {
		if (!item.isDisposed()) { // defensive code
			Object data= item.getData();
			if (data instanceof ICElement && !((ICElement)data).exists()) {
				// @@@ not yet return;
			}
			Image old= item.getImage();
			Image image= lprovider.getImage(data);
			if (image != null && image != old) {
				item.setImage(image);
			}
		}
	}

	/**
	 * Adds a new item to the map.
	 * @param element Element to map
	 * @param item The item used for the element
	 */
	public void addToMap(Object element, Item item) {
		IPath path= getCorrespondingPath(element);
		if (path != null) {
			Object existingMapping= fPathToItem.get(path);
			if (existingMapping == null) {
				fPathToItem.put(path, item);
			} else if (existingMapping instanceof Item) {
				if (existingMapping != item) {
					List list= newList();
					list.add(existingMapping);
					list.add(item);
					fPathToItem.put(path, list);
				}
			} else { // List			
				List list= (List)existingMapping;
				if (!list.contains(item)) {
					list.add(item);
				}
			}
		}
	}

	/**
	 * Removes an element from the map.
	 */	
	public void removeFromMap(Object element, Item item) {
		IPath path= getCorrespondingPath(element);
		if (path != null) {
			Object existingMapping= fPathToItem.get(path);
			if (existingMapping == null) {
				return;
			} else if (existingMapping instanceof Item) {
				fPathToItem.remove(path);
			} else { // List
				List list= (List) existingMapping;
				list.remove(item);
				if (list.isEmpty()) {
					fPathToItem.remove(list);
					releaseList(list);
				}
			}
		}
	}
	
	private List newList() {
		if (!fReuseLists.isEmpty()) {
			return (List) fReuseLists.pop();
		}
		return new ArrayList(2);
	}
	
	private void releaseList(List list) {
		if (fReuseLists.size() < NUMBER_LIST_REUSE) {
			fReuseLists.push(list);
		}
	}
	
	/**
	 * Clears the map.
	 */
	public void clearMap() {
		fPathToItem.clear();
	}
	
	/**
	 * Method that decides which elements can have error markers
	 * Returns null if an element can not have error markers.
	 */	
	private static IPath getCorrespondingPath(Object element) {
		if (element instanceof ICElement) {
			ICElement elem= (ICElement) element;
			//if (!elem.isReadOnly()) { // only modifieable elements can get error ticks
				return elem.getPath();
			//}
			//return null;
		} else if (element instanceof IResource) {
			return ((IResource)element).getFullPath();
		}
		return null;
	}
	
}


