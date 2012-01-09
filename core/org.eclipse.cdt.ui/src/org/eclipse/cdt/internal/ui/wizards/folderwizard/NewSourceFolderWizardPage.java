/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
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
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;

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
		
		setTitle(NewFolderWizardMessages.NewSourceFolderWizardPage_title); 
		setDescription(NewFolderWizardMessages.NewSourceFolderWizardPage_description);		 
		
		fWorkspaceRoot= ResourcesPlugin.getWorkspace().getRoot();
		
		RootFieldAdapter adapter= new RootFieldAdapter();
		
		fProjectField= new StringButtonDialogField(adapter);
		fProjectField.setDialogFieldListener(adapter);
		fProjectField.setLabelText(NewFolderWizardMessages.NewSourceFolderWizardPage_project_label); 
		fProjectField.setButtonLabel(NewFolderWizardMessages.NewSourceFolderWizardPage_project_button);	 
		
		fRootDialogField= new StringButtonDialogField(adapter);
		fRootDialogField.setDialogFieldListener(adapter);
		fRootDialogField.setLabelText(NewFolderWizardMessages.NewSourceFolderWizardPage_root_label); 
		fRootDialogField.setButtonLabel(NewFolderWizardMessages.NewSourceFolderWizardPage_root_button); 
		
		fExcludeInOthersFields= new SelectionButtonDialogField(SWT.CHECK);
		fExcludeInOthersFields.setDialogFieldListener(adapter);
		fExcludeInOthersFields.setLabelText(NewFolderWizardMessages.NewSourceFolderWizardPage_exclude_label); 
		
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
			for (IProject proj : projects) {
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
	@Override
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
		LayoutUtil.setHorizontalGrabbing(fProjectField.getTextControl(null), true);	
		LayoutUtil.setWidthHint(fRootDialogField.getTextControl(null), maxFieldWidth);	
			
		// Bug #220003 : consistency between New Source Folder dialog and Source Location Property tab.
		fExcludeInOthersFields.setSelection(true);
		
		setControl(composite);
		Dialog.applyDialogFont(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ICHelpContextIds.NEW_SRCFLDER_WIZARD_PAGE);		
	}
	
	/*
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			fRootDialogField.setFocus();
		}
	}	
		
	// -------- ContainerFieldAdapter --------

	private class RootFieldAdapter implements IStringButtonAdapter, IDialogFieldListener {

		// -------- IStringButtonAdapter
		@Override
		public void changeControlPressed(DialogField field) {
			packRootChangeControlPressed(field);
		}
		
		// -------- IDialogFieldListener
		@Override
		public void dialogFieldChanged(DialogField field) {
			packRootDialogFieldChanged(field);
		}
	}

	protected void packRootChangeControlPressed(DialogField field) {
		if (field == fRootDialogField) {
			IPath initialPath= new Path(fRootDialogField.getText());
			String title= NewFolderWizardMessages.NewSourceFolderWizardPage_ChooseExistingRootDialog_title; 
			String message= NewFolderWizardMessages.NewSourceFolderWizardPage_ChooseExistingRootDialog_description; 
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
			fProjectStatus.setError(NewFolderWizardMessages.NewSourceFolderWizardPage_error_EnterProjectName); 
			return;
		}
		IPath path= new Path(str);
		if (path.segmentCount() != 1) {
			fProjectStatus.setError(NewFolderWizardMessages.NewSourceFolderWizardPage_error_InvalidProjectPath); 
			return;
		}
		IProject project= fWorkspaceRoot.getProject(path.toString());
		if (!project.exists()) {
			fProjectStatus.setError(NewFolderWizardMessages.NewSourceFolderWizardPage_error_ProjectNotExists); 
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
			CUIPlugin.log(e);
			fCurrCProject= null;
		}	
		fProjectStatus.setError(NewFolderWizardMessages.NewSourceFolderWizardPage_error_NotACProject); 
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
			fRootStatus.setError(NLS.bind(NewFolderWizardMessages.NewSourceFolderWizardPage_error_EnterRootName, fCurrCProject.getProject().getFullPath().toString())); 
		} else {
			IPath path= projPath.append(str);
			IStatus validate= fWorkspaceRoot.getWorkspace().validatePath(path.toString(), IResource.FOLDER);
			if (validate.matches(IStatus.ERROR)) {
				fRootStatus.setError(NLS.bind(NewFolderWizardMessages.NewSourceFolderWizardPage_error_InvalidRootName, validate.getMessage())); 
			} else {
				IResource res= fWorkspaceRoot.findMember(path);
				if (res != null) {
					if (res.getType() != IResource.FOLDER) {
						fRootStatus.setError(NewFolderWizardMessages.NewSourceFolderWizardPage_error_NotAFolder); 
						return;
					}
				}
				ArrayList<IPathEntry> newEntries= new ArrayList<IPathEntry>(fEntries.length + 1);
				int projectEntryIndex= -1;
				
				for (int i= 0; i < fEntries.length; i++) {
					IPathEntry curr= fEntries[i];
					if (curr.getEntryKind() == IPathEntry.CDT_SOURCE) {
						if (path.equals(curr.getPath())) {
							fRootStatus.setError(NewFolderWizardMessages.NewSourceFolderWizardPage_error_AlreadyExisting); 
							return;
						}
						if (projPath.equals(curr.getPath())) {
							projectEntryIndex= i;
						}	
					}
					newEntries.add(curr);
				}
				
				IPathEntry newEntry= CoreModel.newSourceEntry(path);
				
				Set<IPathEntry> modified= new HashSet<IPathEntry>();				
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
					
				fNewEntries= newEntries.toArray(new IPathEntry[newEntries.size()]);

				ICModelStatus status= PathEntryManager.getDefault().validatePathEntry(fCurrCProject, fNewEntries);
				if (!status.isOK()) {
					IStatus status2= PathEntryManager.getDefault().validatePathEntry(fCurrCProject, fNewEntries);
					if (status2.isOK()) {
						return;
					}
					fRootStatus.setError(status.getMessage());
					return;
				} else if (fIsProjectAsSourceFolder) {
					fRootStatus.setInfo(NewFolderWizardMessages.NewSourceFolderWizardPage_warning_ReplaceSF); 
					return;
				}
				if (!modified.isEmpty()) {
					fRootStatus.setInfo(NLS.bind(NewFolderWizardMessages.NewSourceFolderWizardPage_warning_AddedExclusions, String.valueOf(modified.size()))); 
					return;
				}
			}
		}
	}
	
	private void addExclusionPatterns(IPathEntry newEntry, List<IPathEntry> existing, Set<IPathEntry> modifiedEntries) {
		IPath entryPath= newEntry.getPath();
		for (int i= 0; i < existing.size(); i++) {
			IPathEntry curr= existing.get(i);
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
		monitor.beginTask(NewFolderWizardMessages.NewSourceFolderWizardPage_operation, 3); 
		try {
//			IPath projPath= fCurrCProject.getProject().getFullPath();
			String relPath= fRootDialogField.getText();
				
			IFolder folder= fCurrCProject.getProject().getFolder(relPath);
			if (!folder.exists()) {
				CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 1));			
			}
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			
			if(CCorePlugin.getDefault().isNewStyleProject(fCurrCProject.getProject())){
				ICSourceEntry newEntry = new CSourceEntry(folder, null, 0); 
				ICProjectDescription des = CCorePlugin.getDefault().getProjectDescription(fCurrCProject.getProject(), true);
				addEntryToAllCfgs(des, newEntry, fIsProjectAsSourceFolder);
				CCorePlugin.getDefault().setProjectDescription(fCurrCProject.getProject(), des, false, new SubProgressMonitor(monitor, 2));
			} else {
				fCurrCProject.setRawPathEntries(fNewEntries, new SubProgressMonitor(monitor, 2));
			}
	
			fCreatedRoot= fCurrCProject.findSourceRoot(folder);
		} finally {
			monitor.done();
		}
	}
	
	private void addEntryToAllCfgs(ICProjectDescription des, ICSourceEntry entry, boolean removeProj) throws WriteAccessException, CoreException{
		ICConfigurationDescription cfgs[] = des.getConfigurations();
		for(ICConfigurationDescription cfg : cfgs){
			ICSourceEntry[] entries = cfg.getSourceEntries();
			entries = addEntry(entries, entry, removeProj);
			cfg.setSourceEntries(entries);
		}
	}
	
	private ICSourceEntry[] addEntry(ICSourceEntry[] entries, ICSourceEntry entry, boolean removeProj){
		Set<ICSourceEntry> set = new HashSet<ICSourceEntry>();
		for(ICSourceEntry se : entries){
			if(removeProj && new Path(se.getValue()).segmentCount() == 1)
				continue;
			set.add(se);
		}
		set.add(entry);
		return set.toArray(new ICSourceEntry[set.size()]);
	}
	
	// ------------- choose dialogs
	
	private IFolder chooseFolder(String title, String message, IPath initialPath) {	
		Class<?>[] acceptedClasses= new Class<?>[] { IFolder.class };
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
		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
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
			CUIPlugin.log(e);
			projects= new ICProject[0];
		}
		
		ILabelProvider labelProvider= new CElementLabelProvider(CElementLabelProvider.SHOW_DEFAULT);
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(NewFolderWizardMessages.NewSourceFolderWizardPage_ChooseProjectDialog_title); 
		dialog.setMessage(NewFolderWizardMessages.NewSourceFolderWizardPage_ChooseProjectDialog_description); 
		dialog.setElements(projects);
		dialog.setInitialSelections(new Object[] { fCurrCProject });
		if (dialog.open() == Window.OK) {			
			return (ICProject) dialog.getFirstResult();
		}			
		return null;		
	}
}
