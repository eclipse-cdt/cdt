/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.breakpoints;

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;
import org.eclipse.ui.texteditor.MarkerAnnotation;


/**
 * Provides breakpoint's image information.
 */
public class BreakpointImageProvider implements IAnnotationImageProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IAnnotationImageProvider#getManagedImage(org.eclipse.jface.text.source.Annotation)
	 */
	@Override
	public Image getManagedImage( Annotation annotation ) {
		if ( annotation instanceof MarkerAnnotation ) {
			IMarker marker = ((MarkerAnnotation)annotation).getMarker();
			if ( marker != null && marker.exists() ) {
				IBreakpoint breakpoint = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint( marker );
				if ( breakpoint != null ) {
					return DebugUIPlugin.getModelPresentation().getImage( breakpoint );
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IAnnotationImageProvider#getImageDescriptorId(org.eclipse.jface.text.source.Annotation)
	 */
	@Override
	public String getImageDescriptorId( Annotation annotation ) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IAnnotationImageProvider#getImageDescriptor(java.lang.String)
	 */
	@Override
	public ImageDescriptor getImageDescriptor( String imageDescritporId ) {
		return null;
	}
}
