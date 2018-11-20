/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.rewrite;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.internal.core.parser.scanner.MultiMacroExpansionExplorer;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * Allows to understand macro expansions step by step.
 * @since 5.0
 */
public abstract class MacroExpansionExplorer {

	/**
	 * Representation of a single expansion step or a complete expansion.
	 */
	public interface IMacroExpansionStep {
		/**
		 * Returns the code before this step.
		 */
		String getCodeBeforeStep();

		/**
		 * Returns the code after this step.
		 */
		String getCodeAfterStep();

		/**
		 * Returns an array of replacements representing the change from the code before
		 * this step to the code after this step.
		 */
		ReplaceEdit[] getReplacements();

		/**
		 * Returns the macro that gets expanded in this step, or <code>null</code> for
		 * a step representing a full expansion.
		 */
		IMacroBinding getExpandedMacro();

		/**
		 * Returns the location of the macro-definition that gets expanded in this step,
		 * or <code>null</code> for built-in macros or for a step representing a full expansion.
		 */
		IASTFileLocation getLocationOfExpandedMacroDefinition();
	}

	/**
	 * Creates a macro expansion explorer for a given file location in a translation unit.
	 */
	public static MacroExpansionExplorer create(IASTTranslationUnit tu, IASTFileLocation loc) {
		return new MultiMacroExpansionExplorer(tu, loc);
	}

	/**
	 * Creates a macro expansion explorer for a given region in the outermost file of a
	 * translation unit.
	 */
	public static MacroExpansionExplorer create(IASTTranslationUnit tu, IRegion loc) {
		return new MultiMacroExpansionExplorer(tu, loc);
	}

	/**
	 * Returns the full expansion for the region of this expansion explorer.
	 */
	public abstract IMacroExpansionStep getFullExpansion();

	/**
	 * Returns the total number of available steps for expanding the region of this expansion
	 * explorer.
	 */
	public abstract int getExpansionStepCount();

	/**
	 * Returns a description for the requested step within the expansion of the region of this
	 * expansion explorer.
	 * @throws IndexOutOfBoundsException if step < 0 or step >= getExpansionStepCount().
	 */
	public abstract IMacroExpansionStep getExpansionStep(int step) throws IndexOutOfBoundsException;
}
