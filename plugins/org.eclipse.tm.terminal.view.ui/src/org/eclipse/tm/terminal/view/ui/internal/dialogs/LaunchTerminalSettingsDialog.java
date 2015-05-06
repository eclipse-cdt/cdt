/*******************************************************************************
 * Copyright (c) 2011 - 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Max Weninger (Wind River) - [361352] [TERMINALS][SSH] Add SSH terminal support
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.activator.UIPlugin;
import org.eclipse.tm.terminal.view.ui.controls.ConfigurationPanelControl;
import org.eclipse.tm.terminal.view.ui.help.IContextHelpIds;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanel;
import org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate;
import org.eclipse.tm.terminal.view.ui.interfaces.tracing.ITraceIds;
import org.eclipse.tm.terminal.view.ui.launcher.LauncherDelegateManager;
import org.eclipse.tm.terminal.view.ui.nls.Messages;
import org.eclipse.ui.PlatformUI;

/**
 * Launch terminal settings dialog implementation.
 */
public class LaunchTerminalSettingsDialog extends TrayDialog {
	private String contextHelpId = null;

	// The parent selection
	private ISelection selection = null;

	// The sub controls
	/* default */ Combo terminals;
	/* default */ SettingsPanelControl settings;

	// Map the label added to the combo box to the corresponding launcher delegate.
	/* default */ final Map<String, ILauncherDelegate> label2delegate = new HashMap<String, ILauncherDelegate>();

	// The data object containing the currently selected settings
	private Map<String, Object> data = null;

	// The dialog settings storage
	private IDialogSettings dialogSettings;

	// In case of a single available terminal launcher delegate, the label of that delegate
	private String singleDelegateLabel = null;

	/**
	 * The control managing the terminal setting panels.
	 */
	protected class SettingsPanelControl extends ConfigurationPanelControl {

		/**
		 * Constructor.
		 */
        public SettingsPanelControl() {
	        setPanelIsGroup(true);
        }

        /* (non-Javadoc)
         * @see org.eclipse.tm.terminal.view.ui.controls.ConfigurationPanelControl#getGroupLabel()
         */
        @Override
        public String getGroupLabel() {
            return Messages.LaunchTerminalSettingsDialog_group_label;
        }

        /* (non-Javadoc)
         * @see org.eclipse.tm.terminal.view.ui.controls.ConfigurationPanelControl#showConfigurationPanel(java.lang.String)
         */
        @Override
        public void showConfigurationPanel(String key) {
        	// Check if we have to create the panel first
    		IConfigurationPanel configPanel = getConfigurationPanel(key);
    		if (isEmptyConfigurationPanel(configPanel)) {
           		// Get the corresponding delegate
           		ILauncherDelegate delegate = label2delegate.get(key);
           		Assert.isNotNull(delegate);
           		// Create the wizard configuration panel instance
           		configPanel = delegate.getPanel(this);
           		if (configPanel != null) {
           			// Add it to the settings panel control
               		settings.addConfigurationPanel(key, configPanel);
                	// Push the selection to the configuration panel
                	configPanel.setSelection(getSelection());
                	// Create the panel controls
                	configPanel.setupPanel(getPanel());
                	// Restore widget values
                	IDialogSettings dialogSettings = LaunchTerminalSettingsDialog.this.settings.getDialogSettings(LaunchTerminalSettingsDialog.this.getDialogSettings());
                	IDialogSettings configPanelSettings = dialogSettings != null ? dialogSettings.getSection(key) : null;
                	if (configPanelSettings != null) configPanel.doRestoreWidgetValues(configPanelSettings, null);
           		}
    		}

            super.showConfigurationPanel(key);
        }

        /* (non-Javadoc)
         * @see org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer#validate()
         */
        @Override
        public void validate() {
        	LaunchTerminalSettingsDialog.this.validate();
        }
	}

	/**
     * Constructor.
     *
	 * @param shell The parent shell or <code>null</code>.
     */
    public LaunchTerminalSettingsDialog(Shell shell) {
	    this(shell, 0);
    }

    private long start = 0;

	/**
     * Constructor.
     *
	 * @param shell The parent shell or <code>null</code>.
     */
    public LaunchTerminalSettingsDialog(Shell shell, long start) {
	    super(shell);
	    this.start = start;

	    initializeDialogSettings();

		this.contextHelpId = IContextHelpIds.LAUNCH_TERMINAL_SETTINGS_DIALOG;
		setHelpAvailable(true);
    }

    /**
     * Sets the parent selection.
     *
     * @param selection The parent selection or <code>null</code>.
     */
    public void setSelection(ISelection selection) {
    	this.selection = selection;
    }

    /**
     * Returns the parent selection.
     *
     * @return The parent selection or <code>null</code>.
     */
    public ISelection getSelection() {
    	return selection;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#close()
     */
	@Override
	public boolean close() {
		dispose();
		return super.close();
	}

    /**
     * Dispose the dialog resources.
     */
    protected void dispose() {
    	if (settings != null) { settings.dispose(); settings = null; }
    	dialogSettings = null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    @Override
    protected boolean isResizable() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Control composite = super.createContents(parent);

        // Validate the dialog after having created all the content
        validate();

        return composite;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected final Control createDialogArea(Composite parent) {
		if (contextHelpId != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, contextHelpId);
		}

		// Let the super implementation create the dialog area control
		Control control = super.createDialogArea(parent);
		// Setup the inner panel as scrollable composite
		if (control instanceof Composite) {
			ScrolledComposite sc = new ScrolledComposite((Composite)control, SWT.V_SCROLL);

			GridLayout layout = new GridLayout(1, true);
			layout.marginHeight = 0; layout.marginWidth = 0;
			layout.verticalSpacing = 0; layout.horizontalSpacing = 0;

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
	 * Sets the title for this dialog.
	 *
	 * @param title The title.
	 */
	public void setDialogTitle(String title) {
		if (getShell() != null && !getShell().isDisposed()) {
			getShell().setText(title);
		}
	}

	/**
	 * Creates the dialog area content.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 */
    protected void createDialogAreaContent(Composite parent) {
    	Assert.isNotNull(parent);

        if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER)) {
			UIPlugin.getTraceHandler().trace("Creating dialog area after " + (System.currentTimeMillis() - start) + " ms.", //$NON-NLS-1$ //$NON-NLS-2$
												ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER, LaunchTerminalSettingsDialog.this);
		}

    	setDialogTitle(Messages.LaunchTerminalSettingsDialog_title);

    	final List<String> items = getTerminals();

        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0; layout.marginWidth = 0;
        panel.setLayout(layout);
        panel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

        if (items.size() != 1) {
        	Label label = new Label(panel, SWT.HORIZONTAL);
        	label.setText(Messages.LaunchTerminalSettingsDialog_combo_label);

        	terminals = new Combo(panel, SWT.READ_ONLY);
        	terminals.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        	terminals.addSelectionListener(new SelectionAdapter() {
        		@Override
        		public void widgetSelected(SelectionEvent e) {
        			// Get the old panel
        			IConfigurationPanel oldPanel = settings.getActiveConfigurationPanel();
        			// Extract the current settings in an special properties container
        			Map<String, Object> data = new HashMap<String, Object>();
        			if (oldPanel != null) oldPanel.extractData(data);
        			// Clean out settings which are never passed between the panels
        			data.remove(ITerminalsConnectorConstants.PROP_IP_PORT);
        			data.remove(ITerminalsConnectorConstants.PROP_TIMEOUT);
        			data.remove(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID);
        			data.remove(ITerminalsConnectorConstants.PROP_ENCODING);
        			// Switch to the new panel
        			settings.showConfigurationPanel(terminals.getText());
        			// Get the new panel
        			IConfigurationPanel newPanel = settings.getActiveConfigurationPanel();
        			// Re-setup the relevant data
        			if (newPanel != null) newPanel.setupData(data);

        			// resize the dialog if needed to show the complete panel
        			getShell().pack();
        			// validate the settings dialog
        			validate();
        		}
        	});

            // fill the combo with content
            fillCombo(terminals, items);
        } else {
        	Assert.isTrue(items.size() == 1);
        	singleDelegateLabel = items.get(0);
        }

        // Create the settings panel control
        settings = new SettingsPanelControl();

		// Create, initialize and add the first visible panel. All
        // other panels are created on demand only.
        String terminalLabel = terminals != null ? terminals.getItem(0) : singleDelegateLabel;
        if (terminalLabel != null) {
       		// Get the corresponding delegate
       		ILauncherDelegate delegate = label2delegate.get(terminalLabel);
       		Assert.isNotNull(delegate);
       		// Create the wizard configuration panel instance
       		IConfigurationPanel configPanel = delegate.getPanel(settings);
       		if (configPanel != null) {
       			// Add it to the settings panel control
           		settings.addConfigurationPanel(terminalLabel, configPanel);
            	// Push the selection to the configuration panel
            	configPanel.setSelection(getSelection());
       		}
        }

		// Setup the panel control
		settings.setupPanel(panel, terminals != null ? terminals.getItems() : new String[] { singleDelegateLabel });
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.horizontalSpan = 2;
		settings.getPanel().setLayoutData(layoutData);

		// Preselect the first terminal launcher
		if (terminals != null) {
			terminals.select(0);
			settings.showConfigurationPanel(terminals.getText());

			terminals.setEnabled(terminals.getItemCount() > 1);
		} else {
			settings.showConfigurationPanel(singleDelegateLabel);
		}

		restoreWidgetValues();

        applyDialogFont(panel);

		if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER)) {
			UIPlugin.getTraceHandler().trace("Created dialog area after " + (System.currentTimeMillis() - start) + " ms.", //$NON-NLS-1$ //$NON-NLS-2$
												ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER, LaunchTerminalSettingsDialog.this);
		}
    }

    /**
     * Fill the given combo with the given list of terminal launcher delegate labels.
     *
     * @param combo The combo. Must not be <code>null</code>.
     * @param items The list of terminal launcher delegates. Must not be <code>null</code>.
     */
    protected void fillCombo(Combo combo, List<String> items) {
    	Assert.isNotNull(combo);
    	Assert.isNotNull(items);

		if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER)) {
			UIPlugin.getTraceHandler().trace("Filling combo after " + (System.currentTimeMillis() - start) + " ms.", //$NON-NLS-1$ //$NON-NLS-2$
												ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER, LaunchTerminalSettingsDialog.this);
		}

    	Collections.sort(items);
    	combo.setItems(items.toArray(new String[items.size()]));
    }

    /**
     * Returns the list of terminal launcher delegate labels. The method queries the
     * terminal launcher delegates and initialize the <code>label2delegate</code> map.
     *
     * @return The list of terminal launcher delegate labels or an empty list.
     */
    protected List<String> getTerminals() {
    	List<String> items = new ArrayList<String>();

    	if(selection==null || selection.isEmpty()){
    		if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER)) {
    			UIPlugin.getTraceHandler().trace("Getting launcher delegates after " + (System.currentTimeMillis() - start) + " ms.", //$NON-NLS-1$ //$NON-NLS-2$
    												ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER, LaunchTerminalSettingsDialog.this);
    		}

			ILauncherDelegate[] delegates = LauncherDelegateManager.getInstance().getLauncherDelegates(false);

    		if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER)) {
    			UIPlugin.getTraceHandler().trace("Got launcher delegates after " + (System.currentTimeMillis() - start) + " ms.", //$NON-NLS-1$ //$NON-NLS-2$
    												ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER, LaunchTerminalSettingsDialog.this);
    		}

    		for (ILauncherDelegate delegate : delegates) {
    			if (delegate.isHidden() || isFiltered(selection, delegate)) continue;
    			String label = delegate.getLabel();
    			if (label == null || "".equals(label.trim())) label = delegate.getId(); //$NON-NLS-1$
    			label2delegate.put(label, delegate);
    			items.add(label);
    		}
    	} else {
    		if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER)) {
    			UIPlugin.getTraceHandler().trace("Getting applicable launcher delegates after " + (System.currentTimeMillis() - start) + " ms.", //$NON-NLS-1$ //$NON-NLS-2$
    												ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER, LaunchTerminalSettingsDialog.this);
    		}

    		ILauncherDelegate[] delegates = LauncherDelegateManager.getInstance().getApplicableLauncherDelegates(selection);

    		if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER)) {
    			UIPlugin.getTraceHandler().trace("Got applicable launcher delegates after " + (System.currentTimeMillis() - start) + " ms.", //$NON-NLS-1$ //$NON-NLS-2$
    												ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER, LaunchTerminalSettingsDialog.this);
    		}

    		for (ILauncherDelegate delegate : delegates) {
    			if (delegate.isHidden() || isFiltered(selection, delegate)) continue;
    			String label = delegate.getLabel();
    			if (label == null || "".equals(label.trim())) label = delegate.getId(); //$NON-NLS-1$
    			label2delegate.put(label, delegate);
    			items.add(label);
    		}
    	}

    	return items;
    }

    /**
     * Hook to allow additional filtering of the applicable launcher delegates.
     * <p>
     * <b>Note:</b> The default implementation always returns <code>false</code>.
     *
     * @param selection The selection or <code>null</code>.
     * @param delegate The launcher delegate. Must not be <code>null</code>.
     *
     * @return <code>True</code> if the launcher delegate is filtered based on the given selection, <code>false</code> otherwise.
     */
    protected boolean isFiltered(ISelection selection, ILauncherDelegate delegate) {
    	return false;
    }

    /**
     * Validate the dialog.
     */
    public void validate() {
    	IConfigurationPanel panel = this.settings.getActiveConfigurationPanel();
    	Button okButton = getButton(IDialogConstants.OK_ID);
    	if (okButton != null) okButton.setEnabled(panel.isValid());
    }

    /**
     * Set the given message and message type.
     *
     * @param message The message or <code>null</code>.
     * @param messageType The message type or <code>IMessageProvider.NONE</code>.
     */
    public void setMessage(String message, int messageType) {
    	if (settings != null) {
    		settings.setMessage(message, messageType);
    	}
    }

    /**
     * Save the dialog's widget values.
     */
    protected void saveWidgetValues() {
    	IDialogSettings settings = getDialogSettings();
    	if (settings != null && terminals != null) {
    		settings.put("terminalLabel", terminals.getText()); //$NON-NLS-1$
    		this.settings.saveWidgetValues(settings, null);
    	}
    }

    /**
     * Restore the dialog's widget values.
     */
    protected void restoreWidgetValues() {
    	IDialogSettings settings = getDialogSettings();
    	if (settings != null) {
    		String terminalLabel = settings.get("terminalLabel"); //$NON-NLS-1$
    		int index = terminalLabel != null && terminals != null ? Arrays.asList(terminals.getItems()).indexOf(terminalLabel) : -1;
    		if (index != -1) {
    			terminals.select(index);
    			this.settings.showConfigurationPanel(terminals.getText());
    		}

    		this.settings.restoreWidgetValues(settings, null);
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
    	IConfigurationPanel panel = this.settings.getActiveConfigurationPanel();
    	Assert.isNotNull(panel);

    	if (!panel.isValid()) {
			MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			mb.setText(Messages.LaunchTerminalSettingsDialog_error_title);
			mb.setMessage(NLS.bind(Messages.LaunchTerminalSettingsDialog_error_invalidSettings, panel.getMessage() != null ? panel.getMessage() : Messages.LaunchTerminalSettingsDialog_error_unknownReason));
			mb.open();
			return;
    	}
    	data = new HashMap<String, Object>();

    	// Store the id of the selected delegate
    	String terminalLabel = terminals != null ? terminals.getText() : singleDelegateLabel;
    	String delegateId = terminalLabel != null ? label2delegate.get(terminalLabel).getId() : null;
    	if (delegateId != null) data.put(ITerminalsConnectorConstants.PROP_DELEGATE_ID, delegateId);
    	// Store the selection
    	data.put(ITerminalsConnectorConstants.PROP_SELECTION, selection);

    	// Store the delegate specific settings
   		panel.extractData(data);

   		// Save the current widget values
		saveWidgetValues();

		super.okPressed();
    }

    /**
     * Returns the configured terminal launcher settings.
     * <p>
     * The settings are extracted from the UI widgets once
     * OK got pressed.
     *
     * @return The configured terminal launcher settings or <code>null</code>.
     */
    public Map<String, Object> getSettings() {
    	return data;
    }

	/**
	 * Initialize the dialog settings storage.
	 */
	protected void initializeDialogSettings() {
		IDialogSettings settings = UIPlugin.getDefault().getDialogSettings();
		Assert.isNotNull(settings);
		IDialogSettings section = settings.getSection(getClass().getSimpleName());
		if (section == null) {
			section = settings.addNewSection(getClass().getSimpleName());
		}
		setDialogSettings(section);
	}

	/**
	 * Returns the associated dialog settings storage.
	 *
	 * @return The dialog settings storage.
	 */
	public IDialogSettings getDialogSettings() {
		// The dialog settings may not been initialized here. Initialize first in this case
		// to be sure that we do have always the correct dialog settings.
		if (dialogSettings == null) {
			initializeDialogSettings();
		}
		return dialogSettings;
	}

	/**
	 * Sets the associated dialog settings storage.
	 *
	 * @return The dialog settings storage.
	 */
	public void setDialogSettings(IDialogSettings dialogSettings) {
		this.dialogSettings = dialogSettings;
	}
}
