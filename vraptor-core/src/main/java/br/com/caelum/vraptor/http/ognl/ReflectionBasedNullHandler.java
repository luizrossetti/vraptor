/***
 *
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package br.com.caelum.vraptor.http.ognl;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import ognl.ObjectNullHandler;
import ognl.OgnlContext;
import br.com.caelum.vraptor.http.InvalidParameterException;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.vraptor2.Info;

/**
 * This null handler is a decorator for ognl api to invoke vraptor's api in
 * order to be able to instantiate collections, arrays and custom types whenever
 * the property is null.
 *
 * @author Guilherme Silveira
 */
public class ReflectionBasedNullHandler extends ObjectNullHandler {

    private final ListNullHandler list = new ListNullHandler();
    private final GenericNullHandler generic = new GenericNullHandler();

    @Override
	@SuppressWarnings("unchecked")
    public Object nullPropertyValue(Map context, Object target, Object property) {

        OgnlContext ctx = (OgnlContext) context;

        int indexInParent = ctx.getCurrentEvaluation().getNode().getIndexInParent();
        int maxIndex = ctx.getRootEvaluation().getNode().jjtGetNumChildren() - 1;

        if (!(indexInParent != -1 && indexInParent < maxIndex)) {
            return null;
        }

        try {

            Container container = (Container) context.get(Container.class);
            if (target instanceof List) {
                return list.instantiate(container, target, property, ctx.getCurrentEvaluation().getPrevious());
            }

            String propertyCapitalized = Info.capitalize((String) property);
			Method getter = findMethod(target.getClass(), "get" + propertyCapitalized, target.getClass(), null);
            Type returnType = getter.getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) returnType;
                returnType = paramType.getRawType();
            }

            Class<?> baseType = (Class<?>) returnType;
            Object instance;
            if (baseType.isArray()) {
                instance = instantiateArray(baseType);
            } else {
                instance = generic.instantiate(baseType, container);
            }
            Method setter = findMethod(target.getClass(), "set" + propertyCapitalized, target.getClass(), getter.getReturnType());
            setter.invoke(target, instance);
            return instance;

        } catch (InstantiationException e) {
            // TODO better
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            // TODO better
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            // TODO better
            throw new IllegalArgumentException(e);
        } catch (SecurityException e) {
            // TODO better
            throw new IllegalArgumentException(e);
        } catch (NoSuchMethodException e) {
            throw new InvalidParameterException("Unable to find the correct constructor",e);
        }
    }

    private Object instantiateArray(Class<?> baseType) {
        return Array.newInstance(baseType.getComponentType(), 0);
    }

    static <P> Method findMethod(Class<?> type, String name, Class<?> baseType, Class<P> parameterType) {
        Method[] methods = type.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(name)) {
            	if(parameterType==null || (method.getParameterTypes().length==1 && method.getParameterTypes()[0].equals(parameterType))) {
            		return method;
            	}
            }
        }
        if (type.equals(Object.class)) {
            // TODO better
            throw new IllegalArgumentException("Unable to find method for " + name + " @ " + baseType.getName());
        }
        return findMethod(type.getSuperclass(), name, type, parameterType);
    }

}
