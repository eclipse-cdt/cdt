package org.eclipse.dd.dsf.debug;

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.model.IDataModelContext;
import org.eclipse.dd.dsf.model.IDataModelData;
import org.eclipse.dd.dsf.model.IDataModelEvent;
import org.eclipse.dd.dsf.model.IDataModelService;

/**
 * This is just an initial take at the targets interface.
 */
public interface ITargets extends IDataModelService {

    public interface ITargetDMC extends IDataModelContext<ITargetData> {}
    
    public interface ITargetData extends IDataModelData {
        String getName();
        boolean isConnected();
    }
    
    public interface ITargetStateChanged extends IDataModelEvent {}
    
    public interface ICoreDMC extends IDataModelContext<ICoreData> {}

    public interface ICoreData extends IDataModelData {
        String getName();
        boolean isConnected();
        IOS.IOSDMC getOSDMC();
    }

    public interface ICoreStateChanged extends IDataModelEvent {}

    public void getTargets(GetDataDone<ITargetDMC> done);
    public void getCores(ITargetDMC target, GetDataDone<ICoreDMC> done);
    
    public void connectTarget(ITargetDMC targetDmc, Done done);
    public void disconnectTarget(ITargetDMC targetDmc, Done done);
    public void connectCore(ITargetDMC targetDmc, Done done);
    public void disconnectCore(ITargetDMC targetDmc, Done done);
    
}
