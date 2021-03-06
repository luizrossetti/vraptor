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
package br.com.caelum.vraptor.http.route;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.proxy.Proxifier;
import br.com.caelum.vraptor.resource.HttpMethod;
import br.com.caelum.vraptor.resource.ResourceClass;

/**
 * The default parser routes creator uses the path annotation to create rules.
 * Note that methods are only registered to be public accessible if the type is
 * annotated with @Resource.
 *
 * If you want to override the convention for default URI, you can create a class like:
 *
 * public class MyRoutesParser extends PathAnnotationRoutesParser {
 * 		//delegate constructor
 * 		protected String extractControllerNameFrom(Class<?> type) {
 * 			return //your convention here
 * 		}
 *
 * 		protected String defaultUriFor(String controllerName, String methodName) {
 * 			return //your convention here
 * 		}
 * }
 * @author Guilherme Silveira
 * @author Lucas Cavalcanti
 */
@ApplicationScoped
public class PathAnnotationRoutesParser implements RoutesParser {

	private static final Logger logger = LoggerFactory.getLogger(PathAnnotationRoutesParser.class);

    private final Proxifier proxifier;
	private final TypeFinder finder;

    public PathAnnotationRoutesParser(Proxifier proxifier, TypeFinder finder) {
        this.proxifier = proxifier;
		this.finder = finder;
    }

    public List<Route> rulesFor(ResourceClass resource) {
        List<Route> routes = new ArrayList<Route>();
        Class<?> baseType = resource.getType();
        registerRulesFor(baseType, baseType, routes);
        return routes;
    }

    private void registerRulesFor(Class<?> actualType, Class<?> baseType, List<Route> routes) {
        if (actualType.equals(Object.class)) {
            return;
        }
        for (Method javaMethod : actualType.getDeclaredMethods()) {
            if (isEligible(javaMethod)) {
                String uri = getUriFor(javaMethod, baseType);
                RouteBuilder rule = new RouteBuilder(proxifier, finder, uri);
                for (HttpMethod m : HttpMethod.values()) {
                    if (javaMethod.isAnnotationPresent(m.getAnnotation())) {
                        rule.with(m);
                    }
                }
                if (javaMethod.isAnnotationPresent(Path.class)) {
                	rule.withPriority(javaMethod.getAnnotation(Path.class).priority());
                }
                rule.is(baseType, javaMethod);
                routes.add(rule.build());
            }
        }
        registerRulesFor(actualType.getSuperclass(), baseType, routes);
    }

    private boolean isEligible(Method javaMethod) {
        return Modifier.isPublic(javaMethod.getModifiers()) && !Modifier.isStatic(javaMethod.getModifiers());
    }

    private String getUriFor(Method javaMethod, Class<?> type) {
        if (javaMethod.isAnnotationPresent(Path.class)) {
            String uri = javaMethod.getAnnotation(Path.class).value();
            if (!uri.startsWith("/")) {
            	logger.warn("All uris from @Path must start with a '/'. Please change it on " + javaMethod);
            	uri = "/" + uri;
            }
			return uri;
        }
        return defaultUriFor(extractControllerNameFrom(type), javaMethod.getName());
    }

    /**
     * You can override this method for use a different convention for
     * your controller name, given a type
     */
    protected String extractControllerNameFrom(Class<?> type) {
        String baseName = lowerFirstCharacter(type.getSimpleName());
        if (baseName.endsWith("Controller")) {
            return "/" + baseName.substring(0, baseName.lastIndexOf("Controller"));
        }
        return "/" + baseName;
    }

    /**
     * You can override this method for use a different convention for
     * your default URI, given a controller name and a method name
     */
    protected String defaultUriFor(String controllerName, String methodName) {
    	return controllerName + "/" + methodName;
    }

    private String lowerFirstCharacter(String baseName) {
        return baseName.toLowerCase().substring(0, 1) + baseName.substring(1, baseName.length());
    }

}
