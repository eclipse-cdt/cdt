/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represent a preprocessor #include statement.
 * 
 * @author jcamelon
 */
public interface IASTPreprocessorIncludeStatement extends
		IASTPreprocessorStatement {

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
}
