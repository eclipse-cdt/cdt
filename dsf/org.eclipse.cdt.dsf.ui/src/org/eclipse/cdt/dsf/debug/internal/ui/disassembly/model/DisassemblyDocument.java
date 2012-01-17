/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Patrick Chuong (Texas Instruments) - Bug 315443
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model;

import static org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyUtils.DEBUG;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyPosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.ErrorPosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyDocument;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.LabelPosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.SourcePosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.text.REDDocument;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.text.REDTextStore;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.widgets.Display;

/**
 * DisassemblyDocument
 */
public class DisassemblyDocument extends REDDocument implements IDisassemblyDocument {

	public final static String CATEGORY_MODEL = "category_model"; //$NON-NLS-1$
	public final static String CATEGORY_DISASSEMBLY = "category_disassembly"; //$NON-NLS-1$
	public final static String CATEGORY_SOURCE = "category_source"; //$NON-NLS-1$
	public final static String CATEGORY_LABELS = "category_labels"; //$NON-NLS-1$

	/**
	 * For ease of troubleshooting, don't add or remove from this list directly.
	 * Use the add/remove methods. Note that we're not the only ones that
	 * manipulate the list. This list should be accessed only from the GUI thread
	 */
	private final List<AddressRangePosition> fInvalidAddressRanges = new ArrayList<AddressRangePosition>();

	/**
	 * For ease of troubleshooting, don't add or remove from this list directly.
	 * Use the add/remove methods. Note that we're not the only ones that
	 * manipulate the list. This list should be accessed only from the GUI thread
	 */
	private final List<SourcePosition> fInvalidSource = new ArrayList<SourcePosition>();
	
	private final Map<IStorage, SourceFileInfo> fFileInfoMap = new HashMap<IStorage, SourceFileInfo>();

	private int fMaxFunctionLength = 0;
	private BigInteger fMaxOpcodeLength = null;

	private boolean fShowAddresses = false;
	private int fRadix = 16;
	private boolean fShowRadixPrefix = false;
	private String fRadixPrefix;
	private int fNumberOfDigits;
	private boolean fShowFunctionOffset = false;

	private int fNumberOfInstructions;
	private double fMeanSizeOfInstructions = 4;

	private long fErrorAlignment = 0x1L;

	public DisassemblyDocument() {
		super();
	}

	/*
	 * @see org.eclipse.jface.text.AbstractDocument#completeInitialization()
	 */
	@Override
	protected void completeInitialization() {
		super.completeInitialization();
		addPositionCategory(CATEGORY_MODEL);
		addPositionCategory(CATEGORY_DISASSEMBLY);
		addPositionCategory(CATEGORY_SOURCE);
		addPositionCategory(CATEGORY_LABELS);
		setRadix(16);
		setShowRadixPrefix(false);
		fNumberOfInstructions = 0;
		fMeanSizeOfInstructions = 4;
		fMaxFunctionLength = 0;
	}

	/**
	 * Cleanup.
	 */
	@Override
	public void dispose() {
		assert isGuiThread();

		super.dispose();
		
		// cleanup source info
		for (Iterator<SourceFileInfo> iter = fFileInfoMap.values().iterator(); iter.hasNext();) {
			SourceFileInfo fi = iter.next();
			fi.dispose();
		}
		fFileInfoMap.clear();
		fInvalidAddressRanges.clear();
		fInvalidSource.clear();
	}

	/**
	 * Clears all content and state.
	 */
	public void clear() {
		dispose();
		setTextStore(new REDTextStore());
		setLineTracker(new DefaultLineTracker());
		completeInitialization();
	}
	
	public AddressRangePosition[] getInvalidAddressRanges() {
		assert isGuiThread();
		return fInvalidAddressRanges.toArray(new AddressRangePosition[fInvalidAddressRanges.size()]);
	}

	public void setMaxFunctionLength(int functionLength) {
		fMaxFunctionLength = functionLength;
	}

	public int getMaxFunctionLength() {
		return fMaxFunctionLength;
	}

	public void setMaxOpcodeLength(BigInteger longOpcode ) {
		fMaxOpcodeLength = longOpcode;
	}

	public int getMaxOpcodeLength(int radix ) {
		int retVal = 0;
		if (fMaxOpcodeLength != null) {
			String str = fMaxOpcodeLength.toString(radix);
			retVal = str.length();
			switch (radix) {
			    case 8:
			    	retVal += 1; // Padded for 0 prefix
			    	break;
			    case 16:
			    	retVal += 2; // Padded for 0x prefix
			    	break;
			    default:
			    	break;
			}
		}
		return retVal;
	}

	public int getAddressLength() {
		return fNumberOfDigits+2;
	}

	public int getMeanSizeOfInstructions() {
		return (int)(fMeanSizeOfInstructions+.9);
	}

	public Iterator<AddressRangePosition> getModelPositionIterator(BigInteger address) {
		try {
			return getPositionIterator(CATEGORY_MODEL, address);
		} catch (BadPositionCategoryException e) {
			// cannot happen
		}
		return null;
	}

	public Iterator<Position> getPositionIterator(String category, int offset) throws BadPositionCategoryException {
		@SuppressWarnings("unchecked")
		List<Position> positions = (List<Position>) getDocumentManagedPositions().get(category);
		if (positions == null) {
			throw new BadPositionCategoryException();
		}
		int idx = computeIndexInPositionList(positions, offset, true);
		return positions.listIterator(idx);
	}

	public Iterator<AddressRangePosition> getPositionIterator(String category, BigInteger address) throws BadPositionCategoryException {
		@SuppressWarnings("unchecked")
		List<AddressRangePosition> positions = (List<AddressRangePosition>) getDocumentManagedPositions().get(category);
		if (positions == null) {
			throw new BadPositionCategoryException();
		}
		int idx = computeIndexInPositionListFirst(positions, address);
		return positions.listIterator(idx);
	}

	public int computeIndexInCategory(String category, BigInteger address) throws BadPositionCategoryException {
		@SuppressWarnings("unchecked")
		List<AddressRangePosition> c = (List<AddressRangePosition>) getDocumentManagedPositions().get(category);
		if (c == null) {
			throw new BadPositionCategoryException();
		}
		return computeIndexInPositionListFirst(c, address);
	}

	/**
	 * Computes the index in the list of positions at which a position with the
	 * given address would be inserted. The position is supposed to become the
	 * first in this list of all positions with the same offset.
	 * 
	 * @param positions
	 *            the list in which the index is computed
	 * @param address
	 *            the address for which the index is computed
	 * @return the computed index
	 * 
	 */
	protected int computeIndexInPositionListFirst(List<AddressRangePosition> positions, BigInteger address) {
		int size = positions.size();
		if (size == 0) {
			return 0;
		}
		int left = 0;
		int right = size - 1;
		int mid = 0;
		while (left <= right) {
			mid = (left + right) / 2;
			AddressRangePosition range = positions.get(mid);
			int compareSign = address.compareTo(range.fAddressOffset);
			if (compareSign < 0) {
				right = mid - 1;
			} else if (compareSign == 0) {
				break;
			} else if (address.compareTo(range.fAddressOffset.add(range.fAddressLength)) >= 0) {
				left = mid + 1;
			} else {
				break;
			}
		}
		int idx = mid;
		AddressRangePosition p = positions.get(idx);
		if (address.compareTo(p.fAddressOffset) == 0) {
			do {
				--idx;
				if (idx < 0) {
					break;
				}
				p = positions.get(idx);
			} while (address.compareTo(p.fAddressOffset) == 0);
			++idx;
		} else if (address.compareTo(p.fAddressOffset.add(p.fAddressLength)) >= 0) {
			++idx;
		}
		return idx;
	}

	/**
	 * Computes the index in the list of positions at which a position with the
	 * given address would be inserted. The position is supposed to become the
	 * last but one in this list of all positions with the same address.
	 * 
	 * @param positions
	 *            the list in which the index is computed
	 * @param address
	 *            the address for which the index is computed
	 * @return the computed index
	 * 
	 */
	protected int computeIndexInPositionListLast(List<AddressRangePosition> positions, BigInteger address) {
		int size = positions.size();
		if (size == 0) {
			return 0;
		}
		int left = 0;
		int right = size - 1;
		int mid = 0;
		while (left <= right) {
			mid = (left + right) / 2;
			AddressRangePosition range = positions.get(mid);
			if (address.compareTo(range.fAddressOffset) < 0) {
				right = mid - 1;
			} else if (address.compareTo(range.fAddressOffset) == 0) {
				break;
			} else if (address.compareTo(range.fAddressOffset.add(range.fAddressLength)) >= 0) {
				left = mid + 1;
			} else {
				break;
			}
		}
		int idx = mid;
		AddressRangePosition p = positions.get(idx);
		if (address.compareTo(p.fAddressOffset) > 0) {
			++idx;
		} else if (address.compareTo(p.fAddressOffset) == 0 && p.fAddressLength.compareTo(BigInteger.ZERO) == 0) {
			do {
				++idx;
				if (idx == size) {
					break;
				}
				p = positions.get(idx);
			} while (address.compareTo(p.fAddressOffset) == 0 && p.fAddressLength.compareTo(BigInteger.ZERO) == 0);
			//			--idx;
		}
		return idx;
	}

	/**
	 * Computes the index in the list of positions at which a position with the
	 * given offset would be inserted. The position is supposed to become the
	 * last in this list of all positions with the same offset.
	 * 
	 * @param positions
	 *            the list in which the index is computed
	 * @param offset
	 *            the offset for which the index is computed
	 * @return the computed index
	 * 
	 * @see IDocument#computeIndexInCategory(String, int)
	 */
	protected int computeIndexInPositionListLast(List<Position> positions, int offset) {

		if (positions.size() == 0)
			return 0;

		int left = 0;
		int right = positions.size() - 1;
		int mid = 0;
		Position p = null;

		while (left < right) {

			mid = (left + right) / 2;

			p = positions.get(mid);
			if (offset < p.getOffset()) {
				if (left == mid)
					right = left;
				else
					right = mid - 1;
			} else if (offset > p.getOffset()) {
				if (right == mid)
					left = right;
				else
					left = mid + 1;
			} else if (offset == p.getOffset()) {
				left = right = mid;
			}

		}

		int pos = left;
		p = positions.get(pos);
		while (offset >= p.getOffset()) {
			// entry will become the last of all entries with the same offset
			++pos;
			if (pos == positions.size()) {
				break;
			}
			p = positions.get(pos);
		}

		assert 0 <= pos && pos <= positions.size();

		return pos;
	}

	/**
	 * Get the position for the supplied category and index.
	 * 
	 * @param category
	 * @param index
	 * @return a Position matching the category and index, or <code>null</code>.
	 */
	public Position getPositionOfIndex(String category, int index) throws BadPositionCategoryException {
		if (index >= 0) {
			@SuppressWarnings("unchecked")
			List<Position> positions = (List<Position>) getDocumentManagedPositions().get(category);
			if (positions == null) {
				throw new BadPositionCategoryException();
			}
			if (index < positions.size()) {
				return positions.get(index);
			}
		}
		return null;
	}

	/**
	 * @param address
	 * @return
	 */
	public AddressRangePosition getPositionOfAddress(BigInteger address) {
		AddressRangePosition pos = getPositionOfAddress(CATEGORY_DISASSEMBLY, address);
		return pos;
	}

	/**
	 * @param category
	 * @param address
	 * @return
	 */
	public AddressRangePosition getPositionOfAddress(String category, BigInteger address) {
		@SuppressWarnings("unchecked")
		List<AddressRangePosition> positions = (List<AddressRangePosition>) getDocumentManagedPositions().get(category);
		if (positions == null) {
			return null;
		}
		int index = computeIndexInPositionListFirst(positions, address);
		if (index < positions.size()) {
			AddressRangePosition p = positions.get(index);
			if (address.compareTo(p.fAddressOffset) == 0 || p.containsAddress(address)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * @param category
	 * @param range
	 * @return
	 */
	public AddressRangePosition getPositionInAddressRange(String category, AddressRangePosition range) {
		@SuppressWarnings("unchecked")
		List<AddressRangePosition> positions = (List<AddressRangePosition>) getDocumentManagedPositions().get(category);
		if (positions == null) {
			return null;
		}
		BigInteger endAddress = range.fAddressOffset.add(range.fAddressLength);
		int index = computeIndexInPositionListFirst(positions, range.fAddressOffset);
		if (index < positions.size()) {
			do {
				AddressRangePosition p = positions.get(index);
				if (p.fAddressOffset.compareTo(endAddress) >= 0) {
					--index;
				} else {
					return p;
				}
			} while (index >= 0);
		}
		return null;
	}

	/**
	 * Compute the address of the given document line number.
	 * 
	 * @param line
	 * @return the address of the given document line number, -1 if no valid
	 *         address can be computed
	 */
	@Override
	public BigInteger getAddressOfLine(int line) {
		try {
			int offset = getLineOffset(line);
			return getAddressOfOffset(offset);
		} catch (BadLocationException e) {
			// intentionally ignored
		}
		return BigInteger.valueOf(-1);
	}

	/**
	 * Compute the address off the given document offset.
	 * 
	 * @param offset
	 * @return the address of the given document offset, -1 if no valid address
	 *         can be computed
	 */
	public BigInteger getAddressOfOffset(int offset) {
		AddressRangePosition pos;
		try {
			pos = getModelPosition(offset);
		} catch (BadLocationException e) {
			internalError(e);
			return BigInteger.valueOf(-1);
		}
		if (pos == null) {
			return BigInteger.valueOf(-1);
		}
		return pos.fAddressOffset;
	}

	/**
	 * @param offset
	 * @return
	 */
	public AddressRangePosition getDisassemblyPosition(int offset) throws BadLocationException {
		Position p = null;
		try {
			p = getPosition(CATEGORY_DISASSEMBLY, offset, false);
		} catch (BadPositionCategoryException e) {
			// cannot happen
		}
		return (AddressRangePosition) p;
	}

	/**
	 * @param address
	 * @return
	 */
	@Override
	public AddressRangePosition getDisassemblyPosition(BigInteger address) {
		return getPositionOfAddress(CATEGORY_DISASSEMBLY, address);
	}


	/**
	 * @param offset
	 * @return
	 * @throws BadLocationException
	 */
	public AddressRangePosition getModelPosition(int offset) throws BadLocationException {
		Position p = null;
		try {
			p = getPosition(CATEGORY_MODEL, offset, false);
		} catch (BadPositionCategoryException e) {
			// cannot happen
		}
		return (AddressRangePosition) p;
	}

	/**
	 * @param offset
	 * @return
	 * @throws BadLocationException
	 */
	public SourcePosition getSourcePosition(int offset) throws BadLocationException {
		Position p = null;
		try {
			p = getPosition(CATEGORY_SOURCE, offset, true);
		} catch (BadPositionCategoryException e) {
			// cannot happen
		}
		return (SourcePosition) p;
	}

	/**
	 * @param address
	 * @return
	 */
	public SourcePosition getSourcePosition(BigInteger address) {
		return (SourcePosition) getPositionOfAddress(CATEGORY_SOURCE, address);
	}

	/**
	 * @param address
	 * @return
	 */
	public LabelPosition getLabelPosition(BigInteger address) {
		return (LabelPosition) getPositionOfAddress(CATEGORY_LABELS, address);
	}

	/**
	 * @param range
	 * @return
	 */
	public SourcePosition getSourcePositionInAddressRange(AddressRangePosition range) {
		return (SourcePosition) getPositionInAddressRange(CATEGORY_SOURCE, range);
	}

	/**
	 * Compute document position of the given source line.
	 * 
	 * @param file  the file as an <code>IStorage</code>
	 * @param lineNumber  the 0-based line number
	 * @return the document position or <code>null</code>
	 */
	public Position getSourcePosition(IStorage file, int lineNumber) {
		SourceFileInfo info= getSourceInfo(file);
		return getSourcePosition(info, lineNumber);
	}

	/**
	 * Compute document position of the given source line.
	 * 
	 * @param fileName  the file name, may be a raw debugger path or the path to an external file
	 * @param lineNumber  the 0-based line number
	 * @return the document position or <code>null</code>
	 */
	public Position getSourcePosition(String fileName, int lineNumber) {
		SourceFileInfo info= getSourceInfo(fileName);
		if (info == null) {
			info= getSourceInfo(new LocalFileStorage(new File(fileName)));
		}
		return getSourcePosition(info, lineNumber);
	}
	
	/**
	 * Compute document position of the given source line.
	 * 
	 * @param info
	 * @param lineNumber  the 0-based line number
	 * @return the document position or <code>null</code>
	 */
	public Position getSourcePosition(SourceFileInfo info, int lineNumber) {
		if (info == null || info.fSource == null) {
			return null;
		}
		try {
			SourcePosition srcPos= null;
			IRegion stmtLineRegion= info.fSource.getLineInformation(lineNumber);
			final int lineOffset = stmtLineRegion.getOffset();
			final int lineLength = stmtLineRegion.getLength() + 1;
			BigInteger stmtAddress = info.fLine2Addr[lineNumber];
			if (stmtAddress != null && stmtAddress.compareTo(BigInteger.ZERO) > 0) {
				srcPos = getSourcePosition(stmtAddress);
			}
			if (srcPos == null) {
				for (Iterator<Position> iterator = getPositionIterator(CATEGORY_SOURCE, 0); iterator.hasNext(); ) {
					SourcePosition pos= (SourcePosition) iterator.next();
					if (pos.fFileInfo == info && pos.fValid && lineNumber >= pos.fLine) {
						int baseOffset= info.fSource.getLineOffset(pos.fLine);
						if (lineOffset + lineLength - baseOffset <= pos.length) {
							srcPos= pos;
							break;
						}
					}
				}
				if (srcPos == null) {
					return null;
				}
			} else if (!srcPos.fValid) {
				return null;
			}
			assert lineNumber >= srcPos.fLine;
			int baseOffset = info.fSource.getLineOffset(srcPos.fLine);
			int offset = srcPos.offset + lineOffset - baseOffset;
			if (offset >= srcPos.offset && offset < srcPos.offset + srcPos.length) {
				return new Position(offset, lineLength);
			}
		} catch (BadLocationException exc) {
			internalError(exc);
		} catch (BadPositionCategoryException exc) {
			internalError(exc);
		}
		return null;
	}


	/**
	 * @param category
	 * @param offset
	 * @return
	 * @throws BadPositionCategoryException
	 * @throws BadLocationException
	 */
	public Position getPosition(String category, int offset, boolean allowZeroLength) throws BadLocationException, BadPositionCategoryException {
		@SuppressWarnings("unchecked")
		List<Position> list = (List<Position>) getDocumentManagedPositions().get(category);
		int idx;
		idx = computeIndexInPositionList(list, offset, true);
		if (idx > 0) {
			--idx;
		}
		while (idx < list.size()) {
			Position pos = list.get(idx);
			if (pos.offset > offset) {
				break;
			}
			if (pos.includes(offset)) {
				return pos;
			}
			if (allowZeroLength && pos.offset == offset) {
				return pos;
			}
			++idx;
		}
		return null;
	}

	/**
	 * @param pos
	 */
	public void addModelPosition(AddressRangePosition pos) {
		try {
			addPositionLast(CATEGORY_MODEL, pos);
		} catch (BadPositionCategoryException e) {
			// cannot happen
		}
	}

	/**
	 * @param pos
	 */
	public void addModelPositionFirst(AddressRangePosition pos) {
		@SuppressWarnings("unchecked")
		List<AddressRangePosition> list = (List<AddressRangePosition>) getDocumentManagedPositions().get(CATEGORY_MODEL);
		int idx;
		idx = computeIndexInPositionListFirst(list, pos.fAddressOffset.add(pos.fAddressLength));
		if (idx < list.size()) {
			AddressRangePosition nextPos = list.get(idx);
			assert nextPos.fAddressOffset.compareTo(pos.fAddressOffset.add(pos.fAddressLength)) == 0;
		}
		list.add(idx, pos);
	}

	/**
	 * @param pos
	 * @throws BadLocationException
	 */
	public void addDisassemblyPosition(AddressRangePosition pos) throws BadLocationException {
		try {
			addPositionLast(CATEGORY_DISASSEMBLY, pos);
		} catch (BadPositionCategoryException e) {
			// cannot happen
		}
		if (pos instanceof DisassemblyPosition) {
			DisassemblyPosition disassPos = (DisassemblyPosition)pos;
			int functionLength = disassPos.fFunction.length;
			if (functionLength > fMaxFunctionLength) {
				fMaxFunctionLength = functionLength;
			}
			if (disassPos.fOpcodes != null) {
				if (fMaxOpcodeLength == null || fMaxOpcodeLength.compareTo(disassPos.fOpcodes) == -1) {
					fMaxOpcodeLength = disassPos.fOpcodes;
				}
			}
			if (fNumberOfInstructions < 100 && fMeanSizeOfInstructions < 16.0) {
				fMeanSizeOfInstructions = (fMeanSizeOfInstructions * fNumberOfInstructions + pos.fAddressLength.floatValue()) / (++fNumberOfInstructions);
			}
		}
	}

	/**
	 * @param pos
	 * @throws BadPositionCategoryException
	 */
	public void addPositionLast(String category, AddressRangePosition pos) throws BadPositionCategoryException {
		@SuppressWarnings("unchecked")
		List<AddressRangePosition> list = (List<AddressRangePosition>) getDocumentManagedPositions().get(category);
		if (list == null) {
			throw new BadPositionCategoryException();
		}
		if (DEBUG) System.out.println("Adding position to category <" + category + "> : " + pos); //$NON-NLS-1$ //$NON-NLS-2$
		list.add(computeIndexInPositionListLast(list, pos.fAddressOffset), pos);
	}

	/**
	 * @param pos
	 * @throws BadLocationException
	 */
	public void addLabelPosition(AddressRangePosition pos) throws BadLocationException {
		try {
			addPositionLast(CATEGORY_LABELS, pos);
		} catch (BadPositionCategoryException e) {
			// cannot happen
		}
	}

	/**
	 * @param pos
	 */
	public void addSourcePosition(AddressRangePosition pos) throws BadLocationException {
		try {
			addPositionLast(CATEGORY_SOURCE, pos);
		} catch (BadPositionCategoryException e) {
			// cannot happen
		}
	}

	/**
	 * @param pos
	 */
	public void removeDisassemblyPosition(AddressRangePosition pos) {
		try {
			removePosition(CATEGORY_DISASSEMBLY, pos);
		} catch (BadPositionCategoryException e) {
			// cannot happen
		}
	}

	/**
	 * @param pos
	 */
	public void removeSourcePosition(AddressRangePosition pos) {
		try {
			removePosition(CATEGORY_SOURCE, pos);
		} catch (BadPositionCategoryException e) {
			// cannot happen
		}
	}

	/**
	 * @param pos
	 */
	public void removeModelPosition(AddressRangePosition pos) {
		try {
			removePosition(getCategory(pos), pos);
		} catch (BadPositionCategoryException e) {
			// cannot happen
		}
	}

	/**
	 * @param pos
	 * @return
	 */
	private static String getCategory(AddressRangePosition pos) {
		if (pos instanceof LabelPosition) {
			return CATEGORY_LABELS;
		} else if (pos instanceof SourcePosition) {
			return CATEGORY_SOURCE;
		}
		return CATEGORY_DISASSEMBLY;
	}

	/*
	 * @see org.eclipse.jface.text.IDocument#removePosition(java.lang.String,
	 *      org.eclipse.jface.text.Position)
	 */
	@Override
	public void removePosition(String category, Position position) throws BadPositionCategoryException {
		super.removePosition(category, position);

		if (DEBUG && isOneOfOurs(category)) System.out.println("Removing position from category(" + category + ") :" + position);	 //$NON-NLS-1$ //$NON-NLS-2$
		
		if (!category.equals(CATEGORY_MODEL) && position instanceof AddressRangePosition) {
			super.removePosition(CATEGORY_MODEL, position);
		}
	}

	@SuppressWarnings("unchecked")
	public void removePositions(String category, List<AddressRangePosition> toRemove) {
		if (toRemove.isEmpty()) {
			return;
		}
		
		if (DEBUG && isOneOfOurs(category)) { 
			System.out.println("Removing positions from category(" + category + ')'); //$NON-NLS-1$
			int i = 0;
			for (AddressRangePosition pos : toRemove) {
				System.out.println("[" + i++ +"] " + pos); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		List<Position> positions = (List<Position>) getDocumentManagedPositions().get(category);
		if (positions != null) {
			positions.removeAll(toRemove);
		}
		if (category != CATEGORY_MODEL) {
			positions = (List<Position>) getDocumentManagedPositions().get(CATEGORY_MODEL);
			if (positions != null) {
				positions.removeAll(toRemove);
			}
		}
	}

	public void addPositionLast(String category, Position position) throws BadLocationException,
		BadPositionCategoryException {

		if ((0 > position.offset) || (0 > position.length) || (position.offset + position.length > getLength()))
			throw new BadLocationException();

		if (category == null)
			throw new BadPositionCategoryException();

		@SuppressWarnings("unchecked")
		List<Position> list = (List<Position>) getDocumentManagedPositions().get(category);
		if (list == null)
			throw new BadPositionCategoryException();

		list.add(computeIndexInPositionListLast(list, position.offset), position);
	}

	public void checkConsistency() {
		AddressRangePosition last = null;
		try {
			for (Iterator<Position> it = getPositionIterator(CATEGORY_MODEL, 0); it.hasNext();) {
				AddressRangePosition pos = (AddressRangePosition) it.next();
				if (last != null) {
					assert last.fAddressOffset.compareTo(pos.fAddressOffset) <= 0;
					assert last.fAddressOffset.add(last.fAddressLength).compareTo(pos.fAddressOffset) == 0;
					assert last.offset <= pos.offset;
					assert last.offset + last.length == pos.offset;
				}
				last = pos;
			}
		} catch (BadPositionCategoryException e) {
			assert false;
		}
	}

	/**
	 * @param insertPos
	 * @param replaceLength
	 * @param text
	 * @throws BadLocationException
	 */
	public void replace(AddressRangePosition insertPos, int replaceLength, String text) throws BadLocationException {
		int delta = (text != null ? text.length() : 0) - replaceLength;
		if (delta != 0) {
			BigInteger address = insertPos.fAddressOffset;
			Iterator<AddressRangePosition> it = getModelPositionIterator(address);
			while (it.hasNext()) {
				AddressRangePosition pos = it.next();
				assert pos.fAddressOffset.compareTo(address) >= 0;
				if (pos.fAddressOffset.compareTo(address) > 0) {
					break;
				}
				if (pos.offset > insertPos.offset) {
					break;
				}
				if (pos == insertPos) {
					break;
				}
			}
			while (it.hasNext()) {
				AddressRangePosition pos = it.next();
				pos.offset += delta;
			}
		}
		
		if (DEBUG) {
			String escapedText = null;
			if (text != null) {
				escapedText = text.replace(new StringBuffer("\n"), new StringBuffer("\\n")); //$NON-NLS-1$ //$NON-NLS-2$
				escapedText = escapedText.replace(new StringBuffer("\r"), new StringBuffer("\\r")); //$NON-NLS-1$ //$NON-NLS-2$
				escapedText = escapedText.replace(new StringBuffer("\t"), new StringBuffer("\\t")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			System.out.println("Calling AbstractDocument.replace("+insertPos.offset+','+replaceLength+",\""+escapedText+"\")");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		}
		super.replace(insertPos.offset, replaceLength, text);
	}

	/**
	 * @param pos
	 * @param insertPos
	 * @param line
	 * @throws BadPositionCategoryException
	 * @throws BadLocationException
	 */
	public AddressRangePosition insertAddressRange(AddressRangePosition pos, AddressRangePosition insertPos, String line, boolean addToModel)
		throws BadLocationException {
		assert isGuiThread();		
		final BigInteger address = insertPos.fAddressOffset;
		BigInteger length = insertPos.fAddressLength;
		if (pos == null) {
			pos = getPositionOfAddress(address);
		}
		assert !pos.isDeleted && !pos.fValid && (length.compareTo(BigInteger.ZERO) == 0 || pos.containsAddress(address));
		int insertOffset;
		int replaceLength = 0;
		if (length.compareTo(BigInteger.ONE) > 0 && !pos.containsAddress(address.add(length.subtract(BigInteger.ONE)))) {
			// merge with successor positions
			Iterator<AddressRangePosition> it = getModelPositionIterator(pos.fAddressOffset.add(pos.fAddressLength));
			assert it.hasNext();
			do {
				AddressRangePosition overlap = it.next();
				BigInteger posEndAddress= pos.fAddressOffset.add(pos.fAddressLength);
				assert pos.offset <= overlap.offset && overlap.fAddressOffset.compareTo(posEndAddress) == 0;
				if (overlap instanceof LabelPosition || overlap instanceof SourcePosition) {
					// don't override label or source positions, instead fix
					// length of disassembly line to insert
					length = insertPos.fAddressLength = posEndAddress.subtract(address.max(pos.fAddressOffset));
					break;
				}
				pos.fAddressLength = pos.fAddressLength.add(overlap.fAddressLength);
				replaceLength = overlap.offset + overlap.length - pos.offset - pos.length;
				it.remove();
				removeModelPosition(overlap);
				if (!overlap.fValid) {
					removeInvalidAddressRange(overlap);
				}
			} while(!pos.containsAddress(address.add(length.subtract(BigInteger.ONE))));
		}
		BigInteger newEndAddress = pos.fAddressOffset.add(pos.fAddressLength);
		BigInteger newStartAddress = address.add(length);
		assert newEndAddress.compareTo(newStartAddress) >= 0;
		if (address.compareTo(pos.fAddressOffset) == 0) {
			// insert at start of range
			insertOffset = pos.offset;
			if (replaceLength == 0 && newEndAddress.compareTo(newStartAddress) > 0) {
				// optimization: shrink position in place
				pos.fAddressOffset = newStartAddress;
				pos.fAddressLength = pos.fAddressLength.subtract(length);
				// don't insert new pos
				newEndAddress = newStartAddress;
			} else {
				replaceLength += pos.length;
				removeInvalidAddressRange(pos);
				removeDisassemblyPosition(pos);
				pos = null;
			}
		} else {
			// insert in mid/end of range
			insertOffset = pos.offset + pos.length;
			pos.fAddressLength = address.subtract(pos.fAddressOffset);
			assert pos.fAddressLength.compareTo(BigInteger.ZERO) > 0;
			pos = null;
		}
		if (newEndAddress.compareTo(newStartAddress) > 0) {
			pos = insertInvalidAddressRange(insertOffset+replaceLength, 0, newStartAddress, newEndAddress);
		}
		assert pos == null || pos.fAddressLength.compareTo(BigInteger.ZERO) > 0 && pos.containsAddress(address.add(length));
		assert insertOffset + replaceLength <= getLength();

		insertPos.offset = insertOffset;
		if (addToModel) {
			addModelPosition(insertPos);
		}
		replace(insertPos, replaceLength, line);
		if (DEBUG) checkConsistency();
		return pos;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyDocument#insertDisassemblyLine(org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition, java.math.BigInteger, int, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	@Override
	public AddressRangePosition insertDisassemblyLine(AddressRangePosition pos, BigInteger address, int length, String functionOffset, String instruction, String file, int lineNr)
		throws BadLocationException {
		return insertDisassemblyLine(pos, address, length, functionOffset, null, instruction, file, lineNr);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyDocument#insertDisassemblyLine(org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition, java.math.BigInteger, int, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	@Override
	public AddressRangePosition insertDisassemblyLine(AddressRangePosition pos, BigInteger address, int length, String functionOffset, BigInteger opcode, String instruction, String file, int lineNr)
		throws BadLocationException {
		assert isGuiThread();
		String disassLine = null;
		if (instruction == null || instruction.length() == 0) {
			disassLine = ""; //$NON-NLS-1$
		} else {
			disassLine = buildDisassemblyLine(address, functionOffset, instruction);
		}
		AddressRangePosition disassPos;
		if (lineNr < 0) {
			disassPos = new DisassemblyPosition(0, disassLine.length(), address, BigInteger.valueOf(length), functionOffset, opcode);
		} else {
			disassPos = new DisassemblyWithSourcePosition(0, disassLine.length(), address, BigInteger.valueOf(length),
					functionOffset, opcode, file, lineNr);
		}
		pos = insertAddressRange(pos, disassPos, disassLine, true);
		addDisassemblyPosition(disassPos);
		return pos;
	}	
	/**
	 * @param address
	 * @param functionOffset
	 * @param instruction
	 */
	private String buildDisassemblyLine(BigInteger address, String functionOffset, String instruction) {
		StringBuffer buf = new StringBuffer(40);
		if (fShowAddresses) {
			if (fRadixPrefix != null) {
				buf.append(fRadixPrefix);
			}
			String str = address.toString(fRadix);
			for (int i=str.length(); i<fNumberOfDigits; ++i)
				buf.append('0');
			buf.append(str);
			buf.append(':');
			buf.append(' ');
		}
		if (fShowFunctionOffset && functionOffset != null && functionOffset.length() > 0) {
			buf.append(functionOffset);
			int tab = 16;
			if (functionOffset.length() >= 16) {
				tab = (functionOffset.length() + 8) & ~7;
			}
			int diff = tab - functionOffset.length();
			while (diff-- > 0) {
				buf.append(' ');
			}
		} else if (!fShowAddresses) {
			buf.append(' ');
			buf.append(' ');
		}
		int n = instruction.length();
		int prefixLen = buf.length();
		for (int j = 0; j < n; j++) {
			char ch = instruction.charAt(j);
			if (ch == '\t') {
				int tab = (buf.length()-prefixLen + 8) & ~0x7;
				do
					buf.append(' ');
				while (buf.length()-prefixLen < tab);
			} else {
				buf.append(ch);
			}
		}
		buf.append('\n');
		return buf.toString();
	}

	public void setRadix(int radix) {
		fRadix = radix;
		fNumberOfDigits = (int)(Math.log(1L<<32)/Math.log(radix)+0.9);
		setShowRadixPrefix(fShowRadixPrefix);
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

	public AddressRangePosition insertErrorLine(AddressRangePosition pos, BigInteger address, BigInteger length, String line)
		throws BadLocationException {
		assert isGuiThread();
		int hashCode = line.hashCode();
		final long alignment = fErrorAlignment;
		if (alignment > 1 && !(pos instanceof ErrorPosition)) {
			AddressRangePosition before = getPositionOfAddress(address.subtract(BigInteger.ONE));
			if (before instanceof ErrorPosition && before.hashCode() == hashCode && before.offset + before.length == pos.offset) {
				assert before.fAddressOffset.add(before.fAddressLength).compareTo(address) == 0;
				assert pos.fAddressOffset.compareTo(address) == 0;
				// merge with previous error position
				BigInteger pageOffset = before.fAddressOffset.and(BigInteger.valueOf(~(alignment-1)));
				BigInteger mergeLen = pageOffset.add(BigInteger.valueOf(alignment))
					.subtract((before.fAddressOffset.add(before.fAddressLength))).min(length);
				if (mergeLen.compareTo(BigInteger.ZERO) > 0) {
					pos.fAddressLength = pos.fAddressLength.subtract(mergeLen);
					if (pos.fAddressLength.compareTo(BigInteger.ZERO) == 0) {
						replace(pos, pos.length, null);
						removeModelPosition(pos);
						removeInvalidAddressRange(pos);
						pos = null;
					} else {
						pos.fAddressOffset = pos.fAddressOffset.add(mergeLen);
					}
					before.fAddressLength = before.fAddressLength.add(mergeLen);
					address = address.add(mergeLen);
					length = length.subtract(mergeLen);
					if (DEBUG) checkConsistency();
					if (length.compareTo(BigInteger.ZERO) == 0) {
						return pos;
					}
				}
			}
			AddressRangePosition after = getPositionOfAddress(address.add(length));
			if (after instanceof ErrorPosition && after.hashCode() == hashCode && pos != null && pos.offset + pos.length == after.offset) {
				assert after.fAddressOffset == address.add(length);
				assert pos.fAddressOffset.add(pos.fAddressLength).compareTo(after.fAddressOffset) == 0;
				// merge with next error position
				BigInteger pageOffset = after.fAddressOffset.add(BigInteger.valueOf(~(alignment-1)));
				BigInteger mergeLen = after.fAddressOffset.subtract(pageOffset).min(length);
				if (mergeLen.compareTo(BigInteger.ZERO) > 0) {
					after.fAddressOffset = after.fAddressOffset.subtract(mergeLen);
					after.fAddressLength = after.fAddressLength.add(mergeLen);
					pos.fAddressLength = pos.fAddressLength.subtract(mergeLen);
					if (pos.fAddressLength.compareTo(BigInteger.ZERO) == 0) {
						replace(pos, pos.length, null);
						removeModelPosition(pos);
						removeInvalidAddressRange(pos);
						pos = null;
					}
					if (DEBUG) checkConsistency();
					length = length.subtract(mergeLen);
					if (length.compareTo(BigInteger.ZERO) == 0) {
						return pos;
					}
				}
			}
		}
		BigInteger pageOffset = address.and(BigInteger.valueOf(~(alignment-1)));
		BigInteger posLen = pageOffset.add(BigInteger.valueOf(alignment)).subtract(address).min(length);
		while (length.compareTo(BigInteger.ZERO) > 0) {
			AddressRangePosition errorPos = new ErrorPosition(0, 0, address, posLen, hashCode);
			String errorLine = buildDisassemblyLine(address, null, line);
			errorPos.length = errorLine.length();
			pos = insertAddressRange(pos, errorPos, errorLine, true);
			addDisassemblyPosition(errorPos);
			if (!errorPos.fValid) {
				addInvalidAddressRange(errorPos);
			}
			length = length.subtract(posLen);
			address = address.add(posLen);
			posLen = BigInteger.valueOf(alignment).min(length);
		}
		return pos;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyDocument#insertLabel(org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition, java.math.BigInteger, java.lang.String, boolean)
	 */
	@Override
	public AddressRangePosition insertLabel(AddressRangePosition pos, BigInteger address, String label, boolean showLabels)
		throws BadLocationException {
		assert isGuiThread();
		String labelLine = showLabels ? label + ":\n" : ""; //$NON-NLS-1$ //$NON-NLS-2$
		LabelPosition labelPos = getLabelPosition(address);
		if (labelPos != null) {
			assert labelPos.fAddressOffset.compareTo(address) == 0;
			if (labelPos.length != labelLine.length()) {
				int oldLength = labelPos.length;
				labelPos.length = labelLine.length();
				replace(labelPos, oldLength, labelLine);
			}
			return pos;
		}
		labelPos = new LabelPosition(0, labelLine.length(), address, label);
		pos = insertAddressRange(pos, labelPos, labelLine, true);
		addLabelPosition(labelPos);
		return pos;
	}

	/**
	 * @param pos
	 * @param address
	 * @param source
	 * @param line
	 * @param endOfSource
	 * @throws BadLocationException
	 * @throws BadPositionCategoryException
	 */
	public SourcePosition insertSource(SourcePosition pos, String source, int line, boolean endOfSource) {
//		System.out.println("insertSource at "+getAddressText(pos.fAddressOffset));
//		System.out.println(source);
		String sourceLines = source;
		if (source.length() > 0 && sourceLines.charAt(source.length() - 1) != '\n') {
			sourceLines += "\n"; //$NON-NLS-1$
		}
		try {
			assert !pos.fValid;
			int oldLength = pos.length;
			pos.length = sourceLines.length();
			pos.fLine = line;
			pos.fValid = true;
			removeInvalidSourcePosition(pos);
			replace(pos, oldLength, sourceLines);
			if (!endOfSource) {
				if (pos.length > 0) {
					SourcePosition oldPos = getSourcePosition(pos.offset+pos.length);
					if (oldPos == null || oldPos.fAddressOffset.compareTo(pos.fAddressOffset) != 0) {
						pos = new SourcePosition(pos.offset+pos.length, 0, pos.fAddressOffset, pos.fFileInfo, line, pos.fLast, false);
						addSourcePosition(pos);
						addModelPosition(pos);
						addInvalidSourcePositions(pos);
					} else {
						//TLETODO need more checks for correct source pos
						pos = oldPos;
					}
				}
			}
		} catch (BadLocationException e) {
			internalError(e);
		}
		return pos;
	}

    /**
	 * @param pos
	 * @param address
	 * @param fi
	 * @param firstLine
     * @param lastLine 
	 * @return
	 */
	public AddressRangePosition insertInvalidSource(AddressRangePosition pos, BigInteger address, SourceFileInfo fi, int firstLine, int lastLine) {
		assert isGuiThread();
		SourcePosition sourcePos = getSourcePosition(address);
		if (sourcePos != null) {
			return pos;
		}
		String sourceLine = ""; //$NON-NLS-1$
		sourcePos = new SourcePosition(0, sourceLine.length(), address, fi, firstLine, lastLine, false);
		try {
			pos = insertAddressRange(pos, sourcePos, sourceLine, true);
			addSourcePosition(sourcePos);
			assert !fInvalidSource.contains(sourcePos);
			addInvalidSourcePositions(sourcePos);
		} catch (BadLocationException e) {
			internalError(e);
		}
		return pos;
	}

	/**
	 * @param offset
	 * @param replaceLength
	 * @param startAddress
	 * @param endAddress
	 * @return
	 */
	public AddressRangePosition insertInvalidAddressRange(int offset, int replaceLength, BigInteger startAddress, BigInteger endAddress) {
		assert isGuiThread();
		String periods = "...\n"; //$NON-NLS-1$
		AddressRangePosition newPos = new AddressRangePosition(offset, periods.length(), startAddress, endAddress
			.subtract(startAddress), false);
		try {
			addModelPositionFirst(newPos);
			replace(newPos, replaceLength, periods);
			addDisassemblyPosition(newPos);
			addInvalidAddressRange(newPos);
		} catch (BadLocationException e) {
			internalError(e);
		}
		return newPos;
	}

	public void invalidateAddressRange(BigInteger startAddress, BigInteger endAddress, boolean collapse) {
		deleteDisassemblyRange(startAddress, endAddress, true, collapse);
	}
	
	public void deleteDisassemblyRange(BigInteger startAddress, BigInteger endAddress, boolean invalidate, boolean collapse) {
		assert isGuiThread();
		DocumentRewriteSession session = startRewriteSession(DocumentRewriteSessionType.STRICTLY_SEQUENTIAL);
		try {
			String replacement = invalidate ? "...\n" : null; //$NON-NLS-1$
			int replaceLen = replacement != null ? replacement.length() : 0;
			AddressRangePosition lastPos = null;
			ArrayList<AddressRangePosition> toRemove = new ArrayList<AddressRangePosition>();
			Iterator<AddressRangePosition> it = getModelPositionIterator(startAddress);
			while (it.hasNext()) {
				AddressRangePosition pos = it.next();
				BigInteger posEndAddress = pos.fAddressOffset.add(pos.fAddressLength);
				if (pos instanceof LabelPosition) {
					if (!invalidate && pos.length > 0 && posEndAddress.compareTo(endAddress) > 0) {
						try {
							int oldLength = pos.length;
							pos.length = 0;
							replace(pos, oldLength, null);
						} catch (BadLocationException e) {
							internalError(e);
						}
					}
					pos = null;
				} else if (pos instanceof SourcePosition) {
					pos = null;
				} else if (pos instanceof ErrorPosition) {
					pos = null;
				} else if (pos instanceof DisassemblyPosition) {
					// optimization: join adjacent positions
					if (collapse && lastPos != null
							&& (invalidate || lastPos.fValid == pos.fValid)
							&& lastPos.offset+lastPos.length == pos.offset) {
						assert lastPos.fAddressOffset.add(lastPos.fAddressLength).compareTo(pos.fAddressOffset) == 0;
						lastPos.length += pos.length;
						lastPos.fAddressLength = lastPos.fAddressLength.add(pos.fAddressLength);
						toRemove.add(pos);
						if (!pos.fValid) {
							removeInvalidAddressRange(pos);
						}
						pos = null;
						if (posEndAddress.compareTo(endAddress) < 0) {
							continue;
						}
					}
				}
				if (lastPos != null) {
					try {
						if (lastPos.length > 0 || replaceLen > 0) {
							int oldLength = lastPos.length;
							lastPos.length = replaceLen;
							replace(lastPos, oldLength, replacement);
						}
					} catch (BadLocationException e) {
						internalError(e);
					}
				}
				if (pos == null && posEndAddress.compareTo(endAddress) >= 0) {
					break;
				}
				lastPos = null;
				if (pos != null) {
					if (pos.fValid && invalidate) {
						pos.fValid = false;
						addInvalidAddressRange(pos);
					}
					lastPos = pos;
				}
			}
			removePositions(CATEGORY_DISASSEMBLY, toRemove);
		} finally {
			stopRewriteSession(session);
		}
		if (DEBUG) checkConsistency();
	}

	public void invalidateSource() {
		assert isGuiThread();
		Iterator<Position> it;
		try {
			it = getPositionIterator(CATEGORY_SOURCE, 0);
		} catch (BadPositionCategoryException e) {
			internalError(e);
			return;
		}
		while (it.hasNext()) {
			SourcePosition srcPos = (SourcePosition)it.next();
			if (srcPos != null && srcPos.fValid) {
				srcPos.fValid = false;
				assert !fInvalidSource.contains(srcPos);
				addInvalidSourcePositions(srcPos);
			}
		}
	}
	
	public SourcePosition[] getInvalidSourcePositions() {
		assert isGuiThread();
		return fInvalidSource.toArray(new SourcePosition[fInvalidSource.size()]);
	}

	public boolean addInvalidSourcePositions(SourcePosition srcPos) {
		assert isGuiThread();
		if (DEBUG) System.out.println("Adding invalid source position to list: " + srcPos); //$NON-NLS-1$
		return fInvalidSource.add(srcPos);
	}

	public boolean removeInvalidSourcePosition(SourcePosition srcPos) {
		assert isGuiThread();
		if (DEBUG) System.out.println("Removing invalid source position from list: " + srcPos); //$NON-NLS-1$		
		return fInvalidSource.remove(srcPos);
	}
	
	public boolean hasInvalidSourcePositions() {
		assert isGuiThread();
		return fInvalidSource.size() > 0;		
	}

	public void invalidateDisassemblyWithSource(boolean removeDisassembly) {
		for (Iterator<SourceFileInfo> it = fFileInfoMap.values().iterator(); it.hasNext();) {
			SourceFileInfo info = it.next();
			if (info.fLine2Addr != null) {
				deleteDisassemblyRange(info.fStartAddress, info.fEndAddress.add(BigInteger.ONE), !removeDisassembly, !removeDisassembly);
			}
		}
	}

	/**
	 * @param start
	 * @param end
	 * @throws BadLocationException
	 */
	public void deleteLineRange(int start, int end) throws BadLocationException {
		assert isGuiThread();
		if (start >= end) {
			return;
		}
		int startOffset = getLineOffset(start);
		int endOffset = getLineOffset(end);
		int replaceLength = 0;
		AddressRangePosition startPos = getDisassemblyPosition(startOffset);
		if (startPos == null) {
			return;
		}
		startOffset = startPos.offset;
		AddressRangePosition endPos = getDisassemblyPosition(endOffset);
		if (endPos == null) {
			return;
		}
		BigInteger startAddress = BigInteger.ZERO;
		BigInteger addressLength = BigInteger.ZERO;
		ArrayList<AddressRangePosition> toRemove = new ArrayList<AddressRangePosition>();
		try {
			Iterator<AddressRangePosition> it = getPositionIterator(DisassemblyDocument.CATEGORY_MODEL, startAddress);
			while (it.hasNext()) {
				AddressRangePosition p = it.next();
				addressLength = addressLength.add(p.fAddressLength);
				replaceLength += p.length;
				toRemove.add(p);
				if (!p.fValid) {
					if (p instanceof SourcePosition) {
						removeInvalidSourcePosition((SourcePosition)p);
					} else {
						removeInvalidAddressRange(p);
					}
				}
				if (addressLength.compareTo(BigInteger.ZERO) > 0 && p.fAddressOffset.compareTo(endPos.fAddressOffset) >= 0) {
					break;
				}
			}
		} catch (BadPositionCategoryException e) {
			// cannot happen
		}
		for (Iterator<AddressRangePosition> iter = toRemove.iterator(); iter.hasNext();) {
			AddressRangePosition pos = iter.next();
			removeModelPosition(pos);
		}
		if (addressLength.compareTo(BigInteger.ZERO) > 0) {
			insertInvalidAddressRange(startOffset, replaceLength, startAddress, startAddress.add(addressLength));
		}
	}

	public SourceFileInfo getSourceInfo(BigInteger address) {
		AddressRangePosition pos = getDisassemblyPosition(address);
		if (pos instanceof DisassemblyPosition) {
			DisassemblyPosition disassPos = (DisassemblyPosition)pos;
			return getSourceInfo(disassPos.getFile());
		}
		return null;
	}

	public SourceFileInfo getSourceInfo(String file) {
		if (fFileInfoMap == null || file == null) {
			return null;
		}
		for (Iterator<SourceFileInfo> iter = fFileInfoMap.values().iterator(); iter.hasNext();) {
			SourceFileInfo info = iter.next();
			if (file.equals(info.fFileKey)) {
				return info;
			}
		}
		return getSourceInfo(new Path(file));
	}

	public SourceFileInfo getSourceInfo(IPath file) {
		if (fFileInfoMap == null || file == null) {
			return null;
		}
		for (Iterator<SourceFileInfo> iter = fFileInfoMap.values().iterator(); iter.hasNext();) {
			SourceFileInfo info = iter.next();
			if (file.equals(new Path(info.fFileKey))) {
				return info;
			}
		}
		return null;
	}

	public SourceFileInfo getSourceInfo(IStorage sourceElement) {
		if (fFileInfoMap == null) {
			return null;
		}
		SourceFileInfo fi = fFileInfoMap.get(sourceElement);
		return fi;
	}

	public SourceFileInfo createSourceInfo(String fileKey, IStorage sourceElement, Runnable done) {
		SourceFileInfo fi = new SourceFileInfo(fileKey, sourceElement);
		assert fFileInfoMap != null;
		if (fFileInfoMap != null) {
			fFileInfoMap.put(sourceElement, fi);
			new SourceReadingJob(fi, done);
		}
		return fi;
	}

	private void internalError(Throwable e) {
		if (DEBUG) {
			System.err.println("Disassembly: Internal error"); //$NON-NLS-1$
			e.printStackTrace();
		}
	}

	@Override
	public void addInvalidAddressRange(AddressRangePosition pos) {
		assert isGuiThread();
		if (DEBUG) System.out.println("Adding to invalid range list: " + pos); //$NON-NLS-1$
		fInvalidAddressRanges.add(pos);
	}

	public void removeInvalidAddressRanges(Collection<AddressRangePosition> positions) {
		assert isGuiThread();
		if (DEBUG) {
			for (AddressRangePosition pos : positions)
				System.out.println("Removing from invalid range list: " + pos); //$NON-NLS-1$
		}
		fInvalidAddressRanges.removeAll(positions);
	}

	public void removeInvalidAddressRange(AddressRangePosition pos) {
		assert isGuiThread(); 
		if (DEBUG) System.out.println("Removing from invalid range list: " + pos); //$NON-NLS-1$
		fInvalidAddressRanges.remove(pos);
	}
	
	private static boolean isGuiThread() {
		return Display.getCurrent() != null;
	}
	
	private static boolean isOneOfOurs(String category) {
		return category.equals(CATEGORY_MODEL) || 
				category.equals(CATEGORY_DISASSEMBLY) || 
				category.equals(CATEGORY_LABELS) ||
				category.equals(CATEGORY_SOURCE);
	}
}
