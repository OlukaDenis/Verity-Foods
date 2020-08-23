package com.verityfoods.ui.faqs;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.verityfoods.R;
import com.verityfoods.utils.Vars;

public class FaqsFragment extends Fragment {
    private Vars vars;
    private NavController navController;

    public FaqsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_faqs, container, false);
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        vars = new Vars(requireActivity());



        return root;
    }
}