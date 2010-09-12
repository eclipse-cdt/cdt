/*******************************************************************************
 * Copyright (c) 2004, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *    Markus Schorn - initial API and implementation
 *    Sergey Prigogin (Google) 
 ******************************************************************************/ 
package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

/**
 * Refactoring implementation using a refactoring processor.
 */
public class CRenameRefactoring extends ProcessorBasedRefactoring {
	public static final String ID = "org.eclipse.cdt.internal.ui.refactoring.rename.CRenameRefactoring"; //$NON-NLS-1$

    public CRenameRefactoring(CRenameProcessor processor) {
        super(processor);
    }
}
