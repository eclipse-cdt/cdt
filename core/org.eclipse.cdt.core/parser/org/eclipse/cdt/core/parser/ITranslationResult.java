/*
 * Created on 25-Jun-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
 
package org.eclipse.cdt.core.parser;

import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * @author vmozgin
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

public interface ITranslationResult {
	
    public abstract IProblem[] getAllProblems();
    
    /**
     * Answer the initial translation unit corresponding to the present translation result
     */
    public abstract ITranslationUnit getTranslationUnit();
    
    /**
     * Answer the initial file name
     */
    public abstract char[] getFileName();
    
    /**
     * Answer the errors encountered during translation.
     */
    public abstract IProblem[] getErrors();
    
    /**
     * Answer the problems (errors and warnings) encountered during translation.
     *
     * It is intended to be used only once all problems have been detected,
     * and makes sure the problems slot as the exact size of the number of
     * problems.
     */
    public abstract IProblem[] getProblems();
    
    /**
     * Answer the tasks (TO-DO, ...) encountered during translation.
     *
     * It is intended to be used only once all problems have been detected,
     * and makes sure the problems slot as the exact size of the number of
     * problems.
     */
    public abstract IProblem[] getTasks();
    
    public abstract boolean hasErrors();
    public abstract boolean hasProblems();
    public abstract boolean hasSyntaxError();
    public abstract boolean hasTasks();
    public abstract boolean hasWarnings();
    
    public abstract void record(IProblem newProblem, IReferenceContext referenceContext);
    
    public abstract ITranslationResult tagAsAccepted();
}
