package org.eclipse.cdt.ui.wizards.conversion;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 *
 * ConvertToStdMakeProjectWizardPage
 * Standard main page for a wizard that adds a C project Nature to a project with no nature associated with it.
 * This conversion is one way in that the project cannot be converted back (i.e have the nature removed).
 *
 * @author Judy N. Green
 * @since Aug 6, 2002
 *<p>
 * Example useage:
 * <pre>
 * mainPage = new ConvertToStdMakeProjectWizardPage("ConvertProjectPage");
 * mainPage.setTitle("Project Conversion");
 * mainPage.setDescription("Add C or C++ a Nature to a project.");
 * </pre>
 * </p>
 */
public class ConvertToStdMakeProjectWizardPage extends ConvertProjectWizardPage {
    
    private static final String WZ_TITLE = "StdMakeConversionWizard.title"; //$NON-NLS-1$
    private static final String WZ_DESC = "StdMakeConversionWizard.description"; //$NON-NLS-1$
    
	/**
	 * Constructor for ConvertToStdMakeProjectWizardPage.
	 * @param pageName
	 */
	public ConvertToStdMakeProjectWizardPage(String pageName) {
		super(pageName);
	}
    
    /**
     * Method getWzTitleResource returns the correct Title Label for this class
     * overriding the default in the superclass.
     */
    protected String getWzTitleResource(){
        return CUIPlugin.getResourceString(WZ_TITLE);
    }
    
    /**
     * Method getWzDescriptionResource returns the correct description
     * Label for this class overriding the default in the superclass.
     */
    protected String getWzDescriptionResource(){
        return CUIPlugin.getResourceString(WZ_DESC);
    }
       
    /**
     * Method isCandidate returns projects that have
     * no "C" or "C++" Nature, but are Projects in the Eclipse sense.
     * 
     * @param project
     * @return boolean
     */
    public boolean isCandidate(IProject project) {       
        boolean noCNature = false;
        try {
            noCNature =  !project.hasNature(CProjectNature.C_NATURE_ID)
            || !project.hasNature(CCProjectNature.CC_NATURE_ID);
       } catch (CoreException e) {
           noCNature = true;
       }
        return noCNature;
    }    
}
