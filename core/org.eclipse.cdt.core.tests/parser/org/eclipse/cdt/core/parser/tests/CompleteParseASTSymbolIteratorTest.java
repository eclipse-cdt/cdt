/*
 * Created on Feb 25, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.core.parser.tests;

import java.io.StringReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CompleteParseASTSymbolIteratorTest extends CompleteParseBaseTest {
    public CompleteParseASTSymbolIteratorTest(String a)
    {
        super(a);
    }
    
    public static class CompilationUnitCallback extends NullSourceElementRequestor implements ISourceElementRequestor {
    	IASTCompilationUnit compilationUnit;
    	
        public void enterCompilationUnit(IASTCompilationUnit compUnit)
	    {
        	compilationUnit = compUnit;
	    }
        
        public IASTCompilationUnit getCompilationUnit(){
        	return compilationUnit;
        }
    }
    
    protected CompilationUnitCallback callback;
    
    protected IASTScope parse(String code, boolean throwOnError, ParserLanguage language) throws ParserException, ParserFactoryError
    {
    	callback = new CompilationUnitCallback(); 
    	IParser parser = ParserFactory.createParser( 
    		ParserFactory.createScanner( new StringReader( code ), "test-code", new ScannerInfo(),
    			ParserMode.COMPLETE_PARSE, language, callback, new NullLogService(), null ), callback, ParserMode.COMPLETE_PARSE, language, null 	
    		);
    	if( ! parser.parse() && throwOnError ) throw new ParserException( "FAILURE");
    	
        return callback.getCompilationUnit();
    }
    
    protected Iterator getDeclarations(IASTScope scope)
    {
    	//don't want to use this
    	assertTrue( false );
    	return null;
    }
    
    public void testEmptyCompilationUnit() throws Exception
    {
    	IASTScope compilationUnit = parse( "// no real code ");

    	assertNotNull( compilationUnit );
    	assertFalse( compilationUnit.getDeclarations().hasNext() );
    	try{
    		compilationUnit.getDeclarations().next();
    		assertTrue( false );
    	} catch( NoSuchElementException e ){
    		
    	}
    }
    
    public void testSimpleNamespace() throws Exception
    {
    	Iterator declarations = parse( "namespace A { }").getDeclarations();
    	
    	IASTNamespaceDefinition namespaceDefinition = (IASTNamespaceDefinition)declarations.next(); 
    	assertEquals( namespaceDefinition.getName(), "A" ); 
    	assertFalse( namespaceDefinition.getDeclarations().hasNext() );
    	
    	try{
    		declarations.remove();
    		assertTrue( false );
    	} catch( UnsupportedOperationException e ){
    	}
    }

	public void testMultipleNamespaceDefinitions() throws Exception
	{
		Iterator declarations = parse( "namespace A { } namespace A { }").getDeclarations();
		
		IASTNamespaceDefinition namespaceDefinition = (IASTNamespaceDefinition)declarations.next(); 
		assertEquals( namespaceDefinition.getName(), "A" );
		assertFalse( declarations.hasNext() );
	}

    public void testNestedNamespaceDefinitions() throws Exception
    {
		Iterator declarations = parse( "namespace A { namespace B { } }").getDeclarations();
		
		IASTNamespaceDefinition namespaceDefinition = (IASTNamespaceDefinition)declarations.next(); 
		assertEquals( namespaceDefinition.getName(), "A" );
		assertFalse( declarations.hasNext() );
		
		Iterator subDeclarations = namespaceDefinition.getDeclarations();
		IASTNamespaceDefinition subDeclaration = (IASTNamespaceDefinition)subDeclarations.next();
		assertEquals( subDeclaration.getName(), "B" );
		assertFalse( subDeclarations.hasNext() );
    }
    
    public void testEmptyClassDeclaration() throws Exception
    {
    	Iterator declarations = parse( "class A { };").getDeclarations();
    	
    	IASTClassSpecifier classSpec = (IASTClassSpecifier)declarations.next();
    	assertEquals( classSpec.getName(), "A");
    	assertFalse( classSpec.getDeclarations().hasNext() ); 
    	assertFalse( declarations.hasNext() );
    }
    
    public void testNestedSubclass() throws Exception
    {
    	Iterator declarations = parse( "namespace N { class A { }; } class B : protected virtual N::A { };").getDeclarations();
    	
    	IASTNamespaceDefinition namespaceDefinition = (IASTNamespaceDefinition)declarations.next();
    	
    	Iterator nsDecls = namespaceDefinition.getDeclarations();
    	IASTClassSpecifier classA = (IASTClassSpecifier)nsDecls.next();
    	assertFalse( nsDecls.hasNext() );
    	
		IASTClassSpecifier classB = (IASTClassSpecifier)declarations.next();
		
		Iterator baseClauses = classB.getBaseClauses();
		IASTBaseSpecifier baseClass = (IASTBaseSpecifier)baseClauses.next();
		assertEquals( classA, baseClass.getParentClassSpecifier() );
    }
    
    public void testSimpleVariable() throws Exception
    {
    	Iterator declarations = parse( "int x;").getDeclarations();
    	IASTVariable v = (IASTVariable)declarations.next();
    	assertEquals( v.getName(), "x");
    	assertFalse( declarations.hasNext() );
    }
    
	public void testSimpleClassReferenceVariable() throws Exception
	{
		Iterator declarations = parse( "class A { } a; A x;").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)declarations.next();
		assertFalse( classA.getDeclarations().hasNext() );
		
		IASTVariable a = (IASTVariable)declarations.next();
		assertEquals( a.getName(), "a");
		
		IASTVariable v = (IASTVariable)declarations.next();
		assertEquals( v.getName(), "x");
		assertEquals( ((IASTSimpleTypeSpecifier)v.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), classA );
		assertFalse( declarations.hasNext() );
	}
 
	public void testMultipleDeclaratorsVariable() throws Exception
	{
		Iterator declarations = parse( "class A { }; A x, y, z;").getDeclarations();
		
		IASTClassSpecifier classA = (IASTClassSpecifier)declarations.next();
		
		IASTVariable v = (IASTVariable)declarations.next();
		assertEquals( v.getName(), "x");
		
		v = (IASTVariable)declarations.next();
		assertEquals( v.getName(), "y");
		
		v = (IASTVariable)declarations.next();
		assertEquals( v.getName(), "z");
		
		assertEquals( ((IASTSimpleTypeSpecifier)v.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), classA );
		
		assertFalse( declarations.hasNext() );
	}
	
	public void testSimpleField() throws Exception
	{
		Iterator declarations = parse( "class A { double x; };").getDeclarations();
		
		IASTClassSpecifier classA = (IASTClassSpecifier)declarations.next();
		
		Iterator fields = classA.getDeclarations();
		
		IASTField f = (IASTField)fields.next();
		
		assertEquals( f.getName(), "x" );
		
		assertFalse( fields.hasNext() );
		assertFalse( declarations.hasNext() );
	}
	

	
	public void testSimpleFunction() throws Exception
	{
		Iterator declarations = parse( "void foo( void );").getDeclarations();
		IASTFunction function = (IASTFunction)declarations.next();
		assertEquals( function.getName(), "foo" );
		assertFalse( declarations.hasNext() );
	}
	
	public void testSimpleMethod() throws Exception
	{
		Iterator declarations = parse( "class A { void foo(); };").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)declarations.next();
		
		IASTMethod method = (IASTMethod) classA.getDeclarations().next();
		assertEquals( method.getName(), "foo" );
	}
	
	public void testLinkageSpec() throws Exception
	{
		Iterator declarations = parse( "extern \"C\" { int foo(); }").getDeclarations();

		//7.5-4 A linkage specification does not establish a scope
		IASTFunction f = (IASTFunction)declarations.next();
		assertEquals( f.getName(),"foo");
		assertFalse( declarations.hasNext() );
	}
	
	public void testSimpleTypedef() throws Exception
	{
		Iterator iter = parse( "typedef int myInt;\n myInt var;").getDeclarations();
		
		IASTTypedefDeclaration typedef = (IASTTypedefDeclaration)iter.next();
		
		assertEquals( typedef.getName(), "myInt");
		assertEquals( ((IASTSimpleTypeSpecifier)typedef.getAbstractDeclarator().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT );
		
		IASTVariable v = (IASTVariable)iter.next();
		assertEquals( v.getName(), "var");
		
		assertFalse( iter.hasNext() );
		 
	}
	
	public void testOverride() throws Exception
	{
		Iterator i = parse( "void foo();\n void foo( int );\n").getDeclarations();
		IASTFunction f1 = (IASTFunction)i.next();
		IASTFunction f2 = (IASTFunction)i.next();
		assertFalse( i.hasNext() );
	}	
	
	public void testEnumerations() throws Exception
	{
		Iterator declarations = parse( "namespace A { enum E { e1, e2, e3 }; E varE;}").getDeclarations();
		
		IASTNamespaceDefinition namespaceA = (IASTNamespaceDefinition)declarations.next(); 
		
		Iterator namespaceMembers = namespaceA.getDeclarations(); 
		
		IASTEnumerationSpecifier enumE = (IASTEnumerationSpecifier)namespaceMembers.next();
		
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
		
		assertFalse( namespaceMembers.hasNext() );
		assertFalse( declarations.hasNext() );
	}
	
	public void testMethodDefinitions() throws Exception
	{
		Iterator i = parse( " class A { void f(); };  void A::f(){ }" ).getDeclarations();
		
		IASTClassSpecifier classA = (IASTClassSpecifier) i.next();
		assertFalse( i.hasNext() );
		
		i = classA.getDeclarations();
		
		IASTMethod f = (IASTMethod)i.next();
		
		assertFalse( i.hasNext() ); 
	}
	
	public void testConstructorsDestructors() throws Exception
	{
		Iterator i = parse( "class A { A();  ~A();  };  A::A(){}  A::~A(){}" ).getDeclarations();
		
		IASTClassSpecifier classA = (IASTClassSpecifier) i.next();
		
		assertFalse( i.hasNext() );
		
		i = classA.getDeclarations();
		assertTrue( i.hasNext() );
		
		IASTMethod constructor = (IASTMethod) i.next();
		assertTrue( constructor.getName().equals( "A" ) );
		IASTMethod destructor = (IASTMethod) i.next();
		assertTrue( destructor.getName().equals( "~A" ) );
		
		assertFalse( i.hasNext() );
	}
	
}
