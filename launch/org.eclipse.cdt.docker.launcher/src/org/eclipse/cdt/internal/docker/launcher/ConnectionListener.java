package org.eclipse.cdt.internal.docker.launcher;

import org.eclipse.cdt.docker.launcher.DockerLaunchUIPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

public class ConnectionListener implements ISelectionListener {

	private static ConnectionListener instance;

	private IDockerConnection currentConnection;

	private ConnectionListener() {
	}

	public static ConnectionListener getInstance() {
		if (instance == null)
			instance = new ConnectionListener();
		return instance;
	}

	public void init() {
		DockerLaunchUIPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getSelectionService()
				.addSelectionListener(
						"org.eclipse.linuxtools.docker.ui.dockerExplorerView", //$NON-NLS-1$
						this);
	}

	public IDockerConnection getCurrentConnection() {
		return currentConnection;
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		final ITreeSelection treeSelection = (ITreeSelection) selection;
		if (treeSelection.isEmpty()) {
			return;
		}
		final Object firstSegment = treeSelection.getPaths()[0]
				.getFirstSegment();
		if (firstSegment instanceof IDockerConnection) {
			currentConnection = (IDockerConnection) firstSegment;
		}
	}

}
