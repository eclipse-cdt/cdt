/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.preferences;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.ICheckersRegistry;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.internal.ui.CodanUIActivator;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.cdt.codan.internal.ui.dialogs.CustomizeProblemDialog;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
public class CodanPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage {
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private IProblemProfile profile;
	private ISelectionChangedListener problemSelectionListener;
	private ArrayList<IProblem> selectedProblems;
	private Button infoButton;
	private ProblemsTreeEditor checkedTreeEditor;

	public CodanPreferencePage() {
		super(GRID);
		setPreferenceStore(new ScopedPreferenceStore(new InstanceScope(), CodanCorePlugin.PLUGIN_ID));
		// setDescription("Code Analysis Preference Page");
		problemSelectionListener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (infoButton != null) {
					if (event.getSelection() instanceof ITreeSelection) {
						ITreeSelection s = (ITreeSelection) event.getSelection();
						ArrayList<IProblem> list = new ArrayList<IProblem>();
						for (Iterator<?> iterator = s.iterator(); iterator.hasNext();) {
							Object o = iterator.next();
							if (o instanceof IProblem) {
								list.add((IProblem) o);
							}
						}
						setSelectedProblems(list);
					}
				}
			}
		};
	}

	@Override
	protected String getPageId() {
		return "org.eclipse.cdt.codan.internal.ui.preferences.CodanPreferencePage"; //$NON-NLS-1$
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		checkedTreeEditor = new ProblemsTreeEditor(getFieldEditorParent(), profile);
		addField(checkedTreeEditor);
		checkedTreeEditor.getTreeViewer().addSelectionChangedListener(problemSelectionListener);
		checkedTreeEditor.getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				openCustomizeDialog();
			}
		});
		GridData layoutData = new GridData(GridData.FILL, GridData.FILL, true, true);
		layoutData.heightHint = 400;
		checkedTreeEditor.getTreeViewer().getControl().setLayoutData(layoutData);
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
		profile = isPropertyPage() ? getRegistry().getResourceProfileWorkingCopy((IResource) getElement()) : getRegistry()
				.getWorkspaceProfile();
		Composite comp = (Composite) super.createContents(parent);
		createInfoControl(comp);
		return comp;
	}

	/**
	 * @param comp
	 */
	private void createInfoControl(Composite comp) {
		Composite info = new Composite(comp, SWT.NONE);
		info.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		info.setLayout(layout);
		infoButton = new Button(info, SWT.PUSH);
		infoButton.setLayoutData(GridDataFactory.swtDefaults().align(SWT.END, SWT.BEGINNING).create());
		infoButton.setText(CodanUIMessages.CodanPreferencePage_Customize);
		infoButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openCustomizeDialog();
			}
		});
		restoreWidgetValues();
	}

	/**
	 * @param selection
	 */
	protected void setSelectedProblems(ArrayList<IProblem> list) {
		this.selectedProblems = list;
		updateProblemInfo();
	}

	/**
	 * @return
	 */
	protected ICheckersRegistry getRegistry() {
		return CodanRuntime.getInstance().getCheckersRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	public boolean performOk() {
		saveWidgetValues();
		// if (isPropertyPage())
		getRegistry().updateProfile((IResource) getElement(), null);
		return super.performOk();
	}

	private void saveWidgetValues() {
		CodanUIActivator
				.getDefault()
				.getDialogSettings()
				.put(getWidgetId(),
						(selectedProblems == null || selectedProblems.size() == 0) ? EMPTY_STRING : selectedProblems.get(0).getId());
	}

	private void restoreWidgetValues() {
		String id = CodanUIActivator.getDefault().getDialogSettings().get(getWidgetId());
		if (id != null && id.length() > 0 && checkedTreeEditor != null) {
			IProblem problem = profile.findProblem(id);
			if (problem != null)
				checkedTreeEditor.getTreeViewer().setSelection(new StructuredSelection(problem), true);
		} else {
			setSelectedProblems(null);
		}
	}

	/**
	 * @return
	 */
	protected String getWidgetId() {
		return getPageId() + ".selection"; //$NON-NLS-1$
	}

	private void updateProblemInfo() {
		if (selectedProblems == null) {
			infoButton.setEnabled(false);
		} else {
			infoButton.setEnabled(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}

	protected void openCustomizeDialog() {
		if (selectedProblems == null || selectedProblems.size() == 0)
			return;
		CustomizeProblemDialog dialog = new CustomizeProblemDialog(getShell(), selectedProblems.toArray(new IProblem[selectedProblems
				.size()]), (IResource) getElement());
		dialog.open();
		checkedTreeEditor.getTreeViewer().refresh(true);
	}
}