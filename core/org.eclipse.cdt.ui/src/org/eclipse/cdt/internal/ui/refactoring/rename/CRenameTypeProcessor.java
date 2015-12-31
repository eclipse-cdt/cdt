/*******************************************************************************
 * Copyright (c) 2005, 2012 Wind River Systems, Inc. and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Markus Schorn - initial API and implementation 
 ******************************************************************************/ 
package org.eclipse.cdt.internal.ui.refactoring.rename;

/**
 * Handles conflicting bindings for types.
 */
public class CRenameTypeProcessor extends CRenameGlobalProcessor {

    public CRenameTypeProcessor(CRenameProcessor processor, String kind) {
        super(processor, kind);
    }
}
