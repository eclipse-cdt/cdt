/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui;

import java.text.MessageFormat;
import java.util.HashMap;

import org.eclipse.cdt.debug.core.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.ICBreakpoint;
import org.eclipse.cdt.debug.core.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.IStackFrameInfo;
import org.eclipse.cdt.debug.core.IState;
import org.eclipse.cdt.debug.core.cdi.ICDIExitInfo;
import org.eclipse.cdt.debug.core.cdi.ICDISignal;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * 
 * Responsible for providing labels, images, and editors associated 
 * with debug elements in the CDT debug model.
 * 
 * @since Jul 22, 2002
 */
public class CDTDebugModelPresentation extends LabelProvider
									   implements IDebugModelPresentation
{
	/**
	 * Qualified names presentation property (value <code>"org.eclipse.debug.ui.displayQualifiedNames"</code>).
	 * When <code>DISPLAY_QUALIFIED_NAMES</code> is set to <code>True</code>,
	 * this label provider should use fully qualified type names when rendering elements.
	 * When set to <code>False</code>,this label provider should use simple names
	 * when rendering elements.
	 * @see #setAttribute(String, Object)
	 */
	public final static String DISPLAY_QUALIFIED_NAMES = "DISPLAY_QUALIFIED_NAMES"; //$NON-NLS-1$
	
	protected HashMap fAttributes = new HashMap(3);

	protected CDebugImageDescriptorRegistry fDebugImageRegistry = CDebugUIPlugin.getImageDescriptorRegistry();

	private static CDTDebugModelPresentation fInstance = null;

	/**
	 * Constructor for CDTDebugModelPresentation.
	 */
	public CDTDebugModelPresentation()
	{
		super();
		fInstance = this;
	}

	public static CDTDebugModelPresentation getDefault()
	{
		return fInstance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#setAttribute(String, Object)
	 */
	public void setAttribute( String attribute, Object value )
	{
		if ( value != null )
		{
			fAttributes.put( attribute, value );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#computeDetail(IValue, IValueDetailListener)
	 */
	public void computeDetail( IValue value, IValueDetailListener listener )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(Object)
	 */
	public IEditorInput getEditorInput( Object element )
	{
		IFile file = null;
		if ( element instanceof IMarker )
		{
			IResource resource = ((IMarker)element).getResource();
			if ( resource instanceof IFile )
				file = (IFile)resource; 
		}
		if ( element instanceof IFile )
			file = (IFile)element;
		if ( file != null ) 
			return new FileEditorInput( file );
/*
		if ( element instanceof IFileStorage )
		{
			return new FileStorageEditorInput( (IFileStorage)element );
		}
		if ( element instanceof IDisassemblyStorage )
		{
			return new DisassemblyEditorInput( (IStorage)element );
		}
*/
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(IEditorInput, Object)
	 */
	public String getEditorId( IEditorInput input, Object element )
	{
		if ( input != null )
		{
			IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
			IEditorDescriptor descriptor = registry.getDefaultEditor( input.getName() );
			if ( descriptor != null )
				return descriptor.getId();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILabelProvider#getImage(Object)
	 */
	public Image getImage( Object element )
	{
		try
		{
			if ( element instanceof IMarker ) 
			{
				IBreakpoint bp = getBreakpoint( (IMarker)element );
				if ( bp != null && bp instanceof ICBreakpoint ) 
				{
					return getBreakpointImage( (ICBreakpoint)bp );
				}
			}
			if ( element instanceof ICBreakpoint ) 
			{
				return getBreakpointImage( (ICBreakpoint)element );
			}
		}
		catch( CoreException e )
		{
		}
		return super.getImage( element );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILabelProvider#getText(Object)
	 */
	public String getText( Object element )
	{
		boolean showQualified= isShowQualifiedNames();
		StringBuffer label = new StringBuffer();
		try
		{
			if ( element instanceof IVariable )
			{
				label.append( getVariableText( (IVariable)element ) );
				return label.toString();
			}

			if ( element instanceof IStackFrame )
			{
				label.append( getStackFrameText( (IStackFrame)element, showQualified ) );
				return label.toString();
			}

			if ( element instanceof IMarker )
			{
				IBreakpoint breakpoint = getBreakpoint( (IMarker)element );
				if ( breakpoint != null )
				{
					return getBreakpointText( breakpoint, showQualified );
				}
				return null;
			}
			
			if ( element instanceof IBreakpoint )
			{
				return getBreakpointText( (IBreakpoint)element, showQualified );
			}
			
			if ( element instanceof IDebugTarget )
				label.append( getTargetText( (IDebugTarget)element, showQualified ) );
			else if ( element instanceof IThread )
				label.append( getThreadText( (IThread)element, showQualified ) );
			
			if ( element instanceof ITerminate )
			{
				if ( ((ITerminate)element).isTerminated() )
				{
					label.insert( 0, "<terminated>" );
					return label.toString();
				}
			}
			if ( element instanceof IDisconnect )
			{
				if ( ((IDisconnect)element).isDisconnected() )
				{
					label.insert( 0, "<disconnected>" );
					return label.toString();
				}
			}

			if ( label.length() > 0 )
			{
				return label.toString();
			}
		}
		catch( DebugException e )
		{		
			return "<not_responding>";
		}
		catch( CoreException e )
		{
			CDebugUIPlugin.log( e );
		}

		return null;
	}

	protected boolean isShowQualifiedNames()
	{
		Boolean showQualified = (Boolean)fAttributes.get( DISPLAY_QUALIFIED_NAMES );
		showQualified = showQualified == null ? Boolean.FALSE : showQualified;
		return showQualified.booleanValue();
	}

	protected boolean isShowVariableTypeNames()
	{
		Boolean show = (Boolean)fAttributes.get( DISPLAY_VARIABLE_TYPE_NAMES );
		show = show == null ? Boolean.FALSE : show;
		return show.booleanValue();
	}
	
	protected String getTargetText( IDebugTarget target, boolean qualified ) throws DebugException
	{
		if ( target instanceof IState )
		{
			IState state = (IState)target;
			switch( state.getCurrentStateId() )
			{
				case IState.EXITED:
				{
					Object info = state.getCurrentStateInfo();
					String label = target.getName() + " (Exited";
					if ( info != null && info instanceof ICDIExitInfo )
					{
						label += ". Exit code = " + ((ICDIExitInfo)info).getCode();
					}
					return label + ")";
				}
				case IState.SUSPENDED:
				{
					Object info = state.getCurrentStateInfo();
					if ( info != null && info instanceof ICDISignal )
					{
						String label = target.getName() + 
									   MessageFormat.format( " (Signal \'{0}\' received. Meaning: {1})", 
									   						 new String[] { ((ICDISignal)info).getName(), ((ICDISignal)info).getMeaning() } );
						return label;
					}
				}
			}
		}
		return target.getName();
	}
	
	protected String getThreadText( IThread thread, boolean qualified ) throws DebugException
	{
		if ( thread.isTerminated() )
		{
			return getFormattedString( "Thread [{0}] (Terminated)", thread.getName() );
		}
		if ( thread.isStepping() )
		{
			return getFormattedString( "Thread [{0}] (Stepping)", thread.getName());
		}
		if ( !thread.isSuspended() )
		{
			return getFormattedString( "Thread [{0}] (Running)", thread.getName() );
		}
		return getFormattedString( "Thread [{0}] (Suspended)", thread.getName() );
	}

	protected String getStackFrameText( IStackFrame stackFrame, boolean qualified ) throws DebugException 
	{
		IStackFrameInfo info = (IStackFrameInfo)stackFrame.getAdapter( IStackFrameInfo.class );
		if ( info != null )
		{
			String label = new String();
			label += info.getLevel() + " ";
			if ( info.getFunction() != null )
				label += info.getFunction() + "() ";

			if ( info.getFile() != null )
			{
				IPath path = new Path( info.getFile() );
				if ( !path.isEmpty() )
					label += "at " + ( qualified ? path.toOSString() : path.lastSegment() ) + ":";
			}
			if ( info.getFrameLineNumber() != 0 )
				label += info.getFrameLineNumber();
			return label;
		}
		return stackFrame.getName();
	}

	protected String getVariableText( IVariable var ) throws DebugException
	{
		// temporary
		String label = new String();
		if ( var != null )
		{
			label += var.getName();
			IValue value = var.getValue();
			label += "= " + value.getValueString();
		}
		return label;
	}

	/**
	 * Plug in the single argument to the resource String for the key to 
	 * get a formatted resource String.
	 * 
	 */
	public static String getFormattedString( String key, String arg )
	{
		return getFormattedString( key, new String[]{ arg } );
	}

	/**
	 * Plug in the arguments to the resource String for the key to get 
	 * a formatted resource String.
	 * 
	 */
	public static String getFormattedString(String string, String[] args)
	{
		return MessageFormat.format( string, args );
	}

	protected Image getBreakpointImage( ICBreakpoint breakpoint ) throws CoreException
	{
		if ( breakpoint instanceof ICLineBreakpoint )
		{
			return getLineBreakpointImage( (ICLineBreakpoint)breakpoint );
		}
		return null;
	}

	protected Image getLineBreakpointImage( ICLineBreakpoint breakpoint ) throws CoreException
	{
		int flags = computeBreakpointAdornmentFlags( breakpoint );
		CImageDescriptor descriptor = null;
		if ( breakpoint.isEnabled() )
		{
			descriptor = new CImageDescriptor( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_BREAKPOINT ),  flags );
		}
		else
		{
			descriptor = new CImageDescriptor( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED ),  flags );
		}
		return fDebugImageRegistry.get( descriptor );
	}

	protected IBreakpoint getBreakpoint( IMarker marker )
	{
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoint( marker );
	}

	protected String getBreakpointText( IBreakpoint breakpoint, boolean qualified ) throws CoreException
	{

		if ( breakpoint instanceof ICLineBreakpoint )
		{
			return getLineBreakpointText( (ICLineBreakpoint)breakpoint, qualified );
		}
		if ( breakpoint instanceof ICAddressBreakpoint )
		{
			return getAddressBreakpointText( (ICAddressBreakpoint)breakpoint, qualified );
		}
		if ( breakpoint instanceof ICFunctionBreakpoint )
		{
			return getFunctionBreakpointText( (ICFunctionBreakpoint)breakpoint, qualified );
		}
		return ""; //$NON-NLS-1$
	}

	protected String getLineBreakpointText( ICLineBreakpoint breakpoint, boolean qualified ) throws CoreException
	{
		StringBuffer label = new StringBuffer();
		appendResourceName( breakpoint, label, qualified );
		appendLineNumber( breakpoint, label );
		appendIgnoreCount( breakpoint, label );
		appendCondition( breakpoint, label );
		return label.toString();
	}

	protected String getAddressBreakpointText( ICAddressBreakpoint breakpoint, boolean qualified ) throws CoreException
	{
		return null;
	}

	protected String getFunctionBreakpointText( ICFunctionBreakpoint breakpoint, boolean qualified ) throws CoreException
	{
		return null;
	}

	protected StringBuffer appendResourceName( ICLineBreakpoint breakpoint, StringBuffer label, boolean qualified ) throws CoreException
	{
		IPath path = breakpoint.getMarker().getResource().getLocation();
		if ( !path.isEmpty() )
			label.append( qualified ? path.toOSString() : path.lastSegment() );
		return label;
}
	
	protected StringBuffer appendLineNumber( ICLineBreakpoint breakpoint, StringBuffer label ) throws CoreException
	{
		int lineNumber = breakpoint.getLineNumber();
		if ( lineNumber > 0 )
		{
			label.append( " [" ); //$NON-NLS-1$
			label.append( "line:" );
			label.append( ' ' );
			label.append( lineNumber );
			label.append( ']' );
		}
		return label;
	}

	protected StringBuffer appendIgnoreCount( ICBreakpoint breakpoint, StringBuffer label ) throws CoreException
	{
		int ignoreCount = breakpoint.getIgnoreCount();
		if ( ignoreCount > 0 )
		{
			label.append( " [" ); //$NON-NLS-1$
			label.append( "ignore count:" ); //$NON-NLS-1$
			label.append( ' ' );
			label.append( ignoreCount );
			label.append( ']' );
		}
		return label;
	}

	protected void appendCondition( ICLineBreakpoint breakpoint, StringBuffer buffer ) throws CoreException
	{
		String condition = breakpoint.getCondition();
		if ( condition != null && condition.length() > 0 )
		{
			buffer.append( " if " ); 
			buffer.append( condition );
		}
	}

	/**
	 * Returns the adornment flags for the given breakpoint.
	 * These flags are used to render appropriate overlay
	 * icons for the breakpoint.
	 */
	private int computeBreakpointAdornmentFlags( ICBreakpoint breakpoint )
	{
		int flags = 0;
		try
		{
			if ( breakpoint.isEnabled() )
			{
				flags |= CImageDescriptor.ENABLED;
			}
			if ( breakpoint.isInstalled() )
			{
				flags |= CImageDescriptor.INSTALLED;
			}
		}
		catch( CoreException e )
		{
			CDebugUIPlugin.log( e );
		}
		return flags;
	}
}
