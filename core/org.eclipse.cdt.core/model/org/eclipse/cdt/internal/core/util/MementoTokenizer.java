/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.util;

import org.eclipse.cdt.internal.core.model.CElement;

/**
 * A tokenizer to decipher a C element memento string.
 *
 * @since 5.0
 */
public class MementoTokenizer {
	private static final String CPROJECT = Character.toString(CElement.CEM_CPROJECT);
	private static final String SOURCEROOT = Character.toString(CElement.CEM_SOURCEROOT);
	private static final String SOURCEFOLDER = Character.toString(CElement.CEM_SOURCEFOLDER);
	private static final String TRANSLATIONUNIT = Character.toString(CElement.CEM_TRANSLATIONUNIT);
	private static final String SOURCEELEMENT = Character.toString(CElement.CEM_SOURCEELEMENT);
	private static final String ELEMENTTYPE = Character.toString(CElement.CEM_ELEMENTTYPE);
	private static final String PARAMETER = Character.toString(CElement.CEM_PARAMETER);

	private final char[] memento;
	private final int length;
	private int index = 0;

	public MementoTokenizer(String memento) {
		this.memento = memento.toCharArray();
		this.length = this.memento.length;
	}

	public boolean hasMoreTokens() {
		return this.index < this.length;
	}

	public String nextToken() {
		int start = this.index;
		StringBuilder buffer = null;
		switch (this.memento[this.index++]) {
		case CElement.CEM_ESCAPE:
			buffer = new StringBuilder();
			buffer.append(this.memento[this.index]);
			start = ++this.index;
			break;
		case CElement.CEM_CPROJECT:
			return CPROJECT;
		case CElement.CEM_SOURCEROOT:
			return SOURCEROOT;
		case CElement.CEM_SOURCEFOLDER:
			return SOURCEFOLDER;
		case CElement.CEM_TRANSLATIONUNIT:
			return TRANSLATIONUNIT;
		case CElement.CEM_SOURCEELEMENT:
			return SOURCEELEMENT;
		case CElement.CEM_ELEMENTTYPE:
			return ELEMENTTYPE;
		case CElement.CEM_PARAMETER:
			return PARAMETER;
		}
		loop: while (this.index < this.length) {
			switch (this.memento[this.index]) {
			case CElement.CEM_ESCAPE:
				if (buffer == null)
					buffer = new StringBuilder();
				buffer.append(this.memento, start, this.index - start);
				start = ++this.index;
				break;
			case CElement.CEM_CPROJECT:
			case CElement.CEM_TRANSLATIONUNIT:
			case CElement.CEM_SOURCEROOT:
			case CElement.CEM_SOURCEFOLDER:
			case CElement.CEM_SOURCEELEMENT:
			case CElement.CEM_ELEMENTTYPE:
			case CElement.CEM_PARAMETER:
				break loop;
			}
			this.index++;
		}
		if (buffer != null) {
			buffer.append(this.memento, start, this.index - start);
			return buffer.toString();
		} else {
			return new String(this.memento, start, this.index - start);
		}
	}

}
