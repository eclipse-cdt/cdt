package org.eclipse.cdt.internal.ui.util;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.internal.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public class EditorUtility {

	private EditorUtility () {
	}

	/** 
	 * Tests if a cu is currently shown in an editor
	 * @return the IEditorPart if shown, null if element is not open in an editor
	 */     
	public static IEditorPart isOpenInEditor(Object inputElement) {
		IEditorInput input = null;
                
		try {
			input = getEditorInput(inputElement);
		} catch (CModelException x) {
			//CUIPlugin.log(x.getStatus());
		}
                
		if (input != null) {
			IWorkbenchPage p= CUIPlugin.getActivePage();
			if (p != null) {
				return p.findEditor(input);
			}
		}
                
		return null;
	}


	/**
	 * Opens a Java editor for an element such as <code>IJavaElement</code>, <code>IFile</code>, or <code>IStorage</code>.
	 * The editor is activated by default.
	 * @return the IEditorPart or null if wrong element type or opening failed
	 */
	public static IEditorPart openInEditor(Object inputElement) throws CModelException, PartInitException {
		return openInEditor(inputElement, true);
	}
                
	/**
	 * Opens a Java editor for an element (IJavaElement, IFile, IStorage...)
	 * @return the IEditorPart or null if wrong element type or opening failed
	 */
	public static IEditorPart openInEditor(Object inputElement, boolean activate) throws CModelException, PartInitException {
                
		if (inputElement instanceof IFile) {
			return openInEditor((IFile) inputElement, activate);
		}
                
		IEditorInput input = getEditorInput(inputElement);
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fileInput= (IFileEditorInput) input;
			return openInEditor(fileInput.getFile(), activate);
		}
                
		if (input != null) {
			return openInEditor(input, getEditorID(input, inputElement), activate);
		}
                        
		return null;
	}

	/** 
	 * Selects a C Element in an editor
	 */     
	public static void revealInEditor(IEditorPart part, ICElement element) {
		if (element != null && part instanceof CEditor) {
			((CEditor) part).setSelection(element);
		}
	}

	private static IEditorPart openInEditor(IFile file, boolean activate) throws PartInitException {
		if (file != null) {
			IWorkbenchPage p= CUIPlugin.getActivePage();
			if (p != null) {
				IEditorPart editorPart= p.openEditor(file, null, activate);
				initializeHighlightRange(editorPart);
				return editorPart;
			}
		}
		return null;
	}

	private static IEditorPart openInEditor(IEditorInput input, String editorID, boolean activate) throws PartInitException {
		if (input != null) {
			IWorkbenchPage p= CUIPlugin.getActivePage();
			if (p != null) {
				IEditorPart editorPart= p.openEditor(input, editorID, activate);
				initializeHighlightRange(editorPart);
				return editorPart;
			}
		}
		return null;
	}

	private static void initializeHighlightRange(IEditorPart editorPart) {
		if (editorPart instanceof ITextEditor) {
			//TogglePresentationAction toggleAction= new TogglePresentationAction();
			// Initialize editor
			//toggleAction.setEditor((ITextEditor)editorPart);
			// Reset action
			//toggleAction.setEditor(null);
		}
	}

	private static IEditorInput getEditorInput(ICElement element) throws CModelException {
		while (element != null) {
			if (element instanceof IWorkingCopy && ((IWorkingCopy) element).isWorkingCopy()) 
				element= ((IWorkingCopy) element).getOriginalElement();
 
 			if (element instanceof ISourceReference) {
 				ITranslationUnit tu = ((ISourceReference)element).getTranslationUnit();
 				if (tu != null) {
 					element = tu;                    
 				}
 			}

			if (element instanceof ITranslationUnit) {
				ITranslationUnit unit= (ITranslationUnit) element;
				IResource resource= unit.getResource();
				if (resource instanceof IFile)
					return new FileEditorInput((IFile) resource);
			}
                        
			if (element instanceof IBinary) {
				//return new InternalClassFileEditorInput((IBinary) element);
				return new ExternalEditorInput(getStorage((IBinary)element));
			}
                        
			element= element.getParent();
		}
                
		return null;
	}

	public static IEditorInput getEditorInput(Object input) throws CModelException {
		if (input instanceof ICElement) {
			return getEditorInput((ICElement) input);
		}
                        
		if (input instanceof IFile) { 
			return new FileEditorInput((IFile) input);
		}

		if (input instanceof IStorage) { 
			//return new JarEntryEditorInput((IStorage)input);
			return new ExternalEditorInput((IStorage)input);
		}
		return null;
	}


	/**
	 * If the current active editor edits a c element return it, else
	 * return null
	 */
	public static ICElement getActiveEditorCInput() {
		IWorkbenchPage page= CUIPlugin.getActivePage();
		if (page != null) {
			IEditorPart part= page.getActiveEditor();
			if (part != null) {
				IEditorInput editorInput= part.getEditorInput();
				if (editorInput != null) {
					return (ICElement)editorInput.getAdapter(ICElement.class);
				}
			}
		}
		return null;    
	}
        
	/** 
	 * Gets the working copy of an compilation unit opened in an editor
	 * @param part the editor part
	 * @param cu the original compilation unit (or another working copy)
	 * @return the working copy of the compilation unit, or null if not found
	*/     
//	public static ITranslationUnit getWorkingCopy(ITranslationUnit cu) {
//		if (cu == null)
//			return null;
//		if (cu.isWorkingCopy())
//			return cu;
//
//		return (ITranslationUnit)cu.findSharedWorkingCopy(CUIPlugin.getBufferFactory());
//	}


	/**
	 * Returns the translation unit for the given c element.
	 * @param element the c element whose compilation unit is searched for
	 * @return the compilation unit of the given java element
	 */
	private static ITranslationUnit getTranslationUnit(ICElement element) {
                
		if (element == null)
			return null;

		int type= element.getElementType();
		if (ICElement.C_UNIT == type) {
			return (ITranslationUnit) element;
		}
		if (ICElement.C_BINARY == type) {
			return null;
		}
		if (element instanceof ISourceReference) {
			return ((ISourceReference) element).getTranslationUnit();
		}
		return getTranslationUnit(element.getParent());
	}


//	public static IEditorPart openInEditor (IFile file) throws PartInitException {
//		IWorkbenchWindow window= CUIPlugin.getDefault().getActiveWorkbenchWindow();
//		if (window != null) {
//			IWorkbenchPage p= window.getActivePage();
//			if (p != null) {
//				return p.openEditor(file);
//			}
//		}
//		return null;
//	}
//
//
//	public static IEditorPart openInEditor (IPath path) throws PartInitException {
//		IFile f = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
//		if (f == null) {
//			IStorage s = new FileStorage(path);
//			return openInEditor(s, path.lastSegment());
//		}
//		return openInEditor(f);
//	}
//
//	public static IEditorPart openInEditor (IStorage store, String name) throws PartInitException {
//		IEditorInput ei = new ExternalEditorInput(store);
//		IWorkbenchWindow window= CUIPlugin.getDefault().getActiveWorkbenchWindow();
//		if (window != null) {
//			IWorkbenchPage p = window.getActivePage();
//			if (p != null) {
//				return p.openEditor(ei, getEditorID(name));
//			}
//		}
//		return null;
//	}
//

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

	public static String getEditorID(IEditorInput input, Object inputObject) {
		return getEditorID(input.getName());
	}
	
	public static IStorage getStorage(IBinary bin) {
		IStorage store = null;
		Process objdump = null;
		IPath path;
		IResource file = null;
		file = bin.getResource();
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
