/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Oct 4, 2004
 */
package org.eclipse.cdt.ui.tests.regression;

import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.tests.BaseTestFramework;
import org.eclipse.cdt.core.tests.FailingTest;
import org.eclipse.cdt.internal.core.browser.cache.TypeCacheManager;
import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.text.CHelpBookDescriptor;
import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProcessor;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author aniefer
 */
public class ContentAssistRegressionTests extends BaseTestFramework {
    static boolean 					disabledHelpContributions = false;
    final int 						TIMEOUT = 100; 
    public ContentAssistRegressionTests()
    {
        super();
    }
    /**
	 * @param name
	 */
    public ContentAssistRegressionTests(String name)
    {
        super(name);
    }
	
	protected void setUp() throws Exception {
		//TEMPORARY: Disable Type Cache
        super.setUp();
		TypeCacheManager typeCacheManager = TypeCacheManager.getInstance();
		typeCacheManager.setProcessTypeCacheEvents(false);
	}
	
    private void disableContributions (){
        //disable the help books so we don't get proposals we weren't expecting
        final IProject proj = project;
        CHelpBookDescriptor helpBooks[];
		helpBooks = CHelpProviderManager.getDefault().getCHelpBookDescriptors(new ICHelpInvocationContext(){
			public IProject getProject(){return proj;}
			public ITranslationUnit getTranslationUnit(){return null;}
			}
		);
		for( int i = 0; i < helpBooks.length; i++ ){
		    if( helpBooks[i] != null )
		        helpBooks[i].enable( false );
		}
    }
    protected ICompletionProposal[] getResults( IFile file, int offset ) throws Exception {
        if( !disabledHelpContributions )
            disableContributions();
	    ITranslationUnit tu = (ITranslationUnit)CoreModel.getDefault().create( file );
		String buffer = tu.getBuffer().getContents();
		IWorkingCopy wc = null;
		try{
			wc = tu.getWorkingCopy();
		}catch (CModelException e){
			fail("Failed to get working copy"); //$NON-NLS-1$
		}
	
		// call the CompletionProcessor
		CCompletionProcessor completionProcessor = new CCompletionProcessor(null);
		ICompletionProposal[] results = completionProcessor.evalProposals( new Document(buffer), offset, wc, null);
		//This should be replaced with a notification from the CCompletionProcessor
		Thread.sleep(TIMEOUT);
		return ( results != null ? results : new ICompletionProposal [0] );
    }
    
    public static Test suite(){
        return suite( true );
    }
    public static Test suite( boolean cleanup ) {
        TestSuite suite = new TestSuite("ContentAssistRegressionTests"); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testMemberCompletion") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testUnqualifiedWithPrefix") ); //$NON-NLS-1$
		suite.addTest( new ContentAssistRegressionTests("testQualifiedWithPrefix") ); //$NON-NLS-1$
		suite.addTest( new FailingTest(new ContentAssistRegressionTests("test76398"),76398) ); //$NON-NLS-1$
		suite.addTest( new ContentAssistRegressionTests("testQualifiedNoPrefix") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testSourceExtensions") ); //$NON-NLS-1$
        suite.addTest( new FailingTest(new ContentAssistRegressionTests("test76480"),76480) ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testField") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testFieldExpression") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testScope") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testClass") ); //$NON-NLS-1$
        suite.addTest( new FailingTest(new ContentAssistRegressionTests("test72723"),72723) ); //$NON-NLS-1$
        suite.addTest( new FailingTest(new ContentAssistRegressionTests("test72541"),72541) ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testFunction") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testArgument") ); //$NON-NLS-1$
        suite.addTest( new FailingTest(new ContentAssistRegressionTests("test76805"),76805) ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testCStyleCast") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testMethod") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testEnumerations") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testLongTokens") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testCastMultiLevel") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testHeaderExtensions") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testEmptyDocument") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testVariable") ); //$NON-NLS-1$
        suite.addTest( new FailingTest(new ContentAssistRegressionTests("test80510"),80510) ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testSingleNameReference") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testNamespace") ); //$NON-NLS-1$
        suite.addTest( new FailingTest(new ContentAssistRegressionTests("testNamespaceAlias80612"),80612) ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testMacro") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testConstructor") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testUnion") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testKeyword") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testBase") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testThis") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testClassScope") ); //$NON-NLS-1$
        suite.addTest( new FailingTest(new ContentAssistRegressionTests("testClassScope72564"),72564) ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testMultiLevelQualifiers") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testCase") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testType") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testTryCatch") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testArrays") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testStruct") ); //$NON-NLS-1$
        
        //Test framework doesn't include templates
        //suite.addTest( new ContentAssistRegressionTests("testCodeTemplate") ); //$NON-NLS-1$
        
        if( cleanup )
            suite.addTest( new ContentAssistRegressionTests("cleanupProject") );    //$NON-NLS-1$
        
	    return suite;
    }

    public void removeFile(String filename) throws Exception {
    	IResource [] members = project.members();
    	for( int i = 0; i < members.length; i++ ){
            if( members[i].getName().equals( filename ) ) 
            members[i].delete( false, monitor );
        }
    }
	public void testMemberCompletion() throws Exception {
	    StringWriter writer = new StringWriter();
	    writer.write("class A {                 \n"); //$NON-NLS-1$
	    writer.write("   int var;               \n"); //$NON-NLS-1$
	    writer.write("   void f();              \n"); //$NON-NLS-1$
	    writer.write("};                        \n"); //$NON-NLS-1$
	    writer.write("void A::f(){              \n"); //$NON-NLS-1$
	    writer.write("   v[^]                   \n"); //$NON-NLS-1$
	    writer.write("}                         \n"); //$NON-NLS-1$
	
	    String code = writer.toString();
	    IFile t = importFile( "testMemberCompletion.cpp", code ); //$NON-NLS-1$
	    ICompletionProposal [] results = getResults( t, code.indexOf( "[^]" ) ); //$NON-NLS-1$
	    
	    assertEquals( 4, results.length);
	    assertEquals( "var : int", results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "virtual", results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "void", results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "volatile", results[3].getDisplayString()); //$NON-NLS-1$
	}
    //test 1 with prefix 'z', inside various scopes
    public void testUnqualifiedWithPrefix() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("int zVar0;			\n"); //$NON-NLS-1$
        writer.write("class zClass { 		\n"); //$NON-NLS-1$
        writer.write("public:  				\n"); //$NON-NLS-1$
        writer.write(" zClass();  			\n"); //$NON-NLS-1$
        writer.write(" int zField;  		\n"); //$NON-NLS-1$
        writer.write(" void zMethod(); 		\n"); //$NON-NLS-1$
        writer.write("};                    \n"); //$NON-NLS-1$
        writer.write("void zFunction0();   	\n"); //$NON-NLS-1$
        
        String codeH = writer.toString();
        importFile( "testUnqualifiedWithPrefix.h", codeH ); //$NON-NLS-1$
        
        writer = new StringWriter();
        writer.write("#include \"testUnqualifiedWithPrefix.h\"	\n"); //$NON-NLS-1$
        writer.write("int zVar;					\n"); //$NON-NLS-1$
        writer.write("void zFunction(bool);		\n"); //$NON-NLS-1$
        writer.write("struct zStruct { int b;};	\n"); //$NON-NLS-1$
        writer.write("zClass c;//vp1			\n"); //$NON-NLS-1$
        writer.write("namespace test {			\n"); //$NON-NLS-1$
        writer.write(" zStruct s;//vp2			\n"); //$NON-NLS-1$
        writer.write(" const int zVar = zVar0 + zVar;//vp3	\n"); //$NON-NLS-1$
        writer.write("}							\n"); //$NON-NLS-1$
        writer.write("int main(int argc, char **argv) {		\n"); //$NON-NLS-1$
        writer.write(" zVar=0;//vp4				\n"); //$NON-NLS-1$
        writer.write(" using namespace test;	\n"); //$NON-NLS-1$
        writer.write(" zClass c2;//vp5			\n"); //$NON-NLS-1$
        writer.write(" ::zVar=0;//vp6			\n"); //$NON-NLS-1$
        writer.write(" test::zVar;//vp7			\n"); //$NON-NLS-1$
        writer.write(" return (0);				\n"); //$NON-NLS-1$
        writer.write("}							\n"); //$NON-NLS-1$
        writer.write("void zClass::zMethod(){	\n"); //$NON-NLS-1$
        writer.write("	zField=0;//vp8			\n"); //$NON-NLS-1$
        writer.write("}							\n"); //$NON-NLS-1$
        String code = writer.toString();
        IFile t = importFile( "testUnqualifiedWithPrefix.cpp", code ); //$NON-NLS-1$
        //vp1
        ICompletionProposal [] results = getResults( t, code.indexOf( "Class c;//vp1" ) ); //$NON-NLS-1$
        assertEquals( 2, results.length);
        assertEquals( "zClass", 	results[0].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zStruct", 	results[1].getDisplayString()); //$NON-NLS-1$
        //vp2
        results = getResults( t, code.indexOf( "Struct s;//vp2" ) ); //$NON-NLS-1$
        assertEquals( 2, results.length);
        assertEquals( "zClass", 	results[0].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zStruct", 	results[1].getDisplayString()); //$NON-NLS-1$
        //vp3
        results = getResults( t, code.indexOf( "Var0 + zVar;//vp3" ) ); //$NON-NLS-1$
        assertEquals( 6, results.length);
        assertEquals( "zVar : int",				results[0].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zVar0 : int",			results[1].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zFunction(bool) void", 	results[2].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zFunction0() void", 		results[3].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zClass", 				results[4].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zStruct", 				results[5].getDisplayString()); //$NON-NLS-1$
        //vp4
        results = getResults( t, code.indexOf( "Var=0;//vp4" ) ); //$NON-NLS-1$
        assertEquals( 6, results.length);
        assertEquals( "zVar : int",				results[0].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zVar0 : int",			results[1].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zFunction(bool) void", 	results[2].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zFunction0() void", 		results[3].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zClass", 				results[4].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zStruct", 				results[5].getDisplayString()); //$NON-NLS-1$
        //vp5 zVar is ambiguous so it won't show up
        results = getResults( t, code.indexOf( "Class c2;//vp5" ) ); //$NON-NLS-1$
        assertEquals( 5, results.length);
        assertEquals( "zVar0 : int",			results[0].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zFunction(bool) void", 	results[1].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zFunction0() void", 		results[2].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zClass", 				results[3].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zStruct", 				results[4].getDisplayString()); //$NON-NLS-1$
        //vp6
        results = getResults( t, code.indexOf( "Var=0;//vp6" ) ); //$NON-NLS-1$
        assertEquals( 6, results.length);
        assertEquals( "zVar : int",				results[0].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zVar0 : int",			results[1].getDisplayString()); //$NON-NLS-1$
        //vp7
        results = getResults( t, code.indexOf( "Var;//vp7" ) ); //$NON-NLS-1$
        assertEquals( 1, results.length);
        assertEquals( "zVar : const int",				results[0].getDisplayString()); //$NON-NLS-1$
        //vp8
        results = getResults( t, code.indexOf( "Field=0;//vp8" ) ); //$NON-NLS-1$
        assertEquals( 8, results.length);
        assertEquals( "zField : int",				results[0].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zMethod() void",			results[3].getDisplayString()); //$NON-NLS-1$
        
    }
	//test 2 with prefix 'z', qualified, inside 4 scopes
    public void testQualifiedWithPrefix() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class zClass {				\n"); //$NON-NLS-1$
        writer.write(" public:						\n"); //$NON-NLS-1$
        writer.write("  zClass(){} 					\n"); //$NON-NLS-1$
        writer.write("  static const int zField=1; 	\n"); //$NON-NLS-1$
        writer.write("  void zMethod();		\n"); //$NON-NLS-1$
        writer.write("};                    		\n"); //$NON-NLS-1$
        writer.write("int zVar0;					\n"); //$NON-NLS-1$
        writer.write(" void zFunction0();			\n"); //$NON-NLS-1$
         
        String codeH = writer.toString();
        importFile( "testQualifiedWithPrefix.h", codeH ); //$NON-NLS-1$
        
        writer = new StringWriter();
        writer.write("#include \"testQualifiedWithPrefix.h\"			\n"); //$NON-NLS-1$
        writer.write("zClass c;						\n"); //$NON-NLS-1$
        writer.write("int i = c.zField;//vp1-2res	\n"); //$NON-NLS-1$
        writer.write("namespace test {				\n"); //$NON-NLS-1$
        writer.write(" zClass c;					\n"); //$NON-NLS-1$
        writer.write(" int i=c.zField;//vp2-2res	\n"); //$NON-NLS-1$
        writer.write("}								\n"); //$NON-NLS-1$
        writer.write("int main(int argc, char **argv) {		\n"); //$NON-NLS-1$
        writer.write(" c.zField;//vp3-2res	\n"); //$NON-NLS-1$
        writer.write(" using namespace test;		\n"); //$NON-NLS-1$
        writer.write(" //c.z;//vpxxx defect 76398	\n"); //$NON-NLS-1$
        writer.write(" ::c.zMethod();//vp4-2res		\n"); //$NON-NLS-1$
        writer.write(" test::c.zField;//vp5-2res	\n"); //$NON-NLS-1$
        writer.write(" return (0);					\n"); //$NON-NLS-1$
        writer.write("}								\n"); //$NON-NLS-1$
        writer.write("void zClass::zMethod(){		\n"); //$NON-NLS-1$
        writer.write("	zzClass zz;					\n"); //$NON-NLS-1$
        writer.write("  zz.zMethod();//vp6			\n"); //$NON-NLS-1$
        writer.write("}								\n"); //$NON-NLS-1$
        
        String code = writer.toString();
        IFile t = importFile( "testQualifiedWithPrefix.cpp", code ); //$NON-NLS-1$
        //vp1 global scope
        ICompletionProposal [] results = getResults( t, code.indexOf( "Field;//vp1" ) ); //$NON-NLS-1$
        assertEquals( 2, results.length);
        assertEquals( "zField : const int", 	results[0].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zMethod() void", 	results[1].getDisplayString()); //$NON-NLS-1$
        //vp2 namespace scope
        results = getResults( t, code.indexOf( "Field;//vp2" ) ); //$NON-NLS-1$
        assertEquals( 2, results.length);
        assertEquals( "zField : const int", 	results[0].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zMethod() void", 	results[1].getDisplayString()); //$NON-NLS-1$
        //vp3 function scope, global class member ref
        results = getResults( t, code.indexOf( "Field;//vp3" ) ); //$NON-NLS-1$
        assertEquals( 2, results.length);
        assertEquals( "zField : const int", 	results[0].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zMethod() void", 	results[1].getDisplayString()); //$NON-NLS-1$
        //vp4 function scope, ambiguous clarified(global) class member ref
        results = getResults( t, code.indexOf( "Method();//vp4" ) ); //$NON-NLS-1$
        assertEquals( 2, results.length);
        assertEquals( "zField : const int", 	results[0].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zMethod() void", 	results[1].getDisplayString()); //$NON-NLS-1$
        //vp5 function scope, ambiguous clarified(namespace) class member ref
        results = getResults( t, code.indexOf( "Field;//vp5" ) ); //$NON-NLS-1$
        assertEquals( 2, results.length);
        assertEquals( "zField : const int", 	results[0].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zMethod() void", 	results[1].getDisplayString()); //$NON-NLS-1$
        //vp6 method scope
        results = getResults( t, code.indexOf( "Method();//vp6" ) ); //$NON-NLS-1$
        assertEquals( 2, results.length);
        assertEquals( "zField : const int", 	results[0].getDisplayString()); //$NON-NLS-1$
        assertEquals( "zMethod() void", 	results[1].getDisplayString()); //$NON-NLS-1$
        
    } 
    //with prefix 'z', qualified, inside function scope
    ////76398 function scope, ambiguous unclarified class member ref
    public void test76398() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class zClass {				\n"); //$NON-NLS-1$
        writer.write(" public:						\n"); //$NON-NLS-1$
        writer.write("  zClass(){} 					\n"); //$NON-NLS-1$
        writer.write("  static const int zField=1; 	\n"); //$NON-NLS-1$
        writer.write("  void zMethod();		\n"); //$NON-NLS-1$
        writer.write("};                    		\n"); //$NON-NLS-1$
        writer.write("int zVar0;					\n"); //$NON-NLS-1$
        writer.write("void zFunction0();			\n"); //$NON-NLS-1$
        writer.write("zClass c;						\n"); //$NON-NLS-1$
        writer.write("namespace test {				\n"); //$NON-NLS-1$
        writer.write(" zClass c;					\n"); //$NON-NLS-1$
        writer.write("}								\n"); //$NON-NLS-1$
        writer.write("int main(int argc, char **argv) {		\n"); //$NON-NLS-1$
        writer.write(" c.zField;//vp3-function scope,2res	\n"); //$NON-NLS-1$
        writer.write(" using namespace test;		\n"); //$NON-NLS-1$
        writer.write(" c.z;//76398	\n"); //$NON-NLS-1$
        writer.write(" return (0);					\n"); //$NON-NLS-1$
        writer.write("}								\n"); //$NON-NLS-1$
         
        String code = writer.toString();
        IFile t = importFile( "test76398.cpp", code ); //$NON-NLS-1$
        //should not show completions for zField, zMethod since they are
		// ambiguous
        //The defect is the zVar0 and zFunction0 and zClass are showing up when
		// they shouldn't
        ICompletionProposal [] results = getResults( t, code.indexOf( ";//76398" ) ); //$NON-NLS-1$
        assertEquals( 0, results.length);
    }
//  test 3 without prefix 'z', qualified, inside scopes
    public void testQualifiedNoPrefix() throws Exception {
   	StringWriter writer = new StringWriter();
       writer.write("class zClass {					\n"); //$NON-NLS-1$
       writer.write(" public:						\n"); //$NON-NLS-1$
       writer.write("  zClass(){} 					\n"); //$NON-NLS-1$
       writer.write("  static const int zField=1; 	\n"); //$NON-NLS-1$
       writer.write("  int zMethod(int i);			\n"); //$NON-NLS-1$
       writer.write("};                    			\n"); //$NON-NLS-1$
       writer.write("zClass c;						\n"); //$NON-NLS-1$
       writer.write("int i = c.zField;//vp1-2res	\n"); //$NON-NLS-1$
       writer.write("namespace test {				\n"); //$NON-NLS-1$
       writer.write(" zClass c;						\n"); //$NON-NLS-1$
       writer.write(" int i=c.zField;//vp2-2res		\n"); //$NON-NLS-1$
       writer.write("}								\n"); //$NON-NLS-1$
       writer.write("int main(int argc, char **argv) {		\n"); //$NON-NLS-1$
       writer.write(" c.zField;//vp3-2res			\n"); //$NON-NLS-1$
       writer.write(" using namespace test;			\n"); //$NON-NLS-1$
       writer.write(" ::c.zMethod(3);//vp4-2res		\n"); //$NON-NLS-1$
       writer.write(" test::c.zField;//vp5-2res		\n"); //$NON-NLS-1$
       writer.write(" return (0);					\n"); //$NON-NLS-1$
       writer.write("}								\n"); //$NON-NLS-1$
       writer.write("int zClass::zMethod(int i){	\n"); //$NON-NLS-1$
       writer.write("	if (i==0) return (0);		\n"); //$NON-NLS-1$
       writer.write("	zClass zz;					\n"); //$NON-NLS-1$
       writer.write("  return (zz.zMethod(i-1));//vp6		\n"); //$NON-NLS-1$
       writer.write("}								\n"); //$NON-NLS-1$
       
       String code = writer.toString();
       IFile t = importFile( "testQualifiedNoPrefix.cpp", code ); //$NON-NLS-1$
       //vp1 global scope
       ICompletionProposal [] results = getResults( t, code.indexOf( "zField;//vp1" ) ); //$NON-NLS-1$
       assertEquals( 2, results.length);
       assertEquals( "zField : const int", 	results[0].getDisplayString()); //$NON-NLS-1$
       assertEquals( "zMethod(int) int", 	results[1].getDisplayString()); //$NON-NLS-1$
       //vp2 namespace scope
       results = getResults( t, code.indexOf( "zField;//vp2" ) ); //$NON-NLS-1$
       assertEquals( 2, results.length);
       assertEquals( "zField : const int", 	results[0].getDisplayString()); //$NON-NLS-1$
       assertEquals( "zMethod(int) int", 	results[1].getDisplayString()); //$NON-NLS-1$
       //vp3 function scope, global class member ref
       results = getResults( t, code.indexOf( "zField;//vp3" ) ); //$NON-NLS-1$
       assertEquals( 2, results.length);
       assertEquals( "zField : const int", 	results[0].getDisplayString()); //$NON-NLS-1$
       assertEquals( "zMethod(int) int", 	results[1].getDisplayString()); //$NON-NLS-1$
       //vp4 function scope, ambiguous clarified(global) class member ref
       results = getResults( t, code.indexOf( "zMethod(3);//vp4" ) ); //$NON-NLS-1$
       assertEquals( 2, results.length);
       assertEquals( "zField : const int", 	results[0].getDisplayString()); //$NON-NLS-1$
       assertEquals( "zMethod(int) int", 	results[1].getDisplayString()); //$NON-NLS-1$
       //vp5 function scope, ambiguous clarified(namespace) class member ref
       results = getResults( t, code.indexOf( "zField;//vp5" ) ); //$NON-NLS-1$
       assertEquals( 2, results.length);
       assertEquals( "zField : const int", 	results[0].getDisplayString()); //$NON-NLS-1$
       assertEquals( "zMethod(int) int", 	results[1].getDisplayString()); //$NON-NLS-1$
       //vp6 method scope
       results = getResults( t, code.indexOf( "zMethod(i-1));//vp6" ) ); //$NON-NLS-1$
       assertEquals( 2, results.length);
       assertEquals( "zField : const int", 	results[0].getDisplayString()); //$NON-NLS-1$
       assertEquals( "zMethod(int) int", 	results[1].getDisplayString()); //$NON-NLS-1$
       
    }
	//  test 7 different file types
	public void testSourceExtensions() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("int zVar;					\n"); //$NON-NLS-1$
	    writer.write("int zFunction(int);		\n"); //$NON-NLS-1$
	    writer.write("struct zStruct {int b;};	\n"); //$NON-NLS-1$
	    writer.write("struct zStruct a;//vp1-1res			\n"); //$NON-NLS-1$
	    writer.write("int main(int argc, char **argv) {		\n"); //$NON-NLS-1$
	    writer.write(" zVar=0;//vp2-3res		\n"); //$NON-NLS-1$
	    writer.write(" return (0);				\n"); //$NON-NLS-1$
	    writer.write("}							\n"); //$NON-NLS-1$
	    String code = writer.toString();
	    IFile t = importFile( "testSourceExtensions.c", code ); //$NON-NLS-1$
	    //vp1 cfile global scope
	    ICompletionProposal [] results = getResults( t, code.indexOf( "Struct a;//vp1" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "zStruct", 	results[0].getDisplayString()); //$NON-NLS-1$
		//vp2 c file function scope
		results = getResults( t, code.indexOf( "Var=0;//vp2" ) ); //$NON-NLS-1$
		assertEquals( 3, results.length);
		assertEquals( "zVar : int", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "zFunction(int) int", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "zStruct", 	results[2].getDisplayString()); //$NON-NLS-1$
		
        removeFile("testSourceExtensions.c"); //$NON-NLS-1$
		t = importFile( "testSourceExtensions.C", code ); //$NON-NLS-1$
	    //vp1 C file global scope
	    results = getResults( t, code.indexOf( "Struct a;//vp1" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "zStruct", 	results[0].getDisplayString()); //$NON-NLS-1$
		//vp2 C file function scope
		results = getResults( t, code.indexOf( "Var=0;//vp2" ) ); //$NON-NLS-1$
		assertEquals( 3, results.length);
		assertEquals( "zVar : int", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "zFunction(int) int", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "zStruct", 	results[2].getDisplayString()); //$NON-NLS-1$
		removeFile("testSourceExtensions.C"); //$NON-NLS-1$
		t = importFile( "testSourceExtensions.cxx", code ); //$NON-NLS-1$
	    //vp1 cxx file global scope
	    results = getResults( t, code.indexOf( "Struct a;//vp1" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "zStruct", 	results[0].getDisplayString()); //$NON-NLS-1$
		//vp2 css file function scope
		results = getResults( t, code.indexOf( "Var=0;//vp2" ) ); //$NON-NLS-1$
		assertEquals( 3, results.length);
		assertEquals( "zVar : int", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "zFunction(int) int", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "zStruct", 	results[2].getDisplayString()); //$NON-NLS-1$
		removeFile("testSourceExtensions.cxx"); //$NON-NLS-1$
		t = importFile( "testSourceExtensions.cc", code ); //$NON-NLS-1$
	    //vp1 cc file global scope
	    results = getResults( t, code.indexOf( "Struct a;//vp1" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "zStruct", 	results[0].getDisplayString()); //$NON-NLS-1$
		//vp2 cc file function scope
		results = getResults( t, code.indexOf( "Var=0;//vp2" ) ); //$NON-NLS-1$
		assertEquals( 3, results.length);
		assertEquals( "zVar : int", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "zFunction(int) int", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "zStruct", 	results[2].getDisplayString()); //$NON-NLS-1$
	
	}	 
	//c code scope operator shouldn't provide CA contributions
	public void test76480() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("int zVar;					\n"); //$NON-NLS-1$
	    writer.write("int zFunction(int);		\n"); //$NON-NLS-1$
	    writer.write("struct zStruct {int b;};	\n"); //$NON-NLS-1$
	    writer.write("int main(int argc, char **argv) {		\n"); //$NON-NLS-1$
	    writer.write(" ::zVar=0;//vp1-0res		\n"); //$NON-NLS-1$
	    writer.write(" return (0);				\n"); //$NON-NLS-1$
	    writer.write("}							\n"); //$NON-NLS-1$
	    String code = writer.toString();
	    IFile t = importFile( "test76480.c", code ); //$NON-NLS-1$
	    //vp1 function scope
		ICompletionProposal [] results = getResults( t, code.indexOf( "Var=0;//vp1" ) ); //$NON-NLS-1$
		assertEquals( 0, results.length);
		
	}	
	// test27: Complete on a field type
	// named struct with bitfield & typedef struct
	public void testField() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer = new StringWriter();
	    writer.write("struct simplestruct {							\n"); //$NON-NLS-1$
	    writer.write(" 	unsigned field1: 2; // range 0-3			\n"); //$NON-NLS-1$
	    writer.write(" 	unsigned int field2: 1; // range 0-1		\n"); //$NON-NLS-1$
	    writer.write("};											\n"); //$NON-NLS-1$
	    writer.write("typedef struct {								\n"); //$NON-NLS-1$
	    writer.write(" 	static int const field2=5; 					\n"); //$NON-NLS-1$
	    writer.write("} structtype;									\n"); //$NON-NLS-1$
	    writer.write("class A{										\n"); //$NON-NLS-1$
	    writer.write("  public: struct {int aa;} a;					\n"); //$NON-NLS-1$
	    writer.write("};											\n"); //$NON-NLS-1$
	    writer.write("int main(int argc, char **argv) {		   		\n"); //$NON-NLS-1$
	    writer.write(" 	struct simplestruct aStruct={3,0}, *pStruct;\n"); //$NON-NLS-1$
	    writer.write(" 	aStruct.field1;//vp1						\n"); //$NON-NLS-1$
	    writer.write("	pStruct->field1;//vp2						\n"); //$NON-NLS-1$
	    writer.write(" 	structtype anotherStruct;					\n"); //$NON-NLS-1$
	    writer.write("	structtype::field2;//vp3					\n"); //$NON-NLS-1$
	    writer.write("	class A myClass;							\n"); //$NON-NLS-1$
	    writer.write("	int i = myClass.a.aa;//vp4					\n"); //$NON-NLS-1$
	    writer.write("	return (0);									\n"); //$NON-NLS-1$
	    writer.write("} 											\n"); //$NON-NLS-1$
	    String code = writer.toString();
	    IFile t = importFile( "testField.cpp", code ); //$NON-NLS-1$
	    //vp1 bitfield accessed from named struct with "."
		ICompletionProposal [] results = getResults( t, code.indexOf( "field1;//vp1" ) ); //$NON-NLS-1$
		assertEquals( 2, results.length);
		assertEquals( "field1 : unsigned", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "field2 : unsigned int", 	results[1].getDisplayString()); //$NON-NLS-1$
		//vp2 bitfield accessed from named struct with "->"
		results = getResults( t, code.indexOf( "field1;//vp2" ) ); //$NON-NLS-1$
		assertEquals( 2, results.length);
		assertEquals( "field1 : unsigned", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "field2 : unsigned int", 	results[1].getDisplayString()); //$NON-NLS-1$
		//vp3 static const field accessed from typedef struct with "::"
		results = getResults( t, code.indexOf( "field2;//vp3" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "field2 : const int", 	results[0].getDisplayString()); //$NON-NLS-1$
		//vp4 class field of type anonymous struct 
		results = getResults( t, code.indexOf( "a.aa;//vp4" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "a : ", 	results[0].getDisplayString()); //$NON-NLS-1$
	
	}
	public void testFieldExpression() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("struct myStruct_c{						\n" ); //$NON-NLS-1$
		writer.write("int m; struct {int m2;} n; 				\n" ); //$NON-NLS-1$
		writer.write("}ss1={1},ss2={2};							\n" ); //$NON-NLS-1$
		writer.write("void f(){									\n" ); //$NON-NLS-1$
		writer.write("int i=(ss1.m > ss2.m/*vp1*/ ? ss1.n/*vp2*/ : ss2.n/*vp3*/).m2/*vp4*/;\n" ); //$NON-NLS-1$
		writer.write("}											\n" ); //$NON-NLS-1$
		String code = writer.toString();
	    IFile f=importFile( "testFieldExpression.cpp", code ); //$NON-NLS-1$
	    //vp1 first operand of ?: on rhs of >
		ICompletionProposal [] results = getResults( f, code.indexOf( "m/*vp1*/" ) ); //$NON-NLS-1$
		assertEquals( "m : int", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "n : ", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( 2, results.length);
		//vp2 second operand of ?:
		results = getResults( f, code.indexOf( "n/*vp2*/" ) ); //$NON-NLS-1$
		assertEquals( "m : int", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "n : ", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( 2, results.length);
		//vp3 third operand of ?:
		results = getResults( f, code.indexOf( "n/*vp3*/" ) ); //$NON-NLS-1$
		assertEquals( "m : int", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "n : ", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( 2, results.length);
		//vp4 dot reference after ?: expression (note ?: expression cannot be lhs of =)
		results = getResults( f, code.indexOf( "m2/*vp4*/" ) ); //$NON-NLS-1$
		assertEquals( "m2 : int", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( 1, results.length);
	}
	//	test30: Complete on scoped reference
	//  nested class/namespaces, unambiguous scoped & unscoped namespace, ambiguous namespace
	public void testScope() throws Exception {
		StringWriter writer = new StringWriter();
	   	writer = new StringWriter();
	    writer.write("namespace NN1 {		\n" ); //$NON-NLS-1$
	    writer.write("	class C1 {};		\n" ); //$NON-NLS-1$
	    writer.write("	namespace NN2 {		\n" ); //$NON-NLS-1$
		writer.write("		class C2{};		\n" ); //$NON-NLS-1$
		writer.write("		class NNA{};	\n" ); //$NON-NLS-1$
		writer.write("		namespace NN3 {	\n" ); //$NON-NLS-1$
		writer.write("			class NNA{};\n" ); //$NON-NLS-1$
		writer.write("			class C3{	\n" ); //$NON-NLS-1$
		writer.write("			 public:	\n" ); //$NON-NLS-1$
		writer.write("			   class C4{};\n" ); //$NON-NLS-1$
		writer.write("			};			\n" ); //$NON-NLS-1$
		writer.write("		}				\n" ); //$NON-NLS-1$
		writer.write("	}					\n" ); //$NON-NLS-1$
		writer.write("}						\n" ); //$NON-NLS-1$
	    String codeH = writer.toString();
	    importFile( "testScope.h", codeH ); //$NON-NLS-1$
		   
	    writer.write("#include \"testScope.h\"					\n" ); //$NON-NLS-1$
	    writer.write("int main(int argc, char **argv) {		\n" ); //$NON-NLS-1$
	    writer.write("	NN1::NN2::NN3::C3::C4 c4;//vp1:C4	\n" ); //$NON-NLS-1$
		writer.write("	using namespace NN1::NN2;			\n" ); //$NON-NLS-1$
		writer.write("	NN3::C3 c3; //vp2:NN1,NN3,NNA; vp3:C3,NNA\n" ); //$NON-NLS-1$
		writer.write("	using namespace NN3;				\n" ); //$NON-NLS-1$
		writer.write("	NN3::NNA a;//vp4:NN1,NN3(Ambiguous space)\n" ); //$NON-NLS-1$
		writer.write("}										\n" ); //$NON-NLS-1$
		String code = writer.toString();
	    IFile f=importFile( "testScope.cpp", code ); //$NON-NLS-1$
	    //vp1 5 levels of nested class/namespaces
		ICompletionProposal [] results = getResults( f, code.indexOf( "C4 c4;//vp1" ) ); //$NON-NLS-1$
		assertEquals( "C4", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( 1, results.length);
		//vp2 unambiguous NNA, unscoped
		results = getResults( f, code.indexOf( "3::C3 c3; //vp2" ) ); //$NON-NLS-1$
		assertEquals( "NNA", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "NN1", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "NN3", 	results[2].getDisplayString()); //$NON-NLS-1$
		assertEquals( 3, results.length);	
		//vp3 unambigous NNA, scoped
		results = getResults( f, code.indexOf( "C3 c3; //vp2:NN1,NN3,NNA; vp3" ) ); //$NON-NLS-1$
		assertEquals( "C3", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "NNA", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( 2, results.length);
		//vp4 ambiguous NNA, not in contribution list
		results = getResults( f, code.indexOf( "3::NNA a;//vp4" ) ); //$NON-NLS-1$
		assertEquals( "NN1", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "NN3", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( 2, results.length);
		
	}
	//	test33
	public void testClass() throws Exception {
		StringWriter writer = new StringWriter();
	   	writer.write("class Point{							\n" ); //$NON-NLS-1$
	    writer.write("public:								\n" ); //$NON-NLS-1$
	    writer.write(" Point(): xCoord(0){}					\n" ); //$NON-NLS-1$
	    writer.write(" Point(int x);						\n" ); //$NON-NLS-1$
        writer.write(" Point(const Point &rhs);				\n" ); //$NON-NLS-1$
        writer.write(" virtual ~Point(){}					\n" ); //$NON-NLS-1$
        writer.write(" int getX() const {return xCoord;}	\n" ); //$NON-NLS-1$
        writer.write("private:								\n" ); //$NON-NLS-1$
	    writer.write(" int xCoord;							\n" ); //$NON-NLS-1$
        writer.write("};									\n" ); //$NON-NLS-1$
        String codeH = writer.toString();
	    importFile( "testClass.h", codeH ); //$NON-NLS-1$
	    
	    writer = new StringWriter();   
	    writer.write("#include \"testClass.h\"					\n" ); //$NON-NLS-1$
	    writer.write("Point::Point(int x):xCoord(x){}		\n" ); //$NON-NLS-1$
		writer.write("Point::Point(const Point &rhs){		\n" ); //$NON-NLS-1$
		writer.write("	 xCoord = rhs.xCoord;				\n" ); //$NON-NLS-1$
		writer.write("}										\n" ); //$NON-NLS-1$
		writer.write("static const Point zero(0);						\n" ); //$NON-NLS-1$
		writer.write("int main(int argc, char **argv) {					\n" ); //$NON-NLS-1$
		writer.write("	 Point *p1 = new ::Point(0);//vp1 scoped class	\n" ); //$NON-NLS-1$
		writer.write("	 Point &p2 = *(new Point(10));					\n" ); //$NON-NLS-1$
		writer.write("	 Point one(1);									\n" ); //$NON-NLS-1$
		writer.write("	 p1->getX();//vp2: arrow getX(), getY(), ~Point(), operator=) 	\n" ); //$NON-NLS-1$
		writer.write("	 p2.getX();//vp3: dot on dereferenced initialization			\n" ); //$NON-NLS-1$
		writer.write("	 one.getX();//vp4: dot on simple initialization					\n" ); //$NON-NLS-1$
		writer.write("	 *(p1) = ::zero;//vp5 scoped class instance						\n" ); //$NON-NLS-1$
		writer.write("	 return (0);													\n" ); //$NON-NLS-1$
		writer.write("}																	\n" ); //$NON-NLS-1$
		String code = writer.toString();
	    IFile f=importFile( "testClass.cpp", code ); //$NON-NLS-1$
	    //vp1 scoped class
		ICompletionProposal [] results = getResults( f, code.indexOf( "oint(0);//vp1" ) ); //$NON-NLS-1$
		assertEquals( "Point", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( 1, results.length);
		//vp2 arrow ref. this will fail when 72723 is fixed because of the
		//sorting of the results: currently: =,~,get; after fix: ~,get,operator=
		results = getResults( f, code.indexOf( "getX();//vp2" ) ); //$NON-NLS-1$
		//assertEquals( "operator=(const Point&) Point&", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "getX() int", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "~Point()", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( 2, results.length);
		//vp3 dot ref on dereferenced initialization
		results = getResults( f, code.indexOf( "getX();//vp3" ) ); //$NON-NLS-1$
		assertEquals( "getX() int", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "~Point()", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( 2, results.length);
		//vp4 dot on simple initialization
		results = getResults( f, code.indexOf( "getX();//vp4" ) ); //$NON-NLS-1$
		assertEquals( "getX() int", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "~Point()", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( 2, results.length);
		//vp5 scoped class instance
		results = getResults( f, code.indexOf( "ero;//vp5" ) ); //$NON-NLS-1$
		assertEquals( "zero : const Point", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( 1, results.length);
		
		
	}
	//	defect 72723 on test 33: Complete on opertator overloads forgets the operator keyword
	public void test72723() throws Exception {
		
			StringWriter writer = new StringWriter();
		   	writer.write("class Point{							\n" ); //$NON-NLS-1$
		    writer.write("public:								\n" ); //$NON-NLS-1$
		    writer.write(" Point(): xCoord(0){}					\n" ); //$NON-NLS-1$
		    writer.write(" Point(int x);						\n" ); //$NON-NLS-1$
	        writer.write(" Point(const Point &rhs);				\n" ); //$NON-NLS-1$
	        writer.write(" virtual ~Point(){}					\n" ); //$NON-NLS-1$
	        writer.write(" int getX() const {return xCoord;}	\n" ); //$NON-NLS-1$
	        writer.write(" Point& operator=(const Point &rhs);	\n" ); //$NON-NLS-1$
	        writer.write("private:								\n" ); //$NON-NLS-1$
		    writer.write(" int xCoord;							\n" ); //$NON-NLS-1$
	        writer.write("};									\n" ); //$NON-NLS-1$
	        String codeH = writer.toString();
		    importFile( "test72723.h", codeH ); //$NON-NLS-1$
		    
		    writer = new StringWriter();   
		    writer.write("#include \"test72723.h\"				\n" ); //$NON-NLS-1$
			writer.write("Point::Point(int x):xCoord(x){}		\n" ); //$NON-NLS-1$
			writer.write("Point::Point(const Point &rhs){		\n" ); //$NON-NLS-1$
			writer.write("	 xCoord = rhs.xCoord;				\n" ); //$NON-NLS-1$
			writer.write("}										\n" ); //$NON-NLS-1$
			writer.write("Point& Point::operator=(const Point &rhs){\n" ); //$NON-NLS-1$
			writer.write("	 if (this == &rhs) return *this;	\n" ); //$NON-NLS-1$
			writer.write("	 xCoord = rhs.xCoord;				\n" ); //$NON-NLS-1$
			writer.write("	 return *this;						\n" ); //$NON-NLS-1$
			writer.write("}										\n" ); //$NON-NLS-1$
			writer.write("static const Point zero(0);			\n" ); //$NON-NLS-1$
			writer.write("int main(int argc, char **argv) {		\n" ); //$NON-NLS-1$
			writer.write("	 Point *p1 = new ::Point(0);		\n" ); //$NON-NLS-1$
			writer.write("	 Point &p2 = *(new Point(10));			\n" ); //$NON-NLS-1$
			writer.write("	 p1->operator=(zero);//vp1: arrow ref 	\n" ); //$NON-NLS-1$
			writer.write("	 p2.operator=(zero);//vp2: dot ref 		\n" ); //$NON-NLS-1$
			writer.write("	 return (0);							\n" ); //$NON-NLS-1$
			writer.write("}											\n" ); //$NON-NLS-1$
			String code = writer.toString();
		    IFile f=importFile( "test72723.cpp", code ); //$NON-NLS-1$
		    //vp1 arrow ref
		    ICompletionProposal [] results = getResults( f, code.indexOf( "perator=(zero);//vp3" ) ); //$NON-NLS-1$
		    assertEquals( 1, results.length);
			assertEquals( "operator=(const Point&) Point&", 	results[0].getDisplayString()); //$NON-NLS-1$
				
	}
	//	defect 72541 on test 33: Complete on const missing suggestions
	public void test72541() throws Exception {
		StringWriter writer = new StringWriter();
	   	writer.write("class Point {public Point(); int X;};		\n" ); //$NON-NLS-1$
	    writer.write("const Point zero;							\n" ); //$NON-NLS-1$
	    writer.write("int main() {return zero.X;}//vp1 			\n" ); //$NON-NLS-1$
		String code = writer.toString();
	    IFile f=importFile( "test72541.h", code ); //$NON-NLS-1$
		   
	    //vp1 completion on const class
		ICompletionProposal [] results = getResults( f, code.indexOf( "X;}//vp1" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);	
		assertEquals( "X : const int", 	results[0].getDisplayString()); //$NON-NLS-1$
		
	}
	// test 36
	public void testFunction() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("namespace nnnn {				\n"); //$NON-NLS-1$
	   	writer.write("	namespace nnn {				\n"); //$NON-NLS-1$
	   	writer.write("	  void foo(){}				\n"); //$NON-NLS-1$
	   	writer.write("	}							\n"); //$NON-NLS-1$
	    writer.write("}								\n"); //$NON-NLS-1$
        writer.write("int main(int argc, char **argv) {\n"); //$NON-NLS-1$
	   	writer.write("	namespace n=nnnn::nnn;		\n"); //$NON-NLS-1$
	   	writer.write("	n::foo();//vp1				\n"); //$NON-NLS-1$
	   	writer.write("	using namespace nnnn;		\n"); //$NON-NLS-1$
	   	writer.write("	nnn::foo();//vp2			\n"); //$NON-NLS-1$
	   	writer.write("  return (0);					\n"); //$NON-NLS-1$
	   	writer.write("}								\n"); //$NON-NLS-1$
	    String code = writer.toString();
	    IFile t = importFile( "testFunction.cpp", code ); //$NON-NLS-1$
	    //vp1 namespace alias scope
		ICompletionProposal [] results = getResults( t, code.indexOf( "foo();//vp1" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "foo() void", 	results[0].getDisplayString()); //$NON-NLS-1$
		//vp2 nested namespace scope
		results = getResults( t, code.indexOf( "oo();//vp2" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "foo() void", 	results[0].getDisplayString()); //$NON-NLS-1$
		
	}	
	//test 44 argument types from both function declaration and function call
	public void testArgument() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("typedef long int32_t;			\n"); //$NON-NLS-1$
	   	writer.write("#define INT32_C(x) x ## L		\n"); //$NON-NLS-1$
	   	writer.write("#define INT32_MAX (2147483647) \n"); //$NON-NLS-1$
	   	writer.write("int foo(int32_t i){//vp1contributions include types in arg list\n"); //$NON-NLS-1$
	   	writer.write("	return (i);					\n"); //$NON-NLS-1$
	   	writer.write("}								\n"); //$NON-NLS-1$
	   	writer.write("int z(int i){return (i);}		\n"); //$NON-NLS-1$
	   	writer.write("int main(int argc, char **argv) {\n"); //$NON-NLS-1$
	   	writer.write("	int32_t int32iii=2;			\n"); //$NON-NLS-1$
	   	writer.write("	foo(INT32_C(2));//vp2 macros\n"); //$NON-NLS-1$
	   	writer.write("	foo(INT32_MAX);//vp3 defines\n"); //$NON-NLS-1$
	   	writer.write("	foo(int32iii);//vp4 variable\n"); //$NON-NLS-1$
	   	writer.write("	foo(z(5));//vp5 function arg		\n"); //$NON-NLS-1$
	   	writer.write("	return (0);					\n"); //$NON-NLS-1$
	   	writer.write("}								\n"); //$NON-NLS-1$
	   	String code = writer.toString();
	    IFile t = importFile( "testArgument.c", code ); //$NON-NLS-1$
	    //vp1 function declaration args contain type only
		ICompletionProposal [] results = getResults( t, code.indexOf( "32_t i){//vp1" ) ); //$NON-NLS-1$
		//assertEquals( 15, results.length);
		assertEquals( 2, results.length);
		assertEquals( "int32_t", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "int", 		results[1].getDisplayString()); //$NON-NLS-1$
		//vp2 function call args contain #macros, variables but not types
		results = getResults( t, code.indexOf( "(2));//vp2" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "INT32_C(x)", results[0].getDisplayString()); //$NON-NLS-1$
		//vp3 function call args contain #defines
		results = getResults( t, code.indexOf( "AX);//vp3" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "INT32_MAX", 	results[0].getDisplayString()); //$NON-NLS-1$
		//assertEquals( "INT32_MIN", 	results[1].getDisplayString()); //$NON-NLS-1$
		//vp4 function call args contain variables
		results = getResults( t, code.indexOf( "ii);//vp4" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "int32iii : int32_t", 	results[0].getDisplayString()); //$NON-NLS-1$
		//vp5 function call args can contain functions
		results = getResults( t, code.indexOf( "(5));//vp5" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "z(int) int", results[0].getDisplayString()); //$NON-NLS-1$
		
	}

	//	arguments in function call shouldn't contain types
	public void test76805() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("typedef long int32_t;			\n"); //$NON-NLS-1$
	   	writer.write("#define INT32_MAX (2147483647) \n"); //$NON-NLS-1$
	   	writer.write("int foo(int32_t i){			\n"); //$NON-NLS-1$
	   	writer.write("	return (i);					\n"); //$NON-NLS-1$
	   	writer.write("}								\n"); //$NON-NLS-1$
	   	writer.write("int main(int argc, char **argv) {\n"); //$NON-NLS-1$
	   	writer.write("	int32_t int32iii=2;			\n"); //$NON-NLS-1$
	   	writer.write("	foo(INT32_MAX);//vp1 macros, variables, not types\n"); //$NON-NLS-1$
	   	writer.write("	return (0);					\n"); //$NON-NLS-1$
	   	writer.write("}								\n"); //$NON-NLS-1$
	   	String code = writer.toString();
	    IFile t = importFile( "test76805.c", code ); //$NON-NLS-1$
	    //vp1 function call args contain #defines, variables but not types
	    //int32_t should not be in the list
	    ICompletionProposal [] results = getResults( t, code.indexOf( "32_MAX);//vp1" ) ); //$NON-NLS-1$
	    assertEquals( "INT32_MAX", 			results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "int32iii : int32_t", results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( 2, results.length);
		
	}
	//	test 50 type cast c code: narrowing cast & struct cast; 
	//	cpp code: class cast, override typecheck cast, deref cast
	public void testCStyleCast() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("typedef int ZINT;			\n"); //$NON-NLS-1$
	   	writer.write("typedef struct {			\n"); //$NON-NLS-1$
	   	writer.write("	int foobar;				\n"); //$NON-NLS-1$
		writer.write("}foo_c;					\n"); //$NON-NLS-1$
	   	writer.write("typedef struct {} bar_c;	\n"); //$NON-NLS-1$
	   	writer.write("int main(int argc, char **argv) {\n"); //$NON-NLS-1$
	   	writer.write("	ZINT i=(ZINT) 3.1;//vp1 narrowing cast\n"); //$NON-NLS-1$
	   	writer.write("	bar_c *bar;				\n"); //$NON-NLS-1$
	   	writer.write("	foo_c *foo;				\n"); //$NON-NLS-1$
	   	writer.write("	((foo_c*) bar)->foobar;//vp2 complete on struct casted var\n"); //$NON-NLS-1$
	   	writer.write("}								\n"); //$NON-NLS-1$
	   	String code = writer.toString();
	    IFile t = importFile( "testCStyleCast.c", code ); //$NON-NLS-1$
	    ICompletionProposal [] results = getResults( t, code.indexOf( "INT) 3.1;//vp1" ) ); //$NON-NLS-1$
	    assertEquals( "ZINT", 		results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 1, results.length);
		results = getResults( t, code.indexOf( "foobar;//vp2" ) ); //$NON-NLS-1$
		assertEquals( "foobar : int", 		results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 1, results.length);
	    removeFile("testCStyleCase.c");//$NON-NLS-1$
	    
	    writer=new StringWriter();
	    writer.write("typedef int ZINT;						\n"); //$NON-NLS-1$
	    writer.write("class foo_c{								\n"); //$NON-NLS-1$
	    writer.write("		public:	void foobar(){}				\n"); //$NON-NLS-1$
	    writer.write("};										\n"); //$NON-NLS-1$
	    writer.write("class bar_c {};							\n"); //$NON-NLS-1$
  		writer.write("int main(int argc, char **argv) {			\n"); //$NON-NLS-1$
		writer.write("	bar_c *bar;								\n"); //$NON-NLS-1$
		writer.write("	foo_c *foo;								\n"); //$NON-NLS-1$
		writer.write("	((foo_c*) bar)->foobar();//vp1 complete on class casted var\n"); //$NON-NLS-1$
		writer.write("	//cast away constness					\n"); //$NON-NLS-1$
		writer.write("	ZINT i=3;								\n"); //$NON-NLS-1$
		writer.write("	const int *pci = &i;					\n"); //$NON-NLS-1$
	 	writer.write("	void * pv=(void*)pci;//vp2 override typecheck cast \n"); //$NON-NLS-1$
		writer.write("	(*(ZINT *)pv)=4;//vp3 dereferenced casted var\n"); //$NON-NLS-1$
		writer.write("			return 0;						\n"); //$NON-NLS-1$
		writer.write("}								\n"); //$NON-NLS-1$
 	   	code = writer.toString();
  	    t = importFile( "testCStyleCast.cpp", code ); //$NON-NLS-1$
   	    results = getResults( t, code.indexOf( "foobar();//vp1" ) ); //$NON-NLS-1$
  	    assertEquals( "foobar() void", 		results[0].getDisplayString()); //$NON-NLS-1$
  	    assertEquals( 1, results.length);
  		results = getResults( t, code.indexOf( "d*)pci;//vp2" ) ); //$NON-NLS-1$
  		assertEquals( "void", 		results[0].getDisplayString()); //$NON-NLS-1$
  	    assertEquals( 1, results.length);
  	    results = getResults( t, code.indexOf( "INT *)pv)=4;//vp3" ) ); //$NON-NLS-1$
		assertEquals( "ZINT", 		results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 1, results.length);
	    		
	}
	//test 59
	public void testMethod() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("class Point{					\n"); //$NON-NLS-1$
	   	writer.write("	public: Point(): x(5){}		\n"); //$NON-NLS-1$
		writer.write("	Point& compare(const Point &rhs) {\n"); //$NON-NLS-1$
		writer.write("	 (this==&rhs);//vp1			\n"); //$NON-NLS-1$
		writer.write("	 return *this;//vp2			\n"); //$NON-NLS-1$
		writer.write("	}							\n"); //$NON-NLS-1$
		writer.write("	int x;						\n"); //$NON-NLS-1$
		writer.write("};							\n"); //$NON-NLS-1$
		writer.write("int main(int argc, char **argv) {\n"); //$NON-NLS-1$
		writer.write("	Point *p1=new Point();		\n"); //$NON-NLS-1$
		writer.write("	Point &p2=*(new Point());	\n"); //$NON-NLS-1$
		writer.write("	Point **p3;					\n"); //$NON-NLS-1$
		writer.write("	**p3=p2;//vp3 content assist doesn't filter out pointer types by design\n"); //$NON-NLS-1$
		writer.write("	(**p3).x;//vp4 correct dereference\n"); //$NON-NLS-1$
		writer.write("	(*p3).x;//vp5 too few stars - CA notices\n"); //$NON-NLS-1$
		writer.write("	return 0;					\n"); //$NON-NLS-1$
		writer.write("}								\n"); //$NON-NLS-1$
	   	String code = writer.toString();
	    IFile t = importFile( "testMethod.c", code ); //$NON-NLS-1$
	    //vp1 complete on & var
	    ICompletionProposal [] results = getResults( t, code.indexOf( "hs);//vp1" ) ); //$NON-NLS-1$
	    assertEquals( "rhs : const Point&", results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "register", 			results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "reinterpret_cast", 	results[2].getDisplayString()); //$NON-NLS-1$
	    //does return make sense even when there's no &
	    assertEquals( "return", 			results[3].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 4, results.length);
		//vp2 complete on *this
	    results = getResults( t, code.indexOf( "s;//vp2" ) ); //$NON-NLS-1$
		assertEquals( "this", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 1, results.length);
	    //vp3 ignore stars by design, so CA correctly returns all local vars
	    results = getResults( t, code.indexOf( "3=p2;//vp3" ) ); //$NON-NLS-1$
		assertEquals( "p1 : Point*", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "p2 : Point&", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "p3 : Point**", 	results[2].getDisplayString()); //$NON-NLS-1$
		assertEquals( "Point", 			results[3].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 4, results.length);
	    
	    //vp4 complete on members of correctly dereferenced object
	    results = getResults( t, code.indexOf( "x;//vp4" ) ); //$NON-NLS-1$
		assertEquals( "x : int", 		results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "compare(const Point&) Point&", 		results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 2, results.length);
	    
	    //vp5 no completions available for incorrectly dereferenced object
	    results = getResults( t, code.indexOf( "x;//vp5" ) ); //$NON-NLS-1$
		assertEquals( 0, results.length);
	    
	}   
	// test 62
	public void testEnumerations() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("enum day {ztuesday, zthursday};	\n" ); //$NON-NLS-1$
	   	writer.write("enum {zTGIF};					\n" ); //$NON-NLS-1$
	   	writer.write("int i=ztuesday;//vp1 				\n" ); //$NON-NLS-1$
	   	writer.write("class Bar {					\n" ); //$NON-NLS-1$
	  	writer.write(" public:						\n" ); //$NON-NLS-1$
	   	writer.write("	enum R4 {R1} num;			\n" ); //$NON-NLS-1$
	   	writer.write("	enum {R2};					\n" ); //$NON-NLS-1$
	   	writer.write("	static enum {R3} Rnum2;				\n" ); //$NON-NLS-1$
	   	writer.write("	day nd;						\n" ); //$NON-NLS-1$
	   	writer.write("};							\n" ); //$NON-NLS-1$
	   	writer.write("void foo(){					\n" ); //$NON-NLS-1$
	   	writer.write("	day d;//vp2						\n" ); //$NON-NLS-1$
	   	writer.write("	d=ztuesday;//vp3 			\n" ); //$NON-NLS-1$
	   	writer.write("	Bar::R1;//vp4 				\n" ); //$NON-NLS-1$
	   	writer.write("	Bar b; 					\n" ); //$NON-NLS-1$
	   	writer.write("	b.R3;//vp5 					\n" ); //$NON-NLS-1$
	   	writer.write("}								\n" ); //$NON-NLS-1$
	   	String code = writer.toString();
	    IFile t = importFile( "testEnumerations.cpp", code ); //$NON-NLS-1$
	    //vp1 global enumerator reference in global scope, RHS
	    ICompletionProposal [] results = getResults( t, code.indexOf( "uesday;//vp1" ) ); //$NON-NLS-1$
	    assertEquals( "zthursday", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "ztuesday", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "zTGIF", 		results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 3, results.length);
	    //vp2 enumeration reference in function
	    results = getResults( t, code.indexOf( "y d;//vp2" ) ); //$NON-NLS-1$
	    assertEquals( "day", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 1, results.length);
	    //vp3 global enumerator reference in function scope, RHS
	    results = getResults( t, code.indexOf( "uesday;//vp3" ) ); //$NON-NLS-1$
	    assertEquals( "zthursday", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "ztuesday", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "zTGIF", 		results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 3, results.length);
	    //vp4 enum & enumerator "::" reference, no prefix
	    results = getResults( t, code.indexOf( "R1;//vp4" ) ); //$NON-NLS-1$
	    assertEquals( "Rnum2 : ", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "R1", 		results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "R2", 		results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "R3", 		results[3].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "R4", 		results[4].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 5, results.length);
	    //vp5 enumerator "." reference, prefix
	    results = getResults( t, code.indexOf( "3;//vp5" ) ); //$NON-NLS-1$
	    assertEquals( 4, results.length);
	    assertEquals( "Rnum2 : ", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "R1", 		results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "R2", 		results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "R3", 		results[3].getDisplayString()); //$NON-NLS-1$
	    
	}
	//test 70
	public void testLongTokens() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("int i123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890;\n" ); //$NON-NLS-1$
	   	writer.write("typedef int t123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890;\n" ); //$NON-NLS-1$
	   	writer.write("class A {\n" ); //$NON-NLS-1$
	   	writer.write(" A(t123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890 a){\n" ); //$NON-NLS-1$
	   	writer.write("   i123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890=0;//vp1 \n" ); //$NON-NLS-1$
	   	writer.write("   t123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890 t=a;//vp2 \n" ); //$NON-NLS-1$
	   	writer.write("   a=i123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890;//vp3 \n" ); //$NON-NLS-1$
	   	writer.write(" }\n" ); //$NON-NLS-1$
	   	writer.write("};\n" ); //$NON-NLS-1$
	   	String code = writer.toString();
	    IFile t = importFile( "testLongTokens.cpp", code ); //$NON-NLS-1$
	    //vp1 complete on long var
	    ICompletionProposal [] results = getResults( t, code.indexOf( "3456789012345678901234567890123456789012345678901234567890123456789012345678901234567890=0;//vp1" ) ); //$NON-NLS-1$
	    assertEquals( "i123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890 : int", 	results[0].getDisplayString()); //$NON-NLS-1$
	    //vp2 proposal has long type 
	    results = getResults( t, code.indexOf( ";//vp2" ) ); //$NON-NLS-1$
	    assertEquals( "a : t123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", 	results[0].getDisplayString()); //$NON-NLS-1$
	    //vp3 complete on rhs long var with long prefix  
	    results = getResults( t, code.indexOf( "7890;//vp3" ) ); //$NON-NLS-1$
	    assertEquals( "i123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890 : int", 	results[0].getDisplayString()); //$NON-NLS-1$
	}    
	//test 73
	public void testCastMultiLevel() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("class Gee{						\n" ); //$NON-NLS-1$
	   	writer.write("	public: struct {int g1;} g0;	\n" ); //$NON-NLS-1$
		writer.write("};								\n" ); //$NON-NLS-1$
		writer.write("class Foo {						\n" ); //$NON-NLS-1$
		writer.write("	public:	Gee *f0;				\n" ); //$NON-NLS-1$
		writer.write("};								\n" ); //$NON-NLS-1$
		writer.write("class C {							\n" ); //$NON-NLS-1$
		writer.write("	void coo(int i){				\n" ); //$NON-NLS-1$
		writer.write("	  switch (i) {					\n" ); //$NON-NLS-1$
		writer.write("	    (*((new Foo())->f0)).g0.g1;//vp1 \n" ); //$NON-NLS-1$
		writer.write("	    Gee *g;						\n" ); //$NON-NLS-1$
		writer.write("	    ((Foo*)g)->f0;//vp2 		\n" ); //$NON-NLS-1$
		writer.write("	    (*(((Foo*)g)->f0)).g0.g1;//vp3,vp4 \n" ); //$NON-NLS-1$
		writer.write("	  }								\n" ); //$NON-NLS-1$
		writer.write("	}								\n" ); //$NON-NLS-1$
		writer.write("};								\n" ); //$NON-NLS-1$
		String code = writer.toString();
	    IFile t = importFile( "testCastMultiLevel.cpp", code ); //$NON-NLS-1$
	    //vp1 multilevel qual
	    ICompletionProposal [] results = getResults( t, code.indexOf( "g1;//vp1" ) ); //$NON-NLS-1$
	    assertEquals( "g1 : int", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 1, results.length);
	    //vp2 cast & multilevel qual on ->
	    results = getResults( t, code.indexOf( "f0;//vp2" ) ); //$NON-NLS-1$
	    assertEquals( "f0 : Gee*", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 1, results.length);
	    //vp3 cast & multilevel qual on . (class member)
	    results = getResults( t, code.indexOf( "g0.g1;//vp3" ) ); //$NON-NLS-1$
	    assertEquals( "g0 : ", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 1, results.length);
	    //vp4 cast & multilevel qual on . (struct member)
	    results = getResults( t, code.indexOf( "g1;//vp3,vp4" ) ); //$NON-NLS-1$
	    assertEquals( "g1 : int", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 1, results.length);
	   
	}
	//test 79
	public void testHeaderExtensions() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("class C1{				\n" ); //$NON-NLS-1$
		writer.write("	enum {C1a, C1b};	\n" ); //$NON-NLS-1$
		writer.write("	C1() {				\n" ); //$NON-NLS-1$
		writer.write("		C1;//vp1		\n" ); //$NON-NLS-1$
		writer.write("	}					\n" ); //$NON-NLS-1$
		writer.write("};					\n" ); //$NON-NLS-1$
		String code = writer.toString();
	    IFile t = importFile( "testHeaderExtensions.h", code ); //$NON-NLS-1$
	    //vp1 h file
	    ICompletionProposal [] results = getResults( t, code.indexOf( "1;//vp1" ) ); //$NON-NLS-1$
	    assertEquals( "C1", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C1a", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C1b", 	results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 3, results.length);
	    removeFile("testHeaderExtensions.h"); //$NON-NLS-1$
	    
	    //vp2 H file
	    writer = new StringWriter();
	   	writer.write("class C{				\n" ); //$NON-NLS-1$
		writer.write("	public: enum {C1, C3};		\n" ); //$NON-NLS-1$
		writer.write("	C() {				\n" ); //$NON-NLS-1$
		writer.write("		C1;		\n" ); //$NON-NLS-1$
		writer.write("	}					\n" ); //$NON-NLS-1$
		writer.write("};					\n" ); //$NON-NLS-1$
		writer.write("namespace N {			\n" ); //$NON-NLS-1$
		writer.write("int i =C::C1;//vp2			\n" ); //$NON-NLS-1$
		writer.write("}						\n" ); //$NON-NLS-1$
		code = writer.toString();
	    t = importFile( "testHeaderExtensions.H", code ); //$NON-NLS-1$
	    //vp2 H file, namespace scope, class context
	    results = getResults( t, code.indexOf( "C1;//vp2" ) ); //$NON-NLS-1$
	    assertEquals( "C()", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C1", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C3", 	results[2].getDisplayString()); //$NON-NLS-1$
	    //trace showed 4 lookup results
	    assertEquals( 3, results.length);
	    removeFile("testHeaderExtensions.H"); //$NON-NLS-1$
	    
	    // vp3 hxx file
	    writer = new StringWriter();
		writer.write("class C{				\n" ); //$NON-NLS-1$
		writer.write("	enum {C1, C3};		\n" ); //$NON-NLS-1$
		writer.write("	C() {				\n" ); //$NON-NLS-1$
		writer.write("		C1;//vp3		\n" ); //$NON-NLS-1$
		writer.write("	}					\n" ); //$NON-NLS-1$
		writer.write("};					\n" ); //$NON-NLS-1$
		code = writer.toString();
		t = importFile( "testHeaderExtensions.hxx", code ); //$NON-NLS-1$
	    //vp3 hxx file, method scope
	    results = getResults( t, code.indexOf( "1;//vp3" ) ); //$NON-NLS-1$
	    assertEquals( "C", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C1", results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C3", 	results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 3, results.length);
	    removeFile("testHeaderExtensions.hxx"); //$NON-NLS-1$
	    
	    //vp4 hh file
	    writer = new StringWriter();
	   	writer.write("class C{				\n" ); //$NON-NLS-1$
		writer.write("	public: enum {C1, C3};		\n" ); //$NON-NLS-1$
		writer.write("	C() {				\n" ); //$NON-NLS-1$
		writer.write("		C1;		\n" ); //$NON-NLS-1$
		writer.write("	}					\n" ); //$NON-NLS-1$
		writer.write("};					\n" ); //$NON-NLS-1$
		writer.write("namespace N {			\n" ); //$NON-NLS-1$
		writer.write("int i =C::C1;//vp4			\n" ); //$NON-NLS-1$
		writer.write("}						\n" ); //$NON-NLS-1$
		code = writer.toString();
	    t = importFile( "testHeaderExtensions.hh", code ); //$NON-NLS-1$
	    //vp4 hh file, namespace scope, class context
	    results = getResults( t, code.indexOf( "C1;//vp4" ) ); //$NON-NLS-1$
	    assertEquals( "C()", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C1", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C3", 	results[2].getDisplayString()); //$NON-NLS-1$
	    //trace showed 4 lookup results
	    assertEquals( 3, results.length);
	    removeFile("testHeaderExtensions.hh"); //$NON-NLS-1$
	    
	    //vp5 hpp file
	    writer = new StringWriter();
	   	writer.write("class C{				\n" ); //$NON-NLS-1$
		writer.write("	public: enum {C1, C3};		\n" ); //$NON-NLS-1$
		writer.write("	C() {				\n" ); //$NON-NLS-1$
		writer.write("		C1;		\n" ); //$NON-NLS-1$
		writer.write("	}					\n" ); //$NON-NLS-1$
		writer.write("};					\n" ); //$NON-NLS-1$
		writer.write("namespace N {			\n" ); //$NON-NLS-1$
		writer.write("int i =C::C1;//vp5			\n" ); //$NON-NLS-1$
		writer.write("}						\n" ); //$NON-NLS-1$
		code = writer.toString();
	    t = importFile( "testHeaderExtensions.H", code ); //$NON-NLS-1$
	    //vp5 hpp file, namespace scope, class context
	    results = getResults( t, code.indexOf( "C1;//vp5" ) ); //$NON-NLS-1$
	    assertEquals( "C()", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C1", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C3", 	results[2].getDisplayString()); //$NON-NLS-1$
	    //trace showed 4 lookup results
	    assertEquals( 3, results.length);
	    
	}
	//test 9
	public void testEmptyDocument() throws Exception {
	   	String code = "\0"; //$NON-NLS-1$
	    IFile t = importFile( "testEmptyDocument.h", code ); //$NON-NLS-1$
	    //vp1 no prefix, keywords.
	    ICompletionProposal [] results = getResults( t, 0 ); //$NON-NLS-1$
	    assertEquals( "asm", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "auto", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "bool", 	results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "wchar_t", results[results.length-1].getDisplayString()); //$NON-NLS-1$
	    StringWriter writer = new StringWriter();
	   	//vp2 prefix, keywords
	    writer.write("s//vp2" ); //$NON-NLS-1$
	    code =writer.toString();
	   	t = importFile( "testEmptyDocument.cpp", code ); //$NON-NLS-1$
	   	results = getResults( t, code.indexOf( "//vp2" ) ); //$NON-NLS-1$
	    assertEquals( "short", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "signed", results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "static", results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "struct", results[3].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 4, results.length);
		      
	}
	public void testVariable() throws Exception {
		StringWriter writer = new StringWriter();
	   	writer.write("namespace N {					\n" ); //$NON-NLS-1$
	    writer.write("	class ClassT {};			\n" ); //$NON-NLS-1$
	    writer.write("	struct StructT {};			\n" ); //$NON-NLS-1$
		writer.write("	typedef struct {} TypedefT;	\n" ); //$NON-NLS-1$
		writer.write("	union UnionT {};			\n" ); //$NON-NLS-1$
		writer.write("	ClassT var6;				\n" ); //$NON-NLS-1$
		writer.write("	ClassT var7=var6/*vp6*/;				\n" ); //$NON-NLS-1$
		writer.write("}								\n" ); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h=importFile( "testVariable.h", header ); //$NON-NLS-1$
	    writer = new StringWriter();
	    writer.write("#include \"testVariable.h\"	\n" ); //$NON-NLS-1$
	    writer.write("using namespace N;			\n" ); //$NON-NLS-1$
	    writer.write("ClassT var1(){}				\n" ); //$NON-NLS-1$
		writer.write("int f(){						\n" ); //$NON-NLS-1$
	    writer.write("	StructT var2;    			\n" ); //$NON-NLS-1$
		writer.write("	TypedefT var3;     			\n" ); //$NON-NLS-1$
		writer.write("	UnionT var4;				\n" ); //$NON-NLS-1$
		writer.write("	bool var5;	 				\n" ); //$NON-NLS-1$
		writer.write("	var5/*vp1*/;	 			\n" ); //$NON-NLS-1$
		writer.write("}								\n" ); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile f=importFile( "testVariable.cpp", source ); //$NON-NLS-1$
	    ICompletionProposal [] results = getResults( f, source.indexOf( "5/*vp1*/" ) ); //$NON-NLS-1$
		assertEquals( "var2 : StructT", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "var3 : TypedefT", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "var4 : UnionT", 	results[2].getDisplayString()); //$NON-NLS-1$
		assertEquals( "var5 : bool", 	results[3].getDisplayString()); //$NON-NLS-1$
		assertEquals( "var6 : ClassT", 	results[4].getDisplayString()); //$NON-NLS-1$
		assertEquals( "var7 : ClassT", 	results[5].getDisplayString()); //$NON-NLS-1$
		assertEquals( "var1() ClassT", 	results[6].getDisplayString()); //$NON-NLS-1$
		assertEquals( 7, results.length);
		//vp2 namespace scope
		results = getResults( h, header.indexOf( "6/*vp6*/" ) ); //$NON-NLS-1$
		assertEquals( "var6 : ClassT", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( 1, results.length);
		
	}

	public void test80510() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("int aaa;							\n" ); //$NON-NLS-1$
	   	writer.write("class Class1{						\n" ); //$NON-NLS-1$
	   	writer.write("		Class1();					\n" ); //$NON-NLS-1$
	   	writer.write("	int abc;						\n" ); //$NON-NLS-1$
	   	writer.write("};								\n" ); //$NON-NLS-1$
	   	writer.write("Class1::Class1(): abc/*vp1*/(2){}	\n" ); //$NON-NLS-1$
  		String code = writer.toString();
	    IFile t = importFile( "test80510.cpp", code ); //$NON-NLS-1$
	    //vp1 
	    ICompletionProposal [] results = getResults( t, code.indexOf( "bc/*vp1*/" ) ); //$NON-NLS-1$
	    assertEquals( 1, results.length);
		assertEquals( "abc : int", 	results[0].getDisplayString()); //$NON-NLS-1$
	 }
	public void testSingleNameReference() throws Exception {
		StringWriter writer = new StringWriter();
	   	writer.write("int snrV;							\n" ); //$NON-NLS-1$
	   	writer.write("int snrF(int x){}					\n" ); //$NON-NLS-1$
		writer.write("class snrC{void g();int snrFd;};	\n" ); //$NON-NLS-1$
		writer.write("struct snrS{};					\n" ); //$NON-NLS-1$
		writer.write("enum snrE{snrER};					\n" ); //$NON-NLS-1$
		writer.write("void foo() {						\n" ); //$NON-NLS-1$
		writer.write("	union snrU{ snrU(int a) {};int i; char* j;};\n" ); //$NON-NLS-1$
		writer.write("	snrU sn(snrV);					\n" ); //$NON-NLS-1$
		writer.write("	snrV/*vp1*/=snrF/*vp2*/(snrV);	\n" ); //$NON-NLS-1$
		writer.write("}									\n" ); //$NON-NLS-1$
		writer.write("void snrC::g(){					\n" ); //$NON-NLS-1$
		writer.write("	snrFd/*vp3*/ c;					\n" ); //$NON-NLS-1$
		writer.write("}									\n" ); //$NON-NLS-1$
		String source = writer.toString();
	    IFile cpp=importFile( "testSingleNameReference.cpp", source ); //$NON-NLS-1$
	    //vp1 function scope, lvalue
	    ICompletionProposal [] results = getResults( cpp, source.indexOf( "rV/*vp1*/" ) ); //$NON-NLS-1$
	    assertEquals( "sn : snrU", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrV : int", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrF(int) int", 	results[2].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrC", 	results[3].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrS", 	results[4].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrU", 	results[5].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrER", 	results[6].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrE", 	results[7].getDisplayString()); //$NON-NLS-1$
		assertEquals( 8, results.length);
		//vp1 function scope, RHS
	    results = getResults( cpp, source.indexOf( "rF/*vp2*/" ) ); //$NON-NLS-1$
	    assertEquals( "sn : snrU", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrV : int", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrF(int) int", 	results[2].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrC", 	results[3].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrS", 	results[4].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrU", 	results[5].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrER", 	results[6].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrE", 	results[7].getDisplayString()); //$NON-NLS-1$
		assertEquals( 8, results.length);
		//vp1 method scope
	    results = getResults( cpp, source.indexOf( "rFd/*vp3*/" ) ); //$NON-NLS-1$
	    assertEquals( "snrFd : int", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrV : int", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrF(int) int", 	results[2].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrC", 	results[3].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrS", 	results[4].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrER", 	results[5].getDisplayString()); //$NON-NLS-1$
		assertEquals( "snrE", 	results[6].getDisplayString()); //$NON-NLS-1$
		assertEquals( 7, results.length);
	
	}
	public void testNamespace() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write( "namespace Foo{ 				\n" ); //$NON-NLS-1$
		writer.write( "int x; 						\n" ); //$NON-NLS-1$
	    writer.write( " namespace Baz {				\n" ); //$NON-NLS-1$
	    writer.write( "   int i;   					\n" ); //$NON-NLS-1$
	    writer.write( " }							\n" ); //$NON-NLS-1$
	    writer.write( " using namespace Baz/*vp1*/;	\n" ); //$NON-NLS-1$
	    writer.write( "}							\n" ); //$NON-NLS-1$
	    writer.write( "void g() {					\n" ); //$NON-NLS-1$
	    writer.write( "	 Foo::i/*vp2*/ = 1;			\n" ); //$NON-NLS-1$
	    writer.write( "  int y=Foo::Baz::i;//vp3	\n" ); //$NON-NLS-1$
	    writer.write( "}  				        	\n" ); //$NON-NLS-1$
	    writer.write( "namespace Bar = Foo/*vp4*/;	\n" ); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp=importFile( "testNamespace.cpp", source ); //$NON-NLS-1$
	    //vp1 using reference, namespace scope, prefixed
	    ICompletionProposal [] results = getResults( cpp, source.indexOf( "az/*vp1*/" ) ); //$NON-NLS-1$
	    assertEquals( "Baz", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( 1, results.length);
		//vp1b using reference, namespace scope, no prefix
	    results = getResults( cpp, source.indexOf( "Baz/*vp1*/" ) ); //$NON-NLS-1$
	    assertEquals( "Baz", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "Foo", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( 2, results.length);
		//vp2 function scope
	    results = getResults( cpp, source.indexOf( "oo::i/*vp2*/" ) ); //$NON-NLS-1$
	    assertEquals( "Foo", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 1, results.length);
		//vp3 nested namespace, rhs
	    results = getResults( cpp, source.indexOf( "az::i;//vp3" ) ); //$NON-NLS-1$
	    assertEquals( "Baz", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( 1, results.length);
		//vp4 alias definition
	    results = getResults( cpp, source.indexOf( "oo/*vp4*/" ) ); //$NON-NLS-1$
	    assertEquals( "Foo", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( 1, results.length);
	}
	
	public void testNamespaceAlias80612() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write( "namespace Foo{ 				\n" ); //$NON-NLS-1$
		writer.write( "int x; 						\n" ); //$NON-NLS-1$
	    writer.write( "}							\n" ); //$NON-NLS-1$
	    writer.write( "namespace Bar = Foo/*vp4*/;	\n" ); //$NON-NLS-1$
	    writer.write( "Bar::x;						\n" ); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp=importFile( "testNamespaceAlias80612.cpp", source ); //$NON-NLS-1$
	    //vp1 alias reference
	    ICompletionProposal [] results = getResults( cpp, source.indexOf( "ar::x" ) ); //$NON-NLS-1$
	    assertEquals( 1, results.length);
		assertEquals( "Bar", 	results[0].getDisplayString()); //$NON-NLS-1$
		
	}
	//	test 47 
	public void testMacro() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("#define INT32_C(x) x ## L			\n"); //$NON-NLS-1$
	   	writer.write("#define INT32_MAX (2147483647)	\n"); //$NON-NLS-1$
	   	writer.write("int main(int argc, char **argv) {	\n"); //$NON-NLS-1$
	   	writer.write("	INT32_C(2);//vp1				\n"); //$NON-NLS-1$
	   	writer.write("	return (0);						\n"); //$NON-NLS-1$
	   	writer.write("}									\n"); //$NON-NLS-1$
	   	String code = writer.toString();
	    IFile t = importFile( "testMacro.c", code ); //$NON-NLS-1$
	    ICompletionProposal [] results = getResults( t, code.indexOf( "32_C(2);//vp1" ) ); //$NON-NLS-1$
	    assertEquals( "INT32_C(x)", 		results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "INT32_MAX", 			results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( 2, results.length);
	}
	public void testConstructor() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write( "namespace Foo{ 			\n" ); //$NON-NLS-1$
		writer.write( "class Base{				\n" ); //$NON-NLS-1$
		writer.write( "	Base(int x){}			\n" ); //$NON-NLS-1$
		writer.write( "	};						\n" ); //$NON-NLS-1$
		writer.write( "class Derived: Base{	\n" ); //$NON-NLS-1$
		writer.write( "	Derived(): Base/*vp1*/(4){}	\n" ); //$NON-NLS-1$
		writer.write( "	}; 						\n" ); //$NON-NLS-1$
		writer.write( "}						\n" ); //$NON-NLS-1$
		writer.write( "void f(){				\n" ); //$NON-NLS-1$
		writer.write( "	Foo::Derived& x = *(new Foo::Derived/*vp2*/())/;	\n" ); //$NON-NLS-1$
		writer.write( "}; 						\n" ); //$NON-NLS-1$
		String source = writer.toString();
	    IFile cpp=importFile( "testConstructor.cpp", source ); //$NON-NLS-1$
	    //vp1 initializer list
	    ICompletionProposal [] results = getResults( cpp, source.indexOf( "ase/*vp1*/" ) ); //$NON-NLS-1$
	    assertEquals( 1, results.length);
		assertEquals( "Base", 	results[0].getDisplayString()); //$NON-NLS-1$
		results = getResults( cpp, source.indexOf( "erived/*vp2*/" ) ); //$NON-NLS-1$
	    assertEquals( 1, results.length);
		assertEquals( "Derived", 	results[0].getDisplayString()); //$NON-NLS-1$
		
	}
	public void testUnion() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write( "namespace Foo{ 				\n" ); //$NON-NLS-1$
		writer.write( "union uni1{};				\n" ); //$NON-NLS-1$
	    writer.write( "class c1{ 					\n" ); //$NON-NLS-1$
	    writer.write( "  public: union uni2{} s; 	\n" ); //$NON-NLS-1$
	    writer.write( "};				          	\n" ); //$NON-NLS-1$
	    writer.write( "namespace N{   		        \n" ); //$NON-NLS-1$
	    writer.write( " union uni3{};	        	\n" ); //$NON-NLS-1$
	    writer.write( " class c2{ 					\n" ); //$NON-NLS-1$
	    writer.write( "   uni1/*vp1*/ s;			\n" ); //$NON-NLS-1$
	    writer.write( "   uni3 ss;			    	\n" ); //$NON-NLS-1$
	    writer.write( "   c2() {			    	\n" ); //$NON-NLS-1$
	    writer.write( "     c1::uni2/*vp2*/ s;		\n" ); //$NON-NLS-1$
	    writer.write( "     union uni3/*vp3*/ t;	\n" ); //$NON-NLS-1$
	    writer.write( "   }				          	\n" ); //$NON-NLS-1$
	    writer.write( " };				          	\n" ); //$NON-NLS-1$
	    writer.write( "}			                \n" ); //$NON-NLS-1$
		String source = writer.toString();
	    IFile cpp=importFile( "testUnion.cpp", source ); //$NON-NLS-1$
	    //vp1 method scope
	    ICompletionProposal [] results = getResults( cpp, source.indexOf( "1/*vp1*/" ) ); //$NON-NLS-1$
	    assertEquals( 3, results.length);
		assertEquals( "uni1", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "uni3", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "union", 	results[2].getDisplayString()); //$NON-NLS-1$
		//vp2 :: access, in method scope,
		results = getResults( cpp, source.indexOf( "i2/*vp2*/" ) );//$NON-NLS-1$
	    assertEquals( 1, results.length);
	    assertEquals( "uni2", 	results[0].getDisplayString()); //$NON-NLS-1$
		//c style declaration, method scope
		results = getResults( cpp, source.indexOf( "i3/*vp3*/" ) );//$NON-NLS-1$
	    assertEquals( 2, results.length);
	    assertEquals( "uni1", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "uni3", 	results[1].getDisplayString()); //$NON-NLS-1$
		
	}
	
	public void testKeyword() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write( "class/*vp1*/ Foo{ 		\n" ); //$NON-NLS-1$
		writer.write( "void f();			\n" ); //$NON-NLS-1$
	    writer.write( "  public/*vp2*/: Foo(); 	\n" ); //$NON-NLS-1$
	    writer.write( "};				        \n" ); //$NON-NLS-1$
	    writer.write( "void Foo::f() {			\n" ); //$NON-NLS-1$
	    writer.write( "  void/*vp3*/* g; 		\n" ); //$NON-NLS-1$
	    writer.write( "}			          	\n" ); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp=importFile( "testKeyword.cpp", source ); //$NON-NLS-1$
	    //vp1 global
	    ICompletionProposal [] results = getResults( cpp, source.indexOf( "lass/*vp1*/" ) ); //$NON-NLS-1$
	    assertEquals( 3, results.length);
	    assertEquals( "char", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "class", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "const", 	results[2].getDisplayString()); //$NON-NLS-1$
		//vp2 class
		results = getResults( cpp, source.indexOf( "lic/*vp2*/" ) );//$NON-NLS-1$
	    assertEquals( 1, results.length);
		assertEquals( "public", 	results[0].getDisplayString()); //$NON-NLS-1$
		//vp3 method
		results = getResults( cpp, source.indexOf( "d/*vp3*/" ) );//$NON-NLS-1$
	    assertEquals( 1, results.length);
		assertEquals( "void", 	results[0].getDisplayString()); //$NON-NLS-1$
		
	}
	public void testBase() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write( "class Base{ 			\n" ); //$NON-NLS-1$
		writer.write( " Base();				\n" ); //$NON-NLS-1$
	    writer.write( "  protected: int b();\n" ); //$NON-NLS-1$
	    writer.write( "};				    \n" ); //$NON-NLS-1$
	    writer.write( "class Derived: Base{ \n" ); //$NON-NLS-1$
		writer.write( " Derived();			\n" ); //$NON-NLS-1$
	    writer.write( "  void d() {			\n" ); //$NON-NLS-1$
	    writer.write( "  b/*vp1*/{			\n" ); //$NON-NLS-1$
	    writer.write( "};				    \n" ); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp=importFile( "testBase.cpp", source ); //$NON-NLS-1$
	    //vp1 method scope
	    ICompletionProposal [] results = getResults( cpp, source.indexOf( "/*vp1*/" ) ); //$NON-NLS-1$
	    assertEquals( 4, results.length);
		assertEquals( "b() int", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "Base", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "bool", 	results[2].getDisplayString()); //$NON-NLS-1$
		assertEquals( "break", 	results[3].getDisplayString()); //$NON-NLS-1$
		
	}
	public void testThis() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write( "class X{ 								\n" ); //$NON-NLS-1$
		writer.write( " X& X::operator=(const X& rhs){			\n"); //$NON-NLS-1$
	    writer.write( "  if(this/*vp1*/==&rhs)return *this;		\n" ); //$NON-NLS-1$
	    writer.write( "  (*this/*vp2*/).aVar=rhs.aVar;			\n" ); //$NON-NLS-1$
	    writer.write( "	 this/*vp3*/->anotherVar=rhs.anotherVar;\n" ); //$NON-NLS-1$
		writer.write( "  return *this;							\n" ); //$NON-NLS-1$
		writer.write( "  }				    					\n" ); //$NON-NLS-1$
	    writer.write( "	 int aVar, anotherVar; 					\n" ); //$NON-NLS-1$
		writer.write( "};				    					\n" ); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp=importFile( "testThis.cpp", source ); //$NON-NLS-1$
	    //vp1 method scope
	    ICompletionProposal [] results = getResults( cpp, source.indexOf( "s/*vp1*/" ) ); //$NON-NLS-1$
	    assertEquals( 1, results.length);
		assertEquals( "this", 	results[0].getDisplayString()); //$NON-NLS-1$
		results = getResults( cpp, source.indexOf( "s/*vp2*/" ) ); //$NON-NLS-1$
	    assertEquals( 1, results.length);
		assertEquals( "this", 	results[0].getDisplayString()); //$NON-NLS-1$
		results = getResults( cpp, source.indexOf( "s/*vp3*/" ) ); //$NON-NLS-1$
	    assertEquals( 1, results.length);
		assertEquals( "this", 	results[0].getDisplayString()); //$NON-NLS-1$
		
	}
	public void testClassScope() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write( "class Foo{					\n" ); //$NON-NLS-1$
		writer.write( "	public:						\n" ); //$NON-NLS-1$
		writer.write( "		Foo(){}					\n" ); //$NON-NLS-1$
		writer.write( "		int getThatVar();		\n" ); //$NON-NLS-1$
		writer.write( "	private:					\n" ); //$NON-NLS-1$
		writer.write( "		int thatVar;			\n" ); //$NON-NLS-1$
		writer.write( "	};							\n" ); //$NON-NLS-1$
		String header = writer.toString();
	    importFile( "testClassScope.h", header ); //$NON-NLS-1$
	    writer.write( "	#include \"testClassScope.h\"		\n" ); //$NON-NLS-1$
		writer.write( "	int Foo::getThatVar/*vp1*/(){		\n" ); //$NON-NLS-1$
		writer.write( "		return thatVar/*vp2*/; 			\n" ); //$NON-NLS-1$
		writer.write( "	}									\n" ); //$NON-NLS-1$
		String source = writer.toString();
	    IFile cpp=importFile( "testClassScope.cpp", source ); //$NON-NLS-1$
	    //vp1 method scope
	    ICompletionProposal [] results = getResults( cpp, source.indexOf( "Var/*vp1*/" ) ); //$NON-NLS-1$
	    assertEquals( 1, results.length);
		assertEquals( "getThatVar() int", 	results[0].getDisplayString()); //$NON-NLS-1$
		results = getResults( cpp, source.indexOf( "Var/*vp2*/" ) ); //$NON-NLS-1$
	    assertEquals( 1, results.length);
		assertEquals( "thatVar : int", 	results[0].getDisplayString()); //$NON-NLS-1$
			
	}

	public void testClassScope72564() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write( "class X{ 					\n" ); //$NON-NLS-1$
		writer.write( "class ElStatico{				\n" ); //$NON-NLS-1$
		writer.write( "	public:						\n" ); //$NON-NLS-1$
		writer.write( "		ElStatico(){}			\n" ); //$NON-NLS-1$
		writer.write( "		virtual ~ElStatico(){}	\n" ); //$NON-NLS-1$
		writer.write( "	private:					\n" ); //$NON-NLS-1$
		writer.write( "		static int aClsVar;		\n" ); //$NON-NLS-1$
		writer.write( "	};							\n" ); //$NON-NLS-1$
		String header = writer.toString();
	    importFile( "testClassScope72564.h", header ); //$NON-NLS-1$
	    writer.write( "	#include \"testClassScope72564.h\"	\n" ); //$NON-NLS-1$
		writer.write( "	int ElStatico::aClsVar/*vp1*/ = 10;	\n" ); //$NON-NLS-1$
		String source = writer.toString();
	    IFile cpp=importFile( "testClassScope72564.cpp", source ); //$NON-NLS-1$
	    //vp1 method scope
	    ICompletionProposal [] results = getResults( cpp, source.indexOf( "Var/*vp1*/" ) ); //$NON-NLS-1$
	    assertEquals( 1, results.length);
		assertEquals( "aClsVar", 	results[0].getDisplayString()); //$NON-NLS-1$
			
	}
	public void testMultiLevelQualifiers() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write( "class A{ 					\n" ); //$NON-NLS-1$
		writer.write( "public: class B{				\n" ); //$NON-NLS-1$
		writer.write( "public: class C{				\n" ); //$NON-NLS-1$
		writer.write( "public: class D{				\n" ); //$NON-NLS-1$
		writer.write( "public: class E{				\n" ); //$NON-NLS-1$
		writer.write( "public: class F{				\n" ); //$NON-NLS-1$
		writer.write( "public: class G{				\n" ); //$NON-NLS-1$
		writer.write( "public: class H{				\n" ); //$NON-NLS-1$
		writer.write( "public: class I{				\n" ); //$NON-NLS-1$
		writer.write( "public: class J{				\n" ); //$NON-NLS-1$
		writer.write( "public: class K{				\n" ); //$NON-NLS-1$
		writer.write( "public: class L{				\n" ); //$NON-NLS-1$
		writer.write( "public: class M{				\n" ); //$NON-NLS-1$
		writer.write( "}m;}l;}k;}j;}i;}h;}g;}f;}e;}d;}c;}b;};	\n" ); //$NON-NLS-1$
		writer.write( "	A a;									\n" ); //$NON-NLS-1$
		writer.write( "	void f(){								\n" ); //$NON-NLS-1$
		writer.write( "		a.b.c.d.e.f.g.h.i.j.k.l.m/*vp1*/;	\n" ); //$NON-NLS-1$
		writer.write( "	}										\n" ); //$NON-NLS-1$
		String source = writer.toString();
	    IFile cpp=importFile( "testMultiLevelQualifiers.cpp", source ); //$NON-NLS-1$
	    //vp1 13 qualifier depth
	    ICompletionProposal [] results = getResults( cpp, source.indexOf( "m/*vp1*/" ) ); //$NON-NLS-1$
	    assertEquals( 1, results.length);
		assertEquals( "m : M", 	results[0].getDisplayString()); //$NON-NLS-1$
			
	}
	public void testCase() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write( "class Ananas{ 				\n" ); //$NON-NLS-1$
		writer.write( "public: Ananas();			\n" ); //$NON-NLS-1$
		writer.write( "void ananas(int i);			\n" ); //$NON-NLS-1$
		writer.write( "int aNaNaS();				\n" ); //$NON-NLS-1$
		writer.write( "int ANAnas;					\n" ); //$NON-NLS-1$
		writer.write( "};							\n" ); //$NON-NLS-1$
		writer.write( "int ananaS;					\n" ); //$NON-NLS-1$
		writer.write( "void anaNAS(){				\n" ); //$NON-NLS-1$
		writer.write( "Ananas/*vp1*/ a;				\n" ); //$NON-NLS-1$
		writer.write( "a.aNaNaS/*vp2*/();			\n" ); //$NON-NLS-1$
		writer.write( "}							\n" ); //$NON-NLS-1$
		String source = writer.toString();
	    IFile cpp=importFile( "testCase.cpp", source ); //$NON-NLS-1$
	    //vp1 global scope
	    ICompletionProposal [] results = getResults( cpp, source.indexOf( "nas/*vp1*/" ) ); //$NON-NLS-1$
	    assertEquals( 3, results.length);
		assertEquals( "Ananas", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ananaS : int", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "anaNAS() void", 	results[2].getDisplayString()); //$NON-NLS-1$
		//vp2 class scope
		results = getResults( cpp, source.indexOf( "NaS/*vp2*/" ) ); //$NON-NLS-1$
	    assertEquals( 3, results.length);
		assertEquals( "aNaNaS() int", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ANAnas : int", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ananas(int) void", 	results[2].getDisplayString()); //$NON-NLS-1$
	}
	//test 32
	public void testType() throws Exception {
		StringWriter writer = new StringWriter();
	   	writer.write("namespace N {				\n" ); //$NON-NLS-1$
	    writer.write("	class ClassT {};		\n" ); //$NON-NLS-1$
	    writer.write("	struct StructT {};		\n" ); //$NON-NLS-1$
		writer.write("	typedef struct {} TypedefT;		\n" ); //$NON-NLS-1$
		writer.write("	union UnionT {};		\n" ); //$NON-NLS-1$
		writer.write("}							\n" ); //$NON-NLS-1$
	    String codeH = writer.toString();
	    importFile( "testType.h", codeH ); //$NON-NLS-1$
	    writer = new StringWriter();
	    writer.write("#include \"testType.h\"		\n" ); //$NON-NLS-1$
	    writer.write("int main (int argc, char** argv){	\n" ); //$NON-NLS-1$
	    writer.write("	using namespace N;		\n" ); //$NON-NLS-1$
		writer.write("	ClassT/*vp1*/ c;		\n" ); //$NON-NLS-1$
		writer.write("	StructT/*vp2*/ d;    	\n" ); //$NON-NLS-1$
		writer.write("	TypedefT/*vp3*/ e;     	\n" ); //$NON-NLS-1$
		writer.write("	UnionT/*vp4*/ f;		\n" ); //$NON-NLS-1$
		writer.write("	bool/*vp5*/ b;	 		\n" ); //$NON-NLS-1$
		writer.write("}							\n" ); //$NON-NLS-1$
	    String code = writer.toString();
	    IFile f=importFile( "testType.cpp", code ); //$NON-NLS-1$
	    ICompletionProposal [] results = getResults( f, code.indexOf( "T/*vp1*/" ) ); //$NON-NLS-1$
		assertEquals( "ClassT", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( 1, results.length);
		results = getResults( f, code.indexOf( "T/*vp2*/" ) ); //$NON-NLS-1$
		assertEquals( "StructT", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( 1, results.length);
		results = getResults( f, code.indexOf( "T/*vp3*/" ) ); //$NON-NLS-1$
		assertEquals( "TypedefT", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( 1, results.length);
		results = getResults( f, code.indexOf( "T/*vp4*/" ) ); //$NON-NLS-1$
		assertEquals( "UnionT", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( 1, results.length);
		results = getResults( f, code.indexOf( "l/*vp5*/" ) ); //$NON-NLS-1$
		assertEquals( "bool", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( 1, results.length);
	}
	//this doesn't test defect 72403 where try catch fails when a system file is included
	public void testTryCatch() throws Exception {
		StringWriter writer = new StringWriter();
	   	writer.write("class MyException{						\n" ); //$NON-NLS-1$
	   	writer.write("	public:									\n" ); //$NON-NLS-1$
	   	writer.write("	MyException(const char *msg){}			\n" ); //$NON-NLS-1$
	   	writer.write("	const char* getMsg(){return \"error\";}	\n" ); //$NON-NLS-1$
	   	writer.write("};										\n" ); //$NON-NLS-1$
   		writer.write("void someFunction() {						\n" ); //$NON-NLS-1$
		writer.write("	throw MyException(\"someFunction is in trouble\");\n" ); //$NON-NLS-1$
		writer.write("}											\n" ); //$NON-NLS-1$
   		writer.write("void k(){									\n" ); //$NON-NLS-1$
		writer.write("	try {									\n" ); //$NON-NLS-1$
		writer.write("		someFunction/*vp1*/();				\n" ); //$NON-NLS-1$
	   	writer.write("	} catch (MyException &e) {				\n" ); //$NON-NLS-1$
	   	writer.write("		e.getMsg/*vp2*/();					\n" ); //$NON-NLS-1$
	   	writer.write("	}										\n" ); //$NON-NLS-1$
	   	writer.write("}											\n" ); //$NON-NLS-1$
		String code = writer.toString();
	    IFile f=importFile( "testTryCatch.cpp", code ); //$NON-NLS-1$
	    ICompletionProposal [] results = getResults( f, code.indexOf( "Function/*vp1*/" ) ); //$NON-NLS-1$
		assertEquals( "someFunction() void", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( 1, results.length);
		results = getResults( f, code.indexOf( "Msg/*vp2*/" ) ); //$NON-NLS-1$
		assertEquals( "getMsg() const char*", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( 1, results.length);
	}
	public void testArrays() throws Exception {
		StringWriter writer = new StringWriter();
	    writer.write(" 	class A{						\n" ); //$NON-NLS-1$
	   	writer.write("		public: 					\n" ); //$NON-NLS-1$
	   	writer.write("		A(){}						\n" ); //$NON-NLS-1$
		writer.write("		void aa();					\n" ); //$NON-NLS-1$
	   	writer.write("		int aaa;					\n" ); //$NON-NLS-1$
	   	writer.write("		static int bbb;				\n" ); //$NON-NLS-1$
	   	writer.write("	};								\n" ); //$NON-NLS-1$
	   	writer.write("	void fodo(){					\n" ); //$NON-NLS-1$
	   	writer.write("		A a_array[10];				\n" ); //$NON-NLS-1$
	   	writer.write("		A* b_array[10];				\n" ); //$NON-NLS-1$
	   	writer.write("		for (int i=0; i<10;i++){	\n" ); //$NON-NLS-1$
	   	writer.write("			a_array[i].aaa/*vp1*/=3;\n" ); //$NON-NLS-1$
	   	writer.write("			int x=b_array[i]->bbb/*vp2*/;\n" ); //$NON-NLS-1$
	   	writer.write("	}								\n" ); //$NON-NLS-1$
	   	String code = writer.toString();
	    IFile f=importFile( "testArrays.cpp", code ); //$NON-NLS-1$
	    //vp1 dot ref
	    ICompletionProposal [] results = getResults( f, code.indexOf( "a/*vp1*/" ) ); //$NON-NLS-1$
	    assertEquals( "aa() void", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "aaa : int", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( 2, results.length);
		//vp2 arrow ref
		results = getResults( f, code.indexOf( "bbb/*vp2*/" ) ); //$NON-NLS-1$
		assertEquals( "aaa : int", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "bbb : int", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "aa() void", 	results[2].getDisplayString()); //$NON-NLS-1$
		assertEquals( 3, results.length);
	}
	public void testStruct() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("//aggregate struct (no constructor)								\n" ); //$NON-NLS-1$
	   	writer.write("struct myStruct_c{int m;}ss1={1},ss2={2};							\n" ); //$NON-NLS-1$
	   	writer.write("void foo() {														\n" ); //$NON-NLS-1$
	   	writer.write(" 		myStruct_c *ss3;											\n" ); //$NON-NLS-1$
	   	writer.write(" 		ss3->m=3;													\n" ); //$NON-NLS-1$
	   	writer.write(" 		int i=(ss1.m/*vp1*/ > ss3->m/*vp2*/ ? ss1/*vp3*/ : ss2/*vp4*/).m;\n" ); //$NON-NLS-1$	
	   	writer.write("		//struct with constructor									\n" ); //$NON-NLS-1$
	   	writer.write("		sizeof(myStruct_c/*vp8*/);	 								\n" ); //$NON-NLS-1$
		writer.write("		struct myStruct_cpp {										\n" ); //$NON-NLS-1$
	   	writer.write("			myStruct_cpp(int x){z=x;}								\n" ); //$NON-NLS-1$
	   	writer.write("			int z;													\n" ); //$NON-NLS-1$
	   	writer.write("		};															\n" ); //$NON-NLS-1$
	   	writer.write("		myStruct_cpp ss4(4), *ss5;									\n" ); //$NON-NLS-1$
	   	writer.write("		ss5=new myStruct_cpp/*vp9*/(5);									\n" ); //$NON-NLS-1$
	   	writer.write("		3>ss4.z/*vp5*/ ? ss5->z/*vp6*/ : ss4.z/*vp7*/;				\n" ); //$NON-NLS-1$				
	   	writer.write("}																	\n" ); //$NON-NLS-1$
		String code = writer.toString();
	    IFile f=importFile( "testStruct.cpp", code ); //$NON-NLS-1$
	    //vp1 aggregate: first operand of ?:, first operand of >
		ICompletionProposal [] results = getResults( f, code.indexOf( "1.m/*vp1*/" ) ); //$NON-NLS-1$
		assertEquals( "ss3 : myStruct_c*", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss1 : myStruct_c", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss2 : myStruct_c", 	results[2].getDisplayString()); //$NON-NLS-1$
		assertEquals( 3, results.length);
		//vp2 aggregate: first operand of ?:, second operand of >
		results = getResults( f, code.indexOf( "3->m/*vp2*/" ) ); //$NON-NLS-1$
		assertEquals( "ss3 : myStruct_c*", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss1 : myStruct_c", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss2 : myStruct_c", 	results[2].getDisplayString()); //$NON-NLS-1$
		assertEquals( 3, results.length);
		//vp3 aggregate: second operand of ?:
		results = getResults( f, code.indexOf( "1/*vp3*/" ) ); //$NON-NLS-1$
		assertEquals( "ss3 : myStruct_c*", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss1 : myStruct_c", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss2 : myStruct_c", 	results[2].getDisplayString()); //$NON-NLS-1$
		assertEquals( 3, results.length);
		//vp4 aggregate: third operand of ?:
		results = getResults( f, code.indexOf( "2/*vp4*/" ) ); //$NON-NLS-1$
		assertEquals( "ss3 : myStruct_c*", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss1 : myStruct_c", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss2 : myStruct_c", 	results[2].getDisplayString()); //$NON-NLS-1$
		assertEquals( 3, results.length);
		//vp5 first operand of ?:, second operand of >
		results = getResults( f, code.indexOf( "4.z/*vp5*/" ) ); //$NON-NLS-1$
		assertEquals( "ss3 : myStruct_c*", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss4 : myStruct_cpp", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss5 : myStruct_cpp*", 	results[2].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss1 : myStruct_c", 	results[3].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss2 : myStruct_c", 	results[4].getDisplayString()); //$NON-NLS-1$
		assertEquals( 5, results.length);
		//vp6 second operand of ?:
		results = getResults( f, code.indexOf( "5->z/*vp6*/" ) ); //$NON-NLS-1$
		assertEquals( "ss3 : myStruct_c*", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss4 : myStruct_cpp", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss5 : myStruct_cpp*", 	results[2].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss1 : myStruct_c", 	results[3].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss2 : myStruct_c", 	results[4].getDisplayString()); //$NON-NLS-1$
		assertEquals( 5, results.length);
		//vp7 third operand of ?:
		results = getResults( f, code.indexOf( "4.z/*vp7*/" ) ); //$NON-NLS-1$
		assertEquals( "ss3 : myStruct_c*", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss4 : myStruct_cpp", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss5 : myStruct_cpp*", 	results[2].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss1 : myStruct_c", 	results[3].getDisplayString()); //$NON-NLS-1$
		assertEquals( "ss2 : myStruct_c", 	results[4].getDisplayString()); //$NON-NLS-1$
		assertEquals( 5, results.length);
		//vp8 struct type in sizeof 
		results = getResults( f, code.indexOf( "Struct_c/*vp8*/" ) ); //$NON-NLS-1$
		assertEquals( "myStruct_c", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( 1, results.length);
		//vp9 constructor call
		results = getResults( f, code.indexOf( "Struct_cpp/*vp9*/" ) ); //$NON-NLS-1$
		assertEquals( "myStruct_c", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "myStruct_cpp", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( 2, results.length);
	}
	// test 16 template completion
	// template test framework not implemented
	public void testCodeTemplate() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("int main(int argc, char **argv) {	\n" ); //$NON-NLS-1$
	   	writer.write("	int max=10;						\n" ); //$NON-NLS-1$
	   	writer.write("	bool condition=false;			\n" ); //$NON-NLS-1$
	   	writer.write("	int key=1;						\n" ); //$NON-NLS-1$
	   	writer.write("	const int value=1;				\n" ); //$NON-NLS-1$
	   	writer.write("	for/*vp1*/ (int var = 0; var < max; ++var) {\n" ); //$NON-NLS-1$
	   	writer.write("									\n" ); //$NON-NLS-1$
	   	writer.write("	}								\n" ); //$NON-NLS-1$
	   	writer.write("	do/*vp2*/ {						\n" ); //$NON-NLS-1$
	   	writer.write("									\n" ); //$NON-NLS-1$
	   	writer.write("	} while (condition);			\n" ); //$NON-NLS-1$
	   	writer.write("	switch/*vp3*/ (key) {			\n" ); //$NON-NLS-1$
	   	writer.write("	 	case value:					\n" ); //$NON-NLS-1$
	   	writer.write("	 								\n" ); //$NON-NLS-1$
	   	writer.write("	  	break;						\n" ); //$NON-NLS-1$
	   	writer.write("	 	default:					\n" ); //$NON-NLS-1$
	   	writer.write("	  	break;						\n" ); //$NON-NLS-1$
	   	writer.write("	}								\n" ); //$NON-NLS-1$
	   	writer.write("}									\n" ); //$NON-NLS-1$
		String code = writer.toString();
	    IFile t = importFile( "testCodeTemplate.cpp", code ); //$NON-NLS-1$
	    //vp1 for template
	    ICompletionProposal [] results = getResults( t, code.indexOf( "/*vp1*/" ) ); //$NON-NLS-1$
	    assertEquals( 3, results.length);
		assertEquals( "for", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "for - for loop", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "for - for loop with temporary variable", 	results[2].getDisplayString()); //$NON-NLS-1$
	    //vp2 do while loop template
	    assertEquals( 3, results.length);
	    results = getResults( t, code.indexOf( "/*vp2*/" ) ); //$NON-NLS-1$
	    assertEquals( "do", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "double", results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "do - do while statement", 	results[2].getDisplayString()); //$NON-NLS-1$
	    //vp3 switch case statement template
	    assertEquals( 2, results.length);
	    results = getResults( t, code.indexOf( "ch/*vp3*/" ) ); //$NON-NLS-1$
	    assertEquals( "switch", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "switch - switch case statement", 	results[1].getDisplayString()); //$NON-NLS-1$
	    
	}	
}
