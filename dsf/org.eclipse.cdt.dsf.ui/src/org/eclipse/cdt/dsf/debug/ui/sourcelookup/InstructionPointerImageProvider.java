/*******************************************************************************
 *  Copyright (c) 2006, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.ui.sourcelookup;

import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.debug.ui.sourcelookup.InstructionPointerManager.IPAnnotation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 * @since 1.0
 * @deprecated Not used anymore.
 */
@Deprecated
@ThreadSafe
public class InstructionPointerImageProvider implements IAnnotationImageProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IAnnotationImageProvider#getManagedImage(org.eclipse.jface.text.source.Annotation)
	 */
	@Override
	public Image getManagedImage(Annotation annotation) {
		return ((IPAnnotation) annotation).getImage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IAnnotationImageProvider#getImageDescriptorId(org.eclipse.jface.text.source.Annotation)
	 */
	@Override
	public String getImageDescriptorId(Annotation annotation) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IAnnotationImageProvider#getImageDescriptor(java.lang.String)
	 */
	@Override
	public ImageDescriptor getImageDescriptor(String imageDescritporId) {
		return null;
	}

}
