/*******************************************************************************
 * Copyright (c) 2015 Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.selection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.core.parser.ParserException;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.actions.OpenDeclarationsAction;

public abstract class BaseSelectionTests extends BaseUITestCase {
	private IProgressMonitor monitor = new NullProgressMonitor();
	
	public BaseSelectionTests() {
		super();
	}
	public BaseSelectionTests(String name) {
		super(name);
	}
	
	/**
	 * Derived classes should override this to return 'true' if they run tests where the
	 * OpenDeclarationsAction can open a different editor than the one from which the action was invoked. 
	 */
	protected boolean shouldUpdateEditor() {
		return false;
	}
	
	protected IASTNode testF3(IFile file, int offset) throws ParserException, CoreException {
		return testF3(file, offset, 0);
	}

    protected IASTNode testF3(IFile file, int offset, int length) throws ParserException, CoreException {
		if (offset < 0)
			throw new ParserException("offset can not be less than 0 and was " + offset); //$NON-NLS-1$
		
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart part = null;
        try {
            part = page.openEditor(new FileEditorInput(file), "org.eclipse.cdt.ui.editor.CEditor"); //$NON-NLS-1$
        } catch (PartInitException e) {
            assertFalse(true);
        }
        
        if (part instanceof CEditor) {
        	CEditor editor= (CEditor) part;
        	EditorTestHelper.joinReconciler(EditorTestHelper.getSourceViewer(editor), 100, 5000, 10);
            editor.getSelectionProvider().setSelection(new TextSelection(offset,length));
            
            final OpenDeclarationsAction action = (OpenDeclarationsAction) editor.getAction("OpenDeclarations"); //$NON-NLS-1$
            action.runSync();
            
            if (shouldUpdateEditor()) {
            	// update the file/part to point to the newly opened IFile/IEditorPart
                part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor(); 
                assertTrue (part instanceof CEditor);
                editor= (CEditor) part;
        		EditorTestHelper.joinReconciler(EditorTestHelper.getSourceViewer(editor), 0, 5000, 10);
            }
        
            // the action above should highlight the declaration, so now retrieve it and use that selection to get the IASTName selected on the TU
            ISelection sel = editor.getSelectionProvider().getSelection();
            
            final IASTName[] result= { null };
            if (sel instanceof ITextSelection) {
            	final ITextSelection textSel = (ITextSelection)sel;
            	ITranslationUnit tu= (ITranslationUnit) editor.getInputCElement();
        		IStatus ok= ASTProvider.getASTProvider().runOnAST(tu, ASTProvider.WAIT_IF_OPEN, monitor, new ASTRunnable() {
        			@Override
					public IStatus runOnAST(ILanguage language, IASTTranslationUnit ast) throws CoreException {
        				result[0]= ast.getNodeSelector(null).findName(textSel.getOffset(), textSel.getLength());        	
        				return Status.OK_STATUS;
        			}
        		});
        		assertTrue(ok.isOK());
				return result[0];
            }
        }
        
        return null;
    }
}
