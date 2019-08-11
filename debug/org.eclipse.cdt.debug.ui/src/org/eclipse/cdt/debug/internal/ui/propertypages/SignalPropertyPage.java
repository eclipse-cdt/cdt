/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.propertypages;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.cdt.debug.internal.ui.dialogfields.SelectionButtonDialogField;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;

import com.ibm.icu.text.MessageFormat;

/**
 * The property page for a signal.
 */
public class SignalPropertyPage extends PropertyPage {

	private SelectionButtonDialogField fPassButton;
	private SelectionButtonDialogField fStopButton;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		Composite composite = new Composite(parent, SWT.NONE);
		Font font = parent.getFont();
		composite.setFont(font);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create description field
		try {
			String description = getSignal().getDescription();
			Label label = new Label(composite, SWT.WRAP);
			label.setText(MessageFormat.format(PropertyPageMessages.getString("SignalPropertyPage.0"), //$NON-NLS-1$
					new Object[] { description }));
			GridData data = new GridData(
					GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			label.setLayoutData(data);
			label.setFont(font);
		} catch (DebugException e1) {
		}

		// Create pass button
		try {
			boolean pass = getSignal().isPassEnabled();
			fPassButton = new SelectionButtonDialogField(SWT.CHECK);
			fPassButton.setLabelText(PropertyPageMessages.getString("SignalPropertyPage.1")); //$NON-NLS-1$
			fPassButton.setSelection(pass);
			fPassButton.setEnabled(getSignal().canModify());
			fPassButton.doFillIntoGrid(composite, 1);
		} catch (DebugException e) {
		}

		// Create stop button
		try {
			boolean stop = getSignal().isStopEnabled();
			fStopButton = new SelectionButtonDialogField(SWT.CHECK);
			fStopButton.setLabelText(PropertyPageMessages.getString("SignalPropertyPage.2")); //$NON-NLS-1$
			fStopButton.setSelection(stop);
			fStopButton.setEnabled(getSignal().canModify());
			fStopButton.doFillIntoGrid(composite, 1);
		} catch (DebugException e) {
		}

		setValid(true);
		return composite;
	}

	protected SelectionButtonDialogField getPassButton() {
		return fPassButton;
	}

	protected SelectionButtonDialogField getStopButton() {
		return fStopButton;
	}

	public ICSignal getSignal() {
		return (ICSignal) getElement();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		boolean result = super.performOk();
		if (result) {
			DebugPlugin.getDefault().asyncExec(() -> {
				if (!getSignal().canModify())
					return;
				if (getPassButton() != null) {
					try {
						getSignal().setPassEnabled(getPassButton().isSelected());
					} catch (DebugException e1) {
						failed(PropertyPageMessages.getString("SignalPropertyPage.5"), e1); //$NON-NLS-1$
					}
				}
				if (getStopButton() != null) {
					try {
						getSignal().setStopEnabled(getStopButton().isSelected());
					} catch (DebugException e2) {
						failed(PropertyPageMessages.getString("SignalPropertyPage.5"), e2); //$NON-NLS-1$
					}
				}
			});
		}
		return result;
	}

	protected void failed(String message, Throwable e) {
		MultiStatus ms = new MultiStatus(CDIDebugModel.getPluginIdentifier(),
				ICDebugInternalConstants.STATUS_CODE_ERROR, message, null);
		ms.add(new Status(IStatus.ERROR, CDIDebugModel.getPluginIdentifier(),
				ICDebugInternalConstants.STATUS_CODE_ERROR, e.getMessage(), null));
		CDebugUtils.error(ms, getSignal());
	}
}
