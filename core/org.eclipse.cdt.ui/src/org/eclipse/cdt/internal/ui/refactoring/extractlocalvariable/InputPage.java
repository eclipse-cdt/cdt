/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Google
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.internal.ui.refactoring.NameNVisibilityInformation;
import org.eclipse.cdt.internal.ui.refactoring.dialogs.LabeledTextField;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierResult;

/**
 * Input verification page for the ExtractLocalVariable refactoring, cloned
 * from org.eclipse.cdt.internal.ui.refactoring.extractconstant.InputPage.
 * 
 * @author Tom Ball
 */
public class InputPage extends UserInputWizardPage {
	private String label = Messages.VariableName;
	private final NameNVisibilityInformation info;
	private InputForm control;

	public InputPage(String name, NameNVisibilityInformation info) {
		super(name);
		this.info = info;
	}

	public String getVariableName() {
		return info.getName();
	}

	@Override
	public void createControl(Composite parent) {
		control = new InputForm(parent, label);

		setTitle(getName());
		setMessage(Messages.EnterVariableName);
		setPageComplete(false);
		Text nameText = control.getVariableNameText();
		nameText.setText(info.getName());
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				info.setName(control.getVariableNameText().getText());
				checkName();
			}
		});

		nameText.selectAll();
		checkName();
		setControl(control);
	}

	private void verifyName(String name) {
		if (info.getUsedNames().contains(name)) {
			setErrorMessage(NLS.bind(Messages.NameAlreadyDefined, name));
			setPageComplete(false);
		}
	}

	private void checkName() {
		String methodName = control.getVariableNameText().getText();
		IdentifierResult result = IdentifierHelper
				.checkIdentifierName(methodName);
		if (result.isCorrect()) {
			setErrorMessage(null);
			setPageComplete(true);
			verifyName(methodName);
		} else {
			setErrorMessage(NLS.bind(Messages.CheckName, result.getMessage()));
			setPageComplete(false);
		}
	}

	private static class InputForm extends Composite {
		LabeledTextField variableName;

		InputForm(Composite parent, String label) {
			super(parent, SWT.NONE);
			FillLayout layout = new FillLayout(SWT.HORIZONTAL);
			GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true,
					false);
			gridData.horizontalAlignment = GridData.FILL;
			setLayoutData(gridData);
			setLayout(layout);
			variableName = new LabeledTextField(this, label, ""); //$NON-NLS-1$
		}

		Text getVariableNameText() {
			return variableName.getText();
		}
	}
}
