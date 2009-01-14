/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
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

import org.eclipse.cdt.core.dom.ast.DOMException;
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
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNamespace;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTypedef;

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
			return createSimpleDeclSpecifier(IASTSimpleDeclSpecifier.t_void);
		}
		
		return declSpecifier.copy();
	}

	private IASTDeclSpecifier handleLiteralExpression(IASTLiteralExpression extractedNode) {
        switch(extractedNode.getKind()){
          case IASTLiteralExpression.lk_char_constant:
              return createSimpleDeclSpecifier(IASTSimpleDeclSpecifier.t_char);
          case IASTLiteralExpression.lk_float_constant:
              return createSimpleDeclSpecifier(IASTSimpleDeclSpecifier.t_float);
          case IASTLiteralExpression.lk_integer_constant:
              return createSimpleDeclSpecifier(IASTSimpleDeclSpecifier.t_int);
          case IASTLiteralExpression.lk_string_literal:
              return createSimpleDeclSpecifier(ICPPASTSimpleDeclSpecifier.t_wchar_t);
          case IASTLiteralExpression.lk_false: 
              //Like lk_true a boolean type
          case IASTLiteralExpression.lk_true:
              return createSimpleDeclSpecifier(ICPPASTSimpleDeclSpecifier.t_bool);
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
			return createSimpleDeclSpecifier(ICPPASTSimpleDeclSpecifier.t_bool);
			
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
				return getBinaryExpressionType(((CPPASTIdExpression) node.getOperand1()).getExpressionType());
			}
		}

		if(node.getOperand2() instanceof ICPPASTBinaryExpression) {
			IASTDeclSpecifier ret = getTypeFromBinaryExp(((CPPASTBinaryExpression)node.getOperand2()));
			if(ret != null) return ret;
		}else {
			if(node.getOperand2() instanceof CPPASTIdExpression) {
				return getBinaryExpressionType(((CPPASTIdExpression) node.getOperand2()).getExpressionType());
			}
		}
		return null;
	}

	private IASTDeclSpecifier getBinaryExpressionType(IType expressionType) {		
		if (expressionType instanceof CPPBasicType) {
			
			CPPBasicType basicType = (CPPBasicType) expressionType;
			return createSimpleDeclSpecifier(basicType.getType());
			
		} else if (expressionType instanceof CPPTypedef) {
			
			return getDeclSpecForType(((CPPTypedef)expressionType));
			
		} else if (expressionType instanceof CPPClassType) {

			return getDeclSpecForType((CPPClassType)expressionType);
		}
		return null;
	}

	private CPPASTNamedTypeSpecifier getDeclSpecForType(CPPClassType classType) {
		
		IASTName name = null;
		try {
			IBinding bind = classType.getOwner();
			if (bind instanceof CPPNamespace) {
				ICPPASTQualifiedName qname = getQname(classType, bind);
				qname.addName((IASTName) classType.getDefinition().copy());
				name = qname;
			}else {
				name = (IASTName) classType.getDefinition();
			}
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			name= new CPPASTName(classType.getNameCharArray());
		}
		
		return new CPPASTNamedTypeSpecifier(name.copy());
	}

	private ICPPASTQualifiedName getQname(IBinding classType, IBinding bind) {
		CPPNamespace namespace = (CPPNamespace) bind;
		char[][] names = namespace.getFullyQualifiedNameCharArray();
		CPPASTQualifiedName qname = new CPPASTQualifiedName();
		for (char[] string : names) {
			qname.addName(new CPPASTName(string));
		}
		return qname;
	}

	private IASTDeclSpecifier getDeclSpecForType(CPPTypedef typedef) {
		IASTName name = null;
		try {
			IBinding bind = typedef.getOwner();
			if (bind instanceof CPPNamespace) {
				ICPPASTQualifiedName qname = getQname(typedef, bind);
				qname.addName((IASTName) typedef.getDefinition().copy());
				name = qname;
			}else {
				name = (IASTName) typedef.getDefinition();
			}
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			name= new CPPASTName(typedef.getNameCharArray());
		}
		return new CPPASTNamedTypeSpecifier(name.copy());
	}
	
	private static IASTDeclSpecifier createSimpleDeclSpecifier(int type) {
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
			}else if(binding instanceof CPPTypedef) {
				CPPTypedef typedef = (CPPTypedef) binding;
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
