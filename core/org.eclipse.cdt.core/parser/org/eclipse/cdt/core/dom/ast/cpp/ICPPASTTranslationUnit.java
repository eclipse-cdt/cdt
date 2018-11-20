/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTTranslationUnit extends IASTTranslationUnit {
	/**
	 * Returns the global namespace of the translation unit.
	 *
	 * @since 5.11
	 */
	public ICPPNamespace getGlobalNamespace();

	/**
	 * @deprecated Use {@link #getGlobalNamespace()}
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public IBinding resolveBinding();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTTranslationUnit copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTTranslationUnit copy(CopyStyle style);
}
