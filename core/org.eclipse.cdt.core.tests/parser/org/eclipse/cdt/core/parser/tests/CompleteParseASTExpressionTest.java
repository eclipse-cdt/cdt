/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTFunctionReference;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTVariableReference;

/**
 * @author hamer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CompleteParseASTExpressionTest extends CompleteParseBaseTest{
	
	public CompleteParseASTExpressionTest(String a)
	{
		super(a);
	}
	// IASTExpression.Kind.PRIMARY_INTEGER_LITERAL 
	public void testExpressionResultValueWithSimpleTypes1() throws Exception
	{
		Iterator i = parse ("int f(int, int); \n int f(int); \n int x = f(1, 2+3);").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f1 );
		 
	}	
	// IASTExpression.Kind.PRIMARY_CHAR_LITERAL 
	public void testExpressionResultValueWithSimpleTypes2() throws Exception
	{
		Iterator i = parse ("int f(char, int); \n int f(char); \n int x = f('c');").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f2 );
		 
	}	
	// IASTExpression.Kind.PRIMARY_FLOAT_LITERAL 
	public void testExpressionResultValueWithSimpleTypes3() throws Exception
	{
		Iterator i = parse ("int f(char); \n int f(float); \n int x = f(1.13);").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f2 );
		 
	}	
	// IASTExpression.Kind.PRIMARY_STRING_LITERAL 
	public void testExpressionResultValueWithSimpleTypes4() throws Exception
	{
		Iterator i = parse ("int f(char); \n int f(char*); \n int x = f(\"str\");").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f2 );
		 
	}	
	// IASTExpression.Kind.PRIMARY_BOOLEAN_LITERAL 
	public void testExpressionResultValueWithSimpleTypes5() throws Exception
	{
		Iterator i = parse ("int f(bool); \n int f(float); \n int x = f(true);").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f1 );
		 
	}	
	// IASTExpression.Kind.PRIMARY_EMPTY 
	public void testExpressionResultValueWithSimpleTypes6() throws Exception
	{
		Iterator i = parse ("int f(char); \n int f(void); \n int x = f();").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f2 );
		 
	}	
	// IASTExpression.Kind.ID_EXPRESSION
	public void testExpressionResultValueWithReferenceTypes() throws Exception
	{
		Iterator i = parse ("class A{}a;  \n int f(A a); \n int f(void); \n int x = f(a);").getDeclarations();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTClassReference clr1 = (IASTClassReference) references.next();
		IASTVariableReference ar1 = (IASTVariableReference) references.next();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( ar1.getReferencedElement(), a );
		assertEquals( fr1.getReferencedElement(), f1 );
	}	
	// IASTExpression.Kind.UNARY_STAR_CASTEXPRESSION	
	public void testExpressionResultValueWithReferenceTypesAndPointers1() throws Exception
	{
		Iterator i = parse ("class A {}; \n A * pa; \n int f(A ia){} \n int f(void); \n int x = f(*pa);").getDeclarations();
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTClassReference clr1 = (IASTClassReference) references.next();
		IASTClassReference clr2 = (IASTClassReference) references.next();
		IASTVariableReference ar1 = (IASTVariableReference) references.next();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( clr1.getReferencedElement(), cl );
		assertEquals( ar1.getReferencedElement(), a );
		assertEquals( fr1.getReferencedElement(), f1 );
		
	}
	// IASTExpression.Kind.ID_EXPRESSION ( refers to a pointer )
	public void testExpressionResultValueWithReferenceTypesAndPointers2() throws Exception
	{
		Iterator i = parse ("class A {}; \n A * pa; \n int f(A *ia){} \n int f(void); \n int x = f(pa);").getDeclarations();
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTClassReference clr1 = (IASTClassReference) references.next();
		IASTClassReference clr2 = (IASTClassReference) references.next();
		IASTVariableReference ar1 = (IASTVariableReference) references.next();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( clr1.getReferencedElement(), cl );
		assertEquals( ar1.getReferencedElement(), a );
		assertEquals( fr1.getReferencedElement(), f1 );
	}
	// IASTExpression.Kind.UNARY_AMPSND_CASTEXPRESSION
	public void testExpressionResultValueWithReferenceTypesAndPointers3() throws Exception
	{
		Iterator i = parse ("class A {}; \n A * pa; \n int f(A ** ia){} \n int f(void); \n int x = f(&pa);").getDeclarations();
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTClassReference clr1 = (IASTClassReference) references.next();
		IASTClassReference clr2 = (IASTClassReference) references.next();
		IASTVariableReference ar1 = (IASTVariableReference) references.next();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( clr1.getReferencedElement(), cl );
		assertEquals( ar1.getReferencedElement(), a );
		assertEquals( fr1.getReferencedElement(), f1 );
	}
	

}
