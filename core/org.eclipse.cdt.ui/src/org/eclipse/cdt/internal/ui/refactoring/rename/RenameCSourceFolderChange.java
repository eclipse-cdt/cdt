/*******************************************************************************
 * Copyright (c) 2009, 2015 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software (IFS)- initial API and implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.ui.CUIPlugin;
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

/**
 * @author Emanuel Graf IFS
 */
public class RenameCSourceFolderChange extends Change {
	private final IPath oldFolderPath;
	private final IPath newFolderPath;
	private final IProject project;
	private final IFolder oldFolder;

	public RenameCSourceFolderChange(IFolder oldFolder, IPath newFolderPath) {
		super();
		this.oldFolder = oldFolder;
		this.newFolderPath = newFolderPath;
		this.oldFolderPath = oldFolder.getFullPath();
		this.project = oldFolder.getProject();
	}

	@Override
	public Object getModifiedElement() {
		return oldFolder;
	}

	@Override
	public String getName() {
		return NLS.bind(RenameMessages.RenameCSourceFolderChange_Name0, oldFolderPath.lastSegment(),
				newFolderPath.lastSegment());
	}

	@Override
	public void initializeValidationData(IProgressMonitor pm) {
	}

	@Override
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (oldFolder.exists()) {
			return RefactoringStatus.create(Status.OK_STATUS);
		} else {
			return RefactoringStatus.create(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID,
					NLS.bind(RenameMessages.RenameCSourceFolderChange_ErrorMsg, oldFolder.getName())));
		}
	}

	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		changeEntryInAllCfgs(CCorePlugin.getDefault().getProjectDescription(project, true));
		IFolder newFolder = project.getFolder(newFolderPath.removeFirstSegments(1));
		return new RenameCSourceFolderChange(newFolder, oldFolderPath);
	}

	private void changeEntryInAllCfgs(ICProjectDescription des) throws WriteAccessException, CoreException {
		ICConfigurationDescription cfgs[] = des.getConfigurations();
		for (ICConfigurationDescription cfg : cfgs) {
			ICSourceEntry[] entries = cfg.getSourceEntries();
			entries = renameSourceEntries(entries);
			cfg.setSourceEntries(entries);
		}
		CCorePlugin.getDefault().setProjectDescription(project, des, false, new NullProgressMonitor());
	}

	private ICSourceEntry[] renameSourceEntries(ICSourceEntry[] sourceEntries) {
		Set<ICSourceEntry> set = new HashSet<>();
		for (ICSourceEntry entry : sourceEntries) {
			set.add(renameSourceEntry(entry, oldFolderPath, newFolderPath));
		}
		return set.toArray(new ICSourceEntry[set.size()]);
	}

	static ICSourceEntry renameSourceEntry(ICSourceEntry sourceEntry, IPath oldFolderPath, IPath newFolderPath) {
		String entryPath = sourceEntry.getName();
		if (entryPath.equals(oldFolderPath.toString())) {
			return new CSourceEntry(newFolderPath, sourceEntry.getExclusionPatterns(), sourceEntry.getFlags());
		} else {
			IPath oldSegments = oldFolderPath.removeFirstSegments(oldFolderPath.segmentCount() - 1);
			Set<IPath> exclusionPatterns = new HashSet<>();
			for (IPath pattern : sourceEntry.getExclusionPatterns()) {
				if (pattern.equals(oldSegments)) {
					exclusionPatterns.add(newFolderPath.removeFirstSegments(newFolderPath.segmentCount() - 1));
				} else {
					exclusionPatterns.add(pattern);
				}
			}

			return new CSourceEntry(sourceEntry.getValue(),
					exclusionPatterns.toArray(new IPath[exclusionPatterns.size()]), sourceEntry.getFlags());
		}
	}
}
