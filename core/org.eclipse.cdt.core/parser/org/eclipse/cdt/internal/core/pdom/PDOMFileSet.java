/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.util.HashSet;

import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFileSet;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.core.runtime.CoreException;

public final class PDOMFileSet implements IIndexFragmentFileSet {
	private final HashSet<Long> fFileIDs = new HashSet<>();

	@Override
	public void add(IIndexFragmentFile fragFile) {
		PDOMFile pdomFile = (PDOMFile) fragFile;
		fFileIDs.add(pdomFile.getRecord());
	}

	@Override
	public void remove(IIndexFragmentFile fragFile) {
		PDOMFile pdomFile = (PDOMFile) fragFile;
		fFileIDs.remove(pdomFile.getRecord());
	}

	@Override
	public boolean containsFileOfLocalBinding(IIndexFragmentBinding fb) throws CoreException {
		if (fb instanceof PDOMBinding) {
			PDOMBinding pdomBinding = (PDOMBinding) fb;
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

	/**
	 * Returns whether the file set contains the file corresponding to the given record.
	 */
	public boolean containsFile(long fileRecord) {
		return fFileIDs.contains(fileRecord);
	}

	@Override
	public boolean isEmpty() {
		return fFileIDs.isEmpty();
	}
}
