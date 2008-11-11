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
package org.eclipse.cdt.ui.tests.refactoring.utils;

import junit.framework.TestCase;

import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.core.parser.ParserLanguage;

import org.eclipse.cdt.internal.core.dom.parser.c.CASTArraySubscriptExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTConditionalExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTExpressionList;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTName;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypeIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArraySubscriptExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTConditionalExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeleteExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNewExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypenameExpression;

import org.eclipse.cdt.internal.ui.refactoring.utils.ExpressionCopier;

/**
 * @author Emanuel Graf IFS
 *
 */
public class ExpressionCopyTest extends TestCase {
	
	private ExpressionCopier copier = new ExpressionCopier();


	/**
	 * Test method for {@link org.eclipse.cdt.internal.ui.refactoring.utils.ExpressionCopier#copy(org.eclipse.cdt.core.dom.ast.IASTIdExpression)}.
	 */
	public void testCopyIASTIdExpression() {
		IASTIdExpression castIdExp = new CASTIdExpression();
		
		castIdExp.setName(getTestName(ParserLanguage.C));
		IASTIdExpression copy = (IASTIdExpression) copier.createCopy(castIdExp);
		assertTrue(copy instanceof CASTIdExpression);
		assertEquals(castIdExp, copy);
		
		IASTIdExpression cppAstIdExp = new CPPASTIdExpression(getTestName(ParserLanguage.CPP));
		copy = (IASTIdExpression) copier.createCopy(cppAstIdExp);
		assertTrue(copy instanceof CPPASTIdExpression);
		assertEquals(cppAstIdExp, copy);
		
	}



	/**
	 * Test method for {@link org.eclipse.cdt.internal.ui.refactoring.utils.ExpressionCopier#copy(org.eclipse.cdt.core.dom.ast.IASTLiteralExpression)}.
	 */
	public void testCopyIASTLiteralExpression() {
		IASTLiteralExpression litExp = getTestLiteralExp(ParserLanguage.C);
		IASTLiteralExpression copy = (IASTLiteralExpression) copier.createCopy(litExp);
		
		assertTrue(copy instanceof CASTLiteralExpression);
		assertEquals(litExp, copy);
		
		
		litExp = getTestLiteralExp(ParserLanguage.CPP);
		copy = (IASTLiteralExpression) copier.createCopy(litExp);
		assertTrue(copy instanceof CPPASTLiteralExpression);
		assertEquals(litExp, copy);
		
	}

	
	/**
	 * Test method for {@link org.eclipse.cdt.internal.ui.refactoring.utils.ExpressionCopier#copy(IASTArraySubscriptExpression)}.
	 */
	public void testCopyIASTArraySubscriptExpression() {
		IASTIdExpression idExp = new CASTIdExpression(getTestName(ParserLanguage.C));
		IASTLiteralExpression litExp =getTestLiteralExp(ParserLanguage.C);
		
		IASTArraySubscriptExpression arrSubExp = new CASTArraySubscriptExpression(idExp, litExp);
		IASTArraySubscriptExpression copy = (IASTArraySubscriptExpression) copier.createCopy(arrSubExp);
		assertFalse(arrSubExp == copy);
		assertTrue(copy instanceof CASTArraySubscriptExpression);
		assertFalse(arrSubExp.getArrayExpression() == copy.getArrayExpression());
		assertEquals(idExp, (IASTIdExpression)copy.getArrayExpression());
		assertEquals(litExp, (IASTLiteralExpression)copy.getSubscriptExpression());
		
		idExp = new CPPASTIdExpression(getTestName(ParserLanguage.CPP));
		litExp = getTestLiteralExp(ParserLanguage.CPP);		
		arrSubExp = new CPPASTArraySubscriptExpression(idExp, litExp);
		copy = (IASTArraySubscriptExpression) copier.createCopy(arrSubExp);
		assertFalse(arrSubExp == copy);
		assertTrue(copy instanceof CPPASTArraySubscriptExpression);
		assertFalse(arrSubExp.getArrayExpression() == copy.getArrayExpression());
		assertEquals(idExp, (IASTIdExpression)copy.getArrayExpression());
		assertEquals(litExp, (IASTLiteralExpression)copy.getSubscriptExpression());		
	}

	public void testCopyASTBinaryExpression() {
		IASTExpression op1 = getTestLiteralExp(ParserLanguage.C);
		IASTExpression op2 = getTestLiteralExp(ParserLanguage.C);
		
		IASTBinaryExpression binExp = new CASTBinaryExpression(IASTBinaryExpression.op_plus, op1, op2);
		IASTBinaryExpression copy = (IASTBinaryExpression) copier.createCopy(binExp);
		assertTrue(copy instanceof CASTBinaryExpression);
		assertEquals(binExp, copy);
		
		op1 = getTestLiteralExp(ParserLanguage.CPP);
		op2 = getTestLiteralExp(ParserLanguage.CPP);
		binExp = new CPPASTBinaryExpression(IASTBinaryExpression.op_plus, op1, op2);
		copy = (IASTBinaryExpression) copier.createCopy(binExp);
		assertTrue(copy instanceof CPPASTBinaryExpression);
		assertEquals(binExp, copy);
	}

	public void testCopyCastExpression() {
		IASTExpression op1 = getTestLiteralExp(ParserLanguage.C);
		
		//TODO
	}
	
	public void testCopyConditionalExpression() {
		IASTExpression pos = getTestLiteralExp(ParserLanguage.C);
		IASTExpression neg = getTestLiteralExp(ParserLanguage.C);
		IASTExpression cond = getTestLiteralExp(ParserLanguage.C);
		
		IASTConditionalExpression condExp = new CASTConditionalExpression(cond, pos, neg);
		IASTConditionalExpression copy = (IASTConditionalExpression) copier.createCopy(condExp);
		assertTrue(copy instanceof CASTConditionalExpression);
		assertEquals(condExp, copy);
		
		pos = getTestLiteralExp(ParserLanguage.CPP);
		neg = getTestLiteralExp(ParserLanguage.CPP);
		cond = getTestLiteralExp(ParserLanguage.CPP);
		
		condExp = new CPPASTConditionalExpression(cond, pos, neg);
		copy = (IASTConditionalExpression) copier.createCopy(condExp);
		assertTrue(copy instanceof CPPASTConditionalExpression);
		assertEquals(condExp, copy);
	}
	
	public void testCopyExpressionList() {
		IASTLiteralExpression lit1 = getTestLiteralExp(ParserLanguage.C);
		IASTLiteralExpression lit2 = getTestLiteralExp(ParserLanguage.C);
		
		IASTExpressionList list = new CASTExpressionList();
		list.addExpression(lit1);
		list.addExpression(lit2);
		IASTExpressionList copy = (IASTExpressionList) copier.createCopy(list);
		
		assertFalse(list == copy);
		IASTExpression[] orgList = list.getExpressions();
		IASTExpression[] copyList = copy.getExpressions();
		assertEquals(orgList.length, copyList.length);
		for(int i = 0; i < orgList.length; ++i	) {
			assertEquals((IASTLiteralExpression)orgList[i], (IASTLiteralExpression)copyList[i]);
		}
	}
	
	public void testCopyFieldExpression() {
		IASTName name = getTestName(ParserLanguage.C);
		IASTIdExpression id = new CASTIdExpression();
		id.setName(getTestName(ParserLanguage.C));
		
		IASTFieldReference exp = new CASTFieldReference(name, id, true);
		IASTFieldReference copy = (IASTFieldReference) copier.createCopy(exp);
		
		assertEquals(exp, copy);
	}
	
	public void testCopyFunctionCallExpression() {
		IASTIdExpression funcName = new CASTIdExpression(getTestName(ParserLanguage.C));
		IASTLiteralExpression parameter = getTestLiteralExp(ParserLanguage.C);
		IASTFunctionCallExpression funcCall = new CASTFunctionCallExpression();
		funcCall.setFunctionNameExpression(funcName);
		funcCall.setParameterExpression(parameter);
		
		IASTFunctionCallExpression copy = (IASTFunctionCallExpression) copier.createCopy(funcCall);
		assertEquals(funcCall, copy);
	}
	
	public void testCopyTypeIdExpression() {
		IASTTypeIdExpression type = new CASTTypeIdExpression();
		type.setOperator(IASTTypeIdExpression.op_sizeof);
		
		IASTTypeIdExpression copy = (IASTTypeIdExpression) copier.createCopy(type);
		assertEquals(type, copy);
	}
	
	public void testCopyUnaryExpression() {
		IASTUnaryExpression exp = new CASTUnaryExpression();
		exp.setOperand(new CASTIdExpression(getTestName(ParserLanguage.C)));
		exp.setOperator(IASTUnaryExpression.op_postFixDecr);
		
		IASTUnaryExpression copy = (IASTUnaryExpression) copier.createCopy(exp);
		
		assertEquals(exp,copy);
	}
	
	public void testCopyDeleteExp() {
		ICPPASTDeleteExpression exp = new CPPASTDeleteExpression();
		exp.setIsVectored(true);
		exp.setOperand(new CASTIdExpression(getTestName(ParserLanguage.CPP)));
		
		ICPPASTDeleteExpression copy = (ICPPASTDeleteExpression) copier.createCopy(exp);
		
		assertEquals(exp, copy);
		
	}
	
	public void testCopyNewExp() {
		ICPPASTNewExpression exp = new CPPASTNewExpression();
		
		IASTIdExpression placement = new CPPASTIdExpression(getTestName(ParserLanguage.CPP));
		IASTLiteralExpression init = getTestLiteralExp(ParserLanguage.CPP);
		
		exp.setNewPlacement(placement);
		exp.setNewInitializer(init);
		
		ICPPASTNewExpression copy = (ICPPASTNewExpression) copier.createCopy(exp);
		
		assertEquals(exp, copy);
	}
	
	public void testCopySimpleTypeConstructor() {
		ICPPASTSimpleTypeConstructorExpression exp = new CPPASTSimpleTypeConstructorExpression();
		exp.setSimpleType(ICPPASTSimpleTypeConstructorExpression.t_int);
		exp.setInitialValue(getTestLiteralExp(ParserLanguage.CPP));
		
		ICPPASTSimpleTypeConstructorExpression copy = (ICPPASTSimpleTypeConstructorExpression) copier.createCopy(exp);
		
		assertEquals(exp, copy);
	}
	
	public void testCopyTypeIdInitExp() {
		//TODO
	}
	
	public void testCopyTypenameExp() {
		ICPPASTTypenameExpression exp = new CPPASTTypenameExpression();
		exp.setName(getTestName(ParserLanguage.CPP));
		exp.setIsTemplate(true);
		exp.setInitialValue(getTestLiteralExp(ParserLanguage.CPP));
		
		ICPPASTTypenameExpression copy = (ICPPASTTypenameExpression) copier.createCopy(exp);
		
		assertEquals(exp, copy);
	}
	
	private void assertEquals(ICPPASTTypenameExpression exp, ICPPASTTypenameExpression copy) {
		assertFalse(exp == copy);
		
		assertFalse(exp.getName() == copy.getName());
		assertFalse(exp.getInitialValue() == copy.getInitialValue());
		
		assertEquals(exp.getName().toCharArray(), copy.getName().toCharArray());
		assertEquals(exp.isTemplate(), copy.isTemplate());
	}
	
	private void assertEquals(ICPPASTSimpleTypeConstructorExpression exp, ICPPASTSimpleTypeConstructorExpression copy) {
		assertFalse(exp == copy);
		assertEquals(exp.getSimpleType(), copy.getSimpleType());
		assertFalse(exp.getInitialValue() == copy.getInitialValue());
		assertEquals((IASTLiteralExpression)exp.getInitialValue(), (IASTLiteralExpression)copy.getInitialValue());
	}
	
	private void assertEquals(ICPPASTNewExpression exp, ICPPASTNewExpression copy) {
		assertFalse(exp == copy);
		assertEquals(exp.isGlobal(), copy.isGlobal());
		assertEquals(exp.isNewTypeId(), copy.isNewTypeId());
		
		assertFalse(exp.getNewInitializer() == copy.getNewInitializer());
		assertFalse(exp.getNewPlacement() == copy.getNewPlacement());
	}
	
	private void assertEquals(ICPPASTDeleteExpression exp, ICPPASTDeleteExpression copy) {
		assertFalse(exp == copy);
		assertEquals(exp.isGlobal(), copy.isGlobal());
		assertEquals(exp.isVectored(), copy.isVectored());
		assertFalse(exp.getOperand() == copy.getOperand());
		assertEquals((IASTIdExpression)exp.getOperand(), (IASTIdExpression)copy.getOperand());
	}
	
	private void assertEquals(IASTUnaryExpression exp, IASTUnaryExpression copy) {
		assertFalse(exp == copy);
		assertEquals(exp.getOperator(), copy.getOperator());
		assertEquals((IASTIdExpression)exp.getOperand(),(IASTIdExpression)copy.getOperand());
	}
	
	private void assertEquals(IASTTypeIdExpression exp, IASTTypeIdExpression copy) {
		assertFalse(exp == copy);
		assertEquals(exp.getOperator(), copy.getOperator());
	}
	
	private void assertEquals(IASTFunctionCallExpression exp, IASTFunctionCallExpression copy) {
		assertFalse(exp == copy);
		assertFalse(exp.getFunctionNameExpression() == copy.getFunctionNameExpression());
		assertFalse(exp.getParameterExpression() == copy.getParameterExpression());
		
		assertEquals((IASTIdExpression)exp.getFunctionNameExpression(), (IASTIdExpression)copy.getFunctionNameExpression());
		assertEquals((IASTLiteralExpression)exp.getParameterExpression(),(IASTLiteralExpression)copy.getParameterExpression());
	}
	
	private void assertEquals(IASTFieldReference exp, IASTFieldReference copy) {
		assertFalse(exp == copy);
		
		assertEquals(exp.getFieldName().toString(), copy.getFieldName().toString());
		assertEquals((IASTIdExpression) exp.getFieldOwner(), (IASTIdExpression) copy.getFieldOwner());
		assertEquals(exp.isPointerDereference(), copy.isPointerDereference());
		if (exp instanceof ICPPASTFieldReference) {
			assertEquals(((ICPPASTFieldReference)exp.getFieldOwner()).isTemplate(),((ICPPASTFieldReference)copy.getFieldOwner()).isTemplate());
		}
	}
	
	private void assertEquals(IASTConditionalExpression condExp, IASTConditionalExpression copy) {
		assertFalse(condExp == copy);
		assertEquals((IASTLiteralExpression) condExp.getLogicalConditionExpression(),
				(IASTLiteralExpression) copy.getLogicalConditionExpression());
		assertEquals((IASTLiteralExpression) condExp.getPositiveResultExpression(),
				(IASTLiteralExpression) copy.getPositiveResultExpression());
		assertEquals((IASTLiteralExpression) condExp.getNegativeResultExpression(),
				(IASTLiteralExpression) copy.getNegativeResultExpression());
	}

	private void assertEquals(IASTBinaryExpression binExp,
			IASTBinaryExpression copy) {
		assertFalse(binExp == copy);
		assertEquals((IASTLiteralExpression)binExp.getOperand1(), (IASTLiteralExpression)copy.getOperand1());
		assertEquals((IASTLiteralExpression)binExp.getOperand2(), (IASTLiteralExpression)copy.getOperand2());
		assertEquals(binExp.getOperator(), copy.getOperator());
	}


	private void assertEquals(IASTLiteralExpression org,
			IASTLiteralExpression copy) {
		assertFalse(org == copy);
		assertEquals(org.getKind(), copy.getKind());
		assertEquals(org.toString(), copy.toString());
	}
	

	private void assertEquals(IASTIdExpression castIdExp, IASTIdExpression copy) {
		assertFalse(castIdExp == copy);		
		assertEquals(castIdExp.getName().toString(), copy.getName().toString());
	}
	

	private IASTLiteralExpression getTestLiteralExp(ParserLanguage lang) {
		if(lang.isCPP()) {
			return new CPPASTLiteralExpression(IASTLiteralExpression.lk_integer_constant, "0");
		}else {
			return new CASTLiteralExpression(IASTLiteralExpression.lk_integer_constant, "0");
		}
	}
	
	private IASTName getTestName(ParserLanguage lang) {
		if(lang.isCPP()) {
			return new CPPASTName(new char[] {'t', 'e', 's', 't'});
		}else {
			return new CASTName(new char[] {'t', 'e', 's', 't'});
		}
	}

}
