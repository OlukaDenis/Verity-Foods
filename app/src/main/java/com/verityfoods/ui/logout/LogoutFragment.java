package com.verityfoods.ui.logout;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.verityfoods.MainActivity;
import com.verityfoods.R;
import com.verityfoods.ui.auth.AuthChooser;
import com.verityfoods.utils.Vars;

public class LogoutFragment extends Fragment {
    private Vars vars;
    private NavController navController;

    public LogoutFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_logout, container, false);
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        vars = new Vars(requireActivity());



        return root;
    }
}