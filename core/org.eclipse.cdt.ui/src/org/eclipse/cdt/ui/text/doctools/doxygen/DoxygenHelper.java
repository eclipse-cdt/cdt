/*******************************************************************************
 * Copyright (c) 2008, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import org.eclipse.cdt.ui.CUIPlugin;
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
	
	public static final String DOXYGEN_TAG_RECOGNIZED= "org.eclipse.cdt.internal.ui.text.doctools.doxygen.recognizedTag"; //$NON-NLS-1$
	public static final String DOXYGEN_SINGLE_TOKEN= "org.eclipse.cdt.internal.ui.text.doctools.doxygen.single"; //$NON-NLS-1$
	public static final String DOXYGEN_MULTI_TOKEN= "org.eclipse.cdt.internal.ui.text.doctools.doxygen.multi"; //$NON-NLS-1$
	
	/**
	 * @return The tags which are understood by default by the doxygen tool.
	 */
	public static GenericDocTag[] getDoxygenTags() {
		if(fTags==null) {
			try {
				List<GenericDocTag> temp= new ArrayList<GenericDocTag>();
				InputStream is= FileLocator.openStream(CUIPlugin.getDefault().getBundle(), TAGS_CSV, false);
				BufferedReader br= new BufferedReader(new InputStreamReader(is));
				StringBuffer content= new StringBuffer();
				for(String line= br.readLine(); line!=null; line= br.readLine()) {
					content.append(line+"\n"); //$NON-NLS-1$
				}
				String[] values= (content.toString()+"dummy-for-split").split("(\\s)*,(\\s)*"); //$NON-NLS-1$ //$NON-NLS-2$
				
				for(int i=0; i+1<values.length; i+=2) {
					temp.add(new GenericDocTag(values[i], values[i+1]));
				}
				fTags= temp.toArray(new GenericDocTag[temp.size()]);
			} catch(IOException ioe) {
				fTags= new GenericDocTag[0];
				CUIPlugin.log(ioe);
			}
		}
		return fTags;
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore cuis= CUIPlugin.getDefault().getPreferenceStore();
		PreferenceConverter.setDefault(cuis, DoxygenHelper.DOXYGEN_MULTI_TOKEN, new RGB(63, 95, 191));
		PreferenceConverter.setDefault(cuis, DoxygenHelper.DOXYGEN_SINGLE_TOKEN, new RGB(63, 95, 191));
		PreferenceConverter.setDefault(cuis, DoxygenHelper.DOXYGEN_TAG_RECOGNIZED, new RGB(127, 159, 191));
	}
}
