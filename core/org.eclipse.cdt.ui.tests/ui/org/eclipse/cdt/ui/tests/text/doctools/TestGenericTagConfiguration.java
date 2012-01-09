/*******************************************************************************
 * Copyright (c) 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.doctools;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.RGB;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.doctools.generic.AbstractGenericTagDocCommentViewerConfiguration;
import org.eclipse.cdt.ui.text.doctools.generic.GenericDocTag;

public class TestGenericTagConfiguration extends AbstractGenericTagDocCommentViewerConfiguration {
	public static final RGB DEFAULTRGB= new RGB(63, 95, 191);
	
	protected char[] fCommentMarkers;
	
	public TestGenericTagConfiguration(String commentMarkers, String tags, String tagMarkers, String defaultToken, String tagToken) {
		super(mkTags(tags.split("(\\s)*,(\\s)*")), tagMarkers.toCharArray(), defaultToken, tagToken);
		IPreferenceStore cuis= CUIPlugin.getDefault().getPreferenceStore();
		PreferenceConverter.setDefault(cuis, defaultToken, DEFAULTRGB);
		PreferenceConverter.setDefault(cuis, tagToken, new RGB(127, 159, 191));
		fCommentMarkers= commentMarkers.toCharArray();
	}
	
	@Override
	public IAutoEditStrategy createAutoEditStrategy() {
		return null;
	}
	
	private static GenericDocTag[] mkTags(String[] tagNames) {
		GenericDocTag[] tags= new GenericDocTag[tagNames.length];
		for(int i=0; i<tagNames.length; i++) {
			tags[i]= new GenericDocTag(tagNames[i], "no description");
		}
		return tags;
	}
	
	@Override
	public boolean isDocumentationComment(IDocument doc, int offset, int length) {
		try {
			if(offset+2 < doc.getLength()) {
				char c= doc.getChar(offset+2);
				for(int i=0; i<fCommentMarkers.length; i++)
					if(c == fCommentMarkers[i])
						return true;
				return false;
			}
		} catch(BadLocationException ble) {
			CUIPlugin.log(ble);
		}
		return false;
	}
	
	public static class A extends TestGenericTagConfiguration {
		public A() {super("A", "", "@", "test.token.A.default", "test.token.A.tag");}
	}
	
	public static class B extends TestGenericTagConfiguration {
		public B() {super("B", "", "@", "test.token.B.default", "test.token.B.tag");}
	}
	
	public static class C extends TestGenericTagConfiguration {
		public C() {super("C", "", "@", "test.token.C.default", "test.token.C.tag");}
	}
	
	public static class ABC extends TestGenericTagConfiguration {
		public ABC() {super("ABC", "", "@", "test.token.ABC.default", "test.token.ABC.tag");}
	}
	
	public static class BDFG extends TestGenericTagConfiguration {
		public BDFG() {super("BDFG", "", "@", "test.token.BDFG.default", "test.token.BDFG.tag");}
	}
	
	public static class PUNC extends TestGenericTagConfiguration {
		public PUNC() {super("!*#", "", "@", "test.token.BDFG.default", "test.token.BDFG.tag");}
	}
}
