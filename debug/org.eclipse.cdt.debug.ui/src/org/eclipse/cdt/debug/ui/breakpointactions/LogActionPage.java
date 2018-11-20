/*******************************************************************************
 * Copyright (c) 2007, 2012 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
