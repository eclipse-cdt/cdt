/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Nov 29, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;

/**
 * @author aniefer
 */
public class CPPBlockScope extends CPPNamespaceScope implements ICPPBlockScope {
	public CPPBlockScope( IASTNode physicalNode ){
		super( physicalNode );
	}
	
	public IASTName getScopeName(){
	    IASTNode node = getPhysicalNode();
	    if( node instanceof IASTCompoundStatement && node.getParent() instanceof IASTFunctionDefinition ){
	        return ((IASTFunctionDefinition)node.getParent()).getDeclarator().getName();
	    }
	    return null;
	}
}
