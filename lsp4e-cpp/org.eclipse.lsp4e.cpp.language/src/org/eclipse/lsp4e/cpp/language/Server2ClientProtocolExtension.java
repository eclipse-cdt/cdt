/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language;

import java.net.URI;
import java.util.List;

import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4e.cpp.language.cquery.CqueryInactiveRegions;
import org.eclipse.lsp4e.cpp.language.cquery.CquerySemanticHighlights;
import org.eclipse.lsp4e.cpp.language.cquery.IndexingProgressStats;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;


@SuppressWarnings("restriction")
public class Server2ClientProtocolExtension extends LanguageClientImpl {

	@JsonNotification("$cquery/progress")
	public final void indexingProgress(IndexingProgressStats stats) {

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				final String cqueryStatusFieldId = "org.eclipse.lsp4e.cpp.status"; //$NON-NLS-1$
				final int width = 28;
				IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
				for (IWorkbenchWindow window : workbenchWindows) {
					StatusLineManager statusLine = ((WorkbenchWindow) window).getStatusLineManager();
					StatusLineContributionItem cqueryStatusField = (StatusLineContributionItem) statusLine.find(cqueryStatusFieldId);
					if (cqueryStatusField == null) {
						cqueryStatusField = new StatusLineContributionItem(cqueryStatusFieldId, width);
						statusLine.add(cqueryStatusField);
					}
					String msg = stats.getTotalJobs() > 0 ? NLS.bind(Messages.CqueryStateBusy, stats.getTotalJobs())
														  : Messages.CqueryStateIdle;
					cqueryStatusField.setText(msg);
				}
			}
		});
	}

	@JsonNotification("$cquery/setInactiveRegions")
	public final void setInactiveRegions(CqueryInactiveRegions regions) {
		URI uri = regions.getUri();
		List<Range> inactiveRegions = regions.getInactiveRegions();
		CqueryLineBackgroundListener.fileInactiveRegionsMap.put(uri, inactiveRegions);
	}

	@JsonNotification("$cquery/publishSemanticHighlighting")
	public final void semanticHighlights(CquerySemanticHighlights highlights) {
		// TODO: Implement
	}
}
