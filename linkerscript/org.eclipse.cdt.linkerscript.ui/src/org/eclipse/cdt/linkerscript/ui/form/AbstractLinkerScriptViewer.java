package org.eclipse.cdt.linkerscript.ui.form;

import org.eclipse.cdt.linkerscript.linkerScript.LExpression;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

public abstract class AbstractLinkerScriptViewer implements ISelectionProvider {
	protected static final Object[] NOOBJECTS = new Object[0];
	protected static final LinkerScriptFactory factory = LinkerScriptFactory.eINSTANCE;

	private ILinkerScriptModel model;
	private ILinkerScriptModelListener modelListener = this::refreshModelListener;
	private Display display;

	public AbstractLinkerScriptViewer(Display display) {
		this.display = display;
	}

	protected Button createButton(Composite parent, String label, FormToolkit toolkit) {
		Button button;
		if (toolkit != null)
			button = toolkit.createButton(parent, label, SWT.PUSH);
		else {
			button = new Button(parent, SWT.PUSH);
			button.setText(label);
		}
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		button.setLayoutData(gd);

		// Set the default button size
		button.setFont(JFaceResources.getDialogFont());
		PixelConverter converter = new PixelConverter(button);
		int widthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		gd.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);

		return button;
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

	protected String expressionToString(LExpression expression) {
		if (expression != null) {
			ICompositeNode node = NodeModelUtils.getNode(expression);
			return NodeModelUtils.getTokenText(node);
		}
		// TODO
		return "TODO ERROR? Missing Info?";
	}

}
