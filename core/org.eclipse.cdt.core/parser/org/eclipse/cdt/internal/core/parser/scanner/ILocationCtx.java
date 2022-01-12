/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.internal.core.parser.scanner;

/**
 * Interface between location map and preprocessor for modeling contexts that can deal with offsets.
 * These are:
 * synthetic contexts used for pre-included files, file-contexts, macro-expansions.
 * @since 5.0
 */
public interface ILocationCtx {
	/**
	 * If this is a file context the filename of this context is returned,
	 * otherwise the filename of the first enclosing context that is a file context is returned.
	 */
	String getFilePath();

	/**
	 * Returns the enclosing context or <code>null</code> if this is the translation unit context.
	 */
	ILocationCtx getParent();

	/**
	 * Returns inclusion statement that created this context, or <code>null</code>.
	 */
	ASTInclusionStatement getInclusionStatement();
}
