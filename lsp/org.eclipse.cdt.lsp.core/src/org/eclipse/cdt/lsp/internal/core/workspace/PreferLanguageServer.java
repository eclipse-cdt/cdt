/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.core.workspace;

import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.cdt.lsp.core.Activator;
import org.eclipse.cdt.lsp.core.preferences.LanguageServerPreferenceMetadata;
import org.eclipse.cdt.lsp.internal.core.preferences.LanguageServerDefaults;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.eclipse.jface.text.IDocument;

public final class PreferLanguageServer implements Predicate<IDocument> {

	private final ResolveDocumentFile file;
	private final LanguageServerPreferenceMetadata metadata;

	public PreferLanguageServer() {
		file = new ResolveDocumentFile();
		metadata = new LanguageServerDefaults();
	}

	@Override
	public boolean test(IDocument document) {
		Optional<IProject> project = file.apply(document).map(IFile::getProject);
		if (project.isPresent()) {
			return forProject(project.get());
		}
		return forExternal(document);
	}

	private boolean forExternal(IDocument document) {
		//let's use workspace-level setting
		PreferenceMetadata<Boolean> option = metadata.preferLanguageServer();
		return Platform.getPreferencesService().getBoolean(Activator.PLUGIN_ID, option.identifer(),
				option.defaultValue(), null);
	}

	private boolean forProject(IProject project) {
		PreferenceMetadata<Boolean> option = metadata.preferLanguageServer();
		return Platform.getPreferencesService().getBoolean(Activator.PLUGIN_ID, option.identifer(),
				option.defaultValue(), new IScopeContext[] { new ProjectScope(project) });
	}

}
