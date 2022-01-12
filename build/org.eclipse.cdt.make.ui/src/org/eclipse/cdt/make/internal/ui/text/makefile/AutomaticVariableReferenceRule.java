/*******************************************************************************
 * Copyright (c) 2013, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.text.makefile;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.gnu.IVariableDefinition;
import org.eclipse.cdt.make.internal.core.makefile.gnu.GNUMakefile;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WordRule;

/**
 * Rule used to highlight automatic variables in the editor.
 */
public class AutomaticVariableReferenceRule extends WordRule {
	private static final char DOLLAR_SIGN = '$';

	/**
	 * Constructor.
	 * @param token - the token to be returned by the rule.
	 */
	public AutomaticVariableReferenceRule(IToken token) {
		super(new IWordDetector() {
			int count = 0;

			@Override
			public boolean isWordPart(char c) {
				count++;
				return count <= 2;
			}

			@Override
			public boolean isWordStart(char c) {
				count = 1;
				return c == DOLLAR_SIGN;
			}
		});
		// Add automatic variables
		for (IDirective var : new GNUMakefile().getBuiltins()) {
			if (var instanceof IVariableDefinition) {
				addWord(DOLLAR_SIGN + ((IVariableDefinition) var).getName(), token);
			}
		}
		// Add also $0-$9 variables
		for (int n = 0; n <= 9; n++) {
			addWord(Character.toString(DOLLAR_SIGN) + n, token);
		}
	}

}
