/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui;

import java.util.HashMap;

import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;

/**
 * Enter type comment.
 *
 * @since: Feb 4, 2004
 */
public class CDebugModelPresentation extends LabelProvider implements IDebugModelPresentation {

	public final static String DISPLAY_FULL_PATHS = "DISPLAY_FULL_PATHS"; //$NON-NLS-1$

	protected HashMap fAttributes = new HashMap( 3 );

	protected CDebugImageDescriptorRegistry fDebugImageRegistry = CDebugUIPlugin.getImageDescriptorRegistry();

	private OverlayImageCache fImageCache = new OverlayImageCache();
	
	private static CDebugModelPresentation gfInstance = null;

	public static CDebugModelPresentation getDefault() {
		if ( gfInstance == null )
			gfInstance = new CDebugModelPresentation();
		return gfInstance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute( String attribute, Object value ) {
		if ( value == null )
			return;
		getAttributes().put( attribute, value );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#computeDetail(org.eclipse.debug.core.model.IValue, org.eclipse.debug.ui.IValueDetailListener)
	 */
	public void computeDetail( IValue value, IValueDetailListener listener ) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(java.lang.Object)
	 */
	public IEditorInput getEditorInput( Object element ) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(org.eclipse.ui.IEditorInput, java.lang.Object)
	 */
	public String getEditorId( IEditorInput input, Object element ) {
		// TODO Auto-generated method stub
		return null;
	}

	public Image getImage( Object element ) {
		Image baseImage = getBaseImage( element );
		if ( baseImage != null ) {
			ImageDescriptor[] overlays = new ImageDescriptor[] { null, null, null, null };

			return getImageCache().getImageFor( new OverlayImageDescriptor( baseImage, overlays ) );
		}
		return getDefaultImage( element );
	}

	private Image getBaseImage( Object element ) {
		if ( element instanceof ICSharedLibrary )
			return getSharedLibraryImage( (ICSharedLibrary)element );
		if ( element instanceof IRegisterGroup ) 
			return getRegisterGroupImage( (IRegisterGroup)element );
		if ( element instanceof ICSignal ) 
			return getSignalImage( (ICSignal)element );
		if ( element instanceof ICBreakpoint ) 
			return getBreakpointImage( (ICBreakpoint)element );
		return super.getImage( element );
	}

	protected Image getSharedLibraryImage( ICSharedLibrary element ) {
		if ( element.areSymbolsLoaded() ) {
			return getImageCache().getImageFor(
				new OverlayImageDescriptor( getDebugImageRegistry().get( CDebugImages.DESC_OBJS_LOADED_SHARED_LIBRARY ),
											new ImageDescriptor[] { null, CDebugImages.DESC_OVRS_SYMBOLS, null, null } ) );
		}
		return CDebugUIPlugin.getImageDescriptorRegistry().get( CDebugImages.DESC_OBJS_SHARED_LIBRARY );
	}

	protected Image getSignalImage( ICSignal signal ) {
		return CDebugUIPlugin.getImageDescriptorRegistry().get( CDebugImages.DESC_OBJS_SIGNAL );
	}

	protected Image getRegisterGroupImage( IRegisterGroup element ) {
		return fDebugImageRegistry.get( CDebugImages.DESC_OBJS_REGISTER_GROUP );
	}

	protected Image getBreakpointImage( ICBreakpoint breakpoint ) {
		try {
			if ( breakpoint instanceof ICLineBreakpoint ) {
				return getLineBreakpointImage( (ICLineBreakpoint)breakpoint );
			}
			if ( breakpoint instanceof ICWatchpoint ) {
				return getWatchpointImage( (ICWatchpoint)breakpoint );
			}
		}
		catch( CoreException e ) {
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
		return getImageCache().getImageFor( new OverlayImageDescriptor( fDebugImageRegistry.get( descriptor ), computeBreakpointOverlays( breakpoint ) ) );
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

	public String getText( Object element ) {
		StringBuffer baseText = new StringBuffer( getBaseText( element ) );
		return baseText.toString();
	}

	private String getBaseText( Object element ) {
		boolean showQualified = isShowQualifiedNames();
		StringBuffer label = new StringBuffer();

		if ( element instanceof ICSharedLibrary ) {
			label.append( getSharedLibraryText( (ICSharedLibrary)element, showQualified ) );
			return label.toString();
		}
		if ( element instanceof IRegisterGroup ) {
			label.append( getRegisterGroupText( (IRegisterGroup)element ) );
			return label.toString();
		}
		
		if ( label.length() > 0 ) {
			return label.toString();
		}

		return getDefaultText( element );
	}

	protected String getSharedLibraryText( ICSharedLibrary library, boolean qualified ) {
		String label = new String();
		String name = library.getFileName();
		if ( !isEmpty( name ) ) {	
			IPath path = new Path( library.getFileName() );
			if ( !path.isEmpty() )
				label += ( qualified ? path.toOSString() : path.lastSegment() );
		}
		else {
			label += CDebugUIMessages.getString( "CDebugModelPresentation.unknown_1" ); //$NON-NLS-1$
		}
		return label;
	}

	protected String getRegisterGroupText( IRegisterGroup group ) {
		String name = CDebugUIMessages.getString( "CDebugModelPresentation.not_available_1" ); //$NON-NLS-1$
		try {
			name = group.getName();
		}
		catch( DebugException e ) {
			CDebugUIPlugin.log( e.getStatus() );
		}
		return name;
	}

	protected boolean isShowQualifiedNames() {
		Boolean showQualified = (Boolean)getAttributes().get( DISPLAY_FULL_PATHS );
		showQualified = showQualified == null ? Boolean.FALSE : showQualified;
		return showQualified.booleanValue();
	}

	private HashMap getAttributes() {
		return this.fAttributes;
	}

	private OverlayImageCache getImageCache() {
		return this.fImageCache;
	}

	private CDebugImageDescriptorRegistry getDebugImageRegistry() {
		return this.fDebugImageRegistry;
	}

	private boolean isEmpty( String string ) {
		return ( string == null || string.trim().length() == 0 );
	}

	/**
	 * Returns a default text label for the debug element
	 */
	protected String getDefaultText(Object element) {
		return DebugUIPlugin.getDefaultLabelProvider().getText( element );
	}

	/**
	 * Returns a default image for the debug element
	 */
	protected Image getDefaultImage(Object element) {
		return DebugUIPlugin.getDefaultLabelProvider().getImage( element );
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
}
