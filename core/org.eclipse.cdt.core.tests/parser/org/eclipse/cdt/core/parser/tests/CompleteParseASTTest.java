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

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFieldReference;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTVariableReference;
import org.eclipse.cdt.core.parser.ast.IASTNode.ILookupResult;
import org.eclipse.cdt.internal.core.parser.ParserException;


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
		assertEquals( callback.getReferences().size(), 0 );
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
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), variableA );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), variableP );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), variableB ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), variableC ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), variableD );
	}

	public void testBug41520() throws Exception 
	{
		Iterator i = parse( "const int x = 666; const int y( x );").getDeclarations();
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
		try { // This is to prove that there are no exceptions
			// Used to cause AST Semantic exception
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
		}catch (Exception e){
			fail();
		}
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
		Iterator i = parse( "class A{ public: ~A(); }; \n  A::~A() {}; \n" ).getDeclarations();
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
	
	public void testSimpleFunctionBody() throws Exception
	{
		Iterator i = parse( "class A { int f1(); }; const int x = 4; int f() { return x; } int A::f1() { return x; }").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTMethod method_prototype = (IASTMethod)getDeclarations(classA).next();
		IASTVariable x = (IASTVariable) i.next();
		IASTFunction function_f = (IASTFunction) i.next();
		IASTMethod method_f = (IASTMethod)i.next();
		assertEquals( method_f.getName(), method_prototype.getName() );
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 3 );
		IASTVariableReference referenceX = (IASTVariableReference) callback.getReferences().get(0);
		assertEquals( referenceX.getReferencedElement(), x );
		IASTClassReference referenceA = (IASTClassReference) callback.getReferences().get(1);
		assertEquals( referenceA.getReferencedElement(), classA );
		referenceX = (IASTVariableReference) callback.getReferences().get(2);
		assertEquals( referenceX.getReferencedElement(), x );
	}


	public void testSimpleForLoop() throws Exception
	{
		Iterator i = parse( "const int FIVE = 5;  void f() {  int x = 0; for( int i = 0; i < FIVE; ++i ) { x += i; }  }").getDeclarations();
		IASTVariable five = (IASTVariable) i.next();
		IASTFunction f = (IASTFunction) i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 5 );
		i = parse( "const int FIVE = 5;  void f() {  int x = 0; for( int i = 0; i < FIVE; ++i )  x += i;  }").getDeclarations();
		five = (IASTVariable) i.next();
		f = (IASTFunction) i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 5 );
		
		i = parse( "class A { }; void f() {  for( int i = 0; i < (A*)0; ++i ) { A anA; } }").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		f = (IASTFunction)i.next(); 
		assertFalse( i.hasNext() ); 
		assertEquals( callback.getReferences().size(), 4 );
	}

	public void testBug42541() throws Exception
	{
		Iterator i = parse( "union{ int v; char a; } id;" ).getDeclarations();
		IASTVariable id = (IASTVariable)i.next();
		
		IASTClassSpecifier union = (IASTClassSpecifier) id.getAbstractDeclaration().getTypeSpecifier();
		Iterator sub = getDeclarations( union );
		IASTField intV  = (IASTField)sub.next();
		IASTField charA = (IASTField)sub.next();
	}
	
	
	
	public void testSimpleIfStatement() throws Exception
	{
		Iterator i = parse( "const bool T = true; int foo() { if( T ) { return 5; } else if( ! T ) return 20; else { return 10; } }").getDeclarations();
		IASTVariable t = (IASTVariable)i.next(); 
		IASTFunction foo = (IASTFunction)i.next(); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
	}
	
	public void testSimpleWhileStatement() throws Exception
	{
		Iterator i = parse( "const bool T = true; void foo() { int x = 0; while( T ) {  ++x;  if( x == 100 ) break; } }").getDeclarations();
		IASTVariable t = (IASTVariable)i.next(); 
		IASTFunction foo = (IASTFunction)i.next(); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 3 );
	}
	
	public void testSimpleSwitchStatement() throws Exception
	{
		Iterator i = parse( "const int x = 5; const int y = 10; void foo() { switch( x ) { case 1: break; case 2: goto blah; case y: continue; default: break;} }").getDeclarations();
		IASTVariable x = (IASTVariable)i.next();
		IASTVariable y = (IASTVariable)i.next(); 
		IASTFunction foo = (IASTFunction)i.next(); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2  );
	}
	
	public void testSimpleDoStatement() throws Exception
	{
		Iterator i = parse( "const int x = 3; int counter = 0; void foo() { do { ++counter; } while( counter != x ); } ").getDeclarations();
		IASTVariable x = (IASTVariable)i.next(); 
		IASTVariable counter = (IASTVariable)i.next(); 
		IASTFunction foo = (IASTFunction)i.next(); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 3 );
	}
	
	public void testThrowStatement() throws Exception
	{
		Iterator i = parse( "class A { }; void foo() throw ( A ) { throw A; throw; } ").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTFunction functionF = (IASTFunction)i.next(); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		for( int j = 0; j < 2; ++j )
			assertEquals( ((IASTReference)callback.getReferences().get(j) ).getReferencedElement(), classA );
	}
	
	public void testScoping() throws Exception
	{
		Iterator i = parse( "void foo() { int x = 3; if( x == 1 ) { int x = 4; } else int x = 2; }").getDeclarations(); 
		IASTFunction f = (IASTFunction)i.next(); 
		Iterator subDeclarations = getDeclarations(f);
		IASTVariable topX = (IASTVariable)subDeclarations.next();
		assertEquals( topX.getInitializerClause().getAssigmentExpression().getLiteralString(), "3");
		assertEquals( topX.getName(), "x");
		assertFalse( subDeclarations.hasNext() );
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 1 );
		assertEquals( ((IASTReference)callback.getReferences().get(0)).getReferencedElement(), topX ); 
		
		Iterator level1 = getNestedScopes( f );
		IASTCodeScope codeScope = (IASTCodeScope)level1.next();
		Iterator subSubDeclarations = getDeclarations(codeScope);
		IASTVariable secondX = (IASTVariable)subSubDeclarations.next();
		assertEquals( secondX.getInitializerClause().getAssigmentExpression().getLiteralString(), "4");
		codeScope = (IASTCodeScope)level1.next();
		assertFalse( level1.hasNext() );
		subSubDeclarations = getDeclarations(codeScope);
		IASTVariable thirdX = (IASTVariable)subSubDeclarations.next();
		assertEquals( thirdX.getInitializerClause().getAssigmentExpression().getLiteralString(), "2");
		
	}
	
	public void testEnumeratorReferences() throws Exception
	{
		Iterator i = parse( "enum E { e1, e2, e3 }; E anE = e1;").getDeclarations();
		IASTEnumerationSpecifier enumE = (IASTEnumerationSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable anE = (IASTVariable)i.next();
		IASTEnumerator e1 = (IASTEnumerator)enumE.getEnumerators().next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		assertEquals( ((IASTReference)callback.getReferences().get(0)).getReferencedElement(), enumE );
		assertEquals( ((IASTReference)callback.getReferences().get(1)).getReferencedElement(),  e1 );
	}
	
	public void testBug42840() throws Exception
	{
		Iterator i = parse( "void foo(); void foo() { } class SearchMe { };").getDeclarations();
		IASTFunction fooDeclaration = (IASTFunction)i.next(); 
		IASTFunction fooDefinition = (IASTFunction)i.next(); 
		IASTClassSpecifier classSpec = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		assertFalse( i.hasNext() );
		assertTrue( callback.getReferences().isEmpty());
		
		i = parse( "class A { void f ( A );	};	void A::f( A ){ return; }" ).getDeclarations();
		classSpec = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTMethod fooMethodDefinition = (IASTMethod)i.next(); 
		assertFalse( i.hasNext() ); 
		Iterator subIterator = getDeclarations( classSpec );
		IASTMethod fooMethodDeclaration = (IASTMethod)subIterator.next(); 
		assertFalse( subIterator.hasNext());
		Iterator references = callback.getReferences().iterator();
		assertEquals( callback.getReferences().size(), 3 );
		for( int j = 0; j < 3; ++j)
			assertEquals( ((IASTReference)callback.getReferences().get( j )).getReferencedElement(), classSpec ); 
		
	}
	
	public void testBug42872() throws Exception
	{
		Iterator i = parse( "struct B {}; struct D : B {}; void foo(D* dp) { B* bp = dynamic_cast<B*>(dp); }" ).getDeclarations(); 
		IASTClassSpecifier structB = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTClassSpecifier structD = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTFunction foo = (IASTFunction)i.next(); 
		IASTVariable bp = (IASTVariable)getDeclarations(foo).next(); 
		assertFalse( i.hasNext() );
	}
	
	public void testBug43503A() throws Exception {
		Iterator i = parse("class SD_01 { void f_SD_01() {}}; int main(){ SD_01 * a = new SD_01(); a->f_SD_01();	} ").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		Iterator j = getDeclarations(classA);
		IASTMethod f = (IASTMethod)j.next();
		assertFalse(j.hasNext());
		IASTFunction main = (IASTFunction) i.next();
		assertFalse(i.hasNext());
		Iterator k = getDeclarations(main);
		assertTrue(k.hasNext()); 
	}	
	
	
	public void testBug42979() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "class OperatorOverload{\n" );
		code.write( "public:\n" );
		code.write( "  bool operator==( const class OperatorOverload& that )\n" );
		code.write( "  { return true; }\n" );
		code.write( "  bool operator!=( const class OperatorOverload& that );\n" );
		code.write( "}; \n" );
  
		code.write( "bool OperatorOverload::operator!=( const class OperatorOverload& that )\n" );
		code.write( "{ return false; }\n" );

		Iterator i = parse( code.toString() ).getDeclarations();
		IASTClassSpecifier classOp = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		Iterator subDeclarations = getDeclarations(classOp);
		IASTMethod operatorEqualsDeclaration = (IASTMethod)subDeclarations.next();
		IASTMethod operatorNotEqualsDeclaration = (IASTMethod)subDeclarations.next();
		IASTMethod operatorNotEqualDefinition = (IASTMethod)i.next();
		assertEquals( operatorNotEqualDefinition.getName(), operatorNotEqualsDeclaration.getName() );
		assertFalse( i.hasNext());
		assertEquals( callback.getReferences().size(), 4 );
		for( int j =0; j < 4; ++j )
			assertFalse( classOp.getNameOffset() == ((IASTReference)callback.getReferences().get(j)).getOffset() ); 
	}
	/** 
	 * class A { static int x; } int A::x = 5;
	 */
	public void testBug43373() throws Exception
	{
		try { // This is to prove that there are no exceptions
			// Used to cause AST Semantic exception
			Iterator i = parse( "class A { static int x; }; int A::x = 5;" ).getDeclarations();
			IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
			Iterator j = getDeclarations(classA);
			IASTField field1 = (IASTField) j.next();			
			// Note : this used to be considered a variable, not a field
			IASTField field2 = (IASTField)i.next(); 
			
			assertEquals( callback.getReferences().size(), 1 );
			Iterator references = callback.getReferences().iterator();
			assertEquals( ((IASTReference)references.next()).getReferencedElement(), classA );
			assertTrue (field1.getVisiblity() == field2.getVisiblity());
		}catch (Exception e){
			fail();
		}
	}
	
	public void testBug39504() throws Exception
	{
		Iterator i = parse( "const int w = 2; int x[ 5 ]; int y = sizeof (x[w]);" ).getDeclarations();
		IASTVariable varW = (IASTVariable)i.next(); 
		IASTVariable varX = (IASTVariable)i.next(); 
		IASTVariable vary = (IASTVariable)i.next(); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
	}
	
	public void testBug43375() throws Exception
	{
		IASTVariable varX = (IASTVariable)parse( "extern int x;").getDeclarations().next();
		assertTrue( varX.isExtern() ); 	
	}

	public void testBug43503() throws Exception
	{
		StringBuffer buff = new StringBuffer(); 
		
		buff.append( "class SD_02 {");
		buff.append( "	public:");
		buff.append( " void f_SD_02();");
		buff.append( " };");
		buff.append( "class SD_01 {\n"); 
		buff.append( "	public:\n");
		buff.append( "		SD_02 *next;");      // REFERENCE SD_02
		buff.append( "		void f_SD_01();\n");
		buff.append( "};\n");
		buff.append( "int main(){\n");
		buff.append( "	SD_01 a = new SD_01();\n");  // REFERENCE SD_01 * 2
		buff.append( "	a->f_SD_01();\n");			// REFERENCE a && REFERENCE f_SD_01
		buff.append( "}\n");
		buff.append( "void SD_01::f_SD_01()\n");	// REFERENCE SD_01
		buff.append( "{\n");
		buff.append( "   next->f_SD_02();\n");		// REFERENCE next && reference f_SD_02
		buff.append( "}\n");
		Iterator i = parse( buff.toString() ).getDeclarations();
		IASTClassSpecifier SD_02 = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTMethod f_SD_02 = (IASTMethod)getDeclarations( SD_02 ).next();
		IASTClassSpecifier SD_01 = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTField next= (IASTField)getDeclarations( SD_01 ).next();
		IASTFunction main = (IASTFunction)i.next();
		IASTVariable a = (IASTVariable)getDeclarations(main).next();
		IASTMethod f_SD_01 = (IASTMethod)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 8, createTaskList( new Task( SD_02), new Task( SD_01, 3 ), new Task( a ), new Task( f_SD_01 ), new Task( f_SD_02 ), new Task( next ) ));
	}
		
	public void testBug43679_A () throws Exception
	{
		try{ // this used to throw a null pointer exception 
			Iterator i = parse( "struct Sample { int size() const; }; extern const Sample * getSample(); int trouble() {  return getSample()->size(); } ", false ).getDeclarations();
			IASTClassSpecifier A = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
			Iterator j = getDeclarations(A);
			IASTMethod s = (IASTMethod) j.next();
			assertFalse (j.hasNext());
			IASTFunction g = (IASTFunction) i.next();
			IASTFunction t = (IASTFunction) i.next();
			assertFalse (i.hasNext());
			Iterator ref = callback.getReferences().iterator();
			assertAllReferences( 3, createTaskList( new Task(A) , new Task( s ) , new Task (g) ));
	
		} catch(Exception e){
			fail();
		}
	}
	public void testBug43679_B () throws Exception
	{
		try{ // this used to throw a class cast exception 
		Iterator i = parse( "struct Sample{int size() const; }; struct Sample; ", false ).getDeclarations();
		IASTClassSpecifier A = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		Iterator j = getDeclarations(A);
		IASTMethod s = (IASTMethod) j.next();
		assertFalse (j.hasNext());
		IASTAbstractTypeSpecifierDeclaration forwardDecl = (IASTAbstractTypeSpecifierDeclaration)i.next();
		assertFalse (i.hasNext());
		Iterator ref = callback.getReferences().iterator();
		assertFalse (ref.hasNext());
				
		} catch(Exception e){
			fail();
		}
	}
	
	public void testBug43951() throws Exception
	{
		Iterator i = parse( "class B{ B(); ~B(); }; B::B(){} B::~B(){}", false ).getDeclarations();
	
		IASTClassSpecifier b = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		assertEquals( b.getName(), "B");
		IASTMethod constructor = (IASTMethod) i.next();
		assertEquals( constructor.getName(), "B" );
		assertTrue( constructor.previouslyDeclared() );
	}	

	public void testBug44342() throws Exception {
		try{
			IASTScope scope = parse("class A { void f(){} void f(int){} }; int main(){ A * a = new A(); a->f();} ");
			Iterator i = scope.getDeclarations();
			IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
			Iterator j = getDeclarations(classA);
			IASTMethod f = (IASTMethod)j.next();
			IASTMethod f2 = (IASTMethod)j.next();
			assertFalse(j.hasNext());
			IASTFunction main = (IASTFunction) i.next();
			assertFalse(i.hasNext());
			Iterator k = getDeclarations(main);
			assertTrue(k.hasNext());
			IASTVariable a = (IASTVariable)k.next(); 
			Iterator ref = callback.getReferences().iterator();
			assertAllReferences( 4, createTaskList( new Task(classA , 2) , new Task( a ) , new Task (f) ));
			
		}catch (ParserException e){
			// parsing fails for now
			fail();
		}
	}	

	
	public void testCDesignatedInitializers() throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append( "struct Inner { int a,b,c; };"); 
		buffer.append( "struct A { int x; int y[]; struct Inner innerArray[]; int z []; };");
		buffer.append( "struct A myA = { .x = 4, .y[3] = 4, .y[4] = 3, .innerArray[0].a = 3, .innerArray[1].b = 5, .innerArray[2].c=6, .z = { 1,4,5} };");
		Iterator i = parse( buffer.toString(), true, ParserLanguage.C ).getDeclarations();
		IASTClassSpecifier Inner  = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		Iterator members = getDeclarations(Inner);
		IASTField a = (IASTField)members.next();
		IASTField b = (IASTField)members.next();
		IASTField c = (IASTField)members.next();
		assertFalse( members.hasNext());
		IASTClassSpecifier A = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		members = getDeclarations( A );
		IASTField x = (IASTField)members.next();
		IASTField y = (IASTField)members.next(); 
		IASTField innerArray = (IASTField)members.next();
		IASTField z = (IASTField)members.next();
		assertFalse( members.hasNext() );
		IASTVariable myA = (IASTVariable)i.next(); 
		assertFalse( i.hasNext() );
		assertAllReferences( 12, createTaskList( new Task( A ), 
												new Task( x ), 
												new Task( y, 2 ), 
												new Task( Inner ), 
												new Task( innerArray, 3), 
												new Task( a ), 
												new Task( b ), 
												new Task( c ), 
												new Task( z ) ) );
	}
	
	public void testBug39551A() throws Exception
	{
		IASTFunction function = (IASTFunction)parse("extern float _Complex conjf (float _Complex);", true, ParserLanguage.C).getDeclarations().next();
		assertEquals( function.getName(), "conjf");
		assertTrue( ((IASTSimpleTypeSpecifier)function.getReturnType().getTypeSpecifier()).isComplex() );
	}

	public void testBug39551B() throws Exception
	{
		IASTVariable variable = (IASTVariable)parse("_Imaginary double id = 99.99 * __I__;", true, ParserLanguage.C).getDeclarations().next();
		assertEquals( variable.getName(), "id");
		assertTrue( ((IASTSimpleTypeSpecifier)variable.getAbstractDeclaration().getTypeSpecifier()).isImaginary() );
	}
	
	public void testCBool() throws Exception
	{
		IASTVariable variable = (IASTVariable)parse( "_Bool x;", true, ParserLanguage.C ).getDeclarations().next();
		assertEquals( ((IASTSimpleTypeSpecifier)variable.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type._BOOL );
	}
	
	public void testBug44510() throws Exception
	{
		Iterator i = parse( "int initialize(); " +
							"int initialize( char ){} " +
							"int initialize(){ return 1; } " +
							"void main(){ int i = initialize(); }" ).getDeclarations();
		
		IASTFunction function1 = (IASTFunction) i.next();
		assertEquals( function1.previouslyDeclared(), false );
		
		IASTFunction function2 = (IASTFunction) i.next();
		assertEquals( function2.previouslyDeclared(), false );
				
		IASTFunction function3 = (IASTFunction) i.next();
		assertEquals( function3.previouslyDeclared(), true );
		
		IASTFunction main = (IASTFunction) i.next();
		assertFalse( i.hasNext() );
		
		assertAllReferences( 1, createTaskList( new Task( function3 ) ) );
	}	
	
	public void testBug44925() throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append( "class MyClass { };"); 
		buffer.append( "class MyClass myObj1;");
		buffer.append( "enum MyEnum { Item1 };");
		buffer.append( "enum MyEnum myObj2;");
		Iterator i = parse( buffer.toString() ).getDeclarations();
		
		IASTClassSpecifier MyClass  = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();		
		IASTVariable myObj1 = (IASTVariable) i.next();
		IASTEnumerationSpecifier MyEnum = (IASTEnumerationSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable myObj2 = (IASTVariable) i.next();
		
		assertFalse( i.hasNext() );
		
		assertAllReferences( 2, createTaskList( new Task( MyClass ), new Task( MyEnum ) ) ); 	
	}
	
	public void testBug44838() throws Exception
	{
		StringBuffer buffer = new StringBuffer(); 
		buffer.append( "class A { int myX; A( int x ); };\n");
		buffer.append( "A::A( int x ) : myX( x ) { if( x == 5 ) myX++; }\n");
		Iterator i = parse( buffer.toString() ).getDeclarations(); 
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTField myX = (IASTField)getDeclarations( classA ).next(); 
		IASTMethod constructor = (IASTMethod)i.next();
		IASTParameterDeclaration parmX = (IASTParameterDeclaration)constructor.getParameters().next();
		assertTrue( constructor.isConstructor());
		assertFalse(i.hasNext());
	}
	
	public void testBug46165() throws Exception
	{
		StringBuffer buffer = new StringBuffer(); 
		buffer.append( "class A { int myX; A( int x ); };\n");
		buffer.append( "A::A( int x ) : myX( x ) { if( x == 5 ) myX++; }\n");
		Iterator i = parse( buffer.toString() ).getDeclarations(); 
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTField myX = (IASTField)getDeclarations( classA ).next(); 
		IASTMethod constructor = (IASTMethod)i.next();
		IASTParameterDeclaration parmX = (IASTParameterDeclaration)constructor.getParameters().next();
		assertTrue( constructor.isConstructor());
		assertAllReferences( 4, createTaskList( new Task( classA ), new Task( myX, 2 ), new Task( parmX )));
		assertFalse(i.hasNext());
	}

	public void testBug47624() throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append( "struct s { }; \n" );
		buffer.append( "void f ( int s ) { \n" );
		buffer.append( "   struct s sInstance; \n" );
		buffer.append( "}\n");
		
		Iterator i = parse( buffer.toString() ).getDeclarations();
		IASTClassSpecifier structS = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTFunction function = (IASTFunction) i.next();
		Iterator fnIter = getDeclarations( function );
		IASTVariable sInstance = (IASTVariable) fnIter.next();
		IASTElaboratedTypeSpecifier elaborated = (IASTElaboratedTypeSpecifier) sInstance.getAbstractDeclaration().getTypeSpecifier();
		assertFalse( fnIter.hasNext() );
		
		assertAllReferences( 1, createTaskList( new Task( structS ) ) );
		assertFalse( i.hasNext() );
	}
	
	public void testQualifiedLookup() throws Exception{
		//this is meant to test that on a->f, the lookup for f is qualified
		//the namespace is necessary because of bug 47926
		StringBuffer buffer = new StringBuffer();
		buffer.append( "namespace N {" );
		buffer.append( "   void f () {} \n" );
		buffer.append( "   class A { }; \n" );
		buffer.append( "}" );
		buffer.append( "void main() { N::A * a = new N::A();  a->f(); } ");
		
		Iterator i = parse( buffer.toString() ).getDeclarations();
		
		IASTNamespaceDefinition namespace = (IASTNamespaceDefinition) i.next();
		Iterator nsIter = getDeclarations( namespace );
		
		IASTFunction f = (IASTFunction) nsIter.next();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)nsIter.next()).getTypeSpecifier();
		
		assertFalse( nsIter.hasNext() );
		
		IASTFunction main = (IASTFunction) i.next();
		
		Iterator fnIter = getDeclarations( main );
		IASTVariable a = (IASTVariable) fnIter.next();
		
		assertAllReferences( 5, createTaskList( new Task( namespace, 2 ), new Task( classA, 2 ), new Task( a ) ) );
	}
	
	public void testBug43110() throws Exception
	{
		StringBuffer buffer = new StringBuffer(); 
		buffer.append("void x( int y, ... );\n");
		buffer.append("void y( int x... );\n");
		buffer.append("void z(...);");
		Iterator i = parse(buffer.toString() ).getDeclarations();
		while( i.hasNext() )
			assertTrue( ((IASTFunction)i.next()).takesVarArgs() );
	}
	
	public void testBug43110_XRef() throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append( "void foo( ... ) {}\n" );
		buffer.append( "void main( ){ foo( 1 ); }\n" );
		
		Iterator i = parse( buffer.toString() ).getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		assertTrue( foo.takesVarArgs() );
		assertAllReferences( 1, createTaskList( new Task( foo ) ) );

		buffer = new StringBuffer();
		buffer.append( "void foo( ... )   {}\n" );
		buffer.append( "void foo( int x ) {}\n" );
		buffer.append( "void main( ){ foo( 1 ); }\n" );
		
		i = parse( buffer.toString() ).getDeclarations();
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next();
		assertTrue( foo1.takesVarArgs() );
		assertFalse( foo2.takesVarArgs() );
		assertAllReferences( 1, createTaskList( new Task( foo2 ) ) );
		
		buffer = new StringBuffer();
		buffer.append( "void foo( ... )      {}\n" );
		buffer.append( "void foo( int x = 1) {}\n" );
		buffer.append( "void main( ){ foo(); }\n" );
		
		i = parse( buffer.toString() ).getDeclarations();
		foo1 = (IASTFunction)i.next();
		foo2 = (IASTFunction)i.next();
		assertTrue( foo1.takesVarArgs() );
		assertFalse( foo2.takesVarArgs() );
		assertAllReferences( 1, createTaskList( new Task( foo2 ) ) );
		
		buffer = new StringBuffer();
		buffer.append( "void foo( int x ... ) {}\n" );
		buffer.append( "void main( ){ foo( 1, 2, 'a' ); }\n" );
		
		i = parse( buffer.toString() ).getDeclarations();
		foo = (IASTFunction)i.next();
		assertTrue( foo.takesVarArgs() );
		assertAllReferences( 1, createTaskList( new Task( foo ) ) );
	}
	
	public void testBug48307_FriendFunction_1() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write( "class A{ public : void foo(); }; " );
		writer.write( "class B{ ");
		writer.write( "   private : int aPrivate;" );
		writer.write( "   friend void A::foo(); ");
		writer.write( "};" );
		writer.write( "void A::foo(){}" );
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTClassSpecifier classB = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTMethod method = (IASTMethod) i.next();
		
		ILookupResult result = method.lookup( "a", new IASTNode.LookupKind[] { IASTNode.LookupKind.ALL }, classB );

		assertEquals( result.getResultsSize(), 1 );
		IASTField field = (IASTField) result.getNodes().next();
		assertEquals( field.getName(), "aPrivate" );
	}

	public void testBug48307_FriendFunction_2() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write( "void global();" );
		writer.write( "class B{ ");
		writer.write( "   private : int aPrivate;" );
		writer.write( "   friend void global(); ");
		writer.write( "};" );
		writer.write( "void global(){}" );
				
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTFunction functionDecl  = (IASTFunction) i.next();
		IASTClassSpecifier classB = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTFunction functionDef = (IASTFunction) i.next();
		
		ILookupResult result = functionDef.lookup( "a", new IASTNode.LookupKind[] { IASTNode.LookupKind.ALL }, classB );

		assertEquals( result.getResultsSize(), 1 );
		IASTField field = (IASTField) result.getNodes().next();
		assertEquals( field.getName(), "aPrivate" );
	}
}
