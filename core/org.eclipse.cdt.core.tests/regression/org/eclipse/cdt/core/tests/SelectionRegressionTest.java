/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Nov 1, 2004
 */
package org.eclipse.cdt.core.tests;

import java.io.StringWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.IParser.ISelectionParseResult;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.tests.CompleteParseBaseTest.FullParseCallback;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.core.browser.cache.TypeCacheManager;
import org.eclipse.cdt.internal.core.index.domsourceindexer.DOMSourceIndexer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author aniefer
 */
public class SelectionRegressionTest extends BaseTestFramework {

    public SelectionRegressionTest()
    {
        super();
    }
    /**
     * @param name
     */
    public SelectionRegressionTest(String name)
    {
        super(name);
    }
    
    public static Test suite(){
        return suite( true );
    }
    public static Test suite( boolean cleanup ) {
        TestSuite suite = new TestSuite("SelectionRegressionTests"); //$NON-NLS-1$
        suite.addTest( new SelectionRegressionTest("testSimpleOpenDeclaration") ); //$NON-NLS-1$
        suite.addTest( new SelectionRegressionTest( "testClass" ) ); //$NON-NLS-1$
        suite.addTest( new SelectionRegressionTest( "testClassRHS" ) ); //$NON-NLS-1$
    	suite.addTest( new SelectionRegressionTest( "testStruct" ) ); //$NON-NLS-1$
    	suite.addTest( new SelectionRegressionTest( "testStructRHS" ) ); //$NON-NLS-1$
    	suite.addTest( new SelectionRegressionTest( "testEnumeration" ) ); //$NON-NLS-1$
    	suite.addTest( new SelectionRegressionTest( "testEnumerationArg" ) ); //$NON-NLS-1$
    	suite.addTest( new SelectionRegressionTest( "testEnumerator" ) ); //$NON-NLS-1$
    	suite.addTest( new SelectionRegressionTest( "testMethod" ) ); //$NON-NLS-1$
    	suite.addTest( new FailingTest( new SelectionRegressionTest( "testMethodRHS78656" ), 78656 )); //$NON-NLS-1$
    	suite.addTest( new FailingTest( new SelectionRegressionTest( "testMethod78114" ), 78114 )); //$NON-NLS-1$
    	suite.addTest( new FailingTest( new SelectionRegressionTest( "testMethod78118" ), 78118 )); //$NON-NLS-1$
    	suite.addTest( new FailingTest( new SelectionRegressionTest( "testOverloadedMethod78389" ), 78389 )); //$NON-NLS-1$
    	suite.addTest( new FailingTest( new SelectionRegressionTest( "testConstructor78625" ), 78625) ); //$NON-NLS-1$
    	suite.addTest( new SelectionRegressionTest( "testClassField" ) ); //$NON-NLS-1$
    	suite.addTest( new SelectionRegressionTest( "testStructField" ) ); //$NON-NLS-1$
    	suite.addTest( new SelectionRegressionTest( "testNamespace" ) ); //$NON-NLS-1$
    	suite.addTest( new SelectionRegressionTest( "testNamespace77989" ) ); //$NON-NLS-1$
    	suite.addTest( new SelectionRegressionTest( "testFunction" ) ); //$NON-NLS-1$
    	suite.addTest( new SelectionRegressionTest( "testFunctionArg" ) ); //$NON-NLS-1$
    	suite.addTest( new FailingTest(new SelectionRegressionTest( "testFunctionArg78435" ), 78435 )); //$NON-NLS-1$
    	suite.addTest( new SelectionRegressionTest( "testVariable" ) ); //$NON-NLS-1$
    	suite.addTest( new SelectionRegressionTest( "testVariableStruct" ) ); //$NON-NLS-1$
    	suite.addTest( new SelectionRegressionTest( "testVariableArg" ) ); //$NON-NLS-1$
    	suite.addTest( new FailingTest (new SelectionRegressionTest( "testVariableArg77996" ), 77996 )); //$NON-NLS-1$
    	suite.addTest( new FailingTest (new SelectionRegressionTest( "testVariable77996" ), 77996) ); //$NON-NLS-1$
    	suite.addTest( new SelectionRegressionTest( "testUnion" ) ); //$NON-NLS-1$
          
        if( cleanup )
            suite.addTest( new SelectionRegressionTest( "cleanupProject" ) ); //$NON-NLS-1$
        
	    return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
		try{
			if (project == null){
			   cproject = CProjectHelper.createCCProject("RegressionTestProject", "bin"); //$NON-NLS-1$ //$NON-NLS-2$
			   project = cproject.getProject();
			}
		} catch ( CoreException e ) { //boo
		}
		TypeCacheManager typeCacheManager = TypeCacheManager.getInstance();
		typeCacheManager.setProcessTypeCacheEvents(false);
    }
    
    protected void tearDown() throws Exception {
        if( project == null || !project.exists() ) 
            return;
    
		try{
		    project.setSessionProperty( DOMSourceIndexer.activationKey, new Boolean( false ) );
			project.delete(true,true,new NullProgressMonitor());
			project = null;
		} catch ( CoreException e ) { //boo
		}
        super.tearDown();
	}
	
    protected IASTNode getSelection(IFile code, int startOffset, int endOffset) throws Exception {
		return getSelection( code, startOffset, endOffset, ParserLanguage.CPP );
	}

	/**
	 * @param code
	 * @param offset1
	 * @param offset2
	 * @param b
	 * @return
	 */
	protected IASTNode getSelection(IFile file, int startOffset, int endOffset, ParserLanguage language ) throws Exception {
	    FullParseCallback callback = new FullParseCallback();
		IParser parser = ParserFactory.createParser(
							ParserFactory.createScanner(
									new CodeReader( file.getLocation().toOSString(), file.getContents() ),
									new ScannerInfo(),
									ParserMode.SELECTION_PARSE,
									ParserLanguage.CPP,
									callback,
									new NullLogService(), null),
							callback,
							ParserMode.SELECTION_PARSE,
							ParserLanguage.CPP,
							ParserFactory.createDefaultLogService()
						);
		
		ISelectionParseResult result = parser.parse( startOffset, endOffset );

		return (IASTNode) ( (result != null) ? result.getOffsetableNamedElement() : null );
	}
	
	protected void assertNodeLocation( IASTNode result, IFile file, int offset ) throws Exception {
	    if( result != null && result instanceof IASTOffsetableNamedElement ){
	        IASTOffsetableNamedElement el = (IASTOffsetableNamedElement) result;
	        assertEquals( file.getLocation().toOSString(), new String( el.getFilename() ) );
	        assertEquals( offset, el.getNameOffset() );
	        return;
	    }
	    fail("Node not found in " + file.getLocation().toOSString() + " offset " + offset);  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	protected void assertNodeNull( IASTNode node ) throws Exception {
	    if (node == null) {
	        return;
	    }
	    if (node instanceof IASTOffsetableNamedElement) {
	    	IASTOffsetableNamedElement el = (IASTOffsetableNamedElement) node;
	    	fail ("node found at " + new String( el.getFilename()) + " offset " + el.getNameOffset() ); //$NON-NLS-1$ //$NON-NLS-2$
	    return;
	    }
	    fail("Node found when none expected."); //$NON-NLS-1$
	}
	public void testSimpleOpenDeclaration() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("class A{};           \n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       \n"); //$NON-NLS-1$
	    writer.write( "void f(){              \n"); //$NON-NLS-1$
	    writer.write( "   A a;                \n"); //$NON-NLS-1$
	    writer.write( "}                      \n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    
	    int start = source.indexOf( "A" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 1 );
	    assertNodeLocation( node, h, header.indexOf("A")); //$NON-NLS-1$
	}
	public void testClass() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("class A{};           		\n"); //$NON-NLS-1$
	    writer.write("namespace N {           	\n"); //$NON-NLS-1$
	    writer.write("  class B {       		\n"); //$NON-NLS-1$
	    writer.write("     class C{};       	\n"); //$NON-NLS-1$
	    writer.write("  };			     		\n"); //$NON-NLS-1$
	    writer.write("}			     			\n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       	\n"); //$NON-NLS-1$
	    writer.write( "A/*vp1*/ a;              \n"); //$NON-NLS-1$
	    writer.write( "N::B/*vp2*/ b;           \n"); //$NON-NLS-1$
	    writer.write( "using namespace N;       \n"); //$NON-NLS-1$
	    writer.write( "B::C/*vp3*/ c;           \n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln global; Seln global
	    int start = source.indexOf( "A/*vp1*/" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 1 );
	    assertNodeLocation( node, h, header.indexOf("A")); //$NON-NLS-1$ 
	    //vp2 Decln in namespace; Seln global, scoped
	    start = source.indexOf( "N::B/*vp2*/" ); //$NON-NLS-1$
	    node = getSelection( cpp, start, start + 4 );
	    assertNodeLocation( node, h, header.indexOf("B")); //$NON-NLS-1$
	    //vp3 Decln in namespace and nested class; Seln in partially scoped ref
	    start = source.indexOf( "C/*vp3*/" ); //$NON-NLS-1$
	    node = getSelection( cpp, start, start + 1 );
	    assertNodeLocation( node, h, header.indexOf("C")); //$NON-NLS-1$
	
	}
	public void testClassRHS() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("class A{           		\n"); //$NON-NLS-1$
	    writer.write("  enum{E0};        		\n"); //$NON-NLS-1$
	    writer.write("};           				\n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       	\n"); //$NON-NLS-1$
	    writer.write( "int a=A::E0;             \n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp 1 Decln global, Seln global, on rhs of assignment
	    int start = source.indexOf( "A" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 1 );
	    assertNodeLocation( node, h, header.indexOf("A")); //$NON-NLS-1$ 
	}
	public void testStruct() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("struct A{};//vp1		    \n"); //$NON-NLS-1$
	    writer.write("class B{           		\n"); //$NON-NLS-1$
	    writer.write("  public: struct C{};     \n"); //$NON-NLS-1$
	    writer.write("};           				\n"); //$NON-NLS-1$
	    writer.write("namespace N{           	\n"); //$NON-NLS-1$
	    writer.write("  struct A{};//vp3		\n"); //$NON-NLS-1$
	    writer.write("  struct D{};           	\n"); //$NON-NLS-1$
	    writer.write("};           				\n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       	\n"); //$NON-NLS-1$
	    writer.write( "void f(){              	\n"); //$NON-NLS-1$
	    writer.write( "   A a;//vp1             \n"); //$NON-NLS-1$
	    writer.write( "   B::C d;//vp2          \n"); //$NON-NLS-1$
	    writer.write( "   N::A e;//vp3          \n"); //$NON-NLS-1$
	    writer.write( "   using namespace N;    \n"); //$NON-NLS-1$
	    writer.write( "   A/*vp4*/ f;           \n"); //$NON-NLS-1$
	    writer.write( "}                      	\n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln global; Seln in function
	    int start = source.indexOf( "A a;//vp1" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 1 );
	    assertNodeLocation( node, h, header.indexOf("A{};//vp1")); //$NON-NLS-1$
	    //vp2 Decln in class; Seln in function, in :: scope
	    start = source.indexOf( "C" ); //$NON-NLS-1$
	    node = getSelection( cpp, start, start + 1 );
	    assertNodeLocation( node, h, header.indexOf("C")); //$NON-NLS-1$ 
	    //vp3 Decln in namespace; Seln in function, fully qualified
	    start = source.indexOf( "N::A" ); //$NON-NLS-1$
	    node = getSelection( cpp, start, start + 4 );
	    assertNodeLocation( node, h, header.indexOf("A{};//vp3")); //$NON-NLS-1$ //$NON-NLS-2$
	    //vp4 Decln ambiguous; Seln in function, unqualified
	    start = source.indexOf( "A/*vp4*/" ); //$NON-NLS-1$
	    node = getSelection( cpp, start, start + 1 );
	    assertNodeNull( node );
	}
	public void testStructRHS() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("struct A{						\n"); //$NON-NLS-1$
	    writer.write(" static const float pi=3.14;  \n"); //$NON-NLS-1$
	    writer.write("};							\n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       \n"); //$NON-NLS-1$
	    writer.write( "void f(){              \n"); //$NON-NLS-1$
	    writer.write( "   float f=A::pi;      \n"); //$NON-NLS-1$
	    writer.write( "}                      \n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln global; Seln in function, rhs of assignment
	    int start = source.indexOf( "A" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 1 );
	    assertNodeLocation( node, h, header.indexOf("A")); //$NON-NLS-1$ 
	}
	public void testEnumeration() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("enum A{};           	\n"); //$NON-NLS-1$
	    writer.write("class B{      		\n"); //$NON-NLS-1$
	    writer.write(" public:		        \n"); //$NON-NLS-1$
	    writer.write(" enum C {enum2};      \n"); //$NON-NLS-1$
	    writer.write(" void boo();			\n"); //$NON-NLS-1$
	    writer.write("};           			\n"); //$NON-NLS-1$
		String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"      \n"); //$NON-NLS-1$
	    writer.write( "void f(){             \n"); //$NON-NLS-1$
	    writer.write( "   A/*vp1*/ a;        \n"); //$NON-NLS-1$
	    writer.write( "   B::C/*vp2*/ c;     \n"); //$NON-NLS-1$
	    writer.write( "}                     \n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln global; Seln in function
	    int start = source.indexOf( "A" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 1 );
	    assertNodeLocation( node, h, header.indexOf("A")); //$NON-NLS-1$ 
	    //vp2 Decln in class; Seln in function, fully qualified
	    start = source.indexOf( "B::C" ); //$NON-NLS-1$
	    node = getSelection( cpp, start, start + 4 );
	    assertNodeLocation( node, h, header.indexOf("C")); //$NON-NLS-1$ 
	}
	public void testEnumerationArg() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("enum A{};              \n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"      \n"); //$NON-NLS-1$
	    writer.write( "void f(A a){};        \n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln global; Seln in argument list of function
	    int start = source.indexOf( "A" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 1 );
	    assertNodeLocation( node, h, header.indexOf("A")); //$NON-NLS-1$ 
	}
	public void testEnumerator() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("enum A{enum1};           \n"); //$NON-NLS-1$
	    writer.write("class B{      		   \n"); //$NON-NLS-1$
	    writer.write(" public:		           \n"); //$NON-NLS-1$
	    writer.write(" enum {enum2};           \n"); //$NON-NLS-1$
	    writer.write(" enum {enum3} f1;        \n"); //$NON-NLS-1$
	    writer.write(" void boo();			   \n"); //$NON-NLS-1$
	    writer.write("};           			   \n"); //$NON-NLS-1$
		String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"        \n"); //$NON-NLS-1$
	    writer.write( "void f(){               \n"); //$NON-NLS-1$
	    writer.write( "   A a;                 \n"); //$NON-NLS-1$
	    writer.write( "   a=enum1;//vp1        \n"); //$NON-NLS-1$
	    writer.write( "   int i=B::enum2;//vp2 \n"); //$NON-NLS-1$
	    writer.write( "}                   	   \n"); //$NON-NLS-1$
	    writer.write( "void B::boo() {         \n"); //$NON-NLS-1$
	    writer.write( "   f1=enum3;//vp3       \n"); //$NON-NLS-1$
	    writer.write( "}                       \n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln global; Seln in function, on rhs of assignment
	    int start = source.indexOf( "enum1" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 5 );
	    assertNodeLocation( node, h, header.indexOf("enum1")); //$NON-NLS-1$ 
	    //vp2 Decln in class, in anon enumeration; Seln in function, on rhs of assignment, in :: scope
	    start = source.indexOf( "enum2" ); //$NON-NLS-1$
	    node = getSelection( cpp, start, start + 5 );
	    assertNodeLocation( node, h, header.indexOf("enum2")); //$NON-NLS-1$ 
	    //vp3 Decln in class, in anon enumeration with field; Seln in method, on rhs of assignment
	    start = source.indexOf( "enum3" ); //$NON-NLS-1$
	    node = getSelection( cpp, start, start + 5 );
	    assertNodeLocation( node, h, header.indexOf("enum3")); //$NON-NLS-1$ 
	}
	public void testMethod() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("class A{           			\n"); //$NON-NLS-1$
	    writer.write("int method1(){}           	\n"); //$NON-NLS-1$
	    writer.write("static const int method2();   \n"); //$NON-NLS-1$
	    writer.write("}           					\n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       		\n"); //$NON-NLS-1$
	    writer.write( "void f(){              		\n"); //$NON-NLS-1$
	    writer.write( "   A a;                		\n"); //$NON-NLS-1$
	    writer.write( "   a.method1();//vp1        	\n"); //$NON-NLS-1$
	    writer.write( "   A *b=new A();       		\n"); //$NON-NLS-1$
	    writer.write( "   b->method1();//vp2       	\n"); //$NON-NLS-1$
	    writer.write( "   A::method2();//vp3       	\n"); //$NON-NLS-1$
		writer.write( "}                       		\n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln private; Seln in function, in dot reference
	    int start = source.indexOf( "method1();//vp1" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 7 );
	    assertNodeLocation( node, h, header.indexOf("method1")); //$NON-NLS-1$ 
	    //vp2 Decln private; Seln in function, in arrow reference
	    start = source.indexOf( "method1();//vp2" ); //$NON-NLS-1$
	    node = getSelection( cpp, start, start + 7 );
	    assertNodeLocation( node, h, header.indexOf("method1")); //$NON-NLS-1$ 
	    //vp3 Decln private; Seln in function, in scope reference
	    start = source.indexOf( "method2();//vp3" ); //$NON-NLS-1$
	    node = getSelection( cpp, start, start + 7 );
	    assertNodeLocation( node, h, header.indexOf("method2")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testMethodRHS78656() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("class A{           			\n"); //$NON-NLS-1$
	    writer.write("int method1(){}           		\n"); //$NON-NLS-1$
	    writer.write("}           					\n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       		\n"); //$NON-NLS-1$
	    writer.write( "void f(){              		\n"); //$NON-NLS-1$
	    writer.write( "   A a;                		\n"); //$NON-NLS-1$
	    writer.write( "   int i=a.method1();//vp1        	\n"); //$NON-NLS-1$
	    writer.write( "}                       		\n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln private; Seln in dot reference, on RHS of assignment
	    //defect is node not found
	    int start = source.indexOf( "method1();//vp1" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 7 );
	    assertNodeLocation( node, h, header.indexOf("method1")); //$NON-NLS-1$ 
	}
	public void testMethod78114() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("class Point{           	\n"); //$NON-NLS-1$
	    writer.write(" public:            		\n"); //$NON-NLS-1$
	    writer.write(" Point(): xCoord(0){}     \n"); //$NON-NLS-1$
	    writer.write(" private: int xCoord;     \n"); //$NON-NLS-1$
	    writer.write("}							\n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       			\n"); //$NON-NLS-1$
	    writer.write( "void f(){              			\n"); //$NON-NLS-1$
	    writer.write( "   Point &p2 = *(new Point());   \n"); //$NON-NLS-1$
	    writer.write( "}                       			\n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln public; Seln on rhs in code scope
	    //defect is class is found rather than constructor
	    int start = source.indexOf( "Point()" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 5 );
	    assertNodeLocation( node, h, header.indexOf("Point()")); //$NON-NLS-1$ 
	}
	public void testMethod78118() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("class Point{           			\n"); //$NON-NLS-1$
	    writer.write(" public:            				\n"); //$NON-NLS-1$
	    writer.write(" Point(): xCoord(0){}           	\n"); //$NON-NLS-1$
	    writer.write(" Point& operator=(const Point &rhs){return *this};  \n"); //$NON-NLS-1$
	    writer.write("};								\n"); //$NON-NLS-1$
	     String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       			\n"); //$NON-NLS-1$
	    writer.write( "void f(){              			\n"); //$NON-NLS-1$
	    writer.write( "   Point a;                		\n"); //$NON-NLS-1$
	    writer.write( "   const Point zero;        		\n"); //$NON-NLS-1$
	    writer.write( "   a.operator=(zero);//vp1       \n"); //$NON-NLS-1$
	    writer.write( "}                       			\n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln in class, public; Seln in function, in dot reference
	    //defect is npe; parser field, greaterContextDuple is null
	    int start = source.indexOf( "operator=" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 9 );
	    assertNodeLocation( node, h, header.indexOf("operator=")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testOverloadedMethod78389() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("class Point{           			\n"); //$NON-NLS-1$
	    writer.write(" public:            				\n"); //$NON-NLS-1$
	    writer.write(" void method1(){}         		\n"); //$NON-NLS-1$
	    writer.write(" void method1(int i){}          	\n"); //$NON-NLS-1$
	    writer.write("};								\n"); //$NON-NLS-1$
	     String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       			\n"); //$NON-NLS-1$
	    writer.write( "void f(){              			\n"); //$NON-NLS-1$
	    writer.write( "   Point a;                		\n"); //$NON-NLS-1$
	    writer.write( "   a.method1(3);        			\n"); //$NON-NLS-1$
	    writer.write( "}                       			\n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln public; Seln in function, in dot reference
	    //defect is operation unavailable on current selection
	    int start = source.indexOf( "method1" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 7 );
	    assertNodeLocation( node, h, header.indexOf("method1(int i)")); //$NON-NLS-1$ //$NON-NLS-2$
	    
	}
	public void testConstructor78625() throws Exception{
		Writer writer = new StringWriter();
		writer.write("class Mammal {						\n" ); 	//$NON-NLS-1$
		writer.write("	public:								\n" ); 	//$NON-NLS-1$
		writer.write("	Mammal(bool b): isCarnivore(b){}	\n" ); //$NON-NLS-1$
		writer.write("	private:							\n" ); 	//$NON-NLS-1$
		writer.write("	bool isCarnivore;					\n" ); 	//$NON-NLS-1$
		writer.write("};									\n" ); 	//$NON-NLS-1$
		writer.write("class Bear : Mammal{					\n" ); 	//$NON-NLS-1$
		writer.write("public:								\n" ); 		//$NON-NLS-1$
		writer.write("	Bear(int s): Mammal(true){}//vp1 	\n" ); //$NON-NLS-1$
		writer.write("};									\n" ); 	//$NON-NLS-1$
		String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln public; Seln in initializer list of derived class cctor
	    int start = source.indexOf( "Mammal(true)" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 6 );
	    assertNodeLocation( node, cpp, source.indexOf("Mammal(bool b)")); //$NON-NLS-1$
								 
	}
   
	public void testClassField() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("class A{;          		\n"); //$NON-NLS-1$
	    writer.write("int bee;           		\n"); //$NON-NLS-1$
	    writer.write("};         				\n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       	\n"); //$NON-NLS-1$
	    writer.write( "void f(){              	\n"); //$NON-NLS-1$
	    writer.write( "   A *a=new A();         \n"); //$NON-NLS-1$
	    writer.write( "   a->bee;//vp1          \n"); //$NON-NLS-1$
	    writer.write( "   A b;                	\n"); //$NON-NLS-1$
	    writer.write( "   b.bee;//vp2           \n"); //$NON-NLS-1$
	    writer.write( "}                      	\n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln in class; Seln in function, in arrow reference
	    int start = source.indexOf( "bee;//vp1" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 3 );
	    assertNodeLocation( node, h, header.indexOf("bee")); //$NON-NLS-1$
	    //vp2 Decln in class; Seln in function, in dot reference
	    start = source.indexOf( "bee;//vp2" ); //$NON-NLS-1$
	    node = getSelection( cpp, start, start + 3 );
	    assertNodeLocation( node, h, header.indexOf("bee")); //$NON-NLS-1$ 
	}
	public void testStructField() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("namespace N{           	\n"); //$NON-NLS-1$
	    writer.write("struct A{          		\n"); //$NON-NLS-1$
	    writer.write("int bee;           		\n"); //$NON-NLS-1$
	    writer.write("};         				\n"); //$NON-NLS-1$
	    writer.write("}         				\n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       	\n"); //$NON-NLS-1$
	    writer.write( "void f(){              	\n"); //$NON-NLS-1$
	    writer.write( "   N::A *a;              \n"); //$NON-NLS-1$
	    writer.write( "   a->bee;//vp1          \n"); //$NON-NLS-1$
	    writer.write( "   using namespace N;    \n"); //$NON-NLS-1$
	    writer.write( "   A b;                	\n"); //$NON-NLS-1$
	    writer.write( "   b.bee;//vp2           \n"); //$NON-NLS-1$
	    writer.write( "}                      	\n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln in struct in namespace; Seln in function, in arrow reference
	    int start = source.indexOf( "bee;//vp1" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 3 );
	    assertNodeLocation( node, h, header.indexOf("bee")); //$NON-NLS-1$ //$NON-NLS-2$
	    //vp2 Decln in struct in namespace; Seln in function, in dot reference
	    start = source.indexOf( "bee;//vp2" ); //$NON-NLS-1$
	    node = getSelection( cpp, start, start + 3 );
	    assertNodeLocation( node, h, header.indexOf("bee")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testNamespace() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("namespace N{           	\n"); //$NON-NLS-1$
	    writer.write("class A{};           		\n"); //$NON-NLS-1$
	    writer.write("}           				\n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       	\n"); //$NON-NLS-1$
	    writer.write( "void f(){              	\n"); //$NON-NLS-1$
	    writer.write( "   N::A a;               \n"); //$NON-NLS-1$
	    writer.write( "}                      	\n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln global; Seln in function
	    int start = source.indexOf( "N::A a;" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 1 );
	    assertNodeLocation( node, h, header.indexOf("N")); //$NON-NLS-1$ //$NON-NLS-2$
	
	}
	public void testNamespace77989() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("namespace N{           	\n"); //$NON-NLS-1$
	    writer.write("class A{};           		\n"); //$NON-NLS-1$
	    writer.write("}           				\n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       	\n"); //$NON-NLS-1$
	    writer.write( "void f(){              	\n"); //$NON-NLS-1$
	    writer.write( "   using namespace N;//vp1  \n"); //$NON-NLS-1$
	    writer.write( "}                      	\n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln global; Seln in function, in using statement
	    //defect is decln found at the selection, not in the header
	    int start = source.indexOf( "N;//vp1" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 1 );
	    assertNodeLocation( node, h, header.indexOf("N")); //$NON-NLS-1$ 
	    
	}
	public void testFunction() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("namespace N{           	\n"); //$NON-NLS-1$
	    writer.write("char *foo(){}          	\n"); //$NON-NLS-1$
	    writer.write("}          				\n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       	\n"); //$NON-NLS-1$
	    writer.write( "void f(){              	\n"); //$NON-NLS-1$
	    writer.write( "   N::foo();//vp1        \n"); //$NON-NLS-1$
	    writer.write( "   char* x = N::foo();//vp2  \n"); //$NON-NLS-1$
	    writer.write( "}                      	\n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln in namespace; Seln in function, in scope reference
	    int start = source.indexOf( "foo();//vp1" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 3 );
	    assertNodeLocation( node, h, header.indexOf("foo")); //$NON-NLS-1$ 
	    //vp2 Decln in namespace; Seln in function, scoped, on rhs of assignment
	    start = source.indexOf( "N::foo();//vp2" ); //$NON-NLS-1$
	    node = getSelection( cpp, start, start + 6 );
	    assertNodeLocation( node, h, header.indexOf("foo")); //$NON-NLS-1$ 
	}
	public void testFunctionArg() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("double f(double){return 2.0;};          \n"); //$NON-NLS-1$
	    writer.write("double g(double){return 2.0;};          \n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       \n"); //$NON-NLS-1$
	    writer.write( "double sum_sq(double (*foo)(double), double d){}              \n"); //$NON-NLS-1$
	    writer.write( "void hi() {double x = sum_sq(g,g(3.2));}              \n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln global; Seln in function, in actual parameter list of caller
	    int start = source.indexOf( "g,g(3.2)" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 1 );
	    assertNodeLocation( node, h, header.indexOf("g")); //$NON-NLS-1$ 
	    //vp2 Decln global; Seln in function, in actual parameter list of caller
	    start = source.indexOf( "g(3.2)" ); //$NON-NLS-1$
	    node = getSelection( cpp, start, start + 1 );
	    assertNodeLocation( node, h, header.indexOf("g")); //$NON-NLS-1$ 
	}
	public void testFunctionArg78435() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write( "double sum_sq(double (*foo)/*vp1*/(double), double d){}    \n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln is inside formal parameter list; Seln is declaration
	    //defect is that operation is unavailable on current selection
	    int start = source.indexOf( "foo" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 3 );
	    assertNodeLocation( node, cpp, source.indexOf("foo")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	

	public void testVariable() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("char* a_pc=\"hello\";           	\n"); //$NON-NLS-1$
	    writer.write("union B{int x; char y;} b_u;      \n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       			\n"); //$NON-NLS-1$
	    writer.write( "void f(){              			\n"); //$NON-NLS-1$
	    writer.write( "   int s=(sizeof(a_pc));//vp1    \n"); //$NON-NLS-1$
	    writer.write( "   b_u=3;                		\n"); //$NON-NLS-1$
	    writer.write( "}                      			\n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln global, type char*; Seln in function, on rhs, in actual parameter list of caller
	    int start = source.indexOf( "a_pc" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 4 );
	    assertNodeLocation( node, h, header.indexOf("a_pc")); //$NON-NLS-1$ 
	    //vp2 Decln global, type union; Seln in function
	    start = source.indexOf( "b_u" ); //$NON-NLS-1$
	    node = getSelection( cpp, start, start + 3 );
	    assertNodeLocation( node, h, header.indexOf("b_u")); //$NON-NLS-1$ 
	}
	public void testVariableArg78435() throws Exception{
	     
	    Writer writer = new StringWriter();
	    writer.write( "int aa;       					\n"); //$NON-NLS-1$
	    writer.write( "void f(int aa){}              	\n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Variable: Decln is in formal parameter list; Seln is itself
	    //defect is the global aa is found instead
	    int start = source.indexOf( "aa" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 2 );
	    assertNodeLocation( node, cpp, source.indexOf("aa){}")); //$NON-NLS-1$ 
	}
	public void testVariableStruct() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("struct C {int i;};      \n"); //$NON-NLS-1$
	    writer.write("C c;              	  \n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       \n"); //$NON-NLS-1$
	    writer.write( "void f(){              \n"); //$NON-NLS-1$
	    writer.write( "   c.i/*vp1*/=3;       \n"); //$NON-NLS-1$
	    writer.write( "}                      \n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln global, type struct also defined globally; Seln in function
	    int start = source.indexOf( "c.i/*vp1*/" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 1 );
	    assertNodeLocation( node, h, header.indexOf("c;")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testVariableArg() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write( "int aa=3;              \n"); //$NON-NLS-1$
	    writer.write( "void f(int aa){//decl  \n"); //$NON-NLS-1$
	    writer.write( "  int bb=aa;//vp1      \n"); //$NON-NLS-1$
	    writer.write( "}                      \n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln in formal argument list of function; Seln in function definition, on rhs of assignment
	    int start = source.indexOf( "aa;" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 2 );
	    assertNodeLocation( node, cpp, source.indexOf("aa){//decl")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testVariableClass77996() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("class C {public: int i;};           \n"); //$NON-NLS-1$
	    String header = writer.toString();
	    importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       \n"); //$NON-NLS-1$
	    writer.write( "void f(){              \n"); //$NON-NLS-1$
	    writer.write( "  C c;              	  \n"); //$NON-NLS-1$
	    writer.write( "  c.i/*vp1*/=3;        \n"); //$NON-NLS-1$
	    writer.write( "}                      \n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln in function, type declared outside function, Seln in function
	    int start = source.indexOf( "c.i/*vp1*/" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 1 );
	    assertNodeLocation( node, cpp, source.indexOf("c;")); //$NON-NLS-1$ 
	}
	public void testUnion() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("union A{};           	\n"); //$NON-NLS-1$
	    writer.write("class B{           	\n"); //$NON-NLS-1$
	    writer.write(" union C{} c;         \n"); //$NON-NLS-1$
	    writer.write("}           			\n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile( "a.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"     \n"); //$NON-NLS-1$
	    writer.write( "void f(){            \n"); //$NON-NLS-1$
	    writer.write( "   A a;  //vp1       \n"); //$NON-NLS-1$
	    writer.write( "   B::C c; //vp2     \n"); //$NON-NLS-1$
	    writer.write( "}                    \n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    //vp1 Decln global; Seln in function
	    int start = source.indexOf( "A" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 1 );
	    assertNodeLocation( node, h, header.indexOf("A")); //$NON-NLS-1$ 
	    //vp2 Decln in class; Seln in function, scoped
	    start = source.indexOf( "B::C" ); //$NON-NLS-1$
	    node = getSelection( cpp, start, start + 4 );
	    assertNodeLocation( node, h, header.indexOf("C")); //$NON-NLS-1$ 
	}
}
