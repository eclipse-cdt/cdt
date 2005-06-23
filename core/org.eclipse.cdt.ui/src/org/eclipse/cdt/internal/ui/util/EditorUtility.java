/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.editor.ITranslationUnitEditorInput;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
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

	/**
	 * The ID of the default text editor
	 */
	public static final String DEFAULT_TEXT_EDITOR_ID = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$

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
		//if (input instanceof IFileEditorInput) {
		//	IFileEditorInput fileInput= (IFileEditorInput) input;
		//	return openInEditor(fileInput.getFile(), activate);
		//}
                
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
		if (!file.getProject().isAccessible()){
			closedProject(file.getProject());
			return null;
		}
		
		if (file != null) {
		try {
				File tempFile = file.getRawLocation().toFile();
				
				if (tempFile != null){
					String canonicalPath = null;
					try {
						canonicalPath = tempFile.getCanonicalPath();
					} catch (IOException e1) {}
					
					if (canonicalPath != null){
						IPath path = new Path(canonicalPath);
						file = CUIPlugin.getWorkspace().getRoot().getFileForLocation(path);
					}
				}
				
				IEditorInput input = getEditorInput(file);
				if (input != null) {
					return openInEditor(input, getEditorID(input, file), activate);
				}
			} catch (CModelException e) {}
		}
		return null;
	}

	/**
	 * @param project
	 * 
	 */
	private static void closedProject(IProject project) {
		MessageBox errorMsg = new MessageBox(CUIPlugin.getActiveWorkbenchShell(), SWT.ICON_ERROR | SWT.OK);
		errorMsg.setText(CUIPlugin.getResourceString("EditorUtility.closedproject")); //$NON-NLS-1$
		String desc= CUIPlugin.getResourceString("Editorutility.closedproject.description"); //$NON-NLS-1$
		errorMsg.setMessage (MessageFormat.format(desc, new Object[]{project.getName()})); //$NON-NLS-1$
		errorMsg.open();
		
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
				if (resource instanceof IFile) {
					return new FileEditorInput((IFile) resource);
				}
				return new ExternalEditorInput(unit, getStorage(unit));					
			}
                        
			if (element instanceof IBinary) {
				return new ExternalEditorInput(getStorage((IBinary)element), null);
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
			return new ExternalEditorInput((IStorage)input, null);
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
	public static ITranslationUnit getWorkingCopy(ITranslationUnit cu) {
		if (cu == null)
			return null;
		if (cu.isWorkingCopy())
			return cu;

		return cu.findSharedWorkingCopy(CUIPlugin.getDefault().getBufferFactory());
	}


//	/**
//	 * Returns the translation unit for the given c element.
//	 * @param element the c element whose compilation unit is searched for
//	 * @return the compilation unit of the given java element
//	 */
//	private static ITranslationUnit getTranslationUnit(ICElement element) {
//                
//		if (element == null)
//			return null;
//
//		int type= element.getElementType();
//		if (ICElement.C_UNIT == type) {
//			return (ITranslationUnit) element;
//		}
//		if (ICElement.C_BINARY == type) {
//			return null;
//		}
//		if (element instanceof ISourceReference) {
//			return ((ISourceReference) element).getTranslationUnit();
//		}
//		return getTranslationUnit(element.getParent());
//	}

	public static String getEditorID(String name) {
		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
		if (registry != null) {
			IEditorDescriptor descriptor = registry.getDefaultEditor(name);
			if (descriptor != null) {
				return descriptor.getId();
			}
			return registry.findEditor(DEFAULT_TEXT_EDITOR_ID).getId();
		}
		return null;
	}

	public static String getEditorID(IEditorInput input, Object inputObject) {
		String ID =  getEditorID(input.getName());

		if (!"org.eclipse.ui.DefaultTextEditor".equals(ID)) { //$NON-NLS-1$
			return ID;
		}

		// TODO:FIXME:HACK etc ...
		// Unfortunately unless specifying all of possible headers in the plugin.xml
		// and it is not possible(for example filenames: iostream, cstdlib, etc ...
		// We try this hack here.  This is to be remove when the Eclipse Platform
		// implement there contentious IContentType

		ITranslationUnit tunit = null;

		if (input instanceof IFileEditorInput) {
			IFileEditorInput editorInput = (IFileEditorInput)input;
			IFile file = editorInput.getFile();
			ICElement celement = CoreModel.getDefault().create(file);
			if (celement instanceof ITranslationUnit) {
				tunit = (ITranslationUnit)celement;
			}
		} else if (input instanceof ITranslationUnitEditorInput) {
			ITranslationUnitEditorInput editorInput = (ITranslationUnitEditorInput)input;
			tunit = editorInput.getTranslationUnit();
		}

		// Choose an a file base on the extension.
		if (tunit != null) {
			if (tunit.isCLanguage()) {
				return "org.eclipse.cdt.ui.editor.CEditor"; //$NON-NLS-1$
				//return getEditorID("No_ExIsTeNt_FiLe.c");//$NON-NLS-1$
			} else if (tunit.isCXXLanguage()) {
				return "org.eclipse.cdt.ui.editor.CEditor"; //$NON-NLS-1$
				//return getEditorID("No_ExIsTeNt_FiLe.cpp");//$NON-NLS-1$
			} else if (tunit.isASMLanguage()) {
				return "org.eclipse.cdt.ui.editor.asm.AsmEditor"; //$NON-NLS-1$
			}
		}

		return ID;
	}

	/**
	 * Maps the localized modifier name to a code in the same
	 * manner as #findModifier.
	 * 
	 * @return the SWT modifier bit, or <code>0</code> if no match was found
	 */
	public static int findLocalizedModifier(String token) {
		if (token == null)
			return 0;
		
		if (token.equalsIgnoreCase(Action.findModifierString(SWT.CTRL)))
			return SWT.CTRL;
		if (token.equalsIgnoreCase(Action.findModifierString(SWT.SHIFT)))
			return SWT.SHIFT;
		if (token.equalsIgnoreCase(Action.findModifierString(SWT.ALT)))
			return SWT.ALT;
		if (token.equalsIgnoreCase(Action.findModifierString(SWT.COMMAND)))
			return SWT.COMMAND;

		return 0;
	}

	/**
	 * Returns the modifier string for the given SWT modifier
	 * modifier bits.
	 * 
	 * @param stateMask	the SWT modifier bits
	 * @return the modifier string
	 * @since 2.1.1
	 */
	public static String getModifierString(int stateMask) {
		String modifierString= ""; //$NON-NLS-1$
		if ((stateMask & SWT.CTRL) == SWT.CTRL)
			modifierString= appendModifierString(modifierString, SWT.CTRL);
		if ((stateMask & SWT.ALT) == SWT.ALT)
			modifierString= appendModifierString(modifierString, SWT.ALT);
		if ((stateMask & SWT.SHIFT) == SWT.SHIFT)
			modifierString= appendModifierString(modifierString, SWT.SHIFT);
		if ((stateMask & SWT.COMMAND) == SWT.COMMAND)
			modifierString= appendModifierString(modifierString,  SWT.COMMAND);
		
		return modifierString;
	}

	/**
	 * Appends to modifier string of the given SWT modifier bit
	 * to the given modifierString.
	 * 
	 * @param modifierString	the modifier string
	 * @param modifier			an int with SWT modifier bit
	 * @return the concatenated modifier string
	 * @since 2.1.1
	 */
	private static String appendModifierString(String modifierString, int modifier) {
		if (modifierString == null)
			modifierString= ""; //$NON-NLS-1$
		String newModifierString= Action.findModifierString(modifier);
		if (modifierString.length() == 0)
			return newModifierString;
		return CEditorMessages.getFormattedString("EditorUtility.concatModifierStrings", new String[] {modifierString, newModifierString}); //$NON-NLS-1$
	}

	public static IStorage getStorage(IBinary bin) {
		IStorage store = null;
		try {
			store = new FileStorage (new ByteArrayInputStream(bin.getBuffer().getContents().getBytes()), bin.getPath());
		} catch (CModelException e) {
			// nothing;
		}
		return store;
	}
	
	public static IStorage getStorage(ITranslationUnit tu) {
		IStorage store = null;
		try {
			store = new FileStorage (new ByteArrayInputStream(tu.getBuffer().getContents().getBytes()), tu.getPath());
		} catch (CModelException e) {
			// nothing;
		}
		return store;
	}
}
