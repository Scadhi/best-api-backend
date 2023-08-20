package cn.ichensw.bestapiadmin.service;

import cn.ichensw.bestapicommon.model.dto.interfaceinfo.InterfaceInfoAuditRequest;
import cn.ichensw.bestapicommon.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import cn.ichensw.bestapicommon.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import cn.ichensw.bestapicommon.model.entity.InterfaceInfo;
import cn.ichensw.bestapicommon.model.vo.InterfaceInfoVO;
import cn.ichensw.bestclientsdk.client.BestApiClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * @author nero
 * @description 针对表【interface_info(接口信息)】的数据库操作Service
 * @createDate 2023-06-07 09:37:06
 */
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    /**
     * 校验
     *
     * @param interfaceInfo
     * @param add
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);


    /**
     * 获取查询条件
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest);

    /**
     * 获取接口信息封装
     *
     * @param interfaceInfo
     * @param request
     * @return
     */
    InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo, HttpServletRequest request);

    /**
     * 分页获取接口信息封装
     *
     * @param interfaceInfoPage
     * @param request
     * @return
     */
    Page<InterfaceInfoVO> getInterfaceInfoVOPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request);

    /**
     * 创建SDK客户端
     *
     * @param request 当前会话
     * @return SDK客户端
     */
    BestApiClient getBestApiClient(HttpServletRequest request);

    /**
     * 修改接口信息
     *
     * @param interfaceInfoUpdateRequest 接口信息修改请求
     * @return 是否成功
     */
    boolean updateInterfaceInfo(InterfaceInfoUpdateRequest interfaceInfoUpdateRequest);

    /**
     * 审核接口信息
     *
     * @param interfaceInfoAuditRequest 接口信息审核请求
     * @return 是否成功
     */
    boolean auditInterfaceInfo(InterfaceInfoAuditRequest interfaceInfoAuditRequest);

    /**
     * 根据用户ID 分页获取接口信息封装
     *
     * @param interfaceInfoPage 接口信息分页
     * @param request           当前会话
     * @return 接口信息分页
     */
    Page<InterfaceInfoVO> getInterfaceInfoVOByUserIdPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request);

    /**
     * 根据创建人 分页获取接口信息封装
     *
     * @param interfaceInfoPage 接口信息分页
     * @param request           当前会话
     * @return 接口信息分页
     */
    Page<InterfaceInfoVO> getOwnInterfaceInfoVOPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request);
}
