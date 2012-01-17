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
