/*******************************************************************************
 * Copyright (c) 2005, 2011 Wind River Systems, Inc. and others.
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
 * Rename processor that sets up the input page for renaming a global entity.
 */
public class CRenameGlobalProcessor extends CRenameProcessorDelegate {

	public CRenameGlobalProcessor(CRenameProcessor processor, String name) {
		super(processor, name);
		setAvailableOptions(CRefactory.OPTION_IN_CODE_REFERENCES | CRefactory.OPTION_IN_COMMENT
				| CRefactory.OPTION_IN_MACRO_DEFINITION | CRefactory.OPTION_EXHAUSTIVE_FILE_SEARCH);
	}

	@Override
	public int getSaveMode() {
		return RefactoringSaveHelper.SAVE_REFACTORING;
	}
}
