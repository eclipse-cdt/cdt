/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems and others.
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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A vertical ruler column to display the opcodes of instructions.
 */
public class OpcodeRulerColumn extends DisassemblyRulerColumn {

	public static final String ID = "org.eclipse.cdt.dsf.ui.disassemblyColumn.opcode"; //$NON-NLS-1$

	private int fRadix;
	private String fRadixPrefix;

	/**
	 * Default constructor.
	 */
	public OpcodeRulerColumn() {
		super();
		setForeground(getColor(DisassemblyPreferenceConstants.CODE_BYTES_COLOR));
		setRadix(getPreferenceStore().getInt(DisassemblyPreferenceConstants.OPCODE_RADIX));
	}

	public void setRadix(int radix) {
		fRadix= radix;
		setShowRadixPrefix();
	}

	public void setShowRadixPrefix() {
		if (fRadix == 16) {
			fRadixPrefix = "0x"; //$NON-NLS-1$
		} else if (fRadix == 8) {
			fRadixPrefix = "0"; //$NON-NLS-1$
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
			DisassemblyDocument doc = (DisassemblyDocument)getParentRuler().getTextViewer().getDocument();
			try {
				int offset = doc.getLineOffset(line);
				AddressRangePosition pos = doc.getDisassemblyPosition(offset);
				if (pos instanceof DisassemblyPosition && pos.length > 0 && pos.offset == offset && pos.fValid) {
					DisassemblyPosition disassPos = (DisassemblyPosition)pos;
					if (disassPos.fOpcodes != null) {
						// Format the output.
						String str = disassPos.fOpcodes.toString(fRadix);
						int prefixLength = 0;
	
						if (fRadixPrefix != null)
							prefixLength = fRadixPrefix.length();
	
						StringBuilder buf = new StringBuilder(nChars);
	
						if (prefixLength != 0)
							buf.append(fRadixPrefix);
	
						for (int i=str.length()+prefixLength; i < nChars; ++i)
							buf.append('0');
						buf.append(str);
						return buf.toString();
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
		DisassemblyDocument doc = (DisassemblyDocument)getParentRuler().getTextViewer().getDocument();
		return doc.getMaxOpcodeLength(fRadix);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property	= event.getProperty();
		IPreferenceStore store = getPreferenceStore();
		boolean needRedraw = false;
		if (DisassemblyPreferenceConstants.CODE_BYTES_COLOR.equals(property)) {
			setForeground(getColor(property));
			needRedraw = true;
		} else if (DisassemblyPreferenceConstants.OPCODE_RADIX.equals(property)) {
			setRadix(store.getInt(property));
			layout(false);
			needRedraw = true;
		}
		if (needRedraw) {
			redraw();
		}
	}

}
