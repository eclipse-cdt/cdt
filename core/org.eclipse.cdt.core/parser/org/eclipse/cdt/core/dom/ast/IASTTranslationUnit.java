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
 * The translation unit represents a compilable unit of source.
 *
 * @author Doug Schaefer
 */
public interface IASTTranslationUnit extends IASTNode {

	public static final ASTNodeProperty OWNED_DECLARATION = new ASTNodeProperty( "Owned" ); //$NON-NLS-1$
    public static final ASTNodeProperty SCANNER_PROBLEM =  new ASTNodeProperty( "Scanner Problem"); //$NON-NLS-1$

    /**
	 * A translation unit contains an ordered sequence of declarations.
	 * 
	 * @return List of IASTDeclaration
	 */
	public IASTDeclaration[] getDeclarations();
	
	public void addDeclaration( IASTDeclaration declaration );
	
	/**
	 * This returns the global scope for the translation unit.
	 * 
	 * @return the global scope
	 */
	public IScope getScope();
	
	/**
	 * Returns the list of declarations in this translation unit for the given
	 * binding. The list contains the IASTName nodes that declare the binding.
	 * 
	 * @param binding 
	 * @return List of IASTName nodes for the binding's declaration
	 */
	public IASTDeclaration[] getDeclarations(IBinding binding);

	/**
	 * Returns the list of references in this translation unit to the given
	 * binding. This list contains the IASTName nodes that represent a use of
	 * the binding.
	 * 
	 * @param binding
	 * @return List of IASTName nodes representing uses of the binding
	 */
	public IASTName[] getReferences(IBinding binding);
	
	public IASTNodeLocation getLocationInfo( int offset );
	public IASTNodeLocation [] getLocationInfo( int offset, int length );
	
	public IASTNode[] selectNodesForLocation( String path, int offset, int length );
	public IASTNode[] selectNodesForLocation( int offset, int length );
	
	public IASTMacroDefinition [] getMacroDefinitions();
	public IASTPreprocessorIncludeStatement [] getIncludeDirectives();
	public IASTPreprocessorStatement [] getAllPreprocessorStatements();
    public IASTProblem [] getPreprocesorProblems();
	
}
