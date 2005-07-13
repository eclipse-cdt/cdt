/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.selectiontests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorOperation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.index.IIndexDelta;
import org.eclipse.cdt.core.index.IndexChangeEvent;
import org.eclipse.cdt.core.search.DOMSearchUtil;
import org.eclipse.cdt.core.testplugin.FileManager;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.ui.editor.ICEditorActionDefinitionIds;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search2.internal.ui.SearchView;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import junit.framework.TestCase;

/**
 * Base test class for testing Ctrl_F3/F3 with the indexers.
 *  
 * @author dsteffle
 */
public class BaseSelectionTestsIndexer extends TestCase {
	public static final int TIMEOUT = 50;
	protected boolean fileIndexed;
	protected IProject project;
	static FileManager fileManager = new FileManager();
	IProgressMonitor monitor = new NullProgressMonitor();
	
	public BaseSelectionTestsIndexer(String name) {
		super(name);
	}
	
	public void waitForIndex(int maxSec) throws Exception {
		int delay = 0;
		while (fileIndexed != true && delay < (maxSec * 1000))
		{ 
			Thread.sleep(TIMEOUT);
			delay += TIMEOUT;
		}
	}
	
	public void indexChanged(IndexChangeEvent event) {
		IIndexDelta delta = event.getDelta();
		if (delta.getDeltaType() == IIndexDelta.MERGE_DELTA){
			fileIndexed = true;
		}
	}
	
	protected String getMessage(IStatus status) {
		StringBuffer message = new StringBuffer("["); //$NON-NLS-1$
		message.append(status.getMessage());
		if (status.isMultiStatus()) {
			IStatus children[] = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				message.append(getMessage(children[i]));
			}
		}
		message.append("]"); //$NON-NLS-1$
		return message.toString();
	}

    protected IFile importFile(String fileName, String contents ) throws Exception{
    	resetIndexState();
    	
        //Obtain file handle
        IFile file = project.getProject().getFile(fileName);
        
        InputStream stream = new ByteArrayInputStream( contents.getBytes() ); 
        //Create file input stream
        if( file.exists() )
            file.setContents( stream, false, false, monitor );
        else
            file.create( stream, false, monitor );
        
        fileManager.addFile(file);
        
        waitForIndex(20); // only wait 20 seconds max.
        
        return file;
    }

    protected IFolder importFolder(String folderName) throws Exception {
    	IFolder folder = project.getProject().getFolder(folderName);
		
		//Create file input stream
		if( !folder.exists() )
			folder.create( false, false, monitor );
		
		return folder;
    }
    
	public void resetIndexState() {
		fileIndexed = false;
	}
	
	protected IASTNode testF3(IFile file, int offset) throws ParserException {
		return testF3(file, offset, 0);
	}
	
    protected IASTNode testF3(IFile file, int offset, int length) throws ParserException {
		if (offset < 0)
			throw new ParserException("offset can not be less than 0 and was " + offset); //$NON-NLS-1$
		
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart part = null;
        try {
            part = page.openEditor(new FileEditorInput(file), "org.eclipse.cdt.ui.editor.CEditor"); //$NON-NLS-1$
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
            ISelection sel = ((AbstractTextEditor)part).getSelectionProvider().getSelection();
            
            if (sel instanceof TextSelection) {
                IASTName[] names = DOMSearchUtil.getSelectedNamesFrom(file, ((TextSelection)sel).getOffset(), ((TextSelection)sel).getLength());
                
                if (names == null || names.length == 0)
                    return null;

				return names[0];
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
            part = page.openEditor(new FileEditorInput(file), "org.eclipse.cdt.ui.editor.CEditor"); //$NON-NLS-1$
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
    
	protected IASTNode testCtrl_F3(IFile file, int offset) throws ParserException {
		return testCtrl_F3(file, offset, 0);
	}
	
    protected IASTNode testCtrl_F3(IFile file, int offset, int length) throws ParserException {
		if (offset < 0)
			throw new ParserException("offset can not be less than 0 and was " + offset); //$NON-NLS-1$
		
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart part = null;
        try {
            part = page.openEditor(new FileEditorInput(file), "org.eclipse.cdt.ui.editor.CEditor"); //$NON-NLS-1$
        } catch (PartInitException e) {
            assertFalse(true);
        }
        
        if (part instanceof AbstractTextEditor) {
            ((AbstractTextEditor)part).getSelectionProvider().setSelection(new TextSelection(offset,length));
            
            final IAction action = ((AbstractTextEditor)part).getAction("OpenDefinition"); //$NON-NLS-1$
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
            ISelection sel = ((AbstractTextEditor)part).getSelectionProvider().getSelection();
            
            if (sel instanceof TextSelection) {
                IASTName[] names = DOMSearchUtil.getSelectedNamesFrom(file, ((TextSelection)sel).getOffset(), ((TextSelection)sel).getLength());
                
                if (names == null || names.length == 0)
                    return null;

				return names[0];
            }
        }
        
        return null;
    }
    
    protected ISelection testCtrl_F3Selection(IFile file, int offset) throws ParserException {
    	return testCtrl_F3Selection(file, offset, 0);
    }
    
    protected ISelection testCtrl_F3Selection(IFile file, int offset, int length) throws ParserException {
		if (offset < 0)
			throw new ParserException("offset can not be less than 0 and was " + offset); //$NON-NLS-1$
		
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart part = null;
        try {
            part = page.openEditor(new FileEditorInput(file), "org.eclipse.cdt.ui.editor.CEditor"); //$NON-NLS-1$
        } catch (PartInitException e) {
            assertFalse(true);
        }
        
        if (part instanceof AbstractTextEditor) {
            ((AbstractTextEditor)part).getSelectionProvider().setSelection(new TextSelection(offset,length));
            
            final IAction action = ((AbstractTextEditor)part).getAction("OpenDefinition"); //$NON-NLS-1$
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
            part = page.openEditor(new FileEditorInput(file), "org.eclipse.cdt.ui.editor.CEditor"); //$NON-NLS-1$
        } catch (PartInitException e) {
            assertFalse(true);
        }
        
        if (part instanceof AbstractTextEditor) {
            ((AbstractTextEditor)part).getSelectionProvider().setSelection(new TextSelection(offset,length));
            
            final IAction action = ((AbstractTextEditor)part).getAction(ICEditorActionDefinitionIds.FIND_DECL);
            
            action.run();
            
        	// update the file/part to point to the newly opened IFile/IEditorPart
//            IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("org.eclipse.search.ui.views.SearchView");
            
//            String title = view.getTitle();

//            assertTrue( title.indexOf(numOccurrences + " Occurrences") >= 0 ); //$NON-NLS-1$
        }
    }
    
    public void resetIndexer(final String indexerId){
		if ( project != null) {
			ICDescriptorOperation op = new ICDescriptorOperation() {

				public void execute(ICDescriptor descriptor, IProgressMonitor monitor) throws CoreException {
						descriptor.remove(CCorePlugin.INDEXER_UNIQ_ID);
						descriptor.create(CCorePlugin.INDEXER_UNIQ_ID,indexerId);
				}
			};
			try {
				CCorePlugin.getDefault().getCDescriptorManager().runDescriptorOperation(project, op, new NullProgressMonitor());
				CCorePlugin.getDefault().getCoreModel().getIndexManager().indexerChangeNotification(project);
			} catch (CoreException e) {}
		}
    }
}
