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
public interface IProblemBinding extends IBinding {

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
     */
    
    /**
     * Attempt to add a unique symbol, yet the value was already defined.
     * Require attributes: A_SYMBOL_NAME
     * @see #A_SYMBOL_NAME  
     */
    public final static int SEMANTIC_UNIQUE_NAME_PREDEFINED = 0x001;
    
    /**
     * Attempt to use a symbol that was not found. 
     * Require attributes: A_SYMBOL_NAME
     * @see #A_SYMBOL_NAME  
     */ 
    public final static int SEMANTIC_NAME_NOT_FOUND = 0x002;

    /**
     * Name not provided in context that it was required.   
     * Require attributes: none
     */
    public final static int SEMANTIC_NAME_NOT_PROVIDED = 0x003;

    /**
     * Invalid overload of a particular name.
     * Required attributes: A_SYMBOL_NAME
     * @see #A_SYMBOL_NAME  
     */
    public static final int SEMANTIC_INVALID_OVERLOAD = 0x004;

    /**
     * Invalid using directive.  
     * Required attributes: A_NAMESPACE_NAME
     * @see #A_NAMESPACE_NAME
     */
    public static final int SEMANTIC_INVALID_USING = 0x005;
    
    /**
     * Ambiguous lookup for given name. 
     * Required attributes: A_SYMBOL_NAME
     * @see #A_SYMBOL_NAME
     */
    public static final int SEMANTIC_AMBIGUOUS_LOOKUP = 0x006;

    /**
     * Invalid type provided
     * Required attribugtes: A_TYPE_NAME
     * @see #A_TYPE_NAME
     */
    public static final int SEMANTIC_INVALID_TYPE = 0x007;

    public static final int SEMANTIC_CIRCULAR_INHERITANCE = 0x008;

    public static final int SEMANTIC_INVALID_TEMPLATE = 0x009;

    public static final int SEMANTIC_BAD_VISIBILITY = 0x00A;

    public static final int SEMANTIC_UNABLE_TO_RESOLVE_FUNCTION = 0x00B;

    public static final int SEMANTIC_INVALID_TEMPLATE_ARGUMENT = 0x00C;

    public static final int SEMANTIC_INVALID_TEMPLATE_PARAMETER = 0x00D;

    public static final int SEMANTIC_REDECLARED_TEMPLATE_PARAMETER = 0x00E;

    public static final int SEMANTIC_INVALID_CONVERSION_TYPE = 0x00F;

    public static final int SEMANTIC_MALFORMED_EXPRESSION = 0x010;

    public static final int SEMANTIC_ILLFORMED_FRIEND = 0x011;
    
    public static final int SEMANTIC_RECURSIVE_TEMPLATE_INSTANTIATION = 0x012;
    public static final int LAST_PROBLEM = SEMANTIC_RECURSIVE_TEMPLATE_INSTANTIATION;
}
