/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
 *     Axel Mueller - [289339] ported from JDT to CDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.actions.ActionGroup;

public class SurroundWithActionGroup extends ActionGroup {

	private CEditor fEditor;
	private final String fGroup;

	public SurroundWithActionGroup(CEditor editor, String group) {
		fEditor = editor;
		fGroup = group;
	}

	/**
	 * The Menu to show when right click on the editor
	 * {@inheritDoc}
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		ISelectionProvider selectionProvider = fEditor.getSelectionProvider();
		if (selectionProvider == null)
			return;

		ISelection selection = selectionProvider.getSelection();
		if (!(selection instanceof ITextSelection))
			return;

		ITextSelection textSelection = (ITextSelection) selection;
		if (textSelection.getLength() == 0)
			return;

		String menuText = ActionMessages.SurroundWithTemplateMenuAction_SubMenuName;

		MenuManager subMenu = new MenuManager(menuText,
				SurroundWithTemplateMenuAction.SURROUND_WITH_QUICK_MENU_ACTION_ID);
		subMenu.setActionDefinitionId(SurroundWithTemplateMenuAction.SURROUND_WITH_QUICK_MENU_ACTION_ID);
		menu.appendToGroup(fGroup, subMenu);
		subMenu.add(new Action() {
		});
		subMenu.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				manager.removeAll();
				SurroundWithTemplateMenuAction.fillMenu(manager, fEditor);
			}
		});
	}
}
