/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *    Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.List;

import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.refactoring.NodeContainer.NameInformation;

/**
 * Handles the extraction of expression nodes, like return type determination.
 * 
 * @author Mirko Stocker
 * 
 */
public class ExtractExpression extends ExtractedFunctionConstructionHelper {
	
	final static char[] ZERO= {'0'};

	@Override
	public void constructMethodBody(IASTCompoundStatement compound,
			List<IASTNode> list, ASTRewrite rewrite, TextEditGroup group) {

		CPPASTReturnStatement statement = new CPPASTReturnStatement();
		IASTExpression nullReturnExp = new CPPASTLiteralExpression(IASTLiteralExpression.lk_integer_constant, ZERO); 
		statement.setReturnValue(nullReturnExp);
		ASTRewrite nestedRewrite = rewrite.insertBefore(compound, null, statement, group);
		
		nestedRewrite.replace(nullReturnExp, getExpression(list), group);
		
	}

	private IASTExpression getExpression(List<IASTNode> list) {
		if(list.size()> 1 ) {
			CPPASTBinaryExpression bExp = new CPPASTBinaryExpression();
			bExp.setParent(list.get(0).getParent());
			bExp.setOperand1((IASTExpression) list.get(0).copy());
			bExp.setOperator(((IASTBinaryExpression)list.get(1).getParent()).getOperator());
			bExp.setOperand2(getExpression(list.subList(1, list.size())));
			return bExp;
		}else {
			return (IASTExpression) list.get(0).copy();
		}
		
	}

	@Override
	public IASTDeclSpecifier determineReturnType(IASTNode extractedNode, NameInformation _) {
		IASTDeclSpecifier declSpecifier = null;
		
		if (extractedNode instanceof ICPPASTBinaryExpression) {
			declSpecifier = handleBinaryExpression((ICPPASTBinaryExpression) extractedNode);
		}
		
		if (extractedNode instanceof ICPPASTNewExpression) {
			declSpecifier = handleNewExpression((ICPPASTNewExpression) extractedNode);
		}

		if (extractedNode instanceof IASTFunctionCallExpression) {
			declSpecifier = handleFunctionCallExpression((IASTFunctionCallExpression) extractedNode);
		}
		
		if (extractedNode instanceof IASTLiteralExpression) {
		    declSpecifier = handleLiteralExpression((IASTLiteralExpression) extractedNode);
		}
		
		if(declSpecifier == null) {
			if(extractedNode instanceof IASTExpression) {
				IType type = ((IASTExpression)extractedNode).getExpressionType();
				if(type instanceof IBasicType) {
					return createSimpleDeclSpecifier(((IBasicType)type).getKind());
				}
			}
			return createSimpleDeclSpecifier(Kind.eVoid);
		}
		if(declSpecifier.isFrozen()) {
			return declSpecifier.copy();
		}else {
			return declSpecifier;
		}
	}

	private IASTDeclSpecifier handleLiteralExpression(IASTLiteralExpression extractedNode) {
        switch(extractedNode.getKind()){
          case IASTLiteralExpression.lk_char_constant:
              return createSimpleDeclSpecifier(Kind.eChar);
          case IASTLiteralExpression.lk_float_constant:
              return createSimpleDeclSpecifier(Kind.eFloat);
          case IASTLiteralExpression.lk_integer_constant:
              return createSimpleDeclSpecifier(Kind.eInt);
          case IASTLiteralExpression.lk_string_literal:
              return createSimpleDeclSpecifier(Kind.eWChar);
          case IASTLiteralExpression.lk_false: 
              //Like lk_true a boolean type
          case IASTLiteralExpression.lk_true:
              return createSimpleDeclSpecifier(Kind.eBoolean);
          default:
              return null;
          }
    }

    private IASTDeclSpecifier handleNewExpression(ICPPASTNewExpression expression) {
		return expression.getTypeId().getDeclSpecifier();
	}

	private IASTDeclSpecifier handleBinaryExpression(ICPPASTBinaryExpression node) {
		
		switch (node.getOperator()) {
		case IASTBinaryExpression.op_equals:
		case IASTBinaryExpression.op_notequals:
		case IASTBinaryExpression.op_logicalOr:
		case IASTBinaryExpression.op_logicalAnd:
		case IASTBinaryExpression.op_greaterEqual:
		case IASTBinaryExpression.op_greaterThan:
		case IASTBinaryExpression.op_lessEqual:
		case IASTBinaryExpression.op_lessThan:
		
			/* We assume that these operations evaluate to bool and don't 
			 * consider overriden operators from custom types for now.*/
			return createSimpleDeclSpecifier(Kind.eBoolean);
			
		case IASTBinaryExpression.op_plus:
		case IASTBinaryExpression.op_plusAssign:
		case IASTBinaryExpression.op_minus:
		case IASTBinaryExpression.op_minusAssign:
		case IASTBinaryExpression.op_multiply:
		case IASTBinaryExpression.op_multiplyAssign:
		case IASTBinaryExpression.op_divide:
		case IASTBinaryExpression.op_divideAssign:
		case IASTBinaryExpression.op_assign:

			
			return getTypeFromBinaryExp(node);
		}
		
		return null /* not yet handled */;
	}

	private IASTDeclSpecifier getTypeFromBinaryExp(ICPPASTBinaryExpression node) {
		if(node.getOperand1() instanceof ICPPASTBinaryExpression) {
			IASTDeclSpecifier ret = getTypeFromBinaryExp(((CPPASTBinaryExpression)node.getOperand1()));
			if(ret != null) return ret;
		}else {
			if(node.getOperand1() instanceof CPPASTIdExpression) {
				return getBinaryExpressionType((CPPASTIdExpression) node.getOperand1());
			}
		}

		if(node.getOperand2() instanceof ICPPASTBinaryExpression) {
			IASTDeclSpecifier ret = getTypeFromBinaryExp(((CPPASTBinaryExpression)node.getOperand2()));
			if(ret != null) return ret;
		}else {
			if(node.getOperand2() instanceof CPPASTIdExpression) {
				return getBinaryExpressionType((CPPASTIdExpression) node.getOperand2());
			}
		}
		return null;
	}

	private IASTDeclSpecifier getBinaryExpressionType(CPPASTIdExpression expression) {
		IType expressionType = ((IVariable) expression.getName().resolveBinding()).getType();
		if (expressionType instanceof ITypedef) {
			ITypedef typdef = (ITypedef) expressionType;
			IType expandedType = expression.getExpressionType();
			if(typdef.getType().isSameType(expandedType)) {
				return getDeclSpecifierForType(typdef);
			}else{
				return getDeclSpecifierForType(expandedType);
			}
			
		}
		return getDeclSpecifierForType(expressionType);

	}

	private IASTDeclSpecifier getDeclSpecifierForType(IType expressionType) {

		if (expressionType instanceof CPPBasicType) {
			
			CPPBasicType basicType = (CPPBasicType) expressionType;
			return createSimpleDeclSpecifier(basicType.getKind());
			
		} else if (expressionType instanceof ITypedef) {
			return getDeclSpecForType((ITypedef) expressionType);
		} else if (expressionType instanceof ICPPClassType) {
			return getDeclSpecForType((ICPPClassType)expressionType);
		}
		return null;
	}

	private IASTDeclSpecifier getDeclSpecForType(ICPPClassType expressionType) {
		IASTName name = null;

		char[][] qualifiedNameCharArray = CPPVisitor.getQualifiedNameCharArray(expressionType);
		name = new CPPASTQualifiedName();
		for (char[] cs : qualifiedNameCharArray) {
			((ICPPASTQualifiedName) name).addName(new CPPASTName(cs));
		}

		return new CPPASTNamedTypeSpecifier(name);
	}

	private CPPASTNamedTypeSpecifier getDeclSpecForType(ITypedef classType) {

		IASTName name = null;
		char[][] qualifiedNameCharArray = CPPVisitor.getQualifiedNameCharArray(classType);
		name = new CPPASTQualifiedName();
		for (char[] cs : qualifiedNameCharArray) {
			((ICPPASTQualifiedName) name).addName(new CPPASTName(cs));
		}

		return new CPPASTNamedTypeSpecifier(name);
	}

	private static IASTDeclSpecifier createSimpleDeclSpecifier(IBasicType.Kind type) {
		IASTSimpleDeclSpecifier declSpec = new CPPASTSimpleDeclSpecifier();
		declSpec.setType(type);
		return declSpec;
	}
	
	private static IASTName findCalledFunctionName(IASTFunctionCallExpression callExpression) {
		IASTExpression functionNameExpression = callExpression.getFunctionNameExpression();
		IASTName functionName = null;
		
		if(functionNameExpression instanceof CPPASTIdExpression) {
			CPPASTIdExpression idExpression = (CPPASTIdExpression) functionNameExpression;
			functionName = idExpression.getName();
		} else  if(functionNameExpression instanceof CPPASTFieldReference) {
			CPPASTFieldReference fieldReference = (CPPASTFieldReference) functionNameExpression;
			functionName = fieldReference.getFieldName();
		}		
		return functionName;
	}
	
	private static IASTDeclSpecifier handleFunctionCallExpression(IASTFunctionCallExpression callExpression) {
		IASTName functionName = findCalledFunctionName(callExpression);
		if(functionName != null) {
			IBinding binding = functionName.resolveBinding();
			if (binding instanceof CPPFunction) {
				CPPFunction function =  (CPPFunction) binding;
				if(function.getDefinition() != null) {
					IASTNode parent = function.getDefinition().getParent();
					if(parent instanceof CPPASTFunctionDefinition) {
						CPPASTFunctionDefinition definition = (CPPASTFunctionDefinition) parent;
						return definition.getDeclSpecifier();
					}
				} else if(hasDeclaration(function)) {
					IASTNode parent = function.getDeclarations()[0].getParent();
					if (parent instanceof CPPASTSimpleDeclaration) {
						CPPASTSimpleDeclaration declaration = (CPPASTSimpleDeclaration) parent;
						return declaration.getDeclSpecifier();
					}
				}
			}else if(binding instanceof ITypedef) {
				ITypedef typedef = (ITypedef) binding;
				return new CPPASTNamedTypeSpecifier(new CPPASTName(typedef.getNameCharArray()));
			}

		}			
		return null;
	}

	@Override
	protected boolean isReturnTypeAPointer(IASTNode node) {
		if(node instanceof ICPPASTNewExpression) {
			return true;
		} else if(!(node instanceof IASTFunctionCallExpression)) {
			return false;
		}

		IASTName functionName = findCalledFunctionName((IASTFunctionCallExpression) node);
		if(functionName != null) {
			IBinding binding = functionName.resolveBinding();
			if (binding instanceof CPPFunction) {
				CPPFunction function =  (CPPFunction) binding;
				if(function.getDefinition() != null) {
					IASTNode parent = function.getDefinition().getParent();
					if(parent instanceof CPPASTFunctionDefinition) {
						CPPASTFunctionDefinition definition = (CPPASTFunctionDefinition) parent;
						return definition.getDeclarator().getPointerOperators().length > 0;
					}
				} else if(hasDeclaration(function)) {
					IASTNode parent = function.getDeclarations()[0].getParent();
					if (parent instanceof CPPASTSimpleDeclaration) {
						CPPASTSimpleDeclaration declaration = (CPPASTSimpleDeclaration) parent;
						return declaration.getDeclarators().length > 0 && declaration.getDeclarators()[0].getPointerOperators().length > 0;
					}
				}
			}
		}
		return false;
	}

	private static boolean hasDeclaration(CPPFunction function) {
		return function != null && function.getDeclarations() != null && function.getDeclarations().length > 0;
	}
	
	@Override
	public IASTNode createReturnAssignment(IASTNode node, IASTExpressionStatement stmt, IASTExpression callExpression) {
		return callExpression;
	}
}
