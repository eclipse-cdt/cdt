/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.editors;

import java.util.ArrayList;

import org.eclipse.cdt.debug.core.ICBreakpointManager;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.sourcelookup.IDisassemblyStorage;
import org.eclipse.cdt.debug.internal.core.breakpoints.CAddressBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CFunctionBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CLineBreakpoint;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * Enter type comment.
 * 
 * @since: Jan 9, 2003
 */
public class DisassemblyMarkerAnnotationModel extends AbstractMarkerAnnotationModel
{
	/**
	 * Internal resource change listener.
	 */
	class ResourceChangeListener implements IResourceChangeListener
	{
		/*
		 * @see IResourceChangeListener#resourceChanged
		 */
		public void resourceChanged( IResourceChangeEvent e )
		{
			IResourceDelta delta = e.getDelta();
			try
			{
				if ( delta != null )
					delta.accept( getResourceDeltaVisitor() );
			}
			catch( CoreException x )
			{
				doHandleCoreException( x, "Resource Changed" ); 
			}
		}
	}

	/**
	 * Internal resource delta visitor.
	 */
	class ResourceDeltaVisitor implements IResourceDeltaVisitor
	{
		/*
		 * @see IResourceDeltaVisitor#visit
		 */
		public boolean visit( IResourceDelta delta ) throws CoreException
		{
			if ( delta != null && /*getResource().equals( delta.getResource() )*/delta.getResource() instanceof IFile )
			{
				update( delta.getMarkerDeltas() );
				return false;
			}
			return true;
		}
	}

	/** The workspace */
	private IWorkspace fWorkspace;

	/** The resource */
	private IResource fResource;

	/** The resource change listener */
	private IResourceChangeListener fResourceChangeListener = new ResourceChangeListener();

	/** The resource delta visitor */
	private IResourceDeltaVisitor fResourceDeltaVisitor = new ResourceDeltaVisitor();

	private IDisassemblyStorage fStorage = null;

	/**
	 * Constructor for DisassemblyMarkerAnnotationModel.
	 */
	public DisassemblyMarkerAnnotationModel( IDisassemblyStorage storage, IResource resource )
	{
		fResource = resource;
		fWorkspace = resource.getWorkspace();
		fStorage = storage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#retrieveMarkers()
	 */
	protected IMarker[] retrieveMarkers() throws CoreException
	{
		if ( fStorage == null )
			return null;
		IDebugTarget target = fStorage.getDebugTarget();
		if ( target != null )
		{
			IBreakpointManager bm = DebugPlugin.getDefault().getBreakpointManager();
			IBreakpoint[] brkpts = bm.getBreakpoints();
			ArrayList list = new ArrayList( brkpts.length );
			for ( int i = 0; i < brkpts.length; ++i )
			{
				if ( target.supportsBreakpoint( brkpts[i] ) && isAcceptable( brkpts[i].getMarker() ) )
				{
					list.add( brkpts[i].getMarker() );
				}
			}
			return (IMarker[])list.toArray( new IMarker[list.size()] );
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#deleteMarkers(IMarker[])
	 */
	protected void deleteMarkers( final IMarker[] markers ) throws CoreException
	{
		fWorkspace.run( new IWorkspaceRunnable()
							{
								public void run( IProgressMonitor monitor ) throws CoreException
								{
									for ( int i = 0; i < markers.length; ++i )
									{
										markers[i].delete();
									}
								}
							}, null );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#listenToMarkerChanges(boolean)
	 */
	protected void listenToMarkerChanges( boolean listen )
	{
		if ( listen )
			fWorkspace.addResourceChangeListener( fResourceChangeListener );
		else
			fWorkspace.removeResourceChangeListener( fResourceChangeListener );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#isAcceptable(IMarker)
	 */
	protected boolean isAcceptable( IMarker marker )
	{
		try
		{
			return ( marker.getType().equals( CLineBreakpoint.getMarkerType() ) ||
					 marker.getType().equals( CFunctionBreakpoint.getMarkerType() ) ||
					 marker.getType().equals( CAddressBreakpoint.getMarkerType() ) );
		}
		catch( CoreException e )
		{
		}
		return false;
	}

	protected IResourceDeltaVisitor getResourceDeltaVisitor()
	{
		return fResourceDeltaVisitor;
	}

	/**
	 * Updates this model to the given marker deltas.
	 *
	 * @param markerDeltas the list of marker deltas
	 */
	protected void update( IMarkerDelta[] markerDeltas )
	{
		if ( markerDeltas.length == 0 )
			return;

		for( int i = 0; i < markerDeltas.length; i++ )
		{
			IMarkerDelta delta = markerDeltas[i];
			switch( delta.getKind() )
			{
				case IResourceDelta.ADDED :
					addMarkerAnnotation( delta.getMarker() );
					break;
				case IResourceDelta.REMOVED :
					removeMarkerAnnotation( delta.getMarker() );
					break;
				case IResourceDelta.CHANGED :
					modifyMarkerAnnotation( delta.getMarker() );
					break;
			}
		}

		fireModelChanged();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#createPositionFromMarker(IMarker)
	 */
	protected Position createPositionFromMarker( IMarker marker )
	{
		try
		{
			if ( marker.getType().equals( CLineBreakpoint.getMarkerType() ) )
			{
				return createPositionFromLineBreakpoint( marker );
			}
			if ( marker.getType().equals( CFunctionBreakpoint.getMarkerType() ) )
			{
				return createPositionFromLineBreakpoint( marker );
			}
			if ( marker.getType().equals( CAddressBreakpoint.getMarkerType() ) )
			{
				return createPositionFromAddressBreakpoint( marker );
			}
		}
		catch( CoreException e )
		{
		}
		return null;
	}

	private Position createPositionFromLineBreakpoint( IMarker marker )
	{
		if ( fStorage != null )
		{
			IBreakpoint breakpoint = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint( marker );
			if ( breakpoint instanceof ICBreakpoint )
			{		 
				IDebugTarget target = fStorage.getDebugTarget();
				if ( target != null && target.getAdapter( ICBreakpointManager.class ) != null )
				{
					ICBreakpointManager bm = (ICBreakpointManager)target.getAdapter( ICBreakpointManager.class );
					long address = bm.getBreakpointAddress( (ICBreakpoint)breakpoint );
					if ( address != 0 )
						return createPositionFromAddress( address );
				}
			}
		}
		return null;
	}

	private Position createPositionFromAddressBreakpoint( IMarker marker ) throws CoreException
	{
		try
		{
			return createPositionFromAddress( Long.parseLong( marker.getAttribute( ICAddressBreakpoint.ADDRESS, "0" ) ) ); //$NON-NLS-1$
		}
		catch( NumberFormatException e )
		{
		}
		return null;
	}

	private Position createPositionFromAddress( long address )
	{
		try
		{
			int start = -1;
			int line = fStorage.getLineNumber( address );
			if ( line > 0 && fDocument != null ) 
			{
				start = fDocument.getLineOffset( line - 1 );
				if ( start > -1 )
				{
					return new Position( start, fDocument.getLineLength( line - 1 ) );
				}
			}
		}
		catch ( BadLocationException x ) 
		{
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#createMarkerAnnotation(IMarker)
	 */
	protected MarkerAnnotation createMarkerAnnotation( IMarker marker )
	{
		return new DisassemblyMarkerAnnotation( marker );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#getMarkerPosition(IMarker)
	 */
	public Position getMarkerPosition( IMarker marker )
	{
		return createPositionFromMarker( marker );
	}

	protected void doHandleCoreException( CoreException e, String message )
	{
		handleCoreException( e, message );
	}
	
	protected IResource getResource()
	{
		return fResource;
	}
}
