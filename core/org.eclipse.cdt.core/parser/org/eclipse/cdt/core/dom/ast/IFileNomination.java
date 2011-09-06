/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface for constructs that nominate a file for an AST:
 * {@link IASTTranslationUnit}, {@link IASTPreprocessorIncludeStatement}, {@link IIndexFile}.
 * @since 5.4
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IFileNomination {
	/**
	 * Returns macros relevant to parsing of the file included by this include statement and their
	 * definitions at the point of the include. 
	 * <p>
	 * This method should only be called after the included file has been parsed. The method will
	 * return {@link ISignificantMacros#NONE}</code> if it is called prematurely.
	 * @throws CoreException 
	 */
	public ISignificantMacros getSignificantMacros() throws CoreException;
	
	/**
	 * Returns whether pragma once semantics has been detected when parsing the translation unit.
	 */
	public boolean hasPragmaOnceSemantics() throws CoreException;
}
