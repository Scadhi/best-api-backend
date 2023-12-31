package cn.ichensw.bestapicommon.model.dto.sms;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 短信服务传输对象
 * @author nero
 */
@Data
@AllArgsConstructor
public class SmsDTO implements Serializable {

    private static final long serialVersionUID = 8504215015474691352L;
    String phoneNum;

    String code;
}
