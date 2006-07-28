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
package org.eclipse.dd.dsf.model;

/**
 * Data object containing information regarding a model context.  Unlike the 
 * context object, this object does have to be accessed on the dispatch thread, 
 * unless other-wise noted.  And it does not need to be immutable or free of 
 * references to the service.  
 * <p>
 * This interface is intended primarily to allow for future development of
 * a generic API to parametrize data model data.
 * 
 */
public interface IDataModelData {
    
    /** 
     * Returns true if the data represented by this object is still valid.  
     * Data may become invalid if, for example the cache object backing this
     * data was cleared. 
     */
    public boolean isValid();
}
