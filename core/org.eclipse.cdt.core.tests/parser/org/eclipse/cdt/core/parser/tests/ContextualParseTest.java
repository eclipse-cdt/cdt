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
import java.util.Iterator;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTNode.ILookupResult;
import org.eclipse.cdt.core.parser.ast.IASTNode.LookupKind;
import org.eclipse.cdt.internal.core.parser.ParserLogService;

/**
 * @author jcamelon
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ContextualParseTest extends CompleteParseBaseTest {

	
	public ContextualParseTest(String name) {
		super(name);
	}

	protected IASTCompletionNode parse(String code, int offset)
		throws Exception {
		IParserLogService log = new ParserLogService();
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
					log),
				callback,
				ParserMode.COMPLETION_PARSE,
				ParserLanguage.CPP,
				log);
		
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
			assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.STATEMENT_START );
			
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
		assertEquals( node.getCompletionKind(),  IASTCompletionNode.CompletionKind.STATEMENT_START );
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
		
		assertEquals(node.getCompletionKind(), IASTCompletionNode.CompletionKind.STATEMENT_START );
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
		
		assertEquals(node.getCompletionKind(), IASTCompletionNode.CompletionKind.STATEMENT_START );
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
}
