/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Patrick Chuong (Texas Instruments) - Bug 315443
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional;

import java.math.BigInteger;
import java.net.URI;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyPosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.LabelPosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.SourcePosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.DisassemblyDocument;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;

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
	private IAddress fStartAddress;
	private String fLabel;

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
		BigInteger docAddress = null;
		if (sourcePosition != null) {
			docAddress = sourcePosition.fAddressOffset;
			if (sourcePosition.length > 0) {
				fSourceFile = sourcePosition.fFileInfo.fFile;
				AddressRangePosition pos = document.getDisassemblyPosition(docAddress);
				if (pos instanceof DisassemblyPosition) {
					fSourceLine = ((DisassemblyPosition) pos).getLine();
				}
			}
		} else {
			docAddress = document.getAddressOfOffset(offset);
		}
		if (docAddress != null) {
			try {
				fStartAddress = new Addr64(docAddress);
			} catch (RuntimeException rte) {
				// not a valid address
				fStartAddress = null;
			}
		}
		
		try {
			Position labelPosition = document.getPosition(DisassemblyDocument.CATEGORY_LABELS, offset, true);
			if (labelPosition != null) {
				if (labelPosition instanceof LabelPosition) {
					fLabel = ((LabelPosition) labelPosition).fLabel;
				}
			}
		} catch (Exception e) {
			fLabel = null;
		}
	}
	
	/*
	 * @see org.eclipse.jface.viewers.ISelection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return fTextSelection.isEmpty();
	}

	/*
	 * @see org.eclipse.jface.text.ITextSelection#getEndLine()
	 */
	@Override
	public int getEndLine() {
		return fTextSelection.getEndLine();
	}

	/*
	 * @see org.eclipse.jface.text.ITextSelection#getLength()
	 */
	@Override
	public int getLength() {
		return fTextSelection.getLength();
	}

	/*
	 * @see org.eclipse.jface.text.ITextSelection#getOffset()
	 */
	@Override
	public int getOffset() {
		return fTextSelection.getOffset();
	}

	/*
	 * @see org.eclipse.jface.text.ITextSelection#getStartLine()
	 */
	@Override
	public int getStartLine() {
		return fTextSelection.getStartLine();
	}

	/*
	 * @see org.eclipse.jface.text.ITextSelection#getText()
	 */
	@Override
	public String getText() {
		return fTextSelection.getText();
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblySelection#getSourceFile()
	 */
	@Override
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
	@Override
	public int getSourceLine() {
		if (fSourceFile != null) {
			return fSourceLine;
		}
		return -1;
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblySelection#getSourceLocationURI()
	 */
	@Override
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
	@Override
	public IAddress getStartAddress() {
		return fStartAddress;
	}
	
	/**
	 * @since 2.2
	 */
	@Override
	public String getLabel() {
		return fLabel;
	}
}