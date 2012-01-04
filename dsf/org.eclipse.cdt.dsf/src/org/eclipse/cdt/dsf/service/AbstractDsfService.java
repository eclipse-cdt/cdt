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

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;


/**
 * Standard base implementation of the DSF service.  This is a convenience
 * class that provides the basic functionality that all DSF services have 
 * to implement.
 * 
 * @since 1.0
 */
abstract public class AbstractDsfService 
    implements IDsfService, IDsfStatusConstants
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
    @SuppressWarnings("rawtypes")
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

    @Override
    public DsfExecutor getExecutor() { return fSession.getExecutor(); }

	/**
	 * The the returned collection is a superset of the properties specified in
	 * {@link #register(String[], Dictionary)}. That method can add additional
	 * (implicit) properties. For one, it tacks on the
	 * {@link Constants#OBJECTCLASS} property associated with the service after
	 * it has been registered. It also adds a property that designates the dsf
	 * session ID.
	 * 
	 * @see org.eclipse.cdt.dsf.service.IDsfService#getProperties()
	 */
    @SuppressWarnings("rawtypes")
    @Override
    public Dictionary getProperties() { return fProperties; }
    
    @Override
    public String getServiceFilter() { return fFilter; }
    
    @Override
    public int getStartupNumber() { return fStartupNumber; }
    
    @Override
    public void initialize(RequestMonitor rm) {
        fTracker = new DsfServicesTracker(getBundleContext(), fSession.getId());
        fStartupNumber = fSession.getAndIncrementServiceStartupCounter();
        rm.done();
    }
        
    @Override
    public void shutdown(RequestMonitor rm) {
        fTracker.dispose();
        fTracker = null;
        rm.done();
    }
    
    /**
	 * @since 2.0
	 */
    @Override
    public boolean isRegistered() {
        return getServiceRegistration() != null;
    }

    /** Returns the session object for this service */
    @Override
    public DsfSession getSession() { return fSession; }

    /**
     * Sub-classes should return the bundle context of the plugin, which the 
     * service belongs to.
     */
    abstract protected BundleContext getBundleContext();
    
    /**  Returns the tracker for the services that this service depends on. */
    protected DsfServicesTracker getServicesTracker() { return fTracker; }

	/**
	 * Registers this DSF/OSGi service.
	 * 
	 * @param classes
	 *            The class names under which the service can be located. For
	 *            convenience, [classes] need not contain {@link IDsfService} or
	 *            the runtime class of this object; they are automatically added
	 *            if not present. The complete list of classes the service ends
	 *            up being registered in OSGi with is recorded and made
	 *            available via
	 *            <code>getProperties().get(Constants.OBJECTCLASS)</code>
	 * @param properties
	 *            The properties for this service. The keys in the properties
	 *            object must all be <code>String</code> objects. See
	 *            {@link Constants} for a list of standard service property
	 *            keys. Changes should not be made to this object after calling
	 *            this method. To update the service's properties the
	 *            {@link ServiceRegistration#setProperties} method must be
	 *            called. Caller should, at a minimum, pass an empty
	 *            dictionary--never null. We add a property to the collection
	 *            (we modify the caller's object), to record the id of the dsf
	 *            session associated with the service.
	 */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void register(String[] classes, Dictionary properties) {
    	
    	/*
    	 * If this service has already been registered, make sure we
    	 * keep the names it has been registered with.  However, we
    	 * must trigger a new registration or else OSGI will keep the two
    	 * registration separate. 
    	 */
    	if (fRegistration != null) {
    		String[] previousClasses = (String[])fRegistration.getReference().getProperty(Constants.OBJECTCLASS);
    		
            // Use a HashSet to avoid duplicates
        	Set<String> newClasses = new HashSet<String>();
        	newClasses.addAll(Arrays.asList(previousClasses));
        	newClasses.addAll(Arrays.asList(classes));
        	classes = newClasses.toArray(new String[0]);

        	/*
        	 * Also keep all previous properties.
        	 */
            if (fProperties != null) {
            	for (Enumeration e = fProperties.keys() ; e.hasMoreElements();) {
            		Object key = e.nextElement();
            		Object value = fProperties.get(key);
            		properties.put(key, value);
            	}
            }
            
        	// Now, cancel the previous registration
    		unregister();
    	}
        /*
         * Ensure that the list of classes contains the base DSF service 
         * interface, as well as the actual class type of this object.
         */
        if (!Arrays.asList(classes).contains(IDsfService.class.getName())) {
            String[] newClasses = new String[classes.length + 1];
            System.arraycopy(classes, 0, newClasses, 1, classes.length);
            newClasses[0] = IDsfService.class.getName();
            classes = newClasses;
        }
        if (!Arrays.asList(classes).contains(getClass().getName())) {
            String[] newClasses = new String[classes.length + 1];
            System.arraycopy(classes, 0, newClasses, 1, classes.length);
            newClasses[0] = getClass().getName();
            classes = newClasses;
        }
        /*
         * Make sure that the session ID is set in the service properties.
         * The session ID distinguishes this service instance from instances
         * of the same service in other sessions.
         */
        properties.put(PROP_SESSION_ID, getSession().getId());
        fProperties = properties;
        fRegistration = getBundleContext().registerService(classes, this, properties);
        
        /*
         * Retrieve the OBJECTCLASS property directly from the service 
         * registration info.  This is the best bet for getting an accurate 
         * value.
         */
        fProperties.put(Constants.OBJECTCLASS, fRegistration.getReference().getProperty(Constants.OBJECTCLASS));
        
        /*
         * Create the filter for this service based on all the properties.  If 
         * there is a single service instance per session, or if the properties
         * parameter uniquely identifies this service instance among other 
         * instances in this session.  Then this filter will fetch this service
         * and only this service from OSGi.
         */
        fFilter = generateFilter(fProperties);
    }

    /**
     * Generates an LDAP filter to uniquely identify this service.
     */
    @SuppressWarnings({ "rawtypes" })
    private String generateFilter(Dictionary properties) {
        StringBuffer filter = new StringBuffer();
        filter.append("(&"); //$NON-NLS-1$
        
        for (Enumeration keys = properties.keys(); keys.hasMoreElements();) {
            Object key = keys.nextElement();
            Object value = properties.get(key);
            if (value instanceof Object[]) {
                /*
                 * For arrays, add a test to check that every element in array 
                 * is present.  This is here mainly to handle OBJECTCLASS property.
                 */
                for (Object arrayValue : (Object[])value) {
                    filter.append('(');
                    filter.append(key.toString());
                    filter.append("=*"); //$NON-NLS-1$
                    filter.append(arrayValue.toString());
                    filter.append(')');
                }
            } else {
                filter.append('(');
                filter.append(key.toString());
                filter.append('=');
                filter.append(value.toString());
                filter.append(')');
            }
        }
        filter.append(')');
        return filter.toString();
    }
    
    /** 
     * De-registers this service.
     *
     */
    protected void unregister() {
    	if (fRegistration != null) {
    		fRegistration.unregister();
    	}
    	fRegistration = null;
    }

    /** Returns the registration object that was obtained when this service was registered */
    protected ServiceRegistration getServiceRegistration() { return fRegistration; }
}