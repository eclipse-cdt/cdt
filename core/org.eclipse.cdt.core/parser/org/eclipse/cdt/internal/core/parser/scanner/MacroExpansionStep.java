/*******************************************************************************
 * Copyright (c) 2008, 2013 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.rewrite.MacroExpansionExplorer.IMacroExpansionStep;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * Implementation for {@link IMacroExpansionStep}.
 */
public class MacroExpansionStep implements IMacroExpansionStep {
	private final String fBefore;
	private final IMacroBinding fMacroDefinition;
	private final ReplaceEdit[] fReplacements;
	private final IASTFileLocation fMacroLocation;

	public MacroExpansionStep(String before, IMacroBinding def, IASTFileLocation macroLoc, ReplaceEdit[] replacements) {
		fBefore = before;
		fReplacements = replacements;
		fMacroDefinition = def;
		fMacroLocation = macroLoc;
	}

	@Override
	public String getCodeBeforeStep() {
		return fBefore;
	}

	@Override
	public String getCodeAfterStep() {
		StringBuilder result = new StringBuilder();
		int offset = 0;
		for (int i = 0; i < fReplacements.length; i++) {
			ReplaceEdit r = fReplacements[i];
			result.append(fBefore, offset, r.getOffset());
			result.append(r.getText());
			offset = r.getExclusiveEnd();
		}
		result.append(fBefore, offset, fBefore.length());
		return result.toString();
	}

	@Override
	public IMacroBinding getExpandedMacro() {
		return fMacroDefinition;
	}

	@Override
	public ReplaceEdit[] getReplacements() {
		return fReplacements;
	}

	@Override
	public IASTFileLocation getLocationOfExpandedMacroDefinition() {
		return fMacroLocation;
	}
}
