/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.ui.viewmodel.expression;

import java.util.List;

import org.eclipse.dd.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.dd.dsf.debug.ui.viewmodel.expression.ExpressionVMProvider.ExpressionsChangedEvent;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.IVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.VMElementsUpdate;
import org.eclipse.dd.dsf.ui.viewmodel.update.VMCacheManager;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * This is the top-level layout node in the expressions view.  Its job is to:
 * <li>
 *   <ol> retrieve the {@link IExpression} objects from the global {@link IExpressionManager},</ol>
 *   <ol> retrieve the expression string from the <code>IExpression</code> object,</ol>
 *   <ol> then to call the configured expression nodes to parse the expression string.</ol>
 * </li>
 * <p>
 * This node is not intended to have any standard child layout nodes, therefore 
 * the implementation of {@link #setChildNodes(IVMLayoutNode[])} throws an exception.  
 * Instead users should call {@link #setExpressionLayoutNodes(IExpressionLayoutNode[])}
 * to configure layout nodes that this node will delegate to when processing expressions.
 * </p> 
 */
@SuppressWarnings("restriction")
public class ExpressionManagerLayoutNode extends AbstractVMLayoutNode
    implements IElementLabelProvider, IElementEditor
{

    /**
     * VMC of an expression object that failed to get parsed by any of the 
     * configured expression layout nodes.  It is only used to display an
     * error message in the view, and to allow the user to edit the 
     * expression.
     */
    private class InvalidExpressionVMC extends AbstractVMContext {
        final IExpression fExpression;
        
        public InvalidExpressionVMC(IExpression expression) {
            super(getVMProvider().getVMAdapter(), ExpressionManagerLayoutNode.this);
            fExpression = expression;
        }

        @Override
        @SuppressWarnings("unchecked") 
        public Object getAdapter(Class adapter) {
            if (adapter.isAssignableFrom(fExpression.getClass())) {
                return fExpression;
            } else {
                return super.getAdapter(adapter);
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            return obj instanceof InvalidExpressionVMC && ((InvalidExpressionVMC)obj).fExpression.equals(fExpression);
        }
        
        @Override
        public int hashCode() {
            return fExpression.hashCode();
        }
    }
    
    /** Array of expression nodes which parse the user expressions and handle model events */ 
    private IExpressionLayoutNode[] fExpressionNodes = new IExpressionLayoutNode[0];
    
    /** Local reference to the global expression manager */ 
    private IExpressionManager fManager = DebugPlugin.getDefault().getExpressionManager();
    
    /** Cached reference to a cell modifier for editing expression strings of invalid expressions */
    private WatchExpressionCellModifier fWatchExpressionCellModifier = new WatchExpressionCellModifier();
    
    public ExpressionManagerLayoutNode(AbstractVMProvider provider) {
        super(provider);
    }

    public void updateHasElements(IHasChildrenUpdate[] updates) {
        // Test availability of children based on whether there are any expressions 
        // in the manager.  We assume that the getExpressions() will just read 
        // local state data, so we don't bother using a job to perform this 
        // operation.
        for (int i = 0; i < updates.length; i++) {
            updates[i].setHasChilren(fManager.getExpressions().length != 0);
            updates[i].done();
        }
    }
    
    public void updateElementCount(IChildrenCountUpdate update) {
        update.setChildCount(fManager.getExpressions().length);
        update.done();
    }
    
    public void updateElements(final IChildrenUpdate update) {
        final IExpression[] expressions = fManager.getExpressions();

        // For each (expression) element in update, find the layout node that can 
        // parse it.  And for each expression that has a corresponding layout node, 
        // call IExpressionLayoutNode#getElementForExpression to generate a VMC.
        // Since the last is an async call, we need to create a multi-RM to wait
        // for all the calls to complete.
        final CountingRequestMonitor multiRm = new CountingRequestMonitor(getExecutor(), null) {
            @Override
            protected void handleCompleted() {
                update.done();
            }  
        };
        
        int expressionRmCount = 0;
        for (int i = update.getOffset(); i < update.getOffset() + update.getLength() && i < expressions.length; i++) {
            
            // Check the array boundaries as the expression manager could change asynchronously.  
            // The expression manager change should lead to a refresh in the view. 
            if (i > expressions.length) {
                continue;
            }
            
            final String expressionText = expressions[i].getExpressionText();
            final int expressionIdx = i;
            final IExpression expression = expressions[i];
            IExpressionLayoutNode expressionNode = findNodeForExpression(expressionText);
            if (expressionNode == null) {
                update.setChild(new InvalidExpressionVMC(expression), i);
            } else {
                expressionRmCount++;
                // getElementForExpression() accepts a IElementsUpdate as an argument.
                // Construct an instance of VMElementsUpdate which will call a 
                // the request monitor when it is finished.  The request monitor
                // will in turn set the element in the update argument in this method. 
                VMElementsUpdate expressionElementUpdate = new VMElementsUpdate(
                    update, 0, 1,
                    new DataRequestMonitor<List<Object>>(getExecutor(), multiRm) {
                        @Override
                        protected void handleOK() {
                            update.setChild(getData().get(0), expressionIdx);
                            multiRm.done();
                        } 
                        
                        @Override
                        protected void handleError() {
                            update.setChild(new InvalidExpressionVMC(expression), expressionIdx);
                            multiRm.done();
                        }
                    });
                expressionNode.getElementForExpression(expressionElementUpdate, expressionText, expression);
            }
        }
        
        // Set the count to the counting RM.
        multiRm.setDoneCount(expressionRmCount);
    }

    public void update(ILabelUpdate[] updates) {
        // The label update handler only handles labels for the invalid expression VMCs.
        // The expression layout nodes are responsible for supplying label providers 
        // for their VMCs.
        for (ILabelUpdate update : updates) {
            if (update.getElement() instanceof InvalidExpressionVMC) {
                updateInvalidExpressionVMCLabel(update, (InvalidExpressionVMC) update.getElement());
            } else {
                update.done();
            }
        }
    }

    /**
     * Updates the label for the InvalidExpressionVMC.
     */
    private void updateInvalidExpressionVMCLabel(ILabelUpdate update, InvalidExpressionVMC vmc) {
        String[] columnIds = update.getColumnIds() != null ? 
            update.getColumnIds() : new String[] { IDebugVMConstants.COLUMN_ID__NAME };
            
        for (int i = 0; i < columnIds.length; i++) {
            if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(columnIds[i])) {
                update.setLabel(vmc.fExpression.getExpressionText(), i);
                update.setImageDescriptor(DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_EXPRESSION ), i);
            } else if (IDebugVMConstants.COLUMN_ID__NAME.equals(columnIds[i])) {
                update.setLabel(vmc.fExpression.getExpressionText(), i);
                update.setImageDescriptor(DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_EXPRESSION ), i);
            } else if (IDebugVMConstants.COLUMN_ID__VALUE.equals(columnIds[i])) {
                update.setLabel(MessagesForExpressionVM.ExpressionManagerLayoutNode__invalidExpression_valueColumn_label, i);
            } else {
                update.setLabel("", i); //$NON-NLS-1$
            }
        }
        
        
        update.done();
    }
    
    /**
     * Convenience call that iterates through all the configured expression
     * layout nodes and finds the first one that can parse the given expression.
     */
    private IExpressionLayoutNode findNodeForExpression(String expressionText) {
        for (IExpressionLayoutNode node : fExpressionNodes) {
            if (node.getExpressionLength(expressionText) > 0) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * ExpressionManagerLayoutNode does not support child layout nodes.
     * @see #setExpressionLayoutNodes(IExpressionLayoutNode[])
     */
    @Override
    public void setChildNodes(IVMLayoutNode[] childNodes) {
        throw new UnsupportedOperationException("This node does not support children."); //$NON-NLS-1$
    }
    
    /**
     * Configures the set of expression layout nodes that the expression manager layout
     * node will use to parse the expressions.  
     * <p>
     * <i>Note: The nodes specified in the array will be called to parse expressions, 
     * in the order as they are in the array</i>.  Therefore if one node is a "greedy" 
     * parser, and will accept any expression string, it should appear last in the list
     * of the nodes. 
     * </p>
     * @param nodes Array of expression layout nodes to configure with the manager.
     */
    public void setExpressionLayoutNodes(IExpressionLayoutNode[] nodes) {
        fExpressionNodes = nodes;
    }
    
    @Override
    public void dispose() {
        
        for (IExpressionLayoutNode exprNode : fExpressionNodes) {
            exprNode.dispose();
        }
        super.dispose();
    }
    
    @Override
    public int getDeltaFlags(Object event) {
        int retVal = 0;
        
        // Add a flag if the list of expressions in the global expression manager has changed.
        if (event instanceof ExpressionsChangedEvent) {
            retVal |= IModelDelta.CONTENT;
        }

        // If any of the expressions nodes have delta flags, that means that this 
        // node probably needs to generate a delta as well.  Ideally, we would call 
        // IExpressionLayoutNode.getDeltaFlagsForExpression() here, but getDeltaFlags()
        // is an optimization call anyway, and it's OK if it generates some false 
        // positives.  We will call getDeltaFlagsForExpression in buildDelta() instead..
        for (IExpressionLayoutNode node : fExpressionNodes) {
            retVal |= node.getDeltaFlags(event);
        }
        
        return retVal;
    }

    @Override
    public void buildDelta(final Object event, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
        // Add a flag if the list of expressions has changed.
        if (event instanceof ExpressionsChangedEvent) {
            VMCacheManager.getVMCacheManager().flush(super.getVMProvider().getPresentationContext());
            parentDelta.addFlags(IModelDelta.CONTENT);
        }
        
        // Once again, for each expression, find its corresponding layout node and ask that
        // layout node for its delta flags for given event.  If there are delta flags to be 
        // generated, call the asynchronous method to do so.
        CountingRequestMonitor multiRm = new CountingRequestMonitor(getExecutor(), requestMonitor); 
        int buildDeltaForExpressionCallCount = 0;
        
        IExpression[] expressions = fManager.getExpressions();
        for (int i = 0; i < expressions.length; i++ ) {
            String expressionText = expressions[i].getExpressionText();
            IExpressionLayoutNode node = findNodeForExpression(expressionText);
            if (node == null) continue;
            
            int flags = node.getDeltaFlagsForExpression(expressionText, event); 
            
            // If the given node has no delta flags, skip it.
            if (flags == IModelDelta.NO_CHANGE) continue;
            
            node.buildDeltaForExpression(expressions[i], i + nodeOffset, expressionText, event, parentDelta, 
                                         getTreePathFromDelta(parentDelta), 
                                         new RequestMonitor(getExecutor(), multiRm));
            buildDeltaForExpressionCallCount++;
        }
        
        multiRm.setDoneCount(buildDeltaForExpressionCallCount);
    }
        
    
    public CellEditor getCellEditor(IPresentationContext context, String columnId, Object element, Composite parent) {
        if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(columnId)) {
            return new TextCellEditor(parent);
        } 
        return null;
    }
    
    public ICellModifier getCellModifier(IPresentationContext context, Object element) {
        return fWatchExpressionCellModifier;
    }
}
