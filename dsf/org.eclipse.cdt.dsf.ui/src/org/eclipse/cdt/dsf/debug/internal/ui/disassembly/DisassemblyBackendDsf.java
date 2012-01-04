/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Freescale Semiconductor - refactoring
 *     Patrick Chuong (Texas Instruments) - Bug 323279
 *     Patrick Chuong (Texas Instruments) - Bug 329682
 *     Patrick Chuong (Texas Instruments) - Bug 328168
 *     Patrick Chuong (Texas Instruments) - Bug 353351
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import static org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyUtils.DEBUG;
import static org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyUtils.internalError;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AbstractDisassemblyBackend;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyUtils;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.ErrorPosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IDisassembly;
import org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.cdt.dsf.debug.service.IDisassembly2;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMAddress;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMLocation;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IInstruction;
import org.eclipse.cdt.dsf.debug.service.IInstructionWithSize;
import org.eclipse.cdt.dsf.debug.service.IMixedInstruction;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.DsfSession.SessionEndedListener;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;

public class DisassemblyBackendDsf extends AbstractDisassemblyBackend implements SessionEndedListener {

	private volatile IExecutionDMContext fTargetContext;
	private DsfServicesTracker fServicesTracker;
	private IFrameDMContext fTargetFrameContext;
	protected IFrameDMData fTargetFrameData;

	private String fDsfSessionId;

	/**
	 * Constructor
	 */
	public DisassemblyBackendDsf() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#init(org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback)
	 */
	@Override
	public void init(IDisassemblyPartCallback callback) {
		super.init(callback);
		DsfSession.addSessionEndedListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#dispose()
	 */
	@Override
	public void dispose() {
		DsfSession.removeSessionEndedListener(this);
	}

    public static boolean supportsDebugContext_(IAdaptable context) {
        IDMVMContext dmvmContext = (IDMVMContext) context.getAdapter(IDMVMContext.class);
        return dmvmContext != null && hasDisassemblyService(dmvmContext.getDMContext());
    }
    
    private static boolean hasDisassemblyService(final IDMContext dmContext) {
        DsfSession session = DsfSession.getSession(dmContext.getSessionId());
        if (session == null || !session.isActive()) {
            return false;
        }
        if (session.getExecutor().isInExecutorThread()) {
            DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), session.getId());
            IDisassembly disassSvc = tracker.getService(IDisassembly.class);
            tracker.dispose();
            return disassSvc != null;
        }
        Query<Boolean> query = new Query<Boolean>() {
            @Override
            protected void execute(DataRequestMonitor<Boolean> rm) {
                try {
                    rm.setData(hasDisassemblyService(dmContext));
                } finally {
                    rm.done();
                }
            }
        };
        try {
            session.getExecutor().execute(query);
            Boolean result = query.get(1, TimeUnit.SECONDS);
            return result != null && result.booleanValue();
        } catch (Exception exc) {
            // ignored on purpose
        }
        return false;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#supportsDebugContext(org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	public boolean supportsDebugContext(IAdaptable context) {
		return supportsDebugContext_(context);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#hasDebugContext()
	 */
	@Override
	public boolean hasDebugContext() {
		return fTargetContext != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#setDebugContext(org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	public SetDebugContextResult setDebugContext(IAdaptable context) {
		assert supportsDebugContext(context) : "caller should not have invoked us"; //$NON-NLS-1$
		IDMVMContext vmContext = (IDMVMContext) context.getAdapter(IDMVMContext.class);
		IDMContext dmContext = vmContext.getDMContext();
		
		SetDebugContextResult result = new SetDebugContextResult();
		
		String dsfSessionId = dmContext.getSessionId();
		
		if (!dsfSessionId.equals(fDsfSessionId)) {
			// switch to different session or initiate session
			if (DEBUG) System.out.println("DisassemblyBackendDsf() " + dsfSessionId); //$NON-NLS-1$
			fTargetContext= null;
			fTargetFrameContext = null;
			result.contextChanged = true;

			if (dmContext instanceof IFrameDMContext) {
				IFrameDMContext frame= (IFrameDMContext) dmContext;
				IExecutionDMContext executionContext= DMContexts.getAncestorOfType(frame, IExecutionDMContext.class);
				if (executionContext != null) {
					fTargetContext= executionContext;
					fTargetFrameContext= frame;
				}
			}
			if (fTargetContext != null) {
				
				// remove ourselves as a listener with the previous session (context)
		        if (fDsfSessionId != null) {
		    		final DsfSession prevSession = DsfSession.getSession(fDsfSessionId);
		        	if (prevSession != null) {
		        		try {
		        			prevSession.getExecutor().execute(new DsfRunnable() {
		        				@Override
								public void run() {
		        					prevSession.removeServiceEventListener(DisassemblyBackendDsf.this);
		        				}
		        			});
		        		} catch (RejectedExecutionException e) {
		                    // Session is shut down.
		        		}
					}
		        }

	        	result.sessionId = fDsfSessionId = dsfSessionId;
				if (fServicesTracker != null) {
					fServicesTracker.dispose();
				}
		        fServicesTracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), fDsfSessionId);
		        
				// add ourselves as a listener with the new session (context)
	    		final DsfSession newSession = DsfSession.getSession(dsfSessionId);
	        	if (newSession != null) {
	        		try {
	        			newSession.getExecutor().execute(new DsfRunnable() {
	        				@Override
							public void run() {
	        					newSession.addServiceEventListener(DisassemblyBackendDsf.this, null);
	        				}
	        			});
	        		} catch (RejectedExecutionException e) {
	                    // Session is shut down.
	        		}
				}
			}
		} else if (dmContext instanceof IFrameDMContext) {
			result.sessionId = fDsfSessionId;
			// switch to different frame
			IFrameDMContext frame= (IFrameDMContext) dmContext;
			IExecutionDMContext newExeDmc = DMContexts.getAncestorOfType(frame, IExecutionDMContext.class);
			if (newExeDmc != null) {
				IDisassemblyDMContext newDisDmc = DMContexts.getAncestorOfType(newExeDmc, IDisassemblyDMContext.class);
				IDisassemblyDMContext oldDisDmc = DMContexts.getAncestorOfType(fTargetContext, IDisassemblyDMContext.class);
				result.contextChanged = !newDisDmc.equals(oldDisDmc);
				fTargetContext= newExeDmc;
				fTargetFrameContext= frame;
				if (!result.contextChanged) {
					fCallback.gotoFrameIfActive(frame.getLevel());
				}
			} else {			
				fTargetContext = null;
				fTargetFrameContext = null;
				result.contextChanged = true;
			}
		} else {			
			fTargetContext = null;
			fTargetFrameContext = null;
			result.contextChanged = true;
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#clearDebugContext()
	 */
	@Override
	public void clearDebugContext() {
		final DsfSession session = DsfSession.getSession(fDsfSessionId);
		if (session != null) {
			try {
				session.getExecutor().execute(new DsfRunnable() {
					@Override
					public void run() {
						session.removeServiceEventListener(DisassemblyBackendDsf.this);
					}
				});
    		} catch (RejectedExecutionException e) {
                // Session is shut down.
    		}
		}
		fTargetContext= null;
		if (fServicesTracker != null) {
			fServicesTracker.dispose();				
			fServicesTracker= null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#retrieveFrameAddress(int)
	 */
	@Override
	public void retrieveFrameAddress(final int frame) {
		final DsfExecutor executor= getSession().getExecutor();
		executor.execute(new DsfRunnable() {
			@Override
			public void run() {
				retrieveFrameAddressInSessionThread(frame);
			}});
	}

	void retrieveFrameAddressInSessionThread(final int frame) {
		final IStack stack= fServicesTracker.getService(IStack.class);
		final DsfExecutor executor= getSession().getExecutor();

		// Our frame context is currently either un-set or it's set to the frame
		// our caller is specifying. If un-set, then set it and reinvoke this
		// method.
		if (fTargetFrameContext == null) {
			if (frame == 0) {
				stack.getTopFrame(fTargetContext, new DataRequestMonitor<IFrameDMContext>(executor, null) {
					@Override
					protected void handleCompleted() {
						fTargetFrameContext= getData();
						if (fTargetFrameContext != null) {
							retrieveFrameAddressInSessionThread(frame);
						} else {
						    fCallback.setUpdatePending(false);
						}
					}
				});
			} else {
				// TODO retrieve other stack frame
	            fCallback.setUpdatePending(false);
			}
			return;
		}
		else if (frame != fTargetFrameContext.getLevel()) {
		    // frame context has changed in the meantime - reinvoke
            retrieveFrameAddressInSessionThread(fTargetFrameContext.getLevel());
			return;
		}
		
		stack.getFrameData(fTargetFrameContext, new DataRequestMonitor<IFrameDMData>(executor, null) {
			@Override
			protected void handleCompleted() {
				fCallback.setUpdatePending(false);
				IFrameDMData frameData= getData();
				fTargetFrameData= frameData;
				if (!isCanceled() && frameData != null) {
					final IAddress address= frameData.getAddress();
					final BigInteger addressValue= address.getValue();
					if (DEBUG) System.out.println("retrieveFrameAddress done "+ DisassemblyUtils.getAddressText(addressValue)); //$NON-NLS-1$
					fCallback.asyncExec(new Runnable() {
						@Override
						public void run() {
							if (address.getSize() * 8 != fCallback.getAddressSize()) {
								fCallback.addressSizeChanged(address.getSize() * 8);
							}
							if (frame == 0) {
								fCallback.updatePC(addressValue);
							} else {
								fCallback.gotoFrame(frame, addressValue);
							}
						}
					});
				} else {
					final IStatus status= getStatus();
					if (status != null && !status.isOK()) {
						DisassemblyBackendDsf.this.handleError(getStatus());
					}
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#isSuspended()
	 */
	@Override
	public boolean isSuspended() {
		DsfSession session = getSession();
		if (session == null || !session.isActive()) {
			return false;
		}
		if (session.getExecutor().isInExecutorThread()) {
			IRunControl runControl = getRunControl();
			if (runControl == null) {
				return false;
			} else {
				return runControl.isSuspended(fTargetContext);
			}
		}
		Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				try {
					IRunControl runControl = getRunControl();
					if (runControl == null) {
						rm.setData(false);
					} else {
						rm.setData(runControl.isSuspended(fTargetContext));
					}
				} finally {
					rm.done();
				}
			}
		};
		session.getExecutor().execute(query);
		try {
			return query.get();
		} catch (InterruptedException exc) {
		} catch (ExecutionException exc) {
		}
		
		return false;
	}

	private DsfSession getSession() {
		return DsfSession.getSession(fDsfSessionId);
	}

	private IRunControl getRunControl() {
		return getService(IRunControl.class);
	}
	
	private <V> V getService(Class<V> serviceClass) {
		if (fServicesTracker != null) {
			return fServicesTracker.getService(serviceClass);
		}
		return null;
	}
	


	@DsfServiceEventHandler
	public void handleEvent(IExitedDMEvent event) {
		if (fTargetContext == null) {
			return;
		}
		final IExecutionDMContext context= event.getDMContext();
		if (context.equals(fTargetContext)
				|| DMContexts.isAncestorOf(fTargetContext, context)) {
			fCallback.asyncExec(new Runnable() {
				@Override
				public void run() {
					fCallback.handleTargetEnded();
				}});
		}
	}

	@DsfServiceEventHandler
	public void handleEvent(ISuspendedDMEvent event) {
		if (fTargetContext == null) {
			return;
		}
		final IExecutionDMContext context= event.getDMContext();
		if (context.equals(fTargetContext)
				|| DMContexts.isAncestorOf(fTargetContext, context)) {
			fCallback.handleTargetSuspended();
		}
	}

	@DsfServiceEventHandler
	public void handleEvent(IResumedDMEvent event) {
		if (fTargetContext == null) {
			return;
		}
		final IExecutionDMContext context= event.getDMContext();
		if (context.equals(fTargetContext)
				|| DMContexts.isAncestorOf(fTargetContext, context)) {
			fCallback.handleTargetResumed();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.service.DsfSession.SessionEndedListener#sessionEnded(org.eclipse.cdt.dsf.service.DsfSession)
	 */
	@Override
	public void sessionEnded(DsfSession session) {
		if (session.getId().equals(fDsfSessionId)) {
			clearDebugContext();
			fCallback.handleTargetEnded();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#getFrameLevel()
	 */
	@Override
	public int getFrameLevel() {
		if (fTargetFrameContext != null) {
			return fTargetFrameContext.getLevel();
		}
		return -1;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#hasFrameContext()
	 */
	@Override
	public boolean hasFrameContext() {
		return fTargetFrameContext != null;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#getFrameFile()
	 */
	@Override
	public String getFrameFile() {
		return fTargetFrameData.getFile();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#getFrameLine()
	 */
	@Override
	public int getFrameLine() {
		return fTargetFrameData.getLine();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyBackend#retrieveDisassembly(java.math.BigInteger, java.math.BigInteger, java.lang.String, int, int, boolean, boolean, boolean, int)
	 */
	@Override
	public void retrieveDisassembly(final BigInteger startAddress, BigInteger endAddress, final String file, final int lineNumber, final int lines, final boolean mixed, final boolean showSymbols, final boolean showDisassembly, final int linesHint) {
		// make sure address range is no less than 32 bytes
		// this is an attempt to get better a response from the backend (bug 302505)
		final BigInteger finalEndAddress= startAddress.add(BigInteger.valueOf(32)).max(endAddress);

		DsfSession session = getSession();
		if (session == null) {
			return;	// can happen during session termination
		}
		
		final DsfExecutor executor= session.getExecutor();
		final IDisassemblyDMContext context= DMContexts.getAncestorOfType(fTargetContext, IDisassemblyDMContext.class);

		
		// align the start address first (bug 328168)
		executor.execute(new Runnable() {			
			@Override
			public void run() {
				alignOpCodeAddress(startAddress, new DataRequestMonitor<BigInteger>(executor, null) {
					
					@Override
					public void handleCompleted() {
						final BigInteger finalStartAddress = getData();
						if (mixed) {
							final DataRequestMonitor<IMixedInstruction[]> disassemblyRequest= new DataRequestMonitor<IMixedInstruction[]>(executor, null) {
								@Override
								public void handleCompleted() {
									final IMixedInstruction[] data= getData();
									if (!isCanceled() && data != null) {
										fCallback.asyncExec(new Runnable() {
											@Override
											public void run() {
												if (!insertDisassembly(finalStartAddress, finalEndAddress, data, showSymbols, showDisassembly)) {
													// retry in non-mixed mode
													fCallback.retrieveDisassembly(finalStartAddress, finalEndAddress, linesHint, false, true);
												}
											}});
									} else {
										final IStatus status= getStatus();
										if (status != null && !status.isOK()) {
											if( file != null )	{
												fCallback.asyncExec(new Runnable() {
													@Override
													public void run() {
														fCallback.retrieveDisassembly(finalStartAddress, finalEndAddress, linesHint, true, true);
													}});
											}
											else {
												fCallback.asyncExec(new Runnable() {
													@Override
													public void run() {
														fCallback.doScrollLocked(new Runnable() {
															@Override
															public void run() {
																fCallback.insertError(finalStartAddress, status.getMessage());
															}
														});
													}});
											}
										}
										fCallback.setUpdatePending(false);
									}
								}
							};
							if (file != null) {
								executor.execute(new Runnable() {
									@Override
									public void run() {
										final IDisassembly disassembly= fServicesTracker.getService(IDisassembly.class);
										if (disassembly == null) {
											disassemblyRequest.cancel();
											disassemblyRequest.done();
											return;
										}
										disassembly.getMixedInstructions(context, file, lineNumber, lines*2, disassemblyRequest);
									}});
							} else {
								executor.execute(new Runnable() {
									@Override
									public void run() {
										final IDisassembly disassembly= fServicesTracker.getService(IDisassembly.class);
										if (disassembly == null) {
											disassemblyRequest.cancel();
											disassemblyRequest.done();
											return;
										}
										disassembly.getMixedInstructions(context, finalStartAddress, finalEndAddress, disassemblyRequest);
									}});
							}
						} else {
							final DataRequestMonitor<IInstruction[]> disassemblyRequest= new DataRequestMonitor<IInstruction[]>(executor, null) {
								@Override
								public void handleCompleted() {
									if (!isCanceled() && getData() != null) {
										fCallback.asyncExec(new Runnable() {
											@Override
											public void run() {
												if (!insertDisassembly(finalStartAddress, finalEndAddress, getData(), showSymbols, showDisassembly)) {
													fCallback.doScrollLocked(new Runnable() {
														@Override
														public void run() {
															fCallback.insertError(finalStartAddress, DisassemblyMessages.DisassemblyBackendDsf_error_UnableToRetrieveData);
														}
													});
												}
											}});
									} else {
										final IStatus status= getStatus();
										if (status != null && !status.isOK()) {
											fCallback.asyncExec(new Runnable() {
												@Override
												public void run() {
													fCallback.doScrollLocked(new Runnable() {
														@Override
														public void run() {
															fCallback.insertError(finalStartAddress, status.getMessage());
														}
													});
												}});
										}
										fCallback.setUpdatePending(false);
									}
								}
							};
							executor.execute(new Runnable() {
								@Override
								public void run() {
									final IDisassembly disassembly= fServicesTracker.getService(IDisassembly.class);
									if (disassembly == null) {
										disassemblyRequest.cancel();
										disassemblyRequest.done();
										return;
									}
									disassembly.getInstructions(context, finalStartAddress, finalEndAddress, disassemblyRequest);
								}});
						}
					}
				});
			}
		});
	}

	private boolean insertDisassembly(BigInteger startAddress, BigInteger endAddress, IInstruction[] instructions, boolean showSymbols, boolean showDisassembly) {
        if (!fCallback.hasViewer() || fDsfSessionId == null || fTargetContext == null) {
			// return true to avoid a retry
			return true;
		}
		if (DEBUG) System.out.println("insertDisassembly "+ DisassemblyUtils.getAddressText(startAddress)); //$NON-NLS-1$
		assert fCallback.getUpdatePending();
		if (!fCallback.getUpdatePending()) {
			// safe-guard in case something weird is going on
			// return true to avoid a retry
			return true;
		}

		boolean insertedAnyAddress = false;

		try {
			fCallback.lockScroller();
			
			AddressRangePosition p= null;
			for (int j = 0; j < instructions.length; j++) {
				IInstruction instruction = instructions[j];
				BigInteger address= instruction.getAdress();
				if (startAddress == null || startAddress.compareTo(BigInteger.ZERO) < 0) {
					startAddress = address;
					fCallback.setGotoAddressPending(address);
				}
				if (p == null || !p.containsAddress(address)) {
					p = fCallback.getPositionOfAddress(address);
				}
				if (p instanceof ErrorPosition && p.fValid) {
					p.fValid = false;
					fCallback.getDocument().addInvalidAddressRange(p);
				} else if (p == null || address.compareTo(endAddress) > 0) {
					if (DEBUG) System.out.println("Excess disassembly lines at " + DisassemblyUtils.getAddressText(address)); //$NON-NLS-1$
					return insertedAnyAddress;
				} else if (p.fValid) {
					if (DEBUG) System.out.println("Excess disassembly lines at " + DisassemblyUtils.getAddressText(address)); //$NON-NLS-1$
					if (!p.fAddressOffset.equals(address)) {
						// override probably unaligned disassembly
						p.fValid = false;
						fCallback.getDocument().addInvalidAddressRange(p);
					} else {
						continue;
					}
				}
				boolean hasSource= false;
				String compilationPath= null;
				// insert symbol label
				final String functionName= instruction.getFuntionName();
				if (functionName != null && functionName.length() > 0 && instruction.getOffset() == 0) {
					p = fCallback.getDocument().insertLabel(p, address, functionName, showSymbols && (!hasSource || showDisassembly));
				}
				// determine instruction byte length
				BigInteger instrLength= null;
				if (instruction instanceof IInstructionWithSize
				        && ((IInstructionWithSize)instruction).getSize() != null) {
					instrLength= new BigInteger(((IInstructionWithSize)instruction).getSize().toString());
				} else {
					if (j < instructions.length - 1) {
						instrLength= instructions[j+1].getAdress().subtract(instruction.getAdress()).abs();
					}
					if (instrLength == null) {
						// cannot determine length of last instruction
						break;
					}
				}
				final String opCode;
				// insert function name+offset instead of opcode bytes
				if (functionName != null && functionName.length() > 0) {
					opCode= functionName + '+' + instruction.getOffset();
				} else {
					opCode= ""; //$NON-NLS-1$
				}

				p = fCallback.getDocument().insertDisassemblyLine(p, address, instrLength.intValue(), opCode, instruction.getInstruction(), compilationPath, -1);
				if (p == null) {
					break;
				}
				insertedAnyAddress = true;
			}
		} catch (BadLocationException e) {
			// should not happen
			DisassemblyUtils.internalError(e);
		} finally {
			fCallback.setUpdatePending(false);
			if (insertedAnyAddress) {
				fCallback.updateInvalidSource();
				fCallback.unlockScroller();
				fCallback.doPending();
				fCallback.updateVisibleArea();
			} else {
				fCallback.unlockScroller();
			}
		}
		return insertedAnyAddress;
	}

	/**
	 * @param startAddress
	 *            an address the caller is hoping will be covered by this
	 *            insertion. I.e., [mixedInstructions] may or may not contain
	 *            that address; the caller wants to know if it does, and so we
	 *            indicate that via our return value. Can be null to indicate n/a, 
	 *            in which case we return true as long as any instruction was inserted
	 *            as long as any instruction was inserted
	 * @param endAddress
	 *            cut-off address. Any elements in [mixedInstructions] that
	 *            extend beyond this address are ignored.
	 * @param mixedInstructions
	 * @param showSymbols
	 * @param showDisassembly
	 * @return whether [startAddress] was inserted
	 */
	private boolean insertDisassembly(BigInteger startAddress, BigInteger endAddress, IMixedInstruction[] mixedInstructions, boolean showSymbols, boolean showDisassembly) {
        if (!fCallback.hasViewer() || fDsfSessionId == null || fTargetContext == null) {
			// return true to avoid a retry
			return true;
		}
		if (DEBUG) System.out.println("insertDisassembly "+ DisassemblyUtils.getAddressText(startAddress)); //$NON-NLS-1$
		boolean updatePending = fCallback.getUpdatePending();
		assert updatePending;
		if (!updatePending) {
			// safe-guard in case something weird is going on
			// return true to avoid a retry
			return true;
		}

		boolean insertedAnyAddress = false;
		try {
			fCallback.lockScroller();
			
			AddressRangePosition p= null;
			for (int i = 0; i < mixedInstructions.length; ++i) {
				IMixedInstruction mixedInstruction= mixedInstructions[i];
				final String file= mixedInstruction.getFileName();
				int lineNumber= mixedInstruction.getLineNumber() - 1;
				IInstruction[] instructions= mixedInstruction.getInstructions();
				for (int j = 0; j < instructions.length; ++j) {
					IInstruction instruction = instructions[j];
					BigInteger address= instruction.getAdress();
					if (startAddress == null) {
						startAddress = address;
						fCallback.setGotoAddressPending(address);
					}
					if (p == null || !p.containsAddress(address)) {
						p = fCallback.getPositionOfAddress(address);
					}
					if (p instanceof ErrorPosition && p.fValid) {
						p.fValid = false;
						fCallback.getDocument().addInvalidAddressRange(p);
					} else if (p == null || address.compareTo(endAddress) > 0) {
						if (DEBUG) System.out.println("Excess disassembly lines at " + DisassemblyUtils.getAddressText(address)); //$NON-NLS-1$
						return insertedAnyAddress;
					} else if (p.fValid) {
						if (DEBUG) System.out.println("Excess disassembly lines at " + DisassemblyUtils.getAddressText(address)); //$NON-NLS-1$
						if (!p.fAddressOffset.equals(address)) {
							// override probably unaligned disassembly
							p.fValid = false;
							fCallback.getDocument().addInvalidAddressRange(p);
						} else {
							continue;
						}
					}
					boolean hasSource= false;
					if (file != null && lineNumber >= 0) {
						p = fCallback.insertSource(p, address, file, lineNumber);
						hasSource = fCallback.getStorageForFile(file) != null;
					}
					// insert symbol label
					final String functionName= instruction.getFuntionName();
					if (functionName != null && functionName.length() > 0 && instruction.getOffset() == 0) {
						p = fCallback.getDocument().insertLabel(p, address, functionName, showSymbols && (!hasSource || showDisassembly));
					}
					// determine instruction byte length
					BigInteger instrLength= null;
					if (instruction instanceof IInstructionWithSize
					        && ((IInstructionWithSize)instruction).getSize() != null) {
						instrLength= new BigInteger(((IInstructionWithSize)instruction).getSize().toString());
					} else {
						if (j < instructions.length - 1) {
							instrLength= instructions[j+1].getAdress().subtract(instruction.getAdress()).abs();
						} else if (i < mixedInstructions.length - 1) {
							int nextSrcLineIdx= i+1;
							while (nextSrcLineIdx < mixedInstructions.length) {
								IInstruction[] nextInstrs= mixedInstructions[nextSrcLineIdx].getInstructions();
								if (nextInstrs.length > 0) {
									instrLength= nextInstrs[0].getAdress().subtract(instruction.getAdress()).abs();
									break;
								}
								++nextSrcLineIdx;
							}
							if (nextSrcLineIdx >= mixedInstructions.length) {
								break;
							}
						}
						if (instrLength == null) {
							// cannot determine length of last instruction
							break;
						}
					}
					final String opCode;
					// insert function name+offset instead of opcode bytes
					if (functionName != null && functionName.length() > 0) {
						opCode= functionName + '+' + instruction.getOffset();
					} else {
						opCode= ""; //$NON-NLS-1$
					}
					p = fCallback.getDocument().insertDisassemblyLine(p, address, instrLength.intValue(), opCode, instruction.getInstruction(), file, lineNumber);
					if (p == null) {
						break;
					}
					insertedAnyAddress = true;
				}
			}
			
		} catch (BadLocationException e) {
			// should not happen
			DisassemblyUtils.internalError(e);
		} finally {
			fCallback.setUpdatePending(false);
			if (insertedAnyAddress) {
				fCallback.updateInvalidSource();
				fCallback.unlockScroller();
				fCallback.doPending();
				fCallback.updateVisibleArea();
			} else {
				fCallback.unlockScroller();
			}
		}
		return insertedAnyAddress;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#insertSource(org.eclipse.jface.text.Position, java.math.BigInteger, java.lang.String, int)
	 */
	@Override
	public Object insertSource(Position pos, BigInteger address, final String file, int lineNumber) {
		Object sourceElement = null;
		final ISourceLookupDMContext ctx= DMContexts.getAncestorOfType(fTargetContext, ISourceLookupDMContext.class);
		final DsfExecutor executor= getSession().getExecutor();
		Query<Object> query= new Query<Object>() {
			@Override
			protected void execute(final DataRequestMonitor<Object> rm) {
				final DataRequestMonitor<Object> request= new DataRequestMonitor<Object>(executor, rm) {
					@Override
					protected void handleSuccess() {
						rm.setData(getData());
						rm.done();
					}
				};
				final ISourceLookup lookup= getService(ISourceLookup.class);
				lookup.getSource(ctx, file, request);
			}
		};
		try {
			getSession().getExecutor().execute(query);
			sourceElement= query.get();
		} catch (InterruptedException exc) {
			DisassemblyUtils.internalError(exc);
		} catch (ExecutionException exc) {
			DisassemblyUtils.internalError(exc);
		}
		return sourceElement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#gotoSymbol(java.lang.String)
	 */
	@Override
	public void gotoSymbol(final String symbol) {
		evaluateAddressExpression(symbol, false, new DataRequestMonitor<BigInteger>(getSession().getExecutor(), null) {			
			@Override
			protected void handleSuccess() {
				final BigInteger address = getData();
				if (address != null) {
					fCallback.asyncExec(new Runnable() {
						@Override
						public void run() {
							fCallback.gotoAddress(address);
						}});
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#evaluateAddressExpression(java.lang.String, boolean)
	 */
	@Override
	public BigInteger evaluateAddressExpression(final String symbol, final boolean suppressError) {
		Query<BigInteger> query = new Query<BigInteger>() {
			@Override
			protected void execute(DataRequestMonitor<BigInteger> rm) {
				evaluateAddressExpression(symbol, suppressError, rm);				
			}			
		};
		getSession().getExecutor().execute(query);
		try {
			return query.get();
		} catch (Exception e) {
			return null;
		}
	}
	
	private void evaluateAddressExpression(final String symbol, final boolean suppressError, final DataRequestMonitor<BigInteger> rm) {		
		final IExpressions expressions= getService(IExpressions.class);
		if (expressions == null) {
			rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "", null)); //$NON-NLS-1$
			rm.done();
			return;
		}
		
		final IExpressionDMContext exprDmc= expressions.createExpression(fTargetContext, symbol);
		// first, try to get l-value address
		expressions.getExpressionAddressData(exprDmc, new DataRequestMonitor<IExpressionDMAddress>(getSession().getExecutor(), null) {
			@Override
			protected void handleSuccess() {
				IExpressionDMAddress data = getData();
				final IAddress address = data.getAddress();
                if (address != null && address != IExpressionDMLocation.INVALID_ADDRESS) {
                    final BigInteger addressValue = address.getValue();
                    fCallback.asyncExec(new Runnable() {
                        @Override
						public void run() {
                            fCallback.gotoAddress(addressValue);
                        }
                    });
                    rm.setData(addressValue);
                    rm.done();
                } else {
                    handleError();
                }
			}
			@Override
			protected void handleError() {
				// not an l-value, evaluate expression
				final FormattedValueDMContext valueDmc= expressions.getFormattedValueContext(exprDmc, IFormattedValues.HEX_FORMAT);
				expressions.getFormattedExpressionValue(valueDmc, new DataRequestMonitor<FormattedValueDMData>(getSession().getExecutor(), null) {
					@Override
					protected void handleSuccess() {
						FormattedValueDMData data= getData();
						String value= data.getFormattedValue();
						BigInteger address= null;
						try {
							address = DisassemblyUtils.decodeAddress(value);
						} catch (final Exception e) {
							// "value" can be empty i.e *fooX, where fooX is a variable.
							// Not sure if this is a bug or not. So, fail the request instead.
							rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "", null)); //$NON-NLS-1$
							
							if (!suppressError) {
								DisassemblyBackendDsf.this.handleError(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, 
						                DisassemblyMessages.Disassembly_log_error_expression_eval + " (" + e.getMessage() + ")", null)); //$NON-NLS-1$ //$NON-NLS-2$
							}							
						}
						rm.setData(address);
						rm.done();
					}
					@Override
					protected void handleError() {
						if (!suppressError) {
							DisassemblyBackendDsf.this.handleError(getStatus());
						}
						rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "", null)); //$NON-NLS-1$
						rm.done();
					}
				});
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#retrieveDisassembly(java.lang.String, int, java.math.BigInteger, boolean, boolean, boolean)
	 */
	@Override
	public void retrieveDisassembly(final String file, final int lines, final BigInteger endAddress, boolean mixed, final boolean showSymbols, final boolean showDisassembly) {
		String debuggerPath= file;
		// try reverse lookup
		final ISourceLookupDMContext ctx= DMContexts.getAncestorOfType(fTargetContext, ISourceLookupDMContext.class);
		final DsfExecutor executor= getSession().getExecutor();
		Query<String> query= new Query<String>() {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
				final DataRequestMonitor<String> request= new DataRequestMonitor<String>(executor, rm) {
					@Override
					protected void handleSuccess() {
						rm.setData(getData());
						rm.done();
					}
				};
				final ISourceLookup lookup= getService(ISourceLookup.class);
				lookup.getDebuggerPath(ctx, file, request);
			}
		};
		try {
			getSession().getExecutor().execute(query);
			debuggerPath= query.get();
		} catch (InterruptedException exc) {
			internalError(exc);
		} catch (ExecutionException exc) {
			internalError(exc);
		}

		final IDisassemblyDMContext context= DMContexts.getAncestorOfType(fTargetContext, IDisassemblyDMContext.class);

		final String finalFile= debuggerPath;
		final DataRequestMonitor<IMixedInstruction[]> disassemblyRequest= new DataRequestMonitor<IMixedInstruction[]>(executor, null) {
			@Override
			public void handleCompleted() {
				final IMixedInstruction[] data= getData();
				if (!isCanceled() && data != null) {
					fCallback.asyncExec(new Runnable() {
						@Override
						public void run() {
							if (!insertDisassembly(null, endAddress, data, showSymbols, showDisassembly)) {
								// retry in non-mixed mode
								retrieveDisassembly(file, lines, endAddress, false, showSymbols, showDisassembly);
							}
						}});
				} else {
					final IStatus status= getStatus();
					if (status != null && !status.isOK()) {
						DisassemblyBackendDsf.this.handleError(getStatus());
					}
					fCallback.setUpdatePending(false);
				}
			}
		};
		assert !fCallback.getUpdatePending();
		fCallback.setUpdatePending(true);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				final IDisassembly disassembly= fServicesTracker.getService(IDisassembly.class);
				if (disassembly == null) {
					disassemblyRequest.cancel();
					disassemblyRequest.done();
					return;
				}
				disassembly.getMixedInstructions(context, finalFile, 1, lines, disassemblyRequest);
			}});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#evaluateExpression(java.lang.String)
	 */
	@Override
	public String evaluateExpression(final String expression) {
		if (fTargetFrameContext == null) {
			return null;
		}
		final DsfExecutor executor= DsfSession.getSession(fDsfSessionId).getExecutor();
		Query<FormattedValueDMData> query= new Query<FormattedValueDMData>() {
			@Override
			protected void execute(final DataRequestMonitor<FormattedValueDMData> rm) {
				IExecutionDMContext exeCtx = DMContexts.getAncestorOfType(fTargetFrameContext, IExecutionDMContext.class);
				final IRunControl rc= getService(IRunControl.class);
				if (rc == null || !rc.isSuspended(exeCtx)) {
					rm.done();
					return;
				}
				final IExpressions expressions= getService(IExpressions.class);
				if (expressions == null) {
					rm.done();
					return;
				}
				IExpressionDMContext exprDmc= expressions.createExpression(fTargetFrameContext, expression);
				final FormattedValueDMContext valueDmc= expressions.getFormattedValueContext(exprDmc, IFormattedValues.NATURAL_FORMAT);
				expressions.getFormattedExpressionValue(valueDmc, new DataRequestMonitor<FormattedValueDMData>(executor, rm) {
					@Override
					protected void handleSuccess() {
						FormattedValueDMData data= getData();
						rm.setData(data);
						rm.done();
					}
				});
			}};
		
		executor.execute(query);
		FormattedValueDMData data= null;
		try {
			data= query.get();
		} catch (InterruptedException exc) {
		} catch (ExecutionException exc) {
		}
		if (data != null) {
			return data.getFormattedValue();
		}
		return null;
		
	}
	
	/**
	 * Align the opCode of an address.
	 * 
	 * @param addr the address
	 * @param rm the data request monitor
	 */
	void alignOpCodeAddress(final BigInteger addr, final DataRequestMonitor<BigInteger> rm) { 
		IDisassembly2 disassembly = getService(IDisassembly2.class); 
		if (disassembly == null) { 
			rm.setData(addr); 
			rm.done(); 
			return; 
		} 

		final DsfExecutor executor= DsfSession.getSession(fDsfSessionId).getExecutor(); 
		final IDisassemblyDMContext context = DMContexts.getAncestorOfType(fTargetContext, IDisassemblyDMContext.class); 
		disassembly.alignOpCodeAddress(context, addr, new DataRequestMonitor<BigInteger>(executor, rm) { 
			@Override 
			protected void handleFailure() { 
				rm.setData(addr); 
				rm.done(); 
			} 
			@Override 
			protected void handleSuccess() { 
				rm.setData(getData()); 
				rm.done(); 
			} 
		}); 
	}
}