/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.core.runtime.IAdaptable;

/**
 * The translation unit represents a compilable unit of source.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTTranslationUnit extends IASTDeclarationListOwner, IAdaptable {

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
	 * Adds declaration to translation unit. 
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
	 * binding. The list contains the IName nodes that declare the binding.
	 * These may be part of the AST or are pulled in from the index.
	 * 
	 * @param binding
	 * @return Array of IName nodes for the binding's declaration
	 */
	public IName[] getDeclarations(IBinding binding);
    
	/**
	 * Returns the list of declarations in this translation unit for the given
	 * binding. The list contains the IASTName nodes that declare the binding.
	 * These are part of the AST no declarations are pulled in from the index.
	 * 
	 * @param binding
	 * @return Array of IASTName nodes for the binding's declaration
	 */
	public IASTName[] getDeclarationsInAST(IBinding binding);

	/**
     * Returns the array of definitions in this translation unit for the given binding.
     * The array contains the IName nodes that define the binding.
	 * These may be part of the AST or are pulled in from the index.
     *  
     * @param binding
     * @return the definition of the IBinding
     */
    public IName[] getDefinitions(IBinding binding);

	/**
     * Returns the array of definitions in this translation unit for the given binding.
     * The array contains the IASTName nodes that define the binding.
	 * These are part of the AST no definitions are pulled in from the index.
	 * 
	 * @param binding
	 * @return Array of IASTName nodes for the binding's declaration
	 */
	public IASTName[] getDefinitionsInAST(IBinding binding);

	/**
	 * Returns the list of references in this translation unit to the given
	 * binding. This list contains the IASTName nodes that represent a use of
	 * the binding. These are part of the AST no definitions are pulled in from 
	 * the index.
	 * 
	 * @param binding
	 * @return List of IASTName nodes representing uses of the binding
	 */
	public IASTName[] getReferences(IBinding binding);
	
	/**
	 * Returns an IASTNodeSelector object for finding nodes by file offsets.
	 * The object is suitable for working in one of the files that is part of
	 * the translation unit.
	 * @param filePath file of interest, as returned by {@link IASTFileLocation#getFileName()},
	 * or <code>null</code> to specify the root source of the translation-unit.
	 * @return an IASTNodeSelector.
	 * @since 5.0
	 */
	public IASTNodeSelector getNodeSelector(String filePath);
 
	/**
	 * @deprecated use {@link #getNodeSelector(String)}, instead.
	 */
	@Deprecated
	public IASTNode selectNodeForLocation(String path, int offset, int length);

	/**
	 * Returns the macro definitions encountered in parsing this translation unit. The result will not contain
	 * definitions for built-in macros.
	 * <p>
	 * In case the information for a header-file is pulled in from the index,
	 * macro definitions contained therein are not returned.
	 */
	public IASTPreprocessorMacroDefinition[] getMacroDefinitions();

	/**
	 * Returns built-in macro definitions used when parsing this translation unit.
	 * This includes macros obtained from the index. 
	 */
	public IASTPreprocessorMacroDefinition[] getBuiltinMacroDefinitions();

	/**
	 * Returns the include directives encountered in parsing this translation unit. This will also contain directives
	 * used for handling the gcc-options -imacros and -include.
	 * <p>
	 * In case the information for a header-file is pulled in from the index,
	 * include directives contained therein are not returned.
	 */
	public IASTPreprocessorIncludeStatement[] getIncludeDirectives();

	/**
	 * Returns all preprocessor statements. 
	 * In case the information for a header-file is pulled in from the index,
	 * preprocessing statements contained therein are not returned.
	 */
	public IASTPreprocessorStatement[] getAllPreprocessorStatements();

	/**
	 * Returns an array with all macro expansions of this translation unit.
	 */
	public IASTPreprocessorMacroExpansion[] getMacroExpansions();
	
	/**
	 * Returns all preprocessor and scanner problems.
	 * @return <code>IASTProblem[]</code>
	 */
	public IASTProblem[] getPreprocessorProblems();

	/**
	 * Fast access to the count of preprocessor problems to support statistics.
	 */
	public int getPreprocessorProblemsCount();

	/**
	 * Returns the translation unit's full path.  
	 * @return String representation of path.
	 */
	public String getFilePath();
    
    /**
     * Flattens the node locations provided into a single file location.  
     * 
     * @param nodeLocations <code>IASTNodeLocation</code>s to flatten
     * @return null if not possible, otherwise, a file location representing where the macros are. 
     */
    public IASTFileLocation flattenLocationsToFile(IASTNodeLocation[] nodeLocations);
    
    /**
     * @deprecated names for macro expansions are nested inside of {@link IASTPreprocessorMacroExpansion}.
     */
    @Deprecated
	public static final ASTNodeProperty EXPANSION_NAME = new ASTNodeProperty(
    		"IASTTranslationUnit.EXPANSION_NAME - IASTName generated for macro expansions."); //$NON-NLS-1$

    public static final ASTNodeProperty MACRO_EXPANSION = new ASTNodeProperty(
    		"IASTTranslationUnit.MACRO_EXPANSION - IASTPreprocessorMacroExpansion node for macro expansions."); //$NON-NLS-1$

    public static interface IDependencyTree {
        public String getTranslationUnitPath();
        
        public static interface IASTInclusionNode {
            public IASTPreprocessorIncludeStatement getIncludeDirective();
            public IASTInclusionNode[] getNestedInclusions();
        }
        
        public IASTInclusionNode[] getInclusions();
    }
    
    /**
     * Returns the dependency tree for the translation unit. 
	 * <p>
	 * In case the information for a header-file is pulled in from the index,
	 * dependencies contained therein are not part of the dependency tree.
     */
    public IDependencyTree getDependencyTree();

	/**
	 * @param offset
	 */
	public String getContainingFilename(int offset);

	/**
	 * @deprecated don't use it.
	 */
    @Deprecated
	public ParserLanguage getParserLanguage();
    
    /**
     * Returns the Index associated with this translation unit.
     * 
     * @return the Index for this translation unit.
     */
    public IIndex getIndex();
    
    /**
     * Return the set of files that have been skipped because they have been part of the index
     * prior to creating this AST, or <code>null</code> if not available.
     * Applies only, if AST was created with an index and the option to skip headers found in the
     * index.
     * @since 5.1
     */
    IIndexFileSet getIndexFileSet();

    /**
     * Return the set of files in the index that are superseded by this AST, 
     * or <code>null</code> if not available.
     * Applies only, if AST was created with an index.
     * @since 5.3
     */
    IIndexFileSet getASTFileSet();

	/**
	 * In case the AST was created in a way that supports comment parsing,
	 * all comments of the translation unit are returned. Otherwise an
	 * empty array will be supplied.
	 * 
	 * @return <code>IASTComment[]</code>
	 * @since 4.0
	 */
	public IASTComment[] getComments();
	
	/**
	 * Returns the linkage this AST was parsed in.
	 */
	public ILinkage getLinkage();
	
	/**
	 * Returns whether this AST represents a header file.
	 */
	public boolean isHeaderUnit();

	/**
	 * Returns the node factory that was used to build the AST.
	 * @since 5.2
	 */
	public INodeFactory getASTNodeFactory();

    /**
     * Sets the Index to be used for this translation unit.
     * @noreference This method is not intended to be referenced by clients.
     */
    public void setIndex(IIndex index);

	/**
	 * Sets whether this AST represents a header file.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void setIsHeaderUnit(boolean headerUnit);

	/**
	 * Causes this node and all the nodes rooted at this node to become immutable. 
	 * Once the AST is frozen any calls to set or add methods on any of the nodes 
	 * in the AST will result in an IllegalStateException.
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 5.1
	 */
	public void freeze();

	/**
	 * Returns a copy of the AST, however the ILocationResolver 
	 * and the preprocessor nodes are not copied.
	 * 
	 * @see IASTNode#copy()
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 5.1
	 */
	public IASTTranslationUnit copy();
}
