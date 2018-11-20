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
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

/**
 * Updates source folders and associated filters of a C/C++ project in response to a folder rename.
 *
 * @author Emanuel Graf IFS
 */
public class SourceFolderRenameParticipant extends RenameParticipant {
	private IFolder oldFolder;

	public SourceFolderRenameParticipant() {
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {
		return RefactoringStatus.create(Status.OK_STATUS);
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		String newName = getArguments().getNewName();
		IPath newFolderPath = oldFolder.getFullPath().removeLastSegments(1).append(newName);
		return new RenameCSourceFolderChange(oldFolder, newFolderPath);
	}

	@Override
	public String getName() {
		return RenameMessages.SourceFolderRenameParticipant_name;
	}

	@Override
	protected boolean initialize(Object element) {
		if (element instanceof IFolder) {
			oldFolder = (IFolder) element;
			return true;
		}
		return false;
	}
}
