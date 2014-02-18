package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;

import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpRefactoringRunner;

/**
 * @since 5.6
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
	}

}
