package org.eclipse.cdt.internal.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.internal.ui.CPlugin;
import org.eclipse.cdt.internal.ui.text.util.CColorManager;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitioner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * This type shares all scanners and the color manager between
 * its clients.
 */
public class CTextTools {
	
	private class PreferenceListener implements IPropertyChangeListener {
		public void propertyChange(PropertyChangeEvent event) {
			adaptToPreferenceChange(event);
		}
	};
	
	/** The color manager */
	private CColorManager fColorManager;
	/** The C source code scanner */
	private CCodeScanner fCodeScanner;
	/** The C++ source code scanner */
	private CppCodeScanner fCppCodeScanner;
	/** The C partitions scanner */
	private CPartitionScanner fPartitionScanner;
	/** The Java multiline comment scanner */
	private SingleTokenCScanner fMultilineCommentScanner;
	/** The Java singleline comment scanner */
	private SingleTokenCScanner fSinglelineCommentScanner;
	/** The Java string scanner */
	private SingleTokenCScanner fStringScanner;

	/** The preference store */
	private IPreferenceStore fPreferenceStore;	
	/** The preference change listener */
	private PreferenceListener fPreferenceListener= new PreferenceListener();
	
	
	/**
	 * Creates a new C text tools collection and eagerly creates 
	 * and initializes all members of this collection.
	 */
	public CTextTools(IPreferenceStore store) {
		if(store == null) {
			store = CPlugin.getDefault().getPreferenceStore();
		}
		fPreferenceStore = store;
		fPreferenceStore.addPropertyChangeListener(fPreferenceListener);
		
		fColorManager= new CColorManager();
		fCodeScanner= new CCodeScanner(fColorManager, store);
		fCppCodeScanner= new CppCodeScanner(fColorManager, store);
		fPartitionScanner= new CPartitionScanner();
		
		fMultilineCommentScanner= new SingleTokenCScanner(fColorManager, store, ICColorConstants.C_MULTI_LINE_COMMENT);
		fSinglelineCommentScanner= new SingleTokenCScanner(fColorManager, store, ICColorConstants.C_SINGLE_LINE_COMMENT);
		fStringScanner= new SingleTokenCScanner(fColorManager, store, ICColorConstants.C_STRING);
	}
	
	/**
	 * Creates a new C text tools collection and eagerly creates 
	 * and initializes all members of this collection.
	 */
	public CTextTools() {
		this((IPreferenceStore)null);
	}
	
	/**
	 * Disposes all members of this tools collection.
	 */
	public void dispose() {
		
		fCodeScanner= null;
		fPartitionScanner= null;
		
		
		fMultilineCommentScanner= null;
		fSinglelineCommentScanner= null;
		fStringScanner= null;
		
		if (fColorManager != null) {
			fColorManager.dispose();
			fColorManager= null;
		}
		
		if (fPreferenceStore != null) {
			fPreferenceStore.removePropertyChangeListener(fPreferenceListener);
			fPreferenceStore= null;
			fPreferenceListener= null;
		}
	}
	
	/**
	 * Gets the color manager.
	 */
	public CColorManager getColorManager() {
		return fColorManager;
	}
	
	/**
	 * Gets the code scanner used.
	 */
	public RuleBasedScanner getCCodeScanner() {
		return fCodeScanner;
	}
	
	/**
	 * Gets the code scanner used.
	 */
	public RuleBasedScanner getCppCodeScanner() {
		return fCppCodeScanner;
	}
	
	/**
	 * Gets the partition scanner used.
	 */
	public RuleBasedScanner getPartitionScanner() {
		return fPartitionScanner;
	}
	
	/**
	 * Gets the document provider used.
	 */
	public IDocumentPartitioner createDocumentPartitioner() {
		
		String[] types= new String[] {
			CPartitionScanner.C_MULTILINE_COMMENT,
			CPartitionScanner.C_SINGLE_LINE_COMMENT,
			CPartitionScanner.C_STRING
		};
		
		return new RuleBasedPartitioner(getPartitionScanner(), types);
	}
	
	/**
	 * Returns a scanner which is configured to scan Java multiline comments.
	 *
	 * @return a Java multiline comment scanner
	 */
	public RuleBasedScanner getMultilineCommentScanner() {
		return fMultilineCommentScanner;
	}

	/**
	 * Returns a scanner which is configured to scan Java singleline comments.
	 *
	 * @return a Java singleline comment scanner
	 */
	public RuleBasedScanner getSinglelineCommentScanner() {
		return fSinglelineCommentScanner;
	}
	
	/**
	 * Returns a scanner which is configured to scan Java strings.
	 *
	 * @return a Java string scanner
	 */
	public RuleBasedScanner getStringScanner() {
		return fStringScanner;
	}

	
	/**
	 * Determines whether the preference change encoded by the given event
	 * changes the behavior of one its contained components.
	 * 
	 * @param event the event to be investigated
	 * @return <code>true</code> if event causes a behavioral change
	 */
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return  fCodeScanner.affectsBehavior(event) ||
					fCppCodeScanner.affectsBehavior(event) ||
					fMultilineCommentScanner.affectsBehavior(event) ||
					fSinglelineCommentScanner.affectsBehavior(event) ||
					fStringScanner.affectsBehavior(event);
	}
	
	/**
	 * Adapts the behavior of the contained components to the change
	 * encoded in the given event.
	 * 
	 * @param event the event to whch to adapt
	 */
	protected void adaptToPreferenceChange(PropertyChangeEvent event) {
		if (fCodeScanner.affectsBehavior(event))
			fCodeScanner.adaptToPreferenceChange(event);
		if (fCppCodeScanner.affectsBehavior(event))
			fCppCodeScanner.adaptToPreferenceChange(event);
		if (fMultilineCommentScanner.affectsBehavior(event))
			fMultilineCommentScanner.adaptToPreferenceChange(event);
		if (fSinglelineCommentScanner.affectsBehavior(event))
			fSinglelineCommentScanner.adaptToPreferenceChange(event);
		if (fStringScanner.affectsBehavior(event))
			fStringScanner.adaptToPreferenceChange(event);
	}
}