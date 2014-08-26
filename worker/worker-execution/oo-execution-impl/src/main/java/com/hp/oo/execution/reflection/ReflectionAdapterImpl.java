package com.hp.oo.execution.reflection;

import com.hp.oo.execution.services.SessionDataService;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.oo.internal.sdk.execution.FlowExecutionException;
import com.hp.score.api.ControlActionMetadata;
import com.hp.score.lang.SystemContext;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kravtsov
 * @author Avi Moradi
 * @since 09/11/2011
 * @version $Id$
 */
public class ReflectionAdapterImpl implements ReflectionAdapter, ApplicationContextAware {

    @Autowired
    private SessionDataService sessionDataService;

	private static final Logger logger = Logger.getLogger(ReflectionAdapterImpl.class);
	private static final String CONTEXT_PARAM_NAME = "executionContext";

	private ApplicationContext applicationContext;
	private Map<String, Object> cacheBeans = new ConcurrentHashMap<>();
	private Map<String, Method> cacheMethods = new ConcurrentHashMap<>();
	private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
	private Map<String, String[]> cacheParamNames = new ConcurrentHashMap<>();

	@Override
	public Object executeControlAction(ControlActionMetadata actionMetadata, Map<String, ?> actionData) {
		Validate.notNull(actionMetadata, "Action metadata is null");
		if(logger.isDebugEnabled()) logger.debug("Executing control action [" + actionMetadata.getClassName() + '.' + actionMetadata.getMethodName() + ']');
		try {
			Object actionBean = getActionBean(actionMetadata);
			Method actionMethod = getActionMethod(actionMetadata);
			Object[] arguments = buildParametersArray(actionMethod, actionData);
			if(logger.isTraceEnabled()) logger.trace("Invoking...");
			Object result = actionMethod.invoke(actionBean, arguments);
			if(logger.isDebugEnabled()) logger.debug("Control action [" + actionMetadata.getClassName() + '.' + actionMetadata.getMethodName() + "] done");
			return result;
		} catch(IllegalArgumentException ex) {
			String message = "Failed to run the action! Wrong arguments were passed to class: " + actionMetadata.getClassName() + ", method: " + actionMetadata.getMethodName() +
				", reason: " + ex.getMessage();
			throw new FlowExecutionException(message);
		} catch(InvocationTargetException ex) {
			String message = ex.getTargetException() == null ? ex.getMessage() : ex.getTargetException().getMessage();
			logger.error(getExceptionMessage(actionMetadata) + ", reason: " + message, ex);
			throw new FlowExecutionException(message, ex);
		} catch(Exception ex) {
			throw new FlowExecutionException(getExceptionMessage(actionMetadata) + ", reason: " + ex.getMessage(), ex);
		}
	}

	private Object getActionBean(ControlActionMetadata actionMetadata) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Object bean = cacheBeans.get(actionMetadata.getClassName());
		if(bean == null) {
			if(logger.isTraceEnabled()) logger.trace(actionMetadata.getClassName() + " wasn't found in the beans cache");
			Class<?> actionClass = Class.forName(actionMetadata.getClassName());
			try {
				bean = applicationContext.getBean(actionClass);
			} catch(Exception ex) { // Not a spring bean
				if(logger.isTraceEnabled()) logger.trace(ex);
			}
			if(bean == null) bean = actionClass.newInstance();
			cacheBeans.put(actionMetadata.getClassName(), bean);
			if(logger.isTraceEnabled()) logger.trace(actionMetadata.getClassName() + " placed in the beans cache");
		}
		return bean;
	}

	private Method getActionMethod(ControlActionMetadata actionMetadata) throws ClassNotFoundException {
		Method actionMethod = cacheMethods.get(actionMetadata.getClassName() + '.' + actionMetadata.getMethodName());
		if(actionMethod == null) {
			if(logger.isTraceEnabled()) logger.trace(actionMetadata.getClassName() + '.' + actionMetadata.getMethodName() + " wasn't found in the methods cache");
			for(Method method : Class.forName(actionMetadata.getClassName()).getMethods()) {
				if(method.getName().equals(actionMetadata.getMethodName())) {
					actionMethod = method;
					cacheMethods.put(actionMetadata.getClassName() + '.' + actionMetadata.getMethodName(), method);
					break;
				}
			}
		} else if(logger.isTraceEnabled()) {
			logger.trace(actionMetadata.getClassName() + '.' + actionMetadata.getMethodName() + " was found in the methods cache");
		}
		if(actionMethod == null) {
			String errMessage = "Method: " + actionMetadata.getMethodName() + " was not found in class:  " + actionMetadata.getClassName();
			logger.error(errMessage);
			throw new FlowExecutionException(errMessage);
		}
		return actionMethod;
	}

    private Object[] buildParametersArray(Method actionMethod, Map<String, ?> actionData) {
		String actionFullName = actionMethod.getDeclaringClass().getName() + "." + actionMethod.getName();
		String[] paramNames = cacheParamNames.get(actionFullName);
		if(paramNames == null) {
			paramNames = parameterNameDiscoverer.getParameterNames(actionMethod);
			cacheParamNames.put(actionFullName, paramNames);
		}
		List<Object> args = new ArrayList<>(paramNames.length);
		for(String paramName : paramNames) {
			if(CONTEXT_PARAM_NAME.equals(paramName)) { //todo - why not use execution context ??
				Execution execution = (Execution)actionData.get(ExecutionConstants.EXECUTION);
				args.add(execution != null ? execution.getContexts() : null);
				continue;
			}
            if(ExecutionConstants.NON_SERIALIZABLE_EXECUTION_DATA.equals(paramName)) {
                //todo: change to runtime services once we can
                SystemContext systemContext = (SystemContext)actionData.get(ExecutionConstants.SYSTEM_CONTEXT);
                Long executionId = (Long)systemContext.get(ExecutionConstants.EXECUTION_ID_CONTEXT);
                Map<String, Object> nonSerializableExecutionData = sessionDataService.getNonSerializableExecutionData(executionId);
                args.add(nonSerializableExecutionData);
                continue;
            }
			Object param = actionData.get(paramName);
			args.add(param);
		}
		return args.toArray(new Object[args.size()]);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	private static String getExceptionMessage(ControlActionMetadata actionMetadata) {
		return "Failed to run the action! Class: " + actionMetadata.getClassName() + ", method: " + actionMetadata.getMethodName();
	}

}