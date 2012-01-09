/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.spelling;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.ISpellingEngine;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;
import org.eclipse.ui.texteditor.spelling.SpellingContext;
import org.eclipse.ui.texteditor.spelling.SpellingEngineDescriptor;

import org.eclipse.cdt.core.CCorePlugin;

/**
 * A dispatcher that decides what spelling engine to use depending on content type.
 * When C/C++ spelling engine is selected in Preferences/General/Editors/Text Editors/Spelling
 * this class is called to perform spelling check for all text-based content types.
 * If the content type does not match one of C/C++ content types, the spelling check
 * is delegated to the default spelling engine, most likely the one provided by JDT.
 */
public class SpellingEngineDispatcher implements ISpellingEngine {
	private static final String C_SPELLING_ENGINE_ID = "org.eclipse.cdt.internal.ui.text.spelling.CSpellingEngine"; //$NON-NLS-1$

	/** C/C++ source content type */
	private static final IContentType CHEADER_CONTENT_TYPE= Platform.getContentTypeManager().getContentType(CCorePlugin.CONTENT_TYPE_CHEADER);
	private static final IContentType CSOURCE_CONTENT_TYPE= Platform.getContentTypeManager().getContentType(CCorePlugin.CONTENT_TYPE_CSOURCE);
	private static final IContentType CXXHEADER_CONTENT_TYPE= Platform.getContentTypeManager().getContentType(CCorePlugin.CONTENT_TYPE_CXXHEADER);
	private static final IContentType CXXSOURCE_CONTENT_TYPE= Platform.getContentTypeManager().getContentType(CCorePlugin.CONTENT_TYPE_CXXSOURCE);

	/** Available spelling engines by content type */
	private Map<IContentType, SpellingEngine> fEngines= new HashMap<IContentType, SpellingEngine>();
	private ISpellingEngine defaultEngine;

	/**
	 * Initialize concrete engines.
	 */
	public SpellingEngineDispatcher() {
		SpellingEngine engine = new CSpellingEngine();
		if (CHEADER_CONTENT_TYPE != null)
			fEngines.put(CHEADER_CONTENT_TYPE, engine);
		if (CSOURCE_CONTENT_TYPE != null)
			fEngines.put(CSOURCE_CONTENT_TYPE, engine);
		if (CXXHEADER_CONTENT_TYPE != null)
			fEngines.put(CXXHEADER_CONTENT_TYPE, engine);
		if (CXXSOURCE_CONTENT_TYPE != null)
			fEngines.put(CXXSOURCE_CONTENT_TYPE, engine);
		try {
			SpellingEngineDescriptor descriptor =
					EditorsUI.getSpellingService().getDefaultSpellingEngineDescriptor();
			if (!C_SPELLING_ENGINE_ID.equals(descriptor.getId())) {  // Do not delegate to itself.
				defaultEngine =	descriptor.createEngine();
			}
		} catch (CoreException e) {
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.ISpellingEngine#check(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IRegion[], org.eclipse.ui.texteditor.spelling.SpellingContext, org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void check(IDocument document, IRegion[] regions, SpellingContext context, ISpellingProblemCollector collector, IProgressMonitor monitor) {
		ISpellingEngine engine= getEngine(context.getContentType());
		if (engine == null)
			engine= defaultEngine;
		if (engine != null)
			engine.check(document, regions, context, collector, monitor);
	}

	/**
	 * Returns a spelling engine for the given content type or
	 * <code>null</code> if none could be found.
	 *
	 * @param contentType the content type
	 * @return a spelling engine for the given content type or
	 *         <code>null</code> if none could be found
	 */
	private ISpellingEngine getEngine(IContentType contentType) {
		if (contentType == null)
			return null;

		if (fEngines.containsKey(contentType))
			return fEngines.get(contentType);

		return getEngine(contentType.getBaseType());
	}
}
