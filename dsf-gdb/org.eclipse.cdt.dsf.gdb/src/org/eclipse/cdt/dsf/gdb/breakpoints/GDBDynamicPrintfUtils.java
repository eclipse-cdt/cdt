/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Support Dynamic Printf (Bug 400628)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.breakpoints;

import org.eclipse.cdt.dsf.concurrent.Immutable;

/**
 * Utility methods to help deal with Dynamic Printf logic.
 *
 * @since 4.4
 */
public class GDBDynamicPrintfUtils {

	@Immutable
	public static class GDBDynamicPrintfString {
		private boolean fValid;
		private String fStringSection;
		private String[] fArgs;
		private String fErrorMessage;

		public GDBDynamicPrintfString(String str) {
			if (str == null) {
				fErrorMessage = Messages.DynamicPrintf_Invalid_string;
				return;
			}

			str = str.trim();

			// No point in having an empty string, just disable the dprintf instead
			if (str.isEmpty()) {
				fErrorMessage = Messages.DynamicPrintf_Invalid_string;
				return;
			}

			// First character must be a double-quote
			if (str.charAt(0) != '"') {
				fErrorMessage = Messages.DynamicPrintf_Printf_must_start_with_quote;
				return;
			}

			// Now go through the string and find the closing
			// double-quote (but ignore any \")
			char[] chars = str.toCharArray();
			int closingQuoteIndex = 0;
			for (int i = 1; i < chars.length; i++) {
				switch (chars[i]) {
				case '\\':
					// Next char can be ignored
					i++;
					break;
				case '"':
					closingQuoteIndex = i;
					break;
				case '%':
					// We either found a second %, which we must skip to
					// avoid parsing it again, or we didn't and we know
					// our next character must be part of the format.
					// In both cases we can and should skip the next character.
					// Note that we are not going to make sure the format is
					// correct as that becomes really complex.
					i++;
					break;
				}

				// If we found the closing double-quote, there is no need to keep counting
				if (closingQuoteIndex > 0)
					break;
			}

			if (closingQuoteIndex < 1) {
				// Didn't find a closing double-quote!
				fErrorMessage = Messages.DynamicPrintf_Printf_missing_closing_quote;
				return;
			}

			// We extract the string part of the printf string leaving the arguments
			fStringSection = str.substring(0, closingQuoteIndex + 1);

			if (closingQuoteIndex + 1 >= str.length()) {
				// No more characters after the string part
				fArgs = new String[0];
			} else {
				String argString = str.substring(closingQuoteIndex + 1).trim();

				if (argString.charAt(0) != ',') {
					fErrorMessage = Messages.DynamicPrintf_Missing_comma;
					return;
				}

				// Remove the first , that separates the format
				// from the arguments
				String printfArguments = argString.substring(1);
				fArgs = new String[] { printfArguments };
			}

			// Everything is ok!
			fValid = true;
		}

		public boolean isValid() {
			return fValid;
		}

		public String getString() {
			if (!isValid())
				return ""; //$NON-NLS-1$
			return fStringSection;
		}

		public String[] getArguments() {
			if (!isValid())
				return new String[0];
			return fArgs;
		}

		public String getErrorMessage() {
			return fErrorMessage;
		}
	}
}
