/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Tom Ball (Google)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable;

import org.eclipse.cdt.internal.ui.refactoring.VariableNameInformation;
import org.eclipse.cdt.internal.ui.refactoring.dialogs.LabeledTextField;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierResult;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Input verification page for the ExtractLocalVariable refactoring, cloned
 * from org.eclipse.cdt.internal.ui.refactoring.extractconstant.InputPage.
 *
 * @author Tom Ball
 */
public class InputPage extends UserInputWizardPage {
	private static final String PAGE_NAME = "InputPage"; //$NON-NLS-1$

	private VariableNameInformation info;
	private InputForm control;

	public InputPage() {
		super(PAGE_NAME);
	}

	@Override
	public void createControl(Composite parent) {
		this.info = ((ExtractLocalVariableRefactoring) getRefactoring()).getRefactoringInfo();
		control = new InputForm(parent, Messages.VariableName);

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
		nameText.setFocus();
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
		IdentifierResult result = IdentifierHelper.checkIdentifierName(methodName);
		if (result.isCorrect()) {
			setErrorMessage(null);
			setPageComplete(true);
			verifyName(methodName);
		} else {
			setErrorMessage(result.getMessage());
			setPageComplete(false);
		}
	}

	private static class InputForm extends Composite {
		LabeledTextField variableName;

		InputForm(Composite parent, String label) {
			super(parent, SWT.NONE);
			FillLayout layout = new FillLayout(SWT.HORIZONTAL);
			GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
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
