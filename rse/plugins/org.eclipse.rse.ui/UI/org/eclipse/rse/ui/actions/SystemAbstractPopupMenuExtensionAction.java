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

package org.eclipse.rse.ui.actions;

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


/**
 * This is a base class to simplify the creation of actions supplied via the
 * org.eclipse.rse.core.popupMenus extension point.
 * <p>
 * The only method you must implement is {@link #run()}.
 * You may optionally override {@link #getEnabled(Object[])}
 * <p>
 * Convenience methods are:
 * <ul>
 *  <li>{@link #getShell()}
 *  <li>{@link #getProxyAction()}
 *  <li>{@link #getSelection()}
 *  <li>{@link #getSelectionCount()}
 *  <li>{@link #getSelectedRemoteObjects()}
 *  <li>{@link #getFirstSelectedRemoteObject()}
 *  <li>{@link #getRemoteAdapter(Object)}
 *
 *  <li>{@link #getSubSystem()}
 *  <li>{@link #getSubSystemConfiguration()}
 *  <li>{@link #getSystemConnection()}
 * 
 *  <li>{@link #getRemoteObjectName(Object obj, ISystemRemoteElementAdapter adapter)}
 *  <li>{@link #getRemoteObjectSubSystemConfigurationId(Object obj, ISystemRemoteElementAdapter adapter)}
 *  <li>{@link #getRemoteObjectTypeCategory(Object obj, ISystemRemoteElementAdapter adapter)}
 *  <li>{@link #getRemoteObjectType(Object obj, ISystemRemoteElementAdapter adapter)}
 *  <li>{@link #getRemoteObjectSubType(Object obj, ISystemRemoteElementAdapter adapter)}
 *  <li>{@link #getRemoteObjectSubSubType(Object obj, ISystemRemoteElementAdapter adapter)}
 * </ul>
 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter
 * @see org.eclipse.rse.ui.dialogs.SystemPromptDialog
 */
public abstract class SystemAbstractPopupMenuExtensionAction implements IObjectActionDelegate 
{
    protected IWorkbenchPart       viewPart = null;
    protected IStructuredSelection sel = null;
    protected IAction              proxyAction;
    protected Shell                shell;
    protected static final Object[] EMPTY_ARRAY = new Object[0];

	/**
	 * Constructor 
	 */
	public SystemAbstractPopupMenuExtensionAction() 
	{
		super();
	}
	
	// ------------------------
	// OVERRIDABLE METHODS...
	// ------------------------
	
	/**
	 * The user has selected this action. This is where the actual code for the action goes.
	 */
	public abstract void run();

	/**
	 * The user has selected one or more objects. This is an opportunity to enable/disable 
	 *  this action based on the current selection. By default, it is always enabled. Return
	 *  false to disable it.
	 */
	public boolean getEnabled(Object[] currentlySelected)
	{
		return true;
	}
	
	// ---------------------------------
	// IOBJECTACTIONDELEGATE METHODS...
	// ---------------------------------

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart part) 
	{
		this.viewPart = part;
		this.proxyAction = action;
		this.shell = part.getSite().getShell();
	}
	/**
	 * Get the current view part.
	 * Handy for things like getting the shell.
	 */
	public IWorkbenchPart getActivePart()
	{
		return viewPart;
	}

	/**
     * The Eclipse-supplied proxy action has been selected to run.
     * This is the foreward to us, the actual action. This method's default
     * implementation is to simply call {@link #run()}.
     * 
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action)
	{
		run();
	}

	/**
     * Called by Eclipse when the user selects something. Our opportunity
     * to enable or disable this menu item. The default implementation of this
     * method calls getEnabled to determine if the proxy action should be enabled
     * or not, then calls setEnabled on that proxy action with the result.
     * 
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection sel) 
	{
		if (!action.isEnabled())
		  return; // defect 43471: we were overriding the enableFor attribute enablement
		if (sel instanceof IStructuredSelection)
		{
		  this.sel = (IStructuredSelection)sel;
	      action.setEnabled(getEnabled(getSelectedRemoteObjects()));	  
		}
		else
		{
		  this.sel = null;
		  action.setEnabled(false);
		}
	}	

	// ---------------------------------------------
	// CONVENIENCE METHODS FOR SUBCLASSES TO USE...
	// ---------------------------------------------
	/**
	 * For toggle actions (attribute state specified in action tag), set the toggle state
	 */
	public void setChecked(boolean checked)
	{
		proxyAction.setChecked(checked);
	}

	/**
	 * Change the enabled state of the action
	 */
	public void setEnabled(boolean enabled)
	{
		proxyAction.setEnabled(enabled);
	}

    /**
     * Return the proxy action for this action delegate
     */
    public IAction getProxyAction()
    {
    	return proxyAction;
    }	
    
    /**
     * Return the shell hosting this action
     */
    public Shell getShell()
    {
    	return shell;
    }
    
	/**
	 * Retrieve the current selected objects as a structured selection
	 */
	public IStructuredSelection getSelection()
	{
		return sel;
	}
	/**
	 * Retrieve the number of items currently selected
	 */
	public int getSelectionCount()
	{
		return ((sel==null)?0:sel.size());
	}

	/**
	 * Retrieve the currently selected objects as an array of Object objects.
	 * Array may be length 0, but will never be null, for convenience.
	 * To do anything interesting with the object, you will also need to retrieve its adapter
	 * @see #getRemoteAdapter(Object)
	 */
	public Object[] getSelectedRemoteObjects()
	{
		Object[] seld = new Object[(sel!=null) ? sel.size() : 0];
		if (sel == null)
		  return seld;
		Iterator i = sel.iterator();
		int idx=0;
		while (i.hasNext())
		  seld[idx++] = i.next();
		return seld;
	}
	/**
	 * Retrieve the first selected object, for convenience.
	 * Will be null if there is nothing selected
	 * To do anything interesting with the object, you will also need to retrieve its adapter
	 * @see #getRemoteAdapter(Object)
	 */
	public Object getFirstSelectedRemoteObject()
	{
		if (sel == null)
		  return null;
		return sel.getFirstElement();
	}
	/**
	 * Retrieve the adapters of the currently selected objects as an array of ISystemRemoteElementAdapter objects.
	 * Array may be length 0, but will never be null, for convenience.
	 */
	public ISystemRemoteElementAdapter[] getSelectedRemoteObjectAdapters()
	{
		ISystemRemoteElementAdapter[] seld = new ISystemRemoteElementAdapter[(sel!=null) ? sel.size() : 0];
		if (sel == null)
		  return seld;
		Iterator i = sel.iterator();
		int idx=0;
		while (i.hasNext())
		  seld[idx++] = getRemoteAdapter(i.next());
		return seld;
	}
	/**
	 * Retrieve the adapter of the first selected object as an ISystemRemoteElementAdapter object, for convenience.
	 * Will be null if there is nothing selected
	 */
	public ISystemRemoteElementAdapter getFirstSelectedRemoteObjectAdapter()
	{
		if (sel == null)
		  return null;
		return getRemoteAdapter(sel.getFirstElement());
	}

    /**
     * Returns the implementation of ISystemRemoteElementAdapter for the given
     * object.  Returns null if this object does not adaptable to this.
     */
    public ISystemRemoteElementAdapter getRemoteAdapter(Object o) 
    {
    	if (!(o instanceof IAdaptable)) 
          return (ISystemRemoteElementAdapter)Platform.getAdapterManager().getAdapter(o,ISystemRemoteElementAdapter.class);
    	return (ISystemRemoteElementAdapter)((IAdaptable)o).getAdapter(ISystemRemoteElementAdapter.class);
    }

    /**
     * Returns the name of the given remote object, given its remote object adapter.
     * Same as <code>adapter.getName(obj);</code>
     */
    public String getRemoteObjectName(Object obj, ISystemRemoteElementAdapter adapter)
    {
    	return adapter.getName(obj);
    }
    /**
     * Returns the id of the subsystem factory of the given remote object, given its remote object adapter.
     * Same as <code>adapter.getSubSystemConfigurationId(obj);</code>
     */
    public String getRemoteObjectSubSystemConfigurationId(Object obj, ISystemRemoteElementAdapter adapter)
    {
    	return adapter.getSubSystemConfigurationId(obj);
    }
    /**
     * Returns the type category of the given remote object, given its remote object adapter.
     * Same as <code>adapter.getRemoteTypeCategory(obj);</code>
     */
    public String getRemoteObjectTypeCategory(Object obj, ISystemRemoteElementAdapter adapter)
    {
    	return adapter.getRemoteTypeCategory(obj);
    }
    /**
     * Returns the type of the given remote object, given its remote object adapter.
     * Same as <code>adapter.getRemoteType(obj);</code>
     */
    public String getRemoteObjectType(Object obj, ISystemRemoteElementAdapter adapter)
    {
    	return adapter.getRemoteType(obj);
    }
    /**
     * Returns the subtype of the given remote object, given its remote object adapter.
     * Same as <code>adapter.getRemoteSubType(obj);</code>
     */
    public String getRemoteObjectSubType(Object obj, ISystemRemoteElementAdapter adapter)
    {
    	return adapter.getRemoteSubType(obj);
    }
    /**
     * Returns the sub-subtype of the given remote object, given its remote object adapter.
     * Same as <code>adapter.getRemoteSubSubType(obj);</code>
     */
    public String getRemoteObjectSubSubType(Object obj, ISystemRemoteElementAdapter adapter)
    {
    	return adapter.getRemoteSubSubType(obj);
    }
    /**
     * Returns the subsystem from which the selected remote objects were resolved.
     */
    public ISubSystem getSubSystem()
    {
    	ISystemRemoteElementAdapter ra = getFirstSelectedRemoteObjectAdapter();
    	if (ra != null)
    	  return ra.getSubSystem(getFirstSelectedRemoteObject());
    	else
    	  return null;
    }
    /**
     * Returns the subsystem factory which owns the subsystem from which the selected remote objects were resolved
     */
    public ISubSystemConfiguration getSubSystemConfiguration()
    {
    	ISubSystem ss = getSubSystem();
    	if (ss != null)
    	  return ss.getSubSystemConfiguration();
    	else 
    	  return null;
    }    
    
    /**
     * Return the SystemConnection from which the selected remote objects were resolved
     */
    public IHost getSystemConnection()
    {
    	IHost conn = null;
    	ISystemRemoteElementAdapter ra = getFirstSelectedRemoteObjectAdapter();
    	if (ra != null)
    	{
    	   ISubSystem ss = ra.getSubSystem(getFirstSelectedRemoteObject());
    	   if (ss != null)
    	     conn = ss.getHost();
    	}
    	return conn;
    }



    
      
	/**
	 * Debug method to print out details of given selected object...
	 */
	public void printTest() 
	{
		System.out.println("Testing. Number of selected objects = "+getSelectionCount());
        Object obj = getFirstSelectedRemoteObject();
        if (obj == null)
          System.out.println("selected obj is null");
        else
        {
          ISystemRemoteElementAdapter adapter = getRemoteAdapter(obj);
          System.out.println();
          System.out.println("REMOTE INFORMATION FOR FIRST SELECTION");
          System.out.println("--------------------------------------");
          System.out.println("Remote object name................: " + getRemoteObjectName(obj,adapter));
          System.out.println("Remote object subsystem factory id: " + getRemoteObjectSubSystemConfigurationId(obj,adapter));          
          System.out.println("Remote object type category.......: " + getRemoteObjectTypeCategory(obj,adapter));
          System.out.println("Remote object type ...............: " + getRemoteObjectType(obj,adapter));
          System.out.println("Remote object subtype ............: " + getRemoteObjectSubType(obj,adapter));
          System.out.println("Remote object subsubtype .........: " + getRemoteObjectSubSubType(obj,adapter));
          System.out.println();
        }
        System.out.println();
	}
    
}