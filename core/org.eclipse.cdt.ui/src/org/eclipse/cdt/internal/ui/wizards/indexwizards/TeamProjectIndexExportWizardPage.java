/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.wizards.indexwizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardDataTransferPage;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.pdom.TeamPDOMExportOperation;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;

import org.eclipse.cdt.internal.ui.viewsupport.ListContentProvider;
 
public class TeamProjectIndexExportWizardPage extends  WizardDataTransferPage implements Listener {

    private IStructuredSelection fInitialSelection;
	private CheckboxTableViewer fProjectViewer;
    private Text fDestinationField;

    /**
     *	Create an instance of this class
     */
    protected TeamProjectIndexExportWizardPage(String name, IStructuredSelection selection) {
        super(name);
        fInitialSelection= selection;
    }

    /**
     * Create an instance of this class.
     *
     * @param selection the selection
     */
    public TeamProjectIndexExportWizardPage(IStructuredSelection selection) {
        this("indexExportPage", selection); //$NON-NLS-1$
        setTitle(Messages.getString("TeamProjectIndexExportWizardPage.title")); //$NON-NLS-1$
        setDescription(Messages.getString("TeamProjectIndexExportWizardPage.description")); //$NON-NLS-1$
    }

    public void createControl(Composite parent) {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setFont(parent.getFont());

        createResourcesGroup(composite);
//        createButtonsGroup(composite);
        createDestinationGroup(composite);
//        createOptionsGroup(composite);

        restoreWidgetValues(); 
        if (fInitialSelection != null) {
			setupBasedOnInitialSelections();
		}
        setupDestination();

        updateWidgetEnablements();
        setPageComplete(determinePageCompletion());
        setErrorMessage(null);	// should not initially have error message
        
        setControl(composite);
        giveFocusToDestination();
    }

    /**
     * Creates the checkbox tree and list for selecting resources.
     *
     * @param parent the parent control
     */
    protected final void createResourcesGroup(Composite parent) {
        Composite resourcesGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        resourcesGroup.setLayout(layout);
        resourcesGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
        resourcesGroup.setFont(parent.getFont());

        Table table= new Table(resourcesGroup, SWT.CHECK | SWT.BORDER);
        table.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fProjectViewer= new CheckboxTableViewer(table);
		fProjectViewer.setContentProvider(new ListContentProvider());
		fProjectViewer.setLabelProvider(new CElementLabelProvider());        
        ICheckStateListener listener = new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                updateWidgetEnablements();
            }
        };
        fProjectViewer.addCheckStateListener(listener);
        	
        initProjects();
    }

    private void initProjects() {
        ArrayList input = new ArrayList();
        ICProject[] projects;
		try {
			projects = CoreModel.getDefault().getCModel().getCProjects();
	        for (int i = 0; i < projects.length; i++) {
	            if (projects[i].getProject().isOpen()) {
					input.add(projects[i]);
				}
	        }
		} catch (CModelException e) {
			CUIPlugin.getDefault().log(e);
		}
		fProjectViewer.setInput(input);
	}

    private void setupBasedOnInitialSelections() {
    	HashSet names= new HashSet();
        Iterator it = fInitialSelection.iterator();
        while (it.hasNext()) {
            IProject project = (IProject) it.next();
            names.add(project.getName());
        }
        
        Collection prjs= (Collection) fProjectViewer.getInput();
        for (Iterator iterator = prjs.iterator(); iterator.hasNext();) {
			ICProject prj = (ICProject) iterator.next();
			if (names.contains(prj.getElementName())) {
	            fProjectViewer.setChecked(prj, true);
			}
		}
    }
    
    private void setupDestination() {
    	String dest;
    	ICProject[] prjs= getCheckedElements();
    	if (prjs.length > 0) {
    		dest= IndexerPreferences.getIndexImportLocation(prjs[0].getProject());
    	}
    	else {
    		dest= IndexerPreferences.getIndexImportLocation(null);
    	}
    	fDestinationField.setText(dest);
    }
    	
    
    private void createDestinationGroup(Composite parent) {
        Font font = parent.getFont();
        // destination specification group
        Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        destinationSelectionGroup.setLayout(layout);
        destinationSelectionGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
        destinationSelectionGroup.setFont(font);

        Label destinationLabel = new Label(destinationSelectionGroup, SWT.NONE);
        destinationLabel.setText(Messages.getString("TeamProjectIndexExportWizardPage.destinationLabel")); //$NON-NLS-1$
        destinationLabel.setFont(font);

        // destination name entry field
        fDestinationField = new Text(destinationSelectionGroup, SWT.BORDER);
        fDestinationField.addListener(SWT.Modify, this);
        fDestinationField.addListener(SWT.Selection, this);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        fDestinationField.setLayoutData(data);
        fDestinationField.setFont(font);

        new Label(parent, SWT.NONE); // vertical spacer
    }


    public boolean finish() {
        ICProject[] projectsToExport= getCheckedElements();

        // about to invoke the operation so save our state
        saveWidgetValues();
        
        return executeExportOperation(projectsToExport);
    }

	private ICProject[] getCheckedElements() {
		Object[] obj= fProjectViewer.getCheckedElements();
		ICProject[] prjs= new ICProject[obj.length];
		System.arraycopy(obj, 0, prjs, 0, obj.length);
		return prjs;
	}

    private boolean executeExportOperation(final ICProject[] projects) {
    	final String dest= getDestinationValue();
    	final MultiStatus status= new MultiStatus(CUIPlugin.PLUGIN_ID, 
    			0, Messages.getString("TeamProjectIndexExportWizardPage.errorWhileExporting"), null); //$NON-NLS-1$
    			
    	IRunnableWithProgress op= new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask("", projects.length); //$NON-NLS-1$
				for (int i = 0; i < projects.length; i++) {
					ICProject project = projects[i];
					TeamPDOMExportOperation op= new TeamPDOMExportOperation(project);
					op.setTargetLocation(dest);
					try {
						op.run(new SubProgressMonitor(monitor, 1));
					} catch (CoreException e) {
						status.merge(e.getStatus());
					}
				}
			}
    	};
        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            displayErrorDialog(e.getTargetException());
            return false;
        }

        if (!status.isOK()) {
            ErrorDialog.openError(getContainer().getShell(),
                    getErrorDialogTitle(),
                    null, // no special message
                    status);
            return false;
        }

        return true;
    }

    private String getDestinationValue() {
        return fDestinationField.getText().trim();
    }

    private void giveFocusToDestination() {
    	fDestinationField.setFocus();
    }

    /**
     *	Answer a boolean indicating whether the receivers destination specification
     *	widgets currently all contain valid values.
     */
    protected boolean validateDestinationGroup() {
        String destinationValue = getDestinationValue();
        if (destinationValue.length() == 0) {
            setMessage(Messages.getString("TeamProjectIndexExportWizardPage.noDestination")); //$NON-NLS-1$
            return false;
        }

        setErrorMessage(null);
        return true;
    }

    protected boolean validateSourceGroup() {
    	// there must be some resources selected for Export
    	boolean isValid = true;
        Object[] projectsToExport = getCheckedElements();
    	if (projectsToExport.length == 0){
    		setErrorMessage(Messages.getString("TeamProjectIndexExportWizardPage.noSelection")); //$NON-NLS-1$
            isValid =  false;
    	} else {
			setErrorMessage(null);
		}
		return super.validateSourceGroup() && isValid;
	}

    protected void updateWidgetEnablements() {
        boolean pageComplete = determinePageCompletion();
        setPageComplete(pageComplete);
        if (pageComplete) {
			setMessage(null);
		}
        super.updateWidgetEnablements();
    }
    

	public void handleEvent(Event event) {
		updateWidgetEnablements();
	}
	
    protected String getErrorDialogTitle() {
        return Messages.getString("TeamProjectIndexExportWizardPage.errorDlgTitle"); //$NON-NLS-1$
    }

	protected boolean allowNewContainerName() {
		return false;
	}
}
