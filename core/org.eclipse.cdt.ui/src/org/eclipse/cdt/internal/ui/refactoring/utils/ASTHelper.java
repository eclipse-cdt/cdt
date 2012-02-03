/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;

import org.eclipse.cdt.internal.ui.refactoring.extractfunction.TrailNodeEqualityChecker;

public class ASTHelper {
	private ASTHelper() {
	}

	static public IASTNode getDeclarationForNode(IASTNode tmpNode) {
		while (tmpNode != null && !(tmpNode instanceof IASTSimpleDeclaration) && !(tmpNode instanceof IASTParameterDeclaration)) {
			tmpNode = tmpNode.getParent();
		}
		return tmpNode;
	}

	static public IASTDeclarator getDeclaratorForNode(IASTNode aNode) {
		IASTNode tmpNode = getDeclarationForNode(aNode);

		IASTDeclarator declarator = null;
		if (tmpNode instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tmpNode;
			if (decl.getDeclarators().length > 0) {
				declarator = decl.getDeclarators()[0];
			}
		} else if (tmpNode instanceof IASTParameterDeclaration) {
			IASTParameterDeclaration decl = (IASTParameterDeclaration) tmpNode;
			declarator = decl.getDeclarator();
		}
		return declarator;
	}

	static public IASTDeclSpecifier getDeclarationSpecifier(IASTNode declaration) {
		if (declaration != null) {
			if (declaration instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
				return simpleDecl.getDeclSpecifier();
			} else if (declaration instanceof ICPPASTParameterDeclaration) {
				ICPPASTParameterDeclaration paramDecl = (ICPPASTParameterDeclaration) declaration;
				return paramDecl.getDeclSpecifier();
			}
		}
		return null;
	}

	public static boolean samePointers(IASTPointerOperator[] pointerOperators1,
			IASTPointerOperator[] pointerOperators2, TrailNodeEqualityChecker checker) {
		if (pointerOperators2.length == pointerOperators1.length) {
			for (int i = 0; i < pointerOperators2.length; i++) {
				IASTPointerOperator operator1 = pointerOperators1[i];
				IASTPointerOperator operator2 = pointerOperators2[i];
				if (!checker.isEquals(operator1, operator2)) {
					return false;
				}
			}
		} else {
			return false;
		}

		return true;
	}

	public static boolean isClassDeclarationName(IASTName astName) {
		if (astName == null)
			return false;
		IASTNode parent = astName.getParent();
		if (parent instanceof ICPPASTCompositeTypeSpecifier) {
			ICPPASTCompositeTypeSpecifier typeSpecifier = (ICPPASTCompositeTypeSpecifier) parent;
			return typeSpecifier.getKey() == ICPPASTCompositeTypeSpecifier.k_class;
		}
		return false;
	}

	public static ArrayList<ICPPASTNamespaceDefinition> getNamespaces(IASTNode node) {
		ArrayList<ICPPASTNamespaceDefinition> namespaces = new ArrayList<ICPPASTNamespaceDefinition>();
		for (IASTNode aktNode = node; aktNode != null; aktNode = aktNode.getParent()) {
			if (aktNode instanceof ICPPASTNamespaceDefinition) {
				namespaces.add(0, (ICPPASTNamespaceDefinition) aktNode);
			} else if (aktNode instanceof ICPPASTQualifiedName) {
				namespaces.addAll(getNamespaces((ICPPASTQualifiedName) aktNode));
			}
		}
		return namespaces;
	}
	
	public static ArrayList<ICPPASTNamespaceDefinition> getNamespaces(ICPPASTQualifiedName qualifiedName) {
		ArrayList<ICPPASTNamespaceDefinition> namespaces = new ArrayList<ICPPASTNamespaceDefinition>();
		for (IASTName aktQualifiedPartName : qualifiedName.getNames()) {
			IBinding binding = aktQualifiedPartName.resolveBinding();
			for (IASTName aktResolvedName : qualifiedName.getTranslationUnit().getDefinitionsInAST(binding)) {
				if (aktResolvedName.getParent() instanceof ICPPASTNamespaceDefinition) {
					namespaces.add((ICPPASTNamespaceDefinition) aktResolvedName.getParent());
					break;
				}
			}
		}
		return namespaces;
	}

	public static Collection<IASTDeclSpecifier> getCompositTypeSpecifiers(IASTNode baseNode) {
		final Collection<IASTDeclSpecifier> specifiers = new ArrayList<IASTDeclSpecifier>();
		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public int visit(IASTDeclSpecifier declSpec) {
				specifiers.add(declSpec);
				return super.visit(declSpec);
			}
		};
		visitor.shouldVisitDeclSpecifiers = true;
		baseNode.accept(visitor);
		return specifiers;
	}
	
	public static Collection<IASTPreprocessorStatement> getAllInFilePreprocessorStatements(
			IASTTranslationUnit unit, String aktFileName) {
		Collection<IASTPreprocessorStatement> statements = new ArrayList<IASTPreprocessorStatement>();
		for (IASTPreprocessorStatement aktStatement : unit.getAllPreprocessorStatements()) {
			if (aktStatement.getFileLocation() == null) {
				continue;
			} else if (aktStatement.getFileLocation().getFileName().equals(aktFileName)) {
				statements.add(aktStatement);
			}
		}
		return statements;
	}
	
	public static Collection<IASTDeclaration> getAllInFileDeclarations(IASTTranslationUnit unit, String aktFileName) {
		Collection<IASTDeclaration> decls = new ArrayList<IASTDeclaration>();
		for (IASTDeclaration aktDecl: unit.getDeclarations()) {
			if (aktDecl.getFileLocation() == null) {
				continue;
			} else if (aktDecl.getFileLocation().getFileName().equals(aktFileName)) {
				decls.add(aktDecl);
			}
		}
		return decls;
	}
	
	public static ICPPASTUsingDirective getActiveUsingDirecitveForNode(IASTNode node, IASTTranslationUnit unit) {
		ICPPASTUsingDirective activeDirective = null;
		for (IASTDeclaration aktDeclaration : getAllInFileDeclarations(unit, node.getFileLocation().getFileName())) {
			if (aktDeclaration.getFileLocation().getNodeOffset() >= node.getFileLocation().getNodeOffset()) {
				break;
			}
			if (aktDeclaration instanceof ICPPASTUsingDirective) {
				activeDirective = (ICPPASTUsingDirective) aktDeclaration;
			}
		} 
		return activeDirective;
	}

	public static Collection<ICPPASTUsingDeclaration> getUsingDeclarations(IASTTranslationUnit unit) {
		Collection<ICPPASTUsingDeclaration> usingDecls = new ArrayList<ICPPASTUsingDeclaration>();
		for (IASTDeclaration aktDecl : unit.getDeclarations()) {
			if (aktDecl instanceof ICPPASTUsingDeclaration) {
				usingDecls.add((ICPPASTUsingDeclaration) aktDecl);
			}
		}
		return usingDecls;
	}

	public static IASTCompositeTypeSpecifier getCompositeTypeSpecifierForName(IASTName name) {
		IBinding binding = name.resolveBinding();
		for (IASTName aktName : name.getTranslationUnit().getDefinitionsInAST(binding)) {
			if (aktName.getParent() instanceof IASTCompositeTypeSpecifier) {
				return (IASTCompositeTypeSpecifier) aktName.getParent();
			}
		}
		return null;
	}
	
	public static Collection<IASTFunctionDeclarator> getFunctionDeclaratorsForClass(IASTCompositeTypeSpecifier klass) {
		Collection<IASTFunctionDeclarator> declarators = new ArrayList<IASTFunctionDeclarator>();
		for (IASTDeclaration aktDeclaration : klass.getMembers()) {
			if (aktDeclaration instanceof IASTSimpleDeclaration) {
				for (IASTDeclarator aktDeclarator : ((IASTSimpleDeclaration) aktDeclaration).getDeclarators()) {
					if (aktDeclarator instanceof IASTFunctionDeclarator) {
						declarators.add((IASTFunctionDeclarator) aktDeclarator);
					}
				}
			}
		}
		return declarators;
	}

	public static Collection<IASTFunctionDefinition> getFunctionDefinitionsForClass(IASTCompositeTypeSpecifier klass) {
		Collection<IASTFunctionDefinition> definitions = new ArrayList<IASTFunctionDefinition>();
		for (IASTFunctionDeclarator aktDeclarator : getFunctionDeclaratorsForClass(klass)) {
			IBinding binding = aktDeclarator.getName().resolveBinding();
			for (IASTName aktName : aktDeclarator.getTranslationUnit().getDefinitionsInAST(binding)) {
				if (aktName.getParent().getParent().getParent() instanceof IASTFunctionDefinition) {
					definitions.add((IASTFunctionDefinition) aktName.getParent().getParent().getParent());
				}
			}
		}
		return definitions;
	}
}
