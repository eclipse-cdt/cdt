package org.eclipse.cdt.ui.wizards.conversion;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.internal.ui.CPlugin;

/**
 * This wizard provides a method by which the user can 
 * add a C nature to a project that previously had no nature associated with it.
 */
public class UKtoCConversionWizard extends ConversionWizard {
	
    private static final String WZ_TITLE = "UKtoCConversionWizard.title"; //$NON-NLS-1$
	private static final String WZ_DESC = "UKtoCConversionWizard.description"; //$NON-NLS-1$
    private static final String PREFIX= "UKtoCConversionWizard"; //$NON-NLS-1$
    
    private static final String WINDOW_TITLE = "UKtoCConversionWizard.windowTitle";//$NON-NLS-1$
    
    /**
     * UKtoCConversionWizard Wizard constructor
     */
 	public UKtoCConversionWizard() {
		this(getWindowTitleResource(), getWzDescriptionResource());
	}
    /**
     * UKtoCConversionWizard Wizard constructor
     * 
     * @param title
     * @param desc
     */
	public UKtoCConversionWizard(String title, String desc) {
		super(title, desc);
	}
 
    /**
     * Method getWzDescriptionResource,  allows Wizard description label value
     * to be changed by subclasses
     * 
     * @return String
     */
    protected static String getWzDescriptionResource() {
        return CPlugin.getResourceString(WZ_DESC);
    }

    /**
     * Method getWzTitleResource,  allows Wizard description label value
     * to be changed by subclasses
     * 
     * @return String
     */
    protected static String getWzTitleResource() {
        return CPlugin.getResourceString(WZ_TITLE);
    }
    
    /**
     * Method getWindowTitleResource, allows Wizard Title label value to be
     * changed by subclasses
     * 
     * @return String
     */
    protected static String getWindowTitleResource() {

        return CPlugin.getResourceString(WINDOW_TITLE);
    }
    
   /**
     * Method getPrefix,  allows prefix value to be changed by subclasses
     * 
     * @return String
     */
    protected static String getPrefix() {
        return PREFIX;
    }
    
    /**
     * Method addPages adds our C to C++ conversion Wizard page.
     * 
     * @see Wizard#createPages
     */ 
    public void addPages() { 
        mainPage = new ConvertUKtoCProjectWizardPage(getPrefix());
        
        addPage(mainPage);
        
        // ensure proper window name by overwriting the title set by the super class
        this.setWindowTitle(getWindowTitleResource());
    }     
}
