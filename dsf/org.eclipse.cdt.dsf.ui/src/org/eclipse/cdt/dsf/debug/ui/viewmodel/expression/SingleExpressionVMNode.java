/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.ui.viewmodel.expression;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.ExpressionVMProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.ExpressionsChangedEvent;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.InvalidExpressionVMContext;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerCountingRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.jface.viewers.TreePath;

/**
 * A VM node for displaying a single expression in the expression hover.
 * 
 * @since 2.1
 */
public class SingleExpressionVMNode extends AbstractVMNode implements IElementLabelProvider {

	private static class RootDMVMContext extends AbstractVMContext implements IDMVMContext {
		private final IDMContext fDmc;
		public RootDMVMContext(IVMNode node, IDMContext dmc) {
			super(node);
			fDmc = dmc;
		}
	    @Override
		public IDMContext getDMContext() {
			return fDmc;
		}
		@SuppressWarnings("rawtypes")
		@Override
		public Object getAdapter(Class adapter) {
            Object superAdapter = super.getAdapter(adapter);
            if (superAdapter != null) {
                return superAdapter;
            } else {
                // Delegate to the Data Model to find the context.
                if (adapter.isInstance(fDmc)) {
                    return fDmc;
                } else {
                    return fDmc.getAdapter(adapter);
                }
            }
		}

		@Override
		public boolean equals(Object other) {
		    if (this == other) {
		        return true;
		    }
            if (other instanceof RootDMVMContext) {
                RootDMVMContext otherVmc = (RootDMVMContext)other;
                return getVMNode().equals(otherVmc.getVMNode()) && fDmc.equals(otherVmc.fDmc);
            }
            return false;
		}

		@Override
		public int hashCode() {
            return getVMNode().hashCode() + fDmc.hashCode(); 
		}

	}

	private static class SimpleExpression implements IExpression {
		private String fExpressionText;
		SimpleExpression(String text) {
			fExpressionText = text;
		}
	    @Override
		public void dispose() {
		}
	    @Override
		public IDebugTarget getDebugTarget() {
			return null;
		}
	    @Override
		public String getExpressionText() {
			return fExpressionText;
		}
	    @Override
		public IValue getValue() {
			return null;
		}
	    @Override
		public ILaunch getLaunch() {
			return null;
		}
	    @Override
		public String getModelIdentifier() {
			return null;
		}
		@SuppressWarnings("rawtypes")
	    @Override
		public Object getAdapter(Class adapter) {
			return null;
		}
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof SimpleExpression) {
                return fExpressionText.equals(((SimpleExpression) obj).getExpressionText());
            }
            return false;
        }
        @Override
        public int hashCode() {
            return fExpressionText.hashCode();
        }
	}

	private static class SingleExpressionManager {
		private static final IExpression[] NO_EXPRESSIONS = {};
		IExpression fExpression;
		public IExpression[] getExpressions() {
			if (fExpression != null) {
				return new IExpression[] { fExpression };
			}
			return NO_EXPRESSIONS;
		}
		public void setExpression(IExpression expression) {
			fExpression = expression;
		}

	}

	/** Local reference to the expression manager */ 
    private final SingleExpressionManager fManager;
    
    public SingleExpressionVMNode(ExpressionVMProvider provider) {
        super(provider);
        fManager = new SingleExpressionManager();
    }

	@Override
    public String toString() {
        return "SingleExpressionVMNode";  //$NON-NLS-1$ 
    }
    
    private ExpressionVMProvider getExpressionVMProvider() {
        return (ExpressionVMProvider)getVMProvider();
    }

    @Override
	public void update(IHasChildrenUpdate[] updates) {
        // Test availability of children based on whether there are any expressions 
        // in the manager.  We assume that the getExpressions() will just read 
        // local state data, so we don't bother using a job to perform this 
        // operation.
        for (int i = 0; i < updates.length; i++) {
            updates[i].setHasChilren(fManager.getExpressions().length != 0);
            updates[i].done();
        }
    }
    
    @Override
	public void update(IChildrenCountUpdate[] updates) {
        for (IChildrenCountUpdate update : updates) {
            if (!checkUpdate(update)) continue;

            // We assume that the getExpressions() will just read local state data,
            // so we don't bother using a job to perform this operation.
            update.setChildCount(fManager.getExpressions().length);
            update.done();
        }
    }
    
    @Override
	public void update(final IChildrenUpdate[] updates) {
        for (IChildrenUpdate update : updates) {
            doUpdateChildren(update);
        }        
    }
    
    public void doUpdateChildren(final IChildrenUpdate update) {
        final IExpression[] expressions = fManager.getExpressions();
        
        // For each (expression) element in update, find the layout node that can 
        // parse it.  And for each expression that has a corresponding layout node, 
        // call IExpressionLayoutNode#getElementForExpression to generate a VMC.
        // Since the last is an async call, we need to create a multi-RM to wait
        // for all the calls to complete.
        final CountingRequestMonitor multiRm = new ViewerCountingRequestMonitor(getVMProvider().getExecutor(), update);
        int multiRmCount = 0;
        
        int lowOffset= update.getOffset();
        if (lowOffset < 0) {
        	lowOffset = 0;
        }
		int length= update.getLength();
		if (length <= 0) {
			length = expressions.length;
		}
		final int highOffset= lowOffset + length;
		for (int i = lowOffset; i < highOffset && i < expressions.length + 1; i++) {
            if (i < expressions.length) {
                multiRmCount++;
                final int childIndex = i;
                final IExpression expression = expressions[i];
                // getElementForExpression() accepts a IElementsUpdate as an argument.
                // Construct an instance of VMElementsUpdate which will call a 
                // the request monitor when it is finished.  The request monitor
                // will in turn set the element in the update argument in this method. 
                ((ExpressionVMProvider)getVMProvider()).update(
                    new VMExpressionUpdate(
                        update, expression,
                        new DataRequestMonitor<Object>(getVMProvider().getExecutor(), multiRm) {
                            @Override
                            protected void handleSuccess() {
                                update.setChild(getData(), childIndex);
                                multiRm.done();
                            } 
                            
                            @Override
                            protected void handleError() {
                                update.setChild(new InvalidExpressionVMContext(SingleExpressionVMNode.this, expression), childIndex);                                
                                multiRm.done();
                            }
                        })
                    );
            }
        }

        // If no expressions were parsed, we're finished.
        // Set the count to the counting RM.
        multiRm.setDoneCount(multiRmCount);
    }

    @Override
    public void update(ILabelUpdate[] updates) {
        // The label update handler only handles labels for the invalid expression VMCs.
        // The expression layout nodes are responsible for supplying label providers 
        // for their VMCs.
        for (ILabelUpdate update : updates) {
        	update.done();
        }
    }

    @Override
    public int getDeltaFlags(Object event) {
        int retVal = 0;

        // Add a flag if the list of expressions in the global expression manager has changed.
        if (event instanceof ExpressionsChangedEvent) {
            retVal |= IModelDelta.ADDED | IModelDelta.REMOVED | IModelDelta.INSERTED | IModelDelta.CONTENT ;
        }

        for (IExpression expression : fManager.getExpressions()) {
            retVal |= getExpressionVMProvider().getDeltaFlagsForExpression(expression, event);
        }
        
        return retVal;
    }

    @Override
    public void buildDelta(final Object event, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
        if (event instanceof ExpressionsChangedEvent) {
            buildDeltaForExpressionsChangedEvent((ExpressionsChangedEvent)event, parentDelta, nodeOffset, requestMonitor);
        } else {
        
            // For each expression, find its corresponding node and ask that
            // layout node for its delta flags for given event.  If there are delta flags to be 
            // generated, call the asynchronous method to do so.
            CountingRequestMonitor multiRm = new CountingRequestMonitor(getExecutor(), requestMonitor);
            
            int buildDeltaForExpressionCallCount = 0;
            
            IExpression[] expressions = fManager.getExpressions();
            for (int i = 0; i < expressions.length; i++ ) {
                int flags = getExpressionVMProvider().getDeltaFlagsForExpression(expressions[i], event);
                // If the given expression has no delta flags, skip it.
                if (flags == IModelDelta.NO_CHANGE) continue;
    
                int elementOffset = nodeOffset >= 0 ? nodeOffset + i : -1;
                getExpressionVMProvider().buildDeltaForExpression(
                    expressions[i], elementOffset, event, parentDelta, getTreePathFromDelta(parentDelta), 
                    new RequestMonitor(getExecutor(), multiRm));
                buildDeltaForExpressionCallCount++;
            }
            
            multiRm.setDoneCount(buildDeltaForExpressionCallCount);
        }
    }
    
    private void buildDeltaForExpressionsChangedEvent(ExpressionsChangedEvent event, VMDelta parentDelta, 
        int nodeOffset, RequestMonitor requestMonitor) 
    {
        CountingRequestMonitor multiRm = new CountingRequestMonitor(getExecutor(), requestMonitor);
        for (int i = 0; i < event.getExpressions().length; i++) {
            int expIndex = event.getIndex() != -1 
                ? nodeOffset + event.getIndex() + i 
                : -1; 
            getExpressionVMProvider().buildDeltaForExpression(
                event.getExpressions()[i], expIndex, event, parentDelta, getTreePathFromDelta(parentDelta), 
                new RequestMonitor(getExecutor(), multiRm));
        }
        multiRm.setDoneCount(event.getExpressions().length);
    }
        
    private TreePath getTreePathFromDelta(IModelDelta delta) {
        List<Object> elementList = new LinkedList<Object>();
        IModelDelta listDelta = delta;
        elementList.add(0, listDelta.getElement());
        while (listDelta.getParentDelta() != null) {
            elementList.add(0, listDelta.getElement());
            listDelta = listDelta.getParentDelta();
        }
        return new TreePath(elementList.toArray());
    }

	protected void updateElementsInSessionThread(IChildrenUpdate update) {
        doUpdateChildren(update);
	}

	public IDMVMContext createVMContext(IDMContext dmc) {
        return new RootDMVMContext(getVMProvider().getRootVMNode(), dmc);
    }

    public void setExpression(IExpressionDMContext dmc) {
		String text = dmc.getExpression();
		fManager.setExpression(new SimpleExpression(text));
    }
}
