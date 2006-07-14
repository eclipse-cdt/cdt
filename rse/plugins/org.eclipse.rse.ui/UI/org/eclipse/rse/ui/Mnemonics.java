/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.rse.ui.widgets.InheritableEntryField;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;


 
/**
 * A class for creating unique mnemonics per control per window.
 */
public class Mnemonics 
{
	private static final String[] TransparentEndings = { // endings that should appear after a mnemonic
		"...",  // ellipsis
		">>", // standard "more" 
		"<<", // standard "less"
		">", // "more" -- non-standard usage, must appear in list after >> 
		"<", // "less" -- non-standard usage, must appear in list after <<
		":", // colon
		"\uff0e\uff0e\uff0e", // wide ellipsis
		"\uff1e\uff1e", // wide standard "more"
		"\uff1c\uff1c", // wide standard "less"
		"\uff1e", // wide non-standard "more"
		"\uff1c", // wide non-standard "less"
		"\uff1a" // wide colon
	};
	
	private StringBuffer mnemonics = new StringBuffer(); // mnemonics used so far
	private static final String candidateChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private String preferencePageMnemonics = null;    // mnemonics used by Eclipse on preference pages
	private String wizardPageMnemonics = null;    // mnemonics used by Eclipse on wizard pages
	// private static String preferencePageMnemonics = "AD";    // mnemonics used by Eclipse on preference pages
	// private static String wizardPageMnemonics = "FBN";    // mnemonics used by Eclipse on wizard pages
	public static final char MNEMONIC_CHAR = '&';
	private boolean onPrefPage = false;
	private boolean onWizardPage = false;
	private boolean applyMnemonicsToPrecedingLabels = true;
	
    /**
     * Clear the list for re-use
     */
    public void clear()
    {
    	mnemonics = new StringBuffer();
    }
    
    /**
     * Inserts an added mnemonic of the form (&x) into a StringBuffer at the correct point.
     * Checks for transparent endings and trailing spaces.
     * @param label the label to check
     */
    private static void insertMnemonic(StringBuffer label, String mnemonic) {
    	int p = label.length();
		// check for trailing spaces #1
		while (p > 0 && label.charAt(p - 1) == ' ') {
			p--;
		}
		// check for transparent endings
    	for (int i = 0; i < TransparentEndings.length; i++) {
    		String transparentEnding = TransparentEndings[i];
    		int l = transparentEnding.length();
    		int n = p - l;
    		if (n >= 0) {
				String labelEnding = label.substring(n, n + l);
				if (labelEnding.equals(transparentEnding)) {
					p = n;
					break;
				}
    		}
		}
		// check for trailing spaces #2
		while (p > 0 && label.charAt(p - 1) == ' ') {
			p--;
		}
		// make sure there is something left to attach a mnemonic to
		if (p > 0) {
			label.insert(p, mnemonic);
		}
    }
    
	/**
	 * Given a string, this starts at the first character and iterates until
	 * it finds a character not previously used as a mnemonic on this page.
	 * Not normally called from other classes, but rather by the setMnemonic
	 * methods in this class.
	 * @param label String to which to generate and apply the mnemonic
	 * @return input String with '&' inserted in front of the unique character
	 */
	public String setUniqueMnemonic(String label)
	{
		
		// Kludge for now
		// If there is already a mnemonic, remove it
		label = removeMnemonic(label);
		//int iMnemonic = label.indexOf( MNEMONIC_CHAR );
		//if(  iMnemonic >= 0 && iMnemonic < label.length() - 1 ){
			//mnemonics.append( label.charAt( iMnemonic + 1 ) );
			//return label;
		//}
			
		int labelLen = label.length();
		if (labelLen == 0)
		  return label;
		else if ((labelLen == 1) && label.equals("?"))
		  return label;
		StringBuffer newLabel = new StringBuffer(label);        
		int mcharPos = findUniqueMnemonic(label);          
		if (mcharPos != -1)
		  newLabel.insert(mcharPos,MNEMONIC_CHAR);
		// if no unique character found, then
		// find a new arbitrary one from the alphabet...
		else 
		{
		  mcharPos = findUniqueMnemonic(candidateChars);
		  if (mcharPos != -1)
		  {
		  	String addedMnemonic = "(" + MNEMONIC_CHAR + candidateChars.charAt(mcharPos) + ")";
		  	insertMnemonic(newLabel, addedMnemonic);
		  }
		}
		return newLabel.toString();
	} // end getUniqueMnemonic
	/**
	 * Given a label and mnemonic, this applies that mnemonic to the label.
	 * Not normally called from other classes, but rather by the setUniqueMnemonic
	 * methods in this class.
	 * @param label String to which to apply the mnemonic
	 * @param mnemonicChar the character that is to be the mnemonic character
	 * @return input String with '&' inserted in front of the given character
	 */
	public static String applyMnemonic(String label, char mnemonicChar)
	{
		int labelLen = label.length();
		if (labelLen == 0)
		  return label;
		StringBuffer newLabel = new StringBuffer(label);        
		int mcharPos = findCharPos(label, mnemonicChar);
		if (mcharPos != -1)
		  newLabel.insert(mcharPos,MNEMONIC_CHAR);
		else
		{
		  	String addedMnemonic = new String("("+MNEMONIC_CHAR + mnemonicChar + ")");
			insertMnemonic(newLabel, addedMnemonic);
		}
		return newLabel.toString();
	} // end getUniqueMnemonic
	/**
	 * Given a char, find its position in the given string
	 */
	private static int findCharPos(String label, char charToFind)
	{
		int pos = -1;
		for (int idx=0; (pos==-1) && (idx<label.length()); idx++)
		   if (label.charAt(idx) == charToFind)
		     pos = idx;
	    return pos;
	}

	/**
	 * Determine if given char is a unique mnemonic
	 */
	public boolean isUniqueMnemonic(char currchar)
	{
		  boolean isUnique = true;
			 for (int idx=0; isUnique && (idx < mnemonics.length()); idx++)
				if (mnemonics.charAt(idx) == currchar)
				  isUnique = false;
		  return isUnique;
	}
	/**
	 * Find a uniqe mnemonic char in given string.
	 * Note if one is found, it is added to the list of currently used mnemonics!
	 *
	 * @param string to search each char for unique candidate
	 * @return index position of unique character in input string, or -1 if none found.
	 */
	public int findUniqueMnemonic(String label)
	{
		int labelLen = label.length();
		if (labelLen == 0)
		  return -1;
		int retcharPos = -1;          
		label = label.toUpperCase();
		char currchar = label.charAt(0);
		boolean isUnique = false;
		
		// if we're on a preference page, get the preference page mnemonics
		if (onPrefPage) {
			
			if (preferencePageMnemonics == null) {
				preferencePageMnemonics = getPreferencePageMnemonics();
			}
		}
		
		// if we're on a wizard page, get the wizard page mnemonics
		if (onWizardPage) {
			
			if (wizardPageMnemonics == null) {
				wizardPageMnemonics = getWizardPageMnemonics();
			}
		}
		
		// attempt to find the first character in the given
		// string that has not already been used as a mnemonic
		for (int idx=0; (idx<labelLen) && (retcharPos==-1); idx++)
		{
		   currchar = label.charAt(idx);
	
		   if ( !(onPrefPage && preferencePageMnemonics.indexOf( currchar ) != -1)
		      && !(onWizardPage && wizardPageMnemonics.indexOf( currchar ) != -1)
		      && candidateChars.indexOf( currchar ) != -1 )
		   {
			  isUnique = isUniqueMnemonic(currchar);
			  if (isUnique)
			  {
				 mnemonics.append(currchar);
				 retcharPos = idx;
			  }
		   }
		}
	 	return retcharPos;
	}
	
	/**
	 * Returns a string containing the mnemonics for a preference page.
	 * @return the mnemonics.
	 */
	private String getPreferencePageMnemonics() {
		String[] labels = JFaceResources.getStrings(new String[] { "defaults", "apply" });
		return getMnemonicsFromStrings(labels).toUpperCase();
	}
	
	/**
	 * Returns a string containing the mnemonics for a wizard page.
	 * @return the mnemonics.
	 */
	private String getWizardPageMnemonics() {
		String[] labels = new String[] {IDialogConstants.BACK_LABEL, IDialogConstants.NEXT_LABEL, IDialogConstants.FINISH_LABEL};
		return getMnemonicsFromStrings(labels).toUpperCase();
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
				result.append(strings[i].charAt(idx+1));
			}
		}
		
		return result.toString();
	}
    
	/**
	 * Adds a mnemonic to an SWT Button such that the user can select it via Ctrl/Alt+mnemonic.
	 * Note a mnemonic unique to this window is chosen
	 */
	public void setMnemonic(Button button)
	{
		removeMnemonic(button); // just in case it already has a mnemonic
		String text = button.getText();
		if ((text != null) && (text.trim().length() > 0))
		  button.setText(setUniqueMnemonic(text));
	}
	/**
	 * If a button is removed from a dialog window, call this method to remove its mnemonic from the list for this dialog.
	 * This frees it up for another button to use.
	 */
	public void removeMnemonic(Button button)
	{
		String text = button.getText();
		if (text == null)
		  return;
		int idx = text.indexOf(MNEMONIC_CHAR);
		if (idx >= 0)
		{
		   StringBuffer buffer = new StringBuffer(text);
		   char mchar = buffer.charAt(idx+1); // the char after the &
		   buffer.deleteCharAt(idx);  // delete the &
		   boolean found = false;
		   for (int mdx=0; !found && (mdx < mnemonics.length()); mdx++)
			  if (mnemonics.charAt(mdx) == mchar)
			  {
				found = true; 
				mnemonics.deleteCharAt(mdx);
			  }
		   
		   button.setText(buffer.toString());
		}
	}
	/**
	 * Helper method to strip the mnemonic from a string.
	 * Useful if using Eclipse supplied labels
	 */
	public static String removeMnemonic(String text)
	{
		int idx = text.indexOf(MNEMONIC_CHAR);
		if (idx >= 0)
		{
		   StringBuffer buffer = new StringBuffer(text);
		   char mchar = buffer.charAt(idx+1); // the char after the &
		   buffer.deleteCharAt(idx);  // delete the &
	
		   // in case of already appended (&X), remove the remaining (X)
		   if ( buffer.length() > (1 + idx) && idx > 1 && buffer.charAt(idx+1) == ')' && buffer.charAt(idx-1) == '(' )
		      buffer.delete(idx - 1, idx + 2);	
           return buffer.toString();
		}
		else
		  return text;
	}
	/**
	 * Remove and free up mnemonic
	 */
	public String removeAndFreeMnemonic(String text)
	{
		int idx = text.indexOf(MNEMONIC_CHAR);
		if (idx >= 0)
		{
		   StringBuffer buffer = new StringBuffer(text);
		   char mchar = buffer.charAt(idx+1); // the char after the &
		   buffer.deleteCharAt(idx);  // delete the &
		   boolean found = false;
		   for (int mdx=0; !found && (mdx < mnemonics.length()); mdx++)
			  if (mnemonics.charAt(mdx) == mchar)
			  {
				found = true; 
				mnemonics.deleteCharAt(mdx);
			  }
           return buffer.toString();
		}
		else
		  return text;		
	}
	
	/**
	 * Helper method to return the mnemonic from a string.
	 * Helpful when it is necessary to know the mnemonic assigned so it can be reassigned,
	 *  such as is necessary for buttons which toggle their text.
	 * @return the mnemonic if assigned, else a blank character.
	 */
	public static char getMnemonic(String text)
	{
		int idx = text.indexOf(MNEMONIC_CHAR);
		if (idx >= 0)
		  return text.charAt(idx+1);
		else
		  return ' ';	
	}
    
	/**
	 * Given a Composite, this method walks all the children recursively and 
	 *  and sets the mnemonics uniquely for each child control where a 
	 *  mnemonic makes sense (eg, buttons).
	 *  The letter/digit chosen for the mnemonic is unique for this Composite, 
	 *  so you should call this on as high a level of a composite as possible 
	 *  per Window.
	 * Call this after populating your controls.
	 */
	public void setMnemonics(Composite parent)
	{
		Control children[]  = parent.getChildren(); 
		if (children != null)
		{
			  Control currChild = null;
		  boolean bSetText = false;
		  for (int idx=0; idx < children.length; idx++)
		  {
		  	 currChild = children[idx];
		  	 // composite? Recurse
		  	 // d54732: but do not recurse if it is a combo! For a combo, we want to check
		  	 // if there is a preceding label. It's meaningless for a combo to
		  	 // have children anyway (KM)
		  	 if ((currChild instanceof Composite) &&
		  	 		(!applyMnemonicsToPrecedingLabels || (applyMnemonicsToPrecedingLabels &&
		  	 			!(currChild instanceof Combo) && !(currChild instanceof InheritableEntryField))))
		  	   setMnemonics((Composite)currChild);
		  	 // button? select and apply unique mnemonic...
		  	 else if (currChild instanceof Button)
		  	 {		  	 	
		  	 	Button currButton = (Button)currChild;
		  	 	String text = currButton.getText();
		  	 	if ((text!=null) && (text.trim().length()>0))
		  	 	{
		  	 	  currButton.setText(setUniqueMnemonic(text));
		  	 	  bSetText = true;
		  	 	}
		  	 }
			// entry field or combo box? select and apply unique mnemonic to preceding label...
			else if (applyMnemonicsToPrecedingLabels && (idx>0) && ((currChild instanceof Text) || (currChild instanceof Combo) || (currChild instanceof InheritableEntryField)) &&
			         (children[idx-1] instanceof Label)) 
			{
				Label currLabel = (Label)children[idx-1];
			   	String text = currLabel.getText();
			   	if ((text!=null) && (text.trim().length()>0))
			   	{
					currLabel.setText(setUniqueMnemonic(text));
				 	bSetText = true;
			   	}
			}
		  }
		  if ( bSetText == true )
			  parent.layout(true);	// in case a (x) was appended, we need to layout the controls again
		}
	}

	/**
	 * Given a menu, this method walks all the items and assigns each a unique
	 *  memnonic. Also handles casdading submenus.
	 * Call this after populating the menu.
	 */
	public void setMnemonics(Menu menu)
	{
		MenuItem[] children  = menu.getItems(); 
		if ((children != null) && (children.length>0))
		{
		  MenuItem currChild = null;
		  for (int idx=0; idx < children.length; idx++)
		  {
		  	 currChild = children[idx];
		  	 String text = currChild.getText();
		  	 if ((text!=null)&&(text.length()>0))
		  	 {
		  	   if (text.indexOf(MNEMONIC_CHAR) < 0) // bad things happen when setting mnemonics twice!
		  	   {
		  	   	 Image image = currChild.getImage();
		  	     currChild.setText(setUniqueMnemonic(text));
		  	     if (image != null)
		  	       currChild.setImage(image);
		  	   }
		  	 }
		  }
		}
	}

	/**
	 * Given a menu, this method walks all the items and assigns each a unique
	 *  memnonic. Also handles casdading submenus.
	 * <p>
	 * Also, since while we are at it, this overloaded method also sets a given ArmListener
	 *  to each menu item, perhaps for the purpose of displaying tooltip text. 
	 * It makes sense to do this when doing mnemonics because both must be done for every menu item
	 *  with text and must be done exactly once for each.
	 * <p>
	 * Call this after populating the menu.
	 */
	public void setMnemonicsAndArmListener(Menu menu, ArmListener listener)
	{
		MenuItem[] children  = menu.getItems(); 
		if ((children != null) && (children.length>0))
		{
		  MenuItem currChild = null;
		  for (int idx=0; idx < children.length; idx++)
		  {
		  	 currChild = children[idx];
		  	 String text = currChild.getText();
		  	 if ((text!=null)&&(text.length()>0))
		  	 {
		  	 	int mnemonicIndex = text.indexOf(MNEMONIC_CHAR);
		  	   if (mnemonicIndex < 0) // bad things happen when setting mnemonics twice!
		  	   {
		  	   	 Image image = currChild.getImage();
		  	     currChild.setText(setUniqueMnemonic(text));
		  	     if (image != null)
		  	       currChild.setImage(image);
		  	     currChild.addArmListener(listener);
		  	   }
		  	   else 
		  	   // hmm, already has a mnemonic char. Either it is an Eclipse/BP-supplied action, or we have been here before.
		  	   // The distinction is important as want to add an Arm listener, but only once!
		  	   {
		  	   	 // for now we do the brute force ugly thing...
		  	   	 Image image = currChild.getImage();
		  	   	 
		  	   	 // need to adjust any action that already has this mnemonic
		  	   	 char c = text.charAt(mnemonicIndex + 1);
		  	   	 
		  	   	 // anything already have this?
		  	   	 if (!isUniqueMnemonic(c))
		  	   	 {
		  	   	 	// if so, we need to adjust existing action
		  	   	 	for (int n = 0; n < idx; n++)
		  	   	 	{
		  	   	 		MenuItem oChild = children[n];
		  	   	 		String oText = oChild.getText();
		  	   	 		char oldN = getMnemonic(oText);
		  	   	 		if (oldN == c)
		  	   	 		{
		  	   	 			// this one now needs to change
		  	   	 			String cleanText = removeMnemonic(oText);
		  	   	 			oChild.setText(setUniqueMnemonic(cleanText));
		  	   	 		}
		  	   	 	}
		  	   	 }
		  	   	 
		  	   	 text = removeAndFreeMnemonic(text);
		  	     currChild.setText(setUniqueMnemonic(text));
		  	     if (image != null)
		  	       currChild.setImage(image);
		  	     currChild.removeArmListener(listener); // just in case
		  	     currChild.addArmListener(listener);
		  	   }
		  	 }
		  }
		}
	}

	/** 
	 * Set if the mnemonics are for a preference page
	 * Preference pages already have a few buttons with mnemonics set by Eclipse
	 * We have to make sure we do not use the ones they use
	 */
	public Mnemonics setOnPreferencePage(boolean page)
	{
		this.onPrefPage = page;
		return this;
	}

	/** 
	 * Set if the mnemonics are for a wizard page
	 * Wizard pages already have a few buttons with mnemonics set by Eclipse
	 * We have to make sure we do not use the ones they use
	 */
	public Mnemonics setOnWizardPage(boolean page)
	{
		this.onWizardPage = page;
		return this;
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
			
}