/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google) 
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.cdt.internal.ui.viewsupport.BasicElementLabels;

public class ParameterEditDialog extends StatusDialog {
	private final NameInformation fParameter;
	private final boolean fEditType;
	private final boolean fEditDefault;
	private final boolean fEditReturn;
	private Text fType;
	private Text fName;
	private Text fDefaultValue;
	private Button fReturn;

	/**
	 * @param parentShell
	 * @param parameter
	 * @param canEditType
	 * @param canEditDefault
	 * @param canChangeReturn
	 * Can be <code>null</code> if <code>canEditType</code> is <code>false</code>.
	 */
	public ParameterEditDialog(Shell parentShell, NameInformation parameter, boolean canEditType,
			boolean canEditDefault, boolean canChangeReturn) {
		super(parentShell);
		fParameter= parameter;
		fEditType= canEditType;
		fEditDefault= canEditDefault;
		fEditReturn = canChangeReturn;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.ParameterEditDialog_title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite result= (Composite) super.createDialogArea(parent);
		GridLayout layout= (GridLayout) result.getLayout();
		layout.numColumns= 2;
		Label label;
		GridData gd;

		label= new Label(result, SWT.NONE);
		String newName = fParameter.getNewName();
		if (newName.isEmpty()) {
			label.setText(Messages.ParameterEditDialog_message_new);
		} else {
			label.setText(NLS.bind(Messages.ParameterEditDialog_message,
					BasicElementLabels.getCElementName(newName)));
		}
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);

		if (fEditType) {
			label= new Label(result, SWT.NONE);
			label.setText(Messages.ParameterEditDialog_type);
			fType= new Text(result, SWT.BORDER);
			gd= new GridData(GridData.FILL_HORIZONTAL);
			fType.setLayoutData(gd);
			fType.setText(fParameter.getTypeName());
			fType.addModifyListener(
				new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent e) {
						validate((Text) e.widget);
					}
				});
			TextFieldNavigationHandler.install(fType);
		}

		label= new Label(result, SWT.NONE);
		fName= new Text(result, SWT.BORDER);
		initializeDialogUnits(fName);
		label.setText(Messages.ParameterEditDialog_name);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= convertWidthInCharsToPixels(45);
		fName.setLayoutData(gd);
		fName.setText(newName);
		fName.addModifyListener(
			new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					validate((Text) e.widget);
				}
			});
		TextFieldNavigationHandler.install(fName);

		if (fEditDefault && fParameter.isAdded()) {
			label= new Label(result, SWT.NONE);
			label.setText(Messages.ParameterEditDialog_default_value);
			fDefaultValue= new Text(result, SWT.BORDER);
			gd= new GridData(GridData.FILL_HORIZONTAL);
			fDefaultValue.setLayoutData(gd);
			fDefaultValue.setText(fParameter.getDefaultValue());
			fDefaultValue.addModifyListener(
				new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent e) {
						validate((Text) e.widget);
					}
				});
			TextFieldNavigationHandler.install(fDefaultValue);
		}
		if (fEditReturn) {
			fReturn = new Button(result, SWT.CHECK);
			fReturn.setText(Messages.ParameterEditDialog_use_as_return);
			fReturn.setSelection(fParameter.isReturnValue());
			fReturn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		applyDialogFont(result);
		return result;
	}

	@Override
	protected void okPressed() {
		if (fType != null) {
			fParameter.setTypeName(fType.getText());
		}
		fParameter.setNewName(fName.getText());
		if (fDefaultValue != null) {
			fParameter.setDefaultValue(fDefaultValue.getText());
		}
		if (fReturn != null) {
			fParameter.setReturnValue(fReturn.getSelection());
		}
		super.okPressed();
	}

	private void validate(Text first) {
		IStatus[] result= new IStatus[3];
		if (first == fType) {
			result[0]= validateType();
			result[1]= validateName();
			result[2]= validateDefaultValue();
		} else if (first == fName) {
			result[0]= validateName();
			result[1]= validateType();
			result[2]= validateDefaultValue();
		} else {
			result[0]= validateDefaultValue();
			result[1]= validateName();
			result[2]= validateType();
		}
		for (int i= 0; i < result.length; i++) {
			IStatus status= result[i];
			if (status != null && !status.isOK()) {
				updateStatus(status);
				return;
			}
		}
		updateStatus(Status.OK_STATUS);
	}

	private IStatus validateType() {
		// TODO(sprigogin): Implement type validation.
		return Status.OK_STATUS;
	}

	private IStatus validateName() {
		if (fName == null)
			return null;
		String name= fName.getText();
		if (name.isEmpty())
			return createErrorStatus(Messages.ParameterEditDialog_name_error);
		IStatus status= CConventions.validateFieldName(name);
		if (status.matches(IStatus.ERROR))
			return status;
		return Status.OK_STATUS;
	}

	private IStatus validateDefaultValue() {
		if (fDefaultValue == null)
			return null;
		String defaultValue= fDefaultValue.getText();
		if (defaultValue.isEmpty())
			return createErrorStatus(Messages.ParameterEditDialog_default_value_error);
		// TODO(sprigogin): Implement real default value validation.
		return Status.OK_STATUS;
	}

	private Status createErrorStatus(String message) {
		return new Status(IStatus.ERROR, CUIPlugin.getPluginId(), IStatus.ERROR, message, null);
	}
}
