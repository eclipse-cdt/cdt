/*******************************************************************************
 * Copyright (c) 2002, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 *     Andrew Gvozdev (Quoin Inc.)  - Copy build log (bug 306222)
 *     Alex Collins (Broadcom Corp.) - Global console
 *     Sergey Prigogin (Google) - Performance improvements
 *     Jonah Graham (Kichwa Coders) - Significant rewrite, changed model (Bug 314428)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.resources.ResourcesUtil;
import org.eclipse.cdt.internal.ui.buildconsole.BuildConsolePartitionerEditData.UpdateUIData;
import org.eclipse.cdt.internal.ui.preferences.BuildConsolePreferencePage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
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
	 * provides implicit lock.
	 *
	 * The partitions are required to be sorted, and the partitions must not have
	 * any gaps. (The Offset + Length of partition N must equals the Offset of
	 * partition N + 1.)
	 */
	List<BuildConsolePartition> fPartitions = new ArrayList<>();
	/**
	 * Active document, must only be accessed form UI thread which provides
	 * implicit lock
	 */
	BuildConsoleDocument fDocument;

	/**
	 * Provides core implementation of partitioner.
	 */
	BuildConsolePartitionerEditData fEditData;

	/**
	 * Set to true if there is an asyncExec for the UI update already scheduled.
	 */
	private AtomicBoolean fEditUiPending = new AtomicBoolean(false);

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

	private int fUpdateDelay = BuildConsolePreferencePage.DEFAULT_BUILDCONSOLE_UPDATE_DELAY_MS;

	private long fOffset;

	/**
	 * Construct a partitioner that is not associated with a specific project
	 */
	public BuildConsolePartitioner(BuildConsoleManager manager) {
		this(null, manager);
	}

	public BuildConsolePartitioner(IProject project, BuildConsoleManager manager) {
		fProject = project;
		fManager = manager;
		fEditData = new BuildConsolePartitionerEditData(BuildConsolePreferencePage.buildConsoleLines());
		fDocument = new BuildConsoleDocument();
		fDocument.setDocumentPartitioner(this);
		fDocumentMarkerManager = new DocumentMarkerManager(fDocument, this);
		fUpdateDelay = BuildConsolePreferencePage.buildConsoleUpdateDelayMs();
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
	 * @see BuildConsolePreferencePage#DEFAULT_BUILDCONSOLE_UPDATE_DELAY_MS
	 */
	private void scheduleUpdate() {
		Display display = CUIPlugin.getStandardDisplay();
		if (display != null) {
			display.timerExec(fUpdateDelay, this::updateUI);
		}
	}

	private void updateUI() {
		fEditUiPending.set(false);
		UpdateUIData update = fEditData.getUpdate();

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
		update.getStreamsNeedingNotifcation().forEach(this::warnOfContentChange);
		fManager.showConsole(update.hasProblemsAdded());

		/*
		 * The order of these statements matters, the setting the contents of
		 * the document causes listeners to eventually come back and get the
		 * partitions, so the new partitions have to be in place first
		 */
		fPartitions = update.getNewPartitions();

		if (update.needsClearDocumentMarkerManager()) {
			fDocumentMarkerManager.clear();
		}

		try {
			long offsetChangeSinceLastUpdate = update.getOffset() - fOffset;
			int toTrim = (int) Math.min(offsetChangeSinceLastUpdate, fDocument.getLength());

			int length = fDocument.getLength();
			String newContents = update.getNewContents();
			String appendContents = newContents.substring(length - toTrim);
			// The append has to be done before the delete from head
			// to avoid document becoming 0 length and therefore the
			// listeners assume the document has been cleared
			fDocument.replace(length + toTrim, 0, appendContents);
			if (toTrim > 0) {
				fDocument.replace(0, toTrim, ""); //$NON-NLS-1$
			}
		} catch (BadLocationException e) {
			fDocument.set(update.getNewContents());
		}

		fOffset = update.getOffset();
	}

	/**
	 * Adds the new text to the document.
	 *
	 * @param text
	 *            the text to append, cannot be <code>null</code>.
	 * @param stream
	 *            the stream to append to, <code>null</code> means to clear
	 *            everything.
	 * @param marker
	 *            the marker associated with this line of console output, can be
	 *            <code>null</code>
	 */
	public void appendToDocument(String text, IBuildConsoleStreamDecorator stream, ProblemMarkerInfo marker) {
		// Log the output to file ASAP, no need to fEditData lock
		log(text);
		if (stream == null) {
			fEditData.clear();
		} else {
			fEditData.append(text, stream, marker);
		}
		Display display = CUIPlugin.getStandardDisplay();
		if (display != null) {
			if (!fEditUiPending.getAndSet(true)) {
				display.asyncExec(this::scheduleUpdate);
			}
		}

	}

	void warnOfContentChange(IBuildConsoleStreamDecorator stream) {
		if (stream != null) {
			ConsolePlugin.getDefault().getConsoleManager().warnOfContentChange(stream.getConsole());
		}
	}

	public IDocument getDocument() {
		return fDocument;
	}

	public void setDocumentSize(int nLines) {
		fEditData.setMaxLines(nLines);
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
		List<BuildConsolePartition> list = computePartitioningAsList(offset, length);
		return list.toArray(new ITypedRegion[list.size()]);
	}

	public List<BuildConsolePartition> computePartitioningAsList(int offset, int length) {
		List<BuildConsolePartition> list;
		if (offset == 0 && length == fDocument.getLength()) {
			list = fPartitions;
		} else {
			int fromIndex = getPartitionIndex(offset);
			int toIndex = getPartitionIndex(offset + length - 1);
			if (fromIndex < 0 && toIndex < 0) {
				// entire range falls outside, should be unreachable
				return Collections.emptyList();
			} else if (fromIndex < 0) {
				fromIndex = 0;
			} else if (toIndex < 0) {
				toIndex = fPartitions.size() - 1;
			}
			list = fPartitions.subList(fromIndex, toIndex + 1);
		}
		return list;
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getPartition(int)
	 */
	@Override
	public BuildConsolePartition getPartition(int offset) {
		int partitionIndex = getPartitionIndex(offset);
		if (partitionIndex >= 0) {
			return fPartitions.get(partitionIndex);
		}
		return null;
	}

	private int getPartitionIndex(int offset) {
		BuildConsolePartition searchTerm = new BuildConsolePartition(null, offset, 0, null, null, 0);
		int index = Collections.binarySearch(fPartitions, searchTerm,
				(a, b) -> Integer.compare(a.getOffset(), b.getOffset()));
		if (index >= 0) {
			// exact match to beginning of a partition
			return index;
		} else if (index == -1) {
			// before first partition
			return -1;
		} else if (index == -(fPartitions.size() + 1)) {
			// either in or after last partition
			BuildConsolePartition lastPartition = fPartitions.get(fPartitions.size() - 1);
			int lastPartitionEnd = lastPartition.getOffset() + lastPartition.getLength();
			assert offset >= lastPartition.getOffset();
			if (offset < lastPartitionEnd) {
				// within last partition
				return fPartitions.size() - 1;
			} else {
				// after last partition
				return -1;
			}
		} else {
			// within a partition
			return -(index + 1) - 1;
		}
	}

	@Override
	public IRegion documentChanged2(DocumentEvent event) {
		String text = event.getText();
		if (getDocument().getLength() == 0) {
			fPartitions.clear();
			fDocumentMarkerManager.clear();
			fEditData.clear();
			return new Region(0, 0);
		}
		List<BuildConsolePartition> affectedRegions = computePartitioningAsList(event.getOffset(), text.length());
		if (affectedRegions.size() == 0) {
			return null;
		}
		if (affectedRegions.size() == 1) {
			return affectedRegions.get(0);
		}
		int affectedLength = 0;
		for (BuildConsolePartition region : affectedRegions) {
			affectedLength += region.getLength();
		}

		return new Region(affectedRegions.get(0).getOffset(), affectedLength);
	}

	public IConsole getConsole() {
		return this;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty() == BuildConsolePreferencePage.PREF_BUILDCONSOLE_LINES) {
			setDocumentSize(BuildConsolePreferencePage.buildConsoleLines());
		}
		if (event.getProperty() == BuildConsolePreferencePage.PREF_BUILDCONSOLE_UPDATE_DELAY_MS) {
			fUpdateDelay = BuildConsolePreferencePage.buildConsoleUpdateDelayMs();
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
			display.asyncExec(() -> fManager.startConsoleActivity(project));
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
