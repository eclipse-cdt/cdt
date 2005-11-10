/*******************************************************************************
 * Copyright (c) 2005 Todd Papaioannou
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Todd Papaioannou  - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * Join Lines Action is a relatively simple extension of TextEditorAction
 * that, when invoked, will join the current and next line together. 
 * 
 * @author Todd Papaioannou (toddp@acm.org)
 * @version $Date$
 * @see org.eclipse.ui.texteditor.TextEditorAction
 */
public class JoinLinesAction extends TextEditorAction {
	
	// This is the editor component that we will work on
	ITextEditor theEditor;
	
	/**
	 * Create a new instance of JoinLinesAction. We basically just call
	 * our parent's constructor.
	 * 
	 * @param  bundle  The resource bundle
	 * @param  prefix  A prefix to be prepended to the various resource keys
	 *                 (described in <code>ResourceAction</code> constructor), 
	 *                 or <code>null</code> if none
	 * @param  editor  The text editor component on which this Action will work.
	 */
	public JoinLinesAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
	
		super(bundle, prefix, editor);

	} // end of constructor
	

	/**
	 * Run is where all the action (sic!) happens. This is a pretty simple 
	 * action that basically joins the current line and next line together. 
	 * It achieves this by determining which line of the editor's document
	 * we are on, and then replacing the delimiter at the end of the line
	 * with nothing.
	 * 
	 * TODO: Currently, we don't bother to remove any excess whitespace. 
	 *       Doing so in the future might be a nice touch.
	 */
	public void run() {
		
		// Make sure we can proceed.
		if ((theEditor != null) && isEnabled() && canModifyEditor()) {
			
			// Retrieve the current editor's document
			IDocument theDocument = getDocument(theEditor);
			
			if (theDocument != null) {
				
				try {
					
					// First, retrieve the current selection
					ITextSelection theSelection = getSelection(theEditor);
					
					if (theSelection != null) {
					
						// Now, figure out the end line of the selection
						int currentLine = theSelection.getEndLine();
						
						// What's the offset of this line in the document?
						int lineOffset = theDocument.getLineOffset(currentLine);
						
						// And the length of the line?
						int lineLength = theDocument.getLineLength(currentLine);
						
						// What delimeter do we have?
						String delim = theDocument.getLineDelimiter(currentLine);
						
						// How long is it?
						int delimLength = delim.length();
						
						// Now back track to the last real char in the line
						int newLineEnd = lineOffset + lineLength - delimLength;
						
						// Replace the delimter char(s) with nothing
						theDocument.replace(newLineEnd, delimLength, null);
						
					}
					
				}
				catch (BadLocationException e) {
					
					e.printStackTrace();
				}
				
			} // end of if (document)

		} // end of isEnabled()

	} // end of run 
	
	
	/**
	 * Check that we can actually modify the document of the current editor.
	 */
	public void update() {
		
		super.update();
		
		// Make sure we can proceed.
		if (isEnabled() && canModifyEditor()) {
			
			// Retrieve the text editor and store it for later
			theEditor = getTextEditor();
		}
		
	} // end of update 
	
	
	/**
	 * Get Document attempts to retrieve 'editor's document. 
	 *
	 * @param  editor  The editor whose document we want to retrieve.
	 * 
	 * @return An IDocument if there is one, or null.
	 */
	private IDocument getDocument(ITextEditor editor) {

		// What we will return
		IDocument theDocument = null;
		
		// Retrieve the document provider
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		
		if (documentProvider != null) {

			// Retrieve the actual document
			theDocument = documentProvider.getDocument(editor.getEditorInput());
		}

		return theDocument;
		
	} // end of getDocument
	

	/**
	 * Get selection attempts to retrieve 'editor's current selection.
	 *
	 * @param  editor  The editor whose selection we want to retrieve.
	 * 
	 * @return An ITextSelection if there is one, or null.
	 */
	private ITextSelection getSelection(ITextEditor editor) {

		// What we will return
		ITextSelection theSelection = null;
		
		// First try to retrieve the editor's selection provider 
		ISelectionProvider selectionProvider = editor.getSelectionProvider();
		
		if (selectionProvider != null) {
			
			// Now try to retrieve the selection
			ISelection selection = selectionProvider.getSelection();
			
			// Is this of the correct type?
			if (selection instanceof ITextSelection) {
				
				// Ok, cast it and assign it
				theSelection = (ITextSelection) selection;
			}
		}

		return theSelection;
		
	} // end of getSelection
	
} // end of JoinLinesAction
