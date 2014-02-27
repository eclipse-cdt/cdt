/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFileSet;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.core.runtime.CoreException;

public class PDOMFileSet implements IIndexFragmentFileSet {
	private final PDOM pdom;
	private final HashSet<Long> fFileIDs= new HashSet<Long>();
	private static final IIndexFragmentName[] EMPTY_NAMES = new IIndexFragmentName[0];

	public PDOMFileSet(PDOM pdom) {
		this.pdom = pdom;
	}

	@Override
	public void add(IIndexFragmentFile fragFile) {
		PDOMFile pdomFile= (PDOMFile) fragFile;
		fFileIDs.add(pdomFile.getRecord());
	}

	@Override
	public void remove(IIndexFragmentFile fragFile) {
		PDOMFile pdomFile= (PDOMFile) fragFile;
		fFileIDs.remove(pdomFile.getRecord());
	}

	@Override
	public boolean containsFileOfLocalBinding(IIndexFragmentBinding fb) throws CoreException {
		if (fb instanceof PDOMBinding) {
			PDOMBinding pdomBinding= (PDOMBinding) fb;
			return fFileIDs.contains(pdomBinding.getLocalToFileRec());
		} 
		return false;
	}

	@Override
	public boolean contains(IIndexFragmentFile file) throws CoreException {
		if (file instanceof PDOMFile) {
			return fFileIDs.contains(((PDOMFile) file).getRecord());
		}
		return false;
	}

	@Override
	public IIndexFragmentName[] findNames(IBinding binding, int flags) throws CoreException {
		if (pdom == null)
			return EMPTY_NAMES;

		PDOMLinkage pdomLinkage = pdom.getLinkage(binding.getLinkage().getLinkageID());

		char[] bindingName = binding.getNameCharArray();
		int bindingType = pdomLinkage.getBindingType(binding);

		ArrayList<IIndexFragmentName> names= new ArrayList<IIndexFragmentName>();
		for(Long fileRec : fFileIDs) {
			PDOMFile file = PDOMFile.recreateFile(pdom, fileRec);
			for(IIndexName name : file.findNames(0, Integer.MAX_VALUE)) {
				if (!(name instanceof PDOMName)
				 || ((name.isReference() && (flags & IIndexFragment.FIND_REFERENCES) == 0))
				 || ((name.isDeclaration() && (flags & IIndexFragment.FIND_DECLARATIONS) == 0))
				 || ((name.isDefinition() && (flags & IIndexFragment.FIND_DEFINITIONS) == 0)))
					continue;

				PDOMName pdomName = (PDOMName) name;
				PDOMBinding pdomBinding = pdomName.getBinding();
				if ((pdomBinding.isFileLocal() && (flags & IIndexFragment.FIND_NON_LOCAL_ONLY) != 0)
				 || pdomBinding.getNodeType() != bindingType
				 || pdomBinding.getDBName().compareCompatibleWithIgnoreCase(bindingName) != 0)
					continue;

				names.add(pdomName);
			}
		}

		return names.isEmpty() ? EMPTY_NAMES : names.toArray(new IIndexFragmentName[names.size()]);
	}
}
