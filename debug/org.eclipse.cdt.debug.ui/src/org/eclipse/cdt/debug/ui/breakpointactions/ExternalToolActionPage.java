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

public class ExternalToolActionPage extends PlatformObject implements IBreakpointActionPage {

	private ExternalToolAction externalToolAction;
	private ExternalToolActionComposite runComposite;

	@Override
	public void actionDialogCanceled() {
	}

	public ExternalToolAction getExternalToolAction() {
		return externalToolAction;
	}

	@Override
	public void actionDialogOK() {
		externalToolAction.setExternalToolName(runComposite.getLaunchConfigName());
	}

	@Override
	public Composite createComposite(IBreakpointAction action, Composite composite, int style) {
		externalToolAction = (ExternalToolAction) action;
		runComposite = new ExternalToolActionComposite(composite, style, this);
		return runComposite;
	}

}
