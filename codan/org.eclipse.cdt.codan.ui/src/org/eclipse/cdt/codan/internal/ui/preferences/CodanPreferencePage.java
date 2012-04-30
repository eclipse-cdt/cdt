/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Alex Ruiz (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.preferences;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.ICheckersRegistry;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.internal.core.CodanRunner;
import org.eclipse.cdt.codan.internal.ui.CodanUIActivator;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.cdt.codan.internal.ui.dialogs.CustomizeProblemDialog;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogSettings;
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
 * This class represents a preference page that is contributed to the Preferences dialog.
 * By subclassing {@code FieldEditorPreferencePage}, we can use built-in field support in
 * JFace to create a page that is both small and knows how to save, restore and apply its
 * values.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that
 * belongs to the main plug-in class. That way, preferences can be accessed directly via
 * the preference store.
 * </p>
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
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, CodanCorePlugin.PLUGIN_ID));
		problemSelectionListener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (infoButton != null && event.getSelection() instanceof ITreeSelection) {
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
		};
	}

	@Override
	protected String getPageId() {
		return "org.eclipse.cdt.codan.internal.ui.preferences.CodanPreferencePage"; //$NON-NLS-1$
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to
	 * manipulate various types of preferences. Each field editor knows how to
	 * save and restore
	 * its own value.
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
		layoutData.heightHint = 200;
		checkedTreeEditor.getTreeViewer().getControl().setLayoutData(layoutData);
	}

	@Override
	protected Control createContents(Composite parent) {
		if (isPropertyPage()) {
			profile = getRegistry().getResourceProfileWorkingCopy((IResource) getElement());
		} else {
			profile = getRegistry().getWorkspaceProfile();
		}
		Composite comp = (Composite) super.createContents(parent);
		createInfoControl(comp);
		return comp;
	}

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

	protected void setSelectedProblems(ArrayList<IProblem> list) {
		this.selectedProblems = list;
		updateProblemInfo();
	}

	protected ICheckersRegistry getRegistry() {
		return CodanRuntime.getInstance().getCheckersRegistry();
	}

	@Override
	public boolean performOk() {
		saveWidgetValues();
		IResource resource = (IResource) getElement();
		getRegistry().updateProfile(resource, null);
		boolean success = super.performOk();
		if (success) {
			if (resource == null) {
				resource = ResourcesPlugin.getWorkspace().getRoot();
			}
			CodanRunner.asynchronouslyRemoveMarkersForDisabledProblems(resource);
		}
		return success;
	}

	private void saveWidgetValues() {
		String id = !hasSelectedProblems() ? EMPTY_STRING : selectedProblems.get(0).getId();
		getDialogSettings().put(getWidgetId(), id);
	}

	private void restoreWidgetValues() {
		String id = getDialogSettings().get(getWidgetId());
		if (id != null && !id.isEmpty() && checkedTreeEditor != null) {
			IProblem problem = profile.findProblem(id);
			if (problem != null) {
				checkedTreeEditor.getTreeViewer().setSelection(new StructuredSelection(problem), true);
			}
		} else {
			setSelectedProblems(null);
		}
		updateProblemInfo();
	}

	private IDialogSettings getDialogSettings() {
		return CodanUIActivator.getDefault().getDialogSettings();
	}

	protected String getWidgetId() {
		return getPageId() + ".selection"; //$NON-NLS-1$
	}

	private void updateProblemInfo() {
		infoButton.setEnabled(hasSelectedProblems());
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	protected void openCustomizeDialog() {
		if (!hasSelectedProblems()) {
			return;
		}
		IProblem[] selected = selectedProblems.toArray(new IProblem[selectedProblems.size()]);
		CustomizeProblemDialog dialog = new CustomizeProblemDialog(getShell(), selected, (IResource) getElement());
		dialog.open();
		checkedTreeEditor.getTreeViewer().refresh(true);
	}

	private boolean hasSelectedProblems() {
		return selectedProblems != null && !selectedProblems.isEmpty();
	}
}