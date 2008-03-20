/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.c.hover;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.internal.ui.text.AbstractSourceViewerInformationControl;

/**
 * Information control for macro expansion.
 *
 * @since 5.0
 */
public class CMacroExpansionControl extends AbstractSourceViewerInformationControl {

	/**
	 * Creates a new control for use as a hover which does not take the focus.
	 * 
	 * @param parent  parent shell
	 * @param statusFieldText  text to be displayed in the status field, may be <code>null</code>
	 */
	public CMacroExpansionControl(Shell parent, String statusFieldText) {
		super(parent, PopupDialog.HOVER_SHELLSTYLE, SWT.NONE, false, false, false);
		if (statusFieldText != null) {
			setInfoText(statusFieldText);
		}
		setTitleText(CHoverMessages.CMacroExpansionControl_title_macroExpansion);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractSourceViewerInformationControl#hasHeader()
	 */
	protected boolean hasHeader() {
		return true;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractSourceViewerInformationControl#getId()
	 */
	protected String getId() {
		return "org.eclipse.cdt.ui.text.hover.CMacroExpansion"; //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractSourceViewerInformationControl#setInput(java.lang.Object)
	 */
	@Override
	public void setInput(Object input) {
		if (input instanceof CMacroExpansionInput) {
			setInformation(((CMacroExpansionInput) input).fExplorer.getFullExpansion().getCodeAfterStep());
		} else {
			super.setInput(input);
		}
	}
}
