package org.eclipse.cdt.ui.newui;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.core.settings.model.ICProjectDescription;

/**
 * Represents class which is able to display
 * "New configuration" dialog instead of standard one
 * and, if user pressed OK, create new configuration
 * on a basis of its internal data
 * 
 * used by extension point:
 * "org.eclipse.cdt.ui.newCfgDialog" 
 */
public interface INewCfgDialog {
	// Project to work with (set before open() !)
	void setProject(ICProjectDescription prj);
	// Title of dialog box (set before open() !)
	void setTitle(String title);
	// Shell to create dialog (set before open() !)
	void setShell(Shell shell);
	// Opens dialog and (after user presses OK)
	// creates new configuration. 
	// Returns Windows.OK on success. 
	int open();
}
