/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.search.DOMSearchUtil;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.model.CProject;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.editor.ITranslationUnitEditorInput;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Open Definition Action (F2).
 * 
 * @author dsteffle
 */
public class OpenDefinitionAction extends SelectionParseAction implements
        IUpdate {

    public static final IASTName[] BLANK_NAME_ARRAY = new IASTName[0];
    //private String fDialogTitle;
    //private String fDialogMessage;
    SearchEngine searchEngine = null;

    /**
     * Creates a new action with the given editor
     */
    public OpenDefinitionAction(CEditor editor) {
        super( editor );
        setText(CEditorMessages.getString("OpenDefinition.label")); //$NON-NLS-1$
        setToolTipText(CEditorMessages.getString("OpenDefinition.tooltip")); //$NON-NLS-1$
        setDescription(CEditorMessages.getString("OpenDefinition.description")); //$NON-NLS-1$

        searchEngine = new SearchEngine();
    }

//  protected void setDialogTitle(String title) {
//      fDialogTitle= title;
//  }
//  
//  protected void setDialogMessage(String message) {
//      fDialogMessage= message;
//  }
    
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
            // 2- based on the IASTName selected, find the best definition of it in the TU
            // 3- if no IASTName is found for a definition, then search the Index
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
                    IASTName[] domNames = DOMSearchUtil.getNamesFromDOM(searchName, ICSearchConstants.DEFINITIONS);

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
                        } else {
                            operationNotAvailable(CSEARCH_OPERATION_NO_DEFINITION_MESSAGE);
                        }
                    } else {
                        // step 3 starts here
                        ICElement[] scope = new ICElement[1];
                        scope[0] = new CProject(null, fEditor.getInputFile().getProject());
                        Set matches = DOMSearchUtil.getMatchesFromSearchEngine(SearchEngine.createCSearchScope(scope), searchName, ICSearchConstants.DEFINITIONS);

                        if (matches != null && matches.size() > 0) {
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
                        } else {
                            operationNotAvailable(CSEARCH_OPERATION_NO_DEFINITION_MESSAGE);
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

}