/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IProjectEntry;
import org.eclipse.cdt.ui.wizards.IPathEntryContainerPage;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.viewsupport.ListContentProvider;

/**
 * @deprecated as of CDT 4.0. This class was used for Path Container Wizard
 * for 3.X style projects.
 */
@Deprecated
public class ProjectContainerPage extends WizardPage implements IPathEntryContainerPage {

	private int[] fFilterType;
	private TableViewer viewer;
	ICProject fCProject;

	protected ProjectContainerPage(int[] filterType) {
		super("projectContainerPage"); //$NON-NLS-1$
		setTitle(CPathEntryMessages.ProjectContainerPage_title); 
		setDescription(CPathEntryMessages.ProjectContainerPage_description); 
		setImageDescriptor(CPluginImages.DESC_WIZBAN_ADD_LIBRARY);
		fFilterType = filterType;
		validatePage();
	}

	@Override
	public void initialize(ICProject project, IPathEntry[] currentEntries) {
		fCProject = project;
	}

	@Override
	public boolean finish() {
		return true;
	}

	@Override
	public IContainerEntry[] getNewContainers() {
		return new IContainerEntry[0];
	}
	
	IProjectEntry getProjectEntry() {
		if (viewer != null) {
			ISelection selection = viewer.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (ss.size() == 1) {
					ICProject project = (ICProject) ss.getFirstElement();
					return CoreModel.newProjectEntry(project.getPath());
				}
			}
		}
		return null;
	}
	
	void setProjectEntry(IProjectEntry entry) {
		if (entry != null) {
			viewer.setSelection(new StructuredSelection(entry));
		}
	}
	
	@Override
	public void setSelection(IContainerEntry containerEntry) {
	}

	@Override
	public void createControl(Composite parent) {
		// create a composite with standard margins and spacing
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		Label label = new Label(container, SWT.NULL);
		label.setText(CPathEntryMessages.ProjectContainerPage_label); 
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		viewer = new TableViewer(container, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		viewer.setContentProvider(new ListContentProvider());
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				validatePage();
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 400;
		gd.heightHint = 300;
		viewer.getTable().setLayoutData(gd);
		viewer.addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return !element.equals(fCProject);
			}
		});
		setControl(container);
		initializeView();
		validatePage();
	}

	private void initializeView() {
		List<ICProject> list = new ArrayList<ICProject>();
		List<IPathEntry> current;
		try {
			current = Arrays.asList(fCProject.getRawPathEntries());
			ICProject[] cProjects = CoreModel.getDefault().getCModel().getCProjects();
			for (int i = 0; i < cProjects.length; i++) {
				boolean added = false;
				if (!cProjects[i].equals(fCProject) && !current.contains(CoreModel.newProjectEntry(cProjects[i].getPath()))) {
					IPathEntry[] projEntries = cProjects[i].getRawPathEntries();
					for (IPathEntry projEntrie : projEntries) {
						for (int element : fFilterType) {
							if (projEntrie.getEntryKind() == element && projEntrie.isExported()) {
								list.add(cProjects[i]);
								added = true;
								break;
							}
						}
						if (added) {
							break;
						}
					}
				}
			}
		} catch (CModelException e) {
		}
		viewer.setInput(list);
	}

	/**
	 * Method validatePage.
	 */
	protected void validatePage() {
		setPageComplete(getSelected() != null);
	}

	private IPathEntry getSelected() {
		return getProjectEntry();
	}
}
