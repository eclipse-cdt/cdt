/*******************************************************************************
 * Copyright (c) 2005, 2008 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.refactoring.rename;

import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @author markus.schorn@windriver.com
 */
public class RenameTemplatesTests extends RenameTests {

    public RenameTemplatesTests(String name) {
        super(name);
    }
    public static Test suite(){
        return suite(true);
    }
    public static Test suite( boolean cleanup ) {
        TestSuite suite = new TestSuite(RenameTemplatesTests.class); 
        if (cleanup) {
            suite.addTest( new RefactoringTests("cleanupProject") );    //$NON-NLS-1$
        }
        return suite;
    }
    
    
    public void testClassTemplate() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("template <class Type>   \n"); //$NON-NLS-1$
        writer.write("class Array {                \n"); //$NON-NLS-1$
        writer.write("public:                   \n"); //$NON-NLS-1$
        writer.write("   Array(unsigned sz) {}  \n"); //$NON-NLS-1$
        writer.write("   ~Array(){}             \n"); //$NON-NLS-1$
        writer.write("   Type& operator[] (unsigned idx); \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("template <class Type>     \n"); //$NON-NLS-1$
        writer.write("inline Type& Array<Type>::operator[] (unsigned index) {\n"); //$NON-NLS-1$
        writer.write("   return 1;              \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset1= contents.indexOf("Array"); //$NON-NLS-1$
        
        RefactoringStatus stat= checkConditions(cpp, offset1, "WELT"); //$NON-NLS-1$
        assertRefactoringOk(stat);
        
        Change ch= getRefactorChanges(cpp, offset1, "WELT");  //$NON-NLS-1$
        assertTotalChanges(4, ch);
    }
}
