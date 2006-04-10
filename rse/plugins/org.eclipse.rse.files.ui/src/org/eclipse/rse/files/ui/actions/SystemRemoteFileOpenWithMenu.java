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
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.files.ui.resources.UniversalFileTransferUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;


/**
 * Open with menu class for remote files
 */
public class SystemRemoteFileOpenWithMenu extends ContributionItem 
{
	protected IWorkbenchPage page;
	protected IRemoteFile _remoteFile;
	protected IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();

	private static Hashtable imageCache = new Hashtable(11);
	 
	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".OpenWithMenu";//$NON-NLS-1$

	/*
	 * Compares the labels from two IEditorDescriptor objects 
	 */
	private static final Comparator comparer = new Comparator() 
	{
		private Collator collator = Collator.getInstance();

		public int compare(Object arg0, Object arg1) {
			String s1 = ((IEditorDescriptor)arg0).getLabel();
			String s2 = ((IEditorDescriptor)arg1).getLabel();
			return collator.compare(s1, s2);
		}
	}; 


/**
 * Constructs a new instance of <code>SystemOpenWithMenu</code>.  
 */
public SystemRemoteFileOpenWithMenu() 
{
	super(ID);
	this.page = null;
	_remoteFile = null;	
}

/*
 * Initializes the IRemoteFile
 */
public void updateSelection(IStructuredSelection selection)
{
	if (selection.size() == 1)
	{
		_remoteFile = (IRemoteFile)selection.getFirstElement();
	}
}

/**
 * Returns an image to show for the corresponding editor descriptor.
 *
 * @param editorDesc the editor descriptor, or null for the system editor
 * @return the image or null
 */
protected Image getImage(IEditorDescriptor editorDesc) {
	ImageDescriptor imageDesc = getImageDescriptor(editorDesc);
	if (imageDesc == null) {
		return null;
	}
	Image image = (Image) imageCache.get(imageDesc);
	if (image == null) {
		image = imageDesc.createImage();
		imageCache.put(imageDesc, image);
	}
	return image;
}

private String getFileName()
{
	return _remoteFile.getName();
}

/**
 * Returns the image descriptor for the given editor descriptor,
 * or null if it has no image.
 */
private ImageDescriptor getImageDescriptor(IEditorDescriptor editorDesc) {
	ImageDescriptor imageDesc = null;
	if (editorDesc == null) {
		imageDesc = registry.getImageDescriptor(getFileName());
	}
	else {
		imageDesc = editorDesc.getImageDescriptor();
	}
	if (imageDesc == null) {
		if (editorDesc.getId().equals(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID))
			imageDesc = registry.getSystemExternalEditorImageDescriptor(getFileName());
	}
	return imageDesc;
}
/**
 * Creates the menu item for the editor descriptor.
 *
 * @param menu the menu to add the item to
 * @param descriptor the editor descriptor, or null for the system editor
 * @param preferredEditor the descriptor of the preferred editor, or <code>null</code>
 */
protected void createMenuItem(Menu menu, final IEditorDescriptor descriptor, final IEditorDescriptor preferredEditor) 
{
	// XXX: Would be better to use bold here, but SWT does not support it.
	final MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
	boolean isPreferred = preferredEditor != null && descriptor.getId().equals(preferredEditor.getId());
	menuItem.setSelection(isPreferred);
	menuItem.setText(descriptor.getLabel());
	Image image = getImage(descriptor);
	if (image != null) {
		menuItem.setImage(image);
	}
	Listener listener = new Listener() {
		public void handleEvent(Event event) {
			switch (event.type) {
				case SWT.Selection:
					if(menuItem.getSelection())
					{
						openEditor(_remoteFile, descriptor);
					}
					break;
			}
		}
	};
	menuItem.addListener(SWT.Selection, listener);
}


protected void openEditor(IRemoteFile file, IEditorDescriptor descriptor)
{
	SystemEditableRemoteFile editableFile = new SystemEditableRemoteFile(file, descriptor.getId());
	if (descriptor.getId().equals(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID))
	{
		editableFile.openInSystemEditor(SystemBasePlugin.getActiveWorkbenchShell());
	}
	else
	{
		editableFile.open(SystemBasePlugin.getActiveWorkbenchShell());
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
 * Returns the preferred editor for the remote file. If the remote file has a cached local resource,
 * then returns the default editor associated with that resource, by calling <code>IDE.getDefaultEditor(IFile)</code>.
 * Otherwise, get the default editor associated with that remote file name from the editor registry.
 * @param remoteFile the remote file.
 * @return the preferred editor for the remote file, or <code>null</code> if none.
 */
protected IEditorDescriptor getPreferredEditor(IRemoteFile remoteFile) {

	IFile localFile = getLocalResource(remoteFile);
	
	if (localFile == null) {
		return registry.getDefaultEditor(remoteFile.getName());
	}
	else {
		return IDE.getDefaultEditor(localFile);
	}
}


protected IEditorDescriptor getDefaultEditor(IRemoteFile remoteFile)
{
	IFile localFile = getLocalResource(remoteFile);
	
	if (localFile == null) {
		return registry.getDefaultEditor(remoteFile.getName());
	}
	else {
		return IDE.getDefaultEditor(localFile);
	}
}

protected void setDefaultEditor(IRemoteFile remoteFile, String editorId)
{
	IFile localFile = getLocalResource(remoteFile);
	
	if (localFile == null) {
		registry.setDefaultEditor(remoteFile.getName(), editorId);
	}
	else {
		IDE.setDefaultEditor(localFile, editorId);
	}
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

/* (non-Javadoc)
 * Fills the menu with perspective items.
 */
public void fill(Menu menu, int index) 
{
	if (_remoteFile == null) {
		return;
	}

	IEditorDescriptor defaultEditor = registry.findEditor("org.eclipse.ui.DefaultTextEditor"); // may be null
	IEditorDescriptor preferredEditor = getPreferredEditor(_remoteFile); // may be null
	
	Object[] editors = registry.getEditors(getFileName());
	Collections.sort(Arrays.asList(editors), comparer);

	boolean defaultFound = false;
	
	//Check that we don't add it twice. This is possible
	//if the same editor goes to two mappings.
	ArrayList alreadyMapped= new ArrayList();

	for (int i = 0; i < editors.length; i++) {
		IEditorDescriptor editor = (IEditorDescriptor) editors[i];
		if(!alreadyMapped.contains(editor)){
			createMenuItem(menu, editor, preferredEditor);
			if (defaultEditor != null && editor.getId().equals(defaultEditor.getId()))
				defaultFound = true;
			alreadyMapped.add(editor);
		}		
	}

	// Only add a separator if there is something to separate
	if (editors.length > 0)
		new MenuItem(menu, SWT.SEPARATOR);

	// Add default editor. Check it if it is saved as the preference.
	if (!defaultFound && defaultEditor != null) {
		createMenuItem(menu, defaultEditor, preferredEditor);
	}

	// Add system editor (should never be null)
	IEditorDescriptor descriptor = registry.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
	createMenuItem(menu, descriptor, preferredEditor);
	
	//DKM- disable inplace editor for now
	/*
	// Add system in-place editor (can be null)
	descriptor = registry.findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
	if (descriptor != null) {
		createMenuItem(menu, descriptor, preferredEditor);
	}
	*/	
	createDefaultMenuItem(menu, _remoteFile);
}


/* (non-Javadoc)
 * Returns whether this menu is dynamic.
 */
public boolean isDynamic() 
{
	return true;
}


/**
 * Creates the menu item for clearing the current selection.
 *
 * @param menu the menu to add the item to
 * @param file the file bing edited
 * @param registry the editor registry
 */
protected void createDefaultMenuItem(Menu menu, final IRemoteFile file) 
{
	final MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
	IEditorDescriptor defaultEditor = getDefaultEditor(file);
	menuItem.setSelection(defaultEditor == null);
	menuItem.setText(FileResources.DefaultEditorDescription_name); 
	
	Listener listener = new Listener() 
	{
		public void handleEvent(Event event) 
		{
			switch (event.type) 
			{
				case SWT.Selection:
					if(menuItem.getSelection()) 
					{
						setDefaultEditor(file, null);
		
						IEditorDescriptor defaultEditor = getDefaultEditor(file);
						openEditor(file, defaultEditor);
					
					}
					break;
			}
		}
	};
	
	menuItem.addListener(SWT.Selection, listener);
}

}