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

import java.util.Map;

/**
 * This interface represent a preprocessor #include statement.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTPreprocessorIncludeStatement extends IASTPreprocessorStatement {
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
	 * Returns macros and their definitions at the point of the include.
	 * @since 5.4
	 */
	public Map<String, String> getMacroDefinitions();

	/**
	 * Returns macros relevant to parsing of the file included by this include statement and their
	 * definitions at the point of the include. Undefined macros are represented in the map by
	 * <code>null</code> values.
	 * <p>
	 * Unlike {@link #getMacroDefinitions()}, this method can only be called after the included file
	 * has been parsed.
	 * @since 5.4
	 */
	public Map<String, String> getRelevantMacros();

	/**
	 * Returns the include guard macro for the file included by this include statement,
	 * or <code>null</code> if the file doesn't have the include guard. THe include guard macro is
	 * the macro that, when defined, guarantees that the file has no active content irrespectively
	 * of definitions of other macros.
	 * <p>
	 * This method can only be called after the included file has been parsed.
	 * @since 5.4
	 */
	public String getIncludeGuard();
}
