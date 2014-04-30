/*******************************************************************************
 * Copyright (c) 2012,2014 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *     Marc Dumais (Ericsson) - Bug 407640
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.IVisualizerModelDataSource;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.ICPUDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.ICoreDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * DSF session state object.
 * 
 * Encapsulates and manages DsfSession we're currently tracking.
 */
public class DSFSessionState implements IVisualizerModelDataSource
{
	// --- members ---

	/** Current session ID. */
	protected String m_sessionId;

	/** Current set of session event listeners. */
	protected List<Object> m_sessionListeners;
	
	/** Services tracker, used to access services. */
	protected DsfServicesTracker m_servicesTracker;
	
	/** Cache for CPU contexts so we do not need to query backend each time */
    protected ICPUDMContext[] m_cpuContextsCache = null;
    
    /** Cache for core contexts so we do not need to query backend each time */
    protected ICoreDMContext[] m_coreContextsCache = null;

	// --- constructors/destructors ---
		
	public DSFSessionState(String sessionId) {
		m_sessionId = sessionId;
		m_sessionListeners = new ArrayList<Object>();
		m_servicesTracker = new DsfServicesTracker(MulticoreVisualizerUIPlugin.getBundleContext(), m_sessionId);
	}
	
	/** Dispose method. */
	@Override
	public void dispose()
	{
		if (m_sessionId != null) {
			removeAllServiceEventListeners();
			m_sessionId = null;
			m_sessionListeners = null;
		}
		
		if (m_servicesTracker != null) {
			m_servicesTracker.dispose();
			m_servicesTracker = null;
		}
		m_cpuContextsCache = null;
		m_coreContextsCache = null;
	}
	
	
	// --- accessors ---
	
	/** Returns session ID. */
	public String getSessionID()
	{
		return m_sessionId;
	}
	
	/** Returns the cached CPU contexts. Returns null if they are not yet available. */
    public ICPUDMContext[] getCPUContextsCache() {
            return m_cpuContextsCache;
    }
    
    /** Sets the cache of CPU contexts */
    public void setCPUContextsCache(ICPUDMContext[] cpuContextsCache) {
            m_cpuContextsCache = cpuContextsCache;
    }
    
    /** Returns the cached core contexts. Returns null if they are not yet available. */
    public ICoreDMContext[] getCoreContextsCache() {
            return m_coreContextsCache;
    }
    
    /** Sets the cache of core contexts */
    public void setCoreContextsCache(ICoreDMContext[] coreContextsCache) {
            m_coreContextsCache = coreContextsCache;
    }
	
	// --- listener management ---

	/** Adds a service event listener. */
	@Override
	public void addServiceEventListener(Object listener)
	{
		final Object listener_f = listener;
		final DsfSession session_f = getDsfSession();
		if (session_f != null) {
			try {
				session_f.getExecutor().execute(new DsfRunnable() {
					@Override
					public void run() {
						session_f.addServiceEventListener(listener_f, null);
						m_sessionListeners.add(listener_f);
					}
				});
    		} catch (RejectedExecutionException e) {
                // Session is shut down.
    		}
		}
	}
	
	/** Removes a service event listener. */
	@Override
	public void removeServiceEventListener(Object listener)
	{
		final Object listener_f = listener;
		final DsfSession session_f = getDsfSession();
		if (session_f != null) {
			try {
				session_f.getExecutor().execute(new DsfRunnable() {
					@Override
					public void run() {
						if (m_sessionListeners != null) {
							session_f.removeServiceEventListener(listener_f);
							m_sessionListeners.remove(listener_f);
						}
					}
				});
    		} catch (RejectedExecutionException e) {
                // Session is shut down.
    		}
		}
	}
	
	/** Removes all service event listeners. */
	@Override
	public void removeAllServiceEventListeners()
	{
		final DsfSession session_f = getDsfSession();
		if (session_f != null) {
			try {
				session_f.getExecutor().execute(new DsfRunnable() {
					@Override
					public void run() {
						if (m_sessionListeners != null) {
							for (Object listener : m_sessionListeners) {
								session_f.removeServiceEventListener(listener);
							}
							m_sessionListeners.clear();
						}
					}
				});
    		} catch (RejectedExecutionException e) {
                // Session is shut down.
    		}
		}
	}
	
	
	// --- methods ---
	
	/** Gets current DsfSession, if it's still active. */
	protected DsfSession getDsfSession() {
		return DsfSession.getSession(m_sessionId);
	}
	
	/** Executes DsfRunnable. */
	public void execute(DsfRunnable runnable)
	{
		try {
			DsfSession session = getDsfSession();
			if (session == null) {
				// TODO: log this?
			}
			else {
				session.getExecutor().execute(runnable);
			}
		}
		catch (RejectedExecutionException e) {
			// TODO: log or handle this properly.
			System.err.println("DSFSessionState.execute(): session rejected execution request."); //$NON-NLS-1$
		}
	}
	
	/** Gets service of the specified type. */
	@ConfinedToDsfExecutor("getDsfSession().getExecutor()")
	public <V> V getService(Class<V> serviceClass) {
		return (m_servicesTracker == null) ? null : m_servicesTracker.getService(serviceClass);
	}

	@Override
    public String getId() {
            return getSessionID();
    }
	
	@Override
	public boolean isAvailable() {
		return DSFDebugModel.isAvailable(this);
	}
}
