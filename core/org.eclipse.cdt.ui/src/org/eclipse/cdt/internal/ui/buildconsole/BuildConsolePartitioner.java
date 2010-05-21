/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 *     Andrew Gvozdev (Quoin Inc.)  - Copy build log (bug 306222)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.osgi.service.prefs.Preferences;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.preferences.BuildConsolePreferencePage;

public class BuildConsolePartitioner
		implements
			IDocumentPartitioner,
			IDocumentPartitionerExtension,
			IConsole,
			IPropertyChangeListener {

	private IProject fProject;

	/**
	 * List of partitions
	 */
	List<ITypedRegion> fPartitions = new ArrayList<ITypedRegion>(5);

	private int fMaxLines;

	/**
	 * The stream that was last appended to
	 */
	BuildConsoleStreamDecorator fLastStream = null;

	BuildConsoleDocument fDocument;
	DocumentMarkerManager fDocumentMarkerManager;
	boolean killed;
	BuildConsoleManager fManager;

	/**
	 * A queue of stream entries written to standard out and standard err.
	 * Entries appended to the end of the queue and removed from the front.
	 * Intentionally a vector to obtain synchronization as entries are added and
	 * removed.
	 */
	Vector<StreamEntry> fQueue = new Vector<StreamEntry>(5);

	private URI fLogURI;
	private OutputStream fLogStream;

	private class StreamEntry {
		static public final int EVENT_APPEND = 0;
		static public final int EVENT_OPEN_LOG = 1;
		static public final int EVENT_CLOSE_LOG = 2;

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
	 * Sets the indicator that stream was closed so logging should be stopped. Should be called when
	 * build process has finished. Note that there could still be unprocessed console
	 * stream entries in the queue being worked on in the background.
	 */
	public void setStreamClosed() {
		fQueue.add(new StreamEntry(StreamEntry.EVENT_CLOSE_LOG));
		asyncProcessQueue();
	}

	/**
	 * @return {@link URI} of build log or {@code null} if not available.
	 */
	static private URI getLogURI(IProject project) {
		URI logURI = null;

		Preferences prefs = BuildConsoleManager.getBuildLogPreferences(project);
		boolean keepLog = prefs.getBoolean(BuildConsoleManager.KEY_KEEP_LOG, BuildConsoleManager.CONSOLE_KEEP_LOG_DEFAULT);
		if (keepLog) {
			String strLocation = prefs.get(BuildConsoleManager.KEY_LOG_LOCATION, BuildConsoleManager.getDefaultConsoleLogLocation(project));
			if (strLocation.trim().length()>0) {
				logURI = URIUtil.toURI(strLocation);
			}
			if (logURI==null) {
				IStatus status= new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID,"Can't determine URI for location=["+strLocation+"]");  //$NON-NLS-1$ //$NON-NLS-2$
				CUIPlugin.log(status);
			}
		}
		return logURI;
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
			int i = fQueue.size();
			if (i > 0) {
				StreamEntry entry = fQueue.get(i - 1);
				// if last stream is the same and we have not exceeded our
				// display write limit, append.
				if (entry.getStream()==stream && entry.getEventType()==StreamEntry.EVENT_APPEND && entry.getMarker()==marker && entry.size()<10000) {
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
	 * Note that all these are processed by the same by the user-interface thread
	 * as of {@link Display#asyncExec(Runnable)}.
	 */
	private void asyncProcessQueue() {
		Runnable r = new Runnable() {
			public void run() {
				StreamEntry entry;
				try {
					entry = fQueue.remove(0);
				} catch (ArrayIndexOutOfBoundsException e) {
					return;
				}
				switch (entry.getEventType()) {
				case StreamEntry.EVENT_OPEN_LOG:
					logOpen();
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
						addStreamEntryToDocument(entry);
						log(entry.getText());
						checkOverflow();
					} catch (BadLocationException e) {
					}
					break;
				case StreamEntry.EVENT_CLOSE_LOG:
					logClose();
					break;
				}
			}
			private void logOpen() {
				fLogURI = getLogURI(fProject);
				if (fLogURI!=null) {
					try {
						IFileStore logStore = EFS.getStore(fLogURI);
						fLogStream = logStore.openOutputStream(EFS.NONE, null);
					} catch (CoreException e) {
						CUIPlugin.log(e);
					} finally {
						BuildConsoleManager.refreshWorkspaceFiles(fLogURI);
					}
				}
			}

			private void log(String text) {
				if (fLogStream!=null) {
					try {
						fLogStream.write(text.getBytes());
						if (fQueue.isEmpty()) {
							fLogStream.flush();
						}
					} catch (IOException e) {
						CUIPlugin.log(e);
					} finally {
						BuildConsoleManager.refreshWorkspaceFiles(fLogURI);
					}
				}
			}

			private void logClose() {
				if (fLogStream!=null) {
					try {
						fLogStream.close();
					} catch (IOException e) {
						CUIPlugin.log(e);
					} finally {
						BuildConsoleManager.refreshWorkspaceFiles(fLogURI);
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
		if ( entry.getMarker() == null ) {
			// It is plain unmarkered console output
			addPartition(new BuildConsolePartition(fLastStream,
					fDocument.getLength(),
					entry.getText().length(),
					BuildConsolePartition.CONSOLE_PARTITION_TYPE));
		} else {
			// this text line in entry is markered with ProblemMarkerInfo,
			// create special partition for it.
			addPartition(new BuildConsolePartition(fLastStream,
					fDocument.getLength(),
					entry.getText().length(),
					BuildConsolePartition.ERROR_PARTITION_TYPE, entry.getMarker()));
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
			return fPartitions.toArray(new ITypedRegion[fPartitions.size()]);
		}
		int end = offset + length;
		List<ITypedRegion> list = new ArrayList<ITypedRegion>();
		for (int i = 0; i < fPartitions.size(); i++) {
			ITypedRegion partition = fPartitions.get(i);
			int partitionStart = partition.getOffset();
			int partitionEnd = partitionStart + partition.getLength();
			if ( (offset >= partitionStart && offset <= partitionEnd) ||
					(offset < partitionStart && end >= partitionStart)) {
				list.add(partition);
			}
		}
		return list.toArray(new ITypedRegion[list.size()]);
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getPartition(int)
	 */
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
				List<ITypedRegion> newParitions = new ArrayList<ITypedRegion>(fPartitions.size());
				Iterator<ITypedRegion> partitions = fPartitions.iterator();
				while (partitions.hasNext()) {
					ITypedRegion region = partitions.next();
					if (region instanceof BuildConsolePartition) {
						BuildConsolePartition messageConsolePartition = (BuildConsolePartition)region;

						ITypedRegion newPartition = null;
						int offset = region.getOffset();
						if (offset < overflow) {
							int endOffset = offset + region.getLength();
							if (endOffset < overflow ||
									messageConsolePartition.getType() == BuildConsolePartition.ERROR_PARTITION_TYPE ) {
								// remove partition,
								// partitions with problem markers can't be split - remove them too
							} else {
								// split partition
								int length = endOffset - overflow;
								newPartition = messageConsolePartition.createNewPartition(0, length, messageConsolePartition.getType());
							}
						} else {
							// modify partition offset
							newPartition = messageConsolePartition.createNewPartition(messageConsolePartition.getOffset()
									- overflow, messageConsolePartition.getLength(), messageConsolePartition.getType());
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

		fLogURI = null;
		fLogStream = null;

		if (BuildConsolePreferencePage.isClearBuildConsole()) {
			appendToDocument("", null, null); //$NON-NLS-1$
		}
	}

	public ConsoleOutputStream getOutputStream() throws CoreException {
		return new BuildOutputStream(this, fManager.getStreamDecorator(BuildConsoleManager.BUILD_STREAM_TYPE_OUTPUT));
	}

	public ConsoleOutputStream getInfoStream() throws CoreException {
		return new BuildOutputStream(this, fManager.getStreamDecorator(BuildConsoleManager.BUILD_STREAM_TYPE_INFO));
	}

	public ConsoleOutputStream getErrorStream() throws CoreException {
		return new BuildOutputStream(this, fManager.getStreamDecorator(BuildConsoleManager.BUILD_STREAM_TYPE_ERROR));
	}

	/** This method is useful for future debugging and bugfixing */
	@SuppressWarnings("unused")
	private void printDocumentPartitioning() {
		System.out.println("Document partitioning: "); //$NON-NLS-1$
		for (ITypedRegion tr : fPartitions) {
			BuildConsolePartition p = (BuildConsolePartition) tr;
			int start = p.getOffset();
			int end = p.getOffset() + p.getLength();
			String text;
			String isError = "U"; //$NON-NLS-1$
			if (p.getType() == BuildConsolePartition.ERROR_PARTITION_TYPE) {
				isError = "E"; //$NON-NLS-1$
			} else if (p.getType() == BuildConsolePartition.CONSOLE_PARTITION_TYPE) {
				isError = "C"; //$NON-NLS-1$
			}
			try {
				text = fDocument.get(p.getOffset(), p.getLength());
			} catch (BadLocationException e) {
				text = "N/A"; //$NON-NLS-1$
			}
			if (text.endsWith("\n")) { //$NON-NLS-1$
				text = text.substring(0, text.length() - 1);
			}
			System.out.println("    " + isError + " " + start + "-" + end + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					":[" + text + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * @return {@link URI} location of log file.
	 */
	public URI getLogURI() {
		return fLogURI;
	}
}
