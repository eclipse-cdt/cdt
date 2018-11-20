/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia (QNX)- Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpointactions;

import org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 8.0
 */
public class CLICommandActionPage extends PlatformObject implements IBreakpointActionPage {
	private CLICommandAction cliCommandAction;
	private CLICommandActionComposite editor;

	public CLICommandAction getCLICommandAction() {
		return cliCommandAction;
	}

	@Override
	public void actionDialogCanceled() {
	}

	@Override
	public void actionDialogOK() {
		cliCommandAction.setCommand(editor.getCommand());
	}

	@Override
	public Composite createComposite(IBreakpointAction action, Composite composite, int style) {
		cliCommandAction = (CLICommandAction) action;
		editor = new CLICommandActionComposite(composite, style, this);
		return editor;
	}
}
