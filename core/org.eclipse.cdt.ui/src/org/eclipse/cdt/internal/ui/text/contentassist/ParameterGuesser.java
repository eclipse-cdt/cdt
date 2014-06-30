/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mentor Graphics (Mohamed Azab) - added the API to CDT and made the necessary changes
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.Context;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.UDCMode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Cost;

import org.eclipse.cdt.internal.ui.util.StringMatcher;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;

/**
 * This iAPI layout is copied from org.eclipse.jdt.internal.ui.text.java.ParameterGuesser
 * 
 * This class triggers a code-completion that will track all global, local and member variables and order them logically for later
 * use as a parameter guessing proposal.
 */
public class ParameterGuesser {
	private IASTTranslationUnit  fTranslationUnit;
	private static final char[] NO_TRIGGERS= new char[0];
	private final Set<String> fAlreadyMatchedNames;

	private final static class Variable {

		/**
		 * Variable type. Used to choose the best guess based on scope (Local beats instance beats inherited beats global).
		 */
		public static final int LOCAL = 0;
		public static final int FIELD = 1;
		public static final int GLOBAL = 3;

		public final IType qualifiedTypeName;
		public final String name;
		public final int variableType;
		public final int positionScore;

		public final boolean isAutoboxingMatch;

		public final char[] triggerChars;
		public final ImageDescriptor descriptor;

		public boolean alreadyMatched;

		public Variable(IType qualifiedTypeName, String name, int variableType, boolean isAutoboxMatch, int positionScore, char[] triggerChars, ImageDescriptor descriptor) {
			this.qualifiedTypeName= qualifiedTypeName;
			this.name= name;
			this.variableType= variableType;
			this.positionScore= positionScore;
			this.triggerChars= triggerChars;
			this.descriptor= descriptor;
			this.isAutoboxingMatch= isAutoboxMatch;
			this.alreadyMatched= false;
		}

		/*
		 * @see Object#toString()
		 */
		@Override
		public String toString() {

			StringBuffer buffer= new StringBuffer();
			buffer.append(qualifiedTypeName);
			buffer.append(' ');
			buffer.append(name);
			buffer.append(" ("); //$NON-NLS-1$
			buffer.append(variableType);
			buffer.append(')');

			return buffer.toString();
		}
	}

	/**
	 * Creates a parameter guesser
	 */
	public ParameterGuesser(IASTTranslationUnit  translationUnit) {
		fAlreadyMatchedNames= new HashSet<String>();
		fTranslationUnit = translationUnit;
	}

	private List<Variable> evaluateVisibleMatches(IType expectedType, ArrayList<IBinding> suggestions) throws CModelException {
		ArrayList<Variable> res= new ArrayList<Variable>();
		int size = suggestions.size();
		for (int i= 0; i < size; i++) {
			Variable variable= createVariable(suggestions.get(i), expectedType, i);
			if (variable != null) {
				if (fAlreadyMatchedNames.contains(variable.name)) {
					variable.alreadyMatched= true;
				}
				res.add(variable);
			}
		}
		return res;
	}

	private boolean isAnonymousBinding(IBinding binding) {
		char[] name= binding.getNameCharArray();
		return name.length == 0 || name[0] == '{';
	}

	protected IType getType(IBinding binding) {
		if (!isAnonymousBinding(binding)) {
			if (binding instanceof IVariable) {
				return ((IVariable) binding).getType();
			} else {
				return null;
			}
		}
		return null;
	}

	public Variable createVariable(IBinding element, IType enclosingType, int positionScore) throws CModelException {
		IType elementType = getType(element);
		String elementName = element.getName();
		if (elementType != null && (elementType.toString().equals(enclosingType.toString())
				|| elementType.isSameType(enclosingType)
				|| isParent(elementType, enclosingType)
				|| isAutomaticCasting(enclosingType, elementType)
				|| isReferenceTo(enclosingType, elementType)
				|| isReferenceTo(elementType, enclosingType))) {
			int variableType = Variable.GLOBAL;
			if (element instanceof ICPPField) {
				variableType = Variable.FIELD;

			} else if (element instanceof IVariable) {
				try {
					if (element instanceof ICPPBinding && ((ICPPBinding) element).isGloballyQualified()) {
						variableType = Variable.GLOBAL;
					} else {
						variableType = Variable.LOCAL;
					}
				} catch (DOMException e) {
				}
			}

			// Handle reference case
			if (isReferenceTo(enclosingType, elementType))
				elementName = "&" + elementName; //$NON-NLS-1$
			else if (isReferenceTo(elementType, enclosingType))
				elementName = "*" + elementName; //$NON-NLS-1$
			return new Variable(elementType, elementName, variableType, false, positionScore, NO_TRIGGERS, getImageDescriptor(element));
		}
		return null;
	}

	private boolean isReferenceTo(IType ref, IType val) {
		if (ref instanceof IPointerType) {
			IType ptr = ((IPointerType) ref).getType();
			if (ptr.toString().equals(val.toString()) || ptr.isSameType(val))
				return true;
		}
		return false;
	}

	/**
	 * @return true if the parent type is a direct/indirect parent of the child type
	 */
	private boolean isParent(IType child, IType parent) {
		if (child != null && parent != null && child instanceof ICPPClassType && parent instanceof ICPPClassType) {
			ICPPBase [] bases = ((ICPPClassType) child).getBases();
			for (ICPPBase base : bases) {
				IType tmpType = base.getBaseClassType();
				if (tmpType.toString().equals(parent.toString()) || tmpType.isSameType(parent) || isParent(tmpType, parent))
					return true;
			}
		}
		return false;
	}

	/**
	 * @return true if the orginType can be automatically casted to the candidateType
	 */
	private boolean isAutomaticCasting(IType orginType, IType candidateType) {
		try {
			Cost cost = Conversions.checkImplicitConversionSequence(orginType, candidateType, ValueCategory.LVALUE, UDCMode.ALLOWED, Context.ORDINARY, fTranslationUnit);
			if (cost.converts())
				return true;
		} catch (DOMException e) {
			return false;
		}
		return false;
	}

	/**
	 * @see {@link DOMCompletionProposalComputer#getImage()}
	 */
	private ImageDescriptor getImageDescriptor(IBinding binding) {
		ImageDescriptor imageDescriptor = null;

		if (binding instanceof ITypedef) {
			imageDescriptor = CElementImageProvider.getTypedefImageDescriptor();
		} else if (binding instanceof ICompositeType) {
			if (((ICompositeType)binding).getKey() == ICPPClassType.k_class || binding instanceof ICPPClassTemplate)
				imageDescriptor = CElementImageProvider.getClassImageDescriptor();
			else if (((ICompositeType)binding).getKey() == ICompositeType.k_struct)
				imageDescriptor = CElementImageProvider.getStructImageDescriptor();
			else if (((ICompositeType)binding).getKey() == ICompositeType.k_union)
				imageDescriptor = CElementImageProvider.getUnionImageDescriptor();
		} else if (binding instanceof ICPPMethod) {
			switch (((ICPPMethod)binding).getVisibility()) {
			case ICPPMember.v_private:
				imageDescriptor = CElementImageProvider.getMethodImageDescriptor(ASTAccessVisibility.PRIVATE);
				break;
			case ICPPMember.v_protected:
				imageDescriptor = CElementImageProvider.getMethodImageDescriptor(ASTAccessVisibility.PROTECTED);
				break;
			default:
				imageDescriptor = CElementImageProvider.getMethodImageDescriptor(ASTAccessVisibility.PUBLIC);
				break;
			}
		} else if (binding instanceof IFunction) {
			imageDescriptor = CElementImageProvider.getFunctionImageDescriptor();
		} else if (binding instanceof ICPPField) {
			switch (((ICPPField)binding).getVisibility()) {
			case ICPPMember.v_private:
				imageDescriptor = CElementImageProvider.getFieldImageDescriptor(ASTAccessVisibility.PRIVATE);
				break;
			case ICPPMember.v_protected:
				imageDescriptor = CElementImageProvider.getFieldImageDescriptor(ASTAccessVisibility.PROTECTED);
				break;
			default:
				imageDescriptor = CElementImageProvider.getFieldImageDescriptor(ASTAccessVisibility.PUBLIC);
				break;
			}
		} else if (binding instanceof IField) {
			imageDescriptor = CElementImageProvider.getFieldImageDescriptor(ASTAccessVisibility.PUBLIC);
		} else if (binding instanceof IVariable) {
			imageDescriptor = CElementImageProvider.getVariableImageDescriptor();
		} else if (binding instanceof IEnumeration) {
			imageDescriptor = CElementImageProvider.getEnumerationImageDescriptor();
		} else if (binding instanceof IEnumerator) {
			imageDescriptor = CElementImageProvider.getEnumeratorImageDescriptor();
		} else if (binding instanceof ICPPNamespace) {
			imageDescriptor = CElementImageProvider.getNamespaceImageDescriptor();
		} else if (binding instanceof ICPPFunctionTemplate) {
			imageDescriptor = CElementImageProvider.getFunctionImageDescriptor();
		} else if (binding instanceof ICPPUsingDeclaration) {
			IBinding[] delegates = ((ICPPUsingDeclaration) binding).getDelegates();
			if (delegates.length > 0)
				return getImageDescriptor(delegates[0]);
		}
		return imageDescriptor;
	}

	/**
	 * 
	 * Copied from JDT
	 * 
	 * Returns the matches for the type and name argument, ordered by match quality.
	 * 
	 * @param expectedType - the qualified type of the parameter we are trying to match
	 * @param paramName - the name of the parameter (used to find similarly named matches)
	 * @param pos the position
	 * @param suggestions the suggestions or <code>null</code>
	 * @param fillBestGuess <code>true</code> if the best guess should be filled in
	 * @param isLastParameter <code>true</code> iff this proposal is for the last parameter of a method
	 * @return returns the name of the best match, or <code>null</code> if no match found
	 */
	public ICompletionProposal[] parameterProposals(IType expectedType, String paramName, Position pos, ArrayList<IBinding> suggestions, boolean fillBestGuess, boolean isLastParameter) throws CModelException {
		List<Variable> typeMatches= evaluateVisibleMatches(expectedType, suggestions);
		typeMatches = removeDuplicates(typeMatches);
		orderMatches(typeMatches, paramName);

		boolean hasVarWithParamName= false;
		ICompletionProposal[] ret= new ICompletionProposal[typeMatches.size()];
		int i= 0; int replacementLength= 0;
		for (Iterator<Variable> it= typeMatches.iterator(); it.hasNext();) {
			Variable v= it.next();
			if (i == 0) {
				fAlreadyMatchedNames.add(v.name);
				replacementLength= v.name.length();
			}

			String displayString= v.name;
			hasVarWithParamName |= displayString.equals(paramName);

			final char[] triggers;
			if (isLastParameter) {
				triggers= v.triggerChars;
			} else {
				triggers= new char[v.triggerChars.length + 1];
				System.arraycopy(v.triggerChars, 0, triggers, 0, v.triggerChars.length);
				triggers[triggers.length - 1]= ',';
			}
			ret[i++]= new PositionBasedCompletionProposal(v.name, pos, replacementLength, getImage(v.descriptor), displayString, null, null, triggers);
		}
		if (!fillBestGuess && !hasVarWithParamName) {
			// insert a proposal with the argument name
			ICompletionProposal[] extended= new ICompletionProposal[ret.length + 1];
			System.arraycopy(ret, 0, extended, 1, ret.length);
			extended[0]= new PositionBasedCompletionProposal(paramName, pos, replacementLength, null, paramName, null, null, isLastParameter ? null : new char[] {','});
			return extended;
		}
		return ret;
	}

	/**
	 * Copied from JDT
	 */
	private static class MatchComparator implements Comparator<Variable> {

		private String fParamName;

		MatchComparator(String paramName) {
			fParamName= paramName;
		}

		@Override
		public int compare(Variable one, Variable two) {
			return score(two) - score(one);
		}

		/**
		 * The four order criteria as described below - put already used into bit 10, all others
		 * into bits 0-9, 11-20, 21-30; 31 is sign - always 0
		 * 
		 * @param v the variable
		 * @return the score for <code>v</code>
		 */
		private int score(Variable v) {
			int variableScore= 100 - v.variableType; // since these are increasing with distance
			int subStringScore= getLongestCommonSubstring(v.name, fParamName).length();
			// substring scores under 60% are not considered
			// this prevents marginal matches like a - ba and false - isBool that will
			// destroy the sort order
			int shorter= Math.min(v.name.length(), fParamName.length());
			if (subStringScore < 0.6 * shorter)
				subStringScore= 0;

			int positionScore= v.positionScore; // since ???
			int matchedScore= v.alreadyMatched ? 0 : 1;
			int autoboxingScore= v.isAutoboxingMatch ? 0 : 1;

			int score= autoboxingScore << 30 | variableScore << 21 | subStringScore << 11 | matchedScore << 10 | positionScore;
			return score;
		}

	}

	/**
	 * 
	 * Copied from JDT
	 * 
	 * Determine the best match of all possible type matches.  The input into this method is all
	 * possible completions that match the type of the argument. The purpose of this method is to
	 * choose among them based on the following simple rules:
	 *
	 * 	1) Local Variables > Instance/Class Variables > Inherited Instance/Class Variables
	 *
	 * 	2) A longer case insensitive substring match will prevail
	 *
	 *  3) Variables that have not been used already during this completion will prevail over
	 * 		those that have already been used (this avoids the same String/int/char from being passed
	 * 		in for multiple arguments)
	 *
	 * 	4) A better source position score will prevail (the declaration point of the variable, or
	 * 		"how close to the point of completion?"
	 *
	 * @param typeMatches the list of type matches
	 * @param paramName the parameter name
	 */
	private static void orderMatches(List<Variable> typeMatches, String paramName) {
		if (typeMatches != null) Collections.sort(typeMatches, new MatchComparator(paramName));
	}

	/**
	 * 
	 * Copied from JDT
	 * 
	 * Remove the duplicates from the list if any.
	 */
	private static List<Variable> removeDuplicates(List<Variable> typeMatches) {
		HashSet<Variable> set = new HashSet<Variable>();
		set.addAll(typeMatches);
		return Arrays.asList(set.toArray(new Variable[set.size()]));
	}

	/**
	 * 
	 * Copied from JDT
	 * 
	 * Returns the longest common substring of two strings.
	 *
	 * @param first the first string
	 * @param second the second string
	 * @return the longest common substring
	 */
	private static String getLongestCommonSubstring(String first, String second) {

		String shorter= (first.length() <= second.length()) ? first : second;
		String longer= shorter == first ? second : first;

		int minLength= shorter.length();

		StringBuffer pattern= new StringBuffer(shorter.length() + 2);
		String longestCommonSubstring= ""; //$NON-NLS-1$

		for (int i= 0; i < minLength; i++) {
			for (int j= i + 1; j <= minLength; j++) {
				if (j - i < longestCommonSubstring.length())
					continue;

				String substring= shorter.substring(i, j);
				pattern.setLength(0);
				pattern.append('*');
				pattern.append(substring);
				pattern.append('*');

				StringMatcher matcher= new StringMatcher(pattern.toString(), true, false);
				if (matcher.match(longer))
					longestCommonSubstring= substring;
			}
		}

		return longestCommonSubstring;
	}

	/**
	 * Copied from JDT
	 */
	private Image getImage(ImageDescriptor descriptor) {
		return (descriptor == null) ? null : CUIPlugin.getImageDescriptorRegistry().get(descriptor);
	}

}
