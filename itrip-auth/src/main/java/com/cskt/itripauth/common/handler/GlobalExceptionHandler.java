package com.cskt.itripauth.common.handler;

import com.cskt.itripauth.common.constants.ErrorCodeEnum;
import com.cskt.itripauth.common.exception.DaoException;
import com.cskt.itripauth.common.exception.ServiceException;
import com.cskt.itripauth.common.exception.SysException;
import com.cskt.itripauth.common.vo.ReturnResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 捕获异常
     *
     * @param e 异常类型为 Exception
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public ReturnResult error(Exception e) {
        log.error(e.getMessage(), e);
        return ReturnResult.error(ErrorCodeEnum.SYSTEM_EXECUTION_ERROR);
    }

    /**
     * 捕获异常
     *
     * @param e 异常类型为 ServiceException
     * @return
     */
    @ExceptionHandler(value = ServiceException.class)
    public ReturnResult error(ServiceException e) {
        log.error(e.getMessage(), e);
        return ReturnResult.error(e.getErrorCode(), e.getMessage());
    }

    /**
     * 捕获异常
     *
     * @param e 异常类型为 DaoException
     * @return
     */
    @ExceptionHandler(value = DaoException.class)
    public ReturnResult error(DaoException e) {
        log.error(e.getMessage(), e);
        return ReturnResult.error(e.getErrorCode(), e.getMessage());
    }

    /**
     * 捕获异常
     *
     * @param e 异常类型为 SysException
     * @return
     */
    @ExceptionHandler(value = SysException.class)
    public ReturnResult error(SysException e) {
        log.error(e.getMessage(), e);
        return ReturnResult.error(e.getErrorCode(), e.getMessage());
    }
}

