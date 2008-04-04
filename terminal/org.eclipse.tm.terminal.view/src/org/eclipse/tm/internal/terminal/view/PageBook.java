/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - simplified implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.view;

import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A pagebook is a composite control where only a single control is visible at a
 * time. It is similar to a notebook, but without tabs.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class PageBook extends Composite {
	private StackLayout fLayout;
	public PageBook(Composite parent, int style) {
		super(parent, style);
		fLayout= new StackLayout();
		setLayout(fLayout);
	}
	public void showPage(Control page) {
		fLayout.topControl= page;
		layout();
	}
}
