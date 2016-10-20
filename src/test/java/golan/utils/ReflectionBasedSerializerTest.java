package golan.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

public class ReflectionBasedSerializerTest {

  @Test
  public void testToXmlString() throws Exception {

    System.out.println("<testToXmlString>");

    //System.out.println("<Test id=\"types\" description=\"test different types of objects and arrays\">");
    //C c = new C(new D(new Integer[]{4,5,6}), "izik", new int[]{1,2,3});
    //System.out.println(ReflectionBasedSerializer.toXmlString(c, -1, true));
    //System.out.println("</Test>");
    //
    //System.out.println("<Test id=\"duplications\" description=\"test arrays of objects that each one has a pointer to the parent that holds them all \">");
    //Singles singles = new Singles(new int[]{7,8,9});
    //System.out.println(ReflectionBasedSerializer.toXmlString(singles, -1, true));
    //System.out.println("</Test>");
    //
    //System.out.println("<Test id=primitive wrapper array\" description=\"check that we print well array of primitive wrappers \">");
    //final Integer[] items = { 1, 2, 3 };
    //System.out.println(ReflectionBasedSerializer.toXmlString(items, -1, true));
    //System.out.println("</Test>");

    System.out.println("<Test id=primitive wrapper array\" description=\"check that we print well array of primitive wrappers \">");
    final A[] aas = new A[6];
    aas[0] = new A("1_1");
    aas[1] = new A("1_2");
    aas[2] = new A("1_3");
    aas[3] = aas[0];
    aas[4] = aas[1];

    System.out.println(ReflectionBasedSerializer.toXmlString(aas, -1, true));
    System.out.println("</Test>");

    System.out.println("</testToXmlString>");

  }

  private static class A {
    final String str;

    private A(String str_) {this.str = str_;}
  }

  private static class B extends A {
    final int[] arr;

    private B(String str_, int[] arr_) {
      super(str_);
      this.arr = arr_;
    }
  }

  private class C extends B {
    final D d;

    private C(D d, String str_, int[] arr_) {
      super(str_, arr_);
      this.d = d;
    }
  }

  private static class D {
    final ArrayList<Integer> arrayList = new ArrayList<>();

    private D(Integer[] arr_) {
      Collections.addAll(arrayList, arr_);
    }
  }

  private static class Single {
    final int val;
    final Singles parent;

    private Single(int val, Singles parent) {
      this.val = val;
      this.parent = parent;
    }
  }

  private static class Singles {
    final Single[] ees;

    private Singles(int[] arr) {
      ees = new Single[arr.length];
      for (int i = 0; i < arr.length; i++) {
        ees[i] = new Single(arr[i], this);
      }
    }
  }
}