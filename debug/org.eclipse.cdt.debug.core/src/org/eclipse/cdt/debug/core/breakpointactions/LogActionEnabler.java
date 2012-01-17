/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.breakpointactions;

import java.util.List;

import org.eclipse.cdt.debug.internal.core.model.CStackFrame;
import org.eclipse.cdt.debug.internal.core.model.CThread;

public class LogActionEnabler implements ILogActionEnabler {

	private CThread thread;

	public LogActionEnabler(CThread thread) {
		this.thread = thread;
	}

	@Override
	public String evaluateExpression(String expression) throws Exception {
		List frames = thread.computeStackFrames();
		CStackFrame frame = (CStackFrame) frames.get(0);

		return frame.evaluateExpressionToString(expression);
	}

}
