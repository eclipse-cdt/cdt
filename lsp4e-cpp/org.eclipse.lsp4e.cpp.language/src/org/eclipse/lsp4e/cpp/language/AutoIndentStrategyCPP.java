/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language;

import org.eclipse.cdt.internal.ui.text.CAutoIndentStrategy;
import org.eclipse.cdt.ui.CUIPlugin;

@SuppressWarnings("restriction")
public class AutoIndentStrategyCPP extends CAutoIndentStrategy {

	public AutoIndentStrategyCPP() {
		super(CUIPlugin.getDefault().getTextTools().getDocumentPartitioning(), null, true);
	}

}
