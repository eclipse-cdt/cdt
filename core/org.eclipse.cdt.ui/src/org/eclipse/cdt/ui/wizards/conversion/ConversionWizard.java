/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards.conversion;

 
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.NewCProjectWizard;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;

/**
 * ConversionWizard  This wizard provides a method by which the user can
 * change the nature of their projects. This class cannot be implemented.  It
 * is meant to be subclassed, with the subclasses providing the new labels,
 * and pages.
 * 
 * @author Judy N. Green
 * @since Aug 8, 2002
 * @see CtoCCConversionWizard#addPages
 */
public abstract class ConversionWizard
    extends NewCProjectWizard {

    // Titles and descriptions may be overwritten by subclasses through the accessor methods.
    private static final String WZ_TITLE = "ConversionWizard.title"; //$NON-NLS-1$
    private static final String WZ_DESC = "ConversionWizard.description"; //$NON-NLS-1$
    private static final String PREFIX = "ConversionWizard"; //$NON-NLS-1$

    // Window Title should be overwritten by subclasses
    private static final String WINDOW_TITLE = "ConversionWizard.windowTitle"; //$NON-NLS-1$

    // the wizards main page containing the list of projects that the user may select for conversion.
    protected ConvertProjectWizardPage mainPage;

    /**
     * Conversion Wizard constructor
     */
    public ConversionWizard() {
        this(getWindowTitleResource(), getWzDescriptionResource());
    }

    /**
     * Conversion Wizard constructor
     * 
     * @param title
     * @param desc
     */
    public ConversionWizard(String title, String desc) {
        super(title, desc);
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchWizard.
     */
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        super.init(workbench, currentSelection);
        setWindowTitle(getWindowTitleResource());
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
     * Method getPrefix,  allows prefix value to be changed by subclasses
     * 
     * @return String
     */
    protected static String getPrefix() {

        return PREFIX;
    }

    /**
     * Method doRun calls the doRunPrologue and mainPage's  doRun method and the
     * doRunEpliogue. Subclasses may overwrite to add further actions
     * 
     * @see org.eclipse.cdt.ui.wizards.CProjectWizard#doRun(IProgressMonitor)
     */
    protected void doRun(IProgressMonitor monitor) throws CoreException {
        try{
            mainPage.doRun(monitor, getProjectID());
        } catch (CoreException ce){
            CCorePlugin.log(ce);
            throw ce;
        } finally{
            doRunEpilogue(monitor);
            monitor.isCanceled();
        }
    }
    /**
     * Return the type of project that it is being converted to
     * The default if a make project
     */
    public abstract String getProjectID();

    /**
     * Method addPages allows subclasses to add as many pages as they need. Overwrite
     * to create at least one conversion specific page. <p>
     * 
     * i.e. <br>
     *<pre> 
     *   mainPage = new ConvertToStdMakeProjectWizardPage(getPrefix());
     *   addPage(mainPage);
     *</pre>
     * 
     * @see Wizard#createPages
     */
    public abstract void addPages();

    /**
     * Required by superclass but with no implementation here
     * 
     * @param monitor 
     */
    protected void doRunPrologue(IProgressMonitor monitor) {}

    /**
     * Required by superclass but with no implementation here
     * 
     * @param monitor 
     */
    protected void doRunEpilogue(IProgressMonitor monitor) {}
}
