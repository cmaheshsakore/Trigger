/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet;

import android.content.Context;

import com.google.android.gms.wallet.Address;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.NotifyTransactionStatusRequest;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ShopItem;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Helper util methods.
 */
public class WalletUtil {
    private WalletUtil() {}
    
    /**
     * Formats the payment descriptions in a {@code MaskedWallet} for display.
     *
     * @param maskedWallet The wallet that contains the payment descriptions.
     * @return The payment descriptions in a format suitable for display to the user.
     */
    static String formatPaymentDescriptions(MaskedWallet maskedWallet) {
        StringBuilder sb = new StringBuilder();
        for (String description : maskedWallet.getPaymentDescriptions()) {
            sb.append(description);
            sb.append("\n");
        }
        if (sb.length() > 0) {
            // remove trailing newline
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    /**
     * Formats the address for display.
     *
     * @param context The context to get String resources from.
     * @param address The {@link Address} to format.
     * @return The address in a format suitable for display to the user.
     */
    static String formatAddress(Context context, Address address) {
        // different locales may need different address formats, which would be handled in
        // R.string.address_format
        String address2 = address.getAddress2().length() == 0 ?
                address.getAddress2() : address.getAddress2() + "\n";
        String address3 = address.getAddress3().length() == 0 ?
                address.getAddress3() : address.getAddress3() + "\n";
                
                String addressString = "";
                try {
                addressString = context.getString(R.string.address_format, address.getName(),
                        address.getAddress1(), address2, address3, address.getCity(), address.getState(),
                        address.getPostalCode());
                } catch (Exception e) {
                    e.printStackTrace();
                }
        return addressString;
    }

    /**
     * Formats a price for display.
     *
     * @param context The context to get String resources from.
     * @param priceMicros The price to display, in micros.
     * @return The given price in a format suitable for display to the user.
     */
    static String formatPrice(Context context, long priceMicros) {
        return context.getString(R.string.price_format, priceMicros / 1000000d);
    }
    
    static String formatPrice(Context context, double priceMicros) {
        return context.getString(R.string.price_format, priceMicros / 1000000d);
    }
    
    /**
     * Creates a MaskedWalletRequest
     * @param context The context to get string resources from
     * @param itemInfo {@link ItemInfo} containing details of an item
     * @return {@link MaskedWalletRequest} instance
     */
    public static MaskedWalletRequest createMaskedWalletRequest(Context context,
            double estimatedPrice) {
        return MaskedWalletRequest.newBuilder()
                .setMerchantName(RequestConstants.MERCHANT_NAME)
                .setPhoneNumberRequired(true)
                .setShippingAddressRequired(true)
                .setCurrencyCode(RequestConstants.CURRENCY_CODE_USD)
                .setEstimatedTotalPrice(WalletUtil.getPriceString(estimatedPrice))
                .build();
    }

    /**
     * @param context The context to get string resources from
     * @param itemInfo {@link ItemInfo} to use for creating the {@link FullWalletRequest}
     * @param googleTransactionId
     * @return {@link FullWalletRequest} instance
     */
    public static FullWalletRequest createFullWalletRequest(Context context,
            ShopItem itemInfo, String googleTransactionId, double shippingRate, TaxRate tax, double total) {

        double taxAmount = itemInfo.getPriceInDollars() * (tax.getRate() / 100);
        
        return FullWalletRequest.newBuilder()
                .setGoogleTransactionId(googleTransactionId)
                .setCart(Cart.newBuilder()
                        .setCurrencyCode(RequestConstants.CURRENCY_CODE_USD)
                        .setTotalPrice(getPriceString(total))
                        .addLineItem(LineItem.newBuilder()
                                .setCurrencyCode(RequestConstants.CURRENCY_CODE_USD)
                                .setDescription(itemInfo.getName())
                                .setQuantity("1")
                                .setUnitPrice(getPriceString(itemInfo.getPriceInDollars()))
                                .setTotalPrice(getPriceString(itemInfo.getPriceInDollars()))
                                .build())
                        .addLineItem(LineItem.newBuilder()
                                .setCurrencyCode(RequestConstants.CURRENCY_CODE_USD)
                                .setDescription(RequestConstants.DESCRIPTION_LINE_ITEM_SHIPPING)
                                .setRole(LineItem.Role.SHIPPING)
                                .setTotalPrice(getPriceString(shippingRate))
                                .build())
                        .addLineItem(LineItem.newBuilder()
                                .setCurrencyCode(RequestConstants.CURRENCY_CODE_USD)
                                .setDescription(RequestConstants.DESCRIPTION_LINE_ITEM_TAX)
                                .setRole(LineItem.Role.TAX)
                                .setTotalPrice(getPriceString(taxAmount))
                                .build())
                        .build())
                .build();
    }

    public static String getPriceString(double value) {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        DecimalFormat format = (DecimalFormat) nf;
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        //Logger.d("Returning price: " +format.format(value));
        return format.format(value);
    }

    /**
     * @param googleTransactionId
     * @return {@link NotifyTransactionStatusRequest} instance
     */
    public static NotifyTransactionStatusRequest createNotifyTransactionStatusRequest(
            String googleTransactionId, int status) {
        return NotifyTransactionStatusRequest.newBuilder()
                .setGoogleTransactionId(googleTransactionId)
                .setStatus(status)
                .build();
    }    
}
