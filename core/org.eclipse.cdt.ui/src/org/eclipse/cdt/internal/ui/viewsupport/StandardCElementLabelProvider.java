/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;


import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;


/**
 * CElementLabelProvider that respects settings from the Appearance preference page.
 * Triggers a viewer update when a preference changes.
 * 
 * @deprecated Use {@link AppearanceAwareLabelProvider} instead.
 */
@Deprecated
public class StandardCElementLabelProvider extends AppearanceAwareLabelProvider {

	/**
	 * Constructor for StandardCElementLabelProvider.
	 * @see CElementLabelProvider#CElementLabelProvider()
	 */
	public StandardCElementLabelProvider(int textFlags, int imageFlags) {
		super(textFlags, imageFlags);
		initMasks();
		CUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	/**
	 * Creates a StandardCElementLabelProvider with DEFAULT_TEXTFLAGS, DEFAULT_IMAGEFLAGS
	 * and the ErrorTickAdornmentProvider.
	 */	
	public StandardCElementLabelProvider() {
		super();
	}
	
	private void initMasks() {
		// turn on or off the flags depending on property/preference changes.
	}

	/*
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		//String property= event.getProperty();
		/* if (property.equals(AppearancePreferencePage.PREF_METHOD_RETURNTYPE)
				|| property.equals(AppearancePreferencePage.PREF_OVERRIDE_INDICATOR)
				|| property.equals(AppearancePreferencePage.PREF_PKG_NAME_PATTERN_FOR_PKG_VIEW)) {
			initMasks();
			LabelProviderChangedEvent lpEvent= new LabelProviderChangedEvent(this, null); // refresh all
			fireLabelProviderChanged(lpEvent);
		}	*/	
	}

	/*
	 * @see IBaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		CUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		super.dispose();
	}

}
