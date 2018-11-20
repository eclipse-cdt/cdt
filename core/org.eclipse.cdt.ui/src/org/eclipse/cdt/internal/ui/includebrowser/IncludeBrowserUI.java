/*******************************************************************************
 * Copyright (c) 2007, 2013 Wind River Systems, Inc. and others.
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
 *     Martin Oberhuber (Wind River) - bug 398195: consider external API in IB
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.includebrowser;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

public class IncludeBrowserUI {

	public static void open(final IWorkbenchWindow window, final ICElement input) {
		try {
			ITranslationUnit tu = convertToTranslationUnit(input);
			if (tu != null) {
				IWorkbenchPage page = window.getActivePage();
				IBViewPart result = (IBViewPart) page.showView(CUIPlugin.ID_INCLUDE_BROWSER);
				result.setInput(tu);
			}
		} catch (CoreException e) {
			ExceptionHandler.handle(e, window.getShell(), IBMessages.OpenIncludeBrowserAction_label, null);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public static void open(final ITextEditor editor, final ITextSelection sel) {
		if (editor != null) {
			ICElement inputCElement = CUIPlugin.getDefault().getWorkingCopyManager()
					.getWorkingCopy(editor.getEditorInput());
			open(editor.getSite().getWorkbenchWindow(), inputCElement);
		}
	}

	private static ITranslationUnit convertToTranslationUnit(ICElement input)
			throws CoreException, InterruptedException {
		ITranslationUnit result = null;
		if (input instanceof IInclude) {
			result = findTargetTranslationUnit((IInclude) input);
		}
		if (result == null && input instanceof ISourceReference) {
			result = ((ISourceReference) input).getTranslationUnit();
		}
		return result;
	}

	private static ITranslationUnit findTargetTranslationUnit(IInclude input)
			throws CoreException, InterruptedException {
		ICProject project = input.getCProject();
		if (project != null) {
			IIndex index = CCorePlugin.getIndexManager().getIndex(project,
					IIndexManager.ADD_EXTENSION_FRAGMENTS_INCLUDE_BROWSER);
			index.acquireReadLock();
			try {
				IIndexInclude include = IndexUI.elementToInclude(index, input);
				if (include != null) {
					IIndexFileLocation loc = include.getIncludesLocation();
					if (loc != null) {
						return CoreModelUtil.findTranslationUnitForLocation(loc, project);
					}
				}
			} finally {
				index.releaseReadLock();
			}
		}
		return null;
	}
}
