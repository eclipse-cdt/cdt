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

import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.core.search.ICSearchConstants.SearchFor;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.IUpdate;


public class OpenDeclarationsAction extends SelectionParseAction implements IUpdate {
		
	//private String fDialogTitle;
	//private String fDialogMessage;
	SearchEngine searchEngine = null;

	/**
	 * Creates a new action with the given editor
	 */
	public OpenDeclarationsAction(CEditor editor) {
		super( editor );
		setText(CEditorMessages.getString("OpenDeclarations.label")); //$NON-NLS-1$
		setToolTipText(CEditorMessages.getString("OpenDeclarations.tooltip")); //$NON-NLS-1$
		setDescription(CEditorMessages.getString("OpenDeclarations.description")); //$NON-NLS-1$
//		setDialogTitle(CEditorMessages.getString("OpenDeclarations.dialog.title")); //$NON-NLS-1$
//		setDialogMessage(CEditorMessages.getString("OpenDeclarations.dialog.message")); //$NON-NLS-1$

		searchEngine = new SearchEngine();
	}

//	protected void setDialogTitle(String title) {
//		fDialogTitle= title;
//	}
//	
//	protected void setDialogMessage(String message) {
//		fDialogMessage= message;
//	}
	
	protected SelSearchNode getSelectedStringFromEditor() {
		ISelection selection = getSelection();
		if( selection == null || !(selection instanceof ITextSelection) ) 
	 		 return null;

 		return getSelection( (ITextSelection)selection );
	}
	
	 private static class Storage
	{
		private IASTOffsetableNamedElement element;
		private IResource resource;
		private String fileName;

		public IASTOffsetableNamedElement getNamedElement()
		{
			return element;
		}
		/**
		 * @return Returns the fileName.
		 */
		public final String getFileName() {
			return fileName;
		}
		/**
		 * @param fileName The fileName to set.
		 */
		public final void setFileName(String fileName) {
			this.fileName = fileName;
		}
		/**
		 * @return Returns the resource.
		 */
		public final IResource getResource() {
			return resource;
		}
		/**
		 * @param resource The resource to set.
		 */
		public final void setResource(IResource resource) {
			this.resource = resource;
		}
		/**
		 * @param element The element to set.
		 */
		public final void setElement(IASTOffsetableNamedElement element) {
			this.element = element;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		final SelSearchNode selNode = getSelectedStringFromEditor();
		
		if(selNode == null) {
			return;
		}
		
		final Storage storage = new Storage();
		

		IRunnableWithProgress runnable = new IRunnableWithProgress() 
		{
			public void run(IProgressMonitor monitor) {
				IFile resourceFile = fEditor.getInputFile();
				IParser parser = setupParser(resourceFile);
 		 		int selectionStart = selNode.selStart;
 		 		int selectionEnd = selNode.selEnd;

				IParser.ISelectionParseResult result = null;
				IASTOffsetableNamedElement node = null;
				try{
					result = parser.parse(selectionStart,selectionEnd);
					if( result != null )
						node = result.getOffsetableNamedElement();
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
				}

				storage.setFileName( result.getFilename() );
				storage.setElement( node );
				storage.setResource( ParserUtil.getResourceForFilename( result.getFilename() ) );
				return;
			}
 		};

		try {
	 		ProgressMonitorDialog progressMonitor = new ProgressMonitorDialog(getShell());
	 		progressMonitor.run(true, true, runnable);
	 		
	 		IASTOffsetableNamedElement namedElement = storage.getNamedElement();
	 		if( namedElement == null ){
	 			operationNotAvailable();
	 			return;
	 		} else {
	 			clearStatusLine();
	 		}
	 		
			if( storage.getResource() != null )
	 		{
				int nameOffset = 0;
				int nameEndOffset = 0;
				
				nameOffset = namedElement.getNameOffset();
				nameEndOffset = namedElement.getNameEndOffset();
				
				open( storage.getResource(), nameOffset,  nameEndOffset - nameOffset );
	 			return;
	 		}
	 		else
	 		{
	 			String fileName = null;
	 			int nameOffset = 0;
				int nameEndOffset = 0;
				
				fileName = storage.getFileName();
				nameOffset = namedElement.getNameOffset();
				nameEndOffset = namedElement.getNameEndOffset();
				
	 			if (fileName != null){
		 			 open( fileName,nameOffset,  nameEndOffset - nameOffset);
	 			}
	 		}

		} catch(Exception x) {
		 		 CUIPlugin.getDefault().log(x);
		}
	}

	/**
	 * @param string
	 * @param i
	 */
	protected boolean open(String filename, int offset, int length) throws PartInitException, CModelException {
		IPath path = new Path( filename );
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
		if( file != null )
		{
			open( file, offset, length );
			return true;
		}

		FileStorage storage = new FileStorage(null, path);
		IEditorPart part = EditorUtility.openInEditor(storage);
		setSelectionAtOffset(part, offset, length);
		return true;
		
	}
	protected Shell getShell() {
		return fEditor.getSite().getShell();
	}
	
	
	protected void open( IMatch element ) throws CModelException, PartInitException
	{
		open( element.getResource(), element.getStartOffset(), element.getEndOffset() - element.getStartOffset() );
	}
	
	/**
	 * Opens the editor on the given element and subsequently selects it.
	 */
	protected void open( IResource resource, int offset, int length ) throws CModelException, PartInitException {
		IEditorPart part= EditorUtility.openInEditor(resource);
		setSelectionAtOffset(part, offset, length);
	}
						
	/**
	 * @param part
	 * @param offset
	 * @param length TODO
	 */
	private void setSelectionAtOffset(IEditorPart part, int offset, int length) {
		//int line = element.getStartOffset();
		//if(line > 0) line--;
		if(part instanceof CEditor) {
			CEditor ed = (CEditor)part;
			
			try {					
				//IDocument document= ed.getDocumentProvider().getDocument(ed.getEditorInput());
				//if(line > 3) {
				//	ed.selectAndReveal(document.getLineOffset(line - 3), 0);
				//}
				ed.selectAndReveal(offset, length);
			} catch (Exception e) {}
		}
	}
//	/**
//	 * Shows a dialog for resolving an ambigous C element.
//	 * Utility method that can be called by subclassers.
//	 */
//	protected IMatch selectCElement(List elements, Shell shell, String title, String message) {
//		
//		int nResults= elements.size();
//		
//		if (nResults == 0)
//			return null;
//		
//		if (nResults == 1)
//			return (IMatch) elements.get(0);
//			
//
//		ElementListSelectionDialog dialog= new ElementListSelectionDialog(shell, new CSearchResultLabelProvider(), false, false);
//		dialog.setTitle(title);
//		dialog.setMessage(message);
//		dialog.setElements(elements);
//		
//		if (dialog.open() == Window.OK) {
//			Object[] selection= dialog.getResult();
//			if (selection != null && selection.length > 0) {
//				nResults= selection.length;
//				for (int i= 0; i < nResults; i++) {
//					Object current= selection[i];
//					if (current instanceof IMatch)
//						return (IMatch) current;
//				}
//			}
//		}		
//		return null;
//	}	
	

	/* (non-Javadoc)
	  * @see org.eclipse.ui.texteditor.IUpdate#update()
	  */
	 public void update() {
	 		 setEnabled(getSelectedStringFromEditor() != null);
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


}

