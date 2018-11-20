/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format example (Bug 202556)
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.ui.viewmodel;

import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.AbstractDebugVMAdapter;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController;
//import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.ExpressionVMProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.RegisterVMProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMProvider;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.examples.dsf.pda.ui.viewmodel.launch.PDALaunchVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;

@ThreadSafe
@SuppressWarnings("restriction")
public class PDAVMAdapter extends AbstractDebugVMAdapter {
	public PDAVMAdapter(DsfSession session, SteppingController controller) {
		super(session, controller);
		getSession().registerModelAdapter(IColumnPresentationFactory.class, this);
	}

	@Override
	public void dispose() {
		getSession().unregisterModelAdapter(IColumnPresentationFactory.class);
		super.dispose();
	}

	@Override
	protected AbstractDMVMProvider createViewModelProvider(IPresentationContext context) {
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId())) {
			return new PDALaunchVMProvider(this, context, getSession());
		} else if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(context.getId())) {
			return new VariableVMProvider(this, context, getSession());
		} else if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(context.getId())) {
			return new PDAExpressionVMProvider(this, context, getSession());
		} else if (IDebugUIConstants.ID_REGISTER_VIEW.equals(context.getId())) {
			return new RegisterVMProvider(this, context, getSession());
		}
		return null;
	}
}
