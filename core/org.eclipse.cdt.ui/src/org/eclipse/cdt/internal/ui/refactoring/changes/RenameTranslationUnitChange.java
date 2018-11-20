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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.osgi.util.NLS;

public final class RenameTranslationUnitChange extends AbstractCElementRenameChange {

	public RenameTranslationUnitChange(ITranslationUnit tu, String newName) {
		this(tu.getResource().getFullPath(), tu.getElementName(), newName, IResource.NULL_STAMP);
	}

	private RenameTranslationUnitChange(IPath resourcePath, String oldName, String newName, long stampToRestore) {
		super(resourcePath, oldName, newName, stampToRestore);

		setValidationMethod(VALIDATE_NOT_READ_ONLY | SAVE_IF_DIRTY);
	}

	@Override
	protected IPath createNewPath() {
		IPath path = getResourcePath();
		return path.removeLastSegments(1).append(getNewName());
	}

	@Override
	protected Change createUndoChange(long stampToRestore) throws CModelException {
		return new RenameTranslationUnitChange(createNewPath(), getNewName(), getOldName(), stampToRestore);
	}

	@Override
	protected void doRename(IProgressMonitor pm) throws CoreException {
		ITranslationUnit tu = (ITranslationUnit) getModifiedElement();
		if (tu != null)
			tu.rename(getNewName(), false, pm);
	}

	@Override
	public String getName() {
		return NLS.bind(Messages.RenameTranslationUnitChange_name, BasicElementLabels.getCElementName(getOldName()),
				BasicElementLabels.getCElementName(getNewName()));
	}
}
