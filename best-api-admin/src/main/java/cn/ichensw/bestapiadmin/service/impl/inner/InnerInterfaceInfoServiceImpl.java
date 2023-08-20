package cn.ichensw.bestapiadmin.service.impl.inner;

import cn.ichensw.bestapiadmin.mapper.InterfaceInfoMapper;
import cn.ichensw.bestapiadmin.service.InterfaceInfoService;
import cn.ichensw.bestapicommon.model.entity.InterfaceInfo;
import cn.ichensw.bestapicommon.service.InnerInterfaceInfoService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
* @author nero
*/
@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    InterfaceInfoMapper interfaceInfoMapper;

    @Override
    public InterfaceInfo getInterfaceInfo(String path, String method) {
        return interfaceInfoService.query()
                .eq("url", path)
                .eq("method", method)
                .one();
    }
}




