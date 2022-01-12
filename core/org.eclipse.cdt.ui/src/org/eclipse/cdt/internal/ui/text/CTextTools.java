/*******************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.cdt.internal.ui.text.doctools.DocCommentOwnerManager;
import org.eclipse.cdt.internal.ui.text.util.CColorManager;
import org.eclipse.cdt.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.IColorManager;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

/**
 * Tools required to configure a C/C++ source viewer.
 * Scanners must be configured using a {@link CSourceViewerConfiguration}.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class CTextTools {

	/** The color manager */
	private CColorManager fColorManager;

	/** The document partitioning used for the C partitioner */
	private String fDocumentPartitioning = ICPartitions.C_PARTITIONING;

	/**
	 * Creates a new C text tools instance.
	 */
	public CTextTools() {
		fColorManager = new CColorManager(true);
	}

	/**
	 * Disposes all members of this tools collection.
	 */
	public void dispose() {
		if (fColorManager != null) {
			fColorManager.dispose();
			fColorManager = null;
		}
	}

	/**
	 * Gets the color manager.
	 */
	public IColorManager getColorManager() {
		return fColorManager;
	}

	/**
	 * Returns a scanner which is configured to scan
	 * C-specific partitions, which are preprocessor directives, comments,
	 * and regular C source code.
	 *
	 * @param owner may be null
	 * @return a C partition scanner
	 */
	public IPartitionTokenScanner getPartitionScanner(IDocCommentOwner owner) {
		return new FastCPartitionScanner(owner);
	}

	/**
	 * Gets the document provider used.
	 */
	public IDocumentPartitioner createDocumentPartitioner(IDocCommentOwner owner) {
		return new FastCPartitioner(getPartitionScanner(owner), ICPartitions.ALL_CPARTITIONS);
	}

	/**
	 * Sets up the document partitioner for the given document for the given partitioning.
	 *
	 * @param document
	 * @param partitioning
	 * @param owner may be null
	 */
	public void setupCDocumentPartitioner(IDocument document, String partitioning, IDocCommentOwner owner) {
		IDocumentPartitioner partitioner = createDocumentPartitioner(owner);
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3 = (IDocumentExtension3) document;
			extension3.setDocumentPartitioner(partitioning, partitioner);
		} else {
			document.setDocumentPartitioner(partitioner);
		}
		partitioner.connect(document);
	}

	/**
	 * Sets up the given document for the default partitioning.
	 *
	 * @param document the document to be set up
	 * @param location the path of the resource backing the document. May be null.
	 * @param locationKind the type of path specified above. May be null.
	 */
	public void setupCDocument(IDocument document, IPath location, LocationKind locationKind) {
		IDocCommentOwner owner = getDocumentationCommentOwner(location, locationKind);
		setupCDocumentPartitioner(document, fDocumentPartitioning, owner);
	}

	/**
	 * Sets up the given document for the default partitioning.
	 *
	 * @param document the document to be set up
	 */
	public void setupCDocument(IDocument document) {
		setupCDocumentPartitioner(document, fDocumentPartitioning, null);
	}

	/**
	 * Get the document partitioning used for the C partitioner.
	 *
	 * @return the document partitioning used for the C partitioner
	 */
	public String getDocumentPartitioning() {
		return fDocumentPartitioning;
	}

	/**
	 * Set the document partitioning to be used for the C partitioner.
	 */
	public void setDocumentPartitioning(String documentPartitioning) {
		fDocumentPartitioning = documentPartitioning;
	}

	/**
	 * @param location
	 * @param locationKind
	 * @return the documentation comment owner mapped to the specified location. If there is
	 * no mapping, or the <code>location</code>/<code>locationKind</code> is not available, the
	 * workspace default is returned.
	 */
	private IDocCommentOwner getDocumentationCommentOwner(IPath location, LocationKind locationKind) {
		if (location != null && LocationKind.IFILE.equals(locationKind)) {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(location);
			return DocCommentOwnerManager.getInstance().getCommentOwner(file);
		}
		return DocCommentOwnerManager.getInstance().getWorkspaceCommentOwner();
	}
}
