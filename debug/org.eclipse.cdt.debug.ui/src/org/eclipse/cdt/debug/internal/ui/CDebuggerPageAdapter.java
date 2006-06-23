/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui; 

import org.eclipse.cdt.debug.ui.ICDebuggerPage;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
 
/**
 * Migration from <code>AbstractLaunchConfigurationTab</code> to <code>ICDebuggerPage</code>.
 * 
 * @since 3.1
 */
public class CDebuggerPageAdapter implements ICDebuggerPage {

	private ILaunchConfigurationTab fDelegate;
	private String fDebuggerId;

	/** 
	 * Constructor for CDebuggerPageAdapter. 
	 */
	public CDebuggerPageAdapter( ILaunchConfigurationTab tab ) {
		fDelegate = tab;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.ICDebuggerPage#init(java.lang.String)
	 */
	public void init( String debuggerID ) {
		fDebuggerId = debuggerID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.ICDebuggerPage#getDebuggerIdentifier()
	 */
	public String getDebuggerIdentifier() {
		return fDebuggerId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl( Composite parent ) {
		fDelegate.createControl( parent );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getControl()
	 */
	public Control getControl() {
		return fDelegate.getControl();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults( ILaunchConfigurationWorkingCopy configuration ) {
		fDelegate.setDefaults( configuration );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom( ILaunchConfiguration configuration ) {
		fDelegate.initializeFrom( configuration );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
		fDelegate.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply( ILaunchConfigurationWorkingCopy configuration ) {
		fDelegate.performApply( configuration );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getErrorMessage()
	 */
	public String getErrorMessage() {
		return fDelegate.getErrorMessage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getMessage()
	 */
	public String getMessage() {
		return fDelegate.getMessage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid( ILaunchConfiguration launchConfig ) {
		return fDelegate.isValid( launchConfig );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#canSave()
	 */
	public boolean canSave() {
		return fDelegate.canSave();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setLaunchConfigurationDialog(org.eclipse.debug.ui.ILaunchConfigurationDialog)
	 */
	public void setLaunchConfigurationDialog( ILaunchConfigurationDialog dialog ) {
		fDelegate.setLaunchConfigurationDialog( dialog );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#launched(org.eclipse.debug.core.ILaunch)
	 */
	public void launched( ILaunch launch ) {
		fDelegate.launched( launch );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return fDelegate.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return fDelegate.getImage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated( ILaunchConfigurationWorkingCopy workingCopy ) {
		fDelegate.activated( workingCopy );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#deactivated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void deactivated( ILaunchConfigurationWorkingCopy workingCopy ) {
		fDelegate.deactivated( workingCopy );
	}
}
