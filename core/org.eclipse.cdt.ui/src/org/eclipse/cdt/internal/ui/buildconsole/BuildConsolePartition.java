/*******************************************************************************
 * Copyright (c) 2002, 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 * Jonah Graham (Kichwa Coders) - Significant rewrite, changed model (Bug 314428)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.text.TypedRegion;

public class BuildConsolePartition extends TypedRegion {

	/** Associated stream */
	private IBuildConsoleStreamDecorator fStream;

	/** Marker associated with this partition if any */
	private ProblemMarkerInfo fMarker;

	/** Number of newlines in this region */
	private int fNewlines;

	/** Partition type */
	public static final String CONSOLE_PARTITION_TYPE = CUIPlugin.getPluginId() + ".CONSOLE_PARTITION_TYPE"; //$NON-NLS-1$

	/** Partition types to report build problems in the console */
	public static final String ERROR_PARTITION_TYPE = CUIPlugin.getPluginId() + ".ERROR_PARTITION_TYPE"; //$NON-NLS-1$
	public static final String INFO_PARTITION_TYPE = CUIPlugin.getPluginId() + ".INFO_PARTITION_TYPE"; //$NON-NLS-1$
	public static final String WARNING_PARTITION_TYPE = CUIPlugin.getPluginId() + ".WARNING_PARTITION_TYPE"; //$NON-NLS-1$

	public BuildConsolePartition(IBuildConsoleStreamDecorator stream, int offset, int length, String type,
			ProblemMarkerInfo marker, int newlines) {
		super(offset, length, type);
		fStream = stream;
		fMarker = marker;
		fNewlines = newlines;
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
	public IBuildConsoleStreamDecorator getStream() {
		return fStream;
	}

	public ProblemMarkerInfo getMarker() {
		return fMarker;
	}

	/**
	 * Return number of newlines represented in this partition.
	 *
	 * @return number of newlines
	 */
	public int getNewlines() {
		return fNewlines;
	}

	public static boolean isProblemPartitionType(String type) {
		return type == BuildConsolePartition.ERROR_PARTITION_TYPE
				|| type == BuildConsolePartition.WARNING_PARTITION_TYPE
				|| type == BuildConsolePartition.INFO_PARTITION_TYPE;
	}

}
