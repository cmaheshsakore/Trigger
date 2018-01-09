package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet.BaseWalletFragment;
import com.trigger.launcher.fragment.ShopFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.ShopItemDescriptionFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ShopItem;

public class ShopItemDescriptionActivity extends FragmentActivity {

        private ShopItem mItem;
        private ShopItemDescriptionFragment mFragment;
        
        public static final String EXTRA_ITEM = "shop_item";
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            mItem = (ShopItem) getIntent().getParcelableExtra(EXTRA_ITEM);
            if (mItem == null) {
                finish();
            }
            
            
            mFragment = (ShopItemDescriptionFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
            if (mFragment == null) { 
                mFragment = new ShopItemDescriptionFragment();
                mFragment.setArguments(getIntent().getExtras());
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(android.R.id.content, mFragment);
                transaction.commit();
            }
        }
        
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            switch(requestCode) {
                case BaseWalletFragment.REQUEST_CODE_RESOLVE_CHANGE_MASKED_WALLET:
                case BaseWalletFragment.REQUEST_CODE_RESOLVE_ERR:
                case BaseWalletFragment.REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET:
                case BaseWalletFragment.REQUEST_CODE_RESOLVE_LOAD_MASKED_WALLET:
                case ShopFragment.REQUEST_WALLET_PURCHASE:
                    if (mFragment != null) {
                        mFragment.onActivityResult(requestCode, resultCode, data);
                    }
                    break;
            }
        }
}
