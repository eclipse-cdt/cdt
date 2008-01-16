/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.MacroExpansionExplorer;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * Delegates the task of exploring macro expansions to simpler explorers dealing with
 * a single macro, only.
 * @since 5.0
 */
public class MultiMacroExpansionExplorer extends MacroExpansionExplorer {
	private static final class ASTFileLocation implements IASTFileLocation {
		private final String fFilePath;
		private final int fOffset;
		private final int fLength;

		private ASTFileLocation(String filePath, int offset, int length) {
			fFilePath= filePath;
			fOffset= offset;
			fLength= length;
		}
		public int getNodeOffset() {return fOffset;}
		public int getNodeLength() {return fLength;}
		public String getFileName() {return fFilePath;}

		public int getStartingLineNumber() {return 0;}
		public int getEndingLineNumber() {return 0;}
		public IASTFileLocation asFileLocation() {return this;}
	}

	private final char[] fSource;
	private final int[] fBoundaries;
	private final SingleMacroExpansionExplorer[] fDelegates;

	public MultiMacroExpansionExplorer(IASTTranslationUnit tu, IASTFileLocation loc) {
		if (tu == null || loc == null || loc.getNodeLength() == 0) {
			throw new IllegalArgumentException();
		}
		final ILocationResolver resolver = (ILocationResolver) tu.getAdapter(ILocationResolver.class);
		if (resolver == null) {
			throw new IllegalArgumentException();
		}
		final IASTMacroExpansion[] expansions = resolver.getMacroExpansions(loc);
		final int count= expansions.length;
		if (count > 0) {
			int from= loc.getNodeOffset();
			int to= from+loc.getNodeLength();

			final int lfrom = expansions[0].asFileLocation().getNodeOffset();
			final IASTFileLocation l= expansions[count-1].asFileLocation();
			final int lto= l.getNodeOffset() + l.getNodeLength();
			
			if (lfrom < from || lto > to) {
				from= Math.min(from, lfrom);
				to= Math.max(to, lto);
				loc= new ASTFileLocation(loc.getFileName(), from, to-from);
			}
		}
		
		fSource= resolver.getUnpreprocessedSignature(loc);
		fBoundaries= new int[count*2+1];
		fDelegates= new SingleMacroExpansionExplorer[count];

		final int firstOffset= loc.getNodeOffset();
		int bidx= -1;
		int didx= -1;
		for (IASTMacroExpansion expansion : expansions) {
			IASTName ref= expansion.getMacroReference();
			if (ref != null) {
				IASTFileLocation refLoc= expansion.asFileLocation();
				int from= refLoc.getNodeOffset()-firstOffset;
				int to= from+refLoc.getNodeLength();
				fBoundaries[++bidx]= from;
				fBoundaries[++bidx]= to;
				fDelegates[++didx]= new SingleMacroExpansionExplorer(new String(fSource, from, to-from), ref, 
						resolver.getImplicitMacroReferences(ref));
			}
		}
		fBoundaries[++bidx]= fSource.length;
	}

	public MultiMacroExpansionExplorer(final IASTTranslationUnit tu, final IRegion loc) {
		this(tu, new ASTFileLocation(tu.getFilePath(), loc.getOffset(), loc.getLength()));
	}

	@Override
	public IMacroExpansionStep getFullExpansion() {
		List<ReplaceEdit> edits = combineReplaceEdits(fDelegates.length);
		return new MacroExpansionStep(new String(fSource), null, edits.toArray(new ReplaceEdit[edits.size()]));
	}

	/**
	 * Combines the replace edits of the leading delegates.
	 */
	private List<ReplaceEdit> combineReplaceEdits(int count) {
		ArrayList<ReplaceEdit> edits= new ArrayList<ReplaceEdit>();
		for (int i=0; i < count; i++) {
			IMacroExpansionStep step= fDelegates[i].getFullExpansion();
			shiftAndAddEdits(fBoundaries[2*i], step.getReplacements(), edits);
		}
		return edits;
	}

	/**
	 * Shifts and adds the replace edits to the target list.
	 */
	private void shiftAndAddEdits(final int shift, ReplaceEdit[] stepEdits, List<ReplaceEdit> target) {
		for (int j = 0; j < stepEdits.length; j++) {
			final ReplaceEdit r = stepEdits[j];
			final String rtext = r.getText();
			target.add(new ReplaceEdit(shift+r.getOffset(), r.getLength(), rtext));
		}
	}


	@Override
	public int getExpansionStepCount() {
		int result= 0;
		for (int i=0; i < fDelegates.length; i++) {
			result+= fDelegates[i].getExpansionStepCount();
		}
		return result;
	}

	@Override
	public IMacroExpansionStep getExpansionStep(int step) throws IndexOutOfBoundsException {
		if (step < 0) {
			throw new IndexOutOfBoundsException();
		}
		int i;
		MacroExpansionStep dresult= null;
		StringBuilder before= new StringBuilder();
		before.append(fSource, 0, fBoundaries[0]);

		for (i=0; i < fDelegates.length; i++) {
			final SingleMacroExpansionExplorer delegate = fDelegates[i];
			int dsteps= delegate.getExpansionStepCount();
			if (step < dsteps) {
				dresult= delegate.getExpansionStep(step);
				break;
			}
			before.append(delegate.getFullExpansion().getCodeAfterStep());
			appendGap(before, i);
			step-= dsteps;
		}
		if (dresult == null) {
			throw new IndexOutOfBoundsException();
		}
		
		final int shift= before.length();
		final int end= fBoundaries[2*i+1];
		before.append(dresult.getCodeBeforeStep());
		before.append(fSource, end, fSource.length-end);
		
		List<ReplaceEdit> replacements= new ArrayList<ReplaceEdit>();
		shiftAndAddEdits(shift, dresult.getReplacements(), replacements);
		return new MacroExpansionStep(before.toString(), dresult.getExpandedMacro(), replacements.toArray(new ReplaceEdit[replacements.size()]));
	}
	
	private void appendGap(StringBuilder result, int i) {
		int idx= 2*i+1;
		int gapFrom= fBoundaries[idx];
		int gapTo= fBoundaries[++idx];
		result.append(fSource, gapFrom, gapTo-gapFrom);
	}
}
