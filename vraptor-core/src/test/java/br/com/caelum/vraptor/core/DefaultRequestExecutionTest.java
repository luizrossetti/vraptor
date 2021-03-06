package br.com.caelum.vraptor.core;

import java.io.IOException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.extra.ForwardToDefaultViewInterceptor;
import br.com.caelum.vraptor.interceptor.ExecuteMethodInterceptor;
import br.com.caelum.vraptor.interceptor.InstantiateInterceptor;
import br.com.caelum.vraptor.interceptor.InterceptorListPriorToExecutionExtractor;
import br.com.caelum.vraptor.interceptor.OutjectResult;
import br.com.caelum.vraptor.interceptor.ParametersInstantiatorInterceptor;
import br.com.caelum.vraptor.interceptor.ResourceLookupInterceptor;
import br.com.caelum.vraptor.interceptor.download.DownloadInterceptor;
import br.com.caelum.vraptor.interceptor.multipart.MultipartInterceptor;

public class DefaultRequestExecutionTest {

    private Mockery mockery;
    private InterceptorStack stack;
    private DefaultRequestExecution execution;
    private InstantiateInterceptor instantiator;

    @Before
    public void setup() {
        this.mockery = new Mockery();
        this.stack = mockery.mock(InterceptorStack.class);
        this.instantiator = new InstantiateInterceptor(null);
        this.execution = new DefaultRequestExecution(stack, instantiator);
    }

    @Test
    public void shouldAddInterceptorsInOrder() throws InterceptionException, IOException {
        final Sequence sequence = mockery.sequence("executionSequence");
        mockery.checking(new Expectations() {
            {
                one(stack).add(ResourceLookupInterceptor.class); inSequence(sequence);
                one(stack).add(URLParameterExtractorInterceptor.class);
                inSequence(sequence);
                one(stack).add(InterceptorListPriorToExecutionExtractor.class); inSequence(sequence);
                one(stack).add(MultipartInterceptor.class); inSequence(sequence);
                one(stack).add(instantiator); inSequence(sequence);
                one(stack).add(ParametersInstantiatorInterceptor.class);
                inSequence(sequence);
                one(stack).add(ExecuteMethodInterceptor.class); inSequence(sequence);
                one(stack).add(OutjectResult.class); inSequence(sequence);
                one(stack).add(DownloadInterceptor.class); inSequence(sequence);
                one(stack).add(ForwardToDefaultViewInterceptor.class); inSequence(sequence);
                one(stack).next(null, null); inSequence(sequence);
            }
        });
        execution.execute();
        mockery.assertIsSatisfied();
    }

}
