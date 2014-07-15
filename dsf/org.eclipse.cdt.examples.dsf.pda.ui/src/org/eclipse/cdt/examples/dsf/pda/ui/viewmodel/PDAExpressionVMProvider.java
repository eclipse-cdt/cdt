/*****************************************************************
 * Copyright (c) 2011, 2014 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format example (Bug 202556)
 *     Marc Khouzam (Ericsson) - Make use of base class methods for IElementFormatProvider (Bug 439624)
 *****************************************************************/
package org.eclipse.cdt.examples.dsf.pda.ui.viewmodel;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.ExpressionVMProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.IElementFormatProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * PDA View Model provider for the expression view.
 */
@SuppressWarnings("restriction")
public class PDAExpressionVMProvider extends ExpressionVMProvider implements IElementFormatProvider {

	static String myPersistId = "org.eclipse.cdt.examples.dsf.pda.ui.variablePersistable";
	
	public PDAExpressionVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
		super(adapter, context, session);
	}

	@Override
	protected String getElementKey(IVMContext context) {
		if (context instanceof VariableVMNode.VariableExpressionVMC) {
			IExpressionDMContext ctx = DMContexts.getAncestorOfType(((VariableVMNode.VariableExpressionVMC) context).getDMContext(), IExpressionDMContext.class);
			if (ctx != null)
				return ctx.getExpression();
		} else if (context instanceof IDMVMContext) {
			IExpression y = (IExpression) (context).getAdapter(IExpression.class);
			if (y != null)
				return y.getExpressionText();
		}
		return null;
	}
	
	public boolean supportFormat(IVMContext context) {
		if (context instanceof VariableVMNode.VariableExpressionVMC) {
			return true;
		}
		return false;
	}
}
