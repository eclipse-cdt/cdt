package org.eclipse.cdt.ui.wizards.conversion;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.internal.ui.CPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 *
 * ConvertSimpleToCCStdMakeProjectWizardPage
 * Standard main page for a wizard that adds a C++ project Nature to a project with no nature associated with it.
 * This conversion is one way in that the project cannot be converted back (i.e have the nature removed).
 *
 * @author Judy N. Green
 * @since Aug 6, 2002
 *<p>
 * Example useage:
 * <pre>
 * mainPage = new ConvertSimpleToCCStdMakeProjectWizardPage("UKtoCCConvertProjectPage");
 * mainPage.setTitle("Project Conversion");
 * mainPage.setDescription("Add C++ a Nature to a project.");
 * </pre>
 * </p>
 */
public class ConvertSimpleToCCStdMakeProjectWizardPage extends ConvertSimpleToCStdMakeProjectWizardPage {
    
    private static final String WZ_TITLE = "SimpleToCCStdMakeConversionWizard.title"; //$NON-NLS-1$
    private static final String WZ_DESC = "SimpleToCCStdMakeConversionWizard.description"; //$NON-NLS-1$
    
	/**
	 * Constructor for ConvertSimpleToCCStdMakeProjectWizardPage.
	 * @param pageName
	 */
	public ConvertSimpleToCCStdMakeProjectWizardPage(String pageName) {
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
     * neither a "C" Nature nor a "C++" Nature
     * 
     * @param project
     * @return boolean
     */
    protected boolean isCandidate(IProject project) {
        boolean noCNature = super.isCandidate(project);
        boolean noCCNature = false;

       try {
            noCCNature = !project.hasNature(CoreModel.CC_NATURE_ID);
       } catch (CoreException e) {
           noCCNature = true;
        }
        return noCNature && noCCNature;
    }

    /**
     * Method convertProject adds a C++ Nature and default make builder
     * to those projects that were selected by the user.
     * 
     * @param project
     * @param monitor
     * @param projectID
     * @throws CoreException
     */
    public void convertProject(IProject project, IProgressMonitor monitor, String projectID)
        throws CoreException {
        if (!project.isOpen()){
            project.open(monitor);   
        }       
        CCorePlugin.getDefault().convertProjectToCC(project, monitor, projectID);
    }        
}
