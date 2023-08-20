package cn.ichensw.bestapiadmin.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/***
 *
 **/
public class JwtUtils {

    // 设置token的过期时间是一天
    private static final long EXPIRE = 1000 * 60 * 60 * 24;

    private static final SecretKey SIGN_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * 生成JWT令牌
     * @param claims JWT第二部分负载 payload 中存储的内容
     * @return
     */
    public static String generateJwt(Map<String, Object> claims){
        String jwt = Jwts.builder()
                .addClaims(claims)//自定义信息（有效载荷）
                .signWith(SIGN_KEY)//签名算法（头部）
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))//过期时间
                .compact();
        return jwt;
    }

    /**
     * 解析JWT令牌
     * @param jwt JWT令牌
     * @return JWT第二部分负载 payload 中存储的内容
     */
    public static Claims parseJWT(String jwt){
        Claims claims = Jwts.parser()
                .setSigningKey(SIGN_KEY) //指定签名密钥
                //指定令牌Token,把刚传解析之前写入的有效载荷
                .parseClaimsJws(jwt)
                .getBody();
        return claims;
    }
}
