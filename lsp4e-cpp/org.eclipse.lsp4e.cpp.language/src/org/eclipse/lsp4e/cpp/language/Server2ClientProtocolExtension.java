/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.lsp4e.LSPEclipseUtils;
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

	public static URI getUri(IDocument document) {
		URI uri = null;
		IFile file = LSPEclipseUtils.getFile(document);
		if (file != null) {
			uri = LSPEclipseUtils.toUri(file);
		}
		return uri;
	}

	@JsonNotification("$cquery/publishSemanticHighlighting")
	public final void semanticHighlights(CquerySemanticHighlights highlights) {
		URI uriReceived = highlights.getUri();
		List<PresentationReconcilerCPP> matchingReconcilers = new ArrayList<>();

		for (PresentationReconcilerCPP eachReconciler: PresentationReconcilerCPP.presentationReconcilers) {
			IDocument currentReconcilerDoc = eachReconciler.getTextViewer().getDocument();
			URI currentReconcilerUri = getUri(currentReconcilerDoc);

			if(currentReconcilerUri == null) {
				continue;
			}

			if (uriReceived.equals(currentReconcilerUri)) {
				matchingReconcilers.add(eachReconciler);
			}
		}

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				for (PresentationReconcilerCPP p: matchingReconcilers) {
					p.setSemanticHighlights(highlights.getSymbols());
					IDocument currentReconcilerDoc = p.getTextViewer().getDocument();
					if (currentReconcilerDoc == null) {
						continue;
					}
					TextPresentation textPresentation = p.createPresentation(new Region(0, currentReconcilerDoc.getLength()), currentReconcilerDoc);
					p.getTextViewer().changeTextPresentation(textPresentation, false);
				}
			}
		});
	}
}
