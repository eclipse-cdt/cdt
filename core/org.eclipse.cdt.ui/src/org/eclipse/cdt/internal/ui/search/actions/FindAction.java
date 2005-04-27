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

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICExternalBinding;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.search.DOMSearchUtil;
import org.eclipse.cdt.core.search.ICSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.ICSearchConstants.LimitTo;
import org.eclipse.cdt.core.search.ICSearchConstants.SearchFor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchQuery;
import org.eclipse.cdt.internal.ui.search.CSearchResultCollector;
import org.eclipse.cdt.internal.ui.search.CSearchUtil;
import org.eclipse.cdt.internal.ui.search.DOMQuery;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.ITextSelection;
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
	 public static CSearchQuery createDOMSearchQueryForName( IASTName name, LimitTo limitTo, ICSearchScope scope, ICSearchResultCollector collector ){
		 	StringBuffer buffer = new StringBuffer();
			buffer.append("::"); //$NON-NLS-1$
			if (name instanceof CPPASTName && name.getParent() instanceof CPPASTQualifiedName) {
				IASTName[] names = ((CPPASTQualifiedName)name.getParent()).getNames();
				for(int i=0; i<names.length; i++) {
					if (i != 0) buffer.append("::"); //$NON-NLS-1$
					buffer.append(names[i].toString());
				}
			} else {
				buffer.append(name.toString());
			}
			
		 	if( name.resolveBinding() instanceof IFunction ){
				try {
					IBinding binding = name.resolveBinding();
					IFunctionType type = ((IFunction)binding).getType();
					
					buffer.append("("); //$NON-NLS-1$
					if (binding instanceof ICExternalBinding) {
						buffer.append("..."); //$NON-NLS-1$
					} else {
						IType[] parms = type.getParameterTypes();
						for( int i = 0; i < parms.length; i++ ){
							if( i != 0 )
								buffer.append(", "); //$NON-NLS-1$
							buffer.append(ASTTypeUtil.getType(parms[i]));
						}
					}
					buffer.append(")"); //$NON-NLS-1$
				} catch (DOMException e) {
					buffer = new StringBuffer();
					buffer.append(name.toString());
				}
		 	}
		 	
			return new DOMQuery(buffer.toString(), name, limitTo, scope, collector);
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
	 public static CSearchQuery createSearchQueryForName( IASTName name, LimitTo limitTo, ICSearchScope scope ){
		 return createDOMSearchQueryForName( name, limitTo, scope, null );
	 }
	 
	public void run() {
		ISelection sel = getSelection();
		 
	 	if (sel instanceof IStructuredSelection) {
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
	 	} else {
	 		clearStatusLine();
	 	}
		ICElement element = (ICElement) obj;
		
		CSearchQuery job = createSearchQuery( element.getElementName(), CSearchUtil.getSearchForFromElement(element));
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
		
		CSearchResultCollector collector= new CSearchResultCollector();
		
		LimitTo limitTo = getLimitTo();
		
		op = new CSearchQuery(CCorePlugin.getWorkspace(), pattern,true,search,limitTo,scope,scopeDescription,collector);
		return op;
		
	}
	
	public void run(ITextSelection sel){
		if(sel == null) {
			return;
		}
		
		SelSearchNode selNode = getSelection( sel );
		
		IFile resourceFile = null;
        if (fEditor.getEditorInput() instanceof ExternalEditorInput) {
            // TODO Devin should be able to implement this somehow, see PR 78118
            operationNotAvailable(CSEARCH_OPERATION_OPERATION_UNAVAILABLE_MESSAGE);
            return;
        } else {
            resourceFile = fEditor.getInputFile();
        }
        
		IASTTranslationUnit tu = null;
		IASTNode foundNode = null;
        
        ParseWithProgress runnable = new ParseWithProgress(resourceFile);
        ProgressMonitorDialog progressMonitor = new ProgressMonitorDialog(fEditor.getSite().getShell());
        try {
            progressMonitor.run(true, true, runnable);
            tu = runnable.getTu();
        } catch (InvocationTargetException e1) {
            operationNotAvailable(CSEARCH_OPERATION_OPERATION_UNAVAILABLE_MESSAGE);
            return;
        } catch (InterruptedException e1) {
            operationNotAvailable(CSEARCH_OPERATION_OPERATION_UNAVAILABLE_MESSAGE);
            return;
        }
        
		String file=null;
		if( resourceFile != null )
			file = resourceFile.getLocation().toOSString();
		else
		{
			if( resourceFile instanceof ExternalEditorInput )
				file = ((ExternalEditorInput)resourceFile).getStorage().getFullPath().toOSString();
		}
		try{
			foundNode = tu.selectNodeForLocation(file, selNode.selStart, selNode.selEnd - selNode.selStart);
		} 
		catch (ParseError er){}
		catch (Exception ex){}
		catch ( VirtualMachineError vmErr){
			if (vmErr instanceof OutOfMemoryError){
				org.eclipse.cdt.internal.core.model.Util.log(null, "Selection Search Out Of Memory error: " + vmErr.getMessage() + " on File: " + resourceFile.getName(), ICLogConstants.CDT); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		IASTName foundName = null;
		if (foundNode instanceof IASTName) {
			foundName = (IASTName)foundNode;
		} else {
			ASTVisitor collector = null;
			if (DOMSearchUtil.getLanguageFromFile(resourceFile) == ParserLanguage.CPP) {
				collector = new DOMSearchUtil.CPPNameCollector();
			} else {
				collector = new DOMSearchUtil.CNameCollector();
			}
			
			if (foundNode == null) { // nothing found
				operationNotAvailable(CSEARCH_OPERATION_NO_NAMES_SELECTED_MESSAGE);
				return;
			}
			
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
			} else if (names.size() > 1) { // too many names selected
				operationNotAvailable(CSEARCH_OPERATION_TOO_MANY_NAMES_MESSAGE);
				return;
			}
			
			foundName = (IASTName)names.get(0);
		}
		
		LimitTo limitTo = getLimitTo();
		ICSearchScope searchScope = null;
		searchScope = getScope();

		CSearchQuery job = FindAction.createSearchQueryForName(foundName, limitTo, searchScope);
		
		if (job == null)
			return; 
		
		NewSearchUI.activateSearchResultView();
		
		NewSearchUI.runQueryInBackground(job);
	}
    
    private class ParseWithProgress implements IRunnableWithProgress 
    {
        private IFile file=null;
        private IASTTranslationUnit tu = null;
        
        public ParseWithProgress(IFile file) {
            this.file = file;
        }

        public void run(IProgressMonitor monitor) {
            try {
                tu = CDOM.getInstance().getASTService().getTranslationUnit(
                        file,
                        CDOM.getInstance().getCodeReaderFactory(
                                CDOM.PARSE_WORKING_COPY_WHENEVER_POSSIBLE));
            } catch (IASTServiceProvider.UnsupportedDialectException e) {
                operationNotAvailable(CSEARCH_OPERATION_OPERATION_UNAVAILABLE_MESSAGE);
                return;
            }
        }

        public IASTTranslationUnit getTu() {
            return tu;
        }
    };
	
    abstract protected String getScopeDescription(); 

	abstract protected ICSearchScope getScope();
	
	abstract protected LimitTo getLimitTo();
    
}
