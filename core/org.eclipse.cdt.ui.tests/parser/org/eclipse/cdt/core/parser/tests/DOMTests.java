package org.eclipse.cdt.core.parser.tests;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.core.dom.ASMDefinition;
import org.eclipse.cdt.internal.core.dom.AccessSpecifier;
import org.eclipse.cdt.internal.core.dom.ArrayQualifier;
import org.eclipse.cdt.internal.core.dom.BaseSpecifier;
import org.eclipse.cdt.internal.core.dom.ClassKey;
import org.eclipse.cdt.internal.core.dom.ClassSpecifier;
import org.eclipse.cdt.internal.core.dom.ConstructorChain;
import org.eclipse.cdt.internal.core.dom.ConstructorChainElement;
import org.eclipse.cdt.internal.core.dom.ConstructorChainElementExpression;
import org.eclipse.cdt.internal.core.dom.DOMBuilder;
import org.eclipse.cdt.internal.core.dom.DOMFactory;
import org.eclipse.cdt.internal.core.dom.DeclSpecifier;
import org.eclipse.cdt.internal.core.dom.Declarator;
import org.eclipse.cdt.internal.core.dom.ElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.EnumerationSpecifier;
import org.eclipse.cdt.internal.core.dom.EnumeratorDefinition;
import org.eclipse.cdt.internal.core.dom.ExceptionSpecifier;
import org.eclipse.cdt.internal.core.dom.ExplicitTemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.Expression;
import org.eclipse.cdt.internal.core.dom.Inclusion;
import org.eclipse.cdt.internal.core.dom.LinkageSpecification;
import org.eclipse.cdt.internal.core.dom.Macro;
import org.eclipse.cdt.internal.core.dom.Name;
import org.eclipse.cdt.internal.core.dom.NamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.ParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.ParameterDeclarationClause;
import org.eclipse.cdt.internal.core.dom.PointerOperator;
import org.eclipse.cdt.internal.core.dom.SimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.TemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.TemplateParameter;
import org.eclipse.cdt.internal.core.dom.TemplateParameterList;
import org.eclipse.cdt.internal.core.dom.TranslationUnit;
import org.eclipse.cdt.internal.core.dom.UsingDeclaration;
import org.eclipse.cdt.internal.core.dom.UsingDirective;
import org.eclipse.cdt.internal.core.parser.IParser;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.Token;

public class DOMTests extends TestCase {

	public DOMTests( String arg )
	{
		super( arg );
	}
	
	public TranslationUnit parse( String code ) throws Exception
	{
		return parse( code, false, true );
	}
	
	public TranslationUnit parse(String code, boolean quickParse, boolean throwOnError ) throws Exception {
		DOMBuilder domBuilder = DOMFactory.createDOMBuilder(false); 
		IParser parser = new Parser(code, domBuilder, quickParse );
		if( ! parser.parse() )
			if( throwOnError ) throw new ParserException( "Parse failure" );
			else domBuilder.getTranslationUnit().setParseSuccessful( false ); 
		
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
	
	public void testTemplateSpecialization() throws Exception
	{
		TranslationUnit tu = parse( "template<> class stream<char> { /* ... */ };");
		assertEquals( tu.getDeclarations().size(), 1 ); 
		ExplicitTemplateDeclaration explicit = (ExplicitTemplateDeclaration)tu.getDeclarations().get( 0 );
		assertNotNull( explicit ); 
		assertEquals( explicit.getKind(), ExplicitTemplateDeclaration.k_specialization );
		assertEquals( explicit.getDeclarations().size(), 1 );
		SimpleDeclaration declaration = (SimpleDeclaration)explicit.getDeclarations().get(0);
		assertNotNull( declaration );
		ClassSpecifier classSpec = (ClassSpecifier)declaration.getTypeSpecifier();
		assertNotNull( classSpec );
		assertEquals( classSpec.getClassKey(), ClassKey.t_class );
		assertEquals( classSpec.getName().toString(), "stream<char>" );
		assertEquals( declaration.getDeclarators().size(), 0 );
		assertEquals( classSpec.getDeclarations().size(), 0 );
		
	}
	
	public void testTemplateInstantiation() throws Exception
	{
		TranslationUnit tu = parse( "template class Array<char>;");
		assertEquals( tu.getDeclarations().size(), 1 );
		ExplicitTemplateDeclaration explicit = (ExplicitTemplateDeclaration)tu.getDeclarations().get( 0 );
		assertNotNull( explicit );
		assertEquals( explicit.getKind(), ExplicitTemplateDeclaration.k_instantiation ); 
		assertEquals( explicit.getDeclarations().size(), 1 );
		SimpleDeclaration declaration = (SimpleDeclaration)explicit.getDeclarations().get(0);
		assertNotNull( declaration );
		ElaboratedTypeSpecifier classSpec = (ElaboratedTypeSpecifier)declaration.getTypeSpecifier();
		assertNotNull( classSpec );
		assertEquals( classSpec.getClassKey(), ClassKey.t_class );
		assertEquals( classSpec.getName().toString(), "Array<char>");	
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
	
	public void testTypedef() throws Exception
	{
		TranslationUnit tu = parse( "typedef const struct A * const cpStructA;");
		assertEquals( tu.getDeclarations().size(), 1 );
		SimpleDeclaration declaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertTrue( declaration.getDeclSpecifier().isTypedef() );
		assertTrue( declaration.getDeclSpecifier().isConst() );
		ElaboratedTypeSpecifier elab = (ElaboratedTypeSpecifier) declaration.getTypeSpecifier();
		assertEquals( elab.getClassKey(), ClassKey.t_struct ); 
		assertEquals( elab.getName().toString(), "A" );
		List declarators = declaration.getDeclarators(); 
		assertEquals( declarators.size(), 1 );
		Declarator declarator = (Declarator)declarators.get(0);
		assertEquals( declarator.getName().toString(), "cpStructA");
		assertEquals( declarator.getPointerOperators().size(), 1 );
		PointerOperator po = (PointerOperator)declarator.getPointerOperators().get(0);
		assertEquals( po.getType(), PointerOperator.t_pointer);
		assertTrue( po.isConst() ); 
		assertFalse( po.isVolatile());
		
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
		assertEquals( 1, exp.elements().size() ); 
		Token t = (Token)exp.elements().get(0); 
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
		assertEquals( bs.getAccess(), AccessSpecifier.v_public );
		assertEquals( bs.isVirtual(), false ); 
		assertEquals( bs.getName().toString(), "B" ); 
		
		bs = (BaseSpecifier)baseClasses.get( 1 );
		assertEquals( bs.getAccess(), AccessSpecifier.v_private );
		assertEquals( bs.isVirtual(), false ); 
		assertEquals( bs.getName().toString(), "C" );
		 
		bs = (BaseSpecifier)baseClasses.get( 2 );
		assertEquals( bs.getAccess(), AccessSpecifier.v_protected );
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
		assertEquals( initialValueParm1.elements().size(), 3 );
		Token t1 = (Token)initialValueParm1.elements().get( 0 );
		Token t2 = (Token)initialValueParm1.elements().get( 1 ); 
		Token t3 = (Token)initialValueParm1.elements().get( 2 );
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
		code.write( "virtual void foo( void ) const throw ( yay, nay, we::dont::care ) = 0;");
		TranslationUnit translationUnit = parse( code.toString() );
		List tudeclarations = translationUnit.getDeclarations(); 
		assertEquals( 1, tudeclarations.size() ); 
		SimpleDeclaration decl1 = (SimpleDeclaration)tudeclarations.get(0);
		assertEquals( decl1.getDeclSpecifier().getType(), DeclSpecifier.t_void);
		assertTrue( decl1.getDeclSpecifier().isVirtual() );
		assertEquals( decl1.getDeclarators().size(), 1 );
		Declarator declarator = (Declarator)decl1.getDeclarators().get(0);
		assertEquals( declarator.getName().toString(), "foo");
		assertTrue( declarator.isConst() ); 
		assertFalse( declarator.isVolatile() );
		ExceptionSpecifier exceptions = declarator.getExceptionSpecifier(); 
		List typenames = exceptions.getTypeNames();
		assertEquals( typenames.size(), 3 );
		Name n = (Name)typenames.get(0); 
		assertEquals( n.toString(), "yay");
		n = (Name)typenames.get(1);
		assertEquals( n.toString(), "nay");
		n = (Name)typenames.get(2);
		assertEquals( n.toString(), "we::dont::care");
		assertTrue( declarator.isPureVirtual() );
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
		List tokens = q1.getExpression().elements();
		assertEquals( tokens.size(), 1 ); 
		ArrayQualifier q2 =(ArrayQualifier)arrayQualifiers.get(1);  
		assertNull( q2.getExpression() ); 
	}		

	public void testElaboratedParms() throws Exception
	{
		TranslationUnit tu = parse( "int x( struct A myA ) { /* junk */ }", true, true);
		assertEquals( tu.getDeclarations().size(), 1 );
		SimpleDeclaration declaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertEquals( declaration.getDeclSpecifier().getType(), DeclSpecifier.t_int );
		assertEquals( declaration.getDeclarators().size(), 1 );
		Declarator declarator = (Declarator)declaration.getDeclarators().get(0);
		assertEquals( declarator.getName().toString(), "x" );
		assertTrue( declaration.isFunctionDefinition() );
		assertEquals( declarator.getParms().getDeclarations().size(), 1 );
		ParameterDeclaration parm = (ParameterDeclaration)declarator.getParms().getDeclarations().get(0);
		ElaboratedTypeSpecifier typeSpec = (ElaboratedTypeSpecifier)parm.getTypeSpecifier();
		assertEquals( typeSpec.getClassKey(), ClassKey.t_struct );
		assertEquals( typeSpec.getName().toString(), "A" );
		assertEquals( parm.getDeclarators().size(), 1 );
		Declarator subDeclarator = (Declarator)parm.getDeclarators().get(0);
		assertEquals( subDeclarator.getName().toString(), "myA" );
		
	}

	public void testPreprocessor() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "#include <stdio.h>\n#define DEF VALUE\n");
		TranslationUnit tu = parse( code.toString(), true, true );
		assertEquals( tu.getInclusions().size(), 1 ); 
		Inclusion i = (Inclusion)tu.getInclusions().get(0);
		assertEquals( i.getName(), "stdio.h");
		assertEquals( i.getStartingOffset(), 0 ); 
		assertEquals( i.getNameLength(), 7 ); 
		assertEquals( i.getNameOffset(), 10 ); 
		assertEquals( i.getTotalLength(), 18 );
		
		assertEquals( tu.getMacros().size(), 1 );
		Macro m = (Macro)tu.getMacros().get(0);
		assertEquals( m.getName(), "DEF" ); 
		assertEquals( m.getStartingOffset(), 19 );
		assertEquals( m.getNameLength(), 3 );
		assertEquals( m.getNameOffset(), 27 );
		assertEquals( m.getTotalLength(), 18 );
	}

	public void testMemberDeclarations() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "class A {\n" ); 
		code.write( "public:\n");
		code.write( " int isPublic;\n" );
		code.write( "private:\n");
		code.write( " int isPrivate;\n" );
		code.write( "protected:\n");
		code.write( " int isProtected;\n" );
		code.write( "};");
		TranslationUnit translationUnit = parse( code.toString() );
		assertEquals( translationUnit.getDeclarations().size(), 1 );
		SimpleDeclaration classDeclaration = (SimpleDeclaration)
			translationUnit.getDeclarations().get(0);
		assertEquals( classDeclaration.getDeclarators().size(), 0 );
		ClassSpecifier classSpec = (ClassSpecifier)classDeclaration.getTypeSpecifier();
		assertEquals( "A", classSpec.getName().toString() );
		assertEquals( 3, classSpec.getDeclarations().size());
		for( int i = 0; i < 3; ++i )
		{
			SimpleDeclaration subDecl = (SimpleDeclaration)classSpec.getDeclarations().get( i );
			int visibility = AccessSpecifier.v_unknown;
			
			switch( i )
			{
				case 0:
					visibility = AccessSpecifier.v_public;
					break;
				case 1:
					visibility = AccessSpecifier.v_private;
					break;
				case 2:
					visibility = AccessSpecifier.v_protected;
					break;
				default:
					break;
			}
			
			assertEquals( visibility, subDecl.getAccessSpecifier().getAccess() );
		}
				
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
		assertEquals( initValue1.elements().size(), 1 );
		List ptrOps1 = declarator1.getPointerOperators();
		assertNotNull( ptrOps1 );
		assertEquals( 1, ptrOps1.size() );
		PointerOperator po1 = (PointerOperator)ptrOps1.get(0);
		assertNotNull( po1 ); 
		assertFalse( po1.isConst() );
		assertFalse( po1.isVolatile() );
		assertEquals( po1.getType(), PointerOperator.t_pointer );
		Token t1 = (Token)initValue1.elements().get(0);
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
	
	public void testBug26467() throws Exception
	{
		StringWriter code = new StringWriter(); 
		code.write(	"struct foo { int fooInt; char fooChar;	};\n" );
		code.write( "typedef struct foo fooStruct;\n" );
		code.write( "typedef struct { int anonInt; char anonChar; } anonStruct;\n" );
		
		TranslationUnit tu = parse( code.toString() );
		List tuDeclarations = tu.getDeclarations(); 
		assertEquals( tuDeclarations.size(), 3 );
		
		SimpleDeclaration declaration = (SimpleDeclaration)tuDeclarations.get(0);
		ClassSpecifier classSpec = (ClassSpecifier)declaration.getTypeSpecifier();
		assertEquals( declaration.getDeclarators().size(), 0 );
		assertEquals( classSpec.getClassKey(), ClassKey.t_struct);
		assertEquals( classSpec.getName().toString(), "foo");
		List subDeclarations = classSpec.getDeclarations();
		assertEquals( subDeclarations.size(), 2 );
		SimpleDeclaration subDeclaration = (SimpleDeclaration)subDeclarations.get(0);
		assertEquals( subDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_int);
		assertEquals( subDeclaration.getDeclarators().size(), 1 );
		assertEquals( ((Declarator)subDeclaration.getDeclarators().get(0)).getName().toString(), "fooInt" ); 
		subDeclaration = (SimpleDeclaration)subDeclarations.get(1);
		assertEquals( subDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_char);
		assertEquals( subDeclaration.getDeclarators().size(), 1 );
		assertEquals( ((Declarator)subDeclaration.getDeclarators().get(0)).getName().toString(), "fooChar" ); 
				
		declaration = (SimpleDeclaration)tuDeclarations.get(1);
		assertEquals( declaration.getDeclSpecifier().isTypedef(), true ); 
		ElaboratedTypeSpecifier elab = (ElaboratedTypeSpecifier)declaration.getTypeSpecifier();
		assertEquals( elab.getClassKey(), ClassKey.t_struct);
		assertEquals( elab.getName().toString(), "foo" );
		assertEquals( declaration.getDeclarators().size(), 1 );
		assertEquals( ((Declarator)declaration.getDeclarators().get(0)).getName().toString(), "fooStruct" );
		
		declaration = (SimpleDeclaration)tuDeclarations.get(2);
		assertEquals( declaration.getDeclSpecifier().isTypedef(), true ); 
		classSpec = (ClassSpecifier) declaration.getTypeSpecifier();
		assertEquals( classSpec.getClassKey(), ClassKey.t_struct );
		assertNull( classSpec.getName() ); 
		subDeclarations = classSpec.getDeclarations();
		assertEquals( subDeclarations.size(), 2 ); 
		subDeclaration = (SimpleDeclaration)subDeclarations.get(0);
		assertEquals( subDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_int);
		assertEquals( subDeclaration.getDeclarators().size(), 1 );
		assertEquals( ((Declarator)subDeclaration.getDeclarators().get(0)).getName().toString(), "anonInt" ); 
		subDeclaration = (SimpleDeclaration)subDeclarations.get(1);
		assertEquals( subDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_char);
		assertEquals( subDeclaration.getDeclarators().size(), 1 );
		assertEquals( ((Declarator)subDeclaration.getDeclarators().get(0)).getName().toString(), "anonChar" ); 
		assertEquals( declaration.getDeclarators().size(), 1 );
		assertEquals( ((Declarator)declaration.getDeclarators().get(0)).getName().toString(), "anonStruct" );	
	}
	
	public void testASMDefinition() throws Exception
	{
		TranslationUnit tu = parse( "asm( \"mov ep1 ds2\");" );
		assertEquals( tu.getDeclarations().size(), 1 );
		ASMDefinition asm = (ASMDefinition)tu.getDeclarations().get(0);
		assertEquals( asm.getAssemblyCode(), "mov ep1 ds2" );
	}
	
	public void testConstructorChain() throws Exception
	{
		TranslationUnit tu = parse( "TrafficLight_Actor::TrafficLight_Actor( RTController * rtg_rts, RTActorRef * rtg_ref )	: RTActor( rtg_rts, rtg_ref ), myId( 0 ) {}" );
		List tuDeclarations = tu.getDeclarations(); 
		assertEquals( tuDeclarations.size(), 1 );
		SimpleDeclaration decl1 = (SimpleDeclaration)tuDeclarations.get(0);
		List declarators1 = decl1.getDeclarators();
		assertEquals( declarators1.size(), 1 );
		Declarator declarator1 = (Declarator)declarators1.get(0);
		assertEquals( declarator1.getName().toString(), "TrafficLight_Actor::TrafficLight_Actor");
		ConstructorChain chain1 = declarator1.getCtorChain(); 
		List chainElements1 = chain1.getChainElements();
		assertEquals( chainElements1.size(), 2 );
		ConstructorChainElement element1_1 = (ConstructorChainElement) chainElements1.get(0);
		assertEquals( element1_1.getName().toString(), "RTActor");
		List expressions1_1 = element1_1.getExpressionList();
		assertEquals( expressions1_1.size(), 2 );
		ConstructorChainElementExpression expression1_1_1 = (ConstructorChainElementExpression)expressions1_1.get(0);
		assertEquals( expression1_1_1.getExpression().elements().size(), 1 ); 
		Name t1_1_1  = (Name)expression1_1_1.getExpression().elements().get(0);
		ConstructorChainElementExpression expression1_1_2 = (ConstructorChainElementExpression)expressions1_1.get(1);
		assertEquals( expression1_1_2.getExpression().elements().size(), 1 ); 
		Name t1_1_2 = (Name)expression1_1_2.getExpression().elements().get(0);
		
		assertEquals( t1_1_1.toString(), "rtg_rts");
		assertEquals( t1_1_2.toString(), "rtg_ref");
		
		ConstructorChainElement element1_2 = (ConstructorChainElement) chainElements1.get(1);
		assertEquals( element1_2.getName().toString(), "myId" );
		List expressions1_2 = element1_2.getExpressionList();
		assertEquals( expressions1_2.size(), 1 );
		ConstructorChainElementExpression expression = (ConstructorChainElementExpression) expressions1_2.get(0);
		assertEquals( expression.getExpression().elements().size(), 1 );
		Token t = (Token)expression.getExpression().elements().get(0);
		assertEquals( t.getImage(), "0");
		assertEquals( t.getType(), Token.tINTEGER );
		
		
		
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
	
	public void testTemplateDeclarationOfMethod() throws Exception
	{
		TranslationUnit tu = parse( "template<class A, typename B=C> A aTemplatedFunction( B bInstance );");
		assertEquals( tu.getDeclarations().size(), 1 );
		TemplateDeclaration templateDeclaration = (TemplateDeclaration)tu.getDeclarations().get(0);
		assertEquals( templateDeclaration.getTemplateParms().getDeclarations().size(), 2 );
		TemplateParameter templateParameter = (TemplateParameter)templateDeclaration.getTemplateParms().getDeclarations().get(0);
		assertEquals( templateParameter.getKind(), TemplateParameter.k_class );
		assertEquals( templateParameter.getName().toString(), "A");
		templateParameter = (TemplateParameter)templateDeclaration.getTemplateParms().getDeclarations().get(1);
		assertEquals( templateParameter.getKind(), TemplateParameter.k_typename );
		assertEquals( templateParameter.getName().toString(), "B");
		assertEquals( templateParameter.getTypeId().toString(), "C");
		assertEquals( templateDeclaration.getDeclarations().size(), 1 );
		SimpleDeclaration methodDeclaration = (SimpleDeclaration) templateDeclaration.getDeclarations().get(0);
		assertEquals( methodDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_type );
		assertEquals( methodDeclaration.getDeclSpecifier().getTypeName(), "A");
		assertEquals( methodDeclaration.getDeclarators().size(), 1 );
		Declarator declarator = (Declarator)methodDeclaration.getDeclarators().get(0);
		assertEquals( declarator.getName().toString(), "aTemplatedFunction" );
		assertEquals( declarator.getParms().getDeclarations().size(), 1 );
		ParameterDeclaration parameterDeclaration = (ParameterDeclaration)declarator.getParms().getDeclarations().get(0);
		assertEquals( parameterDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_type );
		assertEquals( parameterDeclaration.getDeclSpecifier().getTypeName(), "B" );
		assertEquals( parameterDeclaration.getDeclarators().size(), 1 );
		assertEquals( ((Declarator)parameterDeclaration.getDeclarators().get(0)).getName().toString(), "bInstance");
	}
	
	public void testTemplateDeclarationOfClass() throws Exception {
		TranslationUnit tu = parse( "template<class T, typename Tibor = junk, class, typename, int x, float y,template <class Y> class, template<class A> class AClass> class myarray { /* ... */ };");
		assertEquals( tu.getDeclarations().size(), 1 );
		TemplateDeclaration declaration = (TemplateDeclaration)tu.getDeclarations().get(0);
		assertEquals( declaration.getTemplateParms().getDeclarations().size(), 8 );
		TemplateParameter parameter = (TemplateParameter)declaration.getTemplateParms().getDeclarations().get(0);
		assertEquals( parameter.getKind(), TemplateParameter.k_class);
		assertEquals( parameter.getName().toString(), "T" ); 
		assertNull( parameter.getTypeId());
		parameter = (TemplateParameter)declaration.getTemplateParms().getDeclarations().get(1);
		assertEquals( parameter.getKind(), TemplateParameter.k_typename);
		assertEquals( parameter.getName().toString(), "Tibor" );
		assertEquals( parameter.getTypeId().toString(), "junk");
		parameter = (TemplateParameter)declaration.getTemplateParms().getDeclarations().get(2);
		assertEquals( parameter.getKind(), TemplateParameter.k_class);
		assertNull( parameter.getName() );
		assertNull( parameter.getTypeId());
		parameter = (TemplateParameter)declaration.getTemplateParms().getDeclarations().get(3);
		assertEquals( parameter.getKind(), TemplateParameter.k_typename);
		assertNull( parameter.getName() );
		assertNull( parameter.getTypeId());
		ParameterDeclaration decl = (ParameterDeclaration)declaration.getTemplateParms().getDeclarations().get(4);
		assertEquals( decl.getDeclSpecifier().getType(), DeclSpecifier.t_int );
		assertEquals( 1, decl.getDeclarators().size() );
		assertEquals( "x", ((Declarator)decl.getDeclarators().get(0)).getName().toString() );
		 
		decl = (ParameterDeclaration)declaration.getTemplateParms().getDeclarations().get(5);
		assertEquals( decl.getDeclSpecifier().getType(), DeclSpecifier.t_float );
		assertEquals( 1, decl.getDeclarators().size() );
		assertEquals( "y", ((Declarator)decl.getDeclarators().get(0)).getName().toString() );
		 
		parameter = (TemplateParameter)declaration.getTemplateParms().getDeclarations().get(6);
		assertEquals( parameter.getKind(), TemplateParameter.k_template );
		assertEquals( parameter.getTemplateParms().getDeclarations().size(), 1 );
		assertNull( parameter.getName() );
		TemplateParameter subParameter = (TemplateParameter)parameter.getTemplateParms().getDeclarations().get(0);
		assertEquals( subParameter.getKind(), TemplateParameter.k_class );
		assertEquals( subParameter.getName().toString(), "Y" );
		assertNull( subParameter.getTypeId() );
		
		parameter = (TemplateParameter)declaration.getTemplateParms().getDeclarations().get(7);
		assertEquals( parameter.getKind(), TemplateParameter.k_template );
		assertEquals( parameter.getTemplateParms().getDeclarations().size(), 1 );
		subParameter = (TemplateParameter)parameter.getTemplateParms().getDeclarations().get(0);
		assertEquals( subParameter.getKind(), TemplateParameter.k_class );
		assertEquals( subParameter.getName().toString(), "A" );
		assertNull( subParameter.getTypeId() );
		assertEquals( parameter.getName().toString(), "AClass" );
		assertEquals( declaration.getDeclarations().size(), 1 );
		SimpleDeclaration myArray = (SimpleDeclaration)declaration.getDeclarations().get(0);
		ClassSpecifier classSpec = (ClassSpecifier)myArray.getTypeSpecifier();
		assertEquals( classSpec.getClassKey(), ClassKey.t_class ); 
		assertEquals( classSpec.getName().toString(), "myarray");
		assertEquals( 0, classSpec.getDeclarations().size() );
	}
		
	public void testStruct() throws Exception
	{
		StringWriter writer = new StringWriter(); 
		writer.write( "struct mad_bitptr { unsigned char const *byte;\n" );		writer.write( "unsigned short cache;\n unsigned short left;};" );
		TranslationUnit tu = parse( writer.toString() );
		assertEquals( tu.getDeclarations().size(), 1 );
		SimpleDeclaration declaration = (SimpleDeclaration)tu.getDeclarations().get( 0 );
		ClassSpecifier classSpec = (ClassSpecifier)declaration.getTypeSpecifier(); 
		DeclSpecifier declSpec = declaration.getDeclSpecifier();
		assertEquals( classSpec.getClassKey(), ClassKey.t_struct );
		assertEquals( classSpec.getName().toString(), "mad_bitptr" );
		assertEquals( declaration.getDeclarators().size(), 0 );
		List subDeclarations = classSpec.getDeclarations();
		assertEquals( 3, subDeclarations.size() );
		declaration = (SimpleDeclaration)subDeclarations.get(0);
		declSpec = declaration.getDeclSpecifier();
		assertTrue( declSpec.isUnsigned() );
		assertTrue( declSpec.isConst() );
		assertEquals( declSpec.getType(), DeclSpecifier.t_char );
		assertEquals( declaration.getDeclarators().size(), 1 );
		Declarator d = (Declarator)declaration.getDeclarators().get(0);
		assertEquals( d.getPointerOperators().size(), 1 );
		PointerOperator po = (PointerOperator)d.getPointerOperators().get(0);
		assertEquals( po.getType(), PointerOperator.t_pointer );
		assertFalse( po.isConst() );
		assertFalse(po.isVolatile() );
		assertEquals( d.getName().toString(), "byte" );
		
		declaration = (SimpleDeclaration)subDeclarations.get(1);
		declSpec = declaration.getDeclSpecifier();
		assertTrue( declSpec.isUnsigned());
		assertTrue( declSpec.isShort());
		assertEquals( declaration.getDeclarators().size(), 1 );
		d = (Declarator)declaration.getDeclarators().get(0);
		assertEquals( d.getPointerOperators().size(), 0 );
		assertEquals( d.getName().toString(), "cache" );
		
		
		declaration = (SimpleDeclaration)subDeclarations.get(2);
		declSpec = declaration.getDeclSpecifier();
		assertTrue( declSpec.isUnsigned());
		assertTrue( declSpec.isShort());
		assertEquals( declaration.getDeclarators().size(), 1 );
		d = (Declarator)declaration.getDeclarators().get(0);
		assertEquals( d.getPointerOperators().size(), 0 );
		assertEquals( d.getName().toString(), "left" );
	}


	public void testBug35906() throws Exception
	{
		StringWriter code = new StringWriter(); 
		code.write( "void TTest::MTest() {}\n" ); 
		code.write( "struct TTest::STest *TTest::FTest (int i) {}\n" ); 
		TranslationUnit tu = parse( code.toString() );
		assertEquals( tu.getDeclarations().size(), 2 );
		SimpleDeclaration declaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertEquals( declaration.getDeclSpecifier().getType(), DeclSpecifier.t_void );
		assertEquals( declaration.getDeclarators().size(), 1 );
		Declarator d = (Declarator)declaration.getDeclarators().get(0);
		assertEquals( d.getName().toString(), "TTest::MTest");
		
		declaration = (SimpleDeclaration)tu.getDeclarations().get(1);
		ElaboratedTypeSpecifier spec = (ElaboratedTypeSpecifier)declaration.getTypeSpecifier();
		assertEquals( spec.getClassKey(), ClassKey.t_struct );
		assertEquals( spec.getName().toString(), "TTest::STest" );
	}
	
	public void testBug36073() throws Exception
	{
		StringWriter writer = new StringWriter(); 
		writer.write( "class A{\n" ); 
		writer.write( "int x;\n" ); 
		writer.write( "public:\n" ); 
		writer.write( "A(const A&);\n" ); 
		writer.write( "};\n" ); 
		writer.write( "A::A(const A&v) : x(v.x) { }\n" );
		TranslationUnit tu = parse( writer.toString() ); 
	}
	
	public void testBug36288() throws Exception
	{
		TranslationUnit tu = parse( "int foo() {}\nlong foo2(){}", true, true);  
		assertEquals( tu.getDeclarations().size(), 2 );
		for( int i = 0; i < 2; ++i )
		{
			SimpleDeclaration declaration = (SimpleDeclaration)tu.getDeclarations().get(i);
			assertEquals( declaration.getDeclarators().size(), 1 );
			Declarator d = (Declarator)declaration.getDeclarators().get(0);
			assertEquals( d.getName().toString(), ( i == 0 ) ? "foo" : "foo2");
			assertEquals( declaration.getDeclSpecifier().getType(), (i == 0 ) ? DeclSpecifier.t_int : DeclSpecifier.t_type );
			assertEquals( declaration.getDeclSpecifier().isLong(), ( i == 0 ) ? false : true ); 
		}
	}

	public void testBug36250() throws Exception
	{
		TranslationUnit tu = parse( "int f( int = 0 );");
		assertEquals( tu.getDeclarations().size(), 1 );
		SimpleDeclaration functionDeclaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertEquals( functionDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_int );
		assertEquals( functionDeclaration.getDeclarators().size(), 1 );
		Declarator functionDeclarator = (Declarator)functionDeclaration.getDeclarators().get(0);
		assertEquals( functionDeclarator.getName().toString(), "f" );
		assertEquals( functionDeclarator.getParms().getDeclarations().size(), 1 );
		ParameterDeclaration parameterDeclaration = (ParameterDeclaration)functionDeclarator.getParms().getDeclarations().get(0);
		assertEquals( parameterDeclaration .getDeclSpecifier().getType(), DeclSpecifier.t_int );
		assertEquals( parameterDeclaration .getDeclarators().size(), 1 );
		Declarator parameterDeclarator = (Declarator)parameterDeclaration.getDeclarators().get(0);
		assertNull( parameterDeclarator.getName() );
		assertNotNull( parameterDeclarator.getExpression());
		
	}

	public void testBug36240() throws Exception
	{
		TranslationUnit tu = parse( "A & A::operator=( A ){}");
		assertEquals( tu.getDeclarations().size(), 1 );
		SimpleDeclaration functionDeclaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertEquals( functionDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_type );
		assertEquals( functionDeclaration.getDeclSpecifier().getTypeName(), "A" );
		assertEquals( functionDeclaration.getDeclarators().size(), 1 );
		Declarator functionDeclarator = (Declarator)functionDeclaration.getDeclarators().get(0);
		assertEquals( functionDeclarator.getPointerOperators().size(), 1 );
		PointerOperator po = (PointerOperator)functionDeclarator.getPointerOperators().get(0);
		assertEquals( po.getType(), PointerOperator.t_reference );
		assertFalse( po.isConst() || po.isVolatile() );
		assertEquals( functionDeclarator.getName().toString(), "A::operator=");
		assertEquals( functionDeclarator.getParms().getDeclarations().size(), 1 );
		ParameterDeclaration parameterDeclaration = (ParameterDeclaration)functionDeclarator.getParms().getDeclarations().get(0);
		assertEquals( parameterDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_type );
		assertEquals( parameterDeclaration.getDeclSpecifier().getTypeName(), "A");
		assertEquals( parameterDeclaration .getDeclarators().size(), 1 );
		Declarator parameterDeclarator = (Declarator)parameterDeclaration.getDeclarators().get(0);
		assertNull( parameterDeclarator.getName() );
	}
	
	public void testBug36254() throws Exception
	{
		TranslationUnit tu = parse( "unsigned i;\nvoid f( unsigned p1 = 0 );");
		assertEquals( tu.getDeclarations().size(), 2 );
		SimpleDeclaration declaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertTrue( declaration.getDeclSpecifier().isUnsigned());
		assertEquals( 1, declaration.getDeclarators().size() );
		assertEquals( "i", ((Declarator)declaration.getDeclarators().get(0)).getName().toString() );
		declaration = (SimpleDeclaration)tu.getDeclarations().get(1);
		assertEquals( declaration.getDeclSpecifier().getType(), DeclSpecifier.t_void );
		assertEquals( 1, declaration.getDeclarators().size() );
		Declarator declarator = (Declarator)declaration.getDeclarators().get(0);
		assertEquals( declarator.getName().toString(), "f" );
		assertEquals( declarator.getParms().getDeclarations().size(), 1 );
		ParameterDeclaration parmDecl = (ParameterDeclaration)declarator.getParms().getDeclarations().get(0);
		assertTrue( parmDecl.getDeclSpecifier().isUnsigned());
		assertEquals( parmDecl.getDeclarators().size(), 1 );
		Declarator parmDeclarator = (Declarator) parmDecl.getDeclarators().get(0);
		assertEquals( parmDeclarator.getName().toString(), "p1");
		assertNotNull( parmDeclarator.getExpression());
	}
	
	public void testBug36237() throws Exception
	{
		TranslationUnit tu = parse( "A::A():B( (char *)0 ){}", true, true ); 
		assertEquals( tu.getDeclarations().size(), 1 );  
	}

	public void testPointersToFunctions() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "void (*name)( void );\n");
		code.write( "static void * (*orig_malloc_hook)(const char *file, int line, size_t size);\n");

		TranslationUnit tu = parse( code.toString() );
		assertEquals( tu.getDeclarations().size(), 2 );
		SimpleDeclaration declaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertEquals( declaration.getDeclSpecifier().getType(), DeclSpecifier.t_void );
		assertEquals( declaration.getDeclarators().size(), 1);
		assertNull( ((Declarator)declaration.getDeclarators().get(0)).getName() );
		assertNotNull( ((Declarator)declaration.getDeclarators().get(0)).getDeclarator() );
		assertEquals( ((Declarator)declaration.getDeclarators().get(0)).getDeclarator().getName().toString(), "name" );
		ParameterDeclarationClause clause = ((Declarator)declaration.getDeclarators().get(0)).getParms();
		assertEquals( clause.getDeclarations().size(), 1 );
		assertEquals( ((ParameterDeclaration)clause.getDeclarations().get(0)).getDeclarators().size(), 1 );  
		assertNull(	((Declarator)((ParameterDeclaration)clause.getDeclarations().get(0)).getDeclarators().get(0)).getName() );
		assertEquals( ((ParameterDeclaration)clause.getDeclarations().get(0)).getDeclSpecifier().getType(), DeclSpecifier.t_void );
		
		declaration = (SimpleDeclaration)tu.getDeclarations().get(1); 
		assertEquals( declaration.getDeclSpecifier().getType(), DeclSpecifier.t_void );
		assertTrue( declaration.getDeclSpecifier().isStatic() );
		assertEquals( declaration.getDeclarators().size(), 1);
		assertNull( ((Declarator)declaration.getDeclarators().get(0)).getName() );
		assertNotNull( ((Declarator)declaration.getDeclarators().get(0)).getDeclarator() );
		assertEquals( ((Declarator)declaration.getDeclarators().get(0)).getDeclarator().getName().toString(), "orig_malloc_hook" );
		clause = ((Declarator)declaration.getDeclarators().get(0)).getParms();
		assertEquals( clause.getDeclarations().size(), 3 );
	}
	
	public void testBug36532() throws Exception
	{
		try
		{
			TranslationUnit tu = parse( "template<int f() {\n" );
			fail( "We should not make it this far");
		}
		catch( ParserException pe )
		{
		}
		catch( Exception e )
		{
			fail( "We should have gotten a ParserException rather than" + e);
		}
	}

	public void testBug36432() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "#define CMD_GET		\"g\"\n" ); 	 
		code.write( "#define CMD_ACTION   	\"a\"\n" ); 	 
		code.write( "#define CMD_QUIT		\"q\"\n" );
		code.write( "static const memevent_cmd_func memevent_cmd_funcs[sizeof memevent_cmds - 1] = {\n");
		code.write( "memevent_get,\n");
		code.write( "memevent_action,\n");
		code.write( "memevent_quit,\n");
		code.write( "};\n");
		TranslationUnit tu = parse( code.toString() );
		assertEquals( tu.getDeclarations().size(), 1 );
	}
	
	public void testBug36594() throws Exception
	{
		TranslationUnit tu = parse( "const int n = sizeof(A) / sizeof(B);");
		assertEquals( tu.getDeclarations().size(), 1 );
	}
	
	public void testArrayOfPointerToFunctions() throws Exception
	{
		TranslationUnit tu = parse( "unsigned char (*main_data)[MAD_BUFFER_MDLEN];");
	}
	
	public void testBug36600() throws Exception
	{
		TranslationUnit tu = parse( "enum mad_flow (*input_func)(void *, struct mad_stream *);");
		assertEquals( tu.getDeclarations().size(), 1 );
		SimpleDeclaration simple = (SimpleDeclaration)tu.getDeclarations().get(0);
		ElaboratedTypeSpecifier elab = (ElaboratedTypeSpecifier)simple.getTypeSpecifier();
		assertEquals( elab.getClassKey(), ClassKey.t_enum );
		assertEquals( elab.getName().toString(), "mad_flow");
		assertEquals( simple.getDeclarators().size(), 1 );
		Declarator declarator = (Declarator)simple.getDeclarators().get(0);        
		assertNull( declarator.getName() );
		assertNotNull( declarator.getDeclarator() );
		
		tu = parse( "enum mad_layer layer;");
		assertEquals( tu.getDeclarations().size(),  1);
		SimpleDeclaration declaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		elab = (ElaboratedTypeSpecifier)declaration.getTypeSpecifier();
		assertEquals( elab.getClassKey(), ClassKey.t_enum);
		assertEquals( elab.getName().toString(), "mad_layer");
		assertEquals( declaration.getDeclarators().size(), 1);
		assertEquals( ((Declarator)declaration.getDeclarators().get(0)).getName().toString(), "layer" ); 

	}
	
	public void testBug36247() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "class A {\n" ); 
  		code.write( "INLINE_DEF int f ();\n" ); 
		code.write( "INLINE_DEF A   g ();" ); 
		code.write( "INLINE_DEF A * h ();" ); 
		code.write( "INLINE_DEF A & unlock( void );");
		code.write( "};" );
		TranslationUnit tu = parse(code.toString());
		assertEquals( tu.getDeclarations().size(),1 );
		SimpleDeclaration classDeclaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertEquals( classDeclaration.getDeclarators().size(), 0 );
		assertEquals( classDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_type );
		ClassSpecifier classSpec = (ClassSpecifier)classDeclaration.getTypeSpecifier();
		PointerOperator po =null;
		int number = 4;
		assertEquals( classSpec.getDeclarations().size(), number );
		for( int i = 0; i < number; ++i )
		{
			SimpleDeclaration subDeclaration = (SimpleDeclaration)classSpec.getDeclarations().get(i);
			assertEquals( subDeclaration.getDeclarators().size(), 1 );
			Declarator functionDeclarator = (Declarator)subDeclaration.getDeclarators().get(0);
			assertNotNull( functionDeclarator.getParms());
			if( i == 3)
				assertEquals( 1, functionDeclarator.getParms().getDeclarations().size() );
			else
				assertEquals( 0, functionDeclarator.getParms().getDeclarations().size() );
			List pointerOperators = functionDeclarator.getPointerOperators();  
			switch( i )
			{
				case 0: 
					assertEquals( subDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_int );
					assertEquals( functionDeclarator.getName().toString(), "f" );
					assertEquals( pointerOperators.size(), 0 );
					break;
				case 1:
					assertEquals( subDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_type );
					assertEquals( subDeclaration.getDeclSpecifier().getTypeName(), "A");
					assertEquals( functionDeclarator.getName().toString(), "g" );
					assertEquals( pointerOperators.size(), 0 );	
					break;
				case 2:
					assertEquals( subDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_type );
					assertEquals( subDeclaration.getDeclSpecifier().getTypeName(), "A");
					assertEquals( functionDeclarator.getName().toString(), "h" );
					assertEquals( pointerOperators.size(), 1 );
					po = (PointerOperator)pointerOperators.get(0);
					assertFalse( po.isConst() ); 
					assertFalse( po.isVolatile() );
					assertEquals( po.getType(), PointerOperator.t_pointer );
					break;
				case 3:
					assertEquals( subDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_type );
					assertEquals( subDeclaration.getDeclSpecifier().getTypeName(), "A");
					assertEquals( functionDeclarator.getName().toString(), "unlock" );
					assertEquals( pointerOperators.size(), 1 );
					po = (PointerOperator)pointerOperators.get(0);
					assertFalse( po.isConst() ); 
					assertFalse( po.isVolatile() );
					assertEquals( po.getType(), PointerOperator.t_reference );					
				default:
					break;
			}
			
		}
		
	}
	
	
	public void testBug36559() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "namespace myNameSpace {\n" ); 
		code.write( "template<typename T=short> class B {};\n" );
		code.write( "template<> class B<int> {};\n" ); 
		code.write( "}\n" ); 
		TranslationUnit tu = parse( code.toString() ); 
		assertEquals( tu.getDeclarations().size(),1);
		NamespaceDefinition definition = (NamespaceDefinition)tu.getDeclarations().get(0);
		assertEquals( definition.getName().toString(), "myNameSpace");
		assertEquals( definition.getDeclarations().size(),  2 );
		TemplateDeclaration templateDeclaration = (TemplateDeclaration)definition.getDeclarations().get(0);
		assertFalse( templateDeclaration.isExported());
		assertEquals( templateDeclaration.getTemplateParms().getDeclarations().size(), 1 );
		TemplateParameter parm = (TemplateParameter)templateDeclaration.getTemplateParms().getDeclarations().get(0);
		assertEquals( parm.getKind(), TemplateParameter.k_typename );
		assertEquals( parm.getName().toString(), "T");
		assertEquals( parm.getTypeId().toString(), "short");
		assertEquals( templateDeclaration.getDeclarations().size(),  1 );
		SimpleDeclaration classB = (SimpleDeclaration)templateDeclaration.getDeclarations().get(0);
		assertEquals( classB.getDeclarators().size(), 0 );
		assertEquals( ((ClassSpecifier)classB.getTypeSpecifier()).getName().toString(), "B" );
		assertEquals( ((ClassSpecifier)classB.getTypeSpecifier()).getClassKey(), ClassKey.t_class ); 
		assertEquals( ((ClassSpecifier)classB.getTypeSpecifier()).getDeclarations().size(), 0 ); 
		
		ExplicitTemplateDeclaration etd = (ExplicitTemplateDeclaration)definition.getDeclarations().get(1);
		assertEquals( etd.getKind(), ExplicitTemplateDeclaration.k_specialization );
		assertEquals( etd.getDeclarations().size(), 1 );
		classB = (SimpleDeclaration)etd.getDeclarations().get(0);
		assertEquals( classB.getDeclarators().size(), 0 );
		assertEquals( ((ClassSpecifier)classB.getTypeSpecifier()).getName().toString(), "B<int>" );
		assertEquals( ((ClassSpecifier)classB.getTypeSpecifier()).getClassKey(), ClassKey.t_class ); 
		assertEquals( ((ClassSpecifier)classB.getTypeSpecifier()).getDeclarations().size(), 0 ); 
		
	}
	
	public void testBug36551() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "class TextFrame {\n" ); 
		code.write( "BAD_MACRO()\n"); 
		code.write( "};");
		TranslationUnit tu = parse( code.toString(), true, false );
		assertFalse( tu.isParseSuccessful() );
		assertEquals( tu.getDeclarations().size(), 1 );
		SimpleDeclaration d = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertEquals( d.getDeclarators().size(), 0 );
		ClassSpecifier classSpec = (ClassSpecifier)d.getTypeSpecifier();
		assertEquals( classSpec.getClassKey(), ClassKey.t_class );
		assertEquals( classSpec.getName().toString(), "TextFrame");
		assertEquals( classSpec.getDeclarations().size(), 0 );
		
		code = new StringWriter(); 
		code.write( "namespace X { class A }");
		tu = parse( code.toString(), true, false ); 
		assertFalse( tu.isParseSuccessful() );
		assertEquals( tu.getDeclarations().size(), 1 );
		NamespaceDefinition nd = (NamespaceDefinition)tu.getDeclarations().get(0);
		assertEquals( nd.getDeclarations().size(), 0 );
		assertEquals( nd.getName().toString(), "X");
		
		code = new StringWriter(); 
		code.write( "extern \"C\" { JUNK }" );
		tu = parse( code.toString(), true, false );
		assertFalse( tu.isParseSuccessful() );
		assertEquals( tu.getDeclarations().size(), 1 );
		LinkageSpecification ls = (LinkageSpecification)tu.getDeclarations().get(0);
		assertEquals( ls.getDeclarations().size(), 0);
		assertEquals( ls.getLanguageLinkage(), "C" );		
	}
	
	public void testBug36692() throws Exception  {
		Writer code = new StringWriter();
		code.write("template <typename T, typename Destroyer>\n");
		code.write("void SetLongevity(T* pDynObject, unsigned int longevity,\n");
		code.write("Destroyer d = Private::Deleter<T>::Delete){}\n");

		TranslationUnit tu = parse(code.toString());
		assertEquals( tu.getDeclarations().size(), 1 ); 
		TemplateDeclaration template = (TemplateDeclaration)tu.getDeclarations().get(0);
		assertFalse( template.isExported() );
		TemplateParameterList list = template.getTemplateParms();
		assertEquals( list.getDeclarations().size(), 2 );
		for( int i = 0; i < 2; ++i )
		{
			TemplateParameter parameter = (TemplateParameter)list.getDeclarations().get(i);
			assertEquals( parameter.getName().toString(), i == 0 ? "T": "Destroyer");
			assertEquals( parameter.getKind(), TemplateParameter.k_typename );
		}
		assertEquals( template.getDeclarations().size(),  1 );
		SimpleDeclaration method = (SimpleDeclaration)template.getDeclarations().get(0);
		assertEquals( method.getDeclSpecifier().getType(), DeclSpecifier.t_void );
		assertEquals( method.getDeclarators().size(), 1 );
		assertEquals( method.isFunctionDefinition(), true ); 
		Declarator declarator = (Declarator)method.getDeclarators().get(0);
		assertEquals( declarator.getName().toString(), "SetLongevity");
		ParameterDeclarationClause pdc = declarator.getParms();
		assertEquals( pdc.getDeclarations().size(), 3 ); 
		for( int i = 0; i < 3; ++i )
		{
			ParameterDeclaration parameter = (ParameterDeclaration)pdc.getDeclarations().get(i);
			assertEquals( parameter.getDeclarators().size(), 1 );
			Declarator parameterDeclarator = (Declarator)parameter.getDeclarators().get(0);
			List pointers = parameterDeclarator.getPointerOperators();
			PointerOperator op = null;  
			Expression exp = parameterDeclarator.getExpression(); 
			switch( i )
			{
				case 0:
					assertEquals( parameterDeclarator.getName().toString(), "pDynObject");
					assertEquals( pointers.size(),  1 );
					op = (PointerOperator)pointers.get(0);
					assertFalse( op.isConst());
					assertFalse( op.isVolatile());
					assertEquals( op.getType(), PointerOperator.t_pointer);
					assertNull( exp );
					break;
				case 1:
					assertEquals( parameterDeclarator.getName().toString(), "longevity");
					assertEquals( pointers.size(),  0 );
					assertEquals( parameter.getDeclSpecifier().getType(), DeclSpecifier.t_int );
					assertTrue( parameter.getDeclSpecifier().isUnsigned() );
					assertNull( exp ); 
					break;
				case 2:
					assertEquals( parameterDeclarator.getName().toString(), "d"); 
					assertEquals( pointers.size(),  0 );
					assertNotNull( exp );
					break;
				default:
					break; 
			}
		}
		
	}

	public void testBug36708() throws Exception {
		TranslationUnit tu = parse("enum { isPointer = PointerTraits<T>::result };");
		assertEquals( tu.getDeclarations().size(), 1 );
		SimpleDeclaration simple = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertEquals( simple.getDeclarators().size(), 0 );
		EnumerationSpecifier enum = (EnumerationSpecifier)simple.getTypeSpecifier(); 
		assertNull( enum.getName() );
		List enumerators = enum.getEnumeratorDefinitions();
		assertEquals( enumerators.size(), 1 );
		EnumeratorDefinition enumerator = (EnumeratorDefinition )enumerators.get(0);
		assertEquals( enumerator.getName().toString(), "isPointer");
		assertNotNull( enumerator.getExpression() );
	}

	public void testBug36690() throws Exception {
		TranslationUnit tu = parse("Functor(const Functor& rhs) : spImpl_(Impl::Clone(rhs.spImpl_.get())){}");
		assertEquals( tu.getDeclarations().size(), 1 );
		SimpleDeclaration simple = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertEquals( simple.getDeclarators().size(), 1 );
		Declarator declarator = (Declarator)simple.getDeclarators().get(0);
		ParameterDeclarationClause pdc = declarator.getParms();
		assertEquals( pdc.getDeclarations().size(), 1 );
		ConstructorChain chain = declarator.getCtorChain();
		assertEquals( chain.getChainElements().size(), 1 );
	}

	public void testBug36703() throws Exception {
		TranslationUnit tu = parse("const std::type_info& Get() const;");
		assertEquals( tu.getDeclarations().size(), 1 );
		SimpleDeclaration simple = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertEquals( simple.getDeclSpecifier().isConst(), true );
		assertEquals( simple.getDeclSpecifier().getType(), DeclSpecifier.t_type);
		assertEquals( simple.getDeclSpecifier().getTypeName(), "std::type_info");
		assertEquals( simple.getDeclarators().size(), 1 );
		Declarator declarator = (Declarator)simple.getDeclarators().get(0);
		ParameterDeclarationClause pdc = declarator.getParms();
		assertTrue( declarator.isConst() );
		assertEquals( pdc.getDeclarations().size(),  0 );
		assertEquals( declarator.getName().toString(), "Get");
		assertEquals( declarator.getPointerOperators().size(), 1 );
		PointerOperator pointerOperator = (PointerOperator)declarator.getPointerOperators().get(0);
		assertFalse( pointerOperator.isConst());
		assertFalse( pointerOperator.isVolatile());
		assertEquals( pointerOperator.getType(), PointerOperator.t_reference);
	}
	
	public void testBug36689() throws Exception {
		Writer code = new StringWriter();
		code.write("template\n");
		code.write("<\n");
		code.write("class AbstractFact,\n");
		code.write(
			"template <class, class> class Creator = OpNewFactoryUnit,\n");
		code.write("class TList = typename AbstractFact::ProductList\n");
		code.write(">\n");
		code.write("class ConcreteFactory\n");
		code.write(": public GenLinearHierarchy<\n");
		code.write(
			"typename TL::Reverse<TList>::Result, Creator, AbstractFact>\n");
		code.write("{\n");
		code.write("public:\n");
		code.write(
			"typedef typename AbstractFact::ProductList ProductList;\n");
		code.write("typedef TList ConcreteProductList;\n");
		code.write("};\n");
		TranslationUnit tu = parse(code.toString());
	}
	
	public void testBug36707() throws Exception {
		TranslationUnit tu =
			parse("enum { exists = sizeof(typename H::Small) == sizeof((H::Test(H::MakeT()))) };");
	}
	
	public void testBug36717() throws Exception  {
		TranslationUnit tu = parse("enum { eA = A::b };");
	}
	
	public void testBug36693() throws Exception {
		TranslationUnit tu =
				parse("FixedAllocator::Chunk* FixedAllocator::VicinityFind(void* p){}");
	}

	public void testBug36696() throws Exception {
		Writer code = new StringWriter();
		code.write(
			"template <typename P1> RefCounted(const RefCounted<P1>& rhs)\n");
		code.write(
			": pCount_(reinterpret_cast<const RefCounted&>(rhs).pCount_) {}\n");
		TranslationUnit tu = parse(code.toString());
	}

	public void testBug36713() throws Exception {
		Writer code = new StringWriter();
		code.write("A ( * const fPtr) (void *); \n");
		code.write("A (* const fPtr2) ( A * ); \n");
		code.write("A (*const fPtr3) ( A * ) = function\n");
		TranslationUnit tu = parse(code.toString());
	}

	public void testBug36794() throws Exception
	{
		TranslationUnit tu = parse( "template<> class allocator<void> {};");
		Iterator i = tu.iterateOffsetableElements();
		while( i.hasNext() )
			assertNotNull( i.next() );
	}
	
	public void testBug36811() throws Exception
	{
		Writer code = new StringWriter();  
		code.write( "using namespace std;\n" ); 
		code.write( "class Test {};" );
		TranslationUnit tu = parse( code.toString() );
		assertEquals( tu.getDeclarations().size(), 2 );
		Iterator i = tu.iterateOffsetableElements();
		while( i.hasNext() )
			assertNotNull( i.next() );
	}


}

