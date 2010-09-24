/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.update;

import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.jface.viewers.TreePath;


/**
 * Extension allowing access to the caching VM provider cache entries.
 * 
 * @since 2.2
 */
public interface ICachingVMProviderExtension2 extends ICachingVMProvider {
    
    /**
     * Returns the cache entry for the given parameters.  May return <code>null</code>
     * if the cache entry does not exist in the cache.
     */
    public ICacheEntry getCacheEntry(IVMNode node, Object viewerInput, TreePath path);
    
}
