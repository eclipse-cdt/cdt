/*******************************************************************************
 * Copyright (c) 2008, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools.doxygen;

import java.util.LinkedHashSet;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.ui.text.doctools.DefaultMultilineCommentAutoEditStrategy;

/**
 * {@link IAutoEditStrategy} for adding Doxygen tags for comments.
 * 
 * @since 5.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DoxygenMultilineAutoEditStrategy extends DefaultMultilineCommentAutoEditStrategy {
	private static final String SINGLELINE_COMMENT_PRECEDING = "//!< "; //$NON-NLS-1$
	private static final String PARAM = "@param "; //$NON-NLS-1$
	private static final String RETURN = "@return"; //$NON-NLS-1$

	protected boolean documentPureVirtuals= true;
	protected boolean documentDeclarations= true;
	
	private String fLineDelimiter;
	
	public DoxygenMultilineAutoEditStrategy() {
	}
	
	/**
	 * @param decl the function declarator to document
	 * @param ds the function specifier to document
	 * @return content describing the specified function
	 */
	protected StringBuilder documentFunction(IASTFunctionDeclarator decl, IASTDeclSpecifier ds) {
		StringBuilder result= new StringBuilder();
		
		result.append(documentFunctionParameters(getParameterDecls(decl)));
		
		boolean hasReturn= true;
		if(ds instanceof IASTSimpleDeclSpecifier) {
			IASTSimpleDeclSpecifier sds= (IASTSimpleDeclSpecifier) ds;
			if(sds.getType()==IASTSimpleDeclSpecifier.t_void) {
				hasReturn= false;
			}
		}
		if(hasReturn) {
			result.append(documentFunctionReturn());
		}

		return result;
	}
	
	/**
	 * Returns the comment content to add to the documentation comment.
	 * @param decls The parameter declarations to describe
	 * @return a buffer containing the comment content to generate to describe the parameters of
	 * the specified {@link IASTParameterDeclaration} objects.
	 */
	protected StringBuilder documentFunctionParameters(IASTParameterDeclaration[] decls) {
		StringBuilder result= new StringBuilder();
		for(int i=0; i<decls.length; i++) {
			if(!isVoidParameter(decls[i])) {
				result.append(PARAM).append(getParameterName(decls[i])).append(getLineDelimiter());
			}
		}
		return result;
	}
	
	/**
	 * Get the default line delimiter for the currently customized document
	 * which should be used for new lines.
	 * 
	 * @return the default line delimiter
	 */
	private String getLineDelimiter() {
		return fLineDelimiter;
	}

	/**
	 * @param decl
	 * @return the name of the parameter
	 */
	String getParameterName(IASTParameterDeclaration decl) {
		IASTDeclarator dtor= decl.getDeclarator();
		for(int i=0; i<8 && dtor.getName().getRawSignature().length()==0 && dtor.getNestedDeclarator() != null; i++) {
			dtor= dtor.getNestedDeclarator();
		}
		return dtor.getName().getRawSignature();
	}
	
	/**
	 * @param decl
	 * @return true if the specified parameter declaration is of void type
	 */
	boolean isVoidParameter(IASTParameterDeclaration decl) {
		if(decl.getDeclSpecifier() instanceof IASTSimpleDeclSpecifier) {
			if(((IASTSimpleDeclSpecifier)decl.getDeclSpecifier()).getType() == IASTSimpleDeclSpecifier.t_void) {
				IASTDeclarator dtor= decl.getDeclarator();
				if(dtor.getPointerOperators().length == 0) {
					if(!(dtor instanceof IASTFunctionDeclarator) && !(dtor instanceof IASTArrayDeclarator)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * @return the comment content to describe the return
	 */
	protected StringBuilder documentFunctionReturn() {
		return new StringBuilder(RETURN).append(getLineDelimiter());
	}

	/**
	 * @param decl the function declarator to analyze
	 * @return the parameter declarations for the specified function definition
	 */
	protected IASTParameterDeclaration[] getParameterDecls(IASTFunctionDeclarator decl) {
		IASTParameterDeclaration[] result;
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
	@Override
	protected StringBuilder customizeAfterNewLineForDeclaration(IDocument doc, IASTDeclaration dec, ITypedRegion partition) {
		fLineDelimiter = TextUtilities.getDefaultLineDelimiter(doc);

		while(dec instanceof ICPPASTTemplateDeclaration) /* if? */
			dec= ((ICPPASTTemplateDeclaration)dec).getDeclaration(); 

		if(dec instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition fd= (IASTFunctionDefinition) dec;
			return documentFunction(fd.getDeclarator(), fd.getDeclSpecifier());
		}

		if(dec instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration sdec= (IASTSimpleDeclaration) dec;
			StringBuilder result= new StringBuilder();
			
			if(sdec.getDeclSpecifier() instanceof IASTCompositeTypeSpecifier) {
				return result;
			} else {
				IASTDeclarator[] dcs= sdec.getDeclarators();
				if(dcs.length == 1 && dcs[0] instanceof IASTFunctionDeclarator) {
					IASTFunctionDeclarator fdecl = (IASTFunctionDeclarator)dcs[0];
					boolean shouldDocument= documentDeclarations;
					if(documentPureVirtuals && dcs[0] instanceof ICPPASTFunctionDeclarator) {
						ICPPASTFunctionDeclarator cppfdecl= (ICPPASTFunctionDeclarator) dcs[0];
						shouldDocument = shouldDocument || cppfdecl.isPureVirtual();
					}
					
					if(shouldDocument) {
						return documentFunction(fdecl, sdec.getDeclSpecifier());
					}
				}
			}
		}

		try {
			alterDoc(doc, dec);
		} catch(BadLocationException ble) {
			/*ignore*/
		}

		return new StringBuilder();
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
				IASTNodeLocation loc= enumerator.getName().getFileLocation();
				if(loc != null) {
					int nodeOffset= loc.getNodeOffset()+loc.getNodeLength();
					String cmt= SINGLELINE_COMMENT_PRECEDING+enumerator.getName();
					IRegion line= doc.getLineInformationOfOffset(nodeOffset);
					if(!doc.get(line.getOffset(), line.getLength()).contains("//")) { //$NON-NLS-1$
						noCollisions &= entries.add(new Entry(line.getOffset(),line.getLength(), cmt));
					}
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
