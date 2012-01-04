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
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.text;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.CharBuffer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITextStore;

/**
 * Piece list text store implementation with scratch files.
 */
public final class REDTextStore implements ITextStore {

	private static final int SCRATCH_FILE_THRESHOLD = 1024 * 1024;
	private static final int MAX_SCRATCH_FILES = 4;
	private static final int RECYCLE_THRESHOLD = 20;
	private static final int IN_MEMORY_LIMIT = 1024 * 32;
	private final static int CHUNK_SIZE = 1024 * 4;
	private REDFileRider[] fScratchFiles = new REDFileRider[MAX_SCRATCH_FILES];
	private LinkedRun fHead;
	private LinkedRun fSpare;
	private LinkedRun fCache;
	private int fCachePos;
	private int fLength;
	private int fDeadLength;
	private final RunSpec fRunSpec = new RunSpec();
	private Job fSwapper;

	/**
	 * This job swaps readonly IFileRider to disk.
	 */
	private final class TextStoreSwapper extends Job {
		private IFileRider fRider;
		private String fText;

		private TextStoreSwapper(IFileRider rider, String text) {
			super(""); //$NON-NLS-1$
			fRider = rider;
			fText = text;
			setName("Swapping editor buffer to disk"); //$NON-NLS-1$
			setPriority(Job.LONG);
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			REDFileRider fileRider = null;
			if (!monitor.isCanceled()) {
				try {
//					System.out.println("TextStoreSwapper.run() creating swap file");
					fileRider = new REDFileRider(new REDFile());
					int size = fText.length();
					monitor.beginTask(getName(), size+1);
					int written = 0;
					while (written < size && !monitor.isCanceled()) {
						int n = Math.min(size-written, CHUNK_SIZE);
						fileRider.writeChars(fText, written, n);
						monitor.worked(n);
						written += n;
					}
				} catch (IOException e) {
					cancel();
				}
			}
			if (!monitor.isCanceled()) {
//				System.out.println("TextStoreSwapper.run() swapping");
				fileRider = swap(fRider, fileRider);
				monitor.done();
			}
			// something went wrong, dispose the file
			if (fileRider != null) {
//				System.out.println("TextStoreSwapper.run() disposing");
				fileRider.getFile().dispose();
			}
			// remove references
			fText = null;
			fRider = null;
//			System.out.println("TextStoreSwapper.run() done");
			return Status.OK_STATUS;
		}
	}

	private final static class LinkedRun extends REDRun {
		LinkedRun fNext;
		LinkedRun fPrev;

		LinkedRun(IFileRider rider, String str) throws IOException {
			super(rider, str);
		}
		LinkedRun(IFileRider rider, char[] buf, int off, int n) throws IOException {
			super(rider, buf, off, n);
		}
		LinkedRun(IFileRider rider, int offset, int length) {
			super(rider, offset, length);
		}
	}

	/**
	 * Create an empty text store.
	 */
	public REDTextStore() {
	}

	/**
	 * Create a text store with intial content.
	 */
	public REDTextStore(String text) {
		set(text);
	}

	@Override
	protected void finalize() {
		dispose();
	}

	/**
	 * Free resources.
	 * Can be reactivated by calling <code>set(String)</code>.
	 */
	public void dispose() {
		synchronized (fRunSpec) {
			if (fSwapper != null) {
				fSwapper.cancel();
				fSwapper = null;
			}
			for (int i = 0; i < fScratchFiles.length; ++i) {
				if (fScratchFiles[i] != null) {
					fScratchFiles[i].getFile().dispose();
					fScratchFiles[i] = null;
				}
			}
			fHead = null;
			fCache = null;
			fSpare = null;
			fRunSpec.fRun = null;
			fCachePos = 0;
			fLength = 0;
			fDeadLength = 0;
		}
	}

	// ---- ITextStore interface ------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextStore#get(int)
	 */
	@Override
	public char get(int offset) {
		synchronized (fRunSpec) {
			RunSpec spec = findNextRun(offset, null);
			if (spec.fRun != null) {
				return spec.fRun.charAt(spec.fOff);
			}
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextStore#get(int, int)
	 */
	@Override
	public String get(int offset, int length) {
		synchronized (fRunSpec) {
			// special case: long in-memory text in full length (about to be swapped)
			if (length == fLength && fSwapper != null && fHead != null && fHead.fNext == null) {
				((StringRider)fHead.fRider).fBuffer.position(0);
				return ((StringRider)fHead.fRider).fBuffer.toString();
			}
			return toString(offset, offset + length);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextStore#getLength()
	 */
	@Override
	public int getLength() {
		synchronized (fRunSpec) {
			return fLength;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextStore#set(java.lang.String)
	 */
	@Override
	public void set(String text) {
		synchronized (fRunSpec) {
			dispose();
			if (text != null) {
				fHead = new LinkedRun(new StringRider(text), 0, text.length());
				fLength = text.length();
				if (fLength > IN_MEMORY_LIMIT) {
					fSwapper = new TextStoreSwapper(fHead.fRider, text);
					fSwapper.schedule(1000);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextStore#replace(int, int, java.lang.String)
	 */
	@Override
	public void replace(int offset, int length, String text) {
		synchronized (fRunSpec) {
			if (text == null || text.length() == 0) {
				// delete only
				replace(offset, length, null, 0, 0);
			} else {
				replace(offset, length, text, 0, text.length());
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		synchronized (fRunSpec) {
			return toString(0, getLength());
		}
	}

	// --- implementation ----------------------------------------------------

	/** Get part of the text as string.
	 * The parameters from and to are normalized to be in range: [0, fLength]
	 * Not MT-safe!
	 * @param from The beginning of the stretch of text to be returned; for from == n, the nth character is included.
	 * @param to The end of the stretch of text to be returned; for to == n, the nth character is not included.
	 * @return The stretch [from, to[ as String.
	 */
	private String toString(int from, int to) {
		assert from >= 0 && from <= to && to <= fLength;

		int len = to - from;
		StringBuffer strBuf = new StringBuffer(len);
		if (len > 0) {
			RunSpec spec = findPrevRun(from, fRunSpec);
			try {
				int done = spec.fRun.appendTo(strBuf, len, spec.fOff);
				while (done < len) {
					spec.fRun = spec.fRun.fNext;
					assert spec.fRun != null;
					done += spec.fRun.appendTo(strBuf, len - done, 0);
				}
			} catch (IOException e) {
				internalError(e);
			}
		}
		assert strBuf.length() == len;
		return strBuf.toString();
	}

	/**
	 * Replace [from;deleteLen[ with buf[off;insertLen[
	 * Not MT-safe!
	 * @param from
	 * @param deleteLen
	 * @param buf
	 * @param off
	 * @param insertLen
	 */
	private void replace(int from, int deleteLen, Object buf, int off, int insertLen) {
		assert from >= 0 && from <= fLength;
		assert deleteLen >= 0;
		assert from + deleteLen <= fLength;
		
		RunPair split = null;
		if (deleteLen > 0) {
			split = delete(from, from + deleteLen);
		}
		if (buf == null || insertLen == 0) {
			return;
		}
//		assert off >= 0 && off < buf.length;
//		assert insertLen >= 0 && off+insertLen <= buf.length;
	    if (split == null) {
	    	split = splitRun(from);
	    }
		RunPair insert = makeRuns(split.fBefore, buf, off, insertLen);
//		assert runLength(insert.fBefore, insert.fAfter) == insertLen;
		insertRuns(split, insert.fBefore, insert.fAfter);
		fLength += insertLen;
//		assert runLength(fHead, null) == fLength;
		fCache = insert.fAfter;
		fCachePos = from+insertLen-insert.fAfter.fLength;
//		assert checkConsistency();
		if (split.fBefore != null) {
			mergeRuns(split.fBefore, split.fAfter);
		} else {
			mergeRuns(fHead, split.fAfter);
		}
		if (fDeadLength > fLength / 10) {
			reconcile();
		}
	}

	/**
	 * Recreate text store by reinserting all runs.
	 * Not MT-safe!
	 */
	public void reconcile() {
		LinkedRun run = fHead;
		REDFileRider[] scratchFiles = fScratchFiles;
		fScratchFiles = new REDFileRider[MAX_SCRATCH_FILES];
		fHead = null;
		fCache = null;
		fCachePos = -1;
		fSpare = null;
		fLength = 0;
		fDeadLength = 0;
		char[] buf = new char[CHUNK_SIZE];
		int offset = 0;
		int runOffset = 0;
		while (run != null) {
			int n;
			try {
				do {
					n = run.copyInto(buf, 0, buf.length, runOffset);
					replace(offset, 0, buf, 0, n);
					offset += n;
					runOffset += n;
				} while (runOffset < run.fLength);
			} catch (IOException e) {
				internalError(e);
			}
			run = run.fNext;
			runOffset = 0;
		}
		for (int i = 0; i < scratchFiles.length; ++i) {
			if (scratchFiles[i] != null) {
				scratchFiles[i].getFile().dispose();
				scratchFiles[i] = null;
			}
		}
	}

	// *******************************************************************************************************************************************************
	// P R I V A T E - L I N E
	// *******************************************************************************************************************************************************

	/**
	 * Create a new LinkedRun
	 * @param before LinkedRun before new run
	 * @param n length of content
	 * @return new LinkedRun
	 */
	private LinkedRun createRun(LinkedRun before, int n) {
		IFileRider scratchFile;
		if (before != null && before.fRider.length() == before.fOffset + before.fLength && before.fRider.limit() >= before.fRider.length() + n) {
			scratchFile = before.fRider;
		} else {
			scratchFile = getScratchFile();
		}
		return new LinkedRun(scratchFile, scratchFile.length(), n);
	}

	private REDFileRider getScratchFile() {
		REDFileRider rider = null;
		for (int i = 0; i < fScratchFiles.length; ++i) {
			rider = fScratchFiles[i];
			if (rider == null) {
				try {
					rider = new REDFileRider(new REDFile());
				} catch (IOException e) {
					internalError(e);
				}
				fScratchFiles[i] = rider;
				break;
			} else if (rider.length() < SCRATCH_FILE_THRESHOLD) {
				break;
			}
		}
		return rider;
	}

	/**
	 * Save run for later recycling.
	 * @param run
	 */
	private void spareRun(LinkedRun run, LinkedRun last) {
		// remove readonly runs first
		if (last != null) {
			last.fNext = null;
		}
		LinkedRun cur = run;
		LinkedRun prev = null;
		while (cur != null) {
			if (cur.fRider.isReadonly()) {
				if (prev != null) {
					prev.fNext = cur.fNext;
				} else {
					run = cur.fNext;
				}
				if (cur.fNext != null) {
					cur.fNext.fPrev = prev;
				}
			} else {
				prev = cur;
			}
			cur = cur.fNext;
		}
		if (run == null) {
			return;
		}
		last = prev;
		if (last != null) {
			last.fNext = fSpare;
		}
		if (fSpare != null) {
			fSpare.fPrev = last;
		}
		fSpare = run;
		fSpare.fPrev = null;
	}

	/**
	 * Recycle a run.
	 * @returns run
	 */
	private LinkedRun recycleRun() {
		LinkedRun recycled = fSpare;
		fSpare = null;
		return recycled;
	}

	/**
	 * @param e
	 */
	private void internalError(Exception e) {
		throw new Error("Internal error", e); //$NON-NLS-1$
	}

	/**
	 * REDRunSpec represents a specification of a run, including the run itself, its origin and offset.
	 * It is used for findRun - operations.
	 */
	private final static class RunSpec {
		public LinkedRun fRun = null;
		public int fOff = -1;
		public boolean isValid() {
			return fRun != null;
		}
	}

	/**
	 * auxiliary class: pair of red runs
	 */
	private final static class RunPair {
		public LinkedRun fBefore;
		public LinkedRun fAfter;
	}

	/**
	 * Auxiliary method to delete part of the text.
	 * from and to have gap semantics.
	 * @param from start of the stretch to be deleted.
	 * @param to end of the stretch to be deleted.
	 * @return split pos for insertion
	 */
	private RunPair delete(int from, int to) {
		RunPair start = splitRun(from);
		RunPair end = splitRun(to);
		if (start.fBefore != null) {
			start.fBefore.fNext = end.fAfter;
		} else {
			fHead = end.fAfter;
		}
		if (end.fAfter != null) {
			end.fAfter.fPrev = start.fBefore;
		}
		if (end.fAfter != null) {
			fCache = end.fAfter;
			fCachePos = from;
		} else {
			fCache = fHead;
			fCachePos = 0;
		}
		fLength -= (to - from);
		if (fLength == 0) {
			dispose();
			return null;
		}
		spareRun(start.fAfter, end.fBefore);
		start.fAfter = end.fAfter;
		return start;
	}

	/**
	 * Find the run which contains given position.
	 * caveat: if the given position lies between run a and b, a is returned
	 * @param pos The position to find the run for
	 * @return A run specification representing the found run. May be invalid (if given position was larger than text)
	 * @pre pos >= 0
	 * @pre pos <= length()
	 * @post return != null
	 * @post return.fOff > 0 || pos == 0
	 * @post return.fOff <= return.fRun.fLength
	 * @post return.fOrg >= 0
	 */
	private RunSpec findPrevRun(int pos, RunSpec spec) {
		assert pos >= 0 && pos <= fLength;
		LinkedRun cur;
		int curPos;

		if (fCache != null && fCachePos - pos < pos) {
			assert fCache != null;
			cur = fCache;
			curPos = fCachePos;
		} else {
			cur = fHead;
			curPos = 0;
		}
		while (cur != null && pos - curPos > cur.fLength) {
			curPos += cur.fLength;
			cur = cur.fNext;
		}
		if (pos != 0) {
			while (pos - curPos <= 0 && cur != null) {
				cur = cur.fPrev;
				curPos -= cur.fLength;
			}
		}

		fCache = cur;
		fCachePos = curPos;

		if (spec == null) {
			spec = fRunSpec;
		}
		spec.fRun = cur;
		spec.fOff = pos - curPos;

		return spec;
	}

	/**
	 * Find the run which contains given position.
	 * caveat: if the given position lies between run a and b, b is returned
	 * @param pos The position to find the run for
	 * @return A run specification representing the found run. May be invalid (if given position was larger than text)
	 * @pre pos >= 0
	 * @pre pos <= length()
	 * @post return != null
	 * @post return.fOff >= 0
	 * @post return.fOff < return.fRun.fLength
	 */
	private RunSpec findNextRun(int pos, RunSpec spec) {
		if (pos < fLength) {
			spec = findPrevRun(pos + 1, spec);
			spec.fOff--;
		} else {
			spec = findPrevRun(pos, spec);
		}
		return spec;
	}

	/** Split run at pos and return pair of runs. */
	private RunPair splitRun(int pos) {
		RunPair p = new RunPair();
		if (pos == 0) {
			p.fBefore = null;
			p.fAfter = fHead;
		} else {
			RunSpec spec = findPrevRun(pos, null);
			assert spec.isValid();
			p.fBefore = spec.fRun;
			int len = spec.fRun.length();
			if (spec.fOff != len) { // need to split
				p.fAfter = new LinkedRun(p.fBefore.fRider, p.fBefore.fOffset + spec.fOff, p.fBefore.fLength - spec.fOff);
				p.fBefore.fLength = spec.fOff;
				p.fAfter.fNext = p.fBefore.fNext;
				if (p.fAfter.fNext != null) {
					p.fAfter.fNext.fPrev = p.fAfter;
				}
				p.fBefore.fNext = p.fAfter;
				p.fAfter.fPrev = p.fBefore;
			} else { // we already have a split
				p.fAfter = p.fBefore.fNext;
			}
		}
		return p;
	}

	/**
	 * Merge all runs between start and end where possible.
	 * @pre start != null
	 */
	private void mergeRuns(LinkedRun start, LinkedRun end) {
		LinkedRun cur = start;
		LinkedRun next = cur.fNext;

		while (cur != end && next != null) {
			if (cur.isMergeableWith(next)) {
				if (next == fCache) {
					fCache = cur;
					fCachePos -= cur.fLength;
				}
				cur.fLength += next.fLength;
				cur.fNext = next.fNext;
				if (cur.fNext != null) {
					cur.fNext.fPrev = cur;
				}
				if (next == end) {
					break;
				}
			} else {
				cur = next;
			}
			next = cur.fNext;
		}
	}

	private RunPair makeRuns(LinkedRun before, Object buf, int off, int n) {
		RunPair result = new RunPair();
		LinkedRun run;
		LinkedRun recycled = recycleRun();
		if (recycled != null) {
			result.fBefore = recycled;
			run = recycled;
			do {
				int count = Math.min(run.fLength, n);
				try {
					assert !run.fRider.isReadonly();
					// safeguard
					if (run.fRider.isReadonly()) {
						run = null;
						break;
					}
					run.fRider.seek(run.fOffset);
					if (buf instanceof char[]) {
						run.fRider.writeChars((char[])buf, off, count);
					} else {
						run.fRider.writeChars((String)buf, off, count);
					}
					if (run.fLength - count >= RECYCLE_THRESHOLD) {
						LinkedRun next = run.fNext;
						LinkedRun newRun = new LinkedRun(run.fRider, run.fOffset+count, run.fLength-count);
						joinRuns(run, newRun);
						joinRuns(newRun, next);
					} else {
						fDeadLength += run.fLength - count;
					}
					run.fLength = count;
					off += count;
					n -= count;
					before = run;
					run = run.fNext;
				} catch (IOException e) {
					run = null;
//					internalError(e);
					break;
				}
			} while (run != null && n > 0);
			if (run != null) {
				// shortcut for spareRun(run, null)
				fSpare = run;
			}
		}
		if (n > 0) {
			run = createRun(before, n);
			if (buf instanceof char[]) {
				try {
					run.fRider.seek(run.fOffset);
					run.fRider.writeChars((char[])buf, off, n);
				} catch (IOException e) {
//					internalError(e);
					run = new LinkedRun(new StringRider(CharBuffer.wrap((char[])buf, off, off+n)), 0, n);
				}
			} else {
				try {
					run.fRider.seek(run.fOffset);
					run.fRider.writeChars((String)buf, off, n);
				} catch (IOException e) {
//					internalError(e);
					run = new LinkedRun(new StringRider(CharBuffer.wrap((String)buf, off, off+n)), 0, n);
				}
			}
			if (result.fBefore == null) {
				result.fBefore = run;
			} else {
				joinRuns(before, run);
			}
			result.fAfter = run;
		} else {
			result.fAfter = before;
		}
		return result;
	}
	
	private void joinRuns(LinkedRun start, LinkedRun next) {
		assert start != next;
		start.fNext = next;
		if (next != null) {
			next.fPrev = start;
		}
	}
	private void insertRuns(RunPair pos, LinkedRun start, LinkedRun end) {
		assert pos.fBefore == null || pos.fBefore != pos.fAfter;
		start.fPrev = pos.fBefore;
		if (pos.fBefore != null) {
			pos.fBefore.fNext = start;
		} else {
			fHead = start;
		}
		end.fNext = pos.fAfter;
		if (pos.fAfter != null) {
			pos.fAfter.fPrev = end;
		}
	}
	
	/**
	 * Swap given old (readonly) rider with the new (writable) one.
	 * @param oldRider
	 * @param newRider
	 * @return <code>null</code> if the new rider was consumed
	 */
	private REDFileRider swap(IFileRider oldRider, REDFileRider newRider) {
		synchronized(fRunSpec) {
			// search linked run list starting from head and replace
			// all instances of oldRider with newRider
			// in the general case, spare list should be searched, too
			LinkedRun cur = fHead;
			while (cur != null) {
				if (cur.fRider == oldRider) {
					cur.fRider = newRider;
				}
				cur = cur.fNext;
			}
			for (int i = 0; i < fScratchFiles.length; i++) {
				if (fScratchFiles[i] == null) {
					fScratchFiles[i] = newRider;
					newRider = null;
				}
			}
			if (newRider != null) {
				// unlikely, but possible: need to increase array
				REDFileRider[] scratchFiles = new REDFileRider[fScratchFiles.length+1];
				System.arraycopy(fScratchFiles, 0, scratchFiles, 0, fScratchFiles.length);
				scratchFiles[fScratchFiles.length] = newRider;
				fScratchFiles = scratchFiles;
				newRider = null;
			}
			// clean out fRunSpec reference (just in case)
			fRunSpec.fRun = null;
			// clean out swapper reference
			fSwapper = null;
		}
		return newRider;
	}

	// ---- For debugging purposes

	public void printStatistics(PrintStream out) {
		int nRuns = 0;
		int nSpare = 0;
		int spareLength = 0;
		LinkedRun run = fHead;
		while(run != null) {
			++nRuns;
			run = run.fNext;
		}
		run = fSpare;
		while(run != null) {
			++nSpare;
			spareLength += run.fLength;
			run = run.fNext;
		}
		double runMean = nRuns > 0 ? (double)fLength / nRuns : Double.NaN;
		double spareMean = nSpare > 0 ? spareLength / nSpare : Double.NaN;
		out.println("Length: "+fLength); //$NON-NLS-1$
		out.println("Number of runs: "+nRuns); //$NON-NLS-1$
		out.println("Mean length of runs: " + runMean); //$NON-NLS-1$
		out.println("Length of spare runs: "+spareLength); //$NON-NLS-1$
		out.println("Number of spare runs: "+nSpare); //$NON-NLS-1$
		out.println("Mean length of spare runs: " + spareMean); //$NON-NLS-1$
		out.println("Length of dead runs: " + fDeadLength); //$NON-NLS-1$
	}

	/**
	 * Get structure.
	 * Returns the structure (Runs) as a single string. The Runs are separated
	 * by "->\n". At the end the string "null" is added to indicate, that no
	 * more runs exist. This implies that the string "null" is returned for an
	 * empty text.
	 */
	String getStructure() {
		LinkedRun cur = fHead;
		String structure = ""; //$NON-NLS-1$
		while (cur != null) {
			try {
				structure += cur.asString() + "->\n"; //$NON-NLS-1$
			} catch (IOException e) {
				internalError(e);
			}
			cur = cur.fNext;
		}
		structure += "null"; //$NON-NLS-1$
		return structure;
	}
	
	/**
	 * For debugging purposes only.
	 */
	boolean checkConsistency() {
		LinkedRun run = fHead;
		int length = 0;
		while (run != null) {
			LinkedRun prev = run.fPrev;
			LinkedRun next = run.fNext;
			assert prev != run;
			assert next != run;
			assert prev == null || prev.fNext == run;
			assert next == null || next.fPrev == run;
			while (prev != null) {
				prev = prev.fPrev;
				assert run != prev;
			}
			LinkedRun spare = fSpare;
			while (spare != null) {
				assert run != spare;
				spare = spare.fPrev;
			}
			length += run.fLength;
			run = next;
		}
		assert length == fLength;
		if (fCache != null) {
			int pos = fCachePos;
			run = fCache;
			while (run.fPrev != null) {
				run = run.fPrev;
				pos -= run.fLength;
			}
			assert pos == 0;
			pos = fCachePos;
			run = fCache;
			while (run != null) {
				pos += run.fLength;
				run = run.fNext;
			}
			assert pos == fLength;
		}
		return true;
	}
	
	int runLength(LinkedRun first, LinkedRun last) {
		LinkedRun run = first;
		int length = 0;
		while (run != null) {
			length += run.fLength;
			if (run == last) {
				break;
			}
			run = run.fNext;
		}
		return length;
	}

}
