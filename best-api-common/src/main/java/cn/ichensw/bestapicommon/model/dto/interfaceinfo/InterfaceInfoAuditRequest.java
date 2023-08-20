package cn.ichensw.bestapicommon.model.dto.interfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 审核请求
 *
 */
@Data
public class InterfaceInfoAuditRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 审核结果
     */
    private Integer auditStatus;
}
