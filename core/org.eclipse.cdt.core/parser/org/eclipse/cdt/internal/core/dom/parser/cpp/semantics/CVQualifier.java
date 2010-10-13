/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;


/**
 * Represents the possible cv-qualification of a type.
 */
public enum CVQualifier {
	CONST_VOLATILE_RESTRICT(1 | 2 | 4), CONST_VOLATILE(1 | 2), CONST_RESTRICT(1 | 4), CONST(1), 
	VOLATILE_RESTRICT(2 | 4), VOLATILE(2), RESTRICT(4), NONE(0);

	private static final int C = 1;
	private static final int V = 2;
	private static final int R = 4;

	final private int fQualifiers;
	private CVQualifier(int qualifiers) {
		fQualifiers= qualifiers;
	}
	
	public boolean isConst() {
		return (fQualifiers & C) != 0;
	}
	public boolean isVolatile() {
		return (fQualifiers & V) != 0;
	}
	public boolean isRestrict() {
		return (fQualifiers & R) != 0;
	}

	public boolean isAtLeastAsQualifiedAs(CVQualifier other) {
		return (fQualifiers | other.fQualifiers) == fQualifiers;
	}

	public boolean isMoreQualifiedThan(CVQualifier other) {
		return this != other && isAtLeastAsQualifiedAs(other); 
	}

	public CVQualifier add(CVQualifier cvq) {
		return fromQualifier(fQualifiers | cvq.fQualifiers);
	}

	public CVQualifier remove(CVQualifier cvq) {
		return fromQualifier(fQualifiers & ~cvq.fQualifiers);
	}

	private CVQualifier fromQualifier(final int q) {
		switch(q) {
		case C|V|R: return CONST_VOLATILE_RESTRICT;
		case V|R: 	return VOLATILE_RESTRICT;
		case C|R: 	return CONST_RESTRICT;
		case R: 	return RESTRICT;
		case C|V: 	return CONST_VOLATILE;
		case V: 	return VOLATILE;
		case C: 	return CONST;
		case 0: default: return NONE;
		}
	}
	
	/**
	 * [3.9.3-4] Implements cv-qualification (partial) comparison. There is a (partial)
	 * ordering on cv-qualifiers, so that a type can be said to be more
	 * cv-qualified than another.
	 * @return <ul>
	 * <li>7 if cv1 == const volatile restrict cv2 
	 * <li>6 if cv1 == volatile restrict cv2
	 * <li>5 if cv1 == const restrict cv2
	 * <li>4 if cv1 == restrict cv2
	 * <li>3 if cv1 == const volatile cv2 
	 * <li>2 if cv1 == volatile cv2
	 * <li>1 if cv1 == const cv2
	 * <li>EQ 0 if cv1 == cv2
	 * <li>LT -1 if cv1 is less qualified than cv2 or not comparable
	 * </ul>
	 */
	public int partialComparison(CVQualifier cv2) {
		// same qualifications
		if (this == cv2)
			return 0;

		if (!isAtLeastAsQualifiedAs(cv2))
			return -1;
		return fQualifiers-cv2.fQualifiers;
	}
}
