/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.preferences;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.ICheckersRegistry;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemParameterInfo;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.cdt.codan.internal.ui.dialogs.CustomizeProblemDialog;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */
public class CodanPreferencePage extends FieldEditorOverlayPage implements
		IWorkbenchPreferencePage {
	private IProblemProfile profile;
	private ISelectionChangedListener problemSelectionListener;
	private IProblem selectedProblem;
	private Group info;
	private Label infoMessage;
	private Label infoParams;
	private Button infoButton;

	public CodanPreferencePage() {
		super(GRID);
		setPreferenceStore(new ScopedPreferenceStore(new InstanceScope(),
				CodanCorePlugin.PLUGIN_ID));
		// setDescription("Code Analysis Preference Page");
		problemSelectionListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (info != null) {
					if (event.getSelection() instanceof ITreeSelection) {
						ITreeSelection s = (ITreeSelection) event
								.getSelection();
						if (s.getFirstElement() instanceof IProblem)
							setSelectedProblem((IProblem) s.getFirstElement());
						else
							setSelectedProblem(null);
					}
				}
			}
		};
	}

	protected String getPageId() {
		return "org.eclipse.cdt.codan.internal.ui.preferences.CodanPreferencePage"; //$NON-NLS-1$
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		profile = isPropertyPage() ? getRegistry()
				.getResourceProfileWorkingCopy((IResource) getElement())
				: getRegistry().getWorkspaceProfile();
		CheckedTreeEditor checkedTreeEditor = new ProblemsTreeEditor(
				getFieldEditorParent(), profile);
		addField(checkedTreeEditor);
		checkedTreeEditor.getTreeViewer().addSelectionChangedListener(
				problemSelectionListener);
		checkedTreeEditor.getTreeViewer().addDoubleClickListener(
				new IDoubleClickListener() {
					public void doubleClick(DoubleClickEvent event) {
						openCustomizeDialog();
					}
				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.internal.ui.preferences.FieldEditorOverlayPage#
	 * createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite comp = (Composite) super.createContents(parent);
		createInfoControl(comp);
		return comp;
	}

	/**
	 * @param comp
	 */
	private void createInfoControl(Composite comp) {
		info = new Group(comp, SWT.NONE);
		info.setLayoutData(new GridData(GridData.FILL_BOTH));
		info.setLayout(new GridLayout(2, false));
		info.setText(CodanUIMessages.CodanPreferencePage_Info);
		infoParams = new Label(info, SWT.NONE);
		infoParams.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
				false));
		infoButton = new Button(info, SWT.PUSH);
		infoButton
				.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		infoButton.setText(CodanUIMessages.CodanPreferencePage_Customize);
		infoButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openCustomizeDialog();
			}
		});
		infoMessage = new Label(info, SWT.WRAP);
		GridData ld = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		ld.horizontalSpan = 2;
		ld.widthHint = 400;
		infoMessage.setLayoutData(ld);
		setSelectedProblem(null);
	}

	/**
	 * @param selection
	 */
	protected void setSelectedProblem(IProblem problem) {
		this.selectedProblem = problem;
		updateProblemInfo();
	}

	/**
	 * @return
	 */
	protected ICheckersRegistry getRegistry() {
		return CodanRuntime.getInstance().getChechersRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	public boolean performOk() {
		// if (isPropertyPage())
		getRegistry().updateProfile((IResource) getElement(), null);
		return super.performOk();
	}

	/**
	 * 
	 */
	private void updateProblemInfo() {
		if (selectedProblem == null) {
			infoMessage.setText(""); //$NON-NLS-1$
			infoParams.setText(""); //$NON-NLS-1$
			infoButton.setEnabled(false);
		} else {
			IProblemParameterInfo parameterInfo = selectedProblem
					.getParameterInfo();
			String desc = selectedProblem.getDescription();
			if (desc == null)
				desc = CodanUIMessages.CodanPreferencePage_NoInfo;
			infoMessage.setText(desc);
			infoParams
					.setText(parameterInfo == null ? CodanUIMessages.CodanPreferencePage_NoParameters
							: CodanUIMessages.CodanPreferencePage_HasParameters);
			infoButton.setEnabled(true);
		}
		info.layout(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/**
	 * 
	 */
	protected void openCustomizeDialog() {
		CustomizeProblemDialog d = new CustomizeProblemDialog(getShell(),
				selectedProblem);
		d.open();
	}
}