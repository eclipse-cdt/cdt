/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.pdom.TeamPDOMExportOperation;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;

import org.eclipse.cdt.internal.ui.viewsupport.ListContentProvider;
 
public class TeamProjectIndexExportWizardPage extends WizardPage implements Listener {
    private static final int SIZING_TEXT_FIELD_WIDTH = 250;

    private IStructuredSelection fInitialSelection;
	private CheckboxTableViewer fProjectViewer;
    private Text fDestinationField;
    private Button fResourceSnapshotButton;

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
        setTitle(Messages.TeamProjectIndexExportWizardPage_title); 
        setDescription(Messages.TeamProjectIndexExportWizardPage_description); 
    }

    public void createControl(Composite parent) {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setFont(parent.getFont());

        createResourcesGroup(composite);
        createDestinationGroup(composite);

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
    private final void createResourcesGroup(Composite parent) {
        Composite resourcesGroup = new Composite(parent, SWT.NONE);
        resourcesGroup.setLayout(new GridLayout());
        resourcesGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        resourcesGroup.setFont(parent.getFont());

        new Label(resourcesGroup, SWT.NONE).setText(Messages.TeamProjectIndexExportWizardPage_labelProjectTable);       
        Table table= new Table(resourcesGroup, SWT.CHECK | SWT.BORDER);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
		fProjectViewer= new CheckboxTableViewer(table);
		fProjectViewer.setContentProvider(new ListContentProvider());
		fProjectViewer.setLabelProvider(new CElementLabelProvider());        
        ICheckStateListener checkListener = new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                updateWidgetEnablements();
            }
        };
        fProjectViewer.addCheckStateListener(checkListener);
        	

        // top level group
        Composite buttonComposite = new Composite(resourcesGroup, SWT.NONE);
        buttonComposite.setFont(parent.getFont());

        GridLayout layout = new GridLayout(2, true);
        layout.marginHeight= layout.marginWidth= 0;
        buttonComposite.setLayout(layout);
        buttonComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));


        Button selectButton = createButton(buttonComposite,
                IDialogConstants.SELECT_ALL_ID, Messages.TeamProjectIndexExportWizardPage_selectAll, false);

        SelectionAdapter listener = new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e) {
                fProjectViewer.setAllChecked(true);
                updateWidgetEnablements();
            }
        };
        selectButton.addSelectionListener(listener);

        Button deselectButton = createButton(buttonComposite,
                IDialogConstants.DESELECT_ALL_ID, Messages.TeamProjectIndexExportWizardPage_deselectAll, false);

        listener = new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e) {
            	fProjectViewer.setAllChecked(false);
                updateWidgetEnablements();
            }
        };
        deselectButton.addSelectionListener(listener);

        initProjects();
    }

    private Button createButton(Composite parent, int id, String label,
            boolean defaultButton) {
        Button button = new Button(parent, SWT.PUSH);

        GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
        button.setLayoutData(buttonData);

        button.setData(new Integer(id));
        button.setText(label);
        button.setFont(parent.getFont());

        if (defaultButton) {
            Shell shell = parent.getShell();
            if (shell != null) {
                shell.setDefaultButton(button);
            }
            button.setFocus();
        }
        button.setFont(parent.getFont());
        setButtonLayoutData(button);
        return button;
    }

    private void initProjects() {
        ArrayList<ICProject> input = new ArrayList<ICProject>();
        ICProject[] projects;
		try {
			projects = CoreModel.getDefault().getCModel().getCProjects();
	        for (ICProject project : projects) {
	            if (project.getProject().isOpen()) {
					input.add(project);
				}
	        }
		} catch (CModelException e) {
			CUIPlugin.log(e);
		}
		fProjectViewer.setInput(input);
	}

    private void setupBasedOnInitialSelections() {
    	HashSet<String> names= new HashSet<String>();
        Iterator<?> it = fInitialSelection.iterator();
        while (it.hasNext()) {
            IProject project = (IProject) it.next();
            names.add(project.getName());
        }
        
        Collection<?> prjs= (Collection<?>) fProjectViewer.getInput();
        for (Object element : prjs) {
			ICProject prj = (ICProject) element;
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
    	GridData gd;
        Font font = parent.getFont();
        // destination specification group
        Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);
        destinationSelectionGroup.setLayout(new GridLayout(2, false));
        destinationSelectionGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
        destinationSelectionGroup.setFont(font);

        Label destinationLabel = new Label(destinationSelectionGroup, SWT.NONE);
        destinationLabel.setText(Messages.TeamProjectIndexExportWizardPage_destinationLabel); 
        destinationLabel.setFont(font);
        destinationLabel.setLayoutData(gd= new GridData());
        gd.horizontalSpan= 2;
        
        // destination name entry field
        fDestinationField = new Text(destinationSelectionGroup, SWT.BORDER);
        fDestinationField.addListener(SWT.Modify, this);
        fDestinationField.addListener(SWT.Selection, this);
        fDestinationField.setFont(font);
        fDestinationField.setLayoutData(gd= new GridData());
        gd.grabExcessHorizontalSpace= true;
        gd.horizontalAlignment= GridData.FILL;
        gd.widthHint = SIZING_TEXT_FIELD_WIDTH;

        Button button= createButton(destinationSelectionGroup, IDialogConstants.CLIENT_ID, Messages.TeamProjectIndexExportWizardPage_variableButton, false);
        SelectionAdapter listener = new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e) {
                onInsertVariable();
            }
        };
        button.addSelectionListener(listener);

        // resource snapshot destination group
        Composite resourceSnapshotDestinationGroup = new Composite(parent, SWT.NONE);
        resourceSnapshotDestinationGroup.setLayout(new GridLayout(1, false));
        resourceSnapshotDestinationGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
        resourceSnapshotDestinationGroup.setFont(font);
        
        fResourceSnapshotButton = new Button(resourceSnapshotDestinationGroup, SWT.CHECK);
        fResourceSnapshotButton.setText(Messages.TeamProjectIndexExportWizardPage_resourceSnapshotButton);
        fResourceSnapshotButton.setFont(font);
        fResourceSnapshotButton.setLayoutData(gd= new GridData());
        gd.grabExcessHorizontalSpace= true;
        gd.horizontalAlignment= GridData.FILL;
    }

	protected void onInsertVariable() {
		StringVariableSelectionDialog dlg= new StringVariableSelectionDialog(getShell());
		if (dlg.open() == Window.OK) {
			String var= dlg.getVariableExpression();
			fDestinationField.insert(var);
		}
	}

	public boolean finish() {
        ICProject[] projectsToExport= getCheckedElements();

        // about to invoke the operation so save our state
        saveWidgetValues();
        return executeExportOperation(projectsToExport);
    }

	private void saveWidgetValues() {
	}

	private void restoreWidgetValues() {
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
    			0, Messages.TeamProjectIndexExportWizardPage_errorExporting, null); 
    	final boolean exportResourceSnapshot = fResourceSnapshotButton.getSelection();

    	IRunnableWithProgress op= new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask("", projects.length); //$NON-NLS-1$
				for (ICProject project : projects) {
					TeamPDOMExportOperation op= new TeamPDOMExportOperation(project);
					op.setTargetLocation(dest);
					if (exportResourceSnapshot) {
						op.setOptions(TeamPDOMExportOperation.EXPORT_OPTION_RESOURCE_SNAPSHOT);
					}
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
            CUIPlugin.log(Messages.TeamProjectIndexExportWizardPage_errorExporting,
            		e.getTargetException());
            displayErrorDialog(e.getTargetException());
            return false;
        }

        if (!status.isOK()) {
        	CUIPlugin.log(status);
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
            setMessage(Messages.TeamProjectIndexExportWizardPage_destinationMessage); 
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
    		setErrorMessage(Messages.TeamProjectIndexExportWizardPage_noProjectError); 
            isValid =  false;
    	} else {
			setErrorMessage(null);
		}
		return isValid;
	}

	protected void updateWidgetEnablements() {
        boolean pageComplete = determinePageCompletion();
        setPageComplete(pageComplete);
        if (pageComplete) {
			setMessage(null);
		}
    }
    

	public void handleEvent(Event event) {
		updateWidgetEnablements();
	}
	
	protected String getErrorDialogTitle() {
        return Messages.TeamProjectIndexExportWizardPage_errorDlgTitle; 
    }

    /**
     * Returns whether this page is complete. This determination is made based upon
     * the current contents of this page's controls.  Subclasses wishing to include
     * their controls in this determination should override the hook methods 
     * <code>validateSourceGroup</code> and/or <code>validateOptionsGroup</code>.
     *
     * @return <code>true</code> if this page is complete, and <code>false</code> if
     *   incomplete
     * @see #validateSourceGroup
     * @see #validateDestinationGroup
     */
    private boolean determinePageCompletion() {
        boolean complete = validateSourceGroup() && validateDestinationGroup();

        // Avoid draw flicker by not clearing the error
        // message unless all is valid.
        if (complete) {
			setErrorMessage(null);
		}

        return complete;
    }

    /**
     * Determine if the page is complete and update the page appropriately. 
     */
    protected void updatePageCompletion() {
        boolean pageComplete = determinePageCompletion();
        setPageComplete(pageComplete);
        if (pageComplete) {
            setErrorMessage(null);
        }
    }

    /**
     * Display an error dialog with the specified message.
     *
     * @param message the error message
     */
    private void displayErrorDialog(String message) {
        MessageDialog.open(MessageDialog.ERROR, getContainer().getShell(),
                getErrorDialogTitle(), message, SWT.SHEET);
    }

    /**
     * Display an error dislog with the information from the
     * supplied exception.
     * @param exception Throwable
     */
    private void displayErrorDialog(Throwable exception) {
        String message = exception.getMessage();
        //Some system exceptions have no message
        if (message == null) {
			message = NLS.bind(Messages.TeamProjectIndexExportWizardPage_errorInOperation, exception);
		}
        displayErrorDialog(message);
    }
}
