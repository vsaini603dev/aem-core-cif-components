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
import React from 'react';

import { render } from '@testing-library/react';

import ProductRecsGallery from '../ProductRecsGallery';
import mockMagentoStorefrontEvents from '../../../__test__/mockMagentoStorefrontEvents';
import ContextWrapper from '../../../__test__/context-wrapper';

const mockUseRecommendationsValue = jest.fn();

jest.mock('../../../hooks/useRecommendations', () => ({
    useRecommendations: () => mockUseRecommendationsValue()
}));

jest.mock('../../../hooks/useVisibilityObserver', () => ({
    useVisibilityObserver: () => ({ observeElement: jest.fn() })
}));

describe('ProductRecsGallery', () => {
    let mse;
    const units = [
        {
            unitId: 'my-unit-id',
            products: [
                {
                    sku: 'sku-a',
                    name: 'My Product A',
                    type: 'simple',
                    productId: 1,
                    currency: 'CHF',
                    prices: {
                        minimum: {
                            final: 342.23
                        },
                        maximum: {
                            final: 342.23
                        }
                    },
                    smallImage: {
                        url: 'http://localhost/product-a.jpg'
                    }
                },
                {
                    sku: 'sku-b',
                    name: 'My Product B',
                    type: 'simple',
                    productId: 2,
                    currency: 'CHF',
                    prices: {
                        minimum: {
                            final: 342.23
                        },
                        maximum: {
                            final: 2231.42
                        }
                    },
                    smallImage: {
                        url: 'http://localhost/product-b.png'
                    }
                }
            ]
        }
    ];

    beforeAll(() => {
        mse = window.magentoStorefrontEvents = mockMagentoStorefrontEvents;
    });

    beforeEach(() => {
        window.magentoStorefrontEvents.mockClear();
    });

    beforeEach(() => {
        mockUseRecommendationsValue.mockClear();
    });

    it('renders a loading state', () => {
        mockUseRecommendationsValue.mockReturnValue({ loading: true, units: null });

        const { asFragment } = render(
            <ProductRecsGallery title="My Product Recommendations" recommendationType="most-viewed" />
        );

        expect(asFragment()).toMatchSnapshot();
    });

    it.each([
        ['with add to wish list', true],
        ['without add to wish list', undefined]
    ])('renders a list of products (%s)', (_name, showAddToWishList) => {
        mockUseRecommendationsValue.mockReturnValue({
            loading: false,
            units
        });

        const { asFragment } = render(
            <ProductRecsGallery
                title="My Product Recommendations"
                recommendationType="most-viewed"
                showAddToWishList={showAddToWishList}
            />,
            { wrapper: ContextWrapper }
        );

        expect(asFragment()).toMatchSnapshot();
        expect(mse.publish.recsUnitRender).toHaveBeenCalledWith('my-unit-id');
    });

    it.each([
        ['without products', undefined],
        [
            'with products',
            [
                {
                    sku: 'sku-a',
                    name: 'My Product A',
                    type: 'simple',
                    productId: 1,
                    currency: 'CHF',
                    prices: {
                        minimum: {
                            final: 342.23
                        },
                        maximum: {
                            final: 342.23
                        }
                    },
                    smallImage: {
                        url: 'http://localhost/product-a.jpg'
                    }
                }
            ]
        ]
    ])('dispatches loaded event on host element (%s)', (_title, products) => {
        const dispatchEvent = jest.fn();
        const hostElement = { dispatchEvent };
        const units = products
            ? [
                  {
                      unitId: 'my-unit-id',
                      products
                  }
              ]
            : [];

        mockUseRecommendationsValue.mockReturnValue({
            loading: false,
            units
        });

        render(
            <ProductRecsGallery
                title="My Product Recommendations"
                recommendationType="most-viewed"
                hostElement={hostElement}
            />,
            { wrapper: ContextWrapper }
        );

        expect(dispatchEvent).toHaveBeenCalledTimes(1);
        expect(dispatchEvent.mock.calls[0][0].type).toBe('aem.cif.product-recs-loaded');
        expect(dispatchEvent.mock.calls[0][0].bubbles).toBe(true);
        expect(dispatchEvent.mock.calls[0][0].detail).toEqual(products || []);
    });
});
