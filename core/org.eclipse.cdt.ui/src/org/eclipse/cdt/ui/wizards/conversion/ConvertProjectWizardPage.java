/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards.conversion;


import java.util.Vector;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;


/**
 * <p>
 * ConvertProjectWizardPage  Standard main page for a wizard that converts a
 * project's nature.<br> This class provides the UI components and populates
 * the table with all  projects that meet the criteria specified by
 * subclasses in the method  isCandidate(IProject). This class does the
 * conversion through the method convertProjects([]Object), which is also
 * defined by all subclasses.<br> Subclasses provide the methods that
 * determine what files are displayed and what action is performed on them as
 * well as the labels for the Wizard.</p>
 * 
 * Note: Only Projects that are open will be considered for conversion.
 * 
 * 
 * @author Judy N. Green
 * @since Aug 6, 2002 <p>
 */
public abstract class ConvertProjectWizardPage
    extends WizardPage {

	public static final String  KEY_TITLE = "ConvertionWizard.title"; //$NON-NLS-1$
    public static final String  KEY_CONVERTING = "ConvertionWizard.converting"; //$NON-NLS-1$
    private static final String PROJECT_LIST = "ConversionWizard.projectlist"; //$NON-NLS-1$

	protected boolean convertToC = false;
    protected boolean convertToCC = true;
    protected Button cRadioButton;
    protected Button ccRadioButton;
   
    // The Main widget containing the table and its list of condidate open projects
    protected CheckboxTableViewer tableViewer;
    
    protected Button selectAllButton;
    protected Button deselectAllButton;
    
    // We only need to calculate this once per instantiation of this wizard
    protected Object[] listItems = null;

    /**
     * Constructor for ConvertProjectWizardPage.
     * 
     * @param pageName
     */
    public ConvertProjectWizardPage(String pageName) {
        super(pageName);
        setTitle(getWzTitleResource());
        setDescription(getWzDescriptionResource());
    }

    // get methods to allow values to be changed by subclasses
    protected abstract String getWzTitleResource();

    protected abstract String getWzDescriptionResource();

    /**
     * Returns the elements that the user has checked
     * 
     * @return Object[]
     */
    protected Object[] getCheckedElements() {

        return tableViewer.getCheckedElements();
    }

    /**
     * Creates the main wizard page.
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
     */
    public void createControl(Composite parent) {

        Composite  container = new Composite(parent, SWT.NONE);
        
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        container.setLayout(layout);
        setControl(createAvailableProjectsGroup(container));  
        addToMainPage(container);      
        // will default to false until a selection is made
        setPageComplete(validatePage());
    }
    
    /**
     * Method addToMainPage allows subclasses to add 
     * elements to the main page. 
     * 
     */
    protected void addToMainPage(Composite container){
       
		// Add convert to C or C/C++ buttons
		Composite area = ControlFactory.createGroup(container, CUIMessages.getString("ConvertProjectWizardPage.convertTo"), 2); //$NON-NLS-1$
		

		SelectionListener cListener =  new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {				
				convertToC = cRadioButton.getSelection();
				convertToCC = ccRadioButton.getSelection();	
				validatePage();
			}
		};
		cRadioButton = ControlFactory.createRadioButton(area, 
							  CUIMessages.getString("ConvertProjectWizardPage.CProject"), //$NON-NLS-1$
							  "C ", //$NON-NLS-1$
			  			      cListener);
		cRadioButton.setSelection(convertToC);			  			      
		ccRadioButton = ControlFactory.createRadioButton(area, 
							  CUIMessages.getString("ConvertProjectWizardPage.CppProject"), //$NON-NLS-1$
							  "C++", //$NON-NLS-1$
			  			      cListener);	
		ccRadioButton.setSelection(convertToCC);			  			      
				
		area.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				cRadioButton = null;
				ccRadioButton = null;
			}
		});      
    }

    /**
     * Creates a list of projects that can be selected by the user.
     * 
     * @param parent the parent composite
     * @return Composite
     */
    private final Composite createAvailableProjectsGroup(Composite parent) {

        // Add a label
        Label label = new Label(parent, SWT.LEFT);
        label.setText(CUIPlugin.getResourceString(PROJECT_LIST));
        
        Composite  container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        layout.numColumns = 2;
        container.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        container.setLayoutData(data);
        
        // create the table
        Table    table = new Table(container, 
                                   SWT.CHECK | SWT.BORDER | SWT.MULTI | 
                                   SWT.SINGLE | SWT.H_SCROLL | 
                                   SWT.V_SCROLL);
        data = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(data);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        
        TableLayout tableLayout = new TableLayout();
        table.setHeaderVisible(false);
        table.setLayout(tableLayout);

        // add a table viewer
        tableViewer = new CheckboxTableViewer(table);
        tableViewer.setLabelProvider(new ProjectLabelProvider());
        tableViewer.setContentProvider(new ProjectContentProvider());

        // set initial input
        tableViewer.setInput(getElements());

        // define and assign sorter
        tableViewer.setSorter(new ViewerSorter() {
            public int compare(Viewer viewer, Object object1, Object object2) {

                if ((object1 instanceof IProject) && (object2 instanceof IProject)) {
                    IProject left = (IProject)object1;
                    IProject right = (IProject)object2;
                    int result = left.getName().compareToIgnoreCase(right.getName());

                    if (result != 0) {
                        return result;
                    }
                    return left.getName().compareToIgnoreCase(right.getName());
                }
                return super.compare(viewer, object1, object2);
            }

            public boolean isSorterProperty(Object element, String property) {
                return true;
            }
        });
        tableViewer.setAllChecked(false);        
        tableViewer.refresh();
        
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent e) {
                // will default to false until a selection is made
                setPageComplete(validatePage());
                updateSelectionButtons();
            }
        });
        // Add button panel
        
        Composite buttons= new Composite(container, SWT.NULL);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        layout.verticalSpacing = 8;
        buttons.setLayout(layout);
        
        
        selectAllButton= new Button(buttons, SWT.PUSH);
        selectAllButton.setText(CUIMessages.getString("ConvertProjectWizardPage.SelectAll")); //$NON-NLS-1$
        selectAllButton.setLayoutData(getButtonGridData(selectAllButton));
        selectAllButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                ConvertProjectWizardPage.this.tableViewer.setAllChecked(true);
                // update the pageComplete status
                setPageComplete(true);
                updateSelectionButtons();                
            }
        });

        deselectAllButton= new Button(buttons, SWT.PUSH);
        deselectAllButton.setText(CUIMessages.getString("ConvertProjectWizardPage.DeselectAll")); //$NON-NLS-1$
        deselectAllButton.setLayoutData(getButtonGridData(deselectAllButton));
        deselectAllButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                ConvertProjectWizardPage.this.tableViewer.setAllChecked(false);
                // update the pageComplete status
                setPageComplete(false);
                updateSelectionButtons();
            }
        });
        
        // enable or disable selection buttons
        Object[] elements = getElements();
        boolean enableSelectionButtons = (elements != null) && (elements.length > 0);
                
        selectAllButton.setEnabled(enableSelectionButtons);
        // we've called setAllChecked(false) earlier
        deselectAllButton.setEnabled(false); 

        return parent;
    }
       
     /*
      * Method updateSelectionButtons, enables/disables buttons
      * dependent on what is selected
      */
      
     protected void updateSelectionButtons() { 
        
        // update select and deselect buttons as required 
        Object[] checkedObjects = getCheckedElements(); 
        int totalItems = tableViewer.getTable().getItemCount();
        boolean allSelected = checkedObjects.length == totalItems;
        boolean noneSelected = checkedObjects.length == 0;            
        selectAllButton.setEnabled(!allSelected);
        deselectAllButton.setEnabled(!noneSelected);
      }
    /*
     * Method  getButtonGridData creates 
     * and returns a GridData for the given button
     * 
     * @GridData
     */
    private static GridData getButtonGridData(Button button) {
        GridData data= new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint= SWTUtil.getButtonWidthHint(button);
        data.heightHint= SWTUtil.getButtonHeigthHint(button);
    
        return data;
    }

    /**
     * Returns whether this page's controls currently all contain valid values.
     * 
     * @return <code>true</code> if the user has selected at  least one
     *         candidate project.
     */
    protected boolean validatePage() {

        Object[] selection = getCheckedElements();

        return ((selection != null) && (selection.length > 0));
    }

    /**
     * Provides the contents for the list using the enclosing class's method
     * getElements();
     */
    public class ProjectContentProvider
        implements IStructuredContentProvider {
        public Object[] getElements(Object parent) {
             return listItems;                
        }

        public void dispose() {}

        public void inputChanged(Viewer viewer, Object oldInput, 
                                 Object newInput) {}
    }

    /**
     * Provides labels for the listed items.  In this case it returns each
     * project's name
     */
    public class ProjectLabelProvider
        extends LabelProvider
        implements ITableLabelProvider {
        public String getColumnText(Object obj, int index) {

            if (index == 0) {

                return ((IProject)obj).getName();
            }

            return ""; //$NON-NLS-1$
        }

        public Image getColumnImage(Object obj, int index) {

            return PlatformUI.getWorkbench().getSharedImages().getImage(
                           IDE.SharedImages.IMG_OBJ_PROJECT);
        }
    }

    /**
     * Returns a list of open projects that are determined to be candidates
     * through the method isCandidate().<br>
     * 
     * Note: Only Projects that are open will be considered for conversion.
     * 
     * @return Object[] which may be null
     */
    protected Object[] getElements() {

        IWorkspace workspace = CUIPlugin.getWorkspace();
        IProject[] projects = workspace.getRoot().getProjects();
        Vector     candidates = new Vector(projects.length);
        IProject   next = null;

        // ensure we only present open, valid candidates to the user
        for (int i = 0; i < projects.length; i++) {
            next = projects[i];

            if ((next != null) 
                    && next.isOpen() 
                        && isCandidate(next)) {
                candidates.addElement(next);
            }

            next = null;
        }

        // convert to an array for return
        Object[] candidateArray = null;

        if (candidates.size() > 0) {
            candidateArray = new Object[candidates.size()];
            candidates.copyInto(candidateArray);
        }
        // update the global variable that will 
        // be returned by the ProjectContentProvider
        listItems = candidateArray;
        
        return candidateArray;
    }

    /**
     * doRun can be overwritten in subclasses to change behaviour, but this is
     * generally not required. It is called from the corresponding Conversion
     * Wizard
     * 
     * @param monitor
     * @param projectID
     * @exception CoreException
     */
    public void doRun(IProgressMonitor monitor, String projectID) throws CoreException {

        Object[] selection = getCheckedElements();
        int      totalSelected = selection.length;

        if ((selection != null) && (totalSelected > 0)) {
            if (monitor == null) {
                monitor = new NullProgressMonitor();
            }
            monitor.beginTask(CUIPlugin.getResourceString(KEY_TITLE), 1);
            convertProjects(selection, monitor, projectID);
        }
    }

    /**
     * convertProjects calls the convertProject() method on each project
     * passed to it.
     * 
     * @param selected
     * @param monitor
     * @param projectID
     * @throws CoreException
     */
    private void convertProjects(Object[] selected, IProgressMonitor monitor, String projectID)
                          throws CoreException {
        monitor.beginTask(CUIPlugin.getResourceString(KEY_CONVERTING), 
                          selected.length);
		try {
	        for (int i = 0; i < selected.length; i++) {
	            IProject project = (IProject)selected[i];
    	        convertProject(project, new SubProgressMonitor(monitor, 1), projectID);
        	}
		} finally {
	        monitor.done();
		}
    }

    /**
     * Method finish we always finish successfully  :)
     * 
     * @return boolean
     */
    public boolean finish() {

        return true;
    }

    /**
     * Must be overwritten in subclasses to change behaviour Determines which
     * projects will be displayed in the list
     * 
     * @param project
     * @return boolean
     */
    public abstract boolean isCandidate(IProject project);

    /**
     * convertProject must be overwritten in subclasses to change behaviour
     * 
     * @param project
     * @param monitor
     * @param projectID
     * @throws CoreException
     */
    public void convertProject(IProject project, 
                                IProgressMonitor monitor, 
                                String projectID)
                                throws CoreException{
        // Add the correct nature
    	if (convertToC) {
    		if (!project.hasNature(CProjectNature.C_NATURE_ID)){
    			addCNature(project, monitor, true);          	
    		} else {
    			if (project.hasNature(CCProjectNature.CC_NATURE_ID)){
    				// remove the C++ nature
    				CCProjectNature.removeCCNature(project, monitor);
    			}    			
    		}
    	} else {
    		if (convertToCC && !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
    			addCCNature(project, monitor, true);          	
    		}            			
    	}                           	
    }
	protected void addCNature(IProject project, IProgressMonitor monitor, boolean addMakeBuilder) throws CoreException{
		if ( getWizard() instanceof ConversionWizard) {
     		CCorePlugin.getDefault().convertProjectToC(project, monitor, ((ConversionWizard)getWizard()).getProjectID());
		}
     }
     
     protected void addCCNature(IProject project, IProgressMonitor monitor, boolean addMakeBuilder) throws CoreException{
		if ( getWizard() instanceof ConversionWizard) {
	     	if (project.hasNature(CProjectNature.C_NATURE_ID)) {     		
		     	CCorePlugin.getDefault().convertProjectFromCtoCC(project, monitor);
     		} else {
	     		CCorePlugin.getDefault().convertProjectToCC(project, monitor, ((ConversionWizard)getWizard()).getProjectID());
     		}
		}
     }
    
}
