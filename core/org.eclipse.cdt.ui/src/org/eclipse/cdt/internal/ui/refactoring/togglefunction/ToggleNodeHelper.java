/*******************************************************************************
 * Copyright (c) 2011, 2015 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Martin Schwab & Thomas Kallenberg - initial API and implementation
 *     Marc-Andre Laperle (Ericsson)
 *     Thomas Corbat (IFS)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionWithTryBlock;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite.CommentPosition;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionWithTryBlock;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;

public class ToggleNodeHelper extends NodeHelper {
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private static void removeParameterInitializations(IASTFunctionDeclarator funcDecl) {
		for (IASTNode child : funcDecl.getChildren()) {
			if (child instanceof IASTParameterDeclaration) {
				IASTParameterDeclaration parameter = (IASTParameterDeclaration) child;
				parameter.getDeclarator().setInitializer(null);
			}
		}
	}

	private static List<ICPPASTConstructorChainInitializer> getInitializerList(IASTFunctionDefinition definition) {
		ArrayList<ICPPASTConstructorChainInitializer> initalizers = 
				new ArrayList<ICPPASTConstructorChainInitializer>();
	
		for (IASTNode node : definition.getChildren()) {
			if (node instanceof ICPPASTConstructorChainInitializer) {
				initalizers.add(((ICPPASTConstructorChainInitializer) node).copy(CopyStyle.withLocations));
			}
		}
		return initalizers;
	}

	static IASTSimpleDeclaration createDeclarationFromDefinition(IASTFunctionDefinition oldDefinition) {
		IASTDeclarator newDeclarator = oldDefinition.getDeclarator().copy(CopyStyle.withLocations);
		IASTDeclSpecifier newDeclSpec = oldDefinition.getDeclSpecifier().copy(CopyStyle.withLocations);
		IASTSimpleDeclaration newDeclaration = new CPPASTSimpleDeclaration(newDeclSpec);
		newDeclaration.addDeclarator(newDeclarator);
		return newDeclaration;
	}

	static ICPPASTFunctionDefinition createFunctionSignatureWithEmptyBody(
			IASTDeclSpecifier newDeclSpec, IASTFunctionDeclarator newFuncDecl, 
			IASTFunctionDefinition oldDefinition) {
		ICPPASTFunctionDefinition newFunc = null;
		newFuncDecl = adjustParamNames(newFuncDecl, oldDefinition);
		if (oldDefinition instanceof ICPPASTFunctionWithTryBlock) {
			newFunc = new CPPASTFunctionWithTryBlock(newDeclSpec, newFuncDecl, 
					new CPPASTCompoundStatement());
		} else {
			newFunc = new CPPASTFunctionDefinition(newDeclSpec, newFuncDecl, 
					new CPPASTCompoundStatement());
		}
		copyInitializerList(newFunc, oldDefinition);
		return newFunc;
	}
	
	private static IASTFunctionDeclarator adjustParamNames(IASTFunctionDeclarator newFuncDecl,
			IASTFunctionDefinition oldDefinition) {
		if (oldDefinition.getDeclarator() instanceof IASTStandardFunctionDeclarator) {
			IASTStandardFunctionDeclarator oldStdDec = (IASTStandardFunctionDeclarator) oldDefinition.getDeclarator();
			IASTParameterDeclaration[] definitionParams = oldStdDec.getParameters();
			IASTParameterDeclaration[] declarationParams = ((IASTStandardFunctionDeclarator)newFuncDecl).getParameters();
			for(int i = 0; i < declarationParams.length; ++i) {
				declarationParams[i].getDeclarator().setName(definitionParams[i].getDeclarator().getName().copy(CopyStyle.withLocations));
			}
		}
		return newFuncDecl;
	}

	private static void copyInitializerList(ICPPASTFunctionDefinition newFunc, IASTFunctionDefinition oldFunc) {
		for (ICPPASTConstructorChainInitializer initializer : getInitializerList(oldFunc)) {
			initializer.setParent(newFunc);
			newFunc.addMemberInitializer(initializer);
		}
	}

	static IASTFunctionDefinition getQualifiedNameDefinition(IASTFunctionDefinition oldDefinition, 
			IASTTranslationUnit definitionUnit, IASTNode nameSpace) {
		
		ICPPASTDeclSpecifier newDeclSpec = 
				(ICPPASTDeclSpecifier) oldDefinition.getDeclSpecifier().copy(CopyStyle.withLocations);
		
		newDeclSpec.setVirtual(false);
		newDeclSpec.setInline(true);
		newDeclSpec.setStorageClass(IASTDeclSpecifier.sc_unspecified);
		
		IASTFunctionDeclarator newDeclarator = oldDefinition.getDeclarator().copy(CopyStyle.withLocations);
		newDeclarator.setName(getQualifiedName(oldDefinition.getDeclarator(), nameSpace));
		removeParameterInitializations(newDeclarator);
		
		ICPPASTFunctionDefinition newFunction = 
				createFunctionSignatureWithEmptyBody(newDeclSpec, newDeclarator, oldDefinition);
	
		return newFunction;
	}

	public static ICPPASTTemplateDeclaration getTemplateDeclaration(
			IASTFunctionDefinition oldFunction, IASTFunctionDefinition newFunction) {
		ArrayList<ICPPASTTemplateDeclaration> templateDeclarations = getAllTemplateDeclaration(oldFunction);
		return addTemplateDeclarationsInOrder(templateDeclarations, newFunction);
	}

	private static ICPPASTTemplateDeclaration addTemplateDeclarationsInOrder(
			ArrayList<ICPPASTTemplateDeclaration> templDecs, IASTFunctionDefinition newFunction) {
		ListIterator<ICPPASTTemplateDeclaration> iter1 = templDecs.listIterator();
		ICPPASTTemplateDeclaration child = null;
		while (iter1.hasNext()) {
			child = iter1.next();
			child.setDeclaration(newFunction);
			ListIterator<ICPPASTTemplateDeclaration> iter2 = iter1;
			if (iter2.hasNext()) {
				ICPPASTTemplateDeclaration parent = iter2.next();
				child.setParent(parent);
				parent.setDeclaration(child);
				child = parent;
			}
		}
		return child;
	}

	private static ArrayList<ICPPASTTemplateDeclaration> getAllTemplateDeclaration(IASTNode node) {
		ArrayList<ICPPASTTemplateDeclaration> templdecs = new ArrayList<ICPPASTTemplateDeclaration>();
		while (node.getParent() != null) {
			node = node.getParent();
			if (node instanceof ICPPASTTemplateDeclaration) {
				templdecs.add((ICPPASTTemplateDeclaration) node.copy(CopyStyle.withLocations));
			}
		}
		return templdecs;
	}

	static IASTFunctionDefinition createInClassDefinition(IASTFunctionDeclarator dec, 
			IASTFunctionDefinition def, IASTTranslationUnit insertionAst) {
		IASTFunctionDeclarator declarator = dec.copy(CopyStyle.withLocations);
		IASTDeclSpecifier declSpec =
				def.getDeclSpecifier().copy(CopyStyle.withLocations);
		declSpec.setInline(false);
		if (declSpec instanceof ICPPASTDeclSpecifier && ToggleNodeHelper.isVirtual(dec)) {
			((ICPPASTDeclSpecifier) declSpec).setVirtual(true);
		}
		declSpec.setStorageClass(getStorageClass(dec));
		
		return createFunctionSignatureWithEmptyBody(declSpec, declarator, def);
	}

	static boolean isVirtual(IASTFunctionDeclarator fdec) {
		if (fdec.getParent() instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration dec = (IASTSimpleDeclaration) fdec.getParent();
			return ((ICPPASTDeclSpecifier) dec.getDeclSpecifier()).isVirtual();
		}
		return false;
	}
	
	static int getStorageClass(IASTFunctionDeclarator fdec) {
		if (fdec.getParent() instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration dec = (IASTSimpleDeclaration) fdec.getParent();
			return dec.getDeclSpecifier().getStorageClass();
		}
		return -1;
	}

	static IASTNode getParentRemovePoint(IASTFunctionDefinition definition) {
		IASTNode toremove = definition;
		while (toremove.getParent() != null &&
				toremove.getParent() instanceof ICPPASTTemplateDeclaration) {
			toremove = toremove.getParent();
		}
		return toremove;
	}

	/**
	 * @param declarator the declarator from which the full qualified namespace should be fetched
	 * @param limiter set a limiter in the class hierarchy where the lookup will stop
	 * @return
	 */
	static ICPPASTQualifiedName getQualifiedName(IASTFunctionDeclarator declarator, IASTNode limiter) {
		Stack<IASTNode> nodes = getQualifiedNames(declarator, limiter, declarator);
		CPPASTQualifiedName qName = reAssembleQualifiedName(nodes);
		qName.addName(declarator.getName().copy(CopyStyle.withLocations));
		return qName;
	}

	private static CPPASTQualifiedName reAssembleQualifiedName(Stack<IASTNode> nodes) {
		CPPASTQualifiedName qName = new CPPASTQualifiedName();
		while (!nodes.isEmpty()) {
			IASTNode nnode = nodes.pop();
			if (nnode instanceof IASTCompositeTypeSpecifier) {
				qName.addName(((IASTCompositeTypeSpecifier) nnode).getName());
			} else if (nnode instanceof ICPPASTNamespaceDefinition) {
				qName.addName(((ICPPASTNamespaceDefinition) nnode).getName());
			} else if (nnode instanceof ICPPASTTemplateId) {
				qName.addName((ICPPASTTemplateId) nnode);
			}
		}
		return qName;
	}

	private static Stack<IASTNode> getQualifiedNames(IASTFunctionDeclarator declarator,
			IASTNode limiter, IASTNode node) {
		IASTName lastName = declarator.getName();
		Stack<IASTNode> nodes = new Stack<IASTNode>();
		while (node.getParent() != null && node.getParent() != limiter) {
			node = node.getParent();
			if (node instanceof IASTCompositeTypeSpecifier) {
				nodes.push(((IASTCompositeTypeSpecifier) node).copy(CopyStyle.withLocations));
				lastName = ((IASTCompositeTypeSpecifier) node).getName();
			} else if (node instanceof ICPPASTNamespaceDefinition) {
				nodes.push(((ICPPASTNamespaceDefinition) node).copy(CopyStyle.withLocations));
				lastName = ((ICPPASTNamespaceDefinition) node).getName();
			} else if (shouldAddTemplateBrackets(node)) {
				if (!nodes.isEmpty())
					nodes.pop();
				ICPPASTTemplateId templateID = ToggleNodeHelper.getTemplateParameter(node, lastName);
				nodes.add(templateID);
			} 
		}
		return nodes;
	}

	private static boolean shouldAddTemplateBrackets(IASTNode node) {
		return node instanceof ICPPASTTemplateDeclaration
				&& !(((ICPPASTTemplateDeclaration) node).getDeclaration() 
						instanceof CPPASTFunctionDefinition);
	}

	private static ICPPASTTemplateId getTemplateParameter(IASTNode node, IASTName name) {
		ICPPASTTemplateId templateID = new CPPASTTemplateId();
		templateID.setTemplateName(name.copy(CopyStyle.withLocations));
		for (IASTNode child : node.getChildren()) {
			if (child instanceof ICPPASTSimpleTypeTemplateParameter) {
				ICPPASTSimpleTypeTemplateParameter tempChild = (ICPPASTSimpleTypeTemplateParameter) child;
	
				CPPASTNamedTypeSpecifier namedTypeSpecifier = new CPPASTNamedTypeSpecifier();
				namedTypeSpecifier.setName(tempChild.getName().copy(CopyStyle.withLocations));
				
				CPPASTTypeId id = new CPPASTTypeId();
				id.setDeclSpecifier(namedTypeSpecifier);
				templateID.addTemplateArgument(id);
			}
		}
		return templateID;
	}

	public static String getFilenameWithoutExtension(String filename) {
		int indexP = filename.lastIndexOf('.');
		int indexS = filename.lastIndexOf('/');
		indexS++;
		return filename.substring(indexS, indexP);
	}

	/**
	 * Will extract the innermost ICPPASTFunctionDefinition out of a template declaration.
	 * 
	 * template<typename T>				// <-- input this node
	 * template<typename U>
	 * void function(T t, U u) { ... }  // <-- will find this node here 
	 * 
	 * @param declaration the template declaration that should be searched for the function definition.
	 * @return null if a declaration is found instead of a definition.
	 */
	public static ICPPASTFunctionDefinition getFunctionDefinition(IASTNode declaration) {
		IASTNode node = declaration;
		while (node != null) {
			if (node instanceof ICPPASTTemplateDeclaration) {
				ICPPASTTemplateDeclaration templdec = (ICPPASTTemplateDeclaration) node;
				node = templdec.getDeclaration();
				continue;
			}
			if (node instanceof ICPPASTFunctionDefinition) {
				return (ICPPASTFunctionDefinition) node;
			} else {
				return null;
			}
		}
		return null;
	}
	
	/**
	 * Gets comments inside the body of a function.
	 * @return The body as a string and all the catch handlers
	 */
	public static String getBody(IASTFunctionDefinition oldDefinition, IASTTranslationUnit ast,
			ModificationCollector modifications) {
		return getBodyOnly(oldDefinition, ast, modifications)
				+ getCatchHandlers(oldDefinition, ast, modifications);
	}

	private static String getBodyOnly(IASTFunctionDefinition oldDefinition, IASTTranslationUnit ast,
			ModificationCollector modifications) {
		String leadingComments = getCommentsAsString(getLeadingCommentsFromNode(oldDefinition.getBody(),
				ast, modifications));
		String trailingComments = getCommentsAsString(getTrailingComments(oldDefinition.getBody(),
				ast, modifications));
		return leadingComments + oldDefinition.getBody().getRawSignature() + trailingComments;
	}

	private static String getCatchHandlers(IASTFunctionDefinition oldDefinition, IASTTranslationUnit ast,
			ModificationCollector modifications) {
		if (oldDefinition instanceof ICPPASTFunctionWithTryBlock) {
			ICPPASTCatchHandler[] oldCatches =
					((ICPPASTFunctionWithTryBlock) oldDefinition).getCatchHandlers();
			String allCatchHandlers = ""; //$NON-NLS-1$
			for (int i = 0; i < oldCatches.length; i++) {
				String lead = getCommentsAsString(getLeadingCommentsFromNode(oldCatches[i], ast,
						modifications));
				String trail = getCommentsAsString(getTrailingComments(oldCatches[i], ast, modifications));
				allCatchHandlers += lead + oldCatches[i].getRawSignature() + trail;
			}
			return allCatchHandlers;
		}
		return ""; //$NON-NLS-1$
	}

	private static List<IASTComment> getLeadingCommentsFromNode(IASTNode existingNode,
			IASTTranslationUnit ast, ModificationCollector modifications) {
		ASTRewrite rw = modifications.rewriterForTranslationUnit(ast);
		return rw.getComments(existingNode, CommentPosition.leading);
	}

	private static List<IASTComment> getTrailingComments(IASTNode existingNode,
			IASTTranslationUnit ast, ModificationCollector modifications) {
		ASTRewrite rw = modifications.rewriterForTranslationUnit(ast);
		return rw.getComments(existingNode, CommentPosition.trailing);
	}

	public static IASTNode getParentTemplateDeclaration(IASTNode def) {
		if (def == null)
			return null;
		IASTNode lastSeen = def;
		IASTNode node = def.getParent();
		while (node != null) {
			if (node instanceof ICPPASTTemplateDeclaration || node instanceof IASTSimpleDeclaration) {
				lastSeen = node;
				node = node.getParent();
				continue;
			}
			return lastSeen;
		}
		return lastSeen;
	}
	
	private static String getCommentsAsString(List<IASTComment> commentList) {
		String comments = EMPTY_STRING;
		for (IASTComment c : commentList) {
			comments += c.getRawSignature() + System.getProperty("line.separator"); //$NON-NLS-1$
		}
		return comments;
	}

	/**
	 * Returns all namespace definitions surrounding <code>node</code>, ordered from outer to inner.
	 * @param node to collect the namespaces for.
	 * @return List of the surrounding namespaces.
	 */
	public static List<ICPPASTNamespaceDefinition> findSurroundingNamespaces(IASTNode node) {
		ArrayList<ICPPASTNamespaceDefinition> namespaces = new ArrayList<>();
		ICPPASTNamespaceDefinition currentNamespace = CPPVisitor.findAncestorWithType(node,
				ICPPASTNamespaceDefinition.class);
		while (currentNamespace != null) {
			namespaces.add(0, currentNamespace);
			currentNamespace = CPPVisitor.findAncestorWithType(currentNamespace.getParent(),
					ICPPASTNamespaceDefinition.class);
		}
		return namespaces;
	}
}
