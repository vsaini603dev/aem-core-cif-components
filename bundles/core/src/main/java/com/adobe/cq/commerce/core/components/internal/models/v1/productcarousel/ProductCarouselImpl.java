/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2019 Adobe
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
package com.adobe.cq.commerce.core.components.internal.models.v1.productcarousel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.commerce.core.components.client.MagentoGraphqlClient;
import com.adobe.cq.commerce.core.components.internal.models.v1.common.ProductListItemImpl;
import com.adobe.cq.commerce.core.components.internal.models.v1.common.TitleTypeProvider;
import com.adobe.cq.commerce.core.components.models.common.CommerceIdentifier;
import com.adobe.cq.commerce.core.components.models.common.ProductListItem;
import com.adobe.cq.commerce.core.components.models.productcarousel.ProductCarousel;
import com.adobe.cq.commerce.core.components.models.retriever.AbstractProductsRetriever;
import com.adobe.cq.commerce.core.components.services.urls.UrlProvider;
import com.adobe.cq.commerce.core.components.utils.SiteNavigation;
import com.adobe.cq.commerce.magento.graphql.ConfigurableProduct;
import com.adobe.cq.commerce.magento.graphql.ConfigurableVariant;
import com.adobe.cq.commerce.magento.graphql.ProductInterface;
import com.adobe.cq.commerce.magento.graphql.SimpleProduct;
import com.adobe.cq.commerce.magento.graphql.UrlRewrite;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.designer.Style;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Model(
    adaptables = SlingHttpServletRequest.class,
    adapters = { ProductCarousel.class, ComponentExporter.class },
    resourceType = ProductCarouselImpl.RESOURCE_TYPE)
@Exporter(
    name = ExporterConstants.SLING_MODEL_EXPORTER_NAME,
    extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class ProductCarouselImpl extends ProductCarouselBase implements ProductCarousel {

    protected static final String RESOURCE_TYPE = "core/cif/components/commerce/productcarousel/v1/productcarousel";
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCarouselImpl.class);

    @Self(injectionStrategy = InjectionStrategy.OPTIONAL)
    private MagentoGraphqlClient magentoGraphqlClient;

    @ValueMapValue(
        name = "product",
        injectionStrategy = InjectionStrategy.OPTIONAL)
    private String[] productSkuList;

    @ScriptVariable
    private Page currentPage;

    @OSGiService
    private UrlProvider urlProvider;

    @ScriptVariable
    protected Style currentStyle;

    private Page productPage;
    private List<String> baseProductSkus = Collections.emptyList();

    private AbstractProductsRetriever productsRetriever;

    @PostConstruct
    private void initModel() {

        if (!isConfigured()) {
            return;
        }

        List<String> productSkus = Arrays.asList(productSkuList);
        productPage = SiteNavigation.getProductPage(currentPage);
        if (productPage == null) {
            productPage = currentPage;
        }

        // Make sure we use the base product sku for each selected product (can be a variant)
        baseProductSkus = productSkus
            .stream()
            .map(s -> s.startsWith("/") ? StringUtils.substringAfterLast(s, "/") : s)
            .map(s -> SiteNavigation.toProductSkus(s).getLeft())
            .distinct()
            .collect(Collectors.toList());

        if (magentoGraphqlClient == null) {
            LOGGER.error("Cannot get a GraphqlClient using the resource at {}", resource.getPath());
        } else {
            productsRetriever = new ProductsRetriever(magentoGraphqlClient);
            productsRetriever.setIdentifiers(baseProductSkus);
        }
    }

    @Override
    public boolean isConfigured() {
        return productSkuList != null;
    }

    @Override
    @JsonIgnore
    @Nonnull
    public List<ProductListItem> getProducts() {
        if (productsRetriever == null) {
            return Collections.emptyList();
        }

        List<ProductInterface> products = productsRetriever.fetchProducts();
        Collections.sort(products, Comparator.comparing(item -> baseProductSkus.indexOf(item.getSku())));

        List<ProductListItem> carouselProductList = new ArrayList<>();
        if (!products.isEmpty()) {
            for (String combinedSku : productSkuList) {

                if (combinedSku.startsWith("/")) {
                    combinedSku = StringUtils.substringAfterLast(combinedSku, "/");
                }

                Pair<String, String> skus = SiteNavigation.toProductSkus(combinedSku);
                ProductInterface product = products.stream().filter(p -> p.getSku().equals(skus.getLeft())).findFirst().orElse(null);
                if (product == null) {
                    continue; // Can happen that a product is not found
                }

                // retain urlKey, urlPath and urlRewrites from the base product
                String urlKey = product.getUrlKey();
                String urlPath = product.getUrlPath();
                List<UrlRewrite> urlRewrites = product.getUrlRewrites();
                if (skus.getRight() != null && product instanceof ConfigurableProduct) {
                    SimpleProduct variant = findVariant((ConfigurableProduct) product, skus.getRight());
                    if (variant != null) {
                        product = variant;
                    }
                }

                try {
                    ProductListItemImpl.Builder builder = new ProductListItemImpl.Builder(getId(), productPage, request, urlProvider)
                        .product(product)
                        .image(product.getThumbnail())
                        .sku(skus.getLeft())
                        .urlKey(urlKey)
                        .urlPath(urlPath)
                        .urlRewrites(urlRewrites)
                        .variantSku(skus.getRight());
                    carouselProductList.add(builder.build());
                } catch (Exception e) {
                    LOGGER.error("Failed to instantiate product " + combinedSku, e);
                }
            }
        }
        return carouselProductList;
    }

    @Override
    @JsonIgnore
    public AbstractProductsRetriever getProductsRetriever() {
        return productsRetriever;
    }

    protected SimpleProduct findVariant(ConfigurableProduct configurableProduct, String variantSku) {
        List<ConfigurableVariant> variants = configurableProduct.getVariants();
        if (variants == null || variants.isEmpty()) {
            return null;
        }
        return variants.stream().map(v -> v.getProduct()).filter(sp -> variantSku.equals(sp.getSku())).findFirst().orElse(null);
    }

    @Override
    public String getTitleType() {
        return TitleTypeProvider.getTitleType(currentStyle, resource.getValueMap());
    }

    @JsonProperty("productIdentifiers")
    public List<CommerceIdentifier> getProductCommerceIdentifiers() {
        return baseProductSkus.stream().map(ListItemIdentifier::new).collect(Collectors.toList());
    }

    @Override
    @Deprecated
    @JsonIgnore
    @Nonnull
    public List<ProductListItem> getProductIdentifiers() {
        return baseProductSkus.stream()
            .map(ListItemIdentifier::new)
            .map(id -> new ProductListItemImpl(id, getId(), productPage))
            .collect(Collectors.toList());
    }

    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}
