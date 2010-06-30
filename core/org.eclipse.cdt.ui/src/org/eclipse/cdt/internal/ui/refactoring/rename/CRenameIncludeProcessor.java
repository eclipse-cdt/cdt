/*******************************************************************************
 * Copyright (c) 2004,2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 ******************************************************************************/ 
package org.eclipse.cdt.internal.ui.refactoring.rename;

/**
 * Rename processor setting up input page for renaming include directives.
 */
public class CRenameIncludeProcessor extends CRenameProcessorDelegate {
    
    public CRenameIncludeProcessor(CRenameProcessor input, String kind) {
        super(input, kind);
        setAvailableOptions(CRefactory.OPTION_ASK_SCOPE | 
        		CRefactory.OPTION_EXHAUSTIVE_FILE_SEARCH |
                CRefactory.OPTION_IN_COMMENT | 
                CRefactory.OPTION_IN_MACRO_DEFINITION);
        setOptionsForcingPreview(-1);
        setOptionsEnablingScope(-1);
    }

    @Override
	protected int getAcceptedLocations(int selectedOptions) {
        return selectedOptions | CRefactory.OPTION_IN_INCLUDE_DIRECTIVE;
    }
}
