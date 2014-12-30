/*******************************************************************************
 * Copyright (c) 2005, 2014 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
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
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.editor.SourceHeaderPartnerFinder;
import org.eclipse.cdt.internal.ui.refactoring.changes.CCompositeChange;
import org.eclipse.cdt.internal.ui.refactoring.changes.RenameTranslationUnitChange;
import org.eclipse.cdt.internal.ui.wizards.filewizard.NewSourceFileGenerator;

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
        CRefactoringArgument argument= getArgument();
        IBinding binding= argument.getBinding();
        ArrayList<IBinding> bindings= new ArrayList<>();
        if (binding != null) {
        	recordRename(binding);
            bindings.add(binding);
        }
        if (binding instanceof ICPPClassType) {
            ICPPClassType ctype= (ICPPClassType) binding;
            ICPPConstructor[] ctors= ctype.getConstructors();
			if (ctors != null) {
			    ArrayUtil.addAll(bindings, ctors);
			}
			
			IScope scope= ctype.getCompositeScope();
			if (scope != null) {
			    IBinding[] dtors= scope.find("~" + argument.getName()); //$NON-NLS-1$
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
			String className = binding.getName();
            String headerName = NewSourceFileGenerator.generateHeaderFileNameFromClass(className);
            if (!headerPath.lastSegment().equals(headerName))
            	return;
            IResource file = ResourcesPlugin.getWorkspace().getRoot().findMember(headerPath);
            if (file == null || file.getType() != IResource.FILE)
            	return;
            String newClassName = getReplacementText();
			String newHeaderName = NewSourceFileGenerator.generateHeaderFileNameFromClass(newClassName);
            if (!newHeaderName.equals(headerName)) {
            	renameTranslationUnit((IFile) file, newHeaderName);
            }
            String sourceName = NewSourceFileGenerator.generateSourceFileNameFromClass(className);
            String testName = NewSourceFileGenerator.generateTestFileNameFromClass(className);
            String[] partnerFileSuffixes = getPartnerFileSuffixes();
            IIndexInclude[] includedBy = index.findIncludedBy(names[0].getFile());
            for (IIndexInclude include : includedBy) {
				location = include.getIncludedByLocation();
				fullPath = location.getFullPath();
				if (fullPath == null)
					continue;
				IPath sourcePath = new Path(fullPath);
                file = ResourcesPlugin.getWorkspace().getRoot().findMember(sourcePath);
                if (file != null && file.getType() == IResource.FILE) {
		            if (sourcePath.lastSegment().equals(sourceName)) {
		    			String newName = NewSourceFileGenerator.generateSourceFileNameFromClass(newClassName);
		                if (!newName.equals(sourceName)) {
		                	renameTranslationUnit((IFile) file, newName);
		                }
		            } else if (sourcePath.lastSegment().equals(testName)) {
		    			String newName = NewSourceFileGenerator.generateTestFileNameFromClass(newClassName);
		                file = ResourcesPlugin.getWorkspace().getRoot().findMember(sourcePath);
		                if (!newName.equals(testName)) {
		                	renameTranslationUnit((IFile) file, newName);
		                }
		            } else if (SourceHeaderPartnerFinder.isPartnerFile(sourcePath, headerPath, partnerFileSuffixes)) {
		            	String name = sourcePath.lastSegment();
		            	String baseName = headerPath.removeFileExtension().lastSegment();
		            	if (name.startsWith(baseName)) {
		            		String newBaseName = new Path(headerName).removeFileExtension().lastSegment();
							String newName = newBaseName + name.substring(baseName.length());
			                if (!newName.equals(name)) {
			                	renameTranslationUnit((IFile) file, newName);
			                }
		            	}
		            }
                }
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
			return;
		} catch (InterruptedException e) {
			return;  // Ignore.
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

	private String[] getPartnerFileSuffixes() {
        IFile file= getArgument().getSourceFile();
        IProject project = file == null ? null : file.getProject();
		String value = PreferenceConstants.getPreference(
				PreferenceConstants.INCLUDES_PARTNER_FILE_SUFFIXES, project, ""); //$NON-NLS-1$
		return value.split(","); //$NON-NLS-1$
	}
}
