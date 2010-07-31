/*******************************************************************************
 * Copyright (c) 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * @author Emanuel Graf IFS
 */
public class RenameCSourceFolderChange extends Change {
	private IPath oldName;
	private IPath newName;
	private IProject project;
	private IFolder folder;

	public RenameCSourceFolderChange(IPath oldFolderPath, IPath newFolderPath, IProject project, IFolder oldFolder) {
		super();
		this.oldName = oldFolderPath;
		this.newName = newFolderPath;
		this.project = project;
		folder = oldFolder;
	}

	@Override
	public Object getModifiedElement() {
		return folder;
	}

	@Override
	public String getName() {
		return NLS.bind(RenameMessages.RenameCSourceFolderChange_Name0, oldName.lastSegment(), newName.lastSegment());
	}

	@Override
	public void initializeValidationData(IProgressMonitor pm) {
	}

	@Override
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (folder.exists()) {
			return RefactoringStatus.create(Status.OK_STATUS); 
		} else {
			return RefactoringStatus.create(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID,
					NLS.bind(RenameMessages.RenameCSourceFolderChange_ErrorMsg, folder.getName())));
		}
	}

	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		changeEntryInAllCfgs(CCorePlugin.getDefault().getProjectDescription(project, true));
		IFolder folder2 = project.getFolder(newName.lastSegment());
		return new RenameCSourceFolderChange(newName, oldName, project, folder2);
	}
	
	private void changeEntryInAllCfgs(ICProjectDescription des) throws WriteAccessException, CoreException{
		ICConfigurationDescription cfgs[] = des.getConfigurations();
		for (ICConfigurationDescription cfg : cfgs){
			ICSourceEntry[] entries = cfg.getSourceEntries();
			entries = renameEntry(entries);
			cfg.setSourceEntries(entries);
		}
		CCorePlugin.getDefault().setProjectDescription(project, des, false, new NullProgressMonitor());
	}
	
	private ICSourceEntry[] renameEntry(ICSourceEntry[] entries){
		Set<ICSourceEntry> set = new HashSet<ICSourceEntry>();
		for (ICSourceEntry se : entries){
			String seLocation = se.getName();
			if(seLocation.equals(oldName.toPortableString())) {
				ICSourceEntry newSE = new CSourceEntry(newName, se.getExclusionPatterns(), se.getFlags());
				set.add(newSE);
			} else {
				Set<IPath> exPatters = new HashSet<IPath>();
				for (IPath filter : se.getExclusionPatterns()) {
					IPath oldSegments = oldName.removeFirstSegments(oldName.segmentCount() -1);
					if (filter.equals(oldSegments)) {
						exPatters.add(newName.removeFirstSegments(newName.segmentCount() -1));
					} else {
						exPatters.add(filter);
					}
				}
				
				set.add(new CSourceEntry(se.getValue(), exPatters.toArray(new IPath[exPatters.size()]), se.getFlags()));
			}
		}
		return set.toArray(new ICSourceEntry[set.size()]);
	}
}
