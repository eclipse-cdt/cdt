/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language;

import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4e.cpp.language.LSExtendedResponses.IndexingProgressStats;
import org.eclipse.lsp4e.cpp.language.LSExtendedResponses.CqueryInactiveRegion;
import org.eclipse.lsp4e.cpp.language.LSExtendedResponses.CquerySemanticHighlights;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;

public class Server2ClientProtocolExtension extends LanguageClientImpl {

	@JsonNotification("$cquery/progress")
	public final void indexingProgress(IndexingProgressStats stats) {

	}

	@JsonNotification("$cquery/setInactiveRegions")
	public final void setInactiveRegions(CqueryInactiveRegion regions) {

	}


	@JsonNotification("$cquery/publishSemanticHighlighting")
	public final void semanticHighlights(CquerySemanticHighlights highlights) {

	}
}
