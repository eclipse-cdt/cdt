/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IResource;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Item;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IViewerLabelProvider;
import org.eclipse.jface.viewers.ViewerLabel;

/**
 * Helper class for updating error markers and other decorators that work on resources.
 * Items are mapped to their element's underlying resource.
 * Method <code>resourceChanged</code> updates all items that are affected from the changed
 * elements.
 */
public class ResourceToItemsMapper {

	private static final int NUMBER_LIST_REUSE= 10;

	// map from resource to item
	private HashMap fResourceToItem;
	private Stack fReuseLists;
	
	private ContentViewer fContentViewer;

	public ResourceToItemsMapper(ContentViewer viewer) {
		fResourceToItem= new HashMap();
		fReuseLists= new Stack();
		
		fContentViewer= viewer;
	}

	/**
	 * Must be called from the UI thread.
	 */
	public void resourceChanged(IResource changedResource) {
		Object obj= fResourceToItem.get(changedResource);
		if (obj == null) {
			// not mapped
		} else if (obj instanceof Item) {
			updateItem((Item) obj);
		} else { // List of Items
			List list= (List) obj;
			for (int k= 0; k < list.size(); k++) {
				updateItem((Item) list.get(k));
			}
		}
	}
		
	private void updateItem(Item item) {
		if (!item.isDisposed()) { // defensive code
			ILabelProvider lprovider= (ILabelProvider) fContentViewer.getLabelProvider();
			
			Object data= item.getData();
			
			// If it is an IItemLabelProvider than short circuit: patch Tod (bug 55012)
			if (data != null && lprovider instanceof IViewerLabelProvider) {
				IViewerLabelProvider provider= (IViewerLabelProvider) lprovider;
				
				ViewerLabel updateLabel= new ViewerLabel(item.getText(), item.getImage());
				provider.updateLabel(updateLabel, data);
				
				if (updateLabel.hasNewImage()) {
					item.setImage(updateLabel.getImage());
				}
				if (updateLabel.hasNewText()) {
					item.setText(updateLabel.getText());
				}
			} else {
				Image oldImage= item.getImage();
				Image image= lprovider.getImage(data);
				if (image != null && !image.equals(oldImage)) {
					item.setImage(image);
				}
				String oldText= item.getText();
				String text= lprovider.getText(data);
				if (text != null && !text.equals(oldText)) {
					item.setText(text);
				}
			}
		}
	}

	/**
	 * Adds a new item to the map.
	 * @param element Element to map
	 * @param item The item used for the element
	 */
	public void addToMap(Object element, Item item) {
		IResource resource= getCorrespondingResource(element);
		if (resource != null) {
			Object existingMapping= fResourceToItem.get(resource);
			if (existingMapping == null) {
				fResourceToItem.put(resource, item);
			} else if (existingMapping instanceof Item) {
				if (existingMapping != item) {
					List list= getNewList();
					list.add(existingMapping);
					list.add(item);
					fResourceToItem.put(resource, list);
				}
			} else { // List			
				List list= (List) existingMapping;
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
		IResource resource= getCorrespondingResource(element);
		if (resource != null) {
			Object existingMapping= fResourceToItem.get(resource);
			if (existingMapping == null) {
				return;
			} else if (existingMapping instanceof Item) {
				fResourceToItem.remove(resource);
			} else { // List
				List list= (List) existingMapping;
				list.remove(item);
				if (list.isEmpty()) {
					fResourceToItem.remove(list);
					releaseList(list);
				}
			}
		}
	}
	
	private List getNewList() {
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
		fResourceToItem.clear();
	}
	
	/**
	 * Tests if the map is empty
	 */
	public boolean isEmpty() {
		return fResourceToItem.isEmpty();
	}	
	
	/**
	 * Method that decides which elements can have error markers
	 * Returns null if an element can not have error markers.
	 */	
	private static IResource getCorrespondingResource(Object element) {
		if (element instanceof ICElement) {
			ICElement elem= (ICElement) element;
			if (elem.exists()) { 
				IResource res= elem.getResource();
				if (res == null) {
					ITranslationUnit cu= (ITranslationUnit) elem.getAncestor(ICElement.C_UNIT);
					if (cu != null) {
						// elements in compilation units are mapped to the underlying resource of the original cu
						res= cu.getResource();
					}
				}
				return res; 
			}
			return null;
		} else if (element instanceof IResource) {
			return (IResource) element;
		}
		return null;
	}
	
}
