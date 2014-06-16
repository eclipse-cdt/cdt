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
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

/**
 * Updates include statements and include guards in response to a file or a folder rename.
 */
public class HeaderFileRenameParticipant extends RenameParticipant {
	private IResource renamedResource;
	private Change change;

	public HeaderFileRenameParticipant() {
	}

	@Override
	protected boolean initialize(Object element) {
		if (element instanceof IResource) {
			this.renamedResource = (IResource) element;
			return true;
		}
		return false;
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {
		RenameArguments args = getArguments();
		if (!args.getUpdateReferences())
			return null;
		if (renamedResource.isLinked())
			return null;

		String newName = args.getNewName();

		try {
			// Maps the affected files to new, not yet existing, files.
			final Map<IFile, IFile> movedFiles = new HashMap<>();
			if (renamedResource instanceof IContainer) {
				final IPath oldPath = renamedResource.getFullPath();
				final IPath newPath = oldPath.removeLastSegments(1).append(newName);
				final IWorkspaceRoot workspaceRoot = renamedResource.getWorkspace().getRoot();
				((IContainer) renamedResource).accept(new IResourceProxyVisitor() {
					@Override
					public boolean visit(IResourceProxy proxy) throws CoreException {
						if (proxy.isLinked())
							return false;
						if (proxy.getType() == IResource.FILE) {
							IFile file = (IFile) proxy.requestResource();
							IPath path = replacePrefix(file.getFullPath(), oldPath.segmentCount(), newPath);
							movedFiles.put(file, workspaceRoot.getFile(path));
							return false;
						}
						return true;
					}
				}, IResource.NONE);
			} else if (renamedResource instanceof IFile) {
				IFile file = (IFile) renamedResource;
				movedFiles.put(file, file.getParent().getFile(new Path(newName)));
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
		return RenameMessages.HeaderFileRenameParticipant_name;
	}

	/**
	 * Replaces first few segments of the given path by the contents of another path.
	 *
	 * @param path the original path
	 * @param prefixLength the number of segments of {@code path} to replace
	 * @param newPrefix the replacement path
	 * @return the modified path
	 * @since 5.8
	 */
	private static IPath replacePrefix(IPath path, int prefixLength, IPath newPrefix) {
		return newPrefix.append(path.removeFirstSegments(prefixLength));
	}
}
