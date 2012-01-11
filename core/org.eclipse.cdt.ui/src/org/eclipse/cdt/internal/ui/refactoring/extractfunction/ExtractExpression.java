/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.dom.rewrite.DeclarationGenerator;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;

import org.eclipse.cdt.internal.ui.refactoring.NodeContainer.NameInformation;

/**
 * Handles the extraction of expression nodes, for example, return type determination.
 * 
 * @author Mirko Stocker
 */
public class ExtractExpression extends ExtractedFunctionConstructionHelper {
	final static char[] ZERO= { '0' };

	@Override
	public void constructMethodBody(IASTCompoundStatement compound, List<IASTNode> list,
			ASTRewrite rewrite, TextEditGroup group) {
		CPPASTReturnStatement statement = new CPPASTReturnStatement();
		IASTExpression nullReturnExp =
				new CPPASTLiteralExpression(IASTLiteralExpression.lk_integer_constant, ZERO); 
		statement.setReturnValue(nullReturnExp);
		ASTRewrite nestedRewrite = rewrite.insertBefore(compound, null, statement, group);
		
		nestedRewrite.replace(nullReturnExp, getExpression(list), group);
	}

	private IASTExpression getExpression(List<IASTNode> list) {
		if (list.size() > 1) {
			CPPASTBinaryExpression bExp = new CPPASTBinaryExpression();
			bExp.setParent(list.get(0).getParent());
			bExp.setOperand1((IASTExpression) list.get(0).copy(CopyStyle.withLocations));
			bExp.setOperator(((IASTBinaryExpression) list.get(1).getParent()).getOperator());
			bExp.setOperand2(getExpression(list.subList(1, list.size())));
			return bExp;
		} else {
			return (IASTExpression) list.get(0).copy(CopyStyle.withLocations);
		}
	}

	@Override
	public IASTDeclSpecifier determineReturnType(IASTNode extractedNode, NameInformation _) {
		List<ITypedef> typedefs = getTypedefs(extractedNode);
		if (extractedNode instanceof IASTExpression) {
			IASTExpression exp = (IASTExpression) extractedNode;
			INodeFactory factory = extractedNode.getTranslationUnit().getASTNodeFactory();
			DeclarationGenerator generator = DeclarationGenerator.create(factory);
			IType expressionType = exp.getExpressionType();
			for (ITypedef typedef : typedefs) {
				if (typedef.getType().isSameType(expressionType)) {
					return generator.createDeclSpecFromType(typedef);
				}
			}
			return generator.createDeclSpecFromType(expressionType);
		} else { // Fallback
			return createSimpleDeclSpecifier(Kind.eVoid);
		}
	}

	private List<ITypedef> getTypedefs(IASTNode extractedNode) {
		final ArrayList<ITypedef> typeDefs = new ArrayList<ITypedef>();
		extractedNode.accept(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}
			
			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof IASTIdExpression) {
					IASTIdExpression id = (IASTIdExpression) expression;
					IBinding binding = id.getName().resolveBinding();
					IType expressionType = null;
					if (binding instanceof IVariable) {
						expressionType = ((IVariable) binding).getType();
					}
					if (binding instanceof IType) {
						expressionType = (IType) binding;
					}
					if (expressionType != null && expressionType instanceof ITypedef) {
						ITypedef typdef = (ITypedef) expressionType;
						typeDefs.add(typdef);
					}
				}
				return PROCESS_CONTINUE;
			}
		});
		return typeDefs;
	}

	private static IASTDeclSpecifier createSimpleDeclSpecifier(IBasicType.Kind type) {
		IASTSimpleDeclSpecifier declSpec = new CPPASTSimpleDeclSpecifier();
		declSpec.setType(type);
		return declSpec;
	}
	
	private static IASTName findCalledFunctionName(IASTFunctionCallExpression callExpression) {
		IASTExpression functionNameExpression = callExpression.getFunctionNameExpression();
		IASTName functionName = null;
		
		if (functionNameExpression instanceof CPPASTIdExpression) {
			CPPASTIdExpression idExpression = (CPPASTIdExpression) functionNameExpression;
			functionName = idExpression.getName();
		} else if (functionNameExpression instanceof CPPASTFieldReference) {
			CPPASTFieldReference fieldReference = (CPPASTFieldReference) functionNameExpression;
			functionName = fieldReference.getFieldName();
		}		
		return functionName;
	}

	@Override
	protected boolean hasPointerReturnType(IASTNode node) {
		if (node instanceof ICPPASTNewExpression) {
			return true;
		} else if (!(node instanceof IASTFunctionCallExpression)) {
			return false;
		}

		IASTName functionName = findCalledFunctionName((IASTFunctionCallExpression) node);
		if (functionName != null) {
			IBinding binding = functionName.resolveBinding();
			if (binding instanceof CPPFunction) {
				CPPFunction function =  (CPPFunction) binding;
				if (function.getDefinition() != null) {
					IASTNode parent = function.getDefinition().getParent();
					if (parent instanceof CPPASTFunctionDefinition) {
						CPPASTFunctionDefinition definition = (CPPASTFunctionDefinition) parent;
						return definition.getDeclarator().getPointerOperators().length > 0;
					}
				} else if (hasDeclaration(function)) {
					IASTNode parent = function.getDeclarations()[0].getParent();
					if (parent instanceof CPPASTSimpleDeclaration) {
						CPPASTSimpleDeclaration declaration = (CPPASTSimpleDeclaration) parent;
						return declaration.getDeclarators().length > 0 &&
								declaration.getDeclarators()[0].getPointerOperators().length > 0;
					}
				}
			}
		}
		return false;
	}

	private static boolean hasDeclaration(CPPFunction function) {
		return function != null && function.getDeclarations() != null &&
				function.getDeclarations().length > 0;
	}
	
	@Override
	public IASTNode createReturnAssignment(IASTNode node, IASTExpressionStatement stmt,
			IASTExpression callExpression) {
		return callExpression;
	}
}
