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

	/**
	 * <code>OWNED_DECLARATION</code> represents the relationship between an <code>IASTTranslationUnit</code> and
	 * it's nested <code>IASTDeclaration</code>'s.
	 */
	public static final ASTNodeProperty OWNED_DECLARATION = new ASTNodeProperty(
			"IASTTranslationUnit.OWNED_DECLARATION - IASTDeclaration for IASTTranslationUnit"); //$NON-NLS-1$

	/**
	 * <code>SCANNER_PROBLEM</code> represents the relationship between an <code>IASTTranslationUnit</code> and
	 * it's nested <code>IASTProblem</code>.
	 */
	public static final ASTNodeProperty SCANNER_PROBLEM = new ASTNodeProperty(
			"IASTTranslationUnit.SCANNER_PROBLEM - IASTProblem (scanner caused) for IASTTranslationUnit"); //$NON-NLS-1$

	/**
	 * <code>PREPROCESSOR_STATEMENT</code> represents the relationship between an <code>IASTTranslationUnit</code> and
	 * it's nested <code>IASTPreprocessorStatement</code>.
	 */
	public static final ASTNodeProperty PREPROCESSOR_STATEMENT = new ASTNodeProperty(
			"IASTTranslationUnit.PREPROCESSOR_STATEMENT - IASTPreprocessorStatement for IASTTranslationUnit"); //$NON-NLS-1$
    
	/**
	 * A translation unit contains an ordered sequence of declarations.
	 * 
	 * @return List of IASTDeclaration
	 */
	public IASTDeclaration[] getDeclarations();

	/**
	 * Add declaration to translation unit. 
	 * 
	 * @param declaration <code>IASTDeclaration</code>
	 */
	public void addDeclaration(IASTDeclaration declaration);

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
	public IASTName[] getDeclarations(IBinding binding);

	/**
	 * Returns the list of references in this translation unit to the given
	 * binding. This list contains the IASTName nodes that represent a use of
	 * the binding.
	 * 
	 * @param binding
	 * @return List of IASTName nodes representing uses of the binding
	 */
	public IASTName[] getReferences(IBinding binding);

	/**
	 * @param offset
	 * @param length
	 * @return
	 */
	public IASTNodeLocation[] getLocationInfo(int offset, int length);

	/**
	 * Select the node in the treet that best fits the offset/length/file path. 
	 * 
	 * @param path - file name specified through path
	 * @param offset - location in the file as an offset
	 * @param length - length of selection
	 * @return <code>IASTNode</code> that best fits
	 */
	public IASTNode selectNodeForLocation(String path, int offset, int length);

	/**
	 * Get the macro definitions encountered in parsing this translation unit. 
	 * 
	 * @return <code>IASTPreprocessorMacroDefinition[]</code>
	 */
	public IASTPreprocessorMacroDefinition[] getMacroDefinitions();

	/**
	 * Get the #include directives encountered in parsing this translation unit.
	 * @return <code>IASTPreprocessorIncludeStatement[]</code>
	 */
	public IASTPreprocessorIncludeStatement[] getIncludeDirectives();

	/**
	 * Get all preprocessor statements.
	 * 
	 * @return <code>IASTPreprocessorStatement[]</code>
	 */
	public IASTPreprocessorStatement[] getAllPreprocessorStatements();

	/**
	 * Get all preprocessor and scanner problems.
	 * @return <code>IASTProblem[]</code>
	 */
	public IASTProblem[] getPreprocessorProblems();

	/**
	 * For a given range of locations, return a String that represents what is there underneath the range.
	 * 
	 * @param locations A range of node locations
	 * @return A String signature.
	 */
	public String getUnpreprocessedSignature(IASTNodeLocation[] locations);

	/**
	 * Get the translation unit's full path.  
	 * @return String representation of path.
	 */
	public String getFilePath();
    
    /**
     * Flatten the node locations provided into a single file location.  
     * 
     * @param nodeLocations <code>IASTNodeLocation</code>s to flatten
     * @return null if not possible, otherwise, a file location representing where the macros are. 
     */
    public IASTFileLocation flattenLocationsToFile( IASTNodeLocation [] nodeLocations );
    
    public static final ASTNodeProperty EXPANSION_NAME = new ASTNodeProperty(
    "IASTTranslationUnit.EXPANSION_NAME - IASTName generated for macro expansions."); //$NON-NLS-1$
    
    
    public static interface IDependencyTree
    {
        public String getTranslationUnitPath();
        
        public static interface IASTInclusionNode
        {
            public IASTPreprocessorIncludeStatement getIncludeDirective();
            public IASTInclusionNode [] getNestedInclusions();
        }
        
        public IASTInclusionNode [] getInclusions();
    }
    
    public IDependencyTree getDependencyTree();
    
    
    
}