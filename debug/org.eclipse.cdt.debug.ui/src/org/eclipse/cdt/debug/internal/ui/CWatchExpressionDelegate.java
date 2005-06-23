/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui; 

import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;
 
public class CWatchExpressionDelegate implements IWatchExpressionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IWatchExpressionDelegate#evaluateExpression(java.lang.String, org.eclipse.debug.core.model.IDebugElement, org.eclipse.debug.core.model.IWatchExpressionListener)
	 */
	public void evaluateExpression( final String expression, IDebugElement context, final IWatchExpressionListener listener ) {
		if ( !(context instanceof ICStackFrame) ) {
			listener.watchEvaluationFinished( null );
			return;
		}
		final ICStackFrame frame = (ICStackFrame)context;
		Runnable runnable = new Runnable() {
			public void run() {
				IValue value = null;
				DebugException de = null;
				try {
					value = frame.evaluateExpression( expression );
				}
				catch( DebugException e ) {
					de = e;
				}
				IWatchExpressionResult result = evaluationComplete( expression, value, de );
				listener.watchEvaluationFinished( result );
			}
		};
		DebugPlugin.getDefault().asyncExec( runnable );
	}

	protected IWatchExpressionResult evaluationComplete( final String expression, final IValue value, final DebugException de ) {
		return new IWatchExpressionResult() {
			
			public IValue getValue() {
				return value;
			}
			
			public boolean hasErrors() {
				return ( de != null );
			}
			
			public String getExpressionText() {
				return expression;
			}
			
			/* (non-Javadoc)
			 * @see org.eclipse.debug.core.model.IWatchExpressionResult#getException()
			 */
			public DebugException getException() {
				return de;
			}

			public String[] getErrorMessages() {
				return ( de != null ) ? new String[] { de.getMessage() } : new String[0];
			}
		};
	}
}
