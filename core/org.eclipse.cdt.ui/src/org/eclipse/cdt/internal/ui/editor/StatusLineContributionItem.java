package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
  
/*
 * This code is temporarily here to provide some editor line/column numbers.
 * This is all in Eclipse 2.0, so we remove it when we move forward to that version.
 */


import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.IStatusField;



/**
 * Contribution item for the status line.
 */
public class StatusLineContributionItem extends ContributionItem implements IStatusField {
	
	
	static class StatusLineLabel extends CLabel {
		
		private static int INDENT= 3; // left and right margin used in CLabel
		
		private Point fFixedSize;
		
		public StatusLineLabel(Composite parent, int style) {
			super(parent, style);
			
			GC gc= new GC(parent);
			gc.setFont(parent.getFont());
			Point extent= gc.textExtent("MMMMMMMMM");
			gc.dispose();
			
			fFixedSize= new Point(extent.x + INDENT * 2, 10);
		}
		
		public Point computeSize(int wHint, int hHint, boolean changed) {
			return fFixedSize;
		}
	};
	
	private String fText;
	private Image fImage;
	private StatusLineLabel fLabel;
	
	/**
	 * Creates a new item with the given id.
	 * 
	 * @param id the item's id
	 */
	StatusLineContributionItem(String id) {
		super(id);
	}
	
	/*
	 * @see IStatusField#setText
	 */
	public void setText(String text) {
		fText= text;
		if (fLabel != null && !fLabel.isDisposed()) {
			fLabel.setText(fText);
		}		
	}
	
	/*
	 * @see IStatusField#setImage(Image)
	 */
	public void setImage(Image image) {
		fImage= image;
		if (fLabel != null && !fLabel.isDisposed()) {
			fLabel.setImage(fImage);
		}
	}
	
	/*
	 * @see IContributionItem#fill(Composite)
	 */
	public void fill(Composite parent) {
		fLabel= new StatusLineLabel(parent, SWT.SHADOW_IN);
		fLabel.setData(this);
		
		if (fText != null)
			fLabel.setText(fText);
	}
}


