/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.cdt.core.browser.TypeUtil;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.internal.ui.viewsupport.IViewPartInputProvider;
import org.eclipse.cdt.internal.ui.wizards.NewElementWizardPage;
import org.eclipse.cdt.internal.ui.wizards.classwizard.NewBaseClassSelectionDialog.ITypeSelectionListener;
import org.eclipse.cdt.internal.ui.wizards.classwizard.NewClassCodeGenerator.CodeGeneratorException;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.Separator;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.cdt.ui.CUIPlugin;
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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;

public class NewClassCreationWizardPage extends NewElementWizardPage {

    private static final int NAMESPACE_INDEX = 0;
    private static final int CLASS_INDEX = 1;
    private final static String PAGE_NAME = "NewClassWizardPage"; //$NON-NLS-1$
	private static final int MAX_FIELD_CHARS = 50;
	
	private IWorkspaceRoot fWorkspaceRoot;

	// field IDs
	private static final int SOURCE_FOLDER_ID = 1;
	private static final int ENCLOSING_TYPE_ID = 2;
	private static final int CLASS_NAME_ID = 4;
	private static final int BASE_CLASSES_ID = 8;
	private static final int METHOD_STUBS_ID = 16;
	private static final int HEADER_FILE_ID = 32;
	private static final int SOURCE_FILE_ID = 64;
	private static final int ALL_FIELDS = SOURCE_FOLDER_ID | ENCLOSING_TYPE_ID
            | CLASS_NAME_ID | BASE_CLASSES_ID | METHOD_STUBS_ID
            | HEADER_FILE_ID | SOURCE_FILE_ID;
	protected int fLastFocusedField = 0;

	private StringButtonDialogField fSourceFolderDialogField;
	SelectionButtonDialogField fEnclosingTypeSelection;
	SelectionButtonDialogFieldGroup fEnclosingTypeButtons;
	StringButtonDialogField fEnclosingTypeDialogField;
	SelectionButtonDialogFieldGroup fEnclosingClassAccessButtons;
	StringDialogField fClassNameDialogField;
	BaseClassesListDialogField fBaseClassesDialogField;
	MethodStubsListDialogField fMethodStubsDialogField;
	SelectionButtonDialogField fUseDefaultSelection;
	StringButtonDialogField fHeaderFileDialogField;
	StringButtonDialogField fSourceFileDialogField;

	protected IStatus fSourceFolderStatus;
	protected IStatus fEnclosingTypeStatus;
	protected IStatus fClassNameStatus;
	protected IStatus fBaseClassesStatus;
	protected IStatus fMethodStubsStatus;
	protected IStatus fHeaderFileStatus;
	protected IStatus fSourceFileStatus;
	protected final IStatus STATUS_OK = new StatusInfo();

	private NewClassCodeGenerator fCodeGenerator = null;

	//TODO this should be a prefs option
	private boolean fWarnIfBaseClassNotInPath = false;
	
	public NewClassCreationWizardPage() {
		super(PAGE_NAME);

		setDescription(NewClassWizardMessages.getString("NewClassCreationWizardPage.description")); //$NON-NLS-1$
		
		fWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		fCodeGenerator = null;
		
		SourceFolderFieldAdapter sourceFolderAdapter = new SourceFolderFieldAdapter();
		fSourceFolderDialogField = new StringButtonDialogField(sourceFolderAdapter);
		fSourceFolderDialogField.setDialogFieldListener(sourceFolderAdapter);
		fSourceFolderDialogField.setLabelText(NewClassWizardMessages.getString("NewClassCreationWizardPage.sourceFolder.label")); //$NON-NLS-1$
		fSourceFolderDialogField.setButtonLabel(NewClassWizardMessages.getString("NewClassCreationWizardPage.sourceFolder.button")); //$NON-NLS-1$

		EnclosingTypeFieldAdapter enclosingTypeAdapter = new EnclosingTypeFieldAdapter();
		fEnclosingTypeSelection = new SelectionButtonDialogField(SWT.CHECK);
		fEnclosingTypeSelection.setDialogFieldListener(enclosingTypeAdapter);
		fEnclosingTypeSelection.setLabelText(NewClassWizardMessages.getString("NewClassCreationWizardPage.enclosingType.label")); //$NON-NLS-1$

		String[] buttonNames = new String[] {
	    	/* NAMESPACE_INDEX */ NewClassWizardMessages.getString("NewClassCreationWizardPage.enclosingType.namespace"), //$NON-NLS-1$
	    	/* CLASS_INDEX */ NewClassWizardMessages.getString("NewClassCreationWizardPage.enclosingType.class"), //$NON-NLS-1$
		};
		fEnclosingTypeButtons = new SelectionButtonDialogFieldGroup(SWT.RADIO, buttonNames, buttonNames.length);
		fEnclosingTypeButtons.setDialogFieldListener(enclosingTypeAdapter);
		fEnclosingTypeButtons.setSelection(NAMESPACE_INDEX, true);

		String[] buttonNames2 = new String[] {
			NewClassWizardMessages.getString("NewClassCreationWizardPage.enclosingClassAccess.public"), //$NON-NLS-1$
			NewClassWizardMessages.getString("NewClassCreationWizardPage.enclosingClassAccess.protected"), //$NON-NLS-1$
			NewClassWizardMessages.getString("NewClassCreationWizardPage.enclosingClassAccess.private") //$NON-NLS-1$
		};
		fEnclosingClassAccessButtons = new SelectionButtonDialogFieldGroup(SWT.RADIO, buttonNames2, buttonNames2.length);
		fEnclosingClassAccessButtons.setDialogFieldListener(enclosingTypeAdapter);
		fEnclosingClassAccessButtons.setLabelText(NewClassWizardMessages.getString("NewClassWizardPage.baseclass.access.label")); //$NON-NLS-1$
		fEnclosingClassAccessButtons.setSelection(0, true);
		
		fEnclosingTypeDialogField = new StringButtonDialogField(enclosingTypeAdapter);
		fEnclosingTypeDialogField.setDialogFieldListener(enclosingTypeAdapter);
		fEnclosingTypeDialogField.setButtonLabel(NewClassWizardMessages.getString("NewClassCreationWizardPage.enclosingType.button")); //$NON-NLS-1$

		ClassNameFieldAdapter classAdapter = new ClassNameFieldAdapter();
		fClassNameDialogField = new StringDialogField();
		fClassNameDialogField.setDialogFieldListener(classAdapter);
		fClassNameDialogField.setLabelText(NewClassWizardMessages.getString("NewClassCreationWizardPage.className.label")); //$NON-NLS-1$
		
		BaseClassesFieldAdapter baseClassesAdapter = new BaseClassesFieldAdapter();
		fBaseClassesDialogField = new BaseClassesListDialogField(NewClassWizardMessages.getString("NewClassCreationWizardPage.baseClasses.label"), baseClassesAdapter); //$NON-NLS-1$
		
		MethodStubsFieldAdapter methodStubsAdapter = new MethodStubsFieldAdapter();
		fMethodStubsDialogField = new MethodStubsListDialogField(NewClassWizardMessages.getString("NewClassCreationWizardPage.methodStubs.label"), methodStubsAdapter); //$NON-NLS-1$
	    
		FileGroupFieldAdapter fileGroupAdapter = new FileGroupFieldAdapter();
		fUseDefaultSelection = new SelectionButtonDialogField(SWT.CHECK);
		fUseDefaultSelection.setDialogFieldListener(fileGroupAdapter);
		fUseDefaultSelection.setLabelText(NewClassWizardMessages.getString("NewClassCreationWizardPage.useDefaultLocation.label")); //$NON-NLS-1$
		fHeaderFileDialogField = new StringButtonDialogField(fileGroupAdapter);
		fHeaderFileDialogField.setDialogFieldListener(fileGroupAdapter);
		fHeaderFileDialogField.setLabelText(NewClassWizardMessages.getString("NewClassCreationWizardPage.headerFile.label")); //$NON-NLS-1$
		fHeaderFileDialogField.setButtonLabel(NewClassWizardMessages.getString("NewClassCreationWizardPage.headerFile.button")); //$NON-NLS-1$
		fSourceFileDialogField = new StringButtonDialogField(fileGroupAdapter);
		fSourceFileDialogField.setDialogFieldListener(fileGroupAdapter);
		fSourceFileDialogField.setLabelText(NewClassWizardMessages.getString("NewClassCreationWizardPage.sourceFile.label")); //$NON-NLS-1$
		fSourceFileDialogField.setButtonLabel(NewClassWizardMessages.getString("NewClassCreationWizardPage.sourceFile.button")); //$NON-NLS-1$
		
		fSourceFolderStatus = STATUS_OK;
		fEnclosingTypeStatus = STATUS_OK;
		fClassNameStatus = STATUS_OK;
		fBaseClassesStatus = STATUS_OK;
		fMethodStubsStatus = STATUS_OK;
		fHeaderFileStatus = STATUS_OK;
		fSourceFileStatus = STATUS_OK;
		fLastFocusedField = 0;

		updateEnclosingTypeEnableState();
		updateFileGroupEnableState();
	}
	
	// -------- UI Creation ---------

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
        createEnclosingTypeControls(composite, nColumns);
        
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
	 * Creates the controls for the enclosing class name field. Expects a <code>GridLayout</code> with at 
	 * least 4 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */		
	protected void createEnclosingTypeControls(Composite composite, int nColumns) {
 		fEnclosingTypeSelection.doFillIntoGrid(composite, 1);
		
//		DialogField.createEmptySpace(composite, 1);

		GridData gd;
//		LayoutUtil.setHorizontalSpan(fEnclosingTypeButtons.getLabelControl(composite), 1);
		Control buttonGroup = fEnclosingTypeButtons.getSelectionButtonsGroup(composite);
//		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint = convertWidthInCharsToPixels(32);
		gd.horizontalSpan = nColumns - 2;
//		textControl.setLayoutData(gd);
//		textControl.addFocusListener(new StatusFocusListener(NAMESPACE));
//		buttonGroup.setSize();
//		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
//		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
//		gd.horizontalSpan = nColumns - 2;
		buttonGroup.setLayoutData(gd);
//		DialogField.createEmptySpace(composite, 2);
		DialogField.createEmptySpace(composite, 1);
		
		DialogField.createEmptySpace(composite, 1);

//		LayoutUtil.setHorizontalSpan(fNamespaceDialogField.getLabelControl(composite), 1);
//		Text textControl = fNamespaceDialogField.getTextControl(composite);
//		gd = new GridData(GridData.FILL_HORIZONTAL);
//		gd.widthHint = getMaxFieldWidth();
//		gd.horizontalSpan = 1;
//		textControl.setLayoutData(gd);
//		textControl.addFocusListener(new StatusFocusListener(NAMESPACE));
//		
//		Button button = fNamespaceDialogField.getChangeControl(composite);
//		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
//		gd.heightHint = SWTUtil.getButtonHeigthHint(button);
//		gd.widthHint = SWTUtil.getButtonWidthHint(button);
//		button.setLayoutData(gd);
//
//		DialogField.createEmptySpace(composite, 1);
		
//		LayoutUtil.setHorizontalSpan(fEnclosingClassDialogField.getLabelControl(composite), 1);
		Text textControl = fEnclosingTypeDialogField.getTextControl(composite);
		gd = new GridData(GridData.FILL_HORIZONTAL);
//		gd.widthHint = getMaxFieldWidth() - convertWidthInCharsToPixels(32) - 50;
		gd.horizontalSpan = 2;
		textControl.setLayoutData(gd);
		textControl.addFocusListener(new StatusFocusListener(ENCLOSING_TYPE_ID));
		
		Button button = fEnclosingTypeDialogField.getChangeControl(composite);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.heightHint = SWTUtil.getButtonHeigthHint(button);
//		gd.widthHint = SWTUtil.getButtonWidthHint(button);
		button.setLayoutData(gd);

		DialogField.createEmptySpace(composite, 1);

//		Label label = fEnclosingClassAccessButtons.getLabelControl(composite);
//		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
//		gd.widthHint = convertWidthInCharsToPixels(42);
//		gd.horizontalSpan = 1;
//		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
//		gd.horizontalSpan = nColumns - 1;
//		label.setLayoutData(gd);
//		LayoutUtil.setHorizontalSpan(label, 1);
		buttonGroup = fEnclosingClassAccessButtons.getSelectionButtonsGroup(composite);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
//		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.widthHint = convertWidthInCharsToPixels(42);
		gd.horizontalSpan = nColumns - 2;
		buttonGroup.setLayoutData(gd);
		DialogField.createEmptySpace(composite, 1);
	}	

	/**
	 * Creates the controls for the type name field. Expects a <code>GridLayout</code> with at 
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
	 * Creates the controls for the base classes field. Expects a <code>GridLayout</code> with 
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
	 * Creates the controls for the enclosing class name field. Expects a <code>GridLayout</code> with at 
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
		gd.heightHint = SWTUtil.getButtonHeigthHint(button);
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
		gd.heightHint = SWTUtil.getButtonHeigthHint(button);
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
    	initFields(celem);
    	doStatusUpdate();
    	
    	//TODO restore dialog settings for method stubs
    	// IDialogSettings section = getDialogSettings().getSection(PAGE_NAME);
    	// if (section != null) {
    	// 		enabled = section.getBoolean(stubName);
    	// }
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
    						resource = resource.getParent();
    						celem = (ICElement) resource.getAdapter(ICElement.class);
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
    			CUIPlugin.getDefault().log(e);
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
    	ICElement enclosingElem = initEnclosingType(elem);
	    initSourceFolder(enclosingElem != null ? enclosingElem : elem);
    
    	IQualifiedTypeName className = null;
    	ITextSelection selection = getCurrentTextSelection();
    	if (selection != null) {
    		String text = selection.getText();
    		if (text != null && text.length() > 0 && CConventions.validateClassName(text).isOK()) {
    			className = new QualifiedTypeName(text);
    		}
    	}

    	setClassTypeName(className, false);
        addMethodStub(new ConstructorMethodStub(), true);
        addMethodStub(new DestructorMethodStub(), true);
    	setFileGroupSelection(true, true);
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
    					CUIPlugin.getDefault().log(e);
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
     * Initializes the enclosing type field.
     * 
     * @param elem the C element used to compute the enclosing type
     */
    protected ICElement initEnclosingType(ICElement elem) {
        ICElement enclosingElem = null;
	    while (elem != null) {
	        if (elem.getElementType() == ICElement.C_NAMESPACE
	                || elem.getElementType() == ICElement.C_CLASS) {
	    	    enclosingElem = elem;
	            break;
	        }
	        elem = elem.getParent();
	    }
	    IQualifiedTypeName enclosingTypeName = null;
	    boolean isNamespace = true;
	    if (enclosingElem != null) {
	        enclosingTypeName = TypeUtil.getFullyQualifiedName(enclosingElem);
		    isNamespace = TypeUtil.isNamespace(enclosingElem);
	    }
    	setEnclosingTypeName(enclosingTypeName, isNamespace, false);
    	setEnclosingTypeSelection((enclosingElem != null), true);
    	return enclosingElem;
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

    private final class StatusFocusListener implements FocusListener {
        private int fieldID;

        public StatusFocusListener(int fieldID) {
            this.fieldID = fieldID;
        }
        public void focusGained(FocusEvent e) {
            fLastFocusedField = this.fieldID;
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
	
    private class EnclosingTypeFieldAdapter implements IStringButtonAdapter, IDialogFieldListener {
		public void changeControlPressed(DialogField field) {
		    if (field == fEnclosingTypeDialogField) {
		        ITypeInfo enclosingType = null;
	            boolean isNamespace = isNamespaceButtonSelected();
		        if (isNamespace) {
					enclosingType = chooseNamespace();
		        } else {
			        enclosingType = chooseEnclosingClass();
		        }
			    if (enclosingType != null) {
			        int changedFields = ENCLOSING_TYPE_ID;
			        IPath oldFolderPath = getSourceFolderFullPath();
			        if (oldFolderPath == null) {
						IPath headerPath = getHeaderFileFullPath();
						IPath sourcePath = getSourceFileFullPath();
			            IPath newFolderPath = updateSourceFolderFromPath(enclosingType.getEnclosingProject().getFullPath());
				        if (newFolderPath != null) {
				            changedFields |= SOURCE_FOLDER_ID | HEADER_FILE_ID | SOURCE_FILE_ID;
						    // adjust the relative paths
							if (oldFolderPath != null && oldFolderPath.matchingFirstSegments(newFolderPath) == 0) {
							    if (headerPath != null) {
							        headerPath = newFolderPath.append(headerPath.lastSegment());
							    }
							    if (sourcePath != null) {
							        sourcePath = newFolderPath.append(sourcePath.lastSegment());
							    }
							}
						    setSourceFolderFullPath(newFolderPath, false);
						    // adjust the relative paths
						    setHeaderFileFullPath(headerPath, false);
						    setSourceFileFullPath(sourcePath, false);
				        }
			        }
		            IQualifiedTypeName typeName = enclosingType.getQualifiedTypeName();
			        setEnclosingTypeName(typeName, isNamespace, false);
					handleFieldChanged(changedFields);
		        }
		    }
		}
		
		public void dialogFieldChanged(DialogField field) {
		    if (field == fEnclosingTypeSelection || field == fEnclosingTypeButtons) {
		        updateEnclosingTypeEnableState();
		    }
			handleFieldChanged(ENCLOSING_TYPE_ID);
		}
	}
    
	private class ClassNameFieldAdapter implements IDialogFieldListener {
		public void dialogFieldChanged(DialogField field) {
		    int changedFields = CLASS_NAME_ID;
		    if (isUseDefaultSelected()) {
				updateFilesFromClassName(fClassNameDialogField.getText());
				changedFields |= (HEADER_FILE_ID|SOURCE_FILE_ID);
		    }
			handleFieldChanged(changedFields);
		}
	}

	private static final int MAX_UNIQUE_CLASSNAME = 99;

	private String findUniqueName(String className) {
	    IPath folderPath = getSourceFolderFullPath();
        if (folderPath == null)
            return className;

        String currName = className;
	    int count = 0;
	    String separator = ""; //$NON-NLS-1$
		//TODO could have a prefs option for how unique file names are generated
	    if (Character.isDigit(className.charAt(className.length()-1)))
	        separator = "_"; //$NON-NLS-1$
	    while (count < MAX_UNIQUE_CLASSNAME) {
	        String headerfileName = NewSourceFileGenerator.generateHeaderFileNameFromClass(currName);
	        String sourcefileName = NewSourceFileGenerator.generateSourceFileNameFromClass(currName);
		    IPath path = folderPath.append(headerfileName);
		    if (!path.toFile().exists()) {
			    path = folderPath.append(sourcefileName);
			    if (!path.toFile().exists()) {
				    return currName;
				}
			}
			++count;
			currName = className + separator + count; //$NON-NLS-1$
	    }
		return null;
	}

	private void updateFilesFromClassName(String className) {
	    String headerName = ""; //$NON-NLS-1$
	    String sourceName = ""; //$NON-NLS-1$
		if (className.length() > 0) {
		    className = findUniqueName(className);
		    if (className != null) {
		        headerName = NewSourceFileGenerator.generateHeaderFileNameFromClass(className);
		        sourceName = NewSourceFileGenerator.generateSourceFileNameFromClass(className);
		    }
		}
	    fHeaderFileDialogField.setTextWithoutUpdate(headerName);
	    fSourceFileDialogField.setTextWithoutUpdate(sourceName);
	}

	private final class BaseClassesFieldAdapter implements IListAdapter {
        public void customButtonPressed(ListDialogField field, int index) {
            if (index == 0) {
                chooseBaseClasses();
            }
            handleFieldChanged(BASE_CLASSES_ID);
        }

        public void selectionChanged(ListDialogField field) {
        }

        public void doubleClicked(ListDialogField field) {
        }
    }

	private final class MethodStubsFieldAdapter implements IListAdapter {

        public void customButtonPressed(ListDialogField field, int index) {
        }

        public void selectionChanged(ListDialogField field) {
        }

        public void doubleClicked(ListDialogField field) {
        }
    }

    private class FileGroupFieldAdapter implements IStringButtonAdapter, IDialogFieldListener {
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
		    }
		    if (field == fHeaderFileDialogField) {
	            changedFields |= HEADER_FILE_ID;
	        } else if (field == fSourceFileDialogField) {
	            changedFields |= SOURCE_FILE_ID;
	        }
			handleFieldChanged(changedFields);
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
			status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnterSourceFolderName")); //$NON-NLS-1$
			return status;
		}

		IResource res = fWorkspaceRoot.findMember(folderPath);
		if (res != null && res.exists()) {
			int resType = res.getType();
			if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
				IProject proj = res.getProject();
				if (!proj.isOpen()) {
					status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.NotAFolder", folderPath)); //$NON-NLS-1$
					return status;
				}
			    if (!CoreModel.hasCCNature(proj) && !CoreModel.hasCNature(proj)) {
					if (resType == IResource.PROJECT) {
						status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.warning.NotACProject")); //$NON-NLS-1$
						return status;
					} else {
						status.setWarning(NewClassWizardMessages.getString("NewClassCreationWizardPage.warning.NotInACProject")); //$NON-NLS-1$
					}
				}
			    ICElement e = CoreModel.getDefault().create(res.getFullPath());
			    if (CModelUtil.getSourceFolder(e) == null) {
					status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.NotASourceFolder", folderPath)); //$NON-NLS-1$
					return status;
				}
			} else {
				status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.NotAFolder", folderPath)); //$NON-NLS-1$
				return status;
			}
		} else {
			status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.FolderDoesNotExist", folderPath)); //$NON-NLS-1$
			return status;
		}

		return status;
	}
		
	/**
	 * This method is a hook which gets called after the enclosing type
	 * text input field has changed. This default implementation updates
	 * the model and returns an error status. The underlying model
	 * is only valid if the returned status is OK.
	 * 
	 * @return the model's error status
	 */
	protected IStatus enclosingTypeChanged() {
		StatusInfo status = new StatusInfo();
		if (!isEnclosingTypeSelected()) {
		    return status;
		}
		if (isNamespaceButtonSelected()) {
		    return namespaceChanged();
		} else {
		    return enclosingClassChanged();
		}
	}
	
	private IStatus namespaceChanged() {
	    StatusInfo status = new StatusInfo();

		// must not be empty
	    IQualifiedTypeName typeName = getEnclosingTypeName();
		if (typeName == null) {
			status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnterNamespace")); //$NON-NLS-1$
			return status;
		}

	    IStatus val = CConventions.validateNamespaceName(typeName.toString());
		if (val.getSeverity() == IStatus.ERROR) {
			status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.InvalidNamespace", val.getMessage())); //$NON-NLS-1$
			return status;
		}

		IProject project = getCurrentProject();
	    if (project != null) {
			if (typeName.isQualified()) {
			    // make sure enclosing namespace exists
			    ITypeInfo parentNamespace = AllTypesCache.getType(project, ICElement.C_NAMESPACE, typeName.getEnclosingTypeName());
			    if (parentNamespace == null) {
	                status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnclosingNamespaceNotExists")); //$NON-NLS-1$
	                return status;
			    }
			}
		    
		    ITypeInfo[] types = AllTypesCache.getTypes(project, typeName, false, true);
		    if (types.length > 0) {
			    // look for namespace
			    boolean foundNamespace = false;
			    boolean exactMatch = false;
		        for (int i = 0; i < types.length; ++i) {
		            ITypeInfo currType = types[i];
					if (currType.getCElementType() == ICElement.C_NAMESPACE) {
					    foundNamespace = true;
						exactMatch = currType.getQualifiedTypeName().equals(typeName);
					    if (exactMatch) {
					        // found a matching namespace
					        break;
					    }
					}
		        }
		        if (foundNamespace) {
		            if (exactMatch) {
		                // we're good to go
		                status.setOK();
		            } else {
		                status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.NamespaceExistsDifferentCase")); //$NON-NLS-1$
		            }
	                return status;
			    } else {
			        // look for other types
			        exactMatch = false;
			        for (int i = 0; i < types.length; ++i) {
			            ITypeInfo currType = types[i];
						if (currType.getCElementType() != ICElement.C_NAMESPACE) {
							exactMatch = currType.getQualifiedTypeName().equals(typeName);
						    if (exactMatch) {
						        // found a matching type
						        break;
						    }
						}
			        }
		            if (exactMatch) {
		                status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.TypeMatchingNamespaceExists")); //$NON-NLS-1$
		            } else {
		                status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.TypeMatchingNamespaceExistsDifferentCase")); //$NON-NLS-1$
		            }
		            return status;
			    }
		    } else {
		        status.setWarning(NewClassWizardMessages.getString("NewClassCreationWizardPage.warning.NamespaceNotExists")); //$NON-NLS-1$
		    }
	    }

	    val = CConventions.validateNamespaceName(typeName.lastSegment());
		if (val.getSeverity() == IStatus.ERROR) {
			status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.InvalidNamespace", val.getMessage())); //$NON-NLS-1$
			return status;
		} else if (val.getSeverity() == IStatus.WARNING) {
			status.setWarning(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.warning.NamespaceDiscouraged", val.getMessage())); //$NON-NLS-1$
		}

	    return status;
	}

	private IStatus enclosingClassChanged() {
	    StatusInfo status = new StatusInfo();

		// must not be empty
	    IQualifiedTypeName typeName = getEnclosingTypeName();
		if (typeName == null) {
			status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnterEnclosingClassName")); //$NON-NLS-1$
			return status;
		}

		IStatus val = CConventions.validateClassName(typeName.toString());
		if (val.getSeverity() == IStatus.ERROR) {
			status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.InvalidEnclosingClassName", val.getMessage())); //$NON-NLS-1$
			return status;
		}

	    IProject project = getCurrentProject();
	    if (project != null) {
		    ITypeInfo[] types = AllTypesCache.getTypes(project, typeName, false, true);
		    if (types.length > 0) {
			    // look for class
			    boolean foundClass = false;
			    boolean exactMatch = false;
		        for (int i = 0; i < types.length; ++i) {
		            ITypeInfo currType = types[i];
					if (currType.getCElementType() == ICElement.C_CLASS) {
					    foundClass = true;
						exactMatch = currType.getQualifiedTypeName().equals(typeName);
					    if (exactMatch) {
					        // found a matching class
					        break;
					    }
					}
		        }
		        if (foundClass) {
		            if (exactMatch) {
		                // we're good to go
		                status.setOK();
		            } else {
						status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnclosingClassExistsDifferentCase")); //$NON-NLS-1$
		            }
	                return status;
		        }
		    }

		    status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnclosingClassNotExists")); //$NON-NLS-1$
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
	    
	    IQualifiedTypeName className = getClassTypeName();
		// must not be empty
		if (className == null) {
			status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnterClassName")); //$NON-NLS-1$
			return status;
		}
		if (className.isQualified()) { //$NON-NLS-1$
			status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.QualifiedClassName")); //$NON-NLS-1$
			return status;
		}
	
		IStatus val = CConventions.validateClassName(className.toString());
		if (val.getSeverity() == IStatus.ERROR) {
			status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.InvalidClassName", val.getMessage())); //$NON-NLS-1$
			return status;
		} else if (val.getSeverity() == IStatus.WARNING) {
			status.setWarning(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.warning.ClassNameDiscouraged", val.getMessage())); //$NON-NLS-1$
			// continue checking
		}
	
	    IProject project = getCurrentProject();
	    if (project != null) {
	        IQualifiedTypeName fullyQualifiedName = className;
			if (isEnclosingTypeSelected()) {
			    IQualifiedTypeName enclosing = getEnclosingTypeName();
			    if (enclosing != null) {
			        fullyQualifiedName = enclosing.append(className);
			    }
			}
			
		    ITypeInfo[] types = AllTypesCache.getTypes(project, fullyQualifiedName, false, true);
		    if (types.length > 0) {
			    // look for class
			    boolean foundClass = false;
			    boolean exactMatch = false;
		        for (int i = 0; i < types.length; ++i) {
		            ITypeInfo currType = types[i];
					if (currType.getCElementType() == ICElement.C_CLASS
					        || currType.getCElementType() == ICElement.C_STRUCT) {
					    foundClass = true;
						exactMatch = currType.getQualifiedTypeName().equals(fullyQualifiedName);
					    if (exactMatch) {
					        // found a matching class
					        break;
					    }
					}
		        }
		        if (foundClass) {
		            if (exactMatch) {
						status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.ClassNameExists")); //$NON-NLS-1$
		            } else {
						status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.ClassNameExistsDifferentCase")); //$NON-NLS-1$
		            }
	                return status;
			    } else {
			        // look for other types
			        exactMatch = false;
			        for (int i = 0; i < types.length; ++i) {
			            ITypeInfo currType = types[i];
						if (currType.getCElementType() != ICElement.C_CLASS
						        && currType.getCElementType() != ICElement.C_STRUCT) {
							exactMatch = currType.getQualifiedTypeName().equals(fullyQualifiedName);
						    if (exactMatch) {
						        // found a matching type
						        break;
						    }
						}
			        }
		            if (exactMatch) {
		                status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.TypeMatchingClassExists")); //$NON-NLS-1$
		            } else {
		                status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.TypeMatchingClassExistsDifferentCase")); //$NON-NLS-1$
		            }
		            return status;
			    }
		    }
	    }
		return status;
	}

	/**
	 * Hook method that gets called when the list of base classes has changed. The method 
	 * validates the base classes and returns the status of the validation.
	 * <p>
	 * Subclasses may extend this method to perform their own validation.
	 * </p>
	 * 
	 * @return the status of the validation
	 */
	protected IStatus baseClassesChanged() {
		StatusInfo status = new StatusInfo();
		IProject project = getCurrentProject();
	    if (project != null) {
	        if (fWarnIfBaseClassNotInPath) {
			    // make sure all classes belong to the project
			    IBaseClassInfo[] baseClasses = getBaseClasses();
			    for (int i = 0; i < baseClasses.length; ++i) {
			        ITypeInfo baseType = baseClasses[i].getType();
			        IProject baseProject = baseType.getEnclosingProject();
			        if (baseProject == null || !baseProject.equals(project)) {
			            ITypeReference ref = baseType.getResolvedReference();
			            if (ref == null || PathUtil.makeRelativePathToProjectIncludes(ref.getLocation(), project) == null) {
							status.setWarning(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.warning.BaseClassNotExistsInProject", baseType.getQualifiedTypeName().toString())); //$NON-NLS-1$
							return status;
			            }
			        }
			    }
	        }
	    }
		return status;
	}

	/**
	 * This method is a hook which gets called after the method stubs
	 * input field has changed. This default implementation updates
	 * the model and returns an error status. The underlying model
	 * is only valid if the returned status is OK.
	 * 
	 * @return the model's error status
	 */
	protected IStatus methodStubsChanged() {
		StatusInfo status = new StatusInfo();
		return status;
	}

	/**
	 * This method is a hook which gets called after the header file's
	 * text input field has changed. This default implementation updates
	 * the model and returns an error status. The underlying model
	 * is only valid if the returned status is OK.
	 * 
	 * @return the model's error status
	 */
	protected IStatus headerFileChanged() {
		StatusInfo status = new StatusInfo();
		if (isUseDefaultSelected()) {
		    return status;
		}
		
		IPath path = getHeaderFileFullPath();
		if (path == null) {
			status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnterHeaderFileName")); //$NON-NLS-1$
			return status;
		}
		
		boolean fileExists = false;

		// make sure header is inside source folder
		IPath folderPath = getSourceFolderFullPath();
		if (folderPath != null) {
		    if (!folderPath.isPrefixOf(path)) {
				status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.HeaderFileNotInSourceFolder")); //$NON-NLS-1$
				return status;
		    }
		
			IPath workspacePath = fWorkspaceRoot.getLocation();
		    File file = workspacePath.append(path).toFile();
		    if (file != null && file.exists()) {
		        if (file.isFile()) {
		            fileExists = true;
				    status.setWarning(NewClassWizardMessages.getString("NewClassCreationWizardPage.warning.HeaderFileExists")); //$NON-NLS-1$
		        } else {
					status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.NotAFile", path)); //$NON-NLS-1$
					return status;
		        }
			} else if (path.segmentCount() > 1) {
				IPath parentFolderPath = workspacePath.append(path).removeLastSegments(1);
				File folder = parentFolderPath.toFile();
				if (folder != null && folder.exists() && folder.isDirectory()) {
				    // folder exists
				} else {
					status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.FolderDoesNotExist", PathUtil.getWorkspaceRelativePath(parentFolderPath))); //$NON-NLS-1$
					return status;
				}
			}
			IResource res = fWorkspaceRoot.findMember(path);
			if (res != null && res.exists()) {
				int resType = res.getType();
				if (resType == IResource.FILE) {
					IProject proj = res.getProject();
					if (!proj.isOpen()) {
						status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.NotAFile", path)); //$NON-NLS-1$
						return status;
					}
				    if (!CoreModel.hasCCNature(proj) && !CoreModel.hasCNature(proj)) {
						status.setWarning(NewClassWizardMessages.getString("NewClassCreationWizardPage.warning.NotInACProject")); //$NON-NLS-1$
					} else {
					    fileExists = true;
					    status.setWarning(NewClassWizardMessages.getString("NewClassCreationWizardPage.warning.HeaderFileExists")); //$NON-NLS-1$
					}
				} else {
					status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.NotAFile", path)); //$NON-NLS-1$
					return status;
				}
			}
		}

		if (!fileExists) {
			IStatus val = validateHeaderFileName(getCurrentProject(), path.lastSegment());
			if (val.getSeverity() == IStatus.ERROR) {
				status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.InvalidHeaderFileName", val.getMessage())); //$NON-NLS-1$
				return status;
			} else if (val.getSeverity() == IStatus.WARNING) {
				status.setWarning(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.warning.HeaderFileNameDiscouraged", val.getMessage())); //$NON-NLS-1$
			}
		}
		return status;
	}

	/**
	 * This method is a hook which gets called after the source file's
	 * text input field has changed. This default implementation updates
	 * the model and returns an error status. The underlying model
	 * is only valid if the returned status is OK.
	 * 
	 * @return the model's error status
	 */
	protected IStatus sourceFileChanged() {
		StatusInfo status = new StatusInfo();
		if (isUseDefaultSelected()) {
		    return status;
		}
		
		IPath path = getSourceFileFullPath();
		if (path == null) {
			status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnterSourceFileName")); //$NON-NLS-1$
			return status;
		}
		
		boolean fileExists = false;

		// make sure header is inside source folder
		IPath folderPath = getSourceFolderFullPath();
		if (folderPath != null) {
		    if (!folderPath.isPrefixOf(path)) {
				status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.SourceFileNotInSourceFolder")); //$NON-NLS-1$
				return status;
		    }
		
			IPath workspacePath = fWorkspaceRoot.getLocation();
		    File file = workspacePath.append(path).toFile();
		    if (file != null && file.exists()) {
		        if (file.isFile()) {
		            fileExists = true;
				    status.setWarning(NewClassWizardMessages.getString("NewClassCreationWizardPage.warning.SourceFileExists")); //$NON-NLS-1$
		        } else {
					status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.NotAFile", path)); //$NON-NLS-1$
					return status;
		        }
			} else if (path.segmentCount() > 1) {
				IPath parentFolderPath = workspacePath.append(path).removeLastSegments(1);
				File folder = parentFolderPath.toFile();
				if (folder != null && folder.exists() && folder.isDirectory()) {
				    // folder exists
				} else {
					status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.FolderDoesNotExist", PathUtil.getWorkspaceRelativePath(parentFolderPath))); //$NON-NLS-1$
					return status;
				}
			}
			IResource res = fWorkspaceRoot.findMember(path);
			if (res != null && res.exists()) {
				int resType = res.getType();
				if (resType == IResource.FILE) {
					IProject proj = res.getProject();
					if (!proj.isOpen()) {
						status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.NotAFile", path)); //$NON-NLS-1$
						return status;
					}
				    if (!CoreModel.hasCCNature(proj) && !CoreModel.hasCNature(proj)) {
						status.setWarning(NewClassWizardMessages.getString("NewClassCreationWizardPage.warning.NotInACProject")); //$NON-NLS-1$
					} else {
					    fileExists = true;
					    status.setWarning(NewClassWizardMessages.getString("NewClassCreationWizardPage.warning.SourceFileExists")); //$NON-NLS-1$
					}
				} else {
					status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.NotAFile", path)); //$NON-NLS-1$
					return status;
				}
			}
		}

		if (!fileExists) {
			IStatus val = validateSourceFileName(getCurrentProject(), path.lastSegment());
			if (val.getSeverity() == IStatus.ERROR) {
				status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.InvalidSourceFileName", val.getMessage())); //$NON-NLS-1$
				return status;
			} else if (val.getSeverity() == IStatus.WARNING) {
				status.setWarning(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.warning.SourceFileNameDiscouraged", val.getMessage())); //$NON-NLS-1$
			}
		}
		return status;
	}

	//TODO should this method be part of CConventions.java?
	private static IStatus validateHeaderFileName(IProject project, String name) {
	    IStatus val = validateFileName(name);
	    if (val.getSeverity() == IStatus.ERROR) {
	        return val;
	    }

	    StatusInfo status = new StatusInfo(val.getSeverity(), val.getMessage());

	    if (!CoreModel.isValidHeaderUnitName(project, name)) {
	        status.setWarning(NewClassWizardMessages.getString("NewClassCreationWizardPage.convention.headerFilename.filetype")); //$NON-NLS-1$
	    }

		//TODO could use a prefs option for header file naming conventions
        return status;
	}

	//TODO should this method be part of CConventions.java?
	private static IStatus validateSourceFileName(IProject project, String name) {
	    IStatus val = validateFileName(name);
	    if (val.getSeverity() == IStatus.ERROR) {
	        return val;
	    }

	    StatusInfo status = new StatusInfo(val.getSeverity(), val.getMessage());

	    if (!CoreModel.isValidSourceUnitName(project, name)) {
	        status.setWarning(NewClassWizardMessages.getString("NewClassCreationWizardPage.convention.sourceFilename.filetype")); //$NON-NLS-1$
	    }

		//TODO could use a prefs option for source file naming conventions
        return status;
	}
	
	//TODO should this method be part of CConventions.java?
	private static IStatus validateFileName(String name) {
	    StatusInfo status = new StatusInfo();
	    
	    if (name == null || name.length() == 0) {
	        status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.convention.filename.nullName")); //$NON-NLS-1$
	        return status;
	    }

		IPath path = new Path(name);
		if (!path.isValidSegment(name)) {
	        status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.convention.filename.invalid")); //$NON-NLS-1$
	        return status;
		}
		
		if (name.indexOf(" ") != -1) { //$NON-NLS-1$
	        status.setWarning(NewClassWizardMessages.getString("NewClassCreationWizardPage.convention.filename.nameWithBlanks")); //$NON-NLS-1$
		}
		
		//TODO could use a prefs option for file naming conventions
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
	    if (fieldChanged(fields, ENCLOSING_TYPE_ID)) {
			fEnclosingTypeStatus = enclosingTypeChanged();
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

	protected void doStatusUpdate() {
	    // do the last focused field first
	    IStatus lastStatus = getLastFocusedStatus();

	    // status of all used components
		IStatus[] status = new IStatus[] {
	        lastStatus,
			(fSourceFolderStatus != lastStatus) ? fSourceFolderStatus : STATUS_OK,
			(fEnclosingTypeStatus != lastStatus) ? fEnclosingTypeStatus : STATUS_OK,
			(fClassNameStatus != lastStatus) ? fClassNameStatus : STATUS_OK,
			(fBaseClassesStatus != lastStatus) ? fBaseClassesStatus : STATUS_OK,
			(fMethodStubsStatus != lastStatus) ? fMethodStubsStatus : STATUS_OK,
			(fHeaderFileStatus != lastStatus) ? fHeaderFileStatus : STATUS_OK,
			(fSourceFileStatus != lastStatus) ? fSourceFileStatus : STATUS_OK
		};
		
		// the mode severe status will be displayed and the ok button enabled/disabled.
		updateStatus(status);
	}

	private IStatus getLastFocusedStatus() {
	    switch (fLastFocusedField) {
	    	case SOURCE_FOLDER_ID:
	    	    return fSourceFolderStatus;
	    	case ENCLOSING_TYPE_ID:
	    	    return fEnclosingTypeStatus;
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
	
	private IProject getCurrentProject() {
	    IPath folderPath = getSourceFolderFullPath();
	    if (folderPath != null) {
	        return PathUtil.getEnclosingProject(folderPath);
	    }
	    return null;
	}

	private ICElement getSourceFolderFromPath(IPath path) {
	    if (path == null)
	        return null;
	    while (!path.isEmpty()) {
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
	 * Returns the enclosing type name entered into the enclosing type input field.
	 * 
	 * @return the enclosing type name
	 */
	public IQualifiedTypeName getEnclosingTypeName() {
	    String text = fEnclosingTypeDialogField.getText();
	    if (text.length() > 0)
	        return new QualifiedTypeName(text);
	    return null;
	}

	/**
	 * Sets the enclosing type name. The method updates the underlying model 
	 * and the text of the control.
	 * 
	 * @param typeName the enclosing type name
	 */	
	public void setEnclosingTypeName(IQualifiedTypeName typeName, boolean isNamespace, boolean update) {
	    if (typeName != null) {
			fEnclosingTypeDialogField.setTextWithoutUpdate(typeName.toString()); //$NON-NLS-1$
	    } else {
			fEnclosingTypeDialogField.setTextWithoutUpdate(""); //$NON-NLS-1$
	    }
		fEnclosingTypeButtons.setSelection(isNamespace ? CLASS_INDEX : NAMESPACE_INDEX, false);
		fEnclosingTypeButtons.setSelection(isNamespace ? NAMESPACE_INDEX : CLASS_INDEX, true);
		if (update) {
		    fEnclosingTypeDialogField.dialogFieldChanged();
		}
	}
	
	/**
	 * Returns the selection state of the enclosing class checkbox.
	 * 
	 * @return the selection state of the enclosing class checkbox
	 */
	public boolean isEnclosingTypeSelected() {
		return fEnclosingTypeSelection.isSelected();
	}
	
	/**
	 * Returns the selection state of the enclosing class checkbox.
	 * 
	 * @return the selection state of the enclosing class checkbox
	 */
	public boolean isNamespaceButtonSelected() {
		return fEnclosingTypeButtons.isSelected(NAMESPACE_INDEX);
	}

	/**
	 * Returns the class name entered into the class input field.
	 * 
	 * @return the class name
	 */
	public IQualifiedTypeName getClassTypeName() {
	    String text = fClassNameDialogField.getText();
	    if (text.length() > 0) {
	        return new QualifiedTypeName(text);
	    }
	    return null;
	}

	/**
	 * Sets the class name input field's text to the given value. Method doesn't update
	 * the model.
	 * 
	 * @param name the new type name
	 * @param canBeModified if <code>true</code> the type name field is
	 * editable; otherwise it is read-only.
	 */	
	public void setClassTypeName(IQualifiedTypeName typeName, boolean update) {
		fClassNameDialogField.setTextWithoutUpdate(typeName != null ? typeName.toString() : ""); //$NON-NLS-1$
		if (update) {
		    fClassNameDialogField.dialogFieldChanged();
		}
	}
	
	/**
	 * Returns the chosen base classes.
	 * 
	 * @return array of <code>IBaseClassInfo</code>
	 */
	public IBaseClassInfo[] getBaseClasses() {
	    List classesList = fBaseClassesDialogField.getElements();
	    return (IBaseClassInfo[]) classesList.toArray(new IBaseClassInfo[classesList.size()]);
	}

	/**
	 * Returns the chosen method stubs.
	 * 
	 * @return array of <code>IMethodStub</code> or empty array if none selected.
	 */
	public IMethodStub[] getCheckedMethodStubs() {
	    return fMethodStubsDialogField.getCheckedMethodStubs();
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
	 * Returns the file path entered into the header file field.
	 * 
	 * @return the file path
	 */
	public IPath getHeaderFileFullPath() {
		String str = fHeaderFileDialogField.getText();
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
	 * Sets the current header file.
	 * 
	 * @param path The new header path
	 */ 
	public void setHeaderFileFullPath(IPath path, boolean update) {
	    String str = ""; //$NON-NLS-1$
	    if (path != null) {
		    IPath sourceFolder = getSourceFolderFullPath();
		    if (sourceFolder != null) {
		        IPath relativePath = PathUtil.makeRelativePath(path, sourceFolder);
		        if (relativePath != null)
		        	path = relativePath;
		    }
		    str = path.toString();
	    }
	    fHeaderFileDialogField.setTextWithoutUpdate(str);
	    if (update)
		    fHeaderFileDialogField.dialogFieldChanged();
	}	

	/**
	 * Returns the file path entered into the source file field.
	 * 
	 * @return the file path
	 */
	public IPath getSourceFileFullPath() {
		String str = fSourceFileDialogField.getText();
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
	 * Sets the current source file.
	 * 
	 * @param path The new source path
	 */ 
	public void setSourceFileFullPath(IPath path, boolean update) {
	    String str = ""; //$NON-NLS-1$
	    if (path != null) {
		    IPath sourceFolder = getSourceFolderFullPath();
		    if (sourceFolder != null) {
		        IPath relativePath = PathUtil.makeRelativePath(path, sourceFolder);
		        if (relativePath != null)
		        	path = relativePath;
		    }
		    str = path.makeRelative().toString();
	    }
		fSourceFileDialogField.setTextWithoutUpdate(str);
		if (update)
		    fSourceFileDialogField.dialogFieldChanged();
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
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
		    prepareTypeCache();
			setFocus();
		}
	}

	/**
	 * Sets the focus on the type name input field.
	 */		
	protected void setFocus() {
		fClassNameDialogField.setFocus();
	}
				
    public void addBaseClass(ITypeInfo newBaseClass, ASTAccessVisibility access, boolean isVirtual) {
        List baseClasses = fBaseClassesDialogField.getElements();
        boolean classExists = false;
        if (baseClasses != null) {
            for (Iterator i = baseClasses.iterator(); i.hasNext(); ) {
                BaseClassInfo info = (BaseClassInfo) i.next();
                if (info.getType().equals(newBaseClass)) {
                    classExists = true;
                    break;
                }
            }
        }
        if (!classExists) {
    		// resolve location of base class
			if (newBaseClass.getResolvedReference() == null) {
				final ITypeInfo[] typesToResolve = new ITypeInfo[] { newBaseClass };
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
						AllTypesCache.resolveTypeLocation(typesToResolve[0], progressMonitor);
						if (progressMonitor.isCanceled()) {
							throw new InterruptedException();
						}
					}
				};
				
				try {
					getContainer().run(true, true, runnable);
				} catch (InvocationTargetException e) {
					String title = NewClassWizardMessages.getString("NewClassCreationWizardPage.getClasses.exception.title"); //$NON-NLS-1$
					String message = NewClassWizardMessages.getString("NewClassCreationWizardPage.getClasses.exception.message"); //$NON-NLS-1$
					ExceptionHandler.handle(e, title, message);
					return;
				} catch (InterruptedException e) {
					// cancelled by user
				    return;
				}
			}
			fBaseClassesDialogField.addBaseClass(new BaseClassInfo(newBaseClass, access, isVirtual));
    	}
    }

	public void addMethodStub(IMethodStub methodStub, boolean checked) {
	    fMethodStubsDialogField.addMethodStub(methodStub, checked);
	}

	void prepareTypeCache() {
	    final ITypeSearchScope scope = new TypeSearchScope(true);
		if (!AllTypesCache.isCacheUpToDate(scope)) {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					AllTypesCache.updateCache(scope, monitor);
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
				}
			};
			
			try {
				getContainer().run(true, true, runnable);
			} catch (InvocationTargetException e) {
				String title = NewClassWizardMessages.getString("NewClassCreationWizardPage.getClasses.exception.title"); //$NON-NLS-1$
				String message = NewClassWizardMessages.getString("NewClassCreationWizardPage.getClasses.exception.message"); //$NON-NLS-1$
				ExceptionHandler.handle(e, title, message);
			} catch (InterruptedException e) {
				// cancelled by user
			}
		}
	}

	/**
	 * Sets the enclosing class checkbox's selection state.
	 * 
	 * @param isSelected the checkbox's selection state
	 * @param canBeModified if <code>true</code> the enclosing class checkbox is
	 * modifiable; otherwise it is read-only.
	 */
	public void setEnclosingTypeSelection(boolean isSelected, boolean canBeModified) {
		fEnclosingTypeSelection.setSelection(isSelected);
		fEnclosingTypeSelection.setEnabled(canBeModified);
		updateEnclosingTypeEnableState();
	}
	
	/**
	 * Sets the enclosing class checkbox's selection state.
	 * 
	 * @param isSelected the checkbox's selection state
	 * @param canBeModified if <code>true</code> the enclosing class checkbox is
	 * modifiable; otherwise it is read-only.
	 */
	public void setFileGroupSelection(boolean isSelected, boolean canBeModified) {
		fUseDefaultSelection.setSelection(isSelected);
		fUseDefaultSelection.setEnabled(canBeModified);
		updateFileGroupEnableState();
	}

	/*
	 * Updates the enable state of buttons related to the enclosing type selection checkbox.
	 */
	void updateEnclosingTypeEnableState() {
		boolean enclosing = isEnclosingTypeSelected();
        fEnclosingTypeButtons.setEnabled(enclosing);
        fEnclosingTypeDialogField.setEnabled(enclosing);
        fEnclosingClassAccessButtons.setEnabled(enclosing && !isNamespaceButtonSelected());
	}
	
	/*
	 * Updates the enable state of buttons related to the file group selection checkbox.
	 */
	void updateFileGroupEnableState() {
		boolean filegroup = !isUseDefaultSelected();
		fHeaderFileDialogField.setEnabled(filegroup);
	    fSourceFileDialogField.setEnabled(filegroup);
	}
	
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
	
	ITypeInfo chooseNamespace() {
	    ITypeSearchScope scope;
	    IProject project = getCurrentProject();
	    if (project != null) {
		    scope = new TypeSearchScope(project);
	    } else {
		    scope = new TypeSearchScope(true);
	    }

		ITypeInfo[] elements = AllTypesCache.getNamespaces(scope, false);
		if (elements == null || elements.length == 0) {
			String title = NewClassWizardMessages.getString("NewClassCreationWizardPage.getClasses.noclasses.title"); //$NON-NLS-1$
			String message = NewClassWizardMessages.getString("NewClassCreationWizardPage.getClasses.noclasses.message"); //$NON-NLS-1$
			MessageDialog.openInformation(getShell(), title, message);
			return null;
		}
		
		NamespaceSelectionDialog dialog = new NamespaceSelectionDialog(getShell());
		dialog.setElements(elements);
		int result = dialog.open();
		if (result == IDialogConstants.OK_ID) {
		    ITypeInfo namespace = (ITypeInfo) dialog.getFirstResult();
/*		    if (namespace != null) {
	    		// resolve location of namespace
				if (namespace.getResolvedReference() == null) {
					final ITypeInfo[] typesToResolve = new ITypeInfo[] { namespace };
					IRunnableWithProgress runnable = new IRunnableWithProgress() {
						public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
							AllTypesCache.resolveTypeLocation(typesToResolve[0], progressMonitor);
							if (progressMonitor.isCanceled()) {
								throw new InterruptedException();
							}
						}
					};
					
					try {
						getContainer().run(true, true, runnable);
					} catch (InvocationTargetException e) {
						String title = NewClassWizardMessages.getString("NewClassCreationWizardPage.getClasses.exception.title"); //$NON-NLS-1$
						String message = NewClassWizardMessages.getString("NewClassCreationWizardPage.getClasses.exception.message"); //$NON-NLS-1$
						ExceptionHandler.handle(e, title, message);
						return null;
					} catch (InterruptedException e) {
						// cancelled by user
					    return null;
					}
				}
	    	}
*/			return namespace;
		}
		
		return null;
	}
	
	private final int[] ENCLOSING_CLASS_TYPES = { ICElement.C_CLASS };
	
	ITypeInfo chooseEnclosingClass() {
	    ITypeSearchScope scope;
	    IProject project = getCurrentProject();
	    if (project != null) {
		    scope = new TypeSearchScope(project);
	    } else {
		    scope = new TypeSearchScope(true);
	    }
	    
		ITypeInfo[] elements = AllTypesCache.getTypes(scope, ENCLOSING_CLASS_TYPES);
		if (elements == null || elements.length == 0) {
			String title = NewClassWizardMessages.getString("NewClassCreationWizardPage.getClasses.noclasses.title"); //$NON-NLS-1$
			String message = NewClassWizardMessages.getString("NewClassCreationWizardPage.getClasses.noclasses.message"); //$NON-NLS-1$
			MessageDialog.openInformation(getShell(), title, message);
			return null;
		}
		
		EnclosingClassSelectionDialog dialog = new EnclosingClassSelectionDialog(getShell());
		dialog.setElements(elements);
		int result = dialog.open();
		if (result == IDialogConstants.OK_ID) {
		    ITypeInfo enclosingClass = (ITypeInfo) dialog.getFirstResult();
		    if (enclosingClass != null) {
	    		// resolve location of class
				if (enclosingClass.getResolvedReference() == null) {
					final ITypeInfo[] typesToResolve = new ITypeInfo[] { enclosingClass };
					IRunnableWithProgress runnable = new IRunnableWithProgress() {
						public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
							AllTypesCache.resolveTypeLocation(typesToResolve[0], progressMonitor);
							if (progressMonitor.isCanceled()) {
								throw new InterruptedException();
							}
						}
					};
					
					try {
						getContainer().run(true, true, runnable);
					} catch (InvocationTargetException e) {
						String title = NewClassWizardMessages.getString("NewClassCreationWizardPage.getClasses.exception.title"); //$NON-NLS-1$
						String message = NewClassWizardMessages.getString("NewClassCreationWizardPage.getClasses.exception.message"); //$NON-NLS-1$
						ExceptionHandler.handle(e, title, message);
						return null;
					} catch (InterruptedException e) {
						// cancelled by user
					    return null;
					}
				}
	    	}
			return enclosingClass;
		}
		
		return null;
	}
	
	private final int[] CLASS_TYPES = { ICElement.C_CLASS, ICElement.C_STRUCT };
	
	void chooseBaseClasses() {
		ITypeInfo[] elements = AllTypesCache.getTypes(new TypeSearchScope(true), CLASS_TYPES);
		if (elements == null || elements.length == 0) {
			String title = NewClassWizardMessages.getString("NewClassCreationWizardPage.getClasses.noclasses.title"); //$NON-NLS-1$
			String message = NewClassWizardMessages.getString("NewClassCreationWizardPage.getClasses.noclasses.message"); //$NON-NLS-1$
			MessageDialog.openInformation(getShell(), title, message);
			return;
		}
		
		List oldContents = fBaseClassesDialogField.getElements();
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

	IPath chooseHeaderFile() {
	    SourceFileSelectionDialog dialog = new SourceFileSelectionDialog(getShell());
	    dialog.setTitle(NewClassWizardMessages.getString("NewClassCreationWizardPage.ChooseHeaderFileDialog.title")); //$NON-NLS-1$
		ICElement input = CoreModel.create(fWorkspaceRoot);
		IProject project = getCurrentProject();
		if (project != null)
		    input = CoreModel.getDefault().create(project);
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

	IPath chooseSourceFile() {
	    SourceFileSelectionDialog dialog = new SourceFileSelectionDialog(getShell());
	    dialog.setTitle(NewClassWizardMessages.getString("NewClassCreationWizardPage.ChooseSourceFileDialog.title")); //$NON-NLS-1$
		ICElement input = CoreModel.create(fWorkspaceRoot);
		IProject project = getCurrentProject();
		if (project != null)
		    input = CoreModel.getDefault().create(project);
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
	

	// ---- creation ----------------

	/**
	 * Creates the new class using the entered field values.
	 * 
	 * @param monitor a progress monitor to report progress.
	 * @throws CoreException Thrown when the creation failed.
	 * @throws InterruptedException Thrown when the operation was cancelled.
	 */
	public void createClass(IProgressMonitor monitor) {
        try {
            fCodeGenerator = new NewClassCodeGenerator(
                    getHeaderFileFullPath(),
                    getSourceFileFullPath(),
                    getClassTypeName(),
                    isNamespaceButtonSelected() ? getEnclosingTypeName() : null,
                    getBaseClasses(),
                    getCheckedMethodStubs());
            fCodeGenerator.createClass(monitor);
        } catch (CodeGeneratorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
	/**
	 * Returns the created type. The method only returns a valid type 
	 * after <code>createType</code> has been called.
	 * 
	 * @return the created type
	 * @see #createClass(IProgressMonitor)
	 */			
	public ICElement getCreatedClass() {
	    if (fCodeGenerator != null) {
	        return fCodeGenerator.getCreatedClass();
	    }
	    return null;
	}
	
	public ITranslationUnit getCreatedHeaderTU(){
	    if (fCodeGenerator != null) {
	        return fCodeGenerator.getCreatedHeaderTU();
	    }
	    return null;
	}

	public ITranslationUnit getCreatedSourceTU(){
	    if (fCodeGenerator != null) {
	        return fCodeGenerator.getCreatedSourceTU();
	    }
	    return null;
	}

    private IPath updateSourceFolderFromPath(IPath filePath) {
        ICElement folder = getSourceFolderFromPath(filePath);
        if (folder != null) {
            return folder.getPath();
        } else {
            IProject proj = PathUtil.getEnclosingProject(filePath);
            if (proj != null)
                return proj.getFullPath(); 
        }
        return null;
    }
}
