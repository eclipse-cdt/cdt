/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Interface for problem bindings.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IProblemBinding extends IBinding, IScope, IType, ISemanticProblem {

    /**
     * Returns the AST node that this problem was created for
     */
    public IASTNode getASTNode();
	
	/**
	 * Returns the file name this problem occurred in, or <code>null</code> if it is unknown.
	 */
	public String getFileName();
	
	/**
	 * Returns the line number for this problem, or -1 if it is unknown.
	 */
	public int getLineNumber();

    /**
     * Returns bindings that were considered when resolving the node corresponding
     * to this problem binding, but rejected for some reason.
     * @return an array of candidate bindings.
     * 
	 * This method is experimental.  Clients calling this method should expect
	 * possible changes.
     * @since 5.1 experimental
     */
	public IBinding[] getCandidateBindings();

	/*
     * Parser Semantic Problems
     * All Semantic problems take a char[] as an argument
     */
  
    /**
     * Attempt to use a symbol that was not found. 
     * Require attributes: A_SYMBOL_NAME
     */ 
    public final static int SEMANTIC_NAME_NOT_FOUND = 0x001;

    /**
     * Invalid overload of a particular name.
     * Required attributes: A_SYMBOL_NAME
     */
    public static final int SEMANTIC_INVALID_OVERLOAD = 0x002;

    /**
     * Invalid using directive.  
     * Required attributes: A_NAMESPACE_NAME
     */
    public static final int SEMANTIC_INVALID_USING = 0x003;
    
    /**
     * Ambiguous lookup for given name. 
     * Required attributes: A_SYMBOL_NAME
     */
    public static final int SEMANTIC_AMBIGUOUS_LOOKUP = 0x004;

    /**
     * Invalid type provided
     * Required attributes: A_TYPE_NAME
     */
    public static final int SEMANTIC_INVALID_TYPE = 0x005;

    /**
     * circular inheritance was detected for a class
     */
    public static final int SEMANTIC_CIRCULAR_INHERITANCE = 0x006;

    /**
     * the definition for the class/function can not be found
     */
    public static final int SEMANTIC_DEFINITION_NOT_FOUND = 0x007;
    
    /**
     * the declaration for the K&R style function parameter can not be found
     */
    public static final int SEMANTIC_KNR_PARAMETER_DECLARATION_NOT_FOUND = 0x008;
    
    /**
     * a label statement can not be found to match a goto statement
     */
    public static final int SEMANTIC_LABEL_STATEMENT_NOT_FOUND = 0x009;
    
    /**
     * there was a problem creating the scope
     */
    public static final int SEMANTIC_BAD_SCOPE = 0x00A;
    
    /**
     * invalid redefinition of the name
     */
    public static final int SEMANTIC_INVALID_REDEFINITION = 0x00B;
    
    /**
     * invalid re-declaration of the name
     */
    public static final int SEMANTIC_INVALID_REDECLARATION = 0x00C;
    
    public static final int SEMANTIC_MEMBER_DECLARATION_NOT_FOUND = 0x00D;
    
	public static final int SEMANTIC_RECURSION_IN_LOOKUP = 0x00E;

	/**
	 * @deprecated, there may be additional problems.
	 */
	@Deprecated
	public static final int LAST_PROBLEM = 0x00E;

	/**
	 * @since 5.1
	 */
	public static final int SEMANTIC_INVALID_TEMPLATE_ARGUMENTS = 0x00F;

}
