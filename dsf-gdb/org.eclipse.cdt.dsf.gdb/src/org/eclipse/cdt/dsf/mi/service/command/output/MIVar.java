/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *     Vladimir Prus (Mentor Graphics) - Add getRawFields method.
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;


/**
 * GDB/MI var-list-children
 * -var-list-children var2
 *  ^done,numchild="6",children={child={name="var2.0",exp="0",numchild="0",type="char"},child={name="var2.1",exp="1",numchild="0",type="char"},child={name="var2.2",exp="2",numchild="0",type="char"},child={name="var2.3",exp="3",numchild="0",type="char"},child={name="var2.4",exp="4",numchild="0",type="char"},child={name="var2.5",exp="5",numchild="0",type="char"}}
 *  
 * -var-list-children var3 
 *  ^done,numchild="3",displayhint="array",children=[child={name="var6.[0].[1]",exp="[1]",numchild="0",type="std::basic_string<char, std::char_traits<char>, std::allocator<char> >",thread-id="1"\
,displayhint="string",dynamic="1"},child={name="var6.[0].[2]",exp="[2]",numchild="0",type="std::basic_string<char, std::char_traits<char>, std::allocator<char> >",thread-id="1",displayhint="string",dy\
namic="1"},child={name="var6.[0].[3]",exp="[3]",numchild="0",type="std::basic_string<char, std::char_traits<char>, std::allocator<char> >",thread-id="1",displayhint="string",dynamic="1"}],has_more="0"\
 */
public class MIVar {

    MITuple raw;

    String name = ""; //$NON-NLS-1$
    String type = ""; //$NON-NLS-1$
    String value = ""; //$NON-NLS-1$
    String exp = ""; //$NON-NLS-1$
	private boolean isDynamic = false;
    int numchild;
	private boolean hasMore = false;
	private MIDisplayHint displayHint = MIDisplayHint.NONE;

	/**
	 * Construct from the raw MI tuple. This is the preferred constructor, since calling all others
	 * will require caller to reimplement all or parts of parsing.
	 */
    public MIVar(MITuple tuple) {
    	this.raw = tuple;
        parse(tuple);
    }

    public MIVar(String n, int num, String t) {
    	this(n, false, num, false, t, MIDisplayHint.NONE);
    }

	/**
	 * @param n
	 * @param isDynamic
	 * @param num
	 *            If isDynamic is true, the number of children currently fetched
	 *            by gdb.
	 * @param hasMore
	 *            If isDynamic is true, whether there are more children
	 *            available than just <code>num</code>.
	 * @param t
	 * 
	 * @since 4.0
	 */
	public MIVar(String n, boolean isDynamic, int num, boolean hasMore, String t) {
		this(n, isDynamic, num, hasMore, t, MIDisplayHint.NONE);
    }

	/**
	 * @param n
	 * @param isDynamic
	 * @param num
	 *            If isDynamic is true, the number of children currently fetched
	 *            by gdb.
	 * @param hasMore
	 *            If isDynamic is true, whether there are more children
	 *            available than just <code>num</code>.
	 * @param t
	 * @param displayHint
	 * @since 4.0
	 */
	public MIVar(String n, boolean isDynamic, int num, boolean hasMore, String t, MIDisplayHint displayHint) {
        name = n;
		this.isDynamic = isDynamic;
        numchild = num;
		this.hasMore = hasMore;
        type = t;
        this.displayHint = displayHint;
    }

    /** Return raw fields from MI.
	 * @since 4.7
	 */
    public MITuple getRawFields()
    {
    	assert raw != null;
    	return raw;
    }

    public String getVarName() {
        return name;
    }

    public String getType() {
        return type;
    }

    /**
     * @since 4.6
     */
    public String getValue() {
        return value;
    }

	/**
	 * @return Whether the value and children of this variable are provided
	 *         by a pretty printer.
	 *         
	 * @since 4.0
	 */
    public boolean isDynamic() {
		return isDynamic;
	}

	/**
	 * @return The number of children. If {@link #isDynamic()} returns true,
	 *         the returned value only reflects the number of children currently
	 *         fetched by gdb. Check {@link #hasMore()} in order to find out
	 *         whether the are more children. 
	 */
    public int getNumChild() {
        return numchild;
    }

	/**
	 * @return For dynamic varobjs ({@link #isDynamic() returns true} this
	 *         method returns whether there are children in addition to the
	 *         currently fetched, i.e. whether there are more children than
	 *         {@link #getNumChild()} returns.
	 *         
	 * @since 4.0
	 */
	public boolean hasMore() {
		return hasMore;
	}

    public String getExp() {
        return exp;
    }

	/**
	 * @return Whether the underlying value conceptually represents a string,
	 *         array, or map.
	 *         
	 * @since 4.0
	 */
    public MIDisplayHint getDisplayHint() {
    	return displayHint;
    }
    
    void parse(MITuple tuple) {
        MIResult[] results = tuple.getMIResults();
        for (int i = 0; i < results.length; i++) {
            String var = results[i].getVariable();
            MIValue value = results[i].getMIValue();
            String str = ""; //$NON-NLS-1$
            if (value != null && value instanceof MIConst) {
                str = ((MIConst)value).getString();
            }

            if (var.equals("numchild")) { //$NON-NLS-1$
                try {
                    numchild = Integer.parseInt(str.trim());
                } catch (NumberFormatException e) {
                }
            } else if (var.equals("name")) { //$NON-NLS-1$
                name = str;
            } else if (var.equals("type")) { //$NON-NLS-1$
                type = str;
            } else if (var.equals("value")) { //$NON-NLS-1$
                this.value = str;
            } else if (var.equals("exp")) { //$NON-NLS-1$
                exp = str;
			} else if (var.equals("dynamic") && str.trim().equals("1")) { //$NON-NLS-1$ //$NON-NLS-2$
				isDynamic = true;
			} else if (var.equals("has_more") && str.trim().equals("1")) { //$NON-NLS-1$ //$NON-NLS-2$
				hasMore = true;
            } else if (var.equals("displayhint")) { //$NON-NLS-1$
            	displayHint = new MIDisplayHint(str);
            }
        }
    }
}
