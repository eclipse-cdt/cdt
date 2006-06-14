/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model.failedTests;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.ITypeDef;
import org.eclipse.cdt.core.model.IVariable;
import org.eclipse.cdt.core.model.tests.IntegratedCModelTest;



/**
 * @author jcamelon
 *
 */
public class FailedDeclaratorsTest extends IntegratedCModelTest
{
	// the defect to track these failures is Bug 40768  
	// Problems with nested declarators 
	static final boolean failedTest = true;
	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	public String getSourcefileSubdir() {
		return "resources/cmodel/";
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	public String getSourcefileResource() {
		return "DeclaratorsTests.cpp";
	}
	
	public void testDeclarators_0003() throws CModelException {
		  ITranslationUnit tu = getTU();
		  ICElement element = tu.getElement("decl_0003");
		  if(  failedTest ) return; // here is the where the failure is
		  assertNotNull(element); 
		  assertEquals(element.getElementType(), ICElement.C_FUNCTION_DECLARATION);
		  IFunctionDeclaration decl = (IFunctionDeclaration)element;
		  assertEquals(decl.getSignature(), "decl_0003(char)");
		  assertEquals(decl.getReturnType(), "void");
	  }
	  
	public void testDeclarators_0006() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0006");
		if(  failedTest ) return; // here is the where the failure is
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_VARIABLE);
		IVariable decl = (IVariable)element;
		assertEquals(decl.getTypeName(), "void(*)(char)");
	}
	
	public void testDeclarators_0007() throws CModelException {
		 ITranslationUnit tu = getTU();
		 ICElement element = tu.getElement("decl_0007");
		 if(  failedTest ) return; // here is the where the failure is
		 assertNotNull(element);
		 assertEquals(element.getElementType(), ICElement.C_VARIABLE);
		 IVariable decl = (IVariable)element;
		 assertEquals(decl.getTypeName(), "void(*)(char)");
	 }

	 public void testDeclarators_0011() throws CModelException {
		 ITranslationUnit tu = getTU();
		 ICElement element = tu.getElement("decl_0011");
		 assertNotNull(element);
		 assertEquals(element.getElementType(), ICElement.C_TYPEDEF);
		 ITypeDef decl = (ITypeDef)element;
		if(  failedTest ) return; // here is the where the failure is
		 assertEquals(decl.getTypeName(), "void()(char)");
	 }
    
	 public void testDeclarators_0012() throws CModelException {
		 ITranslationUnit tu = getTU();
		 ICElement element = tu.getElement("decl_0012");
		 assertNotNull(element);
		 if(  failedTest ) return; // here is the where the failure is
		 assertEquals(element.getElementType(), ICElement.C_TYPEDEF);
		 ITypeDef decl = (ITypeDef)element;
		 assertEquals(decl.getTypeName(), "void()(char)");
	 }

	 public void testDeclarators_0013() throws CModelException {
		 ITranslationUnit tu = getTU();
		 ICElement element = tu.getElement("decl_0013");
		 if(  failedTest ) return; // here is the where the failure is
		 assertNotNull(element);
		 assertEquals(element.getElementType(), ICElement.C_TYPEDEF);
		 ITypeDef decl = (ITypeDef)element;
		 assertEquals(decl.getTypeName(), "void()(char)");
	 }

	 public void testDeclarators_0014() throws CModelException {
		 ITranslationUnit tu = getTU();
		 ICElement element = tu.getElement("decl_0014");
		 assertNotNull(element);
		 assertEquals(element.getElementType(), ICElement.C_TYPEDEF);
		 ITypeDef decl = (ITypeDef)element;
		 if(  failedTest ) return; // here is the where the failure is
		 assertEquals(decl.getTypeName(), "void*()(char)");
	 }
	 
	public void testDeclarators_0016() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0016");
		if(  failedTest ) return; // here is the where the failure is
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_TYPEDEF);
		ITypeDef decl = (ITypeDef)element;
		assertEquals(decl.getTypeName(), "void(*)(char)");
	}
    
	public void testDeclarators_0017() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement element = tu.getElement("decl_0017");
		if(  failedTest ) return; // here is the where the failure is
		assertNotNull(element);
		assertEquals(element.getElementType(), ICElement.C_TYPEDEF);
		ITypeDef decl = (ITypeDef)element;
		assertEquals(decl.getTypeName(), "void(*)(char)");
	}
	
	public void testDeclarators_0023() throws CModelException {
		  ITranslationUnit tu = getTU();
		  ICElement element = tu.getElement("decl_0023");
		  if(  failedTest ) return; // here is the where the failure is
		  assertNotNull(element);
		  assertEquals(element.getElementType(), ICElement.C_FUNCTION);
		  IFunction decl = (IFunction)element;
		  assertEquals(decl.getSignature(), "decl_0023(int)");
		  assertEquals(decl.getReturnType(), "void(*(*))(char)");
	  }    

	  public void testDeclarators_0024() throws CModelException {
		  ITranslationUnit tu = getTU();
		  ICElement element = tu.getElement("decl_0024");
		  if(  failedTest ) return; // here is the where the failure is
		  assertNotNull(element);
		  assertEquals(element.getElementType(), ICElement.C_VARIABLE);
		  IVariable decl = (IVariable)element;
		  assertEquals(decl.getTypeName(), "void(*(*(*)(int))(float))(char)");
	  }    
    
	  public void testDeclarators_0031() throws CModelException {
		  ITranslationUnit tu = getTU();
		  ICElement element = tu.getElement("decl_0031");
		  assertNotNull(element);
		  assertEquals(element.getElementType(), ICElement.C_VARIABLE);
		  IVariable decl = (IVariable)element;
		  if(  failedTest ) return; // here is the where the failure is
		  assertEquals(decl.getTypeName(), "int(*)(char(*)(bool))");
	  }
}
