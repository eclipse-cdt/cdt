package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.internal.parser.CStructurizer;
import org.eclipse.core.runtime.IPath;

/**
 * The Element Info of a Translation Unit.
 */
class TranslationUnitInfo extends OpenableInfo {

	/** 
	 * Timestamp of original resource at the time this element
	 * was opened or last updated.
	 */
	protected long fTimestamp;

	protected TranslationUnitInfo (CElement element) {
		super(element);
	}

	protected boolean hasChildren() {
		return true;
	}

	protected ICElement [] getChildren() {
		// CHECKPOINT: replacing the parsing done here before
		return fChildren;		
	}

	protected void parse(InputStream in) {
		try {
			removeChildren();
			if (CCorePlugin.getDefault().useNewParser()) {
				// new parser
				CModelBuilder modelBuilder = new CModelBuilder((TranslationUnit)getElement());
				modelBuilder.parse();

			} else {
				// cdt 1.0 parser
				ModelBuilder modelBuilder= new ModelBuilder((TranslationUnit)getElement());
				CStructurizer.getCStructurizer().parse(modelBuilder, in);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	protected void parse(String buf) {
		// CHECKPOINT: Parsing a string using the StringBufferInputStream
		// FIXME: quick fix for the IBinary which uses fake translationUnit
		if (buf != null) {
			StringBufferInputStream in = new StringBufferInputStream (buf);
			parse (in);
		}
	}

	/* Overide the SourceManipulation for the range.  */
	protected ISourceRange getSourceRange() {
		IPath location = ((TranslationUnit)getElement()).getLocation(); 		
		return new SourceRange(0, (int)location.toFile().length());
	}
}
