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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Convenience class to help track Riverbed services that a given
 * client needs to use.  This class is similar to the standard OSGI 
 * org.osgi.util.tracker.ServiceTracker class, with a few differences:
 * <br>1. This class is assumed to be accessed by a single thread hence it
 * has no synchronization built in, while OSGI ServiceTracker synchronized 
 * access to its data.
 * <br>2. This class is primarily designed to track multiple services of 
 * different type (class), while OSGI ServiceTracker is designed to work with
 * single class type, with optional filtering options.
 * <br>3. This class uses knowledge of Riverbed sessions to help narrow down
 * service references.
 * <br>4. OSGI Service tracker explicitly listens to OSGI service 
 * startup/shutdown events and it will clear a reference to a service as
 * soon as it's shut down.  This class leaves it up to the client to make
 * sure that it doesn't access a service once that service has been shut down.
 * <p>
 * That said, it might be more convenient for certain types of clients to use
 * OSGI Service tracker for the additional features it provides. 
 * 
 * @see org.osgi.util.tracker.ServiceTracker
 */
public class DsfServicesTracker {
    
    private static String getServiceFilter(String sessionId) {
        return ("(" + IDsfService.PROP_SESSION_ID + "=" + sessionId + ")").intern(); 
    }

    private static class ServiceKey 
    {
        String fClassString;
        String fFilter;
        public ServiceKey(String classString, String filter) {
            fClassString = classString;
            fFilter = filter;
        }
        
        public boolean equals(Object other) {
            // I guess this doesn't have to assume fFilter can be null, but oh well.
            return other instanceof ServiceKey && 
                   ((ServiceKey)other).fClassString.equals(fClassString) &&
                   ((fFilter == null && ((ServiceKey)other).fFilter == null) || 
                    (fFilter != null && fFilter.equals(((ServiceKey)other).fFilter))); 
        }
        
        public int hashCode() {
            return fClassString.hashCode() + (fFilter == null ? 0 : fFilter.hashCode());
        }
    }
    
    private BundleContext fBundleContext;
    private Map<ServiceKey,ServiceReference> fServiceReferences = new HashMap<ServiceKey,ServiceReference>();
    private Map<ServiceReference,IDsfService> fServices = new HashMap<ServiceReference,IDsfService>();
    private String fServiceFilter;

    /** 
     * Only constructor.
     * @param bundleContext Context of the plugin that the client lives in. 
     * @param sessionId The Riverbed session that this tracker will be used for. 
     */
    public DsfServicesTracker(BundleContext bundleContext, String sessionId) {
        fBundleContext = bundleContext;
        fServiceFilter = getServiceFilter(sessionId); 
    }
    
    /**
     * Retrieves a service reference for given service class and optional filter.  
     * Filter should be used if there are multiple instances of the desired service
     * running within the same session. 
     * @param serviceClass class of the desired service
     * @param custom filter to use when searching for the service, this filter will 
     * be used instead of the standard filter so it should also specify the desired 
     * session-ID 
     * @return OSGI service reference object to the desired service, null if not found
     */
    public ServiceReference getServiceReference(Class serviceClass, String filter) {
        ServiceKey key = new ServiceKey(serviceClass.getName().intern(), filter != null ? filter : fServiceFilter);
        if (fServiceReferences.containsKey(key)) {
            return fServiceReferences.get(key);
        }
        
        try {
            ServiceReference[] references = fBundleContext.getServiceReferences(key.fClassString, key.fFilter);
            assert references == null || references.length <= 1;
            if (references == null || references.length == 0) {
                return null;
            } else {
                fServiceReferences.put(key, references[0]);
                return references[0];
            }
        } catch(InvalidSyntaxException e) {
            assert false : "Invalid session ID syntax";
        }
        return null;
    }
    
    /**
     * Convenience class to retrieve a service based on class name only.
     * @param serviceClass class of the desired service
     * @return instance of the desired service, null if not found
     */
    public <V extends IDsfService> V getService(Class<V> serviceClass) {
        return getService(serviceClass, null);
    }
    
    /** 
     * Retrieves the service given service class and optional filter.
     * Filter should be used if there are multiple instances of the desired service
     * running within the same session. 
     * @param serviceClass class of the desired service
     * @param custom filter to use when searching for the service, this filter will 
     * be used instead of the standard filter so it should also specify the desired 
     * session-ID 
     * @return instance of the desired service, null if not found
     */
    @SuppressWarnings("unchecked")
    public <V extends IDsfService> V getService(Class<V> serviceClass, String filter) {
        ServiceReference serviceRef = getServiceReference(serviceClass, filter);
        if (serviceRef == null) {
            return null;
        } else {
            if (fServices.containsKey(serviceRef)) {
                return (V)fServices.get(serviceRef);
            } else {
                V service = (V)fBundleContext.getService(serviceRef);
                fServices.put(serviceRef, service);
                return service;
            }
        }
    }
    
    /**
     * Un-gets all the serferences held by this tracker.  Must be called
     * to avoid leaking OSGI service references.
     */
    public void dispose() {
        for (Iterator itr = fServices.keySet().iterator(); itr.hasNext();) {
            fBundleContext.ungetService((ServiceReference)itr.next());
            itr.remove();
        }
    }

}
