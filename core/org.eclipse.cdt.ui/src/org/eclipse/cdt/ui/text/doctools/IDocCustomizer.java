/*******************************************************************************
 * Copyright (c) 2020 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Marco Stornelli - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.ui.text.doctools.DefaultMultilineCommentAutoEditStrategy.CustomizeOptions;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

/**
 * @since 6.7
 */
public interface IDocCustomizer {
	StringBuilder customizeAfterNewLineForDeclaration(IDocument doc, IASTDeclaration dec, ITypedRegion region,
			CustomizeOptions options);
}
