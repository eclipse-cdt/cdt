/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mohamed Azab (Mentor Graphics) - Bug 438549. Add mechanism for parameter guessing.
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
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
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.Context;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.UDCMode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Cost;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;

/**
 * This class is based on org.eclipse.jdt.internal.ui.text.java.ParameterGuesser
 *
 * This class produces a logically-ordered list of applicable variables for later use as parameter guessing
 * proposals for a function parameter.
 */
public class ParameterGuesser {
	private final Set<String> fAlreadyMatchedNames = new HashSet<>();

	/**
	 * Variable type. Used to choose the best guess based on scope (LOCAL is preferred over FIELD,
	 * which is preferred over GLOBAL).
	 */
	static enum VariableType {
		LOCAL(0), FIELD(1), GLOBAL(3); // Give the global variables a lower priority.

		private final int priority;

		private VariableType(int priority) {
			this.priority = priority;
		}

		public int getPriority() {
			return priority;
		}
	}

	private final static class Variable {
		public final String name;
		public final VariableType variableType;
		public final int positionScore;

		public int totalScore;

		public final char[] triggerChars;
		public final ImageDescriptor descriptor;

		public boolean alreadyMatched;

		public Variable(String name, VariableType variableType, int positionScore, char[] triggerChars,
				ImageDescriptor descriptor) {
			this.name = name;
			this.variableType = variableType;
			this.positionScore = positionScore;
			this.triggerChars = triggerChars;
			this.descriptor = descriptor;
		}
	}

	private Collection<Variable> evaluateVisibleMatches(IType expectedType, List<IBinding> suggestions)
			throws CModelException {
		Set<Variable> res = new HashSet<>();
		int size = suggestions.size();
		for (int i = 0; i < size; i++) {
			Variable variable = createVariable(suggestions.get(i), expectedType, i);
			if (variable != null) {
				if (fAlreadyMatchedNames.contains(variable.name)) {
					variable.alreadyMatched = true;
				}
				res.add(variable);
			}
		}
		return res;
	}

	private boolean isAnonymousBinding(IBinding binding) {
		char[] name = binding.getNameCharArray();
		return name.length == 0 || name[0] == '{';
	}

	protected IType getType(IBinding binding) {
		if (!isAnonymousBinding(binding) && binding instanceof IVariable)
			return ((IVariable) binding).getType();
		return null;
	}

	private Variable createVariable(IBinding element, IType enclosingType, int positionScore) throws CModelException {
		IType elementType = getType(element);
		String elementName = element.getName();
		if (elementType != null && enclosingType != null
				&& (elementType.toString().equals(enclosingType.toString()) || elementType.isSameType(enclosingType)
						|| isImplicitlyConvertible(enclosingType, elementType) || isParent(elementType, enclosingType)
						|| isReferenceTo(enclosingType, elementType) || isReferenceTo(elementType, enclosingType))) {
			VariableType variableType = VariableType.GLOBAL;
			if (element instanceof ICPPField) {
				variableType = VariableType.FIELD;

			} else if (element instanceof IVariable) {
				try {
					if (element instanceof ICPPBinding && ((ICPPBinding) element).isGloballyQualified()) {
						variableType = VariableType.GLOBAL;
					} else {
						variableType = VariableType.LOCAL;
					}
				} catch (DOMException e) {
				}
			}

			// Handle reference case
			if (isReferenceTo(enclosingType, elementType))
				elementName = "&" + elementName; //$NON-NLS-1$
			else if (isReferenceTo(elementType, enclosingType))
				elementName = "*" + elementName; //$NON-NLS-1$
			return new Variable(elementName, variableType, positionScore, CharArrayUtils.EMPTY_CHAR_ARRAY,
					getImageDescriptor(element));
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

	private boolean isImplicitlyConvertible(IType orginType, IType candidateType) {
		try {
			Cost cost = Conversions.checkImplicitConversionSequence(orginType, candidateType, ValueCategory.LVALUE,
					UDCMode.ALLOWED, Context.ORDINARY);
			if (cost.converts())
				return true;
		} catch (DOMException e) {
			return false;
		}
		return false;
	}

	/**
	 * Returns true, if the parent type is a direct/indirect parent of the child type
	 */
	private boolean isParent(IType child, IType parent) {
		if (child != null && parent != null && child instanceof ICPPClassType
				&& !(child instanceof ICPPClassSpecialization) && parent instanceof ICPPClassType
				&& !(parent instanceof ICPPClassSpecialization)) {
			ICPPBase[] bases = ((ICPPClassType) child).getBases();
			for (ICPPBase base : bases) {
				IType tmpType = base.getBaseClassType();
				if (tmpType.toString().equals(parent.toString()) || tmpType.isSameType(parent)
						|| isParent(tmpType, parent))
					return true;
			}
		}

		return false;
	}

	private ImageDescriptor getImageDescriptor(IBinding binding) {
		ImageDescriptor imageDescriptor = null;

		if (binding instanceof ITypedef) {
			imageDescriptor = CElementImageProvider.getTypedefImageDescriptor();
		} else if (binding instanceof ICompositeType) {
			if (((ICompositeType) binding).getKey() == ICPPClassType.k_class || binding instanceof ICPPClassTemplate)
				imageDescriptor = CElementImageProvider.getClassImageDescriptor();
			else if (((ICompositeType) binding).getKey() == ICompositeType.k_struct)
				imageDescriptor = CElementImageProvider.getStructImageDescriptor();
			else if (((ICompositeType) binding).getKey() == ICompositeType.k_union)
				imageDescriptor = CElementImageProvider.getUnionImageDescriptor();
		} else if (binding instanceof ICPPMethod) {
			switch (((ICPPMethod) binding).getVisibility()) {
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
			switch (((ICPPField) binding).getVisibility()) {
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
	 * Returns the matches for the type and name argument, ordered by match quality.
	 *
	 * @param expectedType the qualified type of the parameter we are trying to match
	 * @param paramName the name of the parameter (used to find similarly named matches)
	 * @param pos the position
	 * @param suggestions the suggestions or <code>null</code>
	 * @param isLastParameter <code>true</code> iff this proposal is for the last parameter of a method
	 * @return returns the name of the best match, or <code>null</code> if no match found
	 */
	public ICompletionProposal[] parameterProposals(IType expectedType, String paramName, Position pos,
			List<IBinding> suggestions, boolean isLastParameter) throws CModelException {
		List<Variable> typeMatches = new ArrayList<>(evaluateVisibleMatches(expectedType, suggestions));
		orderMatches(typeMatches, paramName);

		ICompletionProposal[] ret = new ICompletionProposal[typeMatches.size()];
		int i = 0;
		int replacementLength = 0;
		for (Variable v : typeMatches) {
			if (i == 0) {
				fAlreadyMatchedNames.add(v.name);
				replacementLength = v.name.length();
			}

			String displayString = v.name;

			final char[] triggers;
			if (isLastParameter) {
				triggers = v.triggerChars;
			} else {
				triggers = new char[v.triggerChars.length + 1];
				System.arraycopy(v.triggerChars, 0, triggers, 0, v.triggerChars.length);
				triggers[triggers.length - 1] = ',';
			}
			ret[i++] = new PositionBasedCompletionProposal(v.name, pos, replacementLength, getImage(v.descriptor),
					displayString, null, null, triggers);
		}
		return ret;
	}

	private static class MatchComparator implements Comparator<Variable> {
		@Override
		public int compare(Variable one, Variable two) {
			return two.totalScore - one.totalScore;
		}
	}

	/**
	 * Determines the best match of all possible type matches. The input into this method is all possible
	 * completions that match the type of the argument. The purpose of this method is to choose among them
	 * based on the following simple rules:
	 *
	 * 1) Local Variables > Instance/Class Variables > Inherited Instance/Class Variables
	 *
	 * 2) A longer case insensitive substring match will prevail
	 *
	 * 3) Variables that have not been used already during this completion will prevail over those that have
	 * already been used (this avoids the same String/int/char from being passed in for multiple arguments)
	 *
	 * 4) A better source position score will prevail (the declaration point of the variable, or
	 * "how close to the point of completion?"
	 *
	 * @param typeMatches
	 *            the list of type matches
	 * @param paramName
	 *            the parameter name
	 */
	private static void orderMatches(List<Variable> typeMatches, String paramName) {
		if (typeMatches != null) {
			calculateVariablesScores(paramName, typeMatches);
			Collections.sort(typeMatches, new MatchComparator());
		}
	}

	/**
	 * Set the replacement scores of the variables with respect to the parameter name.
	 */
	private static void calculateVariablesScores(String parameterName, List<Variable> variables) {
		for (Variable v : variables) {
			v.totalScore = score(v, parameterName);
		}
	}

	/**
	 * The four order criteria as described below - put already used into bit 10, all others into bits
	 * 0-9, 11-20, 21-30; 31 is sign - always 0
	 *
	 * @param v the variable
	 * @param parameterName the name of the parameter to be replaced.
	 * @return the score for <code>v</code>
	 */
	private static int score(Variable v, String parameterName) {
		int variableScore = 100 - v.variableType.getPriority(); // since these are increasing with distance
		int subStringScore = getContainedString(v.name, parameterName).length();
		// Substring scores under 60% are not considered.
		// This prevents marginal matches like a - ba and false - isBool that will
		// destroy the sort order.
		int shorter = Math.min(v.name.length(), parameterName.length());
		if (subStringScore < 0.6 * shorter)
			subStringScore = 0;

		int positionScore = v.positionScore;
		int matchedScore = v.alreadyMatched ? 0 : 1;

		int score = variableScore << 21 | subStringScore << 11 | matchedScore << 10 | positionScore;

		return score;
	}

	/**
	 * Returns the shorter string if it is a substring of the longer string, or an empty
	 * string otherwise.
	 * @param first the first string
	 * @param second the second string
	 */
	private static String getContainedString(String first, String second) {
		// Now only considering the case where shorter string is part of longer string.
		// TODO: Use a more efficient technique to get the common string (i.e. suffix tree).
		String shorterStr = first.length() < second.length() ? first : second;
		String longerStr = first == shorterStr ? second : first;
		if (longerStr.contains(shorterStr)) {
			return shorterStr;
		} else {
			return ""; //$NON-NLS-1$
		}
	}

	private static Image getImage(ImageDescriptor descriptor) {
		return descriptor == null ? null : CUIPlugin.getImageDescriptorRegistry().get(descriptor);
	}
}
