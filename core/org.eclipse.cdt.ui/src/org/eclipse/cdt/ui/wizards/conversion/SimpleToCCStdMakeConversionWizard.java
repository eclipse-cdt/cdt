package org.eclipse.cdt.ui.wizards.conversion;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.internal.ui.CPlugin;

/**
 * This wizard provides a method by which the user can 
 * add a C++ nature to a project that previously had no nature associated with it.
 */
public class SimpleToCCStdMakeConversionWizard extends ConversionWizard {
	
    private static final String WZ_TITLE = "SimpleToCCStdMakeConversionWizard.title"; //$NON-NLS-1$
	private static final String WZ_DESC = "SimpleToCCStdMakeConversionWizard.description"; //$NON-NLS-1$
    private static final String PREFIX= "SimpleToCCStdMakeConversionWizard"; //$NON-NLS-1$
    
    private static final String WINDOW_TITLE = "SimpleToCCStdMakeConversionWizard.windowTitle";//$NON-NLS-1$
    
    /**
     * SimpleToCCStdMakeConversionWizard Wizard constructor
     */
 	public SimpleToCCStdMakeConversionWizard() {
		this(getWindowTitleResource(), getWzDescriptionResource());
	}
    /**
     * SimpleToCCStdMakeConversionWizard Wizard constructor
     * 
     * @param title
     * @param desc
     */
	public SimpleToCCStdMakeConversionWizard(String title, String desc) {
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
     * Method getPrefix, allows prefix value to be changed by subclasses
     * 
     * @return String
     */
    protected static String getPrefix() {
        return PREFIX;
    }
    
    /**
     * Method addPages adds our C++ conversion Wizard page.
     * 
     * @see Wizard#createPages
     */ 
    public void addPages() { 
        mainPage = new ConvertSimpleToCCStdMakeProjectWizardPage(getPrefix());
        
        addPage(mainPage);
    }     
}
