/*******************************************************************************
 *  Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.dialogfields;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class DialogField {

	private Label fLabel;
	protected String fLabelText;

	private IDialogFieldListener fDialogFieldListener;

	private boolean fEnabled;

	public DialogField() {
		fEnabled = true;
		fLabel = null;
		fLabelText = ""; //$NON-NLS-1$
	}

	/**
	 * Sets the label of the dialog field.
	 */
	public void setLabelText(String labeltext) {
		fLabelText = labeltext;
	}

	// ------ change listener

	/**
	 * Defines the listener for this dialog field.
	 */
	public final void setDialogFieldListener(IDialogFieldListener listener) {
		fDialogFieldListener = listener;
	}

	/**
	 * Programmatical invocation of a dialog field change.
	 */
	public void dialogFieldChanged() {
		if (fDialogFieldListener != null) {
			fDialogFieldListener.dialogFieldChanged(this);
		}
	}

	// ------- focus management

	/**
	 * Tries to set the focus to the dialog field.
	 * Returns <code>true</code> if the dialog field can take focus.
	 * 	To be reimplemented by dialog field implementors.
	 */
	public boolean setFocus() {
		return false;
	}

	/**
	 * Posts <code>setFocus</code> to the display event queue.
	 */
	public void postSetFocusOnDialogField(Display display) {
		if (display != null) {
			display.asyncExec(() -> setFocus());
		}
	}

	// ------- layout helpers

	/**
	 * Creates all controls of the dialog field and fills it to a composite.
	 * The composite is assumed to have <code>MGridLayout</code> as
	 * layout.
	 * The dialog field will adjust its controls' spans to the number of columns given.
	 * 	To be reimplemented by dialog field implementors.
	 */
	public Control[] doFillIntoGrid(Composite parent, int nColumns) {
		assertEnoughColumns(nColumns);

		Label label = getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(nColumns));

		return new Control[] { label };
	}

	/**
	 * Returns the number of columns of the dialog field.
	 * 	To be reimplemented by dialog field implementors.
	 */
	public int getNumberOfControls() {
		return 1;
	}

	protected static GridData gridDataForLabel(int span) {
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = span;
		return gd;
	}

	// ------- ui creation

	/**
	 * Creates or returns the created label widget.
	 * @param parent The parent composite or <code>null</code> if the widget has
	 * already been created.
	 */
	public Label getLabelControl(Composite parent) {
		if (fLabel == null) {
			assertCompositeNotNull(parent);

			fLabel = new Label(parent, SWT.LEFT);
			fLabel.setFont(parent.getFont());
			fLabel.setEnabled(fEnabled);
			if (fLabelText != null && !fLabelText.isEmpty()) {
				fLabel.setText(fLabelText);
			} else {
				// XXX: to avoid a 16 pixel wide empty label - revisit
				fLabel.setText("."); //$NON-NLS-1$
				fLabel.setVisible(false);
			}
		}
		return fLabel;
	}

	/**
	 * Creates a spacer control.
	 *
	 * @param parent The parent composite
	 */
	public static Control createEmptySpace(Composite parent) {
		return createEmptySpace(parent, 1);
	}

	/**
	 * Creates a spacer control with the given span.
	 * The composite is assumed to have {@link GridLayout} as layout.
	 *
	 * @param parent The parent composite
	 */
	public static Control createEmptySpace(Composite parent, int span) {
		Label label = new Label(parent, SWT.LEFT);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.grabExcessHorizontalSpace = false;
		gd.horizontalSpan = span;
		gd.horizontalIndent = 0;
		gd.widthHint = 0;
		gd.heightHint = 0;
		label.setLayoutData(gd);
		return label;
	}

	/**
	 * Tests is the control is not <code>null</code> and not disposed.
	*/
	protected final boolean isOkToUse(Control control) {
		return (control != null) && !(control.isDisposed());
	}

	// --------- enable / disable management

	/**
	 * Sets the enable state of the dialog field.
	 */
	public final void setEnabled(boolean enabled) {
		if (enabled != fEnabled) {
			fEnabled = enabled;
			updateEnableState();
		}
	}

	/**
	 * Called when the enable state changed.
	 * To be extended by dialog field implementors.
	 */
	protected void updateEnableState() {
		if (fLabel != null) {
			fLabel.setEnabled(fEnabled);
		}
	}

	/**
	 * Gets the enable state of the dialog field.
	 */
	public final boolean isEnabled() {
		return fEnabled;
	}

	protected final void assertCompositeNotNull(Composite comp) {
		Assert.isNotNull(comp, "uncreated control requested with composite null"); //$NON-NLS-1$
	}

	protected final void assertEnoughColumns(int nColumns) {
		Assert.isTrue(nColumns >= getNumberOfControls(), "given number of columns is too small"); //$NON-NLS-1$
	}

}
