/*******************************************************************************
 * Copyright (c) 2008, 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *    Institute for Software - initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;


/**
 * 
 * Generates source code of expression nodes. The actual string operations are delegated
 * to the <code>Scribe</code> class.
 * 
 * @see Scribe
 * @see IASTExpression
 * @author Emanuel Graf IFS
 * 
 */
public class ExpressionWriter extends NodeWriter{
	
	private static final String VECTORED_DELETE_OP = "[] "; //$NON-NLS-1$
	private static final String DELETE = "delete "; //$NON-NLS-1$
	private static final String STATIC_CAST_OP = "static_cast<"; //$NON-NLS-1$
	private static final String REINTERPRET_CAST_OP = "reinterpret_cast<"; //$NON-NLS-1$
	private static final String DYNAMIC_CAST_OP = "dynamic_cast<"; //$NON-NLS-1$
	private static final String CONST_CAST_OP = "const_cast<"; //$NON-NLS-1$
	private static final String CLOSING_CAST_BRACKET_OP = ">"; //$NON-NLS-1$
	private static final String ARROW = "->"; //$NON-NLS-1$
	private static final String SPACE_QUESTIONMARK_SPACE = " ? "; //$NON-NLS-1$
	private static final String NEW = "new "; //$NON-NLS-1$
	private static final String CLOSING_BRACKET_OP = ")"; //$NON-NLS-1$
	private static final String TYPEOF_OP = "typeof ("; //$NON-NLS-1$
	private static final String ALIGNOF_OP = "alignof ("; //$NON-NLS-1$
	private static final String TYPEID_OP = "typeid ("; //$NON-NLS-1$
	private static final String OPEN_BRACKET_OP = "("; //$NON-NLS-1$
	private static final String SIZEOF_OP = "sizeof "; //$NON-NLS-1$
	private static final String SIZEOF_PARAMETER_PACK_OP = "sizeof... "; //$NON-NLS-1$
	private static final String NOT_OP = "!"; //$NON-NLS-1$
	private static final String TILDE_OP = "~"; //$NON-NLS-1$
	private static final String AMPERSAND_OP = "&"; //$NON-NLS-1$
	private static final String STAR_OP = "*"; //$NON-NLS-1$
	private static final String UNARY_MINUS_OP = "-"; //$NON-NLS-1$
	private static final String UNARY_PLUS_OP = "+"; //$NON-NLS-1$
	private static final String INCREMENT_OP = "++"; //$NON-NLS-1$
	private static final String DECREMENT_OP = "--"; //$NON-NLS-1$
	private static final String MIN_OP = " <? "; //$NON-NLS-1$
	private static final String MAX_OP = " >? "; //$NON-NLS-1$
	private static final String PMARROW_OP = "->*"; //$NON-NLS-1$
	private static final String PMDOT_OP = ".*"; //$NON-NLS-1$
	private static final String ELLIPSES = " ... "; //$NON-NLS-1$
	private static final String NOT_EQUALS_OP = " != "; //$NON-NLS-1$
	private static final String EQUALS_OP = " == "; //$NON-NLS-1$
	private static final String BINARY_OR_ASSIGN = " |= "; //$NON-NLS-1$
	private static final String BINARY_XOR_ASSIGN_OP = " ^= "; //$NON-NLS-1$
	private static final String BINARY_AND_ASSIGN_OP = " &= "; //$NON-NLS-1$
	private static final String SHIFT_RIGHT_ASSIGN_OP = " >>= "; //$NON-NLS-1$
	private static final String SHIFT_LEFT_ASSIGN_OP = " <<= "; //$NON-NLS-1$
	private static final String MINUS_ASSIGN_OP = " -= "; //$NON-NLS-1$
	private static final String PLUS_ASSIGN_OP = " += "; //$NON-NLS-1$
	private static final String MODULO_ASSIGN_OP = " %= "; //$NON-NLS-1$
	private static final String DIVIDE_ASSIGN_OP = " /= "; //$NON-NLS-1$
	private static final String MULTIPLY_ASSIGN_OP = " *= "; //$NON-NLS-1$
	private static final String LOGICAL_OR_OP = " || "; //$NON-NLS-1$
	private static final String LOGICAL_AND_OP = " && "; //$NON-NLS-1$
	private static final String BINARY_OR_OP = " | "; //$NON-NLS-1$
	private static final String BINARY_XOR_OP = " ^ "; //$NON-NLS-1$
	private static final String BINARY_AND_OP = " & "; //$NON-NLS-1$
	private static final String GREAER_EQUAL_OP = " >= "; //$NON-NLS-1$
	private static final String LESS_EQUAL_OP = " <= "; //$NON-NLS-1$
	private static final String GREATER_THAN_OP = " > "; //$NON-NLS-1$
	private static final String LESS_THAN_OP = " < "; //$NON-NLS-1$
	private static final String SHIFT_RIGHT_OP = " >> "; //$NON-NLS-1$
	private static final String SHIFT_LEFT_OP = " << "; //$NON-NLS-1$
	private static final String MINUS_OP = " - "; //$NON-NLS-1$
	private static final String PLUS_OP = " + "; //$NON-NLS-1$
	private static final String MODULO_OP = " % "; //$NON-NLS-1$
	private static final String DIVIDE_OP = " / "; //$NON-NLS-1$
	private static final String MULTIPLY_OP = " * "; //$NON-NLS-1$
	private final MacroExpansionHandler macroHandler;
	
	public ExpressionWriter(Scribe scribe, CPPASTVisitor visitor, MacroExpansionHandler macroHandler, NodeCommentMap commentMap) {
		super(scribe, visitor, commentMap);
		this.macroHandler = macroHandler;
	}
	
	protected void writeExpression(IASTExpression expression) {
		if (expression instanceof IASTBinaryExpression) {
			writeBinaryExpression((IASTBinaryExpression) expression);
		} else if (expression instanceof IASTIdExpression) {
			((IASTIdExpression) expression).getName().accept(visitor);
		} else if (expression instanceof IASTLiteralExpression) {
			writeLiteralExpression((IASTLiteralExpression) expression);
		} else if (expression instanceof IASTUnaryExpression) {
			writeUnaryExpression((IASTUnaryExpression) expression);
		} else if (expression instanceof IASTCastExpression) {
			writeCastExpression((IASTCastExpression) expression);
		} else if (expression instanceof ICPPASTNewExpression) {
			writeCPPNewExpression((ICPPASTNewExpression) expression);
		}else if (expression instanceof IASTConditionalExpression) {
			writeConditionalExpression((IASTConditionalExpression) expression);
		}else if (expression instanceof IASTArraySubscriptExpression) {
			writeArraySubscriptExpression((IASTArraySubscriptExpression) expression);
		}else if (expression instanceof IASTFieldReference) {
			writeFieldReference((IASTFieldReference) expression);
		}else if (expression instanceof IASTFunctionCallExpression) {
			writeFunctionCallExpression((IASTFunctionCallExpression) expression);
		}else if (expression instanceof IASTExpressionList) {
			writeExpressionList((IASTExpressionList) expression);
		}else if (expression instanceof IASTProblemExpression) {
			throw new ProblemRuntimeException(((IASTProblemExpression) expression));
		}else if (expression instanceof IASTTypeIdExpression) {
			writeTypeIdExpression((IASTTypeIdExpression) expression);
		}else if (expression instanceof ICPPASTDeleteExpression) {
			writeDeleteExpression((ICPPASTDeleteExpression) expression);
		}else if (expression instanceof ICPPASTSimpleTypeConstructorExpression) {
			writeSimpleTypeConstructorExpression((ICPPASTSimpleTypeConstructorExpression) expression);
		}
	}

	private String getBinaryExpressionOperator(int operator){

		switch(operator){
		case IASTBinaryExpression.op_multiply:
			return MULTIPLY_OP;
		case IASTBinaryExpression.op_divide:
			return DIVIDE_OP;
		case IASTBinaryExpression.op_modulo:
			return MODULO_OP;
		case IASTBinaryExpression.op_plus:
			return PLUS_OP;
		case IASTBinaryExpression.op_minus:
			return MINUS_OP;
		case IASTBinaryExpression.op_shiftLeft:
			return SHIFT_LEFT_OP;
		case IASTBinaryExpression.op_shiftRight:
			return SHIFT_RIGHT_OP;
		case IASTBinaryExpression.op_lessThan:
			return LESS_THAN_OP;
		case IASTBinaryExpression.op_greaterThan:
			return GREATER_THAN_OP;
		case IASTBinaryExpression.op_lessEqual:
			return LESS_EQUAL_OP;
		case IASTBinaryExpression.op_greaterEqual:
			return GREAER_EQUAL_OP;
		case IASTBinaryExpression.op_binaryAnd:
			return BINARY_AND_OP;
		case IASTBinaryExpression.op_binaryXor:
			return BINARY_XOR_OP;
		case IASTBinaryExpression.op_binaryOr:
			return BINARY_OR_OP;
		case IASTBinaryExpression.op_logicalAnd:
			return LOGICAL_AND_OP;
		case IASTBinaryExpression.op_logicalOr:
			return LOGICAL_OR_OP;
		case IASTBinaryExpression.op_assign:
			return EQUALS;
		case IASTBinaryExpression.op_multiplyAssign:
			return MULTIPLY_ASSIGN_OP;
		case IASTBinaryExpression.op_divideAssign:
			return DIVIDE_ASSIGN_OP;
		case IASTBinaryExpression.op_moduloAssign:
			return MODULO_ASSIGN_OP;
		case IASTBinaryExpression.op_plusAssign:
			return PLUS_ASSIGN_OP;
		case IASTBinaryExpression.op_minusAssign:
			return MINUS_ASSIGN_OP;
		case IASTBinaryExpression.op_shiftLeftAssign:
			return SHIFT_LEFT_ASSIGN_OP;
		case IASTBinaryExpression.op_shiftRightAssign:
			return SHIFT_RIGHT_ASSIGN_OP;
		case IASTBinaryExpression.op_binaryAndAssign:
			return BINARY_AND_ASSIGN_OP;
		case IASTBinaryExpression.op_binaryXorAssign:
			return BINARY_XOR_ASSIGN_OP;
		case IASTBinaryExpression.op_binaryOrAssign:
			return BINARY_OR_ASSIGN;
		case IASTBinaryExpression.op_equals:
			return EQUALS_OP;
		case IASTBinaryExpression.op_notequals:
			return NOT_EQUALS_OP;
		case ICPPASTBinaryExpression.op_pmdot:
			return PMDOT_OP;
		case ICPPASTBinaryExpression.op_pmarrow:
			return PMARROW_OP;
		case IGPPASTBinaryExpression.op_max:
			return MAX_OP;
		case IGPPASTBinaryExpression.op_min:
			return MIN_OP;
		case IASTBinaryExpression.op_ellipses:
			return ELLIPSES;
		default:
			System.err.println("Unknown unaryExpressionType: " + operator); //$NON-NLS-1$
			throw new IllegalArgumentException("Unknown unaryExpressionType: " + operator); //$NON-NLS-1$
		}

	}
	
	private boolean isPrefixExpression(IASTUnaryExpression unExp) {
		int unaryExpressionType = unExp.getOperator();

		switch (unaryExpressionType) {
		case IASTUnaryExpression.op_prefixDecr:	
		case IASTUnaryExpression.op_prefixIncr:
		case IASTUnaryExpression.op_plus:
		case IASTUnaryExpression.op_minus:
		case IASTUnaryExpression.op_star:
		case IASTUnaryExpression.op_amper:
		case IASTUnaryExpression.op_tilde:
		case IASTUnaryExpression.op_not:
		case IASTUnaryExpression.op_sizeof:
		case IASTUnaryExpression.op_sizeofParameterPack:
		case IASTUnaryExpression.op_bracketedPrimary:
		case ICPPASTUnaryExpression.op_throw:
		case ICPPASTUnaryExpression.op_typeid:
		case IASTUnaryExpression.op_alignOf: 
			return true;

		default:
			return false;
		}
	}
	
	private boolean isPostfixExpression(IASTUnaryExpression unExp) {
		int unaryExpressionType = unExp.getOperator();
		switch (unaryExpressionType) {
		case IASTUnaryExpression.op_postFixDecr:	
		case IASTUnaryExpression.op_postFixIncr:
		case IASTUnaryExpression.op_bracketedPrimary:
		case ICPPASTUnaryExpression.op_typeid:
		case IASTUnaryExpression.op_alignOf:
			return true;

		default:
			return false;
		}
	}
	
	private String getPrefixOperator(IASTUnaryExpression unExp) {
		int unaryExpressionType = unExp.getOperator();
		switch (unaryExpressionType) {
		case IASTUnaryExpression.op_prefixDecr:	
			return DECREMENT_OP;
		case IASTUnaryExpression.op_prefixIncr:
			return INCREMENT_OP;
		case IASTUnaryExpression.op_plus:
			return UNARY_PLUS_OP;
		case IASTUnaryExpression.op_minus:
			return UNARY_MINUS_OP;
		case IASTUnaryExpression.op_star:
			return STAR_OP;
		case IASTUnaryExpression.op_amper:
			return AMPERSAND_OP;
		case IASTUnaryExpression.op_tilde:
			return TILDE_OP;
		case IASTUnaryExpression.op_not:
			return NOT_OP;
		case IASTUnaryExpression.op_sizeof:
			return SIZEOF_OP;
		case IASTUnaryExpression.op_sizeofParameterPack:
			return SIZEOF_PARAMETER_PACK_OP;
		case IASTUnaryExpression.op_bracketedPrimary:
			return OPEN_BRACKET_OP;
		case ICPPASTUnaryExpression.op_throw:
			return THROW;
		case ICPPASTUnaryExpression.op_typeid:
			return TYPEID_OP;
		case IASTUnaryExpression.op_alignOf:
			return ALIGNOF_OP;
		default:
			System.err.println("Unkwown unaryExpressionType: " + unaryExpressionType); //$NON-NLS-1$
		throw new IllegalArgumentException("Unkwown unaryExpressionType: " + unaryExpressionType); //$NON-NLS-1$
		}
	}
	
	private String getPostfixOperator(IASTUnaryExpression unExp) {
		int unaryExpressionType = unExp.getOperator();
		switch (unaryExpressionType) {
		case IASTUnaryExpression.op_postFixDecr:
			return DECREMENT_OP;
		case IASTUnaryExpression.op_postFixIncr:
			return INCREMENT_OP;
		case ICPPASTUnaryExpression.op_typeid:
			return CLOSING_BRACKET_OP;
		case IASTUnaryExpression.op_bracketedPrimary:
		case IASTUnaryExpression.op_alignOf:
			return CLOSING_BRACKET_OP;
		default:
			System.err.println("Unkwown unaryExpressionType " + unaryExpressionType); //$NON-NLS-1$
			throw new IllegalArgumentException("Unkwown unaryExpressionType " + unaryExpressionType); //$NON-NLS-1$
		}
	}

	private void writeBinaryExpression(IASTBinaryExpression binExp) {
		IASTExpression operand1 = binExp.getOperand1();
		if (!macroHandler.checkisMacroExpansionNode(operand1)) {
			operand1.accept(visitor);
		}
		IASTExpression operand2 = binExp.getOperand2();
		if(macroHandler.checkisMacroExpansionNode(operand2, false)&& macroHandler.macroExpansionAlreadyPrinted(operand2)) {
			return;
		}
		scribe.print(getBinaryExpressionOperator(binExp.getOperator()));
		operand2.accept(visitor);
	}

	private void writeCPPNewExpression(ICPPASTNewExpression newExp) {
		if(newExp.isGlobal()) {
			scribe.print(COLON_COLON);
		}
		scribe.print(NEW);
		IASTInitializerClause[] placement = newExp.getPlacementArguments();
		if (placement != null) {
			writeArgumentList(placement);
		}
				
		IASTTypeId typeId = newExp.getTypeId();
		visitNodeIfNotNull(typeId);
		
		IASTInitializer initExp= getNewInitializer(newExp);
		if (initExp != null) {
			initExp.accept(visitor);
		}
	}

	protected IASTInitializer getNewInitializer(ICPPASTNewExpression newExp) {
		return newExp.getInitializer();
	}

	private void writeArgumentList(IASTInitializerClause[] args) {
		scribe.print(OPEN_BRACKET_OP);
		boolean needComma= false;
		for (IASTInitializerClause arg : args) {
			if (needComma) {
				scribe.print(COMMA_SPACE);
			}
			arg.accept(visitor);
			needComma= true;
		}
		scribe.print(CLOSING_BRACKET_OP);
	}

	private void writeLiteralExpression(IASTLiteralExpression litExp) {
		scribe.print(litExp.toString());
	}

	private void writeUnaryExpression(IASTUnaryExpression unExp) {
		if(isPrefixExpression(unExp )) {
			scribe.print(getPrefixOperator(unExp));
		}
		unExp.getOperand().accept(visitor);
		if(isPostfixExpression(unExp)) {
			scribe.print(getPostfixOperator(unExp));
		}
	}

	private void writeConditionalExpression(IASTConditionalExpression condExp) {
		condExp.getLogicalConditionExpression().accept(visitor);
		scribe.print(SPACE_QUESTIONMARK_SPACE);
		final IASTExpression positiveExpression = condExp.getPositiveResultExpression();
		// gcc extension allows to omit the positive expression.
		if (positiveExpression == null) {
			scribe.print(' ');
		}
		else {
			positiveExpression.accept(visitor);
		}
		scribe.print(SPACE_COLON_SPACE);
		condExp.getNegativeResultExpression().accept(visitor);
		
	}

	private void writeArraySubscriptExpression(IASTArraySubscriptExpression arrSubExp) {
		arrSubExp.getArrayExpression().accept(visitor);
		scribe.print('[');
		arrSubExp.getArgument().accept(visitor);
		scribe.print(']');
		
	}

	private void writeFieldReference(IASTFieldReference fieldRef) {
		fieldRef.getFieldOwner().accept(visitor);
		if(fieldRef.isPointerDereference()) {
			scribe.print(ARROW);
		}else {
			scribe.print('.');
		}
		if (fieldRef instanceof ICPPASTFieldReference) {
			ICPPASTFieldReference cppFieldRef = (ICPPASTFieldReference) fieldRef;
			if(cppFieldRef.isTemplate()) {
				scribe.print(TEMPLATE);
			}
		}
		fieldRef.getFieldName().accept(visitor);
		
	}

	private void writeFunctionCallExpression(IASTFunctionCallExpression funcCallExp) {
		funcCallExp.getFunctionNameExpression().accept(visitor);
		writeArgumentList(funcCallExp.getArguments());
	}

	private void writeCastExpression(IASTCastExpression castExp) {
		scribe.print(getCastPrefix(castExp.getOperator()));
		castExp.getTypeId().accept(visitor);
		scribe.print(getCastPostfix(castExp.getOperator()));
		if (castExp instanceof ICPPASTCastExpression) {
			scribe.print('(');			
		}
		castExp.getOperand().accept(visitor);
		if (castExp instanceof ICPPASTCastExpression) {
			scribe.print(')');			
		}
	}

	private String getCastPostfix(int castType) {
		switch (castType) {
		case IASTCastExpression.op_cast:
			return CLOSING_BRACKET_OP;
		case ICPPASTCastExpression.op_const_cast:
		case ICPPASTCastExpression.op_dynamic_cast:
		case ICPPASTCastExpression.op_reinterpret_cast:
		case ICPPASTCastExpression.op_static_cast:
			return CLOSING_CAST_BRACKET_OP;
		default:
			throw new IllegalArgumentException("Unknown Cast Type"); //$NON-NLS-1$
		}
	}

	private String getCastPrefix(int castType) {
		switch (castType) {
		case IASTCastExpression.op_cast:
			return OPEN_BRACKET_OP;
		case ICPPASTCastExpression.op_const_cast:
			return CONST_CAST_OP;
		case ICPPASTCastExpression.op_dynamic_cast:
			return DYNAMIC_CAST_OP;
		case ICPPASTCastExpression.op_reinterpret_cast:
			return REINTERPRET_CAST_OP;
		case ICPPASTCastExpression.op_static_cast:
			return STATIC_CAST_OP;
		default:
			throw new IllegalArgumentException("Unknown Cast Type"); //$NON-NLS-1$
		}
	}

	private void writeExpressionList(IASTExpressionList expList) {

		IASTExpression[] expressions = expList.getExpressions();
		writeExpressions(expList, expressions);
	}

	protected void writeExpressions(IASTExpressionList expList, IASTExpression[] expressions) {
		writeNodeList(expressions);
	}

	private void writeTypeIdExpression(IASTTypeIdExpression typeIdExp) {
		scribe.print(getTypeIdExp(typeIdExp));
		typeIdExp.getTypeId().accept(visitor);
		scribe.print(')');		
	}

	private String getTypeIdExp(IASTTypeIdExpression typeIdExp) {
		final int type = typeIdExp.getOperator();
		switch(type) {
		case IASTTypeIdExpression.op_sizeof:
			return SIZEOF_OP + "("; //$NON-NLS-1$
		case ICPPASTTypeIdExpression.op_typeid:
			return TYPEID_OP;
		case IGNUASTTypeIdExpression.op_alignof:
			return ALIGNOF_OP + "("; //$NON-NLS-1$
		case IGNUASTTypeIdExpression.op_typeof:
			return TYPEOF_OP;
		}
		throw new IllegalArgumentException("Unknown TypeId Type"); //$NON-NLS-1$
	}

	private void writeDeleteExpression(ICPPASTDeleteExpression delExp) {
		if(delExp.isGlobal()) {
			scribe.print(COLON_COLON);
		}
		scribe.print(DELETE);
		if(delExp.isVectored()) {
			scribe.print(VECTORED_DELETE_OP);
		}
		delExp.getOperand().accept(visitor);
	}

	private void writeSimpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression simpTypeCtorExp) {
		simpTypeCtorExp.getDeclSpecifier().accept(visitor);
		visitNodeIfNotNull(simpTypeCtorExp.getInitializer());
	}
}

