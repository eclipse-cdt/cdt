/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.Iterator;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.IBufferFactory;
import org.eclipse.cdt.internal.ui.CFileElementWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

/**
 * CDocumentProvider2
 */
public class CDocumentProvider extends TextFileDocumentProvider {
	/**
	 * Bundle of all required informations to allow working copy management.
	 */
	static protected class TranslationUnitInfo extends FileInfo {
		public IWorkingCopy fCopy;
	}

	/** Indicates whether the save has been initialized by this provider */
	private boolean fIsAboutToSave = false;

	/** The save policy used by this provider */
	//private ISavePolicy fSavePolicy;

	/**
	 *  
	 */
	public CDocumentProvider() {
		super();
		setParentDocumentProvider(new TextFileDocumentProvider(new CStorageDocumentProvider()));		
	}

	/**
	 * Creates a translation unit from the given file.
	 * 
	 * @param file
	 *            the file from which to create the translation unit
	 */
	protected ITranslationUnit createTranslationUnit(IFile file) {
		Object element = CoreModel.getDefault().create(file);
		if (element instanceof ITranslationUnit) {
			return (ITranslationUnit) element;
		}
		return null;
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createEmptyFileInfo()
	 */
	protected FileInfo createEmptyFileInfo() {
		return new TranslationUnitInfo();
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createAnnotationModel(org.eclipse.core.resources.IFile)
	 */
	protected IAnnotationModel createAnnotationModel(IFile file) {
		return new CMarkerAnnotationModel(file);
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createFileInfo(java.lang.Object)
	 */
	protected FileInfo createFileInfo(Object element) throws CoreException {
		ITranslationUnit original = null;
		IWorkingCopy copy = null;
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput)element;
			original = createTranslationUnit(input.getFile());
			IBufferFactory factory = CUIPlugin.getDefault().getBufferFactory();
			copy = (IWorkingCopy) original.getSharedWorkingCopy(getProgressMonitor(), factory);
		} else if (element instanceof ITranslationUnitEditorInput) {
			ITranslationUnitEditorInput input = (ITranslationUnitEditorInput)element;
			copy = new CFileElementWorkingCopy(input.getTranslationUnit());
		}
		
		if (copy == null) {
			return null;
		}
		
		FileInfo info = super.createFileInfo(element);
		if (!(info instanceof TranslationUnitInfo))
			return null;
		TranslationUnitInfo tuInfo = (TranslationUnitInfo) info;
		setUpSynchronization(tuInfo);
		
		//IProblemRequestor requestor= tuInfo.fModel instanceof IProblemRequestor ? (IProblemRequestor) tuInfo.fModel : null;
		//original.becomeWorkingCopy(requestor, getProgressMonitor());

		tuInfo.fCopy = copy;

		//if (tuInfo.fModel instanceof CMarkerAnnotationModel) {
		//	CMarkerAnnotationModel model= (CMarkerAnnotationModel) tuInfo.fModel;
		//	model.setCompilationUnit(tuInfo.fCopy);
		//}
		//if (tuInfo.fModel != null)
		//	tuInfo.fModel.addAnnotationModelListener(fGlobalAnnotationModelListener);
		//if (requestor instanceof IProblemRequestorExtension) {
		//	IProblemRequestorExtension extension= (IProblemRequestorExtension)requestor;
		//	extension.setIsActive(isHandlingTemporaryProblems());
		//}
		return tuInfo;
	}

	private void setUpSynchronization(TranslationUnitInfo cuInfo) {
		IDocument document = cuInfo.fTextFileBuffer.getDocument();
		IAnnotationModel model = cuInfo.fModel;
		if (document instanceof ISynchronizable && model instanceof ISynchronizable) {
			Object lock = ((ISynchronizable) document).getLockObject();
			((ISynchronizable) model).setLockObject(lock);
		}
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#disposeFileInfo(java.lang.Object,
	 *      org.eclipse.ui.editors.text.TextFileDocumentProvider.FileInfo)
	 */
	protected void disposeFileInfo(Object element, FileInfo info) {
		if (info instanceof TranslationUnitInfo) {
			TranslationUnitInfo tuInfo = (TranslationUnitInfo) info;
			tuInfo.fCopy.destroy();
			//if (cuInfo.fModel != null)
			//	cuInfo.fModel.removeAnnotationModelListener(fGlobalAnnotationModelListener);
		}
		super.disposeFileInfo(element, info);
	}

	protected void commitFileBuffer(IProgressMonitor monitor, Object element, FileInfo fileInfo, boolean overwrite)
			throws CoreException {
		if (fileInfo instanceof TranslationUnitInfo) {
			TranslationUnitInfo info = (TranslationUnitInfo)fileInfo;

			synchronized (info.fCopy) {
				info.fCopy.reconcile();
			}

			//if (fSavePolicy != null)
			//	fSavePolicy.preSave(info.fCopy);

			try {
				fIsAboutToSave = true;
				//info.fCopy.commit(overwrite, monitor);
				super.commitFileBuffer(monitor, info, overwrite);
			} catch (CoreException x) {
				// inform about the failure
				fireElementStateChangeFailed(element);
				throw x;
			} catch (RuntimeException x) {
				// inform about the failure
				fireElementStateChangeFailed(element);
				throw x;
			} finally {
				fIsAboutToSave = false;
			}
		} else {
			super.commitFileBuffer(monitor, fileInfo, overwrite);
		}
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider#getWorkingCopy(java.lang.Object)
	 */
	public IWorkingCopy getWorkingCopy(Object element) {
		FileInfo fileInfo = getFileInfo(element);
		if (fileInfo instanceof TranslationUnitInfo) {
			TranslationUnitInfo info = (TranslationUnitInfo) fileInfo;
			return info.fCopy;
		}
		return null;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider#shutdown()
	 */
	public void shutdown() {
		//CUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fPropertyListener);
		Iterator e = getConnectedElementsIterator();
		while (e.hasNext())
			disconnect(e.next());
	}

	/**
	 * Returns the underlying resource for the given element.
	 * 
	 * @param the element
	 * @return the underlying resource of the given element
	 */
	public IResource getUnderlyingResource(Object element) {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			return input.getFile();
		}
		return null;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider#saveDocumentContent(org.eclipse.core.runtime.IProgressMonitor,
	 *      java.lang.Object, org.eclipse.jface.text.IDocument, boolean)
	 */
	public void saveDocumentContent(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite)
			throws CoreException {
		if (!fIsAboutToSave)
			return;
		super.saveDocument(monitor, element, document, overwrite);
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider#createLineTracker(java.lang.Object)
	 */
	public ILineTracker createLineTracker(Object element) {
		return new DefaultLineTracker();
	}
}