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
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.swt.widgets.Shell;


/**
 * This is the interface that callers of the SystemSelectRemoteObjectAPIProviderCaller
 *  can optionally implement to be called back for events such as the expansion of a 
 *  promptable, transient filter.
 */
public interface ISystemSelectRemoteObjectAPIProviderCaller 
{
	
    /**
     * Prompt the user to create a new filter as a result of the user expanding a promptable
     * transient filter.
     *
     * @return the filter created by the user or null if they cancelled the prompting
     */
    public ISystemFilter createFilterByPrompting(ISystemFilter filterPrompt, Shell shell)
           throws Exception;
	
}