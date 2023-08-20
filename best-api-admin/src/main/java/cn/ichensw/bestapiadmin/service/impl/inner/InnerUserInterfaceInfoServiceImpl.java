package cn.ichensw.bestapiadmin.service.impl.inner;

import cn.ichensw.bestapiadmin.exception.BusinessException;
import cn.ichensw.bestapiadmin.service.UserInterfaceInfoService;
import cn.ichensw.bestapiadmin.service.UserService;
import cn.ichensw.bestapicommon.common.ErrorCode;
import cn.ichensw.bestapicommon.model.entity.UserInterfaceInfo;
import cn.ichensw.bestapicommon.service.InnerUserInterfaceInfoService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.SQLException;

/**
 * @author nero
 * @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
 * @createDate 2023-06-17 12:32:57
 */
@DubboService
public class InnerUserInterfaceInfoServiceImpl implements InnerUserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = SQLException.class)
    public boolean invokeCount(long interfaceInfoId, long userId) {
        // 查询接口是否存在
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.lambdaQuery()
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                .eq(UserInterfaceInfo::getUserId, userId)
                .one();
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口不存在");
        }
        // 修改调用次数
        return userInterfaceInfoService.lambdaUpdate()
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                .eq(UserInterfaceInfo::getUserId, userId)
                .set(UserInterfaceInfo::getTotalNum, userInterfaceInfo.getTotalNum() + 1)
                .set(UserInterfaceInfo::getLeftNum, userInterfaceInfo.getLeftNum() - 1)
                .update();
    }

    @Override
    public UserInterfaceInfo hasLeftNum(Long interfaceId, Long userId) {
        return userInterfaceInfoService.lambdaQuery()
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceId)
                .eq(UserInterfaceInfo::getUserId, userId)
                .one();
    }

    @Override
    public Boolean addDefaultUserInterfaceInfo(Long interfaceId, Long userId) {
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        userInterfaceInfo.setUserId(userId);
        userInterfaceInfo.setInterfaceInfoId(interfaceId);
        userInterfaceInfo.setLeftNum(99999999);

        return userInterfaceInfoService.save(userInterfaceInfo);
    }

}




