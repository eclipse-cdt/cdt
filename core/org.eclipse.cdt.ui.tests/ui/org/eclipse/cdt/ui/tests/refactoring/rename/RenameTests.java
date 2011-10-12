/*******************************************************************************
 * Copyright (c) 2005, 2010 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *    Markus Schorn - initial API and implementation 
 ******************************************************************************/ 
package org.eclipse.cdt.ui.tests.refactoring.rename;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;

import org.eclipse.cdt.internal.ui.refactoring.rename.CRefactoringArgument;
import org.eclipse.cdt.internal.ui.refactoring.rename.CRefactory;
import org.eclipse.cdt.internal.ui.refactoring.rename.CRenameProcessor;
import org.eclipse.cdt.internal.ui.refactoring.rename.CRenameRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.rename.TextSearchWrapper;

/**
 * @author markus.schorn@windriver.com
 */
public class RenameTests extends RefactoringTests {

    private static final IProgressMonitor NPM = new NullProgressMonitor();

	public RenameTests(String name) {
        super(name);
    }

    public RenameTests() {
    }

    /**
     * @param element   The CElement to rename
     * @param newName   The new name for the element
     * @return
     * @throws Exception
     */
    public Change getRefactorChanges(IFile file, int offset, String newName) throws Exception {
        CRenameRefactoring proc = createRefactoring(file, offset, newName);
        
        ((CRenameProcessor) proc.getProcessor()).lockIndex();
        try {
        	RefactoringStatus rs = checkConditions(proc);
        	if (!rs.hasError()) {
        		Change change = proc.createChange( new NullProgressMonitor() );
        		return change;
        	} 

        	fail ("Input check on "+ newName + " failed. "+rs.getEntryMatchingSeverity(RefactoringStatus.ERROR) ); //$NON-NLS-1$ //$NON-NLS-2$
        	//rs.getFirstMessage(RefactoringStatus.ERROR) is not the message displayed in 
        	//the UI for renaming a method to a constructor, the first message which is only
        	//a warning is shown in the UI. If you click preview, then the error and the warning
        	//is shown. 
        	return null;
        } finally {
            ((CRenameProcessor) proc.getProcessor()).unlockIndex();
        }
    }

    private CRenameRefactoring createRefactoring(IFile file, int offset, String newName) {
    	CRefactoringArgument arg= new CRefactoringArgument(file, offset, 0);
        CRenameProcessor proc= new CRenameProcessor(CRefactory.getInstance(), arg);
        proc.setReplacementText( newName );
        proc.setSelectedOptions(-1);
        proc.setExhaustiveSearchScope(TextSearchWrapper.SCOPE_WORKSPACE);
        return new CRenameRefactoring(proc);
    }

    public String[] getRefactorMessages(IFile file, int offset, String newName) throws Exception {
        String[] result;
        CRenameRefactoring proc = createRefactoring(file, offset, newName);
        ((CRenameProcessor) proc.getProcessor()).lockIndex();
        try {
        	RefactoringStatus rs = checkConditions(proc);
        	if (!rs.hasWarning()){
        		fail ("Input check on "+ newName + " passed. There should have been warnings or errors. ") ; //$NON-NLS-1$ //$NON-NLS-2$
        		return null;
        	}
        	RefactoringStatusEntry[] rse = rs.getEntries();
        	result = new String[rse.length];
        	for (int i=0; i< rse.length; i++){
        		RefactoringStatusEntry entry = rse[i];
        		result[i]=entry.getMessage();

        	} 
        	return result;
        } finally {
            ((CRenameProcessor) proc.getProcessor()).unlockIndex();
        }
    }

    public RefactoringStatus checkConditions(IFile file, int offset, String newName) throws Exception {
        CRenameRefactoring proc = createRefactoring(file, offset, newName);
        ((CRenameProcessor) proc.getProcessor()).lockIndex();
        try {
        	return checkConditions(proc);
        } finally {
            ((CRenameProcessor) proc.getProcessor()).unlockIndex();
        }
    }
    
    private RefactoringStatus checkConditions(CRenameRefactoring proc) throws CoreException {
        RefactoringStatus rs =proc.checkInitialConditions(new NullProgressMonitor() );
        if (!rs.hasError()){
            rs= proc.checkFinalConditions(new NullProgressMonitor());
        }
        return rs;
    }

    public int getRefactorSeverity(IFile file, int offset, String newName) throws Exception {
        CRenameRefactoring proc = createRefactoring(file, offset, newName);
        ((CRenameProcessor) proc.getProcessor()).lockIndex();
        try {
        	RefactoringStatus rs = checkConditions(proc);
        	return rs.getSeverity();
        } finally {
            ((CRenameProcessor) proc.getProcessor()).unlockIndex();
        }
    }

    protected int countOccurrences(String contents, String lookup) {
        int idx= contents.indexOf(lookup);
        int count= 0;
        while (idx >= 0) {
            count++;
            idx= contents.indexOf(lookup, idx+lookup.length());
        }
        return count;
    }
    
	protected void waitForIndexer() throws InterruptedException {
		final IIndexManager im = CCorePlugin.getIndexManager();
		assertTrue(im.joinIndexer(10000, NPM));
		int sleep= 1;
		while (im.isIndexerSetupPostponed(cproject)) {
			Thread.sleep(sleep);
			sleep *= 2;
			assertTrue(sleep < 2000);
		}
		assertTrue(im.joinIndexer(10000, NPM));
	}

	@Override
	protected IFile importFile(String fileName, String contents)
			throws Exception {
		IFile result= super.importFile(fileName, contents);
		waitForIndexer();
		return result;
	}
}
