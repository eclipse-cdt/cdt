/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;

/**
 * General class for common Node operations.
 * 
 * @author Lukas Felber
 *
 */
public class NodeHelper {

	public static IASTDeclaration[] getDeclarations(IASTNode parent) {
		if (parent instanceof ICPPASTCompositeTypeSpecifier) {
			return ((ICPPASTCompositeTypeSpecifier) parent).getMembers();
		} else if (parent instanceof CPPASTTranslationUnit) {
			return ((CPPASTTranslationUnit) parent).getDeclarations();
		} else if (parent instanceof CPPASTNamespaceDefinition) {
			return ((CPPASTNamespaceDefinition) parent).getDeclarations();
		}
		return new IASTDeclaration[0]; 
	}

	
	public static IASTNode findFollowingNode(IASTNode currentNode) {
		if(currentNode == null || currentNode.getParent() == null) {
			return null;
		}
		boolean match = false;
		for(IASTNode actNode : getDeclarations(currentNode.getParent())) {
			if(match) {
				return actNode;
			}
			if(actNode.equals(currentNode)) {
				match = true;
			}
		}
		return null;
	}
	
	public static IASTNode getTopLevelParent(IASTNode currentNode) {
		while(currentNode != null && currentNode.getParent() != null && currentNode.getParent().getParent() != null) {
			return getTopLevelParent(currentNode.getParent());
		}
		return currentNode;
	}
}
