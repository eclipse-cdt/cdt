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

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider;
import org.eclipse.cdt.core.dom.IASTServiceProvider.UnsupportedDialectException;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.search.DOMSearchUtil;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.ICSearchConstants.LimitTo;
import org.eclipse.cdt.core.search.ICSearchConstants.SearchFor;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchQuery;
import org.eclipse.cdt.internal.ui.search.CSearchUtil;
import org.eclipse.cdt.internal.ui.search.DOMQuery;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.internal.ui.util.ProblemTreeViewer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchSite;


public abstract class FindAction extends SelectionParseAction {
	public FindAction(CEditor editor){
		super( editor );
	}
	
	public FindAction(IWorkbenchSite site){
		super( site );
	}
	
    /**
     * This is used to create a DOMSearchQuery based on an IASTName, LimitTo, ICSearchScope and an ICSearchCollector if
     * one should be used.
     * 
     * This is a convenience method to setup the title of the DOMSearchQuery as the rest of the parameters are passed directly
     * to the DOMSearchQuery constructor:
     * 
     * return new DOMQuery(buffer.toString(), name, limitTo, scope, collector);
     * 
     * @param name
     * @param limitTo
     * @param scope
     * @param collector
     * @return
     */
	 public CSearchQuery createDOMSearchQueryForName( IASTName name, LimitTo limitTo, ICSearchScope scope, String searchPattern){
		 return new DOMQuery(DOMSearchUtil.getSearchPattern(name), name, limitTo, scope, searchPattern);
	 }
	 
	 
     /**
      * This is a convenience method and is the same as invoking:
      * createDOMSearchQueryForName( name, limitTo, scope, null );
      * 
      * @param name
      * @param limitTo
      * @param scope
      * @return
      */
	 public CSearchQuery createSearchQueryForName( IASTName name, LimitTo limitTo, ICSearchScope scope, String searchPattern ){
		 return createDOMSearchQueryForName( name, limitTo, scope, searchPattern );
	 }
	 
	public void run() {
		ISelection sel = getSelection();
		 
	 	if (sel instanceof IStructuredSelection) {
	 		Object obj = ((IStructuredSelection)sel).getFirstElement();
	 		
	 		// if possible, try to perform a full blown DOM query before the Index query, if this fails, then perform an Index query
	 		if (obj instanceof ISourceReference && fSite.getSelectionProvider() instanceof ProblemTreeViewer) {
		 		try {
		 			fEditor = ((ProblemTreeViewer)fSite.getSelectionProvider()).getEditor();
		 			IDocument doc = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
	 				ISourceReference ref = (ISourceReference)obj;

					TextSelection selection = new TextSelection(doc, ref.getSourceRange().getIdStartPos(), ref.getSourceRange().getIdLength());
					run(selection);
					return;
				} catch (Exception e) {
				}
	 		}
	 		
	 		run((IStructuredSelection) sel);
		} else if (sel instanceof ITextSelection) {
			run((ITextSelection) sel);
		} 
	}
	
	public void run(IStructuredSelection sel){
		Object obj = sel.getFirstElement();
	 	if( obj == null || !(obj instanceof ICElement ) ){
	 		operationNotAvailable(CSEARCH_OPERATION_OPERATION_UNAVAILABLE_MESSAGE);
	 		return;
	 	}

        clearStatusLine();

		ICElement element = (ICElement) obj;
		
		CSearchQuery job = createSearchQuery( getFullyQualifiedName(element), CSearchUtil.getSearchForFromElement(element));
		
		if (job == null)
			return;
		
		NewSearchUI.activateSearchResultView();
		
		NewSearchUI.runQueryInBackground(job);
	}
	
	 /**
	 * @param node
	 */
	 protected CSearchQuery createSearchQuery(String pattern, SearchFor searchFor) {
	 	CSearchQuery op = null;
		ICSearchScope scope = getScope();
		
		if (scope == null)
			return null;
		
		String scopeDescription = getScopeDescription();
	
		//Create a case sensitive search operation - limited by the node
		List search = new LinkedList();
		search.add(searchFor);
		
		LimitTo limitTo = getLimitTo();
		
		op = new CSearchQuery(CCorePlugin.getWorkspace(), pattern,true,search,limitTo,scope,scopeDescription);
		return op;
		
	}
	
	public void run(ITextSelection sel){
		if(sel == null) {
			return;
		}
		
		SelSearchNode selNode = getSelection( sel );
		
        IASTNode foundNode = null;
        IASTTranslationUnit tu = null;
        ParserLanguage lang = null;
        String file = null;
        
        ParseWithProgress runnable = new ParseWithProgress();
        ProgressMonitorDialog progressMonitor = new ProgressMonitorDialog(fEditor.getSite().getShell());
        try {
            progressMonitor.run(true, true, runnable);
            tu = runnable.getTu();
            file = runnable.getFile();
            lang = runnable.getLang();
        } catch (InvocationTargetException e1) {
            operationNotAvailable(CSEARCH_OPERATION_OPERATION_UNAVAILABLE_MESSAGE);
            return;
        } catch (InterruptedException e1) {
            operationNotAvailable(CSEARCH_OPERATION_OPERATION_UNAVAILABLE_MESSAGE);
            return;
        }
        		
		try{
			foundNode = tu.selectNodeForLocation(file, selNode.selStart, selNode.selEnd - selNode.selStart);
		} 
		catch (ParseError er){}
		catch (Exception ex){}
		catch ( VirtualMachineError vmErr){
			if (vmErr instanceof OutOfMemoryError){
				org.eclipse.cdt.internal.core.model.Util.log(null, "Selection Search Out Of Memory error: " + vmErr.getMessage() + " on File: " + file, ICLogConstants.CDT); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		IASTName foundName = null;
		if (foundNode instanceof IASTName) {
			foundName = (IASTName)foundNode;
		} else {
			ASTVisitor collector = null;
			if (lang == ParserLanguage.CPP) {
				collector = new DOMSearchUtil.CPPNameCollector();
			} else {
				collector = new DOMSearchUtil.CNameCollector();
			}

			if (foundNode != null) { // nothing found
				foundNode.accept( collector );
				
				List names = null;
				if (collector instanceof DOMSearchUtil.CPPNameCollector) {
					names = ((DOMSearchUtil.CPPNameCollector)collector).nameList;
				} else {
					names = ((DOMSearchUtil.CNameCollector)collector).nameList;
				}
				
				if (names.size() == 1) { // just right
					clearStatusLine();
				} else if (names.size() == 0) { // no names selected
					operationNotAvailable(CSEARCH_OPERATION_NO_NAMES_SELECTED_MESSAGE);
					return;
				}
				
				foundName = (IASTName)names.get(0);
			}
		}
		
		//Don't allow searches for labels since i)there is no corresponding ICElement
		//and thus no label provider and ii) it doesn't make sense to do a global search
		//for a local element
		if (foundName != null && foundName.resolveBinding() instanceof ILabel) {
			operationNotAvailable(CSEARCH_OPERATION_OPERATION_UNAVAILABLE_MESSAGE);
			return;
		}
		
		LimitTo limitTo = getLimitTo();
		ICSearchScope searchScope = null;
		searchScope = getScope();

		if (searchScope == null)
			return;
		
		CSearchQuery job = createSearchQueryForName(foundName, limitTo, searchScope, selNode.selText);
		
		if (job == null)
			return; 
		
		NewSearchUI.activateSearchResultView();
		
		NewSearchUI.runQueryInBackground(job);
	}
    
    private class ParseWithProgress implements IRunnableWithProgress 
    {
        private IASTTranslationUnit tu = null;
        private String file=null;
        private ParserLanguage lang=null;
        
        public ParseWithProgress() {}

        public void run(IProgressMonitor monitor) {
            if (fEditor.getEditorInput() instanceof ExternalEditorInput) {
                ExternalEditorInput input = (ExternalEditorInput)fEditor.getEditorInput();
                try {
                    // get the project for the external editor input's translation unit
                    ICElement project = input.getTranslationUnit();
                    while (!(project instanceof ICProject) && project != null) {
                        project = project.getParent();
                    }
                    
                    if (project instanceof ICProject) {
                        tu = CDOM.getInstance().getASTService().getTranslationUnit(input.getStorage(), ((ICProject)project).getProject());
                        lang = DOMSearchUtil.getLanguage(input.getStorage().getFullPath(), ((ICProject)project).getProject());
                    }
                } catch (UnsupportedDialectException e) {
                    operationNotAvailable(CSEARCH_OPERATION_OPERATION_UNAVAILABLE_MESSAGE);
                    return;
                }
                
                file = input.getStorage().getFullPath().toOSString();
            } else {
                IFile resourceFile = null;
                resourceFile = fEditor.getInputFile();
                
                try {
                    tu = CDOM.getInstance().getASTService().getTranslationUnit(
                            resourceFile,
                            CDOM.getInstance().getCodeReaderFactory(
                                    CDOM.PARSE_WORKING_COPY_WHENEVER_POSSIBLE));
                } catch (IASTServiceProvider.UnsupportedDialectException e) {
                    operationNotAvailable(CSEARCH_OPERATION_OPERATION_UNAVAILABLE_MESSAGE);
                    return;
                }
                
                file = resourceFile.getLocation().toOSString();
                lang = DOMSearchUtil.getLanguageFromFile(resourceFile);
            }
        }

        public IASTTranslationUnit getTu() {
            return tu;
        }

        public String getFile() {
            return file;
        }

        public ParserLanguage getLang() {
            return lang;
        }
    };
	
    
    private String getFullyQualifiedName(ICElement element){
      
    	StringBuffer fullName = new StringBuffer(element.getElementName());
    	
    	while (element.getElementType() != 0){
    		element = element.getParent();
    		//Keep going up CModel until we hit Translation Unit
    		//or Working Copy (both represented by C_UNIT) or hit a null
    		if (element.getElementType() == ICElement.C_UNIT ||
    			element == null){
    			fullName.insert(0,"::"); //$NON-NLS-1$
    			break;
    		}
    		else if (element.getElementType() != ICElement.C_ENUMERATION){
    			//get the parent name as long as it is not an enumeration - enumerators
    			//don't use the enumeration name as part of the fully qualified name
    			fullName.insert(0,"::"); //$NON-NLS-1$
    			fullName.insert(0,element.getElementName());
    		}
    	}
    	
    	return fullName.toString();
    }
    
    abstract protected String getScopeDescription(); 

	abstract protected ICSearchScope getScope();
	
	abstract protected LimitTo getLimitTo();
    
}
