/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language;

import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4e.cpp.language.cquery.CqueryInactiveRegions;
import org.eclipse.lsp4e.cpp.language.cquery.CquerySemanticHighlights;
import org.eclipse.lsp4e.cpp.language.cquery.IndexingProgressStats;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;

@SuppressWarnings("restriction")
public class Server2ClientProtocolExtension extends LanguageClientImpl {

	@JsonNotification("$cquery/progress")
	public final void indexingProgress(IndexingProgressStats stats) {

		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		        WorkbenchWindow workbenchWindow = (WorkbenchWindow)PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		        StatusLineManager statusLineManager = workbenchWindow.getStatusLineManager();
		        if(stats.getStatsSum()==0) {
					statusLineManager.setMessage("CQuery : Idle"); //$NON-NLS-1$
				}
				else {
					statusLineManager.setMessage("CQuery : Busy | "+stats.getStatsSum()+" Jobs");  //$NON-NLS-1$ //$NON-NLS-2$
				}
		    }
        });
	}

	@JsonNotification("$cquery/setInactiveRegions")
	public final void setInactiveRegions(CqueryInactiveRegions regions) {
		// TODO: Implement
	}


	@JsonNotification("$cquery/publishSemanticHighlighting")
	public final void semanticHighlights(CquerySemanticHighlights highlights) {
		// TODO: Implement
	}
}
