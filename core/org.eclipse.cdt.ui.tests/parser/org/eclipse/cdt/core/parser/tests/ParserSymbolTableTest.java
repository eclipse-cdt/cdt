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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.core.parser.Declaration;
import org.eclipse.cdt.internal.core.parser.ParserSymbolTable;
import org.eclipse.cdt.internal.core.parser.ParserSymbolTableException;
import org.eclipse.cdt.internal.core.parser.util.TypeInfo;

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
	 * 
	 * @throws Exception
	 * test for circular inheritance 
	 */
	public void testCircularParentLookup() throws Exception{
		newTable();
		
		Declaration a = new Declaration("a");
		table.addDeclaration( a );
		
		Declaration b = new Declaration("b");
		table.addDeclaration(b);
		
		a.addParent( b );
		b.addParent( a );
		
		table.push( a );
		 
		try{
			Declaration look = table.Lookup("foo");
			assertTrue( false );
		} catch ( ParserSymbolTableException e) {
			assertEquals( e.reason, ParserSymbolTableException.r_CircularInheritance );
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
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
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
		enum.setType( TypeInfo.t_enumeration );
		
		Declaration enumerator = new Declaration( "enumerator" );
		enumerator.setType( TypeInfo.t_enumerator );
		
		Declaration stat = new Declaration("static");
		stat.getTypeInfo().setBit( true, TypeInfo.isStatic );
		
		Declaration x = new Declaration("x");
		
		table.push(d);
		table.addDeclaration( enum );
		table.push( enum );
		table.addDeclaration( enumerator );
		table.pop();
		table.addDeclaration( stat );
		table.addDeclaration( x );
		table.pop();
		
		a.addParent( b );
		a.addParent( c );
		b.addParent( d );
		c.addParent( d );
		
		table.push( a );
		try{
			table.Lookup( "enumerator" );
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
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
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
		cls.setType( TypeInfo.t_class );
		
		Declaration struct = new Declaration("struct");
		struct.setType( TypeInfo.t_struct );
		
		Declaration union = new Declaration("union");
		union.setType( TypeInfo.t_union );
		
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
		
		Declaration look = table.ElaboratedLookup( TypeInfo.t_class, "class" );
		assertEquals( look, cls );
		look = table.ElaboratedLookup( TypeInfo.t_struct, "struct" );
		assertEquals( look, struct );
		look = table.ElaboratedLookup( TypeInfo.t_union, "union" );
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
		struct.setType( TypeInfo.t_struct );
		table.addDeclaration( struct );
		
		Declaration function = new Declaration( "stat" );
		function.setType( TypeInfo.t_function );
		table.addDeclaration( function );
		
		Declaration f = new Declaration("f");
		f.setType( TypeInfo.t_function );
		table.addDeclaration( f );
		table.push( f );
		
		Declaration look = table.ElaboratedLookup( TypeInfo.t_struct, "stat" );
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
		nsA.setType( TypeInfo.t_namespace );
		table.addDeclaration( nsA );
		table.push( nsA );
		
		Declaration nsA_i = new Declaration("i");
		table.addDeclaration( nsA_i );
		
		Declaration nsB = new Declaration("B");
		nsB.setType( TypeInfo.t_namespace );
		table.addDeclaration( nsB );
		table.push( nsB );
		
		Declaration nsC = new Declaration("C");
		nsC.setType( TypeInfo.t_namespace );
		table.addDeclaration( nsC );
		table.push( nsC );
		
		Declaration nsC_i = new Declaration("i");
		table.addDeclaration( nsC_i );
		table.pop();
		
		Declaration look = table.Lookup("C");
		table.addUsingDirective( look );
		
		Declaration f1 = new Declaration("f");
		f1.setType( TypeInfo.t_function );
		table.push( f1 );
		
		look = table.Lookup( "i" );
		assertEquals( look, nsC_i ); //C::i visible and hides A::i
		
		table.pop();  //end of f1
		table.pop();  //end of nsB
		
		assertEquals( table.peek(), nsA );
		
		Declaration nsD = new Declaration("D");
		nsD.setType( TypeInfo.t_namespace );
		table.addDeclaration( nsD );
		table.push( nsD );
		
		look = table.Lookup("B");
		assertEquals( look, nsB );
		table.addUsingDirective( look );
		
		look = table.Lookup("C");
		assertEquals( look, nsC );
		table.addUsingDirective( look );
		
		Declaration f2 = new Declaration( "f2" );
		f2.setType( TypeInfo.t_function );
		table.addDeclaration( f2 );
		table.push( f2 );
		
		try
		{
			look = table.Lookup( "i" );
			assertTrue( false );
		}
		catch ( ParserSymbolTableException e )
		{
			//ambiguous B::C::i and A::i
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		table.pop(); //end f2
		table.pop(); //end nsD
		
		Declaration f3 = new Declaration ("f3");
		f3.setType( TypeInfo.t_function );
		table.addDeclaration( f3 );
		table.push( f3 );
		
		look = table.Lookup("i");
		assertEquals( look, nsA_i );  //uses A::i
		
		table.pop();
		table.pop();
		
		Declaration f4 = new Declaration ("f4");
		f4.setType( TypeInfo.t_function );
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
		nsM.setType( TypeInfo.t_namespace );
		
		table.addDeclaration( nsM );
		
		table.push( nsM );
		Declaration nsM_i = new Declaration("i");
		table.addDeclaration( nsM_i );
		table.pop();
		
		Declaration nsN = new Declaration( "N" );
		nsN.setType( TypeInfo.t_namespace );
		
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
			//ambiguous, both M::i and N::i are visible.
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
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
		nsA.setType( TypeInfo.t_namespace );
		table.addDeclaration( nsA );
		table.push( nsA );
		
		Declaration a = new Declaration("a");
		table.addDeclaration( a );
		table.pop();
		
		Declaration nsB = new Declaration("B");
		nsB.setType( TypeInfo.t_namespace );
		table.addDeclaration( nsB );
		table.push( nsB );
		table.addUsingDirective( nsA );
		table.pop();

		Declaration nsC = new Declaration("C");
		nsC.setType( TypeInfo.t_namespace );
		table.addDeclaration( nsC );
		table.push( nsC );
		table.addUsingDirective( nsA );
		table.pop();	
		
		Declaration nsBC = new Declaration("BC");
		nsBC.setType( TypeInfo.t_namespace );
		table.addDeclaration( nsBC );
		table.push( nsBC );
		table.addUsingDirective( nsB );
		table.addUsingDirective( nsC );		
		table.pop();
		
		Declaration f = new Declaration("f");
		f.setType(TypeInfo.t_function);
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
		nsB.setType( TypeInfo.t_namespace );
		table.addDeclaration( nsB );
		table.push( nsB );
		
		Declaration b = new Declaration("b");
		table.addDeclaration( b );
		table.pop();
		
		Declaration nsA = new Declaration( "A" );
		nsA.setType( TypeInfo.t_namespace );
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
		nsA.setType( TypeInfo.t_namespace );
		table.addDeclaration( nsA );
			
		Declaration nsB = new Declaration( "B" );
		nsB.setType( TypeInfo.t_namespace );
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
		nsA.setType( TypeInfo.t_namespace );
		
		table.addDeclaration( nsA );
		table.push( nsA );
		
		Declaration structX = new Declaration("x");
		structX.setType( TypeInfo.t_struct );
		table.addDeclaration( structX );
		
		Declaration intX = new Declaration("x");
		intX.setType( TypeInfo.t_int );
		table.addDeclaration( intX );
		
		Declaration intY = new Declaration("y");
		intY.setType( TypeInfo.t_int );
		table.addDeclaration( intY );

		table.pop();
		
		Declaration nsB = new Declaration("B");
		nsB.setType( TypeInfo.t_namespace );
		
		table.addDeclaration( nsB );
		table.push( nsB );
		
		Declaration structY = new Declaration("y");
		structY.setType( TypeInfo.t_struct );
		table.addDeclaration( structY );
		
		table.pop();
		
		Declaration nsC = new Declaration("C");
		nsC.setType( TypeInfo.t_namespace);
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
		} catch ( ParserSymbolTableException e ) {
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
	}
	
	/**
	 * In a definition for a namespace member in which the declarator-id is a
	 * qualified-id, given that the qualified-id for the namespace member has
	 * the form "nested-name-specifier unqualified-id", the unqualified-id shall
	 * name a member of the namespace designated by the nested-name-specifier.
	 * 
	 * namespace A{    
	 *    namespace B{       
	 *       void  f1(int);    
	 *    }  
	 *    using  namespace B; 
	 * }
	 * void A::f1(int) { ... } //ill-formed, f1 is not a member of A
	 */
	public void testLookupMemberForDefinition() throws Exception{
		newTable();
	
		Declaration nsA = new Declaration( "A" );
		nsA.setType( TypeInfo.t_namespace );
		table.addDeclaration( nsA );
		table.push( nsA );
	
		Declaration nsB = new Declaration( "B" );
		nsB.setType( TypeInfo.t_namespace );
		table.addDeclaration( nsB );
		table.push( nsB );
	
		Declaration f1 = new Declaration("f1");
		f1.setType( TypeInfo.t_function );
		table.addDeclaration( f1 );
	
		table.pop();
	
		table.addUsingDirective( nsB );
		table.pop();
	
		Declaration look = table.LookupNestedNameSpecifier( "A" );
		assertEquals( nsA, look );
		table.push( look );
	
		look = table.LookupMemberForDefinition( "f1" );
		assertEquals( look, null );
	
		//but notice if you wanted to do A::f1 as a function call, it is ok
		look = table.QualifiedLookup( "f1" );
		assertEquals( look, f1 );
	}
	
	/**
	 * testUsingDeclaration
	 * @throws Exception
	 * 7.3.3-4 A using-declaration used as a member-declaration shall refer to a
	 * member of a base-class of the class being defined, shall refer to a
	 * member of an anonymous union that is a member of a base class of the
	 * class being defined or shall refer to an enumerator for an enumeration
	 * type that is a member of a base class of the class being defined
	 *
	 * struct B {
	 *    void f( char );
	 *    enum E { e };
	 *    union { int x; };
	 * };
	 * class C {
	 *	  int g();
	 * }
	 * struct D : B {
	 *    using B::f;	//ok, B is a base class of D
	 *    using B::e;   //ok, e is an enumerator in base class B
	 *    using B::x;   //ok, x is an union member of base class B
	 *    using C::g;   //error, C isn't a base class of D
	 * }
	 */
	public void testUsingDeclaration() throws Exception{
		newTable();
		
		Declaration B = new Declaration("B");
		B.setType( TypeInfo.t_struct );
		table.addDeclaration( B );
		table.push( B );
		
		Declaration f = new Declaration("f");
		f.setType( TypeInfo.t_function );
		table.addDeclaration( f );
	
		Declaration E = new Declaration( "E" );
		E.setType( TypeInfo.t_enumeration );
		table.addDeclaration( E );
		
		table.push( E );
		Declaration e = new Declaration( "e" );
		e.setType( TypeInfo.t_enumerator );
		table.addDeclaration( e );
		table.pop();
		
		//TBD: Anonymous unions are not yet implemented
		
		table.pop();
		
		Declaration C = new Declaration( "C" );
		C.setType( TypeInfo.t_class );
		table.addDeclaration( C );
		
		table.push( C );
		Declaration g = new Declaration( "g" );
		g.setType( TypeInfo.t_function );
		table.addDeclaration( g );
		table.pop();
		
		Declaration D = new Declaration( "D" );
		D.setType( TypeInfo.t_struct );
		Declaration look = table.Lookup( "B" );
		assertEquals( look, B );
		D.addParent( look );
		
		table.addDeclaration( D );
		table.push( D );
		
		Declaration lookB = table.LookupNestedNameSpecifier("B");
		assertEquals( lookB, B );

		table.addUsingDeclaration( "f", lookB );
		
		table.addUsingDeclaration( "e", lookB );
		  
		//TBD anonymous union
		//table.addUsingDeclaration( "x", lookB );
		
		look = table.LookupNestedNameSpecifier("C");
		assertEquals( look, C );
		
		try{
			table.addUsingDeclaration( "g", look );
			assertTrue( false );
		}
		catch ( ParserSymbolTableException exception ){
			assertTrue( true );
		}
	}
	
	/**
	 * testUsingDeclaration_2
	 * @throws Exception
	 * 7.3.3-9 The entity declared by a using-declaration shall be known in the
	 * context using it according to its definition at the point of the using-
	 * declaration.  Definitions added to the namespace after the using-
	 * declaration are not considered when a use of the name is made.
	 * 
	 * namespace A {
	 *     void f(int);
	 * }
	 * using A::f;
	 * 
	 * namespace A {
	 * 	   void f(char);
	 * }
	 * void foo(){
	 * 	  f('a');    //calls f( int )
	 * }
	 * void bar(){
	 * 	  using A::f;
	 * 	  f('a');    //calls f( char );
	 * }	
	 */
	public void testUsingDeclaration_2() throws Exception{
		newTable();
		
		Declaration A = new Declaration( "A" );
		A.setType( TypeInfo.t_namespace );
		table.addDeclaration( A );
		
		table.push( A );
		
		Declaration f1 = new Declaration( "f" );
		f1.setType( TypeInfo.t_function );
		f1.setReturnType( TypeInfo.t_void );
		f1.addParameter( TypeInfo.t_int, 0, "", false );
		table.addDeclaration( f1 );
		
		table.pop();
		
		Declaration look = table.LookupNestedNameSpecifier("A");
		assertEquals( look, A );
		
		Declaration usingF = table.addUsingDeclaration( "f", look );
		
		look = table.Lookup("A");
		assertEquals( look, A );
		
		table.push( look );
		Declaration f2 = new Declaration("f");
		f2.setType( TypeInfo.t_function );
		f2.setReturnType( TypeInfo.t_void );
		f2.addParameter( TypeInfo.t_char, 0, "", false );
		
		table.addDeclaration( f2 );
		
		table.pop();
		
		Declaration foo = new Declaration("foo");
		foo.setType( TypeInfo.t_function );
		table.addDeclaration( foo );
		table.push( foo );
		LinkedList paramList = new LinkedList();
		TypeInfo param = new TypeInfo( TypeInfo.t_char, null );
		paramList.add( param );
		
		look = table.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, usingF );
		assertTrue( look.hasSameParameters( f1 ) );
		
		Declaration bar = new Declaration( "bar" );
		bar.setType( TypeInfo.t_function );
		bar.addParameter( TypeInfo.t_char, 0, null, false );
		table.addDeclaration( bar );
		table.push( bar );
		
		look = table.LookupNestedNameSpecifier( "A" );
		assertEquals( look, A );
		table.addUsingDeclaration( "f", A );
		
		look = table.UnqualifiedFunctionLookup( "f", paramList );
		assertTrue( look != null );
		assertTrue( look.hasSameParameters( f2 ) );
		
		table.pop();
	}
	
	/**
	 * testThisPointer
	 * @throws Exception
	 * In the body of a nonstatic member function... the type of this of a class
	 * X is X*.  If the member function is declared const, the type of this is
	 * const X*, if the member function is declared volatile, the type of this
	 * is volatile X*....
	 */
	public void testThisPointer() throws Exception{
		newTable();
		
		Declaration cls = new Declaration("class");
		cls.setType( TypeInfo.t_class );
		
		Declaration fn = new Declaration("function");
		fn.setType( TypeInfo.t_function );
		fn.setCVQualifier( TypeInfo.cvConst );
		
		table.addDeclaration( cls );
		table.push( cls );
		
		table.addDeclaration( fn );
		table.push( fn );
		
		Declaration look = table.Lookup("this");
		assertTrue( look != null );
		
		assertEquals( look.getType(), TypeInfo.t_type );
		assertEquals( look.getTypeDeclaration(), cls );
		assertEquals( look.getPtrOperator(), "*" );
		assertEquals( look.getCVQualifier(), fn.getCVQualifier() );
		assertEquals( look.getContainingScope(), fn );
	}
	
	/**
	 * testEnumerator
	 * @throws Exception
	 * Following the closing brace of an enum-specifier, each enumerator has the
	 * type of its enumeration.
	 * The enum-name and each enumerator declared by an enum-specifier is
	 * declared in the scope that immediately contains the enum-specifier
	 */
	public void testEnumerator() throws Exception{
		newTable();
		
		Declaration cls = new Declaration("class");
		cls.setType( TypeInfo.t_class );
		
		Declaration enumeration = new Declaration("enumeration");
		enumeration.setType( TypeInfo.t_enumeration );
		
		table.addDeclaration( cls );
		table.push( cls );
		table.addDeclaration( enumeration );
		table.push( enumeration );
		
		Declaration enumerator = new Declaration( "enumerator" );
		enumerator.setType( TypeInfo.t_enumerator );
		table.addDeclaration( enumerator );
		
		table.pop();
		
		Declaration look = table.Lookup( "enumerator" );
		assertEquals( look, enumerator );
		assertEquals( look.getContainingScope(), cls );
		assertEquals( look.getTypeDeclaration(), enumeration );
	}

	/**
	 * 
	 * @throws Exception
	 * 
	 * namespace NS{
	 *    class T {};
	 *    void f( T );
	 * }
	 * NS::T parm;
	 * int main(){
	 *    f( parm );   //ok, calls NS::f
	 * }
	 */
	public void testArgumentDependentLookup() throws Exception{
		newTable();
		
		Declaration NS = new Declaration("NS");
		NS.setType( TypeInfo.t_namespace );
		
		table.addDeclaration( NS );
		table.push( NS );
		
		Declaration T = new Declaration("T");
		T.setType( TypeInfo.t_class );
		
		table.addDeclaration( T );
		
		Declaration f = new Declaration("f");
		f.setType( TypeInfo.t_function );
		f.setReturnType( TypeInfo.t_void );
		
		Declaration look = table.Lookup( "T" );
		assertEquals( look, T );				
		f.addParameter( look, 0, "", false );
		
		table.addDeclaration( f );	
		
		table.pop(); //done NS
				
		look = table.LookupNestedNameSpecifier( "NS" );
		assertEquals( look, NS );
		table.push( look );
		look = table.QualifiedLookup( "T" );
		assertEquals( look, T );
		table.pop();
		
		Declaration param = new Declaration("parm");
		param.setType( TypeInfo.t_type );
		param.setTypeDeclaration( look );
		table.addDeclaration( param );
		
		Declaration main = new Declaration("main");
		main.setType( TypeInfo.t_function );
		main.setReturnType( TypeInfo.t_int );
		table.addDeclaration( main );
		table.push( main );
		
		LinkedList paramList = new LinkedList();
		look = table.Lookup( "parm" );
		assertEquals( look, param );
		TypeInfo p = new TypeInfo( TypeInfo.t_type, look, 0, null, false );
		paramList.add( p );
		
		look = table.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f );
	}
	
	/**
	 * testArgumentDependentLookup_2
	 * @throws Exception
	 * in the following, NS2 is an associated namespace of class B which is an
	 * associated namespace of class A, so we should find f in NS2, we should
	 * not find f in NS1 because usings are ignored for associated scopes.
	 * 
	 *
	 * namespace NS1{
	 *    void f( void * ){}; 
	 * } 
	 * namespace NS2{
	 *	  using namespace NS1;
	 * 	  class B {};
	 *	  void f( void * ){}; 
	 * }
	 * 
	 * class A : public NS2::B {};
	 *
	 * A a;
	 * f( &a );
	 *    
	 */
	public void testArgumentDependentLookup_2() throws Exception{
		newTable();
		
		Declaration NS1 = new Declaration( "NS1" );
		NS1.setType( TypeInfo.t_namespace );
		 
		table.addDeclaration( NS1 );
		table.push( NS1 );
		
		Declaration f1 = new Declaration( "f" );
		f1.setType( TypeInfo.t_function );
		f1.setReturnType( TypeInfo.t_void );
		f1.addParameter( TypeInfo.t_void, 0, "*", false );
		table.addDeclaration( f1 );
		table.pop();
		
		Declaration NS2 = new Declaration( "NS2" );
		NS2.setType( TypeInfo.t_namespace );
		
		table.addDeclaration( NS2 );
		table.push( NS2 );
		
		Declaration look = table.Lookup( "NS1" );
		assertEquals( look, NS1 );
		table.addUsingDirective( look );
		
		Declaration B = new Declaration( "B" );
		B.setType( TypeInfo.t_class );
		table.addDeclaration( B );
		
		Declaration f2 = new Declaration( "f" );
		f2.setType( TypeInfo.t_function );
		f2.setReturnType( TypeInfo.t_void );
		f2.addParameter( TypeInfo.t_void, 0, "*", false );
		table.addDeclaration( f2 );
		table.pop();
		
		Declaration A = new Declaration( "A" );
		A.setType( TypeInfo.t_class );
		look = table.LookupNestedNameSpecifier( "NS2" );
		assertEquals( look, NS2 );
		table.push( look );
		look = table.QualifiedLookup( "B" );
		assertEquals( look, B );
		A.addParent( look );
		
		table.addDeclaration( A );
		
		look = table.Lookup( "A" );
		assertEquals( look, A );
		Declaration a = new Declaration( "a" );
		a.setType( TypeInfo.t_type );
		a.setTypeDeclaration( look );
		table.addDeclaration( a );
		
		LinkedList paramList = new LinkedList();
		look = table.Lookup( "a" );
		assertEquals( look, a );
		TypeInfo param = new TypeInfo( look.getType(), look, 0, "&", false );
		paramList.add( param );
		
		look = table.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f2 );
	}
	
	/**
	 * testFunctionOverloading
	 * @throws Exception
	 * Note that this test has been contrived to not strain the resolution as
	 * that aspect is not yet complete.
	 * 
	 * class C
	 * {   
	 *    void foo( int i );
	 *    void foo( int i, char c );
	 *    void foo( int i, char c, C * ptr ); 
	 * }
	 *
	 * C * c = new C;
	 * c->foo( 1 );
	 * c->foo( 1, 'a' );
	 * c->foo( 1, 'a', c );
	 * 
	 */
	
	public void testFunctionOverloading() throws Exception{
		newTable();
		
		Declaration C = new Declaration( "C" );
		C.setType( TypeInfo.t_class );
		table.addDeclaration(C);
		table.push(C);
				
		Declaration f1 = new Declaration("foo");
		f1.setType( TypeInfo.t_function );
		f1.setReturnType( TypeInfo.t_void );
		f1.addParameter( TypeInfo.t_int, 0, "", false );
		table.addDeclaration( f1 );
		
		Declaration f2 = new Declaration("foo");
		f2.setType( TypeInfo.t_function );
		f2.setReturnType( TypeInfo.t_void );
		f2.addParameter( TypeInfo.t_int, 0, "", false );
		f2.addParameter( TypeInfo.t_char, 0, "", false );
		table.addDeclaration( f2 );
		
		Declaration f3 = new Declaration("foo");
		f3.setType( TypeInfo.t_function );
		f3.setReturnType( TypeInfo.t_void );
		f3.addParameter( TypeInfo.t_int, 0, "", false );
		f3.addParameter( TypeInfo.t_char, 0, "", false );
		f3.addParameter( C, 0, "*", false );
		table.addDeclaration( f3 );
		table.pop();
		
		Declaration look = table.Lookup("C");
		assertEquals( look, C );
		
		Declaration c = new Declaration("c");
		c.setType( TypeInfo.t_type );
		c.setTypeDeclaration( look );
		c.setPtrOperator( "*" );
		table.addDeclaration( c );
		
		look = table.Lookup( "c" );
		assertEquals( look, c );
		assertEquals( look.getTypeDeclaration(), C );
		table.push( look.getTypeDeclaration() );
		
		LinkedList paramList = new LinkedList();
		TypeInfo p1 = new TypeInfo( TypeInfo.t_int, null, 0, "", false);
		TypeInfo p2 = new TypeInfo( TypeInfo.t_char, null, 0, "", false);
		TypeInfo p3 = new TypeInfo( TypeInfo.t_type, c, 0, "", false);
		
		paramList.add( p1 );
		look = table.MemberFunctionLookup( "foo", paramList );
		assertEquals( look, f1 );
		
		paramList.add( p2 );
		look = table.MemberFunctionLookup( "foo", paramList );
		assertEquals( look, f2 );
				
		paramList.add( p3 );
		look = table.MemberFunctionLookup( "foo", paramList );
		assertEquals( look, f3 );
	}
	
	/**
	 * 
	 * @throws Exception
	 * test basic function resolution
	 * 
	 * void f( int i ); 
	 * void f( char c = 'c' );
	 * 
	 * f( 1 );		//calls f( int );
	 * f( 'b' ); 	//calls f( char );
	 * f(); 		//calls f( char );
	 */
	public void testFunctionResolution() throws Exception{
		newTable();
		
		Declaration f1 = new Declaration("f");
		f1.setType( TypeInfo.t_function );
		f1.addParameter( TypeInfo.t_int, 0, "", false );
		table.addDeclaration( f1 );
		
		Declaration f2 = new Declaration("f");
		f2.setType( TypeInfo.t_function );
		f2.addParameter( TypeInfo.t_char, 0, "", true );
		table.addDeclaration( f2 );
		
		LinkedList paramList = new LinkedList();
		TypeInfo p1 = new TypeInfo( TypeInfo.t_int, null, 0, "", false );
		paramList.add( p1 );
		
		Declaration look = table.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		paramList.clear();
		TypeInfo p2 = new TypeInfo( TypeInfo.t_char, null, 0, "", false );
		paramList.add( p2 );
		look = table.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f2 );
		
		paramList.clear();
		TypeInfo p3 = new TypeInfo( TypeInfo.t_bool, null, 0, "", false );
		paramList.add( p3 );
		look = table.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		look = table.UnqualifiedFunctionLookup( "f", null );
		assertEquals( look, f2 );
	}
	
	/** 
	 * 
	 * @throws Exception
	 *
	 * class A { };
	 * class B : public A {};
	 * class C : public B {};
	 * 
	 * void f ( A * );
	 * void f ( B * );
	 * 
	 * A* a = new A();
	 * C* c = new C();
	 * 
	 * f( a );		//calls f( A * );
	 * f( c );		//calls f( B * );   	      
	 */
	public void testFunctionResolution_PointersAndBaseClasses() throws Exception{
		newTable();
		
		Declaration A = new Declaration( "A" );
		A.setType( TypeInfo.t_class );
		table.addDeclaration( A );
		
		Declaration B = new Declaration( "B" );
		B.setType( TypeInfo.t_class );
		B.addParent( A );
		table.addDeclaration( B );
		
		Declaration C = new Declaration( "C" );
		C.setType( TypeInfo.t_class );
		C.addParent( B );
		table.addDeclaration( C );
		
		Declaration f1 = new Declaration( "f" );
		f1.setType( TypeInfo.t_function );
		f1.addParameter( A, 0, "*", false );
		table.addDeclaration( f1 );
		
		Declaration f2 = new Declaration( "f" );
		f2.setType( TypeInfo.t_function );
		f2.addParameter( B, 0, "*", false );
		table.addDeclaration( f2 );
		
		Declaration a = new Declaration( "a" );
		a.setType( TypeInfo.t_type );
		a.setTypeDeclaration( A );
		a.setPtrOperator( "*" );
		
		Declaration c = new Declaration( "c" );
		c.setType( TypeInfo.t_type );
		c.setTypeDeclaration( C );
		c.setPtrOperator( "*" );
		
		LinkedList paramList = new LinkedList();
		TypeInfo p1 = new TypeInfo( TypeInfo.t_type, a, 0, null, false );
		paramList.add( p1 );
		Declaration look = table.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		paramList.clear();
		TypeInfo p2 = new TypeInfo( TypeInfo.t_type, c, 0, "", false );
		paramList.add( p2 );
		look = table.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f2 );
	}
	
	/**
	 * 
	 * @throws Exception
	 *
	 * class A {};
	 * typedef A * B;
	 * 
	 * void f( A * );
	 * void f( A );
	 * 
	 * A a;
	 * B b;
	 * A [] array;
	 *
	 * f( a ); 		//calls f( A );
	 * f( &a );		//calls f( A * );
	 * f( b );		//calls f( A * );
	 * f( *b );		//calls f( A );
	 * f( array );  //calls f( A * );
	 */
	public void testFunctionResolution_TypedefsAndPointers() throws Exception{
		newTable();
		
		Declaration A = new Declaration( "A" );
		A.setType( TypeInfo.t_class );
		table.addDeclaration( A );
		
		Declaration B = new Declaration( "B" );
		B.setType( TypeInfo.t_type );
		B.setTypeDeclaration( A );
		B.setPtrOperator( "*" );
		table.addDeclaration( B );
		
		Declaration f1 = new Declaration( "f" );
		f1.setType( TypeInfo.t_function );
		f1.addParameter( A, 0, "*", false );
		table.addDeclaration( f1 );
		
		Declaration f2 = new Declaration( "f" );
		f2.setType( TypeInfo.t_function );
		f2.addParameter( A, 0, null, false );
		table.addDeclaration( f2 );

		Declaration a = new Declaration( "a" );
		a.setType( TypeInfo.t_type );
		a.setTypeDeclaration( A );
		table.addDeclaration( a );
				
		Declaration b = new Declaration( "b" );
		b.setType( TypeInfo.t_type );
		b.setTypeDeclaration( B );
		table.addDeclaration( b );
		
		Declaration array = new Declaration( "array" );
		array.setType( TypeInfo.t_type );
		array.setTypeDeclaration( A );
		array.setPtrOperator( "[]" );
				
		LinkedList paramList = new LinkedList();
		TypeInfo p = new TypeInfo( TypeInfo.t_type, a, 0, null, false );
		paramList.add( p );
		
		Declaration look = table.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f2 );
		
		p.setPtrOperator( "&" );
		look = table.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		p.setTypeDeclaration( b );
		p.setPtrOperator( null );
		look = table.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		p.setPtrOperator( "*" );
		look = table.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f2 );
		
		p.setTypeDeclaration( array );
		p.setPtrOperator( null );
		look = table.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
	}
	
	/**
	 * 
	 * @throws Exception
	 *
	 *  class A {};
	 *
	 *	class B
	 *	{
	 *	   B( A a ){ };
	 *	};
	 *	
	 *	void f( B b ){};
	 *	
	 *  A a;
	 *	f( a );
	 */
	public void testUserDefinedConversionSequences() throws Exception{
		newTable();
		
		Declaration A = new Declaration( "A" );
		A.setType( TypeInfo.t_class );
		table.addDeclaration( A );
		
		Declaration B = new Declaration( "B" );
		B.setType( TypeInfo.t_class );
		table.addDeclaration( B );
		
		table.push( B );
		
		//12.1-1 "Constructors do not have names"
		Declaration constructor = new Declaration("");
		constructor.setType( TypeInfo.t_function );
		constructor.addParameter( A, 0, null, false );
		table.addDeclaration( constructor );
		
		table.pop();
		
		Declaration f = new Declaration( "f" );
		f.setType( TypeInfo.t_function );
		f.addParameter( B, 0, null, false );
		table.addDeclaration( f );
		
		Declaration a = new Declaration( "a" );
		a.setType( TypeInfo.t_type );
		a.setTypeDeclaration( A );
		table.addDeclaration( a );
		
		LinkedList paramList = new LinkedList();
		TypeInfo p = new TypeInfo( TypeInfo.t_type, a, 0, null, false );
		paramList.add( p );
		
		Declaration look = table.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f );	
	}
	
	/**
	 * 
	 * @throws Exception
	 *
	 * void f( const int *, short );
	 * void f( int *, int );
	 * 
	 * int i;
	 * short s;
	 *
	 * void main() {
	 * 	  f( &i, s );		//ambiguous because &i->int* is better than &i->const int *
	 * 	  					//but s-> short is better than s->int
	 * 	  f( &i, 1L );		//calls f(int *, int) because &i->int* is better than &i->const int *
	 * 	  					//and 1L->short and 1L->int are indistinguishable
	 * 	  f( &i, 'c' );		//calls f( int*, int) because &i->int * is better than &i->const int *
	 * 	  					//and c->int is better than c->short
	 * 	  f( (const)&i, 1L ); //calls f(const int *, short ) because const &i->int* is better than &i->int *
	 * 	  					   //and 1L->short and 1L->int are indistinguishable
	 * }
	 */
	public void testOverloadRanking() throws Exception{
		newTable();
		
		Declaration f1 = new Declaration( "f" );
		f1.setType( TypeInfo.t_function );
		f1.addParameter( TypeInfo.t_int, TypeInfo.cvConst, "*", false );
		f1.addParameter( TypeInfo.t_int | TypeInfo.isShort, 0, null, false );
		
		table.addDeclaration( f1 );
		
		Declaration f2 = new Declaration( "f" );
		f2.setType( TypeInfo.t_function );
		f2.addParameter( TypeInfo.t_int, 0, "*", false );
		f2.addParameter( TypeInfo.t_int, 0, null, false );
		table.addDeclaration( f2 );
		
		Declaration i = new Declaration( "i" );
		i.setType( TypeInfo.t_int );
		table.addDeclaration( i );
		
		Declaration s = new Declaration( "s" );
		s.setType( TypeInfo.t_int );
		s.getTypeInfo().setBit( true, TypeInfo.isShort );
		table.addDeclaration( s );
		
		Declaration main = new Declaration( "main" );
		main.setType( TypeInfo.t_function );
		table.addDeclaration( main );
		table.push( main );
		
		LinkedList params = new LinkedList();
		TypeInfo p1 = new TypeInfo( TypeInfo.t_type, i, 0, "&", false );
		TypeInfo p2 = new TypeInfo( TypeInfo.t_type, s, 0, null, false );
		params.add( p1 );
		params.add( p2 );
		
		Declaration look = null;
		
		try{
			look = table.UnqualifiedFunctionLookup( "f", params );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		
		params.clear();
		TypeInfo p3 = new TypeInfo( TypeInfo.t_int | TypeInfo.isLong, null, 0, null, false );
		params.add( p1 );
		params.add( p3 );
		look = table.UnqualifiedFunctionLookup( "f", params );
		assertEquals( look, f2 );
		
		params.clear();
		TypeInfo p4 = new TypeInfo( TypeInfo.t_char, null, 0, null, false );
		params.add( p1 );
		params.add( p4 );
		look = table.UnqualifiedFunctionLookup( "f", params );
		assertEquals( look, f2 );
		
		params.clear();
		p1.setCVQualifier( TypeInfo.cvConst );
		params.add( p1 );
		params.add( p3 );
		look = table.UnqualifiedFunctionLookup( "f", params );
		assertEquals( look, f1 );
		
	}
	
	/**
	 * 
	 * @throws Exception
	 *
	 * class B;
	 * class A { A( B& ); };
	 * class B { operator A(); };
	 *  
	 * void f(A){}
	 * 
	 * B b;
	 * f( b );	//ambiguous because b->A via constructor or conversion
	 *
	 * class C { C( B& ); };
	 *  
	 * void f(C){}
	 * 
	 * f( b );	//ambiguous because b->C via constructor and b->a via constructor/conversion
	 * 
	 * void f(B){}
	 * 
	 * f( b );  //calls f(B) 
	 */
	   
	public void testUserDefinedConversionByOperator() throws Exception{
		newTable();
		
		Declaration B = new Declaration( "B" );
		B.setType( TypeInfo.t_class );
		
		table.addDeclaration( B );
		
		Declaration A = new Declaration( "A" );
		A.setType( TypeInfo.t_class );
		table.addDeclaration( A );
		
		table.push( A );
		Declaration constructA = new Declaration( "" );
		constructA.setType( TypeInfo.t_function );
		constructA.addParameter( B, 0, "&", false );
		table.addDeclaration( constructA );
		table.pop();
		
		table.push( B );
		Declaration operator = new Declaration( "operator A" );
		operator.setType( TypeInfo.t_function );
		table.addDeclaration( operator );
		table.pop();
		
		Declaration f1 = new Declaration( "f" );
		f1.setType( TypeInfo.t_function );
		f1.addParameter( A, 0, null, false );
		table.addDeclaration( f1 );
		
		Declaration b = new Declaration( "b" );
		b.setType( TypeInfo.t_type );
		b.setTypeDeclaration( B );
		
		LinkedList params = new LinkedList();
		TypeInfo p1 = new TypeInfo( TypeInfo.t_type, b, 0, null, false );
		params.add( p1 );
		
		Declaration look = null;
		
		try{
			look = table.UnqualifiedFunctionLookup( "f", params );
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous ); 
		}
		
		Declaration C = new Declaration("C");
		C.setType( TypeInfo.t_class );
		table.addDeclaration( C );
		
		table.push( C );
		Declaration constructC = new Declaration("");
		constructC.setType( TypeInfo.t_function );
		constructC.addParameter( B, 0, "&", false );
		table.addDeclaration( constructC );
		table.pop();
		
		Declaration f2 = new Declaration( "f" );
		f2.setType( TypeInfo.t_function );
		f2.addParameter(  C, 0, null, false );
		table.addDeclaration( f2 );
		
		try{
			look = table.UnqualifiedFunctionLookup( "f", params );
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous ); 
		}
		
		Declaration f3 = new Declaration( "f" );
		f3.setType( TypeInfo.t_function );
		f3.addParameter(  B, 0, null, false );
		table.addDeclaration( f3 );
		
		look = table.UnqualifiedFunctionLookup( "f", params );
		assertEquals( look, f3 );
	}
}

