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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.dd.dsf.concurrent.MultiRequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.internal.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.dd.dsf.ui.viewmodel.VMChildrenUpdate;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;

/**
 * 
 */
@SuppressWarnings("restriction")
public abstract class AbstractExpressionVMNode extends AbstractDMVMNode 
    implements IExpressionVMNode
{
    
    public AbstractExpressionVMNode(AbstractDMVMProvider provider, DsfSession session, Class<? extends IDMContext> dmcClassType) {
        super(provider, session, dmcClassType);
    }

    public void update(final IExpressionUpdate update) {
        if (!canParseExpression(update.getExpression())) {
            update.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Invalid expression", null)); //$NON-NLS-1$
            update.done();
            return;
        }
        
        update(new IChildrenUpdate[] { new VMChildrenUpdate(
            update, -1, -1,
            new ViewerDataRequestMonitor<List<Object>>(getExecutor(), update) {
                @Override
                protected void handleOK() {
                    if (getData().size() == 0) {
                        update.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "No contexts", null)); //$NON-NLS-1$
                        update.done();
                    } else {
                        final List<Object> elements = getData();

                        final MultiRequestMonitor<DataRequestMonitor<Boolean>> multiRm = new MultiRequestMonitor<DataRequestMonitor<Boolean>>(getExecutor(), null) {
                            @Override
                            protected void handleCompleted() {
                                if (getStatus().isOK()) {
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
                                        update.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Context not found", null)); //$NON-NLS-1$
                                    }
                                } else {
                                    update.setStatus(getStatus());
                                }
                                update.done();
                            }
                        }; 
                            
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
                    }
                }
                
                @Override
                protected void handleCancelOrErrorOrWarning() {
                    update.setStatus(getStatus());
                    update.done();
                }
            })}
        );

    }
    
    
    @ConfinedToDsfExecutor("#getSession#getExecutor")
    protected void testElementForExpression(Object element, IExpression expression, final DataRequestMonitor<Boolean> rm) {
        rm.setData(false);
        rm.done();
    }
    
    protected void associateExpression(Object element, IExpression expression) {
    }
        
}
