package org.eclipse.cdt.internal.ui.editor;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.cdt.internal.core.model.IWorkingCopy;
import org.eclipse.ui.IEditorInput;

/**
 * Interface for accessing working copies of <code>ITranslationUnit</code>
 * objects. The original Translation unit is only given indirectly by means of
 * an <code>IEditorInput</code>. 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see CUIPlugin#getWorkingCopyManager
 * 
 * This interface is similar to the JDT IWorkingCopyManager.
 */

public interface IWorkingCopyManager {

	IWorkingCopy getWorkingCopy(IEditorInput input);

}