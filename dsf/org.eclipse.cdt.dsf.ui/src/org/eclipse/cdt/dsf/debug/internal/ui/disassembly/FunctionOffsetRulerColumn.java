/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyPosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.DisassemblyDocument;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.preferences.DisassemblyPreferenceConstants;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.DisassemblyRulerColumn;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A vertical ruler column to display the function + offset of instructions.
 */
public class FunctionOffsetRulerColumn extends DisassemblyRulerColumn {

	public static final String ID = "org.eclipse.cdt.dsf.ui.disassemblyColumn.functionOffset"; //$NON-NLS-1$

	/** Maximum width of column (in characters) */
	private static final int MAXWIDTH= 20;

	/**
	 * Default constructor.
	 */
	public FunctionOffsetRulerColumn() {
		super();
		setForeground(getColor(DisassemblyPreferenceConstants.FUNCTION_OFFSETS_COLOR));
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

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property	= event.getProperty();
		boolean needRedraw = false;
		if (DisassemblyPreferenceConstants.FUNCTION_OFFSETS_COLOR.equals(property)) {
			setForeground(getColor(property));
			needRedraw = true;
		}
		if (needRedraw) {
			redraw();
		}
	}

}
