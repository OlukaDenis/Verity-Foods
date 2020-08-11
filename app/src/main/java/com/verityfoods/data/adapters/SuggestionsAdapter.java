package com.verityfoods.data.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.cursoradapter.widget.CursorAdapter;

import com.verityfoods.R;
import com.verityfoods.data.model.Product;

import java.util.ArrayList;

public class SuggestionsAdapter extends CursorAdapter {
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private SearchView searchView;
    private ArrayList<Product> products;

    public SuggestionsAdapter(Context context, Cursor c, boolean autoRequery, SearchView searchView, ArrayList<Product> products) {
        super(context, c, autoRequery);
        this.searchView = searchView;
        this.products =products;
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.layout_search_list, parent, false);
        return view;
    }

    @Override
    public View getView(int position, View convertview, ViewGroup arg2) {
        if (convertview == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertview = inflater.inflate(R.layout.layout_search_list,
                    null);
        }
        convertview.setTag(position);
        return super.getView(position, convertview, arg2);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String title=cursor.getString(cursor.getColumnIndex("text"));
        TextView textView=view.findViewById(R.id.tv_search_item);
        textView.setText(title);
        view.setOnClickListener(v -> {
            int id = (Integer) view.getTag();//here is the position
            Product product = products.get(id);
//            Intent detailIntent = new Intent(mContext, MovieDetailActivity.class);
//            //save temporary the movie details
//            MovieCache.movieId = products.get(id).getId();
//
//            detailIntent.putExtra("movie", selectedMovie);
//            // detailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            mContext.startActivity(detailIntent);
            Toast.makeText(context, product.getName(), Toast.LENGTH_SHORT).show();
        });
    }
}