/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;
import org.eclipse.cdt.core.index.IIndexBinding;

/**
 * For testing PDOM binding resolution
 */
public class IndexCBindingResolutionBugs extends IndexBindingResolutionTestBase {

	public static class SingleProject extends IndexCBindingResolutionBugs {
		public SingleProject() {setStrategy(new SinglePDOMTestStrategy(false));}
		public static TestSuite suite() {return suite(SingleProject.class);}
	}
	public static class ProjectWithDepProj extends IndexCBindingResolutionBugs {
		public ProjectWithDepProj() {setStrategy(new ReferencedProject(false));}
		public static TestSuite suite() {return suite(ProjectWithDepProj.class);}
	}
	
	public static void addTests(TestSuite suite) {		
		suite.addTest(SingleProject.suite());
		suite.addTest(ProjectWithDepProj.suite());
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
		assertInstance(type, ITypedef.class);
		type= ((ITypedef) type).getType();
		assertInstance(type, ICompositeType.class);
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
		assertInstance(type, ITypedef.class);
		type= ((ITypedef) type).getType();
		assertInstance(type, IEnumeration.class);
		assertTrue(type instanceof IEnumeration);
    }
    
    // int globalVar;

	// // don't include header
    // char globalVar;
    public void testAstIndexConflictVariable_Bug195127() throws Exception {
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
    public void testAstIndexConflictFunction_Bug195127() throws Exception {
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
    public void testAstIndexConflictStruct_Bug195127() throws Exception {
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
    public void testAstIndexConflictEnumerator_Bug195127() throws Exception {
    	fakeFailForMultiProject();
		IBinding b0 = getBindingFromASTName("anenum", 6);
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
    public void testAstIndexStructFwdDecl_Bug195227() throws Exception {
		IBinding b0 = getBindingFromASTName("member=", 6);
		assertTrue(b0 instanceof IField);
    }

    // struct astruct {
    //    int member;
    // };
    // enum anenum {
    //    eItem0
    // };

	// #include "header.h"
    // struct astruct;
    // enum anenum;
    // void func(struct astruct a, enum anenum b) {
    // }
    public void testAstIndexFwdDecl_Bug195227() throws Exception {
		IBinding b0 = getBindingFromASTName("astruct;", 7);
		IBinding b1 = getBindingFromASTName("anenum;", 6);
		assertTrue(b0 instanceof ICompositeType);
		ICompositeType t= (ICompositeType) b0;
		IField[] f= t.getFields();
		assertEquals(1, f.length);
		assertTrue(b1 instanceof IEnumeration);
		IEnumeration e= (IEnumeration) b1;
		IEnumerator[] ei= e.getEnumerators();
		assertEquals(1, ei.length);

		b0 = getBindingFromASTName("astruct a", 7);
		b1 = getBindingFromASTName("anenum b", 6);
		assertTrue(b0 instanceof ICompositeType);
		t= (ICompositeType) b0;
		f= t.getFields();
		assertEquals(1, f.length);
		assertTrue(b1 instanceof IEnumeration);
		e= (IEnumeration) b1;
		ei= e.getEnumerators();
		assertEquals(1, ei.length);
    } 

    // // no header needed
    
    // typedef struct {
    //    int member;
    // } t_struct;
    // typedef union {
    //    int member;
    // } t_union;
    // typedef enum {
    //    ei
    // } t_enum;
	public void testIsSameAnonymousType_Bug193962() throws DOMException {
		// struct
		IBinding tdAST = getBindingFromASTName("t_struct;", 8);
		assertFalse(tdAST instanceof IIndexBinding);
		IBinding tdIndex= strategy.getIndex().adaptBinding(tdAST);
		assertTrue(tdIndex instanceof IIndexBinding);
		assertTrue(tdAST instanceof ITypedef);
		assertTrue(tdIndex instanceof ITypedef);
		
		IType tAST= ((ITypedef) tdAST).getType();
		IType tIndex= ((ITypedef) tdIndex).getType();
		assertTrue(tAST instanceof ICompositeType);
		assertTrue(tIndex instanceof ICompositeType);
		assertTrue(tAST.isSameType(tIndex));
		assertTrue(tIndex.isSameType(tAST));

		// union
		tdAST = getBindingFromASTName("t_union;", 7);
		assertFalse(tdAST instanceof IIndexBinding);
		tdIndex= strategy.getIndex().adaptBinding(tdAST);
		assertTrue(tdIndex instanceof IIndexBinding);
		assertTrue(tdAST instanceof ITypedef);
		assertTrue(tdIndex instanceof ITypedef);
		
		tAST= ((ITypedef) tdAST).getType();
		tIndex= ((ITypedef) tdIndex).getType();
		assertTrue(tAST instanceof ICompositeType);
		assertTrue(tIndex instanceof ICompositeType);
		assertTrue(tAST.isSameType(tIndex));
		assertTrue(tIndex.isSameType(tAST));

		// enum
		tdAST = getBindingFromASTName("t_enum;", 6);
		assertFalse(tdAST instanceof IIndexBinding);
		tdIndex= strategy.getIndex().adaptBinding(tdAST);
		assertTrue(tdIndex instanceof IIndexBinding);
		assertTrue(tdAST instanceof ITypedef);
		assertTrue(tdIndex instanceof ITypedef);
		
		tAST= ((ITypedef) tdAST).getType();
		tIndex= ((ITypedef) tdIndex).getType();
		assertTrue(tAST instanceof IEnumeration);
		assertTrue(tIndex instanceof IEnumeration);
		assertTrue(tAST.isSameType(tIndex));
		assertTrue(tIndex.isSameType(tAST));
	}
	
	// struct outer {
	//    union {
	//       int var1;
	//    };
	// };
	  
	// #include "header.h"
	// void test() {
	//    struct outer x;
	//    x.var1=1;
	// }
	public void testAnonymousUnion_Bug216791() throws DOMException {
		// struct
		IBinding b = getBindingFromASTName("var1=", 4);
		assertTrue(b instanceof IField);
		IField f= (IField) b;
		IScope outer= f.getCompositeTypeOwner().getScope();
		assertTrue(outer instanceof ICCompositeTypeScope);
		assertEquals("outer", outer.getScopeName().toString());
	}

	// union outer {
	//    struct {
	//       int var1;
	//    };
	// };
	  
	// #include "header.h"
	// void test() {
	//    union outer x;
	//    x.var1=1;
	// }
	public void testAnonymousStruct_Bug216791() throws DOMException {
		// struct
		IBinding b = getBindingFromASTName("var1=", 4);
		assertTrue(b instanceof IField);
		IField f= (IField) b;
		IScope outer= f.getCompositeTypeOwner().getScope();
		assertTrue(outer instanceof ICCompositeTypeScope);
		assertEquals("outer", outer.getScopeName().toString());
	}
	
	// int myFunc();
	
	// int myFunc(var)
	// int var; 
	// { 
	//   return var; 
	// } 
	// int main(void) {
	//    return myFunc(0);
	// }
	public void testKRStyleFunction_Bug216791() throws DOMException {
		// struct
		IBinding b = getBindingFromASTName("myFunc(", 6);
		assertTrue(b instanceof IFunction);
		IFunction f= (IFunction) b;
		IParameter[] params= f.getParameters();
		assertEquals(1, params.length);
		assertTrue(params[0].getType() instanceof IBasicType);
		assertEquals(IBasicType.t_int, ((IBasicType)params[0].getType()).getType());
	}
	
	//	typedef struct S S;
	//	void setValue(S *pSelf, int value);

	//	struct S {
	//		int value;
	//	};
	//	void setValue(S *pSelf, int value) {
	//		pSelf->value = value;
	//	}
	public void testOpaqueStruct_Bug262719() throws Exception {
		IBinding b = getBindingFromASTName("value =", 5);
		assertTrue(b instanceof IField);
	}
	
	//	void f255(
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int);
	//	void f256(
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int);
	
	//	void test() {	
	//     f255(
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
	//     f256(
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
	//	}
	public void testFunctionsWithManyParameters_Bug319186() throws Exception {
		getBindingFromASTName("f255", 0);
		getBindingFromASTName("f256", 0);
	}		
}
