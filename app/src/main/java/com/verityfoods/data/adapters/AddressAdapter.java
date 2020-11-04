package com.verityfoods.data.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.verityfoods.R;
import com.verityfoods.data.interfaces.ItemClickListener;
import com.verityfoods.data.model.Address;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddressAdapter extends FirestoreRecyclerAdapter<Address, AddressAdapter.AddressViewHolder> {
    private static final String TAG = "AddressAdapter";
    private ItemClickListener itemClickListener;
    private Activity activity;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public AddressAdapter(@NonNull FirestoreRecyclerOptions<Address> options, Activity context) {
        super(options);
        this.activity = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull AddressViewHolder holder, int position, @NonNull Address model) {
        holder.bindAddressTo(model);
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_address, parent, false);
        return new AddressAdapter.AddressViewHolder(view);
    }


    public class AddressViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_address_name)
        TextView addressName;

        @BindView(R.id.tv_address_region)
        TextView addressRegion;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                    itemClickListener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }

        public void bindAddressTo(Address address) {
            addressName.setText(address.getName());
            addressRegion.setText(address.getAddress());
        }
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
