package org.eclipse.remote.internal.ui.views;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.remote.internal.ui.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class OpenTerminalHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Need to figure out how to open a terminal for this connection
		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		MessageDialog.open(MessageDialog.INFORMATION, parent,
				Messages.OpenTerminalHandler_OpenTerminalTitle, Messages.OpenTerminalHandler_OpenTerminalDesc, SWT.NONE);
		return Status.OK_STATUS;
	}

}
