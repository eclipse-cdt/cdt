/*******************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * This is a <code>MessageDialog</code> which allows the user
 * to choose that the dialog isn't shown again the next time.
 */
public class OptionalMessageDialog extends MessageDialog {
	// String constants for widgets
	private static final String CHECKBOX_TEXT = MakeUIMessages.getResourceString("OptionalMessageDialog_dontShowAgain"); //$NON-NLS-1$

	// Dialog store id constants
	private static final String STORE_ID = "OptionalMessageDialog.hide."; //$NON-NLS-1$
	private static final String KEY_DETAIL = ".detail"; //$NON-NLS-1$

	public static final int NOT_SHOWN = IDialogConstants.CLIENT_ID + 1;
	public static final int NO_DETAIL = -1;

	private Button fHideDialogCheckBox;
	private String fId;

	/**
	 * Opens the dialog but only if the user hasn't choosen to hide it.
	 * Returns <code>NOT_SHOWN</code> if the dialog was not shown.
	 */
	public static int open(String id, Shell parent, String title, Image titleImage, String message, int dialogType,
			String[] buttonLabels, int defaultButtonIndex) {
		if (!isDialogEnabled(id))
			return OptionalMessageDialog.NOT_SHOWN;

		MessageDialog dialog = new OptionalMessageDialog(id, parent, title, titleImage, message, dialogType,
				buttonLabels, defaultButtonIndex);
		return dialog.open();
	}

	protected OptionalMessageDialog(String id, Shell parent, String title, Image titleImage, String message,
			int dialogType, String[] buttonLabels, int defaultButtonIndex) {
		super(parent, title, titleImage, message, dialogType, buttonLabels, defaultButtonIndex);
		fId = id;
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		fHideDialogCheckBox = new Button(composite, SWT.CHECK | SWT.LEFT);
		fHideDialogCheckBox.setText(CHECKBOX_TEXT);
		fHideDialogCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDialogEnabled(fId, !((Button) e.widget).getSelection());
			}
		});
		applyDialogFont(fHideDialogCheckBox);
		return fHideDialogCheckBox;
	}

	//--------------- Configuration handling --------------

	/**
	 * Returns this dialog
	 *
	 * @return the settings to be used
	 */
	private static IDialogSettings getDialogSettings() {
		IDialogSettings settings = CUIPlugin.getDefault().getDialogSettings();
		settings = settings.getSection(STORE_ID);
		if (settings == null)
			settings = CUIPlugin.getDefault().getDialogSettings().addNewSection(STORE_ID);
		return settings;
	}

	/**
	 * Answers whether the optional dialog is enabled and should be shown.
	 */
	public static boolean isDialogEnabled(String key) {
		IDialogSettings settings = getDialogSettings();
		return !settings.getBoolean(key);
	}

	/**
	 * Sets a detail for the dialog.
	 */
	public static void setDialogDetail(String key, int detail) {
		IDialogSettings settings = getDialogSettings();
		settings.put(key + KEY_DETAIL, detail);
	}

	/**
	 * Returns the detail for this dialog, or NO_DETAIL, if none.
	 */
	public static int getDialogDetail(String key) {
		IDialogSettings settings = getDialogSettings();
		try {
			return settings.getInt(key + KEY_DETAIL);
		} catch (NumberFormatException e) {
			return NO_DETAIL;
		}
	}

	/**
	 * Sets whether the optional dialog is enabled and should be shown.
	 */
	public static void setDialogEnabled(String key, boolean isEnabled) {
		IDialogSettings settings = getDialogSettings();
		settings.put(key, !isEnabled);
	}

	/**
	 * Clears all remembered information about hidden dialogs
	 */
	public static void clearAllRememberedStates() {
		IDialogSettings settings = CUIPlugin.getDefault().getDialogSettings();
		settings.addNewSection(STORE_ID);
	}
}
