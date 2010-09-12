/*******************************************************************************
 * Copyright (c) 2005, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/ 
package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringSaveHelper;


/**
 * Rename processor that sets up the input page for renaming a global entity.
 */
public class CRenameGlobalProcessor extends CRenameProcessorDelegate {

    public CRenameGlobalProcessor(CRenameProcessor processor, String name) {
        super(processor, name);
        setAvailableOptions(CRefactory.OPTION_ASK_SCOPE |
        		CRefactory.OPTION_EXHAUSTIVE_FILE_SEARCH |
                CRefactory.OPTION_IN_CODE |
                CRefactory.OPTION_IN_COMMENT | 
                CRefactory.OPTION_IN_MACRO_DEFINITION);
    }

	@Override
	public int getSaveMode() {
		return RefactoringSaveHelper.SAVE_REFACTORING;
	}
}
