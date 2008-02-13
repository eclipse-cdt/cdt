/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.mi.service;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.debug.service.ISourceLookup;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.mi.internal.MIPlugin;
import org.osgi.framework.BundleContext;

/**
 * ISourceLookup service implementation based on the CDT CSourceLookupDirector.
 */
public class CSourceLookup extends AbstractDsfService implements ISourceLookup {

    private Map<ISourceLookupDMContext,CSourceLookupDirector> fDirectors = new HashMap<ISourceLookupDMContext,CSourceLookupDirector>(); 
    
    public CSourceLookup(DsfSession session) {
        super(session);
    }
    
    @Override
    protected BundleContext getBundleContext() {
        return MIPlugin.getBundleContext();
    }

    
    public void setSourceLookupDirector(ISourceLookupDMContext ctx, CSourceLookupDirector director) {
        fDirectors.put(ctx, director);
    }
    
    @Override
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize(
            new RequestMonitor(getExecutor(), requestMonitor) { 
                @Override
                protected void handleOK() {
                    doInitialize(requestMonitor);
                }});
    }

    private void doInitialize(final RequestMonitor requestMonitor) {
        // Register this service
        register(new String[] { CSourceLookup.class.getName(), ISourceLookup.class.getName() }, new Hashtable<String, String>());
        requestMonitor.done();
    }

    @Override
    public void shutdown(final RequestMonitor requestMonitor) {
        unregister();
        super.shutdown(requestMonitor);
    }

    public void getDebuggerPath(ISourceLookupDMContext sourceLookupCtx, Object source, final DataRequestMonitor<String> rm) 
    {
        if (! (source instanceof String)) {
            // In future if needed other elements such as URIs could be supported.
            rm.setStatus(new Status(IStatus.ERROR, MIPlugin.PLUGIN_ID, IDsfService.NOT_SUPPORTED, "Only string source element is supported", null)); //$NON-NLS-1$);
            rm.done();
            return;
        }
        final String sourceString = (String) source;
        
        if (!fDirectors.containsKey(sourceLookupCtx) ){
            rm.setStatus(new Status(IStatus.ERROR, MIPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "No source director configured for given context", null)); //$NON-NLS-1$);
            rm.done();
            return;
        } 
        final CSourceLookupDirector director = fDirectors.get(sourceLookupCtx);
        
        new Job("Lookup Debugger Path") { //$NON-NLS-1$
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                IPath debuggerPath = director.getCompilationPath(sourceString);
                if (debuggerPath != null) {
                    rm.setData(debuggerPath.toString());
                } else {
                    rm.setData(sourceString);
                }
                rm.done();

                return Status.OK_STATUS;
            }
        }.schedule();       
        
    }

    public void getSource(ISourceLookupDMContext sourceLookupCtx, final String debuggerPath, final DataRequestMonitor<Object> rm) 
    {
        if (!fDirectors.containsKey(sourceLookupCtx) ){
            rm.setStatus(new Status(IStatus.ERROR, MIPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "No source director configured for given context", null)); //$NON-NLS-1$);
            rm.done();
            return;
        } 
        final CSourceLookupDirector director = fDirectors.get(sourceLookupCtx);
        
        new Job("Lookup Source") { //$NON-NLS-1$
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                Object[] sources;
                try {
                    sources = director.findSourceElements(debuggerPath);
                    if (sources == null || sources.length == 0) {
                        rm.setStatus(new Status(IStatus.ERROR, MIPlugin.PLUGIN_ID, IDsfService.REQUEST_FAILED, "No sources found", null)); //$NON-NLS-1$);
                    } else {
                        rm.setData(sources[0]);
                    }                
                } catch (CoreException e) {
                    rm.setStatus(new Status(IStatus.ERROR, MIPlugin.PLUGIN_ID, IDsfService.REQUEST_FAILED, "Source lookup failed", e)); //$NON-NLS-1$);
                } finally {
                    rm.done();
                }

                return Status.OK_STATUS;
            }
        }.schedule();       
    }
}
