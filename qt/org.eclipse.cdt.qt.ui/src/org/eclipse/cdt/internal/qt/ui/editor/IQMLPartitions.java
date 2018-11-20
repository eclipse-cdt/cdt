/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.editor;

/**
 * Constants for the various partitions created by {@link QMLPartitionScanner}.
 */
public interface IQMLPartitions {
	final String QML_PARTITIONING = "___qml_partitioning"; //$NON-NLS-1$
	final String QML_SINGLE_LINE_COMMENT = "__qml_single_comment"; //$NON-NLS-1$
	final String QML_MULTI_LINE_COMMENT = "__qml_multiline_comment"; //$NON-NLS-1$
	final String QML_STRING = "__qml_string"; //$NON-NLS-1$

	final String[] ALL_QMLPARTITIONS = { IQMLPartitions.QML_SINGLE_LINE_COMMENT, IQMLPartitions.QML_MULTI_LINE_COMMENT,
			IQMLPartitions.QML_STRING };
}
