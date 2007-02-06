/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Display;

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

				IPDOM pdom = CCorePlugin.getPDOMManager().getPDOM(workingCopy.getCProject());
				pdom.acquireReadLock();
				try {
					IASTTranslationUnit ast = workingCopy.getLanguage().getASTTranslationUnit(workingCopy, ILanguage.AST_USE_INDEX);
					IASTName[] selectedNames = workingCopy.getLanguage().getSelectedNames(ast, selectionStart, selectionLength);

					if (selectedNames.length > 0 && selectedNames[0] != null) { // got a name
						IASTName searchName = selectedNames[0];
						IBinding binding = searchName.resolveBinding();
						IASTName[] declNames = null;
						if (binding != null && !(binding instanceof ProblemBinding)) {
							boolean isDefinition = searchName.isDefinition();
							declNames = isDefinition ? ast.getDeclarations(binding) : ast.getDefinitions(binding);
							if (declNames.length == 0) // try the other way
								declNames = isDefinition ? ast.getDefinitions(binding) : ast.getDeclarations(binding);
						}
						
						if (declNames == null || declNames.length == 0) { // try the pdom
							IBinding[] bindings = pdom.findBindings(GPPLanguage.createSearchPattern(searchName), monitor);
							for (int i = 0; i < bindings.length; ++i) {
								declNames = ((PDOM)pdom).getDefinitions(bindings[i]);
								if (declNames.length > 0)
									break;
							}
							if (declNames == null || declNames.length == 0) // try the decls
								for (int i = 0; i < bindings.length; ++i) {
									declNames = ((PDOM)pdom).getDeclarations(bindings[i]);
									if (declNames.length > 0)
										break;
								}
						}

						if (declNames != null && declNames.length > 0) { // got one
					    	IASTFileLocation fileloc = declNames[0].getFileLocation();
					    	if (fileloc != null) {
								final IPath path = new Path(fileloc.getFileName());
						    	final int offset = fileloc.getNodeOffset();
						    	final int length = fileloc.getNodeLength();

								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										try {
											open(path, offset, length);
										} catch (CoreException e) {
											CUIPlugin.getDefault().log(e);
										}
									};
								});
							}
						}
					}
				} finally {
					pdom.releaseReadLock();
				}
					
				return Status.OK_STATUS;
			} catch (InterruptedException e) {
				return Status.CANCEL_STATUS;
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
}

