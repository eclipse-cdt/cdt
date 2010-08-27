/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.model.ext;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IPositionConverter;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.core.model.CElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

abstract class CElementHandle implements ICElementHandle, ISourceReference {
	protected static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final ICElement[] NO_CHILDREN = new ICElement[0];
	
	private ICElement fParent;
	private String fName;
	private int fType;

	private IRegion fRegion;
	private long fTimestamp;

	public CElementHandle(ICElement parent, int type, String name) {
		fParent= parent;
		fType= type;
		// anonymous types are assigned a name in the index, we 
		// undo this here
		if (name.length() > 0 && name.charAt(0)=='{') {
			fName= ""; //$NON-NLS-1$
		}
		else {
			fName= name;
		}
		fRegion= new Region(0,0);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ICElement) {
			return CElement.equals(this, (ICElement) obj);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return CElement.hashCode(this);
	}


	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	public void setRangeOfID(IRegion region, long timestamp) {
		fRegion= region;
		fTimestamp= timestamp;
	}

	public ISourceRange getSourceRange() throws CModelException {
		IRegion region= fRegion;
		ITranslationUnit tu= getTranslationUnit();
		if (tu != null) {
			IPositionConverter converter= CCorePlugin.getPositionTrackerManager().findPositionConverter(tu, fTimestamp);
			if (converter != null) {
				region= converter.historicToActual(region);
			}
		}
		int startpos= region.getOffset();
		int length= region.getLength();
		return new SourceRange(startpos, length);
	}

	public String getSource() throws CModelException {
		return null;
	}
	
	public ITranslationUnit getTranslationUnit() {
		ICElement parent= fParent;
		do {
			if (parent instanceof ITranslationUnit) {
				return (ITranslationUnit) parent;
			}
			parent= parent.getParent();
		}
		while (parent != null);
		return null;
	}
	
	public void accept(ICElementVisitor visitor) throws CoreException {
	}

	public boolean exists() {
		return true;
	}

	public ICElement getAncestor(int ancestorType) {
		return null;
	}

	public ICModel getCModel() {
		return fParent.getCModel();
	}

	public ICProject getCProject() {
		return fParent.getCProject();
	}

	public String getElementName() {
		return fName;
	}

	public int getElementType() {
		return fType;
	}

	public ICElement getParent() {
		return fParent;
	}

	public IPath getPath() {
		return getTranslationUnit().getPath();
	}

	public URI getLocationURI() {
		return getTranslationUnit().getLocationURI();
	}
	
	public IResource getResource() {
		return getTranslationUnit().getResource();
	}

	public IResource getUnderlyingResource() {
		return getResource();
	}

	public boolean isReadOnly() {
		return true;
	}

	public boolean isStructureKnown() throws CModelException {
		return false;
	}

	public void copy(ICElement container, ICElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws CModelException {
	}

	public void delete(boolean force, IProgressMonitor monitor) throws CModelException {
	}

	public void move(ICElement container, ICElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws CModelException {
	}

	public void rename(String name, boolean replace, IProgressMonitor monitor) throws CModelException {
	}
	
	public void setTypeName(String type) throws CModelException {
	}

	public String getTypeName() {
		return ""; //$NON-NLS-1$
	}

	public ICElement[] getChildren() throws CModelException {
		return NO_CHILDREN;
	}

	public List<ICElement> getChildrenOfType(int type) throws CModelException {
		return Collections.emptyList();
	}

	public boolean hasChildren() {
		return false;
	}

	public String[] getExceptions() {
		return EMPTY_STRING_ARRAY;
	}

	public String getParameterInitializer(int pos) {
		return ""; //$NON-NLS-1$
	}

	public String getReturnType() {
		return ""; //$NON-NLS-1$
	}
	
	public boolean isConst() {
		return false;
	}

	public boolean isVolatile() throws CModelException {
		return false;
	}

	public boolean isFriend() throws CModelException {
		return false;
	}
	
	public boolean isInline() throws CModelException {
		return false;
	}

	public boolean isOperator() throws CModelException {
		return false;
	}

	public boolean isPureVirtual() throws CModelException {
		return false;
	}

	public boolean isVirtual() throws CModelException {
		return false;
	}

	public boolean isMutable() throws CModelException {
		return false;
	}

	public String getInitializer() {
		return null;
	}
	
	public boolean isAbstract() throws CModelException {
		return false;
	}

	public ASTAccessVisibility getSuperClassAccess(String name) {
		return ASTAccessVisibility.PUBLIC;
	}

	public String[] getSuperClassesNames() {
		return EMPTY_STRING_ARRAY;
	}
	
	protected String[] extractParameterTypes(IFunction func) throws DOMException {
		IParameter[] params= func.getParameters();
		String[] parameterTypes= new String[params.length];
		for (int i = 0; i < params.length; i++) {
			IParameter param = params[i];
			parameterTypes[i]= ASTTypeUtil.getType(param.getType(), false);
		}
		if (parameterTypes.length == 1 && parameterTypes[0].equals("void")) { //$NON-NLS-1$
			return EMPTY_STRING_ARRAY;
		}
		return parameterTypes;
	}
	
	protected ASTAccessVisibility getVisibility(IBinding binding) {
		if (binding instanceof ICPPMember) {
			ICPPMember member= (ICPPMember) binding;
			switch (member.getVisibility()) {
			case ICPPMember.v_private:
				return ASTAccessVisibility.PRIVATE;
			case ICPPMember.v_protected:
				return ASTAccessVisibility.PROTECTED;
			case ICPPMember.v_public:
				return ASTAccessVisibility.PUBLIC;
			}
		}
		return ASTAccessVisibility.PUBLIC;
	}

	/**
	 * @see ICElement
	 */
	public String getHandleIdentifier() {
		ICElement cModelElement= mapToModelElement();
		if (cModelElement != null) {
			return cModelElement.getHandleIdentifier();
		}
		return null;
	}

	private ICElement mapToModelElement() {
		try {
			ISourceRange range= getSourceRange();
			return getTranslationUnit().getElementAtOffset(range.getIdStartPos());
		} catch (CModelException exc) {
			return null;
		}
	}

	public boolean isActive() {
		return true;
	}

	public int getIndex() {
		return 0;
	}
}
