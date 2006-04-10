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

package org.eclipse.rse.model;
/**
 * Interface of event ID constants
 */
public interface ISystemResourceChangeEvents
{
	/**
	 * The event is specifically a filter reference add (filter added)
	 */
	public static final int EVENT_ADD_FILTER_REFERENCE = 10;	
	/**
	 * The event is specifically a filter reference rename (filter renamed)
	 */
	public static final int EVENT_RENAME_FILTER_REFERENCE = 15;	
	/**
	 * The event is specifically a filter reference delete (filter deleted)
	 */
	public static final int EVENT_DELETE_FILTER_REFERENCE = 20;		
	/**
	 * The event is specifically a filter reference change (filter strings changes)
	 */
	public static final int EVENT_CHANGE_FILTER_REFERENCE = 25;
	/**
	 * The event is specifically a filter reference move (filters reordered)
	 */
	public static final int EVENT_MOVE_FILTER_REFERENCES = 30;

	/**
	 * The event is specifically a filter string reference add (filterstring added)
	 */
	public static final int EVENT_ADD_FILTERSTRING_REFERENCE = 41;	
	/**
	 * The event is specifically a filter string reference delete (filterstring deleted)
	 */
	public static final int EVENT_DELETE_FILTERSTRING_REFERENCE = 42;		
	/**
	 * The event is specifically a filter string reference change (filterstring changed)
	 */
	public static final int EVENT_CHANGE_FILTERSTRING_REFERENCE = 43;
	/**
	 * The event is specifically a filter string reference move (filterstrings reordered)
	 */
	public static final int EVENT_MOVE_FILTERSTRING_REFERENCES = 44;

	/**
	 * The event is a resource add.
	 */
	public static final int EVENT_ADD = 50;
	/**
	 * The event is a multi-resource add.
	 */
	public static final int EVENT_ADD_MANY = 51;

	/**
	 * The event is a resource add. The resource is added relative to the "previous" attribute .
	 */
	public static final int EVENT_ADD_RELATIVE = 53;
	/*
	 * The event is a multi-resource add. The resources are added relative to the "previous" attribute
	 *
	public static final int EVENT_ADD_MANY_RELATIVE = 54;
	*/

	/**
	 * After an add, you wish to expand the parent to reveal and select the new child.
	 * This is a harmless operation if the parent was already expanded when EVENT_ADD was sent.
	 */
	public static final int EVENT_REVEAL_AND_SELECT = 52;
	/**
	 * The event is a single resource deletion.
	 */
	public static final int EVENT_DELETE = 55;	
	/*
	 * The event is a single remote resource deletion. You need only set the source, not the parent
	 *
	public static final int EVENT_DELETE_REMOTE = 56;	*/

	/**
	 * The event is a multiple resource deletion. 
	 */
	public static final int EVENT_DELETE_MANY = 60;		
	/*
	 * The event is a multiple resource deletion. You need only set the multisource, not the parent
	 *
	public static final int EVENT_DELETE_REMOTE_MANY = 61;		*/

	/**
	 * The event is a resource rename.
	 */
	public static final int EVENT_RENAME = 65;	
	/*
	 * The event is a remote resource rename. You need only set the source, not the parent
	 *
	public static final int EVENT_RENAME_REMOTE = 66;	*/

	/**
	 * The event is a resource move within the same children set
	 */
	//public static final int EVENT_MOVE = 70;	
	/**
	 * The event is a multiple resource move within the same children set
	 */
	public static final int EVENT_MOVE_MANY = 75;
	/**
	 * The event is a resource change. This results in a shallow refresh: only direct children are refreshed.
	 */
	//public static final int EVENT_CHANGE = 80;
    /**
	 * The event is an icon change event
	 */
	public static final int EVENT_ICON_CHANGE = 81;
	/**
	 * The event is a full refresh event: all expanded sub-nodes are re-queried for their children, unexpanded nodes lose their children cache.
	 */
	public static final int EVENT_REFRESH = 82;
	/**
	 * The event is a selection-dependent refresh event: all expanded sub-nodes are re-queried for their children, unexpanded nodes lose their children cache.
	 */
	public static final int EVENT_REFRESH_SELECTED = 83;
	/**
	 * The event is a selection-dependent refresh event: refreshes the parent of the current selections
	 */
	public static final int EVENT_REFRESH_SELECTED_PARENT = 84;
	/**
	 * The event is a selection-dependent refresh event: from the filter level, all expanded sub-nodes are re-queried for their children, unexpanded nodes lose their children cache.
	 */
	public static final int EVENT_REFRESH_SELECTED_FILTER = 135;
	/**
	 * The event is refreshes a remote object (has an ISystemViewRemoteElementAdapter) given either the remote object or a string that will match on getAbsoluteName.
	 * The tricky part about remote objects is their actual memory object changes on each refresh, so to find one in the tree we must use something
	 * more permanent: hence the use of getAbsoluteName to find it. 
	 * <p>
	 * You can optionally pass a child remote object, or string, or vector of objects or strings, in the parent parameter, and it/they will be selected after the refresh.
	 * If it a string then it must be the result of getAbsoluteName on the adapter.
	 */
	public static final int EVENT_REFRESH_REMOTE = 85;

	/**
	 * The event is a resource property change.
	 */
	public static final int EVENT_PROPERTY_CHANGE = 86;
	/**
	 * The event is a request to update the property sheet of whatever is currently selected.
	 */
	public static final int EVENT_PROPERTYSHEET_UPDATE = 87;

	/**
	 * The event is a resource property change that invalidates child nodes
	 *  in the GUI (eg, hostname change means the expanded information should
	 *  be collapsed)
	 */
	public static final int EVENT_MUST_COLLAPSE = 90;	
	/**
	 * The event is a full collapse of the RSE tree
	 * Pass "false" for the src value to prevent the memory flush, else
	 *  pass any dummy value for the src to prevent crash, but it is ignored
	 */
	public static final int EVENT_COLLAPSE_ALL = 91;	
	/**
	 * The event is a collapse of the selected elements in the tree
	 * Pass any dummy value for the src to prevent crash, but it is ignored
	 */
	public static final int EVENT_COLLAPSE_SELECTED = 92;
	/**
	 * The event is an expand of the selected elements in the tree
	 * Pass any dummy value for the src to prevent crash, but it is ignored
	 */
	public static final int EVENT_EXPAND_SELECTED = 93;
	/**
	 * The event is a generic notification that the children have changed
	 *  and must be refreshed.
	 */
	public static final int EVENT_CHANGE_CHILDREN = 95;
	/**
	 * The event is simply to force selection of the given object.
	 */
	public static final int EVENT_SELECT = 100;
	/**
	 * The event is to select a remote object
	 */
	public static final int EVENT_SELECT_REMOTE = 101;

	/**
	 * The event is to both select and expand the given object.
	 */
	public static final int EVENT_SELECT_EXPAND = 105;
	/**
	 * The event is to log a command that has been run
	 */
	public static final int EVENT_COMMAND_RUN = 110;
	/**
	 * The event is to log a message from a command that has been run
	 */
	public static final int EVENT_COMMAND_MESSAGE = 115;
	/**
	 * The event is to replace the children (similar to EVENT_ADD_MANY), it will 
	 * expand also
	 */
	public static final int EVENT_REPLACE_CHILDREN = 120;
	/**
	 * The event is to log a command that has been run
	 */
	public static final int EVENT_COMPILE_COMMAND_RUN = 125;
	/**
	 * The event is to update the command history drop-down in the Commands view
	 */
	public static final int EVENT_COMMAND_HISTORY_UPDATE = 130;

	/**
	 * The event is to update the commands view when a command is finished
	 * @deprecated use EVENT_COMMAND_SHELL_FINISHED
	 */
	public static final int EVENT_COMMAND_FINISHED = 140;
	
	/**
	 * The event is to update the commands view when a command is finished
	 */
	public static final int EVENT_COMMAND_SHELL_FINISHED = 140;
	public static final int EVENT_COMMAND_SHELL_REMOVED = 141;
	
	/**
	 * The event is to update the search view when a search is finished
	 */
	public static final int EVENT_SEARCH_FINISHED = 150;

    /**
     * Predefined event object for a property sheet update.
     */
    public static final ISystemResourceChangeEvent PROPERTYSHEET_UPDATE_EVENT = 
      new SystemResourceChangeEvent("dummy",EVENT_PROPERTYSHEET_UPDATE,null);
}