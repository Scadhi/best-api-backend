package cn.ichensw.bestapiadmin.aop;

import cn.ichensw.bestapiadmin.config.UserHolder;
import cn.ichensw.bestapiadmin.utils.JwtUtils;
import cn.ichensw.bestapicommon.common.BaseResponse;
import cn.ichensw.bestapicommon.common.ErrorCode;
import cn.ichensw.bestapicommon.constant.UserConstant;
import cn.ichensw.bestapicommon.model.vo.LoginUserVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/***
 *  JwtHandler
 **/
@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("preHandle .... ");
        //1.获取请求url
        //2.判断请求url中是否包含login，如果包含，说明是登录操作，放行，此处已在注册器里实现
        //3.获取请求头中的令牌（token），下面这行解析需要和前端约定好
        String token= request.getHeader("Authorization").replace("Bearer ", "");

        log.info("从请求头中获取的令牌：{}", token);
        //4.判断令牌是否存在，如果不存在，返回错误结果（未登录）
        if (!StringUtils.hasLength(token)) {
            log.info("Token不存在");
            ResponseForFrontend(response);
            return false;
        }
        //5.解析token，如果解析失败，返回错误结果（未登录）
        try {
            // 已登录的用户，保存到UserHolder中
            Claims claims = JwtUtils.parseJWT(token);
            LoginUserVO saveUser = UserHolder.getUser();
            if (saveUser == null) {
                LoginUserVO loginUserVO = new LoginUserVO();
                loginUserVO.setId((Long) claims.get(UserConstant.ID));
                loginUserVO.setUserName((String) claims.get(UserConstant.USERNAME));
                loginUserVO.setUserRole((String) claims.get(UserConstant.USER_ROLE));
                loginUserVO.setUserAvatar((String) claims.get(UserConstant.USER_AVATAR));
                UserHolder.saveUser(loginUserVO);
            }
        } catch (Exception e) {
            log.info("令牌解析失败!");
            //创建响应结果对象
            ResponseForFrontend(response);
            return false;
        }
        //6.放行
        return true;
    }

    private void ResponseForFrontend(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        BaseResponse baseResponse = new BaseResponse(ErrorCode.NOT_LOGIN_ERROR.getCode(), null, "未登录");
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().println(objectMapper.writeValueAsString(baseResponse));
        return;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
