/*******************************************************************************
 * Copyright (c) 2016, 2018 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Jonah Graham (Kichwa Coders) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.ITypedRegion;

public class BuildConsolePartitionerEditData {

	/**
	 * Return data for an update.
	 */
	public interface UpdateUIData {
		/**
		 * The contents have been changed in such a way that an associated
		 * DocumentMarkerManager needs to clear its state. This normally happens
		 * when the document overflows (and older lines are discarded) or when
		 * document is cleared.
		 */
		boolean needsClearDocumentMarkerManager();

		/**
		 * The content's offset since beginning of time.
		 */
		long getOffset();

		/**
		 * New contents for the build console.
		 */
		String getNewContents();

		/**
		 * New partitions that match the new contents.
		 */
		List<BuildConsolePartition> getNewPartitions();

		/**
		 * All the streams that have been written to since the last update.
		 */
		List<IBuildConsoleStreamDecorator> getStreamsNeedingNotifcation();

		/**
		 * True if partitions with problem markers have been added since the last
		 * update.
		 */
		boolean hasProblemsAdded();
	}

	/**
	 * The maximum number of lines the document is allowed to have. This is a
	 * soft limit. 0 or less for unlimited.
	 */
	private int fMaxLines;

	/**
	 * Editable partitions, all modifications are made to this copy of the
	 * partitions, then the UI thread occasionally gets these updates
	 */
	private List<BuildConsolePartition> fEditPartitions = new ArrayList<>();

	/**
	 * Set to true when an edit causes the document marker manager to need to be
	 * cleared out on next UI update.
	 */
	private boolean fClearDocumentMarkerManager = false;

	/**
	 * Offset of the start of the document since the beginning of time.
	 */
	private long fOffset = 0;

	/**
	 * Editable document, all modifications are made to this copy of the
	 * document, then the UI thread occasionally gets these updates
	 */
	private StringBuilder fEditStringBuilder = new StringBuilder();

	/**
	 * Total number of lines in document
	 */
	private int fEditLineCount = 0;

	/**
	 * Set of streams that have been updated since the last UI update.
	 */
	private Set<IBuildConsoleStreamDecorator> fEditStreams = new HashSet<>();

	/**
	 * True if partitions with problem markers have been added since the last UI
	 * update.
	 */
	private boolean fEditProblemsAdded = false;

	public BuildConsolePartitionerEditData(int maxLines) {
		fMaxLines = maxLines;
	}

	public int getMaxLines() {
		return fMaxLines;
	}

	public void setMaxLines(int fMaxLines) {
		this.fMaxLines = fMaxLines;
	}

	/**
	 * Clear the entire document.
	 */
	public void clear() {
		synchronized (this) {
			fEditPartitions.clear();
			fClearDocumentMarkerManager = true;
			fOffset += fEditStringBuilder.length();
			fEditStringBuilder.setLength(0);
			fEditLineCount = 0;
		}
	}

	/**
	 *
	 * Adds the new text to the document.
	 *
	 * @param text
	 *            the text to append, cannot be <code>null</code>.
	 * @param stream
	 *            the stream to append to, cannot be <code>null</code>.
	 * @param marker
	 *            the marker associated with this line of console output, can be
	 *            <code>null</code>
	 *
	 */
	public void append(String text, IBuildConsoleStreamDecorator stream, ProblemMarkerInfo marker) {
		int newlines = (int) text.chars().filter(ch -> ch == '\n').count();
		synchronized (this) {
			fEditStreams.add(stream);

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
				if (marker != null) {
					fEditProblemsAdded = true;
				}
				if (fEditPartitions.isEmpty()) {
					fEditPartitions.add(new BuildConsolePartition(stream, fEditStringBuilder.length(), text.length(),
							partitionType, marker, newlines));
				} else {
					int index = fEditPartitions.size() - 1;
					BuildConsolePartition last = fEditPartitions.get(index);
					/*
					 * Don't permit partitions with markers to be combined
					 */
					boolean canBeCombined = marker == null && last.getMarker() == null;
					/*
					 * Don't permit a single partition to exceed the maximum
					 * number of lines of the whole document, this significantly
					 * simplifies the logic of checkOverflow.
					 */
					canBeCombined = canBeCombined && (fMaxLines <= 0 || (last.getNewlines() + newlines < fMaxLines));
					/*
					 * Don't permit different partition types to be combined
					 */
					canBeCombined = canBeCombined && Objects.equals(last.getType(), partitionType);
					/*
					 * Don't permit different streams to be combined
					 */
					canBeCombined = canBeCombined && Objects.equals(last.getStream(), stream);

					if (canBeCombined) {
						// replace with a single partition
						int combinedOffset = last.getOffset();
						int combinedLength = last.getLength() + text.length();
						int combinedNewlines = last.getNewlines() + newlines;
						BuildConsolePartition partition2 = new BuildConsolePartition(last.getStream(), combinedOffset,
								combinedLength, BuildConsolePartition.CONSOLE_PARTITION_TYPE, null, combinedNewlines);
						fEditPartitions.set(index, partition2);
					} else {
						// different kinds - add a new parition
						fEditPartitions.add(new BuildConsolePartition(stream, fEditStringBuilder.length(),
								text.length(), partitionType, marker, newlines));
					}
				}
				fEditStringBuilder.append(text);
				fEditLineCount += newlines;

				checkOverflow();
			}
		}
	}

	/**
	 * Checks to see if the console buffer has overflowed, and empties the
	 * overflow if needed, updating partitions and hyperlink positions.
	 */
	public void checkOverflow() {
		if (fMaxLines <= 0) {
			return;
		}

		synchronized (this) {

			/*
			 * We actually limit the number of lines to 2 x max lines, bringing
			 * it back to max lines when it overflows. This prevents
			 * recalculating on every update
			 */
			if (fEditLineCount <= fMaxLines * 2)
				return;

			// Update partitions

			int newHeadIndex = fEditPartitions.size();
			int newNewlineCount = 0;
			while (newHeadIndex > 0 && newNewlineCount < fMaxLines) {
				newHeadIndex--;
				BuildConsolePartition part = fEditPartitions.get(newHeadIndex);
				newNewlineCount += part.getNewlines();
			}

			if (newHeadIndex == 0) {
				// Nothing to do
				return;
			}

			int newPartCount = fEditPartitions.size() - newHeadIndex;
			int offsetToOffset = fEditPartitions.get(newHeadIndex).getOffset();
			List<BuildConsolePartition> newParitions = new ArrayList<>(newPartCount);
			Iterator<BuildConsolePartition> partitions = fEditPartitions.listIterator(newHeadIndex);
			while (partitions.hasNext()) {
				BuildConsolePartition partition = partitions.next();

				BuildConsolePartition newPartition = new BuildConsolePartition(partition.getStream(),
						partition.getOffset() - offsetToOffset, partition.getLength(), partition.getType(),
						partition.getMarker(), partition.getNewlines());

				newParitions.add(newPartition);
			}

			fEditPartitions = newParitions;
			fClearDocumentMarkerManager = true;

			fOffset += offsetToOffset;
			fEditStringBuilder.delete(0, offsetToOffset);
			fEditLineCount = newNewlineCount;

		}
	}

	/**
	 * This method is useful for future debugging and bug-fixing
	 */
	@SuppressWarnings("nls")
	public void printDocumentPartitioning() {
		// Non synchronized access, used only for debugging
		System.out.println("Document partitioning: ");
		for (ITypedRegion tr : fEditPartitions) {
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
			text = fEditStringBuilder.substring(p.getOffset(), p.getLength());

			if (text.endsWith("\n")) {
				text = text.substring(0, text.length() - 1);
			}
			System.out.println("    " + isError + " " + start + "-" + end + ":[" + text + "]");
		}
	}

	/**
	 * Obtain the next snapshot of data. This update must be processed by the
	 * UI. i.e. don't call this method unless you are going to handle the update
	 * now.
	 *
	 * @return see {@link UpdateUIData} for details on individual values
	 *         returned.
	 */
	public UpdateUIData getUpdate() {
		boolean clearDocumentMarkerManager;
		boolean problemsAdded;
		long newOffset;
		String newConents;
		List<BuildConsolePartition> newPartitions;
		List<IBuildConsoleStreamDecorator> streamsNeedingNotifcation;

		synchronized (this) {
			newOffset = fOffset;
			newConents = fEditStringBuilder.toString();
			newPartitions = new ArrayList<>(fEditPartitions);
			clearDocumentMarkerManager = fClearDocumentMarkerManager;
			fClearDocumentMarkerManager = false;
			streamsNeedingNotifcation = new ArrayList<>(fEditStreams);
			fEditStreams.clear();
			problemsAdded = fEditProblemsAdded;
			fEditProblemsAdded = false;
		}

		return new UpdateUIData() {

			@Override
			public boolean needsClearDocumentMarkerManager() {
				return clearDocumentMarkerManager;
			}

			@Override
			public List<IBuildConsoleStreamDecorator> getStreamsNeedingNotifcation() {
				return streamsNeedingNotifcation;
			}

			@Override
			public List<BuildConsolePartition> getNewPartitions() {
				return newPartitions;
			}

			@Override
			public String getNewContents() {
				return newConents;
			}

			@Override
			public long getOffset() {
				return newOffset;
			}

			@Override
			public boolean hasProblemsAdded() {
				return problemsAdded;
			}
		};
	}

}