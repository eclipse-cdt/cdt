/********************************************************************************
 * Copyright (c) 2013 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Xuan Chen        (IBM)        - [399101] RSE edit actions on local files that map to actually workspace resources should not use temp files
 ********************************************************************************/

package org.eclipse.rse.internal.files.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.part.FileEditorInput;

public class SystemFileActionUtility {
	
	static void openEditor(IFile localFile, boolean readOnly) throws PartInitException {
		IEditorDescriptor editorDescriptor = null;
		
		try {
			editorDescriptor = IDE.getEditorDescriptor(localFile);
		} catch (PartInitException e) {
			SystemBasePlugin.logError(e.getLocalizedMessage(), e);
		}
		
		if (editorDescriptor == null) {
			if (PlatformUI.isWorkbenchRunning())
			{
				IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
				editorDescriptor = registry.findEditor("org.eclipse.ui.DefaultTextEditor"); //$NON-NLS-1$
			}
		}
		
		//This file is from local connection, and it is inside a project in the work
		IWorkbenchPage activePage = SystemBasePlugin.getActiveWorkbenchWindow().getActivePage();
		
		ResourceAttributes attr = localFile.getResourceAttributes();
		if (attr!=null) {
			attr.setReadOnly(readOnly);
			try
			{
				localFile.setResourceAttributes(attr);
			}
			catch (Exception e)
			{
				SystemBasePlugin.logError(e.getLocalizedMessage(), e);
			}
		}

		// set editor as preferred editor for this file
		String editorId = null;
		if (editorDescriptor != null) {
				editorId = editorDescriptor.getId();
		}

		IDE.setDefaultEditor(localFile, editorId);
		if (editorDescriptor.isOpenExternal()){
			SystemFileActionUtility.openSystemEditor(localFile); // opening regular way doesn't work anymore
		}
		else {
			FileEditorInput finput = new FileEditorInput(localFile);
			IEditorPart editor = null;
			// check for files already open
			if (editorDescriptor != null && editorDescriptor.isOpenExternal()){
				editor = ((WorkbenchPage)activePage).openEditorFromDescriptor(new FileEditorInput(localFile), editorDescriptor, true, null);
			}
			else {
				editor =  activePage.openEditor(finput, editorDescriptor.getId());
			}
	
			return;
		}
	}
	
	static void openSystemEditor(IFile localFile) throws PartInitException {
		IEditorDescriptor editorDescriptor = null;
		
		try {
			editorDescriptor = IDE.getEditorDescriptor(localFile);
		} catch (PartInitException e) {
			SystemBasePlugin.logError(e.getLocalizedMessage(), e);
		}
		
		if (editorDescriptor == null) {
			if (PlatformUI.isWorkbenchRunning())
			{
				IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
				editorDescriptor = registry.findEditor("org.eclipse.ui.DefaultTextEditor"); //$NON-NLS-1$
			}
		}
		
		//This file is from local connection, and it is inside a project in the work
		IWorkbenchPage activePage = SystemBasePlugin.getActiveWorkbenchWindow().getActivePage();

		// set editor as preferred editor for this file
		String editorId = null;
		if (editorDescriptor != null) {
				editorId = editorDescriptor.getId();
		}

		IDE.setDefaultEditor(localFile, editorId);
		
		FileEditorInput fileInput = new FileEditorInput(localFile);
		activePage.openEditor(fileInput, IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
	
		return;
		
	}
	
	/**
	 * Open the in place editor
	 */
	public static void openInPlaceEditor(IFile localFile) throws PartInitException
	{
		IWorkbenchPage activePage = SystemBasePlugin.getActiveWorkbenchWindow().getActivePage();
		FileEditorInput fileInput = new FileEditorInput(localFile);
		activePage.openEditor(fileInput, IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
	}
	
	public static void hackOpenEditor(IFile localFile, IEditorDescriptor descriptor, boolean readOnly) throws PartInitException
	{
		//This file is from local connection, and it is inside a project in the work
		IWorkbenchPage activePage = SystemBasePlugin.getActiveWorkbenchWindow().getActivePage();
		ResourceAttributes attr = localFile.getResourceAttributes();
		if (attr!=null) {
			attr.setReadOnly(readOnly);
			try	{
				localFile.setResourceAttributes(attr);
			}
			catch (Exception e)
			{
				SystemBasePlugin.logError(e.getLocalizedMessage(), e);
			}
		}

		// set editor as preferred editor for this file
		String editorId = descriptor.getId();
		IDE.setDefaultEditor(localFile, editorId);

		FileEditorInput finput = new FileEditorInput(localFile);

		IEditorPart editor = null;
		if (descriptor.isOpenExternal()){
			editor = ((WorkbenchPage)activePage).openEditorFromDescriptor(new FileEditorInput(localFile), descriptor, true, null);
		}
		else {
			editor =  activePage.openEditor(finput, descriptor.getId());
		}
	}
	
	static IFile getProjectFileForLocation(String absolutePath)
	{
		IPath workspacePath = new Path(absolutePath);
		IFile file = SystemBasePlugin.getWorkspaceRoot().getFileForLocation(workspacePath);
		return file;
	}
	

}
