package org.intermine.util;

/*
 * Copyright (C) 2002-2012 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.cglib.proxy.Factory;

import org.intermine.model.FastPathObject;
import org.intermine.model.InterMineObject;

/**
 * Utilities to create DynamicBeans
 *
 * @author Andrew Varley
 */
public final class DynamicUtil
{
    private static Map<Set<? extends Class<?>>, Class<? extends FastPathObject>> classMap
        = new HashMap<Set<? extends Class<?>>, Class<? extends FastPathObject>>();
    private static HashMap<Class<?>, Set<Class<?>>> decomposeMap = new HashMap<Class<?>,
        Set<Class<?>>>();
    private static Map<Class<?>, String> friendlyNameMap = new HashMap<Class<?>, String>();
    private static Map<Class<?>, String> simpleNameMap = new HashMap<Class<?>, String>();

    /**
     * Cannot construct
     */
    private DynamicUtil() {
        // don't instantiate
    }

    /**
     * Create a DynamicBean from a Set of Class objects
     *
     * @param classes the classes and interfaces to extend/implement
     * @return the DynamicBean
     * @throws IllegalArgumentException if there is more than one Class, or if fields are not
     * compatible.
     */
    @SuppressWarnings("unchecked")
    public static synchronized FastPathObject createObject(Set<? extends Class<?>> classes) {
        Class<? extends FastPathObject> requiredClass = classMap.get(classes);
        if (requiredClass != null) {
            return createObject(requiredClass);
        } else {
            Class<?> clazz = null;
            Set<Class<?>> interfaces = new HashSet<Class<?>>();
            for (Class<?> cls : classes) {
                if (cls.isInterface()) {
                    interfaces.add(cls);
                } else if ((clazz == null) || clazz.isAssignableFrom(cls)) {
                    clazz = cls;
                } else if (!cls.isAssignableFrom(clazz)) {
                    throw new IllegalArgumentException("Cannot create a class from multiple"
                            + " classes: " + classes);
                }
            }
            if ((clazz != null) && (!FastPathObject.class.isAssignableFrom(clazz))) {
                throw new ClassCastException("Expected to create a FastPathObject, but was "
                        + clazz.getName());
            }
            Class<? extends FastPathObject> fpclazz = (Class<? extends FastPathObject>) clazz;
            if (fpclazz != null) {
                interfaces.removeAll(Arrays.asList(clazz.getInterfaces()));
            }
            if (interfaces.isEmpty()) {
                if (fpclazz == null) {
                    throw new IllegalArgumentException("Cannot create an object without a class "
                                                       + "for: " + classes);
                } else {
                    try {
                        classMap.put(classes, fpclazz);
                        return fpclazz.newInstance();
                    } catch (InstantiationException e) {
                        IllegalArgumentException e2 = new IllegalArgumentException("Problem running"
                                + " constructor");
                        e2.initCause(e);
                        throw e2;
                    } catch (IllegalAccessException e) {
                        IllegalArgumentException e2 = new IllegalArgumentException("Problem running"
                                + " constructor");
                        e2.initCause(e);
                        throw e2;
                    }
                }
            }
            if ((fpclazz == null) && (interfaces.size() == 1)) {
                try {
                    Class<FastPathObject> retval = (Class<FastPathObject>) Class.forName(interfaces
                            .iterator().next().getName() + "Shadow");
                    classMap.put(classes, retval);
                    return createObject(retval);
                } catch (ClassNotFoundException e) {
                    // No problem - falling back on dynamic
                }
            }
            FastPathObject retval = DynamicBean.create(fpclazz, interfaces.toArray(new Class[] {}));
            classMap.put(classes, retval.getClass());
            return retval;
        }
    }

    /**
     * Create a new object given a class, which may be an interface. This method is equivalent to
     * calling createObject(Collections.singleton(clazz)), except that it is genericised.
     *
     * @param clazz the class of the object to instantiate
     * @param <C> The type of the object that is expected
     * @return the object
     * @throws IllegalArgumentException if an error occurs
     */
    @SuppressWarnings("unchecked")
    public static <C extends FastPathObject> C simpleCreateObject(Class<C> clazz) {
        FastPathObject retval = createObject(Collections.singleton(clazz));
        return (C) retval;
    }

    /**
     * Create a new object given a class (not an interface).  To create an object from interfaces
     * use createObject(Set classes) or simpleCreateObject(Class).
     *
     * @param clazz the class of the object to instantiate
     * @param <C> The type of the object that is expected
     * @return the object
     * @throws IllegalArgumentException if an error occurs
     */
    public static <C extends FastPathObject> C createObject(Class<C> clazz) {
        C retval = null;
        try {
            retval = clazz.newInstance();
        } catch (Exception e) {
            IllegalArgumentException e2 = new IllegalArgumentException();
            e2.initCause(e);
            throw e2;
        }
        if (retval instanceof Factory) {
            ((Factory) retval).setCallback(0, new DynamicBean());
        }
        return retval;
    }

    /**
     * Return the Class for the array of Class objects. This method returns a Class which is
     * descriptive of the component classes, but not necessarily valid for instantiating.
     * This means that passing a single interface into this method will return the interface rather
     * than a class composed from it.
     *
     * @param classes the classes and interfaces to extend/implement
     * @return the Class
     * @throws IllegalArgumentException if there is more than one Class, or if the fields are not
     * compatible.
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends FastPathObject> composeDescriptiveClass(Class<?>... classes) {
        if (classes.length == 1) {
            if (!FastPathObject.class.isAssignableFrom(classes[0])) {
                throw new ClassCastException("Expected a FastPathObject class");
            }
            return (Class<? extends FastPathObject>) classes[0];
        }
        return composeClass(new HashSet<Class<?>>(Arrays.asList(classes)));
    }

    /**
     * Return the Class for the array of Class objects.
     *
     * @param classes the classes and interfaces to extend/implement
     * @return the Class
     * @throws IllegalArgumentException is there is more than one Class, or if the fields are not
     * compatible.
     */
    public static Class<? extends FastPathObject> composeClass(Class<?>... classes) {
        return composeClass(new HashSet<Class<?>>(Arrays.asList(classes)));
    }

    /**
     * Return the Class for a set of Class objects. NOTE: Creating an instance of this class is not
     * trivial: after calling Class.newInstance(), cast the Object to net.sf.cglib.proxy.Factory,
     * and call interceptor(new org.intermine.util.DynamicBean()) on it.
     *
     * @param classes the classes and interfaces to extend/implement
     * @return the Class
     * @throws IllegalArgumentException if there is more than one Class, or if the fields are not
     * compatible.
     */
    public static synchronized Class<? extends FastPathObject> composeClass(Set<Class<?>> classes) {
        Class<? extends FastPathObject> retval = classMap.get(classes);
        if (retval == null) {
            retval = createObject(classes).getClass();
        }
        return retval;
    }

    /**
     * Convert a set of interface names to a set of Class objects
     *
     * @param names the set of interface names
     * @return set of Class objects
     * @throws ClassNotFoundException if class cannot be found
     */
    protected static Set<Class<?>> convertToClasses(Set<String> names)
        throws ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        Iterator<String> iter = names.iterator();
        while (iter.hasNext()) {
            classes.add(Class.forName(iter.next()));
        }

        return classes;
    }

    /**
     * Convert a dynamic Class into a Set of Class objects that comprise it.
     *
     * @param clazz the Class to decompose
     * @return a Set of Class objects
     */
    public static synchronized Set<Class<?>> decomposeClass(Class<?> clazz) {
        Set<Class<?>> retval = decomposeMap.get(clazz);
        if (retval == null) {
            if (net.sf.cglib.proxy.Factory.class.isAssignableFrom(clazz)) {
                // Decompose
                retval = new TreeSet<Class<?>>(new ClassNameComparator());
                retval.add(clazz.getSuperclass());
                Class<?>[] interfs = clazz.getInterfaces();
                for (int i = 0; i < interfs.length; i++) {
                    Class<?> inter = interfs[i];
                    if (net.sf.cglib.proxy.Factory.class != inter) {
                        boolean notIn = true;
                        Iterator<Class<?>> inIter = retval.iterator();
                        while (inIter.hasNext() && notIn) {
                            Class<?> in = inIter.next();
                            if (in.isAssignableFrom(inter)) {
                                // That means that the one already in the return value is more
                                // general than the one we are about to put in, so we can get rid
                                // of the one already in.
                                inIter.remove();
                            }
                            if (inter.isAssignableFrom(in)) {
                                // That means that the one already in the return value is more
                                // specific than the one we would have added, so don't bother.
                                notIn = false;
                            }
                        }
                        if (notIn) {
                            retval.add(inter);
                        }
                    }
                }
            } else if (org.intermine.model.ShadowClass.class.isAssignableFrom(clazz)) {
                try {
                    retval = new TreeSet<Class<?>>(new ClassNameComparator());
                    retval.add((Class<?>) clazz.getField("shadowOf").get(null));
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException("ShadowClass " + clazz.getName() + " has no "
                            + "shadowOf method", e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(clazz.getName()
                            + ".shadowOf method is inaccessible", e);
                }
            } else {
                // Normal class - return it.
                retval = new TreeSet<Class<?>>(new ClassNameComparator());
                retval.add(clazz);
            }
            decomposeMap.put(clazz, retval);
        }
        return retval;
    }

    /**
     * Create an outline business object from a class name and a list of interface names
     * @param className the class name
     * @param implementations a space separated list of interface names
     * @return the materialised business object
     * @throws ClassNotFoundException if className can't be found
     */
    public static FastPathObject instantiateObject(String className, String implementations)
        throws ClassNotFoundException {

        Set<String> classNames = new HashSet<String>();

        if (className != null && !"".equals(className) && !"".equals(className.trim())) {
            classNames.add(className.trim());
        }

        if (implementations != null) {
            classNames.addAll(StringUtil.tokenize(implementations));
        }

        if (classNames.size() == 0) {
            throw new RuntimeException("attempted to create an object without specifying any "
                                       + "classes or interfaces");
        }

        return createObject(convertToClasses(classNames));
    }

    /**
     * Creates a friendly name for a given class.
     *
     * @param clazz the class
     * @return a String describing the class, without package names
     */
    public static synchronized String getFriendlyName(Class<?> clazz) {
        String retval = friendlyNameMap.get(clazz);
        if (retval == null) {
            retval = "";
            Iterator<Class<?>> iter = decomposeClass(clazz).iterator();
            boolean needComma = false;
            while (iter.hasNext()) {
                Class<?> constit = iter.next();
                retval += needComma ? "," : "";
                needComma = true;
                retval += constit.getName().substring(constit.getName().lastIndexOf('.') + 1);
            }
            friendlyNameMap.put(clazz, retval);
        }
        return retval;
    }

    /**
     * Creates a friendly description of an object - that is, the class and the ID (if it has one).
     *
     * @param o the object to be described
     * @return a String description
     */
    public static String getFriendlyDesc(Object o) {
        if (o instanceof InterMineObject) {
            return getFriendlyName(o.getClass()) + ":" + ((InterMineObject) o).getId();
        } else {
            return o.toString();
        }
    }

    /**
     * Returns the simple class name for the given class or throws an exception if
     * there are more than one.
     * @param clazz the class
     * @return the simple class name
     */
    public static synchronized String getSimpleClassName(Class<?> clazz) {
        String retval = simpleNameMap.get(clazz);
        System.out.println("JDJDJD::"+clazz.toString()); // TODO remove
        if (retval == null) {
            Set<Class<?>> decomposedClass = decomposeClass(clazz);
            
            System.out.println("JDJDJD::"+decomposedClass.toString()); // TODO remove
            
            if (decomposedClass.size() > 1) {
//                throw new IllegalArgumentException("No simple name for class: " //c'd out by JD
//                                                   + getFriendlyName(clazz));
                retval = decomposedClass.iterator().next().getName(); // JD
                simpleNameMap.put(clazz, retval);
            } else {
                retval = decomposedClass.iterator().next().getName();
                simpleNameMap.put(clazz, retval);
            }

        }
        return retval;
    }

    /**
     * Returns the simple class name for the given object or throws an exception if
     * there are more than one.
     * @param obj an object from the model
     * @return the simple class name
     */
    public static synchronized String getSimpleClassName(FastPathObject obj) {
        return getSimpleClassName(obj.getClass());
    }

    /**
     * Returns true if sup is a superclass of sub (or the same), taking into account dynamic
     * classes.
     *
     * @param sup the supposed superclass
     * @param sub the supposed subclass
     * @return a boolean
     */
    public static boolean isAssignableFrom(Class<?> sup, Class<?> sub) {
        Set<Class<?>> classes = decomposeClass(sup);
        for (Class<?> clazz : classes) {
            if (!clazz.isAssignableFrom(sub)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if obj is an instance of clazz, taking into account dynamic classes.
     *
     * @param obj the Object
     * @param clazz the Class
     * @return a boolean
     */
    public static boolean isInstance(Object obj, Class<?> clazz) {
        return isAssignableFrom(clazz, obj.getClass());
    }

    private static class ClassNameComparator implements Comparator<Class<?>>
    {
        public int compare(Class<?> a, Class<?> b) {
            return a.getName().compareTo(b.getName());
        }
    }

    /**
     * Returns the result of decomposeClass if that is a single class, or throws an exception if
     * there are more than one.
     *
     * @param clazz the class
     * @return the corresponding non-dynamic class
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends FastPathObject> getSimpleClass(
            Class<? extends FastPathObject> clazz) {
        Set<Class<?>> decomposed = decomposeClass(clazz);
        if (decomposed.size() > 1) {
            throw new IllegalArgumentException("No simple class for " + getFriendlyName(clazz));
        }
        return (Class) decomposed.iterator().next();
    }

    /**
     * For the given objet returns the result of decomposeClass if that is a single class, or throws
     * an exception if there are more than one class.
     *
     * @param obj an object from the model
     * @return the corresponding non-dynamic class
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends FastPathObject> getSimpleClass(FastPathObject obj) {
        return getSimpleClass(obj.getClass());
    }
}
