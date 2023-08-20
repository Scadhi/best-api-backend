package cn.ichensw.bestapiadmin.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.ichensw.bestapiadmin.common.RedisTokenBucket;
import cn.ichensw.bestapiadmin.config.UserHolder;
import cn.ichensw.bestapiadmin.exception.BusinessException;
import cn.ichensw.bestapiadmin.exception.ThrowUtils;
import cn.ichensw.bestapiadmin.mapper.UserMapper;
import cn.ichensw.bestapiadmin.service.UserService;
import cn.ichensw.bestapiadmin.utils.SmsUtils;
import cn.ichensw.bestapiadmin.utils.SqlUtils;
import cn.ichensw.bestapicommon.common.ErrorCode;
import cn.ichensw.bestapicommon.constant.CommonConstant;
import cn.ichensw.bestapicommon.model.dto.sms.SmsDTO;
import cn.ichensw.bestapicommon.model.dto.user.UserQueryRequest;
import cn.ichensw.bestapicommon.model.dto.user.UserRegisterRequest;
import cn.ichensw.bestapicommon.model.entity.User;
import cn.ichensw.bestapicommon.model.enums.UserRoleEnum;
import cn.ichensw.bestapicommon.model.vo.LoginUserVO;
import cn.ichensw.bestapicommon.model.vo.UserVO;
import cn.ichensw.bestapicommon.utils.AuthPhoneNumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.ichensw.bestapicommon.constant.UserConstant.*;

/**
 * 用户服务实现
 *
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisTemplate<String,String> redisTemplate;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RedisTokenBucket redisTokenBucket;
    @Resource
    private SmsUtils smsUtils;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "nero";

    /**
     * 图片验证码 redis 前缀
     */
    private static final String CAPTCHA_PREFIX = "api:captchaId:";

    @Override
    @Transactional(rollbackFor = SQLException.class)
    public Long userRegister(UserRegisterRequest userRegisterRequest, HttpServletRequest request) {
        String userName = userRegisterRequest.getUserName();
        String userPassword = userRegisterRequest.getUserPassword();
        String userAccount = userRegisterRequest.getUserAccount();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String captcha = userRegisterRequest.getCaptcha();
        String phoneNum = userRegisterRequest.getPhoneNum();
        String phoneCaptcha = userRegisterRequest.getPhoneCaptcha();

        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, captcha,phoneNum,phoneCaptcha,userName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if(userName.length() > USERNAME_LENGTH){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户昵称应该小于7个字！");
        }
        if (userAccount.length() < USERACCOUNT_MINLENGTH || userAccount.length() > USERACCOUNT_MAXLENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短或过长");
        }
        if (userPassword.length() < USERPASSWORD_LENGTH || checkPassword.length() < USERPASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        AuthPhoneNumberUtil authPhoneNumberUtil = new AuthPhoneNumberUtil();
        if (!authPhoneNumberUtil.isPhoneNum(phoneNum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机验证码错误");
        }
        //图形验证码
        String signature = request.getHeader("signature");
        if (StringUtils.isEmpty(signature)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String checkCaptcha =  redisTemplate.opsForValue().get(CAPTCHA_PREFIX + signature);
        if (StringUtils.isEmpty(checkCaptcha) || !captcha.equals(checkCaptcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片验证码过期或错误！");
        }

        if(!smsUtils.verifyCode(phoneNum, phoneCaptcha)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机验证码过期或错误！");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount)
                    .or().eq("phoneNum", phoneNum);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复或手机号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 分配 accessKey，secretKey
            String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
            // 4. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserName(StringUtils.upperCase(userAccount));
            user.setUserPassword(encryptPassword);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            user.setPhoneNum(phoneNum);
            user.setUserAvatar("https://nero-api-imagehost.oss-cn-hangzhou.aliyuncs.com/images/user.jpg");
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        //前端必须传一个 signature 来作为唯一标识
        String signature = request.getHeader("signature");
        if (StringUtils.isEmpty(signature)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        try {
            // 自定义纯数字的验证码（随机4位数字，可重复）
            RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
            LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(100, 30);
            lineCaptcha.setGenerator(randomGenerator);
            //设置响应头
            response.setContentType("image/jpeg");
            response.setHeader("Pragma", "No-cache");
            // 输出到页面
            lineCaptcha.write(response.getOutputStream());
            // 打印日志
            log.info("captchaId：{} ----生成的验证码:{}", signature, lineCaptcha.getCode());
            // 将验证码设置到Redis中,2分钟过期
            redisTemplate.opsForValue().set(CAPTCHA_PREFIX + signature, lineCaptcha.getCode(), 2, TimeUnit.MINUTES);
            // 关闭流
            response.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean sendSmsCaptcha(String phoneNum) {

        if (StringUtils.isEmpty(phoneNum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号不能为空");
        }
        AuthPhoneNumberUtil authPhoneNumberUtil = new AuthPhoneNumberUtil();

        // 手机号码格式校验
        boolean checkPhoneNum = authPhoneNumberUtil.isPhoneNum(phoneNum);
        if (!checkPhoneNum) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }

        //生成随机验证码
        int code = (int) ((Math.random() * 9 + 1) * 10000);
        SmsDTO smsDTO = new SmsDTO(phoneNum,String.valueOf(code));
        log.info("本次发送的验证码为：" +code);

        return smsUtils.sendSms(smsDTO);
    }


    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        LoginUserVO safetyUser = this.getLoginUserVO(user);
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    @Override
    public LoginUserVO userLoginBySms(String phoneNum, String phoneCaptcha, HttpServletRequest request,HttpServletResponse response) {
        boolean verifyCode = smsUtils.verifyCode(phoneNum, phoneCaptcha);
        if(!verifyCode){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"手机验证码错误！");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phoneNum",phoneNum);
        User user = this.getOne(queryWrapper);
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在！");
        }
        LoginUserVO safetyUser = this.getLoginUserVO(user);
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        LoginUserVO currentUser = (LoginUserVO) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            // 由于我们使用了自定义请求头，导致之前的跨域处理都无效了，
            // 所以此处前端可能没携带session，session为空抛异常
            // throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
            ThrowUtils.throwIf(UserHolder.getUser()==null,ErrorCode.NOT_LOGIN_ERROR);
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public boolean updateSecretKey(Long id) {
        User user = this.getById(id);
        String accessKey = DigestUtil.md5Hex(SALT + user.getUserAccount() + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT + user.getUserAccount() + RandomUtil.randomNumbers(8));
        user.setSecretKey(secretKey);
        user.setAccessKey(accessKey);
        return this.updateById(user);
    }

    @Override
    public LoginUserVO getLoginUserByThreadLocal() {
        return UserHolder.getUser();
    }

    @Override
    public void generateKey(User user) {
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + user.getUserPassword()).getBytes());
        // 3. 分配 accessKey，secretKey
        String accessKey = DigestUtil.md5Hex(SALT + user.getUserAccount() + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT + user.getUserAccount() + RandomUtil.randomNumbers(8));
        // 4. 插入数据
        user.setUserPassword(encryptPassword);
        user.setAccessKey(accessKey);
        user.setSecretKey(secretKey);
    }


}
