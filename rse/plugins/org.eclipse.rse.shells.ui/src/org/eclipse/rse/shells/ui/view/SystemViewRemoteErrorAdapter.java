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

package org.eclipse.rse.shells.ui.view;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteError;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.view.SystemViewResources;
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
					RSEUIPlugin plugin = RSEUIPlugin.getDefault();

					// path
					_uniquePropertyDescriptorArray[++i] = createSimplePropertyDescriptor(P_ERROR_FILENAME, SystemViewResources.RESID_PROPERTY_ERROR_FILENAME_LABEL, SystemViewResources.RESID_PROPERTY_ERROR_FILENAME_TOOLTIP);

					// line
					_uniquePropertyDescriptorArray[++i] = createSimplePropertyDescriptor(P_ERROR_LINENO, SystemViewResources.RESID_PROPERTY_ERROR_LINENO_LABEL, SystemViewResources.RESID_PROPERTY_ERROR_LINENO_TOOLTIP);	
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
			_propertyDescriptors[++idx] = createSimplePropertyDescriptor(P_FILE_PATH, SystemViewResources.RESID_PROPERTY_FILE_PATH_LABEL, SystemViewResources.RESID_PROPERTY_FILE_PATH_TOOLTIP);				
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

			if (name.equals(P_FILE_PATH))
			{
				return output.getAbsolutePath();
			}
			else if (name.equals(P_ERROR_FILENAME))
			{
				return output.getAbsolutePath();
			}
			else if (name.equals(P_ERROR_LINENO))
			{
				return new Integer(output.getLine());
			}
		}
		return null;
	}

}