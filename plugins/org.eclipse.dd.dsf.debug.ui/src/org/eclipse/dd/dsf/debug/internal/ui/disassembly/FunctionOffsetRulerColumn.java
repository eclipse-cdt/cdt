/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.ui.disassembly;

import org.eclipse.dd.dsf.debug.internal.ui.disassembly.model.AddressRangePosition;
import org.eclipse.dd.dsf.debug.internal.ui.disassembly.model.DisassemblyDocument;
import org.eclipse.dd.dsf.debug.internal.ui.disassembly.model.DisassemblyPosition;
import org.eclipse.jface.text.BadLocationException;

/**
 * A vertical ruler column to display the function + offset of instructions.
 */
public class FunctionOffsetRulerColumn extends DisassemblyRulerColumn {

	/**
	 * Default constructor.
	 */
	public FunctionOffsetRulerColumn() {
		super();
	}

	/*
	 * @see org.eclipse.jface.text.source.LineNumberRulerColumn#createDisplayString(int)
	 */
	@Override
	protected String createDisplayString(int line) {
		DisassemblyDocument doc = (DisassemblyDocument)getParentRuler().getTextViewer().getDocument();
		int offset;
		try {
			offset = doc.getLineOffset(line);
			AddressRangePosition pos = doc.getDisassemblyPosition(offset);
			if (pos instanceof DisassemblyPosition && pos.length > 0 && pos.offset == offset && pos.fValid) {
				DisassemblyPosition disassPos = (DisassemblyPosition)pos;
				return new String(disassPos.fFunction);
			} else if (pos != null && !pos.fValid) {
				return DOTS.substring(0, doc.getMaxFunctionLength());
			}
		} catch (BadLocationException e) {
			// silently ignored
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	protected int computeNumberOfCharacters() {
		DisassemblyDocument doc = (DisassemblyDocument)getParentRuler().getTextViewer().getDocument();
		return doc.getMaxFunctionLength();
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerColumn#getWidth()
	 */
	@Override
	public int getWidth() {
		return super.getWidth();
	}

}
