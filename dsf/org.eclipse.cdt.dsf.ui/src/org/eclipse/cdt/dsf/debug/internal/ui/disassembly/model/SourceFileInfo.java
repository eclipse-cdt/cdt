/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model;

import java.math.BigInteger;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.presentation.ISourcePresentationCreator;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.presentation.SourcePresentationCreatorFactory;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.ui.IEditorInput;

/**
 * Holds information about a source file.
 */
public class SourceFileInfo {
	public final String fFileKey;
	public final IStorage fFile; // fEdition is subject to change; this records value given to us at construction
	public IStorage fEdition;
	public BigInteger[] fLine2Addr;
	public Addr2Line[] fAddr2Line;
	public volatile IDocument fSource;
	public volatile boolean fValid;
	public Object fLinesNode;
	public Throwable fError;
	public volatile SourceReadingJob fReadingJob;
	public volatile Job fEditionJob;
	public ISourcePresentationCreator fPresentationCreator;
	public BigInteger fStartAddress = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);
	public BigInteger fEndAddress = BigInteger.ZERO;

	public SourceFileInfo(String fileKey, IStorage file) {
		fFileKey = fileKey;
		fFile = fEdition = file;
	}

	/**
	 * Initialize source document.
	 * @throws CoreException
	 */
	public void initSource() throws CoreException {
		SourceDocumentProvider provider = DsfUIPlugin.getSourceDocumentProvider();
		IEditorInput input = new SourceEditorInput(fEdition);
		synchronized (provider) {
			provider.connect(input);
		}
		IStatus status = provider.getStatus(input);
		if (status != null && !status.isOK()) {
			throw new CoreException(status);
		}
	}

	/**
	 * Initialize presentation creator.
	 * @param viewer
	 */
	public void initPresentationCreator(ITextViewer viewer) {
		SourceDocumentProvider provider = DsfUIPlugin.getSourceDocumentProvider();
		IEditorInput input = new SourceEditorInput(fEdition);
		IDocument doc = provider.getDocument(input);
		if (doc != null) {
			IContentType contentType = null;
			if (fEdition instanceof IFile) {
				IFile file = (IFile) fEdition;
				contentType = CCorePlugin.getContentType(file.getProject(), file.getName());
			} else {
				contentType = CCorePlugin.getContentType(fEdition.getName());
			}
			ILanguage language = null;
			if (contentType != null) {
				language = LanguageManager.getInstance().getLanguage(contentType);
			}
			if (language != null) {
				fPresentationCreator = SourcePresentationCreatorFactory.create(language, fEdition, viewer);
			}
			int lines = doc.getNumberOfLines();
			fLine2Addr = new BigInteger[lines];
			fAddr2Line = new Addr2Line[lines / 10 + 1];
			// assign fSource last, triggering source update
			fSource = doc;
		}
	}

	/**
	 * Dispose this object.
	 */
	public void dispose() {
		if (fReadingJob != null) {
			if (!fReadingJob.cancel()) {
				fReadingJob.dispose();
			}
			fReadingJob = null;
		}
		if (fPresentationCreator != null) {
			fPresentationCreator.dispose();
			fPresentationCreator = null;
		}
		SourceDocumentProvider provider = DsfUIPlugin.getSourceDocumentProvider();
		synchronized (provider) {
			provider.disconnect(new SourceEditorInput(fEdition));
		}
		fSource = null;
		fValid = false;
		//		fLinesNode = null;
	}

	public String getLine(int lineNr) {
		return getLines(lineNr, lineNr);
	}

	public String getLines(int first, int last) {
		try {
			int startOffset = fSource.getLineOffset(first);
			int endOffset;
			if (last < fSource.getNumberOfLines() - 1) {
				IRegion lastRegion = fSource.getLineInformation(last + 1);
				endOffset = lastRegion.getOffset();
			} else {
				// last line
				IRegion lastRegion = fSource.getLineInformation(last);
				endOffset = lastRegion.getOffset() + lastRegion.getLength();
			}
			return fSource.get(startOffset, endOffset - startOffset);
		} catch (BadLocationException e) {
			return null;
		}
	}

	public IRegion getRegion(int line, int length) {
		try {
			IRegion lineRegion = fSource.getLineInformation(line);
			return new Region(lineRegion.getOffset(), length);
		} catch (BadLocationException e) {
			return null;
		}
	}

	/**
	 * Get or create text presentation for the given region.
	 * Must be called in display thread.
	 * @param region
	 * @return text presentation
	 */
	public TextPresentation getPresentation(IRegion region) {
		if (fSource != null && fPresentationCreator != null) {
			return fPresentationCreator.getPresentation(region, fSource);
		}
		return null;
	}

	/**
	 * @return offset of given line
	 */
	public int getLineOffset(int line) {
		if (fSource != null) {
			try {
				return fSource.getLineOffset(line);
			} catch (BadLocationException e) {
				// ignored
			}
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return fEdition.toString();
	}
}
