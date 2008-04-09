/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;

/**
 * A K&R C function declarator.
 *
 * @author dsteffle
 */
public class CASTKnRFunctionDeclarator extends CASTDeclarator implements ICASTKnRFunctionDeclarator {

	IASTName[] parameterNames = IASTName.EMPTY_NAME_ARRAY;
	IASTDeclaration[] parameterDeclarations = IASTDeclaration.EMPTY_DECLARATION_ARRAY;
	

	public CASTKnRFunctionDeclarator() {
	}

	public CASTKnRFunctionDeclarator(IASTName[] parameterNames, IASTDeclaration[] parameterDeclarations) {
		setParameterNames(parameterNames);
		setParameterDeclarations(parameterDeclarations);
	}


	public void setParameterNames(IASTName[] names) {
		parameterNames = names;
		for(int i = 0; i < names.length; i++) {
			IASTName name = names[i];
			if (name != null) {
				name.setParent(this);
				name.setPropertyInParent(PARAMETER_NAME);
			}
		}
	}


	public IASTName[] getParameterNames() {
		return parameterNames;
	}


	public void setParameterDeclarations(IASTDeclaration[] decls) {
		parameterDeclarations = decls;
		for(int i = 0; i < parameterDeclarations.length; i++) {
			IASTDeclaration decl = parameterDeclarations[i];
			if (decl != null) {
				decl.setParent(this);
				decl.setPropertyInParent(FUNCTION_PARAMETER);
			}
		}
	}


	public IASTDeclaration[] getParameterDeclarations() {
		return parameterDeclarations;
	}

    @Override
	protected boolean postAccept( ASTVisitor action ){
        IASTName [] ns = getParameterNames();
        for ( int i = 0; i < ns.length; i++ ) {
            if( !ns[i].accept( action ) ) return false;
        }
        
        IASTDeclaration [] params = getParameterDeclarations();
        for ( int i = 0; i < params.length; i++ ) {
            if( !params[i].accept( action ) ) return false;
        }

        return true;
    }
	
	public IASTDeclarator getDeclaratorForParameterName(IASTName name) {
		boolean found=false;
		for(int i=0; i<parameterNames.length; i++) {
			if (parameterNames[i] == name) found = true;
		}
		if(!found) return null;
		
		for(int i=0; i<parameterDeclarations.length; i++) {
			if (parameterDeclarations[i] instanceof IASTSimpleDeclaration) {
				IASTDeclarator[] decltors = ((IASTSimpleDeclaration)parameterDeclarations[i]).getDeclarators();
				for(int j=0; j<decltors.length; j++) {
					if(decltors[j].getName().toString().equals(name.toString()))
						return decltors[j];
				}
			}
		}
		
		return null;
	}
	
	@Override
	public int getRoleForName(IASTName name) {
		IASTName [] n = getParameterNames();
		for( int i = 0; i < n.length; ++i )
			if( n[i] == name ) return r_unclear; 
		return super.getRoleForName(name);
	}
}
