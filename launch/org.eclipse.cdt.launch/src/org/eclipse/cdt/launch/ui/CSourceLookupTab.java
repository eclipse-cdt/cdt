/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.launch.ui;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.ui.sourcelookup.SourceLookupBlock;
import org.eclipse.cdt.launch.internal.ui.LaunchImages;
import org.eclipse.cdt.launch.sourcelookup.DefaultSourceLocator;
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
		configuration.setAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, DefaultSourceLocator.ID_DEFAULT_SOURCE_LOCATOR );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom( ILaunchConfiguration configuration )
	{
		IProject project = getProject( configuration );
		IProject oldProject = fBlock.getProject();
		fBlock.setProject( getProject( configuration ) );
		if ( project != null )
		{
			try
			{
				String id = configuration.getAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, "" );
				if ( isEmpty( id ) || DefaultSourceLocator.ID_DEFAULT_SOURCE_LOCATOR.equals( id ) )
				{
					DefaultSourceLocator locator = new DefaultSourceLocator();
					String memento = configuration.getAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, "" );
					if ( project.equals( oldProject ) && !isEmpty( memento ) )
					{
						locator.initializeFromMemento( memento );				
					}
					else
					{
						locator.initializeDefaults( configuration );
					}
					ICSourceLocator clocator = (ICSourceLocator)locator.getAdapter( ICSourceLocator.class );
					if ( clocator != null )
						fBlock.initialize( clocator );
				}
			}
			catch( CoreException e )
			{
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply( ILaunchConfigurationWorkingCopy configuration )
	{
		configuration.setAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, DefaultSourceLocator.ID_DEFAULT_SOURCE_LOCATOR );
		IProject project = getProject( configuration );
		if ( project != null )
		{
			DefaultSourceLocator locator = new DefaultSourceLocator();
			try
			{
				locator.initializeDefaults( configuration );
				ICSourceLocator clocator = (ICSourceLocator)locator.getAdapter( ICSourceLocator.class );
				if ( clocator != null )
				{
					if ( !project.equals( fBlock.getProject() ) )
						fBlock.initialize( clocator );
					clocator.setSourceLocations( fBlock.getSourceLocations() );
					clocator.setSearchForDuplicateFiles( fBlock.searchForDuplicateFiles() );
				}
				configuration.setAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, locator.getMemento() );
			}
			catch( CoreException e )
			{
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName()
	{
		return "Source";
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
			String projectName = configuration.getAttribute( ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "" );
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

}
