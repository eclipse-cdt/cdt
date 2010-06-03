/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.presentation;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;

/**
 * A source tag based on C Model elements.
 */
public class CSourceTag implements ISourceTag {

	/**
	 * The zero-length source range.
	 */
	public class NullRange implements ISourceRange {

		public boolean contains(int offset) {
			return false;
		}

		public int getBeginOffset() {
			return 0;
		}

		public int getEndOffset() {
			return -1;
		}

		public int compareTo(ISourceRange other) {
			if (this == other) {
				return 0;
			}
			return -1;
		}

	}

	/**
	 * The source range.
	 */
	public class CSourceRange implements ISourceRange {

		private org.eclipse.cdt.core.model.ISourceRange fRange;

		/**
		 * @param sourceRange
		 */
		public CSourceRange(org.eclipse.cdt.core.model.ISourceRange sourceRange) {
			fRange= sourceRange;
		}

		public boolean contains(int offset) {
			return fRange.getStartPos() <= offset && offset - fRange.getStartPos() < fRange.getLength();
		}

		public int getBeginOffset() {
			return fRange.getStartPos();
		}

		public int getEndOffset() {
			return fRange.getStartPos() + fRange.getLength() - 1;
		}

		public int compareTo(ISourceRange other) {
			int delta= this.getBeginOffset() - other.getBeginOffset();
			if (delta == 0) {
				delta= this.getEndOffset() - other.getEndOffset();
			}
			return delta;
		}

	}

	/**
	 * The identifier range.
	 */
	public class CIdentifierRange implements ISourceRange {

		private org.eclipse.cdt.core.model.ISourceRange fRange;

		public CIdentifierRange(org.eclipse.cdt.core.model.ISourceRange sourceRange) {
			fRange= sourceRange;
		}

		public boolean contains(int offset) {
			return fRange.getIdStartPos() <= offset && offset - fRange.getIdStartPos() < fRange.getIdLength();
		}

		public int getBeginOffset() {
			return fRange.getIdStartPos();
		}

		public int getEndOffset() {
			return fRange.getIdStartPos() + fRange.getIdLength() - 1;
		}

		public int compareTo(ISourceRange other) {
			int delta= this.getBeginOffset() - other.getBeginOffset();
			if (delta == 0) {
				delta= this.getEndOffset() - other.getEndOffset();
			}
			return delta;
		}

	}
	
	private ISourceReference fReference;
	private int fType;

	/**
	 * Create a new source tag for the given element and type.
	 * 
	 * @param element
	 * @param elementType
	 */
	public CSourceTag(ISourceReference element, int elementType) {
		fReference= element;
		fType= elementType;
	}

	public ISourceRange getFullRange() {
		try {
			return new CSourceRange(fReference.getSourceRange());
		} catch (CModelException e) {
		}
		return new NullRange();
	}

	public String getName() {
		return ((ICElement)fReference).getElementName();
	}

	public String getQualifiedName() {
		return getName();
	}

	public ISourceRange getRangeOfIdentifier() {
		try {
			return new CIdentifierRange(fReference.getSourceRange());
		} catch (CModelException e) {
		}
		return new NullRange();
	}

	public long getSnapshotTime() {
		return 0;
	}

	public int getStyleCode() {
		switch (fType) {
		case ICElement.C_METHOD :
		case ICElement.C_METHOD_DECLARATION:
		case ICElement.C_TEMPLATE_METHOD:
		case ICElement.C_TEMPLATE_METHOD_DECLARATION:
			return ISourceTag.STYLE_Method;
		case ICElement.C_FUNCTION:
		case ICElement.C_FUNCTION_DECLARATION:
		case ICElement.C_TEMPLATE_FUNCTION:
		case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
			return ISourceTag.STYLE_Function;
		case ICElement.C_FIELD :
			return ISourceTag.STYLE_MemberVariable;
		case ICElement.C_VARIABLE:
		case ICElement.C_VARIABLE_DECLARATION:
			return ISourceTag.STYLE_Variable;
		case ICElement.C_CLASS:
		case ICElement.C_TEMPLATE_CLASS:
		case ICElement.C_TEMPLATE_CLASS_DECLARATION:
			return ISourceTag.STYLE_Class;
		case ICElement.C_STRUCT:
		case ICElement.C_TEMPLATE_STRUCT:
		case ICElement.C_TEMPLATE_STRUCT_DECLARATION:
			return ISourceTag.STYLE_Struct;
		case ICElement.C_UNION:
		case ICElement.C_TEMPLATE_UNION:
		case ICElement.C_TEMPLATE_UNION_DECLARATION:
			return ISourceTag.STYLE_Union;
		case ICElement.C_ENUMERATION:
			return ISourceTag.STYLE_Enumeration;
		case ICElement.C_ENUMERATOR:
			return ISourceTag.STYLE_Enumerator;
		case ICElement.C_NAMESPACE:
			return ISourceTag.STYLE_None;
		case ICElement.C_TYPEDEF:
			return ISourceTag.STYLE_Typedef;
		case ICElement.C_MACRO:
			return ISourceTag.STYLE_Macro;
		default:
			return ISourceTag.STYLE_None;
		}
	}

	public ISourceTag getSourceTagAdapter() {
		return this;
	}

}
