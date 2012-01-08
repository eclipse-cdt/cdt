/**********************************************************************
 * Copyright (c) 2004, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Intel Corporation - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 **********************************************************************/

package org.eclipse.cdt.internal.ui.util;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.CWordFinder;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * @since 2.1
 */
public class CUIHelp {

	public static void setHelp(CEditor editor, StyledText text, String contextId) {
		CUIHelpListener listener= new CUIHelpListener(editor, contextId);
		text.addHelpListener(listener);
	}

	private static class CUIHelpListener implements HelpListener {

		private String fContextId;
		private CEditor fEditor;

		public CUIHelpListener(CEditor editor, String contextId) {
			fContextId= contextId;
			fEditor= editor;
		}

		/*
		* @see HelpListener#helpRequested(HelpEvent)
		* 
		*/
		@Override
		public void helpRequested(HelpEvent e) {
			try {
				CHelpDisplayContext.displayHelp(fContextId, fEditor);
			} catch (CoreException x) {
				CUIPlugin.log(x);
			}
		}
	}

	/**
	 * A dynamic help context provider.
	 * 
	 * @since 4.0
	 */
	public static final class CUIHelpContextProvider implements IContextProvider {

		private final ITextEditor fEditor;

		/**
		 * Creates a context provider for the given text editor.
		 * @param editor
		 */
		public CUIHelpContextProvider(ITextEditor editor) {
			fEditor= editor;
		}

		/*
		 * @see org.eclipse.help.IContextProvider#getContext(java.lang.Object)
		 */
		@Override
		public IContext getContext(Object target) {
			String selected = getSelectedString(fEditor);
			IContext context= HelpSystem.getContext(ICHelpContextIds.CEDITOR_VIEW);
			if (context != null) {
				if (selected != null && selected.length() > 0) {
					try {
						context= new CHelpDisplayContext(context, fEditor, selected);
					} catch (CoreException exc) {
					}
				}
			}
			return context;
		}
	
		/*
		 * @see org.eclipse.help.IContextProvider#getContextChangeMask()
		 */
		@Override
		public int getContextChangeMask() {
			return SELECTION;
		}
	
		/*
		 * @see org.eclipse.help.IContextProvider#getSearchExpression(java.lang.Object)
		 */
		@Override
		public String getSearchExpression(Object target) {
			return getSelectedString(fEditor);
		}
	
		private static String getSelectedString(ITextEditor editor){
			String expression = null;
			try{
				ITextSelection selection = (ITextSelection)editor.getSite().getSelectionProvider().getSelection();
				IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
				IRegion region = CWordFinder.findWord(document, selection.getOffset());
				if (region != null)
					expression = document.get(region.getOffset(), region.getLength());
			}
			catch(Exception e){
			}
			return expression;
		}
	
	}

}
