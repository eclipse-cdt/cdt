/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.datamodel;

import org.eclipse.dd.dsf.concurrent.ConfinedToDsfExecutor;

/**
 * Data object containing information regarding a model context.  Unlike the 
 * DM Context object, this object does have to be accessed on the dispatch thread, 
 * unless other-wise noted.  And it does not need to be immutable or free of 
 * references to the service.  In fact, to avoid unnecessary duplication of data
 * it is most practical for the DM Data object to simply retrieve data directly
 * from the service internals (caches, queues, etc).
 */
@ConfinedToDsfExecutor("")
public interface IDMData {
    
    /** 
     * Returns true if the data represented by this object is still valid.  
     * Data may become invalid if, for example the cache object backing this
     * data was cleared. 
     */
    public boolean isValid();
}
