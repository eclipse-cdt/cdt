package org.eclipse.cdt.internal.core.build;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;

public class ScannerInfoSaveParticipant implements ISaveParticipant {

	private static ScannerInfoSaveParticipant instance;
	private Set<ScannerInfoData> toBeSaved = new HashSet<>();

	public ScannerInfoSaveParticipant() {
		assert instance == null;
		instance = this;
	}

	public static ScannerInfoSaveParticipant getInstance() {
		return instance;
	}

	public void save(ScannerInfoData info) {
		toBeSaved.add(info);
	}

	@Override
	public void doneSaving(ISaveContext context) {
	}

	@Override
	public void prepareToSave(ISaveContext context) throws CoreException {
	}

	@Override
	public void rollback(ISaveContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saving(ISaveContext context) throws CoreException {
		for (ScannerInfoData info : toBeSaved) {
			info.save();
		}
		toBeSaved.clear();
	}

}
