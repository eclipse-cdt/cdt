/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search.actions;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
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
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.core.search.OrPattern;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.core.search.ICSearchConstants.SearchFor;
import org.eclipse.cdt.internal.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CSearchResultLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IUpdate;


public class OpenDeclarationsAction extends Action implements IUpdate {
		
	private String fDialogTitle;
	private String fDialogMessage;
	protected CEditor fEditor;
	SearchEngine searchEngine = null;
	/**
	 * Creates a new action with the given label and image.
	 */
	protected OpenDeclarationsAction() {
		
		setText(CEditorMessages.getString("OpenDeclarations.label")); //$NON-NLS-1$
		setToolTipText(CEditorMessages.getString("OpenDeclarations.tooltip")); //$NON-NLS-1$
		setDescription(CEditorMessages.getString("OpenDeclarations.description")); //$NON-NLS-1$
		setDialogTitle(CEditorMessages.getString("OpenDeclarations.dialog.title")); //$NON-NLS-1$
		setDialogMessage(CEditorMessages.getString("OpenDeclarations.dialog.message")); //$NON-NLS-1$

		searchEngine = new SearchEngine();
	}
	/**
	 * Creates a new action with the given editor
	 */
	public OpenDeclarationsAction(CEditor editor) {
		this();
		fEditor = editor;
	}
	
	protected void setDialogTitle(String title) {
		fDialogTitle= title;
	}
	
	protected void setDialogMessage(String message) {
		fDialogMessage= message;
	}
	 /**
	  * Return the selected string from the editor
	  * @return The string currently selected, or null if there is no valid selection
	  */
	 protected SelSearchNode getSelectedStringFromEditor() {
		 if (fEditor.getSelectionProvider() == null) {
		 		 return null;
		 }
	
		 try {
		 		 ISelectionProvider selectionProvider = fEditor.getSelectionProvider();
		 			
		 		 ITextSelection textSelection= (ITextSelection) selectionProvider.getSelection();
		 		 String seltext = textSelection.getText();
		 		 SelSearchNode sel = null;
		 		 if (seltext.equals("")) //$NON-NLS-1$
		 		 {
		 		 		 int selStart =  textSelection.getOffset();
		 		 		 IDocumentProvider prov = fEditor.getDocumentProvider();
		 		 		 IDocument doc = prov.getDocument(fEditor.getEditorInput());
		 		 		 //TODO: Change this to work with qualified identifiers
		 		 		 sel = getSelection(doc, selStart);
		 		 }
		 		 else {
		 		 	sel = new SelSearchNode();
		 		 	sel.selText= seltext;
		 		 	sel.selStart = textSelection.getOffset();
		 		 	sel.selEnd = textSelection.getOffset() + textSelection.getLength();
		 		 }
		 		 return sel;
		 } catch(Exception x) {
		 		 return null;
		 }
	 }
	/**
	 * @see IAction#actionPerformed
	 */
	public void run() {
		final SelSearchNode selNode = getSelectedStringFromEditor();
		
		if(selNode == null) {
			return;
		}
		
		final ArrayList elementsFound = new ArrayList();

		IRunnableWithProgress runnable = new IRunnableWithProgress() 
		{
			public void run(IProgressMonitor monitor) {
				BasicSearchResultCollector  resultCollector =  new BasicSearchResultCollector(monitor);
		 		IWorkingCopyManager fManager = CUIPlugin.getDefault().getWorkingCopyManager();
		 		ITranslationUnit unit = fManager.getWorkingCopy(fEditor.getEditorInput());
		 		//TODO: Change to Project Scope
				ICElement[] projectScopeElement = new ICElement[1];
				projectScopeElement[0] = unit.getCProject();//(ICElement)currentScope.getCProject();
				ICSearchScope scope = SearchEngine.createCSearchScope(projectScopeElement, true);
			
				IFile resourceFile = fEditor.getInputFile();
				IParser parser = setupParser(resourceFile);
				
				IASTNode node = null;
				
				try{
	 		 		int selectionStart = selNode.selStart;
	 		 		int selectionEnd = selNode.selEnd;
					node = parser.parse(selectionStart,selectionEnd);
				} 
				catch (ParseError er){}
				catch ( VirtualMachineError vmErr){
					if (vmErr instanceof OutOfMemoryError){
						org.eclipse.cdt.internal.core.model.Util.log(null, "Open Declarations Out Of Memory error: " + vmErr.getMessage() + " on File: " + resourceFile.getName(), ICLogConstants.CDT); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				catch (Exception ex){}
				
				finally{
					if (node == null){
						return;
					}
				
					SearchFor searchFor = getSearchForFromNode(node);
					ICSearchPattern pattern = SearchEngine.createSearchPattern( selNode.selText,searchFor,ICSearchConstants.DECLARATIONS,true);
					
					
					
					try {
						searchEngine.search(CUIPlugin.getWorkspace(), pattern, scope, resultCollector, true);
					} catch (InterruptedException e) {
					}
			 		elementsFound.addAll(resultCollector.getSearchResults());	 
				}
			}
 		};

		try {
	 		ProgressMonitorDialog progressMonitor = new ProgressMonitorDialog(getShell());
	 		progressMonitor.run(true, true, runnable);
	
	 		if (elementsFound.isEmpty() == true) {
	 			//TODO: Get rid of back up search when selection search improves
	 			//MessageDialog.openInformation(getShell(),CSearchMessages.getString("CSearchOperation.operationUnavailable.title"), CSearchMessages.getString("CSearchOperation.operationUnavailable.message")); //$NON-NLS-1$ 
	 			temporaryBackUpSearch();
	 			return;
	 		}
	
	 		IMatch selected= selectCElement(elementsFound, getShell(), fDialogTitle, fDialogMessage);
	 		if (selected != null) {
	 			open(selected);
	 			return;
	 		}
		} catch(Exception x) {
		 		 CUIPlugin.getDefault().log(x);
		}
	}

	protected Shell getShell() {
		return fEditor.getSite().getShell();
	}
	

	protected void temporaryBackUpSearch(){
		
		final SelSearchNode selNode = getSelectedStringFromEditor();
		
		if (selNode == null)
			return;
		
		final ArrayList elementsFound = new ArrayList();
		
		IRunnableWithProgress runnable = new IRunnableWithProgress() 
		{
		 public void run(IProgressMonitor monitor) {
		 
		 	String selectedText = selNode.selText;
			BasicSearchResultCollector  resultCollector =  new BasicSearchResultCollector(monitor);
	 		IWorkingCopyManager fManager = CUIPlugin.getDefault().getWorkingCopyManager();
	 		ITranslationUnit unit = fManager.getWorkingCopy(fEditor.getEditorInput());

			ICElement[] projectScopeElement = new ICElement[1];
			projectScopeElement[0] = unit.getCProject();//(ICElement)currentScope.getCProject();
			ICSearchScope scope = SearchEngine.createCSearchScope(projectScopeElement, true);
		
			IFile resourceFile = fEditor.getInputFile();
			OrPattern orPattern = new OrPattern();
			// search for global variables, functions, classes, structs, unions, enums and macros
		 		orPattern.addPattern(SearchEngine.createSearchPattern( selectedText, ICSearchConstants.VAR, ICSearchConstants.DECLARATIONS, true ));
		 		orPattern.addPattern(SearchEngine.createSearchPattern( selectedText, ICSearchConstants.FUNCTION, ICSearchConstants.DECLARATIONS, true ));
		 		orPattern.addPattern(SearchEngine.createSearchPattern( selectedText, ICSearchConstants.METHOD, ICSearchConstants.DECLARATIONS, true ));
		 		orPattern.addPattern(SearchEngine.createSearchPattern( selectedText, ICSearchConstants.TYPE, ICSearchConstants.DECLARATIONS, true ));
		 		orPattern.addPattern(SearchEngine.createSearchPattern( selectedText, ICSearchConstants.ENUM, ICSearchConstants.DECLARATIONS, true ));
		 		orPattern.addPattern(SearchEngine.createSearchPattern( selectedText, ICSearchConstants.FIELD, ICSearchConstants.DECLARATIONS, true ));
		 		orPattern.addPattern(SearchEngine.createSearchPattern( selectedText, ICSearchConstants.NAMESPACE, ICSearchConstants.DECLARATIONS, true ));
		 		orPattern.addPattern(SearchEngine.createSearchPattern( selectedText, ICSearchConstants.MACRO, ICSearchConstants.DECLARATIONS, true ));
		 		orPattern.addPattern(SearchEngine.createSearchPattern( selectedText, ICSearchConstants.TYPEDEF, ICSearchConstants.DECLARATIONS, true ));
			try {
				searchEngine.search(CUIPlugin.getWorkspace(), orPattern, scope, resultCollector, true);
			} catch (InterruptedException e) {
			}
			elementsFound.addAll(resultCollector.getSearchResults());
		 }
	  };
	  
	  try {
 		ProgressMonitorDialog progressMonitor = new ProgressMonitorDialog(getShell());
 		progressMonitor.run(true, true, runnable);

 		if (elementsFound.isEmpty() == true) {
 			MessageDialog.openInformation(getShell(),CSearchMessages.getString("CSearchOperation.operationUnavailable.title"), CSearchMessages.getString("CSearchOperation.operationUnavailable.message")); //$NON-NLS-1$ //$NON-NLS-2$ 
 			return;
 		}

 		IMatch selected= selectCElement(elementsFound, getShell(), fDialogTitle, fDialogMessage);
 		if (selected != null) {
 			open(selected);
 			return;
 		}
		} catch(Exception x) {
		 		 CUIPlugin.getDefault().log(x);
		}
	}
	
	/**
	 * Opens the editor on the given element and subsequently selects it.
	 */
	protected void open(IMatch element) throws CModelException, PartInitException {
		IEditorPart part= EditorUtility.openInEditor(element.getResource());
		//int line = element.getStartOffset();
		//if(line > 0) line--;
		if(part instanceof CEditor) {
			CEditor ed = (CEditor)part;
			
			try {					
				IDocument document= ed.getDocumentProvider().getDocument(ed.getEditorInput());
				//if(line > 3) {
				//	ed.selectAndReveal(document.getLineOffset(line - 3), 0);
				//}
				ed.selectAndReveal(element.getStartOffset() /*document.getLineOffset(line)*/, 0);
			} catch (Exception e) {}
		}
	}
						
	/**
	 * Shows a dialog for resolving an ambigous C element.
	 * Utility method that can be called by subclassers.
	 */
	protected IMatch selectCElement(List elements, Shell shell, String title, String message) {
		
		int nResults= elements.size();
		
		if (nResults == 0)
			return null;
		
		if (nResults == 1)
			return (IMatch) elements.get(0);
			

		ElementListSelectionDialog dialog= new ElementListSelectionDialog(shell, new CSearchResultLabelProvider(), false, false);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setElements(elements);
		
		if (dialog.open() == Window.OK) {
			Object[] selection= dialog.getResult();
			if (selection != null && selection.length > 0) {
				nResults= selection.length;
				for (int i= 0; i < nResults; i++) {
					Object current= selection[i];
					if (current instanceof IMatch)
						return (IMatch) current;
				}
			}
		}		
		return null;
	}	
	

	public SelSearchNode getSelection(IDocument doc, int fPos){
		int pos= fPos;
		char c;
		int fStartPos =0, fEndPos=0;
		String selectedWord=null;
		
		try{
			while (pos >= 0) {
				c= doc.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				--pos;
			}
			fStartPos= pos + 1;
			
			pos= fPos;
			int length= doc.getLength();
			while (pos < length) {
				c= doc.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				++pos;
			}
			fEndPos= pos;
			selectedWord = doc.get(fStartPos, (fEndPos - fStartPos));
		}
		catch(BadLocationException e){
		}
		
		SelSearchNode sel = new SelSearchNode();
		sel.selText = selectedWord;
		sel.selStart = fStartPos;
		sel.selEnd = fEndPos;
	
		return sel;		
	}
	 		 
	 /* (non-Javadoc)
	  * @see org.eclipse.ui.texteditor.IUpdate#update()
	  */
	 public void update() {
	 		 setEnabled(getSelectedStringFromEditor() != null);
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
							ParserFactory.createScanner( reader, resourceFile.getLocation().toOSString(), scanInfo, ParserMode.SELECTION_PARSE, language, new NullSourceElementRequestor(), ParserUtil.getScannerLogService(), null ), 
							new NullSourceElementRequestor(), ParserMode.SELECTION_PARSE, language, ParserUtil.getParserLogService() );
			
		} catch( ParserFactoryError pfe ){}
		
	   return parser;
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

	 class SelSearchNode{
	 	protected String selText;
	 	protected int selStart;
	 	protected int selEnd;
	 }
}

