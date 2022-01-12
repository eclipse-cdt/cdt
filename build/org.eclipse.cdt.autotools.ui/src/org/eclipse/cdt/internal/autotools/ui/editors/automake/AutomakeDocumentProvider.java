/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
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
 *     Red Hat Inc. - Modified from MakefileDocumentProvider for Automake
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.io.IOException;
import java.util.Iterator;

import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

public class AutomakeDocumentProvider extends TextFileDocumentProvider implements IMakefileDocumentProvider {

	/**
	 * Remembers a IMakefile for each element.
	 */
	protected class AutomakefileFileInfo extends FileInfo {
		public IMakefile fCopy;
	}

	@Override
	protected FileInfo createEmptyFileInfo() {
		return new AutomakefileFileInfo();
	}

	@Override
	protected FileInfo createFileInfo(Object element) throws CoreException {
		IMakefile original = null;
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput) element;
			if (input.getFile().exists())
				original = createMakefile(input.getFile().getLocation().toOSString());
		} else if (element instanceof IURIEditorInput) {
			IURIEditorInput input = (IURIEditorInput) element;
			original = createMakefile(input.getURI().getPath().toString());
		}
		if (original == null)
			return null;

		FileInfo info = super.createFileInfo(element);
		if (!(info instanceof AutomakefileFileInfo)) {
			return null;
		}

		AutomakefileFileInfo makefileInfo = (AutomakefileFileInfo) info;
		setUpSynchronization(makefileInfo);

		makefileInfo.fCopy = original;

		return makefileInfo;
	}

	/**
	 */
	private IMakefile createMakefile(String fileName) {
		IMakefile makefile = null;
		Automakefile automakefile = new Automakefile();
		try {
			automakefile.parse(fileName);
		} catch (IOException e) {
		}
		makefile = automakefile;
		return makefile;
	}

	@Override
	public IMakefile getWorkingCopy(Object element) {
		FileInfo fileInfo = getFileInfo(element);
		if (fileInfo instanceof AutomakefileFileInfo) {
			return ((AutomakefileFileInfo) fileInfo).fCopy;
		}
		return null;
	}

	@Override
	public void shutdown() {
		Iterator<?> e = getConnectedElementsIterator();
		while (e.hasNext())
			disconnect(e.next());
	}

	@Override
	public void connect(Object element) throws CoreException {
		super.connect(element);
		IMakefile makefile = getWorkingCopy(element);
		AutomakeErrorHandler errorHandler = new AutomakeErrorHandler((IEditorInput) element);
		errorHandler.update(makefile);
	}

	@Override
	public IDocument getDocument(Object element) {
		FileInfo info = getFileInfo(element);
		if (info != null)
			return info.fTextFileBuffer.getDocument();
		return getParentProvider().getDocument(element);
	}

}
