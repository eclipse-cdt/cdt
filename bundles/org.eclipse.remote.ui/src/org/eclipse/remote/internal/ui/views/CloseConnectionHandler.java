package org.eclipse.remote.internal.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionControlService;
import org.eclipse.remote.internal.ui.RemoteUIPlugin;
import org.eclipse.ui.handlers.HandlerUtil;

public class CloseConnectionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection != null && selection instanceof IStructuredSelection) {
			// Get the manageable connections from the selection
			final List<IRemoteConnection> connections = new ArrayList<IRemoteConnection>();
			@SuppressWarnings("unchecked")
			Iterator<Object> i = ((IStructuredSelection) selection).iterator();
			while (i.hasNext()) {
				Object obj = i.next();
				if (obj instanceof IRemoteConnection) {
					IRemoteConnection connection = (IRemoteConnection) obj;
					connections.add(connection);
				}
			}

			new Job(Messages.CloseConnectionHandler_0) {
				protected IStatus run(IProgressMonitor monitor) {
					List<IStatus> status = new ArrayList<>();
					for (IRemoteConnection connection : connections) {
						IRemoteConnectionControlService controlService = connection.getService(IRemoteConnectionControlService.class);
						if (controlService != null) {
							controlService.close();
						}
					}

					if (status.isEmpty()) {
						return Status.OK_STATUS;
					} else {
						return new MultiStatus(RemoteUIPlugin.PLUGIN_ID, 1, status.toArray(new IStatus[status.size()]), Messages.CloseConnectionHandler_1, null);
					}
				}
			}.schedule();
		}
		return Status.OK_STATUS;
	}

}
