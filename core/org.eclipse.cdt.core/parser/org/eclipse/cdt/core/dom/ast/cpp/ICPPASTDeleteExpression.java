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

/**
 * @author jcamelon
 */
public interface ICPPASTDeleteExpression extends IASTExpression {

    public static final ASTNodeProperty OPERAND = new ASTNodeProperty( "Operand"); //$NON-NLS-1$
    public IASTExpression getOperand();
    public void setOperand( IASTExpression expression );
    /**
     * @param global
     */
    public void setIsGlobal(boolean global);
    public boolean isGlobal();

    /**
     * @param vectored
     */
    public void setIsVectored(boolean vectored);
    public boolean isVectored();

}
