/*******************************************************************************
 * Copyright (c) 2006 Red Hat, Inc., (c) 2008 NOKIA Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *     Ed Swartz (NOKIA) - updates
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.internal.autotools.ui.preferences.AutotoolsEditorPreferenceConstants;
import org.eclipse.cdt.internal.autotools.ui.preferences.ColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;


public class AutoconfCodeScanner extends RuleBasedScanner {

	private Map<String, IToken> fTokenMap= new HashMap<String, IToken>();
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

	private static String[] keywords = { 
		"case", "do", "done", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
		"esac",	"if", "elif", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		"else",	"fi", "for",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		"in", "then" };       //$NON-NLS-1$ //$NON-NLS-2$

	static final String[] fTokenProperties = new String[] {
		ColorManager.AUTOCONF_COMMENT_COLOR,
		ColorManager.AUTOCONF_KEYWORD_COLOR,
		ColorManager.AUTOCONF_ACMACRO_COLOR,
		ColorManager.AUTOCONF_AMMACRO_COLOR,
		ColorManager.AUTOCONF_VAR_REF_COLOR,
		ColorManager.AUTOCONF_VAR_SET_COLOR,
		ColorManager.AUTOCONF_CODESEQ_COLOR,
		ColorManager.AUTOCONF_DEFAULT_COLOR,
	};

	public AutoconfCodeScanner() {
		
		initialize();
		
		IToken other= getToken(ColorManager.AUTOCONF_DEFAULT_COLOR);
		IToken keyword = getToken(ColorManager.AUTOCONF_KEYWORD_COLOR);
		IToken comment= getToken(ColorManager.AUTOCONF_COMMENT_COLOR);
		IToken string = getToken(ColorManager.AUTOCONF_DEFAULT_COLOR);
		IToken varRef = getToken(ColorManager.AUTOCONF_VAR_REF_COLOR);
		IToken acmacro = getToken(ColorManager.AUTOCONF_ACMACRO_COLOR);
		IToken ammacro = getToken(ColorManager.AUTOCONF_AMMACRO_COLOR);
		IToken code = getToken(ColorManager.AUTOCONF_CODESEQ_COLOR);
		
		List<IRule> rules= new ArrayList<IRule>();

		// Add rule for single line comments.
		rules.add(new EndOfLineRule("dnl", comment)); //$NON-NLS-1$
		rules.add(new EndOfLineRule("#", comment, '\\')); //$NON-NLS-1$
		
		// Add special recursive rule for strings which allows variable
		// references to be internally tokenized.
		RecursiveSingleLineRule stringRule =
			new RecursiveSingleLineRule("\"", "\"", string, '\\'); //$NON-NLS-1$ //$NON-NLS-2$
		stringRule.addRule(new SingleLineRule("${", "}", varRef)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(stringRule);
		
		// Add rule for variable references
		rules.add(new SingleLineRule("${", "}", varRef)); //$NON-NLS-1$ //$NON-NLS-2$
		// Add rule for strings
		rules.add(new SingleLineRule("\"", "\"", string, '\\')); //$NON-NLS-1$ //$NON-NLS-2$

		// Add rule for PKG_ macros
		rules.add(new AutoconfMacroRule("PKG_", new AutoconfPKGWordDetector(), acmacro)); //$NON-NLS-1$

		// Add rule for AC_ macros
		rules.add(new AutoconfMacroRule("AC_", new AutoconfMacroWordDetector(), acmacro)); //$NON-NLS-1$

		// Add rule for AM_ macros
		rules.add(new AutoconfMacroRule("AM_", new AutoconfMacroWordDetector(), ammacro)); //$NON-NLS-1$

		// Add rule for m4_ macros
		rules.add(new AutoconfMacroRule("m4_", new AutoconfM4WordDetector(), acmacro)); //$NON-NLS-1$
		
		// Add rule for code sequences starting with <<EOF and ending with EOF
		rules.add(new InlineDataRule(code));
		
		// Add word rule for keywords.
		WordRule wordRule= new WordRule(new AutoconfWordDetector(), Token.UNDEFINED);
		for (int i= 0; i < keywords.length; i++)
			wordRule.addWord(keywords[i], keyword);
		rules.add(wordRule);
		
		// Add word rule for identifier.
		rules.add(new AutoconfIdentifierRule(other));
		
		// Make sure we don't treat "\#" as comment start.
		rules.add(new SingleLineRule("\\#", null, Token.UNDEFINED));
		
		rules.add(new WhitespaceRule(new AutoconfWhitespaceDetector()));
		
		setDefaultReturnToken(other);

		IRule[] result= new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
	}
	
	protected Token getToken(String key) {
		return (Token) fTokenMap.get(key);
	}

	private void addToken(String colorKey, String boldKey, String italicKey) {
		fTokenMap.put(colorKey, new Token(createTextAttribute(colorKey, boldKey, italicKey)));
	}

	protected String[] getTokenProperties() {
		return fTokenProperties;
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
	
	/*
	 * @see ITokenScanner#nextToken()
	 */
	public IToken nextToken() {
		return super.nextToken();
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
			color= AutoconfEditor.getPreferenceColor(colorID);
		}
		IPreferenceStore store= AutotoolsPlugin.getDefault().getPreferenceStore();
		int style= store.getBoolean(boldKey) ? SWT.BOLD : SWT.NORMAL;
		if (store.getBoolean(italicKey)) {
			style |= SWT.ITALIC;
		}		
		return new TextAttribute(color, null, style);
	}
	
	/**
	 * Must be called after the constructor has been called.
	 */
	public final void initialize() {
		
		fPropertyNamesColor= getTokenProperties();
		int length= fPropertyNamesColor.length;
		fPropertyNamesBold= new String[length];
		fPropertyNamesItalic= new String[length];

		for (int i= 0; i < length; i++) {
			fPropertyNamesBold[i]= fPropertyNamesColor[i] + AutotoolsEditorPreferenceConstants.EDITOR_BOLD_SUFFIX;
			fPropertyNamesItalic[i]= fPropertyNamesColor[i] + AutotoolsEditorPreferenceConstants.EDITOR_ITALIC_SUFFIX;
			addToken(fPropertyNamesColor[i], fPropertyNamesBold[i], fPropertyNamesItalic[i]);
		}
	}

	/*
	 * @see ICharacterScanner#unread()
	 */
	public void unread() {
	    	--fOffset;
	    	fColumn = UNDEFINED;
	}
}
