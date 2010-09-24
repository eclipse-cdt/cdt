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

import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.jface.viewers.TreePath;

/**
 * Cache entry in a caching VM provider.
 * 
 * @see ICachingVMProvider  
 * @see ICachingVMProviderExtension2
 * 
 * @since 2.2
 */
@ConfinedToDsfExecutor("")
public interface ICacheEntry {
   
    /**
     * The VM node that this cache entry is for. This parameter is part of the 
     * key to finding the cache entry.
     */
    public IVMNode getNode();
    
    /**
     * The viewer input object that this cache entry is for.  This parameter 
     * is part of the key to finding the cache entry.
     */
    public Object getViewerInput();
    
    /**
     * The element path that this cache entry is for.  This parameter is part 
     * of the key to finding the cache entry.
     */
    public TreePath getElementPath();
    
    /**
     * Says whether this cache entry is currently marked as dirty.  If a cache
     * entry is dirty, it means that it contains stale data which has not been
     * flushed as indicated by the cache's update policy.
     * @return
     */
    public boolean isDirty();
    
    /**
     * Returns the a flag indicating whether the element pointing to this entry 
     * has children.  Returns <code>null</code> if this value is not known by 
     * cache. 
     */
    public Boolean getHasChildren();
    
    /**
     * Returns the count of children for the element belonging to this entry.
     * Returns <code>null</code> if this value is not known by cache. 
     */
    public Integer getChildCount();
    
    /**
     * Returns a map of children of the element belonging to this entry.
     * The returned map contains integer keys which are indexes of the 
     * element's children.  The values in the map are the child element.
     * Returns <code>null</code> if this value is not known by cache. 
     */
    public Map<Integer, Object> getChildren();
    
    /**
     * Returns map of properties of the element belonging to this entry.
     * Returns <code>null</code> if this value is not known by cache. 
     */
    public Map<String, Object> getProperties();
    
    /**
     * Returns the archived map of properties of the element belong to this 
     * entry.  The archived properties are properties which were saved when
     * the cache was last flushed, as indicated by the cache's active update
     * policy. Returns <code>null</code> if this value is not known by cache. 
     */
    public Map<String, Object> getArchiveProperties();
}
