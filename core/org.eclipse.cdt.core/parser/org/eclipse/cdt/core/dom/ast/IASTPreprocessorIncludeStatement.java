/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.parser.ISignificantMacros;


/**
 * This interface represent a preprocessor #include statement.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTPreprocessorIncludeStatement extends IASTPreprocessorStatement, IFileNomination {
	/**
	 * <code>INCLUDE_NAME</code> describes the relationship between an include directive and
	 * it's name.
	 */
	public static final ASTNodeProperty INCLUDE_NAME = new ASTNodeProperty(
			"IASTPreprocessorMacroDefinition.INCLUDE_NAME - Include Name"); //$NON-NLS-1$


	/**
	 * Returns the absolute location of the file found through #include.
	 * Only valid if {@link #isResolved()} returns <code>true</code>.
	 */
	public String getPath();
	
	/**
	 * Returns the name of the file as specified in the directive. Does not include quotes or
	 * angle brackets.
	 * @since 4.0
	 */
	public IASTName getName();
	
	/**
	 * Returns whether this is a system include (one specified with angle brackets).
	 * @since 4.0
	 */
	public boolean isSystemInclude();

	/**
	 * Returns whether this include directive was actually taken.
	 * @since 4.0
	 */
	public boolean isActive();

	/**
	 * Returns whether this include file was successfully resolved.
	 * @since 4.0
	 */
	public boolean isResolved();
	
	/**
	 * Returns whether the inclusion was resolved using a heuristics.
	 * @since 5.1
	 */
	public boolean isResolvedByHeuristics();

	/**
	 * Returns the list of versions of the target file, each of which is 
	 * identified by its significant macros, that had been included 
	 * in this translation-unit prior to this statement.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public ISignificantMacros[] getLoadedVersions();

	/**
	 * Returns a hash-code for the contents of the file included, or <code>0</code>
	 * if the content has not been parsed.
	 * @since 5.4
	 */
	public long getContentsHash();

	/**
	 * Returns true, if an attempt will be or has been made to create AST for the target
	 * of this inclusion.
	 * @since 5.4
	 */
	public boolean createsAST();
	
	/**
	 * Returns the file from the index that this include statement has pulled in, or <code>null</code>
	 * if the include creates AST or is unresolved or skipped.
	 * @since 5.4
	 */
	public IIndexFile getImportedIndexFile(); 
}
