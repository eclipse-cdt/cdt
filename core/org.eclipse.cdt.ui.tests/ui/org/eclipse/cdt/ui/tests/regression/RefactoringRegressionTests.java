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
 * Created on Nov 10, 2004
 */
package org.eclipse.cdt.ui.tests.regression;

import java.io.StringWriter;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.tests.FailingTest;
import org.eclipse.cdt.core.tests.SearchRegressionTests;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.internal.corext.refactoring.RenameRefactoring;
import org.eclipse.cdt.internal.corext.refactoring.base.IChange;
import org.eclipse.cdt.internal.corext.refactoring.base.ICompositeChange;
import org.eclipse.cdt.internal.corext.refactoring.base.RefactoringStatus;
import org.eclipse.cdt.internal.corext.refactoring.base.RefactoringStatusEntry;
import org.eclipse.cdt.internal.corext.refactoring.changes.TranslationUnitChange;
import org.eclipse.cdt.internal.corext.refactoring.rename.RenameElementProcessor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import java.util.List;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * @author aniefer
 */
public class RefactoringRegressionTests extends SearchRegressionTests {

    public RefactoringRegressionTests()
    {
        super();
    }
    public RefactoringRegressionTests(String name)
    {
        super(name);
    }    
    
    /**
     * 
     * @param element	The CElement to rename
     * @param newName	The new name for the element
     * @return
     * @throws Exception
     */
    public IChange getRefactorChanges( ISourceReference element, String newName ) throws Exception {
        RenameRefactoring fRefactoring = new RenameRefactoring(element);
        RenameElementProcessor processor = (RenameElementProcessor) fRefactoring.getProcessor();
        processor.setNewElementName( newName );
        
        RefactoringStatus rs =fRefactoring.checkInput( new NullProgressMonitor() );
        if (!rs.hasError()){
	        IChange change = fRefactoring.createChange( new NullProgressMonitor() );
	        return change;
        } 
    
    	fail ("Input check on "+ newName + " failed. "+rs.getFirstMessage(RefactoringStatus.ERROR) ); //$NON-NLS-1$ //$NON-NLS-2$
    	//rs.getFirstMessage(RefactoringStatus.ERROR) is not the message displayed in 
    	//the UI for renaming a method to a constructor, the first message which is only
    	//a warning is shown in the UI. If you click preview, then the error and the warning
    	//is shown. 
    	return null;
    }
    public String[] getRefactorMessages( ISourceReference element, String newName ) throws Exception {
        String[] result;
    	RenameRefactoring fRefactoring = new RenameRefactoring(element);
        RenameElementProcessor processor = (RenameElementProcessor) fRefactoring.getProcessor();
        processor.setNewElementName( newName );
        
        RefactoringStatus rs =fRefactoring.checkInput( new NullProgressMonitor() );
        if (!rs.hasWarning()){
        	fail ("Input check on "+ newName + " passed. There should have been warnings or errors. ") ; //$NON-NLS-1$ //$NON-NLS-2$
        	return null;
        }
        List rse = rs.getEntries();
        result = new String[rse.size()];
        for (int i=0; i< rse.size(); i++){
        	RefactoringStatusEntry entry = (RefactoringStatusEntry) rse.get(i);
        	result[i]=entry.getMessage();

        } 
        return result;
    }
    public int getRefactorSeverity( ISourceReference element, String newName ) throws Exception {
        RenameRefactoring fRefactoring = new RenameRefactoring(element);
        RenameElementProcessor processor = (RenameElementProcessor) fRefactoring.getProcessor();
        processor.setNewElementName( newName );
        
        RefactoringStatus rs =fRefactoring.checkInput( new NullProgressMonitor() );
        
        return (rs.getSeverity());
    }
    protected ISourceReference findElementAtOffset( IFile file, int offset ) throws CModelException{
        CModelManager manager = CModelManager.getDefault();
        TranslationUnit tu = (TranslationUnit) manager.create( file, cproject );
        ICElement el = tu.getElementAtOffset( offset );
        return (ISourceReference) ( ( el instanceof ISourceReference ) ? el : null );
    }
    
    public static Test suite(){
        return suite( true );
    }
    public static Test suite( boolean cleanup ) {
        TestSuite suite = new TestSuite("RefactoringRegressionTests"); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testSimpleRename") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testClass_1") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testAttribute_2") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testMethod_3") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testConstructor_26") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testConstructor_27") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testDestructor_28") ); //$NON-NLS-1$
        suite.addTest( new FailingTest( new RefactoringRegressionTests("testDestructor_29_72612"), 72612) ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testFunction_31") ); //$NON-NLS-1$
        suite.addTest( new FailingTest( new RefactoringRegressionTests("testMethod_32_72717"),72717) ); //$NON-NLS-1$
        suite.addTest( new FailingTest( new RefactoringRegressionTests("testMethod_33_72605"),72605) ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testMethod_34") ); //$NON-NLS-1$
        suite.addTest( new FailingTest( new RefactoringRegressionTests("testMethod_35_72726"),72726) ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testMethod_39") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testMethod_40") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testMethod_41") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testMethod_43") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testMethod_44") ); //$NON-NLS-1$
        suite.addTest( new FailingTest( new RefactoringRegressionTests("testMethod_45_72723"), 72723) ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testStruct_46") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testUnion_47") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testEnumeration_48") ); //$NON-NLS-1$
        suite.addTest( new FailingTest( new RefactoringRegressionTests("testTemplate_49_72626"), 72626) ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testClass_52") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testClass_53") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testAttribute_54") ); //$NON-NLS-1$
        suite.addTest( new FailingTest( new RefactoringRegressionTests("testClass_55_79231"), 79231) ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testClass_55") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testClass_55_72748")); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testClass_56") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testClass_60") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testAttribute_61") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testEnumerator_62") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testAttribute_63") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testAttribute_64") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testAttribute_65") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testNamespace_66") ); //$NON-NLS-1$
        suite.addTest( new FailingTest( new RefactoringRegressionTests("testNamespace_66_79281"), 79281) ); //$NON-NLS-1$
        suite.addTest( new FailingTest( new RefactoringRegressionTests("testNamespace_66_79282"), 79282) ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testFunction_67") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testVariable_68") ); //$NON-NLS-1$
        suite.addTest( new FailingTest( new RefactoringRegressionTests("testVariable_68_79295"), 79295) ); //$NON-NLS-1$
        suite.addTest( new FailingTest( new RefactoringRegressionTests("testClass_81_72620"),72620) ); //$NON-NLS-1$
        suite.addTest( new FailingTest( new RefactoringRegressionTests("testVariable_88_72617"), 72617) ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testClass_92A") ); //$NON-NLS-1$
        suite.addTest( new RefactoringRegressionTests("testClass_92B") ); //$NON-NLS-1$
        
        if( cleanup )
            suite.addTest( new RefactoringRegressionTests("cleanupProject") );    //$NON-NLS-1$
        
	    return suite;
    }

    protected void assertTotalChanges( int numChanges, IChange changes ) throws Exception {
    	int count = 0;
    	if( changes != null )
    		count = countChanges( changes );
        assertEquals( numChanges, count );
    }
    
    private int countChanges( IChange change ){
        int count = 0;
        if( change instanceof ICompositeChange ){
            IChange [] children = ((ICompositeChange) change).getChildren();
            for( int i = 0; i < children.length; i++ ){
                count += countChanges( children[i] );
            }
        } else if( change instanceof TranslationUnitChange ){
            count += countEdits( ((TranslationUnitChange) change).getEdit() );
        }
        return count;
    }
    
    private int countEdits( TextEdit edit ){
        if( edit instanceof MultiTextEdit ){
            return ((MultiTextEdit) edit).getChildrenSize();
        } 
        return 1;
    }
    
    /**
     * 
     * @param changes		The IChange returned from getRefactorChanges
     * @param file			The file we expect to see the change in
     * @param startOffset	The offset of the name that will be changed
     * @param numChars		The length of the original name that is changing
     * @param newText       The new text that the element will be renamed to
     * @throws Exception
     */
    protected void assertChange( IChange changes, IFile file, int startOffset, int numChars, String newText ) throws Exception {
        boolean found = false;
        if( changes != null && changes instanceof ICompositeChange ){
            found = checkCompositeChange( (ICompositeChange) changes, file, startOffset, numChars, newText );
        }
        
        if( !found ) {
        	fail ("Rename at offset " + startOffset + " in \"" + file.getLocation() + "\" not found.");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            assertFalse( true );
        }
    }
    private boolean checkCompositeChange( ICompositeChange composite,  IFile file, int startOffset, int numChars, String newText ){
        boolean found = false;
        IChange [] children = composite.getChildren();
        for( int i = 0; i < children.length; i++ ){
            if( children[i] instanceof ICompositeChange )
                found = checkCompositeChange( (ICompositeChange) children[i], file, startOffset, numChars, newText );
            else if( children[i] instanceof TranslationUnitChange ){
                TranslationUnitChange tuChange = (TranslationUnitChange) children[i];
                if( tuChange.getFile().toString().equals( file.toString() ) ){
                    found = checkTranslationUnitChange( tuChange, startOffset, numChars, newText );
                }
            }
            if( found )
                return found;
        }
        return found;
    }
    
    private boolean checkTranslationUnitChange( TranslationUnitChange change, int startOffset, int numChars, String newText ){
        TextEdit textEdit = change.getEdit();
        if( textEdit instanceof MultiTextEdit ){
            MultiTextEdit multiEdit = (MultiTextEdit) textEdit;
            TextEdit [] edits = multiEdit.getChildren();
            for( int i = 0; i < edits.length; i++ ){
                if( edits[i] instanceof ReplaceEdit && checkReplaceEdit( (ReplaceEdit) edits[i], startOffset, numChars, newText ) )
                    return true;
            }
        } else if( textEdit instanceof ReplaceEdit ){
            return checkReplaceEdit( (ReplaceEdit) textEdit, startOffset, numChars, newText );
        }
        return false;
    }
    
    private boolean checkReplaceEdit( ReplaceEdit edit, int startOffset, int numChars, String newText ){
        return ( edit.getOffset() == startOffset && edit.getLength() == numChars && edit.getText().equals( newText ) );
    }
    
    public void testSimpleRename() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "int boo;            \n" ); //$NON-NLS-1$
        writer.write( "void f() {          \n" ); //$NON-NLS-1$
        writer.write( "   boo++;           \n" ); //$NON-NLS-1$
        writer.write( "}                   \n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "boo" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        
        assertTotalChanges( 2, changes );
        assertChange( changes, file, contents.indexOf("boo"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("boo++"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
    }
    public void testClass_1() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Boo/*vp1*/{}; \n" ); //$NON-NLS-1$
        writer.write( "void f() {          \n" ); //$NON-NLS-1$
        writer.write( "   Boo a;           \n" ); //$NON-NLS-1$
        writer.write( "}                   \n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "Boo/*vp1*/" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "Ooga" ); //$NON-NLS-1$
        
        assertTotalChanges( 2, changes );
        assertChange( changes, file, contents.indexOf("Boo/*vp1*/"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Boo a"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
    }
    public void testAttribute_2() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Boo{ 		   \n" ); //$NON-NLS-1$
        writer.write( "  int att1;//vp1,res1   \n" ); //$NON-NLS-1$
        writer.write( "};                  \n" ); //$NON-NLS-1$
        writer.write( "void f() {          \n" ); //$NON-NLS-1$
        writer.write( "   Boo a;           \n" ); //$NON-NLS-1$
        writer.write( "   a.att1;//res2     \n" ); //$NON-NLS-1$
        writer.write( "}                   \n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "att1;//vp1" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        
        assertTotalChanges( 2, changes );
        assertChange( changes, file, contents.indexOf("att1;//vp1,res1"), 4, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("att1;//res2"), 4, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
    }
    public void testMethod_3() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Boo{ 		   			\n" ); //$NON-NLS-1$
        writer.write( "  int method1(){}//vp1,res1  \n" ); //$NON-NLS-1$
        writer.write( "};                  			\n" ); //$NON-NLS-1$
        writer.write( "void f() {          			\n" ); //$NON-NLS-1$
        writer.write( "   Boo a;           			\n" ); //$NON-NLS-1$
        writer.write( "   a.method1();//res2    	\n" ); //$NON-NLS-1$
        writer.write( "}                   			\n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "method1(){}//vp1" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        
        assertTotalChanges( 2, changes );
        assertChange( changes, file, contents.indexOf("method1(){}//vp1,res1"), 7, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("method1();//res2"), 7, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
    }
  
   
    public void testConstructor_26() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Boo{ 		   			\n" ); //$NON-NLS-1$
        writer.write( "  Boo(){}//vp1,res1  \n" ); //$NON-NLS-1$
        writer.write( "};                  			\n" ); //$NON-NLS-1$
        writer.write( "void f() {          			\n" ); //$NON-NLS-1$
        writer.write( "   Boo a = new Boo();           			\n" ); //$NON-NLS-1$
        writer.write( "}                   			\n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "Boo(){}" ) ); //$NON-NLS-1$
        try {
        	getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        } catch (AssertionFailedError e) {
        	assertTrue(e.getMessage().startsWith("Input check on ooga failed.")); //$NON-NLS-1$
        	return;
        }
        fail ("An error should have occurred in the input check."); //$NON-NLS-1$
    }
    //The constructor name is accepted, but the refactoring doesn't remove the return
    //type and there is a compile error. Renaming to a constructor should be disabled.
    //However, the UI does display the error in the preview panel. Defect 78769 states
    //the error should be shown on the first page. The parser passes, but the UI could be
    //better.
    public void testConstructor_27() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Boo{ 		   	\n" ); //$NON-NLS-1$
        writer.write( "  int foo(){}//vp1	\n" ); //$NON-NLS-1$
        writer.write( "};                  	\n" ); //$NON-NLS-1$
        writer.write( "void f() {          	\n" ); //$NON-NLS-1$
        writer.write( "   Boo a;           	\n" ); //$NON-NLS-1$
        writer.write( "   a.foo();          \n" ); //$NON-NLS-1$
        writer.write( "}                   	\n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "foo(){}" ) ); //$NON-NLS-1$
        try {
        	getRefactorChanges( element, "Boo" ); //$NON-NLS-1$
        } catch (AssertionFailedError e) {
        	//test passes
        	assertTrue(e.getMessage().startsWith("Input check on Boo failed.")); //$NON-NLS-1$
        	return;
        }
        fail ("An error should have occurred in the input check."); //$NON-NLS-1$
    }
    public void testDestructor_28() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Boo{ 		   	\n" ); //$NON-NLS-1$
        writer.write( "  ~Boo(){}//vp1		\n" ); //$NON-NLS-1$
        writer.write( "};                  	\n" ); //$NON-NLS-1$
        writer.write( "void f() {          	\n" ); //$NON-NLS-1$
        writer.write( "   Boo a ;           \n" ); //$NON-NLS-1$
        writer.write( "   a.~Boo();         \n" ); //$NON-NLS-1$
        writer.write( "}                   	\n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "~Boo(){}" ) ); //$NON-NLS-1$
        try {
        	getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        } catch (AssertionFailedError e) {
        	assertTrue(e.getMessage().startsWith("Input check on ooga failed.")); //$NON-NLS-1$
        	return;
        }
        fail ("An error should have occurred in the input check."); //$NON-NLS-1$
    }
    public void testDestructor_29_72612() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Boo{ 		   	\n" ); //$NON-NLS-1$
        writer.write( "  int foo(){}//vp1	\n" ); //$NON-NLS-1$
        writer.write( "};                  	\n" ); //$NON-NLS-1$
        writer.write( "void f() {          	\n" ); //$NON-NLS-1$
        writer.write( "   Boo a;           	\n" ); //$NON-NLS-1$
        writer.write( "   a.foo();          \n" ); //$NON-NLS-1$
        writer.write( "}                   	\n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "foo(){}" ) ); //$NON-NLS-1$
        try {
        	getRefactorChanges( element, "~Boo" ); //$NON-NLS-1$
        } catch (AssertionFailedError e) {
        	//test passes
        	assertTrue(e.getMessage().startsWith("Input check on Boo failed.")); //$NON-NLS-1$
        	return;
        }
        fail ("An error should have occurred in the input check."); //$NON-NLS-1$
    }
    public void testFunction_31() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "void foo(){} 		    \n" ); //$NON-NLS-1$
        writer.write( "void foo/*vp1*/(int i){}	\n" ); //$NON-NLS-1$
        writer.write( "class Foo{               \n" ); //$NON-NLS-1$
        writer.write( "   int method1(){        \n" ); //$NON-NLS-1$
        writer.write( "    foo(3);              \n" ); //$NON-NLS-1$
        writer.write( "    foo();  			    \n" ); //$NON-NLS-1$
        writer.write( "   }                     \n" ); //$NON-NLS-1$
        writer.write( "};                       \n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "foo/*vp1*/" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        assertTotalChanges( 2, changes );
        assertChange( changes, file, contents.indexOf("foo/*vp1*/"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("foo(3)"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
 
   }
    public void testMethod_32_72717() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Base { 		    	\n" ); //$NON-NLS-1$
        writer.write( " virtual void foo()=0;		\n" ); //$NON-NLS-1$
        writer.write( "};							\n" ); //$NON-NLS-1$
        writer.write( "class Derived: public Base { \n" ); //$NON-NLS-1$
        writer.write( " virtual void foo();			\n" ); //$NON-NLS-1$
        writer.write( " void moon/*vp1*/(int i);   	\n" ); //$NON-NLS-1$
        writer.write( "};				 		    \n" ); //$NON-NLS-1$
         
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "moon/*vp1*/" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "foo" ); //$NON-NLS-1$
        assertTotalChanges( 1, changes );
        assertChange( changes, file, contents.indexOf("moon/*vp1*/"), 4, "foo" );  //$NON-NLS-1$//$NON-NLS-2$
       
   }
    
    public void testMethod_33_72605() throws Exception {
	        StringWriter writer = new StringWriter();
	        writer.write( "class Foo { 		    		\n" ); //$NON-NLS-1$
	        writer.write( " void aMethod/*vp1*/(int x=0);		\n" ); //$NON-NLS-1$
	        writer.write( "};							\n" ); //$NON-NLS-1$
	        writer.write( "void Foo::aMethod(int x){}	\n" ); //$NON-NLS-1$
	        String contents = writer.toString();
	        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
	        ISourceReference element = findElementAtOffset( file, contents.indexOf( "aMethod/*vp1*/" ) ); //$NON-NLS-1$
	        IChange changes = getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
	        assertTotalChanges( 2, changes );
	        assertChange( changes, file, contents.indexOf("aMethod/*vp1*/"), 7, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
	        assertChange( changes, file, contents.indexOf("aMethod(int x)"), 7, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
	        
	}
    public void testMethod_34() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Base{	    		\n" ); //$NON-NLS-1$
        writer.write( "  virtual void v/*vp1*/()=0;		\n" ); //$NON-NLS-1$
        writer.write( "};						\n" ); //$NON-NLS-1$
        writer.write( "class Derived: Base { 	\n" ); //$NON-NLS-1$
        writer.write( "  void v(){};    		\n" ); //$NON-NLS-1$
        writer.write( "};						\n" ); //$NON-NLS-1$
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "v/*vp1*/" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        //dervied method is not renamed; only a warning to rename derived or base class methods
        assertTotalChanges( 1, changes );
        assertChange( changes, file, contents.indexOf("v/*vp1*/"), 1, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        
    }
    // defect is input for new name is not allowed
    public void testMethod_35_72726() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Foo{	    						\n" ); //$NON-NLS-1$
        writer.write( "  Foo& operator *=/*vp1*/(const Foo &rhs);\n" ); //$NON-NLS-1$
        writer.write( "  Foo& operator==/*vp2*/(const Foo &rhs);\n" ); //$NON-NLS-1$
        writer.write( "};										\n" ); //$NON-NLS-1$
        writer.write( "Foo& Foo::operator *=(const Foo &rhs){ 	\n" ); //$NON-NLS-1$
        writer.write( "  return *this;   						\n" ); //$NON-NLS-1$
        writer.write( "};										\n" ); //$NON-NLS-1$
        writer.write( "Foo& Foo::operator==(const Foo &rhs){ 	\n" ); //$NON-NLS-1$
        writer.write( "  return *this;   						\n" ); //$NON-NLS-1$
        writer.write( "};										\n" ); //$NON-NLS-1$
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        //vp1 with space
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "operator *=/*vp1*/" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "operator +=" ); //$NON-NLS-1$
        assertTotalChanges( 2, changes );
        assertChange( changes, file, contents.indexOf("operator *=/*vp1*/"), 11, "operator +=" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("operator *=(const"), 11, "operator +=" );  //$NON-NLS-1$//$NON-NLS-2$
        //vp2 without space
        element = findElementAtOffset( file, contents.indexOf( "operator==/*vp2*/" ) ); //$NON-NLS-1$
        changes = getRefactorChanges( element, "operator=" ); //$NON-NLS-1$
        assertTotalChanges( 2, changes );
        assertChange( changes, file, contents.indexOf("operator==/*vp2*/"), 11, "operator=" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("operator==(const"), 11, "operator=" );  //$NON-NLS-1$//$NON-NLS-2$
       
   }
    public void testMethod_39() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Foo{	    							\n" ); //$NON-NLS-1$
        writer.write( "  const void*   method1/*vp1*/(char*); 		\n" ); //$NON-NLS-1$
        writer.write( "  const int   method2/*vp1*/(int j); 		\n" ); //$NON-NLS-1$
        writer.write( "};											\n" ); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile( "t.h", header ); //$NON-NLS-1$
       
        writer = new StringWriter();
        writer.write( "#include \"t.h\"											\n" ); //$NON-NLS-1$
        writer.write( "const void* Foo::method1(char* x){return ((void*) x;}	\n" ); //$NON-NLS-1$
        writer.write( "const int Foo::method2(int){return 5;}  					\n" ); //$NON-NLS-1$
        writer.write( "void test() {											\n" ); //$NON-NLS-1$
        writer.write( "		Foo d;												\n" ); //$NON-NLS-1$
        writer.write( " 	d.method1(\"hello\");								\n" ); //$NON-NLS-1$
        writer.write( " 	int i =d.method2(3);								\n" ); //$NON-NLS-1$
        writer.write( "}														\n" ); //$NON-NLS-1$
        String source = writer.toString();
        IFile cpp = importFile( "t.cpp", source ); //$NON-NLS-1$
        //vp1 const
        ISourceReference element = findElementAtOffset( h, header.indexOf( "method1/*vp1*/" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "m1" ); //$NON-NLS-1$
        //assertTotalChanges( 3, changes );
        assertChange( changes, h, header.indexOf("method1/*vp1*/"), 7, "m1" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, cpp, source.indexOf("method1(char"), 7, "m1" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, cpp, source.indexOf("method1(\"hello"), 7, "m1" );  //$NON-NLS-1$//$NON-NLS-2$
        //vp2 const in definition with ::
        element = findElementAtOffset( cpp, source.indexOf( "Foo::method2" ) ); //$NON-NLS-1$
        changes = getRefactorChanges( element, "m2" ); //$NON-NLS-1$
        assertTotalChanges( 3, changes );
        assertChange( changes, h, header.indexOf("method2/*vp1*/"), 7, "m2" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, cpp, source.indexOf("method2(int"), 7, "m2" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, cpp, source.indexOf("method2(3"), 7, "m2" );  //$NON-NLS-1$//$NON-NLS-2$
        
   }
    public void testMethod_40() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Foo{	    							\n" ); //$NON-NLS-1$
        writer.write( "  static int method1/*vp1*/(char* x);		\n" ); //$NON-NLS-1$
        writer.write( "  static int method2/*vp2*/(int);		    \n" ); //$NON-NLS-1$
        writer.write( "};											\n" ); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile( "t.h", header ); //$NON-NLS-1$
       
        writer = new StringWriter();
        writer.write( "#include \"t.h\"								\n" ); //$NON-NLS-1$
        writer.write( "static int Foo::method1(char* x){return 5;}  \n" ); //$NON-NLS-1$
        writer.write( "static int Foo::method2(int x){return (2);};	\n" ); //$NON-NLS-1$
        writer.write( "void test() {								\n" ); //$NON-NLS-1$
        writer.write( "		Foo::method1(\"hello\");				\n" ); //$NON-NLS-1$
        writer.write( "		int i =Foo::method2(3);					\n" ); //$NON-NLS-1$
        writer.write( "}											\n" ); //$NON-NLS-1$
        String source = writer.toString();
        IFile cpp = importFile( "t.cpp", source ); //$NON-NLS-1$
        //vp1 static method declaration
        ISourceReference element = findElementAtOffset( h, header.indexOf( "method1/*vp1*/" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "m1" ); //$NON-NLS-1$
        assertTotalChanges( 3, changes );
        assertChange( changes, h, header.indexOf("method1/*vp1*/"), 7, "m1" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, cpp, source.indexOf("method1(char"), 7, "m1" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, cpp, source.indexOf("method1(\"hello"), 7, "m1" );  //$NON-NLS-1$//$NON-NLS-2$
        //vp2 static method definition
        element = findElementAtOffset( cpp, source.indexOf( "Foo::method2" ) ); //$NON-NLS-1$
        changes = getRefactorChanges( element, "m2" ); //$NON-NLS-1$
        assertTotalChanges( 3, changes );
        assertChange( changes, h, header.indexOf("method2/*vp2*/"), 7, "m2" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, cpp, source.indexOf("method2(int x"), 7, "m2" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, cpp, source.indexOf("method2(3"), 7, "m2" );  //$NON-NLS-1$//$NON-NLS-2$
       
   }
   
   public void testMethod_41() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Foo{	    							\n" ); //$NON-NLS-1$
        writer.write( "public: 		    							\n" ); //$NON-NLS-1$
        writer.write( "  volatile int  method1/*vp1*/(int);			\n" ); //$NON-NLS-1$
        writer.write( "private:								 		\n" ); //$NON-NLS-1$
        writer.write( "  int b;										\n" ); //$NON-NLS-1$
        writer.write( "};											\n" ); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile( "t.h", header ); //$NON-NLS-1$
       
        writer = new StringWriter();
        writer.write( "#include \"t.h\"								\n" ); //$NON-NLS-1$
        writer.write( "volatile int Foo::method1(int x){return (2);};				\n" ); //$NON-NLS-1$
        writer.write( "void test() {								\n" ); //$NON-NLS-1$
        writer.write( "  Foo d;								\n" ); //$NON-NLS-1$
        writer.write( "  int i =d.method1(1);						\n" ); //$NON-NLS-1$
        writer.write( "}											\n" ); //$NON-NLS-1$
        String source = writer.toString();
        IFile cpp = importFile( "t.cpp", source ); //$NON-NLS-1$
        //vp1 volatile
        ISourceReference element = findElementAtOffset( h, header.indexOf( "method1/*vp1*/" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "m1" ); //$NON-NLS-1$
        assertTotalChanges( 3, changes );
        assertChange( changes, h, header.indexOf("method1/*vp1*/"), 7, "m1" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, cpp, source.indexOf("method1(int x"), 7, "m1" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, cpp, source.indexOf("method1(1"), 7, "m1" );  //$NON-NLS-1$//$NON-NLS-2$
      
   }
   public void testMethod_43() throws Exception {
    StringWriter writer = new StringWriter();
    writer.write( "class Foo{	    							\n" ); //$NON-NLS-1$
    writer.write( "public: 		    							\n" ); //$NON-NLS-1$
    writer.write( "  inline void  method1/*vp1*/(int i) {b=i;}	\n" ); //$NON-NLS-1$
    writer.write( "private:								 		\n" ); //$NON-NLS-1$
    writer.write( "  int b;										\n" ); //$NON-NLS-1$
    writer.write( "};											\n" ); //$NON-NLS-1$
    String header = writer.toString();
    IFile h = importFile( "t.h", header ); //$NON-NLS-1$
   
    writer = new StringWriter();
    writer.write( "#include \"t.h\"								\n" ); //$NON-NLS-1$
    writer.write( "void test() {								\n" ); //$NON-NLS-1$
    writer.write( "  Foo *d;									\n" ); //$NON-NLS-1$
    writer.write( "  d->method1(1);								\n" ); //$NON-NLS-1$
    writer.write( "}											\n" ); //$NON-NLS-1$
    String source = writer.toString();
    IFile cpp = importFile( "t.cpp", source ); //$NON-NLS-1$
    //vp1 inline
    ISourceReference element = findElementAtOffset( h, header.indexOf( "method1/*vp1*/" ) ); //$NON-NLS-1$
    IChange changes = getRefactorChanges( element, "m1" ); //$NON-NLS-1$
    assertTotalChanges( 2, changes );
    assertChange( changes, h, header.indexOf("method1/*vp1*/"), 7, "m1" );  //$NON-NLS-1$//$NON-NLS-2$
    assertChange( changes, cpp, source.indexOf("method1(1"), 7, "m1" );  //$NON-NLS-1$//$NON-NLS-2$
  
   }
   public void testMethod_44() throws Exception {
    StringWriter writer = new StringWriter();
    writer.write( "class Base{	    		\n" ); //$NON-NLS-1$
    writer.write( "  virtual void v();		\n" ); //$NON-NLS-1$
    writer.write( "  int i; 				\n" ); //$NON-NLS-1$
    writer.write( "};						\n" ); //$NON-NLS-1$
    writer.write( "void Base::v(){}			\n" ); //$NON-NLS-1$
    writer.write( "class Derived: Base { 	\n" ); //$NON-NLS-1$
    writer.write( "  virtual void v/*vp1*/(){}//explicitly virtual    		\n" ); //$NON-NLS-1$
    writer.write( "};						\n" ); //$NON-NLS-1$
    writer.write( "class Derived2: Derived {\n" ); //$NON-NLS-1$
    writer.write( "  void v(){i++;}    		\n" ); //$NON-NLS-1$
    writer.write( "};						\n" ); //$NON-NLS-1$
    String contents = writer.toString();
    IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
    //vp1 implicit virtual method
    ISourceReference element = findElementAtOffset( file, contents.indexOf( "v/*vp1*/" ) ); //$NON-NLS-1$
    String[] messages= getRefactorMessages( element, "v2" ); //$NON-NLS-1$
    assertEquals(1, messages.length);
    assertEquals("Renaming a virtual method. Consider renaming the base and derived class methods (if any).", messages[0] );   //$NON-NLS-1$
       
}
   public void testMethod_45_72732() throws Exception {
	    StringWriter writer = new StringWriter();
	    writer.write( "class Base{	    		\n" ); //$NON-NLS-1$
	    writer.write( "  virtual void v();		\n" ); //$NON-NLS-1$
	    writer.write( "  int i; 				\n" ); //$NON-NLS-1$
	    writer.write( "};						\n" ); //$NON-NLS-1$
	    writer.write( "void Base::v(){}			\n" ); //$NON-NLS-1$
	    writer.write( "class Derived: Base { 	\n" ); //$NON-NLS-1$
	    writer.write( "  void v/*vp1*/(){}//implicitly virtual    		\n" ); //$NON-NLS-1$
	    writer.write( "};						\n" ); //$NON-NLS-1$
	    writer.write( "class Derived2: Derived {\n" ); //$NON-NLS-1$
	    writer.write( "  void v(){i++;}    		\n" ); //$NON-NLS-1$
	    writer.write( "};						\n" ); //$NON-NLS-1$
	    String contents = writer.toString();
	    IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
	    //vp1 implicit virtual method
	    ISourceReference element = findElementAtOffset( file, contents.indexOf( "v/*vp1*/" ) ); //$NON-NLS-1$
	    String[] messages= getRefactorMessages( element, "v2" ); //$NON-NLS-1$
	    assertEquals(1, messages.length);
	    assertEquals("Renaming a virtual method. Consider renaming the base and derived class methods (if any).", messages[0] );   //$NON-NLS-1$
	  
    
	}
	public void testStruct_46() throws Exception {
	    StringWriter writer = new StringWriter();
	    writer.write( "struct st1/*vp1*/{};				\n" ); //$NON-NLS-1$
	    writer.write( "class c1/*vp1*/{ 				\n" ); //$NON-NLS-1$
	    writer.write( "  public: struct st2/*vp2*/{} s; \n" ); //$NON-NLS-1$
	    writer.write( "};				          		\n" ); //$NON-NLS-1$
	    writer.write( "namespace N{   		        	\n" ); //$NON-NLS-1$
	    writer.write( " struct st3/*vp3*/{};	        \n" ); //$NON-NLS-1$
	    writer.write( " class c2/*vp1*/{ 				\n" ); //$NON-NLS-1$
	    writer.write( "   st1 s;			    		\n" ); //$NON-NLS-1$
	    writer.write( "   st3 ss;			    		\n" ); //$NON-NLS-1$
	    writer.write( "   c2() {			    		\n" ); //$NON-NLS-1$
	    writer.write( "     c1::st2 s;			    	\n" ); //$NON-NLS-1$
	    writer.write( "   }				          		\n" ); //$NON-NLS-1$
	    writer.write( " };				          		\n" ); //$NON-NLS-1$
	    writer.write( "}			                	\n" ); //$NON-NLS-1$
		   
	    String contents = writer.toString();
	    IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
	    //vp1 global declaration
	    ISourceReference element = findElementAtOffset( file, contents.indexOf( "st1/*vp1*/" ) ); //$NON-NLS-1$
	    IChange changes = getRefactorChanges( element, "Ooga1" ); //$NON-NLS-1$
	    assertTotalChanges( 2, changes );
	    assertChange( changes, file, contents.indexOf("st1/*vp1*/"), 3, "Ooga1" );  //$NON-NLS-1$//$NON-NLS-2$
	    assertChange( changes, file, contents.indexOf("st1 s"), 3, "Ooga1" );  //$NON-NLS-1$//$NON-NLS-2$
		//vp2 Declared in class
	    element = findElementAtOffset( file, contents.indexOf( "st2/*vp2*/" ) ); //$NON-NLS-1$
	    changes = getRefactorChanges( element, "Ooga2" ); //$NON-NLS-1$
	    assertTotalChanges( 2, changes );
	    assertChange( changes, file, contents.indexOf("st2/*vp2*/"), 3, "Ooga2" );  //$NON-NLS-1$//$NON-NLS-2$
	    assertChange( changes, file, contents.indexOf("st2 s"), 3, "Ooga2" );  //$NON-NLS-1$//$NON-NLS-2$
	    //vp3 Declared in namespace
	    element = findElementAtOffset( file, contents.indexOf( "st3/*vp3*/" ) ); //$NON-NLS-1$
	    changes = getRefactorChanges( element, "Ooga3" ); //$NON-NLS-1$
	    assertTotalChanges( 2, changes );
	    assertChange( changes, file, contents.indexOf("st3/*vp3*/"), 3, "Ooga3" );  //$NON-NLS-1$//$NON-NLS-2$
	    assertChange( changes, file, contents.indexOf("st3 ss"), 3, "Ooga3" );  //$NON-NLS-1$//$NON-NLS-2$
	}
	public void testUnion_47() throws Exception {
	    StringWriter writer = new StringWriter();
	    writer.write( "union st1/*vp1*/{};				\n" ); //$NON-NLS-1$
	    writer.write( "class c1/*vp1*/{ 				\n" ); //$NON-NLS-1$
	    writer.write( "  public: union st2/*vp2*/{} s; 	\n" ); //$NON-NLS-1$
	    writer.write( "};				          		\n" ); //$NON-NLS-1$
	    writer.write( "namespace N{   		        	\n" ); //$NON-NLS-1$
	    writer.write( " union st3/*vp3*/{};	        	\n" ); //$NON-NLS-1$
	    writer.write( " class c2/*vp1*/{ 				\n" ); //$NON-NLS-1$
	    writer.write( "   st1 s;			    		\n" ); //$NON-NLS-1$
	    writer.write( "   st3 ss;			    		\n" ); //$NON-NLS-1$
	    writer.write( "   c2() {			    		\n" ); //$NON-NLS-1$
	    writer.write( "     c1::st2 s;			    	\n" ); //$NON-NLS-1$
	    writer.write( "   }				          		\n" ); //$NON-NLS-1$
	    writer.write( " };				          		\n" ); //$NON-NLS-1$
	    writer.write( "}			                	\n" ); //$NON-NLS-1$
		   
	    String contents = writer.toString();
	    IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
	    //vp1 global declaration
	    ISourceReference element = findElementAtOffset( file, contents.indexOf( "st1/*vp1*/" ) ); //$NON-NLS-1$
	    IChange changes = getRefactorChanges( element, "Ooga1" ); //$NON-NLS-1$
	    assertTotalChanges( 2, changes );
	    assertChange( changes, file, contents.indexOf("st1/*vp1*/"), 3, "Ooga1" );  //$NON-NLS-1$//$NON-NLS-2$
	    assertChange( changes, file, contents.indexOf("st1 s"), 3, "Ooga1" );  //$NON-NLS-1$//$NON-NLS-2$
		//vp2 Declared in class
	    element = findElementAtOffset( file, contents.indexOf( "st2/*vp2*/" ) ); //$NON-NLS-1$
	    changes = getRefactorChanges( element, "Ooga2" ); //$NON-NLS-1$
	    assertTotalChanges( 2, changes );
	    assertChange( changes, file, contents.indexOf("st2/*vp2*/"), 3, "Ooga2" );  //$NON-NLS-1$//$NON-NLS-2$
	    assertChange( changes, file, contents.indexOf("st2 s"), 3, "Ooga2" );  //$NON-NLS-1$//$NON-NLS-2$
	    //vp3 Declared in namespace
	    element = findElementAtOffset( file, contents.indexOf( "st3/*vp3*/" ) ); //$NON-NLS-1$
	    changes = getRefactorChanges( element, "Ooga3" ); //$NON-NLS-1$
	    assertTotalChanges( 2, changes );
	    assertChange( changes, file, contents.indexOf("st3/*vp3*/"), 3, "Ooga3" );  //$NON-NLS-1$//$NON-NLS-2$
	    assertChange( changes, file, contents.indexOf("st3 ss"), 3, "Ooga3" );  //$NON-NLS-1$//$NON-NLS-2$
}
	public void testEnumeration_48() throws Exception {
	    StringWriter writer = new StringWriter();
	    writer.write( "enum e1/*vp1*/{E0};				\n" ); //$NON-NLS-1$
	    writer.write( "class c1	{						\n" ); //$NON-NLS-1$
	    writer.write( "  public: enum e2/*vp2*/{E1} s; 	\n" ); //$NON-NLS-1$
	    writer.write( "};				          		\n" ); //$NON-NLS-1$
	    writer.write( "namespace N{   		        	\n" ); //$NON-NLS-1$
	    writer.write( " enum e3/*vp3*/{};	   			\n" ); //$NON-NLS-1$
	    writer.write( " class c2/*vp1*/{ 				\n" ); //$NON-NLS-1$
	    writer.write( "   e1 s;			    			\n" ); //$NON-NLS-1$
	    writer.write( "   e3 ss;			    		\n" ); //$NON-NLS-1$
	    writer.write( "   c2() {			    		\n" ); //$NON-NLS-1$
	    writer.write( "     c1::e2 s;			    	\n" ); //$NON-NLS-1$
	    writer.write( "   }				          		\n" ); //$NON-NLS-1$
	    writer.write( " };				          		\n" ); //$NON-NLS-1$
	    writer.write( "}			                	\n" ); //$NON-NLS-1$
		   
	    String contents = writer.toString();
	    IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
	    //vp1 global declaration
	    ISourceReference element = findElementAtOffset( file, contents.indexOf( "e1/*vp1*/" ) ); //$NON-NLS-1$
	    IChange changes = getRefactorChanges( element, "Ooga1" ); //$NON-NLS-1$
	    assertTotalChanges( 2, changes );
	    assertChange( changes, file, contents.indexOf("e1/*vp1*/"), 2, "Ooga1" );  //$NON-NLS-1$//$NON-NLS-2$
	    assertChange( changes, file, contents.indexOf("e1 s"), 2, "Ooga1" );  //$NON-NLS-1$//$NON-NLS-2$
		//vp2 Declared in class
	    element = findElementAtOffset( file, contents.indexOf( "e2/*vp2*/" ) ); //$NON-NLS-1$
	    changes = getRefactorChanges( element, "Ooga2" ); //$NON-NLS-1$
	    assertTotalChanges( 2, changes );
	    assertChange( changes, file, contents.indexOf("e2/*vp2*/"), 2, "Ooga2" );  //$NON-NLS-1$//$NON-NLS-2$
	    assertChange( changes, file, contents.indexOf("e2 s"), 2, "Ooga2" );  //$NON-NLS-1$//$NON-NLS-2$
	    //vp3 Declared in namespace
	    element = findElementAtOffset( file, contents.indexOf( "e3/*vp3*/" ) ); //$NON-NLS-1$
	    changes = getRefactorChanges( element, "Ooga3" ); //$NON-NLS-1$
	    assertTotalChanges( 2, changes );
	    assertChange( changes, file, contents.indexOf("e3/*vp3*/"), 2, "Ooga3" );  //$NON-NLS-1$//$NON-NLS-2$
	    assertChange( changes, file, contents.indexOf("e3 ss"), 2, "Ooga3" );  //$NON-NLS-1$//$NON-NLS-2$
	}
	public void testTemplate_49_72626() throws Exception {
	    StringWriter writer = new StringWriter();
	    writer.write( "template <class Type>			\n" ); //$NON-NLS-1$
	    writer.write( "class Array/*vp1*/	{			\n" ); //$NON-NLS-1$
	    writer.write( "  public:	Array(){ 			\n" ); //$NON-NLS-1$
	    writer.write( "   a=new Type[10];          		\n" ); //$NON-NLS-1$
	    writer.write( "  }				          		\n" ); //$NON-NLS-1$
	    writer.write( "  virtual Type& operator[](int i){return a[i];}  \n" ); //$NON-NLS-1$
	    writer.write( "  protected:	Type *a;	   		\n" ); //$NON-NLS-1$
	    writer.write( "};				          		\n" ); //$NON-NLS-1$
	    writer.write( "void f(){ 						\n" ); //$NON-NLS-1$
	    writer.write( "   Array<int> a;			    	\n" ); //$NON-NLS-1$
	    writer.write( "}			                	\n" ); //$NON-NLS-1$
		   
	    String contents = writer.toString();
	    IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
	    //vp1 template classes are not supposed to be found until the new Parser is complete (May '05)
	    ISourceReference element = findElementAtOffset( file, contents.indexOf( "Array" ) ); //$NON-NLS-1$
	    String[] messages= getRefactorMessages( element, "Arr2" ); //$NON-NLS-1$
	    assertEquals(1, messages.length);
	    assertEquals("Renaming a template class. Parsing templates are not fully functional. Check the results if you continue.", messages[0] );   //$NON-NLS-1$
	}
	 public void testClass_52() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "namespace N1 { 			\n" ); //$NON-NLS-1$
        writer.write( "class Boo{};        		\n" ); //$NON-NLS-1$
        writer.write( "}                   		\n" ); //$NON-NLS-1$
        writer.write( "namespace N2  {			\n" ); //$NON-NLS-1$
        writer.write( "class Boo/*vp1*/{};      \n" ); //$NON-NLS-1$
        writer.write( "}                   		\n" ); //$NON-NLS-1$
        writer.write( "void f() {          		\n" ); //$NON-NLS-1$
        writer.write( "   N1::Boo c1;           \n" ); //$NON-NLS-1$
        writer.write( "   N2::Boo c2;           \n" ); //$NON-NLS-1$
        writer.write( "}                   		\n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "Boo/*vp1*/" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "Ooga" ); //$NON-NLS-1$
        
        assertTotalChanges( 2, changes );
        assertChange( changes, file, contents.indexOf("Boo/*vp1*/"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Boo c2"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
    }
	 public void testClass_53() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Foo/*vp1*/ {//ren1		\n" ); //$NON-NLS-1$
        writer.write( "  Foo();//ren2        		\n" ); //$NON-NLS-1$
        writer.write( "  virtual ~Foo();//ren3      \n" ); //$NON-NLS-1$
        writer.write( "};                   		\n" ); //$NON-NLS-1$
        writer.write( "Foo::Foo() {}//ren4,5		\n" ); //$NON-NLS-1$
        writer.write( "Foo::~Foo() {}//ren6,7		\n" ); //$NON-NLS-1$
        writer.write( "void f() {          			\n" ); //$NON-NLS-1$
        writer.write( "   Foo *f=new Foo();//ren8,9 \n" ); //$NON-NLS-1$
        writer.write( "   f->~Foo();//ren10         \n" ); //$NON-NLS-1$
        writer.write( "}                   			\n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "Foo/*vp1*/" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "Ooga" ); //$NON-NLS-1$
        
        assertTotalChanges( 10, changes );
        assertChange( changes, file, contents.indexOf("Foo/*vp1*/"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Foo();//ren2"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Foo();//ren3"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Foo::Foo() {}//ren4,5"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Foo() {}//ren4,5"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Foo::~Foo() {}//ren6,7"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Foo() {}//ren6,7"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Foo *f=new Foo();//ren8,9"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Foo();//ren8,9"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Foo();//ren10"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
     }
	 public void testAttribute_54() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Boo{ 		   			\n" ); //$NON-NLS-1$
        writer.write( "  static int att;//vp1,rn1  	\n" ); //$NON-NLS-1$
        writer.write( "};                  			\n" ); //$NON-NLS-1$
        writer.write( "void f() {          			\n" ); //$NON-NLS-1$
        writer.write( "   Boo a;           			\n" ); //$NON-NLS-1$
        writer.write( "   a.att;//rn2     			\n" ); //$NON-NLS-1$
        writer.write( "   Boo::att;//rn3     		\n" ); //$NON-NLS-1$
        writer.write( "}                   			\n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "att;//vp1" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        
        assertTotalChanges( 3, changes );
        assertChange( changes, file, contents.indexOf("att;//vp1"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("att;//rn2"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("att;//rn3"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
     }
	 public void testClass_55() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Foo{		   	\n" ); //$NON-NLS-1$
        writer.write( "  class Hoo{//vp1    \n" ); //$NON-NLS-1$
        writer.write( "     public: Hoo();  \n" ); //$NON-NLS-1$
        writer.write( "  };                 \n" ); //$NON-NLS-1$
        writer.write( "  Foo(){             \n" ); //$NON-NLS-1$
        writer.write( "	 	Foo::Hoo h;     \n" ); //$NON-NLS-1$
        writer.write( "  }           		\n" ); //$NON-NLS-1$
        writer.write( "};    				\n" ); //$NON-NLS-1$
        writer.write( "Foo::Hoo::Hoo(){}    \n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "Hoo{" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        
        assertTotalChanges( 5, changes );
        assertChange( changes, file, contents.indexOf("Hoo{//vp1"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Hoo();"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Hoo h;"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Hoo::Hoo(){}"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Hoo(){}"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
    }
	 public void testClass_55_79231() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Boo{};//vp1		   	\n" ); //$NON-NLS-1$
        writer.write( "class Foo{          		\n" ); //$NON-NLS-1$
        writer.write( "   Foo() {          		\n" ); //$NON-NLS-1$
        writer.write( "     class Boo{};   		\n" ); //$NON-NLS-1$
        writer.write( "     Boo t;    			\n" ); //$NON-NLS-1$
        writer.write( "};                  		\n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        //defect is that the inner class defined in a method is also renamed, when it 
        // shouldn't be.
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "Boo{};//vp1" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "Ooga" ); //$NON-NLS-1$
        
        assertTotalChanges( 1, changes );
        assertChange( changes, file, contents.indexOf("Boo{};//vp1"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
     }
	 
	 public void testClass_55_72748() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Foo{};//vp1		   	\n" ); //$NON-NLS-1$
        writer.write( "void f(){          		\n" ); //$NON-NLS-1$
        writer.write( "  Foo *somePtr;          		\n" ); //$NON-NLS-1$
        writer.write( "  if (somePtr == reinterpret_cast<Foo*>(0)){}         		\n" ); //$NON-NLS-1$
        writer.write( "}                  		\n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        //defect is that the Foo in <> is not renamed 
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "Foo{};//vp1" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "Ooga" ); //$NON-NLS-1$
        
        assertTotalChanges( 3, changes );
        assertChange( changes, file, contents.indexOf("Foo{};//vp1"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Foo *somePtr"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Foo*>(0)"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
      }
	 public void testClass_56() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Foo{};//vp1,rn1		   	\n" ); //$NON-NLS-1$
        writer.write( "class Derived: public Foo{//rn2  \n" ); //$NON-NLS-1$
        writer.write( "  Derived():Foo(){}//rn3         \n" ); //$NON-NLS-1$
        writer.write( "};                  				\n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        //defect is that the inner class defined in a method is also renamed, when it 
        // shouldn't be.
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "Foo{};//vp1" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "Ooga" ); //$NON-NLS-1$
        
        assertTotalChanges( 3, changes );
        assertChange( changes, file, contents.indexOf("Foo{};//vp1"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Foo{//rn2"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Foo(){}//rn3"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
    }
	 public void testClass_60() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Foo{public: Foo();};//vp1		\n" ); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile( "Foo.h", header ); //$NON-NLS-1$
        writer = new StringWriter();
        writer.write( "#include \"Foo.h\"		   			\n" ); //$NON-NLS-1$
        writer.write( "Foo::Foo{};		   				    \n" ); //$NON-NLS-1$
          
        String source = writer.toString();
        importFile( "Foo.cpp", source ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( h, header.indexOf( "Foo{" ) ); //$NON-NLS-1$
        getRefactorChanges( element, "Ooga" ); //$NON-NLS-1$
        IResource [] members = project.members();
        boolean foundh=false, foundcpp=false;
        // make sure file names haven't changed
        for( int i = 0; i < members.length; i++ ){
            if( members[i].getName().equals( "Foo.h" ) ) //$NON-NLS-1$ 
                foundh=true;
            if( members[i].getName().equals( "Foo.cpp" ) ) //$NON-NLS-1$
                foundcpp=true;
         } 
        if (!foundh || !foundcpp){
        	fail ("At least one of Foo.h and Foo.cpp was incorrectly renamed."); //$NON-NLS-1$
        }	
	 }
	 public void testAttribute_61() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Foo{		\n" ); //$NON-NLS-1$
        writer.write( " private: static int count;//vp1		\n" ); //$NON-NLS-1$
        writer.write( "};		\n" ); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile( "Foo.h", header ); //$NON-NLS-1$
        writer = new StringWriter();
        writer.write( "#include \"Foo.h\"		   			\n" ); //$NON-NLS-1$
        writer.write( "int Foo::count=10;		   				    \n" ); //$NON-NLS-1$
          
        String source = writer.toString();
        IFile cpp = importFile( "Foo.cpp", source ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( h, header.indexOf( "count" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        
        assertTotalChanges( 2, changes );
        assertChange( changes, h, header.indexOf("count"), 5, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, cpp, source.indexOf("count"), 5, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        
	 }
	 public void testEnumerator_62() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "enum Foo{E0, E1};//vp1		\n" ); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile( "Foo.h", header ); //$NON-NLS-1$
        writer = new StringWriter();
        writer.write( "#include \"Foo.h\"		   			\n" ); //$NON-NLS-1$
        writer.write( "void f() {		   				    \n" ); //$NON-NLS-1$
        writer.write( " int i=E1;		   			\n" ); //$NON-NLS-1$
        writer.write( "}		   				    \n" ); //$NON-NLS-1$
          
        String source = writer.toString();
        IFile cpp=importFile( "Foo.cpp", source ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( h, header.indexOf( "E1" ) ); //$NON-NLS-1$
        getRefactorChanges( element, "Ooga" ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        
        assertTotalChanges( 2, changes );
        assertChange( changes, h, header.indexOf("E1"), 2, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, cpp, source.indexOf("E1"), 2, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
       
	 }
	 public void testAttribute_63() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Foo{		\n" ); //$NON-NLS-1$
        writer.write( " int att;		\n" ); //$NON-NLS-1$
        writer.write( " Foo(int i);		\n" ); //$NON-NLS-1$
        writer.write( "};		\n" ); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile( "Foo.h", header ); //$NON-NLS-1$
        writer = new StringWriter();
        writer.write( "#include \"Foo.h\"		   			\n" ); //$NON-NLS-1$
        writer.write( "Foo::Foo(int i): att(i) {}		   				    \n" ); //$NON-NLS-1$
          
        String source = writer.toString();
        IFile cpp = importFile( "Foo.cpp", source ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( h, header.indexOf( "att" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        
        assertTotalChanges( 2, changes );
        assertChange( changes, h, header.indexOf("att"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, cpp, source.indexOf("att"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        
	 }
	 public void testAttribute_64() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Foo{				\n" ); //$NON-NLS-1$
        writer.write( "   private: 				\n" ); //$NON-NLS-1$
        writer.write( "   int b;//vp1,rn1		\n" ); //$NON-NLS-1$
        writer.write( "   int m(int b) {		\n" ); //$NON-NLS-1$
        writer.write( "   		return b;		\n" ); //$NON-NLS-1$
        writer.write( "   }						\n" ); //$NON-NLS-1$
        writer.write( "   int n() {				\n" ); //$NON-NLS-1$
        writer.write( "    		return b;//rn2	\n" ); //$NON-NLS-1$
        writer.write( "   }						\n" ); //$NON-NLS-1$
        writer.write( "   int o() {				\n" ); //$NON-NLS-1$
        writer.write( "   		int b=2;		\n" ); //$NON-NLS-1$
        writer.write( "   return b;				\n" ); //$NON-NLS-1$
        writer.write( "	}						\n" ); //$NON-NLS-1$
        writer.write( "};						\n" ); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile( "Foo.h", header ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( h, header.indexOf( "b;//vp1" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        
        assertTotalChanges( 2, changes );
        assertChange( changes, h, header.indexOf("b;//vp1"), 1, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, h, header.indexOf("b;//rn2"), 1, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        
	 }
	 public void testAttribute_65() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class A{				\n" ); //$NON-NLS-1$
        writer.write( "    int x();			\n" ); //$NON-NLS-1$
        writer.write( "};					\n" ); //$NON-NLS-1$
        writer.write( "class B{				\n" ); //$NON-NLS-1$
        writer.write( "    friend class A;	\n" ); //$NON-NLS-1$
        writer.write( "    private: 		\n" ); //$NON-NLS-1$
        writer.write( "    int att;			\n" ); //$NON-NLS-1$
        writer.write( "};					\n" ); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile( "Foo.h", header ); //$NON-NLS-1$
        writer = new StringWriter();
        writer.write( "#include \"Foo.h\"	\n" ); //$NON-NLS-1$
        writer.write( "int A::x() {			\n" ); //$NON-NLS-1$
        writer.write( "	B b;				\n" ); //$NON-NLS-1$
        writer.write( "	int att=b.att;		\n" ); //$NON-NLS-1$
       	writer.write( "} 					\n" ); //$NON-NLS-1$
       	String source = writer.toString();
        IFile cpp = importFile( "Foo.cpp", source ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( h, header.indexOf( "att" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        
        assertTotalChanges( 2, changes );
        assertChange( changes, h, header.indexOf("att"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, cpp, source.indexOf("att;"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        
	 }
	 public void testNamespace_66() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "namespace Foo/*vp1*/{ 			\n" ); //$NON-NLS-1$
        writer.write( " namespace Baz/*vp2*/ {			\n" ); //$NON-NLS-1$
        writer.write( "   int i;   				\n" ); //$NON-NLS-1$
        writer.write( " }						\n" ); //$NON-NLS-1$
        writer.write( " using namespace Baz;	\n" ); //$NON-NLS-1$
        writer.write( "}						\n" ); //$NON-NLS-1$
        writer.write( "void f() {				\n" ); //$NON-NLS-1$
        writer.write( "	 Foo::i = 1;			\n" ); //$NON-NLS-1$
        writer.write( "}  				        \n" ); //$NON-NLS-1$
       
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        //vp1 Foo with ref in function
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "Foo/*vp1*/" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "Ooga" ); //$NON-NLS-1$
        assertTotalChanges( 2, changes );
        assertChange( changes, file, contents.indexOf("Foo/*vp1*/"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Foo::"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        //vp2 nested Baz with ref in using
        element = findElementAtOffset( file, contents.indexOf( "Baz/*vp2*/" ) ); //$NON-NLS-1$
        changes = getRefactorChanges( element, "Wooga" ); //$NON-NLS-1$
        assertTotalChanges( 2, changes );
        assertChange( changes, file, contents.indexOf("Baz/*vp2*/"), 3, "Wooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Baz;"), 3, "Wooga" );  //$NON-NLS-1$//$NON-NLS-2$
   
	 }

	 public void testNamespace_66_79281() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "namespace Foo{ 			\n" ); //$NON-NLS-1$
        writer.write( " int i; 					\n" ); //$NON-NLS-1$
        writer.write( "} 						\n" ); //$NON-NLS-1$
        writer.write( "namespace Bar/*vp1*/ = Foo;		\n" ); //$NON-NLS-1$
        writer.write( "void f() {				\n" ); //$NON-NLS-1$
        writer.write( "  Bar::i = 1;			\n" ); //$NON-NLS-1$
        writer.write( "}  				        \n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "Bar/*vp1*/" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "Ooga" ); //$NON-NLS-1$
        assertTotalChanges( 2, changes );
        assertChange( changes, file, contents.indexOf("Bar/*vp1*/"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Bar::"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
    } 

	public void testNamespace_66_79282() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "namespace Foo/*vp1*/{} 			\n" ); //$NON-NLS-1$
        writer.write( "namespace Bar = Foo;		\n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        //defect is Foo on line 2 is not renamed
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "Foo/*vp1*/" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "Ooga" ); //$NON-NLS-1$
        assertTotalChanges( 2, changes );
        assertChange( changes, file, contents.indexOf("Foo/*vp1*/"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("Foo;"), 3, "Ooga" );  //$NON-NLS-1$//$NON-NLS-2$
    }
	public void testFunction_67() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "void foo/*vp1*/(){}//rn1		\n" ); //$NON-NLS-1$
        writer.write( "void bar(){					\n" ); //$NON-NLS-1$
        writer.write( "  foo();//rn2				\n" ); //$NON-NLS-1$
        writer.write( "}							\n" ); //$NON-NLS-1$
        writer.write( "namespace N{					\n" ); //$NON-NLS-1$
        writer.write( "  class A{					\n" ); //$NON-NLS-1$
        writer.write( "  A() {foo();}//rn3			\n" ); //$NON-NLS-1$
        writer.write( "  };							\n" ); //$NON-NLS-1$
        writer.write( "}							\n" ); //$NON-NLS-1$
         
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "foo/*vp1*/" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        assertTotalChanges( 3, changes );
        assertChange( changes, file, contents.indexOf("foo/*vp1*/"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("foo();//rn2"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("foo();}//rn3"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
   } 
	public void testVariable_68() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class A{					\n" ); //$NON-NLS-1$
        writer.write( "  public: int i;			\n" ); //$NON-NLS-1$
        writer.write( "};						\n" ); //$NON-NLS-1$
        writer.write( "A var;//vp1,rn1			\n" ); //$NON-NLS-1$
        writer.write( "void f(){				\n" ); //$NON-NLS-1$
        writer.write( "  int j = ::var.i;//rn2	\n" ); //$NON-NLS-1$
        writer.write( "}						\n" ); //$NON-NLS-1$
        writer.write( "class B{					\n" ); //$NON-NLS-1$
        writer.write( "  void g(){				\n" ); //$NON-NLS-1$
        writer.write( "    var.i=3;//rn3		\n" ); //$NON-NLS-1$
        writer.write( "  }						\n" ); //$NON-NLS-1$
        writer.write( "};						\n" ); //$NON-NLS-1$
         
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "var;//vp1" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        assertTotalChanges( 3, changes );
        assertChange( changes, file, contents.indexOf("var;//vp1"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("var.i;//rn2"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("var.i=3;//rn3"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
   } 
	public void testVariable_68_79295() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "int var;//vp1			\n" ); //$NON-NLS-1$
        writer.write( "void f(int var){			\n" ); //$NON-NLS-1$
        writer.write( "  int i = var;			\n" ); //$NON-NLS-1$
        writer.write( "}						\n" ); //$NON-NLS-1$
           
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        //defect is the argument and local variable var are incorrectly renamed
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "var;//vp1" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        assertTotalChanges( 1, changes );
        assertChange( changes, file, contents.indexOf("var;//vp1"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
    } 
	public void testVariable_69() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "int i;//vp1			\n" ); //$NON-NLS-1$
        writer.write( "in t j;				\n" ); //$NON-NLS-1$
           
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "i;//vp1" ) ); //$NON-NLS-1$
        
        String[] messages = getRefactorMessages( element, "ooga" ); //$NON-NLS-1$
        assertEquals(1, messages.length);
        assertEquals("Code modification may not be accurate as affected resoure 'RegressionTestProject/t.cpp' has compile errors.", messages[0] );   //$NON-NLS-1$
	} 
	//similar to test 92, except this one will continue with warning, or error status
    //while case in 92 must stop refactor with fatal status
    public void testClass_81_72620() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "union u_haul{};		\n" ); //$NON-NLS-1$
        writer.write( "struct s_haul{};    	\n" ); //$NON-NLS-1$
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "s_haul" ) ); //$NON-NLS-1$
        try {
        	getRefactorChanges( element, "u_haul" ); //$NON-NLS-1$
        } catch (AssertionFailedError e) {
        	assertTrue(e.getMessage().startsWith("Input check on u_haul failed.")); //$NON-NLS-1$
        	return;
        }
        fail ("An error should have occurred in the input check."); //$NON-NLS-1$ 
    }
	public void testVariable_88_72617() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class A{};				\n" ); //$NON-NLS-1$
        writer.write( "A a;//vp1				\n" ); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile( "Foo.h", header ); //$NON-NLS-1$
        writer = new StringWriter();
        writer.write( "#include \"Foo.h\"	\n" ); //$NON-NLS-1$
        writer.write( "void f() {			\n" ); //$NON-NLS-1$
        writer.write( "	A a;				\n" ); //$NON-NLS-1$
        writer.write( "} 					\n" ); //$NON-NLS-1$
       	String source = writer.toString();
        importFile( "Foo.cpp", source ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( h, header.indexOf( "a;//vp1" ) ); //$NON-NLS-1$
        IChange changes = getRefactorChanges( element, "ooga" ); //$NON-NLS-1$
        
        assertTotalChanges( 1, changes );
        assertChange( changes, h, header.indexOf("a;//vp1"), 1, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
    }
	//2 ways to test name collistion on same type:
	//if you don't know the error message, catch on getRefactorChanges
	//or if you want to verify a message or severity, use getRefactorMessages
	//and getRefactorSeverity
	 public void testClass_92A() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Boo{}; 		\n" ); //$NON-NLS-1$
        writer.write( "  void f() {}    	\n" ); //$NON-NLS-1$
        writer.write( "};               	\n" ); //$NON-NLS-1$
        writer.write( "class Foo/*vp1*/{}; 	\n" ); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "Foo/*vp1*/" ) ); //$NON-NLS-1$
        try {
        	getRefactorChanges( element, "Boo" ); //$NON-NLS-1$
        } catch (AssertionFailedError e) {
        	assertTrue(e.getMessage().startsWith("Input check on Boo failed.")); //$NON-NLS-1$
        	return;
        }
        fail ("An error or warning should have occurred in the input check."); //$NON-NLS-1$ 
    }
	public void testClass_92B() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class A{};			\n" ); //$NON-NLS-1$
        writer.write( "class B{};//vp1		\n" ); //$NON-NLS-1$
           
        String contents = writer.toString();
        IFile file = importFile( "t.cpp", contents ); //$NON-NLS-1$
        ISourceReference element = findElementAtOffset( file, contents.indexOf( "B{};//vp1" ) ); //$NON-NLS-1$
        
        String[] messages = getRefactorMessages( element, "A" ); //$NON-NLS-1$
        assertEquals(1, messages.length);
        assertEquals("Another element named 'A' already exists in 't.cpp'", messages[0] );   //$NON-NLS-1$
        // assert that you cannot refactor because severity is FATAL (4)
        int s = getRefactorSeverity(element, "A"); //$NON-NLS-1$
        assertEquals(RefactoringStatus.FATAL,s);
	} 
}
