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

		assertAllReferences( 5 /* should be 8 */, 
			createTaskList( new Task( cl, 2 /* should be 3 */ ), new Task( method ), new Task( a ), new Task( pm ) /* should be ,new Task( f2 ) */  
				));	
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
		
		assertAllReferences( 5 /*  should be more */,
			createTaskList( new Task( cl, 2 ), new Task( method ), new Task( a /*, 2 */), new Task( pm  )/* ,new Task( f2 )*/));
		
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
		Iterator i = parse ("class A { int m; }; \n A a; int A::*pm; \n int f(){} \n int f(int); \n int x = f(a.*pm);").getDeclarations();
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTVariable pm  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 4 /*should be 5 */, createTaskList( new Task( cl /* , 2 */ ), new Task( a), new Task( pm), new Task( f2)));
	}

	// Kind PM_ARROWSTAR          
	public void testPMArrowStar_bug43579() throws Exception
	{
		Iterator i = parse ("class A { int m; }; \n A * a; int A::*pm; \n int f(){} \n int f(int); \n int x = f(a->*pm);").getDeclarations();
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTVariable pm  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 4 /*should be 5 */, createTaskList( new Task( cl /* , 2 */ ), new Task( a), new Task( pm), new Task( f2)));
	}
}
