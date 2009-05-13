/********************************************************************************
 * Copyright (c) 2009 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Mike Kucera.
 *
 * Contributors:
 * Mike Kucera         (IBM) - [241316] [efs] Cannot restore editors for RSE/EFS-backed resources
 * David McKnight      (IBM) - [241316] [efs] Cannot restore editors for RSE/EFS-backed resources
 ********************************************************************************/
package org.eclipse.rse.internal.efs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.UIJob;

public class RemoteEditorManager implements ISaveParticipant, IResourceChangeListener, IWorkbenchListener {
	public static final String PREFERENCE_EDITOR_LIST = "org.eclipse.rse.internal.efs.EditorSaver.preferenceEditorList"; //$NON-NLS-1$

	private static RemoteEditorManager defaultInstance;

	private Map projectNameToUriMap = null;

	private RemoteEditorManager() {}

	public static synchronized RemoteEditorManager getDefault() {
		if(defaultInstance == null)
			defaultInstance = new RemoteEditorManager();
		return defaultInstance;
	}


	/**
	 * Restores remote editors when a remote project is opened.
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		String pref = RSEUIPlugin.getDefault().getPreferenceStore().getString(PREFERENCE_EDITOR_LIST);
		Map projectNameToUriMap = getProjectNameToUriMap(pref);
		if(projectNameToUriMap.isEmpty())
			return;

		IResourceDelta[] children = event.getDelta().getAffectedChildren();
		for(int i = 0; i < children.length; i++) {
			IResourceDelta delta = children[i];

			// if a project has just been opened
			if((delta.getFlags() & IResourceDelta.OPEN) != 0) {
				IProject project = delta.getResource().getProject();
				if(project.isOpen()) {

					// restore remote editors for the project
					List uris = (List) projectNameToUriMap.remove(project.getName());
					if(uris != null) {
						for(Iterator iter = uris.iterator(); iter.hasNext();) {
							final String uriString = (String) iter.next();

							Job job = new UIJob("Restore Remote Editor") { //$NON-NLS-1$
								public IStatus runInUIThread(IProgressMonitor monitor) {
									if(monitor.isCanceled())
										return Status.OK_STATUS;

									try {
										// got this code from http://wiki.eclipse.org/FAQ_How_do_I_open_an_editor_programmatically%3F
										IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
										IFileStore fileStore = EFS.getStore(new URI(uriString));
										IDE.openEditorOnFileStore(page, fileStore);
									} catch (URISyntaxException e) {
										return Activator.errorStatus(e);
									} catch (PartInitException e) {
										return Activator.errorStatus(e);
									} catch (CoreException e) {
										return Activator.errorStatus(e);
									}
									return Status.OK_STATUS;
								}
							};
							job.schedule();

						}
					}
				}
			}
		}

	}

	/**
	 * Saves the URIs of remote resources that are open in editors into the
	 * plugin's preference store.
	 */
	public synchronized void saveRemoteEditors() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = wb.getWorkbenchWindows();
		if(windows.length > 0) {
			String list = generateEditorList();
			RSEUIPlugin.getDefault().getPreferenceStore().putValue(PREFERENCE_EDITOR_LIST, list);
		}
		projectNameToUriMap = null;
	}

	private Map getProjectNameToUriMap(String raw) {
		if(projectNameToUriMap == null) {
			projectNameToUriMap = new HashMap();

			if(raw == null || raw.length() == 0)
				return projectNameToUriMap;

			int index = 0;
			while(true) {
				int i = raw.indexOf(IPath.SEPARATOR, index);
				if(i < 0) break;
				String projectName = raw.substring(index, i);
				index = i + 1;
				i = raw.indexOf(' ', index);
				if(i < 0) break;
				String uriString = raw.substring(index, i);
				index = i + 1;

				List uris = (List) projectNameToUriMap.get(projectName);
				if(uris == null) {
					uris = new LinkedList();
					projectNameToUriMap.put(projectName, uris);
				}
				uris.add(uriString);
			}
		}
		return projectNameToUriMap;
	}


	private String generateEditorList() {
		final StringBuffer sb = new StringBuffer();

		forEachOpenRemoteEditor(new IEditorCallback() {
			public void apply(IWorkbenchPage page, IEditorPart editor, IFile file) {
				IProject project = file.getProject();
				URI uri = file.getLocationURI();
				sb.append(project.getName());
				sb.append(IPath.SEPARATOR); // not allowed in resource names
				sb.append(uri);
				sb.append(' '); // not allowed in URIs
			}
		});

		return sb.toString();
	}


	private static interface IEditorCallback {
		public void apply(IWorkbenchPage page, IEditorPart editor, IFile file);
	}

	private static void forEachOpenRemoteEditor(IEditorCallback callback) {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = wb.getWorkbenchWindows();
		for (int w = 0; w < windows.length; w++){
			IWorkbenchWindow win = windows[w];
			IWorkbenchPage[] pages = win.getPages();
			for (int p = 0; p < pages.length; p++){
				IWorkbenchPage page = pages[p];
				IEditorReference[] activeReferences = page.getEditorReferences();
				for (int er = 0; er < activeReferences.length; er++){
					IEditorReference editorReference = activeReferences[er];

					try {
						IEditorInput input = editorReference.getEditorInput();
						if (input instanceof FileEditorInput){
							IFile file = ((FileEditorInput)input).getFile();
							URI uri = file.getLocationURI();
							if ("rse".equals(uri.getScheme())) { //$NON-NLS-1$
								IEditorPart editor = editorReference.getEditor(false);
								callback.apply(page, editor, file);
							}
						}
					} catch (PartInitException e){
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Close each editor that is open for any file that uses "rse" as it's uri scheme
	 * @return true if successful, false otherwise
	 */
	public boolean closeRemoteEditors() {
		boolean result = true;
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = wb.getWorkbenchWindows();
		for (int w = 0; w < windows.length; w++){
			IWorkbenchWindow win = windows[w];
			IWorkbenchPage[] pages = win.getPages();
			for (int p = 0; p < pages.length && result; p++){
				IWorkbenchPage page = pages[p];
				IEditorReference[] activeReferences = page.getEditorReferences();
				for (int er = 0; er < activeReferences.length; er++){
					IEditorReference editorReference = activeReferences[er];

					try {
						IEditorInput input = editorReference.getEditorInput();
						if (input instanceof FileEditorInput){
							IFile file = ((FileEditorInput)input).getFile();
							URI uri = file.getLocationURI();
							if ("rse".equals(uri.getScheme())) { //$NON-NLS-1$
								IEditorPart editor = editorReference.getEditor(false);

								// close the editor
								result = page.closeEditor(editor, true);
							}
						}
					} catch (PartInitException e){
						e.printStackTrace();
					}
				}
			}
		}
		return result;
	}


	public void saving(ISaveContext context) throws CoreException {
		saveRemoteEditors();
	}


	public void doneSaving(ISaveContext context) {
	}

	public void prepareToSave(ISaveContext context) throws CoreException {
	}

	public void rollback(ISaveContext context) {
	}


	// for IWorkbenchListener
	public void postShutdown(IWorkbench workbench) {
	}

	// for IWorkbenchListener
	public boolean preShutdown(IWorkbench workbench, boolean forced) {
		saveRemoteEditors();
		return closeRemoteEditors();
	}

}
