package ovh.snacking.snacking.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;
import io.realm.Realm;
import io.realm.RealmResults;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.model.Product;
import ovh.snacking.snacking.model.ProductAndGroupBinding;
import ovh.snacking.snacking.model.ProductGroup;

/**
 * Created by ACER on 07/02/2017.
 */

public class TabFragmentRecyclerViewProduct extends Fragment {
    private Realm realm;
    private SectionedRecyclerViewAdapter mSectionAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        realm = RealmSingleton.getInstance(getContext()).getRealm();

        // Create an instance of SectionedRecyclerViewAdapter
        mSectionAdapter = new SectionedRecyclerViewAdapter();

        // Add your Sections
        populateAdapter();

        // Set up your RecyclerView with the SectionedRecyclerViewAdapter
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        GridLayoutManager glm = new GridLayoutManager(getContext(), 4);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(mSectionAdapter.getSectionItemViewType(position)) {
                    case SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER:
                        return 4;
                    default:
                        return 1;
                }
            }
        });
        recyclerView.setLayoutManager(glm);
        recyclerView.setAdapter(mSectionAdapter);
        return view;
    }

    private void populateAdapter() {

        RealmResults<ProductGroup> productGroup = realm.where(ProductGroup.class).findAllSorted("position");

        for (ProductGroup group : productGroup) {
            List<Product> products = getProductInGroup(group);
            if (products.size() > 0) {
                mSectionAdapter.addSection(new ProductSection(String.valueOf(group.getName()), products));
            }
        }

    }

    private List<Product> getProductInGroup(ProductGroup group) {
        List<Product> products = new ArrayList<>();

        RealmResults<ProductAndGroupBinding> productAndGroupBindings = realm.where(ProductAndGroupBinding.class).equalTo("group.id", group.getId()).findAllSorted("position");
        for (ProductAndGroupBinding customerAndGroupBinding : productAndGroupBindings) {
            products.add(customerAndGroupBinding.getProduct());
        }

        return products;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private class ProductSection extends StatelessSection {

        String mTitle;
        List<Product> mList;

        public ProductSection(String title, List<Product> list) {
            // call constructor with layout resources for this Section header and items
            super(R.layout.section_header, R.layout.card_view_product_item);
            this.mTitle = title;
            this.mList = list;
        }

        @Override
        public int getContentItemsTotal() {
            return mList.size(); // number of items of this section
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            // return a custom instance of ViewHolder for the items of this section
            return new ProductSection.ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {

            final ProductSection.ItemViewHolder itemHolder = (ProductSection.ItemViewHolder) holder;

            // bind your view here
            final Product selectedProduct = mList.get(position);
            itemHolder.prodRef.setText(String.valueOf(selectedProduct.getRef()));
            itemHolder.prodLabel.setText(String.valueOf(selectedProduct.getLabel()));

            itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((EditingInvoiceFragment) getParentFragment()).onProductSelected(selectedProduct);
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new ProductSection.HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            ProductSection.HeaderViewHolder headerHolder = (ProductSection.HeaderViewHolder) holder;
            headerHolder.tvTitle.setText(mTitle);
        }



        private class HeaderViewHolder extends RecyclerView.ViewHolder {

            private final TextView tvTitle;

            public HeaderViewHolder(View view) {
                super(view);
                tvTitle = (TextView) view.findViewById(R.id.section_header_title);
            }
        }

        private class ItemViewHolder extends RecyclerView.ViewHolder {

            private final View rootView;
            private final TextView prodRef;
            private final TextView prodLabel;

            private ItemViewHolder(View view) {
                super(view);
                rootView = view;
                prodRef = (TextView) view.findViewById(R.id.product_ref);
                prodLabel = (TextView) view.findViewById(R.id.product_label);
            }
        }
    }
}
