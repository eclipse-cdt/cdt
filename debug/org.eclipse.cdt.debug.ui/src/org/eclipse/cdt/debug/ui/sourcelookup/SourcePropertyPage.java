/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.ui.sourcelookup;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * 
 * Enter type comment.
 * 
 * @since Dec 18, 2002
 */
public class SourcePropertyPage extends PropertyPage
{
	private SourceLookupBlock fBlock = null;
	
	private boolean fHasActiveContents = false;

	/**
	 * Constructor for SourcePropertyPage.
	 */
	public SourcePropertyPage()
	{
		noDefaultAndApplyButton();
		fBlock = new SourceLookupBlock();
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContents( Composite parent )
	{
		ICDebugTarget target = getDebugTarget();
		if ( target == null || target.isTerminated() || target.isDisconnected() )
		{
			return createTerminatedContents( parent );
		}
		fHasActiveContents = true;
		return createActiveContents( parent );
	}

	protected Control createTerminatedContents( Composite parent )
	{
		Label label= new Label( parent, SWT.LEFT );
		label.setText( "Terminated." );
		return label;
	}

	protected Control createActiveContents( Composite parent )
	{
		fBlock.setProject( getProject() );
		fBlock.initialize( getSourceLocator() );
		fBlock.createControl( parent );
		return fBlock.getControl();
	}
	
	protected ICDebugTarget getDebugTarget()
	{
		IAdaptable element = getElement();
		if ( element != null )
		{
			return (ICDebugTarget)element.getAdapter( ICDebugTarget.class );
		}
		return null;
	}
	
	private ICSourceLocator getSourceLocator()
	{
		ICDebugTarget target = getDebugTarget();
		if ( target != null )
		{
			if ( target.getLaunch().getSourceLocator() instanceof IAdaptable )
			{
				return (ICSourceLocator)((IAdaptable)target.getLaunch().getSourceLocator()).getAdapter( ICSourceLocator.class );
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk()
	{
		if ( fBlock.isDirty() )
		{
			try
			{
				setSourceLocations( fBlock.getSourceLocations() );
			}
			catch( DebugException e )
			{
				CDebugUIPlugin.errorDialog( e.getMessage(), (IStatus)null );
				return false;
			}
		}
		return true;
	}
	
	private void setSourceLocations( ICSourceLocation[] locations ) throws DebugException
	{
		ICDebugTarget target = getDebugTarget();
		if ( target != null )
		{
			if ( target.getLaunch().getSourceLocator() instanceof IAdaptable )
			{
				ICSourceLocator locator = (ICSourceLocator)((IAdaptable)target.getLaunch().getSourceLocator()).getAdapter( ICSourceLocator.class );
				if ( locator != null )
				{
					locator.setSourceLocations( locations );
					if ( target.getLaunch().getSourceLocator() instanceof IPersistableSourceLocator )
					{
						ILaunchConfiguration configuration = target.getLaunch().getLaunchConfiguration();
						saveChanges( configuration, (IPersistableSourceLocator)target.getLaunch().getSourceLocator() );
					}
				}
			}
		}
	}

	protected void saveChanges( ILaunchConfiguration configuration, IPersistableSourceLocator locator )
	{
		try
		{
			ILaunchConfigurationWorkingCopy copy = configuration.copy( configuration.getName() );
			copy.setAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, locator.getMemento() );
			copy.doSave();
		}
		catch( CoreException e )
		{
			CDebugUIPlugin.errorDialog( e.getMessage(), (IStatus)null );
		}
	}

	private IProject getProject()
	{
		IProject project = null;
		ICDebugTarget target = getDebugTarget();
		if ( target != null )
		{
			ILaunchConfiguration configuration = target.getLaunch().getLaunchConfiguration();
			try
			{
				String projectName = configuration.getAttribute( ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "" );
				if ( projectName != null && projectName.length() > 0 )
					project = ResourcesPlugin.getWorkspace().getRoot().getProject( projectName );
			}
			catch( CoreException e )
			{
			}
		}
		return project;
	}
}
