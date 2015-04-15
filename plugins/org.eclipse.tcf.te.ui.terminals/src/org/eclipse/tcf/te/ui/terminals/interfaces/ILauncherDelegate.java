/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.interfaces;

import java.util.Map;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.tcf.te.core.terminals.interfaces.ITerminalService;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;

/**
 * Terminal launcher delegate.
 */
@SuppressWarnings("restriction")
public interface ILauncherDelegate extends IExecutableExtension, IAdaptable {

	/**
	 * Returns the unique id of the launcher delegate. The returned
	 * id must be never <code>null</code> or an empty string.
	 *
	 * @return The unique id.
	 */
	public String getId();

	/**
	 * Returns the label or UI name of the launcher delegate.
	 *
	 * @return The label or UI name. An empty string if not set.
	 */
	public String getLabel();

	/**
	 * Returns if or if not the launcher delegate is hidden for the user.
	 *
	 * @return <code>True</code> if the launcher delegate is hidden, <code>false</code> otherwise.
	 */
	public boolean isHidden();

	/**
	 * Returns the enablement expression.
	 *
	 * @return The enablement expression or <code>null</code>.
	 */
	public Expression getEnablement();

	/**
	 * Returns if or if not the user needs to set configuration details for this launcher to work.
	 * The settings to configure are provided to the user through the configuration panel returned
	 * by {@link #getPanel(BaseDialogPageControl)}.
	 *
	 * @return <code>True</code> if a user configuration is required, <code>false</code> otherwise.
	 */
	public boolean needsUserConfiguration();

	/**
	 * Returns the configuration panel instance to present to the user. The instance must be always
	 * the same on subsequent calls until disposed.
	 * <p>
	 * The method may return <code>null</code> if the launcher does not provide any user
	 * configurable settings. In this case, {@link #needsUserConfiguration()} should return
	 * <code>false</code>.
	 *
	 * @param container The configuration panel container or <code>null</code>.
	 * @return The configuration panel instance or <code>null</code>
	 */
	public IConfigurationPanel getPanel(IConfigurationPanelContainer container);

	/**
	 * Execute the terminal launch.
	 *
	 * @param properties The properties. Must not be <code>null</code>.
	 * @param done The callback or <code>null</code>.
	 */
	public void execute(Map<String, Object> properties, ITerminalService.Done done);

	/**
	 * Creates the terminal connector for this launcher delegate based on
	 * the given properties.
	 *
	 * @param properties The terminal properties. Must not be <code>null</code>.
	 * @return The terminal connector or <code>null</code>.
	 */
    public ITerminalConnector createTerminalConnector(Map<String, Object> properties);
}
