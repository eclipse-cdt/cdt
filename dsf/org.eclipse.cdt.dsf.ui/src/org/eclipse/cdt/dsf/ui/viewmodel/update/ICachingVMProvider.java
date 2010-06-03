/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
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
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider;

/**
 * A view model provider which supports caching of data returned by view model
 * nodes.  The methods in this interface allow clients to configure how the  
 * cache should be updated in response to different events.
 * 
 * @since 1.0
 */
public interface ICachingVMProvider extends IVMProvider, IElementPropertiesProvider {

    /**
     * A prefix used to create a property to indicate whether a given property
     * has changed since the last cache update with the {@link IVMUpdatePolicy#ARCHIVE} 
     * flag.  The caching VM provider appends these properties to an element's set of 
     * properties as they are retrieved through the cache.
     * 
     * @see org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider
     * @see IVMUpdatePolicy#ARCHIVE
     * 
     * @since 2.0
     */
    public static final String PROP_IS_CHANGED_PREFIX = "is_changed."; //$NON-NLS-1$
    
    /**
     * A property used to indicate whether a given cache entry is currently dirty.  
     * The caching VM provider appends this property to an 
     * element's set of properties as they are retrieved through the cache.
     * 
     * @see org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider
     * @see IVMUpdatePolicy#DIRTY
     * 
     * @since 2.0
     */
    public static final String PROP_CACHE_ENTRY_DIRTY = "cache_entry_dirty"; //$NON-NLS-1$
    
    /**
     * Property name for the current update policy in view. 
     * 
     * @see org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider
     * 
     * @since 2.1
     */
    public static final String PROP_UPDATE_POLICY_ID = "update_policy_id"; //$NON-NLS-1$
    
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
