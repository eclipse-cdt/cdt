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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.IRemoteLineReference;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.files.ui.resources.ISystemTextEditor;
import org.eclipse.rse.files.ui.resources.RemoteSourceLookupDirector;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.files.ui.resources.SystemIFileProperties;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteError;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;


/**
 * Open With menu class for openning remote files to a certain line number
 */
public class SystemRemoteFileLineOpenWithMenu extends SystemRemoteFileOpenWithMenu 
{
	protected IRemoteLineReference _remoteLine;
	protected IProject _associatedProject;
	
	public void updateSelection(IStructuredSelection selection)
	{
		if (selection.size() == 1)
		{
			Object element = selection.getFirstElement();
			if (element instanceof IRemoteLineReference)
			{							
				
				_remoteLine = (IRemoteLineReference)element;
				_remoteFile = outputToFile(_remoteLine);
				if (_remoteFile == null)
				{
					return;
				}
				if (_remoteLine instanceof IRemoteOutput)
				{
					IRemoteOutput output = (IRemoteOutput)_remoteLine;
					_associatedProject = ((IRemoteCommandShell)output.getParent()).getAssociatedProject();
				}

			}
		}
	}
	
	public void fill(Menu menu, int index) 
	{
		if (_associatedProject != null)
		{
				IEditorDescriptor defaultEditor = getDefaultTextEditor();
				// project file edit action if there's an associated project
				IEditorDescriptor projectEditDescriptor = getPreferredEditor(_remoteFile);
				createProjectFileMenuItem(menu, defaultEditor, projectEditDescriptor);		
		}
		
		super.fill(menu, index);
	}
	
	
	protected IEditorRegistry getEditorRegistry()
	{
		return SystemPlugin.getDefault().getWorkbench().getEditorRegistry();
	}
	
	protected IEditorDescriptor getDefaultTextEditor()
	{
		IEditorRegistry registry = getEditorRegistry();
		return registry.findEditor("org.eclipse.ui.DefaultTextEditor");
	}
	
	/**
	 * Creates the menu item for the editor descriptor.
	 *
	 * @param menu the menu to add the item to
	 * @param descriptor the editor descriptor, or null for the system editor
	 * @param preferredEditor the descriptor of the preferred editor, or <code>null</code>
	 */
	protected void createProjectFileMenuItem(Menu menu, final IEditorDescriptor descriptor, final IEditorDescriptor preferredEditor) 
	{
		// XXX: Would be better to use bold here, but SWT does not support it.
		final MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setSelection(true);
		String pLabel = SystemMessage.sub(FileResources.RESID_OPEN_FROM_ASSOCIATED_PROJECT, "&1", _associatedProject.getName());
		menuItem.setText(pLabel);
		Image image = getImage(descriptor);
		if (image != null) 
		{
			menuItem.setImage(image);
		}
		Listener listener = new Listener() 
		{
			public void handleEvent(Event event) 
			{
				switch (event.type) 
				{
					case SWT.Selection:
						if(menuItem.getSelection())
						{
							openWorkspaceFile(_remoteFile, (IRemoteOutput)_remoteLine, descriptor);
						}
						break;
				}
			}
		};
		menuItem.addListener(SWT.Selection, listener);
	}
	
	
	public static IRemoteFile outputToFile(IRemoteLineReference output)
	{
		IRemoteFile file = null;
		Object parent = output.getParent();
		IRemoteFileSubSystem fs = null;
		if (parent instanceof IRemoteCommandShell)
		{
			fs = RemoteFileUtility.getFileSubSystem(((IRemoteCommandShell)parent).getCommandSubSystem().getHost());
		}
		else if (parent instanceof IRemoteFile)
		{
			return (IRemoteFile) parent;
		}

		if (fs != null)
		{
			String path = output.getAbsolutePath();
			if (path != null && path.length() > 0)
			{
				Object obj = null;
				try
				{
					obj = fs.getObjectWithAbsoluteName(path);
				}
				catch (Exception e)
				{
					return null;
				}
				if (obj != null && obj instanceof IRemoteFile)
				{
					file = (IRemoteFile) obj;
			
					return file;
				} 
			}
		}

		return file;
	}
	
	
	
	protected void openEditor(IRemoteFile file, IEditorDescriptor descriptor)
	{
		SystemEditableRemoteFile editableFile = new SystemEditableRemoteFile(file, descriptor.getId());
		editableFile.open(SystemBasePlugin.getActiveWorkbenchShell());
		handleGotoLine();
	}
	
	
	protected void handleGotoLine()
	{
		handleGotoLine(_remoteFile, _remoteLine.getLine(), _remoteLine.getCharStart(), _remoteLine.getCharEnd());	
	}
	
		
	public static void handleGotoLine(IRemoteFile remoteFile, int line, int charStart, int charEnd)
	{
		if (line > 0)
		{
			IWorkbench desktop = PlatformUI.getWorkbench();
			IWorkbenchPage persp = desktop.getActiveWorkbenchWindow().getActivePage();
			IEditorPart editor = null;
			String fileName = remoteFile.getAbsolutePath();
			IEditorReference[] editors = persp.getEditorReferences();
			for (int i = 0; i < editors.length; i++)
			{
				IEditorReference ref = editors[i];
				IEditorPart editorp = ref.getEditor(false);
				if (editorp != null)
				{
					IEditorInput einput = editorp.getEditorInput();
					if (einput instanceof IFileEditorInput)
					{
						IFileEditorInput input = (IFileEditorInput) einput;
						IFile efile = input.getFile();

						SystemIFileProperties properties = new SystemIFileProperties(efile);
						String comparePath = properties.getRemoteFilePath();

						if (comparePath != null && (comparePath.replace('\\','/').equals(fileName.replace('\\','/'))))
						{
							editor = editorp;
							persp.bringToTop(editor);
							if (editor instanceof ISystemTextEditor)
							{
								ISystemTextEditor lpex = (ISystemTextEditor)editor;
								lpex.gotoLine(line);
								lpex.selectText(charStart, charEnd);
		
							}
							else
							{
								try
								{
									IMarker marker = efile.createMarker(IMarker.TEXT);
									marker.setAttribute(IMarker.LINE_NUMBER, line);
									marker.setAttribute(IMarker.CHAR_START, charStart);
									marker.setAttribute(IMarker.CHAR_END, charEnd);
									
									IDE.gotoMarker(editor, marker);
								
									
								}
								catch (CoreException e)
								{
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Open workspace file associated with IRemoteCommandShell.  If there is no associated project
	 * return.
	 * @param remoteFile
	 * @param output
	 * @return
	 */
	protected boolean openWorkspaceFile(IRemoteFile remoteFile, IRemoteOutput output, IEditorDescriptor desc)
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
				
					if (desc == null)
					{
						desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(remoteFile.getName());
					}
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