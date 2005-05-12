package org.eclipse.cdt.internal.ui.editor.asm;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.internal.ui.text.ICColorConstants;
import org.eclipse.cdt.internal.ui.text.CCommentScanner;
import org.eclipse.cdt.internal.ui.text.SingleTokenCScanner;
import org.eclipse.cdt.internal.ui.text.util.CColorManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * This type shares all scanners and the color manager between
 * its clients.
 */
public class AsmTextTools {
	
    private class PreferenceListener implements IPropertyChangeListener, Preferences.IPropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            adaptToPreferenceChange(event);
        }
        public void propertyChange(Preferences.PropertyChangeEvent event) {
            adaptToPreferenceChange(new PropertyChangeEvent(event.getSource(), event.getProperty(), event.getOldValue(), event.getNewValue()));
        }
    }
    
	/** The color manager -- use the same as for C code */
	private CColorManager fColorManager;
	/** The Asm source code scanner */
	private AsmCodeScanner fCodeScanner;
	/** The Asm partitions scanner */
	private AsmPartitionScanner fPartitionScanner;
	/** The ASM multiline comment scanner */
	private CCommentScanner fMultilineCommentScanner;
	/** The ASM singleline comment scanner */
	private CCommentScanner fSinglelineCommentScanner;
	/** The ASM string scanner */
	private SingleTokenCScanner fStringScanner;
	
	
	/** The preference store */
	private IPreferenceStore fPreferenceStore;
    /** The core preference store */
    private Preferences fCorePreferenceStore;		
	/** The preference change listener */
	private PreferenceListener fPreferenceListener= new PreferenceListener();
	
	
	/**
	 * Creates a new Asm text tools collection and eagerly creates 
	 * and initializes all members of this collection.
	 */
	public AsmTextTools(IPreferenceStore store) {
        this(store, null);
    }
    
    /**
     * Creates a new Asm text tools collection and eagerly creates 
     * and initializes all members of this collection.
     */
    public AsmTextTools(IPreferenceStore store, Preferences coreStore) {
		if(store == null) {
			store = CUIPlugin.getDefault().getPreferenceStore();
		}
        
        fPreferenceStore = store;
		store.addPropertyChangeListener(fPreferenceListener);
        
        fCorePreferenceStore= coreStore;
        if (fCorePreferenceStore != null) {
            fCorePreferenceStore.addPropertyChangeListener(fPreferenceListener);
        }
        
		fColorManager= new CColorManager();
		fCodeScanner= new AsmCodeScanner(fColorManager, store);
		fPartitionScanner= new AsmPartitionScanner();
				
        fMultilineCommentScanner= new CCommentScanner(fColorManager, store, coreStore, ICColorConstants.C_MULTI_LINE_COMMENT);
        fSinglelineCommentScanner= new CCommentScanner(fColorManager, store, coreStore, ICColorConstants.C_SINGLE_LINE_COMMENT);
		fStringScanner= new SingleTokenCScanner(fColorManager, store, ICColorConstants.C_STRING);
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
            
            if (fCorePreferenceStore != null) {
                fCorePreferenceStore.removePropertyChangeListener(fPreferenceListener);
                fCorePreferenceStore= null;
            }
            
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
			AsmPartitionScanner.ASM_MULTILINE_COMMENT,
			AsmPartitionScanner.ASM_SINGLE_LINE_COMMENT,
			AsmPartitionScanner.ASM_STRING
		};
		
		//return new RuleBasedPartitioner(getPartitionScanner(), types);
		//return new DefaultPartitioner(getPartitionScanner(), types);
		return new FastPartitioner(getPartitionScanner(), types);
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
		if (fMultilineCommentScanner.affectsBehavior(event))
			fMultilineCommentScanner.adaptToPreferenceChange(event);
		if (fSinglelineCommentScanner.affectsBehavior(event))
			fSinglelineCommentScanner.adaptToPreferenceChange(event);
		if (fStringScanner.affectsBehavior(event))
			fStringScanner.adaptToPreferenceChange(event);
	}
		
}