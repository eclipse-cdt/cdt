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
package org.eclipse.cdt.core.parser.tests;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTArrayModifier;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceAlias;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.internal.core.parser.ParserException;
/**
 * @author jcamelon
 *
 */
public class QuickParseASTTests extends BaseASTTest
{
    public QuickParseASTTests(String a)
    {
        super(a);
    }
    /**
     * Test code: int x = 5;
     * Purpose: to test the simple decaration in it's simplest form.
     */
    public void testIntGlobal() throws Exception
    {
        // Parse and get the translation Unit
        IASTCompilationUnit translationUnit = parse("int x = 5;"); //$NON-NLS-1$
        Iterator i = translationUnit.getDeclarations();
        assertTrue(i.hasNext());
        IASTVariable var = (IASTVariable)i.next();
        assertTrue(
            var.getAbstractDeclaration().getTypeSpecifier()
                instanceof IASTSimpleTypeSpecifier);
        assertTrue(
            ((IASTSimpleTypeSpecifier)var
                .getAbstractDeclaration()
                .getTypeSpecifier())
                .getType()
                == IASTSimpleTypeSpecifier.Type.INT);
        assertEquals(var.getName(), "x"); //$NON-NLS-1$
        assertNull(var.getInitializerClause());
        assertFalse(i.hasNext());
    }
    /**
     * Test code: class A { } a;
     * Purpose: tests the use of a classSpecifier in 
     */
    public void testEmptyClass() throws Exception
    {
        // Parse and get the translation unit
        Writer code = new StringWriter();
        code.write("class A { } a;"); //$NON-NLS-1$
        IASTCompilationUnit translationUnit = parse(code.toString());
        Iterator i = translationUnit.getDeclarations();
        assertTrue(i.hasNext());
        IASTVariable var = (IASTVariable)i.next();
        assertEquals(var.getName(), "a"); //$NON-NLS-1$
        assertTrue(
            var.getAbstractDeclaration().getTypeSpecifier()
                instanceof IASTClassSpecifier);
        IASTClassSpecifier classSpec =
            (IASTClassSpecifier)var.getAbstractDeclaration().getTypeSpecifier();
        assertEquals(classSpec.getName(), "A"); //$NON-NLS-1$
        assertFalse(classSpec.getDeclarations().hasNext());
        assertFalse(i.hasNext());
    }
    /**
     * Test code: class A { public: int x; };
     * Purpose: tests a declaration in a class scope.
     */
    public void testSimpleClassMember() throws Exception
    {
        // Parse and get the translaton unit
        Writer code = new StringWriter();
        code.write("class A { public: int x; };"); //$NON-NLS-1$
        IASTCompilationUnit cu = parse(code.toString());
        Iterator i = cu.getDeclarations();
        assertTrue(i.hasNext());
        IASTAbstractTypeSpecifierDeclaration declaration =
            (IASTAbstractTypeSpecifierDeclaration)i.next();
        assertFalse(i.hasNext());
        assertTrue(
            declaration.getTypeSpecifier() instanceof IASTClassSpecifier);
        assertTrue(
            ((IASTClassSpecifier)declaration.getTypeSpecifier()).getClassKind()
                == ASTClassKind.CLASS);
        Iterator j =
            ((IASTClassSpecifier)declaration.getTypeSpecifier())
                .getDeclarations();
        assertTrue(j.hasNext());
        IASTField field = (IASTField)j.next();
        assertFalse(j.hasNext());
        assertTrue(field.getName().equals("x")); //$NON-NLS-1$
        assertTrue(
            field.getAbstractDeclaration().getTypeSpecifier()
                instanceof IASTSimpleTypeSpecifier);
        assertTrue(
            ((IASTSimpleTypeSpecifier)field
                .getAbstractDeclaration()
                .getTypeSpecifier())
                .getType()
                == IASTSimpleTypeSpecifier.Type.INT);
    }
    public void testNamespaceDefinition() throws Exception
    {
        for (int i = 0; i < 2; ++i)
        {
            IASTCompilationUnit translationUnit;
            if (i == 0)
                translationUnit = parse("namespace KingJohn { int x; }"); //$NON-NLS-1$
            else
                translationUnit = parse("namespace { int x; }"); //$NON-NLS-1$
            Iterator iter = translationUnit.getDeclarations();
            assertTrue(iter.hasNext());
            IASTNamespaceDefinition namespaceDefinition =
                (IASTNamespaceDefinition)iter.next();
            assertFalse(iter.hasNext());
            if (i == 0)
                assertTrue(namespaceDefinition.getName().equals("KingJohn")); //$NON-NLS-1$
            else
                assertEquals(namespaceDefinition.getName(), ""); //$NON-NLS-1$
            Iterator j = namespaceDefinition.getDeclarations();
            assertTrue(j.hasNext());
            IASTVariable var = (IASTVariable)j.next();
            assertFalse(j.hasNext());
            assertTrue(
                var.getAbstractDeclaration().getTypeSpecifier()
                    instanceof IASTSimpleTypeSpecifier);
            assertTrue(
                ((IASTSimpleTypeSpecifier)var
                    .getAbstractDeclaration()
                    .getTypeSpecifier())
                    .getType()
                    == IASTSimpleTypeSpecifier.Type.INT);
            assertEquals(var.getName(), "x"); //$NON-NLS-1$
        }
    }
    
	public void testLinkageSpecification() throws Exception
	{
		for( int i = 0; i < 2; ++i )
		{
			IASTCompilationUnit compilationUnit; 
			if( i == 0  )
				compilationUnit = parse("extern \"C\" { int x(void); }"); //$NON-NLS-1$
			else
				compilationUnit = parse("extern \"ADA\" int x(void);"); //$NON-NLS-1$
				
			Iterator declarations = compilationUnit.getDeclarations();
			assertTrue( declarations.hasNext() );
			IASTLinkageSpecification linkage = (IASTLinkageSpecification)declarations.next(); 
			assertFalse( declarations.hasNext() );
			
			if( i == 0 )
				assertEquals( "C", linkage.getLinkageString() ); //$NON-NLS-1$
			else
				assertEquals( "ADA", linkage.getLinkageString() ); //$NON-NLS-1$
			
			Iterator subDeclarations = linkage.getDeclarations();
			assertTrue( subDeclarations.hasNext() );
			IASTFunction function = (IASTFunction)subDeclarations.next();
			assertFalse( subDeclarations.hasNext());
			
			assertEquals( ((IASTSimpleTypeSpecifier)function.getReturnType().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT );
			assertEquals( function.getName(), "x"); //$NON-NLS-1$
			
			Iterator parameters = function.getParameters();
			assertTrue( parameters.hasNext() );
			IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
			assertFalse( parameters.hasNext() );
			assertEquals( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.VOID );
			assertEquals( parm.getName(), "" ); //$NON-NLS-1$
		}
	}
    
	public void testEnumSpecifier() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "enum { yo, go = 3, away };\n"); //$NON-NLS-1$
		code.write( "enum hasAString { last = 666 };"); //$NON-NLS-1$
		IASTCompilationUnit translationUnit = parse( code.toString() );
		Iterator declarations = translationUnit.getDeclarations();
		
		for( int i = 0; i < 2; ++i )
		{ 
			assertTrue( declarations.hasNext() );
			IASTAbstractTypeSpecifierDeclaration abstractDeclaration = (IASTAbstractTypeSpecifierDeclaration)declarations.next(); 
			IASTEnumerationSpecifier enumerationSpec = (IASTEnumerationSpecifier)abstractDeclaration.getTypeSpecifier();
			if( i == 0 )
				assertEquals( enumerationSpec.getName(), "" ); //$NON-NLS-1$
			else
				assertEquals( enumerationSpec.getName(), "hasAString" ); //$NON-NLS-1$
			Iterator j = enumerationSpec.getEnumerators();
			
			if( i == 0 )
			{
				IASTEnumerator enumerator1 = (IASTEnumerator)j.next();
				assertEquals( enumerator1.getName(), "yo"); //$NON-NLS-1$
				assertNull( enumerator1.getInitialValue() );
				IASTEnumerator enumerator2 = (IASTEnumerator)j.next();
				assertNotNull( enumerator2.getInitialValue() );
				assertEquals( enumerator2.getInitialValue().getLiteralString(), "3"); //$NON-NLS-1$
				assertEquals( enumerator2.getInitialValue().getExpressionKind(), IASTExpression.Kind.PRIMARY_INTEGER_LITERAL );
				assertEquals( enumerator2.getName(), "go");  //$NON-NLS-1$
				IASTEnumerator enumerator3 = (IASTEnumerator)j.next();
				assertEquals( enumerator3.getName(), "away"); //$NON-NLS-1$
				assertNull( enumerator3.getInitialValue() );
				assertFalse( j.hasNext() );  
			}
			else
			{
				IASTEnumerator enumerator2 = (IASTEnumerator)j.next();
				assertNotNull( enumerator2.getInitialValue() );
				assertEquals( enumerator2.getInitialValue().getLiteralString(), "666"); //$NON-NLS-1$
				assertEquals( enumerator2.getInitialValue().getExpressionKind(), IASTExpression.Kind.PRIMARY_INTEGER_LITERAL );
				assertEquals( enumerator2.getName(), "last");  //$NON-NLS-1$
				assertFalse( j.hasNext() );
			}
		}		
	}
	
	public void testTypedef() throws Exception
	{
		Iterator i = parse( "typedef const struct A * const cpStructA;").getDeclarations(); //$NON-NLS-1$
		IASTTypedefDeclaration typedef = (IASTTypedefDeclaration)i.next(); 
		assertFalse( i.hasNext() );
		assertTrue( typedef.getAbstractDeclarator().isConst() );
		assertTrue( typedef.getAbstractDeclarator().getTypeSpecifier() instanceof IASTElaboratedTypeSpecifier );
		IASTElaboratedTypeSpecifier elab = (IASTElaboratedTypeSpecifier)typedef.getAbstractDeclarator().getTypeSpecifier();
		assertEquals( elab.getName(), "A"); //$NON-NLS-1$
		assertEquals( elab.getClassKind(), ASTClassKind.STRUCT );
		assertTrue( typedef.getAbstractDeclarator().getPointerOperators().hasNext() );
		Iterator pIter = typedef.getAbstractDeclarator().getPointerOperators();
		ASTPointerOperator po =(ASTPointerOperator)pIter.next(); 
		assertEquals( po, ASTPointerOperator.CONST_POINTER ); 
		assertFalse( pIter.hasNext() );
		assertEquals( typedef.getName(), "cpStructA"); //$NON-NLS-1$
		
	}
	
	public void testUsingClauses() throws Exception
	{
		Writer code = new StringWriter();
		
		code.write("using namespace A::B::C;\n"); //$NON-NLS-1$
		code.write("using namespace C;\n"); //$NON-NLS-1$
		code.write("using B::f;\n"); //$NON-NLS-1$
		code.write("using ::f;\n"); //$NON-NLS-1$
		code.write("using typename crap::de::crap;"); //$NON-NLS-1$
		Iterator declarations = parse(code.toString()).getDeclarations();
		
		IASTUsingDirective usingDirective = (IASTUsingDirective)declarations.next();
		assertEquals( usingDirective.getNamespaceName(), "A::B::C" ); //$NON-NLS-1$
		
		usingDirective = (IASTUsingDirective)declarations.next();
		assertEquals( usingDirective.getNamespaceName(), "C" ); //$NON-NLS-1$
		
		IASTUsingDeclaration usingDeclaration = (IASTUsingDeclaration)declarations.next(); 
		assertEquals( usingDeclaration.usingTypeName(), "B::f" );  //$NON-NLS-1$
		
		usingDeclaration = (IASTUsingDeclaration)declarations.next(); 
		assertEquals( usingDeclaration.usingTypeName(), "::f" );  //$NON-NLS-1$
		usingDeclaration = (IASTUsingDeclaration)declarations.next();
		assertEquals( usingDeclaration.usingTypeName(), "crap::de::crap" ); //$NON-NLS-1$
		assertTrue( usingDeclaration.isTypename() ); 
		  
		assertFalse( declarations.hasNext());
	}
	
	/**
	 * Test code: class A : public B, private C, virtual protected D { public: int x, y; float a,b,c; }
	 * Purpose: tests a declaration in a class scope.
	 */
	public void testSimpleClassMembers() throws Exception {
		// Parse and get the translaton unit
		Writer code = new StringWriter();
		code.write("class A : public B, private C, virtual protected D { public: int x, y; float a,b,c; };"); //$NON-NLS-1$
		Iterator declarations = parse( code.toString() ).getDeclarations();
		IASTClassSpecifier classSpec = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		assertFalse( declarations.hasNext() );
		Iterator baseClauses = classSpec.getBaseClauses();
		IASTBaseSpecifier baseSpec = (IASTBaseSpecifier)baseClauses.next(); 
		assertEquals( baseSpec.getParentClassName(), "B" ); //$NON-NLS-1$
		assertEquals( baseSpec.getAccess(), ASTAccessVisibility.PUBLIC );
		baseSpec = (IASTBaseSpecifier)baseClauses.next();
		assertEquals( baseSpec.getParentClassName(), "C" ); //$NON-NLS-1$
		assertEquals( baseSpec.getAccess(), ASTAccessVisibility.PRIVATE);
		baseSpec = (IASTBaseSpecifier)baseClauses.next();
		assertEquals( baseSpec.getAccess(), ASTAccessVisibility.PROTECTED );
		assertTrue( baseSpec.isVirtual() );
		assertEquals( baseSpec.getParentClassName(), "D" ); //$NON-NLS-1$
		assertFalse( baseClauses.hasNext() );

		Iterator members = classSpec.getDeclarations();
		IASTField field = (IASTField)members.next(); 
		assertEquals( ((IASTSimpleTypeSpecifier)field.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT ); 
		assertEquals( field.getName(), "x" );  //$NON-NLS-1$
		
		field = (IASTField)members.next(); 
		assertEquals( ((IASTSimpleTypeSpecifier)field.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT ); 
		assertEquals( field.getName(), "y" );  //$NON-NLS-1$
		
		field = (IASTField)members.next(); 
		assertEquals( ((IASTSimpleTypeSpecifier)field.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.FLOAT ); 
		assertEquals( field.getName(), "a" );  //$NON-NLS-1$
		field = (IASTField)members.next(); 
		assertEquals( ((IASTSimpleTypeSpecifier)field.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.FLOAT ); 
		assertEquals( field.getName(), "b" );  //$NON-NLS-1$
		field = (IASTField)members.next(); 
		assertEquals( ((IASTSimpleTypeSpecifier)field.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.FLOAT ); 
		assertEquals( field.getName(), "c" ); //$NON-NLS-1$
		assertFalse( members.hasNext() ); 
		
	}
	
		/**
		 * Test code: int myFunction( void ); 
		 */
		public void testSimpleFunctionDeclaration() throws Exception
		{
			// Parse and get the translaton unit
			Writer code = new StringWriter();
			code.write("void myFunction( void );"); //$NON-NLS-1$
			Iterator declarations = parse( code.toString()).getDeclarations();
			IASTFunction f1 = (IASTFunction)declarations.next(); 
			assertFalse( declarations.hasNext() ); 
			assertEquals( f1.getName(), "myFunction"); //$NON-NLS-1$
			assertEquals( ((IASTSimpleTypeSpecifier)f1.getReturnType().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.VOID ); 
			Iterator parameters = f1.getParameters();
			IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
			assertFalse( parameters.hasNext() );
			assertEquals( parm.getName(), "" ); //$NON-NLS-1$
			assertEquals( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.VOID );
			
		}
	
	/**
	 * Test code: bool myFunction( int parm1 = 3 * 4, double parm2 );
	 * @throws Exception
	 */
	public void testFunctionDeclarationWithParameters() throws Exception
	{
		// Parse and get the translaton unit
		Writer code = new StringWriter();
		code.write("bool myFunction( int parm1 = 3 * 4, double parm2 );"); //$NON-NLS-1$
		Iterator declarations = parse(code.toString()).getDeclarations();
		IASTFunction f1 = (IASTFunction)declarations.next(); 
		assertFalse( declarations.hasNext() ); 
		assertEquals( f1.getName(), "myFunction"); //$NON-NLS-1$
		assertEquals( ((IASTSimpleTypeSpecifier)f1.getReturnType().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.BOOL ); 
		Iterator parameters = f1.getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
		assertEquals( parm.getName(), "parm1" ); //$NON-NLS-1$
		assertEquals( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT );
		assertEquals( parm.getDefaultValue().getAssigmentExpression().getExpressionKind(), IASTExpression.Kind.MULTIPLICATIVE_MULTIPLY );
		
		parm = (IASTParameterDeclaration)parameters.next(); 
		assertEquals( parm.getName(), "parm2" ); //$NON-NLS-1$
		assertEquals( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.DOUBLE );
		assertNull( parm.getDefaultValue()  );
		assertFalse( parameters.hasNext());
		
	}
	
	public void testAssignmentExpressions() throws Exception 
	{
		parse( "int x = y = z = 5;"); //$NON-NLS-1$
	}
	
	public void testBug39348() throws Exception
	{
		parse("unsigned char a[sizeof (struct sss)];"); //$NON-NLS-1$
	}
	
	public void testBug39501() throws Exception
	{
		parse("struct A { A() throw (int); };"); //$NON-NLS-1$
	}
	
	public void testBug39349() throws Exception
	{
		parse( "enum foo {  foo1   = 0,  foo2   = 0xffffffffffffffffULL,  foo3   = 0xf0fffffffffffffeLLU };" );  //$NON-NLS-1$
	}
	
	public void testBug39544() throws Exception	{
		parse("wchar_t wc = L'X';");  //$NON-NLS-1$
	}
	
	public void testBug36290() throws Exception {
		parse("typedef void ( A:: * pMethod ) ( void ); "); //$NON-NLS-1$
		parse("typedef void (boo) ( void ); "); //$NON-NLS-1$
		parse("typedef void boo (void); "); //$NON-NLS-1$
	}
	
	public void testBug36769B() throws Exception {
		parse("class X { operator int(); } \n"); //$NON-NLS-1$
		parse("class X { operator int*(); } \n"); //$NON-NLS-1$
		parse("class X { operator int&(); } \n"); //$NON-NLS-1$
		parse("class X { operator A(); } \n"); //$NON-NLS-1$
		parse("class X { operator A*(); } \n"); //$NON-NLS-1$
		parse("class X { operator A&(); } \n"); //$NON-NLS-1$
		
		parse("X::operator int() { } \n"); //$NON-NLS-1$
		parse("X::operator int*() { } \n"); //$NON-NLS-1$
		parse("X::operator int&() { } \n"); //$NON-NLS-1$
		parse("X::operator A() { } \n"); //$NON-NLS-1$
		parse("X::operator A*() { } \n"); //$NON-NLS-1$
		parse("X::operator A&() { } \n"); //$NON-NLS-1$
		
		parse("template <class A,B> class X<A,C> { operator int(); } \n"); //$NON-NLS-1$
		parse("template <class A,B> class X<A,C> { operator int*(); } \n"); //$NON-NLS-1$
		parse("template <class A,B> class X<A,C> { operator int&(); } \n"); //$NON-NLS-1$
		parse("template <class A,B> class X<A,C> { operator A(); } \n"); //$NON-NLS-1$
		parse("template <class A,B> class X<A,C> { operator A*(); } \n"); //$NON-NLS-1$
		parse("template <class A,B> class X<A,C> { operator A&(); } \n"); //$NON-NLS-1$

		parse("template <class A,B> X<A,C>::operator int() { } \n"); //$NON-NLS-1$
		parse("template <class A,B> X<A,C>::operator int*() { } \n"); //$NON-NLS-1$
		parse("template <class A,B> X<A,C>::operator int&() { } \n"); //$NON-NLS-1$
		parse("template <class A,B> X<A,C>::operator A() { } \n"); //$NON-NLS-1$
		parse("template <class A,B> X<A,C>::operator A*() { } \n"); //$NON-NLS-1$
		parse("template <class A,B> X<A,C>::operator A&() { } \n"); //$NON-NLS-1$
	}
	public void testBug36932C() throws Exception {
		parse("X::X( ) : var( new int ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new int(5) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new int(B) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new int(B,C) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new int[5] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new int[5][10] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new int[B] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new int[B][C][D] ) {}"); //$NON-NLS-1$

		parse("X::X( ) : var( new A ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new A(5) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new A(B) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new A(B,C) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new A[5] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new A[5][10] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new A[B] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new A[B][C][D] ) {}"); //$NON-NLS-1$

		parse("X::X( ) : var( new (int) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (int)(5) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (int)(B) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (int)(B,C) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (int)[5] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (int)[5][10] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (int)[B] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (int)[B][C][D] ) {}"); //$NON-NLS-1$

		parse("X::X( ) : var( new (A) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (A)(5) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (A)(B) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (A)(B,C) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (A)[5] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (A)[5][10] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (A)[B] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (A)[B][C][D] ) {}"); //$NON-NLS-1$

		parse("X::X( ) : var( new (0) int ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) int(5) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) int(B) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) int(B,C) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) int[5] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) int[5][10] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) int[B] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) int[B][C][D] ) {}"); //$NON-NLS-1$

		parse("X::X( ) : var( new (0) A ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) A(5) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) A(B) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) A(B,C) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) A[5] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) A[5][10] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) A[B] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) A[B][C][D] ) {}"); //$NON-NLS-1$

		parse("X::X( ) : var( new (0) (int) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) (int)(5) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) (int)(B) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) (int)(B,C) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) (int)[5] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) (int)[5][10] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) (int)[B] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) (int)[B][C][D] ) {}"); //$NON-NLS-1$

		parse("X::X( ) : var( new (0) (A) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) (A)(5) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) (A)(B) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) (A)(B,C) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) (A)[5] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) (A)[5][10] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) (A)[B] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (0) (A)[B][C][D] ) {}"); //$NON-NLS-1$

		parse("X::X( ) : var( new (P) int ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) int(5) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) int(B) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) int(B,C) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) int[5] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) int[5][10] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) int[B] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) int[B][C][D] ) {}"); //$NON-NLS-1$

		parse("X::X( ) : var( new (P) A ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) A(5) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) A(B) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) A(B,C) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) A[5] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) A[5][10] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) A[B] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) A[B][C][D] ) {}"); //$NON-NLS-1$

		parse("X::X( ) : var( new (P) (int) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) (int)(5) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) (int)(B) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) (int)(B,C) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) (int)[5] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) (int)[5][10] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) (int)[B] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) (int)[B][C][D] ) {}"); //$NON-NLS-1$

		parse("X::X( ) : var( new (P) (A) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) (A)(5) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) (A)(B) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) (A)(B,C) ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) (A)[5] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) (A)[5][10] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) (A)[B] ) {}"); //$NON-NLS-1$
		parse("X::X( ) : var( new (P) (A)[B][C][D] ) {}"); //$NON-NLS-1$
	}
    

	public void testBugSingleton192() throws Exception {
		parse("int Test::* pMember_;" ); //$NON-NLS-1$
	}
	public void testBug36931() throws Exception {
		parse("A::nested::nested(){}; "); //$NON-NLS-1$
		parse("int A::nested::foo() {} "); //$NON-NLS-1$
		parse("int A::nested::operator+() {} "); //$NON-NLS-1$
		parse("A::nested::operator int() {} "); //$NON-NLS-1$
		parse("static const int A::nested::i = 1; "); //$NON-NLS-1$
        
		parse("template <class B,C> A<B>::nested::nested(){}; "); //$NON-NLS-1$
		parse("template <class B,C> int A::nested<B,D>::foo() {} "); //$NON-NLS-1$
		parse("template <class B,C> int A<B,C>::nested<C,B>::operator+() {} "); //$NON-NLS-1$
		parse("template <class B,C> A::nested::operator int() {} "); //$NON-NLS-1$
	}
	
	public void testBug37019() throws Exception {
		parse("static const A a( 1, 0 );"); //$NON-NLS-1$
	}	

	public void testBug36766and36769A() throws Exception {
		Writer code = new StringWriter();
		code.write("template <class _CharT, class _Alloc>\n"); //$NON-NLS-1$
		code.write("rope<_CharT, _Alloc>::rope(size_t __n, _CharT __c,\n"); //$NON-NLS-1$
		code.write("const allocator_type& __a): _Base(__a)\n"); //$NON-NLS-1$
		code.write("{}\n"); //$NON-NLS-1$
		parse(code.toString());
	}

	public void testBug36766and36769B() throws Exception {
		Writer code = new StringWriter();
		code.write("template<class _CharT>\n"); //$NON-NLS-1$
		code.write("bool _Rope_insert_char_consumer<_CharT>::operator()\n"); //$NON-NLS-1$
		code.write("(const _CharT* __leaf, size_t __n)\n"); //$NON-NLS-1$
		code.write("{}\n"); //$NON-NLS-1$
		parse(code.toString());
	}

	public void testBug36766and36769C() throws Exception {
		Writer code = new StringWriter();
		code.write("template <class _CharT, class _Alloc>\n"); //$NON-NLS-1$
		code.write("_Rope_char_ref_proxy<_CharT, _Alloc>&\n"); //$NON-NLS-1$
		code.write(
			"_Rope_char_ref_proxy<_CharT, _Alloc>::operator= (_CharT __c)\n"); //$NON-NLS-1$
		code.write("{}\n"); //$NON-NLS-1$
		parse(code.toString());
	}
	
	public void testBug36766and36769D() throws Exception {
		Writer code = new StringWriter();
		code.write("template <class _CharT, class _Alloc>\n"); //$NON-NLS-1$
		code.write("rope<_CharT, _Alloc>::~rope()\n"); //$NON-NLS-1$
		code.write("{}\n"); //$NON-NLS-1$
		parse(code.toString());
	}
    
	public void testBug36932A() throws Exception {
		parse("A::A( ) : var( new char [ (unsigned)bufSize ] ) {}"); //$NON-NLS-1$
	}
        
	public void testBug36932B() throws Exception {
		parse(" p = new int; "); //$NON-NLS-1$
		parse(" p = new int(5); "); //$NON-NLS-1$
		parse(" p = new int(B); "); //$NON-NLS-1$
		parse(" p = new int(B,C); "); //$NON-NLS-1$
		parse(" p = new int[5]; "); //$NON-NLS-1$
		parse(" p = new int[5][10]; "); //$NON-NLS-1$
		parse(" p = new int[B]; "); //$NON-NLS-1$
		parse(" p = new int[B][C][D]; "); //$NON-NLS-1$
 
		parse(" p = new A; "); //$NON-NLS-1$
		parse(" p = new A(5); "); //$NON-NLS-1$
		parse(" p = new A(B); "); //$NON-NLS-1$
		parse(" p = new A(B,C); "); //$NON-NLS-1$
		parse(" p = new A[5]; "); //$NON-NLS-1$
		parse(" p = new A[5][10]; "); //$NON-NLS-1$
		parse(" p = new A[B]; "); //$NON-NLS-1$
		parse(" p = new A[B][C][D]; "); //$NON-NLS-1$

		parse(" p = new (int); "); //$NON-NLS-1$
		parse(" p = new (int)(5); "); //$NON-NLS-1$
		parse(" p = new (int)(B); "); //$NON-NLS-1$
		parse(" p = new (int)(B,C); "); //$NON-NLS-1$
		parse(" p = new (int)[5]; "); //$NON-NLS-1$
		parse(" p = new (int)[5][10]; "); //$NON-NLS-1$
		parse(" p = new (int)[B]; "); //$NON-NLS-1$
		parse(" p = new (int)[B][C][D]; "); //$NON-NLS-1$

		parse(" p = new (A); "); //$NON-NLS-1$
		parse(" p = new (A)(5); "); //$NON-NLS-1$
		parse(" p = new (A)(B); "); //$NON-NLS-1$
		parse(" p = new (A)(B,C); "); //$NON-NLS-1$
		parse(" p = new (A)[5]; "); //$NON-NLS-1$
		parse(" p = new (A)[5][10]; "); //$NON-NLS-1$
		parse(" p = new (A)[B]; "); //$NON-NLS-1$
		parse(" p = new (A)[B][C][D]; "); //$NON-NLS-1$

		parse(" p = new (0) int; "); //$NON-NLS-1$
		parse(" p = new (0) int(5); "); //$NON-NLS-1$
		parse(" p = new (0) int(B); "); //$NON-NLS-1$
		parse(" p = new (0) int(B,C); "); //$NON-NLS-1$
		parse(" p = new (0) int[5]; "); //$NON-NLS-1$
		parse(" p = new (0) int[5][10]; "); //$NON-NLS-1$
		parse(" p = new (0) int[B]; "); //$NON-NLS-1$
		parse(" p = new (0) int[B][C][D]; "); //$NON-NLS-1$

		parse(" p = new (0) A; "); //$NON-NLS-1$
		parse(" p = new (0) A(5); "); //$NON-NLS-1$
		parse(" p = new (0) A(B); "); //$NON-NLS-1$
		parse(" p = new (0) A(B,C); "); //$NON-NLS-1$
		parse(" p = new (0) A[5]; "); //$NON-NLS-1$
		parse(" p = new (0) A[5][10]; "); //$NON-NLS-1$
		parse(" p = new (0) A[B]; "); //$NON-NLS-1$
		parse(" p = new (0) A[B][C][D]; "); //$NON-NLS-1$

		parse(" p = new (0) (int); "); //$NON-NLS-1$
		parse(" p = new (0) (int)(5); "); //$NON-NLS-1$
		parse(" p = new (0) (int)(B); "); //$NON-NLS-1$
		parse(" p = new (0) (int)(B,C); "); //$NON-NLS-1$
		parse(" p = new (0) (int)[5]; "); //$NON-NLS-1$
		parse(" p = new (0) (int)[5][10]; "); //$NON-NLS-1$
		parse(" p = new (0) (int)[B]; "); //$NON-NLS-1$
		parse(" p = new (0) (int)[B][C][D]; "); //$NON-NLS-1$

		parse(" p = new (0) (A); "); //$NON-NLS-1$
		parse(" p = new (0) (A)(5); "); //$NON-NLS-1$
		parse(" p = new (0) (A)(B); "); //$NON-NLS-1$
		parse(" p = new (0) (A)(B,C); "); //$NON-NLS-1$
		parse(" p = new (0) (A)[5]; "); //$NON-NLS-1$
		parse(" p = new (0) (A)[5][10]; "); //$NON-NLS-1$
		parse(" p = new (0) (A)[B]; "); //$NON-NLS-1$
		parse(" p = new (0) (A)[B][C][D]; "); //$NON-NLS-1$

		parse(" p = new (P) int; "); //$NON-NLS-1$
		parse(" p = new (P) int(5); "); //$NON-NLS-1$
		parse(" p = new (P) int(B); "); //$NON-NLS-1$
		parse(" p = new (P) int(B,C); "); //$NON-NLS-1$
		parse(" p = new (P) int[5]; "); //$NON-NLS-1$
		parse(" p = new (P) int[5][10]; "); //$NON-NLS-1$
		parse(" p = new (P) int[B]; "); //$NON-NLS-1$
		parse(" p = new (P) int[B][C][D]; "); //$NON-NLS-1$

		parse(" p = new (P) A; "); //$NON-NLS-1$
		parse(" p = new (P) A(5); "); //$NON-NLS-1$
		parse(" p = new (P) A(B); "); //$NON-NLS-1$
		parse(" p = new (P) A(B,C); "); //$NON-NLS-1$
		parse(" p = new (P) A[5]; "); //$NON-NLS-1$
		parse(" p = new (P) A[5][10]; "); //$NON-NLS-1$
		parse(" p = new (P) A[B]; "); //$NON-NLS-1$
		parse(" p = new (P) A[B][C][D]; "); //$NON-NLS-1$

		parse(" p = new (P) (int); "); //$NON-NLS-1$
		parse(" p = new (P) (int)(5); "); //$NON-NLS-1$
		parse(" p = new (P) (int)(B); "); //$NON-NLS-1$
		parse(" p = new (P) (int)(B,C); "); //$NON-NLS-1$
		parse(" p = new (P) (int)[5]; "); //$NON-NLS-1$
		parse(" p = new (P) (int)[5][10]; "); //$NON-NLS-1$
		parse(" p = new (P) (int)[B]; "); //$NON-NLS-1$
		parse(" p = new (P) (int)[B][C][D]; "); //$NON-NLS-1$

		parse(" p = new (P) (A); "); //$NON-NLS-1$
		parse(" p = new (P) (A)(5); "); //$NON-NLS-1$
		parse(" p = new (P) (A)(B); "); //$NON-NLS-1$
		parse(" p = new (P) (A)(B,C); "); //$NON-NLS-1$
		parse(" p = new (P) (A)[5]; "); //$NON-NLS-1$
		parse(" p = new (P) (A)[5][10]; "); //$NON-NLS-1$
		parse(" p = new (P) (A)[B]; "); //$NON-NLS-1$
		parse(" p = new (P) (A)[B][C][D]; "); //$NON-NLS-1$
	}

	public void testBug36769A() throws Exception {
		Writer code = new StringWriter();
		code.write("template <class A, B> cls<A, C>::operator op &() const {}\n"); //$NON-NLS-1$
		code.write("template <class A, B> cls<A, C>::cls() {}\n"); //$NON-NLS-1$
		code.write("template <class A, B> cls<A, C>::~cls() {}\n"); //$NON-NLS-1$
			
		parse( code.toString());
	}
	
	public void testBug36714() throws Exception {
		Writer code = new StringWriter();
		code.write("unsigned long a = 0UL;\n"); //$NON-NLS-1$
		code.write("unsigned long a2 = 0L; \n"); //$NON-NLS-1$
	
		parse( code.toString() );
	}
	
	public void testBugFunctor758() throws Exception {
		parse( "template <typename Fun> Functor(Fun fun) : spImpl_(new FunctorHandler<Functor, Fun>(fun)){}" );  //$NON-NLS-1$
	}
	
	public void testBug36932() throws Exception
	{
		parse( "A::A(): b( new int( 5 ) ), b( new B ), c( new int ) {}" ); //$NON-NLS-1$
	}

	public void testBug36704() throws Exception {
		Writer code = new StringWriter(); 
		code.write( "template <class T, class U>\n" );  //$NON-NLS-1$
		code.write( "struct Length< Typelist<T, U> >\n" ); //$NON-NLS-1$
		code.write( "{\n" ); //$NON-NLS-1$
		code.write( "enum { value = 1 + Length<U>::value };\n" ); //$NON-NLS-1$
		code.write( "};\n" ); //$NON-NLS-1$
		parse(code.toString());
	}

	public void testBug36699() throws Exception {
		Writer code = new StringWriter();
		code.write(
			"template <	template <class> class ThreadingModel = DEFAULT_THREADING,\n"); //$NON-NLS-1$
		code.write("std::size_t chunkSize = DEFAULT_CHUNK_SIZE,\n"); //$NON-NLS-1$
		code.write(
			"std::size_t maxSmallObjectSize = MAX_SMALL_OBJECT_SIZE	>\n"); //$NON-NLS-1$
		code.write("class SmallObject : public ThreadingModel<\n"); //$NON-NLS-1$
		code.write(
			"SmallObject<ThreadingModel, chunkSize, maxSmallObjectSize> >\n"); //$NON-NLS-1$
		code.write("{};\n"); //$NON-NLS-1$
		parse(code.toString());
	}

	public void testBug36691() throws Exception {
		Writer code = new StringWriter();
		code.write("template <class T, class H>\n"); //$NON-NLS-1$
		code.write(
			"typename H::template Rebind<T>::Result& Field(H& obj)\n"); //$NON-NLS-1$
		code.write("{	return obj;	}\n"); //$NON-NLS-1$
		parse(code.toString());
	}
	
	
	public void testBug39554() throws Exception
	{
		 parse("_Pragma(\"foobar\")", true, true, ParserLanguage.C ); //$NON-NLS-1$
	}
	
	public void testBug36702() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "void mad_decoder_init(struct mad_decoder *, void *,\n" );  //$NON-NLS-1$
		code.write( "			  enum mad_flow (*)(void *, struct mad_stream *),\n" );  //$NON-NLS-1$
		code.write( "			  enum mad_flow (*)(void *, struct mad_header const *),\n" );  //$NON-NLS-1$
		code.write( "			  enum mad_flow (*)(void *,\n" );  //$NON-NLS-1$
		code.write( "					struct mad_stream const *,\n" );  //$NON-NLS-1$
		code.write( "					struct mad_frame *),\n" );  //$NON-NLS-1$
		code.write( "			  enum mad_flow (*)(void *,\n" );  //$NON-NLS-1$
		code.write( "					struct mad_header const *,\n" );  //$NON-NLS-1$
		code.write( "					struct mad_pcm *),\n" );  //$NON-NLS-1$
		code.write( "			  enum mad_flow (*)(void *,\n" );  //$NON-NLS-1$
		code.write( "					struct mad_stream *,\n" );  //$NON-NLS-1$
		code.write( "					struct mad_frame *),\n" );  //$NON-NLS-1$
		code.write( "			  enum mad_flow (*)(void *, void *, unsigned int *)\n" );  //$NON-NLS-1$
		code.write( ");\n" );   //$NON-NLS-1$
		
		parse( code.toString() );
		
	}
	
	public void testBug36852() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "int CBT::senseToAllRect( double id_standardQuot = DOSE, double id_minToleranz =15.0,\n" );   //$NON-NLS-1$
		code.write( "double id_maxToleranz = 15.0, unsigned int iui_minY = 0, \n" ); //$NON-NLS-1$
		code.write( "unsigned int iui_maxY = HEIGHT );\n" ); //$NON-NLS-1$
		parse( code.toString() );
	}
	
	public void testBug36689() throws Exception {
		Writer code = new StringWriter();
		code.write("template\n"); //$NON-NLS-1$
		code.write("<\n"); //$NON-NLS-1$
		code.write("class AbstractFact,\n"); //$NON-NLS-1$
		code.write(
			"template <class, class> class Creator = OpNewFactoryUnit,\n"); //$NON-NLS-1$
		code.write("class TList = typename AbstractFact::ProductList\n"); //$NON-NLS-1$
		code.write(">\n"); //$NON-NLS-1$
		code.write("class ConcreteFactory\n"); //$NON-NLS-1$
		code.write(": public GenLinearHierarchy<\n"); //$NON-NLS-1$
		code.write(
			"typename TL::Reverse<TList>::Result, Creator, AbstractFact>\n"); //$NON-NLS-1$
		code.write("{\n"); //$NON-NLS-1$
		code.write("public:\n"); //$NON-NLS-1$
		code.write(
			"typedef typename AbstractFact::ProductList ProductList;\n"); //$NON-NLS-1$
		code.write("typedef TList ConcreteProductList;\n"); //$NON-NLS-1$
		code.write("};\n"); //$NON-NLS-1$
		parse(code.toString());
	}
	
	public void testBug36707() throws Exception {
		parse("enum { exists = sizeof(typename H::Small) == sizeof((H::Test(H::MakeT()))) };"); //$NON-NLS-1$
	}
	
	public void testBug36717() throws Exception  {
		parse("enum { eA = A::b };"); //$NON-NLS-1$
	}
	
	public void testBug36693() throws Exception {
		parse("FixedAllocator::Chunk* FixedAllocator::VicinityFind(void* p){}"); //$NON-NLS-1$
	}

	public void testWeirdExpression() throws Exception
	{
		parse( "int x = rhs.spImpl_.get();"); //$NON-NLS-1$
	}

	public void testBug36696() throws Exception {
		Writer code = new StringWriter();
		code.write(
			"template <typename P1> RefCounted(const RefCounted<P1>& rhs)\n"); //$NON-NLS-1$
		code.write(
			": pCount_(reinterpret_cast<const RefCounted&>(rhs).pCount_) {}\n"); //$NON-NLS-1$
		parse(code.toString());
	}

	public void testArrayOfPointerToFunctions() throws Exception
	{
		parse( "unsigned char (*main_data)[MAD_BUFFER_MDLEN];"); //$NON-NLS-1$
	}

	public void testBug36073() throws Exception
	{
		StringWriter writer = new StringWriter(); 
		writer.write( "class A{\n" );  //$NON-NLS-1$
		writer.write( "int x;\n" );  //$NON-NLS-1$
		writer.write( "public:\n" );  //$NON-NLS-1$
		writer.write( "A(const A&);\n" );  //$NON-NLS-1$
		writer.write( "};\n" );  //$NON-NLS-1$
		writer.write( "A::A(const A&v) : x(v.x) { }\n" ); //$NON-NLS-1$
		parse( writer.toString() );
	}
	
	
	public void testTemplateSpecialization() throws Exception
	{
		Iterator declarations = parse( "template<> class stream<char> { /* ... */ };").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier specifier = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)((IASTTemplateSpecialization)declarations.next()).getOwnedDeclaration()).getTypeSpecifier();
		assertFalse( declarations.hasNext());
		assertEquals( specifier.getName(), "stream<char>");  //$NON-NLS-1$
		assertFalse( specifier.getDeclarations().hasNext() );		
	}
	
	public void testTemplateInstantiation() throws Exception
	{
		Iterator declarations = parse( "template class Array<char>;").getDeclarations(); //$NON-NLS-1$
		IASTElaboratedTypeSpecifier specifier = (IASTElaboratedTypeSpecifier)((IASTAbstractTypeSpecifierDeclaration)((IASTTemplateInstantiation)declarations.next()).getOwnedDeclaration()).getTypeSpecifier();
		assertFalse( declarations.hasNext() );
		assertEquals( specifier.getName(), "Array<char>"); //$NON-NLS-1$
		assertEquals( specifier.getClassKind(), ASTClassKind.CLASS );
	}
	
	/**
	 * Test code:  "class A { int floor( double input ), someInt; };"
	 */
	public void testMultipleDeclarators() throws Exception
	{
		// Parse and get the translaton unit
		Iterator declarations = parse("class A { int floor( double input ), someInt; };").getDeclarations(); //$NON-NLS-1$
		Iterator members = ((IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier()).getDeclarations(); 
		assertFalse( declarations.hasNext() );
		IASTMethod decl1 = (IASTMethod)members.next();
		assertEquals( ((IASTSimpleTypeSpecifier)decl1.getReturnType().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT );
		Iterator parameters = decl1.getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
		assertEquals( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.DOUBLE );
		assertFalse( parameters.hasNext());
		assertEquals( parm.getName(), "input"); //$NON-NLS-1$
		
		IASTField decl2 = (IASTField)members.next();
		assertEquals( decl2.getName(), "someInt"); //$NON-NLS-1$
		assertEquals( ((IASTSimpleTypeSpecifier)decl2.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT ); 
		assertFalse( members.hasNext());
	}

	public void testFunctionModifiers() throws Exception
	{
		Iterator declarations = parse( "class A {virtual void foo( void ) const throw ( yay, nay, we::dont::care ) = 0;};").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier classSpec = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		assertFalse( declarations.hasNext());
		Iterator members = classSpec.getDeclarations();
		IASTMethod method = (IASTMethod)members.next(); 
		assertFalse( members.hasNext() );
		assertTrue( method.isVirtual());
		assertEquals( method.getName(), "foo"); //$NON-NLS-1$
		assertEquals( ((IASTSimpleTypeSpecifier)method.getReturnType().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.VOID );
		Iterator parameters = method.getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
		assertEquals( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.VOID );
		assertFalse( parameters.hasNext());
		assertEquals( parm.getName(), ""); //$NON-NLS-1$
		assertTrue( method.isConst() ); 
		assertTrue( method.isPureVirtual() );
		assertNotNull( method.getExceptionSpec() );
		Iterator exceptions = method.getExceptionSpec().getTypeIds();
		assertEquals( (String)exceptions.next(), "yay"); //$NON-NLS-1$
		assertEquals( (String)exceptions.next(), "nay"); //$NON-NLS-1$
		assertEquals( (String)exceptions.next(), "we::dont::care"); //$NON-NLS-1$
		assertFalse( exceptions.hasNext() );
	}


	public void testArrays() throws Exception
	{
		Iterator declarations = parse("int x [5][];").getDeclarations(); //$NON-NLS-1$
		IASTVariable x = (IASTVariable)declarations.next(); 
		assertFalse( declarations.hasNext() );
		assertEquals( ((IASTSimpleTypeSpecifier)x.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT );
		assertEquals( x.getName(), "x"); //$NON-NLS-1$
		Iterator arrayMods = x.getAbstractDeclaration().getArrayModifiers();
		IASTArrayModifier mod = (IASTArrayModifier)arrayMods.next();
		assertEquals( mod.getExpression().getExpressionKind(), IASTExpression.Kind.PRIMARY_INTEGER_LITERAL );
		assertEquals( mod.getExpression().getLiteralString(), "5" ); //$NON-NLS-1$
		mod = (IASTArrayModifier)arrayMods.next();
		assertNull( mod.getExpression());
		assertFalse( arrayMods.hasNext() ); 
	}		

	public void testElaboratedParms() throws Exception
	{
		Iterator declarations = parse( "int x( struct A myA ) { /* junk */ }" ).getDeclarations(); //$NON-NLS-1$
		IASTFunction f = (IASTFunction)declarations.next();
		assertSimpleReturnType( f, IASTSimpleTypeSpecifier.Type.INT );
		Iterator parms = f.getParameters(); 
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parms.next();
		assertFalse( parms.hasNext());
		assertEquals( parm.getName(), "myA"); //$NON-NLS-1$
		assertEquals( ((IASTElaboratedTypeSpecifier)parm.getTypeSpecifier()).getName(), "A" ); //$NON-NLS-1$
		assertEquals( ((IASTElaboratedTypeSpecifier)parm.getTypeSpecifier()).getClassKind(), ASTClassKind.STRUCT );
		assertFalse( declarations.hasNext());
	}
	
	public void testMemberDeclarations() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "class A {\n" );  //$NON-NLS-1$
		code.write( "public:\n"); //$NON-NLS-1$
		code.write( " int is0;\n" ); //$NON-NLS-1$
		code.write( "private:\n"); //$NON-NLS-1$
		code.write( " int is1;\n" ); //$NON-NLS-1$
		code.write( "protected:\n"); //$NON-NLS-1$
		code.write( " int is2;\n" ); //$NON-NLS-1$
		code.write( "};"); //$NON-NLS-1$
		Iterator declarations = parse( code.toString()).getDeclarations();
		IASTClassSpecifier classSpec = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		assertFalse(declarations.hasNext());
		Iterator members = classSpec.getDeclarations();
		for( int i = 0; i < 3; ++i )
		{
			IASTField field = (IASTField)members.next();
			assertEquals( field.getName(), "is"+ new Integer( i ).toString()); //$NON-NLS-1$
			ASTAccessVisibility visibility = null; 
			switch( i )
			{
				case 0:
					visibility = ASTAccessVisibility.PUBLIC;
					break;

				case 1:
					visibility = ASTAccessVisibility.PRIVATE;
					break;
 
				default: 
					visibility = ASTAccessVisibility.PROTECTED;
					break;
			}
			assertEquals( field.getVisiblity(), visibility );
		}
		assertFalse( members.hasNext());
	}

	public void testPointerOperators() throws Exception
	{
		Iterator declarations = parse("int * x = 0, & y, * const * volatile * z;").getDeclarations(); //$NON-NLS-1$
		for( int i = 0; i < 3; ++i )
		{
			IASTVariable v = (IASTVariable)declarations.next();
			assertSimpleType( v, IASTSimpleTypeSpecifier.Type.INT );
			Iterator pointerOperators = v.getAbstractDeclaration().getPointerOperators();
			ASTPointerOperator pointerOp = (ASTPointerOperator)pointerOperators.next(); 
			
			switch( i )
			{
				case 0:
					assertEquals( v.getName(), "x"); //$NON-NLS-1$
					assertEquals( pointerOp, ASTPointerOperator.POINTER );
					assertFalse( pointerOperators.hasNext());
					break;
				case 1:
					assertEquals( v.getName(), "y"); //$NON-NLS-1$
					assertEquals( pointerOp, ASTPointerOperator.REFERENCE);
					assertFalse( pointerOperators.hasNext()); 
					break;
				case 2:  
					assertEquals( v.getName(), "z"); //$NON-NLS-1$
					assertEquals( pointerOp, ASTPointerOperator.CONST_POINTER );
					assertEquals( pointerOperators.next(), ASTPointerOperator.VOLATILE_POINTER );
					assertEquals( pointerOperators.next(), ASTPointerOperator.POINTER );
					assertFalse( pointerOperators.hasNext());
					break;
			}
		}
		assertFalse( declarations.hasNext() );
	}
	
	public void testBug26467() throws Exception
	{
		StringWriter code = new StringWriter(); 
		code.write(	"struct foo { int fooInt; char fooChar;	};\n" ); //$NON-NLS-1$
		code.write( "typedef struct foo fooStruct;\n" ); //$NON-NLS-1$
		code.write( "typedef struct { int anonInt; char anonChar; } anonStruct;\n" ); //$NON-NLS-1$
		Iterator declarations = parse( code.toString()).getDeclarations();
		IASTClassSpecifier classSpec = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		assertEquals( classSpec.getClassKind(), ASTClassKind.STRUCT);
		assertEquals( classSpec.getName(), "foo" ); //$NON-NLS-1$
		Iterator members = classSpec.getDeclarations();
		IASTField field = (IASTField)members.next(); 
		assertSimpleType(field, IASTSimpleTypeSpecifier.Type.INT );
		assertEquals( field.getName(), "fooInt"); //$NON-NLS-1$
		field = (IASTField)members.next(); 
		assertSimpleType(field, IASTSimpleTypeSpecifier.Type.CHAR );
		assertEquals( field.getName(), "fooChar"); //$NON-NLS-1$
		assertFalse( members.hasNext());
		IASTTypedefDeclaration firstTypeDef = (IASTTypedefDeclaration)declarations.next();
		assertEquals( ((IASTElaboratedTypeSpecifier)firstTypeDef.getAbstractDeclarator().getTypeSpecifier()).getClassKind(), ASTClassKind.STRUCT );
		assertEquals( ((IASTElaboratedTypeSpecifier)firstTypeDef.getAbstractDeclarator().getTypeSpecifier()).getName(), "foo"); //$NON-NLS-1$
		assertEquals( firstTypeDef.getName(), "fooStruct"); //$NON-NLS-1$
		IASTTypedefDeclaration secondTypeDef = (IASTTypedefDeclaration)declarations.next();
		classSpec = (IASTClassSpecifier)secondTypeDef.getAbstractDeclarator().getTypeSpecifier();
		assertEquals( classSpec.getClassKind(), ASTClassKind.STRUCT);
		assertEquals( classSpec.getName(), "" ); //$NON-NLS-1$
		members = classSpec.getDeclarations();
		field = (IASTField)members.next(); 
		assertSimpleType(field, IASTSimpleTypeSpecifier.Type.INT );
		assertEquals( field.getName(), "anonInt"); //$NON-NLS-1$
		field = (IASTField)members.next(); 
		assertSimpleType(field, IASTSimpleTypeSpecifier.Type.CHAR );
		assertEquals( field.getName(), "anonChar"); //$NON-NLS-1$
		assertFalse( members.hasNext());
		assertEquals( secondTypeDef.getName(), "anonStruct"); //$NON-NLS-1$
		
	}
	
	public void testASMDefinition() throws Exception
	{
		Iterator declarations = parse( "asm( \"mov ep1 ds2\");" ).getDeclarations(); //$NON-NLS-1$
		IASTASMDefinition asm = (IASTASMDefinition)declarations.next(); 
		assertFalse( declarations.hasNext());
		assertEquals( asm.getBody(), "mov ep1 ds2"); //$NON-NLS-1$
	}
	
	public void testConstructorChain() throws Exception
	{
		Iterator declarations = parse( "TrafficLight_Actor::TrafficLight_Actor( RTController * rtg_rts, RTActorRef * rtg_ref )	: RTActor( rtg_rts, rtg_ref ), myId( 0 ) {}" ).getDeclarations(); //$NON-NLS-1$
		declarations.next(); // cannot properly do this test now with new callback structure in quickparse mode
	}
	
	public void testBug36237() throws Exception
	{
		parse( "A::A():B( (char *)0 ){}" );    //$NON-NLS-1$
	}
	
	public void testBug36532() throws Exception
	{
		try
		{
			parse( "template<int f() {\n" ); //$NON-NLS-1$
			fail( "We should not make it this far"); //$NON-NLS-1$
		}
		catch( ParserException pe )
		{
		}
		catch( Exception e )
		{
			fail( "We should have gotten a ParserException rather than" + e); //$NON-NLS-1$
		}
	}
	
	public void testPreprocessor() throws Exception {
		
		String code = "#include <stdio.h>\n#define DEF VALUE\n"; //$NON-NLS-1$
		
		IASTCompilationUnit tu = parse( code.toString()  );
		assertFalse( tu.getDeclarations().hasNext());
		Iterator inclusions = quickParseCallback.getInclusions();
		Iterator macros = quickParseCallback.getMacros();
		
		IASTInclusion i = (IASTInclusion)inclusions.next(); 
		assertFalse( inclusions.hasNext());
		
		assertEquals( i.getName(), "stdio.h"); //$NON-NLS-1$
		assertEquals( i.getStartingOffset(), 0 ); 
		assertEquals( i.getNameOffset(), code.indexOf("stdio.h") );  //$NON-NLS-1$
		assertEquals( i.getEndingOffset(), code.indexOf(">") + 1); //$NON-NLS-1$
		
		
		IASTMacro m = (IASTMacro)macros.next();
		assertEquals( m.getName(), "DEF" );  //$NON-NLS-1$
		assertEquals( m.getStartingOffset(), code.indexOf("#define") ); //$NON-NLS-1$
		assertEquals( m.getNameOffset(), code.indexOf("DEF") ); //$NON-NLS-1$
		assertEquals( m.getEndingOffset(), code.indexOf("VALUE") + 5); //$NON-NLS-1$
	}
	
	public void testTemplateDeclarationOfFunction() throws Exception
	{
		Iterator declarations = parse( "template<class A, typename B=C> A aTemplatedFunction( B bInstance );").getDeclarations(); //$NON-NLS-1$
		IASTTemplateDeclaration templateDeclaration = (IASTTemplateDeclaration)declarations.next();
		assertFalse( declarations.hasNext());
		Iterator templateParms = templateDeclaration.getTemplateParameters();
		IASTTemplateParameter parm = (IASTTemplateParameter)templateParms.next(); 
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.CLASS );
		assertEquals( parm.getIdentifier(), "A"); //$NON-NLS-1$
		parm = (IASTTemplateParameter)templateParms.next();
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.TYPENAME );
		assertEquals( parm.getIdentifier(), "B"); //$NON-NLS-1$
		assertEquals( parm.getDefaultValueIdExpression(), "C" ); //$NON-NLS-1$
		IASTFunction f = (IASTFunction)templateDeclaration.getOwnedDeclaration();
		assertEquals( f.getName(), "aTemplatedFunction" ); //$NON-NLS-1$
		assertSimpleReturnType( f, IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME );
		assertEquals( ((IASTSimpleTypeSpecifier)f.getReturnType().getTypeSpecifier()).getTypename(), "A" ); //$NON-NLS-1$
		Iterator parameters = f.getParameters();
		IASTParameterDeclaration parmDeclaration = (IASTParameterDeclaration)parameters.next();
		assertFalse( parameters.hasNext() );
		assertEquals( parmDeclaration.getName(), "bInstance"); //$NON-NLS-1$
		assertEquals( ((IASTSimpleTypeSpecifier)parmDeclaration.getTypeSpecifier()).getType(),IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME );
		assertEquals( ((IASTSimpleTypeSpecifier)parmDeclaration.getTypeSpecifier()).getTypename(), "B" ); //$NON-NLS-1$
	}
	
	public void testTemplateDeclarationOfClass() throws Exception {
		Iterator declarations = parse( "template<class T, typename Tibor = junk, class, typename, int x, float y,template <class Y> class, template<class A> class AClass> class myarray { /* ... */ };").getDeclarations(); //$NON-NLS-1$
		IASTTemplateDeclaration templateDeclaration = (IASTTemplateDeclaration)declarations.next();
		assertFalse( declarations.hasNext());
		Iterator templateParms = templateDeclaration.getTemplateParameters();
		IASTTemplateParameter parm = (IASTTemplateParameter)templateParms.next(); 
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.CLASS );
		assertEquals( parm.getIdentifier(), "T"); //$NON-NLS-1$
		parm = (IASTTemplateParameter)templateParms.next(); 
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.TYPENAME );
		assertEquals( parm.getIdentifier(), "Tibor"); //$NON-NLS-1$
		assertEquals( parm.getDefaultValueIdExpression(), "junk"); //$NON-NLS-1$
		parm = (IASTTemplateParameter)templateParms.next();
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.CLASS );
		assertEquals( parm.getIdentifier(), ""); //$NON-NLS-1$
		parm = (IASTTemplateParameter)templateParms.next();
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.TYPENAME );
		assertEquals( parm.getIdentifier(), ""); //$NON-NLS-1$
		parm = (IASTTemplateParameter)templateParms.next();
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.PARAMETER );
		assertEquals( parm.getParameterDeclaration().getName(), "x"); //$NON-NLS-1$
		assertEquals( ((IASTSimpleTypeSpecifier)parm.getParameterDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT ); 
		parm = (IASTTemplateParameter)templateParms.next();
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.PARAMETER );
		assertEquals( parm.getParameterDeclaration().getName(), "y"); //$NON-NLS-1$
		assertEquals( ((IASTSimpleTypeSpecifier)parm.getParameterDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.FLOAT ); 
		parm = (IASTTemplateParameter)templateParms.next();
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.TEMPLATE_LIST);
		assertEquals( parm.getIdentifier(), ""); //$NON-NLS-1$
		Iterator subParms = parm.getTemplateParameters();
		parm = (IASTTemplateParameter)subParms.next(); 
		assertFalse( subParms.hasNext() ); 
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.CLASS ); 
		assertEquals( parm.getIdentifier(), "Y" ); //$NON-NLS-1$
		parm = (IASTTemplateParameter)templateParms.next();
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.TEMPLATE_LIST);
		assertEquals( parm.getIdentifier(), "AClass"); //$NON-NLS-1$
		subParms = parm.getTemplateParameters();
		parm = (IASTTemplateParameter)subParms.next(); 
		assertFalse( subParms.hasNext() ); 
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.CLASS ); 
		assertEquals( parm.getIdentifier(), "A" ); //$NON-NLS-1$
		assertFalse( templateParms.hasNext() );
		IASTClassSpecifier classSpec = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)templateDeclaration.getOwnedDeclaration()).getTypeSpecifier();
		assertEquals( classSpec.getName(), "myarray"); //$NON-NLS-1$
		assertFalse( classSpec.getDeclarations().hasNext() );
	}
	
	public void testBug35906() throws Exception
	{
		StringWriter code = new StringWriter(); 
		code.write( "void TTest::MTest() {}\n" );  //$NON-NLS-1$
		code.write( "struct TTest::STest *TTest::FTest (int i) {}\n" );  //$NON-NLS-1$
		Iterator declarations = parse( code.toString() ).getDeclarations();
		IASTFunction f = (IASTFunction)declarations.next(); 
		assertEquals( f.getName(), "TTest::MTest"); //$NON-NLS-1$
		assertSimpleReturnType( f, IASTSimpleTypeSpecifier.Type.VOID 	);
		f = (IASTFunction)declarations.next();
		assertFalse( declarations.hasNext()); 
		assertEquals( f.getName(), "TTest::FTest"); //$NON-NLS-1$
		assertEquals( ((IASTElaboratedTypeSpecifier)f.getReturnType().getTypeSpecifier()).getClassKind(), ASTClassKind.STRUCT );
		assertEquals( ((IASTElaboratedTypeSpecifier)f.getReturnType().getTypeSpecifier()).getName(), "TTest::STest"); //$NON-NLS-1$
		Iterator pointerOperators = f.getReturnType().getPointerOperators(); 
		assertEquals( pointerOperators.next(), ASTPointerOperator.POINTER );
		assertFalse( pointerOperators.hasNext() ); 
		Iterator parameters = f.getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
		assertFalse( parameters.hasNext() );
		assertEquals( parm.getName(), "i"); //$NON-NLS-1$
		assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.INT );
	}
	
	public void testBug36288() throws Exception
	{
		Iterator declarations = parse( "int foo() {}\nlong foo2(){}" ).getDeclarations();   //$NON-NLS-1$
		IASTFunction f = (IASTFunction)declarations.next();
		assertSimpleReturnType( f, IASTSimpleTypeSpecifier.Type.INT );
		assertEquals( f.getName(), "foo"); //$NON-NLS-1$
		f = (IASTFunction)declarations.next();
		assertSimpleReturnType( f, IASTSimpleTypeSpecifier.Type.INT );
		assertTrue( ((IASTSimpleTypeSpecifier)f.getReturnType().getTypeSpecifier()).isLong() ); 
		assertEquals( f.getName(), "foo2"); //$NON-NLS-1$
		assertFalse( declarations.hasNext() );
	}

	public void testBug36250() throws Exception
	{
		Iterator declarations = parse( "int f( int = 0 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction f = (IASTFunction)declarations.next(); 
		assertFalse( declarations.hasNext() );
		assertSimpleReturnType( f, IASTSimpleTypeSpecifier.Type.INT );
		assertEquals( f.getName(), "f"); //$NON-NLS-1$
		Iterator parameters = f.getParameters(); 
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
		assertFalse( parameters.hasNext() );
		assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.INT );
		assertEquals( parm.getName(), "" ); //$NON-NLS-1$
		assertEquals( parm.getDefaultValue().getKind(), IASTInitializerClause.Kind.ASSIGNMENT_EXPRESSION );
		assertEquals( parm.getDefaultValue().getAssigmentExpression().getExpressionKind(), IASTExpression.Kind.PRIMARY_INTEGER_LITERAL );
		assertEquals( parm.getDefaultValue().getAssigmentExpression().getLiteralString(), "0" ); //$NON-NLS-1$
	}

	public void testBug36240() throws Exception
	{
		Iterator declarations = parse( "A & A::operator=( A ){}").getDeclarations(); //$NON-NLS-1$
		IASTFunction f = (IASTFunction)declarations.next(); 
		IASTSimpleTypeSpecifier typeSpec = (IASTSimpleTypeSpecifier)f.getReturnType().getTypeSpecifier();
		assertEquals( typeSpec.getType(), IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME );
		assertEquals( typeSpec.getTypename(), "A"); //$NON-NLS-1$
		Iterator pointerOps = f.getReturnType().getPointerOperators();
		assertEquals( pointerOps.next(), ASTPointerOperator.REFERENCE ); 
		assertFalse( pointerOps.hasNext() );
		assertEquals( f.getName(), "A::operator ="); //$NON-NLS-1$
		Iterator parms = f.getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parms.next();
		assertEquals( parm.getName(), "" ); //$NON-NLS-1$
		typeSpec = (IASTSimpleTypeSpecifier)parm.getTypeSpecifier();
		assertEquals( typeSpec.getType(), IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME );
		assertEquals( typeSpec.getTypename(), "A" ); //$NON-NLS-1$
	}
	
	public void testBug36254() throws Exception
	{
		Iterator declarations = parse( "unsigned i;\nvoid f( unsigned p1 = 0 );").getDeclarations(); //$NON-NLS-1$
		IASTVariable v = (IASTVariable)declarations.next(); 
		assertSimpleType( v, IASTSimpleTypeSpecifier.Type.INT);
		assertTrue( ((IASTSimpleTypeSpecifier)v.getAbstractDeclaration().getTypeSpecifier()).isUnsigned() ); 
		IASTFunction f = (IASTFunction)declarations.next(); 
		assertSimpleReturnType(f, IASTSimpleTypeSpecifier.Type.VOID );
		assertEquals( f.getName(), "f"); //$NON-NLS-1$
		Iterator parms = f.getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parms.next();
		assertEquals( parm.getName(), "p1"); //$NON-NLS-1$
		assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.INT );
		assertTrue( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).isUnsigned() ); 
		assertEquals( parm.getDefaultValue().getKind(), IASTInitializerClause.Kind.ASSIGNMENT_EXPRESSION );
		assertEquals( parm.getDefaultValue().getAssigmentExpression().getExpressionKind(), IASTExpression.Kind.PRIMARY_INTEGER_LITERAL );
		assertEquals( parm.getDefaultValue().getAssigmentExpression().getLiteralString(), "0" ); //$NON-NLS-1$
		assertFalse( declarations.hasNext());
	}
	
	public void testBug36432() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "#define CMD_GET		\"g\"\n" ); 	  //$NON-NLS-1$
		code.write( "#define CMD_ACTION   	\"a\"\n" ); 	  //$NON-NLS-1$
		code.write( "#define CMD_QUIT		\"q\"\n" ); //$NON-NLS-1$
		code.write( "static const memevent_cmd_func memevent_cmd_funcs[sizeof memevent_cmds - 1] = {\n"); //$NON-NLS-1$
		code.write( "memevent_get,\n"); //$NON-NLS-1$
		code.write( "memevent_action,\n"); //$NON-NLS-1$
		code.write( "memevent_quit,\n"); //$NON-NLS-1$
		code.write( "};\n"); //$NON-NLS-1$
		parse( code.toString() );
	}
	
	public void testBug36594() throws Exception
	{
		parse( "const int n = sizeof(A) / sizeof(B);"); //$NON-NLS-1$
	}
	
	public void testBug36794() throws Exception
	{
		parse( "template<> class allocator<void> {};"); //$NON-NLS-1$
		Iterator i = quickParseCallback.iterateOffsetableElements();
		while( i.hasNext() )
			assertNotNull( i.next() );
	}
	
	public void testBug36799() throws Exception
	{
		parse( "static const int __WORD_BIT = int(CHAR_BIT*sizeof(unsigned int));"); //$NON-NLS-1$
	}


	public void testBug36764() throws Exception
	{
		parse( "struct{ int x : 4; int y : 8; };" ); //$NON-NLS-1$
	}
	
	public void testOrder() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "#define __SGI_STL_INTERNAL_ALGOBASE_H\n" );  //$NON-NLS-1$
		code.write( "#include <string.h>\n" );  //$NON-NLS-1$
		code.write( "template <class _Tp>\n" );  //$NON-NLS-1$
		code.write( "inline void swap(_Tp& __a, _Tp& __b) {\n" );  //$NON-NLS-1$
		code.write( "__STL_REQUIRES(_Tp, _Assignable);\n" );  //$NON-NLS-1$
		code.write( "_Tp __tmp = __a;\n" );  //$NON-NLS-1$
		code.write( "__a = __b;\n" );  //$NON-NLS-1$
		code.write( "__b = __tmp;\n" );  //$NON-NLS-1$
		code.write( "}\n" );  //$NON-NLS-1$
		
		parse( code.toString() );
		Iterator i = quickParseCallback.iterateOffsetableElements();
		assertTrue( i.hasNext() );
		assertTrue( i.next() instanceof IASTMacro );  
		assertTrue( i.hasNext() );
		assertTrue( i.next() instanceof IASTInclusion );
		assertTrue( i.hasNext() );
		assertTrue( i.next() instanceof IASTDeclaration );
		assertFalse( i.hasNext() );
	}
	
	public void testBug36771() throws Exception {
		Writer code = new StringWriter();
		code.write("#include /**/ \"foo.h\"\n"); //$NON-NLS-1$
	
		parse( code.toString()  );
	
		Iterator includes = quickParseCallback.getInclusions();
	
		IASTInclusion include = (IASTInclusion)includes.next();
		assertTrue( include.getName().equals("foo.h") ); //$NON-NLS-1$
		assertFalse( includes.hasNext() );
	}
	
	
	public void testBug36811() throws Exception
	{
		Writer code = new StringWriter();  
		code.write( "using namespace std;\n" );  //$NON-NLS-1$
		code.write( "class Test {};" ); //$NON-NLS-1$
		parse( code.toString() );
		Iterator i = quickParseCallback.iterateOffsetableElements();
		while( i.hasNext() )
			assertNotNull( i.next() );
	}

	public void testBug36708() throws Exception {
		Iterator declarations = parse("enum { isPointer = PointerTraits<T>::result };").getDeclarations(); //$NON-NLS-1$
		IASTEnumerationSpecifier enumSpec = (IASTEnumerationSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		assertFalse( declarations.hasNext() );
		Iterator enumerators = enumSpec.getEnumerators();	
		IASTEnumerator enumerator = (IASTEnumerator)enumerators.next(); 
		assertFalse( enumerators.hasNext() );
		assertEquals( enumerator.getName(), "isPointer"); //$NON-NLS-1$
		assertEquals( enumerator.getInitialValue().getExpressionKind(), IASTExpression.Kind.ID_EXPRESSION ); 
		assertEquals( enumerator.getInitialValue().getIdExpression(), "PointerTraits<T>::result"); //$NON-NLS-1$
	}

	public void testBug36690() throws Exception {
		parse("Functor(const Functor& rhs) : spImpl_(Impl::Clone(rhs.spImpl_.get())){}").getDeclarations(); //$NON-NLS-1$
	}

	public void testBug36703() throws Exception {
		parse("const std::type_info& Get() const;"); //$NON-NLS-1$
	}
	
	public void testBug36692() throws Exception  {
		Writer code = new StringWriter();
		code.write("template <typename T, typename Destroyer>\n"); //$NON-NLS-1$
		code.write("void SetLongevity(T* pDynObject, unsigned int longevity,\n"); //$NON-NLS-1$
		code.write("Destroyer d = Private::Deleter<T>::Delete){}\n"); //$NON-NLS-1$
		parse(code.toString());
	}
	
	public void testBug36551() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "class TextFrame {\n" );  //$NON-NLS-1$
		code.write( "BAD_MACRO()\n");  //$NON-NLS-1$
		code.write( "};"); //$NON-NLS-1$
		parse( code.toString(), true, false );
	}
	
	public void testBug36247() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "class A {\n" );  //$NON-NLS-1$
		code.write( "INLINE_DEF int f ();\n" );  //$NON-NLS-1$
		code.write( "INLINE_DEF A   g ();" );  //$NON-NLS-1$
		code.write( "INLINE_DEF A * h ();" );  //$NON-NLS-1$
		code.write( "INLINE_DEF A & unlock( void );"); //$NON-NLS-1$
		code.write( "};" ); //$NON-NLS-1$
		parse(code.toString());
	}
	
	public void testStruct() throws Exception
	{
		StringWriter writer = new StringWriter(); 
		writer.write( "struct mad_bitptr { unsigned char const *byte;\n" ); //$NON-NLS-1$
		writer.write( "unsigned short cache;\n unsigned short left;};" ); //$NON-NLS-1$
		parse( writer.toString() );
	}
	
	public void testBug36559() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "namespace myNameSpace {\n" );  //$NON-NLS-1$
		code.write( "template<typename T=short> class B {};\n" ); //$NON-NLS-1$
		code.write( "template<> class B<int> {};\n" );  //$NON-NLS-1$
		code.write( "}\n" );  //$NON-NLS-1$
		parse( code.toString() ); 
	}
	
	public void testPointersToFunctions() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "void (*name)( void );\n"); //$NON-NLS-1$
		code.write( "static void * (* const orig_malloc_hook)(const char *file, int line, size_t size);\n"); //$NON-NLS-1$

		Iterator declarations = parse( code.toString() ).getDeclarations();
		IASTVariable p2f = (IASTVariable)declarations.next();
		assertSimpleType( p2f, IASTSimpleTypeSpecifier.Type.VOID );
		assertEquals( p2f.getName(), "name" ); //$NON-NLS-1$
		Iterator parameters = p2f.getAbstractDeclaration().getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
		assertFalse( parameters.hasNext() );
		assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.VOID );
		assertEquals( parm.getName(), "" ); //$NON-NLS-1$
		
		p2f = (IASTVariable)declarations.next(); 
		assertSimpleType( p2f, IASTSimpleTypeSpecifier.Type.VOID );
		assertTrue( p2f.isStatic() );
		Iterator rtPo = p2f.getAbstractDeclaration().getPointerOperators();
		assertEquals( rtPo.next(), ASTPointerOperator.POINTER );
		assertFalse( rtPo.hasNext() );
		parameters = p2f.getAbstractDeclaration().getParameters();
		parm = (IASTParameterDeclaration)parameters.next(); 
	    assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.CHAR );
	    assertEquals( parm.getName(), "file" ); //$NON-NLS-1$
	    assertTrue( parm.isConst() );
	    assertTrue( parm.getPointerOperators().hasNext() );
		parm = (IASTParameterDeclaration)parameters.next(); 
		assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.INT );
		assertEquals( parm.getName(), "line" ); //$NON-NLS-1$
		parm = (IASTParameterDeclaration)parameters.next(); 
		assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME );
		assertEquals( parm.getName(), "size" ); //$NON-NLS-1$
		assertFalse( parameters.hasNext() );		
	}
	
	public void testBug36600() throws Exception
	{
		IASTVariable p2f = (IASTVariable)parse( "enum mad_flow (*input_func)(void *, struct mad_stream *);").getDeclarations().next(); //$NON-NLS-1$
		IASTElaboratedTypeSpecifier elab = (IASTElaboratedTypeSpecifier)p2f.getAbstractDeclaration().getTypeSpecifier();
		assertEquals( elab.getName(), "mad_flow"); //$NON-NLS-1$
		assertEquals( elab.getClassKind(), ASTClassKind.ENUM );
		assertEquals( p2f.getName(), "input_func"); //$NON-NLS-1$
		Iterator parms = p2f.getAbstractDeclaration().getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parms.next();
		assertEquals( parm.getName(), "" ); //$NON-NLS-1$
		assertEquals( parm.getPointerOperators().next(), ASTPointerOperator.POINTER);
		assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.VOID);
		parm = (IASTParameterDeclaration)parms.next();
		assertEquals( parm.getName(), "" ); //$NON-NLS-1$
		assertEquals( parm.getPointerOperators().next(), ASTPointerOperator.POINTER);
		elab = (IASTElaboratedTypeSpecifier)parm.getTypeSpecifier();
		assertEquals( elab.getName(), "mad_stream"); //$NON-NLS-1$
		assertEquals( elab.getClassKind(), ASTClassKind.STRUCT );
		
		
	}

	public void testBug36713() throws Exception {
		Writer code = new StringWriter();
		code.write("A ( * const fPtr) (void *); \n"); //$NON-NLS-1$
		code.write("A (* const fPtr2) ( A * ); \n"); //$NON-NLS-1$
		parse(code.toString()).getDeclarations();
	}

	// K&R Test hasn't been ported from DOMTests
	// still need to figure out how to represent these in the AST
//	public void testOldKRFunctionDeclarations() throws Exception
//	{
//		// Parse and get the translaton unit
//		Writer code = new StringWriter();
//		code.write("bool myFunction( parm1, parm2, parm3 )\n");
//		code.write("const char* parm1;\n");
//		code.write("int (*parm2)(float);\n");
//		code.write("{}");
//		TranslationUnit translationUnit = parse(code.toString());
//
//		// Get the declaration
//		List declarations = translationUnit.getDeclarations();
//		assertEquals(1, declarations.size());
//		SimpleDeclaration simpleDeclaration = (SimpleDeclaration)declarations.get(0);
//		assertEquals( simpleDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_bool );
//		List declarators  = simpleDeclaration.getDeclarators(); 
//		assertEquals( 1, declarators.size() ); 
//		Declarator functionDeclarator = (Declarator)declarators.get( 0 ); 
//		assertEquals( functionDeclarator.getName().toString(), "myFunction" );
//        
//		ParameterDeclarationClause pdc = functionDeclarator.getParms(); 
//		assertNotNull( pdc ); 
//		List parameterDecls = pdc.getDeclarations(); 
//		assertEquals( 3, parameterDecls.size() );
//		ParameterDeclaration parm1 = (ParameterDeclaration)parameterDecls.get( 0 );
//		assertNotNull( parm1.getDeclSpecifier().getName() );
//		assertEquals( "parm1", parm1.getDeclSpecifier().getName().toString() );
//		List parm1Decls = parm1.getDeclarators(); 
//		assertEquals( 1, parm1Decls.size() ); 
//
//		ParameterDeclaration parm2 = (ParameterDeclaration)parameterDecls.get( 1 );
//		assertNotNull( parm2.getDeclSpecifier().getName() );
//		assertEquals( "parm2", parm2.getDeclSpecifier().getName().toString() );
//		List parm2Decls = parm2.getDeclarators(); 
//		assertEquals( 1, parm2Decls.size() );
//        
//		ParameterDeclaration parm3 = (ParameterDeclaration)parameterDecls.get( 2 );
//		assertNotNull( parm3.getDeclSpecifier().getName() );
//		assertEquals( "parm3", parm3.getDeclSpecifier().getName().toString() );
//		List parm3Decls = parm3.getDeclarators(); 
//		assertEquals( 1, parm3Decls.size() );
//        
//		OldKRParameterDeclarationClause clause = pdc.getOldKRParms(); 
//		assertNotNull( clause );
//		assertEquals( clause.getDeclarations().size(), 2 );
//		SimpleDeclaration decl1 = (SimpleDeclaration)clause.getDeclarations().get(0);
//		assertEquals( decl1.getDeclarators().size(), 1 );
//		assertTrue(decl1.getDeclSpecifier().isConst());
//		assertFalse(decl1.getDeclSpecifier().isVolatile());
//		assertEquals( decl1.getDeclSpecifier().getType(), DeclSpecifier.t_char);
//		Declarator declarator1 = (Declarator)decl1.getDeclarators().get( 0 );
//		assertEquals( declarator1.getName().toString(), "parm1" );
//		List ptrOps1 = declarator1.getPointerOperators();
//		assertNotNull( ptrOps1 );
//		assertEquals( 1, ptrOps1.size() );
//		PointerOperator po1 = (PointerOperator)ptrOps1.get(0);
//		assertNotNull( po1 ); 
//		assertFalse( po1.isConst() );
//		assertFalse( po1.isVolatile() );
//		assertEquals( po1.getType(), PointerOperator.t_pointer );
//        
//		SimpleDeclaration declaration = (SimpleDeclaration)clause.getDeclarations().get(1);
//		assertEquals( declaration.getDeclSpecifier().getType(), DeclSpecifier.t_int );
//		assertEquals( declaration.getDeclarators().size(), 1);
//		assertNull( ((Declarator)declaration.getDeclarators().get(0)).getName() );
//		assertNotNull( ((Declarator)declaration.getDeclarators().get(0)).getDeclarator() );
//		assertEquals( ((Declarator)declaration.getDeclarators().get(0)).getDeclarator().getName().toString(), "parm2" );
//		ParameterDeclarationClause clause2 = ((Declarator)declaration.getDeclarators().get(0)).getParms();
//		assertEquals( clause2.getDeclarations().size(), 1 );
//		assertEquals( ((ParameterDeclaration)clause2.getDeclarations().get(0)).getDeclarators().size(), 1 );  
//		assertNull( ((Declarator)((ParameterDeclaration)clause2.getDeclarations().get(0)).getDeclarators().get(0)).getName() );
//		assertEquals( ((ParameterDeclaration)clause2.getDeclarations().get(0)).getDeclSpecifier().getType(), DeclSpecifier.t_float );          
//	}
    
	public void testPointersToMemberFunctions() throws Exception
	{
		IASTVariable p2m  = (IASTVariable)parse("void (A::*name)(void);").getDeclarations().next(); //$NON-NLS-1$
		assertSimpleType( p2m, IASTSimpleTypeSpecifier.Type.VOID );
		assertEquals( p2m.getName(), "A::* name"); //$NON-NLS-1$
		assertEquals( p2m.getAbstractDeclaration().getPointerToFunctionOperator(), ASTPointerOperator.POINTER);
		Iterator parameters = p2m.getAbstractDeclaration().getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
		assertFalse( parameters.hasNext() );
		assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.VOID );
		assertEquals( parm.getName(), "" ); //$NON-NLS-1$
	}
     
	public void testBug39550() throws Exception
	{
		parse("double x = 0x1.fp1;").getDeclarations().next(); //$NON-NLS-1$
	}
	
	// digraphs/trigraphs have been temporarily remove
	public void testBug39552A(int x) throws Exception
	{
		Writer code = new StringWriter();

		code.write("%:define glue(x, y) x %:%: y	/* #define glue(x, y) x ## y. */\n"); //$NON-NLS-1$
		code.write("#ifndef glue\n"); //$NON-NLS-1$
		code.write("#error glue not defined!\n"); //$NON-NLS-1$
		code.write("#endif\n"); //$NON-NLS-1$
        
		code.write("%:define str(x) %:x		/* #define str(x) #x */\n"); //$NON-NLS-1$
        
		code.write("int main (int argc, char *argv<::>) /* argv[] */\n"); //$NON-NLS-1$
		code.write("glue (<, %) /* { */\n"); //$NON-NLS-1$
		code.write("			 /* di_str[] = */\n"); //$NON-NLS-1$
		code.write("  const char di_str glue(<, :)glue(:, >) = str(%:%:<::><%%>%:);\n"); //$NON-NLS-1$
		code.write("  /* Check the glue macro actually pastes, and that the spelling of\n"); //$NON-NLS-1$
		code.write("	 all digraphs is preserved.  */\n"); //$NON-NLS-1$
		code.write("  if (glue(strc, mp) (di_str, \"%:%:<::><%%>%:\"))\n"); //$NON-NLS-1$
		code.write("	err (\"Digraph spelling not preserved!\");\n"); //$NON-NLS-1$
		code.write("  return 0;\n"); //$NON-NLS-1$
		code.write("glue (%, >) /* } */\n"); //$NON-NLS-1$

		parse(code.toString());
	}
    
	// digraphs/trigraphs have been temporarily remove
	public void testBug39552B(int x) throws Exception
	{
		Writer code = new StringWriter();

		code.write("??=include <stdio.h>\n"); //$NON-NLS-1$
		code.write("??=define TWELVE 1??/\n"); //$NON-NLS-1$
		code.write("2\n"); //$NON-NLS-1$
        
		code.write("static const char str??(??) = \"0123456789??/n\";\n"); //$NON-NLS-1$
        
		code.write("int\n"); //$NON-NLS-1$
		code.write("main(void)\n"); //$NON-NLS-1$
		code.write("??<\n"); //$NON-NLS-1$
		code.write("  unsigned char x = 5;\n"); //$NON-NLS-1$
		code.write("  if (sizeof str != TWELVE)\n"); //$NON-NLS-1$
		code.write("	abort ();\n"); //$NON-NLS-1$
		code.write("  /* Test ^=, the only multi-character token to come from trigraphs.  */\n"); //$NON-NLS-1$
		code.write("  x ??'= 3;\n"); //$NON-NLS-1$
		code.write("  if (x != 6)\n"); //$NON-NLS-1$
		code.write("	abort ();\n"); //$NON-NLS-1$
		code.write("  if ((5 ??! 3) != 7)\n"); //$NON-NLS-1$
		code.write("	abort ();\n"); //$NON-NLS-1$
		code.write("  return 0;\n"); //$NON-NLS-1$
		code.write("??>\n"); //$NON-NLS-1$
		
		parse(code.toString());
	}
	
	public void testBug39553() throws Exception	
	{
		parse("#define COMP_INC \"foobar.h\"  \n" + "#include COMP_INC\n"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue( quickParseCallback.getInclusions().hasNext() );
	}
	
	public void testBug39537() throws Exception
	{
		parse("typedef foo<(U::id > 0)> foobar;"); //$NON-NLS-1$
		assertTrue( quickParseCallback.getCompilationUnit().getDeclarations().hasNext() );
	}
	
	public void testBug39546() throws Exception
	{
		parse("signed char c = (signed char) 0xffffffff;"); //$NON-NLS-1$
		assertTrue( quickParseCallback.getCompilationUnit().getDeclarations().hasNext() );
	}
	
	public void testIndirectDeclarators() throws Exception
	{
		IASTVariable v = (IASTVariable)parse( "void (*x)( int );").getDeclarations().next(); //$NON-NLS-1$
		assertEquals( v.getName(), "x"); //$NON-NLS-1$
		assertSimpleType( v, IASTSimpleTypeSpecifier.Type.VOID );
		assertParameterSimpleType( (IASTParameterDeclaration)v.getAbstractDeclaration().getParameters().next(), IASTSimpleTypeSpecifier.Type.INT  );
		assertEquals( v.getAbstractDeclaration().getPointerToFunctionOperator(), ASTPointerOperator.POINTER );
		
		v = (IASTVariable)parse( "const int * (* const something)( const int * const * const  );").getDeclarations().next(); //$NON-NLS-1$
		assertEquals( v.getName(), "something"); //$NON-NLS-1$
		assertEquals( v.getAbstractDeclaration().getPointerToFunctionOperator(), ASTPointerOperator.CONST_POINTER);
		assertTrue( v.getAbstractDeclaration().isConst() );
		assertSimpleType( v, IASTSimpleTypeSpecifier.Type.INT );
		assertEquals( v.getAbstractDeclaration().getPointerOperators().next(), ASTPointerOperator.POINTER );
		IASTParameterDeclaration parm = (IASTParameterDeclaration)v.getAbstractDeclaration().getParameters().next();
		assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.INT );
		Iterator pointerOps = parm.getPointerOperators();
		assertEquals( pointerOps.next(), ASTPointerOperator.CONST_POINTER );
		assertEquals( pointerOps.next(), ASTPointerOperator.CONST_POINTER );
		assertFalse( pointerOps.hasNext() );
		
		IASTTypedefDeclaration typedef = (IASTTypedefDeclaration)parse( "typedef void (*life)(int);").getDeclarations().next(); //$NON-NLS-1$
		assertEquals( typedef.getName(), "life"); //$NON-NLS-1$
		assertSimpleType( typedef, IASTSimpleTypeSpecifier.Type.VOID );
		assertParameterSimpleType( (IASTParameterDeclaration)typedef.getAbstractDeclarator().getParameters().next(), IASTSimpleTypeSpecifier.Type.INT  );
		
		IASTFunction f = (IASTFunction)parse( "void (f)(void);").getDeclarations().next(); //$NON-NLS-1$
		assertEquals( f.getName(), "f"); //$NON-NLS-1$
		
		typedef = (IASTTypedefDeclaration)parse( "typedef void (life)(int);").getDeclarations().next(); //$NON-NLS-1$
		assertEquals( typedef.getName(), "life"); //$NON-NLS-1$
		
	}
	
	public void testBug39532() throws Exception
	{
		parse("class N1::N2::B : public A {};"); //$NON-NLS-1$
		assertTrue( quickParseCallback.getCompilationUnit().getDeclarations().hasNext() );
	}
	
	public void testBug39540() throws Exception
	{
		parse("class {} const null;"); //$NON-NLS-1$
		assertTrue( quickParseCallback.getCompilationUnit().getDeclarations().hasNext() );
	}
	
	public void testBug39530() throws Exception
	{
		parse( "X sPassed(-1)"); //$NON-NLS-1$
	}

	public void testBug39526() throws Exception
	{
		parse("UnitList unit_list (String(\"keV\"));"); //$NON-NLS-1$
	}
	
	public void testBug39535() throws Exception
	{
		parse("namespace bar = foo;"); //$NON-NLS-1$
	}

	public void testBug39504B() throws Exception
	{
		parse("int y = sizeof (int*);"); //$NON-NLS-1$
	}
	public void testBug39505A() throws Exception
	{
		parse("int AD::* gp_down = static_cast<int AD::*>(gp_stat);"); //$NON-NLS-1$
	}
	public void testBug39505B() throws Exception
	{
		parse("int* gp_down = static_cast<int*>(gp_stat);"); //$NON-NLS-1$
	}
	
	public void testBug42985() throws Exception
	{
		parse( "const int x = 4; int y = ::x;"); //$NON-NLS-1$
	}

    public void testBug40419() throws Exception
	{
		Writer code = new StringWriter();
		try
		{ 
			code.write( "template <class T, class U>	struct SuperSubclass {\n"  ); //$NON-NLS-1$
			code.write( "enum { value = (::Loki::Conversion<const volatile U*, const volatile T*>::exists && \n" ); //$NON-NLS-1$
			code.write( "!::Loki::Conversion<const volatile T*, const volatile void*>::sameType) };	};" ); //$NON-NLS-1$
		} catch( IOException ioe ){}
		parse( code.toString() );
	}

	public void testBug39556() throws Exception
	{
		parse("int *restrict ip_fn (void);", true, true, ParserLanguage.C).getDeclarations().next(); //$NON-NLS-1$

	}
			
	/**
	 * Test code: struct Example { Example(); Example(int); ~Example();};
	 * Purpose: tests a declaration in a class scope.
	 */
	public void testBug43371 () throws Exception
	{
		// Parse and get the translaton unit
		Writer code = new StringWriter();
		code.write("struct Example { Example(); Example(int); ~Example();};"); //$NON-NLS-1$
		IASTCompilationUnit cu = parse(code.toString());
		Iterator i = cu.getDeclarations();
		assertTrue(i.hasNext());
		IASTAbstractTypeSpecifierDeclaration declaration =
			(IASTAbstractTypeSpecifierDeclaration)i.next();
		assertFalse(i.hasNext());
		assertTrue(	declaration.getTypeSpecifier() instanceof IASTClassSpecifier);
		assertTrue(((IASTClassSpecifier)declaration.getTypeSpecifier()).getClassKind()== ASTClassKind.STRUCT);
		Iterator j =((IASTClassSpecifier)declaration.getTypeSpecifier()).getDeclarations();
		assertTrue(j.hasNext());
		IASTMethod m1 = (IASTMethod)j.next();
		IASTMethod m2 = (IASTMethod)j.next();
		IASTMethod m3 = (IASTMethod)j.next();
		assertFalse(j.hasNext());
		assertTrue(m1.getVisiblity() == ASTAccessVisibility.PUBLIC);
		assertTrue(m2.getVisiblity() == ASTAccessVisibility.PUBLIC);
		assertTrue(m3.getVisiblity() == ASTAccessVisibility.PUBLIC);
	}
	
	public void testBug43644() throws Exception
	{
		Iterator i = parse( "void foo();{ int x; }", true, false ).getDeclarations(); //$NON-NLS-1$
		IASTFunction f = (IASTFunction)i.next();
		assertEquals( f.getName(), "foo");  //$NON-NLS-1$
		assertFalse( i.hasNext() );		
	}
	
	public void testBug43062() throws Exception
	{
		Iterator i = parse( "class X { operator short  (); 	operator int unsigned(); operator int signed(); };").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier classX = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		assertFalse( i.hasNext() );
		Iterator members = classX.getDeclarations();
        IASTMethod shortMethod = (IASTMethod)members.next();
        IASTMethod unsignedMethod = (IASTMethod)members.next();
        IASTMethod signedMethod = (IASTMethod)members.next(); 
        assertFalse( members.hasNext() );
		assertEquals( shortMethod.getName(), "operator short"); //$NON-NLS-1$
		assertEquals( unsignedMethod.getName(), "operator int unsigned"); //$NON-NLS-1$
		assertEquals( signedMethod.getName(), "operator int signed"); //$NON-NLS-1$
	}

	public void testBug39531() throws Exception
	{
		parse("class AString { operator char const *() const; };"); //$NON-NLS-1$
	}
	
	public void testBug40007() throws Exception
	{
		assertCodeFailsParse("int y = #;"); //$NON-NLS-1$
	}
	
	public void testBug40759() throws Exception
	{
		IASTClassSpecifier classSpec = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)assertSoleDeclaration( "#define X SomeName \n class X {};" )).getTypeSpecifier(); //$NON-NLS-1$
		assertEquals( classSpec.getNameOffset() + 1, classSpec.getNameEndOffset() );
		assertEquals( classSpec.getName(), "SomeName"); //$NON-NLS-1$
	}
	
	public void testBug44633() throws Exception
	{
		Writer writer = new StringWriter(); 
		writer.write( "template <typename T> class A {};\n" );  //$NON-NLS-1$
		writer.write( "class B {  template <typename T> friend class A;\n" );  //$NON-NLS-1$
		writer.write( "void method();\n" );  //$NON-NLS-1$
		writer.write( "};\n" );  //$NON-NLS-1$
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		assertTrue( i.next() instanceof IASTTemplateDeclaration );  
		IASTClassSpecifier classB = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		Iterator members = classB.getDeclarations(); 
		assertTrue (members.next() instanceof IASTTemplateDeclaration );  
		assertTrue( members.next() instanceof IASTMethod );  
		assertFalse( i.hasNext() );
	}

	public void testBug39525() throws Exception
	{
		parse("C &(C::*DD)(const C &x) = &C::operator=;"); //$NON-NLS-1$
	}

	public void testBug41935() throws Exception
	{
		Iterator i = parse( "namespace A	{  int x; } namespace B = A;" ).getDeclarations(); //$NON-NLS-1$
		assertTrue( i.next() instanceof IASTNamespaceDefinition ); 
		IASTNamespaceAlias a = (IASTNamespaceAlias)i.next();
		assertEquals( a.getName(), "B" ); //$NON-NLS-1$
		assertFalse( i.hasNext() );
	}

	public void testBug39528() throws Exception
	{
		Writer code = new StringWriter();
		try
		{
			code.write("struct B: public A {\n"); //$NON-NLS-1$
			code.write("  A a;\n"); //$NON-NLS-1$
			code.write("  B() try : A(1), a(2)\n"); //$NON-NLS-1$
			code.write("	{ throw 1; }\n"); //$NON-NLS-1$
			code.write("  catch (...)\n"); //$NON-NLS-1$
			code.write("	{ if (c != 3) r |= 1; }\n"); //$NON-NLS-1$
			code.write("};\n"); //$NON-NLS-1$
		}
		catch (IOException ioe)
		{
		}
		IASTClassSpecifier structB = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)assertSoleDeclaration(code.toString())).getTypeSpecifier();
		Iterator members = structB.getDeclarations();
		assertTrue( members.next() instanceof IASTField);
		IASTMethod b = (IASTMethod)members.next();
		assertFalse( members.hasNext() );
		assertTrue( b.hasFunctionTryBlock() );
	}

	public void testBug39538() throws Exception
	{
		parse("template C::operator int<float> ();"); //$NON-NLS-1$
	}

	public void testBug39536() throws Exception
	{
		Writer writer = new StringWriter(); 
		writer.write( "template<class E>\n" ); //$NON-NLS-1$
		writer.write( "class X {\n" ); //$NON-NLS-1$
		writer.write( "X<E>();  // This fails \n" ); //$NON-NLS-1$
		writer.write( "inline X<E>(int); // This also fails \n" ); //$NON-NLS-1$
		writer.write( "inline ~X<E>(); // This works fine \n" ); //$NON-NLS-1$
		writer.write( "};\n" ); //$NON-NLS-1$
		IASTTemplateDeclaration template = (IASTTemplateDeclaration)assertSoleDeclaration( writer.toString() );
		IASTClassSpecifier X = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)template.getOwnedDeclaration()).getTypeSpecifier();
		Iterator members = X.getDeclarations();
		for( int i = 0; i < 3; ++i )
			assertTrue( members.next() instanceof IASTMethod );
		assertFalse( members.hasNext() );
	}

	public void testBug39536A() throws Exception
	{
		IASTTemplateDeclaration template = (IASTTemplateDeclaration)parse("template<class E> class X { X<E>(); };").getDeclarations().next(); //$NON-NLS-1$
		IASTClassSpecifier classX = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)template.getOwnedDeclaration()).getTypeSpecifier();
		IASTDeclaration d = (IASTDeclaration)classX.getDeclarations().next();
		assertTrue( d instanceof IASTMethod);    
	}
	public void testBug39536B() throws Exception
	{
		parse("template<class E> class X { inline X<E>(int); };"); //$NON-NLS-1$
	}

	public void testBug39542() throws Exception
	{
		parse("void f(int a, struct {int b[a];} c) {}"); //$NON-NLS-1$
	}

	//Here starts C99-specific section
	public void testBug39549() throws Exception
	{
		parse("struct X x = { .b = 40, .z = { sizeof(X), 42 }, .t[3] = 2, .t.f[3].x = A * B };", true, true, ParserLanguage.C); //$NON-NLS-1$
		// with trailing commas
		parse("struct X x = { .b = 40, .z = { sizeof(X), 42,}, .t[3] = 2, .t.f[3].x = A * B  ,};", true, true, ParserLanguage.C); //$NON-NLS-1$
	}
	
	public void testBug39551A() throws Exception
	{
		IASTFunction function = (IASTFunction)parse("extern float _Complex conjf (float _Complex);", true, true, ParserLanguage.C).getDeclarations().next(); //$NON-NLS-1$
		assertEquals( function.getName(), "conjf"); //$NON-NLS-1$
		assertTrue( ((IASTSimpleTypeSpecifier)function.getReturnType().getTypeSpecifier()).isComplex() );
	}

	public void testBug39551B() throws Exception
	{
		IASTVariable variable = (IASTVariable)parse("_Imaginary double id = 99.99 * __I__;", true, true, ParserLanguage.C).getDeclarations().next(); //$NON-NLS-1$
		assertEquals( variable.getName(), "id"); //$NON-NLS-1$
		assertTrue( ((IASTSimpleTypeSpecifier)variable.getAbstractDeclaration().getTypeSpecifier()).isImaginary() );
	}
	
	public void testCBool() throws Exception
	{
		IASTVariable variable = (IASTVariable)assertSoleDeclaration( "_Bool x;", ParserLanguage.C ); //$NON-NLS-1$
		assertSimpleType( variable, IASTSimpleTypeSpecifier.Type._BOOL );
	}

	public void testBug39678() throws Exception
	{
		IASTVariable variable = (IASTVariable) assertSoleDeclaration("char *s = L\"a\" \"b\";"); //$NON-NLS-1$
	}
	
	public void testBug43110() throws Exception
	{
		IASTFunction function = (IASTFunction) assertSoleDeclaration( "void x( int y, ... );"); //$NON-NLS-1$
		assertTrue( function.takesVarArgs() );
		function = (IASTFunction) assertSoleDeclaration( "void x( int y... );"); //$NON-NLS-1$
		assertTrue( function.takesVarArgs() );
	}
	
//	public void testBug44370() throws Exception
//	{
//		parse( "#define SWAP(x,y) {x|=y;y|=x;x|=y;}\n"); //$NON-NLS-1$
//		Iterator macros = quickParseCallback.getMacros();
//		assertNotNull(macros);
//		assertTrue( macros.hasNext());
//		IASTMacro swap = (IASTMacro) macros.next();
//		assertFalse( macros.hasNext() );
//		assertEquals( swap.getName(), "SWAP"); //$NON-NLS-1$
//		assertEquals( swap.getMacroType(), IMacroDescriptor.MacroType.FUNCTION_LIKE );
//		String [] params = swap.getParameters();
//		assertEquals( params.length, 2 );
//		assertEquals( params[0], "x"); //$NON-NLS-1$
//		assertEquals( params[1], "y"); //$NON-NLS-1$
//		String completeSignature = swap.getCompleteSignature().trim();
//		assertEquals( completeSignature, "#define SWAP(x,y) {x|=y;y|=x;x|=y;}"); //$NON-NLS-1$
//		assertEquals( swap.getExpansionSignature().trim(),"{x|=y;y|=x;x|=y;}"); //$NON-NLS-1$
//		IToken [] tokens = swap.getTokenizedExpansion();
//		validateToken( tokens[0], IToken.tLBRACE);
//		validateIdentifier( tokens[1], "x"); //$NON-NLS-1$
//		validateToken( tokens[2], IToken.tBITORASSIGN );
//		validateIdentifier( tokens[3], "y"); //$NON-NLS-1$
//		validateToken( tokens[4], IToken.tSEMI );
//		validateIdentifier( tokens[5], "y"); //$NON-NLS-1$
//		validateToken( tokens[6], IToken.tBITORASSIGN );
//		validateIdentifier( tokens[7], "x"); //$NON-NLS-1$
//		validateToken( tokens[8], IToken.tSEMI );
//		validateIdentifier( tokens[9], "x"); //$NON-NLS-1$
//		validateToken( tokens[10], IToken.tBITORASSIGN );
//		validateIdentifier( tokens[11], "y"); //$NON-NLS-1$
//		validateToken( tokens[12], IToken.tSEMI );
//		validateToken( tokens[13], IToken.tRBRACE );
//	}
//	/**
//	 * @param token
//	 * @param string
//	 */
//	private void validateIdentifier(IToken token, String identifierName ) {
//		validateToken( token, IToken.tIDENTIFIER);
//		assertEquals( token.getImage(), identifierName  );
//	}
//	/**
//	 * @param token
//	 * @param i
//	 */
//	private void validateToken(IToken token, int signal) {
//		assertEquals( token.getType(), signal );
//	}
	
	public void testBug47752() throws Exception
	{
		parse("void func( cFoo bar ) try {	} catch ( const char * error ){	}" ); //$NON-NLS-1$
	}
	
	public void testBug47628() throws Exception
	{
		Writer writer = new StringWriter(); 
		writer.write( "void h(char) { }\n"); //$NON-NLS-1$
		writer.write( "void h(unsigned char) { }\n"); //$NON-NLS-1$
		writer.write( "void h(signed char) { }  // not shown in outline, parsed as char\n"); //$NON-NLS-1$
		Iterator i = parse( writer.toString() ).getDeclarations();
		IASTFunction h1 = (IASTFunction) i.next(); 
		assertEquals( h1.getName(), "h"); //$NON-NLS-1$
		Iterator parms = h1.getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration) parms.next();
		assertTrue( parm.getTypeSpecifier() instanceof IASTSimpleTypeSpecifier ); 
		assertEquals( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).getTypename(), "char" ); //$NON-NLS-1$
		IASTFunction h2 = (IASTFunction) i.next();
		assertEquals( h2.getName(), "h"); //$NON-NLS-1$
		parms = h2.getParameters();
		parm = (IASTParameterDeclaration) parms.next();
		assertTrue( parm.getTypeSpecifier() instanceof IASTSimpleTypeSpecifier ); 
		assertEquals( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).getTypename(), "unsigned char" ); //$NON-NLS-1$
		
		IASTFunction h3 = (IASTFunction) i.next();
		assertEquals( h3.getName(), "h"); //$NON-NLS-1$
		parms = h3.getParameters();
		parm = (IASTParameterDeclaration) parms.next();
		assertTrue( parm.getTypeSpecifier() instanceof IASTSimpleTypeSpecifier ); 
		assertEquals( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).getTypename(), "signed char" ); //$NON-NLS-1$
		
		assertFalse( i.hasNext() );
	}
	
	public void testBug44336() throws Exception
	{
		Iterator i = parse( "class A {};  typedef typename A foo;" ).getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier classA = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		assertEquals( classA.getClassKind(), ASTClassKind.CLASS );
		assertEquals( classA.getName(), "A"); //$NON-NLS-1$
		IASTTypedefDeclaration typedefDeclaration = (IASTTypedefDeclaration) i.next();
		assertFalse( i.hasNext() );
	}
	
    public void testBug39705() throws Exception
    {
        parse("#ident \"@(#)filename.c   1.3 90/02/12\""); //$NON-NLS-1$
    }
        
    


    public void testBug39704B() throws Exception
    {
		IASTVariable d = (IASTVariable)assertSoleDeclaration("extern int (* import) (void) __attribute__((dllimport));"); //$NON-NLS-1$
		assertEquals( d.getName(), "import"); // false assertion  //$NON-NLS-1$
    }
    public void testBug39704C() throws Exception
    {
 		IASTFunction f = (IASTFunction)assertSoleDeclaration("int func2 (void) __attribute__((dllexport));"); //$NON-NLS-1$
		assertEquals( f.getName(), "func2"); //$NON-NLS-1$
    }
    

    
	public void testBug45235() throws Exception
	{
		Iterator i = parse( "class A { friend class B; friend void f(); }; " ).getDeclarations(); //$NON-NLS-1$
		
		IASTClassSpecifier A = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		
		i = A.getDeclarations();
		
		IASTAbstractTypeSpecifierDeclaration forewardDecl = (IASTAbstractTypeSpecifierDeclaration)i.next();
		IASTFunction f = (IASTFunction) i.next();
				
		assertTrue( forewardDecl.isFriendDeclaration() );
		assertTrue( f.isFriend() );
	}
    
    public void testBug59179() throws Exception
    {
    	Iterator i = parse( "class __decl  main{  int main; };", true, false ).getDeclarations(); //$NON-NLS-1$
    	assertFalse( i.hasNext() );
    }

    public void testBug57652() throws Exception
	{
    	parse("struct file_operations driver_fops = {  open: device_open, release: device_release	};", true, true, ParserLanguage.C ).getDeclarations(); //$NON-NLS-1$
	}
    
    
    public void testBug60142() throws Exception
	{
    	IASTVariable var = (IASTVariable) assertSoleDeclaration( "unsigned long var;"); //$NON-NLS-1$
    	assertEquals( var.getName(), "var"); //$NON-NLS-1$
    	IASTSimpleTypeSpecifier specifier = ((IASTSimpleTypeSpecifier)var.getAbstractDeclaration().getTypeSpecifier());
		assertEquals( specifier.getTypename(), "unsigned long" ); //$NON-NLS-1$
		assertTrue( specifier.isLong() );
		assertTrue( specifier.isUnsigned() );
		assertEquals( specifier.getType(), IASTSimpleTypeSpecifier.Type.INT );
	}
    
    public void testBug61431() throws Exception
	{
    	for( int i = 0; i < 2; ++i )
    	{
    		ParserLanguage language = ( i == 0 ) ? ParserLanguage.C : ParserLanguage.CPP;
			Iterator iter = parse( "int k[][] = { {0, {1}, {2,3}};", true, false, language ).getDeclarations(); //$NON-NLS-1$
			assertFalse( iter.hasNext() );
    	}
	}
    
    public void testBadIdentifier() throws Exception
	{
    	parse( "class 0302 { private: int stinks; };", true, false ); //$NON-NLS-1$	
	}
    
    public void testBug67622() throws Exception
	{
    	parse( "const char * x = __FILE__;"); //$NON-NLS-1$
	}
    
    public void testBug68116() throws Exception
	{
    	StringBuffer buffer = new StringBuffer( "char dummy[] = \"0123456789" ); //$NON-NLS-1$
    	for( int i = 0; i < 5000; ++i )
    		buffer.append( "0123456789"); //$NON-NLS-1$
    	buffer.append( "\";"); //$NON-NLS-1$
    	parse( buffer.toString() );
	}
	
    public void testBug69161() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "#define MACRO(s) s\n "); //$NON-NLS-1$
    	writer.write( "char *testQueries[] =\n"); //$NON-NLS-1$
    	writer.write( "{\n"); //$NON-NLS-1$
    	writer.write( "MACRO(\",\"),\n");  //$NON-NLS-1$
    	writer.write( "MACRO(\"(\"),\n"); //$NON-NLS-1$
    	writer.write( "MACRO(\")\")\n"); //$NON-NLS-1$
    	writer.write( "};\n"); //$NON-NLS-1$
    	IASTVariable t = (IASTVariable) assertSoleDeclaration( writer.toString() );
    	assertTrue( quickParseCallback.problems.isEmpty() );
    	assertEquals( t.getName(), "testQueries"); //$NON-NLS-1$
	}
}