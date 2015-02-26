/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.controls;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel;
import org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanelContainer;
import org.eclipse.tcf.te.ui.terminals.panels.AbstractConfigurationPanel;

/**
 * Base control to deal with wizard or property page controls
 * which should share the same UI space.
 */
public class ConfigurationPanelControl implements IConfigurationPanelContainer, IMessageProvider {
	private final Map<String, IConfigurationPanel> configurationPanels = new Hashtable<String, IConfigurationPanel>();

	private String message = null;
	private int messageType = IMessageProvider.NONE;

	private boolean isGroup;

	private Composite panel;
	private StackLayout panelLayout;

	private String activeConfigurationPanelKey = null;
	private IConfigurationPanel activeConfigurationPanel = null;

	private final AbstractConfigurationPanel EMPTY_PANEL;

	/**
	 * An empty configuration panel implementation.
	 */
	private static final class EmptySettingsPanel extends AbstractConfigurationPanel {

		/**
		 * Constructor.
		 *
		 * @param container The configuration panel container or <code>null</code>.
	     */
	    public EmptySettingsPanel(IConfigurationPanelContainer container) {
		    super(container);
	    }

	    /* (non-Javadoc)
	     * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel#setupPanel(org.eclipse.swt.widgets.Composite)
	     */
        @Override
	    public void setupPanel(Composite parent) {
	    	Composite panel = new Composite(parent, SWT.NONE);
	    	panel.setLayout(new GridLayout());
	    	panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

	    	panel.setBackground(parent.getBackground());

	    	setControl(panel);
	    }

        /* (non-Javadoc)
         * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractConfigurationPanel#isValid()
         */
	    @Override
	    public boolean isValid() {
	        return false;
	    }
	}

	/**
	 * Cleanup all resources the control might have been created.
	 */
	public void dispose() {
		EMPTY_PANEL.dispose();
	}

	/**
	 * Constructor.
	 */
	public ConfigurationPanelControl() {
		EMPTY_PANEL = new EmptySettingsPanel(this);
		clear();
		setPanelIsGroup(false);
	}

	/**
	 * Sets if or if not the controls panel is a <code>Group</code>.
	 *
	 * @param isGroup <code>True</code> if the controls panel is a group, <code>false</code> otherwise.
	 */
	public void setPanelIsGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	/**
	 * Returns if or if not the controls panel is a <code>Group</code>.
	 *
	 * @return <code>True</code> if the controls panel is a group, <code>false</code> otherwise.
	 */
	public boolean isPanelIsGroup() {
		return isGroup;
	}

	/**
	 * Returns the controls panel.
	 *
	 * @return The controls panel or <code>null</code>.
	 */
	public Composite getPanel() {
		return panel;
	}

	/**
	 * Returns the label text to set for the group (if the panel is a group).
	 *
	 * @return The label text to apply or <code>null</code>.
	 */
	public String getGroupLabel() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanelContainer#validate()
	 */
	@Override
	public void validate() {
	}

	/**
	 * To be called from the embedding control to setup the controls UI elements.
	 *
	 * @param parent The parent control. Must not be <code>null</code>!
	 */
	public void setupPanel(Composite parent, String[] configurationPanelKeys) {
		Assert.isNotNull(parent);

		if (isPanelIsGroup()) {
			panel = new Group(parent, SWT.NONE);
			if (getGroupLabel() != null) ((Group)panel).setText(getGroupLabel());
		} else {
			panel = new Composite(parent, SWT.NONE);
		}
		Assert.isNotNull(panel);
		panel.setFont(parent.getFont());
		panel.setBackground(parent.getBackground());

		panelLayout = new StackLayout();
		panel.setLayout(panelLayout);

		setupConfigurationPanels(panel, configurationPanelKeys);
		EMPTY_PANEL.setupPanel(panel);
	}

	/**
	 * Removes all configuration panels.
	 */
	public void clear() {
		configurationPanels.clear();
	}

	/**
	 * Returns a unsorted list of all registered configuration panel id's.
	 *
	 * @return A list of registered configuration panel id's.
	 */
	public String[] getConfigurationPanelIds() {
		return configurationPanels.keySet().toArray(new String[configurationPanels.keySet().size()]);
	}

	/**
	 * Returns the configuration panel instance registered for the given configuration panel key.
	 *
	 * @param key The key to get the configuration panel for. Must not be <code>null</code>!
	 * @return The configuration panel instance or an empty configuration panel if the key is unknown.
	 */
	public IConfigurationPanel getConfigurationPanel(String key) {
		IConfigurationPanel panel = key != null ? configurationPanels.get(key) : null;
		return panel != null ? panel : EMPTY_PANEL;
	}

	/**
	 * Returns if or if not the given configuration panel is equal to the
	 * empty configuration panel.
	 *
	 * @param panel The configuration panel or <code>null</code>.
	 * @return <code>True</code> if the configuration panel is equal to the empty configuration panel.
	 */
	public final boolean isEmptyConfigurationPanel(IConfigurationPanel panel) {
		return EMPTY_PANEL == panel;
	}

	/**
	 * Adds the given configuration panel under the given configuration panel key to the
	 * list of known panels. If the given configuration panel is <code>null</code>, any
	 * configuration panel stored under the given key is removed from the list of known panels.
	 *
	 * @param key The key to get the configuration panel for. Must not be <code>null</code>!
	 * @param panel The configuration panel instance or <code>null</code>.
	 */
	public void addConfigurationPanel(String key, IConfigurationPanel panel) {
		if (key == null) return;
		if (panel != null) {
			configurationPanels.put(key, panel);
		} else {
			configurationPanels.remove(key);
		}
	}

	/**
	 * Setup the configuration panels for being presented to the user. This method is called by the
	 * controls <code>doSetupPanel(...)</code> and initialize all possible configuration panels to show.
	 * The default implementation iterates over the given list of configuration panel keys and calls
	 * <code>setupPanel(...)</code> for each of them.
	 *
	 * @param parent The parent composite to use for the configuration panels. Must not be <code>null</code>!
	 * @param configurationPanelKeys The list of configuration panels to initialize. Might be <code>null</code> or empty!
	 */
	public void setupConfigurationPanels(Composite parent, String[] configurationPanelKeys) {
		Assert.isNotNull(parent);

		if (configurationPanelKeys != null) {
			for (int i = 0; i < configurationPanelKeys.length; i++) {
				IConfigurationPanel configPanel = getConfigurationPanel(configurationPanelKeys[i]);
				Assert.isNotNull(configPanel);
				configPanel.setupPanel(parent);
			}
		}
	}

	/**
	 * Make the wizard configuration panel registered under the given configuration panel key the
	 * most top configuration panel. If no configuration panel is registered under the given key,
	 * nothing will happen.
	 *
	 * @param key The key to get the wizard configuration panel for. Must not be <code>null</code>!
	 */
	public void showConfigurationPanel(String key) {
		String activeKey = getActiveConfigurationPanelKey();
		if (key != null && key.equals(activeKey) && activeConfigurationPanel != null) {
			return;
		}
		IConfigurationPanel configPanel = getActiveConfigurationPanel();
		Map<String, Object> data = new HashMap<String, Object>();
		if (configPanel != null) configPanel.extractData(data);
		configPanel = getConfigurationPanel(key);
		Assert.isNotNull(configPanel);
		if (configPanel.getControl() != null) {
			activeConfigurationPanel = configPanel;
			activeConfigurationPanelKey = key;
			panelLayout.topControl = configPanel.getControl();
			panel.layout();
			if (!data.isEmpty()) configPanel.updateData(data);
			configPanel.activate();
		}
		else {
			activeConfigurationPanelKey = key;
		}
	}

	/**
	 * Returns the currently active configuration panel.
	 *
	 * @return The active configuration panel or <code>null</code>.
	 */
	public IConfigurationPanel getActiveConfigurationPanel() {
		return activeConfigurationPanel;
	}

	/**
	 * Returns the currently active configuration panel key.
	 *
	 * @return The active configuration panel key or <code>null</code>.
	 */
	public String getActiveConfigurationPanelKey() {
		return activeConfigurationPanelKey;
	}

	/**
	 * Returns the dialog settings to use to save and restore control specific
	 * widget values.
	 *
	 * @param settings The parent dialog settings. Must not be <code>null</code>.
	 * @return The dialog settings to use.
	 */
	public final IDialogSettings getDialogSettings(IDialogSettings settings) {
		Assert.isNotNull(settings);

		// Store the settings of the control within it's own section.
		String sectionName = this.getClass().getSimpleName();
		Assert.isNotNull(sectionName);

		IDialogSettings section = settings.getSection(sectionName);
		if (section == null) {
			section = settings.addNewSection(sectionName);
		}

        return section;
	}

	/**
	 * Restore the widget values from the dialog settings store to recreate the control history.
	 * <p>
	 * <b>Note:</b>
	 * The control is saving the widget values into a section equal to the class name {@link Class#getName()}.
	 * After the sections has been created, the method calls <code>doRestoreWidgetValues</code> for restoring
	 * the single properties from the dialog settings. Subclasses may override <code>doRestoreWidgetValues</code>
	 * only to deal with the single properties only or <code>restoreWidgetValues</code> when to override the
	 * creation of the subsections.
	 *
	 * @param settings The dialog settings object instance to restore the widget values from. Must not be <code>null</code>!
	 * @param idPrefix The prefix to use for every dialog settings slot keys. If <code>null</code>, the dialog settings slot keys are not to prefix.
	 */
	public final void restoreWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);

		// now, call the hook for actually reading the single properties from the dialog settings.
		doRestoreWidgetValues(getDialogSettings(settings), idPrefix);
	}

	/**
	 * Hook to restore the widget values finally plain from the given dialog settings. This method should
	 * not fragment the given dialog settings any further.
	 *
	 * @param settings The dialog settings to restore the widget values from. Must not be <code>null</code>!
	 * @param idPrefix The prefix to use for every dialog settings slot keys. If <code>null</code>, the dialog settings slot keys are not to prefix.
	 */
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);

		for (String panelKey : configurationPanels.keySet()) {
			IConfigurationPanel configPanel = getConfigurationPanel(panelKey);
			if (configPanel != null && !isEmptyConfigurationPanel(configPanel)) {
				IDialogSettings configPanelSettings = settings.getSection(panelKey);
				if (configPanelSettings == null) configPanelSettings = settings.addNewSection(panelKey);
				configPanel.doRestoreWidgetValues(configPanelSettings, idPrefix);
			}
		}
	}

	/**
	 * Saves the widget values to the dialog settings store for remembering the history. The control might
	 * be embedded within multiple pages multiple times handling different properties. Because the single
	 * controls should not mix up the history, we create subsections within the given dialog settings if
	 * they do not already exist. After the sections has been created, the method calls <code>doSaveWidgetValues</code>
	 * for saving the single properties to the dialog settings. Subclasses may override <code>doSaveWidgetValues</code>
	 * only to deal with the single properties only or <code>saveWidgetValues</code> when to override the
	 * creation of the subsections.
	 *
	 * @param settings The dialog settings object instance to save the widget values to. Must not be <code>null</code>!
	 * @param idPrefix The prefix to use for every dialog settings slot keys. If <code>null</code>, the dialog settings slot keys are not to prefix.
	 */
	public final void saveWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);

		// now, call the hook for actually writing the single properties to the dialog settings.
		doSaveWidgetValues(getDialogSettings(settings), idPrefix);
	}

	/**
	 * Hook to save the widget values finally plain to the given dialog settings. This method should
	 * not fragment the given dialog settings any further.
	 *
	 * @param settings The dialog settings to save the widget values to. Must not be <code>null</code>!
	 * @param idPrefix The prefix to use for every dialog settings slot keys. If <code>null</code>, the dialog settings slot keys are not to prefix.
	 */
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);

		IConfigurationPanel configPanel = getActiveConfigurationPanel();
		if (configPanel != null && !isEmptyConfigurationPanel(configPanel)) {
			String key = getActiveConfigurationPanelKey();
			IDialogSettings configPanelSettings = settings.getSection(key);
			if (configPanelSettings == null) configPanelSettings = settings.addNewSection(key);
			configPanel.doSaveWidgetValues(configPanelSettings, idPrefix);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessage()
	 */
	@Override
	public final String getMessage() {
		return message;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessageType()
	 */
	@Override
	public final int getMessageType() {
		return messageType;
	}

	/**
	 * Set the message and the message type to display.
	 *
	 * @param message The message or <code>null</code>.
	 * @param messageType The message type or <code>IMessageProvider.NONE</code>.
	 */
	@Override
    public final void setMessage(String message, int messageType) {
		this.message = message;
		this.messageType = messageType;
	}
}
