/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICSourceLocator;
import org.eclipse.cdt.debug.core.IStackFrameInfo;
import org.eclipse.cdt.debug.internal.core.model.*;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;

/**
 * 
 * Locates source for a C/C++ debug session.
 * 
 * @since Aug 19, 2002
 */
public class CSourceLocator implements ICSourceLocator
{
	/**
	 * The project in which to look for source.
	 */
	private IProject fProject;

	/**
	 * The debug target this source locator is associated with.
	 */
	private CDebugTarget fTarget;

	/**
	 * The source presentation mode.
	 */
	private int fMode = ICSourceLocator.MODE_SOURCE;

	private int fInternalMode = MODE_SOURCE;

	/**
	 * Constructor for CSourceLocator.
	 */
	public CSourceLocator( CDebugTarget target, IProject project )
	{
		fProject = project;
		fTarget = target;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(IStackFrame)
	 */
	public Object getSourceElement( IStackFrame stackFrame )
	{
		Object result = null;
		if ( stackFrame != null && stackFrame.getAdapter( IStackFrameInfo.class ) != null )
		{
			try
			{
				result = getInput( (IStackFrameInfo)stackFrame.getAdapter( IStackFrameInfo.class ) );
			}
			catch( DebugException e )
			{
				CDebugCorePlugin.log( e );
			}
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSourceLocator#getLineNumber(IStackFrameInfo)
	 */
	public int getLineNumber( IStackFrameInfo frameInfo )
	{
		int result = 0;
		if ( getMode() == MODE_SOURCE )
			result = frameInfo.getFrameLineNumber();
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSourceLocator#getMode()
	 */
	public int getMode()
	{
		return fMode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSourceLocator#setMode(int)
	 */
	public void setMode( int mode )
	{
	}

	protected void setInternalMode( int mode ) 
	{
		fInternalMode = mode;
	}

	protected Object getInput( IStackFrameInfo frameInfo ) throws DebugException
	{
		Object result = null;
		switch( getMode() )
		{
			case ICSourceLocator.MODE_SOURCE:
				result = getSourceInput( frameInfo );
				break;
/*
			case ICSourceLocator.MODE_DISASSEMBLY:
				result = getDisassemblyInput( frameInfo );
				break;
			case ICSourceLocator.MODE_MIXED:
				result = getMixedInput( frameInfo );
				break;
*/
		}
		return result;
	}

	private Object getSourceInput( IStackFrameInfo info )
	{
		Object result = null;
		if ( info != null && fProject != null )
		{
			setInternalMode( ICSourceLocator.MODE_SOURCE );
			String fileName = info.getFile();
			if ( fileName != null && fileName.length() > 0 )
				result = findFile( fProject, fileName );
		}		
		// switch to assembly mode if source file not found	
/*
		if ( result == null )
			result = getDisassemblyInput( info );
*/
		return result;
	}

	private Object findFile( IProject project, String fileName )
	{
		IPath path = new Path( fileName );
		fileName = path.toOSString();

		/*
		 * We have a few possible cases: either we get a source file name with no path,
		 * or we get a fully-qualified filename from the debugger.
		 */
		String pPath = new String( project.getLocation().toOSString() );
		int i;

		if ( (i = fileName.indexOf( pPath )) >= 0 ) 
		{
			i += pPath.length() + 1;
			if ( fileName.length() > i )
				return project.getFile( fileName.substring( i ) );
		} 
		// Then just do a search on our project, and if that fails, any dependent projects
		IFile f = null;
		try 
		{
			f = findFile( (IContainer)project, path );
			if ( f != null ) 
				return f;
			if ( f == null ) 
			{
				IProject[] p = project.getReferencedProjects();
				for ( int j= 0; j < p.length; j++ ) 
				{
					if ( (f = findFile( (IContainer)p[j], new Path( fileName ) )) != null ) 
					{
						return f;
					}
				}
			}
		} 
		catch( CoreException e ) 
		{
		}
		if ( f != null )
			return f;
		// Search for an external file
//		return findExternalFile( fileName );
		return null;
	}

	private IFile findFile( IContainer parent, IPath name ) throws CoreException 
	{
		if ( name.isAbsolute() )
		{
			if ( name.toOSString().startsWith( parent.getLocation().toOSString() ) )
			{
				name = new Path( name.toOSString().substring( parent.getLocation().toOSString().length() + 1 ) );
			}
		}
		IResource found = parent.findMember( name );
		if ( found != null && found.getType() == IResource.FILE ) 
		{
			return (IFile)found;
		}
		IResource[] children= parent.members();
		for ( int i= 0; i < children.length; i++ ) 
		{
			if ( children[i] instanceof IContainer ) 
			{
				return findFile( (IContainer)children[i], name );
			}
		}
		return null;		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSourceLocator#getSourceElement(String)
	 */
	public Object getSourceElement( String fileName )
	{
		return findFile( fProject, fileName );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSourceLocator#getSourceElementForAddress(long)
	 */
	public Object getSourceElementForAddress( long address )
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSourceLocator#getSourceElementForFunction(String)
	 */
	public Object getSourceElementForFunction( String function )
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSourceLocator#contains(IResource)
	 */
	public boolean contains( IResource resource )
	{
		if ( resource instanceof IProject )
		{
			if ( fProject.equals( resource ) )
			{
				return true;
			}
			IProject[] projects = getReferencedProjects();
			for ( int i = 0; i < projects.length; ++i )
			{
				if ( projects[i].equals( resource ) )
				{
					return true;
				}
			}
			return false;
		}
		if ( resource instanceof IFile )
		{
			return containsFile( resource.getLocation() );
		}
		return false;
	}
	
	protected IProject[] getReferencedProjects()
	{
		IProject[] projects = new IProject[0];
		try
		{
			projects = fProject.getReferencedProjects();
		}
		catch( CoreException e )
		{
			CDebugCorePlugin.log( e );
		}
		return projects;
	}
	
	protected boolean containsFile( IPath path )
	{
		try 
		{
			if ( findFile( (IContainer)fProject, path ) != null )
				return true;
			IProject[] p = fProject.getReferencedProjects();
			for ( int j= 0; j < p.length; j++ ) 
			{
				if ( findFile( (IContainer)p[j], path ) != null ) 
					return true;
			}
		} 
		catch( CoreException e ) 
		{
		}
		return false;
	}
}
