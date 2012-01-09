/*******************************************************************************
 *  Copyright (c) 2005, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.selection;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search2.internal.ui.SearchView;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.FileManager;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.core.parser.ParserException;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.ICEditorActionDefinitionIds;
import org.eclipse.cdt.internal.ui.search.actions.OpenDeclarationsAction;

/**
 * Base test class for testing Ctrl_F3/F3 with the indexers.
 *  
 * @author dsteffle
 */
public class BaseSelectionTestsIndexer extends BaseUITestCase {
	protected ICProject fCProject;
	static FileManager fileManager = new FileManager();
	IProgressMonitor monitor = new NullProgressMonitor();
	
	public BaseSelectionTestsIndexer(String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		OpenDeclarationsAction.sIsJUnitTest= true;
		IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewReference[] refs= page.getViewReferences();
		for (IViewReference viewReference : refs) {
			page.setPartState(viewReference, IWorkbenchPage.STATE_RESTORED);
		}
	}
	
	public void waitForIndex(int maxSec) throws Exception {
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(maxSec * 1000, new NullProgressMonitor()));
	}
	
	protected String getMessage(IStatus status) {
		StringBuffer message = new StringBuffer("["); //$NON-NLS-1$
		message.append(status.getMessage());
		if (status.isMultiStatus()) {
			IStatus children[] = status.getChildren();
			for (IStatus element : children) {
				message.append(getMessage(element));
			}
		}
		message.append("]"); //$NON-NLS-1$
		return message.toString();
	}

    protected IFile importFile(String fileName, String contents) throws Exception{
        //Obtain file handle
        IFile file = fCProject.getProject().getFile(fileName);
        
        InputStream stream = new ByteArrayInputStream(contents.getBytes()); 
        //Create file input stream
        if (file.exists())
            file.setContents(stream, true, false, monitor);
        else
            file.create(stream, true, monitor);
        
        fileManager.addFile(file);
        
        waitForIndex(20); // only wait 20 seconds max.
        
        return file;
    }
    
    protected IFile importFileWithLink(String fileName, String contents) throws Exception{
        //Obtain file handle
        IFile file = fCProject.getProject().getFile(fileName);
        
        IPath location = new Path(fCProject.getProject().getLocation().removeLastSegments(1).toOSString() + File.separator + fileName); 
        
        File linkFile = new File(location.toOSString());
        if (linkFile.exists()) {
        	linkFile.delete();
        }
        
        file.createLink(location, IResource.ALLOW_MISSING_LOCAL, null);
        
        InputStream stream = new ByteArrayInputStream(contents.getBytes()); 
        //Create file input stream
        if (file.exists()) {
			long timestamp= file.getLocalTimeStamp();
        	file.setContents(stream, true, false, monitor);
        	if (file.getLocalTimeStamp() == timestamp) {
        		file.setLocalTimeStamp(timestamp+1000);
        	}
        } else {
            file.create(stream, true, monitor);
        }
        
        fileManager.addFile(file);
        
        return file;
    }
    
    protected IFile importFileInsideLinkedFolder(String fileName, String contents, String folderName) throws Exception{
    	IProject project= fCProject.getProject();
    	IFolder linkedFolder = project.getFolder(folderName);
    	IPath folderLocation = new Path(project.getLocation().toOSString() + File.separator + folderName + "_this_is_linked"); //$NON-NLS-1$
    	IFolder actualFolder = project.getFolder(folderName + "_this_is_linked"); //$NON-NLS-1$
    	if (!actualFolder.exists())
    		actualFolder.create(true, true, monitor);
    	
    	linkedFolder.createLink(folderLocation, IResource.NONE, monitor);
    	
    	actualFolder.delete(true, false, monitor);
    	
    	IFile file = linkedFolder.getFile(fileName);
    	
        InputStream stream = new ByteArrayInputStream(contents.getBytes()); 
        //Create file input stream
        if (file.exists())
            file.setContents(stream, true, false, monitor);
        else
            file.create(stream, true, monitor);
            	
        fileManager.addFile(file);
    	
        return file;
    }

    protected IFolder importFolder(String folderName) throws Exception {
    	IFolder folder = fCProject.getProject().getFolder(folderName);
		
		//Create file input stream
		if (!folder.exists())
			folder.create(false, false, monitor);
		
		return folder;
    }
    
    protected String getEditorID() {
    	return "org.eclipse.cdt.ui.editor.CEditor";
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
            part = page.openEditor(new FileEditorInput(file), getEditorID(), true); 
        } catch (PartInitException e) {
            assertFalse(true);
        }
        
        if (part instanceof CEditor) {
        	CEditor editor= (CEditor) part;
    		EditorTestHelper.joinReconciler(EditorTestHelper.getSourceViewer(editor), 100, 5000, 10);
            ((AbstractTextEditor)part).getSelectionProvider().setSelection(new TextSelection(offset,length));
            
            final OpenDeclarationsAction action = (OpenDeclarationsAction) editor.getAction("OpenDeclarations"); //$NON-NLS-1$
            action.runSync();
        
        	// update the file/part to point to the newly opened IFile/IEditorPart
            part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor(); 
            assertTrue (part instanceof CEditor);
            editor= (CEditor) part;
    		EditorTestHelper.joinReconciler(EditorTestHelper.getSourceViewer(editor), 100, 5000, 10);

    		// the action above should highlight the declaration, so now retrieve it and use that selection to get the IASTName selected on the TU
            ISelection sel= editor.getSelectionProvider().getSelection();
            
            final IASTName[] result= {null};
            if (sel instanceof ITextSelection) {
            	final ITextSelection textSel = (ITextSelection)sel;
            	ITranslationUnit tu = (ITranslationUnit)editor.getInputCElement();
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
    
    protected ISelection testF3Selection(IFile file, int offset) throws ParserException {
    	return testF3Selection(file, offset, 0);
    }
    
    protected ISelection testF3Selection(IFile file, int offset, int length) throws ParserException {
		if (offset < 0)
			throw new ParserException("offset can not be less than 0 and was " + offset); //$NON-NLS-1$
		
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart part = null;
        try {
            part = page.openEditor(new FileEditorInput(file), getEditorID()); 
        } catch (PartInitException e) {
            assertFalse(true);
        }
        
        if (part instanceof AbstractTextEditor) {
            ((AbstractTextEditor)part).getSelectionProvider().setSelection(new TextSelection(offset,length));
            
            final IAction action = ((AbstractTextEditor)part).getAction("OpenDeclarations"); //$NON-NLS-1$
            action.run();
        
        	// update the file/part to point to the newly opened IFile/IEditorPart
            part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor(); 
            IEditorInput input = part.getEditorInput(); 
            if (input instanceof FileEditorInput) {
            	file = ((FileEditorInput)input).getFile();
            } else {
            	assertFalse(true); // bail!
            }             
            
            // the action above should highlight the declaration, so now retrieve it and use that selection to get the IASTName selected on the TU
            return ((AbstractTextEditor)part).getSelectionProvider().getSelection();
        }
        
        return null;
    }
            
    protected void testSimple_Ctrl_G_Selection(IFile file, int offset, int length, int numOccurrences) throws ParserException {
		if (offset < 0)
			throw new ParserException("offset can not be less than 0 and was " + offset); //$NON-NLS-1$
		
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart part = null;
        try {
            part = page.openEditor(new FileEditorInput(file), getEditorID()); 
        } catch (PartInitException e) {
            assertFalse(true);
        }
        
        if (part instanceof AbstractTextEditor) {
            ((AbstractTextEditor)part).getSelectionProvider().setSelection(new TextSelection(offset,length));
            
            final IAction action = ((AbstractTextEditor)part).getAction(ICEditorActionDefinitionIds.FIND_DECL);
            
            action.run();
            
        	// update the file/part to point to the newly opened IFile/IEditorPart
            int occurs= 0;
            for (int i = 0; i < 20; i++) {
            	SearchView view = (SearchView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("org.eclipse.search.ui.views.SearchView");
            	if (view != null) {
            		ISearchResult result= view.getCurrentSearchResult();
            		if (!NewSearchUI.isQueryRunning(result.getQuery())) {
            			if (result instanceof AbstractTextSearchResult) {
            				AbstractTextSearchResult ar= (AbstractTextSearchResult) result;
            				occurs= ar.getMatchCount();
            				if (occurs > 0) 
            					break;
            			}
            		}
            	}                
                runEventQueue(50);
			}
            assertEquals(numOccurrences, occurs);
        }
    }
}
