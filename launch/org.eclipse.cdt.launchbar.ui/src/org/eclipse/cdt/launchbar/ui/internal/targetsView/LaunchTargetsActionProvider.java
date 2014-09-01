package org.eclipse.cdt.launchbar.ui.internal.targetsView;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.navigator.CommonActionProvider;

public class LaunchTargetsActionProvider extends CommonActionProvider {

	@Override
	public void fillContextMenu(IMenuManager menu) {
		menu.add(new PropertyDialogAction(new SameShellProvider(getActionSite().getViewSite().getShell()),
				getActionSite().getStructuredViewer()));
	}

}
