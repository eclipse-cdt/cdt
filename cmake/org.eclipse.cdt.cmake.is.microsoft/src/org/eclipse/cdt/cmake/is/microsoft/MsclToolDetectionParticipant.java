/*******************************************************************************
 * Copyright (c) 2019 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.microsoft;

import org.eclipse.cdt.cmake.is.core.participant.DefaultToolDetectionParticipant;

/**
 * Microsoft C and C++ compiler (cl).
 *
 * @author Martin Weber
 */
public class MsclToolDetectionParticipant extends DefaultToolDetectionParticipant {

	public MsclToolDetectionParticipant() {
		super("cl", true, "exe", new MsclToolCommandlineParser()); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
