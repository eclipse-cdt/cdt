/*******************************************************************************
 * Copyright (c) 2004, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *    Markus Schorn - initial API and implementation 
 *    IBM Corporation - Bug 112366
 ******************************************************************************/ 
package org.eclipse.cdt.internal.ui.refactoring.rename;

import com.ibm.icu.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextEditChangeGroup;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.ui.refactoring.CTextFileChange;


/**
 * Abstract base for all different rename processors used by the top 
 * processor.
 */
public abstract class CRenameProcessorDelegate {
    private CRenameProcessor fTopProcessor;
    private ArrayList<CRefactoringMatch> fMatches= null;
    protected String fProcessorBaseName;
    private int fAvailableOptions=         
        CRefactory.OPTION_ASK_SCOPE | 
        CRefactory.OPTION_IN_CODE |
        CRefactory.OPTION_IN_COMMENT | 
        CRefactory.OPTION_IN_MACRO_DEFINITION |
        CRefactory.OPTION_IN_STRING_LITERAL;

    private int fOptionsForcingPreview=
        CRefactory.OPTION_IN_CODE |
        CRefactory.OPTION_IN_COMMENT | 
        CRefactory.OPTION_IN_MACRO_DEFINITION |
        CRefactory.OPTION_IN_PREPROCESSOR_DIRECTIVE |
        CRefactory.OPTION_IN_STRING_LITERAL;
    
    private int fOptionsEnablingScope= fOptionsForcingPreview;        


    protected CRenameProcessorDelegate(CRenameProcessor topProcessor, String name) {
        fTopProcessor= topProcessor;
        fProcessorBaseName= name;
    }
    
    final public CRefactoringArgument getArgument() {
        return fTopProcessor.getArgument();
    }
    final public String getReplacementText() {
        return fTopProcessor.getReplacementText();
    }
    final public int getSelectedScope() {
        return fTopProcessor.getScope();
    }
    final public int getSelectedOptions() {
        return fTopProcessor.getSelectedOptions();
    }
    final public String getSelectedWorkingSet() {
        return fTopProcessor.getWorkingSet();
    }
    final public CRefactory getManager() {
        return fTopProcessor.getManager();
    }
    final public ASTManager getAstManager() {
        return fTopProcessor.getAstManager();
    }
    final public String getProcessorName() {
        String identifier= getArgument().getName();
        if (identifier != null) {
            return MessageFormat.format(
                    Messages.getString("CRenameProcessorDelegate.wizard.title"),  //$NON-NLS-1$
                    new Object[] {fProcessorBaseName, identifier});
        }
        return null;
    }

    /**
     * The options presented by the page in the refactoring wizard.
     */
    public void setAvailableOptions(int options) {
        fAvailableOptions= options;
    }
    final int getAvailableOptions() {
        return fAvailableOptions;
    }

    /**
     * The options each of which forces the preview, when selected.
     */
    public void setOptionsForcingPreview(int options) {
        fOptionsForcingPreview= options;
    }
    
    final int getOptionsForcingPreview() {
    	return fOptionsForcingPreview;
    }

    /**
     * The options that need the scope definition. When one of them is 
     * selected, the scope options are enabled.
     */
    public void setOptionsEnablingScope(int options) {
        fOptionsEnablingScope= options;
    }
    
    final int getOptionsEnablingScope() {
        return fOptionsEnablingScope;
    }

    protected int getSearchScope() {
        return getSelectedScope();
    }

    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        return new RefactoringStatus();
    }

    public RefactoringStatus checkFinalConditions(IProgressMonitor monitor, CheckConditionsContext context) throws CoreException, OperationCanceledException {
        RefactoringStatus result= new RefactoringStatus();
        monitor.beginTask(Messages.getString("CRenameProcessorDelegate.task.checkFinalCondition"), 2); //$NON-NLS-1$
        IFile file= getArgument().getSourceFile();
        //assert file!=null;
    
        // perform text-search
        fMatches= new ArrayList<CRefactoringMatch>();
        TextSearchWrapper txtSearch= getManager().getTextSearch();
        IStatus stat= txtSearch.searchWord(getSearchScope(), file, getSelectedWorkingSet(), 
                getManager().getCCppPatterns(), getArgument().getName(), 
                new SubProgressMonitor(monitor, 1), fMatches);
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
        result.merge(RefactoringStatus.create(stat));
        if (result.hasFatalError()) {
            return result;
        }
        selectMatchesByLocation(fMatches);        
        analyzeTextMatches(fMatches, new SubProgressMonitor(monitor, 1), result);
        if (result.hasFatalError()) {
            return result;
        }
        
        HashSet<IFile> fileset= new HashSet<IFile>();
        int potentialMatchCount= 0;
        int commentCount=0;
        for (Iterator<CRefactoringMatch> iter = fMatches.iterator(); iter.hasNext();) {
            CRefactoringMatch tm = iter.next();
            if (tm.isInComment()) {
                commentCount++;
                fileset.add(tm.getFile());
            }
            else {
                switch(tm.getAstInformation()) {
                case CRefactoringMatch.AST_REFERENCE_OTHER:
                    iter.remove();
                    break;
                case CRefactoringMatch.POTENTIAL:
                    potentialMatchCount++;
                    fileset.add(tm.getFile());
                    break;
                default:
                    fileset.add(tm.getFile());
                break;
                }
            }
        }
        if (potentialMatchCount != 0) {
            String msg= null;
            if (potentialMatchCount == 1) {
                msg= Messages.getString("CRenameProcessorDelegate.warning.potentialMatch.singular"); //$NON-NLS-1$
            }
            else {
                msg= MessageFormat.format(
                    Messages.getString("CRenameProcessorDelegate.warning.potentialMatch.plural"), //$NON-NLS-1$
                    new Object[]{new Integer(potentialMatchCount)});
            }
            result.addWarning(msg);
        }
        if (commentCount != 0) {
            String msg= null;
            if (commentCount == 1) {
                msg= Messages.getString("CRenameProcessorDelegate.warning.commentMatch.singular"); //$NON-NLS-1$
            }
            else {
                msg= MessageFormat.format(
                    Messages.getString("CRenameProcessorDelegate.warning.commentMatch.plural"), //$NON-NLS-1$
                    new Object[]{new Integer(commentCount)});
            }
            result.addWarning(msg);
        }
        IFile[] files= fileset.toArray(new IFile[fileset.size()]);
        if (context != null) {
            ValidateEditChecker editChecker= 
                (ValidateEditChecker) context.getChecker(ValidateEditChecker.class);
            editChecker.addFiles(files);
        }
        monitor.done();
        return result;
    }

    protected void analyzeTextMatches(ArrayList<CRefactoringMatch> matches, IProgressMonitor monitor, RefactoringStatus status) {
        CRefactoringArgument argument= getArgument();
        IBinding[] renameBindings= getBindingsToBeRenamed(status);
        if (renameBindings != null && renameBindings.length > 0 && 
                argument.getArgumentKind() != CRefactory.ARGUMENT_UNKNOWN) {
            ASTManager mngr= getAstManager();
            mngr.setValidBindings(renameBindings);
            mngr.setRenameTo(getReplacementText());
            mngr.analyzeTextMatches(fTopProcessor.getIndex(), matches, monitor, status);
        }
    }

    private void selectMatchesByLocation(ArrayList<CRefactoringMatch> matches) {
        int acceptTextLocation= getAcceptedLocations(getSelectedOptions());
        for (Iterator<CRefactoringMatch> iter = matches.iterator(); iter.hasNext();) {
            CRefactoringMatch match = iter.next();
            int location= match.getLocation();
            if (location != 0 && ((location & acceptTextLocation) == 0)) {
                iter.remove();
            }
        }
    }

    protected int getAcceptedLocations(int selectedOptions) {
        return selectedOptions;
    }

    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        if (fMatches.size() == 0) {
            return null;
        }
        Collections.sort(fMatches, new Comparator<CRefactoringMatch>(){
            public int compare(CRefactoringMatch m1, CRefactoringMatch m2) {
                IFile f1= m1.getFile();
                IFile f2= m2.getFile();
                int cmp= f1.getName().compareTo(f2.getName());
                if (cmp != 0) return cmp;
    
                cmp= f1.getFullPath().toString().compareTo(f2.getFullPath().toString());   
                if (cmp != 0) return cmp;
                
                return m1.getOffset() - m2.getOffset();
            }});
        pm.beginTask(Messages.getString("CRenameProcessorDelegate.task.createChange"), fMatches.size()); //$NON-NLS-1$
        final String identifier= getArgument().getName();
        final String replacement= getReplacementText();
        CompositeChange overallChange= new CompositeChange(getProcessorName()); 
        IFile file= null;
        TextFileChange fileChange= null;
        MultiTextEdit fileEdit= null;
        for (CRefactoringMatch match : fMatches) {
            switch(match.getAstInformation()) {
            case CRefactoringMatch.AST_REFERENCE_OTHER:
                continue;
            case CRefactoringMatch.IN_COMMENT:
            case CRefactoringMatch.POTENTIAL:
                break;
            case CRefactoringMatch.AST_REFERENCE:
                break;
            }
            if (match.getAstInformation() != CRefactoringMatch.AST_REFERENCE_OTHER) {
                IFile mfile= match.getFile();
                if (file==null || !file.equals(mfile) || fileEdit == null || fileChange == null) {
                    file= mfile;
                    fileEdit= new MultiTextEdit();
                    fileChange = new CTextFileChange(file.getName(), file);
                    fileChange.setEdit(fileEdit);
                    overallChange.add(fileChange);
                }
                
                ReplaceEdit replaceEdit= new ReplaceEdit(match.getOffset(), 
                        identifier.length(), replacement);
                fileEdit.addChild(replaceEdit);
                TextEditGroup editGroup= new TextEditGroup(match.getLabel(), replaceEdit);
                TextEditChangeGroup changeGroup= new TextEditChangeGroup(fileChange, editGroup);
                fileChange.addTextEditChangeGroup(changeGroup);
            }
            pm.worked(1);
        }
        return overallChange;
    }

    /**
     * Returns the array of bindings that must be renamed
     */
    protected IBinding[] getBindingsToBeRenamed(RefactoringStatus status) {
        return new IBinding[] {getArgument().getBinding()};
    }
}
