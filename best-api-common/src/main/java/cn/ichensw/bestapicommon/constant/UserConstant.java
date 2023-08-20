package cn.ichensw.bestapicommon.constant;

/**
 * 用户常量
 *
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    /**
     * 用户id
     */
    String ID = "id";

    /**
     * 用户名称
     */
    String USERNAME = "username";

    /**
     * 用户角色
     */
    String USER_ROLE = "user_role";
    /**
     * 用户头像
     */
    String USER_AVATAR = "user_avatar";

    int USERACCOUNT_MINLENGTH = 4;
    int USERACCOUNT_MAXLENGTH = 13;
    int USERNAME_LENGTH = 7;

    int USERPASSWORD_LENGTH = 8;

    // endregion
}
