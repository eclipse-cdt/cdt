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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProcessor;
import org.eclipse.core.resources.IFile;
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
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest( new ContentAssistRegressionTests("testMemberCompletion") ); //$NON-NLS-1$
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
        
        assertEquals( results.length, 4 );
        assertEquals( results[0].getDisplayString(), "var : int" ); //$NON-NLS-1$
        assertEquals( results[1].getDisplayString(), "virtual" ); //$NON-NLS-1$
        assertEquals( results[2].getDisplayString(), "void" ); //$NON-NLS-1$
        assertEquals( results[3].getDisplayString(), "volatile" ); //$NON-NLS-1$
    }
}
