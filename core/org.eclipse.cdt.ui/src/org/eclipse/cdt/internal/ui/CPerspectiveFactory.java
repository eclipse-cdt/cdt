package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.ui.*;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

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
		
		IFolderLayout folder1= layout.createFolder("topLeft", IPageLayout.LEFT, (float)0.25, editorArea);
		folder1.addView(IPageLayout.ID_RES_NAV);
		folder1.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		folder1.addView(CUIPlugin.CVIEW_ID);
		
		IFolderLayout folder2= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea);
		folder2.addView(IPageLayout.ID_TASK_LIST);
		folder2.addView(CUIPlugin.CONSOLE_ID);
		folder2.addView(IPageLayout.ID_PROP_SHEET);
		
		IFolderLayout folder3= layout.createFolder("topRight", IPageLayout.RIGHT,(float)0.75, editorArea);
		folder3.addView(IPageLayout.ID_OUTLINE);

		layout.addActionSet(CUIPlugin.SEARCH_ACTION_SET_ID);
		layout.addActionSet(CUIPlugin.FOLDER_ACTION_SET_ID);
		
		// views - build console
		layout.addShowViewShortcut(CUIPlugin.CONSOLE_ID);
		
		// views - searching
		layout.addShowViewShortcut(SearchUI.SEARCH_RESULT_VIEW_ID);
		
		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(CUIPlugin.CVIEW_ID);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);

		// link - things we should do
		layout.addShowInPart(CUIPlugin.CVIEW_ID);
		layout.addShowInPart(IPageLayout.ID_RES_NAV);

		// new actions - C project creation wizard
		layout.addNewWizardShortcut(CUIPlugin.CLASS_WIZARD_ID);
		layout.addNewWizardShortcut(CUIPlugin.FILE_WIZARD_ID);
		layout.addNewWizardShortcut(CUIPlugin.FOLDER_WIZARD_ID);
	}
}
