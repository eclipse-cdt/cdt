/*******************************************************************************
 * Copyright (c) 2007, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson           - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.ExprMetaGetValueInfo;

public class ExprMetaGetValue extends ExprMetaCommand<ExprMetaGetValueInfo> {

	public ExprMetaGetValue(FormattedValueDMContext ctx) {
		super(ctx);
	}
	
    @Override
    public String toString() {
    	IExpressionDMContext exprDmc = DMContexts.getAncestorOfType(getContext(), IExpressionDMContext.class);
    	if (exprDmc != null) {
    		return getClass().getSimpleName() + "(\"" + //$NON-NLS-1$
    				exprDmc.getExpression() + "\", " + //$NON-NLS-1$
    				((FormattedValueDMContext)getContext()).getFormatID() + ")"; //$NON-NLS-1$
    	} else {
    		return super.toString();
    	}
    }
}
