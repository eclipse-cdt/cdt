package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

/**
 * @author ThomasF
 *
 * This action is specifically designed to invoke the working set selection
 * dialog to allow the user to select/edit a working set.
 */
public class NewWorkingSetFilterAction extends Action {
	CView cview;
	Shell shell;
	
	public NewWorkingSetFilterAction(Shell shell, CView cview, String label) {
		super(label);	
		this.cview = cview;
		this.shell = shell;
	}
	
	public void run() {
		if(cview == null || shell == null) {
			return;
		}
	
		IWorkingSetManager wsmanager = cview.getViewSite().getWorkbenchWindow().getWorkbench().getWorkingSetManager();
		IWorkingSetSelectionDialog dialog;
		dialog = wsmanager.createWorkingSetSelectionDialog(shell, false);
		if(dialog.open() == Window.CANCEL) {
			return;
		}
		
		IWorkingSet [] selection = dialog.getSelection();
		if(selection.length != 0) {
			CWorkingSetFilter filter = cview.getWorkingSetFilter();
			if(filter == null) {
				return;
			}
			
			filter.setWorkingSetName(selection[0].getName());

			TreeViewer viewer= cview.getViewer();
			viewer.getControl().setRedraw(false);
			viewer.refresh();
			viewer.getControl().setRedraw(true);
		}
	}

}
