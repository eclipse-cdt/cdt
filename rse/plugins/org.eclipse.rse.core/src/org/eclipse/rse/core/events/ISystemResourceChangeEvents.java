/********************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 * David McKnight   (IBM)        - [190805] [performance][dstore] Right-click > Disconnect on a dstore connection is slow and spawns many Jobs
 ********************************************************************************/

package org.eclipse.rse.core.events;

import org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier;

/**
 * Interface of event ID constants
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISystemResourceChangeEvents
{
	/**
	 * The event is specifically a filter reference add (filter added)
	 * An ISystemFilter is expected as a parameter of this event
	 */
	public static final int EVENT_ADD_FILTER_REFERENCE = 10;
	/**
	 * The event is specifically a filter reference rename (filter renamed)
	 * An ISystemFilter is expected as a parameter of this event
	 */
	public static final int EVENT_RENAME_FILTER_REFERENCE = 15;
	/**
	 * The event is specifically a filter reference delete (filter deleted)
	 * An ISystemFilter is expected as a parameter of this event
	 */
	public static final int EVENT_DELETE_FILTER_REFERENCE = 20;
	/**
	 * The event is specifically a filter reference change (filter strings changes)
	 * An ISystemFilter is expected as a parameter of this event
	 */
	public static final int EVENT_CHANGE_FILTER_REFERENCE = 25;

	/**
	 * The event is specifically a filter reference move (filters reordered)
	 * An array of ISystemFilter[] is the expected multi-source
	 * parameter.  The source is the first item in that array.
	 */
	public static final int EVENT_MOVE_FILTER_REFERENCES = 30;

	/**
	 * The event is specifically a filter string reference add (filterstring added)
	 * An ISystemFilterString is expected as a parameter of this event
	 */
	public static final int EVENT_ADD_FILTERSTRING_REFERENCE = 41;

	/**
	 * The event is specifically a filter string reference delete (filterstring deleted)
	 * An ISystemFilterString is expected as a parameter of this event
	 */
	public static final int EVENT_DELETE_FILTERSTRING_REFERENCE = 42;

	/**
	 * The event is specifically a filter string reference change (filterstring changed)
	 * An ISystemFilterString is expected as a parameter of this event
	 */
	public static final int EVENT_CHANGE_FILTERSTRING_REFERENCE = 43;

	/**
	 * The event is specifically a filter string reference move (filterstrings reordered)
	 * An array of ISystemFilterString[] is the expected multi-source
	 * parameter.  The source is the first item in that array.
	 */
	public static final int EVENT_MOVE_FILTERSTRING_REFERENCES = 44;

	/**
	 * The event is a resource add.
	 * Any RSE object is the expected parameter
	 */
	public static final int EVENT_ADD = 50;

	/**
	 * The event is a multi-resource add.
	 * An array of RSE objects (i.e. Object[]) is the multi-source
	 * parameter (the source is the first item in that array) and a parent
	 * RSE object is expected.
	 */
	public static final int EVENT_ADD_MANY = 51;

	/**
	 * The event is a resource add. The resource is added relative to the "previous" attribute .
	 * The expected parameters are an RSE object and it's parent RSE object
	 */
	public static final int EVENT_ADD_RELATIVE = 53;

	/**
	 * After an add, you wish to expand the parent to reveal and select the new child.
	 * This is a harmless operation if the parent was already expanded when EVENT_ADD was sent.
	 * The expected parameters are an RSE object and the selected object
	 */
	public static final int EVENT_REVEAL_AND_SELECT = 52;

	/**
	 * The event is a single resource deletion.
	 * An RSE object is the expected parameter
	 */
	public static final int EVENT_DELETE = 55;

	/**
	 * The event is a multiple resource deletion.
	 * An array of RSE objects (i.e. Object[]) is the expected multi-source
	 * parameter.  The source is the first item in that array.
	 */
	public static final int EVENT_DELETE_MANY = 60;

	/**
	 * The event is a resource rename.
	 * An RSE object is the expected parameter
	 */
	public static final int EVENT_RENAME = 65;

	/**
	 * The event is a multiple resource move within the same children set
	 * An array of RSE objects (i.e. Object[]) is the multi-source
	 * parameter (the source is the first item in that array) and a parent
	 * RSE object is expected.
	 */
	public static final int EVENT_MOVE_MANY = 75;

	/**
	 * The event is an icon change event A source RSE object or an array of
	 * source RSE objects and the parent RSE object are the expected parameters
	 * 
	 * @since 3.1 an array of source RSE objects is allowed
	 */
	public static final int EVENT_ICON_CHANGE = 81;

	/**
	 * Refresh the single item passed in the "source" field of the event.
	 *
	 * All expanded sub-nodes are re-queried for their children, unexpanded
	 * nodes lose their children cache. Selection is not maintained by this
	 * event (use EVENT_REFRESH_REMOTE instead to maintain the selection).
	 *
	 * A source RSE object to refresh is the expected parameter
	 */
	public static final int EVENT_REFRESH = 82;

	/**
	 * Refresh the items currently selected in the SystemView.
	 *
	 * All expanded sub-nodes are re-queried for their children, unexpanded nodes
	 * lose their children cache. After refreshing, selection of the currently selected
	 * elements is restored if possible (in case an absoluteName is available).
	 *
	 * In case any of the selected items is a leaf node, the parent of that
	 * leaf node is refreshed rather than the leaf node itself. In this particular
	 * case, a multiselect is not considered properly.
	 *
	 * The SystemScratchpadView also listens to this event and refreshes those
	 * elements that are selected in it.
	 *
	 * @deprecated obtain the selection yourself and do EVENT_REFRESH or EVENT_REFRESH_REMOTE
	 */
	public static final int EVENT_REFRESH_SELECTED = 83;

	/**
	 * Refresh the parent of the first item currently selected in the SystemView.
	 *
	 * This only refreshes the parent TreeItem of the first item in the selection.
	 * It does not consider multiselect, multiple occurrences of the Item under multiple
	 * filters, and does not maintain the current selection.
	 *
	 * @deprecated obtain the selection yourself and do EVENT_REFRESH or EVENT_REFRESH_REMOTE
	 */
	public static final int EVENT_REFRESH_SELECTED_PARENT = 84;

	/**
	 * Refresh the filter under which the first item currently selected in the
	 * SystemView is found.
	 *
	 * From the filter level, all expanded sub-nodes are re-queried
	 * for their children, unexpanded nodes lose their children cache.
	 * After refreshing, selection of the currently selected elements
	 * is restored if possible. Multiselect is not considered properly.
	 *
	 * @deprecated Refreshing a particular context(filter) only can lead
	 *     to inconsistencies, so better obtain the selection yourself
	 *     and do EVENT_REFRESH or EVENT_REFRESH_REMOTE
	 */
	public static final int EVENT_REFRESH_SELECTED_FILTER = 135;

	/**
	 * Refresh a remote object in the SystemView, given either the remote
	 * object or a string that will match on getAbsoluteName, and optionally
	 * (re)select a list of objects after refreshing.
	 *
	 * An object is considered remote if it has an adapter that implements
	 * {@link ISystemViewElementAdapter} where the adapter returns true for
	 * the isRemote(Object) call.  This method refreshes all occurrences of
	 * the remote object, even under multiple filters.  The tricky part about
	 * remote objects is their actual memory object changes on each refresh,
	 * so to find one in the tree we must use something more permanent: hence
	 * the use of getAbsoluteName to find it.
	 * <p>
	 * You can optionally pass a child remote object, or string, or Vector of
	 * objects or strings, in the "parent" parameter of the event, and it/they
	 * will be selected after the refresh. When passing a string, it must be
	 * the result of {@link IRemoteObjectIdentifier#getAbsoluteName(Object)}
	 * on the adapter.
	 *
	 * A remote RSE object is the expected source parameter
	 */
	public static final int EVENT_REFRESH_REMOTE = 85;

	/**
	 * The event is a resource property change.
	 * A source RSE object and it's parent RSE object are the expected parameters
	 */
	public static final int EVENT_PROPERTY_CHANGE = 86;

	/**
	 * The event is a request to update the property sheet of whatever is currently selected.
	 * A source RSE object and it's parent RSE object are the expected parameters
	 */
	public static final int EVENT_PROPERTYSHEET_UPDATE = 87;

	/**
	 * The event is a resource property change that invalidates child nodes
	 *  in the GUI (eg, hostname change means the expanded information should
	 *  be collapsed)
	 *  A source RSE object is the expected parameter
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
	 *  A source RSE object and (optionally) it's parent RSE object are the
	 *  expected parameters
	 */
	public static final int EVENT_CHANGE_CHILDREN = 95;

	/**
	 * The event is simply to force selection of the given object.
	 * A source RSE object is the expected parameter
	 */
	public static final int EVENT_SELECT = 100;

	/**
	 * Select one or more remote objects.
	 *
	 * The "src" parameter holds a remote object, or string, or Vector of
	 * objects or strings. When passing a string, it must be the result of
	 * {@link IRemoteObjectIdentifier#getAbsoluteName(Object)}
	 * on the adapter of the object.
	 * The "parent" parameter can optionally hold a model object that is
	 * the parent of the objects to be refreshed, in order to optimize searches.
	 */
	public static final int EVENT_SELECT_REMOTE = 101;

	/**
	 * The event is to both select and expand the given object.
	 * A source RSE object is the expected parameter
	 */
	public static final int EVENT_SELECT_EXPAND = 105;

	/**
	 * The event is to log a command that has been run
	 * A source RSE object is the expected parameter
	 */
	public static final int EVENT_COMMAND_RUN = 110;

	/**
	 * The event is to log a message from a command that has been run
	 * A source RSE object and it's parent RSE object are the expected parameters
	 */
	public static final int EVENT_COMMAND_MESSAGE = 115;

	/**
	 * The event is to replace the children (similar to EVENT_ADD_MANY), it will
	 * expand also
	 * An array of RSE objects (i.e. Object[]) is the multi-source
	 * parameter (the source is the first item in that array) and a parent
	 * RSE object is expected.
	 */
	public static final int EVENT_REPLACE_CHILDREN = 120;

	/**
	 * The event is to log a command that has been run
	 * @deprecated
	 */
	public static final int EVENT_COMPILE_COMMAND_RUN = 125;

	/**
	 * The event is to update the command history drop-down in the remote shell view
	 * A source RSE object is the expected parameter
	 *
	 * TODO should be moved out of core since this is command-specific
	 */
	public static final int EVENT_COMMAND_HISTORY_UPDATE = 130;

	/**
	 * The event is to update the remote shell view when a command is finished
	 * @deprecated use EVENT_COMMAND_SHELL_FINISHED
	 */
	public static final int EVENT_COMMAND_FINISHED = 140;

	/**
	 * The event is to update the remote shell view when a command is finished
	 * A source RSE object is the expected parameter
	 */
	public static final int EVENT_COMMAND_SHELL_FINISHED = 140;

	/**
	 * The event is to indicate that a shell has been removed
	 * A source RSE object is the expected parameter
	 */
	public static final int EVENT_COMMAND_SHELL_REMOVED = 141;

	/**
	 * The event is to update the search view when a search is finished
	 * A IHostSearchResultConfiguration is the expected parameter
	 */
	public static final int EVENT_SEARCH_FINISHED = 150;

	/**
	 * Predefined event object for a property sheet update.
	 */
	public static final ISystemResourceChangeEvent PROPERTYSHEET_UPDATE_EVENT =
		new SystemResourceChangeEvent("dummy",EVENT_PROPERTYSHEET_UPDATE,null); //$NON-NLS-1$
}