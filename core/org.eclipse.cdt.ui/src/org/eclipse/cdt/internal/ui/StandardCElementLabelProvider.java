/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;


import org.eclipse.cdt.ui.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * CElementLabelProvider that respects settings from the Appearance preference page.
 * Triggers a viewer update when a preference changes.
 */
public class StandardCElementLabelProvider extends CElementLabelProvider implements IPropertyChangeListener {

	//public final static int DEFAULT_FLAGS = SHOW_OVERLAY_ICONS | SHOW_PARAMETERS;
	public final static int DEFAULT_FLAGS = SHOW_OVERLAY_ICONS ;
	
	/**
	 * Constructor for StandardCElementLabelProvider.
	 * @see CElementLabelProvider#CElementLabelProvider
	 */
	public StandardCElementLabelProvider(int flags, IAdornmentProvider[] adormentProviders) {
		super(flags, adormentProviders);
		initMasks();
		CUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	/**
	 * Creates a StandardCElementLabelProvider with DEFAULT_TEXTFLAGS, DEFAULT_IMAGEFLAGS
	 * and the ErrorTickAdornmentProvider.
	 */	
	public StandardCElementLabelProvider() {
		this(DEFAULT_FLAGS, new IAdornmentProvider[] { new ErrorTickAdornmentProvider() });
	}
	
	private void initMasks() {
		// turn on or off the flags depending on property/preference changes.
	}

	/*
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
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
	public void dispose() {
		CUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		super.dispose();
	}

}
