/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.AddressRangePosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.DisassemblyDocument;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.DisassemblyPosition;
import org.eclipse.jface.text.BadLocationException;

/**
 * A vertical ruler column to display the function + offset of instructions.
 */
public class FunctionOffsetRulerColumn extends DisassemblyRulerColumn {

	/** Maximum width of column (in characters) */
	private static final int MAXWIDTH= 20;

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
				int length = disassPos.fFunction.length;
				if (length > MAXWIDTH) {
					return "..." + new String(disassPos.fFunction, length - MAXWIDTH + 3, MAXWIDTH - 3); //$NON-NLS-1$
				}
				return new String(disassPos.fFunction);
			} else if (pos != null && !pos.fValid) {
				return DOTS.substring(0, Math.min(MAXWIDTH, doc.getMaxFunctionLength()));
			}
		} catch (BadLocationException e) {
			// silently ignored
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	protected int computeNumberOfCharacters() {
		DisassemblyDocument doc = (DisassemblyDocument)getParentRuler().getTextViewer().getDocument();
		return Math.min(MAXWIDTH, doc.getMaxFunctionLength());
	}

}
