/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;

import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol;

/**
 * @author jcamelon
 *
 */
public class ASTTemplateSpecialization extends ASTTemplateDeclaration implements IASTTemplateSpecialization
{
	private ISymbol owned = null;
    /**
     * 
     */
    public ASTTemplateSpecialization( ITemplateSymbol template, IASTScope scope  )
    {
        super(template, scope, null);
    }

    public IASTDeclaration getOwnedDeclaration()
    {
    	if( owned != null && owned.getASTExtension() != null )
    		return owned.getASTExtension().getPrimaryDeclaration();
    	
    	return null;
    }
    
	public void setOwnedDeclaration(ISymbol symbol) {
		owned = symbol;
	}
}
