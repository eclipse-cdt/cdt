/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.ui.wizards;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.browser.UnknownTypeInfo;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.wizards.BaseClassSelectionDialog;
import org.eclipse.cdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LinkToFileGroup;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.Separator;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.CodeGeneration;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Wizard page to  create a new class. 
 * <p>
 * Note: This class is not intended to be subclassed. To implement a different kind of 
 * a new class wizard page, extend <code>NewTypeWizardPage</code>.
 * </p>
 * 
 */

public class NewClassWizardPage extends WizardPage implements Listener {
	// the page name
	private final static String PAGE_NAME= "NewClassWizardPage"; //$NON-NLS-1$
	private final String HEADER_EXT = ".h"; //$NON-NLS-1$
	private final String BODY_EXT = ".cpp"; //$NON-NLS-1$
	
	// the current resource selection
	private	IStructuredSelection currentSelection;
	// cache of C Model current selection 
	private ICElement eSelection = null;
	// default location where source files will be created
	private IPath defaultSourceFolder = null;
	private ITypeSearchScope fClassScope = null;
	private final int[] fClassTypes= { ICElement.C_CLASS, ICElement.C_STRUCT };
	private IProject fSelectedProject = null;

	// cache of newly-created files
	private ITranslationUnit parentHeaderTU = null;
	private ITranslationUnit parentBodyTU = null;
	// the created class element
	private /*IStructure*/ ICElement createdClass = null;
	
		 private ITypeInfo[] elementsOfTypeClassInProject = null;
	
	// Controls
	private StringDialogField fClassNameDialogField;
	private StringButtonDialogField fBaseClassDialogField;
	private SelectionButtonDialogFieldGroup fAccessButtons;	
	private SelectionButtonDialogFieldGroup fConstDestButtons;
	private LinkToFileGroup linkedResourceGroupForHeader;
	private LinkToFileGroup linkedResourceGroupForBody;
		
	private final int PUBLIC_INDEX= 0, PROTECTED_INDEX= 1, PRIVATE_INDEX= 2, DEFAULT_INDEX= 3;

	private IStatus fCurrStatus;
	protected IStatus fClassNameStatus;
	protected IStatus fBaseClassStatus;
	protected IStatus fLinkedResourceGroupForHeaderStatus;
	protected IStatus fLinkedResourceGroupForBodyStatus;

	private boolean hasCppNature = false;
	

	// -------------------- Initialization ------------------
	public NewClassWizardPage(IStructuredSelection selection) {
		super(PAGE_NAME);	
		currentSelection = selection;
		hasCppNature = isSelectionCPP(currentSelection);
		if(hasCppNature){
			initializePageControls();
		}
	}
	
	protected void initializePageControls(){
		TypeFieldsAdapter adapter= new TypeFieldsAdapter();

		fClassNameDialogField= new StringDialogField();
		fClassNameDialogField.setDialogFieldListener(adapter);
		fClassNameDialogField.setLabelText(NewWizardMessages.getString("NewClassWizardPage.classname.label")); //$NON-NLS-1$

		fBaseClassDialogField= new StringButtonDialogField(adapter);
		fBaseClassDialogField.setDialogFieldListener(adapter);
		fBaseClassDialogField.setLabelText(NewWizardMessages.getString("NewClassWizardPage.baseclass.label")); //$NON-NLS-1$
		fBaseClassDialogField.setButtonLabel(NewWizardMessages.getString("NewClassWizardPage.baseclass.button")); //$NON-NLS-1$

		String[] buttonNames1= new String[] {
			/* 0 == PUBLIC_INDEX */ NewWizardMessages.getString("NewClassWizardPage.baseclass.access.public"), //$NON-NLS-1$
			/* 1 == PROTECTED_INDEX */ NewWizardMessages.getString("NewClassWizardPage.baseclass.access.protected"), //$NON-NLS-1$
			/* 2 == PRIVATE_INDEX */ NewWizardMessages.getString("NewClassWizardPage.baseclass.access.private"), //$NON-NLS-1$
			/* 3 == DEFAULT_INDEX */ NewWizardMessages.getString("NewClassWizardPage.baseclass.access.access") //$NON-NLS-1$
		};
		fAccessButtons= new SelectionButtonDialogFieldGroup(SWT.RADIO, buttonNames1, 4);
		fAccessButtons.setDialogFieldListener(adapter);
		fAccessButtons.setLabelText(NewWizardMessages.getString("NewClassWizardPage.baseclass.access.label")); //$NON-NLS-1$
		fAccessButtons.setSelection(0, true);

		String[] buttonNames2= new String[] {
			/* 0 == INLINE_INDEX */ NewWizardMessages.getString("NewClassWizardPage.constdest.inline"), //$NON-NLS-1$
			/* 1 == VIRTUAL_DEST_INDEX */ NewWizardMessages.getString("NewClassWizardPage.constdest.virtualdestructor"), //$NON-NLS-1$
			/* 2 == INCLUDE_GUARD_INDEX */ NewWizardMessages.getString("NewClassWizardPage.constdest.includeguard"), //$NON-NLS-1$
		};				
		
		fConstDestButtons= new SelectionButtonDialogFieldGroup(SWT.CHECK, buttonNames2, 3);
		fConstDestButtons.setDialogFieldListener(adapter);
		fConstDestButtons.setSelection(1, true);
		fConstDestButtons.setSelection(2, true);
				
		linkedResourceGroupForHeader = new LinkToFileGroup(adapter, this);
		linkedResourceGroupForHeader.setDialogFieldListener(adapter);
		linkedResourceGroupForHeader.setLabelText(NewWizardMessages.getString("NewClassWizardPage.files.header")); //$NON-NLS-1$
		linkedResourceGroupForBody = new LinkToFileGroup(adapter, this);
		linkedResourceGroupForBody.setDialogFieldListener(adapter);
		linkedResourceGroupForBody.setLabelText(NewWizardMessages.getString("NewClassWizardPage.files.body")); //$NON-NLS-1$

		fClassNameStatus=  new StatusInfo();
		((StatusInfo)fClassNameStatus).setError(NewWizardMessages.getString("NewClassWizardPage.error.EnterClassName")); //$NON-NLS-1$
		fBaseClassStatus=  new StatusInfo();
		fLinkedResourceGroupForHeaderStatus=  new StatusInfo();
		fLinkedResourceGroupForBodyStatus=  new StatusInfo();
	}
	
	public void init() {
		eSelection = getSelectionCElement(currentSelection);
		fSelectedProject = getSelectionProject(currentSelection);
		
		fClassScope = new TypeSearchScope();
		fClassScope.add(fSelectedProject);

		IResource resource = getSelectionResourceElement(currentSelection);
		if (resource != null)
			defaultSourceFolder = resource.getFullPath();
		if (fSelectedProject != null && hasCppNature && defaultSourceFolder != null) {
			fAccessButtons.setEnabled(false);
			setPageComplete(false);
		} else {
			eSelection = null;
			defaultSourceFolder = null;
			StatusInfo status = new StatusInfo();
			status.setError(NewWizardMessages.getString("NewClassWizardPage.error.NotAvailableForNonCppProjects")); //$NON-NLS-1$
			updateStatus(status);
		}
	}	
	
	// ----------------- Creating Controls -------------------- 			
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		if(!hasCppNature)
		{
			setControl(new Composite(parent, SWT.NULL));
			return;
		}
			
		int nColumns= 5;
				
		initializeDialogUnits(parent);
		// top level group
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(nColumns, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		composite.setFont(parent.getFont());
		//WorkbenchHelp.setHelp(composite, IHelpContextIds.NEW_FILE_WIZARD_PAGE);

		createClassNameControls(composite, nColumns);
		createBaseClassControls(composite, nColumns);		
		createModifierControls(composite, nColumns);
		createSeparator(composite, nColumns);
		createFilesControls(composite, nColumns);
		
		composite.layout();			

		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		
	}
	
	protected void createClassNameControls(Composite composite, int nColumns) {		
		fClassNameDialogField.doFillIntoGrid(composite, nColumns - 1);
		DialogField.createEmptySpace(composite);			
		LayoutUtil.setWidthHint(fClassNameDialogField.getTextControl(null), getMaxFieldWidth());
	}

	protected void createBaseClassControls(Composite composite, int nColumns) {
		fBaseClassDialogField.doFillIntoGrid(composite, nColumns);
		LayoutUtil.setWidthHint(fBaseClassDialogField.getTextControl(null), getMaxFieldWidth());
		createAccessControls(composite, nColumns);
	}
	
	protected void createAccessControls(Composite composite, int nColumns){
		LayoutUtil.setHorizontalSpan(fAccessButtons.getLabelControl(composite), 1);
		
		Control control= fAccessButtons.getSelectionButtonsGroup(composite);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= nColumns - 2;
		control.setLayoutData(gd);
		
		DialogField.createEmptySpace(composite);
		
	}

	protected void createModifierControls(Composite composite, int nColumns) {
		LayoutUtil.setHorizontalSpan(fConstDestButtons.getLabelControl(composite), 1);
		
		Control control= fConstDestButtons.getSelectionButtonsGroup(composite);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= nColumns - 1;
		control.setLayoutData(gd);		

		DialogField.createEmptySpace(composite);
	}

	
	protected void createFilesControls(Composite composite, int nColumns){
		linkedResourceGroupForHeader.doFillIntoGrid(composite, nColumns);
		linkedResourceGroupForBody.doFillIntoGrid(composite, nColumns);
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
	 * Returns the recommended maximum width for text fields (in pixels). This
	 * method requires that createContent has been called before this method is
	 * call. Subclasses may override to change the maximum width for text 
	 * fields.
	 * 
	 * @return the recommended maximum width for text fields.
	 */
	protected int getMaxFieldWidth() {
		return convertWidthInCharsToPixels(40);
	}
	
	// --------------------- listeners --------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {

	}
	
	private class TypeFieldsAdapter implements IStringButtonAdapter, IDialogFieldListener, IListAdapter {
		
		// -------- IStringButtonAdapter
		public void changeControlPressed(DialogField field) {
			classPageChangeControlPressed(field);
		}
		
		// -------- IListAdapter
		public void customButtonPressed(ListDialogField field, int index) {
			classPageCustomButtonPressed(field, index);
		}
		
		public void selectionChanged(ListDialogField field) {}
		
		// -------- IDialogFieldListener
		public void dialogFieldChanged(DialogField field) {
			classPageDialogFieldChanged(field);
		}
		
		public void doubleClicked(ListDialogField field) {
		}
	}	
	
	private void classPageChangeControlPressed(DialogField field) {
		if (field == fBaseClassDialogField) {
			ITypeInfo info= chooseBaseClass();
	 		if (info != null) {
	 			fBaseClassDialogField.setText(info.getQualifiedTypeName().getFullyQualifiedName());
			}
		}
	}
	
	private void classPageCustomButtonPressed(DialogField field, int index) {		
	}
	
	/*
	 * A field on the type has changed. The fields' status and all dependend
	 * status are updated.
	 */
	private void classPageDialogFieldChanged(DialogField field) {
		if(field == fClassNameDialogField){
			String text = fClassNameDialogField.getText();
			if(!linkedResourceGroupForHeader.linkCreated()){
				if (text.length() > 0) {
					linkedResourceGroupForHeader.setText(text + HEADER_EXT);
				} else {
					linkedResourceGroupForHeader.setText(text);
				}				
			}
			if(!linkedResourceGroupForBody.linkCreated()){
				if (text.length() > 0) {			
					linkedResourceGroupForBody.setText(text + BODY_EXT);
				} else{				
					linkedResourceGroupForBody.setText(text);
				}
			}
			fClassNameStatus = classNameChanged();
		}
		
		if(field == fBaseClassDialogField){
			String text = fBaseClassDialogField.getText();
			fAccessButtons.setEnabled(text.length() > 0);
			fBaseClassStatus = baseClassNameChanged();
		}
		
		if (field == linkedResourceGroupForHeader) {
			fLinkedResourceGroupForHeaderStatus = linkedResourceGroupChanged(linkedResourceGroupForHeader, true);
		}
		if (field == linkedResourceGroupForBody) {
			fLinkedResourceGroupForBodyStatus = linkedResourceGroupChanged(linkedResourceGroupForBody, false);
		}
		
		doStatusUpdate();		
	}		

	IStatus linkedResourceGroupChanged(LinkToFileGroup linkedGroup, boolean isHeader) {
		StatusInfo status = new StatusInfo();
		if (linkedGroup.linkCreated()) {
			// must not be empty
			String text = linkedGroup.getText();
			if (text == null || text.length() == 0) {
				if (isHeader)
					status.setError(NewWizardMessages.getString("NewClassWizardPage.error.EnterHeaderFile")); //$NON-NLS-1$
				else
					status.setError(NewWizardMessages.getString("NewClassWizardPage.error.EnterBodyFile")); //$NON-NLS-1$
			} else {
				// check if file exists
				IPath filePath = getContainerFullPath(linkedGroup);
				boolean validFile = false;
				if (filePath != null) {
					File f = filePath.toFile();
					validFile = (f != null && f.exists() && f.isFile());
				}
				if (!validFile) {
					if (isHeader)
						status.setError(NewWizardMessages.getString("NewClassWizardPage.error.NoHeaderFile")); //$NON-NLS-1$
					else
						status.setError(NewWizardMessages.getString("NewClassWizardPage.error.NoBodyFile")); //$NON-NLS-1$
				}
			}
		} else {
			String text = fClassNameDialogField.getText();
			if (isHeader) {
				if (text.length() > 0) {
					linkedGroup.setText(text + HEADER_EXT);
				} else {
					linkedGroup.setText(text);
				}				
			} else {
				if (text.length() > 0) {			
					linkedGroup.setText(text + BODY_EXT);
				} else{				
					linkedGroup.setText(text);
				}
			}
		}
		return status;
	}
	
	// --------------- Helper methods for creating controls -----
	public boolean selectionIsCpp(){
		return hasCppNature;
	}
	
	private boolean isSelectionCPP(IStructuredSelection sel){
		IProject project = getSelectionProject(sel);
		if (project != null)
			return CoreModel.hasCCNature(project);
		else
			return false;
	}
	
	private ICElement getSelectionCElement(IStructuredSelection selection) {
		ICElement elem= null;
		if (selection != null && !selection.isEmpty()) {
			Object selectedElement= selection.getFirstElement();
			if (selectedElement instanceof IAdaptable) {
				IAdaptable adaptable= (IAdaptable) selectedElement;
				elem= (ICElement) adaptable.getAdapter(ICElement.class);
				if (elem == null) {
					IResource resource= (IResource) adaptable.getAdapter(IResource.class);
					if (resource != null && resource.getType() != IResource.ROOT) {
						while (elem == null && resource.getType() != IResource.PROJECT) {
							resource= resource.getParent();
							elem= (ICElement) resource.getAdapter(ICElement.class);
						}
						if (elem == null) {
							elem= CoreModel.getDefault().create(resource); // C project
						}
					}
				}
			}
		}

		if (elem == null || elem.getElementType() == ICElement.C_MODEL) {
			try {
				ICProject[] projects = CoreModel.create(CUIPlugin.getWorkspace().getRoot()).getCProjects();
				if (projects.length == 1) {
					elem= projects[0];
				}
			} catch (CModelException e) {
			}
		}
		return elem;
	}

	private IResource getSelectionResourceElement(IStructuredSelection selection) {
		IResource resource= null;
		if (selection != null && !selection.isEmpty()) {
			Object selectedElement= selection.getFirstElement();
			if (selectedElement instanceof IAdaptable) {
				IAdaptable adaptable= (IAdaptable) selectedElement;
				resource= (IResource) adaptable.getAdapter(IResource.class);
				if (resource != null && resource instanceof IFile)
					resource= resource.getParent();
			}
		}
		return resource;
	}
	
	private IProject getSelectionProject(IStructuredSelection selection) {
		if (selection != null && !selection.isEmpty()) {
			Object selectedElement= selection.getFirstElement();
			if (selectedElement instanceof IAdaptable) {
				IAdaptable adaptable= (IAdaptable) selectedElement;
				IResource resource= (IResource) adaptable.getAdapter(IResource.class);
				if (resource != null) {
					return resource.getProject();
				}
			}
		}
		return null;
	}
	
	private ITypeInfo[] findClassElementsInProject(){		 		 
		if(eSelection == null){
			return null;
		}
		
		if(	elementsOfTypeClassInProject != null ){
			return elementsOfTypeClassInProject;
		}
		
		if (!AllTypesCache.isCacheUpToDate(fClassScope)) {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					AllTypesCache.updateCache(fClassScope, monitor);
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
				}
			};
			
			try {
				getContainer().run(true, true, runnable);
			} catch (InvocationTargetException e) {
				String title= NewWizardMessages.getString("NewClassWizardPage.getProjectClasses.exception.title"); //$NON-NLS-1$
				String message= NewWizardMessages.getString("NewClassWizardPage.getProjectClasses.exception.message"); //$NON-NLS-1$
				ExceptionHandler.handle(e, title, message);
				elementsOfTypeClassInProject = new ITypeInfo[0];
				return elementsOfTypeClassInProject;
			} catch (InterruptedException e) {
				// cancelled by user
				elementsOfTypeClassInProject = new ITypeInfo[0];
				return elementsOfTypeClassInProject;
			}
		}

		elementsOfTypeClassInProject = AllTypesCache.getTypes(fClassScope, fClassTypes);
		Arrays.sort(elementsOfTypeClassInProject, TYPE_NAME_COMPARATOR);
		return elementsOfTypeClassInProject;				
	}
	
	protected ITypeInfo chooseBaseClass(){
		ITypeInfo[] elementsFound= findClassElementsInProject();
		if (elementsFound == null || elementsFound.length == 0) {
			String title= NewWizardMessages.getString("NewClassWizardPage.getProjectClasses.noclasses.title"); //$NON-NLS-1$
			String message= NewWizardMessages.getString("NewClassWizardPage.getProjectClasses.noclasses.message"); //$NON-NLS-1$
			MessageDialog.openInformation(getShell(), title, message);
			return null;
		}
		
		BaseClassSelectionDialog dialog= new BaseClassSelectionDialog(getShell());
		dialog.setElements(elementsFound);
		
		int result= dialog.open();
		if (result != IDialogConstants.OK_ID)
			return null;
		
		return (ITypeInfo)dialog.getFirstResult();
	}
	
	// ------------- getter methods for dialog controls ------------- 			
	public String getNewClassName(){
		return fClassNameDialogField.getText();		
	}
	
	public String getBaseClassName(){
		return fBaseClassDialogField.getText();
	}
	
	public boolean isIncludeGuard(){
		return fConstDestButtons.isSelected(2);	
	}
	
	public boolean isVirtualDestructor(){
		return fConstDestButtons.isSelected(1);
	}
	
	public boolean isInline(){
		return fConstDestButtons.isSelected(0);
	}
	
	public String getAccess(){
		if(fAccessButtons.isSelected(0))
			return "public"; //$NON-NLS-1$
		else if(fAccessButtons.isSelected(1))
			return "protected"; //$NON-NLS-1$
		else if(fAccessButtons.isSelected(2))
			return "private"; //$NON-NLS-1$
		else return "";  //$NON-NLS-1$
	}
	
	public ITranslationUnit getCreatedClassHeaderFile(){
		return parentHeaderTU;
	}

	public ITranslationUnit getCreatedClassBodyFile(){
		return parentBodyTU;
	}

	public /*IStructure*/ ICElement getCreatedClassElement(){
		return createdClass;
	}
	
	public IStructure getBaseClassElement(){
		
		return null;
	}
	// -------------- Create a new Class  ----------------------	
	
	public boolean createClass(IProgressMonitor monitor){		
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}

		monitor.beginTask(NewWizardMessages.getString("NewTypeWizardPage.operationdesc"), 10); //$NON-NLS-1$

		// resolve location of base class
		String baseClassName = getBaseClassName();
		ITypeInfo baseClass = null;
		if ((baseClassName != null) && (baseClassName.length() > 0))
		{
			ITypeInfo[] classElements = findClassElementsInProject();
			baseClass = findInList(classElements, new QualifiedTypeName(baseClassName));
			if (baseClass != null && baseClass.getResolvedReference() == null) {
				final ITypeInfo[] typesToResolve = new ITypeInfo[] { baseClass };
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
					String title= NewWizardMessages.getString("NewClassWizardPage.getProjectClasses.exception.title"); //$NON-NLS-1$
					String message= NewWizardMessages.getString("NewClassWizardPage.getProjectClasses.exception.message"); //$NON-NLS-1$
					ExceptionHandler.handle(e, title, message);
					return false;
				} catch (InterruptedException e) {
					// cancelled by user
					return false;
				}
			}
		}
		
		String lineDelimiter= null;	
		lineDelimiter= System.getProperty("line.separator", "\n");  //$NON-NLS-1$ //$NON-NLS-2$
		
		parentHeaderTU = createTranslationUnit(linkedResourceGroupForHeader, true);		
		parentBodyTU = createTranslationUnit(linkedResourceGroupForBody, false);		
		monitor.worked(1);
		
		if(parentHeaderTU != null && !parentHeaderTU.isReadOnly()){
			String header = constructHeaderFileContent(parentHeaderTU, lineDelimiter, baseClass);
			IWorkingCopy headerWC = null;
			try {
				headerWC = parentHeaderTU.getSharedWorkingCopy(null, CUIPlugin.getDefault().getBufferFactory());
				headerWC.getBuffer().append(header);
				headerWC.reconcile();	
				headerWC.commit(true, monitor);
				//createdClass= (IStructure)headerWC.getElement(getNewClassName());				
				createdClass= headerWC.getElement(getNewClassName());
			} catch (CModelException cme) {
				MessageDialog.openError(getContainer().getShell(), WorkbenchMessages.getString("WizardNewFileCreationPage.internalErrorTitle"), cme.getMessage()); //$NON-NLS-2$ //$NON-NLS-1$
			} finally {
				if (headerWC != null) {
					headerWC.destroy();
				}
			}
		}
		if(parentBodyTU != null && !parentBodyTU.isReadOnly()){
			String body = constructBodyFileContent(lineDelimiter);
			IWorkingCopy bodyWC = null;
			try {
				bodyWC = parentBodyTU.getSharedWorkingCopy(null, CUIPlugin.getDefault().getBufferFactory());
				bodyWC.getBuffer().append(body);
				bodyWC.reconcile();
				bodyWC.commit(true, monitor);
			} catch (CModelException cme) {
				MessageDialog.openError(getContainer().getShell(), WorkbenchMessages.getString("WizardNewFileCreationPage.internalErrorTitle"), cme.getMessage()); //$NON-NLS-2$ //$NON-NLS-1$
			} finally {
				if (bodyWC != null) {
					bodyWC.destroy();
				}
			}
		}
		monitor.done();
		return true;	
	}
	
	protected ITranslationUnit createTranslationUnit(LinkToFileGroup linkedGroup, boolean isHeader){
		ITranslationUnit createdUnit = null;
		IFile createdFile = null;
		createdFile= createNewFile(linkedGroup, isHeader);
		// turn the file into a translation unit
		if(createdFile != null){
			Object element= CoreModel.getDefault().create(createdFile);
			if (element instanceof ITranslationUnit)
				createdUnit = (ITranslationUnit) element;
		}
		
		return createdUnit;
	}
		
	protected IFile createNewFile(LinkToFileGroup linkedGroup, boolean isHeader) {
		final IFile newFileHandle = createFileHandle(linkedGroup, isHeader);

		if(newFileHandle.exists()){
			return newFileHandle;
		}

		// create the new file and cache it if successful
		final IPath newFilePath = getContainerFullPath(linkedGroup);
		final boolean isLinkedFile = linkedGroup.linkCreated();
		final IPath containerPath = getContainerPath(linkedGroup);
		final InputStream initialContents = getInitialContents();
	
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws CoreException,
				InterruptedException
			{
				try {
					monitor.beginTask(WorkbenchMessages.getString("WizardNewFileCreationPage.progress"), 2000); //$NON-NLS-1$
					if(!(containerExists(containerPath))){
						ContainerGenerator generator = new ContainerGenerator(containerPath);
						generator.generateContainer(new SubProgressMonitor(monitor, 1000));
					}
					createFile(newFileHandle,initialContents, newFilePath, isLinkedFile, new SubProgressMonitor(monitor, 1000));
				} finally {
					monitor.done();
				}
			}
		};

		try {
			getContainer().run(false, true, op);
		} catch (InterruptedException e) {
			return null;
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof CoreException) {
				ErrorDialog.openError(
					getContainer().getShell(), // Was Utilities.getFocusShell()
					WorkbenchMessages.getString("WizardNewFileCreationPage.errorTitle"),  //$NON-NLS-1$
					null,	// no special message
					((CoreException) e.getTargetException()).getStatus());
			}
			else {
				// CoreExceptions are handled above, but unexpected runtime exceptions and errors may still occur.
				WorkbenchPlugin.log(MessageFormat.format("Exception in {0}.getNewFile(): {1}", new Object[] {getClass().getName(), e.getTargetException()}));//$NON-NLS-1$
				MessageDialog.openError(getContainer().getShell(), WorkbenchMessages.getString("WizardNewFileCreationPage.internalErrorTitle"), WorkbenchMessages.format("WizardNewFileCreationPage.internalErrorMessage", new Object[] {e.getTargetException().getMessage()})); //$NON-NLS-2$ //$NON-NLS-1$
			}
			return null;
		}

		return newFileHandle;
	}

	protected IFile createFileHandle(LinkToFileGroup linkedGroup, boolean isHeader) {
		IWorkspaceRoot root= CUIPlugin.getWorkspace().getRoot();						
		IPath filePath = getContainerFullPath(linkedGroup);
		IFile newFile = root.getFileForLocation(filePath);
		if(newFile == null)
			newFile = root.getFile(filePath);
		return newFile;
	}

	protected void createFile(IFile fileHandle, InputStream contents, IPath linkTargetPath, boolean isLinkedFile, IProgressMonitor monitor) throws CoreException {
		if (contents == null)
			contents = new ByteArrayInputStream(new byte[0]);
			
		try {
			if (isLinkedFile) {
				fileHandle.createLink(linkTargetPath, IResource.ALLOW_MISSING_LOCAL, monitor);
			} else {
				fileHandle.create(contents, false, monitor);
			}
		}
		catch (CoreException e) {
			// If the file already existed locally, just refresh to get contents
			if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
				fileHandle.refreshLocal(IResource.DEPTH_ZERO, null);
			else
				throw e;
		}

		if (monitor.isCanceled())
			throw new OperationCanceledException();
	}
	
	
	/*
	 * returns the path without the file name
	 */
	protected IPath getContainerPath(LinkToFileGroup linkedGroup) {
		if(linkedGroup.linkCreated()){
			String pathName = linkedGroup.getResolvedPath();
			if((pathName == null) || (pathName.length() <1))
				return null;
			IPath containerPath = new Path (pathName);
			return containerPath.removeLastSegments(1).removeTrailingSeparator().makeAbsolute();
		}else {
			return defaultSourceFolder;			
		}
	}

	/*
	 * returns the path including the file name
	 */ 
	protected IPath getContainerFullPath(LinkToFileGroup linkedGroup) {
		if(linkedGroup.linkCreated()){
			String pathName = linkedGroup.getResolvedPath();
			if (pathName == null || pathName.length() < 1)
				return null;
			else
				//The user may not have made this absolute so do it for them
				return (new Path(pathName)).makeAbsolute();
		} else {
			String pathName = linkedGroup.getText();
			IPath containerPath = defaultSourceFolder;
			containerPath.addTrailingSeparator();
			return ((containerPath.append(pathName)).makeAbsolute());						
		}
	}

	/**
	 * return the path of the new ClassName
	 * @return
	 */
	protected IPath getHeaderFullPath() {
		String pathName = getNewClassName() + HEADER_EXT;
		IPath containerPath = defaultSourceFolder;
		//containerPath.addTrailingSeparator();
		return ((containerPath.append(pathName)).makeAbsolute());								
	}

	protected IPath getBodyFullPath() {
		String pathName = getNewClassName() + BODY_EXT;
		IPath containerPath = defaultSourceFolder;
		//containerPath.addTrailingSeparator();
		return ((containerPath.append(pathName)).makeAbsolute());								
	}

	protected boolean containerExists(IPath containerPath) {
		IContainer container = null;
		IWorkspaceRoot root= CUIPlugin.getWorkspace().getRoot();	
		container = root.getContainerForLocation(containerPath);
		if(container == null)
			return false;
					
		return true;
	}
	
	protected InputStream getInitialContents() {
		return null;
	}
	
	protected String getClassComment(ITranslationUnit parentTU) {
		if (PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.CODEGEN_ADD_COMMENTS)) {
			try {
				StringBuffer className= new StringBuffer();
				className.append(getNewClassName());
				String comment= CodeGeneration.getClassComment(parentTU, className.toString(), String.valueOf('\n'));
				if (comment != null && isValidComment(comment)) {
					return comment;
				}
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
		}
		return null;
	}
	
	// ------------ Constructing File Contents -----------------
	protected String constructHeaderFileContent(ITranslationUnit header, String lineDelimiter, ITypeInfo baseClass) {
		StringBuffer text = new StringBuffer();
		boolean extendingBase = false;
		String baseClassName = getBaseClassName();
		String baseClassFileName = ""; //$NON-NLS-1$
		boolean systemIncludePath = false;
		
		if (baseClass != null) {
			extendingBase = true;
			ITypeReference location = baseClass.getResolvedReference();
			if (location != null) {
				IPath projectPath = fSelectedProject.getFullPath();
				IPath relativePath = location.getRelativeIncludePath(fSelectedProject);
				if (!relativePath.equals(location.getLocation())) {
					systemIncludePath = true;
				} else {
					if (projectPath.isPrefixOf(location.getPath()) && projectPath.isPrefixOf(header.getPath()))
						relativePath = location.getRelativePath(header.getPath());
				}
				baseClassFileName = relativePath.toString();
			}
		}
		if (baseClassFileName.length() == 0) {
			baseClassFileName = baseClassName + HEADER_EXT;
		}
		
		if(isIncludeGuard()){
			text.append("#ifndef "); //$NON-NLS-1$
			text.append(getNewClassName().toUpperCase());
			text.append("_H"); //$NON-NLS-1$
			text.append(lineDelimiter);
			text.append("#define "); //$NON-NLS-1$
			text.append(getNewClassName().toUpperCase());
			text.append("_H"); //$NON-NLS-1$
			text.append(lineDelimiter);
			text.append(lineDelimiter);
		}
		// add the include statement if we are extending a base class
		// and we are not already in the base class header file (via link to file option )
		if((extendingBase) && (!(header.getElementName().equals(baseClassFileName)))){
			text.append("#include "); //$NON-NLS-1$
			if (systemIncludePath)
				text.append('<'); //$NON-NLS-1$
			else
				text.append('\"'); //$NON-NLS-1$
			text.append(baseClassFileName);
			if (systemIncludePath)
				text.append('>'); //$NON-NLS-1$
			else
				text.append('\"'); //$NON-NLS-1$
			text.append(lineDelimiter);			
			text.append(lineDelimiter);			
		}
		text.append("class "); //$NON-NLS-1$
		text.append(getNewClassName());
		if(extendingBase){
			text.append(" : "); //$NON-NLS-1$
			text.append(getAccess());
			text.append(" "); //$NON-NLS-1$
			text.append(baseClassName);
		}
		text.append("{"); //$NON-NLS-1$
		text.append(lineDelimiter);			
		
		text.append("public:"); //$NON-NLS-1$
		text.append(lineDelimiter);			
		text.append(lineDelimiter);			
		
		// constructor
		text.append('\t');
		text.append(getNewClassName());
		text.append("()"); //$NON-NLS-1$
		if(isInline()){
			text.append(" {}"); //$NON-NLS-1$
			text.append(lineDelimiter);						
		}else {
			text.append(";"); //$NON-NLS-1$
			text.append(lineDelimiter);						
		}
		
		// destructor
		text.append('\t');		
		if(isVirtualDestructor()){
			text.append("virtual "); //$NON-NLS-1$
		}
		text.append("~"); //$NON-NLS-1$
		text.append(getNewClassName());
		text.append("()"); //$NON-NLS-1$
		if(isInline()){
			text.append(" {}"); //$NON-NLS-1$
			text.append(lineDelimiter);						
		}else {
			text.append(";"); //$NON-NLS-1$
			text.append(lineDelimiter);						
		}
		text.append("};"); //$NON-NLS-1$
		text.append(lineDelimiter);		
		
		if(isIncludeGuard()){
			text.append(lineDelimiter);
			text.append("#endif // "); //$NON-NLS-1$
			text.append(getNewClassName().toUpperCase());
			text.append("_H"); //$NON-NLS-1$
			text.append(lineDelimiter);		
		}
		
		return text.toString();	 		
	}
	
	protected String constructBodyFileContent(String lineDelimiter){
		StringBuffer text = new StringBuffer();
		if(getCreatedClassHeaderFile() != null){
			text.append("#include \""); //$NON-NLS-1$
			text.append(getCreatedClassHeaderFile().getElementName());
		}
		text.append("\""); //$NON-NLS-1$
		text.append(lineDelimiter);			
		text.append(lineDelimiter);			
		
		if(isInline())
			return text.toString();
		
		// constructor
		text.append(getNewClassName());
		text.append("::");  //$NON-NLS-1$
		text.append(getNewClassName());
		text.append("()"); //$NON-NLS-1$
		text.append(lineDelimiter);			
		text.append("{}"); //$NON-NLS-1$
		text.append(lineDelimiter);			
		
		// destructor
		text.append(getNewClassName());
		text.append("::~");  //$NON-NLS-1$
		text.append(getNewClassName());
		text.append("()");  //$NON-NLS-1$
		text.append(lineDelimiter);			
		text.append("{}");  //$NON-NLS-1$
		text.append(lineDelimiter);				
		return text.toString();
	}
	
	// ------ validation --------
	protected void doStatusUpdate() {
		// status of all used components
		IStatus[] status= new IStatus[] {
			fClassNameStatus,
			fBaseClassStatus,
			fLinkedResourceGroupForHeaderStatus,
			fLinkedResourceGroupForBodyStatus
			};
		
		// the mode severe status will be displayed and the ok button enabled/disabled.
		updateStatus(status);
	}
	
	protected void updateStatus(IStatus[] status) {
		updateStatus(StatusUtil.getMostSevere(status));
	}

	protected void updateStatus(IStatus status) {
		fCurrStatus= status;
		setPageComplete(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);
	}

	protected boolean isValidComment(String template) {
		return true;
	}	
	
	// status validation
	/**
	 * Hook method that gets called when the class name has changed. The method validates the 
	 * class name and returns the status of the validation.
	 * <p>
	 * Subclasses may extend this method to perform their own validation.
	 * </p>
	 * 
	 * @return the status of the validation
	 */
	protected IStatus classNameChanged() {
		StatusInfo status= new StatusInfo();
		String className= getNewClassName();
		// must not be empty
		if (className.length() == 0) {
			status.setError(NewWizardMessages.getString("NewClassWizardPage.error.EnterClassName")); //$NON-NLS-1$
			return status;
		}
		if (className.indexOf("::") != -1) { //$NON-NLS-1$
			status.setError(NewWizardMessages.getString("NewClassWizardPage.error.QualifiedName")); //$NON-NLS-1$
			return status;
		}
		IStatus val= CConventions.validateClassName(className);
		if (val.getSeverity() == IStatus.ERROR) {
			status.setError(NewWizardMessages.getFormattedString("NewClassWizardPage.error.InvalidClassName", val.getMessage())); //$NON-NLS-1$
			return status;
		} else if (val.getSeverity() == IStatus.WARNING) {
			status.setWarning(NewWizardMessages.getFormattedString("NewClassWizardPage.warning.ClassNameDiscouraged", val.getMessage())); //$NON-NLS-1$
			// continue checking
		}		

		// must not exist
		ITypeInfo[] elementsFound = findClassElementsInProject();
		QualifiedTypeName typeName = new QualifiedTypeName(getNewClassName());
		if (foundInList(elementsFound, typeName)) {
			status.setError(NewWizardMessages.getString("NewClassWizardPage.error.ClassNameExists")); //$NON-NLS-1$
		}
		return status;
	}
	/**
	 * Hook method that gets called when the superclass name has changed. The method 
	 * validates the superclass name and returns the status of the validation.
	 * <p>
	 * Subclasses may extend this method to perform their own validation.
	 * </p>
	 * 
	 * @return the status of the validation
	 */
	protected IStatus baseClassNameChanged() {
		String baseClassName = getBaseClassName();		
		StatusInfo status= new StatusInfo();
		if (baseClassName.length() == 0) {
			// accept the empty field (stands for java.lang.Object)
			return status;
		}

		// class name must follow the C/CPP convensions
		IStatus val= CConventions.validateClassName(baseClassName);
		if (val.getSeverity() == IStatus.ERROR) {
			status.setError(NewWizardMessages.getString("NewClassWizardPage.error.InvalidBaseClassName")); //$NON-NLS-1$
			return status;
		} 

		// if class does not exist, give warning 
		ITypeInfo[] elementsFound = findClassElementsInProject();
		if (!foundInList(elementsFound, new QualifiedTypeName(baseClassName))) {
			status.setWarning(NewWizardMessages.getString("NewClassWizardPage.warning.BaseClassNotExists")); //$NON-NLS-1$
		}
		return status;		
	}
		
	private boolean foundInList(ITypeInfo[] elements, IQualifiedTypeName typeName){
		return (findInList(elements, typeName) != null);
	}
	
	private Comparator TYPE_NAME_COMPARATOR = new Comparator() {
		 public int compare(Object o1, Object o2) {
	 		 return ((ITypeInfo)o1).getName().compareTo(((ITypeInfo)o2).getName());
		 }
	};

	private ITypeInfo findInList(ITypeInfo[] elements, IQualifiedTypeName typeName) {
		if (elements == null || elements.length == 0)
			return null;

		ITypeInfo key = new UnknownTypeInfo(typeName);
		int index = Arrays.binarySearch(elements, key, TYPE_NAME_COMPARATOR);
		if (index >= 0 && index < elements.length) {
			for (int i = index - 1; i >= 0; --i) {
				ITypeInfo curr = elements[i];
				if (key.getName().equals(curr.getName())) {
					if (key.getQualifiedTypeName().equals(curr.getQualifiedTypeName())) {
						return curr;
					}
				} else {
					break;
				}
			}
			for (int i = index; i < elements.length; ++i) {
				ITypeInfo curr = elements[i];
				if (key.getName().equals(curr.getName())) {
					if (key.getQualifiedTypeName().equals(curr.getQualifiedTypeName())) {
						return curr;
					}
				} else {
					break;
				}
			}
		}
		return null;
	}	
}
