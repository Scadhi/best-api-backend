package cn.ichensw.bestapiadmin.controller;

import cn.hutool.json.JSONUtil;
import cn.ichensw.bestapiadmin.annotation.AuthCheck;
import cn.ichensw.bestapiadmin.config.GatewayConfig;
import cn.ichensw.bestapiadmin.config.UserHolder;
import cn.ichensw.bestapiadmin.exception.BusinessException;
import cn.ichensw.bestapiadmin.exception.ThrowUtils;
import cn.ichensw.bestapiadmin.service.InterfaceInfoService;
import cn.ichensw.bestapiadmin.service.UserInterfaceInfoService;
import cn.ichensw.bestapiadmin.service.UserService;
import cn.ichensw.bestapicommon.common.*;
import cn.ichensw.bestapicommon.constant.CommonConstant;
import cn.ichensw.bestapicommon.constant.UserConstant;
import cn.ichensw.bestapicommon.constant.enums.AuditStatusEnum;
import cn.ichensw.bestapicommon.constant.enums.MethodEnum;
import cn.ichensw.bestapicommon.model.dto.interfaceinfo.*;
import cn.ichensw.bestapicommon.model.entity.InterfaceInfo;
import cn.ichensw.bestapicommon.model.entity.User;
import cn.ichensw.bestapicommon.model.entity.UserInterfaceInfo;
import cn.ichensw.bestapicommon.model.enums.InterfaceInfoStatusEnum;
import cn.ichensw.bestapicommon.model.vo.InterfaceInfoVO;
import cn.ichensw.bestclientsdk.client.BestApiClient;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

/**
 * 接口管理
 *
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private GatewayConfig gatewayConfig;

    // region 增删改查

    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @Transactional(rollbackFor = SQLException.class)
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);

        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
        interfaceInfo.setRequestParamsRemark(JSONUtil.toJsonStr(interfaceInfoAddRequest.getRequestParamsRemark()));
        interfaceInfo.setResponseParamsRemark(JSONUtil.toJsonStr(interfaceInfoAddRequest.getResponseParamsRemark()));
        // 需要新增待审核
        interfaceInfo.setAuditStatus(AuditStatusEnum.AUDITING.getCode());
        boolean result = interfaceInfoService.save(interfaceInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newInterfaceInfoId = interfaceInfo.getId();

        // 如果是接口创始人的话，默认开通最调用大次数
        if (loginUser.getId().equals(interfaceInfo.getUserId())) {
            UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
            userInterfaceInfo.setInterfaceInfoId(interfaceInfo.getId());
            userInterfaceInfo.setUserId(loginUser.getId());
            userInterfaceInfo.setLeftNum(99999999);
            userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, true);
            result = userInterfaceInfoService.save(userInterfaceInfo);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        }

        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @Transactional(rollbackFor = SQLException.class)
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param interfaceInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    //@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Transactional(rollbackFor = SQLException.class)
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = interfaceInfoService.updateInterfaceInfo(interfaceInfoUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 审核（仅管理员）
     *
     * @param interfaceInfoAuditRequest
     * @return
     */
    @PostMapping("/audit")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Transactional(rollbackFor = SQLException.class)
    public BaseResponse<Boolean> auditInterfaceInfo(@RequestBody InterfaceInfoAuditRequest interfaceInfoAuditRequest) {
        if (interfaceInfoAuditRequest == null || interfaceInfoAuditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = interfaceInfoService.auditInterfaceInfo(interfaceInfoAuditRequest);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id 接口id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<InterfaceInfoVO> getInterfaceInfoVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVO(interfaceInfo, request));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param interfaceInfoQueryRequest 查询条件
     * @param request                   请求
     * @return 分页列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoVOByPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                         HttpServletRequest request) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        interfaceInfoQueryRequest.setSortField("createTime");
        // 倒序排序
        interfaceInfoQueryRequest.setSortOrder(CommonConstant.SORT_ORDER_DESC);
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVOPage(interfaceInfoPage, request));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param interfaceInfoQueryRequest 查询条件
     * @param request                   请求
     * @return 分页列表
     */
    @PostMapping("/audit/list/page/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listAuditInterfaceInfoVOByPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                         HttpServletRequest request) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        interfaceInfoQueryRequest.setSortField("createTime");
        // 限制必须审核通过
        interfaceInfoQueryRequest.setAuditStatus(AuditStatusEnum.AGREE.getCode());
        // 倒序排序
        interfaceInfoQueryRequest.setSortOrder(CommonConstant.SORT_ORDER_DESC);
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVOPage(interfaceInfoPage, request));
    }
    /**
     * 根据 当前用户ID 分页获取列表（封装类）
     *
     * @param interfaceInfoQueryRequest 查询条件
     * @param request                   请求
     * @return 分页列表
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoVOByUserIdPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                         HttpServletRequest request) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        interfaceInfoQueryRequest.setSortField("createTime");
        // 限制必须审核通过
        interfaceInfoQueryRequest.setAuditStatus(AuditStatusEnum.AGREE.getCode());
        // 倒序排序
        interfaceInfoQueryRequest.setSortOrder(CommonConstant.SORT_ORDER_DESC);
        // 限制爬虫
        ThrowUtils.throwIf(size > 30, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVOByUserIdPage(interfaceInfoPage, request));
    }

    /**
     * 根据 当前用户ID 分页获取当前创建人的列表（封装类）
     *
     * @param interfaceInfoQueryRequest 查询条件
     * @param request                   请求
     * @return 分页列表
     */
    @PostMapping("/own/list/page/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listOwnInterfaceInfoVOByPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                               HttpServletRequest request) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        interfaceInfoQueryRequest.setSortField("createTime");
        // 设置创建人id
        Long userId = UserHolder.getUser().getId();
        interfaceInfoQueryRequest.setUserId(userId);
        // 倒序排序
        interfaceInfoQueryRequest.setSortOrder(CommonConstant.SORT_ORDER_DESC);
        // 限制爬虫
        ThrowUtils.throwIf(size > 30, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
        return ResultUtils.success(interfaceInfoService.getOwnInterfaceInfoVOPage(interfaceInfoPage, request));
    }


    // endregion

    // region 发布下线接口调用
    /**
     * 发布（仅管理员）
     *
     * @param interfaceInfoInvokeRequest 接口信息
     * @return 是否成功
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Transactional(rollbackFor = SQLException.class)
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
                                                     HttpServletRequest request) {
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断接口是否存在
        Long id = interfaceInfoInvokeRequest.getId();
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        // 判断是否是审核通过状态，不然不让更新
        Integer auditStatus = oldInterfaceInfo.getAuditStatus();
        if (!auditStatus.equals(AuditStatusEnum.AGREE.getCode())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "审核不通过，无法发布");
        }
        // 判断是否可以调用
        String requestParams = interfaceInfoInvokeRequest.getRequestParams();
        // 接口请求地址
        String host = oldInterfaceInfo.getHost();
        String url = oldInterfaceInfo.getUrl();
        String method = oldInterfaceInfo.getMethod();
        // 获取SDK客户端
        BestApiClient bestApiClient = interfaceInfoService.getBestApiClient(request);
        // 设置网关地址
        bestApiClient.setGatewayHost(gatewayConfig.getHost());
        try {
            // 执行方法
            String invokeResult = null;
            if (method.equals(MethodEnum.GET.getDesc())) {
                invokeResult = bestApiClient.invokeInterfaceByGet(requestParams, host, url, method);
            } else {
                invokeResult = bestApiClient.invokeInterfaceByPost(requestParams, host, url, method);
            }
            if (StringUtils.isBlank(invokeResult)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口数据为空");
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败");
        }

        // 修改接口状态为 上线状态
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 下线（仅管理员）
     *
     * @param idRequest 接口id
     * @return 是否成功
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Transactional(rollbackFor = SQLException.class)
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断接口是否存在
        Long id = idRequest.getId();
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        // 判断是否是审核通过状态，不然不让更新
        Integer auditStatus = oldInterfaceInfo.getAuditStatus();
        if (!auditStatus.equals(AuditStatusEnum.AGREE.getCode())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "审核不通过，无法下线");
        }

        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 测试调用
     *
     * @param interfaceInfoInvokeRequest 测试调用请求类
     * @return 是否成功
     */
    @PostMapping(value = "/invoke")
    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
                                                    HttpServletRequest request) throws UnsupportedEncodingException {
        // 校验传参和接口是否存在
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = interfaceInfoInvokeRequest.getId();
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        Long userId = oldInterfaceInfo.getUserId();
        // 只有创建人和管理员可以无视接口是否开启调用
        if (oldInterfaceInfo.getStatus().equals(InterfaceInfoStatusEnum.OFFLINE.getValue())
                && !userId.equals(UserHolder.getUser().getId())
                && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口已关闭");
        }
        // 接口请求地址
        String url = oldInterfaceInfo.getUrl();
        String host = oldInterfaceInfo.getHost();
        String method = oldInterfaceInfo.getMethod();
        String requestParams = interfaceInfoInvokeRequest.getRequestParams();
        // 获取SDK客户端
        BestApiClient bestApiClient = interfaceInfoService.getBestApiClient(request);
        // 设置网关地址
        bestApiClient.setGatewayHost(gatewayConfig.getHost());
        String invokeResult = null;
        try {
            // 执行方法
            if (method.equals(MethodEnum.GET.getDesc())) {
                invokeResult = bestApiClient.invokeInterfaceByGet(requestParams, host, url, method);
            } else {
                invokeResult = bestApiClient.invokeInterfaceByPost(requestParams, host, url, method);
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口调用失败");
        }
        // 走到下面，接口肯定调用成功了
        if (StringUtils.isBlank(invokeResult)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口返回值为空");
        } else {
            return ResultUtils.success(invokeResult);
        }
    }
    // endregion

}
