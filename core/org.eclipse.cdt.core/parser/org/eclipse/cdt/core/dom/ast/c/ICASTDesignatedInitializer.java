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
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;

/**
 * @author jcamelon
 */
public interface ICASTDesignatedInitializer extends
        IASTInitializer {

    public static final ICASTDesignator [] EMPTY_DESIGNATOR_ARRAY = new ICASTDesignator[0];
    public static final ASTNodeProperty DESIGNATOR = new ASTNodeProperty( "Designator"); //$NON-NLS-1$
    public void addDesignator( ICASTDesignator designator );
    public ICASTDesignator [] getDesignators();
    
    public static final ASTNodeProperty OPERAND = new ASTNodeProperty( "RHS Initializer"); //$NON-NLS-1$
    public IASTInitializer getOperandInitializer();
    public void setOperandInitializer( IASTInitializer rhs );
}
