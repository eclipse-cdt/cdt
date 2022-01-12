/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor.asm;

import org.eclipse.cdt.internal.ui.editor.AbstractCModelOutlinePage;
import org.eclipse.cdt.ui.actions.CustomFiltersActionGroup;
import org.eclipse.cdt.ui.actions.OpenViewActionGroup;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Content outline page for assembly translation units.
 *
 * @since 5.0
 */
public class AsmContentOutlinePage extends AbstractCModelOutlinePage {

	/**
	 * Creates a new outline page for the given editor.
	 * @param editor
	 */
	public AsmContentOutlinePage(ITextEditor editor) {
		super("#ASMOutlineContext", editor); //$NON-NLS-1$
	}

	@Override
	protected OpenViewActionGroup createOpenViewActionGroup() {
		OpenViewActionGroup ovag = new OpenViewActionGroup(this);
		ovag.setEnableIncludeBrowser(false);
		ovag.setSuppressCallHierarchy(true);
		ovag.setSuppressTypeHierarchy(true);
		return ovag;
	}

	@Override
	protected ActionGroup createCustomFiltersActionGroup() {
		return new CustomFiltersActionGroup("org.eclipse.cdt.ui.AsmOutlinePage", getTreeViewer()); //$NON-NLS-1$
	}

}
