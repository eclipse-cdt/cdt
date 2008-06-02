/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.gdb.internal.ui.viewmodel;

import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.ExpressionVMProvider;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.modules.ModulesVMProvider;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.register.RegisterVMProvider;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.variable.VariableVMProvider;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.gdb.internal.ui.viewmodel.launch.LaunchVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;

/* 
 * 
 */
@ThreadSafe
@SuppressWarnings("restriction")
public class GdbViewModelAdapter extends AbstractDMVMAdapter
{
    public GdbViewModelAdapter(DsfSession session) {
        super(session);
        getSession().registerModelAdapter(IColumnPresentationFactory.class, this);
    }    

    @Override
    public void dispose() {
        getSession().unregisterModelAdapter(IColumnPresentationFactory.class);
        super.dispose();
    }
    
    @Override
    protected AbstractDMVMProvider createViewModelProvider(IPresentationContext context) {
        if ( IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId()) ) {
            return new LaunchVMProvider(this, context, getSession()); 
        } else if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(context.getId()) ) {
            return new VariableVMProvider(this, context, getSession());
        } else if (IDebugUIConstants.ID_REGISTER_VIEW.equals(context.getId()) ) {
            return new RegisterVMProvider(this, context, getSession());
        } else if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(context.getId()) ) {
            return new ExpressionVMProvider(this, context, getSession());
        } else if (IDebugUIConstants.ID_MODULE_VIEW.equals(context.getId()) ) {
            return new ModulesVMProvider(this, context, getSession());
        }
        return null;
    }    
}
