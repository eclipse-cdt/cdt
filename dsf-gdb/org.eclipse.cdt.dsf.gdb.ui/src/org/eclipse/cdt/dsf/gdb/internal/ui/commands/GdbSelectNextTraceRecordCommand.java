/*******************************************************************************
 * Copyright (c) 2010, 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import java.util.Hashtable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.gdb.internal.commands.ISelectNextTraceRecordHandler;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.GDBTraceControl_7_2.TraceRecordSelectedChangedEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceStatusDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.AbstractDebugCommand;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;

/**
 * Command to select the next trace record
 * 
 * @since 2.1
 */
@SuppressWarnings("restriction")
public class GdbSelectNextTraceRecordCommand extends AbstractDebugCommand implements ISelectNextTraceRecordHandler {
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;
	private final DsfSession fSession;

	public GdbSelectNextTraceRecordCommand(DsfSession session) {
		fExecutor = session.getExecutor();
		fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
		fSession = session;
	}    

	public void dispose() {
		fTracker.dispose();
	}

	@Override
	protected void doExecute(Object[] targets, IProgressMonitor monitor, IRequest request) throws CoreException {
		if (targets.length != 1) {
			return;
		}

		final ITraceTargetDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext)targets[0]).getDMContext(), ITraceTargetDMContext.class);
		if (dmc == null) {
			return;
		}

      	Query<Object> selectRecordQuery = new Query<Object>() {
            @Override
            public void execute(final DataRequestMonitor<Object> rm) {
        		final IGDBTraceControl traceControl = fTracker.getService(IGDBTraceControl.class);

       			if (traceControl != null) {
       				traceControl.getCurrentTraceRecordContext(
       					    dmc,
       						new DataRequestMonitor<ITraceRecordDMContext>(fExecutor, rm) {
       							@Override
       							protected void handleSuccess() {
       								final ITraceRecordDMContext previousDmc = getData();
       							    ITraceRecordDMContext nextDmc = traceControl.createNextRecordContext(previousDmc);
       							    // Must send the event right away to tell the services we are starting visualization
       							    // If we don't, the services won't behave accordingly soon enough
       							    // Bug 347514
						            fSession.dispatchEvent(new TraceRecordSelectedChangedEvent(nextDmc), new Hashtable<String, String>());
						            
						            traceControl.selectTraceRecord(nextDmc, new ImmediateRequestMonitor(rm) {
						            	@Override
						            	protected void handleError() {
						            		// If we weren't able to select the next record, we must notify that we are still on the previous one
						            		// since we have already sent a TraceRecordSelectedChangedEvent early, but it didn't happen.
						            		fSession.dispatchEvent(new TraceRecordSelectedChangedEvent(previousDmc), new Hashtable<String, String>());
						            		rm.done();
						            	}
						            });
       							};
       						});
       			} else {
       				rm.done();
       			}
       		}
       	};
    	try {
    		fExecutor.execute(selectRecordQuery);
    		selectRecordQuery.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
        } catch (RejectedExecutionException e) {
        	// Can be thrown if the session is shutdown
        }
	}

	@Override
	protected boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request)
	throws CoreException 
	{
		if (targets.length != 1) {
			return false;
		}

		final ITraceTargetDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext)targets[0]).getDMContext(), ITraceTargetDMContext.class);
		if (dmc == null) {
			return false;
		}

        Query<Boolean> canSelectRecordQuery = new Query<Boolean>() {
        	@Override
        	public void execute(final DataRequestMonitor<Boolean> rm) {
        		IGDBTraceControl traceControl = fTracker.getService(IGDBTraceControl.class);

        		if (traceControl != null) {
        			traceControl.getTraceStatus(dmc, new DataRequestMonitor<ITraceStatusDMData>(fExecutor, rm) {
							@Override
   							protected void handleSuccess() {
	        					if (getData().getNumberOfCollectedFrame() > 0) {
	        						IGDBTraceControl traceControl = fTracker.getService(IGDBTraceControl.class);
	        						if (traceControl != null) {
	        							traceControl.isTracing(dmc, new DataRequestMonitor<Boolean>(fExecutor, rm) {
	        								@Override
	        								protected void handleSuccess() {
	        									rm.setData(!getData());
	        									rm.done();
	        								};
	        							});
	        						} else {
	        							rm.setData(false);
	        							rm.done();
	        						}
	        					} else {
	        						rm.setData(false);
	        						rm.done();
	        					}
	        				};
        			});
        		} else {
        			rm.setData(false);
        			rm.done();
        		}
        	}
        };
		try {
			fExecutor.execute(canSelectRecordQuery);
			return canSelectRecordQuery.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (RejectedExecutionException e) {
			// Can be thrown if the session is shutdown
		}

		return false;
	}

	@Override
	protected Object getTarget(Object element) {
		if (element instanceof IDMVMContext) {
			return element;
		}
		return null;
	}

	@Override
	protected boolean isRemainEnabled(IDebugCommandRequest request) {
		return true;
	}
}
