/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * For testing PDOM binding resolution
 */
public class IndexCBindingResolutionBugs extends IndexBindingResolutionTestBase {

	public static class SingleProject extends IndexCBindingResolutionBugs {
		public SingleProject() {setStrategy(new SinglePDOMTestStrategy(false));}
	}
	public static class ProjectWithDepProj extends IndexCBindingResolutionBugs {
		public ProjectWithDepProj() {setStrategy(new ReferencedProject(false));}
	}
	
	public static void addTests(TestSuite suite) {		
		suite.addTest(suite(SingleProject.class));
		suite.addTest(suite(ProjectWithDepProj.class));
	}
	
	
	// #include <stdio.h>	
	// void func1(void)
	//	{
	//	    int i = 0;
	//	    for (i=0; i<10;i++)
	//	    {
	//	        printf("%i", i);
	//	    }
	//
	//	}
	
	//  #include "header.h"
	//
	//	int main(void)
	//	{
	//	    while (1)
	//	    {
	//	        func1();
	//	    }
	//	    return 0;
	//	}
	public void testBug175267() throws DOMException {
		IBinding b0 = getBindingFromASTName("func1()", 5);
		assertTrue(b0 instanceof IFunction);
		IFunction f0 = (IFunction) b0;
		IParameter[] params= f0.getParameters();
		assertEquals(1, params.length);
		IType param= params[0].getType();
		assertTrue(param instanceof IBasicType);
		IType returnType= f0.getType().getReturnType();
		assertTrue(returnType instanceof IBasicType);
	}

	//  void func1(void);
	
	//  #include "header.h"
	//
	//	int main(void)
	//	{
	//      void* v= func1;
	//	}
	public void testBug181735() throws DOMException {
		IBinding b0 = getBindingFromASTName("func1;", 5);
		assertTrue(b0 instanceof IFunction);
	}
	
    // typedef struct {
    //    int utm;
    // } usertype;
    // void func(usertype t);

	// #include "header.h"
    // void test() {
	//    usertype ut;
	//    func(ut);
    // }
    public void testFuncWithTypedefForAnonymousStruct_190730() throws Exception {
		IBinding b0 = getBindingFromASTName("func(", 4);
		assertTrue(b0 instanceof IFunction);
		IFunction f= (IFunction) b0;
		IParameter[] pars= f.getParameters();
		assertEquals(1, pars.length);
		IType type= pars[0].getType();
		assertTrue(type instanceof ICompositeType);
    }

    // typedef enum {
    //    eItem
    // } userEnum;
    // void func(userEnum t);

	// #include "header.h"
    // void test() {
	//    userEnum ut;
	//    func(ut);
    // }
    public void testFuncWithTypedefForAnonymousEnum_190730() throws Exception {
		IBinding b0 = getBindingFromASTName("func(", 4);
		assertTrue(b0 instanceof IFunction);
		IFunction f= (IFunction) b0;
		IParameter[] pars= f.getParameters();
		assertEquals(1, pars.length);
		IType type= pars[0].getType();
		assertTrue(type instanceof IEnumeration);
//		type= ((ITypedef) type).getType();
//		assertTrue(type instanceof IEnumeration);
    }
}
