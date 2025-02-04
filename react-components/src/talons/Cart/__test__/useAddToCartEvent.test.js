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
import useAddToCartEvent from '../useAddToCartEvent';
import { render, wait } from '../../../utils/test-utils';

describe('useAddToCartEvent', () => {
    const dispatchEvent = items =>
        document.dispatchEvent(
            new CustomEvent('aem.cif.add-to-cart', {
                detail: items
            })
        );

    const MockComponent = props => {
        useAddToCartEvent(props);
        return <></>;
    };

    it('calls fallback if no items are in the event details', async () => {
        // given
        const fallback = jest.fn();

        // when
        render(<MockComponent fallbackHandler={fallback} />);
        dispatchEvent([]);

        // then
        await wait(() => expect(fallback).toHaveBeenCalledTimes(1));
    });

    it('calls addToCartApi#addPhysicalProductItems if only non-virtual items', async () => {
        // given
        const addPhysicalProductItems = jest.fn();

        // when
        render(<MockComponent addToCartApi={{ addPhysicalProductItems }} />);
        dispatchEvent([{ virtual: false, sku: 'sample', quantity: '1.0' }]);

        // then
        await wait(() => {
            expect(addPhysicalProductItems).toHaveBeenCalledTimes(1);
            expect(addPhysicalProductItems).toHaveBeenCalledWith([
                {
                    data: { sku: 'sample', quantity: 1.0 }
                }
            ]);
        });
    });

    it('calls addToCartApi#addPhysicalAndVirtualProductItems if virtual and non-virtual items', async () => {
        // given
        const addPhysicalAndVirtualProductItems = jest.fn();

        // when
        render(<MockComponent addToCartApi={{ addPhysicalAndVirtualProductItems }} />);
        dispatchEvent([
            { virtual: false, sku: 'physical', quantity: '1' },
            { virtual: true, sku: 'virtual', quantity: '1.5' }
        ]);

        // then
        await wait(() => {
            expect(addPhysicalAndVirtualProductItems).toHaveBeenCalledTimes(1);
            expect(addPhysicalAndVirtualProductItems).toHaveBeenCalledWith(
                [
                    {
                        data: { sku: 'physical', quantity: 1.0 }
                    }
                ],
                [
                    {
                        data: { sku: 'virtual', quantity: 1.5 }
                    }
                ]
            );
        });
    });

    it('calls addToCartApi#addVirtualProductItems if only virtual items', async () => {
        // given
        const addVirtualProductItems = jest.fn();

        // when
        render(<MockComponent addToCartApi={{ addVirtualProductItems }} />);
        dispatchEvent([{ virtual: true, sku: 'virtual', quantity: '1.5' }]);

        // then
        await wait(() => {
            expect(addVirtualProductItems).toHaveBeenCalledTimes(1);
            expect(addVirtualProductItems).toHaveBeenCalledWith([
                {
                    data: { sku: 'virtual', quantity: 1.5 }
                }
            ]);
        });
    });

    it('calls addToCartApi#addBundledProductItems if bundled product items', async () => {
        // given
        const addBundledProductItems = jest.fn();

        // when
        render(<MockComponent addToCartApi={{ addBundledProductItems }} />);
        dispatchEvent([
            {
                sku: 'bundle',
                quantity: '1',
                bundle: true,
                options: [
                    {
                        id: 'foo',
                        quantity: 1,
                        value: ['1']
                    }
                ]
            }
        ]);

        // then
        await wait(() => {
            expect(addBundledProductItems).toHaveBeenCalledTimes(1);
            expect(addBundledProductItems).toHaveBeenCalledWith([
                {
                    data: {
                        sku: 'bundle',
                        quantity: 1
                    },
                    bundle_options: [
                        {
                            id: 'foo',
                            quantity: 1,
                            value: ['1']
                        }
                    ]
                }
            ]);
        });
    });

    it('calls addToCartApi#addGiftCardProductItems if gift card product items', async () => {
        // given
        const addGiftCardProductItems = jest.fn();

        // when
        render(<MockComponent addToCartApi={{ addGiftCardProductItems }} />);
        dispatchEvent([
            {
                sku: 'gift-card',
                virtual: false,
                giftCard: true,
                quantity: 1,
                entered_options: [
                    {
                        uid: 'Z2lmdGNhcmQvZ2lmdGNhcmRfc2VuZGVyX25hbWU=',
                        value: 'bla'
                    },
                    {
                        uid: 'Z2lmdGNhcmQvZ2lmdGNhcmRfc2VuZGVyX2VtYWls',
                        value: 'bla'
                    },
                    {
                        uid: 'Z2lmdGNhcmQvZ2lmdGNhcmRfcmVjaXBpZW50X25hbWU=',
                        value: 'bla'
                    },
                    {
                        uid: 'Z2lmdGNhcmQvZ2lmdGNhcmRfcmVjaXBpZW50X2VtYWls',
                        value: 'bla'
                    },
                    {
                        uid: 'Z2lmdGNhcmQvZ2lmdGNhcmRfbWVzc2FnZQ==',
                        value: 'da'
                    }
                ],
                selected_options: ['Z2lmdGNhcmQvZ2lmdGNhcmRfYW1vdW50LzEyLjAwMDA=']
            }
        ]);

        // then
        await wait(() => {
            expect(addGiftCardProductItems).toHaveBeenCalledTimes(1);
            expect(addGiftCardProductItems).toHaveBeenCalledWith([
                {
                    sku: 'gift-card',
                    quantity: 1,
                    entered_options: [
                        {
                            uid: 'Z2lmdGNhcmQvZ2lmdGNhcmRfc2VuZGVyX25hbWU=',
                            value: 'bla'
                        },
                        {
                            uid: 'Z2lmdGNhcmQvZ2lmdGNhcmRfc2VuZGVyX2VtYWls',
                            value: 'bla'
                        },
                        {
                            uid: 'Z2lmdGNhcmQvZ2lmdGNhcmRfcmVjaXBpZW50X25hbWU=',
                            value: 'bla'
                        },
                        {
                            uid: 'Z2lmdGNhcmQvZ2lmdGNhcmRfcmVjaXBpZW50X2VtYWls',
                            value: 'bla'
                        },
                        {
                            uid: 'Z2lmdGNhcmQvZ2lmdGNhcmRfbWVzc2FnZQ==',
                            value: 'da'
                        }
                    ],
                    selected_options: ['Z2lmdGNhcmQvZ2lmdGNhcmRfYW1vdW50LzEyLjAwMDA=']
                }
            ]);
        });
    });
});
