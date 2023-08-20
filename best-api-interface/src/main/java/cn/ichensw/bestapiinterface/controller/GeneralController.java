package cn.ichensw.bestapiinterface.controller;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.ichensw.bestapicommon.common.BusinessException;
import cn.ichensw.bestapicommon.common.ErrorCode;
import cn.ichensw.bestapicommon.constant.enums.MethodEnum;
import cn.ichensw.bestapicommon.utils.URLUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/***
 * generalController
 **/
@RestController
@RequestMapping("/api/interface")
public class GeneralController {

    private static final String DYE_DATA_HEADER = "X-Dye-Data";
    private static final String DYE_DATA_VALUE = "nero";

    @PostMapping("/general")
    public String generalCall(HttpServletRequest request) {
        String url = request.getHeader("apihost") + request.getHeader("url");
        String method = request.getHeader("method");
        String body = URLUtil.decode(request.getHeader("body"), "UTF-8");
        // 如果是get请求
        String result = null;
        if(method.equals(MethodEnum.GET.getDesc())){
            try(HttpResponse httpResponse = HttpRequest.get(url + "?" + body)
                    .header(DYE_DATA_HEADER, DYE_DATA_VALUE)
                    .execute()) {
                if (httpResponse.getStatus() == HttpStatus.HTTP_NOT_FOUND) {
                    result = String.format
                            ("{\"code\": %d,\"msg\":\"%s\",\"data\":\"%s\"}",
                                    ErrorCode.NOT_FOUND_ERROR.getCode(),
                                    "接口请求路径不存在", "null");
                } else {
                    result= httpResponse.body();
                }
            }
        }
        else if(method.equals(MethodEnum.POST.getDesc())){
            try(HttpResponse httpResponse = HttpRequest.post(url)
                    // 处理中文编码
                    .header("Accept-Charset", CharsetUtil.UTF_8)
                    .header(DYE_DATA_HEADER, DYE_DATA_VALUE)
                    // 传递参数
                    .body(body)
                    .execute())
            {
                if (httpResponse.getStatus() == HttpStatus.HTTP_NOT_FOUND) {
                    result = String.format
                            ("{\"code\": %d,\"msg\":\"%s\",\"data\":\"%s\"}",
                                    ErrorCode.NOT_FOUND_ERROR.getCode(),
                                    "接口请求路径不存在", "null");
                } else {
                    result= httpResponse.body();
                }
            }
        }
        if(result.contains("Whitelabel Error Page")){
            // 不用写message，不管写什么都会被最后一个throw覆盖
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        // put和post基本一致
        // 不用转JSON，因为Client收到结果会转
        return result;
    }

}
