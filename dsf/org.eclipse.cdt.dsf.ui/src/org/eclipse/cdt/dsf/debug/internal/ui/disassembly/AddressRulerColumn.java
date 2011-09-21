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

import java.math.BigInteger;

import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.DisassemblyDocument;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.SourceFileInfo;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.preferences.DisassemblyPreferenceConstants;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.DisassemblyRulerColumn;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.IVerticalRulerInfoExtension;
import org.eclipse.jface.text.source.IVerticalRulerListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;

/**
 * A vertical ruler column to display the instruction address.
 */
public class AddressRulerColumn extends DisassemblyRulerColumn implements IVerticalRulerInfo, IVerticalRulerInfoExtension, IAnnotationHover {

	public static final String ID = "org.eclipse.cdt.dsf.ui.disassemblyColumn.address"; //$NON-NLS-1$
	
	private int fRadix;
	private boolean fShowRadixPrefix;
	private String fRadixPrefix;
	private int fNumberOfDigits;
	private int fAddressSize;

	/**
	 * Default constructor.
	 */
	public AddressRulerColumn() {
		super(SWT.LEFT);
		IPreferenceStore prefs = getPreferenceStore();
		setForeground(getColor(DisassemblyPreferenceConstants.ADDRESS_COLOR));
		setRadix(prefs.getInt(DisassemblyPreferenceConstants.ADDRESS_RADIX));
		setShowRadixPrefix(prefs.getBoolean(DisassemblyPreferenceConstants.SHOW_ADDRESS_RADIX));
		setAddressSize(32);
	}

	@Override
	protected String createDisplayString(int line) {
		DisassemblyDocument doc = (DisassemblyDocument)getParentRuler().getTextViewer().getDocument();
		int offset;
		try {
			offset = doc.getLineOffset(line);
			AddressRangePosition pos = doc.getDisassemblyPosition(offset);
			if (pos != null && pos.length > 0 && pos.offset == offset) {
				if (pos.fValid) {
					return getAddressText(pos.fAddressOffset);
				} else {
					return DOTS.substring(0, computeNumberOfCharacters());
				}
			}
			SourcePosition srcPos = doc.getSourcePosition(offset);
			if (srcPos != null && srcPos.fValid && srcPos.length > 0) {
				int srcLine;
				int nLines;
				if (srcPos.fFileInfo.fSource == null) {
					srcLine = srcPos.fLine;
					nLines = srcLine+1;
				} else {
				 	int delta = offset-srcPos.offset;
				 	int baseOffset = srcPos.fFileInfo.fSource.getLineOffset(srcPos.fLine);
					srcLine = srcPos.fFileInfo.fSource.getLineOfOffset(baseOffset+delta);
					nLines = srcPos.fFileInfo.fSource.getNumberOfLines();
				}
				String digitStr = Integer.toString(srcLine+1);
				int maxDigits = (int)(Math.log(nLines)/Math.log(10))+1;
				return SPACES.substring(0, maxDigits-digitStr.length())+digitStr;
			}
		} catch (BadLocationException e) {
			// silently ignored
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	protected int computeNumberOfCharacters() {
		return fNumberOfDigits + (fRadixPrefix != null ? fRadixPrefix.length() : 0) + 1;
	}

	public void setAddressSize(int bits) {
		fAddressSize= bits;
		calculateNumberOfDigits();
	}

	public void setRadix(int radix) {
		fRadix= radix;
		calculateNumberOfDigits();
		setShowRadixPrefix(fShowRadixPrefix);
	}

	private void calculateNumberOfDigits() {
		fNumberOfDigits= BigInteger.ONE.shiftLeft(fAddressSize).subtract(BigInteger.ONE).toString(fRadix).length();
	}
	
	public void setShowRadixPrefix(boolean showRadixPrefix) {
		fShowRadixPrefix = showRadixPrefix;
		if (!fShowRadixPrefix) {
			fRadixPrefix = null;
		} else if (fRadix == 16) {
			fRadixPrefix = "0x"; //$NON-NLS-1$
		} else if (fRadix == 8) {
			fRadixPrefix = "0"; //$NON-NLS-1$
		} else {
			fRadixPrefix = null;
		}
	}

	private String getAddressText(BigInteger address) {
		StringBuffer buf = new StringBuffer(fNumberOfDigits + 3);
		if (fRadixPrefix != null) {
			buf.append(fRadixPrefix);
		}
		String str = address.toString(fRadix);
		for (int i=str.length(); i<fNumberOfDigits; ++i)
			buf.append('0');
		buf.append(str);
		buf.append(':');
		return buf.toString();
	}

	@Override
	public int getLineOfLastMouseButtonActivity() {
		return getParentRuler().getLineOfLastMouseButtonActivity();
	}

	@Override
	public int toDocumentLineNumber(int y_coordinate) {
		return getParentRuler().toDocumentLineNumber(y_coordinate);
	}

	@Override
	public IAnnotationHover getHover() {
		return this;
	}

	@Override
	public IAnnotationModel getModel() {
		return null;
	}

	@Override
	public void addVerticalRulerListener(IVerticalRulerListener listener) {
	}

	@Override
	public void removeVerticalRulerListener(IVerticalRulerListener listener) {
	}

	@Override
	public String getHoverInfo(ISourceViewer sourceViewer, int line) {
		DisassemblyDocument doc = (DisassemblyDocument)getParentRuler().getTextViewer().getDocument();
		BigInteger address = doc.getAddressOfLine(line);
		SourceFileInfo info = doc.getSourceInfo(address);
		if (info != null) {
			return info.fFile.getFullPath().toOSString();
		}
		return null;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property	= event.getProperty();
		IPreferenceStore store = getPreferenceStore();
		boolean needRedraw = false;
		if (DisassemblyPreferenceConstants.ADDRESS_COLOR.equals(property)) {
			setForeground(getColor(property));
			needRedraw = true;
		} else if (DisassemblyPreferenceConstants.ADDRESS_RADIX.equals(property)) {
			setRadix(store.getInt(property));
			updateNumberOfDigits();
			computeIndentations();
			layout(false);
			needRedraw = true;
		} else if (DisassemblyPreferenceConstants.SHOW_ADDRESS_RADIX.equals(property)) {
			setShowRadixPrefix(store.getBoolean(property));
			updateNumberOfDigits();
			computeIndentations();
			layout(false);
			needRedraw = true;
		}
		if (needRedraw) {
			redraw();
		}
	}

}
