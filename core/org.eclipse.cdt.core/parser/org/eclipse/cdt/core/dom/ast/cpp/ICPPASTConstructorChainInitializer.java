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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * @author jcamelon
 */
public interface ICPPASTConstructorChainInitializer extends IASTNode {
    public static final ICPPASTConstructorChainInitializer [] EMPTY_CONSTRUCTORCHAININITIALIZER_ARRAY = new ICPPASTConstructorChainInitializer[0];
    
    public static final ASTNodeProperty MEMBER_ID = new ASTNodeProperty( "Member Initializer Id"); //$NON-NLS-1$
    public IASTName getMemberInitializerId();
    public void setMemberInitializerId( IASTName name );
    
    public static final ASTNodeProperty INITIALIZER = new ASTNodeProperty( "Expression Initializer"); //$NON-NLS-1$
    public IASTExpression getInitializerValue();
    public void setInitializerValue( IASTExpression expression );

}
