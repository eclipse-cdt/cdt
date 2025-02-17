/*******************************************************************************
 * Copyright (c) 2025 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.switchtolsp;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This interface can be implemented by CDT-LSP to display a banner in the {@link CEditor}
 * to encourage users to try the new C/C++ editing experience based on CDT/LSP
 *
 * See org.eclipse.cdt.lsp/OSGI-INF/org.eclipse.cdt.lsp.internal.switchtolsp.SwitchToLsp.xml
 * for the use.
 *
 * This interface is not public API, it is provided as a hook for CDT LSP and may
 * be changed at any point.
 */
public interface ISwitchToLsp {

	/**
	 * Create the banner controls for the "try new experience"
	 *
	 * @param part the editor part that the banner is added on
	 * @param parent the parent control
	 * @return the new parent control the editor should use
	 */
	Composite createTryLspEditor(ITextEditor part, Composite parent);

}
