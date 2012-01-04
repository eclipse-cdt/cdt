/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.launch;

import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMData;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;

/**
 * Default DSF selection policy implementation modelled after platform version
 * (<code>DefaultSelectionPolicy</code>).
 * @since 1.1
 */
public class DefaultDsfSelectionPolicy implements IModelSelectionPolicy {

	private IDMContext fDMContext;

	/**
	 * Create selection policy instance for the given data model context.
	 * 
	 * @param dmContext
	 */
	public DefaultDsfSelectionPolicy(IDMContext dmContext) {
		fDMContext= dmContext;
	}

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy#contains(org.eclipse.jface.viewers.ISelection, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	@Override
	public boolean contains(ISelection selection, IPresentationContext context) {
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId())) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss= (IStructuredSelection) selection;
				Object element= ss.getFirstElement();
				if (element instanceof IDMVMContext) {
					IDMVMContext dmvmContext= (IDMVMContext) element;
					IDMContext dmContext= dmvmContext.getDMContext();
					if (dmContext != null) {
						return fDMContext.getSessionId().equals(dmContext.getSessionId());
					}
				}
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy#isSticky(org.eclipse.jface.viewers.ISelection, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	@Override
	public boolean isSticky(ISelection selection, IPresentationContext context) {
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId())) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss= (IStructuredSelection) selection;
				Object element= ss.getFirstElement();
				return isSticky(element);
			}
		}
		return false;
	}

	protected boolean isSticky(Object element) {
		if (element instanceof IDMVMContext) {
			IDMVMContext dmvmContext= (IDMVMContext) element;
			final IDMContext dmContext= dmvmContext.getDMContext();
			if (dmContext instanceof IFrameDMContext) {
				final IExecutionDMContext execContext= DMContexts.getAncestorOfType(dmContext, IExecutionDMContext.class);
				if (execContext != null) {
					Query<Boolean> query = new Query<Boolean>() {
						@Override
						protected void execute(DataRequestMonitor<Boolean> rm) {
							DsfServicesTracker servicesTracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), dmContext.getSessionId());
							try {
								IRunControl runControl= servicesTracker.getService(IRunControl.class);
								if (runControl != null) {
									rm.setData(runControl.isSuspended(execContext));
								} else {
								    rm.setData(false);
								}
							} finally {
								servicesTracker.dispose();
								rm.done();
							}
						}
					};
					DsfSession session = DsfSession.getSession(dmContext.getSessionId());
					if (session != null) {
						if (session.getExecutor().isInExecutorThread()) {
							query.run();
						} else {
							session.getExecutor().execute(query);
						}
						try {
							Boolean result = query.get();
							return result != null && result.booleanValue();
						} catch (InterruptedException exc) {
							Thread.currentThread().interrupt();
						} catch (ExecutionException exc) {
							DsfUIPlugin.log(exc);
						}
					}
				}
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy#overrides(org.eclipse.jface.viewers.ISelection, org.eclipse.jface.viewers.ISelection, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	@Override
	public boolean overrides(ISelection existing, ISelection candidate, IPresentationContext context) {
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId())) {
			if (existing instanceof IStructuredSelection && candidate instanceof IStructuredSelection) {
				IStructuredSelection ssExisting = (IStructuredSelection) existing;
				IStructuredSelection ssCandidate = (IStructuredSelection) candidate;
				return overrides(ssExisting.getFirstElement(), ssCandidate.getFirstElement());
			}
		}
		return true;
	}

	
	protected boolean overrides(Object existing, Object candidate) {
		if (existing == null || existing.equals(candidate)) {
			return true;
		}
		if (existing instanceof IDMVMContext && candidate instanceof IDMVMContext) {
			IDMContext curr = ((IDMVMContext) existing).getDMContext();
			IDMContext cand = ((IDMVMContext) candidate).getDMContext();
			if (curr instanceof IFrameDMContext && cand instanceof IFrameDMContext) {
				IExecutionDMContext currExecContext= DMContexts.getAncestorOfType(curr, IExecutionDMContext.class);
				if (currExecContext != null) {
					IExecutionDMContext candExecContext= DMContexts.getAncestorOfType(cand, IExecutionDMContext.class);
					return currExecContext.equals(candExecContext) || 
					    !isSticky(existing) || 
					    frameOverrides((IFrameDMContext)curr, (IFrameDMContext)cand);
				}
			}
		}
		return !isSticky(existing);
	}
	
	/**
	 * Last test for whether a stack frame overrides another stack frame.
	 * If two stack frames are from the same execution container (process) and 
	 * the entire process has stopped (as in all-stop run control), and the 
	 * current frame's thread was stopped due to the container stopping, then
	 * the new frame selection should override the current one.  This is because
	 * the new thread is most likely a the thread that triggered the container 
	 * to stop.   
	 * @param curr Currently selected stack frame.
	 * @param cand Candidate stack frame to be selected.
	 * @return <code>true</code> if the new frame should override current selection. 
	 */
	private boolean frameOverrides(final IFrameDMContext curr, final IFrameDMContext cand) {
	    // We're assuming that frames are from different execution contexts. 
	    
	    // Check if they are from the same container context:
        final IContainerDMContext currContContext= DMContexts.getAncestorOfType(curr, IContainerDMContext.class);
        IContainerDMContext candContContext= DMContexts.getAncestorOfType(cand, IContainerDMContext.class);
        if (currContContext == null || !currContContext.equals(candContContext)) {
            // If from different containers, frames should not override each other.
            return false;
        }
        
        Query<Boolean> query = new Query<Boolean>() {
            @Override
            protected void execute(final DataRequestMonitor<Boolean> rm) {
                DsfServicesTracker servicesTracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), curr.getSessionId());

                // Check if container is not suspended.
                IRunControl runControl= servicesTracker.getService(IRunControl.class);
                if (runControl != null && runControl.isSuspended(currContContext)) {
                    IExecutionDMContext execDmc = DMContexts.getAncestorOfType(curr, IExecutionDMContext.class);
                    // If container is suspended, check whether the current thread was stopped due 
                    // to container suspended event.
                    runControl.getExecutionData(
                        execDmc, 
                        new DataRequestMonitor<IExecutionDMData>(ImmediateExecutor.getInstance(), rm) {
                            @Override
                            protected void handleSuccess() {
                                rm.setData( getData().getStateChangeReason() == IRunControl.StateChangeReason.CONTAINER );
                                rm.done();
                            };
                        });
                } else {
                    // If container is not suspended it's running, then do not override the selection.
                    rm.setData(false);
                    rm.done();
                } 
                // In either case, we won't need the services tracker anymore.
                servicesTracker.dispose();
            }
        };
        
        DsfSession session = DsfSession.getSession(curr.getSessionId());
        if (session != null) {
            if (session.getExecutor().isInExecutorThread()) {
                query.run();
            } else {
                session.getExecutor().execute(query);
            }
            try {
                Boolean result = query.get();
                return result != null && result.booleanValue();
            } catch (InterruptedException exc) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException exc) {
                DsfUIPlugin.log(exc);
            }
        }
        return false;
	}

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy#replaceInvalidSelection(org.eclipse.jface.viewers.ISelection, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public ISelection replaceInvalidSelection(ISelection invalidSelection, ISelection newSelection) {
        if (invalidSelection instanceof ITreeSelection) {
            ITreeSelection treeSelection = (ITreeSelection)invalidSelection;
            if (treeSelection.getPaths().length == 1) {
                TreePath path = treeSelection.getPaths()[0];
                if (path.getSegmentCount() > 1) {
                    return new TreeSelection(path.getParentPath());
                }
            }
        }
        return newSelection;
	}

}
