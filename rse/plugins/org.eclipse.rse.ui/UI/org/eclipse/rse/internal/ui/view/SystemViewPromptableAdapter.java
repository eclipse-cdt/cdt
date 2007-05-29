/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.internal.ui.actions.SystemRunAction;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.model.ISystemPromptableObject;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.swt.widgets.Shell;



/**
 * Adapter for displaying special-case prompt objects in the system views.
 * These are objects that, when expanded, launch a wizard to create something.
 */
public class SystemViewPromptableAdapter 
       extends AbstractSystemViewAdapter
{		
	protected SystemRunAction runAction;

    /**
     * Return the actions to show in the popup menu for the current selection 
     */
	public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup)
    {
    	ISystemPromptableObject object = (ISystemPromptableObject)selection.getFirstElement();
    	if ((object != null) && !object.hasChildren())
    	{
    	  if (runAction == null)
    	    runAction = getRunAction(shell);
          menu.add(menuGroup, runAction);   	
    	}
    }
	
	/**
	 * Only called if the selected object has no children.
	 * @param shell the shell.
	 * @return the run action.
	 */
	protected SystemRunAction getRunAction(Shell shell) {
		return (new SystemRunAction(shell));
	}

	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * @param element The element for which an image is desired
	 */
	public ImageDescriptor getImageDescriptor(Object element)
	{
		return ((ISystemPromptableObject)element).getImageDescriptor();
	}
	
	/**
	 * Return the label for this object. 
	 */
	public String getText(Object element)
	{
		return ((ISystemPromptableObject)element).getText();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier#getAbsoluteName(java.lang.Object)
	 */
	public String getAbsoluteName(Object element)
	{
		return getText(element);
	}		
	/**
	 * Return the type label for this object
	 */
	public String getType(Object element)
	{
		return ((ISystemPromptableObject)element).getType();
	}	
	
	/**
	 * Return the parent of this object. 
	 */
	public Object getParent(Object element)
	{
		return ((ISystemPromptableObject)element).getParent();
	}	
	
	/**
	 * Return the children of this object. Not applicable for us.
	 */
	public Object[] getChildren(IAdaptable element, IProgressMonitor monitor)
	{
		// Note: Do _not_ call promptable.run(getShell()) here. It leads only to
		// senseless invocations of the new connection wizard dialog on refreshs!
		// It cannot be a desirable effect of refreshing the system view to create
		// new connections. We leave the invocation of the dialog to the double
		// click handler and the context menu.
		ISystemPromptableObject promptable = (ISystemPromptableObject)element;
		 return promptable.getChildren();
	}
	
	/**
	 * Return true if this object has children. 
	 * We return true, as either we'll expand and prompt, or expand and show child prompts
	 */
	public boolean hasChildren(IAdaptable element)
	{
		ISystemPromptableObject promptable = (ISystemPromptableObject)element;
		return promptable.hasChildren();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#handleDoubleClick(java.lang.Object)
	 */
	public boolean handleDoubleClick(Object element) {
		ISystemPromptableObject promptable = (ISystemPromptableObject)element;
		promptable.run(getShell());
		return true;
	}
	
	/**
	 * Return our unique property descriptors
	 */
	protected org.eclipse.ui.views.properties.IPropertyDescriptor[] internalGetPropertyDescriptors()
	{
		return null;
	}	
	/**
	 * Return our unique property values
	 */
	public Object internalGetPropertyValue(Object key)
	{
		return null;
	}	
	/**
	 * Don't show delete
	 */
	public boolean showDelete(Object element)
	{
		return false;
	}

	/**
	 * Don't show rename
	 */
	public boolean showRename(Object element)
	{
		return false;
	}

	/**
	 * Do show refresh, expand and collapse, but only if there are children
	 */
	public boolean showRefresh(Object element)
	{
		ISystemPromptableObject promptable = (ISystemPromptableObject)element;
		return promptable.hasChildren();
	}    

	/**
	 * Don't show "Open in new perspective"
	 */
	public boolean showOpenViewActions(Object element)
	{
		return false;
	}    

	// ------------------------------------------------------------
	// METHODS FOR SAVING AND RESTORING EXPANSION STATE OF VIEWER...
	// ------------------------------------------------------------

	/**
	 * Return what to save to disk to identify this element in the persisted list of expanded elements.
	 * This just defaults to getName, but if that is not sufficient override it here.
	 */
	public String getMementoHandle(Object element)
	{
		return getName(element);
	}
	/**
	 * Return a short string to uniquely identify the type of resource. Eg "conn" for connection.
	 * This just defaults to getType, but if that is not sufficient override it here, since that is
	 * a translated string.
	 */
	public String getMementoHandleKey(Object element)
	{
		return "Prompt"; //$NON-NLS-1$
	}

    /**
     * Somtimes we don't want to remember an element's expansion state, such as for temporarily inserted 
     *  messages. In these cases return false from this method. The default is true.
     * <p>
     * WE RETURN FALSE.
     */
    public boolean saveExpansionState(Object element)
    {
    	return false;
    }
    
	/**
	 * This is a local RSE artifact so returning false
	 * 
	 * @param element the object to check
	 * @return false since this is not remote
	 */
	public boolean isRemote(Object element) {
		return false;
	}
}