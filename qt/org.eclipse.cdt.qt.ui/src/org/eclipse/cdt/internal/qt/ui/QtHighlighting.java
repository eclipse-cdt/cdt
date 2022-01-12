/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.cdt.internal.qt.ui;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.internal.qt.core.QtKeywords;
import org.eclipse.cdt.ui.text.ISemanticHighlighter;
import org.eclipse.cdt.ui.text.ISemanticToken;

public class QtHighlighting implements ISemanticHighlighter {
	@Override
	public boolean consumes(ISemanticToken token) {
		IBinding binding = token.getBinding();
		if (binding instanceof IMacroBinding) {
			IASTNode node = token.getNode();
			if (node instanceof IASTName && ((IASTName) node).isReference()) {
				String n = binding.getName();
				return QtKeywords.SIGNALS.equals(n) || QtKeywords.SLOTS.equals(n) || QtKeywords.Q_SIGNALS.equals(n)
						|| QtKeywords.Q_SLOTS.equals(n);
			}
		}

		return false;
	}
}
