package org.eclipse.cdt.ui.wizards.conversion;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * This wizard provides a method by which the user can 
 * add a C nature to a project that previously had no nature associated with it.
 */
public class ConvertToStdMakeConversionWizard extends ConversionWizard {
	
    private static final String WZ_TITLE = "ConvertToStdMakeConversionWizard.title"; //$NON-NLS-1$
	private static final String WZ_DESC = "ConvertToStdMakeConversionWizard.description"; //$NON-NLS-1$
    private static final String PREFIX= "ConvertToStdMakeConversionWizard"; //$NON-NLS-1$
    
    private static final String WINDOW_TITLE = "ConvertToStdMakeConversionWizard.windowTitle";//$NON-NLS-1$
    
    /**
     * ConvertToStdMakeConversionWizard Wizard constructor
     */
 	public ConvertToStdMakeConversionWizard() {
		this(getWindowTitleResource(), getWzDescriptionResource());
	}
    /**
     * ConvertToStdMakeConversionWizard Wizard constructor
     * 
     * @param title
     * @param desc
     */
	public ConvertToStdMakeConversionWizard(String title, String desc) {
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
     * Method addPages adds our Simple to C conversion Wizard page.
     * 
     * @see Wizard#createPages
     */ 
    public void addPages() { 
        mainPage = new ConvertToStdMakeProjectWizardPage(getPrefix());
        
        addPage(mainPage);
    }     

	public String getProjectID() {
		return CCorePlugin.PLUGIN_ID + ".make";//$NON-NLS-1$
	}
}
