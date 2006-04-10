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
 *  JFace tree viewer, in our subclasses. We capture those here in an interface so they
 *  can be implemented by other viewers that wish to fully drive our UI. Typically this 
 *  is for interesting properties in the property sheet.
 * <p>
 * Ultimately, these are methods that AbstractTreeViewer itself should have!
 */
public interface ISystemTree 
{


	/**
	 * This is called to ensure all elements in a multiple-selection have the same parent in the
	 *  tree viewer. If they don't we automatically disable all actions. 
	 * <p>
	 * Designed to be as fast as possible by going directly to the SWT widgets
	 */
	public boolean sameParent();
    /**
     * Called to select an object within the tree, and optionally expand it
     */   
    public void select(Object element, boolean expand);
	/**
	 * Return the number of immediate children in the tree, for the given tree node
	 */
    public int getChildCount(Object element);
	/**
	 * This is called to accurately get the parent object for the current selection
	 *  for this viewer. 
	 * <p>
	 * The getParent() method in the adapter is very unreliable... adapters can't be sure
	 * of the context which can change via filtering and view options.
	 */
	public Object getSelectedParent();
	/**
	 * This returns the element immediately before the first selected element in this tree level.
	 * Often needed for enablement decisions for move up actions.
	 */
	public Object getPreviousElement();
	/**
	 * This returns the element immediately after the last selected element in this tree level
	 * Often needed for enablement decisions for move down actions.
	 */
	public Object getNextElement();
	/**
	 * This is called to walk the tree back up to the roots and return the visible root
	 *  node for the first selected object.
	 */
	public Object getRootParent();
	/**
	 * This returns an array containing each element in the tree, up to but not including the root.
	 * The array is in reverse order, starting at the leaf and going up.
	 */
	public Object[] getElementNodes(Object element);
	/**
	 * Helper method to determine if a given object is currently selected.
	 * Does consider if a child node of the given object is currently selected.
	 */
	public boolean isSelectedOrChildSelected(Object parentElement);
    /**
     * Called when a property is updated and we need to inform the Property Sheet viewer.
     * There is no formal mechanism for this so we simulate a selection changed event as
     *  this is the only event the property sheet listens for.
     */
    public void updatePropertySheet();	
    /**
     * Returns the tree item of the first selected object. Used for setViewerItem in a resource
     *  change event.
     */
    public Item getViewerItem();

    /**
     * Returns true if any of the selected items are currently expanded
     */
    public boolean areAnySelectedItemsExpanded();    
    /**
     * Returns true if any of the selected items are expandable but not yet expanded
     */
    public boolean areAnySelectedItemsExpandable();    
}