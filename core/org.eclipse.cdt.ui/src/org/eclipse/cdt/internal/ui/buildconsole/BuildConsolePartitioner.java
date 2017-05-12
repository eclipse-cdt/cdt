/*******************************************************************************
 * Copyright (c) 2002, 2017 QNX Software Systems and others.
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
 *     Jonah Graham (Kichwa Coders) - Significant rewrite, changed model (Bug 314428)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
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

import org.eclipse.cdt.internal.ui.buildconsole.BuildConsolePartitionerEditData.UpdateUIData;
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
	private static final int UDPATE_DELAY_MS = 75;

	/**
	 * @see #UDPATE_DELAY_MS
	 */
	private void scheduleUpdate() {
		Display display = CUIPlugin.getStandardDisplay();
		if (display != null) {
			display.timerExec(UDPATE_DELAY_MS, this::updateUI);
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

		/*
		 * The order of these statements matters, the setting the contents of
		 * the document causes listeners to eventually come back and get the
		 * partitions, so the new partitions have to be in place first
		 */
		fPartitions = update.getNewPartitions();

		if (update.needsClearDocumentMarkerManager()) {
			fDocumentMarkerManager.clear();
		}

		/*
		 * This call is slow, it updates the UI as a side effect.
		 *
		 * XXX: Doing a set on the whole document means that all the line
		 * numbers need to be recalculated. This can be optimized further by
		 * keeping track of what needs to be edited. However, for now this
		 * optimization has not been done because although this leads to
		 * increased CPU usage, it does not lead to a delay in total processing
		 * time, but rather to a decrease in frame rate. Furthermore, if the
		 * document overflows, the document's line numbers need to be
		 * recalculated anyway, so little benefit.
		 */
		fDocument.set(update.getNewContents());
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
		fManager.showConsole();
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
			fDocumentMarkerManager.clear();
			fEditData.clear();
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
