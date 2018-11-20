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
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.corext.refactoring.participants.ResourceModifications;
import org.eclipse.cdt.internal.ui.refactoring.reorg.RefactoringModifications;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.IParticipantDescriptorFilter;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;

/**
 * Stores objects renamed by the rename refactoring.
 */
public class RenameModifications extends RefactoringModifications {
	private List<Object> fRename;
	private List<RefactoringArguments> fRenameArguments;
	private List<IParticipantDescriptorFilter> fParticipantDescriptorFilter;

	public RenameModifications() {
		fRename = new ArrayList<>();
		fRenameArguments = new ArrayList<>();
		fParticipantDescriptorFilter = new ArrayList<>();
	}

	public void rename(IBinding binding, RenameArguments args) {
		add(binding, args, null);
	}

	public void rename(IResource resource, RenameArguments args) {
		add(resource, args, null);
	}

	public void rename(ISourceRoot sourceFolder, RenameArguments arguments) {
		add(sourceFolder, arguments, null);
		if (sourceFolder.getResource() != null) {
			getResourceModifications().addRename(sourceFolder.getResource(), arguments);
		}
	}

	public void rename(ITranslationUnit unit, RenameArguments args) {
		add(unit, args, null);
		if (unit.getResource() != null) {
			getResourceModifications().addRename(unit.getResource(),
					new RenameArguments(args.getNewName(), args.getUpdateReferences()));
		}
	}

	@Override
	public void buildDelta(IResourceChangeDescriptionFactory builder) {
		for (int i = 0; i < fRename.size(); i++) {
			Object element = fRename.get(i);
			if (element instanceof IResource) {
				ResourceModifications.buildMoveDelta(builder, (IResource) element,
						(RenameArguments) fRenameArguments.get(i));
			}
		}
		getResourceModifications().buildDelta(builder);
	}

	@Override
	public void buildValidateEdits(ValidateEditChecker checker) {
		for (Iterator<Object> iter = fRename.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (element instanceof ITranslationUnit) {
				ITranslationUnit unit = (ITranslationUnit) element;
				IResource resource = unit.getResource();
				if (resource != null && resource.getType() == IResource.FILE) {
					checker.addFile((IFile) resource);
				}
			}
		}
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status, RefactoringProcessor owner,
			String[] natures, SharableParticipants shared) {
		List<RefactoringParticipant> result = new ArrayList<>();
		for (int i = 0; i < fRename.size(); i++) {
			ArrayUtil.addAll(result, ParticipantManager.loadRenameParticipants(status, owner, fRename.get(i),
					(RenameArguments) fRenameArguments.get(i), fParticipantDescriptorFilter.get(i), natures, shared));
		}
		ArrayUtil.addAll(result, getResourceModifications().getParticipants(status, owner, natures, shared));
		return result.toArray(new RefactoringParticipant[result.size()]);
	}

	private void add(Object element, RefactoringArguments args, IParticipantDescriptorFilter filter) {
		Assert.isNotNull(element);
		Assert.isNotNull(args);
		fRename.add(element);
		fRenameArguments.add(args);
		fParticipantDescriptorFilter.add(filter);
	}
}
