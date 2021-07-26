/*******************************************************************************
 * Copyright (c) 2019 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.jsoncdb.intel;

import org.eclipse.cdt.jsoncdb.core.participant.DefaultToolDetectionParticipant;

/**
 * C, OS X, clang.
 *
 * @author Martin Weber
 */
public class IclToolDetectionParticipant extends DefaultToolDetectionParticipant {

	public IclToolDetectionParticipant() {
		super("icl", IntelCToolCommandlineParser.INSTANCE); //$NON-NLS-1$
	}
}
