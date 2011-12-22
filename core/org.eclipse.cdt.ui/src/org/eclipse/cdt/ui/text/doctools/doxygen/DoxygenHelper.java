/*******************************************************************************
 * Copyright (c) 2008, 2011 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools.doxygen;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.doctools.generic.GenericDocTag;

/**
 * Makes available information for Doxygen support.
 *
 * @since 5.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DoxygenHelper extends AbstractPreferenceInitializer {
	private static final IPath TAGS_CSV= new Path("doxygenTags.csv"); //$NON-NLS-1$
	private static GenericDocTag[] fTags;

	/**
	 * @deprecated Use {@link PreferenceConstants#DOXYGEN_TAG_COLOR} instead.
	 */
	@Deprecated
	public static final String DOXYGEN_TAG_RECOGNIZED= PreferenceConstants.DOXYGEN_TAG_COLOR;
	/**
	 * @deprecated Use {@link PreferenceConstants#DOXYGEN_SINGLE_LINE_COLOR} instead.
	 */
	@Deprecated
	public static final String DOXYGEN_SINGLE_TOKEN= PreferenceConstants.DOXYGEN_SINGLE_LINE_COLOR;
	/**
	 * @deprecated Use {@link PreferenceConstants#DOXYGEN_MULTI_LINE_COLOR} instead.
	 */
	@Deprecated
	public static final String DOXYGEN_MULTI_TOKEN= PreferenceConstants.DOXYGEN_MULTI_LINE_COLOR;

	/**
	 * @return The tags which are understood by default by the doxygen tool.
	 */
	public static GenericDocTag[] getDoxygenTags() {
		if(fTags==null) {
			InputStream is = null;
			try {
				List<GenericDocTag> temp= new ArrayList<GenericDocTag>();
				is= FileLocator.openStream(CUIPlugin.getDefault().getBundle(), TAGS_CSV, false);
				BufferedReader br= new BufferedReader(new InputStreamReader(is));
				StringBuilder content= new StringBuilder(2000);
				for(String line= br.readLine(); line!=null; line= br.readLine()) {
					content.append(line).append('\n');
				}
				content.append("dummy-for-split"); //$NON-NLS-1$
				String[] values= content.toString().split("(\\s)*,(\\s)*"); //$NON-NLS-1$

				for(int i=0; i+1<values.length; i+=2) {
					temp.add(new GenericDocTag(values[i], values[i+1]));
				}
				fTags= temp.toArray(new GenericDocTag[temp.size()]);
			} catch(IOException ioe) {
				fTags= new GenericDocTag[0];
				CUIPlugin.log(ioe);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException exc) {}
				}
			}
		}
		return fTags;
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		// doxygen colors are now initialized in PreferenceConstants
	}
}
