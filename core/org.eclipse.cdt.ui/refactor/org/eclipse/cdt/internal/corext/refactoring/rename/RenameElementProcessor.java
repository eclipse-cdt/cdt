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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IEnumeration;
import org.eclipse.cdt.core.model.IEnumerator;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceManipulation;
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
	private final String QUALIFIER = "::"; //$NON-NLS-1$
	private final String TELTA = "~"; //$NON-NLS-1$
	
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
			return "";	 //$NON-NLS-1$
		String name = fCElement.getElementName();
		if (name.indexOf(QUALIFIER) != -1){
			return (name.substring(name.lastIndexOf(QUALIFIER) + 2, name.length()));
		}
		return name; 
	}

	private int getCurrentElementNameLength() {
		if(fCElement == null)
			return 0;	
		String name = fCElement.getElementName();
		if (name.indexOf(QUALIFIER) != -1){
			String unQualifiedName =name.substring(name.lastIndexOf(QUALIFIER) + 2, name.length()); 
			return (unQualifiedName.length());
		}
		return name.length(); 
	}

	private int getCurrentElementNameStartPos() {
		if(fCElement == null)
			return 0;	
		String name = fCElement.getElementName();
		if (name.indexOf(QUALIFIER) != -1){
			return (((CElement)fCElement).getIdStartPos() + name.lastIndexOf(QUALIFIER) + 2);
		}
		return ((CElement)fCElement).getIdStartPos(); 
	}
	
	private String getElementQualifiedName(ICElement element) throws CModelException{
		if(!eligibleForRefactoring(element)){
			return "";
		} else {
			StringBuffer name = new StringBuffer();
			if(element instanceof IFunctionDeclaration){
				IFunctionDeclaration function = (IFunctionDeclaration)element;
				if((element instanceof IMethodDeclaration) && ( ((IMethodDeclaration)element).isFriend() )){
					// go up until you hit a namespace or a translation unit.
					ICElement parent = (ICElement) element.getParent();
					while (!(parent instanceof INamespace) && (!(parent instanceof ITranslationUnit) )){
						parent = parent.getParent();
					}
					name.append(getElementQualifiedName(parent));										
				}else {
				// add the whole signature
				name.append(getElementQualifiedName(element.getParent()));
				}
				name.append("::");
				name.append(function.getSignature());
			} else {
				if (element instanceof IEnumerator) {
					IEnumeration enum = (IEnumeration) element.getParent();
					name.append(getElementQualifiedName(enum.getParent()));					
				}else {
					name.append(getElementQualifiedName(element.getParent()));
				}
				name.append("::");
				name.append(element.getElementName());				
			}
			return name.toString();
		}
	}
	public RefactoringStatus checkNewElementName(String newName) throws CModelException{
		if (!eligibleForRefactoring(fCElement)) { 
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.getString("RenameTypeRefactoring.wrong_element")); //$NON-NLS-1$
		}

		Assert.isNotNull(newName, "new name"); //$NON-NLS-1$
		
		RefactoringStatus result= null;
		if (fCElement instanceof IStructure){
			result= Checks.checkClassName(newName);	
		}
		else if ((fCElement instanceof IMethodDeclaration) || (fCElement instanceof IFunctionDeclaration)){
			result= Checks.checkMethodName(newName);
		}
		else if (fCElement instanceof IField){
			result= Checks.checkFieldName(newName);
		} 
		else {
			result = Checks.checkIdentifier(newName);
		}
		
		if (!(fCElement instanceof IFunctionDeclaration)){
			if(checkSiblingsCollision(true).hasError()){
				String msg= RefactoringCoreMessages.getFormattedString("RenameTypeRefactoring.member_type_exists", //$NON-NLS-1$
						new String[]{fNewElementName, fCElement.getParent().getElementName()});
				result.addFatalError(msg);		
			}
		}
		
		if( fCElement instanceof IMethodDeclaration){
			IMethodDeclaration method = (IMethodDeclaration)fCElement;
			if (method.isVirtual()){
				String msg= RefactoringCoreMessages.getFormattedString("RenameTypeRefactoring.virtual_method", //$NON-NLS-1$
						new String[]{fNewElementName, fCElement.getParent().getElementName()});
				result.addWarning(msg);	
			}
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
		//RefactoringStatus result= null;
		if (!eligibleForRefactoring(fCElement)) { 
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.getString("RenameTypeRefactoring.wrong_element")); //$NON-NLS-1$
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

			result.merge(Checks.checkIfTuBroken(fCElement));
			if (result.hasFatalError())
				return result;
			pm.worked(1);
		
			result.merge(checkEnclosingElements());
			pm.worked(1);

			result.merge(checkEnclosedElements());
			pm.worked(1);

			result.merge(checkSiblingsCollision(false));
			pm.worked(1);
			
			if (result.hasFatalError())
				return result;
						
			fReferences= null;
			pm.setTaskName(RefactoringCoreMessages.getString("RenameTypeRefactoring.searching"));	 //$NON-NLS-1$
			fReferences= getReferences(getElementQualifiedName(fCElement), new SubProgressMonitor(pm, 35), fUpdateReferences);
			pm.worked(6);
			
			result.merge(analyzeAffectedTranslationUnits());

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
			
			addReferenceUpdates(manager, new SubProgressMonitor(pm, 3));
			
			pm.worked(1);
			
			// now both declarations and references are searched for in references
			//addTypeDeclarationUpdate(manager);
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
			
			if(tu == null)
				return;
			ITranslationUnit wc = tu;
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
	
	private SearchResultGroup[] getReferences(String searchPrefix, IProgressMonitor pm, boolean updateReferences) throws CoreException {
		return RefactoringSearchEngine.search(pm, createRefactoringScope(), createSearchPattern(searchPrefix, updateReferences));
	}
	
	private ICSearchScope createRefactoringScope() throws CoreException {
		ICElement[] projectScopeElement = new ICElement[1];
		projectScopeElement[0] = fCElement.getCProject();
		ICSearchScope scope = SearchEngine.createCSearchScope(projectScopeElement, true);
		return scope;	
	}
	
	private OrPattern createSearchPattern(String searchPrefix, boolean updateReferences) throws CoreException {
		OrPattern orPattern = new OrPattern();
		if(fCElement instanceof IStructure){
			if(updateReferences){
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.TYPE,	ICSearchConstants.ALL_OCCURRENCES, false ));				
			}else {
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
					ICSearchConstants.TYPE,	ICSearchConstants.DECLARATIONS, false ));
			}
			IStructure structure = (IStructure) fCElement;
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix + QUALIFIER + structure.getElementName(),
					ICSearchConstants.METHOD, ICSearchConstants.ALL_OCCURRENCES, false ));
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix + QUALIFIER + TELTA + structure.getElementName(),
					ICSearchConstants.METHOD, ICSearchConstants.ALL_OCCURRENCES, false ));				
		}
		else if(fCElement instanceof IMethod){
			if(updateReferences){
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.METHOD, ICSearchConstants.ALL_OCCURRENCES, false ));				
			}else {
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.METHOD, ICSearchConstants.DEFINITIONS, false ));

				// The inline declaration is the same as the definition
			// we don't need to  search for the definition if it is inline
/*			ICElement parent = fCElement.getParent();
			if( (!(((IMethod)fCElement).isInline())) && (!(parent instanceof IStructure )) ) {
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.METHOD, ICSearchConstants.DECLARATIONS, false ));
			}
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
					ICSearchConstants.METHOD, ICSearchConstants.REFERENCES, false ));
*/			}
			
		} 		
		else if(fCElement instanceof IMethodDeclaration){
			if(updateReferences){
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.METHOD, ICSearchConstants.ALL_OCCURRENCES, false ));
			}else {
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.METHOD, ICSearchConstants.DECLARATIONS, false ));
			}
		} 
		else if(fCElement instanceof IFunction){
			if(updateReferences){
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.FUNCTION, ICSearchConstants.ALL_OCCURRENCES, false ));
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.METHOD, ICSearchConstants.ALL_OCCURRENCES, false ));				
			}else {
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.FUNCTION, ICSearchConstants.DEFINITIONS, false ));
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.METHOD, ICSearchConstants.DEFINITIONS, false ));								
			}
		} 
		else if(fCElement instanceof IFunctionDeclaration){
			if(updateReferences){
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.FUNCTION, ICSearchConstants.ALL_OCCURRENCES, false ));				
			}else {
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.FUNCTION, ICSearchConstants.DECLARATIONS, false ));
			}
		} 
		else if(fCElement instanceof IEnumeration){
			if(updateReferences){
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.ENUM, ICSearchConstants.ALL_OCCURRENCES, false ));
			}else {
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.ENUM, ICSearchConstants.DECLARATIONS, false ));				
			}
		} 		
		else if(fCElement instanceof IEnumerator){
			if(updateReferences){
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.ENUMTOR, ICSearchConstants.ALL_OCCURRENCES, false ));
			}else {
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.ENUMTOR, ICSearchConstants.DECLARATIONS, false ));				
			}
		} 		
		else if(fCElement instanceof IField){
			if(updateReferences){
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.FIELD, ICSearchConstants.ALL_OCCURRENCES, false ));
			}else {
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.FIELD, ICSearchConstants.DECLARATIONS, false ));				
			}
		} 
		else if(fCElement instanceof IVariable){
			if(updateReferences){
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.VAR, ICSearchConstants.ALL_OCCURRENCES, false ));
			}else {
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.VAR, ICSearchConstants.DECLARATIONS, false ));				
			}
		} 
		else if(fCElement instanceof INamespace){
			if(updateReferences){
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.NAMESPACE, ICSearchConstants.ALL_OCCURRENCES, false ));
			}else {
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix,	
						ICSearchConstants.NAMESPACE, ICSearchConstants.DECLARATIONS, false ));				
			}
		} 
		else {
			if(updateReferences){
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix, 
						ICSearchConstants.UNKNOWN_SEARCH_FOR, ICSearchConstants.ALL_OCCURRENCES, false ));
			}else {
				orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix, 
						ICSearchConstants.UNKNOWN_SEARCH_FOR, ICSearchConstants.DECLARATIONS, false ));				
			}
		} 
		return orPattern;
	}

	private RefactoringStatus checkEnclosedElements() throws CoreException {
		ICElement enclosedElement= findEnclosedElements(fCElement, fNewElementName);
		if (enclosedElement == null)
			return null;
		String msg= RefactoringCoreMessages.getFormattedString("RenameTypeRefactoring.encloses",  //$NON-NLS-1$
																		new String[]{fCElement.getElementName(), fNewElementName});
		return RefactoringStatus.createErrorStatus(msg);
	}

	private RefactoringStatus checkEnclosingElements() throws CoreException {
		ICElement enclosingElement= findEnclosingElements(fCElement, fNewElementName);
		if (enclosingElement == null)
			return null;
			
		String msg= RefactoringCoreMessages.getFormattedString("RenameTypeRefactoring.enclosed",//$NON-NLS-1$
								new String[]{fCElement.getElementName(), fNewElementName});
		return RefactoringStatus.createErrorStatus(msg);
	}
	
	private static ICElement findEnclosedElements(ICElement element, String newName) throws CoreException {
		if(element instanceof IParent){
			ICElement[] enclosedTypes= ((IParent)element).getChildren();
			for (int i= 0; i < enclosedTypes.length; i++){
				if (newName.equals(enclosedTypes[i].getElementName()) || findEnclosedElements(enclosedTypes[i], newName) != null)
					return enclosedTypes[i];
			}
		}
		return null;
	}
		
	private static ICElement findEnclosingElements(ICElement element, String newName) {
		ICElement enclosing= element.getParent();
		while ((enclosing != null) && (!(enclosing instanceof ITranslationUnit))){
			if (newName.equals(enclosing.getElementName()))
				return enclosing;
			else 
				enclosing= enclosing.getParent();	
		}
		return null;
	}
	
	private boolean isTopLevelStructure(ICElement element){
		if(element instanceof IStructure){
			ICElement parent = element.getParent();
			while (!(parent instanceof ITranslationUnit)){
				if(parent instanceof IStructure)
					return false;
				parent = parent.getParent();
			}
			return true;
		}
		return false;
	}
	
	private ICElement[] getSiblings(ICElement element, boolean localSiblings) throws CModelException{
		// only for top level structures
		if ((localSiblings) || (!isTopLevelStructure(element))){
			ICElement[] siblings= ((IParent)fCElement.getParent()).getChildren();
			return siblings;
		}
		else {
			Set siblingsSet = new HashSet();
			
			ICElement parent = element.getParent();
			int level = 1;
			boolean folderIsFound = false;
			while (!folderIsFound) {
				if (parent instanceof ICContainer){
					folderIsFound = true;
					break;
				}						
				parent = parent.getParent();
				level++;
			}
			// now we are at the first folder or project container
			// get siblings at level = level
			Set parentsSet = new HashSet();
			Set childrenSet = new HashSet();
			ICElement[] pr =((IParent)parent).getChildren();
			// add all translation unit children but not subfolders
			for (int i =0; i < pr.length; i++){
				if(!(pr[i] instanceof ICContainer))
					parentsSet.add(pr[i]);
			}
			// compare to elements in added translation units. 
			int currentLevel = 1;
			while (currentLevel < level) {
				Iterator itr = parentsSet.iterator();
				while (itr.hasNext()){
					Object o = itr.next();
					if(o instanceof ISourceManipulation) {
						ICElement p = (ICElement)o;
						if(p instanceof IParent){
							ICElement[] ch = ((IParent)p).getChildren();
							for (int i = 0; i < ch.length; i++){
								childrenSet.add(ch[i]);
							}
						}
					}
				}
				currentLevel++;
				if (currentLevel < level){
					parentsSet.clear();
					parentsSet.addAll(childrenSet);
					childrenSet.clear();
				}
			}
			return (ICElement[])childrenSet.toArray(new ICElement[childrenSet.size()]);	
		}
	}
	
	private RefactoringStatus checkSiblingsCollision(boolean localSiblings) {		
		RefactoringStatus result= new RefactoringStatus();
		try {
			// get the siblings of the CElement and check if it has the same name
			ICElement[] siblings = getSiblings(fCElement, localSiblings);
			for (int i = 0; i <siblings.length; ++i ){
				ICElement sibling = siblings[i];
				if ((sibling.getElementName().equals(fNewElementName)) 
						&& (sibling.getElementType() == fCElement.getElementType())  ) {
					if(localSiblings){
						String msg= RefactoringCoreMessages.getFormattedString("RenameTypeRefactoring.member_type_exists", //$NON-NLS-1$
								new String[]{fNewElementName, fCElement.getParent().getElementName()});
						result.addFatalError(msg);
						
					}else {
						String msg= RefactoringCoreMessages.getFormattedString("RenameTypeRefactoring.global_member_type_exists", //$NON-NLS-1$
								new String[]{fNewElementName, fCElement.getParent().getElementName()});
						result.addFatalError(msg);
					}
				}
			}
		} catch (CModelException e) {
			result.addFatalError(e.getMessage());
		}
		return result;
	}

	private RefactoringStatus analyzeAffectedTranslationUnits() throws CoreException{
		RefactoringStatus result= new RefactoringStatus();
		fReferences= Checks.excludeTranslationUnits(fReferences, result);
		if (result.hasFatalError())
			return result;
		
		result.merge(Checks.checkCompileErrorsInAffectedFiles(fReferences));	
		return result;
	}
	
	private boolean isConstructorOrDestructor(IFunctionDeclaration function) throws CModelException{
		// check declarations
		if(function instanceof IMethodDeclaration){
			IMethodDeclaration method = (IMethodDeclaration)function;
			if((method.isConstructor()) || (method.isDestructor()))
				return true;
		}
		// check definitions
		String returnType = function.getReturnType(); 
		if(( returnType == null) || (returnType.length() == 0) )
			return true;
		
		if(getCurrentElementName().startsWith("~"))
			return true;
		
		return false;
	}
	
	private boolean eligibleForRefactoring(ICElement element) throws CModelException{
		if((element == null) 
			|| (!(element instanceof ISourceReference)) 
			|| (element instanceof ITranslationUnit) 
			|| (element instanceof IMacro)
			|| (element instanceof IInclude)){
			return false;
		} else // disabling renaming of constructors and destructors 
			if(element instanceof IFunctionDeclaration){
			IFunctionDeclaration function = (IFunctionDeclaration)element;
			if (isConstructorOrDestructor(function))
				return false;
			else
				return true;
		}
		else {
			return true;
		}
	}
}
