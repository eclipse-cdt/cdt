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
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;

/**
 * @author jcamelon
 */
public interface ICPPASTNewExpression extends IASTExpression {

    public boolean isGlobal();
    public void setIsGlobal( boolean value );
    
    public static final ASTNodeProperty NEW_PLACEMENT = new ASTNodeProperty( "New Placement"); //$NON-NLS-1$
    public IASTExpression getNewPlacement();
    public void setNewPlacement( IASTExpression expression );
    
    public static final ASTNodeProperty NEW_INITIALIZER = new ASTNodeProperty( "New Initializer"); //$NON-NLS-1$
    public IASTExpression getNewInitializer();
    public void setNewInitializer( IASTExpression expression );
    
    public static final ASTNodeProperty TYPE_ID = new ASTNodeProperty( "Type Id"); //$NON-NLS-1$
    public IASTTypeId getTypeId();
    public void setTypeId( IASTTypeId typeId );

    public boolean isNewTypeId();
    public void setIsNewTypeId( boolean value );
    
    public static final ASTNodeProperty NEW_TYPEID_ARRAY_EXPRESSION = new ASTNodeProperty( "Array Size Expression"); //$NON-NLS-1$
    public IASTExpression [] getNewTypeIdArrayExpressions();
    public void addNewTypeIdArrayExpression( IASTExpression expression );

}
