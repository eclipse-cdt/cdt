/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui;

/**
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICDTConstants {
	// CDT Extension Points
	public static final String EP_TEXT_HOVERS = "textHovers"; //$NON-NLS-1$

	// Persistance tags.
	public static final String TAG_TEXT_HOVER = "textHover"; //$NON-NLS-1$

	// Atributes
	public static final String ATT_CLASS = "class"; //$NON-NLS-1$
	public static final String ATT_ID = "id"; //$NON-NLS-1$
	public static final String ATT_NAME = "name"; //$NON-NLS-1$
	public static final String ATT_PERSPECTIVE = "perspective"; //$NON-NLS-1$
}
