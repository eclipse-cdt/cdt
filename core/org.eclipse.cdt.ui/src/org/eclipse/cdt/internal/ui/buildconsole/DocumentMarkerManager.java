/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dmitry Kozlov (CodeSourcery) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.jface.text.ITypedRegion;

import org.eclipse.cdt.core.ProblemMarkerInfo;

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
		if ( fPartitioner.fPartitions.size() == 0 ) return;
		if ( highlightedPartitionIndex == -1 ) { 
			moveToFirstError();
			return;
		}
		int i = highlightedPartitionIndex + 1; 
		do {
			if ( i == fPartitioner.fPartitions.size() ) {
				i = 0;
			}
			if ( fPartitioner.fPartitions.get(i).getType() == BuildConsolePartition.ERROR_PARTITION_TYPE ) {
				highlightedPartitionIndex = i;
				return;
			} else {
				i++;
			}
		} while ( highlightedPartitionIndex != i);
	}
	
	/** Decrement index */
	void moveToPreviousError() {	
		if ( fPartitioner.fPartitions.size() == 0 ) return;
		if ( highlightedPartitionIndex == -1 ) { 
			moveToFirstError();
			return;
		}
		
		int i = highlightedPartitionIndex - 1; 
		do {
			if ( i == -1 ) {
				i = fPartitioner.fPartitions.size() - 1;
			}
			if ( fPartitioner.fPartitions.get(i).getType() == BuildConsolePartition.ERROR_PARTITION_TYPE ) {
				highlightedPartitionIndex = i;
				return;
			} else {
				i--;
			}
		} while ( highlightedPartitionIndex != i);
	}
	
	void moveToFirstError() {
		for (int i=0; i<fPartitioner.fPartitions.size(); i++) {
			if ( fPartitioner.fPartitions.get(i).getType() == BuildConsolePartition.ERROR_PARTITION_TYPE ) {
				highlightedPartitionIndex = i;
				return;			
			}
		}
		highlightedPartitionIndex = -1;
	}

	/** Returns true if offset points to error partition and false otherwise */
	boolean moveToErrorByOffset(int offset) {
		ITypedRegion p = fPartitioner.getPartition(offset);
		if ( BuildConsolePartition.ERROR_PARTITION_TYPE.equals(p.getType()) ) {
			highlightedPartitionIndex = fPartitioner.fPartitions.indexOf(p);
			return true;
		}
		return false;
	}
	
	/** Get marker for current error */
	ProblemMarkerInfo getCurrentErrorMarker() {
		BuildConsolePartition p = getCurrentPartition();
		if ( p != null ) { 
			return p.getMarker();
		} else {
			return null;
		}
	}

	/** Get partition for current error */
	BuildConsolePartition getCurrentPartition() {
		if ( 0 <= highlightedPartitionIndex && 	
				highlightedPartitionIndex < fPartitioner.fPartitions.size() ) {
			BuildConsolePartition p = (BuildConsolePartition)fPartitioner.fPartitions.get(highlightedPartitionIndex);
			return p;
		}
		return null;
	}	
	
	void clear() {
		highlightedPartitionIndex = -1;		
	}
}
