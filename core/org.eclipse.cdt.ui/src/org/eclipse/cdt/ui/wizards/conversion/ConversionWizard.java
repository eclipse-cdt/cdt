package org.eclipse.cdt.ui.wizards.conversion;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.ui.CPlugin;
import org.eclipse.cdt.ui.wizards.CProjectWizard;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.swt.widgets.TabFolder;
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
    extends CProjectWizard {

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

        return CPlugin.getResourceString(WINDOW_TITLE);
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
     * Method getPrefix,  allows prefix value to be changed by subclasses
     * 
     * @return String
     */
    protected static String getPrefix() {

        return PREFIX;
    }

    /**
     * Method addTabItems, allows subclasses to add additional pages
     * 
     * @see org.eclipse.cdt.ui.wizards.CProjectWizard#addTabItems(TabFolder)
     */
    public void addTabItems(TabFolder folder) {

        // we have no tabs, but must implement this abstract method
    }

    /**
     * Method doRun calls the doRunPrologue and mainPage's  doRun method and the
     * doRunEpliogue. Subclasses may overwrite to add further actions
     * 
     * @see org.eclipse.cdt.ui.wizards.CProjectWizard#doRun(IProgressMonitor)
     */
    protected void doRun(IProgressMonitor monitor) {
        doRunPrologue(monitor);
        try{
            mainPage.doRun(monitor, getProjectID());
        } catch (CoreException ce){
            CCorePlugin.log(ce);
        } finally{
            doRunEpilogue(monitor);
            monitor.isCanceled();
        }
    }
    /**
     * Return the type of project that it is being converted to
     * The default if a make project
     */
    public String getProjectID() {
        return CCorePlugin.getDefault().PLUGIN_ID + ".make";//$NON-NLS-1$
    }

    /**
     * Method addPages allows subclasses to add as many pages as they need. Overwrite
     * to create a conversion specific page.
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