/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software (IFS)- initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

/**
 * @author Emanuel Graf IFS
 */
public class ImplementMethodInputPage extends UserInputWizardPage {
	private ImplementMethodData data;
	private ContainerCheckedTreeViewer tree;

	public ImplementMethodInputPage(ImplementMethodData data, ImplementMethodWizard implementMethodRefactoringWizard) {
		super(Messages.ImplementMethodInputPage_PageTitle);
		this.setData(data);
	}

	@Override
	public boolean canFlipToNextPage() {
		if (data.needParameterInput()) {
			return super.canFlipToNextPage();
		} else { // getNextPage call is too expensive in this case.
			return isPageComplete();
		}
	}

	@Override
	public void createControl(Composite parent) {
		setTitle(Messages.ImplementMethodInputPage_PageTitle);
		setMessage(Messages.ImplementMethodInputPage_Header);

		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		createTree(comp);
		createFieldManagementButtonsComposite(comp);

		setControl(comp);
		checkPage();
	}

	private Composite createFieldManagementButtonsComposite(Composite comp) {
		Composite btComp = new Composite(comp, SWT.NONE);
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		layout.spacing = 4;
		btComp.setLayout(layout);

		GridData gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		btComp.setLayoutData(gd);

		final Button selectAll = new Button(btComp, SWT.PUSH);
		selectAll.setText(Messages.ImplementMethodInputPage_SelectAll);
		selectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] items = data.getElements(null);
				for (Object treeItem : items) {
					MethodToImplementConfig method = (MethodToImplementConfig) treeItem;
					method.setChecked(true);
					tree.setChecked(treeItem, true);
				}
				checkPage();
			}
		});

		final Button deselectAll = new Button(btComp, SWT.PUSH);
		deselectAll.setText(Messages.ImplementMethodInputPage_DeselectAll);
		deselectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] items = data.getElements(null);
				for (Object treeItem : items) {
					MethodToImplementConfig method = (MethodToImplementConfig) treeItem;
					method.setChecked(false);
					tree.setChecked(treeItem, false);
				}
				checkPage();
			}
		});

		return btComp;
	}

	private void createTree(Composite comp) {
		tree = new ContainerCheckedTreeViewer(comp);
		tree.setContentProvider(data);
		tree.setAutoExpandLevel(2);
		tree.setInput(""); //$NON-NLS-1$
		tree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

		tree.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				MethodToImplementConfig config = ((MethodToImplementConfig) event.getElement());
				config.setChecked(event.getChecked());
				checkPage();
			}
		});

		for (MethodToImplementConfig config : data.getMethodsToImplement()) {
			tree.setChecked(config, config.isChecked());
		}
	}

	@Override
	public IWizardPage getNextPage() {
		if (data.needParameterInput()) {
			return getWizard().getPageForConfig(data.getFirstConfigNeedingParameterNames());
		} else {
			return computeSuccessorPage();
		}
	}

	@Override
	public ImplementMethodWizard getWizard() {
		return (ImplementMethodWizard) super.getWizard();
	}

	public void setData(ImplementMethodData data) {
		this.data = data;
	}

	public ImplementMethodData getData() {
		return data;
	}

	private void checkPage() {
		if (data.getMethodsToImplement().size() > 0) {
			setPageComplete(true);
		} else {
			setPageComplete(false);
		}
	}
}
