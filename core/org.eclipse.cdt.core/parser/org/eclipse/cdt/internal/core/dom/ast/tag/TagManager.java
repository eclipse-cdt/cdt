/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.internal.core.dom.ast.tag;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.tag.IBindingTagger;
import org.eclipse.cdt.core.dom.ast.tag.ITag;
import org.eclipse.cdt.core.dom.ast.tag.ITaggable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class TagManager
{
	public static final TagManager INSTANCE = new TagManager();

	private Map<String, TaggerDescriptor> taggers;

	private TagManager()
	{
		taggers = loadExtensions();
	}

	private static final String ExtensionPoint = "tagger"; //$NON-NLS-1$

	private static Map<String, TaggerDescriptor> loadExtensions()
	{
		Map<String, TaggerDescriptor> taggers = new HashMap<String, TaggerDescriptor>();

		// load the extensions
		IConfigurationElement[] elements
			= Platform.getExtensionRegistry().getConfigurationElementsFor( CCorePlugin.PLUGIN_ID, ExtensionPoint );
		for (IConfigurationElement element : elements)
		{
			TaggerDescriptor desc = new TaggerDescriptor( element );
			taggers.put( desc.getId(), desc );
		}

		return taggers;
	}

	/**
	 * Provide an opportunity for all registered taggers to process the given bindings.
	 */
	public void process( IBinding dest, IBinding source )
	{
		// The taggable is created from the destination binding so that the associated information is stored
		// in the same place.  E.g., when creating a PDOMBinding, we want to store the tag in the PDOM's database.
		ITaggable taggable = ITaggable.Converter.from( dest );
		if( taggable == null )
			return;

		for( TaggerDescriptor desc : taggers.values() )
		{
			IBindingTagger tagger = desc.getBindingTaggerFor( dest );
			if( tagger != null )
				tagger.process( taggable, source );
		}
	}

	/**
	 * Provide an opportunity for the specific tagger to process the given bindings.  Returns the tag
	 * if one was created and null otherwise.
	 */
	public ITag process( String taggerId, ITaggable taggable, IBinding source )
	{
		if( taggable == null )
			return null;

		TaggerDescriptor desc = taggers.get( taggerId );
		if( desc == null )
			return null;

		IBindingTagger tagger = desc.getBindingTaggerFor( source );
		return tagger == null ? null : tagger.process( taggable, source );
	}
}
