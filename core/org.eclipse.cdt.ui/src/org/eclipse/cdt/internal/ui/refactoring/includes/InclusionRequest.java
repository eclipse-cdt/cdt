/*******************************************************************************
 * Copyright (c) 2012, 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.parser.util.StringUtil;
import org.eclipse.core.runtime.IPath;

class InclusionRequest {
	private static final String UNINITIALIZED = "uninitialized"; //$NON-NLS-1$

	private final IBinding fBinding;
	private final Map<IIndexFile, IPath> fDeclaringFiles;
	private final boolean fReachable;
	private List<IPath> fCandidatePaths;
	private IPath fResolvedPath;
	private String fQualifiedName = UNINITIALIZED;

	/**
	 * @param binding the binding that requires inclusion
	 * @param declaringHeaders headers that can be included to declare the binding and paths
	 *     that can be used to include them
	 * @param reachable indicates whether the headers were previously included or not
	 */
	public InclusionRequest(IBinding binding, Map<IIndexFile, IPath> declaringHeaders, boolean reachable) {
		fBinding = binding;
		fDeclaringFiles = Collections.unmodifiableMap(declaringHeaders);
		fReachable = reachable;
		fCandidatePaths = new ArrayList<>(new HashSet<>(fDeclaringFiles.values()));
	}

	public IBinding getBinding() {
		return fBinding;
	}

	/**
	 * Returns the qualified name of the binding, or {@code null} if the binding doesn't have
	 * a qualified name.
	 */
	public String getBindingQualifiedName() {
		if (fQualifiedName == UNINITIALIZED) {
			fQualifiedName = null;
			if (fBinding instanceof ICPPBinding) {
				ICPPBinding cppBinding = (ICPPBinding) fBinding;
				try {
					if (cppBinding.isGloballyQualified()) {
						fQualifiedName = StringUtil.join(cppBinding.getQualifiedName(), "::"); //$NON-NLS-1$
					}
				} catch (DOMException e) {
					// Leave null;
				}
			} else if (fBinding instanceof IMacroBinding || fBinding.getOwner() == null) {
				fQualifiedName = fBinding.getName();
			}
		}
		return fQualifiedName;
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

	/** For debugging only */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(fBinding.getName());
		buf.append(" defined in "); //$NON-NLS-1$
		for (int i = 0; i < fCandidatePaths.size(); i++) {
			if (i != 0)
				buf.append(", "); //$NON-NLS-1$
			buf.append(fCandidatePaths.get(i).toOSString());
		}
		if (fResolvedPath != null) {
			buf.append(" represented by "); //$NON-NLS-1$
			buf.append(fResolvedPath.toOSString());
		}
		return buf.toString();
	}
}