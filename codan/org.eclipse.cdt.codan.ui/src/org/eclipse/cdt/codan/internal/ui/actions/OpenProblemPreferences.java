package org.eclipse.cdt.codan.internal.ui.actions;

import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.internal.core.model.CodanProblem;
import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.cdt.codan.internal.ui.dialogs.CustomizeProblemDialog;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class OpenProblemPreferences implements IObjectActionDelegate {
	private ISelection selection;
	private IWorkbenchPart targetPart;

	public OpenProblemPreferences() {
	}

	public void run(IAction action) {
		if (selection instanceof IStructuredSelection) {
			Object firstElement = ((IStructuredSelection) selection)
					.getFirstElement(); // TODO support multiple
			if (firstElement instanceof IMarker) {
				IMarker marker = (IMarker) firstElement;
				String id = CodanProblemMarker.getProblemId(marker);
				if (id == null)
					return;
				IResource resource = marker.getResource();
				IProblemProfile profile = CodanProblemMarker
						.getProfile(resource);
				CodanProblem problem = ((CodanProblem) profile.findProblem(id));
				new CustomizeProblemDialog(targetPart.getSite().getShell(),
						problem, resource).open();
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}
}
