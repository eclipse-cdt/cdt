/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;

/**
 * A <code>ViewerPane</code> is a convenience class which installs a
 * <code>CLabel</code> and a <code>Toolbar</code> in a <code>ViewForm</code>.
 * <P>
 */
public class ViewerPane extends ViewForm {

	private ToolBarManager fToolBarManager;

	public ViewerPane(Composite parent, int style) {
		super(parent, style);

		marginWidth = 0;
		marginHeight = 0;

		CLabel label = new CLabel(this, SWT.NONE);
		setTopLeft(label);

		ToolBar tb = new ToolBar(this, SWT.FLAT);
		setTopCenter(tb);
		fToolBarManager = new ToolBarManager(tb);
	}

	/**
	 * Sets the receiver's title text.
	 */
	public void setText(String label) {
		CLabel cl = (CLabel) getTopLeft();
		cl.setText(label);
	}

	public String getText() {
		CLabel cl = (CLabel) getTopLeft();
		return cl.getText();
	}

	/**
	 * Sets the receiver's title image.
	 */
	public void setImage(Image image) {
		CLabel cl = (CLabel) getTopLeft();
		cl.setImage(image);
	}

	public Image getImage() {
		CLabel cl = (CLabel) getTopLeft();
		return cl.getImage();
	}

	public ToolBarManager getToolBarManager() {
		return fToolBarManager;
	}
}
