/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.editor;

import java.util.Iterator;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

public class MakefileDocumentProvider extends TextFileDocumentProvider implements IMakefileDocumentProvider {
	IMakefile fMakefile;

	protected class MakefileAnnotationModel extends ResourceMarkerAnnotationModel /*implements IProblemRequestor */ {
		public MakefileAnnotationModel(IResource resource) {
			super(resource);
		}

		public void setMakefile(IMakefile makefile) {
			fMakefile = makefile;
		}
	}

	/**
	 * Remembers a IMakefile for each element.
	 */
	protected class MakefileFileInfo extends FileInfo {
		public IMakefile fCopy;
	}

	public MakefileDocumentProvider() {
		IDocumentProvider provider = new TextFileDocumentProvider(new MakefileStorageDocumentProvider());
		provider = new ForwardingDocumentProvider(MakefileDocumentSetupParticipant.MAKEFILE_PARTITIONING,
				new MakefileDocumentSetupParticipant(), provider);
		setParentDocumentProvider(provider);
	}

	/**
	 */
	private IMakefile createMakefile(IFile file) throws CoreException {
		if (file.exists()) {
			return MakeCorePlugin.getDefault().createMakefile(file);
		}
		return null;
	}

	@Override
	protected IAnnotationModel createAnnotationModel(IFile file) {
		return new MakefileAnnotationModel(file);
	}

	@Override
	protected FileInfo createFileInfo(Object element) throws CoreException {
		if (!(element instanceof IFileEditorInput)) {
			return null;
		}

		IFileEditorInput input = (IFileEditorInput) element;
		IMakefile original = createMakefile(input.getFile());
		if (original == null) {
			return null;
		}

		FileInfo info = super.createFileInfo(element);
		if (!(info instanceof MakefileFileInfo)) {
			return null;
		}

		MakefileFileInfo makefileInfo = (MakefileFileInfo) info;
		setUpSynchronization(makefileInfo);

		makefileInfo.fCopy = original;

		if (makefileInfo.fModel instanceof MakefileAnnotationModel) {
			MakefileAnnotationModel model = (MakefileAnnotationModel) makefileInfo.fModel;
			model.setMakefile(makefileInfo.fCopy);
		}
		return makefileInfo;
	}

	@Override
	protected void disposeFileInfo(Object element, FileInfo info) {
		if (info instanceof MakefileFileInfo) {
			MakefileFileInfo makefileInfo = (MakefileFileInfo) info;
			if (makefileInfo.fCopy != null) {
				makefileInfo.fCopy = null;
			}
		}
		super.disposeFileInfo(element, info);
	}

	@Override
	protected FileInfo createEmptyFileInfo() {
		return new MakefileFileInfo();
	}

	@Override
	public IMakefile getWorkingCopy(Object element) {
		FileInfo fileInfo = getFileInfo(element);
		if (fileInfo instanceof MakefileFileInfo) {
			MakefileFileInfo info = (MakefileFileInfo) fileInfo;
			return info.fCopy;
		}
		return null;
	}

	@Override
	public void shutdown() {
		Iterator<?> e = getConnectedElementsIterator();
		while (e.hasNext()) {
			disconnect(e.next());
		}
	}

	@Override
	protected DocumentProviderOperation createSaveOperation(Object element, IDocument document, boolean overwrite)
			throws CoreException {
		if (!(element instanceof IFileEditorInput)) {
			return null;
		}
		return super.createSaveOperation(element, document, overwrite);
	}
}
