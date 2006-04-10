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
import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemPropertyPageExtensionManager;
import org.eclipse.rse.ui.GenericMessages;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.PropertyDialog;
import org.eclipse.ui.internal.dialogs.PropertyPageManager;


/**
 * The action shows properties for remote objects
 */
public class SystemRemotePropertiesAction 
       extends SystemBaseAction 
       
{
		
	/**
	 * Constructor
	 */
	public SystemRemotePropertiesAction(Shell shell) 
	{
		super(SystemResources.ACTION_REMOTE_PROPERTIES_LABEL, SystemResources.ACTION_REMOTE_PROPERTIES_TOOLTIP,shell);
        allowOnMultipleSelection(false);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_PROPERTIES);        
	}

	/**
	 * We override from parent to do unique checking...
	 * <p>
	 * It is too expense to check for registered property pages at popup time, so we just return true.
	 * <p>
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = true;
		return enable;
	}
	
	/**
	 * Returns the name of the given element.
	 * @param element the element
	 * @return the name of the element
	 */
	private String getName(Object element) 
	{
		return getAdapter(element).getName(element);
	}
	/**
	 * Returns whether the provided object has pages registered in the property page
	 * manager.
	 */
	public boolean hasPropertyPagesFor(Object object) 
	{
		//PropertyPageContributorManager manager = PropertyPageContributorManager.getManager();
		return getOurPropertyPageManager().hasContributorsFor(getRemoteAdapter(object), object);
	}
	/**
	 * Get the remote property page extension manager
	 */
	private SystemPropertyPageExtensionManager getOurPropertyPageManager()
	{
		return SystemPropertyPageExtensionManager.getManager();
	}
	/**
	 * Returns whether this action is actually applicable to the current selection.
	 * Returns true if there are any registered property pages applicable for the
	 * given input object.
	 * <p>
	 * This method is generally too expensive to use when updating the enabled state
	 * of the action.
	 * </p>
	 *
	 * @return <code>true</code> if there are property pages for the currently
	 *   selected element, and <code>false</code> otherwise
	 */
	public boolean isApplicableForSelection() 
	{
		return hasPropertyPagesFor(getFirstSelection());
	}
	/**
	 * The <code>PropertyDialogAction</code> implementation of this 
	 * <code>IAction</code> method performs the action by opening the Property Page
	 * Dialog for the current selection. If no pages are found, an informative 
	 * message dialog is presented instead.
	 */
	public void run() 
	{		
		PropertyPageManager pageManager = new PropertyPageManager();
		String title = "";//$NON-NLS-1$	

		// get selection
		//Object element = getFirstSelection();
		IAdaptable element = (IAdaptable)getFirstSelection();
		if (element == null)
		  return;
		ISystemRemoteElementAdapter adapter = getRemoteAdapter(element);			
		if (adapter == null)
		  return;

		// load pages for the selection
		// fill the manager with contributions from the matching contributors
		getOurPropertyPageManager().contribute(pageManager, getRemoteAdapter(element), element);
		//PropertyPageContributorManager.getManager().contribute(pageManager, element);		
	
	    Shell shell = getShell();
	    
		// testing if there are pages in the manager
		Iterator pages = pageManager.getElements(PreferenceManager.PRE_ORDER).iterator();
		String name = getName(element);
		if (!pages.hasNext()) {
			MessageDialog.openInformation(
				shell,
				GenericMessages.PropertyDialog_messageTitle, 
				MessageFormat.format(GenericMessages.PropertyDialog_noPropertyMessage, new Object[] {name})); 
			return;
		} 
		else
		{	
			title = MessageFormat.format(GenericMessages.PropertyDialog_propertyMessage, new Object[] {name});
		}

		PropertyDialog propertyDialog = new PropertyDialog(shell, pageManager, getSelection()); 
		propertyDialog.create();
		propertyDialog.getShell().setText(title);
		
		
		
		// TODO - hack to make this work in  3.1
		String id = PlatformUI.PLUGIN_ID + ".property_dialog_context";
		PlatformUI.getWorkbench().getHelpSystem().setHelp(propertyDialog.getShell(), id);
		
		propertyDialog.open();
	}
}