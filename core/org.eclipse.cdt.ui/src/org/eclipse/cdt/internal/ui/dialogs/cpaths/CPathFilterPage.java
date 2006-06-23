/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.viewsupport.ListContentProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class CPathFilterPage extends WizardPage {

	private final int[] fFilterType;

	private CheckboxTableViewer viewer;
	private IPathEntry fParentEntry;
	private List fPaths;
	private List fExclusions;
	private ViewerFilter filter;
	
	protected ICElement fCElement;

	protected CPathFilterPage(ICElement cElement, int[] filterType) {
		super("CPathFilterPage"); //$NON-NLS-1$
		setTitle(CPathEntryMessages.getString("CPathFilterPage.title")); //$NON-NLS-1$
		setDescription(CPathEntryMessages.getString("CPathFilterPage.description")); //$NON-NLS-1$
		setImageDescriptor(CPluginImages.DESC_WIZBAN_ADD_LIBRARY);
		fFilterType = filterType;
		fCElement = cElement;
		validatePage();
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		Label label = new Label(container, SWT.NULL);
		label.setText(CPathEntryMessages.getString("CPathFilterPage.label")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		viewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.setContentProvider(new ListContentProvider());
		viewer.setLabelProvider(new CPElementLabelProvider(false, false));
		viewer.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				validatePage();
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 400;
		gd.heightHint = 300;
		viewer.getTable().setLayoutData(gd);
		setControl(container);
		Dialog.applyDialogFont(container);
	}

	public void setVisible(boolean visible) {
		if (fPaths != null) {
			viewer.setInput(fPaths);
		}
		super.setVisible(visible);
	}

	public void setParentEntry(IPathEntry entry) {
		fParentEntry = entry;
		if (fParentEntry.getEntryKind() == IPathEntry.CDT_PROJECT) {
			IProject project = CUIPlugin.getWorkspace().getRoot().getProject(fParentEntry.getPath().segment(0));
			if (project.isAccessible()) {
				ICProject cProject = CoreModel.getDefault().create(project);
				try {
					fPaths = Arrays.asList(cProject.getRawPathEntries());
				} catch (CModelException e) {
				}
			}
		} else if (fParentEntry.getEntryKind() == IPathEntry.CDT_CONTAINER) {
			try {
				IPathEntryContainer container = CoreModel.getPathEntryContainer(fParentEntry.getPath(), fCElement.getCProject());
				if (container != null) {
					fPaths = Arrays.asList(container.getPathEntries());
				}
			} catch (CModelException e) {
			}
		}
		createExlusions(fParentEntry.getEntryKind() == IPathEntry.CDT_PROJECT);
	}

	
	private void createExlusions(boolean showExported) {
		fExclusions = new ArrayList();
		if (filter != null) {
			viewer.removeFilter(filter);
		}
		filter = new CPElementFilter(fExclusions.toArray(), fFilterType, showExported, false);
		viewer.addFilter(filter);
	}

	/**
	 * Method validatePage.
	 */
	void validatePage() {
		setPageComplete(getSelectedEntries().length > 0);
	}

	public IPathEntry[] getSelectedEntries() {
		if (viewer != null) {
			Object[] paths = viewer.getCheckedElements();
			IPathEntry[] entries = new IPathEntry[paths.length];
			System.arraycopy(paths, 0, entries, 0, entries.length);
			return entries;
		}
		return new IPathEntry[0];
	}

}
