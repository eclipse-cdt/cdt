/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui;

import java.text.MessageFormat;
import java.util.HashMap;

import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit;
import org.eclipse.cdt.debug.core.cdi.ICDIExitInfo;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryEvent;
import org.eclipse.cdt.debug.core.cdi.ICDISignalExitInfo;
import org.eclipse.cdt.debug.core.cdi.ICDISignalReceived;
import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointScope;
import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointTrigger;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDebugElementErrorStatus;
import org.eclipse.cdt.debug.core.model.ICDebugTargetType;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.cdt.debug.core.model.ICVariable;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.core.model.IDummyStackFrame;
import org.eclipse.cdt.debug.core.model.IExecFileInfo;
import org.eclipse.cdt.debug.core.model.IStackFrameInfo;
import org.eclipse.cdt.debug.core.model.IState;
import org.eclipse.cdt.debug.core.sourcelookup.IDisassemblyStorage;
import org.eclipse.cdt.debug.internal.core.sourcelookup.DisassemblyManager;
import org.eclipse.cdt.debug.internal.ui.editors.CDebugEditor;
import org.eclipse.cdt.debug.internal.ui.editors.DisassemblyEditorInput;
import org.eclipse.cdt.debug.internal.ui.editors.EditorInputDelegate;
import org.eclipse.cdt.debug.internal.ui.editors.FileNotFoundElement;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.resource.ImageDescriptor;
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
	public final static String DISPLAY_FULL_PATHS = "DISPLAY_FULL_PATHS"; //$NON-NLS-1$

	private static final String DUMMY_STACKFRAME_LABEL = "..."; 
	
	protected HashMap fAttributes = new HashMap(3);

	protected CDebugImageDescriptorRegistry fDebugImageRegistry = CDebugUIPlugin.getImageDescriptorRegistry();

	private static CDTDebugModelPresentation fInstance = null;

	private OverlayImageCache fImageCache = new OverlayImageCache();

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
		CDTValueDetailProvider.getDefault().computeDetail( value, listener );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(Object)
	 */
	public IEditorInput getEditorInput( Object element )
	{
		if ( element instanceof IMarker )
		{
			IResource resource = ((IMarker)element).getResource();
			if ( resource instanceof IFile )
				return new FileEditorInput( (IFile)resource ); 
		}
		if ( element instanceof IFile )
		{
			return new FileEditorInput( (IFile)element );
		}
		if ( element instanceof ICAddressBreakpoint )
		{
			return getDisassemblyEditorInput( (ICAddressBreakpoint)element );
		}
		if ( element instanceof ICLineBreakpoint )
		{
			IFile file = (IFile)((ICLineBreakpoint)element).getMarker().getResource().getAdapter( IFile.class );
			if ( file != null )
				return new FileEditorInput( file );
		}
		if ( element instanceof FileStorage )
		{
			return new ExternalEditorInput( (IStorage)element );
		}
		if ( element instanceof IDisassemblyStorage )
		{
			return new DisassemblyEditorInput( (IStorage)element );
		}
		if ( element instanceof FileNotFoundElement )
		{
			return new EditorInputDelegate( (FileNotFoundElement)element );
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(IEditorInput, Object)
	 */
	public String getEditorId( IEditorInput input, Object element )
	{
		if ( input instanceof EditorInputDelegate )
		{
			if ( ((EditorInputDelegate)input).getDelegate() == null )
				return CDebugEditor.EDITOR_ID;
			else
				return getEditorId( ((EditorInputDelegate)input).getDelegate(), element );
		}

		String id = null;
		if ( input != null )
		{
			IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
			IEditorDescriptor descriptor = registry.getDefaultEditor( input.getName() );
			id = ( descriptor != null ) ? descriptor.getId() : CUIPlugin.EDITOR_ID;
		}
		if ( CUIPlugin.EDITOR_ID.equals( id ) )
		{
			return CDebugEditor.EDITOR_ID;
		}
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILabelProvider#getImage(Object)
	 */
	public Image getImage( Object element )
	{
		Image baseImage = getBaseImage( element );
		if ( baseImage != null )
		{
			ImageDescriptor[] overlays = new ImageDescriptor[] { null, null, null, null };
			if ( element instanceof ICDebugElementErrorStatus && !((ICDebugElementErrorStatus)element).isOK() )
			{
				switch( ((ICDebugElementErrorStatus)element).getSeverity() )
				{
					case ICDebugElementErrorStatus.WARNING:
						overlays[OverlayImageDescriptor.BOTTOM_LEFT] = CDebugImages.DESC_OVRS_WARNING;
						break;
					case ICDebugElementErrorStatus.ERROR:
						overlays[OverlayImageDescriptor.BOTTOM_LEFT] = CDebugImages.DESC_OVRS_ERROR;
						break;
				}
			}
			if ( element instanceof ICVariable && ((ICVariable)element).isArgument() )
				overlays[OverlayImageDescriptor.TOP_RIGHT] = CDebugImages.DESC_OVRS_ARGUMENT;
			
			return fImageCache.getImageFor( new OverlayImageDescriptor( baseImage, overlays ) );
		}
		return null;
	}

	private Image getBaseImage( Object element )
	{
		if ( element instanceof IDebugTarget )
		{
			ICDebugTargetType targetType = (ICDebugTargetType)((IDebugTarget)element).getAdapter( ICDebugTargetType.class );
			int type = ( targetType != null ) ? targetType.getTargetType() : ICDebugTargetType.TARGET_TYPE_UNKNOWN;
			if ( type == ICDebugTargetType.TARGET_TYPE_LOCAL_CORE_DUMP )
			{
				return fDebugImageRegistry.get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_DEBUG_TARGET_TERMINATED ) );
			}
			IDebugTarget target = (IDebugTarget)element;
			if ( target.isTerminated() || target.isDisconnected() )
			{
				return fDebugImageRegistry.get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_DEBUG_TARGET_TERMINATED ) );
			}
			else
			{
				return fDebugImageRegistry.get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_DEBUG_TARGET ) );
			}
		}
		if ( element instanceof IThread )
		{
			ICDebugTargetType targetType = (ICDebugTargetType)((IThread)element).getDebugTarget().getAdapter( ICDebugTargetType.class );
			int type = ( targetType != null ) ? targetType.getTargetType() : ICDebugTargetType.TARGET_TYPE_UNKNOWN;
			if ( type == ICDebugTargetType.TARGET_TYPE_LOCAL_CORE_DUMP )
			{
				return fDebugImageRegistry.get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_THREAD_TERMINATED ) );
			}
			IThread thread = (IThread)element;
			if ( thread.isSuspended() )
			{
				return fDebugImageRegistry.get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED ) );
			}
			else if (thread.isTerminated())
			{
				return fDebugImageRegistry.get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_THREAD_TERMINATED ) );
			}
			else
			{
				return fDebugImageRegistry.get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_THREAD_RUNNING ) );
			}
		}
		
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
			if ( element instanceof IRegisterGroup ) 
			{
				return getRegisterGroupImage( (IRegisterGroup)element );
			}
			if ( element instanceof IExpression ) 
			{
				return getExpressionImage( (IExpression)element );
			}
			if ( element instanceof IRegister ) 
			{
				return getRegisterImage( (IRegister)element );
			}
			if ( element instanceof IVariable ) 
			{
				return getVariableImage( (IVariable)element );
			}
			if ( element instanceof ICSharedLibrary )
			{
				return getSharedLibraryImage( (ICSharedLibrary)element );
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
		StringBuffer baseText = new StringBuffer( getBaseText( element ) );
		if ( element instanceof ICDebugElementErrorStatus && !((ICDebugElementErrorStatus)element).isOK() )
		{
			baseText.append( getFormattedString( " <{0}>", ((ICDebugElementErrorStatus)element).getMessage() ) );
		}
		return baseText.toString();
	}

	private String getBaseText( Object element )
	{
		boolean showQualified= isShowQualifiedNames();
		StringBuffer label = new StringBuffer();
		try
		{
			if ( element instanceof ICSharedLibrary )
			{
				label.append( getSharedLibraryText( (ICSharedLibrary)element, showQualified ) );
				return label.toString();
			}
			
			if ( element instanceof IRegisterGroup )
			{
				label.append( ((IRegisterGroup)element).getName() );
				return label.toString();
			}

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
		Boolean showQualified = (Boolean)fAttributes.get( DISPLAY_FULL_PATHS );
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
					if ( info != null && info instanceof ICDISignalExitInfo)
					{
						ICDISignalExitInfo sigInfo = (ICDISignalExitInfo)info;
						label += MessageFormat.format( ": Signal ''{0}'' received. Description: {1}.", 
											  new String[] { sigInfo.getName(), sigInfo.getDescription() } );						
					}
					else if ( info != null && info instanceof ICDIExitInfo )
					{
						label += ". Exit code = " + ((ICDIExitInfo)info).getCode();
					}
					return label + ")";
				}
				case IState.SUSPENDED:
				{
					Object info = state.getCurrentStateInfo();
					if ( info != null && info instanceof ICDISignalReceived )
					{
						ICDISignal signal = ((ICDISignalReceived)info).getSignal();
						String label = target.getName() + 
									   MessageFormat.format( " (Signal ''{0}'' received. Description: {1})", 
									   						 new String[] { signal.getName(), signal.getDescription() } );
						return label;
					}
					if ( info != null && info instanceof ICDIWatchpointTrigger )
					{
						String label = target.getName() + 
									   MessageFormat.format( " (Watchpoint triggered. Old value: ''{0}''. New value: ''{1}'')", 
									   						 new String[] { ((ICDIWatchpointTrigger)info).getOldValue(), 
									   						 				((ICDIWatchpointTrigger)info).getNewValue() } );
						return label;
					}
					if ( info != null && info instanceof ICDIWatchpointScope )
					{
						return target.getName() + " (Watchpoint is out of scope)";
					}
					if ( info != null && info instanceof ICDIBreakpointHit )
					{
						return target.getName() + " (Breakpoint hit)";
					}
					if ( info != null && info instanceof ICDISharedLibraryEvent )
					{
						return target.getName() + " (Stopped on shared library event)";
					}
					if ( info != null && info instanceof ICDISession )
					{
						return target.getName() + " (Suspended)";
					}
				}
			}
		}
		return target.getName();
	}
	
	protected String getThreadText( IThread thread, boolean qualified ) throws DebugException
	{
		ICDebugTargetType targetType = (ICDebugTargetType)thread.getDebugTarget().getAdapter( ICDebugTargetType.class );
		int type = ( targetType != null ) ? targetType.getTargetType() : ICDebugTargetType.TARGET_TYPE_UNKNOWN;
		if ( type == ICDebugTargetType.TARGET_TYPE_LOCAL_CORE_DUMP )
		{
			return getFormattedString( "Thread [{0}]", thread.getName() );
		}
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
			StringBuffer label = new StringBuffer();
			label.append( info.getLevel() );
			label.append( ' ' );
			if ( info.getFunction() != null )
			{
				String function = info.getFunction().trim();
				if ( function.length() > 0 )
				{
					label.append( function );
					label.append( "() " );
					if ( info.getFile() != null )
					{
						IPath path = new Path( info.getFile() );
						if ( !path.isEmpty() )
						{
							label.append( "at " );
							label.append( ( qualified ? path.toOSString() : path.lastSegment() ) );
							label.append( ":" );
							if ( info.getFrameLineNumber() != 0 )
								label.append( info.getFrameLineNumber() );
						}
					}
				}
			}
			else
				label.append( "<symbol is not available>" );
			return label.toString();
		}
		return ( stackFrame.getAdapter( IDummyStackFrame.class ) != null ) ? 
				getDummyStackFrameLabel( stackFrame ) : stackFrame.getName();
	}

	private String getDummyStackFrameLabel( IStackFrame stackFrame )
	{
		return DUMMY_STACKFRAME_LABEL;
	}

	protected String getVariableText( IVariable var ) throws DebugException
	{
		StringBuffer label = new StringBuffer();
		if ( var instanceof ICVariable )
		{
			ICType type = null;
			try
			{
				type = ((ICVariable)var).getType();
			}
			catch( DebugException e )
			{
				// don't display type
			}
			if ( type != null && isShowVariableTypeNames() )
			{
				String typeName = getVariableTypeName( type );
				if ( typeName != null && typeName.length() > 0 )
				{
					label.append( typeName );
					if ( type.isArray() )
					{
						int[] dims = type.getArrayDimensions();
						for ( int i = 0; i < dims.length; ++i )
						{
							label.append( '[' );					
							label.append( dims[i] );					
							label.append( ']' );					
						}
					}
					label.append( ' ' );
				}
			}
			if ( !((ICVariable)var).isEnabled() )
				label.append( "<disabled> " );
			String name = var.getName();
			if ( name != null )
				label.append( name.trim() );
			IValue value = var.getValue();
			if ( value instanceof ICValue && value.getValueString() != null )
			{
				String valueString = value.getValueString().trim();
				if ( type != null && type.isCharacter() )
				{
					if ( valueString.length() == 0 )
						valueString = ".";
					label.append( "= " );
					label.append( valueString );
				}
				else if ( type != null && type.isFloatingPointType() )
				{
					Number floatingPointValue = CDebugUtils.getFloatingPointValue( (ICValue)value );
					if ( CDebugUtils.isNaN( floatingPointValue ) )
						valueString = "NAN";
					if ( CDebugUtils.isPositiveInfinity( floatingPointValue ) )
						valueString = "Infinity";
					if ( CDebugUtils.isNegativeInfinity( floatingPointValue ) )
						valueString = "-Infinity";
					label.append( "= " );
					label.append( valueString );
				}
				else if ( type == null || ( !type.isArray() && !type.isStructure() ) )
				{
					if ( valueString.length() > 0 )
					{
						label.append( "= " );
						label.append( valueString );
					}
				}
			}
		}
		return label.toString();
	}

	protected String getSharedLibraryText( ICSharedLibrary library, boolean qualified ) throws DebugException
	{
		String label = new String();
		IPath path = new Path( library.getFileName() );
		if ( !path.isEmpty() )
			label += ( qualified ? path.toOSString() : path.lastSegment() );
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
		if ( breakpoint instanceof ICWatchpoint )
		{
			return getWatchpointImage( (ICWatchpoint)breakpoint );
		}
		return null;
	}

	protected Image getLineBreakpointImage( ICLineBreakpoint breakpoint ) throws CoreException
	{
		ImageDescriptor descriptor = null;
		if ( breakpoint.isEnabled() )
		{
			descriptor = DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_BREAKPOINT );
		}
		else
		{
			descriptor = DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED );
		}
		;
		return fImageCache.getImageFor( new OverlayImageDescriptor( fDebugImageRegistry.get( descriptor ), computeBreakpointOverlays( breakpoint ) ) );
	}

	protected Image getWatchpointImage( ICWatchpoint watchpoint ) throws CoreException
	{
		ImageDescriptor descriptor = null;
		if ( watchpoint.isEnabled() )
		{
			if ( watchpoint.isReadType() && !watchpoint.isWriteType() )
				descriptor = CDebugImages.DESC_OBJS_READ_WATCHPOINT_ENABLED;
			else if ( !watchpoint.isReadType() && watchpoint.isWriteType() )
				descriptor = CDebugImages.DESC_OBJS_WRITE_WATCHPOINT_ENABLED;
			else
				descriptor = CDebugImages.DESC_OBJS_WATCHPOINT_ENABLED;
		}
		else
		{
			if ( watchpoint.isReadType() && !watchpoint.isWriteType() )
				descriptor = CDebugImages.DESC_OBJS_READ_WATCHPOINT_DISABLED;
			else if ( !watchpoint.isReadType() && watchpoint.isWriteType() )
				descriptor = CDebugImages.DESC_OBJS_WRITE_WATCHPOINT_DISABLED;
			else
				descriptor = CDebugImages.DESC_OBJS_WATCHPOINT_DISABLED;
		}
		return fImageCache.getImageFor( new OverlayImageDescriptor( fDebugImageRegistry.get( descriptor ), computeBreakpointOverlays( watchpoint ) ) );
	}

	protected IBreakpoint getBreakpoint( IMarker marker )
	{
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoint( marker );
	}

	protected String getBreakpointText( IBreakpoint breakpoint, boolean qualified ) throws CoreException
	{
		if ( breakpoint instanceof ICAddressBreakpoint )
		{
			return getAddressBreakpointText( (ICAddressBreakpoint)breakpoint, qualified );
		}
		if ( breakpoint instanceof ICFunctionBreakpoint )
		{
			return getFunctionBreakpointText( (ICFunctionBreakpoint)breakpoint, qualified );
		}
		if ( breakpoint instanceof ICLineBreakpoint )
		{
			return getLineBreakpointText( (ICLineBreakpoint)breakpoint, qualified );
		}
		if ( breakpoint instanceof ICWatchpoint )
		{
			return getWatchpointText( (ICWatchpoint)breakpoint, qualified );
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

	protected String getWatchpointText( ICWatchpoint watchpoint, boolean qualified ) throws CoreException
	{
		StringBuffer label = new StringBuffer();
		appendResourceName( watchpoint, label, qualified );
		appendWatchExpression( watchpoint, label );
		appendIgnoreCount( watchpoint, label );
		appendCondition( watchpoint, label );
		return label.toString();
	}

	protected String getAddressBreakpointText( ICAddressBreakpoint breakpoint, boolean qualified ) throws CoreException
	{
		StringBuffer label = new StringBuffer();
		appendResourceName( breakpoint, label, qualified );
		appendAddress( breakpoint, label );
		appendIgnoreCount( breakpoint, label );
		appendCondition( breakpoint, label );
		return label.toString();
	}

	protected String getFunctionBreakpointText( ICFunctionBreakpoint breakpoint, boolean qualified ) throws CoreException
	{
		StringBuffer label = new StringBuffer();
		appendResourceName( breakpoint, label, qualified );
		appendFunction( breakpoint, label );
		appendIgnoreCount( breakpoint, label );
		appendCondition( breakpoint, label );
		return label.toString();
	}

	protected StringBuffer appendResourceName( ICBreakpoint breakpoint, StringBuffer label, boolean qualified ) throws CoreException
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

	protected StringBuffer appendAddress( ICAddressBreakpoint breakpoint, StringBuffer label ) throws CoreException
	{
		try
		{
			long address = Long.parseLong( breakpoint.getAddress() );
			label.append( " [address: " );
			label.append( CDebugUtils.toHexAddressString( address ) );
			label.append( ']' );
		}
		catch( NumberFormatException e )
		{
		}
		return label;
	}

	protected StringBuffer appendFunction( ICFunctionBreakpoint breakpoint, StringBuffer label ) throws CoreException
	{
		String function = breakpoint.getFunction();
		if ( function != null && function.trim().length() > 0 )
		{
			label.append( " [" ); //$NON-NLS-1$
			label.append( "function:" );
			label.append( ' ' );
			label.append( function.trim() );
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

	protected void appendCondition( ICBreakpoint breakpoint, StringBuffer buffer ) throws CoreException
	{
		String condition = breakpoint.getCondition();
		if ( condition != null && condition.length() > 0 )
		{
			buffer.append( " if " ); 
			buffer.append( condition );
		}
	}

	private void appendWatchExpression( ICWatchpoint watchpoint, StringBuffer label ) throws CoreException
	{
		String expression = watchpoint.getExpression();
		if ( expression != null && expression.length() > 0 )
		{
			label.append( " at \'" ); 
			label.append( expression );
			label.append( '\'' );
		}
	}

	private ImageDescriptor[] computeBreakpointOverlays( ICBreakpoint breakpoint )
	{
		ImageDescriptor[] overlays = new ImageDescriptor[] { null, null, null, null };
		try
		{
			if ( breakpoint.isConditional() )
			{
				overlays[OverlayImageDescriptor.TOP_LEFT] = ( breakpoint.isEnabled() ) ? 
					CDebugImages.DESC_OVRS_BREAKPOINT_CONDITIONAL : CDebugImages.DESC_OVRS_BREAKPOINT_CONDITIONAL_DISABLED;
			}
			if ( breakpoint.isInstalled() )
			{
				overlays[OverlayImageDescriptor.BOTTOM_LEFT] = ( breakpoint.isEnabled() ) ? 
					CDebugImages.DESC_OVRS_BREAKPOINT_INSTALLED : CDebugImages.DESC_OVRS_BREAKPOINT_INSTALLED_DISABLED;
			}
			if ( breakpoint instanceof ICAddressBreakpoint )
			{
				overlays[OverlayImageDescriptor.TOP_RIGHT] = ( breakpoint.isEnabled() ) ? 
					CDebugImages.DESC_OVRS_ADDRESS_BREAKPOINT : CDebugImages.DESC_OVRS_ADDRESS_BREAKPOINT_DISABLED;
			}
			if ( breakpoint instanceof ICFunctionBreakpoint )
			{
				overlays[OverlayImageDescriptor.TOP_RIGHT] = ( breakpoint.isEnabled() ) ? 
					CDebugImages.DESC_OVRS_FUNCTION_BREAKPOINT : CDebugImages.DESC_OVRS_FUNCTION_BREAKPOINT_DISABLED;
			}
		}
		catch( CoreException e )
		{
			CDebugUIPlugin.log( e );
		}
		return overlays;
	}

	protected Image getVariableImage( IVariable element ) throws DebugException
	{
		if ( element instanceof ICVariable )
		{
			ICType type = null;
			try
			{
				type = ((ICVariable)element).getType();
			}
			catch( DebugException e )
			{
				// use default image
			}
			if ( type != null && (type.isPointer() || type.isReference()) )
				return fDebugImageRegistry.get( ( ((ICVariable)element).isEnabled() ) ?
							CDebugImages.DESC_OBJS_VARIABLE_POINTER : CDebugImages.DESC_OBJS_VARIABLE_POINTER_DISABLED );
			else if ( ((ICVariable)element).hasChildren() )
				return fDebugImageRegistry.get( ( ((ICVariable)element).isEnabled() ) ? 
							CDebugImages.DESC_OBJS_VARIABLE_AGGREGATE : CDebugImages.DESC_OBJS_VARIABLE_AGGREGATE_DISABLED );
			else
				return fDebugImageRegistry.get( ( ((ICVariable)element).isEnabled() ) ?
							CDebugImages.DESC_OBJS_VARIABLE_SIMPLE : CDebugImages.DESC_OBJS_VARIABLE_SIMPLE_DISABLED );
		}
		return null;
	}

	protected Image getRegisterGroupImage( IRegisterGroup element ) throws DebugException
	{
		return fDebugImageRegistry.get( CDebugImages.DESC_OBJS_REGISTER_GROUP );
	}

	protected Image getRegisterImage( IRegister element ) throws DebugException
	{
		return fDebugImageRegistry.get( CDebugImages.DESC_OBJS_REGISTER );
	}

	protected Image getExpressionImage( IExpression element ) throws DebugException
	{
		return fDebugImageRegistry.get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_EXPRESSION ) );
	}

	protected Image getSharedLibraryImage( ICSharedLibrary element ) throws DebugException
	{
		if ( element.areSymbolsLoaded() )
		{
			return fImageCache.getImageFor( new OverlayImageDescriptor( fDebugImageRegistry.get( CDebugImages.DESC_OBJS_LOADED_SHARED_LIBRARY ), 
											new ImageDescriptor[] { null, CDebugImages.DESC_OVRS_SYMBOLS, null, null } ) );
		}
		return CDebugUIPlugin.getImageDescriptorRegistry().get( CDebugImages.DESC_OBJS_SHARED_LIBRARY );
	}

	protected DisassemblyEditorInput getDisassemblyEditorInput( ICAddressBreakpoint breakpoint )
	{
		IDebugTarget[] targets = DebugPlugin.getDefault().getLaunchManager().getDebugTargets();
		for ( int i = 0; i < targets.length; ++i )
		{
			IResource resource = breakpoint.getMarker().getResource();
			if ( resource != null && resource instanceof IFile && 
				 targets[i].getAdapter( IExecFileInfo.class )!= null &&
				 ((IFile)resource).getLocation().toOSString().equals( ((IExecFileInfo)targets[i].getAdapter( IExecFileInfo.class )).getExecFile().getLocation().toOSString() ) )
			{
				if ( targets[i].getAdapter( DisassemblyManager.class ) != null )
				{
					try
					{
						long address = Long.parseLong( breakpoint.getAddress() );
						IStorage storage = (IStorage)(((DisassemblyManager)targets[i].getAdapter( DisassemblyManager.class )).getSourceElement( address ) );
						if ( storage != null )
							return new DisassemblyEditorInput( storage );
					}
					catch( NumberFormatException e )
					{
					}
					catch( CoreException e )
					{
					}
				}
			}
		}
		return null;
	}

	private String getVariableTypeName( ICType type )
	{
		String typeName = type.getName();
		if ( type.isArray() && typeName != null )
		{
			int index = typeName.indexOf( '[' );
			if ( index != -1 )
				return typeName.substring( 0, index ).trim();
		}
		return typeName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose()
	{
		fImageCache.disposeAll();
	}
}
