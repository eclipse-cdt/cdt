/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * Martin Oberhuber (Wind River) - [189130] Move SystemIFileProperties from UI to Core
 * David McKnight   (IBM)        - [224377] "open with" menu does not have "other" option
 *******************************************************************************/

package org.eclipse.rse.internal.files.ui.actions;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.subsystems.IRemoteLineReference;
import org.eclipse.rse.files.ui.resources.ISystemTextEditor;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.subsystems.files.core.SystemIFileProperties;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Menu;
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


/**
 * Open With menu class for openning remote files to a certain line number
 */
public class SystemRemoteFileLineOpenWithMenu extends SystemRemoteFileOpenWithMenu 
{
	protected IRemoteLineReference _remoteLine;

	
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

			}
		}
	}
	
	public void fill(Menu menu, int index) 
	{
		
		super.fill(menu, index);
	}
	
	
	protected IEditorRegistry getEditorRegistry()
	{
		return RSEUIPlugin.getDefault().getWorkbench().getEditorRegistry();
	}
	
	protected IEditorDescriptor getDefaultTextEditor()
	{
		IEditorRegistry registry = getEditorRegistry();
		return registry.findEditor("org.eclipse.ui.DefaultTextEditor"); //$NON-NLS-1$
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
					if (file.isFile())
					{
						return file;
					}
					return null;
				} 
			}
		}

		return file;
	}
	
	
	
	protected void openEditor(IRemoteFile file, IEditorDescriptor descriptor)
	{
		SystemEditableRemoteFile editableFile = new SystemEditableRemoteFile(file, descriptor);
		editableFile.open(SystemBasePlugin.getActiveWorkbenchShell());
		handleGotoLine();
	}
	
	
	protected void handleGotoLine()
	{
		handleGotoLine(_remoteFile, _remoteLine.getLine(), _remoteLine.getCharStart(), _remoteLine.getCharEnd());	
	}
	
		
	public static void handleGotoLine(IRemoteFile remoteFile, int line, int charStart, int charEnd)
	{
		//if (line > 0)
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
	
}
