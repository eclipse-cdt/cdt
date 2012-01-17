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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ResumeActionComposite extends Composite {

	private Text pauseTime;

	/**
	 * Create the composite
	 * 
	 * @param parent
	 * @param style
	 */
	public ResumeActionComposite(Composite parent, int style, ResumeActionPage page) {
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		setLayout(gridLayout);

		final Label resumeAfterLabel = new Label(this, SWT.NONE);
		resumeAfterLabel.setText(Messages.getString("ResumeActionComposite.ResumeAfterLabel")); //$NON-NLS-1$

		pauseTime = new Text(this, SWT.BORDER);
		pauseTime.setText(Integer.toString(page.getResumeAction().getPauseTime()));
		
		
		final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.widthHint = 35;
		pauseTime.setLayoutData(gridData);

		final Label secondsLabel = new Label(this, SWT.NONE);
		secondsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		secondsLabel.setText(Messages.getString("ResumeActionComposite.Seconds")); //$NON-NLS-1$
		//
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	int getPauseTime() {
		return Integer.parseInt(pauseTime.getText());
	}

}
