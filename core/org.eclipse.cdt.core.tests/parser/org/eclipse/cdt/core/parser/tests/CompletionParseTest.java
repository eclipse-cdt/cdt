/*
 * Created on Dec 8, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.core.parser.tests;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
import org.eclipse.cdt.core.parser.ast.IASTNode.ILookupResult;
import org.eclipse.cdt.core.parser.ast.IASTNode.LookupKind;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;

/**
 * @author jcamelon
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class CompletionParseTest extends CompletionParseBaseTest {

	
	public CompletionParseTest(String name) {
		super(name);
	}

	public void testBaseCase_SimpleDeclaration() throws Exception
	{
		StringWriter writer = new StringWriter(); 
		writer.write( "class ABC " );  //$NON-NLS-1$
		writer.write( "{int x;}; " );  //$NON-NLS-1$
		writer.write( "AB\n\n" ); //$NON-NLS-1$

		IASTCompletionNode node = null;
		Iterator keywords = null;
		
		node = parse( writer.toString(), 21); 
		assertNotNull( node );
		assertNotNull( node.getCompletionPrefix() );
		assertEquals( node.getCompletionScope(), ((Scope)callback.getCompilationUnit()).getScope() );
		assertEquals( node.getCompletionPrefix(), "A"); //$NON-NLS-1$
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.VARIABLE_TYPE );
		keywords = node.getKeywords();
		assertFalse( keywords.hasNext() );

		node = parse( writer.toString(), 12); 
		assertNotNull( node );
		assertNotNull( node.getCompletionPrefix() );
		assertTrue( node.getCompletionScope() instanceof IASTClassSpecifier );
		assertEquals( node.getCompletionPrefix(), "i"); //$NON-NLS-1$
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.FIELD_TYPE );
		keywords = node.getKeywords(); 
		assertTrue( keywords.hasNext() );
		assertEquals( (String) keywords.next(), "inline"); //$NON-NLS-1$
		assertEquals( (String) keywords.next(), "int"); //$NON-NLS-1$
		assertFalse( keywords.hasNext() );
		
		node = parse( writer.toString(), 22); 
		assertNotNull( node );
		assertNotNull( node.getCompletionPrefix() );
		assertEquals( node.getCompletionScope(), ((Scope)callback.getCompilationUnit()).getScope() );
		assertEquals( node.getCompletionPrefix(), "AB"); //$NON-NLS-1$
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.VARIABLE_TYPE );
		keywords = node.getKeywords(); 
		assertFalse( keywords.hasNext() );
	
		node = parse( writer.toString(), 6); 
		assertNotNull( node );
		assertNotNull( node.getCompletionPrefix() );
		assertEquals( node.getCompletionScope(), ((Scope)callback.getCompilationUnit()).getScope() );
		assertEquals( node.getCompletionPrefix(), ""); //$NON-NLS-1$
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.CLASS_REFERENCE );
		keywords = node.getKeywords(); 
		assertFalse( keywords.hasNext() );
	}
	
	public void testCompletionLookup_Unqualified() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "int aVar; " ); //$NON-NLS-1$
		writer.write( "void foo( ) { " ); //$NON-NLS-1$
		writer.write( "   int anotherVar; " ); //$NON-NLS-1$
		writer.write( "   a " ); //$NON-NLS-1$
		writer.write( "} " ); //$NON-NLS-1$
		
		String code = writer.toString();
		
		for( int i = 0; i < 2; ++i )
		{	
			int index = ( i == 0 ? code.indexOf( " a " ) + 2 : code.indexOf( " a ") + 1 ); //$NON-NLS-1$ //$NON-NLS-2$
			
			IASTCompletionNode node = parse( code, index );	
			assertNotNull( node );
			
			String prefix = node.getCompletionPrefix();
			assertNotNull( prefix );
			assertTrue( node.getCompletionScope() instanceof IASTFunction );
			assertEquals( prefix, i == 0 ? "a" :"" ); //$NON-NLS-1$ //$NON-NLS-2$
			assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE );
			
			IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
			kinds[0] = IASTNode.LookupKind.ALL; 
			ILookupResult result = node.getCompletionScope().lookup( prefix, kinds, node.getCompletionContext(), null );
			assertEquals( result.getPrefix(), prefix );
			
			Iterator iter = result.getNodes();
			
			IASTVariable anotherVar = (IASTVariable) iter.next();
			
			IASTVariable aVar = (IASTVariable) iter.next();
			
			if( i != 0 )
			{
				IASTFunction foo = (IASTFunction) iter.next();
				assertEquals( foo.getName(), "foo"); //$NON-NLS-1$
			}
					
			assertFalse( iter.hasNext() );
			assertEquals( anotherVar.getName(), "anotherVar" ); //$NON-NLS-1$
			assertEquals( aVar.getName(), "aVar" ); //$NON-NLS-1$
		}
	}
	
	public void testCompletionLookup_Qualified() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "int aVar; " ); //$NON-NLS-1$
		writer.write( "struct D{ " ); //$NON-NLS-1$
		writer.write( "   int aField; " ); //$NON-NLS-1$
		writer.write( "   void aMethod(); " ); //$NON-NLS-1$
		writer.write( "}; " ); //$NON-NLS-1$
		writer.write( "void foo(){" ); //$NON-NLS-1$
		writer.write( "   D d; " ); //$NON-NLS-1$
		writer.write( "   d.a " ); //$NON-NLS-1$
		writer.write( "}\n" ); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( "d.a" ); //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, index + 3 );				
		assertNotNull( node );
		
		String prefix = node.getCompletionPrefix();
		assertNotNull( prefix );
		assertEquals( prefix, "a" ); //$NON-NLS-1$
		
		assertTrue( node.getCompletionScope() instanceof IASTFunction );
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.MEMBER_REFERENCE );
		assertNotNull( node.getCompletionContext() );
		assertTrue( node.getCompletionContext() instanceof IASTVariable );
		
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
		kinds[0] = IASTNode.LookupKind.ALL; 
		ILookupResult result = node.getCompletionScope().lookup( prefix, kinds, node.getCompletionContext(), null );
		assertEquals( result.getPrefix(), prefix );
		
		Iterator iter = result.getNodes();
		
		IASTField aField = (IASTField) iter.next();
		IASTMethod aMethod = (IASTMethod) iter.next();
		
		assertFalse( iter.hasNext() );
		
		assertEquals( aMethod.getName(), "aMethod" ); //$NON-NLS-1$
		assertEquals( aField.getName(), "aField" ); //$NON-NLS-1$
	}
	
	public void testMemberCompletion_Arrow() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "class A {" ); //$NON-NLS-1$
		writer.write( "   public:   void aPublicBaseMethod();" ); //$NON-NLS-1$
		writer.write( "   private:  void aPrivateBaseMethod();" ); //$NON-NLS-1$
		writer.write( "};" ); //$NON-NLS-1$
		writer.write( "class B : public A {" ); //$NON-NLS-1$
		writer.write( "   public:   void aMethod();" ); //$NON-NLS-1$
		writer.write( "};" );		 //$NON-NLS-1$
		writer.write( "void foo(){" );		 //$NON-NLS-1$
		writer.write( "   B * b = new B();" );		 //$NON-NLS-1$
		writer.write( "   b-> \n" ); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( "b->" ); //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, index + 3 );
		assertNotNull(node);
		assertEquals( node.getCompletionPrefix(), "" ); //$NON-NLS-1$
		
		assertEquals(node.getCompletionKind(), IASTCompletionNode.CompletionKind.MEMBER_REFERENCE);
		assertTrue(node.getCompletionScope() instanceof IASTFunction );
		assertEquals( ((IASTFunction)node.getCompletionScope()).getName(), "foo" );  //$NON-NLS-1$
		assertTrue(node.getCompletionContext() instanceof IASTVariable );
		assertEquals( ((IASTVariable)node.getCompletionContext()).getName(), "b" ); //$NON-NLS-1$
	}
	
	public void testMemberCompletion_Dot() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "class A {" ); //$NON-NLS-1$
		writer.write( "   public:   void aPublicBaseMethod();" ); //$NON-NLS-1$
		writer.write( "   private:  void aPrivateBaseMethod();" ); //$NON-NLS-1$
		writer.write( "};" ); //$NON-NLS-1$
		writer.write( "class B : public A {" ); //$NON-NLS-1$
		writer.write( "   public:   void aMethod();" ); //$NON-NLS-1$
		writer.write( "};" );		 //$NON-NLS-1$
		writer.write( "void foo(){" );		 //$NON-NLS-1$
		writer.write( "   B b;" );		 //$NON-NLS-1$
		writer.write( "   b. \n" ); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( "b." ); //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, index + 2 );
		assertNotNull(node);
		assertEquals( node.getCompletionPrefix(), "" ); //$NON-NLS-1$
		
		assertEquals(node.getCompletionKind(), IASTCompletionNode.CompletionKind.MEMBER_REFERENCE);
		assertTrue(node.getCompletionScope() instanceof IASTFunction );
		assertEquals( ((IASTFunction)node.getCompletionScope()).getName(), "foo" );  //$NON-NLS-1$
		assertTrue(node.getCompletionContext() instanceof IASTVariable );
		assertEquals( ((IASTVariable)node.getCompletionContext()).getName(), "b" ); //$NON-NLS-1$
	}
	
	
	public void testCompletionLookup_Pointer() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( "class A {" ); //$NON-NLS-1$
		writer.write( "   public:   void aPublicBaseMethod();" ); //$NON-NLS-1$
		writer.write( "   private:  void aPrivateBaseMethod();" ); //$NON-NLS-1$
		writer.write( "};" ); //$NON-NLS-1$
		writer.write( "class B : public A {" ); //$NON-NLS-1$
		writer.write( "   public:   void aMethod();" ); //$NON-NLS-1$
		writer.write( "};" );		 //$NON-NLS-1$
		writer.write( "void foo(){" );		 //$NON-NLS-1$
		writer.write( "   B * b = new B();" );		 //$NON-NLS-1$
		writer.write( "   b->a \n" ); //$NON-NLS-1$
		
		for( int i = 0; i < 2; ++i )
		{	
			String code = writer.toString();
			
			int index;
			
			index = (i == 0 )? (code.indexOf( "b->a" )+4) :(code.indexOf( "b->") + 3); //$NON-NLS-1$ //$NON-NLS-2$
			
			IASTCompletionNode node = parse( code, index);
			
			assertNotNull( node );
			String prefix = node.getCompletionPrefix();
			
			assertEquals( prefix, ( i == 0 ) ? "a" :""); //$NON-NLS-1$ //$NON-NLS-2$
			
			assertTrue( node.getCompletionScope() instanceof IASTFunction );
			assertEquals( node.getCompletionKind(),  IASTCompletionNode.CompletionKind.MEMBER_REFERENCE );
			assertNotNull( node.getCompletionContext() );
			assertTrue( node.getCompletionContext() instanceof IASTVariable );
			
			IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
			kinds[0] = IASTNode.LookupKind.METHODS; 
			ILookupResult result = node.getCompletionScope().lookup( prefix, kinds, node.getCompletionContext(), null );
			assertEquals( result.getPrefix(), prefix );
			
			Iterator iter = result.getNodes();
			IASTMethod method = (IASTMethod) iter.next();
			IASTMethod baseMethod = (IASTMethod) iter.next();
			
			assertFalse( iter.hasNext() );
			
			assertEquals( method.getName(), "aMethod" ); //$NON-NLS-1$
			assertEquals( baseMethod.getName(), "aPublicBaseMethod" );		 //$NON-NLS-1$
		}
	}
	
	public void testCompletionLookup_FriendClass_1() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( "class A {" ); //$NON-NLS-1$
		writer.write( "   private:  void aPrivateMethod();" ); //$NON-NLS-1$
		writer.write( "   friend class C;" ); //$NON-NLS-1$
		writer.write( "};" ); //$NON-NLS-1$
		
		writer.write( "class C {" ); //$NON-NLS-1$
		writer.write( "   void foo();" ); //$NON-NLS-1$
		writer.write( "};" );	 //$NON-NLS-1$
		
		writer.write( "void C::foo(){" );		 //$NON-NLS-1$
		writer.write( "   A a;" );		 //$NON-NLS-1$
		writer.write( "   a.a \n" ); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( "a.a" ); //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, index + 3 );
		
		assertNotNull( node );
		
		String prefix = node.getCompletionPrefix();
		assertEquals( prefix, "a" ); //$NON-NLS-1$
		
		assertTrue( node.getCompletionScope() instanceof IASTFunction );
		assertEquals( node.getCompletionKind(),  IASTCompletionNode.CompletionKind.MEMBER_REFERENCE );
		assertNotNull( node.getCompletionContext() );
		assertTrue( node.getCompletionContext() instanceof IASTVariable );
		
		ILookupResult result = node.getCompletionScope().lookup( prefix, new IASTNode.LookupKind [] { IASTNode.LookupKind.METHODS }, node.getCompletionContext(), null );
		assertEquals( result.getPrefix(), prefix );
		
		Iterator iter = result.getNodes();
		assertTrue( iter.hasNext() );
		
		IASTMethod method = (IASTMethod) iter.next();
		
		assertFalse( iter.hasNext() );
		
		assertEquals( method.getName(), "aPrivateMethod" ); //$NON-NLS-1$
	}
	
	public void testCompletionLookup_FriendClass_2() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( "class C {" ); //$NON-NLS-1$
		writer.write( "   void foo();" ); //$NON-NLS-1$
		writer.write( "};" );		 //$NON-NLS-1$
		writer.write( "class A {" ); //$NON-NLS-1$
		writer.write( "   private:  void aPrivateMethod();" ); //$NON-NLS-1$
		writer.write( "   friend class C;" ); //$NON-NLS-1$
		writer.write( "};" ); //$NON-NLS-1$

		writer.write( "void C::foo(){" );		 //$NON-NLS-1$
		writer.write( "   A a;" );		 //$NON-NLS-1$
		writer.write( "   a.a \n" ); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( "a.a" ); //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, index + 3 );
		
		assertNotNull( node );
		
		String prefix = node.getCompletionPrefix();
		assertEquals( prefix, "a" ); //$NON-NLS-1$
		
		assertTrue( node.getCompletionScope() instanceof IASTFunction );
		assertEquals( node.getCompletionKind(),  IASTCompletionNode.CompletionKind.MEMBER_REFERENCE );
		assertNotNull( node.getCompletionContext() );
		assertTrue( node.getCompletionContext() instanceof IASTVariable );
		
		ILookupResult result = node.getCompletionScope().lookup( prefix, new IASTNode.LookupKind [] { IASTNode.LookupKind.METHODS }, node.getCompletionContext(), null );
		assertEquals( result.getPrefix(), prefix );
		
		Iterator iter = result.getNodes();
		assertTrue( iter.hasNext() );
		
		IASTMethod method = (IASTMethod) iter.next();
		
		assertFalse( iter.hasNext() );
		
		assertEquals( method.getName(), "aPrivateMethod" ); //$NON-NLS-1$
	}
	
	
	public void testCompletionLookup_ParametersAsLocalVariables() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( "int foo( int aParameter ){" ); //$NON-NLS-1$
		writer.write( "   int aLocal;" ); //$NON-NLS-1$
		writer.write( "   if( aLocal != 0 ){" );		 //$NON-NLS-1$
		writer.write( "      int aBlockLocal;" ); //$NON-NLS-1$
		writer.write( "      a \n" ); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( " a " ); //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, index + 2 );
		
		assertNotNull( node );
		
		String prefix = node.getCompletionPrefix();
		assertEquals( prefix, "a" ); //$NON-NLS-1$
		
		assertTrue( node.getCompletionScope() instanceof IASTCodeScope );
		assertEquals( node.getCompletionKind(),  IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE );
		assertNull( node.getCompletionContext() );
				
		ILookupResult result = node.getCompletionScope().lookup( prefix, new IASTNode.LookupKind [] { IASTNode.LookupKind.LOCAL_VARIABLES }, node.getCompletionContext(), null );
		assertEquals( result.getPrefix(), prefix );
		
		Iterator iter = result.getNodes();
				
		IASTVariable aBlockLocal = (IASTVariable) iter.next();
		IASTVariable aLocal = (IASTVariable) iter.next();
		IASTParameterDeclaration aParameter = (IASTParameterDeclaration) iter.next();
		
		assertFalse( iter.hasNext() );
		
		assertEquals( aBlockLocal.getName(), "aBlockLocal" ); //$NON-NLS-1$
		assertEquals( aLocal.getName(), "aLocal" ); //$NON-NLS-1$
		assertEquals( aParameter.getName(), "aParameter" ); //$NON-NLS-1$
	}
	
	public void testCompletionLookup_LookupKindTHIS() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( "int aGlobalVar;" ); //$NON-NLS-1$
		writer.write( "namespace NS { " ); //$NON-NLS-1$
		writer.write( "   int aNamespaceFunction(){}" ); //$NON-NLS-1$
		writer.write( "   class Base { " ); //$NON-NLS-1$
		writer.write( "      protected: int aBaseField;" ); //$NON-NLS-1$
		writer.write( "   };" ); //$NON-NLS-1$
		writer.write( "   class Derived : public Base {" ); //$NON-NLS-1$
		writer.write( "      int aMethod();" ); //$NON-NLS-1$
		writer.write( "   };" ); //$NON-NLS-1$
		writer.write( "}" ); //$NON-NLS-1$
		writer.write( "int NS::Derived::aMethod(){"); //$NON-NLS-1$
		writer.write( "   int aLocal;" ); //$NON-NLS-1$
		writer.write( "   a  "); //$NON-NLS-1$

		String code = writer.toString();
		int index = code.indexOf( " a " ); //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, index + 2 );
		
		assertNotNull( node );
		
		assertEquals( node.getCompletionPrefix(), "a" ); //$NON-NLS-1$
		assertTrue( node.getCompletionScope() instanceof IASTMethod );
		
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(),
																new IASTNode.LookupKind[] { IASTNode.LookupKind.THIS },
																node.getCompletionContext(), null );
		
		assertEquals( result.getResultsSize(), 2 );
		
		Iterator iter = result.getNodes();
		IASTMethod method = (IASTMethod) iter.next();
		IASTField field = (IASTField) iter.next();
		assertFalse( iter.hasNext() );
		assertEquals( method.getName(), "aMethod" ); //$NON-NLS-1$
		assertEquals( field.getName(), "aBaseField" ); //$NON-NLS-1$
		
		result = node.getCompletionScope().lookup( node.getCompletionPrefix(),
												   new IASTNode.LookupKind[] { IASTNode.LookupKind.THIS, IASTNode.LookupKind.METHODS },
												   node.getCompletionContext(), null );
		
		assertEquals( result.getResultsSize(), 1 );
		iter = result.getNodes();
		method = (IASTMethod) iter.next();
		assertFalse( iter.hasNext() );
		assertEquals( method.getName(), "aMethod" ); //$NON-NLS-1$
	}
	
	public void testCompletionInConstructor() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("class SimpleTest{"); //$NON-NLS-1$
		writer.write("	public:"); //$NON-NLS-1$
		writer.write("SimpleTest();"); //$NON-NLS-1$
		writer.write("~SimpleTest();"); //$NON-NLS-1$
		writer.write("int a, b, c, aa, bb, cc, abc;"); //$NON-NLS-1$
		writer.write("};"); //$NON-NLS-1$
		writer.write("SimpleTest::~SimpleTest()"); //$NON-NLS-1$
		writer.write("{}"); //$NON-NLS-1$
		writer.write("SimpleTest::SimpleTest()"); //$NON-NLS-1$
		writer.write("{"); //$NON-NLS-1$
		writer.write("/**/a"); //$NON-NLS-1$
		writer.write("}"); //$NON-NLS-1$

		IASTCompletionNode node = parse( writer.toString(), writer.toString().indexOf("/**/a") + 5 ); //$NON-NLS-1$
		assertNotNull(node);
		assertEquals(node.getCompletionPrefix(), "a"); //$NON-NLS-1$
		assertTrue(node.getCompletionScope() instanceof IASTMethod);
		IASTMethod inquestion = (IASTMethod)node.getCompletionScope();
		assertEquals( inquestion.getName(), "SimpleTest"); //$NON-NLS-1$
		assertTrue(inquestion.isConstructor());
		
		assertEquals(node.getCompletionKind(), IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE );
		assertNull(node.getCompletionContext());
		LookupKind[] kinds = new LookupKind[ 1 ];
		kinds[0] = LookupKind.FIELDS;
		
		ILookupResult result = inquestion.lookup( "a", kinds, null, null ); //$NON-NLS-1$
		assertEquals(result.getResultsSize(), 3 );
	}
	
	public void testCompletionInDestructor() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("class SimpleTest{"); //$NON-NLS-1$
		writer.write("	public:"); //$NON-NLS-1$
		writer.write("SimpleTest();"); //$NON-NLS-1$
		writer.write("~SimpleTest();"); //$NON-NLS-1$
		writer.write("int a, b, c, aa, bb, cc, abc;"); //$NON-NLS-1$
		writer.write("};"); //$NON-NLS-1$
		writer.write("SimpleTest::SimpleTest()"); //$NON-NLS-1$
		writer.write("{}"); //$NON-NLS-1$
		writer.write("SimpleTest::~SimpleTest()"); //$NON-NLS-1$
		writer.write("{"); //$NON-NLS-1$
		writer.write("/**/a"); //$NON-NLS-1$
		writer.write("}"); //$NON-NLS-1$

		IASTCompletionNode node = parse( writer.toString(), writer.toString().indexOf("/**/a") + 5 ); //$NON-NLS-1$
		assertNotNull(node);
		assertEquals(node.getCompletionPrefix(), "a"); //$NON-NLS-1$
		assertTrue(node.getCompletionScope() instanceof IASTMethod);
		IASTMethod inquestion = (IASTMethod)node.getCompletionScope();
		assertEquals( inquestion.getName(), "~SimpleTest"); //$NON-NLS-1$
		assertTrue(inquestion.isDestructor());
		
		assertEquals(node.getCompletionKind(), IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE );
		assertNull(node.getCompletionContext());
		LookupKind[] kinds = new LookupKind[ 1 ];
		kinds[0] = LookupKind.FIELDS;
		
		ILookupResult result = inquestion.lookup( "a", kinds, null, null ); //$NON-NLS-1$
		assertEquals(result.getResultsSize(), 3 );
	}
	
	public void testBug48307_FriendFunction_1() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write( "class A{ public : void foo(); }; " ); //$NON-NLS-1$
		writer.write( "class B{ "); //$NON-NLS-1$
		writer.write( "   private : int aPrivate;" ); //$NON-NLS-1$
		writer.write( "   friend void A::foo(); "); //$NON-NLS-1$
		writer.write( "};" ); //$NON-NLS-1$
		writer.write( "void A::foo(){" ); //$NON-NLS-1$
		writer.write( "   B b;"); //$NON-NLS-1$
		writer.write( "   b.aP" ); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( "b.aP" ); //$NON-NLS-1$
		IASTCompletionNode node = parse( code, index + 4  );
		
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
				new IASTNode.LookupKind[] { IASTNode.LookupKind.ALL }, 
				node.getCompletionContext(), null );

		assertNotNull( result );
		assertEquals( result.getResultsSize(), 1 );
		IASTField field = (IASTField) result.getNodes().next();
		assertEquals( field.getName(), "aPrivate" ); //$NON-NLS-1$
	}

	public void testBug48307_FriendFunction_2() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write( "void global();" ); //$NON-NLS-1$
		writer.write( "class B{ "); //$NON-NLS-1$
		writer.write( "   private : int aPrivate;" ); //$NON-NLS-1$
		writer.write( "   friend void global(); "); //$NON-NLS-1$
		writer.write( "};" ); //$NON-NLS-1$
		writer.write( "void global(){" ); //$NON-NLS-1$
		writer.write( "   B b;"); //$NON-NLS-1$
		writer.write( "   b.aP" ); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( "b.aP" ); //$NON-NLS-1$
		IASTCompletionNode node = parse( code, index + 4  );
		
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
				new IASTNode.LookupKind[] { IASTNode.LookupKind.ALL }, 
				node.getCompletionContext(), null );

		assertNotNull( result );
		assertEquals( result.getResultsSize(), 1 );
		IASTField field = (IASTField) result.getNodes().next();
		assertEquals( field.getName(), "aPrivate" ); //$NON-NLS-1$
	}
	
	public void testBug51260() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( " class A { public: void a(); }; " ); //$NON-NLS-1$
		writer.write( " class B : public virtual A { public: void b(); };" ); //$NON-NLS-1$
		writer.write( " class C : public virtual A { public: void c(); };" ); //$NON-NLS-1$
		writer.write( " class D : public B, C { public: void d(); };" ); //$NON-NLS-1$
		
		writer.write( " void A::a(){} "); //$NON-NLS-1$
		writer.write( " void B::b(){} "); //$NON-NLS-1$
		writer.write( " void C::c(){} "); //$NON-NLS-1$
		writer.write( " void D::d(){ SP }" ); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( "SP" ); //$NON-NLS-1$
		IASTCompletionNode node = parse( code, index );
		
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
		                                                         new IASTNode.LookupKind[]{ IASTNode.LookupKind.THIS },
																 node.getCompletionContext(), null );
		
		assertTrue( node.getCompletionScope() instanceof IASTMethod );
		assertEquals( ((IASTMethod)node.getCompletionScope()).getName(), "d" ); //$NON-NLS-1$
		assertEquals( result.getResultsSize(), 4 );
		
		Iterator iter = result.getNodes();
		IASTMethod d = (IASTMethod) iter.next();
		IASTMethod b = (IASTMethod) iter.next();
		IASTMethod a = (IASTMethod) iter.next();
		IASTMethod c = (IASTMethod) iter.next();
		
		assertFalse( iter.hasNext() );
		
		assertEquals( a.getName(), "a" ); //$NON-NLS-1$
		assertEquals( b.getName(), "b" ); //$NON-NLS-1$
		assertEquals( c.getName(), "c" ); //$NON-NLS-1$
		assertEquals( d.getName(), "d" ); //$NON-NLS-1$
		
	}
	
	public void testBug52948() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( "typedef int Int; "); //$NON-NLS-1$
		writer.write( "InSP" ); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( "SP" ); //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, index );
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
                                                                 new IASTNode.LookupKind[]{ IASTNode.LookupKind.TYPEDEFS },
				                                                 node.getCompletionContext(), null );
		
		assertEquals( result.getResultsSize(), 1 );
		
		Iterator iter = result.getNodes();
		IASTTypedefDeclaration typeDef = (IASTTypedefDeclaration) iter.next();
		
		assertEquals( typeDef.getName(), "Int" ); //$NON-NLS-1$
		assertFalse( iter.hasNext() );
	}
	
	
	public void testCompletionInFunctionBodyFullyQualified() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "int aInteger = 5;\n"); //$NON-NLS-1$
		writer.write( "namespace NMS { \n"); //$NON-NLS-1$
		writer.write( " int foo() { \n"); //$NON-NLS-1$
		writer.write( "::A "); //$NON-NLS-1$
		writer.write( "}\n}\n"); //$NON-NLS-1$
		String code = writer.toString();
		
		for( int i = 0; i < 2; ++i )
		{
			String stringToCompleteAfter = ( i == 0 ) ? "::" : "::A"; //$NON-NLS-1$ //$NON-NLS-2$
			IASTCompletionNode node = parse( code, code.indexOf( stringToCompleteAfter) + stringToCompleteAfter.length() );
			
			validateCompletionNode(node, ( i == 0 ? "" : "A"), IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE, getCompilationUnit(), false ); //$NON-NLS-1$ //$NON-NLS-2$
			
			ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
	                new IASTNode.LookupKind[]{ IASTNode.LookupKind.ALL },
	                node.getCompletionContext(), null );
	
			Set results = new HashSet();
			results.add( "aInteger"); //$NON-NLS-1$
			if( i == 0 )
			{
				results.add( "NMS"); //$NON-NLS-1$
				results.add( "__builtin_va_list "); //$NON-NLS-1$
				results.add("__builtin_expect"); //$NON-NLS-1$
				results.add("__builtin_prefetch"); //$NON-NLS-1$
				results.add("__builtin_huge_val"); //$NON-NLS-1$
				results.add("__builtin_huge_valf"); //$NON-NLS-1$
				results.add("__builtin_huge_vall"); //$NON-NLS-1$
				results.add("__builtin_inf"); //$NON-NLS-1$
				results.add("__builtin_inff"); //$NON-NLS-1$
				results.add("__builtin_infl"); //$NON-NLS-1$
				results.add("__builtin_nan"); //$NON-NLS-1$
				results.add("__builtin_nanf"); //$NON-NLS-1$
				results.add("__builtin_nanl"); //$NON-NLS-1$
				results.add("__builtin_nans"); //$NON-NLS-1$
				results.add("__builtin_nansf"); //$NON-NLS-1$
				results.add("__builtin_nansl"); //$NON-NLS-1$
				results.add("__builtin_ffs"); //$NON-NLS-1$
				results.add("__builtin_clz"); //$NON-NLS-1$
				results.add("__builtin_ctz"); //$NON-NLS-1$
				results.add("__builtin_popcount"); //$NON-NLS-1$
				results.add("__builtin_parity"); //$NON-NLS-1$
				results.add("__builtin_ffsl"); //$NON-NLS-1$
				results.add("__builtin_clzl"); //$NON-NLS-1$
				results.add("__builtin_ctzl"); //$NON-NLS-1$
				results.add("__builtin_popcountl"); //$NON-NLS-1$
				results.add("__builtin_parityl"); //$NON-NLS-1$
				results.add("__builtin_ffsll"); //$NON-NLS-1$
				results.add("__builtin_clzll"); //$NON-NLS-1$
				results.add("__builtin_ctzll"); //$NON-NLS-1$
				results.add("__builtin_popcountll"); //$NON-NLS-1$
				results.add("__builtin_parityll"); //$NON-NLS-1$
			}
			validateLookupResult(result, results );
		}
	}

	public void testCompletionInFunctionBodyQualifiedName() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "namespace ABC {\n"); //$NON-NLS-1$
		writer.write( "  struct DEF { int x; }; \n" ); //$NON-NLS-1$
		writer.write( "  struct GHI { float y;};\n"); //$NON-NLS-1$
		writer.write( "}\n"); //$NON-NLS-1$
		writer.write( "int main() { ABC::D }\n"); //$NON-NLS-1$
		String code = writer.toString();
		
		for( int j = 0; j< 2; ++j )
		{
			String stringToCompleteAfter = (j == 0 ) ? "::" : "::D"; //$NON-NLS-1$ //$NON-NLS-2$
			IASTCompletionNode node = parse( code, code.indexOf( stringToCompleteAfter) + stringToCompleteAfter.length() );
			
			IASTNamespaceDefinition namespaceDefinition = null;
			Iterator i = callback.getCompilationUnit().getDeclarations();
			while( i.hasNext() )
			{
				IASTDeclaration d = (IASTDeclaration) i.next();
				if( d instanceof IASTNamespaceDefinition ) 
					if( ((IASTNamespaceDefinition)d).getName().equals( "ABC") ) //$NON-NLS-1$
					{
						namespaceDefinition = (IASTNamespaceDefinition) d;
						break;
					}
			}
			assertNotNull( namespaceDefinition );
			validateCompletionNode( node, 
					( j == 0 ) ? "" : "D",  //$NON-NLS-1$ //$NON-NLS-2$
					IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE, namespaceDefinition, false ); 
	
			ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
	                new IASTNode.LookupKind[]{ IASTNode.LookupKind.ALL },
	                node.getCompletionContext(), null );
			
			Set results = new HashSet();
			results.add( "DEF"); //$NON-NLS-1$
			if( j == 0 )
				results.add( "GHI"); //$NON-NLS-1$
			validateLookupResult(result, results );
		}

	}
	
	public void testCompletionWithTemplateInstanceAsParent() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "template < class T > class A { public : int a_member; }; "); //$NON-NLS-1$
		writer.write( "template < class T > class B : public A< T > { public : int b_member; }; "); //$NON-NLS-1$
		writer.write( "void f() { "); //$NON-NLS-1$
		writer.write( "   B< int > b; "); //$NON-NLS-1$
		writer.write( "   b.SP "); //$NON-NLS-1$
		
		String code = writer.toString();
		IASTCompletionNode node = parse( code, code.indexOf( "SP" ) ); //$NON-NLS-1$
		
		ILookupResult result = node.getCompletionScope().lookup( "",  //$NON-NLS-1$
				                                                 new IASTNode.LookupKind[]{ IASTNode.LookupKind.ALL }, 
																 node.getCompletionContext(), null );
		assertEquals( result.getResultsSize(), 2 );
		
		Iterator i = result.getNodes();
		
		assertTrue( i.next() instanceof IASTField );
		assertTrue( i.next() instanceof IASTField );
		assertFalse( i.hasNext() );
	}
	
	public void testBug58178() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "#define GL_T 0x2001\n"); //$NON-NLS-1$
		writer.write( "#define GL_TRUE 0x1\n"); //$NON-NLS-1$
		writer.write( "typedef unsigned char   GLboolean;\n"); //$NON-NLS-1$
		writer.write( "static GLboolean should_rotate = GL_T"); //$NON-NLS-1$
		String code = writer.toString();
		final String where = "= GL_T"; //$NON-NLS-1$
		IASTCompletionNode node = parse( code, code.indexOf( where ) + where.length() );
		assertEquals( node.getCompletionPrefix(), "GL_T"); //$NON-NLS-1$
	}

	public void testBug52253() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "class CMyClass {public:\n void doorBell(){ return; }};"); //$NON-NLS-1$
		writer.write( "int	main(int argc, char **argv) {CMyClass mc; mc.do }"); //$NON-NLS-1$
		String code = writer.toString();
		final String where = "mc.do"; //$NON-NLS-1$
		IASTCompletionNode node = parse( code, code.indexOf( where) + where.length() );
		assertEquals( node.getCompletionPrefix(), "do"); //$NON-NLS-1$
		assertEquals( node.getCompletionKind(), CompletionKind.MEMBER_REFERENCE );
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
                new IASTNode.LookupKind[]{ IASTNode.LookupKind.ALL }, 
				 node.getCompletionContext(), null );
		assertEquals( result.getResultsSize(), 1 );
		Iterator i = result.getNodes();
		IASTMethod doorBell = (IASTMethod) i.next();
		assertFalse( i.hasNext() );
		assertEquals( doorBell.getName(), "doorBell"); //$NON-NLS-1$
		
	}
	
	public void testBug58492() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("struct Cube {                       "); //$NON-NLS-1$
		writer.write("   int nLen;                        "); //$NON-NLS-1$
		writer.write("   int nWidth;                      "); //$NON-NLS-1$
		writer.write("   int nHeight;                     "); //$NON-NLS-1$
		writer.write("};                                  "); //$NON-NLS-1$
		writer.write("int volume( struct Cube * pCube ) { "); //$NON-NLS-1$
		writer.write("   pCube->SP                        "); //$NON-NLS-1$

		String code = writer.toString();
		IASTCompletionNode node = parse( code, code.indexOf("SP"), ParserLanguage.C ); //$NON-NLS-1$
		
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(),
				                                                 new IASTNode.LookupKind[] {IASTNode.LookupKind.ALL },
																 node.getCompletionContext(), null );
		assertEquals( result.getResultsSize(), 3 );
		Iterator i = result.getNodes();
		assertTrue( i.next() instanceof IASTField );
		assertTrue( i.next() instanceof IASTField );
		assertTrue( i.next() instanceof IASTField );
	}
	
	public void testCompletionOnExpression() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "class ABC { public: void voidMethod(); };\n"); //$NON-NLS-1$
		writer.write( "ABC * someFunction(void) { return new ABC(); }\n"); //$NON-NLS-1$
		writer.write( "void testFunction( void ) { someFunction()->V  }\n" ); //$NON-NLS-1$
		String code = writer.toString();
		for( int i = 0; i < 2; ++i )
		{
			int index = code.indexOf( "V"); //$NON-NLS-1$
			if( i == 1 ) ++index;
			IASTCompletionNode node = parse( code, index );
			assertEquals( node.getCompletionPrefix(), (i == 0 )? "": "V"); //$NON-NLS-1$ //$NON-NLS-2$
			assertEquals( node.getCompletionKind(), CompletionKind.MEMBER_REFERENCE );
			assertTrue( node.getCompletionContext() instanceof IASTExpression );
		}
		
	}
	
	public void testCompletionInTypeDef() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( "struct A {  int name;  };  \n" ); //$NON-NLS-1$
		writer.write( "typedef struct A * PA;     \n" ); //$NON-NLS-1$
		writer.write( "int main() {               \n" ); //$NON-NLS-1$
		writer.write( "   PA a;                   \n" ); //$NON-NLS-1$
		writer.write( "   a->SP                   \n" ); //$NON-NLS-1$
		writer.write( "}                          \n" ); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( "SP" ); //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, index );
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
                                                                 new IASTNode.LookupKind[]{ IASTNode.LookupKind.ALL },
				                                                 node.getCompletionContext(), null );
		assertEquals( result.getResultsSize(), 1 );
		
		Iterator iter = result.getNodes();
		IASTField name = (IASTField) iter.next();
		
		assertEquals( name.getName(), "name" ); //$NON-NLS-1$
		assertFalse( iter.hasNext() );
	}
	
	public void testBug59134() throws Exception
	{
		String code = "int main(){ siz }"; //$NON-NLS-1$
		IASTCompletionNode node = parse( code, code.indexOf("siz") ); //$NON-NLS-1$
		assertNotNull( node );
		Iterator keywords = node.getKeywords();
		boolean passed = false;
		while( keywords.hasNext() )
		{
			String keyword = (String) keywords.next();
			if( keyword.equals( "sizeof")) //$NON-NLS-1$
				passed = true;
		}
		assertTrue( passed );
		
	}
	
	public void testBug59893() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "struct A {  	                 \n" ); //$NON-NLS-1$ 
		writer.write( "   void f1() const volatile;	 \n" ); //$NON-NLS-1$ 
		writer.write( "   void f2() const;  		 \n" ); //$NON-NLS-1$
		writer.write( "   void f3() volatile;        \n" ); //$NON-NLS-1$
		writer.write( "   void f4();                 \n" ); //$NON-NLS-1$
		writer.write( "};                            \n" ); //$NON-NLS-1$
		writer.write( "void main( const A& a1 )      \n" ); //$NON-NLS-1$
		writer.write( "{                             \n" ); //$NON-NLS-1$
		writer.write( "   const volatile A * a2;     \n" ); //$NON-NLS-1$
		writer.write( "   const A * a3;              \n" ); //$NON-NLS-1$
		writer.write( "   volatile A * a4;           \n" ); //$NON-NLS-1$
		writer.write( "   A * a5;                    \n" ); //$NON-NLS-1$
		
		String code = writer.toString();
		
		IASTCompletionNode node = parse( code + "a1. ", code.length() + 3 ); //$NON-NLS-1$
		
		assertNotNull( node );
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(),
				                                                 new IASTNode.LookupKind[]{ IASTNode.LookupKind.ALL },
		                                                         node.getCompletionContext(), null );
		assertEquals( result.getResultsSize(), 2 );
		
		node = parse( code + "a2-> ", code.length() + 4 ); //$NON-NLS-1$
		assertNotNull( node );
		result = node.getCompletionScope().lookup( node.getCompletionPrefix(),
		                                           new IASTNode.LookupKind[]{ IASTNode.LookupKind.ALL },
		                                           node.getCompletionContext(), null );
		assertEquals( result.getResultsSize(), 1 );
		
		node = parse( code + "a3-> ", code.length() + 4 ); //$NON-NLS-1$
		assertNotNull( node );
		result = node.getCompletionScope().lookup( node.getCompletionPrefix(),
		                                           new IASTNode.LookupKind[]{ IASTNode.LookupKind.ALL },
		                                           node.getCompletionContext(), null );
		assertEquals( result.getResultsSize(), 2 );
		
		node = parse( code + "a4-> ", code.length() + 4 ); //$NON-NLS-1$
		assertNotNull( node );
		result = node.getCompletionScope().lookup( node.getCompletionPrefix(),
		                                           new IASTNode.LookupKind[]{ IASTNode.LookupKind.ALL },
		                                           node.getCompletionContext(), null );
		assertEquals( result.getResultsSize(), 2 );
		
		node = parse( code + "a5-> ", code.length() + 4 ); //$NON-NLS-1$
		assertNotNull( node );
		result = node.getCompletionScope().lookup( node.getCompletionPrefix(),
		                                           new IASTNode.LookupKind[]{ IASTNode.LookupKind.ALL },
		                                           node.getCompletionContext(), null );
		assertEquals( result.getResultsSize(), 4 );
	}
	
	public void testBug59893_Expression() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "struct A {  	                 \n" ); //$NON-NLS-1$ 
		writer.write( "   void f2() const;  		 \n" ); //$NON-NLS-1$
		writer.write( "   void f4();                 \n" ); //$NON-NLS-1$
		writer.write( "};                            \n" ); //$NON-NLS-1$
		writer.write( "const A * foo(){}             \n" ); //$NON-NLS-1$
		writer.write( "void main( )                  \n" ); //$NON-NLS-1$
		writer.write( "{                             \n" ); //$NON-NLS-1$
		writer.write( "   foo()->SP                  \n" ); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( "SP" ); //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, index );
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
                                                                 new IASTNode.LookupKind[]{ IASTNode.LookupKind.ALL },
				                                                 node.getCompletionContext(), null );
		assertEquals( result.getResultsSize(), 1 );
	}
	
	public void testParameterListFunctionReference() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "int foo( int firstParam, int secondParam );\n"); //$NON-NLS-1$
		writer.write( "void main() { \n"); //$NON-NLS-1$
		writer.write( "  int abc;\n"); //$NON-NLS-1$
		writer.write( "  int x;\n" ); //$NON-NLS-1$
		writer.write( "  foo( x,a"); //$NON-NLS-1$
		String code = writer.toString();
		for( int i = 0; i < 2; ++i )
		{
			int index = code.indexOf( "x,a") + 2; //$NON-NLS-1$
			if( i == 1 ) index++;
			IASTCompletionNode node = parse( code, index );
			validateCompletionNode(node, (( i == 0 ) ? "" : "a" ), CompletionKind.FUNCTION_REFERENCE, null, true ); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull( node.getFunctionParameters() );
			ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
                    new IASTNode.LookupKind[]{ IASTNode.LookupKind.LOCAL_VARIABLES },
                    node.getCompletionContext(), null );
			assertNotNull(result);
			assertEquals( result.getResultsSize(), ( i == 0 ) ? 2 : 1 );
		}
	}
	
	public void testParameterListConstructorReference() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "class A { \n"); //$NON-NLS-1$
		writer.write( "public:\n"); //$NON-NLS-1$
		writer.write( "  A( int first, int second );\n"); //$NON-NLS-1$
		writer.write( "};\n" ); //$NON-NLS-1$
		writer.write( "void main() { \n"); //$NON-NLS-1$
		writer.write( "  int four, x;"); //$NON-NLS-1$
		writer.write( "  A * a = new A( x,f "); //$NON-NLS-1$
		String code = writer.toString();
		for( int i = 0; i < 2; ++i )
		{
			int index = code.indexOf( "x,f") + 2; //$NON-NLS-1$
			if( i == 1 ) index++;
			IASTCompletionNode node = parse( code, index );
			validateCompletionNode(node, (( i == 0 ) ? "" : "f" ), CompletionKind.CONSTRUCTOR_REFERENCE, null, true ); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull( node.getFunctionParameters() );
			ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
                    new IASTNode.LookupKind[]{ IASTNode.LookupKind.LOCAL_VARIABLES },
                    node.getCompletionContext(), null );
			assertNotNull(result);
			assertEquals( result.getResultsSize(), ( i == 0 ) ? 2 : 1 );
		}
	}
	
	public void testConstructors() throws Exception
	{
		String code = "class Foo{ public: Foo(); };  Foo::SP "; //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, code.indexOf( "SP" ) ); //$NON-NLS-1$
		
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
                                                                 new IASTNode.LookupKind[]{ IASTNode.LookupKind.CONSTRUCTORS },
                                                                 node.getCompletionContext(), null );
		assertEquals( result.getResultsSize(), 1 );
		IASTMethod constructor = (IASTMethod) result.getNodes().next();
		assertEquals( constructor.getName(), "Foo" ); //$NON-NLS-1$
	}

	public void testBug50807() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "void foo();" ); //$NON-NLS-1$
		writer.write( "void foo( int );" ); //$NON-NLS-1$
		writer.write( "void foo( int, char );" ); //$NON-NLS-1$
		writer.write( "void foo( int, int, int );" ); //$NON-NLS-1$
		writer.write( "void bar(){ " ); //$NON-NLS-1$
		
		String code = writer.toString() + "foo( SP"; //$NON-NLS-1$
		IASTCompletionNode node = parse( code, code.indexOf( "SP" ) ); //$NON-NLS-1$
		
		assertEquals( node.getCompletionPrefix(), "" ); //$NON-NLS-1$
		assertEquals( node.getFunctionName(), "foo" ); //$NON-NLS-1$
		ILookupResult result = node.getCompletionScope().lookup( node.getFunctionName(), 
                                                                 new IASTNode.LookupKind[]{ IASTNode.LookupKind.FUNCTIONS },
                                                                 node.getCompletionContext(), null );
		assertEquals( result.getResultsSize(), 4 );
		
		code = writer.toString() + "foo( 1, SP"; //$NON-NLS-1$
		node = parse( code, code.indexOf( "SP" ) ); //$NON-NLS-1$
		
		assertEquals( node.getCompletionPrefix(), "" ); //$NON-NLS-1$
		assertEquals( node.getFunctionName(), "foo" ); //$NON-NLS-1$
		result = node.getCompletionScope().lookup( node.getFunctionName(), 
                                                   new IASTNode.LookupKind[]{ IASTNode.LookupKind.FUNCTIONS },
                                                   node.getCompletionContext(), node.getFunctionParameters() );
		
		assertEquals( result.getResultsSize(), 2 );
	}
	
	public void testBug60298() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "class ABC { public: ABC(); int myInt(); };\n"); //$NON-NLS-1$
		writer.write( "int ABC::" ); //$NON-NLS-1$
		String code = writer.toString();
		IASTCompletionNode node = parse( code, code.indexOf( "::") + 2 ); //$NON-NLS-1$
		assertEquals( node.getCompletionKind(), CompletionKind.SINGLE_NAME_REFERENCE );
		
	}
	
	public void testBug62344() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( " namespace Foo{  class bar{}; }     "); //$NON-NLS-1$
		writer.write( " void main() {                      "); //$NON-NLS-1$
		writer.write( "    Foo::bar * foobar = new Foo::SP "); //$NON-NLS-1$
		
		String code = writer.toString();
		IASTCompletionNode node = parse( code, code.indexOf( "SP" ) ); //$NON-NLS-1$
		assertEquals( node.getCompletionKind(), CompletionKind.NEW_TYPE_REFERENCE );
	}
	
	public void testBug62339() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "struct Cube { int nLength; int nWidth; int nHeight; };\n" ); //$NON-NLS-1$
		writer.write( "int main(int argc, char **argv) { struct Cube * pCube;\n" ); //$NON-NLS-1$
		writer.write( "  pCube = (str" ); //$NON-NLS-1$
		String code = writer.toString();
		IASTCompletionNode node = parse( code, code.indexOf( "(str") + 4 );  //$NON-NLS-1$
		assertNotNull( node );
		boolean foundStruct = false;
		Iterator i = node.getKeywords();
		while( i.hasNext() )
			if( ((String) i.next()).equals( "struct"))  //$NON-NLS-1$
				foundStruct = true;
		assertTrue( foundStruct );		
	}
	
	public void testBug62721() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "int f() {\n" ); //$NON-NLS-1$
		writer.write( "short blah;\n" ); //$NON-NLS-1$
		writer.write( "int x = sizeof(bl" ); //$NON-NLS-1$
		String code = writer.toString();
		IASTCompletionNode node = parse( code, code.indexOf( "of(bl") + 3); //$NON-NLS-1$
		assertNotNull( node );
		assertEquals( node.getCompletionKind(), CompletionKind.SINGLE_NAME_REFERENCE );
		assertNull( node.getCompletionContext() );
		IASTNode.LookupKind [] kinds = new IASTNode.LookupKind[1];
		kinds[0] = IASTNode.LookupKind.LOCAL_VARIABLES;
		IASTNode.ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), kinds, null, null );
		assertEquals( result.getResultsSize(), 1 );
		IASTNode blah = (IASTNode) result.getNodes().next();
		assertTrue( blah instanceof IASTVariable );
		assertEquals( ((IASTVariable)blah).getName(), "blah" ); //$NON-NLS-1$
	}
	
	public void testBug62725() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "int f() {\n" ); //$NON-NLS-1$
		writer.write( " int biSizeImage = 5;\n" ); //$NON-NLS-1$
		writer.write( "for (int i = 0; i < bi " ); //$NON-NLS-1$
		String code = writer.toString();
		IASTCompletionNode node = parse( code, code.indexOf( "< bi") + 4 ); //$NON-NLS-1$
		assertNotNull( node );
		assertEquals( node.getCompletionPrefix(), "bi"); //$NON-NLS-1$\
		assertNull( node.getCompletionContext() );
		assertTrue( node.getCompletionScope() instanceof IASTFunction );
		assertTrue( ((IASTFunction)node.getCompletionScope()).getName().equals( "f" ) ); //$NON-NLS-1$
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE );
	}
	
	public void testBug62728() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "struct Temp { char * t; };" ); //$NON-NLS-1$
		writer.write( "int f(Temp * t) {\n" ); //$NON-NLS-1$
		writer.write( "t->t[5] = t-> "); //$NON-NLS-1$
		String code = writer.toString();
		IASTCompletionNode node = parse( code, code.indexOf( "= t->") + 5 ); //$NON-NLS-1$
		assertNotNull( node );
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.MEMBER_REFERENCE );
		assertTrue( node.getCompletionScope() instanceof IASTFunction );
		assertTrue( ((IASTFunction)node.getCompletionScope()).getName().equals( "f" ) ); //$NON-NLS-1$
		assertNotNull( node.getCompletionContext() );
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
		writer.write( "e mye = bl\n"); //$NON-NLS-1$
		String code = writer.toString();
		IASTCompletionNode node = parse( code, code.indexOf( "= bl") + 4); //$NON-NLS-1$
		assertNotNull( node );
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE );
		assertEquals( node.getCompletionPrefix(), "bl"); //$NON-NLS-1$
		assertNull( node.getCompletionContext() );
		assertFalse( node.getKeywords().hasNext() );
		
		LookupKind[] kind = new LookupKind[1];
		kind[0] = LookupKind.ENUMERATORS;
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), kind, null, null );
		assertNotNull( result );
		assertEquals( result.getResultsSize(), 3 );
	}
	
	public void testBug52988() throws Exception
	{
		for( int i = 0; i < 2; ++i )
		{
			ParserLanguage language = ( i == 0 ) ? ParserLanguage.C : ParserLanguage.CPP;	
			String code = "void foo() { "; //$NON-NLS-1$
			Set kset = KeywordSets.getKeywords( KeywordSetKey.STATEMENT, language );

			validateAllKeywordsAndPrefixes( code, kset, language ); 
		}
	}

	/**
	 * @param startingCode
	 * @param keywordsToTry
	 * @param language
	 * @throws Exception
	 */
	private void validateAllKeywordsAndPrefixes(String startingCode, Set keywordsToTry, ParserLanguage language) throws Exception {
		Iterator keywordIterator = keywordsToTry.iterator();
		while( keywordIterator.hasNext() )
		{
			String keyword = (String) keywordIterator.next();
			for( int i = 0; i < keyword.length(); ++i )
			{
				String substring = keyword.subSequence( 0, i ).toString();
				String totalCode = (startingCode + substring);
				IASTCompletionNode node = parse( totalCode, totalCode.length() - 1, language );
				assertNotNull( node );
				assertTrue( "Failure on keyword=" + keyword + " prefix=" + substring, setContainsKeyword( node.getKeywords(), keyword )); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
	}

	/**
	 * @param keywords
	 * @param keyword
	 * @return
	 */
	private boolean setContainsKeyword(Iterator keywords, String keyword) {
		while( keywords.hasNext() )
		{
			if( keywords.next().equals( keyword )) return true;
		}
		return false;
	}
	
	public void testBug66543() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "struct packet { int a; int b; };\n" ); //$NON-NLS-1$
		writer.write( "struct packet buffer[5];\n" ); //$NON-NLS-1$
		writer.write( "int main(int argc, char **argv) {\n" ); //$NON-NLS-1$
		writer.write( " buffer[2]." ); //$NON-NLS-1$
		String code = writer.toString();
		IASTCompletionNode node = parse( code, code.indexOf( "[2].") + 4 ); //$NON-NLS-1$
		assertNotNull( node );
		assertNotNull( node.getCompletionContext() );
		IASTNode.LookupKind [] kinds = new LookupKind[ 1 ];
		kinds[0] = LookupKind.FIELDS;
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), kinds, node.getCompletionContext(), node.getFunctionParameters() );
		assertNotNull( result );
		assertEquals( result.getResultsSize(), 2 );
	}
	
	public void testBug69439() throws Exception
	{
		String code = "float f = 123."; //$NON-NLS-1$
		IASTCompletionNode node = parse( code, code.indexOf( ".") + 1 ); //$NON-NLS-1$
		assertNotNull( node );
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.NO_SUCH_KIND );
		
	}
	
}
