/***
 *
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of the
 * copyright holders nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written
 * permission.
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
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package br.com.caelum.vraptor.interceptor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;

import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.core.MethodInfo;
import br.com.caelum.vraptor.resource.ResourceMethod;
import br.com.caelum.vraptor.vraptor2.Info;

/**
 * Outjects the result of the method invocation to the desired result
 *
 * @author guilherme silveira
 */
public class OutjectResult implements Interceptor {

	private final Result result;
	private final MethodInfo info;

	public OutjectResult(Result result, MethodInfo info) {
		this.result = result;
		this.info = info;
	}

	public boolean accepts(ResourceMethod method) {
		return method.getResource().getType().isAnnotationPresent(Resource.class);
	}

	public void intercept(InterceptorStack stack, ResourceMethod method, Object resourceInstance)
			throws InterceptionException {
		Type returnType = method.getMethod().getGenericReturnType();
		if (!returnType.equals(void.class)) {
			result.include(nameFor(returnType), this.info.getResult());
		}
		stack.next(method, resourceInstance);
	}

	/*
	 * TODO: externalize as an application Component
	 * and maybe use a cache.
	 */
	@SuppressWarnings("unchecked")
	String nameFor(Type generic) {
		if (generic instanceof ParameterizedType) {
			ParameterizedType type = (ParameterizedType) generic;
			Class raw = (Class) type.getRawType();
			if (Collection.class.isAssignableFrom(raw)) {
				return nameFor(type.getActualTypeArguments()[0]) + "List";
			}
			return nameFor(raw);
		}

		if (generic instanceof WildcardType) {
			WildcardType wild = (WildcardType) generic;
			if ((wild.getLowerBounds().length != 0)) {
				return nameFor(wild.getLowerBounds()[0]);
			}
			else return nameFor(wild.getUpperBounds()[0]);
		}

		Class raw = (Class) generic;


		if (raw.isArray()) {
			return nameFor(raw.getComponentType()) + "List";
		}

		String name = raw.getSimpleName();

		// common case: SomeClass -> someClass
		if(Character.isLowerCase(name.charAt(1))) {
			return Info.decapitalize(name);
		}

		// different case: URLClassLoader -> urlClassLoader
		for (int i = 1; i < name.length(); i++) {
			if(Character.isLowerCase(name.charAt(i))) {
				return name.substring(0, i-1).toLowerCase()+name.substring(i-1, name.length());
			}
		}

		// all uppercase: URL -> url
		return name.toLowerCase();
	}

}
