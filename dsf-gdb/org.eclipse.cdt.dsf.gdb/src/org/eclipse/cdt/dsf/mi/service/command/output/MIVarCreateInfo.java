/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * GDB/MI var-create.
 * -var-create "-" * a
 * ^done,name="var1",numchild="0",value="11",type="int"
 * -var-create "-" * buf
 * ^done,name="var1",numchild="6",value=[6]",type="char [6]"
 *
 * Note that the value is returned in the output, as of GDB6.7
 */
public class MIVarCreateInfo extends MIInfo {

	private MIVar child;

	public MIVarCreateInfo(MIOutput record) {
		super(record);
		if (isDone()) {
			MIResultRecord rr = getMIOutput().getMIResultRecord();
			if (rr != null) {
				child = new MIVar(rr.getFields());
			}
		}
	}

	public String getType() {
		return child.getType();
	}

	/**
	 * @return Whether the created variable's value and children are provided
	 *         by a pretty printer.
	 *
	 * @since 4.0
	 */
	public boolean isDynamic() {
		return child.isDynamic();
	}

	/**
	 * @return The number of children. If {@link #isDynamic()} returns true,
	 *         the returned value only reflects the number of children currently
	 *         fetched by gdb. Check {@link #hasMore()} in order to find out
	 *         whether the are more children.
	 */
	public int getNumChildren() {
		return child.getNumChild();
	}

	/**
	 * @return For dynamic varobjs ({@link #isDynamic() returns true} this
	 *         method returns whether there are children in addition to the
	 *         currently fetched, i.e. whether there are more children than
	 *         {@link #getNumChildren()} returns.
	 *
	 * @since 4.0
	 */
	public boolean hasMore() {
		return child.hasMore();
	}

	public String getName() {
		return child.getVarName();
	}

	public String getValue() {
		return child.getValue();
	}

	/**
	 * @return Whether the underlying value conceptually represents a string,
	 *         array, or map.
	 *
	 * @since 4.0
	 */
	public MIDisplayHint getDisplayHint() {
		return child.getDisplayHint();
	}

	public MIVar getMIVar() {
		return child;
	}
}
