package com.verityfoods.ui.orders;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.verityfoods.R;
import com.verityfoods.data.adapters.OrderItemsAdapter;
import com.verityfoods.data.model.Address;
import com.verityfoods.data.model.Cart;
import com.verityfoods.data.model.Category;
import com.verityfoods.data.model.Order;
import com.verityfoods.data.model.User;
import com.verityfoods.utils.AppUtils;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrderDetailFragment extends Fragment {
    private static final String TAG = "OrderDetailFragment";
    private Order order;
    private User user;
    private Address address;
    private Vars vars;
    private List<Cart> orderItems;
    private LinearLayoutManager layoutManager;

    @BindView(R.id.detail_order_no)
    TextView orderNumber;

    @BindView(R.id.detail_order_date)
    TextView orderDate;

    @BindView(R.id.detail_order_delivery_method)
    TextView deliveryMethod;

    @BindView(R.id.detail_order_total)
    TextView orderTotal;

    @BindView(R.id.detail_order_status)
    TextView orderStatus;

    @BindView(R.id.detail_order_payment_method)
    TextView paymentMethod;

    @BindView(R.id.detail_order_count)
    TextView orderCount;

    @BindView(R.id.detail_order_address_user)
    TextView addressUserName;

    @BindView(R.id.detail_order_address_name)
    TextView addressName;

    @BindView(R.id.detail_order_address_phone)
    TextView addressUserPhone;

    @BindView(R.id.detail_order_items_recycler)
    RecyclerView orderItemsRecycler;

    public OrderDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_order_detail, container, false);

        vars = new Vars(requireActivity());
        ButterKnife.bind(this, root);

        Bundle bundle = getArguments();
        assert bundle != null;
        order = (Order) bundle.getSerializable(Globals.ORDER_OBJ);

        layoutManager = new LinearLayoutManager(requireContext());
        orderItemsRecycler.setLayoutManager(layoutManager);

        populateOderDetails();
        return root;
    }

    private void populateOderDetails() {
        orderItems = order.getProducts();
        user = order.getUser();
        address = order.getAddress();

        int size = orderItems.size();
        String m = size + " items";
        orderCount.setText(m);

        String num = "ORDER NO: " + order.getOrder_number();
        orderNumber.setText(num);

        orderDate.setText(order.getDateAdded());
        orderTotal.setText(AppUtils.formatCurrency(order.getTotal()));
        orderStatus.setText(order.getStatus());
        paymentMethod.setText(order.getPaymentMethod());
        deliveryMethod.setText(order.getDeliveryMethod());

        addressUserName.setText(address.getName());
        addressUserPhone.setText(address.getPhone());
        addressName.setText(address.getAddress());


        OrderItemsAdapter adapter  = new OrderItemsAdapter(orderItems, getContext());
        orderItemsRecycler.setAdapter(adapter);
    }
}