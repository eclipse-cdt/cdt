/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.text.TypedRegion;

public class BuildConsolePartition extends TypedRegion {

	/** Associated stream */
	private BuildConsoleStreamDecorator fStream;
	
	/** Marker associated with this partition if any */
	private ProblemMarkerInfo fMarker; 

	/** Partition type */
	public static final String CONSOLE_PARTITION_TYPE = CUIPlugin.getPluginId() + ".CONSOLE_PARTITION_TYPE"; //$NON-NLS-1$	
	
	/** Partition types to report build problems in the console */
	public static final String ERROR_PARTITION_TYPE = CUIPlugin.getPluginId() + ".ERROR_PARTITION_TYPE"; //$NON-NLS-1$  
	public static final String INFO_PARTITION_TYPE = CUIPlugin.getPluginId() + ".INFO_PARTITION_TYPE"; //$NON-NLS-1$  
	public static final String WARNING_PARTITION_TYPE = CUIPlugin.getPluginId() + ".WARNING_PARTITION_TYPE"; //$NON-NLS-1$  
	
	public BuildConsolePartition(BuildConsoleStreamDecorator stream, int offset, int length, String type) {
		super(offset, length, type);
		fStream = stream;
	}

	public BuildConsolePartition(BuildConsoleStreamDecorator stream, int offset, int length, String type, ProblemMarkerInfo marker) {
		super(offset, length, type);
		fStream = stream;
		fMarker = marker;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object partition) {
		if (super.equals(partition)) {
			return fStream.equals(((BuildConsolePartition) partition).getStream());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() + fStream.hashCode();
	}

	/**
	 * Returns this partition's stream
	 * 
	 * @return this partition's stream
	 */
	public BuildConsoleStreamDecorator getStream() {
		return fStream;
	}

	/**
	 * Returns whether this partition is allowed to be combined with the given
	 * partition.
	 * 
	 * @param partition
	 * @return boolean
	 */
	public boolean canBeCombinedWith(BuildConsolePartition partition) {
		// Error partitions never can be combined together
		String type = getType();
		if (isProblemPartitionType(type)) {
			return false; 
		}

		int start = getOffset();
		int end = start + getLength();
		int otherStart = partition.getOffset();
		int otherEnd = otherStart + partition.getLength();
		boolean overlap = (otherStart >= start && otherStart <= end) || (start >= otherStart && start <= otherEnd);
		return getStream() != null && overlap && type.equals(partition.getType()) && getStream().equals(partition.getStream());
	}

	/**
	 * Returns a new partition representing this and the given parition
	 * combined.
	 * 
	 * @param partition
	 * @return partition
	 */
	public BuildConsolePartition combineWith(BuildConsolePartition partition) {
		int start = getOffset();
		int end = start + getLength();
		int otherStart = partition.getOffset();
		int otherEnd = otherStart + partition.getLength();
		int theStart = Math.min(start, otherStart);
		int theEnd = Math.max(end, otherEnd);
		return createNewPartition(theStart, theEnd - theStart, CONSOLE_PARTITION_TYPE);
	}

	/**
	 * Creates a new partition of this type with the given offset, and length.
	 * @param offset
	 * @param length
	 * @return a new partition with the given range
	 */
	public BuildConsolePartition createNewPartition(int offset, int length, String type) {
		return new BuildConsolePartition(getStream(), offset, length, type, getMarker());
	}

	public ProblemMarkerInfo getMarker() {
		return fMarker;
	}

	public static boolean isProblemPartitionType(String type) {
		return type==BuildConsolePartition.ERROR_PARTITION_TYPE
			|| type==BuildConsolePartition.WARNING_PARTITION_TYPE
			|| type==BuildConsolePartition.INFO_PARTITION_TYPE;
	}
	

}
