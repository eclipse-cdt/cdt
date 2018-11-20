/*******************************************************************************
 * Copyright (c) 2014, 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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
import org.eclipse.ltk.core.refactoring.participants.ISharableParticipant;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

/**
 * Updates include statements and include guards in response to a file or a folder move.
 */
public class HeaderFileMoveParticipant extends MoveParticipant implements ISharableParticipant {
	private Map<IResource, MoveArguments> movedResources;
	private Change change;

	public HeaderFileMoveParticipant() {
	}

	@Override
	protected boolean initialize(Object element) {
		addElement(element, getArguments());
		return movedResources != null;
	}

	@Override
	public void addElement(Object element, RefactoringArguments arguments) {
		if (element instanceof IResource && arguments instanceof MoveArguments) {
			if (movedResources == null)
				movedResources = new HashMap<>();
			movedResources.put((IResource) element, (MoveArguments) arguments);
		}
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {
		try {
			if (movedResources == null)
				return RefactoringStatus.create(Status.OK_STATUS);

			// Maps the affected files to new, not yet existing, files.
			final Map<IFile, IFile> movedFiles = new HashMap<>();

			for (Map.Entry<IResource, MoveArguments> entry : movedResources.entrySet()) {
				IResource movedResource = entry.getKey();
				MoveArguments args = entry.getValue();
				if (!args.getUpdateReferences())
					continue;
				if (movedResource.isLinked())
					continue;

				Object destinationResource = args.getDestination();
				if (!(destinationResource instanceof IContainer))
					continue;
				final IContainer destination = (IContainer) destinationResource;
				final IPath destinationLocation = destination.getLocation();
				if (destinationLocation.equals(movedResource.getLocation().removeLastSegments(1)))
					continue;

				if (movedResource instanceof IFolder) {
					IFolder folder = (IFolder) movedResource;
					final int prefixLength = folder.getFullPath().segmentCount() - 1;
					folder.accept(new IResourceProxyVisitor() {
						@Override
						public boolean visit(IResourceProxy proxy) throws CoreException {
							if (proxy.isLinked())
								return false;
							if (proxy.getType() == IResource.FILE) {
								IFile file = (IFile) proxy.requestResource();
								movedFiles.put(file,
										destination.getFile(file.getFullPath().removeFirstSegments(prefixLength)));
								return false;
							}
							return true;
						}
					}, IResource.NONE);
				} else if (movedResource instanceof IFile) {
					IFile file = (IFile) movedResource;
					movedFiles.put(file, destination.getFile(new Path(movedResource.getName())));
				}
			}

			HeaderFileReferenceAdjuster includeAdjuster = new HeaderFileReferenceAdjuster(movedFiles,
					Collections.<IContainer, IContainer>emptyMap(), getProcessor());
			change = includeAdjuster.createChange(context, pm);
		} catch (CoreException e) {
			return RefactoringStatus.create(e.getStatus());
		} finally {
			pm.done();
		}
		return RefactoringStatus.create(Status.OK_STATUS);
	}

	@Override
	public Change createPreChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		change = RenameParticipantHelper.postprocessParticipantChange(change, this);
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
