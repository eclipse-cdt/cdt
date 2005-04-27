/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search.actions;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.core.search.DOMSearchUtil;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.model.CProject;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.editor.ITranslationUnitEditorInput;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IUpdate;

public class OpenDeclarationsAction extends SelectionParseAction implements IUpdate {
	public static final IASTName[] BLANK_NAME_ARRAY = new IASTName[0];
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
	String projectName = "";  //$NON-NLS-1$
	 private static class Storage
	{
		private IResource resource;
		private String fileName;
		private int offset=0;
		private int length=0;

		public final String getFileName() {
			return fileName;
		}
		public Storage() {
		}
		public final IResource getResource() {
			return resource;
		}
		public int getLength() {
			return length;
		}
		public int getOffset() {
			return offset;
		}
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
		public void setLength(int length) {
			this.length = length;
		}
		public void setOffset(int offset) {
			this.offset = offset;
		}
		public void setResource(IResource resource) {
			this.resource = resource;
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
			// steps:
			// 1- parse and get the best selected name based on the offset/length into that TU
			// 2- based on the IASTName selected, find the best declaration of it in the TU
			// 3- if no IASTName is found for a declaration, then search the Index
			public void run(IProgressMonitor monitor) {
				int selectionStart = selNode.selStart;
				int selectionLength = selNode.selEnd - selNode.selStart;
                
                IFile resourceFile = null;
                
                IASTName[] selectedNames = BLANK_NAME_ARRAY;
                if (fEditor.getEditorInput() instanceof ExternalEditorInput) {
                    if( fEditor.getEditorInput() instanceof ITranslationUnitEditorInput )
                    {
                        ITranslationUnitEditorInput ip = (ITranslationUnitEditorInput) fEditor.getEditorInput();
                        IResource r = ip.getTranslationUnit().getUnderlyingResource();
                        if( r.getType() == IResource.FILE )
                            resourceFile = (IFile) r;
                        else
                        {
                            operationNotAvailable(CSEARCH_OPERATION_OPERATION_UNAVAILABLE_MESSAGE);
                            return;

                        }
                    }
                }
                else
                    resourceFile = fEditor.getInputFile();
                

                if (resourceFile != null) 
                    projectName = findProjectName(resourceFile);
                
                // step 1 starts here
                if (resourceFile != null)
                    selectedNames = DOMSearchUtil.getSelectedNamesFrom(resourceFile, selectionStart, selectionLength);
                				
				if (selectedNames.length > 0 && selectedNames[0] != null) { // just right, only one name selected
					IASTName searchName = selectedNames[0];
					// step 2 starts here
					IASTName[] domNames = DOMSearchUtil.getNamesFromDOM(searchName, ICSearchConstants.DECLARATIONS);
					if (domNames != null && domNames.length > 0 && domNames[0] != null) {
                        String fileName=null;
                        int start=0;
                        int end=0;
                        
                        if ( domNames[0].getTranslationUnit() != null ) {
                            IASTFileLocation location = domNames[0].getTranslationUnit().flattenLocationsToFile( domNames[0].getNodeLocations() );
                            fileName = location.getFileName();
                            start = location.getNodeOffset();
                            end = location.getNodeOffset() + location.getNodeLength();
                        }
                        
                        if (fileName != null) {
                            storage.setFileName(fileName);
                            storage.setLength(end - start);
                            storage.setOffset(start);
                            storage.setResource(ParserUtil.getResourceForFilename( fileName ));
                        }
					} else {
						// step 3 starts here
						ICElement[] scope = new ICElement[1];
						scope[0] = new CProject(null, fEditor.getInputFile().getProject());
						Set matches = DOMSearchUtil.getMatchesFromSearchEngine(SearchEngine.createCSearchScope(scope), searchName, ICSearchConstants.DECLARATIONS);

						if (matches != null) {
							Iterator itr = matches.iterator();
							while(itr.hasNext()) {
								Object match = itr.next();
								if (match instanceof IMatch) {
									IMatch theMatch = (IMatch)match;
									storage.setFileName(theMatch.getLocation().toOSString());
									storage.setLength(theMatch.getEndOffset() - theMatch.getStartOffset());
									storage.setOffset(theMatch.getStartOffset());
                                    storage.setResource(ParserUtil.getResourceForFilename(theMatch.getLocation().toOSString()));
									break;
								}
							}
						}
					}
				} else if (selectedNames.length == 0){
					operationNotAvailable(CSEARCH_OPERATION_NO_NAMES_SELECTED_MESSAGE);
					return;
				} else {
					operationNotAvailable(CSEARCH_OPERATION_TOO_MANY_NAMES_MESSAGE);
					return;
				}
				
				return;
			}
            
            private String findProjectName(IFile resourceFile) {
                if( resourceFile == null ) return ""; //$NON-NLS-1$
                IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
                for( int i = 0; i < projects.length; ++i )
                {
                    if( projects[i].contains(resourceFile) )
                        return projects[i].getName();
                }
                return ""; //$NON-NLS-1$
            }
 		};

		try {
	 		ProgressMonitorDialog progressMonitor = new ProgressMonitorDialog(getShell());
	 		progressMonitor.run(true, true, runnable);
	 		
			int nameOffset = storage.getOffset();
			int nameLength = storage.getLength();
			if( storage.getResource() != null )
	 		{
                clearStatusLine();
				open( storage.getResource(), nameOffset,  nameLength );
	 			return;
	 		}
			String fileName = storage.getFileName();
			
 			if (fileName != null){
                clearStatusLine();
	 			open( fileName, nameOffset, nameLength);
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

        ICProject cproject = CoreModel.getDefault().getCModel().getCProject( projectName );
		ITranslationUnit unit = CoreModel.getDefault().createTranslationUnitFrom(cproject, path);
		if (unit != null) {
			setSelectionAtOffset( EditorUtility.openInEditor(unit), offset, length );
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
		if( part instanceof AbstractTextEditor )
        {
			try {
            ((AbstractTextEditor) part).selectAndReveal(offset, length);
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

}

