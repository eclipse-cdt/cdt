/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search.actions;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
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
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.core.search.ILineLocatable;
import org.eclipse.cdt.core.search.IMatchLocatable;
import org.eclipse.cdt.core.search.IOffsetLocatable;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * @author aniefer
 * Created on Jun 2, 2004
 */
public class SelectionParseAction extends Action {
	private static final String OPERATOR = "operator"; //$NON-NLS-1$
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
                    
                    index++;
                    
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

    protected SelSearchNode getSelectedStringFromEditor() {
        ISelection selection = getSelection();
        if( selection == null || !(selection instanceof ITextSelection) ) 
             return null;

        return getSelection( (ITextSelection)selection );
    }
    
    String projectName = "";  //$NON-NLS-1$
    protected static class Storage
    {
        private IResource resource;
        private String fileName;
		private IMatchLocatable locatable;
		
        public final String getFileName() {
            return fileName;
        }
        public Storage() {
        }
        public final IResource getResource() {
            return resource;
        }
	    public final IMatchLocatable getLocatable() {
	        return locatable;
	    }
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
        public void setLocatable(IMatchLocatable locatable) {
            this.locatable = locatable;
        }
        public void setResource(IResource resource) {
            this.resource = resource;
        }
        
    }
    
    /**
     * @param string
     * @param i
     */
    protected boolean open(String filename, IMatchLocatable locatable) throws PartInitException, CModelException {
        IPath path = new Path( filename );
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
        if( file != null )
        {
            open( file, locatable );
            return true;
        }

        ICProject cproject = CoreModel.getDefault().getCModel().getCProject( projectName );
        ITranslationUnit unit = CoreModel.getDefault().createTranslationUnitFrom(cproject, path);
        if (unit != null) {
            setSelectionAtOffset( EditorUtility.openInEditor(unit), locatable );
            return true;
        }
        
        FileStorage storage = new FileStorage(null, path);
        IEditorPart part = EditorUtility.openInEditor(storage);
        setSelectionAtOffset(part, locatable);
        return true;
        
    }
    protected Shell getShell() {
        return fEditor.getSite().getShell();
    }
    
    /**
     * Opens the editor on the given element and subsequently selects it.
     */
    protected void open( IResource resource, IMatchLocatable locatable ) throws CModelException, PartInitException {
        IEditorPart part= EditorUtility.openInEditor(resource);
        setSelectionAtOffset(part, locatable);
    }
                        
    /**
     * @param part
     * @param offset
     * @param length TODO
     */
    protected void setSelectionAtOffset(IEditorPart part, IMatchLocatable locatable) {
        if( part instanceof AbstractTextEditor )
        {
			int startOffset=0;
			int length=0;
		
			if (locatable instanceof IOffsetLocatable){
			    startOffset = ((IOffsetLocatable)locatable).getNameStartOffset();
			    length = ((IOffsetLocatable)locatable).getNameEndOffset() - startOffset;
			} else if (locatable instanceof ILineLocatable){
				int tempstartOffset = ((ILineLocatable)locatable).getStartLine();
				
				IDocument doc =  ((AbstractTextEditor) part).getDocumentProvider().getDocument(part.getEditorInput());;
				try {
					//NOTE: Subtract 1 from the passed in line number because, even though the editor is 1 based, the line
					//resolver doesn't take this into account and is still 0 based
					startOffset = doc.getLineOffset(tempstartOffset-1);
					length=doc.getLineLength(tempstartOffset-1);
				} catch (BadLocationException e) {}			
				
				//Check to see if an end offset is provided
				int tempendOffset = ((ILineLocatable)locatable).getEndLine();
				//Make sure that there is a real value for the end line
				if (tempendOffset>0 && tempendOffset>tempstartOffset){
					try {
						//See NOTE above
						int endOffset = doc.getLineOffset(tempendOffset-1);
						length=endOffset - startOffset;
					} catch (BadLocationException e) {}		
				}
					
			}
            try {
            ((AbstractTextEditor) part).selectAndReveal(startOffset, length);
            } catch (Exception e) {}
        }
    }
//  /**
//   * Shows a dialog for resolving an ambigous C element.
//   * Utility method that can be called by subclassers.
//   */
//  protected IMatch selectCElement(List elements, Shell shell, String title, String message) {
//      
//      int nResults= elements.size();
//      
//      if (nResults == 0)
//          return null;
//      
//      if (nResults == 1)
//          return (IMatch) elements.get(0);
//          
//
//      ElementListSelectionDialog dialog= new ElementListSelectionDialog(shell, new CSearchResultLabelProvider(), false, false);
//      dialog.setTitle(title);
//      dialog.setMessage(message);
//      dialog.setElements(elements);
//      
//      if (dialog.open() == Window.OK) {
//          Object[] selection= dialog.getResult();
//          if (selection != null && selection.length > 0) {
//              nResults= selection.length;
//              for (int i= 0; i < nResults; i++) {
//                  Object current= selection[i];
//                  if (current instanceof IMatch)
//                      return (IMatch) current;
//              }
//          }
//      }       
//      return null;
//  }   
    

    /* (non-Javadoc)
      * @see org.eclipse.ui.texteditor.IUpdate#update()
      */
     public void update() {
             setEnabled(getSelectedStringFromEditor() != null);
     }
	     
}
