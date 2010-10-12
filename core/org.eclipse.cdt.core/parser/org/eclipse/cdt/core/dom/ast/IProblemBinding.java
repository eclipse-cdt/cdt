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
    public final static int SEMANTIC_NAME_NOT_FOUND = BINDING_NOT_FOUND;
    public static final int SEMANTIC_INVALID_OVERLOAD = BINDING_INVALID_OVERLOAD;
    public static final int SEMANTIC_INVALID_USING = BINDING_INVALID_USING;
    public static final int SEMANTIC_AMBIGUOUS_LOOKUP = BINDING_AMBIGUOUS_LOOKUP;
    public static final int SEMANTIC_INVALID_TYPE = BINDING_INVALID_TYPE;
    public static final int SEMANTIC_CIRCULAR_INHERITANCE = BINDING_CIRCULAR_INHERITANCE;
    public static final int SEMANTIC_DEFINITION_NOT_FOUND = BINDING_DEFINITION_NOT_FOUND;
    public static final int SEMANTIC_KNR_PARAMETER_DECLARATION_NOT_FOUND = BINDING_KNR_PARAMETER_DECLARATION_NOT_FOUND;
    public static final int SEMANTIC_LABEL_STATEMENT_NOT_FOUND = BINDING_LABEL_STATEMENT_NOT_FOUND;
    public static final int SEMANTIC_BAD_SCOPE = BINDING_BAD_SCOPE;
    public static final int SEMANTIC_INVALID_REDEFINITION = BINDING_INVALID_REDEFINITION;
    public static final int SEMANTIC_INVALID_REDECLARATION = BINDING_INVALID_REDECLARATION;
    public static final int SEMANTIC_MEMBER_DECLARATION_NOT_FOUND = BINDING_MEMBER_DECLARATION_NOT_FOUND;
	public static final int SEMANTIC_RECURSION_IN_LOOKUP = BINDING_RECURSION_IN_LOOKUP;
	/** @since 5.1 */
	public static final int SEMANTIC_INVALID_TEMPLATE_ARGUMENTS = BINDING_INVALID_TEMPLATE_ARGUMENTS;

	/**
	 * @deprecated, there may be additional problems.
	 */
	@Deprecated
	public static final int LAST_PROBLEM = 0x00E;


}
