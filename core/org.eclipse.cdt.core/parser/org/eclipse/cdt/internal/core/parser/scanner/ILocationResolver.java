/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree;


/**
 * Interface between the ast and the location-resolver for resolving offsets.
 * @since 5.0
 */
public interface ILocationResolver extends org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver {
    
	/**
	 * Introduces the ast translation unit to the location resolver. Must be called before any tokens from the
	 * scanner are obtained.
	 */
	void setRootNode(IASTTranslationUnit tu);

	/**
	 * @see IASTTranslationUnit#getAllPreprocessorStatements()
	 */
	IASTPreprocessorStatement [] getAllPreprocessorStatements();

	/**
	 * @see IASTTranslationUnit#getMacroDefinitions()
	 */
	IASTPreprocessorMacroDefinition [] getMacroDefinitions();

	/**
	 * @see IASTTranslationUnit#getBuiltinMacroDefinitions()
	 */
	IASTPreprocessorMacroDefinition [] getBuiltinMacroDefinitions();

	/**
	 * @see IASTTranslationUnit#getIncludeDirectives()
	 */
	IASTPreprocessorIncludeStatement [] getIncludeDirectives();

	/**
	 * @see IASTTranslationUnit#getPreprocessorProblems()
	 */
    IASTProblem[] getScannerProblems();

    /**
     * @see IASTTranslationUnit#getFilePath()
     */
    public String getTranslationUnitPath();
    
    /**
     * @see IASTTranslationUnit#getContainingFilename()
     */
	public String getContainingFilename(int offset);
	
    /**
     * @see IASTTranslationUnit#getDependencyTree()
     */
    public IDependencyTree getDependencyTree();

    /**
     * Returns explicit and implicit references for a macro.
     */
    public IASTName[] getReferences(IMacroBinding binding);

    /**
     * Returns the definition for a macro.
     */
    public IASTName[] getDeclarations(IMacroBinding binding);
}
