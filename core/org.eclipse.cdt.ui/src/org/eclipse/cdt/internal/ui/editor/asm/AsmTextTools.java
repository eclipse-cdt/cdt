package org.eclipse.cdt.internal.ui.editor.asm;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.internal.ui.CPlugin;
import org.eclipse.cdt.internal.ui.text.util.CColorManager;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.RuleBasedPartitioner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * This type shares all scanners and the color manager between
 * its clients.
 */
public class AsmTextTools {
	
	private class PreferenceListener implements IPropertyChangeListener {
		public void propertyChange(PropertyChangeEvent event) {
			adaptToPreferenceChange(event);
		}
	};
	/** The color manager -- use the same as for C code */
	private CColorManager fColorManager;
	/** The Asm source code scanner */
	private AsmCodeScanner fCodeScanner;
	/** The Asm partitions scanner */
	private AsmPartitionScanner fPartitionScanner;
		
	/** The preference change listener */
	private PreferenceListener fPreferenceListener= new PreferenceListener();
	
	
	/**
	 * Creates a new Asm text tools collection and eagerly creates 
	 * and initializes all members of this collection.
	 */
	public AsmTextTools(IPreferenceStore store) {
		if(store == null) {
			store = CPlugin.getDefault().getPreferenceStore();
		}
		store.addPropertyChangeListener(fPreferenceListener);
		fColorManager= new CColorManager();
		fCodeScanner= new AsmCodeScanner(fColorManager, store);
		fPartitionScanner= new AsmPartitionScanner();
	}
	
	/**
	 * Creates a new Asm text tools collection and eagerly creates 
	 * and initializes all members of this collection.
	 */
	public AsmTextTools() {
		this((IPreferenceStore)null);
	}
	/**
	 * Disposes all members of this tools collection.
	 */
	public void dispose() {
		
		fCodeScanner= null;
		fPartitionScanner= null;
		
		fColorManager.dispose();
		fColorManager= null;
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
	public RuleBasedScanner getCodeScanner() {
		return fCodeScanner;
	}
	
	/**
	 * Gets the partition scanner used.
	 */
	public IPartitionTokenScanner getPartitionScanner() {
		return fPartitionScanner;
	}
	
	/**
	 * Gets the document provider used.
	 */
	public IDocumentPartitioner createDocumentPartitioner() {
		
		String[] types= new String[] {
			AsmPartitionScanner.C_MULTILINE_COMMENT
		};
		
		//return new RuleBasedPartitioner(getPartitionScanner(), types);
		return new DefaultPartitioner(getPartitionScanner(), types);
	}
	
		
	/**
	 * Determines whether the preference change encoded by the given event
	 * changes the behavior of one its contained components.
	 * 
	 * @param event the event to be investigated
	 * @return <code>true</code> if event causes a behavioral change
	 */
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return  fCodeScanner.affectsBehavior(event);
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
	}
}