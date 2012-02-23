/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.ui.cxx.externaltool;

import static org.eclipse.cdt.codan.core.cxx.util.FileTypes.isCppFile;
import static org.eclipse.cdt.codan.core.cxx.util.FileTypes.isHeaderFile;
import static org.eclipse.cdt.codan.ui.CodanEditorUtility.isResourceOpenInEditor;
import static org.eclipse.cdt.codan.ui.cxx.externaltool.CEditors.activeCEditor;

import org.eclipse.cdt.codan.core.externaltool.ISupportedResourceVerifier;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * Implementation of <code>{@link ISupportedResourceVerifier}</code> for C/C++ files.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
class CxxSupportedResourceVerifier implements ISupportedResourceVerifier {
	/**
	 * Indicates whether the external tool is capable of processing the given
	 * <code>{@link IResource}</code>.
	 * <p>
	 * The minimum requirements that the given {@code IResource} should satisfy are:
	 * <ul>
	 * <li>should be C/C++ file</li>
	 * <li>should be displayed in the current active {@code CEditor}</li>
	 * <li>should not have any unsaved changes</li>
	 * </ul>
	 * </p>
	 * @param resource the given {@code IResource}.
	 * @return {@code true} if the external tool is capable of processing the given file,
	 *         {@code false} otherwise.
	 */
	@Override
	public boolean isSupported(IResource resource) {
		return isFileOfSupportedType(resource) && isOpenInActiveCEditor(resource);
	}

	private boolean isFileOfSupportedType(IResource resource) {
		return isCppFile(resource) || isHeaderFile(resource);
	}

	private boolean isOpenInActiveCEditor(IResource resource) {
		TextEditor activeCEditor = activeCEditor();
		if (activeCEditor == null) {
			return false;
		}
		return !activeCEditor.isDirty() && isResourceOpenInEditor(resource, activeCEditor);
	}
}
