/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.failedTests;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.tests.CompleteParseBaseTest;

/**
 * @author jcamelon
 *
 */
public class FailedCompleteParseASTExpressionTest extends CompleteParseBaseTest
{
    /**
     * 
     */
    public FailedCompleteParseASTExpressionTest()
    {
        super();
    }
    /**
     * @param name
     */
    public FailedCompleteParseASTExpressionTest(String name)
    {
        super(name);
    }
    
//	public void testPostfixSubscriptA_Bug43238() throws Exception
//	{
//		Iterator i = parse ("int pa[10][5] ; \n int f(int ia){} \n int f(void); \n int x = f(pa[1][2]);").getDeclarations();
//		IASTVariable pa  = (IASTVariable) i.next();
//		IASTFunction f1 = (IASTFunction) i.next();
//		IASTFunction f2 = (IASTFunction) i.next();
//		IASTVariable x  = (IASTVariable) i.next();
//		Iterator references = callback.getReferences().iterator();
//		assertEquals( callback.getReferences().size(), 1 ); // should be = 2
//		assertEquals( ((IASTReference)references.next()).getReferencedElement(), pa );
//		//assertEquals( ((IASTReference)references.next()).getReferencedElement(), f1 );
//	}
//	public void testPostfixSubscriptB_Bug43238() throws Exception
//	{
//		Iterator i = parse ("int* pa[10][5] ; \n int f(int* ia){} \n int f(void); \n int x = f(pa[1][2]);").getDeclarations();
//		IASTVariable pa  = (IASTVariable) i.next();
//		IASTFunction f1 = (IASTFunction) i.next();
//		IASTFunction f2 = (IASTFunction) i.next();
//		IASTVariable x  = (IASTVariable) i.next();
//		Iterator references = callback.getReferences().iterator();
//		assertEquals( callback.getReferences().size(), 1 ); // should be = 2
//		assertEquals( ((IASTReference)references.next()).getReferencedElement(), pa );
//		//assertEquals( ((IASTReference)references.next()).getReferencedElement(), f1 );
//	}
//	public void testPostfixSubscriptWithReferences_Bug43238() throws Exception
//	{
//		Iterator i = parse ("class A{}; \n A *pa[10][5] ; \n int f(A* ia){} \n int f(void); \n int x = f(pa[1][2]);").getDeclarations();
//		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
//		IASTVariable pa  = (IASTVariable) i.next();
//		IASTFunction f1 = (IASTFunction) i.next();
//		IASTFunction f2 = (IASTFunction) i.next();
//		IASTVariable x  = (IASTVariable) i.next();
//		Iterator references = callback.getReferences().iterator();
//		assertEquals( ((IASTReference) references.next()).getReferencedElement(), cl );
//		assertEquals( ((IASTReference) references.next()).getReferencedElement(), cl );
//		assertEquals( ((IASTReference)references.next()).getReferencedElement(), pa );
//		assertEquals( ((IASTReference)references.next()).getReferencedElement(), f1 );
//	}	
	public void testConditionalExpression_Bug43159() throws Exception { 
		Iterator i = parse( "int foo(bool); int foo(int); int a = 10, b = 4, c = 2; int x = foo( a > 5 ? b : c );").getDeclarations();
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable c = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 3 ); // should be 4
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), c ); 
		//assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}
	
	public void testConditionalExpressionWithReferencesB_Bug43106() throws Exception { 
		Iterator i = parse( "class A{}; class B : public A{}; int foo(); int foo(A&); A a ; B b; int c = 0; int x = foo( c > 5 ? b : a );").getDeclarations();
		IASTClassSpecifier cla = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();		
		IASTClassSpecifier clb = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();		
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable c = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 7 ); // should be 8
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), cla );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), cla );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), cla );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), clb );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), c ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		//assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}
	public void testPMDotStarPointerToMemberFunction_Bug43242() throws Exception
	{
		Iterator i = parse ("class A { int m(int); }; \n A a; int A::*pm = &A::m; \n int f(){} \n int f(int); \n int x = f((a.*pm)(5));").getDeclarations();
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		Iterator members = getDeclarations(cl);
		IASTMethod method = (IASTMethod)members.next();
		IASTVariable a  = (IASTVariable) i.next();
		IASTVariable pm  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		assertEquals( callback.getReferences().size(), 5 ); // should be 6
		assertEquals( ((IASTReference) references.next()).getReferencedElement(), cl );
		assertEquals( ((IASTReference) references.next()).getReferencedElement(), cl );
		assertEquals( ((IASTReference) references.next()).getReferencedElement(), method );
		assertEquals( ((IASTReference) references.next()).getReferencedElement(), a );
		assertEquals( ((IASTReference) references.next()).getReferencedElement(), pm );
//		assertEquals( ((IASTReference) references.next()).getReferencedElement(), f2 );				
	}
	public void testPMArrowStarPointerToMemberFunction_Bug43242() throws Exception
	{
		Iterator i = parse ("class A { int m(int); }; \n A * a; int A::*pm = &A::m; \n int f(){} \n int f(int); \n int x = f((a->*pm)(5));").getDeclarations();
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		Iterator members = getDeclarations(cl);
		IASTMethod method = (IASTMethod)members.next();
		IASTVariable a  = (IASTVariable) i.next();
		IASTVariable pm  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		assertEquals( callback.getReferences().size(), 5 ); // should be 6
		assertEquals( ((IASTReference) references.next()).getReferencedElement(), cl );
		assertEquals( ((IASTReference) references.next()).getReferencedElement(), cl );
		assertEquals( ((IASTReference) references.next()).getReferencedElement(), method );
		assertEquals( ((IASTReference) references.next()).getReferencedElement(), a );
		assertEquals( ((IASTReference) references.next()).getReferencedElement(), pm );
//		assertEquals( ((IASTReference) references.next()).getReferencedElement(), f2 );				
	}  
	public void testUnaryStarCastexpressionPointerToFunction_Bug43241() throws Exception
	{
		Iterator i = parse ("int m(int); \n int *pm = &m; \n int f(){} \n int f(int); \n int x = f((*pm)(5));").getDeclarations();
		IASTFunction m = (IASTFunction) i.next();
		IASTVariable pm  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		assertEquals( callback.getReferences().size(), 2 ); // should be 3
		assertEquals( ((IASTReference) references.next()).getReferencedElement(), m );
		assertEquals( ((IASTReference) references.next()).getReferencedElement(), pm );
//		assertEquals( ((IASTReference) references.next()).getReferencedElement(), f2 );				
	}
	    
}
