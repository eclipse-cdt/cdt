/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Dmitry Kozlov (CodeSourcery) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.jface.text.ITypedRegion;

/**
 * Manages current position of highlighted error in BuildConsole
 */
class DocumentMarkerManager {

	BuildConsoleDocument fDocument;
	BuildConsolePartitioner fPartitioner;

	int highlightedPartitionIndex = -1;

	DocumentMarkerManager(BuildConsoleDocument document, BuildConsolePartitioner partitioner) {
		fDocument = document;
		fPartitioner = partitioner;
	}

	/** Increment index */
	void moveToNextError() {
		if (fPartitioner.fPartitions.size() == 0)
			return;
		if (highlightedPartitionIndex == -1) {
			moveToFirstError();
			return;
		}
		int i = highlightedPartitionIndex + 1;
		do {
			if (i == fPartitioner.fPartitions.size()) {
				i = 0;
			}
			String type = fPartitioner.fPartitions.get(i).getType();
			if (BuildConsolePartition.isProblemPartitionType(type)) {
				highlightedPartitionIndex = i;
				return;
			} else {
				i++;
			}
		} while (highlightedPartitionIndex != i);
	}

	/** Decrement index */
	void moveToPreviousError() {
		if (fPartitioner.fPartitions.size() == 0)
			return;
		if (highlightedPartitionIndex == -1) {
			moveToFirstError();
			return;
		}

		int i = highlightedPartitionIndex - 1;
		do {
			if (i == -1) {
				i = fPartitioner.fPartitions.size() - 1;
			}
			String type = fPartitioner.fPartitions.get(i).getType();
			if (BuildConsolePartition.isProblemPartitionType(type)) {
				highlightedPartitionIndex = i;
				return;
			} else {
				i--;
			}
		} while (highlightedPartitionIndex != i);
	}

	void moveToFirstError() {
		for (int i = 0; i < fPartitioner.fPartitions.size(); i++) {
			String type = fPartitioner.fPartitions.get(i).getType();
			if (BuildConsolePartition.isProblemPartitionType(type)) {
				highlightedPartitionIndex = i;
				return;
			}
		}
		highlightedPartitionIndex = -1;
	}

	/** Returns true if offset points to error partition and false otherwise */
	boolean moveToErrorByOffset(int offset) {
		ITypedRegion p = fPartitioner.getPartition(offset);
		String type = p.getType();
		if (BuildConsolePartition.isProblemPartitionType(type)) {
			highlightedPartitionIndex = fPartitioner.fPartitions.indexOf(p);
			return true;
		}
		return false;
	}

	/** Get marker for current error */
	ProblemMarkerInfo getCurrentErrorMarker() {
		BuildConsolePartition p = getCurrentPartition();
		if (p != null) {
			return p.getMarker();
		} else {
			return null;
		}
	}

	/** Get partition for current error */
	BuildConsolePartition getCurrentPartition() {
		if (0 <= highlightedPartitionIndex && highlightedPartitionIndex < fPartitioner.fPartitions.size()) {
			BuildConsolePartition p = fPartitioner.fPartitions.get(highlightedPartitionIndex);
			return p;
		}
		return null;
	}

	void clear() {
		highlightedPartitionIndex = -1;
	}
}
