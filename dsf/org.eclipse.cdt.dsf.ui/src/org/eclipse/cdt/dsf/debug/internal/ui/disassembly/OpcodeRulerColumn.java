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

import java.math.BigInteger;
import java.util.IllegalFormatException;

import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyPosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.DisassemblyDocument;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.preferences.DisassemblyPreferenceConstants;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.DisassemblyRulerColumn;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A vertical ruler column to display the opcodes of instructions.
 */
public class OpcodeRulerColumn extends DisassemblyRulerColumn {

	public static final String ID = "org.eclipse.cdt.dsf.ui.disassemblyColumn.opcode"; //$NON-NLS-1$

	/** Maximum width of column (in characters) */
	/** 15 bytes plus hex prefix and separator */
	private static final int MAXWIDTH_WITH_PREFIX = 74;
	/** 15 bytes plus separator */
	private static final int MAXWIDTH_WITHOUT_PREFIX = 44;

	private int fRadix;
	private String fRadixPrefix;

	/**
	 * Default constructor.
	 */
	public OpcodeRulerColumn() {
		super();
		setBackground(getColor(DisassemblyPreferenceConstants.RULER_BACKGROUND_COLOR));
		setForeground(getColor(DisassemblyPreferenceConstants.CODE_BYTES_COLOR));
		setShowRadixPrefix(getPreferenceStore().getBoolean(DisassemblyPreferenceConstants.SHOW_ADDRESS_RADIX));
	}

	public void setShowRadixPrefix(boolean showRadix) {
		if (showRadix) {
			fRadixPrefix = "0x"; //$NON-NLS-1$
		} else {
			fRadixPrefix = null;
		}
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
					if (disassPos.fOpcode != null) {
						// Format the output.
						return getOpcodeString(disassPos.fOpcode, fRadix);
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

	protected String getOpcodeString(Byte[] opcode, int fRadix) {
		String opcodeString = ""; //$NON-NLS-1$
		try {
			for (int i = 0; i < opcode.length; i++) {
				if (!opcodeString.isEmpty()) {
					opcodeString += " "; //$NON-NLS-1$
				}

				String hexOpcode = String.format("%02x", //$NON-NLS-1$
						BigInteger.valueOf(opcode[i].intValue() & 0xff));
				if (fRadixPrefix != null)
					hexOpcode = fRadixPrefix + hexOpcode;
				opcodeString += hexOpcode;
			}
		} catch (IllegalFormatException e) {
			// silently ignored
		}
		return opcodeString;
	}

	@Override
	protected int computeNumberOfCharacters() {
		DisassemblyDocument doc = (DisassemblyDocument) getParentRuler().getTextViewer().getDocument();
		return Math.min(fRadixPrefix == null ? MAXWIDTH_WITHOUT_PREFIX : MAXWIDTH_WITH_PREFIX,
				doc.getMaxOpcodeLength(fRadixPrefix));
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		IPreferenceStore store = getPreferenceStore();
		boolean needRedraw = false;
		if (DisassemblyPreferenceConstants.CODE_BYTES_COLOR.equals(property)) {
			setForeground(getColor(property));
			needRedraw = true;
		} else if (DisassemblyPreferenceConstants.SHOW_ADDRESS_RADIX.equals(property)) {
			setShowRadixPrefix(store.getBoolean(property));
			updateNumberOfDigits();
			computeIndentations();
			layout(false);
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
