/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser;

import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

/**
 * A {@link ITypeReference} tailored for ast bindings.
 * @since 5.0
 */
public class ASTTypeReference extends IndexTypeReference {

	private final IIndexFileLocation fIfl;

	public ASTTypeReference(IIndexFileLocation ifl, IBinding binding, IFile file, int offset, int length) {
		super(binding, file, file.getProject(), offset, length);
		fIfl = ifl;
	}

	public ASTTypeReference(IIndexFileLocation ifl, IBinding binding, IPath location, int offset, int length) {
		super(binding, location, null, offset, length);
		fIfl = ifl;
	}

	public IIndexFileLocation getIFL() {
		return fIfl;
	}
}
