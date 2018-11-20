/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import java.io.File;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModelFactory;

public class DebugMarkerAnnotationModelFactory extends ResourceMarkerAnnotationModelFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.ResourceMarkerAnnotationModelFactory#createAnnotationModel(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IAnnotationModel createAnnotationModel(IPath location) {
		IFile file = FileBuffers.getWorkspaceFileAtLocation(location);
		if (file != null) {
			return super.createAnnotationModel(location);
		}
		File osFile = new File(location.toOSString());
		if (osFile.exists()) {
			return new DebugMarkerAnnotationModel(osFile);
		}
		return null;
	}

}
