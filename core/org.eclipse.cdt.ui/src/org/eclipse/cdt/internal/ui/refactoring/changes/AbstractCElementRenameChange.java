/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.changes;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.resource.ResourceChange;

public abstract class AbstractCElementRenameChange extends ResourceChange {
	private final String fNewName;
	private final String fOldName;
	private final IPath fResourcePath;
	private final long fStampToRestore;

	protected AbstractCElementRenameChange(IPath resourcePath, String oldName, String newName) {
		this(resourcePath, oldName, newName, IResource.NULL_STAMP);
	}

	protected AbstractCElementRenameChange(IPath resourcePath, String oldName, String newName, long stampToRestore) {
		Assert.isNotNull(newName, "new name"); //$NON-NLS-1$
		Assert.isNotNull(oldName, "old name"); //$NON-NLS-1$
		fResourcePath = resourcePath;
		fOldName = oldName;
		fNewName = newName;
		fStampToRestore = stampToRestore;
	}

	protected abstract IPath createNewPath();

	protected abstract Change createUndoChange(long stampToRestore) throws CoreException;

	protected abstract void doRename(IProgressMonitor pm) throws CoreException;

	@Override
	public Object getModifiedElement() {
		return CoreModel.getDefault().create(getResource());
	}

	@Override
	protected IResource getModifiedResource() {
		return getResource();
	}

	public String getNewName() {
		return fNewName;
	}

	public String getOldName() {
		return fOldName;
	}

	protected final IResource getResource() {
		return ResourcesPlugin.getWorkspace().getRoot().findMember(fResourcePath);
	}

	protected IPath getResourcePath() {
		return fResourcePath;
	}

	@Override
	public final Change perform(IProgressMonitor pm) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(pm, Messages.AbstractCElementRenameChange_renaming, 1);
		IResource resource = getResource();
		IPath newPath = createNewPath();
		Change result = createUndoChange(resource.getModificationStamp());
		doRename(subMonitor.split(1));
		if (fStampToRestore != IResource.NULL_STAMP) {
			IResource newResource = ResourcesPlugin.getWorkspace().getRoot().findMember(newPath);
			newResource.revertModificationStamp(fStampToRestore);
		}
		return result;
	}
}