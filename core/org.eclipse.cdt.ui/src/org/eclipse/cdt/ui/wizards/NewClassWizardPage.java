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
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.cdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LinkToFileGroup;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.Separator;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.cdt.ui.CSearchResultLabelProvider;
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
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
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
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
	private final static String PAGE_NAME= "NewClassWizardPage";
	private final String HEADER_EXT = ".h";
	private final String BODY_EXT = ".cpp";
	
	// the current resource selection
	private	IStructuredSelection currentSelection;
	// cache of C Model current selection 
	private ICElement eSelection = null;
	
	// cache of newly-created files
	private ITranslationUnit parentHeaderTU = null;
	private ITranslationUnit parentBodyTU = null;
	// the created class element
	private /*IStructure*/ ICElement createdClass = null;
	
	private List elementsOfTypeClassInProject = null;
	
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

	private boolean hasCppNature = false;
	
	BasicSearchResultCollector  resultCollector;
	SearchEngine searchEngine;

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
		fClassNameDialogField.setLabelText(NewWizardMessages.getString("NewClassWizardPage.classname.label"));

		fBaseClassDialogField= new StringButtonDialogField(adapter);
		fBaseClassDialogField.setDialogFieldListener(adapter);
		fBaseClassDialogField.setLabelText(NewWizardMessages.getString("NewClassWizardPage.baseclass.label"));
		fBaseClassDialogField.setButtonLabel(NewWizardMessages.getString("NewClassWizardPage.baseclass.button"));

		String[] buttonNames1= new String[] {
			/* 0 == PUBLIC_INDEX */ NewWizardMessages.getString("NewClassWizardPage.baseclass.access.public"),
			/* 1 == PROTECTED_INDEX */ NewWizardMessages.getString("NewClassWizardPage.baseclass.access.protected"),
			/* 2 == PRIVATE_INDEX */ NewWizardMessages.getString("NewClassWizardPage.baseclass.access.private"),
			/* 3 == DEFAULT_INDEX */ NewWizardMessages.getString("NewClassWizardPage.baseclass.access.access")
		};
		fAccessButtons= new SelectionButtonDialogFieldGroup(SWT.RADIO, buttonNames1, 4);
		fAccessButtons.setDialogFieldListener(adapter);
		fAccessButtons.setLabelText(NewWizardMessages.getString("NewClassWizardPage.baseclass.access.label"));
		fAccessButtons.setSelection(0, true);

		String[] buttonNames2= new String[] {
			/* 0 == INLINE_INDEX */ NewWizardMessages.getString("NewClassWizardPage.constdest.inline"),
			/* 1 == VIRTUAL_DEST_INDEX */ NewWizardMessages.getString("NewClassWizardPage.constdest.virtualdestructor"),
			/* 2 == INCLUDE_GUARD_INDEX */ NewWizardMessages.getString("NewClassWizardPage.constdest.includeguard"),
		};				
		
		fConstDestButtons= new SelectionButtonDialogFieldGroup(SWT.CHECK, buttonNames2, 3);
		fConstDestButtons.setDialogFieldListener(adapter);
				
		linkedResourceGroupForHeader = new LinkToFileGroup(adapter, this);
		linkedResourceGroupForHeader.setLabelText(NewWizardMessages.getString("NewClassWizardPage.files.header"));
		linkedResourceGroupForBody = new LinkToFileGroup(adapter, this);
		linkedResourceGroupForBody.setLabelText(NewWizardMessages.getString("NewClassWizardPage.files.body"));

		fClassNameStatus=  new StatusInfo();
		fBaseClassStatus=  new StatusInfo();

		resultCollector = new BasicSearchResultCollector ();
		searchEngine = new SearchEngine();		
	}
	
	public void init() {
		if(hasCppNature){
			fAccessButtons.setEnabled(false);
			setPageComplete(false);
			eSelection = getSelectionCElement(currentSelection);						
		}else {
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
			BasicSearchMatch element= (BasicSearchMatch)chooseBaseClass();
			if (element != null) {
				fBaseClassDialogField.setText(element.getName());
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
			if(fBaseClassDialogField.getText().length() >= 0)
			{
				fAccessButtons.setEnabled(true);
				fBaseClassStatus = baseClassNameChanged();
			}
			else{
				fAccessButtons.setEnabled(false);
			}							
		}		
		doStatusUpdate();		
	}		
	
	// --------------- Helper methods for creating controls -----
	public boolean selectionIsCpp(){
		return hasCppNature;
	}
	
	private boolean isSelectionCPP(IStructuredSelection sel){
		IProject project = null;
		ICElement element = getSelectionCElement(sel);
		if (element == null){
			IResource resource = getSelectionResourceElement(sel);
			project = resource.getProject();
		}else {
			project = element.getCProject().getProject();
		}
		if (project != null)
			return CoreModel.getDefault().hasCCNature(project);
		else
			return false;
	}
	
	private ICElement getSelectionCElement(IStructuredSelection sel) {
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			List list= ((IStructuredSelection)sel).toList();
			if (list.size() == 1) {
				Object element= list.get(0);
				if (element instanceof ICElement) {
					return (ICElement)element;
				} 
			}
		}
		return null;
	}

	private IResource getSelectionResourceElement(IStructuredSelection sel) {
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			List list= ((IStructuredSelection)sel).toList();
			if (list.size() == 1) {
				Object element= list.get(0);
				if (element instanceof IResource) {
					if(element instanceof IFile){
						IFile file = (IFile)element;
						return (IResource) file.getParent();
					}else {
						return (IResource)element;
					}
				} 
			}
		}
		return null;
	}
	
	private void getChildrenOfTypeClass(IParent parent, List elementsFound, IProgressMonitor monitor, int worked){
		ICElement[] elements = parent.getChildren();
		monitor.worked( worked );
		
		for (int i = 0; i< elements.length; i++){
			ICElement element = (ICElement)elements[i];
			if(element.getElementType() == ICElement.C_CLASS){
				elementsFound.add(element);
			}
			if(element instanceof IParent){
				getChildrenOfTypeClass((IParent)element, elementsFound, monitor, worked+1);
			}
		}
	}
	
	private void searchForClasses(ICProject cProject, List elementsFound, IProgressMonitor monitor, int worked){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "*", ICSearchConstants.CLASS, ICSearchConstants.DECLARATIONS, false );
		ICElement[] elements = new ICElement[1];
		elements[0] = cProject;
		ICSearchScope scope = SearchEngine.createCSearchScope(elements, true);

		searchEngine.search(CUIPlugin.getWorkspace(), pattern, scope, resultCollector, false);
		elementsFound.addAll(resultCollector.getSearchResults());
	}
	
	private List getClassElementsInProject(){
		return elementsOfTypeClassInProject;
	}
	
	private List findClassElementsInProject(){		
		if(eSelection == null){
			return new LinkedList();			
		}

		if(	elementsOfTypeClassInProject != null ){
			return elementsOfTypeClassInProject;
		}

		elementsOfTypeClassInProject = new LinkedList();		
		IRunnableWithProgress runnable= new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				if (monitor == null) {
					monitor= new NullProgressMonitor();
				}				
				monitor.beginTask(NewWizardMessages.getString("NewClassWizardPage.operations.getProjectClasses"), 5); //$NON-NLS-1$
				try{
					ICProject cProject = eSelection.getCProject();
					searchForClasses(cProject, elementsOfTypeClassInProject, monitor, 1);
					//getChildrenOfTypeClass((IParent)cProject, elementsOfTypeClassInProject, monitor, 1);
					monitor.worked(5);
				} finally{
					monitor.done();
				}
			}
		};
		
		try {
			getWizard().getContainer().run(false, true, runnable);
		} catch (InvocationTargetException e) {				
		} catch (InterruptedException e) {
		} 
		finally {
		}
		return elementsOfTypeClassInProject;				
	}
	
	protected Object chooseBaseClass(){
		// find the available classes in this project
		List elementsFound = findClassElementsInProject();
		
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new CSearchResultLabelProvider());
		dialog.setTitle(NewWizardMessages.getString("BaseClassSelectionDialog.title")); //$NON-NLS-1$
		dialog.setMessage(NewWizardMessages.getString("BaseClassSelectionDialog.message")); //$NON-NLS-1$
		dialog.setElements(elementsFound.toArray());
		dialog.setFilter("*");
		
		if (dialog.open() == ElementListSelectionDialog.OK) {
			Object element= dialog.getFirstResult();
			return element;
		}		
		return null;
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
			return "public";
		else if(fAccessButtons.isSelected(1))
			return "protected";
		else if(fAccessButtons.isSelected(2))
			return "private";
		else return ""; 
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
		try{
			
			String lineDelimiter= null;	
			lineDelimiter= System.getProperty("line.separator", "\n"); 
			
			parentHeaderTU = createTranslationUnit(linkedResourceGroupForHeader);		
			parentBodyTU = createTranslationUnit(linkedResourceGroupForBody);		
			monitor.worked(1);
	
			if(parentHeaderTU != null){
				String header = constructHeaderFileContent(lineDelimiter);
				IWorkingCopy headerWC = parentHeaderTU.getSharedWorkingCopy(null, CUIPlugin.getDefault().getDocumentProvider().getBufferFactory());
				headerWC.getBuffer().append(header);
				synchronized(headerWC)	{
					headerWC.reconcile();	
					headerWC.commit(true, monitor);
				}
				//createdClass= (IStructure)headerWC.getElement(getNewClassName());				
				createdClass= headerWC.getElement(getNewClassName());				
			}
			if(parentBodyTU != null){
				String body = constructBodyFileContent(lineDelimiter);
				IWorkingCopy bodyWC = parentBodyTU.getSharedWorkingCopy(null, CUIPlugin.getDefault().getDocumentProvider().getBufferFactory());
				bodyWC.getBuffer().append(body);
				synchronized(bodyWC){
					bodyWC.reconcile();
					bodyWC.commit(true, monitor);
				}
			}
		
			return true;	
		}catch(CModelException e){
			MessageDialog.openError(getContainer().getShell(), WorkbenchMessages.getString("WizardNewFileCreationPage.internalErrorTitle"), WorkbenchMessages.format("WizardNewFileCreationPage.internalErrorMessage", new Object[] {e.getMessage()})); //$NON-NLS-2$ //$NON-NLS-1$
			return false;			
		}finally{
			monitor.done();
		}
					
	}
	
	protected ITranslationUnit createTranslationUnit(LinkToFileGroup linkedGroup){
		ITranslationUnit createdUnit = null;
		IFile createdFile = null;
		createdFile= createNewFile(linkedGroup);
		// turn the file into a translation unit
		if(createdFile != null){
			Object element= CoreModel.getDefault().create(createdFile);
			if (element instanceof ITranslationUnit)
				createdUnit = (ITranslationUnit) element;
		}
		
		return createdUnit;
	}
		
	protected IFile createNewFile(LinkToFileGroup linkedGroup) {
		final IPath newFilePath = getContainerFullPath(linkedGroup);
		final IFile newFileHandle = createFileHandle(newFilePath);

		if(newFileHandle.exists()){
			return newFileHandle;
		}

		// create the new file and cache it if successful
		final boolean linkedFile = linkedGroup.linkCreated();
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
					createFile(newFileHandle,initialContents, newFilePath, linkedFile, new SubProgressMonitor(monitor, 1000));
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
	protected IFile createFileHandle(IPath filePath) {
		IFile newFile = null;
		IWorkspaceRoot root= CUIPlugin.getWorkspace().getRoot();	
		newFile = root.getFileForLocation(filePath);
		if(newFile == null)
			newFile = root.getFile(filePath);
					
		return newFile;
	}

	protected void createFile(IFile fileHandle, InputStream contents, IPath targetPath, boolean linkedFile, IProgressMonitor monitor) throws CoreException {
		if (contents == null)
			contents = new ByteArrayInputStream(new byte[0]);
			
		try {
			fileHandle.create(contents, false, monitor);
		}
		catch (CoreException e) {
			// If the file already existed locally, just refresh to get contents
			int code = e.getStatus().getCode();
			if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
				fileHandle.refreshLocal(IResource.DEPTH_ZERO, null);
			else
				throw e;
		}

		if (monitor.isCanceled())
			throw new OperationCanceledException();
	}
	
	// -------------Helper methods for creating the class -------
	protected IPath getSelectionPath(){
		if(eSelection == null){
			IResource resourceSelection = getSelectionResourceElement(currentSelection);
			if(resourceSelection != null){
				return resourceSelection.getLocation().makeAbsolute();
			}
			else
				return null;
		}		
		// if it is a file, return the parent path
		if(eSelection instanceof ITranslationUnit)
			return (eSelection.getParent().getPath());
		// if it is a root, project, or folder, return its path	
		if(eSelection instanceof IOpenable){
			return (eSelection.getPath());				
		}else {
			// if it is an element in a file, return its openable parent's path
			ICElement current = eSelection.getParent();
			while (current != null){
				if ((current instanceof IOpenable) && !(current instanceof ITranslationUnit)){
					return current.getPath();
				}
				current = current.getParent();
			}
			return null;
		}									
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
			return (getSelectionPath());			
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
			IPath containerPath = getSelectionPath();
			containerPath.addTrailingSeparator();
			return ((containerPath.append(pathName)).makeAbsolute());						
		}
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
	protected String constructHeaderFileContent(String lineDelimiter){
		StringBuffer text = new StringBuffer();
		boolean extendingBase = false;
		String baseClassName = getBaseClassName();
		String baseClassFileName = "";
		if((baseClassName != null) && (baseClassName.length() > 0))
		{
			extendingBase = true;
			List classElements = findClassElementsInProject();
			BasicSearchMatch baseClass = (BasicSearchMatch)findInList(baseClassName, null, classElements);

//			if(baseClass != null){
//				IPath baseClassFileLocation = baseClass.getLocation();
//				IPath newFilePath = getContainerFullPath(linkedResourceGroupForHeader);
//				baseClassFileName = baseClassName + HEADER_EXT;
//			} else {
				baseClassFileName = baseClassName + HEADER_EXT;
//			}
		}
		
		if(isIncludeGuard()){
			text.append("#ifndef ");
			text.append(getNewClassName().toUpperCase());
			text.append("_H");
			text.append(lineDelimiter);
			text.append("#define ");
			text.append(getNewClassName().toUpperCase());
			text.append("_H");
			text.append(lineDelimiter);
			text.append(lineDelimiter);
		}
		
		if(extendingBase){
			text.append("#include \"");
			text.append(baseClassFileName);
			text.append('\"');
			text.append(lineDelimiter);			
			text.append(lineDelimiter);			
		}
		text.append("class ");
		text.append(getNewClassName());
		if(extendingBase){
			text.append(" : ");
			text.append(getAccess());
			text.append(" ");
			text.append(baseClassName);
		}
		text.append("{");
		text.append(lineDelimiter);			
		
		text.append("public:");
		text.append(lineDelimiter);			
		text.append(lineDelimiter);			
		
		// constructor
		text.append('\t');
		text.append(getNewClassName());
		text.append("()");
		if(isInline()){
			text.append(" {}");
			text.append(lineDelimiter);						
		}else {
			text.append(";");
			text.append(lineDelimiter);						
		}
		
		// destructor
		text.append('\t');		
		if(isVirtualDestructor()){
			text.append("virtual ");
		}
		text.append("~");
		text.append(getNewClassName());
		text.append("()");
		if(isInline()){
			text.append(" {}");
			text.append(lineDelimiter);						
		}else {
			text.append(";");
			text.append(lineDelimiter);						
		}
		text.append("};");
		text.append(lineDelimiter);		
		
		if(isIncludeGuard()){
			text.append(lineDelimiter);
			text.append("#endif // ");
			text.append(getNewClassName().toUpperCase());
			text.append("_H");
			text.append(lineDelimiter);		
		}
					
		return text.toString();	 		
	}
		
	protected String constructBodyFileContent(String lineDelimiter){
		StringBuffer text = new StringBuffer();
		text.append("#include \"");
		text.append(getCreatedClassHeaderFile().getElementName());
		text.append("\"");
		text.append(lineDelimiter);			
		text.append(lineDelimiter);			
		
		if(isInline())
			return text.toString();
			
		// constructor
		text.append(getNewClassName());
		text.append("::"); 
		text.append(getNewClassName());
		text.append("()");
		text.append(lineDelimiter);			
		text.append("{}");
		text.append(lineDelimiter);			
		
		// destructor
		text.append(getNewClassName());
		text.append("::~"); 
		text.append(getNewClassName());
		text.append("()"); 
		text.append(lineDelimiter);			
		text.append("{}"); 
		text.append(lineDelimiter);				
		return text.toString();
	}
	

	// ------ validation --------
	protected void doStatusUpdate() {
		// status of all used components
		IStatus[] status= new IStatus[] {
			fClassNameStatus,
			fBaseClassStatus,
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
		if (className.indexOf("::") != -1) {
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
		List elementsFound = findClassElementsInProject();
		if(foundInList(getNewClassName(), getContainerPath(linkedResourceGroupForHeader), elementsFound)){
			status.setWarning(NewWizardMessages.getString("NewClassWizardPage.error.ClassNameExists")); //$NON-NLS-1$
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
		List elementsFound = findClassElementsInProject();
		if(!foundInList(baseClassName, null, elementsFound)){
			status.setWarning(NewWizardMessages.getString("NewClassWizardPage.warning.BaseClassNotExists")); //$NON-NLS-1$
		}
		return status;		
	}
		
	private Object findInList(String name, IPath path, List elements){
		Iterator i = elements.iterator();
		while (i.hasNext()){
			BasicSearchMatch element = (BasicSearchMatch)i.next();
			if(path != null){
				// check both the name and the path
				if ((name.equals(element.getName())) && (path.makeAbsolute().equals(element.getLocation())))
					return element;
			} else {
				// we don't care about the path
				if (name.equals(element.getName()))
					return element;				
			}
		}
		return null;
	}
	
	private boolean foundInList(String name, IPath path, List elements){
		if(findInList(name, path, elements) != null)
			return true;
		else
			return false;
	}

}
