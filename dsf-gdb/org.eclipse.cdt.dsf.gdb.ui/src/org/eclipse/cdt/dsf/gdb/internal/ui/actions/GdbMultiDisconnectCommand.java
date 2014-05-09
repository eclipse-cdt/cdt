/*******************************************************************************
 * Copyright (c) 2014 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfCommandRunnable;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IMultiDetach;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;

public class GdbMultiDisconnectCommand extends GdbDisconnectCommand {

	private class DummyDMVMContext implements IDMVMContext {

		final private IDMContext fContext;

		private DummyDMVMContext(IDMContext context) {
			super();
			fContext = context;
		}

		@Override
		public IVMNode getVMNode() {
			return null;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Object getAdapter(Class adapter) {
			return null;
		}

		@Override
		public IDMContext getDMContext() {
			return fContext;
		}
	}

	private class DebugCommandRequestWrapper implements IDebugCommandRequest {

		final private IDebugCommandRequest fRequest;
		final private Object[] fElements;

		private DebugCommandRequestWrapper(IDebugCommandRequest request, Object[] elements) {
			super();
			fRequest = request;
			fElements = elements;
		}

		@Override
		public void setStatus(IStatus status) {
			fRequest.setStatus(status);
		}

		@Override
		public IStatus getStatus() {
			return fRequest.getStatus();
		}

		@Override
		public void done() {
			fRequest.done();
		}

		@Override
		public void cancel() {
			fRequest.cancel();
		}

		@Override
		public boolean isCanceled() {
			return false;
		}

		@Override
		public Object[] getElements() {
			return fElements;
		}
		
		IDebugCommandRequest getOriginalRequest() {
			return fRequest;
		}
	}
	
	private class EnabledStateRequestWrapper extends DebugCommandRequestWrapper implements IEnabledStateRequest {

		private EnabledStateRequestWrapper(IEnabledStateRequest request, Object[] elements) {
			super(request, elements);
		}

		@Override
		public void setEnabled(boolean result) {
			((IEnabledStateRequest)getOriginalRequest()).setEnabled(result);
		}
	}

	private final String fSessionId;
    private final DsfExecutor fExecutor;
    private final DsfServicesTracker fTracker;

	public GdbMultiDisconnectCommand(DsfSession session) {
		super(session);
    	fSessionId = session.getId();
        fExecutor = session.getExecutor();
        fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
	}

    @Override
	public void dispose() {
        fTracker.dispose();
    }

    @Override
    public void canExecute(final IEnabledStateRequest request) {
        if (request.getElements().length == 0) {
            request.setEnabled(false);
            request.done();
            return;
        }

        try {
	        fExecutor.execute(new Runnable() {
				
				@Override
				public void run() {
					modifyEnabledStateRequest(
						request, 
						new DataRequestMonitor<IEnabledStateRequest>(fExecutor, null) {
							@Override
							protected void handleCompleted() {
								if (isSuccess()) {
									final IMultiDetach multiDetach = fTracker.getService(IMultiDetach.class);
									if (getData().getElements().length == 0) {
					                	request.setEnabled(false);
										request.done();
										return;
									}
	
									if (multiDetach == null || getData().getElements().length == 1) {
										GdbMultiDisconnectCommand.super.canExecute(getData());
									}
									else {
										ImmediateExecutor.getInstance().execute(new DsfCommandRunnable(fTracker, getData().getElements(), getData()) { 
								            @Override 
								            public void doExecute() {
						                		multiDetach.canDetachDebuggerFromSomeProcesses(
						                			getContexts(), 
						                			new DataRequestMonitor<Boolean>(ImmediateExecutor.getInstance(), null) {
						                				@Override
														protected void handleCompleted() {
						                					request.setEnabled(isSuccess() ? getData() : false);
						                					request.done();
						                				}
						                				
						                			});
								            }
				                		});
					                } 
								}
								else {
									request.setEnabled(false);
									request.done();
								}
							}
						});
				}
			});
        }
        catch (RejectedExecutionException e) {
        	request.setEnabled(false);
        	request.done();
        }
	}

    @Override
	public boolean execute(final IDebugCommandRequest request) {
        if (request.getElements().length == 0) {
            request.done();
            return false;
        }

        try {
	        fExecutor.execute(new Runnable() {
				
				@Override
				public void run() {
					modifyDebugCommandRequest(
						request, 
						new DataRequestMonitor<IDebugCommandRequest>(fExecutor, null) {
							@Override
							protected void handleCompleted() {
								if (isSuccess()) {
									if (isSuccess()) {
										final IMultiDetach multiDetach = fTracker.getService(IMultiDetach.class);
										if (getData().getElements().length == 0) {
											request.done();
											return;
										}
	
										if (multiDetach == null || getData().getElements().length == 1) {
											GdbMultiDisconnectCommand.super.execute(getData());
										}
										else {
											ImmediateExecutor.getInstance().execute(new DsfCommandRunnable(fTracker, getData().getElements(), getData()) { 
									            @Override 
									            public void doExecute() {
							                		multiDetach.detachDebuggerFromProcesses(
							                			getContexts(), 
							                			new DataRequestMonitor<Boolean>(ImmediateExecutor.getInstance(), null) {
							                				@Override
															protected void handleCompleted() {
							                					request.done();
							                				}
							                				
							                			});
									            }
					                		});
						                } 
									}
									else {
										request.done();
									}
								}
							}
						});
				}
	        });
        }
        catch (RejectedExecutionException e) {
        	request.done();
        }
		return false;
	}    

    /**
     * {@link DsfCommandRunnable} works only with requests who's elements are instances of 
     * {@link IDMVMContext}. To support "Disconnect" for {@link GdbLaunch} types we do the following:
     * 1. find all {@link IContainerDMContext} elements of the given launch
     * 2. create {@link DummyDMVMContext} elements for each container
     * 3. create a wrapper around the original request that has dummy objects created 
     *    at the previous step as its elements
     * The resulting wrapper object is passed to {@link DsfCommandRunnable} instead 
     * of the original request.
     *  
     * This method processes {@link IDebugCommandRequest} request types.
     */
    private void modifyDebugCommandRequest(final IDebugCommandRequest request, final DataRequestMonitor<IDebugCommandRequest> drm) {
    	GdbLaunch launch = null;
    	for (Object obj : request.getElements()) {
    		if (obj instanceof GdbLaunch && ((GdbLaunch)obj).getSession().getId().equals(fSessionId)) {
    			launch = (GdbLaunch)obj;
    			break;
    		}
    	}
    	if (launch != null) {
	    	IProcesses procService = fTracker.getService(IProcesses.class);
	    	ICommandControlService controlService = fTracker.getService(ICommandControlService.class);
	    	if (procService == null || controlService == null) {
	    		drm.setData(request);
	    		drm.done();
	    		return;
	    	}
	    	
	    	procService.getProcessesBeingDebugged(
	    		controlService.getContext(), 
	    		new DataRequestMonitor<IDMContext[]>(ImmediateExecutor.getInstance(), drm) {
	    			@Override
	    			protected void handleSuccess() {
	    				List<IDMVMContext> list = new ArrayList<IDMVMContext>(getData().length);
	    				for (IDMContext c : getData()) {
	    					if (c instanceof IContainerDMContext) {
	    						list.add(new DummyDMVMContext(c));
	    					}
	    				}
    					drm.setData(new DebugCommandRequestWrapper(request, list.toArray(new IDMVMContext[list.size()])));
	    				drm.done();
	    			}
	    		});
    	}
    	else {
    		drm.setData(request);
    		drm.done();
     	}
    }

    /**
     * {@link DsfCommandRunnable} works only with requests who's elements are instances of 
     * {@link IDMVMContext}. To support "Disconnect" for {@link GdbLaunch} types we do the following:
     * 1. find all {@link IContainerDMContext} elements of the given launch
     * 2. create {@link DummyDMVMContext} elements for each container
     * 3. create a wrapper around the original request that has dummy objects created 
     *    at the previous step as its elements
     * The resulting wrapper object is passed to {@link DsfCommandRunnable} instead 
     * of the original request.
     *  
     * This method processes {@link IEnabledStateRequest} request types.
     */
    private void modifyEnabledStateRequest(final IEnabledStateRequest request, final DataRequestMonitor<IEnabledStateRequest> drm) {
    	GdbLaunch launch = null;
    	for (Object obj : request.getElements()) {
    		if (obj instanceof GdbLaunch && ((GdbLaunch)obj).getSession().getId().equals(fSessionId)) {
    			launch = (GdbLaunch)obj;
    			break;
    		}
    	}
    	if (launch != null) {
	    	IProcesses procService = fTracker.getService(IProcesses.class);
	    	ICommandControlService controlService = fTracker.getService(ICommandControlService.class);
	    	if (procService == null || controlService == null) {
	    		drm.setData(request);
	    		drm.done();
	    		return;
	    	}
    	
	    	procService.getProcessesBeingDebugged(
	    		controlService.getContext(), 
	    		new DataRequestMonitor<IDMContext[]>(ImmediateExecutor.getInstance(), drm) {
	    			@Override
	    			protected void handleSuccess() {
	    				List<IDMVMContext> list = new ArrayList<IDMVMContext>(getData().length);
	    				for (IDMContext c : getData()) {
	    					if (c instanceof IContainerDMContext) {
	    						list.add(new DummyDMVMContext(c));
	    					}
	    				}
    					drm.setData(new EnabledStateRequestWrapper(request, list.toArray(new IDMVMContext[list.size()])));
	    				drm.done();
	    			}
	    		});
    	}
    	else {
    		drm.setData(request);
    		drm.done();
     	}
    }
}
