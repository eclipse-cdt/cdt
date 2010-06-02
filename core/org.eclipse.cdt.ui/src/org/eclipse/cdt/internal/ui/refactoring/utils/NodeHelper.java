/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.refactoring.MethodContext;

/**
 * General class for common Node operations.
 * 
 * @author Lukas Felber & Guido Zgraggen
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
	
	public static IASTNode findTopLevelParent(IASTNode currentNode) {
		while(currentNode != null && currentNode.getParent() != null && currentNode.getParent().getParent() != null) {
			return findTopLevelParent(currentNode.getParent());
		}
		return currentNode;
	}
	
	public static boolean isSameNode(IASTNode node1, IASTNode node2) {
		if(node1 == null || node2 == null) {
			return false;
		}
		return node1.getNodeLocations()[0].getNodeOffset() == node2.getNodeLocations()[0].getNodeOffset() 
			&& node1.getNodeLocations()[0].getNodeLength() == node2.getNodeLocations()[0].getNodeLength()
			&& new Path(node1.getFileLocation().getFileName()).equals(new Path(node2.getFileLocation().getFileName()));
	}
	
	public static IASTSimpleDeclaration findSimpleDeclarationInParents(IASTNode node) {
		while(node != null){
			if (node instanceof IASTSimpleDeclaration) {
				return (IASTSimpleDeclaration) node;
			}
			node = node.getParent();
		}
		return null;
	}
	
	public static MethodContext findMethodContext(IASTNode node, IIndex index) throws CoreException{
		IASTTranslationUnit translationUnit = node.getTranslationUnit();
		boolean found = false;
		MethodContext context = new MethodContext();
		context.setType(MethodContext.ContextType.NONE);
		IASTName name = null;
		while(node != null && !found){
			node = node.getParent();
			if(node instanceof IASTFunctionDeclarator){
				name=((IASTFunctionDeclarator)node).getName();
				found = true;
				context.setType(MethodContext.ContextType.FUNCTION);
			} else if (node instanceof IASTFunctionDefinition){
				name=CPPVisitor.findInnermostDeclarator(((IASTFunctionDefinition)node).getDeclarator()).getName();
				found = true;
				context.setType(MethodContext.ContextType.FUNCTION);
			} 
		}
		if(index != null) {
			getMethodContexWithIndex(index, translationUnit, context, name);
		}else {
			getMethodContex(translationUnit, context, name);
		}
		return context;
	}


	private static void getMethodContex(IASTTranslationUnit translationUnit,
			MethodContext context, IASTName name) {
		if(name instanceof ICPPASTQualifiedName){
			 ICPPASTQualifiedName qname =( ICPPASTQualifiedName )name;
			 context.setMethodQName(qname);
			 IBinding bind = qname.resolveBinding();
			 IASTName[] decl = translationUnit.getDeclarationsInAST(bind);
			 for (IASTName tmpname : decl) {
				 IASTNode methoddefinition = tmpname.getParent().getParent();
				 if (methoddefinition instanceof IASTSimpleDeclaration) {
					 context.setMethodDeclarationName(tmpname);
					 context.setType(MethodContext.ContextType.METHOD);
				 }
			 }

		 }
	}


	private static void getMethodContexWithIndex(IIndex index,
			IASTTranslationUnit translationUnit, MethodContext context,
			IASTName name) throws CoreException {
		IBinding bind = name.resolveBinding();
		if (bind instanceof ICPPMethod) {
			context.setType(MethodContext.ContextType.METHOD);
			IIndexName[] decl;
			decl = index.findDeclarations(bind);
			String tuFileLoc = translationUnit.getFileLocation().getFileName(); 
			if(decl.length == 0) {
				context.setMethodDeclarationName(name);
			}
			for (IIndexName tmpname : decl) {
				IASTTranslationUnit locTu = translationUnit;
				if(!tuFileLoc.equals(tmpname.getFileLocation().getFileName())) {
					locTu = TranslationUnitHelper.loadTranslationUnit(tmpname.getFileLocation().getFileName(), false);
				}
				IASTName declName = DeclarationFinder.findDeclarationInTranslationUnit(locTu, tmpname);
				if(declName != null) {
					IASTNode methoddefinition = declName.getParent().getParent();
					if (methoddefinition instanceof IASTSimpleDeclaration || methoddefinition instanceof IASTFunctionDefinition) {
						context.setMethodDeclarationName(declName);
					}
				}
			}
		}
		if(name instanceof ICPPASTQualifiedName){
			ICPPASTQualifiedName qname =( ICPPASTQualifiedName )name;
			context.setMethodQName(qname);
		}
	}
	
	public static IASTCompoundStatement findCompoundStatementInAncestors(IASTNode node) {
		while(node != null){
			if (node instanceof IASTCompoundStatement) {
				return (IASTCompoundStatement) node;
			}
			node = node.getParent();
		}
		return null;
	}
	
	public static IASTCompositeTypeSpecifier findClassInAncestors(IASTNode node) {
		while(!(node instanceof IASTCompositeTypeSpecifier)){
			if(node instanceof IASTTranslationUnit) {
				return null;
			}
			node = node.getParent();
		}
		return (IASTCompositeTypeSpecifier) node;
	}

	public static IASTFunctionDefinition findFunctionDefinitionInAncestors(IASTNode node) {
		while(node != null){
			if (node instanceof IASTFunctionDefinition) {
				return (IASTFunctionDefinition) node;
			}
			node = node.getParent();
		}
		return null;
	}

	public static boolean isMethodDeclaration(IASTSimpleDeclaration simpleDeclaration) {
		if(simpleDeclaration == null) {
			return false;
		}
		return simpleDeclaration.getDeclarators().length == 1 && simpleDeclaration.getDeclarators()[0] instanceof ICPPASTFunctionDeclarator;
	}

	public static boolean isContainedInTemplateDeclaration(IASTNode node) {
		if(node == null) {
			return false;
		} else if (node instanceof ICPPASTTemplateDeclaration) {
			return true;
		}
		return isContainedInTemplateDeclaration(node.getParent());
	}
}
