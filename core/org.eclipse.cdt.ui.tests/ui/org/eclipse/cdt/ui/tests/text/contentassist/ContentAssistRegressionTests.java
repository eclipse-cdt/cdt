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
package org.eclipse.cdt.ui.tests.text.contentassist;

import java.io.StringWriter;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.tests.BaseTestFramework;
import org.eclipse.cdt.core.tests.FailingTest;
import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProcessor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author aniefer
 */
public class ContentAssistRegressionTests extends BaseTestFramework {

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
       
    protected ICompletionProposal[] getResults( IFile file, int offset ) throws Exception { 
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
		return results;
    }
    
    public static Test suite(){
        return suite( true );
    }
    public static Test suite( boolean cleanup ) {
        TestSuite suite = new TestSuite("ContentAssistRegressionTests"); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("testMemberCompletion") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("test1") ); //$NON-NLS-1$
		suite.addTest( new ContentAssistRegressionTests("test2") ); //$NON-NLS-1$
		suite.addTest( new FailingTest(new ContentAssistRegressionTests("test76398"),76398) ); //$NON-NLS-1$
		suite.addTest( new ContentAssistRegressionTests("test3") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("test7") ); //$NON-NLS-1$
        suite.addTest( new FailingTest(new ContentAssistRegressionTests("test76480"),76480) ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("test27") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("test30") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("test33") ); //$NON-NLS-1$
        suite.addTest( new FailingTest(new ContentAssistRegressionTests("test72723"),72723) ); //$NON-NLS-1$
        suite.addTest( new FailingTest(new ContentAssistRegressionTests("test72541"),72541) ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("test36") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("test44") ); //$NON-NLS-1$
        suite.addTest( new FailingTest(new ContentAssistRegressionTests("test76805"),76805) ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("test47") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("test50") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("test59") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("test62") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("test70") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("test73") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("test79") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("test9") ); //$NON-NLS-1$
        //Test framework doesn't include templates
        //suite.addTest( new ContentAssistRegressionTests("test16") ); //$NON-NLS-1$
        suite.addTest( new ContentAssistRegressionTests("test28") ); //$NON-NLS-1$
        
        if( cleanup )
            suite.addTest( new ContentAssistRegressionTests("cleanupProject") );    //$NON-NLS-1$
        
	    return suite;
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
        IFile t = importFile( "t.cpp", code ); //$NON-NLS-1$
        ICompletionProposal [] results = getResults( t, code.indexOf( "[^]" ) ); //$NON-NLS-1$
        
        assertEquals( 4, results.length);
        assertEquals( "var : int", results[0].getDisplayString()); //$NON-NLS-1$
        assertEquals( "virtual", results[1].getDisplayString()); //$NON-NLS-1$
        assertEquals( "void", results[2].getDisplayString()); //$NON-NLS-1$
        assertEquals( "volatile", results[3].getDisplayString()); //$NON-NLS-1$
    }
    public void removeFile(String filename) throws Exception {
    	IResource [] members = project.members();
    	for( int i = 0; i < members.length; i++ ){
            if( members[i].getName().equals( filename ) ) //$NON-NLS-1$ //$NON-NLS-2$
            members[i].delete( false, monitor );
        }
    }
    //with prefix 'z', inside various scopes
    public void test1() throws Exception {
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
        importFile( "test1.h", codeH ); //$NON-NLS-1$
        
        writer = new StringWriter();
        writer.write("#include \"test1.h\"	\n"); //$NON-NLS-1$
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
        IFile t = importFile( "test1.cpp", code ); //$NON-NLS-1$
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
        results = getResults( t, code.indexOf( "Var0 + zVar;//vp3" ) ); //$NON-NLS-1$
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
    //with prefix 'z', qualified, inside 4 scopes
    public void test2() throws Exception {
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
        importFile( "test2.h", codeH ); //$NON-NLS-1$
        
        writer = new StringWriter();
        writer.write("#include \"test2.h\"			\n"); //$NON-NLS-1$
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
        IFile t = importFile( "test2.cpp", code ); //$NON-NLS-1$
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
        IFile t = importFile( "test2.cpp", code ); //$NON-NLS-1$
        //should not show completions for zField, zMethod since they are
		// ambiguous
        //The defect is the zVar0 and zFunction0 and zClass are showing up when
		// they shouldn't
        ICompletionProposal [] results = getResults( t, code.indexOf( ";//76398" ) ); //$NON-NLS-1$
        assertEquals( 0, results.length);
    }
//  without prefix 'z', qualified, inside scopes
    public void test3() throws Exception {
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
       writer.write(" //c.z;//vpxxx defect 76398	\n"); //$NON-NLS-1$
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
       IFile t = importFile( "test3.cpp", code ); //$NON-NLS-1$
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
    //  different file types
	public void test7() throws Exception {
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
	    IFile t = importFile( "test7.c", code ); //$NON-NLS-1$
	    //vp1 global scope
	    ICompletionProposal [] results = getResults( t, code.indexOf( "Struct a;//vp1" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "zStruct", 	results[0].getDisplayString()); //$NON-NLS-1$
		//vp2 function scope
		results = getResults( t, code.indexOf( "Var=0;//vp2" ) ); //$NON-NLS-1$
		assertEquals( 3, results.length);
		assertEquals( "zVar : int", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "zFunction(int) int", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "zStruct", 	results[2].getDisplayString()); //$NON-NLS-1$
		
        removeFile("test7.c"); //$NON-NLS-1$
		t = importFile( "test7.C", code ); //$NON-NLS-1$
	    //vp1 global scope
	    results = getResults( t, code.indexOf( "Struct a;//vp1" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "zStruct", 	results[0].getDisplayString()); //$NON-NLS-1$
		//vp2 function scope
		results = getResults( t, code.indexOf( "Var=0;//vp2" ) ); //$NON-NLS-1$
		assertEquals( 3, results.length);
		assertEquals( "zVar : int", 	results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals( "zFunction(int) int", 	results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( "zStruct", 	results[2].getDisplayString()); //$NON-NLS-1$
		t = importFile( "test7.cxx", code ); //$NON-NLS-1$
	    //vp1 global scope
	    results = getResults( t, code.indexOf( "Struct a;//vp1" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "zStruct", 	results[0].getDisplayString()); //$NON-NLS-1$
		//vp2 function scope
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
	    IFile t = importFile( "test7.c", code ); //$NON-NLS-1$
	    //vp1 function scope
		ICompletionProposal [] results = getResults( t, code.indexOf( "Var=0;//vp1" ) ); //$NON-NLS-1$
		assertEquals( 0, results.length);
		
	}	
	// test27: Complete on a field type
	// named struct with bitfield & typedef struct
	// missing anonymous struct with class
	public void test27() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer = new StringWriter();
	    writer.write("struct simplestruct {							\n"); //$NON-NLS-1$
	    writer.write(" 	unsigned field1: 2; // range 0-3		\n"); //$NON-NLS-1$
	    writer.write(" 	unsigned int field2: 1; // range 0-1		\n"); //$NON-NLS-1$
	    writer.write("};											\n"); //$NON-NLS-1$
	    writer.write("typedef struct {								\n"); //$NON-NLS-1$
	    writer.write(" 	static int const field2=5; // range 0-3		\n"); //$NON-NLS-1$
	    writer.write("} structtype;									\n"); //$NON-NLS-1$
	    writer.write("int main(int argc, char **argv) {		   		\n"); //$NON-NLS-1$
	    writer.write(" 	struct simplestruct aStruct={3,0}, *pStruct;	\n"); //$NON-NLS-1$
	    writer.write(" 	aStruct.field1;//vp1						\n"); //$NON-NLS-1$
	    writer.write("	pStruct->field1;//vp2						\n"); //$NON-NLS-1$
	    writer.write(" 	structtype anotherStruct;					\n"); //$NON-NLS-1$
	    writer.write("	anotherStruct.field2;//vp3					\n"); //$NON-NLS-1$
	    writer.write("	return (0);									\n"); //$NON-NLS-1$
	    writer.write("} 											\n"); //$NON-NLS-1$
	    String code = writer.toString();
	    IFile t = importFile( "test27.cpp", code ); //$NON-NLS-1$
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
		//vp3 static const field accessed from typedef struct with "."
		results = getResults( t, code.indexOf( "field2;//vp3" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "field2 : const int", 	results[0].getDisplayString()); //$NON-NLS-1$
		
	}
	//	test30: Complete on scoped reference
	//  nested class/namespaces, unambiguous scoped & unscoped namespace, ambiguous namespace
	public void test30() throws Exception {
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
	    importFile( "test30.h", codeH ); //$NON-NLS-1$
		   
	    writer.write("#include \"scope.h\"					\n" ); //$NON-NLS-1$
	    writer.write("int main(int argc, char **argv) {		\n" ); //$NON-NLS-1$
	    writer.write("	NN1::NN2::NN3::C3::C4 c4;//vp1:C4	\n" ); //$NON-NLS-1$
		writer.write("	using namespace NN1::NN2;			\n" ); //$NON-NLS-1$
		writer.write("	NN3::C3 c3; //vp2:NN1,NN3,NNA; vp3:C3,NNA\n" ); //$NON-NLS-1$
		writer.write("	using namespace NN3;				\n" ); //$NON-NLS-1$
		writer.write("	NN3::NNA a;//vp4:NN1,NN3(Ambiguous space)\n" ); //$NON-NLS-1$
		writer.write("}										\n" ); //$NON-NLS-1$
		String code = writer.toString();
	    IFile f=importFile( "test30.cpp", code ); //$NON-NLS-1$
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
	//	test33: Complete on class reference using ., -> and scoped resoution qualifiers
	public void test33() throws Exception {
		StringWriter writer = new StringWriter();
	   	writer.write("class Point{							\n" ); //$NON-NLS-1$
	    writer.write("public:								\n" ); //$NON-NLS-1$
	    writer.write(" Point(): xCoord(0){}					\n" ); //$NON-NLS-1$
	    writer.write(" Point(int x);						\n" ); //$NON-NLS-1$
        writer.write(" Point(const Point &rhs);				\n" ); //$NON-NLS-1$
        writer.write(" virtual ~Point(){}					\n" ); //$NON-NLS-1$
        writer.write(" int getX() const {return xCoord;}	\n" ); //$NON-NLS-1$
        //writer.write(" Point& operator=(const Point &rhs);	\n" ); //$NON-NLS-1$
        writer.write("private:								\n" ); //$NON-NLS-1$
	    writer.write(" int xCoord;							\n" ); //$NON-NLS-1$
        writer.write("};									\n" ); //$NON-NLS-1$
        String codeH = writer.toString();
	    importFile( "test33.h", codeH ); //$NON-NLS-1$
	    
	    writer = new StringWriter();   
	    writer.write("#include \"test33.h\"					\n" ); //$NON-NLS-1$
	    writer.write("Point::Point(int x):xCoord(x){}		\n" ); //$NON-NLS-1$
		writer.write("Point::Point(const Point &rhs){		\n" ); //$NON-NLS-1$
		writer.write("	 xCoord = rhs.xCoord;				\n" ); //$NON-NLS-1$
		writer.write("}										\n" ); //$NON-NLS-1$
		//writer.write("Point& Point::operator=(const Point &rhs){\n" ); //$NON-NLS-1$
		//writer.write("	 if (this == &rhs) return *this;	\n" ); //$NON-NLS-1$
		//writer.write("	 xCoord = rhs.xCoord;				\n" ); //$NON-NLS-1$
		//writer.write("	 return *this;						\n" ); //$NON-NLS-1$
		//writer.write("}										\n" ); //$NON-NLS-1$
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
	    IFile f=importFile( "test33.cpp", code ); //$NON-NLS-1$
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
		    importFile( "test33.h", codeH ); //$NON-NLS-1$
		    
		    writer = new StringWriter();   
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
			writer.write("	 Point one(1);					 		\n" ); //$NON-NLS-1$
			writer.write("	 one.operator=(zero);//vp3: dot ref		\n" ); //$NON-NLS-1$
			writer.write("	 return (0);							\n" ); //$NON-NLS-1$
			writer.write("}											\n" ); //$NON-NLS-1$
			String code = writer.toString();
		    IFile f=importFile( "test72723.cpp", code ); //$NON-NLS-1$
		    //vp1 arrow ref
		    ICompletionProposal [] results = getResults( f, code.indexOf( "perator=(zero);//vp3" ) ); //$NON-NLS-1$
		    assertEquals( 1, results.length);
			assertEquals( "operator=(const Point&) Point&", 	results[0].getDisplayString()); //$NON-NLS-1$
			//vp2 dot ref on dereferenced initialization 
			results = getResults( f, code.indexOf( "perator=(zero);//vp2" ) ); //$NON-NLS-1$
			assertEquals( 1, results.length);
			assertEquals( "operator=(const Point&) Point&", 	results[0].getDisplayString()); //$NON-NLS-1$
			//vp3 dot ref on simple initialization 
		    results = getResults( f, code.indexOf( "perator=(zero);//vp3" ) ); //$NON-NLS-1$
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
	// function reference with ., -> and scope, but can't access functions with . or ->
	// so test only ::
	public void test36() throws Exception {
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
	    IFile t = importFile( "test36.cpp", code ); //$NON-NLS-1$
	    //vp1 namespace alias scope
		ICompletionProposal [] results = getResults( t, code.indexOf( "foo();//vp1" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "foo() void", 	results[0].getDisplayString()); //$NON-NLS-1$
		//vp2 nested namespace scope
		results = getResults( t, code.indexOf( "oo();//vp2" ) ); //$NON-NLS-1$
		assertEquals( 1, results.length);
		assertEquals( "foo() void", 	results[0].getDisplayString()); //$NON-NLS-1$
		
	}	
	//argument types from both function declaration and function call
	public void test44() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("//#include <stdint.h>			\n"); //$NON-NLS-1$
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
	    IFile t = importFile( "test44.c", code ); //$NON-NLS-1$
	    //vp1 function declaration args contain type only
		ICompletionProposal [] results = getResults( t, code.indexOf( "32_t i){//vp1" ) ); //$NON-NLS-1$
		//assertEquals( 15, results.length);
		assertEquals( 2, results.length);
		
		//why is the first just int when manually the first is int16_t, and int is last?
		//looks like the stdint.h didn't get parsed.
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
	   	//writer.write("#include <stdint.h>			\n"); //$NON-NLS-1$
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
	//	macro reference explicit and implicit defined vars & functions
	public void test47() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	//writer.write("#include <stdint.h>			\n"); //$NON-NLS-1$
	   	writer.write("#define INT32_C(x) x ## L		\n"); //$NON-NLS-1$
	   	writer.write("#define INT32_MAX (2147483647)\n"); //$NON-NLS-1$
	   	writer.write("int main(int argc, char **argv) {\n"); //$NON-NLS-1$
	   	writer.write("	INT32_C(2);//vp1\n"); //$NON-NLS-1$
	   	writer.write("	return (0);					\n"); //$NON-NLS-1$
	   	writer.write("}								\n"); //$NON-NLS-1$
	   	String code = writer.toString();
	    IFile t = importFile( "test47.c", code ); //$NON-NLS-1$
	    ICompletionProposal [] results = getResults( t, code.indexOf( "32_C(2);//vp1" ) ); //$NON-NLS-1$
	    assertEquals( "INT32_C(x)", 		results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "INT32_MAX", 			results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals( 2, results.length);
	}
	//	type cast c code: narrowing cast & struct cast; 
	//	cpp code: class cast, override typecheck cast, deref cast
	public void test50() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("typedef int ZINT;		\n"); //$NON-NLS-1$
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
	    IFile t = importFile( "test50.c", code ); //$NON-NLS-1$
	    ICompletionProposal [] results = getResults( t, code.indexOf( "INT) 3.1;//vp1" ) ); //$NON-NLS-1$
	    assertEquals( "ZINT", 		results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 1, results.length);
		results = getResults( t, code.indexOf( "foobar;//vp2" ) ); //$NON-NLS-1$
		assertEquals( "foobar : int", 		results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 1, results.length);
	    removeFile("test50.c");//$NON-NLS-1$
	    
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
  	    t = importFile( "test50.cpp", code ); //$NON-NLS-1$
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
	public void test59() throws Exception {
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
	    IFile t = importFile( "test50.c", code ); //$NON-NLS-1$
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
	public void test62() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("enum day {ztuesday, zthursday};	\n" ); //$NON-NLS-1$
	   	writer.write("enum {zTGIF};					\n" ); //$NON-NLS-1$
	   	writer.write("int i=ztuesday;//vp1 				\n" ); //$NON-NLS-1$
	   	writer.write("class Bar {					\n" ); //$NON-NLS-1$
	  	writer.write(" public:						\n" ); //$NON-NLS-1$
	   	writer.write("	enum A {R1} num;			\n" ); //$NON-NLS-1$
	   	writer.write("	enum {R2};					\n" ); //$NON-NLS-1$
	   	writer.write("	enum {R3} num2;				\n" ); //$NON-NLS-1$
	   	writer.write("	day nd;						\n" ); //$NON-NLS-1$
	   	writer.write("};							\n" ); //$NON-NLS-1$
	   	writer.write("void foo(){					\n" ); //$NON-NLS-1$
	   	writer.write("	day d;//vp2						\n" ); //$NON-NLS-1$
	   	writer.write("	d=ztuesday;//vp3 			\n" ); //$NON-NLS-1$
	   	writer.write("	Bar b;						\n" ); //$NON-NLS-1$
	   	writer.write("	b.num2;//vp4 				\n" ); //$NON-NLS-1$
	   	writer.write("	b.nd=ztuesday;//vp5 			\n" ); //$NON-NLS-1$
	   	writer.write("	Bar::R1;//vp6 				\n" ); //$NON-NLS-1$
	   	writer.write("	b.R3;//vp7 					\n" ); //$NON-NLS-1$
	   	writer.write("}								\n" ); //$NON-NLS-1$
	   	String code = writer.toString();
	    IFile t = importFile( "test62.cpp", code ); //$NON-NLS-1$
	    //vp1 assgnmt to global enumerator: 3 completions
	    ICompletionProposal [] results = getResults( t, code.indexOf( "uesday;//vp1" ) ); //$NON-NLS-1$
	    assertEquals( "zthursday", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "ztuesday", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "zTGIF", 		results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 3, results.length);
	    //vp2 enumeration
	    results = getResults( t, code.indexOf( "y d;//vp2" ) ); //$NON-NLS-1$
	    assertEquals( "day", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 1, results.length);
	    //vp3 global named enum accessed by var
	    results = getResults( t, code.indexOf( "uesday;//vp3" ) ); //$NON-NLS-1$
	    assertEquals( "zthursday", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "ztuesday", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "zTGIF", 		results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 3, results.length);
	    //vp4 3 enumeration members accessed by object and "."
	    results = getResults( t, code.indexOf( "um2;//vp4" ) ); //$NON-NLS-1$
	    assertEquals( "nd : day", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "num : A", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "num2 : ", 		results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 3, results.length);
	    //vp5 enumerator in global named enum accessed by object member var
	    results = getResults( t, code.indexOf( "uesday;//vp5" ) ); //$NON-NLS-1$
	    assertEquals( "zthursday", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "ztuesday", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "zTGIF", 		results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 3, results.length);
	    //vp6 static access ; 4 completions include enumeration & enumerators
	    results = getResults( t, code.indexOf( "R1;//vp6" ) ); //$NON-NLS-1$
	    assertEquals( "R1", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "R2", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "R3", 	results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "A", 		results[3].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 4, results.length);
	    //vp7 3 enumerators can also be accessed through object.
	    results = getResults( t, code.indexOf( "3;//vp7" ) ); //$NON-NLS-1$
	    assertEquals( "R1", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "R2", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "R3", 	results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 3, results.length);
	   
	}
	public void test70() throws Exception {
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
	    IFile t = importFile( "test70.cpp", code ); //$NON-NLS-1$
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
	public void test73() throws Exception {
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
	    IFile t = importFile( "test73.cpp", code ); //$NON-NLS-1$
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
	public void test79() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("class C1{				\n" ); //$NON-NLS-1$
		writer.write("	enum {C1a, C1b};		\n" ); //$NON-NLS-1$
		writer.write("	C1() {				\n" ); //$NON-NLS-1$
		writer.write("		C1;//vp1		\n" ); //$NON-NLS-1$
		writer.write("	}					\n" ); //$NON-NLS-1$
		writer.write("};					\n" ); //$NON-NLS-1$
		String code = writer.toString();
	    IFile t = importFile( "test79.h", code ); //$NON-NLS-1$
	    //vp1 multilevel qual
	    ICompletionProposal [] results = getResults( t, code.indexOf( "1;//vp1" ) ); //$NON-NLS-1$
	    assertEquals( "C1", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C1a", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C1b", 	results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 3, results.length);
	    removeFile("test79.h"); //$NON-NLS-1$
	    
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
	    t = importFile( "test79.H", code ); //$NON-NLS-1$
	    //vp2 H file, namespace scope, class context
	    results = getResults( t, code.indexOf( "C1;//vp2" ) ); //$NON-NLS-1$
	    assertEquals( "C()", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C1", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C3", 	results[2].getDisplayString()); //$NON-NLS-1$
	    //trace showed 4 lookup results
	    assertEquals( 3, results.length);
	    removeFile("test79.H"); //$NON-NLS-1$
	    
	    // vp3 hxx file
	    writer = new StringWriter();
		writer.write("class C{				\n" ); //$NON-NLS-1$
		writer.write("	enum {C1, C3};		\n" ); //$NON-NLS-1$
		writer.write("	C() {				\n" ); //$NON-NLS-1$
		writer.write("		C1;//vp3		\n" ); //$NON-NLS-1$
		writer.write("	}					\n" ); //$NON-NLS-1$
		writer.write("};					\n" ); //$NON-NLS-1$
		code = writer.toString();
		t = importFile( "test79.hxx", code ); //$NON-NLS-1$
	    //vp3 hxx file, method scope
	    results = getResults( t, code.indexOf( "1;//vp3" ) ); //$NON-NLS-1$
	    assertEquals( "C", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C1", results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C3", 	results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 3, results.length);
	    removeFile("test79.hxx"); //$NON-NLS-1$
	    
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
	    t = importFile( "test79.hh", code ); //$NON-NLS-1$
	    //vp4 hh file, namespace scope, class context
	    results = getResults( t, code.indexOf( "C1;//vp4" ) ); //$NON-NLS-1$
	    assertEquals( "C()", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C1", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C3", 	results[2].getDisplayString()); //$NON-NLS-1$
	    //trace showed 4 lookup results
	    assertEquals( 3, results.length);
	    removeFile("test79.hh"); //$NON-NLS-1$
	    
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
	    t = importFile( "test79.H", code ); //$NON-NLS-1$
	    //vp5 hpp file, namespace scope, class context
	    results = getResults( t, code.indexOf( "C1;//vp5" ) ); //$NON-NLS-1$
	    assertEquals( "C()", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C1", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "C3", 	results[2].getDisplayString()); //$NON-NLS-1$
	    //trace showed 4 lookup results
	    assertEquals( 3, results.length);
	    removeFile("test79.hpp"); //$NON-NLS-1$
	  
	}
	public void test9() throws Exception {
	   	String code = "\0"; //$NON-NLS-1$
	    IFile t = importFile( "test9.h", code ); //$NON-NLS-1$
	    //CA on empty doc provides list of keywords.
	    ICompletionProposal [] results = getResults( t, 0 ); //$NON-NLS-1$
	    assertEquals( "asm", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "auto", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "bool", 	results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "wchar_t", results[results.length-1].getDisplayString()); //$NON-NLS-1$
	    StringWriter writer = new StringWriter();
	   	writer.write("s//vp2" ); //$NON-NLS-1$
	    code =writer.toString();
	   	t = importFile( "test9.cpp", code ); //$NON-NLS-1$
	   	results = getResults( t, code.indexOf( "//vp2" ) ); //$NON-NLS-1$
	    assertEquals( "short", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "signed", results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "static", results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "struct", results[3].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 4, results.length);
		      
	}
	// test template completion
	// template test framework not implemented
	public void test16() throws Exception {
	   	StringWriter writer = new StringWriter();
	   	writer.write("int main(int argc, char **argv) {	\n" ); //$NON-NLS-1$
	   	writer.write("	int max=10;						\n" ); //$NON-NLS-1$
	   	writer.write("	bool condition=false;			\n" ); //$NON-NLS-1$
	   	writer.write("	int key=1;						\n" ); //$NON-NLS-1$
	   	writer.write("	const int value=1;				\n" ); //$NON-NLS-1$
	   	writer.write("	for/*vp1*/ (int var = 0; var < max; ++var) {\n" ); //$NON-NLS-1$
	   	writer.write("		\n" ); //$NON-NLS-1$
	   	writer.write("	}								\n" ); //$NON-NLS-1$
	   	writer.write("	do/*vp2*/ {						\n" ); //$NON-NLS-1$
	   	writer.write("	\n" ); //$NON-NLS-1$
	   	writer.write("	} while (condition);			\n" ); //$NON-NLS-1$
	   	writer.write("	switch/*vp3*/ (key) {			\n" ); //$NON-NLS-1$
	   	writer.write("	 	case value:					\n" ); //$NON-NLS-1$
	   	writer.write("	 		\n" ); //$NON-NLS-1$
	   	writer.write("	  	break;						\n" ); //$NON-NLS-1$
	   	writer.write("	 	default:					\n" ); //$NON-NLS-1$
	   	writer.write("	  	break;						\n" ); //$NON-NLS-1$
	   	writer.write("	}\n" ); //$NON-NLS-1$
	   	writer.write("}\n" ); //$NON-NLS-1$
		String code = writer.toString();
	    IFile t = importFile( "test16.cpp", code ); //$NON-NLS-1$
	    //vp1 for template
	    ICompletionProposal [] results = getResults( t, code.indexOf( "/*vp1*/" ) ); //$NON-NLS-1$
	    assertEquals( "for", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "for - for loop", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "for - for loop with temporary variable", 	results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 3, results.length);
		//vp2 do while loop template
	    results = getResults( t, code.indexOf( "/*vp2*/" ) ); //$NON-NLS-1$
	    assertEquals( "do", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "double", results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "do - do while statement", 	results[2].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 3, results.length);
	    //vp3 switch case statement template
	    results = getResults( t, code.indexOf( "ch/*vp3*/" ) ); //$NON-NLS-1$
	    assertEquals( "switch", 	results[0].getDisplayString()); //$NON-NLS-1$
	    assertEquals( "switch - switch case statement", 	results[1].getDisplayString()); //$NON-NLS-1$
	    assertEquals( 2, results.length);
	    
	}
	//  variable type class, struct, typedef struct, union, bool
	public void test28() throws Exception {
		StringWriter writer = new StringWriter();
	   	writer.write("namespace N {				\n" ); //$NON-NLS-1$
	    writer.write("	class ClassT {};		\n" ); //$NON-NLS-1$
	    writer.write("	struct StructT {};		\n" ); //$NON-NLS-1$
		writer.write("	typedef struct {} TypedefT;		\n" ); //$NON-NLS-1$
		writer.write("	union UnionT {};		\n" ); //$NON-NLS-1$
		writer.write("}							\n" ); //$NON-NLS-1$
	    String codeH = writer.toString();
	    importFile( "test28.h", codeH ); //$NON-NLS-1$
	    writer = new StringWriter();
	    writer.write("#include \"test28.h\"		\n" ); //$NON-NLS-1$
	    writer.write("int main (int argc, char** argv){	\n" ); //$NON-NLS-1$
	    writer.write("	using namespace N;		\n" ); //$NON-NLS-1$
		writer.write("	ClassT/*vp1*/ c;		\n" ); //$NON-NLS-1$
		writer.write("	StructT/*vp2*/ d;    	\n" ); //$NON-NLS-1$
		writer.write("	TypedefT/*vp3*/ e;     	\n" ); //$NON-NLS-1$
		writer.write("	UnionT/*vp4*/ f;		\n" ); //$NON-NLS-1$
		writer.write("	bool/*vp5*/ b;	 		\n" ); //$NON-NLS-1$
		writer.write("}							\n" ); //$NON-NLS-1$
	    String code = writer.toString();
	    IFile f=importFile( "test28.cpp", code ); //$NON-NLS-1$
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

}
