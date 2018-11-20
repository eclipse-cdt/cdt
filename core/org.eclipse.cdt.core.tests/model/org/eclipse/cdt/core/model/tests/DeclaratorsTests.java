/*******************************************************************************
 *  Copyright (c) 2005, 2008 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 9, 2003
 * by bnicolle
 */
package org.eclipse.cdt.core.model.tests;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.ITypeDef;
import org.eclipse.cdt.core.model.IVariable;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author bnicolle
 *
 */
public class DeclaratorsTests extends IntegratedCModelTest {
	/**
	 * @param name
	 */
	public DeclaratorsTests(String name) {
		super(name);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	@Override
	public String getSourcefileSubdir() {
		return "resources/cmodel/";
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	@Override
	public String getSourcefileResource() {
		return "DeclaratorsTests.cpp";
	}

	/**
	 * @returns a test suite named after this class
	 *          containing all its public members named "test*"
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(DeclaratorsTests.class);
		return suite;
	}

	public void testDeclarators_0001() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0001");
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_FUNCTION_DECLARATION);
		IFunctionDeclaration decl = (IFunctionDeclaration) element;
		assertEquals(decl.getSignature(), "decl_0001(char)");
		assertEquals(decl.getReturnType(), "void");
	}

	public void testDeclarators_0002() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0002");
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_FUNCTION_DECLARATION);
		IFunctionDeclaration decl = (IFunctionDeclaration) element;
		assertEquals(decl.getSignature(), "decl_0002(char)");
		assertEquals(decl.getReturnType(), "void");
	}

	public void testDeclarators_0003() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0003");
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_FUNCTION_DECLARATION);
		IFunctionDeclaration decl = (IFunctionDeclaration) element;
		assertEquals(decl.getSignature(), "decl_0003(char)");
		assertEquals(decl.getReturnType(), "void");
	}

	public void testDeclarators_0004() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0004");
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_FUNCTION_DECLARATION);
		IFunctionDeclaration decl = (IFunctionDeclaration) element;
		assertEquals(decl.getSignature(), "decl_0004(char)");
		assertEquals(decl.getReturnType(), "void*");
	}

	public void testDeclarators_0005() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0005");
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_VARIABLE);
		IVariable decl = (IVariable) element;
		assertEquals(decl.getTypeName(), "void(*)(char)");
	}

	public void testDeclarators_0006() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0006");
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_VARIABLE);
		IVariable decl = (IVariable) element;
		assertEquals(decl.getTypeName(), "void(*)(char)");
	}

	public void testDeclarators_0007() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0007");
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_VARIABLE);
		IVariable decl = (IVariable) element;
		assertEquals(decl.getTypeName(), "void(*)(char)");
	}

	public void testDeclarators_0011() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0011");
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_TYPEDEF);
		ITypeDef decl = (ITypeDef) element;
		assertEquals(decl.getTypeName(), "void(char)");
	}

	public void testDeclarators_0012() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0012");
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_TYPEDEF);
		ITypeDef decl = (ITypeDef) element;
		assertEquals(decl.getTypeName(), "void(char)");
	}

	public void testDeclarators_0013() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0013");
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_TYPEDEF);
		ITypeDef decl = (ITypeDef) element;
		assertEquals(decl.getTypeName(), "void(char)");
	}

	public void testDeclarators_0014() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0014");
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_TYPEDEF);
		ITypeDef decl = (ITypeDef) element;
		assertEquals(decl.getTypeName(), "void*(char)");
	}

	public void testDeclarators_0015() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0015");
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_TYPEDEF);
		ITypeDef decl = (ITypeDef) element;
		assertEquals(decl.getTypeName(), "void(*)(char)");
	}

	public void testDeclarators_0016() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0016");
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_TYPEDEF);
		ITypeDef decl = (ITypeDef) element;
		assertEquals(decl.getTypeName(), "void(*)(char)");
	}

	public void testDeclarators_0017() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0017");
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_TYPEDEF);
		ITypeDef decl = (ITypeDef) element;
		assertEquals(decl.getTypeName(), "void(*)(char)");
	}

	public void testDeclarators_0023() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0023");
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_FUNCTION);
		IFunction decl = (IFunction) element;
		assertEquals(decl.getSignature(), "decl_0023(int)");
		assertEquals(decl.getReturnType(), "void(**)(char)");
	}

	public void testDeclarators_0024() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0024");
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_VARIABLE);
		IVariable decl = (IVariable) element;
		assertEquals(decl.getTypeName(), "void(*(*(*)(int))(float))(char)");
	}

	public void testDeclarators_0031() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0031");
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_VARIABLE);
		IVariable decl = (IVariable) element;
		assertEquals(decl.getTypeName(), "int(*)(char(*)(bool))");
	}
}
