/*
 * Created on 25-Jun-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
 
package org.eclipse.cdt.core.parser;

import java.util.Map;

/**
 * @author vmozgin
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface ITranslationOptions {
	
    /**
     * Option IDs
     */
    public static final String OPTION_TaskTags 
    		= "org.eclipse.cdt.core.translation.taskTags"; //$NON-NLS-1$
    public static final String OPTION_TaskPriorities 
    		= "org.eclipse.cdt.core.translation.taskPriorities"; //$NON-NLS-1$
    
    /** 
     * Initializing the compiler options with external settings
     */
    public abstract void initialize(Map settings);

    public abstract char[][] getTaskTags();
    public abstract char[][] getTaskPriorities();
}
