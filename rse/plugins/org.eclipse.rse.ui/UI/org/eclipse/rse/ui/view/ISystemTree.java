/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight    (IBM)   - [187711] Select SystemView APIs exposed by the ISystemTree interface
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 *******************************************************************************/

package org.eclipse.rse.ui.view;

import java.util.List;

import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

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
	
	/**
	 * Find the first binary-match or name-match of a remote object, given its binary object.
	 * @param remoteObject - The remote object to find.
	 * @param parentItem - Optionally, the parent item to start the search at
	 * @return TreeItem hit if found
	 * @since 3.0
	 */
	public Item findFirstRemoteItemReference(Object remoteObject, Item parentItem);
	
	/**
	 * Expand a given filter, given a subsystem that contains a reference to the filter's pool.
	 * This will expand down to the filter if needed
	 * @param parentSubSystem - the subsystem containing a reference to the filter's parent pool
	 * @param filter - the filter to find, reveal, and expand within the subsystem context
	 * @return the filter reference to the filter if found and expanded. This is a unique binary address 
	 *   within the object's in this tree, so can be used in the viewer methods to affect this particular
	 *   node.
	 * @since 3.0
	 */
	public ISystemFilterReference revealAndExpand(ISubSystem parentSubSystem, ISystemFilter filter);
	
	/**
	 * Return the Tree widget
	 * @return tree widget
	 * @since 3.0
	 */
	public Tree getTree();
	
	/**
	 * Create tree items for the specified children
	 * 
	 * @param widget the parent item for the items to create
	 * @param children the children to create items for
	 * @since 3.0
	 */
	public void createTreeItems(TreeItem widget, Object[] children);
	
	/**
	 * Recursively tries to find a given remote object. Since the object memory object 
	 *  for a remote object is not dependable we call getAbsoluteName() on the adapter to
	 *  do the comparisons. Note this does not take into account the parent connection or 
	 *  subsystem or filter, hence you must know where to start the search, else you risk
	 *  finding the wrong one.
	 *
	 * @param element the remote object to which we want to find a tree item which references it. Can be a string or an object
	 * @param elementObject the actual remote element to find, for binary matching, optionally for cases when element is a string
	 * @param matches the List to populate with hits, or <code>null</code> to
	 *    get a new List created and returned with the hits.
	 * @return the List populated with hits, or <code>null</code> if <code>null</code>
	 *    was passed in as the List to populate and no hits were found.
	 * @since 3.0
	 */
	public List findAllRemoteItemReferences(Object element, Object elementObject, List matches);
	
	/**
	 * Sets the auto expand level for the corresponding tree
	 * @param level the level to expand
	 * @since 3.0
	 */
	public void setAutoExpandLevel(int level);
	
	/**
	 * Adds a double-click listener
	 * @param listener the listener to add
	 * @since 3.0
	 */
	public void addDoubleClickListener(IDoubleClickListener listener);
	
	/**
	 * Checks whether the element is expandable or not
	 * @param elementOrTreePath the object to expand
	 * @return whether the item is expandable
	 * @since 3.0
	 */
	public boolean isExpandable(Object elementOrTreePath);

	/**
	 * Expands the parent object down to the remote object
	 * @param parentObject the parent object
	 * @param remoteObject the child object
	 * @since 3.0
	 */
	public void expandTo(Object parentObject, Object remoteObject);
	
	/**
	 * Expand to the object specified by the filter string
	 * @param filterString the string represending the object to expand to
	 * @since 3.0
	 */
	public void expandTo(String filterString);
	
	/**
	 * Adds a view filter
	 * @param filter the view filter
	 * @since 3.0
	 */
	public void addFilter(ViewerFilter filter);
	

	/**
	 * Adds a selection changed listener
	 * @param listener the listener
	 * @since 3.0
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener);

}
