/*******************************************************************************
 * Copyright (c) 2004, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

/**
 * This is the processor used for the rename. It decides which of the delegates to
 * use and forwards further calls to the delegate.
 */
public class CRenameProcessor extends RenameProcessor {
	public static final String IDENTIFIER = "org.eclips.cdt.refactoring.RenameProcessor"; //$NON-NLS-1$

	private static final String[] AFFECTED_PROJECT_NATURES = { CCProjectNature.CC_NATURE_ID,
			CProjectNature.C_NATURE_ID };

	private final CRefactoringArgument fArgument;
	private CRenameProcessorDelegate fDelegate;
	private String fReplacementText;
	private String fWorkingSetName;
	private int fExhaustiveSearchScope;
	private int fSelectedOptions;
	private final CRefactory fManager;
	private final ASTManager fAstManager;
	private IIndex fIndex;
	private int fIndexLockCount;
	private RefactoringStatus fInitialConditionsStatus;

	public CRenameProcessor(CRefactory refactoringManager, CRefactoringArgument arg) {
		fManager = refactoringManager;
		fArgument = arg;
		fAstManager = new ASTManager(arg);
	}

	public CRefactoringArgument getArgument() {
		return fArgument;
	}

	@Override
	public Object[] getElements() {
		return new Object[] { fArgument.getBinding() };
	}

	@Override
	public String getProcessorName() {
		String result = null;
		if (fDelegate != null) {
			result = fDelegate.getProcessorName();
		}
		if (result == null) {
			String identifier = getArgument().getName();
			if (identifier != null && identifier.length() > 0) {
				result = NLS.bind(RenameMessages.CRenameTopProcessor_wizard_title, identifier);
			}
		}
		if (result == null) {
			result = RenameMessages.CRenameTopProcessor_wizard_backup_title;
		}

		return result;
	}

	@Override
	public boolean isApplicable() throws CoreException {
		return true;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		if (fInitialConditionsStatus != null) {
			return fInitialConditionsStatus; // Already checked.
		}
		String identifier = null;
		fInitialConditionsStatus = new RefactoringStatus();
		if (fArgument != null) {
			fAstManager.analyzeArgument(fIndex, pm, fInitialConditionsStatus);
			identifier = fArgument.getName();
		}
		if (identifier == null || identifier.length() == 0) {
			fInitialConditionsStatus.addFatalError(RenameMessages.CRenameTopProcessor_error_invalidTextSelection);
			return fInitialConditionsStatus;
		}
		IFile file = fArgument.getSourceFile();
		IPath path = null;
		if (file != null) {
			path = file.getLocation();
		}
		if (path == null) {
			return RefactoringStatus
					.createFatalErrorStatus(RenameMessages.CRenameTopProcessor_error_renameWithoutSourceFile);
		}

		updateBinding();

		fDelegate = createDelegate();
		if (fDelegate == null) {
			fInitialConditionsStatus.addFatalError(RenameMessages.CRenameTopProcessor_error_invalidName);
			return fInitialConditionsStatus;
		}
		RefactoringStatus status = fDelegate.checkInitialConditions(new NullProgressMonitor());
		fInitialConditionsStatus.merge(status);
		return fInitialConditionsStatus;
	}

	/**
	 * Change the binding for the renaming of constructors and destructor to the class.
	 */
	private void updateBinding() {
		IBinding binding = fArgument.getBinding();
		if (binding instanceof ICPPConstructor
				|| (binding instanceof ICPPMethod && ((ICPPMethod) binding).isDestructor())) {
			// Switch binding to class level when constructor or destructor selected
			IBinding newBinding = ((ICPPMember) binding).getClassOwner();
			IScope scope = fArgument.getScope();
			try {
				scope = newBinding.getScope();
			} catch (DOMException e) {
				CUIPlugin.log(e);
			}
			fArgument.setBinding(fArgument.getTranslationUnit(), newBinding, scope);

			if (fArgument.getName().startsWith("~")) { //$NON-NLS-1$
				fArgument.setName(newBinding.getName());
			}
		}
	}

	private CRenameProcessorDelegate createDelegate() {
		switch (fArgument.getArgumentKind()) {
		case CRefactory.ARGUMENT_LOCAL_VAR:
			return new CRenameLocalProcessor(this, RenameMessages.CRenameTopProcessor_localVar, fArgument.getScope());
		case CRefactory.ARGUMENT_PARAMETER:
			return new CRenameLocalProcessor(this, RenameMessages.CRenameTopProcessor_parameter, fArgument.getScope());
		case CRefactory.ARGUMENT_FILE_LOCAL_VAR:
			return new CRenameLocalProcessor(this, RenameMessages.CRenameTopProcessor_filelocalVar, null);
		case CRefactory.ARGUMENT_GLOBAL_VAR:
			return new CRenameGlobalProcessor(this, RenameMessages.CRenameTopProcessor_globalVar);
		case CRefactory.ARGUMENT_ENUMERATOR:
			return new CRenameGlobalProcessor(this, RenameMessages.CRenameTopProcessor_enumerator);
		case CRefactory.ARGUMENT_FIELD:
			return new CRenameGlobalProcessor(this, RenameMessages.CRenameTopProcessor_field);
		case CRefactory.ARGUMENT_FILE_LOCAL_FUNCTION:
			return new CRenameLocalProcessor(this, RenameMessages.CRenameTopProcessor_filelocalFunction, null);
		case CRefactory.ARGUMENT_GLOBAL_FUNCTION:
			return new CRenameGlobalProcessor(this, RenameMessages.CRenameTopProcessor_globalFunction);
		case CRefactory.ARGUMENT_VIRTUAL_METHOD:
			return new CRenameMethodProcessor(this, RenameMessages.CRenameTopProcessor_virtualMethod, true);
		case CRefactory.ARGUMENT_NON_VIRTUAL_METHOD:
			return new CRenameMethodProcessor(this, RenameMessages.CRenameTopProcessor_method, false);
		case CRefactory.ARGUMENT_CLASS_TYPE:
			return new CRenameClassProcessor(this, RenameMessages.CRenameTopProcessor_type);
		case CRefactory.ARGUMENT_NAMESPACE:
			return new CRenameTypeProcessor(this, RenameMessages.CRenameTopProcessor_namespace);
		case CRefactory.ARGUMENT_TYPE:
			return new CRenameTypeProcessor(this, RenameMessages.CRenameTopProcessor_type);
		case CRefactory.ARGUMENT_MACRO:
			return new CRenameMacroProcessor(this, RenameMessages.CRenameTopProcessor_macro);
		case CRefactory.ARGUMENT_INCLUDE_DIRECTIVE:
			return new CRenameIncludeProcessor(this, RenameMessages.CRenameIncludeProcessor_includeDirective);
		default:
			return null;
		}
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws CoreException, OperationCanceledException {
		return fDelegate.checkFinalConditions(pm, context);
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return fDelegate.createChange(pm);
	}

	@Override
	public final RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants shared)
			throws CoreException {
		return fDelegate.getRenameModifications().loadParticipants(status, this, AFFECTED_PROJECT_NATURES, shared);
	}

	/**
	 * Options for the input page in the refactoring wizard.
	 */
	public int getAvailableOptions() {
		if (fDelegate == null) {
			return 0;
		}
		return fDelegate.getAvailableOptions();
	}

	/**
	 * Options for the input page that trigger the preview.
	 */
	public int getOptionsForcingPreview() {
		if (fDelegate == null) {
			return 0;
		}
		return fDelegate.getOptionsForcingPreview();
	}

	/**
	 * The options that may need exhaustive file search since index lookup is not guaranteed to
	 * return all files participating in refactoring. When one of these options is selected,
	 * the exhaustive file search is enabled.
	 */
	public int getOptionsEnablingExhaustiveSearch() {
		if (fDelegate == null) {
			return 0;
		}
		return fDelegate.getOptionsEnablingExhaustiveSearch();
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	public int getExhaustiveSearchScope() {
		return (fSelectedOptions & CRefactory.OPTION_EXHAUSTIVE_FILE_SEARCH) != 0 ? fExhaustiveSearchScope
				: TextSearchWrapper.SCOPE_FILE;
	}

	public void setExhaustiveSearchScope(int scope) {
		fExhaustiveSearchScope = scope;
	}

	public int getSelectedOptions() {
		return fSelectedOptions;
	}

	public void setSelectedOptions(int selectedOptions) {
		fSelectedOptions = selectedOptions;
	}

	public boolean isPreviewRequired() {
		return (fSelectedOptions & getOptionsForcingPreview()) != 0;
	}

	public String getWorkingSetName() {
		return fWorkingSetName;
	}

	/**
	 * Sets the name of the working set. If the name of the working set is invalid,
	 * it's set to an empty string.
	 */
	public void setWorkingSetName(String workingSet) {
		fWorkingSetName = checkWorkingSet(workingSet);
	}

	public String getReplacementText() {
		return fReplacementText;
	}

	public void setReplacementText(String replacementText) {
		fReplacementText = replacementText;
	}

	public CRefactory getManager() {
		return fManager;
	}

	public ASTManager getAstManager() {
		return fAstManager;
	}

	public void lockIndex() throws CoreException, InterruptedException {
		if (fIndexLockCount == 0) {
			if (fIndex == null) {
				ICProject[] projects = CoreModel.getDefault().getCModel().getCProjects();
				fIndex = CCorePlugin.getIndexManager().getIndex(projects, IIndexManager.ADD_EXTENSION_FRAGMENTS_EDITOR);
			}
			fIndex.acquireReadLock();
		}
		fIndexLockCount++;
	}

	public void unlockIndex() {
		if (--fIndexLockCount <= 0) {
			if (fAstManager != null) {
				fAstManager.dispose();
			}
			if (fIndex != null) {
				fIndex.releaseReadLock();
			}
			fIndex = null;
		}
	}

	public IIndex getIndex() {
		return fIndex;
	}

	/**
	 * @return a save mode from {@link org.eclipse.cdt.internal.ui.refactoring.RefactoringSaveHelper}
	 */
	public int getSaveMode() {
		return fDelegate.getSaveMode();
	}

	private String checkWorkingSet(String workingSet) {
		if (workingSet != null && workingSet.length() > 0) {
			IWorkingSetManager wsManager = PlatformUI.getWorkbench().getWorkingSetManager();
			if (wsManager.getWorkingSet(workingSet) != null) {
				return workingSet;
			}
		}
		return ""; //$NON-NLS-1$
	}
}
