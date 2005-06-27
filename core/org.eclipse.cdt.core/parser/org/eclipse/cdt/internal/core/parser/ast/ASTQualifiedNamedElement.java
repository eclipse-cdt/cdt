/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast;

import java.util.ArrayList;

import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTScopedElement;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;

/**
 * @author jcamelon
 *
 */
public class ASTQualifiedNamedElement implements IASTQualifiedNameElement
{
    /**
     * @param scope
     */
    public ASTQualifiedNamedElement(IASTScope scope, char[] name )
    {
        ArrayList names = new ArrayList(4);
		IASTScope parent = scope;
        
		names.add( name ); // push on our own name
		while (parent != null)
		{
			if (parent instanceof IASTNamespaceDefinition
				|| parent instanceof IASTClassSpecifier )
			{
				names.add( ((IASTOffsetableNamedElement)parent).getNameCharArray() );
				if( parent instanceof IASTScopedElement  )
					parent = ((IASTScopedElement)parent).getOwnerScope();				
			}
			else if( parent instanceof IASTTemplateDeclaration )
			{
				if( parent instanceof IASTScopedElement  )
					parent = ((IASTScopedElement)parent).getOwnerScope();
				continue;
			}
			else 
				break;
		}
		if (names.size() != 0)
		{
			qualifiedNames = new char[names.size()][];
			int counter = 0;
			for( int i = names.size() - 1; i >= 0; i-- )
				qualifiedNames[counter++] = (char[])names.get(i);
		}
		else 
			qualifiedNames = null;

    }
    
	public String[] getFullyQualifiedName()
	{
	    String[] result = new String[qualifiedNames.length ];
	    for( int i = 0; i < qualifiedNames.length; i++ ){
	        result[i] = String.valueOf(qualifiedNames[i]);
	    }
		return result;
	}
	
	public char[][] getFullyQualifiedNameCharArrays(){
	    return qualifiedNames;
	}

    private final char[][] qualifiedNames;

}
