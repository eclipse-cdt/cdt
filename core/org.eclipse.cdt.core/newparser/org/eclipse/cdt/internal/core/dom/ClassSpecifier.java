package org.eclipse.cdt.internal.core.dom;

import java.util.LinkedList;
import java.util.List;

public class ClassSpecifier extends TypeSpecifier {

	public static final int t_class = 0;
	public static final int t_struct = 1;
	public static final int t_union = 2;

	private final int classKey;
	public int getClassKey() { return classKey; }

	public ClassSpecifier(int classKey, SimpleDeclaration declaration) {
		super(declaration);
		this.classKey = classKey;
	}
	
	private Name name;
	public void setName(Name n) { name = n; }
	public Name getName() { return name; }
	
	private List baseSpecifiers = new LinkedList();
	public void addBaseSpecifier(BaseSpecifier baseSpecifier) {
		baseSpecifiers.add(baseSpecifier);
	}
	public List getBaseSpecifiers() { return baseSpecifiers; }
	
	private List memberDeclarations = new LinkedList();
	public void addMemberDeclaration(MemberDeclaration memberDeclaration) {
		memberDeclarations.add(memberDeclaration);
	}
	public List getMemberDeclarations() { return memberDeclarations; }
}
