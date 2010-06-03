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

import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.DefaultVMContentProviderStrategy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * The IElementContentProvider implementation to be used with an expression
 * view model provider.
 * 
 * @see ExpressionVMProvider
 */
public class ExpressionVMProviderContentStragegy extends DefaultVMContentProviderStrategy {
    public ExpressionVMProviderContentStragegy(ExpressionVMProvider provider) {
        super(provider);
    }

    private ExpressionVMProvider getExpressionVMProvider() {
        return (ExpressionVMProvider)getVMProvider();
    }

    public void update(final IExpressionUpdate update) {
        final IExpressionVMNode matchingNode = 
            getExpressionVMProvider().findNodeToParseExpression(null, update.getExpression());

        if (matchingNode != null) {
            updateExpressionWithNode(matchingNode, update);
        } else {
            update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Cannot parse expression", null)); //$NON-NLS-1$
            update.done();
        }
    }        

    private void updateExpressionWithNode(final IExpressionVMNode node, final IExpressionUpdate update) {
        // Call the expression node to parse the expression and fill in the value.
        node.update(
            new VMExpressionUpdate(
                update, update.getExpression(),
                new ViewerDataRequestMonitor<Object>(getVMProvider().getExecutor(), update) {
                    @Override
                    protected void handleSuccess() {
                        // Check if the evaluated node has child expression nodes.  
                        // If it does, check if any of those nodes can evaluate the given
                        // expression further.  If they can, call the child node to further
                        // process the expression.  Otherwise we found our element and 
                        // we're done. 
                        final IExpressionVMNode matchingNode = getExpressionVMProvider().
                            findNodeToParseExpression(node, update.getExpression());
                        
                        if (matchingNode != null && !matchingNode.equals(node)) {
                            updateExpressionWithNode(
                                matchingNode,
                                new VMExpressionUpdate(
                                    update.getElementPath().createChildPath(getData()), update.getViewerInput(), 
                                    update.getPresentationContext(), update.getExpression(), 
                                    new ViewerDataRequestMonitor<Object>(getVMProvider().getExecutor(), update) {
                                        
                                        @Override
                                        protected void handleSuccess() {
                                            update.setExpressionElement(getData());
                                            update.done();
                                        }
                                    })
                                );
                        } else {
                            update.setExpressionElement(getData());
                            update.done();
                        }
                    } 
                })
            );
    }        
}
