/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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

import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;

/**
 * CUILabelProvider that respects settings from the Appearance preference page.
 * Triggers a viewer update when a preference changes (currently none).
 */
public class AppearanceAwareLabelProvider extends CUILabelProvider implements IPropertyChangeListener {

	public final static long DEFAULT_TEXTFLAGS= CElementLabels.M_PARAMETER_TYPES | CElementLabels.PROJECT_POST_QUALIFIED | CElementLabels.F_APP_TYPE_SIGNATURE | CElementLabels.M_APP_RETURNTYPE;
	public final static int DEFAULT_IMAGEFLAGS= CElementImageProvider.OVERLAY_ICONS;

	/**
	 * Constructor for AppearanceAwareLabelProvider.
	 */
	public AppearanceAwareLabelProvider(long textFlags, int imageFlags) {
		super(textFlags, imageFlags);
	}

	/**
	 * Creates a labelProvider with DEFAULT_TEXTFLAGS and DEFAULT_IMAGEFLAGS
	 */	
	public AppearanceAwareLabelProvider() {
		this(DEFAULT_TEXTFLAGS, DEFAULT_IMAGEFLAGS);
	}
	
	/*
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
	}

}
