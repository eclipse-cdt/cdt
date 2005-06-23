/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.folderwizard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.internal.core.model.PathEntryManager;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.TypedElementSelectionValidator;
import org.eclipse.cdt.internal.ui.dialogs.TypedViewerFilter;
import org.eclipse.cdt.internal.ui.util.CoreUtility;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.wizards.NewElementWizardPage;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;

public class NewSourceFolderWizardPage extends NewElementWizardPage {
		
	private static final String PAGE_NAME= "NewSourceFolderWizardPage"; //$NON-NLS-1$

	private StringButtonDialogField fProjectField;
	private StatusInfo fProjectStatus;
	
	private StringButtonDialogField fRootDialogField;
	private StatusInfo fRootStatus;
	
	private SelectionButtonDialogField fExcludeInOthersFields;
	
	private IWorkspaceRoot fWorkspaceRoot;
	
	private ICProject fCurrCProject;
	private IPathEntry[] fEntries;
	
	private IPathEntry[] fNewEntries;
	
	private boolean fIsProjectAsSourceFolder;
	
	private ISourceRoot fCreatedRoot;
	
	public NewSourceFolderWizardPage() {
		super(PAGE_NAME);
		
		setTitle(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.title")); //$NON-NLS-1$
		setDescription(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.description"));		 //$NON-NLS-1$
		
		fWorkspaceRoot= ResourcesPlugin.getWorkspace().getRoot();
		
		RootFieldAdapter adapter= new RootFieldAdapter();
		
		fProjectField= new StringButtonDialogField(adapter);
		fProjectField.setDialogFieldListener(adapter);
		fProjectField.setLabelText(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.project.label")); //$NON-NLS-1$
		fProjectField.setButtonLabel(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.project.button"));	 //$NON-NLS-1$
		
		fRootDialogField= new StringButtonDialogField(adapter);
		fRootDialogField.setDialogFieldListener(adapter);
		fRootDialogField.setLabelText(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.root.label")); //$NON-NLS-1$
		fRootDialogField.setButtonLabel(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.root.button")); //$NON-NLS-1$
		
		fExcludeInOthersFields= new SelectionButtonDialogField(SWT.CHECK);
		fExcludeInOthersFields.setDialogFieldListener(adapter);
		fExcludeInOthersFields.setLabelText(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.exclude.label")); //$NON-NLS-1$
		
//TODO		fExcludeInOthersFields.setEnabled(CoreModel.ENABLED.equals(CoreModel.getOption(CoreModel.CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS)));
		
		fRootStatus= new StatusInfo();
		fProjectStatus= new StatusInfo();
	}
			
	// -------- Initialization ---------
		
	public void init(IStructuredSelection selection) {
		if (selection == null || selection.isEmpty()) {
			setDefaultAttributes();
			return;
		}
		
		Object selectedElement= selection.getFirstElement();
		if (selectedElement == null) {
			selectedElement= EditorUtility.getActiveEditorCInput();
		}				
		
		String projPath= null;
		
		if (selectedElement instanceof IResource) {
			IProject proj= ((IResource)selectedElement).getProject();
			if (proj != null) {
				projPath= proj.getFullPath().makeRelative().toString();
			}	
		} else if (selectedElement instanceof ICElement) {
			ICProject jproject= ((ICElement)selectedElement).getCProject();
			if (jproject != null) {
				projPath= jproject.getProject().getFullPath().makeRelative().toString();
			}
		}	
		
		if (projPath != null) {
			fProjectField.setText(projPath);
			fRootDialogField.setText(""); //$NON-NLS-1$
		} else {
			setDefaultAttributes();
		}
	}
	
	private void setDefaultAttributes() {
		String projPath= ""; //$NON-NLS-1$
		
		try {
			// find the first C project
			IProject[] projects= fWorkspaceRoot.getProjects();
			for (int i= 0; i < projects.length; i++) {
				IProject proj= projects[i];
				if (proj.hasNature(CProjectNature.C_NATURE_ID) || proj.hasNature(CCProjectNature.CC_NATURE_ID)) {
					projPath= proj.getFullPath().makeRelative().toString();
					break;
				}
			}					
		} catch (CoreException e) {
			// ignore here
		}
		fProjectField.setText(projPath);
		fRootDialogField.setText("");		 //$NON-NLS-1$
	}
	

	// -------- UI Creation ---------

	/*
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		
		Composite composite= new Composite(parent, SWT.NONE);
			
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;	
		layout.numColumns= 3;
		composite.setLayout(layout);
				
		fProjectField.doFillIntoGrid(composite, 3);	
		fRootDialogField.doFillIntoGrid(composite, 3);
		fExcludeInOthersFields.doFillIntoGrid(composite, 3);
		
		int maxFieldWidth= convertWidthInCharsToPixels(40);
		LayoutUtil.setWidthHint(fProjectField.getTextControl(null), maxFieldWidth);
		LayoutUtil.setHorizontalGrabbing(fProjectField.getTextControl(null));	
		LayoutUtil.setWidthHint(fRootDialogField.getTextControl(null), maxFieldWidth);	
			
		setControl(composite);
		Dialog.applyDialogFont(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ICHelpContextIds.NEW_SRCFLDER_WIZARD_PAGE);		
	}
	
	/*
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			fRootDialogField.setFocus();
		}
	}	
		
	// -------- ContainerFieldAdapter --------

	private class RootFieldAdapter implements IStringButtonAdapter, IDialogFieldListener {

		// -------- IStringButtonAdapter
		public void changeControlPressed(DialogField field) {
			packRootChangeControlPressed(field);
		}
		
		// -------- IDialogFieldListener
		public void dialogFieldChanged(DialogField field) {
			packRootDialogFieldChanged(field);
		}
	}
	protected void packRootChangeControlPressed(DialogField field) {
		if (field == fRootDialogField) {
			IPath initialPath= new Path(fRootDialogField.getText());
			String title= NewFolderWizardMessages.getString("NewSourceFolderWizardPage.ChooseExistingRootDialog.title"); //$NON-NLS-1$
			String message= NewFolderWizardMessages.getString("NewSourceFolderWizardPage.ChooseExistingRootDialog.description"); //$NON-NLS-1$
			IFolder folder= chooseFolder(title, message, initialPath);
			if (folder != null) {
				IPath path= folder.getFullPath().removeFirstSegments(1);
				fRootDialogField.setText(path.toString());
			}
		} else if (field == fProjectField) {
			ICProject jproject= chooseProject();
			if (jproject != null) {
				IPath path= jproject.getProject().getFullPath().makeRelative();
				fProjectField.setText(path.toString());
			}
		} 
	}	
	
	protected void packRootDialogFieldChanged(DialogField field) {
		if (field == fRootDialogField) {
			updateRootStatus();
		} else if (field == fProjectField) {
			updateProjectStatus();
			updateRootStatus();
		} else if (field == fExcludeInOthersFields) {
			updateRootStatus();
		}
		updateStatus(new IStatus[] { fProjectStatus, fRootStatus });
	}
	
	
	private void updateProjectStatus() {
		fCurrCProject= null;
		fIsProjectAsSourceFolder= false;
		
		String str= fProjectField.getText();
		if (str.length() == 0) {
			fProjectStatus.setError(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.error.EnterProjectName")); //$NON-NLS-1$
			return;
		}
		IPath path= new Path(str);
		if (path.segmentCount() != 1) {
			fProjectStatus.setError(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.error.InvalidProjectPath")); //$NON-NLS-1$
			return;
		}
		IProject project= fWorkspaceRoot.getProject(path.toString());
		if (!project.exists()) {
			fProjectStatus.setError(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.error.ProjectNotExists")); //$NON-NLS-1$
			return;
		}
		try {
			if (project.hasNature(CProjectNature.C_NATURE_ID) || project.hasNature(CCProjectNature.CC_NATURE_ID)) {
				fCurrCProject= CoreModel.getDefault().create(project);
				fEntries= fCurrCProject.getRawPathEntries();
				fProjectStatus.setOK();
				return;
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
			fCurrCProject= null;
		}	
		fProjectStatus.setError(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.error.NotACProject")); //$NON-NLS-1$
	}

	private void updateRootStatus() {
		fRootDialogField.enableButton(fCurrCProject != null);
		fIsProjectAsSourceFolder= false;
		if (fCurrCProject == null) {
			return;
		}
		fRootStatus.setOK();
		
		IPath projPath= fCurrCProject.getProject().getFullPath();
		String str= fRootDialogField.getText();
		if (str.length() == 0) {
			fRootStatus.setError(NewFolderWizardMessages.getFormattedString("NewSourceFolderWizardPage.error.EnterRootName", fCurrCProject.getProject().getFullPath().toString())); //$NON-NLS-1$
		} else {
			IPath path= projPath.append(str);
			IStatus validate= fWorkspaceRoot.getWorkspace().validatePath(path.toString(), IResource.FOLDER);
			if (validate.matches(IStatus.ERROR)) {
				fRootStatus.setError(NewFolderWizardMessages.getFormattedString("NewSourceFolderWizardPage.error.InvalidRootName", validate.getMessage())); //$NON-NLS-1$
			} else {
				IResource res= fWorkspaceRoot.findMember(path);
				if (res != null) {
					if (res.getType() != IResource.FOLDER) {
						fRootStatus.setError(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.error.NotAFolder")); //$NON-NLS-1$
						return;
					}
				}
				ArrayList newEntries= new ArrayList(fEntries.length + 1);
				int projectEntryIndex= -1;
				
				for (int i= 0; i < fEntries.length; i++) {
					IPathEntry curr= fEntries[i];
					if (curr.getEntryKind() == IPathEntry.CDT_SOURCE) {
						if (path.equals(curr.getPath())) {
							fRootStatus.setError(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.error.AlreadyExisting")); //$NON-NLS-1$
							return;
						}
						if (projPath.equals(curr.getPath())) {
							projectEntryIndex= i;
						}	
					}
					newEntries.add(curr);
				}
				
				IPathEntry newEntry= CoreModel.newSourceEntry(path);
				
				Set modified= new HashSet();				
				if (fExcludeInOthersFields.isSelected()) {
					addExclusionPatterns(newEntry, newEntries, modified);
					newEntries.add(CoreModel.newSourceEntry(path));
				} else {
					if (projectEntryIndex != -1) {
						fIsProjectAsSourceFolder= true;
						newEntries.set(projectEntryIndex, newEntry);
					} else {
						newEntries.add(CoreModel.newSourceEntry(path));
					}
				}
					
				fNewEntries= (IPathEntry[]) newEntries.toArray(new IPathEntry[newEntries.size()]);

				ICModelStatus status= PathEntryManager.getDefault().validatePathEntry(fCurrCProject, fNewEntries);
				if (!status.isOK()) {
					IStatus status2= PathEntryManager.getDefault().validatePathEntry(fCurrCProject, fNewEntries);
					if (status2.isOK()) {
						return;
					}
					fRootStatus.setError(status.getMessage());
					return;
				} else if (fIsProjectAsSourceFolder) {
					fRootStatus.setInfo(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.warning.ReplaceSF")); //$NON-NLS-1$
					return;
				}
				if (!modified.isEmpty()) {
					fRootStatus.setInfo(NewFolderWizardMessages.getFormattedString("NewSourceFolderWizardPage.warning.AddedExclusions", String.valueOf(modified.size()))); //$NON-NLS-1$
					return;
				}
			}
		}
	}
	
	private void addExclusionPatterns(IPathEntry newEntry, List existing, Set modifiedEntries) {
		IPath entryPath= newEntry.getPath();
		for (int i= 0; i < existing.size(); i++) {
			IPathEntry curr= (IPathEntry) existing.get(i);
			IPath currPath= curr.getPath();
			if (curr.getEntryKind() == IPathEntry.CDT_SOURCE && currPath.isPrefixOf(entryPath)) {
				IPath[] exclusionFilters= ((ISourceEntry)curr).getExclusionPatterns();
				if (!CoreModelUtil.isExcludedPath(entryPath, exclusionFilters)) {
					IPath pathToExclude= entryPath.removeFirstSegments(currPath.segmentCount()).addTrailingSeparator();
					IPath[] newExclusionFilters= new IPath[exclusionFilters.length + 1];
					System.arraycopy(exclusionFilters, 0, newExclusionFilters, 0, exclusionFilters.length);
					newExclusionFilters[exclusionFilters.length]= pathToExclude;
					
					IPathEntry updated= CoreModel.newSourceEntry(currPath, newExclusionFilters);
					existing.set(i, updated);
					modifiedEntries.add(updated);
				}
			}
		}
	}	
	
	// ---- creation ----------------
	
	public ISourceRoot getNewSourceRoot() {
		return fCreatedRoot;
	}
	
	public IResource getCorrespondingResource() {
		return fCurrCProject.getProject().getFolder(fRootDialogField.getText());
	}
	
	public void createSourceRoot(IProgressMonitor monitor) throws CoreException, InterruptedException {
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		monitor.beginTask(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.operation"), 3); //$NON-NLS-1$
		try {
			IPath projPath= fCurrCProject.getProject().getFullPath();
			String relPath= fRootDialogField.getText();
				
			IFolder folder= fCurrCProject.getProject().getFolder(relPath);
			if (!folder.exists()) {
				CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 1));			
			}
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			
			fCurrCProject.setRawPathEntries(fNewEntries, new SubProgressMonitor(monitor, 2));
	
			fCreatedRoot= fCurrCProject.findSourceRoot(folder);
		} finally {
			monitor.done();
		}
	}
	
	// ------------- choose dialogs
	
	private IFolder chooseFolder(String title, String message, IPath initialPath) {	
		Class[] acceptedClasses= new Class[] { IFolder.class };
		ISelectionStatusValidator validator= new TypedElementSelectionValidator(acceptedClasses, false);
		ViewerFilter filter= new TypedViewerFilter(acceptedClasses, null);	
		
		ILabelProvider lp= new WorkbenchLabelProvider();
		ITreeContentProvider cp= new WorkbenchContentProvider();

		IProject currProject= fCurrCProject.getProject();

		ElementTreeSelectionDialog dialog= new ElementTreeSelectionDialog(getShell(), lp, cp);
		dialog.setValidator(validator);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.addFilter(filter);
		dialog.setInput(currProject);
		dialog.setSorter(new ResourceSorter(ResourceSorter.NAME));
		IResource res= currProject.findMember(initialPath);
		if (res != null) {
			dialog.setInitialSelection(res);
		}

		if (dialog.open() == Window.OK) {
			return (IFolder) dialog.getFirstResult();
		}			
		return null;		
	}
	
	private ICProject chooseProject() {
		ICProject[] projects;
		try {
			projects= CoreModel.create(fWorkspaceRoot).getCProjects();
		} catch (CModelException e) {
			CUIPlugin.getDefault().log(e);
			projects= new ICProject[0];
		}
		
		ILabelProvider labelProvider= new CElementLabelProvider(CElementLabelProvider.SHOW_DEFAULT);
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.ChooseProjectDialog.title")); //$NON-NLS-1$
		dialog.setMessage(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.ChooseProjectDialog.description")); //$NON-NLS-1$
		dialog.setElements(projects);
		dialog.setInitialSelections(new Object[] { fCurrCProject });
		if (dialog.open() == Window.OK) {			
			return (ICProject) dialog.getFirstResult();
		}			
		return null;		
	}
				
}
