/*******************************************************************************
 * Copyright (c) 2002, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * @deprecated Do not use.
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface ITokenDuple {
	public abstract IToken getFirstToken();

	public abstract IToken getLastToken();

	public List<IASTNode>[] getTemplateIdArgLists();

	public ITokenDuple getLastSegment();

	public ITokenDuple getLeadingSegments();

	public int getSegmentCount();

	public abstract Iterator<IToken> iterator();

	@Override
	public abstract String toString();

	public char[] toCharArray();

	public abstract int length();

	public IToken getToken(int index);

	public ITokenDuple[] getSegments();

	public int getStartOffset();

	public int getEndOffset();

	public char[] extractNameFromTemplateId();
}
