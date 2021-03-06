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
package br.com.caelum.vraptor.view;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.vraptor.core.DefaultMethodInfo;
import br.com.caelum.vraptor.http.MutableRequest;
import br.com.caelum.vraptor.http.route.Router;
import br.com.caelum.vraptor.proxy.Proxifier;
import br.com.caelum.vraptor.resource.DefaultResourceMethod;
import br.com.caelum.vraptor.resource.HttpMethod;
import br.com.caelum.vraptor.resource.ResourceMethod;

public class DefaultPageResultTest {

    private Mockery mockery;
    private MutableRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private ResourceMethod method;
    private PathResolver fixedResolver;
    private DefaultMethodInfo requestInfo;
	private DefaultPageResult view;
	private Router router;

    @Before
    public void setup() {
        this.mockery = new Mockery();
        request = mockery.mock(MutableRequest.class);
        response = mockery.mock(HttpServletResponse.class);
        dispatcher = mockery.mock(RequestDispatcher.class);
        method = DefaultResourceMethod.instanceFor(AnyResource.class, AnyResource.class.getDeclaredMethods()[0]);
        requestInfo = new DefaultMethodInfo();
        requestInfo.setResourceMethod(method);
        fixedResolver = new PathResolver() {
            public String pathFor(ResourceMethod method) {
                return "fixed";
            }
        };
        router = mockery.mock(Router.class);
		view = new DefaultPageResult(request, response, requestInfo, fixedResolver, mockery.mock(Proxifier.class), router);
    }

    public static class AnyResource {
    	public void method() {
    	}
    }
    @Test
	public void shouldRedirectWhenUriDoesntBelongToAnyLogic() throws Exception {
		mockery.checking(new Expectations() {
			{
				one(router).parse("/any/url", HttpMethod.GET, request);
				will(returnValue(null));

				one(response).sendRedirect("/any/url");
			}
		});

		view.redirect("/any/url");
		mockery.assertIsSatisfied();
	}
    @Test
    public void shouldForwardWhenUriDoesntBelongToAnyLogic() throws Exception {
    	mockery.checking(new Expectations() {
    		{
				one(router).parse("/any/url", HttpMethod.GET, request);
				will(returnValue(null));

    			one(request).getRequestDispatcher("/any/url");
                will(returnValue(dispatcher));
                one(dispatcher).forward(request, response);

                one(request).getParameter("_method");
                will(returnValue("GET"));
    		}
    	});

    	view.forward("/any/url");
    	mockery.assertIsSatisfied();
    }
    @Test
    public void shouldThrowExceptionWhenUriDoesntBelongToAnyLogicOnRedirect() throws Exception {
    	mockery.checking(new Expectations() {
    		{
    			one(router).parse("/any/url", HttpMethod.GET, request);
    			will(returnValue(method));

    			never(response).sendRedirect("/any/url");
    		}
    	});

    	try {
			view.redirect("/any/url");
			Assert.fail("Should throw exception");
		} catch (ResultException e) {
			mockery.assertIsSatisfied();
		}
    }
    @Test
    public void shouldThrowExceptionWhenUriDoesntBelongToAnyLogicOnForward() throws Exception {
    	mockery.checking(new Expectations() {
    		{
    			one(router).parse("/any/url", HttpMethod.GET, request);
    			will(returnValue(method));

    			never(request).getRequestDispatcher("/any/url");

                one(request).getParameter("_method");
                will(returnValue("GET"));
    		}
    	});

    	try {
    		view.forward("/any/url");
    		Assert.fail("Should throw exception");
    	} catch (ResultException e) {
    		mockery.assertIsSatisfied();
    	}
    }
    @Test
    public void shouldAllowCustomPathResolverWhileForwarding() throws ServletException, IOException {
        mockery.checking(new Expectations() {
            {
                one(request).getRequestDispatcher("fixed");
                will(returnValue(dispatcher));
                one(dispatcher).forward(request, response);
            }
        });
        view.forward();
        mockery.assertIsSatisfied();
    }


    @Test
    public void shouldAllowCustomPathResolverWhileIncluding() throws ServletException, IOException {
        mockery.checking(new Expectations() {
            {
                one(request).getRequestDispatcher("fixed");
                will(returnValue(dispatcher));
                one(dispatcher).include(request, response);
            }
        });
        view.include();
        mockery.assertIsSatisfied();
    }

}
