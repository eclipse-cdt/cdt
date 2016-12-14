/*******************************************************************************
 * Copyright (c) 2016 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.model.ITranslationUnitHolder;

/**
 * A text editor whose contents are represented by the C model.
 * Implementations include CEditor and AsmTextEditor.
 * @since 6.2
 */
public interface ICModelBasedEditor extends ITextEditor, ITranslationUnitHolder {
}
