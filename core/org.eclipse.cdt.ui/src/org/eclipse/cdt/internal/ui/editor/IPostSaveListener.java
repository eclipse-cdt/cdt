/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * Listener that is informed when a translation unit is saved.
 * <p>
 * The difference between this interface and JDT's {@code IPostSaveListener} is that CDT does not 
 * allow post-save listeners to make changes to the saved file.
 * </p>
 * 
 * @since 5.4
 */
public interface IPostSaveListener {
	/**
	 * Informs this post-save listener that the given translation unit has been saved. The listener
	 * should <strong>not</strong> modify the given translation unit.
	 * 
	 * @param translationUnit the translation unit which was saved
	 * @param monitor the progress monitor for reporting progress
	 */
	void saved(ITranslationUnit translationUnit, IProgressMonitor monitor);
}
