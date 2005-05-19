/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on May 18, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTSimpleDeclSpecifier;

/**
 * @author aniefer
 *
 */
public class GCCASTSimpleDeclSpecifier extends CASTSimpleDeclSpecifier
		implements IGCCASTSimpleDeclSpecifier {

	private IASTExpression typeOfExpression;
	
	/**
	 * 
	 */
	public GCCASTSimpleDeclSpecifier() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTSimpleDeclSpecifier#setTypeofExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
	 */
	public void setTypeofExpression(IASTExpression typeofExpression) {
		this.typeOfExpression = typeofExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTSimpleDeclSpecifier#getTypeofExpression()
	 */
	public IASTExpression getTypeofExpression() {
		return typeOfExpression;
	}

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
		if( typeOfExpression != null )
			if( !typeOfExpression.accept( action ) ) return false;
		
        return true;
    }
}
