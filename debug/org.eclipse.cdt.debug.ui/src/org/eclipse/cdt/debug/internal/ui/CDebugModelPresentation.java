/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui;

import java.util.HashMap;

import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
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
			label += "unknown";
		}
		return label;
	}

	protected String getRegisterGroupText( IRegisterGroup group ) {
		String name = "<not available>";
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
}
