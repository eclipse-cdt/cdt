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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
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
import org.eclipse.cdt.core.parser.ParseError.ParseErrorKind;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.ICSearchConstants.LimitTo;
import org.eclipse.cdt.core.search.ICSearchConstants.SearchFor;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchOperation;
import org.eclipse.cdt.internal.ui.search.CSearchResultCollector;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.ui.IEditorPart;
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
		ParserLanguage language = CoreModel.getDefault().hasCCNature(currentProject) ? ParserLanguage.CPP : ParserLanguage.C;
		
		IParser parser = null;
		FileReader reader = null;
		try {
			reader = new FileReader(resourceFile.getLocation().toFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try
		{
			parser = ParserFactory.createParser( 
							ParserFactory.createScanner( reader, resourceFile.getLocation().toOSString(), scanInfo, ParserMode.SELECTION_PARSE, language, new NullSourceElementRequestor(), ParserUtil.getScannerLogService() ), 
							new NullSourceElementRequestor(), ParserMode.SELECTION_PARSE, language, ParserUtil.getParserLogService() );
			
		} catch( ParserFactoryError pfe ){}
		
	   return parser;
	 }
	 /**
	 * @param node
	 */
	 protected CSearchOperation createSearchOperation(String pattern, SearchFor searchFor) {
		CSearchOperation op = null;
		ICSearchScope scope = getScope();
		String scopeDescription = getScopeDescription();
	
		//Create a case sensitive search operation - limited by the node
		List search = new LinkedList();
		search.add(searchFor);
		
		CSearchResultCollector collector= new CSearchResultCollector();
		
		LimitTo limitTo = getLimitTo();
		
		op = new CSearchOperation(CCorePlugin.getWorkspace(), pattern,true,search,limitTo,scope,scopeDescription,collector);
		return op;
		
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
		ICElement tempElement = null;
		
		IEditorPart temp = fSite.getPage().getActiveEditor();
		CEditor cTemp = null;
		if (temp instanceof CEditor){
			cTemp = (CEditor) temp;
		}
		
		if (cTemp == null ||
			cTemp.getSelectionProvider() == null){
			operationNotAvailableDialog();
			return;
		}
		
		fEditor=cTemp;
	 	ITextSelection selection= (ITextSelection) fEditor.getSelectionProvider().getSelection();
	 		
		run(selection);
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
		IASTNode node = null;
		
		try{
			node = parser.parse(selectionStart,selectionEnd);
		} 
		catch (ParseError er){
			ParseErrorKind per = er.getErrorKind();
			int val = per.getEnumValue();
			er.printStackTrace();
		}
		catch (Exception ex){
		ex.printStackTrace();}
		
		if (node == null){
			operationNotAvailableDialog();
			return;
		}
		
		CSearchOperation op = createSearchOperation(selectedText.getText(),getSearchForFromNode(node));
		
		performSearch(op);
	}
	
	private SearchFor getSearchForFromNode(IASTNode node){
		SearchFor searchFor = null;
		
		if (node instanceof IASTClassSpecifier){
			//Find out if class, struct, union
			   IASTClassSpecifier tempNode = (IASTClassSpecifier) node;
			   if(tempNode.getClassKind().equals(ASTClassKind.CLASS)){
			   	searchFor = ICSearchConstants.CLASS;
			   }
			   else if (tempNode.getClassKind().equals(ASTClassKind.STRUCT)){
			   	searchFor = ICSearchConstants.STRUCT;
			   }
			   else if (tempNode.getClassKind().equals(ASTClassKind.UNION)){
			   	searchFor = ICSearchConstants.UNION;
			   }
			}
			else if (node instanceof IASTMethod){
				searchFor = ICSearchConstants.METHOD;
			}
			else if (node instanceof IASTFunction){
				searchFor = ICSearchConstants.FUNCTION;
			}
			else if (node instanceof IASTField){
				searchFor = ICSearchConstants.FIELD;
			}
			else if (node instanceof IASTVariable){
				searchFor = ICSearchConstants.VAR;
			}
			else if (node instanceof IASTEnumerationSpecifier){
				searchFor = ICSearchConstants.ENUM;
			}
			else if (node instanceof IASTEnumerator){
				searchFor = ICSearchConstants.FIELD;
			}
			else if (node instanceof IASTNamespaceDefinition){
				searchFor = ICSearchConstants.NAMESPACE;
			}
			
			return searchFor;
	}
	
	protected void performSearch(CSearchOperation op){
		 try {
		 	SearchUI.activateSearchResultView();
		 	
	 		 ProgressMonitorDialog progressMonitor = new ProgressMonitorDialog(fSite.getShell());
	 		 progressMonitor.run(true, true, op);
	
		 } catch(Exception x) {
		 		 CUIPlugin.getDefault().log(x);
		 }
	}
	
	private void operationNotAvailableDialog(){
		MessageDialog.openInformation(fEditor.getSite().getShell(),CSearchMessages.getString("CSearchOperation.operationUnavailable.title"), CSearchMessages.getString("CSearchOperation.operationUnavailable.message")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	abstract protected String getScopeDescription(); 

	abstract protected ICSearchScope getScope();
	
	abstract protected LimitTo getLimitTo();
}
