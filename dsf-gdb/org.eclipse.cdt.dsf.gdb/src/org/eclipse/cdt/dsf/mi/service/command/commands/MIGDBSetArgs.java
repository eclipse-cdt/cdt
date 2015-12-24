/*******************************************************************************
 * Copyright (c) 2008, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *     Sergey Prigogin (Google)
 *     Marc Khouzam (Ericsson) - Support empty arguments (bug 412471)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import java.util.ArrayList;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;

/**
 *      -gdb-set args ARGS
 *
 * Set the inferior program arguments, to be used in the next `-exec-run'.
 * @since 1.1
 */
public class MIGDBSetArgs extends MIGDBSet {

	/** @since 4.0 */
	public MIGDBSetArgs(IMIContainerDMContext dmc) {
		this(dmc, new String[0]);
	}

	/** @since 4.0 */
	public MIGDBSetArgs(IMIContainerDMContext dmc, String[] arguments) {
		super(dmc, null);
		fParameters = new ArrayList<Adjustable>();
		fParameters.add(new MIStandardParameterAdjustable("args")); //$NON-NLS-1$
		for (int i = 0; i < arguments.length; i++) {
			fParameters.add(new MIArgumentAdjustable(arguments[i]));
		}
	}

	private static class MIArgumentAdjustable extends MICommandAdjustable {

		public MIArgumentAdjustable(String value) {
			super(value);
		}

		@Override
		public String getAdjustedValue() {
			// Replace and concatenate all occurrences of:
			// ' with "'"
			//   (as ' is used to surround everything else
			//    it has to be quoted or escaped)
			// newline character with $'\n'
			//   (\n is treated literally within quotes or
			//    as just 'n' otherwise, whilst supplying
			//    the newline character literally ends the command)
			// Anything in between and around these occurrences
			// is surrounded by single quotes.
			//   (to prevent bash from carrying out substitutions
			//    or running arbitrary code with backticks or $())
			StringBuilder builder = new StringBuilder();
			builder.append('\'');
			for (int j = 0; j < value.length(); j++) {
				char c = value.charAt(j);
				if (c == '\'') {
					builder.append("'\"'\"'"); //$NON-NLS-1$
				} else if (c == '\n') {
					builder.append("'$'\\n''"); //$NON-NLS-1$
				} else {
					builder.append(c);
				}
			}
			builder.append('\'');
			return builder.toString();
		}
	}
}
