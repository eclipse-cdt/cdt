package org.eclipse.cdt.internal.ui.util;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.spawner.ProcessFactory;

public class EditorUtility {

	private EditorUtility () {
	}

	public static IEditorPart openInEditor (IFile file) throws PartInitException {
		IWorkbenchWindow window= CUIPlugin.getDefault().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage p= window.getActivePage();
			if (p != null) {
				return p.openEditor(file);
			}
		}
		return null;
	}

	public static IEditorPart openInEditor (ICElement element) throws PartInitException {
		IResource res = null;
		try {
			res = element.getUnderlyingResource();
		} catch (CModelException e) {
		}

		// Treat binary differently
		if (element instanceof IBinary) {
			IStorage store = getStorage((IBinary)element);
			if (store != null) {
				return openInEditor(store, element.getElementName());
			}
		}

		if (res != null && res instanceof IFile) {
			IEditorPart editor =  openInEditor((IFile)res);
			if (editor instanceof CEditor) {
				CEditor e = (CEditor)editor;
				StructuredSelection selection = new StructuredSelection(element);
				e.selectionChanged (new SelectionChangedEvent (e.getOutlinePage (), selection));
			}
			return editor;
		}
		return null;
	}

	public static IEditorPart openInEditor (IPath path) throws PartInitException {
		IFile f = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
		if (f == null) {
			IStorage s = new FileStorage(path);
			return openInEditor(s, path.lastSegment());
		}
		return openInEditor(f);
	}

	public static IEditorPart openInEditor (IStorage store, String name) throws PartInitException {
		IEditorInput ei = new ExternalEditorInput(store);
		IWorkbenchWindow window= CUIPlugin.getDefault().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage p = window.getActivePage();
			if (p != null) {
				return p.openEditor(ei, getEditorID(name));
			}
		}
		return null;
	}

	public static String getEditorID(String name) {
		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
		if (registry != null) {
			IEditorDescriptor descriptor = registry.getDefaultEditor(name);
			if (descriptor != null) {
				return descriptor.getId();
			} else {
				return registry.getDefaultEditor().getId();
			}
		}
		return null;
	}
	
	/** 
	 * Selects a C Element in an editor
	 */	
	public static void revealInEditor(IEditorPart part, ICElement element) {
		if (element != null && part instanceof CEditor) {
			//((CEditor) part).setSelection(element);
		}
	}

	public static IStorage getStorage(IBinary bin) {
		IStorage store = null;
		Process objdump = null;
		IPath path;
		IResource file = null;
		try {
			file = bin.getResource();
		} catch (CModelException e1) {
		}
		if (file == null)
			return store;
		path = file.getLocation();
		try {
			String[] args = new String[] {"objdump", "-CxS", path.toOSString()};
			objdump = ProcessFactory.getFactory().exec(args);
			StringBuffer buffer = new StringBuffer();
			BufferedReader stdout =
				new BufferedReader(new InputStreamReader(objdump.getInputStream()));
			char[] buf = new char[128];
			while (stdout.read(buf, 0, buf.length) != -1) {
				buffer.append(buf);
			}
			store = new FileStorage(new ByteArrayInputStream(buffer.toString().getBytes()), path);
		} catch (SecurityException e) {
		} catch (IndexOutOfBoundsException e) {
		} catch (NullPointerException e) {
		} catch (IOException e) {
		} finally {
			if (objdump != null) {
				objdump.destroy();
			}
		}
		return store;
	}
}
