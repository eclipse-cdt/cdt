/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.internal.core.ICWatchpointTarget;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMAddress;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.AbstractExpressionVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.SyncVariableDataAccess;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIExpressions;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.VMChildrenUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TreePath;

/**
 * Specialization of DSF's VariableVMNode. See
 * {@link GdbVariableVMNode#createVMContext(IDMContext)} for why this is needed.
 */
public class GdbVariableVMNode extends VariableVMNode {

	// Notes on gdb's pretty printer support (bug 302121):
	// If
	//   - an expression has children
	//   - and those children are provided by a pretty printer
	//   - and the expression is not yet initialized
	// the expression might have a large number of children. Asking gdb to
	// provide all children, or even just the number of all children, will
	// lead to extremely slow response times.
	// Furthermore, there are C/C++ data structures (e.g. linked lists) that
	// can lead to endless loops if not correctly initialized and a pretty
	// printer tries to obtain the number of children. In this case, gdb
	// will never return.
	//
	// In order to address this problem, IMIExpressions deriving from
	// IExpressions has been introduced.
	// It lets the client specify a maximum number of children to be considered,
	// both when asking the number of sub-expression, or the sub-expressions
	// itself.
	//
	// The algorithm how it is used is as following:
	//   - We don't show all children in the UI, but only up to a certain limit.
	//     A special context type IncompleteChildrenVMC is used to show that
	//     there are more children than those currently visible.
	//     The user can fetch more children on demand.
	//   - updateHasElementsInSessionThread asks only for up to one child.
	//   - updateElementCountInSessionThread checks whether the expression
	//     requires a limit on the child count limit. If yes, it asks
	//     the expression service for up to limit + 1 children. The + 1
	//     represent the child for the <...more children...> node. I.e.,
	//     if the returned number of children is limit + 1, then there is
	//     an <...more_children...> node. Otherwise, there is not.
	//  - updateElementsInSessionThread sooner or later delegates to
	//    fillUpdateWithVMCs. fillUpdateWithVMCs checks whether there are
	//    limit + 1 children, and if so, will create an IncompleteChildrenVMC
	//    for the last child, discarding the original expression context.
	
	/**
	 * Specialization of VariableVMNode.VariableExpressionVMC that participates
	 * in the "Add Watchpoint" object contribution action.
	 */
	public class GdbVariableExpressionVMC extends VariableVMNode.VariableExpressionVMC implements ICWatchpointTarget {
        
		/**
		 * Constructor (passthru)
		 */
		public GdbVariableExpressionVMC(IDMContext dmc) {
            super(dmc);
        }
        
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.internal.core.IWatchpointTarget#getSize()
		 */
        @Override
		public void getSize(final ICWatchpointTarget.GetSizeRequest request) {
			final IExpressionDMContext exprDmc = DMContexts.getAncestorOfType(getDMContext(), IExpressionDMContext.class);
			if (exprDmc != null) {
	            getSession().getExecutor().execute(new Runnable() {
	                @Override
	                public void run() {
	                    final IExpressions expressionService = getServicesTracker().getService(IExpressions.class);
	                    if (expressionService != null) {
	                    	final DataRequestMonitor<IExpressionDMAddress> drm = new DataRequestMonitor<IExpressionDMAddress>(getSession().getExecutor(), null) {
                                @Override
								public void handleCompleted() {
                                	if (isSuccess()) {
                                		request.setSize(getData().getSize());
                                	}
                                	request.setStatus(getStatus());
                                    request.done();
                                }
	                    	};
	                    	
	                        expressionService.getExpressionAddressData(exprDmc, drm);
	                    }
	        			else {
	        				request.setStatus(internalError());
	        				request.done();
	        			}
	                }
	            });
			}
			else {
				request.setStatus(internalError());
				request.done();
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.internal.core.IWatchpointTarget#canCreateWatchpoint(org.eclipse.cdt.debug.internal.core.IWatchpointTarget.CanCreateWatchpointRequest)
		 */
        @Override
		public void canSetWatchpoint(final ICWatchpointTarget.CanCreateWatchpointRequest request) {
			// If the expression is an l-value, then we say it supports a
			// watchpoint. The logic here is basically the same as what's in
			// getSize(), as the same DSF service method tells us (a) if it's an
			// lvalue, and (b) its size.
			final IExpressionDMContext exprDmc = DMContexts.getAncestorOfType(getDMContext(), IExpressionDMContext.class);
			if (exprDmc != null) {
	            getSession().getExecutor().execute(new Runnable() {
	                @Override
	                public void run() {
	                    final IExpressions expressionService = getServicesTracker().getService(IExpressions.class);
	                    if (expressionService != null) {
	                    	final DataRequestMonitor<IExpressionDMAddress> drm = new DataRequestMonitor<IExpressionDMAddress>(getSession().getExecutor(), null) {
                                @Override
								public void handleCompleted() {
                                	if (isSuccess()) {
	                                    request.setCanCreate(getData().getSize() > 0);
                                	}
                                	request.setStatus(getStatus());                                	
	                                request.done();
                                }
	                    	};
	                    	
	                        expressionService.getExpressionAddressData(exprDmc, drm);
	                    }
	        			else {
	        				request.setStatus(internalError());
	        				request.done();
	        			}
	                }
	            });
			}
			else {
				request.setStatus(internalError());
				request.done();
			}
		}
	};
	
	/**
	 * The special context representing more children to be available.
	 * 
	 * @since 3.0
	 */
	public class IncompleteChildrenVMC extends AbstractVMContext {

		private IExpressionDMContext parentDmc;
		
		public IncompleteChildrenVMC(IExpressionDMContext exprDmc, int childCountLimit) {
			super(GdbVariableVMNode.this);
			this.parentDmc = exprDmc;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof IncompleteChildrenVMC && 
		    ((IncompleteChildrenVMC)obj).parentDmc.equals(parentDmc);
		}

		@Override
		public int hashCode() {
			return parentDmc.hashCode();
		}

		public IExpressionDMContext getParentDMContext() {
			return parentDmc;
		}		
	}
	
	/**
	 * Maps expressions to their current limit on the maximum number of children.
	 */
	private Map<IExpressionDMContext, Integer> childCountLimits = new HashMap<IExpressionDMContext, Integer>();

	/**
	 * Utility method to create an IStatus object for an internal error 
	 */
	private static Status internalError() {
		return new Status(Status.ERROR, GdbUIPlugin.getUniqueIdentifier(), Messages.Internal_Error);
	}
	/**
	 * Constructor (passthru)
	 */
	public GdbVariableVMNode(AbstractDMVMProvider provider, DsfSession session,
			SyncVariableDataAccess syncVariableDataAccess) {
		super(provider, session, syncVariableDataAccess);
	}

	/**
	 * The primary reason for the specialization of VariableVMNode is to create
	 * a GDB-specific VM context that implements ICWatchpointTarget, so that the
	 * "Add Watchpoint" context menu appears for variables and expressions in
	 * GDB-DSF sessions but not necessarily other DSF-based sessions [bugzilla
	 * 248606]
	 * 
	 * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode#createVMContext(org.eclipse.cdt.dsf.datamodel.IDMContext)
	 */
    @Override
    protected IDMVMContext createVMContext(IDMContext dmc) {
        return new GdbVariableExpressionVMC(dmc);
    }
    
	@Override
	protected void updateHasElementsInSessionThread(final IHasChildrenUpdate update) {
		if (update.getElement() instanceof IncompleteChildrenVMC) {
			update.setHasChilren(false);
			update.done();
			return;
		}
				
		super.updateHasElementsInSessionThread(update);
	}
	
	@Override
	protected void updateElementCountInSessionThread(final IChildrenCountUpdate update) {
        // Get the data model context object for the current node in the hierarchy.
        
        final IExpressionDMContext expressionDMC = findDmcInPath(update.getViewerInput(), update.getElementPath(), IExpressionDMContext.class);
        
        if ( expressionDMC != null ) {
            final IExpressions expressionService = getServicesTracker().getService(IExpressions.class);
            
            if (expressionService == null) {
                handleFailedUpdate(update);
                return;
            }

            if (expressionService instanceof IMIExpressions) {
            	final IMIExpressions miExpressions = (IMIExpressions) expressionService;

				miExpressions.safeToAskForAllSubExpressions(expressionDMC,
						new ViewerDataRequestMonitor<Boolean>(getSession().getExecutor(), update) {

					@Override
					protected void handleCompleted() {
						if (! isSuccess()) {
							handleFailedUpdate(update);
							return;
						}

						boolean limitRequired = ! getData().booleanValue();
						if (limitRequired) {

							final int childCountLimit = getOrInitChildCountLimit(expressionDMC);
							
							miExpressions.getSubExpressionCount(
									expressionDMC, childCountLimit + 1, 
									new ViewerDataRequestMonitor<Integer>(getExecutor(), update) {
										@Override
										public void handleCompleted() {
											if (!isSuccess()) {
												handleFailedUpdate(update);
												return;
											}

											int childCount = getData();
											if (childCountLimit < childCount) {
												childCount = childCountLimit + 1;
											}
											
											update.setChildCount(childCount);
											update.done();
										}
									});
						} else {
							GdbVariableVMNode.super.updateElementCountInSessionThread(update);
						}
					}
				});
            	
            	return;
            }
        }
        
        super.updateElementCountInSessionThread(update);
	}

	@Override
	protected void fillUpdateWithVMCs(IChildrenUpdate update,
			IDMContext[] dmcs, int firstIndex) {
		super.fillUpdateWithVMCs(update, dmcs, firstIndex);
		
        IExpressionDMContext expressionDMC = findDmcInPath(update.getViewerInput(), update.getElementPath(), IExpressionDMContext.class);
        
        if (expressionDMC != null) {
        	int childCountLimit = getChildCountLimit(expressionDMC);
        	int childCount = firstIndex + update.getLength();
        	if (childCountLimit < childCount) {
        		update.setChild(new IncompleteChildrenVMC(expressionDMC, childCountLimit), childCountLimit);
        	}
        }
	}

	@Override
	public void update(IPropertiesUpdate[] updates) {
		List<IPropertiesUpdate> realExpressions = new ArrayList<IPropertiesUpdate>();

		for (IPropertiesUpdate update : updates) {
			if (update.getElement() instanceof IncompleteChildrenVMC) {
				if (update.getProperties().contains(
						AbstractExpressionVMNode.PROP_ELEMENT_EXPRESSION)) {
					update.setProperty(
							AbstractExpressionVMNode.PROP_ELEMENT_EXPRESSION,
							Messages.More_Children);

				}
				
				if (update.getProperties().contains(PROP_NAME)) {
					update.setProperty(PROP_NAME, Messages.More_Children);
				}
				update.done();
			} else {
				realExpressions.add(update);
			}
		}
		
		super.update(realExpressions.toArray(new IPropertiesUpdate[realExpressions.size()]));
	}

	private int getInitialChildCountLimit() {
		Object initialLimitProperty = getVMProvider().getPresentationContext().getProperty(
						IGdbDebugPreferenceConstants.PREF_INITIAL_CHILD_COUNT_LIMIT_FOR_COLLECTIONS);
		
		return (initialLimitProperty instanceof Integer) ? (Integer) initialLimitProperty
				: 100;
	}
	
	/**
	 * The given expression context requires a child count limit. If a limit
	 * is already available from preceding calls, obtain this limit. Otherwise
	 * calculate the initial value, store it, and return it.
	 * 
	 * @param expressionDMC
	 * @return The child count limit to apply for the given expression.
	 * 
	 * @since 3.0
	 */
	protected int getOrInitChildCountLimit(IExpressionDMContext expressionDMC) {
		if (childCountLimits.containsKey(expressionDMC)) {
			return childCountLimits.get(expressionDMC);
		}

		int initialLimit = getInitialChildCountLimit();
		childCountLimits.put(expressionDMC, initialLimit);
		
		return initialLimit;
	}
	
	/**
	 * @param expressionDMC
	 * @return The currently stored child count limit for the given expression,
	 *         or {@link Integer#MAX_VALUE} if no child count limit is currently
	 *         stored.
	 * 
	 * @since 3.0
	 */
	protected int getChildCountLimit(IExpressionDMContext expressionDMC) {
		if (childCountLimits.containsKey(expressionDMC)) {
			return childCountLimits.get(expressionDMC);
		}
		return Integer.MAX_VALUE; 
	}
	
    private void resetChildCountLimits(IExecutionDMContext execCtx) {
    	int initialLimit = getInitialChildCountLimit();
    	for (IExpressionDMContext limitCtx : childCountLimits.keySet()) {
    		if (DMContexts.isAncestorOf(limitCtx, execCtx)) {
    			childCountLimits.put(limitCtx, initialLimit);
    		}			
		}
    }
    
    private void resetAllChildCountLimits() {
    	int initialLimit = getInitialChildCountLimit();
    	for (IExpressionDMContext limitCtx : childCountLimits.keySet()) {
    		childCountLimits.put(limitCtx, initialLimit);
		}
    }
    
	/**
	 * Increment the child count limit by the default increment.
	 * This implementation doubles the current limit.
	 * 
	 * @since 3.0
	 */
	public void incrementChildCountLimit(IExpressionDMContext expressionDMC) {
		assert(childCountLimits.containsKey(expressionDMC));
		
		int childCountLimit = getChildCountLimit(expressionDMC);
		if (childCountLimit < Integer.MAX_VALUE / 2) {
			childCountLimits.put(expressionDMC, childCountLimit * 2);
		}
	}
	
	@Override
	public int getDeltaFlags(Object e) {
		int flags = super.getDeltaFlags(e);
		
		if (e instanceof FetchMoreChildrenEvent) {
			flags |= IModelDelta.CONTENT;
		} else if (e instanceof ISuspendedDMEvent) {
			// The child count limit must be reset.
			flags |= IModelDelta.CONTENT;
        } else if (e instanceof PropertyChangeEvent) {
            String property = ((PropertyChangeEvent)e).getProperty();
            if (IGdbDebugPreferenceConstants.PREF_INITIAL_CHILD_COUNT_LIMIT_FOR_COLLECTIONS.equals(property)) 
            {
                flags |= IModelDelta.CONTENT;
            }
		}
    
		return flags;
	}
	
	
	@Override
	public int getDeltaFlagsForExpression(IExpression expression, Object event) {
		int flags = super.getDeltaFlagsForExpression(expression, event);

		if (event instanceof FetchMoreChildrenEvent) {
			flags |= IModelDelta.CONTENT;
		} else if (event instanceof PropertyChangeEvent) {
			String property = ((PropertyChangeEvent) event).getProperty();
			if (IGdbDebugPreferenceConstants.PREF_INITIAL_CHILD_COUNT_LIMIT_FOR_COLLECTIONS
					.equals(property)) {
				flags |= IModelDelta.CONTENT;
			}
		}

		return flags;
	}
	
	@Override
	public void buildDelta(Object e, VMDelta parentDelta, int nodeOffset,
			RequestMonitor rm) {
		
        if (e instanceof FetchMoreChildrenEvent) {
        	buildDeltaForFetchMoreChildrenEvent((FetchMoreChildrenEvent) e, parentDelta, rm);
        	return;
		} else if (e instanceof ISuspendedDMEvent) {
			resetChildCountLimits(((ISuspendedDMEvent) e).getDMContext());
        } else if (e instanceof PropertyChangeEvent) {
            String property = ((PropertyChangeEvent)e).getProperty();
            if (IGdbDebugPreferenceConstants.PREF_INITIAL_CHILD_COUNT_LIMIT_FOR_COLLECTIONS.equals(property)) 
            {
            	resetAllChildCountLimits();
                buildDeltaForChildCountLimitPreferenceChangedEvent(parentDelta, rm);
                return;
            }
        }
        
        super.buildDelta(e, parentDelta, nodeOffset, rm);
	}
	
	@Override
	public void buildDeltaForExpressionElement(Object element, int elementIdx,
			Object event, VMDelta parentDelta, RequestMonitor rm) {
		
        if (event instanceof FetchMoreChildrenEvent) {
        	FetchMoreChildrenEvent fetchMoreEvent = (FetchMoreChildrenEvent) event;
        	GdbVariableExpressionVMC topLevelExpressionVMC = (GdbVariableExpressionVMC) element;
        	if (topLevelExpressionVMC.equals(fetchMoreEvent.getPath().getFirstSegment())) {
        		buildDeltaForFetchMoreChildrenEvent(fetchMoreEvent, parentDelta, rm);
        		return;
        	}
		} else if (event instanceof ISuspendedDMEvent) {
			resetChildCountLimits(((ISuspendedDMEvent) event).getDMContext());
		} else if (event instanceof IContainerSuspendedDMEvent) {
			resetChildCountLimits(((IContainerSuspendedDMEvent) event).getDMContext());
        } else if (event instanceof PropertyChangeEvent) {
            String property = ((PropertyChangeEvent)event).getProperty();
            if (IGdbDebugPreferenceConstants.PREF_INITIAL_CHILD_COUNT_LIMIT_FOR_COLLECTIONS.equals(property)) 
            {
            	resetAllChildCountLimits();
                buildDeltaForChildCountLimitPreferenceChangedEvent(parentDelta, rm);
                return;
            }
        }

        super.buildDeltaForExpressionElement(element, elementIdx, event, parentDelta,
				rm);
	}
	
	private void buildDeltaForFetchMoreChildrenEvent(
			FetchMoreChildrenEvent fetchMoreChidrenEvent,
			VMDelta parentDelta, final RequestMonitor rm) {

		TreePath path = fetchMoreChidrenEvent.getPath();
		
		// Add all the parents of the expression. Those didn't change, however.
		for (int i = 0; i < path.getSegmentCount() - 2; ++i) {
			parentDelta = parentDelta.addNode(path.getSegment(i), IModelDelta.NO_CHANGE);
		}
		
		// Add the node for the expression. This one changed, of course. 
		final VMDelta expressionDelta =
			parentDelta.addNode(path.getSegment(path.getSegmentCount() - 2), IModelDelta.CONTENT);

		// Make sure the element formerly know as <...more_children...> is selected
		// afterwards.
		
        final int offset = getChildCountLimit(fetchMoreChidrenEvent.getDMContext()) / 2;
        // The one trailing element is to see whether there are more children.
        final int maxLength = offset + 1;
		getVMProvider().updateNode(
            this,
            new VMChildrenUpdate(
                expressionDelta, getVMProvider().getPresentationContext(), offset, maxLength,
                new DataRequestMonitor<List<Object>>(getExecutor(), rm) {
                    @Override
                    public void handleCompleted() {

                    	// FIXME  if the new child has children they do not appear because of this code.
//                        final List<Object> data= getData();
//                        if (data != null && data.size() != 0) {
//							expressionDelta.addNode(data.get(0), offset, IModelDelta.SELECT);
//                        }
                        rm.done();
                    }
                })
            );
    }
	
	private void buildDeltaForChildCountLimitPreferenceChangedEvent(
			final VMDelta parentDelta, final RequestMonitor rm) {
        parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        rm.done();
    }
}
