/*
 * Created on Mar 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.core.parser.tests;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author aniefer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CompleteParseASTTemplateTest extends CompleteParseBaseTest {
	/**
	 * @param name
	 */
	public CompleteParseASTTemplateTest(String name) {
		super(name);
	}
	
	public void testTemplateClassDeclaration() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template < class T > class A {  T t;  }; " );
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		Iterator params = template.getTemplateParameters();
		
		IASTTemplateParameter T = (IASTTemplateParameter) params.next();
		assertEquals( T.getIdentifier(), "T" );
		assertFalse( params.hasNext() );
		assertFalse( i.hasNext() );
		
		i = getDeclarations( template );

		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		assertEquals( classA.getName(), "A" );
		
		assertFalse( i.hasNext() );
		
		i = getDeclarations( classA );
		
		IASTField t = (IASTField) i.next();
		assertEquals( t.getName(), "t" );

		IASTSimpleTypeSpecifier specifier = (IASTSimpleTypeSpecifier) t.getAbstractDeclaration().getTypeSpecifier();
		assertEquals( specifier.getTypename(), "T" );
		//assertEquals( specifier.getTypeSpecifier(), T ); //TODO uncomment when bug 54029 is fixed
	}
	
	public void testTemplateFunction() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template < class T > void f( T t ){} " );
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		
		Iterator params = template.getTemplateParameters();
		
		IASTTemplateParameter T = (IASTTemplateParameter) params.next();
		assertEquals( T.getIdentifier(), "T" );
		assertFalse( params.hasNext() );
		assertFalse( i.hasNext() );
		
		i = getDeclarations( template );
		IASTFunction f = (IASTFunction) i.next();
		assertEquals( f.getName(), "f" );
		
		params = f.getParameters();
		IASTParameterDeclaration t = (IASTParameterDeclaration) params.next();
		assertEquals( t.getName(), "t" );
		IASTSimpleTypeSpecifier typeSpec = (IASTSimpleTypeSpecifier) t.getTypeSpecifier();
		assertEquals( typeSpec.getTypename(), "T" );
		//assertEquals( typeSpec.getTypeSpecifier(), T );  //TODO uncomment when bug 54029 is fixed
	}
	
	public void testTemplateFunctionDefinition() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template <class T> void f( T t );" );
		writer.write( "template <class U> void f( U u ) { }" );
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		
		Iterator params = template.getTemplateParameters();
		
		IASTTemplateParameter T = (IASTTemplateParameter) params.next();
		assertEquals( T.getIdentifier(), "T" );
		assertFalse( params.hasNext() );
		
		Iterator tempDecls = getDeclarations( template );
		IASTFunction f = (IASTFunction) tempDecls.next();
		assertEquals( f.getName(), "f" );
		assertFalse( f.hasFunctionBody() );
		assertFalse( tempDecls.hasNext() );
		
		params = f.getParameters();
		IASTParameterDeclaration t = (IASTParameterDeclaration) params.next();
		assertEquals( t.getName(), "t" );
		IASTSimpleTypeSpecifier typeSpec = (IASTSimpleTypeSpecifier) t.getTypeSpecifier();
		assertEquals( typeSpec.getTypename(), "T" );
		//assertEquals( typeSpec.getTypeSpecifier(), T );  //TODO uncomment when bug 54029 is fixed
		
		IASTTemplateDeclaration template2 = (IASTTemplateDeclaration) i.next();
		
		params = template2.getTemplateParameters();
		
		IASTTemplateParameter U = (IASTTemplateParameter) params.next();
		assertEquals( U.getIdentifier(), "U" );
		assertFalse( params.hasNext() );
		
		tempDecls = getDeclarations( template2 );
		IASTFunction f2 = (IASTFunction) tempDecls.next();
		assertEquals( f2.getName(), "f" );
		assertTrue( f2.previouslyDeclared() );
		
		params = f2.getParameters();
		IASTParameterDeclaration u = (IASTParameterDeclaration) params.next();
		assertEquals( u.getName(), "u" );
		typeSpec = (IASTSimpleTypeSpecifier) u.getTypeSpecifier();
		assertEquals( typeSpec.getTypename(), "U" );
		//assertEquals( typeSpec.getTypeSpecifier(), U );  //TODO uncomment when bug 54029 is fixed
		
		assertFalse( i.hasNext() );
	}
	
	public void testClassMemberTemplate() throws Exception{
		Writer writer = new StringWriter();
		writer.write( "namespace N { " );
		writer.write( "   class A { " );
		writer.write( "      template < class T > T f();" );
		writer.write( "   }; " );
		writer.write( "}" );
		writer.write( "template <class U> U N::A::f() {} " );
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTNamespaceDefinition N = (IASTNamespaceDefinition) i.next();
		
		Iterator i2 = getDeclarations( N );
		
		IASTClassSpecifier A = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i2.next()).getTypeSpecifier();
		assertFalse( i2.hasNext() );
		
		i2 = getDeclarations( A );
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i2.next();
		Iterator params = template.getTemplateParameters();
		IASTTemplateParameter T = (IASTTemplateParameter) params.next();
		assertFalse( params.hasNext() );
		assertFalse( i2.hasNext() );
		
		i2 = getDeclarations( template );
		
		IASTMethod f = (IASTMethod) i2.next();
		assertEquals( ((IASTSimpleTypeSpecifier)f.getReturnType().getTypeSpecifier()).getTypename(), "T" );
		assertFalse( i2.hasNext() );
		
		IASTTemplateDeclaration template2 = (IASTTemplateDeclaration) i.next();
		params = template.getTemplateParameters();
		IASTTemplateParameter U = (IASTTemplateParameter) params.next();
		assertFalse( params.hasNext() );
		assertFalse( i.hasNext() );
		
		i2 = getDeclarations( template2 );
		
		IASTMethod f2 = (IASTMethod) i2.next();
		assertEquals( ((IASTSimpleTypeSpecifier)f2.getReturnType().getTypeSpecifier()).getTypename(), "U" );
		assertQualifiedName( f2.getFullyQualifiedName(), new String [] { "N", "A", "f" } );
		assertTrue( f2.previouslyDeclared() );
		assertFalse( i2.hasNext() );
	}
	
	public void testOverloadedFunctionTemplates() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( " template < class T > void f ( T )   {} " );
		writer.write( " template < class T > void f ( T * ) {} " );
		writer.write( " int * p;" );
		writer.write( " void main () {" );
		writer.write( "    f( p );" );
		writer.write( "    f( *p );" );
		writer.write( " }" );
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template1 = (IASTTemplateDeclaration) i.next();
		IASTTemplateParameter T1 = (IASTTemplateParameter) template1.getTemplateParameters().next();
		
		IASTFunction f1 = (IASTFunction) template1.getOwnedDeclaration();
		
		IASTTemplateDeclaration template2 = (IASTTemplateDeclaration) i.next();
		IASTFunction f2 = (IASTFunction) template2.getOwnedDeclaration();
		IASTTemplateParameter T2 = (IASTTemplateParameter) template2.getTemplateParameters().next();
		
		IASTVariable p = (IASTVariable) i.next();
		IASTFunction main = (IASTFunction) i.next();
		assertFalse( i.hasNext() );
		
		assertAllReferences( 6, createTaskList( new Task( T1 ), 
											    new Task( T2 ), 
												new Task( f1, 1, false, false ), 
												new Task( p, 2 ), 
												new Task( f2, 1, false, false ) ) );
		
	}
	
	public void testOverloadedFunctionTemplates_2() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("template< class T > struct A {  };                 \n");
		writer.write("template< class T > void h( const T & );	//#1     \n");
		writer.write("template< class T > void h( A<T>& );		//#2     \n");
		writer.write("void foo() {                                       \n");
		writer.write("   A<int> z;                                       \n");
		writer.write("   h( z );  //calls 2                              \n");
		
		writer.write("   const A<int> z2;                                \n");
		writer.write("   h( z2 ); //calls 1 because 2 is not callable.   \n");
		writer.write( "}                                                 \n");
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration templateA = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration templateh1 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration templateh2 = (IASTTemplateDeclaration) i.next();
		
		IASTClassSpecifier A = (IASTClassSpecifier) templateA.getOwnedDeclaration();
		IASTFunction h1 = (IASTFunction) templateh1.getOwnedDeclaration();
		IASTFunction h2 = (IASTFunction) templateh2.getOwnedDeclaration();
		
		IASTTemplateParameter T1 = (IASTTemplateParameter) templateA.getTemplateParameters().next();
		IASTTemplateParameter T2 = (IASTTemplateParameter) templateh1.getTemplateParameters().next();
		IASTTemplateParameter T3 = (IASTTemplateParameter) templateh2.getTemplateParameters().next();
		
		IASTFunction foo = (IASTFunction) i.next();
		assertFalse( i.hasNext() );
		
		i = getDeclarations( foo );
		IASTVariable z = (IASTVariable) i.next();
		IASTVariable z2 = (IASTVariable) i.next();
		assertFalse( i.hasNext() );
		
		assertEquals( ((IASTSimpleTypeSpecifier)z.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), A );
		assertEquals( ((IASTSimpleTypeSpecifier)z2.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), A );
		
		assertAllReferences( 9, createTaskList( new Task( T2 ), 
											    new Task( T3 ), 
												new Task( A, 3 ), 
												new Task( z ), 
												new Task( z2 ),
												new Task( h1, 1, false, false ), 	
												new Task( h2, 1, false, false ) ) );
		
		
	}
	
	public void testTemplateClassPartialSpecialization() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template < class T1, class T2, int I > class A {};  //#1\n" );
		writer.write( "template < class T, int I >            class A < T, T*, I >   {};  //#2\n");
		writer.write( "template < class T1, class T2, int I > class A < T1*, T2, I > {};  //#3\n");
		writer.write( "template < class T >                   class A < int, T*, 5 > {};  //#4\n");
		writer.write( "template < class T1, class T2, int I > class A < T1, T2*, I > {};  //#5\n");

		writer.write( "A <int, int, 1>   a1;		//uses #1 \n");
		writer.write( "A <int, int*, 1>  a2;		//uses #2, T is int, I is 1 \n");
		writer.write( "A <int, char*, 5> a4;		//uses #4, T is char \n");
		writer.write( "A <int, char*, 1> a5;		//uses #5, T is int, T2 is char, I is1 \n");

		Iterator i = parse( writer.toString() ).getDeclarations();
		
		writer.write( "  A <int*, int*, 2> amgiguous; //ambiguous, matches #3 & #5 \n");
		
		try{
			//we expect this parse to fail because of the ambiguity in the last line
			parse( writer.toString() );
			assertFalse( true );
		} catch ( ParserException e ){
			assertEquals( e.getMessage(), "FAILURE" );
		}
	 
		IASTTemplateDeclaration template1 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration spec2 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration spec3 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration spec4 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration spec5 = (IASTTemplateDeclaration) i.next();
		
		IASTVariable a1 = (IASTVariable) i.next();
		IASTVariable a2 = (IASTVariable) i.next();
		IASTVariable a4 = (IASTVariable) i.next();
		IASTVariable a5 = (IASTVariable) i.next();
		
		assertFalse( i.hasNext() );
		
		IASTClassSpecifier A1 = (IASTClassSpecifier)template1.getOwnedDeclaration();
		IASTClassSpecifier A2 = (IASTClassSpecifier)spec2.getOwnedDeclaration();
		IASTClassSpecifier A3 = (IASTClassSpecifier)spec3.getOwnedDeclaration();
		IASTClassSpecifier A4 = (IASTClassSpecifier)spec4.getOwnedDeclaration();
		IASTClassSpecifier A5 = (IASTClassSpecifier)spec5.getOwnedDeclaration();
		
		assertEquals( ((IASTSimpleTypeSpecifier)a1.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), A1 );
		assertEquals( ((IASTSimpleTypeSpecifier)a2.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), A2 );
		assertEquals( ((IASTSimpleTypeSpecifier)a4.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), A4 );
		assertEquals( ((IASTSimpleTypeSpecifier)a5.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), A5 );
		
	}
	
	public void testTemplateInstanceAsBaseClause() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template< class T > class A { T t; };  \n" );
		writer.write( "class B : public A< int > {};          \n" );
		writer.write( "void f( int );                         \n" );
		
		writer.write( "void main(){                           \n" );
		writer.write( "   B b;                                \n" );
		writer.write( "   f( b.t );                           \n" );  //if this function call is good, it implies that b.t is type int
		writer.write( "}                                      \n" );
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTTemplateParameter T = (IASTTemplateParameter) template.getTemplateParameters().next();
		IASTClassSpecifier B = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTFunction f = (IASTFunction) i.next();
		IASTFunction main = (IASTFunction) i.next();
		assertFalse( i.hasNext() );
		
		IASTClassSpecifier A = (IASTClassSpecifier) template.getOwnedDeclaration();
		i = getDeclarations( A );
		IASTField t = (IASTField) i.next();
		assertFalse( i.hasNext() );
		
		i = getDeclarations( main );
		
		IASTVariable b = (IASTVariable) i.next();
		assertFalse( i.hasNext() );
		
		assertAllReferences( 6, createTaskList( new Task( T ), 
											    new Task( A ), 
												new Task( B ), 
												new Task( b ),
												new Task( t ), 	
												new Task( f ) ) );
	}
	
	public void testTemplateParameterAsBaseClause() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template < class T > class A : public T {};  \n" );
		writer.write( "class B { int i; };                           \n" );
		writer.write( "void main() {                                \n" );
		writer.write( "   A<B> a;                                   \n" );
		writer.write( "   a.i;                                      \n" );
		writer.write( "}                                            \n" );
		writer.write( "\n" );
		
		Iterator iter = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) iter.next();
		IASTTemplateParameter T = (IASTTemplateParameter) template.getTemplateParameters().next();
		IASTClassSpecifier B = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)iter.next()).getTypeSpecifier();
		IASTFunction main = (IASTFunction) iter.next();
		assertFalse( iter.hasNext() );

		IASTClassSpecifier A = (IASTClassSpecifier) template.getOwnedDeclaration();
		
		iter = getDeclarations( B );
		IASTVariable i = (IASTVariable) iter.next();
		
		iter = getDeclarations( main );
		IASTVariable a = (IASTVariable) iter.next();
		
		assertAllReferences( 5, createTaskList( new Task( T ), new Task( A ), new Task( B ), new Task( a ), new Task( i ) ) ); 	
	}

	public void testTypedefedTemplate() throws Exception{
		Writer writer = new StringWriter();
		writer.write( "template < class T > class _A{ int x; }; \n" );
		writer.write( "typedef _A < char >  A;                  \n" );
		writer.write( "void foo() {                             \n" );
		writer.write( "   A a;                                  \n" );
		writer.write( "   a.x;                                  \n" );
		writer.write( "}                                        \n" );
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration _A = (IASTTemplateDeclaration) i.next();
		IASTTypedefDeclaration A = (IASTTypedefDeclaration) i.next();
		IASTFunction foo = (IASTFunction) i.next();
		
		IASTClassSpecifier classA = (IASTClassSpecifier) _A.getOwnedDeclaration();
		IASTVariable x = (IASTVariable) getDeclarations( classA ).next();
		IASTVariable a = (IASTVariable) getDeclarations( foo ).next();
		
		assertAllReferences( 4, createTaskList( new Task( classA ), new Task( A ), new Task( a ), new Task( x ) ) );
	}
	
	public void testTypedefedTemplate_2() throws Exception{
		Writer writer = new StringWriter();
		writer.write( "template < class T > struct A { T x; };      \n" );
		writer.write( "template < class U > struct B {              \n" );
		writer.write( "   typedef A< U > AU;                        \n" );
		writer.write( "   void f( U );                              \n" );
		writer.write( "   void f( char );                           \n" );
		writer.write( "   void g(){                                 \n" );
		writer.write( "      AU au;                                 \n" );
		writer.write( "      f( au.x );                             \n" );
		writer.write( "   }                                         \n" );
		writer.write( "};                                           \n" );
		writer.write( "void f2( int );                              \n" );
		writer.write( "void f2( char );                             \n" );
		writer.write( "void h(){                                    \n" );
		writer.write( "   B< int >::AU b;                           \n" );
		writer.write( "   f2( b.x );                                \n" );
		writer.write( "}                                            \n" );
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration tA = (IASTTemplateDeclaration) i.next();
		IASTTemplateParameter T = (IASTTemplateParameter) tA.getTemplateParameters().next();
		IASTClassSpecifier A = (IASTClassSpecifier) tA.getOwnedDeclaration();
		IASTField x = (IASTField) getDeclarations( A ).next();
		IASTTemplateDeclaration tB = (IASTTemplateDeclaration) i.next();
		IASTClassSpecifier B = (IASTClassSpecifier) tB.getOwnedDeclaration();
		IASTTemplateParameter U = (IASTTemplateParameter) tB.getTemplateParameters().next();
		IASTFunction f21 = (IASTFunction) i.next();
		IASTFunction f22 = (IASTFunction) i.next();
		IASTFunction h = (IASTFunction) i.next();
		
		i = getDeclarations( B );
		IASTTypedefDeclaration AU = (IASTTypedefDeclaration) i.next(); 
		IASTMethod f11 = (IASTMethod) i.next();
		IASTMethod f12 = (IASTMethod) i.next();
		IASTMethod g = (IASTMethod) i.next();
		
		IASTVariable au = (IASTVariable) getDeclarations( g ).next();
		IASTVariable b = (IASTVariable) getDeclarations( h ).next();
		
		assertAllReferences( 13, createTaskList( new Task( A ),
												 new Task( T ),
												 new Task( U, 2 ),
				                                 new Task( AU, 2 ),
								  			     new Task( au ),
								  			     new Task( x, 2 ),
											     new Task( f11, 1, false, false ),
											     new Task( B ),
											     new Task( b ),
											     new Task( f21, 1, false, false ) ) );
	}
	
	public void testInstantiatingDeferredInstances() throws Exception{
		Writer writer = new StringWriter();
		writer.write( "template < class T > struct A { A < T > next; };  \n" );
		writer.write( "A< int > a; \n" );
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTTemplateParameter T = (IASTTemplateParameter) template.getTemplateParameters().next();
		IASTClassSpecifier A = (IASTClassSpecifier) template.getOwnedDeclaration();
		IASTField next = (IASTField) getDeclarations( A ).next();
		IASTVariable a = (IASTVariable) i.next();
		
		assertAllReferences( 3, createTaskList( new Task( A, 2 ), new Task( T ) ) );
	}
	
	public void testTemplateArgumentDeduction() throws Exception{
		Writer writer = new StringWriter();
		writer.write( "template< class T > struct B {};                   \n" );
		writer.write( "template< class T > struct D : public B < T > {};  \n" );
		writer.write( "struct D2 : public B< int > {};                    \n" );
		writer.write( "template< class T > T f( B<T> & ) {}               \n" );
		writer.write( "void test( int );                                  \n" );
		writer.write( "void test( char );                                 \n" );
		writer.write( "void main() {                                      \n" );
		writer.write( "   D<int> d;                                       \n" );
		writer.write( "   D2     d2;                                      \n" );
		writer.write( "   test( f( d ) );                                 \n" );
		writer.write( "   test( f( d2 ) );                                \n" );
		writer.write( "}                                                  \n" );
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration templateB = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration templateD = (IASTTemplateDeclaration) i.next();
		IASTClassSpecifier D2 = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTTemplateDeclaration templateF = (IASTTemplateDeclaration) i.next();
		IASTFunction test1 = (IASTFunction) i.next();
		IASTFunction test2 = (IASTFunction) i.next();
		IASTFunction main = (IASTFunction) i.next();
		
		assertFalse( i.hasNext() );		
		assertReferenceTask( new Task( test1, 2, false, false ) );
	}
	public void testClassTemplateStaticMemberDefinition() throws Exception {
		Writer writer = new StringWriter();
		writer.write( "template< class T > class A{                      \n" );
		writer.write( "   typedef T * PT;                                \n" );
		writer.write( "   static T member;                               \n" );
		writer.write( "};                                                \n" );
		writer.write( "template< class T> A<T>::PT A<T>::member = null;  \n" );
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTTemplateParameter T1 = (IASTTemplateParameter) template.getTemplateParameters().next();
		IASTTemplateDeclaration template2 = (IASTTemplateDeclaration) i.next();
		IASTTemplateParameter T2 = (IASTTemplateParameter) template2.getTemplateParameters().next();
		
		IASTField member = (IASTField) getDeclarations( template2 ).next();
		assertEquals( member.getName(), "member" );
		
		assertReferenceTask( new Task( T1, 2, false, false ) );
		assertReferenceTask( new Task( T2, 2, false, false ) );
	}
	
	public void testTemplateTemplateParameter() throws Exception{
		Writer writer = new StringWriter();
		writer.write( " template< class T > class A {                    ");
		writer.write( "    int x;                                        ");
		writer.write( " };                                               ");
		writer.write( " template < class T > class A < T * > {           ");
		writer.write( "    long x;                                       ");
		writer.write( " };                                               ");
		writer.write( " template< template< class U > class V > class C{ ");
		writer.write( "    V< int > y;                                   ");
		writer.write( "    V< int * > z;                                 ");
		writer.write( " };                                               ");
		writer.write( " void f( int );                                   ");
		writer.write( " void f( long );                                  ");
		writer.write( " void main() {                                    ");
		writer.write( "    C< A > c;                                     ");
		writer.write( "    f( c.y.x );                                   ");
		writer.write( "    f( c.z.x );                                   ");
		writer.write( " }                                                ");
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration templateA = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration templateA2 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration templateC = (IASTTemplateDeclaration) i.next();
		
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		
		IASTFunction main = (IASTFunction) i.next();
		IASTVariable c = (IASTVariable) getDeclarations( main ).next();
		
		IASTSimpleTypeSpecifier spec = (IASTSimpleTypeSpecifier) c.getAbstractDeclaration().getTypeSpecifier();
		IASTClassSpecifier C = (IASTClassSpecifier) spec.getTypeSpecifier();
		
		assertReferenceTask( new Task( f1, 1, false, false ) );
		assertReferenceTask( new Task( f2, 1, false, false ) );
	}
	
	public void testBug56834() throws Exception{
		Iterator i = parse( "template < class T, class U = T > class A;" ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		
		assertFalse( i.hasNext() );
		
		i = template.getTemplateParameters();
		
		IASTTemplateParameter T = (IASTTemplateParameter) i.next();
		IASTTemplateParameter U = (IASTTemplateParameter) i.next();
	}
	
	public void testDefaultTemplateParameters() throws Exception {
		Iterator i = parse( "template < class T = int > class A{};  A<> a;" ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTVariable a = (IASTVariable) i.next();
	}
	
	public void testBug56834WithInstantiation() throws Exception{
		Writer writer = new StringWriter();
		writer.write( "template< class T, class U = T > class A {};" );
		writer.write( "A< char > a;" );
		Iterator i = parse(  writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTVariable a = (IASTVariable) i.next();
	}
	
	public void testDefaultTemplateParameterWithDeferedInstance() throws Exception{
		Writer writer = new StringWriter();
		writer.write( "template < class T > class A;  \n" );
		writer.write( "template < class U, class V = A< U > > class B; \n" );
		writer.write( "B< int > b;" );
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration templateA = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration templateB = (IASTTemplateDeclaration) i.next();
		IASTVariable b = (IASTVariable) i.next();
	}
	
	public void testExplicitInstantiation() throws Exception{
		
		Writer writer = new StringWriter();
		writer.write( "template < class T > class A { }; " );
		writer.write( "template class A< int >; " );
		writer.write( "A< int > a; " );
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTClassSpecifier A = (IASTClassSpecifier) template.getOwnedDeclaration();
		IASTTemplateInstantiation instance = (IASTTemplateInstantiation) i.next();
		IASTVariable var = (IASTVariable) i.next();
		
		assertAllReferences( 2, createTaskList( new Task( A, 2 ) ) );
	}
	
	public void testTemplateParametersInExpressions() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template < typename _Tp > power( _Tp, unsigned int );     \n" );
		writer.write( "template < typename _Tp > _Tp helper( _Tp __x, int _n )   \n" );
		writer.write( "{ " );
		writer.write( "   return n < 0 ? _Tp( 1 ) / power( __x, -__n )           \n" );
		writer.write( "                : power( __x, __n );                      \n" );
		writer.write( "} " );
		
		parse( writer.toString () );
	}
	
	public void testBug44338() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template < bool T > class A {   ");
		writer.write( "   void foo( bool b = T );      ");
		writer.write( "};                              ");
		writer.write( "typedef A< 1 < 2 > A_TRUE;      ");
		writer.write( "typedef A< ( 1 > 2 ) > A_FALSE; ");
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTTypedefDeclaration  a_true = (IASTTypedefDeclaration) i.next();
		IASTTypedefDeclaration  a_false = (IASTTypedefDeclaration) i.next();
	}
	
	public void testBug44338_2() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template < int i > class X {};   ");
		writer.write( "template < class T > class Y {}; ");
		writer.write( "Y< X < 1 > > y1;                 ");
		writer.write( "Y< X < 6 >> 1 > > y2;            ");
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration templateX = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration templateY = (IASTTemplateDeclaration) i.next();
		IASTVariable y1 = (IASTVariable) i.next();
		IASTVariable y2 = (IASTVariable) i.next();
	}
	
	public void testBug4338_3() throws Exception
	{
		try{
			//this is expected to fail the parse
			parse( "template < int i > class X {};  X< 1 > 2 > x; " );
			assertTrue( false );
		} catch ( ParserException e ){
			assertTrue( e.getMessage().equals( "FAILURE" ) );
		}
	}
	
	public void testBug57754() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("template < class T > class A{                      ");
		writer.write("   typedef int _type;                              ");
		writer.write("   void f( _type, T );                             ");
		writer.write("};                                                 ");
		writer.write("template < class T > void A< T >::f( _type, T ) {} ");
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTClassSpecifier cls = (IASTClassSpecifier) template.getOwnedDeclaration();
		
		i = getDeclarations( cls );
		IASTTypedefDeclaration _type = (IASTTypedefDeclaration) i.next();
		
		assertReferenceTask( new Task( _type, 2 ) );
	}
	
	public void testContructorsAndExplicitSpecialization() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("template < class T > class A {  ");
		writer.write("   A();                         ");
		writer.write("   A( int );                    ");
		writer.write("   ~A();                        ");
		writer.write("};                              ");
		writer.write("template <> A< char >::~A();    ");

		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTTemplateSpecialization spec = (IASTTemplateSpecialization) i.next();
	}
}
