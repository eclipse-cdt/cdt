/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.ui.preferences.BuildConsolePreferencePage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitionerExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;

public class BuildConsolePartitioner
		implements
			IDocumentPartitioner,
			IDocumentPartitionerExtension,
			IConsole,
			IPropertyChangeListener {

	/**
	 * List of partitions
	 */
	List fPartitions = new ArrayList(5);

	private int fMaxLines;

	/**
	 * The stream that was last appended to
	 */
	BuildConsoleStream fLastStream = null;

	BuildConsoleDocument fDocument;
	boolean killed;
	BuildConsoleManager fManager;

	/**
	 * A queue of stream entries written to standard out and standard err.
	 * Entries appended to the end of the queue and removed from the front.
	 * Intentionally a vector to obtain synchronization as entries are added and
	 * removed.
	 */
	Vector fQueue = new Vector(5);

	//private boolean fAppending;

	class StreamEntry {

		/**
		 * Identifier of the stream written to.
		 */
		private BuildConsoleStream fStream;
		/**
		 * The text written
		 */
		private StringBuffer fText = null;

		StreamEntry(String text, BuildConsoleStream stream) {
			fText = new StringBuffer(text);
			fStream = stream;
		}

		/**
		 * Returns the stream identifier
		 */
		public BuildConsoleStream getStream() {
			return fStream;
		}

		public void appendText(String text) {
			fText.append(text);
		}

		public int size() {
			return fText.length();
		}

		/**
		 * Returns the text written
		 */
		public String getText() {
			return fText.toString();
		}
	}

	public BuildConsolePartitioner(BuildConsoleManager manager) {
		fManager = manager;
		fMaxLines = BuildConsolePreferencePage.buildConsoleLines();
		fDocument = new BuildConsoleDocument();
		fDocument.setDocumentPartitioner(this);
		connect(fDocument);
	}

	/**
	 * Adds the new text to the document.
	 * 
	 * @param text
	 *            the text to append
	 * @param stream
	 *            the stream to append to
	 */

	public void appendToDocument(String text, BuildConsoleStream stream) {
		boolean addToQueue = true;
		synchronized (fQueue) {
			int i = fQueue.size();
			if (i > 0) {
				StreamEntry entry = (StreamEntry)fQueue.get(i - 1);
				// if last stream is the same and we have not exceeded our
				// display write limit, append.
				if (entry.getStream() == stream && entry.size() < 10000) {
					entry.appendText(text);
					addToQueue = false;
				}
			}
			if (addToQueue) {
				fQueue.add(new StreamEntry(text, stream));
			}
		}
		Runnable r = new Runnable() {

			public void run() {
				StreamEntry entry;
				try {
					entry = (StreamEntry)fQueue.remove(0);
				} catch (ArrayIndexOutOfBoundsException e) {
					return;
				}
				fLastStream = entry.getStream();
				try {
					warnOfContentChange(fLastStream);
					if (fLastStream == null) {
						fDocument.set(entry.getText());
					} else {
						fDocument.replace(fDocument.getLength(), 0, entry.getText());
						checkOverflow();
					}
				} catch (BadLocationException e) {
				}
			}
		};
		Display display = CUIPlugin.getStandardDisplay();
		if (addToQueue && display != null) {
			display.asyncExec(r);
		}
	}

	void warnOfContentChange(BuildConsoleStream stream) {
		if (stream != null) {
			ConsolePlugin.getDefault().getConsoleManager().warnOfContentChange(stream.getConsole());
		}
		fManager.showConsole();
	}

	public IDocument getDocument() {
		return fDocument;
	}

	public void setDocumentSize(int nLines) {
		fMaxLines = nLines;
		nLines = fDocument.getNumberOfLines();
		checkOverflow();
	}

	public void connect(IDocument document) {
		CUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	public void disconnect() {
		fDocument.setDocumentPartitioner(null);
		CUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		killed = true;
	}

	public void documentAboutToBeChanged(DocumentEvent event) {
	}

	public boolean documentChanged(DocumentEvent event) {
		return documentChanged2(event) != null;
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getLegalContentTypes()
	 */
	public String[] getLegalContentTypes() {
		return new String[]{BuildConsolePartition.CONSOLE_PARTITION_TYPE};
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getContentType(int)
	 */
	public String getContentType(int offset) {
		ITypedRegion partition = getPartition(offset);
		if (partition != null) {
			return partition.getType();
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#computePartitioning(int,
	 *      int)
	 */
	public ITypedRegion[] computePartitioning(int offset, int length) {
		if (offset == 0 && length == fDocument.getLength()) {
			return (ITypedRegion[])fPartitions.toArray(new ITypedRegion[fPartitions.size()]);
		}
		int end = offset + length;
		List list = new ArrayList();
		for (int i = 0; i < fPartitions.size(); i++) {
			ITypedRegion partition = (ITypedRegion)fPartitions.get(i);
			int partitionStart = partition.getOffset();
			int partitionEnd = partitionStart + partition.getLength();
			if ( (offset >= partitionStart && offset <= partitionEnd) || (offset < partitionStart && end >= partitionStart)) {
				list.add(partition);
			}
		}
		return (ITypedRegion[])list.toArray(new ITypedRegion[list.size()]);
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getPartition(int)
	 */
	public ITypedRegion getPartition(int offset) {
		for (int i = 0; i < fPartitions.size(); i++) {
			ITypedRegion partition = (ITypedRegion)fPartitions.get(i);
			int start = partition.getOffset();
			int end = start + partition.getLength();
			if (offset >= start && offset < end) {
				return partition;
			}
		}
		return null;
	}

	public IRegion documentChanged2(DocumentEvent event) {
		String text = event.getText();
		if (getDocument().getLength() == 0) {
			// cleared
			fPartitions.clear();
			return new Region(0, 0);
		}
		addPartition(new BuildConsolePartition(fLastStream, event.getOffset(), text.length()));
		ITypedRegion[] affectedRegions = computePartitioning(event.getOffset(), text.length());
		if (affectedRegions.length == 0) {
			return null;
		}
		if (affectedRegions.length == 1) {
			return affectedRegions[0];
		}
		int affectedLength = affectedRegions[0].getLength();
		for (int i = 1; i < affectedRegions.length; i++) {
			ITypedRegion region = affectedRegions[i];
			affectedLength += region.getLength();
		}

		return new Region(affectedRegions[0].getOffset(), affectedLength);
	}

	/**
	 * Checks to see if the console buffer has overflowed, and empties the
	 * overflow if needed, updating partitions and hyperlink positions.
	 */
	protected void checkOverflow() {
		if (fMaxLines >= 0) {
			int nLines = fDocument.getNumberOfLines();
			if (nLines > fMaxLines + 1) {
				int overflow = 0;
				try {
					overflow = fDocument.getLineOffset(nLines - fMaxLines);
				} catch (BadLocationException e1) {
				}
				// update partitions
				List newParitions = new ArrayList(fPartitions.size());
				Iterator partitions = fPartitions.iterator();
				while (partitions.hasNext()) {
					ITypedRegion region = (ITypedRegion)partitions.next();
					if (region instanceof BuildConsolePartition) {
						BuildConsolePartition messageConsolePartition = (BuildConsolePartition)region;

						ITypedRegion newPartition = null;
						int offset = region.getOffset();
						if (offset < overflow) {
							int endOffset = offset + region.getLength();
							if (endOffset < overflow) {
								// remove partition
							} else {
								// split partition
								int length = endOffset - overflow;
								newPartition = messageConsolePartition.createNewPartition(0, length);
							}
						} else {
							// modify parition offset
							newPartition = messageConsolePartition.createNewPartition(messageConsolePartition.getOffset()
									- overflow, messageConsolePartition.getLength());
						}
						if (newPartition != null) {
							newParitions.add(newPartition);
						}
					}
				}
				fPartitions = newParitions;

				try {
					fDocument.replace(0, overflow, ""); //$NON-NLS-1$
				} catch (BadLocationException e) {
				}
			}
		}
	}

	/**
	 * Adds a new colored input partition, combining with the previous partition
	 * if possible.
	 */
	private BuildConsolePartition addPartition(BuildConsolePartition partition) {
		if (fPartitions.isEmpty()) {
			fPartitions.add(partition);
		} else {
			int index = fPartitions.size() - 1;
			BuildConsolePartition last = (BuildConsolePartition)fPartitions.get(index);
			if (last.canBeCombinedWith(partition)) {
				// replace with a single partition
				partition = last.combineWith(partition);
				fPartitions.set(index, partition);
			} else {
				// different kinds - add a new parition
				fPartitions.add(partition);
			}
		}
		return partition;
	}

	public IConsole getConsole() {
		return this;
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty() == BuildConsolePreferencePage.PREF_BUILDCONSOLE_LINES) {
			setDocumentSize(BuildConsolePreferencePage.buildConsoleLines());
		}
	}

	public void start(final IProject project) {
		Display display = CUIPlugin.getStandardDisplay();
		if (display != null) {
			display.asyncExec(new Runnable() {

				public void run() {
					fManager.startConsoleActivity(project);
				}
			});
		}

		if (BuildConsolePreferencePage.isClearBuildConsole()) {
			appendToDocument("", null); //$NON-NLS-1$
		}
	}

	public class BuildOutputStream extends ConsoleOutputStream {

		final BuildConsoleStream fStream;

		public BuildOutputStream(BuildConsoleStream stream) {
			fStream = stream;
		}

		public void flush() throws IOException {
		}

		public void close() throws IOException {
			flush();
		}

		public void write(byte[] b, int off, int len) throws IOException {
			appendToDocument(new String(b, off, len), fStream);
		}
	}

	public ConsoleOutputStream getOutputStream() throws CoreException {
		return new BuildOutputStream(fManager.getStream(BuildConsoleManager.BUILD_STREAM_TYPE_OUTPUT));
	}

	public ConsoleOutputStream getInfoStream() throws CoreException {
		return new BuildOutputStream(fManager.getStream(BuildConsoleManager.BUILD_STREAM_TYPE_INFO));
	}

	public ConsoleOutputStream getErrorStream() throws CoreException {
		return new BuildOutputStream(fManager.getStream(BuildConsoleManager.BUILD_STREAM_TYPE_ERROR));
	}

}
