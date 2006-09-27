/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.ui.PreferenceConstants;


/**
 * Initialized with a color manager and a preference store, its subclasses are
 * only responsible for providing a list of preference keys for based on which tokens
 * are generated and to use this tokens to define the rules controlling this scanner.
 * <p>
 * This scanner stores the color defined by the color preference key into
 * the color manager under the same key.
 * </p>
 * <p>
 * Preference color key + {@link PreferenceConstants#EDITOR_BOLD_SUFFIX} are used
 * to retrieve whether the token is rendered in bold.
 * </p>
 * <p>
 * Preference color key + {@link PreferenceConstants#EDITOR_ITALIC_SUFFIX} are used
 * to retrieve whether the token is rendered in italic.
 * </p>
 * <p>
 * Preference color key + {@link PreferenceConstants#EDITOR_STRIKETHROUGH_SUFFIX} are used
 * to retrieve whether the token is rendered in strikethrough.
 * </p>
 * <p>
 * Preference color key + {@link PreferenceConstants#EDITOR_UNDERLINE_SUFFIX} are used
 * to retrieve whether the token is rendered in underline.
 * </p>
 */
public abstract class AbstractCScanner extends BufferedRuleBasedScanner {


	private IColorManager fColorManager;
	private IPreferenceStore fPreferenceStore;

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
	 *
	 * @since 4.0
	 */
	private String[] fPropertyNamesItalic;
	/**
	 * Preference keys for boolean preferences which are <code>true</code>,
	 * iff the corresponding token should be rendered strikethrough.
	 *
	 * @since 4.0
	 */
	private String[] fPropertyNamesStrikethrough;
	/**
	 * Preference keys for boolean preferences which are <code>true</code>,
	 * iff the corresponding token should be rendered underline.
	 *
	 * @since 4.0
	 */
	private String[] fPropertyNamesUnderline;


	private boolean fNeedsLazyColorLoading;

	/**
	 * Returns an array of preference keys which define the tokens
	 * used in the rules of this scanner.
	 * <p>
	 * The preference key is used access the color in the preference
	 * store and in the color manager.
	 * </p>
	 * <p>
	 * Preference key + {@link PreferenceConstants#EDITOR_BOLD_SUFFIX} is used
	 * to retrieve whether the token is rendered in bold.
	 * </p>
	 * <p>
	 * Preference key + {@link PreferenceConstants#EDITOR_ITALIC_SUFFIX} is used
	 * to retrieve whether the token is rendered in italic.
	 * </p>
	 * <p>
	 * Preference key + {@link PreferenceConstants#EDITOR_UNDERLINE_SUFFIX} is used
	 * to retrieve whether the token is rendered underlined.
	 * </p>
	 * <p>
	 * Preference key + {@link PreferenceConstants#EDITOR_STRIKETHROUGH_SUFFIX} is used
	 * to retrieve whether the token is rendered stricken out.
	 * </p>
	 */
	abstract protected String[] getTokenProperties();

	/**
	 * Creates the list of rules controlling this scanner.
	 */
	abstract protected List createRules();


	/**
	 * Creates an abstract C scanner.
	 */
	public AbstractCScanner(IColorManager manager, IPreferenceStore store) {
		super();
		fColorManager= manager;
		fPreferenceStore= store;
	}

	/**
	 * Creates an abstract C scanner.
	 */
	public AbstractCScanner(IColorManager manager, IPreferenceStore store, int bufsize) {
		super(bufsize);
		fColorManager= manager;
		fPreferenceStore= store;
	}

	/**
	 * Must be called after the constructor has been called.
	 */
	public final void initialize() {

		fPropertyNamesColor= getTokenProperties();
		int length= fPropertyNamesColor.length;
		fPropertyNamesBold= new String[length];
		fPropertyNamesItalic= new String[length];
		fPropertyNamesStrikethrough= new String[length];
		fPropertyNamesUnderline= new String[length];

		for (int i= 0; i < length; i++) {
			fPropertyNamesBold[i]= getBoldKey(fPropertyNamesColor[i]);
			fPropertyNamesItalic[i]= getItalicKey(fPropertyNamesColor[i]);
			fPropertyNamesStrikethrough[i]= getStrikethroughKey(fPropertyNamesColor[i]);
			fPropertyNamesUnderline[i]= getUnderlineKey(fPropertyNamesColor[i]);
		}
		
		fNeedsLazyColorLoading= Display.getCurrent() == null;
		for (int i= 0; i < length; i++) {
			if (fNeedsLazyColorLoading)
				addTokenWithProxyAttribute(fPropertyNamesColor[i], fPropertyNamesBold[i], fPropertyNamesItalic[i], fPropertyNamesStrikethrough[i], fPropertyNamesUnderline[i]);
			else
				addToken(fPropertyNamesColor[i], fPropertyNamesBold[i], fPropertyNamesItalic[i], fPropertyNamesStrikethrough[i], fPropertyNamesUnderline[i]);
		}

		initializeRules();
	}
	
	protected String getBoldKey(String colorKey) {
		return colorKey + PreferenceConstants.EDITOR_BOLD_SUFFIX;
	}

	protected String getItalicKey(String colorKey) {
		return colorKey + PreferenceConstants.EDITOR_ITALIC_SUFFIX;
	}
	
	protected String getStrikethroughKey(String colorKey) {
		return colorKey + PreferenceConstants.EDITOR_STRIKETHROUGH_SUFFIX;
	}
	
	protected String getUnderlineKey(String colorKey) {
		return colorKey + PreferenceConstants.EDITOR_UNDERLINE_SUFFIX;
	}
	
	public IToken nextToken() {
		if (fNeedsLazyColorLoading)
			resolveProxyAttributes();
		return super.nextToken();
	}

	private void resolveProxyAttributes() {
		if (fNeedsLazyColorLoading && Display.getCurrent() != null) {
			for (int i= 0; i < fPropertyNamesColor.length; i++) {
				addToken(fPropertyNamesColor[i], fPropertyNamesBold[i], fPropertyNamesItalic[i], fPropertyNamesStrikethrough[i], fPropertyNamesUnderline[i]);
			}
			fNeedsLazyColorLoading= false;
		}
	}

	private void addTokenWithProxyAttribute(String colorKey, String boldKey, String italicKey, String strikethroughKey, String underlineKey) {
		fTokenMap.put(colorKey, new Token(createTextAttribute(null, boldKey, italicKey, strikethroughKey, underlineKey)));
	}

	private void addToken(String colorKey, String boldKey, String italicKey, String strikethroughKey, String underlineKey) {
		if (fColorManager != null && colorKey != null && fColorManager.getColor(colorKey) == null) {
			RGB rgb= PreferenceConverter.getColor(fPreferenceStore, colorKey);
			if (fColorManager instanceof IColorManagerExtension) {
				IColorManagerExtension ext= (IColorManagerExtension) fColorManager;
				ext.unbindColor(colorKey);
				ext.bindColor(colorKey, rgb);
			}
		}

		if (!fNeedsLazyColorLoading)
			fTokenMap.put(colorKey, new Token(createTextAttribute(colorKey, boldKey, italicKey, strikethroughKey, underlineKey)));
		else {
			Token token= ((Token)fTokenMap.get(colorKey));
			if (token != null)
				token.setData(createTextAttribute(colorKey, boldKey, italicKey, strikethroughKey, underlineKey));
		}
	}

	/**
	 * Create a text attribute based on the given color, bold, italic, strikethrough and underline preference keys.
	 *
	 * @param colorKey the color preference key
	 * @param boldKey the bold preference key
	 * @param italicKey the italic preference key
	 * @param strikethroughKey the strikethrough preference key
	 * @param underlineKey the italic preference key
	 * @return the created text attribute
	 * @since 3.0
	 */
	private TextAttribute createTextAttribute(String colorKey, String boldKey, String italicKey, String strikethroughKey, String underlineKey) {
		Color color= null;
		if (colorKey != null)
			color= fColorManager.getColor(colorKey);

		int style= fPreferenceStore.getBoolean(boldKey) ? SWT.BOLD : SWT.NORMAL;
		if (fPreferenceStore.getBoolean(italicKey))
			style |= SWT.ITALIC;

		if (fPreferenceStore.getBoolean(strikethroughKey))
			style |= TextAttribute.STRIKETHROUGH;

		if (fPreferenceStore.getBoolean(underlineKey))
			style |= TextAttribute.UNDERLINE;

		return new TextAttribute(color, null, style);
	}

	protected Token getToken(String key) {
		if (fNeedsLazyColorLoading)
			resolveProxyAttributes();
		return (Token) fTokenMap.get(key);
	}

	private void initializeRules() {
		List rules= createRules();
		if (rules != null) {
			IRule[] result= new IRule[rules.size()];
			rules.toArray(result);
			setRules(result);
		}
	}

	private int indexOf(String property) {
		if (property != null) {
			int length= fPropertyNamesColor.length;
			for (int i= 0; i < length; i++) {
				if (property.equals(fPropertyNamesColor[i]) || property.equals(fPropertyNamesBold[i]) || property.equals(fPropertyNamesItalic[i]) || property.equals(fPropertyNamesStrikethrough[i]) || property.equals(fPropertyNamesUnderline[i]))
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
			adaptToColorChange(token, event);
		else if (fPropertyNamesBold[index].equals(p))
			adaptToStyleChange(token, event, SWT.BOLD);
		else if (fPropertyNamesItalic[index].equals(p))
			adaptToStyleChange(token, event, SWT.ITALIC);
		else if (fPropertyNamesStrikethrough[index].equals(p))
			adaptToStyleChange(token, event, TextAttribute.STRIKETHROUGH);
		else if (fPropertyNamesUnderline[index].equals(p))
			adaptToStyleChange(token, event, TextAttribute.UNDERLINE);
	}

	private void adaptToColorChange(Token token, PropertyChangeEvent event) {
		RGB rgb= null;

		Object value= event.getNewValue();
		if (value instanceof RGB)
			rgb= (RGB) value;
		else if (value instanceof String)
			rgb= StringConverter.asRGB((String) value);

		if (rgb != null) {

			String property= event.getProperty();
			Color color= fColorManager.getColor(property);

			if ((color == null || !rgb.equals(color.getRGB())) && fColorManager instanceof IColorManagerExtension) {
				IColorManagerExtension ext= (IColorManagerExtension) fColorManager;

			 	ext.unbindColor(property);
			 	ext.bindColor(property, rgb);

				color= fColorManager.getColor(property);
			}

			Object data= token.getData();
			if (data instanceof TextAttribute) {
				TextAttribute oldAttr= (TextAttribute) data;
				token.setData(new TextAttribute(color, oldAttr.getBackground(), oldAttr.getStyle()));
			}
		}
	}

	private void adaptToStyleChange(Token token, PropertyChangeEvent event, int styleAttribute) {
		boolean eventValue= false;
		Object value= event.getNewValue();
		if (value instanceof Boolean)
			eventValue= ((Boolean) value).booleanValue();
		else if (IPreferenceStore.TRUE.equals(value))
			eventValue= true;

		Object data= token.getData();
		if (data instanceof TextAttribute) {
			TextAttribute oldAttr= (TextAttribute) data;
			boolean activeValue= (oldAttr.getStyle() & styleAttribute) == styleAttribute;
			if (activeValue != eventValue)
				token.setData(new TextAttribute(oldAttr.getForeground(), oldAttr.getBackground(), eventValue ? oldAttr.getStyle() | styleAttribute : oldAttr.getStyle() & ~styleAttribute));
		}
	}
	/**
	 * Returns the preference store.
	 *
	 * @return the preference store.
	 *
	 * @since 3.0
	 */
	protected IPreferenceStore getPreferenceStore() {
		return fPreferenceStore;
	}
}
