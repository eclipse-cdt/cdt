/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.ui.text;

import org.eclipse.cdt.internal.ui.text.CAutoIndentStrategy;
import org.eclipse.cdt.ui.CUIPlugin;

/*
 * 	Class to re-use existing auto-indentation support of CEditor in Generic Editor of LSP4E-CPP.
 */

public class AutoIndentStrategyCPP extends CAutoIndentStrategy {

	public AutoIndentStrategyCPP() {
		// TODO: Pass in the project so the auto edit strategy respects the project's preferences.
		super(CUIPlugin.getDefault().getTextTools().getDocumentPartitioning(), null, true);
	}
}
