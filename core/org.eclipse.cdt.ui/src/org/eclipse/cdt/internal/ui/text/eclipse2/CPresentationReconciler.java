package org.eclipse.cdt.internal.ui.text.eclipse2;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.util.Assert;



/**
 * Standard implementation of <code>IPresentationReconciler</code>.
 * This implementation assumes that the tasks performed by its 
 * presentation damagers and repairers are lightweight and of low cost.
 * This presentation reconciler runs in the UI thread and always repairs
 * the complete damage caused by a document change rather than just the
 * portion overlapping with the viewer's viewport.<p>
 * Usually, clients instantiate this class and configure it before using it.
 */
public class CPresentationReconciler implements IPresentationReconciler {
		
	protected final static String TRACKED_PARTITION= "__reconciler_tracked_partition"; //$NON-NLS-1$
	
	
	/**
	 * Internal listener class.
	 */
	class InternalListener implements ITextInputListener, IDocumentListener, ITextListener, 
										IDocumentPartitioningListener, IDocumentPartitioningListenerExtension {
		
		/*
		 * @see ITextInputListener#inputDocumentAboutToBeChanged
		 */
		public void inputDocumentAboutToBeChanged(IDocument oldDocument, IDocument newDocument) {
			if (oldDocument != null) {
				try {
					
					fViewer.removeTextListener(this);
					oldDocument.removeDocumentListener(this);
					oldDocument.removeDocumentPartitioningListener(this);
					
					oldDocument.removePositionUpdater(fPositionUpdater);
					oldDocument.removePositionCategory(fPositionCategory);
				
				} catch (BadPositionCategoryException x) {
					// should not happend for former input documents;
				}
			}
		}
		
		/*
		 * @see ITextInputListener#inputDocumenChanged
		 */
		public void inputDocumentChanged(IDocument oldDocument, IDocument newDocument) {
			if (newDocument != null) {
				
				newDocument.addPositionCategory(fPositionCategory);
				newDocument.addPositionUpdater(fPositionUpdater);
				
				newDocument.addDocumentPartitioningListener(this);
				newDocument.addDocumentListener(this);
				fViewer.addTextListener(this);
				
				setDocumentToDamagers(newDocument);
				setDocumentToRepairers(newDocument);
				processDamage(new Region(0, newDocument.getLength()), newDocument);
			}
		}
		
		/*
		 * @see IDocumentPartitioningListener#documentPartitioningChanged
		 */
		public void documentPartitioningChanged(IDocument document) {
			fDocumentPartitioningChanged= true;
		}
		
		/*
		 * @see IDocumentPartitioningListenerExtension#documentPartitioningChanged
		 */
		public void documentPartitioningChanged(IDocument document, IRegion changedRegion) {
			fDocumentPartitioningChanged= true;
			fChangedDocumentPartitions= changedRegion;
		}
				
		/*
		 * @see IDocumentListener#documentAboutToBeChanged
		 */
		public void documentAboutToBeChanged(DocumentEvent e) {
			try {
				int offset= e.getOffset() + e.getLength();
				fRememberedPosition= new TypedPosition(e.getDocument().getPartition(offset));
				e.getDocument().addPosition(fPositionCategory, fRememberedPosition);
			} catch (BadLocationException x) {
				// can not happen
			} catch (BadPositionCategoryException x) {
				// should not happen on input elements
			}
		}
		
		/*
		 * @see IDocumentListener#documentChanged
		 */
		public void documentChanged(DocumentEvent e) {
			try {
				e.getDocument().removePosition(fPositionCategory, fRememberedPosition);
			} catch (BadPositionCategoryException x) {
				// can not happen on input documents
			}
		}
		
		/*
		 * @see ITextListener#textChanged
		 */
		public void textChanged(TextEvent e) {
		 	
		 	DocumentEvent de= e.getDocumentEvent();
		 	
		 	if (de == null) {
		 		
		 		IDocument d= fViewer.getDocument();
		 		processDamage(new Region(0, d.getLength()), d);
		 	
		 	} else {
			 	
			 	IRegion damage= getDamage(de);
				if (damage != null)
					processDamage(damage, de.getDocument());
		 	}
		 	
			fDocumentPartitioningChanged= false;
			fChangedDocumentPartitions= null;
		}
	};
	
	/** The map of presentation damagers */
	private Map fDamagers;
	/** The map of presentation repairers */
	private Map fRepairers;

	private ITextViewer fViewer;
	
	private InternalListener fInternalListener= new InternalListener();
	
	private String fPositionCategory;
	private IPositionUpdater fPositionUpdater;
	private TypedPosition fRememberedPosition;
	
	private boolean fDocumentPartitioningChanged= false;
	private IRegion fChangedDocumentPartitions= null;
	
	
	/**
	 * Creates a new presentation reconciler. There are no damagers or repairers
	 * registered with this reconciler.
	 */
	public CPresentationReconciler() {
		super();
		fPositionCategory= TRACKED_PARTITION + hashCode();
		fPositionUpdater= new DefaultPositionUpdater(fPositionCategory);
	}
	
	/**
	 * Registers a given presentation damager for a particular content type.
	 * If there is already a damager registered for this type, the new damager 
	 * is registered instead of the old one.
	 *
	 * @param damager the presentation damager to register, or <code>null</code> to remove an existing one
	 * @param contentType the content type under which to register
	 */
	public void setDamager(IPresentationDamager damager, String contentType) {
		
		Assert.isNotNull(contentType);
		
		if (fDamagers == null) 
			fDamagers= new HashMap();
			
		if (damager == null)
			fDamagers.remove(contentType);
		else
			fDamagers.put(contentType, damager);
	}
	
	/**
	 * Registers a given presentation repairer for a particular content type.
	 * If there is already a repairer registered for this type, the new repairer 
	 * is registered instead of the old one.
	 *
	 * @param repairer the presentation repairer to register, or <code>null</code> to remove an existing one
	 * @param contentType the content type under which to register
	 */
	public void setRepairer(IPresentationRepairer repairer, String contentType) {
		
		Assert.isNotNull(contentType);
					
		if (fRepairers == null)
			fRepairers= new HashMap();
		
		if (repairer == null)
			fRepairers.remove(contentType);
		else
			fRepairers.put(contentType, repairer);
	}
		
	/*
	 * @see IPresentationReconciler#install
	 */
	public void install(ITextViewer viewer) {
		Assert.isNotNull(viewer);
		
		fViewer= viewer;
		fViewer.addTextInputListener(fInternalListener);
	}
	
	/*
	 * @see IPresentationReconciler#uninstall
	 */
	public void uninstall() {
		fViewer.removeTextInputListener(fInternalListener);
	}
	 
	/*
	 * @see IPresentationReconciler#getDamager
	 */
	public IPresentationDamager getDamager(String contentType) {
		
		if (fDamagers == null)
			return null;
						
		return (IPresentationDamager) fDamagers.get(contentType);
	}
	
	/*
	 * @see IPresentationReconciler#getRepairer
	 */
	public IPresentationRepairer getRepairer(String contentType) {
		
		if (fRepairers == null)
			return null;
						
		return (IPresentationRepairer) fRepairers.get(contentType);
	}
	
	/**
	 * Informs all registed damagers about the document on which they will work. 
	 *
	 * @param document the document on which to work
	 */
	private void setDocumentToDamagers(IDocument document) {
		if (fDamagers != null) {
			Iterator e= fDamagers.values().iterator();
			while (e.hasNext()) {
				IPresentationDamager damager= (IPresentationDamager) e.next();
				damager.setDocument(document);
			}
		}
	}
	
	/**
	 * Informs all registed repairers about the document on which they will work.
	 *
	 * @param document the document on which to work
	 */
	private void setDocumentToRepairers(IDocument document) {
		if (fRepairers != null) {
			Iterator e= fRepairers.values().iterator();
			while (e.hasNext()) {
				IPresentationRepairer repairer= (IPresentationRepairer) e.next();
				repairer.setDocument(document);
			}
		}
	}
	
	/**
	 * Constructs a "repair description" for the given damage and returns 
	 * this description as a text presentation. For this, it queries the 
	 * partitioning of the damage region and asks for each partition an 
	 * appropriate presentation repairer to construct the "repair description"
	 * for this partition.
	 *
	 * @param damage the damage to be repaired
	 * @param document the document whose presentation must be repaired
	 * @return the presentation repair descritption as text presentation
	 */
	private TextPresentation createPresentation(IRegion damage, IDocument document) { 
		try {
			
			TextPresentation presentation= new TextPresentation();
			
			ITypedRegion[] partitioning= document.computePartitioning(damage.getOffset(), damage.getLength());
			for (int i= 0; i < partitioning.length; i++) {
				ITypedRegion r= partitioning[i];
				IPresentationRepairer repairer= getRepairer(r.getType());
				if (repairer != null)
					repairer.createPresentation(presentation, r);
			}
			
			return presentation;
			
		} catch (BadLocationException x) {
		}
		
		return null;
	}
	
		
	/**
	 * Checks for the first and the last affected partition and calls their damagers.
	 * Invalidates everything from the start of the damage for the first partition
	 * until the end of the damage for the last partition.
	 *
	 * @param e the event describing the document change
	 * @return the damaged caused by the change
	 */
	private IRegion getDamage(DocumentEvent e) {
		
		IRegion damage= null;
		
		try {
			
			ITypedRegion partition= e.getDocument().getPartition(e.getOffset());
			IPresentationDamager damager= getDamager(partition.getType());
			if (damager == null)
				return null;
				
			IRegion r= damager.getDamageRegion(partition, e, fDocumentPartitioningChanged);
			
			if (!fDocumentPartitioningChanged) {
				damage= r;
			} else {
				
				int damageEnd= getDamageEndOffset(e);
				
				int parititionDamageEnd= -1;
				if (fChangedDocumentPartitions != null)
					parititionDamageEnd= fChangedDocumentPartitions.getOffset() + fChangedDocumentPartitions.getLength();
					
				int end= Math.max(damageEnd, parititionDamageEnd);
				
				damage= end == -1 ? r : new Region(r.getOffset(), end - r.getOffset());
			}
			
		} catch (BadLocationException x) {
		}
		
		return damage;
	}
	
	/**
	 * Returns the end offset of the damage. If a partition has been splitted by
	 * the given document event also the second half of the original
	 * partition must be considered. This is achieved by using the remembered 
	 * partition range.
	 *
	 * @param e the event describing the change
	 * @return the damage end offset (excluding)
	 * @exception BadLocationException if method accesses invalid offset
	 */
	private int getDamageEndOffset(DocumentEvent e) throws BadLocationException {
		
		IDocument d= e.getDocument();
		
		int length= 0;
		if (e.getText() != null) {
			length= e.getText().length();
			if (length > 0)
				-- length;
		}
		
		ITypedRegion partition= d.getPartition(e.getOffset() + length);
		int endOffset= partition.getOffset() + partition.getLength();		
		if (endOffset == e.getOffset())
			return -1;
			
		int end= fRememberedPosition.getOffset() + fRememberedPosition.getLength();
		if (endOffset < end)
			partition= d.getPartition(end);
		
		IPresentationDamager damager= getDamager(partition.getType());
		if (damager == null)
			return -1;
			
		IRegion r= damager.getDamageRegion(partition, e, fDocumentPartitioningChanged);
		
		return r.getOffset() + r.getLength();
	}
		
	/**
	 * Processes the given damage.
	 * @param damage the damage to be repaired
	 * @param document the document whose presentation must be repaired
	 */
	private void processDamage(IRegion damage, IDocument document) {
		if (damage != null && damage.getLength() > 0) {
			TextPresentation p= createPresentation(damage, document);
			if (p != null && !p.isEmpty())
				applyTextRegionCollection(p);
		}
	}
	
	/**
	 * Applies the given text presentation to the text viewer the presentation
	 * reconciler is installed on.
	 *
	 * @param presentation the text presentation to be applied to the text viewer
	 */
	private void applyTextRegionCollection(TextPresentation presentation) {
		fViewer.changeTextPresentation(presentation, false);
	}	
}


