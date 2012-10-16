/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndexFile;

class InclusionRequest {
	private final IBinding fBinding;
	private final Map<IIndexFile, IPath> fDeclaringFiles;
	private final boolean fReachable;
	private List<IPath> fCandidatePaths;
	private IPath fResolvedPath;

	/**
	 * @param binding the binding that requires inclusion
	 * @param declaringHeaders headers that can be included to declare the binding and paths
	 *     that can be used to include them 
	 * @param reachable indicates whether the headers were previously included or not
	 */
	public InclusionRequest(IBinding binding, Map<IIndexFile, IPath> declaringHeaders,
			boolean reachable) {
		fBinding = binding;
		fDeclaringFiles = Collections.unmodifiableMap(declaringHeaders);
		fReachable = reachable;
		fCandidatePaths = new ArrayList<IPath>(new HashSet<IPath>(fDeclaringFiles.values()));
	}

	public IBinding getBinding() {
		return fBinding;
	}
	
	public Map<IIndexFile, IPath> getDeclaringFiles() {
		return fDeclaringFiles;
	}

	public List<IPath> getCandidatePaths() {
		return fCandidatePaths;
	}

	public void setCandidatePaths(List<IPath> paths) {
		fCandidatePaths = paths;
	}

	public boolean isReachable() {
		return fReachable;
	}

	public void resolve(IPath path) {
		if (fResolvedPath != null)
			throw new IllegalStateException();
		fResolvedPath = path;
	}

	public IPath getResolvedPath() {
		return fResolvedPath;
	}

	public boolean isResolved() {
		return fResolvedPath != null;
	}
}