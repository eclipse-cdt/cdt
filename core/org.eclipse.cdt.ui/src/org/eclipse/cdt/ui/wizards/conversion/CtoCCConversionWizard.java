package org.eclipse.cdt.ui.wizards.conversion;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.ui.CUIPlugin;

/**
 * This wizard provides a method by which the user can 
 * change the nature of their projects.
 */
public class CtoCCConversionWizard extends ConversionWizard {
	
    private static final String WZ_TITLE = "CtoCCConversionWizard.title"; //$NON-NLS-1$
	private static final String WZ_DESC = "CtoCCConversionWizard.description"; //$NON-NLS-1$
    private static final String PREFIX= "CtoCCConversionWizard"; //$NON-NLS-1$
    
    private static final String WINDOW_TITLE = "CtoCCConversionWizard.windowTitle";//$NON-NLS-1$
    
    /**
     * CtoCCConversion Wizard constructor
     */
 	public CtoCCConversionWizard() {
		this(getWindowTitleResource(), getWzDescriptionResource());
	}
    /**
     * CtoCCConversion Wizard constructor
     * 
     * @param title
     * @param desc
     */
	public CtoCCConversionWizard(String title, String desc) {
		super(title, desc);
	}
 
    /**
     * Method getWzDescriptionResource,  allows Wizard description label value
     * to be changed by subclasses
     * 
     * @return String
     */
    protected static String getWzDescriptionResource() {
        return CUIPlugin.getResourceString(WZ_DESC);
    }

    /**
     * Method getWzTitleResource,  allows Wizard description label value
     * to be changed by subclasses
     * 
     * @return String
     */
    protected static String getWzTitleResource() {
        return CUIPlugin.getResourceString(WZ_TITLE);
    }
    
    /**
     * Method getWindowTitleResource, allows Wizard Title label value to be
     * changed by subclasses
     * 
     * @return String
     */
    protected static String getWindowTitleResource() {

        return CUIPlugin.getResourceString(WINDOW_TITLE);
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
        mainPage = new ConvertCtoCCStdMakeProjectWizardPage(getPrefix());
        
        addPage(mainPage);
    }     
}
