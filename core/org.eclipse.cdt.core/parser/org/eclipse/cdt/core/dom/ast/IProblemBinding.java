/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Jan 17, 2005
 */
package org.eclipse.cdt.core.dom.ast;

/**
 * @author aniefer
 */
public interface IProblemBinding extends IBinding, IScope, IType {

    /**
     * Returns the problem id
     * 
     * @return the problem id
     */
    int getID();

    /**
     * Answer a localized, human-readable message string which describes the problem.
     * 
     * @return a localized, human-readable message string which describes the problem
     */
    String getMessage();
    
    /*
     * Parser Semantic Problems
     * All Semantic problems take a char[] as an argument
     */
  
    /**
     * Attempt to use a symbol that was not found. 
     * Require attributes: A_SYMBOL_NAME
     * @see #A_SYMBOL_NAME  
     */ 
    public final static int SEMANTIC_NAME_NOT_FOUND = 0x001;

    /**
     * Invalid overload of a particular name.
     * Required attributes: A_SYMBOL_NAME
     * @see #A_SYMBOL_NAME  
     */
    public static final int SEMANTIC_INVALID_OVERLOAD = 0x002;

    /**
     * Invalid using directive.  
     * Required attributes: A_NAMESPACE_NAME
     * @see #A_NAMESPACE_NAME
     */
    public static final int SEMANTIC_INVALID_USING = 0x003;
    
    /**
     * Ambiguous lookup for given name. 
     * Required attributes: A_SYMBOL_NAME
     * @see #A_SYMBOL_NAME
     */
    public static final int SEMANTIC_AMBIGUOUS_LOOKUP = 0x004;

    /**
     * Invalid type provided
     * Required attribugtes: A_TYPE_NAME
     * @see #A_TYPE_NAME
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
     * invalid redeclaration of the name
     */
    public static final int SEMANTIC_INVALID_REDECLARATION = 0x00C;
    
    public static final int LAST_PROBLEM = SEMANTIC_INVALID_REDECLARATION;
}
