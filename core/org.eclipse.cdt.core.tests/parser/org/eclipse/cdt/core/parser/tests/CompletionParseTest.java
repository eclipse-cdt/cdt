/*
 * Created on Dec 8, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.core.parser.tests;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
import org.eclipse.cdt.core.parser.ast.IASTNode.ILookupResult;
import org.eclipse.cdt.core.parser.ast.IASTNode.LookupKind;

/**
 * @author jcamelon
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class CompletionParseTest extends CompleteParseBaseTest {

	
	public CompletionParseTest(String name) {
		super(name);
	}

	protected IASTCompletionNode parse(String code, int offset)
		throws Exception {
		callback = new FullParseCallback();
		IParser parser = null;

		parser =
			ParserFactory.createParser(
				ParserFactory.createScanner(
					new StringReader(code),
					"completion-test",
					new ScannerInfo(),
					ParserMode.COMPLETION_PARSE,
					ParserLanguage.CPP,
					callback,
					new NullLogService(), null),
				callback,
				ParserMode.COMPLETION_PARSE,
				ParserLanguage.CPP,
				null);
		
		return parser.parse( offset );

	}

	protected IASTCompletionNode parse(String code, int offset, ParserLanguage lang ) throws Exception {
		callback = new FullParseCallback();
		IParser parser = null;
	
		parser =
			ParserFactory.createParser(
				ParserFactory.createScanner(
					new StringReader(code),
					"completion-test",
					new ScannerInfo(),
					ParserMode.COMPLETION_PARSE,
					lang,
					callback,
					new NullLogService(), null),
				callback,
				ParserMode.COMPLETION_PARSE,
				lang,
				null);
		
		return parser.parse( offset );
	
	}
	public void testBaseCase_SimpleDeclaration() throws Exception
	{
		StringWriter writer = new StringWriter(); 
		writer.write( "class ABC " ); 
		writer.write( "{int x;}; " ); 
		writer.write( "AB\n\n" );

		IASTCompletionNode node = null;
		Iterator keywords = null;
		
		node = parse( writer.toString(), 21); 
		assertNotNull( node );
		assertNotNull( node.getCompletionPrefix() );
		assertEquals( node.getCompletionScope(), ((Scope)callback.getCompilationUnit()).getScope() );
		assertEquals( node.getCompletionPrefix(), "A");
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.VARIABLE_TYPE );
		keywords = node.getKeywords();
		assertFalse( keywords.hasNext() );

		node = parse( writer.toString(), 12); 
		assertNotNull( node );
		assertNotNull( node.getCompletionPrefix() );
		assertTrue( node.getCompletionScope() instanceof IASTClassSpecifier );
		assertEquals( node.getCompletionPrefix(), "i");
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.FIELD_TYPE );
		keywords = node.getKeywords(); 
		assertTrue( keywords.hasNext() );
		assertEquals( (String) keywords.next(), "inline");
		assertEquals( (String) keywords.next(), "int");
		assertFalse( keywords.hasNext() );
		
		node = parse( writer.toString(), 22); 
		assertNotNull( node );
		assertNotNull( node.getCompletionPrefix() );
		assertEquals( node.getCompletionScope(), ((Scope)callback.getCompilationUnit()).getScope() );
		assertEquals( node.getCompletionPrefix(), "AB");
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.VARIABLE_TYPE );
		keywords = node.getKeywords(); 
		assertFalse( keywords.hasNext() );
	
		node = parse( writer.toString(), 6); 
		assertNotNull( node );
		assertNotNull( node.getCompletionPrefix() );
		assertEquals( node.getCompletionScope(), ((Scope)callback.getCompilationUnit()).getScope() );
		assertEquals( node.getCompletionPrefix(), "");
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.USER_SPECIFIED_NAME );
		keywords = node.getKeywords(); 
		assertFalse( keywords.hasNext() );
	}
	
	public void testCompletionLookup_Unqualified() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "int aVar; " );
		writer.write( "void foo( ) { " );
		writer.write( "   int anotherVar; " );
		writer.write( "   a " );
		writer.write( "} " );
		
		String code = writer.toString();
		
		for( int i = 0; i < 2; ++i )
		{	
			int index = ( i == 0 ? code.indexOf( " a " ) + 2 : code.indexOf( " a ") + 1 );
			
			IASTCompletionNode node = parse( code, index );	
			assertNotNull( node );
			
			String prefix = node.getCompletionPrefix();
			assertNotNull( prefix );
			assertTrue( node.getCompletionScope() instanceof IASTFunction );
			assertEquals( prefix, i == 0 ? "a" :"" );
			assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE );
			
			IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
			kinds[0] = IASTNode.LookupKind.ALL; 
			ILookupResult result = node.getCompletionScope().lookup( prefix, kinds, node.getCompletionContext() );
			assertEquals( result.getPrefix(), prefix );
			
			Iterator iter = result.getNodes();
			
			IASTVariable anotherVar = (IASTVariable) iter.next();
			
			IASTVariable aVar = (IASTVariable) iter.next();
			
			if( i != 0 )
			{
				IASTFunction foo = (IASTFunction) iter.next();
				assertEquals( foo.getName(), "foo");
			}
					
			assertFalse( iter.hasNext() );
			assertEquals( anotherVar.getName(), "anotherVar" );
			assertEquals( aVar.getName(), "aVar" );
		}
	}
	
	public void testCompletionLookup_Qualified() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "int aVar; " );
		writer.write( "struct D{ " );
		writer.write( "   int aField; " );
		writer.write( "   void aMethod(); " );
		writer.write( "}; " );
		writer.write( "void foo(){" );
		writer.write( "   D d; " );
		writer.write( "   d.a " );
		writer.write( "}\n" );
		
		String code = writer.toString();
		int index = code.indexOf( "d.a" );
		
		IASTCompletionNode node = parse( code, index + 3 );				
		assertNotNull( node );
		
		String prefix = node.getCompletionPrefix();
		assertNotNull( prefix );
		assertEquals( prefix, "a" );
		
		assertTrue( node.getCompletionScope() instanceof IASTFunction );
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.MEMBER_REFERENCE );
		assertNotNull( node.getCompletionContext() );
		assertTrue( node.getCompletionContext() instanceof IASTClassSpecifier );
		
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
		kinds[0] = IASTNode.LookupKind.ALL; 
		ILookupResult result = node.getCompletionScope().lookup( prefix, kinds, node.getCompletionContext() );
		assertEquals( result.getPrefix(), prefix );
		
		Iterator iter = result.getNodes();
		
		IASTField aField = (IASTField) iter.next();
		IASTMethod aMethod = (IASTMethod) iter.next();
		
		assertFalse( iter.hasNext() );
		
		assertEquals( aMethod.getName(), "aMethod" );
		assertEquals( aField.getName(), "aField" );
	}
	
	public void testMemberCompletion_Arrow() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "class A {" );
		writer.write( "   public:   void aPublicBaseMethod();" );
		writer.write( "   private:  void aPrivateBaseMethod();" );
		writer.write( "};" );
		writer.write( "class B : public A {" );
		writer.write( "   public:   void aMethod();" );
		writer.write( "};" );		
		writer.write( "void foo(){" );		
		writer.write( "   B * b = new B();" );		
		writer.write( "   b-> \n" );
		
		String code = writer.toString();
		int index = code.indexOf( "b->" );
		
		IASTCompletionNode node = parse( code, index + 3 );
		assertNotNull(node);
		assertEquals( node.getCompletionPrefix(), "" );
		
		assertEquals(node.getCompletionKind(), IASTCompletionNode.CompletionKind.MEMBER_REFERENCE);
		assertTrue(node.getCompletionScope() instanceof IASTFunction );
		assertEquals( ((IASTFunction)node.getCompletionScope()).getName(), "foo" ); 
		assertTrue(node.getCompletionContext() instanceof IASTClassSpecifier );
		assertEquals( ((IASTClassSpecifier)node.getCompletionContext()).getName(), "B" );
	}
	
	public void testMemberCompletion_Dot() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "class A {" );
		writer.write( "   public:   void aPublicBaseMethod();" );
		writer.write( "   private:  void aPrivateBaseMethod();" );
		writer.write( "};" );
		writer.write( "class B : public A {" );
		writer.write( "   public:   void aMethod();" );
		writer.write( "};" );		
		writer.write( "void foo(){" );		
		writer.write( "   B b;" );		
		writer.write( "   b. \n" );
		
		String code = writer.toString();
		int index = code.indexOf( "b." );
		
		IASTCompletionNode node = parse( code, index + 2 );
		assertNotNull(node);
		assertEquals( node.getCompletionPrefix(), "" );
		
		assertEquals(node.getCompletionKind(), IASTCompletionNode.CompletionKind.MEMBER_REFERENCE);
		assertTrue(node.getCompletionScope() instanceof IASTFunction );
		assertEquals( ((IASTFunction)node.getCompletionScope()).getName(), "foo" ); 
		assertTrue(node.getCompletionContext() instanceof IASTClassSpecifier );
		assertEquals( ((IASTClassSpecifier)node.getCompletionContext()).getName(), "B" );
	}
	
	
	public void testCompletionLookup_Pointer() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( "class A {" );
		writer.write( "   public:   void aPublicBaseMethod();" );
		writer.write( "   private:  void aPrivateBaseMethod();" );
		writer.write( "};" );
		writer.write( "class B : public A {" );
		writer.write( "   public:   void aMethod();" );
		writer.write( "};" );		
		writer.write( "void foo(){" );		
		writer.write( "   B * b = new B();" );		
		writer.write( "   b->a \n" );
		
		for( int i = 0; i < 2; ++i )
		{	
			String code = writer.toString();
			
			int index;
			
			index = (i == 0 )? (code.indexOf( "b->a" )+4) :(code.indexOf( "b->") + 3);
			
			IASTCompletionNode node = parse( code, index);
			
			assertNotNull( node );
			String prefix = node.getCompletionPrefix();
			
			assertEquals( prefix, ( i == 0 ) ? "a" :"");
			
			assertTrue( node.getCompletionScope() instanceof IASTFunction );
			assertEquals( node.getCompletionKind(),  IASTCompletionNode.CompletionKind.MEMBER_REFERENCE );
			assertNotNull( node.getCompletionContext() );
			assertTrue( node.getCompletionContext() instanceof IASTClassSpecifier );
			
			IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
			kinds[0] = IASTNode.LookupKind.METHODS; 
			ILookupResult result = node.getCompletionScope().lookup( prefix, kinds, node.getCompletionContext() );
			assertEquals( result.getPrefix(), prefix );
			
			Iterator iter = result.getNodes();
			IASTMethod method = (IASTMethod) iter.next();
			IASTMethod baseMethod = (IASTMethod) iter.next();
			
			assertFalse( iter.hasNext() );
			
			assertEquals( method.getName(), "aMethod" );
			assertEquals( baseMethod.getName(), "aPublicBaseMethod" );		
		}
	}
	
	public void testCompletionLookup_FriendClass_1() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( "class A {" );
		writer.write( "   private:  void aPrivateMethod();" );
		writer.write( "   friend class C;" );
		writer.write( "};" );
		
		writer.write( "class C {" );
		writer.write( "   void foo();" );
		writer.write( "};" );	
		
		writer.write( "void C::foo(){" );		
		writer.write( "   A a;" );		
		writer.write( "   a.a \n" );
		
		String code = writer.toString();
		int index = code.indexOf( "a.a" );
		
		IASTCompletionNode node = parse( code, index + 3 );
		
		assertNotNull( node );
		
		String prefix = node.getCompletionPrefix();
		assertEquals( prefix, "a" );
		
		assertTrue( node.getCompletionScope() instanceof IASTFunction );
		assertEquals( node.getCompletionKind(),  IASTCompletionNode.CompletionKind.MEMBER_REFERENCE );
		assertNotNull( node.getCompletionContext() );
		assertTrue( node.getCompletionContext() instanceof IASTClassSpecifier );
		
		ILookupResult result = node.getCompletionScope().lookup( prefix, new IASTNode.LookupKind [] { IASTNode.LookupKind.METHODS }, node.getCompletionContext() );
		assertEquals( result.getPrefix(), prefix );
		
		Iterator iter = result.getNodes();
		assertTrue( iter.hasNext() );
		
		IASTMethod method = (IASTMethod) iter.next();
		
		assertFalse( iter.hasNext() );
		
		assertEquals( method.getName(), "aPrivateMethod" );
	}
	
	public void testCompletionLookup_FriendClass_2() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( "class C {" );
		writer.write( "   void foo();" );
		writer.write( "};" );		
		writer.write( "class A {" );
		writer.write( "   private:  void aPrivateMethod();" );
		writer.write( "   friend class C;" );
		writer.write( "};" );

		writer.write( "void C::foo(){" );		
		writer.write( "   A a;" );		
		writer.write( "   a.a \n" );
		
		String code = writer.toString();
		int index = code.indexOf( "a.a" );
		
		IASTCompletionNode node = parse( code, index + 3 );
		
		assertNotNull( node );
		
		String prefix = node.getCompletionPrefix();
		assertEquals( prefix, "a" );
		
		assertTrue( node.getCompletionScope() instanceof IASTFunction );
		assertEquals( node.getCompletionKind(),  IASTCompletionNode.CompletionKind.MEMBER_REFERENCE );
		assertNotNull( node.getCompletionContext() );
		assertTrue( node.getCompletionContext() instanceof IASTClassSpecifier );
		
		ILookupResult result = node.getCompletionScope().lookup( prefix, new IASTNode.LookupKind [] { IASTNode.LookupKind.METHODS }, node.getCompletionContext() );
		assertEquals( result.getPrefix(), prefix );
		
		Iterator iter = result.getNodes();
		assertTrue( iter.hasNext() );
		
		IASTMethod method = (IASTMethod) iter.next();
		
		assertFalse( iter.hasNext() );
		
		assertEquals( method.getName(), "aPrivateMethod" );
	}
	
	
	public void testCompletionLookup_ParametersAsLocalVariables() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( "int foo( int aParameter ){" );
		writer.write( "   int aLocal;" );
		writer.write( "   if( aLocal != 0 ){" );		
		writer.write( "      int aBlockLocal;" );
		writer.write( "      a \n" );
		
		String code = writer.toString();
		int index = code.indexOf( " a " );
		
		IASTCompletionNode node = parse( code, index + 2 );
		
		assertNotNull( node );
		
		String prefix = node.getCompletionPrefix();
		assertEquals( prefix, "a" );
		
		assertTrue( node.getCompletionScope() instanceof IASTCodeScope );
		assertEquals( node.getCompletionKind(),  IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE );
		assertNull( node.getCompletionContext() );
				
		ILookupResult result = node.getCompletionScope().lookup( prefix, new IASTNode.LookupKind [] { IASTNode.LookupKind.LOCAL_VARIABLES }, node.getCompletionContext() );
		assertEquals( result.getPrefix(), prefix );
		
		Iterator iter = result.getNodes();
				
		IASTVariable aBlockLocal = (IASTVariable) iter.next();
		IASTVariable aLocal = (IASTVariable) iter.next();
		IASTParameterDeclaration aParameter = (IASTParameterDeclaration) iter.next();
		
		assertFalse( iter.hasNext() );
		
		assertEquals( aBlockLocal.getName(), "aBlockLocal" );
		assertEquals( aLocal.getName(), "aLocal" );
		assertEquals( aParameter.getName(), "aParameter" );
	}
	
	public void testCompletionLookup_LookupKindTHIS() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( "int aGlobalVar;" );
		writer.write( "namespace NS { " );
		writer.write( "   int aNamespaceFunction(){}" );
		writer.write( "   class Base { " );
		writer.write( "      protected: int aBaseField;" );
		writer.write( "   };" );
		writer.write( "   class Derived : public Base {" );
		writer.write( "      int aMethod();" );
		writer.write( "   };" );
		writer.write( "}" );
		writer.write( "int NS::Derived::aMethod(){");
		writer.write( "   int aLocal;" );
		writer.write( "   a  ");

		String code = writer.toString();
		int index = code.indexOf( " a " );
		
		IASTCompletionNode node = parse( code, index + 2 );
		
		assertNotNull( node );
		
		assertEquals( node.getCompletionPrefix(), "a" );
		assertTrue( node.getCompletionScope() instanceof IASTMethod );
		
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(),
																new IASTNode.LookupKind[] { IASTNode.LookupKind.THIS },
																node.getCompletionContext() );
		
		assertEquals( result.getResultsSize(), 2 );
		
		Iterator iter = result.getNodes();
		IASTMethod method = (IASTMethod) iter.next();
		IASTField field = (IASTField) iter.next();
		assertFalse( iter.hasNext() );
		assertEquals( method.getName(), "aMethod" );
		assertEquals( field.getName(), "aBaseField" );
		
		result = node.getCompletionScope().lookup( node.getCompletionPrefix(),
												   new IASTNode.LookupKind[] { IASTNode.LookupKind.THIS, IASTNode.LookupKind.METHODS },
												   node.getCompletionContext() );
		
		assertEquals( result.getResultsSize(), 1 );
		iter = result.getNodes();
		method = (IASTMethod) iter.next();
		assertFalse( iter.hasNext() );
		assertEquals( method.getName(), "aMethod" );
	}
	
	public void testCompletionInConstructor() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("class SimpleTest{");
		writer.write("	public:");
		writer.write("SimpleTest();");
		writer.write("~SimpleTest();");
		writer.write("int a, b, c, aa, bb, cc, abc;");
		writer.write("};");
		writer.write("SimpleTest::~SimpleTest()");
		writer.write("{}");
		writer.write("SimpleTest::SimpleTest()");
		writer.write("{");
		writer.write("/**/a");
		writer.write("}");

		IASTCompletionNode node = parse( writer.toString(), writer.toString().indexOf("/**/a") + 5 );
		assertNotNull(node);
		assertEquals(node.getCompletionPrefix(), "a");
		assertTrue(node.getCompletionScope() instanceof IASTMethod);
		IASTMethod inquestion = (IASTMethod)node.getCompletionScope();
		assertEquals( inquestion.getName(), "SimpleTest");
		assertTrue(inquestion.isConstructor());
		
		assertEquals(node.getCompletionKind(), IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE );
		assertNull(node.getCompletionContext());
		LookupKind[] kinds = new LookupKind[ 1 ];
		kinds[0] = LookupKind.FIELDS;
		
		ILookupResult result = inquestion.lookup( "a", kinds, null );
		assertEquals(result.getResultsSize(), 3 );
	}
	
	public void testCompletionInDestructor() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("class SimpleTest{");
		writer.write("	public:");
		writer.write("SimpleTest();");
		writer.write("~SimpleTest();");
		writer.write("int a, b, c, aa, bb, cc, abc;");
		writer.write("};");
		writer.write("SimpleTest::SimpleTest()");
		writer.write("{}");
		writer.write("SimpleTest::~SimpleTest()");
		writer.write("{");
		writer.write("/**/a");
		writer.write("}");

		IASTCompletionNode node = parse( writer.toString(), writer.toString().indexOf("/**/a") + 5 );
		assertNotNull(node);
		assertEquals(node.getCompletionPrefix(), "a");
		assertTrue(node.getCompletionScope() instanceof IASTMethod);
		IASTMethod inquestion = (IASTMethod)node.getCompletionScope();
		assertEquals( inquestion.getName(), "~SimpleTest");
		assertTrue(inquestion.isDestructor());
		
		assertEquals(node.getCompletionKind(), IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE );
		assertNull(node.getCompletionContext());
		LookupKind[] kinds = new LookupKind[ 1 ];
		kinds[0] = LookupKind.FIELDS;
		
		ILookupResult result = inquestion.lookup( "a", kinds, null );
		assertEquals(result.getResultsSize(), 3 );
	}
	
	public void testBug48307_FriendFunction_1() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write( "class A{ public : void foo(); }; " );
		writer.write( "class B{ ");
		writer.write( "   private : int aPrivate;" );
		writer.write( "   friend void A::foo(); ");
		writer.write( "};" );
		writer.write( "void A::foo(){" );
		writer.write( "   B b;");
		writer.write( "   b.aP" );
		
		String code = writer.toString();
		int index = code.indexOf( "b.aP" );
		IASTCompletionNode node = parse( code, index + 4  );
		
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
				new IASTNode.LookupKind[] { IASTNode.LookupKind.ALL }, 
				node.getCompletionContext() );

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
		writer.write( "void global(){" );
		writer.write( "   B b;");
		writer.write( "   b.aP" );
		
		String code = writer.toString();
		int index = code.indexOf( "b.aP" );
		IASTCompletionNode node = parse( code, index + 4  );
		
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
				new IASTNode.LookupKind[] { IASTNode.LookupKind.ALL }, 
				node.getCompletionContext() );

		assertEquals( result.getResultsSize(), 1 );
		IASTField field = (IASTField) result.getNodes().next();
		assertEquals( field.getName(), "aPrivate" );
	}
	
	public void testBug51260() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( " class A { public: void a(); }; " );
		writer.write( " class B : public virtual A { public: void b(); };" );
		writer.write( " class C : public virtual A { public: void c(); };" );
		writer.write( " class D : public B, C { public: void d(); };" );
		
		writer.write( " void A::a(){} ");
		writer.write( " void B::b(){} ");
		writer.write( " void C::c(){} ");
		writer.write( " void D::d(){ SP }" );
		
		String code = writer.toString();
		int index = code.indexOf( "SP" );
		IASTCompletionNode node = parse( code, index );
		
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
		                                                         new IASTNode.LookupKind[]{ IASTNode.LookupKind.THIS },
																 node.getCompletionContext() );
		
		assertEquals( result.getResultsSize(), 4 );
		
		Iterator iter = result.getNodes();
		IASTMethod d = (IASTMethod) iter.next();
		IASTMethod b = (IASTMethod) iter.next();
		IASTMethod a = (IASTMethod) iter.next();
		IASTMethod c = (IASTMethod) iter.next();
		
		assertFalse( iter.hasNext() );
		
		assertEquals( a.getName(), "a" );
		assertEquals( b.getName(), "b" );
		assertEquals( c.getName(), "c" );
		assertEquals( d.getName(), "d" );
		
	}
	
	public void testBug52948() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( "typedef int Int; ");
		writer.write( "InSP" );
		
		String code = writer.toString();
		int index = code.indexOf( "SP" );
		
		IASTCompletionNode node = parse( code, index );
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
                                                                 new IASTNode.LookupKind[]{ IASTNode.LookupKind.TYPEDEFS },
				                                                 node.getCompletionContext() );
		
		assertEquals( result.getResultsSize(), 1 );
		
		Iterator iter = result.getNodes();
		IASTTypedefDeclaration typeDef = (IASTTypedefDeclaration) iter.next();
		
		assertEquals( typeDef.getName(), "Int" );
		assertFalse( iter.hasNext() );
	}
	
	public void testCompletionInTypeDef() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( "struct A {  int name;  };  \n" );
		writer.write( "typedef struct A * PA;     \n" );
		writer.write( "int main() {               \n" );
		writer.write( "   PA a;                   \n" );
		writer.write( "   a->SP                   \n" );
		writer.write( "}                          \n" );
		
		String code = writer.toString();
		int index = code.indexOf( "SP" );
		
		IASTCompletionNode node = parse( code, index );
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
                                                                 new IASTNode.LookupKind[]{ IASTNode.LookupKind.ALL },
				                                                 node.getCompletionContext() );
		
		assertEquals( result.getResultsSize(), 1 );
		
		Iterator iter = result.getNodes();
		IASTField name = (IASTField) iter.next();
		
		assertEquals( name.getName(), "name" );
		assertFalse( iter.hasNext() );
	}
	
	public void testCompletionInFunctionBodyFullyQualified() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "int aInteger = 5;\n");
		writer.write( "namespace NMS { \n");
		writer.write( " int foo() { \n");
		writer.write( "::A ");
		writer.write( "}\n}\n");
		String code = writer.toString();
		
		for( int i = 0; i < 2; ++i )
		{
			String stringToCompleteAfter = ( i == 0 ) ? "::" : "::A";
			IASTCompletionNode node = parse( code, code.indexOf( stringToCompleteAfter) + stringToCompleteAfter.length() );
			
			validateCompletionNode(node, ( i == 0 ? "" : "A"), IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE, getCompilationUnit() );
			
			ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
	                new IASTNode.LookupKind[]{ IASTNode.LookupKind.ALL },
	                node.getCompletionContext() );
	
			Set results = new HashSet();
			results.add( "aInteger");
			if( i == 0 )
				results.add( "NMS");
			validateLookupResult(result, results );
		}

	}

	/**
	 * @param result
	 */
	private void validateLookupResult(ILookupResult result, Set matches) {
		
		assertNotNull( matches );
		assertEquals( result.getResultsSize(), matches.size() );
		
		Iterator iter = result.getNodes();
		while( iter.hasNext() )
		{
			IASTOffsetableNamedElement element = (IASTOffsetableNamedElement) iter.next();
			assertTrue( matches.contains( element.getName() ));
		}
	}

	/**
	 * @return
	 */
	protected IASTCompilationUnit getCompilationUnit() {
		IASTCompilationUnit compilationUnit = (IASTCompilationUnit) ((Scope) callback.getCompilationUnit()).getScope();
		return compilationUnit;
	}

	/**
	 * @param node
	 */
	protected void validateCompletionNode(IASTCompletionNode node, String prefix, CompletionKind kind, IASTNode context ) {
		assertNotNull( node );
		assertEquals( node.getCompletionPrefix(), prefix);
		assertEquals( node.getCompletionKind(), kind );
		assertEquals( node.getCompletionContext(), context );
		assertFalse( node.getKeywords().hasNext() );
	}
	
	public void testCompletionInFunctionBodyQualifiedName() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "namespace ABC {\n");
		writer.write( "  struct DEF { int x; }; \n" );
		writer.write( "  struct GHI { float y;};\n");
		writer.write( "}\n");
		writer.write( "int main() { ABC::D }\n");
		String code = writer.toString();
		
		for( int j = 0; j< 2; ++j )
		{
			String stringToCompleteAfter = (j == 0 ) ? "::" : "::D";
			IASTCompletionNode node = parse( code, code.indexOf( stringToCompleteAfter) + stringToCompleteAfter.length() );
			
			IASTNamespaceDefinition namespaceDefinition = null;
			Iterator i = callback.getCompilationUnit().getDeclarations();
			while( i.hasNext() )
			{
				IASTDeclaration d = (IASTDeclaration) i.next();
				if( d instanceof IASTNamespaceDefinition ) 
					if( ((IASTNamespaceDefinition)d).getName().equals( "ABC") )
					{
						namespaceDefinition = (IASTNamespaceDefinition) d;
						break;
					}
			}
			assertNotNull( namespaceDefinition );
			validateCompletionNode( node, 
					( j == 0 ) ? "" : "D", 
					IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE, namespaceDefinition ); 
	
			ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
	                new IASTNode.LookupKind[]{ IASTNode.LookupKind.ALL },
	                node.getCompletionContext() );
			
			Set results = new HashSet();
			results.add( "DEF");
			if( j == 0 )
				results.add( "GHI");
			validateLookupResult(result, results );
		}

	}
	
	public void testCompletionWithTemplateInstanceAsParent() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "template < class T > class A { public : int a_member; }; ");
		writer.write( "template < class T > class B : public A< T > { public : int b_member; }; ");
		writer.write( "void f() { ");
		writer.write( "   B< int > b; ");
		writer.write( "   b.SP ");
		
		String code = writer.toString();
		IASTCompletionNode node = parse( code, code.indexOf( "SP" ) );
		
		ILookupResult result = node.getCompletionScope().lookup( "", 
				                                                 new IASTNode.LookupKind[]{ IASTNode.LookupKind.ALL }, 
																 node.getCompletionContext() );
		assertEquals( result.getResultsSize(), 2 );
		
		Iterator i = result.getNodes();
		
		assertTrue( i.next() instanceof IASTField );
		assertTrue( i.next() instanceof IASTField );
		assertFalse( i.hasNext() );
	}
	
	public void testBug58178() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "#define GL_T 0x2001\n");
		writer.write( "#define GL_TRUE 0x1\n");
		writer.write( "typedef unsigned char   GLboolean;\n");
		writer.write( "static GLboolean should_rotate = GL_T");
		String code = writer.toString();
		final String where = "= GL_T";
		IASTCompletionNode node = parse( code, code.indexOf( where ) + where.length() );
		assertEquals( node.getCompletionPrefix(), "GL_T");
	}

	public void testBug52253() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "class CMyClass {public:\n void doorBell(){ return; }};");
		writer.write( "int	main(int argc, char **argv) {CMyClass mc; mc.do }");
		String code = writer.toString();
		final String where = "mc.do";
		IASTCompletionNode node = parse( code, code.indexOf( where) + where.length() );
		assertEquals( node.getCompletionPrefix(), "do");
		assertEquals( node.getCompletionKind(), CompletionKind.MEMBER_REFERENCE );
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
                new IASTNode.LookupKind[]{ IASTNode.LookupKind.ALL }, 
				 node.getCompletionContext() );
		assertEquals( result.getResultsSize(), 1 );
		Iterator i = result.getNodes();
		IASTMethod doorBell = (IASTMethod) i.next();
		assertFalse( i.hasNext() );
		assertEquals( doorBell.getName(), "doorBell");
		
	}
	
	public void testBug58492() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("struct Cube {                       ");
		writer.write("   int nLen;                        ");
		writer.write("   int nWidth;                      ");
		writer.write("   int nHeight;                     ");
		writer.write("};                                  ");
		writer.write("int volume( struct Cube * pCube ) { ");
		writer.write("   pCube->SP                        ");

		String code = writer.toString();
		IASTCompletionNode node = parse( code, code.indexOf("SP"), ParserLanguage.C );
		
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(),
				                                                 new IASTNode.LookupKind[] {IASTNode.LookupKind.ALL },
																 node.getCompletionContext() );
		assertEquals( result.getResultsSize(), 3 );
		Iterator i = result.getNodes();
		assertTrue( i.next() instanceof IASTField );
		assertTrue( i.next() instanceof IASTField );
		assertTrue( i.next() instanceof IASTField );
	}	
}
