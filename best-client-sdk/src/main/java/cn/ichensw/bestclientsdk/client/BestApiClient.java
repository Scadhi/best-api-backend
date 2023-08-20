package cn.ichensw.bestclientsdk.client;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static cn.ichensw.bestclientsdk.utils.SignUtils.genSign;


/**
 * API 调用
 */
public class BestApiClient {

    public static String GATEWAY_HOST = "http://localhost:8090";

    private static String GENERAL_API = "/api/interface/general";
    private static final String DYE_DATA_HEADER = "X-Dye-Data";
    private static final String DYE_DATA_VALUE = "nero";

    private String accessKey;

    private String secretKey;

    public BestApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public void setGatewayHost(String gatewayHost) {
        GATEWAY_HOST = gatewayHost;
    }


    private Map<String, String> getHeaderMap(String body, String method, String url, String apihost) throws UnsupportedEncodingException {
        HashMap<String, String> map = new HashMap<>();
        map.put("accessKey", accessKey);
        map.put("nonce", RandomUtil.randomNumbers(10));
        map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        map.put("sign", genSign(body, secretKey));
        body = URLUtil.encode(body, CharsetUtil.CHARSET_UTF_8);
        map.put("body", body);
        map.put("method", method);
        map.put("url", url);
        map.put("apihost", apihost);
        return map;
    }

    public String invokeInterfaceGeneral(String params, String apihost, String url, String method) throws UnsupportedEncodingException {
        HttpResponse httpResponse = HttpRequest.post(GATEWAY_HOST + GENERAL_API)
                .header("Accept-Charset", CharsetUtil.UTF_8)
                .addHeaders(getHeaderMap(params, method, url, apihost))
                .body(params)
                .execute();
        return JSONUtil.formatJsonStr(httpResponse.body());
    }

    public String invokeInterfaceByPost(String params, String apihost, String url, String method) throws UnsupportedEncodingException {
        HttpResponse httpResponse = HttpRequest.post(GATEWAY_HOST + url)
                .header("Accept-Charset", CharsetUtil.UTF_8)
                .addHeaders(getHeaderMap(params, method, url, apihost))
                .body(params)
                .execute();
        return JSONUtil.formatJsonStr(httpResponse.body());
    }

    public String invokeInterfaceByGet(String params, String apihost, String url, String method) throws UnsupportedEncodingException {
        HttpResponse httpResponse = HttpRequest.post(GATEWAY_HOST + url)
                .header("Accept-Charset", CharsetUtil.UTF_8)
                .addHeaders(getHeaderMap(params, method, url, apihost))
                .body(params)
                .execute();
        return JSONUtil.formatJsonStr(httpResponse.body());
    }

}
