/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;




public class SystemEditFileLineAction extends SystemEditFileAction {

	
	protected IRemoteFile _remoteFile;
	protected int _line, _charStart, _charEnd;
	
	/**
	 * Constructor for SystemEditFileAction.
	 */
	public SystemEditFileLineAction(String text, String tooltip, ImageDescriptor image, Shell parent, String editorId, IRemoteFile remoteFile, int line, int charStart, int charEnd) {
		super(text, tooltip, image, parent, editorId);
		_line = line;
		_remoteFile = remoteFile;
		_charStart = charStart;
		_charEnd = charEnd;
	}
		
	public void run() {
		process(_remoteFile);
	}
	
	/**
	 * Process the object: download file, open in editor, etc.
	 */
	protected void process(IRemoteFile remoteFile) {
		super.process(remoteFile);
		handleGotoLine();			
	}
	
	protected void handleGotoLine() {
		handleGotoLine(_remoteFile, _line, _charStart, _charEnd);	
	}
		
	public static void handleGotoLine(IRemoteFile remoteFile, int line, int charStart, int charEnd) {
		
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
						
						IFileEditorInput input = (IFileEditorInput) einput;
						IFile efile = input.getFile();

						SystemIFileProperties properties = new SystemIFileProperties(efile);
						String comparePath = properties.getRemoteFilePath();

						if (comparePath != null && (comparePath.replace('\\','/').equals(fileName.replace('\\','/')))) {
							
							editor = editorp;
							persp.bringToTop(editor);
								
							try {
								IMarker marker = createMarker(efile, line, charStart, charEnd);
								IDE.gotoMarker(editor, marker);
							}
							catch (CoreException e) {
								SystemBasePlugin.logError("Error occured in handleGotoLine", e);
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