/*
 * Copyright (c) 2000, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.tangosol.internal.util.collection;

import com.tangosol.io.ExternalizableLite;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.WrapperCollections;

import com.tangosol.util.function.Remote;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * A wrapper Map that controls wrapped map's serialization.
 *
 * @param <K> the key type
 * @param <V> the value type
 *
 * @author as  2014.11.19
 * @since 12.2.1
 */
@SuppressWarnings({"NullableProblems"})
public class PortableMap<K, V>
        extends WrapperCollections.AbstractWrapperMap<K, V>
        implements PortableObject, ExternalizableLite
    {
    // ---- constructors ----------------------------------------------------

    /**
     * Construct RemoteMap instance which wraps an instance of a {@link HashMap}.
     */
    public PortableMap()
        {
        }

    /**
     * Construct RemoteMap instance with a specified supplier.
     *
     * @param supplier  a wrapped set supplier
     */
    public PortableMap(Remote.Supplier<Map<K, V>> supplier)
        {
        m_supplier = supplier;
        }

    /**
    * Return the supplier for this map.
    *
    * @return the supplier.
    */
    public Remote.Supplier<Map<K, V>> getSupplier()
        {
        return m_supplier == null ? DEFAULT_SUPPLIER : m_supplier;
        }

    @Override
    protected Map<K, V> getDelegate()
        {
        return m_mapDelegate == null ? (m_mapDelegate = getSupplier().get()) : m_mapDelegate;
        }

    // ---- ExternalizableLite implementation -------------------------------

    @Override
    public void readExternal(DataInput in) throws IOException
        {
        m_supplier = ExternalizableHelper.readObject(in);
        ExternalizableHelper.readMap(in, getDelegate(), null);
        }

    @Override
    public void writeExternal(DataOutput out) throws IOException
        {
        ExternalizableHelper.writeObject(out, m_supplier);
        ExternalizableHelper.writeMap(out, getDelegate());
        }

    // ---- PortableObject implementation -----------------------------------

    @Override
    public void readExternal(PofReader in) throws IOException
        {
        m_supplier    = in.readObject(0);
        m_mapDelegate = in.readMap(1, getDelegate());
        }

    @Override
    public void writeExternal(PofWriter out) throws IOException
        {
        out.writeObject(0, m_supplier);
        out.writeMap(1, getDelegate());
        }

    // ---- static members -------------------------------------------------

    /**
     * Default supplier for the wrapped Map instance, used for
     * interoperability with .NET and C++ clients which are not able to handle
     * lambda-based Java suppliers.
     */
    protected static final Remote.Supplier DEFAULT_SUPPLIER = HashMap::new;

    // ---- data members ----------------------------------------------------

    /**
     * Supplier for the wrapped Map instance.
     */
    @JsonbProperty("supplier")
    protected Remote.Supplier<Map<K, V>> m_supplier;
    }
