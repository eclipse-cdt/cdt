/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia, Tomasz Wesolowksi
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *    Tomasz Wesolowski - extension
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.rewrite.DeclarationGenerator;

/**
 * Useful functions for doing code analysis on c/c++ AST
 */
public final class CxxAstUtils {
	private static CxxAstUtils instance;

	private CxxAstUtils() {
		// private constructor
	}

	public synchronized static CxxAstUtils getInstance() {
		if (instance == null)
			instance = new CxxAstUtils();
		return instance;
	}

	public IType unwindTypedef(IType type) {
		if (!(type instanceof IBinding))
			return type;
		IBinding typeName = (IBinding) type;
		// unwind typedef chain
		try {
			while (typeName instanceof ITypedef) {
				IType t = ((ITypedef) typeName).getType();
				if (t instanceof IBinding)
					typeName = (IBinding) t;
				else
					return t;
			}
		} catch (Exception e) { // in CDT 6.0 getType throws DOMException
			Activator.log(e);
		}
		return (IType) typeName;
	}

	public boolean isInMacro(IASTNode node) {
		IASTNodeSelector nodeSelector = node.getTranslationUnit()
				.getNodeSelector(node.getTranslationUnit().getFilePath());
		IASTFileLocation fileLocation = node.getFileLocation();
	
		IASTPreprocessorMacroExpansion macro = nodeSelector
				.findEnclosingMacroExpansion(fileLocation.getNodeOffset(),
						fileLocation.getNodeLength());
		return macro != null;
	}

	public IASTFunctionDefinition getEnclosingFunction(IASTNode node) {
		while (node != null && !(node instanceof IASTFunctionDefinition)) {
			node = node.getParent();
		}
		return (IASTFunctionDefinition) node;
	}

	public IASTCompositeTypeSpecifier getEnclosingCompositeTypeSpecifier(IASTNode node) {
		while (node != null && !(node instanceof IASTCompositeTypeSpecifier)) {
			node = node.getParent();
		}
		return (IASTCompositeTypeSpecifier) node;
	}

	public IASTStatement getEnclosingStatement(IASTNode node) {
		while (node != null && !(node instanceof IASTStatement)) {
			node = node.getParent();
		}
		return (IASTStatement) node;
	}

	/**
	 * @param astName a name for the declaration
	 * @param factory the factory 
	 * @return
	 */
	public IASTDeclaration createDeclaration(IASTName astName, INodeFactory factory) {
		IASTSimpleDeclaration declaration = factory.newSimpleDeclaration(null);
		
		IASTDeclarator declarator = factory.newDeclarator(astName.copy());
		IASTDeclSpecifier declspec = factory.newSimpleDeclSpecifier();
		((IASTSimpleDeclSpecifier)declspec).setType(Kind.eVoid);
	
		if (astName.getParent() instanceof IASTIdExpression
				&& astName.getParent().getParent() instanceof IASTBinaryExpression
				&& ((IASTBinaryExpression) astName.getParent().getParent())
						.getOperator() == IASTBinaryExpression.op_assign
				&& astName.getParent().getParent().getParent() instanceof IASTExpressionStatement) {
			IASTNode binaryExpr = astName.getParent().getParent();
			IASTExpression tgt = null;
			for (IASTNode node : binaryExpr.getChildren()) {
				if (node != astName.getParent()) {
					// use this expression as type source
					tgt = (IASTExpression) node;
					break;
				}
			}
			if (tgt != null) {
				DeclarationGenerator generator = DeclarationGenerator.create(factory);
				IType type = tgt.getExpressionType();
				declarator = generator.createDeclaratorFromType(type, astName.toCharArray());
				declspec = generator.createDeclSpecFromType(type);
			}
		}
		
		declaration.setDeclSpecifier(declspec);
		declaration.addDeclarator(declarator);
		return declaration;
	}
}
