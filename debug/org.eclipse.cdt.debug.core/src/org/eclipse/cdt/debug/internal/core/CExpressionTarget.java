/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.core; 

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CExpression;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

/**
 * Implements the expression evaluation target.
 */
public class CExpressionTarget {

	private CDebugTarget fDebugTarget;
	private Map fExpressions = null;
	
	public CExpressionTarget( CDebugTarget target ) {
		fDebugTarget = target;
		fExpressions = new HashMap( 10 );
	}

	public CDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	public IValue evaluateExpression( ICDIStackFrame context, String expressionText ) throws DebugException {
		CExpression expression = (CExpression)fExpressions.remove( expressionText );
		if ( expression != null ) {
			expression.dispose();
		}
		expression = (CExpression)CDIDebugModel.createExpression( getDebugTarget(), expressionText );
		fExpressions.put( expressionText, expression );
		return expression.getValue(context);
		
	}

	public void dispose() {
		Iterator it = fExpressions.values().iterator();
		while( it.hasNext() ) {
			((CExpression)it.next()).dispose();
		}
		fExpressions.clear();
	}
}
