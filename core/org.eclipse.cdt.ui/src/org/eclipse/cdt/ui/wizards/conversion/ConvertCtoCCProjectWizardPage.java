package org.eclipse.cdt.ui.wizards.conversion;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.internal.ui.CPlugin;
import org.eclipse.cdt.internal.ui.util.CoreUtility;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 *
 * ConvertProjectWizardPage
 * Standard main page for a wizard that converts a project's nature from C to C++.
 * This conversion is one way in that the project cannot be converted back from a C++ project to a C project.
 *
 * @author Judy N. Green
 * @since Aug 6, 2002
 *<p>
 * Example useage:
 * <pre>
 * mainPage = new ConvertCtoCCProjectWizardPage("CtoCCConvertProjectPage");
 * mainPage.setTitle("Project Conversion");
 * mainPage.setDescription("Convert a project's nature from C to C++.");
 * </pre>
 * </p>
 */
public class ConvertCtoCCProjectWizardPage extends ConvertProjectWizardPage {
    
    private static final String WZ_TITLE = "CtoCCConversionWizard.title"; //$NON-NLS-1$
    private static final String WZ_DESC = "CtoCCConversionWizard.description"; //$NON-NLS-1$
    
	/**
	 * Constructor for ConvertCtoCCProjectWizardPage.
	 * @param pageName
	 */
	public ConvertCtoCCProjectWizardPage(String pageName) {
		super(pageName);
	}
    
    /**
     * Method getWzTitleResource returns the correct Title Label for this class
     * overriding the default in the superclass.
     */
    protected String getWzTitleResource(){
        return CPlugin.getResourceString(WZ_TITLE);
    }
    
    /**
     * Method getWzDescriptionResource returns the correct description
     * Label for this class overriding the default in the superclass.
     */
    protected String getWzDescriptionResource(){
        return CPlugin.getResourceString(WZ_DESC);
    }
  
    /**
     * Method isCandidate returns projects that have
     * a "C" Nature but do not have a "C++" Nature
     * 
     * @param project
     * @return boolean
     */
    protected boolean isCandidate(IProject project) {
        if (project.isOpen()) {
            try {
                if (project.hasNature(CoreModel.C_NATURE_ID) 
                        && !project.hasNature(CoreModel.CC_NATURE_ID))
                    return true;
            } catch (CoreException e) {
               CPlugin.log(e);
            }
        }
        return false;
    }
  
    /**
     * Method convertProject adds a C++ Nature to those projects 
     * that were selected by the user.
     * 
     * @param project
     * @param monitor
     * @param projectID
     * @throws CoreException
     */
    public void convertProject(IProject project, IProgressMonitor monitor, String projectID)
        throws CoreException {
        CoreUtility.addNatureToProject(project, CoreModel.CC_NATURE_ID, monitor);
        CCorePlugin.getDefault().mapCProjectOwner(project, projectID);
    }        
}
