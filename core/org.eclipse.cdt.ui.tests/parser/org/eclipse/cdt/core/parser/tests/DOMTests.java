package org.eclipse.cdt.core.parser.tests;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.core.dom.BaseSpecifier;
import org.eclipse.cdt.internal.core.dom.ClassSpecifier;
import org.eclipse.cdt.internal.core.dom.DOMBuilder;
import org.eclipse.cdt.internal.core.dom.Declarator;
import org.eclipse.cdt.internal.core.dom.ParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.ParameterDeclarationClause;
import org.eclipse.cdt.internal.core.dom.SimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.TranslationUnit;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.util.DeclarationSpecifier;
import org.eclipse.cdt.internal.core.parser.util.Name;

/**
 * Tests the construction of DOMs for snippets of code
 */
public class DOMTests extends TestCase {

	public TranslationUnit parse(String code) throws Exception {
		DOMBuilder domBuilder = new DOMBuilder();
		Parser parser = new Parser(code, domBuilder);
		if( ! parser.parse() ) throw new ParserException( "Parse failure" ); 
		
		return domBuilder.getTranslationUnit();
	}
	
	/**
	 * Test code: int x;
	 * Purpose: to test the simple decaration in it's simplest form.
	 */
	public void testIntGlobal() throws Exception {
		// Parse and get the translation Unit
		TranslationUnit translationUnit = parse("int x;");
		
		// Get the simple declaration
		List declarations = translationUnit.getDeclarations();
		assertEquals(1, declarations.size());
		SimpleDeclaration declaration = (SimpleDeclaration)declarations.get(0);
		
		// Make sure it is only an int
		assertEquals(DeclarationSpecifier.t_int, declaration.getDeclSpecifier().getDeclSpecifierSeq());
		
		// Get the declarator and check its name
		List declarators = declaration.getDeclarators();
		assertEquals(1, declarators.size());
		Declarator declarator = (Declarator)declarators.get(0);
		Name name = declarator.getName();
		assertEquals("x", name.toString());
	}
	
	/**
	 * Test code: class A { } a;
	 * Purpose: tests the use of a classSpecifier in 
	 */
	public void testEmptyClass() throws Exception {
		// Parse and get the translation unit
		Writer code = new StringWriter();
		code.write("class A { } a;");
		TranslationUnit translationUnit = parse(code.toString());
		
		// Get the simple declaration
		List declarations = translationUnit.getDeclarations();
		assertEquals(1, declarations.size());
		SimpleDeclaration declaration = (SimpleDeclaration)declarations.get(0);
		
		// Make sure it is a type specifier
		assertEquals(0, declaration.getDeclSpecifier().getDeclSpecifierSeq());
		
		// Get the class specifier and check its name
		ClassSpecifier classSpecifier = (ClassSpecifier)declaration.getTypeSpecifier();
		Name className = classSpecifier.getName();
		assertEquals("A", className.toString());
		
		// Get the declarator and check it's name
		List declarators = declaration.getDeclarators();
		assertEquals(1, declarators.size());
		Declarator declarator = (Declarator)declarators.get(0);
		Name name = declarator.getName();
		assertEquals("a", name.toString());
	}

	/**
	 * Test code: class A { public: int x; };
	 * Purpose: tests a declaration in a class scope.
	 */
	public void testSimpleClassMember() throws Exception {
		// Parse and get the translaton unit
		Writer code = new StringWriter();
		code.write("class A { public: int x; };");
		TranslationUnit translationUnit = parse(code.toString());
		
		// Get the declaration
		List declarations = translationUnit.getDeclarations();
		assertEquals(1, declarations.size());
		SimpleDeclaration declaration = (SimpleDeclaration)declarations.get(0);

		// Make sure there is no declarator
		assertEquals(0, declaration.getDeclarators().size());

		// Make sure it's a type specifier
		assertEquals(0, declaration.getDeclSpecifier().getDeclSpecifierSeq());
		
		// Get the class specifier and check its name
		ClassSpecifier classSpecifier = (ClassSpecifier)declaration.getTypeSpecifier();
		Name className = classSpecifier.getName();
		assertEquals("A", className.toString());
		
		// Get the member declaration
		declarations = classSpecifier.getDeclarations();
		assertEquals(1, declarations.size());
		declaration = (SimpleDeclaration)declarations.get(0);
		
		// Make sure it's an int
		assertEquals(DeclarationSpecifier.t_int, declaration.getDeclSpecifier().getDeclSpecifierSeq());
		
		// Get the declarator and check it's name
		List declarators = declaration.getDeclarators();
		assertEquals(1, declarators.size());
		Declarator declarator = (Declarator)declarators.get(0);
		Name name = declarator.getName();
		assertEquals("x", name.toString());
	}
	/**
	 * Test code: class A : public B, private C, virtual protected D { public: int x, y; float a,b,c; }
	 * Purpose: tests a declaration in a class scope.
	 */
	public void testSimpleClassMembers() throws Exception {
		// Parse and get the translaton unit
		Writer code = new StringWriter();
		code.write("class A : public B, private C, virtual protected D { public: int x, y; float a,b,c; };");
		TranslationUnit translationUnit = parse(code.toString());
		
		// Get the declaration
		List declarations = translationUnit.getDeclarations();
		assertEquals(1, declarations.size());
		SimpleDeclaration declaration = (SimpleDeclaration)declarations.get(0);

		// Make sure there is no declarator
		assertEquals(0, declaration.getDeclarators().size());

		// Make sure it's a type specifier
		assertEquals(0, declaration.getDeclSpecifier().getDeclSpecifierSeq());
		
		// Get the class specifier and check its name
		ClassSpecifier classSpecifier = (ClassSpecifier)declaration.getTypeSpecifier();
		Name className = classSpecifier.getName();
		assertEquals("A", className.toString());
		
		List baseClasses = classSpecifier.getBaseSpecifiers();
		assertEquals( 3, baseClasses.size() );
		BaseSpecifier bs = (BaseSpecifier)baseClasses.get( 0 ); 
		assertEquals( bs.getAccess(), BaseSpecifier.t_public );
		assertEquals( bs.isVirtual(), false ); 
		assertEquals( bs.getName(), "B" ); 
		
		bs = (BaseSpecifier)baseClasses.get( 1 );
		assertEquals( bs.getAccess(), BaseSpecifier.t_private );
		assertEquals( bs.isVirtual(), false ); 
		assertEquals( bs.getName(), "C" );
		 
		bs = (BaseSpecifier)baseClasses.get( 2 );
		assertEquals( bs.getAccess(), BaseSpecifier.t_protected );
		assertEquals( bs.isVirtual(), true ); 
		assertEquals( bs.getName(), "D" ); 
		
		
		// Get the member declaration
		declarations = classSpecifier.getDeclarations();
		assertEquals(2, declarations.size());
		declaration = (SimpleDeclaration)declarations.get(0);
		
		// Make sure it's an int
		assertEquals(DeclarationSpecifier.t_int, declaration.getDeclSpecifier().getDeclSpecifierSeq());
		
		// Get the declarator and check it's name
		List declarators = declaration.getDeclarators();
		assertEquals(2, declarators.size());
		Declarator declarator = (Declarator)declarators.get(0);
		Name name = declarator.getName();
		assertEquals("x", name.toString());
		declarator = (Declarator)declarators.get(1); 
		name = declarator.getName();
		assertEquals("y", name.toString());
		
		declaration = (SimpleDeclaration)declarations.get(1); 
		// Make sure it's an float
		assertEquals(DeclarationSpecifier.t_float, declaration.getDeclSpecifier().getDeclSpecifierSeq());
		declarators = declaration.getDeclarators(); 
		assertEquals( 3, declarators.size() );
		name  = ((Declarator)declarators.get(0)).getName(); 
		assertEquals( "a", name.toString() );
		name  = ((Declarator)declarators.get(1)).getName();
		assertEquals( "b", name.toString() );
		name  = ((Declarator)declarators.get(2)).getName();		
		assertEquals( "c", name.toString() );
		
	}
	

	/**
	 * Test code: int myFunction( void ); 
	 */
	public void testSimpleFunctionDeclaration() throws Exception
	{
		// Parse and get the translaton unit
		Writer code = new StringWriter();
		code.write("void myFunction( void );");
		TranslationUnit translationUnit = parse(code.toString());
		
		// Get the declaration
		List declarations = translationUnit.getDeclarations();
		assertEquals(1, declarations.size());
		SimpleDeclaration simpleDeclaration = (SimpleDeclaration)declarations.get(0);
		assertEquals( simpleDeclaration.getDeclSpecifier().getType(), DeclarationSpecifier.t_void );
		List declarators  = simpleDeclaration.getDeclarators(); 
		assertEquals( 1, declarators.size() ); 
		Declarator functionDeclarator = (Declarator)declarators.get( 0 ); 
		assertEquals( functionDeclarator.getName().toString(), "myFunction" );
		ParameterDeclarationClause pdc = functionDeclarator.getParms(); 
		assertNotNull( pdc ); 
		List parameterDecls = pdc.getDeclarations(); 
		assertEquals( 1, parameterDecls.size() );
		ParameterDeclaration parm1 = (ParameterDeclaration)parameterDecls.get( 0 );
		assertEquals( DeclarationSpecifier.t_void, parm1.getDeclSpecifier().getType() );
		List parm1Decls = parm1.getDeclarators(); 
		assertEquals( 1, parm1Decls.size() ); 
		Declarator parm1Declarator = (Declarator) parm1Decls.get(0); 
		assertNull( parm1Declarator.getName() );  
	}
	
	/**
	 * Test code:  "class A { int floor( double input ), someInt; };"
	 */
	public void testMultipleDeclarators() throws Exception
	{
		// Parse and get the translaton unit
		Writer code = new StringWriter();
		code.write("class A { int floor( double input ), someInt; };");
		TranslationUnit translationUnit = parse(code.toString());
		
		List tudeclarations = translationUnit.getDeclarations(); 
		assertEquals( 1, tudeclarations.size() ); 
		SimpleDeclaration classDecl = (SimpleDeclaration)tudeclarations.get(0);
		assertEquals( 0, classDecl.getDeclarators().size() ); 
		ClassSpecifier classSpec = (ClassSpecifier)classDecl.getTypeSpecifier();
		 
		List classDeclarations = classSpec.getDeclarations(); 
		assertEquals( classDeclarations.size(), 1 ); 
		SimpleDeclaration simpleDeclaration = (SimpleDeclaration)classDeclarations.get(0);
		assertEquals( simpleDeclaration.getDeclSpecifier().getType(), DeclarationSpecifier.t_int );
		List simpleDeclarators =  simpleDeclaration.getDeclarators(); 
		assertEquals( simpleDeclarators.size(), 2 ); 
		Declarator methodDeclarator = (Declarator)simpleDeclarators.get(0);
		assertEquals( methodDeclarator.getName().toString(), "floor" ); 
		ParameterDeclarationClause pdc = methodDeclarator.getParms(); 
		assertNotNull( pdc );
		List parameterDeclarations = pdc.getDeclarations(); 
		assertEquals( 1, parameterDeclarations.size() ); 
		ParameterDeclaration parm1Declaration = (ParameterDeclaration)parameterDeclarations.get(0);
		assertEquals(  DeclarationSpecifier.t_double, parm1Declaration.getDeclSpecifier().getType() ); 
		List parm1Declarators = parm1Declaration.getDeclarators(); 
		assertEquals( parm1Declarators.size(), 1 ); 
		Declarator parm1Declarator = (Declarator)parm1Declarators.get(0);
		assertEquals( parm1Declarator.getName().toString(), "input" );
		Declarator integerDeclarator = (Declarator)simpleDeclarators.get(1);
		assertEquals( integerDeclarator.getName().toString(), "someInt" ); 
		assertNull( integerDeclarator.getParms() ); 
	}

//	public void testErrors()
//	{
//		validateWeEncounterAnError( "void myFunc( int hey, flo );");
//	}
 
	public void validateWeEncounterAnError( String codeText )
	{
		try
		{
			// Parse and get the translaton unit
			Writer code = new StringWriter();
			code.write(codeText);
			try
			{
				TranslationUnit translationUnit = parse(code.toString());
				fail( "We should not reach this line.  Failure."); 
			} catch( ParserException pe )
			{
			}
			catch( Exception e )
			{
				fail( "Unknown exception " + e.getMessage() );
			}
		}catch( IOException io )
		{
			fail( "IOException thrown");
		}
				
	}
}

