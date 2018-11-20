/*******************************************************************************
 * Copyright (c) 2005, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.ui.refactoring.changes.CCompositeChange;
import org.eclipse.cdt.internal.ui.refactoring.changes.RenameTranslationUnitChange;
import org.eclipse.cdt.internal.ui.util.NameComposer;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;

/**
 * Processor adding constructor and destructor to the bindings to be renamed.
 */
public class CRenameClassProcessor extends CRenameTypeProcessor {
	private final List<Change> tuRenames = new ArrayList<>();

	public CRenameClassProcessor(CRenameProcessor processor, String kind) {
		super(processor, kind);
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		Change change = super.createChange(pm);
		if (tuRenames.isEmpty())
			return change;

		CompositeChange compositeChange;
		if (change instanceof CompositeChange) {
			compositeChange = (CompositeChange) change;
		} else {
			compositeChange = new CCompositeChange(""); //$NON-NLS-1$
			compositeChange.markAsSynthetic();
			compositeChange.add(change);
		}
		for (Change tuRename : tuRenames) {
			compositeChange.add(tuRename);
		}
		return compositeChange;
	}

	@Override
	protected IBinding[] getBindingsToBeRenamed(RefactoringStatus status) {
		tuRenames.clear();
		CRefactoringArgument argument = getArgument();
		IBinding binding = argument.getBinding();
		ArrayList<IBinding> bindings = new ArrayList<>();
		if (binding != null) {
			recordRename(binding);
			bindings.add(binding);
		}
		if (binding instanceof ICPPClassType) {
			ICPPClassType ctype = (ICPPClassType) binding;
			ICPPConstructor[] ctors = ctype.getConstructors();
			if (ctors != null) {
				ArrayUtil.addAll(bindings, ctors);
			}

			IScope scope = ctype.getCompositeScope();
			if (scope != null) {
				IBinding[] dtors = scope.find("~" + argument.getName(), argument.getTranslationUnit()); //$NON-NLS-1$
				if (dtors != null) {
					ArrayUtil.addAll(bindings, dtors);
				}
			}

			renameTranslationUnits(ctype);
		}
		return bindings.toArray(new IBinding[bindings.size()]);
	}

	private void renameTranslationUnits(ICPPBinding binding) {
		IIndex index = getIndex();
		if (index == null) {
			return;
		}
		try {
			index.acquireReadLock();
			Set<IIndexFileLocation> locations = new HashSet<>();
			IIndexName[] names = index.findNames(binding, IIndex.FIND_DEFINITIONS);
			for (IIndexName name : names) {
				locations.add(name.getFile().getLocation());
			}
			if (locations.size() != 1)
				return;
			IIndexFileLocation location = locations.iterator().next();
			String fullPath = location.getFullPath();
			if (fullPath == null)
				return;
			IPath headerPath = new Path(fullPath);
			IResource file = ResourcesPlugin.getWorkspace().getRoot().findMember(headerPath);
			if (file == null || file.getType() != IResource.FILE)
				return;

			IProject project = getProject();
			int headerCapitalization = PreferenceConstants.getPreference(
					PreferenceConstants.NAME_STYLE_CPP_HEADER_CAPITALIZATION, project,
					PreferenceConstants.NAME_STYLE_CAPITALIZATION_ORIGINAL);
			String headerWordDelimiter = PreferenceConstants
					.getPreference(PreferenceConstants.NAME_STYLE_CPP_HEADER_WORD_DELIMITER, project, ""); //$NON-NLS-1$
			int sourceCapitalization = PreferenceConstants.getPreference(
					PreferenceConstants.NAME_STYLE_CPP_SOURCE_CAPITALIZATION, project,
					PreferenceConstants.NAME_STYLE_CAPITALIZATION_ORIGINAL);
			String sourceWordDelimiter = PreferenceConstants
					.getPreference(PreferenceConstants.NAME_STYLE_CPP_SOURCE_WORD_DELIMITER, project, ""); //$NON-NLS-1$

			String headerName = headerPath.lastSegment();
			String className = binding.getName();
			NameComposer nameComposer = NameComposer.createByExample(className, headerName, headerCapitalization,
					headerWordDelimiter);
			if (nameComposer == null)
				return;

			String newClassName = getReplacementText();
			String newHeaderName = nameComposer.compose(newClassName);
			if (!newHeaderName.equals(headerName)) {
				renameTranslationUnit((IFile) file, newHeaderName);
			}

			IIndexInclude[] includedBy = index.findIncludedBy(names[0].getFile());
			for (IIndexInclude include : includedBy) {
				location = include.getIncludedByLocation();
				fullPath = location.getFullPath();
				if (fullPath == null)
					continue;
				IPath filePath = new Path(fullPath);
				file = ResourcesPlugin.getWorkspace().getRoot().findMember(filePath);
				if (file != null && file.getType() == IResource.FILE) {
					String fileName = filePath.lastSegment();
					if (CoreModel.isValidHeaderUnitName(project, fileName)) {
						nameComposer = NameComposer.createByExample(className, fileName, headerCapitalization,
								headerWordDelimiter);
					} else {
						nameComposer = NameComposer.createByExample(className, fileName, sourceCapitalization,
								sourceWordDelimiter);
					}
					if (nameComposer != null) {
						String newName = nameComposer.compose(newClassName);
						if (!newName.equals(fileName)) {
							renameTranslationUnit((IFile) file, newName);
						}
					}
				}
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
			return;
		} catch (InterruptedException e) {
			return; // Ignore.
		} finally {
			index.releaseReadLock();
		}
	}

	protected void renameTranslationUnit(IFile file, String newName) {
		ICElement elem = CoreModel.getDefault().create(file);
		if (elem instanceof ITranslationUnit) {
			tuRenames.add(new RenameTranslationUnitChange((ITranslationUnit) elem, newName));
			getRenameModifications().rename(file, new RenameArguments(newName, true));
		}
	}

	protected IProject getProject() {
		IFile file = getArgument().getSourceFile();
		if (file == null)
			return null;
		return file.getProject();
	}
}
