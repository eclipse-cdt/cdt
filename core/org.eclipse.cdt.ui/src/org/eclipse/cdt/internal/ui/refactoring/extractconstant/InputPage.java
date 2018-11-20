/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractconstant;

import org.eclipse.cdt.internal.ui.refactoring.MethodContext.ContextType;
import org.eclipse.cdt.internal.ui.refactoring.dialogs.NameAndVisibilityComposite;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierResult;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class InputPage extends UserInputWizardPage {
	private static final String PAGE_NAME = "InputPage"; //$NON-NLS-1$

	protected ExtractConstantInfo info;
	protected NameAndVisibilityComposite control;
	private boolean showVisibilityPane;

	public InputPage() {
		super(PAGE_NAME);
	}

	@Override
	public void createControl(Composite parent) {
		this.info = ((ExtractConstantRefactoring) getRefactoring()).getRefactoringInfo();
		this.showVisibilityPane = info.getMethodContext().getType() == ContextType.METHOD;
		control = new NameAndVisibilityComposite(parent, Messages.InputPage_ConstName, info.getName());
		setTitle(getName());
		setPageComplete(false);
		control.getConstantNameText().addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				info.setName(control.getConstantNameText().getText());
				checkName();
			}
		});

		for (Control buttons : control.getVisibiltyGroup().getChildren()) {
			buttons.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					String text = ((Button) e.getSource()).getText();
					visibilityChange(text);
				}
			});
		}

		Button replaceAllOccurencesButton = new Button(control, SWT.CHECK);
		replaceAllOccurencesButton.setSelection(info.isReplaceAllOccurences());
		replaceAllOccurencesButton.setText(Messages.InputPage_ReplaceAllOccurrences);
		replaceAllOccurencesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				info.setReplaceAllLiterals(replaceAllOccurencesButton.getSelection());
			}
		});

		checkName();
		control.getVisibiltyGroup().setVisible(showVisibilityPane);
		setControl(control);
	}

	private void checkName() {
		String methodName = control.getConstantNameText().getText();
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

	private void verifyName(String name) {
		if (info.isNameUsed(name)) {
			setErrorMessage(NLS.bind(Messages.InputPage_NameAlreadyDefined, name));
			setPageComplete(false);
		}
	}

	private void visibilityChange(String visibilityText) {
		info.setVisibility(VisibilityEnum.getEnumForStringRepresentation(visibilityText));
	}
}
