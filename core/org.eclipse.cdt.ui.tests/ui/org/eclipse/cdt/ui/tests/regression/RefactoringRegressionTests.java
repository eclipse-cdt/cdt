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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.tests.SearchRegressionTests;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.internal.corext.refactoring.RenameRefactoring;
import org.eclipse.cdt.internal.corext.refactoring.base.IChange;
import org.eclipse.cdt.internal.corext.refactoring.base.ICompositeChange;
import org.eclipse.cdt.internal.corext.refactoring.changes.TranslationUnitChange;
import org.eclipse.cdt.internal.corext.refactoring.rename.RenameElementProcessor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
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
        
        fRefactoring.checkInput( new NullProgressMonitor() );
        IChange change = fRefactoring.createChange( new NullProgressMonitor() );
        return change;
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
        
        if( cleanup )
            suite.addTest( new RefactoringRegressionTests("cleanupProject") );    //$NON-NLS-1$
        
	    return suite;
    }

    protected void assertTotalChanges( IChange changes, int numChanges ) throws Exception {
        int count = countChanges( changes );
        assertEquals( count, numChanges );
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
        if( changes instanceof ICompositeChange ){
            found = checkCompositeChange( (ICompositeChange) changes, file, startOffset, numChars, newText );
        }
        
        if( !found )
            assertFalse( true );
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
        
        assertTotalChanges( changes, 2 );
        assertChange( changes, file, contents.indexOf("boo"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
        assertChange( changes, file, contents.indexOf("boo++"), 3, "ooga" );  //$NON-NLS-1$//$NON-NLS-2$
    }
}
