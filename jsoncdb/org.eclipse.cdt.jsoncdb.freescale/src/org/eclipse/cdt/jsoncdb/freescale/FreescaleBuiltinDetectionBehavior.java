/*******************************************************************************
 * Copyright (c) 2019-2020 Martin Weber.
 *                    2023 Thomas Kucharcyzk
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.jsoncdb.freescale;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.jsoncdb.core.participant.builtins.IBuiltinsOutputProcessor;
import org.eclipse.cdt.jsoncdb.freescale.participant.builtins.FreescaleOutputProcessor;
import org.eclipse.cdt.jsoncdb.freescale.participant.builtins.IBuiltinsDetectionBehaviorFilebased;

/**
 * The {link IBuiltinsDetectionBehaviorFilebased} for the Freescale compiler.
 * This implementation parses a file to detect compiler built-ins
 *
 * @author Martin Weber, Thomas Kucharcyzk.
 */
public class FreescaleBuiltinDetectionBehavior implements IBuiltinsDetectionBehaviorFilebased {
	private final List<String> enablingArgs = Arrays.asList("-Ldf=\"built_ins.h\""); //$NON-NLS-1$

	@Override
	public List<String> getBuiltinsOutputEnablingArgs() {
		return enablingArgs;
	}

	@Override
	public IBuiltinsOutputProcessor createCompilerOutputProcessor() {
		return new FreescaleOutputProcessor();
	}

	@Override
	public boolean suppressErrormessage() {
		return false;
	}

	@Override
	public File getFile() {
		return new File("built_ins.h"); //$NON-NLS-1$
	}
}
