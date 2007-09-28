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
package org.eclipse.dd.dsf.debug.ui.viewmodel.expression;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.dd.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.MultiRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.debug.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.IVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.VMElementsUpdate;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMLayoutNode;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.jface.viewers.TreePath;

/**
 * 
 */
@SuppressWarnings("restriction")
public abstract class AbstractExpressionLayoutNode<V extends IDMData> extends AbstractDMVMLayoutNode<V> 
    implements IExpressionLayoutNode
{
    
    public AbstractExpressionLayoutNode(AbstractVMProvider provider, DsfSession session, Class<? extends IDMContext<V>> dmcClassType) {
        super(provider, session, dmcClassType);
    }

    public void getElementForExpression(final IChildrenUpdate update, final String expressionText, final IExpression expression) {
        final int exprLength = getExpressionLength(expressionText);
        if (exprLength < 0) {
            update.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR, "Invalid expression", null)); //$NON-NLS-1$
            update.done();
            return;
        }
        
        final String nodeExpressionText = exprLength > 0 ? expressionText.substring(0, exprLength) : expressionText; 
        
        getElementForExpressionPart(
            update, nodeExpressionText, 
            new DataRequestMonitor<Object>(getExecutor(), null) {
                @Override
                protected void handleOK() {
                    /**
                     * If the current expression is the whole expression from the argument, 
                     * return the VMC.  Otherwise, call the child nodes to continue evaluating
                     * the expression.
                     */
                    if (exprLength == expressionText.length()) {
                        Object element = getData();
                        associateExpression(element, expression);
                        update.setChild(element, 0);
                        update.done();
                    } else {
                        getVMContextForExpressionFromChildNodes(
                            update, getData(), expressionText.substring(exprLength), expression);
                    }
                }
                
                @Override
                protected void handleErrorOrCancel() {
                    update.setStatus(getStatus());
                    update.done();
                }
            });
    }

    protected void getElementForExpressionPart(final IChildrenUpdate update, final String expressionPartText, final DataRequestMonitor<Object> rm) {
        updateElements(new VMElementsUpdate(
            update, -1, -1,
            new DataRequestMonitor<List<Object>>(getExecutor(), rm) {
                @Override
                protected void handleOK() {
                    if (getData().size() == 0) {
                        rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR, "No contexts", null)); //$NON-NLS-1$
                        rm.done();
                    } else {
                        final List<Object> elements = getData();

                        final MultiRequestMonitor<DataRequestMonitor<Boolean>> multiRm = new MultiRequestMonitor<DataRequestMonitor<Boolean>>(getExecutor(), rm) {
                            @Override
                            protected void handleOK() {
                                boolean foundMatchingContext = false;
                                for (int i = 0; i < getRequestMonitors().size(); i++) {
                                    if (getRequestMonitors().get(i).getData()) {
                                        rm.setData(elements.get(i));
                                        foundMatchingContext = true;
                                        break;
                                    }
                                }
                                if (!foundMatchingContext) {
                                    rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR, "Context not found", null)); //$NON-NLS-1$
                                }
                                rm.done();
                            }
                        }; 
                            
                        for (Object element : elements) {
                            testContextForExpression(
                                element, expressionPartText, 
                                multiRm.add(
                                    new DataRequestMonitor<Boolean>(getExecutor(), null) { 
                                        @Override
                                        protected void handleCompleted() {
                                            multiRm.requestMonitorDone(this);
                                        }
                                    }));
                        }
                    }
                }
                
                @Override
                protected void handleErrorOrCancel() {
                    update.setStatus(getStatus());
                    update.done();
                }
            })
        );
    }

    
    @ConfinedToDsfExecutor("#getSession#getExecutor")
    protected abstract void testContextForExpression(Object element, final String expression, final DataRequestMonitor<Boolean> rm);
    protected abstract void associateExpression(Object element, IExpression expression);
        
    protected void getVMContextForExpressionFromChildNodes(final IChildrenUpdate update, Object parentElement, String childExpression, IExpression expression) {
        IChildrenUpdate childUpdate = new ChildExpressionElementUpdate(
            update, update.getElementPath().createChildPath(parentElement),
            new DataRequestMonitor<List<Object>>(getExecutor(), null) {
                @Override
                protected void handleOK() {
                    update.setChild(getData().get(0), 0);
                    update.done();
                }
                @Override
                protected void handleErrorOrCancel() {
                    update.setStatus(getStatus());
                    update.done();
                }
            });
        
        
        for (int i = 0; i < getChildLayoutNodes().length; i++) {
            if (getChildLayoutNodes()[i] instanceof IExpressionLayoutNode) {
                IExpressionLayoutNode childNode = (IExpressionLayoutNode)getChildLayoutNodes()[i];
                if (childNode.getExpressionLength(childExpression) > 0) {
                    // The child node will call update.done();
                    childNode.getElementForExpression(childUpdate, childExpression, expression);
                    return;
                }
            }
        }

        // If we didn't find a matching child node in the for loop above, return an error.
        update.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR, "Invalid expression", null)); //$NON-NLS-1$
        update.done();

    }
    
    public int getDeltaFlagsForExpression(String expressionText, Object event) {
        int exprLength = getExpressionLength(expressionText);
        if (exprLength >= 0) {
            if (exprLength == expressionText.length()) {
                return getDeltaFlags(event);
            } else {
                int retVal = getDeltaFlagsForExpressionPart(event);
                String childExpression = expressionText.substring(exprLength);
                for (int i = 0; i < getChildLayoutNodes().length; i++) {
                    if (getChildLayoutNodes()[i] instanceof IExpressionLayoutNode) {
                        IExpressionLayoutNode exprNode = (IExpressionLayoutNode)getChildLayoutNodes()[i];
                        if (exprNode.getExpressionLength(childExpression) > 0) {
                            // The child node will call update.done();
                            retVal |= exprNode.getDeltaFlagsForExpression(childExpression, event);
                        }
                    }
                }
                return retVal;
            }
        }

        return IModelDelta.NO_CHANGE;
    }

    protected abstract int getDeltaFlagsForExpressionPart(Object event);
    
    public void buildDeltaForExpression(final IExpression expression, final int elementIdx, final String expressionText, final Object event, final VMDelta parentDelta, final TreePath path, final RequestMonitor rm) 
    {
        // Find the expression part that belong to this node.  If expression 
        // is not recognized, do nothing.
        final int exprLength = getExpressionLength(expressionText);
        if (exprLength < 0) {
            rm.done();
            return;
        }
        
        final String nodeExpressionText = exprLength > 0 ? expressionText.substring(0, exprLength) : expressionText; 
        
        getElementForExpressionPart(
            new ElementsUpdate(new DataRequestMonitor<List<Object>>(getExecutor(), null), path), 
            nodeExpressionText, 
            new DataRequestMonitor<Object>(getExecutor(), null) {
                @Override
                protected void handleOK() {
                    if (exprLength == expressionText.length()) {
                        associateExpression(getData(), expression);
                        buildDeltaForExpressionElement(getData(), elementIdx, event, parentDelta, rm);
                    } else {
                        TreePath newPath = path.createChildPath(getData());
                        callChildExpressionNodesToBuildDelta(
                            expression, elementIdx, expressionText.substring(exprLength), event, parentDelta, newPath, rm);
                    }
                }
                
                @Override
                protected void handleErrorOrCancel() {
                    // There is no matching element for given expression.  That's OK, it just 
                    // means that the expression is invalid.
                    rm.done();
                }
            });
    }

    protected void buildDeltaForExpressionElement(Object element, int elementIdx, Object event, VMDelta parentDelta, final RequestMonitor rm) 
    {
        // Find the child nodes that have deltas for the given event. 
        final Map<IVMLayoutNode,Integer> childNodesWithDeltaFlags = getChildNodesWithDeltaFlags(event);

        // If no child layout nodes have deltas we can stop here. 
        if (childNodesWithDeltaFlags.size() == 0) {
            rm.done();
            return;
        }            
        
        callChildNodesToBuildDelta(
            childNodesWithDeltaFlags, parentDelta.addNode(element, elementIdx, IModelDelta.NO_CHANGE), event, rm);
    }

    protected void callChildExpressionNodesToBuildDelta(IExpression expression, int elementIdx, String expressionRemainder, Object event, VMDelta parentDelta, TreePath path, final RequestMonitor rm) 
    {
        final CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), null) {
            @Override
            protected void handleCompleted() {
                rm.done();
            }  
        };
        
        int childRmCount = 0;
        
        for (int i = 0; i < getChildLayoutNodes().length; i++) {
            if (getChildLayoutNodes()[i] instanceof IExpressionLayoutNode) {
                IExpressionLayoutNode childNode = (IExpressionLayoutNode)getChildLayoutNodes()[i];
                if (childNode.getExpressionLength(expressionRemainder) > 0 &&
                    childNode.getDeltaFlagsForExpression(expressionRemainder, event) != IModelDelta.NO_CHANGE)
                {
                    childNode.buildDeltaForExpression(
                        expression, elementIdx, expressionRemainder, event, parentDelta, path, countingRm);
                    childRmCount++;
                    // The child node will call update.done();
                }
            }
        }
        
        if (childRmCount > 0) {
            countingRm.setCount(childRmCount);
        } else {            
            countingRm.done();
        }
    }

    
    class ChildExpressionElementUpdate extends VMElementsUpdate {
        private final TreePath fPath;
        
        ChildExpressionElementUpdate(IChildrenUpdate clientUpdate, TreePath path, DataRequestMonitor<List<Object>> rm) {
            super(clientUpdate, 0, 1, rm);
            fPath = path;
        }

        @Override
        public Object getElement() {
            return fPath.getLastSegment();
        }
        
        @Override
        public TreePath getElementPath() {
            return fPath;
        }
    }
}
