/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.internal.ui.search.actions;

import java.io.CharArrayReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.ICSearchConstants.LimitTo;
import org.eclipse.cdt.core.search.ICSearchConstants.SearchFor;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchQuery;
import org.eclipse.cdt.internal.ui.search.CSearchResultCollector;
import org.eclipse.cdt.internal.ui.search.CSearchUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchSite;


public abstract class FindAction extends Action {
	
	protected IWorkbenchSite fSite;
	protected CEditor fEditor;
	
	public FindAction(CEditor editor){
		fEditor=editor;
		fSite=editor.getSite();
	}
	
	public FindAction(IWorkbenchSite site){
		fSite=site;
	}
	
	protected IParser setupParser(IFile resourceFile){
		//Get the scanner info
		IProject currentProject = resourceFile.getProject();
		IScannerInfo scanInfo = new ScannerInfo();
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(currentProject);
		if (provider != null){
		  IScannerInfo buildScanInfo = provider.getScannerInformation(currentProject);
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
		Reader reader = null;
		try {
			if( workingCopy == null )
				reader = new FileReader(resourceFile.getLocation().toFile());
			else 
				reader = new CharArrayReader( workingCopy.getContents() );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try
		{
			parser = ParserFactory.createParser( 
							ParserFactory.createScanner( reader, resourceFile.getLocation().toOSString(), scanInfo, ParserMode.SELECTION_PARSE, language, new NullSourceElementRequestor(), ParserUtil.getScannerLogService(), null ), 
							new NullSourceElementRequestor(), ParserMode.SELECTION_PARSE, language, ParserUtil.getParserLogService() );
			
		} catch( ParserFactoryError pfe ){}
		
	   return parser;
	 }
	 /**
	 * @param node
	 */
	 protected CSearchQuery createSearchQuery(String pattern, SearchFor searchFor) {
	 	CSearchQuery op = null;
		ICSearchScope scope = getScope();
		String scopeDescription = getScopeDescription();
	
		//Create a case sensitive search operation - limited by the node
		List search = new LinkedList();
		search.add(searchFor);
		
		CSearchResultCollector collector= new CSearchResultCollector();
		
		LimitTo limitTo = getLimitTo();
		
		op = new CSearchQuery(CCorePlugin.getWorkspace(), pattern,true,search,limitTo,scope,scopeDescription,collector);
		return op;
		
	}

	 protected CSearchQuery createSearchQuery( IASTOffsetableNamedElement node ){
	 	String pattern = null;
	 	
	 	if( node instanceof IASTQualifiedNameElement ){
	 		String [] qualNames = ((IASTQualifiedNameElement)node).getFullyQualifiedName();
	 		pattern = "::" + qualNames[0]; //$NON-NLS-1$
	 		for( int i = 1; i < qualNames.length; i++ ){
				 pattern += "::"; //$NON-NLS-1$
				 pattern += qualNames[i];
	 		}
	 	} else {
	 		pattern = node.getName();
	 	}
	 	
	 	if( node instanceof IASTFunction ){
	 		pattern += '(';
	 		String[] parameterTypes = ASTUtil.getFunctionParameterTypes((IASTFunction) node);
	 		for( int i = 0; i < parameterTypes.length; i++ ){
	 			if( i != 0 )
	 				pattern += ", "; //$NON-NLS-1$
	 			pattern += parameterTypes[i];
	 		}
	 		pattern += ')';
	 	}
	 	
	 	return createSearchQuery( pattern, CSearchUtil.getSearchForFromNode(node) );
	 }
	 
	protected ISelection getSelection(){
		ISelection sel = null;
		if (fSite != null){
			sel = fSite.getSelectionProvider().getSelection();
		}
		
		return sel;
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
	 		operationNotAvailableDialog();
	 		return;
	 	}
		ICElement element = (ICElement) obj;
		
		CSearchQuery job = createSearchQuery( element.getElementName(), CSearchUtil.getSearchForFromElement(element));
		NewSearchUI.activateSearchResultView();
		
		NewSearchUI.runQuery(job);
	}
	
	public void run(ITextSelection sel){
		 if(sel == null) {
	 		 return;
		 }
		 
		 final ITextSelection selectedText = sel;
		 int selectionStart = selectedText.getOffset();
		 int selectionEnd = selectionStart + selectedText.getLength();
		 
		IFile resourceFile = fEditor.getInputFile();
		IParser parser = setupParser(resourceFile);
		IASTOffsetableNamedElement node = null;
		IParser.ISelectionParseResult result = null;
		
		try{
			result = parser.parse(selectionStart,selectionEnd);
			if( result != null )
				node = result.getOffsetableNamedElement();
		} 
		catch (ParseError er){}
		catch (Exception ex){}
		catch ( VirtualMachineError vmErr){
			if (vmErr instanceof OutOfMemoryError){
				org.eclipse.cdt.internal.core.model.Util.log(null, "Selection Search Out Of Memory error: " + vmErr.getMessage() + " on File: " + resourceFile.getName(), ICLogConstants.CDT); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		if (node == null || !( node instanceof IASTNode )){
			operationNotAvailableDialog();
			return;
		}
	
		CSearchQuery job = createSearchQuery(node);
		NewSearchUI.activateSearchResultView();
		
		NewSearchUI.runQuery(job);
	}
	
	private void operationNotAvailableDialog(){
		MessageDialog.openInformation(fEditor.getSite().getShell(),CSearchMessages.getString("CSearchOperation.operationUnavailable.title"), CSearchMessages.getString("CSearchOperation.operationUnavailable.message")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	abstract protected String getScopeDescription(); 

	abstract protected ICSearchScope getScope();
	
	abstract protected LimitTo getLimitTo();
}
