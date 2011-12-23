/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.tracepointactions;

import org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction;
import org.eclipse.cdt.debug.ui.breakpointactions.IBreakpointActionPage;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.EvaluateAction;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @since 2.1
 */
public class EvaluateActionPage extends PlatformObject implements IBreakpointActionPage {

	private Text fEvalString;
	private EvaluateAction fEvalAction;

	/**
	 * Create the composite
	 */
	private Composite createEvaluateActionComposite(Composite parent, int style) {
		Composite composite = new Composite(parent, style);
		composite.setLayout(new GridLayout(2, false));

		final Label evalLabel = new Label(composite, SWT.NONE);
		evalLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		evalLabel.setText(MessagesForTracepointActions.TracepointActions_Evaluate_Label);

		fEvalString = new Text(composite, SWT.BORDER);
		fEvalString.setText(fEvalAction.getEvalString());
		fEvalString.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		return composite;
	}


	public EvaluateAction getEvalAction() {
		return fEvalAction;
	}

    @Override
	public void actionDialogCanceled() {
	}

    @Override
	public void actionDialogOK() {
		fEvalAction.setEvalString(fEvalString.getText());
	}

    @Override
	public Composite createComposite(IBreakpointAction action, Composite composite, int style) {
		fEvalAction = (EvaluateAction) action;
		return createEvaluateActionComposite(composite, style);
	}
}
