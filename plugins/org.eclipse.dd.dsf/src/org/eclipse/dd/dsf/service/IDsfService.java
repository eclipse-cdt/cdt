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
package org.eclipse.dd.dsf.service;

import java.util.Dictionary;

import org.eclipse.dd.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;

/**
 * The inteface that all DSF services must implement.  It only privides a 
 * few features to help manage and identify the servies using the OSGI services 
 * framework.
 * <p>
 * Each service should register itself with OSGI services framework using
 * the BundleContext.registerService() method.  And each service should use the 
 * session ID that it is registering with as one of the service properties.  If there
 * is more than one instance of the service to be instanciated for a given session, 
 * additional properties should be used when registering the service to allow clients 
 * to uniquely identify the services.
 * <p>
 * By convention, all methods of DSF services can be called only on the dispatch
 * thread of the DSF executor that is associated with the service.  If a 
 * service exposes a method that is to be called on non-dispatch thread, it should 
 * be documented so. 
 *  
 * @see org.osgi.framework.BundleContext#registerService(String[], Object, Dictionary)
 */
@ConfinedToDsfExecutor("getExecutor")
public interface IDsfService {
    
    /**
     * Property name for the session-id of this service.  This property should be set by 
     * all DSF services when they are registered with OSGI service framework. 
     */
    final static String PROP_SESSION_ID = "org.eclipse.dd.dsf.service.IService.session_id"; //$NON-NLS-1$
    
    /** 
     * Error code indicating that the service is in a state which does not allow the 
     * request to be processed.  For example if the client requested target information
     * after target was disconnected. 
     */
    final static int INVALID_STATE = 10001;
    
    /** 
     * Error code indicating that client supplied an invalid handle to the service.
     * A handle could become invalid after an object it represents is removed from 
     * the system.
     */
    final static int INVALID_HANDLE = 10002;
    
    /**
     * Error code indicating that the client request is not supported/implemented.
     */
    final static int NOT_SUPPORTED = 10003;
    
    /**
     * Error code indicating that the request to a sub-service or an external process 
     * failed.
     */
    final static int REQUEST_FAILED = 10004;

    /**
     * Error code indicating an unexpected condition in the service, i.e. programming error.
     */
    final static int INTERNAL_ERROR = 10005;
    
    
    /**
     * Returns the executor that should be used to call methods of this service.
     * @return 
     */
    DsfExecutor getExecutor();
    
    /**
     * Returns the map of properties that this service was registered with.
     */
    @SuppressWarnings("unchecked")
    Dictionary getProperties();
    
    /**
     * Returns a filter string that can be used to uniquely identify this 
     * service. This filter string should be based on the properties and class 
     * name, which were used to register this service.
     * @see org.osgi.framework.BundleContext#getServiceReferences
     */
    String getServiceFilter();
    
    /**
     * Performs initialization and registration of the given service.  Implementation 
     * should initialize the service, so that all methods and events belonging to this 
     * service can be used  following the initialization.  
     * <br>Note: Since service initializaiton should be performed by an external
     * logic, if this service depends on other services, the implementaion should 
     * assume that these services are already present, and if they are not, the 
     * initializaiton should fail.  
     * @param done callback to be submitted when the initialization is complete
     */
    void initialize(Done done);
    
    /**
     * Performs shutdown and de-registration of the given service.    
     * @param done callback to be submitted when shutdown is complete
     */
    void shutdown(Done done);
    
    /**
     * Returns the startup order number of this service among services in the same session.
     * Implementations should get this number during initialization by calling 
     * Session.getAndIncrementServiceStartupCounter().  This counter is used to Session 
     * objects to prioritize the listeners of service events.
     * @return startup order number of this service
     * @see org.eclipse.dd.dsf.service.DsfSession#getAndIncrementServiceStartupCounter()
     */
    int getStartupNumber();
}
