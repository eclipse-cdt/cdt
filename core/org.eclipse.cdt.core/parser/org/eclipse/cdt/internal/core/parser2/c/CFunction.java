
/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser2.c;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * Created on Nov 5, 2004
 * @author aniefer
 */
public class CFunction implements IFunction {
	final private IASTFunctionDeclarator declarator;
	final private IScope functionScope;

	public CFunction( IASTFunctionDeclarator declarator ){
		declarator = checkForDefinition( declarator );
		this.declarator = declarator;
		this.functionScope = new CFunctionScope( this );
	}
	
	private IASTFunctionDeclarator checkForDefinition( IASTFunctionDeclarator dtor ){
		if( dtor.getParent() instanceof IASTFunctionDefinition )
			return dtor;
		
		IASTFunctionDeclarator def = CVisitor.findDefinition( dtor );
		if( def != null && def != dtor ){
			dtor = def;
			((CASTName)dtor.getName()).setBinding( this );
		}
		return dtor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getParameters()
	 */
	public List getParameters() {
		List params = declarator.getParameters();
		int size = params.size();
		List result = new ArrayList( size );
		if( size > 0 ){
			for( int i = 0; i < size; i++ ){
				IASTParameterDeclaration p = (IASTParameterDeclaration) params.get(i);
				result.add( p.getDeclarator().getName().resolveBinding() );
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return declarator.getName().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return CVisitor.getContainingScope( (IASTDeclaration) declarator.getParent() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getFunctionScope()
	 */
	public IScope getFunctionScope() {
		return functionScope;
	}
	
	public IASTDeclaration getDeclaration(){
	    return (IASTDeclaration) declarator.getParent();
	}
}
