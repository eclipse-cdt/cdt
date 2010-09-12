/*******************************************************************************
 * Copyright (c) 2004, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *    Markus Schorn - initial API and implementation
 *    Sergey Prigogin (Google)
 ******************************************************************************/ 
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ide.IDE;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringStarter;

/**
 * Serves to launch the various refactorings.
 */
public class CRefactory {
    public static final int OPTION_ASK_SCOPE 					= 0x01;
    public static final int OPTION_IN_COMMENT 					= 0x02;
    public static final int OPTION_IN_STRING_LITERAL 			= 0x04;
    public static final int OPTION_IN_INCLUDE_DIRECTIVE 		= 0x08;
    public static final int OPTION_IN_MACRO_DEFINITION 			= 0x10;
    public static final int OPTION_IN_PREPROCESSOR_DIRECTIVE 	= 0x20;
    public static final int OPTION_IN_INACTIVE_CODE 			= 0x40;
    public static final int OPTION_IN_CODE 						= 0x80;
    public static final int OPTION_DO_VIRTUAL					= 0x100;
    public static final int OPTION_EXHAUSTIVE_FILE_SEARCH		= 0x200;

    public static final int ARGUMENT_UNKNOWN				=  0;
    public static final int ARGUMENT_LOCAL_VAR				=  1;
    public static final int ARGUMENT_PARAMETER 				=  2;
    public static final int ARGUMENT_FILE_LOCAL_VAR 		=  3;
    public static final int ARGUMENT_GLOBAL_VAR 			=  4;
    public static final int ARGUMENT_FIELD					=  5;
    public static final int ARGUMENT_FILE_LOCAL_FUNCTION	=  6;
    public static final int ARGUMENT_GLOBAL_FUNCTION 		=  7;
    public static final int ARGUMENT_VIRTUAL_METHOD 		=  8;
    public static final int ARGUMENT_NON_VIRTUAL_METHOD 	=  9;
    public static final int ARGUMENT_TYPE 					= 10;
    public static final int ARGUMENT_MACRO 					= 11;
    public static final int ARGUMENT_INCLUDE_DIRECTIVE 		= 12;
    public static final int ARGUMENT_ENUMERATOR             = 13;
    public static final int ARGUMENT_CLASS_TYPE             = 14;
    public static final int ARGUMENT_NAMESPACE              = 15;
    
    private static CRefactory sInstance= new CRefactory();
    private TextSearchWrapper fTextSearch;
    
    public static CRefactory getInstance() {
        return sInstance;
    }
    
    private CRefactory() {
    }
    
    // Runs the rename refactoring.
    public void rename(Shell shell, ICElement arg) {
        if (!IDE.saveAllEditors(new IResource[] { ResourcesPlugin.getWorkspace().getRoot() }, false)) {
            return;
        }
        CRefactoringArgument iarg= new CRefactoringArgument(arg);
        final CRenameProcessor processor = new CRenameProcessor(this, iarg);
		CRenameRefactoring refactoring= new CRenameRefactoring(processor);
        openDialog(shell, refactoring, false);
    }
    
	public void rename(Shell shell, IWorkingCopy workingCopy, ITextSelection selection) {
        IResource res= workingCopy.getResource();
        if (!(res instanceof IFile)) {
        	return;
        }
        if (!IDE.saveAllEditors(new IResource[] { ResourcesPlugin.getWorkspace().getRoot() }, false)) {
            return;
        }
        CRefactoringArgument iarg=
        	new CRefactoringArgument((IFile) res, selection.getOffset(), selection.getLength());
        final CRenameProcessor processor = new CRenameProcessor(this, iarg);
		CRenameRefactoring refactoring= new CRenameRefactoring(processor);
        openDialog(shell, refactoring, false);
	}

	/**
	 * Opens the refactoring dialog.
	 *
	 * <p>
	 * This method has to be called from within the UI thread.
	 * </p>
	 *
	 * @param shell a shell used as a parent for the refactoring, preview, or error dialog
	 * @param showPreviewOnly if <code>true</code>, the dialog skips all user input pages and
	 * directly shows the preview or error page. Otherwise, shows all pages.
	 * @return <code>true</code> if the refactoring has been executed successfully,
	 * or <code>false</code> if it has been canceled.
	 */
	static boolean openDialog(Shell shell, CRenameRefactoring refactoring, boolean showPreviewOnly) {
		try {
    		CRenameRefactoringWizard wizard;
    		if (!showPreviewOnly) {
    			wizard = new CRenameRefactoringWizard(refactoring);
    		} else {
    			wizard = new CRenameRefactoringWizard(refactoring) {
    				@Override
					protected void addUserInputPages() {
    					// nothing to add
    				}
    			};
    			wizard.setForcePreviewReview(showPreviewOnly);
    		}
    		RefactoringStarter starter = new RefactoringStarter();
			CRenameProcessor processor = (CRenameProcessor) refactoring.getProcessor();
        	processor.lockIndex();
        	try {
        		processor.checkInitialConditions(new NullProgressMonitor());
        		return starter.activate(wizard, shell, RenameMessages.CRefactory_title_rename, processor.getSaveMode());
        	} finally {
        		processor.unlockIndex();
        	}
        } catch (InterruptedException e) {
			Thread.currentThread().interrupt();
        } catch (CoreException e) {
        	CUIPlugin.log(e);
		}
        return false;
	}

    public TextSearchWrapper getTextSearch() {
        if (fTextSearch == null) {
            return new TextSearchWrapper();
        }
        return fTextSearch;
    }
    
    public String[] getCCppPatterns() {
        IContentType[] cts= Platform.getContentTypeManager().getAllContentTypes();
        HashSet<String> all= new HashSet<String>();
        for (IContentType type : cts) {
            boolean useit= false;
            while (!useit && type != null) {
                String id= type.getId();
                if (id.equals(CCorePlugin.CONTENT_TYPE_CHEADER) ||
                        id.equals(CCorePlugin.CONTENT_TYPE_CSOURCE) ||
                        id.equals(CCorePlugin.CONTENT_TYPE_CXXHEADER) ||
                        id.equals(CCorePlugin.CONTENT_TYPE_CXXSOURCE)) {
                	useit= true;
                } else {
                    type= type.getBaseType();
                }
            }
            if (useit) {
                String exts[] = type.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
                all.addAll(Arrays.asList(exts));
            }
        }
        String[] result= new String[all.size()];
        Iterator<String> it= all.iterator();
        for (int i= 0; i < result.length; i++) {
            result[i]= "*." + it.next(); //$NON-NLS-1$
        }
        return result;
    }
}
