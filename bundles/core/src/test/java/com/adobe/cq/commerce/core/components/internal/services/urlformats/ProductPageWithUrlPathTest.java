/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2021 Adobe
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
package com.adobe.cq.commerce.core.components.internal.services.urlformats;

import java.util.Arrays;

import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.junit.Test;

import com.adobe.cq.commerce.core.components.services.urls.ProductUrlFormat;
import com.adobe.cq.commerce.magento.graphql.UrlRewrite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ProductPageWithUrlPathTest {

    public final ProductUrlFormat subject = ProductPageWithUrlPath.INSTANCE;

    @Test
    public void testFormatWithMissingParameters() {
        ProductUrlFormat.Params params = new ProductUrlFormat.Params();

        assertEquals("{{page}}.html/{{url_path}}.html", subject.format(params));
    }

    @Test
    public void testFormat() {
        ProductUrlFormat.Params params = new ProductUrlFormat.Params();
        params.setPage("/page/path");
        params.setUrlPath("foo-bar");

        assertEquals("/page/path.html/foo-bar.html", subject.format(params));

        params.setVariantSku("variant");

        assertEquals("/page/path.html/foo-bar.html#variant", subject.format(params));
    }

    @Test
    public void testFormatWithUrlRewrites() {
        ProductUrlFormat.Params params = new ProductUrlFormat.Params();
        params.setPage("/page/path");
        params.setUrlKey("bar");
        params.setUrlRewrites(Arrays.asList(
            new UrlRewrite().setUrl("foo"),
            new UrlRewrite().setUrl("foo/bar")));

        assertEquals("/page/path.html/foo/bar.html", subject.format(params));

        params.setVariantSku("variant");

        assertEquals("/page/path.html/foo/bar.html#variant", subject.format(params));
    }

    @Test
    public void testParse() {
        MockRequestPathInfo pathInfo = new MockRequestPathInfo();
        pathInfo.setResourcePath("/page/path");
        pathInfo.setSuffix("/foo-bar/foo-bar-product.html");
        ProductUrlFormat.Params parameters = subject.parse(pathInfo, null);

        assertEquals("/page/path", parameters.getPage());
        assertEquals("foo-bar/foo-bar-product", parameters.getUrlPath());
        assertEquals("foo-bar-product", parameters.getUrlKey());
        assertEquals("foo-bar", parameters.getCategoryUrlKey());
    }

    @Test
    public void testParseNoCategory() {
        MockRequestPathInfo pathInfo = new MockRequestPathInfo();
        pathInfo.setResourcePath("/page/path");
        pathInfo.setSuffix("/foo-bar.html");
        ProductUrlFormat.Params parameters = subject.parse(pathInfo, null);

        assertEquals("/page/path", parameters.getPage());
        assertEquals("foo-bar", parameters.getUrlKey());
        assertEquals("foo-bar", parameters.getUrlPath());
        assertNull(parameters.getCategoryUrlKey());
    }

    @Test
    public void testParseWithNestedCategory() {
        MockRequestPathInfo pathInfo = new MockRequestPathInfo();
        pathInfo.setResourcePath("/page/path");
        pathInfo.setSuffix("/foo-bar/sub/category/deep.html");
        ProductUrlFormat.Params parameters = subject.parse(pathInfo, null);

        assertEquals("/page/path", parameters.getPage());
        assertEquals("foo-bar/sub/category/deep", parameters.getUrlPath());
        assertEquals("deep", parameters.getUrlKey());
        assertEquals("category", parameters.getCategoryUrlKey());
    }

    @Test
    public void testParseNull() {
        ProductUrlFormat.Params parameters = subject.parse(null, null);
        assertNull(parameters.getPage());
        assertNull(parameters.getSku());
        assertNull(parameters.getUrlKey());
        assertNull(parameters.getUrlPath());
    }

    @Test
    public void testParseNoSuffix() {
        MockRequestPathInfo pathInfo = new MockRequestPathInfo();
        pathInfo.setResourcePath("/page/path");
        ProductUrlFormat.Params parameters = subject.parse(pathInfo, null);

        assertEquals("/page/path", parameters.getPage());
        assertNull(parameters.getUrlPath());
    }
}
