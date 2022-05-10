/*******************************************************************************
 * Copyright (c) 2022 COSEDA Technologies GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dominic Scharfe (COSEDA Technologies GmbH) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.editor.multipage;

import org.eclipse.cdt.internal.ui.editor.CEditorActionContributor;
import org.eclipse.cdt.ui.ICEditor;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;

/**
 * Delegates to {@link CEditorActionContributor} to enable the CDT actions for the {@link MultiPageEditorExample}.
 */
public class MultiPageEditorContributorExample extends MultiPageEditorActionBarContributor {
	private final CEditorActionContributor cEditorActionContributor;

	public MultiPageEditorContributorExample() {
		cEditorActionContributor = new CEditorActionContributor();
	}

	@Override
	public void init(IActionBars bars) {
		super.init(bars);
		cEditorActionContributor.init(bars);
	}

	@Override
	public void setActivePage(IEditorPart part) {
		if (cEditorActionContributor != null) {
			if (part instanceof ICEditor) {
				cEditorActionContributor.setActiveEditor(part);
			} else {
				cEditorActionContributor.setActiveEditor(null);
			}
		}
	}

	@Override
	public void dispose() {
		cEditorActionContributor.dispose();
		super.dispose();
	}
}
