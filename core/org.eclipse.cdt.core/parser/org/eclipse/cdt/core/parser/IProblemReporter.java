/*
 * Created on 25-Jun-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.core.parser;

/**
 * @author vmozgin
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface IProblemReporter {
    
    public ITranslationOptions getOptions();
    
    public abstract void task(
        String tag,
        String message,
        String priority,
        int start,
        int end,
        int line,
        ITranslationResult result);
}
