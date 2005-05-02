/*******************************************************************************
 * Copyright (c) 2004,2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.internal.ui.search.actions;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * @author aniefer
 * Created on Jun 2, 2004
 */
public class SelectionParseAction extends Action {
	private static final String OPERATOR = "operator"; //$NON-NLS-1$
    protected static final String CSEARCH_OPERATION_TOO_MANY_NAMES_MESSAGE = "CSearchOperation.tooManyNames.message"; //$NON-NLS-1$
	protected static final String CSEARCH_OPERATION_NO_NAMES_SELECTED_MESSAGE = "CSearchOperation.noNamesSelected.message"; //$NON-NLS-1$
	protected static final String CSEARCH_OPERATION_OPERATION_UNAVAILABLE_MESSAGE = "CSearchOperation.operationUnavailable.message"; //$NON-NLS-1$
	
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

	protected IParser setupParser(IFile resourceFile) {
		

		//Get the scanner info
		IProject currentProject = resourceFile.getProject();
		IScannerInfo scanInfo = new ScannerInfo();
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(currentProject);
		if (provider != null){
		  IScannerInfo buildScanInfo = provider.getScannerInformation(resourceFile);
		  if (buildScanInfo != null){
			scanInfo = new ScannerInfo(buildScanInfo.getDefinedSymbols(), buildScanInfo.getIncludePaths());
		  }
		}
		
		//C or CPP?
		ParserLanguage language = CoreModel.hasCCNature(currentProject) ? ParserLanguage.CPP : ParserLanguage.C;
		
		IWorkingCopy workingCopy = null;
		if( fEditor.isDirty() ){
			IWorkingCopy [] workingCopies = CUIPlugin.getSharedWorkingCopies();
			if( workingCopies != null ){
				for( int i = 0; i < workingCopies.length; i++ ){
					if( workingCopies[i].getUnderlyingResource().equals( resourceFile ) ){
						workingCopy = workingCopies[i];
						break;
					}
				}
			}
		}
		
		IParser parser = null;
		CodeReader reader = null;
		try {
			if( workingCopy == null )
				reader = new CodeReader(resourceFile.getLocation().toOSString(), resourceFile.getCharset() );
			else 
				reader = new CodeReader(resourceFile.getLocation().toOSString(), workingCopy.getContents());
		} catch (IOException e) {
			e.printStackTrace();
		} catch ( CoreException e ) {
            e.printStackTrace();
        }
		
		try
		{
			parser = ParserFactory.createParser( 
							ParserFactory.createScanner( reader, scanInfo, ParserMode.SELECTION_PARSE, language, new NullSourceElementRequestor(), ParserUtil.getScannerLogService(), null ), 
							new NullSourceElementRequestor(), ParserMode.SELECTION_PARSE, language, ParserUtil.getParserLogService() );
			
		} catch( ParserFactoryError pfe ){}
		
	   return parser;
	 }

	protected void operationNotAvailable(final String message) {
		// run the code to update the status line on the Display thread
		// this way any other thread can invoke operationNotAvailable(String)
		CUIPlugin.getStandardDisplay().asyncExec(new Runnable(){
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				IStatusLineManager statusManager = null;
				 if (fSite instanceof IViewSite){
				 	statusManager = ((IViewSite) fSite).getActionBars().getStatusLineManager();
				 }
				 else if (fSite instanceof IEditorSite){
				 	statusManager = ((IEditorSite) fSite).getActionBars().getStatusLineManager();
				 }	
				 if( statusManager != null )
				 	statusManager.setErrorMessage(CSearchMessages.getString(message));//$NON-NLS-1$
			}
		});
	}
	protected void clearStatusLine() {
		// run the code to update the status line on the Display thread
		// this way any other thread can invoke clearStatusLine()
		CUIPlugin.getStandardDisplay().asyncExec(new Runnable(){
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				IStatusLineManager statusManager = null;
				 if (fSite instanceof IViewSite){
				 	statusManager = ((IViewSite) fSite).getActionBars().getStatusLineManager();
				 }
				 else if (fSite instanceof IEditorSite){
				 	statusManager = ((IEditorSite) fSite).getActionBars().getStatusLineManager();
				 }	
				 if( statusManager != null )
				 	statusManager.setErrorMessage( "" ); //$NON-NLS-1$
			}
		});
	}

	//TODO: Change this to work with qualified identifiers
	public SelSearchNode getSelection( int fPos ) {
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
        
        SelSearchNode sel = new SelSearchNode();

        boolean selectedOperator=false;
        if (selectedWord != null && selectedWord.indexOf(OPERATOR) >= 0 && fPos >= fStartPos + selectedWord.indexOf(OPERATOR) && fPos < fStartPos + selectedWord.indexOf(OPERATOR) + OPERATOR.length()) {
            selectedOperator=true;
        }
    
        // if the operator was selected, get its proper bounds
        if (selectedOperator && fEditor.getEditorInput() instanceof IFileEditorInput &&  
                CoreModel.hasCCNature(((IFileEditorInput)fEditor.getEditorInput()).getFile().getProject())) {
            int actualStart=fStartPos + selectedWord.indexOf(OPERATOR);
            int actualEnd=getOperatorActualEnd(doc, fStartPos + selectedWord.indexOf(OPERATOR) + OPERATOR.length());
            
            actualEnd=(actualEnd>0?actualEnd:fEndPos);
            
            try {
                sel.selText = doc.get(actualStart, actualEnd - actualStart);
            } catch (BadLocationException e) {}
            sel.selStart = actualStart;
            sel.selEnd = actualEnd;
        // TODO Devin this only works for definitions of destructors right now
        // if there is a destructor and the cursor is in the destructor name's segment then get the entire destructor
        } else if (selectedWord != null && selectedWord.indexOf('~') >= 0 && fPos - 2 >= fStartPos + selectedWord.lastIndexOf(new String(Keywords.cpCOLONCOLON))) {
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
                try {
                    sel.selText = doc.get(nonJavaStart, (nonJavaEnd - nonJavaStart));
                } catch (BadLocationException e) {}
                sel.selStart = nonJavaStart;
                sel.selEnd = nonJavaEnd;
            } else {
                try {
                    sel.selText = doc.get(actualStart, length);
                } catch (BadLocationException e) {}
                sel.selStart = actualStart;
                sel.selEnd = actualStart + length;                
            }
        } else {
            // otherwise use the non-java identifier parts as boundaries for the selection
            try {
                sel.selText = doc.get(nonJavaStart, (nonJavaEnd - nonJavaStart));
            } catch (BadLocationException e) {}
            sel.selStart = nonJavaStart;
            sel.selEnd = nonJavaEnd;
        }
    
        return sel;     
	}
    
    private int getOperatorActualEnd(IDocument doc, int index) {
        char c1, c2;
        int actualEnd=-1;
        boolean multiComment=false;
        boolean singleComment=false;
        int possibleEnd=-1;
        while (actualEnd==-1) {
            try {
                c1=doc.getChar(index);
                c2=doc.getChar(index+1);
                
                // skip anything within a single-line comment
                if (singleComment) {
                    char c3=doc.getChar(index-1);
                    if (c3 != '\\' && (c1 == '\n' || c1 == '\r' && c2 == '\n' )) {
                        singleComment=false;
                    }
                    index++;
                    continue;
                }
                // skip anything within a multi-line comment
                if (multiComment) {
                    if (c1 == '*' && c2 == '/') {
                        multiComment=false;
                        index+=2;
                        continue;
                    }
                    index++;
                    continue;
                }
                
                switch(c1) {
                case '+': {
                    switch(c2) {
                    case '=':
                    case '+':
                        actualEnd=index+2;
                        break;
                    default:
                        actualEnd=index+1;
                        break;
                    }
                    break;
                }
                case '-': {
                    switch(c2) {
                    case '=':
                        actualEnd=index+2;
                        break;
                    case '-':
                        switch(doc.getChar(index+2)) {
                        case '>': {
                            switch(doc.getChar(index+3)) {
                            case '*':
                                actualEnd=index+4;
                                break;
                            default:
                                actualEnd=index+3;
                                break;
                            }
                            break;
                        }
                        default:
                            actualEnd=index+2;
                            break;                                
                        }
                        break;
                    default:
                        
                        break;
                    }
                    break;
                }
                case '|': {
                    switch(c2) {
                    case '=':
                    case '|':
                        actualEnd=index+2;
                        break;
                    default:
                        actualEnd=index+1;
                        break;
                    }
                    break;                  
                }
                case '&': {
                    switch(c2) {
                    case '=':
                    case '&':
                        actualEnd=index+2;
                        break;
                    default:
                        actualEnd=index+1;
                        break;
                    }
                    break;
                }
                case '/': {
                    switch(c2) {
                    case '/':
                        singleComment=true;
                        index+=2;
                        break;
                    case '*':
                        multiComment=true;
                        index+=2;
                        break;
                    case '=':
                        actualEnd=index+2;
                        break;
                    default:
                        actualEnd=index+1;
                        break;
                    }
                    break;
                }
                case '*':
                case '%': 
                case '^': 
                case '!': 
                case '=': {
                    switch(c2) {
                    case '=':
                        actualEnd=index+2;
                        break;
                    default:
                        actualEnd=index+1;
                        break;
                    }
                    break;
                }
                case '(': {
                    if (possibleEnd > 0)
                        actualEnd = possibleEnd;
                    break;
                }   
                case ']':
                case ')':
                case ',':
                case '~': {
                    actualEnd=index+1;
                    break;
                }
                case '<': {
                    switch(c2) {
                    case '=':
                    case '<':
                        switch(doc.getChar(index+2)) {
                        case '=':
                            actualEnd=index+3;
                            break;
                        default:
                            actualEnd=index+2;
                            break;
                        }
                        break;
                    default:
                        actualEnd=index;
                        break;
                    }
                    break;                  
                }
                case '>': {
                    switch(c2) {
                    case '=':
                    case '>':
                        switch(doc.getChar(index+2)) {
                        case '=':
                            actualEnd=index+3;
                            break;
                        default:
                            actualEnd=index+2;
                            break;
                        }
                        break;
                    default:
                        actualEnd=index;
                        break;
                    }
                    break;  
                }
                case 'n': { // start of "new"
                    while (doc.getChar(++index) != 'w');
                    possibleEnd=++index;
                    break;
                }
                case 'd': { // start of "delete"
                    while (doc.getChar(++index) != 't' && doc.getChar(index+1) != 'e');
                    index+=2;
                    possibleEnd=index;
                    break;
                }
                default:
                    index++;
                    break;
                }
            } catch (BadLocationException e) {
                // something went wrong
                return -1;
            }
        }
        
        return actualEnd;
    }
    
	/**
	  * Return the selected string from the editor
	  * @return The string currently selected, or null if there is no valid selection
	  */
	protected SelSearchNode getSelection( ITextSelection textSelection ) {
		if( textSelection == null )
			return null;
		
		 String seltext = textSelection.getText();
		 SelSearchNode sel = null;
		 if ( seltext == null || seltext.length() == 0 ) {
	 		 int selStart =  textSelection.getOffset();
	 		 sel = getSelection(selStart);
		 } else {
		 	sel = new SelSearchNode();
		 	sel.selText= seltext;
		 	sel.selStart = textSelection.getOffset();
		 	sel.selEnd = textSelection.getOffset() + textSelection.getLength();
		 }
		 return sel;
	}
	
	protected ISelection getSelection() {
		ISelection sel = null;
		if (fSite != null && fSite.getSelectionProvider() != null ){
			sel = fSite.getSelectionProvider().getSelection();
		}
		
		return sel;
	}
	
	class SelSearchNode{
	 	protected String selText;
	 	protected int selStart;
	 	protected int selEnd;
	}

}
