/**********************************************************************
 * Copyright (c) 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * @author dsteffle
 */
public class ShowInDOMViewAction extends ActionDelegate implements
		IEditorActionDelegate {

	private static final String FIND_NODE_IN_AST_DOM_VIEW = "Find Node in AST DOM View"; //$NON-NLS-1$
	CEditor editor = null;
	IASTTranslationUnit tu = null;
	IViewPart view = null;
	String file = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof CEditor)
			editor = (CEditor)targetEditor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		TextSelection selection = null;
		
		if (editor != null &&
				editor.getSelectionProvider().getSelection() instanceof TextSelection) {
			selection = (TextSelection)editor.getSelectionProvider().getSelection();
		}
		if (selection != null) {
			if (!isFileInView()) {
				view = DOMAST.openDOMASTViewRunAction(editor, new FindDisplayNode(selection.getOffset(), selection.getLength()), FIND_NODE_IN_AST_DOM_VIEW);				
			} else {
				new FindDisplayNode(selection.getOffset(), selection.getLength()).run();
			}
		}
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(CTestPlugin.getStandardDisplay().getActiveShell(), DOMAST.VIEW_NAME, message);
	}
	
	private boolean isFileInView() {
		if( editor.getInputFile() != null )
			file = editor.getInputFile().getLocation().toOSString();
		else
		{
			if( editor.getEditorInput() instanceof ExternalEditorInput )
				file = ((ExternalEditorInput)editor.getEditorInput()).getStorage().getFullPath().toOSString();
		}
		
		if (file == null) return false;

		try {
			view = editor.getSite().getPage().showView(OpenDOMViewAction.VIEW_ID);
		} catch (PartInitException pie) {}
		
		if (view instanceof DOMAST) {
			IContentProvider provider = ((DOMAST)view).getContentProvider();
			if (provider != null && provider instanceof DOMAST.ViewContentProvider) {
				tu = ((DOMAST.ViewContentProvider)provider).getTU();
				
				if (tu != null) {
					String fileName = null;
					
					// check if file is tu
					IASTNodeLocation[] locs = tu.getNodeLocations();
					for (int i=0; i<locs.length; i++) {
						if (locs[i] instanceof IASTFileLocation) {
							fileName = ((IASTFileLocation)locs[i]).getFileName();
						}
					}
					
					if (fileName != null && fileName.equals(file) ) {
						return true;
					}
					
					// check the #includes on the TU (i.e. check if selecting something from a header file shown in DOM View)
					IASTPreprocessorIncludeStatement[] includes = tu.getIncludeDirectives();
					for(int i=0; i<includes.length; i++) {
						if (includes[i].getPath().equals(file)) {
							return true;
						}
					}
				}
			}
		}

		
		return false;
	}

	private class FindDisplayNode implements Runnable {
		private static final String IAST_NODE_NOT_FOUND = "IASTNode not found for the selection.  Try refreshing the AST View if changes were made to the source."; //$NON-NLS-1$
		private static final String IASTNode_NOT_FOUND = IAST_NODE_NOT_FOUND;
		int offset = 0;
		int length = 0;
		
		public FindDisplayNode(int offset, int length) {
			this.offset = offset;
			this.length = length;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.Action#run()
		 */
		public void run() {
			if (view instanceof DOMAST) {
				IContentProvider provider = ((DOMAST)view).getContentProvider();
				if (provider != null && provider instanceof DOMAST.ViewContentProvider) {
					tu = ((DOMAST.ViewContentProvider)provider).getTU();
					if( editor.getInputFile() != null )
						file = editor.getInputFile().getLocation().toOSString();
					else
					{
						if( editor.getEditorInput() instanceof ExternalEditorInput )
							file = ((ExternalEditorInput)editor.getEditorInput()).getStorage().getFullPath().toOSString();
					}
				}
			}
			
			// the selection is within a file currently shown in the DOM AST View
			if (tu != null && file != null && view instanceof DOMAST) {
				IASTNode node = tu.selectNodeForLocation(file, offset, length);
				if (node != null && ((DOMAST)view).getContentProvider() instanceof DOMAST.ViewContentProvider) {
					((DOMAST.ViewContentProvider)((DOMAST)view).getContentProvider()).findAndSelect(node, true); // use offsets when searching for node equality
				} else {
					showMessage(IASTNode_NOT_FOUND);
				}
			}
		}
	}
	
}
