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

import java.io.StringWriter;
import java.util.Iterator;

import junit.framework.AssertionFailedError;

import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.tests.CompleteParseBaseTest;
import org.eclipse.cdt.internal.core.parser.ParserException;
/**
 * @author jcamelon
 *
 */
public class FailedCompleteParseASTTest extends CompleteParseBaseTest
{
    /**
     * 
     */
    public FailedCompleteParseASTTest()
    {
        super();
    }
    /**
     * @param name
     */
    public FailedCompleteParseASTTest(String name)
    {
        super(name);
    }
	
	public void testPMDotStarPointerToMemberFunction_Bug43242() throws Exception
	{
		//parse no longer passes
		try{
			parse ("class A { int m(int); }; \n A a; int A::*pm = &A::m; \n int f(){} \n int f(int); \n int x = f((a.*pm)(5));"); //$NON-NLS-1$
			fail();
		} catch ( ParserException e ){
			assertTrue( e.getMessage().equals( "FAILURE" ) ); //$NON-NLS-1$
		}
//		Iterator i = parse ("class A { int m(int); }; \n A a; int A::*pm = &A::m; \n int f(){} \n int f(int); \n int x = f((a.*pm)(5));").getDeclarations();
//		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
//		Iterator members = getDeclarations(cl);
//		IASTMethod method = (IASTMethod)members.next();
//		IASTVariable a  = (IASTVariable) i.next();
//		IASTVariable pm  = (IASTVariable) i.next();
//		IASTFunction f1 = (IASTFunction) i.next();
//		IASTFunction f2 = (IASTFunction) i.next();
//		IASTVariable x  = (IASTVariable) i.next();
//
//		assertAllReferences( 5 /* should be 8 */, 
//			createTaskList( new Task( cl, 2 /* should be 3 */ ), new Task( method ), new Task( a ), new Task( pm ) /* should be ,new Task( f2 ) */  
//				));	
	}
	public void testPMArrowStarPointerToMemberFunction_Bug43242() throws Exception
	{
		//parse no longer passes
		try{
			parse ("class A { int m(int); }; \n A * a; int A::*pm = &A::m; \n int f(){} \n int f(int); \n int x = f((a->*pm)(5));"); //$NON-NLS-1$
			fail();
		} catch ( ParserException e ){
			assertTrue( e.getMessage().equals( "FAILURE" ) ); //$NON-NLS-1$
		}
//		Iterator i = parse ("class A { int m(int); }; \n A * a; int A::*pm = &A::m; \n int f(){} \n int f(int); \n int x = f((a->*pm)(5));").getDeclarations();
//		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
//		Iterator members = getDeclarations(cl);
//		IASTMethod method = (IASTMethod)members.next();
//		IASTVariable a  = (IASTVariable) i.next();
//		IASTVariable pm  = (IASTVariable) i.next();
//		IASTFunction f1 = (IASTFunction) i.next();
//		IASTFunction f2 = (IASTFunction) i.next();
//		IASTVariable x  = (IASTVariable) i.next();
//		
//		assertAllReferences( 5 /*  should be more */,
//			createTaskList( new Task( cl, 2 ), new Task( method ), new Task( a /*, 2 */), new Task( pm  )/* ,new Task( f2 )*/));
//		
	}  
	public void testUnaryStarCastexpressionPointerToFunction_Bug43241() throws Exception
	{
		Iterator i = parse ("int m(int); \n int *pm = &m; \n int f(){} \n int f(int); \n int x = f((*pm)(5));").getDeclarations(); //$NON-NLS-1$
		IASTFunction m = (IASTFunction) i.next();
		IASTVariable pm  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 2 /* should be 3 */, 
			createTaskList( new Task( m ), new Task( pm ) /* ,new Task( f2 )*/));
	}
	
	// Kind DELETE_CASTEXPRESSION        
	// Kind DELETE_VECTORCASTEXPRESSION  
	// Kind CASTEXPRESSION               
	// Kind PM_DOTSTAR                   
	public void testPMDotStar_bug43579() throws Exception
	{
		//parse no longer passes
		try{
			parse ( "class A { int m; }; \n A a; int A::*pm; \n int f(){} \n int f(int); \n int x = f(a.*pm);" ); //$NON-NLS-1$
			fail();
		} catch ( ParserException e ){
			assertTrue( e.getMessage().equals( "FAILURE" ) ); //$NON-NLS-1$
		}
//		Iterator i = parse ("class A { int m; }; \n A a; int A::*pm; \n int f(){} \n int f(int); \n int x = f(a.*pm);").getDeclarations();
//		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
//		IASTVariable a  = (IASTVariable) i.next();
//		IASTVariable pm  = (IASTVariable) i.next();
//		IASTFunction f1 = (IASTFunction) i.next();
//		IASTFunction f2 = (IASTFunction) i.next();
//		IASTVariable x  = (IASTVariable) i.next();
//		assertFalse( i.hasNext() );
//		assertAllReferences( 4 /*should be 5 */, createTaskList( new Task( cl /* , 2 */ ), new Task( a), new Task( pm), new Task( f2)));
	}

	// Kind PM_ARROWSTAR          
	public void testPMArrowStar_bug43579() throws Exception
	{
		//parse no longer passes
		try{
			parse ("class A { int m; }; \n A * a; int A::*pm; \n int f(){} \n int f(int); \n int x = f(a->*pm);"); //$NON-NLS-1$
			fail();
		} catch ( ParserException e ){
			assertTrue( e.getMessage().equals( "FAILURE" ) ); //$NON-NLS-1$
		}
//		Iterator i = parse ("class A { int m; }; \n A * a; int A::*pm; \n int f(){} \n int f(int); \n int x = f(a->*pm);").getDeclarations();
//		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
//		IASTVariable a  = (IASTVariable) i.next();
//		IASTVariable pm  = (IASTVariable) i.next();
//		IASTFunction f1 = (IASTFunction) i.next();
//		IASTFunction f2 = (IASTFunction) i.next();
//		IASTVariable x  = (IASTVariable) i.next();
//		assertFalse( i.hasNext() );
//		assertAllReferences( 4 /*should be 5 */, createTaskList( new Task( cl /* , 2 */ ), new Task( a), new Task( pm), new Task( f2)));
	}
	
	
	public void testUsingOverloadedName_bug71317() throws Exception {
		// using a globaly defined function overloaded in a namespace
		try {
			parse("int foo(int); \n namespace NS { \n int foo(int); \n using ::foo; \n } \n");//$NON-NLS-1$
			fail();
		} catch ( ParserException e ){
			assertTrue( e.getMessage().equals( "FAILURE" ) ); //$NON-NLS-1$
		}
//		Iterator i = parse("int foo(); \n namespace NS { int bar(); \n using ::foo; \n } \n").getDeclarations();//$NON-NLS-1$
//		IASTFunction fd1 = (IASTFunction) i.next();
//		IASTNamespaceDefinition nd = (IASTNamespaceDefinition) i.next();
//		assertFalse(i.hasNext());
//		Iterator j = nd.getDeclarations();
//		IASTFunction fd2 = (IASTFunction) j.next();
//		IASTUsingDeclaration ud = (IASTUsingDeclaration) j.next();
//		assertFalse(j.hasNext());
	}
	
	public void testParametrizedTypeDefinition_bug69751() throws Exception {
		try {
			// a typedef refers to an unknown type in a template parameter
			parse("template <typename T> \n class A { \n typedef typename T::size_type size_type; \n void foo() { size_type i; } \n }; \n");//$NON-NLS-1$
			fail();
		} catch (ParserException e) {
			assertTrue( e.getMessage().equals( "FAILURE" ) ); //$NON-NLS-1$
		}
//		Iterator i = parse("template <typename T> \n class A { \n typedef typename T::size_type size_type; \n void foo() { size_type i; } \n }; \n").getDeclarations();//$NON-NLS-1$
//		IASTTemplateDeclaration td = (IASTTemplateDeclaration)i.next();
//		assertFalse(i.hasNext());
//		IASTClassSpecifier cs = (IASTClassSpecifier) td.getOwnedDeclaration();
//		Iterator j = cs.getDeclarations();
//		IASTTypedefDeclaration tdd = (IASTTypedefDeclaration) j.next();
//		IASTMethod m = (IASTMethod) j.next();
//		assertFalse(j.hasNext());
//		Iterator k = m.getDeclarations();
//		IASTVariable v = (IASTVariable) k.next();
//		assertFalse(k.hasNext());
	}
	

	
	public void testGNUExternalTemplate_bug71603() throws Exception {
		try {
			parse("template <typename T> \n class A {}; \n extern template class A<int>; \n"); //$NON-NLS-1$
			fail();
		} catch (ParserException e) {
			assertTrue( e.getMessage().equals( "FAILURE" ) ); //$NON-NLS-1$
		}
//		Iterator i = parse("template <typename T> \n class A {}; \n extern template class A<int>; \n").getDeclarations();
//		IASTTemplateDeclaration td = (IASTTemplateDeclaration) i.next();
//		IASTClassSpecifier cs = (IASTClassSpecifier) td.getOwnedDeclaration();
//		IASTTemplateInstantiation ti = (IASTTemplateInstantiation) i.next();
//		assertFalse(i.hasNext());
	}
}
