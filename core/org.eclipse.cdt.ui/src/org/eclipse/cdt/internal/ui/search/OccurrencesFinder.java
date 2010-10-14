/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.util.Messages;

public class OccurrencesFinder implements IOccurrencesFinder {
	
	public static final String ID= "OccurrencesFinder"; //$NON-NLS-1$
	
	/**
	 * If set, don't search for implicit references.
	 */
	public static final int OPTION_EXCLUDE_IMPLICIT_REFERENCES = 1;
	
	private IASTTranslationUnit fRoot;
	private IASTName fSelectedNode;
	private IBinding fTarget;

	private List<OccurrenceLocation> fResult;

	private String fReadDescription;
	private String fWriteDescription;
	
	private int fOptions;
	
	public OccurrencesFinder() {
		super();
	}
	
	public String initialize(IASTTranslationUnit root, IASTNode node) {
		if (!(node instanceof IASTName))
			return CSearchMessages.OccurrencesFinder_no_element; 
		fRoot= root;
		fSelectedNode= (IASTName)node;
		fTarget= fSelectedNode.resolveBinding();
		if (fTarget == null)
			return CSearchMessages.OccurrencesFinder_no_binding; 
		
		fReadDescription= Messages.format(CSearchMessages.OccurrencesFinder_occurrence_description, fTarget.getName());
		fWriteDescription= Messages.format(CSearchMessages.OccurrencesFinder_occurrence_write_description, fTarget.getName());
		return null;
	}
	
	/**
	 * Specify search options.
	 * 
	 * @param options
	 */
	public void setOptions(int options) {
		fOptions = options;
	}
	
	private void performSearch() {
		if (fResult == null) {
			fResult= new ArrayList<OccurrenceLocation>();
			IASTName[] names= fRoot.getDeclarationsInAST(fTarget);
			for (IASTName candidate : names) {
				if (candidate.isPartOfTranslationUnitFile()) {
					addUsage(candidate, candidate.resolveBinding());
				}
			}
			names= fRoot.getReferences(fTarget);
			for (IASTName candidate : names) {
				if (candidate.isPartOfTranslationUnitFile()) {
					addUsage(candidate, candidate.resolveBinding());
				}
			}
			if (needImplicitReferences() && canHaveImplicitReference(fTarget)) {
				names= CPPVisitor.getImplicitReferences(fRoot, fTarget);
				for (IASTName candidate : names) {
					if (candidate.isPartOfTranslationUnitFile()) {
						addUsage(candidate, candidate.resolveBinding());
					}
				}
			}
		}
	}
	
	private boolean needImplicitReferences() {
		return (fOptions & OPTION_EXCLUDE_IMPLICIT_REFERENCES) == 0;
	}

	private boolean canHaveImplicitReference(IBinding binding) {
		final char[] op = Keywords.cOPERATOR;
		final char[] nameCharArray = binding.getNameCharArray();
		if (nameCharArray.length > 0) {
			if (nameCharArray[0] == '~') {
				return true;
			}
			if (CharArrayUtils.equals(nameCharArray, 0, op.length, op)) {
				return true;
			}
		}
		if (binding instanceof ICPPConstructor)
			return true;
		
		return false;
	}

	public OccurrenceLocation[] getOccurrences() {
		performSearch();
		if (fResult.isEmpty())
			return null;
		return fResult.toArray(new OccurrenceLocation[fResult.size()]);
	}

	public IASTTranslationUnit getASTRoot() {
		return fRoot;
	}
		
	/*
	 * @see org.eclipse.cdt.internal.ui.search.IOccurrencesFinder#getJobLabel()
	 */
	public String getJobLabel() {
		return CSearchMessages.OccurrencesFinder_searchfor ; 
	}
	
	public String getElementName() {
		if (fSelectedNode != null) {
			return new String(fSelectedNode.toCharArray());
		}
		return null;
	}
	
	public String getUnformattedPluralLabel() {
		return CSearchMessages.OccurrencesFinder_label_plural;
	}
	
	public String getUnformattedSingularLabel() {
		return CSearchMessages.OccurrencesFinder_label_singular;
	}
	
	private boolean addUsage(IASTName node, IBinding binding) {
		if (binding != null /* && Bindings.equals(binding, fTarget) */) {
			if (node instanceof ICPPASTTemplateId) {
				node= ((ICPPASTTemplateId) node).getTemplateName();
			}
			IASTFileLocation fileLocation= node.getImageLocation();
			if (fileLocation == null || !fRoot.getFilePath().equals(fileLocation.getFileName())) {
				fileLocation= node.getFileLocation();
			}
			if (fileLocation != null) {
				final int offset= fileLocation.getNodeOffset();
				final int length= fileLocation.getNodeLength();
				if (offset >= 0 && length > 0) {
					if (binding instanceof IVariable) {
						final boolean isWriteOccurrence = CSearchUtil.isWriteOccurrence(node, binding);
						int flag = isWriteOccurrence ? F_WRITE_OCCURRENCE : F_READ_OCCURRENCE;
						String description = isWriteOccurrence ? fWriteDescription : fReadDescription;
						fResult.add(new OccurrenceLocation(offset, length, flag, description));
					}
					else {
						fResult.add(new OccurrenceLocation(offset, length, F_READ_OCCURRENCE, fWriteDescription));
					}
				}
			}
			return true;
		}
		return false;
	}

	public int getSearchKind() {
		return K_OCCURRENCE;
	}
	
	public String getID() {
		return ID;
	}
}
