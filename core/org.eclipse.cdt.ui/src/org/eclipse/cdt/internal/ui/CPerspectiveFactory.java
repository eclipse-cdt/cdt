/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import org.eclipse.cdt.internal.ui.wizards.CWizardRegistry;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

public class CPerspectiveFactory implements IPerspectiveFactory {
	/**
	 * Constructs a new Default layout engine.
	 */
	public CPerspectiveFactory() {
		super();
	}

	/**
	 * @see IPerspectiveFactory#createInitialLayout
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();

		IFolderLayout folder1 = layout.createFolder("topLeft", IPageLayout.LEFT, (float) 0.25, editorArea); //$NON-NLS-1$
		folder1.addView(ProjectExplorer.VIEW_ID);
		folder1.addPlaceholder(CUIPlugin.CVIEW_ID);
		folder1.addPlaceholder(IPageLayout.ID_RES_NAV);
		folder1.addPlaceholder(IPageLayout.ID_BOOKMARKS);

		IFolderLayout folder2 = layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.75, editorArea); //$NON-NLS-1$
		folder2.addView(IPageLayout.ID_PROBLEM_VIEW);
		folder2.addView(IPageLayout.ID_TASK_LIST);
		folder2.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		folder2.addView(IPageLayout.ID_PROP_SHEET);

		IFolderLayout folder3 = layout.createFolder("topRight", IPageLayout.RIGHT, (float) 0.75, editorArea); //$NON-NLS-1$
		folder3.addView(IPageLayout.ID_OUTLINE);

		layout.addActionSet(CUIPlugin.SEARCH_ACTION_SET_ID);
		layout.addActionSet(CUIPlugin.ID_CELEMENT_CREATION_ACTION_SET);
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);

		// views - build console
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);

		// views - searching
		layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);

		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(CUIPlugin.CVIEW_ID);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);

		addCWizardShortcuts(layout);
	}

	private void addCWizardShortcuts(IPageLayout layout) {
		// new actions - C project creation wizard
		String[] wizIDs = CWizardRegistry.getProjectWizardIDs();
		for (int i = 0; i < wizIDs.length; ++i) {
			// Hide the C Project and C++ Project wizards until we can remove them
			if (!wizIDs[i].endsWith(".NewCWizard1") && !wizIDs[i].endsWith(".NewCWizard2")) { //$NON-NLS-1$ //$NON-NLS-2$
				layout.addNewWizardShortcut(wizIDs[i]);
			}
		}
		// new actions - C folder creation wizard
		wizIDs = CWizardRegistry.getFolderWizardIDs();
		for (int i = 0; i < wizIDs.length; ++i) {
			layout.addNewWizardShortcut(wizIDs[i]);
		}
		// new actions - C file creation wizard
		wizIDs = CWizardRegistry.getFileWizardIDs();
		for (int i = 0; i < wizIDs.length; ++i) {
			layout.addNewWizardShortcut(wizIDs[i]);
		}
		// new actions - C type creation wizard
		wizIDs = CWizardRegistry.getTypeWizardIDs();
		for (int i = 0; i < wizIDs.length; ++i) {
			layout.addNewWizardShortcut(wizIDs[i]);
		}
	}
}
