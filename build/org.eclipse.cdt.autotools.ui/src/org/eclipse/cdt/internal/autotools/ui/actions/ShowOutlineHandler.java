/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.actions;

import org.eclipse.cdt.autotools.ui.editors.AutoconfEditor;
import org.eclipse.cdt.internal.autotools.ui.editors.QuickOutlineDialog;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class ShowOutlineHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof AutoconfEditor) {
			QuickOutlineDialog quickOutlinePopupDialog = new QuickOutlineDialog(editor.getSite().getShell(), SWT.NONE,
					(AutoconfEditor) editor);
			quickOutlinePopupDialog.setVisible(true);
		}
		return null;
	}

}
