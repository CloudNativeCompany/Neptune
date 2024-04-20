/*
 * Copyright (c) 2022 The Neptune Project
 *
 * Licensed under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neptune.common.infra;

import org.neptune.common.util.Requires;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * org.neptune.rpc.util - ExtensionLoader
 *  类似系统提供的ServiceLoader, 并做了一些增强
 *  1. 支持按照名字查找
 *  2. 支持按照权重排序查找
 *  3. 仅支持 {@link Extension} 注解接口的SPI加载
 *
 * @author tony-is-coding
 * @date 2021/12/22 15:00
 */
public final class ExtensionLoader<S> {

    private static final String PREFIX = "META-INF/services/";

    // the class or interface representing the service being loaded
    private final Class<S> service;

    // the class loader used to locate, load, and instantiate providers
    private final ClassLoader loader;

    // cached providers, in instantiation order
    private final LinkedHashMap<String, S> providers = new LinkedHashMap<>();

    // the current lazy-lookup iterator
    private LazyIterator lookupIterator;

    public static <S> ExtensionLoader<S> load(Class<S> service) {
        return ExtensionLoader.load(service, Thread.currentThread().getContextClassLoader());
    }

    public static <S> ExtensionLoader<S> load(Class<S> service, ClassLoader loader) {
        return new ExtensionLoader<>(service, loader);
    }

    public S first() {
        List<S> sortList = sort();
        if (sortList.isEmpty()) {
            throw fail(service, "could not find any implementation for class");
        }
        return sortList.get(0);
    }

    public S find(String implName) {
        for (S s : providers.values()) {
            Extension spi = s.getClass().getAnnotation(Extension.class);
            if (spi != null && spi.name().equalsIgnoreCase(implName)) {
                return s;
            }
        }
        while (lookupIterator.hasNext()) {
            Class<S> cls = lookupIterator.next();
            Extension spi = cls.getAnnotation(Extension.class);
            if (spi != null && spi.name().equalsIgnoreCase(implName)) {
                try {
                    S provider = service.cast(cls.newInstance());
                    providers.put(cls.getName(), provider);
                    return provider;
                } catch (Throwable x) {
                    throw fail(service, "provider " + cls.getName() + " could not be instantiated", x);
                }
            }
        }
        throw fail(service, "provider " + implName + " could not be found");
    }

    public void reload() {
        providers.clear();
        lookupIterator = new LazyIterator(service, loader);
    }

    private ExtensionLoader(Class<S> service, ClassLoader loader) {
        this.service = Requires.requireNotNull(service, "service interface cannot be null");
        this.loader = (loader == null) ? ClassLoader.getSystemClassLoader() : loader;
        reload();
    }


    private Iterator<S> iterator() {
        return new Iterator<S>() {
            final Iterator<Map.Entry<String, S>> knownProviders = providers.entrySet().iterator();
            @Override
            public boolean hasNext() {
                return knownProviders.hasNext() || lookupIterator.hasNext();
            }
            @Override
            public S next() {
                if (knownProviders.hasNext()) {
                    return knownProviders.next().getValue();
                }
                Class<S> cls = lookupIterator.next();
                try {
                    S provider = service.cast(cls.newInstance());
                    providers.put(cls.getName(), provider);
                    return provider;
                } catch (Throwable x) {
                    throw fail(service, "provider " + cls.getName() + " could not be instantiated", x);
                }
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private List<S> sort() {
        Iterator<S> itr = iterator();
        List<S> providers = new ArrayList<>();
        while (itr.hasNext()) {
            providers.add(itr.next());
        }
        if (providers.size() <= 1) {
            return providers;
        }
        providers.sort((o1, o2) -> {
            Extension o1_spi = o1.getClass().getAnnotation(Extension.class);
            Extension o2_spi = o2.getClass().getAnnotation(Extension.class);
            int o1_priority = o1_spi == null ? 0 : o1_spi.priority();
            int o2_priority = o2_spi == null ? 0 : o2_spi.priority();
            // 优先级高的排前边
            return o2_priority - o1_priority;
        });
        return providers;
    }


    private int parseLine(Class<?> service, URL u, BufferedReader r, int lc, List<String> names)
            throws IOException, ServiceConfigurationError {

        String ln = r.readLine();
        if (ln == null) {
            return -1;
        }
        int ci = ln.indexOf('#');
        if (ci >= 0) {
            ln = ln.substring(0, ci);
        }
        ln = ln.trim();
        int n = ln.length();
        if (n != 0) {
            if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0)) {
                throw fail(service, u, lc, "illegal configuration-file syntax");
            }
            int cp = ln.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp)) {
                throw fail(service, u, lc, "illegal provider-class name: " + ln);
            }
            for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
                cp = ln.codePointAt(i);
                if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
                    throw fail(service, u, lc, "Illegal provider-class name: " + ln);
                }
            }
            if (!providers.containsKey(ln) && !names.contains(ln)) {
                names.add(ln);
            }
        }
        return lc + 1;
    }

    private Iterator<String> parse(Class<?> service, URL url) {
        ArrayList<String> names = new ArrayList<>();
        try (InputStream in = url.openStream();
             BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            int lc = 1;
            // noinspection StatementWithEmptyBody
            while ((lc = parseLine(service, url, r, lc, names)) >= 0)
                ;
        } catch (IOException x) {
            throw fail(service, "error reading configuration file", x);
        }
        return names.iterator();
    }

    // 惰性的内部加载器
    private class LazyIterator implements Iterator<Class<S>> {
        Class<S> service;
        ClassLoader loader;
        Enumeration<URL> configs = null;
        Iterator<String> pending = null;
        String nextName = null;

        private LazyIterator(Class<S> service, ClassLoader loader) {
            this.service = service;
            this.loader = loader;
        }

        @Override
        public boolean hasNext() {
            if (nextName != null) {
                return true;
            }
            if (configs == null) {
                try {
                    String fullName = PREFIX + service.getName();
                    if (loader == null) {
                        configs = ClassLoader.getSystemResources(fullName);
                    } else {
                        configs = loader.getResources(fullName);
                    }
                } catch (IOException x) {
                    throw fail(service, "error locating configuration files", x);
                }
            }
            while ((pending == null) || !pending.hasNext()) {
                if (!configs.hasMoreElements()) {
                    return false;
                }
                pending = parse(service, configs.nextElement());
            }
            nextName = pending.next();
            return true;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<S> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            String name = nextName;
            nextName = null;
            Class<?> cls;
            try {
                cls = Class.forName(name, false, loader);
            } catch (ClassNotFoundException x) {
                throw fail(service, "provider " + name + " not found");
            }
            if (!service.isAssignableFrom(cls)) {
                throw fail(service, "provider " + name + " not a subtype");
            }
            return (Class<S>) cls;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


    private static ServiceConfigurationError fail(Class<?> service, String msg, Throwable cause) {
        return new ServiceConfigurationError(service.getName() + ": " + msg, cause);
    }

    private static ServiceConfigurationError fail(Class<?> service, String msg) {
        return new ServiceConfigurationError(service.getName() + ": " + msg);
    }

    private static ServiceConfigurationError fail(Class<?> service, URL url, int line, String msg) {
        return fail(service, url + ":" + line + ": " + msg);
    }

    /**
     * Returns a string describing this service.
     */
    @Override
    public String toString() {
        return "ExtensionLoader[" + service.getName() + "]";
    }
}
