package org.eclipse.cdt.internal.corext.textmanipulation;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.Assert;


public abstract class SimpleTextEdit extends TextEdit {

	private TextRange fRange;
	private String fText;

	public static SimpleTextEdit createReplace(int offset, int length, String text) {
		return new SimpleTextEditImpl(offset, length, text);
	}

	public static SimpleTextEdit createInsert(int offset, String text) {
		return new SimpleTextEditImpl(offset, 0, text);
	}
	
	public static SimpleTextEdit createDelete(int offset, int length) {
		return new SimpleTextEditImpl(offset, length, ""); //$NON-NLS-1$
	}
	
	private final static class SimpleTextEditImpl extends SimpleTextEdit {
		protected SimpleTextEditImpl(TextRange range, String text) {
			super(range, text);
		}
		protected SimpleTextEditImpl(int offset, int length, String text) {
			super(offset, length, text);
		}
		public TextEdit copy() {
			return new SimpleTextEditImpl(getTextRange().copy(), getText());
		}	
	}
	
	protected SimpleTextEdit() {
		this(TextRange.UNDEFINED, ""); //$NON-NLS-1$
	}
	
	protected SimpleTextEdit(int offset, int length, String text) {
		this(new TextRange(offset, length), text);
	}
	protected SimpleTextEdit(TextRange range, String text) {
		Assert.isNotNull(range);
		Assert.isNotNull(text);
		fRange= range;
		fText= text;
	}
	
	/**
	 * Returns the text edit's text
	 * 
	 * @return the text edit's text
	 */
	public String getText() {
		return fText;
	}
		
	/**
	 * Sets the text edit's text
	 * <p>
	 * This method should only be called from within the <code>
	 * connect</code> method.
	 * 
	 * @param text the text edit's text
	 */	
	protected final void setText(String text) {
		fText= text;
		Assert.isNotNull(fText);
	}
	
	/**
	 * Sets the text edit's range.
	 * <p>
	 * This method should only be called from within the <code>
	 * connect</code> method.
	 * 
	 * @param range the text edit's range.
	 */	
	protected void setTextRange(TextRange range) {
		fRange= range;
		Assert.isNotNull(fRange);
	}
	
	/* non Java-doc
	 * @see TextEdit#getTextRange
	 */
	public TextRange getTextRange() {
		return fRange;
	}
	
	/* non Java-doc
	 * @see TextEdit#doPerform
	 */
	public final TextEdit perform(TextBuffer buffer) throws CoreException {
		String current= buffer.getContent(fRange.fOffset, fRange.fLength);
		buffer.replace(fRange, fText);
		return new SimpleTextEditImpl(fRange, current);
	}	
}

