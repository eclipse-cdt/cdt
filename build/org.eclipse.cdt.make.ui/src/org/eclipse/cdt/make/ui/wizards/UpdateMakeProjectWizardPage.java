/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.wizards;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.part.WizardCheckboxTablePart;
import org.eclipse.cdt.make.internal.ui.wizards.StatusWizardPage;
import org.eclipse.cdt.make.ui.actions.UpdateMakeProjectAction;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class UpdateMakeProjectWizardPage extends StatusWizardPage {

	private static final String MAKE_UPDATE_TITLE = "MakeWizardUpdatePage.title"; //$NON-NLS-1$
	private static final String MAKE_UPDATE_DESCRIPTION = "MakeWizardUpdatePage.description"; //$NON-NLS-1$

	private IProject[] selected;
	private CheckboxTableViewer makeProjectListViewer;
	private TablePart tablePart;

	public class MakeProjectContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object parent) {
			return UpdateMakeProjectAction.getOldProjects();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class TablePart extends WizardCheckboxTablePart {
		public TablePart(String mainLabel) {
			super(mainLabel);
		}
		public void updateCounter(int count) {
			super.updateCounter(count);
			dialogChanged();
		}
		protected StructuredViewer createStructuredViewer(Composite parent, int style) {
			StructuredViewer viewer = super.createStructuredViewer(parent, style);
			return viewer;
		}
	}

	public UpdateMakeProjectWizardPage(IProject[] selected) {
		super("UpdateMakeProjectWizardPage", true); //$NON-NLS-1$
		setTitle(MakeUIPlugin.getResourceString(MAKE_UPDATE_TITLE));
		setDescription(MakeUIPlugin.getResourceString(MAKE_UPDATE_DESCRIPTION));
		this.selected = selected;
		tablePart = new TablePart(MakeUIPlugin.getResourceString("MakeWizardUpdatePage.projectList")); //$NON-NLS-1$
	}

	public void dispose() {
		super.dispose();
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 5;
		container.setLayout(layout);

		tablePart.createControl(container);
		makeProjectListViewer = tablePart.getTableViewer();
		makeProjectListViewer.setContentProvider(new MakeProjectContentProvider());
		makeProjectListViewer.setLabelProvider(new WorkbenchLabelProvider());

		GridData gd = (GridData) tablePart.getControl().getLayoutData();
		gd.heightHint = 300;
		gd.widthHint = 300;

		makeProjectListViewer.setInput(MakeUIPlugin.getWorkspace().getRoot());
		tablePart.setSelection(selected);
		setControl(container);
		Dialog.applyDialogFont(container);
	}

	public void storeSettings() {
	}

	public Object[] getSelected() {
		return tablePart.getSelection();
	}

	void dialogChanged() {
		IStatus genStatus = validatePlugins();
		updateStatus(genStatus);
	}


	private IStatus validatePlugins() {
		Object[] allModels = UpdateMakeProjectAction.getOldProjects();
		if (allModels == null || allModels.length == 0) {
			return createStatus(IStatus.ERROR, MakeUIPlugin.getResourceString("MakeWizardUpdatePage.status.noProjectsToUpdate")); //$NON-NLS-1$
		}
		if (tablePart.getSelectionCount() == 0) {
			return createStatus(IStatus.ERROR, MakeUIPlugin.getResourceString("MakeWizardUpdatePage.status.noProjectsSelected")); //$NON-NLS-1$
		}
		return createStatus(IStatus.OK, ""); //$NON-NLS-1$
	}
}
