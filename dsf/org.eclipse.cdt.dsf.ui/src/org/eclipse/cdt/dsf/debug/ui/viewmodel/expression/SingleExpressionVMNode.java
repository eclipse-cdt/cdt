/*******************************************************************************
 * Copyright (c) 2009, 2015 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
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
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(Class<T> adapter) {
            T superAdapter = super.getAdapter(adapter);
            if (superAdapter != null) {
                return superAdapter;
            } else {
                // Delegate to the Data Model to find the context.
                if (adapter.isInstance(fDmc)) {
                    return (T)fDmc;
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

        @Override
        public String toString() {
            return fDmc.toString();
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
	    @Override
		public <T> T getAdapter(Class<T> adapter) {
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

    public SingleExpressionVMNode(ExpressionVMProvider provider) {
        super(provider);
    }

	@Override
    public String toString() {
        return "SingleExpressionVMNode";  //$NON-NLS-1$ 
    }
    
    private ExpressionVMProvider getExpressionVMProvider() {
        return (ExpressionVMProvider)getVMProvider();
    }

    private IExpressionDMContext getUpdateExpressionDMC(IViewerUpdate update) {
        if (update.getElement() instanceof IDMVMContext) {
            IDMContext dmc = ((IDMVMContext)update.getElement()).getDMContext();
            if (dmc instanceof IExpressionDMContext) {
                return (IExpressionDMContext)dmc;
            }
        }
        return null;
    }


    @Override
	public void update(IHasChildrenUpdate[] updates) {
        for (int i = 0; i < updates.length; i++) {
            updates[i].setHasChilren(getUpdateExpressionDMC(updates[i]) != null);
            updates[i].done();
        }
    }
    
    @Override
	public void update(IChildrenCountUpdate[] updates) {
        for (IChildrenCountUpdate update : updates) {
            if (!checkUpdate(update)) continue;

            // We assume that the getExpressions() will just read local state data,
            // so we don't bother using a job to perform this operation.
            update.setChildCount(getUpdateExpressionDMC(update) != null ? 1 : 0);
            update.done();
        }
    }
    
    @Override
	public void update(final IChildrenUpdate[] updates) {
        for (IChildrenUpdate update : updates) {
            IExpressionDMContext dmc = getUpdateExpressionDMC(update);
            if (dmc != null) {
                doUpdateChildren(update, new SimpleExpression(dmc.getExpression()));                
            }
            else {
                handleFailedUpdate(update);
            }
        }        
    }
    
    public void doUpdateChildren(final IChildrenUpdate update, final IExpression expression) {
        // getElementForExpression() accepts a IElementsUpdate as an argument.
        // Construct an instance of VMElementsUpdate which will call a 
        // the request monitor when it is finished.  The request monitor
        // will in turn set the element in the update argument in this method. 
        ((ExpressionVMProvider)getVMProvider()).update(
            new VMExpressionUpdate(
                update, expression,
                new ViewerDataRequestMonitor<Object>(getVMProvider().getExecutor(), update) {
                    @Override
                    protected void handleSuccess() {
                        update.setChild(getData(), 0);
                        update.done();
                    } 
                    
                    @Override
                    protected void handleError() {
                        update.setChild(new InvalidExpressionVMContext(SingleExpressionVMNode.this, expression), 0);
                        update.done();
                    }
                })
            );
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

        // The expression in the hover is not known here, so assume that all 
        // expression nodes need to provide delta flags for event.  Iterate 
        // through them here and collect the flags.
        for (IExpressionVMNode node : getExpressionVMProvider().getExpressionNodes()) {
            retVal |= getDeltaFlagsForNode(node, event);
        }
        
        return retVal;
    }

    private int getDeltaFlagsForNode(IVMNode node, Object event) {
        int retVal = node.getDeltaFlags(event);
        for (IVMNode child : getVMProvider().getChildVMNodes(node)) {
            if (!node.equals(child)) {
                retVal |= getDeltaFlagsForNode(child, event);
            }
        }
        return retVal;
    }
    
    @Override
    public void buildDelta(final Object event, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
        if (event instanceof ExpressionsChangedEvent) {
            buildDeltaForExpressionsChangedEvent((ExpressionsChangedEvent)event, parentDelta, nodeOffset, requestMonitor);
        } else {
            Object parent = parentDelta.getElement();
            if (parent instanceof IDMVMContext) {
                IDMContext dmc = ((IDMVMContext)parent).getDMContext();
                if (dmc instanceof IExpressionDMContext) {
                    IExpression expression = new SimpleExpression( ((IExpressionDMContext)dmc).getExpression() );
                    int flags = getExpressionVMProvider().getDeltaFlagsForExpression(expression, event);
                    // If the given expression has no delta flags, skip it.
                    if (flags != IModelDelta.NO_CHANGE) {
                        getExpressionVMProvider().buildDeltaForExpression(
                            expression, nodeOffset, event, parentDelta, getTreePathFromDelta(parentDelta), 
                            requestMonitor);
                        return;
                    }
                }
            }
            requestMonitor.done();
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

	public IDMVMContext createVMContext(IDMContext dmc) {
        return new RootDMVMContext(getVMProvider().getRootVMNode(), dmc);
    }

}
