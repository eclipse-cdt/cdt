/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
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

}
