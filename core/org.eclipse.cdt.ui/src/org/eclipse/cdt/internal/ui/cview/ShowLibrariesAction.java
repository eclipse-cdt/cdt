package org.eclipse.cdt.internal.ui.cview;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionProviderAction;

import org.eclipse.cdt.internal.ui.CPlugin;

/**
 * The ShowLibrariesAction is the class that adds the filter views to a PackagesView.
 */
class ShowLibrariesAction extends SelectionProviderAction {

	private CView cview; 
	private Shell shell;
	
	/**
	 * Create a new filter action
	 * @param shell the shell that will be used for the list selection
	 * @param packages the PackagesExplorerPart
	 * @param label the label for the action
	 */
	public ShowLibrariesAction(Shell shell, CView cview, String label) {
		super(cview.getViewer(), label);
		this.cview = cview;
		CLibFilter filter = cview.getLibraryFilter();
		setChecked(filter.getShowLibraries());		
		updateToolTipText();
		setEnabled(true);
		this.shell= shell;
	}

	/**
	 * Implementation of method defined on <code>IAction</code>.
	 */
	public void run() {
		CLibFilter filter = cview.getLibraryFilter();
		filter.setShowLibraries(isChecked());
		updateToolTipText();
		saveInPreferences();
		
		cview.getViewer().getControl().setRedraw(false);
		cview.getViewer().refresh();
		cview.getViewer().getControl().setRedraw(true);
	}

	/**
	 * Save the supplied patterns in the preferences for the UIPlugin.
	 * They are saved in the format patern,pattern,.
	 */
	private void saveInPreferences() {
		CPlugin plugin = CPlugin.getDefault();
		Boolean b = new Boolean (cview.getLibraryFilter().getShowLibraries());
	
		plugin.getPreferenceStore().putValue(cview.TAG_SHOWLIBRARIES, b.toString());
	}

	private void updateToolTipText() {
		CLibFilter filter = cview.getLibraryFilter();
		if (filter.getShowLibraries())
			setToolTipText("Hide Referenced Libs");
		else 
			setToolTipText("Show Referenced Libs");
	}
}
