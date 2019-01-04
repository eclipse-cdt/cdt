/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.lsp.core;

import java.net.URI;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.google.gson.JsonObject;

public class CqueryLanguageServer implements ICPPLanguageServer {

	@Override
	public Object getLSSpecificInitializationOptions(Object defaultInitOptions, URI rootPath) {

		// TODO: Allow user to specify cache directory path

		IPath cacheDirectory = Path.fromOSString(rootPath.getPath()).append(".cdt-lsp/cquery_index"); //$NON-NLS-1$
		JsonObject result = (defaultInitOptions instanceof JsonObject) ? (JsonObject) defaultInitOptions
				: new JsonObject();
		result.addProperty("cacheDirectory", cacheDirectory.toString()); //$NON-NLS-1$
		result.addProperty("emitInactiveRegions", //$NON-NLS-1$
				CUIPlugin.getDefault().getPreferenceStore().getBoolean(CEditor.INACTIVE_CODE_ENABLE));
		JsonObject semanticHighlights = new JsonObject();
		semanticHighlights.addProperty("enabled", CUIPlugin.getDefault().getPreferenceStore() //$NON-NLS-1$
				.getBoolean(org.eclipse.cdt.ui.PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED));
		result.add("highlight", semanticHighlights); //$NON-NLS-1$
		return result;
	}

}
