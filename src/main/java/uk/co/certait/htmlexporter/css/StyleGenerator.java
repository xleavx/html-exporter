/**
 * Copyright (C) 2012 alanhay <alanhay99@hotmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.certait.htmlexporter.css;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.osbcp.cssparser.PropertyValue;
import com.osbcp.cssparser.Rule;
import com.osbcp.cssparser.Selector;

public class StyleGenerator {
	private static final String PX = "px";
	private static final String PERCENTAGE = "%";

	protected Style createStyle(Rule rule, Selector selector) {
		Style style = new Style();

		populateIntegerProperties(rule, selector, style);
		populateStringProperties(rule, selector, style);
		populateColorProperties(rule, selector, style);

		return style;
	}

	protected void populateIntegerProperties(Rule rule, Selector selector, Style style) {
		for (PropertyValue pv : rule.getPropertyValues()) {
			for (CssIntegerProperty p : CssIntegerProperty.values()) {
				if (p.getProperty().equals(pv.getProperty())) {
					if (!pv.getValue().contains(PERCENTAGE)) {
						double value = Double.parseDouble(pv.getValue().replaceAll(PX, "").trim());

						if (value < 1) {
							value = 1;
						}

						style.addProperty(p, (int) value);
					}
				}
			}
		}
	}

	protected void populateStringProperties(Rule rule, Selector selector, Style style) {
		for (PropertyValue pv : rule.getPropertyValues()) {
			for (CssStringProperty p : CssStringProperty.values()) {
				if (p.getProperty().equals(pv.getProperty())) {
					style.addProperty(p, pv.getValue().trim());
				}
			}
		}
	}

	protected void populateColorProperties(Rule rule, Selector selector, Style style) {
		for (PropertyValue pv : rule.getPropertyValues()) {
			for (CssColorProperty p : CssColorProperty.values()) {
				if (p.getProperty().equals(pv.getProperty())) {
					style.addProperty(p, createColor(pv.getValue().trim()));
				}
			}
		}
	}

	private Color createColor(String hex) {
		//hex = hex.toUpperCase();
		//return Color.decode(hex);
          return webColor(hex); // improved
	}

  public static Color webColor(String colorString) {
    return webColor(colorString, 1.0);
  }

  public static Color webColor(String colorString, double opacity) {
    if (colorString == null) {
      throw new NullPointerException(
        "The color components or name must be specified");
    }
    if (colorString.isEmpty()) {
      throw new IllegalArgumentException("Invalid color specification");
    }

    String color = colorString.toLowerCase(Locale.ROOT);

    if (color.startsWith("#")) {
      color = color.substring(1);
    } else if (color.startsWith("0x")) {
      color = color.substring(2);
    } else if (color.startsWith("rgb")) {
      if (color.startsWith("(", 3)) {
        return parseRGBColor(color, 4, false, opacity);
      } else if (color.startsWith("a(", 3)) {
        return parseRGBColor(color, 5, true, opacity);
      }
    } else if (color.startsWith("hsl")) {
      if (color.startsWith("(", 3)) {
        return parseHSLColor(color, 4, false, opacity);
      } else if (color.startsWith("a(", 3)) {
        return parseHSLColor(color, 5, true, opacity);
      }
    } else {
      Color col = namedColors.get(color);//todo check named colors from java.awt.Color  //NamedColors.get(color);
      if (col != null) {
        if (opacity == 1.0) {
          return col;
        } else {
          return new Color(col.getRed(), col.getGreen(), col.getBlue(), (float) opacity);
        }
      }
    }

    int len = color.length();

    try {
      int r;
      int g;
      int b;
      int a;

      if (len == 3) {
        r = Integer.parseInt(color.substring(0, 1), 16);
        g = Integer.parseInt(color.substring(1, 2), 16);
        b = Integer.parseInt(color.substring(2, 3), 16);
        return new Color((float)(r / 15.0), (float)(g / 15.0), (float)(b / 15.0), (float) opacity);
      } else if (len == 4) {
        r = Integer.parseInt(color.substring(0, 1), 16);
        g = Integer.parseInt(color.substring(1, 2), 16);
        b = Integer.parseInt(color.substring(2, 3), 16);
        a = Integer.parseInt(color.substring(3, 4), 16);
        return new Color((float)(r / 15.0), (float)(g / 15.0), (float)(b / 15.0), (float) (opacity * a / 15.0));
      } else if (len == 6) {
        r = Integer.parseInt(color.substring(0, 2), 16);
        g = Integer.parseInt(color.substring(2, 4), 16);
        b = Integer.parseInt(color.substring(4, 6), 16);
        return new Color(r, g, b, (int)(opacity*255+0.5));
      } else if (len == 8) {
        r = Integer.parseInt(color.substring(0, 2), 16);
        g = Integer.parseInt(color.substring(2, 4), 16);
        b = Integer.parseInt(color.substring(4, 6), 16);
        a = Integer.parseInt(color.substring(6, 8), 16);
        return new Color(r, g, b, (float)(opacity * a / 255.0));
      }
    } catch (NumberFormatException nfe) {}

    throw new IllegalArgumentException("Invalid color specification");
  }

  private static Color parseRGBColor(String color, int roff, boolean hasAlpha, double a) {
    try {
      int rend = color.indexOf(',', roff);
      int gend = rend < 0 ? -1 : color.indexOf(',', rend+1);
      int bend = gend < 0 ? -1 : color.indexOf(hasAlpha ? ',' : ')', gend+1);
      int aend = hasAlpha ? (bend < 0 ? -1 : color.indexOf(')', bend+1)) : bend;
      if (aend >= 0) {
        double r = parseComponent(color, roff, rend, PARSE_COMPONENT);
        double g = parseComponent(color, rend+1, gend, PARSE_COMPONENT);
        double b = parseComponent(color, gend+1, bend, PARSE_COMPONENT);
        if (hasAlpha) {
          a *= parseComponent(color, bend+1, aend, PARSE_ALPHA);
        }
        return new Color((float)r, (float)g, (float)b, (float)a);
      }
    } catch (NumberFormatException nfe) {}

    throw new IllegalArgumentException("Invalid color specification");
  }

  private static Color parseHSLColor(String color, int hoff,
                                     boolean hasAlpha, double a)
  {
    try {
      int hend = color.indexOf(',', hoff);
      int send = hend < 0 ? -1 : color.indexOf(',', hend+1);
      int lend = send < 0 ? -1 : color.indexOf(hasAlpha ? ',' : ')', send+1);
      int aend = hasAlpha ? (lend < 0 ? -1 : color.indexOf(')', lend+1)) : lend;
      if (aend >= 0) {
        double h = parseComponent(color, hoff, hend, PARSE_ANGLE);
        double s = parseComponent(color, hend+1, send, PARSE_PERCENT);
        double l = parseComponent(color, send+1, lend, PARSE_PERCENT);
        if (hasAlpha) {
          a *= parseComponent(color, lend+1, aend, PARSE_ALPHA);
        }
        return Color.getHSBColor((float)h, (float)s, (float) l); // a
        //return Color.hsb(h, s, l, a);
      }
    } catch (NumberFormatException nfe) {}

    throw new IllegalArgumentException("Invalid color specification");
  }

  private static final int PARSE_COMPONENT = 0; // percent, or clamped to [0,255] => [0,1]
  private static final int PARSE_PERCENT = 1; // clamped to [0,100]% => [0,1]
  private static final int PARSE_ANGLE = 2; // clamped to [0,360]
  private static final int PARSE_ALPHA = 3; // clamped to [0.0,1.0]
  private static double parseComponent(String color, int off, int end, int type) {
    color = color.substring(off, end).trim();
    if (color.endsWith("%")) {
      if (type > PARSE_PERCENT) {
        throw new IllegalArgumentException("Invalid color specification");
      }
      type = PARSE_PERCENT;
      color = color.substring(0, color.length()-1).trim();
    } else if (type == PARSE_PERCENT) {
      throw new IllegalArgumentException("Invalid color specification");
    }
    double c = ((type == PARSE_COMPONENT)
      ? Integer.parseInt(color)
      : Double.parseDouble(color));
    switch (type) {
      case PARSE_ALPHA:
        return (c < 0.0) ? 0.0 : ((c > 1.0) ? 1.0 : c);
      case PARSE_PERCENT:
        return (c <= 0.0) ? 0.0 : ((c >= 100.0) ? 1.0 : (c / 100.0));
      case PARSE_COMPONENT:
        return (c <= 0.0) ? 0.0 : ((c >= 255.0) ? 1.0 : (c / 255.0));
      case PARSE_ANGLE:
        return ((c < 0.0)
          ? ((c % 360.0) + 360.0)
          : ((c > 360.0)
          ? (c % 360.0)
          : c));
    }

    throw new IllegalArgumentException("Invalid color specification");
  }

  private static final Map<String, Color> namedColors = createNamedColors();
  private static Map<String, Color> createNamedColors() {
    Map<String, Color> colors = new HashMap<String,Color>(16);
    colors.put("black",                Color.BLACK);
    colors.put("blue",                 Color.BLUE);
    colors.put("cyan",                 Color.CYAN);
    colors.put("gray",                 Color.GRAY);
    colors.put("green",                Color.GREEN);
    colors.put("magenta",              Color.MAGENTA);
    colors.put("orange",               Color.ORANGE);
    colors.put("pink",                 Color.PINK);
    colors.put("red",                  Color.RED);
    colors.put("white",                Color.WHITE);
    colors.put("yellow",               Color.YELLOW);
    return colors;
  }

}
