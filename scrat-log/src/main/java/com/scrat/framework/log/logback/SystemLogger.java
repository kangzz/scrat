package com.scrat.framework.log.logback;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.rpc.RpcContext;

/**
 * 描述：系统日志AOP
 * 作者 ：kangzz
 * 日期 ：2016-11-15 11:08:31
 */
@Component
@Aspect
public class SystemLogger {

	private static final Logger logger = LoggerFactory.getLogger(SystemLogger.class);

	/** 客户地址 */
	private final static String HOST = "address";

	/** 调用的接口 */
	private final static String INTERFACE = "service";

	/** 调用的方法名称 */
	private final static String METHOD = "method";


	@Around("execution(* com.scrat..*.web..*.* (..))")
	public Object doBasicProfiling(final ProceedingJoinPoint joinPoint) throws Throwable {
		long start_all = System.currentTimeMillis();
		long end_all = 0L;
		String declaringTypeName = joinPoint.getSignature().getDeclaringTypeName();
		String signatureName = joinPoint.getSignature().getName();
		Object [] args = joinPoint.getArgs();
		//Transaction tran = Cat.newTransaction("Aspect-proxy", declaringTypeName + "." + signatureName);
		if (RpcContext.getContext().getRemoteAddressString() != null && RpcContext.getContext().getMethodName() != null
				&& RpcContext.getContext().getUrl() != null) {
			MDC.put(HOST, RpcContext.getContext().getRemoteAddressString());
			MDC.put(INTERFACE, RpcContext.getContext().getUrl().getServiceInterface());
			MDC.put(METHOD, RpcContext.getContext().getMethodName());
		} else {
			MDC.put(HOST, "127.0.0.1");
			MDC.put(INTERFACE, "none");
			MDC.put(METHOD, "none");
		}

		final DataLogEntity de = new DataLogEntity();
		de.setClassName(declaringTypeName);
		de.setMethodName(signatureName);
		de.setParams(args);
		String logJson = de.toJsonStr();
		// 参数日志
		if (logger.isDebugEnabled()) {
			logger.debug(de.toJsonStr());
		}
		try {
			long start = System.currentTimeMillis();
			final Object retVal = joinPoint.proceed();
			long end = System.currentTimeMillis();
			// 记录耗时
			logger.info("{}, 耗时：{} ms, 进入aop到执行完耗时：{} ms", logJson, (end - start), (end - start_all));
			//Cat.logEvent(declaringTypeName, signatureName, "0", logJson+" 耗时:" + (end - start) + " ms" + " 时间戳：" + System.currentTimeMillis());
			/**
			 * 设置消息的状态,必须设置，0:标识成功,其他标识发生了异常
			 */
			//tran.setStatus(Transaction.SUCCESS);
			end_all = System.currentTimeMillis();
			return retVal;
		} catch (final Exception e) {
			final ErrorLogEntity ele = new ErrorLogEntity(de);
			ele.setThrowMessage(e.toString());
			logger.error(ele.toJsonStr());
			/**
			 * cat使用log4j记录异常信息
			 */
			//Cat.logError(ele.toJsonStr(), e);
			/**
			 * 发生异常了，要设置消息的状态为e
			 */
			//tran.setStatus(e);
			end_all = System.currentTimeMillis();
			// 方法返回值类型
			DataTransferObject dto = new DataTransferObject();
			dto.setErrCode(DataTransferObject.ERROR);
			dto.setMsg("服务错误");
			Class returnType = null;
			if (null != joinPoint.getSignature()) {
				returnType = ((MethodSignature) joinPoint.getSignature()).getReturnType();
			}
			if (null != returnType && returnType.equals(String.class)){
				return dto.toJsonString();
			}else if (null != returnType && returnType.equals(DataTransferObject.class)){
				return dto;
			}else{
				throw e;
			}
		} finally {
			MDC.remove(HOST);
			MDC.remove(INTERFACE);
			MDC.remove(METHOD);
			//tran.complete();
			//logger.info("{}, 接入cat后整体耗时: {} ms", logJson, (end_all - start_all));
		}
	}
}
