/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represent a preprocessor function-style macro definition. e.g.
 * #define ABC( def ) GHI
 * 
 * Note: macros that are expanded as parameters to function style macros are not captured in this abstraction.
 *  
 * @author jcamelon
 */
public interface IASTPreprocessorFunctionStyleMacroDefinition extends
		IASTPreprocessorMacroDefinition {

	/**
	 * This property represents the relationship between a function style macro
	 * definition and one of its parameters.
	 */
	public static final ASTNodeProperty PARAMETER = new ASTNodeProperty(
			"IASTPreprocessorFunctionStyleMacroDefinition.PARAMETER - Function Macro Parameter"); //$NON-NLS-1$

	/**
	 * Get the macro parameters.
	 * 
	 * @return <code>IASTFunctionStyleMacroParameter[]</code> parameters
	 */
	public IASTFunctionStyleMacroParameter[] getParameters();

	/**
	 * Add a function-style macro parameter.
	 * 
	 * @param parm
	 *            <code>IASTFunctionStyleMacroParameter</code>
	 */
	public void addParameter(IASTFunctionStyleMacroParameter parm);

}