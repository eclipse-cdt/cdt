package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.IEditorPart;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;

import org.eclipse.cdt.internal.ui.refactoring.overridemethods.OverrideMethodsRefactoringRunner;

/**
 * @since 6.2
 */
public class OverrideMethodsAction extends RefactoringAction implements ISelectionChangedListener {

	public OverrideMethodsAction() {
		super(Messages.OverrideMethods_label);
	}
	
	public OverrideMethodsAction(IEditorPart editor) {
		this();
		setEditor(editor);
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection selection) {
		if (wc.getResource() != null) {
			new OverrideMethodsRefactoringRunner(wc, selection, shellProvider, wc.getCProject()).run();
		}
	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
	}

    @Override
	public void updateSelection(ICElement elem) {
    	super.updateSelection(elem);
    	setEnabled(false);
    }

	public void update(ISelection selection) {
		
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		
	}
}
