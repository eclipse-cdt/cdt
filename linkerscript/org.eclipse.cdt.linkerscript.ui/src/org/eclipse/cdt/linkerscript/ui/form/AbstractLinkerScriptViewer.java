package org.eclipse.cdt.linkerscript.ui.form;

import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractLinkerScriptViewer implements ISelectionProvider {
	protected static final Object[] NOOBJECTS = new Object[0];
	protected static final LinkerScriptFactory factory = LinkerScriptFactory.eINSTANCE;

	private ILinkerScriptModel model;
	private ILinkerScriptModelListener modelListener = this::refreshModelListener;
	private Display display;

	public AbstractLinkerScriptViewer(Display display) {
		this.display = display;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		getViewer().addSelectionChangedListener(listener);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		getViewer().removeSelectionChangedListener(listener);
	}

	@Override
	public ISelection getSelection() {
		return getViewer().getSelection();

	}

	@Override
	public void setSelection(ISelection selection) {
		getViewer().setSelection(selection);
	}

	/**
	 * Get the control representing this entire viewer, including its buttons,
	 * etc.
	 */
	public abstract Control getControl();

	private void refreshModelListener() {
		if (display.isDisposed()) {
			return;
		}

		display.asyncExec(() -> refresh());
	}

	protected void refresh() {
		if (getViewer().getControl().isDisposed()) {
			return;
		}

		getViewer().refresh();

	}

	protected abstract Viewer getViewer();

	public ILinkerScriptModel getModel() {
		return model;
	}

	public void setInput(ILinkerScriptModel model) {
		if (this.model != null) {
			this.model.removeModelListener(modelListener);
		}

		this.model = model;
		getViewer().setInput(model);
		refresh();

		model.addModelListener(modelListener);
	}

}
