/**********************************************************************
 * Copyright (c) 2002 - 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.launch.ui;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.sourcelookup.SourceLookupBlock;
import org.eclipse.cdt.launch.internal.ui.LaunchImages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Enter type comment.
 * 
 * @since: Feb 13, 2003
 */
public class CSourceLookupTab extends CLaunchConfigurationTab
{
	private SourceLookupBlock fBlock = null;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl( Composite parent )
	{
		Composite control = new Composite( parent, SWT.NONE );
		control.setLayout( new GridLayout() );
		setControl( control );

		WorkbenchHelp.setHelp(getControl(), ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_SOURCELOOKUP_TAB);
		
		fBlock = new SourceLookupBlock();
		fBlock.createControl( control );
		fBlock.setLaunchConfigurationDialog( getLaunchConfigurationDialog() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults( ILaunchConfigurationWorkingCopy configuration )
	{
		configuration.setAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, CDebugUIPlugin.getDefaultSourceLocatorID() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom( ILaunchConfiguration configuration )
	{
		fBlock.initialize( configuration );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply( ILaunchConfigurationWorkingCopy configuration )
	{
		configuration.setAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, CDebugUIPlugin.getDefaultSourceLocatorID() );
		IProject project = getProject( configuration );
		if ( project != null )
			fBlock.performApply( configuration );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName()
	{
		return LaunchUIPlugin.getResourceString("CSourceLookupTab.Source"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() 
	{
		return LaunchImages.get( LaunchImages.IMG_VIEW_SOURCE_TAB );
	}

	private IProject getProject( ILaunchConfiguration configuration )
	{
		IProject project = null;
		try
		{
			String projectName = configuration.getAttribute( ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "" ); //$NON-NLS-1$
			if ( !isEmpty( projectName ) )
				project = ResourcesPlugin.getWorkspace().getRoot().getProject( projectName );
		}
		catch( CoreException e )
		{
		}
		return project;
	}

	private boolean isEmpty( String string )
	{
		return string == null || string.length() == 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	public void dispose()
	{
		if ( fBlock != null )
			fBlock.dispose();
		super.dispose();
	}
}
