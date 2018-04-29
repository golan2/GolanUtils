package golan.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <pre>
 * <B>Copyright:</B>   Izik Golan
 * <B>Owner:</B>       <a href="mailto:golan2@hotmail.com">Izik Golan</a>
 * <B>Creation:</B>    13/03/2015 11:33
 * <B>Since:</B>       BSM 9.21
 * <B>Description:</B>
 *
 * </pre>
 */
public class ReflectionBasedSerializer {
  private static final MyLog log = new MyLog();

  public static String toXmlStringIgnoreEx(Object o) throws RuntimeException {
    try {
      return toXmlString(o, 0, false);
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException("Failed to serialize object: " + o.toString() , e);
    }
  }

  public static String toXmlString(Object o) throws IllegalAccessException {
    return toXmlString(o, 0, false);
  }

  public static String toXmlString(Object o, int depth, boolean includingStaticFields) throws IllegalAccessException {
    StringBuilder sb = new StringBuilder();
    try {
      toXmlString(o, sb, depth, includingStaticFields, new HashSet<Integer>());
    }
    catch (IllegalAccessException e) {
      throw e;
    }
    catch (Throwable t) {
      log.error("Failed to serialize. Partial: " + sb.toString(), t);
    }
    return sb.toString();
  }

  private static String valueToString(Object obj) {
    final String strValue = String.valueOf(obj);
    boolean containsInvalidXmlCharacters = (strValue.contains("<")) || (strValue.contains(">")) || (strValue.contains("\"")) || (strValue.contains("\'")) || (strValue.contains("&"));
    if (containsInvalidXmlCharacters) {
      return "<![CDATA[" + strValue.trim().replaceAll(">\\s*<", "><") + "]]>";
    }
    else {
      return strValue;
    }
  }


  private static void toXmlString(Object o, StringBuilder sb, int depth, boolean includingStaticFields, Set<Integer> dejaVu) throws IllegalAccessException {
    if (o==null) {
      sb.append("null");
      return;
    }

    if (isPrimitive(o)) {
      sb.append(valueToString(o));
      return;
    }

    if (depth==0) {
      sb.append(valueToString(o));
      return;
    }

    final int globalHashCode = System.identityHashCode(o);

    if (dejaVu!=null && dejaVu.contains(globalHashCode)) {
      sb.append("<Object dejaVu=\"true\"");
      sb.append(" globalHashCode=\"").append(globalHashCode).append("\" ");
      sb.append("/>");
      return;
    }
    else {
      dejaVu.add(globalHashCode);
    }

    if (o.getClass().isArray()) {
      arrayToXmlString(o, sb, depth, includingStaticFields, dejaVu);
      return;
    }

    Class clazz = o.getClass();
    Field f[] = getFieldsFromClass(clazz, includingStaticFields);

    //sb.append("<Object depth=\"").append(depth).append("\" type=\"").append(clazz.getSimpleName()).append("\" parents=\"").append(calculateParents(clazz)).append("\">");
    sb.append("<Object");
    sb.append(" type=\"").append(valueToString(clazz.getSimpleName())).append("\" ");
    sb.append(" depth=\"").append(depth).append("\" ");
    sb.append(" globalHashCode=\"").append(globalHashCode).append("\" ");
    if (includingStaticFields) {
      //sb.append("type=\"");
      //sb.append(clazz.getSimpleName());
      //sb.append("\" ");
      sb.append("parents=\"");
      sb.append(valueToString(calculateParents(clazz)));
      sb.append("\"");
    }
    sb.append(">");
    sb.append("<Fields>");
    for (Field aF : f) {
      if (aF!=null) {
        aF.setAccessible(true);
        final String fieldName = aF.getName();
        final Object fieldValue = aF.get(o);
        if (!"serialVersionUID".equals(fieldName) && fieldValue!=o) {
          sb.append("<Field>");
          sb.append("<Name>").append(fieldName).append("</Name>");
          if (fieldValue !=null && depth!=0) {
            sb.append("<Value>");
            toXmlString(fieldValue, sb, depth-1, includingStaticFields, dejaVu);
            sb.append("</Value>");
          }
          else {
            if (isPrimitive(o)) {
              sb.append(valueToString(o));
            }
            else {
              //sb.append("<Value><![CDATA[").append(fieldValue).append("]]></Value>");
              sb.append("<Value>").append(fieldValue).append("</Value>");
            }
          }

          sb.append("</Field>");
        }
      }
    }
    sb.append("</Fields>");

    //sb.append("</Object>");
    sb.append("</Object>");
  }

  private static void arrayToXmlString(Object element, StringBuilder sb, int depth, boolean includingStaticFields, Set<Integer> dejaVu) throws IllegalAccessException {
    Class eClass = element.getClass();


    final String arrValues;
    final int length;
    if (eClass == byte[].class) {
      length = ((byte[]) element).length;
      arrValues = Arrays.toString((byte[]) element);
    }
    else if (eClass == short[].class) {
      length = ((short[]) element).length;
      arrValues = Arrays.toString((short[]) element);
    }
    else if (eClass == int[].class) {
      length = ((int[]) element).length;
      arrValues = Arrays.toString((int[]) element);
    }
    else if (eClass == long[].class) {
      length = ((long[]) element).length;
      arrValues = Arrays.toString((long[]) element);
    }
    else if (eClass == char[].class) {
      length = ((char[]) element).length;
      arrValues = Arrays.toString((char[]) element);
    }
    else if (eClass == float[].class) {
      length = ((float[]) element).length;
      arrValues = Arrays.toString((float[]) element);
    }
    else if (eClass == double[].class) {
      length = ((double[]) element).length;
      arrValues = Arrays.toString((double[]) element);
    }
    else if (eClass == boolean[].class) {
      length = ((boolean[]) element).length;
      arrValues = Arrays.toString((boolean[]) element);
    }
    else if (element instanceof Object[]) {
      Object[] array = (Object[]) element;
      sb.append("<Array size=\"").append(array.length).append("\">");
      for (Object item : array) {
        sb.append("<Item>");
        toXmlString(item, sb, depth-1, includingStaticFields, dejaVu);
        sb.append("</Item>");
      }
      sb.append("</Array>");
      return;
    }
    else { // element is an array of object references
      sb.append("<Unknown class=\""+element.getClass().getSimpleName()+"\" toString=\""+valueToString(element)+"\"/>");
      return;
    }

    sb.append("<Array size=\"").append(length).append("\">").append(valueToString(arrValues)).append("</Array>");
  }

  private static String escapeXml(Class clazz) {
    return clazz.getSimpleName();
  }

  private static Field[] getFieldsFromClass(Class clazz, boolean includingStaticFields) {
    if (clazz==null) return new Field[0];

    final Field[] fieldsFromParent=getFieldsFromClass(clazz.getSuperclass(), includingStaticFields);

    final Field[] fieldsFromThis=clazz.getDeclaredFields();
    Field[] res = new Field[fieldsFromParent.length+fieldsFromThis.length];
    int index = 0;
    for (Field field : fieldsFromParent) {
      res[index++] = field;
    }

    for (Field field : fieldsFromThis) {
      if (includingStaticFields || !Modifier.isStatic(field.getModifiers())) {
        res[index++] = field;
      }
    }
    return res;
  }

  private static String calculateParents(Class clazz) {
    final StringBuilder result = new StringBuilder();
    addParentsToStringBuilder(result, clazz);
    return result.toString();
  }


  private static void addParentsToStringBuilder(StringBuilder sb, Class clazz) {
    if (clazz==null) return;
    final Class parent=clazz.getSuperclass();
    if (parent==null) return;
    sb.append(parent.getSimpleName()).append(",");
    addParentsToStringBuilder(sb, parent);

  }

  private static boolean isPrimitive(Object o) {
    return
        o instanceof Boolean |
            o instanceof Integer |
            o instanceof Long |
            o instanceof Float |
            o instanceof Double |
            o instanceof String
        ;
  }
}
