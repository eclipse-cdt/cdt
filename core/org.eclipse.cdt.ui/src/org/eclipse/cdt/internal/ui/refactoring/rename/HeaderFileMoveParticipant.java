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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;

/**
 * Updates include statements and include guards in response to a file or a folder move.
 */
public class HeaderFileMoveParticipant extends MoveParticipant {
	private IResource movedResource;
	private Change change;

	public HeaderFileMoveParticipant() {
	}

	@Override
	protected boolean initialize(Object element) {
		if (element instanceof IResource) {
			this.movedResource = (IResource) element;
			return true;
		}
		return false;
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {
		MoveArguments args = getArguments();
		if (!args.getUpdateReferences())
			return null;
		if (movedResource.isLinked())
			return null;

		Object destinationResource = args.getDestination();
		if (!(destinationResource instanceof IContainer))
			return null;
		final IContainer destination = (IContainer) destinationResource;
		final IPath destinationLocation = destination.getLocation();
		if (destinationLocation.equals(movedResource.getLocation().removeLastSegments(1)))
			return null;

		try {
			// Maps the affected files to new, not yet existing, files.
			final Map<IFile, IFile> movedFiles = new HashMap<>();
			if (movedResource instanceof IContainer) {
				final int prefixLength = movedResource.getFullPath().segmentCount() - 1;
				((IContainer) movedResource).accept(new IResourceProxyVisitor() {
					@Override
					public boolean visit(IResourceProxy proxy) throws CoreException {
						if (proxy.isLinked())
							return false;
						if (proxy.getType() == IResource.FILE) {
							IFile file = (IFile) proxy.requestResource();
							movedFiles.put(file, destination.getFile(file.getFullPath().removeFirstSegments(prefixLength)));
							return false;
						}
						return true;
					}
				}, IResource.NONE);
			} else if (movedResource instanceof IFile) {
				IFile file = (IFile) movedResource;
				movedFiles.put(file, destination.getFile(new Path(movedResource.getName())));
			}
	
			HeaderFileReferenceAdjuster includeAdjuster = new HeaderFileReferenceAdjuster(movedFiles);
			change = includeAdjuster.createChange(context, pm);
		} catch (CoreException e) {
			return RefactoringStatus.create(e.getStatus());
		}
		return RefactoringStatus.create(Status.OK_STATUS);
	}

	@Override
	public Change createPreChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		pm.done();
		return change;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		pm.done();
		return null;
	}

	@Override
	public String getName() {
		return RenameMessages.HeaderFileMoveParticipant_name;
	}
}
