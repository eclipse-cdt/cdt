/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
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
 *     Patrick Chuong (Texas Instruments) - Bug 315443
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional;

import java.net.URI;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;

/**
 * This interface represents a selection in a {@link IDisassemblyPart}.
 * In addition to text selection attributes this interface provides information
 * about the address and source file position for the start offset of the selection.
 *
 * @since 2.1
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IDisassemblySelection extends ITextSelection {

	/**
	 * @return the address associated with the start of the selection, may be <code>null</code>
	 */
	IAddress getStartAddress();

	/**
	 * @return the {@link IFile} associated with the selection, may be <code>null</code>
	 */
	IFile getSourceFile();

	/**
	 * @return the source location {@link URI} of the associated source file, may be <code>null</code>
	 */
	URI getSourceLocationURI();

	/**
	 * @return the 0-based line number of the source file associated with the selection, -1 if not available
	 */
	int getSourceLine();

	/**
	 * @return the label, may be <code>null</code>
	 *
	 * @since 2.2
	 */
	String getLabel();
}
