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
package org.eclipse.cdt.internal.core.parser.ast.quick;

import java.util.Stack;

import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTScope;

/**
 * @author jcamelon
 *
 */
public class ASTQualifiedNamedDeclaration extends ASTDeclaration
{

    /**
     * @param scope
     */
    public ASTQualifiedNamedDeclaration(IASTScope scope, String name )
    {
        super(scope);
		Stack names = new Stack();
		IASTScope parent = getOwnerScope();
        
		names.push( name ); // push on our own name
		while (parent != null)
		{
			if (parent instanceof IASTNamespaceDefinition
				|| parent instanceof IASTClassSpecifier )
			{
				names.push(((IASTOffsetableNamedElement)parent).getName());
				parent = ((IASTDeclaration)parent).getOwnerScope();
			}
			else
				break;
		}
		if (names.size() != 0)
		{
			qualifiedNames = new String[names.size()];
			int counter = 0;
			while (!names.empty())
				qualifiedNames[counter++] = (String)names.pop();
		}
		else 
			qualifiedNames = null;

    }
    
	public String[] getFullyQualifiedName()
	{
		return qualifiedNames;
	}

    private final String[] qualifiedNames;

}
