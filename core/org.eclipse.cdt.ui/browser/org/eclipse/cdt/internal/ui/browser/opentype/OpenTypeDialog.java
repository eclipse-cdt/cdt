/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - adapted for use in CDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.opentype;

import org.eclipse.cdt.ui.browser.typeinfo.TypeSelectionDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * A dialog to select a type from a list of types. The selected type will be
 * opened in the editor.
 */
public class OpenTypeDialog extends TypeSelectionDialog {

	private static final String DIALOG_SETTINGS= OpenTypeDialog.class.getName();

	/**
	 * Constructs an instance of <code>OpenTypeDialog</code>.
	 * @param parent  the parent shell.
	 */
	public OpenTypeDialog(Shell parent) {
		super(parent);
		setTitle(OpenTypeMessages.getString("OpenTypeDialog.title")); //$NON-NLS-1$
		setMessage(OpenTypeMessages.getString("OpenTypeDialog.message")); //$NON-NLS-1$
		setDialogSettings(DIALOG_SETTINGS);
	}
}
