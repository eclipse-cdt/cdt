/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.ui.builder;
import org.eclipse.cdt.core.builder.model.ICBuildConfig;
import org.eclipse.cdt.core.builder.model.ICBuildConfigWorkingCopy;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A tool settings configuration tab group is used to edit/view
 * parameters passed to a tool as part of a C/'C++ build step.
 * CToolPoint settings are presented in a dialog with a tab folder.
 * Each tab presents UI elements appropriate for manipulating
 * a set of parameters for a tool.
 * <p>
 * This interface is intended to be implemented by clients.
 */
public interface ICToolTab {

	/**
	 * Creates the top level control for this settings tab under
	 * the given parent composite.  This method is called once on
	 * tab creation, after <code>setConfigurationDialog</code>
	 * is called.
	 * <p>
	 * Implementors are responsible for ensuring that
	 * the created control can be accessed via <code>getControl</code>
	 * </p>
	 *
	 * @param parent the parent composite
	 */
	public void createControl(Composite parent);
	
	/**
	 * Returns the top level control for this tab.
	 * <p>
	 * May return <code>null</code> if the control
	 * has not been created yet.
	 * </p>
	 *
	 * @return the top level control or <code>null</code>
	 */
	public Control getControl();	
	
	/**
	 * Initializes the given configuration with default values
	 * for this tab. This method is called when a new configuration
	 * is created such that the configuration can be initialized with
	 * meaningful values. This method may be called before this
	 * tab's control is created.
	 * 
	 * @param configuration configuration
	 */
	public void setDefaults(ICBuildConfigWorkingCopy configuration);	
	
	/**
	 * Initializes this tab's controls with values from the given
	 * configuration. This method is called when a configuration is
	 * selected to view or edit, after thistab's control has been
	 * created.
	 * 
	 * @param configuration configuration
	 */
	public void initializeFrom(ICBuildConfig configuration);		
	
	/**
	 * Notifies this configuration tab that it has been disposed. Marks
	 * the end of this tab's lifecycle, allowing this tab to perform any
	 * cleanup required.
	 */
	public void dispose();
	
	/**
	 * Copies values from this tab into the given configuration.
	 * 
	 * @param configuration configuration
	 */
	public void performApply(ICBuildConfigWorkingCopy configuration);
	
	/**
	 * Returns the current error message for this tab.
	 * May be <code>null</code> to indicate no error message.
	 * <p>
	 * An error message should describe some error state,
	 * as opposed to a message which may simply provide instruction
	 * or information to the user.
	 * </p>
	 * 
	 * @return the error message, or <code>null</code> if none
	 */
	public String getErrorMessage();
	
	/**
	 * Returns the current message for this tab.
	 * <p>
	 * A message provides instruction or information to the 
	 * user, as opposed to an error message which should 
	 * describe some error state.
	 * </p>
	 * 
	 * @return the message, or <code>null</code> if none
	 */
	public String getMessage();	
	
	/**
	 * Returns whether this tab is in a state that allows the configuration
	 * whose values this tab is showing to be saved.
	 * <p>
	 * This information is typically used by the configuration dialog to
	 * decide when it is okay to save a configuration.
	 * </p>
	 * 
	 * @return whether this tab is in a state that allows the current
	 *          configuration to be saved
	 */
	public boolean canSave();
	
	/**
	 * Sets the configuration dialog that hosts this tab.  This is the
	 * first method called on a configuration tab, and marks the beginning
	 * of this tab's lifecycle.
	 * 
	 * @param dialog configuration dialog
	 */
	public void setConfigurationDialog(ICBuildConfigDialog dialog);
	
	/**
	 * Returns the name of this tab.
	 * 
	 * @return the name of this tab
	 */
	public String getName();
	
	/**
	 * Returns the image for this tab, or <code>null</code> if none
	 * 
	 * @return the image for this tab, or <code>null</code> if none
	 */
	public Image getImage();	

	/**
	 * Returns true if the current contents of a tab are valid.
	 * 
	 * @param config build configuration.
	 * @return boolean true if tab contents are valid, fale if not.
	 */
	boolean isValid(ICBuildConfigWorkingCopy config);

}
