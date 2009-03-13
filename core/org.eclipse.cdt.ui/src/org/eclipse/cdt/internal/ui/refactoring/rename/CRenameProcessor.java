/*******************************************************************************
 * Copyright (c) 2004, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import com.ibm.icu.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;


/**
 * This is the processor used for the rename. It decides which of the delegates to
 * use and forwards further calls to the delegate.
 */
public class CRenameProcessor extends RenameProcessor {
    public static final String IDENTIFIER= "org.eclips.cdt.refactoring.RenameProcessor"; //$NON-NLS-1$

    private CRefactoringArgument fArgument;
    private CRenameProcessorDelegate fDelegate;
    private String fReplacementText;
    private String fWorkingSet;
    private int fScope;
    private int fSelectedOptions;
    private CRefactory fManager;
    private ASTManager fAstManager;
	private IIndex fIndex;
    
    public CRenameProcessor(CRefactory refactoringManager, CRefactoringArgument arg) {
        fManager= refactoringManager;
        fArgument= arg;
        fAstManager= new ASTManager(arg);
    }
    
    public CRefactoringArgument getArgument() {
        return fArgument;
    }

    // overrider
    @Override
	public Object[] getElements() {
        return new Object[] {fArgument.getBinding()};
    }

    // overrider
    @Override
	public String getProcessorName() {
        String result= null;
        if (fDelegate != null) { 
            result= fDelegate.getProcessorName();
        }
        if (result == null) {
            String identifier= getArgument().getName();
            if (identifier != null && identifier.length() > 0) {
                result= MessageFormat.format(Messages.getString("CRenameTopProcessor.wizard.title"),  //$NON-NLS-1$
                    new Object[] {identifier});
            }
        }
        if (result == null) {
            result= Messages.getString("CRenameTopProcessor.wizard.backup.title"); //$NON-NLS-1$
        }

        return result;
    }

    // overrider
    @Override
	public boolean isApplicable() throws CoreException {
        return true;
    }

    // overrider
    @Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
            throws CoreException, OperationCanceledException {
        String identifier= null;
        RefactoringStatus status= new RefactoringStatus();
        if (fArgument != null) {
            fAstManager.analyzeArgument(fIndex, pm, status);
            identifier= fArgument.getName();
        }
        if (identifier == null || identifier.length() < 1) {
            status.addFatalError(Messages.getString("CRenameTopProcessor.error.invalidTextSelection")); //$NON-NLS-1$
            return status;
        }
        IFile file= fArgument.getSourceFile();
        IPath path= null;
        if (file != null) {
            path= file.getLocation();
        }
        if (path == null) {
            return RefactoringStatus.createFatalErrorStatus(Messages.getString("CRenameTopProcessor.error.renameWithoutSourceFile")); //$NON-NLS-1$
        }
        
        fDelegate= createDelegate();
        if (fDelegate == null) {
            status.addFatalError(Messages.getString("CRenameTopProcessor.error.invalidName")); //$NON-NLS-1$
            return status;
        }            
        RefactoringStatus s1= fDelegate.checkInitialConditions(new NullProgressMonitor());
        status.merge(s1);
        return status;
    }

    private CRenameProcessorDelegate createDelegate() {
        switch (fArgument.getArgumentKind()) {
        	case CRefactory.ARGUMENT_LOCAL_VAR: 
                return new CRenameLocalProcessor(this, 
                        Messages.getString("CRenameTopProcessor.localVar"), //$NON-NLS-1$
                        fArgument.getScope());
        	case CRefactory.ARGUMENT_PARAMETER:
                return new CRenameLocalProcessor(this, 
                        Messages.getString("CRenameTopProcessor.parameter"), //$NON-NLS-1$
                        fArgument.getScope());
        	case CRefactory.ARGUMENT_FILE_LOCAL_VAR:
                return new CRenameLocalProcessor(this, 
                        Messages.getString("CRenameTopProcessor.filelocalVar"), //$NON-NLS-1$
                        null);
        	case CRefactory.ARGUMENT_GLOBAL_VAR:
                return new CRenameGlobalProcessor(this, Messages.getString("CRenameTopProcessor.globalVar")); //$NON-NLS-1$
            case CRefactory.ARGUMENT_ENUMERATOR:
                return new CRenameGlobalProcessor(this, Messages.getString("CRenameTopProcessor.enumerator")); //$NON-NLS-1$
        	case CRefactory.ARGUMENT_FIELD:
                return new CRenameGlobalProcessor(this, Messages.getString("CRenameTopProcessor.field")); //$NON-NLS-1$
        	case CRefactory.ARGUMENT_FILE_LOCAL_FUNCTION:
                return new CRenameLocalProcessor(this, 
                        Messages.getString("CRenameTopProcessor.filelocalFunction"), //$NON-NLS-1$
                        null);
        	case CRefactory.ARGUMENT_GLOBAL_FUNCTION:
                return new CRenameGlobalProcessor(this, Messages.getString("CRenameTopProcessor.globalFunction")); //$NON-NLS-1$
        	case CRefactory.ARGUMENT_VIRTUAL_METHOD:
                return new CRenameMethodProcessor(this, Messages.getString("CRenameTopProcessor.virtualMethod")); //$NON-NLS-1$
        	case CRefactory.ARGUMENT_NON_VIRTUAL_METHOD:
                return new CRenameMethodProcessor(this, Messages.getString("CRenameTopProcessor.method")); //$NON-NLS-1$
            case CRefactory.ARGUMENT_CLASS_TYPE:                
                return new CRenameClassProcessor(this, Messages.getString("CRenameTopProcessor.type")); //$NON-NLS-1$
            case CRefactory.ARGUMENT_NAMESPACE:
                return new CRenameTypeProcessor(this, Messages.getString("CRenameTopProcessor.namespace")); //$NON-NLS-1$
        	case CRefactory.ARGUMENT_TYPE:
                return new CRenameTypeProcessor(this, Messages.getString("CRenameTopProcessor.type")); //$NON-NLS-1$
        	case CRefactory.ARGUMENT_MACRO:
                return new CRenameMacroProcessor(this, Messages.getString("CRenameTopProcessor.macro")); //$NON-NLS-1$
        	case CRefactory.ARGUMENT_INCLUDE_DIRECTIVE:
                return new CRenameIncludeProcessor(this, Messages.getString("CRenameIncludeProcessor.includeDirective")); //$NON-NLS-1$
        	default:
                return null;
        }
    }

    // overrider
    @Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm,
            CheckConditionsContext context) throws CoreException,
            OperationCanceledException {
        return fDelegate.checkFinalConditions(pm, context);
    }

    // overrider
    @Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
            OperationCanceledException {
        return fDelegate.createChange(pm);
    }

    // overrider
    @Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status,
            SharableParticipants sharedParticipants) throws CoreException {
        RenameArguments arguments= new RenameArguments(getReplacementText(), 
                true);
        final String[] natures= {CCProjectNature.CC_NATURE_ID, CCProjectNature.C_NATURE_ID};
        List<RenameParticipant> result= new ArrayList<RenameParticipant>();
        IBinding binding= getArgument().getBinding();
        if (binding != null) {
            result.addAll(Arrays.asList(ParticipantManager.loadRenameParticipants(status, 
                    this,  binding, arguments, natures, sharedParticipants)));
        }
        return result.toArray(new RefactoringParticipant[result.size()]);
    }

    // options for the input page in the refactoring wizard
    public int getAvailableOptions() {
        if (fDelegate == null) {
            return 0;
        }
        return fDelegate.getAvailableOptions();
    }

    // options for the input page that trigger the preview
    public int getOptionsForcingPreview() {
        if (fDelegate == null) {
            return 0;
        }
        return fDelegate.getOptionsForcingPreview();
    }

    // options for the input page that trigger the preview
    public int getOptionsEnablingScope() {
        if (fDelegate == null) {
            return 0;
        }
        return fDelegate.getOptionsEnablingScope();
    }

    // overrider
    @Override
	public String getIdentifier() {
        return IDENTIFIER;
    }

    public int getScope() {
        return fScope;
    }
    public void setScope(int scope) {
        fScope = scope;
    }
    public int getSelectedOptions() {
        return fSelectedOptions;
    }
    public void setSelectedOptions(int selectedOptions) {
        fSelectedOptions = selectedOptions;
    }
    public String getWorkingSet() {
        return fWorkingSet;
    }
    public void setWorkingSet(String workingSet) {
        fWorkingSet = workingSet;
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
		if (fIndex == null) {
			ICProject[] projects= CoreModel.getDefault().getCModel().getCProjects();
			fIndex= CCorePlugin.getIndexManager().getIndex(projects);
		}
		fIndex.acquireReadLock();
	}
	
	public void unlockIndex() {
		if (fIndex != null) {
			fIndex.releaseReadLock();
		}
		fIndex= null;
	}

	public IIndex getIndex() {
		return fIndex;
	}
}
