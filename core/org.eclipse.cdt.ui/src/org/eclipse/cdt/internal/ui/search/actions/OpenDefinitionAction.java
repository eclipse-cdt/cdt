/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;

/**
 * Open Definition Action (Ctrl+F3).
 * 
 * @author dsteffle
 */
public class OpenDefinitionAction extends SelectionParseAction {

    public static final IASTName[] BLANK_NAME_ARRAY = new IASTName[0];
	ITextSelection selNode;

    /**
     * Creates a new action with the given editor
     */
    public OpenDefinitionAction(CEditor editor) {
        super( editor );
        setText(CEditorMessages.getString("OpenDefinition.label")); //$NON-NLS-1$
        setToolTipText(CEditorMessages.getString("OpenDefinition.tooltip")); //$NON-NLS-1$
        setDescription(CEditorMessages.getString("OpenDefinition.description")); //$NON-NLS-1$
    }

	private class Runner extends Job {
		Runner() {
			super(CEditorMessages.getString("OpenDeclarations.label")); //$NON-NLS-1$
		}

		protected IStatus run(IProgressMonitor monitor) {
			try {
				int selectionStart = selNode.getOffset();
				int selectionLength = selNode.getLength();
					
				IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
				if (workingCopy == null)
					return Status.CANCEL_STATUS;
					
				IIndex index= CCorePlugin.getIndexManager().getIndex(workingCopy.getCProject(), 
						IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);
				try {
					index.acquireReadLock();
				} catch (InterruptedException e1) {
					return Status.CANCEL_STATUS;
				}
				try {
					IASTTranslationUnit ast = workingCopy.getAST(index, ITranslationUnit.AST_SKIP_ALL_HEADERS);
					IASTName[] selectedNames = workingCopy.getLanguage().getSelectedNames(ast, selectionStart, selectionLength);

					if (selectedNames.length > 0 && selectedNames[0] != null) { // just right, only one name selected
						IASTName searchName = selectedNames[0];

						IBinding binding = searchName.resolveBinding();
						if (binding != null) {
							final IName[] declNames = ast.getDefinitions(binding);
							for (int i = 0; i < declNames.length; i++) {
						    	IASTFileLocation fileloc = declNames[i].getFileLocation();
					    		// no source location - TODO spit out an error in the status bar
						    	if (fileloc != null) {
						    		final IPath path = new Path(fileloc.getFileName());
						    		final int offset = fileloc.getNodeOffset();
						    		final int length = fileloc.getNodeLength();

						    		runInUIThread(new Runnable() {
						    			public void run() {
						    				try {
						    					open(path, offset, length);
						    				} catch (CoreException e) {
						    					CUIPlugin.getDefault().log(e);
						    				}
						    			}
						    		});
						    		break;
						    	}
							}
						}
					}
				}
				finally {
					index.releaseReadLock();
				}
					
				return Status.OK_STATUS;
			} catch (CoreException e) {
				return e.getStatus();
			}
		}
	}

	public void run() {
		selNode = getSelectedStringFromEditor();
		if (selNode != null) {
			new Runner().schedule();
		}
	}
    
	/**
	 * For the purpose of regression testing.
	 * @since 4.0
	 */
	private void runInUIThread(Runnable runnable) {
		if (Display.getCurrent() != null) {
			runnable.run();
		}
		else {
			Display.getDefault().asyncExec(runnable);
		}
	}

	/**
	 * For the purpose of regression testing.
	 * @since 4.0
	 */
	public void runSync() {
		selNode = getSelectedStringFromEditor();
		if (selNode != null) {
			new Runner().run(new NullProgressMonitor());
		}
	}

}
