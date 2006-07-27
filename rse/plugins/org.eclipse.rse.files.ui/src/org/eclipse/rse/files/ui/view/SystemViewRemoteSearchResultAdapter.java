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

package org.eclipse.rse.files.ui.view;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.files.ui.actions.SystemRemoteFileSearchOpenWithMenu;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.services.search.IHostSearchResult;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.shells.core.model.ISystemOutputRemoteTypes;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.actions.SystemCopyToClipboardAction;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemDragDropAdapter;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemViewResources;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;


/**
 * Adapter class to enable the output of a search to be "live" in that it has actions and properties.
 */
public class SystemViewRemoteSearchResultAdapter extends AbstractSystemViewAdapter implements ISystemRemoteElementAdapter, ISystemOutputRemoteTypes
{

	protected IPropertyDescriptor[] _propertyDescriptors;

	
	private SystemCopyToClipboardAction _copyOutputAction = null;

	public SystemViewRemoteSearchResultAdapter() {
	}
	
	/**
	 * We should not add common actions such as compile and user actions for this adapter.
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#addCommonRemoteActions(org.eclipse.rse.ui.SystemMenuManager, org.eclipse.jface.viewers.IStructuredSelection, org.eclipse.swt.widgets.Shell, java.lang.String)
	 */
	public void addCommonRemoteActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup) {
		
		if (selection != null && !selection.isEmpty()) {
			
			Iterator iter = selection.iterator();
			
			boolean found = false;
			
			// go through selections and see if there is one IHostSearchResult
			// if there is, we do not add any common remote actions
			while (iter.hasNext()) { 
				Object obj = iter.next();
				
				if (obj instanceof IHostSearchResult) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				super.addCommonRemoteActions(menu, selection, shell, menuGroup);
			}
 		}
		else {
			super.addCommonRemoteActions(menu, selection, shell, menuGroup);
		}
	}

	/**
	 * Contributed context menu actions for a remote search result
	 */
	public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup)
	{
		Object firstSelection = selection.getFirstElement();

		if (firstSelection != null)
		{
			if (_copyOutputAction == null)
			{
				_copyOutputAction = new SystemCopyToClipboardAction(shell, RSEUIPlugin.getTheSystemRegistry().getSystemClipboard());
			}
			menu.add(menuGroup, _copyOutputAction);

			if (selection.size() == 1)
			{
				if (firstSelection instanceof IHostSearchResult)
				{
				//	IHostSearchResult result = (IHostSearchResult) firstSelection;					
					//SystemSearchCreateEditLineActions createActions = new SystemSearchCreateEditLineActions();
					//createActions.create(menu, selection, shell, menuGroup);					
					MenuManager submenu = new MenuManager(FileResources.ResourceNavigator_openWith, ISystemContextMenuConstants.GROUP_OPENWITH);
					    
					SystemRemoteFileSearchOpenWithMenu openWithMenu = new SystemRemoteFileSearchOpenWithMenu();
					openWithMenu.updateSelection(selection);
					submenu.add(openWithMenu);
					menu.getMenuManager().appendToGroup(ISystemContextMenuConstants.GROUP_OPENWITH, submenu);
				}
			}
		}
		else
		{
			return;
		}
	}

	/**
	 * Returns the parent of the search result (i.e. IHostSearchResults)
	 */
	public Object getParent(Object element) 
	{
		if (element instanceof IHostSearchResult)
		{
			IHostSearchResult output = (IHostSearchResult) element;
			return output.getParent();
		}
		return null;
	}

	/**
	 * Returns the display text for this search result
	 */
	public String getText(Object element)
	{
		if (element instanceof IHostSearchResult)
		{
			IHostSearchResult output = (IHostSearchResult) element;
			return output.getText();
		}

		return null;
	}

	/**
	 * Returns nothing
	 */
	public String getType(Object element)
	{
		return null;
	}

	/**
	 * Returns nothing
	 */
	public Object[] getChildren(Object element)
	{
		return null;
	}

	/**
	 * Returns false.
	 */
	public boolean hasChildren(Object element)
	{
		return false;
	}

	/**
	 * Returns the associated remote file for a search result
	 * @param output the search result
	 * @return the associated remote file
	 */
	public static IRemoteFile outputToFile(IHostSearchResult output) 
	{
		return (IRemoteFile)output.getParent();
	}


	/**
	 * Opens the appropriate editor for a remote search result object
	 */
	public boolean handleDoubleClick(Object element)
	{
		boolean result = false;
		if (element instanceof IHostSearchResult)
		{
			
			IHostSearchResult searchResult = (IHostSearchResult) element;
			IRemoteFile file = outputToFile(searchResult);
			if (file != null && file.isFile())
			{
				ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)((IAdaptable)file).getAdapter(ISystemViewElementAdapter.class);
				result = adapter.handleDoubleClick(file);
				int line = searchResult.getLine();

				if (result)
				{
					if (line > 0) 
					{
						SystemRemoteFileSearchOpenWithMenu.handleGotoLine(file, searchResult);
					}
					return true;
				}
			}
		}

		return result;
	}

	/**
	 * Returns the associated subsystem for this search result
	 */
	public ISubSystem getSubSystem(Object element)
	{
		if (element instanceof IHostSearchResult)
		{
			IHostSearchResult output = (IHostSearchResult) element;
			Object parent = output.getParent();
			
			if (parent instanceof IRemoteFile) {
				return ((IRemoteFile)parent).getParentRemoteFileSubSystem();
			}
		}

		return null;
	}

	/**
	  * Return the fully qualified name of this remote object. 
	  */
	public String getAbsoluteName(Object element)
	{
		if (element instanceof IHostSearchResult)
		{
			IHostSearchResult searchResult = (IHostSearchResult)element;
			
			StringBuffer buf = new StringBuffer();
			
			String str = getAbsoluteParentName(element);
			
			if (str == null) {
			    return null;
			}
			
			// create the absolute name with this format
			// remoteFilePath:SEARCH<searchString:index>
			buf.append(str);
			buf.append(IHostSearchResult.SEARCH_RESULT_DELIMITER);
			buf.append(IHostSearchResult.SEARCH_RESULT_OPEN_DELIMITER);
			buf.append(searchResult.getMatchingSearchString().toString());
			buf.append(IHostSearchResult.SEARCH_RESULT_INDEX_DELIMITER);
			buf.append(searchResult.getIndex());
			buf.append(IHostSearchResult.SEARCH_RESULT_CLOSE_DELIMITER);
			
			return buf.toString();
		}
		
		return null;
	}

	/**
	  * Return fully qualified name that uniquely identifies this remote object's remote parent within its subsystem
	  */
	public String getAbsoluteParentName(Object element)
	{
	    Object parent = getParent(element);
	    
	    if ((parent != null) && (parent instanceof IRemoteFile)) {
	        ISystemRemoteElementAdapter parentAdapter = SystemAdapterHelpers.getRemoteAdapter(parent);
	        
	        if (parentAdapter != null) {
	            return parentAdapter.getAbsoluteName(parent);
	        }
	    }
	    
		return null;
	}

	/**
	  * Return the subsystem factory id that owns this remote object
	  * The value must not be translated, so that property pages registered via xml can subset by it.
	  */
	public String getSubSystemFactoryId(Object element)
	{
		return null;
	}

	/**
	  * Return a value for the type category property for this object
	  * The value must not be translated, so that property pages registered via xml can subset by it.
	  */
	public String getRemoteTypeCategory(Object element)
	{
		return null;
	}

	/**
	  * Return a value for the type property for this object
	  * The value must not be translated, so that property pages registered via xml can subset by it.
	  */
	public String getRemoteType(Object element)
	{
		return null;
	}

	/**
	  * Return a value for the subtype property for this object.
	  * Not all object types support a subtype, so returning null is ok.
	  * The value must not be translated, so that property pages registered via xml can subset by it.
	  */
	public String getRemoteSubType(Object element)
	{
		return null;
	}

	/**
	  * Return a value for the sub-subtype property for this object.
	  * Not all object types support a sub-subtype, so returning null is ok.
	  * The value must not be translated, so that property pages registered via xml can subset by it.
	  */
	public String getRemoteSubSubType(Object element)
	{
		return null;
	}
	/**
	 * Return the source type of the selected object. Typically, this only makes sense for compilable
	 *  source members. For non-compilable remote objects, this typically just returns null.
	 */
	public String getRemoteSourceType(Object element)
	{
		return null;
	}

	/**
	  * Some view has updated the name or properties of this remote object. As a result, the 
	  *  remote object's contents need to be refreshed. You are given the old remote object that has
	  *  old data, and you are given the new remote object that has the new data. For example, on a
	  *  rename the old object still has the old name attribute while the new object has the new 
	  *  new attribute.
	  * <p>
	  * This is called by viewers like SystemView in response to rename and property change events.
	  * <p>
	  * @param oldElement the element that was found in the tree
	  * @param newElement the updated element that was passed in the REFRESH_REMOTE event
	  * @return true if you want the viewer that called this to refresh the children of this object,
	  *   such as is needed on a rename of a folder, say.
	  */
	public boolean refreshRemoteObject(Object oldElement, Object newElement)
	{
		return false;
	}

	/**
	 * Given a remote object, returns it remote parent object. Eg, given a file, return the folder
	 *  it is contained in.
	 */
	public Object getRemoteParent(Shell shell, Object element) throws Exception
	{
		if (element instanceof IHostSearchResult)
		{
			return ((IHostSearchResult) element).getParent();
		}
		return null;
	}

	/**
	  * Given a remote object, return the unqualified names of the objects contained in that parent. This is
	  *  used for testing for uniqueness on a rename operation, for example. Sometimes, it is not 
	  *  enough to just enumerate all the objects in the parent for this purpose, because duplicate
	  *  names are allowed if the types are different, such as on iSeries. In this case return only 
	  *  the names which should be used to do name-uniqueness validation on a rename operation.
	  */
	public String[] getRemoteParentNamesInUse(Shell shell, Object element) throws Exception
	{
		return null;
	}
	
	public IPropertyDescriptor[] getUniquePropertyDescriptors()
	{
		return new IPropertyDescriptor[0];
	}

	/**
	 * Returns the unique property descriptors for a search result
	 */
	protected org.eclipse.ui.views.properties.IPropertyDescriptor[] internalGetPropertyDescriptors()
	{
		if (_propertyDescriptors == null)
		{
			_propertyDescriptors = new PropertyDescriptor[2];
			int idx = -1;

			// path
			_propertyDescriptors[++idx] = createSimplePropertyDescriptor(P_FILE_PATH, SystemViewResources.RESID_PROPERTY_FILE_PATH_LABEL, SystemViewResources.RESID_PROPERTY_FILE_PATH_TOOLTIP);
			
			// char start
			_propertyDescriptors[++idx] = createSimplePropertyDescriptor(P_SEARCH_LINE, SystemViewResources.RESID_PROPERTY_SEARCH_LINE_LABEL, SystemViewResources.RESID_PROPERTY_SEARCH_LINE_TOOLTIP);
			//_propertyDescriptors[++idx] = createSimplePropertyDescriptor(P_SEARCH_CHAR_END, SystemViewResources.RESID_PROPERTY_SEARCH_CHAR_END_ROOT);
		}
		return _propertyDescriptors;
	}

	/**
	* Returns the current collection of property descriptors.
	* By default returns descriptors for name and type only.
	* Override if desired.
	* @return an array containing all descriptors.  
	*/
	protected Object internalGetPropertyValue(Object key)
	{
		String name = (String) key;
		if (propertySourceInput instanceof IHostSearchResult)
		{
			IHostSearchResult output = (IHostSearchResult) propertySourceInput;

			if (name.equals(P_FILE_PATH))
			{
				return output.getAbsolutePath();
			}
			else if (name.equals(P_SEARCH_LINE))
			{
			    return new Integer(output.getLine());
			}
			/*
			else if (name.equals(P_SEARCH_CHAR_END))
			{
			    return new Integer(output.getCharEnd());
			}
			*/			    
		}

		return null;
	}


	/**
	 * Returns the associated image descriptor for a search result
	 */
	public ImageDescriptor getImageDescriptor(Object element)
	{
		if (element instanceof IHostSearchResult)
		{
			ImageDescriptor imageDescriptor = null;
			imageDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_SEARCH_RESULT_ID);

			return imageDescriptor;
		}
		else
		{ // return some default	 
			ImageDescriptor imageDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_BLANK_ID);
			return imageDescriptor;
		}
	}

	/**
	 * Return true if we should show the delete action in the popup for the given element.
	 * If true, then canDelete will be called to decide whether to enable delete or not.
	 */
	public boolean showDelete(Object element)
	{
		return false;
	}
	/**
	 * Return true if this object is deletable by the user. If so, when selected,
	 *  the Edit->Delete menu item will be enabled.
	 * By default, returns false. Override if your object is deletable.
	 */
	public boolean canDelete(Object element)
	{
		return false;
	}

	// ------------------------------------------
	// METHODS TO SUPPORT COMMON REFRESH ACTION...
	// ------------------------------------------
	/**
	 * Return true if we should show the refresh action in the popup for the given element.
	 */
	public boolean showRefresh(Object element)
	{
		return false;
	}

	// ------------------------------------------------------------
	// METHODS TO SUPPORT COMMON OPEN-IN-NEW-PERSPECTIVE ACTIONS...
	// ------------------------------------------------------------
	/**
	 * Return true if we should show the refresh action in the popup for the given element.
	 */
	public boolean showOpenViewActions(Object element)
	{
		return false;
	}

	// ------------------------------------------
	// METHODS TO SUPPORT COMMON RENAME ACTION...
	// ------------------------------------------

	/**
	 * Return true if we should show the rename action in the popup for the given element.
	 * If true, then canRename will be called to decide whether to enable rename or not.
	 */
	public boolean showRename(Object element)
	{
		return false;
	}
	/**
	 * Return true if this object is renamable by the user. If so, when selected,
	 *  the Rename popup menu item will be enabled.
	 * By default, returns false. Override if your object is renamable.
	 */
	public boolean canRename(Object element)
	{
		return false;
	}

	/**
	 * Perform the rename action. By default does nothing. Override if your object is renamable.
	 * Return true if this was successful. Return false if it failed and you issued a msg. 
	 * Throw an exception if it failed and you want to use the generic msg.
	 */
	public boolean doRename(Shell shell, Object element, String name) throws Exception
	{
		return false;
	}

	// Drag and drop

	/**
	 * Indicates whether the specified object can have another object copied to it
	 * @param element the object to copy to
	 * @return whether this object can be copied to or not
	 */
	public boolean canDrop(Object element)
	{

		return false;
	}

	/**
	 * Indicates whether the specified object can be copied 
	 * @param element the object to copy
	 */
	public boolean canDrag(Object element)
	{
		if (element instanceof IHostSearchResult)
		{
			return true;
		}

		return false;
	}

	/**
	 * Copy the specified remote output object.  This method returns a string representing
	 * the text of the remote output;
	 * 
	 * @param element the output to copy
	 * @param sameSystemType not applicable for remote output
	 * @param monitor the progress monitor
	 */
	public Object doDrag(Object element, boolean sameSystemType, IProgressMonitor monitor)
	{
		if (element instanceof List)
		{
			List resultSet = new ArrayList();
			List set = (List)element;
			for (int i = 0; i < set.size(); i++)
			{
				resultSet.add(getText(set.get(i)));
			}
			return resultSet;
		}
		else
		{
			return getText(element);
		}
	}
	
	/**
	  * Return true if it is valid for the src object to be dropped in the target
	  * @param src the object to drop
	  * @param target the object which src is dropped in
	  * @param sameSystem whether this is the same system
	  * @return whether this is a valid operation
	  */
	public boolean validateDrop(Object src, Object target, boolean sameSystem)
	{
		return false;
	}

	/**
	 * Perform a copy via drag and drop.
	 * @param src the object to be copied.  If the target and source are not on the same system, then this is a
	 * temporary object produced by the doDrag.
	 * @param target the object to be copied to.
	 * @param sameSystem an indication whether the target and source reside on the same type of system
	 * @param indicates the type of source
	 * @param monitor the progress monitor
	 * @return an indication whether the operation was successful or not.
	 */
	public Object doDrop(Object src, Object target, boolean sameSystemType, boolean sameSystem, int srcType, IProgressMonitor monitor)
	{
		IRemoteFile folder = outputToFile((IHostSearchResult) target);
		if (folder != null)
		{
			ISystemDragDropAdapter adapter = (ISystemDragDropAdapter) ((IAdaptable) folder).getAdapter(ISystemDragDropAdapter.class);
			return adapter.doDrop(src, folder, sameSystemType, sameSystem, srcType, monitor);
		}
		return null;
	}
	
	/**
	 * Indicates whether the search result can be opened in an editor
	 */
	public boolean canEdit(Object element)
	{
		if (element instanceof IHostSearchResult)
		{
			IHostSearchResult output = (IHostSearchResult) element;
			IRemoteFile file = outputToFile(output);
			if (file != null && file.isFile())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the associated remote editable object for this search result
	 */
	public ISystemEditableRemoteObject getEditableRemoteObject(Object element)
	{
		if (element instanceof IHostSearchResult)
		{
			IHostSearchResult output = (IHostSearchResult) element;
			IRemoteFile file = outputToFile(output);
			if (file != null  && file.isFile())
			{
				return new SystemEditableRemoteFile(file);
			}
		}
		return null;
	}
	
	/**
	 * Return a filter string that corresponds to this object.
	 * @param object the object to obtain a filter string for
	 * @return the corresponding filter string if applicable
	 */
	public String getFilterStringFor(Object object)
	{
		return null;
	}
	
	/**
	 * Returns <code>false</code>.
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#supportsUserDefinedActions(java.lang.Object)
	 */
	public boolean supportsUserDefinedActions(Object object) {
		return false;
	}
	

	
	/**
	 * Returns the current value for the named property.
	 * 
	 * @param property the name or key of the property as named by its property descriptor
	 * @param formatted indication of whether to return the value in formatted or raw form
	 * @return the current value of the given property
	 */
	public Object getPropertyValue(Object property, boolean formatted)
	{
		String name = (String) property;
		if (propertySourceInput instanceof IRemoteCommandShell)
		{
			IRemoteCommandShell cmdShell = (IRemoteCommandShell) propertySourceInput;
			if (name.equals(ISystemPropertyConstants.P_SHELL_STATUS))
			{
			    if (cmdShell.isActive())
			    {
			        return SystemViewResources.RESID_PROPERTY_SHELL_STATUS_ACTIVE_VALUE;
			    }
			    else
			    {
			        return SystemViewResources.RESID_PROPERTY_SHELL_STATUS_INACTIVE_VALUE;
			    }
			}
			else if (name.equals(ISystemPropertyConstants.P_SHELL_CONTEXT))
			{
			    Object context = cmdShell.getContext();
			    if (context instanceof IRemoteFile)
			    {			        
			        IRemoteFile cwd = (IRemoteFile)context;
			        if (cwd != null)
			        {	
			        	return cwd.getAbsolutePath();			        
			        }	
			    }
			    else
			     {
			        return context;
			     }
			}
		}
		return "";
	}
}