/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.cdt.core.model.util.CElementBaseLabels;

/**
 * CUILabelProvider that respects settings from the Appearance preference page.
 * Triggers a viewer update when a preference changes (currently none).
 */
public class AppearanceAwareLabelProvider extends CUILabelProvider implements IPropertyChangeListener {

	public final static int DEFAULT_TEXTFLAGS= CElementBaseLabels.M_PARAMETER_TYPES | CElementBaseLabels.PROJECT_POST_QUALIFIED;
	public final static int DEFAULT_IMAGEFLAGS= CElementImageProvider.OVERLAY_ICONS;
	
	private int fTextFlagMask;
	private int fImageFlagMask;

	/**
	 * Constructor for AppearanceAwareLabelProvider.
	 */
	public AppearanceAwareLabelProvider(int textFlags, int imageFlags) {
		super(textFlags, imageFlags);
		initMasks();
//		PreferenceConstants.getPreferenceStore().addPropertyChangeListener(this);
	}

	/**
	 * Creates a labelProvider with DEFAULT_TEXTFLAGS and DEFAULT_IMAGEFLAGS
	 */	
	public AppearanceAwareLabelProvider() {
		this(DEFAULT_TEXTFLAGS, DEFAULT_IMAGEFLAGS);
	}
	
	private void initMasks() {
		fTextFlagMask= -1;
		fImageFlagMask= -1;
	}

	/*
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
	}

	/*
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
//		PreferenceConstants.getPreferenceStore().removePropertyChangeListener(this);
		super.dispose();
	}

	/*
	 * @see CUILabelProvider#evaluateImageFlags()
	 */
	protected int evaluateImageFlags(Object element) {
		return getImageFlags() & fImageFlagMask;
	}

	/*
	 * @see CUILabelProvider#evaluateTextFlags()
	 */
	protected int evaluateTextFlags(Object element) {
		return getTextFlags() & fTextFlagMask;
	}

}
