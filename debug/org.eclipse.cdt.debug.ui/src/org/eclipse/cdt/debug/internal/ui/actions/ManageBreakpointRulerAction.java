/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * 
 * Enter type comment.
 * 
 * @since Aug 23, 2002
 */
public class ManageBreakpointRulerAction extends Action implements IUpdate
{
	private IVerticalRulerInfo fRuler;
	private ITextEditor fTextEditor;
	private String fMarkerType;
	private List fMarkers;

	private String fAddLabel;
	private String fRemoveLabel;

	/**
	 * Constructor for ManageBreakpointRulerAction.
	 * 
	 * @param ruler
	 * @param editor
	 */
	public ManageBreakpointRulerAction( IVerticalRulerInfo ruler, ITextEditor editor )
	{
		fRuler = ruler;
		fTextEditor = editor;
		fMarkerType = IBreakpoint.BREAKPOINT_MARKER;
		fAddLabel = "Add Breakpoint";
		fRemoveLabel = "Remove Breakpoint";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update()
	{
		fMarkers = getMarkers();
		setText( fMarkers.isEmpty() ? fAddLabel : fRemoveLabel );
	}

	/**
	 * @see Action#run()
	 */
	public void run()
	{
		if ( fMarkers.isEmpty() )
		{
			addMarker();
		}
		else
		{
			removeMarkers( fMarkers );
		}
	}

	protected List getMarkers()
	{
		List breakpoints = new ArrayList();

		IResource resource = getResource();
		IDocument document = getDocument();
		AbstractMarkerAnnotationModel model = getAnnotationModel();

		if ( model != null )
		{
			try
			{
				IMarker[] markers = null;
				if ( resource instanceof IFile )
				{
					markers = resource.findMarkers( IBreakpoint.BREAKPOINT_MARKER,
													true,
													IResource.DEPTH_INFINITE );
				}
				else
				{
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
					markers = root.findMarkers( IBreakpoint.BREAKPOINT_MARKER,
												true,
												IResource.DEPTH_INFINITE );
				}

				if ( markers != null )
				{
					IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
					for ( int i = 0; i < markers.length; i++ )
					{
						IBreakpoint breakpoint = breakpointManager.getBreakpoint( markers[i] );
						if ( breakpoint != null && 
							 breakpointManager.isRegistered( breakpoint ) && 
							 includesRulerLine( model.getMarkerPosition( markers[i] ), document ) )
							breakpoints.add( markers[i] );
					}
				}
			}
			catch( CoreException x )
			{
				CDebugUIPlugin.log( x.getStatus() );
			}
		}
		return breakpoints;
	}

	/** 
	 * Returns the resource for which to create the marker, 
	 * or <code>null</code> if there is no applicable resource.
	 *
	 * @return the resource for which to create the marker or <code>null</code>
	 */
	protected IResource getResource()
	{
		IEditorInput input = fTextEditor.getEditorInput();
		IResource resource = (IResource)input.getAdapter( IFile.class );
		if ( resource == null )
			resource = (IResource)input.getAdapter( IResource.class );
		return resource;
	}

	/**
	 * Checks whether a position includes the ruler's line of activity.
	 *
	 * @param position the position to be checked
	 * @param document the document the position refers to
	 * @return <code>true</code> if the line is included by the given position
	 */
	protected boolean includesRulerLine( Position position, IDocument document )
	{
		if ( position != null )
		{
			try
			{
				int markerLine = document.getLineOfOffset( position.getOffset() );
				int line = fRuler.getLineOfLastMouseButtonActivity();
				if ( line == markerLine )
				{
					return true;
				}
			}
			catch( BadLocationException x )
			{
			}
		}
		return false;
	}

	/**
	 * Returns this action's vertical ruler info.
	 *
	 * @return this action's vertical ruler
	 */
	protected IVerticalRulerInfo getVerticalRulerInfo()
	{
		return fRuler;
	}

	/**
	 * Returns this action's editor.
	 *
	 * @return this action's editor
	 */
	protected ITextEditor getTextEditor()
	{
		return fTextEditor;
	}

	/**
	 * Returns the <code>AbstractMarkerAnnotationModel</code> of the editor's input.
	 *
	 * @return the marker annotation model
	 */
	protected AbstractMarkerAnnotationModel getAnnotationModel()
	{
		IDocumentProvider provider = fTextEditor.getDocumentProvider();
		IAnnotationModel model = provider.getAnnotationModel( fTextEditor.getEditorInput() );
		if ( model instanceof AbstractMarkerAnnotationModel )
		{
			return (AbstractMarkerAnnotationModel)model;
		}
		return null;
	}

	/**
	 * Returns the <code>IDocument</code> of the editor's input.
	 *
	 * @return the document of the editor's input
	 */
	protected IDocument getDocument()
	{
		IDocumentProvider provider = fTextEditor.getDocumentProvider();
		return provider.getDocument( fTextEditor.getEditorInput() );
	}

	protected void addMarker()
	{
		IEditorInput editorInput = getTextEditor().getEditorInput();
		IDocument document = getDocument();
		int rulerLine = getVerticalRulerInfo().getLineOfLastMouseButtonActivity();
		// create breakpoint
	}

	protected void removeMarkers( List markers )
	{
		IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
		try
		{
			Iterator e = markers.iterator();
			while( e.hasNext() )
			{
				IBreakpoint breakpoint = breakpointManager.getBreakpoint( (IMarker)e.next() );
				breakpointManager.removeBreakpoint( breakpoint, true );
			}
		}
		catch( CoreException e )
		{
//			CDebugUIPlugin.errorDialog( ActionMessages.getString("ManageBreakpointRulerAction.error.removing.message1"), e); //$NON-NLS-1$
		}
	}
}
