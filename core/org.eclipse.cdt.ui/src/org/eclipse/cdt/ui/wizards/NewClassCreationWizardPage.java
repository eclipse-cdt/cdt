/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     IBM Corporation
 *     Warren Paul (Nokia) - 174238
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import java.net.URI;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.cdt.core.browser.TypeUtil;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.PathUtil;

import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.internal.ui.wizards.NewElementWizardPage;
import org.eclipse.cdt.internal.ui.wizards.SourceFolderSelectionDialog;
import org.eclipse.cdt.internal.ui.wizards.classwizard.BaseClassInfo;
import org.eclipse.cdt.internal.ui.wizards.classwizard.BaseClassesListDialogField;
import org.eclipse.cdt.internal.ui.wizards.classwizard.ConstructorMethodStub;
import org.eclipse.cdt.internal.ui.wizards.classwizard.DestructorMethodStub;
import org.eclipse.cdt.internal.ui.wizards.classwizard.IBaseClassInfo;
import org.eclipse.cdt.internal.ui.wizards.classwizard.IMethodStub;
import org.eclipse.cdt.internal.ui.wizards.classwizard.MethodStubsListDialogField;
import org.eclipse.cdt.internal.ui.wizards.classwizard.NamespaceSelectionDialog;
import org.eclipse.cdt.internal.ui.wizards.classwizard.NewBaseClassSelectionDialog;
import org.eclipse.cdt.internal.ui.wizards.classwizard.NewClassCodeGenerator;
import org.eclipse.cdt.internal.ui.wizards.classwizard.NewClassWizardMessages;
import org.eclipse.cdt.internal.ui.wizards.classwizard.NewClassWizardPrefs;
import org.eclipse.cdt.internal.ui.wizards.classwizard.NewClassWizardUtil;
import org.eclipse.cdt.internal.ui.wizards.classwizard.SourceFileSelectionDialog;
import org.eclipse.cdt.internal.ui.wizards.classwizard.NewBaseClassSelectionDialog.ITypeSelectionListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.Separator;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.cdt.internal.ui.wizards.filewizard.NewSourceFileGenerator;

public class NewClassCreationWizardPage extends NewElementWizardPage {
    protected final static String PAGE_NAME = "NewClassWizardPage"; //$NON-NLS-1$
    protected static final int MAX_FIELD_CHARS = 50;
	
	// field IDs
    protected static final int SOURCE_FOLDER_ID = 1;
    protected static final int NAMESPACE_ID = 2;
    protected static final int CLASS_NAME_ID = 4;
    protected static final int BASE_CLASSES_ID = 8;
    protected static final int METHOD_STUBS_ID = 16;
    protected static final int HEADER_FILE_ID = 32;
    protected static final int SOURCE_FILE_ID = 64;
//    protected static final int TEST_FILE_ID = 128; XXX
    protected static final int ALL_FIELDS = SOURCE_FOLDER_ID | NAMESPACE_ID
            | CLASS_NAME_ID | BASE_CLASSES_ID | METHOD_STUBS_ID
            | HEADER_FILE_ID | SOURCE_FILE_ID/* | TEST_FILE_ID*/;
	protected int fLastFocusedField = 0;

    protected StringButtonDialogField fSourceFolderDialogField;
    protected SelectionButtonDialogField fNamespaceSelection;
    protected StringButtonDialogField fNamespaceDialogField;
    protected StringDialogField fClassNameDialogField;
    protected BaseClassesListDialogField fBaseClassesDialogField;
    protected MethodStubsListDialogField fMethodStubsDialogField;
    protected SelectionButtonDialogField fUseDefaultSelection;
    protected StringButtonDialogField fHeaderFileDialogField;
    protected StringButtonDialogField fSourceFileDialogField;
//    protected StringButtonDialogField fTestFileDialogField; XXX

	protected IStatus fSourceFolderStatus;
	protected IStatus fNamespaceStatus;
	protected IStatus fClassNameStatus;
	protected IStatus fBaseClassesStatus;
	protected IStatus fMethodStubsStatus;
	protected IStatus fHeaderFileStatus;
	protected IStatus fSourceFileStatus;
	protected final IStatus STATUS_OK = new StatusInfo();

    protected IFile fCreatedSourceFile;
    protected IFile fCreatedHeaderFile;
    protected ICElement fCreatedClass;
    
    /**
     * This flag isFirstTime is used to keep a note
     * that the class creation wizard has just been 
     * created.
     */
    private boolean isFirstTime = false;
    /**
     * the default method stubs
     */
    protected static final IMethodStub[] DEFAULT_METHOD_STUBS = {
        new ConstructorMethodStub(),
        new DestructorMethodStub()
    };
    
    
	/**
	 * Constructor for NewClassCreationWizardPage
	 */
	public NewClassCreationWizardPage() {
		super(PAGE_NAME);
		setTitle(NewClassWizardMessages.NewClassCreationWizardPage_title);
		setDescription(NewClassWizardMessages.NewClassCreationWizardPage_description);
		
		SourceFolderFieldAdapter sourceFolderAdapter = new SourceFolderFieldAdapter();
		fSourceFolderDialogField = new StringButtonDialogField(sourceFolderAdapter);
		fSourceFolderDialogField.setDialogFieldListener(sourceFolderAdapter);
		fSourceFolderDialogField.setLabelText(NewClassWizardMessages.NewClassCreationWizardPage_sourceFolder_label);
		fSourceFolderDialogField.setButtonLabel(NewClassWizardMessages.NewClassCreationWizardPage_sourceFolder_button);

		NamespaceFieldAdapter namespaceAdapter = new NamespaceFieldAdapter();
		fNamespaceSelection = new SelectionButtonDialogField(SWT.CHECK);
		fNamespaceSelection.setDialogFieldListener(namespaceAdapter);
		fNamespaceSelection.setLabelText(NewClassWizardMessages.NewClassCreationWizardPage_namespace_label);

		fNamespaceDialogField = new StringButtonDialogField(namespaceAdapter);
		fNamespaceDialogField.setDialogFieldListener(namespaceAdapter);
		fNamespaceDialogField.setButtonLabel(NewClassWizardMessages.NewClassCreationWizardPage_namespace_button);

		ClassNameFieldAdapter classAdapter = new ClassNameFieldAdapter();
		fClassNameDialogField = new StringDialogField();
		fClassNameDialogField.setDialogFieldListener(classAdapter);
		fClassNameDialogField.setLabelText(NewClassWizardMessages.NewClassCreationWizardPage_className_label);
		
		BaseClassesFieldAdapter baseClassesAdapter = new BaseClassesFieldAdapter();
		fBaseClassesDialogField = new BaseClassesListDialogField(NewClassWizardMessages.NewClassCreationWizardPage_baseClasses_label, baseClassesAdapter);
		
		MethodStubsFieldAdapter methodStubsAdapter = new MethodStubsFieldAdapter();
		fMethodStubsDialogField = new MethodStubsListDialogField(NewClassWizardMessages.NewClassCreationWizardPage_methodStubs_label, methodStubsAdapter);
	    
		FileGroupFieldAdapter fileGroupAdapter = new FileGroupFieldAdapter();
		fUseDefaultSelection = new SelectionButtonDialogField(SWT.CHECK);
		fUseDefaultSelection.setDialogFieldListener(fileGroupAdapter);
		fUseDefaultSelection.setLabelText(NewClassWizardMessages.NewClassCreationWizardPage_useDefaultLocation_label);
		fHeaderFileDialogField = new StringButtonDialogField(fileGroupAdapter);
		fHeaderFileDialogField.setDialogFieldListener(fileGroupAdapter);
		fHeaderFileDialogField.setLabelText(NewClassWizardMessages.NewClassCreationWizardPage_headerFile_label);
		fHeaderFileDialogField.setButtonLabel(NewClassWizardMessages.NewClassCreationWizardPage_headerFile_button);
		fSourceFileDialogField = new StringButtonDialogField(fileGroupAdapter);
		fSourceFileDialogField.setDialogFieldListener(fileGroupAdapter);
		fSourceFileDialogField.setLabelText(NewClassWizardMessages.NewClassCreationWizardPage_sourceFile_label);
		fSourceFileDialogField.setButtonLabel(NewClassWizardMessages.NewClassCreationWizardPage_sourceFile_button);
		
		fSourceFolderStatus = STATUS_OK;
		fNamespaceStatus = STATUS_OK;
		fClassNameStatus = STATUS_OK;
		fBaseClassesStatus = STATUS_OK;
		fMethodStubsStatus = STATUS_OK;
		fHeaderFileStatus = STATUS_OK;
		fSourceFileStatus = STATUS_OK;
		fLastFocusedField = 0;
		
		isFirstTime = true;
		
		updateNamespaceEnableState();
		updateFileGroupEnableState();
	}
	
	// -------- UI Creation ---------

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        
        Composite composite = new Composite(parent, SWT.NONE);
        int nColumns = 4;
        
        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());
        
        createSourceFolderControls(composite, nColumns);
        createNamespaceControls(composite, nColumns);
        
        createSeparator(composite, nColumns);
        
        createClassNameControls(composite, nColumns);
        createBaseClassesControls(composite, nColumns);
        createMethodStubsControls(composite, nColumns);
        
        createSeparator(composite, nColumns);
        
        createFileControls(composite, nColumns);
        
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
	 * Creates the controls for the namespace field. Expects a <code>GridLayout</code> with at 
	 * least 4 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */		
	protected void createNamespaceControls(Composite composite, int nColumns) {
		Composite tabGroup= new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
 		tabGroup.setLayout(layout);

		fNamespaceSelection.doFillIntoGrid(tabGroup, 1);

		Text textControl= fNamespaceDialogField.getTextControl(composite);
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= getMaxFieldWidth();
		gd.horizontalSpan= 2;
		textControl.setLayoutData(gd);
		textControl.addFocusListener(new StatusFocusListener(NAMESPACE_ID));
		
		Button button= fNamespaceDialogField.getChangeControl(composite);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.widthHint = SWTUtil.getButtonWidthHint(button);
		button.setLayoutData(gd);
	}	

	/**
	 * Creates the controls for the class name field. Expects a <code>GridLayout</code> with at 
	 * least 2 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */		
	protected void createClassNameControls(Composite composite, int nColumns) {
		fClassNameDialogField.doFillIntoGrid(composite, nColumns - 1);
		DialogField.createEmptySpace(composite);
		Text textControl = fClassNameDialogField.getTextControl(null);
		LayoutUtil.setWidthHint(textControl, getMaxFieldWidth());
		textControl.addFocusListener(new StatusFocusListener(CLASS_NAME_ID));
	}

	/**
	 * Creates the controls for the base classes field. Expects a <code>GridLayout</code> with 
	 * at least 3 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */			
	protected void createBaseClassesControls(Composite composite, int nColumns) {
	    fBaseClassesDialogField.doFillIntoGrid(composite, nColumns);
	    Control listControl = fBaseClassesDialogField.getListControl(null);
		GridData gd = (GridData) listControl.getLayoutData();
		gd.heightHint = convertHeightInCharsToPixels(5);
		gd.grabExcessVerticalSpace = false;
		gd.widthHint = getMaxFieldWidth();
		listControl.addFocusListener(new StatusFocusListener(BASE_CLASSES_ID));
	}
	
	/**
	 * Creates the controls for the method stubs field. Expects a <code>GridLayout</code> with 
	 * at least 4 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */			
	protected void createMethodStubsControls(Composite composite, int nColumns) {
		fMethodStubsDialogField.doFillIntoGrid(composite, nColumns);
	    Control listControl = fMethodStubsDialogField.getListControl(null);
		GridData gd = (GridData) listControl.getLayoutData();
		gd.heightHint = convertHeightInCharsToPixels(5);
		gd.grabExcessVerticalSpace = false;
		gd.widthHint = getMaxFieldWidth();
		listControl.addFocusListener(new StatusFocusListener(METHOD_STUBS_ID));
	}
	
	/**
	 * Creates the controls for the file name fields. Expects a <code>GridLayout</code> with at 
	 * least 4 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */		
	protected void createFileControls(Composite composite, int nColumns) {
		Composite tabGroup = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
 		tabGroup.setLayout(layout);

		fUseDefaultSelection.doFillIntoGrid(tabGroup, 1);
		
		LayoutUtil.setHorizontalSpan(fHeaderFileDialogField.getLabelControl(composite), 1);
		Text textControl = fHeaderFileDialogField.getTextControl(composite);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = getMaxFieldWidth();
		gd.horizontalSpan = 1;
		textControl.setLayoutData(gd);
		textControl.addFocusListener(new StatusFocusListener(HEADER_FILE_ID));
		
		Button button = fHeaderFileDialogField.getChangeControl(composite);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.widthHint = SWTUtil.getButtonWidthHint(button);
		button.setLayoutData(gd);

		DialogField.createEmptySpace(composite, 1);
		
		LayoutUtil.setHorizontalSpan(fSourceFileDialogField.getLabelControl(composite), 1);
		textControl = fSourceFileDialogField.getTextControl(composite);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = getMaxFieldWidth();
		gd.horizontalSpan = 1;
		textControl.setLayoutData(gd);
		textControl.addFocusListener(new StatusFocusListener(SOURCE_FILE_ID));
		
		button = fSourceFileDialogField.getChangeControl(composite);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.widthHint = SWTUtil.getButtonWidthHint(button);
		button.setLayoutData(gd);
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
        
        String namespace = null;
        if (celem != null) {
            ICElement ns = NewClassWizardUtil.getNamespace(celem);
            if (ns != null) {
                namespace = TypeUtil.getFullyQualifiedName(ns).toString();
                if (namespace != null && namespace.length() == 0) {
                    namespace = null;
                }
            }
        }
        setNamespaceText(namespace, false);
        setNamespaceSelection(namespace != null, true);
        
        IPath folderPath = null;
        if (celem != null) {
            ICContainer folder = NewClassWizardUtil.getSourceFolder(celem);
            if (folder == null) {
                ICProject cproject = celem.getCProject();
                if (cproject != null) {
                    folder = NewClassWizardUtil.getFirstSourceRoot(cproject);
                }
            }
            if (folder != null) {
                folderPath = folder.getResource().getFullPath();
            }
        }
        setSourceFolderFullPath(folderPath, false);
    
        String className = null;
        ITextSelection textSel = getEditorTextSelection();
        if (textSel != null) {
            String text = textSel.getText();
            if (text != null && text.length() > 0 &&
            		CConventions.validateClassName(text).isOK()) {
                className = text;
            }
        }
        setClassName(className, false);
        
        IMethodStub[] stubs = getDefaultMethodStubs();
        for (int i = 0; i < stubs.length; ++i) {
            addMethodStub(stubs[i], true);
        }
        //TODO restore dialog settings for method stubs?
        // IDialogSettings section = getDialogSettings().getSection(PAGE_NAME);
        // if (section != null) {
        //      enabled = section.getBoolean(stubName);
        // }
        
        setUseDefaultSelection(true, true);

        handleFieldChanged(ALL_FIELDS);
    }
    
    /**
     * Attempts to extract a C Element from the initial selection.
     * 
     * @param selection the initial selection
     * @return a C Element, or <code>null</code> if not available
     */
    protected ICElement getInitialCElement(IStructuredSelection selection) {
        ICElement celem = NewClassWizardUtil.getCElementFromSelection(selection);
        if (celem == null) {
            celem = NewClassWizardUtil.getCElementFromEditor();
        }
        if (celem == null || celem.getElementType() == ICElement.C_MODEL) {
            try {
                ICProject[] projects = CoreModel.create(NewClassWizardUtil.getWorkspaceRoot()).getCProjects();
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
     * Returns the text selection of the current editor. <code>null</code> is returned
     * when the current editor does not have focus or does not return a text selection.
     *
     * @return the selection of the current editor, or <code>null</code>.
     */
    protected ITextSelection getEditorTextSelection() {
        IWorkbenchPage page = CUIPlugin.getActivePage();
        if (page != null) {
        	IWorkbenchPart part = page.getActivePart();
        	if (part instanceof IEditorPart) {
        		ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        		if (selectionProvider != null) {
        			ISelection selection = selectionProvider.getSelection();
        			if (selection instanceof ITextSelection) {
        				return (ITextSelection) selection;
        			}
        		}
        	}
        }
    	return null;
    }
	
    /**
     * Returns the method stubs to display in the wizard.
     * 
     * @return array of method stubs
     */
    protected IMethodStub[] getDefaultMethodStubs() {
        return DEFAULT_METHOD_STUBS;
    }
    
    /**
     * Returns the text entered into the source folder input field.
     * 
     * @return the source folder
     */
    public String getSourceFolderText() {
        return fSourceFolderDialogField.getText();
    }
    
    /**
     * Sets the text of the source folder input field.
     * 
     * @param folder the folder name
     * @param update <code>true</code> if the dialog should be updated
     */ 
    public void setSourceFolderText(String folder, boolean update) {
        fSourceFolderDialogField.setTextWithoutUpdate(folder != null ? folder : ""); //$NON-NLS-1$
        if (update) {
            fSourceFolderDialogField.dialogFieldChanged();
        }
    }
    
    /**
     * Returns the current source folder as a path.
     * 
     * @return the source folder path
     */
    protected IPath getSourceFolderFullPath() {
        String text = getSourceFolderText();
        if (text.length() > 0)
            return new Path(text).makeAbsolute();
        return null;
    }
    
    /**
     * Sets the source folder from the given path.
     * 
     * @param folderPath the source folder path
     * @param update <code>true</code> if the dialog should be updated
     */
    protected void setSourceFolderFullPath(IPath folderPath, boolean update) {
        String str = (folderPath != null) ? folderPath.makeRelative().toString() : ""; //.makeRelative().toString(); //$NON-NLS-1$
        setSourceFolderText(str, update);
    }
    
    /**
     * Returns the current project, based on the current source folder.
     * 
     * @return the current project
     */
    protected ICProject getCurrentProject() {
        IPath folderPath = getSourceFolderFullPath();
        if (folderPath != null) {
            return toCProject(PathUtil.getEnclosingProject(folderPath));
        }
        return null;
    }

	private ICProject toCProject(IProject enclosingProject) {
		if (enclosingProject != null)
			return CoreModel.getDefault().create(enclosingProject);
		return null;
	}

    /**
     * Returns the text entered into the namespace input field.
     * 
     * @return the namespace
     */
    public String getNamespaceText() {
        return fNamespaceDialogField.getText();
    }

    /**
     * Sets the text of the namespace input field.
     * 
     * @param namespace the namespace name
     * @param update <code>true</code> if the dialog should be updated
     */ 
    public void setNamespaceText(String namespace, boolean update) {
        fNamespaceDialogField.setTextWithoutUpdate(namespace != null ? namespace : ""); //$NON-NLS-1$
        if (update) {
            fNamespaceDialogField.dialogFieldChanged();
        }
    }
    
    /**
     * Returns the selection state of the namespace checkbox.
     * 
     * @return the selection state of the namespace checkbox
     */
    public boolean isNamespaceSelected() {
        return fNamespaceSelection.isSelected();
    }
    
    /**
     * Sets the namespace checkbox's selection state.
     * 
     * @param isSelected the checkbox's selection state
     * @param canBeModified if <code>true</code> the checkbox is
     * modifiable; otherwise it is read-only.
     */
    public void setNamespaceSelection(boolean isSelected, boolean canBeModified) {
        fNamespaceSelection.setSelection(isSelected);
        fNamespaceSelection.setEnabled(canBeModified);
        updateNamespaceEnableState();
    }
    
    /**
     * Updates the enable state of the namespace button.
     */
    private void updateNamespaceEnableState() {
        fNamespaceDialogField.setEnabled(isNamespaceSelected());
    }
    
    /**
     * Returns the class name entered into the class input field.
     * 
     * @return the class name
     */
    public String getClassName() {
        return fClassNameDialogField.getText();
    }

    /**
     * Sets the text of the class name input field.
     * 
     * @param name the new class name
     * @param update <code>true</code> if the dialog should be updated
     */ 
    public void setClassName(String name, boolean update) {
        fClassNameDialogField.setTextWithoutUpdate(name != null ? name : ""); //$NON-NLS-1$
        if (update) {
            fClassNameDialogField.dialogFieldChanged();
        }
    }
    
    /**
     * Returns the currently selected (checked) method stubs.
     * 
     * @return array of <code>IMethodStub</code> or empty array if none selected.
     */
    protected IMethodStub[] getSelectedMethodStubs() {
        return fMethodStubsDialogField.getCheckedMethodStubs();
    }

    /**
     * Adds a method stub to the method stubs field.
     * @param methodStub the method stub to add
     * @param selected <code>true</code> if the stub is initially selected (checked)
     */
    protected void addMethodStub(IMethodStub methodStub, boolean selected) {
        fMethodStubsDialogField.addMethodStub(methodStub, selected);
    }

    /**
     * Returns the contents of the base classes field.
     * 
     * @return array of <code>IBaseClassInfo</code>
     */
    protected IBaseClassInfo[] getBaseClasses() {
        List<IBaseClassInfo> classesList = fBaseClassesDialogField.getElements();
        return classesList.toArray(new IBaseClassInfo[classesList.size()]);
    }
    
    /**
     * Adds a base class to the base classes field.
     * @param newBaseClass the new base class
     * @param access the access visibility (public/private/protected)
     * @param isVirtual <code>true</code> if the inheritance is virtual
     */
    protected void addBaseClass(ITypeInfo newBaseClass, ASTAccessVisibility access, boolean isVirtual) {
        // check if already exists
        List<IBaseClassInfo> baseClasses = fBaseClassesDialogField.getElements();
        if (baseClasses != null) {
            for (IBaseClassInfo baseClassInfo : baseClasses) {
                BaseClassInfo info = (BaseClassInfo) baseClassInfo;
                if (info.getType().equals(newBaseClass)) {
                    // already added
                    return;
                }
            }
        }

        if (verifyBaseClasses()) {
            NewClassWizardUtil.resolveClassLocation(newBaseClass, getContainer());
        }

        fBaseClassesDialogField.addBaseClass(new BaseClassInfo(newBaseClass, access, isVirtual));
    }

    /**
     * Returns the selection state of the file group checkbox.
     * 
     * @return the selection state of the file group checkbox
     */
    public boolean isUseDefaultSelected() {
        return fUseDefaultSelection.isSelected();
    }
    
    /**
     * Sets the use default checkbox's selection state.
     * 
     * @param isSelected the checkbox's selection state
     * @param canBeModified if <code>true</code> the checkbox is
     * modifiable; otherwise it is read-only.
     */
    public void setUseDefaultSelection(boolean isSelected, boolean canBeModified) {
        fUseDefaultSelection.setSelection(isSelected);
        fUseDefaultSelection.setEnabled(canBeModified);
        updateFileGroupEnableState();
    }

    /**
     * Updates the enable state of buttons related to the use default checkbox.
     */
    private void updateFileGroupEnableState() {
        boolean filegroup = !isUseDefaultSelected();
        fHeaderFileDialogField.setEnabled(filegroup);
        fSourceFileDialogField.setEnabled(filegroup);
    }
    
    /**
     * Returns the text entered into the header file input field.
     * 
     * @return the header file
     */
    public String getHeaderFileText() {
        return fHeaderFileDialogField.getText();
    }
    
    /**
     * Sets the text of the header file input field.
     * 
     * @param header the header file name
     * @param update <code>true</code> if the dialog should be updated
     */ 
    public void setHeaderFileText(String header, boolean update) {
        fHeaderFileDialogField.setTextWithoutUpdate(header != null ? header : ""); //$NON-NLS-1$
        if (update) {
            fHeaderFileDialogField.dialogFieldChanged();
        }
    }
    
    /**
     * Returns the current header file as a path.
     * 
     * @return the header file path
     */
    protected IPath getHeaderFileFullPath() {
        String str = getHeaderFileText();
        IPath path = null;
        if (str.length() > 0) {
            path = new Path(str);
            if (!path.isAbsolute()) {
                IPath folderPath = getSourceFolderFullPath();
                if (folderPath != null)
                    path = folderPath.append(path);
            }
        }
        return path;
    }
    
    /**
     * Sets the header file from the given path.
     * 
     * @param path the header file path
     * @param update <code>true</code> if the dialog should be updated
     */
    protected void setHeaderFileFullPath(IPath path, boolean update) {
        String str = null;
        if (path != null) {
            IPath sourceFolder = getSourceFolderFullPath();
            if (sourceFolder != null) {
                IPath relativePath = PathUtil.makeRelativePath(path, sourceFolder);
                if (relativePath != null)
                    path = relativePath;
            }
            str = path.makeRelative().toString();
        }
        setHeaderFileText(str, update);
    }


    /**
     * Returns the text entered into the source file input field.
     * 
     * @return the source file
     */
    public String getSourceFileText() {
        return fSourceFileDialogField.getText();
    }
    
    /**
     * Sets the text of the source file input field.
     * 
     * @param source the source file name
     * @param update <code>true</code> if the dialog should be updated
     */ 
    public void setSourceFileText(String source, boolean update) {
        fSourceFileDialogField.setTextWithoutUpdate(source != null ? source : ""); //$NON-NLS-1$
        if (update) {
            fSourceFileDialogField.dialogFieldChanged();
        }
    }
    
    /**
     * Returns the current source file as a path.
     * 
     * @return the source file path
     */
    protected IPath getSourceFileFullPath() {
        String str = getSourceFileText();
        IPath path = null;
        if (str.length() > 0) {
            path = new Path(str);
            if (!path.isAbsolute()) {
                IPath folderPath = getSourceFolderFullPath();
                if (folderPath != null)
                    path = folderPath.append(path);
            }
        }
        return path;
    }
    
    /**
     * Sets the source file from the given path.
     * 
     * @param path the source file path
     * @param update <code>true</code> if the dialog should be updated
     */
    protected void setSourceFileFullPath(IPath path, boolean update) {
        String str = null;
        if (path != null) {
            IPath sourceFolder = getSourceFolderFullPath();
            if (sourceFolder != null) {
                IPath relativePath = PathUtil.makeRelativePath(path, sourceFolder);
                if (relativePath != null)
                    path = relativePath;
            }
            str = path.makeRelative().toString();
        }
        setSourceFileText(str, update);
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
     * Sets the focus on the class name input field.
     */     
    protected void setFocus() {
        fClassNameDialogField.setFocus();
    }
    
    // ----------- UI Validation ----------
    
    /**
     * Causes doStatusUpdate() to be called whenever the focus changes.
     * Remembers the last focused field.
     */
    private final class StatusFocusListener implements FocusListener {
        private int fieldID;

        public StatusFocusListener(int fieldID) {
            this.fieldID = fieldID;
        }

        public void focusGained(FocusEvent e) {
            if (fLastFocusedField != this.fieldID) {
                fLastFocusedField = this.fieldID;
            	if( isFirstTime ) {
            		isFirstTime = false;
            		return;
            	}
                doStatusUpdate();
            }
        }

        public void focusLost(FocusEvent e) {
            if (fLastFocusedField != 0) {
                fLastFocusedField = 0;
                doStatusUpdate();
            }
        }
    }

    /**
     * handles changes to the source folder field
     */
    private final class SourceFolderFieldAdapter implements IStringButtonAdapter, IDialogFieldListener {
		public void changeControlPressed(DialogField field) {
		    IPath oldFolderPath = getSourceFolderFullPath();
			IPath newFolderPath = chooseSourceFolder(oldFolderPath);
			if (newFolderPath != null) {
				IPath headerPath = getHeaderFileFullPath();
				IPath sourcePath = getSourceFileFullPath();
				setSourceFolderFullPath(newFolderPath, false);
			    if (!isUseDefaultSelected()) {
					if (oldFolderPath != null && oldFolderPath.matchingFirstSegments(newFolderPath) == 0) {
					    if (headerPath != null) {
					        headerPath = newFolderPath.append(headerPath.lastSegment());
					    }
					    if (sourcePath != null) {
					        sourcePath = newFolderPath.append(sourcePath.lastSegment());
					    }
					}
				    // adjust the relative paths
				    setHeaderFileFullPath(headerPath, false);
				    setSourceFileFullPath(sourcePath, false);
			    }
				handleFieldChanged(SOURCE_FOLDER_ID|ALL_FIELDS);
			}
		}
		
		public void dialogFieldChanged(DialogField field) {
			handleFieldChanged(SOURCE_FOLDER_ID|ALL_FIELDS);
		}
	}
    
    private IPath chooseSourceFolder(IPath initialPath) {
        ICElement initElement = NewClassWizardUtil.getSourceFolder(initialPath);
        if (initElement instanceof ISourceRoot) {
            ICProject cProject = initElement.getCProject();
            ISourceRoot projRoot = cProject.findSourceRoot(cProject.getProject());
            if (projRoot != null && projRoot.equals(initElement))
                initElement = cProject;
        }
        
        SourceFolderSelectionDialog dialog = new SourceFolderSelectionDialog(getShell());
        dialog.setInput(CoreModel.create(NewClassWizardUtil.getWorkspaceRoot()));
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
	
    /**
     * handles changes to the namespace field
     */
    private final class NamespaceFieldAdapter implements IStringButtonAdapter, IDialogFieldListener {
		public void changeControlPressed(DialogField field) {
	        ITypeInfo ns = chooseNamespace();
		    if (ns != null) {
		        int changedFields = NAMESPACE_ID|CLASS_NAME_ID;
		        IPath oldFolderPath = getSourceFolderFullPath();
		        if (oldFolderPath == null) {
					IPath headerPath = getHeaderFileFullPath();
					IPath sourcePath = getSourceFileFullPath();
		            IPath newFolderPath = updateSourceFolderFromPath(ns.getEnclosingProject().getProject().getFullPath());
			        if (newFolderPath != null) {
			            changedFields |= SOURCE_FOLDER_ID | HEADER_FILE_ID | SOURCE_FILE_ID;
					    setSourceFolderFullPath(newFolderPath, false);
					    // adjust the relative paths
					    setHeaderFileFullPath(headerPath, false);
					    setSourceFileFullPath(sourcePath, false);
			        }
		        }
		        setNamespaceText(ns.getQualifiedTypeName().toString(), false);
				handleFieldChanged(changedFields);
	        }
		}
		
		public void dialogFieldChanged(DialogField field) {
	        updateNamespaceEnableState();
			handleFieldChanged(NAMESPACE_ID|CLASS_NAME_ID);
		}
	}
    
    private IPath updateSourceFolderFromPath(IPath filePath) {
        ICElement folder = NewClassWizardUtil.getSourceFolder(filePath);
        if (folder instanceof ISourceRoot) {
            ICProject cProject = folder.getCProject();
            ISourceRoot projRoot = cProject.findSourceRoot(cProject.getProject());
            if (projRoot != null && projRoot.equals(folder))
                folder = cProject;
        }
        if (folder != null) {
            return folder.getPath();
        }
        IProject proj = PathUtil.getEnclosingProject(filePath);
        if (proj != null)
            return proj.getFullPath(); 
        return null;
    }
    
    private ITypeInfo chooseNamespace() {
        ITypeSearchScope scope;
        ICProject project = getCurrentProject();
        if (project != null) {
            scope = new TypeSearchScope(project);
        } else {
            scope = new TypeSearchScope(true);
        }

        ITypeInfo[] elements = AllTypesCache.getNamespaces(scope, false);
        if (elements == null || elements.length == 0) {
            String title = NewClassWizardMessages.NewClassCreationWizardPage_getTypes_noNamespaces_title;
            String message = NewClassWizardMessages.NewClassCreationWizardPage_getTypes_noNamespaces_message;
            MessageDialog.openInformation(getShell(), title, message);
            return null;
        }
        
        NamespaceSelectionDialog dialog = new NamespaceSelectionDialog(getShell());
        dialog.setElements(elements);
        int result = dialog.open();
        if (result == IDialogConstants.OK_ID) {
            return (ITypeInfo) dialog.getFirstResult();
        }
        
        return null;
    }
    
    /**
     * handles changes to the class name field
     */
	private final class ClassNameFieldAdapter implements IDialogFieldListener {
		public void dialogFieldChanged(DialogField field) {
		    int changedFields = CLASS_NAME_ID;
		    if (isUseDefaultSelected()) {
				updateFilesFromClassName(fClassNameDialogField.getText());
				changedFields |= (HEADER_FILE_ID|SOURCE_FILE_ID);
		    }
			handleFieldChanged(changedFields);
		}
	}
    
    /**
     * handles changes to the base classes field
     */
	private final class BaseClassesFieldAdapter implements IListAdapter<IBaseClassInfo> {
        public void customButtonPressed(ListDialogField<IBaseClassInfo> field, int index) {
            if (index == 0) {
                chooseBaseClasses();
            }
            handleFieldChanged(BASE_CLASSES_ID);
        }

        public void selectionChanged(ListDialogField<IBaseClassInfo> field) {
        }

        public void doubleClicked(ListDialogField<IBaseClassInfo> field) {
        }
    }
    
    private void chooseBaseClasses() {
        ITypeInfo[] elements = NewClassWizardUtil.getReachableClasses(getCurrentProject());
        if (elements == null || elements.length == 0) {
            String title = NewClassWizardMessages.NewClassCreationWizardPage_getTypes_noClasses_title;
            String message = NewClassWizardMessages.NewClassCreationWizardPage_getTypes_noClasses_message;
            MessageDialog.openInformation(getShell(), title, message);
            return;
        }
        
        List<IBaseClassInfo> oldContents = fBaseClassesDialogField.getElements();
        NewBaseClassSelectionDialog dialog = new NewBaseClassSelectionDialog(getShell());
        dialog.addListener(new ITypeSelectionListener() {
            public void typeAdded(ITypeInfo newBaseClass) {
                addBaseClass(newBaseClass, ASTAccessVisibility.PUBLIC, false);
            }
        });
        dialog.setElements(elements);
        int result = dialog.open();
        if (result != IDialogConstants.OK_ID) {
            // restore the old contents
            fBaseClassesDialogField.setElements(oldContents);
        }
    }
    
    /**
     * handles changes to the method stubs field
     */
	private final class MethodStubsFieldAdapter implements IListAdapter<IMethodStub> {

        public void customButtonPressed(ListDialogField<IMethodStub> field, int index) {
        }

        public void selectionChanged(ListDialogField<IMethodStub> field) {
        }

        public void doubleClicked(ListDialogField<IMethodStub> field) {
        }
    }

    /**
     * handles changes to the file name fields
     */
    private final class FileGroupFieldAdapter implements IStringButtonAdapter, IDialogFieldListener {
		public void changeControlPressed(DialogField field) {
		    IPath filePath = null;
			IPath headerPath = getHeaderFileFullPath();
			IPath sourcePath = getSourceFileFullPath();
		    if (field == fHeaderFileDialogField) {
		        filePath = chooseHeaderFile();
		        if (filePath != null) {
		            headerPath = filePath;
		        }
		    } else if (field == fSourceFileDialogField) {
				filePath = chooseSourceFile();
		        if (filePath != null) {
		            sourcePath = filePath;
		        }
		    }
		    if (filePath != null) {
		        IPath folderPath = null;
			    int changedFields = 0;
	            int headerSegments = 0;
	            int sourceSegments = 0;
	            if (headerPath != null)
	                headerSegments = filePath.matchingFirstSegments(headerPath);
	            if (sourcePath != null)
	                sourceSegments = filePath.matchingFirstSegments(sourcePath);
	            int segments = Math.min(headerSegments, sourceSegments);
	            if (segments > 0) {
	                IPath newFolderPath = filePath.uptoSegment(segments);
		            folderPath = updateSourceFolderFromPath(newFolderPath);
	            }
		        if (folderPath != null) {
		            changedFields |= SOURCE_FOLDER_ID | HEADER_FILE_ID | SOURCE_FILE_ID;
				    // adjust the relative paths
		            setSourceFolderFullPath(folderPath, false);
				    setHeaderFileFullPath(headerPath, false);
				    setSourceFileFullPath(sourcePath, false);
		        }
			    if (field == fHeaderFileDialogField) {
		            setHeaderFileFullPath(filePath, false);
		            changedFields |= HEADER_FILE_ID;
		        } else if (field == fSourceFileDialogField) {
		            setSourceFileFullPath(filePath, false);
		            changedFields |= SOURCE_FILE_ID;
		        }
				handleFieldChanged(changedFields);
		    }
		}
		
		public void dialogFieldChanged(DialogField field) {
		    int changedFields = 0;
		    if (field == fUseDefaultSelection) {
		        boolean enabled = !isUseDefaultSelected();
		        fHeaderFileDialogField.setEnabled(enabled);
		        fSourceFileDialogField.setEnabled(enabled);
		        if (!enabled) {
					updateFilesFromClassName(fClassNameDialogField.getText());
		        }
		        changedFields = HEADER_FILE_ID | SOURCE_FILE_ID;
			    updateFileGroupEnableState();
			    handleFieldChanged(SOURCE_FOLDER_ID);
		    }
		    if (field == fHeaderFileDialogField) {
	            changedFields |= HEADER_FILE_ID;
	        } else if (field == fSourceFileDialogField) {
	            changedFields |= SOURCE_FILE_ID;
	        }
			handleFieldChanged(changedFields);
		}
	}
    
    private IPath chooseHeaderFile() {
        SourceFileSelectionDialog dialog = new SourceFileSelectionDialog(getShell());
        dialog.setTitle(NewClassWizardMessages.NewClassCreationWizardPage_ChooseHeaderFileDialog_title);
        ICElement input = CoreModel.create(NewClassWizardUtil.getWorkspaceRoot());
        ICProject project = getCurrentProject();
        if (project != null)
            input = project;
        dialog.setInput(input);

        IPath filePath = getHeaderFileFullPath();
        if (filePath != null) {
            String folderName = filePath.removeLastSegments(1).toString();
            String fileName = filePath.lastSegment();
            dialog.setInitialSelection(folderName, fileName);
        } else {
            filePath = getSourceFolderFullPath();
            if (filePath != null) {
                dialog.setInitialSelection(filePath.toString(), null);
            }
        }
        
        if (dialog.open() == Window.OK) {
            return dialog.getFilePath();
        }
        return null;
    }   

    private IPath chooseSourceFile() {
        SourceFileSelectionDialog dialog = new SourceFileSelectionDialog(getShell());
        dialog.setTitle(NewClassWizardMessages.NewClassCreationWizardPage_ChooseSourceFileDialog_title);
        ICElement input = CoreModel.create(NewClassWizardUtil.getWorkspaceRoot());
        ICProject project = getCurrentProject();
        if (project != null)
            input = project;
        dialog.setInput(input);

        IPath filePath = getSourceFileFullPath();
        if (filePath != null) {
            String folderName = filePath.removeLastSegments(1).toString();
            String fileName = filePath.lastSegment();
            dialog.setInitialSelection(folderName, fileName);
        } else {
            filePath = getSourceFolderFullPath();
            if (filePath != null) {
                dialog.setInitialSelection(filePath.toString(), null);
            }
        }
        
        if (dialog.open() == Window.OK) {
            return dialog.getFilePath();
        }
        return null;
    }   
    
    /**
     * update header and source file fields from the class name
     */
    private void updateFilesFromClassName(String className) {
        String headerName = null;
        String sourceName = null;
        IPath folder = getSourceFolderFullPath();
        if (className != null && className.length() > 0) {
            String[] names = generateFileNames(className, folder);
            if (names != null && names.length == 2) {
                headerName = names[0];
                sourceName = names[1];
            }
        }
        fHeaderFileDialogField.setTextWithoutUpdate(headerName != null ? headerName : ""); //$NON-NLS-1$
        fSourceFileDialogField.setTextWithoutUpdate(sourceName != null ? sourceName : ""); //$NON-NLS-1$
    }
    
    private static final int MAX_UNIQUE_CLASSNAME = 99;

    /**
     * Returns the names of the header file and source file which will be
     * used when this class is created, e.g. "MyClass" -> ["MyClass.h","MyClass.cpp"]
     * Note: the file names should be unique to avoid overwriting existing files.
     * 
     * @param className the class name
     * @param folder the folder where the files are to be created, or <code>null</code>
     * @return an array of 2 Strings, containing the header file name and
     * source file name, respectively.
     */
    protected String[] generateFileNames(String className, IPath folder) {
        String headerName = null;
        String sourceName = null;
        
        if (folder == null) {
            headerName = NewSourceFileGenerator.generateHeaderFileNameFromClass(className);
            sourceName = NewSourceFileGenerator.generateSourceFileNameFromClass(className);
        } else {
            // make sure the file names are unique
            String currName = className;
            int count = 0;
            String separator = ""; //$NON-NLS-1$
            if (Character.isDigit(className.charAt(className.length() - 1)))
                separator = "_"; //$NON-NLS-1$
            while (count < MAX_UNIQUE_CLASSNAME) {
                String header = NewSourceFileGenerator.generateHeaderFileNameFromClass(currName);
                IPath path = folder.append(header);
                if (!path.toFile().exists()) {
                    String source = NewSourceFileGenerator.generateSourceFileNameFromClass(currName);
                    path = folder.append(source);
                    if (!path.toFile().exists()) {
                        headerName = header;
                        sourceName = source;
                        // we're done
                        break;
                    }
                }
                ++count;
                currName = className + separator + count; 
            }
        }
        
        return new String[] { headerName, sourceName };
    }
    
    /**
     * Hook method that gets called when a field on this page has changed.
     * 
     * @param fields Bitwise-OR'd ids of the fields that changed.
     */
    protected void handleFieldChanged(int fields) {
        if (fields == 0)
            return; // no change

        if (fieldChanged(fields, SOURCE_FOLDER_ID)) {
            fSourceFolderStatus = sourceFolderChanged();
        }
        if (fieldChanged(fields, NAMESPACE_ID)) {
            fNamespaceStatus = namespaceChanged();
        }
        if (fieldChanged(fields, CLASS_NAME_ID)) {
            fClassNameStatus = classNameChanged();
        }
        if (fieldChanged(fields, BASE_CLASSES_ID)) {
            fBaseClassesStatus = baseClassesChanged();
        }
        if (fieldChanged(fields, METHOD_STUBS_ID)) {
            fMethodStubsStatus = methodStubsChanged();
        }
        if (fieldChanged(fields, HEADER_FILE_ID)) {
            fHeaderFileStatus = headerFileChanged();
        }
        if (fieldChanged(fields, SOURCE_FILE_ID)) {
            fSourceFileStatus = sourceFileChanged();
        }
        doStatusUpdate();
    }

    private boolean fieldChanged(int fields, int fieldID) {
        return ((fields & fieldID) != 0);
    }

    /**
     * Updates the status line and the ok button according to the status of the fields
     * on the page. The most severe error is taken, with the last-focused field being
     * evaluated first. 
     */
    protected void doStatusUpdate() {
        // do the last focused field first
        IStatus lastStatus = getLastFocusedStatus();

        final boolean isClassNameWarning = fClassNameStatus.getSeverity() == IStatus.WARNING;
        // status of all used components
		IStatus[] status = new IStatus[] {
            lastStatus,
            (fSourceFolderStatus != lastStatus) ? fSourceFolderStatus : STATUS_OK,
            (fNamespaceStatus != lastStatus) ? fNamespaceStatus : STATUS_OK,

            // give priority to file-level warnings over
        	// class name warnings
            (fHeaderFileStatus != lastStatus && isClassNameWarning) ? fHeaderFileStatus : STATUS_OK,
            (fSourceFileStatus != lastStatus && isClassNameWarning) ? fSourceFileStatus : STATUS_OK,
                    
            (fClassNameStatus != lastStatus) ? fClassNameStatus : STATUS_OK,
            (fBaseClassesStatus != lastStatus) ? fBaseClassesStatus : STATUS_OK,
            (fMethodStubsStatus != lastStatus) ? fMethodStubsStatus : STATUS_OK,
            (fSourceFileStatus != lastStatus) ? fSourceFileStatus : STATUS_OK,
            (fHeaderFileStatus != lastStatus) ? fHeaderFileStatus : STATUS_OK,
        };
        
        // the mode severe status will be displayed and the ok button enabled/disabled.
        updateStatus(status);
    }

    /**
     * Returns the status of the last field which had focus.
     * 
     * @return status of the last field which had focus
     */
    protected IStatus getLastFocusedStatus() {
        switch (fLastFocusedField) {
            case SOURCE_FOLDER_ID:
                return fSourceFolderStatus;
            case NAMESPACE_ID:
                return fNamespaceStatus;
            case CLASS_NAME_ID:
                return fClassNameStatus;
            case BASE_CLASSES_ID:
                return fBaseClassesStatus;
            case METHOD_STUBS_ID:
                return fMethodStubsStatus;
            case HEADER_FILE_ID:
                return fHeaderFileStatus;
            case SOURCE_FILE_ID:
                return fSourceFileStatus;
           default:
               return STATUS_OK;
        }
    }
    
    /**
     * Hook method that gets called when the source folder has changed. The method validates the 
     * source folder and returns the status of the validation.
     * 
     * @return the status of the validation
     */
	protected IStatus sourceFolderChanged() {
		StatusInfo status = new StatusInfo();
		
		IPath folderPath = getSourceFolderFullPath();
		if (folderPath == null) {
			status.setError(NewClassWizardMessages.NewClassCreationWizardPage_error_EnterSourceFolderName);
			return status;
		}

		IResource res = NewClassWizardUtil.getWorkspaceRoot().findMember(folderPath);
		if (res != null && res.exists()) {
			int resType = res.getType();
			if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
				IProject proj = res.getProject();
				if (!proj.isOpen()) {
					status.setError(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_error_NotAFolder, folderPath));
					return status;
				}
			    if (!CoreModel.hasCCNature(proj) && !CoreModel.hasCNature(proj)) {
					if (resType == IResource.PROJECT) {
						status.setError(NewClassWizardMessages.NewClassCreationWizardPage_warning_NotACProject);
						return status;
					}
					status.setWarning(NewClassWizardMessages.NewClassCreationWizardPage_warning_NotInACProject);
				}
			    if (NewClassWizardUtil.getSourceFolder(res) == null) {
			    	if (isUseDefaultSelected()) {
						status.setError(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_error_NotASourceFolder, folderPath));
						return status;
			    	}
				}
			} else {
				status.setError(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_error_NotAFolder, folderPath));
				return status;
			}
		} else {
			status.setError(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_error_FolderDoesNotExist, folderPath));
			return status;
		}

		return status;
	}
		
	/**
     * Hook method that gets called when the namespace has changed. The method validates the 
     * namespace and returns the status of the validation.
     * 
     * @return the status of the validation
     */
	protected IStatus namespaceChanged() {
		StatusInfo status = new StatusInfo();
		if (!isNamespaceSelected()) {
		    return status;
		}

		// must not be empty
        String namespace = getNamespaceText();
		if (namespace == null || namespace.length() == 0) {
			status.setError(NewClassWizardMessages.NewClassCreationWizardPage_error_EnterNamespace);
			return status;
		}

		IStatus val = CConventions.validateNamespaceName(namespace);
		if (val.getSeverity() == IStatus.ERROR) {
			status.setError(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_error_InvalidNamespace, val.getMessage()));
			return status;
		} else if (val.getSeverity() == IStatus.WARNING) {
			status.setWarning(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_warning_NamespaceDiscouraged, val.getMessage()));
		}

	 	IQualifiedTypeName typeName = new QualifiedTypeName(namespace);
		ICProject project = getCurrentProject();

		if (project != null) {
			/* search for parent name space first */
			int searchResult;
			if (typeName.isQualified()) {
				searchResult = NewClassWizardUtil.searchForCppType(typeName.getEnclosingTypeName(),project, ICPPNamespace.class);
				if (searchResult != NewClassWizardUtil.SEARCH_MATCH_FOUND_EXACT) {
					status.setError(NewClassWizardMessages.NewClassCreationWizardPage_error_EnclosingNamespaceNotExists);
					return status;
				}
			}
			searchResult = NewClassWizardUtil.searchForCppType(typeName, project, ICPPNamespace.class);
			switch(searchResult) {
			case NewClassWizardUtil.SEARCH_MATCH_FOUND_EXACT:
				status.setOK();
				return status;				
			case NewClassWizardUtil.SEARCH_MATCH_FOUND_EXACT_ANOTHER_TYPE:
				status.setWarning(NewClassWizardMessages.NewClassCreationWizardPage_error_TypeMatchingNamespaceExists);
				return status;
			case NewClassWizardUtil.SEARCH_MATCH_FOUND_ANOTHER_NAMESPACE:
				status.setWarning(NewClassWizardMessages.NewClassCreationWizardPage_error_NamespaceExistsDifferentCase);
				return status;
			case NewClassWizardUtil.SEARCH_MATCH_FOUND_ANOTHER_TYPE:
				status.setWarning(NewClassWizardMessages.NewClassCreationWizardPage_error_TypeMatchingNamespaceExistsDifferentCase);
				return status;
			case NewClassWizardUtil.SEARCH_MATCH_NOTFOUND:
				status.setWarning(NewClassWizardMessages.NewClassCreationWizardPage_warning_NamespaceNotExists);
				break;
			}
	    }

	    val = CConventions.validateNamespaceName(typeName.lastSegment());
		if (val.getSeverity() == IStatus.ERROR) {
			status.setError(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_error_InvalidNamespace, val.getMessage()));
			return status;
		}
	    return status;
	}
	
	/**
	 * Hook method that gets called when the class name has changed. The method validates the 
	 * class name and returns the status of the validation.
	 * 
	 * @return the status of the validation
	 */
	protected IStatus classNameChanged() {
	    StatusInfo status = new StatusInfo();
	    
	    String className = getClassName();
		// must not be empty
		if (className == null || className.length() == 0) {
			status.setError(NewClassWizardMessages.NewClassCreationWizardPage_error_EnterClassName);
			return status;
		}

        IQualifiedTypeName typeName = new QualifiedTypeName(className);
        if (typeName.isQualified()) {
            status.setError(NewClassWizardMessages.NewClassCreationWizardPage_error_QualifiedClassName);
            return status;
        }
    
		IStatus val = CConventions.validateClassName(className);
		if (val.getSeverity() == IStatus.ERROR) {
			status.setError(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_error_InvalidClassName, val.getMessage()));
			return status;
		} else if (val.getSeverity() == IStatus.WARNING) {
			status.setWarning(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_warning_ClassNameDiscouraged, val.getMessage()));
			// continue checking
		}
	
	    ICProject project = getCurrentProject();
	    if (project != null) {
	    	
		    IQualifiedTypeName fullyQualifiedName = typeName;
			if (isNamespaceSelected()) {
                String namespace = getNamespaceText();
                if (namespace != null && namespace.length() > 0) {
			        fullyQualifiedName = new QualifiedTypeName(namespace).append(typeName);
			    }
			}
			int searchResult = NewClassWizardUtil.searchForCppType(fullyQualifiedName, project, ICPPClassType.class);
			switch(searchResult) {
			case NewClassWizardUtil.SEARCH_MATCH_FOUND_EXACT:
				status.setError(NewClassWizardMessages.NewClassCreationWizardPage_error_ClassNameExists);
				return status;				
			case NewClassWizardUtil.SEARCH_MATCH_FOUND_EXACT_ANOTHER_TYPE:
				status.setError(NewClassWizardMessages.NewClassCreationWizardPage_error_TypeMatchingClassExists);
				return status;
			case NewClassWizardUtil.SEARCH_MATCH_FOUND_ANOTHER_NAMESPACE:
				status.setWarning(NewClassWizardMessages.NewClassCreationWizardPage_error_ClassNameExistsDifferentCase);
				return status;
			case NewClassWizardUtil.SEARCH_MATCH_FOUND_ANOTHER_TYPE:
				status.setWarning(NewClassWizardMessages.NewClassCreationWizardPage_error_TypeMatchingClassExistsDifferentCase);
				return status;
			case NewClassWizardUtil.SEARCH_MATCH_NOTFOUND:
				break;
			}	
	    }
		return status;
	}
    
 	/**
	 * Hook method that gets called when the list of base classes has changed. The method 
	 * validates the base classes and returns the status of the validation.
	 * 
	 * @return the status of the validation
	 */
	protected IStatus baseClassesChanged() {
        if (verifyBaseClasses()) {
			IPath folder = getSourceFolderFullPath();
            ICProject project = getCurrentProject();
			if (project != null) {
                IBaseClassInfo[] baseClasses = getBaseClasses();
                // make sure all classes belong to the project
                if (baseClasses != null && baseClasses.length > 0) {
                    IStatus status = baseClassesChanged(project, folder, baseClasses);
                    if (status.isMultiStatus()) {
                        // we only want to show the most severe error
                        return StatusUtil.getMostSevere(status.getChildren());
                    }
                    return status;
                }
            }
        }
		return Status.OK_STATUS;
	}
    
    /**
     * This method validates the base classes by searching through the project's
     * include paths and checking if each base class is reachable.
     * 
     * @param project the current project
     * @param sourceFolder the current source folder
     * @param baseClasses an array of base classes
     * 
     * @return the status of the validation
     */
    protected IStatus baseClassesChanged(ICProject project, IPath sourceFolder, IBaseClassInfo[] baseClasses) {
        MultiStatus status = new MultiStatus(CUIPlugin.getPluginId(), IStatus.OK, "", null); //$NON-NLS-1$
        IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project.getProject());
        if (provider != null) {
            //TODO get the scanner info for the actual source folder
            IScannerInfo info = provider.getScannerInformation(project.getProject());
            if (info != null) {
                String[] includePaths = info.getIncludePaths();
                for (int i = 0; i < baseClasses.length; ++i) {
                    IBaseClassInfo baseClass = baseClasses[i];
                    ITypeInfo baseType = baseClass.getType();
                    StatusInfo baseClassStatus = new StatusInfo();
                    if (!NewClassWizardUtil.isTypeReachable(baseType, project, includePaths)) {
                        baseClassStatus.setError(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_error_BaseClassNotExistsInProject, baseType.getQualifiedTypeName().toString()));
                    }
                    status.add(baseClassStatus);
                }
            }
        }
        return status;
    }

    /**
     * Checks if the base classes need to be verified (ie they must exist in the project)
     * 
     * @return <code>true</code> if the base classes should be verified
     */
    public boolean verifyBaseClasses() {
        return NewClassWizardPrefs.verifyBaseClasses();
    }
    
    /**
     * Hook method that gets called when the list of method stubs has changed. The method 
     * validates the method stubs and returns the status of the validation.
     * 
     * @return the status of the validation
     */
	protected IStatus methodStubsChanged() {
        // do nothing
        return Status.OK_STATUS;
	}

    /**
     * Hook method that gets called when the header file has changed. The method 
     * validates the header file and returns the status of the validation.
     * 
     * @return the status of the validation
     */
	protected IStatus headerFileChanged() {
		StatusInfo status = new StatusInfo();
		
		IPath path = getHeaderFileFullPath();
		if (path == null && isUseDefaultSelected()) {
			return status;
		}
		if (path == null) {
			status.setError(NewClassWizardMessages.NewClassCreationWizardPage_error_EnterHeaderFileName);
			return status;
		}
		
		IPath sourceFolderPath = getSourceFolderFullPath();
		if (sourceFolderPath == null || !sourceFolderPath.isPrefixOf(path)) {
			status.setError(NewClassWizardMessages.NewClassCreationWizardPage_error_HeaderFileNotInSourceFolder);
			return status;
		}
		
		// Make sure the file location is under a source root
		if (NewClassWizardUtil.getSourceFolder(path) == null) {
			status.setError(NewClassWizardMessages.NewClassCreationWizardPage_error_HeaderFileNotInSourceFolder);
			return status;
		}
		
		boolean fileExists = false;
		// check if file already exists
		IResource file = NewClassWizardUtil.getWorkspaceRoot().getFile(path);
    	if (file.getType() == IResource.FILE) {
    		if (!file.exists()) {
				URI location = file.getLocationURI();
				try {
					IFileStore store = EFS.getStore(location);
					fileExists = store.fetchInfo().exists();
				} catch (CoreException e) {
					status.setError(NewClassWizardMessages.NewClassCreationWizardPage_error_LocationUnknown);
					return status;
				}
    		} else {
    			fileExists = true;
    		}
    		
			IProject proj = file.getProject();
			if (!proj.isOpen()) {
				status.setError(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_error_NotAFile, path));
				return status;
			}

		    if (!CoreModel.hasCCNature(proj) && !CoreModel.hasCNature(proj)) {
				status.setWarning(NewClassWizardMessages.NewClassCreationWizardPage_warning_NotInACProject);
			} else if (fileExists) {
			    status.setWarning(NewClassWizardMessages.NewClassCreationWizardPage_warning_HeaderFileExists);
			}
    	} else {
    		status.setError(NewClassWizardMessages.NewClassCreationWizardPage_error_NotAFile);
    		return status;
    	}
		
		// check if folder exists
		IPath folderPath = path.removeLastSegments(1).makeRelative();
		IResource folder = NewClassWizardUtil.getWorkspaceRoot().findMember(folderPath);
		if (folder == null || !folder.exists() || (folder.getType() != IResource.PROJECT && folder.getType() != IResource.FOLDER)) {
			status.setError(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_error_FolderDoesNotExist, folderPath));
			return status;
		}

		if (!fileExists) {
			IStatus val = CConventions.validateHeaderFileName(getCurrentProject().getProject(), path.lastSegment());
			if (val.getSeverity() == IStatus.ERROR) {
				status.setError(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_error_InvalidHeaderFileName, val.getMessage()));
				return status;
			} else if (val.getSeverity() == IStatus.WARNING) {
				status.setWarning(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_warning_HeaderFileNameDiscouraged, val.getMessage()));
			}
		}
		return status;
	}

    /**
     * Hook method that gets called when the source file has changed. The method 
     * validates the source file and returns the status of the validation.
     * 
     * @return the status of the validation
     */
	protected IStatus sourceFileChanged() {
		StatusInfo status = new StatusInfo();
		
		IPath path = getSourceFileFullPath();
		if (path == null) {
			status.setError(NewClassWizardMessages.NewClassCreationWizardPage_error_EnterSourceFileName);
			return status;
		}
		
		IPath sourceFolderPath = getSourceFolderFullPath();
		if (sourceFolderPath == null || !sourceFolderPath.isPrefixOf(path)) {
			status.setError(NewClassWizardMessages.NewClassCreationWizardPage_error_SourceFileNotInSourceFolder);
			return status;
		}
		
		// Make sure the file location is under a source root
		if (NewClassWizardUtil.getSourceFolder(path) == null) {
			status.setError(NewClassWizardMessages.NewClassCreationWizardPage_error_SourceFileNotInSourceFolder);
			return status;
		}
		
		boolean fileExists = false;
		// check if file already exists
		IResource file = NewClassWizardUtil.getWorkspaceRoot().getFile(path);
    	if (file.getType() == IResource.FILE) {
    		if (!file.exists()) {
				URI location = file.getLocationURI();
				try {
					IFileStore store = EFS.getStore(location);
					fileExists = store.fetchInfo().exists();
				} catch (CoreException e) {
					status.setError(NewClassWizardMessages.NewClassCreationWizardPage_error_LocationUnknown);
					return status;
				}
    		} else {
    			fileExists = true;
    		}
    		
			IProject proj = file.getProject();
			if (!proj.isOpen()) {
				status.setError(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_error_NotAFile, path));
				return status;
			}

		    if (!CoreModel.hasCCNature(proj) && !CoreModel.hasCNature(proj)) {
				status.setWarning(NewClassWizardMessages.NewClassCreationWizardPage_warning_NotInACProject);
			} else if (fileExists) {
			    status.setWarning(NewClassWizardMessages.NewClassCreationWizardPage_warning_SourceFileExists);
			}
    	} else {
    		status.setError(NewClassWizardMessages.NewClassCreationWizardPage_error_NotAFile);
    		return status;
    	}
		
		// check if folder exists
		IPath folderPath = path.removeLastSegments(1).makeRelative();
		IResource folder = NewClassWizardUtil.getWorkspaceRoot().findMember(folderPath);
		if (folder == null || !folder.exists() || (folder.getType() != IResource.PROJECT && folder.getType() != IResource.FOLDER)) {
			status.setError(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_error_FolderDoesNotExist, folderPath));
			return status;
		}

		if (!fileExists) {
			IStatus val = CConventions.validateSourceFileName(getCurrentProject().getProject(), path.lastSegment());
			if (val.getSeverity() == IStatus.ERROR) {
				status.setError(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_error_InvalidSourceFileName, val.getMessage()));
				return status;
			} else if (val.getSeverity() == IStatus.WARNING) {
				status.setWarning(NLS.bind(NewClassWizardMessages.NewClassCreationWizardPage_warning_SourceFileNameDiscouraged, val.getMessage()));
			}
		}
		return status;
	}
    
    // -------- Code Generation ---------
    
	/**
	 * Creates the new class using the entered field values.
	 * 
	 * @param monitor a progress monitor to report progress.
	 * @throws CoreException Thrown when the creation failed.
	 * @throws InterruptedException Thrown when the operation was cancelled.
	 */
	public void createClass(IProgressMonitor monitor) throws CoreException, InterruptedException {
        fCreatedClass = null;
        fCreatedHeaderFile = null;
        fCreatedSourceFile = null;
        
        createClass(
                getCanonicalPath(getHeaderFileFullPath()),
                getCanonicalPath(getSourceFileFullPath()),
                getClassName(),
                isNamespaceSelected() ? getNamespaceText() : null,
                getBaseClasses(),
                getSelectedMethodStubs(), monitor);
	}
    
    private IPath getCanonicalPath(IPath path) throws CoreException {
    	IWorkspaceRoot root = NewClassWizardUtil.getWorkspaceRoot();
    	IFile file = root.getFile(path);
    	URI location = file.getLocationURI();
    	URI canonicalLocation = EFS.getStore(location).toURI();
    	IFile[] files = root.findFilesForLocationURI(canonicalLocation);
    	if (files.length > 0) {
    		return files[0].getFullPath();
    	}
    	return null;
	}

	/**
     * Returns whether the generated header and source files should be
     * opened in editors after the finish button is pressed.
     * 
     * @return <code>true</code> if the header and source file should be
     * displayed
     */
    public boolean openClassInEditor() {
        return NewClassWizardPrefs.openClassInEditor();
    }
    
    /**
     * Creates a new class.
     * 
     * @param headerPath the header file path
     * @param sourcePath the source file path
     * @param className the class name
     * @param namespace the namespace
     * @param baseClasses array of base classes
     * @param methodStubs array of method stubs
     * @param monitor a progress monitor
     * @throws CoreException if the creation failed
     * @throws InterruptedException if the operation was cancelled
     */
    protected void createClass(IPath headerPath, IPath sourcePath, String className, String namespace, IBaseClassInfo[] baseClasses, IMethodStub[] methodStubs, IProgressMonitor monitor) throws CoreException, InterruptedException {
        NewClassCodeGenerator generator = new NewClassCodeGenerator(
                headerPath,
                sourcePath,
                className,
                namespace,
                baseClasses,
                methodStubs);
        generator.setForceSourceFileCreation(true);
        generator.createClass(monitor);
        
        fCreatedClass = generator.getCreatedClass();
        fCreatedHeaderFile = generator.getCreatedHeaderFile();
        fCreatedSourceFile = generator.getCreatedSourceFile();
    }
	
	/**
	 * Returns the created class. The method only returns a valid class 
	 * after <code>createClass</code> has been called.
	 * 
	 * @return the created class
	 * @see #createClass(IProgressMonitor)
	 */			
	public ICElement getCreatedClass() {
        return fCreatedClass;
	}
    
    /**
     * Returns the created header file. The method only returns a valid file 
     * after <code>createClass</code> has been called.
     *
     * @return the created header file
     * @see #createClass(IProgressMonitor)
     */         
    public IFile getCreatedHeaderFile() {
        return fCreatedHeaderFile;
    }
	
   /**
    * Returns the created source file. The method only returns a valid file 
    * after <code>createClass</code> has been called.
    * 
    * @return the created source file
    * @see #createClass(IProgressMonitor)
    */         
    public IFile getCreatedSourceFile() {
        return fCreatedSourceFile;
    }
}
