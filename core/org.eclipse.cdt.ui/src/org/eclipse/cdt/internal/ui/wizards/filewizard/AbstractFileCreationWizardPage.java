/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     IBM Corporation
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.filewizard;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.views.contentoutline.ContentOutline;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.PathUtil;

import org.eclipse.cdt.internal.corext.util.CModelUtil;

import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.preferences.CodeTemplatePreferencePage;
import org.eclipse.cdt.internal.ui.viewsupport.IViewPartInputProvider;
import org.eclipse.cdt.internal.ui.wizards.NewElementWizardPage;
import org.eclipse.cdt.internal.ui.wizards.SourceFolderSelectionDialog;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ComboDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.Separator;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringButtonDialogField;

public abstract class AbstractFileCreationWizardPage extends NewElementWizardPage {

	private static final int MAX_FIELD_CHARS = 50;
	private static final String NO_TEMPLATE = ""; //$NON-NLS-1$
	
	private IWorkspaceRoot fWorkspaceRoot;

	// field IDs
	private static final int SOURCE_FOLDER_ID = 1;
	protected static final int NEW_FILE_ID = 2;
	private static final int ALL_FIELDS = SOURCE_FOLDER_ID | NEW_FILE_ID;
	int fLastFocusedField = 0;
	private StringButtonDialogField fSourceFolderDialogField;
	private IStatus fSourceFolderStatus;
	private IStatus fNewFileStatus;
	private final IStatus STATUS_OK = new StatusInfo();
	
	/**
     * This flag isFirstTime is used to keep a note
     * that the file creation wizard has just been 
     * created.
     */
    private boolean isFirstTime = true;

	private Template[] fTemplates;

	private ComboDialogField fTemplateDialogField;
	
	public AbstractFileCreationWizardPage(String name) {
		super(name);

		setDescription(NewFileWizardMessages.AbstractFileCreationWizardPage_description); 
		
		fWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		SourceFolderFieldAdapter sourceFolderAdapter = new SourceFolderFieldAdapter();
		fSourceFolderDialogField = new StringButtonDialogField(sourceFolderAdapter);
		fSourceFolderDialogField.setDialogFieldListener(sourceFolderAdapter);
		fSourceFolderDialogField.setLabelText(NewFileWizardMessages.AbstractFileCreationWizardPage_sourceFolder_label); 
		fSourceFolderDialogField.setButtonLabel(NewFileWizardMessages.AbstractFileCreationWizardPage_sourceFolder_button); 

		fTemplates= getApplicableTemplates();
		if (fTemplates != null && fTemplates.length > 0) {
			fTemplateDialogField= new ComboDialogField(SWT.READ_ONLY);
			fTemplateDialogField.setLabelText(NewFileWizardMessages.AbstractFileCreationWizardPage_template_label);
		}

		fSourceFolderStatus = STATUS_OK;
		fNewFileStatus = STATUS_OK;
		fLastFocusedField = 0;
	}
	
	// -------- UI Creation ---------

	public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        
        Composite composite = new Composite(parent, SWT.NONE);
        int nColumns = 3;
        
        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());
        
        createSourceFolderControls(composite, nColumns);
        
        createFileControls(composite, nColumns - 1);
        // Placeholder for the right column.
        (new Composite(composite, SWT.NO_FOCUS)).setLayoutData(new GridData(1, 1));

        createTemplateControls(composite, nColumns);

		composite.layout();

		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
    }
	
	/**
	 * Creates a separator line. Expects a <code>GridLayout</code> with at least 1 column.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */
	protected void createSeparator(Composite composite, int nColumns) {
		(new Separator(SWT.SEPARATOR | SWT.HORIZONTAL)).doFillIntoGrid(composite, nColumns, convertHeightInCharsToPixels(1));		
	}

	/**
	 * Creates the necessary controls (label, text field and browse button) to edit
	 * the source folder location. The method expects that the parent composite
	 * uses a <code>GridLayout</code> as its layout manager and that the
	 * grid layout has at least 3 columns.
	 * 
	 * @param parent the parent composite
	 * @param nColumns the number of columns to span. This number must be
	 *  greater or equal three
	 */
	protected void createSourceFolderControls(Composite parent, int nColumns) {
		fSourceFolderDialogField.doFillIntoGrid(parent, nColumns);
		Text textControl = fSourceFolderDialogField.getTextControl(null);
		LayoutUtil.setWidthHint(textControl, getMaxFieldWidth());
		textControl.addFocusListener(new StatusFocusListener(SOURCE_FOLDER_ID));
	}
	
	/**
	 * Creates the controls for the file name field. Expects a <code>GridLayout</code> with at 
	 * least 2 columns.
	 * 
	 * @param parent the parent composite
	 * @param nColumns number of columns to span
	 */		
	protected abstract void createFileControls(Composite parent, int nColumns);

	/**
	 * Creates the controls for the file template field. Expects a <code>GridLayout</code> with at 
	 * least 3 columns.
	 * 
	 * @param parent the parent composite
	 * @param columns number of columns to span
	 */		
	protected void createTemplateControls(Composite parent, int columns) {
		if (fTemplateDialogField != null) {
			fTemplateDialogField.doFillIntoGrid(parent, columns - 1);
			Button configureButton= new Button(parent, SWT.PUSH);
			configureButton.setText(NewFileWizardMessages.AbstractFileCreationWizardPage_configure_label);
			configureButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
			configureButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					editTemplates();
				}
			});
			Combo comboControl= fTemplateDialogField.getComboControl(null);
			LayoutUtil.setWidthHint(comboControl, getMaxFieldWidth());
		}
	}

	protected void editTemplates() {
		String prefPageId= CodeTemplatePreferencePage.PREF_ID;
		Map<String, String> data= null;
		String templateName= null;
		Template template= getSelectedTemplate();
		if (template != null) {
			templateName= template.getName();
		}
		if (templateName != null) {
			data= new HashMap<String, String>();
			data.put(CodeTemplatePreferencePage.DATA_SELECT_TEMPLATE, templateName);
		}
		PreferenceDialog dialog= PreferencesUtil.createPreferenceDialogOn(getShell(), prefPageId, new String[] { prefPageId }, data);
		if (dialog.open() == Window.OK) {
			updateTemplates();
		}
	}

	protected void updateTemplates() {
		if (fTemplateDialogField != null) {
			Template selected= getSelectedTemplate();
			String name = selected != null ?
					selected.getName() :
					getDefaultTemplateName();
			fTemplates= getApplicableTemplates();
			int idx= NO_TEMPLATE.equals(name) ? 0 : 1;
			String[] names= new String[fTemplates.length + 1];
			for (int i = 0; i < fTemplates.length; i++) {
				names[i + 1]= fTemplates[i].getName();
				if (name != null && name.equals(names[i + 1])) {
					idx= i + 1;
				}
			}
			names[0]= NewFileWizardMessages.AbstractFileCreationWizardPage_noTemplate;
			fTemplateDialogField.setItems(names);
			fTemplateDialogField.selectItem(idx);
		}
	}

	/**
     * Configure the set of templates to select from.
	 * @return the set of templates
	 */
	protected abstract Template[] getApplicableTemplates();

	/**
	 * Returns the selected template and saves its name for future use.
	 *  
	 * @return the selected template or <code>null</code> if none.
	 */
	protected Template getTemplate() {
		Template template = getSelectedTemplate();
		saveLastUsedTemplateName(template != null ? template.getName() : NO_TEMPLATE);
		return template;
	}

	private Template getSelectedTemplate() {
		if (fTemplateDialogField != null) {
			int index= fTemplateDialogField.getSelectionIndex() - 1;
			if (index >= 0 && index < fTemplates.length) {
				return fTemplates[index];
			}
		}
		return null;
	}
	
    /**
     * The wizard owning this page is responsible for calling this method with the
     * current selection. The selection is used to initialize the fields of the wizard 
     * page.
     * 
     * @param selection used to initialize the fields
     */
    public void init(IStructuredSelection selection) {
    	ICElement celem = getInitialCElement(selection);
    	initFields(celem);
    	doStatusUpdate();
    }
	
    /**
     * Utility method to inspect a selection to find a C element. 
     * 
     * @param selection the selection to be inspected
     * @return a C element to be used as the initial selection, or <code>null</code>,
     * if no C element exists in the given selection
     */
    protected ICElement getInitialCElement(IStructuredSelection selection) {
    	ICElement celem = null;
    	if (selection != null && !selection.isEmpty()) {
    		Object selectedElement = selection.getFirstElement();
    		if (selectedElement instanceof IAdaptable) {
    			IAdaptable adaptable = (IAdaptable) selectedElement;			
    			
    			celem = (ICElement) adaptable.getAdapter(ICElement.class);
    			if (celem == null) {
    				IResource resource = (IResource) adaptable.getAdapter(IResource.class);
    				if (resource != null && resource.getType() != IResource.ROOT) {
    					while (celem == null && resource.getType() != IResource.PROJECT) {
    						celem = (ICElement) resource.getAdapter(ICElement.class);
    						resource = resource.getParent();
    					}
    					if (celem == null) {
    						celem = CoreModel.getDefault().create(resource); // c project
    					}
    				}
    			}
    		}
    	}
    	if (celem == null) {
    		IWorkbenchPart part = CUIPlugin.getActivePage().getActivePart();
    		if (part instanceof ContentOutline) {
    			part = CUIPlugin.getActivePage().getActiveEditor();
    		}
    		
    		if (part instanceof IViewPartInputProvider) {
    			Object elem = ((IViewPartInputProvider)part).getViewPartInput();
    			if (elem instanceof ICElement) {
    				celem = (ICElement) elem;
    			}
    		}

    		if (celem == null && part instanceof CEditor) {
		    	IEditorInput input = ((IEditorPart)part).getEditorInput();
		    	if (input != null) {
					final IResource res = (IResource) input.getAdapter(IResource.class);
					if (res != null && res instanceof IFile) {
					    celem = CoreModel.getDefault().create((IFile)res);
					}
		    	}
    		}
    	}
    
    	if (celem == null || celem.getElementType() == ICElement.C_MODEL) {
    		try {
    			ICProject[] projects = CoreModel.create(getWorkspaceRoot()).getCProjects();
    			if (projects.length == 1) {
    				celem = projects[0];
    			}
    		} catch (CModelException e) {
    			CUIPlugin.log(e);
    		}
    	}
    	return celem;
    }

	/**
     * Initializes all fields provided by the page with a given selection.
     * 
     * @param elem the selection used to initialize this page or <code>
     * null</code> if no selection was available
     */
    protected void initFields(ICElement elem) {
	    initSourceFolder(elem);
	    updateTemplates();
    	handleFieldChanged(ALL_FIELDS);
    }

    /**
     * Initializes the source folder field.
     * 
     * @param elem the C element used to compute the initial folder
     */
    protected void initSourceFolder(ICElement elem) {
    	ICContainer folder = null;
    	if (elem != null) {
    	    folder = CModelUtil.getSourceFolder(elem);
    		if (folder == null) {
    			ICProject cproject = elem.getCProject();
    			if (cproject != null) {
    				try {
    					if (cproject.exists()) {
    					    ISourceRoot[] roots = cproject.getSourceRoots();
    					    if (roots != null && roots.length > 0)
    					        folder = roots[0];
    					}
    				} catch (CModelException e) {
    					CUIPlugin.log(e);
    				}
    				if (folder == null) {
    				    folder = cproject.findSourceRoot(cproject.getResource());
    				}
    			}
    		}
    	}
	    setSourceFolderFullPath(folder != null ? folder.getResource().getFullPath() : null, false);
    }
	
     /**
	 * Returns the recommended maximum width for text fields (in pixels). This
	 * method requires that createContent has been called before this method is
	 * call. Subclasses may override to change the maximum width for text 
	 * fields.
	 * 
	 * @return the recommended maximum width for text fields.
	 */
	protected int getMaxFieldWidth() {
		return convertWidthInCharsToPixels(MAX_FIELD_CHARS);
	}

    /**
     * Returns the test selection of the current editor. <code>null</code> is returned
     * when the current editor does not have focus or does not return a text selection.
     * @return Returns the test selection of the current editor or <code>null</code>.
     *
     * @since 3.0 
     */
    protected ITextSelection getCurrentTextSelection() {
    	IWorkbenchPart part = CUIPlugin.getActivePage().getActivePart();
    	if (part instanceof IEditorPart) {
    		ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
    		if (selectionProvider != null) {
    			ISelection selection = selectionProvider.getSelection();
    			if (selection instanceof ITextSelection) {
    				return (ITextSelection) selection;
    			}
    		}
    	}
    	return null;
    }
	
	/**
	 * Sets the focus to the source folder's text field.
	 */	
	protected void setFocusOnSourceFolder() {
		fSourceFolderDialogField.setFocus();
	}

    protected final class StatusFocusListener implements FocusListener {
        private int fieldID;

        public StatusFocusListener(int fieldID) {
            this.fieldID = fieldID;
        }
        
        public void focusGained(FocusEvent e) {
            fLastFocusedField = this.fieldID;
            if (isFirstTime) {
            	isFirstTime = false;
            	return;
            }
        	doStatusUpdate();
        }
        
        public void focusLost(FocusEvent e) {
            fLastFocusedField = 0;
            doStatusUpdate();
        }
    }

    private class SourceFolderFieldAdapter implements IStringButtonAdapter, IDialogFieldListener {
		public void changeControlPressed(DialogField field) {
		    IPath oldFolderPath = getSourceFolderFullPath();
			IPath newFolderPath = chooseSourceFolder(oldFolderPath);
			if (newFolderPath != null) {
				setSourceFolderFullPath(newFolderPath, false);
				handleFieldChanged(ALL_FIELDS);
			}
		}
		
		public void dialogFieldChanged(DialogField field) {
			handleFieldChanged(ALL_FIELDS);
		}
	}
	
    // ----------- validation ----------
			
	/**
	 * This method is a hook which gets called after the source folder's
	 * text input field has changed. This default implementation updates
	 * the model and returns an error status. The underlying model
	 * is only valid if the returned status is OK.
	 * 
	 * @return the model's error status
	 */
	protected IStatus sourceFolderChanged() {
		StatusInfo status = new StatusInfo();
		
		IPath folderPath = getSourceFolderFullPath();
		if (folderPath == null) {
			status.setError(NewFileWizardMessages.AbstractFileCreationWizardPage_error_EnterSourceFolderName); 
			return status;
		}

		IResource res = fWorkspaceRoot.findMember(folderPath);
		if (res != null && res.exists()) {
			int resType = res.getType();
			if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
				IProject proj = res.getProject();
				if (!proj.isOpen()) {
					status.setError(NLS.bind(NewFileWizardMessages.AbstractFileCreationWizardPage_error_NotAFolder, folderPath)); 
					return status;
				}
			    if (!CoreModel.hasCCNature(proj) && !CoreModel.hasCNature(proj)) {
					if (resType == IResource.PROJECT) {
						status.setError(NewFileWizardMessages.AbstractFileCreationWizardPage_warning_NotACProject); 
						return status;
					}
					status.setWarning(NewFileWizardMessages.AbstractFileCreationWizardPage_warning_NotInACProject); 
				}
			    ICElement e = CoreModel.getDefault().create(res.getFullPath());
			    if (CModelUtil.getSourceFolder(e) == null) {
					status.setError(NLS.bind(NewFileWizardMessages.AbstractFileCreationWizardPage_error_NotASourceFolder, folderPath)); 
					return status;
				}
			} else {
				status.setError(NLS.bind(NewFileWizardMessages.AbstractFileCreationWizardPage_error_NotAFolder, folderPath)); 
				return status;
			}
		} else {
			status.setError(NLS.bind(NewFileWizardMessages.AbstractFileCreationWizardPage_error_FolderDoesNotExist, folderPath)); 
			return status;
		}

		return status;
	}
		
	/**
	 * Hook method that gets called when a field on this page has changed.
	 * 
	 * @param fields Bitwise-OR'd ids of the fields that changed.
	 */
	protected void handleFieldChanged(int fields) {
	    if (fields == 0)
	        return;	// no change

	    if (fieldChanged(fields, SOURCE_FOLDER_ID)) {
			fSourceFolderStatus = sourceFolderChanged();
	    }
	    if (fieldChanged(fields, NEW_FILE_ID)) {
	    	fNewFileStatus = fileNameChanged();
	    }
		doStatusUpdate();
	}

	private boolean fieldChanged(int fields, int fieldID) {
	    return ((fields & fieldID) != 0);
	}

	protected void doStatusUpdate() {
	    // do the last focused field first
	    IStatus lastStatus = getLastFocusedStatus();

	    // status of all used components
		IStatus[] status = new IStatus[] {
	        lastStatus,
			(fSourceFolderStatus != lastStatus) ? fSourceFolderStatus : STATUS_OK,
			(fNewFileStatus != lastStatus) ? fNewFileStatus : STATUS_OK,
		};
		
		// the mode severe status will be displayed and the ok button enabled/disabled.
		updateStatus(status);
	}

	private IStatus getLastFocusedStatus() {
	    switch (fLastFocusedField) {
	    	case SOURCE_FOLDER_ID:
	    	    return fSourceFolderStatus;
	    	case NEW_FILE_ID:
	    	    return fNewFileStatus;
    	   default:
               return STATUS_OK;
	    }
    }

	public IPath getSourceFolderFullPath() {
		String text = fSourceFolderDialogField.getText();
		if (text.length() > 0)
		    return new Path(text).makeAbsolute();
	    return null;
	}

	public void setSourceFolderFullPath(IPath folderPath, boolean update) {
		String str = (folderPath != null) ? folderPath.makeRelative().toString() : ""; //.makeRelative().toString(); //$NON-NLS-1$
		fSourceFolderDialogField.setTextWithoutUpdate(str);
		if (update) {
		    fSourceFolderDialogField.dialogFieldChanged();
		}
	}
	
	protected IProject getCurrentProject() {
	    IPath folderPath = getSourceFolderFullPath();
	    if (folderPath != null) {
	        return PathUtil.getEnclosingProject(folderPath);
	    }
	    return null;
	}

    /**
	 * Returns the workspace root.
	 * 
	 * @return the workspace root
	 */ 
	protected IWorkspaceRoot getWorkspaceRoot() {
		return fWorkspaceRoot;
	}	
	
	/*
	 * @see WizardPage#becomesVisible
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			setFocus();
		}
	}

	/**
	 * Sets the focus on the starting input field.
	 */		
	protected abstract void setFocus();
				
	IPath chooseSourceFolder(IPath initialPath) {
	    ICElement initElement = getSourceFolderFromPath(initialPath);
	    if (initElement instanceof ISourceRoot) {
	        ICProject cProject = initElement.getCProject();
	        ISourceRoot projRoot = cProject.findSourceRoot(cProject.getProject());
	        if (projRoot != null && projRoot.equals(initElement))
	            initElement = cProject;
	    }
		
		SourceFolderSelectionDialog dialog = new SourceFolderSelectionDialog(getShell());
		dialog.setInput(CoreModel.create(fWorkspaceRoot));
		dialog.setInitialSelection(initElement);
		
		if (dialog.open() == Window.OK) {
			Object result = dialog.getFirstResult();
			if (result instanceof ICElement) {
			    ICElement element = (ICElement)result;
				if (element instanceof ICProject) {
					ICProject cproject = (ICProject)element;
					ISourceRoot folder = cproject.findSourceRoot(cproject.getProject());
					if (folder != null)
					    return folder.getResource().getFullPath();
				}
				return element.getResource().getFullPath();
			}
		}
		return null;
	}

	private ICElement getSourceFolderFromPath(IPath path) {
	    if (path == null)
	        return null;
	    while (path.segmentCount() > 0) {
		    IResource res = fWorkspaceRoot.findMember(path);
			if (res != null && res.exists()) {
				int resType = res.getType();
				if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
				    ICElement elem = CoreModel.getDefault().create(res.getFullPath());
				    ICContainer sourceFolder = CModelUtil.getSourceFolder(elem);
				    if (sourceFolder != null)
				        return sourceFolder;
				    if (resType == IResource.PROJECT) {
				        return elem;
				    }
				}
			}
			path = path.removeLastSegments(1);
	    }
		return null;
	}

	/**
	 * Returns the full path computed from the file name field
	 * and the source folder.
	 * 
	 * @return the file path
	 */
	public abstract IPath getFileFullPath();
	
	/**
	 * Hook method that gets called when the file name has changed. The method validates the 
	 * file name and returns the status of the validation.
	 * 
	 * @return the status of the validation
	 */
	protected abstract IStatus fileNameChanged();

	/**
	 * Creates the new file using the entered field values.
	 * 
	 * @param monitor a progress monitor to report progress.
	 * @throws CoreException Thrown when the creation failed.
	 */
	public abstract void createFile(IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Returns the created file. The method only returns a valid translation unit 
	 * after <code>createFile</code> has been called.
	 * 
	 * @return the created translation unit
	 * @see #createFile(IProgressMonitor)
	 */			
	public abstract ITranslationUnit getCreatedFileTU();
	
	/**
	 * @return the name of the template used in the previous dialog invocation, or
	 * the name of the default template.
	 */
	public abstract String getDefaultTemplateName();
	
	/**
	 * Saves the name of the last used template.
	 * 
	 * @param name the name of a template, or an empty string for no template.
	 */
	public abstract void saveLastUsedTemplateName(String name);
}
