/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
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

	IASTName[] parameterNames = null;
	IASTDeclaration[] parameterDeclarations = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator#setParameterNames(org.eclipse.cdt.core.dom.ast.IASTName[])
	 */
	public void setParameterNames(IASTName[] names) {
		parameterNames = names;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator#getParameterNames()
	 */
	public IASTName[] getParameterNames() {
		return parameterNames;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator#setParameterDeclarations(org.eclipse.cdt.core.dom.ast.IASTDeclaration[])
	 */
	public void setParameterDeclarations(IASTDeclaration[] decls) {
		parameterDeclarations = decls;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator#getParameterDeclarations()
	 */
	public IASTDeclaration[] getParameterDeclarations() {
		return parameterDeclarations;
	}

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
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator#getDeclaratorForParameterName()
	 */
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarator#getRoleForName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public int getRoleForName(IASTName name) {
		IASTName [] n = getParameterNames();
		for( int i = 0; i < n.length; ++i )
			if( n[i] == name ) return r_unclear; 
		return super.getRoleForName(name);
	}
}
