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
		enum.setType( Declaration.t_enum );
		
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
}