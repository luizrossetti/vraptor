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
package br.com.caelum.vraptor.core;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import br.com.caelum.vraptor.Convert;
import br.com.caelum.vraptor.Converter;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.converter.BigDecimalConverter;
import br.com.caelum.vraptor.converter.BigIntegerConverter;
import br.com.caelum.vraptor.converter.BooleanConverter;
import br.com.caelum.vraptor.converter.ByteConverter;
import br.com.caelum.vraptor.converter.CharacterConverter;
import br.com.caelum.vraptor.converter.DoubleConverter;
import br.com.caelum.vraptor.converter.EnumConverter;
import br.com.caelum.vraptor.converter.FloatConverter;
import br.com.caelum.vraptor.converter.IntegerConverter;
import br.com.caelum.vraptor.converter.LocaleBasedCalendarConverter;
import br.com.caelum.vraptor.converter.LocaleBasedDateConverter;
import br.com.caelum.vraptor.converter.LongConverter;
import br.com.caelum.vraptor.converter.PrimitiveBooleanConverter;
import br.com.caelum.vraptor.converter.PrimitiveByteConverter;
import br.com.caelum.vraptor.converter.PrimitiveCharConverter;
import br.com.caelum.vraptor.converter.PrimitiveDoubleConverter;
import br.com.caelum.vraptor.converter.PrimitiveFloatConverter;
import br.com.caelum.vraptor.converter.PrimitiveIntConverter;
import br.com.caelum.vraptor.converter.PrimitiveLongConverter;
import br.com.caelum.vraptor.converter.PrimitiveShortConverter;
import br.com.caelum.vraptor.converter.ShortConverter;
import br.com.caelum.vraptor.extra.ForwardToDefaultViewInterceptor;
import br.com.caelum.vraptor.http.DefaultResourceTranslator;
import br.com.caelum.vraptor.http.ParameterNameProvider;
import br.com.caelum.vraptor.http.ParametersProvider;
import br.com.caelum.vraptor.http.ParanamerNameProvider;
import br.com.caelum.vraptor.http.TypeCreator;
import br.com.caelum.vraptor.http.UrlToResourceTranslator;
import br.com.caelum.vraptor.http.asm.AsmBasedTypeCreator;
import br.com.caelum.vraptor.http.ognl.EmptyElementsRemoval;
import br.com.caelum.vraptor.http.ognl.OgnlParametersProvider;
import br.com.caelum.vraptor.http.route.DefaultRouter;
import br.com.caelum.vraptor.http.route.DefaultTypeFinder;
import br.com.caelum.vraptor.http.route.NoRoutesConfiguration;
import br.com.caelum.vraptor.http.route.PathAnnotationRoutesParser;
import br.com.caelum.vraptor.http.route.Router;
import br.com.caelum.vraptor.http.route.RoutesConfiguration;
import br.com.caelum.vraptor.http.route.RoutesParser;
import br.com.caelum.vraptor.http.route.TypeFinder;
import br.com.caelum.vraptor.interceptor.DefaultInterceptorRegistry;
import br.com.caelum.vraptor.interceptor.ExecuteMethodInterceptor;
import br.com.caelum.vraptor.interceptor.InstantiateInterceptor;
import br.com.caelum.vraptor.interceptor.InterceptorListPriorToExecutionExtractor;
import br.com.caelum.vraptor.interceptor.InterceptorRegistry;
import br.com.caelum.vraptor.interceptor.OutjectResult;
import br.com.caelum.vraptor.interceptor.ParametersInstantiatorInterceptor;
import br.com.caelum.vraptor.interceptor.ResourceLookupInterceptor;
import br.com.caelum.vraptor.interceptor.download.DownloadInterceptor;
import br.com.caelum.vraptor.interceptor.multipart.DefaultMultipartConfig;
import br.com.caelum.vraptor.interceptor.multipart.MultipartConfig;
import br.com.caelum.vraptor.interceptor.multipart.MultipartInterceptor;
import br.com.caelum.vraptor.interceptor.multipart.UploadedFileConverter;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.ConverterHandler;
import br.com.caelum.vraptor.ioc.InterceptorStereotypeHandler;
import br.com.caelum.vraptor.ioc.ResourceHandler;
import br.com.caelum.vraptor.ioc.StereotypeHandler;
import br.com.caelum.vraptor.proxy.ObjenesisProxifier;
import br.com.caelum.vraptor.proxy.Proxifier;
import br.com.caelum.vraptor.reflection.CacheBasedTypeCreator;
import br.com.caelum.vraptor.resource.DefaultResourceNotFoundHandler;
import br.com.caelum.vraptor.resource.ResourceNotFoundHandler;
import br.com.caelum.vraptor.validator.DefaultValidator;
import br.com.caelum.vraptor.view.AcceptHeaderToFormat;
import br.com.caelum.vraptor.view.DefaultAcceptHeaderToFormat;
import br.com.caelum.vraptor.view.DefaultHttpResult;
import br.com.caelum.vraptor.view.DefaultLogicResult;
import br.com.caelum.vraptor.view.DefaultPageResult;
import br.com.caelum.vraptor.view.DefaultPathResolver;
import br.com.caelum.vraptor.view.DefaultValidationViewsFactory;
import br.com.caelum.vraptor.view.EmptyResult;
import br.com.caelum.vraptor.view.HttpResult;
import br.com.caelum.vraptor.view.LogicResult;
import br.com.caelum.vraptor.view.PageResult;
import br.com.caelum.vraptor.view.PathResolver;
import br.com.caelum.vraptor.view.ValidationViewsFactory;

/**
 * List of base components to vraptor. Those components should be available with
 * any chosen ioc implementation.
 *
 * @author guilherme silveira
 */
public class BaseComponents {

    private final static Map<Class<?>, Class<?>> APPLICATION_COMPONENTS = classMap(
    		TypeCreator.class, 				AsmBasedTypeCreator.class,
    		AcceptHeaderToFormat.class, 	DefaultAcceptHeaderToFormat.class,
    		Converters.class, 				DefaultConverters.class,
            InterceptorRegistry.class, 		DefaultInterceptorRegistry.class,
            MultipartConfig.class, 			DefaultMultipartConfig.class,
            UrlToResourceTranslator.class, 	DefaultResourceTranslator.class,
            Router.class, 					DefaultRouter.class,
            ResourceNotFoundHandler.class, 	DefaultResourceNotFoundHandler.class,
            EmptyElementsRemoval.class, 	EmptyElementsRemoval.class,
            RoutesConfiguration.class, 		NoRoutesConfiguration.class,
            Proxifier.class, 				ObjenesisProxifier.class,
            ParameterNameProvider.class, 	ParanamerNameProvider.class,
            TypeFinder.class, 				DefaultTypeFinder.class,
            RoutesParser.class, 			PathAnnotationRoutesParser.class
    );

    private final static Map<Class<?>, Class<?>> CACHED_COMPONENTS = classMap(
    		TypeCreator.class, CacheBasedTypeCreator.class
    );

    private static final Map<Class<?>, Class<?>> REQUEST_COMPONENTS = classMap(
            InterceptorStack.class, 						DefaultInterceptorStack.class,
            MethodInfo.class, 								DefaultMethodInfo.class,
            LogicResult.class, 								DefaultLogicResult.class,
            PageResult.class, 								DefaultPageResult.class,
            HttpResult.class, 								DefaultHttpResult.class,
            PathResolver.class, 							DefaultPathResolver.class,
            RequestExecution.class, 						DefaultRequestExecution.class,
            ValidationViewsFactory.class,					DefaultValidationViewsFactory.class,
            Result.class, 									DefaultResult.class,
            Validator.class, 								DefaultValidator.class,
            DownloadInterceptor.class, 						DownloadInterceptor.class,
            EmptyResult.class, 								EmptyResult.class,
            ExecuteMethodInterceptor.class, 				ExecuteMethodInterceptor.class,
            ForwardToDefaultViewInterceptor.class, 			ForwardToDefaultViewInterceptor.class,
            InstantiateInterceptor.class, 					InstantiateInterceptor.class,
            InterceptorListPriorToExecutionExtractor.class, InterceptorListPriorToExecutionExtractor.class,
            Localization.class, 							JstlLocalization.class,
            MultipartInterceptor.class, 					MultipartInterceptor.class,
            ParametersProvider.class, 						OgnlParametersProvider.class,
            OutjectResult.class, 							OutjectResult.class,
            ParametersInstantiatorInterceptor.class, 		ParametersInstantiatorInterceptor.class,
            ResourceLookupInterceptor.class, 				ResourceLookupInterceptor.class,
            URLParameterExtractorInterceptor.class, 		URLParameterExtractorInterceptor.class
    );

    @SuppressWarnings("unchecked")
	private static final Class<? extends Converter<?>>[] BUNDLED_CONVERTERS = new Class[] {
    		BigDecimalConverter.class,
    		BigIntegerConverter.class,
    		BooleanConverter.class,
    		ByteConverter.class,
    		CharacterConverter.class,
    		DoubleConverter.class,
    		EnumConverter.class,
    		FloatConverter.class,
    		IntegerConverter.class,
    		LocaleBasedCalendarConverter.class,
    		LocaleBasedDateConverter.class,
    		LongConverter.class,
    		PrimitiveBooleanConverter.class,
    		PrimitiveByteConverter.class,
    		PrimitiveCharConverter.class,
    		PrimitiveDoubleConverter.class,
    		PrimitiveFloatConverter.class,
			PrimitiveIntConverter.class,
			PrimitiveLongConverter.class,
			PrimitiveShortConverter.class,
			ShortConverter.class,
			UploadedFileConverter.class };


    @SuppressWarnings("unchecked")
	private static final Class<? extends StereotypeHandler>[] STEREOTYPE_HANDLERS = new Class[] {
		ResourceHandler.class,
		ConverterHandler.class,
		InterceptorStereotypeHandler.class,
	};

    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation>[] STEREOTYPES = new Class[] {
    	Resource.class,
    	Convert.class,
    	Component.class
    };

    public static Map<Class<?>, Class<?>> getCachedComponents() {
		return CACHED_COMPONENTS;
	}

    public static Map<Class<?>, Class<?>> getApplicationScoped() {
        return APPLICATION_COMPONENTS;
    }

    public static Map<Class<?>, Class<?>> getRequestScoped() {
        return REQUEST_COMPONENTS;
    }

    public static Class<? extends Converter<?>>[] getBundledConverters() {
        return BUNDLED_CONVERTERS;
    }

    public static Class<? extends Annotation>[] getStereotypes() {
    	return STEREOTYPES;
    }

    public static Class<? extends StereotypeHandler>[] getStereotypeHandlers() {
    	return STEREOTYPE_HANDLERS;
    }

    private static Map<Class<?>, Class<?>> classMap(Class<?>... items) {
        HashMap<Class<?>, Class<?>> map = new HashMap<Class<?>, Class<?>>();
        Iterator<Class<?>> it = Arrays.asList(items).iterator();
        while (it.hasNext()) {
            Class<?> key = it.next();
            Class<?> value = it.next();
            if (value == null) {
                throw new IllegalArgumentException("The number of items should be even.");
            }
            map.put(key, value);
        }
        return Collections.unmodifiableMap(map);
    }


}
