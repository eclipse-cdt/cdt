/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Tobias Schwarz   (Wind River) - [173267] "empty list" should not be displayed
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core
 * Martin Oberhuber (Wind River) - [218524][api] Remove deprecated ISystemViewInputProvider#getShell()
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 ********************************************************************************/

package org.eclipse.rse.ui.view;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.ISystemViewInputProvider;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;



/**
 * This is a base class that a provider of root nodes to the remote systems tree
 * viewer part can use as a parent class.
 *
 * This class existed in RSE 1.0, was made "internal" for RSE 2.0 and restored
 * as API for RSE 3.0.
 *
 * @since 3.0
 */
public abstract class SystemAbstractAPIProvider
       implements ISystemViewInputProvider
{
	protected Viewer viewer;
	/**
	 * @deprecated don't use this field
	 */
	protected ISystemRegistry sr;

	/**
	 * @deprecated don't use this field
	 */
	protected Object[] emptyList = new Object[0];
	/**
	 * @deprecated don't use this field
	 */
	protected Object[] msgList   = new Object[1];
	/**
	 * @deprecated Use {@link #checkForEmptyList(Object[], Object, boolean)} instead.
	 */
	protected SystemMessageObject nullObject     = null;
	/**
	 * This field was renamed from canceledObject in RSE 3.0.
	 * 
	 * @since org.eclipse.rse.ui 3.0
	 * @deprecated don't use this field
	 */
	protected SystemMessageObject cancelledObject = null;
	/**
	 * @deprecated don't use this field
	 */
	protected SystemMessageObject errorObject    = null;

	private Preferences fPrefStore = null;

	/**
	 * Constructor
	 */
	public SystemAbstractAPIProvider()
	{
		super();
		sr = RSECorePlugin.getTheSystemRegistry();
	}

    /**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 */
    public Object getAdapter(Class adapterType)
    {
   	    return Platform.getAdapterManager().getAdapter(this, adapterType);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#setViewer(java.lang.Object)
     */
    public void setViewer(Object viewer)
    {
    	this.viewer = (Viewer)viewer;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#getViewer()
     */
    public Object getViewer()
    {
    	return viewer;
    }

    protected final void initMsgObjects()
 	{
 		nullObject     = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_EMPTY),ISystemMessageObject.MSGTYPE_EMPTY, null);
 		cancelledObject = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_LIST_CANCELLED),ISystemMessageObject.MSGTYPE_CANCEL, null);
 		errorObject    = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_FAILED),ISystemMessageObject.MSGTYPE_ERROR, null);
 	}

	/**
	 * In getChildren, return <samp>checkForEmptyList(children, parent,
	 * true/false)</samp> versus your array directly. This method checks for a
	 * null array which is not allowed and replaces it with an empty array. If
	 * true is passed then it returns the "Empty list" message object if the
	 * array is null or empty.
	 * <p>
	 * <i>Callable by subclasses. Do not override.</i> <br>
	 *
	 * @param children The list of children.
	 * @param parent The parent for the children.
	 * @param returnNullMsg <code>true</code> if an "Empty List" message should
	 * 		be returned.
	 * @return The list of children, a list with the "Empty List" message object
	 * 	or an empty list.
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
	 */
    protected Object[] checkForEmptyList(Object[] children, Object parent, boolean returnNullMsg) {
    	if ((children == null) || (children.length == 0)) {
    		if (fPrefStore == null) {
    			fPrefStore = RSEUIPlugin.getDefault().getPluginPreferences();
    		}
    		if (!returnNullMsg
    				|| (fPrefStore != null && !fPrefStore
    						.getBoolean(ISystemPreferencesConstants.SHOW_EMPTY_LISTS))) {
    			return emptyList;
    		} else {
    			return new Object[] {
    				new SystemMessageObject(
    					RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_EMPTY),
    					ISystemMessageObject.MSGTYPE_EMPTY,
    					parent)};
    		}
    	}
    	return children;
    }

    /**
     * In getChildren, return checkForNull(children, true/false) vs your array directly.
     * This method checks for a null array which not allow and replaces it with an empty array.
     * If true is passed then it returns the "Empty list" message object if the array is null or empty
     *
     * @deprecated Use {@link #checkForEmptyList(Object[], Object, boolean)} instead.
     */
    protected Object[] checkForNull(Object[] children, boolean returnNullMsg)
    {
	   if ((children == null) || (children.length==0))
	   {
	   	 if (!returnNullMsg)
           return emptyList;
         else
         {
	 	   if (nullObject == null)
	 	     initMsgObjects();
	 	   msgList[0] = nullObject;
	 	   return msgList;
         }
	   }
       else
         return children;
    }

    /**
     * Return the "Operation cancelled by user" msg as an object array so can be used to answer getChildren()
     */
    protected Object[] getCancelledMessageObject()
    {
		 if (cancelledObject == null)
		   initMsgObjects();
		 msgList[0] = cancelledObject;
		 return msgList;
    }

    /**
     * Return the "Operation failed" msg as an object array so can be used to answer getChildren()
     */
    protected Object[] getFailedMessageObject()
    {
		 if (errorObject == null)
		   initMsgObjects();
		 msgList[0] = errorObject;
		 return msgList;
    }

	/**
	 * Return true if we are listing connections or not, so we know whether we are interested in
	 *  connection-add events
	 */
	public boolean showingConnections()
	{
		return false;
	}

	// ------------------
	// HELPER METHODS...
	// ------------------
    /**
     * Returns the implementation of ISystemViewElement for the given
     * object.  Returns null if the adapter is not defined or the
     * object is not adaptable.
     */
    protected ISystemViewElementAdapter getViewAdapter(Object o)
    {
    	return SystemAdapterHelpers.getViewAdapter(o);
    }

    /**
     * Returns the implementation of ISystemRemoteElement for the given
     * object.  Returns null if this object does not adaptable to this.
     */
    protected ISystemRemoteElementAdapter getRemoteAdapter(Object o)
    {
    	return SystemAdapterHelpers.getRemoteAdapter(o);
    }
}