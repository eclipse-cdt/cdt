/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.service;

import java.util.Dictionary;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;

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
 * 
 * @since 1.0
 */
@ConfinedToDsfExecutor("getExecutor")
public interface IDsfService {
    
    /**
     * Property name for the session-id of this service.  This property should be set by 
     * all DSF services when they are registered with OSGI service framework. 
     */
    public final static String PROP_SESSION_ID = "org.eclipse.cdt.dsf.service.IService.session_id"; //$NON-NLS-1$
        
    /** 
     * Returns the DSF Session that this service belongs to.
     */
    public DsfSession getSession();
    
    /**
     * Returns the executor that should be used to call methods of this service.
     * This method is equivalent to calling getSession().getExecutor()
     */
    public DsfExecutor getExecutor();
    
    /**
     * Returns the map of properties that this service was registered with.
     */
    @SuppressWarnings("rawtypes")
    public Dictionary getProperties();
    
    /**
     * Returns a filter string that can be used to uniquely identify this 
     * service. This filter string should be based on the properties and class 
     * name, which were used to register this service.
     * @see org.osgi.framework.BundleContext#getServiceReferences
     */
    public String getServiceFilter();
    
    /**
     * Performs initialization and registration of the given service.  Implementation 
     * should initialize the service, so that all methods and events belonging to this 
     * service can be used  following the initialization.  
     * <br>Note: Since service initializaiton should be performed by an external
     * logic, if this service depends on other services, the implementaion should 
     * assume that these services are already present, and if they are not, the 
     * initializaiton should fail.  
     * @param requestMonitor callback to be submitted when the initialization is complete
     */
    public void initialize(RequestMonitor requestMonitor);
    
    /**
     * Performs shutdown and de-registration of the given service.    
     * @param requestMonitor callback to be submitted when shutdown is complete
     */
    public void shutdown(RequestMonitor requestMonitor);
    
    /**
     * Returns whether the service is currently registered.  A service should be 
     * registered after it is initialized and before it has been shut down.
     * 
     * @return <code>true</code> if registered
     * 
     * @since 2.0
     */
    public boolean isRegistered(); 
    
    /**
     * Returns the startup order number of this service among services in the same session.
     * Implementations should get this number during initialization by calling 
     * Session.getAndIncrementServiceStartupCounter().  This counter is used to Session 
     * objects to prioritize the listeners of service events.
     * @return startup order number of this service
     * @see org.eclipse.cdt.dsf.service.DsfSession#getAndIncrementServiceStartupCounter()
     */
    public int getStartupNumber();
}
