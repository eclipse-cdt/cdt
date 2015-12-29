/*******************************************************************************
 * Copyright (c) 2002, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 *     Andrew Gvozdev (Quoin Inc.)  - Copy build log (bug 306222)
 *     Alex Collins (Broadcom Corp.) - Global console
 *     Sergey Prigogin (Google) - Performance improvements
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IMarker;
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

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.resources.ResourcesUtil;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.preferences.BuildConsolePreferencePage;

public class BuildConsolePartitioner
		implements IDocumentPartitioner, IDocumentPartitionerExtension, IConsole, IPropertyChangeListener {

	private static class SynchronizedDeque<E> implements Deque<E> {
		private final Deque<E> deque;

		public SynchronizedDeque(Deque<E> deque) {
			this.deque = deque;
		}

		@Override
		public synchronized boolean isEmpty() {
			return deque.isEmpty();
		}

		@Override
		public synchronized void addFirst(E e) {
			deque.addFirst(e);
		}

		@Override
		public synchronized void addLast(E e) {
			deque.addLast(e);
		}

		@Override
		public synchronized Object[] toArray() {
			return deque.toArray();
		}

		@Override
		public synchronized <T> T[] toArray(T[] a) {
			return deque.toArray(a);
		}

		@Override
		public synchronized boolean offerFirst(E e) {
			return deque.offerFirst(e);
		}

		@Override
		public synchronized boolean offerLast(E e) {
			return deque.offerLast(e);
		}

		@Override
		public synchronized E removeFirst() {
			return deque.removeFirst();
		}

		@Override
		public synchronized E removeLast() {
			return deque.removeLast();
		}

		@Override
		public synchronized E pollFirst() {
			return deque.pollFirst();
		}

		@Override
		public synchronized E pollLast() {
			return deque.pollLast();
		}

		@Override
		public synchronized E getFirst() {
			return deque.getFirst();
		}

		@Override
		public synchronized E getLast() {
			return deque.getLast();
		}

		@Override
		public synchronized E peekFirst() {
			return deque.peekFirst();
		}

		@Override
		public synchronized E peekLast() {
			return deque.peekLast();
		}

		@Override
		public synchronized boolean removeFirstOccurrence(Object o) {
			return deque.removeFirstOccurrence(o);
		}

		@Override
		public synchronized boolean containsAll(Collection<?> c) {
			return deque.containsAll(c);
		}

		@Override
		public synchronized boolean removeLastOccurrence(Object o) {
			return deque.removeLastOccurrence(o);
		}

		@Override
		public synchronized boolean addAll(Collection<? extends E> c) {
			return deque.addAll(c);
		}

		@Override
		public synchronized boolean add(E e) {
			return deque.add(e);
		}

		@Override
		public synchronized boolean removeAll(Collection<?> c) {
			return deque.removeAll(c);
		}

		@Override
		public synchronized boolean offer(E e) {
			return deque.offer(e);
		}

		@Override
		public synchronized boolean retainAll(Collection<?> c) {
			return deque.retainAll(c);
		}

		@Override
		public synchronized E remove() {
			return deque.remove();
		}

		@Override
		public synchronized E poll() {
			return deque.poll();
		}

		@Override
		public synchronized E element() {
			return deque.element();
		}

		@Override
		public synchronized void clear() {
			deque.clear();
		}

		@Override
		public synchronized boolean equals(Object o) {
			return deque.equals(o);
		}

		@Override
		public synchronized E peek() {
			return deque.peek();
		}

		@Override
		public synchronized void push(E e) {
			deque.push(e);
		}

		@Override
		public synchronized E pop() {
			return deque.pop();
		}

		@Override
		public synchronized int hashCode() {
			return deque.hashCode();
		}

		@Override
		public synchronized boolean remove(Object o) {
			return deque.remove(o);
		}

		@Override
		public synchronized boolean contains(Object o) {
			return deque.contains(o);
		}

		@Override
		public synchronized int size() {
			return deque.size();
		}

		@Override
		public synchronized Iterator<E> iterator() {
			return deque.iterator();
		}

		@Override
		public synchronized Iterator<E> descendingIterator() {
			return deque.descendingIterator();
		}
	}

	private IProject fProject;

	private int openStreamCount = 0;

	/**
	 * List of partitions
	 */
	List<ITypedRegion> fPartitions = new ArrayList<ITypedRegion>(5);

	private int fMaxLines;

	/**
	 * The stream that was last appended to
	 */
	BuildConsoleStreamDecorator fLastStream;

	BuildConsoleDocument fDocument;
	DocumentMarkerManager fDocumentMarkerManager;
	boolean killed;
	BuildConsoleManager fManager;

	/**
	 * A queue of stream entries written to standard out and standard err.
	 * Entries appended to the end of the queue and removed from the front.
	 */
	private final Deque<StreamEntry> fQueue =
			new SynchronizedDeque<StreamEntry>(new ArrayDeque<StreamEntry>());

	private URI fLogURI;
	private OutputStream fLogStream;

	private class StreamEntry {
		public static final int EVENT_APPEND = 0;
		public static final int EVENT_OPEN_LOG = 1;
		public static final int EVENT_CLOSE_LOG = 2;
		public static final int EVENT_OPEN_APPEND_LOG = 3;

		/** Identifier of the stream written to. */
		private BuildConsoleStreamDecorator fStream;
		/** The text written */
		private StringBuffer fText = null;
		/** Problem marker corresponding to the line of text */
		private ProblemMarkerInfo fMarker;
		/** Type of event **/
		private int eventType;

		public StreamEntry(String text, BuildConsoleStreamDecorator stream, ProblemMarkerInfo marker) {
			fText = new StringBuffer(text);
			fStream = stream;
			fMarker = marker;
			eventType = EVENT_APPEND;
		}

		/**
		 * This constructor is used for special events such as clear console or close log.
		 *
		 * @param event - kind of event.
		 */
		public StreamEntry(int event) {
			fText = null;
			fStream = null;
			fMarker = null;
			eventType = event;
		}

		/**
		 * Returns the stream identifier
		 */
		public BuildConsoleStreamDecorator getStream() {
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

		/**
		 * Returns error marker
		 */
		public ProblemMarkerInfo getMarker() {
			return fMarker;
		}

		/**
		 * Returns type of event
		 */
		public int getEventType() {
			return eventType;
		}
	}

	/**
	 * Construct a partitioner that is not associated with a specific project
	 */
	public BuildConsolePartitioner(BuildConsoleManager manager) {
		this(null, manager);
	}

	public BuildConsolePartitioner(IProject project, BuildConsoleManager manager) {
		fProject = project;
		fManager = manager;
		fMaxLines = BuildConsolePreferencePage.buildConsoleLines();
		fDocument = new BuildConsoleDocument();
		fDocument.setDocumentPartitioner(this);
		fDocumentMarkerManager = new DocumentMarkerManager(fDocument, this);
		connect(fDocument);

		fLogURI = null;
		fLogStream = null;
	}

	/**
	 * Sets the indicator that stream was opened so logging can be started. Should be called
	 * when opening the output stream.
	 */
	public void setStreamOpened() {
		fQueue.add(new StreamEntry(StreamEntry.EVENT_OPEN_LOG));
		asyncProcessQueue();
	}

	/**
	 * Open the stream for appending. Must be called after a call to setStreamOpened().
	 * Can be used to reopen a stream for writing after it has been closed, without
	 * emptying the log file.
	 */
	public void setStreamAppend() {
		fQueue.add(new StreamEntry(StreamEntry.EVENT_OPEN_APPEND_LOG));
		asyncProcessQueue();
	}

	/**
	 * Sets the indicator that stream was closed so logging should be stopped. Should be called when
	 * build process has finished. Note that there could still be unprocessed console
	 * stream entries in the queue being worked on in the background.
	 */
	public void setStreamClosed() {
		fQueue.add(new StreamEntry(StreamEntry.EVENT_CLOSE_LOG));
		asyncProcessQueue();
	}

	/**
	 * Adds the new text to the document.
	 *
	 * @param text - the text to append.
	 * @param stream - the stream to append to.
	 */
	public void appendToDocument(String text, BuildConsoleStreamDecorator stream, ProblemMarkerInfo marker) {
		boolean addToQueue = true;
		synchronized (fQueue) {
			StreamEntry entry = fQueue.peekLast();
			if (entry != null) {
				// If last stream is the same and the size of the queued entry has not exceeded
				// the batch size, avoid creating a new entry and append the new text to the last
				// entry in the queue. The batch size is adaptive and grows with the length of
				// the queue.
				if (entry.getStream() == stream && entry.getEventType() == StreamEntry.EVENT_APPEND &&
						entry.getMarker() == marker && entry.size() < 2000 * fQueue.size()) {
					entry.appendText(text);
					addToQueue = false;
				}
			}
			if (addToQueue) {
				fQueue.add(new StreamEntry(text, stream, marker));
			}
		}
		if (addToQueue) {
			asyncProcessQueue();
		}
	}

	/**
	 * Asynchronous processing of stream entries to append to console.
	 * Note that all these are processed by the same thread - the user-interface thread
	 * as of {@link Display#asyncExec(Runnable)}.
	 */
	private void asyncProcessQueue() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				StreamEntry entry;
				entry = fQueue.pollFirst();
				if (entry == null)
					return;

				switch (entry.getEventType()) {
				case StreamEntry.EVENT_OPEN_LOG:
					openStreamCount++;
					//$FALL-THROUGH$
				case StreamEntry.EVENT_OPEN_APPEND_LOG:
					logOpen(entry.getEventType() == StreamEntry.EVENT_OPEN_APPEND_LOG);
					break;
				case StreamEntry.EVENT_APPEND:
					fLastStream = entry.getStream();
					try {
						warnOfContentChange(fLastStream);

						if (fLastStream == null) {
							// special case to empty document
							fPartitions.clear();
							fDocumentMarkerManager.clear();
							fDocument.set(""); //$NON-NLS-1$
						}
						String text = entry.getText();
						if (text.length() > 0) {
							addStreamEntryToDocument(entry);
							log(text);
							boolean allowSlack = false;
							entry = fQueue.peekFirst();
							if (entry != null && entry.getEventType() == StreamEntry.EVENT_APPEND) {
								// Buffer truncation is an expensive operation. Allow some slack
								// if more data is coming and we will be truncating the buffer
								// again soon.
								allowSlack = true;
							}
							checkOverflow(allowSlack);
						}
					} catch (BadLocationException e) {
					}
					break;
				case StreamEntry.EVENT_CLOSE_LOG:
					openStreamCount--;
					if (openStreamCount <= 0) {
						openStreamCount = 0;
						logClose();
					}
					break;
				}
			}

			/**
			 * Open the log
			 * @param append Set to true if the log should be opened for appending, false for overwriting.
			 */
			private void logOpen(boolean append) {
				fLogURI = fManager.getLogURI(fProject);
				if (fLogURI != null) {
					try {
						IFileStore logStore = EFS.getStore(fLogURI);
						// Ensure the directory exists before opening the file
						IFileStore dir = logStore.getParent();
						if (dir != null)
							dir.mkdir(EFS.NONE, null);
						int opts = append ? EFS.APPEND : EFS.NONE;
						fLogStream = logStore.openOutputStream(opts, null);
					} catch (CoreException e) {
						CUIPlugin.log(e);
					} finally {
						ResourcesUtil.refreshWorkspaceFiles(fLogURI);
					}
				}
			}

			private void log(String text) {
				if (fLogStream != null) {
					try {
						fLogStream.write(text.getBytes());
						if (fQueue.isEmpty()) {
							fLogStream.flush();
						}
					} catch (IOException e) {
						CUIPlugin.log(e);
					} finally {
						ResourcesUtil.refreshWorkspaceFiles(fLogURI);
					}
				}
			}

			private void logClose() {
				if (fLogStream != null) {
					try {
						fLogStream.close();
					} catch (IOException e) {
						CUIPlugin.log(e);
					} finally {
						ResourcesUtil.refreshWorkspaceFiles(fLogURI);
					}
					fLogStream = null;
				}
			}

		};
		Display display = CUIPlugin.getStandardDisplay();
		if (display != null) {
			display.asyncExec(r);
		}
	}

	private void addStreamEntryToDocument(StreamEntry entry) throws BadLocationException {
		ProblemMarkerInfo marker = entry.getMarker();
		if (marker == null) {
			// It is plain unmarkered console output
			addPartition(new BuildConsolePartition(fLastStream,
					fDocument.getLength(),
					entry.getText().length(),
					BuildConsolePartition.CONSOLE_PARTITION_TYPE));
		} else {
			// this text line in entry is markered with ProblemMarkerInfo,
			// create special partition for it.
			String errorPartitionType;
			if (marker.severity == IMarker.SEVERITY_INFO) {
				errorPartitionType = BuildConsolePartition.INFO_PARTITION_TYPE;
			} else if (marker.severity == IMarker.SEVERITY_WARNING) {
				errorPartitionType = BuildConsolePartition.WARNING_PARTITION_TYPE;
			} else {
				errorPartitionType = BuildConsolePartition.ERROR_PARTITION_TYPE;
			}
			addPartition(new BuildConsolePartition(fLastStream,
					fDocument.getLength(),
					entry.getText().length(),
					errorPartitionType, marker));
		}
		fDocument.replace(fDocument.getLength(), 0, entry.getText());
	}

	void warnOfContentChange(BuildConsoleStreamDecorator stream) {
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
		checkOverflow(false);
	}

	@Override
	public void connect(IDocument document) {
		CUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	@Override
	public void disconnect() {
		fDocument.setDocumentPartitioner(null);
		CUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		killed = true;
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
	}

	@Override
	public boolean documentChanged(DocumentEvent event) {
		return documentChanged2(event) != null;
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getLegalContentTypes()
	 */
	@Override
	public String[] getLegalContentTypes() {
		return new String[]{BuildConsolePartition.CONSOLE_PARTITION_TYPE};
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getContentType(int)
	 */
	@Override
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
	@Override
	public ITypedRegion[] computePartitioning(int offset, int length) {
		if (offset == 0 && length == fDocument.getLength()) {
			return fPartitions.toArray(new ITypedRegion[fPartitions.size()]);
		}
		int end = offset + length;
		List<ITypedRegion> list = new ArrayList<ITypedRegion>();
		for (int i = 0; i < fPartitions.size(); i++) {
			ITypedRegion partition = fPartitions.get(i);
			int partitionStart = partition.getOffset();
			int partitionEnd = partitionStart + partition.getLength();
			if ((offset >= partitionStart && offset <= partitionEnd) ||
					(offset < partitionStart && end >= partitionStart)) {
				list.add(partition);
			}
		}
		return list.toArray(new ITypedRegion[list.size()]);
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getPartition(int)
	 */
	@Override
	public ITypedRegion getPartition(int offset) {
		for (int i = 0; i < fPartitions.size(); i++) {
			ITypedRegion partition = fPartitions.get(i);
			int start = partition.getOffset();
			int end = start + partition.getLength();
			if (offset >= start && offset < end) {
				return partition;
			}
		}
		return null;
	}

	@Override
	public IRegion documentChanged2(DocumentEvent event) {
		String text = event.getText();
		if (getDocument().getLength() == 0) {
			// cleared
			fPartitions.clear();
			return new Region(0, 0);
		}
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
	protected void checkOverflow(boolean allowSlack) {
		if (fMaxLines <= 0)
			return;
		int nLines = fDocument.getNumberOfLines();
		if (nLines <= (allowSlack ? fMaxLines * 2 : fMaxLines) + 1)
			return;

		int overflow = 0;
		try {
			overflow = fDocument.getLineOffset(nLines - fMaxLines);
		} catch (BadLocationException e) {
		}
		// Update partitions
		List<ITypedRegion> newParitions = new ArrayList<ITypedRegion>(fPartitions.size());
		Iterator<ITypedRegion> partitions = fPartitions.iterator();
		while (partitions.hasNext()) {
			ITypedRegion region = partitions.next();
			if (region instanceof BuildConsolePartition) {
				BuildConsolePartition messageConsolePartition = (BuildConsolePartition) region;

				ITypedRegion newPartition = null;
				int offset = region.getOffset();
				String type = messageConsolePartition.getType();
				if (offset < overflow) {
					int endOffset = offset + region.getLength();
					if (endOffset < overflow || BuildConsolePartition.isProblemPartitionType(type)) {
						// Remove partition,
						// partitions with problem markers can't be split - remove them too.
					} else {
						// Split partition
						int length = endOffset - overflow;
						newPartition = messageConsolePartition.createNewPartition(0, length, type);
					}
				} else {
					// Modify partition offset
					offset = messageConsolePartition.getOffset() - overflow;
					newPartition = messageConsolePartition.createNewPartition(offset, messageConsolePartition.getLength(), type);
				}
				if (newPartition != null) {
					newParitions.add(newPartition);
				}
			}
		}
		fPartitions = newParitions;
		fDocumentMarkerManager.moveToFirstError();

		try {
			fDocument.replace(0, overflow, ""); //$NON-NLS-1$
		} catch (BadLocationException e) {
		}
	}

	/**
	 * Adds a new partition, combining with the previous partition if possible.
	 */
	private BuildConsolePartition addPartition(BuildConsolePartition partition) {
		if (fPartitions.isEmpty()) {
			fPartitions.add(partition);
		} else {
			int index = fPartitions.size() - 1;
			BuildConsolePartition last = (BuildConsolePartition) fPartitions.get(index);
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

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty() == BuildConsolePreferencePage.PREF_BUILDCONSOLE_LINES) {
			setDocumentSize(BuildConsolePreferencePage.buildConsoleLines());
		}
	}

	@Override
	public void start(final IProject project) {
		Display display = CUIPlugin.getStandardDisplay();
		if (display != null) {
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					fLogStream = null;
					fLogURI = null;
					fManager.startConsoleActivity(project);
				}
			});
		}


		if (BuildConsolePreferencePage.isClearBuildConsole()) {
			appendToDocument("", null, null); //$NON-NLS-1$
		}
	}

	@Override
	public ConsoleOutputStream getOutputStream() throws CoreException {
		return new BuildOutputStream(this, fManager.getStreamDecorator(BuildConsoleManager.BUILD_STREAM_TYPE_OUTPUT));
	}

	@Override
	public ConsoleOutputStream getInfoStream() throws CoreException {
		return new BuildOutputStream(this, fManager.getStreamDecorator(BuildConsoleManager.BUILD_STREAM_TYPE_INFO));
	}

	@Override
	public ConsoleOutputStream getErrorStream() throws CoreException {
		return new BuildOutputStream(this, fManager.getStreamDecorator(BuildConsoleManager.BUILD_STREAM_TYPE_ERROR));
	}

	/** This method is useful for future debugging and bug-fixing */
	@SuppressWarnings({ "unused", "nls" })
	private void printDocumentPartitioning() {
		System.out.println("Document partitioning: ");
		for (ITypedRegion tr : fPartitions) {
			BuildConsolePartition p = (BuildConsolePartition) tr;
			int start = p.getOffset();
			int end = p.getOffset() + p.getLength();
			String text;
			String isError = "U";
			String type = p.getType();
			if (type == BuildConsolePartition.ERROR_PARTITION_TYPE) {
				isError = "E";
			} else if (type == BuildConsolePartition.WARNING_PARTITION_TYPE) {
				isError = "W";
			} else if (type == BuildConsolePartition.INFO_PARTITION_TYPE) {
				isError = "I";
			} else if (type == BuildConsolePartition.CONSOLE_PARTITION_TYPE) {
				isError = "C";
			}
			try {
				text = fDocument.get(p.getOffset(), p.getLength());
			} catch (BadLocationException e) {
				text = "N/A";
			}
			if (text.endsWith("\n")) {
				text = text.substring(0, text.length() - 1);
			}
			System.out.println("    " + isError + " " + start + "-" + end + ":[" + text + "]");
		}
	}

	/**
	 * @return {@link URI} location of log file.
	 */
	public URI getLogURI() {
		return fLogURI;
	}

	IProject getProject() {
		return fProject;
	}
}
