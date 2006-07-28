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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.DsfPlugin;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.osgi.framework.Filter;

/**
 * Class to manage Riverbed sessions.  A Riverbed session is a way to 
 * associate a set of Riverbed services that are running simultaneously and 
 * are interacting with each other to provide a complete set of functionality.
 * <p>
 * Properties of a session are following:
 * <br>1. Each session is associated with a single Riverbed executor, although there
 * could be multiple sessions using the same executor.
 * <br>2. Each session has a unique String identifier, which has to be used by
 * the services belonging to this session when registering with OSGI services.
 * <br>3. Each session has its set of service event listeners.
 * <br>4. Start and end of each session is announced by events, which are always
 * sent on that session's executor dispatch thread.      
 * 
 * @see org.eclipse.dd.dsf.concurrent.DsfExecutor
 */
public class DsfSession 
{
    
    /** 
     * Listener for session started events.  This listener is always going to be
     * called in the dispatch thread of the session's executor.  
     * 
     */
    public static interface SessionStartedListener {
        /** 
         * Called when a new session is started.  It is always called in the 
         * dispatch thread of the new session.
         */
        public void sessionStarted(DsfSession session);
    }

    /** 
     * Listener for session ended events.  This listener is always going to be
     * called in the dispatch thread of the session's executor. 
     */
    public static interface SessionEndedListener {
        /** 
         * Called when a session is ended.  It is always called in the 
         * dispatch thread of the session.
         */
        public void sessionEnded(DsfSession session);
    }

    private static int fgSessionIdCounter = 0; 
    private static Set<DsfSession> fgActiveSessions = Collections.synchronizedSet(new HashSet<DsfSession>());
    private static List<SessionStartedListener> fSessionStartedListeners = Collections.synchronizedList(new ArrayList<SessionStartedListener>());
    private static List<SessionEndedListener> fSessionEndedListeners = Collections.synchronizedList(new ArrayList<SessionEndedListener>());
    
    /** Returns true if given session is currently active */
    public static boolean isSessionActive(String sessionId) {
        return getSession(sessionId) != null;
    }
    
    /** Returns a session instance for given session identifier */
    public static DsfSession getSession(String sessionId) {
        for (DsfSession session : fgActiveSessions) {
            if (session.getId().equals(sessionId)) {
                return session;
            }
        } 
        return null;
    }
    
    /** 
     * Registers a listener for session started events.
     * Can be called on any thread. 
     */
    public static void addSessionStartedListener(SessionStartedListener listener) {
        assert !fSessionStartedListeners.contains(listener);
        fSessionStartedListeners.add(listener);
    }
    
    /** 
     * Un-registers a listener for session started events. 
     * Can be called on any thread. 
     */ 
    public static void removeSessionStartedListener(SessionStartedListener listener) {
        assert fSessionStartedListeners.contains(listener);
        fSessionStartedListeners.remove(listener);
    }
    
    /** 
     * Registers a listener for session ended events. 
     * Can be called on any thread. 
     */ 
    public static void addSessionEndedListener(SessionEndedListener listener) {
        assert !fSessionEndedListeners.contains(listener);
        fSessionEndedListeners.add(listener);
    }

    /** 
     * Un-registers a listener for session ended events. 
     * Can be called on any thread. 
     */ 
    public static void removeSessionEndedListener(SessionEndedListener listener) {
        assert fSessionEndedListeners.contains(listener);
        fSessionEndedListeners.remove(listener);
    }

    /**
     * Starts and returns a new session instance.  This method can be called on any
     * thread, but the session-started listeners will be called using the session's 
     * executor.
     * @param executor The Riverbed executor to use for this session.
     * @return instance object of the new session
     */
    public static DsfSession startSession(DsfExecutor executor) {
        synchronized(fgActiveSessions) {
            final DsfSession newSession = new DsfSession(executor, Integer.toString(fgSessionIdCounter++));
            fgActiveSessions.add(newSession);
            executor.submit( new DsfRunnable() { public void run() {
                SessionStartedListener[] listeners = fSessionStartedListeners.toArray(
                    new SessionStartedListener[fSessionStartedListeners.size()]);
                for (int i = 0; i < listeners.length; i++) {
                    listeners[i].sessionStarted(newSession);
                }
            }});
            return newSession;
        }
    }

    /**
     * Terminates the given session.  This method can be also called on any
     * thread, but the session-ended listeners will be called using the session's 
     * executor.
     * @param session session to terminate
     */
    public static void endSession(final DsfSession session) {
        synchronized(fgActiveSessions) {
            if (!fgActiveSessions.contains(session)) {
                throw new IllegalArgumentException();
            }
            fgActiveSessions.remove(session);
            session.getExecutor().submit( new DsfRunnable() { public void run() {
                SessionEndedListener[] listeners = fSessionEndedListeners.toArray(
                    new SessionEndedListener[fSessionEndedListeners.size()]);
                for (int i = 0; i < listeners.length; i++) {
                    listeners[i].sessionEnded(session);
                }
            }});
        }
    }

    private static class ListenerEntry {
        Object fListener;
        Filter fFilter;

        ListenerEntry(Object listener, Filter filter) {
            fListener = listener;
            fFilter = filter;
        }
        
        public boolean equals(Object other) {
            return other instanceof ListenerEntry && fListener.equals(((ListenerEntry)other).fListener);
        }
        
        public int hashCode() { return fListener.hashCode(); }
    }

    /** Session ID of this session. */
    private String fId;
    
    /** Dispatch-thread executor for this session */
    private DsfExecutor fExecutor;
    
    /** Service start-up counter for this session */ 
    private int fServiceInstanceCounter;
    
    /** Map of registered event listeners. */
    private Map<ListenerEntry,Method[]> fListeners = new HashMap<ListenerEntry,Method[]>();
    
    /** 
     * Map of registered adapters, for implementing the 
     * IModelContext.getAdapter() method.
     * @see org.eclipse.dd.dsf.model.AbstractDMC#getAdapter 
     */
    private Map<Class,Object> fAdapters = Collections.synchronizedMap(new HashMap<Class,Object>());

    
    /** Returns the ID of this session */
    public String getId() { return fId; }
    
    /** Returns the Riverbed executor of this session */
    public DsfExecutor getExecutor() { return fExecutor; }
 
    /**
     * Adds a new listener for service events in this session.
     * @param listener the listener that will receive service events
     * @param filter optional filter to restrict the services that the 
     * listener will receive events from 
     */
    public void addServiceEventListener(Object listener, Filter filter) {
        ListenerEntry entry = new ListenerEntry(listener, filter);
        assert !fListeners.containsKey(entry);
        fListeners.put(entry, getEventHandlerMethods(listener));
    }
    
    /**
     * Removes the given listener.
     * @param listener listener to remove
     */
    public void removeServiceEventListener(Object listener) {
        ListenerEntry entry = new ListenerEntry(listener, null);
        assert fListeners.containsKey(entry);
        fListeners.remove(entry);
    }

    /**
     * Retrieves and increments the startup counter for services in this session.
     * Riverbed services should retrieve this counter when they are initialized, 
     * and should return it through IService.getStartupNumber().  This number is then
     * used to prioritize service events.
     * @return current startup counter value
     */
    public int getAndIncrementServiceStartupCounter() { return fServiceInstanceCounter++; }
    
    /**
     * Dispatches the given event to service event listeners.  The event is submitted to 
     * the executor to be dispatched.
     * @param event to be sent out
     * @param serviceProperties properties of the service requesting the event to be dispatched
     */
    public void dispatchEvent(final Object event, final Dictionary serviceProperties) {
        getExecutor().submit(new DsfRunnable() { public void run() {
        	// TED added FIXME otherwise no way to detect!!!
        	try { 
            doDispatchEvent(event, serviceProperties);
        	} catch(Throwable e) { e.printStackTrace(); } 
        }});
    }
    
    /**
     * Registers a IModelContext adapter of given type.
     * @param adapterType class type to register the adapter for
     * @param adapter adapter instance to register
     * @see org.eclipse.dsdp.model.AbstractDMC#getAdapter
     */
    public void registerModelAdapter(Class adapterType, Object adapter) {
        fAdapters.put(adapterType, adapter);
    }
    
    /**
     * Un-registers a IModelContext adapter of given type.
     * @param adapterType adapter type to unregister
     * @see org.eclipse.dsdp.model.AbstractDMC#getAdapter
     */
    public void unregisterModelAdapter(Class adapterType) {
        fAdapters.remove(adapterType);
    }
    
    /** 
     * Retrieves an adapter for given type for IModelContext.
     * @param adapterType adapter type to look fors
     * @return adapter object for given type, null if none is registered with the session
     * @see org.eclipse.dsdp.model.AbstractDMC#getAdapter
     */
    public Object getModelAdapter(Class adapterType) {
        return fAdapters.get(adapterType);
    }
    
    public boolean equals(Object other) {
        return other instanceof DsfSession && fId.equals(((DsfSession)other).fId);
    }
    
    public int hashCode() { return fId.hashCode(); }

    private void doDispatchEvent(Object event, Dictionary serviceProperties) {
        // Build a list of listeners;
        SortedMap<ListenerEntry,List<Method>> listeners = new TreeMap<ListenerEntry,List<Method>>(new Comparator<ListenerEntry>() {
                public int compare(ListenerEntry o1, ListenerEntry o2) {
                    if (o1.fListener == o2.fListener) {
                        return 0;
                    } if (o1.fListener instanceof IDsfService && !(o2.fListener instanceof IDsfService)) {
                        return Integer.MAX_VALUE;
                    } else if (o2.fListener instanceof IDsfService && !(o1.fListener instanceof IDsfService)) {
                        return Integer.MIN_VALUE;
                    } else if ( (o1.fListener instanceof IDsfService) && (o2.fListener instanceof IDsfService) ) {
                        return ((IDsfService)o1.fListener).getStartupNumber() - ((IDsfService)o2.fListener).getStartupNumber();
                    }
                    return 1;
                };
                
                public boolean equals(Object obj) {
                    return obj == this;
                };
            });

        // Build a list of listeners and methods that are registered for this event class.
        Class<?> eventClass = event.getClass();
        for (Map.Entry<ListenerEntry,Method[]> entry : fListeners.entrySet()) {
            if (entry.getKey().fFilter != null && !entry.getKey().fFilter.match(serviceProperties)) {
                // Dispatching service doesn't match the listener's filter, skip it.
                continue;
            }
            Method[] allMethods = entry.getValue();
            List<Method> matchingMethods = new ArrayList<Method>();
            for (Method method : allMethods) {
            	assert method.getParameterTypes().length > 0 : eventClass.getName() + "." + method.getName()
            		+ " signature contains zero parameters";
    	    	if ( method.getParameterTypes()[0].isAssignableFrom(eventClass) ) {
                    matchingMethods.add(method);
                }
            }
            if (!matchingMethods.isEmpty()) {
                listeners.put(entry.getKey(), matchingMethods);
            }
        }
        
        // Call the listeners
        for (Map.Entry<ListenerEntry,List<Method>> entry : listeners.entrySet()) {
            for (Method method : entry.getValue()) {
                try {
                    method.invoke(entry.getKey().fListener, new Object[] { event } );
                }
                catch (IllegalAccessException e) {
                    DsfPlugin.getDefault().getLog().log(new Status(
                        IStatus.ERROR, DsfPlugin.PLUGIN_ID, -1, "Security exception when calling a service event handler method", e));
                    assert false : "IServiceEventListener.ServiceHandlerMethod method not accessible, is listener declared public?";
                }
                catch (InvocationTargetException e) {
                    DsfPlugin.getDefault().getLog().log(new Status(
                        IStatus.ERROR, DsfPlugin.PLUGIN_ID, -1, "Invocation exception when calling a service event handler method", e));
                    assert false : "Exception thrown by a IServiceEventListener.ServiceHandlerMethod method";
                }
            }
        }
    }

    private Method[] getEventHandlerMethods(Object listener) 
    {
        List<Method> retVal = new ArrayList<Method>();
        try {
            Method[] methods = listener.getClass().getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(DsfServiceEventHandler.class)) {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    if (paramTypes.length > 2) {
                        throw new IllegalArgumentException("ServiceEventHandler method has incorrect number of parameters");
                    } 
                    retVal.add(method);
                }
            }
        } catch(SecurityException e) {
            throw new IllegalArgumentException("No permission to access ServiceEventHandler method");
        }
        
        if (retVal.isEmpty()) {
            throw new IllegalArgumentException("No methods marked with @ServiceEventHandler in listener, is listener declared public?");
        }
        return retVal.toArray(new Method[retVal.size()]);
    }
    
    /**
     * Class to be instanciated only using startSession()
     */
    private DsfSession(DsfExecutor executor, String id) {
        fId = id;
        fExecutor = executor;
    }
    
}