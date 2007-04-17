/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;

public class OpenDeclarationsAction extends SelectionParseAction {
	public static final IASTName[] BLANK_NAME_ARRAY = new IASTName[0];
	ITextSelection selNode;

	/**
	 * Creates a new action with the given editor
	 */
	public OpenDeclarationsAction(CEditor editor) {
		super( editor );
		setText(CEditorMessages.getString("OpenDeclarations.label")); //$NON-NLS-1$
		setToolTipText(CEditorMessages.getString("OpenDeclarations.tooltip")); //$NON-NLS-1$
		setDescription(CEditorMessages.getString("OpenDeclarations.description")); //$NON-NLS-1$
	}

	private class Runner extends Job {
		Runner() {
			super(CEditorMessages.getString("OpenDeclarations.dialog.title")); //$NON-NLS-1$
		}

		protected IStatus run(IProgressMonitor monitor) {
			try {
				int selectionStart = selNode.getOffset();
				int selectionLength = selNode.getLength();
					
				IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
				if (workingCopy == null)
					return Status.CANCEL_STATUS;

				IIndex index = CCorePlugin.getIndexManager().getIndex(workingCopy.getCProject(),
						IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);
				
				try {
					index.acquireReadLock();
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}
				
				try {
					IASTTranslationUnit ast = workingCopy.getAST(index, ITranslationUnit.AST_SKIP_ALL_HEADERS);
					IASTName[] selectedNames = workingCopy.getLanguage().getSelectedNames(ast, selectionStart, selectionLength);
					
					if (selectedNames.length > 0 && selectedNames[0] != null) { // just right, only one name selected
						IASTName searchName = selectedNames[0];
						boolean isDefinition= searchName.isDefinition();
						IBinding binding = searchName.resolveBinding();
						if (binding != null && !(binding instanceof IProblemBinding)) {
							// 1. Try definition
							IName[] declNames= isDefinition ?
									findDeclarations(index, ast, binding) :
									findDefinitions(index, ast, binding);
							
							if (declNames.length == 0) {
								declNames= isDefinition ?
										findDefinitions(index, ast, binding) :
										findDeclarations(index, ast, binding);
							}

							for (int i = 0; i < declNames.length; i++) {
								IASTFileLocation fileloc = declNames[i].getFileLocation();
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
					} else {
						// Check if we're in an include statement
						IASTPreprocessorStatement[] preprocs = ast.getAllPreprocessorStatements();
						for (int i = 0; i < preprocs.length; ++i) {
							if (!(preprocs[i] instanceof IASTPreprocessorIncludeStatement))
								continue;
							IASTFileLocation loc = preprocs[i].getFileLocation();
							if (loc != null
									&& loc.getFileName().equals(ast.getFilePath())
									&& loc.getNodeOffset() < selectionStart
									&& loc.getNodeOffset() + loc.getNodeLength() > selectionStart) {
								// Got it
								String name = ((IASTPreprocessorIncludeStatement)preprocs[i]).getPath();
								if (name != null) {
									final IPath path = new Path(name);
									runInUIThread(new Runnable() {
										public void run() {
											try {
												open(path, 0, 0);
											} catch (CoreException e) {
												CUIPlugin.getDefault().log(e);
											}
										}
									});
								}
								break;
							}
						}
					}
				} finally {
					index.releaseReadLock();
				}

				return Status.OK_STATUS;
			} catch (CoreException e) {
				return e.getStatus();
			}
		}

		private IName[] findDefinitions(IIndex index, IASTTranslationUnit ast,
				IBinding binding) throws CoreException {
			IName[] declNames= ast.getDefinitionsInAST(binding);
			if (declNames.length == 0) {
					// 2. Try definition in index
				declNames = index.findDefinitions(binding);
			}
			return declNames;
		}

		private IName[] findDeclarations(IIndex index, IASTTranslationUnit ast,
				IBinding binding) throws CoreException {
			IName[] declNames= ast.getDeclarationsInAST(binding);
			for (int i = 0; i < declNames.length; i++) {
				IName name = declNames[i];
				if (name.isDefinition()) 
					declNames[i]= null;
			}
			declNames= (IName[]) ArrayUtil.removeNulls(IName.class, declNames);
			if (declNames.length == 0) {
				declNames= index.findNames(binding, IIndex.FIND_DECLARATIONS);
			}
			return declNames;
		}
	}

	public void run() {
		selNode = getSelectedStringFromEditor();
		if (selNode != null) {
			new Runner().schedule();
		}
	}

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

