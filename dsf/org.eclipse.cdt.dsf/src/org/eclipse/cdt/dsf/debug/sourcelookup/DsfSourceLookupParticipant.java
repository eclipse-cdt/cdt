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
package org.eclipse.cdt.dsf.debug.sourcelookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;

/**
 * Source lookup participant that should be used with DSF-based debuggers.
 * 
 * @since 1.0
 */
@ThreadSafe
public class DsfSourceLookupParticipant implements ISourceLookupParticipant {
    protected static final Object[] EMPTY = new Object[0]; 

    private DsfExecutor fExecutor;
    private String fSessionId;
    private DsfServicesTracker fServicesTracker;
    private ISourceLookupDirector fDirector;
    private Map<String, List<Object>> fLookupCache = Collections.synchronizedMap(new HashMap<String, List<Object>>());
        
    public DsfSourceLookupParticipant(DsfSession session) {
        fSessionId = session.getId();
        fExecutor = session.getExecutor();
        fServicesTracker = new DsfServicesTracker(DsfPlugin.getBundleContext(), fSessionId);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant#init(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector)
     */
    @Override
    public void init(ISourceLookupDirector director) {
        fDirector = director;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant#dispose()
     */
    @Override
    public void dispose() {
        fServicesTracker.dispose();
        fDirector = null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant#findSourceElements(java.lang.Object)
     */
    @Override
    public Object[] findSourceElements(Object object) throws CoreException {
        CoreException single = null;
        MultiStatus multiStatus = null;
        List<Object> results = null;
        
        String name = getSourceName(object);
        if (name != null) {
            results = fLookupCache.get(name);
            if (results != null) {
                return results.toArray();
            } else {
                results = new ArrayList<Object>();
            }
            ISourceContainer[] containers = getSourceContainers();
            for (int i = 0; i < containers.length; i++) {
                try {
                    ISourceContainer container = containers[i];
                    if (container != null) {
                        Object[] objects = container.findSourceElements(name);
                        if (objects.length > 0) {
                            if (isFindDuplicates()) {
                                results.addAll(Arrays.asList(objects));
                            } else {
                                results.add(objects[0]);
                                break;
                            }
                        }
                    }
                } catch (CoreException e) {
                    if (single == null) {
                        single = e;
                    } else if (multiStatus == null) {
                        multiStatus = new MultiStatus(DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, new IStatus[]{single.getStatus()}, "Source Lookup error", null);  //$NON-NLS-1$
                        multiStatus.add(e.getStatus());
                    } else {
                        multiStatus.add(e.getStatus());
                    }
                }
            }
            
            if (!results.isEmpty()) {
                synchronized(fLookupCache) {
                    if (!fLookupCache.containsKey(name)) {
                        fLookupCache.put(name, results);
                    }
                }
            }
        }
        if (results == null || results.isEmpty()) {
            if (multiStatus != null) {
                throw new CoreException(multiStatus);
            } else if (single != null) {
                throw single;
            }
            return EMPTY;
        }
        return results.toArray();
    }   
    
    /**
     * Returns whether this participant's source lookup director is configured
     * to search for duplicate source elements.
     * 
     * @return whether this participant's source lookup director is configured
     * to search for duplicate source elements
     */
    protected boolean isFindDuplicates() {
        ISourceLookupDirector director = fDirector;
        if (director != null) {
            return director.isFindDuplicates();
        }
        return false;
    }   
    
    /**
     * Returns the source containers currently registered with this participant's
     * source lookup director.
     * 
     * @return the source containers currently registered with this participant's
     * source lookup director
     */
    protected ISourceContainer[] getSourceContainers() {
        ISourceLookupDirector director = fDirector;
        if (director != null) {
            return director.getSourceContainers();
        }
        return new ISourceContainer[0];
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant#sourceContainersChanged(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector)
     */
    @Override
    public void sourceContainersChanged(ISourceLookupDirector director) {
        fLookupCache.clear();
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupParticipant#getSourceName(java.lang.Object)
	 */
    @Override
	public String getSourceName(Object object) throws CoreException {
		if ( !(object instanceof IDMContext) || 
             !((IDMContext)object).getSessionId().equals(fSessionId) ) 
        {
            throw new CoreException(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, -1, "Invalid object", null)); //$NON-NLS-1$
        }
        
        final IDMContext dmc = (IDMContext)object;
        Query<String> query = new Query<String>() { 
            @Override
            protected void execute(final DataRequestMonitor<String> rm) {
                getSourceNameOnDispatchThread(dmc, rm);
            }};
            fExecutor.execute(query);
            try {
                String result = query.get();
                if ((result != null) && (result.length() == 0)) {
                	// interface javadoc says we should return null 
                	result = null; 
                }
                return result;
            } catch (InterruptedException e) { assert false : "Interrupted exception in DSF executor";  //$NON-NLS-1$
            } catch (ExecutionException e) { 
                if (e.getCause() instanceof CoreException) {
                    throw (CoreException)e.getCause();
                }
                assert false : "Unexptected exception"; //$NON-NLS-1$
            }
            return null; // Should never get here.
	}

    @ConfinedToDsfExecutor("fExecutor")
    private void getSourceNameOnDispatchThread(IDMContext dmc, final DataRequestMonitor<String> rm) {
        if (!(dmc instanceof IStack.IFrameDMContext)) {
            rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "No source for this object", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        IFrameDMContext frameDmc = (IFrameDMContext)dmc; 
            
        IStack stackService = fServicesTracker.getService(IStack.class); 
        if (stackService == null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Stack data not available", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        stackService.getFrameData(
            frameDmc, 
            new DataRequestMonitor<IFrameDMData>(fExecutor, rm) { @Override
            public void handleSuccess() {
                rm.setData(getData().getFile());
                rm.done();
            }});
    }
}
