/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;

import org.eclipse.cdt.internal.core.dom.parser.c.CASTArraySubscriptExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCastExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTConditionalExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTExpressionList;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypeIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArraySubscriptExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCastExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTConditionalExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeleteExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionList;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNewExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypenameExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;

/**
 * 
 * Creates a copy of an {@link IASTExpression}. 
 * 
 * @author Emanuel Graf IFS
 *
 */
public class ExpressionCopier {
	
	
	public IASTExpression createCopy(IASTExpression exp) {
		if (exp instanceof IASTLiteralExpression) {
			return copy((IASTLiteralExpression) exp);
		}
		if(exp instanceof IASTIdExpression) {
			return copy((IASTIdExpression)exp);
		}
		if(exp instanceof IASTArraySubscriptExpression) {
			return copy((IASTArraySubscriptExpression)exp);
		}
		if(exp instanceof IASTBinaryExpression) {
			return copy((IASTBinaryExpression)exp);
		}
		if(exp instanceof IASTCastExpression) {
			return copy((IASTCastExpression)exp);
		}
		if(exp instanceof IASTConditionalExpression) {
			return copy((IASTConditionalExpression)exp);
		}
		if(exp instanceof IASTExpressionList) {
			return copy((IASTExpressionList)exp);
		}
		if(exp instanceof IASTFieldReference) {
			return copy((IASTFieldReference)exp);
		}
		if(exp instanceof IASTFunctionCallExpression) {
			return copy((IASTFunctionCallExpression)exp);
		}
		if(exp instanceof IASTTypeIdExpression) {
			return copy((IASTTypeIdExpression)exp);
		}
		if(exp instanceof IASTUnaryExpression) {
			return copy((IASTUnaryExpression)exp);
		}
		if(exp instanceof ICPPASTDeleteExpression) {
			return copy((ICPPASTDeleteExpression)exp);
		}
		if(exp instanceof ICPPASTNewExpression) {
			return copy((ICPPASTNewExpression)exp);
		}
		if(exp instanceof ICPPASTSimpleTypeConstructorExpression) {
			return copy((ICPPASTSimpleTypeConstructorExpression)exp);
		}
		if(exp instanceof ICPPASTTypenameExpression) {
			return copy((ICPPASTTypenameExpression)exp);
		}
		
		return exp;
	}

	private IASTExpression copy(ICPPASTTypenameExpression exp) {
		ICPPASTTypenameExpression copy = new CPPASTTypenameExpression();
		copy.setIsTemplate(exp.isTemplate());
		copy.setName(new CPPASTName(exp.getName().toCharArray()));
		copy.setInitialValue(createCopy(exp.getInitialValue()));
		return copy;
	}

	private IASTExpression copy(ICPPASTSimpleTypeConstructorExpression exp) {
		ICPPASTSimpleTypeConstructorExpression copy = new CPPASTSimpleTypeConstructorExpression();
		copy.setSimpleType(exp.getSimpleType());
		copy.setInitialValue(createCopy(exp.getInitialValue()));
		return copy;
	}

	private IASTExpression copy(ICPPASTNewExpression exp) {
		ICPPASTNewExpression copy = new CPPASTNewExpression();
		
		copy.setIsGlobal(exp.isGlobal());
		copy.setIsNewTypeId(exp.isNewTypeId());
		copy.setNewInitializer(createCopy(exp.getNewInitializer()));
		copy.setNewPlacement(createCopy(exp.getNewPlacement()));
		copy.setTypeId(exp.getTypeId());
		
		return copy;
	}

	private IASTExpression copy(ICPPASTDeleteExpression exp) {
		ICPPASTDeleteExpression copy = new CPPASTDeleteExpression();
		
		copy.setIsGlobal(exp.isGlobal());
		copy.setIsVectored(exp.isVectored());
		copy.setOperand(createCopy(exp.getOperand()));
		
		return copy;
	}

	private IASTExpression copy(IASTUnaryExpression exp) {
		IASTUnaryExpression copy;
		if(exp instanceof CPPASTUnaryExpression) {
			copy = new CPPASTUnaryExpression();
		}else {
			copy = new CASTUnaryExpression();
		}
		copy.setOperator(exp.getOperator());
		copy.setOperand(createCopy(exp.getOperand()));
		return copy;
	}

	private IASTTypeIdExpression copy(IASTTypeIdExpression exp) {
		IASTTypeIdExpression copy;
		if(exp instanceof CPPASTTypeIdExpression) {
			copy = new CPPASTTypeIdExpression();
		}else {
			copy = new CASTTypeIdExpression();
		}
		
		copy.setOperator(exp.getOperator());
		copy.setTypeId(exp.getTypeId());
		return copy;
	}

	private IASTExpression copy(IASTFunctionCallExpression exp) {
		IASTFunctionCallExpression copy;
		if(exp instanceof CPPASTFunctionCallExpression) {
			copy = new CPPASTFunctionCallExpression();
		}else {
			copy = new CASTFunctionCallExpression();
		}
		copy.setFunctionNameExpression(createCopy(exp.getFunctionNameExpression()));
		copy.setParameterExpression(createCopy(exp.getParameterExpression()));
		
		return copy;
	}

	private IASTBinaryExpression copy(IASTBinaryExpression exp) {
		IASTBinaryExpression copy;
		if(exp instanceof CPPASTBinaryExpression) {
			copy = new CPPASTBinaryExpression();
		}else {
			copy = new CASTBinaryExpression();
		}
		
		copy.setOperand1(createCopy(exp.getOperand1()));
		copy.setOperand2(createCopy(exp.getOperand2()));
		copy.setOperator(exp.getOperator());
		
		return copy;
	}

	private IASTArraySubscriptExpression copy(IASTArraySubscriptExpression exp) {
		IASTArraySubscriptExpression copy;
		if(exp instanceof CPPASTArraySubscriptExpression) {
			copy = new CPPASTArraySubscriptExpression();
		}else {
			copy = new CASTArraySubscriptExpression();
		}
		
		copy.setArrayExpression(createCopy(exp.getArrayExpression()));
		copy.setSubscriptExpression(createCopy(exp.getSubscriptExpression()));
		return copy;
	}

	private IASTIdExpression copy(IASTIdExpression exp) {
		IASTIdExpression copy;
		if (exp instanceof CPPASTIdExpression) {
			copy = new CPPASTIdExpression();
		}else {
			copy = new CASTIdExpression();
		}
		copy.setName(exp.getName());
		return copy;
		
	}

	private IASTLiteralExpression copy(IASTLiteralExpression exp) {
		IASTLiteralExpression copy;
		if (exp instanceof ICPPASTLiteralExpression) {
			copy = new CPPASTLiteralExpression();
		}else {
			copy = new CASTLiteralExpression();
		}
		copy.setKind(exp.getKind());
		copy.setValue(exp.toString());
		return copy;
	}

	private IASTCastExpression copy(IASTCastExpression exp) {
		IASTCastExpression copy;
		if(exp instanceof ICPPASTCastExpression) {
			copy = new CPPASTCastExpression();
		}else {
			copy = new CASTCastExpression();
		}
		copy.setOperator(exp.getOperator());
		copy.setOperand(createCopy(exp.getOperand()));
		copy.setTypeId(exp.getTypeId());
		return copy;
	}
	
	private IASTConditionalExpression copy(IASTConditionalExpression exp) {
		IASTConditionalExpression copy;
		if(exp instanceof CASTConditionalExpression) {
			copy = new CASTConditionalExpression();
		}else {
			copy = new CPPASTConditionalExpression();
		}
		copy.setLogicalConditionExpression(createCopy(exp.getLogicalConditionExpression()));
		copy.setPositiveResultExpression(createCopy(exp.getPositiveResultExpression()));
		copy.setNegativeResultExpression(createCopy(exp.getNegativeResultExpression()));
		return copy;
	}
	
	private IASTExpressionList copy(IASTExpressionList exp) {
		IASTExpressionList copy;
		if(exp instanceof CASTExpressionList) {
			copy = new CASTExpressionList();
		}else {
			copy = new CPPASTExpressionList();
		}
		for (IASTExpression expression : exp.getExpressions()) {
			copy.addExpression(createCopy(expression));
		}
		return copy;
	}
	
	private IASTFieldReference copy(IASTFieldReference exp) {
		IASTFieldReference copy;
		if (exp instanceof ICPPASTFieldReference) {
			copy = new CPPASTFieldReference();
			((ICPPASTFieldReference)copy).setIsTemplate(((ICPPASTFieldReference)exp).isTemplate());
		}else {
			copy = new CASTFieldReference();
		}
		
		copy.setFieldName(exp.getFieldName());
		copy.setFieldOwner(createCopy(exp.getFieldOwner()));
		copy.setIsPointerDereference(exp.isPointerDereference());
		return copy;
	}
}
