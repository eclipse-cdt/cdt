/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.ast;

import org.eclipse.cdt.core.parser.Enum;

/**
 * @author jcamelon
 *
 */
public class ASTPointerOperator extends Enum 
{
    public static final ASTPointerOperator REFERENCE 	 			 	= new ASTPointerOperator( 0 ); 
	public static final ASTPointerOperator POINTER 		 			 	= new ASTPointerOperator( 1 );
	public static final ASTPointerOperator CONST_POINTER 			 	= new ASTPointerOperator( 2 );
	public static final ASTPointerOperator VOLATILE_POINTER 		 	= new ASTPointerOperator( 3 );
	public static final ASTPointerOperator RESTRICT_POINTER             = new ASTPointerOperator( 4 );

    /**
     * @param enumValue
     */
    protected ASTPointerOperator(int enumValue)
    {
        super(enumValue);
    }
    
    public boolean isStarOperator()
    {
    	return ( ( this == VOLATILE_POINTER ) || ( this == CONST_POINTER ) || ( this == RESTRICT_POINTER ) || ( this == POINTER ));
    }
    
    public boolean isReferenceOperator()
    {
    	return ( this == REFERENCE );
    }
}
