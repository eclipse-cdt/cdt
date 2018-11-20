/*******************************************************************************
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Eidsness - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.ast.tag;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.tag.IBindingTagger;
import org.eclipse.cdt.core.dom.ast.tag.ITag;
import org.eclipse.cdt.core.dom.ast.tag.ITagReader;
import org.eclipse.cdt.core.dom.ast.tag.ITagWriter;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.cdt.internal.core.pdom.tag.PDOMTaggable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class TagManager {
	private static final String EXTENSION_POINT = "tagger"; //$NON-NLS-1$
	private static TagManager INSTANCE;

	private Map<String, TaggerDescriptor> taggers;

	public static TagManager getInstance() {
		if (INSTANCE == null)
			INSTANCE = new TagManager();
		return INSTANCE;
	}

	private TagManager() {
		taggers = loadExtensions();
	}

	private static Map<String, TaggerDescriptor> loadExtensions() {
		Map<String, TaggerDescriptor> taggers = new HashMap<>();

		// Load the extensions
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(CCorePlugin.PLUGIN_ID, EXTENSION_POINT);
		for (IConfigurationElement element : elements) {
			TaggerDescriptor desc = new TaggerDescriptor(element);
			taggers.put(desc.getId(), desc);
		}

		return taggers;
	}

	/**
	 * Provides an opportunity for the specified tagger to process the given values. The tagger will
	 * only run if its enablement expression returns true for the arguments.
	 */
	public ITag process(String taggerId, ITagWriter tagWriter, IBinding binding, IASTName ast) {
		TaggerDescriptor desc = taggers.get(taggerId);
		if (desc == null)
			return null;

		IBindingTagger tagger = desc.getBindingTaggerFor(binding, ast);
		return tagger == null ? null : tagger.process(tagWriter, binding, ast);
	}

	/** Provides an opportunity for all enabled taggers to process the given values. */
	public Iterable<ITag> process(ITagWriter tagWriter, IBinding binding, IASTName ast) {
		List<ITag> tags = new LinkedList<>();
		for (TaggerDescriptor desc : taggers.values()) {
			IBindingTagger tagger = desc.getBindingTaggerFor(binding, ast);
			if (tagger != null) {
				ITag tag = tagger.process(tagWriter, binding, ast);
				if (tag != null)
					tags.add(tag);
			}
		}

		return tags;
	}

	/** Adds or removes tags from the destination to ensure that it has the same tag information as the source. */
	public void syncTags(IPDOMBinding dst, IBinding src) {
		// don't try to copy any tags when there are no contributors to this extension point
		if (dst == null || taggers.isEmpty())
			return;

		ITagReader tagReader = CCorePlugin.getTagService().findTagReader(src);
		if (tagReader == null)
			return;

		ITagWriter tagWriter = new PDOMTaggable(dst.getPDOM(), dst.getRecord());
		tagWriter.setTags(tagReader.getTags());
	}
}
