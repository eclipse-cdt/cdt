/*******************************************************************************
 * Copyright (c) 2007, 2008 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.spelling;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.ISpellingEngine;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;
import org.eclipse.ui.texteditor.spelling.SpellingContext;
import org.eclipse.ui.texteditor.spelling.SpellingService;

/**
 * Platform's SpellingService uses a spelling engine that is independent
 * of the content type (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=185695).
 * We are providing our own SpellingService to be able to use the C/C++ specific
 * spelling engine even when it is not selected in
 * Preferences/General/Editors/Text Editors/Spelling.
 */
public class CSpellingService extends SpellingService {
	private static CSpellingService fInstance;
	
	private IPreferenceStore fPreferences;
	private ISpellingEngine fEngine;
	

	public static CSpellingService getInstance() {
		if (fInstance == null) {
			fInstance = new CSpellingService(EditorsUI.getPreferenceStore());
		}
		return fInstance;
	}

	@Override
	public void check(final IDocument document, final IRegion[] regions, final SpellingContext context,
			final ISpellingProblemCollector collector, final IProgressMonitor monitor) {
		try {
			collector.beginCollecting();
			if (fPreferences.getBoolean(PREFERENCE_SPELLING_ENABLED))
				if (fEngine == null) {
					fEngine = new CSpellingEngine();
				}
				ISafeRunnable runnable= new ISafeRunnable() {
					@Override
					public void run() throws Exception {
						fEngine.check(document, regions, context, collector, monitor);
					}
					@Override
					public void handleException(Throwable x) {
					}
				};
				SafeRunner.run(runnable);
		} finally {
			collector.endCollecting();
		}
	}
	
	private CSpellingService(IPreferenceStore preferences) {
		super(preferences);
		fPreferences = preferences;
	}
}
