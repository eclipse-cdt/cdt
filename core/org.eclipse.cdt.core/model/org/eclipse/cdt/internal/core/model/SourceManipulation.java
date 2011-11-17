/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.cdt.core.model.ISourceManipulation;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITemplate;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.util.MementoTokenizer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Abstract class for C elements which implement ISourceReference.
 */

public class SourceManipulation extends Parent implements ISourceManipulation, ISourceReference {

	/**
	 * An empty list of Strings
	 */
	protected static final String[] fgEmptyStrings = {};
	private boolean fIsActive= true;
	private short fIndex= 0;

	public SourceManipulation(ICElement parent, String name, int type) {
		super(parent, name, type);
	}

	/**
	 * @see ISourceManipulation
	 */
	@Override
	public void copy(ICElement container, ICElement sibling, String rename, boolean force,
		IProgressMonitor monitor) throws CModelException {
		if (container == null) {
			throw new IllegalArgumentException(CoreModelMessages.getString("operation.nullContainer")); //$NON-NLS-1$
		}
		ICElement[] elements= new ICElement[] {this};
		ICElement[] containers= new ICElement[] {container};
		ICElement[] siblings= null;
		if (sibling != null) {
			siblings= new ICElement[] {sibling};
		}
		String[] renamings= null;
		if (rename != null) {
			renamings= new String[] {rename};
		}
		getCModel().copy(elements, containers, siblings, renamings, force, monitor);
	}

	/**
	 * @see ISourceManipulation
	 */
	@Override
	public void delete(boolean force, IProgressMonitor monitor) throws CModelException {
		ICElement[] elements = new ICElement[] {this};
		getCModel().delete(elements, force, monitor);
	}

	/**
	 * @see ISourceManipulation
	 */
	@Override
	public void move(ICElement container, ICElement sibling, String rename, boolean force,
		IProgressMonitor monitor) throws CModelException {
		if (container == null) {
			throw new IllegalArgumentException(CoreModelMessages.getString("operation.nullContainer")); //$NON-NLS-1$
		}
		ICElement[] elements= new ICElement[] {this};
		ICElement[] containers= new ICElement[] {container};
		ICElement[] siblings= null;
		if (sibling != null) {
			siblings= new ICElement[] {sibling};
		}
		String[] renamings= null;
		if (rename != null) {
			renamings= new String[] {rename};
		}
		getCModel().move(elements, containers, siblings, renamings, force, monitor);
	}

	/**
	 * @see ISourceManipulation
	 */
	@Override
	public void rename(String name, boolean force, IProgressMonitor monitor) throws CModelException {
		if (name == null) {
			throw new IllegalArgumentException("element.nullName"); //$NON-NLS-1$
		}
		ICElement[] elements= new ICElement[] {this};
		ICElement[] dests= new ICElement[] {this.getParent()};
		String[] renamings= new String[] {name};
		getCModel().rename(elements, dests, renamings, force, monitor);
	}

	/**
	 * @see IMember
	 */
	@Override
	public ITranslationUnit getTranslationUnit() {
		try {
			return getSourceManipulationInfo().getTranslationUnit();
		} catch (CModelException e) {
			return null;
		}
	}

	/**
	 * Elements within compilation units and class files have no
	 * corresponding resource.
	 *
	 * @see ICElement
	 */
	public IResource getCorrespondingResource() throws CModelException {
		return null;
	}

	/**
	 * Returns the first parent of the element that is an instance of
	 * IOpenable.
	 */
	@Override
	public IOpenable getOpenableParent() {
		ICElement current = getParent();
		while (current != null){
			if (current instanceof IOpenable){
				return (IOpenable) current;
			}
			current = current.getParent();
		}
		return null;
	}

	/**
	 * @see ISourceReference
	 */
	@Override
	public String getSource() throws CModelException {
		return getSourceManipulationInfo().getSource();
	}

	/**
	 * @see ISourceReference
	 */
	@Override
	public ISourceRange getSourceRange() throws CModelException {
		return getSourceManipulationInfo().getSourceRange();
	}

	/**
	 * @see ICElement
	 */
	@Override
	public IResource getUnderlyingResource() {
		return getParent().getUnderlyingResource();
	}

	@Override
	public IResource getResource() {
		return null;
	}

	@Override
	protected CElementInfo createElementInfo () {
		return new SourceManipulationInfo(this);
	}

	protected SourceManipulationInfo getSourceManipulationInfo() throws CModelException {
		return (SourceManipulationInfo)getElementInfo();
	}

	public boolean isIdentical(SourceManipulation other) throws CModelException{
		return (this.equals(other)
		&& (this.getSourceManipulationInfo().hasSameContentsAs(other.getSourceManipulationInfo())));
	}

	/*
	 * @see CElement#generateInfos
	 */
	@Override
	protected void generateInfos(CElementInfo info, Map<ICElement, CElementInfo> newElements, IProgressMonitor pm) throws CModelException {
		Openable openableParent = (Openable)getOpenableParent();
		if (openableParent == null) {
			return;
		}

		newElements.put(this, info);

		CElementInfo openableParentInfo = (CElementInfo) CModelManager.getDefault().getInfo(openableParent);
		if (openableParentInfo == null) {
			openableParent.generateInfos(openableParent.createElementInfo(), newElements, pm);
		}
	}

	public void setPos(int startPos, int length) {
		try {
			getSourceManipulationInfo().setPos(startPos, length);
		} catch (CModelException e) {
			//
		}
	}

	public void setIdPos(int startPos, int length) {
		try {
			getSourceManipulationInfo().setIdPos(startPos, length);
		} catch (CModelException e) {
			//
		}
	}

	public void setLines(int startLine, int endLine) {
		try {
			getSourceManipulationInfo().setLines(startLine, endLine);
		} catch (CModelException e) {
			//
		}
	}

	/*
	 * @see CElement
	 */
	@Override
	public ICElement getHandleFromMemento(String token, MementoTokenizer memento) {
		switch (token.charAt(0)) {
			case CEM_SOURCEELEMENT:
				if (!memento.hasMoreTokens()) return this;
				token= memento.nextToken();
				// element name
				final String elementName;
				if (token.charAt(0) != CEM_ELEMENTTYPE) {
					elementName= token;
					if (!memento.hasMoreTokens()) return null;
					token= memento.nextToken();
				} else {
					// anonymous
					elementName= ""; //$NON-NLS-1$
				}
				// element type
				if (token.charAt(0) != CEM_ELEMENTTYPE || !memento.hasMoreTokens()) {
					return null;
				}
				String typeString= memento.nextToken();
				int elementType;
				try {
					elementType= Integer.parseInt(typeString);
				} catch (NumberFormatException nfe) {
					CCorePlugin.log(nfe);
					return null;
				}
				token= null;
				// optional: parameters
				String[] mementoParams= {};
				if (memento.hasMoreTokens()) {
					List<String> params= new ArrayList<String>();
					do {
						token= memento.nextToken();
						if (token.charAt(0) != CEM_PARAMETER) {
							break;
						}
						if (!memento.hasMoreTokens()) {
							params.add(""); //$NON-NLS-1$
							token= null;
							break;
						}
						params.add(memento.nextToken());
						token= null;
					} while (memento.hasMoreTokens());
					mementoParams= params.toArray(new String[params.size()]);
				}
 				CElement element= null;
				ICElement[] children;
				try {
					children= getChildren();
				} catch (CModelException exc) {
					CCorePlugin.log(exc);
					return null;
				}
				switch (elementType) {
				case ICElement.C_FUNCTION:
				case ICElement.C_FUNCTION_DECLARATION:
				case ICElement.C_METHOD:
				case ICElement.C_METHOD_DECLARATION:
				case ICElement.C_TEMPLATE_FUNCTION:
				case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
				case ICElement.C_TEMPLATE_METHOD:
				case ICElement.C_TEMPLATE_METHOD_DECLARATION:
					for (ICElement element2 : children) {
							if (elementType == element2.getElementType()
									&& elementName.equals(element2.getElementName())) {
								assert element2 instanceof IFunctionDeclaration;
								String[] functionParams= ((IFunctionDeclaration)element2).getParameterTypes();
								if (Arrays.equals(functionParams, mementoParams)) {
									element= (CElement) element2;
									break;
								}
							}
						}
					break;
				case ICElement.C_TEMPLATE_CLASS:
				case ICElement.C_TEMPLATE_STRUCT:
				case ICElement.C_TEMPLATE_UNION:
					for (ICElement element2 : children) {
							if (elementType == element2.getElementType()
									&& elementName.equals(element2.getElementName())) {
								assert element2 instanceof ITemplate;
								String[] templateParams= ((ITemplate)element2).getTemplateParameterTypes();
								if (Arrays.equals(templateParams, mementoParams)) {
									element= (CElement) element2;
									break;
								}
							}
						}
					break;
				default:
					for (ICElement element2 : children) {
							if (elementType == element2.getElementType()
									&& elementName.equals(element2.getElementName())) {
								element= (CElement) element2;
								break;
							}
						}
					break;
				}
				if (element != null) {
					if (token != null) {
						return element.getHandleFromMemento(token, memento);
					} else {
						return element.getHandleFromMemento(memento);
					}
				}
		}
		return null;
	}

	@Override
	public void getHandleMemento(StringBuilder buff) {
		((CElement)getParent()).getHandleMemento(buff);
		buff.append(getHandleMementoDelimiter());
		escapeMementoName(buff, getElementName());
		buff.append(CEM_ELEMENTTYPE);
		buff.append(Integer.toString(getElementType()));
	}

	@Override
	protected char getHandleMementoDelimiter() {
		return CElement.CEM_SOURCEELEMENT;
	}

	@Override
	public boolean isActive() {
		return fIsActive;
	}

	@Override
	public int getIndex() {
		return fIndex;
	}

	public void setActive(boolean active) {
		fIsActive= active;
	}

	public void setIndex(int i) {
		fIndex= (short) i;
	}

	@Override
	public int hashCode() {
		return Util.combineHashCodes(fIndex, super.hashCode());
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ISourceReference) {
			if (fIndex != ((ISourceReference) other).getIndex())
				return false;
		}
		return super.equals(other);
	}
}
