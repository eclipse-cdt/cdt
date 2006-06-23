/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.ui;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * The common interface for UI components of the launch configuration tabs.
 */
public interface IMILaunchConfigurationComponent {

	/**
	 * Creates the top level control for this component under the given parent composite.
	 * <p>
	 * Implementors are responsible for ensuring that the created control can be accessed via <code>getControl</code>
	 * </p>
	 * 
	 * @param parent the parent composite
	 */
	public void createControl( Composite parent );

	/**
	 * Returns the top level control for this component.
	 * <p>
	 * May return <code>null</code> if the control has not been created yet.
	 * </p>
	 * 
	 * @return the top level control or <code>null</code>
	 */
	public Control getControl();

	/**
	 * Initializes the given component with default values. 
	 * This method may be called before this tab's control is created.
	 * 
	 * @param configuration launch configuration
	 */
	public void setDefaults( ILaunchConfigurationWorkingCopy configuration );

	/**
	 * Initializes this component's controls with values from the given 
	 * launch configuration.
	 * 
	 * @param configuration launch configuration
	 */
	public void initializeFrom( ILaunchConfiguration configuration );

	/**
	 * Notifies this component that it has been disposed. 
	 * Marks the end of this component's lifecycle, allowing 
	 * to perform any cleanup required.
	 */
	public void dispose();

	/**
	 * Copies values from this component into the given launch configuration.
	 * 
	 * @param configuration launch configuration
	 */
	public void performApply( ILaunchConfigurationWorkingCopy configuration );

	/**
	 * Returns whether this component is in a valid state in the context 
	 * of the specified launch configuration.
	 *
	 * @param launchConfig launch configuration which provides context 
	 * 		   for validating this component.
	 *         This value must not be <code>null</code>.
	 *
	 * @return whether this component is in a valid state
	 */
	public boolean isValid(ILaunchConfiguration launchConfig);
}
