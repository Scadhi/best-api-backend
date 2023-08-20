package cn.ichensw.bestapiadmin.exception;

import cn.ichensw.bestapicommon.common.BaseResponse;
import cn.ichensw.bestapicommon.common.ErrorCode;
import cn.ichensw.bestapicommon.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

/**
 * 全局异常处理器
 *
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    @ExceptionHandler(SQLException.class)
    public BaseResponse<?> SQLExceptionHandler(SQLException e) {
        log.error("SQLException", e);
        return ResultUtils.error(ErrorCode.OPERATION_ERROR, "操作失败");
    }
}
