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
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;

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
    
    // int globalVar;

	// // don't include header
    // char globalVar;
    public void _testAstIndexConflictVariable_Bug195127() throws Exception {
    	fakeFailForMultiProject();
		IBinding b0 = getBindingFromASTName("globalVar;", 9);
		assertTrue(b0 instanceof IVariable);
		IVariable v= (IVariable) b0;
		IType type= v.getType();
		assertTrue(type instanceof IBasicType);
		assertTrue(((IBasicType) type).getType() == IBasicType.t_char);
    }

    // int globalFunc();

	// // don't include header
    // char globalFunc();
    public void _testAstIndexConflictFunction_Bug195127() throws Exception {
    	fakeFailForMultiProject();
		IBinding b0 = getBindingFromASTName("globalFunc(", 10);
		assertTrue(b0 instanceof IFunction);
		IFunction f= (IFunction) b0;
		IType type= f.getType().getReturnType();
		assertTrue(type instanceof IBasicType);
		assertTrue(((IBasicType) type).getType() == IBasicType.t_char);
    }

    // struct astruct {
    //    int member;
    // };

	// // don't include header
    // struct astruct {
    //    char member;
    //    int additionalMember;
    // };
    public void _testAstIndexConflictStruct_Bug195127() throws Exception {
    	fakeFailForMultiProject();
		IBinding b0 = getBindingFromASTName("astruct", 7);
		assertTrue(b0 instanceof ICompositeType);
		ICompositeType ct= (ICompositeType) b0;
		IField[] fields= ct.getFields();
		assertEquals(2, fields.length);
		IField member= fields[0];
		IField additionalMember= fields[1];
		if (member.getName().equals("additionalMember")) {
			IField h= member; member= additionalMember; additionalMember= h;
		}
		assertEquals("member", member.getName());
		assertEquals("additionalMember", additionalMember.getName());
		IType type= member.getType();
		assertTrue(type instanceof IBasicType);
		assertTrue(((IBasicType) type).getType() == IBasicType.t_char);
    }

    // enum anenum {
    //    eItem0
    // };

	// // don't include header
    // enum anenum {
    //    eItem0, eItem1
    // };
    public void _testAstIndexConflictEnumerator_Bug195127() throws Exception {
    	fakeFailForMultiProject();
		IBinding b0 = getBindingFromASTName("anenum", 7);
		assertTrue(b0 instanceof IEnumeration);
		IEnumeration enumeration= (IEnumeration) b0;
		IEnumerator[] enumerators= enumeration.getEnumerators();
		assertEquals(2, enumerators.length);
    }

    // typedef int atypedef;

	// // don't include header
    // typedef char atypedef;
    public void testAstIndexConflictTypedef_Bug195127() throws Exception {
    	fakeFailForMultiProject();
		IBinding b0 = getBindingFromASTName("atypedef;", 8);
		assertTrue(b0 instanceof ITypedef);
		ITypedef t= (ITypedef) b0;
		IType type= t.getType();
		assertTrue(type instanceof IBasicType);
		assertTrue(((IBasicType) type).getType() == IBasicType.t_char);
    }

    // struct st_20070703 {
    //    int member;
    // };

	// #include "header.h"
    // struct st_20070703;
    // void func(struct st_20070703* x) {
    //    x->member= 0;
    // }
    public void _testAstIndexConflictStruct_Bug195227() throws Exception {
    	fakeFailForMultiProject();
		IBinding b0 = getBindingFromASTName("member=", 6);
		assertTrue(b0 instanceof IField);
    }

}
