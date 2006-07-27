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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.files.ui.resources.UniversalFileTransferUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemSeparatorAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;


/**
 * Class for creating edit actions
 * @deprecated - use <code>SystemRemoteFileOpenWithMenu</code> now
 *  
 */
public class SystemCreateEditActions
{

	
	protected IEditorRegistry registry;
	
	/*
	 * Compares the labels from two IEditorDescriptor objects 
	 */
	private static final Comparator comparer = new Comparator() {
		private Collator collator = Collator.getInstance();

		public int compare(Object arg0, Object arg1) {
			String s1 = ((IEditorDescriptor)arg0).getLabel();
			String s2 = ((IEditorDescriptor)arg1).getLabel();
			return collator.compare(s1, s2);
		}
	};

	/**
	 * Constructor for CreateEditActions
	 */
	public SystemCreateEditActions() {
		super();
		registry = PlatformUI.getWorkbench().getEditorRegistry();
	}

	/**
	 * Create edit actions
	 */
	public void create(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup)
	{

		if (selection.size() <= 0)
			return;
		
		// create basic open 
		createForAnySelection(menu, selection, shell, menuGroup);
		
		
		// create open with->
		if (selection.size() == 1)
		{
			createForSingleSelection(menu, selection.getFirstElement(), shell, menuGroup);
		}
		else
		{ // editing is only allowed when there is only one selection
			return;
		}
	}

	public void createForAnySelection(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup)
	{
	    String label = SystemResources.ACTION_CASCADING_OPEN_LABEL;
	    String tooltip = SystemResources.ACTION_CASCADING_OPEN_TOOLTIP;
		SystemEditFilesAction action = new SystemEditFilesAction(label, tooltip, shell);
		
		// add action
		menu.add(ISystemContextMenuConstants.GROUP_OPEN, action);
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
	 * Create actions when one file has been selected.
	 */
	public void createForSingleSelection(SystemMenuManager menu, Object element, Shell shell, String menuGroup) {
		IRemoteFile remoteFile = null;

		if ((element == null) || !(element instanceof IRemoteFile)) {
			return;
		}
		else {
			remoteFile = (IRemoteFile)element;
		}

		IEditorDescriptor defaultEditor = getDefaultTextEditor();
		
		IEditorDescriptor preferredEditor = getPreferredEditor(remoteFile); // may be null
		
		Object[] editors = registry.getEditors(remoteFile.getName());
		Collections.sort(Arrays.asList(editors), comparer);

		boolean defaultFound = false;
		
		//Check that we don't add it twice. This is possible
		//if the same editor goes to two mappings.
		ArrayList alreadyMapped = new ArrayList();

		for (int i = 0; i < editors.length; i++) {
			
			IEditorDescriptor editor = (IEditorDescriptor) editors[i];
			
			if (!alreadyMapped.contains(editor)) {
				
				createEditAction(menu, shell, remoteFile, editor, preferredEditor);
				
				// remember if we find default text editor
				if (defaultEditor != null && editor.getId().equals(defaultEditor.getId())) {
					defaultFound = true;
				}
				
				alreadyMapped.add(editor);
			}		
		}
		
		// only add a separator if there are associated editors
		if (editors.length > 0) {
			SystemSeparatorAction sep = new SystemSeparatorAction(shell);
			menu.add(ISystemContextMenuConstants.GROUP_OPENWITH, sep);
		}

		// add default text editor if it was not already associated
		if (!defaultFound && defaultEditor != null) {
			createEditAction(menu, shell, remoteFile, defaultEditor, preferredEditor);
		}

		// add system editor (should never be null)
		IEditorDescriptor descriptor = registry.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
		createEditAction(menu, shell, remoteFile, descriptor, preferredEditor);
		
		/* DKM - 56067
		// add system in-place editor (can be null)
		descriptor = registry.findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
		
		if (descriptor != null) {
			createEditAction(menu, shell, remoteFile, descriptor, preferredEditor);
		}
		*/
		
		// TODO: default action
		// createDefaultEditAction(menu, file);
	}
	
	/**
	 * Creates an edit action for a specific editor. If the editor is the preferred editor, then it is selected.
	 * @param menu the menu manager.
	 * @param shell the shell.
	 * @param remoteFile the remote file.
	 * @param descriptor the editor descriptor.
	 * @param preferredEditor the descriptor of the preferred editor, or <code>null</code>.
	 */
	private void createEditAction(SystemMenuManager menu, Shell shell, IRemoteFile remoteFile, IEditorDescriptor descriptor, final IEditorDescriptor preferredEditor) {
		String editorId = descriptor.getId();
		
		String label = descriptor.getLabel();
		ImageDescriptor image = getImageDescriptor(remoteFile, descriptor);
		
		// create action
		SystemEditFileAction action = new SystemEditFileAction(label, label, image, IAction.AS_RADIO_BUTTON, shell, editorId);
		
		// check if editor is the preferred editor
		boolean isPreferred = preferredEditor != null && editorId.equals(preferredEditor.getId());
		
		// mark action as checked or not
		action.setChecked(isPreferred);
		
		// add action
		menu.add(ISystemContextMenuConstants.GROUP_OPENWITH, action);
	}
	
	
	/**
	 * Returns the preferred editor for the remote file. If the remote file has a cached local resource,
	 * then returns the default editor associated with that resource, by calling <code>IDE.getDefaultEditor(IFile)</code>.
	 * Otherwise, get the default editor associated with that remote file name from the editor registry.
	 * @param remoteFile the remote file.
	 * @return the preferred editor for the remote file, or <code>null</code> if none.
	 */
	private IEditorDescriptor getPreferredEditor(IRemoteFile remoteFile) {
	
		IFile localFile = getLocalResource(remoteFile);
		
		if (localFile == null) {
			return registry.getDefaultEditor(remoteFile.getName());
		}
		else {
			return IDE.getDefaultEditor(localFile);
		}
	}
	
	/**
	 * Get the local cache of the remote file, or <code>null</code> if none.
	 * @param remoteFile the remote file.
	 * @return the local cached resource, or <code>null</code> if none.
	 */
	private IFile getLocalResource(IRemoteFile remoteFile) 
	{
	    return (IFile)UniversalFileTransferUtility.getTempFileFor(remoteFile);
	}
	
	/**
	 * Returns the image descriptor for the given editor descriptor. If the editor descriptor is
	 * <code>null</code>, returns the image descriptor for the remote file name.
	 * @param remoteFile the remote file.
	 */
	private ImageDescriptor getImageDescriptor(IRemoteFile remoteFile, IEditorDescriptor editorDesc) {
		ImageDescriptor imageDesc = null;
		
		if (editorDesc == null) {
			imageDesc = registry.getImageDescriptor(remoteFile.getName());
		}
		else {
			imageDesc = editorDesc.getImageDescriptor();
		}
		
		if (imageDesc == null) {
			
			if (editorDesc.getId().equals(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID))
				imageDesc = registry.getSystemExternalEditorImageDescriptor(remoteFile.getName());
		}
		
		return imageDesc;
	}
}