package br.com.caelum.vraptor.vraptor2;

import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.Interceptor;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.resource.ResourceMethod;
import br.com.caelum.vraptor.view.jsp.PageResult;

public class ViewInterceptor implements Interceptor {

    private RequestResult reqResult;
    private final PageResult result;
    private final ComponentInfoProvider info;

    public ViewInterceptor(PageResult result, RequestResult reqResult, ComponentInfoProvider info) {
        this.result = result;
        this.reqResult = reqResult;
        this.info = info;
    }

    public boolean accepts(ResourceMethod method) {
        return true;
    }

    public void intercept(InterceptorStack stack, ResourceMethod method, Object resourceInstance) throws InterceptionException {
        if (info.shouldShowView(method)) {
            this.result.forward(reqResult.getValue());
        }
    }

}