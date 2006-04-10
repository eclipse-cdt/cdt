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
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.ISystemMessageObject;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;



/**
 * Adapter for displaying temporary message objects when an expand fails or is cancelled.
 * <p>
 * The objects must all implement com.ibm.etools.systems.model.ISystemExpandablePromptableObject
 *  to use this adapter.
 */
public class SystemViewMessageAdapter 
       extends AbstractSystemViewAdapter implements ISystemViewElementAdapter
{		
	
	/**
	 * Add actions to context menu.
	 * We don't add any for message objects, at this point.
	 */
	public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup)
    {
    }

	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * @param element The element for which an image is desired
	 */
	public ImageDescriptor getImageDescriptor(Object element)
	{
		ISystemMessageObject msg = (ISystemMessageObject)element;
		int type = msg.getType();
		if (type==ISystemMessageObject.MSGTYPE_ERROR)
          	return SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_ERROR_ID);
        else if (type==ISystemMessageObject.MSGTYPE_CANCEL)
          	return SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_INFO_TREE_ID);
          	// DY:  icon vetoed by UCD
          	// return SystemPlugin.getDefault().getImageDescriptor(ISystemConstants.ICON_SYSTEM_CANCEL_ID);
        else if (type==ISystemMessageObject.MSGTYPE_EMPTY)
          	return SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_EMPTY_ID);
        else if (type==ISystemMessageObject.MSGTYPE_OBJECTCREATED)
          	return SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_OK_ID);
        else 
          	return SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_INFO_TREE_ID);
	}
	
	/**
	 * Return the label for this object. Uses getMessage() on the given ISystemMessageObject object.
	 */
	public String getText(Object element)
	{
		ISystemMessageObject msg = (ISystemMessageObject)element;
		return msg.getMessage();
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
		return SystemViewResources.RESID_PROPERTY_MESSAGE_TYPE_VALUE;		
	}	
	
	/**
	 * Return the parent of this object. 
	 */
	public Object getParent(Object element)
	{
		ISystemMessageObject msg = (ISystemMessageObject)element;
		return msg.getParent();
	}	
	
	/**
	 * Return the children of this object. Not applicable for us.
	 */
	public Object[] getChildren(Object element)
	{
		return null;		
	}
	
	/**
	 * Return true if this object has children. Always false for us.
	 */
	public boolean hasChildren(Object element)
	{
		return false;
	}
	/**
	 * Return our unique property descriptors
	 */
	protected IPropertyDescriptor[] internalGetPropertyDescriptors()
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
	 * Don't show refresh
	 */
	public boolean showRefresh(Object element)
	{
		return false;
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
		return "Msg";
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