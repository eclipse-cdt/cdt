package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.internal.ui.wizards.CWizardRegistry;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

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
	public void createInitialLayout(IPageLayout layout) {
 		String editorArea = layout.getEditorArea();
		
		IFolderLayout folder1= layout.createFolder("topLeft", IPageLayout.LEFT, (float)0.25, editorArea); //$NON-NLS-1$
		folder1.addView(CUIPlugin.CVIEW_ID);
		folder1.addView(IPageLayout.ID_RES_NAV);
		folder1.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		
		IFolderLayout folder2= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea); //$NON-NLS-1$
		folder2.addView(IPageLayout.ID_PROBLEM_VIEW);
		folder2.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		folder2.addView(IPageLayout.ID_PROP_SHEET);
		
		IFolderLayout folder3= layout.createFolder("topRight", IPageLayout.RIGHT,(float)0.75, editorArea); //$NON-NLS-1$
		folder3.addView(IPageLayout.ID_OUTLINE);

		layout.addActionSet(CUIPlugin.SEARCH_ACTION_SET_ID);
		layout.addActionSet(CUIPlugin.ID_CELEMENT_CREATION_ACTION_SET);
		
		// views - build console
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		
		// views - searching
		layout.addShowViewShortcut(SearchUI.SEARCH_RESULT_VIEW_ID);
		
		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(CUIPlugin.CVIEW_ID);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);

		// link - things we should do
		layout.addShowInPart(CUIPlugin.CVIEW_ID);
		layout.addShowInPart(IPageLayout.ID_RES_NAV);
		
		addCWizardShortcuts(layout);
	}
	
	private void addCWizardShortcuts(IPageLayout layout) {
		// new actions - C project creation wizard
		String[] wizIDs = CWizardRegistry.getProjectWizardIDs();
		for (int i = 0; i < wizIDs.length; ++i) {
			layout.addNewWizardShortcut(wizIDs[i]);
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
