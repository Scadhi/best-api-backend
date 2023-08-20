package cn.ichensw.bestapicommon.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/***
 * @title AuditStatusEnum
 * @description TODO
 * @author Skadhi
 * @version 1.0.0
 * @create 2023-08-12 16:43
 **/
@Getter
@AllArgsConstructor
public enum AuditStatusEnum {

    /**
     *
     */
    AUDITING(0,"待审核"),
    AGREE(1,"审核通过"),
    REFUSED(2, "审核拒绝")
    ;
    int code;
    String desc;

    public static String getDesById(Integer code) {
        AuditStatusEnum[] enums = values();
        for (AuditStatusEnum aEnum : enums) {
            if (aEnum.code == code) {
                return aEnum.desc;
            }
        }
        return "";
    }

    public static AuditStatusEnum getById(Integer code) {
        AuditStatusEnum[] enums = values();
        for (AuditStatusEnum aEnum : enums) {
            if (aEnum.code == code) {
                return aEnum;
            }
        }
        return null;
    }
}
