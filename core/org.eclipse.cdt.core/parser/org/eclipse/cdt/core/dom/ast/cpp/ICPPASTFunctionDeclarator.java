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
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;

/**
 * C++ adds a few things to function declarators.
 *  
 * @author Doug Schaefer
 */
public interface ICPPASTFunctionDeclarator extends IASTFunctionDeclarator {

	
    public boolean isConst();
	public void setConst( boolean value );
	
	public boolean isVolatile();
	public void setVolatile( boolean value );
	
	public static final ASTNodeProperty EXCEPTION_TYPEID = new ASTNodeProperty( "Exception TypeId"); //$NON-NLS-1$
	public IASTTypeId [] getExceptionSpecification();
	public void addExceptionSpecificationTypeId( IASTTypeId typeId );
    /**
     * @param isPureVirtual
     */
	public boolean isPureVirtual();
    public void setPureVirtual(boolean isPureVirtual);
	
    public static final ASTNodeProperty CONSTRUCTOR_CHAIN_MEMBER = new ASTNodeProperty( "Constructor Chain Member"); //$NON-NLS-1$
    public ICPPASTConstructorChainInitializer[] getConstructorChain();
    public void addConstructorToChain( ICPPASTConstructorChainInitializer initializer );

}
