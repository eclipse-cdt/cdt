/*******************************************************************************
 * Copyright (c) 2003, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * @deprecated as of CDT 4.0. This Block was used for New Project Wizard
 * for 3.X style projects.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
@Deprecated
public class ReferenceBlock extends AbstractCOptionPage {

	private static final String PREFIX = "ReferenceBlock"; //$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; //$NON-NLS-1$
	private static final String DESC = PREFIX + ".desc"; //$NON-NLS-1$

	private CheckboxTableViewer referenceProjectsViewer;

	private static final int PROJECT_LIST_MULTIPLIER = 10;

	public ReferenceBlock() {
		super(CUIPlugin.getResourceString(LABEL));
		setDescription(CUIPlugin.getResourceString(DESC));
	}

	@Override
	public Image getImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);
	}

	/**
	 * Returns a content provider for the reference project
	 * viewer. It will return all projects in the workspace.
	 *
	 * @return the content provider
	 */
	protected IStructuredContentProvider getContentProvider() {
		return new WorkbenchContentProvider() {
			@Override
			public Object[] getChildren(Object element) {
				if (!(element instanceof IWorkspace))
					return new Object[0];
				ArrayList<IProject> aList = new ArrayList<>(15);
				final IProject[] projects = ((IWorkspace) element).getRoot().getProjects();
				for (int i = 0; i < projects.length; i++) {
					if (CoreModel.hasCNature(projects[i])) {
						// Do not show the actual project being look at
						if ((getContainer().getProject() != null) && getContainer().getProject().equals(projects[i])) {
							continue;
						}
						aList.add(projects[i]);
					}
				}
				return aList.toArray();
			}
		};
	}

	protected void initializeValues() {
		if (getContainer().getProject() != null) {
			try {
				IProject[] referenced = getContainer().getProject().getReferencedProjects();
				referenceProjectsViewer.setCheckedElements(referenced);
			} catch (CoreException e) {
			}
		}
	}

	/**
	 * Returns the referenced projects selected by the user.
	 *
	 * @return the referenced projects
	 */
	public IProject[] getReferencedProjects() {
		Object[] elements = referenceProjectsViewer.getCheckedElements();
		IProject[] projects = new IProject[elements.length];
		System.arraycopy(elements, 0, projects, 0, elements.length);
		return projects;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(composite, SWT.LEFT);
		label.setText(CUIPlugin.getResourceString(DESC));
		GridData lbldata = new GridData(GridData.FILL_HORIZONTAL);
		lbldata.horizontalSpan = 1;
		label.setLayoutData(lbldata);

		referenceProjectsViewer = CheckboxTableViewer.newCheckList(composite, SWT.TOP | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.heightHint = getDefaultFontHeight(referenceProjectsViewer.getTable(), PROJECT_LIST_MULTIPLIER);

		//Only set a height hint if it will not result in a cut off dialog
		referenceProjectsViewer.getTable().setLayoutData(data);

		referenceProjectsViewer.setLabelProvider(new WorkbenchLabelProvider());
		referenceProjectsViewer.setContentProvider(getContentProvider());
		referenceProjectsViewer.setInput(ResourcesPlugin.getWorkspace());

		initializeValues();
		setControl(composite);
	}

	/**
	 * Get the defualt widget height for the supplied control.
	 * @return int
	 * @param control - the control being queried about fonts
	 * @param lines - the number of lines to be shown on the table.
	 */
	private static int getDefaultFontHeight(Control control, int lines) {
		FontData[] viewerFontData = control.getFont().getFontData();
		int fontHeight = 10;

		//If we have no font data use our guess
		if (viewerFontData.length > 0)
			fontHeight = viewerFontData[0].getHeight();
		return lines * fontHeight;

	}

	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
		IProject[] refProjects = getReferencedProjects();
		if (refProjects != null) {
			IProject project = getContainer().getProject();
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask(CUIMessages.ReferenceBlock_task_ReferenceProjects, 1);
			try {
				IProjectDescription description = project.getDescription();
				description.setReferencedProjects(refProjects);
				project.setDescription(description, SubMonitor.convert(monitor, 1));
			} catch (CoreException e) {
			}
		}

	}

	@Override
	public void performDefaults() {
		initializeValues();
	}
}
