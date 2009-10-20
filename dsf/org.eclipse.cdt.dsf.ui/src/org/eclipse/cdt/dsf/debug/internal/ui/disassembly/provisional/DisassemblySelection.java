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
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional;

import java.math.BigInteger;
import java.net.URI;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.DisassemblyDocument;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.DisassemblyPosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.SourcePosition;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;

/**
 * Default implementation of {@link IDisassemblySelection}.
 * 
 * @since 2.1
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DisassemblySelection implements IDisassemblySelection {

	private final ITextSelection fTextSelection;
	private IStorage fSourceFile;
	private int fSourceLine;
	private BigInteger fStartAddress;

	/**
	 * Create a disassembly selection from a normal text selection and a disassembly part.
	 * 
	 * @param selection  the text selection
	 * @param part  the disassembly part
	 */
	public DisassemblySelection(ITextSelection selection, IDisassemblyPart part) {
		this(selection, (DisassemblyDocument) part.getTextViewer().getDocument());
	}

	DisassemblySelection(ITextSelection selection, DisassemblyDocument document) {
		fTextSelection = selection;
		int offset = selection.getOffset();
		SourcePosition sourcePosition;
		try {
			sourcePosition = document.getSourcePosition(offset);
		} catch (BadLocationException exc) {
			sourcePosition = null;
		}
		if (sourcePosition != null) {
			fStartAddress = sourcePosition.fAddressOffset;
			if (sourcePosition.length > 0) {
				fSourceFile = sourcePosition.fFileInfo.fFile;
				DisassemblyPosition pos = (DisassemblyPosition) document.getDisassemblyPosition(fStartAddress);
				if (pos != null) {
					fSourceLine = pos.getLine();
				}
			}
		} else {
			fStartAddress = document.getAddressOfOffset(offset);
		}
	}
	
	/*
	 * @see org.eclipse.jface.viewers.ISelection#isEmpty()
	 */
	public boolean isEmpty() {
		return fTextSelection.isEmpty();
	}

	/*
	 * @see org.eclipse.jface.text.ITextSelection#getEndLine()
	 */
	public int getEndLine() {
		return fTextSelection.getEndLine();
	}

	/*
	 * @see org.eclipse.jface.text.ITextSelection#getLength()
	 */
	public int getLength() {
		return fTextSelection.getLength();
	}

	/*
	 * @see org.eclipse.jface.text.ITextSelection#getOffset()
	 */
	public int getOffset() {
		return fTextSelection.getOffset();
	}

	/*
	 * @see org.eclipse.jface.text.ITextSelection#getStartLine()
	 */
	public int getStartLine() {
		return fTextSelection.getStartLine();
	}

	/*
	 * @see org.eclipse.jface.text.ITextSelection#getText()
	 */
	public String getText() {
		return fTextSelection.getText();
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblySelection#getSourceFile()
	 */
	public IFile getSourceFile() {
		if (fSourceFile != null) {
			IResource resource = (IResource) fSourceFile.getAdapter(IResource.class);
			if (resource instanceof IFile) {
				return (IFile) resource;
			}
		}
		return null;
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblySelection#getSourceLine()
	 */
	public int getSourceLine() {
		if (fSourceFile != null) {
			return fSourceLine;
		}
		return -1;
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblySelection#getSourceLocationURI()
	 */
	public URI getSourceLocationURI() {
		if (fSourceFile != null) {
			IResource resource = (IResource) fSourceFile.getAdapter(IResource.class);
			if (resource instanceof IFile) {
				return resource.getLocationURI();
			} else {
				IPath location = fSourceFile.getFullPath();
				if (location != null) {
					return URIUtil.toURI(location);
				}
			}
		}
		return null;
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblySelection#getStartAddress()
	 */
	public BigInteger getStartAddress() {
		return fStartAddress;
	}

}