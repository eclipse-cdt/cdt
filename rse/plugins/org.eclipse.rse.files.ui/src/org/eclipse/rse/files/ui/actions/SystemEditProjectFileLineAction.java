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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.files.ui.resources.RemoteSourceLookupDirector;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteError;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;




public class SystemEditProjectFileLineAction extends SystemEditFileAction {

	
	protected IRemoteFile _remoteFile;
	protected IRemoteOutput _output;
	
	/**
	 * Constructor for SystemEditFileAction
	 */
	public SystemEditProjectFileLineAction(String text, String tooltip, ImageDescriptor image, Shell parent, String editorId, 
			IRemoteFile remoteFile, IRemoteOutput output) 
	{
		super(text, tooltip, image, parent, editorId);

		_output = output;
		_remoteFile = remoteFile;
	}
		
	public void run() 
	{		
		process();
	}
	
	
	
	/**
	 * Process the object: download file, open in editor, etc.
	 */
	protected void process() 
	{
		openWorkspaceFile(_remoteFile, _output);			
	}
		
	
	protected IEditorRegistry getEditorRegistry()
	{
		return RSEUIPlugin.getDefault().getWorkbench().getEditorRegistry();
	}
	
	protected IEditorDescriptor getDefaultTextEditor()
	{
		IEditorRegistry registry = getEditorRegistry();
		return registry.findEditor("org.eclipse.ui.DefaultTextEditor");
	}
	/**
	 * Open workspace file associated with IRemoteCommandShell.  If there is no associated project
	 * return.
	 * @param remoteFile
	 * @param output
	 * @return
	 */
	protected boolean openWorkspaceFile(IRemoteFile remoteFile, IRemoteOutput output)
	{
		IRemoteCommandShell cmd = (IRemoteCommandShell)(output.getParent());
		IProject associatedProject = cmd.getAssociatedProject();
		if (associatedProject != null)
		{
			ProjectSourceContainer container = new ProjectSourceContainer(associatedProject, false);
			ISourceLookupDirector director = new RemoteSourceLookupDirector(); 
			container.init(director);
			try
			{
				Object[] matches = container.findSourceElements(remoteFile.getName());
				for (int i = 0; i < matches.length; i++)
				{
					//System.out.println("match="+matches[i]);
				}
				
				if (matches.length == 1)
				{
					IFile localMatch = (IFile)matches[0];
				
					
					
					IWorkbenchPage activePage = SystemBasePlugin.getActiveWorkbenchWindow().getActivePage();
	
					FileEditorInput finput = new FileEditorInput(localMatch);						
				
					IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(remoteFile.getName());
					if (desc == null)
					{
						desc = getDefaultTextEditor();
					}
					String editorid = desc.getId();
	
					IEditorPart editor = activePage.openEditor(finput, editorid);
					
					int line = output.getLine();
					int charStart = output.getCharStart();
					int charEnd = output.getCharEnd();
					
					try
					{
						IMarker marker = null;
						
						// DKM - should we?  this will populate the Problems view..but resources are actually remote
						if (output instanceof IRemoteError)
						{
							IRemoteError error = (IRemoteError)output;
							String type = error.getType();
							
							marker = localMatch.createMarker(IMarker.TEXT);
					
							if (type.equals("error"))
							{
								marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
							}
							else if (type.equals("warning"))
							{
								marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
							}
							else if (type.equals("informational"))
							{
								marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
							}
							
							marker.setAttribute(IMarker.MESSAGE, output.getText());
							marker.setAttribute(IMarker.LINE_NUMBER, line);
							marker.setAttribute(IMarker.CHAR_START, charStart);
							marker.setAttribute(IMarker.CHAR_END, charEnd);
							
						}
						else
						{
							marker = localMatch.createMarker(IMarker.TEXT);						
							marker.setAttribute(IMarker.LINE_NUMBER, line);
							marker.setAttribute(IMarker.CHAR_START, charStart);
							marker.setAttribute(IMarker.CHAR_END, charEnd);
						}
						IDE.gotoMarker(editor, marker);
					
						
					}
					catch (CoreException e)
					{
						e.printStackTrace();
					}
					return true;
				}
			}
			catch(Exception e)
			{				
				e.printStackTrace();
			}
		}	
		
		return false;
	}
}
	