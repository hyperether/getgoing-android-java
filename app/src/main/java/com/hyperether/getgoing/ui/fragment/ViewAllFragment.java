package com.hyperether.getgoing.ui.fragment;

import static androidx.navigation.ui.NavigationUI.navigateUp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.databinding.FragmentViewAllBinding;
import com.hyperether.getgoing.repository.room.entity.DbRoute;
import com.hyperether.getgoing.ui.adapter.RouteAdapter;
import com.hyperether.getgoing.viewmodel.AllRouteViewModel;

import java.util.List;

public class ViewAllFragment extends Fragment {
    private NavController navigationController;

    private RouteAdapter routeAdapter;
    private FragmentViewAllBinding binding;
    private AllRouteViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewAllBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        setupRecyclerView();
//        setupViewModel();
//        observeViewModel();
    }

//    public void navigateUp() {
//        Toast.makeText(requireContext(), "Provera", Toast.LENGTH_SHORT).show();
//        //NavHostFragment.findNavController(this).navigateUp();
//    }

//    private void setupRecyclerView() {
//        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
//       ((LinearLayoutManager) layoutManager).setOrientation(RecyclerView.HORIZONTAL);
//        binding.rvAllRoutes.setLayoutManager(layoutManager);
//
//    }
//
//    private void setupViewModel() {
//        viewModel = new ViewModelProvider(this).get(AllRouteViewModel.class);
//    }
//
//    private void observeViewModel() {
//        LiveData<List<DbRoute>> liveData = viewModel.getAllRoutes();
//
//        // Provera da li je LiveData različit od null pre dodavanja observera
//        if (liveData != null) {
//            // Observing LiveData za listu ruta
//            liveData.observe(getViewLifecycleOwner(), routes -> {
//                // Ažuriranje RecyclerView-a kada se podaci promene
//                routeAdapter = new RouteAdapter(routes);
//                binding.rvAllRoutes.setAdapter(routeAdapter);
//                updateRouteAdapter(routes);
//            });
//        }
//    }
//
//    private void updateRouteAdapter(List<DbRoute> routes) {
//        // Ažuriranje RecyclerView-a kada se podaci promene
//        if (routeAdapter == null) {
//            routeAdapter = new RouteAdapter(routes);
//            binding.rvAllRoutes.setAdapter(routeAdapter);
//        } else {
//            routeAdapter.updateData(routes);
//        }
//    }
}
