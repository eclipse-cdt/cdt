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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier.ClassNameType;
import org.eclipse.cdt.core.parser.ast.IASTNode.LookupKind;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTClassSpecifier;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTCompilationUnit;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTField;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension;
import org.eclipse.cdt.internal.core.parser.pst.ITypeInfo;
import org.eclipse.cdt.internal.core.parser.pst.IUsingDeclarationSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IUsingDirectiveSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableException;
import org.eclipse.cdt.internal.core.parser.pst.StandardSymbolExtension;
import org.eclipse.cdt.internal.core.parser.pst.TypeFilter;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfoProvider;
import org.eclipse.cdt.internal.core.parser.scanner2.ObjectMap;




/**
 * @author aniefer
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ParserSymbolTableTest extends TestCase {

	private static final char[] EMPTY_CHAR_ARRAY = "".toCharArray();
	public ParserSymbolTable table = null;
	public TypeInfoProvider provider = null;
	
	public ParserSymbolTableTest( String arg )
	{
		super( arg );
	}
	
	public ParserSymbolTable newTable(){
		return newTable( ParserLanguage.CPP, ParserMode.COMPLETE_PARSE );
	}
	
	public ParserSymbolTable newTable( ParserLanguage language, ParserMode mode ){
		table = new ParserSymbolTable( language, mode );
		provider = table.getTypeInfoProvider();
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
		
		ISymbol x = table.newSymbol( "x" ); //$NON-NLS-1$
		x.setTypeInfo( TypeInfoProvider.newTypeInfo() );
		IContainerSymbol compUnit = table.getCompilationUnit();
		compUnit.addSymbol( x );
	
		ObjectMap declarations = compUnit.getContainedSymbols();
		assertEquals( 1, declarations.size() );
		
		ISymbol contained = (ISymbol) declarations.getAt( 0 );
		
		assertEquals( declarations.size(), 1 );
		assertEquals( x, contained );
		assertEquals( contained.getName(), "x" ); //$NON-NLS-1$
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}

	/**
	 * testSimpleLookup
	 * Add a declaration to the table, then look it up.
	 * @throws Exception
	 */
	public void testSimpleLookup() throws Exception{
		newTable(); //new symbol table
		
		ISymbol x = table.newSymbol( "x", ITypeInfo.t_int ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( x );
		
		ISymbol look = table.getCompilationUnit().lookup( "x" ); //$NON-NLS-1$
		
		assertEquals( x, look );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	public void testLookupNonExistant() throws Exception{
		newTable();
		
		ISymbol look = table.getCompilationUnit().lookup("boo"); //$NON-NLS-1$
		assertEquals( look, null );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	public void testSimpleSetGetObject() throws Exception{
		newTable();
		
		IContainerSymbol x = table.newContainerSymbol( "x", ITypeInfo.t_namespace ); //$NON-NLS-1$
		
		ISymbolASTExtension extension = new StandardSymbolExtension(x,null);  
		
		x.setASTExtension( extension );
				
		table.getCompilationUnit().addSymbol( x );
		
		ISymbol look = table.getCompilationUnit().lookup( "x" ); //$NON-NLS-1$
		
		assertEquals( look.getASTExtension(), extension );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * testHide
	 * test that a declaration in a scope hides declarations in containing
	 * scopes
	 * @throws Exception
	 */
	public void testHide() throws Exception{
		newTable();
		
		ISymbol firstX = table.newSymbol("x"); //$NON-NLS-1$
		firstX.setTypeInfo( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		table.getCompilationUnit().addSymbol( firstX );
		
		IDerivableContainerSymbol firstClass = table.newDerivableContainerSymbol("class"); //$NON-NLS-1$
		firstClass.setType( ITypeInfo.t_class );
		table.getCompilationUnit().addSymbol( firstClass );

		ISymbol look = firstClass.lookup( "x" ); //$NON-NLS-1$
		assertEquals( look, firstX );
		
		ISymbol secondX = table.newSymbol("x"); //$NON-NLS-1$
		firstClass.addSymbol( secondX );
		
		look = firstClass.lookup( "x" ); //$NON-NLS-1$
		assertEquals( look, secondX );
		
		look = table.getCompilationUnit().lookup( "x" ); //$NON-NLS-1$
		assertEquals( look, firstX );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * testContainingScopeLookup
	 * test lookup of something declared in the containing scope
	 * @throws Exception
	 */
	public void testContainingScopeLookup() throws Exception{
		newTable();
		
		ISymbol x = table.newSymbol("x"); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( x );

		IDerivableContainerSymbol decl = table.newDerivableContainerSymbol("class"); //$NON-NLS-1$
		decl.setType( ITypeInfo.t_class );
		table.getCompilationUnit().addSymbol( decl );
		
		ISymbol look = decl.lookup( "x" ); //$NON-NLS-1$
		
		assertEquals( x, look );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * testParentLookup
	 * test lookup of a variable declaration in the parent
	 *  
	 * @throws Exception
	 */
	public void testParentLookup() throws Exception{
		newTable();
		
		IDerivableContainerSymbol parent = table.newDerivableContainerSymbol("parent"); //$NON-NLS-1$
		parent.setType( ITypeInfo.t_class );

		IDerivableContainerSymbol class1 = table.newDerivableContainerSymbol("class"); //$NON-NLS-1$
		class1.setType( ITypeInfo.t_class );
		class1.addParent( parent );
		
		ISymbol decl = table.newSymbol( "x", ITypeInfo.t_int ); //$NON-NLS-1$
		parent.addSymbol( decl );
		
		table.getCompilationUnit().addSymbol( parent );
		table.getCompilationUnit().addSymbol( class1 );
		
		ISymbol look = class1.lookup( "x" ); //$NON-NLS-1$
		assertEquals( look, decl );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
	
		IDerivableContainerSymbol parent2 = table.newDerivableContainerSymbol("parent2"); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( parent2 );
		
		IDerivableContainerSymbol class1 = (IDerivableContainerSymbol) table.getCompilationUnit().lookup( "class" ); //$NON-NLS-1$
		class1.addParent( parent2 );
		
		ISymbol decl = table.newSymbol( "x", ITypeInfo.t_int ); //$NON-NLS-1$
		parent2.addSymbol( decl );
				
		try{
			class1.lookup( "x" ); //$NON-NLS-1$
			assertTrue( false );
		}
		catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}	
	
	/**
	 * 
	 * @throws Exception
	 * test for circular inheritance 
	 */
	public void testCircularParentLookup() throws Exception{
		newTable();
		
		IDerivableContainerSymbol a = table.newDerivableContainerSymbol("a"); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( a );
		
		IDerivableContainerSymbol b = table.newDerivableContainerSymbol("b"); //$NON-NLS-1$
		b.addParent( a );
		table.getCompilationUnit().addSymbol( b );
			
		a.addParent( b );
		 
		try{
			a.lookup("foo"); //$NON-NLS-1$
			assertTrue( false );
		} catch ( ParserSymbolTableException e) {
			assertEquals( e.reason, ParserSymbolTableException.r_CircularInheritance );
		}
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );	
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
		
		IDerivableContainerSymbol decl = table.newDerivableContainerSymbol("class"); //$NON-NLS-1$
		IDerivableContainerSymbol c    = table.newDerivableContainerSymbol("C"); //$NON-NLS-1$
		
		IDerivableContainerSymbol a    = table.newDerivableContainerSymbol("A"); //$NON-NLS-1$
		a.addParent( c, true, ASTAccessVisibility.PUBLIC, 3, null );
		
		IDerivableContainerSymbol b    = table.newDerivableContainerSymbol("B"); //$NON-NLS-1$
		b.addParent( c, true, ASTAccessVisibility.PUBLIC, 6, null );
		
		decl.addParent( a );
		decl.addParent( b );
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		compUnit.addSymbol( c );
		
		ISymbol x = table.newSymbol( "x", ITypeInfo.t_int ); //$NON-NLS-1$
		c.addSymbol( x );
		
		compUnit.addSymbol( decl );
		compUnit.addSymbol( a );
		compUnit.addSymbol( b );
		
		ISymbol look = decl.lookup( "x" );  //$NON-NLS-1$
		
		assertEquals( look, x );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IDerivableContainerSymbol cls = (IDerivableContainerSymbol) compUnit.lookup("class"); //$NON-NLS-1$
		IDerivableContainerSymbol c   = (IDerivableContainerSymbol) compUnit.lookup("C"); //$NON-NLS-1$
		IDerivableContainerSymbol d   = table.newDerivableContainerSymbol("D"); //$NON-NLS-1$
		
		d.addParent( c );
		cls.addParent( d );
		
		compUnit.addSymbol( d );
		
		try{
			cls.lookup( "x" ); //$NON-NLS-1$
			assertTrue( false );
		}
		catch( ParserSymbolTableException e){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IDerivableContainerSymbol a = table.newDerivableContainerSymbol("a" ); //$NON-NLS-1$
		IDerivableContainerSymbol b = table.newDerivableContainerSymbol( "b" ); //$NON-NLS-1$
		IDerivableContainerSymbol c = table.newDerivableContainerSymbol( "c" ); //$NON-NLS-1$
		IDerivableContainerSymbol d = table.newDerivableContainerSymbol( "d" ); //$NON-NLS-1$
	
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		compUnit.addSymbol( a );
		compUnit.addSymbol( b );
		compUnit.addSymbol( c );
		compUnit.addSymbol( d );
		
		IContainerSymbol enum = table.newContainerSymbol( "enum", ITypeInfo.t_enumeration ); //$NON-NLS-1$
		
		ISymbol enumerator = table.newSymbol( "enumerator", ITypeInfo.t_enumerator ); //$NON-NLS-1$
		
		ISymbol stat = table.newSymbol( "static", ITypeInfo.t_int ); //$NON-NLS-1$
		stat.getTypeInfo().setBit( true, ITypeInfo.isStatic );
		
		ISymbol x = table.newSymbol( "x", ITypeInfo.t_int ); //$NON-NLS-1$
		
		d.addSymbol( enum );
		d.addSymbol( stat );
		d.addSymbol( x );
		
		enum.addSymbol( enumerator );
		
		a.addParent( b );
		a.addParent( c );
		b.addParent( d );
		c.addParent( d );
		
		try{
			a.lookup( "enumerator" ); //$NON-NLS-1$
			assertTrue( true );	
		}
		catch ( ParserSymbolTableException e){
			assertTrue( false );
		}
		
		try{
			a.lookup( "static" ); //$NON-NLS-1$
			assertTrue( true );	
		}
		catch ( ParserSymbolTableException e){
			assertTrue( false );
		}
		
		try{
			a.lookup( "x" ); //$NON-NLS-1$
			assertTrue( false );	
		}
		catch ( ParserSymbolTableException e){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * testElaboratedLookup
	 * @throws Exception
	 * test lookup of hidden names using elaborated type spec
	 */
	public void testElaboratedLookup() throws Exception{
		newTable();
		
		IDerivableContainerSymbol cls = table.newDerivableContainerSymbol( "class" ); //$NON-NLS-1$
		cls.setType( ITypeInfo.t_class );
		
		IDerivableContainerSymbol struct = table.newDerivableContainerSymbol("struct"); //$NON-NLS-1$
		struct.setType( ITypeInfo.t_struct );
		
		IContainerSymbol union = table.newContainerSymbol("union"); //$NON-NLS-1$
		union.setType( ITypeInfo.t_union );
		
		IDerivableContainerSymbol hideCls = table.newDerivableContainerSymbol( "class" ); //$NON-NLS-1$
		hideCls.setType( ITypeInfo.t_int );
		IDerivableContainerSymbol hideStruct = table.newDerivableContainerSymbol("struct"); //$NON-NLS-1$
		hideStruct.setType( ITypeInfo.t_int );
		IContainerSymbol hideUnion = table.newContainerSymbol("union"); //$NON-NLS-1$
		hideUnion.setType( ITypeInfo.t_int );
		
		IDerivableContainerSymbol a = table.newDerivableContainerSymbol("a"); //$NON-NLS-1$
		IDerivableContainerSymbol b = table.newDerivableContainerSymbol("b"); //$NON-NLS-1$
		
		a.addSymbol(hideCls);
		a.addSymbol(hideStruct);
		a.addSymbol(hideUnion);
		
		a.addParent( b );
		
		b.addSymbol(cls);
		b.addSymbol(struct);
		b.addSymbol(union);
		
		table.getCompilationUnit().addSymbol( a );
		table.getCompilationUnit().addSymbol( b );
		
		ISymbol look = a.elaboratedLookup( ITypeInfo.t_class, "class" ); //$NON-NLS-1$
		assertEquals( look, cls );
		look = a.elaboratedLookup( ITypeInfo.t_struct, "struct" ); //$NON-NLS-1$
		assertEquals( look, struct );
		look = a.elaboratedLookup( ITypeInfo.t_union, "union" ); //$NON-NLS-1$
		assertEquals( look, union );
		
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		//pre-condition
		IContainerSymbol A = table.newContainerSymbol("A"); //$NON-NLS-1$
		compUnit.addSymbol(A);

		ISymbol member = table.newSymbol("member"); //$NON-NLS-1$
		A.addSymbol(member);
				
		//at time of "A a;"
		ISymbol look = compUnit.lookup("A"); //$NON-NLS-1$
		assertEquals( look, A );
		ISymbol a = table.newSymbol("a"); //$NON-NLS-1$
		a.setTypeSymbol( look );
		compUnit.addSymbol( a );
		
		//later "a.member"
		look = compUnit.lookup("a"); //$NON-NLS-1$
		assertEquals( look, a );
		IContainerSymbol type = (IContainerSymbol) look.getTypeSymbol();
		assertEquals( type, A );
		
		look = type.lookup("member"); //$NON-NLS-1$
		assertEquals( look, member );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IDerivableContainerSymbol struct = table.newDerivableContainerSymbol("stat"); //$NON-NLS-1$
		struct.setType( ITypeInfo.t_struct );
		compUnit.addSymbol( struct );
		
		IParameterizedSymbol function = table.newParameterizedSymbol( "stat" ); //$NON-NLS-1$
		function.setType( ITypeInfo.t_function );
		compUnit.addSymbol( function );
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f"); //$NON-NLS-1$
		f.setType( ITypeInfo.t_function );
		compUnit.addSymbol( f );
				
		ISymbol look = f.elaboratedLookup( ITypeInfo.t_struct, "stat" ); //$NON-NLS-1$
		assertEquals( look, struct );
		
		look = f.lookup( "stat" ); //$NON-NLS-1$
		assertEquals( look, function );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol nsA = table.newContainerSymbol("A"); //$NON-NLS-1$
		nsA.setType( ITypeInfo.t_namespace );
		table.getCompilationUnit().addSymbol( nsA );
		
		ISymbol nsA_i = table.newSymbol("i"); //$NON-NLS-1$
		nsA.addSymbol( nsA_i );
		
		IContainerSymbol nsB = table.newContainerSymbol("B"); //$NON-NLS-1$
		nsB.setType( ITypeInfo.t_namespace );
		nsA.addSymbol( nsB );
		
		IContainerSymbol nsC = table.newContainerSymbol("C"); //$NON-NLS-1$
		nsC.setType( ITypeInfo.t_namespace );
		nsB.addSymbol( nsC );
		
		ISymbol nsC_i = table.newSymbol("i"); //$NON-NLS-1$
		nsC.addSymbol( nsC_i );
		
		ISymbol look = nsB.lookup("C"); //$NON-NLS-1$
		assertEquals( look, nsC );
		nsB.addUsingDirective( nsC );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol("f"); //$NON-NLS-1$
		f1.setType( ITypeInfo.t_function );
		
		nsB.addSymbol( f1 );
		
		look = f1.lookup( "i" ); //$NON-NLS-1$
		assertEquals( look, nsC_i ); //C::i visible and hides A::i
		
		IContainerSymbol nsD = table.newContainerSymbol("D"); //$NON-NLS-1$
		nsD.setType( ITypeInfo.t_namespace );
		nsA.addSymbol( nsD );
		
		look = nsD.lookup("B"); //$NON-NLS-1$
		assertEquals( look, nsB );
		nsD.addUsingDirective( nsB );
		
		look = nsD.lookup("C"); //$NON-NLS-1$
		assertEquals( look, nsC );
		nsD.addUsingDirective( nsC );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f2" ); //$NON-NLS-1$
		f2.setType( ITypeInfo.t_function );
		nsD.addSymbol( f2 );
		
		try
		{
			look = f2.lookup( "i" ); //$NON-NLS-1$
			assertTrue( false );
		}
		catch ( ParserSymbolTableException e )
		{
			//ambiguous B::C::i and A::i
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		
		IParameterizedSymbol f3 = table.newParameterizedSymbol("f3"); //$NON-NLS-1$
		f3.setType( ITypeInfo.t_function );
		nsA.addSymbol( f3 );
		
		look = f3.lookup("i"); //$NON-NLS-1$
		assertEquals( look, nsA_i );  //uses A::i
		
		IParameterizedSymbol f4 = table.newParameterizedSymbol("f4"); //$NON-NLS-1$
		f4.setType( ITypeInfo.t_function );
		table.getCompilationUnit().addSymbol( f4 );
		
		look = f4.lookup("i"); //$NON-NLS-1$
		assertEquals( look, null );//neither i is visible here.
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IContainerSymbol nsM = table.newContainerSymbol( "M" ); //$NON-NLS-1$
		nsM.setType( ITypeInfo.t_namespace );
		
		compUnit.addSymbol( nsM );
		
		ISymbol nsM_i = table.newSymbol("i"); //$NON-NLS-1$
		nsM.addSymbol( nsM_i );
				
		IContainerSymbol nsN = table.newContainerSymbol( "N" ); //$NON-NLS-1$
		nsN.setType( ITypeInfo.t_namespace );
		
		compUnit.addSymbol( nsN );
		
		ISymbol nsN_i = table.newSymbol("i"); //$NON-NLS-1$
		nsN.addSymbol( nsN_i );
		nsN.addUsingDirective( nsM );
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f"); //$NON-NLS-1$
		compUnit.addSymbol( f );
		
		f.addUsingDirective( nsN );
		
		ISymbol look = null;
		try
		{
			look = f.lookup( "i" ); //$NON-NLS-1$
			assertTrue( false );
		}
		catch ( ParserSymbolTableException e )
		{
			//ambiguous, both M::i and N::i are visible.
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		
		look = f.lookupNestedNameSpecifier("N"); //$NON-NLS-1$
		assertEquals( look, nsN );
		
		look = ((IContainerSymbol) look).qualifiedLookup("i"); //ok //$NON-NLS-1$
		assertEquals( look, nsN_i );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IContainerSymbol nsA = table.newContainerSymbol("A"); //$NON-NLS-1$
		nsA.setType( ITypeInfo.t_namespace );
		compUnit.addSymbol( nsA );
		
		ISymbol a = table.newSymbol("a"); //$NON-NLS-1$
		nsA.addSymbol( a );
				
		IContainerSymbol nsB = table.newContainerSymbol("B"); //$NON-NLS-1$
		nsB.setType( ITypeInfo.t_namespace );
		compUnit.addSymbol( nsB );
		nsB.addUsingDirective( nsA );
		
		IContainerSymbol nsC = table.newContainerSymbol("C"); //$NON-NLS-1$
		nsC.setType( ITypeInfo.t_namespace );
		compUnit.addSymbol( nsC );
		nsC.addUsingDirective( nsA );
		
		IContainerSymbol nsBC = table.newContainerSymbol("BC"); //$NON-NLS-1$
		nsBC.setType( ITypeInfo.t_namespace );
		compUnit.addSymbol( nsBC );
		nsBC.addUsingDirective( nsB );
		nsBC.addUsingDirective( nsC );		
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f"); //$NON-NLS-1$
		f.setType(ITypeInfo.t_function);
		compUnit.addSymbol( f );
		
		ISymbol look = f.lookupNestedNameSpecifier("BC"); //$NON-NLS-1$
		assertEquals( look, nsBC );
		look = ((IContainerSymbol)look).qualifiedLookup("a"); //$NON-NLS-1$
		assertEquals( look, a );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IContainerSymbol nsB = table.newContainerSymbol( "B" ); //$NON-NLS-1$
		nsB.setType( ITypeInfo.t_namespace );
		compUnit.addSymbol( nsB );
		
		ISymbol b = table.newSymbol("b"); //$NON-NLS-1$
		nsB.addSymbol( b );
		
		IContainerSymbol nsA = table.newContainerSymbol( "A" ); //$NON-NLS-1$
		nsA.setType( ITypeInfo.t_namespace );
		compUnit.addSymbol( nsA );
		
		nsA.addUsingDirective( nsB );
		
		ISymbol a = table.newSymbol("a"); //$NON-NLS-1$
		nsA.addSymbol( a );
		
		nsB.addUsingDirective( nsA );
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f"); //$NON-NLS-1$
		compUnit.addSymbol(f);
		
		IContainerSymbol lookA = f.lookupNestedNameSpecifier("A"); //$NON-NLS-1$
		assertEquals( lookA, nsA );
		
		ISymbol look = lookA.qualifiedLookup("a"); //$NON-NLS-1$
		assertEquals( look, a );
		
		look = lookA.qualifiedLookup("b"); //$NON-NLS-1$
		assertEquals( look, b );
		
		IContainerSymbol lookB = f.lookupNestedNameSpecifier("B"); //$NON-NLS-1$
		look = lookB.qualifiedLookup("a"); //$NON-NLS-1$
		assertEquals( look, a );
		
		look = lookB.qualifiedLookup("b"); //$NON-NLS-1$
		assertEquals( look, b );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IContainerSymbol nsA = table.newContainerSymbol( "A" ); //$NON-NLS-1$
		nsA.setType( ITypeInfo.t_namespace );
		compUnit.addSymbol( nsA );
			
		IContainerSymbol nsB = table.newContainerSymbol( "B" ); //$NON-NLS-1$
		nsB.setType( ITypeInfo.t_namespace );
		compUnit.addSymbol( nsB );
		nsB.addUsingDirective( nsA );
		
		nsA.addUsingDirective( nsB );
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f"); //$NON-NLS-1$
		compUnit.addSymbol(f);
		f.addUsingDirective(nsA);
		f.addUsingDirective(nsB);
		
		ISymbol look = f.lookup("i"); //$NON-NLS-1$
		assertEquals( look, null );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IContainerSymbol nsA = table.newContainerSymbol("A"); //$NON-NLS-1$
		nsA.setType( ITypeInfo.t_namespace );
		
		compUnit.addSymbol( nsA );
		
		IContainerSymbol structX = table.newContainerSymbol("x"); //$NON-NLS-1$
		structX.setType( ITypeInfo.t_struct );
		nsA.addSymbol( structX );
		
		ISymbol intX = table.newSymbol("x"); //$NON-NLS-1$
		intX.setType( ITypeInfo.t_int );
		nsA.addSymbol( intX );
		
		ISymbol intY = table.newSymbol("y"); //$NON-NLS-1$
		intY.setType( ITypeInfo.t_int );
		nsA.addSymbol( intY );

		IContainerSymbol nsB = table.newContainerSymbol("B"); //$NON-NLS-1$
		nsB.setType( ITypeInfo.t_namespace );
		
		compUnit.addSymbol( nsB );
		IContainerSymbol structY = table.newContainerSymbol("y"); //$NON-NLS-1$
		structY.setType( ITypeInfo.t_struct );
		nsB.addSymbol( structY );
		
		IContainerSymbol nsC = table.newContainerSymbol("C"); //$NON-NLS-1$
		nsC.setType( ITypeInfo.t_namespace);
		compUnit.addSymbol( nsC );
		
		ISymbol look = nsC.lookup("A"); //$NON-NLS-1$
		assertEquals( look, nsA );
		nsC.addUsingDirective( nsA );
		
		look = nsC.lookup("B"); //$NON-NLS-1$
		assertEquals( look, nsB );
		nsC.addUsingDirective( nsB );
		
		//lookup C::x
		look = nsC.lookupNestedNameSpecifier("C"); //$NON-NLS-1$
		assertEquals( look, nsC );
		look = ((IContainerSymbol)look).qualifiedLookup( "x" ); //$NON-NLS-1$
		assertEquals( look, intX );
		
		//lookup C::y
		look = nsC.lookupNestedNameSpecifier("C"); //$NON-NLS-1$
		assertEquals( look, nsC );

		try{
			look = ((IContainerSymbol)look).qualifiedLookup( "y" ); //$NON-NLS-1$
			assertTrue(false);
		} catch ( ParserSymbolTableException e ) {
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
	
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IContainerSymbol nsA = table.newContainerSymbol( "A" ); //$NON-NLS-1$
		nsA.setType( ITypeInfo.t_namespace );
		compUnit.addSymbol( nsA );
	
		IContainerSymbol nsB = table.newContainerSymbol( "B" ); //$NON-NLS-1$
		nsB.setType( ITypeInfo.t_namespace );
		nsA.addSymbol( nsB );
	
		IParameterizedSymbol f1 = table.newParameterizedSymbol("f1"); //$NON-NLS-1$
		f1.setType( ITypeInfo.t_function );
		nsB.addSymbol( f1 );
	
		nsA.addUsingDirective( nsB );
	
		IContainerSymbol lookA = compUnit.lookupNestedNameSpecifier( "A" ); //$NON-NLS-1$
		assertEquals( nsA, lookA );
	
		ISymbol look = lookA.lookupMemberForDefinition( "f1" ); //$NON-NLS-1$
		assertEquals( look, null );
	
		//but notice if you wanted to do A::f1 as a function call, it is ok
		look = lookA.qualifiedLookup( "f1" ); //$NON-NLS-1$
		assertEquals( look, f1 );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol("B"); //$NON-NLS-1$
		B.setType( ITypeInfo.t_struct );
		compUnit.addSymbol( B );
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f"); //$NON-NLS-1$
		f.setType( ITypeInfo.t_function );
		B.addSymbol( f );
	
		IContainerSymbol E = table.newContainerSymbol( "E" ); //$NON-NLS-1$
		E.setType( ITypeInfo.t_enumeration );
		B.addSymbol( E );
		
		ISymbol e = table.newSymbol( "e" ); //$NON-NLS-1$
		e.setType( ITypeInfo.t_enumerator );
		E.addSymbol( e );
		
		/**
		 * TBD: Anonymous unions are not yet implemented
		 */
		
		IDerivableContainerSymbol C = table.newDerivableContainerSymbol( "C" ); //$NON-NLS-1$
		C.setType( ITypeInfo.t_class );
		compUnit.addSymbol( C );
		
		IParameterizedSymbol g = table.newParameterizedSymbol( "g" ); //$NON-NLS-1$
		g.setType( ITypeInfo.t_function );
		C.addSymbol( g );
		
		IDerivableContainerSymbol D = table.newDerivableContainerSymbol( "D" ); //$NON-NLS-1$
		D.setType( ITypeInfo.t_struct );
		ISymbol look = compUnit.lookup( "B" ); //$NON-NLS-1$
		assertEquals( look, B );
		D.addParent( B );
		
		compUnit.addSymbol( D );
		
		IContainerSymbol lookB = D.lookupNestedNameSpecifier("B"); //$NON-NLS-1$
		assertEquals( lookB, B );

		D.addUsingDeclaration( "f", lookB ); //$NON-NLS-1$
		D.addUsingDeclaration( "e", lookB ); //$NON-NLS-1$
		  
		//TBD anonymous union
		//D.addUsingDeclaration( "x", lookB );
		
		look = D.lookupNestedNameSpecifier("C"); //$NON-NLS-1$
		assertEquals( look, C );
		
		try{
			D.addUsingDeclaration( "g", C ); //$NON-NLS-1$
			assertTrue( false );
		}
		catch ( ParserSymbolTableException exception ){
			assertTrue( true );
		}
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IContainerSymbol A = table.newContainerSymbol( "A", ITypeInfo.t_namespace ); //$NON-NLS-1$
		compUnit.addSymbol( A );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f1.setReturnType( table.newSymbol( "", ITypeInfo.t_void ) ); //$NON-NLS-1$
		f1.addParameter( ITypeInfo.t_int, 0, null, false );
		A.addSymbol( f1 );
		
		ISymbol look = compUnit.lookupNestedNameSpecifier("A"); //$NON-NLS-1$
		assertEquals( look, A );
		
		IUsingDeclarationSymbol using = compUnit.addUsingDeclaration( "f", A ); //$NON-NLS-1$
		assertEquals( using.getReferencedSymbols().size(), 1 );
		
		assertEquals( using.getReferencedSymbols().get(0), f1 );
		
		IParameterizedSymbol usingF = (IParameterizedSymbol)using.getDeclaredSymbols().get(0); 
		
		look = compUnit.lookup("A"); //$NON-NLS-1$
		assertEquals( look, A );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol("f"); //$NON-NLS-1$
		f2.setType( ITypeInfo.t_function );
		f2.setReturnType( table.newSymbol( "", ITypeInfo.t_void ) ); //$NON-NLS-1$
		f2.addParameter( ITypeInfo.t_char, 0, null, false );
		
		A.addSymbol( f2 );
		
		IParameterizedSymbol foo = table.newParameterizedSymbol("foo"); //$NON-NLS-1$
		foo.setType( ITypeInfo.t_function );
		compUnit.addSymbol( foo );

		ArrayList paramList = new ArrayList();
		ITypeInfo param = TypeInfoProvider.newTypeInfo( ITypeInfo.t_char, 0, null );
		paramList.add( param );
		
		look = foo.unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, usingF );
		assertTrue( usingF.hasSameParameters( f1 ) );
		
		IParameterizedSymbol bar = table.newParameterizedSymbol( "bar" ); //$NON-NLS-1$
		bar.setType( ITypeInfo.t_function );
		bar.addParameter( ITypeInfo.t_char, 0, null, false );
		compUnit.addSymbol( bar );
		
		look = bar.lookupNestedNameSpecifier( "A" ); //$NON-NLS-1$
		assertEquals( look, A );
		
		using = bar.addUsingDeclaration( "f", A ); //$NON-NLS-1$
		
		List list = using.getReferencedSymbols();
		assertTrue( list.contains( f1 ) );
		assertTrue( list.contains( f2 ) );
		assertEquals( list.size(), 2 );

		int index = list.indexOf( f2 );
		list = using.getDeclaredSymbols();
		
		look = bar.unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertTrue( look != null );
		assertEquals( look, list.get( index ) );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IDerivableContainerSymbol cls = table.newDerivableContainerSymbol( "class", ITypeInfo.t_class ); //$NON-NLS-1$
		
		IParameterizedSymbol fn = table.newParameterizedSymbol("function", ITypeInfo.t_function ); //$NON-NLS-1$
		fn.setType( ITypeInfo.t_function );
		fn.getTypeInfo().setBit( true, ITypeInfo.isConst );
		
		table.getCompilationUnit().addSymbol( cls );
		cls.addSymbol( fn );
		
		ISymbol look = fn.lookup("this"); //$NON-NLS-1$
		assertTrue( look != null );
		
		assertEquals( look.getType(), ITypeInfo.t_type );
		assertEquals( look.getTypeSymbol(), cls );
		assertTrue( look.getTypeInfo().checkBit( ITypeInfo.isConst ) );
		assertEquals( ((ITypeInfo.PtrOp)look.getPtrOperators().iterator().next()).getType(), ITypeInfo.PtrOp.t_pointer );
		
		assertEquals( look.getContainingSymbol(), fn );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol cls = table.newContainerSymbol("class"); //$NON-NLS-1$
		cls.setType( ITypeInfo.t_class );
		
		IContainerSymbol enumeration = table.newContainerSymbol("enumeration"); //$NON-NLS-1$
		enumeration.setType( ITypeInfo.t_enumeration );
		
		table.getCompilationUnit().addSymbol( cls );
		cls.addSymbol( enumeration );
		
		ISymbol enumerator = table.newSymbol( "enumerator" ); //$NON-NLS-1$
		enumerator.setType( ITypeInfo.t_enumerator );
		enumeration.addSymbol( enumerator );
		
		ISymbol look = cls.lookup( "enumerator" ); //$NON-NLS-1$
		assertEquals( look, enumerator );
		assertEquals( look.getContainingSymbol(), cls );
		assertEquals( look.getTypeSymbol(), enumeration );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IContainerSymbol NS = table.newContainerSymbol("NS"); //$NON-NLS-1$
		NS.setType( ITypeInfo.t_namespace );
		
		compUnit.addSymbol( NS );
		
		IDerivableContainerSymbol T = table.newDerivableContainerSymbol("T"); //$NON-NLS-1$
		T.setType( ITypeInfo.t_class );
		
		NS.addSymbol( T );
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f"); //$NON-NLS-1$
		f.setType( ITypeInfo.t_function );
		f.setReturnType( table.newSymbol( "", ITypeInfo.t_void ) ); //$NON-NLS-1$
		
		ISymbol look = NS.lookup( "T" ); //$NON-NLS-1$
		assertEquals( look, T );				
		f.addParameter( look, 0, null, false );
		
		NS.addSymbol( f );	
				
		look = compUnit.lookupNestedNameSpecifier( "NS" ); //$NON-NLS-1$
		assertEquals( look, NS );
		look = NS.qualifiedLookup( "T" ); //$NON-NLS-1$
		assertEquals( look, T );
		
		ISymbol param = table.newSymbol("parm"); //$NON-NLS-1$
		param.setType( ITypeInfo.t_type );
		param.setTypeSymbol( look );
		compUnit.addSymbol( param );
		
		IParameterizedSymbol main = table.newParameterizedSymbol("main"); //$NON-NLS-1$
		main.setType( ITypeInfo.t_function );
		main.setReturnType( table.newSymbol( "", ITypeInfo.t_int ) ); //$NON-NLS-1$
		compUnit.addSymbol( main );

		ArrayList paramList = new ArrayList();
		look = main.lookup( "parm" ); //$NON-NLS-1$
		assertEquals( look, param );
		ITypeInfo p = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, look );
		paramList.add( p );
		
		look = main.unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, f );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IContainerSymbol NS1 = table.newContainerSymbol( "NS1" ); //$NON-NLS-1$
		NS1.setType( ITypeInfo.t_namespace );
		 
		compUnit.addSymbol( NS1 );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f1.setReturnType( table.newSymbol( "", ITypeInfo.t_void ) ); //$NON-NLS-1$
		f1.addParameter( ITypeInfo.t_void, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		NS1.addSymbol( f1 );
		
		IContainerSymbol NS2 = table.newContainerSymbol( "NS2" ); //$NON-NLS-1$
		NS2.setType( ITypeInfo.t_namespace );
		
		compUnit.addSymbol( NS2 );
		
		ISymbol look = NS2.lookup( "NS1" ); //$NON-NLS-1$
		assertEquals( look, NS1 );
		NS2.addUsingDirective( NS1 );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B" ); //$NON-NLS-1$
		B.setType( ITypeInfo.t_class );
		NS2.addSymbol( B );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f" ); //$NON-NLS-1$
		f2.setType( ITypeInfo.t_function );
		f2.setReturnType( table.newSymbol( "", ITypeInfo.t_void ) ); //$NON-NLS-1$
		f2.addParameter( ITypeInfo.t_void, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		NS2.addSymbol( f2 );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A" ); //$NON-NLS-1$
		A.setType( ITypeInfo.t_class );
		look = compUnit.lookupNestedNameSpecifier( "NS2" ); //$NON-NLS-1$
		assertEquals( look, NS2 );
		
		look = NS2.qualifiedLookup( "B" ); //$NON-NLS-1$
		assertEquals( look, B );
		A.addParent( B );
		
		compUnit.addSymbol( A );
		
		look = compUnit.lookup( "A" ); //$NON-NLS-1$
		assertEquals( look, A );
		ISymbol a = table.newSymbol( "a" ); //$NON-NLS-1$
		a.setType( ITypeInfo.t_type );
		a.setTypeSymbol( look );
		compUnit.addSymbol( a );
		
		ArrayList paramList = new ArrayList();
		look = compUnit.lookup( "a" ); //$NON-NLS-1$
		assertEquals( look, a );
		ITypeInfo param = TypeInfoProvider.newTypeInfo( look.getType(), 0, look, null, false );
		//new PtrOp( PtrOp.t_reference )
		param.applyOperatorExpression( ITypeInfo.OperatorExpression.addressof );
		paramList.add( param );
		
		look = compUnit.unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, f2 );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IDerivableContainerSymbol C = table.newDerivableContainerSymbol( "C" ); //$NON-NLS-1$
		C.setType( ITypeInfo.t_class );
		compUnit.addSymbol(C);
				
		IParameterizedSymbol f1 = table.newParameterizedSymbol("foo"); //$NON-NLS-1$
		f1.setType( ITypeInfo.t_function );
		f1.setReturnType( table.newSymbol( "", ITypeInfo.t_void ) ); //$NON-NLS-1$
		f1.addParameter( ITypeInfo.t_int, 0, null, false );
		C.addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol("foo"); //$NON-NLS-1$
		f2.setType( ITypeInfo.t_function );
		f2.setReturnType( table.newSymbol( "", ITypeInfo.t_void ) ); //$NON-NLS-1$
		f2.addParameter( ITypeInfo.t_int, 0, null, false );
		f2.addParameter( ITypeInfo.t_char, 0, null, false );
		C.addSymbol( f2 );
		
		IParameterizedSymbol f3 = table.newParameterizedSymbol("foo"); //$NON-NLS-1$
		f3.setType( ITypeInfo.t_function );
		f3.setReturnType( table.newSymbol( "", ITypeInfo.t_void ) ); //$NON-NLS-1$
		f3.addParameter( ITypeInfo.t_int, 0, null, false );
		f3.addParameter( ITypeInfo.t_char, 0, null, false );
		f3.addParameter( C, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		C.addSymbol( f3 );
		
		ISymbol look = compUnit.lookup("C"); //$NON-NLS-1$
		assertEquals( look, C );
		
		ISymbol c = table.newSymbol("c"); //$NON-NLS-1$
		c.setType( ITypeInfo.t_type );
		c.setTypeSymbol( look );
		c.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer, false, false ) );
		compUnit.addSymbol( c );
		
		look = compUnit.lookup( "c" ); //$NON-NLS-1$
		assertEquals( look, c );
		assertEquals( look.getTypeSymbol(), C );
		
		ArrayList paramList = new ArrayList();
															  
		ITypeInfo p1 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null );
		ITypeInfo p2 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_char, 0, null );
		ITypeInfo p3 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, c );
		
		paramList.add( p1 );
		look = C.memberFunctionLookup( "foo", paramList ); //$NON-NLS-1$
		assertEquals( look, f1 );
		
		paramList.add( p2 );
		look = C.memberFunctionLookup( "foo", paramList ); //$NON-NLS-1$
		assertEquals( look, f2 );
				
		paramList.add( p3 );
		look = C.memberFunctionLookup( "foo", paramList ); //$NON-NLS-1$
		assertEquals( look, f3 );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol("f"); //$NON-NLS-1$
		f1.setType( ITypeInfo.t_function );
		f1.addParameter( ITypeInfo.t_int, 0, null, false );
		compUnit.addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol("f"); //$NON-NLS-1$
		f2.setType( ITypeInfo.t_function );
		f2.addParameter( ITypeInfo.t_char, 0, null, true );
		compUnit.addSymbol( f2 );
		
		ArrayList paramList = new ArrayList();
		ITypeInfo p1 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null );
		paramList.add( p1 );
		
		ISymbol look = compUnit.unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, f1 );
		
		paramList.clear();
		ITypeInfo p2 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_char, 0, null );
		paramList.add( p2 );
		look = compUnit.unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, f2 );
		
		paramList.clear();
		ITypeInfo p3 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_bool, 0, null );
		paramList.add( p3 );
		look = compUnit.unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, f1 );
		
		look = compUnit.unqualifiedFunctionLookup( "f", null ); //$NON-NLS-1$
		assertEquals( look, f2 );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A" ); //$NON-NLS-1$
		A.setType( ITypeInfo.t_class );
		compUnit.addSymbol( A );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B" ); //$NON-NLS-1$
		B.setType( ITypeInfo.t_class );
		B.addParent( A );
		compUnit.addSymbol( B );
		
		IDerivableContainerSymbol C = table.newDerivableContainerSymbol( "C" ); //$NON-NLS-1$
		C.setType( ITypeInfo.t_class );
		C.addParent( B );
		compUnit.addSymbol( C );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f" ); //$NON-NLS-1$
		f1.setType( ITypeInfo.t_function );
		f1.addParameter( A, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		compUnit.addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f" ); //$NON-NLS-1$
		f2.setType( ITypeInfo.t_function );
		f2.addParameter( B, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		compUnit.addSymbol( f2 );
		
		ISymbol a = table.newSymbol( "a" ); //$NON-NLS-1$
		a.setType( ITypeInfo.t_type );
		a.setTypeSymbol( A );
		a.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer, false, false ) );
		
		ISymbol c = table.newSymbol( "c" ); //$NON-NLS-1$
		c.setType( ITypeInfo.t_type );
		c.setTypeSymbol( C );
		c.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer, false, false ) );
		
		ArrayList paramList = new ArrayList();
		ITypeInfo p1 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, a );
		paramList.add( p1 );
		ISymbol look = compUnit.unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, f1 );
		
		paramList.clear();
		ITypeInfo p2 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, c );
		paramList.add( p2 );
		look = compUnit.unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, f2 );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A" ); //$NON-NLS-1$
		A.setType( ITypeInfo.t_class );
		compUnit.addSymbol( A );
		
		ISymbol B = table.newSymbol( "B" ); //$NON-NLS-1$
		B.setType( ITypeInfo.t_type );
		B.setTypeSymbol( A );
		B.getTypeInfo().setBit( true, ITypeInfo.isTypedef );
		B.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer, false, false ) );
		compUnit.addSymbol( B );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f" ); //$NON-NLS-1$
		f1.setType( ITypeInfo.t_function );
		f1.addParameter( A, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		compUnit.addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f" ); //$NON-NLS-1$
		f2.setType( ITypeInfo.t_function );
		f2.addParameter( A, 0, null, false );
		compUnit.addSymbol( f2 );

		ISymbol a = table.newSymbol( "a" ); //$NON-NLS-1$
		a.setType( ITypeInfo.t_type );
		a.setTypeSymbol( A );
		compUnit.addSymbol( a );
				
		ISymbol b = table.newSymbol( "b" ); //$NON-NLS-1$
		b.setType( ITypeInfo.t_type );
		b.setTypeSymbol( B );
		compUnit.addSymbol( b );
		
		ISymbol array = table.newSymbol( "array" ); //$NON-NLS-1$
		array.setType( ITypeInfo.t_type );
		array.setTypeSymbol( A );
		array.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_array, false, false ) );
				
		ArrayList paramList = new ArrayList();
		ITypeInfo p = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, a );
		paramList.add( p );
		
		ISymbol look = compUnit.unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, f2 );
		
		p.applyOperatorExpression( ITypeInfo.OperatorExpression.addressof );
		look = compUnit.unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, f1 );
		
		p.setTypeSymbol( b );
		p.getPtrOperators().clear();
		look = compUnit.unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, f1 );
		
		p = p.getFinalType( null );
		p.applyOperatorExpression( ITypeInfo.OperatorExpression.indirection );
		paramList.clear();
		paramList.add( p );
		look = compUnit.unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, f2 );
		
		p.setTypeSymbol( array );
		p.getPtrOperators().clear();
		look = compUnit.unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, f1 );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A" ); //$NON-NLS-1$
		A.setType( ITypeInfo.t_class );
		compUnit.addSymbol( A );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B" ); //$NON-NLS-1$
		B.setType( ITypeInfo.t_class );
		compUnit.addSymbol( B );
		
		IParameterizedSymbol constructor = table.newParameterizedSymbol("B"); //$NON-NLS-1$
		constructor.setType( ITypeInfo.t_constructor );
		constructor.addParameter( A, 0, null, false );
		B.addConstructor( constructor );
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f" ); //$NON-NLS-1$
		f.setType( ITypeInfo.t_function );
		f.addParameter( B, 0, null, false );
		compUnit.addSymbol( f );
		
		ISymbol a = table.newSymbol( "a" ); //$NON-NLS-1$
		a.setTypeInfo( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type ) );
		a.setTypeSymbol( A );
		compUnit.addSymbol( a );
		
		ArrayList paramList = new ArrayList();
		ITypeInfo p = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, a );
		paramList.add( p );
		
		ISymbol look = compUnit.unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, f );	
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
	 * 	  f( (const int *)&i, 1L ); //calls f(const int *, short ) because const &i->int* is better than &i->int *
	 * 	  					   //and 1L->short and 1L->int are indistinguishable
	 * }
	 */
	public void testOverloadRanking() throws Exception{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f" ); //$NON-NLS-1$
		f1.setType( ITypeInfo.t_function );
		f1.addParameter( ITypeInfo.t_int, ITypeInfo.isConst, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer, false, false ), false );
		f1.addParameter( ITypeInfo.t_int, ITypeInfo.isShort, null, false );
		
		compUnit.addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f" ); //$NON-NLS-1$
		f2.setType( ITypeInfo.t_function );
		f2.addParameter( ITypeInfo.t_int, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		f2.addParameter( ITypeInfo.t_int, 0, null, false );
		compUnit.addSymbol( f2 );
		
		ISymbol i = table.newSymbol( "i" ); //$NON-NLS-1$
		i.setType( ITypeInfo.t_int );
		compUnit.addSymbol( i );
		
		ISymbol s = table.newSymbol( "s" ); //$NON-NLS-1$
		s.setType( ITypeInfo.t_int );
		s.getTypeInfo().setBit( true, ITypeInfo.isShort );
		compUnit.addSymbol( s );
		
		IParameterizedSymbol main = table.newParameterizedSymbol( "main" ); //$NON-NLS-1$
		main.setType( ITypeInfo.t_function );
		compUnit.addSymbol( main );
		
		ArrayList params = new ArrayList();
		ITypeInfo p1 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, i );
		p1.applyOperatorExpression( ITypeInfo.OperatorExpression.addressof );
		ITypeInfo p2 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, s );
		params.add( p1 );
		params.add( p2 );
		
		ISymbol look = null;
		
		try{
			look = main.unqualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		
		params.clear();
		ITypeInfo p3 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, ITypeInfo.isLong, null );
		params.add( p1 );
		params.add( p3 );
		look = main.unqualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
		assertEquals( look, f2 );
		
		params.clear();
		ITypeInfo p4 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_char, 0, null );
		params.add( p1 );
		params.add( p4 );
		look = main.unqualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
		assertEquals( look, f2 );
		
		params.clear();
		p1 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, ITypeInfo.isConst, null, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer, false, false ), false );
		
		params.add( p1 );
		params.add( p3 );
		look = main.unqualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
		assertEquals( look, f1 );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B" ); //$NON-NLS-1$
		B.setType( ITypeInfo.t_class );
		
		compUnit.addSymbol( B );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A" ); //$NON-NLS-1$
		A.setType( ITypeInfo.t_class );
		compUnit.addSymbol( A );
		
		IParameterizedSymbol constructA = table.newParameterizedSymbol( "A" ); //$NON-NLS-1$
		constructA.setType( ITypeInfo.t_constructor );
		constructA.addParameter( B, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference ), false );
		A.addConstructor( constructA );
		
		IParameterizedSymbol operator = table.newParameterizedSymbol( "operator A" ); //$NON-NLS-1$
		operator.setType( ITypeInfo.t_function );
		B.addSymbol( operator );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f" ); //$NON-NLS-1$
		f1.setType( ITypeInfo.t_function );
		f1.addParameter( A, 0, null, false );
		compUnit.addSymbol( f1 );
		
		ISymbol b = table.newSymbol( "b" ); //$NON-NLS-1$
		b.setType( ITypeInfo.t_type );
		b.setTypeSymbol( B );
		
		ArrayList params = new ArrayList();
		ITypeInfo p1 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, b );
		params.add( p1 );
		
		ISymbol look = null;
		
		try{
			look = compUnit.unqualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous ); 
		}
		
		IDerivableContainerSymbol C = table.newDerivableContainerSymbol("C"); //$NON-NLS-1$
		C.setType( ITypeInfo.t_class );
		compUnit.addSymbol( C );
		
		IParameterizedSymbol constructC = table.newParameterizedSymbol("C"); //$NON-NLS-1$
		constructC.setType( ITypeInfo.t_constructor );
		constructC.addParameter( B, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference ), false );
		C.addConstructor( constructC );

		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f" ); //$NON-NLS-1$
		f2.setType( ITypeInfo.t_function );
		f2.addParameter(  C, 0, null, false );
		compUnit.addSymbol( f2 );
		
		try{
			look = compUnit.unqualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous ); 
		}
		
		IParameterizedSymbol f3 = table.newParameterizedSymbol( "f" ); //$NON-NLS-1$
		f3.setType( ITypeInfo.t_function );
		f3.addParameter(  B, 0, null, false );
		compUnit.addSymbol( f3 );
		
		look = compUnit.unqualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
		assertEquals( look, f3 );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
//	public void testMarkRollback() throws Exception{
//		newTable();
//		
//		IDerivableContainerSymbol A = table.newDerivableContainerSymbol("A");
//		A.setType( TypeInfo.t_class );
//		table.getCompilationUnit().addSymbol( A );
//		
//		Mark mark = table.setMark();
//		
//		ISymbol f = table.newSymbol("f");
//		A.addSymbol( f );
//		
//		ISymbol look = A.lookup("f");
//		assertEquals( look, f );
//		
//		assertTrue( table.rollBack( mark ) );
//		
//		look = A.lookup("f");
//		assertEquals( look, null );
//		
//		IDerivableContainerSymbol B = table.newDerivableContainerSymbol("B");
//		B.setType( TypeInfo.t_class );
//		
//		mark = table.setMark();
//		table.getCompilationUnit().addSymbol( B );
//		Mark mark2 = table.setMark();
//		A.addParent( B );
//		Mark mark3 = table.setMark();
//		
//		IParameterizedSymbol C = table.newParameterizedSymbol("C");
//		C.addParameter( TypeInfo.t_class, 0, null, false );
//		
//		assertEquals( C.getParameterList().size(), 1 );
//		table.rollBack( mark3 );
//		assertEquals( C.getParameterList().size(), 0 );
//		assertEquals( A.getParents().size(), 1 );
//		table.rollBack( mark2 );
//		assertEquals( A.getParents().size(), 0 );
//		
//		assertFalse( table.commit( mark2 ) );
//		assertFalse( table.rollBack( mark2 ) );
//		
//		B.setType( TypeInfo.t_namespace );
//		
//		mark = table.setMark();
//		C.addUsingDirective( B );
//		assertEquals( C.getUsingDirectives().size(), 1 );
//		table.rollBack( mark );
//		assertEquals( C.getUsingDirectives().size(), 0 );
//	}
	
	/**
	 * class A;
	 *
	 * A * a;
	 *
	 * class A {};
	 *
	 * @throws Exception
	 */
	public void testForwardClassDeclaration() throws Exception{
		newTable();
		
		ISymbol forwardSymbol = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		forwardSymbol.setIsForwardDeclaration( true );
		
		table.getCompilationUnit().addSymbol( forwardSymbol );
		
		/*...*/
		
		ISymbol lookup = table.getCompilationUnit().lookup( "A" ); //$NON-NLS-1$
		ISymbol otherLookup = table.getCompilationUnit().elaboratedLookup( ITypeInfo.t_class, "A" ); //$NON-NLS-1$
		
		assertEquals( lookup, otherLookup );
		assertEquals( lookup, forwardSymbol );
		
		ISymbol a = table.newSymbol( "a", ITypeInfo.t_type ); //$NON-NLS-1$
		a.setTypeSymbol( forwardSymbol );
		a.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ) );
		
		table.getCompilationUnit().addSymbol( a );
		
		/*...*/
		
		lookup = table.getCompilationUnit().lookup( "A" ); //$NON-NLS-1$
		IDerivableContainerSymbol classA = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		assertTrue( lookup.isForwardDeclaration() );
		lookup.setForwardSymbol( classA ); 
		
		table.getCompilationUnit().addSymbol( classA );
		
		lookup = table.getCompilationUnit().lookup( "a" ); //$NON-NLS-1$
		assertEquals( lookup, a );
		assertEquals( a.getTypeSymbol(), classA );
		
		lookup = table.getCompilationUnit().lookup( "A" ); //$NON-NLS-1$
		assertEquals( lookup, classA );
		
		lookup = table.getCompilationUnit().elaboratedLookup( ITypeInfo.t_class, "A" ); //$NON-NLS-1$
		assertEquals( lookup, classA );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * class A;
	 * 
	 * class B {
	 *    static void f( A * );
	 *    static void f( int );
	 * };
	 * 
	 * A* a1;
	 * 
	 * class A {};
	 * 
	 * void B::f( A * ) {}
	 * void B::f( int ) {}
	 * 
	 * A* a2;
	 * 
	 * B::f( a1 );
	 * B::f( a2 );
	 * 
	 * @throws Exception
	 */
	public void testForwardDeclarationUsedAsFunctionParam() throws Exception{
		newTable();
		
		ISymbol forwardSymbol = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		forwardSymbol.setIsForwardDeclaration( true );
		table.getCompilationUnit().addSymbol( forwardSymbol );
		
		/*...*/
	
		IDerivableContainerSymbol classB = table.newDerivableContainerSymbol( "B", ITypeInfo.t_class ); //$NON-NLS-1$
			
		IParameterizedSymbol fn1 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		ISymbol lookup = table.getCompilationUnit().lookup( "A" ); //$NON-NLS-1$
		assertEquals( lookup, forwardSymbol );
		fn1.addParameter( lookup, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		fn1.getTypeInfo().setBit( true, ITypeInfo.isStatic );
		classB.addSymbol( fn1 );
		
		IParameterizedSymbol fn2 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		fn2.addParameter( ITypeInfo.t_int, 0, null, false );
		fn2.getTypeInfo().setBit( true, ITypeInfo.isStatic );
		classB.addSymbol( fn2 );
		
		table.getCompilationUnit().addSymbol( classB );
		
		/*...*/
		
		ISymbol a1 = table.newSymbol( "a1", ITypeInfo.t_type ); //$NON-NLS-1$
		lookup = table.getCompilationUnit().lookup( "A" ); //$NON-NLS-1$
		assertEquals( lookup, forwardSymbol );
		a1.setTypeSymbol( lookup );
		a1.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ) );
		
		table.getCompilationUnit().addSymbol( a1 );
		
		/*...*/
		
		lookup = table.getCompilationUnit().lookup( "A" ); //$NON-NLS-1$
		IDerivableContainerSymbol classA = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		assertTrue( lookup.isForwardDeclaration() );
		lookup.setForwardSymbol( classA ); 
		table.getCompilationUnit().addSymbol( classA );
		
		/*..*/
		ISymbol a2 = table.newSymbol( "a2", ITypeInfo.t_type ); //$NON-NLS-1$
		lookup = table.getCompilationUnit().lookup( "A" ); //$NON-NLS-1$
		assertEquals( lookup, classA );
		a2.setTypeSymbol( lookup );
		a2.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ) );
		
		table.getCompilationUnit().addSymbol( a2 );
		
		/*..*/
		
		ArrayList paramList = new ArrayList();
		ITypeInfo p1 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, a1 );
		paramList.add( p1 );
		ISymbol look = classB.memberFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, fn1 );
		
		paramList.clear();
		p1 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, a2 );
		paramList.add( p1 );
		look = classB.memberFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, fn1 );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	public void testConstructors() throws Exception{
		newTable();
		
		IDerivableContainerSymbol classA = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		
		IParameterizedSymbol constructor1 = table.newParameterizedSymbol( "A", ITypeInfo.t_constructor ); //$NON-NLS-1$
		constructor1.addParameter( classA, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference ), false );
		
		IParameterizedSymbol constructor2 = table.newParameterizedSymbol( "A", ITypeInfo.t_constructor ); //$NON-NLS-1$
		constructor2.addParameter( ITypeInfo.t_int, 0, null, false );
		
		IParameterizedSymbol constructor3 = table.newParameterizedSymbol( "A", ITypeInfo.t_constructor ); //$NON-NLS-1$
		constructor3.addParameter( ITypeInfo.t_char, 0, null, false );
		
		classA.addConstructor( constructor1 );
		classA.addConstructor( constructor2 );
		classA.addConstructor( constructor3 );
		
		assertEquals( classA.getConstructors().size(), 3 );
		
		IParameterizedSymbol cloned = (IParameterizedSymbol) constructor2.clone();
		try{
			classA.addConstructor( cloned );
		} catch ( ParserSymbolTableException e ) {
			assertEquals( e.reason, ParserSymbolTableException.r_InvalidOverload );
		}
		
		ArrayList paramList = new ArrayList();
		paramList.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		
		ISymbol lookup = classA.lookupConstructor( paramList );
		
		assertEquals( lookup, constructor2 );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * 
	 * @throws Exception
	 * 
	 * namespace A
	 * { 
	 *    int x;
	 * }
	 * namespace B = A;
	 * 
	 * ++B::x;
	 */
	public void testNamespaceAlias() throws Exception{
		newTable();
		
		IContainerSymbol NSA = table.newContainerSymbol( "A", ITypeInfo.t_namespace ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( NSA );
		
		ISymbol x = table.newSymbol( "x", ITypeInfo.t_int ); //$NON-NLS-1$
		NSA.addSymbol( x );
		
		IContainerSymbol NSB = table.newContainerSymbol( "B", ITypeInfo.t_namespace ); //$NON-NLS-1$
		NSB.setForwardSymbol( NSA );  //alias B to A
		
		table.getCompilationUnit().addSymbol( NSB );
		
		ISymbol lookup = table.getCompilationUnit().lookup( "B" ); //$NON-NLS-1$
		assertEquals( lookup, NSB );
		
		lookup = NSB.lookup( "x" ); //$NON-NLS-1$
		assertEquals( lookup, x );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * 
	 * @throws Exception
	 * namespace A 
	 * {
	 *   void f( );
	 * }
	 * namespace B = A;
	 * 
	 * B::f();
	 * 
	 * using namespace B;
	 * f();
	 */
	public void testUsingNamespaceAlias() throws Exception{
		newTable();
		
		IContainerSymbol NSA = table.newContainerSymbol( "A", ITypeInfo.t_namespace ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( NSA );
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f.setReturnType( table.newSymbol( "", ITypeInfo.t_void ) ); //$NON-NLS-1$
		
		NSA.addSymbol( f );
		
		IContainerSymbol NSB = table.newContainerSymbol( "B", ITypeInfo.t_namespace ); //$NON-NLS-1$
		NSB.setForwardSymbol( NSA );
		table.getCompilationUnit().addSymbol( NSB );
		
		//look for function that has no parameters
		ArrayList paramList = new ArrayList();
		ISymbol look = NSB.qualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, f );
		
		table.getCompilationUnit().addUsingDirective( NSB );
		
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		assertEquals( look, f );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * 
	 * @throws Exception
	 * 
	 * The general rule is that when you set a TypeInfo's type to be t_type, you 
	 * should set the type symbol to be something.  This is to test that the function
	 * resolution can handle a bad typeInfo that has a null symbol without throwing a NPE
	 */
	public void testBadParameterInfo() throws Exception{
		newTable();
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f.setReturnType( table.newSymbol( "", ITypeInfo.t_void ) ); //$NON-NLS-1$
		
		IDerivableContainerSymbol a = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( a );
		
		f.addParameter( a, 0, null, false );
		
		table.getCompilationUnit().addSymbol( f );
		
		ArrayList paramList = new ArrayList ();
		
		ITypeInfo param = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, null );
		
		paramList.add( param );
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		
		assertEquals( look, null );
		
		ISymbol intermediate = table.newSymbol( "", ITypeInfo.t_type ); //$NON-NLS-1$
		
		param.setTypeSymbol( intermediate );
		
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", paramList ); //$NON-NLS-1$
		
		assertEquals( look, null );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * class A {
	 *    A ( C ) {};
	 * } a;
	 * class B : public A {} b;
	 * class C {
	 *    C ( A ) {};
	 * } c;
	 * 
	 * isTrue ? &a : &b;	//expect type = 2nd operand ( A )
	 * isTrue ? &a : &c;	//expect null, neither converts
	 * isTrue ? a : c;		//expect exception, both convert 
	 * 
	 * @throws Exception
	 */
	public void testGetConditionalOperand_bug43106() throws Exception{
		newTable();
		
		IDerivableContainerSymbol clsA = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		IDerivableContainerSymbol clsB = table.newDerivableContainerSymbol( "B", ITypeInfo.t_class ); //$NON-NLS-1$
		
		clsB.addParent( clsA );
		
		table.getCompilationUnit().addSymbol( clsA );
		table.getCompilationUnit().addSymbol( clsB );
		
		ISymbol a = table.newSymbol( "a", ITypeInfo.t_type ); //$NON-NLS-1$
		a.setTypeSymbol( clsA );
		
		ISymbol b = table.newSymbol( "b", ITypeInfo.t_type ); //$NON-NLS-1$
		b.setTypeSymbol( clsB );
		
		table.getCompilationUnit().addSymbol( a );
		table.getCompilationUnit().addSymbol( b );
		
		ITypeInfo secondOp = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, a );
		secondOp.applyOperatorExpression( ITypeInfo.OperatorExpression.addressof );
		ITypeInfo thirdOp = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, b );
		thirdOp.applyOperatorExpression( ITypeInfo.OperatorExpression.addressof );
		
		ITypeInfo returned = table.getConditionalOperand( secondOp, thirdOp );
		assertEquals( returned, secondOp );
		
		IDerivableContainerSymbol clsC = table.newDerivableContainerSymbol( "C", ITypeInfo.t_class ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( clsC );
		ISymbol c = table.newSymbol( "c", ITypeInfo.t_type ); //$NON-NLS-1$
		c.setTypeSymbol( clsC );
		table.getCompilationUnit().addSymbol( c );
		
		ITypeInfo anotherOp = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, c );
		anotherOp.applyOperatorExpression( ITypeInfo.OperatorExpression.addressof );
		
		returned = table.getConditionalOperand( secondOp, anotherOp );
		assertEquals( returned, null );
		
		IParameterizedSymbol constructorA = table.newParameterizedSymbol( "A", ITypeInfo.t_constructor ); //$NON-NLS-1$
		constructorA.addParameter( clsC, 0, null, false );
		clsA.addConstructor( constructorA );
		
		IParameterizedSymbol constructorC = table.newParameterizedSymbol( "C", ITypeInfo.t_constructor ); //$NON-NLS-1$
		constructorC.addParameter( clsA, 0, null, false );
		clsC.addConstructor( constructorC );
		
		secondOp.getPtrOperators().clear();
		anotherOp.getPtrOperators().clear();
		try{
			
			returned = table.getConditionalOperand( secondOp, anotherOp );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			//good
		}
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * 
	 * @throws Exception
	 * class A {};
	 * class B : public A {} b;
	 * class C : private A {} c;
	 * int f ( A & );
	 * 
	 * int i = f ( b );  //calls f( A & );
	 * 
	 * int f ( B & );
	 * i = f( b );   	//now calls f( B& );
	 * 
	 * i = f( c );	//exception, A is not an accessible base class
	 */
	public void testDerivedReference() throws Exception{
		newTable();
		
		IDerivableContainerSymbol clsA = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		IDerivableContainerSymbol clsB = table.newDerivableContainerSymbol( "B", ITypeInfo.t_class ); //$NON-NLS-1$
		IDerivableContainerSymbol clsC = table.newDerivableContainerSymbol( "C", ITypeInfo.t_class ); //$NON-NLS-1$
		
		clsB.addParent( clsA );
		clsC.addParent( clsA, false, ASTAccessVisibility.PRIVATE, 0, null );
		
		ISymbol b = table.newSymbol("b", ITypeInfo.t_type ); //$NON-NLS-1$
		b.setTypeSymbol( clsB );
		
		ISymbol c = table.newSymbol("c", ITypeInfo.t_type ); //$NON-NLS-1$
		c.setTypeSymbol( clsC );
		
		table.getCompilationUnit().addSymbol( clsA );
		table.getCompilationUnit().addSymbol( clsB );
		table.getCompilationUnit().addSymbol( clsC );
		table.getCompilationUnit().addSymbol( b );
		table.getCompilationUnit().addSymbol( c );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f1.addParameter( clsA, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference ), false );
		table.getCompilationUnit().addSymbol( f1 );
		
		ArrayList parameters = new ArrayList();
		ITypeInfo param = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, b );
		parameters.add( param );
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", parameters ); //$NON-NLS-1$
		assertEquals( look, f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f2.addParameter( clsB, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference ), false );
		table.getCompilationUnit().addSymbol( f2 );
		
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", parameters ); //$NON-NLS-1$
		assertEquals( look, f2 );
		
		parameters.clear();
		param = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, c );
		parameters.add( param );
		try{
			look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", parameters ); //$NON-NLS-1$
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			//good
		}
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * 
	 * @throws Exception
	 * 
	 * class A {
	 * private : 
	 *    A ( const A & ) {}
	 * } a;
	 * 
	 * class B : public A {
	 * } b;
	 * 
	 * 1 > 2 ? a : b;	//fails, b can't be converted to a without the A( const A & ) copy constructor
	 * -----------------------
	 * class A {
	 *    A ( const A & ) {}
	 * } a;
	 * class B : public A {} b;
	 * 
	 * 1 > 2 ? a : b;  //succeeds, b can be converted to a using copy constructor
	 * 
	 */
	public void testAddCopyConstructor() throws Exception {
		newTable();
		
		IDerivableContainerSymbol clsA = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( clsA );
		
		ISymbol a = table.newSymbol( "a", ITypeInfo.t_type ); //$NON-NLS-1$
		a.setTypeSymbol( clsA );
		table.getCompilationUnit().addSymbol( a );
		
		IDerivableContainerSymbol clsB = table.newDerivableContainerSymbol( "B", ITypeInfo.t_class ); //$NON-NLS-1$
		clsB.addParent( clsA );
		table.getCompilationUnit().addSymbol( clsB );
		
		ISymbol b = table.newSymbol( "b", ITypeInfo.t_type ); //$NON-NLS-1$
		b.setTypeSymbol( clsB );
		table.getCompilationUnit().addSymbol( b );
		
		ITypeInfo secondOp = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, a, null, false );
		ITypeInfo thirdOp = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, b, null, false );
		
		ITypeInfo returned = table.getConditionalOperand( secondOp, thirdOp );
		assertEquals( returned, null );
		
		clsA.addCopyConstructor();
		clsB.addCopyConstructor();
		
		returned = table.getConditionalOperand( secondOp, thirdOp );
		assertEquals( returned, secondOp );	
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	public void testbug43834() throws Exception{
		newTable();
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( f );
		
		ArrayList parameters = new ArrayList();
		ITypeInfo param = TypeInfoProvider.newTypeInfo( ITypeInfo.t_void, 0, null );
		parameters.add( param );
		
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", parameters ); //$NON-NLS-1$
		assertEquals( look, f );
		
		f.addParameter( ITypeInfo.t_void, 0, null, false );
		
		parameters.clear();
		
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", parameters ); //$NON-NLS-1$
		assertEquals( look, f );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * 
	 * namespace A{
	 *    void f();
	 * }
	 * namespace B{
	 *    int f;
	 * }
	 * namespace C{
	 *    using namespace A;
	 *    using namespace B;
	 *    using f;		//ambiguous, int f or void f()?
	 * }
	 */
	public void testBug43503_AmbiguousUsing() throws Exception{
		newTable();
		IContainerSymbol NSA = table.newContainerSymbol( "A", ITypeInfo.t_namespace ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( NSA );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		NSA.addSymbol( f1 );
		
		IContainerSymbol NSB = table.newContainerSymbol( "B", ITypeInfo.t_namespace ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( NSB );
		
		ISymbol f2 = table.newSymbol( "f", ITypeInfo.t_int ); //$NON-NLS-1$
		NSB.addSymbol( f2 );
		
		IContainerSymbol NSC = table.newContainerSymbol( "C", ITypeInfo.t_namespace ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( NSC );
		NSC.addUsingDirective( NSA );
		NSC.addUsingDirective( NSB );
		
		try{
			NSC.addUsingDeclaration( "f" ); //$NON-NLS-1$
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * void f( void );
	 * void f( int );
	 * 
	 * void * pF = &f;  //lookup without function parameters, should be ambiguous
	 * @throws Exception
	 */
	public void testBug43503_UnableToResolveFunction() throws Exception{
		newTable();
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f2.addParameter( ITypeInfo.t_int, 0, null, false );
		
		table.getCompilationUnit().addSymbol( f1 );
		table.getCompilationUnit().addSymbol( f2 );
		
		try{
			table.getCompilationUnit().lookup( "f" ); //$NON-NLS-1$
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_UnableToResolveFunction );
		}
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * int initialize();
	 * int initialize(){
	 *    return 3;
	 * }
	 * 
	 * int i = initialize();
	 * 
	 * @throws Exception
	 */
	public void testBug44510() throws Exception{
		newTable();
		
		IParameterizedSymbol init1 = table.newParameterizedSymbol( "initialize", ITypeInfo.t_function ); //$NON-NLS-1$
		
		table.getCompilationUnit().addSymbol( init1 );
		
		IParameterizedSymbol init2 = table.newParameterizedSymbol( "initialize", ITypeInfo.t_function ); //$NON-NLS-1$
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "initialize", new ArrayList() ); //$NON-NLS-1$
		assertEquals( look, init1 );
		
		init1.setIsForwardDeclaration( true );
		init1.setForwardSymbol( init2 );
		
		table.getCompilationUnit().addSymbol( init2 );
		
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "initialize", new ArrayList() ); //$NON-NLS-1$
		
		assertEquals( look, init2 ); 
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * class A {
	 *    void f( int ) {}
	 *    void f( ) {}
	 * };
	 * class B : public A {
	 *    void f( char ) { }
	 * } b;
	 * 
	 * b.f( 1 );  //calls B::f
	 * b.f();     //error
	 * @throws Exception
	 */
	public void testBug46882() throws Exception{
		newTable();
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		
		table.getCompilationUnit().addSymbol( A );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f1.addParameter( ITypeInfo.t_int, 0, null, false );
		A.addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		A.addSymbol( f2 );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", ITypeInfo.t_class ); //$NON-NLS-1$
		B.addParent( A );
		
		table.getCompilationUnit().addSymbol( B );
		
		IParameterizedSymbol f3 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f3.addParameter( ITypeInfo.t_char, 0, null, false );
		B.addSymbol( f3 );
		
		List params = new ArrayList();
		params.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		
		ISymbol look = B.qualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
		assertEquals( look, f3 );
		
		params.clear();
		look = B.qualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
		assertEquals( look, null );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * int aVar;
	 * void foo( ) {
	 *    int anotherVar;
	 *    a(CTRL+SPACE)
	 * }
	 */
	public void testPrefixLookup_Unqualified() throws Exception {
		newTable( ParserLanguage.CPP, ParserMode.COMPLETION_PARSE );
		
		ISymbol aVar = table.newSymbol( "aVar", ITypeInfo.t_int ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( aVar );
		
		IParameterizedSymbol foo = table.newParameterizedSymbol( "foo", ITypeInfo.t_function ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( foo );
		
		ISymbol anotherVar = table.newSymbol( "anotherVar", ITypeInfo.t_int ); //$NON-NLS-1$
		foo.addSymbol( anotherVar );
		
		List results = foo.prefixLookup( null, "a", false, null ); //$NON-NLS-1$
		assertTrue( results != null );
		assertEquals( results.size(), 2 );
		
		assertTrue( results.contains( aVar ) );
		assertTrue( results.contains( anotherVar ) );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * int aVar;	//not a member of D, not reported
	 * 
	 * class D{
	 *    int aField;
	 *    void aMethod();
	 * };
	 * 
	 * D d;
	 * d.a(CTRL+SPACE)
	 */
	public void testPrefixLookup_Qualified() throws Exception {
		newTable( ParserLanguage.CPP, ParserMode.COMPLETION_PARSE );
		
		ISymbol aVar = table.newSymbol( "aVar", ITypeInfo.t_int ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( aVar );
		
		IDerivableContainerSymbol D = table.newDerivableContainerSymbol( "D", ITypeInfo.t_class ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( D );
		
		ISymbol aField = table.newSymbol( "aField", ITypeInfo.t_int ); //$NON-NLS-1$
		IParameterizedSymbol aMethod = table.newParameterizedSymbol( "aMethod", ITypeInfo.t_function ); //$NON-NLS-1$
		
		D.addSymbol( aField );
		D.addSymbol( aMethod );
		
		List results = D.prefixLookup( null, "a", true, null ); //$NON-NLS-1$
		
		assertTrue( results != null );
		assertEquals( results.size(), 2 );
		
		assertTrue( !results.contains( aVar ) );
		assertTrue( results.contains( aField ) );
		assertTrue( results.contains( aMethod ) );
		
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * class A {
	 *    int aVar
	 *    int anotherVar;		//hidden, not reported
	 *    void af ();			//hidden, not reported
	 * };
	 * 
	 * class B : public A {
	 *    int anotherVar;
	 *    void af( char ); 
	 * } b;
	 * 
	 * b.a(CTRL+SPACE)
	 * @throws Exception
	 */
	public void testPrefixLookup_Inheritance() throws Exception {
		newTable( ParserLanguage.CPP, ParserMode.COMPLETION_PARSE );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( A );
		
		ISymbol aVar = table.newSymbol( "aVar", ITypeInfo.t_int ); //$NON-NLS-1$
		ISymbol anotherVar1 = table.newSymbol( "anotherVar", ITypeInfo.t_int ); //$NON-NLS-1$
		A.addSymbol( aVar );
		A.addSymbol( anotherVar1 );
		
		IParameterizedSymbol af1 = table.newParameterizedSymbol( "af", ITypeInfo.t_function ); //$NON-NLS-1$
		A.addSymbol( af1 );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", ITypeInfo.t_class ); //$NON-NLS-1$
		B.addParent( A );
		
		table.getCompilationUnit().addSymbol( B );
		
		ISymbol anotherVar2 = table.newSymbol( "anotherVar", ITypeInfo.t_int ); //$NON-NLS-1$
		B.addSymbol( anotherVar2 );
		
		IParameterizedSymbol af2 = table.newParameterizedSymbol( "af", ITypeInfo.t_function ); //$NON-NLS-1$
		af2.addParameter(  ITypeInfo.t_char, 0, null, false );
		B.addSymbol( af2 );
		
		
		List results = B.prefixLookup( null, "a", true, null ); //$NON-NLS-1$
		
		assertTrue( results != null );
		assertEquals( results.size(), 3 );
		assertTrue( ! results.contains( anotherVar1 ) );
		assertTrue( ! results.contains( af1 ) );
		assertTrue( results.contains( aVar ) );
		assertTrue( results.contains( anotherVar2 ) );
		assertTrue( results.contains( af2 ) );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * int aa;
	 * namespace {
	 *    namespace U {
	 *       int a;
	 *    }
	 *    namespace V{
	 *       int a;
	 *    }
	 *    namespace W{
	 *       int a;
	 *    }
	 * 
	 *    void f(){
	 *       using namespace U;
	 *       using namespace V;
	 *       using namespace W;
	 *       a(CTRL+SPACE)
	 *    }
	 *  }
	 * 
	 * @throws Exception
	 */
	public void testPrefixLookup_Ambiguities() throws Exception{
		newTable( ParserLanguage.CPP, ParserMode.COMPLETION_PARSE );
		
		ISymbol aa = table.newSymbol( "aa", ITypeInfo.t_int ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( aa );
		
		IContainerSymbol ns = table.newContainerSymbol( "", ITypeInfo.t_namespace ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( ns );
		
		IContainerSymbol U = table.newContainerSymbol( "U", ITypeInfo.t_namespace ); //$NON-NLS-1$
		ns.addSymbol( U );
		ISymbol a1 = table.newSymbol( "a", ITypeInfo.t_int ); //$NON-NLS-1$
		U.addSymbol( a1 );
		
		IContainerSymbol V = table.newContainerSymbol( "V", ITypeInfo.t_namespace ); //$NON-NLS-1$
		ns.addSymbol( V );
		ISymbol a2 = table.newSymbol( "a", ITypeInfo.t_int ); //$NON-NLS-1$
		V.addSymbol( a2 );
		
		IContainerSymbol W = table.newContainerSymbol( "W", ITypeInfo.t_namespace ); //$NON-NLS-1$
		ns.addSymbol( W );
		ISymbol a3 = table.newSymbol( "a", ITypeInfo.t_int ); //$NON-NLS-1$
		W.addSymbol( a3 );
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		ns.addSymbol( f );

		f.addUsingDirective( U );
		f.addUsingDirective( V );
		f.addUsingDirective( W );
		
		List results = f.prefixLookup( null, "a", false, null ); //$NON-NLS-1$
		
		assertTrue( results != null );
		assertEquals( results.size(), 1 );
		assertTrue( results.contains( aa ) );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * int i;
	 * class A { 
	 *    void g(){
	 *       A a;
	 *       a.i++;  //fail qualified lookup, no i in A
	 *       i++;	 //success unqualified lookup
	 *    }    
	 * };
	 * 
	 * @throws Exception
	 */
	public void testQualifiedUnqualifiedLookup() throws Exception{
		newTable();
		
		ISymbol i = table.newSymbol( "i", ITypeInfo.t_int ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( i );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( A );
		
		IParameterizedSymbol g = table.newParameterizedSymbol( "g", ITypeInfo.t_function ); //$NON-NLS-1$
		A.addSymbol( g );
		
		assertEquals( null, A.qualifiedLookup( "i" ) ); //$NON-NLS-1$
		assertEquals( i, g.lookup( "i" ) ); //$NON-NLS-1$
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * class D { };
	 * class A { 
	 * 	   public: static int i; 
	 *     private: static int j;
	 *     friend class D;
	 * };
	 * class B : private A {};
	 * class C : public B, public A {};
	 * 
	 * 
	 * @throws Exception
	 */
	public void testVisibilityDetermination() throws Exception{
		newTable();
		
		IDerivableContainerSymbol D = table.newDerivableContainerSymbol( "D", ITypeInfo.t_class ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( D );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		ISymbol i = table.newSymbol( "i", ITypeInfo.t_int ); //$NON-NLS-1$
		ISymbol j = table.newSymbol( "j", ITypeInfo.t_int ); //$NON-NLS-1$
		
		table.getCompilationUnit().addSymbol( A );
		
		ISymbol friend = A.lookupForFriendship( "D" ); //$NON-NLS-1$
		assertEquals( friend, D );
		A.addFriend( friend );
		
		A.addSymbol( i );
		A.addSymbol( j );

		IASTCompilationUnit compUnit = new ASTCompilationUnit(table.getCompilationUnit() );
		ISymbolASTExtension cuExtension = new StandardSymbolExtension( table.getCompilationUnit(), (ASTSymbol) compUnit );
		table.getCompilationUnit().setASTExtension( cuExtension );
		
		IASTClassSpecifier clsSpec = new ASTClassSpecifier( A, ASTClassKind.CLASS, ClassNameType.IDENTIFIER, ASTAccessVisibility.PUBLIC, 0, 0, 0, 0, 0, new ArrayList( ), EMPTY_CHAR_ARRAY );  //$NON-NLS-1$
		ISymbolASTExtension clsExtension = new StandardSymbolExtension( A, (ASTSymbol) clsSpec );
		A.setASTExtension( clsExtension );
		
		IASTField field = new ASTField(i, null, null, null, 0, 0, 0, 0, 0, new ArrayList(), false, null, ASTAccessVisibility.PUBLIC, EMPTY_CHAR_ARRAY );
		ISymbolASTExtension extension = new StandardSymbolExtension( i, (ASTSymbol) field );
		i.setASTExtension( extension );
		
		field = new ASTField(i, null, null, null, 0, 0, 0, 0, 0, new ArrayList(), false, null, ASTAccessVisibility.PRIVATE, EMPTY_CHAR_ARRAY );
		extension = new StandardSymbolExtension( j, (ASTSymbol) field );
		j.setASTExtension( extension );
	
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", ITypeInfo.t_class ); //$NON-NLS-1$
		B.addParent( A, false, ASTAccessVisibility.PRIVATE, 0, null );
		table.getCompilationUnit().addSymbol( B );
		
		IDerivableContainerSymbol C = table.newDerivableContainerSymbol( "C", ITypeInfo.t_class ); //$NON-NLS-1$
		C.addParent( B );
		C.addParent( A );
		table.getCompilationUnit().addSymbol( C );
		
		assertTrue( table.getCompilationUnit().isVisible( i, A ) );
		assertFalse( table.getCompilationUnit().isVisible( i, B ) );
		assertTrue( table.getCompilationUnit().isVisible(i, C ) );
		assertTrue( D.isVisible( j, A ) );
		assertFalse( D.isVisible( j, B ) );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * struct a1{};
	 * void aFoo() {}
	 * int aa;
	 * class A2{
	 *    struct a3 {};
	 *    int a3;
	 *    void aF();
	 *    void f() {
	 *       int aLocal;
	 *       A(CTRL+SPACE)
	 *    };
	 * };
	 * @throws Exception
	 */
	public void testPrefixFiltering() throws Exception{
		newTable( ParserLanguage.CPP, ParserMode.COMPLETION_PARSE );
		IDerivableContainerSymbol a1 = table.newDerivableContainerSymbol( "a1", ITypeInfo.t_struct ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( a1 );
		
		IParameterizedSymbol aFoo = table.newParameterizedSymbol( "aFoo", ITypeInfo.t_function ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( aFoo );
		
		ISymbol aa = table.newSymbol( "aa", ITypeInfo.t_int ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( aa );
		
		IDerivableContainerSymbol A2 = table.newDerivableContainerSymbol( "A2", ITypeInfo.t_class ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( A2 );
		
		IDerivableContainerSymbol a3 = table.newDerivableContainerSymbol( "a3", ITypeInfo.t_struct ); //$NON-NLS-1$
		A2.addSymbol( a3 );
		
		ISymbol a3_int = table.newSymbol( "a3", ITypeInfo.t_int ); //$NON-NLS-1$
		A2.addSymbol( a3_int );
		
		IParameterizedSymbol aF = table.newParameterizedSymbol( "aF", ITypeInfo.t_function ); //$NON-NLS-1$
		A2.addSymbol( aF );
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		A2.addSymbol( f );
		
		ISymbol aLocal = table.newSymbol( "aLocal", ITypeInfo.t_int ); //$NON-NLS-1$
		f.addSymbol( aLocal );
		
		List results = f.prefixLookup( new TypeFilter( LookupKind.STRUCTURES ), "A", false, null ); //$NON-NLS-1$
		
		assertEquals( results.size(), 3 );
		
		assertTrue( results.contains( a1 ) );
		assertTrue( results.contains( A2 ) );
		assertTrue( results.contains( a3 ) );
		
		results = f.prefixLookup( null, "a", false, null ); //$NON-NLS-1$
		assertEquals( results.size(), 7 );
		assertTrue( results.contains( aF ) );
		assertTrue( results.contains( A2 ) );
		assertTrue( results.contains( a3_int ) );
		assertTrue( results.contains( a1 ) );
		assertTrue( results.contains( aFoo ) );
		assertTrue( results.contains( aa ) );
		assertTrue( results.contains( aLocal ) );
		
		results = f.prefixLookup( new TypeFilter( LookupKind.FUNCTIONS ), "a", false, null ); //$NON-NLS-1$
		assertEquals( results.size(), 1 );
		assertTrue( results.contains( aFoo ) );
		
		results = f.prefixLookup( new TypeFilter( LookupKind.METHODS ), "a", false, null ); //$NON-NLS-1$
		assertEquals( results.size(), 1 );
		assertTrue( results.contains( aF ) );
		
		results = f.prefixLookup( new TypeFilter( LookupKind.LOCAL_VARIABLES ), "a", false, null ); //$NON-NLS-1$
		assertEquals( results.size(), 1 );
		assertTrue( results.contains( aLocal ) );
		
		results = f.prefixLookup( new TypeFilter( LookupKind.VARIABLES ), "a", false, null ); //$NON-NLS-1$
		assertEquals( results.size(), 1 );
		assertTrue( results.contains( aa ) );
		
		results = f.prefixLookup( new TypeFilter( LookupKind.FIELDS), "a", false, null ); //$NON-NLS-1$
		assertEquals( results.size(), 1 );
		assertTrue( results.contains( a3_int ) );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * void foo( ... ){ }
	 * 
	 * foo( 1 );
	 *  
	 * @throws Exception
	 */
	public void testBug43110_Ellipses() throws Exception{
		newTable();
		
		IParameterizedSymbol foo = table.newParameterizedSymbol( "foo", ITypeInfo.t_function ); //$NON-NLS-1$
		foo.setHasVariableArgs( true );
		
		table.getCompilationUnit().addSymbol( foo );
		
		List params = new ArrayList();
		
		ITypeInfo p1 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null );
		params.add( p1 );
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "foo", params ); //$NON-NLS-1$
		
		assertEquals( foo, look );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * void foo( ... )   {}; //#1
	 * void foo( int i ) {}; //#2
	 * 
	 * foo( 1 );  // calls foo #2
	 * @throws Exception
	 */
	public void testBug43110_EllipsesRanking() throws Exception{
		newTable();
		
		IParameterizedSymbol foo1 = table.newParameterizedSymbol( "foo", ITypeInfo.t_function ); //$NON-NLS-1$
		foo1.setHasVariableArgs( true );
		
		table.getCompilationUnit().addSymbol( foo1 );
		
		IParameterizedSymbol foo2 = table.newParameterizedSymbol( "foo", ITypeInfo.t_function ); //$NON-NLS-1$
		foo2.addParameter( ITypeInfo.t_int, 0, null, false );
		table.getCompilationUnit().addSymbol( foo2 );
		
		List params = new ArrayList();
		
		ITypeInfo p1 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null );
		params.add( p1 );
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "foo", params ); //$NON-NLS-1$
		
		assertEquals( foo2, look );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * void foo( int i = 0 ) {};  //#1
	 * void foo( ... ) {};        //#2
	 * 
	 * foo(); //calls #1
	 * @throws Exception
	 */
	public void testBug43110_ElipsesRanking_2() throws Exception{
		newTable();
		
		IParameterizedSymbol foo1 = table.newParameterizedSymbol( "foo", ITypeInfo.t_function ); //$NON-NLS-1$
		foo1.addParameter( ITypeInfo.t_int, 0, null, true );
		table.getCompilationUnit().addSymbol( foo1 );
		
		IParameterizedSymbol foo2 = table.newParameterizedSymbol( "foo", ITypeInfo.t_function ); //$NON-NLS-1$
		foo2.setHasVariableArgs( true );
		table.getCompilationUnit().addSymbol( foo2 );
		
		List params = new ArrayList();
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "foo", params ); //$NON-NLS-1$
		
		assertEquals( foo1, look );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * int global;
	 * class A {
	 *    A();
	 *    int var;
	 *    void foo();
	 * };
	 *
	 */
	public void testIterator_1() throws Exception{
		newTable();
		
		ISymbol global = table.newSymbol( "global", ITypeInfo.t_int ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( global );
		
		IDerivableContainerSymbol cls = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		
		table.getCompilationUnit().addSymbol( cls );
		
		IParameterizedSymbol constructor = table.newParameterizedSymbol( "A", ITypeInfo.t_constructor ); //$NON-NLS-1$
		cls.addConstructor( constructor );
		
		ISymbol var = table.newSymbol( "var", ITypeInfo.t_int ); //$NON-NLS-1$
		cls.addSymbol( var );
		
		IParameterizedSymbol foo = table.newParameterizedSymbol( "foo", ITypeInfo.t_function ); //$NON-NLS-1$
		cls.addSymbol( foo );
		
		
		Iterator iter = table.getCompilationUnit().getContentsIterator();
		assertEquals( iter.next(), global );
		IContainerSymbol symbol = (IContainerSymbol) iter.next();
		assertEquals( symbol, cls );
		assertFalse( iter.hasNext() );
		
		iter = symbol.getContentsIterator();
		assertEquals( iter.next(), constructor );
		assertEquals( iter.next(), var );
		assertEquals( iter.next(), foo );
		assertFalse( iter.hasNext() );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * int foo();
	 * namespace A{
	 *    int bar();
	 *    int bar( int );
	 * };
	 * class B{
	 *    void func(){ 
	 *       using namespace A;
	 *    }
	 * };
	 * @throws Exception
	 */
	public void testIterator_2() throws Exception{
		newTable();
		
		IParameterizedSymbol foo = table.newParameterizedSymbol( "foo", ITypeInfo.t_function ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( foo );
		
		IContainerSymbol nsA = table.newContainerSymbol( "A", ITypeInfo.t_namespace ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( nsA );
		
		IParameterizedSymbol bar1 = table.newParameterizedSymbol( "bar", ITypeInfo.t_function ); //$NON-NLS-1$
		nsA.addSymbol( bar1 );
		
		IParameterizedSymbol bar2 = table.newParameterizedSymbol( "bar", ITypeInfo.t_function ); //$NON-NLS-1$
		bar2.addParameter( ITypeInfo.t_int, 0, null, false );
		nsA.addSymbol( bar2 );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol("B", ITypeInfo.t_class); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( B );
		
		B.addCopyConstructor();
		
		IParameterizedSymbol func = table.newParameterizedSymbol( "func", ITypeInfo.t_function ); //$NON-NLS-1$
		B.addSymbol( func );
		
		IUsingDirectiveSymbol using = func.addUsingDirective( nsA );
		
		Iterator iter = table.getCompilationUnit().getContentsIterator();
		
		assertEquals( iter.next(), foo );
		
		IContainerSymbol s1 = (IContainerSymbol) iter.next();
		assertEquals( s1, nsA );
		
		IContainerSymbol s2 = (IContainerSymbol) iter.next();
		assertEquals( s2, B );
		
		assertFalse( iter.hasNext() );
		
		iter = s1.getContentsIterator();
		assertEquals( iter.next(), bar1 );
		assertEquals( iter.next(), bar2 );
		assertFalse( iter.hasNext() );
		
		iter = s2.getContentsIterator();
		
		//Copy constructor!!
		ISymbol copy = (ISymbol) iter.next();
		assertTrue( copy instanceof IParameterizedSymbol );
		assertEquals( copy.getName(), "B" ); //$NON-NLS-1$
		assertEquals( copy.getType(), ITypeInfo.t_constructor );
		
		assertEquals( iter.next(), func );
		assertFalse( iter.hasNext() );
		
		iter = func.getContentsIterator();
		//this pointer!!
		ISymbol th = (ISymbol) iter.next();
		assertEquals( th.getName(), "this" ); //$NON-NLS-1$
		assertEquals( th.getTypeSymbol(), B );
		
		assertEquals( iter.next(), using );
		
		assertFalse( iter.hasNext() );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * void f( long long int ){}   //#1
	 * void f( long int ) {}       //#2
	 *  
	 * f( 1L );    //#2   
	 * f( 1LL );   //#1
	 * 
	 * @throws Exception
	 */
	public void testLongLong() throws Exception{
		newTable();
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f1.addParameter( ITypeInfo.t_int, ITypeInfo.isLongLong, null, false );
		table.getCompilationUnit().addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f2.addParameter( ITypeInfo.t_int, ITypeInfo.isLong, null, false );
		table.getCompilationUnit().addSymbol( f2 );
		
		List params = new ArrayList();
		params.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, ITypeInfo.isLong, null ) );
		
		IParameterizedSymbol lookup = table.getCompilationUnit().unqualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
		assertEquals( lookup, f2 );
		
		params.clear();
		params.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, ITypeInfo.isLongLong, null ) );
		lookup = table.getCompilationUnit().unqualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
		assertEquals( lookup, f1 );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * void f( float _Complex ){}
	 * void g( float ) {}
	 * 
	 * float _Complex c;
	 * float fl;
	 * float _Imaginary i;
	 * 
	 * f( c );
	 * f( fl );
	 * g( c );
	 * g( i );
	 * @throws Exception
	 */
	public void testComplex() throws Exception{
		newTable();
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f.addParameter( ITypeInfo.t_float, ITypeInfo.isComplex, null, false );
		
		table.getCompilationUnit().addSymbol( f );
		
		IParameterizedSymbol g = table.newParameterizedSymbol( "g", ITypeInfo.t_function ); //$NON-NLS-1$
		g.addParameter( ITypeInfo.t_float, 0, null, false );
		table.getCompilationUnit().addSymbol( g );
		
		List params = new ArrayList();
		params.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_float, ITypeInfo.isComplex, null ) );
		
		IParameterizedSymbol lookup = table.getCompilationUnit().unqualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
		
		assertEquals( lookup, f );
		
		params.clear();
		params.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_float, 0, null ) );
		lookup = table.getCompilationUnit().unqualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
		assertEquals( lookup, f );
		
		params.clear();
		params.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_float, ITypeInfo.isComplex, null ) );
		lookup = table.getCompilationUnit().unqualifiedFunctionLookup( "g", params ); //$NON-NLS-1$
		assertEquals( lookup, g );

		params.clear();
		params.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_float, ITypeInfo.isImaginary, null ) );
		lookup = table.getCompilationUnit().unqualifiedFunctionLookup( "g", params ); //$NON-NLS-1$
		assertEquals( lookup, g );		
		
		lookup = table.getCompilationUnit().unqualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
		assertEquals( lookup, f );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	public void test_Bool() throws Exception{
		newTable();
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f.addParameter( ITypeInfo.t__Bool, 0, null, false );
		
		table.getCompilationUnit().addSymbol( f );
		
		IParameterizedSymbol g = table.newParameterizedSymbol( "g", ITypeInfo.t_function ); //$NON-NLS-1$
		g.addParameter( ITypeInfo.t_int, 0, null, false );
		
		table.getCompilationUnit().addSymbol( g );
		
		List params = new ArrayList();
		params.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t__Bool, 0, null ) );
		
		IParameterizedSymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
		assertEquals( look, f );
		
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "g", params ); //$NON-NLS-1$
		assertEquals( look, g );
		
		params.clear();
		params.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
		assertEquals( look, f );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * typedef int Int;
	 * void f( int i );
	 * void f( Int i );
	 * 
	 * 
	 * @throws Exception
	 */
	public void testBug47636FunctionParameterComparisons_1() throws Exception{
		newTable();
		
		ISymbol Int = table.newSymbol( "Int", ITypeInfo.t_type ); //$NON-NLS-1$
		Int.getTypeInfo().setBit( true, ITypeInfo.isTypedef );
		Int.setTypeSymbol( table.newSymbol( ParserSymbolTable.EMPTY_NAME, ITypeInfo.t_int ) );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f1.addParameter( ITypeInfo.t_int, 0, null, false );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f2.addParameter( Int, 0, null, false );
		
		assertTrue( f1.hasSameParameters( f2 ) );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}

	/**
	* void g( char * );
	* void g( char [] );
	*/
	public void testBug47636FunctionParameterComparisons_2() throws Exception{
		newTable();
		
		IParameterizedSymbol g1 = table.newParameterizedSymbol( "g", ITypeInfo.t_function ); //$NON-NLS-1$
		g1.addParameter( ITypeInfo.t_char, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		
		IParameterizedSymbol g2 = table.newParameterizedSymbol( "g", ITypeInfo.t_function ); //$NON-NLS-1$
		g2.addParameter( ITypeInfo.t_char, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_array ), false );
		
		assertTrue( g1.hasSameParameters( g2 ) );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * void h( int() );
	 * void h( int (*) () );
	 */
	public void testBug47636FunctionParameterComparisons_3() throws Exception{
		newTable();
		
		IParameterizedSymbol f = table.newParameterizedSymbol( ParserSymbolTable.EMPTY_NAME, ITypeInfo.t_function );
		f.setReturnType( table.newSymbol( ParserSymbolTable.EMPTY_NAME, ITypeInfo.t_int ) );
		
		IParameterizedSymbol h1 = table.newParameterizedSymbol( "h", ITypeInfo.t_function ); //$NON-NLS-1$
		h1.addParameter( f, 0, null, false );
		
		IParameterizedSymbol h2 = table.newParameterizedSymbol( "h", ITypeInfo.t_function ); //$NON-NLS-1$
		h2.addParameter( f, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		
		assertTrue( h1.hasSameParameters( h2 ) );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
	/**
	 * f( int );
	 * f( const int );
	 */
	public void testBug47636FunctionParameterComparisons_4() throws Exception{
		newTable();

		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f1.addParameter( ITypeInfo.t_int, 0, null, false );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f2.addParameter( ITypeInfo.t_int, ITypeInfo.isConst, null, false );
		
		assertTrue( f1.hasSameParameters( f2 ) );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	}
	
//	public void testBug52111RemoveSymbol() throws Exception{
//		newTable();
//		
//		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
//		table.getCompilationUnit().addSymbol( A );
//		
//		ISymbol i = table.newSymbol( "i", ITypeInfo.t_int ); //$NON-NLS-1$
//		A.addSymbol( i );
//		
//		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
//		A.addSymbol( f1 );
//		
//		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
//		f2.addParameter( ITypeInfo.t_int, 0, null, false );
//		
//		A.addSymbol( f2 );
//		
//		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", ITypeInfo.t_class ); //$NON-NLS-1$
//		B.addParent( A );
//		
//		table.getCompilationUnit().addSymbol( B );
//		
//		ISymbol look = B.qualifiedLookup( "i" ); //$NON-NLS-1$
//		assertEquals( look, i );
//		
//		Iterator iter = A.getContentsIterator();
//		assertEquals( iter.next(), i );
//		assertEquals( iter.next(), f1 );
//		assertEquals( iter.next(), f2 );
//		assertFalse( iter.hasNext() );
//		
//		assertTrue( A.removeSymbol( i ) );
//		
//		iter = A.getContentsIterator();
//		assertEquals( iter.next(), f1 );
//		assertEquals( iter.next(), f2 );
//		assertFalse( iter.hasNext() );
//		
//		look = B.qualifiedLookup( "i" ); //$NON-NLS-1$
//		assertNull( look );
//		
//		List params = new ArrayList();
//		
//		look = B.qualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
//		assertEquals( look, f1 );
//		
//		assertTrue( A.removeSymbol( f1 ) );
//		iter = A.getContentsIterator();
//		assertEquals( iter.next(), f2 );
//		assertFalse( iter.hasNext() );
//		
//		look = B.qualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
//		assertNull( look );
//		
//		params.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
//		look = B.qualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
//		
//		assertEquals( look, f2 );
//		assertTrue( A.removeSymbol( f2 ) );
//		
//		iter = A.getContentsIterator();
//		assertFalse( iter.hasNext() );
//		
//		look = B.qualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
//		assertNull( look );
//		
//		assertEquals( A.getContainedSymbols().size(), 0 );
//		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
//	}
}

