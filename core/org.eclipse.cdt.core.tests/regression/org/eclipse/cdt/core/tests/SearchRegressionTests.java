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
 * Created on Oct 4, 2004
 */
package org.eclipse.cdt.core.tests;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexChangeListener;
import org.eclipse.cdt.core.index.IIndexDelta;
import org.eclipse.cdt.core.index.IndexChangeEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.browser.cache.TypeCacheManager;
import org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexer;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

/**
 * @author aniefer
 */
public class SearchRegressionTests extends BaseTestFramework implements ICSearchConstants, IIndexChangeListener{
    static protected 	ICSearchScope 				scope;
    static protected 	SearchEngine				searchEngine;
    static protected 	BasicSearchResultCollector	resultCollector;
    static private 		boolean 					indexChanged = false;
    {
        scope = SearchEngine.createWorkspaceScope();
		resultCollector = new BasicSearchResultCollector();
		searchEngine = new SearchEngine();
    }
    public SearchRegressionTests()
    {
        super();
    }
    /**
     * @param name
     */
    public SearchRegressionTests(String name)
    {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
		try{
			project.setSessionProperty(IndexManager.indexerIDKey, sourceIndexerID);
		    project.setSessionProperty( SourceIndexer.activationKey, new Boolean( true ) );
		} catch ( CoreException e ) { //boo
		}
		TypeCacheManager typeCacheManager = TypeCacheManager.getInstance();
		typeCacheManager.setProcessTypeCacheEvents(false);
		
        IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
        //indexManager.reset();
        
		sourceIndexer = (SourceIndexer) CCorePlugin.getDefault().getCoreModel().getIndexManager().getIndexerForProject(project); 
        sourceIndexer.addIndexChangeListener( this );
    }
    
    protected void tearDown() throws Exception {
        if( project == null || !project.exists() ) 
            return;
    
        IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
        sourceIndexer.removeIndexChangeListener( this );
		try{
		    project.setSessionProperty( SourceIndexer.activationKey, new Boolean( false ) );
		} catch ( CoreException e ) { //boo
		}
        super.tearDown();
	}
    
    protected Set search( ICSearchPattern pattern ) {
		try {
			searchEngine.search( workspace, pattern, scope, resultCollector, false );
		} catch (InterruptedException e) {
		    //boo
		}
		
		return resultCollector.getSearchResults();
	}
    protected Set search( ICSearchPattern pattern, ICElement[] list ) {
    	//leave default scope as workspace
		try {
			ICSearchScope searchScope = SearchEngine.createCSearchScope(list);
			searchEngine.search( workspace, pattern, searchScope, resultCollector, false );
		} catch (InterruptedException e) {
		    //boo
		}
		
		return resultCollector.getSearchResults();
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.index.IIndexChangeListener#indexChanged(org.eclipse.cdt.core.index.IndexChangeEvent)
     */
    public void indexChanged( IndexChangeEvent event ) {
        if( event.getDelta().getDeltaType() == IIndexDelta.MERGE_DELTA ){
            indexChanged = true;
        }
    }
    
    protected IFile importFile(String fileName, String contents ) throws Exception{
        indexChanged = false;
        IFile file = super.importFile( fileName, contents );
	
        while( !indexChanged ){
            Thread.sleep( 100 );
        }
       
		return file;
	}
    
    public void assertMatch( Set matches, IFile file, int offset ) throws Exception {
        Iterator i = matches.iterator();
        while( i.hasNext() ){
            IMatch match = (IMatch) i.next();
            if( match.getStartOffset() == offset && match.getLocation().equals( file.getLocation() ) )
                return; //match
        }
        fail( "Match at offset " + offset + " in \"" + file.getLocation() + "\" not found." );    //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
    }
 
    public static Test suite(){
        return suite( true );
    }
      
    public static Test suite( boolean cleanup ) {
        TestSuite suite = new TestSuite("SearchRegressionTests"); //$NON-NLS-1$
        
        suite.addTest( new SearchRegressionTests("testClassDeclarationReference") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testClassStructDeclaration") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testClassStructReference") ); //$NON-NLS-1$
        
        suite.addTest( new SearchRegressionTests("testNamespaceDeclaration") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testNamespaceDefinition") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testNamespaceReference") ); //$NON-NLS-1$
        
        suite.addTest( new SearchRegressionTests("testMethodDeclaration") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testMethodDefinition") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testMethodReference") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testMethodReferenceOperator") ); //$NON-NLS-1$
        suite.addTest( new FailingTest( new SearchRegressionTests("testMethodReferenceImplicitOperator"), 80117 ) ); //defect80117 //$NON-NLS-1$ 
        suite.addTest( new FailingTest( new SearchRegressionTests("testMethodReferenceInitializer"), 76169 ) ); //defect76169 //$NON-NLS-1$ 
        //fails because inline def refers to a member not declared yet
        suite.addTest( new FailingTest( new SearchRegressionTests("testMethodReferenceInline"), 79425 ) );       //defect79425//$NON-NLS-1$
        //method call with constructor call not found 
        suite.addTest( new FailingTest( new SearchRegressionTests("testMethodReferenceWithCctor"), 79789 ) );       //defect79789//$NON-NLS-1$
        //constructor call in function argument not found
        suite.addTest( new FailingTest( new SearchRegressionTests("testConstructorReferenceArg"), 79785 ) );     //defect79785 //$NON-NLS-1$
        //constructor call by itself not found
        suite.addTest( new FailingTest( new SearchRegressionTests("testConstructorReferenceAlone"), 79792 ) );     //defect79792 //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testDestructorReference") );     //defect79792 //$NON-NLS-1$
               
        suite.addTest( new SearchRegressionTests("testFunctionDeclaration") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testFunctionDefinition") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testFunctionReference") ); //$NON-NLS-1$
        
        suite.addTest( new SearchRegressionTests("testFieldDeclaration") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testBitFieldDeclaration") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testFieldDefinition") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testFieldReference") ); //$NON-NLS-1$
        suite.addTest( new FailingTest( new SearchRegressionTests("testNestedFieldReference"), 76203 ) );       //defect76203//$NON-NLS-1$
        
        suite.addTest( new SearchRegressionTests("testVarDeclaration") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testVarDefinition") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testVarReference") ); //$NON-NLS-1$
        suite.addTest( new FailingTest( new SearchRegressionTests("testVarDeclarationArgument"), 75901 ) );     //defect75901 //$NON-NLS-1$
        //var in initializer list of constructor not found
        suite.addTest( new FailingTest( new SearchRegressionTests("testVarReferenceInitializer"), 72735 ) );    //defect72735 //$NON-NLS-1$
        //definition of a var in an argument list is not found
        suite.addTest( new FailingTest( new SearchRegressionTests("testVarDefinitionArgument"), 75901 ) );     //defect75901 //$NON-NLS-1$
        
        suite.addTest( new SearchRegressionTests("testUnionDeclaration") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testUnionReference") ); //$NON-NLS-1$
        
        suite.addTest( new SearchRegressionTests("testEnumerationDeclaration") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testEnumerationReference") ); //$NON-NLS-1$
        //search doesn't distinguish between global and local symbols
        suite.addTest( new FailingTest( new SearchRegressionTests("testEnumerationReferenceGlobal"), 79811 ) );     //defect79811 //$NON-NLS-1$
        
        suite.addTest( new SearchRegressionTests("testEnumeratorDeclaration") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testEnumeratorReference") ); //$NON-NLS-1$
        suite.addTest( new FailingTest( new SearchRegressionTests("testEnumeratorDeclarationCase"), 79717 ) );     //defect79717 //$NON-NLS-1$  
        
        suite.addTest( new SearchRegressionTests("testTypedefDeclaration") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testMacroDeclaration") ); //$NON-NLS-1$
        
        suite.addTest( new SearchRegressionTests("testMethodFieldReferenceInExpressions") ); //$NON-NLS-1$
        
        if( cleanup )
            suite.addTest( new SearchRegressionTests( "cleanupProject" ) ); //$NON-NLS-1$
        
	    return suite;
    }
    
    public void testClassDeclarationReference() throws Exception {
        Writer writer = new StringWriter();
        writer.write(" class A {               \n" ); //$NON-NLS-1$
        writer.write("    int foo();           \n" ); //$NON-NLS-1$
        writer.write(" };                      \n" ); //$NON-NLS-1$
        writer.write(" int A::foo() {          \n" ); //$NON-NLS-1$
        writer.write(" }                       \n" ); //$NON-NLS-1$
        
        String code = writer.toString();
        IFile f = importFile( "ClassDeclarationReference.cpp", code ); //$NON-NLS-1$
        ICElement[] list = new ICElement[1];
        list[0]=cproject.getCProject();
       
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A", CLASS, ALL_OCCURRENCES, true ); //$NON-NLS-1$
		Set matches = search( pattern, list );
		
		assertEquals( 2, matches.size() );
		assertMatch( matches, f, code.indexOf( "A {" )  ); //$NON-NLS-1$ 
		assertMatch( matches, f, code.indexOf( "A::" ) ); //$NON-NLS-1$ 
   	}
//  test 85b class struct declaration
    public void testClassStructDeclaration() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write(" namespace N1 {							\n" ); //$NON-NLS-1$
    	writer.write("	struct linkedlist1{//decl 				\n" ); //$NON-NLS-1$
    	writer.write("		int field1_t;					    \n" ); //$NON-NLS-1$
    	writer.write("		struct linkedlist1 *field2;	  		\n" ); //$NON-NLS-1$
    	writer.write("	};										\n" ); //$NON-NLS-1$
    	writer.write("	class linkedlist2{//decl				\n" ); //$NON-NLS-1$
    	writer.write("		int field3;							\n" ); //$NON-NLS-1$
    	writer.write("		class linkedlist2 *field4;			\n" ); //$NON-NLS-1$
    	writer.write("	};										\n" ); //$NON-NLS-1$
    	writer.write(" namespace N2 {							\n" ); //$NON-NLS-1$
    	writer.write("  class C { 								\n" ); //$NON-NLS-1$
    	writer.write("  	struct linkedlist1 field5;		    \n" ); //$NON-NLS-1$
    	writer.write("   	linkedlist2 *field6;				\n" ); //$NON-NLS-1$
    	writer.write("  };										\n" ); //$NON-NLS-1$
    	writer.write(" }										\n" ); //$NON-NLS-1$
        writer.write("  class linkedlist2 var1;					\n" ); //$NON-NLS-1$
    	writer.write(" }										\n" ); //$NON-NLS-1$
        String code = writer.toString();			
        IFile gh = importFile( "ClassStructDeclaration.h", code ); //$NON-NLS-1$
             
        ICSearchPattern pattern = SearchEngine.createSearchPattern( "N1::*", CLASS_STRUCT, DECLARATIONS, true ); //$NON-NLS-1$
    	Set matches = search( pattern );
    		
    	assertEquals( 2, matches.size() );
    	assertMatch( matches, gh, code.indexOf( "linkedlist1{//decl" )  ); //$NON-NLS-1$ 
    	assertMatch( matches, gh, code.indexOf( "linkedlist2{//decl" )  ); //$NON-NLS-1$ 
    	
    }
    // test 90
    public void testClassStructReference() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("class Mammal {							\n" ); 	//$NON-NLS-1$
        writer.write("	public:									\n" ); 	//$NON-NLS-1$
        writer.write("	Mammal(bool b){}						\n" );  //$NON-NLS-1$
        writer.write("  static litter=12;						\n" ); 	//$NON-NLS-1$
        writer.write("};										\n" ); 	//$NON-NLS-1$
        writer.write("class Bear : Mammal/*ref1*/{				\n" ); 	//$NON-NLS-1$
        writer.write("public:									\n" ); 	//$NON-NLS-1$
        writer.write("	Bear(int s,int t): Mammal(true) { 		\n" );  //$NON-NLS-1$
        writer.write("    biotics.littersPerYear=t;				\n" ); 	//$NON-NLS-1$
        writer.write("  }										\n" ); 	//$NON-NLS-1$
        writer.write("  struct {								\n" ); 	//$NON-NLS-1$
    	writer.write("	  int littersPerYear; 					\n" ); 	//$NON-NLS-1$
        writer.write("  } biotics;								\n" ); 	//$NON-NLS-1$
        writer.write("  class BearPaws{};						\n" ); 	//$NON-NLS-1$
        writer.write("};										\n" ); 	//$NON-NLS-1$
        writer.write("struct bioticPotential {					\n" ); 	//$NON-NLS-1$
    	writer.write("  int litterSize;							\n" ); 	//$NON-NLS-1$
        writer.write("	int matureYears;						\n" ); 	//$NON-NLS-1$
        writer.write("};										\n" );	//$NON-NLS-1$
        writer.write("struct bioticPotential/*ref2*/ bp;		\n" ); //$NON-NLS-1$
        writer.write("int main(int argc, char **argv) {			\n" ); //$NON-NLS-1$
        writer.write(" 	bioticPotential/*ref3*/ brownbear_bt = {1, 3};			\n" ); //$NON-NLS-1$
        writer.write("	Bear/*ref4*/ *grizzly = new Bear(4,3);					\n" ); //$NON-NLS-1$
        writer.write("    	brownbear_bt.litterSize = Mammal::litter;/*ref5*/	\n" ); //$NON-NLS-1$
        writer.write("  Bear::BearPaws p;//ref6,ref7							\n" ); 	//$NON-NLS-1$
        writer.write("} 														\n" ); 	//$NON-NLS-1$
        String code = writer.toString();
        IFile b = importFile( "ClassStructReference.cpp", code ); //$NON-NLS-1$
        ICElement[] list = new ICElement[1];
        list[0]=cproject.getCProject();
       
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "*", CLASS_STRUCT, REFERENCES, true ); //$NON-NLS-1$
		Set matches = search( pattern,list );
		
		assertMatch( matches, b, code.indexOf( "Mammal/*ref1*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, b, code.indexOf( "bioticPotential/*ref2*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, b, code.indexOf( "bioticPotential/*ref3*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, b, code.indexOf( "Bear/*ref4*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, b, code.indexOf( "Mammal::litter;/*ref5*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, b, code.indexOf( "Bear::BearPaws p;//ref6" )  ); //$NON-NLS-1$ 
		assertMatch( matches, b, code.indexOf( "BearPaws p;//ref6,ref7" )  ); //$NON-NLS-1$ 
		assertEquals( 7, matches.size() );
		
    }
    //test 88
    public void testNamespaceDeclaration() throws Exception {
        Writer writer = new StringWriter();
        writer.write("namespace N {//defn,decl				\n" ); //$NON-NLS-1$
        writer.write("  int a;								\n" ); //$NON-NLS-1$
        writer.write("  namespace M {//defn,decl			\n" ); //$NON-NLS-1$
        writer.write("    struct linkedlist {				\n" ); //$NON-NLS-1$
        writer.write("      int item;						\n" ); //$NON-NLS-1$
        writer.write("      struct linkedlist *next;		\n" ); //$NON-NLS-1$
        writer.write("    };								\n" ); //$NON-NLS-1$
        writer.write("  }									\n" ); //$NON-NLS-1$
        writer.write("}										\n" ); //$NON-NLS-1$
        String code = writer.toString();
        IFile gh = importFile( "NamespaceDeclaration.h", code ); //$NON-NLS-1$
        
        writer = new StringWriter();
        writer.write("#include \"NamespaceDeclaration.h\"					\n" ); //$NON-NLS-1$
        writer.write("using namespace N::M;				\n" ); //$NON-NLS-1$
        writer.write("linkedlist serial_numbers;		\n" ); //$NON-NLS-1$
        writer.write("namespace R {//defn,decl			\n" ); //$NON-NLS-1$
        writer.write("	class C {						\n" ); //$NON-NLS-1$
        writer.write("	public:							\n" ); //$NON-NLS-1$
        writer.write("	  char c();						\n" ); //$NON-NLS-1$
        writer.write("	};								\n" ); //$NON-NLS-1$
        writer.write("}									\n" ); //$NON-NLS-1$
        writer.write("using namespace R;				\n" ); //$NON-NLS-1$
        writer.write("char C::c(){						\n" ); //$NON-NLS-1$
        writer.write("return 'a';						\n" ); //$NON-NLS-1$
        writer.write("}									\n" ); //$NON-NLS-1$
        String code2 = writer.toString();
        IFile g = importFile( "NamespaceDeclaration.cpp", code2 ); //$NON-NLS-1$
        ICElement[] list = new ICElement[1];
        list[0]=cproject.getCProject();
       
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "*", NAMESPACE, DECLARATIONS, true ); //$NON-NLS-1$
		Set matches = search( pattern,list );
		
		assertEquals( 3, matches.size() );
		assertMatch( matches, gh, code.indexOf( "N" )  ); //$NON-NLS-1$ 
		assertMatch( matches, gh, code.indexOf( "M" )  ); //$NON-NLS-1$ 
		assertMatch( matches, g, code2.indexOf( "R {//defn,decl" )  ); //$NON-NLS-1$ 

   	}
   
    // test SE47
    public void testNamespaceDefinition() throws Exception {
        Writer writer = new StringWriter();
        writer.write("namespace N/*def1*/ {				\n" ); //$NON-NLS-1$
        writer.write("  int a;							\n" ); //$NON-NLS-1$
        writer.write("  namespace M/*def2*/ {			\n" ); //$NON-NLS-1$
        writer.write("    struct linkedlist {};			\n" ); //$NON-NLS-1$
        writer.write("  }								\n" ); //$NON-NLS-1$
        writer.write("}									\n" ); //$NON-NLS-1$
        writer.write("namespace N/*def3*/{int i;}		\n" ); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile( "NamespaceDefinition.h", header ); //$NON-NLS-1$
        
        writer = new StringWriter();
        writer.write("#include \"NamespaceDefinition.h\"\n" ); //$NON-NLS-1$
        writer.write("using namespace N::M;				\n" ); //$NON-NLS-1$
        writer.write("linkedlist serial_numbers;		\n" ); //$NON-NLS-1$
        writer.write("namespace R/*def4*/ {				\n" ); //$NON-NLS-1$
        writer.write("	class C {};						\n" ); //$NON-NLS-1$
        writer.write("}									\n" ); //$NON-NLS-1$
        writer.write("using namespace R;				\n" ); //$NON-NLS-1$
        String source = writer.toString();
        IFile cpp = importFile( "NamespaceDefinition.cpp", source ); //$NON-NLS-1$
        ICElement[] list = new ICElement[1];
        list[0]=cproject.getCProject();
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "*", NAMESPACE, DEFINITIONS, true ); //$NON-NLS-1$
		Set matches = search( pattern,list );
	
		assertEquals( 4, matches.size() );
		assertMatch( matches, h, header.indexOf( "N/*def1*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, h, header.indexOf( "M" )  ); //$NON-NLS-1$ 
		assertMatch( matches, h, header.indexOf( "N/*def3*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, source.indexOf( "R/*def4*/" )  ); //$NON-NLS-1$

   	}
    //test 74 namespace references
    public void testNamespaceReference() throws Exception {
    	Writer writer = new StringWriter();
        writer.write("namespace N1 {\n" ); //$NON-NLS-1$
        writer.write("	class C1 {};\n" ); //$NON-NLS-1$
        writer.write("	namespace N2 {\n" ); //$NON-NLS-1$
		writer.write("		class C2{};\n" ); //$NON-NLS-1$
		writer.write("		namespace N3 {\n" ); //$NON-NLS-1$
		writer.write("			class C3{};\n" ); //$NON-NLS-1$
		writer.write("		}		\n" ); //$NON-NLS-1$
		writer.write("	}			\n" ); //$NON-NLS-1$
		writer.write("}				\n" ); //$NON-NLS-1$
        String code = writer.toString();
        /*IFile nh = */importFile( "NamespaceReference.h", code ); //$NON-NLS-1$
            
        writer = new StringWriter();
        writer.write("#include \"NamespaceReference.h\"				\n" ); //$NON-NLS-1$
        writer.write("N1::N2::C2 *c = new N1::N2::C2();		\n" ); //$NON-NLS-1$
        writer.write("using namespace N1::N2;				\n" ); //$NON-NLS-1$
        writer.write("N3::C3 *d = new N3::C3();       		\n" ); //$NON-NLS-1$
        String code2 = writer.toString();
        IFile n = importFile( "NamespaceReference.cpp", code2 ); //$NON-NLS-1$
        ICElement[] list = new ICElement[1];
        list[0]=cproject.getCProject();    
    	ICSearchPattern pattern = SearchEngine.createSearchPattern( "*", NAMESPACE, REFERENCES, true ); //$NON-NLS-1$
    	Set matches = search( pattern,list );
    		
    	assertEquals( 8, matches.size() );
    	assertMatch( matches, n, code2.indexOf( "N1::N2::C2 *c" )  ); //$NON-NLS-1$ 
    	assertMatch( matches, n, code2.indexOf( "N2::C2 *c" )  ); //$NON-NLS-1$ 
    	assertMatch( matches, n, code2.indexOf( "N1::N2::C2();" )  ); //$NON-NLS-1$ 
    	assertMatch( matches, n, code2.indexOf( "N2::C2();" )  ); //$NON-NLS-1$ 
    	assertMatch( matches, n, code2.indexOf( "N1::N2;" )  ); //$NON-NLS-1$ 
    	assertMatch( matches, n, code2.indexOf( "N2;" )  ); //$NON-NLS-1$ 
    	assertMatch( matches, n, code2.indexOf( "N3::C3 *d" )  ); //$NON-NLS-1$ 
    	assertMatch( matches, n, code2.indexOf( "N3::C3();" )  ); //$NON-NLS-1$ 

    }
    //test SE100a
    public void testMethodDeclaration() throws Exception {
        
    	Writer writer = new StringWriter();
    	writer.write("class M {									\n" ); //$NON-NLS-1$
    	writer.write("public:									\n" ); //$NON-NLS-1$
    	writer.write("	int m;									\n" ); //$NON-NLS-1$
    	writer.write("	M/*dec1*/();							\n" ); //$NON-NLS-1$
    	writer.write("	explicit M/*dec2*/(int i){}				\n" ); //$NON-NLS-1$
    	writer.write("	virtual ~M/*dec3*/(){}					\n" ); //$NON-NLS-1$
    	writer.write("	void m1/*dec4*/();						\n" ); //$NON-NLS-1$
    	writer.write("	static int m2/*dec5*/(){return 1;}		\n" ); //$NON-NLS-1$
    	writer.write("	M& operator <</*dec7*/ (const M &rhs);	\n" ); //$NON-NLS-1$
    	writer.write("	M& operator==/*dec8*/ (const M &rhs);	\n" ); //$NON-NLS-1$
    	writer.write("};										\n" ); //$NON-NLS-1$
    	writer.write("namespace N{								\n" ); //$NON-NLS-1$
    	writer.write("class C{ 									\n" ); //$NON-NLS-1$
    	writer.write("	inline int m3/*dec6*/(int i){return 2;}	\n" ); //$NON-NLS-1$
    	writer.write("};										\n" ); //$NON-NLS-1$
    	writer.write("}											\n" ); //$NON-NLS-1$
    	writer.write("int f1(M m){}								\n" ); //$NON-NLS-1$
    	writer.write("void foo(){								\n" ); //$NON-NLS-1$
        writer.write("	f1(M(3)); 								\n" ); //$NON-NLS-1$
    	writer.write("}											\n" ); //$NON-NLS-1$
        writer.write("M::M() {									\n" ); //$NON-NLS-1$
	    writer.write("	fz();									\n" ); //$NON-NLS-1$
	    writer.write("}											\n" ); //$NON-NLS-1$
        writer.write("void M::m1() {							\n" ); //$NON-NLS-1$
	    writer.write("	fz();									\n" ); //$NON-NLS-1$
	    writer.write(" }										\n" ); //$NON-NLS-1$
        String code = writer.toString();			
        IFile gh = importFile( "MethodDeclaration.cpp", code ); //$NON-NLS-1$
        //vp1 constructor, explicit cctor, destructor
        ICSearchPattern pattern=SearchEngine.createSearchPattern("*M", METHOD, DECLARATIONS, true); //$NON-NLS-1$
        ICElement[] list = new ICElement[1];
        list[0]=cproject.getCProject();
        Set matches = search(pattern,list);
        assertEquals( 3, matches.size());
		assertMatch( matches, gh, code.indexOf( "M/*dec1*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, gh, code.indexOf( "M/*dec2*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, gh, code.indexOf( "~M/*dec3*/" )  ); //$NON-NLS-1$ 
		//vp2 method, static, inline
        pattern=SearchEngine.createSearchPattern("m*", METHOD, DECLARATIONS, true); //$NON-NLS-1$
        matches = search(pattern,list);
        assertEquals( 3, matches.size());
		assertMatch( matches, gh, code.indexOf( "m1/*dec4*/" )  ); //$NON-NLS-1$
		assertMatch( matches, gh, code.indexOf( "m2/*dec5*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, gh, code.indexOf( "m3/*dec6*/" )  ); //$NON-NLS-1$ 
		//vp3 namespace scope, fully qualified search
		pattern=SearchEngine.createSearchPattern("N::C::m3", METHOD, DECLARATIONS, true); //$NON-NLS-1$
        matches = search(pattern,list);
        assertEquals( 1, matches.size());
		assertMatch( matches, gh, code.indexOf( "m3/*dec6*/" )  ); //$NON-NLS-1$ 
		//vp4 operator with and without space in name
		pattern=SearchEngine.createSearchPattern("operator ??", METHOD, DECLARATIONS, true); //$NON-NLS-1$
        matches = search(pattern,list);
        assertEquals( 2, matches.size());
		assertMatch( matches, gh, code.indexOf( "operator <</*dec7*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, gh, code.indexOf( "operator==/*dec8*/" )  ); //$NON-NLS-1$ 
		}
    //  test SE100b
    public void testMethodDefinition() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("class N {									\n" ); //$NON-NLS-1$
    	writer.write(" N& operator ||(const N &rhs){}			\n" ); //$NON-NLS-1$
    	writer.write(" N& operator|(const N &rhs);				\n" ); //$NON-NLS-1$
    	writer.write(" int m;									\n" ); //$NON-NLS-1$
    	writer.write("};										\n" ); //$NON-NLS-1$
    	String header = writer.toString();			
        importFile( "MethodDefinition.h", header ); //$NON-NLS-1$
       
    	writer = new StringWriter();
    	writer.write("#include \"MethodDefinition.h\"			\n" ); //$NON-NLS-1$
    	writer.write("N& N::operator|/*def1*/(const N &rhs){}	\n" ); //$NON-NLS-1$
    	writer.write("class M {									\n" ); //$NON-NLS-1$
    	writer.write("public:									\n" ); //$NON-NLS-1$
    	writer.write("	int m;									\n" ); //$NON-NLS-1$
    	writer.write("	M/*def2*/(){}							\n" ); //$NON-NLS-1$
    	writer.write("	explicit M(int i);						\n" ); //$NON-NLS-1$
    	writer.write("	virtual ~M/*def3*/(){}					\n" ); //$NON-NLS-1$
    	writer.write("	void m1();								\n" ); //$NON-NLS-1$
    	writer.write("	static int m2/*def4*/(){return 1;}		\n" ); //$NON-NLS-1$
    	writer.write("};										\n" ); //$NON-NLS-1$
    	writer.write("namespace NN{								\n" ); //$NON-NLS-1$
    	writer.write("class C{ 									\n" ); //$NON-NLS-1$
    	writer.write("	inline int m3/*def5*/(int i){return 2;}	\n" ); //$NON-NLS-1$
    	writer.write("	void foo() const;						\n" ); //$NON-NLS-1$
    	writer.write("};										\n" ); //$NON-NLS-1$
    	writer.write("}											\n" ); //$NON-NLS-1$
    	writer.write("											\n" ); //$NON-NLS-1$
    	writer.write("void NN::C::foo/*def6*/() const{}			\n" ); //$NON-NLS-1$
        writer.write("M::M/*def7*/(int i) {						\n" ); //$NON-NLS-1$
	    writer.write("	m=i;									\n" ); //$NON-NLS-1$
	    writer.write("}											\n" ); //$NON-NLS-1$
        writer.write("void M::m1/*def8*/() {}					\n" ); //$NON-NLS-1$
	    String code = writer.toString();			
        IFile cpp = importFile( "MethodDefinition.cpp", code ); //$NON-NLS-1$
       	//vp1 operator, constructor, destructor, inline, static, const, explicit constructor
        ICElement[] list = {cproject.findElement(new Path("MethodDefinition.cpp"))}; //$NON-NLS-1$
        ICSearchPattern pattern=SearchEngine.createSearchPattern("*", METHOD, DEFINITIONS, true); //$NON-NLS-1$
        Set matches = search(pattern, list);
        assertMatch( matches, cpp, code.indexOf( "operator|/*def1*/" )  ); //$NON-NLS-1$
    	assertMatch( matches, cpp, code.indexOf( "M/*def2*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, code.indexOf( "~M/*def3*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, code.indexOf( "m2/*def4*/" )  ); //$NON-NLS-1$
		assertMatch( matches, cpp, code.indexOf( "m3/*def5*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, code.indexOf( "foo/*def6*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, code.indexOf( "M/*def7*/" )  ); //$NON-NLS-1$
		assertMatch( matches, cpp, code.indexOf( "m1/*def8*/" )  ); //$NON-NLS-1$
		assertEquals( 8, matches.size());
        //vp2 operator with space in search pattern
		pattern=SearchEngine.createSearchPattern("operator |*", METHOD, DEFINITIONS, true); //$NON-NLS-1$
        matches = search(pattern, list);
        assertEquals( 1, matches.size());
        assertMatch( matches, cpp, code.indexOf( "operator|/*def1*/" )  ); //$NON-NLS-1$
    }
    // test SE50, SE100c
    public void testMethodReference() throws Exception {
        Writer writer = new StringWriter();
    	writer.write("class K{					\n" ); //$NON-NLS-1$
        writer.write("public:					\n" ); //$NON-NLS-1$
        writer.write("	K();					\n" ); //$NON-NLS-1$
        writer.write("	virtual ~K();			\n" ); //$NON-NLS-1$
        writer.write("	static int fx();		\n" ); //$NON-NLS-1$
        writer.write("	inline void fz(){}		\n" ); //$NON-NLS-1$
        writer.write("	void fy(){fz/*ref*/()}	\n" ); //$NON-NLS-1$
        writer.write("	int kk;					\n" ); //$NON-NLS-1$
        writer.write("	explicit K(int i){}		\n" ); //$NON-NLS-1$
        writer.write("};						\n" ); //$NON-NLS-1$
        String code = writer.toString();
        importFile( "MethodReference.h", code ); //$NON-NLS-1$
        
        writer = new StringWriter();
        writer.write("#include \"MethodReference.h\"\n" ); //$NON-NLS-1$
        writer.write("K::K(){}						\n" ); //$NON-NLS-1$
        writer.write("K::~K(){}						\n" ); //$NON-NLS-1$
        writer.write("int K::fx(){return 12;}		\n" ); //$NON-NLS-1$
        writer.write("void fw(){}					\n" ); //$NON-NLS-1$
        writer.write("void foo(){					\n" ); //$NON-NLS-1$
        writer.write("	int i = K::fx/*ref1*/();	\n" ); //$NON-NLS-1$
        writer.write("	(new K/*ref4*/())->fy/*ref2*/();	\n" ); //$NON-NLS-1$
        writer.write("	fw();						\n" ); //$NON-NLS-1$
        writer.write("	K k;						\n" ); //$NON-NLS-1$
        writer.write("	k.fy/*ref3*/();				\n" ); //$NON-NLS-1$
        writer.write("  k.~K/*ref5*/();				\n" ); //$NON-NLS-1$
        writer.write("}								\n" ); //$NON-NLS-1$
        String code2 = writer.toString();
        IFile k = importFile( "MethodReference.cpp", code2 ); //$NON-NLS-1$
        ICElement[] list = new ICElement[1];
        list[0]=cproject.findElement(new Path("MethodReference.cpp")); //$NON-NLS-1$
        //vp1 ::, ->, . , inline references
        ICSearchPattern pattern=SearchEngine.createSearchPattern("f*", METHOD, REFERENCES, true); //$NON-NLS-1$
        Set matches = search( pattern,list);
		//assertEquals( 3, matches.size());
        assertMatch( matches, k, code2.lastIndexOf( "fx/*ref1*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, k, code2.indexOf( "fy/*ref2*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, k, code2.lastIndexOf( "fy/*ref3*/" )  ); //$NON-NLS-1$ 
		//vp2 constructor
		pattern=SearchEngine.createSearchPattern("*K", METHOD, REFERENCES, true); //$NON-NLS-1$
        matches = search( pattern);
		assertEquals( 2, matches.size());
        assertMatch( matches, k, code2.indexOf( "K/*ref4*/" )  ); //$NON-NLS-1$ 
        assertMatch( matches, k, code2.indexOf( "~K/*ref5*/" )  ); //$NON-NLS-1$ 
	}
    // test SE50, SE100c
    public void testMethodReferenceOperator() throws Exception {
        Writer writer = new StringWriter();
    	writer.write("class K{							\n" ); //$NON-NLS-1$
        writer.write("public:							\n" ); //$NON-NLS-1$
        writer.write("	K();							\n" ); //$NON-NLS-1$
        writer.write("	K& operator == (const K &rhs){}	\n" ); //$NON-NLS-1$
        writer.write("};								\n" ); //$NON-NLS-1$
        writer.write("void foo(){						\n" ); //$NON-NLS-1$
        writer.write("	K k;							\n" ); //$NON-NLS-1$
        writer.write("	K m=k;							\n" ); //$NON-NLS-1$
        writer.write("	if (m.operator==/*ref1*/(k)){}	\n" ); //$NON-NLS-1$
        writer.write("}									\n" ); //$NON-NLS-1$
        String source = writer.toString();
        IFile cpp = importFile( "MethodReferenceOperator.cpp", source ); //$NON-NLS-1$
        //vp1 explicit operator== call
        ICSearchPattern pattern=SearchEngine.createSearchPattern("operator ==", METHOD, REFERENCES, true); //$NON-NLS-1$
        Set matches = search( pattern );
		assertEquals( 1, matches.size());
        assertMatch( matches, cpp, source.indexOf( "operator==/*ref1*/" )  ); //$NON-NLS-1$ 
    }
    public void testMethodReferenceImplicitOperator() throws Exception {
        Writer writer = new StringWriter();
    	writer.write("class K{							\n" ); //$NON-NLS-1$
        writer.write("public:							\n" ); //$NON-NLS-1$
        writer.write("	K();							\n" ); //$NON-NLS-1$
        writer.write("	K& operator == (const K &rhs){}	\n" ); //$NON-NLS-1$
        writer.write("};								\n" ); //$NON-NLS-1$
        writer.write("void foo(){						\n" ); //$NON-NLS-1$
        writer.write("	K k;							\n" ); //$NON-NLS-1$
        writer.write("	K m=k;							\n" ); //$NON-NLS-1$
        writer.write("	if (m==/*ref*/k){}				\n" ); //$NON-NLS-1$
        writer.write("}									\n" ); //$NON-NLS-1$
        String source = writer.toString();
        IFile cpp = importFile( "MethodReferenceImplicitOperator.cpp", source ); //$NON-NLS-1$
        //vp1 implicit operator== call (A==B)
		ICSearchPattern pattern=SearchEngine.createSearchPattern("operator ==", METHOD, REFERENCES, true); //$NON-NLS-1$
        Set matches = search( pattern );
		assertEquals( 1, matches.size());
        assertMatch( matches, cpp, source.indexOf( "==/*ref*/" )  ); //$NON-NLS-1$ 
    }

	//76169
	public void testMethodReferenceInitializer() throws Exception {
		Writer writer = new StringWriter();
	    writer.write("class Mammal {			\n" ); //$NON-NLS-1$
	    writer.write("	public:					\n" ); //$NON-NLS-1$
	    writer.write("	Mammal(int t): num(t){}	\n" ); //$NON-NLS-1$
	    writer.write("	private:				\n" ); //$NON-NLS-1$
	    writer.write("	int num;				\n" ); //$NON-NLS-1$
	    writer.write("};						\n" ); //$NON-NLS-1$
	    writer.write("class Bear : Mammal{		\n" ); //$NON-NLS-1$
		writer.write("public:					\n" ); //$NON-NLS-1$
	    writer.write("	  Bear(int t): Mammal(t) {} \n" ); //$NON-NLS-1$
	    writer.write("};						\n" ); //$NON-NLS-1$
	    String code = writer.toString();
	    IFile g = importFile( "MethodReferenceInitializer.cpp", code ); //$NON-NLS-1$
	    
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "Mammal", METHOD, REFERENCES, true ); //$NON-NLS-1$
		Set matches = search( pattern );
		
		assertEquals( 1, matches.size() );
		assertMatch( matches, g, code.indexOf( "Mammal(t)" )  ); //$NON-NLS-1$ 
		
	}
	public void testMethodReferenceInline() throws Exception {
	    
		Writer writer = new StringWriter();
		writer.write("class A{						\n" ); //$NON-NLS-1$
		writer.write("  friend class M;				\n" ); //$NON-NLS-1$
		writer.write("  void aa();					\n" ); //$NON-NLS-1$
		writer.write("};							\n" ); //$NON-NLS-1$
		writer.write("class M {						\n" ); //$NON-NLS-1$
		writer.write("public:						\n" ); //$NON-NLS-1$
		writer.write("	M();						\n" ); //$NON-NLS-1$
		writer.write("	void zz(){a->aa();}//ref		\n" ); //$NON-NLS-1$
		writer.write("  A *a;						\n" ); //$NON-NLS-1$
		writer.write("};							\n" ); //$NON-NLS-1$
		String code = writer.toString();			
	    IFile gh = importFile( "MethodReferenceInline.cpp", code ); //$NON-NLS-1$
	    //ref inside inline defn
	    ICSearchPattern pattern=SearchEngine.createSearchPattern("aa", METHOD, REFERENCES, true); //$NON-NLS-1$
	    Set matches = search(pattern);
	    assertEquals( 1, matches.size());
		assertMatch( matches, gh, code.indexOf( "aa();}//ref" )  ); //$NON-NLS-1$ 
	}
    public void testMethodReferenceWithCctor() throws Exception {
        Writer writer = new StringWriter();
    	writer.write("class C{			\n" ); //$NON-NLS-1$
        writer.write(" void cc(){}		\n" ); //$NON-NLS-1$
        writer.write("};				\n" ); //$NON-NLS-1$
        writer.write("void g(){			\n" ); //$NON-NLS-1$
        writer.write("	C().cc/*ref*/();\n" ); //$NON-NLS-1$
        writer.write("}					\n" ); //$NON-NLS-1$
       	String source = writer.toString();
        IFile cpp = importFile( "MethodReferenceWithCctor.cpp", source ); //$NON-NLS-1$
        //vp1 constructor by itself
		ICSearchPattern pattern=SearchEngine.createSearchPattern("cc", METHOD, REFERENCES, true); //$NON-NLS-1$
        Set matches = search( pattern );
		assertEquals( 1, matches.size());
        assertMatch( matches, cpp, source.indexOf( "cc/*ref*/" )  ); //$NON-NLS-1$ 
		
   	}
    public void testConstructorReferenceArg() throws Exception {
        Writer writer = new StringWriter();
    	writer.write("class C{			\n" ); //$NON-NLS-1$
        writer.write(" C();				\n" ); //$NON-NLS-1$
        writer.write("};				\n" ); //$NON-NLS-1$
        writer.write("void f(C c){}		\n" ); //$NON-NLS-1$
        writer.write("void g(){			\n" ); //$NON-NLS-1$
        writer.write("	f(C/*ref*/());;	\n" ); //$NON-NLS-1$
        writer.write("}					\n" ); //$NON-NLS-1$
       	String source = writer.toString();
        IFile cpp = importFile( "ConstructorReferenceArg.cpp", source ); //$NON-NLS-1$
        //vp1 constructor, destructor
		ICSearchPattern pattern=SearchEngine.createSearchPattern("C", METHOD, REFERENCES, true); //$NON-NLS-1$
        Set matches = search( pattern );
		assertEquals( 1, matches.size());
        assertMatch( matches, cpp, source.indexOf( "C/*ref*/" )  ); //$NON-NLS-1$ 
		
   	}
    public void testConstructorReferenceAlone() throws Exception {
        Writer writer = new StringWriter();
    	writer.write("class C{			\n" ); //$NON-NLS-1$
        writer.write(" C(){}			\n" ); //$NON-NLS-1$
        writer.write("};				\n" ); //$NON-NLS-1$
        writer.write("void g(){			\n" ); //$NON-NLS-1$
        writer.write("	C/*ref*/();		\n" ); //$NON-NLS-1$
        writer.write("}					\n" ); //$NON-NLS-1$
       	String source = writer.toString();
        IFile cpp = importFile( "ConstructorReferenceAlone.cpp", source ); //$NON-NLS-1$
        //vp1 constructor by itself
		ICSearchPattern pattern=SearchEngine.createSearchPattern("C", METHOD, REFERENCES, true); //$NON-NLS-1$
        Set matches = search( pattern );
		assertEquals( 1, matches.size());
        assertMatch( matches, cpp, source.indexOf( "C/*ref*/" )  ); //$NON-NLS-1$ 
		
   	}
	public void testDestructorReference() throws Exception {
	    
		Writer writer = new StringWriter();
		writer.write("class A{					\n" ); //$NON-NLS-1$
		writer.write("  friend class M;			\n" ); //$NON-NLS-1$
		writer.write("  ~A();					\n" ); //$NON-NLS-1$
		writer.write("};						\n" ); //$NON-NLS-1$
		writer.write("class M {					\n" ); //$NON-NLS-1$
		writer.write("public:					\n" ); //$NON-NLS-1$
		writer.write("	M();					\n" ); //$NON-NLS-1$
		writer.write("	~M();					\n" ); //$NON-NLS-1$
		writer.write("  A *a;					\n" ); //$NON-NLS-1$
		writer.write("};						\n" ); //$NON-NLS-1$
		writer.write("M::~M(){a->~A();}//ref	\n" ); //$NON-NLS-1$
		writer.write("void f() {				\n" ); //$NON-NLS-1$
	    writer.write("	M m;					\n" ); //$NON-NLS-1$
		writer.write("	m.~M();//ref			\n" ); //$NON-NLS-1$
	    writer.write("}							\n" ); //$NON-NLS-1$
	    String source = writer.toString();			
	    IFile cpp = importFile( "DestructorReference.cpp", source ); //$NON-NLS-1$
	    //vp1 arrow ref
	    ICSearchPattern pattern=SearchEngine.createSearchPattern("~A", METHOD, REFERENCES, true); //$NON-NLS-1$
	    Set matches = search(pattern);
	    assertEquals( 1, matches.size());
		assertMatch( matches, cpp, source.indexOf( "~A();}//ref" )  ); //$NON-NLS-1$ 
		//vp2 dot ref
		pattern=SearchEngine.createSearchPattern("~M", METHOD, REFERENCES, true); //$NON-NLS-1$
	    matches = search(pattern);
	    assertEquals( 1, matches.size());
		assertMatch( matches, cpp, source.indexOf( "~M();//ref" )  ); //$NON-NLS-1$ 
	}
	public void testFunctionDeclaration() throws Exception {
        Writer writer = new StringWriter();
    	writer.write("namespace N{					\n" ); //$NON-NLS-1$
        writer.write("int f1/*decl*/(){return 0;}	\n" ); //$NON-NLS-1$
        writer.write("}								\n" ); //$NON-NLS-1$
        writer.write("static int f2/*decl*/();		\n" ); //$NON-NLS-1$
        writer.write("extern int f3/*decl*/();		\n" ); //$NON-NLS-1$
        writer.write("inline void f4/*decl*/(int *a, int *b){int t=*b; *b=*a; *a=t;}	\n" ); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile( "FunctionDeclaration.h", header ); //$NON-NLS-1$
        
        writer = new StringWriter();
        writer.write("#include \"FunctionDeclaration.h\"\n" ); //$NON-NLS-1$
        writer.write("void f5()/*decl*/{				\n" ); //$NON-NLS-1$
        writer.write("	int i = N::f1();				\n" ); //$NON-NLS-1$
        writer.write("	int j = f3();					\n" ); //$NON-NLS-1$
        writer.write("	f4(&i, &j);						\n" ); //$NON-NLS-1$
      	writer.write("}									\n" ); //$NON-NLS-1$
        String source = writer.toString();
        IFile cpp = importFile( "FunctionDeclaration.cpp", source ); //$NON-NLS-1$
        ICSearchPattern pattern=SearchEngine.createSearchPattern("f*", FUNCTION, DECLARATIONS, true); //$NON-NLS-1$
        ICElement[] list = new ICElement[1];
        list[0]=cproject.getCProject();
        Set matches = search( pattern,list );
		assertMatch( matches, h, header.indexOf( "f1" )  ); //$NON-NLS-1$ 
		assertMatch( matches, h, header.indexOf( "f2" )  ); //$NON-NLS-1$ 
		assertMatch( matches, h, header.indexOf( "f3" )  ); //$NON-NLS-1$ 
		assertMatch( matches, h, header.indexOf( "f4" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, source.indexOf( "f5" )  ); //$NON-NLS-1$ 
		assertEquals( 5, matches.size());
		
   	}
	public void testFunctionDefinition() throws Exception {
        Writer writer = new StringWriter();
    	writer.write("namespace N{					\n" ); //$NON-NLS-1$
        writer.write("int f1/*def*/(){return 0;}	\n" ); //$NON-NLS-1$
        writer.write("}								\n" ); //$NON-NLS-1$
        writer.write("static int f2();				\n" ); //$NON-NLS-1$
        writer.write("extern int f3/*decl*/();		\n" ); //$NON-NLS-1$
        writer.write("inline void f4/*def*/(int *a, int *b){int t=*b; *b=*a; *a=t;}	\n" ); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile( "FunctionDefinition.h", header ); //$NON-NLS-1$
        
        writer = new StringWriter();
        writer.write("#include \"FunctionDefinition.h\"\n" ); //$NON-NLS-1$
        writer.write("int f2/*def*/(){return 2;}		\n" ); //$NON-NLS-1$
        writer.write("void f5/*def*/(){					\n" ); //$NON-NLS-1$
        writer.write("	int i = N::f1();				\n" ); //$NON-NLS-1$
        writer.write("	int j = f3();					\n" ); //$NON-NLS-1$
        writer.write("	f4(&i, &j);						\n" ); //$NON-NLS-1$
      	writer.write("}									\n" ); //$NON-NLS-1$
        String source = writer.toString();
        IFile cpp = importFile( "FunctionDefinition.cpp", source ); //$NON-NLS-1$
        ICSearchPattern pattern=SearchEngine.createSearchPattern("f*", FUNCTION, DEFINITIONS, true); //$NON-NLS-1$
        ICElement[] list = new ICElement[1];
        list[0]=cproject.getCProject();
        Set matches = search( pattern,list );
		assertMatch( matches, h, header.indexOf( "f1" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, source.indexOf( "f2" )  ); //$NON-NLS-1$ 
		assertMatch( matches, h, header.indexOf( "f4" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, source.indexOf( "f5" )  ); //$NON-NLS-1$ 
		assertEquals( 4, matches.size());
		
   	}
    //SE50 working set
    public void testFunctionReference() throws Exception {
        Writer writer = new StringWriter();
    	writer.write("namespace N{			\n" ); //$NON-NLS-1$
        writer.write("int fz(){return 0;}	\n" ); //$NON-NLS-1$
        writer.write("}						\n" ); //$NON-NLS-1$
        String header = writer.toString();
        importFile( "FunctionReference.h", header ); //$NON-NLS-1$
        
        writer = new StringWriter();
        writer.write("#include \"FunctionReference.h\"	\n" ); //$NON-NLS-1$
        writer.write("int fw(){return 0;}				\n" ); //$NON-NLS-1$
        writer.write("void fg(int i){}					\n" ); //$NON-NLS-1$
        writer.write("void f(){							\n" ); //$NON-NLS-1$
        writer.write("	int i = N::fz/*ref1*/();		\n" ); //$NON-NLS-1$
        writer.write("	fg/*ref2*/(fw/*ref3*/());		\n" ); //$NON-NLS-1$
      	writer.write("}									\n" ); //$NON-NLS-1$
      	writer.write("using N::fz/*ref4*/;				\n" ); //$NON-NLS-1$
        String source = writer.toString();
        IFile k = importFile( "FunctionReference.cpp", source ); //$NON-NLS-1$
        ICElement[] list = {cproject.findElement(new Path("FunctionReference.cpp"))}; //$NON-NLS-1$
        ICSearchPattern pattern=SearchEngine.createSearchPattern("f*", FUNCTION, REFERENCES, true); //$NON-NLS-1$
        
        Set matches = search( pattern,list);
		assertMatch( matches, k, source.indexOf( "fz/*ref1*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, k, source.lastIndexOf( "fg/*ref2*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, k, source.indexOf( "fw/*ref3*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, k, source.lastIndexOf( "fz/*ref4*/" )  ); //$NON-NLS-1$ 
		assertEquals( 4, matches.size());
		
   	}
	// test SE53 // SE60c
    public void testFieldDeclaration() throws Exception {
        Writer writer = new StringWriter();
    	writer.write(" namespace N2 {							\n" ); //$NON-NLS-1$
    	writer.write("	struct linkedlist1{ 					\n" ); //$NON-NLS-1$
    	writer.write("		long field1_t;					    \n" ); //$NON-NLS-1$
    	writer.write("		struct linkedlist1 *field2;	  		\n" ); //$NON-NLS-1$
    	writer.write("		static int sfield;	  				\n" ); //$NON-NLS-1$
    	writer.write("	};										\n" ); //$NON-NLS-1$
    	writer.write("	class linkedlist2{ 						\n" ); //$NON-NLS-1$
    	writer.write("		unsigned char field3;							\n" ); //$NON-NLS-1$
    	writer.write("		class linkedlist2 *field4;			\n" ); //$NON-NLS-1$
    	writer.write("	};										\n" ); //$NON-NLS-1$
    	writer.write(" }										\n" ); //$NON-NLS-1$
        writer.write(" class C { 								\n" ); //$NON-NLS-1$
    	writer.write(" 	struct {} field5;		    			\n" ); //$NON-NLS-1$
    	writer.write("  class C1 {		    					\n" ); //$NON-NLS-1$
    	writer.write("  	N2::linkedlist2 *field6;		    \n" ); //$NON-NLS-1$
    	writer.write("  };										\n" ); //$NON-NLS-1$
    	writer.write(" };										\n" ); //$NON-NLS-1$
    	String code = writer.toString();			
        IFile gh = importFile( "FieldDeclaration.h", code ); //$NON-NLS-1$
        
        ICSearchPattern pattern = SearchEngine.createSearchPattern("*", FIELD, DECLARATIONS, true); //$NON-NLS-1$
        ICElement[] list = new ICElement[1];
        list[0]=cproject.getCProject();
        Set matches = search( pattern,list);
        assertMatch( matches, gh, code.indexOf( "field1" )  ); //$NON-NLS-1$ 
		assertMatch( matches, gh, code.indexOf( "field2" )  ); //$NON-NLS-1$ 
		assertMatch( matches, gh, code.indexOf( "sfield" )  ); //$NON-NLS-1$ 
		assertMatch( matches, gh, code.indexOf( "field3" )  ); //$NON-NLS-1$ 
		assertMatch( matches, gh, code.indexOf( "field4" )  ); //$NON-NLS-1$ 
		assertMatch( matches, gh, code.indexOf( "field5" )  ); //$NON-NLS-1$ 
		assertMatch( matches, gh, code.indexOf( "field6" )  ); //$NON-NLS-1$ 
		assertEquals( 7, matches.size());
		
   	}
    public void testBitFieldDeclaration() throws Exception {
        Writer writer = new StringWriter();
    	writer.write("struct Date	{											\n" ); //$NON-NLS-1$
    	writer.write("    unsigned nWeekDay  : 3;    // 0..7   (3 bits)			\n" ); //$NON-NLS-1$
    	writer.write("    unsigned nMonthDay : 6;    // 0..31  (6 bits)			\n" ); //$NON-NLS-1$
    	writer.write("    unsigned           : 0;    // Force alignment to next boundary.\n" ); //$NON-NLS-1$
    	writer.write("    unsigned nMonth    : 5;    // 0..12  (5 bits)			\n" ); //$NON-NLS-1$
    	writer.write("    unsigned nYear     : 8;    // 0..100 (8 bits)			\n" ); //$NON-NLS-1$
		writer.write("}; 														\n" ); //$NON-NLS-1$
		String code = writer.toString();			
        IFile cpp = importFile( "BitFieldDeclaration.cpp", code ); //$NON-NLS-1$

        ICSearchPattern pattern = SearchEngine.createSearchPattern("n*", FIELD, DECLARATIONS, true); //$NON-NLS-1$
        ICElement[] list = new ICElement[1];
        list[0]=cproject.getCProject();
        Set matches = search( pattern,list);
        //currently anon bitfields are not found (80166)
        assertEquals( 4, matches.size());
        assertMatch( matches, cpp, code.indexOf( "nWeekDay" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, code.indexOf( "nMonthDay" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, code.indexOf( "nMonth" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, code.indexOf( "nYear" )  ); //$NON-NLS-1$ 
	}

    // test SE63b var field defn
    public void testFieldDefinition() throws Exception {

    	Writer writer = new StringWriter();
    	writer.write("    enum ZooLocs {ZOOANIMAL, BEAR, PANDA};\n" ); //$NON-NLS-1$
    	writer.write("    namespace zoo {						\n" ); //$NON-NLS-1$
    	writer.write("    class ZooAnimal{						\n" ); //$NON-NLS-1$
    	writer.write("    public:								\n" ); //$NON-NLS-1$
    	writer.write("    	ZooAnimal(char *s = \"ZooAnimal\");	\n" ); //$NON-NLS-1$
    	writer.write("    protected:							\n" ); //$NON-NLS-1$
    	writer.write("    	char *name/*def*/;					\n" ); //$NON-NLS-1$
    	writer.write("    	ZooAnimal *next/*def*/;				\n" ); //$NON-NLS-1$
    	writer.write("    };									\n" ); //$NON-NLS-1$
    	writer.write("    class Mammal{							\n" ); //$NON-NLS-1$
    	writer.write("    protected:							\n" ); //$NON-NLS-1$
    	writer.write("    	bool isCarnivore/*def*/;			\n" ); //$NON-NLS-1$
    	writer.write("    };									\n" ); //$NON-NLS-1$
    	writer.write("    class Bear : public ZooAnimal, Mammal{\n" ); //$NON-NLS-1$
    	writer.write("    public:  static int number;			\n" ); //$NON-NLS-1$
    	writer.write("    protected:							\n" ); //$NON-NLS-1$
    	writer.write("    	ZooLocs zooArea/*def*/;				\n" ); //$NON-NLS-1$
    	writer.write("      class Beez{							\n" ); //$NON-NLS-1$
    	writer.write("        const double dutch/*def*/;		\n" ); //$NON-NLS-1$
    	writer.write("      };									\n" ); //$NON-NLS-1$
    	writer.write("    };									\n" ); //$NON-NLS-1$
    	writer.write("    }										\n" ); //$NON-NLS-1$
    	writer.write("    int zoo::Bear::number/*def*/=3;									\n" ); //$NON-NLS-1$
    	String code = writer.toString();			
        IFile z = importFile( "FieldDefinition.cpp", code ); //$NON-NLS-1$
               
        ICSearchPattern pattern = SearchEngine.createSearchPattern("*", FIELD, DEFINITIONS, true); //$NON-NLS-1$
        ICElement[] list = new ICElement[1];
        list[0]=cproject.getCProject();
        Set matches = search( pattern,list);
        assertEquals( 6, matches.size());
		assertMatch( matches, z, code.indexOf( "name/*def*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, z, code.indexOf( "next/*def*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, z, code.indexOf( "isCarnivore/*def*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, z, code.indexOf( "zooArea/*def*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, z, code.indexOf( "dutch/*def*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, z, code.indexOf( "number/*def*/" )  ); //$NON-NLS-1$ 
}
    public void testFieldReference() throws Exception {
    	Writer writer = new StringWriter();
        writer.write("class Mammal {//decl						\n" ); //$NON-NLS-1$
        writer.write("	public:									\n" ); //$NON-NLS-1$
        writer.write("	Mammal(bool b): isCarnivore/*ref*/(b){}	\n" ); //$NON-NLS-1$
        writer.write("	private:								\n" ); //$NON-NLS-1$
        writer.write("	bool isCarnivore;						\n" ); //$NON-NLS-1$
        writer.write("};										\n" ); //$NON-NLS-1$
        writer.write("class Bear : Mammal{						\n" ); //$NON-NLS-1$
        writer.write("public:									\n" ); //$NON-NLS-1$
        writer.write("	Bear(int s,int t): Mammal(true) { 		\n" ); //$NON-NLS-1$
        writer.write("    biotics.matureYears=s;//ref			\n" ); //$NON-NLS-1$
        writer.write("  }										\n" ); //$NON-NLS-1$
        writer.write("  struct {								\n" ); //$NON-NLS-1$
    	writer.write("    int matureYears;						\n" ); //$NON-NLS-1$
        writer.write("  } biotics;								\n" ); //$NON-NLS-1$
        writer.write("};										\n" ); //$NON-NLS-1$
        writer.write("struct bioticPotential {					\n" ); //$NON-NLS-1$
    	writer.write("  int litterSize;							\n" ); //$NON-NLS-1$
        writer.write("	int matureYears;						\n" ); //$NON-NLS-1$
        writer.write("};										\n" ); //$NON-NLS-1$
        writer.write("int main(int argc, char **argv) {			\n" ); //$NON-NLS-1$
        writer.write(" 	bioticPotential brownbear_bt = {1, 3};	\n" ); //$NON-NLS-1$
        writer.write("	Bear *grizzly = new Bear(4,3);			\n" ); //$NON-NLS-1$
        writer.write("    	brownbear_bt.matureYears = grizzly->biotics.matureYears;//ref\n" ); //$NON-NLS-1$
        writer.write("} 										\n" ); 	//$NON-NLS-1$
        String code = writer.toString();
        IFile b = importFile( "FieldReference.cpp", code ); //$NON-NLS-1$
        //vp1 class field ref in constructor initializer list
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "isCarnivore", FIELD, REFERENCES, true ); //$NON-NLS-1$
		Set matches = search( pattern );
		assertEquals( 1, matches.size() );
		assertMatch( matches, b, code.indexOf( "isCarnivore/*ref*/" )  ); //$NON-NLS-1$ 
		//vp2 struct field ref in method to anon struct, ref in function on rhs
		pattern = SearchEngine.createSearchPattern( "biotics", FIELD, REFERENCES, true ); //$NON-NLS-1$
		matches = search( pattern );
		assertEquals( 2, matches.size() );
		assertMatch( matches, b, code.indexOf( "biotics.matureYears=s;" )  ); //$NON-NLS-1$ 
		assertMatch( matches, b, code.indexOf( "biotics.matureYears;//ref" )  ); //$NON-NLS-1$ 
		
    }
    //76203
	public void testNestedFieldReference() throws Exception {
		Writer writer = new StringWriter();
	    writer.write("class Bear {								\n" ); //$NON-NLS-1$
	    writer.write("public:									\n" ); //$NON-NLS-1$
	    writer.write("	Bear(int s,int t) { 					\n" ); //$NON-NLS-1$
	    writer.write("    biotics.matureYears=s;//ref			\n" ); //$NON-NLS-1$
	    writer.write("    biotics.littersPerYear=t;				\n" ); //$NON-NLS-1$
	    writer.write("  }										\n" ); //$NON-NLS-1$
	    writer.write("  struct {								\n" ); //$NON-NLS-1$
		writer.write("    int matureYears;						\n" ); //$NON-NLS-1$
	    writer.write("	  int littersPerYear; 					\n" ); //$NON-NLS-1$
	    writer.write("  } biotics;								\n" ); //$NON-NLS-1$
	    writer.write("};										\n" ); //$NON-NLS-1$
	    writer.write("struct bioticPotential {					\n" ); //$NON-NLS-1$
		writer.write("  int litterSize;							\n" ); //$NON-NLS-1$
	    writer.write("	int matureYears;						\n" ); //$NON-NLS-1$
	    writer.write("};										\n" ); //$NON-NLS-1$
	    writer.write("int main(int argc, char **argv) {			\n" ); //$NON-NLS-1$
	    writer.write(" 	bioticPotential brownbear_bt = {1, 3};	\n" ); //$NON-NLS-1$
	    writer.write("	Bear *grizzly = new Bear(4,3);			\n" ); //$NON-NLS-1$
	    writer.write("    	brownbear_bt.matureYears = grizzly->biotics.matureYears;//ref\n" ); //$NON-NLS-1$
	    writer.write("} 										\n" ); //$NON-NLS-1$
	    String code = writer.toString();
	    IFile b = importFile( "NestedFieldReference.cpp", code ); //$NON-NLS-1$
	    
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "matureYears", FIELD, REFERENCES, true ); //$NON-NLS-1$
		Set matches = search( pattern );
		assertEquals( 3, matches.size() );
		assertMatch( matches, b, code.indexOf( "matureYears = grizzly" )  ); //$NON-NLS-1$ 
		assertMatch( matches, b, code.indexOf( "matureYears;//ref" )  ); //$NON-NLS-1$ 
		assertMatch( matches, b, code.indexOf( "matureYears=s;//ref,ref" )  ); //$NON-NLS-1$ /
			
	}
    public void testVarDeclaration() throws Exception {
        Writer writer = new StringWriter();
    	writer.write(" namespace N2 {							\n" ); //$NON-NLS-1$
    	writer.write("	class linkedlist2{ 						\n" ); //$NON-NLS-1$
    	writer.write("		int field3;							\n" ); //$NON-NLS-1$
    	writer.write("		class linkedlist2 *field4;			\n" ); //$NON-NLS-1$
    	writer.write("	};										\n" ); //$NON-NLS-1$
    	writer.write("  class linkedlist2 var1;					\n" ); //$NON-NLS-1$
    	writer.write(" }										\n" ); //$NON-NLS-1$
    	writer.write("extern bool var2;							\n" ); //$NON-NLS-1$
    	writer.write("unsigned int var3(1024);					\n" ); //$NON-NLS-1$
    	writer.write("unsigned long long var4=123456,			\n" ); //$NON-NLS-1$
    	writer.write("		var5=1234567;						\n" ); //$NON-NLS-1$
    	writer.write("void f(){									\n" ); //$NON-NLS-1$
    	writer.write("		volatile unsigned long var6;		\n" ); //$NON-NLS-1$
    	writer.write("  	for (register int var7=0; var7<10000; var7++){}	\n" ); //$NON-NLS-1$
    	writer.write(" }										\n" ); //$NON-NLS-1$
    	String code = writer.toString();			
        IFile gh = importFile( "VarDeclaration.h", code ); //$NON-NLS-1$
        
        ICSearchPattern pattern=SearchEngine.createSearchPattern("*", VAR, DECLARATIONS, true); //$NON-NLS-1$
        ICElement[] list = new ICElement[1];
        list[0]=cproject.getCProject();
        Set matches = search( pattern,list);
        assertEquals( 7, matches.size());
		assertMatch( matches, gh, code.indexOf( "var1" )  ); //$NON-NLS-1$ 
		assertMatch( matches, gh, code.indexOf( "var2" )  ); //$NON-NLS-1$ 
		assertMatch( matches, gh, code.indexOf( "var3" )  ); //$NON-NLS-1$ 
		assertMatch( matches, gh, code.indexOf( "var4" )  ); //$NON-NLS-1$ 
		assertMatch( matches, gh, code.indexOf( "var5" )  ); //$NON-NLS-1$ 
		assertMatch( matches, gh, code.indexOf( "var6" )  ); //$NON-NLS-1$ 
		assertMatch( matches, gh, code.indexOf( "var7" )  ); //$NON-NLS-1$ 
	}
	//  SE63a  var field defn 
	public void testVarDefinition() throws Exception {
	
	   	Writer writer = new StringWriter();
	    writer.write("namespace zoo {				\n" ); //$NON-NLS-1$
	    writer.write("  int var1/*def*/;			\n" ); //$NON-NLS-1$
	    writer.write("}								\n" ); //$NON-NLS-1$
	    writer.write("extern int var;				\n" ); //$NON-NLS-1$
	    writer.write("extern char var2/*def*/=3;	\n" ); //$NON-NLS-1$
	    writer.write("int& var3/*def*/=zoo::var1;	\n" ); //$NON-NLS-1$
	    writer.write("long long var4/*def*/;		\n" ); //$NON-NLS-1$
	    writer.write("void f(){						\n" ); //$NON-NLS-1$
	    writer.write("	short var5/*def*/=0xabcd;	\n" ); //$NON-NLS-1$
	    writer.write("  const float var6/*def*/=1.5;\n" ); //$NON-NLS-1$
	    writer.write("}								\n" ); //$NON-NLS-1$
	    writer.write("class A{						\n" ); //$NON-NLS-1$
	    writer.write("  void f(){					\n" ); //$NON-NLS-1$
	    writer.write("	  long double** var7/*def*/;	\n" ); //$NON-NLS-1$
	    writer.write("    float var8/*def*/=1.5;	\n" ); //$NON-NLS-1$
	    writer.write("  }							\n" ); //$NON-NLS-1$
	    writer.write("};							\n" ); //$NON-NLS-1$
	    String code = writer.toString();			
	    IFile cpp = importFile( "VarDefinition.cpp", code ); //$NON-NLS-1$
	    ICSearchPattern pattern = SearchEngine.createSearchPattern("*", VAR, DEFINITIONS, true); //$NON-NLS-1$
	    ICElement[] list = new ICElement[1];
        list[0]=cproject.getCProject();
        Set matches = search( pattern,list);
	    assertEquals( 8, matches.size());
		assertMatch( matches, cpp, code.indexOf( "var1" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, code.indexOf( "var2" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, code.indexOf( "var3" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, code.indexOf( "var4" )  ); //$NON-NLS-1$
		assertMatch( matches, cpp, code.indexOf( "var5" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, code.indexOf( "var6" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, code.indexOf( "var7" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, code.indexOf( "var8" )  ); //$NON-NLS-1$
    }
    //  test SE60b
    public void testVarReference() throws Exception {
        Writer writer = new StringWriter();
    	writer.write("char * strcpy( char *, const char * );   		\n" ); //$NON-NLS-1$
    	writer.write("namespace zoo {								\n" ); //$NON-NLS-1$
   		writer.write("  int sci=1;									\n" ); //$NON-NLS-1$
    	writer.write("  class ZooAnimal{							\n" ); //$NON-NLS-1$
    	writer.write("    public:									\n" ); //$NON-NLS-1$
    	writer.write("    	ZooAnimal(char *s = \"ZooAnimal\"){}	\n" ); //$NON-NLS-1$
    	writer.write("    protected:								\n" ); //$NON-NLS-1$
    	writer.write("    	char *name;								\n" ); //$NON-NLS-1$
    	writer.write("  };											\n" ); //$NON-NLS-1$	
    	writer.write("  class Bear : public ZooAnimal{				\n" ); //$NON-NLS-1$
    	writer.write("    public:									\n" ); //$NON-NLS-1$
    	writer.write("    	Bear(char *sci = \"Ursidae\" );			\n" ); //$NON-NLS-1$
    	writer.write("    protected:								\n" ); //$NON-NLS-1$
    	writer.write("    	char *sciName;							\n" ); //$NON-NLS-1$
    	writer.write("  };											\n" ); //$NON-NLS-1$
        writer.write("  Bear::Bear(char *sci)	{					\n" ); //$NON-NLS-1$
        writer.write("    	strcpy (sciName, sci/*ref1*/);			\n" ); //$NON-NLS-1$
        writer.write("  	int j=zoo::sci/*ref2*/;					\n" ); //$NON-NLS-1$
    	writer.write("  }											\n" ); //$NON-NLS-1$
    	writer.write(" }											\n" ); //$NON-NLS-1$
    	writer.write("void f() {									\n" ); //$NON-NLS-1$
    	writer.write("  int sci;									\n" ); //$NON-NLS-1$
    	writer.write("  sci/*ref3*/++;								\n" ); //$NON-NLS-1$
    	writer.write("  --sci/*ref4*/;								\n" ); //$NON-NLS-1$
    	writer.write("}												\n" ); //$NON-NLS-1$
        String code = writer.toString();			
        IFile z = importFile( "VarReference.cpp", code ); //$NON-NLS-1$
              
        ICSearchPattern pattern=SearchEngine.createSearchPattern("sci", VAR, REFERENCES, true); //$NON-NLS-1$
		Set matches = search(pattern);
        assertEquals( 4, matches.size());
		assertMatch( matches, z, code.indexOf( "sci/*ref1*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, z, code.indexOf( "sci/*ref2*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, z, code.indexOf( "sci/*ref3*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, z, code.indexOf( "sci/*ref4*/" )  ); //$NON-NLS-1$ 
    	
    }
    // 75901 //  test SE60f1 var all
    public void testVarDeclarationArgument() throws Exception {
        Writer writer = new StringWriter();
    	writer.write("namespace zoo {							\n" ); //$NON-NLS-1$				
    	writer.write(" class Bear {								\n" ); //$NON-NLS-1$				
    	writer.write("   public:								\n" ); //$NON-NLS-1$					
    	writer.write("   	Bear( char *s/*decl1*/ = \"Bear\" );\n" ); //$NON-NLS-1$    								
    	writer.write(" };										\n" ); //$NON-NLS-1$		
    	writer.write("  Bear::Bear(char *s/*decl2*/)			\n" ); //$NON-NLS-1$
    	writer.write("   	: ZooAnimal(s) {					\n" ); //$NON-NLS-1$
        writer.write("  }										\n" ); //$NON-NLS-1$						
        writer.write("}											\n" ); //$NON-NLS-1$
        String code = writer.toString();			
        IFile z = importFile( "VarDeclarationArgument.cpp", code ); //$NON-NLS-1$
        ICSearchPattern pattern=SearchEngine.createSearchPattern("s", VAR, DECLARATIONS, true); //$NON-NLS-1$
        Set matches = search(pattern);
        assertMatch( matches, z, code.indexOf( "s/*decl1*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, z, code.indexOf( "s/*decl2*/" )  ); //$NON-NLS-1$ 
		assertEquals( 2, matches.size());
		  
   	}
    //  72735 test SE60f2 var all
    public void testVarReferenceInitializer() throws Exception{
    	Writer writer = new StringWriter();
    	writer.write("enum ZooLocs {ZOOANIMAL, BEAR, PANDA};					\n" ); //$NON-NLS-1$
    	writer.write("namespace zoo {											\n" ); //$NON-NLS-1$
   		writer.write("  class ZooAnimal{										\n" ); //$NON-NLS-1$
    	writer.write("    public:												\n" ); //$NON-NLS-1$
    	writer.write("    	ZooAnimal(char *s = \"ZooAnimal\");					\n" ); //$NON-NLS-1$
    	writer.write("  };														\n" ); //$NON-NLS-1$	
    	writer.write("  class Bear : public ZooAnimal{							\n" ); //$NON-NLS-1$
    	writer.write("    public:												\n" ); //$NON-NLS-1$
    	writer.write("    	Bear( char *s = \"Bear\", ZooLocs loc = \"BEAR\" );	\n" ); //$NON-NLS-1$
    	writer.write("    protected:											\n" ); //$NON-NLS-1$
    	writer.write("    	ZooLocs zooArea;									\n" ); //$NON-NLS-1$
    	writer.write("  };														\n" ); //$NON-NLS-1$
        writer.write("  Bear::Bear(char *s, ZooLocs loc)						\n" ); //$NON-NLS-1$
        writer.write("   	: ZooAnimal(s), zooArea (loc/*ref*/) {}				\n" ); //$NON-NLS-1$
    	writer.write("  }														\n" ); //$NON-NLS-1$
    	writer.write(" }														\n" ); //$NON-NLS-1$
        String code = writer.toString();			
        IFile z = importFile( "VarReferenceInitializer.cpp", code ); //$NON-NLS-1$
        ICSearchPattern pattern=SearchEngine.createSearchPattern("loc", VAR, REFERENCES, true); //$NON-NLS-1$
        Set matches = search(pattern);
        assertEquals( 1, matches.size());
		assertMatch( matches, z, code.indexOf( "loc/*ref*/" )  ); //$NON-NLS-1$ 
	
    }
    // 75901 //  test SE60f1 var all
    public void testVarDefinitionArgument() throws Exception {
        Writer writer = new StringWriter();
    	writer.write(" class C {							\n" ); //$NON-NLS-1$				
    	writer.write("   public:							\n" ); //$NON-NLS-1$					
    	writer.write("   	C( char *s/*def1*/ = \"Bear\" );\n" ); //$NON-NLS-1$    								
    	writer.write(" };									\n" ); //$NON-NLS-1$		
    	writer.write("  C::C(char *s/*def2*/){}				\n" ); //$NON-NLS-1$
    	String code = writer.toString();			
        IFile z = importFile( "VarDeclarationArgument.cpp", code ); //$NON-NLS-1$
        ICSearchPattern pattern=SearchEngine.createSearchPattern("s", VAR, DEFINITIONS, true); //$NON-NLS-1$
        Set matches = search(pattern);
        assertEquals( 2, matches.size());
		assertMatch( matches, z, code.indexOf( "s/*def1*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, z, code.indexOf( "s/*def2*/" )  ); //$NON-NLS-1$ 
		  
   	}
    
    // test 85a union declaration
    public void testUnionDeclaration() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("enum Tokentype {ID, ICONST};		\n" ); //$NON-NLS-1$
    	writer.write("class Token {						\n" ); //$NON-NLS-1$
    	writer.write("  public:							\n" ); //$NON-NLS-1$
    	writer.write("  Tokentype tok;					\n" ); //$NON-NLS-1$
    	writer.write("  union { //anon field	 		\n" ); //$NON-NLS-1$
    	writer.write("	char *sval;						\n" ); //$NON-NLS-1$
    	writer.write("	int ival;						\n" ); //$NON-NLS-1$
    	writer.write("	double dval;		   			\n" ); //$NON-NLS-1$    	
    	writer.write("	} val1;							\n" ); //$NON-NLS-1$
    	writer.write("};								\n" ); //$NON-NLS-1$
    	writer.write("static union { //anon static		\n" ); //$NON-NLS-1$
    	writer.write("	char *sval;						\n" ); //$NON-NLS-1$
    	writer.write("	int ival;						\n" ); //$NON-NLS-1$
    	writer.write("	double dval;					\n" ); //$NON-NLS-1$       	
    	writer.write("};								\n" ); //$NON-NLS-1$
    	writer.write("union TokenValue { 				\n" ); //$NON-NLS-1$	 
    	writer.write("TokenValue (int ix) {ival = ix;}	\n" ); //$NON-NLS-1$
    	writer.write("TokenValue (char *s) {sval = s;}	\n" ); //$NON-NLS-1$
    	writer.write("TokenValue () {}  				\n" ); //$NON-NLS-1$
    	writer.write("	char *sval;						\n" ); //$NON-NLS-1$
    	writer.write("	int ival;						\n" ); //$NON-NLS-1$
    	writer.write("	double dval;					\n" ); //$NON-NLS-1$	       	
    	writer.write("};								\n" ); //$NON-NLS-1$
    	writer.write("TokenValue val2;     				\n" ); //$NON-NLS-1$   
    	String code = writer.toString();
        IFile uh = importFile( "UnionDeclaration.h", code ); //$NON-NLS-1$
            
        ICSearchPattern pattern = SearchEngine.createSearchPattern( "*", UNION, DECLARATIONS, true ); //$NON-NLS-1$
        ICElement[] list = new ICElement[1];
        list[0]=cproject.getCProject();
        Set matches = search( pattern,list );
    		
    	assertEquals( 3, matches.size() );
    	assertMatch( matches, uh, code.indexOf( "union { //anon field" )  ); //$NON-NLS-1$ 
    	assertMatch( matches, uh, code.indexOf( "union { //anon static" )  ); //$NON-NLS-1$ 
    	assertMatch( matches, uh, code.indexOf( "TokenValue" )  ); //$NON-NLS-1$ 
    	
    }
    public void testUnionReference() throws Exception {
    	Writer writer = new StringWriter();
    	writer = new StringWriter();
    	writer.write("enum Tokentype {ID, ICONST};		\n" ); //$NON-NLS-1$
    	writer.write("class Token {						\n" ); //$NON-NLS-1$
    	writer.write("  public:							\n" ); //$NON-NLS-1$
    	writer.write("  Tokentype tok;					\n" ); //$NON-NLS-1$
    	writer.write("union TokenValue { 				\n" ); //$NON-NLS-1$	 
    	writer.write("TokenValue (int ix) {ival = ix;}	\n" ); //$NON-NLS-1$
    	writer.write("TokenValue (char *s) {sval = s;}	\n" ); //$NON-NLS-1$
    	writer.write("TokenValue () {}  				\n" ); //$NON-NLS-1$
    	writer.write("	char *sval;						\n" ); //$NON-NLS-1$
    	writer.write("	int ival;						\n" ); //$NON-NLS-1$
    	writer.write("	double dval;					\n" ); //$NON-NLS-1$	       	
    	writer.write("};								\n" ); //$NON-NLS-1$
    	writer.write("TokenValue val2;     				\n" ); //$NON-NLS-1$   
    	String header = writer.toString();
        importFile( "UnionReference.h", header ); //$NON-NLS-1$
        writer = new StringWriter();
        writer.write("#include \"UnionReference.h\"		\n" ); //$NON-NLS-1$
    	writer.write("union Uu{int i; double d;};		\n" ); //$NON-NLS-1$
    	writer.write("void f(TokenValue/*ref1*/ t){};	\n" ); //$NON-NLS-1$
    	writer.write("void g(){							\n" ); //$NON-NLS-1$
    	writer.write("  f(TokenValue/*ref2*/(3));		\n" ); //$NON-NLS-1$
    	writer.write("  char *p=\"hello\";				\n" ); //$NON-NLS-1$
    	writer.write("  f(p);							\n" ); //$NON-NLS-1$
    	writer.write("  TokenValue/*ref3*/ t=(TokenValue/*ref4*/) p;\n" ); //$NON-NLS-1$
    	writer.write("}									\n" ); //$NON-NLS-1$
        writer.write("Uu/*ref5*/ t;					\n" ); //$NON-NLS-1$
        String source = writer.toString();
    	IFile cpp = importFile( "UnionReference.cpp", source ); //$NON-NLS-1$
    	ICElement[] list = {cproject.findElement(new Path("UnionReference.cpp"))}; //$NON-NLS-1$
        ICSearchPattern pattern = SearchEngine.createSearchPattern( "*", UNION, REFERENCES, true ); //$NON-NLS-1$
    	Set matches = search( pattern, list );
    		
    	assertEquals( 5, matches.size() );
    	assertMatch( matches, cpp, source.indexOf( "TokenValue/*ref1*/" )  ); //$NON-NLS-1$ 
    	assertMatch( matches, cpp, source.indexOf( "TokenValue/*ref2*/" )  ); //$NON-NLS-1$
    	assertMatch( matches, cpp, source.indexOf( "TokenValue/*ref3*/" )  ); //$NON-NLS-1$ 
    	assertMatch( matches, cpp, source.indexOf( "TokenValue/*ref4*/" )  ); //$NON-NLS-1$
    	assertMatch( matches, cpp, source.indexOf( "Uu/*ref5*/" )  ); //$NON-NLS-1$ 
    	
    }
    // test SE44
    public void testEnumerationDeclaration() throws Exception {
	    Writer writer = new StringWriter();
	    //next line commented because it affects testEnumeratorDeclarationCase
    	//writer.write("enum EE/*def1*/ {A0, A1};				\n" ); //$NON-NLS-1$
    	writer.write("class B { 							\n" ); //$NON-NLS-1$
    	writer.write("	enum EE/*def2*/ {B0, B1};			\n" ); //$NON-NLS-1$
    	writer.write("  B(){								\n" ); //$NON-NLS-1$
    	writer.write("		enum EE/*def3*/ {M0, M1};		\n" ); //$NON-NLS-1$
    	writer.write("  }									\n" ); //$NON-NLS-1$
    	writer.write("	void foo();							\n" ); //$NON-NLS-1$
    	writer.write("};									\n" ); //$NON-NLS-1$
    	String header = writer.toString();
        IFile h = importFile( "EnumerationDeclaration.h", header ); //$NON-NLS-1$
        writer = new StringWriter();
        writer.write("#include \"EnumerationDeclaration.h\"	\n" ); //$NON-NLS-1$
    	writer.write("void B::foo() {						\n" ); //$NON-NLS-1$
    	writer.write("		enum EE/*def4*/ {F0, F1};		\n" ); //$NON-NLS-1$
    	writer.write("		enum EE ee;						\n" ); //$NON-NLS-1$
    	writer.write("};									\n" ); //$NON-NLS-1$
    	writer.write("void boo() {							\n" ); //$NON-NLS-1$
    	writer.write("		enum EE/*def5*/ {G0, G1};		\n" ); //$NON-NLS-1$
    	writer.write("};									\n" ); //$NON-NLS-1$
        String source = writer.toString();			
        IFile cpp = importFile( "EnumerationDeclaration.cpp", source ); //$NON-NLS-1$
        //vp1 global,class,constructor; working set
        ICElement[] list = {cproject.findElement(new Path("EnumerationDeclaration.h"))}; //$NON-NLS-1$
        ICSearchPattern pattern = SearchEngine.createSearchPattern( "EE", ENUM, DECLARATIONS, true ); //$NON-NLS-1$
		Set matches = search( pattern, list );
		assertEquals( 2, matches.size() );
		//if including first line in snippet, then include this vp and change 2 to 3 results
		//assertMatch( matches, h, header.indexOf( "EE/*def1*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, h, header.indexOf( "EE/*def2*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, h, header.indexOf( "EE/*def3*/" )  ); //$NON-NLS-1$ 
		//vp2 method,function; working set
		list[0] = cproject.findElement(new Path("EnumerationDeclaration.cpp")); //$NON-NLS-1$
		matches = search( pattern, list );
		assertEquals( 2, matches.size() );
		assertMatch( matches, cpp, source.indexOf( "EE/*def4*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, source.indexOf( "EE/*def5*/" )  ); //$NON-NLS-1$ 
    }
    public void testEnumerationReference() throws Exception {
	    Writer writer = new StringWriter();
	    //next line commented because it affects testEnumeratorDeclarationCase
    	//writer.write("enum EE {A0, A1};				\n" ); //$NON-NLS-1$
    	writer.write("class B { 					\n" ); //$NON-NLS-1$
    	writer.write("	enum EE {B0, B1};			\n" ); //$NON-NLS-1$
    	writer.write("  B(){						\n" ); //$NON-NLS-1$
    	writer.write("  }							\n" ); //$NON-NLS-1$
    	writer.write("	EE/*ref8*/ bee;				\n" ); //$NON-NLS-1$
    	writer.write("	void foo();					\n" ); //$NON-NLS-1$
    	writer.write("};							\n" ); //$NON-NLS-1$
    	writer.write("B::EE/*ref2*/ eieio;			\n" ); //$NON-NLS-1$
    	String header = writer.toString();
        IFile h = importFile( "EnumerationReference.h", header ); //$NON-NLS-1$
        writer = new StringWriter();
        writer.write("#include \"EnumerationReference.h\"	\n" ); //$NON-NLS-1$
    	writer.write("void B::foo() {						\n" ); //$NON-NLS-1$
    	writer.write("		enum EE {F0, F1};				\n" ); //$NON-NLS-1$
    	writer.write("		enum EE/*ref3*/ ib4e;			\n" ); //$NON-NLS-1$
    	writer.write("}										\n" ); //$NON-NLS-1$
    	writer.write("void boo(B::EE/*ref4*/ e) {			\n" ); //$NON-NLS-1$
    	writer.write("		enum EE {G0, G1};				\n" ); //$NON-NLS-1$
    	//if including first line in snippet, then include the next line too
		//writer.write("		::EE/*ref5*/ f;				\n" ); //$NON-NLS-1$
    	writer.write("		EE/*ref6*/ i=(EE/*ref7*/)2;		\n" ); //$NON-NLS-1$
    	writer.write("}										\n" ); //$NON-NLS-1$
        String source = writer.toString();			
        IFile cpp = importFile( "EnumerationReference.cpp", source ); //$NON-NLS-1$
        //vp1 global and class reference; working set
        ICElement[] list = {cproject.findElement(new Path("EnumerationReference.h"))}; //$NON-NLS-1$
        ICSearchPattern pattern = SearchEngine.createSearchPattern( "EE", ENUM, REFERENCES, true ); //$NON-NLS-1$
        Set matches = search( pattern, list );
		assertEquals( 2, matches.size() );
		assertMatch( matches, h, header.indexOf( "EE/*ref2*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, h, header.indexOf( "EE/*ref8*/" )  ); //$NON-NLS-1$ 
		//vp2 reference in method,function,cast; working set
		list[0] = cproject.findElement(new Path("EnumerationReference.cpp")); //$NON-NLS-1$
		matches = search( pattern, list );
		assertEquals( 4, matches.size() );
		//if including first line in snippet, then include this vp and change 4 to 5 results
		//assertMatch( matches, cpp, source.indexOf( "EE/*ref5*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, source.indexOf( "EE/*ref3*/" )  ); //$NON-NLS-1$
		assertMatch( matches, cpp, source.indexOf( "EE/*ref4*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, source.indexOf( "EE/*ref6*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, source.indexOf( "EE/*ref7*/" )  ); //$NON-NLS-1$ 
		//vp3 class-qualified reference in a function argument and class declaration; workspace
		pattern = SearchEngine.createSearchPattern( "B::EE", ENUM, REFERENCES, true ); //$NON-NLS-1$
		matches = search( pattern );
		assertEquals( 3, matches.size() );
		assertMatch( matches, h, header.indexOf( "EE/*ref2*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, h, header.indexOf( "EE/*ref8*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, cpp, source.indexOf( "EE/*ref4*/" )  ); //$NON-NLS-1$ 
		
    }
    public void testEnumerationReferenceGlobal() throws Exception {
	    Writer writer = new StringWriter();
    	writer.write("enum E{E0};				\n" ); //$NON-NLS-1$
    	writer.write("void f() { 				\n" ); //$NON-NLS-1$
    	writer.write("	enum E{E1};				\n" ); //$NON-NLS-1$
    	writer.write("   E e;					\n" ); //$NON-NLS-1$
    	writer.write("}							\n" ); //$NON-NLS-1$
    	writer.write("E/*ref*/ f; 				\n" ); //$NON-NLS-1$
    	String source = writer.toString();
        IFile cpp = importFile( "EnumerationReferenceGlobal.cpp", source ); //$NON-NLS-1$
        //vp1 global and class reference; working set
        ICSearchPattern pattern = SearchEngine.createSearchPattern( "::E", ENUM, REFERENCES, true ); //$NON-NLS-1$
		Set matches = search( pattern );
		assertEquals( 1, matches.size() );
		assertMatch( matches, cpp, source.indexOf( "E/*ref*/" )  ); //$NON-NLS-1$ 
    }
    public void testEnumeratorDeclaration() throws Exception {
        Writer writer = new StringWriter();
        	writer.write("enum ZooLocs {ZOOANIMAL, BEAR/*decl1*/};			\n" ); //$NON-NLS-1$
        	writer.write("class Bear {										\n" ); //$NON-NLS-1$
        	writer.write("    public:										\n" ); //$NON-NLS-1$
        	writer.write("		enum BType{BEAR/*decl2*/, PANDA/*decl3*/};	\n" ); //$NON-NLS-1$
        	writer.write("    	Bear(ZooLocs loc = ::BEAR );				\n" ); //$NON-NLS-1$
        	writer.write("};												\n" ); //$NON-NLS-1$
            String code = writer.toString();			
            IFile z = importFile( "EnumeratorDeclaration.cpp", code ); //$NON-NLS-1$
            ICSearchPattern pattern = SearchEngine.createSearchPattern( "BEAR", ENUMTOR, DECLARATIONS, true ); //$NON-NLS-1$
    		//vp1 enumtor in global and class scopes
            Set matches = search( pattern );
            assertEquals( 2, matches.size() );
    		assertMatch( matches, z, code.indexOf( "BEAR/*decl1*/" )  ); //$NON-NLS-1$ 
    		assertMatch( matches, z, code.indexOf( "BEAR/*decl2*/" )  ); //$NON-NLS-1$ 
    		//vp2 global enumtor with fully qualified search pattern
            pattern = SearchEngine.createSearchPattern( "::BEAR", ENUMTOR, DECLARATIONS, true ); //$NON-NLS-1$
    		matches = search( pattern );
    		assertEquals( 1, matches.size() );
    		assertMatch( matches, z, code.indexOf( "BEAR/*decl1*/" )  ); //$NON-NLS-1$ 
    		//vp3 class enumtor with fully qualified search pattern
            pattern = SearchEngine.createSearchPattern( "Bear::PANDA", ENUMTOR, DECLARATIONS, true ); //$NON-NLS-1$
    		matches = search( pattern );
    		assertEquals( 1, matches.size() );
    		assertMatch( matches, z, code.indexOf( "PANDA" )  ); //$NON-NLS-1$ 
    		
    }
	public void testEnumeratorReference() throws Exception {
	    Writer writer = new StringWriter();
	    	writer.write("enum ZooLocs {ZOOANIMAL, BEAR};			\n" ); //$NON-NLS-1$
	    	writer.write("class Bear{								\n" ); //$NON-NLS-1$
	    	writer.write("    public:								\n" ); //$NON-NLS-1$
	    	writer.write("		enum BType {BEAR=2, PANDA};			\n" ); //$NON-NLS-1$
	    	writer.write("    	Bear(ZooLocs loc = ::BEAR/*ref1*/ );\n" ); //$NON-NLS-1$
	    	writer.write("    protected:							\n" ); //$NON-NLS-1$
	    	writer.write("    	ZooLocs zooArea;					\n" ); //$NON-NLS-1$
	    	writer.write("};										\n" ); //$NON-NLS-1$
	        writer.write("Bear::Bear(ZooLocs loc){					\n" ); //$NON-NLS-1$
	        writer.write("    	zooArea=::BEAR/*ref2*/;				\n" ); //$NON-NLS-1$
	        writer.write("    	BEAR/*ref3*/;						\n" ); //$NON-NLS-1$
	        writer.write("}											\n" ); //$NON-NLS-1$
	        writer.write("void f(){									\n" ); //$NON-NLS-1$
	        writer.write(" Bear::PANDA/*ref5*/;						\n" ); //$NON-NLS-1$
	        writer.write("}											\n" ); //$NON-NLS-1$
	    	String code = writer.toString();			
	        IFile z = importFile( "EnumeratorReference.cpp", code ); //$NON-NLS-1$
	        //vp1 fully qualified, partially qualified and unqualified reference
	        ICSearchPattern pattern = SearchEngine.createSearchPattern( "BEAR", ENUMTOR, REFERENCES, true ); //$NON-NLS-1$
			Set matches = search( pattern );
			assertEquals( 3, matches.size() );
			assertMatch( matches, z, code.indexOf( "BEAR/*ref1*/" )  ); //$NON-NLS-1$ 
			assertMatch( matches, z, code.indexOf( "BEAR/*ref2*/" )  ); //$NON-NLS-1$ 
			assertMatch( matches, z, code.indexOf( "BEAR/*ref3*/" )  ); //$NON-NLS-1$ 
			pattern = SearchEngine.createSearchPattern( "Bear::PANDA", ENUMTOR, REFERENCES, true ); //$NON-NLS-1$
			matches = search( pattern );
			assertEquals( 1, matches.size() );
			assertMatch( matches, z, code.indexOf( "PANDA/*ref5*/" )  ); //$NON-NLS-1$ 
	}
    public void testEnumeratorDeclarationCase() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("enum A {B};				\n" ); //$NON-NLS-1$
    	writer.write("class C {					\n" ); //$NON-NLS-1$
    	writer.write("		enum D {B0};		\n" ); //$NON-NLS-1$
    	writer.write("};						\n" ); //$NON-NLS-1$
        String code = writer.toString();			
        IFile z = importFile( "EnumeratorDeclarationCase.cpp", code ); //$NON-NLS-1$
        //vp1 case sensitive qualified search on inside enumtor prefixed by outside enumtor
        ICSearchPattern pattern = SearchEngine.createSearchPattern( "C::B0", ENUMTOR, DECLARATIONS, true ); //$NON-NLS-1$
        Set matches = search( pattern );
        assertEquals( 1, matches.size() );
        assertMatch( matches, z, code.indexOf( "B0" )  ); //$NON-NLS-1$ 
		//vp1 case insensitive qualified search on inside enumtor prefixed by outside enumtor
        pattern = SearchEngine.createSearchPattern( "C::B0", ENUMTOR, DECLARATIONS, false ); //$NON-NLS-1$
		matches = search( pattern );
		assertEquals( 1, matches.size() );
		assertMatch( matches, z, code.indexOf( "B0" )  ); //$NON-NLS-1$ 
    }
    //typedef is not currently shown in the Search dialog
    //but are found when you select all types.
    public void testTypedefDeclaration() throws Exception {
        Writer writer = new StringWriter();
        writer.write(" class A {               					\n" ); //$NON-NLS-1$
        writer.write("    typedef struct alpha {} beta/*decl*/;	\n" ); //$NON-NLS-1$
        writer.write(" };                      					\n" ); //$NON-NLS-1$
        
        String code = writer.toString();
        IFile f = importFile( "TypedefDeclaration.cpp", code ); //$NON-NLS-1$
        
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "beta", TYPEDEF, DECLARATIONS, true ); //$NON-NLS-1$
		Set matches = search( pattern );
		
		assertEquals( 1, matches.size() );
		assertMatch( matches, f, code.indexOf( "beta/*decl*/" )  ); //$NON-NLS-1$ 
	}
    public void testMacroDeclaration() throws Exception {
        Writer writer = new StringWriter();
        writer.write("#define paster/*decl1*/( n ) printf( \"token\" #n \" = %d\", token##n )	\n" ); //$NON-NLS-1$
        writer.write("#define token9/*decl2*/ 3  												\n" ); //$NON-NLS-1$                    						
        writer.write("#define HELLO/*decl3*/													\n" ); //$NON-NLS-1$
		writer.write("#ifdef HELLO																\n" ); //$NON-NLS-1$
		writer.write(" #define HI/*decl4*/														\n" ); //$NON-NLS-1$
		writer.write("#endif																	\n" ); //$NON-NLS-1$
		writer.write("#ifdef GOODBYE															\n" ); //$NON-NLS-1$
		writer.write(" #define BYE/*decl5*/														\n" ); //$NON-NLS-1$
		writer.write("#endif																	\n" ); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile( "MacroDeclaration.cpp", header ); //$NON-NLS-1$
        //vp1 identifier as a function with arguements
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "paster", MACRO, DECLARATIONS, true ); //$NON-NLS-1$
		Set matches = search( pattern );
		assertEquals( 1, matches.size() );
		assertMatch( matches, h, header.indexOf( "paster/*decl1*/" )  ); //$NON-NLS-1$ 
		//vp2 identifier with token-string
		pattern = SearchEngine.createSearchPattern( "token9", MACRO, DECLARATIONS, true ); //$NON-NLS-1$
		matches = search( pattern );
		assertEquals( 1, matches.size() );
		assertMatch( matches, h, header.indexOf( "token9/*decl2*/" )  ); //$NON-NLS-1$ 
		//vp3 identifer without token-string
		pattern = SearchEngine.createSearchPattern( "HELLO", MACRO, DECLARATIONS, true ); //$NON-NLS-1$
		matches = search( pattern );
		assertEquals( 1, matches.size() );
		assertMatch( matches, h, header.indexOf( "HELLO/*decl3*/" )  ); //$NON-NLS-1$ 
		//vp4-identifier inside defined ifdef
		pattern = SearchEngine.createSearchPattern( "HI", MACRO, DECLARATIONS, true ); //$NON-NLS-1$
		matches = search( pattern );
		assertEquals( 1, matches.size() );
		assertMatch( matches, h, header.indexOf( "HI/*decl4*" )  ); //$NON-NLS-1$ 
		//vp5-identifier inside undefined ifdef
		pattern = SearchEngine.createSearchPattern( "BYE", MACRO, DECLARATIONS, true ); //$NON-NLS-1$
		matches = search( pattern );
		assertEquals( 0, matches.size() );
	}
	public void testMethodFieldReferenceInExpressions() throws Exception {
		Writer writer = new StringWriter();
	    writer.write("class M {						\n" );//$NON-NLS-1$
	    writer.write("	public:						\n" );//$NON-NLS-1$
	    writer.write("	int mm(){return 0;}			\n" );//$NON-NLS-1$
	    writer.write("	float fm;					\n" );//$NON-NLS-1$
	    writer.write("};							\n" );//$NON-NLS-1$
	    writer.write("class N{						\n" );//$NON-NLS-1$
	    writer.write("public:						\n" );//$NON-NLS-1$
	    writer.write("	double mn(){return 3.2;} 	\n" );//$NON-NLS-1$
	    writer.write("  static const int fn= 12;	\n" );//$NON-NLS-1$
	    writer.write("};							\n" );//$NON-NLS-1$
	    writer.write("void foo() {					\n" );//$NON-NLS-1$
	    writer.write(" 	M m; 						\n" );//$NON-NLS-1$
	    writer.write("	N *n;						\n" );//$NON-NLS-1$
	    writer.write("  double i= ((double)m.mm/*11*/()+n->fn/*21*/-m.fm/*22*/	\n" );//$NON-NLS-1$
	    writer.write("  /n->mn/*12*/())*(m.fm/*23*/*n->mn/*13*/())				\n" );//$NON-NLS-1$
	    writer.write("  &&m.mm/*14*/()||(int)n->fn/*24*/						\n" );//$NON-NLS-1$
	    writer.write("  &(int)n->mn/*15*/()|(int)m.fm/*25*/						\n" );//$NON-NLS-1$
	    writer.write("  <<(int)n->fn/*26*/>>m.mm/*16*/(); 						\n" );//$NON-NLS-1$
	    writer.write("} 														\n" );//$NON-NLS-1$
	    String code = writer.toString();
	    IFile b = importFile( "MethodFieldReferenceInExpressions.cpp", code ); //$NON-NLS-1$
	    //vp1 methods
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "m*", METHOD, REFERENCES, true ); //$NON-NLS-1$
		Set matches = search( pattern );
		assertEquals( 6, matches.size() );
		assertMatch( matches, b, code.indexOf( "mm/*11*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, b, code.indexOf( "mn/*12*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, b, code.indexOf( "mn/*13*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, b, code.indexOf( "mm/*14*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, b, code.indexOf( "mn/*15*/" )  ); //$NON-NLS-1$
		assertMatch( matches, b, code.indexOf( "mm/*16*/" )  ); //$NON-NLS-1$
		//vp2 fields
		pattern = SearchEngine.createSearchPattern( "f*", FIELD, REFERENCES, true ); //$NON-NLS-1$
		matches = search( pattern );
		assertEquals( 6, matches.size() );
		assertMatch( matches, b, code.indexOf( "fn/*21*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, b, code.indexOf( "fm/*22*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, b, code.indexOf( "fm/*23*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, b, code.indexOf( "fn/*24*/" )  ); //$NON-NLS-1$ 
		assertMatch( matches, b, code.indexOf( "fm/*22*/" )  ); //$NON-NLS-1$
		assertMatch( matches, b, code.indexOf( "fn/*26*/" )  ); //$NON-NLS-1$
	}
}
