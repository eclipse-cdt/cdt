package org.eclipse.cdt.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
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
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.internal.ui.CPlugin;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.swt.IValidation;

public class ReferenceBlock implements IWizardTab {

	private static final String PREFIX = "ReferenceBlock"; // $NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; // $NON-NLS-1$
	private static final String DESC = PREFIX + ".desc"; // $NON-NLS-1$

	private CheckboxTableViewer referenceProjectsViewer;
	private static final int PROJECT_LIST_MULTIPLIER = 15;
	
	IProject project;
	IValidation page;

	/** (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public Composite getControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(composite, SWT.LEFT);
		label.setText(CPlugin.getResourceString(DESC));
		GridData lbldata = new GridData(GridData.FILL_HORIZONTAL);
		lbldata.horizontalSpan = 1;
		label.setLayoutData(lbldata);

		referenceProjectsViewer = ControlFactory.createListViewer
			(composite, null, SWT.DEFAULT, SWT.DEFAULT, GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);

		referenceProjectsViewer.setLabelProvider(new WorkbenchLabelProvider());
		referenceProjectsViewer.setContentProvider(getContentProvider());
		referenceProjectsViewer.setInput(ResourcesPlugin.getWorkspace());

		initializeValues();

		return composite;
	}

	public Image getImage() {
		return CPluginImages.get(CPluginImages.IMG_OBJS_PROJECT);
	}

	public String getLabel() {
		return CPlugin.getResourceString(LABEL);
	}

	/**
	 * Returns a content provider for the reference project
	 * viewer. It will return all projects in the workspace.
	 *
	 * @return the content provider
	 */
	protected IStructuredContentProvider getContentProvider() {
		return new WorkbenchContentProvider() {
			public Object[] getChildren(Object element) {
				if (!(element instanceof IWorkspace))
					return new Object[0];
				ArrayList aList = new ArrayList(15);
				final IProject[] projects = ((IWorkspace)element).getRoot().getProjects();
				for (int i = 0; i < projects.length; i++) {
					if (CoreModel.hasCNature(projects[i])) {
						// Do not show the actual project being look at
						if ((project != null) && project.equals(projects[i])) {
							continue;
						}
						aList.add(projects[i]);
					}
				}
				return aList.toArray();
			}
		};
	}

	/**
	 * Get the default widget height for the supplied control.
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

	protected void initializeValues () {
		if (project != null) {
			try {
				IProject[] referenced = project.getReferencedProjects();
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

	public boolean isValid() {
		return true;
	}
	
	public void setVisible(boolean visible) {
	}

	public void doRun(IProject project, IProgressMonitor monitor) {
		IProject[] refProjects = getReferencedProjects();
		if (refProjects != null) {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask("Reference Projects", 1);
			try {
				IProjectDescription description = project.getDescription();
				description.setReferencedProjects(refProjects);
				project.setDescription(description, new SubProgressMonitor(monitor, 1));
			} catch (CoreException e) {
			}
		}
	}

	/**
	 *
	 */
	public ReferenceBlock(IValidation page) {
		this(page, null);
	}

	public ReferenceBlock(IValidation page, IProject project) {
		super();
		this.page = page;
		this.project = project;
	}
}
