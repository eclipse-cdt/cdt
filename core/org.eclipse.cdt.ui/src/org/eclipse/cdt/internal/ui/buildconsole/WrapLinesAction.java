/*******************************************************************************
 * Copyright (c) 2013, 2013 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.StyledText;

import org.eclipse.cdt.ui.CDTSharedImages;

import org.eclipse.cdt.internal.ui.preferences.BuildConsolePreferencePage;

/**
 * Toggles word wrapping of the console
 */
public class WrapLinesAction extends Action {
	private BuildConsoleViewer fConsoleViewer;

	/**
	 * Constructor.
	 * 
	 * @param viewer - console viewer.
	 */
	public WrapLinesAction(BuildConsoleViewer viewer) {
		super(ConsoleMessages.WrapLinesAction_WrapLines); 
		fConsoleViewer = viewer;
		propertyChange();
		setToolTipText(ConsoleMessages.WrapLinesAction_WrapLines);
		setImageDescriptor(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_WRAP_LINE)); 
	}

	public void propertyChange() {
		if (BuildConsolePreferencePage.isConsoleWrapLinesAllowed()) {
			setEnabled(true);
			setChecked(BuildConsolePreferencePage.isConsoleWrapLines());
		} else {
			setEnabled(false);
			setChecked(false);
		}
	}

	private void setWordWrap(boolean wrap) {
		StyledText styledText = fConsoleViewer.getTextWidget();
		if (styledText != null) {
			// It should not be possible for setWordWrap when disabled (aka
			// isConsoleWrapLinesAllowed != true) - however if someone calls
			// the method (or setChecked) programatically, ensure we don't
			// let the wordwrap come on
			if (BuildConsolePreferencePage.isConsoleWrapLinesAllowed() && wrap) {
				styledText.setWordWrap(wrap);
			} else {
				styledText.setWordWrap(false);
			}
		}
	}

	@Override
	public void run() {
		setWordWrap(isChecked());
	}

	@Override
	public void setChecked(boolean checked) {
		super.setChecked(checked);
		setWordWrap(checked);
	}
}
