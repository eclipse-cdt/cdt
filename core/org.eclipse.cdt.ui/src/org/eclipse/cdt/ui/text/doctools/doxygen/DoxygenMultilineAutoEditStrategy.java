/*******************************************************************************
 * Copyright (c) 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools.doxygen;

import java.util.LinkedHashSet;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.ui.text.doctools.DefaultMultilineCommentAutoEditStrategy;

/**
 * {@link IAutoEditStrategy} for adding Doxygen tags for comments.
 * 
 * <em>This class is not intended to be sub-classed by clients</em>
 * @since 5.0
 */
public class DoxygenMultilineAutoEditStrategy extends DefaultMultilineCommentAutoEditStrategy {
	private static final String SINGLELINE_COMMENT_PRECEDING = "//!< "; //$NON-NLS-1$
	private static final String PARAM = "@param "; //$NON-NLS-1$
	private static final String RETURN = "@return\n"; //$NON-NLS-1$

	/**
	 * Default constructor
	 */
	public DoxygenMultilineAutoEditStrategy() {
	
	}
	
	/**
	 * Returns the comment content to add to the documentation comment.
	 * @param decls The parameter declarations to describe
	 * @return a buffer containing the comment content to generate to describe the parameters of
	 * the specified {@link IASTParameterDeclaration} objects.
	 */
	protected StringBuffer paramTags(IASTParameterDeclaration[] decls) {
		StringBuffer result= new StringBuffer();
		for(int i=0; i<decls.length; i++) {
			IASTDeclarator dtor= decls[i].getDeclarator();
			result.append(PARAM+dtor.getName()+"\n"); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * @return the comment content to describe the return tag
	 */
	protected StringBuffer returnTag() {
		return new StringBuffer(RETURN);
	}

	/**
	 * @param def the function definition to analyze
	 * @return the parameter declarations for the specified function definition
	 */
	protected IASTParameterDeclaration[] getParameterDecls(IASTFunctionDefinition def) {
		IASTParameterDeclaration[] result;
		IASTFunctionDeclarator decl= def.getDeclarator();
		if (decl instanceof IASTStandardFunctionDeclarator) {
			IASTStandardFunctionDeclarator standardFunctionDecl= (IASTStandardFunctionDeclarator)decl;
			result= standardFunctionDecl.getParameters();
		} else /*if (def instanceof ICASTKnRFunctionDeclarator) {
			ICASTKnRFunctionDeclarator knrDeclarator= (ICASTKnRFunctionDeclarator)decl;
			result= knrDeclarator.getParameterDeclarations();
		} else */{
			result= new IASTParameterDeclaration[0];
		}
		return result;
	}

	/*
	 * @see org.eclipse.cdt.ui.text.doctools.DefaultMultilineCommentAutoEditStrategy#customizeAfterNewLineForDeclaration(org.eclipse.jface.text.IDocument, org.eclipse.cdt.core.dom.ast.IASTDeclaration, org.eclipse.jface.text.ITypedRegion)
	 */
	public StringBuffer customizeAfterNewLineForDeclaration(IDocument doc, IASTDeclaration dec, ITypedRegion partition) {
		StringBuffer result= new StringBuffer();

		while(dec instanceof ICPPASTTemplateDeclaration) /* if? */
			dec= ((ICPPASTTemplateDeclaration)dec).getDeclaration(); 

		if(dec instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition fd= (IASTFunctionDefinition) dec;
			result.append(paramTags(getParameterDecls(fd)));

			IASTDeclSpecifier ds= fd.getDeclSpecifier();
			boolean hasReturn= true;
			if(ds instanceof IASTSimpleDeclSpecifier) {
				IASTSimpleDeclSpecifier sds= (IASTSimpleDeclSpecifier) ds;
				if(sds.getType()==IASTSimpleDeclSpecifier.t_void) {
					hasReturn= false;
				}
			}
			if(hasReturn) {
				result.append(returnTag());
			}

			return result;
		}

		if(dec instanceof IASTSimpleDeclaration && ((IASTSimpleDeclaration)dec).getDeclSpecifier() instanceof IASTCompositeTypeSpecifier) {
			return result;
		}

		try {
			alterDoc(doc, dec);
		} catch(BadLocationException ble) {
			/*ignore*/
		}

		return new StringBuffer();
	}

	/*
	 * Add post-declaration comments to enumerators, after initializing a doc-comment on an enumeration
	 */
	private void alterDoc(IDocument doc, IASTDeclaration dec) throws BadLocationException {
		if(dec instanceof IASTSimpleDeclaration && ((IASTSimpleDeclaration)dec).getDeclSpecifier() instanceof IASTEnumerationSpecifier) {
			IASTEnumerationSpecifier spc= (IASTEnumerationSpecifier)  ((IASTSimpleDeclaration)dec).getDeclSpecifier();
			IASTEnumerator[] enms= spc.getEnumerators();

			class Entry {
				final int offset, length;
				StringBuilder comment;
				Entry(int offset, int length, String comment) {
					this.offset= offset;
					this.length= length;
					this.comment= new StringBuilder(comment);
				}
				@Override
				public int hashCode() {
					return offset;
				}
				@Override
				public boolean equals(Object obj) {
					if(obj instanceof Entry) {
						Entry other= (Entry) obj;
						return offset == other.offset;
					}
					return false;
				}
			}

			boolean noCollisions= true;
			LinkedHashSet<Entry> entries= new LinkedHashSet<Entry>();
			for(IASTEnumerator enumerator : enms) {
				IASTNodeLocation[] locs= enumerator.getName().getNodeLocations();
				if(locs.length==1) {
					int nodeOffset= locs[0].getNodeOffset()+locs[0].getNodeLength();
					String cmt= SINGLELINE_COMMENT_PRECEDING+enumerator.getName();
					IRegion line= doc.getLineInformationOfOffset(nodeOffset);
					if(!doc.get(line.getOffset(), line.getLength()).contains("//")) { //$NON-NLS-1$
						noCollisions &= entries.add(new Entry(line.getOffset(),line.getLength(), cmt));
					}
				} else {
					// TODO
				}
			}

			/*
			 * Only auto-insert comments if each enumerator is declared on a unique line
			 */
			if(noCollisions) {
				int max= Integer.MIN_VALUE;
				for(Entry e : entries) {
					if(e.length > max)
						max= e.length;
				}

				int addedLength=0;
				for(Entry e : entries) {
					// pad with whitespace
					int toAdd= max-e.length;
					for(int j=0; j<toAdd; j++) {
						e.comment.insert(0, " "); //$NON-NLS-1$
					}
					doc.replace(e.offset+e.length+addedLength, 0, e.comment.toString());
					addedLength+= e.comment.length();
				}
			}
		}
	}
}
