/**********************************************************************
 * Copyright (c) 2002,2003,2004 Rational Software Corporation and others.
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

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTVariableReference;
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
    	IASTScope compilationUnit = parse( "// no real code "); //$NON-NLS-1$
    	assertNotNull( compilationUnit );
    	assertFalse( compilationUnit.getDeclarations().hasNext() );
    }
    
    public void testSimpleNamespace() throws Exception
    {
    	Iterator declarations = parse( "namespace A { }").getDeclarations(); //$NON-NLS-1$
    	IASTNamespaceDefinition namespaceDefinition = (IASTNamespaceDefinition)declarations.next(); 
    	assertEquals( namespaceDefinition.getName(), "A" );  //$NON-NLS-1$
    	assertFalse( getDeclarations( namespaceDefinition ).hasNext() );
    }

	public void testMultipleNamespaceDefinitions() throws Exception
	{
		Iterator declarations = parse( "namespace A { } namespace A { }").getDeclarations(); //$NON-NLS-1$
		IASTNamespaceDefinition namespaceDefinition = (IASTNamespaceDefinition)declarations.next(); 
		assertEquals( namespaceDefinition.getName(), "A" ); //$NON-NLS-1$
		namespaceDefinition = (IASTNamespaceDefinition)declarations.next(); 
		assertEquals( namespaceDefinition.getName(), "A" );  //$NON-NLS-1$
		assertFalse( getDeclarations( namespaceDefinition ).hasNext() );
	}

    public void testNestedNamespaceDefinitions() throws Exception
    {
		Iterator declarations = parse( "namespace A { namespace B { } }").getDeclarations(); //$NON-NLS-1$
		IASTNamespaceDefinition namespaceDefinition = (IASTNamespaceDefinition)declarations.next(); 
		assertEquals( namespaceDefinition.getName(), "A" ); //$NON-NLS-1$
		assertFalse( declarations.hasNext() );
		Iterator subDeclarations = getDeclarations( namespaceDefinition );
		IASTNamespaceDefinition subDeclaration = (IASTNamespaceDefinition)subDeclarations.next();
		assertEquals( subDeclaration.getName(), "B" ); //$NON-NLS-1$
		assertFalse( subDeclarations.hasNext() );
    }
    
    public void testEmptyClassDeclaration() throws Exception
    {
    	Iterator declarations = parse( "class A { };").getDeclarations(); //$NON-NLS-1$
    	IASTAbstractTypeSpecifierDeclaration abs = (IASTAbstractTypeSpecifierDeclaration)declarations.next();
    	IASTClassSpecifier classSpec = (IASTClassSpecifier)abs.getTypeSpecifier();
    	assertEquals( classSpec.getName(), "A"); //$NON-NLS-1$
    	assertFalse( getDeclarations( classSpec ).hasNext() ); 
    	assertFalse( declarations.hasNext() );
    }
    
    public void testSimpleSubclass() throws Exception
    {
    	Iterator declarations = parse( "class A { };  class B : public A { };").getDeclarations(); //$NON-NLS-1$
    	IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTClassSpecifier classB = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		Iterator parentClasses = classB.getBaseClauses();
		IASTBaseSpecifier baseClass = (IASTBaseSpecifier)parentClasses.next();
		assertEquals( classA, baseClass.getParentClassSpecifier() );
		assertEquals( baseClass.getParentClassName(), "A"); //$NON-NLS-1$
		assertEquals( baseClass.getAccess(), ASTAccessVisibility.PUBLIC);
		assertFalse( baseClass.isVirtual() );
    }
    
    public void testNestedSubclass() throws Exception
    {
    	Iterator declarations = parse( "namespace N { class A { }; } class B : protected virtual N::A { };").getDeclarations(); //$NON-NLS-1$
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
    	Iterator declarations = parse( "int x;").getDeclarations(); //$NON-NLS-1$
    	IASTVariable v = (IASTVariable)declarations.next();
    	assertEquals( v.getName(), "x"); //$NON-NLS-1$
    	assertEquals( ((IASTSimpleTypeSpecifier)v.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT ); 
    }
    
	public void testSimpleClassReferenceVariable() throws Exception
	{
		Iterator declarations = parse( "class A { }; A x;").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTVariable v = (IASTVariable)declarations.next();
		assertEquals( v.getName(), "x"); //$NON-NLS-1$
		assertEquals( ((IASTSimpleTypeSpecifier)v.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), classA );
		assertAllReferences( 1, createTaskList( new Task( classA )));
	}
    
	public void testNestedClassReferenceVariable() throws Exception
	{
		Iterator declarations = parse( "namespace N { class A { }; } N::A x;").getDeclarations(); //$NON-NLS-1$
		IASTNamespaceDefinition namespace = (IASTNamespaceDefinition)declarations.next();
		Iterator iter = getDeclarations( namespace );
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)iter.next()).getTypeSpecifier();
		IASTVariable v = (IASTVariable)declarations.next();
		assertEquals( v.getName(), "x"); //$NON-NLS-1$
		assertEquals( ((IASTSimpleTypeSpecifier)v.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), classA );
		assertEquals( callback.getReferences().size(), 2 ); 
	}
	
	public void testMultipleDeclaratorsVariable() throws Exception
	{
		Iterator declarations = parse( "class A { }; A x, y, z;").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTVariable v = (IASTVariable)declarations.next();
		assertEquals( v.getName(), "x"); //$NON-NLS-1$
		assertEquals( ((IASTSimpleTypeSpecifier)v.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), classA );
		assertEquals( callback.getReferences().size(), 3 ); 
		Iterator i = callback.getReferences().iterator();
		while( i.hasNext() )
			assertEquals( ((IASTReference)i.next()).getReferencedElement(), classA ); 
	}
	
	public void testSimpleField() throws Exception
	{
		Iterator declarations = parse( "class A { double x; };").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		Iterator fields =getDeclarations(classA);
		IASTField f = (IASTField)fields.next(); 
		assertEquals( f.getName(), "x" ); //$NON-NLS-1$
		assertEquals( ((IASTSimpleTypeSpecifier)f.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.DOUBLE ); 
	}
	
	public void testUsingClauses() throws Exception
	{
		Iterator declarations = parse( "namespace A { namespace B { int x;  class C { static int y = 5; }; } } \n using namespace A::B;\n using A::B::x;using A::B::C;using A::B::C::y;").getDeclarations(); //$NON-NLS-1$
		IASTNamespaceDefinition namespaceA = (IASTNamespaceDefinition)declarations.next();
		IASTNamespaceDefinition  namespaceB = (IASTNamespaceDefinition)getDeclarations( namespaceA ).next();
		Iterator i = getDeclarations( namespaceB );
		IASTVariable variableX = (IASTVariable)i.next();
		IASTClassSpecifier classC = ((IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier());
		IASTField fieldY = (IASTField)getDeclarations( classC ).next(); 
		assertQualifiedName( fieldY.getFullyQualifiedName(), new String [] { "A", "B", "C", "y" } );		 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IASTUsingDirective directive = (IASTUsingDirective)declarations.next();
		assertEquals( directive.getNamespaceDefinition(), namespaceB );
		assertEquals( directive.getNamespaceName(), "A::B" ); //$NON-NLS-1$
		IASTUsingDeclaration declaration = (IASTUsingDeclaration)declarations.next();
		assertEquals( declaration.getUsingTypes().next(), variableX );
		declaration = (IASTUsingDeclaration)declarations.next();
		assertEquals( declaration.getUsingTypes().next(), classC );
		declaration = (IASTUsingDeclaration)declarations.next();
		assertEquals( declaration.getUsingTypes().next(), fieldY );
		assertEquals( callback.getReferences().size(), 12 );
		
	}
	
	public void testEnumerations() throws Exception
	{
		Iterator declarations = parse( "namespace A { enum E { e1, e2, e3 }; E varE;}").getDeclarations(); //$NON-NLS-1$
		IASTNamespaceDefinition namespaceA = (IASTNamespaceDefinition)declarations.next(); 
		Iterator namespaceMembers = getDeclarations( namespaceA ); 
		IASTEnumerationSpecifier enumE = (IASTEnumerationSpecifier)((IASTAbstractTypeSpecifierDeclaration)namespaceMembers.next()).getTypeSpecifier();
		assertEquals( enumE.getName(), "E"); //$NON-NLS-1$
		assertQualifiedName( enumE.getFullyQualifiedName(), new String [] { "A", "E" } );		 //$NON-NLS-1$ //$NON-NLS-2$
		Iterator enumerators = enumE.getEnumerators();
		IASTEnumerator enumerator_e1 = (IASTEnumerator)enumerators.next();
		IASTEnumerator enumerator_e2 = (IASTEnumerator)enumerators.next();
		IASTEnumerator enumerator_e3 = (IASTEnumerator)enumerators.next();
		assertFalse( enumerators.hasNext() );
		assertEquals( enumerator_e1.getName(), "e1"); //$NON-NLS-1$
		assertEquals( enumerator_e2.getName(), "e2"); //$NON-NLS-1$
		assertEquals( enumerator_e3.getName(), "e3"); //$NON-NLS-1$
		IASTVariable varE = (IASTVariable)namespaceMembers.next();
		assertEquals( ((IASTSimpleTypeSpecifier)varE.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), enumE );
	}
	
	public void testSimpleFunction() throws Exception
	{
		Iterator declarations = parse( "void foo( void );").getDeclarations(); //$NON-NLS-1$
		IASTFunction function = (IASTFunction)declarations.next();
		assertEquals( function.getName(), "foo" ); //$NON-NLS-1$
		assertEquals( callback.getReferences().size(), 0 );
	}
	
	public void testSimpleFunctionWithTypes() throws Exception
	{
		Iterator declarations = parse( "class A { public: \n class B { }; }; const A::B &  foo( A * myParam );").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		Iterator classDecls = getDeclarations(classA);
		IASTClassSpecifier classB = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)classDecls.next()).getTypeSpecifier();
		IASTFunction function = (IASTFunction)declarations.next(); 
		assertAllReferences( 3, createTaskList( new Task( classA ,2), new Task(classB)));
	}
	
	public void testSimpleMethod() throws Exception
	{
		Iterator declarations = parse( "class A { void foo(); };").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTMethod method = (IASTMethod)getDeclarations( classA ).next();
		assertEquals( method.getName(), "foo" ); //$NON-NLS-1$
	}
	
	public void testSimpleMethodWithTypes() throws Exception
	{
		Iterator declarations = parse( "class U { }; class A { U foo( U areDumb ); };").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier classU = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTMethod method = (IASTMethod)getDeclarations( classA ).next();
		assertEquals( method.getName(), "foo" ); //$NON-NLS-1$
		assertEquals( callback.getReferences().size(), 2 );
	}
	
	public void testUsingDeclarationWithFunctionsAndMethods() throws Exception
	{
		Iterator declarations = parse( "namespace N { int foo(void); } class A { static int bar(void); }; using N::foo; using ::A::bar;" ).getDeclarations(); //$NON-NLS-1$
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
		IASTLinkageSpecification linkage = (IASTLinkageSpecification)parse( "extern \"C\" { int foo(); }").getDeclarations().next(); //$NON-NLS-1$
		Iterator i = getDeclarations( linkage );
		IASTFunction f = (IASTFunction)i.next();
		assertEquals( f.getName(),"foo"); //$NON-NLS-1$
	}
	

	public void testBogdansExample() throws Exception
	{
		IASTNamespaceDefinition namespaceA = (IASTNamespaceDefinition)parse( "namespace A { namespace B {	enum e1{e_1,e_2};	int x;	class C	{	static int y = 5;	}; }} ").getDeclarations().next(); //$NON-NLS-1$
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
		Iterator declarations = parse( "namespace N{ class A {}; }	using namespace N;	class B: public A{};").getDeclarations(); //$NON-NLS-1$
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
		Iterator iter = parse( "typedef int myInt;\n myInt var;").getDeclarations(); //$NON-NLS-1$
		IASTTypedefDeclaration typedef = (IASTTypedefDeclaration)iter.next();
		assertEquals( typedef.getName(), "myInt"); //$NON-NLS-1$
		assertEquals( ((IASTSimpleTypeSpecifier)typedef.getAbstractDeclarator().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT );
		IASTVariable v = (IASTVariable)iter.next();
		assertEquals( v.getName(), "var"); //$NON-NLS-1$
		assertEquals( callback.getReferences().size(), 1 ); 
	}
	
	public void testComplexTypedef() throws Exception
	{
		Iterator declarations = parse( "class A{ }; typedef A ** A_DOUBLEPTR;").getDeclarations(); //$NON-NLS-1$
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
		
		code.write("class A {} a;\n"); //$NON-NLS-1$
		Iterator i = parse(code.toString()).getDeclarations();
		IASTVariable instanceA = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 0 );
	}
	
	public void testNestedClassname() throws Exception
	{
		Iterator declarations = parse( "namespace A { } \n class A::B { };").getDeclarations(); //$NON-NLS-1$
		IASTNamespaceDefinition namespaceA = (IASTNamespaceDefinition)declarations.next();
		IASTClassSpecifier classB = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		assertEquals( classB.getOwnerScope(), namespaceA );
		assertEquals( callback.getReferences().size(), 1 );
	}
	
	public void testForwardDeclaration() throws Exception
	{
		Iterator i = parse( "class forward;").getDeclarations(); //$NON-NLS-1$
		assertTrue( i.hasNext() );
		IASTAbstractTypeSpecifierDeclaration d = (IASTAbstractTypeSpecifierDeclaration)i.next(); 
		IASTElaboratedTypeSpecifier elab = (IASTElaboratedTypeSpecifier)d.getTypeSpecifier();
		assertEquals( elab.getName(), "forward"); //$NON-NLS-1$
		assertEquals( elab.getClassKind(), ASTClassKind.CLASS );
	}
	
	public void testElaboratedType() throws Exception
	{
		Iterator i = parse( "class A; class A * a;").getDeclarations(); //$NON-NLS-1$
		IASTElaboratedTypeSpecifier elab = (IASTElaboratedTypeSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		assertEquals( elab.getName(), "A" ); //$NON-NLS-1$
		IASTVariable variableA = (IASTVariable)i.next();
		assertEquals( variableA.getName(), "a"); //$NON-NLS-1$
		assertEquals( variableA.getAbstractDeclaration().getTypeSpecifier(), elab ); 
	}
	
	public void testForewardDeclarationWithUsage() throws Exception
	{
		Iterator declarations = parse( "class A; A * anA;class A { };").getDeclarations(); //$NON-NLS-1$
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
		IASTASMDefinition asm = (IASTASMDefinition)parse( "asm ( \"blah blah blah\" );" ).getDeclarations().next(); //$NON-NLS-1$
		assertEquals( asm.getBody(), "blah blah blah");   //$NON-NLS-1$
	}

	public void testOverride() throws Exception
	{
		Iterator i = parse( "void foo();\n void foo( int );\n").getDeclarations(); //$NON-NLS-1$
		IASTFunction f1 = (IASTFunction)i.next();
		IASTFunction f2 = (IASTFunction)i.next();
		assertFalse( i.hasNext() );
	}	 
	
	public void testSimpleExpression() throws Exception
	{
		Iterator i = parse( "int x; int y = x;").getDeclarations(); //$NON-NLS-1$
		IASTVariable varX = (IASTVariable)i.next();
		IASTVariable varY = (IASTVariable)i.next();
		assertEquals( callback.getReferences().size(), 1 );
	}
	
	public void testParameterExpressions() throws Exception
	{
		Iterator i = parse( "int x = 5; void foo( int sub = x ) { }").getDeclarations(); //$NON-NLS-1$
		IASTVariable varX = (IASTVariable)i.next();
		IASTFunction funFoo = (IASTFunction)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 1 );	
	}
	
	public void testNestedNamespaceExpression() throws Exception
	{
		Iterator i = parse( "namespace A { int x = 666; } int y  = A::x;").getDeclarations(); //$NON-NLS-1$
		IASTNamespaceDefinition namespaceA = (IASTNamespaceDefinition)i.next(); 
		IASTVariable variableY = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );		
	}
	
	public void testConstructorChain() throws Exception
	{
		Iterator i = parse( "int x = 5;\n class A \n{ public : \n int a; \n A() : a( x ) { } };").getDeclarations();  //$NON-NLS-1$
		IASTVariable variableX = (IASTVariable)i.next(); 
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		assertFalse( i.hasNext() );
		Iterator s = getDeclarations( classA ); 
		IASTField fieldA = (IASTField)s.next(); 
		IASTMethod methodA = (IASTMethod)s.next(); 
		assertFalse( s.hasNext() );
		assertAllReferences( 2, createTaskList( new Task( fieldA), new Task( variableX )));
	}
	
	public void testArrayModExpression() throws Exception
	{
		Iterator i = parse( "const int x = 5; int y [ x ]; ").getDeclarations(); //$NON-NLS-1$
		IASTVariable varX = (IASTVariable)i.next();
		IASTVariable varY = (IASTVariable)i.next(); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 1 );
	}


	public void testPointerVariable() throws Exception
	{
		Iterator i = parse( "class A { }; A * anA;").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable varAnA = (IASTVariable)i.next();
		assertFalse( i.hasNext() ); 
		assertEquals( callback.getReferences().size(), 1 ); 
		IASTClassReference ref = (IASTClassReference)callback.getReferences().get(0);
		assertEquals( ref.getReferencedElement(), classA );
	}	
	
	public void testExceptionSpecification() throws Exception
	{
		Iterator i = parse( "class A { }; void foo( void ) throw ( A );").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTFunction function = (IASTFunction)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 1 );
		IASTClassReference ref = (IASTClassReference)callback.getReferences().get(0);
		assertEquals( ref.getReferencedElement(), classA );		
	}
	 
	public void testNewExpressions() throws Exception
	{
		Iterator declarations = parse( "int A; int B; int C; int D; int P; int*p = new  (P) (A)[B][C][D];" ).getDeclarations(); //$NON-NLS-1$
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
		Iterator i = parse( "const int x = 666; const int y( x );").getDeclarations(); //$NON-NLS-1$
		IASTVariable variableX = (IASTVariable)i.next();
		IASTVariable variableY = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
	}
	
	public void testNewXReferences() throws Exception
	{
		Iterator declarations = parse( "const int max = 5;\n int * x = new int[max];").getDeclarations(); //$NON-NLS-1$
		IASTVariable max = (IASTVariable) declarations.next();
		IASTVariable x = (IASTVariable) declarations.next(); 
		assertFalse( declarations.hasNext() );
		assertEquals( callback.getReferences().size(), 1 );
		IASTVariableReference maxRef = (IASTVariableReference) callback.getReferences().get(0);
		assertEquals( maxRef.getReferencedElement(), max );
	}
	
	public void testQualifiedNameReferences() throws Exception
	{
			// Used to cause AST Semantic exception
			Iterator i = parse( "class A{ class B{ class C { public: int cMethod(); }; }; }; \n  int A::B::C::cMethod() {}; \n" ).getDeclarations(); //$NON-NLS-1$
			IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
			Iterator j = getDeclarations(classA);
			IASTClassSpecifier classB = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)j.next()).getTypeSpecifier();
			Iterator k = getDeclarations(classB);
			IASTClassSpecifier classC = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)k.next()).getTypeSpecifier();			
			// Note : this used to be considered a function, not a method
			IASTMethod method = (IASTMethod)i.next(); 
			
			assertAllReferences( 3, createTaskList( new Task( classA ), new Task( classB ), new Task( classC )));
		
	}

	public void testIsConstructor() throws Exception
	{
		Iterator i = parse( "class A{ public: A(); }; \n  A::A() {}; \n" ).getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTMethod method = (IASTMethod)i.next();
		assertTrue (method.isConstructor()); 
	}

	public void testIsDestructor() throws Exception
	{
		Iterator i = parse( "class A{ public: ~A(); }; \n  A::~A() {}; \n" ).getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTMethod method = (IASTMethod)i.next();
		assertTrue (method.isDestructor()); 
	}
	
	public void testBug41445() throws Exception
	{
		Iterator i = parse( "class A { }; namespace N { class B : public A { struct A {}; }; }").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier classA = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTNamespaceDefinition namespaceN = (IASTNamespaceDefinition)i.next(); 
		Iterator sub = getDeclarations( namespaceN );
		IASTClassSpecifier classB = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)sub.next()).getTypeSpecifier();
		IASTClassSpecifier structA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)( getDeclarations( classB ).next())).getTypeSpecifier();
	}
	
	public void testSimpleFunctionBody() throws Exception
	{
		Iterator i = parse( "class A { int f1(); }; const int x = 4; int f() { return x; } int A::f1() { return x; }").getDeclarations(); //$NON-NLS-1$
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
		Iterator i = parse( "const int FIVE = 5;  void f() {  int x = 0; for( int i = 0; i < FIVE; ++i ) { x += i; }  }").getDeclarations(); //$NON-NLS-1$
		IASTVariable five = (IASTVariable) i.next();
		IASTFunction f = (IASTFunction) i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 5 );
		i = parse( "const int FIVE = 5;  void f() {  int x = 0; for( int i = 0; i < FIVE; ++i )  x += i;  }").getDeclarations(); //$NON-NLS-1$
		five = (IASTVariable) i.next();
		f = (IASTFunction) i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 5 );
		
		i = parse( "class A { }; void f() {  for( int i = 0; i < (A*)0; ++i ) { A anA; } }").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		f = (IASTFunction)i.next(); 
		assertFalse( i.hasNext() ); 
		assertEquals( callback.getReferences().size(), 4 );
	}

	public void testBug42541() throws Exception
	{
		Iterator i = parse( "union{ int v; char a; } id;" ).getDeclarations(); //$NON-NLS-1$
		IASTVariable id = (IASTVariable)i.next();
		
		IASTClassSpecifier union = (IASTClassSpecifier) id.getAbstractDeclaration().getTypeSpecifier();
		Iterator sub = getDeclarations( union );
		IASTField intV  = (IASTField)sub.next();
		IASTField charA = (IASTField)sub.next();
	}
	
	
	
	public void testSimpleIfStatement() throws Exception
	{
		Iterator i = parse( "const bool T = true; int foo() { if( T ) { return 5; } else if( ! T ) return 20; else { return 10; } }").getDeclarations(); //$NON-NLS-1$
		IASTVariable t = (IASTVariable)i.next(); 
		IASTFunction foo = (IASTFunction)i.next(); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
	}
	
	public void testSimpleWhileStatement() throws Exception
	{
		Iterator i = parse( "const bool T = true; void foo() { int x = 0; while( T ) {  ++x;  if( x == 100 ) break; } }").getDeclarations(); //$NON-NLS-1$
		IASTVariable t = (IASTVariable)i.next(); 
		IASTFunction foo = (IASTFunction)i.next(); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 3 );
	}
	
	public void testSimpleSwitchStatement() throws Exception
	{
		Iterator i = parse( "const int x = 5; const int y = 10; void foo() { switch( x ) { case 1: break; case 2: goto blah; case y: continue; default: break;} }").getDeclarations(); //$NON-NLS-1$
		IASTVariable x = (IASTVariable)i.next();
		IASTVariable y = (IASTVariable)i.next(); 
		IASTFunction foo = (IASTFunction)i.next(); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2  );
	}
	
	public void testSimpleDoStatement() throws Exception
	{
		Iterator i = parse( "const int x = 3; int counter = 0; void foo() { do { ++counter; } while( counter != x ); } ").getDeclarations(); //$NON-NLS-1$
		IASTVariable x = (IASTVariable)i.next(); 
		IASTVariable counter = (IASTVariable)i.next(); 
		IASTFunction foo = (IASTFunction)i.next(); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 3 );
	}
	
	public void testThrowStatement() throws Exception
	{
		Iterator i = parse( "class A { }; void foo() throw ( A ) { throw A; throw; } ").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTFunction functionF = (IASTFunction)i.next(); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		for( int j = 0; j < 2; ++j )
			assertEquals( ((IASTReference)callback.getReferences().get(j) ).getReferencedElement(), classA );
	}
	
	public void testScoping() throws Exception
	{
		Iterator i = parse( "void foo() { int x = 3; if( x == 1 ) { int x = 4; } else int x = 2; }").getDeclarations();  //$NON-NLS-1$
		IASTFunction f = (IASTFunction)i.next(); 
		Iterator subDeclarations = getDeclarations(f);
		IASTVariable topX = (IASTVariable)subDeclarations.next();
		assertEquals( topX.getInitializerClause().getAssigmentExpression().getLiteralString(), "3"); //$NON-NLS-1$
		assertEquals( topX.getName(), "x"); //$NON-NLS-1$
		assertFalse( subDeclarations.hasNext() );
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 1 );
		assertEquals( ((IASTReference)callback.getReferences().get(0)).getReferencedElement(), topX ); 
		
		Iterator level1 = getNestedScopes( f );
		IASTCodeScope codeScope = (IASTCodeScope)level1.next();
		Iterator subSubDeclarations = getDeclarations(codeScope);
		IASTVariable secondX = (IASTVariable)subSubDeclarations.next();
		assertEquals( secondX.getInitializerClause().getAssigmentExpression().getLiteralString(), "4"); //$NON-NLS-1$
		codeScope = (IASTCodeScope)level1.next();
		assertFalse( level1.hasNext() );
		subSubDeclarations = getDeclarations(codeScope);
		IASTVariable thirdX = (IASTVariable)subSubDeclarations.next();
		assertEquals( thirdX.getInitializerClause().getAssigmentExpression().getLiteralString(), "2"); //$NON-NLS-1$
		
	}
	
	public void testEnumeratorReferences() throws Exception
	{
		Iterator i = parse( "enum E { e1, e2, e3 }; E anE = e1;").getDeclarations(); //$NON-NLS-1$
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
		Iterator i = parse( "void foo(); void foo() { } class SearchMe { };").getDeclarations(); //$NON-NLS-1$
		IASTFunction fooDeclaration = (IASTFunction)i.next(); 
		IASTFunction fooDefinition = (IASTFunction)i.next(); 
		IASTClassSpecifier classSpec = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		assertFalse( i.hasNext() );
		assertTrue( callback.getReferences().isEmpty());
		
		i = parse( "class A { void f ( A );	};	void A::f( A ){ return; }" ).getDeclarations(); //$NON-NLS-1$
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
		Iterator i = parse( "struct B {}; struct D : B {}; void foo(D* dp) { B* bp = dynamic_cast<B*>(dp); }" ).getDeclarations();  //$NON-NLS-1$
		IASTClassSpecifier structB = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTClassSpecifier structD = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTFunction foo = (IASTFunction)i.next(); 
		IASTVariable bp = (IASTVariable)getDeclarations(foo).next(); 
		assertFalse( i.hasNext() );
	}
	
	public void testBug43503A() throws Exception {
		Iterator i = parse("class SD_01 { void f_SD_01() {}}; int main(){ SD_01 * a = new SD_01(); a->f_SD_01();	} ").getDeclarations(); //$NON-NLS-1$
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
		code.write( "class OperatorOverload{\n" ); //$NON-NLS-1$
		code.write( "public:\n" ); //$NON-NLS-1$
		code.write( "  bool operator==( const class OperatorOverload& that )\n" ); //$NON-NLS-1$
		code.write( "  { return true; }\n" ); //$NON-NLS-1$
		code.write( "  bool operator!=( const class OperatorOverload& that );\n" ); //$NON-NLS-1$
		code.write( "}; \n" ); //$NON-NLS-1$
  
		code.write( "bool OperatorOverload::operator!=( const class OperatorOverload& that )\n" ); //$NON-NLS-1$
		code.write( "{ return false; }\n" ); //$NON-NLS-1$

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
		Iterator i = parse( "class A { static int x; }; int A::x = 5;" ).getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		Iterator j = getDeclarations(classA);
		IASTField field1 = (IASTField) j.next();			
			// Note : this used to be considered a variable, not a field
		IASTField field2 = (IASTField)i.next(); 
		assertTrue (field1.getVisiblity() == field2.getVisiblity());
		assertAllReferences( 1, createTaskList( new Task( classA )));			
	}
	
	public void testBug39504() throws Exception
	{
		Iterator i = parse( "const int w = 2; int x[ 5 ]; int y = sizeof (x[w]);" ).getDeclarations(); //$NON-NLS-1$
		IASTVariable varW = (IASTVariable)i.next(); 
		IASTVariable varX = (IASTVariable)i.next(); 
		IASTVariable vary = (IASTVariable)i.next(); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
	}
	
	public void testBug43375() throws Exception
	{
		IASTVariable varX = (IASTVariable)parse( "extern int x;").getDeclarations().next(); //$NON-NLS-1$
		assertTrue( varX.isExtern() ); 	
	}

	public void testBug43503() throws Exception
	{
		StringBuffer buff = new StringBuffer(); 
		
		buff.append( "class SD_02 {"); //$NON-NLS-1$
		buff.append( "	public:"); //$NON-NLS-1$
		buff.append( " void f_SD_02();"); //$NON-NLS-1$
		buff.append( " };"); //$NON-NLS-1$
		buff.append( "class SD_01 {\n");  //$NON-NLS-1$
		buff.append( "	public:\n"); //$NON-NLS-1$
		buff.append( "		SD_02 *next;");      // REFERENCE SD_02 //$NON-NLS-1$
		buff.append( "		void f_SD_01();\n"); //$NON-NLS-1$
		buff.append( "};\n"); //$NON-NLS-1$
		buff.append( "int main(){\n"); //$NON-NLS-1$
		buff.append( "	SD_01 a = new SD_01();\n");  // REFERENCE SD_01 * 2 //$NON-NLS-1$
		buff.append( "	a->f_SD_01();\n");			// REFERENCE a && REFERENCE f_SD_01 //$NON-NLS-1$
		buff.append( "}\n"); //$NON-NLS-1$
		buff.append( "void SD_01::f_SD_01()\n");	// REFERENCE SD_01 //$NON-NLS-1$
		buff.append( "{\n"); //$NON-NLS-1$
		buff.append( "   next->f_SD_02();\n");		// REFERENCE next && reference f_SD_02 //$NON-NLS-1$
		buff.append( "}\n"); //$NON-NLS-1$
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
			Iterator i = parse( "struct Sample { int size() const; }; extern const Sample * getSample(); int trouble() {  return getSample()->size(); } ", false ).getDeclarations(); //$NON-NLS-1$
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
		Iterator i = parse( "struct Sample{int size() const; }; struct Sample; ", false ).getDeclarations(); //$NON-NLS-1$
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
		Iterator i = parse( "class B{ B(); ~B(); }; B::B(){} B::~B(){}", false ).getDeclarations(); //$NON-NLS-1$
	
		IASTClassSpecifier b = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		assertEquals( b.getName(), "B"); //$NON-NLS-1$
		IASTMethod constructor = (IASTMethod) i.next();
		assertEquals( constructor.getName(), "B" ); //$NON-NLS-1$
		assertTrue( constructor.previouslyDeclared() );
	}	

	public void testBug44342() throws Exception {
		try{
			IASTScope scope = parse("class A { void f(){} void f(int){} }; int main(){ A * a = new A(); a->f();} "); //$NON-NLS-1$
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
		buffer.append( "struct Inner { int a,b,c; };");  //$NON-NLS-1$
		buffer.append( "struct A { int x; int y[]; struct Inner innerArray[]; int z []; };"); //$NON-NLS-1$
		buffer.append( "struct A myA = { .x = 4, .y[3] = 4, .y[4] = 3, .innerArray[0].a = 3, .innerArray[1].b = 5, .innerArray[2].c=6, .z = { 1,4,5} };"); //$NON-NLS-1$
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
		IASTFunction function = (IASTFunction)parse("extern float _Complex conjf (float _Complex);", true, ParserLanguage.C).getDeclarations().next(); //$NON-NLS-1$
		assertEquals( function.getName(), "conjf"); //$NON-NLS-1$
		assertTrue( ((IASTSimpleTypeSpecifier)function.getReturnType().getTypeSpecifier()).isComplex() );
	}

	public void testBug39551B() throws Exception
	{
	    //this used to be 99.99 * __I__, but I don't know where the __I__ came from, its not in C99, nor in GCC
		IASTVariable variable = (IASTVariable)parse("_Imaginary double id = 99.99 * 1i;", true, ParserLanguage.C).getDeclarations().next(); //$NON-NLS-1$
		assertEquals( variable.getName(), "id"); //$NON-NLS-1$
		assertTrue( ((IASTSimpleTypeSpecifier)variable.getAbstractDeclaration().getTypeSpecifier()).isImaginary() );
	}
	
	public void testCBool() throws Exception
	{
		IASTVariable variable = (IASTVariable)parse( "_Bool x;", true, ParserLanguage.C ).getDeclarations().next(); //$NON-NLS-1$
		assertEquals( ((IASTSimpleTypeSpecifier)variable.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type._BOOL );
	}
	
	public void testCBoolAsParameter() throws Exception
	{
		Iterator i = parse( "void f( _Bool b ) {} " + //$NON-NLS-1$
							"_Bool g( _Bool b ) {} " + //$NON-NLS-1$
							"void main(){" + //$NON-NLS-1$
							"   _Bool b;  " + //$NON-NLS-1$
							"   f(b);" + //$NON-NLS-1$
							"	f( g( (_Bool) 1 )  );" + //$NON-NLS-1$
							"}",  //$NON-NLS-1$
							true, ParserLanguage.C ).getDeclarations();
		
		IASTFunction f = (IASTFunction) i.next();
		IASTFunction g = (IASTFunction) i.next();
		IASTFunction main = (IASTFunction) i.next();
		IASTVariable b = (IASTVariable) getDeclarations( main ).next();
		
		assertAllReferences( 4, createTaskList( new Task( f, 2 ), new Task( b ), new Task( g ) ) );
	}
	
	public void testBug44510() throws Exception
	{
		Iterator i = parse( "int initialize(); " + //$NON-NLS-1$
							"int initialize( char ){} " + //$NON-NLS-1$
							"int initialize(){ return 1; } " + //$NON-NLS-1$
							"void main(){ int i = initialize(); }" ).getDeclarations(); //$NON-NLS-1$
		
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
		buffer.append( "class MyClass { };");  //$NON-NLS-1$
		buffer.append( "class MyClass myObj1;"); //$NON-NLS-1$
		buffer.append( "enum MyEnum { Item1 };"); //$NON-NLS-1$
		buffer.append( "enum MyEnum myObj2;"); //$NON-NLS-1$
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
		buffer.append( "class A { int myX; A( int x ); };\n"); //$NON-NLS-1$
		buffer.append( "A::A( int x ) : myX( x ) { if( x == 5 ) myX++; }\n"); //$NON-NLS-1$
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
		buffer.append( "class A { int myX; A( int x ); };\n"); //$NON-NLS-1$
		buffer.append( "A::A( int x ) : myX( x ) { if( x == 5 ) myX++; }\n"); //$NON-NLS-1$
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
		buffer.append( "struct s { }; \n" ); //$NON-NLS-1$
		buffer.append( "void f ( int s ) { \n" ); //$NON-NLS-1$
		buffer.append( "   struct s sInstance; \n" ); //$NON-NLS-1$
		buffer.append( "}\n"); //$NON-NLS-1$
		
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
		buffer.append( "namespace N {" ); //$NON-NLS-1$
		buffer.append( "   void f () {} \n" ); //$NON-NLS-1$
		buffer.append( "   class A { }; \n" ); //$NON-NLS-1$
		buffer.append( "}" ); //$NON-NLS-1$
		buffer.append( "void main() { N::A * a = new N::A();  a->f(); } "); //$NON-NLS-1$
		
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
		buffer.append("void x( int y, ... );\n"); //$NON-NLS-1$
		buffer.append("void y( int x... );\n"); //$NON-NLS-1$
		buffer.append("void z(...);"); //$NON-NLS-1$
		Iterator i = parse(buffer.toString() ).getDeclarations();
		while( i.hasNext() )
			assertTrue( ((IASTFunction)i.next()).takesVarArgs() );
	}
	
	public void testBug43110_XRef() throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append( "void foo( ... ) {}\n" ); //$NON-NLS-1$
		buffer.append( "void main( ){ foo( 1 ); }\n" ); //$NON-NLS-1$
		
		Iterator i = parse( buffer.toString() ).getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		assertTrue( foo.takesVarArgs() );
		assertAllReferences( 1, createTaskList( new Task( foo ) ) );

		buffer = new StringBuffer();
		buffer.append( "void foo( ... )   {}\n" ); //$NON-NLS-1$
		buffer.append( "void foo( int x ) {}\n" ); //$NON-NLS-1$
		buffer.append( "void main( ){ foo( 1 ); }\n" ); //$NON-NLS-1$
		
		i = parse( buffer.toString() ).getDeclarations();
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next();
		assertTrue( foo1.takesVarArgs() );
		assertFalse( foo2.takesVarArgs() );
		assertAllReferences( 1, createTaskList( new Task( foo2 ) ) );
		
		buffer = new StringBuffer();
		buffer.append( "void foo( ... )      {}\n" ); //$NON-NLS-1$
		buffer.append( "void foo( int x = 1) {}\n" ); //$NON-NLS-1$
		buffer.append( "void main( ){ foo(); }\n" ); //$NON-NLS-1$
		
		i = parse( buffer.toString() ).getDeclarations();
		foo1 = (IASTFunction)i.next();
		foo2 = (IASTFunction)i.next();
		assertTrue( foo1.takesVarArgs() );
		assertFalse( foo2.takesVarArgs() );
		assertAllReferences( 1, createTaskList( new Task( foo2 ) ) );
		
		buffer = new StringBuffer();
		buffer.append( "void foo( int x ... ) {}\n" ); //$NON-NLS-1$
		buffer.append( "void main( ){ foo( 1, 2, 'a' ); }\n" ); //$NON-NLS-1$
		
		i = parse( buffer.toString() ).getDeclarations();
		foo = (IASTFunction)i.next();
		assertTrue( foo.takesVarArgs() );
		assertAllReferences( 1, createTaskList( new Task( foo ) ) );
	}
	
	public void testErrorHandling_1() throws Exception
	{
		Iterator i = parse( "A anA; int x = c; class A {}; A * anotherA = &anA; int b;", false ).getDeclarations(); //$NON-NLS-1$
		IASTVariable x = (IASTVariable)i.next();
		assertEquals( x.getName(), "x"); //$NON-NLS-1$
		IASTClassSpecifier A = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		assertEquals( A.getName(), "A"); //$NON-NLS-1$
		IASTVariable anotherA = (IASTVariable)i.next();
		assertEquals( anotherA.getName(), "anotherA"); //$NON-NLS-1$
		IASTVariable b = (IASTVariable)i.next();
		assertEquals( b.getName(), "b"); //$NON-NLS-1$
		assertFalse(i.hasNext()); // should be true
	}
	
	public void testBug44340() throws Exception {
		// inline function with reference to variables declared after them
		IASTScope scope = parse ("class A{ int getX() {return x[1];} int x[10];};", false ); //$NON-NLS-1$
		Iterator i = scope.getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		Iterator j = getDeclarations(classA);
		IASTMethod g = (IASTMethod)j.next();
		IASTField x = (IASTField)j.next();
		assertFalse(j.hasNext());
		assertAllReferences( 1, createTaskList( new Task( x )));		
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
	
	public void testBug47636() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "void f( char [] ); \n" ); //$NON-NLS-1$
		writer.write( "void f( char * ){} \n" ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTFunction fDec = (IASTFunction) i.next();
		assertEquals( fDec.getName(), "f"); //$NON-NLS-1$
			
		
		IASTFunction fDef = (IASTFunction) i.next();
		assertEquals( fDef.getName(), "f"); //$NON-NLS-1$
		
		assertTrue( fDef.previouslyDeclared() );
		
		assertFalse( i.hasNext() );
	}
	
	public void testBug45697() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( " int f( bool ); \n"); //$NON-NLS-1$
		writer.write( " int f( char ){ } "); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTFunction f1 = (IASTFunction) i.next();
		assertEquals( f1.getName(), "f"); //$NON-NLS-1$
		Iterator parms = f1.getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration) parms.next();
		assertTrue( parm.getTypeSpecifier() instanceof IASTSimpleTypeSpecifier ); 
		assertEquals( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).getTypename(), "bool" ); //$NON-NLS-1$
		
		IASTFunction f2 = (IASTFunction) i.next();
		assertEquals( f2.getName(), "f"); //$NON-NLS-1$
		parms = f2.getParameters();
		parm = (IASTParameterDeclaration) parms.next();
		assertTrue( parm.getTypeSpecifier() instanceof IASTSimpleTypeSpecifier ); 
		assertEquals( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).getTypename(), "char" ); //$NON-NLS-1$
		assertFalse( f2.previouslyDeclared() );
		assertFalse( i.hasNext() );
	}

	public void testBug54639() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "typedef enum _A { } A, *pA; " ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTypedefDeclaration typedef = (IASTTypedefDeclaration)i.next();
		assertEquals( typedef.getName(), "A" ); //$NON-NLS-1$
		IASTEnumerationSpecifier enumSpec = (IASTEnumerationSpecifier) typedef.getAbstractDeclarator().getTypeSpecifier();
		assertEquals( enumSpec.getName(), "_A" ); //$NON-NLS-1$
		
		IASTTypedefDeclaration typedef2 = (IASTTypedefDeclaration)i.next();
		assertEquals( typedef2.getName(), "pA" ); //$NON-NLS-1$
		assertEquals( typedef2.getAbstractDeclarator().getPointerOperators().next(), ASTPointerOperator.POINTER );
		enumSpec = (IASTEnumerationSpecifier) typedef2.getAbstractDeclarator().getTypeSpecifier();
		assertEquals( enumSpec.getName(), "_A" ); //$NON-NLS-1$
		
		assertFalse( i.hasNext() ); 
	}
	
	public void testBug55163() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "void foo() { \n"); //$NON-NLS-1$
		writer.write( "   int i, n; \n"); //$NON-NLS-1$
		writer.write( "   double di; \n"); //$NON-NLS-1$
		writer.write( "   for( i = n - 1, di = (double)( i + i ); i > 0; i-- ){ } \n"); //$NON-NLS-1$
		writer.write( "}\n"); //$NON-NLS-1$
		
		Iterator iter = parse( writer.toString() ).getDeclarations();
		
		IASTFunction foo = (IASTFunction) iter.next();
		assertFalse( iter.hasNext() );
		iter = getDeclarations( foo );
		IASTVariable i = (IASTVariable)iter.next();
		IASTVariable n = (IASTVariable)iter.next();
		IASTVariable di = (IASTVariable)iter.next();
		
		assertAllReferences( 7, createTaskList( new Task( n ), new Task( i, 5 ), new Task( di ) ) );
		
	}
	public void testBug55673() throws Exception{
		Writer writer = new StringWriter();
		writer.write( "struct Example { int i;  int ( * pfi ) ( int ); }; "); //$NON-NLS-1$
		
		Iterator iter = parse( writer.toString() ).getDeclarations();
		
		IASTClassSpecifier example = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)iter.next()).getTypeSpecifier();
		assertFalse( iter.hasNext() );
		
		iter = getDeclarations( example ); 
		
		IASTField i = (IASTField) iter.next();
		IASTField pfi = (IASTField) iter.next();
		
		assertFalse( iter.hasNext() );
	}
	
	public void testBug54531() throws Exception
	{
		Iterator i = parse( "typedef enum _A {} A, *pA;" ).getDeclarations(); //$NON-NLS-1$
		IASTTypedefDeclaration theEnum  = (IASTTypedefDeclaration) i.next();
		assertEquals( theEnum.getName(), "A"); //$NON-NLS-1$
		IASTTypedefDeclaration thePointer = (IASTTypedefDeclaration) i.next();
		assertEquals( thePointer.getName(), "pA" ); //$NON-NLS-1$
		assertFalse( i.hasNext() );
	}
	
	public void testBug56516() throws Exception
	{
		Iterator i = parse( "typedef struct blah sb;").getDeclarations(); //$NON-NLS-1$
		IASTTypedefDeclaration sb = (IASTTypedefDeclaration) i.next();
		assertEquals( sb.getName(), "sb"); //$NON-NLS-1$
		assertFalse( i.hasNext() );
		IASTElaboratedTypeSpecifier elab = ((IASTElaboratedTypeSpecifier)sb.getAbstractDeclarator().getTypeSpecifier());
		assertEquals( elab.getName(), "blah"); //$NON-NLS-1$
		assertEquals( elab.getClassKind(), ASTClassKind.STRUCT );
	}
	
	public void testBug53786() throws Exception
	{
		Iterator i = parse( "struct Example {  struct Data * data; };").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier Example = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		assertFalse( i.hasNext() );
		assertEquals( Example.getName(), "Example"); //$NON-NLS-1$
		assertEquals( Example.getClassKind(), ASTClassKind.STRUCT );
		Iterator j = getDeclarations( Example );
		IASTField data = (IASTField) j.next();
		assertFalse( j.hasNext() );
		assertEquals( data.getName(), "data" ); //$NON-NLS-1$
	}
	
	public void testBug54029() throws Exception
	{
		Iterator i = parse( "typedef int T; T i;" ).getDeclarations(); //$NON-NLS-1$
		IASTTypedefDeclaration typedef = (IASTTypedefDeclaration) i.next();
		assertEquals( typedef.getName(), "T"); //$NON-NLS-1$
		assertTrue( typedef.getAbstractDeclarator().getTypeSpecifier() instanceof IASTSimpleTypeSpecifier );
		assertEquals( ((IASTSimpleTypeSpecifier)typedef.getAbstractDeclarator().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT );
		IASTVariable variable = (IASTVariable) i.next();
		assertFalse( i.hasNext() );
		assertEquals( variable.getName(), "i"); //$NON-NLS-1$
		assertEquals( ((IASTSimpleTypeSpecifier)variable.getAbstractDeclaration().getTypeSpecifier()).getTypename(), "T" ); //$NON-NLS-1$
		assertNotNull( ((IASTSimpleTypeSpecifier)variable.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier() );
		assertEquals( ((IASTSimpleTypeSpecifier)variable.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), typedef );
	}

	public void testBug47625() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("struct s { int num; }; "); //$NON-NLS-1$
		writer.write("namespace ns{ "); //$NON-NLS-1$
		writer.write("   struct s { double num; };"); //$NON-NLS-1$
		writer.write("   s inner = { 3.14 };"); //$NON-NLS-1$
		writer.write("   ::s outer = { 42 };"); //$NON-NLS-1$
		writer.write("}"); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		IASTClassSpecifier outerS = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTNamespaceDefinition ns = (IASTNamespaceDefinition) i.next();
		
		i = getDeclarations( ns );
		IASTClassSpecifier innerS = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable inner = (IASTVariable) i.next();
		IASTVariable outer = (IASTVariable) i.next();
		
		assertAllReferences( 2, createTaskList( new Task( outerS ), new Task( innerS ) ) );
	}
	
	public void testBug57754() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "struct X {          " ); //$NON-NLS-1$
		writer.write( "   typedef int T;   " ); //$NON-NLS-1$
		writer.write( "   void f( T );     " ); //$NON-NLS-1$
		writer.write( "};                  " ); //$NON-NLS-1$
		writer.write( "void X::f( T ) { }  " ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTClassSpecifier X = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTMethod f = (IASTMethod) i.next();
		
		assertTrue( f.previouslyDeclared() );
		
		i = getDeclarations( X );
		IASTTypedefDeclaration T = (IASTTypedefDeclaration) i.next();
		
		assertAllReferences( 3, createTaskList( new Task( X ), new Task( T, 2 ) ) );
	}	
	
	public void testBug57800() throws Exception
	{
		Writer writer= new StringWriter();
		writer.write( "class G2 { int j; };"); //$NON-NLS-1$
		writer.write( "typedef G2 AltG2;"); //$NON-NLS-1$
		writer.write( "class AltG3 : AltG2 {  int x;};"); //$NON-NLS-1$
		Iterator i = parse( writer.toString() ).getDeclarations();
		IASTClassSpecifier G2 = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTTypedefDeclaration AltG2 = (IASTTypedefDeclaration) i.next();
		IASTClassSpecifier AltG3 = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		assertFalse( i.hasNext() );
		Iterator baseClauses = AltG3.getBaseClauses();
		IASTBaseSpecifier baseClause = (IASTBaseSpecifier) baseClauses.next();
		assertFalse( baseClauses.hasNext() );
		assertEquals( baseClause.getParentClassSpecifier(), AltG2 );
	}
	
	public void testBug46246() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "struct A {                 "); //$NON-NLS-1$
		writer.write( "   struct B { int ab; } b; "); //$NON-NLS-1$
		writer.write( "   int a;                  "); //$NON-NLS-1$
		writer.write( "};                         "); //$NON-NLS-1$
		writer.write( "struct A a1;               "); //$NON-NLS-1$
		writer.write( "struct B b1;               "); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString(), true, ParserLanguage.C ).getDeclarations();
		IASTClassSpecifier A = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a1 = (IASTVariable) i.next();
		IASTVariable b1 = (IASTVariable) i.next();
		i = getDeclarations( A );
		IASTField b = (IASTField) i.next();
		IASTField a = (IASTField) i.next();
		IASTClassSpecifier B = (IASTClassSpecifier) b.getAbstractDeclaration().getTypeSpecifier();
		
		assertAllReferences( 2, createTaskList( new Task( A ), new Task( B ) ) );
	}
	
	public void testBug45235() throws Exception
	{
		Iterator i = parse( "class A { friend class B; friend void f(); }; " ).getDeclarations(); //$NON-NLS-1$
		
		IASTClassSpecifier A = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		
		i = getDeclarations( A );
		
		IASTAbstractTypeSpecifierDeclaration forewardDecl = (IASTAbstractTypeSpecifierDeclaration)i.next();
		IASTFunction f = (IASTFunction) i.next();
				
		assertTrue( forewardDecl.isFriendDeclaration() );
		assertTrue( f.isFriend() );
	}
	
	public void testBug57791() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write(" void f() {                  "); //$NON-NLS-1$
		writer.write("    struct astruct astruct;  "); //$NON-NLS-1$
		writer.write("    astruct.foo++;           "); //$NON-NLS-1$
		writer.write(" }"); //$NON-NLS-1$
		
		parse( writer.toString(), true, ParserLanguage.C );
	}
	
	public void testBug44249() throws Exception
	{

		Iterator i = parse( "class SD_01 { public:\n	void SD_01::f_SD_01();};" ).getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier SD_01 = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		assertFalse( i.hasNext() );
		i = getDeclarations( SD_01 );
		IASTMethod f_SD_01 = (IASTMethod) i.next();
		assertFalse( i.hasNext() );
		assertEquals( f_SD_01.getName(), "f_SD_01"); //$NON-NLS-1$
		assertAllReferences( 1, createTaskList( new Task( SD_01 )));
	}
	
	public void testBug59149() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "class A{ friend class B; friend class B; };" ); //$NON-NLS-1$
		writer.write( "class B{ };" ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		IASTClassSpecifier A = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTClassSpecifier B = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
	}	
    
    public void testBug59302() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write("class A { class N{}; };         "); //$NON-NLS-1$
    	writer.write("class B { friend class A::N; }; "); //$NON-NLS-1$
    	
    	Iterator i = parse( writer.toString() ).getDeclarations();
    	IASTClassSpecifier A = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
    	IASTClassSpecifier B = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
    	
    	i = getDeclarations( A );
    	IASTClassSpecifier N = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
    	
    	assertFalse( A.getFriends().hasNext() );
    	assertEquals( B.getFriends().next(), N );
	}
	
    

    public void testULong() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "#ifndef ASMINCLUDE\n"); //$NON-NLS-1$
    	writer.write( "typedef unsigned short         ushort;\n"); //$NON-NLS-1$
    	writer.write( "typedef volatile unsigned long semaphore;\n"); //$NON-NLS-1$
    	writer.write( "typedef unsigned long          ulong;\n"); //$NON-NLS-1$
    	writer.write( "#ifndef _NO_LONGLONG\n"); //$NON-NLS-1$
    	writer.write( "typedef long long              longlong;\n"); //$NON-NLS-1$
    	writer.write( "typedef unsigned long long     ulonglong;\n"); //$NON-NLS-1$
    	writer.write( "#endif  /* _NO_LONGLONG */\n"); //$NON-NLS-1$
    	writer.write( "#endif  /*  ASMINCLUDE  */\n"); //$NON-NLS-1$
    	writer.write( "typedef struct section_type_ {\n"); //$NON-NLS-1$
    	writer.write( "ulong source;\n"); //$NON-NLS-1$
    	writer.write( "ulong dest;\n"); //$NON-NLS-1$
    	writer.write( "ulong bytes;\n"); //$NON-NLS-1$
    	writer.write( "} section_type;\n"); //$NON-NLS-1$
    	Iterator i = parse( writer.toString() ).getDeclarations();
    	IASTTypedefDeclaration ushort = (IASTTypedefDeclaration) i.next();
    	IASTTypedefDeclaration semaphore = (IASTTypedefDeclaration) i.next();
    	IASTTypedefDeclaration ulong = (IASTTypedefDeclaration) i.next();
    	IASTTypedefDeclaration longlong = (IASTTypedefDeclaration) i.next();
    	IASTTypedefDeclaration ulonglong = (IASTTypedefDeclaration) i.next();
    	IASTTypedefDeclaration section_type = (IASTTypedefDeclaration) i.next();
    	IASTClassSpecifier section_type_ = (IASTClassSpecifier) section_type.getAbstractDeclarator().getTypeSpecifier();
    	Iterator fields = getDeclarations(section_type_);
    	IASTField source = (IASTField) fields.next();
    	assertEquals( ((IASTSimpleTypeSpecifier)source.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), ulong );
    	IASTField dest = (IASTField) fields.next();
    	assertEquals( ((IASTSimpleTypeSpecifier)dest.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), ulong );
    	IASTField bytes = (IASTField) fields.next();
    	assertEquals( ((IASTSimpleTypeSpecifier)bytes.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), ulong );
    	
    	assertFalse( i.hasNext() );
    	
	}
 
    public void testBug47926() throws Exception
	{
    	Iterator i = parse( "void f() {} class A {}; void main() { A * a = new A(); a->f();	}", false ).getDeclarations(); //$NON-NLS-1$
    	IASTFunction f = (IASTFunction) i.next();
    	IASTClassSpecifier A = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next() ).getTypeSpecifier();
    	IASTFunction main = (IASTFunction) i.next();
    	assertFalse( i.hasNext() );
    	i = getDeclarations( main );
    	IASTVariable a = (IASTVariable) i.next();
    	assertAllReferences( 3, createTaskList( new Task( A, 2 ), new Task( a )));
	}
    
    public void testBug50984_ASTMethod_getOwnerClassSpecifier_ClassCastException() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "template < typename _OutIter >                                 " ); //$NON-NLS-1$
    	writer.write( "class num_put {                                                " ); //$NON-NLS-1$
    	writer.write( "   typedef _OutIter iter_type;                                 " ); //$NON-NLS-1$
    	writer.write( "   template< typename _ValueT >                                " ); //$NON-NLS-1$
    	writer.write( "    iter_type _M_convert_float( iter_type );                   " ); //$NON-NLS-1$
    	writer.write( "};                                                             " ); //$NON-NLS-1$
    	writer.write( "template < typename _OutIter >                                 " ); //$NON-NLS-1$
    	writer.write( "template < typename _ValueT  >                                 " ); //$NON-NLS-1$
    	writer.write( "_OutIter num_put<_OutIter>::_M_convert_float( _OutIter ) { }   " ); //$NON-NLS-1$
    	
    	Iterator i = parse( writer.toString() ).getDeclarations();
    	
    	IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
    	IASTClassSpecifier num_put = (IASTClassSpecifier) template.getOwnedDeclaration();
    	IASTTemplateDeclaration defn = (IASTTemplateDeclaration) i.next();
    	IASTMethod convert = (IASTMethod) defn.getOwnedDeclaration();
    	
    	assertEquals( convert.getOwnerClassSpecifier(), num_put );
   	}
    
    public void testGloballyQualifiedUsingDeclaration() throws Exception
	{
		Iterator declarations = parse( "int iii; namespace N { using ::iii; }" ).getDeclarations(); //$NON-NLS-1$
		
		IASTVariable iii = (IASTVariable) declarations.next();
		IASTNamespaceDefinition namespaceN = (IASTNamespaceDefinition)declarations.next();
		
		IASTUsingDeclaration using = (IASTUsingDeclaration)(getDeclarations(namespaceN).next()); 

		assertEquals( callback.getReferences().size(), 1 );
		
		Iterator references = callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), iii );
	}
    
    public void test57513_new() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "class A{ A(); A( int ); };   \n" ); //$NON-NLS-1$
    	writer.write( " void f() {                  \n" ); //$NON-NLS-1$
    	writer.write( "    A * a1 = new A;          \n" ); //$NON-NLS-1$
    	writer.write( "    A * a2 = new(1)A();      \n" ); //$NON-NLS-1$
    	writer.write( "    A * a3 = new A( 1 );     \n" ); //$NON-NLS-1$
    	writer.write( "}                            \n" ); //$NON-NLS-1$
    	
    	Iterator i = parse( writer.toString() ).getDeclarations();
    	
    	IASTClassSpecifier A = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next() ).getTypeSpecifier();
    	IASTFunction f = (IASTFunction) i.next();
    	assertFalse( i.hasNext() );
    	
    	i = getDeclarations( A );
    	IASTMethod constructor1 = (IASTMethod) i.next();
    	IASTMethod constructor2 = (IASTMethod) i.next();
    	assertFalse( i.hasNext() );
    	
    	assertReferenceTask( new Task( constructor1, 2, false, false ) );
    	assertReferenceTask( new Task( constructor2, 1, false, false ) );
    	assertReferenceTask( new Task( A, 3, false, false ) );
	}

    public void test57513_NoConstructor() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "class A{  };   \n" ); //$NON-NLS-1$
    	writer.write( " void f() {                  \n" ); //$NON-NLS-1$
    	writer.write( "    A * a1 = new A;          \n" ); //$NON-NLS-1$
    	writer.write( "}                            \n" ); //$NON-NLS-1$
    	
    	Iterator i = parse( writer.toString() ).getDeclarations();
    	
    	IASTClassSpecifier A = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next() ).getTypeSpecifier();
    	IASTFunction f = (IASTFunction) i.next();
    	assertFalse( i.hasNext() );
    	
    	assertReferenceTask( new Task( A, 2, false, false ) );
	}
    
    public void test57513_ctorinit() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "class A{ A(); A( A * ); };   \n" ); //$NON-NLS-1$
    	writer.write( "class B : public A { B(); }; \n" ); //$NON-NLS-1$
    	writer.write( "B::B():A( new A ){}          \n" ); //$NON-NLS-1$
    	
    	Iterator i = parse( writer.toString() ).getDeclarations();
    	
    	IASTClassSpecifier A = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next() ).getTypeSpecifier();
    	IASTClassSpecifier B = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next() ).getTypeSpecifier();
    	IASTMethod constructorB = (IASTMethod) i.next();
    	assertFalse( i.hasNext() );
    	
    	i = getDeclarations( A );
    	IASTMethod constructor1 = (IASTMethod) i.next();
    	IASTMethod constructor2 = (IASTMethod) i.next();
    	assertFalse( i.hasNext() );
    	
    	assertReferenceTask( new Task( constructor1, 1, false, false ) );
    	assertReferenceTask( new Task( constructor2, 1, false, false ) );
    	assertReferenceTask( new Task( A, 2, false, false ) );
	}
    
    public void test575513_qualified() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "namespace Foo{                     " ); //$NON-NLS-1$
    	writer.write( "   class Bar{ public : Bar(); };   " ); //$NON-NLS-1$
    	writer.write( "}                                  " ); //$NON-NLS-1$
    	writer.write( "void main(){                       " ); //$NON-NLS-1$
    	writer.write( "  Foo::Bar * bar = new Foo::Bar(); " ); //$NON-NLS-1$
    	writer.write( "}                                  " ); //$NON-NLS-1$
    	
    	Iterator i = parse( writer.toString() ).getDeclarations();
    	
    	IASTNamespaceDefinition namespace = (IASTNamespaceDefinition) i.next();
    	IASTFunction main = (IASTFunction) i.next();
    	i = getDeclarations( namespace );
    	IASTClassSpecifier Bar = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next() ).getTypeSpecifier();
    	i = getDeclarations( Bar );
    	IASTMethod constructor = (IASTMethod) i.next();
    	
    	assertAllReferences( 4, createTaskList( new Task(namespace, 2 ), new Task( Bar, 1 ), new Task( constructor, 1 ) ) );
	}
    
    public void testBug60944() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "typedef int OurInt;\n"); //$NON-NLS-1$
    	writer.write( "class A { int x; };\n"); //$NON-NLS-1$
    	writer.write( "typedef A AnotherA;\n"); //$NON-NLS-1$
    	writer.write( "typedef AnotherA SecondA;\n"); //$NON-NLS-1$
    	writer.write( "typedef OurInt AnotherInt;\n" ); //$NON-NLS-1$
    	Iterator i = parse( writer.toString() ).getDeclarations();
    	IASTTypedefDeclaration OurInt = (IASTTypedefDeclaration) i.next();
    	assertTrue( OurInt.getFinalTypeSpecifier() instanceof IASTSimpleTypeSpecifier );
    	assertEquals( ((IASTSimpleTypeSpecifier)OurInt.getFinalTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT );
    	IASTClassSpecifier A = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
    	IASTTypedefDeclaration AnotherA = (IASTTypedefDeclaration) i.next();
    	assertEquals( AnotherA.getFinalTypeSpecifier(), A );
    	IASTTypedefDeclaration SecondA = (IASTTypedefDeclaration) i.next();
    	assertEquals( SecondA.getFinalTypeSpecifier(), A );
    	IASTTypedefDeclaration AnotherInt = (IASTTypedefDeclaration) i.next();
    	assertTrue( AnotherInt.getFinalTypeSpecifier() instanceof IASTSimpleTypeSpecifier );
    	assertEquals( ((IASTSimpleTypeSpecifier)AnotherInt.getFinalTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT );

    	assertFalse( i.hasNext() );
	}
    
    public void testDestructorReference() throws Exception
    {
    	Writer writer = new StringWriter();
    	writer.write( "class ABC {\n"); //$NON-NLS-1$
    	writer.write( " public:\n"); //$NON-NLS-1$
    	writer.write( " ~ABC(){ }\n"); //$NON-NLS-1$
    	writer.write( "};\n"); //$NON-NLS-1$
    	writer.write( "int main() { ABC * abc = new ABC();\n"); //$NON-NLS-1$
    	writer.write( "abc->~ABC();\n"); //$NON-NLS-1$
    	writer.write( "}\n"); //$NON-NLS-1$
    	
		Iterator declarations = parse( writer.toString() ).getDeclarations();  
		IASTClassSpecifier ABC = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTFunction main = (IASTFunction) declarations.next();
		assertFalse( declarations.hasNext() );
		Iterator members = getDeclarations(ABC);
		IASTFunction destructor = (IASTFunction) members.next();
		assertFalse( members.hasNext() );
		Iterator localVariables = getDeclarations( main );
		IASTVariable variable = (IASTVariable) localVariables.next();
		assertFalse( localVariables.hasNext() );
		assertAllReferences( 4, createTaskList( new Task( ABC, 2 ), new Task( variable ), new Task( destructor )));
    }
    
    public void testBug39676_tough() throws Exception
	{
    	parse( "int widths[] = { [0 ... 9] = 1, [10 ... 99] = 2, [100] = 3 };", true, ParserLanguage.C ); //$NON-NLS-1$
	}
    
    public void testBug60939() throws Exception
	{
    	for( int i = 0; i < 2; ++i )
    	{
	    	Writer writer = new StringWriter();
	    	writer.write( "namespace ABC { class DEF { }; }\n"); //$NON-NLS-1$
	    	if( i == 0 )
	    		writer.write( "using namespace ABC;\n"); //$NON-NLS-1$
	    	else
	    		writer.write( "using ABC::DEF;\n"); //$NON-NLS-1$
	    	writer.write( "class GHI : public DEF { };"); //$NON-NLS-1$
	    	Iterator d = parse( writer.toString() ).getDeclarations();
	    	IASTNamespaceDefinition ABC = (IASTNamespaceDefinition) d.next();
	    	if( i == 0 )
	    		assertTrue( d.next() instanceof IASTUsingDirective );
	    	else
	    		assertTrue( d.next() instanceof IASTUsingDeclaration );
	    	IASTClassSpecifier GHI = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)d.next()).getTypeSpecifier();
	    	Iterator baseClauses = GHI.getBaseClauses();
	    	IASTBaseSpecifier baseClause = (IASTBaseSpecifier) baseClauses.next();
	    	IASTClassSpecifier DEF = (IASTClassSpecifier) baseClause.getParentClassSpecifier();
	    	String [] theTruth = new String[2];
	    	theTruth[0] = "ABC"; //$NON-NLS-1$
	    	theTruth[1] = "DEF"; //$NON-NLS-1$
	    	qualifiedNamesEquals( DEF.getFullyQualifiedName(), theTruth );
    	}
    	

	}
    
    public void testBug64010() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( " #define ONE	else if (0) { } \n"); //$NON-NLS-1$
    	writer.write( " #define TEN	ONE ONE ONE ONE ONE ONE ONE ONE ONE ONE \n "); //$NON-NLS-1$
    	writer.write( " #define HUN	TEN TEN TEN TEN TEN TEN TEN TEN TEN TEN \n "); //$NON-NLS-1$
    	writer.write( " #define THOU	HUN HUN HUN HUN HUN HUN HUN HUN HUN HUN \n"); //$NON-NLS-1$
		writer.write("void foo()                                                "); //$NON-NLS-1$
		writer.write("{                                                         "); //$NON-NLS-1$
		writer.write("   if (0) { }                                             "); //$NON-NLS-1$
		writer.write("   /* 11,000 else if's.  */                               "); //$NON-NLS-1$
		writer.write("   THOU THOU THOU THOU THOU THOU THOU THOU THOU THOU THOU "); //$NON-NLS-1$
		writer.write("}                                                         "); //$NON-NLS-1$
		
		Iterator d = parse( writer.toString() ).getDeclarations();
	
		IASTFunction f = (IASTFunction) d.next();
	}
    
    public void testBug64271() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "typedef int DWORD;\n" ); //$NON-NLS-1$
		writer.write( "typedef char BYTE;\n"); //$NON-NLS-1$
		writer.write( "#define MAKEFOURCC(ch0, ch1, ch2, ch3)                              \\\n"); //$NON-NLS-1$
		writer.write( "((DWORD)(BYTE)(ch0) | ((DWORD)(BYTE)(ch1) << 8) |       \\\n"); //$NON-NLS-1$
		writer.write( "((DWORD)(BYTE)(ch2) << 16) | ((DWORD)(BYTE)(ch3) << 24 ))\n"); //$NON-NLS-1$
		writer.write( "enum e {\n"); //$NON-NLS-1$
		writer.write( "blah1 = 5,\n"); //$NON-NLS-1$
		writer.write( "blah2 = MAKEFOURCC('a', 'b', 'c', 'd'),\n"); //$NON-NLS-1$
		writer.write( "blah3\n"); //$NON-NLS-1$
		writer.write( "};\n"); //$NON-NLS-1$
		writer.write( "e mye = blah;\n"); //$NON-NLS-1$
		parse( writer.toString() );
	}
    
    public void testBug47752() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "class BBC\n"); //$NON-NLS-1$
    	writer.write( "{\n"); //$NON-NLS-1$
    	writer.write( "int x;\n"); //$NON-NLS-1$
    	writer.write( "};\n"); //$NON-NLS-1$
    	writer.write( "void func( BBC bar )\n"); //$NON-NLS-1$
    	writer.write( "try\n"); //$NON-NLS-1$
    	writer.write( "{\n"); //$NON-NLS-1$
    	writer.write( "}\n"); //$NON-NLS-1$
    	writer.write( "catch ( BBC error )\n"); //$NON-NLS-1$
    	writer.write( "{\n"); //$NON-NLS-1$
    	writer.write( "		  //... error handling code ...\n"); //$NON-NLS-1$
    	writer.write( "}\n"); //$NON-NLS-1$
    	parse( writer.toString() );
    	assertEquals( callback.getReferences().size(), 2 );
	}
    public void testBug61972() throws Exception
	{
    	parse( "#define DEF1(A1) A1\n#define DEF2     DEF1(DEF2)\nDEF2;", false ); //$NON-NLS-1$
	}
    
    public void testBug65569() throws Exception
	{
    	parse( "class Sample;\nstruct Sample { /* ... */ };" ); //$NON-NLS-1$
	}
    
    public void testBug64268() throws Exception
	{
    	Writer writer = new StringWriter();
		writer.write("#define BODY \\\n"); //$NON-NLS-1$
		writer.write("for (;;) {	 \\\n"); //$NON-NLS-1$
		writer.write("/* this multi-line comment messes \\\n"); //$NON-NLS-1$
		writer.write("up the parser.  */ }\n"); //$NON-NLS-1$
		writer.write("	void abc() {\n"); //$NON-NLS-1$
		writer.write("BODY\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
		parse( writer.toString() );
	}
    
    public void testBug67622() throws Exception
	{
    	parse( "const char * x = __FILE__;"); //$NON-NLS-1$
	}
    
    public void testBug67680() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "template < class T> class Base {};                  \n" ); //$NON-NLS-1$
    	writer.write( "class Derived : public Base, Base<int>, foo {};     \n" ); //$NON-NLS-1$
    	
    	Iterator i = parse( writer.toString(), false ).getDeclarations();
    	
    	IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
    	IASTClassSpecifier base = (IASTClassSpecifier) template.getOwnedDeclaration();
    	IASTClassSpecifier derived = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
    	
    	//only Base<int> is a valid parent, Base and foo are not expected to show up in the iterator.
    	i = derived.getBaseClauses();
    	IASTBaseSpecifier parent = (IASTBaseSpecifier) i.next();
    	assertFalse( i.hasNext() );
    	
    	assertEquals( parent.getParentClassSpecifier(), base );
	}
    
    public void testTypeIDSignature() throws Exception
    {
    	IASTVariable v = (IASTVariable) parse( "int * v = (int*)0;").getDeclarations().next();//$NON-NLS-1$
    	IASTTypeId typeId = v.getInitializerClause().getAssigmentExpression().getTypeId();
    	assertEquals( typeId.getFullSignature(), "int *"); //$NON-NLS-1$
    }
    
    public void testUnaryAmperCast() throws Exception{
    	Writer writer = new StringWriter();
    	writer.write( "void f( char * );              \r\n "); //$NON-NLS-1$
    	writer.write( "void f( char   );              \n "); //$NON-NLS-1$
    	writer.write( "void main() {                  \n "); //$NON-NLS-1$
    	writer.write( "   char * t = new char [ 5 ];  \n "); //$NON-NLS-1$
    	writer.write( "   f( &t[1] );                 \n "); //$NON-NLS-1$
    	writer.write( "}                              \n "); //$NON-NLS-1$
    	
    	Iterator i = parse( writer.toString() ).getDeclarations();
    	IASTFunction f1 = (IASTFunction) i.next();
    	IASTFunction f2 = (IASTFunction) i.next();
    	
    	IASTFunction main  = (IASTFunction) i.next();
    	IASTVariable t = (IASTVariable) getDeclarations( main ).next();
    	
    	assertAllReferences( 2, createTaskList( new Task( f1, 1, false, false), new Task( t ) ) );
    }
	
    public void testBug68235() throws Exception{
    	Writer writer = new StringWriter();
    	writer.write( " struct xTag { int x; };               "); //$NON-NLS-1$
    	writer.write( " typedef xTag xType;                   "); //$NON-NLS-1$
    	writer.write( " typedef struct yTag { int x; } yType; "); //$NON-NLS-1$
    	writer.write( " class C1 { xType x; yType y; };       "); //$NON-NLS-1$
    	
    	Iterator i = parse( writer.toString() ).getDeclarations();
    	
    	IASTClassSpecifier xTag = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
    	IASTTypedefDeclaration xType = (IASTTypedefDeclaration) i.next();
    	IASTSimpleTypeSpecifier typeSpec = (IASTSimpleTypeSpecifier) xType.getAbstractDeclarator().getTypeSpecifier();
    	assertEquals( typeSpec.getTypeSpecifier(), xTag );
    	
    	IASTTypedefDeclaration yType = (IASTTypedefDeclaration) i.next();
    	IASTClassSpecifier yTag = (IASTClassSpecifier) yType.getAbstractDeclarator().getTypeSpecifier();
    	
    	IASTClassSpecifier C1 = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
    	assertFalse( i.hasNext() );
    	i = getDeclarations( C1 );
    	
    	IASTField x = (IASTField) i.next();
    	IASTField y = (IASTField) i.next();
    	assertFalse( i.hasNext() );
    	
    	IASTSimpleTypeSpecifier simple = (IASTSimpleTypeSpecifier) x.getAbstractDeclaration().getTypeSpecifier();
    	assertEquals( simple.getTypeSpecifier(), xType );
    	simple = (IASTSimpleTypeSpecifier) y.getAbstractDeclaration().getTypeSpecifier();
    	assertEquals( simple.getTypeSpecifier(), yType );
    }
    
    public void testBug60407() throws Exception
    {
    	Writer writer = new StringWriter();
    	writer.write( "struct ZZZ { int x, y, z; };\r\n" ); //$NON-NLS-1$
    	writer.write( "typedef struct ZZZ _FILE;\n" ); //$NON-NLS-1$
    	writer.write( "typedef _FILE FILE;\n" ); //$NON-NLS-1$
    	writer.write( "static void static_function(FILE * lcd){}\n" ); //$NON-NLS-1$
    	writer.write( "int	main(int argc, char **argv) {\n" ); //$NON-NLS-1$
    	writer.write( "FILE * file = 0;\n" ); //$NON-NLS-1$
    	writer.write( "static_function( file );\n" ); //$NON-NLS-1$
    	writer.write( "return 0;\n" );	 //$NON-NLS-1$
    	writer.write( "}\n" ); //$NON-NLS-1$
    	parse( writer.toString() );
    }
    
    public void testBug68623() throws Exception{
        Writer writer = new StringWriter();
        writer.write( "class A {                         \n" ); //$NON-NLS-1$
        writer.write( "   A();                           \n" ); //$NON-NLS-1$
        writer.write( "   class sub{};                   \n" ); //$NON-NLS-1$
        writer.write( "   sub * x;                       \n" ); //$NON-NLS-1$
        writer.write( "};                                \n" ); //$NON-NLS-1$
        writer.write( "A::A() : x( (sub *) 0 ) {}        \n" ); //$NON-NLS-1$
        
        parse( writer.toString() );
        
        writer = new StringWriter();
        writer.write( "class A {                         \n" ); //$NON-NLS-1$
        writer.write( "   A() : x (0) {}                 \n" ); //$NON-NLS-1$
        writer.write( "   int x;                         \n" ); //$NON-NLS-1$
        writer.write( "};                                \n" ); //$NON-NLS-1$
        
        Iterator i = parse( writer.toString() ).getDeclarations();
        IASTClassSpecifier A = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
        
        i = A.getDeclarations();
        IASTMethod constructor = (IASTMethod) i.next();
        IASTField x = (IASTField) i.next();
        
        assertAllReferences( 1, createTaskList( new Task( x ) ) );
    }
    
    public void testBug69798() throws Exception{
        Writer writer = new StringWriter();
        writer.write( "enum Flags { FLAG1, FLAG2 };                          \n" ); //$NON-NLS-1$
        writer.write( "int f() { int a, b;  b = ( a ? FLAG1 : 0 ) | FLAG2; } \n" ); //$NON-NLS-1$
        
        parse( writer.toString() );
    }
    
    public void testBug69662() throws Exception{
        Writer writer = new StringWriter();
        writer.write( "class A { operator float * (); };  \n" ); //$NON-NLS-1$
        writer.write( "A::operator float * () { }         \n" ); //$NON-NLS-1$
        
        parse( writer.toString() );
    }
    
    
    public void testBug68528() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "namespace N526026\n" ); //$NON-NLS-1$
    	writer.write( "{\n" ); //$NON-NLS-1$
    	writer.write( "template <typename T>\n" ); //$NON-NLS-1$
    	writer.write( "class T526026\n" ); //$NON-NLS-1$
    	writer.write( "{\n" ); //$NON-NLS-1$
    	writer.write( "typedef int diff;\n" ); //$NON-NLS-1$
    	writer.write( "};\n" ); //$NON-NLS-1$
    	writer.write( "\n" ); //$NON-NLS-1$
    	writer.write( "template<typename T>\n" ); //$NON-NLS-1$
    	writer.write( "inline T526026< T >\n" );  //$NON-NLS-1$
    	writer.write( "operator+(typename T526026<T>::diff d, const T526026<T> & x )\n" );  //$NON-NLS-1$
    	writer.write( "{ return T526026< T >(); }\n" ); //$NON-NLS-1$
    	writer.write( "}\n" ); //$NON-NLS-1$
    	parse( writer.toString(), false );
	}
    
    public void testBug71094() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "using namespace DOESNOTEXIST;\n" );  //$NON-NLS-1$
    	writer.write( "class A { int x; };\n" ); //$NON-NLS-1$
    	Iterator i = parse( writer.toString(), false ).getDeclarations();
    	assertTrue( i.hasNext() );
    	assertTrue( i.next() instanceof IASTAbstractTypeSpecifierDeclaration );
    	assertFalse( i.hasNext() );
	}
    
	public void testPredefinedSymbol_bug70928() throws Exception {
		// GNU builtin storage class type __cdecl preceded by a custom return type 
		Iterator i = parse("typedef int size_t; \n int __cdecl foo(); \n").getDeclarations();//$NON-NLS-1$
		IASTTypedefDeclaration td = (IASTTypedefDeclaration) i.next();
		IASTFunction fd = (IASTFunction) i.next();
		assertFalse(i.hasNext());
	}

    public void testBug73652() throws Exception
	{
    	StringWriter writer = new StringWriter();
    	writer.write( "#define DoSuperMethodA IDoSuperMethodA\n" ); //$NON-NLS-1$
    	writer.write( "#define IDoSuperMethodA(a,b,c) IIntuition->IDoSuperMethodA(a,b,c)\n" ); //$NON-NLS-1$
		writer.write( "void hang(void)\n" ); //$NON-NLS-1$
		writer.write( "{\n" ); //$NON-NLS-1$
		writer.write( "DoSuperMethodA(0,0,0);\n" ); //$NON-NLS-1$
		writer.write( "}\n" ); //$NON-NLS-1$
		parse( writer.toString() , false );
	}
    
    public void testBug73428() throws Exception
	{
    	parse( "namespace {  }");//$NON-NLS-1$
    	assertTrue( callback.problems.isEmpty() );
    	parse( "namespace {  };");//$NON-NLS-1$
    	assertTrue( callback.problems.isEmpty() );
    	parse( "namespace {  int abc; };");//$NON-NLS-1$
    	assertTrue( callback.problems.isEmpty() );
    	parse( "namespace {  int abc; }");//$NON-NLS-1$
    	assertTrue( callback.problems.isEmpty() );
	}
    
    public void testBug73615() throws Exception
	{
    	for( int i = 0; i < 2; ++i )
    	{
    		StringWriter writer = new StringWriter();
    		if( i == 0 )
    			writer.write( "class B;\n"); //$NON-NLS-1$
    		writer.write( "class A { A( B * ); };\n"); //$NON-NLS-1$
    		if( i == 0 )
    			parse( writer.toString() );
    		else
    			parse( writer.toString(), false );
    	}
	}
    
    public void testBug74180() throws Exception
    {
        parse( "enum DHCPFOBoolean { false, true } additionalHB, more_payload; \n", true, ParserLanguage.C ); //$NON-NLS-1$
        assertTrue( callback.problems.isEmpty() );
    }
    
    public void testBug72691() throws Exception{
        StringWriter writer = new StringWriter();
        writer.write( "typedef int * PINT; \n" ); //$NON-NLS-1$
        writer.write( "typedef int * PINT; \n" ); //$NON-NLS-1$
        writer.write( "PINT pint;          \n" ); //$NON-NLS-1$
        Iterator i = parse( writer.toString() ).getDeclarations();
        assertTrue( i.next() instanceof IASTTypedefDeclaration );
        assertTrue( i.next() instanceof IASTTypedefDeclaration );
        assertTrue( i.next() instanceof IASTVariable);
        assertFalse( i.hasNext() );
        assertTrue( callback.problems.isEmpty() );
    }
    
    public void testBug72691_2() throws Exception{
        StringWriter writer = new StringWriter();
        writer.write( "typedef int * PINT;    \n" ); //$NON-NLS-1$
        writer.write( "namespace N {          \n" ); //$NON-NLS-1$
        writer.write( "   typedef int * PINT; \n" ); //$NON-NLS-1$
        writer.write( "}                      \n" ); //$NON-NLS-1$
        writer.write( "using namespace N;     \n" ); //$NON-NLS-1$
        writer.write( "PINT pint;             \n" ); //$NON-NLS-1$
        parse( writer.toString() );
    }
    
    public void testBug74328() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "int\n" );  //$NON-NLS-1$
    	writer.write( "main(int argc, char **argv) {\n" ); //$NON-NLS-1$
    	writer.write( "	char *sign;\n" ); //$NON-NLS-1$
    	writer.write( "sign = \"\"; // IProblem generated here, syntax error\n" ); //$NON-NLS-1$
    	writer.write( "return argc;\n" ); //$NON-NLS-1$
    	writer.write( "}\n" ); //$NON-NLS-1$
    	parse( writer.toString() );
	}
    
    public void testBug71733() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "void foo( int );\n"); //$NON-NLS-1$
    	writer.write( "#define BLAH() \\\n"); //$NON-NLS-1$
    	writer.write( "  foo ( /*  slash / is misinterpreted as end of comment */ \\\n"); //$NON-NLS-1$
    	writer.write( "    4 );\n"); //$NON-NLS-1$
    	writer.write( "int f() { BLAH() }\n"); //$NON-NLS-1$
    	parse( writer.toString() );
    	assertEquals( callback.getReferences().size(), 1 );
	}
    
    public void testBug69526() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "unsigned inkernel;\n" ); //$NON-NLS-1$
    	writer.write( "#define lock_kernel() (inkernel |= 0x01)" ); //$NON-NLS-1$
    	writer.write( "int main(int argc, char **argv) {" ); //$NON-NLS-1$
    	writer.write( "lock_kernel();" ); //$NON-NLS-1$
    	writer.write( "}" ); //$NON-NLS-1$
    	parse( writer.toString() );
	}
    
    public void testBug69454() throws Exception
    {
        Writer writer = new StringWriter();
        writer.write( "#define CATCH_ALL_EXCEPTIONS()                         \\\n" ); //$NON-NLS-1$
        writer.write( "   catch( Exception &ex ) { handleException( ex ); }   \\\n" ); //$NON-NLS-1$
        writer.write( "   catch( ... )           { handleException();    }      \n" ); //$NON-NLS-1$
        writer.write( "class Exception;                                         \n" ); //$NON-NLS-1$
        writer.write( "void handleException( Exception & ex ) {}                \n" ); //$NON-NLS-1$
        writer.write( "void handleException() {}                                \n" ); //$NON-NLS-1$
        writer.write( "void f() {                                               \n" ); //$NON-NLS-1$
        writer.write( "   try { int i; }                                        \n" ); //$NON-NLS-1$
        writer.write( "   CATCH_ALL_EXCEPTIONS();                               \n" ); //$NON-NLS-1$
        writer.write( "}                                                        \n" ); //$NON-NLS-1$

        parse( writer.toString() );
        assertFalse( callback.getProblems().hasNext() );
    }
    

    public void testBug72692A() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "extern double pow(double, double);\n"); //$NON-NLS-1$
    	writer.write( "extern double pow2(double, double){}\n"); //$NON-NLS-1$
    	writer.write( "namespace DS {\n"); //$NON-NLS-1$
    	writer.write( "using ::pow;\n"); //$NON-NLS-1$
    	writer.write( "using ::pow2;\n"); //$NON-NLS-1$
    	writer.write( "}\n"); //$NON-NLS-1$
    	writer.write( "using DS::pow;\n"); //$NON-NLS-1$
    	writer.write( "using DS::pow2;\n"); //$NON-NLS-1$
    	parse( writer.toString() );
	}
    
    public void testBug72692B() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "extern double pow(double, double);\n"); //$NON-NLS-1$
    	writer.write( "namespace DS {\n"); //$NON-NLS-1$
    	writer.write( "using ::pow;\n"); //$NON-NLS-1$
    	writer.write( "inline float pow(float __x, float __y)\n" ); //$NON-NLS-1$
    	writer.write( "{ return ::pow(static_cast<double>(__x), static_cast<double>(__y)); }\n" ); //$NON-NLS-1$
    	writer.write( "}\n"); //$NON-NLS-1$
    	writer.write( "using namespace DS;\n"); //$NON-NLS-1$
    	writer.write( "float foo() { double d1 = 3.0, d2 = 4.0; return pow(d1, d2); }"); //$NON-NLS-1$
    	parse( writer.toString() );
	}

    public void testBug72692C() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "extern double pow(double, double){}\n"); //$NON-NLS-1$
    	writer.write( "namespace DS {\n"); //$NON-NLS-1$
    	writer.write( "using ::pow;\n"); //$NON-NLS-1$
    	writer.write( "}\n"); //$NON-NLS-1$
    	writer.write( "using DS::pow;\n"); //$NON-NLS-1$
    	parse( writer.toString() );
	}

    
    public void testBug74575A() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "double pow(double, double);\n"); //$NON-NLS-1$
    	writer.write( "float pow(float __x, float __y)\n" ); //$NON-NLS-1$
    	writer.write( "{ return 0; }\n"); //$NON-NLS-1$
    	Iterator i = parse( writer.toString() ).getDeclarations();
    	IASTFunction doublePow = (IASTFunction) i.next();
    	IASTFunction floatPow = (IASTFunction) i.next();
    	assertFalse( i.hasNext() );
    	assertEquals( floatPow.getName(), "pow" ); //$NON-NLS-1$
    	assertEquals( doublePow.getName(), "pow"); //$NON-NLS-1$
    	assertEquals( ((IASTSimpleTypeSpecifier)doublePow.getReturnType().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.DOUBLE );
    	assertEquals( ((IASTSimpleTypeSpecifier)floatPow.getReturnType().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.FLOAT );
    	assertFalse( doublePow.hasFunctionBody() );
    	assertTrue( floatPow.hasFunctionBody() ); 
	}
    
    public void testBug75338() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "class Thrown { };\n"); //$NON-NLS-1$
    	writer.write( "void foo() throw( Thrown );"); //$NON-NLS-1$
    	Iterator i = (Iterator) parse( writer.toString() ).getDeclarations();
    	assertTrue( i.next() instanceof IASTAbstractTypeSpecifierDeclaration );
    	IASTFunction foo = (IASTFunction) i.next();
    	assertFalse( i.hasNext() );
    	IASTExceptionSpecification exSpec = foo.getExceptionSpec();
    	assertNotNull( exSpec );
    	Iterator typeIds = exSpec.getTypeIds();
    	assertTrue( typeIds.hasNext() );
    	IASTTypeId typeId = (IASTTypeId) typeIds.next();
    	assertEquals( typeId.getTypeOrClassName(), "Thrown" ); //$NON-NLS-1$
	}
    
    public void testBug75532() throws Exception
	{
    	try {
        	Writer writer = new StringWriter();
        	writer.write( "#if 2147483647 == 0x7fffffff\n"); // check reported hex problem //$NON-NLS-1$
        	writer.write( "#error This was equal, but not for the eclipse.\n"); //$NON-NLS-1$
        	writer.write( "#endif\n"); //$NON-NLS-1$
        	writer.write( "#if 010 == 8\n"); // check octal //$NON-NLS-1$
        	writer.write( "#error octal test\n"); //$NON-NLS-1$
        	writer.write( "#endif\n"); //$NON-NLS-1$
        	
        	parse( writer.toString() );

        	assertTrue(false);
    	} catch (ParserException pe) {
    		// expected IProblem
    	} finally {
        	Iterator probs = callback.getProblems();
    		assertTrue( probs.hasNext() );
        	Object ipo = probs.next();
        	assertTrue( ipo instanceof IProblem );
        	IProblem ip = (IProblem)ipo;
        	assertTrue(ip.getArguments().indexOf("This was equal, but not for the eclipse") >= 0); //$NON-NLS-1$
        	assertTrue( probs.hasNext() );
        	ipo = probs.next();
        	assertTrue( ipo instanceof IProblem );
        	ip = (IProblem)ipo;
        	assertTrue(ip.getArguments().indexOf("octal test") >= 0); //$NON-NLS-1$
    	}
	}
    
    public void testBug74847() throws Exception {
        String code = "class A : public FOO {};"; //$NON-NLS-1$
        Iterator i = parse( code, false ).getDeclarations();
        IASTClassSpecifier A = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
        assertFalse( A.getBaseClauses().hasNext() );
        i = callback.getProblems();
        IProblem problem = (IProblem) i.next();
        assertEquals( problem.getID(), IProblem.SEMANTIC_NAME_NOT_FOUND );
        assertEquals( problem.getSourceStart(), code.indexOf( "FOO" ) ); //$NON-NLS-1$
        assertFalse( i.hasNext() );
    }
    
    public void testBug76696() throws Exception{
        Writer writer = new StringWriter();
		writer.write(" void f(){       \n"); //$NON-NLS-1$
		writer.write("    if( A a) {   \n"); //$NON-NLS-1$
		writer.write("    } else {     \n"); //$NON-NLS-1$
		writer.write("    }	           \n"); //$NON-NLS-1$
		writer.write(" }               \n"); //$NON-NLS-1$
		
		parse( writer.toString(), false );
		Iterator i = callback.getProblems();
		IProblem problem = (IProblem) i.next();
		assertEquals( IProblem.SYNTAX_ERROR, problem.getID() );
		assertFalse( i.hasNext() );
    }
    
    public void testBug74069() throws Exception{
        Writer writer = new StringWriter();
        writer.write( "int f() {                \n"); //$NON-NLS-1$
        writer.write( "   int a, b, c;          \n"); //$NON-NLS-1$
        writer.write( "   if( a < b )           \n"); //$NON-NLS-1$
        writer.write( "      if( b < c )        \n"); //$NON-NLS-1$
        writer.write( "         return b;       \n"); //$NON-NLS-1$
        writer.write( "      else if ( a < c )  \n"); //$NON-NLS-1$
        writer.write( "         return c;       \n"); //$NON-NLS-1$
        writer.write( "      else               \n"); //$NON-NLS-1$
        writer.write( "         return a;       \n"); //$NON-NLS-1$
        writer.write( "   else if( a < c )      \n"); //$NON-NLS-1$
        writer.write( "      return a;          \n"); //$NON-NLS-1$
        writer.write( "   else if( b < c )      \n"); //$NON-NLS-1$
        writer.write( "      return c;          \n"); //$NON-NLS-1$
        writer.write( "   else                  \n"); //$NON-NLS-1$
        writer.write( "      return b;          \n"); //$NON-NLS-1$
        writer.write( "}                        \n"); //$NON-NLS-1$
        
        parse( writer.toString() );
        Iterator i = callback.getProblems();
        assertFalse( i.hasNext() );
    }
    public void testBug77805() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("#if X // Do something only if X is true\n"); //$NON-NLS-1$
    	writer.write("/* some statements */\n"); //$NON-NLS-1$
    	writer.write("#endif\n"); //$NON-NLS-1$
    	parse(writer.toString());
    }
    
    public void testBug77821() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("typedef struct { /* ... */ }TYPE;\n"); //$NON-NLS-1$
    	writer.write("void ptrArith(const TYPE* pType) {\n"); //$NON-NLS-1$
    	writer.write("TYPE *temp = 0;\n"); //$NON-NLS-1$
    	writer.write("temp = (TYPE*)(pType + 1); /* Parser error is here */\n}\n"); //$NON-NLS-1$
    	parse(writer.toString());
    }
    
    public void testBug77009() throws Exception
	{
		Iterator i = parse("int foo(volatile int &);\n").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction) i.next();
		Iterator parms = foo.getParameters();
	 	IASTParameterDeclaration blank = (IASTParameterDeclaration)parms.next();
		assertEquals( ASTUtil.getType( (IASTAbstractDeclaration)blank ), "volatile int&" ); //$NON-NLS-1$
	}
    
    
    public void testBug77281() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("void fun2(float a, float b) {}\n"); //$NON-NLS-1$
		writer.write("int main() { fun2(0.24f, 0.25f); }\n"); //$NON-NLS-1$
    	parse(writer.toString());
    }
    
    public void testBug77921() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("void f()\n{\n"); //$NON-NLS-1$
    	writer.write("static float v0[] = { -1.0f, -1.0f,  1.0f };\n}\n"); //$NON-NLS-1$
    	parse(writer.toString());
    }
    
    public void testBug76763() throws Exception 
	{
    	Writer writer = new StringWriter();
    	writer.write("#error oops!"); //$NON-NLS-1$
    	try {
    		parse(writer.toString());
    	} catch (ParserException pe) {
    		// expected IProblem
    	} finally {
	    	Iterator i = callback.getProblems();
	    	assertTrue( i.hasNext() );
	    	Object ipo = i.next();
	    	assertTrue( ipo instanceof IProblem );
	    	IProblem ip = (IProblem)ipo;
	    	assertTrue(new String(ip.getArguments()).equals("oops!")); //$NON-NLS-1$
	    	assertFalse( i.hasNext() );
    	}
	}
    public void testBug71317A() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("void f();\n"); //$NON-NLS-1$
	    writer.write("namespace NS {\n"); //$NON-NLS-1$
	    writer.write("using ::f;\n"); //$NON-NLS-1$
	    writer.write("using ::f;\n}"); //$NON-NLS-1$
	    parse(writer.toString());
    }

    public void testBug71317B() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("void f();\n"); //$NON-NLS-1$
	    writer.write("namespace NS {\n"); //$NON-NLS-1$
	    writer.write("void f();\n"); //$NON-NLS-1$
	    writer.write("using ::f;\n}"); //$NON-NLS-1$
	    
	    try{
	    	parse(writer.toString());
	    	assertTrue(false);
		} catch (ParserException pe) {
			// expected IProblem
		} finally {
	    	Iterator probs = callback.getProblems();
			assertTrue( probs.hasNext() );
	    	Object ipo = probs.next();
	    	assertTrue( ipo instanceof IProblem );
	    	IProblem ip = (IProblem)ipo;
	    	assertEquals(ip.getSourceLineNumber(), 4);
		}
    }
    
    public void testBug77097() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("#define SOME_MACRO() { \\\r\n"); //$NON-NLS-1$
    	writer.write("printf(\"Hello World\"); \\\r\n"); //$NON-NLS-1$
    	writer.write("printf(\"Good morning\"); \\\r\n"); //$NON-NLS-1$
    	parse(writer.toString());
    }
    
    public void testBug77276() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("#if (!defined(OS_LIBMODE_R) && !defined(OS_LIBMODE_RP) && \\\r\n"); //$NON-NLS-1$
    	writer.write("!defined(OS_LIBMODE_T))\r\n"); //$NON-NLS-1$
    	writer.write("#define OS_LIBMODE_DP\r\n"); //$NON-NLS-1$
    	writer.write("#endif\r\n"); //$NON-NLS-1$
    	parse(writer.toString());
    }
    
    public void testBug78165() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("struct Node {\n"); //$NON-NLS-1$
    	writer.write("struct Node* Next; // OK: Refers to Node at global scope\n"); //$NON-NLS-1$
    	writer.write("struct Data* Data; // OK: Declares type Data at global scope and member Data\n"); //$NON-NLS-1$
    	writer.write("};\n"); //$NON-NLS-1$
    	writer.write("struct Data {\n"); //$NON-NLS-1$
    	writer.write("struct Node* Node; // OK: Refers to Node at global scope\n"); //$NON-NLS-1$
    	writer.write("friend struct Glob; // OK: Refers to (as yet) undeclared Glob at global scope.\n"); //$NON-NLS-1$
    	writer.write("};\n"); //$NON-NLS-1$
    	writer.write("struct Base {\n"); //$NON-NLS-1$
    	writer.write("struct Data; // OK: Declares nested Data\n"); //$NON-NLS-1$
    	writer.write("struct ::Data* thatData; // OK: Refers to ::Data\n"); //$NON-NLS-1$
    	writer.write("struct Base::Data* thisData; // OK: Refers to nested Data\n"); //$NON-NLS-1$
    	writer.write("friend class ::Data; // OK: global Data is a friend\n"); //$NON-NLS-1$
    	writer.write("friend class Data; // OK: nested Data is a friend\n"); //$NON-NLS-1$
    	writer.write("struct Data { /* ... */ }; // Defines nested Data\n"); //$NON-NLS-1$
    	writer.write("struct Data; // OK: Redeclares nested Data\n"); //$NON-NLS-1$
    	writer.write("};\n"); //$NON-NLS-1$
    	writer.write("struct Data; // OK: Redeclares Data at global scope\n"); //$NON-NLS-1$
    	writer.write("struct Base::Data* pBase; // OK: refers to nested Data\n"); //$NON-NLS-1$

    	Iterator i = parse( writer.toString() ).getDeclarations();
    	IASTAbstractTypeSpecifierDeclaration node = (IASTAbstractTypeSpecifierDeclaration)i.next();
    	assertEquals(node.getStartingLine(), 1);
    	assertTrue(node.getTypeSpecifier() instanceof IASTClassSpecifier);
    	IASTClassSpecifier typeSpec = (IASTClassSpecifier)node.getTypeSpecifier();
    	assertEquals(typeSpec.getName(), "Node"); //$NON-NLS-1$
    	IASTAbstractTypeSpecifierDeclaration data = (IASTAbstractTypeSpecifierDeclaration)i.next();
    	assertEquals(data.getStartingLine(), 5);
    	assertTrue(data.getTypeSpecifier() instanceof IASTClassSpecifier);
    	typeSpec = (IASTClassSpecifier)data.getTypeSpecifier();
    	assertEquals(typeSpec.getName(), "Data"); //$NON-NLS-1$
    	IASTAbstractTypeSpecifierDeclaration base = (IASTAbstractTypeSpecifierDeclaration)i.next();
    	assertEquals(base.getStartingLine(), 9);
    	assertTrue(base.getTypeSpecifier() instanceof IASTClassSpecifier);
    	typeSpec = (IASTClassSpecifier)base.getTypeSpecifier();
    	assertEquals(typeSpec.getName(), "Base"); //$NON-NLS-1$
    	IASTAbstractTypeSpecifierDeclaration data2 = (IASTAbstractTypeSpecifierDeclaration)i.next();
    	assertEquals(data2.getStartingLine(), 18);
    	assertTrue(data2.getTypeSpecifier() instanceof IASTElaboratedTypeSpecifier);
    	IASTElaboratedTypeSpecifier typeSpec2 = (IASTElaboratedTypeSpecifier)data2.getTypeSpecifier();
    	assertEquals(typeSpec2.getName(), "Data"); //$NON-NLS-1$
    	IASTVariable pBase = (IASTVariable)i.next();
    	assertEquals(pBase.getStartingLine(), 19);
    }
    
    public void testBug79471() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("void testFloatAccess(float * fp) {\n"); //$NON-NLS-1$
    	writer.write("#define VAL 2.0f\n"); //$NON-NLS-1$
    	writer.write("if(*fp > VAL) { /* Syntax error is here */\n}\n}\n"); //$NON-NLS-1$
    	parse(writer.toString());
    	writer = new StringWriter();
    	writer.write("void testFloatAccess(float * fp) {\n"); //$NON-NLS-1$
    	writer.write("#define VAL 2.0l\n"); //$NON-NLS-1$
    	writer.write("if(*fp > VAL) { /* Syntax error is here */\n}\n}\n"); //$NON-NLS-1$
    	parse(writer.toString());
    	writer = new StringWriter();
    	writer.write("void testFloatAccess(float * fp) {\n"); //$NON-NLS-1$
    	writer.write("#define VAL 2.0f\n"); //$NON-NLS-1$
    	writer.write("if(VAL > VAL) { /* Syntax error is here */\n}\n}\n"); //$NON-NLS-1$
    	parse(writer.toString());
    }
    
    public void testBug79787() throws Exception {
		try {
			parse("#error what?\r\n");
		} catch (ParserException pe) {
			// expected IProblem
		} finally {
			Iterator probs = callback.getProblems();
			assertTrue(probs.hasNext());
			Object ipo = probs.next();
			assertTrue(ipo instanceof IProblem);
			IProblem ip = (IProblem) ipo;
			assertEquals(ip.getArguments(), "what?");
		}
	}
    	
}

