package cn.ichensw.bestapicommon.model.vo;

import lombok.Data;

/***
 * JWTRespond
 **/
@Data
public class JWTRespond {
    //设置token
    private String token;
    //把LoginUser传进data，就不用侵入源代码了
    private Object loginUserData;
}
