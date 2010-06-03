/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.datamodel;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.core.runtime.IAdaptable;

/**
 * The base class for data model objects.  
 * <p>
 * DSF services need to return objects to clients which can be used as 
 * handles to track data stored in the service.  Clients such as lazy-loading 
 * tree and table views retrieve a list of handles, then as needed, they 
 * retrieve the children and label information for these handles.  Because of 
 * this pattern, services need to be able to return a set of handle objects,
 * then as needed clients can retrieve data corresponding to these handles.
 * The Data Model Context object is the interface that DSF services should use
 * to represent the handle objects that are to be referenced by view model.
 * <p>
 * <i>Note: DM contexts are meant to be immutable and thus accessible from 
 * any thread instead of just the services dispatch thread. This is because 
 * clients may need to call context objects' methods on non-dispatch thread, 
 * especially equals and hashCode.</i>    
 * <p>
 * <i>Note #2: DM Contexts should also avoid holding references to service 
 * instances or other large chunks of data, because some of the clients may 
 * hold onto these objects for longer time than the life of the service.  
 * This may prevent the service from being garbage collected, possibly keeping 
 * a lot of resources tied up.  
 * 
 * @since 1.0
 */
@Immutable
public interface IDMContext extends IAdaptable 
{
    /** 
     * Each model context object needs to track the session from which it 
     * originated.  The session ID allows clients to choose the correct
     * dispatch thread with which to access the service, and it allows the
     * service to be uniquely identified among other sessions.   
     * @return Session ID of the service that originated the context.
     */
    public String getSessionId();
    
    /**
     * Returns the parent context of this context.  ModelContext objects can be 
     * chained this way to allow methods that require context from multiple 
     * services to retrieve this context from a single handle that comes from
     * the client.
     * @return parent context of this context.
     */
    public IDMContext[] getParents();
}
