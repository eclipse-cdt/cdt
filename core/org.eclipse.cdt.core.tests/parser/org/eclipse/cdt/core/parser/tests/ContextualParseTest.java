/*
 * Created on Dec 8, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.core.parser.tests;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTNode.LookupResult;
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
					ParserMode.CONTEXTUAL_PARSE,
					ParserLanguage.CPP,
					callback,
					log),
				callback,
				ParserMode.CONTEXTUAL_PARSE,
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
		int index = code.indexOf( " a " );
		
		IASTCompletionNode node = parse( code, index + 2 );	
		assertNotNull( node );
		
		String prefix = node.getCompletionPrefix();
		assertNotNull( prefix );
		assertTrue( node.getCompletionScope() instanceof IASTFunction );
		assertEquals( prefix, "a" );
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE );
		
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
		kinds[0] = IASTNode.LookupKind.ALL; 
		LookupResult result = node.getCompletionScope().lookup( prefix, kinds, node.getCompletionContext() );
		assertEquals( result.getPrefix(), prefix );
		
		Iterator iter = result.getNodes();
		
		IASTVariable anotherVar = (IASTVariable) iter.next();
		IASTVariable aVar = (IASTVariable) iter.next();
				
		assertFalse( iter.hasNext() );
		assertEquals( anotherVar.getName(), "anotherVar" );
		assertEquals( aVar.getName(), "aVar" );
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
		LookupResult result = node.getCompletionScope().lookup( prefix, kinds, node.getCompletionContext() );
		assertEquals( result.getPrefix(), prefix );
		
		Iterator iter = result.getNodes();
		
		IASTMethod aMethod = null;
		IASTField aField = null;
		
		//we can't currently predict the order in this case
		for( int i = 1; i <= 2; i++ ){
			IASTNode astNode = (IASTNode) iter.next();
			if( astNode instanceof IASTMethod ){
				aMethod = (IASTMethod) astNode;
			} else{
				aField = (IASTField) astNode;
			}
		}
		
		assertFalse( iter.hasNext() );
		
		assertEquals( aMethod.getName(), "aMethod" );
		assertEquals( aField.getName(), "aField" );
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
		
		String code = writer.toString();
		int index = code.indexOf( "b->a" );
		
		IASTCompletionNode node = parse( code, index + 4 );
		
		assertNotNull( node );
		
		String prefix = node.getCompletionPrefix();
		assertEquals( prefix, "a" );
		
		assertTrue( node.getCompletionScope() instanceof IASTFunction );
		assertEquals( node.getCompletionKind(),  IASTCompletionNode.CompletionKind.MEMBER_REFERENCE );
		assertNotNull( node.getCompletionContext() );
		assertTrue( node.getCompletionContext() instanceof IASTClassSpecifier );
		
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
		kinds[0] = IASTNode.LookupKind.METHODS; 
		LookupResult result = node.getCompletionScope().lookup( prefix, kinds, node.getCompletionContext() );
		assertEquals( result.getPrefix(), prefix );
		
		Iterator iter = result.getNodes();
		IASTMethod method = (IASTMethod) iter.next();
		IASTMethod baseMethod = (IASTMethod) iter.next();
		
		assertFalse( iter.hasNext() );
		
		assertEquals( method.getName(), "aMethod" );
		assertEquals( baseMethod.getName(), "aPublicBaseMethod" );
	}
}
