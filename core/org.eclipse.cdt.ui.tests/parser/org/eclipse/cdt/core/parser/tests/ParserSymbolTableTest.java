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
		
		ParserSymbolTable.Declaration x = table.new Declaration( "x" );
		ParserSymbolTable.Declaration compUnit = (ParserSymbolTable.Declaration) table.getCompilationUnit();
		compUnit.addDeclaration( x );
	
		Map declarations = compUnit.getContainedDeclarations();
		assertEquals( 1, declarations.size() );
		
		Iterator iter = declarations.values().iterator();
		ParserSymbolTable.Declaration contained = (ParserSymbolTable.Declaration) iter.next();
		
		assertEquals( false, iter.hasNext() );
		assertEquals( x, contained );
		assertEquals( contained.getName(), "x" );
	}

	/**
	 * testSimpleLookup
	 * Add a declaration to the table, then look it up.
	 * @throws Exception
	 */
	public void testSimpleLookup() throws Exception{
		newTable(); //new symbol table
		
		ParserSymbolTable.Declaration x = table.new Declaration( "x" );
		table.getCompilationUnit().addDeclaration( x );
		
		ParserSymbolTable.Declaration look = table.getCompilationUnit().Lookup( "x" );
		
		assertEquals( x, look );
	}
	
	public void testLookupNonExistant() throws Exception{
		newTable();
		
		ParserSymbolTable.Declaration look = table.getCompilationUnit().Lookup("boo");
		assertEquals( look, null );
	}
	
	/**
	 * testSimplePushPop
	 * test pushing and popping
	 * @throws Exception
	 *//*
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
	}*/

	public void testSimpleSetGetObject() throws Exception{
		newTable();
		
		ParserSymbolTable.Declaration x = table.new Declaration("x");
		
		Object obj = new Object();
		x.setCallbackExtension( obj );
				
		table.getCompilationUnit().addDeclaration( x );
		
		ParserSymbolTable.Declaration look = table.getCompilationUnit().Lookup( "x" );
		
		assertEquals( look.getCallbackExtension(), obj );
	}
	
	/**
	 * testHide
	 * test that a declaration in a scope hides declarations in containing
	 * scopes
	 * @throws Exception
	 */
	public void testHide() throws Exception{
		newTable();
		
		ParserSymbolTable.Declaration firstX = table.new Declaration("x");
		table.getCompilationUnit().addDeclaration( firstX );
		
		ParserSymbolTable.Declaration firstClass = table.new Declaration("class");
		firstClass.setType( ParserSymbolTable.TypeInfo.t_class );
		table.getCompilationUnit().addDeclaration( firstClass );

		ParserSymbolTable.Declaration look = firstClass.Lookup( "x" );
		assertEquals( look, firstX );
		
		ParserSymbolTable.Declaration secondX = table.new Declaration("x");
		firstClass.addDeclaration( secondX );
		
		look = firstClass.Lookup( "x" );
		assertEquals( look, secondX );
		
		look = table.getCompilationUnit().Lookup( "x" );
		assertEquals( look, firstX );
	}
	
	/**
	 * testContainingScopeLookup
	 * test lookup of something declared in the containing scope
	 * @throws Exception
	 */
	public void testContainingScopeLookup() throws Exception{
		newTable();
		
		ParserSymbolTable.Declaration x = table.new Declaration("x");
		table.getCompilationUnit().addDeclaration( x );

		ParserSymbolTable.Declaration decl = table.new Declaration("class");
		decl.setType( ParserSymbolTable.TypeInfo.t_class );
		table.getCompilationUnit().addDeclaration( decl );
		
		ParserSymbolTable.Declaration look = decl.Lookup( "x" );
		
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
		
		ParserSymbolTable.Declaration parent = table.new Declaration("parent");
		parent.setType( ParserSymbolTable.TypeInfo.t_class );

		ParserSymbolTable.Declaration class1 = table.new Declaration("class");
		class1.setType( ParserSymbolTable.TypeInfo.t_class );
		class1.addParent( parent );
		
		ParserSymbolTable.Declaration decl = table.new Declaration("x");
		parent.addDeclaration( decl );
		
		table.getCompilationUnit().addDeclaration( parent );
		table.getCompilationUnit().addDeclaration( class1 );
		
		ParserSymbolTable.Declaration look = class1.Lookup( "x" );
		assertEquals( look, decl );
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
	
		ParserSymbolTable.Declaration parent2 = table.new Declaration("parent2");
		table.getCompilationUnit().addDeclaration( parent2 );
		
		ParserSymbolTable.Declaration class1 = table.getCompilationUnit().Lookup( "class" );
		class1.addParent( parent2 );
		
		ParserSymbolTable.Declaration decl = table.new Declaration("x");
		parent2.addDeclaration( decl );
				
		try{
			class1.Lookup( "x" );
			assertTrue( false );
		}
		catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
	}	
	
	/**
	 * 
	 * @throws Exception
	 * test for circular inheritance 
	 */
	public void testCircularParentLookup() throws Exception{
		newTable();
		
		ParserSymbolTable.Declaration a = table.new Declaration("a");
		table.getCompilationUnit().addDeclaration( a );
		
		ParserSymbolTable.Declaration b = table.new Declaration("b");
		b.addParent( a );
		table.getCompilationUnit().addDeclaration( b );
			
		a.addParent( b );
		 
		try{
			ParserSymbolTable.Declaration look = a.Lookup("foo");
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
		
		ParserSymbolTable.Declaration decl = table.new Declaration("class");
		ParserSymbolTable.Declaration c    = table.new Declaration("C");
		
		ParserSymbolTable.Declaration a    = table.new Declaration("A");
		a.addParent( c, true );
		
		ParserSymbolTable.Declaration b    = table.new Declaration("B");
		b.addParent( c, true );
		
		decl.addParent( a );
		decl.addParent( b );
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		compUnit.addDeclaration( c );
		
		ParserSymbolTable.Declaration x = table.new Declaration( "x" );
		c.addDeclaration( x );
		
		compUnit.addDeclaration( decl );
		compUnit.addDeclaration( a );
		compUnit.addDeclaration( b );
		
		ParserSymbolTable.Declaration look = decl.Lookup( "x" ); 
		
		assertEquals( look, x );
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration cls = compUnit.Lookup("class");
		ParserSymbolTable.Declaration c   = compUnit.Lookup("C");
		ParserSymbolTable.Declaration d   = table.new Declaration("D");
		
		d.addParent( c );
		cls.addParent( d );
		
		try{
			cls.Lookup( "x" );
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
		
		ParserSymbolTable.Declaration a = table.new Declaration( "a" );
		ParserSymbolTable.Declaration b = table.new Declaration( "b" );
		ParserSymbolTable.Declaration c = table.new Declaration( "c" );
		ParserSymbolTable.Declaration d = table.new Declaration( "d" );
	
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		compUnit.addDeclaration( a );
		compUnit.addDeclaration( b );
		compUnit.addDeclaration( c );
		compUnit.addDeclaration( d );
		
		ParserSymbolTable.Declaration enum = table.new Declaration("enum");
		enum.setType( ParserSymbolTable.TypeInfo.t_enumeration );
		
		ParserSymbolTable.Declaration enumerator = table.new Declaration( "enumerator" );
		enumerator.setType( ParserSymbolTable.TypeInfo.t_enumerator );
		
		ParserSymbolTable.Declaration stat = table.new Declaration("static");
		stat.getTypeInfo().setBit( true, ParserSymbolTable.TypeInfo.isStatic );
		
		ParserSymbolTable.Declaration x = table.new Declaration("x");
		
		d.addDeclaration( enum );
		d.addDeclaration( stat );
		d.addDeclaration( x );
		
		enum.addDeclaration( enumerator );
		
		a.addParent( b );
		a.addParent( c );
		b.addParent( d );
		c.addParent( d );
		
		try{
			a.Lookup( "enumerator" );
			assertTrue( true );	
		}
		catch ( ParserSymbolTableException e){
			assertTrue( false );
		}
		
		try{
			a.Lookup( "static" );
			assertTrue( true );	
		}
		catch ( ParserSymbolTableException e){
			assertTrue( false );
		}
		
		try{
			a.Lookup( "x" );
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
		
		ParserSymbolTable.Declaration cls = table.new Declaration( "class" );
		cls.setType( ParserSymbolTable.TypeInfo.t_class );
		
		ParserSymbolTable.Declaration struct = table.new Declaration("struct");
		struct.setType( ParserSymbolTable.TypeInfo.t_struct );
		
		ParserSymbolTable.Declaration union = table.new Declaration("union");
		union.setType( ParserSymbolTable.TypeInfo.t_union );
		
		ParserSymbolTable.Declaration hideCls = table.new Declaration( "class" );
		ParserSymbolTable.Declaration hideStruct = table.new Declaration("struct");
		ParserSymbolTable.Declaration hideUnion = table.new Declaration("union");
		
		ParserSymbolTable.Declaration a = table.new Declaration("a");
		ParserSymbolTable.Declaration b = table.new Declaration("b");
		
		a.addDeclaration(hideCls);
		a.addDeclaration(hideStruct);
		a.addDeclaration(hideUnion);
		
		a.addParent( b );
		
		b.addDeclaration(cls);
		b.addDeclaration(struct);
		b.addDeclaration(union);
		
		ParserSymbolTable.Declaration look = a.ElaboratedLookup( ParserSymbolTable.TypeInfo.t_class, "class" );
		assertEquals( look, cls );
		look = a.ElaboratedLookup( ParserSymbolTable.TypeInfo.t_struct, "struct" );
		assertEquals( look, struct );
		look = a.ElaboratedLookup( ParserSymbolTable.TypeInfo.t_union, "union" );
		assertEquals( look, union );
	}
	
	/**
	 * testDeclarationType
	 * @throws Exception
	 * test the use of ParserSymbolTable.Declaration type in the scenario
	 * 		A a;
	 * 		a.member <=...>;
	 * where A was previously declared
	 */
	public void testDeclarationType() throws Exception{
		newTable();
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		//pre-condition
		ParserSymbolTable.Declaration A = table.new Declaration("A");
		compUnit.addDeclaration(A);

		ParserSymbolTable.Declaration member = table.new Declaration("member");
		A.addDeclaration(member);
				
		//at time of "A a;"
		ParserSymbolTable.Declaration look = compUnit.Lookup("A");
		assertEquals( look, A );
		ParserSymbolTable.Declaration a = table.new Declaration("a");
		a.setTypeDeclaration( look );
		compUnit.addDeclaration( a );
		
		//later "a.member"
		look = compUnit.Lookup("a");
		assertEquals( look, a );
		ParserSymbolTable.Declaration type = look.getTypeDeclaration();
		assertEquals( type, A );
		
		look = type.Lookup("member");
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration struct = table.new Declaration( "stat");
		struct.setType( ParserSymbolTable.TypeInfo.t_struct );
		compUnit.addDeclaration( struct );
		
		ParserSymbolTable.Declaration function = table.new Declaration( "stat" );
		function.setType( ParserSymbolTable.TypeInfo.t_function );
		compUnit.addDeclaration( function );
		
		ParserSymbolTable.Declaration f = table.new Declaration("f");
		f.setType( ParserSymbolTable.TypeInfo.t_function );
		compUnit.addDeclaration( f );
				
		ParserSymbolTable.Declaration look = f.ElaboratedLookup( ParserSymbolTable.TypeInfo.t_struct, "stat" );
		assertEquals( look, struct );
		
		look = f.Lookup( "stat" );
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
		
		ParserSymbolTable.Declaration nsA = table.new Declaration("A");
		nsA.setType( ParserSymbolTable.TypeInfo.t_namespace );
		table.getCompilationUnit().addDeclaration( nsA );
		
		ParserSymbolTable.Declaration nsA_i = table.new Declaration("i");
		nsA.addDeclaration( nsA_i );
		
		ParserSymbolTable.Declaration nsB = table.new Declaration("B");
		nsB.setType( ParserSymbolTable.TypeInfo.t_namespace );
		nsA.addDeclaration( nsB );
		
		ParserSymbolTable.Declaration nsC = table.new Declaration("C");
		nsC.setType( ParserSymbolTable.TypeInfo.t_namespace );
		nsB.addDeclaration( nsC );
		
		ParserSymbolTable.Declaration nsC_i = table.new Declaration("i");
		nsC.addDeclaration( nsC_i );
		
		ParserSymbolTable.Declaration look = nsB.Lookup("C");
		nsB.addUsingDirective( look );
		
		ParserSymbolTable.Declaration f1 = table.new Declaration("f");
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		
		nsB.addDeclaration( f1 );
		
		look = f1.Lookup( "i" );
		assertEquals( look, nsC_i ); //C::i visible and hides A::i
		
		ParserSymbolTable.Declaration nsD = table.new Declaration("D");
		nsD.setType( ParserSymbolTable.TypeInfo.t_namespace );
		nsA.addDeclaration( nsD );
		
		look = nsD.Lookup("B");
		assertEquals( look, nsB );
		nsD.addUsingDirective( look );
		
		look = nsD.Lookup("C");
		assertEquals( look, nsC );
		nsD.addUsingDirective( look );
		
		ParserSymbolTable.Declaration f2 = table.new Declaration( "f2" );
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		nsD.addDeclaration( f2 );
		
		try
		{
			look = f2.Lookup( "i" );
			assertTrue( false );
		}
		catch ( ParserSymbolTableException e )
		{
			//ambiguous B::C::i and A::i
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		
		ParserSymbolTable.Declaration f3 = table.new Declaration ("f3");
		f3.setType( ParserSymbolTable.TypeInfo.t_function );
		nsA.addDeclaration( f3 );
		
		look = f3.Lookup("i");
		assertEquals( look, nsA_i );  //uses A::i
		
		ParserSymbolTable.Declaration f4 = table.new Declaration ("f4");
		f4.setType( ParserSymbolTable.TypeInfo.t_function );
		table.getCompilationUnit().addDeclaration( f4 );
		
		look = f4.Lookup("i");
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration nsM = table.new Declaration( "M" );
		nsM.setType( ParserSymbolTable.TypeInfo.t_namespace );
		
		compUnit.addDeclaration( nsM );
		
		ParserSymbolTable.Declaration nsM_i = table.new Declaration("i");
		nsM.addDeclaration( nsM_i );
				
		ParserSymbolTable.Declaration nsN = table.new Declaration( "N" );
		nsN.setType( ParserSymbolTable.TypeInfo.t_namespace );
		
		compUnit.addDeclaration( nsN );
		
		ParserSymbolTable.Declaration nsN_i = table.new Declaration("i");
		nsN.addDeclaration( nsN_i );
		nsN.addUsingDirective( nsM );
		
		ParserSymbolTable.Declaration f = table.new Declaration("f");
		compUnit.addDeclaration( f );
		
		f.addUsingDirective( nsN );
		
		ParserSymbolTable.Declaration look = null;
		try
		{
			look = f.Lookup( "i" );
			assertTrue( false );
		}
		catch ( ParserSymbolTableException e )
		{
			//ambiguous, both M::i and N::i are visible.
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		
		look = f.LookupNestedNameSpecifier("N");
		look = look.QualifiedLookup("i"); //ok
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration nsA = table.new Declaration("A");
		nsA.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addDeclaration( nsA );
		
		ParserSymbolTable.Declaration a = table.new Declaration("a");
		nsA.addDeclaration( a );
				
		ParserSymbolTable.Declaration nsB = table.new Declaration("B");
		nsB.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addDeclaration( nsB );
		nsB.addUsingDirective( nsA );
		
		ParserSymbolTable.Declaration nsC = table.new Declaration("C");
		nsC.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addDeclaration( nsC );
		nsC.addUsingDirective( nsA );
		
		ParserSymbolTable.Declaration nsBC = table.new Declaration("BC");
		nsBC.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addDeclaration( nsBC );
		nsBC.addUsingDirective( nsB );
		nsBC.addUsingDirective( nsC );		
		
		ParserSymbolTable.Declaration f = table.new Declaration("f");
		f.setType(ParserSymbolTable.TypeInfo.t_function);
		compUnit.addDeclaration( f );
		
		ParserSymbolTable.Declaration look = f.LookupNestedNameSpecifier("BC");
		assertEquals( look, nsBC );
		look = look.QualifiedLookup("a");
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration nsB = table.new Declaration( "B" );
		nsB.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addDeclaration( nsB );
		
		ParserSymbolTable.Declaration b = table.new Declaration("b");
		nsB.addDeclaration( b );
		
		ParserSymbolTable.Declaration nsA = table.new Declaration( "A" );
		nsA.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addDeclaration( nsA );
		
		nsA.addUsingDirective( nsB );
		
		ParserSymbolTable.Declaration a = table.new Declaration("a");
		nsA.addDeclaration( a );
		
		nsB.addUsingDirective( nsA );
		
		ParserSymbolTable.Declaration f = table.new Declaration("f");
		compUnit.addDeclaration(f);
		
		ParserSymbolTable.Declaration lookA = f.LookupNestedNameSpecifier("A");
		ParserSymbolTable.Declaration look = lookA.QualifiedLookup("a");
		assertEquals( look, a );
		
		look = lookA.QualifiedLookup("b");
		assertEquals( look, b );
		
		ParserSymbolTable.Declaration lookB = f.LookupNestedNameSpecifier("B");
		look = lookB.QualifiedLookup("a");
		assertEquals( look, a );
		
		look = lookB.QualifiedLookup("b");
		assertEquals( look, b );
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration nsA = table.new Declaration( "A" );
		nsA.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addDeclaration( nsA );
			
		ParserSymbolTable.Declaration nsB = table.new Declaration( "B" );
		nsB.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addDeclaration( nsB );
		nsB.addUsingDirective( nsA );
		
		nsA.addUsingDirective( nsB );
		
		ParserSymbolTable.Declaration f = table.new Declaration("f");
		compUnit.addDeclaration(f);
		f.addUsingDirective(nsA);
		f.addUsingDirective(nsB);
		
		ParserSymbolTable.Declaration look = f.Lookup("i");
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration nsA = table.new Declaration("A");
		nsA.setType( ParserSymbolTable.TypeInfo.t_namespace );
		
		compUnit.addDeclaration( nsA );
		
		ParserSymbolTable.Declaration structX = table.new Declaration("x");
		structX.setType( ParserSymbolTable.TypeInfo.t_struct );
		nsA.addDeclaration( structX );
		
		ParserSymbolTable.Declaration intX = table.new Declaration("x");
		intX.setType( ParserSymbolTable.TypeInfo.t_int );
		nsA.addDeclaration( intX );
		
		ParserSymbolTable.Declaration intY = table.new Declaration("y");
		intY.setType( ParserSymbolTable.TypeInfo.t_int );
		nsA.addDeclaration( intY );

		ParserSymbolTable.Declaration nsB = table.new Declaration("B");
		nsB.setType( ParserSymbolTable.TypeInfo.t_namespace );
		
		compUnit.addDeclaration( nsB );
		ParserSymbolTable.Declaration structY = table.new Declaration("y");
		structY.setType( ParserSymbolTable.TypeInfo.t_struct );
		nsB.addDeclaration( structY );
		
		ParserSymbolTable.Declaration nsC = table.new Declaration("C");
		nsC.setType( ParserSymbolTable.TypeInfo.t_namespace);
		compUnit.addDeclaration( nsC );
		
		ParserSymbolTable.Declaration look = nsC.Lookup("A");
		assertEquals( look, nsA );
		nsC.addUsingDirective( look );
		
		look = nsC.Lookup("B");
		assertEquals( look, nsB );
		nsC.addUsingDirective( look );
		
		//lookup C::x
		look = nsC.LookupNestedNameSpecifier("C");
		assertEquals( look, nsC );
		look = look.QualifiedLookup( "x" );
		assertEquals( look, intX );
		
		//lookup C::y
		look = nsC.LookupNestedNameSpecifier("C");
		assertEquals( look, nsC );

		try{
			look = look.QualifiedLookup( "y" );
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
	
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration nsA = table.new Declaration( "A" );
		nsA.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addDeclaration( nsA );
	
		ParserSymbolTable.Declaration nsB = table.new Declaration( "B" );
		nsB.setType( ParserSymbolTable.TypeInfo.t_namespace );
		nsA.addDeclaration( nsB );
	
		ParserSymbolTable.Declaration f1 = table.new Declaration("f1");
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		nsB.addDeclaration( f1 );
	
		nsA.addUsingDirective( nsB );
	
		ParserSymbolTable.Declaration lookA = compUnit.LookupNestedNameSpecifier( "A" );
		assertEquals( nsA, lookA );
	
		ParserSymbolTable.Declaration look = lookA.LookupMemberForDefinition( "f1" );
		assertEquals( look, null );
	
		//but notice if you wanted to do A::f1 as a function call, it is ok
		look = lookA.QualifiedLookup( "f1" );
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration B = table.new Declaration("B");
		B.setType( ParserSymbolTable.TypeInfo.t_struct );
		compUnit.addDeclaration( B );
		
		ParserSymbolTable.Declaration f = table.new Declaration("f");
		f.setType( ParserSymbolTable.TypeInfo.t_function );
		B.addDeclaration( f );
	
		ParserSymbolTable.Declaration E = table.new Declaration( "E" );
		E.setType( ParserSymbolTable.TypeInfo.t_enumeration );
		B.addDeclaration( E );
		
		ParserSymbolTable.Declaration e = table.new Declaration( "e" );
		e.setType( ParserSymbolTable.TypeInfo.t_enumerator );
		E.addDeclaration( e );
		
		/**
		 * TBD: Anonymous unions are not yet implemented
		 */
		
		ParserSymbolTable.Declaration C = table.new Declaration( "C" );
		C.setType( ParserSymbolTable.TypeInfo.t_class );
		compUnit.addDeclaration( C );
		
		ParserSymbolTable.Declaration g = table.new Declaration( "g" );
		g.setType( ParserSymbolTable.TypeInfo.t_function );
		C.addDeclaration( g );
		
		ParserSymbolTable.Declaration D = table.new Declaration( "D" );
		D.setType( ParserSymbolTable.TypeInfo.t_struct );
		ParserSymbolTable.Declaration look = compUnit.Lookup( "B" );
		assertEquals( look, B );
		D.addParent( look );
		
		compUnit.addDeclaration( D );
		
		ParserSymbolTable.Declaration lookB = D.LookupNestedNameSpecifier("B");
		assertEquals( lookB, B );

		D.addUsingDeclaration( "f", lookB );
		D.addUsingDeclaration( "e", lookB );
		  
		//TBD anonymous union
		//D.addUsingDeclaration( "x", lookB );
		
		look = D.LookupNestedNameSpecifier("C");
		assertEquals( look, C );
		
		try{
			D.addUsingDeclaration( "g", look );
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration A = table.new Declaration( "A" );
		A.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addDeclaration( A );
		
		ParserSymbolTable.Declaration f1 = table.new Declaration( "f" );
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		f1.setReturnType( ParserSymbolTable.TypeInfo.t_void );
		f1.addParameter( ParserSymbolTable.TypeInfo.t_int, 0, "", false );
		A.addDeclaration( f1 );
		
		ParserSymbolTable.Declaration look = compUnit.LookupNestedNameSpecifier("A");
		assertEquals( look, A );
		
		ParserSymbolTable.Declaration usingF = compUnit.addUsingDeclaration( "f", look );
		
		look = compUnit.Lookup("A");
		assertEquals( look, A );
		
		ParserSymbolTable.Declaration f2 = table.new Declaration("f");
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		f2.setReturnType( ParserSymbolTable.TypeInfo.t_void );
		f2.addParameter( ParserSymbolTable.TypeInfo.t_char, 0, "", false );
		
		look.addDeclaration( f2 );
		
		ParserSymbolTable.Declaration foo = table.new Declaration("foo");
		foo.setType( ParserSymbolTable.TypeInfo.t_function );
		compUnit.addDeclaration( foo );

		LinkedList paramList = new LinkedList();
		ParserSymbolTable.TypeInfo param = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_char, null );
		paramList.add( param );
		
		look = foo.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, usingF );
		assertTrue( look.hasSameParameters( f1 ) );
		
		ParserSymbolTable.Declaration bar = table.new Declaration( "bar" );
		bar.setType( ParserSymbolTable.TypeInfo.t_function );
		bar.addParameter( ParserSymbolTable.TypeInfo.t_char, 0, null, false );
		compUnit.addDeclaration( bar );
		
		look = bar.LookupNestedNameSpecifier( "A" );
		assertEquals( look, A );
		bar.addUsingDeclaration( "f", A );
		
		look = bar.UnqualifiedFunctionLookup( "f", paramList );
		assertTrue( look != null );
		assertTrue( look.hasSameParameters( f2 ) );
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
		
		ParserSymbolTable.Declaration cls = table.new Declaration("class");
		cls.setType( ParserSymbolTable.TypeInfo.t_class );
		
		ParserSymbolTable.Declaration fn = table.new Declaration("function");
		fn.setType( ParserSymbolTable.TypeInfo.t_function );
		fn.setCVQualifier( ParserSymbolTable.TypeInfo.cvConst );
		
		table.getCompilationUnit().addDeclaration( cls );
		cls.addDeclaration( fn );
		
		ParserSymbolTable.Declaration look = fn.Lookup("this");
		assertTrue( look != null );
		
		assertEquals( look.getType(), ParserSymbolTable.TypeInfo.t_type );
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
		
		ParserSymbolTable.Declaration cls = table.new Declaration("class");
		cls.setType( ParserSymbolTable.TypeInfo.t_class );
		
		ParserSymbolTable.Declaration enumeration = table.new Declaration("enumeration");
		enumeration.setType( ParserSymbolTable.TypeInfo.t_enumeration );
		
		table.getCompilationUnit().addDeclaration( cls );
		cls.addDeclaration( enumeration );
		
		ParserSymbolTable.Declaration enumerator = table.new Declaration( "enumerator" );
		enumerator.setType( ParserSymbolTable.TypeInfo.t_enumerator );
		enumeration.addDeclaration( enumerator );
		
		ParserSymbolTable.Declaration look = cls.Lookup( "enumerator" );
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration NS = table.new Declaration("NS");
		NS.setType( ParserSymbolTable.TypeInfo.t_namespace );
		
		compUnit.addDeclaration( NS );
		
		ParserSymbolTable.Declaration T = table.new Declaration("T");
		T.setType( ParserSymbolTable.TypeInfo.t_class );
		
		NS.addDeclaration( T );
		
		ParserSymbolTable.Declaration f = table.new Declaration("f");
		f.setType( ParserSymbolTable.TypeInfo.t_function );
		f.setReturnType( ParserSymbolTable.TypeInfo.t_void );
		
		ParserSymbolTable.Declaration look = NS.Lookup( "T" );
		assertEquals( look, T );				
		f.addParameter( look, 0, "", false );
		
		NS.addDeclaration( f );	
				
		look = compUnit.LookupNestedNameSpecifier( "NS" );
		assertEquals( look, NS );
		look = look.QualifiedLookup( "T" );
		assertEquals( look, T );
		
		ParserSymbolTable.Declaration param = table.new Declaration("parm");
		param.setType( ParserSymbolTable.TypeInfo.t_type );
		param.setTypeDeclaration( look );
		compUnit.addDeclaration( param );
		
		ParserSymbolTable.Declaration main = table.new Declaration("main");
		main.setType( ParserSymbolTable.TypeInfo.t_function );
		main.setReturnType( ParserSymbolTable.TypeInfo.t_int );
		compUnit.addDeclaration( main );

		LinkedList paramList = new LinkedList();
		look = main.Lookup( "parm" );
		assertEquals( look, param );
		ParserSymbolTable.TypeInfo p = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, look, 0, null, false );
		paramList.add( p );
		
		look = main.UnqualifiedFunctionLookup( "f", paramList );
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration NS1 = table.new Declaration( "NS1" );
		NS1.setType( ParserSymbolTable.TypeInfo.t_namespace );
		 
		compUnit.addDeclaration( NS1 );
		
		ParserSymbolTable.Declaration f1 = table.new Declaration( "f" );
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		f1.setReturnType( ParserSymbolTable.TypeInfo.t_void );
		f1.addParameter( ParserSymbolTable.TypeInfo.t_void, 0, "*", false );
		NS1.addDeclaration( f1 );
		
		ParserSymbolTable.Declaration NS2 = table.new Declaration( "NS2" );
		NS2.setType( ParserSymbolTable.TypeInfo.t_namespace );
		
		compUnit.addDeclaration( NS2 );
		
		ParserSymbolTable.Declaration look = NS2.Lookup( "NS1" );
		assertEquals( look, NS1 );
		NS2.addUsingDirective( look );
		
		ParserSymbolTable.Declaration B = table.new Declaration( "B" );
		B.setType( ParserSymbolTable.TypeInfo.t_class );
		NS2.addDeclaration( B );
		
		ParserSymbolTable.Declaration f2 = table.new Declaration( "f" );
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		f2.setReturnType( ParserSymbolTable.TypeInfo.t_void );
		f2.addParameter( ParserSymbolTable.TypeInfo.t_void, 0, "*", false );
		NS2.addDeclaration( f2 );
		
		ParserSymbolTable.Declaration A = table.new Declaration( "A" );
		A.setType( ParserSymbolTable.TypeInfo.t_class );
		look = compUnit.LookupNestedNameSpecifier( "NS2" );
		assertEquals( look, NS2 );
		
		look = NS2.QualifiedLookup( "B" );
		assertEquals( look, B );
		A.addParent( look );
		
		compUnit.addDeclaration( A );
		
		look = compUnit.Lookup( "A" );
		assertEquals( look, A );
		ParserSymbolTable.Declaration a = table.new Declaration( "a" );
		a.setType( ParserSymbolTable.TypeInfo.t_type );
		a.setTypeDeclaration( look );
		compUnit.addDeclaration( a );
		
		LinkedList paramList = new LinkedList();
		look = compUnit.Lookup( "a" );
		assertEquals( look, a );
		ParserSymbolTable.TypeInfo param = new ParserSymbolTable.TypeInfo( look.getType(), look, 0, "&", false );
		paramList.add( param );
		
		look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration C = table.new Declaration( "C" );
		C.setType( ParserSymbolTable.TypeInfo.t_class );
		compUnit.addDeclaration(C);
				
		ParserSymbolTable.Declaration f1 = table.new Declaration("foo");
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		f1.setReturnType( ParserSymbolTable.TypeInfo.t_void );
		f1.addParameter( ParserSymbolTable.TypeInfo.t_int, 0, "", false );
		C.addDeclaration( f1 );
		
		ParserSymbolTable.Declaration f2 = table.new Declaration("foo");
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		f2.setReturnType( ParserSymbolTable.TypeInfo.t_void );
		f2.addParameter( ParserSymbolTable.TypeInfo.t_int, 0, "", false );
		f2.addParameter( ParserSymbolTable.TypeInfo.t_char, 0, "", false );
		C.addDeclaration( f2 );
		
		ParserSymbolTable.Declaration f3 = table.new Declaration("foo");
		f3.setType( ParserSymbolTable.TypeInfo.t_function );
		f3.setReturnType( ParserSymbolTable.TypeInfo.t_void );
		f3.addParameter( ParserSymbolTable.TypeInfo.t_int, 0, "", false );
		f3.addParameter( ParserSymbolTable.TypeInfo.t_char, 0, "", false );
		f3.addParameter( C, 0, "*", false );
		C.addDeclaration( f3 );
		
		ParserSymbolTable.Declaration look = compUnit.Lookup("C");
		assertEquals( look, C );
		
		ParserSymbolTable.Declaration c = table.new Declaration("c");
		c.setType( ParserSymbolTable.TypeInfo.t_type );
		c.setTypeDeclaration( look );
		c.setPtrOperator( "*" );
		compUnit.addDeclaration( c );
		
		look = compUnit.Lookup( "c" );
		assertEquals( look, c );
		assertEquals( look.getTypeDeclaration(), C );
		
		LinkedList paramList = new LinkedList();
		ParserSymbolTable.TypeInfo p1 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_int, null, 0, "", false);
		ParserSymbolTable.TypeInfo p2 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_char, null, 0, "", false);
		ParserSymbolTable.TypeInfo p3 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, c, 0, "", false);
		
		paramList.add( p1 );
		look = C.MemberFunctionLookup( "foo", paramList );
		assertEquals( look, f1 );
		
		paramList.add( p2 );
		look = C.MemberFunctionLookup( "foo", paramList );
		assertEquals( look, f2 );
				
		paramList.add( p3 );
		look = C.MemberFunctionLookup( "foo", paramList );
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration f1 = table.new Declaration("f");
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		f1.addParameter( ParserSymbolTable.TypeInfo.t_int, 0, "", false );
		compUnit.addDeclaration( f1 );
		
		ParserSymbolTable.Declaration f2 = table.new Declaration("f");
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		f2.addParameter( ParserSymbolTable.TypeInfo.t_char, 0, "", true );
		compUnit.addDeclaration( f2 );
		
		LinkedList paramList = new LinkedList();
		ParserSymbolTable.TypeInfo p1 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_int, null, 0, "", false );
		paramList.add( p1 );
		
		ParserSymbolTable.Declaration look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		paramList.clear();
		ParserSymbolTable.TypeInfo p2 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_char, null, 0, "", false );
		paramList.add( p2 );
		look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f2 );
		
		paramList.clear();
		ParserSymbolTable.TypeInfo p3 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_bool, null, 0, "", false );
		paramList.add( p3 );
		look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		look = compUnit.UnqualifiedFunctionLookup( "f", null );
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration A = table.new Declaration( "A" );
		A.setType( ParserSymbolTable.TypeInfo.t_class );
		compUnit.addDeclaration( A );
		
		ParserSymbolTable.Declaration B = table.new Declaration( "B" );
		B.setType( ParserSymbolTable.TypeInfo.t_class );
		B.addParent( A );
		compUnit.addDeclaration( B );
		
		ParserSymbolTable.Declaration C = table.new Declaration( "C" );
		C.setType( ParserSymbolTable.TypeInfo.t_class );
		C.addParent( B );
		compUnit.addDeclaration( C );
		
		ParserSymbolTable.Declaration f1 = table.new Declaration( "f" );
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		f1.addParameter( A, 0, "*", false );
		compUnit.addDeclaration( f1 );
		
		ParserSymbolTable.Declaration f2 = table.new Declaration( "f" );
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		f2.addParameter( B, 0, "*", false );
		compUnit.addDeclaration( f2 );
		
		ParserSymbolTable.Declaration a = table.new Declaration( "a" );
		a.setType( ParserSymbolTable.TypeInfo.t_type );
		a.setTypeDeclaration( A );
		a.setPtrOperator( "*" );
		
		ParserSymbolTable.Declaration c = table.new Declaration( "c" );
		c.setType( ParserSymbolTable.TypeInfo.t_type );
		c.setTypeDeclaration( C );
		c.setPtrOperator( "*" );
		
		LinkedList paramList = new LinkedList();
		ParserSymbolTable.TypeInfo p1 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, a, 0, null, false );
		paramList.add( p1 );
		ParserSymbolTable.Declaration look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		paramList.clear();
		ParserSymbolTable.TypeInfo p2 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, c, 0, "", false );
		paramList.add( p2 );
		look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration A = table.new Declaration( "A" );
		A.setType( ParserSymbolTable.TypeInfo.t_class );
		compUnit.addDeclaration( A );
		
		ParserSymbolTable.Declaration B = table.new Declaration( "B" );
		B.setType( ParserSymbolTable.TypeInfo.t_type );
		B.setTypeDeclaration( A );
		B.setPtrOperator( "*" );
		compUnit.addDeclaration( B );
		
		ParserSymbolTable.Declaration f1 = table.new Declaration( "f" );
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		f1.addParameter( A, 0, "*", false );
		compUnit.addDeclaration( f1 );
		
		ParserSymbolTable.Declaration f2 = table.new Declaration( "f" );
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		f2.addParameter( A, 0, null, false );
		compUnit.addDeclaration( f2 );

		ParserSymbolTable.Declaration a = table.new Declaration( "a" );
		a.setType( ParserSymbolTable.TypeInfo.t_type );
		a.setTypeDeclaration( A );
		compUnit.addDeclaration( a );
				
		ParserSymbolTable.Declaration b = table.new Declaration( "b" );
		b.setType( ParserSymbolTable.TypeInfo.t_type );
		b.setTypeDeclaration( B );
		compUnit.addDeclaration( b );
		
		ParserSymbolTable.Declaration array = table.new Declaration( "array" );
		array.setType( ParserSymbolTable.TypeInfo.t_type );
		array.setTypeDeclaration( A );
		array.setPtrOperator( "[]" );
				
		LinkedList paramList = new LinkedList();
		ParserSymbolTable.TypeInfo p = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, a, 0, null, false );
		paramList.add( p );
		
		ParserSymbolTable.Declaration look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f2 );
		
		p.setPtrOperator( "&" );
		look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		p.setTypeDeclaration( b );
		p.setPtrOperator( null );
		look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		p.setPtrOperator( "*" );
		look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f2 );
		
		p.setTypeDeclaration( array );
		p.setPtrOperator( null );
		look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration A = table.new Declaration( "A" );
		A.setType( ParserSymbolTable.TypeInfo.t_class );
		compUnit.addDeclaration( A );
		
		ParserSymbolTable.Declaration B = table.new Declaration( "B" );
		B.setType( ParserSymbolTable.TypeInfo.t_class );
		compUnit.addDeclaration( B );
		
		//12.1-1 "Constructors do not have names"
		ParserSymbolTable.Declaration constructor = table.new Declaration("");
		constructor.setType( ParserSymbolTable.TypeInfo.t_function );
		constructor.addParameter( A, 0, null, false );
		B.addDeclaration( constructor );
		
		ParserSymbolTable.Declaration f = table.new Declaration( "f" );
		f.setType( ParserSymbolTable.TypeInfo.t_function );
		f.addParameter( B, 0, null, false );
		compUnit.addDeclaration( f );
		
		ParserSymbolTable.Declaration a = table.new Declaration( "a" );
		a.setType( ParserSymbolTable.TypeInfo.t_type );
		a.setTypeDeclaration( A );
		compUnit.addDeclaration( a );
		
		LinkedList paramList = new LinkedList();
		ParserSymbolTable.TypeInfo p = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, a, 0, null, false );
		paramList.add( p );
		
		ParserSymbolTable.Declaration look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration f1 = table.new Declaration( "f" );
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		f1.addParameter( ParserSymbolTable.TypeInfo.t_int, ParserSymbolTable.TypeInfo.cvConst, "*", false );
		f1.addParameter( ParserSymbolTable.TypeInfo.t_int | ParserSymbolTable.TypeInfo.isShort, 0, null, false );
		
		compUnit.addDeclaration( f1 );
		
		ParserSymbolTable.Declaration f2 = table.new Declaration( "f" );
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		f2.addParameter( ParserSymbolTable.TypeInfo.t_int, 0, "*", false );
		f2.addParameter( ParserSymbolTable.TypeInfo.t_int, 0, null, false );
		compUnit.addDeclaration( f2 );
		
		ParserSymbolTable.Declaration i = table.new Declaration( "i" );
		i.setType( ParserSymbolTable.TypeInfo.t_int );
		compUnit.addDeclaration( i );
		
		ParserSymbolTable.Declaration s = table.new Declaration( "s" );
		s.setType( ParserSymbolTable.TypeInfo.t_int );
		s.getTypeInfo().setBit( true, ParserSymbolTable.TypeInfo.isShort );
		compUnit.addDeclaration( s );
		
		ParserSymbolTable.Declaration main = table.new Declaration( "main" );
		main.setType( ParserSymbolTable.TypeInfo.t_function );
		compUnit.addDeclaration( main );
		
		LinkedList params = new LinkedList();
		ParserSymbolTable.TypeInfo p1 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, i, 0, "&", false );
		ParserSymbolTable.TypeInfo p2 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, s, 0, null, false );
		params.add( p1 );
		params.add( p2 );
		
		ParserSymbolTable.Declaration look = null;
		
		try{
			main = main.UnqualifiedFunctionLookup( "f", params );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		
		params.clear();
		ParserSymbolTable.TypeInfo p3 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_int | ParserSymbolTable.TypeInfo.isLong, null, 0, null, false );
		params.add( p1 );
		params.add( p3 );
		look = main.UnqualifiedFunctionLookup( "f", params );
		assertEquals( look, f2 );
		
		params.clear();
		ParserSymbolTable.TypeInfo p4 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_char, null, 0, null, false );
		params.add( p1 );
		params.add( p4 );
		look = main.UnqualifiedFunctionLookup( "f", params );
		assertEquals( look, f2 );
		
		params.clear();
		p1.setCVQualifier( ParserSymbolTable.TypeInfo.cvConst );
		params.add( p1 );
		params.add( p3 );
		look = main.UnqualifiedFunctionLookup( "f", params );
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
		
		ParserSymbolTable.Declaration compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration B = table.new Declaration( "B" );
		B.setType( ParserSymbolTable.TypeInfo.t_class );
		
		compUnit.addDeclaration( B );
		
		ParserSymbolTable.Declaration A = table.new Declaration( "A" );
		A.setType( ParserSymbolTable.TypeInfo.t_class );
		compUnit.addDeclaration( A );
		
		ParserSymbolTable.Declaration constructA = table.new Declaration( "" );
		constructA.setType( ParserSymbolTable.TypeInfo.t_function );
		constructA.addParameter( B, 0, "&", false );
		A.addDeclaration( constructA );
		
		ParserSymbolTable.Declaration operator = table.new Declaration( "operator A" );
		operator.setType( ParserSymbolTable.TypeInfo.t_function );
		B.addDeclaration( operator );
		
		ParserSymbolTable.Declaration f1 = table.new Declaration( "f" );
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		f1.addParameter( A, 0, null, false );
		compUnit.addDeclaration( f1 );
		
		ParserSymbolTable.Declaration b = table.new Declaration( "b" );
		b.setType( ParserSymbolTable.TypeInfo.t_type );
		b.setTypeDeclaration( B );
		
		LinkedList params = new LinkedList();
		ParserSymbolTable.TypeInfo p1 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, b, 0, null, false );
		params.add( p1 );
		
		ParserSymbolTable.Declaration look = null;
		
		try{
			look = compUnit.UnqualifiedFunctionLookup( "f", params );
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous ); 
		}
		
		ParserSymbolTable.Declaration C = table.new Declaration("C");
		C.setType( ParserSymbolTable.TypeInfo.t_class );
		compUnit.addDeclaration( C );
		
		ParserSymbolTable.Declaration constructC = table.new Declaration("");
		constructC.setType( ParserSymbolTable.TypeInfo.t_function );
		constructC.addParameter( B, 0, "&", false );
		C.addDeclaration( constructC );

		ParserSymbolTable.Declaration f2 = table.new Declaration( "f" );
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		f2.addParameter(  C, 0, null, false );
		compUnit.addDeclaration( f2 );
		
		try{
			look = compUnit.UnqualifiedFunctionLookup( "f", params );
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous ); 
		}
		
		ParserSymbolTable.Declaration f3 = table.new Declaration( "f" );
		f3.setType( ParserSymbolTable.TypeInfo.t_function );
		f3.addParameter(  B, 0, null, false );
		compUnit.addDeclaration( f3 );
		
		look = compUnit.UnqualifiedFunctionLookup( "f", params );
		assertEquals( look, f3 );
	}
}

