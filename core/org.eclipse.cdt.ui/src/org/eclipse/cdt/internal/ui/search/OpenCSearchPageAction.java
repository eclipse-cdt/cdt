/*
 * Created on Jul 11, 2003
 */
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * @author bgheorgh
 */
public class OpenCSearchPageAction implements IWorkbenchWindowActionDelegate {

	private static final String C_SEARCH_PAGE_ID= "org.eclipse.cdt.ui.CSearchPage";  //$NON-NLS-1$

	private IWorkbenchWindow fWindow;
	
	public OpenCSearchPageAction() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	public void run(IAction action) {
		if (fWindow == null || fWindow.getActivePage() == null) {
			beep();
			return;
		}
		SearchUI.openSearchDialog(fWindow, C_SEARCH_PAGE_ID);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing since the action isn't selection dependent.
	}

	public void dispose() {
		fWindow= null;
	}

	protected void beep() {
		Shell shell= CUIPlugin.getActiveWorkbenchShell();
		if (shell != null && shell.getDisplay() != null)
			shell.getDisplay().beep();
	}	

}
