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
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.service.command.events.MITracepointSelectedEvent;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.events.MIEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.CLITraceDumpInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResultRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MITraceFindInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MITraceListVariablesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MITraceListVariablesInfo.MITraceVariableInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MITraceStatusInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MITraceStopInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.osgi.framework.BundleContext;

/**
 * This class implements the IGDBTraceControl interface which gives access
 * to the debugger's tracing functionality.
 * 
 * @since 3.0
 */
public class GDBTraceControl_7_2 extends AbstractDsfService implements IGDBTraceControl, ICachingService {

	@Immutable
	protected static final class MITraceRecordDMContext extends AbstractDMContext implements ITraceRecordDMContext {

		// The trace record GDB reference
		private final String fReference;

		/**
		 * @param session    the DsfSession for this service
		 * @param parents    the parent contexts
		 * @param reference  the trace record reference
		 * @since 4.0
		 */
		public MITraceRecordDMContext(DsfSession session, ITraceTargetDMContext parent, String reference) {
			super(session.getId(), new IDMContext[] { parent });
			fReference = reference;
		}

		/** @since 4.0 */
		@Override
		public String getRecordId() {
			return fReference;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.datamodel.AbstractDMContext#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			return baseEquals(obj) && (fReference == null ? ((MITraceRecordDMContext) obj).fReference == null : 
									  (fReference.equals(((MITraceRecordDMContext) obj).fReference)));
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.datamodel.AbstractDMContext#hashCode()
		 */
		@Override
		public int hashCode() {
			return baseHashCode() ^ (fReference == null ? 0 : fReference.hashCode());
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return baseToString() + ".reference(" + fReference + ")";  //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	/**
	 * Trace record context used to indicate that there is no current trace record selected.
	 */
	@Immutable
	protected static final class InvalidTraceRecordDMContext extends AbstractDMContext implements ITraceRecordDMContext {

		/**
		 * @param session    the DsfSession for this service
		 * @param parents    the parent contexts
		 * @param reference  the trace record reference
		 */
		public InvalidTraceRecordDMContext(DsfSession session, ITraceTargetDMContext parent) {
			super(session.getId(), new IDMContext[] { parent });
		}

		/** @since 4.0 */
		@Override
		public String getRecordId() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.datamodel.AbstractDMContext#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			return baseEquals(obj);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.datamodel.AbstractDMContext#hashCode()
		 */
		@Override
		public int hashCode() {
			return baseHashCode();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return baseToString() + ".noTraceRecord";  //$NON-NLS-1$
		}
	}

	private class TraceVariableDMData implements ITraceVariableDMData {
		private String fName;
		private String fValue;
		private String fInitialValue;
		
		public TraceVariableDMData(String name, String initial, String value) {
			fName = name;
			fInitialValue = initial;
			fValue = value;
		}
		
		@Override
		public String getName() {
			return fName;
		}

		@Override
		public String getValue() {
			return fValue;
		}

		@Override
		public String getInitialValue() {
			return fInitialValue;
		}
	}
	
	private class TraceRecordDMData implements ITraceRecordDMData {
		private String fContent;
		private String fTracepointNum;
		private String fTimestamp;
		private String fFrameNumber;
		
		public TraceRecordDMData(String content, String tracepointNum, String frameNumber, String timestamp) {
			fContent = content;
			fTracepointNum = tracepointNum;
			fTimestamp = timestamp;
			fFrameNumber = frameNumber;
		}

		@Override
		public String getContent() {
			return fContent;
		}

		@Override
		public String getTracepointNumber() {
			return fTracepointNum;
		}
		
		@Override
		public String getRecordId() {
			return fFrameNumber;
		}

		@Override
		public String getTimestamp() {
			return fTimestamp;
		}
	}
	
	private class TraceStatusDMData implements ITraceStatusDMData {
		private int fFreeBufferSize;
		private int fTotalBufferSize;
		private int fNumberOfCollectedFrames;
		private boolean fTracingActive;
		private boolean fTracingSupported;
		private STOP_REASON_ENUM fStopReason;
		private Integer fStoppingTracepoint;
		
		/**
		 * Create a status when tracing is supported
		 */
		public TraceStatusDMData(boolean active, int free, int total, int frames, 
				STOP_REASON_ENUM reason, Integer tracepoint) {
			fFreeBufferSize = free;
			fTotalBufferSize = total;
			fNumberOfCollectedFrames = frames;
			fTracingActive = active;
			fTracingSupported = true;
			fStopReason = reason;
			fStoppingTracepoint = tracepoint;
		}
		
		/**
		 * Status without a Stop reason
		 */
		public TraceStatusDMData(boolean active, int free, int total, int frames) {
			this(active, free, total, frames, null, null);
		}
		
		/**
		 * Create a status when tracing is not supported
		 */
		public TraceStatusDMData() {
			this(false, 0, 0, 0);
			fTracingSupported = false;
		}
		
		@Override
		public int getFreeBufferSize() {
			return fFreeBufferSize;
		}
		
		@Override
		public int getNumberOfCollectedFrame() {
			return fNumberOfCollectedFrames;
		}
		
		@Override
		public int getTotalBufferSize() {
			return fTotalBufferSize;
		}
		
		@Override
		public boolean isTracingActive() {
			return fTracingActive;
		}
		
		@Override
		public boolean isTracingSupported() {
			return fTracingSupported;
		}
		
		@Override
		public STOP_REASON_ENUM getStopReason() {
			return fStopReason;
		}
		
		@Override
		public Integer getStoppingTracepoint() {
			if (fStopReason == null) {
				return null;
			}
			return fStoppingTracepoint;
		}
		
		@SuppressWarnings("nls")
		@Override
		public String toString() {
			String str = "\n";
			
			if (!fTracingSupported) {
				return "\nTracing is not supported\n";
			}
			
	    	if (fBackend.getSessionType() == SessionType.CORE) {
	    		str += "Off-line trace visualization\n";
	    	} else {
	    		str += "Tracing with live execution\n";
	    	}
	    	
			if (fCurrentRecordDmc instanceof MITraceRecordDMContext) {
				str += "Looking at trace frame " + 
				             ((MITraceRecordDMContext)fCurrentRecordDmc).getRecordId() +
				             ", tracepoint " + fTracepointIndexForTraceRecord + "\n\n";
			} else {
				str += "Not currently looking at any trace frame\n\n";
			}
			
			str += "Tracing is currently" + (!fTracingActive ? " not":"") + " active\n";
			str += "Buffer contains " + fNumberOfCollectedFrames + " trace frame" + 
			       (fNumberOfCollectedFrames>1?"s":"") + "\n";//" (out of ? created in total)\n";
			str += "Currently using " + (fTotalBufferSize - fFreeBufferSize) + 
			       " bytes out of " + fTotalBufferSize + "\n";
			
			if (fStopReason != null) {
				assert !fTracingActive;
				str += "Tracing stopped because of";
				if (fStopReason == STOP_REASON_ENUM.REQUEST) {
					str += " user request";
				} else if (fStopReason == STOP_REASON_ENUM.PASSCOUNT) {
					str += " passcount";
					if (fStoppingTracepoint != null) {
						str += " of tracepoint number " + fStoppingTracepoint;
					}
				} else if (fStopReason == STOP_REASON_ENUM.OVERFLOW) {
					str += " buffer full";
				} else if (fStopReason == STOP_REASON_ENUM.DISCONNECTION) {
					str += " disconnection";
				} else if (fStopReason == STOP_REASON_ENUM.ERROR) {
					str += " error";
				} else {
					str += " unknow reason";
				}
				str += "\n";
			}
			


			return str;
		}
	}
	
	private static class TracingSupportedChangeEvent extends AbstractDMEvent<ITraceTargetDMContext>
	implements ITracingSupportedChangeDMEvent {
		private final boolean fTracingSupported;
		
        public TracingSupportedChangeEvent(ITraceTargetDMContext context, boolean supported) {
        	super(context);
        	fTracingSupported = supported;
        }

    	@Override
		public boolean isTracingSupported() {
			return fTracingSupported;
		}
	}

	private static class TracingStartedEvent extends AbstractDMEvent<ITraceTargetDMContext>
	implements ITracingStartedDMEvent {
        public TracingStartedEvent(ITraceTargetDMContext context) {
        	super(context);
        }
	}
	
	private static class TracingStoppedEvent extends AbstractDMEvent<ITraceTargetDMContext>
	implements ITracingStoppedDMEvent {
		public TracingStoppedEvent(ITraceTargetDMContext context) {
			super(context);
		}
	}
	
	public static class TraceRecordSelectedChangedEvent extends AbstractDMEvent<ITraceRecordDMContext>
	implements ITraceRecordSelectedChangedDMEvent {
		final boolean fVisualModeEnabled;
		
        public TraceRecordSelectedChangedEvent(ITraceRecordDMContext context) {
        	super(context);
        	fVisualModeEnabled = !(context instanceof InvalidTraceRecordDMContext);
        }

    	@Override
		public boolean isVisualizationModeEnabled() {
			return fVisualModeEnabled;
		}
	}	

    private CommandCache fTraceCache;
	private ICommandControlService fConnection;
	private CommandFactory fCommandFactory;
	private IGDBBackend fBackend;
	
	private ITraceRecordDMContext fCurrentRecordDmc;
	private int fTracepointIndexForTraceRecord;

	private boolean fIsTracingActive;
	private boolean fIsTracingCurrentlySupported;
	private boolean fIsTracingFeatureAvailable = true;
	private int fTraceRecordsStored;

	public GDBTraceControl_7_2(DsfSession session, ILaunchConfiguration config) {
		super(session);
	}

	/**
	 * This method initializes this service.
	 * 
	 * @param requestMonitor
	 *            The request monitor indicating the operation is finished
	 */
	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
			@Override
			protected void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}
	
	/**
	 * This method initializes this service after our superclass's initialize()
	 * method succeeds.
	 * 
	 * @param requestMonitor
	 *            The call-back object to notify when this service's
	 *            initialization is done.
	 */
	private void doInitialize(RequestMonitor requestMonitor) {
		// Register this service.
		register(new String[] {IGDBTraceControl.class.getName()},
				 new Hashtable<String, String>());
		

		fConnection = getServicesTracker().getService(ICommandControlService.class);
        fTraceCache = new CommandCache(getSession(), fConnection);
        fTraceCache.setContextAvailable(fConnection.getContext(), true);
        
        fBackend = getServicesTracker().getService(IGDBBackend.class);
        fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

		requestMonitor.done();
	}

	/**
	 * This method shuts down this service. It unregisters the service, stops
	 * receiving service events, and calls the superclass shutdown() method to
	 * finish the shutdown process.
	 * 
	 * @return void
	 */
	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		unregister();
		super.shutdown(requestMonitor);
	}
	
	/**
	 * @return The bundle context of the plug-in to which this service belongs.
	 */
	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

	@Override
    public void canStartTracing(ITraceTargetDMContext context, final DataRequestMonitor<Boolean> rm) {
    	if (context == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}
    	
    	if (fIsTracingCurrentlySupported == false) {
    		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Tracing not supported", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}
    	
    	if (fBackend.getSessionType() == SessionType.CORE) {
    		rm.setData(false);
    		rm.done();
    		return;
    	}
    	
    	if (fCurrentRecordDmc != null) {
    		// We are visualizing data, no more tracing possible.
    		rm.setData(false);
    		rm.done();
    		return;    		
    	}

 		rm.setData(true);
 		rm.done();
    }
    
	@Override
    public void startTracing(final ITraceTargetDMContext context, final RequestMonitor rm) {
    	if (context == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}

    	canStartTracing(context, new DataRequestMonitor<Boolean>(getExecutor(), rm) {
    		@Override
    		protected void handleSuccess() {
    			if (!getData()) {
    	            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Cannot start tracing", null)); //$NON-NLS-1$
    	            rm.done();
    	            return;
    			}
    			
    	        fConnection.queueCommand(
    	        		fCommandFactory.createMITraceStart(context),
    	        		new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
    	        			@Override
    	        			protected void handleSuccess() {
    	        				fIsTracingActive = true;
    	        		        getSession().dispatchEvent(new TracingStartedEvent(context), getProperties());
    	        				rm.done();
    	        			}
                            @Override
                            protected void handleError() {
                                // Send an event to cause a refresh of the button states
                                IDMEvent<ITraceTargetDMContext> event;
                                if (fIsTracingActive) {
                                    event = new TracingStartedEvent(context);
                                } else {
                                    event = new TracingStoppedEvent(context);
                                }
                                getSession().dispatchEvent(event, getProperties());
                                rm.done();
                            }
    	        		});
    		}
    	});
    }
    
	@Override
    public void canStopTracing(ITraceTargetDMContext context, DataRequestMonitor<Boolean> rm) {
    	if (context == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}
    	
    	if (fIsTracingCurrentlySupported == false) {
    		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Tracing not supported", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}

    	if (fBackend.getSessionType() == SessionType.CORE) {
    		rm.setData(false);
    		rm.done();
    		return;
    	}

    	if (fCurrentRecordDmc != null) {
    		// We are visualizing data, no more tracing possible.
    		rm.setData(false);
    		rm.done();
    		return;    		
    	}

       	isTracing(context, rm);
    }
    
	@Override
    public void stopTracing(final ITraceTargetDMContext context, final RequestMonitor rm) {
    	if (context == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}

    	canStopTracing(context, new DataRequestMonitor<Boolean>(getExecutor(), rm) {
    		@Override
    		protected void handleSuccess() {
    			if (!getData()) {
    	            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Cannot stop tracing", null)); //$NON-NLS-1$
    	            rm.done();
    	            return;
    			}
    			
    	        fConnection.queueCommand(
    	        		fCommandFactory.createMITraceStop(context),
    	        		new DataRequestMonitor<MITraceStopInfo>(getExecutor(), rm) {
    	        			@Override
    	        			protected void handleSuccess() {
    	        				MITraceStopInfo info = getData();

            					// Update the tracing state in case it was stopped by the backend
            					if (fIsTracingActive != info.isTracingActive()) {
            						fIsTracingActive = info.isTracingActive();
            						if (!fIsTracingActive) {
            	        		        getSession().dispatchEvent(new TracingStoppedEvent(context), getProperties());
            						}
            					}
            					
            					fTraceRecordsStored = info.getNumberOfCollectedFrame();
    	        				rm.done();
    	        			}
    	        		});
    		}
    	});    	
    }
    
	@Override
    public void isTracing(ITraceTargetDMContext context, final DataRequestMonitor<Boolean> rm) {
    	if (context == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}

    	if (fBackend.getSessionType() == SessionType.CORE) {
    		rm.setData(false);
    		rm.done();
    		return;
    	}

    	// Although tracing can be automatically stopped on the target, we
    	// don't go to the backend for this call, or we would make too many calls
    	// Instead, we can use our buffered state; we simply won't know about an
    	// automatic stop until a forced refresh.  (Note that the MI notification
    	// about automatic stops, is not available until GDB 7.2 is released)
    	rm.setData(fIsTracingActive);
    	rm.done();
    }
    
	@Override
	public void canSaveTraceData(ITraceTargetDMContext context, DataRequestMonitor<Boolean> rm) {
    	if (context == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}
    	
    	if (fIsTracingCurrentlySupported == false) {
    		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Tracing not supported", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}

    	if (fBackend.getSessionType() == SessionType.CORE) {
    		rm.setData(false);
    		rm.done();
    		return;
    	}

		rm.setData(fTraceRecordsStored > 0);
		rm.done();
	}

	@Override
	public void saveTraceData(final ITraceTargetDMContext context, final String file, 
			                  final boolean remoteSave, final RequestMonitor rm) {
    	if (context == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}

    	canSaveTraceData(context, new DataRequestMonitor<Boolean>(getExecutor(), rm) {
    		@Override
    		protected void handleSuccess() {
    			if (!getData()) {
    	            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Cannot save trace data", null)); //$NON-NLS-1$
    	            rm.done();
    	            return;
    			}

    			fConnection.queueCommand(
    					fCommandFactory.createMITraceSave(context, file, remoteSave),
    					new DataRequestMonitor<MIInfo>(getExecutor(), rm));
       		}
    	});
	}

	@Override
	public void canLoadTraceData(ITraceTargetDMContext context, DataRequestMonitor<Boolean> rm) {
    	if (context == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}

    	// If this service has been instantiated, it means we support loading trace data.
    	// Unlike the other operations, loading trace data does not require any GDB special state
    	// (like being connected to a target)
		rm.setData(true);
		rm.done();
	}

	@Override
	public void loadTraceData(final ITraceTargetDMContext context, final String file, final RequestMonitor rm) {
    	if (context == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}

    	canLoadTraceData(context, new DataRequestMonitor<Boolean>(getExecutor(), rm) {
    		@Override
    		protected void handleSuccess() {
    			if (!getData()) {
    	            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Cannot load trace data", null)); //$NON-NLS-1$
    	            rm.done();
    	            return;
    			}

    			fConnection.queueCommand(
    					fCommandFactory.createMITargetSelectTFile(context, file), 
    					new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
    						@Override
    						protected void handleSuccess() {
    							fIsTracingCurrentlySupported = true;
    							// Workaround for GDB pre-release where we don't get the details
    							// of the frame when we load a trace file.
    							// To get around this, we can force a select of record 0
    							final ITraceRecordDMContext initialRecord = createTraceRecordContext(context, "0"); //$NON-NLS-1$
    							selectTraceRecord(initialRecord, new ImmediateRequestMonitor(rm) {
    							    @Override
    							    protected void handleSuccess() {
    							        // This event will indicate to the other services that we are visualizing trace data.
    							        getSession().dispatchEvent(new TraceRecordSelectedChangedEvent(initialRecord), getProperties());
    							        rm.done();
    							    }
    							});
    						}
    					});
    		}
    	});
	}
	
	@Override
    public void getTraceStatus(final ITraceTargetDMContext context, final DataRequestMonitor<ITraceStatusDMData> rm) {
    	if (context == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}

    	if (fIsTracingFeatureAvailable == false) {
    		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Tracing not supported", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}

        fConnection.queueCommand(
        		fCommandFactory.createMITraceStatus(context),
        		new DataRequestMonitor<MITraceStatusInfo>(getExecutor(), rm) {
        			@Override
        			protected void handleError() {
        				// The MI command
        				fIsTracingFeatureAvailable = false;
        				super.handleError();
        			}
        			@Override
        			protected void handleSuccess() {
        				MITraceStatusInfo info = getData();
        				
        				if (fIsTracingCurrentlySupported != info.isTracingSupported()) {
        					fIsTracingCurrentlySupported = info.isTracingSupported();
	        		        getSession().dispatchEvent(new TracingSupportedChangeEvent(context, fIsTracingCurrentlySupported), getProperties());
        				}
        				
        				if (fIsTracingCurrentlySupported) {
        					// Update the tracing state in case it was stopped by the backend
        					if (fIsTracingActive != info.isTracingActive()) {
        						fIsTracingActive = info.isTracingActive();
        						if (fIsTracingActive) {
        	        		        getSession().dispatchEvent(new TracingStartedEvent(context), getProperties());
        						} else {
        	        		        getSession().dispatchEvent(new TracingStoppedEvent(context), getProperties());
        						}
        					}
        					
        					fTraceRecordsStored = info.getNumberOfCollectedFrame();
        					
        					STOP_REASON_ENUM stopReason = info.getStopReason();
        					if (stopReason == null) {
        						rm.setData(new TraceStatusDMData(info.isTracingActive(), 
        								                         info.getFreeBufferSize(), 
        														 info.getTotalBufferSize(),
        														 info.getNumberOfCollectedFrame()));
        					} else {
        						rm.setData(new TraceStatusDMData(info.isTracingActive(), 
        								                         info.getFreeBufferSize(), 
										                         info.getTotalBufferSize(), 
										                         info.getNumberOfCollectedFrame(),
										                         stopReason, 
										                         info.getStopTracepoint()));
        					}
        				} else {
        					fTraceRecordsStored = 0;
        					fIsTracingActive = false;
        					rm.setData(new TraceStatusDMData());        					
        				}
        				rm.done();
        			}
        		});
    }
    
	@Override
	public void createTraceVariable(ITraceTargetDMContext context,
									String varName, 
									String varValue,
									RequestMonitor rm) {
    	if (context == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}

    	if (fIsTracingCurrentlySupported == false) {
    		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Tracing not supported", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}

		if (varValue == null) {
			fConnection.queueCommand(
					fCommandFactory.createMITraceDefineVariable(context, varName),
					new DataRequestMonitor<MIInfo>(getExecutor(), rm));			
		} else {
			fConnection.queueCommand(
					fCommandFactory.createMITraceDefineVariable(context, varName, varValue),
					new DataRequestMonitor<MIInfo>(getExecutor(), rm));
		}
	}
	

	@Override
    public void getTraceVariables(ITraceTargetDMContext context, final DataRequestMonitor<ITraceVariableDMData[]> rm) {
    	if (context == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}

    	if (fIsTracingCurrentlySupported == false) {
    		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Tracing not supported", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}

    	// It may be possible to cache this call, if we can figure out that all the cases
    	// where to data can change, to clear the cache in those cases
		fConnection.queueCommand(
				fCommandFactory.createMITraceListVariables(context),
				new DataRequestMonitor<MITraceListVariablesInfo>(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						MITraceVariableInfo[] vars = getData().getTraceVariables();
						TraceVariableDMData[] varDataArray = new TraceVariableDMData[vars.length];
						for (int i = 0; i < vars.length; i++) {
							varDataArray[i] = new TraceVariableDMData(vars[i].getName(),
									                                  vars[i].getInitialValue(),
									                                  vars[i].getCurrentValue());
						}
						
						rm.setData(varDataArray);
						rm.done();
					}
				});
	}
    
    /**
     * Create a trace record context
     * @since 4.0
     */
	@Override
    public ITraceRecordDMContext createTraceRecordContext(ITraceTargetDMContext ctx, String recordId) {
    	return new MITraceRecordDMContext(getSession(), ctx, recordId);
    }

	@Override
    public ITraceRecordDMContext createNextRecordContext(ITraceRecordDMContext ctx) {
    	ITraceTargetDMContext targetDmc = DMContexts.getAncestorOfType(ctx, ITraceTargetDMContext.class);
    	if (ctx instanceof InvalidTraceRecordDMContext) {
    		// No specified context, so we return the context for the first
    		return createTraceRecordContext(targetDmc, "0"); //$NON-NLS-1$
    	}
    	if (ctx instanceof MITraceRecordDMContext) {
        	String recordId = ((MITraceRecordDMContext)ctx).getRecordId();
        	int recordIndex = Integer.parseInt(recordId);
        	recordIndex++;
        	if (recordIndex == fTraceRecordsStored) {
        		// Loop back to the front
        		recordIndex = 0;
        	}
    		return new MITraceRecordDMContext(getSession(),
    				                          targetDmc,
    				                          Integer.toString(recordIndex));
    	}
    	return null;
    }

	@Override
    public ITraceRecordDMContext createPrevRecordContext(ITraceRecordDMContext ctx) {
    	if (ctx instanceof MITraceRecordDMContext) {
        	ITraceTargetDMContext targetDmc = DMContexts.getAncestorOfType(ctx, ITraceTargetDMContext.class);
        	String recordId = ((MITraceRecordDMContext)ctx).getRecordId();
        	int recordIndex = Integer.parseInt(recordId);
        	if (recordIndex == 0) {
        		// Loop back to the end
        		recordIndex = fTraceRecordsStored; // The last index of a trace record (zero-based)
        	}
        	recordIndex--;
    		return new MITraceRecordDMContext(getSession(),
    				                          targetDmc,
    				                          Integer.toString(recordIndex));
    	}
    	return null;
    }


	@Override
    public void getCurrentTraceRecordContext(ITraceTargetDMContext context, DataRequestMonitor<ITraceRecordDMContext> drm) {
    	if (fCurrentRecordDmc == null) {
    		drm.setData(new InvalidTraceRecordDMContext(getSession(), context));
    	} else {
    		drm.setData(fCurrentRecordDmc);
    	}
    	drm.done();
    }

	@Override
    public void selectTraceRecord(final ITraceRecordDMContext context, final RequestMonitor rm) {
    	if (fIsTracingCurrentlySupported == false) {
    		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Tracing not supported", null)); //$NON-NLS-1$
            rm.done();
            return;
    	}

    	if (context instanceof MITraceRecordDMContext) {
    		ITraceTargetDMContext targetDmc = DMContexts.getAncestorOfType(context, ITraceTargetDMContext.class);
        	String recordId = ((MITraceRecordDMContext)context).getRecordId();
        	final int reference = Integer.parseInt(recordId);

    		if (reference < 0) {
    			// This is how we indicate that we want to exit visualization mode
    			// We should have a specific call in the IGDBTraceControl interface to do this.
    			stopVisualizingTraceData(targetDmc, rm);
        		return;
    		}
    		
    		fConnection.queueCommand(
    				fCommandFactory.createMITraceFindFrameNumber(targetDmc, reference),
    				new DataRequestMonitor<MITraceFindInfo>(getExecutor(), rm) {
    					@Override
    					protected void handleSuccess() {
    						if (getData().isFound() == false) {
    							rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Could not find trace record", null)); //$NON-NLS-1$
    							rm.done();
    							return;
    						}

    						fCurrentRecordDmc = context;
    						fTracepointIndexForTraceRecord = getData().getTraceRecord().getTracepointId();

    						// We could rely on the TraceRecordSelectedChangedEvent to update all the views, but this
    						// would require a lot of changes.
    						// Notice that looking at a new trace record should behave in the same manner
    						// as when the debugger suspends during normal execution; therefore we can simply 
    						// trigger an MIStoppedEvent, as if reported by GDB.  Note that we do this already for
    						// cases where GDB is missing such a event (like older versions of GDB when using a CLI command)
    						IMIProcesses procService = getServicesTracker().getService(IMIProcesses.class);
    						if (procService == null) {
    							rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Could not find necessary services", null)); //$NON-NLS-1$
    							rm.done();
    							return;
    						}
    						
    						final MIResultRecord rr = getData().getMIOutput().getMIResultRecord();
    						if (rr == null) {
    							assert false;
    							rm.done();
    							return;
    						}
    						
    						// First find the process we are using.
    						ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(context, ICommandControlDMContext.class);
    						procService.getProcessesBeingDebugged(
    								controlDmc,
    								new DataRequestMonitor<IDMContext[]>(getExecutor(), rm) {
    									@Override
    									protected void handleSuccess() {
    										assert getData() != null;
    										assert getData().length == 1;

    										if (getData() == null || getData().length < 1) {
    			    							rm.done();
    			    							return;
    										}
    										
    										// Choose the first process for now, until gdb can tell
    										// us which process the trace record is associated with.
    										// Or maybe GDB already tells us by only reporting a single
    										// process?
    										IContainerDMContext processContainerDmc = (IContainerDMContext)(getData()[0]);
    										
    										// Now find the proper thread.  We must do this here because in post-mortem debugging
    										// we cannot rely on MIRunControl using 'thread', as it will fail
    			    						IMIProcesses procService = getServicesTracker().getService(IMIProcesses.class);
    			    						if (procService == null) {
    			    							rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Could not find necessary services", null)); //$NON-NLS-1$
    			    							rm.done();
    			    							return;
    			    						}

    			    						procService.getProcessesBeingDebugged(
    			    								processContainerDmc,
    			    								new DataRequestMonitor<IDMContext[]>(getExecutor(), rm) {
    			    									@Override
    			    									protected void handleSuccess() {
    			    										assert getData() != null;
    			    										assert getData().length == 1;

    			    										if (getData() == null || getData().length < 1) {
    			    			    							rm.done();
    			    			    							return;
    			    										}

    			    										IExecutionDMContext execDmc = (IExecutionDMContext)getData()[0];
    			    										MIEvent<?> event = MITracepointSelectedEvent.parse(execDmc, rr.getToken(), rr.getMIResults());
    			    										getSession().dispatchEvent(event, getProperties());

    			    										rm.done();
    			    									}
    			    								});
    									}
    								});
    					}
    				});
    	} else {
    		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid trace record context.", null)); //$NON-NLS-1$
    		rm.done();
    	}
    }

    private void stopVisualizingTraceData(final ITraceTargetDMContext context, final RequestMonitor rm) {
    	if (fIsTracingCurrentlySupported == false) {
    		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Tracing not supported", null)); //$NON-NLS-1$
    		rm.done();
    		return;
    	}

    	if (fBackend.getSessionType() == SessionType.CORE) {
    		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Cannot stop visualizing for a post mortem session", null)); //$NON-NLS-1$
    		rm.done();
    		return;
    	}

    	fConnection.queueCommand(
    			fCommandFactory.createMITraceFindNone(context),
    			new DataRequestMonitor<MITraceFindInfo>(getExecutor(), rm) {
    				@Override
    				protected void handleSuccess() {
    					assert getData().isFound() == false;
    					fCurrentRecordDmc = null;
    					// This event will indicate to the other services that we are no longer visualizing trace data.
    					ITraceRecordDMContext invalidDmc = new InvalidTraceRecordDMContext(getSession(), context);
    					getSession().dispatchEvent(new TraceRecordSelectedChangedEvent(invalidDmc), getProperties());


    					rm.done();
    					return;
    				}
    			});
    }
    
	@Override
	public void getTraceRecordData(final ITraceRecordDMContext context, final DataRequestMonitor<ITraceRecordDMData> rm) {
    	if (context instanceof MITraceRecordDMContext) {
    		
    		RequestMonitor tdumpRm = new ImmediateRequestMonitor(rm) {
				@Override
				protected void handleSuccess() {
					fConnection.queueCommand(
							fCommandFactory.createCLITraceDump(context),
							new DataRequestMonitor<CLITraceDumpInfo>(getExecutor(), rm) {
								@Override
								protected void handleSuccess() {

									TraceRecordDMData data = new TraceRecordDMData(
											getData().getContent(),
											getData().getTracepointNumber(),
											getData().getFrameNumber(),
											getData().getTimestamp()
									);
									rm.setData(data);
									rm.done();
								}
							});
				}
			};
			
    		// If we are pointing to the right context, we can do the tdump right away,
    		// if not, we should first select the record.
    		// This is because 'tdump' does not take any parameters to specify
    		// which record we want to dump.
    		if (fCurrentRecordDmc.equals(context)) {
    			tdumpRm.done();
    		} else {
    			selectTraceRecord(context, tdumpRm); 
    		}
    	} else {
    		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid trace record context.", null)); //$NON-NLS-1$
    		rm.done();
    	}
	}
	
	@Override
	public void flushCache(IDMContext context) {
        fTraceCache.reset(context);
	}
}
