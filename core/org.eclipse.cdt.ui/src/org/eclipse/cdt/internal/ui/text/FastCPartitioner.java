/*******************************************************************************
 * Copyright (c) 2007, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

/**
 * A slightly adapted FastPartitioner.
 */
public class FastCPartitioner extends FastPartitioner {
	/**
	 * Creates a new partitioner for the given content types.
	 *
	 * @param scanner
	 * @param legalContentTypes
	 */
	public FastCPartitioner(IPartitionTokenScanner scanner, String[] legalContentTypes) {
		super(scanner, legalContentTypes);
	}

	@Override
	public ITypedRegion getPartition(int offset, boolean preferOpenPartitions) {
		if (preferOpenPartitions && offset == fDocument.getLength() && offset > 0) {
			ITypedRegion region = super.getPartition(offset - 1, false);
			try {
				if (ICPartitions.C_MULTI_LINE_COMMENT.equals(region.getType())) {
					if (!fDocument.get(offset - 2, 2).equals("*/")) { //$NON-NLS-1$
						return region;
					}
				} else if (ICPartitions.C_SINGLE_LINE_COMMENT.equals(region.getType())) {
					if (fDocument.getChar(offset - 1) != '\n') {
						return region;
					}
				} else if (ICPartitions.C_MULTI_LINE_DOC_COMMENT.equals(region.getType())) {
					if (!fDocument.get(offset - 2, 2).equals("*/")) { //$NON-NLS-1$
						return region;
					}
				} else if (ICPartitions.C_SINGLE_LINE_DOC_COMMENT.equals(region.getType())) {
					if (fDocument.getChar(offset - 1) != '\n') {
						return region;
					}
				} else if (ICPartitions.C_PREPROCESSOR.equals(region.getType())) {
					if (fDocument.getChar(offset - 1) != '\n') {
						return region;
					}
				}
			} catch (BadLocationException exc) {
			}
		}
		return super.getPartition(offset, preferOpenPartitions);
	}

	/**
	 * @return the DocCommentOwner associated with this partition scanner, or null
	 * if there is no owner.
	 * @since 5.0
	 */
	public IDocCommentOwner getDocCommentOwner() {
		if (fScanner instanceof FastCPartitionScanner) {
			return ((FastCPartitionScanner) fScanner).getDocCommentOwner();
		}
		return null;
	}
}
