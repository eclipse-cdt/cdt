/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.launch.ui;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.sourcelookup.SourceLookupBlock;
import org.eclipse.cdt.launch.sourcelookup.DefaultSourceLocator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Composite;

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
		fBlock = new SourceLookupBlock();
		fBlock.createControl( parent );
		fBlock.setLaunchConfigurationDialog( getLaunchConfigurationDialog() );
		setControl( fBlock.getControl() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults( ILaunchConfigurationWorkingCopy configuration )
	{
		configuration.setAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, DefaultSourceLocator.ID_DEFAULT_SOURCE_LOCATOR );
		DefaultSourceLocator locator = new DefaultSourceLocator();
		try
		{
			locator.initializeDefaults( configuration );
			configuration.setAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, locator.getMemento() );
		}
		catch( CoreException e )
		{
			CDebugUIPlugin.log( e.getStatus() );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom( ILaunchConfiguration configuration )
	{
		try
		{
			String id = configuration.getAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, "" );
			if ( isEmpty( id )|| DefaultSourceLocator.ID_DEFAULT_SOURCE_LOCATOR.equals( id ) )
			{
				DefaultSourceLocator locator = new DefaultSourceLocator();
				String memento = configuration.getAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, "" );
				if ( !isEmpty( memento ) )
				{
					locator.initializeFromMemento( memento );				
				}
				else
				{
					locator.initializeDefaults( configuration );
				}
				ICSourceLocator clocator = (ICSourceLocator)locator.getAdapter( ICSourceLocator.class );
				if ( clocator != null )
					fBlock.initialize( clocator.getSourceLocations() );
			}
		}
		catch( CoreException e )
		{
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply( ILaunchConfigurationWorkingCopy configuration )
	{
		DefaultSourceLocator locator = new DefaultSourceLocator();
		try
		{
			locator.initializeDefaults( configuration );
			ICSourceLocator clocator = (ICSourceLocator)locator.getAdapter( ICSourceLocator.class );
			if ( clocator != null )
				clocator.setSourceLocations( fBlock.getSourceLocations() );
			configuration.setAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, DefaultSourceLocator.ID_DEFAULT_SOURCE_LOCATOR );
			configuration.setAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, locator.getMemento() );
		}
		catch( CoreException e )
		{
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName()
	{
		return "Source Lookup";
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
