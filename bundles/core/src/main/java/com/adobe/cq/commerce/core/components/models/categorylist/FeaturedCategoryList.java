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
package com.adobe.cq.commerce.core.components.models.categorylist;

import java.util.List;

import org.osgi.annotation.versioning.ConsumerType;

import com.adobe.cq.commerce.core.components.models.retriever.AbstractCategoriesRetriever;
import com.adobe.cq.commerce.magento.graphql.CategoryTree;
import com.adobe.cq.wcm.core.components.models.Component;

/**
 * Provides the list of categories to CategoryList Componenet.
 */
@ConsumerType
public interface FeaturedCategoryList extends Component {

    /**
     * Returns the categories data in a list from Magento depending on configurations.
     *
     * @return {@code  List<CategoryInterface>}
     */
    List<CategoryTree> getCategories();

    /**
     * Returns a list of category identifiers configured for this component
     * 
     * @return a {@code List} of {@code CategoryListItem} objects or an empty list if no categories are configured
     */
    List<FeaturedCategoryListItem> getCategoryItems();

    /**
     * Returns in instance of the category retriever for fetching category data via GraphQL.
     *
     * @return category retriever instance
     */
    AbstractCategoriesRetriever getCategoriesRetriever();

    /**
     * Returns true if the component is correctly configured, false otherwise.
     *
     * @return true or false
     */
    boolean isConfigured();

    /**
     * Should return the HTML tag type for the component title.
     * 
     * @return The HTML tag type that should be used to display the component title.
     */
    String getTitleType();
}
