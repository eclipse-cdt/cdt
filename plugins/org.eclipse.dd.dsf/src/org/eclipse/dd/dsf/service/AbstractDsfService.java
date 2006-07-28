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
import java.util.Enumeration;

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;


/**
 * Standard base implementation of the Riverbed service.  This is a convinience
 * class that provides the basic functionality that all Riverbed services have 
 * to implement.
 */
abstract public class AbstractDsfService 
    implements IDsfService
{
    /** Reference to the session that this service belongs to. */ 
    private DsfSession fSession;

    /** Startup order number of this service. */
    private int fStartupNumber;
    
    /** Registration object for this service. */
    private ServiceRegistration fRegistration;
    
    /** Tracker for services that this service depends on. */
    private DsfServicesTracker fTracker;
    
    /** Properties that this service was registered with */
    private Dictionary fProperties;

    /** Properties that this service was registered with */
    private String fFilter;


    /** 
     * Only constructor, requires a reference to the session that this
     * service  belongs to.
     * @param session
     */
    public AbstractDsfService(DsfSession session) {
        fSession = session;
    }

    public DsfExecutor getExecutor() { return fSession.getExecutor(); }
    public Dictionary getProperties() { return fProperties; }
    public String getServiceFilter() { return fFilter; }
    public int getStartupNumber() { return fStartupNumber; }
    public void initialize(Done done) {
        fTracker = new DsfServicesTracker(getBundleContext(), fSession.getId());
        fStartupNumber = fSession.getAndIncrementServiceStartupCounter();
        getExecutor().submit(done);
    }
        
    public void shutdown(Done done) {
        fTracker.dispose();
        fTracker = null;
        getExecutor().submit(done);
    }

    /** Returns the session object for this service */
    public DsfSession getSession() { return fSession; }

    /**
     * Sub-classes should return the bundle context of the plugin, which the 
     * service belongs to.
     */
    abstract protected BundleContext getBundleContext();
    
    /**  Returns the tracker for the services that this service depends on. */
    protected DsfServicesTracker getServicesTracker() { return fTracker; }    
    
    /**
     * Registers this service.
     * <br> FIXME: Move registering call to default initialize()/shutdown().  Add a new 
     * protected method calcProperties() to get the initial list of properties.
     */
    @SuppressWarnings("unchecked")
    protected void register(String[] classes, Dictionary properties) {
        String[] newClasses = new String[classes.length + 2];
        System.arraycopy(classes, 0, newClasses, 2, classes.length);
        newClasses[0] = IDsfService.class.getName();
        newClasses[1] = getClass().getName();
        properties.put(PROP_SESSION_ID, getSession().getId());
        fProperties = properties;
        fRegistration = getBundleContext().registerService(newClasses, this, properties);
        fRegistration.getReference().getProperty(Constants.OBJECTCLASS);
        fFilter = generateFilter(fProperties);
        fProperties.put(Constants.OBJECTCLASS, fRegistration.getReference().getProperty(Constants.OBJECTCLASS));
    }
    
    private String generateFilter(Dictionary properties) {
        StringBuffer filter = new StringBuffer();
        filter.append("(&");
        
        // Add the service class to the filter.
        filter.append('(');
        filter.append(Constants.OBJECTCLASS);
        filter.append('=');        
        filter.append(this.getClass().getName());
        filter.append(')');
        
        for (Enumeration keys = properties.keys(); keys.hasMoreElements();) {
            Object key = keys.nextElement();
            filter.append('(');
            filter.append(key.toString());
            filter.append('=');
            filter.append(properties.get(key).toString());
            filter.append(')');
        }
        filter.append(')');
        return filter.toString();
    }
    
    /** 
     * De-registers this service.
     *
     */
    protected void unregister() {
        fRegistration.unregister();        
    }

    /** Returns the registration object that was obtained when this service was registered */
    protected ServiceRegistration getServiceRegistration() { return fRegistration; }
}