/*******************************************************************************
 * Copyright (c) 2013 Sebastian Bauer
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sebastian Bauer - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.c.hover;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorInput;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IDescription;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * Class implementing a text hover using description stored in the IBinding.
 */
public class DescriptionHover extends AbstractCEditorTextHover {

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (getEditor() == null) return null;

		IEditorInput editorInput = getEditor().getEditorInput();
		ITranslationUnit itu = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);

		try {
			ASTVisitor visit = new ASTVisitor(true) {
				@Override
				public int visit(IASTName name) {
					name.resolveBinding();
					return PROCESS_CONTINUE;
				}
			};
			IASTTranslationUnit ast = itu.getAST();
			ast.accept(visit);

			IASTName hoveredName = ast.getNodeSelector(null).findEnclosingName(hoverRegion.getOffset(), 1);

			final IBinding binding = hoveredName.resolveBinding();
			final IDescription desc = (IDescription)binding.getAdapter(IDescription.class);

			if (desc != null) {
				return desc.getDescription();
			}
		} catch (Exception e) {
			/* Ignore */
		}
		return null;
	}
}
