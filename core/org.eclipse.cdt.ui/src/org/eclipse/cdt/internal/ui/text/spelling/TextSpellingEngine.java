/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.spelling;

import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellChecker;
import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellEventListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;

/**
 * Text spelling engine
 */
public class TextSpellingEngine extends SpellingEngine {
	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.SpellingEngine#check(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IRegion[], org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellChecker, org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void check(IDocument document, IRegion[] regions, ISpellChecker checker,
			ISpellingProblemCollector collector, IProgressMonitor monitor) {
		ISpellEventListener listener = new SpellEventListener(collector, document);
		try {
			checker.addListener(listener);
			for (int i = 0; i < regions.length; i++) {
				if (monitor != null && monitor.isCanceled())
					return;
				checker.execute(new SpellCheckIterator(document, regions[i], checker.getLocale()));
			}
		} finally {
			checker.removeListener(listener);
		}
	}
}
