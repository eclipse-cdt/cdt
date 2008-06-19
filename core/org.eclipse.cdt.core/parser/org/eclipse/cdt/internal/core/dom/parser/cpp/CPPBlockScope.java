/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

/*
 * Created on Nov 29, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * @author aniefer
 */
public class CPPBlockScope extends CPPNamespaceScope implements ICPPBlockScope {
	public CPPBlockScope( IASTNode physicalNode ){
		super( physicalNode );
	}
	
	@Override
	public IName getScopeName(){
	    IASTNode node = getPhysicalNode();
	    if (node instanceof IASTCompoundStatement) {
	    	final IASTNode parent= node.getParent();
	    	if (parent instanceof IASTFunctionDefinition) {
	    		IASTDeclarator dtor= ((IASTFunctionDefinition)parent).getDeclarator();
	    		dtor = CPPVisitor.findInnermostDeclarator(dtor);
				return dtor.getName();
	    	}
	    }
	    return null;
	}
}
