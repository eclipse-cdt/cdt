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
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
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
	private Composite parametersTab;
	private ISelectionChangedListener problemSelectionListener;
	private IProblem selectedProblem;

	public CodanPreferencePage() {
		super(GRID);
		setPreferenceStore(new ScopedPreferenceStore(new InstanceScope(),
				CodanCorePlugin.PLUGIN_ID));
		setDescription("Code Analyzers Preference Page");
		problemSelectionListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (parametersTab != null) {
					if (event.getSelection() instanceof ITreeSelection) {
						ITreeSelection s = (ITreeSelection) event
								.getSelection();
						if (s.getFirstElement() instanceof IProblem)
							setSelectedProblem((IProblem) s.getFirstElement());
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
		final TabFolder tabFolder = new TabFolder(getFieldEditorParent(),
				SWT.TOP);
		tabFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// createMainTab(tabFolder);
		createParamtersTab(tabFolder);
		createScopeTab(tabFolder);
		checkedTreeEditor.getTreeViewer().addSelectionChangedListener(
				problemSelectionListener);
	}

	/**
	 * @param selection
	 */
	protected void setSelectedProblem(IProblem problem) {
		if (this.selectedProblem != problem) {
			saveProblemEdits();
		}
		this.selectedProblem = problem;
		Control[] children = parametersTab.getChildren();
		for (int i = 0; i < children.length; i++) {
			Control control = children[i];
			control.dispose();
		}
		ParametersComposite comp = new ParametersComposite(parametersTab,
				problem);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		parametersTab.pack(true);
		parametersTab.layout(true);
	}

	/**
	 * @param tabFolder
	 */
	private void createParamtersTab(TabFolder tabFolder) {
		TabItem tabItem1 = new TabItem(tabFolder, SWT.NULL);
		tabItem1.setText("Parameters");
		parametersTab = new Composite(tabFolder, SWT.NONE);
		tabItem1.setControl(parametersTab);
		parametersTab.setLayout(new GridLayout());
	}

	/**
	 * @param tabFolder
	 */
	private void createScopeTab(TabFolder tabFolder) {
		TabItem tabItem1 = new TabItem(tabFolder, SWT.NULL);
		tabItem1.setText("Scope");
		Composite comp = new Composite(tabFolder, SWT.NONE);
		tabItem1.setControl(comp);
		comp.setLayout(new GridLayout());
		Label label = new Label(comp, SWT.NONE);
		label.setText("Scope: TODO");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
		saveProblemEdits();
		return super.performOk();
	}

	/**
	 * 
	 */
	private void saveProblemEdits() {
		if (selectedProblem==null) return;
		Control[] children = parametersTab.getChildren();
		for (int i = 0; i < children.length; i++) {
			Control control = children[i];
			if (control instanceof ParametersComposite) {
				((ParametersComposite) control).save((IProblemWorkingCopy) selectedProblem); // XXX
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
}