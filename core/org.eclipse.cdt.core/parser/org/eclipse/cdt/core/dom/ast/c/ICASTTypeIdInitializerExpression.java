/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;

/**
 * @author jcamelon
 */
public interface ICASTTypeIdInitializerExpression extends IASTExpression {

    public static final ASTNodeProperty TYPE_ID = new ASTNodeProperty( "TypeId"); //$NON-NLS-1$
    public static final ASTNodeProperty INITIALIZER = new ASTNodeProperty( "Initializer"); //$NON-NLS-1$
    
    public IASTTypeId getTypeId();
    public void setTypeId( IASTTypeId typeId );
    public IASTInitializer getInitializer();
    public void setInitializer( IASTInitializer initializer );
    
}
