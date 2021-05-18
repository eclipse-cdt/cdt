/*******************************************************************************
 * Copyright (c) 2011, 2014 Wind River Systems and others.
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
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyPosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.DisassemblyDocument;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.preferences.DisassemblyPreferenceConstants;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.DisassemblyRulerColumn;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A vertical ruler column to display the opcodes of instructions.
 */
public class OpcodeRulerColumn extends DisassemblyRulerColumn {

	public static final String ID = "org.eclipse.cdt.dsf.ui.disassemblyColumn.opcode"; //$NON-NLS-1$

	/** Maximum width of column (in characters) */
	/** 15 bytes plus separator for typical 8-bit instruction words */
	private static final int MAXWIDTH = 44;

	/**
	 * Default constructor.
	 */
	public OpcodeRulerColumn() {
		super();
		setBackground(getColor(DisassemblyPreferenceConstants.RULER_BACKGROUND_COLOR));
		setForeground(getColor(DisassemblyPreferenceConstants.CODE_BYTES_COLOR));
	}

	/*
	 * @see org.eclipse.jface.text.source.LineNumberRulerColumn#createDisplayString(int)
	 */
	@Override
	protected String createDisplayString(int line) {
		int nChars = computeNumberOfCharacters();
		if (nChars > 0) {
			DisassemblyDocument doc = (DisassemblyDocument) getParentRuler().getTextViewer().getDocument();
			try {
				int offset = doc.getLineOffset(line);
				AddressRangePosition pos = doc.getDisassemblyPosition(offset);
				if (pos instanceof DisassemblyPosition && pos.length > 0 && pos.offset == offset && pos.fValid) {
					DisassemblyPosition disassPos = (DisassemblyPosition) pos;
					if (disassPos.fRawOpcode != null) {
						if (disassPos.fRawOpcode.length() > nChars) {
							return disassPos.fRawOpcode.substring(0, nChars - 1) + "…"; //$NON-NLS-1$
						} else {
							return disassPos.fRawOpcode;
						}
					}
				} else if (pos != null && !pos.fValid) {
					return DOTS.substring(0, nChars);
				}
			} catch (BadLocationException e) {
				// silently ignored
			}
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	protected int computeNumberOfCharacters() {
		DisassemblyDocument doc = (DisassemblyDocument) getParentRuler().getTextViewer().getDocument();
		return Math.min(MAXWIDTH, doc.getMaxOpcodeLength());
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		boolean needRedraw = false;
		if (DisassemblyPreferenceConstants.CODE_BYTES_COLOR.equals(property)) {
			setForeground(getColor(property));
			needRedraw = true;
		} else if (DisassemblyPreferenceConstants.RULER_BACKGROUND_COLOR.equals(property)) {
			setBackground(getColor(property));
			needRedraw = true;
		}
		if (needRedraw) {
			redraw();
		}
	}

}
