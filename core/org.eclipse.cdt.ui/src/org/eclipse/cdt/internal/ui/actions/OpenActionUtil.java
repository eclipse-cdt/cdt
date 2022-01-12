/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Marc-Andre Laperle - Bug 309112 - Remember dialog bounds
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.cdt.internal.ui.viewsupport.CUILabelProvider;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;

public class OpenActionUtil {

	private static final String DIALOG_SETTINGS_SECTION_NAME = CUIPlugin.PLUGIN_ID
			+ ".OPEN_ACTION_SELECTION_DIALOG_SECTION"; //$NON-NLS-1$

	private OpenActionUtil() {
		// no instance.
	}

	/**
	 * Opens the editor on the given element and subsequently selects it.
	 */
	public static void open(Object element) throws CModelException, PartInitException {
		open(element, true);
	}

	/**
	 * Opens the editor on the given element and subsequently selects it.
	 */
	public static void open(Object element, boolean activate) throws CModelException, PartInitException {
		IEditorPart part = EditorUtility.openInEditor(element, activate);
		if (element instanceof ICElement)
			EditorUtility.revealInEditor(part, (ICElement) element);
	}

	/**
	 * Filters out source references from the given code resolve results.
	 * A utility method that can be called by subclassers.
	 */
	public static List<ISourceReference> filterResolveResults(ICElement[] codeResolveResults) {
		int nResults = codeResolveResults.length;
		List<ISourceReference> refs = new ArrayList<>(nResults);
		for (int i = 0; i < nResults; i++) {
			if (codeResolveResults[i] instanceof ISourceReference)
				refs.add((ISourceReference) codeResolveResults[i]);
		}
		return refs;
	}

	/**
	 * Shows a dialog for resolving an ambiguous C element.
	 * Utility method that can be called by subclasses.
	 */
	public static ICElement selectCElement(ICElement[] elements, Shell shell, String title, String message) {
		return selectCElement(elements, shell, title, message, 0, 0);
	}

	/**
	 * Shows a dialog for resolving an ambiguous C element.
	 * @see CElementLabels
	 * @param elements an array of ambiguous elements.
	 * @param shell parent shell for showing the dialog
	 * @param title title of the dialog
	 * @param message message to be shown in the dialog
	 * @param textFlags text flags to change the label provider
	 * @param imageFlags image flags to change the label provider
	 * @return the selected element or <code>null</code>
	 * @since 4.0
	 */
	public static ICElement selectCElement(ICElement[] elements, Shell shell, String title, String message,
			long textFlags, int imageFlags) {
		int nResults = elements.length;

		if (nResults == 0)
			return null;

		if (nResults == 1)
			return elements[0];

		ILabelProvider labelProvider;
		if (textFlags == 0 && imageFlags == 0) {
			labelProvider = new CElementLabelProvider(
					CElementLabelProvider.SHOW_DEFAULT | CElementLabelProvider.SHOW_QUALIFIED);
		} else {
			labelProvider = new CUILabelProvider(textFlags, imageFlags);
		}

		// TODO: Use CDT's ElementListSelectionDialog as in selectPath().
		org.eclipse.ui.dialogs.ElementListSelectionDialog dialog = new org.eclipse.ui.dialogs.ElementListSelectionDialog(
				shell, labelProvider) {
			@Override
			protected IDialogSettings getDialogBoundsSettings() {
				IDialogSettings settings = CUIPlugin.getDefault().getDialogSettings();
				IDialogSettings section = settings.getSection(DIALOG_SETTINGS_SECTION_NAME);
				if (section == null) {
					section = settings.addNewSection(DIALOG_SETTINGS_SECTION_NAME);
				}
				return section;
			}
		};
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setElements(elements);

		if (dialog.open() == Window.OK) {
			Object[] selection = dialog.getResult();
			if (selection != null && selection.length > 0) {
				nResults = selection.length;
				for (int i = 0; i < nResults; i++) {
					Object current = selection[i];
					if (current instanceof ICElement)
						return (ICElement) current;
				}
			}
		}
		return null;
	}

	/**
	 * Shows a dialog for resolving an ambiguous path.
	 * @param paths an array of ambiguous paths
	 * @param title title of the dialog
	 * @param message message to be shown in the dialog
	 * @return the selected path or <code>null</code>
	 */
	public static IPath selectPath(List<IPath> paths, String title, String message) {
		ILabelProvider renderer = new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IPath) {
					IPath file = (IPath) element;
					return file.lastSegment() + " - " + file.toString(); //$NON-NLS-1$
				}
				return super.getText(element);
			}
		};

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(CUIPlugin.getActiveWorkbenchShell(),
				renderer, false, false);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setElements(paths);

		if (dialog.open() == Window.OK) {
			return (IPath) dialog.getSelectedElement();
		}
		return null;
	}
}
