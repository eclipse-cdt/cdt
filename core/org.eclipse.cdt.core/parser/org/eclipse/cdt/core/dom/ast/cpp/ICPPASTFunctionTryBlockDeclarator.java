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

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

/**
 * @author jcamelon
 */
public interface ICPPASTFunctionTryBlockDeclarator extends
        ICPPASTFunctionDeclarator {

    public static final ASTNodeProperty CATCH_HANDLER = new ASTNodeProperty( "Catch Handler"); //$NON-NLS-1$
    public void addCatchHandler( IASTStatement statement );
    public List getCatchHandlers();
}
