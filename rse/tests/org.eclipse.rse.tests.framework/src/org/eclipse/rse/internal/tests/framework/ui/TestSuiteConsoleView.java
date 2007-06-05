/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 *******************************************************************************/
package org.eclipse.rse.internal.tests.framework.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

/**
 * Provides a view of any image that needs to be displayed by a test case.
 */
public class TestSuiteConsoleView extends ViewPart {

	private Text console;
	private Color backgroundColor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		console = new Text(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		backgroundColor = new Color(parent.getDisplay(), new RGB(255, 255, 255));
		clear();
	}
	
	public void add(String text) {
		if (!(console == null || console.isDisposed())) {
			String consoleText = console.getText();
			consoleText += text;
			console.setText(consoleText);
		}
	}
	
	public void clear() {
		if (!(console == null || console.isDisposed())) {
			console.setBackground(backgroundColor);
			console.setText(""); //$NON-NLS-1$
		}
	}
	
	public void setFocus() {
	}
	
	public void dispose() {
		if (!(backgroundColor == null || backgroundColor.isDisposed())) {
			backgroundColor.dispose();
		}
	}

}
