package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

class PDOMCPPFriend extends PDOMNode {

	private static final int FRIEND_SPECIFIER = PDOMNode.RECORD_SIZE + 0;
	private static final int NEXT_FRIEND = PDOMNode.RECORD_SIZE + 4;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMNode.RECORD_SIZE + 8;

	public PDOMCPPFriend(PDOM pdom, int record) {
		super(pdom, record);
	}
	
	public PDOMCPPFriend(PDOM pdom, PDOMName friendSpec) throws CoreException {
		super(pdom, null);
		Database db = pdom.getDB();

		int friendrec = friendSpec != null ? friendSpec.getRecord() : 0;
		db.putInt(record + FRIEND_SPECIFIER, friendrec);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_FRIEND_DECLARATION;
	}

	public PDOMName getSpecifierName() throws CoreException {
		int rec = pdom.getDB().getInt(record + FRIEND_SPECIFIER);
		if (rec != 0) return new PDOMName(pdom, rec);
		return null;
	}
	
	public IBinding getFriendSpecifier() {
		PDOMName friendSpecName;
		try {
			friendSpecName = getSpecifierName();
			if (friendSpecName != null) {
				return friendSpecName.getBinding();
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
	
	public void setNextFriend(PDOMCPPFriend nextFriend) throws CoreException {
		int rec = nextFriend != null ? nextFriend.getRecord() : 0;
		pdom.getDB().putInt(record + NEXT_FRIEND, rec);
	}
	
	public PDOMCPPFriend getNextFriend() throws CoreException {
		int rec = pdom.getDB().getInt(record + NEXT_FRIEND);
		return rec != 0 ? new PDOMCPPFriend(pdom, rec) : null;
	}

	public void delete() throws CoreException {
		pdom.getDB().free(record);
	}
	
}
