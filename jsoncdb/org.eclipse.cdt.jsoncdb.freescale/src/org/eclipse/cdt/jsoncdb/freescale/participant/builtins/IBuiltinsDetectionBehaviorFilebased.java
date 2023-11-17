/*******************************************************************************
 * Copyright (c) 2023 Thomas Kucharczyk
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.jsoncdb.freescale.participant.builtins;

import java.io.File;

import org.eclipse.cdt.jsoncdb.core.participant.builtins.IBuiltinsDetectionBehavior;

/**
 * Specifies how built-in compiler macros and include path detection is handled
 * for a specific compiler.
 *
 * @author Thomas Kucharcyzk
 */
public interface IBuiltinsDetectionBehaviorFilebased extends IBuiltinsDetectionBehavior {
	/**
	 * Processor will try to parse a file given by this function
	 */
	File getFile();
}
