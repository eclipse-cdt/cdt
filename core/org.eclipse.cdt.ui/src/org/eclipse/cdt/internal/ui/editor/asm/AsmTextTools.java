/*******************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Wind River Systems, Inc.
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor.asm;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.cdt.core.model.AssemblyLanguage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICColorConstants;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.ITokenStore;
import org.eclipse.cdt.ui.text.ITokenStoreFactory;

import org.eclipse.cdt.internal.ui.text.CCommentScanner;
import org.eclipse.cdt.internal.ui.text.SingleTokenCScanner;
import org.eclipse.cdt.internal.ui.text.TokenStore;
import org.eclipse.cdt.internal.ui.text.asm.AsmPartitionScanner;
import org.eclipse.cdt.internal.ui.text.util.CColorManager;


/**
 * This type shares all scanners and the color manager between
 * its clients.
 * 
 * @deprecated No longer used within CDT.
 */
@Deprecated
public class AsmTextTools {
	
    private class PreferenceListener implements IPropertyChangeListener {
        @Override
		public void propertyChange(PropertyChangeEvent event) {
            adaptToPreferenceChange(event);
        }
    }
    
	/** The color manager -- use the same as for C code */
	private CColorManager fColorManager;
	/** The Asm source code scanner */
	private AsmCodeScanner fCodeScanner;
	/** The ASM multiline comment scanner */
	private CCommentScanner fMultilineCommentScanner;
	/** The ASM singleline comment scanner */
	private CCommentScanner fSinglelineCommentScanner;
	/** The ASM string scanner */
	private SingleTokenCScanner fStringScanner;
	/** The ASM preprocessor scanner */
	private AsmPreprocessorScanner fPreprocessorScanner;
	
	/** The preference store */
	private IPreferenceStore fPreferenceStore;
	/** The preference change listener */
	private PreferenceListener fPreferenceListener= new PreferenceListener();
	
	
    /**
     * Creates a new Asm text tools collection and eagerly creates 
     * and initializes all members of this collection.
     */
    public AsmTextTools(IPreferenceStore store) {
    	fPreferenceStore = store != null ? store : CUIPlugin.getDefault().getCombinedPreferenceStore();
    	fColorManager= new CColorManager();
    	
		ITokenStoreFactory factory= new ITokenStoreFactory() {
			@Override
			public ITokenStore createTokenStore(String[] propertyColorNames) {
				return new TokenStore(fColorManager, fPreferenceStore, propertyColorNames);
			}
		};

		fCodeScanner= new AsmCodeScanner(factory, AssemblyLanguage.getDefault());
		fPreprocessorScanner= new AsmPreprocessorScanner(factory, AssemblyLanguage.getDefault());
        fMultilineCommentScanner= new CCommentScanner(factory, ICColorConstants.C_MULTI_LINE_COMMENT);
        fSinglelineCommentScanner= new CCommentScanner(factory, ICColorConstants.C_SINGLE_LINE_COMMENT);
		fStringScanner= new SingleTokenCScanner(factory, ICColorConstants.C_STRING);

		// listener must be registered after initializing scanners
        fPreferenceStore.addPropertyChangeListener(fPreferenceListener);
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
	public RuleBasedScanner getCodeScanner() {
		return fCodeScanner;
	}
		
	/**
	 * Returns a scanner which is configured to scan multiline comments.
	 *
	 * @return a multiline comment scanner
	 */
	public RuleBasedScanner getMultilineCommentScanner() {
		return fMultilineCommentScanner;
	}

	/**
	 * Returns a scanner which is configured to scan singleline comments.
	 *
	 * @return a singleline comment scanner
	 */
	public RuleBasedScanner getSinglelineCommentScanner() {
		return fSinglelineCommentScanner;
	}
	
	/**
	 * Returns a scanner which is configured to scan strings.
	 *
	 * @return a string scanner
	 */
	public RuleBasedScanner getStringScanner() {
		return fStringScanner;
	}

	/**
	 * Returns a scanner which is configured to scan Asm preprocessor directives.
	 *
	 * @return an Asm preprocessor directives scanner
	 */
	public RuleBasedScanner getPreprocessorScanner() {
		return fPreprocessorScanner;
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
					fMultilineCommentScanner.affectsBehavior(event) ||
					fSinglelineCommentScanner.affectsBehavior(event) ||
					fStringScanner.affectsBehavior(event) ||
					fPreprocessorScanner.affectsBehavior(event);
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
		if (fMultilineCommentScanner.affectsBehavior(event))
			fMultilineCommentScanner.adaptToPreferenceChange(event);
		if (fSinglelineCommentScanner.affectsBehavior(event))
			fSinglelineCommentScanner.adaptToPreferenceChange(event);
		if (fStringScanner.affectsBehavior(event))
			fStringScanner.adaptToPreferenceChange(event);
		if (fPreprocessorScanner.affectsBehavior(event))
			fPreprocessorScanner.adaptToPreferenceChange(event);
	}

	public IDocumentPartitioner createDocumentPartitioner() {
		return new FastPartitioner(new AsmPartitionScanner(), ICPartitions.ALL_ASM_PARTITIONS);
	}

}
