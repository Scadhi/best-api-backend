package cn.ichensw.bestapiadmin.service;

import cn.ichensw.bestapicommon.model.dto.user.UserQueryRequest;
import cn.ichensw.bestapicommon.model.dto.user.UserRegisterRequest;
import cn.ichensw.bestapicommon.model.entity.User;
import cn.ichensw.bestapicommon.model.vo.LoginUserVO;
import cn.ichensw.bestapicommon.model.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 用户服务
 *
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userRegisterRequest
     * @param request
     * @return 新用户id
     */
    Long userRegister(UserRegisterRequest userRegisterRequest, HttpServletRequest request);

    /**
     * 生成图像验证码
     * @param request
     * @param response
     */
    void getCaptcha(HttpServletRequest request, HttpServletResponse response);

    /**
     * 发送短信验证码
     * @param phoneNum
     * @return
     */
    Boolean sendSmsCaptcha(String phoneNum);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 使用手机号登录
     * @param phoneNum
     * @param phoneCaptcha
     * @param request
     * @return
     */
    LoginUserVO userLoginBySms(String phoneNum, String phoneCaptcha, HttpServletRequest request,HttpServletResponse response);


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 更新 secretKey
     * @param id 用户id
     * @return boolean
     */
    boolean updateSecretKey(Long id);

    /**
     * 返回threadlocal对象
     * @return: LoginUserVO
     **/
    LoginUserVO getLoginUserByThreadLocal();

    /**
     * 为用户生成ak和sk
     * @return: User
     **/
    void generateKey(User user);
}
