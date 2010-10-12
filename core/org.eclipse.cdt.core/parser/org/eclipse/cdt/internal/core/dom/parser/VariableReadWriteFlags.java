/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;

/**
 * Helper class to determine whether a variable is accessed for reading and/or writing.
 * The algorithm works starting from the variable and looking upwards what's being done
 * with the variable. C- and C++ specific things are handled in sub-classes.
 */
public abstract class VariableReadWriteFlags {
	protected static final int READ = PDOMName.READ_ACCESS;
	protected static final int WRITE = PDOMName.WRITE_ACCESS;
	
	protected VariableReadWriteFlags() {
	}

	protected int rwAnyNode(IASTNode node, int indirection) {
		final IASTNode parent= node.getParent();
		if (parent instanceof IASTExpression) {
			return rwInExpression(node, (IASTExpression) parent, indirection);
		}
		else if (parent instanceof IASTStatement) {
			return rwInStatement(node, (IASTStatement) parent, indirection);
		}
		else if (parent instanceof IASTEqualsInitializer) {
			return rwInInitializerExpression(indirection, parent);
		}
		else if (parent instanceof IASTArrayModifier) {
			return READ;	// dimension
		}
		return READ | WRITE;	// fallback
	}

	protected int rwInInitializerExpression(int indirection, IASTNode parent) {
		IASTNode grand= parent.getParent();
		if (grand instanceof IASTDeclarator) {
			IBinding binding= ((IASTDeclarator) grand).getName().getBinding();
			if (binding instanceof IVariable) {
				return rwAssignmentToType(((IVariable) binding).getType(), indirection);
			}
		}
		return READ | WRITE;  // fallback
	}

	protected int rwInExpression(IASTNode node, IASTExpression expr, int indirection) {
		if (expr instanceof IASTBinaryExpression) {
			return rwInBinaryExpression(node, (IASTBinaryExpression) expr, indirection);			
		}
		if (expr instanceof IASTCastExpression) { // must be ahead of unary
			return rwAnyNode(expr, indirection);
		}
		if (expr instanceof IASTUnaryExpression) {
			return rwInUnaryExpression(node, (IASTUnaryExpression) expr, indirection);			
		}
		if (expr instanceof IASTArraySubscriptExpression) {
			if (indirection > 0 && node.getPropertyInParent() == IASTArraySubscriptExpression.ARRAY) {
				return rwAnyNode(expr, indirection-1);
			}
			return READ;
		}
		if (expr instanceof IASTConditionalExpression) {
			if (node.getPropertyInParent() == IASTConditionalExpression.LOGICAL_CONDITION) {
				return READ;
			}
			return rwAnyNode(expr, indirection);
		}
		if (expr instanceof IASTExpressionList) {
			final IASTExpressionList exprList = (IASTExpressionList)expr;
			final IASTNode grand= expr.getParent();
			if (grand instanceof IASTFunctionCallExpression && expr.getPropertyInParent() == IASTFunctionCallExpression.ARGUMENT) {
				final IASTFunctionCallExpression funcCall = (IASTFunctionCallExpression) grand;
				return rwArgumentForFunctionCall(node, exprList, funcCall, indirection);
			}
			// only the first expression is passed on.
			final IASTExpression[] expressions = exprList.getExpressions();
			if (expressions.length > 0 && expressions[0] == node) {
				return rwAnyNode(expr, indirection);
			}
			return 0;
		}
		if (expr instanceof IASTFieldReference) {
			if (node.getPropertyInParent() == IASTFieldReference.FIELD_NAME) {
				return rwAnyNode(expr, indirection);
			}
			return READ;
		}
		if (expr instanceof IASTFunctionCallExpression) {
			if (node.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME) {
				return READ;
			}
			return rwArgumentForFunctionCall((IASTFunctionCallExpression) expr, 0, indirection);
		}
		if (expr instanceof IASTIdExpression) {
			return rwAnyNode(expr, indirection);
		}
		if (expr instanceof IASTProblemExpression) {
			return READ | WRITE;			
		}
		if (expr instanceof IASTTypeIdExpression) {
			return 0;
		}

		return READ | WRITE; // fall back
	}

	protected int rwArgumentForFunctionCall(IASTNode node, final IASTExpressionList exprList,
			final IASTFunctionCallExpression funcCall, int indirection) {
		final IASTExpression[] expressions = exprList.getExpressions();
		for (int i = 0; i < expressions.length; i++) {
			if (expressions[i] == node) {
				return rwArgumentForFunctionCall(funcCall, i, indirection);
			}
		}
		return READ | WRITE;// fallback
	}

	protected int rwArgumentForFunctionCall(final IASTFunctionCallExpression func, int parameterIdx, int indirection) {
		final IASTExpression functionNameExpression = func.getFunctionNameExpression();
		if (functionNameExpression != null) {
			final IType type= functionNameExpression.getExpressionType();
			if (type instanceof IFunctionType) {
				IType[] ptypes= ((IFunctionType) type).getParameterTypes();
				if (ptypes != null && ptypes.length > parameterIdx) {
					return rwAssignmentToType(ptypes[parameterIdx], indirection);
				}
			}
		}
		return READ | WRITE; // fallback
	}

	protected abstract int rwAssignmentToType(IType type, int indirection);
	
	protected int rwInStatement(IASTNode node, IASTStatement stmt, int indirection) {
		if (stmt instanceof IASTCaseStatement) {
			if (node.getPropertyInParent() == IASTCaseStatement.EXPRESSION) {
				return READ;
			}
		}
		else if (stmt instanceof IASTDoStatement) {
			if (node.getPropertyInParent() == IASTDoStatement.CONDITION) {
				return READ;
			}
		}
		else if (stmt instanceof IASTExpressionStatement) {
			IASTNode parent= stmt.getParent();
			while (parent instanceof IASTCompoundStatement) {
				IASTCompoundStatement compound= (IASTCompoundStatement) parent;
				IASTStatement[] statements= compound.getStatements();
				if (statements[statements.length-1] != stmt) {
					return 0;
				}
				stmt= compound;
				parent= stmt.getParent();
			}
			if (parent instanceof IGNUASTCompoundStatementExpression) {
				return rwAnyNode(parent, indirection);
			}
		}
		else if (stmt instanceof IASTForStatement) {
			if (node.getPropertyInParent() == IASTForStatement.CONDITION) {
				return READ;
			}
		}
		else if (stmt instanceof IASTIfStatement) {
			if (node.getPropertyInParent() == IASTIfStatement.CONDITION) {
				return READ;
			}
		}
		else if (stmt instanceof IASTProblemStatement) {
			return READ | WRITE;
		}
		else if (stmt instanceof IASTReturnStatement) {
			return indirection == 0 ? READ : WRITE;
		}
		else if (stmt instanceof IASTSwitchStatement) {
			if (node.getPropertyInParent() == IASTSwitchStatement.CONTROLLER_EXP) {
				return READ;
			}
		}
		else if (stmt instanceof IASTWhileStatement) {
			if (node.getPropertyInParent() == IASTWhileStatement.CONDITIONEXPRESSION) {
				return READ;
			}
		}
		return 0;
	}

	protected int rwInUnaryExpression(IASTNode node, IASTUnaryExpression expr, int indirection) {
		switch(expr.getOperator()) {
		case IASTUnaryExpression.op_bracketedPrimary:
			return rwAnyNode(expr, indirection);
		
		case IASTUnaryExpression.op_amper:
			return rwAnyNode(expr, indirection+1);

		case IASTUnaryExpression.op_star:
			if (indirection > 0) {
				return rwAnyNode(expr, indirection-1);
			}
			return READ;
			
		case IASTUnaryExpression.op_postFixDecr:
		case IASTUnaryExpression.op_postFixIncr:
		case IASTUnaryExpression.op_prefixDecr:
		case IASTUnaryExpression.op_prefixIncr:
			return READ | WRITE;
		
		case IASTUnaryExpression.op_minus:
		case IASTUnaryExpression.op_not:
		case IASTUnaryExpression.op_plus:
		case IASTUnaryExpression.op_tilde:
			return PDOMName.READ_ACCESS;

		case IASTUnaryExpression.op_sizeof:
		case IASTUnaryExpression.op_sizeofParameterPack:
		case IASTUnaryExpression.op_alignOf:
			return 0;
		}
		return READ;
	}

	protected int rwInBinaryExpression(IASTNode node, IASTBinaryExpression expr, int indirection) {
		switch(expr.getOperator()) {
		case IASTBinaryExpression.op_assign:
			if (node.getPropertyInParent() == IASTBinaryExpression.OPERAND_ONE) {
				return WRITE;
			}
			return rwAssignmentToType(expr.getOperand1().getExpressionType(), indirection);
			
		case IASTBinaryExpression.op_binaryAndAssign:
		case IASTBinaryExpression.op_binaryOrAssign:
		case IASTBinaryExpression.op_binaryXorAssign:
		case IASTBinaryExpression.op_divideAssign:
		case IASTBinaryExpression.op_minusAssign:
		case IASTBinaryExpression.op_moduloAssign:
		case IASTBinaryExpression.op_multiplyAssign:
		case IASTBinaryExpression.op_plusAssign:
		case IASTBinaryExpression.op_shiftLeftAssign:
		case IASTBinaryExpression.op_shiftRightAssign:
			if (node.getPropertyInParent() == IASTBinaryExpression.OPERAND_ONE) {
				return READ | WRITE;
			}
			return READ;
			
		case IASTBinaryExpression.op_binaryAnd:
		case IASTBinaryExpression.op_binaryOr:
		case IASTBinaryExpression.op_binaryXor:
		case IASTBinaryExpression.op_divide:
		case IASTBinaryExpression.op_equals:
		case IASTBinaryExpression.op_greaterEqual:
		case IASTBinaryExpression.op_greaterThan:
		case IASTBinaryExpression.op_lessEqual:
		case IASTBinaryExpression.op_lessThan:
		case IASTBinaryExpression.op_logicalAnd:
		case IASTBinaryExpression.op_logicalOr:
		case IASTBinaryExpression.op_modulo:
		case IASTBinaryExpression.op_multiply:
		case IASTBinaryExpression.op_notequals:
		case IASTBinaryExpression.op_shiftLeft:
		case IASTBinaryExpression.op_shiftRight:
			return READ;

		case IASTBinaryExpression.op_minus:
		case IASTBinaryExpression.op_plus:
			if (indirection > 0) {
				// can be pointer arithmetics
				return rwAnyNode(expr, indirection);
			}
			return READ;
		}
		return READ; // fallback
	}
}
