/*******************************************************************************
 * Copyright (c) 2013, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.provisional.api;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

/**
 * Abstract settings page providing a common implementation of the listener handling.
 */
public abstract class AbstractSettingsPage implements ISettingsPage, IMessageProvider {
	// A message associated with the control.
	private String message = null;

	// The message type of the associated message.
	private int messageType = IMessageProvider.NONE;

	// Reference to the listener
	private final ListenerList listeners = new ListenerList();

	// Flag to control the control decorations
	private boolean hasDecoration = false;

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage#addListener(org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage.Listener)
	 */
	public void addListener(Listener listener) {
    	Assert.isNotNull(listener);
    	listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage#removeListener(org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage.Listener)
	 */
	public void removeListener(Listener listener) {
    	Assert.isNotNull(listener);
    	listeners.remove(listener);
	}

	/**
	 * Fire the listeners for the given control.
	 *
	 * @param control The control or <code>null</code>.
	 */
	public void fireListeners(Control control) {
		Object[] list = listeners.getListeners();
		for (int i = 0; i < list.length; i++) {
			Object l = list[i];
			if (!(l instanceof Listener)) continue;
			((Listener)l).onSettingsPageChanged(control);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessage()
	 */
	public final String getMessage() {
		return message;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessageType()
	 */
	public final int getMessageType() {
		return messageType;
	}

	/**
	 * Set the message and the message type.
	 *
	 * @param message The message or <code>null</code>.
	 * @param messageType The type of the message (NONE, INFORMATION, WARNING, ERROR).
	 */
	public final void setMessage(String message, int messageType) {
		this.message = message;
		this.messageType = messageType;
	}

	/**
	 * Sets if or if not the settings panel widgets will have control decorations
	 * or not. The method has effect only if called before {@link #createControl(org.eclipse.swt.widgets.Composite)}.
	 *
	 * @param value <code>True</code> if the panel widgets have control decorations, <code>false</code> otherwise.
	 */
	public final void setHasControlDecoration(boolean value) {
		this.hasDecoration = value;
	}

	/**
	 * Returns if or if not the settings panel widgets will have control
	 * decorations or not.
	 *
	 * @return <code>True</code> if the panel widgets have control decorations, <code>false</code> otherwise.
	 */
	protected final boolean hasControlDecoration() {
		return hasDecoration;
	}

	/**
	 * Creates a new instance of a {@link ControlDecoration} object associated with
	 * the given control. The method is called after the control has been created.
	 *
	 * @param control The control. Must not be <code>null</code>.
	 * @return The control decoration object instance.
	 */
	protected final ControlDecoration createControlDecoration(Control control) {
		Assert.isNotNull(control);
		if (!hasDecoration) return null;
		ControlDecoration controlDecoration = new ControlDecoration(control, getControlDecorationPosition());
		controlDecoration.setShowOnlyOnFocus(false);
		control.setData("controlDecoration", controlDecoration); //$NON-NLS-1$
		return controlDecoration;
	}

	/**
	 * Returns the control decoration position. The default is
	 * {@link SWT#TOP} | {@link SWT#LEFT}.
	 *
	 * @return The control position.
	 */
	protected int getControlDecorationPosition() {
		return SWT.TOP | SWT.LEFT;
	}

	/**
	 * Updates the control decoration of the given control to represent the given message
	 * and message type. If the message is <code>null</code> or the message type is
	 * {@link IMessageProvider#NONE} no decoration will be shown.
	 *
	 * @param control The control. Must not be <code>null</code>.
	 * @param message The message.
	 * @param messageType The message type.
	 */
	protected final void updateControlDecoration(Control control, String message, int messageType) {
		Assert.isNotNull(control);

		ControlDecoration controlDecoration = (ControlDecoration)control.getData("controlDecoration"); //$NON-NLS-1$
		if (controlDecoration != null) {
			// The description is the same as the message
			controlDecoration.setDescriptionText(message);

			// The icon depends on the message type
			FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();

			// Determine the id of the decoration to show
			String decorationId = FieldDecorationRegistry.DEC_INFORMATION;
			if (messageType == IMessageProvider.ERROR) {
				decorationId = FieldDecorationRegistry.DEC_ERROR;
			} else if (messageType == IMessageProvider.WARNING) {
				decorationId = FieldDecorationRegistry.DEC_WARNING;
			}

			// Get the field decoration
			FieldDecoration fieldDeco = registry.getFieldDecoration(decorationId);
			if (fieldDeco != null) {
				controlDecoration.setImage(fieldDeco.getImage());
			}

			if (message == null || messageType == IMessageProvider.NONE) {
				controlDecoration.hide();
			}
			else {
				controlDecoration.show();
			}
		}
	}

}
