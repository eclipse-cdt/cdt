/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.rename;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IEnumeration;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IVariable;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.OrPattern;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.model.CElement;
import org.eclipse.cdt.internal.corext.Assert;
import org.eclipse.cdt.internal.corext.refactoring.Checks;
import org.eclipse.cdt.internal.corext.refactoring.CompositeChange;
import org.eclipse.cdt.internal.corext.refactoring.IReferenceUpdating;
import org.eclipse.cdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.cdt.internal.corext.refactoring.RefactoringSearchEngine;
import org.eclipse.cdt.internal.corext.refactoring.RenameProcessor;
import org.eclipse.cdt.internal.corext.refactoring.ResourceUtil;
import org.eclipse.cdt.internal.corext.refactoring.SearchResultGroup;
import org.eclipse.cdt.internal.corext.refactoring.TextChangeManager;
import org.eclipse.cdt.internal.corext.refactoring.base.IChange;
import org.eclipse.cdt.internal.corext.refactoring.base.RefactoringStatus;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.text.edits.ReplaceEdit;

public class RenameElementProcessor extends RenameProcessor implements IReferenceUpdating{
	private ICElement fCElement = null;
	private SearchResultGroup[] fReferences;
	private TextChangeManager fChangeManager;
	private final String QUALIFIER = "::";
	
	private boolean fUpdateReferences;
	
	public ICElement getCElement() {
		return fCElement;
	}

	//---- IRefactoringProcessor ---------------------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.corext.refactoring.IRefactoringProcessor#initialize(java.lang.Object[])
	 */
	public void initialize(Object[] elements) throws CoreException {
		Assert.isTrue(elements != null && elements.length == 1);
		Object element= elements[0];
		if(element == null)
			return;
		if (!(element instanceof ISourceReference))
			return;
		fCElement= (ICElement)element;
		setNewElementName(fCElement.getElementName());
		fUpdateReferences= true; //default is yes
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.corext.refactoring.IRefactoringProcessor#isAvailable()
	 */
	public boolean isAvailable() throws CoreException {
		if (fCElement == null)
			return false;
		if (!(fCElement instanceof ISourceReference))
			return false;
		if (! Checks.isAvailable(fCElement))
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.corext.refactoring.IRefactoringProcessor#getProcessorName()
	 */
	public String getProcessorName() {
		return RefactoringCoreMessages.getFormattedString(
				"RenameTypeRefactoring.name",  //$NON-NLS-1$
				new String[]{fCElement.getElementName(), fNewElementName});
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.corext.refactoring.IRefactoringProcessor#getElements()
	 */
	public Object[] getElements() {
		return new Object[] {fCElement};
	}
	
	//---- IRenameProcessor ----------------------------------------------
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.corext.refactoring.IRenameProcessor#getCurrentElementName()
	 */
	public String getCurrentElementName() {
		if(fCElement == null)
			return "";	
		String name = fCElement.getElementName();
		if (name.indexOf(QUALIFIER) != -1){
			return (name.substring(name.lastIndexOf(QUALIFIER) + 2, name.length()));
		}
		return name; 
	}

	public int getCurrentElementNameLength() {
		if(fCElement == null)
			return 0;	
		String name = fCElement.getElementName();
		if (name.indexOf(QUALIFIER) != -1){
			String unQualifiedName =name.substring(name.lastIndexOf(QUALIFIER) + 2, name.length()); 
			return (unQualifiedName.length());
		}
		return name.length(); 
	}

	public int getCurrentElementNameStartPos() {
		if(fCElement == null)
			return 0;	
		String name = fCElement.getElementName();
		if (name.indexOf(QUALIFIER) != -1){
			return (((CElement)fCElement).getIdStartPos() + name.lastIndexOf(QUALIFIER) + 2);
		}
		return ((CElement)fCElement).getIdStartPos(); 
	}
	
	public RefactoringStatus checkNewElementName(String newName){
		if ((fCElement == null) || (!(fCElement instanceof ISourceReference)) || (fCElement instanceof ITranslationUnit)) { 
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.getString("RenameTypeRefactoring.wrong_element"));
		}

		Assert.isNotNull(newName, "new name"); //$NON-NLS-1$
		RefactoringStatus result= null;
		if (fCElement instanceof IStructure){
			result= Checks.checkClassName(newName);	
		}
		else if (fCElement instanceof IMethodDeclaration) {
			result= Checks.checkMethodName(newName);
		}
		else if (fCElement instanceof IField){
			result= Checks.checkFieldName(newName);
		} 
		else {
			result = Checks.checkIdentifier(newName);
		}
				
		if (Checks.isAlreadyNamed(fCElement, newName))
			result.addFatalError(RefactoringCoreMessages.getString("RenameTypeRefactoring.choose_another_name"));	 //$NON-NLS-1$
		return result;
	}
	//---- IReferenceUpdating --------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.corext.refactoring.IReferenceUpdating#canEnableUpdateReferences()
	 */
	public boolean canEnableUpdateReferences() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.corext.refactoring.IReferenceUpdating#getUpdateReferences()
	 */
	public boolean getUpdateReferences() {
		return fUpdateReferences;	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.corext.refactoring.IReferenceUpdating#setUpdateReferences(boolean)
	 */
	public void setUpdateReferences(boolean update){
		fUpdateReferences= update;
	}
	//------------------------------------------
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.corext.refactoring.ITextUpdating#canEnableTextUpdating()
	 */
	public boolean canEnableTextUpdating() {
		return false;
	}



	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.corext.refactoring.IRefactoringProcessor#checkActivation()
	 */
	public RefactoringStatus checkActivation() throws CoreException {
		RefactoringStatus result= null;
		if ((fCElement == null) || (!(fCElement instanceof ISourceReference)) || (fCElement instanceof ITranslationUnit)) { 
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.getString("RenameTypeRefactoring.wrong_element"));
		}		
		return Checks.checkIfTuBroken(fCElement);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.corext.refactoring.IRefactoringProcessor#checkInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RefactoringStatus checkInput(IProgressMonitor pm)
		throws CoreException {
		Assert.isNotNull(fCElement, "type"); //$NON-NLS-1$
		Assert.isNotNull(fNewElementName, "newName"); //$NON-NLS-1$
		RefactoringStatus result= new RefactoringStatus();
		try{
			pm.beginTask("", 20); //$NON-NLS-1$
			pm.setTaskName(RefactoringCoreMessages.getString("RenameTypeRefactoring.checking"));//$NON-NLS-1$
			result.merge(checkNewElementName(fNewElementName));
			
			if (result.hasFatalError())
				return result;
			pm.worked(5);
			
			fReferences= null;
			if (fUpdateReferences){
				pm.setTaskName(RefactoringCoreMessages.getString("RenameTypeRefactoring.searching"));	 //$NON-NLS-1$
				fReferences= getReferences(fCElement.getElementName(), new SubProgressMonitor(pm, 35));
			}
			pm.worked(10);

			pm.setTaskName(RefactoringCoreMessages.getString("RenameTypeRefactoring.checking")); //$NON-NLS-1$
			if (pm.isCanceled())
				throw new OperationCanceledException();
						
			if (result.hasFatalError())
				return result;			
			// more checks go here
			fChangeManager= createChangeManager(new SubProgressMonitor(pm, 35));
			pm.worked(5);
			
			return result;
		} finally {
			pm.done();
		}	
		}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.corext.refactoring.IRefactoringProcessor#createChange(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IChange createChange(IProgressMonitor pm) throws CoreException {
		pm.beginTask(RefactoringCoreMessages.getString("RenameTypeRefactoring.creating_change"), 4); //$NON-NLS-1$
		CompositeChange builder= new CompositeChange(
				RefactoringCoreMessages.getString("Change.javaChanges")); //$NON-NLS-1$
		builder.addAll(fChangeManager.getAllChanges());
		pm.worked(1);	
		return builder;	
	}

	private IFile[] getAllFilesToModify() throws CoreException {
		List result= new ArrayList();
		result.addAll(Arrays.asList(ResourceUtil.getFiles(fChangeManager.getAllTranslationUnits())));
		return (IFile[]) result.toArray(new IFile[result.size()]);
	}
	
	private TextChangeManager createChangeManager(IProgressMonitor pm) throws CoreException {
		try{
			pm.beginTask("", 7); //$NON-NLS-1$
			TextChangeManager manager= new TextChangeManager();
			
			if (fUpdateReferences)
				addReferenceUpdates(manager, new SubProgressMonitor(pm, 3));
			
			pm.worked(1);
			
			addTypeDeclarationUpdate(manager);
			pm.worked(1);
						
			return manager;
		} finally{
			pm.done();
		}	
	}
	
	private void addReferenceUpdates(TextChangeManager manager, IProgressMonitor pm) throws CoreException {
		pm.beginTask("", fReferences.length); //$NON-NLS-1$
		for (int i= 0; i < fReferences.length; i++){
			IResource res= fReferences[i].getResultGroupResource();
			if (res == null)
				continue;
			ITranslationUnit cu= (ITranslationUnit) CoreModel.getDefault().create(res);
			if (cu == null)
				continue;	
			
			ITranslationUnit tu = CModelUtil.toWorkingCopy(cu);
			
			if((tu == null) || (!( tu instanceof ITranslationUnit)))
				return;
			ITranslationUnit wc = (ITranslationUnit)tu;
			String name= RefactoringCoreMessages.getString("RenameTypeRefactoring.update_reference"); //$NON-NLS-1$
			BasicSearchMatch[] results= fReferences[i].getSearchResults();

			for (int j= 0; j < results.length; j++){
				BasicSearchMatch searchResult= results[j];
				int oldNameLength = getCurrentElementNameLength();
				int offset= searchResult.getEndOffset() - oldNameLength;
				manager.get(wc).addTextEdit(name, 
						new ReplaceEdit(offset, oldNameLength, fNewElementName));
			}
			pm.worked(1);
		}
	}
		
	private void addTypeDeclarationUpdate(TextChangeManager manager) throws CoreException {
		String name= RefactoringCoreMessages.getString("RenameTypeRefactoring.update"); //$NON-NLS-1$
		if(fCElement instanceof ISourceReference){
			ITranslationUnit cu= ((ISourceReference)fCElement).getTranslationUnit(); // WorkingCopyUtil.getWorkingCopyIfExists(fCElement.getTranslationUnit());
			manager.get(cu).addTextEdit(name, new ReplaceEdit(getCurrentElementNameStartPos(), getCurrentElementNameLength(), fNewElementName));
		}
	}
	
	private SearchResultGroup[] getReferences(String searchPrefix, IProgressMonitor pm) throws CoreException {
		return RefactoringSearchEngine.search(pm, createRefactoringScope(), createSearchPattern(searchPrefix));
	}
	
	private ICSearchScope createRefactoringScope() throws CoreException {
		return SearchEngine.createWorkspaceScope();
	}
	
	private OrPattern createSearchPattern(String searchPrefix) throws CoreException {
		OrPattern orPattern = new OrPattern();
		if(fCElement instanceof IStructure){
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
				ICSearchConstants.TYPE,	ICSearchConstants.REFERENCES, false ));
			IStructure structure = (IStructure) fCElement;
			if(structure.getElementType() == ICElement.C_CLASS){
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.METHOD, ICSearchConstants.ALL_OCCURRENCES, false ));
				orPattern.addPattern(SearchEngine.createSearchPattern( "~"+ searchPrefix,	
						ICSearchConstants.METHOD, ICSearchConstants.ALL_OCCURRENCES, false ));				
			}
		}
		else if(fCElement instanceof IMethod){
			// The inline declaration is the same as the definition
			// we don't need to  search for the declaration if it is inline
			ICElement parent = fCElement.getParent();
			if( (!(((IMethod)fCElement).isInline())) && (!(parent instanceof IStructure )) ) {
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.METHOD, ICSearchConstants.DECLARATIONS, false ));
			}
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
					ICSearchConstants.METHOD, ICSearchConstants.REFERENCES, false ));
		} 		
		else if(fCElement instanceof IMethodDeclaration){
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
					ICSearchConstants.METHOD, ICSearchConstants.DEFINITIONS, false ));
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
					ICSearchConstants.METHOD, ICSearchConstants.REFERENCES, false ));
		} 
		else if(fCElement instanceof IFunction){
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
					ICSearchConstants.FUNCTION, ICSearchConstants.DECLARATIONS, false ));
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
					ICSearchConstants.FUNCTION, ICSearchConstants.REFERENCES, false ));
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
					ICSearchConstants.METHOD, ICSearchConstants.DECLARATIONS, false ));
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
					ICSearchConstants.METHOD, ICSearchConstants.REFERENCES, false ));
		} 
		else if(fCElement instanceof IFunctionDeclaration){
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
					ICSearchConstants.FUNCTION, ICSearchConstants.DEFINITIONS, false ));
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
					ICSearchConstants.FUNCTION, ICSearchConstants.REFERENCES, false ));
		} 
		else if(fCElement instanceof IField){
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
					ICSearchConstants.FIELD, ICSearchConstants.REFERENCES, false ));
		} 
		else if(fCElement instanceof IVariable){
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
					ICSearchConstants.VAR, ICSearchConstants.REFERENCES, false ));
		} 
		else if(fCElement instanceof INamespace){
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
					ICSearchConstants.NAMESPACE, ICSearchConstants.REFERENCES, false ));
		} 
		else if(fCElement instanceof IEnumeration){
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
					ICSearchConstants.ENUM, ICSearchConstants.REFERENCES, false ));
		} 		
		else {
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix, 
					ICSearchConstants.UNKNOWN_SEARCH_FOR, ICSearchConstants.REFERENCES,	false ));
		} 
		return orPattern;
	}
	
}
