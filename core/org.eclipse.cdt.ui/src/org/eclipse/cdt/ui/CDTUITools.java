/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui;

import java.net.URI;

import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.IDocumentProvider;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.IColorManager;
import org.eclipse.cdt.internal.ui.editor.ITranslationUnitEditorInput;
import org.eclipse.cdt.internal.ui.text.asm.AsmPartitionScanner;
import org.eclipse.cdt.internal.ui.util.EditorUtility;

/**
 * This class provides utilities for clients of the CDT UI plug-in.
 * This class provides static methods for:
 * <ul>
 *  <li>opening an editor on a C model element.</li>
 *  <li>accessing working copy manager and document provider used with C model elements.</li>
 *  <li>accessing color manager used for syntax coloring of C/C++ files.</li>
 * </ul>
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 5.1
 */
public final class CDTUITools {

	private CDTUITools() {
		// prevent instantiation
	}
	
	/**
	 * Returns the color manager which is used to manage
	 * colors needed for syntax highlighting.
	 *
	 * @return the color manager to be used for C/C++ text viewers
	 */
	public static IColorManager getColorManager() {
		return CUIPlugin.getDefault().getTextTools().getColorManager();
	}

	/**
	 * Opens an editor on the given C model element in the active page. Valid are elements that are {@link ISourceReference}.
	 *
	 * @param element the input element
	 * @return returns the editor part of the opened editor or <code>null</code> if the element is not a {@link ISourceReference} or the
	 * file was opened in an external editor.
	 * @exception PartInitException if the editor could not be initialized or no workbench page is active
	 * @exception CModelException if this element does not exist or if an exception occurs while accessing its underlying resource
	 */
	public static IEditorPart openInEditor(ICElement element) throws CModelException, PartInitException {
		return openInEditor(element, true, true);
	}

	/**
	 * Opens an editor on the given C model element in the active page. Valid are elements that are {@link ISourceReference}.
	 *
	 * @param element the input element
	 * @return returns the editor part of the opened editor or <code>null</code> if the element is not a {@link ISourceReference} or the
	 * file was opened in an external editor.
	 * @exception PartInitException if the editor could not be initialized or no workbench page is active
	 * @exception CModelException if this element does not exist or if an exception occurs while accessing its underlying resource
	 */
	public static IEditorPart openInEditor(ICElement element, boolean activate, boolean reveal) throws CModelException, PartInitException {
		if (!(element instanceof ISourceReference)) {
			return null;
		}
		IEditorPart part= EditorUtility.openInEditor(element, activate);
		if (reveal && part != null) {
			EditorUtility.revealInEditor(part, element);
		}
		return part;
	}

	/**
	 * Reveals the given C model element  in the given editor.. 
	 *
	 * @param part the editor displaying a translation unit
	 * @param element the element to be revealed
	 */
	public static void revealInEditor(IEditorPart part, ICElement element) {
		EditorUtility.revealInEditor(part, element);
	}

	/**
	 * Returns the working copy manager for the CDT UI plug-in.
	 *
	 * @return the working copy manager for the CDT UI plug-in
	 */
	public static IWorkingCopyManager getWorkingCopyManager() {
		return CUIPlugin.getDefault().getWorkingCopyManager();
	}

	/**
	 * Returns the document provider used for C/C++ files.
	 *
	 * @return the document provider for C/C++ files.
	 *
	 * @see IDocumentProvider
	 */
	public static IDocumentProvider getDocumentProvider() {
		return CUIPlugin.getDefault().getDocumentProvider();
	}

	/**
	 * Returns the <code>ICElement</code> element wrapped by the given editor input.
	 *
	 * @param editorInput the editor input
	 * @return the ICElement wrapped by <code>editorInput</code> or <code>null</code> if none
	 */
	public static ICElement getEditorInputCElement(IEditorInput editorInput) {
		if (editorInput instanceof ITranslationUnitEditorInput) {
			return ((ITranslationUnitEditorInput) editorInput).getTranslationUnit();
		}
		IWorkingCopy tu= CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
		if (tu != null)
			return tu;

		return editorInput.getAdapter(ICElement.class);
	}

	/**
	 * Utility method to get an editor input for the given file system location.
	 * If the location denotes a workspace file, a <code>FileEditorInput</code>
	 * is returned, otherwise, the input is an <code>IURIEditorInput</code>
	 * assuming the location points to an existing file in an Eclipse file system.
	 * The <code>ICElement</code> is used to determine the associated project
	 * in case the location can not be resolved to a workspace <code>IFile</code>.
	 *
	 * @param locationURI  a valid Eclipse file system URI
	 * @param context  an element related to the target file, may be <code>null</code>
	 * @return an editor input
	 */
	public static IEditorInput getEditorInputForLocation(URI locationURI, ICElement context) {
		return EditorUtility.getEditorInputForLocation(locationURI, context);
	}

	/**
	 * Utility method to get an editor input for the given file system location.
	 * If the location denotes a workspace file, a <code>FileEditorInput</code>
	 * is returned, otherwise, the input is an <code>IURIEditorInput</code>
	 * assuming the location points to an existing file in the file system.
	 * The <code>ICElement</code> is used to determine the associated project
	 * in case the location can not be resolved to a workspace <code>IFile</code>.
	 *
	 * @param location  a valid file system location
	 * @param context  an element related to the target file, may be <code>null</code>
	 * @return an editor input
	 */
	public static IEditorInput getEditorInputForLocation(IPath location, ICElement context) {
		return EditorUtility.getEditorInputForLocation(location, context);
	}

	/**
	 * Sets up the given document for the default C/C++ partitioning.
	 * 
	 * @param document the document to be set up
	 * @param location the path of the resource backing the document. May be null.
	 * @param locationKind the type of path specified above. May be null.
	 */
	public static void setupCDocument(IDocument document, IPath location, LocationKind locationKind) {
		CUIPlugin.getDefault().getTextTools().setupCDocument(document, location, locationKind);
	}

	/**
	 * Create a document partitioner suitable for Assembly source.
	 */
	public static IDocumentPartitioner createAsmDocumentPartitioner() {
		return new FastPartitioner(new AsmPartitionScanner(), ICPartitions.ALL_ASM_PARTITIONS);
	}

	/**
	 * Sets up the given document for the default Assembly partitioning.
	 * 
	 * @param document the document to be set up
	 */
	public static void setupAsmDocument(IDocument document) {
		IDocumentPartitioner partitioner= createAsmDocumentPartitioner();
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3= (IDocumentExtension3) document;
			extension3.setDocumentPartitioner(ICPartitions.C_PARTITIONING, partitioner);
		} else {
			document.setDocumentPartitioner(partitioner);
		}
		partitioner.connect(document);
	}

}
