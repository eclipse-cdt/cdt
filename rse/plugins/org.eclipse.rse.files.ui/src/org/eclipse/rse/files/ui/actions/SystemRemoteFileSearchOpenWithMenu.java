/********************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui.actions;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.files.ui.resources.SystemIFileProperties;
import org.eclipse.rse.services.search.IHostSearchResult;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteSearchResult;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;


/**
 * Open with menu for remote search result openning
 *  
 */
public class SystemRemoteFileSearchOpenWithMenu extends
		SystemRemoteFileLineOpenWithMenu 
{
	protected IRemoteSearchResult _searchResult;
	public void updateSelection(IStructuredSelection selection)
	{
		if (selection.size() == 1)
		{
			Object element = selection.getFirstElement();
			if (element instanceof IRemoteSearchResult)
			{							
				_searchResult = (IRemoteSearchResult)element;
				_remoteFile = outputToFile(_searchResult);
			}
		}
	}
	
	protected void handleGotoLine()
	{
		handleGotoLine(_remoteFile, _searchResult);
	}
	
	public static void handleGotoLine(IRemoteFile remoteFile, IHostSearchResult searchResult) {
		
		int line = searchResult.getLine();
		
		if (line > 0) {
			
			IWorkbench desktop = PlatformUI.getWorkbench();
			IWorkbenchPage persp = desktop.getActiveWorkbenchWindow().getActivePage();
			IEditorPart editor = null;
			String fileName = remoteFile.getAbsolutePath();
			IEditorReference[] editors = persp.getEditorReferences();
			
			for (int i = 0; i < editors.length; i++) {
				IEditorReference ref = editors[i];
				IEditorPart editorp = ref.getEditor(false);
						
				if (editorp != null) {
						
					IEditorInput einput = editorp.getEditorInput();
					
					if (einput instanceof IFileEditorInput) {
						IFileEditorInput input = (IFileEditorInput)einput;
						IFile efile = input.getFile();

						SystemIFileProperties properties = new SystemIFileProperties(efile);
						String comparePath = properties.getRemoteFilePath();

						if (comparePath != null && (comparePath.replace('\\','/').equals(fileName.replace('\\','/')))) {
							editor = editorp;
							persp.bringToTop(editor);
							
							int firstStartOffset = -1;
							int firstEndOffset = -1;
							
							int matchSize = searchResult.numOfMatches();
							
							if (matchSize > 0) {
								firstStartOffset = searchResult.getCharStart(0);
								firstEndOffset = searchResult.getCharEnd(0);
							}
							/* DKM- always use markers now
							if (editor instanceof ISystemTextEditor) {
								ISystemTextEditor lpex = (ISystemTextEditor)editor;
								lpex.gotoLine(line);
								lpex.selectText(firstStartOffset, firstEndOffset);
							}
							else
							*/
							 {
								
								try {
									
									// create a marker for the first match
									IMarker firstMarker = createMarker(efile, line, firstStartOffset, firstEndOffset);
									
									int charStart = -1;
									int charEnd = -1;
									
									for (int idx = 1; idx < matchSize; idx++) {
										charStart = searchResult.getCharStart(idx);
										charEnd = searchResult.getCharEnd(idx);
										createMarker(efile, line, charStart, charEnd);
									}
									
									// highlight the first marker (first match)
									IDE.gotoMarker(editor, firstMarker);
								}
								catch (CoreException e) {
									SystemBasePlugin.logError("Error occured trying to create a marker", e);
								}
							}
						}
					}
				}
			}
		}
	}
	
	protected static IMarker createMarker(IFile file, int line, int charStart, int charEnd) throws CoreException {
		IMarker marker = file.createMarker(IMarker.TEXT);
		marker.setAttribute(IMarker.LINE_NUMBER, line);
		marker.setAttribute(IMarker.CHAR_START, charStart);
		marker.setAttribute(IMarker.CHAR_END, charEnd);
		return marker;	
	}
}