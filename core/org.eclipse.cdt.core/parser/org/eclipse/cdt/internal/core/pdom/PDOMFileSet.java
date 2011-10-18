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

import java.util.HashSet;

import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFileSet;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.core.runtime.CoreException;

public class PDOMFileSet implements IIndexFragmentFileSet {
	private HashSet<Long> fFileIDs= new HashSet<Long>();
	
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
		PDOMBinding pdomBinding= (PDOMBinding) fb;
		return fFileIDs.contains(pdomBinding.getLocalToFileRec());
	}

	@Override
	public boolean contains(IIndexFragmentFile file) throws CoreException {
		if (file instanceof PDOMFile) {
			return fFileIDs.contains(((PDOMFile) file).getRecord());
		}
		return false;
	}
}
