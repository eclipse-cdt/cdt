package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.internal.core.newparser.Parser;
import org.eclipse.cdt.internal.parser.CStructurizer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

class TranslationUnitInfo extends CFileInfo {

	protected TranslationUnitInfo (CElement element) {
		super(element);
	}

	protected boolean hasChildren() {
		return true;
	}

	protected ICElement [] getChildren() {
		if (hasChanged()) {
			InputStream in = null;
			try {
				IResource res = getElement().getUnderlyingResource();
				if (res != null && res.getType() == IResource.FILE) {
					in = ((IFile)res).getContents();
					parse(in);
				} 
			} catch (CoreException e) {
				//e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();	
					} catch (IOException e) {
					}
				}
			}
				
		}
		return super.getChildren();
	}

	protected void parse(InputStream in) {
		try {
			removeChildren();
			if (CCorePlugin.getDefault().useNewParser()) {
				// new parser
				NewModelBuilder modelBuilder = new NewModelBuilder((TranslationUnit)getElement());
				Parser parser = new Parser(in, modelBuilder, true);
				parser.parse();
			} else {
				// cdt 1.0 parser
				ModelBuilder modelBuilder= new ModelBuilder((TranslationUnit)getElement());
				CStructurizer.getCStructurizer().parse(modelBuilder, in);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/* Overide the SourceManipulation for the range.  */
	protected ISourceRange getSourceRange() {
		IPath location = ((TranslationUnit)getElement()).getLocation(); 		
		return new SourceRange(0, (int)location.toFile().length());
	}
}
