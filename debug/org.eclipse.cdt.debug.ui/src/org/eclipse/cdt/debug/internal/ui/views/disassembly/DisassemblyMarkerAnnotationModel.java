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
package org.eclipse.cdt.debug.internal.ui.views.disassembly;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

/**
 * Marker annotation model for disassembly.
 */
public class DisassemblyMarkerAnnotationModel extends ResourceMarkerAnnotationModel {

	private DisassemblyEditorInput fInput;

	public DisassemblyMarkerAnnotationModel() {
		super( ResourcesPlugin.getWorkspace().getRoot() );
	}

	protected DisassemblyEditorInput getInput() {
		return this.fInput;
	}

	protected void setInput( DisassemblyEditorInput input ) {
		this.fInput = input;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#deleteMarkers(org.eclipse.core.resources.IMarker[])
	 */
	protected void deleteMarkers( IMarker[] markers ) throws CoreException {
		// TODO Auto-generated method stub
		super.deleteMarkers( markers );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#isAcceptable(org.eclipse.core.resources.IMarker)
	 */
	protected boolean isAcceptable( IMarker marker ) {
		// TODO Auto-generated method stub
		return super.isAcceptable( marker );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#retrieveMarkers()
	 */
	protected IMarker[] retrieveMarkers() throws CoreException {
		// TODO Auto-generated method stub
		return super.retrieveMarkers();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#createPositionFromMarker(org.eclipse.core.resources.IMarker)
	 */
	protected Position createPositionFromMarker( IMarker marker ) {
		// TODO Auto-generated method stub
		return super.createPositionFromMarker( marker );
	}
}
