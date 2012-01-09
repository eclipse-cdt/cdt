/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.presentation.IPresentationDamager;

/**
 * A simple presentation damager always damaging the whole partition.
 * This is necessary if the partition contains multiline highlight regions.
 * 
 * @since 4.0
 */
public class PartitionDamager implements IPresentationDamager {

	/*
	 * @see org.eclipse.jface.text.presentation.IPresentationDamager#getDamageRegion(org.eclipse.jface.text.ITypedRegion, org.eclipse.jface.text.DocumentEvent, boolean)
	 */
	@Override
	public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent event,
			boolean documentPartitioningChanged) {
		if (!documentPartitioningChanged && event.getOffset() == partition.getOffset() + partition.getLength()) {
			IRegion lineRegion;
			try {
				lineRegion = event.fDocument.getLineInformationOfOffset(event.getOffset());
				int start= partition.getOffset();
				int end= lineRegion.getOffset() + lineRegion.getLength();
				return new Region(start, end - start);
			} catch (BadLocationException exc) {
				// ignore
			}
		}
		return partition;
	}

	/*
	 * @see org.eclipse.jface.text.presentation.IPresentationDamager#setDocument(org.eclipse.jface.text.IDocument)
	 */
	@Override
	public void setDocument(IDocument document) {
	}

}
