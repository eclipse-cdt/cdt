/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.reducer;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.cdt.internal.ui.util.RowLayouter;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class RemoveUnusedDeclarationsInputPage extends UserInputWizardPage {
	public static final String PAGE_NAME = "RemoveUnusedDeclarationsInputPage"; //$NON-NLS-1$

	private RemoveFunctionBodiesRefactoring refactoring;
	private Text textField;
	private IDialogSettings settings;

	public RemoveUnusedDeclarationsInputPage() {
		super(PAGE_NAME);
		setImageDescriptor(CPluginImages.DESC_WIZBAN_REFACTOR_TU);
	}

	@Override
	public void createControl(Composite parent) {
		refactoring = (RemoveFunctionBodiesRefactoring) getRefactoring();

		Composite result = new Composite(parent, SWT.NONE);
		setControl(result);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		result.setLayout(layout);
		RowLayouter layouter = new RowLayouter(2);
		GridData gd = null;

		initializeDialogUnits(result);

		Composite group = new Composite(result, SWT.NONE);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout = new GridLayout();
		layout.numColumns = 4;
		layout.marginWidth = 0;
		group.setLayout(layout);
	}

	private Text createTextInputField(Composite parent, int style) {
		Text result = new Text(parent, style);
		result.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				//				textModified(result.getText());
			}
		});
		TextFieldNavigationHandler.install(result);
		return result;
	}
}
