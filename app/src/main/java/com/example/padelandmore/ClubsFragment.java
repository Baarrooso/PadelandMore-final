package com.example.padelandmore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ClubsFragment extends Fragment implements ClubsAdapter.OnClubClickListener {

    private RecyclerView recyclerClubes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clubs, container, false);

        recyclerClubes = view.findViewById(R.id.recyclerClubes);

        // Botón atrás
        View btnBack = view.findViewById(R.id.btnBackClubs);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        List<Club> clubes = new ArrayList<>();
        clubes.add(new Club("1", "Club Padel Madrid", "Madrid, Centro", ""));
        clubes.add(new Club("2", "Club Padel Alcalá", "Alcalá de Henares", ""));
        clubes.add(new Club("3", "Club Padel Getafe", "Getafe, Sur", ""));
        clubes.add(new Club("4", "Club Premium Tenis", "Madrid, Chamberí", ""));

        ClubsAdapter adapter = new ClubsAdapter(clubes, this);
        recyclerClubes.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerClubes.setAdapter(adapter);

        return view;
    }

    @Override
    public void onClubClick(Club club) {
        // Abrir ReservarPistaFragment pasando el club seleccionado
        ReservarPistaFragment fragment = new ReservarPistaFragment();
        Bundle args = new Bundle();
        args.putString("clubId", club.getId());
        args.putString("clubName", club.getNombre());
        fragment.setArguments(args);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}

