/********************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [187860] review for adding foreign lang support
 * David Dykstal (IBM) - [226561] Add API markup for noextend / noimplement where needed
 * Kevin Doyle 	 (IBM) - [242250] Using Mnemonics twice as fast as we should be
 * David McKnight (IBM) - [431291] RSE dialogs missing some mnemonics
 ********************************************************************************/

package org.eclipse.rse.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.rse.ui.widgets.InheritableEntryField;
import org.eclipse.rse.ui.widgets.SystemHistoryCombo;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UCharacter.UnicodeBlock;
import com.ibm.icu.util.ULocale;

/**
 * Instances of this class may be used to supply mnemonics to the
 * text for controls.
 * There are preferences which can be set by products to control how
 * these mnemonics are generated and applied.
 * <p>
 * There are two types of mnemonics which can be added to a label:
 * embedded mnemonics and appended mnemonics. An embedded mnemonic uses
 * an existing letter in the label for the mnemonic. An appended mnemonic
 * is added to the end of the label (but prior to any punctuation or accelerators)
 * and is of the form (X).
 * <p>
 * The org.eclipse.rse.ui/MNEMONICS_POLICY preference establishes the
 * desire to generated embedded mnemonics using letters that already
 * exist in the text of the controls and/or to generate appended mnemonics
 * if an embedded mnemonic cannot be found or is not desired.
 * The policy is composed of bit flags.
 * See {@link #EMBED_MNEMONICS} and {@link #APPEND_MNEMONICS} for the flag values.
 * See {@link #POLICY_DEFAULT} for the default policy value.
 * A policy value of 0 will disable the generation of all mnemonics.
 * <p>
 * The org.eclipse.rse.ui/APPEND_MNEMONICS_PATTERN preference is used to
 * further qualify the appending behavior by the current locale. If the
 * current locale name matches this pattern then appending can be performed.
 * See {@link #APPEND_MNEMONICS_PATTERN_DEFAULT} for the default pattern.
 * <p>
 * Mnemonics on menus are allowed to have duplicates. Attempts are made to find the
 * least used mnemonic when finding a duplicate.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Mnemonics {

	/**
	 * An option bit mask - value 1.
	 * If on, specifies whether or not to insert mnemonic indications into
	 * the current text of a label.
	 * If off, all other options are ignored.
	 */
	public static final int EMBED_MNEMONICS = 1;

	/**
	 * An option bit mask - value 2.
	 * If on, specifies to generate mnemonics of the form (X) at the end of labels for
	 * those languages matching the locale pattern.
	 * If off, then only characters from the label will be used as mnemonics.
	 */
	public static final int APPEND_MNEMONICS = 2;

	/**
	 * The simple name of the preference that holds the pattern  to be used for matching
	 * against the locale to determine if APPEND_MNEMONICS option applies.
	 */
	public static final String APPEND_MNEMONICS_PATTERN_PREFERENCE = "APPEND_MNEMONICS_PATTERN"; //$NON-NLS-1$

	/**
	 * Some products will to append mnemonics only for certain locales.
	 * The following provides the default pattern for matching the locale.
	 * The default pattern matches Chinese, Japanese, and Korean.
	 */
	public static final String APPEND_MNEMONICS_PATTERN_DEFAULT = "zh.*|ja.*|ko.*"; //$NON-NLS-1$

	/**
	 * The simple name of the preference that determines the policy to be  used when applying mnemonics to menus and composites.
	 */
	public static final String POLICY_PREFERENCE = "MNEMONICS_POLICY"; //$NON-NLS-1$

	/**
	 * The default mnemonics policy. If no policy is specified in a call to generate
	 * mnemonics this policy will be used. Can be overridden by the
	 * org.eclipse.rse.ui/MNEMONICS_POLICY preference.
	 */
	public static final int POLICY_DEFAULT = EMBED_MNEMONICS | APPEND_MNEMONICS;

	private static final char MNEMONIC_CHAR = '&';

	/*
	 * Interesting ISO 639-1 language codes.
	 */
	private static final String LC_GREEK = "el"; //$NON-NLS-1$
	private static final String LC_RUSSIAN = "ru"; //$NON-NLS-1$

	/*
	 * Known valid mnemonic candidates
	 */
	private static final String GREEK_MNEMONICS = "\u0391\u0392\u0393\u0394\u0395\u0396\u0397\u0398\u0399\u039a\u039b\u039c\u039d\u039e\u039f\u03a0\u03a1\u03a3\u03a4\u03a5\u03a6\u03a7\u03a8\u03a9"; //$NON-NLS-1$
	private static final String RUSSIAN_MNEMONICS = "\u0410\u0411\u0412\u0413\u0414\u0145\u0401\u0416\u0417\u0418\u0419\u041a\u041b\u041c\u041d\u041e\u041f\u0420\u0421\u0422\u0423\u0424\u0425\u0426\u0427\u0428\u0429\u042a\u042b\u042c\u042d\u042e\u042f"; //$NON-NLS-1$
	private static final String LATIN_MNEMONICS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; //$NON-NLS-1$

	private final static Pattern TRANSPARENT_ENDING_PATTERN = Pattern.compile("(\\s|\\.\\.\\.|>|<|:|\uff0e\uff0e\uff0e|\uff1e|\uff1c|\uff1a|\\t.*)+$"); //$NON-NLS-1$
	private boolean applyMnemonicsToPrecedingLabels = true;

	private Map usedCharacters = new HashMap();

	/**
	 * Helper method to return the mnemonic from a string.
	 * Helpful when it is necessary to know the mnemonic assigned so it can be reassigned,
	 *  such as is necessary for buttons which toggle their text.
	 * @param text the label from which to extract the mnemonic
	 * @return the mnemonic if assigned, else a blank character.
	 */
	public static char getMnemonic(String text) {
		int idx = text.indexOf(MNEMONIC_CHAR);
		if (idx >= 0 && idx < (text.length() - 1))
			return text.charAt(idx + 1);
		else
			return ' ';
	}

	/**
	 * Given a label and mnemonic, this applies that mnemonic to the label.
	 * Not normally called from other classes, but rather by the setUniqueMnemonic
	 * methods in this class.
	 * @param label String to which to apply the mnemonic
	 * @param mnemonicChar the character that is to be the mnemonic character
	 * @return input String with '&' inserted in front of the given character,
	 *     or with "(c)" appended to the label at a proper position in case the
	 *     character c is not part of the label.
	 */
	public static String applyMnemonic(String label, char mnemonicChar) {
		int labelLen = label.length();
		if (labelLen == 0) return label;
		StringBuffer newLabel = new StringBuffer(label);
		int mcharPos = label.indexOf(mnemonicChar);
		if (mcharPos != -1)
			newLabel.insert(mcharPos, MNEMONIC_CHAR);
		else {
			String addedMnemonic = new String("(" + MNEMONIC_CHAR + mnemonicChar + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			int p = getEndingPosition(label);
			newLabel.insert(p, addedMnemonic);
		}
		return newLabel.toString();
	}

	/**
	 * Helper method to strip the mnemonic from a string.
	 * Useful if using Eclipse supplied labels.
	 * @param text the label from which to strip the mnemonic
	 * @return the label with the mnemonic stripped
	 */
	public static String removeMnemonic(String text) {
		String[] parts = text.split("\\(\\&.\\)", 2); //$NON-NLS-1$
		if (parts.length == 1) {
			parts = text.split("\\&", 2); //$NON-NLS-1$
		}
		if (parts.length == 2) {
			text = parts[0] + parts[1];
		}
		return text;
	}

	/**
	 * Finds the point at which to insert a mnemonic of the form (&X).
	 * Checks for transparent endings.
	 * @param label the label to check
	 * @return the position at which a mnemonic of the form (&X) can be inserted.
	 */
	private static int getEndingPosition(String label) {
		Matcher m = TRANSPARENT_ENDING_PATTERN.matcher(label);
		int result = m.find() ? m.start() : label.length();
		return result;
	}

	/**
	 * Clear the list of used mnemonic characters
	 */
	public void clear() {
		usedCharacters.clear();
	}

	/**
	 * Resets the list of used mnemonic characters to those in the string.
	 * 
	 * @param usedMnemonics A String listing the characters to mark used as
	 *            mnemonics. Each character will be considered in a case
	 *            insensitive manner.
	 */
	public void clear(String usedMnemonics) {
		clear();
		makeUsed(usedMnemonics);
	}

	/**
	 * Sets a mnemonic in the given string and returns the result.
	 * Functions according to the default policy as specified in
	 * Sets the mnemonic according to the org.eclipse.rse.ui/MNEMONICS_POLICY preference.
	 * Not normally called from other classes, but rather by the setMnemonic
	 * methods in this class.
	 * @param label The string to which to apply the mnemonic
	 * @return the result string with the mnemonic embedded or appended
	 */
	public String setUniqueMnemonic(String label) {
		Plugin plugin = RSEUIPlugin.getDefault();
		Preferences preferences = plugin.getPluginPreferences();
		int flags = preferences.getInt(POLICY_PREFERENCE);
		String localePattern = preferences.getString(APPEND_MNEMONICS_PATTERN_PREFERENCE);
		String result = setUniqueMnemonic(label, flags, localePattern, false);
		return result;
	}

	/**
	 * Given a string, this starts at the first character and iterates until
	 * it finds a character not already used as a mnemonic.
	 * Not normally called from other classes, but rather by the setMnemonic
	 * methods in this class.
	 * If the label already has a mnemonic it is not touched.
	 * @param label String to which to apply a mnemonic
	 * @param flags The policy bit field composed of the following options
	 * EMBED_MNEMONICS and APPEND_MNEMONICS
	 * @param allowDuplicates true if duplicates can be allowed. Typically used only
	 * when assigning mnemonics to menu items. If true, it will attempt to assign the
	 * least used duplicate mnemonic for the string from the context established so far.
	 * @return input String with '&' inserted in front of the mnemonic character
	 */
	private String setUniqueMnemonic(String label, int flags, String localePattern, boolean allowDuplicates) {
		// determine the cases where the label does not need a mnemonic
		if (flags == 0 || label == null || label.trim().length() == 0 || label.equals("?")) { //$NON-NLS-1$
			return label;
		}
		StringBuffer buffer = new StringBuffer(label);
		char mn = getMnemonic(label);
		if (mn == ' ' && ((flags & EMBED_MNEMONICS) > 0)) { // no mnemonic exists, try embedding
			int p = findUniqueMnemonic(label);
			if (p >= 0) { // a character in the label can be used as the mnemonic
				mn = label.charAt(p);
				buffer.insert(p, MNEMONIC_CHAR);
			}
		}
		if (mn == ' ' && allowDuplicates) { // no mnemonic exists, try a duplicate
			int n = getEndingPosition(label);
			int m = 999;
			int p = -1;
			for (int i = 0; i < n; i++) {
				char ch = label.charAt(i);
				if (isAcceptable(ch) && timesUsed(ch) < m) {
					m = timesUsed(ch);
					p = i;
				}
			}
			if (p >= 0) {
				mn = label.charAt(p);
				buffer.insert(p, MNEMONIC_CHAR);
			}
		}
		if (mn == ' ' && ((flags & APPEND_MNEMONICS) > 0)) { // no mnemonic exists, try appending a mnemonic
			String localeName = ULocale.getDefault().getName();
			if (localeName.matches(localePattern)) {
				String candidates = getCandidates();
				int p = findUniqueMnemonic(candidates);
				if (p >= 0) {
					mn = candidates.charAt(p);
					String mnemonicString = "(" + MNEMONIC_CHAR + mn + ")"; //$NON-NLS-1$ //$NON-NLS-2$
					p = getEndingPosition(label);
					buffer.insert(p, mnemonicString);
				}
			}
		}
		makeUsed(mn);
		return buffer.toString();
	}

	/**
	 * Determine if given char is a unique mnemonic.
	 * @param ch the character to test.
	 * @return true if the character has not yet been used.
	 */
	public boolean isUniqueMnemonic(char ch) {
		return timesUsed(ch) == 0;
	}

	private Preferences getPreferences() {
		return RSEUIPlugin.getDefault().getPluginPreferences();
	}

	private String getLocalePattern() {
		return getPreferences().getString(APPEND_MNEMONICS_PATTERN_PREFERENCE);
	}

	private int getPolicy() {
		return getPreferences().getInt(POLICY_PREFERENCE);
	}

	private boolean isEmbedding() {
		return (getPolicy() & EMBED_MNEMONICS) > 0;
	}

	private boolean isAppending() {
		return (getPolicy() & APPEND_MNEMONICS) > 0;
	}

	/**
	 * @return a string of acceptable mnemonic candidates for the language
	 * of the current locale.
	 */
	private String getCandidates() {
		/*
		 * This is a coarse-grained approach and uses the 2-letter language codes from ISO 639-1.
		 * This should be quite sufficient for mnemonic generation.
		 */
		String language = ULocale.getDefault().getLanguage();
		if (language.equals(LC_GREEK)) return GREEK_MNEMONICS;
		if (language.equals(LC_RUSSIAN)) return RUSSIAN_MNEMONICS;
		return LATIN_MNEMONICS;
	}

	/**
	 * Find a unique mnemonic char in given string.
	 * @param label the string in which to search for the best mnemonic character
	 * @return index position of unique character in input string, or -1 if none found.
	 */
	private int findUniqueMnemonic(String label) {
		int uniqueIndex = -1;
		char ch = label.charAt(0);
		for (int i = 0; (i < label.length()) && (uniqueIndex == -1); i++) {
			ch = label.charAt(i);
			if (ch == '\t') { // stop at accelerators too
				break;
			}
			if (timesUsed(ch) == 0 && isAcceptable(ch)) {
				uniqueIndex = i;
			}
		}
		return uniqueIndex;
	}

	private boolean isAcceptable(char ch) {
		UnicodeBlock block = UnicodeBlock.of(ch);
		boolean result = (isAcceptable(block) && UCharacter.isLetter(ch)); // the character is a letter
		return result;
	}

	private boolean isAcceptable(UnicodeBlock block) {
		if (block == UnicodeBlock.BASIC_LATIN) return true;
		if (block == UnicodeBlock.LATIN_1_SUPPLEMENT) return true;
		if (block == UnicodeBlock.LATIN_EXTENDED_A) return true;
		if (block == UnicodeBlock.LATIN_EXTENDED_B) return true;
		if (block == UnicodeBlock.LATIN_EXTENDED_C) return true;
		if (block == UnicodeBlock.LATIN_EXTENDED_D) return true;
		if (block == UnicodeBlock.GREEK) return true;
		if (block == UnicodeBlock.CYRILLIC) return true;
		if (block == UnicodeBlock.HEBREW) return true;
		if (block == UnicodeBlock.ARABIC) return true;
		return false;
	}

	/**
	 * Returns the number of times a given character is used as a mnemonic in this
	 * context.
	 * @param ch the character to examine
	 * @return the number of times it has been reported as being used.
	 */
	private int timesUsed(char ch) {
		// TODO if we are guaranteed java 1.5 we can use Character.valueOf(ch)
		int result = 0;
		Integer count = (Integer) usedCharacters.get(new Character(ch));
		if (count != null) {
			result = count.intValue();
		}
		return result;
	}

	private void makeUsed(char ch) {
		// TODO if we are guaranteed java 1.5 we can use Character.valueOf(ch)
		if (ch != ' ') {
			makeUsed(new Character(Character.toLowerCase(ch)));
			makeUsed(new Character(Character.toUpperCase(ch)));
		}
	}

	private void makeUsed(Character ch) {
		Integer count = (Integer) usedCharacters.get(ch);
		if (count == null) {
			count = new Integer(1);
		}
		usedCharacters.put(ch, count);
	}

	private void makeUsed(String s) {
		for (int i = 0; i < s.length(); i++) {
			makeUsed(s.charAt(i));
		}
	}

	/**
	 * Returns a string with the mnemonics for a given array of strings.
	 * @param strings the array of strings.
	 * @return a string containing the mnemonics.
	 */
	private String getMnemonicsFromStrings(String[] strings) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < strings.length; i++) {
			int idx = strings[i].indexOf(MNEMONIC_CHAR);
			if (idx != -1) {
				result.append(strings[i].charAt(idx + 1));
			}
		}
		return result.toString();
	}

	/**
	 * Set whether to apply mnemonics to labels preceding text fields, combos and inheritable entry fields.
	 * This is for consistency with Eclipse. Only set to <code>false</code> if it does not work
	 * in your dialog, wizard, preference or property page, i.e. you have labels preceding these
	 * widgets that do not necessarily refer to them.
	 * @param apply <code>true</code> to apply mnemonic to preceding labels, <code>false</code> otherwise.
	 * @return this instance, for convenience
	 */
	public Mnemonics setApplyMnemonicsToPrecedingLabels(boolean apply) {
		this.applyMnemonicsToPrecedingLabels = apply;
		return this;
	}

	/**
	 * Adds a mnemonic to an SWT Button such that the user can select it via Ctrl/Alt+mnemonic.
	 * Note a mnemonic unique to this window is chosen.
	 * @param button the button to equip with a mnemonic
	 * @return <code>true</code> if the button was actually changed
	 */
	public boolean setMnemonic(Button button) {
		boolean changed = false;
		String text = button.getText();
		if ((text != null) && (text.trim().length() > 0)) {
			String newText = setUniqueMnemonic(text);
			if (!text.equals(newText)) {
				button.setText(newText);
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * Given a menu, this method walks all the items and assigns each a mnemonic.
	 * Note that menu item mnemonics do not have to be unique.
	 * The mnemonics used on cascaded menus are independent of those of the parent.
	 * Handles cascading menus.
	 * Call this after populating the menu.
	 * @param menu the menu to examine
	 */
	public void setMnemonics(Menu menu) {
		// this set will contain menu items without mnemonics in order of length of their text
		Collection embeddingItems = new TreeSet(new Comparator() {
			public int compare(Object o1, Object o2) {
				String t1 = ((MenuItem) o1).getText();
				String t2 = ((MenuItem) o2).getText();
				int l1 = getEndingPosition(t1);
				int l2 = getEndingPosition(t2);
				if (l1 < l2) return -1;
				if (l1 > l2) return 1;
				return t1.compareTo(t2);
			}
		});
		Collection appendingItems = new ArrayList(10);
		// handle cascades, populate the set, record existing mnemonics
		MenuItem[] menuItems = menu.getItems();
		for (int i = 0; i < menuItems.length; i++) {
			MenuItem menuItem = menuItems[i];
			Menu cascade = menuItem.getMenu();
			if (cascade != null) {
				Mnemonics context = new Mnemonics();
				context.setMnemonics(cascade);
			}
			String text = menuItem.getText();
			if (text.length() > 0) {
				char ch = getMnemonic(text);
				if (ch == ' ') {
					embeddingItems.add(menuItem);
					appendingItems.add(menuItem);
				} else {
					makeUsed(ch);
				}
			}
		}
		// assign mnemonics to the items of the set
		String localePattern = getLocalePattern();
		if (isEmbedding()) {
			processMenuItems(embeddingItems, EMBED_MNEMONICS, localePattern);
		}
		if (isAppending()) {
			processMenuItems(appendingItems, APPEND_MNEMONICS, localePattern);
		}
	}

	private void processMenuItems(Collection collection, int flags, String localePattern) {
		for (Iterator z = collection.iterator(); z.hasNext();) {
			MenuItem menuItem = (MenuItem) z.next();
			String text = menuItem.getText();
			String newText = setUniqueMnemonic(text, flags, localePattern, true);
			if (!text.equals(newText)) {
				Image image = menuItem.getImage();
				menuItem.setText(newText);
				if (image != null) {
					menuItem.setImage(image);
				}
			}
		}
	}

	/**
	 * Given a Composite, this method walks all the children recursively and
	 * and sets the mnemonics uniquely for each child control where a
	 * mnemonic makes sense (eg, buttons).
	 * The letter/digit chosen for the mnemonic is unique for this Composite,
	 * so you should call this on as high a level of a composite as possible
	 * per window.
	 * Call this after populating your controls.
	 * @param parent the parent control to examine.
	 */
	public void setMnemonics(Composite parent) {
		setMnemonics(parent, new HashSet());
	}

	/**
	 * Given a Composite, this method walks all the children recursively and
	 * and sets the mnemonics uniquely for each child control where a
	 * mnemonic makes sense (for example, buttons).
	 * The letter/digit chosen for the mnemonic is unique for this Composite,
	 * so you should call this on as high a level of a composite as possible
	 * per window.
	 * Call this after populating your controls.
	 * @param parent the parent control to examine.
	 * @param ignoredControls the set of controls in which to not set mnemonics.
	 * If the controls are composites, their children are also not examined.
	 */
	public void setMnemonics(Composite parent, Set ignoredControls) {
		gatherCompositeMnemonics(parent);
		boolean mustLayout = setCompositeMnemonics(parent, ignoredControls);
		if (mustLayout) {
			parent.layout(true);
		}
	}
	
	private boolean setCompositeMnemonics(Composite parent, Set ignoredControls) {
		Control children[] = parent.getChildren();
		Control currentChild = null;
		boolean mustLayout = false;
		for (int i = 0; i < children.length; i++) {
			Control previousChild = currentChild;
			currentChild = children[i];
			if (!ignoredControls.contains(currentChild)) {
				if (currentChild instanceof Combo || currentChild instanceof InheritableEntryField || currentChild instanceof Text) {
					if (applyMnemonicsToPrecedingLabels && previousChild instanceof Label && currentChild.isEnabled()) {
						Label label = (Label) previousChild;
						String text = label.getText();
						if ((text != null) && (text.trim().length() > 0)) {
							String newText = setUniqueMnemonic(text);
							if (!text.equals(newText)) {
								label.setText(newText);
								mustLayout = true;
							}
						}
					}
				} else if (currentChild instanceof Button) {
					mustLayout |= setMnemonic((Button)currentChild);
				} else if (currentChild instanceof Composite) {
					/*
					 * d54732: (KM) we test Composites last since we don't want to recurse if it is a Combo.
					 * For a combo, we want to check if there is a preceding label.
					 * It's meaningless for a combo to have children.
					 */
					mustLayout |= setCompositeMnemonics((Composite) currentChild, ignoredControls);
					if (currentChild instanceof SystemHistoryCombo){
						// also apply the preceding label
						if (applyMnemonicsToPrecedingLabels && previousChild instanceof Label && currentChild.isEnabled()) {
							Label label = (Label) previousChild;
							String text = label.getText();
							if ((text != null) && (text.trim().length() > 0)) {
								String newText = setUniqueMnemonic(text);
								if (!text.equals(newText)) {
									label.setText(newText);
									mustLayout = true;
								}
							}
						}
					}
				} // ignore other controls
			}
		}
		return mustLayout;
	}


	private void gatherCompositeMnemonics(Composite parent) {
		Control children[] = parent.getChildren();
		Control currentChild = null;
		for (int i = 0; i < children.length; i++) {
			Control previousChild = currentChild;
			currentChild = children[i];
			String childText = null;
			if (currentChild instanceof Combo || currentChild instanceof InheritableEntryField || currentChild instanceof Text) {
				if (applyMnemonicsToPrecedingLabels && previousChild instanceof Label) {
					Label label = (Label) previousChild;
					childText = label.getText();
				}
			} else if (currentChild instanceof Button) {
				childText = ((Button)currentChild).getText();
			} else if (currentChild instanceof Composite) {
				gatherCompositeMnemonics((Composite) currentChild);
			} // ignore other controls
			if (childText != null) {
				char ch = getMnemonic(childText);
				makeUsed(ch);
			}
		}
	}

	/**
	 * Set if the mnemonics are for a preference page
	 * Preference pages already have a few buttons with mnemonics set by Eclipse
	 * We have to make sure we do not use the ones they use
	 */
	public Mnemonics setOnPreferencePage(boolean page) {
		if (page) {
			String[] labels = JFaceResources.getStrings(new String[] { "defaults", "apply" }); //$NON-NLS-1$ //$NON-NLS-2$
			String used = getMnemonicsFromStrings(labels).toUpperCase();
			makeUsed(used);
		}
		return this;
	}

	/**
	 * Set if the mnemonics are for a wizard page
	 * Wizard pages already have a few buttons with mnemonics set by Eclipse
	 * We have to make sure we do not use the ones they use
	 */
	public Mnemonics setOnWizardPage(boolean page) {
		if (page) {
			String[] labels = new String[] { IDialogConstants.BACK_LABEL, IDialogConstants.NEXT_LABEL, IDialogConstants.FINISH_LABEL };
			String used = getMnemonicsFromStrings(labels).toUpperCase();
			makeUsed(used);
		}
		return this;
	}

}