package org.eclipse.cdt.internal.ui.cview;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionProviderAction;

import org.eclipse.cdt.ui.CUIPlugin;

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
		CUIPlugin plugin = CUIPlugin.getDefault();
		Boolean b = new Boolean (cview.getLibraryFilter().getShowLibraries());
	
		plugin.getPreferenceStore().putValue(CView.TAG_SHOWLIBRARIES, b.toString());
	}

	private void updateToolTipText() {
		CLibFilter filter = cview.getLibraryFilter();
		if (filter.getShowLibraries())
			setToolTipText(CViewMessages.getString("ShowLibrariesAction.hideReferenced.tooltip")); //$NON-NLS-1$
		else 
			setToolTipText(CViewMessages.getString("ShowLibrariesAction.showReferenced.tooltip")); //$NON-NLS-1$
	}
}
