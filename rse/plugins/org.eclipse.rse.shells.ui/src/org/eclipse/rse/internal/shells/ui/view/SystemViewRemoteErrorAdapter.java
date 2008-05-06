/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [216252] [nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * Radoslav Gerganov (ProSyst)   - [229725] Right click popup menu inside Local Shell view has two copy entries
 *******************************************************************************/

package org.eclipse.rse.internal.shells.ui.view;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.internal.shells.ui.ShellResources;
import org.eclipse.rse.internal.ui.view.SystemViewResources;
import org.eclipse.rse.shells.ui.view.SystemViewRemoteOutputAdapter;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteError;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;


/**
 * Comment goes here
 */
public class SystemViewRemoteErrorAdapter extends SystemViewRemoteOutputAdapter
{

	protected IPropertyDescriptor[] _uniquePropertyDescriptorArray;
	/**
	 * Used to add context menu actions for the given remote output
	 */
	public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup)
	{
		Iterator iter = selection.iterator();
		// check if the selection contains only IRemoteError objects
		while (iter.hasNext()) {
			Object current = iter.next();
			if (!(current instanceof IRemoteError)) {
				return;
			}
		}
		// add the same context menu actions as for IRemoteOutput
		super.addActions(menu, selection, shell, menuGroup);
	}
	
	public IPropertyDescriptor[] getUniquePropertyDescriptors()
	{
			if (_uniquePropertyDescriptorArray == null)
				{
					int nbrOfProperties = 2;

					_uniquePropertyDescriptorArray = new PropertyDescriptor[nbrOfProperties];
					//PropertyDescriptor[] defaultProperties = (PropertyDescriptor[]) getDefaultDescriptors();

					int i = -1;

					// add our unique property descriptors...
					//RSEUIPlugin plugin = RSEUIPlugin.getDefault();

					// path
					_uniquePropertyDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_ERROR_FILENAME, ShellResources.RESID_PROPERTY_ERROR_FILENAME_LABEL, ShellResources.RESID_PROPERTY_ERROR_FILENAME_TOOLTIP);

					// line
					_uniquePropertyDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_ERROR_LINENO, ShellResources.RESID_PROPERTY_ERROR_LINENO_LABEL, ShellResources.RESID_PROPERTY_ERROR_LINENO_TOOLTIP);	
				}

				return _uniquePropertyDescriptorArray;

	}

	/**
	 * Return our unique property descriptors
	 */
	protected IPropertyDescriptor[] internalGetPropertyDescriptors()
	{
		if (_propertyDescriptors == null)
		{
			// unique ones
			IPropertyDescriptor[] unique = getUniquePropertyDescriptors();
			// our additional							 
			_propertyDescriptors = new PropertyDescriptor[1+ unique.length];
			int idx = -1;

			// path
			_propertyDescriptors[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_PATH, SystemViewResources.RESID_PROPERTY_FILE_PATH_LABEL, SystemViewResources.RESID_PROPERTY_FILE_PATH_TOOLTIP);				
			// append...
			for (int i = 0; i < unique.length; i++)
			{
				_propertyDescriptors[++idx] = unique[i];
			}
		}
		return _propertyDescriptors;
	}

	/**
	* Returns the current collection of property descriptors.
	* @return an array containing all descriptors.  
	*/
	protected Object internalGetPropertyValue(Object key)
	{
		String name = (String) key;
		if (propertySourceInput instanceof IRemoteError)
		{			
			IRemoteError output = (IRemoteError) propertySourceInput;

			if (name.equals(ISystemPropertyConstants.P_FILE_PATH))
			{
				return output.getAbsolutePath();
			}
			else if (name.equals(ISystemPropertyConstants.P_ERROR_FILENAME))
			{
				return output.getAbsolutePath();
			}
			else if (name.equals(ISystemPropertyConstants.P_ERROR_LINENO))
			{
				return new Integer(output.getLine());
			}
		}
		return null;
	}

}
