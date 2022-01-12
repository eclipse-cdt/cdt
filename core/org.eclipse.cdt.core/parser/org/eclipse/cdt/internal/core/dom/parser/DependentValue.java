/*******************************************************************************
 * Copyright (c) 2016 Nathan Ridge and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents the value of an expression, which is not simplified yet,
 * usually because it depends on the value of a template parameter.
 */
public class DependentValue implements IValue {
	public static final int MAX_RECURSION_DEPTH = 25;

	private ICPPEvaluation fEvaluation;
	private char[] fSignature;

	private DependentValue(ICPPEvaluation evaluation) {
		fEvaluation = evaluation;
	}

	@Override
	public final Number numberValue() {
		return null;
	}

	@Override
	public final ICPPEvaluation getEvaluation() {
		return fEvaluation;
	}

	@Override
	public final char[] getSignature() {
		if (fSignature == null) {
			fSignature = fEvaluation.getSignature();
		}
		return fSignature;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buf) throws CoreException {
		buf.putShort(ITypeMarshalBuffer.DEPENDENT_VALUE);
		fEvaluation.marshal(buf, true);
	}

	public static IValue unmarshal(short firstBytes, ITypeMarshalBuffer buf) throws CoreException {
		ICPPEvaluation eval = buf.unmarshalEvaluation();
		if (eval != null)
			return new DependentValue(eval);
		return IntegralValue.UNKNOWN;
	}

	@Override
	public int hashCode() {
		return CharArrayUtils.hash(getSignature());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DependentValue)) {
			return false;
		}
		final DependentValue rhs = (DependentValue) obj;
		return CharArrayUtils.equals(getSignature(), rhs.getSignature());
	}

	/**
	 * For debugging only.
	 */
	@Override
	public String toString() {
		return new String(getSignature());
	}

	/**
	 * Creates a value representing the given template parameter
	 * in the given template.
	 */
	public static DependentValue create(ICPPTemplateDefinition template, ICPPTemplateNonTypeParameter tntp) {
		EvalBinding eval = new EvalBinding(tntp, null, template);
		return new DependentValue(eval);
	}

	/**
	 * Create a value wrapping the given evaluation.
	 */
	public static DependentValue create(ICPPEvaluation eval) {
		return new DependentValue(eval);
	}

	@Override
	public final int numberOfSubValues() {
		return 1;
	}

	@Override
	public final ICPPEvaluation getSubValue(int index) {
		return index == 0 ? fEvaluation : EvalFixed.INCOMPLETE;
	}

	@Override
	public final ICPPEvaluation[] getAllSubValues() {
		return new ICPPEvaluation[] { getEvaluation() };
	}

	@Override
	public void setSubValue(int position, ICPPEvaluation newValue) {
		if (position == 0) {
			fEvaluation = newValue;
		} else {
			throw new IllegalArgumentException("Invalid offset in POD value: " + position); //$NON-NLS-1$
		}
	}

	@Override
	public IValue clone() {
		return new DependentValue(fEvaluation);
	}

	@Override
	public boolean isEquivalentTo(IValue other) {
		if (!(other instanceof DependentValue)) {
			return false;
		}
		DependentValue o = (DependentValue) other;
		return fEvaluation.isEquivalentTo(o.fEvaluation);
	}
}
