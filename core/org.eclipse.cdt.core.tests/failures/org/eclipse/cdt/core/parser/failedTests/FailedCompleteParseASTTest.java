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
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTScope;
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
			parse ("class A { int m(int); }; \n A a; int A::*pm = &A::m; \n int f(){} \n int f(int); \n int x = f((a.*pm)(5));");
		} catch ( ParserException e ){
			assertTrue( e.getMessage().equals( "FAILURE" ) );
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
			parse ("class A { int m(int); }; \n A * a; int A::*pm = &A::m; \n int f(){} \n int f(int); \n int x = f((a->*pm)(5));");
		} catch ( ParserException e ){
			assertTrue( e.getMessage().equals( "FAILURE" ) );
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
		Iterator i = parse ("int m(int); \n int *pm = &m; \n int f(){} \n int f(int); \n int x = f((*pm)(5));").getDeclarations();
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
			parse ( "class A { int m; }; \n A a; int A::*pm; \n int f(){} \n int f(int); \n int x = f(a.*pm);" );
		} catch ( ParserException e ){
			assertTrue( e.getMessage().equals( "FAILURE" ) );
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
			parse ("class A { int m; }; \n A * a; int A::*pm; \n int f(){} \n int f(int); \n int x = f(a->*pm);");
		} catch ( ParserException e ){
			assertTrue( e.getMessage().equals( "FAILURE" ) );
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

	public void testErrorHandling_1() throws Exception
	{
		Iterator i = parse( "A anA; int x = c; class A {}; A * anotherA = &anA; int b;", false ).getDeclarations();
		IASTVariable x = (IASTVariable)i.next();
		assertEquals( x.getName(), "x");
		IASTClassSpecifier A = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable anotherA = (IASTVariable)i.next();
		assertFalse(i.hasNext()); // should be true
		// this variable is skipped because of wrong error handling
//		IASTVariable b = (IASTVariable)i.next();
//		assertEquals( b.getName(), "b");
//		assertFalse(i.hasNext());
	}

	public void testBug44340() throws Exception {
	  try {
	  	// inline function with reference to variables declared after them
		IASTScope scope = parse ("class A{ int getX() {return x[1];} int x[10];};");
		Iterator i = scope.getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		Iterator j = getDeclarations(classA);
		IASTMethod g = (IASTMethod)j.next();
		IASTField x = (IASTField)j.next();
		assertFalse(j.hasNext());
		assertAllReferences( 1, createTaskList( new Task( x )));
		
	  } catch (ParserException e){
		// parsing fails for now	  	
	  }
		
		
	}
	
	public void testBug47926() throws Exception{
		StringBuffer buffer = new StringBuffer();
		buffer.append( "void f () {} \n" );
		buffer.append( "class A { }; \n" );
		buffer.append( "void main() { A * a = new A();  a->f(); } ");
		
		Iterator i = parse( buffer.toString() ).getDeclarations();
		
		IASTFunction f = (IASTFunction) i.next();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		
		IASTFunction main = (IASTFunction) i.next();
		
		Iterator fnIter = getDeclarations( main );
		IASTVariable a = (IASTVariable) fnIter.next();
		
		//there should be no reference to f, but there is
		//assertAllReferences( 3, createTaskList( new Task( classA, 2 ), new Task( a ) ) );
		assertAllReferences( 4, createTaskList( new Task( classA, 2 ), new Task( a ), new Task( f ) ) );
	}
}
