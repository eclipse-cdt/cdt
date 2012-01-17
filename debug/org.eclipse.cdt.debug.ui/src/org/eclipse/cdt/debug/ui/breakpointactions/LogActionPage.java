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
package org.eclipse.cdt.debug.ui.breakpointactions;

import org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.swt.widgets.Composite;

public class LogActionPage extends PlatformObject implements IBreakpointActionPage {

	private LogAction logAction;
	private LogActionComposite editor;

	public LogAction getLogAction() {
		return logAction;
	}

	@Override
	public void actionDialogCanceled() {
	}

	@Override
	public void actionDialogOK() {
		logAction.setMessage(editor.getMessage());
		logAction.setEvaluateExpression(editor.getIsExpression());
	}

	@Override
	public Composite createComposite(IBreakpointAction action, Composite composite, int style) {
		logAction = (LogAction) action;
		editor = new LogActionComposite(composite, style, this);
		return editor;
	}

}
