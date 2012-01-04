/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.expression;

import java.util.List;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.MultiRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.VMChildrenUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;

/**
 * Base class for VM Nodes which can be used in the expressions view.  
 * <p>
 * This base class uses the methods {@link #canParseExpression(IExpression)} and
 * {@link #update(IChildrenUpdate[])} to implement the 
 * {@link IExpressionVMNode#update(IExpressionUpdate)}
 * method.  Two additional abstract protected methods need to be implemented
 * by the sub-class as well.   
 * </p>
 */
public abstract class AbstractExpressionVMNode extends AbstractDMVMNode 
    implements IExpressionVMNode
{
    /**
     * @since 2.0
     */    
    protected static final String PROP_ELEMENT_EXPRESSION = "element_expression";  //$NON-NLS-1$

    
    public AbstractExpressionVMNode(AbstractDMVMProvider provider, DsfSession session, Class<? extends IDMContext> dmcClassType) {
        super(provider, session, dmcClassType);
    }

    @Override
	public void update(final IExpressionUpdate update) {
        if (!canParseExpression(update.getExpression())) {
            // This method should not be called if canParseExpression() returns false.
            // Return an internal error status.
            update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Cannot parse expression", null)); //$NON-NLS-1$
            update.done();
            return;
        }
        
        // Retrieve the list of all elements from the sub-class.  Then compare 
        // each returned element to the expression in the update, using 
        // testElementForExpression().  The element that matches the expression
        // is returned to the client.
        // If no matching element is found, the createInvalidExpressionVMContext() 
        // method is called to a special context.
        update(new IChildrenUpdate[] { new VMChildrenUpdate(
            update, -1, -1,
            new ViewerDataRequestMonitor<List<Object>>(getExecutor(), update) {
                @Override
                protected void handleSuccess() {
                    if (getData().size() == 0) {
                        update.setExpressionElement(createInvalidExpressionVMContext(update.getExpression()));
                        update.done();
                    } else {
                        final List<Object> elements = getData();

                        final MultiRequestMonitor<DataRequestMonitor<Boolean>> multiRm = new MultiRequestMonitor<DataRequestMonitor<Boolean>>(getExecutor(), null) {
                            @Override
                            protected void handleCompleted() {
                                if (isSuccess()) {
                                    boolean foundMatchingContext = false;
                                    for (int i = 0; i < getRequestMonitors().size(); i++) {
                                        if (getRequestMonitors().get(i).getData()) {
                                            Object element = elements.get(i);
                                            associateExpression(element, update.getExpression());
                                            update.setExpressionElement(element);
                                            foundMatchingContext = true;
                                            break;
                                        }
                                    }
                                    if (!foundMatchingContext) {
                                        update.setExpressionElement(createInvalidExpressionVMContext(update.getExpression()));
                                    }
                                } else {
                                    update.setStatus(getStatus());
                                }
                                update.done();
                            }
                        }; 
                        multiRm.requireDoneAdding();
                            
                        for (Object element : elements) {
                            testElementForExpression(
                                element, update.getExpression(), 
                                multiRm.add(
                                    new DataRequestMonitor<Boolean>(getExecutor(), null) { 
                                        @Override
                                        protected void handleCompleted() {
                                            multiRm.requestMonitorDone(this);
                                        }
                                    }));
                        }
                        multiRm.doneAdding();                        
                    }
                }
                
                @Override
                protected void handleFailure() {
                    update.setStatus(getStatus());
                    update.done();
                }
            })}
        );

    }
    
    
    /**
     * Tests whether the given element matches the given expression.
     * 
     * @param element Element to test against the given expression.
     * @param expression Expression to use to check if the element is matching.
     * @param rm The request monitor for the result. 
     */
    @ConfinedToDsfExecutor("#getSession#getExecutor")
    protected void testElementForExpression(Object element, IExpression expression, final DataRequestMonitor<Boolean> rm) {
        rm.setData(false);
        rm.done();
    }
    
    /**
     * Sets the given expression as the expression belonging to the given 
     * element.
     * <p>
     * This base class creates VM context elements using the extending class's 
     * {@link #update(IChildrenUpdate[])} method.  The element matching the 
     * expression is found using {@link #testElementForExpression(Object, IExpression, DataRequestMonitor)}.
     * Once the matching element is found it needs to be linked to the expression
     * so that it can be distinguished from other contexts created for identical
     * but distinct expressions.  This method accomplishes this task.  Elements
     * which are associated with expressions should use the expression object
     * for implementation of {@link #equals(Object)} and {@link #hashCode()}
     * methods. 
     * </p> 
     *  
     * @param element
     * @param expression
     */
    protected void associateExpression(Object element, IExpression expression) {
    }
     
    /**
     * Create a place holder for an invalid expression.  If for a given expression, 
     * this VM node returns true from {@link #canParseExpression(IExpression)}, which
     * indicates that the expression matches the node's expected format, but the node
     * then is not able to find the element represented by the expression, then an 
     * "invalid" expression context needs to be created.  
     * <p>
     * This method can be overriden to provide a node-specific invalid expression 
     * context.
     * </p>
     *
     * @param expression Expression to create the context for.
     * @return Returns a VM context object representing an invalid expression with 
     * 
     * @since 1.1
     */
    protected IVMContext createInvalidExpressionVMContext(IExpression expression) {
        return new InvalidExpressionVMContext(this, expression);
    }
}
