/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.scanner.SignificantMacros;

/**
 * Significant macros describe the conditions under which the preprocessor selects
 * the same active code branches in a file.
 * @since 5.4
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISignificantMacros {
	interface IVisitor {
		/**
		 * Returns whether to continue the visit.
		 */
		boolean visitDefined(char[] macro);

		/**
		 * Returns whether to continue the visit.
		 */
		boolean visitUndefined(char[] macro);

		/**
		 * Returns whether to continue the visit.
		 */
		boolean visitValue(char[] macro, char[] value);
	}

	ISignificantMacros NONE = new SignificantMacros(CharArrayUtils.EMPTY);

	/**
	 * Returns whether visitor continued its visit till the end.
	 */
	boolean accept(IVisitor visitor);

	/**
	 * Returns the significant macros encoded as an array of characters.
	 */
	char[] encode();
}
