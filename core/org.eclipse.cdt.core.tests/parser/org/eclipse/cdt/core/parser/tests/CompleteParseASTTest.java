/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: -
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFieldReference;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTVariableReference;


/**
 * @author jcamelon
 *
 */
public class CompleteParseASTTest extends CompleteParseBaseTest
{
	/**
     * @param a
     */
    public CompleteParseASTTest(String a)
    {
        super(a);
    }
    
    public void testEmptyCompilationUnit() throws Exception
    {
    	IASTScope compilationUnit = parse( "// no real code ");
    	assertNotNull( compilationUnit );
    	assertFalse( compilationUnit.getDeclarations().hasNext() );
    }
    
    public void testSimpleNamespace() throws Exception
    {
    	Iterator declarations = parse( "namespace A { }").getDeclarations();
    	IASTNamespaceDefinition namespaceDefinition = (IASTNamespaceDefinition)declarations.next(); 
    	assertEquals( namespaceDefinition.getName(), "A" ); 
    	assertFalse( getDeclarations( namespaceDefinition ).hasNext() );
    }

	public void testMultipleNamespaceDefinitions() throws Exception
	{
		Iterator declarations = parse( "namespace A { } namespace A { }").getDeclarations();
		IASTNamespaceDefinition namespaceDefinition = (IASTNamespaceDefinition)declarations.next(); 
		assertEquals( namespaceDefinition.getName(), "A" );
		namespaceDefinition = (IASTNamespaceDefinition)declarations.next(); 
		assertEquals( namespaceDefinition.getName(), "A" ); 
		assertFalse( getDeclarations( namespaceDefinition ).hasNext() );
	}

    public void testNestedNamespaceDefinitions() throws Exception
    {
		Iterator declarations = parse( "namespace A { namespace B { } }").getDeclarations();
		IASTNamespaceDefinition namespaceDefinition = (IASTNamespaceDefinition)declarations.next(); 
		assertEquals( namespaceDefinition.getName(), "A" );
		assertFalse( declarations.hasNext() );
		Iterator subDeclarations = getDeclarations( namespaceDefinition );
		IASTNamespaceDefinition subDeclaration = (IASTNamespaceDefinition)subDeclarations.next();
		assertEquals( subDeclaration.getName(), "B" );
		assertFalse( subDeclarations.hasNext() );
    }
    
    public void testEmptyClassDeclaration() throws Exception
    {
    	Iterator declarations = parse( "class A { };").getDeclarations();
    	IASTAbstractTypeSpecifierDeclaration abs = (IASTAbstractTypeSpecifierDeclaration)declarations.next();
    	IASTClassSpecifier classSpec = (IASTClassSpecifier)abs.getTypeSpecifier();
    	assertEquals( classSpec.getName(), "A");
    	assertFalse( getDeclarations( classSpec ).hasNext() ); 
    	assertFalse( declarations.hasNext() );
    }
    
    public void testSimpleSubclass() throws Exception
    {
    	Iterator declarations = parse( "class A { };  class B : public A { };").getDeclarations();
    	IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTClassSpecifier classB = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		Iterator parentClasses = classB.getBaseClauses();
		IASTBaseSpecifier baseClass = (IASTBaseSpecifier)parentClasses.next();
		assertEquals( classA, baseClass.getParentClassSpecifier() );
		assertEquals( baseClass.getParentClassName(), "A");
		assertEquals( baseClass.getAccess(), ASTAccessVisibility.PUBLIC);
		assertFalse( baseClass.isVirtual() );
    }
    
    public void testNestedSubclass() throws Exception
    {
    	Iterator declarations = parse( "namespace N { class A { }; } class B : protected virtual N::A { };").getDeclarations();
    	IASTNamespaceDefinition namespaceDefinition = (IASTNamespaceDefinition)declarations.next();
    	IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)getDeclarations( namespaceDefinition).next() ).getTypeSpecifier(); 
		IASTClassSpecifier classB = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		Iterator baseClauses = classB.getBaseClauses(); 
		IASTBaseSpecifier baseClass = (IASTBaseSpecifier)baseClauses.next();
		assertEquals( classA, baseClass.getParentClassSpecifier() );
		assertEquals( callback.getReferences().size(), 2 );
    }
    
    public void testSimpleVariable() throws Exception
    {
    	Iterator declarations = parse( "int x;").getDeclarations();
    	IASTVariable v = (IASTVariable)declarations.next();
    	assertEquals( v.getName(), "x");
    	assertEquals( ((IASTSimpleTypeSpecifier)v.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT ); 
    }
    
	public void testSimpleClassReferenceVariable() throws Exception
	{
		Iterator declarations = parse( "class A { }; A x;").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTVariable v = (IASTVariable)declarations.next();
		assertEquals( v.getName(), "x");
		assertEquals( ((IASTSimpleTypeSpecifier)v.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), classA ); 
	}
    
	public void testNestedClassReferenceVariable() throws Exception
	{
		Iterator declarations = parse( "namespace N { class A { }; } N::A x;").getDeclarations();
		IASTNamespaceDefinition namespace = (IASTNamespaceDefinition)declarations.next();
		Iterator iter = getDeclarations( namespace );
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)iter.next()).getTypeSpecifier();
		IASTVariable v = (IASTVariable)declarations.next();
		assertEquals( v.getName(), "x");
		assertEquals( ((IASTSimpleTypeSpecifier)v.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), classA );
		assertEquals( callback.getReferences().size(), 2 ); 
	}
	
	public void testMultipleDeclaratorsVariable() throws Exception
	{
		Iterator declarations = parse( "class A { }; A x, y, z;").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTVariable v = (IASTVariable)declarations.next();
		assertEquals( v.getName(), "x");
		assertEquals( ((IASTSimpleTypeSpecifier)v.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), classA );
		assertEquals( callback.getReferences().size(), 3 ); 
		Iterator i = callback.getReferences().iterator();
		while( i.hasNext() )
			assertEquals( ((IASTReference)i.next()).getReferencedElement(), classA ); 
	}
	
	public void testSimpleField() throws Exception
	{
		Iterator declarations = parse( "class A { double x; };").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		Iterator fields =getDeclarations(classA);
		IASTField f = (IASTField)fields.next(); 
		assertEquals( f.getName(), "x" );
		assertEquals( ((IASTSimpleTypeSpecifier)f.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.DOUBLE ); 
	}
	
	public void testUsingClauses() throws Exception
	{
		Iterator declarations = parse( "namespace A { namespace B { int x;  class C { static int y = 5; }; } } \n using namespace A::B;\n using A::B::x;using A::B::C;using A::B::C::y;").getDeclarations();
		IASTNamespaceDefinition namespaceA = (IASTNamespaceDefinition)declarations.next();
		IASTNamespaceDefinition  namespaceB = (IASTNamespaceDefinition)getDeclarations( namespaceA ).next();
		Iterator i = getDeclarations( namespaceB );
		IASTVariable variableX = (IASTVariable)i.next();
		IASTClassSpecifier classC = ((IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier());
		IASTField fieldY = (IASTField)getDeclarations( classC ).next(); 
		assertQualifiedName( fieldY.getFullyQualifiedName(), new String [] { "A", "B", "C", "y" } );		
		IASTUsingDirective directive = (IASTUsingDirective)declarations.next();
		assertEquals( directive.getNamespaceDefinition(), namespaceB );
		IASTUsingDeclaration declaration = (IASTUsingDeclaration)declarations.next();
		assertEquals( declaration.getUsingType(), variableX );
		declaration = (IASTUsingDeclaration)declarations.next();
		assertEquals( declaration.getUsingType(), classC );
		declaration = (IASTUsingDeclaration)declarations.next();
		assertEquals( declaration.getUsingType(), fieldY );
		assertEquals( callback.getReferences().size(), 12 );
		
	}
	
	public void testEnumerations() throws Exception
	{
		Iterator declarations = parse( "namespace A { enum E { e1, e2, e3 }; E varE;}").getDeclarations();
		IASTNamespaceDefinition namespaceA = (IASTNamespaceDefinition)declarations.next(); 
		Iterator namespaceMembers = getDeclarations( namespaceA ); 
		IASTEnumerationSpecifier enumE = (IASTEnumerationSpecifier)((IASTAbstractTypeSpecifierDeclaration)namespaceMembers.next()).getTypeSpecifier();
		assertEquals( enumE.getName(), "E");
		assertQualifiedName( enumE.getFullyQualifiedName(), new String [] { "A", "E" } );		
		Iterator enumerators = enumE.getEnumerators();
		IASTEnumerator enumerator_e1 = (IASTEnumerator)enumerators.next();
		IASTEnumerator enumerator_e2 = (IASTEnumerator)enumerators.next();
		IASTEnumerator enumerator_e3 = (IASTEnumerator)enumerators.next();
		assertFalse( enumerators.hasNext() );
		assertEquals( enumerator_e1.getName(), "e1");
		assertEquals( enumerator_e2.getName(), "e2");
		assertEquals( enumerator_e3.getName(), "e3");
		IASTVariable varE = (IASTVariable)namespaceMembers.next();
		assertEquals( ((IASTSimpleTypeSpecifier)varE.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), enumE );
	}
	
	public void testSimpleFunction() throws Exception
	{
		Iterator declarations = parse( "void foo( void );").getDeclarations();
		IASTFunction function = (IASTFunction)declarations.next();
		assertEquals( function.getName(), "foo" );
		assertEquals( callback.getReferences().size(), 0 );
	}
	
	public void testSimpleFunctionWithTypes() throws Exception
	{
		Iterator declarations = parse( "class A { public: \n class B { }; }; const A::B &  foo( A * myParam );").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTFunction function = (IASTFunction)declarations.next(); 
		assertEquals( callback.getReferences().size(), 3 ); 
	}
	
	public void testSimpleMethod() throws Exception
	{
		Iterator declarations = parse( "class A { void foo(); };").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTMethod method = (IASTMethod)getDeclarations( classA ).next();
		assertEquals( method.getName(), "foo" );
	}
	
	public void testSimpleMethodWithTypes() throws Exception
	{
		Iterator declarations = parse( "class U { }; class A { U foo( U areDumb ); };").getDeclarations();
		IASTClassSpecifier classU = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTMethod method = (IASTMethod)getDeclarations( classA ).next();
		assertEquals( method.getName(), "foo" );
		assertEquals( callback.getReferences().size(), 2 );
	}
	
	public void testUsingDeclarationWithFunctionsAndMethods() throws Exception
	{
		Iterator declarations = parse( "namespace N { int foo(void); } class A { static int bar(void); }; using N::foo; using ::A::bar;" ).getDeclarations();
		IASTNamespaceDefinition namespaceN = (IASTNamespaceDefinition)declarations.next();
		IASTFunction fooFunction = (IASTFunction)(getDeclarations(namespaceN).next()); 
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTMethod methodM = (IASTMethod)(getDeclarations(classA).next());
		IASTUsingDeclaration using1 = (IASTUsingDeclaration)declarations.next(); 
		IASTUsingDeclaration using2 = (IASTUsingDeclaration)declarations.next();
		assertEquals( callback.getReferences().size(), 4 );
		Iterator references = callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), namespaceN );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), fooFunction );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), classA );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), methodM ); 
	}
	
	public void testLinkageSpec() throws Exception
	{
		IASTLinkageSpecification linkage = (IASTLinkageSpecification)parse( "extern \"C\" { int foo(); }").getDeclarations().next();
		Iterator i = getDeclarations( linkage );
		IASTFunction f = (IASTFunction)i.next();
		assertEquals( f.getName(),"foo");
	}
	

	public void testBogdansExample() throws Exception
	{
		IASTNamespaceDefinition namespaceA = (IASTNamespaceDefinition)parse( "namespace A { namespace B {	enum e1{e_1,e_2};	int x;	class C	{	static int y = 5;	}; }} ").getDeclarations().next();
		IASTNamespaceDefinition namespaceB = (IASTNamespaceDefinition)(getDeclarations(namespaceA).next());
		Iterator subB = getDeclarations( namespaceB );
		IASTEnumerationSpecifier enumE1 = (IASTEnumerationSpecifier)((IASTAbstractTypeSpecifierDeclaration)subB.next()).getTypeSpecifier();
		Iterator enumerators = enumE1.getEnumerators();
		IASTEnumerator enumeratorE_1 = (IASTEnumerator)enumerators.next();
		assertEquals( enumeratorE_1.getOwnerEnumerationSpecifier(), enumE1 );
		IASTVariable variableX = (IASTVariable)subB.next(); 
		IASTClassSpecifier classC = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)subB.next()).getTypeSpecifier();
	}
	
	public void testAndrewsExample() throws Exception
	{
		Iterator declarations = parse( "namespace N{ class A {}; }	using namespace N;	class B: public A{};").getDeclarations();
		IASTNamespaceDefinition namespaceN = (IASTNamespaceDefinition)declarations.next();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)getDeclarations( namespaceN ).next()).getTypeSpecifier(); 
		IASTUsingDirective usingClause = (IASTUsingDirective)declarations.next();
		IASTClassSpecifier classB = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTBaseSpecifier baseSpec = (IASTBaseSpecifier)classB.getBaseClauses().next();
		assertEquals( baseSpec.getParentClassSpecifier(), classA );
		assertEquals( callback.getReferences().size(), 2 );
	}
	
	public void testSimpleTypedef() throws Exception
	{
		Iterator iter = parse( "typedef int myInt;\n myInt var;").getDeclarations();
		IASTTypedefDeclaration typedef = (IASTTypedefDeclaration)iter.next();
		assertEquals( typedef.getName(), "myInt");
		assertEquals( ((IASTSimpleTypeSpecifier)typedef.getAbstractDeclarator().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT );
		IASTVariable v = (IASTVariable)iter.next();
		assertEquals( v.getName(), "var");
		assertEquals( callback.getReferences().size(), 1 ); 
	}
	
	public void testComplexTypedef() throws Exception
	{
		Iterator declarations = parse( "class A{ }; typedef A ** A_DOUBLEPTR;").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTTypedefDeclaration typedef = (IASTTypedefDeclaration)declarations.next();
		assertEquals( ((IASTSimpleTypeSpecifier)typedef.getAbstractDeclarator().getTypeSpecifier()).getTypeSpecifier(), classA ); 
		assertEquals( callback.getReferences().size(), 1 );
	}
	
	
	protected void assertQualifiedName(String [] fromAST, String [] theTruth)
	 {
		 assertNotNull( fromAST );
		 assertNotNull( theTruth );
		 assertEquals( fromAST.length, theTruth.length );
		 for( int i = 0; i < fromAST.length; ++i )
		 {
			 assertEquals( fromAST[i], theTruth[i]);
		 }
	 }

	public void testBug40842() throws Exception{
		Writer code = new StringWriter();
		
		code.write("class A {} a;\n");
		Iterator i = parse(code.toString()).getDeclarations();
		IASTVariable instanceA = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
	}
	
	public void testNestedClassname() throws Exception
	{
		Iterator declarations = parse( "namespace A { } \n class A::B { };").getDeclarations();
		IASTNamespaceDefinition namespaceA = (IASTNamespaceDefinition)declarations.next();
		IASTClassSpecifier classB = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		assertEquals( classB.getOwnerScope(), namespaceA );
		assertEquals( callback.getReferences().size(), 1 );
	}
	
	public void testForwardDeclaration() throws Exception
	{
		Iterator i = parse( "class forward;").getDeclarations();
		assertTrue( i.hasNext() );
		IASTAbstractTypeSpecifierDeclaration d = (IASTAbstractTypeSpecifierDeclaration)i.next(); 
		IASTElaboratedTypeSpecifier elab = (IASTElaboratedTypeSpecifier)d.getTypeSpecifier();
		assertEquals( elab.getName(), "forward");
		assertEquals( elab.getClassKind(), ASTClassKind.CLASS );
	}
	
	public void testElaboratedType() throws Exception
	{
		Iterator i = parse( "class A; class A * a;").getDeclarations();
		IASTElaboratedTypeSpecifier elab = (IASTElaboratedTypeSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		assertEquals( elab.getName(), "A" );
		IASTVariable variableA = (IASTVariable)i.next();
		assertEquals( variableA.getName(), "a");
		assertEquals( variableA.getAbstractDeclaration().getTypeSpecifier(), elab ); 
	}
	
	public void testForewardDeclarationWithUsage() throws Exception
	{
		Iterator declarations = parse( "class A; A * anA;class A { };").getDeclarations();
		IASTAbstractTypeSpecifierDeclaration forewardDecl = (IASTAbstractTypeSpecifierDeclaration)declarations.next(); 
		IASTVariable variable = (IASTVariable)declarations.next();
		IASTAbstractTypeSpecifierDeclaration classDecl = (IASTAbstractTypeSpecifierDeclaration)declarations.next();
		IASTElaboratedTypeSpecifier elab = (IASTElaboratedTypeSpecifier)forewardDecl.getTypeSpecifier();
		IASTClassSpecifier clasSpec = (IASTClassSpecifier)classDecl.getTypeSpecifier();
		assertEquals( elab.getName(), clasSpec.getName() );
		String [] fqnClass = clasSpec.getFullyQualifiedName();
		String [] fqnElab = elab.getFullyQualifiedName();
		assertEquals( fqnClass.length, fqnElab.length );
		for( int i = 0; i < fqnClass.length; ++i )
			assertEquals( fqnClass[i], fqnElab[i]);
		assertEquals( callback.getReferences().size(), 1 );
		assertEquals( callback.getForewardDecls().size(), 1 );
		IASTClassReference ref = (IASTClassReference)callback.getReferences().get(0);
		assertTrue( ref.getReferencedElement() instanceof IASTElaboratedTypeSpecifier );
		assertEquals( ref.getReferencedElement(), elab );
	}
		
	
	public void testASM() throws Exception
	{
		IASTASMDefinition asm = (IASTASMDefinition)parse( "asm ( \"blah blah blah\" );" ).getDeclarations().next();
		assertEquals( asm.getBody(), "blah blah blah");  
	}

	public void testOverride() throws Exception
	{
		Iterator i = parse( "void foo();\n void foo( int );\n").getDeclarations();
		IASTFunction f1 = (IASTFunction)i.next();
		IASTFunction f2 = (IASTFunction)i.next();
		assertFalse( i.hasNext() );
	}	 
	
	public void testSimpleExpression() throws Exception
	{
		Iterator i = parse( "int x; int y = x;").getDeclarations();
		IASTVariable varX = (IASTVariable)i.next();
		IASTVariable varY = (IASTVariable)i.next();
		assertEquals( callback.getReferences().size(), 1 );
	}
	
	public void testParameterExpressions() throws Exception
	{
		Iterator i = parse( "int x = 5; void foo( int sub = x ) { }").getDeclarations();
		IASTVariable varX = (IASTVariable)i.next();
		IASTFunction funFoo = (IASTFunction)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 1 );	
	}
	
	public void testNestedNamespaceExpression() throws Exception
	{
		Iterator i = parse( "namespace A { int x = 666; } int y  = A::x;").getDeclarations();
		IASTNamespaceDefinition namespaceA = (IASTNamespaceDefinition)i.next(); 
		IASTVariable variableY = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );		
	}
	
	public void testConstructorChain() throws Exception
	{
		Iterator i = parse( "int x = 5;\n class A \n{ public : \n int a; \n A() : a( x ) { } };").getDeclarations(); 
		IASTVariable variableX = (IASTVariable)i.next(); 
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		assertFalse( i.hasNext() );
		Iterator s = getDeclarations( classA ); 
		IASTField fieldA = (IASTField)s.next(); 
		IASTMethod methodA = (IASTMethod)s.next(); 
		assertFalse( s.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		IASTFieldReference reference1 = (IASTFieldReference)callback.getReferences().get(0);
		IASTVariableReference reference2 = (IASTVariableReference)callback.getReferences().get(1);
		assertEquals( reference1.getReferencedElement(), fieldA );
		assertEquals( reference2.getReferencedElement(), variableX ); 
	}
	
	public void testArrayModExpression() throws Exception
	{
		Iterator i = parse( "const int x = 5; int y [ x ]; ").getDeclarations();
		IASTVariable varX = (IASTVariable)i.next();
		IASTVariable varY = (IASTVariable)i.next(); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 1 );
	}


	public void testPointerVariable() throws Exception
	{
		Iterator i = parse( "class A { }; A * anA;").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable varAnA = (IASTVariable)i.next();
		assertFalse( i.hasNext() ); 
		assertEquals( callback.getReferences().size(), 1 ); 
		IASTClassReference ref = (IASTClassReference)callback.getReferences().get(0);
		assertEquals( ref.getReferencedElement(), classA );
	}	
	
	public void testExceptionSpecification() throws Exception
	{
		Iterator i = parse( "class A { }; void foo( void ) throw ( A );").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTFunction function = (IASTFunction)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 1 );
		IASTClassReference ref = (IASTClassReference)callback.getReferences().get(0);
		assertEquals( ref.getReferencedElement(), classA );		
	}
	 
	public void testNewExpressions() throws Exception
	{
		Iterator declarations = parse( "int A; int B; int C; int D; int P; int*p = new  (P) (A)[B][C][D];" ).getDeclarations();
		IASTVariable variableA = (IASTVariable)declarations.next();
		IASTVariable variableB = (IASTVariable)declarations.next();
		IASTVariable variableC = (IASTVariable)declarations.next();
		IASTVariable variableD = (IASTVariable)declarations.next();
		IASTVariable variableP = (IASTVariable)declarations.next();
		IASTVariable variablep = (IASTVariable)declarations.next();
		assertEquals( callback.getReferences().size(), 5 );
		Iterator references = callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), variableP );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), variableB );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), variableC ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), variableD ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), variableA );
	}

	public void testBug41520() throws Exception 
	{
		Iterator i = parse( "const int x = 666, y( x );").getDeclarations();
		IASTVariable variableX = (IASTVariable)i.next();
		IASTVariable variableY = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
	}
	
	public void testNewXReferences() throws Exception
	{
		Iterator declarations = parse( "const int max = 5;\n int * x = new int[max];").getDeclarations();
		IASTVariable max = (IASTVariable) declarations.next();
		IASTVariable x = (IASTVariable) declarations.next(); 
		assertFalse( declarations.hasNext() );
		assertEquals( callback.getReferences().size(), 1 );
		IASTVariableReference maxRef = (IASTVariableReference) callback.getReferences().get(0);
		assertEquals( maxRef.getReferencedElement(), max );
	}
	
	public void testQualifiedNameReferences() throws Exception
	{
		Iterator i = parse( "class A{ class B{ class C { public: int cMethod(); }; }; }; \n  int A::B::C::cMethod() {}; \n" ).getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		Iterator j = getDeclarations(classA);
		IASTClassSpecifier classB = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)j.next()).getTypeSpecifier();
		Iterator k = getDeclarations(classB);
		IASTClassSpecifier classC = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)k.next()).getTypeSpecifier();
		
		// Note : this used to be considered a function, not a method
		IASTMethod method = (IASTMethod)i.next(); 
		
		assertEquals( callback.getReferences().size(), 3 );
		Iterator references = callback.getReferences().iterator();
		assertEquals( ((IASTClassReference)references.next()).getReferencedElement(), classA );
		assertEquals( ((IASTClassReference)references.next()).getReferencedElement(), classB );
		assertEquals( ((IASTClassReference)references.next()).getReferencedElement(), classC );
	}

	public void testIsConstructor() throws Exception
	{
		Iterator i = parse( "class A{ public: A(); }; \n  A::A() {}; \n" ).getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTMethod method = (IASTMethod)i.next();
		assertTrue (method.isConstructor()); 
	}

	public void testIsDestructor() throws Exception
	{
		Iterator i = parse( "class A{ public: A(); }; \n  A::~A() {}; \n" ).getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTMethod method = (IASTMethod)i.next();
		assertTrue (method.isDestructor()); 
	}
	
	public void testBug41445() throws Exception
	{
		Iterator i = parse( "class A { }; namespace N { class B : public A { struct A {}; }; }").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTNamespaceDefinition namespaceN = (IASTNamespaceDefinition)i.next(); 
		Iterator sub = getDeclarations( namespaceN );
		IASTClassSpecifier classB = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)sub.next()).getTypeSpecifier();
		IASTClassSpecifier structA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)( getDeclarations( classB ).next())).getTypeSpecifier();
	}
}
