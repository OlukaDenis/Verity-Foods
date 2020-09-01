package com.verityfoods.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.verityfoods.R;
import com.verityfoods.data.model.Variable;

public class VariableViewHolder extends RecyclerView.ViewHolder {
    public TextView variableName;

    public VariableViewHolder(@NonNull View itemView) {
        super(itemView);
        variableName = itemView.findViewById(R.id.variable_name);
    }

    public void bindVariable(Variable variable) {
        variableName.setText(variable.getQty());
    }
}
