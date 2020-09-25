/*******************************************************************************
 * Copyright (c) 2019 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.intel;

import org.eclipse.cdt.cmake.is.core.participant.DefaultToolDetectionParticipant;

/**
 * C, Linux & OS X, EDG.
 *
 * @author Martin Weber
 */
public class IccToolDetectionParticipant extends DefaultToolDetectionParticipant {

	public IccToolDetectionParticipant() {
		super("icc", IntelCToolCommandlineParser.INSTANCE); //$NON-NLS-1$
		// for the record: builtin detection: -EP -dM for macros, -H for include FILES.
		// NOTE: Windows: /QdM.
	}
}
