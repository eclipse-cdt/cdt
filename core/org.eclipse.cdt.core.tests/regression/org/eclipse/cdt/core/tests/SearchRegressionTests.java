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
import org.eclipse.cdt.core.search.OrPattern;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * @author aniefer
 */
public class SearchRegressionTests extends BaseTestFramework implements ICSearchConstants, IIndexChangeListener{
    static protected ICSearchScope 			scope;
    static protected SearchEngine			searchEngine;
    static protected BasicSearchResultCollector	resultCollector;
    static private boolean indexChanged = false;
    
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
		    project.setSessionProperty( IndexManager.activationKey, new Boolean( true ) );
		} catch ( CoreException e ) { //boo
		}
        IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
        indexManager.addIndexChangeListener( this );
    }
    
    protected void tearDown() throws Exception {
        if( project == null || !project.exists() ) 
            return;
    
        IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
        indexManager.removeIndexChangeListener( this );
		try{
		    project.setSessionProperty( IndexManager.activationKey, new Boolean( false ) );
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
			ICSearchScope scope = SearchEngine.createCSearchScope(list);
			searchEngine.search( workspace, pattern, scope, resultCollector, false );
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
        suite.addTest( new SearchRegressionTests("testEnumerationDeclaration") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testNamespaceDefinition") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testMethodFuncReference") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testVarFieldDeclaration") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testMethodAll") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testVarReference") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testVarDefinition") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testFieldDefinition") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testNamespaceReference") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testUnionDeclaration") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testClassStructDeclaration") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testNamespaceDeclaration") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testClassStructReference") ); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testFieldReference") ); //$NON-NLS-1$

        suite.addTest( new FailingTest( new SearchRegressionTests("testMethodReferenceInitializer"), 76169 ) ); //defect76169 //$NON-NLS-1$ 
        suite.addTest( new FailingTest( new SearchRegressionTests("testVarDeclarationArgument"), 75901 ) );     //defect75901 //$NON-NLS-1$
        suite.addTest( new FailingTest( new SearchRegressionTests("testVarReferenceInitializer"), 72735 ) );    //defect72735 //$NON-NLS-1$
        suite.addTest( new FailingTest( new SearchRegressionTests("testNestedFieldReference"), 76203 ) );       //defect76203//$NON-NLS-1$
        
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
        
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A", CLASS, ALL_OCCURRENCES, true ); //$NON-NLS-1$
		Set matches = search( pattern );
		
		assertEquals( 2, matches.size() );
		assertMatch( matches, f, code.indexOf( "A {" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, f, code.indexOf( "A::" ) ); //$NON-NLS-1$ //$NON-NLS-2$
   	}
    // test SE44
    public void testEnumerationDeclaration() throws Exception {
        Writer writer = new StringWriter();
        writer.write(" enum day {sun, mon, tue};               \n" ); //$NON-NLS-1$
                  
        String code = writer.toString();
        IFile f = importFile( "EnumerationDeclaration.cpp", code ); //$NON-NLS-1$
        
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "day", ENUM, DECLARATIONS, true ); //$NON-NLS-1$
		Set matches = search( pattern );
		
		assertEquals( 1, matches.size() );
		assertMatch( matches, f, code.indexOf( "day" )  ); //$NON-NLS-1$ //$NON-NLS-2$

   	}
    // test SE47
    public void testNamespaceDefinition() throws Exception {
        Writer writer = new StringWriter();
        writer.write("namespace N {							\n" ); //$NON-NLS-1$
        writer.write("  int a;								\n" ); //$NON-NLS-1$
        writer.write("  namespace M {						\n" ); //$NON-NLS-1$
        writer.write("    struct linkedlist {				\n" ); //$NON-NLS-1$
        writer.write("      int item;						\n" ); //$NON-NLS-1$
        writer.write("      struct linkedlist *next;		\n" ); //$NON-NLS-1$
        writer.write("    };								\n" ); //$NON-NLS-1$
        writer.write("  }									\n" ); //$NON-NLS-1$
        writer.write("}										\n" ); //$NON-NLS-1$
        String code = writer.toString();
        IFile gh = importFile( "NamespaceDefinition.h", code ); //$NON-NLS-1$
        
        writer = new StringWriter();
        writer.write("#include \"NamespaceDefinition.h\"					\n" ); //$NON-NLS-1$
        writer.write("using namespace N::M;				\n" ); //$NON-NLS-1$
        writer.write("linkedlist serial_numbers;		\n" ); //$NON-NLS-1$
        writer.write("namespace R {//defn						\n" ); //$NON-NLS-1$
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
        IFile g = importFile( "NamespaceDefinition.cpp", code2 ); //$NON-NLS-1$
        
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "*", NAMESPACE, DEFINITIONS, true ); //$NON-NLS-1$
		Set matches = search( pattern );
		
		assertEquals( 3, matches.size() );
		assertMatch( matches, gh, code.indexOf( "N" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, gh, code.indexOf( "M" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, g, code2.indexOf( "R {//defn" )  ); //$NON-NLS-1$ //$NON-NLS-2$

   	}
    // test SE50
    public void testMethodFuncReference() throws Exception {
        Writer writer = new StringWriter();
    	writer.write("typedef int I32;		\n" ); //$NON-NLS-1$
        writer.write("void fz(){}			\n" ); //$NON-NLS-1$
        writer.write("class K{				\n" ); //$NON-NLS-1$
        writer.write("public:				\n" ); //$NON-NLS-1$
        writer.write("	K();				\n" ); //$NON-NLS-1$
        writer.write("	virtual ~K();		\n" ); //$NON-NLS-1$
        writer.write("	static I32 fx();	\n" ); //$NON-NLS-1$
        writer.write("	void fy(){			\n" ); //$NON-NLS-1$
        writer.write("	  fz();				\n" ); //$NON-NLS-1$
        writer.write("	}					\n" ); //$NON-NLS-1$
        writer.write("};					\n" ); //$NON-NLS-1$
        String code = writer.toString();
        IFile kh = importFile( "MethodFuncReference.h", code ); //$NON-NLS-1$
        
        writer = new StringWriter();
        writer.write("#include \"MethodFuncReference.h\"			\n" ); //$NON-NLS-1$
        writer.write("K::K(){}					\n" ); //$NON-NLS-1$
        writer.write("K::~K(){}					\n" ); //$NON-NLS-1$
        writer.write("I32 K::fx(){return 12;}	\n" ); //$NON-NLS-1$
        writer.write("void fß(){}				\n" ); //$NON-NLS-1$
        writer.write("void callFuncMeth(){		\n" ); //$NON-NLS-1$
        writer.write("	I32 i = K::fx();		\n" ); //$NON-NLS-1$
        writer.write("	(new K())->fy();		\n" ); //$NON-NLS-1$
        writer.write("	fß();					\n" ); //$NON-NLS-1$
      	writer.write("}							\n" ); //$NON-NLS-1$
        String code2 = writer.toString();
        IFile k = importFile( "MethodFuncReference.cpp", code2 ); //$NON-NLS-1$
        OrPattern orPattern=new OrPattern();
        orPattern.addPattern(SearchEngine.createSearchPattern("f*", METHOD, REFERENCES, true)); //$NON-NLS-1$
        orPattern.addPattern(SearchEngine.createSearchPattern("f*", FUNCTION, REFERENCES, true)); //$NON-NLS-1$
        
        Set matches = search( orPattern);
		assertEquals( 4, matches.size());
		assertMatch( matches, k, code2.lastIndexOf( "fx" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, k, code2.lastIndexOf( "fy" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, k, code2.lastIndexOf( "fß" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, kh, code.lastIndexOf( "fz" )  ); //$NON-NLS-1$ //$NON-NLS-2$

   	}
    // test SE53 // SE60c
    public void testVarFieldDeclaration() throws Exception {
        Writer writer = new StringWriter();
    	writer.write(" namespace N2 {							\n" ); //$NON-NLS-1$
    	writer.write("	struct linkedlist1{ 					\n" ); //$NON-NLS-1$
    	writer.write("		int field1_t;					    \n" ); //$NON-NLS-1$
    	writer.write("		struct linkedlist1 *field2;	  		\n" ); //$NON-NLS-1$
    	writer.write("	};										\n" ); //$NON-NLS-1$
    	writer.write("	class linkedlist2{ 						\n" ); //$NON-NLS-1$
    	writer.write("		int field3;							\n" ); //$NON-NLS-1$
    	writer.write("		class linkedlist2 *field4;			\n" ); //$NON-NLS-1$
    	writer.write("	};										\n" ); //$NON-NLS-1$
    	writer.write("  class C { 								\n" ); //$NON-NLS-1$
    	writer.write("  	struct linkedlist1 field5;		    \n" ); //$NON-NLS-1$
    	writer.write("   	linkedlist2 *field6;				\n" ); //$NON-NLS-1$
    	writer.write("  };										\n" ); //$NON-NLS-1$
    	writer.write("  class linkedlist2 var1;					\n" ); //$NON-NLS-1$
    	writer.write(" }										\n" ); //$NON-NLS-1$
        String code = writer.toString();			
        IFile gh = importFile( "VarFieldDeclaration.h", code ); //$NON-NLS-1$
        
       
        OrPattern orPattern=new OrPattern();
        orPattern.addPattern(SearchEngine.createSearchPattern("*", VAR, DECLARATIONS, true)); //$NON-NLS-1$
        orPattern.addPattern(SearchEngine.createSearchPattern("*", FIELD, DECLARATIONS, true)); //$NON-NLS-1$
        
        Set matches = search( orPattern);
        
        assertEquals( 7, matches.size());
		assertMatch( matches, gh, code.indexOf( "field1" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, gh, code.indexOf( "field2" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, gh, code.indexOf( "field3" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, gh, code.indexOf( "field4" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, gh, code.indexOf( "field5" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, gh, code.indexOf( "field6" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, gh, code.indexOf( "var1" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		
   	}
    //test SE100 
    public void testMethodAll() throws Exception {
        
    	Writer writer = new StringWriter();
    	writer.write(" typedef int INT64;						\n" ); //$NON-NLS-1$
    	writer.write("class M {									\n" ); //$NON-NLS-1$
    	writer.write("public:									\n" ); //$NON-NLS-1$
    	writer.write("	M();//decl								\n" ); //$NON-NLS-1$
    	writer.write("	~M(){}//defn,decl						\n" ); //$NON-NLS-1$
    	writer.write("	void m1();//decl					\n" ); //$NON-NLS-1$
    	writer.write("	static INT64 m2(){return 12;}//defn,decl\n" ); //$NON-NLS-1$
    	writer.write("};										\n" ); //$NON-NLS-1$
    	writer.write("INT64 fz();								\n" ); //$NON-NLS-1$
    	writer.write("INT64 fz() {								\n" ); //$NON-NLS-1$
        writer.write("	(new M())->m1(); //ref,ref				\n" ); //$NON-NLS-1$
    	writer.write("	return 12;								\n" ); //$NON-NLS-1$
	    writer.write("}											\n" ); //$NON-NLS-1$
        writer.write("M::M() {//secondM is defn				\n" ); //$NON-NLS-1$
	    writer.write("	fz();									\n" ); //$NON-NLS-1$
	    writer.write("}											\n" ); //$NON-NLS-1$
        writer.write("void M::m1() {//m1 is defn				\n" ); //$NON-NLS-1$
	    writer.write("	fz();									\n" ); //$NON-NLS-1$
	    writer.write(" }										\n" ); //$NON-NLS-1$
        String code = writer.toString();			
        IFile gh = importFile( "MethodAll.cpp", code ); //$NON-NLS-1$
        //all
        ICSearchPattern pattern=SearchEngine.createSearchPattern("*", METHOD, ALL_OCCURRENCES, true); //$NON-NLS-1$
        Set matches = search(pattern);
        assertEquals( 8, matches.size());
		//decln
        pattern=SearchEngine.createSearchPattern("*", METHOD, DECLARATIONS, true); //$NON-NLS-1$
        matches = search(pattern);
        assertEquals( 4, matches.size());
		assertMatch( matches, gh, code.indexOf( "M();" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, gh, code.indexOf( "~M(){}" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, gh, code.indexOf( "m1();//decl" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, gh, code.indexOf( "m2(){return 12;}//defn,decl" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		//defn
		pattern=SearchEngine.createSearchPattern("*", METHOD, DEFINITIONS, true); //$NON-NLS-1$
        matches = search(pattern);
        assertEquals( 4, matches.size());
		assertMatch( matches, gh, code.indexOf( "~M(){}" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, gh, code.indexOf( "m2(){return 12;}//defn,decl" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, gh, code.indexOf( "M() {//secondM is defn" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, gh, code.indexOf( "m1() {//m1 is defn" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		//ref
		pattern=SearchEngine.createSearchPattern("*", METHOD, REFERENCES, true); //$NON-NLS-1$
        matches = search(pattern);
        assertEquals( 2, matches.size());
		assertMatch( matches, gh, code.indexOf( "M())->m1(); //ref,ref" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, gh, code.indexOf( "m1(); //ref,ref" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		//fully qualified all
		pattern=SearchEngine.createSearchPattern("M::m1", METHOD, ALL_OCCURRENCES, true); //$NON-NLS-1$
        matches = search(pattern);
        assertEquals( 3, matches.size());
		assertMatch( matches, gh, code.indexOf( "m1() {//m1 is defn" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, gh, code.indexOf( "m1();//decl" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, gh, code.indexOf( "m1(); //ref,ref" )  ); //$NON-NLS-1$ //$NON-NLS-2$
   	}
       
//  test SE60b var all
    public void testVarReference() throws Exception {
        Writer writer = new StringWriter();
        	writer.write("#include <string.h>										\n" ); //$NON-NLS-1$
        	writer.write("enum ZooLocs {ZOOANIMAL, BEAR, PANDA};					\n" ); //$NON-NLS-1$
        	writer.write("namespace zoo {											\n" ); //$NON-NLS-1$
       		writer.write("  int foo=1;//vardefn, vardecl							\n" ); //$NON-NLS-1$
        	writer.write("  class ZooAnimal{										\n" ); //$NON-NLS-1$
        	writer.write("    public:												\n" ); //$NON-NLS-1$
        	writer.write("    	ZooAnimal(char *s = \"ZooAnimal\");					\n" ); //$NON-NLS-1$
        	writer.write("		virtual ~ZooAnimal() { delete name; }				\n" ); //$NON-NLS-1$
        	writer.write("    protected:											\n" ); //$NON-NLS-1$
        	writer.write("    	char *name;											\n" ); //$NON-NLS-1$
        	writer.write("    	ZooAnimal *next;									\n" ); //$NON-NLS-1$
        	writer.write("  };														\n" ); //$NON-NLS-1$	
        	writer.write("  class Bear : public ZooAnimal{							\n" ); //$NON-NLS-1$
        	writer.write("    public:												\n" ); //$NON-NLS-1$
        	writer.write("    	Bear( char *s = \"Bear\", ZooLocs loc = BEAR, char *sci = \"Ursidae\" );\n" ); //$NON-NLS-1$
        	writer.write("   	~Bear();											\n" ); //$NON-NLS-1$
        	writer.write("    protected:											\n" ); //$NON-NLS-1$
        	writer.write("    	char *sciName;										\n" ); //$NON-NLS-1$
        	writer.write("    	ZooLocs zooArea;									\n" ); //$NON-NLS-1$
        	writer.write("  };														\n" ); //$NON-NLS-1$
            writer.write("  ZooAnimal::ZooAnimal(char *s) : next (0) {//s-vardecl	\n" ); //$NON-NLS-1$
            writer.write("    	name=new char[ strlen(s) +1];//s-varref				\n" ); //$NON-NLS-1$
        	writer.write("    	strcpy(name,s);//s-varref							\n" ); //$NON-NLS-1$
        	writer.write("  }														\n" ); //$NON-NLS-1$
            writer.write("  Bear::Bear(char *s, ZooLocs loc, char *sci)//s,loc,sci-vardecl\n" ); //$NON-NLS-1$
            writer.write("   	: ZooAnimal(s), zooArea (loc) {//s,loc-varref		\n" ); //$NON-NLS-1$
        	writer.write("    	sciName = new char[ strlen(sci)+1];//sci-varref		\n" ); //$NON-NLS-1$
        	writer.write("    	strcpy (sciName, sci);//sci-varref					\n" ); //$NON-NLS-1$
        	writer.write("  }														\n" ); //$NON-NLS-1$
        	writer.write(" }														\n" ); //$NON-NLS-1$
            String code = writer.toString();			
            IFile z = importFile( "VarReference.cpp", code ); //$NON-NLS-1$
                  
            //ref
    		ICSearchPattern pattern=SearchEngine.createSearchPattern("sci", VAR, REFERENCES, true); //$NON-NLS-1$
    		Set matches = search(pattern);
            assertEquals( 2, matches.size());
    		assertMatch( matches, z, code.indexOf( "sci)+1];//sci-varref" )  ); //$NON-NLS-1$ //$NON-NLS-2$
    		assertMatch( matches, z, code.indexOf( "sci);//sci-varref" )  ); //$NON-NLS-1$ //$NON-NLS-2$
    	
    }
    // 75901 //  test SE60f1 var all
    public void testVarDeclarationArgument() throws Exception {
        Writer writer = new StringWriter();
    	writer.write("#include <string.h>										\n" ); //$NON-NLS-1$
    	writer.write("enum ZooLocs {ZOOANIMAL, BEAR, PANDA};					\n" ); //$NON-NLS-1$
    	writer.write("namespace zoo {											\n" ); //$NON-NLS-1$
   		writer.write("  int foo=1;//vardefn, vardecl							\n" ); //$NON-NLS-1$
    	writer.write("  class ZooAnimal{										\n" ); //$NON-NLS-1$
    	writer.write("    public:												\n" ); //$NON-NLS-1$
    	writer.write("    	ZooAnimal(char *s = \"ZooAnimal\");					\n" ); //$NON-NLS-1$
    	writer.write("		virtual ~ZooAnimal() { delete name; }				\n" ); //$NON-NLS-1$
    	writer.write("    protected:											\n" ); //$NON-NLS-1$
    	writer.write("    	char *name;											\n" ); //$NON-NLS-1$
    	writer.write("    	ZooAnimal *next;									\n" ); //$NON-NLS-1$
    	writer.write("  };														\n" ); //$NON-NLS-1$	
    	writer.write("  class Bear : public ZooAnimal{							\n" ); //$NON-NLS-1$
    	writer.write("    public:												\n" ); //$NON-NLS-1$
    	writer.write("    	Bear( char *s = \"Bear\", ZooLocs loc = BEAR, char *sci = \"Ursidae\" );\n" ); //$NON-NLS-1$
    	writer.write("   	~Bear();											\n" ); //$NON-NLS-1$
    	writer.write("    protected:											\n" ); //$NON-NLS-1$
    	writer.write("    	char *sciName;										\n" ); //$NON-NLS-1$
    	writer.write("    	ZooLocs zooArea;									\n" ); //$NON-NLS-1$
    	writer.write("  };														\n" ); //$NON-NLS-1$
        writer.write("  ZooAnimal::ZooAnimal(char *s) : next (0) {//s-vardecl	\n" ); //$NON-NLS-1$
        writer.write("    	name=new char[ strlen(s) +1];//s-varref				\n" ); //$NON-NLS-1$
    	writer.write("    	strcpy(name,s);//s-varref							\n" ); //$NON-NLS-1$
    	writer.write("  }														\n" ); //$NON-NLS-1$
        writer.write("  Bear::Bear(char *s, ZooLocs loc, char *sci)//s,loc,sci-vardecl\n" ); //$NON-NLS-1$
        writer.write("   	: ZooAnimal(s), zooArea (loc) {//s,loc-varref		\n" ); //$NON-NLS-1$
    	writer.write("    	sciName = new char[ strlen(sci)+1];//sci-varref		\n" ); //$NON-NLS-1$
    	writer.write("    	strcpy (sciName, sci);//sci-varref					\n" ); //$NON-NLS-1$
    	writer.write("  }														\n" ); //$NON-NLS-1$
    	writer.write(" }														\n" ); //$NON-NLS-1$
        String code = writer.toString();			
        IFile z = importFile( "VarDeclarationArgument.cpp", code ); //$NON-NLS-1$
        ICSearchPattern pattern=SearchEngine.createSearchPattern("s", VAR, DECLARATIONS, true); //$NON-NLS-1$
        Set matches = search(pattern);
        assertEquals( 2, matches.size());
		assertMatch( matches, z, code.indexOf( "s = \"Bear\"" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, z, code.indexOf( "char *s" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, z, code.indexOf( "char *s" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		  
   	}
    //  72735 test SE60f2 var all
    public void testVarReferenceInitializer() throws Exception{
    	Writer writer = new StringWriter();
    	writer.write("#include <string.h>										\n" ); //$NON-NLS-1$
    	writer.write("enum ZooLocs {ZOOANIMAL, BEAR, PANDA};					\n" ); //$NON-NLS-1$
    	writer.write("namespace zoo {											\n" ); //$NON-NLS-1$
   		writer.write("  int foo=1;//vardefn, vardecl							\n" ); //$NON-NLS-1$
    	writer.write("  class ZooAnimal{										\n" ); //$NON-NLS-1$
    	writer.write("    public:												\n" ); //$NON-NLS-1$
    	writer.write("    	ZooAnimal(char *s = \"ZooAnimal\");					\n" ); //$NON-NLS-1$
    	writer.write("		virtual ~ZooAnimal() { delete name; }				\n" ); //$NON-NLS-1$
    	writer.write("    protected:											\n" ); //$NON-NLS-1$
    	writer.write("    	char *name;											\n" ); //$NON-NLS-1$
    	writer.write("    	ZooAnimal *next;									\n" ); //$NON-NLS-1$
    	writer.write("  };														\n" ); //$NON-NLS-1$	
    	writer.write("  class Bear : public ZooAnimal{							\n" ); //$NON-NLS-1$
    	writer.write("    public:												\n" ); //$NON-NLS-1$
    	writer.write("    	Bear( char *s = \"Bear\", ZooLocs loc = BEAR, char *sci = \"Ursidae\" );\n" ); //$NON-NLS-1$
    	writer.write("   	~Bear();											\n" ); //$NON-NLS-1$
    	writer.write("    protected:											\n" ); //$NON-NLS-1$
    	writer.write("    	char *sciName;										\n" ); //$NON-NLS-1$
    	writer.write("    	ZooLocs zooArea;									\n" ); //$NON-NLS-1$
    	writer.write("  };														\n" ); //$NON-NLS-1$
        writer.write("  ZooAnimal::ZooAnimal(char *s) : next (0) {//s-vardecl	\n" ); //$NON-NLS-1$
        writer.write("    	name=new char[ strlen(s) +1];//s-varref				\n" ); //$NON-NLS-1$
    	writer.write("    	strcpy(name,s);//s-varref							\n" ); //$NON-NLS-1$
    	writer.write("  }														\n" ); //$NON-NLS-1$
        writer.write("  Bear::Bear(char *s, ZooLocs loc, char *sci)//s,loc,sci-vardecl\n" ); //$NON-NLS-1$
        writer.write("   	: ZooAnimal(s), zooArea (loc) {//s,loc-varref		\n" ); //$NON-NLS-1$
    	writer.write("    	sciName = new char[ strlen(sci)+1];//sci-varref		\n" ); //$NON-NLS-1$
    	writer.write("    	strcpy (sciName, sci);//sci-varref					\n" ); //$NON-NLS-1$
    	writer.write("  }														\n" ); //$NON-NLS-1$
    	writer.write(" }														\n" ); //$NON-NLS-1$
        String code = writer.toString();			
        IFile z = importFile( "VarReferenceInitializer.cpp", code ); //$NON-NLS-1$
        ICSearchPattern pattern=SearchEngine.createSearchPattern("loc", VAR, REFERENCES, true); //$NON-NLS-1$
        Set matches = search(pattern);
        assertEquals( 1, matches.size());
		//broken... initializer list of Bear cctor
		assertMatch( matches, z, code.indexOf( "loc) {//s,loc-varref" )  ); //$NON-NLS-1$ //$NON-NLS-2$
	
    }
    //  SE63a  var field defn // test SE60a var all
    public void testVarDefinition() throws Exception {

       	Writer writer = new StringWriter();
        writer.write("    #include <string.h>					\n" ); //$NON-NLS-1$
        writer.write("    namespace zoo {						\n" ); //$NON-NLS-1$
        writer.write("    int foo;//vardefn, vardecl			\n" ); //$NON-NLS-1$
        writer.write("    }										\n" ); //$NON-NLS-1$
        String code = writer.toString();			
        IFile z = importFile( "VarDefinition.cpp", code ); //$NON-NLS-1$
        ICSearchPattern pattern = SearchEngine.createSearchPattern("*", VAR, DEFINITIONS, true); //$NON-NLS-1$
        Set matches = search( pattern);
        assertEquals( 1, matches.size());
    	assertMatch( matches, z, code.indexOf( "foo;//vardefn" )  ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    // test SE63b var field defn
    public void testFieldDefinition() throws Exception {

    	Writer writer = new StringWriter();
    	writer.write("    #include <string.h>					\n" ); //$NON-NLS-1$
    	writer.write("    enum ZooLocs {ZOOANIMAL, BEAR, PANDA};\n" ); //$NON-NLS-1$
    	writer.write("    namespace zoo {						\n" ); //$NON-NLS-1$
    	writer.write("    int foo;//vardefn, vardecl			\n" ); //$NON-NLS-1$
    	writer.write("    class ZooAnimal{						\n" ); //$NON-NLS-1$
    	writer.write("    public:								\n" ); //$NON-NLS-1$
    	writer.write("    	ZooAnimal(char *s = \"ZooAnimal\");	\n" ); //$NON-NLS-1$
    	writer.write("    	virtual ~ZooAnimal() { delete name; }\n" ); //$NON-NLS-1$
    	writer.write("    protected:							\n" ); //$NON-NLS-1$
    	writer.write("    	char *name;//fielddefn,decl			\n" ); //$NON-NLS-1$
    	writer.write("    	ZooAnimal *next;//fielddefn,decl	\n" ); //$NON-NLS-1$
    	writer.write("    };									\n" ); //$NON-NLS-1$
    	writer.write("    class Mammal{							\n" ); //$NON-NLS-1$
    	writer.write("    public: 								\n" ); //$NON-NLS-1$
    	writer.write("    	Mammal(bool b);						\n" ); //$NON-NLS-1$
    	writer.write("    	~Mammal(){}							\n" ); //$NON-NLS-1$
    	writer.write("    protected:							\n" ); //$NON-NLS-1$
    	writer.write("    	bool isCarnivore;//fielddefn,decl	\n" ); //$NON-NLS-1$
    	writer.write("    };									\n" ); //$NON-NLS-1$
    	writer.write("    class Bear : public ZooAnimal, Mammal{\n" ); //$NON-NLS-1$
    	writer.write("    public:								\n" ); //$NON-NLS-1$
    	writer.write("    	Bear( char *s = \"Bear\", ZooLocs loc = BEAR, char *sci = \"Ursidae\" );\n" ); //$NON-NLS-1$
    	writer.write("    	~Bear();							\n" ); //$NON-NLS-1$
    	writer.write("    protected:							\n" ); //$NON-NLS-1$
    	writer.write("    	char *sciName;//fielddefn,decl		\n" ); //$NON-NLS-1$
    	writer.write("    	ZooLocs zooArea;//fielddefn,decl	\n" ); //$NON-NLS-1$
    	writer.write("    };									\n" ); //$NON-NLS-1$
    	writer.write("    }										\n" ); //$NON-NLS-1$
    	writer.write("    using namespace zoo;					\n" ); //$NON-NLS-1$
    	writer.write("    ZooAnimal::ZooAnimal(char *s) : next (0){\n" ); //$NON-NLS-1$
    	writer.write("    	name=new char[ strlen(s) +1];		\n" ); //$NON-NLS-1$
    	writer.write("    	strcpy(name,s);						\n" ); //$NON-NLS-1$
    	writer.write("    }										\n" ); //$NON-NLS-1$
    	writer.write("    Mammal::Mammal(bool b) : isCarnivore (b) {}\n" ); //$NON-NLS-1$
    	writer.write("    Bear::Bear(char *s, ZooLocs loc, char *sci)\n" ); //$NON-NLS-1$
    	writer.write("    	: ZooAnimal(s), zooArea (loc), Mammal(true) {\n" ); //$NON-NLS-1$
    	writer.write("    	sciName = new char[ strlen(sci)+1];	\n" ); //$NON-NLS-1$
    	writer.write("    	strcpy (sciName, sci);				\n" ); //$NON-NLS-1$
    	writer.write("    }										\n" ); //$NON-NLS-1$
    	String code = writer.toString();			
        IFile z = importFile( "FieldDefinition.cpp", code ); //$NON-NLS-1$
               
        ICSearchPattern pattern = SearchEngine.createSearchPattern("*", FIELD, DEFINITIONS, true); //$NON-NLS-1$
        Set matches = search( pattern);
        assertEquals( 5, matches.size());
		assertMatch( matches, z, code.indexOf( "name;//field" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, z, code.indexOf( "next;//fielddefn" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, z, code.indexOf( "isCarnivore;//fielddefn" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, z, code.indexOf( "sciName;//fielddefn" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, z, code.indexOf( "zooArea;//fielddefn" )  ); //$NON-NLS-1$ //$NON-NLS-2$
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
            
    	ICSearchPattern pattern = SearchEngine.createSearchPattern( "*", NAMESPACE, REFERENCES, true ); //$NON-NLS-1$
    	Set matches = search( pattern );
    		
    	assertEquals( 8, matches.size() );
    	assertMatch( matches, n, code2.indexOf( "N1::N2::C2 *c" )  ); //$NON-NLS-1$ //$NON-NLS-2$
    	assertMatch( matches, n, code2.indexOf( "N2::C2 *c" )  ); //$NON-NLS-1$ //$NON-NLS-2$
    	assertMatch( matches, n, code2.indexOf( "N1::N2::C2();" )  ); //$NON-NLS-1$ //$NON-NLS-2$
    	assertMatch( matches, n, code2.indexOf( "N2::C2();" )  ); //$NON-NLS-1$ //$NON-NLS-2$
    	assertMatch( matches, n, code2.indexOf( "N1::N2;" )  ); //$NON-NLS-1$ //$NON-NLS-2$
    	assertMatch( matches, n, code2.indexOf( "N2;" )  ); //$NON-NLS-1$ //$NON-NLS-2$
    	assertMatch( matches, n, code2.indexOf( "N3::C3 *d" )  ); //$NON-NLS-1$ //$NON-NLS-2$
    	assertMatch( matches, n, code2.indexOf( "N3::C3();" )  ); //$NON-NLS-1$ //$NON-NLS-2$

    }
//  test 85a union declaration
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
    	Set matches = search( pattern );
    		
    	assertEquals( 3, matches.size() );
    	assertMatch( matches, uh, code.indexOf( "union { //anon field" )  ); //$NON-NLS-1$ //$NON-NLS-2$
    	assertMatch( matches, uh, code.indexOf( "union { //anon static" )  ); //$NON-NLS-1$ //$NON-NLS-2$
    	assertMatch( matches, uh, code.indexOf( "TokenValue" )  ); //$NON-NLS-1$ //$NON-NLS-2$
    	
    }
//  test 85b class struct declaration
    public void testClassStructDeclaration() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write(" namespace N1 {							\n" ); //$NON-NLS-1$
    	writer.write("	struct linkedlist1{//decl 					\n" ); //$NON-NLS-1$
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
    	assertMatch( matches, gh, code.indexOf( "linkedlist1{//decl" )  ); //$NON-NLS-1$ //$NON-NLS-2$
    	assertMatch( matches, gh, code.indexOf( "linkedlist2{//decl" )  ); //$NON-NLS-1$ //$NON-NLS-2$
    	
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
        
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "*", NAMESPACE, DECLARATIONS, true ); //$NON-NLS-1$
		Set matches = search( pattern );
		
		assertEquals( 3, matches.size() );
		assertMatch( matches, gh, code.indexOf( "N" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, gh, code.indexOf( "M" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, g, code2.indexOf( "R {//defn,decl" )  ); //$NON-NLS-1$ //$NON-NLS-2$

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
		assertMatch( matches, g, code.indexOf( "Mammal(t)" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		
    }
    // test 90
    public void testClassStructReference() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("class Mammal {//decl			\n" ); 	//$NON-NLS-1$
        writer.write("	public:						\n" ); 	//$NON-NLS-1$
        writer.write("	Mammal(bool b): isCarnivore(b){}	\n//ref" ); //$NON-NLS-1$
        writer.write("	private:					\n" ); 	//$NON-NLS-1$
        writer.write("	bool isCarnivore;			\n" ); 	//$NON-NLS-1$
        writer.write("};							\n" ); 	//$NON-NLS-1$
        writer.write("class Bear : Mammal{//ref		\n" ); 	//$NON-NLS-1$
        writer.write("public:						\n" ); 		//$NON-NLS-1$
        writer.write("	Bear(int s,int t): Mammal(true) { \n" ); //$NON-NLS-1$
        writer.write("    biotics.matureYears=s;		\n" ); 	//$NON-NLS-1$
        writer.write("    biotics.littersPerYear=t;		\n" ); 	//$NON-NLS-1$
        writer.write("  }								\n" ); 	//$NON-NLS-1$
        writer.write("  struct {						\n" ); 	//$NON-NLS-1$
    	writer.write("    int matureYears;				\n" ); 	//$NON-NLS-1$
        writer.write("	  int littersPerYear; 		\n" ); 	//$NON-NLS-1$
        writer.write("  } biotics;					\n" ); 	//$NON-NLS-1$
        writer.write("};							\n" ); 	//$NON-NLS-1$
        writer.write("struct bioticPotential {		\n" ); 	//$NON-NLS-1$
    	writer.write("  int litterSize;				\n" ); 	//$NON-NLS-1$
        writer.write("	int matureYears;			\n" ); 	//$NON-NLS-1$
        writer.write("};							\n" );	//$NON-NLS-1$
        writer.write("int main(int argc, char **argv) {\n" ); //$NON-NLS-1$
        writer.write(" 	bioticPotential brownbear_bt = {1, 3};\n" ); //$NON-NLS-1$
        writer.write("	Bear *grizzly = new Bear(4,3);\n" ); //$NON-NLS-1$
        writer.write("    	brownbear_bt.matureYears = grizzly->biotics.matureYears;\n" ); //$NON-NLS-1$
        writer.write("} 								\n" ); 	//$NON-NLS-1$
        String code = writer.toString();
        IFile b = importFile( "ClassStructReference.cpp", code ); //$NON-NLS-1$
        
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "*", CLASS_STRUCT, REFERENCES, true ); //$NON-NLS-1$
		Set matches = search( pattern );
		
		assertEquals( 3, matches.size() );
		assertMatch( matches, b, code.indexOf( "bioticPotential brownbear_bt" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, b, code.indexOf( "Mammal{//ref" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, b, code.indexOf( "Bear *grizzly" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		
    }
    public void testFieldReference() throws Exception {
    	Writer writer = new StringWriter();
        writer.write("class Mammal {//decl			\n" ); 	//$NON-NLS-1$
        writer.write("	public:						\n" ); 	//$NON-NLS-1$
        writer.write("	Mammal(bool b): isCarnivore(b){}	\n//ref" ); //$NON-NLS-1$
        writer.write("	private:					\n" ); 	//$NON-NLS-1$
        writer.write("	bool isCarnivore;			\n" ); 	//$NON-NLS-1$
        writer.write("};							\n" ); 	//$NON-NLS-1$
        writer.write("class Bear : Mammal{			\n" ); 	//$NON-NLS-1$
        writer.write("public:						\n" ); 	//$NON-NLS-1$
        writer.write("	Bear(int s,int t): Mammal(true) { 	\n" ); //$NON-NLS-1$
        writer.write("    biotics.matureYears=s;//ref,ref	\n" ); //$NON-NLS-1$
        writer.write("    biotics.littersPerYear=t;//ref,ref\n" ); //$NON-NLS-1$
        writer.write("  }								\n" ); 	//$NON-NLS-1$
        writer.write("  struct {						\n" ); 	//$NON-NLS-1$
    	writer.write("    int matureYears;			\n" ); 	//$NON-NLS-1$
        writer.write("	  int littersPerYear; 		\n" ); 	//$NON-NLS-1$
        writer.write("  } biotics;					\n" ); 	//$NON-NLS-1$
        writer.write("};							\n" ); 	//$NON-NLS-1$
        writer.write("struct bioticPotential {		\n" ); 	//$NON-NLS-1$
    	writer.write("  int litterSize;				\n" ); 	//$NON-NLS-1$
        writer.write("	int matureYears;			\n" ); 	//$NON-NLS-1$
        writer.write("};							\n" );	//$NON-NLS-1$
        writer.write("int main(int argc, char **argv) {\n" ); //$NON-NLS-1$
        writer.write(" 	bioticPotential brownbear_bt = {1, 3};\n" ); //$NON-NLS-1$
        writer.write("	Bear *grizzly = new Bear(4,3);\n" ); //$NON-NLS-1$
        writer.write("    	brownbear_bt.matureYears = grizzly->biotics.matureYears;//ref\n" ); //$NON-NLS-1$
        writer.write("} 								\n" ); 	//$NON-NLS-1$
        String code = writer.toString();
        IFile b = importFile( "FieldReference.cpp", code ); //$NON-NLS-1$
        
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "isCarnivore", FIELD, REFERENCES, true ); //$NON-NLS-1$
		Set matches = search( pattern );
		assertEquals( 1, matches.size() );
		assertMatch( matches, b, code.indexOf( "isCarnivore(b){}" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		
		pattern = SearchEngine.createSearchPattern( "biotics", FIELD, REFERENCES, true ); //$NON-NLS-1$
		matches = search( pattern );
		assertEquals( 3, matches.size() );
		assertMatch( matches, b, code.indexOf( "biotics.matureYears=s;" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, b, code.indexOf( "biotics.littersPerYear=t;" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, b, code.indexOf( "biotics.matureYears;//ref" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		
    }
    //76203
    public void testNestedFieldReference() throws Exception {
    	Writer writer = new StringWriter();
        writer.write("class Bear {					\n" ); 	//$NON-NLS-1$
        writer.write("public:						\n" ); 	//$NON-NLS-1$
        writer.write("	Bear(int s,int t) { 		\n" ); //$NON-NLS-1$
        writer.write("    biotics.matureYears=s;//ref	\n" ); //$NON-NLS-1$
        writer.write("    biotics.littersPerYear=t;		\n" ); //$NON-NLS-1$
        writer.write("  }								\n" ); 	//$NON-NLS-1$
        writer.write("  struct {						\n" ); 	//$NON-NLS-1$
    	writer.write("    int matureYears;			\n" ); 	//$NON-NLS-1$
        writer.write("	  int littersPerYear; 		\n" ); 	//$NON-NLS-1$
        writer.write("  } biotics;					\n" ); 	//$NON-NLS-1$
        writer.write("};							\n" ); 	//$NON-NLS-1$
        writer.write("struct bioticPotential {		\n" ); 	//$NON-NLS-1$
    	writer.write("  int litterSize;				\n" ); 	//$NON-NLS-1$
        writer.write("	int matureYears;			\n" ); 	//$NON-NLS-1$
        writer.write("};							\n" );	//$NON-NLS-1$
        writer.write("int main(int argc, char **argv) {			\n" ); //$NON-NLS-1$
        writer.write(" 	bioticPotential brownbear_bt = {1, 3};	\n" ); //$NON-NLS-1$
        writer.write("	Bear *grizzly = new Bear(4,3);			\n" ); //$NON-NLS-1$
        writer.write("    	brownbear_bt.matureYears = grizzly->biotics.matureYears;//ref\n" ); //$NON-NLS-1$
        writer.write("} 										\n" ); 	//$NON-NLS-1$
        String code = writer.toString();
        IFile b = importFile( "NestedFieldReference.cpp", code ); //$NON-NLS-1$
        
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "matureYears", FIELD, REFERENCES, true ); //$NON-NLS-1$
		Set matches = search( pattern );
		assertEquals( 3, matches.size() );
		assertMatch( matches, b, code.indexOf( "matureYears = grizzly" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, b, code.indexOf( "matureYears;//ref" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, b, code.indexOf( "matureYears=s;//ref,ref" )  ); //$NON-NLS-1$ //$NON-NLS-2$
			
    }
}
