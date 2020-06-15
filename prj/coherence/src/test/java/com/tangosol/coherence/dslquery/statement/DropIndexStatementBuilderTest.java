/*
 * Copyright (c) 2000, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.tangosol.coherence.dslquery.statement;

import com.tangosol.coherence.dslquery.CohQLException;
import com.tangosol.coherence.dslquery.CoherenceQueryLanguage;
import com.tangosol.coherence.dslquery.ExecutionContext;
import com.tangosol.coherence.dsltools.termtrees.NodeTerm;
import com.tangosol.coherence.dsltools.termtrees.Terms;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.TypeAssertion;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author jk  2013.12.11
 */
public class DropIndexStatementBuilderTest
    {
    @Test
    public void shouldRealizeQuery()
            throws Exception
        {
        CoherenceQueryLanguage language = new CoherenceQueryLanguage();
        ExecutionContext       context  = mock(ExecutionContext.class);
        NodeTerm               term     = (NodeTerm) Terms.create("sqlDropIndexNode(from('test'),"
                                              + "extractor(derefNode(identifier(foo))))");

        when(context.getCoherenceQueryLanguage()).thenReturn(language);

        DropIndexStatementBuilder builder = DropIndexStatementBuilder.INSTANCE;

        DropIndexStatementBuilder.DropIndexStatement query
                = builder.realize(context, term, null, null);

        assertThat(query.f_sCacheName, is("test"));
        assertThat(query.f_extractor, is((Object) new ReflectionExtractor("getFoo")));
        }

    @Test
    public void shouldThrowExceptionIfCacheIsEmptyString()
            throws Exception
        {
        expectedEx.expect(CohQLException.class);
        expectedEx.expectMessage("Cache name needed for drop index");

        ExecutionContext context = mock(ExecutionContext.class);
        NodeTerm         term    = (NodeTerm) Terms.create("sqlDropIndexNode(from(''),"
                                       + "extractor(derefNode(identifier(foo))))");

        DropIndexStatementBuilder.INSTANCE.realize(context, term, null, null);
        }

    @Test
    public void shouldThrowExceptionIfCacheIsBlank()
            throws Exception
        {
        expectedEx.expect(CohQLException.class);
        expectedEx.expectMessage("Cache name needed for drop index");

        ExecutionContext context = mock(ExecutionContext.class);
        NodeTerm         term    = (NodeTerm) Terms.create("sqlDropIndexNode(from(),"
                                       + "extractor(derefNode(identifier(foo))))");

        DropIndexStatementBuilder.INSTANCE.realize(context, term, null, null);
        }

    @Test
    public void shouldThrowExceptionIfCacheIsMissing()
            throws Exception
        {
        expectedEx.expect(CohQLException.class);
        expectedEx.expectMessage("Cache name needed for drop index");

        ExecutionContext context = mock(ExecutionContext.class);
        NodeTerm         term    = (NodeTerm) Terms.create("sqlDropIndexNode("
                                       + "extractor(derefNode(identifier(foo))))");

        DropIndexStatementBuilder.INSTANCE.realize(context, term, null, null);
        }

    @Test
    public void shouldThrowExceptionIfExtractorIsBlank()
            throws Exception
        {
        expectedEx.expect(CohQLException.class);
        expectedEx.expectMessage("ValueExtractor(s) needed for drop index");

        ExecutionContext context = mock(ExecutionContext.class);
        NodeTerm         term    = (NodeTerm) Terms.create("sqlDropIndexNode(from('test'),extractor())");

        DropIndexStatementBuilder.INSTANCE.realize(context, term, null, null);
        }

    @Test
    public void shouldThrowExceptionIfExtractorIsMissing()
            throws Exception
        {
        expectedEx.expect(CohQLException.class);
        expectedEx.expectMessage("ValueExtractor(s) needed for drop index");

        ExecutionContext context = mock(ExecutionContext.class);
        NodeTerm         term    = (NodeTerm) Terms.create("sqlDropIndexNode(from('test'))");

        DropIndexStatementBuilder.INSTANCE.realize(context, term, null, null);
        }

    @Test
    public void shouldAssertCacheExistsInSanityCheck()
            throws Exception
        {
        String           cacheName = "test";
        ValueExtractor   extractor = new ReflectionExtractor("getFoo");
        ExecutionContext context   = mock(ExecutionContext.class);

        DropIndexStatementBuilder.DropIndexStatement query = new DropIndexStatementBuilder.DropIndexStatement("test", extractor)
                {
                @Override
                protected void assertCacheName(String sName, ExecutionContext context)
                    {
                    }
                };

        DropIndexStatementBuilder.DropIndexStatement spyQuery = spy(query);

        spyQuery.sanityCheck(context);

        verify(spyQuery).assertCacheName(cacheName, context);
        }

    @Test
    public void shouldDropIndex()
            throws Exception
        {
        ValueExtractor           extractor    = new ReflectionExtractor("getFoo");
        ConfigurableCacheFactory cacheFactory = mock(ConfigurableCacheFactory.class);
        NamedCache               cache        = mock(NamedCache.class);
        ExecutionContext         context      = mock(ExecutionContext.class);

        when(context.getCacheFactory()).thenReturn(cacheFactory);
        when(cacheFactory.ensureTypedCache(anyString(), nullable(ClassLoader.class), any(TypeAssertion.class))).thenReturn(cache);

        DropIndexStatementBuilder.DropIndexStatement statement
                = new DropIndexStatementBuilder.DropIndexStatement("test", extractor);

        statement.execute(context);

        InOrder inOrder = inOrder(cacheFactory, cache);

        inOrder.verify(cacheFactory).ensureTypedCache(eq("test"), nullable(ClassLoader.class), any(TypeAssertion.class));
        inOrder.verify(cache).removeIndex(extractor);
        }

    /**
     * JUnit rule to use to capture expected exceptions
     */
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    }
