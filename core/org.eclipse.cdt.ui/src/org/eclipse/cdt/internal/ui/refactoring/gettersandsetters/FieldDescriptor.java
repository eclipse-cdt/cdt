package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

import org.eclipse.cdt.internal.ui.refactoring.gettersandsetters.AccessorDescriptor.AccessorKind;

class FieldDescriptor {
	private final IASTName fieldName;
	private final IASTSimpleDeclaration fieldDeclaration;
	private final AccessorDescriptor getter;
	private final AccessorDescriptor setter;
	private final AccessorDescriptor[] childNodes;
	private final GetterSetterContext context;
	
	FieldDescriptor(IASTSimpleDeclaration fieldDeclaration, GetterSetterContext context) {
		this.fieldName = GetterSetterContext.getDeclarationName(fieldDeclaration);
		this.fieldDeclaration = fieldDeclaration;
		this.context = context;
		Set<String> namesToAvoid = getNamesToAvoid();
		String name = GetterSetterNameGenerator.generateGetterName(fieldName, namesToAvoid);
		this.getter = new AccessorDescriptor(AccessorKind.GETTER, name,	this);
		name = GetterSetterNameGenerator.generateSetterName(fieldName, namesToAvoid);
		 if (!isAssignable(fieldDeclaration))
			 name = null;
		this.setter = new AccessorDescriptor(AccessorKind.SETTER, name,	this);

		List<AccessorDescriptor> children = new ArrayList<AccessorDescriptor>(2);
		if (getter.canBeGenerated()) {
			children.add(getter);
		}
		if (setter.canBeGenerated()) {
			children.add(setter);
		}
		childNodes = children.toArray(new AccessorDescriptor[children.size()]);
	}

	private Set<String> getNamesToAvoid() {
		Set<String> namesToAvoid = new HashSet<String>();
		// Add field names.
		for (IASTSimpleDeclaration fieldDeclaration : context.existingFields) {
			namesToAvoid.add(String.valueOf(GetterSetterContext.getDeclarationName(fieldDeclaration).getSimpleID()));
		}
		// Add constructor name.
		if (!context.existingFields.isEmpty()) {
			IASTNode node = context.existingFields.get(0).getParent();
			while (!(node instanceof IASTCompositeTypeSpecifier)) {
				node = node.getParent();
			}
			IASTCompositeTypeSpecifier comp = (IASTCompositeTypeSpecifier) node;
			namesToAvoid.add(String.valueOf(comp.getName().getLastName().getSimpleID()));
		}
		return namesToAvoid;
	}

	private static boolean isAssignable(IASTSimpleDeclaration declaration) {
		IASTName name = GetterSetterContext.getDeclarationName(declaration);
		IBinding binding = name.resolveBinding();
		if (!(binding instanceof ICPPField))
			return false;
		ICPPField field = (ICPPField) binding;
		IType type = field.getType();
		type = SemanticUtil.getNestedType(type, SemanticUtil.TDEF);
		if (type instanceof IArrayType || type instanceof ICPPReferenceType)
			return false;
		if (type instanceof IPointerType && ((IPointerType) type).isConst())
			return false;
		if (type instanceof IQualifierType && ((IQualifierType) type).isConst())
			return false;
		return true;
	}

	@Override
	public String toString() {
		return fieldName.toString();
	}

	AccessorDescriptor[] getChildNodes() {
		return childNodes;
	}

	boolean missingGetterOrSetter() {
		return getter.canBeGenerated() || setter.canBeGenerated();
	}

	public GetterSetterContext getContext() {
		return context;
	}

	public IASTName getFieldName() {
		return fieldName;
	}

	public IASTSimpleDeclaration getFieldDeclaration() {
		return fieldDeclaration;
	}

	public AccessorDescriptor getGetter() {
		return getter;
	}

	public AccessorDescriptor getSetter() {
		return setter;
	}
}
