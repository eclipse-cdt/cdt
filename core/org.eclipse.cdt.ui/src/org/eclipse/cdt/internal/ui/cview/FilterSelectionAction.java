package org.eclipse.cdt.internal.ui.cview;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.StringWriter;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import org.eclipse.cdt.ui.CUIPlugin;
//import org.eclipse.cdt.core.model.CElementFilters;

/**
 * The FilterAction is the class that adds the filter views to a PackagesView.
 */
class FilterSelectionAction extends SelectionProviderAction {

	
	private CView cview; 
	private Shell shell;
	
	/**
	 * Create a new filter action
	 * @param shell the shell that will be used for the list selection
	 * @param packages the PackagesExplorerPart
	 * @param label the label for the action
	 */
	public FilterSelectionAction(Shell shell, CView cview, String label) {
		super(cview.getViewer(), label);
		setToolTipText("Filter Selection Action");
		setEnabled(true);
		this.shell= shell;
		this.cview= cview;
	}

	/**
	 * Implementation of method defined on <code>IAction</code>.
	 */
	public void run() {
		CPatternFilter filter= cview.getPatternFilter();
		FiltersContentProvider contentProvider= new FiltersContentProvider(filter);
	
		ListSelectionDialog dialog =
			new ListSelectionDialog(
				shell,
				cview.getViewer(),
				contentProvider,
				new LabelProvider(),
				"Select Filter"); //$NON-NLS-1$
	
		dialog.setInitialSelections(contentProvider.getInitialSelections());
		dialog.open();
		if (dialog.getReturnCode() == Window.OK) {
			Object[] results= dialog.getResult();
			String[] selectedPatterns= new String[results.length];
			System.arraycopy(results, 0, selectedPatterns, 0, results.length);
			filter.setPatterns(selectedPatterns);
			CElementFilters.setPatterns(selectedPatterns);
			saveInPreferences(selectedPatterns);
			TreeViewer viewer= cview.getViewer();
			viewer.getControl().setRedraw(false);
			viewer.refresh();
			viewer.getControl().setRedraw(true);
		}
	}
	/**
	 * Save the supplied patterns in the preferences for the UIPlugin.
	 * They are saved in the format patern,pattern,.
	 */
	private void saveInPreferences(String[] patterns) {
		CUIPlugin plugin= CUIPlugin.getDefault();
		StringWriter writer= new StringWriter();
	
		for (int i = 0; i < patterns.length; i++) {
			writer.write(patterns[i]);
			writer.write(CPatternFilter.COMMA_SEPARATOR);
		}
	
		plugin.getPreferenceStore().setValue(
			CPatternFilter.FILTERS_TAG, writer.toString());
	}
}
