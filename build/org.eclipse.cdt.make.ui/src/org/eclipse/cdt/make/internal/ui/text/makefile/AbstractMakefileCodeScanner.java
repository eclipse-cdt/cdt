/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.text.makefile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.preferences.MakefileEditorPreferenceConstants;
import org.eclipse.cdt.make.internal.ui.text.ColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * AbstractMakefileEditorScanner
 */
public abstract class AbstractMakefileCodeScanner extends RuleBasedScanner {

	private Map fTokenMap= new HashMap();
	private String[] fPropertyNamesColor;
	/**
	 * Preference keys for boolean preferences which are <code>true</code>,
	 * iff the corresponding token should be rendered bold. 
	 */
	private String[] fPropertyNamesBold;
	/**
	 * Preference keys for boolean preferences which are <code>true</code>,
	 * iff the corresponding token should be rendered italic.
	 */
	private String[] fPropertyNamesItalic;
	

	/** 
	 * Returns the list of preference keys which define the tokens
	 * used in the rules of this scanner.
	 */
	abstract protected String[] getTokenProperties();
		
	/**
	 * Creates the list of rules controlling this scanner.
	 */
	abstract protected List createRules();
		
	/**
	 * Must be called after the constructor has been called.
	 */
	public final void initialize() {
		
		fPropertyNamesColor= getTokenProperties();
		int length= fPropertyNamesColor.length;
		fPropertyNamesBold= new String[length];
		fPropertyNamesItalic= new String[length];

		for (int i= 0; i < length; i++) {
			fPropertyNamesBold[i]= fPropertyNamesColor[i] + MakefileEditorPreferenceConstants.EDITOR_BOLD_SUFFIX;
			fPropertyNamesItalic[i]= fPropertyNamesColor[i] + MakefileEditorPreferenceConstants.EDITOR_ITALIC_SUFFIX;
			addToken(fPropertyNamesColor[i], fPropertyNamesBold[i], fPropertyNamesItalic[i]);
		}
		
		initializeRules();
	}

	private void initializeRules() {
		List rules= createRules();
		if (rules != null) {
			IRule[] result= new IRule[rules.size()];
			rules.toArray(result);
			setRules(result);
		}
	}

	protected Token getToken(String key) {
		return (Token) fTokenMap.get(key);
	}

	private void addToken(String colorKey, String boldKey, String italicKey) {
		fTokenMap.put(colorKey, new Token(createTextAttribute(colorKey, boldKey, italicKey)));
	}

	private int indexOf(String property) {
		if (property != null) {
			int length= fPropertyNamesColor.length;
			for (int i= 0; i < length; i++) {
				if (property.equals(fPropertyNamesColor[i]) || property.equals(fPropertyNamesBold[i]) || property.equals(fPropertyNamesItalic[i]))
					return i;
			}
		}
		return -1;
	}
	
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return indexOf(event.getProperty()) >= 0;
	}
	
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		String p= event.getProperty();
		int index= indexOf(p);
		Token token= getToken(fPropertyNamesColor[index]);
		if (fPropertyNamesColor[index].equals(p))
			adaptToColorChange(event, token);
		else if (fPropertyNamesBold[index].equals(p))
			adaptToStyleChange(event, token, SWT.BOLD);
		else if (fPropertyNamesItalic[index].equals(p))
			adaptToStyleChange(event, token, SWT.ITALIC);
	}

	protected void adaptToColorChange(PropertyChangeEvent event, Token token) {
		RGB rgb= null;
		Object value= event.getNewValue();
		if (value instanceof RGB) {
			rgb= (RGB) value;
		} else if (value instanceof String) {
			rgb= StringConverter.asRGB((String) value);
		}
			
		if (rgb != null) {
			TextAttribute attr= (TextAttribute) token.getData();
			token.setData(new TextAttribute(ColorManager.getDefault().getColor(rgb), attr.getBackground(), attr.getStyle()));
		}
	}

	protected void adaptToStyleChange(PropertyChangeEvent event, Token token, int styleAttribute) {
	 	if (token == null) {
			return;
		}
		boolean eventValue= false;
		Object value= event.getNewValue();
		if (value instanceof Boolean) {
			eventValue= ((Boolean) value).booleanValue();
		} else if (IPreferenceStore.TRUE.equals(value)) {
			eventValue= true;
		}
		
		TextAttribute attr= (TextAttribute) token.getData();
		boolean activeValue= (attr.getStyle() & styleAttribute) == styleAttribute;
		if (activeValue != eventValue) { 
			token.setData(new TextAttribute(attr.getForeground(), attr.getBackground(), eventValue ? attr.getStyle() | styleAttribute : attr.getStyle() & ~styleAttribute));
		}
	}

	protected TextAttribute createTextAttribute(String colorID, String boldKey, String italicKey) {
		Color color= null;
		if (colorID != null) {
			color= MakeUIPlugin.getPreferenceColor(colorID);
		}
		IPreferenceStore store= MakeUIPlugin.getDefault().getPreferenceStore();
		int style= store.getBoolean(boldKey) ? SWT.BOLD : SWT.NORMAL;
		if (store.getBoolean(italicKey)) {
			style |= SWT.ITALIC;
		}		
		return new TextAttribute(color, null, style);
	}
}
