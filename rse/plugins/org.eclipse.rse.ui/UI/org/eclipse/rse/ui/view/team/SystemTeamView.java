/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.view.team;
import java.util.Vector;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.ISystemResourceChangeEvent;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeListener;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.view.ISystemSelectAllTarget;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;


//import org.eclipse.rse.ui.view.ISystemViewElementAdapter;

/**
 * We specialize tree viewer for the Team view, so we know
 *  when we are dealing with the team view in common code.
 */
public class SystemTeamView extends TreeViewer implements ISystemSelectAllTarget, ISystemResourceChangeListener
{
	private SystemTeamViewPart teamViewPart;

	/**
	 * @param parent
	 */
	public SystemTeamView(Composite parent, SystemTeamViewPart teamViewPart)
	{
		super(parent);
		this.teamViewPart = teamViewPart;
		SystemWidgetHelpers.setHelp(getTree(), SystemPlugin.HELPPREFIX+"teamview");
	}

	/**
	 * @param parent
	 * @param style
	 */
	public SystemTeamView(Composite parent, int style, SystemTeamViewPart teamViewPart)
	{
		super(parent, style);
		this.teamViewPart = teamViewPart;
		SystemWidgetHelpers.setHelp(getTree(), SystemPlugin.HELPPREFIX+"teamview");
	}

	/**
	 * @param tree
	 */
	public SystemTeamView(Tree tree, SystemTeamViewPart teamViewPart)
	{
		super(tree);
		this.teamViewPart = teamViewPart;
		SystemWidgetHelpers.setHelp(getTree(), SystemPlugin.HELPPREFIX+"teamview");		
	}
	
	/**
	 * Return the part view part of this tree view
	 */
	public SystemTeamViewPart getTeamViewPart()
	{
		return teamViewPart;
	}

	/**
	 * This returns an array containing each element in the tree, up to but not including the root.
	 * The array is in reverse order, starting at the leaf and going up.
	 */
	public Object[] getElementNodes(Object element)
	{
		Widget w = findItem(element);
		if ((w != null) && (w instanceof TreeItem))
			return getElementNodes((TreeItem)w);
		return null;
	}
	/**
	 * This returns an array containing each element in the tree, up to but not including the root.
	 * The array is in reverse order, starting at the leaf and going up.
	 * This flavour is optimized for the case when you have the tree item directly.
	 */
	public Object[] getElementNodes(TreeItem item)
	{
		Vector v = new Vector();
		v.addElement(item.getData());
		while (item != null)
		{
		   item = item.getParentItem();
		   if (item != null)    	
				v.addElement(item.getData());
		}
		Object[] nodes = new Object[v.size()];
		for (int idx=0; idx<nodes.length; idx++)
		   nodes[idx] = v.elementAt(idx);
		return nodes;			
	}
	
	/**
	 * Use findItem to find a tree item given its data object, or null if given object not in tree.
	 */
	public TreeItem findTreeItem(Object dataObject)
	{
		Widget widget = findItem(dataObject);
		if (widget instanceof TreeItem)
			return (TreeItem)widget;
		else
			return	null;
	}
	/**
	 * Given a tree item, search the immediate children for an item representing the given object.
	 */
	public TreeItem findChildTreeItem(TreeItem parentItem, Object dataObject)
	{
		TreeItem[] childItems = parentItem.getItems();
		TreeItem childItem = null;
		for (int idx=0; (childItem==null) && (idx<childItems.length); idx++)
		{
			if ((childItems[idx].getData() != null) && childItems[idx].getData().equals(dataObject))
				childItem = childItems[idx];
		}
		return childItem;
	}	

	/**
	 * Called when a property is updated and we need to inform the Property Sheet viewer.
	 * There is no formal mechanism for this so we simulate a selection changed event as
	 *  this is the only event the property sheet listens for.
	 */
	public void updatePropertySheet()
	{    	
		ISelection selection = getSelection();
		if (selection == null)
		  return;
		// create an event
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		// fire the event
		fireSelectionChanged(event);
	}
	
	// ----------------------------------------
	// ISystemResourceChangeListener methods...
	// ----------------------------------------
	
	/**
	 * Called when something changes in the model
	 */
	public void systemResourceChanged(ISystemResourceChangeEvent event)
	{
		   int type = event.getType();    	   
		   Object src = event.getSource();
		   Object parent = event.getParent();
		   switch(type)
		   {
				case ISystemResourceChangeEvents.EVENT_COLLAPSE_ALL:
					collapseToLevel(getInput(), AbstractTreeViewer.ALL_LEVELS);
					break;
		   }
	}	
	
	/**
	 * Return the shell
	 */
	public Shell getShell()
	{
		return super.getControl().getShell();
	}
			
	// -----------------------------------------------------------------
	// ISystemSelectAllTarget methods to facilitate the global action...
	// -----------------------------------------------------------------
	/**
	 * Return true if select all should be enabled for the given object.
	 * For a tree view, you should return true if and only if the selected object has children.
	 * You can use the passed in selection or ignore it and query your own selection.
	 */
	public boolean enableSelectAll(IStructuredSelection selection)
	{
		Tree tree = getTree();
		TreeItem[] items = tree.getSelection();
		if ((items==null) || (items.length!=1)) // only allow for single selections
		  return false;

		TreeItem ti = items[0];
		int count = getItemCount(ti);
		if (count == 1) // is it a dummy?
		{
			if ((getItems(ti)[0]).getData() == null)
			  count = 0; // assume a dummy
		}
		return (count > 0);
	}
	/**
	 * When this action is run via Edit->Select All or via Ctrl+A, perform the
	 * select all action. For a tree view, this should select all the children 
	 * of the given selected object. You can use the passed in selected object
	 * or ignore it and query the selected object yourself. 
	 */
	public void doSelectAll(IStructuredSelection selection)
	{
		Tree tree = getTree();
		TreeItem[] currSel = tree.getSelection();
		TreeItem[] childItems = currSel[0].getItems();
		if (childItems.length == 0)
		  return;
		tree.setSelection(childItems);
		Object[] childObjects = new Object[childItems.length];
		for (int idx=0; idx<childObjects.length; idx++)
		   childObjects[idx] = childItems[idx].getData();
		fireSelectionChanged(
		   new SelectionChangedEvent(this,
				 new StructuredSelection(childObjects)));
	}

	/**
	 * Handles a collapse-selected request
	 */
	public void collapseSelected()
	{		
		TreeItem[] selectedItems = ((Tree)getControl()).getSelection();		
		if ((selectedItems != null) && (selectedItems.length>0))
		{
			for (int idx=0; idx<selectedItems.length; idx++)
			   selectedItems[idx].setExpanded(false);
		}
	}
	/**
	 * Handles an expand-selected request
	 */
	public void expandSelected()
	{		
		TreeItem[] selectedItems = ((Tree)getControl()).getSelection();		
		if ((selectedItems != null) && (selectedItems.length>0))
		{
			for (int idx=0; idx<selectedItems.length; idx++)
			{
			   if (!selectedItems[idx].getExpanded())
			   {
				  createChildren(selectedItems[idx]);        	   	  
			   }
			   selectedItems[idx].setExpanded(true);
			}
		}
	}
	
}