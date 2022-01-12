/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.reducer;

import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ICEditor;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class RemoveUnusedDeclarationsHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof ITextSelection) {
			IWorkbenchPart part = HandlerUtil.getActivePart(event);
			if (part instanceof ICEditor) {
				ICEditor editor = (ICEditor) part;
				IWorkingCopy wc = CUIPlugin.getDefault().getWorkingCopyManager()
						.getWorkingCopy(editor.getEditorInput());
				if (wc != null && wc.getResource() != null) {
					RefactoringRunner runner = new RemoveUnusedDeclarationsRefactoringRunner(wc, selection,
							editor.getEditorSite(), wc.getCProject());
					runner.run();
				}
			}
		}

		return null;
	}
}
