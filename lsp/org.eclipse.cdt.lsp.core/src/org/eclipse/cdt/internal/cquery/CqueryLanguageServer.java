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

package org.eclipse.cdt.internal.cquery;

import java.net.URI;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.lsp.LanguageServerConfiguration;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.google.gson.JsonObject;

public class CqueryLanguageServer implements LanguageServerConfiguration {

	public static final String CQUERY_ID = "cquery"; //$NON-NLS-1$

	@Override
	public String identifier() {
		return CqueryLanguageServer.CQUERY_ID;
	}

	@Override
	public String label() {
		return "CQuery";
	}

	@Override
	public Object options(Object defaults, URI uri) {
		// TODO: Allow user to specify cache directory path
		IPath cacheDirectory = Path.fromOSString(uri.getPath()).append(".cdt-lsp/cquery_index"); //$NON-NLS-1$
		JsonObject result = (defaults instanceof JsonObject) ? (JsonObject) defaults : new JsonObject();
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
