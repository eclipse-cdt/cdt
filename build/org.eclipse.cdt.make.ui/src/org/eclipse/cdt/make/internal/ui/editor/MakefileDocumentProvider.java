/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.editor;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.text.makefile.MakefilePartitionScanner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;

/**
 */
public class MakefileDocumentProvider extends FileDocumentProvider implements IMakefileDocumentProvider {

	private static MakefilePartitionScanner scanner = null;

	/**
	 * Bundle of all required informations to allow working copy management.
	 */
	protected class MakefileInfo extends FileInfo {

		IMakefile fCopy;

		public MakefileInfo(IDocument document, IAnnotationModel model, FileSynchronizer fileSynchronizer, IMakefile copy) {
			super(document, model, fileSynchronizer);
			fCopy = copy;
		}

		public void setModificationStamp(long timeStamp) {
			fModificationStamp = timeStamp;
		}
	}

	/**
	 * Constructor for MakefileDocumentProvider.
	 */
	public MakefileDocumentProvider() {
		super();
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createDocument(Object)
	 */
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			IDocumentPartitioner partitioner = createPartitioner();
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}

	private IDocumentPartitioner createPartitioner() {
		return new DefaultPartitioner(getPartitionScanner(), MakefilePartitionScanner.TYPES);
	}

	private MakefilePartitionScanner getPartitionScanner() {
		if (scanner == null)
			scanner = new MakefilePartitionScanner();
		return scanner;
	}

	/*
	 * @see AbstractDocumentProvider#createElementInfo(Object)
	 */
	protected ElementInfo createElementInfo(Object element) throws CoreException {
		if (element instanceof IFileEditorInput) {

			IFileEditorInput input = (IFileEditorInput) element;
			IMakefile makefile = createMakefile(input.getFile());
			if (makefile == null) {
				return super.createElementInfo(element);
			}
			try {
				refreshFile(input.getFile());
			} catch (CoreException x) {
				handleCoreException(x, MakeUIPlugin.getResourceString("MakeDocumentProvider.exception.createElementInfo")); //$NON-NLS-1$
			}

			IDocument d = null;
			IStatus s = null;

			try {
				d = createDocument(element);
			} catch (CoreException x) {
				s = x.getStatus();
				d = createEmptyDocument();
			}

			IAnnotationModel m = createAnnotationModel(element);
			FileSynchronizer f = new FileSynchronizer(input);
			f.install();

			FileInfo info = new MakefileInfo(d, m, f, makefile);
			info.fModificationStamp = computeModificationStamp(input.getFile());
			info.fStatus = s;
			info.fEncoding = getPersistedEncoding(input);

			return info;
		}

		return super.createElementInfo(element);
	}

	/**
	 * @param file
	 * @return
	 */
	private IMakefile createMakefile(IFile file) {
		return MakeCorePlugin.getDefault().createMakefile(file);
	}

        /*
         * @see AbstractDocumentProvider#doSaveDocument(IProgressMonitor, Object, IDocument, boolean)
         */
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {
                                                                                                                             
		// Update the makefile directives tree;
		ElementInfo elementInfo= getElementInfo(element);
		if (elementInfo instanceof MakefileInfo) {
			MakefileInfo info= (MakefileInfo) elementInfo;
			String content = document.get();
			StringReader reader = new StringReader(content);
			try {
				info.fCopy.parse(info.fCopy.getFileName(), reader);
			} catch (IOException e) {
			}
		}
		super.doSaveDocument(monitor, element, document, overwrite);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.ui.editor.IMakefileDocumentProvider#shutdown()
	 */
	public void shutdown() {
		Iterator e = getConnectedElements();
		while (e.hasNext()) {
			disconnect(e.next());
		}
	}

	/*
	 * @see org.eclipse.cdt.make.internal.ui.editor.ICompilationUnitDocumentProvider#getWorkingCopy(java.lang.Object)
	 */
	public IMakefile getWorkingCopy(Object element) {

		ElementInfo elementInfo = getElementInfo(element);
		if (elementInfo instanceof MakefileInfo) {
			MakefileInfo info = (MakefileInfo) elementInfo;
			return info.fCopy;
		}
		return null;
	}

}
