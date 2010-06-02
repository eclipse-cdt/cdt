/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;

public class ProjectSelectionPage extends WizardPage implements IWizardDataPage {
		
	private static final String PAGE_NAME= "NewProjectSelectionWizardPage"; //$NON-NLS-1$
	private static final String PAGE_TITLE = Messages.getString("ProjectSelectionPage.0"); //$NON-NLS-1$
	private static final String PAGE_DESCRIPTION = Messages.getString("ProjectSelectionPage.1"); //$NON-NLS-1$

	private Label projectNameLabel;
	private Button projectBrowseButton;
	private Text projectNameText;
	private String projectName = ""; //$NON-NLS-1$

	private IWorkspaceRoot workspaceRoot;
	private ICProject currentCProject;
	private IWizardPage next;
	
	public ProjectSelectionPage() {
		super(PAGE_NAME);
		setTitle(PAGE_TITLE);
		setDescription(PAGE_DESCRIPTION);
		
		workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		setPageComplete(false);
	}
			
	public void init(IStructuredSelection selection) {
		if (selection == null || selection.isEmpty()) {
			setDefaultAttributes();
			return;
		}
		
		Object selectedElement= selection.getFirstElement();
		if (selectedElement == null) {
			selectedElement= getActiveEditorCInput();
		}				
		
		String projPath= null;
		
		if (selectedElement instanceof IResource) {
			IProject project= ((IResource)selectedElement).getProject();
			if (project != null) {
				projPath= project.getFullPath().makeRelative().toString();
			}	
		} else if (selectedElement instanceof ICElement) {
			ICProject cProject= ((ICElement)selectedElement).getCProject();
			if (cProject != null) {
				projPath= cProject.getProject().getFullPath().makeRelative().toString();
			}
		}	
		
		if (projPath != null) {
			projectName = projPath;
		} else {
			setDefaultAttributes();
		}
	}

	/**
	 * If the current active editor edits a c element return it, else
	 * return null
	 */
	private ICElement getActiveEditorCInput() {
		IWorkbenchPage page= CUIPlugin.getActivePage();
		if (page != null) {
			IEditorPart part= page.getActiveEditor();
			if (part != null) {
				IEditorInput editorInput= part.getEditorInput();
				if (editorInput != null) {
					return (ICElement)editorInput.getAdapter(ICElement.class);
				}
			}
		}
		return null;    
	}
        
	private void setDefaultAttributes() {
		
		try {
			// find the first C project
			IProject[] projects= workspaceRoot.getProjects();
			for (int i= 0; i < projects.length; i++) {
				IProject project= projects[i];
				if (project.hasNature(CProjectNature.C_NATURE_ID) || project.hasNature(CCProjectNature.CC_NATURE_ID)) {
					projectName = project.getFullPath().makeRelative().toString();
					break;
				}
			}					
		} catch (CoreException e) {
			// ignore here
		}
	}
	
	private Map<String, String> data = new HashMap<String, String>(2);
	
	public Map<String, String> getPageData() {
		String cPojectName = currentCProject.getResource().getName().trim();
		data.put("projectName", cPojectName); //$NON-NLS-1$
		data.put("baseName", getBaseName(cPojectName)); //$NON-NLS-1$
		return data;
	}

	private String getBaseName(String name) {
		String baseName = name;
		int dot = baseName.lastIndexOf('.');
		if (dot != -1) {
			baseName = baseName.substring(dot + 1);
		}
		dot = baseName.indexOf(' ');
		if (dot != -1) {
			baseName = baseName.substring(0, dot);
		}
		return baseName;
	}
	
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		
		Composite composite= new Composite(parent, SWT.NONE);
			
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;	
		layout.numColumns= 3;
		composite.setLayout(layout);
		
		createProjectFiled(composite);
		
		setControl(composite);
		Dialog.applyDialogFont(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ICHelpContextIds.NEW_SRCFLDER_WIZARD_PAGE);		
	
		projectNameText.setFocus();
		projectNameText.setSelection(0, projectNameText.getText().length());
		
		setPageComplete(validatePage());
	}
	
	private void createProjectFiled(Composite parent) {
		getLabelControl(parent);
		GridData gdLabel = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gdLabel.horizontalSpan= 1;
		projectNameLabel.setLayoutData(gdLabel);
		
		
		getTextControl(parent);
		GridData gdText = new GridData();
		gdText.horizontalAlignment= GridData.FILL;
		gdText.grabExcessHorizontalSpace= true;
		gdText.horizontalSpan= 1;
		gdText.widthHint = convertWidthInCharsToPixels(40);
		projectNameText.setLayoutData(gdText);
		
		getButtonControl(parent);
		GridData gdButton = new GridData();
		gdButton.horizontalAlignment= GridData.FILL;
		gdButton.grabExcessHorizontalSpace= false;
		gdButton.horizontalSpan= 1;
		projectBrowseButton.setLayoutData(gdButton);
	}

	/**
	 * Creates or returns the created Label control.
	 * @param parent The parent composite
	 */		
	private void getLabelControl(Composite parent) {
		projectNameLabel = new Label(parent, SWT.LEFT | SWT.WRAP);
		projectNameLabel.setText(Messages.getString("ProjectSelectionPage.4")); //$NON-NLS-1$
		projectNameLabel.setFont(parent.getFont());
		projectNameLabel.setEnabled(true);		
	}

	/**
	 * Creates or returns the created text control.
	 * @param parent The parent composite
	 */		
	private void getTextControl(Composite parent) {
		projectNameText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		projectNameText.setText(projectName);
		projectNameText.setFont(parent.getFont());
		projectNameText.setEnabled(true);
		projectNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
			}
		});
	}

	/**
	 * Creates or returns the created buttom widget.
	 * @param parent The parent composite
	 */		
	private void getButtonControl(Composite parent) {
		projectBrowseButton = new Button(parent, SWT.PUSH);
		projectBrowseButton.setText(Messages.getString("ProjectSelectionPage.5")); //$NON-NLS-1$
		projectBrowseButton.setFont(parent.getFont());
		projectBrowseButton.setEnabled(true);
		projectBrowseButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				packRootChangeControlPressed();
			}
			public void widgetSelected(SelectionEvent e) {
				packRootChangeControlPressed();
			}
		});	
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
	}	
		
	protected void packRootChangeControlPressed() {
		ICProject cProject= chooseProject();
		if (cProject != null) {
			IPath path= cProject.getProject().getFullPath().makeRelative();
			projectName = path.toOSString();
			projectNameText.setText(projectName);
		}
	}	
	
    private boolean validatePage() {
		currentCProject= null;
		
		String projectName = projectNameText.getText();
		if (projectName.length() == 0) {
            setErrorMessage(Messages.getString("ProjectSelectionPage.6")); //$NON-NLS-1$
			return false;
		}
		
		IPath path= new Path(projectName);
		if (path.segmentCount() != 1) {
            setErrorMessage(Messages.getString("ProjectSelectionPage.7")); //$NON-NLS-1$
			return false;
		}
		
		IProject project= workspaceRoot.getProject(path.toString());
		if (!project.exists()) {
            setErrorMessage(Messages.getString("ProjectSelectionPage.8")); //$NON-NLS-1$
			return false;
		}
		
		try {
			if (project.hasNature(CProjectNature.C_NATURE_ID) || project.hasNature(CCProjectNature.CC_NATURE_ID)) {
				currentCProject= CoreModel.getDefault().create(project);
		        setErrorMessage(null);
				return true;
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
			currentCProject= null;
		}
		
        setErrorMessage(Messages.getString("ProjectSelectionPage.9")); //$NON-NLS-1$
		return false;
    }
    
	private ICProject chooseProject() {
		ICProject[] projects;
		try {
			projects= CoreModel.create(workspaceRoot).getCProjects();
		} catch (CModelException e) {
			CUIPlugin.log(e);
			projects= new ICProject[0];
		}
		
		ILabelProvider labelProvider= new CElementLabelProvider(CElementLabelProvider.SHOW_DEFAULT);
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(Messages.getString("ProjectSelectionPage.10")); //$NON-NLS-1$
		dialog.setMessage(Messages.getString("ProjectSelectionPage.11")); //$NON-NLS-1$
		dialog.setElements(projects);
		dialog.setInitialSelections(new Object[] { currentCProject });
		if (dialog.open() == Window.OK) {			
			return (ICProject) dialog.getFirstResult();
		}			
		return null;		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.ui.templateengine.IWizardDataPage#setNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public void setNextPage(IWizardPage next) {
		this.next= next;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	@Override
	public IWizardPage getNextPage() {
		if(next != null) {
			return next;
		}
		return super.getNextPage();
	}
}
