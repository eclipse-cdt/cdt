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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;

/**
 * 
 */
public class WatchExpressionDelegate implements IWatchExpressionDelegate {
    public void evaluateExpression(final String expression, IDebugElement context, IWatchExpressionListener listener) {
        listener.watchEvaluationFinished(new IWatchExpressionResult() {
            public String[] getErrorMessages() { return new String[0]; }
            public DebugException getException() { return null; }
            public String getExpressionText() { return expression; }
            public IValue getValue() { return null; }
            public boolean hasErrors() { return false; }
        });
    }
}
