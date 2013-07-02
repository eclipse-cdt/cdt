package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;

import org.eclipse.cdt.internal.ui.refactoring.createmethod.CreateMethodRefactoringWizardRunner;

/**
 * @since 5.7
 */
public class CreateMethodAction extends RefactoringAction {

	public CreateMethodAction() {
		super(Messages.CreateMethodAction_label);
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
		// TODO Auto-generated method stub

	}
	
	public void run(IMarker marker) {
		IFile file = FileBuffers.getWorkspaceFileAtLocation(marker.getResource().getLocation());
		ITranslationUnit tu = (ITranslationUnit) CCorePlugin.getDefault().getCoreModel().create(file);
		CreateMethodRefactoringWizardRunner runner = new CreateMethodRefactoringWizardRunner(
				tu, null, PlatformUI.getWorkbench().getModalDialogShellProvider(), tu.getCProject(), marker);
		runner.run();
	}

}
