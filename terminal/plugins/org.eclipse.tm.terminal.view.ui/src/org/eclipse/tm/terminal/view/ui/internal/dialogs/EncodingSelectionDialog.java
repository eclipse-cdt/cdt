/*******************************************************************************
 * Copyright (c) 2012, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.internal.dialogs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.terminal.view.ui.help.IContextHelpIds;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;
import org.eclipse.tm.terminal.view.ui.nls.Messages;
import org.eclipse.tm.terminal.view.ui.panels.AbstractExtendedConfigurationPanel;
import org.eclipse.ui.PlatformUI;

/**
 * Encoding selection dialog implementation.
 */
public class EncodingSelectionDialog extends TrayDialog {
	private String contextHelpId = null;

	// The selected encoding or null
	/* default */ String encoding = null;

	// Reference to the encodings panel
	private EncodingPanel encodingPanel = null;

	/**
	 * Encodings panel implementation
	 */
	protected class EncodingPanel extends AbstractExtendedConfigurationPanel {

		/**
		 * Constructor
		 *
		 * @param container The configuration panel container or <code>null</code>.
		 */
		public EncodingPanel(IConfigurationPanelContainer container) {
			super(container);
		}

		@Override
		public void setupPanel(Composite parent) {
			Composite panel = new Composite(parent, SWT.NONE);
			panel.setLayout(new GridLayout());
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			panel.setLayoutData(data);

			// Create the encoding selection combo
			createEncodingUI(panel, false);
			if (EncodingSelectionDialog.this.encoding != null) {
				setEncoding(EncodingSelectionDialog.this.encoding);
			}

			setControl(panel);
		}

		@Override
		protected void saveSettingsForHost(boolean add) {
		}

		@Override
		protected void fillSettingsForHost(String host) {
		}

		@Override
		protected String getHostFromSettings() {
			return null;
		}

		@Override
		public String getEncoding() {
			return super.getEncoding();
		}

		@Override
		public void setEncoding(String encoding) {
			super.setEncoding(encoding);
		}
	}

	/**
	 * Constructor.
	 *
	 * @param shell The parent shell or <code>null</code>.
	 */
	public EncodingSelectionDialog(Shell shell) {
		super(shell);

		this.contextHelpId = IContextHelpIds.ENCODING_SELECTION_DIALOG;
		setHelpAvailable(true);
	}

	@Override
	protected final Control createDialogArea(Composite parent) {
		if (contextHelpId != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, contextHelpId);
		}

		// Let the super implementation create the dialog area control
		Control control = super.createDialogArea(parent);
		// Setup the inner panel as scrollable composite
		if (control instanceof Composite) {
			ScrolledComposite sc = new ScrolledComposite((Composite) control, SWT.V_SCROLL);

			GridLayout layout = new GridLayout(1, true);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.verticalSpacing = 0;
			layout.horizontalSpacing = 0;

			sc.setLayout(layout);
			sc.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

			sc.setExpandHorizontal(true);
			sc.setExpandVertical(true);

			Composite composite = new Composite(sc, SWT.NONE);
			composite.setLayout(new GridLayout());

			// Setup the dialog area content
			createDialogAreaContent(composite);

			sc.setContent(composite);
			sc.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

			// Return the scrolled composite as new dialog area control
			control = sc;
		}

		return control;
	}

	/**
	 * Creates the dialog area content.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 */
	protected void createDialogAreaContent(Composite parent) {
		Assert.isNotNull(parent);

		setDialogTitle(Messages.EncodingSelectionDialog_title);

		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		encodingPanel = new EncodingPanel(null);
		encodingPanel.setupPanel(panel);

		applyDialogFont(panel);
	}

	/**
	 * Sets the title for this dialog.
	 *
	 * @param title The title.
	 */
	public void setDialogTitle(String title) {
		if (getShell() != null && !getShell().isDisposed()) {
			getShell().setText(title);
		}
	}

	@Override
	protected void okPressed() {
		// Save the selected encoding
		if (encodingPanel != null)
			encoding = encodingPanel.getEncoding();
		super.okPressed();
	}

	@Override
	protected void cancelPressed() {
		// Reset the encoding
		encoding = null;
		super.cancelPressed();
	}

	/**
	 * Set the encoding to default to on creating the dialog.
	 */
	public final void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Returns the selected encoding or <code>null</code>.
	 */
	public final String getEncoding() {
		return encoding;
	}
}
