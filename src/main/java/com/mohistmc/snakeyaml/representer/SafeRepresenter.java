/**
 * Copyright (c) 2008, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.mohistmc.snakeyaml.representer;

import com.mohistmc.snakeyaml.DumperOptions;
import com.mohistmc.snakeyaml.error.YAMLException;
import com.mohistmc.snakeyaml.external.biz.base64Coder.Base64Coder;
import com.mohistmc.snakeyaml.nodes.Node;
import com.mohistmc.snakeyaml.nodes.Tag;
import com.mohistmc.snakeyaml.reader.StreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Represent standard Java classes
 */
class SafeRepresenter extends BaseRepresenter {

  protected Map<Class<? extends Object>, Tag> classTags;
  protected TimeZone timeZone = null;
  protected DumperOptions.NonPrintableStyle nonPrintableStyle;

  public SafeRepresenter(DumperOptions options) {
    if (options == null) {
      throw new NullPointerException("DumperOptions must be provided.");
    }
    this.nullRepresenter = new RepresentNull();
    this.representers.put(String.class, new RepresentString());
    this.representers.put(Boolean.class, new RepresentBoolean());
    this.representers.put(Character.class, new RepresentString());
    this.representers.put(UUID.class, new RepresentUuid());
    this.representers.put(byte[].class, new RepresentByteArray());

    com.mohistmc.snakeyaml.representer.Represent primitiveArray = new RepresentPrimitiveArray();
    representers.put(short[].class, primitiveArray);
    representers.put(int[].class, primitiveArray);
    representers.put(long[].class, primitiveArray);
    representers.put(float[].class, primitiveArray);
    representers.put(double[].class, primitiveArray);
    representers.put(char[].class, primitiveArray);
    representers.put(boolean[].class, primitiveArray);

    this.multiRepresenters.put(Number.class, new RepresentNumber());
    this.multiRepresenters.put(List.class, new RepresentList());
    this.multiRepresenters.put(Map.class, new RepresentMap());
    this.multiRepresenters.put(Set.class, new RepresentSet());
    this.multiRepresenters.put(Iterator.class, new RepresentIterator());
    this.multiRepresenters.put(new Object[0].getClass(), new RepresentArray());
    this.multiRepresenters.put(Date.class, new RepresentDate());
    this.multiRepresenters.put(Enum.class, new RepresentEnum());
    this.multiRepresenters.put(Calendar.class, new RepresentDate());
    classTags = new HashMap<>();
    this.nonPrintableStyle = options.getNonPrintableStyle();
    setDefaultScalarStyle(options.getDefaultScalarStyle());
    setDefaultFlowStyle(options.getDefaultFlowStyle());
  }

  protected Tag getTag(Class<?> clazz, Tag defaultTag) {
      return classTags.getOrDefault(clazz, defaultTag);
  }

  /**
   * Define a tag for the <code>Class</code> to serialize.
   *
   * @param clazz <code>Class</code> which tag is changed
   * @param tag new tag to be used for every instance of the specified <code>Class</code>
   * @return the previous tag associated with the <code>Class</code>
   */
  public Tag addClassTag(Class<? extends Object> clazz, Tag tag) {
    if (tag == null) {
      throw new NullPointerException("Tag must be provided.");
    }
    return classTags.put(clazz, tag);
  }

  protected class RepresentNull implements com.mohistmc.snakeyaml.representer.Represent {

    public Node representData(Object data) {
      return representScalar(Tag.NULL, "null");
    }
  }

  private static final Pattern MULTILINE_PATTERN = Pattern.compile("\n|\u0085|\u2028|\u2029");

  protected class RepresentString implements com.mohistmc.snakeyaml.representer.Represent {

    public Node representData(Object data) {
      Tag tag = Tag.STR;
      DumperOptions.ScalarStyle style = defaultScalarStyle;
      String value = data.toString();
      if (nonPrintableStyle == DumperOptions.NonPrintableStyle.BINARY
          && !StreamReader.isPrintable(value)) {
        tag = Tag.BINARY;
        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        // sometimes above will just silently fail - it will return incomplete data
        // it happens when String has invalid code points
        // (for example half surrogate character without other half)
        final String checkValue = new String(bytes, StandardCharsets.UTF_8);
        if (!checkValue.equals(value)) {
          throw new YAMLException("invalid string value has occurred");
        }
        char[] binary = Base64Coder.encode(bytes);
        value = String.valueOf(binary);
        style = DumperOptions.ScalarStyle.LITERAL;
      }
      // if no other scalar style is explicitly set, use literal style for
      // multiline scalars
      if (defaultScalarStyle == DumperOptions.ScalarStyle.PLAIN
          && MULTILINE_PATTERN.matcher(value).find()) {
        style = DumperOptions.ScalarStyle.LITERAL;
      }
      return representScalar(tag, value, style);
    }
  }

  protected class RepresentBoolean implements com.mohistmc.snakeyaml.representer.Represent {

    public Node representData(Object data) {
      String value;
      if (Boolean.TRUE.equals(data)) {
        value = "true";
      } else {
        value = "false";
      }
      return representScalar(Tag.BOOL, value);
    }
  }

  protected class RepresentNumber implements com.mohistmc.snakeyaml.representer.Represent {

    public Node representData(Object data) {
      Tag tag;
      String value;
      if (data instanceof Byte || data instanceof Short || data instanceof Integer
          || data instanceof Long || data instanceof BigInteger) {
        tag = Tag.INT;
        value = data.toString();
      } else {
        Number number = (Number) data;
        tag = Tag.FLOAT;
        if (number.equals(Double.NaN)) {
          value = ".NaN";
        } else if (number.equals(Double.POSITIVE_INFINITY)) {
          value = ".inf";
        } else if (number.equals(Double.NEGATIVE_INFINITY)) {
          value = "-.inf";
        } else {
          value = number.toString();
        }
      }
      return representScalar(getTag(data.getClass(), tag), value);
    }
  }

  protected class RepresentList implements com.mohistmc.snakeyaml.representer.Represent {

    @SuppressWarnings("unchecked")
    public Node representData(Object data) {
      return representSequence(getTag(data.getClass(), Tag.SEQ), (List<Object>) data,
          DumperOptions.FlowStyle.AUTO);
    }
  }

  protected class RepresentIterator implements com.mohistmc.snakeyaml.representer.Represent {

    @SuppressWarnings("unchecked")
    public Node representData(Object data) {
      Iterator<Object> iter = (Iterator<Object>) data;
      return representSequence(getTag(data.getClass(), Tag.SEQ), new IteratorWrapper(iter),
          DumperOptions.FlowStyle.AUTO);
    }
  }

  private record IteratorWrapper(Iterator<Object> iter) implements Iterable<Object> {

    public Iterator<Object> iterator() {
        return iter;
      }
    }

  protected class RepresentArray implements com.mohistmc.snakeyaml.representer.Represent {

    public Node representData(Object data) {
      Object[] array = (Object[]) data;
      List<Object> list = Arrays.asList(array);
      return representSequence(Tag.SEQ, list, DumperOptions.FlowStyle.AUTO);
    }
  }

  /**
   * Represents primitive arrays, such as short[] and float[], by converting them into equivalent
   * List<Short> and List<Float> using the appropriate autoboxing type.
   */
  protected class RepresentPrimitiveArray implements com.mohistmc.snakeyaml.representer.Represent {

    public Node representData(Object data) {
      Class<?> type = data.getClass().getComponentType();

      if (byte.class == type) {
        return representSequence(Tag.SEQ, asByteList(data), DumperOptions.FlowStyle.AUTO);
      } else if (short.class == type) {
        return representSequence(Tag.SEQ, asShortList(data), DumperOptions.FlowStyle.AUTO);
      } else if (int.class == type) {
        return representSequence(Tag.SEQ, asIntList(data), DumperOptions.FlowStyle.AUTO);
      } else if (long.class == type) {
        return representSequence(Tag.SEQ, asLongList(data), DumperOptions.FlowStyle.AUTO);
      } else if (float.class == type) {
        return representSequence(Tag.SEQ, asFloatList(data), DumperOptions.FlowStyle.AUTO);
      } else if (double.class == type) {
        return representSequence(Tag.SEQ, asDoubleList(data), DumperOptions.FlowStyle.AUTO);
      } else if (char.class == type) {
        return representSequence(Tag.SEQ, asCharList(data), DumperOptions.FlowStyle.AUTO);
      } else if (boolean.class == type) {
        return representSequence(Tag.SEQ, asBooleanList(data), DumperOptions.FlowStyle.AUTO);
      }

      throw new YAMLException("Unexpected primitive '" + type.getCanonicalName() + "'");
    }

    private List<Byte> asByteList(Object in) {
      byte[] array = (byte[]) in;
      List<Byte> list = new ArrayList<>(array.length);
        for (byte b : array) {
            list.add(b);
        }
      return list;
    }

    private List<Short> asShortList(Object in) {
      short[] array = (short[]) in;
      List<Short> list = new ArrayList<>(array.length);
        for (short value : array) {
            list.add(value);
        }
      return list;
    }

    private List<Integer> asIntList(Object in) {
      int[] array = (int[]) in;
      List<Integer> list = new ArrayList<>(array.length);
        for (int j : array) {
            list.add(j);
        }
      return list;
    }

    private List<Long> asLongList(Object in) {
      long[] array = (long[]) in;
      List<Long> list = new ArrayList<>(array.length);
        for (long l : array) {
            list.add(l);
        }
      return list;
    }

    private List<Float> asFloatList(Object in) {
      float[] array = (float[]) in;
      List<Float> list = new ArrayList<>(array.length);
        for (float v : array) {
            list.add(v);
        }
      return list;
    }

    private List<Double> asDoubleList(Object in) {
      double[] array = (double[]) in;
      List<Double> list = new ArrayList<>(array.length);
        for (double v : array) {
            list.add(v);
        }
      return list;
    }

    private List<Character> asCharList(Object in) {
      char[] array = (char[]) in;
      List<Character> list = new ArrayList<>(array.length);
        for (char c : array) {
            list.add(c);
        }
      return list;
    }

    private List<Boolean> asBooleanList(Object in) {
      boolean[] array = (boolean[]) in;
      List<Boolean> list = new ArrayList<>(array.length);
        for (boolean b : array) {
            list.add(b);
        }
      return list;
    }
  }

  protected class RepresentMap implements com.mohistmc.snakeyaml.representer.Represent {

    @SuppressWarnings("unchecked")
    public Node representData(Object data) {
      return representMapping(getTag(data.getClass(), Tag.MAP), (Map<Object, Object>) data,
          DumperOptions.FlowStyle.AUTO);
    }
  }

  protected class RepresentSet implements com.mohistmc.snakeyaml.representer.Represent {

    @SuppressWarnings("unchecked")
    public Node representData(Object data) {
      Map<Object, Object> value = new LinkedHashMap<>();
      Set<Object> set = (Set<Object>) data;
      for (Object key : set) {
        value.put(key, null);
      }
      return representMapping(getTag(data.getClass(), Tag.SET), value,
          DumperOptions.FlowStyle.AUTO);
    }
  }

  protected class RepresentDate implements com.mohistmc.snakeyaml.representer.Represent {

    public Node representData(Object data) {
      // because SimpleDateFormat ignores timezone we have to use Calendar
      Calendar calendar;
      if (data instanceof Calendar) {
        calendar = (Calendar) data;
      } else {
        calendar =
            Calendar.getInstance(getTimeZone() == null ? TimeZone.getTimeZone("UTC") : timeZone);
        calendar.setTime((Date) data);
      }
      int years = calendar.get(Calendar.YEAR);
      int months = calendar.get(Calendar.MONTH) + 1; // 0..12
      int days = calendar.get(Calendar.DAY_OF_MONTH); // 1..31
      int hour24 = calendar.get(Calendar.HOUR_OF_DAY); // 0..24
      int minutes = calendar.get(Calendar.MINUTE); // 0..59
      int seconds = calendar.get(Calendar.SECOND); // 0..59
      int millis = calendar.get(Calendar.MILLISECOND);
      StringBuilder buffer = new StringBuilder(String.valueOf(years));
      while (buffer.length() < 4) {
        // ancient years
        buffer.insert(0, "0");
      }
      buffer.append("-");
      if (months < 10) {
        buffer.append("0");
      }
      buffer.append(months);
      buffer.append("-");
      if (days < 10) {
        buffer.append("0");
      }
      buffer.append(days);
      buffer.append("T");
      if (hour24 < 10) {
        buffer.append("0");
      }
      buffer.append(hour24);
      buffer.append(":");
      if (minutes < 10) {
        buffer.append("0");
      }
      buffer.append(minutes);
      buffer.append(":");
      if (seconds < 10) {
        buffer.append("0");
      }
      buffer.append(seconds);
      if (millis > 0) {
        if (millis < 10) {
          buffer.append(".00");
        } else if (millis < 100) {
          buffer.append(".0");
        } else {
          buffer.append(".");
        }
        buffer.append(millis);
      }

      // Get the offset from GMT taking DST into account
      int gmtOffset = calendar.getTimeZone().getOffset(calendar.getTime().getTime());
      if (gmtOffset == 0) {
        buffer.append('Z');
      } else {
        if (gmtOffset < 0) {
          buffer.append('-');
          gmtOffset *= -1;
        } else {
          buffer.append('+');
        }
        int minutesOffset = gmtOffset / (60 * 1000);
        int hoursOffset = minutesOffset / 60;
        int partOfHour = minutesOffset % 60;

        if (hoursOffset < 10) {
          buffer.append('0');
        }
        buffer.append(hoursOffset);
        buffer.append(':');
        if (partOfHour < 10) {
          buffer.append('0');
        }
        buffer.append(partOfHour);
      }

      return representScalar(getTag(data.getClass(), Tag.TIMESTAMP), buffer.toString(),
          DumperOptions.ScalarStyle.PLAIN);
    }
  }

  protected class RepresentEnum implements com.mohistmc.snakeyaml.representer.Represent {

    public Node representData(Object data) {
      Tag tag = new Tag(data.getClass());
      return representScalar(getTag(data.getClass(), tag), ((Enum<?>) data).name());
    }
  }

  protected class RepresentByteArray implements com.mohistmc.snakeyaml.representer.Represent {

    public Node representData(Object data) {
      char[] binary = Base64Coder.encode((byte[]) data);
      return representScalar(Tag.BINARY, String.valueOf(binary), DumperOptions.ScalarStyle.LITERAL);
    }
  }

  public TimeZone getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(TimeZone timeZone) {
    this.timeZone = timeZone;
  }

  protected class RepresentUuid implements Represent {

    public Node representData(Object data) {
      return representScalar(getTag(data.getClass(), new Tag(UUID.class)), data.toString());
    }
  }
}
