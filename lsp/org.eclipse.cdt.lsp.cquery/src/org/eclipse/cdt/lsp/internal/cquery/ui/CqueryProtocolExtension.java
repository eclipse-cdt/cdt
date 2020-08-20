/*******************************************************************************
 * Copyright (c) 2018, 2020 Manish Khurana, Nathan Ridge and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.cquery.ui;

import org.eclipse.cdt.lsp.LanguageProtocolExtension;
import org.eclipse.cdt.lsp.internal.core.ShowStatus;
import org.eclipse.cdt.lsp.internal.cquery.CqueryInactiveRegions;
import org.eclipse.cdt.lsp.internal.cquery.CqueryMessages;
import org.eclipse.cdt.lsp.internal.cquery.CquerySemanticHighlights;
import org.eclipse.cdt.lsp.internal.cquery.IndexingProgressStats;
import org.eclipse.cdt.lsp.internal.text.SetInactiveRegions;
import org.eclipse.cdt.lsp.internal.ui.StatusLineMessage;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.osgi.service.component.annotations.Component;

@SuppressWarnings("restriction")
@Component
public class CqueryProtocolExtension implements LanguageProtocolExtension {

	private final ShowStatus progress;
	private final SetInactiveRegions inactive;
	private final PublishSemanticHighlighting highlighting;

	public CqueryProtocolExtension() {
		this.progress = new ShowStatus(() -> CqueryMessages.CqueryLanguageServer_label, new StatusLineMessage());
		this.inactive = new SetInactiveRegions();
		this.highlighting = new PublishSemanticHighlighting();
	}

	@Override
	public String targetIdentifier() {
		return "cquery"; //$NON-NLS-1$
	}

	@JsonNotification("$cquery/progress")
	public final void indexingProgress(IndexingProgressStats stats) {
		progress.accept(stats::getTotalJobs);
	}

	@JsonNotification("$cquery/setInactiveRegions")
	public final void setInactiveRegions(CqueryInactiveRegions regions) {
		inactive.accept(regions::getUri, regions::getInactiveRegions);
	}

	@JsonNotification("$cquery/publishSemanticHighlighting")
	public final void semanticHighlights(CquerySemanticHighlights highlights) {
		highlighting.accept(highlights);
	}

}
