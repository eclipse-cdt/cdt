/*******************************************************************************
 * Copyright (c) 2017 Pavel Marek
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Pavel Marek - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.overridemethods;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.internal.ui.preferences.CodeStylePreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * This class represents the only InputPage of the wizard for this code generation
 * @author Pavel Marek
 */
public class OverrideMethodsInputPage extends UserInputWizardPage {
	private OverrideMethodsRefactoring fRefactoring;
	private CheckboxTreeViewer fTree;

	public OverrideMethodsInputPage(OverrideMethodsRefactoring refactoring) {
		super(Messages.OverrideMethodsInputPage_Name);
		this.fRefactoring = refactoring;
	}

	/**
	 * Adds "Select All" and "Deselect All" to given Composite.
	 * @param parent
	 */
	private Composite createButtons(Composite parent) {
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		layout.spacing = 4;
		buttonComposite.setLayout(layout);

		Button selAllButton = new Button(buttonComposite, SWT.PUSH);
		selAllButton.setText(Messages.OverrideMethodsInputPage_SelectAll);
		selAllButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				List<Method> allMethods = fRefactoring.getMethodContainer().getAllMethods();

				// Add all methods from container to PrintData.
				fTree.setCheckedElements(allMethods.toArray());
				fRefactoring.getPrintData().addMethods(allMethods);
				checkPageComplete();
			}

		});

		Button deselAllButton = new Button(buttonComposite, SWT.PUSH);
		deselAllButton.setText(Messages.OverrideMethodsInputPage_DeselectAll);
		deselAllButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				List<Method> allMethods = fRefactoring.getMethodContainer().getAllMethods();

				// Uncheck all methods from tree.
				for (Method method : allMethods) {
					fTree.setChecked(method, false);
				}

				fRefactoring.getPrintData().removeMethods(allMethods);
				checkPageComplete();
			}

		});
		return buttonComposite;
	}

	private void createTree(Composite parent) {
		fTree = new ContainerCheckedTreeViewer(parent);
		fTree.setContentProvider(fRefactoring.getMethodContainer());
		fTree.setAutoExpandLevel(3);
		// Populate the tree.
		fTree.setInput(fRefactoring.getMethodContainer().getInitialInput());
		// Horizontal fill.
		fTree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

		fTree.addCheckStateListener(new ICheckStateListener() {

			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				VirtualMethodPrintData printData = fRefactoring.getPrintData();

				// ICPPClassType (parent) checked.
				if (event.getElement() instanceof ICPPClassType) {
					ICPPClassType parentClass = (ICPPClassType) event.getElement();
					VirtualMethodContainer methodContainer = fRefactoring.getMethodContainer();
					List<Method> selectedMethods = methodContainer.getMethods(parentClass);

					// Add (or remove) all methods that are displayed as children in the tree
					// to PrintData.
					if (event.getChecked()) {
						printData.addMethods(selectedMethods);
					} else {
						printData.removeMethods(selectedMethods);
					}
				} else if (event.getElement() instanceof Method) {
					Method selectedMethod = (Method) event.getElement();

					if (event.getChecked()) {
						printData.addMethod(selectedMethod);
					} else {
						printData.removeMethod(selectedMethod);
					}
				}

				checkPageComplete();
			}
		});

		// Set all nodes (Methods) in the tree to unchecked.
		for (Method method : fRefactoring.getMethodContainer().getAllMethods()) {
			fTree.setChecked(method, false);
		}
	}

	@Override
	public void createControl(Composite parent) {
		setTitle(Messages.OverrideMethodsInputPage_Name);
		setMessage(Messages.OverrideMethodsInputPage_Header);

		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		createTree(comp);
		GridData gd = new GridData(GridData.FILL_BOTH);
		fTree.getTree().setLayoutData(gd);

		Composite buttonContainer = createButtons(comp);
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		buttonContainer.setLayoutData(gd);

		final Button ignoreVirtual = new Button(comp, SWT.CHECK);
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.heightHint = 20;
		ignoreVirtual.setLayoutData(gd);
		ignoreVirtual.setText(Messages.OverrideMethodsRefactoring_PreserveVirtual);
		ignoreVirtual.setSelection(fRefactoring.getOptions().preserveVirtual());
		ignoreVirtual.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fRefactoring.getOptions().setPreserveVirtual(ignoreVirtual.getSelection());
			}
		});
		final Button addOverridden = new Button(comp, SWT.CHECK);
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.heightHint = 40;
		addOverridden.setLayoutData(gd);
		addOverridden.setText(Messages.OverrideMethodsRefactoring_AddOverride);
		addOverridden.setSelection(fRefactoring.getOptions().addOverride());
		addOverridden.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fRefactoring.getOptions().setAddOverride(addOverridden.getSelection());
			}
		});
		Link link = new Link(comp, SWT.WRAP);
		link.setText(Messages.OverrideMethodsRefactoring_LinkDescription);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(getShell(), CodeStylePreferencePage.PREF_ID,
						new String[] { CodeStylePreferencePage.PREF_ID }, null).open();
			}
		});
		link.setToolTipText(Messages.OverrideMethodsRefactoring_LinkTooltip);

		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.grabExcessHorizontalSpace = true;
		link.setLayoutData(gd);

		checkPageComplete();
		setControl(comp);
	}

	/**
	 * Sets page complete under certain conditions.
	 *
	 * Note that if the page is complete, the "Preview" and "OK" buttons
	 * are enabled.
	 */
	private void checkPageComplete() {
		if (fRefactoring.getPrintData().isEmpty()) {
			setPageComplete(false);
		} else {
			setPageComplete(true);
		}
	}
}
