/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search.actions;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.parser.Keywords;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.StatusLineHandler;

/**
 * @author aniefer
 * Created on Jun 2, 2004
 */
public class SelectionParseAction extends Action {
	protected static final String CSEARCH_OPERATION_NO_NAMES_SELECTED_MESSAGE = "CSearchOperation.noNamesSelected.message"; //$NON-NLS-1$
	protected static final String CSEARCH_OPERATION_OPERATION_UNAVAILABLE_MESSAGE = "CSearchOperation.operationUnavailable.message"; //$NON-NLS-1$
    protected static final String CSEARCH_OPERATION_NO_DEFINITION_MESSAGE = "CSearchOperation.noDefinitionFound.message"; //$NON-NLS-1$
    protected static final String CSEARCH_OPERATION_NO_DECLARATION_MESSAGE = "CSearchOperation.noDeclarationFound.message"; //$NON-NLS-1$
        
	protected IWorkbenchSite fSite;
	protected CEditor fEditor;

	public SelectionParseAction() {
		super();
	}
	
	public SelectionParseAction( CEditor editor ) {
		super();
		fEditor=editor;
		fSite=editor.getSite();
	}
	
	public SelectionParseAction(IWorkbenchSite site){
		super();
		fSite=site;
	}

	public IWorkbenchSite getSite() {
		return fSite;
	}
	
	protected void showStatusLineMessage(final String message) {
		StatusLineHandler.showStatusLineMessage(fSite, message);
	}
	protected void clearStatusLine() {
		StatusLineHandler.clearStatusLine(fSite);
	}

	//TODO: Change this to work with qualified identifiers
	public ITextSelection getSelection( int fPos ) {
 		IDocumentProvider prov = ( fEditor != null ) ? fEditor.getDocumentProvider() : null;
 		IDocument doc = ( prov != null ) ? prov.getDocument(fEditor.getEditorInput()) : null;
 		
 		if( doc == null )
 			return null;
 		 
		int pos= fPos;
		char c;
		int fStartPos =0, fEndPos=0;
        int nonJavaStart=-1, nonJavaEnd=-1;
		String selectedWord=null;
		
		try{
			while (pos >= 0) {
				c= doc.getChar(pos);

                // TODO this logic needs to be improved
                // ex: ~destr[cursor]uctors, p2->ope[cursor]rator=(zero), etc
                if (nonJavaStart == -1 && !Character.isJavaIdentifierPart(c)) {
                    nonJavaStart=pos+1;
                }
                    
                if (Character.isWhitespace(c))
                    break;

				--pos;
			}
			fStartPos= pos + 1;
			
			pos= fPos;
			int length= doc.getLength();
			while (pos < length) {
				c= doc.getChar(pos);

                if (nonJavaEnd == -1 && !Character.isJavaIdentifierPart(c)) {
                    nonJavaEnd=pos;
                }
                if (Character.isWhitespace(c))
                    break;
				++pos;
			}
			fEndPos= pos;
			selectedWord = doc.get(fStartPos, (fEndPos - fStartPos));
        }
        catch(BadLocationException e){
        }
        
        // TODO Devin this only works for definitions of destructors right now
        // if there is a destructor and the cursor is in the destructor name's segment then get the entire destructor
        if (selectedWord != null && selectedWord.indexOf('~') >= 0 && fPos - 2 >= fStartPos + selectedWord.lastIndexOf(new String(Keywords.cpCOLONCOLON))) {
            int tildePos = selectedWord.indexOf('~');
            int actualStart=fStartPos + tildePos;
            int length=0;
            char temp;
            char[] lastSegment = selectedWord.substring(tildePos).toCharArray();
            for(int i=1; i<lastSegment.length; i++) {
                temp = lastSegment[i];
                if (!Character.isJavaIdentifierPart(temp)) {
                    length=i;
                    break;
                }
            }
            
            // if the cursor is after the destructor name then use the regular boundaries 
            if (fPos >= actualStart + length) {
            	return new TextSelection(doc, nonJavaStart, length);
            } else {
            	return new TextSelection(doc, actualStart, length);
            }
        } else {
            // otherwise use the non-java identifier parts as boundaries for the selection
        	return new TextSelection(doc, nonJavaStart, nonJavaEnd - nonJavaStart);
        }
	}
    
	/**
	  * Return the selected string from the editor
	  * @return The string currently selected, or null if there is no valid selection
	  */
	protected ITextSelection getSelection( ITextSelection textSelection ) {
		if( textSelection == null )
			return null;
		
		 if (textSelection.getLength() == 0) {
	 		 return getSelection(textSelection.getOffset());
		 } else {
			 return textSelection;
		 }
	}
	
	protected ISelection getSelection() {
		ISelection sel = null;
		if (fSite != null && fSite.getSelectionProvider() != null ){
			sel = fSite.getSelectionProvider().getSelection();
		}
		
		return sel;
	}
	
    protected ITextSelection getSelectedStringFromEditor() {
        ISelection selection = getSelection();
        if( selection == null || !(selection instanceof ITextSelection) ) 
             return null;

        return getSelection( (ITextSelection)selection );
    }
    
    /**
     * Open the editor on the given name.
     * 
     * @param name
     */
    protected void open(IName name) throws CoreException {
		clearStatusLine();

    	IASTFileLocation fileloc = name.getFileLocation();
    	if (fileloc == null) {
    		reportSymbolLookupFailure(new String(name.toCharArray()));
    		return;
    	}
    	
		IPath path = new Path(fileloc.getFileName());
    	int currentOffset = fileloc.getNodeOffset();
    	int currentLength = fileloc.getNodeLength();
    	
		open(path, currentOffset, currentLength);
    }

	protected void open(IPath path, int currentOffset, int currentLength) throws CoreException {
		clearStatusLine();

		IEditorPart editor = EditorUtility.openInEditor(path, fEditor.getInputCElement());
		if (editor instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor)editor;
			textEditor.selectAndReveal(currentOffset, currentLength);
		} else {
			reportSourceFileOpenFailure(path);
		}
	}
    
    public void update() {
		setEnabled(getSelectedStringFromEditor() != null);
	}

    protected void reportSourceFileOpenFailure(IPath path) {
    	showStatusLineMessage(MessageFormat.format(
    			CSearchMessages.getString("SelectionParseAction.FileOpenFailure.format"), //$NON-NLS-1$
    			new String[] { path.toOSString() }));
    }
    
    protected void reportSelectionMatchFailure() {
    	showStatusLineMessage(CSearchMessages.getString("SelectionParseAction.SelectedTextNotSymbol.message")); //$NON-NLS-1$
    }
    
    protected void reportSymbolLookupFailure(String symbol) {
    	showStatusLineMessage(MessageFormat.format(
    			CSearchMessages.getString("SelectionParseAction.SymbolNotFoundInIndex.format"), //$NON-NLS-1$
    			new String[] { symbol }));
    }
    
    protected void reportIncludeLookupFailure(String filename) {
    	showStatusLineMessage(MessageFormat.format(
    			CSearchMessages.getString("SelectionParseAction.IncludeNotFound.format"), //$NON-NLS-1$
    			new String[] { filename }));
    }

}
