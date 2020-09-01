package com.verityfoods.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.verityfoods.R;
import com.verityfoods.data.model.Variable;

public class VariableViewHolder extends RecyclerView.ViewHolder {
    public TextView variableName;
    private Context context;

    public VariableViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        this.context = context;

        variableName = itemView.findViewById(R.id.variable_name);
    }

    public void bindVariable(Variable variable) {
        variableName.setText(variable.getQty());
        if (variable.isPreferred()) {
            variableName.setBackground(context.getResources().getDrawable(R.drawable.variable_filled_bg));
            variableName.setTextColor(context.getResources().getColor(R.color.white));
        }
    }
}
