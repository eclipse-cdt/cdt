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

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.internal.ui.viewsupport.IViewPartInputProvider;
import org.eclipse.cdt.internal.ui.wizards.NewElementWizardPage;
import org.eclipse.cdt.internal.ui.wizards.TypedElementSelectionValidator;
import org.eclipse.cdt.internal.ui.wizards.TypedViewerFilter;
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
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CElementSorter;
import org.eclipse.cdt.ui.CUIPlugin;
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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.views.contentoutline.ContentOutline;

public class NewClassCreationWizardPage extends NewElementWizardPage {

	private final static String PAGE_NAME = "NewClassWizardPage"; //$NON-NLS-1$
	
	private IWorkspaceRoot fWorkspaceRoot;

	/** ID of the source folder field */
	protected static final String SOURCE_FOLDER = PAGE_NAME + ".sourcefolder"; //$NON-NLS-1$
	private StringButtonDialogField fSourceFolderDialogField;
	protected IStatus fSourceFolderStatus;
	ICContainer fCurrentSourceFolder;
	
	/** ID of the namespace input field. */
	protected final static String NAMESPACE = PAGE_NAME + ".namespace"; //$NON-NLS-1$
	StringButtonDialogField fNamespaceDialogField;
	private boolean fCanModifyNamespace;
	protected IStatus fNamespaceStatus;
	ITypeInfo fCurrentNamespace;
	
	/** ID of the enclosing class input field. */
	protected final static String ENCLOSING_CLASS = PAGE_NAME + ".enclosingclass"; //$NON-NLS-1$
	SelectionButtonDialogField fEnclosingClassSelection;
	StringButtonDialogField fEnclosingClassDialogField;
	private boolean fCanModifyEnclosingClass;
	protected IStatus fEnclosingClassStatus;
	ITypeInfo fCurrentEnclosingClass;
	
	/** Field ID of the class name input field. */	
	protected final static String CLASSNAME = PAGE_NAME + ".classname"; //$NON-NLS-1$
	StringDialogField fClassNameDialogField;
	protected IStatus fClassNameStatus;

	/** ID of the base classes input field. */
	protected final static String BASECLASSES = PAGE_NAME + ".baseclasses"; //$NON-NLS-1$
	BaseClassesListDialogField fBaseClassesDialogField;
	protected IStatus fBaseClassesStatus;

	/** ID of the method stubs input field. */
	protected final static String METHODSTUBS = PAGE_NAME + ".methodstubs"; //$NON-NLS-1$
	MethodStubsListDialogField fMethodStubsDialogField;
	protected IStatus fMethodStubsStatus;

	SelectionButtonDialogField fUseDefaultSelection;

	/** ID of the header file input field. */
	protected final static String HEADERFILE = PAGE_NAME + ".headerfile"; //$NON-NLS-1$
	StringButtonDialogField fHeaderFileDialogField;
	protected IStatus fHeaderFileStatus;
	IPath fCurrentHeaderFile;

	/** ID of the header file input field. */
	protected final static String SOURCEFILE = PAGE_NAME + ".sourcefile"; //$NON-NLS-1$
	StringButtonDialogField fSourceFileDialogField;
	protected IStatus fSourceFileStatus;
	IPath fCurrentSourceFile;

	protected final IStatus STATUS_OK = new StatusInfo();
	protected String fLastFocusedField = null;

	private NewClassCodeGenerator fCodeGenerator = null;

	public NewClassCreationWizardPage() {
		super(PAGE_NAME);

		setTitle(NewClassWizardMessages.getString("NewClassCreationWizardPage.title")); //$NON-NLS-1$
		setDescription(NewClassWizardMessages.getString("NewClassCreationWizardPage.description")); //$NON-NLS-1$
		
		fWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		fCodeGenerator = null;
		
		SourceFolderFieldAdapter sourceFolderAdapter = new SourceFolderFieldAdapter();
		fSourceFolderDialogField = new StringButtonDialogField(sourceFolderAdapter);
		fSourceFolderDialogField.setDialogFieldListener(sourceFolderAdapter);
		fSourceFolderDialogField.setLabelText(NewClassWizardMessages.getString("NewClassCreationWizardPage.sourceFolder.label")); //$NON-NLS-1$
		fSourceFolderDialogField.setButtonLabel(NewClassWizardMessages.getString("NewClassCreationWizardPage.sourceFolder.button")); //$NON-NLS-1$

		NamespaceFieldAdapter namespaceAdapter = new NamespaceFieldAdapter();
		fNamespaceDialogField = new StringButtonDialogField(namespaceAdapter);
		fNamespaceDialogField.setDialogFieldListener(namespaceAdapter);
		fNamespaceDialogField.setLabelText(NewClassWizardMessages.getString("NewClassCreationWizardPage.namespace.label")); //$NON-NLS-1$
		fNamespaceDialogField.setButtonLabel(NewClassWizardMessages.getString("NewClassCreationWizardPage.namespace.button")); //$NON-NLS-1$

		EnclosingClassFieldAdapter enclosingClassAdapter = new EnclosingClassFieldAdapter();
		fEnclosingClassSelection = new SelectionButtonDialogField(SWT.CHECK);
		fEnclosingClassSelection.setDialogFieldListener(enclosingClassAdapter);
		fEnclosingClassSelection.setLabelText(NewClassWizardMessages.getString("NewClassCreationWizardPage.enclosingClass.label")); //$NON-NLS-1$
		fEnclosingClassDialogField = new StringButtonDialogField(enclosingClassAdapter);
		fEnclosingClassDialogField.setDialogFieldListener(enclosingClassAdapter);
		fEnclosingClassDialogField.setButtonLabel(NewClassWizardMessages.getString("NewClassCreationWizardPage.enclosingClass.button")); //$NON-NLS-1$

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
		fNamespaceStatus = STATUS_OK;
		fEnclosingClassStatus = STATUS_OK;
		fClassNameStatus = STATUS_OK;
		fBaseClassesStatus = STATUS_OK;
		fMethodStubsStatus = STATUS_OK;
		fHeaderFileStatus = STATUS_OK;
		fSourceFileStatus = STATUS_OK;
		fLastFocusedField = null;

		fCurrentSourceFolder = null;
		fCanModifyNamespace = true;
		fCurrentEnclosingClass = null;
		fCurrentHeaderFile = null;
		fCurrentSourceFile = null;
		fCanModifyEnclosingClass = true;
		updateEnableState();
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
        createNamespaceControls(composite, nColumns);
        createEnclosingClassControls(composite, nColumns);
        
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
		textControl.addFocusListener(new StatusFocusListener(SOURCE_FOLDER));
	}
	
	/**
	 * Creates the controls for the namespace field. Expects a <code>GridLayout</code> with at 
	 * least 4 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */		
	protected void createNamespaceControls(Composite composite, int nColumns) {
	    fNamespaceDialogField.doFillIntoGrid(composite, nColumns);
		Text textControl = fNamespaceDialogField.getTextControl(null);
		LayoutUtil.setWidthHint(textControl, getMaxFieldWidth());
		textControl.addFocusListener(new StatusFocusListener(NAMESPACE));
	}	

	/**
	 * Creates the controls for the enclosing class name field. Expects a <code>GridLayout</code> with at 
	 * least 4 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */		
	protected void createEnclosingClassControls(Composite composite, int nColumns) {
		Composite tabGroup = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
 		tabGroup.setLayout(layout);

		fEnclosingClassSelection.doFillIntoGrid(tabGroup, 1);

		Text textControl = fEnclosingClassDialogField.getTextControl(composite);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = getMaxFieldWidth();
		gd.horizontalSpan = 2;
		textControl.setLayoutData(gd);
		textControl.addFocusListener(new StatusFocusListener(ENCLOSING_CLASS));
		
		Button button = fEnclosingClassDialogField.getChangeControl(composite);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.heightHint = SWTUtil.getButtonHeigthHint(button);
		gd.widthHint = SWTUtil.getButtonWidthHint(button);
		button.setLayoutData(gd);
//		ControlContentAssistHelper.createTextContentAssistant(text, fEnclosingTypeCompletionProcessor);
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
		textControl.addFocusListener(new StatusFocusListener(CLASSNAME));
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
		listControl.addFocusListener(new StatusFocusListener(BASECLASSES));
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
		listControl.addFocusListener(new StatusFocusListener(METHODSTUBS));
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
		textControl.addFocusListener(new StatusFocusListener(HEADERFILE));
		
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
		textControl.addFocusListener(new StatusFocusListener(SOURCEFILE));
		
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
    	initSourceFolder(elem);
    
    	ITypeInfo namespace = null;
    	ITypeInfo enclosingClass = null;
    	//TODO evaluate the enclosing class
    			
    	String className = ""; //$NON-NLS-1$
    	
    	ITextSelection selection = getCurrentTextSelection();
    	if (selection != null) {
    		String text = selection.getText();
    		if (CConventions.validateClassName(text).isOK()) {
    			className = text;
    		}
    	}
    
    	setNamespace(namespace, true);
    	setEnclosingClass(enclosingClass, true);
    	setEnclosingClassSelection(false, true);
    	setClassName(className, true);
        addMethodStub(new ConstructorMethodStub(), true);
        addMethodStub(new DestructorMethodStub(), true);
    	setFileGroupSelection(true, true);
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
    	setSourceFolder(folder, true);
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
		return convertWidthInCharsToPixels(60);
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
        private String fieldName;

        public StatusFocusListener(String fieldName) {
            this.fieldName = fieldName;
        }
        public void focusGained(FocusEvent e) {
            fLastFocusedField = fieldName;
        }

        public void focusLost(FocusEvent e) {
            fLastFocusedField = null;
        }
    }

    private class SourceFolderFieldAdapter implements IStringButtonAdapter, IDialogFieldListener {
		public void changeControlPressed(DialogField field) {
			// take the current cproject as init element of the dialog
			ICElement folder = chooseSourceFolder(fCurrentSourceFolder);
			if (folder != null) {
				setSourceFolder(folder, true);
				prepareTypeCache();
			}
		}
		
		public void dialogFieldChanged(DialogField field) {
			fSourceFolderStatus = sourceFolderChanged();
			// tell all others
		    updateEnableState();
			handleFieldChanged(SOURCE_FOLDER);
		}
	}
	
	private class NamespaceFieldAdapter implements IStringButtonAdapter, IDialogFieldListener {
		public void changeControlPressed(DialogField field) {
			ITypeInfo namespace = chooseNamespace();
			if (namespace != null) {
			    fCurrentNamespace = namespace;
			    String name = namespace.getQualifiedTypeName().getFullyQualifiedName();

			    // this will trigger dialogFieldChanged
			    fNamespaceDialogField.setText(name);
			}
		}

		public void dialogFieldChanged(DialogField field) {
			fNamespaceStatus = namespaceChanged();
			updateEnclosingClassFromNamespace();
			// tell all others
		    updateEnableState();
			handleFieldChanged(NAMESPACE);
		}
	}

	private class EnclosingClassFieldAdapter implements IStringButtonAdapter, IDialogFieldListener {
		public void changeControlPressed(DialogField field) {
			ITypeInfo enclosingClass = chooseEnclosingClass();
			if (enclosingClass != null) {
			    fCurrentEnclosingClass = enclosingClass;
			    IQualifiedTypeName qualClassName = enclosingClass.getQualifiedTypeName();

			    // this will trigger dialogFieldChanged
			    fEnclosingClassDialogField.setText(qualClassName.getFullyQualifiedName());
			}
		}
		
		public void dialogFieldChanged(DialogField field) {
		    if (field == fEnclosingClassSelection) {
		        boolean enclosing = isEnclosingClassSelected();
		        fEnclosingClassDialogField.setEnabled(enclosing);
		    } else {
		        updateNamespaceFromEnclosingClass();
		    }
		    fEnclosingClassStatus = enclosingClassChanged();
			// tell all others
		    updateEnableState();
			handleFieldChanged(ENCLOSING_CLASS);
		}
	}

    void updateEnclosingClassFromNamespace() {
		fCurrentEnclosingClass = null;
		fEnclosingClassDialogField.setTextWithoutUpdate(""); //$NON-NLS-1$
    }

    void updateNamespaceFromEnclosingClass() {
        boolean enclosing = isEnclosingClassSelected();
        String name = ""; //$NON-NLS-1$
        if (enclosing && fCurrentEnclosingClass != null) {
			fCurrentNamespace = fCurrentEnclosingClass.getEnclosingNamespace();
			if (fCurrentNamespace != null) {
			    IQualifiedTypeName qualNSName = fCurrentNamespace.getQualifiedTypeName();
			    name = qualNSName.getFullyQualifiedName();
			}
        }
        fNamespaceDialogField.setTextWithoutUpdate(name);
    }
    
	private class ClassNameFieldAdapter implements IDialogFieldListener {
		public void dialogFieldChanged(DialogField field) {
		    if (isUseDefaultSelected()) {
				String className = fClassNameDialogField.getText();
				if (className.length() > 0) {
				    fHeaderFileDialogField.setText(NewSourceFileGenerator.generateHeaderFileNameFromClass(className));
				    fSourceFileDialogField.setText(NewSourceFileGenerator.generateSourceFileNameFromClass(className));
				} else {
				    fHeaderFileDialogField.setText(""); //$NON-NLS-1$
				    fSourceFileDialogField.setText(""); //$NON-NLS-1$
				}
		    }
		    fClassNameStatus = classNameChanged();
			// tell all others
		    updateEnableState();
			handleFieldChanged(CLASSNAME);
		}
	}

	private final class BaseClassesFieldAdapter implements IListAdapter {
        public void customButtonPressed(ListDialogField field, int index) {
            if (index == 0) {
                chooseBaseClasses();
            }
            fBaseClassesStatus = baseClassesChanged();
		    updateEnableState();
            handleFieldChanged(BASECLASSES);
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
		    if (field == fHeaderFileDialogField) {
				IPath filePath = chooseSourceFile(getHeaderFilePath(), NewClassWizardMessages.getString("NewClassCreationWizardPage.ChooseHeaderFileDialog.title")); //$NON-NLS-1$
				if (filePath != null) {
					setHeaderFile(filePath, true);
				}
		    } else if (field == fSourceFileDialogField) {
				IPath filePath = chooseSourceFile(getSourceFilePath(), NewClassWizardMessages.getString("NewClassCreationWizardPage.ChooseSourceFileDialog.title")); //$NON-NLS-1$
				if (filePath != null) {
					setSourceFile(filePath, true);
				}
		    }
		}
		
		public void dialogFieldChanged(DialogField field) {
		    if (field == fUseDefaultSelection) {
		        boolean enabled = !isUseDefaultSelected();
		        fHeaderFileDialogField.setEnabled(enabled);
		        fSourceFileDialogField.setEnabled(enabled);
		    }
			fHeaderFileStatus = headerFileChanged();
			fSourceFileStatus = sourceFileChanged();
			// tell all others
		    updateEnableState();
			handleFieldChanged(HEADERFILE);
			handleFieldChanged(SOURCEFILE);
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
		
		fCurrentSourceFolder = null;
		String str = getSourceFolderText();
		if (str.length() == 0) {
			status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnterSourceFolderName")); //$NON-NLS-1$
			return status;
		}
		IPath path = new Path(str);
		IResource res = fWorkspaceRoot.findMember(path);
		if (res != null && res.exists()) {
			int resType = res.getType();
			if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
				IProject proj = res.getProject();
				if (!proj.isOpen()) {
					status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.NotAFolder", str)); //$NON-NLS-1$
					return status;
				}
			    ICElement e = CoreModel.getDefault().create(res.getFullPath());
			    fCurrentSourceFolder = CModelUtil.getSourceFolder(e);
				if (fCurrentSourceFolder == null) {
					status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.NotASourceFolder", str)); //$NON-NLS-1$
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
			} else {
				status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.NotAFolder", str)); //$NON-NLS-1$
				return status;
			}
		} else {
			status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.FolderDoesNotExist", str)); //$NON-NLS-1$
			return status;
		}
		return status;
	}
		
	/**
	 * This method is a hook which gets called after the namespace
	 * text input field has changed. This default implementation updates
	 * the model and returns an error status. The underlying model
	 * is only valid if the returned status is OK.
	 * 
	 * @return the model's error status
	 */
	protected IStatus namespaceChanged() {
	    StatusInfo status = new StatusInfo();
		fCurrentNamespace = null;

		String namespace = getNamespace();

		// check if empty
		if (namespace.length() == 0) {
		    return status;
		}

		IStatus val = CConventions.validateClassName(namespace);
		if (val.getSeverity() == IStatus.ERROR) {
			status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.InvalidNamespace", val.getMessage())); //$NON-NLS-1$
			return status;
		} else if (val.getSeverity() == IStatus.WARNING) {
			status.setWarning(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.warning.NamespaceDiscouraged", val.getMessage())); //$NON-NLS-1$
			// continue checking
		}
	
	    IProject project = getCurrentProject();
	    if (project != null) {
		    ITypeSearchScope scope = prepareTypeCache();
		    if (scope == null) {
				status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.noTypeCache")); //$NON-NLS-1$
				return status;
		    }
		
			IQualifiedTypeName qualName = new QualifiedTypeName(namespace);

		    ITypeInfo[] types = AllTypesCache.getTypes(project, qualName, false);
	        for (int i = 0; i < types.length; ++i) {
	            if (types[i].getCElementType() == ICElement.C_NAMESPACE) {
	                fCurrentNamespace = types[i];
	                break;
	            }
	        }
	        if (fCurrentNamespace == null) {
			    types = AllTypesCache.getTypes(project, qualName, true);
		        for (int i = 0; i < types.length; ++i) {
		            if (types[i].getCElementType() == ICElement.C_NAMESPACE) {
						status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.NamespaceExistsDifferentCase")); //$NON-NLS-1$
						return status;
		            }
		        }
				status.setWarning(NewClassWizardMessages.getString("NewClassCreationWizardPage.warning.NamespaceNotExists")); //$NON-NLS-1$
				return status;
	        }
	    }
		return status;
	}

	/**
	 * This method is a hook which gets called after the enclosing class
	 * text input field has changed. This default implementation updates
	 * the model and returns an error status. The underlying model
	 * is only valid if the returned status is OK.
	 * 
	 * @return the model's error status
	 */
	protected IStatus enclosingClassChanged() {
		StatusInfo status = new StatusInfo();
		if (!isEnclosingClassSelected()) {
		    return status;
		}
		fCurrentEnclosingClass = null;

		String enclosing = getEnclosingClass();
		// must not be empty
		if (enclosing.length() == 0) {
			status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnterEnclosingClassName")); //$NON-NLS-1$
			return status;
		}

		IStatus val = CConventions.validateClassName(enclosing);
		if (val.getSeverity() == IStatus.ERROR) {
			status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.InvalidEnclosingClassName", val.getMessage())); //$NON-NLS-1$
			return status;
		}
	
	    IProject project = getCurrentProject();
	    if (project != null) {
		    ITypeSearchScope scope = prepareTypeCache();
		    if (scope == null) {
				status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.noTypeCache")); //$NON-NLS-1$
				return status;
		    }
		
			IQualifiedTypeName qualName = new QualifiedTypeName(enclosing);

		    ITypeInfo[] types = AllTypesCache.getTypes(project, qualName, false);
	        for (int i = 0; i < types.length; ++i) {
	            if (types[i].getCElementType() == ICElement.C_CLASS) {
	                fCurrentEnclosingClass = types[i];
	                break;
	            }
	        }
	        if (fCurrentEnclosingClass == null) {
			    types = AllTypesCache.getTypes(project, qualName, true);
		        for (int i = 0; i < types.length; ++i) {
		            if (types[i].getCElementType() == ICElement.C_CLASS) {
						status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnclosingClassExistsDifferentCase")); //$NON-NLS-1$
						return status;
		            }
		        }
				status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnclosingClassNotExists")); //$NON-NLS-1$
				return status;
	        } else {
                IProject enclosingProject = fCurrentEnclosingClass.getEnclosingProject();
                if (enclosingProject == null || !enclosingProject.equals(project)) {
    				status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnclosingClassNotExistsInProject")); //$NON-NLS-1$
    				return status;
                }
//                ITypeReference ref = fCurrentEnclosingClass.getResolvedReference();
//                if (ref == null || enclosingProject.getFullPath().isPrefixOf(ref.getPath())) {
//    				status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnclosingClassNotExistsInProject")); //$NON-NLS-1$
//    				return status;
//                }
	        }
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
		if (className.length() == 0) {
			status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnterClassName")); //$NON-NLS-1$
			return status;
		}
		if (className.indexOf("::") != -1) { //$NON-NLS-1$
			status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.QualifiedName")); //$NON-NLS-1$
			return status;
		}
	
		IStatus val = CConventions.validateClassName(className);
		if (val.getSeverity() == IStatus.ERROR) {
			status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.InvalidClassName", val.getMessage())); //$NON-NLS-1$
			return status;
		} else if (val.getSeverity() == IStatus.WARNING) {
			status.setWarning(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.warning.ClassNameDiscouraged", val.getMessage())); //$NON-NLS-1$
			// continue checking
		}
	
	    IProject project = getCurrentProject();
	    if (project != null) {
		    ITypeSearchScope scope = prepareTypeCache();
		    if (scope == null) {
				status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.noTypeCache")); //$NON-NLS-1$
				return status;
		    }
			
			IQualifiedTypeName qualName = new QualifiedTypeName(className);

			// must not exist
			if (!isEnclosingClassSelected()) {
			    ITypeInfo namespace = getCurrentNamespace();
			    if (namespace != null) {
			        ITypeInfo[] types = namespace.getEnclosedTypes();
			        for (int i = 0; i < types.length; ++i) {
			            IQualifiedTypeName typeName = types[i].getQualifiedTypeName().removeFirstSegments(1);
			            if (typeName.equalsIgnoreCase(qualName)) {
			                if (typeName.equals(qualName))
			                    status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.ClassNameExists")); //$NON-NLS-1$
			                else
			                    status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.ClassNameExistsDifferentCase")); //$NON-NLS-1$
							return status;
			            }
			        }
			    } else {
				    ITypeInfo[] types = AllTypesCache.getTypes(project, qualName, false);
				    if (types.length != 0) {
						status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.ClassNameExists")); //$NON-NLS-1$
						return status;
				    }
				    types = AllTypesCache.getTypes(project, qualName, true);
				    if (types.length != 0) {
						status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.ClassNameExistsDifferentCase")); //$NON-NLS-1$
						return status;
				    }
			    }
			} else {
			    ITypeInfo enclosingClass = getCurrentEnclosingClass();
			    if (enclosingClass != null) {
			        ITypeInfo[] types = enclosingClass.getEnclosedTypes();
			        for (int i = 0; i < types.length; ++i) {
			            IQualifiedTypeName typeName = types[i].getQualifiedTypeName().removeFirstSegments(1);
			            if (typeName.equalsIgnoreCase(qualName)) {
			                if (typeName.equals(qualName))
			                    status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.ClassNameExists")); //$NON-NLS-1$
			                else
			                    status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.ClassNameExistsDifferentCase")); //$NON-NLS-1$
							return status;
			            }
			        }
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
		    // make sure all classes belong to the project
		    IBaseClassInfo[] baseClasses = getBaseClasses();
		    for (int i = 0; i < baseClasses.length; ++i) {
		        ITypeInfo baseType = baseClasses[i].getType();
		        IProject baseProject = baseType.getEnclosingProject();
		        if (baseProject != null && !baseProject.equals(project)) {
					status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.BaseClassNotExistsInProject", baseType.getQualifiedTypeName().toString())); //$NON-NLS-1$
					return status;
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
		
		fCurrentHeaderFile = null;
		String str = getHeaderFileName();
		if (str.length() == 0) {
			status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnterHeaderFileName")); //$NON-NLS-1$
			return status;
		}

		IProject project = getCurrentProject();
		if (project == null) {
//			status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.SourceFolderRequired")); //$NON-NLS-1$
			return status;
		}

		IPath path = getHeaderFilePath();
		IResource res = fWorkspaceRoot.findMember(path);
		if (res != null && res.exists()) {
			int resType = res.getType();
			if (resType == IResource.FILE) {
				IProject proj = res.getProject();
				if (!proj.isOpen()) {
					status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.NotAFile", str)); //$NON-NLS-1$
					return status;
				}
			    if (!CoreModel.hasCCNature(proj) && !CoreModel.hasCNature(proj)) {
					status.setWarning(NewClassWizardMessages.getString("NewClassCreationWizardPage.warning.NotInACProject")); //$NON-NLS-1$
				} else {
				    status.setWarning(NewClassWizardMessages.getString("NewClassCreationWizardPage.warning.HeaderFileExists")); //$NON-NLS-1$
				}
			} else {
				status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.NotAFile", str)); //$NON-NLS-1$
				return status;
			}
		} else {
			IStatus val = validateHeaderFileName(project, path.lastSegment());
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
		
		fCurrentSourceFile = null;
		String str = getSourceFileName();
		if (str.length() == 0) {
			status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.EnterSourceFileName")); //$NON-NLS-1$
			return status;
		}

		IProject project = getCurrentProject();
		if (project == null) {
//			status.setError(NewClassWizardMessages.getString("NewClassCreationWizardPage.error.SourceFolderRequired")); //$NON-NLS-1$
			return status;
		}

		IPath path = getSourceFilePath();
		IResource res = fWorkspaceRoot.findMember(path);
		if (res != null && res.exists()) {
			int resType = res.getType();
			if (resType == IResource.FILE) {
				IProject proj = res.getProject();
				if (!proj.isOpen()) {
					status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.NotAFile", str)); //$NON-NLS-1$
					return status;
				}
			    if (!CoreModel.hasCCNature(proj) && !CoreModel.hasCNature(proj)) {
					status.setWarning(NewClassWizardMessages.getString("NewClassCreationWizardPage.warning.NotInACProject")); //$NON-NLS-1$
				} else {
				    status.setWarning(NewClassWizardMessages.getString("NewClassCreationWizardPage.warning.SourceFileExists")); //$NON-NLS-1$
				}
			} else {
				status.setError(NewClassWizardMessages.getFormattedString("NewClassCreationWizardPage.error.NotAFile", str)); //$NON-NLS-1$
				return status;
			}
		} else {
			IStatus val = validateSourceFileName(project, path.lastSegment());
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
	 * Hook method that gets called when a field on this page has changed. For this page the 
	 * method gets called when the source folder field changes.
	 * <p>
	 * Every sub type is responsible to call this method when a field on its page has changed.
	 * Subtypes override (extend) the method to add verification when a own field has a
	 * dependency to an other field. For example the class name input must be verified
	 * again when the package field changes (check for duplicated class names).
	 * 
	 * @param fieldName The name of the field that has changed (field id). For the
	 * source folder the field id is <code>SOURCE_FOLDER</code>
	 */
	protected void handleFieldChanged(String fieldName) {
		if (fieldName == SOURCE_FOLDER) {
			fNamespaceStatus = namespaceChanged();
			fEnclosingClassStatus = enclosingClassChanged();
			fClassNameStatus = classNameChanged();
			fBaseClassesStatus = baseClassesChanged();
			fMethodStubsStatus = methodStubsChanged();
		}
		doStatusUpdate();
	}

	protected void doStatusUpdate() {
	    // status of all used components
		IStatus[] status = new IStatus[] {
	        getLastFocusedStatus(),
			fSourceFolderStatus,
			isEnclosingClassSelected() ? fEnclosingClassStatus : fNamespaceStatus,
			fClassNameStatus,
			fBaseClassesStatus,
			fMethodStubsStatus,
			fHeaderFileStatus,
			fSourceFileStatus
		};
		
		// the mode severe status will be displayed and the ok button enabled/disabled.
		updateStatus(status);
	}

	private IStatus getLastFocusedStatus() {
	    if (fLastFocusedField == null) {
	        return STATUS_OK;
	    }
	        
        if (fLastFocusedField.equals(SOURCE_FOLDER)) {
            return fSourceFolderStatus;
        } else if (fLastFocusedField.equals(NAMESPACE) || fLastFocusedField.equals(ENCLOSING_CLASS)) {
            return isEnclosingClassSelected() ? fEnclosingClassStatus : fNamespaceStatus;
        } else if (fLastFocusedField.equals(CLASSNAME)) {
            return fClassNameStatus;
        } else if (fLastFocusedField.equals(BASECLASSES)) {
            return fBaseClassesStatus;
        } else if (fLastFocusedField.equals(METHODSTUBS)) {
            return fMethodStubsStatus;
        } else if (fLastFocusedField.equals(HEADERFILE)) {
            return fHeaderFileStatus;
        } else if (fLastFocusedField.equals(SOURCEFILE)) {
            return fSourceFileStatus;
        } else {
            return STATUS_OK;
        }
    }

    /**
	 * Returns the current text of source folder text field.
	 * 
	 * @return the text of the source folder text field
	 */ 	
	public String getSourceFolderText() {
		return fSourceFolderDialogField.getText();
	}
	
	/**
	 * Returns the namespace entered into the namespace input field.
	 * 
	 * @return the namespace
	 */
	public String getNamespace() {
		return fNamespaceDialogField.getText();
	}

	private ITypeInfo getCurrentNamespace() {
        return fCurrentNamespace;
    }

	/**
	 * Returns the enclosing class name entered into the enclosing class input field.
	 * 
	 * @return the enclosing class name
	 */
	public String getEnclosingClass() {
		return fEnclosingClassDialogField.getText();
	}
	
	private ITypeInfo getCurrentEnclosingClass() {
        return fCurrentEnclosingClass;
    }

	/**
	 * Returns the selection state of the enclosing class checkbox.
	 * 
	 * @return the selection state of the enclosing class checkbox
	 */
	public boolean isEnclosingClassSelected() {
		return fEnclosingClassSelection.isSelected();
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
	 * Returns the file name entered into the class definition field.
	 * 
	 * @return the file name
	 */
	public String getHeaderFileName() {
		return fHeaderFileDialogField.getText();
	}

	public IPath getHeaderFilePath() {
	    String name = getHeaderFileName();
	    if (name.length() > 0) {
	        IPath headerPath = new Path(name);
	        if (headerPath.isAbsolute()) {
	            return headerPath;
	        } else if (fCurrentSourceFolder != null) {
	            return fCurrentSourceFolder.getPath().append(headerPath);
	        }
	    }
	    return null;
	}
	
	/**
	 * Returns the type name entered into the type input field.
	 * 
	 * @return the type name
	 */
	public String getSourceFileName() {
		return fSourceFileDialogField.getText();
	}

	public IPath getSourceFilePath() {
	    String name = getSourceFileName();
	    if (name.length() > 0) {
	        IPath headerPath = new Path(name);
	        if (headerPath.isAbsolute()) {
	            return headerPath;
	        } else if (fCurrentSourceFolder != null) {
	            return fCurrentSourceFolder.getPath().append(headerPath);
	        }
	    }
	    return null;
	}

	public IProject getCurrentProject() {
	    if (fCurrentSourceFolder != null) {
	        return fCurrentSourceFolder.getCProject().getProject();
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
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			setFocus();
		}
	}

	/**
	 * Sets the focus on the type name input field.
	 */		
	protected void setFocus() {
		fClassNameDialogField.setFocus();
	}
				
	/**
	 * Sets the type name input field's text to the given value. Method doesn't update
	 * the model.
	 * 
	 * @param name the new type name
	 * @param canBeModified if <code>true</code> the type name field is
	 * editable; otherwise it is read-only.
	 */	
	public void setClassName(String name, boolean canBeModified) {
		fClassNameDialogField.setText(name);
		fClassNameDialogField.setEnabled(canBeModified);
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
            prepareTypeCache();
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

	ITypeSearchScope prepareTypeCache() {
	    IProject project = getCurrentProject();
	    if (project == null)
	        return null;
	
	    final ITypeSearchScope scope = new TypeSearchScope();
	    scope.add(project);
	
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
				return null;
			} catch (InterruptedException e) {
				// cancelled by user
			    return null;
			}
		}
		return scope;
	}

	/**
	 * Sets the current source folder (model and text field) to the given source folder.
	 * 
	 * @param folder The new folder.
	 * @param canBeModified if <code>false</code> the source folder field can 
	 * not be changed by the user. If <code>true</code> the field is editable
	 */ 
	public void setSourceFolder(ICElement folder, boolean canBeModified) {
	    if (folder instanceof ICContainer) {
			fCurrentSourceFolder = (ICContainer)folder;
	    } else {
	        fCurrentSourceFolder = null;
	    }
		String str = (folder == null) ? "" : folder.getPath().makeRelative().toString(); //$NON-NLS-1$
		fSourceFolderDialogField.setText(str);
		fSourceFolderDialogField.setEnabled(canBeModified);
	}	
		
	/**
	 * Sets the namespace to the given value. The method updates the model 
	 * and the text of the control.
	 * 
	 * @param namespace the namespace to be set
	 * @param canBeModified if <code>true</code> the namespace is
	 * editable; otherwise it is read-only.
	 */
	public void setNamespace(ITypeInfo namespace, boolean canBeModified) {
		fCurrentNamespace = namespace;
		fCanModifyNamespace = canBeModified;
		if (namespace != null) {
		    String name = namespace.getQualifiedTypeName().getFullyQualifiedName();
			fNamespaceDialogField.setText(name);
		} else {
		    fNamespaceDialogField.setText(""); //$NON-NLS-1$
		}
		updateEnableState();
	}

	/**
	 * Sets the enclosing type. The method updates the underlying model 
	 * and the text of the control.
	 * 
	 * @param type the enclosing type
	 * @param canBeModified if <code>true</code> the enclosing type field is
	 * editable; otherwise it is read-only.
	 */	
	public void setEnclosingClass(ITypeInfo enclosingClass, boolean canBeModified) {
		fCurrentEnclosingClass = enclosingClass;
		fCanModifyEnclosingClass = canBeModified;
		if (enclosingClass != null) {
		    String name = enclosingClass.getQualifiedTypeName().getFullyQualifiedName();
			fEnclosingClassDialogField.setText(name);
		} else {
			fEnclosingClassDialogField.setText(""); //$NON-NLS-1$
		}
		updateEnableState();
	}
	
	/**
	 * Sets the enclosing class checkbox's selection state.
	 * 
	 * @param isSelected the checkbox's selection state
	 * @param canBeModified if <code>true</code> the enclosing class checkbox is
	 * modifiable; otherwise it is read-only.
	 */
	public void setEnclosingClassSelection(boolean isSelected, boolean canBeModified) {
		fEnclosingClassSelection.setSelection(isSelected);
		fEnclosingClassSelection.setEnabled(canBeModified);
		updateEnableState();
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
		updateEnableState();
	}

	/**
	 * Sets the current header file.
	 * 
	 * @param path The new header path
	 * @param canBeModified if <code>false</code> the header file field can 
	 * not be changed by the user. If <code>true</code> the field is editable
	 */ 
	public void setHeaderFile(IPath headerPath, boolean canBeModified) {
        fCurrentHeaderFile = headerPath;
	    if (fCurrentSourceFolder != null) {
	        IPath sourcePath = fCurrentSourceFolder.getPath();
	        IPath relPath = NewClassCodeGenerator.makeRelativePath(headerPath, sourcePath);
	        if (relPath != null)
	            headerPath = relPath;
	    }
		String str = (headerPath == null) ? "" : headerPath.makeRelative().toString(); //$NON-NLS-1$
		fHeaderFileDialogField.setText(str);
		fHeaderFileDialogField.setEnabled(!isUseDefaultSelected() && canBeModified);
	}	

	/**
	 * Sets the current source file.
	 * 
	 * @param path The new source path
	 * @param canBeModified if <code>false</code> the header file field can 
	 * not be changed by the user. If <code>true</code> the field is editable
	 */ 
	public void setSourceFile(IPath sourcePath, boolean canBeModified) {
		fCurrentSourceFile = sourcePath;
	    if (fCurrentSourceFolder != null && fCurrentSourceFolder.getPath().isPrefixOf(sourcePath)) {
	        sourcePath = sourcePath.removeFirstSegments(fCurrentSourceFolder.getPath().segmentCount());
	    }
		String str = (sourcePath == null) ? "" : sourcePath.makeRelative().toString(); //$NON-NLS-1$
		fSourceFileDialogField.setText(str);
		fSourceFileDialogField.setEnabled(!isUseDefaultSelected() && canBeModified);
	}	

	/*
	 * Updates the enable state of buttons related to the enclosing class selection checkbox.
	 */
	void updateEnableState() {
		IProject project = getCurrentProject();
		boolean validProject = (project != null);
	    fBaseClassesDialogField.setEnabled(validProject);

		boolean filegroup = !isUseDefaultSelected();
		fHeaderFileDialogField.setEnabled(validProject && filegroup);
	    fSourceFileDialogField.setEnabled(validProject && filegroup);

		boolean enclosing = isEnclosingClassSelected();
		fNamespaceDialogField.setEnabled(validProject && fCanModifyNamespace && !enclosing);
		fEnclosingClassDialogField.setEnabled(validProject && fCanModifyEnclosingClass && enclosing);
	}
	
	ICElement chooseSourceFolder(ICElement initElement) {
		Class[] acceptedClasses = new Class[] { ICContainer.class, ICProject.class };
		TypedElementSelectionValidator validator = new TypedElementSelectionValidator(acceptedClasses, false) {
			public boolean isSelectedValid(Object element) {
				if (element instanceof ICProject) {
					ICProject cproject = (ICProject)element;
					IPath path = cproject.getProject().getFullPath();
					return (cproject.findSourceRoot(path) != null);
				} else if (isValidSourceFolder(element)) {
				    return true;
				}
				return false;
			}
		};
		
		acceptedClasses = new Class[] { ICModel.class, ICContainer.class, ICProject.class };
		ViewerFilter filter = new TypedViewerFilter(acceptedClasses) {
			public boolean select(Viewer viewer, Object parent, Object element) {
				if (isValidSourceFolder(element)) {
			        return true;
			    }
				return super.select(viewer, parent, element);
			}
		};

		CElementContentProvider provider = new CElementContentProvider();
		ILabelProvider labelProvider = new CElementLabelProvider(CElementLabelProvider.SHOW_DEFAULT); 
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), labelProvider, provider);
		dialog.setValidator(validator);
		dialog.setSorter(new CElementSorter());
		dialog.setTitle(NewClassWizardMessages.getString("NewClassCreationWizardPage.ChooseSourceFolderDialog.title")); //$NON-NLS-1$
		dialog.setMessage(NewClassWizardMessages.getString("NewClassCreationWizardPage.ChooseSourceFolderDialog.description")); //$NON-NLS-1$
		dialog.addFilter(filter);
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
					    return folder;
				}
				return element;
			}
		}
		return null;
	}	
	
	boolean isValidSourceFolder(Object obj) {
		return (obj instanceof ICContainer
		        && CModelUtil.getSourceFolder((ICContainer)obj) != null);
	}
	
	boolean isValidHeaderFile(Object obj) {
		return (obj instanceof ITranslationUnit
		        && CModelUtil.getSourceFolder((ITranslationUnit)obj) != null);
	}

	boolean isValidSourceFile(Object obj) {
		return (obj instanceof ITranslationUnit
		        && CModelUtil.getSourceFolder((ITranslationUnit)obj) != null);
	}

	ITypeInfo chooseNamespace() {
	    ITypeSearchScope scope = prepareTypeCache();
	    if (scope == null)
	        return null;

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
	    ITypeSearchScope scope = prepareTypeCache();
	    if (scope == null)
	        return null;

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
	    ITypeSearchScope scope = prepareTypeCache();
	    if (scope == null)
	        return;

		ITypeInfo[] elements = AllTypesCache.getTypes(scope, CLASS_TYPES);
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

	IPath chooseSourceFile(IPath filePath, String title) {
	    SourceFileSelectionDialog dialog = new SourceFileSelectionDialog(getShell());
		ICElement input = CoreModel.create(fWorkspaceRoot);
		if (fCurrentSourceFolder != null) {
		    ICProject cproject = fCurrentSourceFolder.getCProject();
		    if (cproject != null)
		        input = cproject;
		}
	    dialog.setInput(input);
	    if (filePath != null) {
	        dialog.setInitialFields(filePath.removeLastSegments(1).toString(), filePath.lastSegment());
	    } else if (fCurrentSourceFolder != null) {
	        dialog.setInitialFields(fCurrentSourceFolder.getPath().toString(), null);
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
	public void createClass(IProgressMonitor monitor) throws CoreException, InterruptedException {
        fCodeGenerator = new NewClassCodeGenerator(
            getHeaderFilePath(),
            getSourceFilePath(),
            getClassName(),
            getNamespace(),
            getBaseClasses(),
            getCheckedMethodStubs());
	    fCodeGenerator.createClass(monitor);
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
}
