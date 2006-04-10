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
import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * This class is a provider of root nodes to the remote systems tree viewer part.
 * It is used when the contents are used to show the resolution of a single filter string.
 */
public class SystemResolveFilterStringAPIProviderImpl extends SystemTestFilterStringAPIProviderImpl
{



	/**
	 * Constructor 
	 * @param subsystem The subsystem that will resolve the filter string
	 * @param filterString The filter string to test
	 */
	public SystemResolveFilterStringAPIProviderImpl(ISubSystem subsystem, String filterString)
	{
		super(subsystem, filterString);
	} // end constructor
	

    /**
     * Return true to show the button bar above the viewer.
     * The tool bar contains "Get List" and "Refresh" buttons and is typicall
     * shown in dialogs that list only remote system objects.
     */
    public boolean showButtonBar()
    {
    	return false;
    } // end showButtonBar()
     
} // end class SystemResolveFilterStringAPIProviderImpl