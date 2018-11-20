/*******************************************************************************
 * Copyright (c) 2004, 2011 Wind River Systems, Inc. and others.
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
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringSaveHelper;

/**
 * Rename processor setting up input page for renaming include directives.
 */
public class CRenameIncludeProcessor extends CRenameProcessorDelegate {

	public CRenameIncludeProcessor(CRenameProcessor input, String kind) {
		super(input, kind);
		setAvailableOptions(CRefactory.OPTION_IN_COMMENT | CRefactory.OPTION_IN_MACRO_DEFINITION
				| CRefactory.OPTION_EXHAUSTIVE_FILE_SEARCH);
		setOptionsForcingPreview(-1);
		setOptionsEnablingExhaustiveSearch(-1);
	}

	@Override
	protected int getAcceptedLocations(int selectedOptions) {
		return selectedOptions | CRefactory.OPTION_IN_INCLUDE_DIRECTIVE;
	}

	@Override
	public int getSaveMode() {
		// TODO(sprigogin): Should it be SAVE_REFACTORING?
		return RefactoringSaveHelper.SAVE_ALL;
	}
}
