package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IWorkingCopy;

import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpRefactoringRunner;

/**
 * @since 5.8
 */
public class PullUpMemberAction extends RefactoringAction {

	public PullUpMemberAction() {
		super(Messages.PullUpMember_label);
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection s) {
		if (wc.getResource() != null) {
			new PullUpRefactoringRunner(wc.getTranslationUnit(), s, 
					shellProvider, wc.getCProject()).run();
		}
	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
		if (elem instanceof ISourceReference) {
			new PullUpRefactoringRunner(elem, null, shellProvider, elem.getCProject()).run();
		}
	}

    @Override
	public void updateSelection(ICElement elem) {
		super.updateSelection(elem);
    	if (!(elem instanceof IMethodDeclaration) || !(elem instanceof ISourceReference) ||
    			((ISourceReference) elem).getTranslationUnit().getResource() == null) {
    		this.setEnabled(false);
    	}
	}
}
