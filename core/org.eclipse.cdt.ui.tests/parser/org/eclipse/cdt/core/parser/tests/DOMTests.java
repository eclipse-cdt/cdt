package org.eclipse.cdt.core.parser.tests;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.core.dom.ArrayQualifier;
import org.eclipse.cdt.internal.core.dom.BaseSpecifier;
import org.eclipse.cdt.internal.core.dom.ClassSpecifier;
import org.eclipse.cdt.internal.core.dom.DOMBuilder;
import org.eclipse.cdt.internal.core.dom.Declarator;
import org.eclipse.cdt.internal.core.dom.EnumerationSpecifier;
import org.eclipse.cdt.internal.core.dom.EnumeratorDefinition;
import org.eclipse.cdt.internal.core.dom.Expression;
import org.eclipse.cdt.internal.core.dom.LinkageSpecification;
import org.eclipse.cdt.internal.core.dom.NamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.ParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.ParameterDeclarationClause;
import org.eclipse.cdt.internal.core.dom.PointerOperator;
import org.eclipse.cdt.internal.core.dom.SimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.TranslationUnit;
import org.eclipse.cdt.internal.core.dom.UsingDeclaration;
import org.eclipse.cdt.internal.core.dom.UsingDirective;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.Token;
import org.eclipse.cdt.internal.core.parser.util.DeclSpecifier;
import org.eclipse.cdt.internal.core.parser.util.Name;

/**
 * Tests the construction of DOMs for snippets of code
 */
public class DOMTests extends TestCase {

	public DOMTests( String arg )
	{
		super( arg );
	}
	
	public TranslationUnit parse(String code) throws Exception {
		DOMBuilder domBuilder = new DOMBuilder();
		Parser parser = new Parser(code, domBuilder);
		if( ! parser.parse() ) throw new ParserException( "Parse failure" ); 
		
		return domBuilder.getTranslationUnit();
	}
	
	public void testNamespaceDefinition() throws Exception
	{
		for( int i = 0; i < 2; ++i )
		{
			TranslationUnit translationUnit; 
			if( i == 0  )
				translationUnit = parse("namespace KingJohn { int x; }");
			else
				translationUnit = parse("namespace { int x; }");
				
			List declarations = translationUnit.getDeclarations();
			assertEquals( declarations.size(), 1 );
			NamespaceDefinition namespace = (NamespaceDefinition)declarations.get(0);
			
			if( i == 0 )
				assertEquals( namespace.getName().toString(), "KingJohn" );
			else
				assertNull( namespace.getName() );
			List namespaceDeclarations = namespace.getDeclarations();
			assertEquals( namespaceDeclarations.size(), 1 );
			SimpleDeclaration simpleDec = (SimpleDeclaration)namespaceDeclarations.get(0);
			assertEquals( simpleDec.getDeclSpecifier().getType(), DeclSpecifier.t_int ); 
			List declarators = simpleDec.getDeclarators(); 
			assertEquals( declarators.size(), 1 );
			Declarator declarator = (Declarator)declarators.get(0);
			assertEquals( declarator.getName().toString(), "x");
		}
	}
	
	public void testLinkageSpecification() throws Exception
	{
		for( int i = 0; i < 2; ++i )
		{
			TranslationUnit translationUnit; 
			if( i == 0  )
				translationUnit = parse("extern \"C\" { int x(void); }");
			else
				translationUnit = parse("extern \"ADA\" int x(void);");
				
			List declarations = translationUnit.getDeclarations();
			assertEquals( declarations.size(), 1 );
			LinkageSpecification linkage = (LinkageSpecification)declarations.get(0);
			if( i == 0 )
				assertEquals( "C", linkage.getLanguageLinkage() );
			else
				assertEquals( "ADA", linkage.getLanguageLinkage() );
			
			List subDeclarations = linkage.getDeclarations();
			assertEquals( subDeclarations.size(), 1 );
			
			SimpleDeclaration simpleDec = (SimpleDeclaration)subDeclarations.get(0);
			assertEquals( simpleDec.getDeclSpecifier().getType(), DeclSpecifier.t_int );
			List declarators = simpleDec.getDeclarators();
			assertEquals( declarators.size(), 1 );
			Declarator declarator = (Declarator)declarators.get(0);
			assertEquals( declarator.getName().toString(), "x" );
			assertNotNull( declarator.getParms() );
		}
		
	}
	
	public void testEnumSpecifier() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "enum { yo, go = 3, away };\n");
		code.write( "enum hasAName { last = 666 };");
		TranslationUnit translationUnit = parse( code.toString() );
		List declarations = translationUnit.getDeclarations(); 
		assertEquals( declarations.size(), 2 );
		
		SimpleDeclaration declaration1 = (SimpleDeclaration)declarations.get(0);
		EnumerationSpecifier enumSpecifier = (EnumerationSpecifier)declaration1.getTypeSpecifier();
		assertNull( enumSpecifier.getName() ); 
		List firstEnumItems = enumSpecifier.getEnumeratorDefinitions();
		assertEquals( 3, firstEnumItems.size());
		EnumeratorDefinition enumDef1_1 = (EnumeratorDefinition)firstEnumItems.get(0);
		assertEquals( enumDef1_1.getName().toString(), "yo" );
		assertNull( enumDef1_1.getExpression() );

		EnumeratorDefinition enumDef1_2 = (EnumeratorDefinition)firstEnumItems.get(1);
		assertEquals( enumDef1_2.getName().toString(), "go" );
		assertNotNull( enumDef1_2.getExpression() );
		
		EnumeratorDefinition enumDef1_3 = (EnumeratorDefinition)firstEnumItems.get(2);
		assertEquals( enumDef1_3.getName().toString(), "away" );
		assertNull( enumDef1_3.getExpression() );

		SimpleDeclaration declaration2 = (SimpleDeclaration)declarations.get(1);
		EnumerationSpecifier enumSpecifier2 = (EnumerationSpecifier)declaration2.getTypeSpecifier();
		assertEquals( enumSpecifier2.getName().toString(), "hasAName" ); 
		
	}
	public void testUsingClauses() throws Exception
	{
		Writer code = new StringWriter();
		
		code.write("using namespace A::B::C;\n");
		code.write("using namespace C;\n");
		code.write("using B::f;\n");
		code.write("using ::f;\n");
		code.write("using typename crap::de::crap;");
		TranslationUnit translationUnit = parse(code.toString());
		
		List declarations = translationUnit.getDeclarations(); 
		assertEquals( declarations.size(), 5 );
		
		UsingDirective first, second; 
		UsingDeclaration third, fourth, fifth; 
		
		first = (UsingDirective) declarations.get(0);
		assertEquals( first.getNamespaceName().toString(), "A::B::C" ); 
		
		second = (UsingDirective) declarations.get(1);
		assertEquals( second.getNamespaceName().toString(), "C" ); 
		
		third = (UsingDeclaration) declarations.get(2);
		assertEquals( third.getMappedName().toString(), "B::f" );
		assertFalse( third.isTypename() ); 
		
		fourth = (UsingDeclaration) declarations.get(3);
		assertEquals( fourth.getMappedName().toString(), "::f" );
		assertFalse( fourth.isTypename() ); 
		
		fifth = (UsingDeclaration) declarations.get(4);
		assertTrue( fifth.isTypename() );
		assertEquals( fifth.getMappedName().toString(), "crap::de::crap" );
	}
	
	public void testDeclSpecifier() throws Exception
	{
		DeclSpecifier d = new DeclSpecifier();
		d.setTypedef( true ); 
		assertTrue( d.isTypedef() );
		d.setTypedef( false ); 
		assertFalse( d.isTypedef() ); 
		d.setAuto(true);
		assertTrue( d.isAuto() ); 
		d.setAuto(false);
		assertFalse( d.isAuto());
		d.setRegister(true); 
		assertTrue( d.isRegister() );
		d.setRegister(false); 
		assertFalse( d.isRegister() );
		d.setStatic(true);
		assertTrue( d.isStatic() );
		d.setStatic(false);
		assertFalse( d.isStatic() );
		 
		d.setExtern(true);
		assertTrue( d.isExtern() );
		d.setExtern(false);
		assertFalse( d.isExtern() );
	 
		d.setMutable(true); 
		assertTrue( d.isMutable() );
		d.setMutable(false); 
		assertFalse( d.isMutable() );
	
		d.setInline(true);
		assertTrue( d.isInline() );
		d.setInline(false);
		assertFalse( d.isInline() );
	
		d.setVirtual(true); 
		assertTrue( d.isVirtual() );
		d.setVirtual(false); 
		assertFalse( d.isVirtual() );
	
		d.setExplicit(true); 
		assertTrue( d.isExplicit() );
		d.setExplicit(false); 
		assertFalse( d.isExplicit() );
	
		d.setTypedef(true);
		assertTrue( d.isTypedef() );
		d.setTypedef(false);
		assertFalse( d.isTypedef() );
	
		d.setFriend(true);
		assertTrue( d.isFriend()); 
		d.setFriend(false);
		assertFalse( d.isFriend()); 
	
		d.setConst(true);
		assertTrue( d.isConst() );
		d.setConst(false);
		assertFalse( d.isConst() );
	
		d.setVolatile(true); 
		assertTrue( d.isVolatile() );
		d.setVolatile(false); 
		assertFalse( d.isVolatile() );

		d.setUnsigned(true);
		assertTrue( d.isUnsigned()); 
		d.setUnsigned(false);
		assertFalse( d.isUnsigned()); 
	 
		d.setShort(true);
		assertTrue( d.isShort()); 
		d.setShort(false);
		assertFalse( d.isShort()); 
		
		d.setLong(true);
		assertTrue( d.isLong() );
		d.setLong(false);
		assertFalse( d.isLong() ); 

		for( int i = 0; i <= 7; ++i )
		{ 
			d.setType( i ); 
			for( int j = 0; j <= 7; ++j )
			{
				if( j == i )
					assertTrue( d.getType() == j ); 
				else
					assertFalse( d.getType() == j );
			}
		}
 
	}
	
	/**
	 * Test code: int x = 5;
	 * Purpose: to test the simple decaration in it's simplest form.
	 */
	public void testIntGlobal() throws Exception {
		// Parse and get the translation Unit
		TranslationUnit translationUnit = parse("int x = 5;");
		
		// Get the simple declaration
		List declarations = translationUnit.getDeclarations();
		assertEquals(1, declarations.size());
		SimpleDeclaration declaration = (SimpleDeclaration)declarations.get(0);
		
		// Make sure it is only an int
		assertEquals(DeclSpecifier.t_int, declaration.getDeclSpecifier().getDeclSpecifierSeq());
		
		// Get the declarator and check its name
		List declarators = declaration.getDeclarators();
		assertEquals(1, declarators.size());
		Declarator declarator = (Declarator)declarators.get(0);
		Name name = declarator.getName();
		assertEquals("x", name.toString());
		
		Expression exp = declarator.getExpression(); 
		assertNotNull( exp );
		assertEquals( 1, exp.tokens().size() ); 
		Token t = (Token)exp.tokens().get(0); 
		assertEquals( t.getImage(), "5" );
		assertEquals( t.getType(), Token.tINTEGER);
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
		assertEquals(DeclSpecifier.t_int, declaration.getDeclSpecifier().getDeclSpecifierSeq());
		
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
		assertEquals( bs.getName().toString(), "B" ); 
		
		bs = (BaseSpecifier)baseClasses.get( 1 );
		assertEquals( bs.getAccess(), BaseSpecifier.t_private );
		assertEquals( bs.isVirtual(), false ); 
		assertEquals( bs.getName().toString(), "C" );
		 
		bs = (BaseSpecifier)baseClasses.get( 2 );
		assertEquals( bs.getAccess(), BaseSpecifier.t_protected );
		assertEquals( bs.isVirtual(), true ); 
		assertEquals( bs.getName().toString(), "D" ); 
		
		
		// Get the member declaration
		declarations = classSpecifier.getDeclarations();
		assertEquals(2, declarations.size());
		declaration = (SimpleDeclaration)declarations.get(0);
		
		// Make sure it's an int
		assertEquals(DeclSpecifier.t_int, declaration.getDeclSpecifier().getDeclSpecifierSeq());
		
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
		assertEquals(DeclSpecifier.t_float, declaration.getDeclSpecifier().getDeclSpecifierSeq());
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
		assertEquals( simpleDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_void );
		List declarators  = simpleDeclaration.getDeclarators(); 
		assertEquals( 1, declarators.size() ); 
		Declarator functionDeclarator = (Declarator)declarators.get( 0 ); 
		assertEquals( functionDeclarator.getName().toString(), "myFunction" );
		ParameterDeclarationClause pdc = functionDeclarator.getParms(); 
		assertNotNull( pdc ); 
		List parameterDecls = pdc.getDeclarations(); 
		assertEquals( 1, parameterDecls.size() );
		ParameterDeclaration parm1 = (ParameterDeclaration)parameterDecls.get( 0 );
		assertEquals( DeclSpecifier.t_void, parm1.getDeclSpecifier().getType() );
		List parm1Decls = parm1.getDeclarators(); 
		assertEquals( 1, parm1Decls.size() ); 
		Declarator parm1Declarator = (Declarator) parm1Decls.get(0); 
		assertNull( parm1Declarator.getName() );  
	}
	
	/**
	 * Test code: bool myFunction( int parm1 = 3 * 4, double parm2 );
	 * @throws Exception
	 */
	public void testFunctionDeclarationWithParameters() throws Exception
	{
		// Parse and get the translaton unit
		Writer code = new StringWriter();
		code.write("bool myFunction( int parm1 = 3 * 4, double parm2 );");
		TranslationUnit translationUnit = parse(code.toString());

		// Get the declaration
		List declarations = translationUnit.getDeclarations();
		assertEquals(1, declarations.size());
		SimpleDeclaration simpleDeclaration = (SimpleDeclaration)declarations.get(0);
		assertEquals( simpleDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_bool );
		List declarators  = simpleDeclaration.getDeclarators(); 
		assertEquals( 1, declarators.size() ); 
		Declarator functionDeclarator = (Declarator)declarators.get( 0 ); 
		assertEquals( functionDeclarator.getName().toString(), "myFunction" );
		ParameterDeclarationClause pdc = functionDeclarator.getParms(); 
		assertNotNull( pdc ); 
		List parameterDecls = pdc.getDeclarations(); 
		assertEquals( 2, parameterDecls.size() );
		ParameterDeclaration parm1 = (ParameterDeclaration)parameterDecls.get( 0 );
		assertEquals( DeclSpecifier.t_int, parm1.getDeclSpecifier().getType() );
		List parm1Decls = parm1.getDeclarators(); 
		assertEquals( 1, parm1Decls.size() ); 
		Declarator parm1Declarator = (Declarator) parm1Decls.get(0); 
		assertEquals( "parm1", parm1Declarator.getName().toString() );
		Expression initialValueParm1 = parm1Declarator.getExpression();
		assertEquals( initialValueParm1.tokens().size(), 3 );
		Token t1 = (Token)initialValueParm1.tokens().get( 0 );
		Token t2 = (Token)initialValueParm1.tokens().get( 1 ); 
		Token t3 = (Token)initialValueParm1.tokens().get( 2 );
		assertEquals( t1.getType(), Token.tINTEGER );
		assertEquals( t1.getImage(), "3" ); 
		assertEquals( t3.getType(), Token.tSTAR ); 
		assertEquals( t2.getType(), Token.tINTEGER );
		assertEquals( t2.getImage(), "4" );   

		ParameterDeclaration parm2 = (ParameterDeclaration)parameterDecls.get( 1 );
		assertEquals( DeclSpecifier.t_double, parm2.getDeclSpecifier().getType() );
		List parm2Decls = parm2.getDeclarators(); 
		assertEquals( 1, parm2Decls.size() ); 
		Declarator parm2Declarator = (Declarator) parm2Decls.get(0); 
		assertEquals( "parm2", parm2Declarator.getName().toString() );  
		
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
		assertEquals( simpleDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_int );
		List simpleDeclarators =  simpleDeclaration.getDeclarators(); 
		assertEquals( simpleDeclarators.size(), 2 ); 
		Declarator methodDeclarator = (Declarator)simpleDeclarators.get(0);
		assertEquals( methodDeclarator.getName().toString(), "floor" ); 
		ParameterDeclarationClause pdc = methodDeclarator.getParms(); 
		assertNotNull( pdc );
		List parameterDeclarations = pdc.getDeclarations(); 
		assertEquals( 1, parameterDeclarations.size() ); 
		ParameterDeclaration parm1Declaration = (ParameterDeclaration)parameterDeclarations.get(0);
		assertEquals(  DeclSpecifier.t_double, parm1Declaration.getDeclSpecifier().getType() ); 
		List parm1Declarators = parm1Declaration.getDeclarators(); 
		assertEquals( parm1Declarators.size(), 1 ); 
		Declarator parm1Declarator = (Declarator)parm1Declarators.get(0);
		assertEquals( parm1Declarator.getName().toString(), "input" );
		Declarator integerDeclarator = (Declarator)simpleDeclarators.get(1);
		assertEquals( integerDeclarator.getName().toString(), "someInt" ); 
		assertNull( integerDeclarator.getParms() ); 
	}

	public void testFunctionModifiers() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "void foo( void ) const throw ( yay, nay, we::dont::care );");
		TranslationUnit translationUnit = parse( code.toString() );
		List tudeclarations = translationUnit.getDeclarations(); 
		assertEquals( 1, tudeclarations.size() ); 
		SimpleDeclaration decl1 = (SimpleDeclaration)tudeclarations.get(0);
		assertEquals( decl1.getDeclSpecifier().getType(), DeclSpecifier.t_void);
		assertEquals( decl1.getDeclarators().size(), 1 );
		Declarator declarator = (Declarator)decl1.getDeclarators().get(0);
		assertEquals( declarator.getName().toString(), "foo");
		assertTrue( declarator.isConst() ); 
		assertFalse( declarator.isVolatile() );
		List exceptions = declarator.getExceptionSpecifier(); 
		assertEquals( exceptions.size(), 3 );
		Name n = (Name)exceptions.get(0); 
		assertEquals( n.toString(), "yay");
		n = (Name)exceptions.get(1);
		assertEquals( n.toString(), "nay");
		n = (Name)exceptions.get(2);
		assertEquals( n.toString(), "we::dont::care");
	}


	public void testArrays() throws Exception
	{
		// Parse and get the translaton unit
		Writer code = new StringWriter();
		code.write("int x [5][];");
		TranslationUnit translationUnit = parse( code.toString() );
		List tudeclarations = translationUnit.getDeclarations(); 
		assertEquals( 1, tudeclarations.size() ); 
		SimpleDeclaration decl1 = (SimpleDeclaration)tudeclarations.get(0);
		assertEquals( decl1.getDeclSpecifier().getType(), DeclSpecifier.t_int);
		assertEquals( decl1.getDeclarators().size(), 1 );
		Declarator declarator = (Declarator)decl1.getDeclarators().get(0);
		assertEquals( declarator.getName().toString(), "x");
		List arrayQualifiers = declarator.getArrayQualifiers(); 
		assertEquals( 2, arrayQualifiers.size() ); 
		ArrayQualifier q1 =(ArrayQualifier)arrayQualifiers.get(0);
		assertNotNull( q1.getExpression() ); 
		List tokens = q1.getExpression().tokens();
		assertEquals( tokens.size(), 1 ); 
		ArrayQualifier q2 =(ArrayQualifier)arrayQualifiers.get(1);  
		assertNull( q2.getExpression() ); 
	}		

	public void testPointerOperators() throws Exception
	{
		// Parse and get the translaton unit
		Writer code = new StringWriter();
		code.write("int * x = 0, & y, * const * const volatile * z;");
		TranslationUnit translationUnit = parse(code.toString());
		
		List tudeclarations = translationUnit.getDeclarations(); 
		assertEquals( 1, tudeclarations.size() ); 
		SimpleDeclaration decl1 = (SimpleDeclaration)tudeclarations.get(0);
		assertEquals( decl1.getDeclSpecifier().getType(), DeclSpecifier.t_int);
		
		assertEquals( 3, decl1.getDeclarators().size() ); 
		
		Declarator declarator1 = (Declarator)decl1.getDeclarators().get( 0 );
		assertEquals( declarator1.getName().toString(), "x" );
		Expression initValue1  = declarator1.getExpression();
		assertEquals( initValue1.tokens().size(), 1 );
		List ptrOps1 = declarator1.getPointerOperators();
		assertNotNull( ptrOps1 );
		assertEquals( 1, ptrOps1.size() );
		PointerOperator po1 = (PointerOperator)ptrOps1.get(0);
		assertNotNull( po1 ); 
		assertFalse( po1.isConst() );
		assertFalse( po1.isVolatile() );
		assertEquals( po1.getType(), PointerOperator.t_pointer );
		Token t1 = (Token)initValue1.tokens().get(0);
		assertEquals( t1.getType(), Token.tINTEGER ); 
		assertEquals( t1.getImage(), "0");

		Declarator declarator2 = (Declarator)decl1.getDeclarators().get( 1 );
		assertEquals( declarator2.getName().toString(), "y" );
		assertNull( declarator2.getExpression() ); 
		List ptrOps2 = declarator2.getPointerOperators();
		assertNotNull( ptrOps2 );
		assertEquals( 1, ptrOps2.size() );
		PointerOperator po2 = (PointerOperator)ptrOps2.get(0);
		assertNotNull( po2 ); 
		assertFalse( po2.isConst() );
		assertFalse( po2.isVolatile() );
		assertEquals( po2.getType(), PointerOperator.t_reference );
		
		Declarator declarator3 = (Declarator)decl1.getDeclarators().get( 2 );
		assertEquals( "z", declarator3.getName().toString() );
		List ptrOps3 = declarator3.getPointerOperators();
		assertNotNull( ptrOps3 );
		assertEquals( 3, ptrOps3.size() );
		
		//* const  
		PointerOperator po3 = (PointerOperator)ptrOps3.get(0);
		assertNotNull( po3 );
		assertTrue( po3.isConst() ); 
		assertFalse( po3.isVolatile() ); 
		assertEquals( po3.getType(), PointerOperator.t_pointer );
		// * const volatile
		PointerOperator po4 = (PointerOperator)ptrOps3.get(1);
		assertNotNull( po4 );
		assertEquals( po4.getType(), PointerOperator.t_pointer );
		assertTrue( po4.isConst() ); 
		assertTrue( po4.isVolatile() ); 
		// *
		PointerOperator po5 = (PointerOperator)ptrOps3.get(2);
		assertNotNull( po5 );
		assertFalse( po5.isConst() ); 
		assertFalse( po5.isVolatile() ); 
		assertEquals( po5.getType(), PointerOperator.t_pointer );
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

