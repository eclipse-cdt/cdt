/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

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
import org.eclipse.cdt.core.dom.ast.tag.ITaggable;
import org.eclipse.cdt.core.dom.ast.tag.IWritableTag;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class TagManager
{
	private static TagManager INSTANCE;

	private Map<String, TaggerDescriptor> taggers;

	public static TagManager getInstance()
	{
		if( INSTANCE == null )
			INSTANCE = new TagManager();
		return INSTANCE;
	}

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

	/** Provide an opportunity for the specified tagger to process the given values.  The tagger will only
	 *  run if its enablement expression returns true for the arguments. */
	public ITag process( String taggerId, ITaggable taggable, IBinding binding, IASTName ast )
	{
		TaggerDescriptor desc = taggers.get( taggerId );
		if( desc == null )
			return null;

		IBindingTagger tagger = desc.getBindingTaggerFor( binding, ast );
		return tagger == null ? null : tagger.process( taggable, binding, ast );
	}

	/** Provide an opportunity for all enabled taggers to process the given values. */
	public Iterable<ITag> process( ITaggable taggable, IBinding binding, IASTName ast )
	{
		List<ITag> tags = new LinkedList<ITag>();
		for( TaggerDescriptor desc : taggers.values() )
		{
			IBindingTagger tagger = desc.getBindingTaggerFor( binding, ast );
			if( tagger != null )
			{
				ITag tag = tagger.process( taggable, binding, ast );
				if( tag != null )
					tags.add( tag );
			}
		}
		return tags;
	}

	/** Copy all tags from the source to the destination binding. */
	public static void copyTags( IBinding dst, IBinding src )
	{
		ITaggable srcTaggable = CCorePlugin.getTaggableService().findTaggable( src );
		if( srcTaggable == null )
			return;

		ITaggable dstTaggable = CCorePlugin.getTaggableService().findTaggable( dst );
		if( dstTaggable == null )
			return;

		for( ITag srcTag : srcTaggable.getTags() )
		{
			byte[] srcData = srcTag.getBytes( 0, -1 );
			if( srcData == null )
				throw new RuntimeException( "unable to read tag data" ); //$NON-NLS-1$

			IWritableTag dstTag = dstTaggable.createTag( srcTag.getTaggerId(), srcData.length );
			if( ! dstTag.putBytes( 0, srcData, srcData.length ) )
				throw new RuntimeException( "unable to write tag data" ); //$NON-NLS-1$
		}
	}
}
