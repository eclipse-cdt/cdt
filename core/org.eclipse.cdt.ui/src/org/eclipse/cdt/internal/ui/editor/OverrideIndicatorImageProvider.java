/*******************************************************************************
 * Copyright (c) 2010, 2015 Tomasz Wesolowski and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;

/**
 * @author Tomasz Wesolowski
 */
public class OverrideIndicatorImageProvider implements IAnnotationImageProvider {
	private static final String OVERRIDE_IMG_DESC_ID = "CPluginImages.DESC_OBJS_OVERRIDES"; //$NON-NLS-1$
	private static final String IMPLEMENT_IMG_DESC_ID = "CPluginImages.DESC_OBJS_IMPLEMENTS"; //$NON-NLS-1$
	private static final String SHADOW_IMG_DESC_ID = "CPluginImages.DESC_OBJS_SHADOWS"; //$NON-NLS-1$

	@Override
	public Image getManagedImage(Annotation annotation) {
		return null;
	}

	@Override
	public String getImageDescriptorId(Annotation annotation) {
		if (!isImageProviderFor(annotation)) {
			return null;
		}
		switch (getAnnotationType(annotation)) {
		case OverrideIndicatorManager.ANNOTATION_OVERRIDES:
			return OVERRIDE_IMG_DESC_ID;
		case OverrideIndicatorManager.ANNOTATION_IMPLEMENTS:
			return IMPLEMENT_IMG_DESC_ID;
		case OverrideIndicatorManager.ANNOTATION_SHADOWS:
			return SHADOW_IMG_DESC_ID;
		}
		assert false;
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor(String imageDescritporId) {
		if (imageDescritporId.equals(OVERRIDE_IMG_DESC_ID)) {
			return CPluginImages.DESC_OBJS_OVERRIDES;
		} else if (imageDescritporId.equals(IMPLEMENT_IMG_DESC_ID)) {
			return CPluginImages.DESC_OBJS_IMPLEMENTS;
		} else if (imageDescritporId.equals(SHADOW_IMG_DESC_ID)) {
			return CPluginImages.DESC_OBJS_SHADOWS;
		}
		assert false;
		return null;
	}

	private boolean isImageProviderFor(Annotation annotation) {
		return annotation != null
				&& OverrideIndicatorManager.OverrideIndicator.ANNOTATION_TYPE_ID.equals(annotation.getType());
	}

	private int getAnnotationType(Annotation annotation) {
		return ((OverrideIndicatorManager.OverrideIndicator) annotation).getIndicationType();
	}
}
