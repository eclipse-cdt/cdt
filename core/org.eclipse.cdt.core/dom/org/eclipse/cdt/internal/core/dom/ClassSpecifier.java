package org.eclipse.cdt.internal.core.dom;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ClassSpecifier extends TypeSpecifier implements IScope, IOffsetable, IAccessable {

	private String classKeyImage = null;
    private AccessSpecifier access = new AccessSpecifier( AccessSpecifier.v_private );
	private ClassKey key = new ClassKey();
	private int startingOffset = 0, totalLength = 0, nameOffset = 0;
	private int topLine = 0, bottomLine = 0; 

	public int getClassKey() { return key.getClassKey(); }

	public ClassSpecifier(int classKey, TypeSpecifier.IOwner declaration) {
		super(declaration);
		this.key.setClassKey(classKey);
		if( classKey == ClassKey.t_class )
			classKeyImage = "class";
		else if( classKey == ClassKey.t_struct )
			classKeyImage = "struct";
		else if( classKey == ClassKey.t_union )
			classKeyImage = "union";
	}
	
	private String name;
	public void setName(String n) { name = n; }
	public String getName() { return name; }
	
	private List baseSpecifiers = new LinkedList();
	public void addBaseSpecifier(BaseSpecifier baseSpecifier) {
		baseSpecifiers.add(baseSpecifier);
	}
	public List getBaseSpecifiers() { return Collections.unmodifiableList(baseSpecifiers); }
	
	private List declarations = new LinkedList();
	
	public void addDeclaration(Declaration declaration) {
		declarations.add(declaration);
	}

	public List getDeclarations() {
		return Collections.unmodifiableList( declarations );
	}
	/**
	 * @return int
	 */
	public int getVisibility() {
		return access.getAccess();
	}

	/**
	 * Sets the currentVisiblity.
	 * @param currentVisiblity The currentVisiblity to set
	 */
	public void setVisibility(int currentVisiblity) {
		access.setAccess(currentVisiblity);
	}

	/**
	 * @return
	 */
	public int getStartingOffset() {
		return startingOffset;
	}

	public int getClassKeyEndOffset()
	{
		return startingOffset + classKeyImage.length();
	}
	/**
	 * @return
	 */
	public int getTotalLength() {
		return totalLength;
	}

	/**
	 * @param i
	 */
	public void setStartingOffset(int i) {
		startingOffset = i;
	}

	/**
	 * @param i
	 */
	public void setTotalLength(int i) {
		totalLength = i;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsetable#setTopLine(int)
	 */
	public void setTopLine(int lineNumber) {
		topLine = lineNumber;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsetable#setBottomLine(int)
	 */
	public void setBottomLine(int lineNumber) {
		bottomLine = lineNumber;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsetable#getTopLine()
	 */
	public int getTopLine() {
		return topLine;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsetable#getBottomLine()
	 */
	public int getBottomLine() {
		return bottomLine;
	}

    /**
     * @return
     */
    public String getClassKeyImage()
    {
        return classKeyImage;
    }

    /**
     * @return
     */
    public int getNameOffset()
    {
        return nameOffset;
    }

    /**
     * @param i
     */
    public void setNameOffset(int i)
    {
        nameOffset = i;
    }

}
