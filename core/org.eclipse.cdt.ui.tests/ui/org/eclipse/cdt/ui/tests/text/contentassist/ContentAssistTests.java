/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Sep 9, 2004
 */
package org.eclipse.cdt.ui.tests.text.contentassist;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;

import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.CHelpBookDescriptor;
import org.eclipse.cdt.internal.ui.text.contentassist.CContentAssistProcessor;

/**
 * @author aniefer
 */
public class ContentAssistTests extends BaseUITestCase {
    private NullProgressMonitor		monitor= new NullProgressMonitor();
    static IProject 				project;
    static boolean 					disabledHelpContributions = false;
    
    public void setUp() {
		//(CCorePlugin.getDefault().getCoreModel().getIndexManager()).reset();
    	
    	if (project == null) {
    		ICProject cPrj; 
    		try {
    			cPrj = CProjectHelper.createCCProject("ContentAssistTestProject", "bin", IPDOMManager.ID_FAST_INDEXER); //$NON-NLS-1$ //$NON-NLS-2$

    			project = cPrj.getProject();
    		} catch ( CoreException e ) {
    			/*boo*/
    		}
    		if (project == null)
    			fail("Unable to create project"); //$NON-NLS-1$
    		assertTrue(CCorePlugin.getIndexManager().joinIndexer(10000, monitor));
    	}
	}
    public ContentAssistTests()
    {
        super();
    }
    /**
     * @param name
     */
    public ContentAssistTests(String name)
    {
        super(name);
    }
    
    private void disableContributions (){
        //disable the help books so we don't get proposals we weren't expecting
        CHelpBookDescriptor helpBooks[];
		helpBooks = CHelpProviderManager.getDefault().getCHelpBookDescriptors(new ICHelpInvocationContext(){
			public IProject getProject(){return project;}
			public ITranslationUnit getTranslationUnit(){return null;}
			}
		);
		for( int i = 0; i < helpBooks.length; i++ ){
		    if( helpBooks[i] != null )
		        helpBooks[i].enable( false );
		}
    }
    
    public static Test suite() {
        TestSuite suite= suite(ContentAssistTests.class, "_");
        suite.addTest( new ContentAssistTests("cleanupProject") );    //$NON-NLS-1$
	    return suite;
    }
    
    public void cleanupProject() throws Exception {
        try{
	        project.delete( true, false, monitor );
	        project = null;
	    } catch( Throwable e ){
	        /*boo*/
	    }
    }
    
    protected void tearDown() throws Exception {
        if( project == null || !project.exists() ) 
            return;
        
        IResource [] members = project.members();
        for( int i = 0; i < members.length; i++ ){
            if( members[i].getName().equals( ".project" ) || members[i].getName().equals( ".cdtproject" ) ) //$NON-NLS-1$ //$NON-NLS-2$
                continue;
            if (members[i].getName().equals(".settings")) 
            	continue;
            try{
                members[i].delete( false, monitor );
            } catch( Throwable e ){
                /*boo*/
            }
        }
        project= null;
	}
    
    protected IFile importFile(String fileName, String contents ) throws Exception{
		//Obtain file handle
		IFile file = project.getProject().getFile(fileName);
		
		InputStream stream = new ByteArrayInputStream( contents.getBytes() ); 
		//Create file input stream
		if( file.exists() )
		    file.setContents( stream, false, false, monitor );
		else
			file.create( stream, false, monitor );
		
		return file;
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
	
		// call the ContentAssistProcessor
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		FileEditorInput editorInput = new FileEditorInput(file);
		IEditorPart editorPart = page.openEditor(editorInput, "org.eclipse.cdt.ui.editor.CEditor");
		CEditor editor = (CEditor) editorPart ;
		IAction completionAction = editor.getAction("ContentAssistProposal");

		String contentType = editor.getViewer().getDocument().getContentType(offset);
		ContentAssistant assistant = new ContentAssistant();
		CContentAssistProcessor processor = new CContentAssistProcessor(editor, assistant, contentType);
		return processor.computeCompletionProposals(editor.getViewer(), offset);
    }
    
    public void testBug69334() throws Exception {
        importFile( "test.h", "class Test{ public : Test( int ); }; \n" );  //$NON-NLS-1$//$NON-NLS-2$
        StringWriter writer = new StringWriter();
        writer.write( "#include \"test.h\"                \n"); //$NON-NLS-1$
        writer.write( "Test::Test( int i ) { return; }    \n"); //$NON-NLS-1$
        writer.write( "int main() {                       \n"); //$NON-NLS-1$
        writer.write( "   int veryLongName = 1;           \n"); //$NON-NLS-1$
        writer.write( "   Test * ptest = new Test( very   \n"); //$NON-NLS-1$
        
        String code = writer.toString();
        IFile cu = importFile( "test.cpp", code ); //$NON-NLS-1$
        
        ICompletionProposal [] results = getResults( cu, code.indexOf( "very " ) + 4 ); //$NON-NLS-1$
        
        assertEquals( 1, results.length );
        assertEquals( "veryLongName : int", results[0].getDisplayString() ); //$NON-NLS-1$
    }

    public void testBug72824() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Strategy {                             \n"); //$NON-NLS-1$
        writer.write( "public :                                     \n"); //$NON-NLS-1$
        writer.write( "   enum _Ability { IDIOT, NORMAL, CHEAT } ;  \n"); //$NON-NLS-1$
        writer.write( "   Strategy( _Ability a ) { }                \n"); //$NON-NLS-1$
        writer.write( "};                                           \n"); //$NON-NLS-1$
        writer.write( "int main(){                                  \n"); //$NON-NLS-1$
        
        String code = writer.toString();
        String c2 = code + "   Strategy *p[3] = { new Strategy( Str \n"; //$NON-NLS-1$
        
        IFile cu = importFile( "strategy.cpp", c2 ); //$NON-NLS-1$
        
        ICompletionProposal [] results = getResults( cu, c2.indexOf( "Str " ) + 3 ); //$NON-NLS-1$
        assertEquals( 1, results.length );
        assertEquals( "Strategy", results[0].getDisplayString() ); //$NON-NLS-1$
        
        c2 = code + "   Strategy *p[3] = { new Strategy( Strategy:: \n"; //$NON-NLS-1$

        cu = importFile( "strategy.cpp", c2 ); //$NON-NLS-1$
        
        results = getResults( cu, c2.indexOf( "::" ) + 2 ); //$NON-NLS-1$
        assertEquals( 3, results.length );
        assertEquals( "CHEAT", results[0].getDisplayString()  ); //$NON-NLS-1$
        assertEquals( "IDIOT", results[1].getDisplayString()  ); //$NON-NLS-1$
        assertEquals( "NORMAL", results[2].getDisplayString()  ); //$NON-NLS-1$
    }
    
    public void testBug72559() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("void foo(){               \n"); //$NON-NLS-1$
        writer.write("   int var;               \n"); //$NON-NLS-1$
        writer.write("   {                      \n"); //$NON-NLS-1$
        writer.write("      float var;          \n"); //$NON-NLS-1$
        writer.write("      v                   \n"); //$NON-NLS-1$
        writer.write("   }                      \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$

        String code = writer.toString();
        IFile cu = importFile( "t.cpp", code ); //$NON-NLS-1$
        ICompletionProposal [] results = getResults( cu, code.indexOf( "v " ) + 1 ); //$NON-NLS-1$
        
        assertEquals( results.length, 3 );
        assertEquals( results[0].getDisplayString(), "var : float" ); //$NON-NLS-1$
        assertEquals( results[1].getDisplayString(), "virtual" ); //$NON-NLS-1$
        assertEquals( results[2].getDisplayString(), "volatile" ); //$NON-NLS-1$
    }
}
