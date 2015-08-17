package org.eclipse.cdt.arduino.ui.internal.launch;

import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.ide.IDE;

public class ArduinoHyperlink implements IHyperlink {

	private final IMarker marker;

	public ArduinoHyperlink(IMarker marker) {
		this.marker = marker;
	}

	@Override
	public void linkEntered() {
	}

	@Override
	public void linkExited() {
	}

	@Override
	public void linkActivated() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IDE.openEditor(page, marker);
		} catch (PartInitException e) {
			Activator.log(e);
		}
	}

}
