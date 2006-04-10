/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.files.ui.resources.SystemIFileProperties;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteSearchResult;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;


/**
 * Edit action used by search to create markers for all matches in a line and highlight the first match.
 */
public class SystemSearchEditFileLineAction extends SystemEditFileAction {
	
	protected IRemoteFile _remoteFile;
	protected IRemoteSearchResult _searchResult;

	/**
	 * Constructor to create an edit action that jumps to a file line.
	 * @param text the label for the action.
	 * @param tooltip the tooltip for the action.
	 * @param image the image for the action.
	 * @param parent the parent shell.
	 * @param editorId the editor id.
	 * @param remoteFile the remote file that is to be opened.
	 * @param line the line number.
	 */
	public SystemSearchEditFileLineAction(String text, String tooltip, ImageDescriptor image, Shell parent, String editorId, IRemoteFile remoteFile, IRemoteSearchResult searchResult) {
		super(text, tooltip, image, parent, editorId);
		this._remoteFile = remoteFile;
		this._searchResult = searchResult;
	}
	
	/**
	 * Calls process().
	 */
	public void run() {
		process(_remoteFile, _searchResult);
	}
	
	/**
	 * Process the remote file selection.
	 */
	public void process(IRemoteFile remoteFile, IRemoteSearchResult searchResult)  {
		super.process(remoteFile);
		handleGotoLine(remoteFile, searchResult);			
	}
	
	public static void handleGotoLine(IRemoteFile remoteFile, IRemoteSearchResult searchResult) {
		
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