/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.launch.sourcelookup;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.ISourceMode;
import org.eclipse.cdt.debug.ui.sourcelookup.CUISourceLocator;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;

/**
 * The wrapper for the CUISourceLocator class.
 * 
 * @since: Dec 11, 2002
 */
public class DefaultSourceLocator implements IPersistableSourceLocator, IAdaptable
{
	/**
	 * Identifier for the 'Default C/C++ Source Locator' extension
	 * (value <code>"org.eclipse.cdt.launch.DefaultSourceLocator"</code>).
	 */
	public static final String ID_DEFAULT_SOURCE_LOCATOR = LaunchUIPlugin.getUniqueIdentifier() + ".DefaultSourceLocator"; //$NON-NLS-1$

	private CUISourceLocator fSourceLocator = null;
	private final static int ERROR = 1000;   // ????

	/**
	 * Constructor for DefaultSourceLocator.
	 */
	public DefaultSourceLocator()
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#getMemento()
	 */
	public String getMemento() throws CoreException
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeFromMemento(String)
	 */
	public void initializeFromMemento( String memento ) throws CoreException
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeDefaults(ILaunchConfiguration)
	 */
	public void initializeDefaults( ILaunchConfiguration configuration ) throws CoreException
	{
		fSourceLocator = new CUISourceLocator( getProject( configuration ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(IStackFrame)
	 */
	public Object getSourceElement( IStackFrame stackFrame )
	{
		return ( fSourceLocator != null ) ? fSourceLocator.getSourceElement( stackFrame ) : null;
	}
	
	private IProject getProject( ILaunchConfiguration configuration ) throws CoreException
	{
		String projectName = configuration.getAttribute( ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null );
		if ( projectName != null )
		{
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( projectName );
			if ( project.exists() )
			{
				return project;
			}
		}
		throw new CoreException( new Status( IStatus.ERROR, 
											 LaunchUIPlugin.getUniqueIdentifier(),
											 ERROR,
											 MessageFormat.format( "Project \"{0}\" does not exist.", new String[] { projectName } ),
											 null ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( fSourceLocator != null )
		{
			if ( adapter.equals( ICSourceLocator.class ) )
			{
				return fSourceLocator.getAdapter( adapter );
			}
			if ( adapter.equals( ISourceMode.class ) )
			{
				return fSourceLocator.getAdapter( adapter );
			}
		}
		return null;
	}
}
