/*******************************************************************************
 * Copyright (c) 2016 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.cdt.core.model.ITranslationUnitHolder;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A text editor whose contents are represented by the C model.
 * Implementations include CEditor and AsmTextEditor.
 * @since 6.2
 */
public interface ICModelBasedEditor extends ITextEditor, ITranslationUnitHolder {
}
