/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.core.parser.tests;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.Map;
import org.eclipse.cdt.internal.core.parser.Declaration;
import org.eclipse.cdt.internal.core.parser.ParserSymbolTable;
import org.eclipse.cdt.internal.core.parser.ParserSymbolTableException;

/**
 * @author aniefer
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ParserSymbolTableTest extends TestCase {

	public ParserSymbolTable table = null;
	
	public ParserSymbolTableTest( String arg )
	{
		super( arg );
	}
	
	public ParserSymbolTable newTable(){
		table = new ParserSymbolTable();
		return table;
	}
	
	/**
	 * testSimpleAdd.  
	 * Add a declaration to the table and confirm it is there.
	 * 
	 * @throws Exception
	 */
	public void testSimpleAdd() throws Exception{
		newTable(); //create the symbol table
		
		Declaration decl = new Declaration( "x" );
		
		table.addDeclaration( decl );
		
		Declaration compUnit = table.getCompilationUnit();
		assertEquals( compUnit, table.peek() );
		
		Map declarations = compUnit.getContainedDeclarations();
		assertEquals( 1, declarations.size() );
		
		Iterator iter = declarations.values().iterator();
		Declaration contained = (Declaration) iter.next();
		
		assertEquals( false, iter.hasNext() );
		assertEquals( decl, contained );
		assertEquals( contained.getName(), "x" );
	}

	/**
	 * testSimpleLookup
	 * Add a declaration to the table, then look it up.
	 * @throws Exception
	 */
	public void testSimpleLookup() throws Exception{
		newTable(); //new symbol table
		
		Declaration decl = new Declaration( "x" );
		
		table.addDeclaration( decl );
		
		Declaration look = table.Lookup( "x" );
		
		assertEquals( decl, look );
	}
	
	public void testLookupNonExistant() throws Exception{
		newTable();
		
		Declaration look = table.Lookup( "boo" );
		assertEquals( look, null );
	}
	
	/**
	 * testSimplePushPop
	 * test pushing and popping
	 * @throws Exception
	 */
	public void testSimplePushPop() throws Exception{
		newTable();
		
		Declaration pushing = new Declaration( "class" );
		assertEquals( pushing.getContainingScope(), null );
		
		table.push( pushing );
		assertEquals( pushing, table.peek() );
		assertEquals( pushing.getContainingScope(), table.getCompilationUnit() );
		
		Declaration popped = table.pop();
		assertEquals( pushing, popped );
		assertEquals( table.peek(), table.getCompilationUnit() );
	}

	public void testSimpleSetGetObject() throws Exception{
		newTable();
		
		Declaration decl = new Declaration( "x" );
		Object obj = new Object();
		
		decl.setObject( obj );
		
		table.addDeclaration( decl );
		
		Declaration look = table.Lookup( "x" );
		
		assertEquals( look.getObject(), obj );
	}
	
	/**
	 * testHide
	 * test that a declaration in a scope hides declarations in containing
	 * scopes
	 * @throws Exception
	 */
	public void testHide() throws Exception{
		newTable();
		
		Declaration firstX = new Declaration( "x" );
		table.addDeclaration( firstX );
		
		Declaration firstClass = new Declaration( "class" );
		table.addDeclaration( firstClass );
		table.push( firstClass );
		
		Declaration look = table.Lookup( "x" );
		assertEquals( look, firstX );
		
		Declaration secondX = new Declaration( "x" );
		table.addDeclaration( secondX );
		
		look = table.Lookup( "x" );
		assertEquals( look, secondX );
		
		table.pop();
		
		look = table.Lookup( "x" );
		assertEquals( look, firstX );
	}
	
	/**
	 * testContainingScopeLookup
	 * test lookup of something declared in the containing scope
	 * @throws Exception
	 */
	public void testContainingScopeLookup() throws Exception{
		newTable();
		
		Declaration x = new Declaration("x");
		Declaration cls = new Declaration("class");
		
		table.addDeclaration( x );
		table.addDeclaration( cls );
		table.push( cls );
		
		Declaration look = table.Lookup( "x" );
		
		assertEquals( x, look );
	}
	
	/**
	 * testParentLookup
	 * test lookup of a variable declaration in the parent
	 *  
	 * @throws Exception
	 */
	public void testParentLookup() throws Exception{
		newTable();
		
		Declaration class1 = new Declaration( "class" );
		Declaration parent = new Declaration( "parent" );
		Declaration decl   = new Declaration( "x" );
		
		table.addDeclaration( parent );
		table.push( parent );
		table.addDeclaration( decl );
		table.pop();
		
		class1.addParent( parent );
		table.addDeclaration( class1 );
		table.push( class1 );
		
		Declaration look = table.Lookup( "x" );
		assertEquals( look, decl );
		
		table.pop();
		assertEquals( table.peek(), table.getCompilationUnit() );
	}

	/**
	 * testAmbiguousParentLookup
	 * calls testParentLookup
	 * 
	 * tests that if a variable is declared in two parents that the lookup
	 * returns an ambiguous result.
	 * 
	 * @throws Exception
	 */
	public void testAmbiguousParentLookup() throws Exception{
		testParentLookup();
		
		Declaration parent2 = new Declaration( "parent2" );
		
		table.addDeclaration( parent2 );
		
		Declaration class1 = table.Lookup( "class" );
		class1.addParent( parent2 );
		
		Declaration decl = new Declaration("x");
		table.push( parent2 );
		table.addDeclaration( decl );
		table.pop();
		
		table.push( class1 );
		try{
			table.Lookup( "x" );
			assertTrue( false );
		}
		catch ( ParserSymbolTableException e ){
			assertTrue( true );
		}
			
	}	
	
	/**
	 * testVirtualParentLookup
	 * 
	 * @throws Exception
	 * tests lookup of name in virtual baseclass C
	 * 
	 * 				C
	 * 			   / \
	 *            A   B
	 *             \ /
	 *            class
	 */
	public void testVirtualParentLookup() throws Exception{
		newTable();
		
		Declaration decl = new Declaration("class");
		Declaration c    = new Declaration("C");
		
		Declaration a    = new Declaration("A");
		a.addParent( c, true );
		
		Declaration b    = new Declaration("B");
		b.addParent( c, true );
		
		decl.addParent( a );
		decl.addParent( b );
		
		table.addDeclaration( c );
		table.push( c );
		Declaration x = new Declaration( "x" );
		table.addDeclaration( x );
		table.pop();
		
		table.addDeclaration( decl );
		table.addDeclaration( a );
		table.addDeclaration( b );
		
		table.push(decl);
		
		Declaration look = table.Lookup( "x" ); 
		
		assertEquals( look, x );
		
		table.pop();
		
		assertEquals( table.peek(), table.getCompilationUnit() );
	}
	
	/**
	 * testAmbiguousVirtualParentLookup
	 * @throws Exception
	 * 
	 * tests lookup of name in base class C in the following hierarchy
	 *                  C   C
	 *                 / \  | 
	 *                A   B D
	 *                 \ / / 
	 *                 class
	 */
	public void testAmbiguousVirtualParentLookup() throws Exception{
		testVirtualParentLookup();
		
		Declaration cls = table.Lookup("class");
		Declaration c   = table.Lookup("C");
		Declaration d   = new Declaration("D");
		
		d.addParent( c );
		
		cls.addParent( d );
		
		table.push( cls );
		
		try{
			table.Lookup( "x" );
			assertTrue( false );
		}
		catch( ParserSymbolTableException e){
			assertTrue( true );
		}
	}
	
	/**
	 * testStaticEnumParentLookup
	 * 
	 * @throws Exception
	 * 
	 *             D   D
	 *             |   |
	 *             B   C
	 *              \ /
	 *               A
	 * 
	 * Things defined in D are not ambiguous if they are static or an enum
	 */
	public void testStaticEnumParentLookup() throws Exception{
		newTable();
		
		Declaration a = new Declaration( "a" );
		Declaration b = new Declaration( "b" );
		Declaration c = new Declaration( "c" );
		Declaration d = new Declaration( "d" );
	
		table.addDeclaration( a );
		table.addDeclaration( b );
		table.addDeclaration( c );
		table.addDeclaration( d );
		
		Declaration enum = new Declaration("enum");
		enum.setType( Declaration.t_enumerator );
		
		Declaration stat = new Declaration("static");
		stat.setStatic(true);
		
		Declaration x = new Declaration("x");
		
		table.push(d);
		table.addDeclaration( enum );
		table.addDeclaration( stat );
		table.addDeclaration( x );
		table.pop();
		
		a.addParent( b );
		a.addParent( c );
		b.addParent( d );
		c.addParent( d );
		
		table.push( a );
		try{
			table.Lookup( "enum" );
			assertTrue( true );	
		}
		catch ( ParserSymbolTableException e){
			assertTrue( false );
		}
		
		try{
			table.Lookup( "static" );
			assertTrue( true );	
		}
		catch ( ParserSymbolTableException e){
			assertTrue( false );
		}
		
		try{
			table.Lookup( "x" );
			assertTrue( false );	
		}
		catch ( ParserSymbolTableException e){
			assertTrue( true );
		}
	}
	
	/**
	 * testElaboratedLookup
	 * @throws Exception
	 * test lookup of hidden names using elaborated type spec
	 */
	public void testElaboratedLookup() throws Exception{
		newTable();
		
		Declaration cls = new Declaration( "class" );
		cls.setType( Declaration.t_class );
		
		Declaration struct = new Declaration("struct");
		struct.setType( Declaration.t_struct );
		
		Declaration union = new Declaration("union");
		union.setType( Declaration.t_union );
		
		Declaration hideCls = new Declaration( "class" );
		Declaration hideStruct = new Declaration("struct");
		Declaration hideUnion = new Declaration("union");
		
		Declaration a = new Declaration("a");
		Declaration b = new Declaration("b");
		
		table.push(a);
		table.addDeclaration(hideCls);
		table.addDeclaration(hideStruct);
		table.addDeclaration(hideUnion);
		
		a.addParent( b );
		
		table.push(b);
		table.addDeclaration(cls);
		table.addDeclaration(struct);
		table.addDeclaration(union);
		table.pop();
		
		Declaration look = table.ElaboratedLookup( Declaration.t_class, "class" );
		assertEquals( look, cls );
		look = table.ElaboratedLookup( Declaration.t_struct, "struct" );
		assertEquals( look, struct );
		look = table.ElaboratedLookup( Declaration.t_union, "union" );
		assertEquals( look, union );
	}
	
	/**
	 * testDeclarationType
	 * @throws Exception
	 * test the use of Declaration type in the scenario
	 * 		A a;
	 * 		a.member <=...>;
	 * where A was previously declared
	 */
	public void testDeclarationType() throws Exception{
		newTable();
		//pre-condition
		Declaration A = new Declaration("A");
		table.addDeclaration(A);

		Declaration member = new Declaration("member");
		table.push(A);
		table.addDeclaration(member);
		table.pop();
				
		//at time of "A a;"
		Declaration look = table.Lookup("A");
		assertEquals( look, A );
		Declaration a = new Declaration("a");
		a.setTypeDeclaration( look );
		table.addDeclaration( a );
		
		//later "a.member"
		look = table.Lookup("a");
		assertEquals( look, a );
		Declaration type = look.getTypeDeclaration();
		assertEquals( type, A );
		table.push(type);
		look = table.Lookup("member");
		assertEquals( look, member );
	}
	
	/**
	 * testFunctions
	 * @throws Exception
	 * Functions are stored by signature. Where the signature can really be of
	 * any for you like, as long as it can't possibly be a regular name (ie
	 * including the parenthese is good...)
	 * So lookup of function names proceeds inthe same manner as normal names,
	 * this test doesn't really test anything new
	 */
	
	public void testFunctions() throws Exception{
		newTable();
		
		Declaration cls = new Declaration( "class");
		Declaration f1 = new Declaration("foo()");
		Declaration f2 = new Declaration("foo(int)");
		Declaration f3 = new Declaration("foo(int,char)");
		
		table.addDeclaration(cls);
		table.push(cls);
		
		table.addDeclaration( f1 );
		table.addDeclaration( f2 );
		table.addDeclaration( f3 );
		
		//return type can be specified by setting the TypeDeclaration
		Declaration returnType = new Declaration("return");
		f1.setTypeDeclaration( returnType );
		f2.setTypeDeclaration( returnType );
		f3.setTypeDeclaration( returnType );
		
		assertEquals( table.Lookup("foo()"), f1 );
		assertEquals( table.Lookup("foo(int)"), f2 );
		assertEquals( table.Lookup("foo(int,char)"), f3 );
		
		//notice that, with the current implementation, you can't do a lookup
		//on just the function name without the rest of the signature
		assertEquals( table.Lookup("foo"), null );
	}
	
	/**
	 * 
	 * @throws Exception
	 * 
	 * 	struct stat {
	 * 		//...
	 *  }
	 *  int stat( struct stat* );
	 *  void f() 
	 *  {
	 *  	struct stat *ps;
	 *   	stat(ps);
	 *  }
	 */
	public void testFunctionHidesClass() throws Exception{
		newTable();
		
		Declaration struct = new Declaration( "stat");
		struct.setType( Declaration.t_struct );
		table.addDeclaration( struct );
		
		Declaration function = new Declaration( "stat" );
		function.setType( Declaration.t_function );
		table.addDeclaration( function );
		
		Declaration f = new Declaration("f");
		f.setType( Declaration.t_function );
		table.addDeclaration( f );
		table.push( f );
		
		Declaration look = table.ElaboratedLookup( Declaration.t_struct, "stat" );
		assertEquals( look, struct );
		
		look = table.Lookup( "stat" );
		assertEquals( look, function );
	}
	
	/**
	 * 
	 * @throws Exception
	 * 
	 * namespace A {
	 *    int i;
	 *    namespace B {
	 *       namespace C{
	 *          int i;
	 *       }
	 *       using namespace A::B::C;
	 *       void f1() {
	 *          i = 5;  //OK, C::i visible and hides A::i
	 *       }
	 *    }
	 *    namespace D{
	 *       using namespace B;
	 *       using namespace C;
	 *       void f2(){
	 *          i = 5;  //ambiguous, B::C and A::i
	 *       }
	 *    }
	 *    void f3() {
	 *       i = 5;   //uses A::i
	 *    }
	 * }
	 * void f4(){
	 *    i = 5;   //no i is visible here 
	 * }
	 * 
	 */
	public void testUsingDirectives_1() throws Exception{
		newTable();
		
		Declaration nsA = new Declaration("A");
		nsA.setType( Declaration.t_namespace );
		table.addDeclaration( nsA );
		table.push( nsA );
		
		Declaration nsA_i = new Declaration("i");
		table.addDeclaration( nsA_i );
		
		Declaration nsB = new Declaration("B");
		nsB.setType( Declaration.t_namespace );
		table.addDeclaration( nsB );
		table.push( nsB );
		
		Declaration nsC = new Declaration("C");
		nsC.setType( Declaration.t_namespace );
		table.addDeclaration( nsC );
		table.push( nsC );
		
		Declaration nsC_i = new Declaration("i");
		table.addDeclaration( nsC_i );
		table.pop();
		
		Declaration look = table.Lookup("C");
		table.addUsingDirective( look );
		
		Declaration f1 = new Declaration("f");
		f1.setType( Declaration.t_function );
		table.push( f1 );
		
		look = table.Lookup( "i" );
		assertEquals( look, nsC_i ); //C::i visible and hides A::i
		
		table.pop();  //end of f1
		table.pop();  //end of nsB
		
		assertEquals( table.peek(), nsA );
		
		Declaration nsD = new Declaration("D");
		nsD.setType( Declaration.t_namespace );
		table.addDeclaration( nsD );
		table.push( nsD );
		
		look = table.Lookup("B");
		assertEquals( look, nsB );
		table.addUsingDirective( look );
		
		look = table.Lookup("C");
		assertEquals( look, nsC );
		table.addUsingDirective( look );
		
		Declaration f2 = new Declaration( "f2" );
		f2.setType( Declaration.t_function );
		table.addDeclaration( f2 );
		table.push( f2 );
		
		try
		{
			look = table.Lookup( "i" );
			assertTrue( false );
		}
		catch ( ParserSymbolTableException e )
		{
			assertTrue(true); //ambiguous B::C::i and A::i
		}
		table.pop(); //end f2
		table.pop(); //end nsD
		
		Declaration f3 = new Declaration ("f3");
		f3.setType( Declaration.t_function );
		table.addDeclaration( f3 );
		table.push( f3 );
		
		look = table.Lookup("i");
		assertEquals( look, nsA_i );  //uses A::i
		
		table.pop();
		table.pop();
		
		Declaration f4 = new Declaration ("f4");
		f4.setType( Declaration.t_function );
		table.addDeclaration( f4 );
		table.push( f4 );
		
		look = table.Lookup("i");
		assertEquals( look, null );//neither i is visible here.
	}
	/**
	 * 
	 * @throws Exception
	 * 
	 * namespace M {
	 *    int i;
	 * }
	 * namespace N {
	 *    int i;
	 *    using namespace M;
	 * }
	 * 
	 * void f() {
	 *    using namespace N;
	 *    i = 7;           //error, both M::i and N::i are visible
	 *    N::i = 5;        //ok, i directly declared in N, using M not
	 *                       considered (since this is a qualified lookup)
	 * }
	 * 
	 */
	public void testTransitiveUsingDirective() throws Exception
	{
		newTable();
		
		Declaration nsM = new Declaration( "M" );
		nsM.setType( Declaration.t_namespace );
		
		table.addDeclaration( nsM );
		
		table.push( nsM );
		Declaration nsM_i = new Declaration("i");
		table.addDeclaration( nsM_i );
		table.pop();
		
		Declaration nsN = new Declaration( "N" );
		nsN.setType( Declaration.t_namespace );
		
		table.addDeclaration( nsN );
		
		table.push( nsN );
		Declaration nsN_i = new Declaration("i");
		table.addDeclaration( nsN_i );
		table.addUsingDirective( nsM );
		table.pop();
		
		Declaration f = new Declaration("f");
		table.addDeclaration( f );
		table.push( f );
		
		table.addUsingDirective( nsN );
		
		Declaration look = null;
		try
		{
			look = table.Lookup( "i" );
			assertTrue( false );
		}
		catch ( ParserSymbolTableException e )
		{
			assertTrue( true );		//ambiguous, both M::i and N::i are visible.
		}
		
		look = table.LookupNestedNameSpecifier("N");
		table.push( look );
		look = table.QualifiedLookup("i"); //ok
		assertEquals( look, nsN_i );
	}
	
	/**
	 * 
	 * @throws Exception
	 * The same declaration found more than once is not an ambiguity
	 * namespace A{
	 *    int a;
	 * }
	 * namespace B{
	 *    using namespace A;
	 * }
	 * namespace C{
	 *    using namespace A;
	 * }
	 * 
	 * namespace BC{
	 *    using namespace B;
	 *    using namespace C;
	 * }
	 * 
	 * void f(){
	 *    BC::a++; //ok 
	 * }
	 */
	public void testUsing_SameDeclarationTwice() throws Exception
	{
		newTable();
		
		Declaration nsA = new Declaration("A");
		nsA.setType( Declaration.t_namespace );
		table.addDeclaration( nsA );
		table.push( nsA );
		
		Declaration a = new Declaration("a");
		table.addDeclaration( a );
		table.pop();
		
		Declaration nsB = new Declaration("B");
		nsB.setType( Declaration.t_namespace );
		table.addDeclaration( nsB );
		table.push( nsB );
		table.addUsingDirective( nsA );
		table.pop();

		Declaration nsC = new Declaration("C");
		nsC.setType( Declaration.t_namespace );
		table.addDeclaration( nsC );
		table.push( nsC );
		table.addUsingDirective( nsA );
		table.pop();	
		
		Declaration nsBC = new Declaration("BC");
		nsBC.setType( Declaration.t_namespace );
		table.addDeclaration( nsBC );
		table.push( nsBC );
		table.addUsingDirective( nsB );
		table.addUsingDirective( nsC );		
		table.pop();
		
		Declaration f = new Declaration("f");
		f.setType(Declaration.t_function);
		table.addDeclaration( f );
		table.push(f);
		
		Declaration look = table.LookupNestedNameSpecifier("BC");
		assertEquals( look, nsBC );
		table.push(look);
		look = table.QualifiedLookup("a");
		assertEquals( look, a );
	}
	
	/**
	 * 
	 * @throws Exception
	 * 
	 * namespace B {
	 *    int b;
	 * }
	 * namespace A {
	 *    using namespace B;
	 *    int a;
	 * }
	 * namespace B {
	 *    using namespace A;
	 * }
	 * 
	 * void f(){
	 *    A::a++;   //ok
	 *    A::b++;   //ok
	 *    B::a++;   //ok       
	 *    B::b++;   //ok 
	 * }
	 */
	public void testUsing_SearchedOnce() throws Exception
	{
		newTable();
		
		Declaration nsB = new Declaration( "B" );
		nsB.setType( Declaration.t_namespace );
		table.addDeclaration( nsB );
		table.push( nsB );
		
		Declaration b = new Declaration("b");
		table.addDeclaration( b );
		table.pop();
		
		Declaration nsA = new Declaration( "A" );
		nsA.setType( Declaration.t_namespace );
		table.addDeclaration( nsA );
		table.push( nsA );
		
		table.addUsingDirective( nsB );
		
		Declaration a = new Declaration("a");
		table.addDeclaration( a );
		
		table.pop();
		
		table.push( nsB );
		table.addUsingDirective( nsA );
		table.pop();
		
		Declaration f = new Declaration("f");
		table.addDeclaration(f);
		table.push(f);
		
		Declaration look = table.LookupNestedNameSpecifier("A");
		table.push(look);
		look = table.QualifiedLookup("a");
		assertEquals( look, a );
		
		look = table.QualifiedLookup("b");
		assertEquals( look, b );
		table.pop();
		
		look = table.LookupNestedNameSpecifier("B");
		table.push(look);
		look = table.QualifiedLookup("a");
		assertEquals( look, a );
		
		look = table.QualifiedLookup("b");
		assertEquals( look, b );
		table.pop();
		 
	}
	
	/**
	 * we pass if we don't go into an infinite loop.
	 * TBD: we need a mechanism to detect failure of this
	 * test instead of just looping forever.
	 * 
	 * @throws Exception
	 * 
	 * namespace A{
	 * }
	 * namespace B{
	 *    using namespace A;
	 * }
	 * namespace A{
	 *    using namespace B;
	 * }
	 * void f(){
	 *    using namespace A;
	 *    using namespace B;
	 *    i = 1; //not declared anywhere.
	 * }
	 */
	public void testUsing_SearchedOnce_2() throws Exception
	{
		newTable();
		
		Declaration nsA = new Declaration( "A" );
		nsA.setType( Declaration.t_namespace );
		table.addDeclaration( nsA );
			
		Declaration nsB = new Declaration( "B" );
		nsB.setType( Declaration.t_namespace );
		table.addDeclaration( nsB );
		table.push( nsB );
		table.addUsingDirective( nsA );
		table.pop();
		
		table.push( nsA );
		table.addUsingDirective( nsB );
		table.pop();

		Declaration f = new Declaration("f");
		table.addDeclaration(f);
		table.push(f);
		table.addUsingDirective(nsA);
		table.addUsingDirective(nsB);
		
		Declaration look = table.Lookup("i");
		assertEquals( look, null );
		
	}
	
	/**
	 * During lookup of a qualified namespace member name, if the lookup finds
	 * more than one declaration of the member, non-type names hide class or
	 * enumeration names if and only if the declarations are from the same
	 * namespace
	 * @throws Exception
	 * 
	 * namespace A {
	 *    struct x { };
	 *    int x;
	 *    int y;
	 * }
	 * namespace B {
	 *    struct y { };
	 * }
	 * 
	 * namespace C {
	 *    using namespace A;
	 *    using namespace B;
	 * 
	 *    int i = C::x;      //ok, finds A::x
	 *    int j = C::y;      //ambiguous, A::y or B::y
	 * }
	 */
	public void testNamespaceMemberHiding() throws Exception{
		newTable();
		
		Declaration nsA = new Declaration("A");
		nsA.setType( Declaration.t_namespace );
		
		table.addDeclaration( nsA );
		table.push( nsA );
		
		Declaration structX = new Declaration("x");
		structX.setType( Declaration.t_struct );
		table.addDeclaration( structX );
		
		Declaration intX = new Declaration("x");
		intX.setType( Declaration.t_int );
		table.addDeclaration( intX );
		
		Declaration intY = new Declaration("y");
		intY.setType( Declaration.t_int );
		table.addDeclaration( intY );

		table.pop();
		
		Declaration nsB = new Declaration("B");
		nsB.setType( Declaration.t_namespace );
		
		table.addDeclaration( nsB );
		table.push( nsB );
		
		Declaration structY = new Declaration("y");
		structY.setType( Declaration.t_struct );
		table.addDeclaration( structY );
		
		table.pop();
		
		Declaration nsC = new Declaration("C");
		nsC.setType( Declaration.t_namespace);
		table.addDeclaration( nsC );
		
		table.push( nsC );
		
		Declaration look = table.Lookup("A");
		assertEquals( look, nsA );
		table.addUsingDirective( look );
		
		look = table.Lookup("B");
		assertEquals( look, nsB );
		table.addUsingDirective( look );
		
		//lookup C::x
		look = table.LookupNestedNameSpecifier("C");
		assertEquals( look, nsC );
		table.push(look);
		look = table.QualifiedLookup( "x" );
		assertEquals( look, intX );
		table.pop();
		
		//lookup C::y
		look = table.LookupNestedNameSpecifier("C");
		assertEquals( look, nsC );
		table.push(look);
		try{
			look = table.QualifiedLookup( "y" );
			assertTrue(false);
		} catch ( Exception e ) {
			assertTrue(true);
		}
	}
	
}
