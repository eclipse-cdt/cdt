package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.core.search.OrPattern;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CSearchResultLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * This action opens a java CEditor on the element represented by text selection of
 * the connected java source viewer.
 * 
 * Use action from package org.eclipse.jdt.ui.actions
 */
public class OpenDeclarationsAction extends Action implements IUpdate {
		
	private String fDialogTitle;
	private String fDialogMessage;
	protected CEditor fEditor;
	SearchEngine searchEngine = null;
	
	/**
	 * Creates a new action with the given label and image.
	 */
	protected OpenDeclarationsAction() {
		super();
		
		setText(CEditorMessages.getString("OpenDeclarations.label")); //$NON-NLS-1$
		setToolTipText(CEditorMessages.getString("OpenDeclarations.tooltip")); //$NON-NLS-1$
		setDescription(CEditorMessages.getString("OpenDeclarations.description")); //$NON-NLS-1$
		setDialogTitle(CEditorMessages.getString("OpenDeclarations.dialog.title")); //$NON-NLS-1$
		setDialogMessage(CEditorMessages.getString("OpenDeclarations.dialog.message")); //$NON-NLS-1$

		searchEngine = new SearchEngine();
	}
	
	/**
	 * Creates a new action with the given image.
	 */
	public OpenDeclarationsAction(ImageDescriptor image) {
		this();
		setImageDescriptor(image);
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
	
	public void setContentEditor(CEditor editor) {	
		fEditor= editor;
	}

		 /**
		  * Return the selected string from the editor
		  * @return The string currently selected, or null if there is no valid selection
		  */
		 protected String getSelectedStringFromEditor() {
		 		 if (fEditor.getSelectionProvider() == null) {
		 		 		 return null;
		 		 }

		 		 try {
		 		 		 ITextSelection selection= (ITextSelection) fEditor.getSelectionProvider().getSelection();
		 		 		 String sel = selection.getText();
		 		 		 if (sel.equals("")) //$NON-NLS-1$
		 		 		 {
		 		 		 		 int selStart =  selection.getOffset();
		 		 		 
		 		 		 		 IDocumentProvider prov = fEditor.getDocumentProvider();
		 		 		 		 IDocument doc = prov.getDocument(fEditor.getEditorInput());
		 		 		 		 sel = getSelection(doc, selStart);
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
		final String selectedText = getSelectedStringFromEditor();
		
		if(selectedText == null) {
			return;
		}
		
		final ArrayList elementsFound = new ArrayList();

		IRunnableWithProgress runnable = new IRunnableWithProgress() 
		{
			public void run(IProgressMonitor monitor) {
				BasicSearchResultCollector  resultCollector =  new BasicSearchResultCollector(monitor);
		 		IWorkingCopyManager fManager = CUIPlugin.getDefault().getWorkingCopyManager();
		 		ITranslationUnit unit = fManager.getWorkingCopy(fEditor.getEditorInput());

				ICElement[] projectScopeElement = new ICElement[1];
				projectScopeElement[0] = unit.getCProject();//(ICElement)currentScope.getCProject();
				ICSearchScope scope = SearchEngine.createCSearchScope(projectScopeElement, true);
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
	

	public String getSelection(IDocument doc, int fPos){
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
	
		return selectedWord;		
	}
		 		 
		 /* (non-Javadoc)
		  * @see org.eclipse.ui.texteditor.IUpdate#update()
		  */
		 public void update() {
		 		 setEnabled(getSelectedStringFromEditor() != null);
		 }

}

