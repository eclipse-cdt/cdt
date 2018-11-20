/*******************************************************************************
 * Copyright (c) 2002, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.ILocationProvider;

/**
 * ITranslationUnitEditorInput
 */
public interface ITranslationUnitEditorInput extends IStorageEditorInput, ILocationProvider {

	ITranslationUnit getTranslationUnit();

}
