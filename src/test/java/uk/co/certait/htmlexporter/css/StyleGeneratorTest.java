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

import org.junit.Assert;
import org.junit.Test;

import java.awt.Color;

public class StyleGeneratorTest {
  @Test
  public void testWebColor() {
    Assert.assertEquals(StyleGenerator.webColor("#0000ff"), Color.BLUE);
    Assert.assertEquals(StyleGenerator.webColor("blue"), Color.BLUE);
    Assert.assertEquals(StyleGenerator.webColor("Blue"), Color.BLUE);
    Assert.assertEquals(StyleGenerator.webColor("rgb(166, 229, 168)"), new Color(166, 229, 168));
  }

}
