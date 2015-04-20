package org.eclipse.remote.internal.console.actions;

import java.nio.charset.Charset;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.internal.console.TerminalConsoleFactory;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenCommandShellHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection != null && selection instanceof IStructuredSelection) {
			IRemoteConnection connection = (IRemoteConnection) ((IStructuredSelection) selection).iterator().next();
			TerminalConsoleFactory.openConsole(connection, Charset.defaultCharset().name());
		}
		return Status.OK_STATUS;
	}

}
