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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

/**
 * @author Emanuel Graf IFS
 */
public class RenameSourceFolder extends RenameParticipant {

	private IFolder oldFolder;
	private String newName;

	public RenameSourceFolder() {
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {
		RenameArguments arg = getArguments();
		newName = arg.getNewName();		
		return RefactoringStatus.create(Status.OK_STATUS);
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		IPath oldFolderPath = oldFolder.getFullPath();
		IPath newFolderPath = oldFolder.getFullPath().uptoSegment(oldFolder.getFullPath().segmentCount() - 1).append(newName);
		return new RenameCSourceFolderChange(oldFolderPath, newFolderPath, oldFolder.getProject(), oldFolder);
	}

	@Override
	public String getName() {
		return Messages.RenameSourceFolder_0;
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
