package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * CElementLabelProvider that respects settings from the Appearance preference page.
 * Triggers a viewer update when a preference changes.
 */
public class StandardCElementLabelProvider extends CElementLabelProvider implements IPropertyChangeListener {

	public final static int DEFAULT_TEXTFLAGS= 0; //CElementLabels.ROOT_VARIABLE | JavaElementLabels.M_PARAMETER_TYPES |  JavaElementLabels.M_APP_RETURNTYPE;
	public final static int DEFAULT_IMAGEFLAGS= CElementImageProvider.OVERLAY_ICONS;
	
	private int fTextFlagMask;
	private int fImageFlagMask;

	/**
	 * Constructor for StandardCElementLabelProvider.
	 * @see CElementLabelProvider#CElementLabelProvider
	 */
	public StandardCElementLabelProvider(int textFlags, int imageFlags, IAdornmentProvider[] adormentProviders) {
		super(textFlags, imageFlags, adormentProviders);
		initMasks();
		CUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	/**
	 * Creates a StandardCElementLabelProvider with DEFAULT_TEXTFLAGS, DEFAULT_IMAGEFLAGS
	 * and the ErrorTickAdornmentProvider.
	 */	
	public StandardCElementLabelProvider() {
		this(DEFAULT_TEXTFLAGS, DEFAULT_IMAGEFLAGS, new IAdornmentProvider[] { new ErrorTickAdornmentProvider() });
	}
	
	private void initMasks() {
		fTextFlagMask= -1;
		/* if (!AppearancePreferencePage.showMethodReturnType()) {
			fTextFlagMask ^= JavaElementLabels.M_APP_RETURNTYPE;
		}
		if (!AppearancePreferencePage.isCompressingPkgNameInPackagesView()) {
			fTextFlagMask ^= JavaElementLabels.P_COMPRESSED;
		} */
		
		fImageFlagMask= -1;
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

	/*
	 * @see JavaUILabelProvider#getImageFlags()
	 */
	public int getImageFlags() {
		return super.getImageFlags() & fImageFlagMask;
	}

	/*
	 * @see JavaUILabelProvider#getTextFlags()
	 */
	public int getTextFlags() {
		return super.getTextFlags() & fTextFlagMask;
	}

}
