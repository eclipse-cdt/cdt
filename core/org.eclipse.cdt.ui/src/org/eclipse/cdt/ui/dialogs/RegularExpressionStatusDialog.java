/*******************************************************************************
 * Copyright (c) 2009, 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.dialogs;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.text.FindReplaceDocumentAdapterContentProposalProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;

import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;

/**
 * Input Dialog for validating regular expression syntax.
 *
 * @since 5.2
 */
public class RegularExpressionStatusDialog extends InputStatusDialog {
	private static final IInputStatusValidator fValidator = new IInputStatusValidator() {
		@Override
		public IStatus isValid(String newText) {
			StatusInfo status = new StatusInfo();
			if (newText.length() == 0) {
				status.setWarning(DialogsMessages.RegularExpression_EmptyPattern);
			} else {
				try {
					Pattern.compile(newText);
				} catch (Exception e) {
					// only first line of PatternSyntaxException is really descriptive
					status.setError(e.getMessage().split("[\n\r]", 2)[0]); //$NON-NLS-1$
				}
			}
			return status;
		}
	};

	/**
	 * Constructor
	 *
	 * @param shell - the parent shell, or <code>null</code> to create a top-level shell
	 * @param initialValue the initial input value, or <code>null</code> if none
	 *            (equivalent to the empty string)
	 */
	public RegularExpressionStatusDialog(Shell shell, String initialValue) {
		super(shell, DialogsMessages.RegularExpression_Validate,
				DialogsMessages.RegularExpression_Enter,
				initialValue, fValidator);
	}

	/**
	 * Constructor
	 *
	 * @param shell - the parent shell, or <code>null</code> to create a top-level shell
	 * @param dialogTitle - the dialog title, or <code>null</code> if none
	 * @param dialogMessage - the dialog message, or <code>null</code> if none
	 * @param initialValue the initial input value, or <code>null</code> if none
	 *            (equivalent to the empty string)
	 */
	public RegularExpressionStatusDialog(Shell shell, String dialogTitle, String dialogMessage, String initialValue) {
		super(shell, dialogTitle, dialogMessage, initialValue, fValidator);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.dialogs.InputStatusDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);

		new ContentAssistCommandAdapter(
				getText(),
				new TextContentAdapter(),
				new FindReplaceDocumentAdapterContentProposalProvider(true),
				null,
				null,
				true);


		setHelpAvailable(false);
		return control;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.StatusDialog#create()
	 */
	@Override
	public void create() {
		super.create();
		if (getValue().length()>0)
			validateInput();
	}
}
