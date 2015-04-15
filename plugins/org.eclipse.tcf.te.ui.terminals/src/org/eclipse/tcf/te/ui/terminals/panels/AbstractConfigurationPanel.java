/*******************************************************************************
 * Copyright (c) 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.panels;

import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel;
import org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanelContainer;

/**
 * Abstract terminal launcher configuration panel implementation.
 */
public abstract class AbstractConfigurationPanel implements IConfigurationPanel {
	private final IConfigurationPanelContainer container;

	private Composite topControl = null;

	// The selection
	private ISelection selection;

	private String message = null;
	private int messageType = IMessageProvider.NONE;

	private boolean enabled = true;

	/**
	 * Constructor.
	 *
	 * @param container The configuration panel container or <code>null</code>.
	 */
	public AbstractConfigurationPanel(IConfigurationPanelContainer container) {
		super();
		this.container = container;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel#getContainer()
	 */
	@Override
	public IConfigurationPanelContainer getContainer() {
	    return container;
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
	protected final void setMessage(String message, int messageType) {
		this.message = message;
		this.messageType = messageType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel#dispose()
	 */
	@Override
	public void dispose() {
	}

	/**
	 * Sets the top control.
	 *
	 * @param topControl The top control or <code>null</code>.
	 */
	protected void setControl(Composite topControl) {
		this.topControl = topControl;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel#getControl()
	 */
	@Override
	public Composite getControl() {
		return topControl;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void setSelection(ISelection selection) {
		this.selection = selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel#getSelection()
	 */
	@Override
	public ISelection getSelection() {
		return selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel#doRestoreWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
	}

	/**
	 * Returns the correctly prefixed dialog settings slot id. In case the given id
	 * suffix is <code>null</code> or empty, <code>id</code> is returned as is.
	 *
	 * @param settingsSlotId The dialog settings slot id to prefix.
	 * @param prefix The prefix.
	 * @return The correctly prefixed dialog settings slot id.
	 */
	public final String prefixDialogSettingsSlotId(String settingsSlotId, String prefix) {
		if (settingsSlotId != null && prefix != null && prefix.trim().length() > 0) {
			settingsSlotId = prefix + "." + settingsSlotId; //$NON-NLS-1$
		}
		return settingsSlotId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
     * @return Returns the enabled state.
     */
    public boolean isEnabled() {
	    return enabled;
    }

    /* (non-Javadoc)
     * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel#isValid()
     */
	@Override
	public boolean isValid() {
		setMessage(null, NONE);
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel#activate()
	 */
	@Override
	public void activate() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel#extractData(java.util.Map)
	 */
    @Override
    public void extractData(Map<String, Object> data) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel#setupData(java.util.Map)
     */
    @Override
    public void setupData(Map<String, Object> data) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel#updateData(java.util.Map)
     */
    @Override
    public void updateData(Map<String, Object> data) {
    }
}
