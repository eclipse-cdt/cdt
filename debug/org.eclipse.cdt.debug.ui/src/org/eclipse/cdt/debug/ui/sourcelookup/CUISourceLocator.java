/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.ui.sourcelookup;

import org.eclipse.cdt.debug.core.model.IStackFrameInfo;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.ISourceMode;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLocator;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceManager;
import org.eclipse.cdt.debug.internal.ui.editors.FileNotFoundElement;
import org.eclipse.cdt.debug.internal.ui.editors.NoSymbolOrSourceElement;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;

/**
 * 
 * A source locator that prompts the user to find source when source cannot
 * be found on the current source lookup path.
 * 
 * @since Sep 24, 2002
 */
public class CUISourceLocator implements IAdaptable
{
	/**
	 * The project being debugged.
	 */
	private IProject fProject = null; 
	
	/**
	 * Underlying source locator.
	 */
	private CSourceManager fSourceLocator;
	
	/**
	 * Constructor for CUISourceLocator.
	 */
	public CUISourceLocator( IProject project )
	{
		fProject = project;
		fSourceLocator = new CSourceManager( new CSourceLocator( project ) );
	}

	public Object getSourceElement( IStackFrame stackFrame )
	{
		Object res = fSourceLocator.getSourceElement( stackFrame );
		if ( res == null )
		{
			IStackFrameInfo frameInfo = (IStackFrameInfo)stackFrame.getAdapter( IStackFrameInfo.class );
			if ( frameInfo != null && frameInfo.getFile() != null && frameInfo.getFile().length() > 0 )
			{
				res = new FileNotFoundElement( stackFrame );
			}
			else // don't show in editor
			{
				res = new NoSymbolOrSourceElement( stackFrame );
			}
		}
		return res;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( fSourceLocator != null )
		{
			if ( adapter.equals( ICSourceLocator.class ) )
				return fSourceLocator;
			if ( adapter.equals( IResourceChangeListener.class ) && fSourceLocator instanceof IAdaptable )
				return ((IAdaptable)fSourceLocator).getAdapter( IResourceChangeListener.class );
			if ( adapter.equals( ISourceMode.class ) )
				return fSourceLocator;
		}
		return null;
	}

	public IProject getProject()
	{
		return fProject;
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
}
