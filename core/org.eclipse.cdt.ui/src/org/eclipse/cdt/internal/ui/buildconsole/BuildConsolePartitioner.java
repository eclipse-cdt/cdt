/*******************************************************************************
 * Copyright (c) 2002, 2016 QNX Software Systems and others.
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
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

/*
 * XXX the wrap lines is way too slow to be usable on large {@link #fMaxLines}
 * (not sure limit, 500 seems ok, 10000 is a problem) Best idea may be to do the
 * wrapping "fixed" within the
 * {@link #appendToDocument(String, BuildConsoleStreamDecorator, ProblemMarkerInfo)}
 */
public class BuildConsolePartitioner
		implements IDocumentPartitioner, IDocumentPartitionerExtension, IConsole, IPropertyChangeListener {

	private IProject fProject;

	/**
	 * Active list of partitions, must only be accessed form UI thread which
	 * provides implicit lock
	 */
	List<ITypedRegion> fPartitions = new ArrayList<ITypedRegion>();
	/**
	 * Active document, must only be accessed form UI thread which provides
	 * implicit lock
	 */
	BuildConsoleDocument fDocument;

	/**
	 * Encapsulation of variables for log files that must be accessed
	 * synchronized on fEditData
	 */
	private static class EditData {
		/**
		 * Editable partitions, all modifications are made to this copy of the
		 * partitions, then the UI thread occasionally gets these updates
		 */
		private List<BuildConsolePartition> fEditPartitions = new ArrayList<BuildConsolePartition>();

		/**
		 * Editable document, all modifications are made to this copy of the
		 * document, then the UI thread occasionally gets these updates
		 */
		private StringBuilder fEditDocument = new StringBuilder();

		/**
		 * Total number of lines in document
		 */
		public int fEditLineCount = 0;

		/**
		 * Set to true if there is an asyncExec for the UI update already
		 * scheduled.
		 */
		private boolean fEditUiPending = false;

		/**
		 * Set of streams that have been updated since the last UI update.
		 */
		Set<BuildConsoleStreamDecorator> fEditStreams = new HashSet<>();

	}

	/**
	 * All operations on the edit data needs to be synchronized on this object
	 */
	private EditData fEditData = new EditData();

	private int fMaxLines;

	DocumentMarkerManager fDocumentMarkerManager;
	BuildConsoleManager fManager;

	/**
	 * Encapsulation of variables for log files that must be accessed
	 * synchronized on fLogFile. The key part we want to synchronize is the
	 * writes to fLogStream so that different sources (stderr/stdout) don't get
	 * unnecessarily intermixed.
	 */
	private static class LogFile {
		private OutputStream fLogStream;
		private int openStreamCount = 0;
		/**
		 * This value can be obtained independently without a lock.
		 */
		private URI fLogURI;
	}

	/**
	 * All operations on the log files need to be synchronized on this object
	 */
	private LogFile fLogFile = new LogFile();

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
	}

	/**
	 * Sets the indicator that stream was opened so logging can be started.
	 * Should be called when opening the output stream.
	 */
	public void setStreamOpened() {
		synchronized (fLogFile) {
			fLogFile.openStreamCount++;
			logOpen(false);
		}
	}

	/**
	 * Open the stream for appending. Must be called after a call to
	 * setStreamOpened(). Can be used to reopen a stream for writing after it
	 * has been closed, without emptying the log file.
	 */
	public void setStreamAppend() {
		logOpen(true);
	}

	/**
	 * Sets the indicator that stream was closed so logging should be stopped.
	 * Should be called when build process has finished. Note that there could
	 * still be unprocessed console stream entries in the queue being worked on
	 * in the background.
	 */
	public void setStreamClosed() {
		synchronized (fLogFile) {
			fLogFile.openStreamCount--;
			if (fLogFile.openStreamCount <= 0) {
				fLogFile.openStreamCount = 0;
				if (fLogFile.fLogStream != null) {
					try {
						fLogFile.fLogStream.close();
					} catch (IOException e) {
						CUIPlugin.log(e);
					} finally {
						ResourcesUtil.refreshWorkspaceFiles(fLogFile.fLogURI);
					}
					fLogFile.fLogStream = null;
				}
			}
		}
	}

	/**
	 * Open the log
	 *
	 * @param append
	 *            Set to true if the log should be opened for appending, false
	 *            for overwriting.
	 */
	private void logOpen(boolean append) {
		synchronized (fLogFile) {
			fLogFile.fLogURI = fManager.getLogURI(fProject);
			if (fLogFile.fLogURI != null) {
				try {
					IFileStore logStore = EFS.getStore(fLogFile.fLogURI);
					// Ensure the directory exists before opening the file
					IFileStore dir = logStore.getParent();
					if (dir != null)
						dir.mkdir(EFS.NONE, null);
					int opts = append ? EFS.APPEND : EFS.NONE;
					fLogFile.fLogStream = logStore.openOutputStream(opts, null);
				} catch (CoreException e) {
					CUIPlugin.log(e);
				} finally {
					ResourcesUtil.refreshWorkspaceFiles(fLogFile.fLogURI);
				}
			}
		}
	}

	private void log(String text) {
		synchronized (fLogFile) {
			if (fLogFile.fLogStream != null) {
				try {
					fLogFile.fLogStream.write(text.getBytes());
					fLogFile.fLogStream.flush();
				} catch (IOException e) {
					CUIPlugin.log(e);
				}
			}
		}
	}

	/**
	 * Update the UI after a short delay. The reason for a short delay is to try
	 * and reduce the "frame rate" of the build console updates, this reduces
	 * the total load on the main thread. User's won't be able to tell that
	 * there is an extra delay.
	 *
	 * A too short time has little effect and a too long time starts to be
	 * visible to the user. With my experiments to get under 50% CPU utilization
	 * on the main thread requires at least 35 msec delay between updates. 250
	 * msec leads to visible delay to user and ~20% utilization. And finally the
	 * chosen value, 75 msec leads to ~35% utilization and no user visible
	 * delay.
	 */
	private void scheduleUpdate() {
		Display display = CUIPlugin.getStandardDisplay();
		if (display != null) {
			display.timerExec(75, this::updateUI);
		}
	}

	private void updateUI() {
		String newConents;
		List<ITypedRegion> newPartitions;
		List<BuildConsoleStreamDecorator> streamsNeedingNotifcation;

		synchronized (fEditData) {
			newConents = fEditData.fEditDocument.toString();
			newPartitions = new ArrayList<>(fEditData.fEditPartitions);
			streamsNeedingNotifcation = new ArrayList<>(fEditData.fEditStreams);
			fEditData.fEditStreams.clear();
			fEditData.fEditUiPending = false;
		}

		/*
		 * We refresh the log file here although not technically a UI operation.
		 * We used to refresh the file on every log write, but that is very
		 * expensive and this call has to search the whole workspace to map the
		 * URI to the corresponding IFile. (At the time everything was done in
		 * the UI thread, not just the refresh, so this is an improvement.)
		 *
		 * XXX: Consider caching the IFile.
		 *
		 * XXX: Consider doing the refresh asynchronously in another thread.
		 * Keep in mind that the log file can easily be written at rate 10x
		 * faster than Eclipse's refresh mechanism can detect, which is 1ms.
		 */
		ResourcesUtil.refreshWorkspaceFiles(fLogFile.fLogURI);

		// notify all streams with data we are about to update
		streamsNeedingNotifcation.forEach(this::warnOfContentChange);

		/*
		 * The order of these statements matters, the setting the contents of
		 * the document causes listeners to eventually come back and get the
		 * partitions, so the new partitions have to be in place first
		 */
		fPartitions = newPartitions;

		/*
		 * This call is slow, it updates the UI as a side effect.
		 *
		 * XXX: Doing a set on the whole document means that all the line
		 * numbers need to be recalculated. This can be optimized further by
		 * keeping track of what needs to be edited. However, for now this
		 * optimization has not been done because although this leads to
		 * increased CPU usage, it does not lead to a delay in total processing
		 * time, but rather to a decrease in frame rate.
		 */
		fDocument.set(newConents);
	}

	/**
	 * Adds the new text to the document.
	 *
	 * @param text
	 *            the text to append, cannot be <code>null</code>.
	 * @param stream
	 *            the stream to append to, <code>null</code> means to clear everything.
	 * @param marker
	 *            the marker associated with this line of console output, can be <code>null</code>
	 */
	public void appendToDocument(String text, BuildConsoleStreamDecorator stream, ProblemMarkerInfo marker) {
		// Log the output to file ASAP, no need to fEditData lock
		log(text);
		int newlines = (int) text.chars().filter(ch -> ch == '\n').count();

		synchronized (fEditData) {
			if (stream == null) {
				// special case to empty document
				fEditData.fEditPartitions.clear();
				fDocumentMarkerManager.clear();
				fEditData.fEditDocument.setLength(0);
				fEditData.fEditLineCount = 0;
			} else {
				fEditData.fEditStreams.add(stream);
			}

			if (text.length() > 0) {
				String partitionType;
				if (marker == null) {
					partitionType = BuildConsolePartition.CONSOLE_PARTITION_TYPE;
				} else if (marker.severity == IMarker.SEVERITY_INFO) {
					partitionType = BuildConsolePartition.INFO_PARTITION_TYPE;
				} else if (marker.severity == IMarker.SEVERITY_WARNING) {
					partitionType = BuildConsolePartition.WARNING_PARTITION_TYPE;
				} else {
					partitionType = BuildConsolePartition.ERROR_PARTITION_TYPE;
				}
				if (fEditData.fEditPartitions.isEmpty()) {
					fEditData.fEditPartitions
							.add(new BuildConsolePartition(stream, fEditData.fEditDocument.length(),
									text.length(), partitionType, marker, newlines));
				} else {
					boolean canBeCombined = false;
					if (marker == null) {
						int index = fEditData.fEditPartitions.size() - 1;
						BuildConsolePartition last = fEditData.fEditPartitions.get(index);
						/*
						 * Don't permit a single partition to exceed the maximum
						 * number of lines of the whole document, this
						 * significantly simplifies the logic of checkOverflow.
						 */
						canBeCombined = fMaxLines <= 0 || last.getNewlines() + newlines < fMaxLines;
						canBeCombined = canBeCombined && Objects.equals(last.getType(), partitionType);
						canBeCombined = canBeCombined && Objects.equals(last.getStream(), stream);
						if (canBeCombined) {
							// replace with a single partition
							int combinedOffset = last.getOffset();
							int combinedLength = last.getLength() + text.length();
							int combinedNewlines = last.getNewlines() + newlines;
							BuildConsolePartition partition2 = new BuildConsolePartition(last.getStream(),
									combinedOffset, combinedLength,
									BuildConsolePartition.CONSOLE_PARTITION_TYPE, last.getMarker(),
									combinedNewlines);
							fEditData.fEditPartitions.set(index, partition2);
						}
					}
					if (!canBeCombined) {
						// different kinds - add a new parition
						fEditData.fEditPartitions
								.add(new BuildConsolePartition(stream, fEditData.fEditDocument.length(),
										text.length(), partitionType, marker, newlines));
					}
				}
				fEditData.fEditDocument.append(text);
				fEditData.fEditLineCount += newlines;

				checkOverflow();
			}

			synchronized (fEditData) {
				if (!fEditData.fEditUiPending) {
					Display display = CUIPlugin.getStandardDisplay();
					if (display != null) {
						fEditData.fEditUiPending = true;
						display.asyncExec(this::scheduleUpdate);
					}
				}
			}
		}

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
	}

	@Override
	public void connect(IDocument document) {
		CUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	@Override
	public void disconnect() {
		fDocument.setDocumentPartitioner(null);
		CUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
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
		return new String[] { BuildConsolePartition.CONSOLE_PARTITION_TYPE };
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
			if ((offset >= partitionStart && offset <= partitionEnd)
					|| (offset < partitionStart && end >= partitionStart)) {
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
			fPartitions.clear();
			synchronized (fEditData) {
				fEditData.fEditPartitions.clear();
				fEditData.fEditDocument.setLength(0);
				fEditData.fEditLineCount = 0;
			}
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
		if (fMaxLines <= 0) {
			// XXX: The preferences UI prevents fMaxLines <= 10, so this code
			// is, in practice, unreachable.
			return;
		}

		synchronized (fEditData) {

			/*
			 * We actually limit the number of lines to 2 x max lines, bringing
			 * it back to max lines when it overflows. This prevents
			 * recalculating on every update
			 */
			if (fEditData.fEditLineCount <= fMaxLines * 2)
				return;

			// Update partitions

			int newHeadIndex = fEditData.fEditPartitions.size();
			int newNewlineCount = 0;
			while (newHeadIndex > 0 && newNewlineCount < fMaxLines) {
				newHeadIndex--;
				BuildConsolePartition part = fEditData.fEditPartitions.get(newHeadIndex);
				newNewlineCount += part.getNewlines();
			}

			if (newHeadIndex == 0) {
				// Nothing to do
				return;
			}

			int newPartCount = fEditData.fEditPartitions.size() - newHeadIndex;
			int offsetToOffset = fEditData.fEditPartitions.get(newHeadIndex).getOffset();
			List<BuildConsolePartition> newParitions = new ArrayList<>(newPartCount);
			Iterator<BuildConsolePartition> partitions = fEditData.fEditPartitions.listIterator(newHeadIndex);
			while (partitions.hasNext()) {
				BuildConsolePartition partition = partitions.next();

				BuildConsolePartition newPartition = new BuildConsolePartition(partition.getStream(),
						partition.getOffset() - offsetToOffset, partition.getLength(), partition.getType(),
						partition.getMarker(), partition.getNewlines());

				newParitions.add(newPartition);
			}

			fEditData.fEditPartitions = newParitions;
			fDocumentMarkerManager.clear();

			fEditData.fEditDocument.delete(0, offsetToOffset);
			fEditData.fEditLineCount = newNewlineCount;

		}
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
		synchronized (fLogFile) {
			fLogFile.fLogStream = null;
			fLogFile.fLogURI = null;
		}

		Display display = CUIPlugin.getStandardDisplay();
		if (display != null) {
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
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
		return new BuildOutputStream(this,
				fManager.getStreamDecorator(BuildConsoleManager.BUILD_STREAM_TYPE_OUTPUT));
	}

	@Override
	public ConsoleOutputStream getInfoStream() throws CoreException {
		return new BuildOutputStream(this,
				fManager.getStreamDecorator(BuildConsoleManager.BUILD_STREAM_TYPE_INFO));
	}

	@Override
	public ConsoleOutputStream getErrorStream() throws CoreException {
		return new BuildOutputStream(this,
				fManager.getStreamDecorator(BuildConsoleManager.BUILD_STREAM_TYPE_ERROR));
	}

	/** This method is useful for future debugging and bug-fixing */
	@SuppressWarnings({ "unused", "nls" })
	private void printDocumentPartitioning() {
		// Non synchronized access, used only for debugging
		System.out.println("Document partitioning: ");
		for (ITypedRegion tr : fEditData.fEditPartitions) {
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
			text = fEditData.fEditDocument.substring(p.getOffset(), p.getLength());

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
		return fLogFile.fLogURI;
	}

	IProject getProject() {
		return fProject;
	}
}
