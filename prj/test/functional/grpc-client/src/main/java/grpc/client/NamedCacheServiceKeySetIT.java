/*
 * Copyright (c) 2000, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package grpc.client;

import com.oracle.coherence.client.NamedCacheGrpcClient;
import com.oracle.coherence.grpc.Requests;

import com.tangosol.io.DefaultSerializer;
import com.tangosol.io.Serializer;
import com.tangosol.io.SerializerFactory;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.OperationalContext;

import com.tangosol.net.grpc.GrpcDependencies;
import com.tangosol.util.Base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.extension.RegisterExtension;

import org.junit.jupiter.params.ParameterizedTest;

import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests for {@link NamedCacheGrpcClient} key set methods.
 *
 * @author Jonathan Knight  2019.11.12
 * @since 20.06
 */
class NamedCacheServiceKeySetIT
    {
    // ----- test lifecycle -------------------------------------------------

    @BeforeAll
    static void setupBaseTest() throws Exception
        {
        s_realCache = s_serverHelper.getSession().getCache(CACHE_NAME);
        }

    @BeforeEach
    void beforeEach()
        {
        s_realCache.clear();
        }

    private <K, V> NamedCache<K, V> createClient(String serializerName, Serializer serializer)
        {
        return s_serverHelper.createClient(GrpcDependencies.DEFAULT_SCOPE, CACHE_NAME, serializerName, serializer);
        }

    // ----- test methods ---------------------------------------------------

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldGetKeySetOfEmptyCache(String sSerializerName, Serializer serializer)
        {
        s_realCache.clear();
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);

        Set<String> keys = cache.keySet();
        assertThat(keys, is(notNullValue()));
        assertThat(keys.isEmpty(), is(true));
        assertThat(keys.size(), is(0));
        }

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldGetKeySetIteratorOfEmptyCache(String sSerializerName, Serializer serializer)
        {
        s_realCache.clear();
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);

        Set<String> keys = cache.keySet();
        assertThat(keys, is(notNullValue()));

        Iterator<String> iterator = keys.iterator();
        assertThat(iterator, is(notNullValue()));
        assertThat(iterator.hasNext(), is(false));
        }

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldGetKeySetOfPopulatedCache(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);

        Set<String> keys = cache.keySet();
        assertThat(keys, is(notNullValue()));
        assertThat(keys.isEmpty(), is(false));
        assertThat(keys.size(), is(s_realCache.size()));
        }

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldGetKeySetIteratorOfPopulatedCache(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);

        Set<String> keys = cache.keySet();
        assertThat(keys, is(notNullValue()));

        Iterator<String> iterator = keys.iterator();
        assertThat(iterator, is(notNullValue()));
        assertThat(iterator.hasNext(), is(true));

        Set<String> set = new HashSet<>();
        int count = 0;
        while (iterator.hasNext())
            {
            set.add(iterator.next());
            count++;
            }

        assertThat(count, is(s_realCache.size()));
        assertThat(set, is(s_realCache.keySet()));
        }

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldRemoveFromKeySetIteratorOfPopulatedCache(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);

        Set<String> keys = cache.keySet();
        assertThat(keys, is(notNullValue()));

        Iterator<String> iterator = keys.iterator();
        assertThat(iterator, is(notNullValue()));
        assertThat(iterator.hasNext(), is(true));

        while (iterator.hasNext())
            {
            iterator.next();
            iterator.remove();
            }

        assertThat(s_realCache.isEmpty(), is(true));
        }

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldClearKeySet(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);

        Set<String> keys = cache.keySet();
        keys.clear();
        assertThat(s_realCache.isEmpty(), is(true));
        }

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldContainAllKeys(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);

        Set<String> keys   = cache.keySet();
        boolean     result = keys.containsAll(Arrays.asList("key-1", "key-2", "key-3"));
        assertThat(result, is(true));
        }

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldNotContainAllKeys(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);

        Set<String> keys   = cache.keySet();
        boolean     result = keys.containsAll(Arrays.asList("key-1", "key-B", "key-3"));
        assertThat(result, is(false));
        }

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldContainKeys(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);

        Set<String> keys = cache.keySet();

        for (String key : s_realCache.keySet())
            {
            boolean result = keys.contains(key);
            assertThat("keySet should contain key " + key, result, is(true));
            }
        }

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldNotContainKey(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);

        Set<String> keys   = cache.keySet();
        boolean     result = keys.contains("key-A");
        assertThat(result, is(false));
        }

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldBeEqual(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);
        Set<String>                set   = new HashSet<>(s_realCache.keySet());

        Set<String> keys = cache.keySet();
        assertThat(keys.equals(set), is(true));
        }

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldNotBeEqual(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);
        Set<String>                set   = new HashSet<>(Arrays.asList("key-1", "key-2", "key-3"));

        Set<String> keys = cache.keySet();
        assertThat(keys.equals(set), is(false));
        }

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldHaveSameHashCode(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);
        Set<String>                set   = new HashSet<>(s_realCache.keySet());

        Set<String> keys = cache.keySet();
        assertThat(keys.hashCode(), is(set.hashCode()));
        }

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldRemoveAll(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);
        Set<String>                set   = new HashSet<>(Arrays.asList("key-1", "key-2", "key-3", "key-A", "key-B"));

        Set<String> keys   = cache.keySet();
        boolean     result = keys.removeAll(set);
        assertThat(result, is(true));
        assertThat(s_realCache.size(), is(7));
        assertThat(s_realCache.containsKey("key-1"), is(false));
        assertThat(s_realCache.containsKey("key-2"), is(false));
        assertThat(s_realCache.containsKey("key-3"), is(false));
        }

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldNotRemoveAll(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);
        Set<String>                set   = new HashSet<>(Arrays.asList("key-A", "key-B"));

        Set<String> keys   = cache.keySet();
        boolean     result = keys.removeAll(set);
        assertThat(result, is(false));
        assertThat(s_realCache.size(), is(10));
        }

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldRetainAll(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);
        Set<String>                set   = new HashSet<>(Arrays.asList("key-1", "key-2", "key-3", "key-A", "key-B"));

        Set<String> keys   = cache.keySet();
        boolean     result = keys.retainAll(set);
        assertThat(result, is(true));
        assertThat(s_realCache.size(), is(3));
        assertThat(s_realCache.containsKey("key-1"), is(true));
        assertThat(s_realCache.containsKey("key-2"), is(true));
        assertThat(s_realCache.containsKey("key-3"), is(true));
        }

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldRetainAllWithNoneMatchingAndAllRemoved(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);
        Set<String>                set   = new HashSet<>(Arrays.asList("key-A", "key-B"));

        Set<String> keys   = cache.keySet();
        boolean     result = keys.retainAll(set);
        assertThat(result, is(true));
        assertThat(s_realCache.size(), is(0));
        }

    @ParameterizedTest(name = "{index} serializer={0}")
    @MethodSource("serializers")
    void shouldRetainAllWhereAllMatchAndNoneRemoved(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);

        Set<String> keys   = cache.keySet();
        boolean     result = keys.retainAll(s_realCache.keySet());
        assertThat(result, is(false));
        assertThat(s_realCache.size(), is(10));
        }


    // ToDo: enable this when entrySet(Filter) is implemented
//    @ParameterizedTest(name = "{index} serializer={0}")
//    @MethodSource("serializers")
    void shouldConvertToObjectArray(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);

        Set<String> entries  = cache.keySet();
        Object[]    result   = entries.toArray();
        Object[]    expected = s_realCache.entrySet().toArray();
        assertThat(result, is(expected));
        }

    // ToDo: enable this when entrySet(Filter) is implemented
//    @ParameterizedTest(name = "{index} serializer={0}")
//    @MethodSource("serializers")
    void shouldConvertToArray(String sSerializerName, Serializer serializer)
        {
        clearAndPopulateRealCache(10);
        NamedCache<String, String> cache = createClient(sSerializerName, serializer);

        Set<String> keys     = cache.keySet();
        String[]    result   = keys.toArray(new String[0]);
        String[]    expected = s_realCache.keySet().toArray(new String[0]);
        assertThat(result, is(expected));
        }

    // ----- helper methods -------------------------------------------------

    protected void clearAndPopulateRealCache(int count)
        {
        s_realCache.clear();
        for (int i = 0; i < count; i++)
            {
            s_realCache.put("key-" + i, "value-" + i);
            }
        }

    /**
     * Obtain the {@link Serializer} instances to use for parameterized
     * test {@link org.junit.jupiter.params.provider.Arguments}.
     *
     * @return the {@link Serializer} instances to use for test
     * {@link org.junit.jupiter.params.provider.Arguments}
     */
    protected static Stream<Arguments> serializers()
        {
        List<Arguments> args   = new ArrayList<>();
        ClassLoader     loader = Base.getContextClassLoader();

        args.add(Arguments.of("", new DefaultSerializer()));

        OperationalContext ctx = (OperationalContext) CacheFactory.getCluster();
        for (Map.Entry<String, SerializerFactory> entry : ctx.getSerializerMap().entrySet())
            {
            args.add(Arguments.of(entry.getKey(), entry.getValue().createSerializer(loader)));
            }

        return args.stream();
        }

    // ----- constants ------------------------------------------------------

    protected static final String CACHE_NAME = "testCache";

    // ----- data members ---------------------------------------------------

    protected static NamedCache<String, String> s_realCache;

    @RegisterExtension
    protected static ServerHelper s_serverHelper = new ServerHelper()
            .setProperty("coherence.ttl",         "0")
            .setProperty("coherence.wka",         "127.0.0.1")
            .setProperty("coherence.localhost",   "127.0.0.1")
            .setProperty("coherence.clustername", "NamedCacheServiceKeySetIT")
            .setProperty("coherence.override",    "coherence-json-override.xml")
            .setProperty("coherence.pof.config",  "test-pof-config.xml")
            .setProperty("coherence.cacheconfig", "coherence-config.xml");
    }
