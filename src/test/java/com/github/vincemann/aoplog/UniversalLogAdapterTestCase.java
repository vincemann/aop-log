/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests string building by {@link UniversalLogAdapter}.
 */
public class UniversalLogAdapterTestCase {

    private UniversalLogAdapter logAdapter;

    @Before
    public void setUp() throws Exception {
        logAdapter = new UniversalLogAdapter(false, null, false);
    }

    private String identityHashCode(Object value) {
        return Integer.toHexString(System.identityHashCode(value));
    }

    @Test
    public void testAsStringNull() throws Exception {
        assertEquals("NIL", logAdapter.asString(null, null, null));
    }

    @Test
    public void testAsStringObject() throws Exception {
        assertEquals("Object[]", logAdapter.asString(new Object(), null, null));
    }

    @Test
    public void testAsStringNullRef() throws Exception {
        class NullRef {
            private String s;
            private String[] arr = new String[]{null};
        }
        assertEquals("NullRef[s=NIL;arr={NIL}]", logAdapter.asString(new NullRef(), null, null));

        logAdapter = new UniversalLogAdapter(true, null, false);
        assertEquals("NullRef[arr={NIL}]", logAdapter.asString(new NullRef(), null, null));
    }

    @Test
    public void testAsStringInheritance() throws Exception {
        class Int {
            private int i = 1;
        }
        assertEquals("Int[i=1]", logAdapter.asString(new Int(), null, null));

        class NamedInt extends Int {
            private String name = "s";
        }
        assertEquals("NamedInt[name=s;i=1]", logAdapter.asString(new NamedInt(), null, null));
    }

    @Test
    public void testAsStringArray() throws Exception {
        assertEquals("int[][{1,2}]", logAdapter.asString(new int[]{1, 2}, null, null));
        assertEquals("int[][][{{1},{2}}]", logAdapter.asString(new int[][]{{1}, {2}}, null, null));
    }

    @Test
    public void testAsStringCollection() throws Exception {
        List<Integer> collection = new ArrayList<Integer>(Arrays.asList(1, 2));
        assertEquals("ArrayList[{1,2}]", logAdapter.asString(collection, null, null));
    }

    @Test
    public void testAsStringMap() throws Exception {
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        map.put("s1", 1);
        map.put("s2", 2);
        assertEquals("LinkedHashMap[{s1=1,s2=2}]", logAdapter.asString(map, null, null));
    }

    @Test
    public void testAsStringCycleItself() throws Exception {
        class CycleItself {
            private CycleItself c = this;
        }
        CycleItself value = new CycleItself();
        assertEquals("CycleItself[c=CycleItself@" + identityHashCode(value) + "]", logAdapter.asString(value, null, null));
    }

    @Test
    public void testAsStringCycleItselfInArr() throws Exception {
        class CycleItselfInArr {
            private CycleItselfInArr[] c = new CycleItselfInArr[]{this};
        }
        CycleItselfInArr value = new CycleItselfInArr();
        String hash = Integer.toHexString(System.identityHashCode(value));
        assertEquals("CycleItselfInArr[c={CycleItselfInArr@" + hash + "}]", logAdapter.asString(value, null, null));
    }


    @Test
    public void testAsStringCycleInArr() throws Exception {
        // memo: Tests for filling ToString.valuesInProgress in ToString.parse():
        // the filling prevents java.lang.StackOverflowError
        class CycleInArr {
            private Object[] c = new Object[1];

            {
                c[0] = c;
            }
        }
        CycleInArr value = new CycleInArr();
        assertEquals("CycleInArr[c={Object[]@" + identityHashCode(value.c) + "}]", logAdapter.asString(value, null, null));
    }

    @Test
    public void testAsStringCycleArray() throws Exception {
        // memo: Tests for filling ToString.valuesInProgress in ToString.addStart():
        // the filling prevents java.lang.StackOverflowError or/and incorrect output (depends on the same filling in ToString.parse())
        Object[] arr = new Object[1];
        arr[0] = arr;
        // correct Object[][{Object[]@17182c1}]
        // wrong   Object[][{{Object[]@601bb1}}]
        assertEquals("Object[][{Object[]@" + identityHashCode(arr) + "}]", logAdapter.asString(arr, null, null));
    }

    @Test
    public void testAsStringCrossReference() throws Exception {
        class CrossReference {
            private Object[] c = new Object[1];
        }
        CrossReference a = new CrossReference();
        CrossReference b = new CrossReference();
        a.c[0] = b;
        b.c[0] = a;
        assertEquals("CrossReference[c={CrossReference@" + identityHashCode(b) + "}]", logAdapter.asString(a, null, null));
    }

    @Test
    public void testAsStringSamePrimitive() throws Exception {
        class SamePrimitive {
            private Object[] c = new Object[2];

            {
                c[0] = Boolean.TRUE;
                c[1] = new Object[]{Boolean.TRUE};
            }
        }
        assertEquals("SamePrimitive[c={true,{true}}]", logAdapter.asString(new SamePrimitive(), null, null));
    }

    @Test
    public void testAsStringCycleIdentityEquals() throws Exception {
        class IdentityEquals {
            private IdentityEquals c;

            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof IdentityEquals;
            }

        }
        IdentityEquals a = new IdentityEquals();
        a.c = new IdentityEquals() {
            @Override
            public String toString() {
                return "extended-IdentityEquals";
            }
        };
        // memo: general equals in ToString.valuesInProgress leads to detect [a.c] as repeated [a] and incorrect out IdentityEquals[c=@126b249]
        assertEquals("IdentityEquals[c=extended-IdentityEquals]", logAdapter.asString(a, null, null));
    }

    @Test
    public void testAsStringGeneric() throws Exception {
        class Generic<T> {
            private T thing;

            private Generic(T thing) {
                this.thing = thing;
            }
        }
        assertEquals("Generic[thing=str]", logAdapter.asString(new Generic<String>("str"), null, null));
    }

    @Test
    public void testAsStringToString() throws Exception {
        class Foo {
            @Override
            public String toString() {
                return "abracadabra";
            }
        }
        assertEquals("abracadabra", logAdapter.asString(new Foo(), null, null));

        class Bar {
            private Foo name = new Foo();
        }
        assertEquals("Bar[name=abracadabra]", logAdapter.asString(new Bar(), null, null));
    }

    @Test
    public void testAsStringArrayCropping() throws Exception {
        int[][] array = {{1, 2, 3}, {4, 5, 6}};
        assertEquals("int[][][{{1,2,3},{4,5,6}}]", logAdapter.asString(array, null, null));

        logAdapter = new UniversalLogAdapter(false, 4, null, false);
        assertEquals("int[][][{{1,2,3},{4,5,6}}]", logAdapter.asString(array, null, null));

        logAdapter = new UniversalLogAdapter(false, 3, null, false);
        assertEquals("int[][][{{1,2,3},{4,5,6}}]", logAdapter.asString(array, null, null));

        logAdapter = new UniversalLogAdapter(false, 2, null, false);
        assertEquals("int[][][{{1,2,..<size=3>..},{4,5,..<size=3>..}}]", logAdapter.asString(array, null, null));

        logAdapter = new UniversalLogAdapter(false, 1, null, false);
        assertEquals("int[][][{{1,..<size=3>..},..<size=2>..}]", logAdapter.asString(array, null, null));

        logAdapter = new UniversalLogAdapter(false, 0, null, false);
        assertEquals("int[][][{..<size=2>..}]", logAdapter.asString(array, null, null));
    }

    @Test
    public void testAsStringCollectionCropping() throws Exception {
        List<Integer> listA = new ArrayList<Integer>(Arrays.asList(1, 2, 3));
        List<Integer> listB = new ArrayList<Integer>(Arrays.asList(4, 5, 6));
        List<List<Integer>> collection = new ArrayList<List<Integer>>();
        collection.add(listA);
        collection.add(listB);
        assertEquals("ArrayList[{{1,2,3},{4,5,6}}]", logAdapter.asString(collection, null, null));

        logAdapter = new UniversalLogAdapter(false, 4, null, false);
        assertEquals("ArrayList[{{1,2,3},{4,5,6}}]", logAdapter.asString(collection, null, null));

        logAdapter = new UniversalLogAdapter(false, 3, null, false);
        assertEquals("ArrayList[{{1,2,3},{4,5,6}}]", logAdapter.asString(collection, null, null));

        logAdapter = new UniversalLogAdapter(false, 2, null, false);
        assertEquals("ArrayList[{{1,2,..<size=3>..},{4,5,..<size=3>..}}]", logAdapter.asString(collection, null, null));

        logAdapter = new UniversalLogAdapter(false, 1, null, false);
        assertEquals("ArrayList[{{1,..<size=3>..},..<size=2>..}]", logAdapter.asString(collection, null, null));

        logAdapter = new UniversalLogAdapter(false, 0, null, false);
        assertEquals("ArrayList[{..<size=2>..}]", logAdapter.asString(collection, null, null));
    }

}
