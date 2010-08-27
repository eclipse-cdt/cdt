/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM Corporation) - Initial API and implementation 
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;

public class CPPBlockScope extends CPPNamespaceScope implements ICPPBlockScope {
	public CPPBlockScope( IASTNode physicalNode ){
		super( physicalNode );
	}
	
	@Override
	public EScopeKind getKind() {
		return EScopeKind.eLocal;
	}
	
	@Override
	public IName getScopeName(){
	    IASTNode node = getPhysicalNode();
	    if (node instanceof IASTCompoundStatement) {
	    	final IASTNode parent= node.getParent();
	    	if (parent instanceof IASTFunctionDefinition) {
	    		IASTDeclarator dtor= ((IASTFunctionDefinition)parent).getDeclarator();
	    		dtor = ASTQueries.findInnermostDeclarator(dtor);
				return dtor.getName();
	    	}
	    }
	    return null;
	}
}
