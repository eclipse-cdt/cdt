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
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

/**
 * @author jcamelon
 */
public interface ICPPASTCatchHandler extends IASTStatement {

	public static final ICPPASTCatchHandler [] EMPTY_CATCHHANDLER_ARRAY = new ICPPASTCatchHandler[0];
	
    public static final ASTNodeProperty DECLARATION = new ASTNodeProperty( "Declaration"); //$NON-NLS-1$
    public static final ASTNodeProperty CATCH_BODY = new ASTNodeProperty( "Catch Body"); //$NON-NLS-1$

    /**
     * @param isEllipsis
     */
    public void setIsCatchAll(boolean isEllipsis);
    public boolean isCatchAll();

    /**
     * @param compoundStatement
     */
    public void setCatchBody(IASTStatement compoundStatement);
    public IASTStatement getCatchBody();

    /**
     * @param decl
     */
    public void setDeclaration(IASTDeclaration decl);
    public IASTDeclaration getDeclaration();
    

}
