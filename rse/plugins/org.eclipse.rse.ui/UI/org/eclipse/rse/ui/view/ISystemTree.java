/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.view;

import org.eclipse.swt.widgets.Item;

/**
 * To drive our GUI we find ourselves adding additional useful methods on top of the
 * JFace tree viewer in our subclasses. We capture those here in an interface so they
 * can be implemented by other viewers that wish to fully drive our UI. Typically this 
 * is for interesting properties in the property sheet.
 */
public interface ISystemTree {

	/**
	 * This is called to ensure all elements in a multiple-selection have the same parent in the
	 * tree viewer. Typically used to disable actions that must take place on a coordinated
	 * selection.
	 * @return true if the elements of the selection in the viewer all have the same parent 
	 */
	public boolean sameParent();

	/**
	 * Called to select an object within the tree, and optionally expand it.
	 * @param element the element in the tree to select
	 * @param expand true if the element is to be expanded
	 */
	public void select(Object element, boolean expand);

	/**
	 * @param element the element in the tree to query
	 * @return the number of immediate children in the tree, for the given tree node
	 */
	public int getChildCount(Object element);

	/**
	 * This is called to accurately get the parent object for the current selection
	 * for this viewer. 
	 * The getParent() method in the adapter is unreliable since adapted objects are
	 * unaware of the context which can change via filtering and view options.
	 * @return the parent of the selection
	 */
	public Object getSelectedParent();

	/**
	 * This returns the element immediately before the first selected element in this tree level.
	 * Often needed for enablement decisions for move up actions.
	 * @return the object prior to the selection, null if there is none
	 */
	public Object getPreviousElement();

	/**
	 * This returns the element immediately after the last selected element in this tree level
	 * Often needed for enablement decisions for move down actions.
	 * @return the object after the selection, null if there is none
	 */
	public Object getNextElement();

	/**
	 * This is called to walk the tree back up to the roots and return the visible root
	 * node for the first selected object.
	 * @return the root object
	 */
	public Object getRootParent();

	/**
	 * This returns an array containing each element in the tree, up to but not including the root.
	 * The array is in reverse order, starting at the leaf and going up.
	 * @param element the element from which to begin
	 * @return the array of objects in the path from leaf to root. Excluding the leaf and root.
	 */
	public Object[] getElementNodes(Object element);

	/**
	 * Helper method to determine if a given object is currently selected.
	 * Considers an element to be "selected" if a child node of the given object is currently selected.
	 * @param parentElement the element to query
	 * @return true if the element covers any portion of the selection
	 */
	public boolean isSelectedOrChildSelected(Object parentElement);

	/**
	 * Called when a property is updated and we need to inform the Property Sheet viewer.
	 * There is no formal mechanism for this so we simulate a selection changed event as
	 * this is the only event the property sheet listens for.
	 */
	public void updatePropertySheet();

	/**
	 * Returns the tree item of the first selected object. Used for setViewerItem in a resource
	 * change event.
	 * @return the item 
	 */
	public Item getViewerItem();

	/**
	 * @return true if any of the selected items are currently expanded
	 */
	public boolean areAnySelectedItemsExpanded();

	/**
	 * @return true if any of the selected items are expandable but not yet expanded
	 */
	public boolean areAnySelectedItemsExpandable();
}