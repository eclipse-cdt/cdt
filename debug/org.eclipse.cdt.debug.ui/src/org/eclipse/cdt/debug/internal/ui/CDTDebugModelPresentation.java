/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import java.text.MessageFormat;
import java.util.HashMap;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit;
import org.eclipse.cdt.debug.core.cdi.ICDIExitInfo;
import org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryEvent;
import org.eclipse.cdt.debug.core.cdi.ICDISignalExitInfo;
import org.eclipse.cdt.debug.core.cdi.ICDISignalReceived;
import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointScope;
import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointTrigger;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.model.CDebugElementState;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.cdt.debug.core.model.ICDebugElementStatus;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICGlobalVariable;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.ICThread;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.cdt.debug.core.model.ICVariable;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.core.model.IDummyStackFrame;
import org.eclipse.cdt.debug.internal.ui.editors.CDebugEditor;
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
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
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
 * Responsible for providing labels, images, and editors associated with debug elements in the CDT debug model.
 */
public class CDTDebugModelPresentation extends LabelProvider implements IDebugModelPresentation {

	/**
	 * Qualified names presentation property (value <code>"org.eclipse.debug.ui.displayQualifiedNames"</code>). When <code>DISPLAY_QUALIFIED_NAMES</code>
	 * is set to <code>True</code>, this label provider should use fully qualified type names when rendering elements. When set to <code>False</code> ,this
	 * label provider should use simple names when rendering elements.
	 * 
	 * @see #setAttribute(String, Object)
	 */
	public final static String DISPLAY_FULL_PATHS = "DISPLAY_FULL_PATHS"; //$NON-NLS-1$

	private static final String DUMMY_STACKFRAME_LABEL = "..."; //$NON-NLS-1$

	protected HashMap fAttributes = new HashMap( 3 );

	protected CDebugImageDescriptorRegistry fDebugImageRegistry = CDebugUIPlugin.getImageDescriptorRegistry();

	private static CDTDebugModelPresentation fInstance = null;

	private OverlayImageCache fImageCache = new OverlayImageCache();

	/**
	 * Constructor for CDTDebugModelPresentation.
	 */
	public CDTDebugModelPresentation() {
		super();
		fInstance = this;
	}

	public static CDTDebugModelPresentation getDefault() {
		return fInstance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#setAttribute(String, Object)
	 */
	public void setAttribute( String attribute, Object value ) {
		if ( value != null ) {
			fAttributes.put( attribute, value );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#computeDetail(IValue, IValueDetailListener)
	 */
	public void computeDetail( IValue value, IValueDetailListener listener ) {
		CValueDetailProvider.getDefault().computeDetail( value, listener );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(Object)
	 */
	public IEditorInput getEditorInput( Object element ) {
		if ( element instanceof IMarker ) {
			IResource resource = ((IMarker)element).getResource();
			if ( resource instanceof IFile )
				return new FileEditorInput( (IFile)resource );
		}
		if ( element instanceof IFile ) {
			return new FileEditorInput( (IFile)element );
		}
		if ( element instanceof ICLineBreakpoint ) {
			IFile file = (IFile)((ICLineBreakpoint)element).getMarker().getResource().getAdapter( IFile.class );
			if ( file != null )
				return new FileEditorInput( file );
		}
		if ( element instanceof FileStorage ) {
			return new ExternalEditorInput( (IStorage)element );
		}
		if ( element instanceof FileNotFoundElement ) {
			return new EditorInputDelegate( (FileNotFoundElement)element );
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(IEditorInput, Object)
	 */
	public String getEditorId( IEditorInput input, Object element ) {
		if ( input instanceof EditorInputDelegate ) {
			if ( ((EditorInputDelegate)input).getDelegate() == null )
				return CDebugEditor.EDITOR_ID;
			return getEditorId( ((EditorInputDelegate)input).getDelegate(), element );
		}
		String id = null;
		if ( input != null ) {
			IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
			IEditorDescriptor descriptor = registry.getDefaultEditor( input.getName() );
			id = (descriptor != null) ? descriptor.getId() : CUIPlugin.EDITOR_ID;
		}
		if ( CUIPlugin.EDITOR_ID.equals( id ) ) {
			return CDebugEditor.EDITOR_ID;
		}
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILabelProvider#getImage(Object)
	 */
	public Image getImage( Object element ) {
		Image baseImage = getBaseImage( element );
		if ( baseImage != null ) {
			ImageDescriptor[] overlays = new ImageDescriptor[]{ null, null, null, null };
			if ( element instanceof ICDebugElementStatus && !((ICDebugElementStatus)element).isOK() ) {
				switch( ((ICDebugElementStatus)element).getSeverity() ) {
					case ICDebugElementStatus.WARNING:
						overlays[OverlayImageDescriptor.BOTTOM_LEFT] = CDebugImages.DESC_OVRS_WARNING;
						break;
					case ICDebugElementStatus.ERROR:
						overlays[OverlayImageDescriptor.BOTTOM_LEFT] = CDebugImages.DESC_OVRS_ERROR;
						break;
				}
			}
			if ( element instanceof IWatchExpression && ((IWatchExpression)element).hasErrors() )
				overlays[OverlayImageDescriptor.BOTTOM_LEFT] = CDebugImages.DESC_OVRS_ERROR;
			if ( element instanceof ICVariable && ((ICVariable)element).isArgument() )
				overlays[OverlayImageDescriptor.TOP_RIGHT] = CDebugImages.DESC_OVRS_ARGUMENT;
			if ( element instanceof ICGlobalVariable && !(element instanceof IRegister) )
				overlays[OverlayImageDescriptor.TOP_RIGHT] = CDebugImages.DESC_OVRS_GLOBAL;
			return fImageCache.getImageFor( new OverlayImageDescriptor( baseImage, overlays ) );
		}
		return null;
	}

	private Image getBaseImage( Object element ) {
		if ( element instanceof ICDebugTarget ) {
			ICDebugTarget target = (ICDebugTarget)element;
			if ( target.isPostMortem() ) {
				return fDebugImageRegistry.get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_DEBUG_TARGET_TERMINATED ) );
			}
			if ( target.isTerminated() || target.isDisconnected() ) {
				return fDebugImageRegistry.get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_DEBUG_TARGET_TERMINATED ) );
			}
			return fDebugImageRegistry.get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_DEBUG_TARGET ) );
		}
		if ( element instanceof ICThread ) {
			ICThread thread = (ICThread)element;
			ICDebugTarget target = (ICDebugTarget)thread.getDebugTarget();
			if ( target.isPostMortem() ) {
				return fDebugImageRegistry.get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_THREAD_TERMINATED ) );
			}
			if ( thread.isSuspended() ) {
				return fDebugImageRegistry.get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED ) );
			}
			else if ( thread.isTerminated() ) {
				return fDebugImageRegistry.get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_THREAD_TERMINATED ) );
			}
			else {
				return fDebugImageRegistry.get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_THREAD_RUNNING ) );
			}
		}
		try {
			if ( element instanceof IMarker ) {
				IBreakpoint bp = getBreakpoint( (IMarker)element );
				if ( bp != null && bp instanceof ICBreakpoint ) {
					return getBreakpointImage( (ICBreakpoint)bp );
				}
			}
			if ( element instanceof ICBreakpoint ) {
				return getBreakpointImage( (ICBreakpoint)element );
			}
			if ( element instanceof IRegisterGroup ) {
				return getRegisterGroupImage( (IRegisterGroup)element );
			}
			if ( element instanceof IExpression ) {
				return getExpressionImage( (IExpression)element );
			}
			if ( element instanceof IRegister ) {
				return getRegisterImage( (IRegister)element );
			}
			if ( element instanceof IVariable ) {
				return getVariableImage( (IVariable)element );
			}
			if ( element instanceof ICSharedLibrary ) {
				return getSharedLibraryImage( (ICSharedLibrary)element );
			}
		}
		catch( CoreException e ) {
		}
		return super.getImage( element );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILabelProvider#getText(Object)
	 */
	public String getText( Object element ) {
		StringBuffer baseText = new StringBuffer( getBaseText( element ) );
		if ( element instanceof ICDebugElementStatus && !((ICDebugElementStatus)element).isOK() ) {
			baseText.append( getFormattedString( " <{0}>", ((ICDebugElementStatus)element).getMessage() ) ); //$NON-NLS-1$
		}
		return baseText.toString();
	}

	private String getBaseText( Object element ) {
		boolean showQualified = isShowQualifiedNames();
		StringBuffer label = new StringBuffer();
		try {
			if ( element instanceof ICSharedLibrary ) {
				label.append( getSharedLibraryText( (ICSharedLibrary)element, showQualified ) );
				return label.toString();
			}
			if ( element instanceof IRegisterGroup ) {
				label.append( ((IRegisterGroup)element).getName() );
				return label.toString();
			}
			if ( element instanceof IWatchExpression ) {
				return getWatchExpressionText( (IWatchExpression)element );
			}
			if ( element instanceof IVariable ) {
				label.append( getVariableText( (IVariable)element ) );
				return label.toString();
			}
			if ( element instanceof IValue ) {
				label.append( getValueText( (IValue)element ) );
				return label.toString();
			}
			if ( element instanceof IStackFrame ) {
				label.append( getStackFrameText( (IStackFrame)element, showQualified ) );
				return label.toString();
			}
			if ( element instanceof IMarker ) {
				IBreakpoint breakpoint = getBreakpoint( (IMarker)element );
				if ( breakpoint != null ) {
					return getBreakpointText( breakpoint, showQualified );
				}
				return null;
			}
			if ( element instanceof IBreakpoint ) {
				return getBreakpointText( (IBreakpoint)element, showQualified );
			}
			if ( element instanceof IDebugTarget )
				label.append( getTargetText( (IDebugTarget)element, showQualified ) );
			else if ( element instanceof IThread )
				label.append( getThreadText( (IThread)element, showQualified ) );
			if ( element instanceof ITerminate ) {
				if ( ((ITerminate)element).isTerminated() ) {
					label.insert( 0, CDebugUIMessages.getString( "CDTDebugModelPresentation.0" ) ); //$NON-NLS-1$
					return label.toString();
				}
			}
			if ( element instanceof IDisconnect ) {
				if ( ((IDisconnect)element).isDisconnected() ) {
					label.insert( 0, CDebugUIMessages.getString( "CDTDebugModelPresentation.1" ) ); //$NON-NLS-1$
					return label.toString();
				}
			}
			if ( label.length() > 0 ) {
				return label.toString();
			}
		}
		catch( DebugException e ) {
			return MessageFormat.format( CDebugUIMessages.getString( "CDTDebugModelPresentation.2" ), new String[] { e.getMessage() } ); //$NON-NLS-1$
		}
		catch( CoreException e ) {
			CDebugUIPlugin.log( e );
		}
		return getDefaultText( element );
	}

	protected boolean isShowQualifiedNames() {
		Boolean showQualified = (Boolean)fAttributes.get( DISPLAY_FULL_PATHS );
		showQualified = showQualified == null ? Boolean.FALSE : showQualified;
		return showQualified.booleanValue();
	}

	protected boolean isShowVariableTypeNames() {
		Boolean show = (Boolean)fAttributes.get( DISPLAY_VARIABLE_TYPE_NAMES );
		show = show == null ? Boolean.FALSE : show;
		return show.booleanValue();
	}

	protected String getTargetText( IDebugTarget target, boolean qualified ) throws DebugException {
		ICDebugTarget t = (ICDebugTarget)target.getAdapter( ICDebugTarget.class );
		if ( t != null ) {
			if ( !t.isPostMortem() ) {
				CDebugElementState state = t.getState();
				if ( state.equals( CDebugElementState.EXITED ) ) {
					Object info = t.getCurrentStateInfo();
					String label = CDebugUIMessages.getString( "CDTDebugModelPresentation.3" ); //$NON-NLS-1$
					String reason = ""; //$NON-NLS-1$
					if ( info != null && info instanceof ICDISignalExitInfo ) {
						ICDISignalExitInfo sigInfo = (ICDISignalExitInfo)info;
						reason = ' ' + MessageFormat.format( CDebugUIMessages.getString( "CDTDebugModelPresentation.5" ), new String[]{ sigInfo.getName(), sigInfo.getDescription() } ); //$NON-NLS-1$
					}
					else if ( info != null && info instanceof ICDIExitInfo ) {
						reason = ' ' + MessageFormat.format( CDebugUIMessages.getString( "CDTDebugModelPresentation.6" ), new Integer[] { new Integer( ((ICDIExitInfo)info).getCode() ) } ); //$NON-NLS-1$
					}
					return MessageFormat.format( label, new String[] { target.getName(), reason } );
				}
				else if ( state.equals( CDebugElementState.SUSPENDED ) ) {
						return MessageFormat.format( CDebugUIMessages.getString( "CDTDebugModelPresentation.7" ), new String[] { target.getName() } ); //$NON-NLS-1$
				}
			}
		}
		return target.getName();
	}

	protected String getThreadText( IThread thread, boolean qualified ) throws DebugException {
		ICDebugTarget target = (ICDebugTarget)thread.getDebugTarget().getAdapter( ICDebugTarget.class );
		if ( target.isPostMortem() ) {
			return getFormattedString( CDebugUIMessages.getString( "CDTDebugModelPresentation.8" ), thread.getName() ); //$NON-NLS-1$
		}
		if ( thread.isTerminated() ) {
			return getFormattedString( CDebugUIMessages.getString( "CDTDebugModelPresentation.9" ), thread.getName() ); //$NON-NLS-1$
		}
		if ( thread.isStepping() ) {
			return getFormattedString( CDebugUIMessages.getString( "CDTDebugModelPresentation.10" ), thread.getName() ); //$NON-NLS-1$
		}
		if ( !thread.isSuspended() ) {
			return getFormattedString( CDebugUIMessages.getString( "CDTDebugModelPresentation.11" ), thread.getName() ); //$NON-NLS-1$
		}
		if ( thread.isSuspended() ) {
			String reason = ""; //$NON-NLS-1$
			ICDebugElement element = (ICDebugElement)thread.getAdapter( ICDebugElement.class );
			if ( element != null ) {
				Object info = element.getCurrentStateInfo();
				if ( info != null && info instanceof ICDISignalReceived ) {
					ICDISignal signal = ((ICDISignalReceived)info).getSignal();
					reason = MessageFormat.format( CDebugUIMessages.getString( "CDTDebugModelPresentation.13" ), new String[]{ signal.getName(), signal.getDescription() } ); //$NON-NLS-1$
				}
				else if ( info != null && info instanceof ICDIWatchpointTrigger ) {
					reason = MessageFormat.format( CDebugUIMessages.getString( "CDTDebugModelPresentation.14" ), new String[]{ ((ICDIWatchpointTrigger)info).getOldValue(), ((ICDIWatchpointTrigger)info).getNewValue() } ); //$NON-NLS-1$
				}
				else if ( info != null && info instanceof ICDIWatchpointScope ) {
					reason = CDebugUIMessages.getString( "CDTDebugModelPresentation.15" ); //$NON-NLS-1$
				}
				else if ( info != null && info instanceof ICDIBreakpointHit ) {
					reason = CDebugUIMessages.getString( "CDTDebugModelPresentation.16" ); //$NON-NLS-1$
				}
				else if ( info != null && info instanceof ICDISharedLibraryEvent ) {
					reason = CDebugUIMessages.getString( "CDTDebugModelPresentation.17" ); //$NON-NLS-1$
				}
			}
			return MessageFormat.format( CDebugUIMessages.getString( "CDTDebugModelPresentation.18" ), new String[] { thread.getName(), reason } ); //$NON-NLS-1$
		}
		return MessageFormat.format( CDebugUIMessages.getString( "CDTDebugModelPresentation.19" ), new String[] { thread.getName() } ); //$NON-NLS-1$
	}

	protected String getStackFrameText( IStackFrame f, boolean qualified ) throws DebugException {
		if ( f instanceof ICStackFrame ) {
			ICStackFrame frame = (ICStackFrame)f;
			StringBuffer label = new StringBuffer();
			label.append( frame.getLevel() );
			label.append( ' ' );
			String function = frame.getFunction();
			if ( function != null ) {
				function = function.trim();
				if ( function.length() > 0 ) {
					label.append( function );
					label.append( "() " ); //$NON-NLS-1$
					if ( frame.getFile() != null ) {
						IPath path = new Path( frame.getFile() );
						if ( !path.isEmpty() ) {
							label.append( CDebugUIMessages.getString( "CDTDebugModelPresentation.20" ) ); //$NON-NLS-1$
							label.append( ' ' );
							label.append( (qualified ? path.toOSString() : path.lastSegment()) );
							label.append( ':' );
							if ( frame.getFrameLineNumber() != 0 )
								label.append( frame.getFrameLineNumber() );
						}
					}
				}
			}
			if ( isEmpty( function ) )
				label.append( CDebugUIMessages.getString( "CDTDebugModelPresentation.21" ) ); //$NON-NLS-1$
			return label.toString();
		}
		return (f.getAdapter( IDummyStackFrame.class ) != null) ? getDummyStackFrameLabel( f ) : f.getName();
	}

	private String getDummyStackFrameLabel( IStackFrame stackFrame ) {
		return DUMMY_STACKFRAME_LABEL;
	}

	protected String getWatchExpressionText( IWatchExpression expression ) {
		StringBuffer result = new StringBuffer();
		result.append( '"' ).append( expression.getExpressionText() ).append( '"' );
		if ( expression.isPending() ) {
			result.append( " = " ).append( "..." ); //$NON-NLS-1$//$NON-NLS-2$
		}
		else {
			IValue value = expression.getValue();
			if ( value instanceof ICValue ) {
				ICType type = null;
				try {
					type = ((ICValue)value).getType();
				}
				catch( DebugException e1 ) {
				}
				if ( type != null && isShowVariableTypeNames() ) {
					String typeName = getVariableTypeName( type );
					if ( !isEmpty( typeName ) ) {
						result.insert( 0, typeName + ' ' );
					}
				}
				String valueString = DebugUIPlugin.getModelPresentation().getText( value );
				if ( valueString.length() > 0 ) {
					result.append( " = " ).append( valueString ); //$NON-NLS-1$
				}
			}
		}
		if ( !expression.isEnabled() ) {
			result.append( ' ' );
			result.append( CDebugUIMessages.getString( "CDTDebugModelPresentation.22" ) ); //$NON-NLS-1$
		}
		return result.toString();
	}

	protected String getVariableText( IVariable var ) throws DebugException {
		StringBuffer label = new StringBuffer();
		if ( var instanceof ICVariable ) {
			ICType type = null;
			try {
				type = ((ICVariable)var).getType();
			}
			catch( DebugException e ) {
				// don't display type
			}
			if ( type != null && isShowVariableTypeNames() ) {
				String typeName = getVariableTypeName( type );
				if ( typeName != null && typeName.length() > 0 ) {
					label.append( typeName ).append( ' ' );
				}
			}
			String name = var.getName();
			if ( name != null )
				label.append( name.trim() );
			IValue value = var.getValue();
			String valueString = DebugUIPlugin.getModelPresentation().getText( value );
			if ( !isEmpty( valueString ) ) {
				label.append( " = " ); //$NON-NLS-1$
				label.append( valueString );
			}
		}
		if ( !((ICVariable)var).isEnabled() ) {
			label.append( ' ' );
			label.append( CDebugUIMessages.getString( "CDTDebugModelPresentation.25" ) ); //$NON-NLS-1$
		}
		return label.toString();
	}

	protected String getValueText( IValue value ) throws DebugException {
		StringBuffer label = new StringBuffer();
		if ( value instanceof ICDebugElementStatus && !((ICDebugElementStatus)value).isOK() ) {
			label.append(  getFormattedString( CDebugUIMessages.getString( "CDTDebugModelPresentation.4" ), ((ICDebugElementStatus)value).getMessage() ) ); //$NON-NLS-1$
		}
		else if ( value instanceof ICValue ) {
			ICType type = null;
			try {
				type = ((ICValue)value).getType();
			}
			catch( DebugException e ) {
			}
			String valueString = value.getValueString().trim();
			if ( valueString != null ) {
				if ( type != null && type.isCharacter() ) {
					if ( valueString.length() == 0 )
						valueString = "."; //$NON-NLS-1$
					label.append( valueString );
				}
				else if ( type != null && type.isFloatingPointType() ) {
					Number floatingPointValue = CDebugUtils.getFloatingPointValue( (ICValue)value );
					if ( CDebugUtils.isNaN( floatingPointValue ) )
						valueString = "NAN"; //$NON-NLS-1$
					if ( CDebugUtils.isPositiveInfinity( floatingPointValue ) )
						valueString = CDebugUIMessages.getString( "CDTDebugModelPresentation.23" ); //$NON-NLS-1$
					if ( CDebugUtils.isNegativeInfinity( floatingPointValue ) )
						valueString = CDebugUIMessages.getString( "CDTDebugModelPresentation.24" ); //$NON-NLS-1$
					label.append( valueString );
				}
				else if ( type == null || (!type.isArray() && !type.isStructure()) ) {
					if ( valueString.length() > 0 ) {
						label.append( valueString );
					}
				}
			}
		}	
		return label.toString();
	}

	protected String getSharedLibraryText( ICSharedLibrary library, boolean qualified ) {
		String label = new String();
		IPath path = new Path( library.getFileName() );
		if ( !path.isEmpty() )
			label += (qualified ? path.toOSString() : path.lastSegment());
		return label;
	}

	/**
	 * Plug in the single argument to the resource String for the key to get a formatted resource String.
	 *  
	 */
	public static String getFormattedString( String key, String arg ) {
		return getFormattedString( key, new String[]{ arg } );
	}

	/**
	 * Plug in the arguments to the resource String for the key to get a formatted resource String.
	 *  
	 */
	public static String getFormattedString( String string, String[] args ) {
		return MessageFormat.format( string, args );
	}

	protected Image getBreakpointImage( ICBreakpoint breakpoint ) throws CoreException {
		if ( breakpoint instanceof ICLineBreakpoint ) {
			return getLineBreakpointImage( (ICLineBreakpoint)breakpoint );
		}
		if ( breakpoint instanceof ICWatchpoint ) {
			return getWatchpointImage( (ICWatchpoint)breakpoint );
		}
		return null;
	}

	protected Image getLineBreakpointImage( ICLineBreakpoint breakpoint ) throws CoreException {
		ImageDescriptor descriptor = null;
		if ( breakpoint.isEnabled() ) {
			descriptor = CDebugImages.DESC_OBJS_BREAKPOINT_ENABLED;
		}
		else {
			descriptor = CDebugImages.DESC_OBJS_BREAKPOINT_DISABLED;
		}
		return fImageCache.getImageFor( new OverlayImageDescriptor( fDebugImageRegistry.get( descriptor ), computeBreakpointOverlays( breakpoint ) ) );
	}

	protected Image getWatchpointImage( ICWatchpoint watchpoint ) throws CoreException {
		ImageDescriptor descriptor = null;
		if ( watchpoint.isEnabled() ) {
			if ( watchpoint.isReadType() && !watchpoint.isWriteType() )
				descriptor = CDebugImages.DESC_OBJS_READ_WATCHPOINT_ENABLED;
			else if ( !watchpoint.isReadType() && watchpoint.isWriteType() )
				descriptor = CDebugImages.DESC_OBJS_WRITE_WATCHPOINT_ENABLED;
			else
				descriptor = CDebugImages.DESC_OBJS_WATCHPOINT_ENABLED;
		}
		else {
			if ( watchpoint.isReadType() && !watchpoint.isWriteType() )
				descriptor = CDebugImages.DESC_OBJS_READ_WATCHPOINT_DISABLED;
			else if ( !watchpoint.isReadType() && watchpoint.isWriteType() )
				descriptor = CDebugImages.DESC_OBJS_WRITE_WATCHPOINT_DISABLED;
			else
				descriptor = CDebugImages.DESC_OBJS_WATCHPOINT_DISABLED;
		}
		return fImageCache.getImageFor( new OverlayImageDescriptor( fDebugImageRegistry.get( descriptor ), computeBreakpointOverlays( watchpoint ) ) );
	}

	protected IBreakpoint getBreakpoint( IMarker marker ) {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoint( marker );
	}

	protected String getBreakpointText( IBreakpoint breakpoint, boolean qualified ) throws CoreException {
		if ( breakpoint instanceof ICAddressBreakpoint ) {
			return getAddressBreakpointText( (ICAddressBreakpoint)breakpoint, qualified );
		}
		if ( breakpoint instanceof ICFunctionBreakpoint ) {
			return getFunctionBreakpointText( (ICFunctionBreakpoint)breakpoint, qualified );
		}
		if ( breakpoint instanceof ICLineBreakpoint ) {
			return getLineBreakpointText( (ICLineBreakpoint)breakpoint, qualified );
		}
		if ( breakpoint instanceof ICWatchpoint ) {
			return getWatchpointText( (ICWatchpoint)breakpoint, qualified );
		}
		return ""; //$NON-NLS-1$
	}

	protected String getLineBreakpointText( ICLineBreakpoint breakpoint, boolean qualified ) throws CoreException {
		StringBuffer label = new StringBuffer();
		appendResourceName( breakpoint, label, qualified );
		appendLineNumber( breakpoint, label );
		appendIgnoreCount( breakpoint, label );
		appendCondition( breakpoint, label );
		return label.toString();
	}

	protected String getWatchpointText( ICWatchpoint watchpoint, boolean qualified ) throws CoreException {
		StringBuffer label = new StringBuffer();
		appendResourceName( watchpoint, label, qualified );
		appendWatchExpression( watchpoint, label );
		appendIgnoreCount( watchpoint, label );
		appendCondition( watchpoint, label );
		return label.toString();
	}

	protected String getAddressBreakpointText( ICAddressBreakpoint breakpoint, boolean qualified ) throws CoreException {
		StringBuffer label = new StringBuffer();
		appendResourceName( breakpoint, label, qualified );
		appendAddress( breakpoint, label );
		appendIgnoreCount( breakpoint, label );
		appendCondition( breakpoint, label );
		return label.toString();
	}

	protected String getFunctionBreakpointText( ICFunctionBreakpoint breakpoint, boolean qualified ) throws CoreException {
		StringBuffer label = new StringBuffer();
		appendResourceName( breakpoint, label, qualified );
		appendFunction( breakpoint, label );
		appendIgnoreCount( breakpoint, label );
		appendCondition( breakpoint, label );
		return label.toString();
	}

	protected StringBuffer appendResourceName( ICBreakpoint breakpoint, StringBuffer label, boolean qualified ) {
		IPath path = breakpoint.getMarker().getResource().getLocation();
		if ( !path.isEmpty() )
			label.append( qualified ? path.toOSString() : path.lastSegment() );
		return label;
	}

	protected StringBuffer appendLineNumber( ICLineBreakpoint breakpoint, StringBuffer label ) throws CoreException {
		int lineNumber = breakpoint.getLineNumber();
		if ( lineNumber > 0 ) {
			label.append( ' ' );
			label.append( MessageFormat.format( CDebugUIMessages.getString( "CDTDebugModelPresentation.26" ), new String[]{ Integer.toString( lineNumber ) } ) ); //$NON-NLS-1$
		}
		return label;
	}

	protected StringBuffer appendAddress( ICAddressBreakpoint breakpoint, StringBuffer label ) throws CoreException {
		try {
			label.append( ' ' );
			label.append( MessageFormat.format( CDebugUIMessages.getString( "CDTDebugModelPresentation.27" ), new String[]{ breakpoint.getAddress() } ) ); //$NON-NLS-1$
		}
		catch( NumberFormatException e ) {
		}
		return label;
	}

	protected StringBuffer appendFunction( ICFunctionBreakpoint breakpoint, StringBuffer label ) throws CoreException {
		String function = breakpoint.getFunction();
		if ( function != null && function.trim().length() > 0 ) {
			label.append( ' ' );
			label.append( MessageFormat.format( CDebugUIMessages.getString( "CDTDebugModelPresentation.28" ), new String[]{ function.trim() } ) ); //$NON-NLS-1$
		}
		return label;
	}

	protected StringBuffer appendIgnoreCount( ICBreakpoint breakpoint, StringBuffer label ) throws CoreException {
		int ignoreCount = breakpoint.getIgnoreCount();
		if ( ignoreCount > 0 ) {
			label.append( ' ' );
			label.append( MessageFormat.format( CDebugUIMessages.getString( "CDTDebugModelPresentation.29" ), new String[]{ Integer.toString( ignoreCount ) } ) ); //$NON-NLS-1$
		}
		return label;
	}

	protected void appendCondition( ICBreakpoint breakpoint, StringBuffer buffer ) throws CoreException {
		String condition = breakpoint.getCondition();
		if ( condition != null && condition.length() > 0 ) {
			buffer.append( ' ' );
			buffer.append( CDebugUIMessages.getString( "CDTDebugModelPresentation.30" ) ); //$NON-NLS-1$
			buffer.append( ' ' );
			buffer.append( condition );
		}
	}

	private void appendWatchExpression( ICWatchpoint watchpoint, StringBuffer label ) throws CoreException {
		String expression = watchpoint.getExpression();
		if ( expression != null && expression.length() > 0 ) {
			label.append( ' ' );
			label.append( CDebugUIMessages.getString( "CDTDebugModelPresentation.31" ) ); //$NON-NLS-1$
			label.append( " \'" ); //$NON-NLS-1$
			label.append( expression );
			label.append( '\'' );
		}
	}

	private ImageDescriptor[] computeBreakpointOverlays( ICBreakpoint breakpoint ) {
		ImageDescriptor[] overlays = new ImageDescriptor[]{ null, null, null, null };
		try {
			if ( breakpoint.isConditional() ) {
				overlays[OverlayImageDescriptor.TOP_LEFT] = (breakpoint.isEnabled()) ? CDebugImages.DESC_OVRS_BREAKPOINT_CONDITIONAL : CDebugImages.DESC_OVRS_BREAKPOINT_CONDITIONAL_DISABLED;
			}
			if ( breakpoint.isInstalled() ) {
				overlays[OverlayImageDescriptor.BOTTOM_LEFT] = (breakpoint.isEnabled()) ? CDebugImages.DESC_OVRS_BREAKPOINT_INSTALLED : CDebugImages.DESC_OVRS_BREAKPOINT_INSTALLED_DISABLED;
			}
			if ( breakpoint instanceof ICAddressBreakpoint ) {
				overlays[OverlayImageDescriptor.TOP_RIGHT] = (breakpoint.isEnabled()) ? CDebugImages.DESC_OVRS_ADDRESS_BREAKPOINT : CDebugImages.DESC_OVRS_ADDRESS_BREAKPOINT_DISABLED;
			}
			if ( breakpoint instanceof ICFunctionBreakpoint ) {
				overlays[OverlayImageDescriptor.TOP_RIGHT] = (breakpoint.isEnabled()) ? CDebugImages.DESC_OVRS_FUNCTION_BREAKPOINT : CDebugImages.DESC_OVRS_FUNCTION_BREAKPOINT_DISABLED;
			}
		}
		catch( CoreException e ) {
			CDebugUIPlugin.log( e );
		}
		return overlays;
	}

	protected Image getVariableImage( IVariable element ) {
		if ( element instanceof ICVariable ) {
			ICType type = null;
			try {
				type = ((ICVariable)element).getType();
			}
			catch( DebugException e ) {
				// use default image
			}
			if ( type != null && (type.isPointer() || type.isReference()) )
				return fDebugImageRegistry.get( (((ICVariable)element).isEnabled()) ? CDebugImages.DESC_OBJS_VARIABLE_POINTER : CDebugImages.DESC_OBJS_VARIABLE_POINTER_DISABLED );
			else if ( type != null && (type.isArray() || type.isStructure()) )
				return fDebugImageRegistry.get( (((ICVariable)element).isEnabled()) ? CDebugImages.DESC_OBJS_VARIABLE_AGGREGATE : CDebugImages.DESC_OBJS_VARIABLE_AGGREGATE_DISABLED );
			else
				return fDebugImageRegistry.get( (((ICVariable)element).isEnabled()) ? CDebugImages.DESC_OBJS_VARIABLE_SIMPLE : CDebugImages.DESC_OBJS_VARIABLE_SIMPLE_DISABLED );
		}
		return null;
	}

	protected Image getRegisterGroupImage( IRegisterGroup element ) {
		return fDebugImageRegistry.get( CDebugImages.DESC_OBJS_REGISTER_GROUP );
	}

	protected Image getRegisterImage( IRegister element ) {
		return fDebugImageRegistry.get( CDebugImages.DESC_OBJS_REGISTER );
	}

	protected Image getExpressionImage( IExpression element ) {
		return fDebugImageRegistry.get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_EXPRESSION ) );
	}

	protected Image getSharedLibraryImage( ICSharedLibrary element ) {
		if ( element.areSymbolsLoaded() ) {
			return fImageCache.getImageFor( new OverlayImageDescriptor( fDebugImageRegistry.get( CDebugImages.DESC_OBJS_LOADED_SHARED_LIBRARY ), new ImageDescriptor[]{ null, CDebugImages.DESC_OVRS_SYMBOLS, null, null } ) );
		}
		return CDebugUIPlugin.getImageDescriptorRegistry().get( CDebugImages.DESC_OBJS_SHARED_LIBRARY );
	}

	private String getVariableTypeName( ICType type ) {
		StringBuffer result = new StringBuffer();
		String typeName = type.getName();
		if ( type.isArray() && typeName != null ) {
			int index = typeName.indexOf( '[' );
			if ( index != -1 )
				typeName = typeName.substring( 0, index ).trim();
		}
		if ( typeName != null && typeName.length() > 0 ) {
			result.append( typeName );
			if ( type.isArray() ) {
				int[] dims = type.getArrayDimensions();
				for( int i = 0; i < dims.length; ++i ) {
					result.append( '[' );
					result.append( dims[i] );
					result.append( ']' );
				}
			}
		}
		return result.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		fImageCache.disposeAll();
	}

	private boolean isEmpty( String str ) {
		return (str == null || str.length() == 0);
	}

	/**
	 * Returns a default text label for the debug element
	 */
	protected String getDefaultText( Object element ) {
		return DebugUIPlugin.getDefaultLabelProvider().getText( element );
	}

	/**
	 * Returns a default image for the debug element
	 */
	protected Image getDefaultImage( Object element ) {
		return DebugUIPlugin.getDefaultLabelProvider().getImage( element );
	}
}
