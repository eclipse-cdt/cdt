/*******************************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

	public void appendToDocument(final String text, final BuildConsoleStream stream) {
		if( text.length() == 0 )
			return;
		
		Runnable r = new Runnable() {

			public void run() {
				fLastStream = stream;
				try {
					if (stream == null) {
						fDocument.set(text);
					} else {
						fDocument.replace(fDocument.getLength(), 0, text);
						checkOverflow();
					}
				} catch (BadLocationException e) {
				}
			}
		};
		Display display = CUIPlugin.getStandardDisplay();
		if (display != null) {
			display.asyncExec(r);
		}
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

	public void start(IProject project) {
		if (BuildConsolePreferencePage.isClearBuildConsole()) {
			appendToDocument("", null); //$NON-NLS-1$
		}
		fManager.startConsoleActivity(project);
	}

	public class BuildOutputStream extends ConsoleOutputStream {

		final BuildConsoleStream fStream;

		public BuildOutputStream(BuildConsoleStream stream) {
			fStream = stream;
		}

		public void flush() throws IOException {
			if( fBuffer.length() > 0 )
				appendToDocument(readBuffer(), fStream);
			fManager.showConsole();
		}

		public void close() throws IOException {
			flush();
		}

		public synchronized void write(byte[] b, int off, int len) throws IOException {
			super.write(b, off, len);
			if( fBuffer.length() > 4096 )
				flush();
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