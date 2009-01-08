/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.update;

import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;

/**
 * A view model provider which supports caching of data returned by view model
 * nodes.  The methods in this interface allow clients to configure how the  
 * cache should be updated in response to different events.
 */
public interface ICachingVMProvider extends IVMProvider {
    
    /**
     * Returns the update policies that the given provider supports.
     */
    public IVMUpdatePolicy[] getAvailableUpdatePolicies();

    /**
     * Returns the active update policy.
     */
    public IVMUpdatePolicy getActiveUpdatePolicy();

    /**
     * Sets the active update policy.  This has to be one of the update
     * policies supported by the provider.
     */
    public void setActiveUpdatePolicy(IVMUpdatePolicy mode);
    
    /**
     * Forces the view to flush its cache and re-fetch data from the view
     * model nodes.
     */
    public void refresh();
    
}
