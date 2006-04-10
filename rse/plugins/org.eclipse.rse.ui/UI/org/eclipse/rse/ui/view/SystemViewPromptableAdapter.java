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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.model.ISystemPromptableObject;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.actions.SystemRunAction;
import org.eclipse.swt.widgets.Shell;



/**
 * Adapter for displaying special-case prompt objects in the system views.
 * These are objects that, when expanded, launch a wizard to create something.
 */
public class SystemViewPromptableAdapter 
       extends AbstractSystemViewAdapter implements ISystemViewElementAdapter
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
	/**
	 * Return the absolute name, versus just display name, of this object. 
	 * Just uses getText(element);
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
	public Object[] getChildren(Object element)
	{
		ISystemPromptableObject promptable = (ISystemPromptableObject)element;
		if (!promptable.hasChildren())
		  return promptable.run(getShell());
		else
		  return promptable.getChildren();
	}
	
	/**
	 * Return true if this object has children. 
	 * We return true, as either we'll expand and prompt, or expand and show child prompts
	 */
	public boolean hasChildren(Object element)
	{
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
		return "Prompt";
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
}