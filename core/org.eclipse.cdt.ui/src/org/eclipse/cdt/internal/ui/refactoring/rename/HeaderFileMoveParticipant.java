/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;

public class HeaderFileMoveParticipant extends MoveParticipant {
	private IResource element;

	public HeaderFileMoveParticipant() {
	}

	@Override
	protected boolean initialize(Object element) {
		if (element instanceof IResource) {
			this.element = (IResource) element;
			return true;
		}
		return false;
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {
		return RefactoringStatus.create(Status.OK_STATUS);
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		MoveArguments args = getArguments();
		if (!args.getUpdateReferences())
			return null;
		Object destination = args.getDestination();
		if (!(destination instanceof IContainer))
			return null;
		IPath destinationLocation = ((IContainer) destination).getLocation();
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return RenameMessages.HeaderFileMoveParticipant_name;
	}
}
