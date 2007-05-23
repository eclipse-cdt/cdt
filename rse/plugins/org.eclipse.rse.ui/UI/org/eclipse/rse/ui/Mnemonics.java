/********************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others. All rights reserved.
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
 ********************************************************************************/

package org.eclipse.rse.ui;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.rse.ui.widgets.InheritableEntryField;
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
 * A class for creating unique mnemonics for each control in a given
 * context - usually a composite control of some sort.
 */
public class Mnemonics {

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
	
	private final static Pattern TRANSPARENT_ENDING_PATTERN = Pattern.compile("(\\s|\\.\\.\\.|>|<|:|\uff0e\uff0e\uff0e|\uff1e|\uff1c|\uff1a)+$|\\t.*$"); //$NON-NLS-1$
	private boolean applyMnemonicsToPrecedingLabels = true;

	private Set usedSet = new HashSet();

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
	 * Finds the point at which to insert a mnemonic of the form (&x).
	 * Checks for transparent endings and trailing spaces.
	 * @param label the label to check
	 * @return the position at which a mnemonic can be inserted.
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
		usedSet.clear();
	}
	
	/**
	 * Resets the list of used mnemonic characters to those in the string.
	 * @param usedMnemonics
	 */
	public void clear(String usedMnemonics) {
		makeUsed(usedMnemonics);
	}

	/**
	 * Given a string, this starts at the first character and iterates until
	 * it finds a character not already used as a mnemonic.
	 * Not normally called from other classes, but rather by the setMnemonic
	 * methods in this class.
	 * Sets the mnemonic according to the org.eclipse.rse.ui/MNEMONIC_POLICY preference.
	 * (Note: this preference and the values below are NOT guaranteed API as yet and may change 
	 * without notice).
	 * In all policies, if the label has a mnemonic it is not touched.
	 * Duplicate mnemonics can occur between labels that have hard coded mnemonics.
	 * <ul>
	 * <li>0 = The labels are left untouched, mnemonics are never added.</li>
	 * <li>1 = Mnemonics are added to a label that does not have a mnemonic
	 * using a letter from the label
	 * only if an unused mnemonic can be found in the label.</li>
	 * <li>2 = A mnemonic is added to a label that does not have a mnemonic
	 * using a letter from the label
	 * even if that character has already been used in the context.
	 * This will typically result in a duplicate mnemonic assignment.</li>
	 * <li>3 = A mnemonic is added to a label that does not have a mnemonic
	 * using a letter from the label or generating an (&x) mnemonic if a 
	 * unique letter can be found.</li>
	 * </ul>
	 * @param label String to which to generate and apply the mnemonic
	 * @return input String with '&' inserted in front of the mnemonic character
	 */
	public String setUniqueMnemonic(String label) {
		int policy = 3;
		// determine the cases where the label does not need a mnemonic
		if (policy == 0 || label == null || label.trim().length() == 0 || label.equals("?")) { //$NON-NLS-1$
			return label;
		}
		// if a mnemonic exists in the label mark it as used
		char mn = getMnemonic(label);
		makeUsed(mn);
		// if a mnemonic exists in the label use it, if not add one
		if (mn == ' ') { // no mnemonic exists
			int p = findUniqueMnemonic(label);
			String mnemonicString = ""; //$NON-NLS-1$
			if (p >= 0) { // a character in the label can be used as the mnemonic
				makeUsed(label.charAt(p));
				mnemonicString = Character.toString(MNEMONIC_CHAR);
			} else {
				// a unique character in the label cannot be found, add one according to the policy
				if (policy == 1) { // policy 1, do not add one if one cannot be found
				}
				else if (policy == 2) { // policy 2, use a letter from the label anyway, favor upper case
					int endingPosition=getEndingPosition(label);
					for (p = 0; p < endingPosition; p++) {
						mn = label.charAt(p);
						if (UCharacter.isUpperCase(mn)) break;
					}
					if (p == endingPosition) {
						for (p = 0; p < endingPosition; p++) {
							mn = label.charAt(p);
							if (UCharacter.isLetter(mn)) break;
						}
					}
					if (p < label.length()) {
						mnemonicString = Character.toString(MNEMONIC_CHAR);
					}
				}
				else if (policy == 3) { // policy 3, add a mnemonic at the end
					String candidates = getCandidates();
					p = findUniqueMnemonic(candidates);
					if (p >= 0) {
						mn = candidates.charAt(p);
						mnemonicString = "(" + MNEMONIC_CHAR + mn + ")"; //$NON-NLS-1$ //$NON-NLS-2$
						p = getEndingPosition(label);
						makeUsed(mn);
					}
				}
			}
			StringBuffer newLabel = new StringBuffer(label);
			if (p >= 0) {
				newLabel.insert(p, mnemonicString);
			}
			label = newLabel.toString();
		} else { // a valid mnemonic already exists in the label
			makeUsed(mn);
		}
		return label;
	}

	/**
	 * Determine if given char is a unique mnemonic.
	 * @param ch the character to test.
	 * @return true if the character has not yet been used.
	 */
	public boolean isUniqueMnemonic(char ch) {
		return !isUsed(ch);
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
			if (!isUsed(ch) && isAcceptable(ch)) {
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
	
	private boolean isUsed(char ch) {
		// TODO if we are guaranteed java 1.5 we can use Character.valueOf(ch)
		boolean result = usedSet.contains(new Character(ch));
		return result;
	}
	
	private void makeUsed(char ch) {
		if (ch != ' ') {
			char lower = Character.toLowerCase(ch);
			char upper = Character.toUpperCase(ch);
			usedSet.add(new Character(lower));
			usedSet.add(new Character(upper));
		}
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
	 * Given a menu, this method walks all the items and assigns each a unique
	 * mnemonic. Also handles cascading menus.
	 * The mnemonics
	 * used on cascades are independent of those of the parent.
	 * Call this after populating the menu.
	 * @param menu the menu to examine
	 */
	public void setMnemonics(Menu menu) {
		gatherMenuMnemonics(menu);
		MenuItem[] items = menu.getItems();
		for (int i = 0; i < items.length; i++) {
			MenuItem menuItem = items[i];
			String text = menuItem.getText();
			if (text.indexOf(MNEMONIC_CHAR) < 0) { // if there is no mnemonic
				String newText = setUniqueMnemonic(text);
				if (!text.equals(newText)) {
					Image image = menuItem.getImage();
					menuItem.setText(newText);
					if (image != null) {
						menuItem.setImage(image);
					}
				}
			}
			Menu cascade = menuItem.getMenu();
			if (cascade != null) {
				Mnemonics context = new Mnemonics();
				context.setMnemonics(cascade);
			}
		}
	}
	
	private void gatherMenuMnemonics(Menu menu) {
		MenuItem[] items = menu.getItems();
		for (int i = 0; i < items.length; i++) {
			MenuItem menuItem = items[i];
			String text = menuItem.getText();
			char ch = getMnemonic(text);
			makeUsed(ch);
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
				if (currentChild instanceof Combo || currentChild instanceof InheritableEntryField) {
					if (applyMnemonicsToPrecedingLabels && previousChild instanceof Label) {
						Label label = (Label) previousChild;
						String text = label.getText();
						if ((text != null) && (text.trim().length() > 0)) {
							String newText = setUniqueMnemonic(text);
							if (!text.equals(newText)) {
								label.setText(setUniqueMnemonic(text));
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