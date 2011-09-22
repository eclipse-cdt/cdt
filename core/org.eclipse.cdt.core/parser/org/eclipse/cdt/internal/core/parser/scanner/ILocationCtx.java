/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
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
