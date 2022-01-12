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

public class ResumeActionPage extends PlatformObject implements IBreakpointActionPage {

	private ResumeActionComposite resumeComposite;
	private ResumeAction resumeAction;

	public ResumeAction getResumeAction() {
		return resumeAction;
	}

	@Override
	public void actionDialogCanceled() {
	}

	@Override
	public void actionDialogOK() {
		resumeAction.setPauseTime(resumeComposite.getPauseTime());
	}

	@Override
	public Composite createComposite(IBreakpointAction action, Composite composite, int style) {
		resumeAction = (ResumeAction) action;
		resumeComposite = new ResumeActionComposite(composite, style, this);
		return resumeComposite;
	}

}
